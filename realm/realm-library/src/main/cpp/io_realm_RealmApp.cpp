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

#include "io_realm_RealmApp.h"

#include "java_network_transport.hpp"
#include "util.hpp"
#include "jni_util/java_method.hpp"
#include "jni_util/jni_utils.hpp"

#include <sync/app.hpp>

using namespace realm;
using namespace realm::app;
using namespace realm::jni_util;
using namespace realm::_impl;

// Helper method for constructing callbacks for REST calls that must return an actual result to Java
template<typename T>
std::function<void(T, Optional<app::AppError>)> create_result_callback(JNIEnv* env, jobject j_callback, const std::function<jobject (JNIEnv*, T)>& success_mapper) {
    jobject callback = env->NewGlobalRef(j_callback);
    return [callback, success_mapper](T result, Optional<app::AppError> error) {
        JNIEnv* env = JniUtils::get_env(true);

        static JavaClass java_callback_class(env, "io/realm/RealmApp$OsJNIResultCallback");
        static JavaMethod java_notify_onerror(env, java_callback_class, "onError", "(Ljava/lang/String;ILjava/lang/String;)V");
        static JavaMethod java_notify_onsuccess(env, java_callback_class, "onSuccess", "(Ljava/lang/Object;)V");

        if (error) {
            auto err = error.value();
            std::string error_category = err.error_code.category().name();
            env->CallVoidMethod(callback,
                                java_notify_onerror,
                                to_jstring(env, error_category),
                                err.error_code.value(),
                                to_jstring(env, err.message));
        } else {
            jobject success_obj = success_mapper(env, result);
            env->CallVoidMethod(callback, java_notify_onsuccess, success_obj);
        }
        env->DeleteGlobalRef(callback);
    };
}

// Helper method for constructing callbacks for REST calls that doesn't return any results to Java.
std::function<void(Optional<app::AppError>)> create_void_callback(JNIEnv* env, jobject j_callback) {
    jobject callback = env->NewGlobalRef(j_callback);
    return [&](Optional<app::AppError> error) {
        JNIEnv* env = JniUtils::get_env(true);

        static JavaClass java_callback_class(env, "io/realm/RealmApp$OsJNIVoidResultCallback");
        static JavaMethod java_notify_onerror(env, java_callback_class, "onError", "(Ljava/lang/String;ILjava/lang/String;)V");
        static JavaMethod java_notify_onsuccess(env, java_callback_class, "onSuccess", "(Ljava/lang/Object;)V");

        if (error) {
            auto err = error.value();
            std::string error_category = err.error_code.category().name();
            env->CallVoidMethod(callback,
                                java_notify_onerror,
                                to_jstring(env, error_category),
                                err.error_code.value(),
                                to_jstring(env, err.message));
        } else {
            env->CallVoidMethod(callback, java_notify_onsuccess, NULL);
        }
        env->DeleteGlobalRef(callback);
    };
}

JNIEXPORT jlong JNICALL Java_io_realm_RealmApp_nativeCreate(JNIEnv* env, jobject obj,
                                                            jstring j_app_id,
                                                            jstring j_base_url,
                                                            jstring j_app_name,
                                                            jstring j_app_version,
                                                            jlong j_request_timeout_ms)
{
    try {
        jobject java_app_obj = env->NewGlobalRef(obj); // FIXME: Leaking the app object
        std::function<std::unique_ptr<GenericNetworkTransport>()> transport_generator = [java_app_obj] {
            JNIEnv* env = JniUtils::get_env(true);
            static JavaMethod get_network_transport_method(env, java_app_obj, "getNetworkTransport", "()Lio/realm/internal/objectstore/OsJavaNetworkTransport;");
            jobject network_transport_impl = env->CallObjectMethod(java_app_obj, get_network_transport_method);
            return std::unique_ptr<GenericNetworkTransport>(new JavaNetworkTransport(network_transport_impl));
        };

        JStringAccessor app_id(env, j_app_id);
        JStringAccessor base_url(env, j_base_url);
        JStringAccessor app_name(env, j_app_name);
        JStringAccessor app_version(env, j_app_version);
        return reinterpret_cast<jlong>(new App(App::Config{
                app_id,
                transport_generator,
                util::Optional<std::string>(base_url),
                util::Optional<std::string>(app_name),
                util::Optional<std::string>(app_version),
                util::Optional<std::uint64_t>(j_request_timeout_ms)
        }));
    }
    CATCH_STD()
    return 0;
}


JNIEXPORT void JNICALL Java_io_realm_RealmApp_nativeLogin(JNIEnv* env, jclass, jlong j_app_ptr, jlong j_credentials_ptr, jobject j_callback)
{
    try {
        App *app = reinterpret_cast<App *>(j_app_ptr);
        auto credentials = reinterpret_cast<AppCredentials *>(j_credentials_ptr);
        std::function<jobject(JNIEnv*, std::shared_ptr<SyncUser>)> mapper = [](JNIEnv* env, std::shared_ptr<SyncUser> user) {
            auto* java_user = new std::shared_ptr<SyncUser>(std::move(user));
            return JavaClassGlobalDef::new_long(env, reinterpret_cast<int64_t>(java_user));
        };
        auto callback = create_result_callback(env, j_callback, mapper);
        app->log_in_with_credentials(*credentials, callback);
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_RealmApp_nativeLogOut(JNIEnv* env, jclass, jlong j_app_ptr, jlong j_user_ptr, jobject j_callback)
{
    try {
        App* app = reinterpret_cast<App*>(j_app_ptr);
        auto user = *reinterpret_cast<std::shared_ptr<SyncUser>*>(j_user_ptr);
        app->log_out(user, create_void_callback(env, j_callback));
    }
    CATCH_STD()
}

JNIEXPORT jobject JNICALL Java_io_realm_RealmApp_nativeCurrentUser(JNIEnv* env, jclass, jlong j_app_ptr)
{
    try {
        App* app = reinterpret_cast<App*>(j_app_ptr);
        std::shared_ptr<SyncUser> user = app->current_user();
        if (user) {
            auto* java_user = new std::shared_ptr<SyncUser>(std::move(user));
            return JavaClassGlobalDef::new_long(env, reinterpret_cast<int64_t>(java_user));
        }
        else {
            return NULL;
        }
    }
    CATCH_STD()
    return NULL;
}

