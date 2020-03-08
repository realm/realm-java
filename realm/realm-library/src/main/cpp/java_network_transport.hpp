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

#include "util.hpp"
#include "sync/generic_network_transport.hpp"
#include "jni_util/java_class.hpp"
#include "jni_util/java_method.hpp"

using namespace realm::app;
using namespace realm::jni_util;

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
        static JavaMethod http_code_method(env, responseClass, "getHttpResponseCode", "()I");
        static JavaMethod custom_code_method(env, responseClass, "getCustomResponseCode", "()I");
        static JavaMethod headers_method(env, responseClass, "getHeaders", "()Ljava/util/Map;");
        static JavaMethod body_method(env, responseClass, "getBody", "()Ljava/lang/String;");

        if (env->ExceptionCheck()) {
            // This should not happen. All exceptions should ideally have been caught by Java
            // and turned into a realm::app::Response object
            throw std::logic_error("Unexcepted exception thrown"); // FIXME better error
        } else {
            jint http_code = env->CallIntMethod(response, http_code_method);
            jint custom_code = env->CallIntMethod(response, custom_code_method);
            JStringAccessor java_body(env, (jstring) env->CallObjectMethod(response, body_method));
            jobject java_headers = env->CallObjectMethod(response, headers_method);
            (void) java_headers; // FIXME
            auto response_headers = std::map<std::string, std::string>();
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
