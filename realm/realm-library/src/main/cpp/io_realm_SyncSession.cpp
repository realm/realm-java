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

#include "io_realm_SyncSession.h"
#include "objectserver_shared.hpp"
#include "object-store/src/sync/sync_session.hpp"
#include "jni_util/log.hpp"

using namespace std;
using namespace realm;
using namespace sync;

/*
JNIEXPORT jlong JNICALL
Java_io_realm_SyncSession_nativeGetSession(JNIEnv *env, jobject, jstring localRealmPath, jlong nativeConfigWrapperPointer) {
    JStringAccessor local_realm_path(env, localRealmPath);
    auto config_wrapper = reinterpret_cast<JniConfigWrapper*>(nativeConfigWrapperPointer);
    std::shared_ptr<SyncSession> session = SyncManager::shared().get_session(local_realm_path, *config_wrapper->get_config()->sync_config.get());
    return reinterpret_cast<jlong>(session.get());
}
*/

/*
JNIEXPORT jlong JNICALL
Java_io_realm_SyncSession_nativeCreateSession
  (JNIEnv *env, jobject sessionObject, jstring localRealmPath, jstring userIdentity)
{
    TR_ENTER()
//    try {
//        JStringAccessor local_path(env, localRealmPath);
//        JStringAccessor user_identity(env, userIdentity);
//
//        std::shared_ptr<SyncUser> user = SyncManager::shared().get_existing_logged_in_user(user_identity);
//        if (!user)
//        {
////            ThrowException(env, IllegalArgument, "User wasn't logged in: " + std::string(user_identity));
////            return realm::not_found;
//            // FIXME work-around until user migration complete
//            realm::jni_util::Log::e("BEFORE GET_USER");
//            user = SyncManager::shared().get_user(user_identity, "foo", realm::util::Optional<std::string>("http://auth.realm.io/auth"));
//            realm::jni_util::Log::e("AFTER GET_USER");
//        }
//
//        SyncManager::share
//
//        JniSession* jni_session = new JniSession(env, local_path, user, sessionObject);
//        realm::jni_util::Log::e("GOT JNI_SESSION");
//        return reinterpret_cast<jlong>(jni_session);
//    } CATCH_STD()
    return 0;
}
*/

JNIEXPORT void JNICALL
Java_io_realm_SyncSession_nativeStartSession
        (JNIEnv *env, jobject, jstring localRealmPath)
{
    TR_ENTER()
    try {
        std::ostringstream ss;
        std::thread::id this_id = std::this_thread::get_id();
        ss << "StartSession: " << this_id;
        realm::jni_util::Log::e(ss.str().c_str());

        JStringAccessor local_realm_path(env, localRealmPath);
        std::shared_ptr<SyncSession> session = SyncManager::shared().get_existing_active_session(local_realm_path);
        if (!session) {
            std::ostringstream ss;
            ss << "No session found for " << local_realm_path;
            ThrowException(env, IllegalState, ss.str());
        }  else {
            SyncSession::revive_if_needed(session);
        }
    } CATCH_STD()
}

JNIEXPORT void JNICALL
Java_io_realm_SyncSession_nativeStopSession
        (JNIEnv *env, jobject, jstring localRealmPath)
{
    TR_ENTER()
    try {
        JStringAccessor local_realm_path(env, localRealmPath);
        std::shared_ptr<SyncSession> session = SyncManager::shared().get_existing_active_session(local_realm_path);
        if (!session) {
            std::ostringstream ss;
            ss << "No session found for " << local_realm_path;
            ThrowException(env, IllegalState, ss.str());
        }  else {
            session->close();
        }
    } CATCH_STD()
}

//JNIEXPORT void JNICALL Java_io_realm_SyncSession_nativeBind
//  (JNIEnv *env, jobject, jlong sessionPointer, jstring remoteUrl, jstring accessToken)
//{
//    TR_ENTER()
//    try {
//        auto *session_wrapper = reinterpret_cast<JniSession*>(sessionPointer);
//
//        const char *token_tmp = env->GetStringUTFChars(accessToken, NULL);
//        std::string access_token(token_tmp);
//        env->ReleaseStringUTFChars(accessToken, token_tmp);
//
//        JStringAccessor url_tmp(env, remoteUrl); // throws
//        StringData remote_url = StringData(url_tmp);
//
//        // Bind the local Realm to the remote one
//        session_wrapper->get_session()->bind(remote_url, access_token);
//    } CATCH_STD()
//}
//
//
//JNIEXPORT void JNICALL Java_io_realm_SyncSession_nativeUnbind
//  (JNIEnv *env, jobject, jlong sessionPointer)
//{
//    TR_ENTER()
//    JniSession* session = SS(sessionPointer);
//    session->close(env);
//    delete session; // TODO Can we avoid killing the session here?
//}

//JNIEXPORT void JNICALL Java_io_realm_SyncSession_nativeRefresh
//  (JNIEnv *env, jobject, jlong sessionPointer, jstring accessToken)
//{
//    TR_ENTER()
//    try {
//        JniSession* session_wrapper = SS(sessionPointer);
//
//        JStringAccessor token_tmp(env, accessToken); // throws
//        StringData access_token = StringData(token_tmp);
//
//        session_wrapper->get_session()->refresh(access_token);
//    } CATCH_STD()
//}
//
//JNIEXPORT void JNICALL
//Java_io_realm_SyncSession_nativeNotifyCommitHappened
//  (JNIEnv *env, jobject, jlong sessionPointer, jlong version)
//{
//    TR_ENTER()
//    try {
//        JniSession* session_wrapper = SS(sessionPointer);
//        session_wrapper->get_session()->nonsync_transact_notify(version);
//    } CATCH_STD()
//}


