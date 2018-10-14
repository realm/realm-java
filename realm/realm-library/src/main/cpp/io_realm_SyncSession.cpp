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
#include "java_class_global_def.hpp"
#include "jni_util/java_global_ref.hpp"
#include "jni_util/java_local_ref.hpp"
#include "jni_util/java_method.hpp"
#include "jni_util/java_class.hpp"
#include "jni_util/jni_utils.hpp"

using namespace realm;
using namespace realm::jni_util;
using namespace realm::sync;
using namespace realm::_impl;

static_assert(SyncSession::PublicState::WaitingForAccessToken ==
                  static_cast<SyncSession::PublicState>(io_realm_SyncSession_STATE_VALUE_WAITING_FOR_ACCESS_TOKEN),
              "");
static_assert(SyncSession::PublicState::Active ==
                  static_cast<SyncSession::PublicState>(io_realm_SyncSession_STATE_VALUE_ACTIVE),
              "");
static_assert(SyncSession::PublicState::Dying ==
                  static_cast<SyncSession::PublicState>(io_realm_SyncSession_STATE_VALUE_DYING),
              "");
static_assert(SyncSession::PublicState::Inactive ==
                  static_cast<SyncSession::PublicState>(io_realm_SyncSession_STATE_VALUE_INACTIVE),
              "");

static_assert(SyncSession::ConnectionState::Disconnected ==
              static_cast<SyncSession::ConnectionState >(io_realm_SyncSession_CONNECTION_VALUE_DISCONNECTED),
              "");
static_assert(SyncSession::ConnectionState::Connecting ==
              static_cast<SyncSession::ConnectionState>(io_realm_SyncSession_CONNECTION_VALUE_CONNECTING),
              "");
