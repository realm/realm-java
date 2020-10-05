/*
 * Copyright 2020 Realm Inc.
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

#include "io_realm_mongodb_User.h"

#include "java_network_transport.hpp"
#include "util.hpp"
#include "jni_util/java_method.hpp"
#include "jni_util/jni_utils.hpp"

#include <realm/object-store/sync/app.hpp>
#include <jni_util/bson_util.hpp>

using namespace realm;
using namespace realm::app;
using namespace realm::jni_util;
using namespace realm::_impl;

JNIEXPORT void JNICALL Java_io_realm_mongodb_User_nativeLinkUser(JNIEnv* env,
                                                             jclass,
                                                             jlong j_app_ptr,
                                                             jlong j_user_ptr,
                                                             jlong j_credentials_ptr,
                                                             jobject j_callback)
{
    try {
        auto app = *reinterpret_cast<std::shared_ptr<App>*>(j_app_ptr);
        auto user = *reinterpret_cast<std::shared_ptr<SyncUser>*>(j_user_ptr);
        auto credentials = reinterpret_cast<AppCredentials*>(j_credentials_ptr);
        std::function<jobject(JNIEnv*, std::shared_ptr<SyncUser>)> mapper = [](JNIEnv* env, std::shared_ptr<SyncUser> user) {
            auto* java_user = new std::shared_ptr<SyncUser>(std::move(user));
            return JavaClassGlobalDef::new_long(env, reinterpret_cast<int64_t>(java_user));
        };
        auto callback = JavaNetworkTransport::create_result_callback(env, j_callback, mapper);
        app->link_user(user, *credentials, callback);
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_mongodb_User_nativeRemoveUser(JNIEnv* env,
                                                               jclass,
                                                               jlong j_app_ptr,
                                                               jlong j_user_ptr,
                                                               jobject j_callback)
{
    try {
        auto app = *reinterpret_cast<std::shared_ptr<App>*>(j_app_ptr);
        auto user = *reinterpret_cast<std::shared_ptr<SyncUser>*>(j_user_ptr);
        app->remove_user(user, JavaNetworkTransport::create_void_callback(env, j_callback));
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_mongodb_User_nativeLogOut(JNIEnv* env, jclass, jlong j_app_ptr, jlong j_user_ptr, jobject j_callback)
{
    try {
        auto app = *reinterpret_cast<std::shared_ptr<App>*>(j_app_ptr);
        auto user = *reinterpret_cast<std::shared_ptr<SyncUser>*>(j_user_ptr);
        app->log_out(user, JavaNetworkTransport::create_void_callback(env, j_callback));
    }
    CATCH_STD()
}
