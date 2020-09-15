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
        std::string msg = util::format("An exception has been thrown on the sync client thread:\n%1", e.what());
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
    std::unique_ptr<util::Logger> make_logger(util::Logger::Level) override
    {
        auto logger = std::make_unique<CoreLoggerBridge>(std::string("REALM_SYNC"));
        // Cast to std::unique_ptr<util::Logger>
        return std::move(logger);
    }
} s_sync_logger_factory;

static void finalize_client(jlong ptr) {
    delete reinterpret_cast<App*>(ptr);
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_objectstore_OsApp_nativeGetFinalizerMethodPtr(JNIEnv*, jclass) {
    return reinterpret_cast<jlong>(&finalize_client);
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_objectstore_OsApp_nativeCreate(JNIEnv* env, jobject obj,
                                                               jstring j_app_id,
                                                               jstring j_base_url,
                                                               jstring j_app_name,
                                                               jstring j_app_version,
                                                               jlong j_request_timeout_ms,
                                                               jbyteArray j_encryption_key,
                                                               jstring j_sync_base_dir,
                                                               jstring j_user_agent_binding_info,
                                                               jstring j_user_agent_application_info,
                                                               jstring j_platform,
                                                               jstring j_platform_version,
                                                               jstring j_sdk_version)
{
    try {

        JStringAccessor app_id(env, j_app_id);

        // Check if we already have a cached instance, if yes, return that instead. The Java GC
        // will only cleanup the shared pointer, but leave the cached instance alone. This also
        // means that no App is never fully closed. This should be safe as App doesn't implement
        // Closable in Java, so it doesn't have a a visible lifecycle.
        auto cached_app = App::get_cached_app(app_id);
        if (cached_app) {
            return reinterpret_cast<jlong>(new std::shared_ptr<App>(cached_app));
        }

        // App Config
        std::function<std::unique_ptr<GenericNetworkTransport>()> transport_generator = [java_app_ref = JavaGlobalRefByCopy(env, obj)] {
            JNIEnv* env = JniUtils::get_env(true);
            static JavaMethod get_network_transport_method(env, java_app_ref.get(), "getNetworkTransport", "()Lio/realm/internal/objectstore/OsJavaNetworkTransport;");
            jobject network_transport_impl = env->CallObjectMethod(java_app_ref.get(), get_network_transport_method);
            return std::unique_ptr<GenericNetworkTransport>(new JavaNetworkTransport(network_transport_impl));
        };

        JStringAccessor base_url(env, j_base_url);
        JStringAccessor app_name(env, j_app_name);
        JStringAccessor app_version(env, j_app_version);
        JStringAccessor platform(env, j_platform);
        JStringAccessor platform_version(env, j_platform_version);
        JStringAccessor sdk_version(env, j_sdk_version);
        JByteArrayAccessor encryption_key(env, j_encryption_key);

        auto app_config = App::Config{
                app_id,
                transport_generator,
                util::Optional<std::string>(base_url),
                util::Optional<std::string>(app_name),
                util::Optional<std::string>(app_version),
                util::Optional<std::uint64_t>(j_request_timeout_ms),
                platform,
                platform_version,
                sdk_version
        };

        // Sync Config
        JStringAccessor base_file_path(env, j_sync_base_dir); // throws
        JStringAccessor user_agent_binding_info(env, j_user_agent_binding_info); // throws
        JStringAccessor user_agent_application_info(env, j_user_agent_application_info); // throws

        SyncClientConfig client_config;
        client_config.base_file_path = base_file_path;
        client_config.user_agent_binding_info = user_agent_binding_info;
        client_config.user_agent_application_info = user_agent_application_info;

        if(j_encryption_key == nullptr){
            client_config.metadata_mode = SyncManager::MetadataMode::NoEncryption;
        } else {
            client_config.metadata_mode = SyncManager::MetadataMode::Encryption;
            client_config.custom_encryption_key = encryption_key.transform<std::vector<char>>();
        }

        SharedApp app = App::get_shared_app(app_config, client_config);
        // Init logger. Must be called after .configure()
        app->sync_manager()->set_logger_factory(s_sync_logger_factory);
        // Register Sync Client thread start/stop callback. Must be called after .configure()
        static AndroidClientListener client_thread_listener(env);
        g_binding_callback_thread_observer = &client_thread_listener;

        return reinterpret_cast<jlong>(new std::shared_ptr<App>(app));
    }
    CATCH_STD()
    return 0;
}


JNIEXPORT void JNICALL Java_io_realm_internal_objectstore_OsApp_nativeLogin(JNIEnv* env, jclass, jlong j_app_ptr, jlong j_credentials_ptr, jobject j_callback)
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

JNIEXPORT void JNICALL Java_io_realm_internal_objectstore_OsApp_nativeLogOut(JNIEnv* env, jclass, jlong j_app_ptr, jlong j_user_ptr, jobject j_callback)
{
    try {
        auto app = *reinterpret_cast<std::shared_ptr<App>*>(j_app_ptr);
        auto user = *reinterpret_cast<std::shared_ptr<SyncUser>*>(j_user_ptr);
        app->log_out(user, JavaNetworkTransport::create_void_callback(env, j_callback));
    }
    CATCH_STD()
}

JNIEXPORT jobject JNICALL Java_io_realm_internal_objectstore_OsApp_nativeCurrentUser(JNIEnv* env, jclass, jlong j_app_ptr)
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

JNIEXPORT jlongArray JNICALL Java_io_realm_internal_objectstore_OsApp_nativeGetAllUsers(JNIEnv* env, jclass, jlong j_app_ptr)
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

JNIEXPORT void JNICALL Java_io_realm_internal_objectstore_OsApp_nativeSwitchUser(JNIEnv* env,
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
        jobject j_headers = JniUtils::to_hash_map(env, request.headers);
        jstring j_body = env->NewStringUTF(request.body.c_str());

        static JavaClass request_class(env, "io/realm/internal/objectstore/OsJavaNetworkTransport$Request");
        static JavaMethod request_constructor(env, request_class, "<init>","(Ljava/lang/String;Ljava/lang/String;Ljava/util/Map;Ljava/lang/String;)V");
        jobject j_request = env->NewObject(request_class, request_constructor, j_method, j_url, j_headers, j_body);

        return j_request;
    }
    CATCH_STD()

    return nullptr;
}

