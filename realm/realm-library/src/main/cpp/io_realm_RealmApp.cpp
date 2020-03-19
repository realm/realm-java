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

#include <sync/app.hpp>

using namespace realm;
using namespace realm::app;
using namespace realm::jni_util;
using namespace realm::_impl;

JNIEXPORT jlong JNICALL Java_io_realm_RealmApp_nativeCreate(JNIEnv* env, jobject obj,
                                                            jstring j_app_id,
                                                            jstring j_base_url,
                                                            jstring j_app_name,
                                                            jstring j_app_version,
                                                            jlong j_request_timeout_ms)
{
    try {
        JavaVM* jvm;
        jint ret = env->GetJavaVM(&jvm);
        if (ret != 0) {
            throw std::runtime_error(util::format("Failed to get Java VM. Error: %d", ret));
        }
        jobject java_app_obj = env->NewGlobalRef(obj); // FIXME: Leaking the app object
        std::function<std::unique_ptr<GenericNetworkTransport>()> transport_generator = [jvm, java_app_obj] {
            JNIEnv* env;
            if (jvm->GetEnv((void**)&env, JNI_VERSION_1_6) != JNI_OK) {
                jvm->AttachCurrentThread(&env, nullptr); // Should never fail
            }
            static JavaMethod get_network_transport_method(env, java_app_obj, "getNetworkTransport", "()Lio/realm/internal/objectstore/OsJavaNetworkTransport;");
            jobject network_transport_impl = env->CallObjectMethod(java_app_obj, get_network_transport_method);
            return std::unique_ptr<GenericNetworkTransport>(new JavaNetworkTransport(jvm, network_transport_impl));
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
        // Caching callback method ID's in static fields to prevent looking them up more than once
        static JavaClass java_callback_class(env, "io/realm/internal/objectstore/OsJavaNetworkTransport$NetworkTransportJNIResultCallback");
        static JavaMethod java_notify_onerror(env, java_callback_class, "onError", "(Ljava/lang/String;ILjava/lang/String;)V");
        static JavaMethod java_notify_onsuccess(env, java_callback_class, "onSuccess", "(Ljava/lang/Object;)V");

        App* app = reinterpret_cast<App*>(j_app_ptr);
        auto credentials = reinterpret_cast<AppCredentials*>(j_credentials_ptr);
        jobject callback = env->NewGlobalRef(j_callback);
        app->log_in_with_credentials(*credentials, [&](std::shared_ptr<SyncUser> user, Optional<app::AppError> error) {
            if (error) {
                auto err = error.value();
                std::string error_category = err.error_code.category().name();
                env->CallVoidMethod(callback,
                                    java_notify_onerror,
                                    to_jstring(env, error_category),
                                    err.error_code.value(),
                                    to_jstring(env, err.message));
            } else {
                auto* java_user = new std::shared_ptr<SyncUser>(std::move(user));
                jobject ptr_value = JavaClassGlobalDef::new_long(env, reinterpret_cast<int64_t>(java_user));
                env->CallVoidMethod(callback, java_notify_onsuccess, ptr_value);
            }
            env->DeleteGlobalRef(callback);
        });
    }
    CATCH_STD()
}
