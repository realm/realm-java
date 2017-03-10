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

#include "io_realm_SyncManager.h"

#include "object-store/src/sync/sync_manager.hpp"

#include "binding_callback_thread_observer.hpp"
#include "util.hpp"

#include "jni_util/jni_utils.hpp"
#include "jni_util/java_method.hpp"

using namespace realm;
using namespace realm::sync;
using namespace realm::jni_util;

std::unique_ptr<Client> sync_client;

struct AndroidClientListener : public realm::BindingCallbackThreadObserver {

    void did_create_thread() override {
        Log::d("SyncClient thread created");
        // Attach the sync client thread to the JVM so errors can be returned properly
        JniUtils::get_env(true);
    }

    void will_destroy_thread() override {
        Log::d("SyncClient thread destroyed");
        // Failing to detach the JVM before closing the thread will crash on ART
        JniUtils::detach_current_thread();
    }
} s_client_thread_listener;

JNIEXPORT void JNICALL Java_io_realm_SyncManager_nativeInitializeSyncClient
    (JNIEnv* env, jclass)
{
    TR_ENTER()
    if (sync_client) return;

    try {
        // Setup SyncManager
        g_binding_callback_thread_observer = &s_client_thread_listener;

        // Create SyncClient
        sync::Client::Config config;
        config.logger = &CoreLoggerBridge::shared();
        sync_client = std::make_unique<Client>(std::move(config)); // Throws
    } CATCH_STD()
}

JNIEXPORT void JNICALL
Java_io_realm_SyncManager_nativeReset(JNIEnv* env, jclass) {

    TR_ENTER()
    try {
        SyncManager::shared().reset_for_testing();
    } CATCH_STD()
}

JNIEXPORT void JNICALL
Java_io_realm_SyncManager_nativeConfigureMetaDataSystem(JNIEnv *env, jclass,
                                                        jstring baseFile) {
    TR_ENTER()
    try {
        JStringAccessor base_file_path(env, baseFile); // throws
        SyncManager::shared().configure_file_system(base_file_path, SyncManager::MetadataMode::NoEncryption);
    } CATCH_STD()
}
