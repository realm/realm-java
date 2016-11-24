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
#include "io_realm_ObjectStoreUserStore.h"
#include "sync/sync_manager.hpp"
#include "sync/sync_user.hpp"
#include "util.hpp"

using namespace realm;

static const std::shared_ptr<SyncUser>& currentUserOrThrow() { //throws
    std::vector<std::shared_ptr<SyncUser>> all_users = SyncManager::shared().all_users();
    if (all_users.size() > 1) {
        std::runtime_error("cannot be called if more that one valid, logged-in user exists.");//TODO user appropriate exception
    } else if (all_users.size() < 1) {
        std::runtime_error("no user logged-in yet.");//TODO user appropriate exception
    } else {
        return all_users.front();
    }
}

JNIEXPORT jstring JNICALL Java_io_realm_ObjectStoreUserStore_getCurrentUser
        (JNIEnv *env, jclass) {
    TR_ENTER()
    const std::shared_ptr<SyncUser>& user = currentUserOrThrow();
    if (user->state() == SyncUser::State::Active) {
        return env->NewStringUTF(user->refresh_token().c_str());
    } else {
        return NULL;
    }
}

/*
 * Class:     io_realm_ObjectStoreSyncManager
 * Method:    updateOrCreateUser
 * Signature: (Ljava/lang/String;Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_io_realm_ObjectStoreUserStore_updateOrCreateUser
        (JNIEnv *env, jclass, jstring identity, jstring jsonToken, jstring url) {
    TR_ENTER()
    try {
        JStringAccessor user_identity(env, identity); // throws
        JStringAccessor user_json_token(env, jsonToken); // throws
        JStringAccessor auth_url(env, url); // throws

        SyncManager::shared().get_user(user_identity, user_json_token, util::Optional<std::string>(auth_url));
    } CATCH_STD()
}

/*
 * Class:     io_realm_ObjectStoreSyncManager
 * Method:    logoutCurrentUser
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_io_realm_ObjectStoreUserStore_logoutCurrentUser
        (JNIEnv *, jclass) {
    TR_ENTER()
    const std::shared_ptr<SyncUser>& user = currentUserOrThrow();
    user->log_out();
}


/*
 * Class:     io_realm_ObjectStoreSyncManager
 * Method:    configureMetaDataSystem
 * Signature: (Ljava/lang/String;[B)V
 */
JNIEXPORT void JNICALL Java_io_realm_ObjectStoreUserStore_configureMetaDataSystem
        (JNIEnv *env, jclass, jstring baseFile, jbyteArray aesKey) {
    TR_ENTER()
    try {
        JStringAccessor base_file_path(env, baseFile); // throws
        SyncManager::shared().configure_file_system(base_file_path, SyncManager::MetadataMode::NoEncryption);
        //TODO use encryption mode with the provided key
        //     JniByteArray key_array(env, aesKey);
    } CATCH_STD()

}

/*
 * Class:     io_realm_ObjectStoreSyncManager
 * Method:    getAllUsers
 * Signature: ()[Ljava/lang/String;
 */
JNIEXPORT jobjectArray JNICALL Java_io_realm_ObjectStoreUserStore_getAllUsers
        (JNIEnv *env, jclass) {
    TR_ENTER()
    std::vector<std::shared_ptr<SyncUser>> all_users = SyncManager::shared().all_users();
    if (!all_users.empty()) {
        std::vector<jobject> valid_users;
        for (auto& user : all_users) {
            if ((*user).state() == SyncUser::State::Active) {
                valid_users.push_back(env->NewStringUTF((*user).refresh_token().c_str()));
            }
        }

        std::vector<jobject>::size_type size_valid_users = valid_users.size();
        jclass stringClass = env->FindClass("java/lang/String");
        jobjectArray users_token = env->NewObjectArray(size_valid_users, stringClass, 0);
        if (users_token == NULL) {
            ThrowException(env, OutOfMemory, "Could not allocate memory to return all users.");
            return NULL;
        }

        for (std::vector<jobject>::size_type i = 0; i != size_valid_users; ++i) {
            env->SetObjectArrayElement(users_token, i, valid_users[i]);
        }

        return users_token;
    }
    return NULL;
}

/*
 * Class:     io_realm_ObjectStoreSyncManager
 * Method:    reset_for_testing
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_io_realm_ObjectStoreUserStore_reset_1for_1testing
        (JNIEnv *, jclass) {
    TR_ENTER();
    SyncManager::shared().reset_for_testing();
}
