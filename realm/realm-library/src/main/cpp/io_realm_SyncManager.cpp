/*
 * Copyright 2016 Realm Inc.
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

#include "io_realm_SyncManager.h"

#include <realm/group_shared.hpp>

#include <sync/sync_manager.hpp>
#include <sync/sync_session.hpp>
#include <binding_callback_thread_observer.hpp>

#include "util.hpp"
#include "jni_util/java_class.hpp"
#include "jni_util/java_method.hpp"
#include "jni_util/jni_utils.hpp"

using namespace realm;
using namespace realm::jni_util;
using namespace realm::util;

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
    // For some reasons, FindClass() doesn't work in the native thread even when the JVM is attached before. Get the
    // RealmError class on a normal JVM thread and throw it later on the sync client thread.
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

JNIEXPORT void JNICALL Java_io_realm_SyncManager_nativeReset(JNIEnv* env, jclass)
{
    TR_ENTER()
    try {
        SyncManager::shared().reset_for_testing();
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_SyncManager_nativeInitializeSyncManager(JNIEnv* env, jclass, jstring sync_base_dir)
{
    TR_ENTER()
    try {
        JStringAccessor base_file_path(env, sync_base_dir); // throws
        SyncManager::shared().configure_file_system(base_file_path, SyncManager::MetadataMode::NoEncryption);

        static AndroidClientListener client_thread_listener(env);
        // Register Sync Client thread start/stop callback
        g_binding_callback_thread_observer = &client_thread_listener;

        // init logger
        SyncManager::shared().set_logger_factory(s_sync_logger_factory);
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_SyncManager_nativeSimulateSyncError(JNIEnv* env, jclass, jstring local_realm_path,
                                                                         jint err_code, jstring err_message,
                                                                         jboolean is_fatal)
{
    TR_ENTER()
    try {
        JStringAccessor path(env, local_realm_path);
        JStringAccessor message(env, err_message);

        auto session = SyncManager::shared().get_existing_active_session(path);
        if (!session) {
            ThrowException(env, IllegalArgument, concat_stringdata("Session not found: ", path));
            return;
        }
        std::error_code code = std::error_code{static_cast<int>(err_code), realm::sync::protocol_error_category()};
        SyncSession::OnlyForTesting::handle_error(*session, {code, std::string(message), to_bool(is_fatal)});
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_SyncManager_nativeReconnect(JNIEnv* env, jclass)
{
    TR_ENTER()
    try {
        SyncManager::shared().reconnect();
    }
    CATCH_STD()
}
