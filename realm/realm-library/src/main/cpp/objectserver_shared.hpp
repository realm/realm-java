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

#include "util.hpp"

// maintain a reference to the threads allocated dynamically, to prevent deallocation
// after Java_io_realm_internal_SharedGroup_nativeStartSession completes.
// To be released later, maybe on JNI_OnUnload
extern std::thread* sync_client_thread;
extern JNIEnv* sync_client_env;


// Wrapper class for realm::Session. This allows us to manage the C++ session and callback lifecycle correctly.
// TODO Use OS SyncSession instead
class JniSession {

public:
    JniSession() = delete;
    JniSession(JNIEnv* env, realm::sync::Client* sync_client, std::string local_realm_path, jobject java_session_obj)
    {
        // Get the coordinator for the given path, or null if there is none
        m_sync_session = new realm::sync::Session(*sync_client, local_realm_path);
            m_global_obj_ref = env->NewGlobalRef(java_session_obj);
            jobject global_obj_ref_tmp(m_global_obj_ref);
            auto sync_transact_callback = [local_realm_path](realm::VersionID, realm::VersionID) {
                auto coordinator = realm::_impl::RealmCoordinator::get_existing_coordinator(realm::StringData(local_realm_path));
                if (coordinator) {
                    coordinator->notify_others();
                }
            };
            auto error_handler = [&, global_obj_ref_tmp](int error_code, std::string message) {
                std::string log = num_to_string(error_code) + " " + message.c_str();
                log_message(sync_client_env, log_debug, log.c_str());
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
        sync_client_env->DeleteGlobalRef(m_global_obj_ref);
        delete m_sync_session;
    }

private:
    realm::sync::Session* m_sync_session;
    jobject m_global_obj_ref;
};

#endif // REALM_OBJECTSERVER_SHARED_HPP
