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
#include "jni_util/java_global_ref.hpp"
#include "jni_util/java_method.hpp"
#include "jni_util/java_class.hpp"
#include "jni_util/java_local_ref.hpp"
#include "jni_util/jni_utils.hpp"

using namespace realm;
using namespace jni_util;
using namespace sync;

JNIEXPORT jboolean JNICALL Java_io_realm_SyncSession_nativeRefreshAccessToken(JNIEnv* env, jclass,
                                                                              jstring j_local_realm_path,
                                                                              jstring j_access_token,
                                                                              jstring j_sync_realm_url)
{
    TR_ENTER()
    try {
        JStringAccessor local_realm_path(env, j_local_realm_path);
        auto session = SyncManager::shared().get_existing_session(local_realm_path);
        if (session) {
            JStringAccessor access_token(env, j_access_token);
            JStringAccessor realm_url(env, j_sync_realm_url);
            session->refresh_access_token(access_token, std::string(realm_url));
            return JNI_TRUE;
        }
        else {
            Log::d("no active/inactive session found");
        }
    }
    CATCH_STD()
    return JNI_FALSE;
}

JNIEXPORT jlong JNICALL Java_io_realm_SyncSession_nativeAddProgressListener(JNIEnv* env, jclass,
                                                                            jstring j_local_realm_path,
                                                                            jlong listener_id, jint direction,
                                                                            jboolean is_streaming)
{
    try {
        // JNIEnv is thread confined, so we need a deep copy in order to capture the string in the lambda
        std::string local_realm_path(JStringAccessor(env, j_local_realm_path));
        std::shared_ptr<SyncSession> session = SyncManager::shared().get_existing_active_session(local_realm_path);
        if (!session) {
            // FIXME: We should lift this restriction
            ThrowException(env, IllegalState,
                           "Cannot register a progress listener before a session is "
                           "created. A session will be created after the first call to Realm.getInstance().");
            return 0;
        }

        SyncSession::NotifierType type =
            (direction == 1) ? SyncSession::NotifierType::download : SyncSession::NotifierType::upload;

        static JavaClass java_syncmanager_class(env, "io/realm/SyncManager");
        static JavaMethod java_notify_progress_listener(env, java_syncmanager_class, "notifyProgressListener", "(Ljava/lang/String;JJJ)V", true);

        std::function<SyncProgressNotifierCallback> callback = [local_realm_path, listener_id](
            uint64_t transferred, uint64_t transferrable) {
            JNIEnv* local_env = jni_util::JniUtils::get_env(true);

            auto path = to_jstring(local_env, local_realm_path);
            local_env->CallStaticVoidMethod(java_syncmanager_class, java_notify_progress_listener, path, listener_id,
                                            static_cast<jlong>(transferred), static_cast<jlong>(transferrable));

            // All exceptions will be caught on the Java side of handlers, but Errors will still end
            // up here, so we need to do something sensible with them.
            // Throwing a C++ exception will terminate the sync thread and cause the pending Java
            // exception to become visible. For some (unknown) reason Logcat will not see the C++
            // exception, only the Java one.
            if (local_env->ExceptionCheck()) {
                local_env->ExceptionDescribe();
                throw std::runtime_error("An unexpected Error was thrown from Java. See LogCat");
            }

            // Callback happens on a thread not controlled by the JVM. So manual cleanup is
            // required.
            local_env->DeleteLocalRef(path);
        };
        uint64_t token = session->register_progress_notifier(callback, type, to_bool(is_streaming));
        return static_cast<jlong>(token);
    }
    CATCH_STD()
    return 0;
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

JNIEXPORT jboolean JNICALL Java_io_realm_SyncSession_nativeWaitForDownloadCompletion(JNIEnv* env,
                                                                                     jobject session_object,
                                                                                     jstring j_local_realm_path)
{
    TR_ENTER()
    try {
        JStringAccessor local_realm_path(env, j_local_realm_path);
        auto session = SyncManager::shared().get_existing_session(local_realm_path);

        if (session) {
            static JavaClass java_sync_session_class(env, "io/realm/SyncSession");
            static JavaMethod java_notify_result_method(env, java_sync_session_class, "notifyAllChangesDownloaded",
                                                        "(Ljava/lang/Long;Ljava/lang/String;)V");
            JavaGlobalRef java_session_object_ref(env, session_object);

            bool listener_registered =
                session->wait_for_download_completion([java_session_object_ref](std::error_code error) {
                    JNIEnv* env = JniUtils::get_env(true);
                    jobject java_error_code = nullptr;
                    jstring java_error_message = nullptr;
                    if (error != std::error_code{}) {
                        java_error_code = NewLong(env, error.value());
                        java_error_message = env->NewStringUTF(error.message().c_str());
                    }
                    env->CallVoidMethod(java_session_object_ref.get(), java_notify_result_method, java_error_code,
                                        java_error_message);
                });

            return to_jbool(listener_registered);
        }
    }
    CATCH_STD()
    return JNI_FALSE;
}