static_assert(SyncSession::ConnectionState::Connected ==
              static_cast<SyncSession::ConnectionState>(io_realm_SyncSession_CONNECTION_VALUE_CONNECTED),
              "");

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

            session->refresh_access_token(access_token, std::string(session->config().realm_url()));
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

            JavaLocalRef<jstring> path(local_env, to_jstring(local_env, local_realm_path));
            local_env->CallStaticVoidMethod(java_syncmanager_class, java_notify_progress_listener, path.get(),
                                            listener_id, static_cast<jlong>(transferred),
                                            static_cast<jlong>(transferrable));

            // All exceptions will be caught on the Java side of handlers, but Errors will still end
            // up here, so we need to do something sensible with them.
            // Throwing a C++ exception will terminate the sync thread and cause the pending Java
            // exception to become visible. For some (unknown) reason Logcat will not see the C++
            // exception, only the Java one.
            if (local_env->ExceptionCheck()) {
                local_env->ExceptionDescribe();
                throw std::runtime_error("An unexpected Error was thrown from Java. See LogCat");
            }
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
                                                                                     jint callback_id,
                                                                                     jstring j_local_realm_path)
{
    TR_ENTER()
    try {
        JStringAccessor local_realm_path(env, j_local_realm_path);
        auto session = SyncManager::shared().get_existing_session(local_realm_path);

        if (session) {
            static JavaClass java_sync_session_class(env, "io/realm/SyncSession");
            static JavaMethod java_notify_result_method(env, java_sync_session_class, "notifyAllChangesSent",
                                                        "(ILjava/lang/Long;Ljava/lang/String;)V");
            JavaGlobalRef java_session_object_ref(env, session_object);

            bool listener_registered =
                session->wait_for_download_completion([java_session_object_ref, callback_id](std::error_code error) {
                    JNIEnv* env = JniUtils::get_env(true);
                    JavaLocalRef<jobject> java_error_code;
                    JavaLocalRef<jstring> java_error_message;
                    if (error != std::error_code{}) {
                        java_error_code =
                            JavaLocalRef<jobject>(env, JavaClassGlobalDef::new_long(env, error.value()));
                        java_error_message = JavaLocalRef<jstring>(env, env->NewStringUTF(error.message().c_str()));
                    }
                    env->CallVoidMethod(java_session_object_ref.get(), java_notify_result_method,
                                        callback_id, java_error_code.get(), java_error_message.get());
                });

            return to_jbool(listener_registered);
        }
    }
    CATCH_STD()
    return JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_io_realm_SyncSession_nativeWaitForUploadCompletion(JNIEnv* env,
                                                                                   jobject session_object,
                                                                                   jint callback_id,
                                                                                   jstring j_local_realm_path)
{
    TR_ENTER()
    try {
        JStringAccessor local_realm_path(env, j_local_realm_path);
        auto session = SyncManager::shared().get_existing_session(local_realm_path);

        if (session) {
            static JavaClass java_sync_session_class(env, "io/realm/SyncSession");
            static JavaMethod java_notify_result_method(env, java_sync_session_class, "notifyAllChangesSent",
                                                        "(ILjava/lang/Long;Ljava/lang/String;)V");
            JavaGlobalRef java_session_object_ref(env, session_object);

            bool listener_registered =
                session->wait_for_upload_completion([java_session_object_ref, callback_id](std::error_code error) {
                    JNIEnv* env = JniUtils::get_env(true);
                    JavaLocalRef<jobject> java_error_code;
                    JavaLocalRef<jstring> java_error_message;
                    if (error != std::error_code{}) {
                        java_error_code = JavaLocalRef<jobject>(env, JavaClassGlobalDef::new_long(env, error.value()));
                        java_error_message = JavaLocalRef<jstring>(env, env->NewStringUTF(error.message().c_str()));
                    }
                    env->CallVoidMethod(java_session_object_ref.get(), java_notify_result_method,
                                        callback_id, java_error_code.get(), java_error_message.get());
                });

            return to_jbool(listener_registered);
        }
    }
    CATCH_STD()
    return JNI_FALSE;
}


JNIEXPORT jbyte JNICALL Java_io_realm_SyncSession_nativeGetState(JNIEnv* env, jclass, jstring j_local_realm_path)
{
    TR_ENTER()
    try {
        JStringAccessor local_realm_path(env, j_local_realm_path);
        auto session = SyncManager::shared().get_existing_session(local_realm_path);

        if (session) {
            switch (session->state()) {
                case SyncSession::PublicState::WaitingForAccessToken:
                    return io_realm_SyncSession_STATE_VALUE_WAITING_FOR_ACCESS_TOKEN;
                case SyncSession::PublicState::Active:
                    return io_realm_SyncSession_STATE_VALUE_ACTIVE;
                case SyncSession::PublicState::Dying:
                    return io_realm_SyncSession_STATE_VALUE_DYING;
                case SyncSession::PublicState::Inactive:
                    return io_realm_SyncSession_STATE_VALUE_INACTIVE;
            }
        }
    }
    CATCH_STD()
    return -1;
}

JNIEXPORT jbyte JNICALL Java_io_realm_SyncSession_nativeGetConnectionState(JNIEnv* env, jclass, jstring j_local_realm_path)
{
    TR_ENTER()
    try {
        JStringAccessor local_realm_path(env, j_local_realm_path);
        auto session = SyncManager::shared().get_existing_session(local_realm_path);

        if (session) {
            switch (session->connection_state()) {
                case SyncSession::ConnectionState::Disconnected:
                    return io_realm_SyncSession_CONNECTION_VALUE_DISCONNECTED;
                case SyncSession::ConnectionState::Connecting:
                    return io_realm_SyncSession_CONNECTION_VALUE_CONNECTING;
                case SyncSession::ConnectionState::Connected:
                    return io_realm_SyncSession_CONNECTION_VALUE_CONNECTED;
            }
        }
    }
    CATCH_STD()
    return -1;
}

static jlong get_connection_value(SyncSession::ConnectionState state) {
    switch (state) {
        case SyncSession::ConnectionState::Disconnected: return static_cast<jlong>(io_realm_SyncSession_CONNECTION_VALUE_DISCONNECTED);
        case SyncSession::ConnectionState::Connecting: return static_cast<jlong>(io_realm_SyncSession_CONNECTION_VALUE_CONNECTING);
        case SyncSession::ConnectionState::Connected: return static_cast<jlong>(io_realm_SyncSession_CONNECTION_VALUE_CONNECTED);
    }
    return static_cast<jlong>(-1);
}

JNIEXPORT jlong JNICALL Java_io_realm_SyncSession_nativeAddConnectionListener(JNIEnv* env, jclass, jstring j_local_realm_path)
{
    try {
        // JNIEnv is thread confined, so we need a deep copy in order to capture the string in the lambda
        std::string local_realm_path(JStringAccessor(env, j_local_realm_path));
        std::shared_ptr<SyncSession> session = SyncManager::shared().get_existing_session(local_realm_path);
        if (!session) {
            // FIXME: We should lift this restriction
            ThrowException(env, IllegalState,
            "Cannot register a connection listener before a session is "
            "created. A session will be created after the first call to Realm.getInstance().");
            return 0;
        }

        static JavaClass java_syncmanager_class(env, "io/realm/SyncManager");
        static JavaMethod java_notify_connection_listener(env, java_syncmanager_class, "notifyConnectionListeners", "(Ljava/lang/String;JJ)V", true);

        std::function<SyncSession::ConnectionStateCallback > callback = [local_realm_path](SyncSession::ConnectionState old_state, SyncSession::ConnectionState new_state) {
            JNIEnv* local_env = jni_util::JniUtils::get_env(true);

            jlong old_connection_value = get_connection_value(old_state);
            jlong new_connection_value = get_connection_value(new_state);

            JavaLocalRef<jstring> path(local_env, to_jstring(local_env, local_realm_path));
            local_env->CallStaticVoidMethod(java_syncmanager_class, java_notify_connection_listener, path.get(),
                                        old_connection_value, new_connection_value);

            // All exceptions will be caught on the Java side of handlers, but Errors will still end
            // up here, so we need to do something sensible with them.
            // Throwing a C++ exception will terminate the sync thread and cause the pending Java
            // exception to become visible. For some (unknown) reason Logcat will not see the C++
            // exception, only the Java one.
            if (local_env->ExceptionCheck()) {
                local_env->ExceptionDescribe();
                throw std::runtime_error("An unexpected Error was thrown from Java. See LogCat");
            }
        };
        uint64_t token = session->register_connection_change_callback(callback);
        return static_cast<jlong>(token);
    }
    CATCH_STD()
    return 0;
}

JNIEXPORT void JNICALL Java_io_realm_SyncSession_nativeRemoveConnectionListener(JNIEnv* env, jclass, jlong listener_id, jstring j_local_realm_path)
{
    try {
        // JNIEnv is thread confined, so we need a deep copy in order to capture the string in the lambda
        std::string local_realm_path(JStringAccessor(env, j_local_realm_path));
        std::shared_ptr<SyncSession> session = SyncManager::shared().get_existing_session(local_realm_path);
        if (session) {
            session->unregister_connection_change_callback(static_cast<uint64_t>(listener_id));
        }
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_SyncSession_nativeStart(JNIEnv* env, jclass, jstring j_local_realm_path)
{
    TR_ENTER()
    try {
        JStringAccessor local_realm_path(env, j_local_realm_path);
        auto session = SyncManager::shared().get_existing_session(local_realm_path);
        if (!session) {
            // FIXME: We should lift this restriction
            ThrowException(env, IllegalState,
            "Cannot call start() before a session is "
            "created. A session will be created after the first call to Realm.getInstance().");
            return;
        }
        session->revive_if_needed();
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_SyncSession_nativeStop(JNIEnv* env, jclass, jstring j_local_realm_path)
{
    TR_ENTER()
    try {
        JStringAccessor local_realm_path(env, j_local_realm_path);
        auto session = SyncManager::shared().get_existing_session(local_realm_path);
        if (session) {
            session->log_out();
        }
    }
    CATCH_STD()
}
