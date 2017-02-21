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
#include <object-store/src/sync/impl/sync_client.hpp>

#include "objectserver_shared.hpp"

#include "io_realm_SyncManager.h"

using namespace realm;
using namespace realm::sync;
using namespace realm::jni_util;

std::unique_ptr<Client> sync_client;

struct AndroidClientListener : public realm::ClientThreadListener {

    void on_client_thread_ready() override {
        realm::jni_util::Log::d("on_client_thread_ready");
        // Attach the sync client thread to the JVM so errors can be returned properly
        realm::jni_util::JniUtils::get_env(true);
    }

    void on_client_thread_closing() override {
        realm::jni_util::Log::d("on_client_thread_closing");
        // Failing to detach the JVM before closing the thread will crash on ART
        realm::jni_util::JniUtils::detach_current_thread();
    }
} s_client_thread_listener;

JNIEXPORT void JNICALL Java_io_realm_SyncManager_nativeInitializeSyncClient
    (JNIEnv* env, jclass)
{
    TR_ENTER()
    if (sync_client) return;

    try {
        // Setup SyncManager
        SyncManager::shared().set_client_thread_listener(s_client_thread_listener);

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
