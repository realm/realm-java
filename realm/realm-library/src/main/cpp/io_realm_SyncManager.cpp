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

#include "jni_util/log.hpp"

using namespace realm;
using namespace realm::sync;
using namespace realm::jni_util;

std::unique_ptr<Client> sync_client;

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
    TR_ENTER()
    if (sync_client) return;

    try {
        sync::Client::Config config;
        config.logger = &CoreLoggerBridge::shared();
        sync_client = std::make_unique<Client>(std::move(config)); // Throws

        // This function should only be called once, so below is safe.
        sync_manager = reinterpret_cast<jclass>(env->NewGlobalRef(sync_manager_class));
        sync_manager_notify_error_handler = env->GetStaticMethodID(sync_manager,
                                                                   "notifyErrorHandler", "(ILjava/lang/String;)V");
        sync_client->set_error_handler(error_handler);
    } CATCH_STD()
}

// Create the thread from java side to avoid some strange errors when native throws.
JNIEXPORT void JNICALL
Java_io_realm_SyncManager_nativeRunClient(JNIEnv *env, jclass)
{
    try {
        sync_client->run();
    } CATCH_STD()
}
