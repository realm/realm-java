/*
 * Copyright 2017 Realm Inc.
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
#include <string>

#include "io_realm_SyncSession.h"

#include "object-store/src/sync/sync_manager.hpp"
#include "object-store/src/sync/sync_session.hpp"

#include "util.hpp"
#include "jni_util/jni_utils.hpp"

using namespace realm;
using namespace sync;

JNIEXPORT jboolean JNICALL Java_io_realm_SyncSession_nativeRefreshAccessToken(JNIEnv* env, jclass,
                                                                              jstring localRealmPath,
                                                                              jstring accessToken,
                                                                              jstring sync_realm_url)
{
    TR_ENTER()
    try {
        JStringAccessor local_realm_path(env, localRealmPath);
        auto session = SyncManager::shared().get_existing_session(local_realm_path);
        if (session) {
            JStringAccessor access_token(env, accessToken);
            JStringAccessor realm_url(env, sync_realm_url);
            session->refresh_access_token(access_token, std::string(realm_url));
            return JNI_TRUE;
        }
        else {
            realm::jni_util::Log::d("no active/inactive session found");
        }
    }
    CATCH_STD()
    return JNI_FALSE;
}


JNIEXPORT jlong JNICALL Java_io_realm_SyncSession_nativeAddProgressListener(JNIEnv* env, jclass,
                                                                            jstring localRealmPath, jlong listenerId,
                                                                            jint direction, jboolean isStreaming)
{
    try {
        JStringAccessor local_realm_path(env, localRealmPath);
        std::shared_ptr<SyncSession> session = SyncManager::shared().get_existing_active_session(local_realm_path);
        if (!session) {
            ThrowException(env, IllegalState, "FIXME: Cannot register a progress listener before a session is "
                                              "created. Only happens during Realm.getInstance() right now.");
            return static_cast<jlong>(0);
        }

        SyncSession::NotifierType type =
            (direction == 1) ? SyncSession::NotifierType::download : SyncSession::NotifierType::upload;

        std::function<SyncProgressNotifierCallback> callback = [localRealmPath, listenerId](uint64_t transferred,
                                                                                            uint64_t transferrable) {
            JNIEnv* env = jni_util::JniUtils::get_env(true);
            env->CallStaticVoidMethod(java_syncmanager, java_notify_progress_listener, localRealmPath, listenerId,
                                      static_cast<jlong>(transferred), static_cast<jlong>(transferrable));
        };
        uint64_t token = session->register_progress_notifier(callback, type, to_bool(isStreaming));
        return static_cast<jlong>(token);
    }
    CATCH_STD()
    return static_cast<jlong>(0);
}

JNIEXPORT void JNICALL Java_io_realm_SyncSession_nativeRemoveProgressListener(JNIEnv* env, jclass,
                                                                              jstring localRealmPath,
                                                                              jlong listenerToken)
{
    try {
        JStringAccessor local_realm_path(env, localRealmPath);
        std::shared_ptr<SyncSession> session = SyncManager::shared().get_existing_active_session(local_realm_path);
        if (session) {
            session->unregister_progress_notifier(static_cast<uint64_t>(listenerToken));
        }
    }
    CATCH_STD()
}
