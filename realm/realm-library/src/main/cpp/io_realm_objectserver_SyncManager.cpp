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

#include "io_realm_objectserver_SyncManager.h"
#include "objectserver_shared.hpp"
#include "util.hpp"
#include <realm/group_shared.hpp>
#include <realm/replication.hpp>
#include <realm/commit_log.hpp>

#include <realm/sync/history.hpp>
#include <realm/sync/client.hpp>
#include <realm/util/logger.hpp>
#include <mutex>
#include <thread>
#include <vector>
#include <chrono>
#include <functional>
#include <android/log.h>
#include <object-store/src/sync_manager.hpp>

using namespace std;
using namespace realm;
using namespace sync;

class AndroidLogger: public realm::util::RootLogger
{
public:
    void do_log(std::string msg)
    {
        // Figure out how to log properly. We need level/code/message
        // Think it has been fixed in later versions of Core
        log_message(sync_client_env, log_debug, msg.c_str());
    }
};

struct AndroidLoggerFactory : public realm::SyncLoggerFactory {
    std::unique_ptr<realm::util::Logger> make_logger(realm::util::Logger::Level level) {
        auto logger = std::make_unique<AndroidLogger>();
        logger->set_level_threshold(level);
        return std::move(logger);
    }
} s_logger_factory;

// Object Server global vars, see objectserver_shared.hpp
std::thread* sync_client_thread;
JNIEnv* sync_client_env;

JNIEXPORT void JNICALL Java_io_realm_objectserver_SyncManager_nativeInitializeSyncClient
    (JNIEnv *env, jclass)
{
    TR_ENTER(env)
    try {
        // Prepare Sync Client. It will be created on demand

        SyncLoginFunction loginDelegate = [=](const Realm::Config& config) {
            // Ignore this for now. We are handling this manually.
        };

        SyncClientReadyFunction clientReadyDelegate = [=](const realm::sync::Client&) {
            //Attaching thread to Java so we can perform JNI calls
            JavaVMAttachArgs args;
            args.version = JNI_VERSION_1_6;
            args.name = NULL; // java thread a name
            args.group = NULL; // java thread group
            g_vm->AttachCurrentThread(&sync_client_env, &args);
        };

        SyncManager& sync_manager = SyncManager::shared();
        sync_manager.set_login_function(loginDelegate);
        sync_manager.set_logger_factory(s_logger_factory);
        sync_manager.set_log_level(util::Logger::Level::warn);
        sync_manager.set_client_ready_callback(clientReadyDelegate);
    } CATCH_STD()
}

JNIEXPORT void JNICALL
Java_io_realm_objectserver_SyncManager_nativeSetSyncClientLogLevel(JNIEnv* env, jclass, jint logLevel)
{
    util::Logger::Level native_log_level;
    bool valid_log_level = true;
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
            valid_log_level = false;
            ThrowException(env, IllegalArgument, "Invalid log level: " + logLevel);
    }
    if (valid_log_level) {
        realm::SyncManager::shared().set_log_level(native_log_level);
    }
}


