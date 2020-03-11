/*
 * Copyright 2020 Realm Inc.
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

#include "io_realm_internal_objectstore_OsSyncUser.h"

#include "util.hpp"
#include "jni_util/java_class.hpp"

#include <sync/sync_user.hpp>

using namespace realm;
using namespace realm::jni_util;

static void finalize_user(jlong ptr)
{
    delete reinterpret_cast<std::shared_ptr<SyncUser>*>(ptr);
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_objectstore_OsSyncUser_nativeGetFinalizerMethodPtr(JNIEnv*, jclass)
{
    return reinterpret_cast<jlong>(&finalize_user);
}

JNIEXPORT jstring JNICALL Java_io_realm_internal_objectstore_OsSyncUser_nativeGetName(JNIEnv* env, jclass, jlong j_native_ptr)
{
    try {
        auto user = *reinterpret_cast<std::shared_ptr<SyncUser>*>(j_native_ptr);
        return to_jstring(env, user->user_profile().name);
    }
    CATCH_STD();
    return nullptr;
}

JNIEXPORT jstring JNICALL Java_io_realm_internal_objectstore_OsSyncUser_nativeGetEmail(JNIEnv* env, jclass, jlong j_native_ptr)
{
    try {
        auto user = *reinterpret_cast<std::shared_ptr<SyncUser>*>(j_native_ptr);
        return to_jstring(env, user->user_profile().email);
    }
    CATCH_STD();
    return nullptr;
}

JNIEXPORT jstring JNICALL Java_io_realm_internal_objectstore_OsSyncUser_nativeGetPictureUrl(JNIEnv* env, jclass, jlong j_native_ptr)
{
    try {
        auto user = *reinterpret_cast<std::shared_ptr<SyncUser>*>(j_native_ptr);
        return to_jstring(env, user->user_profile().picture_url);
    }
    CATCH_STD();
    return nullptr;
}

JNIEXPORT jstring JNICALL Java_io_realm_internal_objectstore_OsSyncUser_nativeGetFirstName(JNIEnv* env, jclass, jlong j_native_ptr)
{
    try {
        auto user = *reinterpret_cast<std::shared_ptr<SyncUser>*>(j_native_ptr);
        return to_jstring(env, user->user_profile().first_name);
    }
    CATCH_STD();
    return nullptr;
}

JNIEXPORT jstring JNICALL Java_io_realm_internal_objectstore_OsSyncUser_nativeGetLastName(JNIEnv* env, jclass, jlong j_native_ptr)
{
    try {
        auto user = *reinterpret_cast<std::shared_ptr<SyncUser>*>(j_native_ptr);
        return to_jstring(env, user->user_profile().last_name);
    }
    CATCH_STD();
    return nullptr;
}

JNIEXPORT jstring JNICALL Java_io_realm_internal_objectstore_OsSyncUser_nativeGetGender(JNIEnv* env, jclass, jlong j_native_ptr)
{
    try {
        auto user = *reinterpret_cast<std::shared_ptr<SyncUser>*>(j_native_ptr);
        return to_jstring(env, user->user_profile().gender);
    }
    CATCH_STD();
    return nullptr;
}

JNIEXPORT jstring JNICALL Java_io_realm_internal_objectstore_OsSyncUser_nativeGetBirthDay(JNIEnv* env, jclass, jlong j_native_ptr)
{
    try {
        auto user = *reinterpret_cast<std::shared_ptr<SyncUser>*>(j_native_ptr);
        return to_jstring(env, user->user_profile().birthday);
    }
    CATCH_STD();
    return nullptr;
}

JNIEXPORT jstring JNICALL Java_io_realm_internal_objectstore_OsSyncUser_nativeGetMinAge(JNIEnv* env, jclass, jlong j_native_ptr)
{
    try {
        auto user = *reinterpret_cast<std::shared_ptr<SyncUser>*>(j_native_ptr);
        return to_jstring(env, user->user_profile().min_age);
    }
    CATCH_STD();
    return nullptr;
}

JNIEXPORT jstring JNICALL Java_io_realm_internal_objectstore_OsSyncUser_nativeGetMaxAge(JNIEnv* env, jclass, jlong j_native_ptr)
{
    try {
        auto user = *reinterpret_cast<std::shared_ptr<SyncUser>*>(j_native_ptr);
        return to_jstring(env, user->user_profile().max_age);
    }
    CATCH_STD();
    return nullptr;
}

JNIEXPORT jstring JNICALL Java_io_realm_internal_objectstore_OsSyncUser_nativeGetAccessToken(JNIEnv* env, jclass, jlong j_native_ptr)
{
    try {
        auto user = *reinterpret_cast<std::shared_ptr<SyncUser>*>(j_native_ptr);
        std::string token = user->access_token();
        return to_jstring(env, token);
    }
    CATCH_STD();
    return nullptr;
}

JNIEXPORT jstring JNICALL Java_io_realm_internal_objectstore_OsSyncUser_nativeGetRefreshToken(JNIEnv* env, jclass, jlong j_native_ptr)
{
    try {
        auto user = *reinterpret_cast<std::shared_ptr<SyncUser>*>(j_native_ptr);
        std::string token = user->refresh_token();
        return to_jstring(env, token);
    }
    CATCH_STD();
    return nullptr;
}

JNIEXPORT jobjectArray JNICALL Java_io_realm_internal_objectstore_OsSyncUser_nativeGetIdentities(JNIEnv* env, jclass, jlong j_native_ptr)
{
    try {
        auto user = *reinterpret_cast<std::shared_ptr<SyncUser>*>(j_native_ptr);
        std::vector<SyncUserIdentity> ids = user->identities();
        static JavaClass stringClass(env, "java/lang/String");
        jobjectArray arr = env->NewObjectArray(ids.size()*2, stringClass, NULL);
        int j = 0;
        for(size_t i = 0; i < ids.size(); ++i) {
            SyncUserIdentity id = ids[i];
            env->SetObjectArrayElement( arr, j, to_jstring(env, id.id));
            env->SetObjectArrayElement( arr, j+1, to_jstring(env, id.provider_type));
            j = j+2;
        }
        return arr;
    }
    CATCH_STD();
    return nullptr;
}

JNIEXPORT jstring JNICALL Java_io_realm_internal_objectstore_OsSyncUser_nativeGetIdentity(JNIEnv* env, jclass, jlong j_native_ptr)
{
    try {
        auto user = *reinterpret_cast<std::shared_ptr<SyncUser>*>(j_native_ptr);
        return to_jstring(env, user->identity());
    }
    CATCH_STD();
    return nullptr;
}


