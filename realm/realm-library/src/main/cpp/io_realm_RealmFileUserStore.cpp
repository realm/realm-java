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

static const char* ERR_MULTIPLE_LOGGED_IN_USERS = "Cannot be called if more that one valid, logged in user exists.";
static const char* ERR_NO_LOGGED_IN_USER = "No user logged in yet.";
static const char* ERR_COULD_NOT_ALLOCATE_MEMORY = "Could not allocate memory to return all users.";

static const std::shared_ptr<SyncUser>& currentUserOrThrow();

JNIEXPORT jstring JNICALL
Java_io_realm_RealmFileUserStore_nativeGetCurrentUser (JNIEnv *env, jclass)
{
    TR_ENTER()
    try {
        const std::shared_ptr<SyncUser> &user = currentUserOrThrow();
        if (user->state() == SyncUser::State::Active) {
            return to_jstring(env, user->refresh_token().data());
        } else {
            return nullptr;
        }
    } CATCH_STD()
    return nullptr;
}

JNIEXPORT void JNICALL
Java_io_realm_RealmFileUserStore_nativeUpdateOrCreateUser (JNIEnv *env, jclass, jstring identity, jstring jsonToken, jstring url)
{
    TR_ENTER()
    try {
        JStringAccessor user_identity(env, identity); // throws
        JStringAccessor user_json_token(env, jsonToken); // throws
        JStringAccessor auth_url(env, url); // throws

        SyncManager::shared().get_user(user_identity, user_json_token, std::string(auth_url));
    } CATCH_STD()
}

JNIEXPORT void JNICALL
Java_io_realm_RealmFileUserStore_nativeLogoutCurrentUser (JNIEnv *env, jclass)
{
    TR_ENTER()
    try {
        const std::shared_ptr<SyncUser>& user = currentUserOrThrow();
        user->log_out();
    } CATCH_STD()
}


JNIEXPORT void JNICALL
Java_io_realm_RealmFileUserStore_nativeConfigureMetaDataSystem (JNIEnv *env, jclass, jstring baseFile)
{
    TR_ENTER()
    try {
        JStringAccessor base_file_path(env, baseFile); // throws
        SyncManager::shared().configure_file_system(base_file_path, SyncManager::MetadataMode::NoEncryption);
    } CATCH_STD()
}

JNIEXPORT jobjectArray JNICALL
Java_io_realm_RealmFileUserStore_nativeGetAllUsers (JNIEnv *env, jclass)
{
    TR_ENTER()
    std::vector<std::shared_ptr<SyncUser>> all_users = SyncManager::shared().all_users();
    if (!all_users.empty()) {
        std::vector<std::shared_ptr<SyncUser>> valid_users;
        jsize array_length = std::count_if(all_users.begin(),all_users.end(),
                        [&](const std::shared_ptr<SyncUser>& user) {
                            if (user->state() == SyncUser::State::Active) {
                                valid_users.emplace_back(std::move(user));
                                return true;
                            }
                            return false;
                        });

        jobjectArray users_token = env->NewObjectArray(array_length, java_lang_string, 0);
        if (users_token == NULL) {
            ThrowException(env, OutOfMemory, ERR_COULD_NOT_ALLOCATE_MEMORY);
            return nullptr;
        }

        for (auto user : valid_users) {
            env->SetObjectArrayElement(users_token, --array_length, to_jstring(env, user->refresh_token().data()));
        }

        return users_token;
    }
    return nullptr;
}

JNIEXPORT void JNICALL
Java_io_realm_RealmFileUserStore_nativeResetForTesting (JNIEnv *, jclass)
{
    TR_ENTER();
    SyncManager::shared().reset_for_testing();
}

static const std::shared_ptr<SyncUser>& currentUserOrThrow() //throws
{
    std::vector<std::shared_ptr<SyncUser>> all_users = SyncManager::shared().all_users();
    if (all_users.size() > 1) {
        throw std::runtime_error(ERR_MULTIPLE_LOGGED_IN_USERS);
    } else if (all_users.size() < 1) {
        throw std::runtime_error(ERR_NO_LOGGED_IN_USER);
    } else {
        return all_users.front();
    }
}
