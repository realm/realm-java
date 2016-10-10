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

#include <jni.h>

#include <chrono>
#include <functional>
#include <mutex>
#include <vector>

#include <realm/group_shared.hpp>
#include <realm/sync/history.hpp>
#include <realm/sync/client.hpp>

#include "objectserver_shared.hpp"

#include "io_realm_SyncManager.h"

using namespace realm;
using namespace realm::sync;

std::unique_ptr<Client> sync_client;

class AndroidLogger: public util::RootLogger
{
public:
    void do_log(Level level, std::string msg)
    {
        // FIXME Sync only calls the logger from the thread running the client, so it should
        // be safe to store the env when starting the thread.
        JNIEnv *env;
        g_vm->AttachCurrentThread(&env, nullptr);
        jmethodID log_method;
        switch (level) {
            case Level::trace: log_method = log_trace; break;
            case Level::debug: log_method = log_debug; break;
            case Level::detail: log_method = log_debug; break;
            case Level::info: log_method = log_info; break;
            case Level::warn: log_method = log_warn; break;
            case Level::error: log_method = log_error; break;
            case Level::fatal: log_method = log_fatal; break;
            case Level::all:
            case Level::off:
                throw invalid_argument(util::format("Unknown logger argument: %s.", util::Logger::get_level_prefix(level)));
        }
        log_message(env, log_method, msg.c_str());
    }
    static AndroidLogger& shared() noexcept;
};

// Not used by now
struct AndroidLoggerFactory : public realm::SyncLoggerFactory {
    std::unique_ptr<util::Logger> make_logger(util::Logger::Level level) {
        auto logger = std::make_unique<AndroidLogger>();
        logger->set_level_threshold(level);
        return std::unique_ptr<util::Logger>(std::move(logger));
    }
} s_logger_factory;

// TODO: Move to a better place & not needed after moving to OS
AndroidLogger& AndroidLogger::shared() noexcept
{
    static AndroidLogger logger;
    return logger;
}

static jclass sync_manager = nullptr;
static jmethodID sync_manager_notify_error_handler = nullptr;

static void error_handler(int error_code, std::string message)
{
    JNIEnv* env;
    if (g_vm->GetEnv((void **) &env, JNI_VERSION_1_6) != JNI_OK) {
        throw std::runtime_error("JVM is not attached to this thread. Called in error_handler.");
    }

    env->CallStaticVoidMethod(sync_manager,
                              sync_manager_notify_error_handler, error_code, env->NewStringUTF(message.c_str()));
}

JNIEXPORT void JNICALL Java_io_realm_SyncManager_nativeInitializeSyncClient
    (JNIEnv *env, jclass sync_manager_class)
{
    TR_ENTER(env)
    try_catch<void>(env, [&]() {
        if (sync_client) return;

        AndroidLogger::shared().set_level_threshold(util::Logger::Level::warn);

        sync::Client::Config config;
        config.logger = &AndroidLogger::shared();
        sync_client = std::make_unique<Client>(std::move(config)); // Throws

        // This function should only be called once, so below is safe.
        sync_manager = sync_manager_class;
        sync_manager_notify_error_handler = env->GetStaticMethodID(sync_manager,
                                                                   "notifyErrorHandler", "(ILjava/lang/String;)V");
        sync_client->set_error_handler(error_handler);
    });
}

// Create the thread from java side to avoid some strange errors when native throws.
JNIEXPORT void JNICALL
Java_io_realm_SyncManager_nativeRunClient(JNIEnv *env, jclass)
{
    try_catch<void>(env, [&]() {
        sync_client->run();
    });
}

JNIEXPORT void JNICALL
Java_io_realm_SyncManager_nativeSetSyncClientLogLevel(JNIEnv* env, jclass, jint logLevel)
{
    try_catch<void>(env, [&]() {
        util::Logger::Level native_log_level;
        switch(logLevel) {
            case io_realm_log_LogLevel_ALL: native_log_level = util::Logger::Level::all; break;
            case io_realm_log_LogLevel_TRACE: native_log_level = util::Logger::Level::trace; break;
            case io_realm_log_LogLevel_DEBUG: native_log_level = util::Logger::Level::debug; break;
            case io_realm_log_LogLevel_INFO: native_log_level = util::Logger::Level::info; break;
            case io_realm_log_LogLevel_WARN: native_log_level = util::Logger::Level::warn; break;
            case io_realm_log_LogLevel_ERROR: native_log_level = util::Logger::Level::error; break;
            case io_realm_log_LogLevel_FATAL: native_log_level = util::Logger::Level::fatal; break;
            case io_realm_log_LogLevel_OFF: native_log_level = util::Logger::Level::off; break;
            default:
                throw invalid_argument("Invalid log level: " + logLevel);
        }
        // FIXME: This call is not thread safe. Switch to OS implementation to make it thread safe.
        AndroidLogger::shared().set_level_threshold(native_log_level);
    });
}
