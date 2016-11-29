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

#include <realm/sync/history.hpp>
#include <realm/sync/client.hpp>
#include <realm/util/logger.hpp>
#include <object-store/src/impl/realm_coordinator.hpp>
#include <object-store/src/sync/sync_manager.hpp>
#include <object-store/src/sync/sync_user.hpp>
#include <object-store/src/sync/sync_config.hpp>

#include "util.hpp"

using namespace realm;

// Wrapper class for realm::Session. This allows us to manage the C++ session and callback lifecycle correctly.
class JniSession {

public:
    JniSession(const JniSession&) = delete;
    JniSession& operator=(const JniSession&) = delete;
    JniSession(JniSession&&) = delete;
    JniSession& operator=(JniSession&&) = delete;

    JniSession(JNIEnv* env, std::string local_realm_path, std::shared_ptr<SyncUser> user, jobject java_session_obj)
    {
        m_java_session_ref = env->NewGlobalRef(java_session_obj);
        jobject global_obj_ref_tmp(m_java_session_ref);

        auto error_handler = [&, global_obj_ref_tmp](int error_code, std::string message, realm::SyncSessionError) {
            JNIEnv *local_env;
            g_vm->AttachCurrentThread(&local_env, nullptr);
            jclass java_session_class = local_env->GetObjectClass(global_obj_ref_tmp);
            jmethodID notify_error_handler = local_env->GetMethodID(java_session_class,
                                                                    "notifySessionError", "(ILjava/lang/String;)V");
            local_env->CallVoidMethod(global_obj_ref_tmp,
                                      notify_error_handler, error_code, env->NewStringUTF(message.c_str()));
        };

        auto bind_handler = [](const std::string&, const SyncConfig&, std::shared_ptr<SyncSession>) {
            // Callback to Java requesting token


            // Do something
        };

        SyncConfig config = SyncConfig(
                user,
                std::string(),
                SyncSessionStopPolicy::Immediately,
                bind_handler,
                error_handler
        );

        m_sync_session = SyncManager::shared().get_session(local_realm_path, config);
//
//        extern std::unique_ptr<realm::sync::Client> sync_client;
//        // Get the coordinator for the given path, or null if there is none
//        m_sync_session = new realm::sync::Session(*sync_client, local_realm_path);
//        auto sync_transact_callback = [local_realm_path](realm::VersionID, realm::VersionID) {
//            auto coordinator = realm::_impl::RealmCoordinator::get_existing_coordinator(
//                    realm::StringData(local_realm_path));
//            if (coordinator) {
//                coordinator->wake_up_notifier_worker();
//            }
//        };
//        m_sync_session->set_sync_transact_callback(sync_transact_callback);
//        m_sync_session->set_error_handler(std::move(error_handler));
    }

    inline std::shared_ptr<SyncSession> get_session() const noexcept
    {
        return m_sync_session;
    }

    // Call this just before destroying the object to release JNI resources.
    inline void close(JNIEnv* env)
    {
        env->DeleteGlobalRef(m_java_session_ref);
    }

    ~JniSession()
    {
    }

private:
    std::shared_ptr<SyncSession> m_sync_session;
    jobject m_java_session_ref;
};

#endif // REALM_OBJECTSERVER_SHARED_HPP
