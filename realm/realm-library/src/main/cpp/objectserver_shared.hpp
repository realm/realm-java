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
                     REALM_UNUSED jstring sync_user_identity)
    {
#ifdef REALM_ENABLE_SYNC
        m_config = config;
        jclass java_syncmanager = GetClass(env, "io/realm/SyncManager");
        jmethodID java_error_callback = env->GetStaticMethodID(java_syncmanager, "notifyErrorHandler", "(ILjava/lang/String;Ljava/lang/String;)V");

        auto error_handler = [&, java_error_callback](std::shared_ptr<SyncSession> session, SyncError error) {
            JNIEnv *env = realm::jni_util::JniUtils::get_env(true);
            env->CallVoidMethod(java_syncmanager,
                                      java_error_callback,
                                      error.error_code,
                                      env->NewStringUTF(error.message.c_str()),
                                      env->NewStringUTF(session.get()->path().c_str()));
        };

        auto bind_handler = [](const std::string&, const SyncConfig&, std::shared_ptr<SyncSession>) {
            // Callback to Java requesting token
            // FIXME
        };

        // Get logged in user
        JStringAccessor user_identity(env, sync_user_identity);
        JStringAccessor realm_url(env, sync_realm_url);
        std::shared_ptr<SyncUser> user = SyncManager::shared().get_existing_logged_in_user(user_identity);
        if (!user)
        {
//            ThrowException(env, IllegalArgument, "User wasn't logged in: " + std::string(user_identity));
//            return realm::not_found;
            // FIXME work-around until user migration complete
            user = SyncManager::shared().get_user(user_identity, "foo", realm::util::Optional<std::string>("http://auth.realm.io/auth"));
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
