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

#include "io_realm_objectserver_session_Session.h"
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

using namespace std;
using namespace realm;
using namespace sync;


JNIEXPORT jlong JNICALL Java_io_realm_objectserver_session_Session_nativeCreateSession
  (JNIEnv *env, jobject, jlong clientPointer, jstring localRealmPath)
{
    TR_ENTER()
    Client* sync_client = SC(clientPointer);
    if (sync_client == NULL) {
        return 0;
    }
    try {
        const char *path_tmp = env->GetStringUTFChars(localRealmPath, NULL);
        std::string local_path(path_tmp);
        env->ReleaseStringUTFChars(localRealmPath, path_tmp);

        Session* sync_session = new Session(*sync_client, local_path);

        std::function<Session::SyncTransactCallback> sync_transact_callback = [local_path](Session::version_type) {
            // FIXME Realm changed
            // FIXME Install error handler
            sync_client_env->CallStaticVoidMethod(sync_manager, sync_manager_notify_handler, sync_client_env->NewStringUTF(local_path.c_str()));//REALM_CHANGE
        };
        sync_session->set_sync_transact_callback(sync_transact_callback);
        return reinterpret_cast<jlong>(sync_session);
    } CATCH_STD()
    return 0;
}

JNIEXPORT void JNICALL Java_io_realm_objectserver_session_Session_nativeBind
  (JNIEnv *env, jobject, jlong sessionPointer, jstring remoteUrl, jstring accessToken)
{
    TR_ENTER()
    try {
        Session* session = SS(sessionPointer);

        const char *token_tmp = env->GetStringUTFChars(accessToken, NULL);
        std::string access_token(token_tmp);
        env->ReleaseStringUTFChars(accessToken, token_tmp);

        JStringAccessor url_tmp(env, remoteUrl); // throws
        StringData remote_url = StringData(url_tmp);

        // Bind the local Realm to the remote one
        session->bind(remote_url, access_token);
    } CATCH_STD()
}


JNIEXPORT void JNICALL Java_io_realm_objectserver_session_Session_nativeUnbind
  (JNIEnv *, jobject, jlong sessionPointer)
{
    TR_ENTER()
    Session* session = SS(sessionPointer);
    delete session; // TODO Can we avoid killing the session here?
}

JNIEXPORT void JNICALL Java_io_realm_objectserver_session_Session_nativeRefresh
  (JNIEnv *env, jobject, jlong sessionPointer, jstring accessToken)
{
    TR_ENTER()
    try {
        Session* session = SS(sessionPointer);

        JStringAccessor token_tmp(env, accessToken); // throws
        StringData access_token = StringData(token_tmp);

        session->refresh(access_token);
    } CATCH_STD()
}
