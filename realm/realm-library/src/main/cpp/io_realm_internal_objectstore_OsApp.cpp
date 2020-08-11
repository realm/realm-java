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

#include "io_realm_internal_objectstore_OsApp.h"

#include "java_network_transport.hpp"
#include "util.hpp"
#include "jni_util/java_method.hpp"
#include "jni_util/jni_utils.hpp"

#include <binding_callback_thread_observer.hpp>
#include <sync/app.hpp>
#include <sync/sync_manager.hpp>
#include <jni_util/bson_util.hpp>

using namespace realm;
using namespace realm::app;
using namespace realm::jni_util;
using namespace realm::_impl;

static void finalize_client(jlong ptr) {
    delete reinterpret_cast<App*>(ptr);
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_objectstore_OsApp_nativeGetFinalizerMethodPtr(JNIEnv*, jclass) {
    return reinterpret_cast<jlong>(&finalize_client);
}

JNIEXPORT jobject JNICALL Java_io_realm_internal_objectstore_OsApp_nativeMakeStreamingRequest(JNIEnv* env,
                                                                               jclass,
                                                                               jlong j_app_ptr,
                                                                               jlong j_user_ptr,
                                                                               jstring j_function_name,
                                                                               jstring j_bson_args,
                                                                               jstring j_service_name)
{
    try {
        auto app = *reinterpret_cast<std::shared_ptr<App>*>(j_app_ptr);
        auto user = *reinterpret_cast<std::shared_ptr<SyncUser>*>(j_user_ptr);

        JStringAccessor function_name(env, j_function_name);
        JStringAccessor service_name(env, j_service_name);

        bson::BsonArray filter(JniBsonProtocol::parse_checked(env, j_bson_args, Bson::Type::Array, "BSON filter must be an Array"));

        const Request &request = app->make_streaming_request(user, function_name, filter,
                                                             std::string(service_name));

        jstring j_method;

        switch (request.method){
            case HttpMethod::get:
                j_method = env->NewStringUTF("get");
                break;
            case HttpMethod::post:
                j_method = env->NewStringUTF("post");
                break;
            case HttpMethod::patch:
                j_method = env->NewStringUTF("patch");
                break;
            case HttpMethod::put:
                j_method = env->NewStringUTF("put");
                break;
            case HttpMethod::del:
                j_method = env->NewStringUTF("del");
                break;
        }

        jstring j_url = env->NewStringUTF(request.url.c_str());
        jobject j_headers = JniUtils::to_hashmap(env, request.headers);
        jstring j_body = env->NewStringUTF(request.body.c_str());

        static JavaClass request_class(env, "io/realm/internal/objectstore/OsJavaNetworkTransport$Request");
        static JavaMethod request_constructor(env, request_class, "<init>","(Ljava/lang/String;Ljava/lang/String;Ljava/util/Map;Ljava/lang/String;)V");
        jobject j_request = env->NewObject(request_class, request_constructor, j_method, j_url, j_headers, j_body);

        return j_request;
    }
    CATCH_STD()

    return nullptr;
}

