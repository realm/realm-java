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

#include "io_realm_internal_objectstore_OsJavaNetworkTransport.h"

#include "java_network_transport.hpp"
#include "util.hpp"
#include "jni_util/java_method.hpp"
#include "jni_util/jni_utils.hpp"

#include <realm/object-store/sync/app.hpp>

using namespace realm;
using namespace realm::app;
using namespace realm::jni_util;
using namespace realm::_impl;

JNIEXPORT void JNICALL Java_io_realm_internal_objectstore_OsJavaNetworkTransport_nativeHandleResponse(JNIEnv* env,
                                                                                                      jclass,
                                                                                                      jobject j_response_obj,
                                                                                                      jlong j_completion_block_ptr)
{
    try {
        auto& completion_block = *reinterpret_cast<std::function<void(const Response)>*>(j_completion_block_ptr);

        // Read response
        static const JavaClass& response_class(JavaClassGlobalDef::network_transport_response_class());
        static JavaMethod get_http_code_method(env, response_class, "getHttpResponseCode", "()I");
        static JavaMethod get_custom_code_method(env, response_class, "getCustomResponseCode", "()I");
        static JavaMethod get_headers_method(env, response_class, "getJNIFriendlyHeaders", "()[Ljava/lang/String;");
        static JavaMethod get_body_method(env, response_class, "getBody", "()Ljava/lang/String;");

        jint http_code = env->CallIntMethod(j_response_obj, get_http_code_method);
        jint custom_code = env->CallIntMethod(j_response_obj, get_custom_code_method);
        JStringAccessor java_body(env, (jstring) env->CallObjectMethod(j_response_obj, get_body_method), true);
        JObjectArrayAccessor<JStringAccessor, jstring> java_headers(env, static_cast<jobjectArray>(env->CallObjectMethod(j_response_obj, get_headers_method)));
        auto response_headers = std::map<std::string, std::string>();
        for (int i = 0; i < java_headers.size(); i = i + 2) {
            JStringAccessor key = java_headers[i];
            JStringAccessor value = java_headers[i+1];
            response_headers.insert(std::pair<std::string,std::string>(key,value));
        }
        std::string body = java_body;

        // Trigger callback into ObjectStore and cleanup
        completion_block(Response{(int) http_code, (int) custom_code, response_headers, body});
        delete& completion_block;
    }
    CATCH_STD()
}

