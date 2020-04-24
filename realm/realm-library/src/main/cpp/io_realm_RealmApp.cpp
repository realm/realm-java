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

#include <binding_callback_thread_observer.hpp>
#include <sync/app.hpp>
#include <sync/sync_manager.hpp>

using namespace realm;
using namespace realm::app;
using namespace realm::jni_util;
using namespace realm::_impl;

struct AndroidClientListener : public realm::BindingCallbackThreadObserver {
    AndroidClientListener(JNIEnv* env)
            : m_realm_exception_class(env, "io/realm/exceptions/RealmError")
    {
    }

    void did_create_thread() override
    {
        Log::d("SyncClient thread created");
        // Attach the sync client thread to the JVM so errors can be returned properly
        JniUtils::get_env(true);
    }

    void will_destroy_thread() override
    {
        // avoid allocating any NewString if we have a pending exception
        // otherwise a "JNI called with pending exception" will be called
        if (JniUtils::get_env(true)->ExceptionCheck() == JNI_FALSE) {
            Log::d("SyncClient thread destroyed");
        }

        // Failing to detach the JVM before closing the thread will crash on ART
        JniUtils::detach_current_thread();
    }

    void handle_error(std::exception const& e) override
    {
        JNIEnv* env = JniUtils::get_env(true);
        std::string msg = format("An exception has been thrown on the sync client thread:\n%1", e.what());
        Log::f(msg.c_str());
        // Since user has no way to handle exceptions thrown on the sync client thread, we just convert it to a Java
        // exception to get more debug information for ourself.
        // FIXME: We really need to find a universal and clever way to get the native backtrace when exception thrown
        env->ThrowNew(m_realm_exception_class, msg.c_str());
    }

private:
    // FindClass() doesn't work in the native thread even when the JVM is attached before, due to it
    // using another ClassLoader. So we get the RealmError class on a normal JVM thread and throw it
    // later on the sync client thread.
    JavaClass m_realm_exception_class;
};

struct AndroidSyncLoggerFactory : public realm::SyncLoggerFactory {
    // The level param is ignored. Use the global RealmLog.setLevel() to control all log levels.
    std::unique_ptr<util::Logger> make_logger(Logger::Level) override
    {
        auto logger = std::make_unique<CoreLoggerBridge>(std::string("REALM_SYNC"));
        // Cast to std::unique_ptr<util::Logger>
        return std::move(logger);
    }
} s_sync_logger_factory;

JNIEXPORT jlong JNICALL Java_io_realm_RealmApp_nativeCreate(JNIEnv* env, jobject obj,
                                                            jstring j_app_id,
                                                            jstring j_base_url,
                                                            jstring j_app_name,
                                                            jstring j_app_version,
                                                            jlong j_request_timeout_ms,
                                                            jstring j_sync_base_dir,
                                                            jstring j_user_agent_binding_info,
                                                            jstring j_user_agent_application_info)
{
    try {

        // App Config
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

        auto app_config = App::Config{
                app_id,
                transport_generator,
                util::Optional<std::string>(base_url),
                util::Optional<std::string>(app_name),
                util::Optional<std::string>(app_version),
                util::Optional<std::uint64_t>(j_request_timeout_ms)
        };

        // Sync Config
        JStringAccessor base_file_path(env, j_sync_base_dir); // throws
        JStringAccessor user_agent_binding_info(env, j_user_agent_binding_info); // throws
        JStringAccessor user_agent_application_info(env, j_user_agent_application_info); // throws

        SyncClientConfig client_config;
        client_config.base_file_path = base_file_path;
        client_config.metadata_mode = SyncManager::MetadataMode::NoEncryption;
        client_config.user_agent_binding_info = user_agent_binding_info;
        client_config.user_agent_application_info = user_agent_application_info;

        // FIXME: SyncManager is still a singleton. Should be refactored to allow multiple
        SyncManager::shared().configure(client_config, app_config);
        // Init logger. Must be called after .configure()
        SyncManager::shared().set_logger_factory(s_sync_logger_factory);
        // Register Sync Client thread start/stop callback. Must be called after .configure()
        static AndroidClientListener client_thread_listener(env);
        g_binding_callback_thread_observer = &client_thread_listener;

        return reinterpret_cast<jlong>(new std::shared_ptr<App>(SyncManager::shared().app()));
    }
    CATCH_STD()
    return 0;
}


JNIEXPORT void JNICALL Java_io_realm_RealmApp_nativeLogin(JNIEnv* env, jclass, jlong j_app_ptr, jlong j_credentials_ptr, jobject j_callback)
{
    try {
        auto app = *reinterpret_cast<std::shared_ptr<App>*>(j_app_ptr);
        auto credentials = reinterpret_cast<AppCredentials *>(j_credentials_ptr);
        std::function<jobject(JNIEnv*, std::shared_ptr<SyncUser>)> mapper = [](JNIEnv* env, std::shared_ptr<SyncUser> user) {
            auto* java_user = new std::shared_ptr<SyncUser>(std::move(user));
            return JavaClassGlobalDef::new_long(env, reinterpret_cast<int64_t>(java_user));
        };
        auto callback = JavaNetworkTransport::create_result_callback(env, j_callback, mapper);
        app->log_in_with_credentials(*credentials, callback);
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_RealmApp_nativeLogOut(JNIEnv* env, jclass, jlong j_app_ptr, jlong j_user_ptr, jobject j_callback)
{
    try {
        auto app = *reinterpret_cast<std::shared_ptr<App>*>(j_app_ptr);
        auto user = *reinterpret_cast<std::shared_ptr<SyncUser>*>(j_user_ptr);
        app->log_out(user, JavaNetworkTransport::create_void_callback(env, j_callback));
    }
    CATCH_STD()
}

JNIEXPORT jobject JNICALL Java_io_realm_RealmApp_nativeCurrentUser(JNIEnv* env, jclass, jlong j_app_ptr)
{
    try {
        auto app = *reinterpret_cast<std::shared_ptr<App>*>(j_app_ptr);
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

JNIEXPORT jlongArray JNICALL Java_io_realm_RealmApp_nativeGetAllUsers(JNIEnv* env, jclass, jlong j_app_ptr)
{
    try {
        auto app = *reinterpret_cast<std::shared_ptr<App>*>(j_app_ptr);
        std::vector<std::shared_ptr<SyncUser>> users = app->all_users();
        auto size = users.size();

        jlongArray java_users = env->NewLongArray(size);
        if (!java_users) {
            ThrowException(env, OutOfMemory, "Could not allocate memory to create array of users.");
            return nullptr;
        }

        jlong* user_ptrs = new jlong[size];
        for(size_t i = 0; i < size; ++i) {
            auto *java_user = new std::shared_ptr<SyncUser>(std::move(users[i]));
            user_ptrs[i] = reinterpret_cast<int64_t>(java_user);
        }

        env->SetLongArrayRegion(java_users, 0, size, user_ptrs);
        delete[] user_ptrs;
        return java_users;
    }
    CATCH_STD()
    return nullptr;
}

JNIEXPORT void JNICALL Java_io_realm_RealmApp_nativeSwitchUser(JNIEnv* env,
                                                               jclass,
                                                               jlong j_app_ptr,
                                                               jlong j_user_ptr)
{
    try {
        auto app = *reinterpret_cast<std::shared_ptr<App>*>(j_app_ptr);
        auto user = *reinterpret_cast<std::shared_ptr<SyncUser>*>(j_user_ptr);
        app->switch_user(user);
    }
    CATCH_STD()
}

