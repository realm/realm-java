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

#include "io_realm_internal_objectstore_OsPush.h"

#include "java_class_global_def.hpp"
#include "java_network_transport.hpp"
#include "util.hpp"
#include "jni_util/java_method.hpp"
#include "jni_util/jni_utils.hpp"

#include <realm/util/optional.hpp>
#include <sync/app.hpp>
#include <jni.h>

using namespace realm;
using namespace realm::app;
using namespace realm::bson;
using namespace realm::jni_util;
using namespace realm::_impl;

static void finalize_push_client(jlong ptr) {
    delete reinterpret_cast<PushClient*>(ptr);
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_objectstore_OsPush_nativeGetFinalizerMethodPtr(JNIEnv*, jclass) {
    return reinterpret_cast<jlong>(&finalize_push_client);
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_objectstore_OsPush_nativeCreate(JNIEnv* env,
                                                       jclass,
                                                       jlong j_app_ptr,
                                                       jstring j_service_name) {
    try {
        std::shared_ptr<App> &app = *reinterpret_cast<std::shared_ptr<App> *>(j_app_ptr);
        JStringAccessor service_name(env, j_service_name);
        PushClient client(app->push_notification_client(service_name));
        return reinterpret_cast<jlong>(new PushClient(std::move(client)));
    }
    CATCH_STD()
    return reinterpret_cast<jlong>(nullptr);
}

JNIEXPORT void JNICALL
Java_io_realm_internal_objectstore_OsPush_nativeRegisterDevice(JNIEnv *env,
                                                               jclass,
                                                               jlong j_push_client_ptr,
                                                               jlong j_user_ptr,
                                                               jstring j_service_name,
                                                               jstring j_registration_token,
                                                               jobject j_callback) {
    try {
        auto push_client = reinterpret_cast<PushClient*>(j_push_client_ptr);
        auto user = *reinterpret_cast<std::shared_ptr<SyncUser>*>(j_user_ptr);

        JStringAccessor service_name(env, j_service_name);
        JStringAccessor registration_token(env, j_registration_token);

        push_client->register_device(registration_token,
                                     user,
                                     JavaNetworkTransport::create_void_callback(env, j_callback));
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL
Java_io_realm_internal_objectstore_OsPush_nativeDeregisterDevice(JNIEnv *env,
                                                               jclass,
                                                               jlong j_push_client_ptr,
                                                               jlong j_user_ptr,
                                                               jstring j_service_name,
                                                               jobject j_callback) {
    try {
        auto push_client = reinterpret_cast<PushClient*>(j_push_client_ptr);
        auto user = *reinterpret_cast<std::shared_ptr<SyncUser>*>(j_user_ptr);

        JStringAccessor service_name(env, j_service_name);

        push_client->deregister_device(user,
                                       JavaNetworkTransport::create_void_callback(env, j_callback));
    }
    CATCH_STD()
}
