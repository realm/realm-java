/*
 * Copyright 2016 Realm Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include "io_realm_mongodb_sync_Sync.h"

#include <impl/realm_coordinator.hpp>
#include <sync/app.hpp>
#include <sync/sync_manager.hpp>
#include <sync/sync_session.hpp>
#include <binding_callback_thread_observer.hpp>

#include "util.hpp"
#include <jni_util/bson_util.hpp>
#include "jni_util/java_class.hpp"
#include "jni_util/java_method.hpp"
#include "jni_util/jni_utils.hpp"

using namespace realm;
using namespace realm::jni_util;
using namespace realm::util;

JNIEXPORT void JNICALL Java_io_realm_mongodb_sync_Sync_nativeReset(JNIEnv* env, jclass, jlong j_app_ptr)
{
    try {
        auto app = *reinterpret_cast<std::shared_ptr<app::App>*>(j_app_ptr);
        app->sync_manager()->reset_for_testing();
        app::App::clear_cached_apps();
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_mongodb_sync_Sync_nativeSimulateSyncError(JNIEnv* env, jclass, jlong j_app_ptr, jstring local_realm_path,
                                                                       jint err_code, jstring err_message,
                                                                       jboolean is_fatal)
{
    try {
        auto app = *reinterpret_cast<std::shared_ptr<app::App>*>(j_app_ptr);
        JStringAccessor path(env, local_realm_path);
        JStringAccessor message(env, err_message);

        auto session = app->sync_manager()->get_existing_active_session(path);
        if (!session) {
            ThrowException(env, IllegalArgument, concat_stringdata("Session not found: ", path));
            return;
        }
        std::error_code code = std::error_code{static_cast<int>(err_code), realm::sync::protocol_error_category()};
        SyncSession::OnlyForTesting::handle_error(*session, {code, std::string(message), to_bool(is_fatal)});
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_mongodb_sync_Sync_nativeReconnect(JNIEnv* env, jclass, jlong j_app_ptr)
{
    try {
        auto app = *reinterpret_cast<std::shared_ptr<app::App>*>(j_app_ptr);
        app->sync_manager()->reconnect();
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_mongodb_sync_Sync_nativeCreateSession(JNIEnv* env, jclass, jlong j_native_config_ptr)
{
    try {
        auto& config = *reinterpret_cast<Realm::Config*>(j_native_config_ptr);
        _impl::RealmCoordinator::get_coordinator(config)->create_session(config);
    }
    CATCH_STD()
}

JNIEXPORT jstring JNICALL Java_io_realm_mongodb_sync_Sync_nativeGetPathForRealm(JNIEnv* env,
                                                                                jclass,
                                                                                jlong j_app_ptr,
                                                                                jstring j_user_id,
                                                                                jstring j_encoded_partition_value,
                                                                                jstring j_override_filename)
{
    try {
        // This is a little bit of a hack. Normally Realm Java doesn't generate the C++ SyncConfig
        // until the Realm is opened, but the Sync API for creating the Realm path require that
        // it is created up front. So we cheat and create a SyncConfig with the minimal values
        // needed for the path to be calculated.
        auto app = *reinterpret_cast<std::shared_ptr<app::App>*>(j_app_ptr);
        JStringAccessor user_id(env, j_user_id);
        std::shared_ptr<SyncUser> user = app->sync_manager()->get_existing_logged_in_user(user_id);
        if (!user) {
            throw std::logic_error("User is not logged in");
        }
        Bson bson(JniBsonProtocol::jstring_to_bson(env, j_encoded_partition_value));
        std::stringstream buffer;
        buffer << bson;
        SyncConfig config{user, buffer.str()};
        util::Optional<std::string> file_name = util::none;
        if (j_override_filename != nullptr) {
            JStringAccessor override_file_name(env, j_override_filename);
            file_name = std::string(override_file_name);
        }
        return to_jstring(env, app->sync_manager()->path_for_realm(config, file_name));
    }
    CATCH_STD()
    return nullptr;
}
