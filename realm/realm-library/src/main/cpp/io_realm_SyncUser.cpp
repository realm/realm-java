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

#include "io_realm_SyncUser.h"

#include "object-store/src/sync/sync_manager.hpp"
#include "object-store/src/sync/sync_session.hpp"

#include "util.hpp"

using namespace realm;

static const char* ERR_COULD_NOT_ALLOCATE_MEMORY = "Could not allocate memory to return all sessions path.";

JNIEXPORT jobjectArray JNICALL
Java_io_realm_SyncUser_nativeAllSessionsPath(JNIEnv *env, jclass, jstring sync_user_identity) {
    TR_ENTER()
    try {
        JStringAccessor user_identity(env, sync_user_identity);
        std::shared_ptr<SyncUser> user = SyncManager::shared().get_existing_logged_in_user(user_identity);

        if (!user) {
            return nullptr;
        }
        auto sessions = user.get()->all_sessions();
        if (!sessions.empty()) {
            size_t len = sessions.size();
            jobjectArray sessions_path = env->NewObjectArray(len, java_lang_string, 0);
            if (sessions_path == nullptr) {
                ThrowException(env, OutOfMemory, ERR_COULD_NOT_ALLOCATE_MEMORY);
                return nullptr;
            }
            for (size_t i = 0; i < len; ++i) {
                env->SetObjectArrayElement(sessions_path, i, to_jstring(env, sessions[i]->path().data()));
            }

            return sessions_path;
        }
    }
    CATCH_STD()
    return nullptr;
}
