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
#include <thread>

#include <realm/sync/history.hpp>
#include <realm/sync/client.hpp>
#include <realm/util/logger.hpp>

#include "util.hpp"

// maintain a reference to the threads allocated dynamically, to prevent deallocation
// after Java_io_realm_internal_SharedGroup_nativeStartSession completes.
// To be released later, maybe on JNI_OnUnload
extern std::thread* sync_client_thread;
extern JNIEnv* sync_client_env;


// Wrapper class for realm::Session. This allows us to manage the C++ session and callback lifecycle correctly.
class JNISession {

public:
    JNISession() = delete;
    JNISession(realm::sync::Client* sync_client, std::string local_realm_path, jobject java_session_obj, JNIEnv* env)
    {
            sync_session = new realm::sync::Session(*sync_client, local_realm_path);
            global_obj_ref = env->NewGlobalRef(java_session_obj);
            jobject global_obj_ref_tmp(global_obj_ref);
            auto sync_transact_callback = [local_realm_path](realm::sync::Session::version_type) {
                // FIXME Realm changed. Does this come from free with OS notifications?
                jstring java_local_path = sync_client_env->NewStringUTF(local_realm_path.c_str());
                sync_client_env->CallStaticVoidMethod(
                    sync_manager,
                    sync_manager_notify_handler,
                    java_local_path
                );
                sync_client_env->DeleteLocalRef(java_local_path);
            };
            auto error_handler = [&, global_obj_ref_tmp](int error_code, std::string message) {
                jstring error_message = sync_client_env->NewStringUTF(message.c_str());
                sync_client_env->CallVoidMethod(
                    global_obj_ref_tmp,
                    session_error_handler,
                    error_code,
                    error_message
                );
                sync_client_env->DeleteLocalRef(error_message);
            };
            sync_session->set_sync_transact_callback(sync_transact_callback);
            sync_session->set_error_handler(std::move(error_handler));
    }

    inline realm::sync::Session* get_session() const noexcept
    {
        return sync_session;
    }

    ~JNISession()
    {
        sync_client_env->DeleteGlobalRef(global_obj_ref);
        delete sync_session;
    }

private:
    realm::sync::Session* sync_session;
    jobject global_obj_ref;
};

#endif // REALM_OBJECTSERVER_SHARED_HPP
