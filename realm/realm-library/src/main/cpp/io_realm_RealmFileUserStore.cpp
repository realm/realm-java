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

#include <jni.h>
#include <jni_util/log.hpp>
#include "io_realm_RealmFileUserStore.h"
#include "sync/sync_manager.hpp"
#include "sync/sync_user.hpp"
#include "util.hpp"

using namespace realm;

static const char* ERR_COULD_NOT_ALLOCATE_MEMORY = "Could not allocate memory to return all users.";

static jstring to_user_string_or_null(JNIEnv* env, const std::shared_ptr<SyncUser>& user)
{
    if (user) {
        return to_jstring(env, user->refresh_token().data());
    }
    else {
        return nullptr;
    }
}

JNIEXPORT jstring JNICALL Java_io_realm_RealmFileUserStore_nativeGetCurrentUser(JNIEnv* env, jclass)
{
    TR_ENTER()
    try {
        const std::shared_ptr<SyncUser>& user = SyncManager::shared().get_current_user();
        return to_user_string_or_null(env, user);
    }
    CATCH_STD()
    return nullptr;
}

JNIEXPORT jstring JNICALL Java_io_realm_RealmFileUserStore_nativeGetUser(JNIEnv* env, jclass, jstring identity)
{
    TR_ENTER()
    try {
        JStringAccessor id(env, identity); // throws
        const std::shared_ptr<SyncUser>& user = SyncManager::shared().get_existing_logged_in_user(id);
        return to_user_string_or_null(env, user);
    }
    CATCH_STD()
    return nullptr;
}

JNIEXPORT void JNICALL Java_io_realm_RealmFileUserStore_nativeUpdateOrCreateUser(JNIEnv* env, jclass,
                                                                                 jstring identity, jstring json_token,
                                                                                 jstring url)
{
    TR_ENTER()
    try {
        JStringAccessor user_identity(env, identity);     // throws
        JStringAccessor user_json_token(env, json_token); // throws
        JStringAccessor auth_url(env, url);               // throws
        SyncManager::shared().get_user(user_identity, user_json_token, std::string(auth_url));
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_RealmFileUserStore_nativeLogoutUser(JNIEnv* env, jclass, jstring identity)
{
    TR_ENTER()
    try {
        JStringAccessor id(env, identity); // throws
        const std::shared_ptr<SyncUser>& user = SyncManager::shared().get_existing_logged_in_user(id);
        if (user) {
            user->log_out();
        }
    }
    CATCH_STD()
}

JNIEXPORT jobjectArray JNICALL Java_io_realm_RealmFileUserStore_nativeGetAllUsers(JNIEnv* env, jclass)
{
    TR_ENTER()
    std::vector<std::shared_ptr<SyncUser>> all_users = SyncManager::shared().all_logged_in_users();
    if (!all_users.empty()) {
        size_t len = all_users.size();
        jobjectArray users_token = env->NewObjectArray(len, java_lang_string, 0);
        if (users_token == nullptr) {
            ThrowException(env, OutOfMemory, ERR_COULD_NOT_ALLOCATE_MEMORY);
            return nullptr;
        }
        for (size_t i = 0; i < len; ++i) {
            env->SetObjectArrayElement(users_token, i, to_jstring(env, all_users[i]->refresh_token().data()));
        }

        return users_token;
    }
    return nullptr;
}
