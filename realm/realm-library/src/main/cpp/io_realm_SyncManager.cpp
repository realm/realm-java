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
using namespace realm::jni_util;

std::unique_ptr<Client> sync_client;

static jclass sync_manager = nullptr;
static jmethodID sync_manager_notify_error_handler = nullptr;
static jmethodID sync_manager_on_session_created = nullptr;
static jmethodID sync_manager_on_session_destroyed = nullptr;

static void error_handler(int error_code, std::string message)
{
    JNIEnv* env;
    if (g_vm->GetEnv((void **) &env, JNI_VERSION_1_6) != JNI_OK) {
        throw std::runtime_error("JVM is not attached to this thread. Called in error_handler.");
    }

    env->CallStaticVoidMethod(sync_manager,
                              sync_manager_notify_error_handler,
                              error_code,
                              env->NewStringUTF(message.c_str()));
}

static void client_thread_ready(sync::Client*)
{
    // Attach the sync client thread to the JVM so errors can be returned properly
    JNIEnv *env;
    if (g_vm->GetEnv((void **) &env, JNI_VERSION_1_6) != JNI_OK) {
        g_vm->AttachCurrentThread(&env, nullptr); // Should never fail
    }
}

struct AndroidLoggerFactory : public realm::SyncLoggerFactory {
    std::unique_ptr<realm::util::Logger> make_logger(realm::util::Logger::Level level) {
        std::unique_ptr<Logger> log_ptr(new CoreLoggerBridge());
    }
} s_logger_factory;

JNIEXPORT void JNICALL Java_io_realm_SyncManager_nativeInitializeSyncClient
    (JNIEnv *env, jclass sync_manager_class)
{
    TR_ENTER()
    if (sync_client) return;

    try {
        // Register Java callback function for error handling
        // This function should only be called once, so doing it here should be fine.
        sync_manager = reinterpret_cast<jclass>(env->NewGlobalRef(sync_manager_class));
        sync_manager_notify_error_handler = env->GetStaticMethodID(sync_manager,
                                                                   "notifyErrorHandler",
                                                                   "(ILjava/lang/String;Ljava/lang/String;)V");
//        sync_manager_on_session_created  = env->GetStaticMethodID(sync_manager,
//                                                                  "onObjectStoreSessionCreated",
//                                                                  "(Ljava/lang/String;)V");
//        sync_manager_on_session_destroyed  = env->GetStaticMethodID(sync_manager,
//                                                                    "onObjectStoreSessionDestroyed",
//                                                                    "(Ljava/lang/String;)V");
//
        // Setup SyncManager
        SyncManager::shared().set_logger_factory(s_logger_factory);
        SyncManager::shared().set_error_handler(error_handler);
        SyncManager::shared().set_client_thread_ready_callback(client_thread_ready);
        SyncManager::shared().reset_for_testing();
//        bool should_encrypt = !getenv("REALM_DISABLE_METADATA_ENCRYPTION");
//        auto mode = should_encrypt ? SyncManager::MetadataMode::Encryption : SyncManager::MetadataMode::NoEncryption;
//        rootDirectory = rootDirectory ?: [NSURL fileURLWithPath:RLMDefaultDirectoryForBundleIdentifier(nil)];
//        SyncManager::shared().configure_file_system(rootDirectory.path.UTF8String, mode);
//        sync::Client::Config
//        sync::Client::Config config;
//        config.logger = &CoreLoggerBridge::shared();
//        sync_client = std::make_unique<Client>(std::move(config)); // Throws
//
//        sync_client->set_error_handler(error_handler);
    } CATCH_STD()
}

JNIEXPORT void JNICALL
Java_io_realm_SyncManager_nativeReset(JNIEnv *env, jclass type) {

    TR_ENTER()
    try {
        SyncManager::shared().reset_for_testing();
    } CATCH_STD()
}