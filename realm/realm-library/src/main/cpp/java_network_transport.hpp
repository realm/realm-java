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
#include "jni_util/jni_utils.hpp"

using namespace realm::app;
using namespace realm::jni_util;
using namespace realm::_impl;

namespace realm {

struct JavaNetworkTransport : public app::GenericNetworkTransport {

    JavaNetworkTransport(jobject java_network_transport_impl) {
        JNIEnv* env = JniUtils::get_env(true);
        m_java_network_transport_impl = env->NewGlobalRef(java_network_transport_impl);
        jclass cls = env->GetObjectClass(m_java_network_transport_impl);
        auto method_name = "sendRequest";
        auto signature = "(Ljava/lang/String;Ljava/lang/String;JLjava/util/Map;Ljava/lang/String;)Lio/realm/internal/objectstore/OsJavaNetworkTransport$Response;";
        m_send_request_method = env->GetMethodID(cls, method_name, signature);
        REALM_ASSERT_RELEASE_EX(m_send_request_method != nullptr, method_name, signature);
    }

    void send_request_to_server(const app::Request request, std::function<void(const app::Response)> completionBlock)
    {
        JNIEnv* env = JniUtils::get_env(true);

        // Setup method
        std::string method;
        switch(request.method) {
            case app::HttpMethod::get: method = "get"; break;
            case app::HttpMethod::post: method = "post"; break;
            case app::HttpMethod::patch: method = "patch"; break;
            case app::HttpMethod::put: method = "put"; break;
            case app::HttpMethod::del: method = "delete"; break;
        }

        // Create headers
        static JavaClass mapClass(env, "java/util/HashMap");
        static JavaMethod init(env, mapClass, "<init>", "(I)V");
        static JavaMethod put_method(env, mapClass, "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
        size_t map_size = request.headers.size();
        jobject request_headers = env->NewObject(mapClass, init, (jsize) map_size);
        for (auto header : request.headers) {
            jstring key = to_jstring(env, header.first);
            jstring value = to_jstring(env, header.second);
            env->CallObjectMethod(request_headers, put_method, key, value);
            env->DeleteLocalRef(key);
            env->DeleteLocalRef(value);
        }

        // Execute network request on the Java side
        jstring jmethod = to_jstring(env, method);
        jstring jurl = to_jstring(env, request.url);
        jstring jbody = to_jstring(env, request.body);
        jobject response = env->CallObjectMethod(
                m_java_network_transport_impl,
                m_send_request_method,
                jmethod,
                jurl,
                static_cast<jlong>(request.timeout_ms),
                request_headers,
                jbody
        );
        env->DeleteLocalRef(jmethod);
        env->DeleteLocalRef(jurl);
        env->DeleteLocalRef(jbody);
        env->DeleteLocalRef(request_headers);

        if (env->ExceptionCheck()) {
            // This should not happen. All exceptions should ideally have been caught by Java
            // and turned into a realm::app::Response object. If this happened just
            // let the Java exception bubble up.
            return;
        } else {
            // Read response
            static const JavaClass& response_class(JavaClassGlobalDef::network_transport_response_class());
            static JavaMethod get_http_code_method(env, response_class, "getHttpResponseCode", "()I");
            static JavaMethod get_custom_code_method(env, response_class, "getCustomResponseCode", "()I");
            static JavaMethod get_headers_method(env, response_class, "getJNIFriendlyHeaders", "()[Ljava/lang/String;");
            static JavaMethod get_body_method(env, response_class, "getBody", "()Ljava/lang/String;");

            jint http_code = env->CallIntMethod(response, get_http_code_method);
            jint custom_code = env->CallIntMethod(response, get_custom_code_method);
            JStringAccessor java_body(env, (jstring) env->CallObjectMethod(response, get_body_method), true);
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

    // Helper method for constructing callbacks for REST calls that must return an actual result to Java
    template<typename T>
    static std::function<void(T, util::Optional<app::AppError>)> create_result_callback(JNIEnv* env, jobject j_callback, const std::function<jobject (JNIEnv*, T)>& success_mapper) {
        return [callback = JavaGlobalRefByCopy(env, j_callback), success_mapper](T result, util::Optional<app::AppError> error) {
            JNIEnv* env = JniUtils::get_env(true);

            static JavaClass java_callback_class(env, "io/realm/internal/jni/OsJNIResultCallback");
            static JavaMethod java_notify_onerror(env, java_callback_class, "onError", "(Ljava/lang/String;ILjava/lang/String;)V");
            static JavaMethod java_notify_onsuccess(env, java_callback_class, "onSuccess", "(Ljava/lang/Object;)V");

            if (error) {
                auto err = error.value();
                std::string error_category = err.error_code.category().name();
                env->CallVoidMethod(callback.get(),
                                    java_notify_onerror,
                                    to_jstring(env, error_category),
                                    err.error_code.value(),
                                    to_jstring(env, err.message));
            } else {
                jobject success_obj = success_mapper(env, result);
                env->CallVoidMethod(callback.get(), java_notify_onsuccess, success_obj);
            }
        };
    }

    // Helper method for constructing callbacks for REST calls that doesn't return any results to Java.
    static std::function<void(util::Optional<app::AppError>)> create_void_callback(JNIEnv* env, jobject j_callback) {
        return [callback = JavaGlobalRefByCopy(env, j_callback)](util::Optional<app::AppError> error) {
            JNIEnv* env = JniUtils::get_env(true);

            static JavaClass java_callback_class(env, "io/realm/internal/jni/OsJNIVoidResultCallback");
            static JavaMethod java_notify_onerror(env, java_callback_class, "onError", "(Ljava/lang/String;ILjava/lang/String;)V");
            static JavaMethod java_notify_onsuccess(env, java_callback_class, "onSuccess", "(Ljava/lang/Object;)V");

            if (error) {
                auto err = error.value();
                std::string error_category = err.error_code.category().name();
                env->CallVoidMethod(callback.get(),
                                    java_notify_onerror,
                                    to_jstring(env, error_category),
                                    err.error_code.value(),
                                    to_jstring(env, err.message));
            } else {
                env->CallVoidMethod(callback.get(), java_notify_onsuccess, NULL);
            }
        };
    }

    ~JavaNetworkTransport() {
        JniUtils::get_env(true)->DeleteGlobalRef(m_java_network_transport_impl);
    }

private:
    jobject m_java_network_transport_impl;     // Global ref of Java implementation of the network transport.
    jmethodID m_send_request_method;
};

} // realm namespace

#endif

