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

#include "io_realm_RealmUser.h"

#include "java_network_transport.hpp"
#include "util.hpp"
#include "jni_util/java_method.hpp"
#include "jni_util/jni_utils.hpp"

#include <sync/app.hpp>

using namespace realm;
using namespace realm::app;
using namespace realm::jni_util;
using namespace realm::_impl;

JNIEXPORT void JNICALL Java_io_realm_RealmUser_nativeLogOut(JNIEnv* env, jclass, jlong j_app_ptr, jlong j_user_ptr, jobject j_callback)
{
    try {
        App* app = reinterpret_cast<App*>(j_app_ptr);
        auto user = *reinterpret_cast<std::shared_ptr<SyncUser>*>(j_user_ptr);
        app->log_out(user, JavaNetworkTransport::create_void_callback(env, j_callback));
    }
    CATCH_STD()
}
