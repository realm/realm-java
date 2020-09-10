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

#include "io_realm_mongodb_sync_SyncSession.h"

#include "sync/app.hpp"
#include "sync/sync_manager.hpp"
#include "sync/sync_session.hpp"

#include "util.hpp"
#include "java_class_global_def.hpp"
#include "jni_util/java_global_ref_by_move.hpp"
#include "jni_util/java_global_ref_by_copy.hpp"
#include "jni_util/java_local_ref.hpp"
#include "jni_util/java_method.hpp"
#include "jni_util/java_class.hpp"
#include "jni_util/jni_utils.hpp"

using namespace realm;
using namespace realm::jni_util;
using namespace realm::sync;
using namespace realm::_impl;

static_assert(SyncSession::PublicState::Active ==
                  static_cast<SyncSession::PublicState>(io_realm_mongodb_sync_SyncSession_STATE_VALUE_ACTIVE),
              "");
static_assert(SyncSession::PublicState::Dying ==
                  static_cast<SyncSession::PublicState>(io_realm_mongodb_sync_SyncSession_STATE_VALUE_DYING),
              "");
static_assert(SyncSession::PublicState::Inactive ==
                  static_cast<SyncSession::PublicState>(io_realm_mongodb_sync_SyncSession_STATE_VALUE_INACTIVE),
              "");

static_assert(SyncSession::ConnectionState::Disconnected ==
              static_cast<SyncSession::ConnectionState >(io_realm_mongodb_sync_SyncSession_CONNECTION_VALUE_DISCONNECTED),
              "");
static_assert(SyncSession::ConnectionState::Connecting ==
              static_cast<SyncSession::ConnectionState>(io_realm_mongodb_sync_SyncSession_CONNECTION_VALUE_CONNECTING),
              "");
static_assert(SyncSession::ConnectionState::Connected ==
              static_cast<SyncSession::ConnectionState>(io_realm_mongodb_sync_SyncSession_CONNECTION_VALUE_CONNECTED),
              "");

