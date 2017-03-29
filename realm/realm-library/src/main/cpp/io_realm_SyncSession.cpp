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
                                                                              jstring j_local_realm_path,
                                                                              jstring j_access_token,
                                                                              jstring sync_realm_url)
{
    TR_ENTER()
    try {
        JStringAccessor local_realm_path(env, j_local_realm_path);
        auto session = SyncManager::shared().get_existing_session(local_realm_path);
        if (session) {
            JStringAccessor access_token(env, j_access_token);
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
                                                                            jstring j_local_realm_path, jlong listener_id,
                                                                            jint direction, jboolean is_streaming)
{
    try {
        // JNIEnv is thread confined, so we need a deep copy in order to capture the string in the lambda
        std::string local_realm_path(JStringAccessor(env, j_local_realm_path));
        std::shared_ptr<SyncSession> session = SyncManager::shared().get_existing_active_session(local_realm_path);
        if (!session) {
            // FIXME: We should lift this restriction
            ThrowException(env, IllegalState, "Cannot register a progress listener before a session is "
                                              "created. A session will be created after the first call to Realm.getInstance().");
            return static_cast<jlong>(0);
        }

        SyncSession::NotifierType type =
            (direction == 1) ? SyncSession::NotifierType::download : SyncSession::NotifierType::upload;

        std::function<SyncProgressNotifierCallback> callback = [local_realm_path, listener_id](
            uint64_t transferred, uint64_t transferrable) {
            JNIEnv* env = jni_util::JniUtils::get_env(true);

            auto path = env->NewStringUTF(local_realm_path.c_str());
            env->CallStaticVoidMethod(java_syncmanager_class, java_notify_progress_listener, path, listener_id,
                                      static_cast<jlong>(transferred), static_cast<jlong>(transferrable));

            // All exceptions will be caught on the Java side of handlers, but errors will still end
            // up here, so we need to do something sensible with them.
            // Throwing a C++ exception will terminate the sync thread and cause the pending Java
            // exception to become visible. For some (unknown) reason Logcat will not see the C++
            // exception, only the Java one.
            if (env->ExceptionCheck()) {
                throw std::runtime_error("An unexpected Error was thrown from Java");
            }

            // Callback happens on a thread not controlled by the JVM. So manual cleanup is
            // required.
            env->DeleteLocalRef(path);
        };
        uint64_t token = session->register_progress_notifier(callback, type, to_bool(isStreaming));
        return static_cast<jlong>(token);
    }
    CATCH_STD()
    return static_cast<jlong>(0);
}

JNIEXPORT void JNICALL Java_io_realm_SyncSession_nativeRemoveProgressListener(JNIEnv* env, jclass,
                                                                              jstring j_local_realm_path,
                                                                              jlong listener_token)
{
    try {
        JStringAccessor local_realm_path(env, j_local_realm_path);
        std::shared_ptr<SyncSession> session = SyncManager::shared().get_existing_active_session(local_realm_path);
        if (session) {
            session->unregister_progress_notifier(static_cast<uint64_t>(listener_token));
        }
    }
    CATCH_STD()
}
