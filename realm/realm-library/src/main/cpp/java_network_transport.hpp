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

#ifndef REALM_JAVA_NETWORK_TRANSPORT
#define REALM_JAVA_NETWORK_TRANSPORT

#include "java_accessor.hpp"
#include "util.hpp"
#include "sync/generic_network_transport.hpp"
#include "jni_util/java_class.hpp"
#include "jni_util/java_method.hpp"

using namespace realm::app;
using namespace realm::jni_util;
using namespace realm::_impl;

namespace realm {

struct JavaNetworkTransport : public app::GenericNetworkTransport {

    JavaNetworkTransport(JavaVM* vm, jobject java_network_transport_impl) {
        m_jvm = vm;
        m_java_network_transport_impl = java_network_transport_impl; //env->NewGlobalRef(java_network_transport_impl);
        JNIEnv* env = get_current_env();
        jclass cls = env->GetObjectClass(m_java_network_transport_impl);
        auto method_name = "sendRequest";
        auto signature = "(Ljava/lang/String;Ljava/lang/String;JLjava/util/Map;Ljava/lang/String;)Lio/realm/internal/objectstore/OsJavaNetworkTransport$Response;";
        m_send_request_method = env->GetMethodID(cls, method_name, signature);
        REALM_ASSERT_RELEASE_EX(m_send_request_method != nullptr, method_name, signature);
    }

    void send_request_to_server(const app::Request request, std::function<void(const app::Response)> completionBlock)
    {
        JNIEnv* env = get_current_env();

        // Setup method
        std::string method;
        switch(request.method) {
            case app::HttpMethod::get: method = "get"; break;
            case app::HttpMethod::post: method = "post"; break;
            case app::HttpMethod::patch: method = "patch"; break;
            case app::HttpMethod::put: method = "put"; break;
            case app::HttpMethod::del: method = "del"; break;
        }

        // Create headers
        static JavaClass mapClass(env, "java/util/HashMap");
        static JavaMethod init(env, mapClass, "<init>", "(I)V");
        static JavaMethod put_method(env, mapClass, "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
        jsize map_size = request.headers.size();
        jobject request_headers = env->NewObject(mapClass, init, map_size);
        for (auto header : request.headers) {
            env->CallObjectMethod(request_headers, put_method, to_jstring(env, header.first), to_jstring(env, header.second));
        }

        // Execute network request on the Java side
        jobject response = env->CallObjectMethod(m_java_network_transport_impl,
                                            m_send_request_method,
                                            to_jstring(env, method),
                                            to_jstring(env, request.url),
                                            static_cast<jlong>(request.timeout_ms),
                                            request_headers,
                                            to_jstring(env, request.body)
                                            );
        env->DeleteLocalRef(request_headers);

        // Read response
        static JavaClass responseClass(env, "io/realm/internal/objectstore/OsJavaNetworkTransport$Response");
        static JavaMethod get_http_code_method(env, responseClass, "getHttpResponseCode", "()I");
        static JavaMethod get_custom_code_method(env, responseClass, "getCustomResponseCode", "()I");
        static JavaMethod get_headers_method(env, responseClass, "getJNIFriendlyHeaders", "()[Ljava/lang/String;");
        static JavaMethod get_body_method(env, responseClass, "getBody", "()Ljava/lang/String;");

        if (env->ExceptionCheck()) {
            // This should not happen. All exceptions should ideally have been caught by Java
            // and turned into a realm::app::Response object
            throw std::logic_error("Unexcepted exception thrown"); // FIXME better error
        } else {
            jint http_code = env->CallIntMethod(response, get_http_code_method);
            jint custom_code = env->CallIntMethod(response, get_custom_code_method);
            JStringAccessor java_body(env, (jstring) env->CallObjectMethod(response, get_body_method));
            JObjectArrayAccessor<JStringAccessor, jstring> java_headers(env, static_cast<jobjectArray>(env->CallObjectMethod(response, get_headers_method)));
            auto response_headers = std::map<std::string, std::string>();
            for (int i = 0; i < java_headers.size(); i = i + 2) {
                JStringAccessor key = java_headers[i];
                JStringAccessor value = java_headers[i+1];
                response_headers.insert(std::pair<std::string,std::string>(key,value));
            }
            std::string body = java_body;
            completionBlock(Response{(int) http_code, (int) custom_code, response_headers, body});
        }
    }

    ~JavaNetworkTransport() {
        get_current_env()->DeleteGlobalRef(m_java_network_transport_impl);
    }

private:
    JavaVM* m_jvm;
    jobject m_java_network_transport_impl;     // Global ref of Java implementation of the network transport.
    jmethodID m_send_request_method;
    inline JNIEnv* get_current_env() noexcept
    {
        JNIEnv* env;
        if (m_jvm->GetEnv((void**)&env, JNI_VERSION_1_6) != JNI_OK) {
            m_jvm->AttachCurrentThread(&env, nullptr); // Should never fail
        }
        return env;
    }
};

} // realm namespace

#endif