JNIEXPORT jlong JNICALL Java_io_realm_mongodb_sync_SyncSession_nativeAddProgressListener(JNIEnv* env, jlong j_app_ptr, jobject j_session_object,
                                                                            jstring j_local_realm_path,
                                                                            jlong listener_id, jint direction,
                                                                            jboolean is_streaming)
{
    try {
        auto app = *reinterpret_cast<std::shared_ptr<app::App>*>(j_app_ptr);
        // JNIEnv is thread confined, so we need a deep copy in order to capture the string in the lambda
        std::string local_realm_path(JStringAccessor(env, j_local_realm_path));
        std::shared_ptr<SyncSession> session = app->sync_manager()->get_existing_session(local_realm_path);
        if (!session) {
            // FIXME: We should lift this restriction
            ThrowException(env, IllegalState,
                           "Cannot register a progress listener before a session is "
                           "created. A session will be created after the first call to Realm.getInstance().");
            return 0;
        }

        SyncSession::NotifierType type = (direction == 1) ? SyncSession::NotifierType::download : SyncSession::NotifierType::upload;

        static JavaClass java_syncsession_class(env, "io/realm/mongodb/sync/SyncSession");
        static JavaMethod java_notify_progress_listener(env, java_syncsession_class, "notifyProgressListener", "(JJJ)V");

        auto callback = [session_ref = JavaGlobalRefByCopy(env, j_session_object), local_realm_path, listener_id](uint64_t transferred, uint64_t transferrable) {
            JNIEnv* local_env = jni_util::JniUtils::get_env(true);

            JavaLocalRef<jstring> path(local_env, to_jstring(local_env, local_realm_path));
            local_env->CallVoidMethod(session_ref.get(),
                    java_notify_progress_listener,
                    listener_id,
                    static_cast<jlong>(transferred),
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

JNIEXPORT void JNICALL Java_io_realm_mongodb_sync_SyncSession_nativeRemoveProgressListener(JNIEnv* env, jclass,
                                                                              jlong j_app_ptr,
                                                                              jstring j_local_realm_path,
                                                                              jlong listener_token)
{
    try {
        auto app = *reinterpret_cast<std::shared_ptr<app::App>*>(j_app_ptr);
        JStringAccessor local_realm_path(env, j_local_realm_path);
        std::shared_ptr<SyncSession> session = app->sync_manager()->get_existing_session(local_realm_path);
        if (session) {
            session->unregister_progress_notifier(static_cast<uint64_t>(listener_token));
        }
    }
    CATCH_STD()
}

JNIEXPORT jboolean JNICALL Java_io_realm_mongodb_sync_SyncSession_nativeWaitForDownloadCompletion(JNIEnv* env,
                                                                                     jlong j_app_ptr,
                                                                                     jobject session_object,
                                                                                     jint callback_id,
                                                                                     jstring j_local_realm_path)
{
    try {
        auto app = *reinterpret_cast<std::shared_ptr<app::App>*>(j_app_ptr);
        JStringAccessor local_realm_path(env, j_local_realm_path);
        auto session = app->sync_manager()->get_existing_session(local_realm_path);

        if (session) {
            static JavaClass java_sync_session_class(env, "io/realm/mongodb/sync/SyncSession");
            static JavaMethod java_notify_result_method(env, java_sync_session_class, "notifyAllChangesSent",
                                                        "(ILjava/lang/String;Ljava/lang/Long;Ljava/lang/String;)V");

            session->wait_for_download_completion([session_ref = JavaGlobalRefByCopy(env, session_object), callback_id](std::error_code error) {
                JNIEnv* env = JniUtils::get_env(true);
                JavaLocalRef<jstring> java_error_category;
                JavaLocalRef<jobject> java_error_code;
                JavaLocalRef<jstring> java_error_message;
                if (error != std::error_code{}) {
                    java_error_category = JavaLocalRef<jstring>(env, env->NewStringUTF(error.category().name()));
                    java_error_code = JavaLocalRef<jobject>(env, JavaClassGlobalDef::new_long(env, error.value()));
                    java_error_message = JavaLocalRef<jstring>(env, env->NewStringUTF(error.message().c_str()));
                }
                env->CallVoidMethod(session_ref.get(), java_notify_result_method,
                                    callback_id, java_error_category.get(), java_error_code.get(), java_error_message.get());
            });
            return to_jbool(JNI_TRUE);
        }
    }
    CATCH_STD()
    return JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_io_realm_mongodb_sync_SyncSession_nativeWaitForUploadCompletion(JNIEnv* env,
                                                                                   jlong j_app_ptr,
                                                                                   jobject session_object,
                                                                                   jint callback_id,
                                                                                   jstring j_local_realm_path)
{
    try {
        auto app = *reinterpret_cast<std::shared_ptr<app::App>*>(j_app_ptr);
        JStringAccessor local_realm_path(env, j_local_realm_path);
        auto session = app->sync_manager()->get_existing_session(local_realm_path);

        if (session) {
            static JavaClass java_sync_session_class(env, "io/realm/mongodb/sync/SyncSession");
            static JavaMethod java_notify_result_method(env, java_sync_session_class, "notifyAllChangesSent",
                                                        "(ILjava/lang/String;Ljava/lang/Long;Ljava/lang/String;)V");

            session->wait_for_upload_completion([session_ref = JavaGlobalRefByCopy(env, session_object), callback_id] (std::error_code error) {
                JNIEnv* env = JniUtils::get_env(true);
                JavaLocalRef<jstring> java_error_category;
                JavaLocalRef<jobject> java_error_code;
                JavaLocalRef<jstring> java_error_message;
                if (error != std::error_code{}) {
                    java_error_category = JavaLocalRef<jstring>(env, env->NewStringUTF(error.category().name()));
                    java_error_code = JavaLocalRef<jobject>(env, JavaClassGlobalDef::new_long(env, error.value()));
                    java_error_message = JavaLocalRef<jstring>(env, env->NewStringUTF(error.message().c_str()));
                }
                env->CallVoidMethod(session_ref.get(), java_notify_result_method,
                                    callback_id, java_error_category.get(), java_error_code.get(), java_error_message.get());
            });
            return JNI_TRUE;
        }
    }
    CATCH_STD()
    return JNI_FALSE;
}


JNIEXPORT jbyte JNICALL Java_io_realm_mongodb_sync_SyncSession_nativeGetState(JNIEnv* env, jclass, jlong j_app_ptr, jstring j_local_realm_path)
{
    try {
        auto app = *reinterpret_cast<std::shared_ptr<app::App>*>(j_app_ptr);
        JStringAccessor local_realm_path(env, j_local_realm_path);
        auto session = app->sync_manager()->get_existing_session(local_realm_path);

        if (session) {
            switch (session->state()) {
                case SyncSession::PublicState::Active:
                    return io_realm_mongodb_sync_SyncSession_STATE_VALUE_ACTIVE;
                case SyncSession::PublicState::Dying:
                    return io_realm_mongodb_sync_SyncSession_STATE_VALUE_DYING;
                case SyncSession::PublicState::Inactive:
                    return io_realm_mongodb_sync_SyncSession_STATE_VALUE_INACTIVE;
            }
        }
    }
    CATCH_STD()
    return -1;
}

JNIEXPORT jbyte JNICALL Java_io_realm_mongodb_sync_SyncSession_nativeGetConnectionState(JNIEnv* env, jclass, jlong j_app_ptr, jstring j_local_realm_path)
{
    try {
        auto app = *reinterpret_cast<std::shared_ptr<app::App>*>(j_app_ptr);
        JStringAccessor local_realm_path(env, j_local_realm_path);
        auto session = app->sync_manager()->get_existing_session(local_realm_path);

        if (session) {
            switch (session->connection_state()) {
                case SyncSession::ConnectionState::Disconnected:
                    return io_realm_mongodb_sync_SyncSession_CONNECTION_VALUE_DISCONNECTED;
                case SyncSession::ConnectionState::Connecting:
                    return io_realm_mongodb_sync_SyncSession_CONNECTION_VALUE_CONNECTING;
                case SyncSession::ConnectionState::Connected:
                    return io_realm_mongodb_sync_SyncSession_CONNECTION_VALUE_CONNECTED;
            }
        }
    }
    CATCH_STD()
    return -1;
}

static jlong get_connection_value(SyncSession::ConnectionState state) {
    switch (state) {
        case SyncSession::ConnectionState::Disconnected: return static_cast<jlong>(io_realm_mongodb_sync_SyncSession_CONNECTION_VALUE_DISCONNECTED);
        case SyncSession::ConnectionState::Connecting: return static_cast<jlong>(io_realm_mongodb_sync_SyncSession_CONNECTION_VALUE_CONNECTING);
        case SyncSession::ConnectionState::Connected: return static_cast<jlong>(io_realm_mongodb_sync_SyncSession_CONNECTION_VALUE_CONNECTED);
    }
    return static_cast<jlong>(-1);
}

JNIEXPORT jlong JNICALL Java_io_realm_mongodb_sync_SyncSession_nativeAddConnectionListener(JNIEnv* env, jobject j_session_object, jlong j_app_ptr, jstring j_local_realm_path)
{
    try {
        auto app = *reinterpret_cast<std::shared_ptr<app::App>*>(j_app_ptr);
        // JNIEnv is thread confined, so we need a deep copy in order to capture the string in the lambda
        std::string local_realm_path(JStringAccessor(env, j_local_realm_path));
        std::shared_ptr<SyncSession> session = app->sync_manager()->get_existing_session(local_realm_path);
        if (!session) {
            // FIXME: We should lift this restriction
            ThrowException(env, IllegalState,
            "Cannot register a connection listener before a session is "
            "created. A session will be created after the first call to Realm.getInstance().");
            return 0;
        }

        static JavaClass java_syncmanager_class(env, "io/realm/mongodb/sync/SyncSession");
        static JavaMethod java_notify_connection_listener(env, java_syncmanager_class, "notifyConnectionListeners", "(JJ)V");

        std::function<SyncSession::ConnectionStateCallback > callback = [session_ref = JavaGlobalRefByCopy(env, j_session_object)](SyncSession::ConnectionState old_state, SyncSession::ConnectionState new_state) {
            JNIEnv* local_env = jni_util::JniUtils::get_env(true);

            jlong old_connection_value = get_connection_value(old_state);
            jlong new_connection_value = get_connection_value(new_state);

            local_env->CallVoidMethod(session_ref.get(), java_notify_connection_listener,
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

JNIEXPORT void JNICALL Java_io_realm_mongodb_sync_SyncSession_nativeRemoveConnectionListener(JNIEnv* env, jclass, jlong j_app_ptr, jlong listener_id, jstring j_local_realm_path)
{
    try {
        auto app = *reinterpret_cast<std::shared_ptr<app::App>*>(j_app_ptr);
        // JNIEnv is thread confined, so we need a deep copy in order to capture the string in the lambda
        std::string local_realm_path(JStringAccessor(env, j_local_realm_path));
        std::shared_ptr<SyncSession> session = app->sync_manager()->get_existing_session(local_realm_path);
        if (session) {
            session->unregister_connection_change_callback(static_cast<uint64_t>(listener_id));
        }
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_mongodb_sync_SyncSession_nativeStart(JNIEnv* env, jclass, jlong j_app_ptr, jstring j_local_realm_path)
{
    try {
        auto app = *reinterpret_cast<std::shared_ptr<app::App>*>(j_app_ptr);
        JStringAccessor local_realm_path(env, j_local_realm_path);
        auto session = app->sync_manager()->get_existing_session(local_realm_path);
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

JNIEXPORT void JNICALL Java_io_realm_mongodb_sync_SyncSession_nativeStop(JNIEnv* env, jclass, jlong j_app_ptr, jstring j_local_realm_path)
{
    try {
        auto app = *reinterpret_cast<std::shared_ptr<app::App>*>(j_app_ptr);
        JStringAccessor local_realm_path(env, j_local_realm_path);
        auto session = app->sync_manager()->get_existing_session(local_realm_path);
        if (session) {
            session->log_out();
        }
    }
    CATCH_STD()
}
