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
#include <object-store/src/sync/sync_session.hpp>
#include "util.hpp"
#include "jni_util/jni_utils.hpp"

using namespace realm;

// Wrapper class for SyncConfig. This is required as we need to keep track of both the Java session
// object as part of the configuration.
// NOTICE: It is an requirement that the Java `SyncSession` object is created before this and
// is only GC'ed after this object has been destroyed.
class JniConfigWrapper {

public:
    JniConfigWrapper(const JniConfigWrapper&) = delete;

    JniConfigWrapper& operator=(const JniConfigWrapper&) = delete;
    JniConfigWrapper(JniConfigWrapper&&) = delete;
    JniConfigWrapper& operator=(JniConfigWrapper&&) = delete;

    // Non-sync constructor
    JniConfigWrapper(JNIEnv*, Realm::Config* config) : m_config (config) {}

    // Sync constructor
    JniConfigWrapper(REALM_UNUSED JNIEnv* env,
                     REALM_UNUSED Realm::Config* config,
                     REALM_UNUSED jstring sync_realm_url,
                     REALM_UNUSED jstring sync_realm_auth_url,
                     REALM_UNUSED jstring sync_user_identity,
                     REALM_UNUSED jstring sync_refresh_token)
    {
#ifdef REALM_ENABLE_SYNC
        m_config = config;

        // error handler will be called form the sync client thread
        // we shouldn't capture jobjects (jclass, jmethodID) by reference or valie
        // since they're only valid for the caller thread
        auto error_handler = [&](std::shared_ptr<SyncSession> session, SyncError error) {
            realm::jni_util::Log::d("error_handler lambda invoked");

            JNIEnv *env = realm::jni_util::JniUtils::get_env(true);
            env->CallStaticVoidMethod(java_syncmanager,
                                      java_error_callback_method,
                                      error.error_code.value(),
                                      env->NewStringUTF(error.message.c_str()),
                                      env->NewStringUTF(session.get()->path().c_str()));
        };

        // path on disk of the Realm file.
        // the sync configuration object.
        // the session which should be bound.
        auto bind_handler = [&](const std::string& path, const SyncConfig& syncConfig, std::shared_ptr<SyncSession> session) {
            realm::jni_util::Log::d("Callback to Java requesting token for path");

            JNIEnv *env = realm::jni_util::JniUtils::get_env(true);
            jstring access_token_string = (jstring) env->CallStaticObjectMethod(java_syncmanager,
                                java_bind_session_method,
                                to_jstring(env, path.c_str()));
            if (access_token_string) {
                // reusing cached valid token
                JStringAccessor access_token(env, access_token_string);
                session->refresh_access_token(access_token, realm::util::Optional<std::string>(syncConfig.realm_url));
            }
        };

        // Get logged in user
        JStringAccessor user_identity(env, sync_user_identity);
        JStringAccessor realm_url(env, sync_realm_url);
        std::shared_ptr<SyncUser> user = SyncManager::shared().get_existing_logged_in_user(user_identity);
        if (!user)
        {
            JStringAccessor realm_auth_url(env, sync_realm_auth_url);
            JStringAccessor refresh_token(env, sync_refresh_token);
            user = SyncManager::shared().get_user(user_identity, refresh_token, realm::util::Optional<std::string>(realm_auth_url));
        }
        config->sync_config = std::make_shared<SyncConfig>(user,
                                                           realm_url,
                                                           SyncSessionStopPolicy::Immediately,
                                                           bind_handler,
                                                           error_handler);
#endif
    }

    inline Realm::Config* get_config() {
        return m_config;
    }


    // Call this just before destroying the object to release JNI resources.
    inline void close(JNIEnv*)
    {
    }

    ~JniConfigWrapper()
    {
    }

private:
    Realm::Config* m_config;
};

#endif // REALM_OBJECTSERVER_SHARED_HPP
