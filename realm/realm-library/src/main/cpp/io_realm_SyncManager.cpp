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

#include "io_realm_SyncManager.h"

#include <object-store/src/impl/realm_coordinator.hpp>
#include <sync/sync_manager.hpp>
#include <sync/sync_session.hpp>
#include <binding_callback_thread_observer.hpp>

#include "util.hpp"
#include "jni_util/java_class.hpp"
#include "jni_util/java_method.hpp"
#include "jni_util/jni_utils.hpp"

using namespace realm;
using namespace realm::jni_util;
using namespace realm::util;

JNIEXPORT void JNICALL Java_io_realm_RealmSync_nativeReset(JNIEnv* env, jclass)
{
    try {
        SyncManager::shared().reset_for_testing();
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_RealmSync_nativeSimulateSyncError(JNIEnv* env, jclass, jstring local_realm_path,
                                                                       jint err_code, jstring err_message,
                                                                       jboolean is_fatal)
{
    try {
        JStringAccessor path(env, local_realm_path);
        JStringAccessor message(env, err_message);

        auto session = SyncManager::shared().get_existing_active_session(path);
        if (!session) {
            ThrowException(env, IllegalArgument, concat_stringdata("Session not found: ", path));
            return;
        }
        std::error_code code = std::error_code{static_cast<int>(err_code), realm::sync::protocol_error_category()};
        SyncSession::OnlyForTesting::handle_error(*session, {code, std::string(message), to_bool(is_fatal)});
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_RealmSync_nativeReconnect(JNIEnv* env, jclass)
{
    try {
        SyncManager::shared().reconnect();
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_RealmSync_nativeCreateSession(JNIEnv* env, jclass, jlong j_native_config_ptr)
{
    try {
        auto& config = *reinterpret_cast<Realm::Config*>(j_native_config_ptr);
        _impl::RealmCoordinator::get_coordinator(config)->create_session(config);
    }
    CATCH_STD()
}