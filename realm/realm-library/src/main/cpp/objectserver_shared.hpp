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
#ifndef REALM_OBJECTSERVER_SHARED_HPP
#define REALM_OBJECTSERVER_SHARED_HPP

#include <jni.h>
#include <string>
#include <thread>
#include <memory>

#include <realm/sync/history.hpp>
#include <realm/sync/client.hpp>
#include <realm/sync/protocol.hpp>
#include <realm/util/logger.hpp>

#include <impl/realm_coordinator.hpp>
#include <sync/sync_manager.hpp>

#include "util.hpp"
#include "jni_util/jni_utils.hpp"
#include "jni_util/java_global_weak_ref.hpp"
#include "jni_util/java_method.hpp"


// Wrapper class for realm::Session. This allows us to manage the C++ session and callback lifecycle correctly.
// TODO Use OS SyncSession instead
class JniSession {

public:
    JniSession(const JniSession&) = delete;
    JniSession& operator=(const JniSession&) = delete;
    JniSession(JniSession&&) = delete;
    JniSession& operator=(JniSession&&) = delete;

    JniSession(JNIEnv* env, std::string local_realm_path, jobject java_session_obj)
    : m_java_session_ref(std::make_shared<realm::jni_util::JavaGlobalWeakRef>(env, java_session_obj))
    {
        extern std::unique_ptr<realm::sync::Client> sync_client;
        // Get the coordinator for the given path, or null if there is none
        m_sync_session = new realm::sync::Session(*sync_client, local_realm_path);
        // error_handler could be called after JniSession destructed. So we need to pass a weak ref to lambda to avoid
        // the corrupted pointer.
        std::weak_ptr<realm::jni_util::JavaGlobalWeakRef> weak_session_ref(m_java_session_ref);
        auto sync_transact_callback = [local_realm_path](realm::VersionID, realm::VersionID) {
            auto coordinator = realm::_impl::RealmCoordinator::get_existing_coordinator(
                    realm::StringData(local_realm_path));
            if (coordinator) {
                coordinator->wake_up_notifier_worker();
            }
        };
        auto error_handler = [weak_session_ref](std::error_code error_code, bool is_fatal, const std::string message) {
            if (error_code.category() != realm::sync::protocol_error_category() ||
                    error_code.category() != realm::sync::client_error_category()) {
                // FIXME: Consider below when moving to the OS sync manager.
                // Ignore this error since it may cause exceptions in java ErrorCode.fromInt(). Throwing exception there
                // will trigger "called with pending exception" later since the thread is created by java, and the
                // endless loop is in native code. The java exception will never be thrown because of the endless loop
                // will never quit to java land.
                realm::jni_util::Log::e("Unhandled sync client error code %1, %2. is_fatal: %3.",
                                        error_code.value(), error_code.message(), is_fatal);
                return;
            }

            auto session_ref = weak_session_ref.lock();
            if (session_ref) {
                session_ref.get()->call_with_local_ref([&](JNIEnv* local_env, jobject obj) {
                    static realm::jni_util::JavaMethod notify_error_handler(
                            local_env, obj, "notifySessionError", "(ILjava/lang/String;)V");
                    local_env->CallVoidMethod(
                            obj, notify_error_handler, error_code.value(), local_env->NewStringUTF(message.c_str()));
                });
            }
        };
        m_sync_session->set_sync_transact_callback(sync_transact_callback);
        m_sync_session->set_error_handler(std::move(error_handler));
    }

    inline realm::sync::Session* get_session() const noexcept
    {
        return m_sync_session;
    }

    ~JniSession()
    {
        delete m_sync_session;
    }

private:
    realm::sync::Session* m_sync_session;
    std::shared_ptr<realm::jni_util::JavaGlobalWeakRef> m_java_session_ref;
};

#endif // REALM_OBJECTSERVER_SHARED_HPP
