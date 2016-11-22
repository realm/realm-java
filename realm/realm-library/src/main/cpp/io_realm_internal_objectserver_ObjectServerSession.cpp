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

#include "io_realm_internal_objectserver_ObjectServerSession.h"
#include "objectserver_shared.hpp"
#include "util.hpp"
#include <realm/group_shared.hpp>
#include <realm/replication.hpp>

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


JNIEXPORT jlong JNICALL Java_io_realm_internal_objectserver_ObjectServerSession_nativeCreateSession
  (JNIEnv *env, jobject obj, jstring localRealmPath)
{
    TR_ENTER()
    try {
        JStringAccessor local_path(env, localRealmPath);
        JniSession* jni_session = new JniSession(env, local_path, obj);
        return reinterpret_cast<jlong>(jni_session);
    } CATCH_STD()
    return 0;
}

JNIEXPORT void JNICALL Java_io_realm_internal_objectserver_ObjectServerSession_nativeBind
  (JNIEnv *env, jobject, jlong sessionPointer, jstring remoteUrl, jstring accessToken)
{
    TR_ENTER()
    try {
        auto *session_wrapper = reinterpret_cast<JniSession*>(sessionPointer);

        const char *token_tmp = env->GetStringUTFChars(accessToken, NULL);
        std::string access_token(token_tmp);
        env->ReleaseStringUTFChars(accessToken, token_tmp);

        JStringAccessor url_tmp(env, remoteUrl); // throws
        StringData remote_url = StringData(url_tmp);

        // Bind the local Realm to the remote one
        session_wrapper->get_session()->bind(remote_url, access_token);
    } CATCH_STD()
}


JNIEXPORT void JNICALL Java_io_realm_internal_objectserver_ObjectServerSession_nativeUnbind
  (JNIEnv *env, jobject, jlong sessionPointer)
{
    TR_ENTER()
    JniSession* session = SS(sessionPointer);
    session->close(env);
    delete session; // TODO Can we avoid killing the session here?
}

JNIEXPORT void JNICALL Java_io_realm_internal_objectserver_ObjectServerSession_nativeRefresh
  (JNIEnv *env, jobject, jlong sessionPointer, jstring accessToken)
{
    TR_ENTER()
    try {
        JniSession* session_wrapper = SS(sessionPointer);

        JStringAccessor token_tmp(env, accessToken); // throws
        StringData access_token = StringData(token_tmp);

        session_wrapper->get_session()->refresh(access_token);
    } CATCH_STD()
}

JNIEXPORT void JNICALL
Java_io_realm_internal_objectserver_ObjectServerSession_nativeNotifyCommitHappened
  (JNIEnv *env, jobject, jlong sessionPointer, jlong version)
{
    TR_ENTER()
    try {
        JniSession* session_wrapper = SS(sessionPointer);
        session_wrapper->get_session()->nonsync_transact_notify(version);
    } CATCH_STD()
}


