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

#include "io_realm_sync_SyncManager.h"
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

using namespace std;
using namespace realm;
using namespace sync;

class AndroidLogger: public realm::util::RootLogger
{
public:
    void do_log(std::string msg)
    {
        __android_log_print(ANDROID_LOG_INFO, "[SYNC]", "> %s", msg.c_str());
    }
};

// maintain a reference to the threads allocated dynamically, to prevent deallocation
// after Java_io_realm_internal_SharedGroup_nativeStartSession completes.
// To be released later, maybe on JNI_OnUnload
std::thread* sync_client_thread;
JNIEnv* sync_client_env;

JNIEXPORT jlong JNICALL Java_io_realm_sync_SyncManager_syncCreateClient
  (JNIEnv *env, jclass)
{
    TR_ENTER(env)
    try {
        AndroidLogger* base_logger = new AndroidLogger();//FIXME find a way to delete it when we delete the client

        sync::Client::Config config;
        config.logger = base_logger;
        //config.reconnect = sync::Client::Reconnect::immediately;// for testing?
        config.reconnect = sync::Client::Reconnect::normal;

        sync::Client* m_sync_client = new sync::Client(config);
        sync_client_thread = new std::thread([m_sync_client](){
            //Attaching thread to Java so we can perform JNI calls
            JavaVMAttachArgs args;
            args.version = JNI_VERSION_1_6;
            args.name = NULL; // java thread a name
            args.group = NULL; // java thread group
            g_vm->AttachCurrentThread(&sync_client_env, &args);

            m_sync_client->run();
        });

        return reinterpret_cast<jlong>(m_sync_client);

    } CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_sync_SyncManager_syncCreateSession
  (JNIEnv *env, jclass, jlong clientPointer, jstring realmPath, jstring serverUrl, jstring userToken)
{
    TR_ENTER(env)
    Client* sync_client = SC(clientPointer);
    if (sync_client == NULL) {
        return 0;
    }
    try {
        const char *token_tmp = env->GetStringUTFChars(userToken, NULL);
        std::string user_token(token_tmp);
        env->ReleaseStringUTFChars(userToken, token_tmp);

        const char *path_tmp = env->GetStringUTFChars(realmPath, NULL);
        std::string path(path_tmp);
        env->ReleaseStringUTFChars(realmPath, path_tmp);

        JStringAccessor server_url_tmp(env, serverUrl); // throws
        StringData server_url = StringData(server_url_tmp);

        Session* sync_session = new Session(*sync_client, path);

        std::function<Session::SyncTransactCallback> sync_transact_callback = [path](Session::version_type) {
            sync_client_env->CallStaticVoidMethod(sync_manager, sync_manager_notify_handler, sync_client_env->NewStringUTF(path.c_str()));//REALM_CHANGE
        };
        sync_session->set_sync_transact_callback(sync_transact_callback);
        sync_session->bind(server_url, user_token);
        return reinterpret_cast<jlong>(sync_session);
    } CATCH_STD()
    return 0;
}

