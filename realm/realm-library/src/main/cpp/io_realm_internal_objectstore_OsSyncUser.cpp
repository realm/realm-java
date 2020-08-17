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

#include "java_class_global_def.hpp"
#include "util.hpp"
#include "jni_util/java_class.hpp"
#include "java_network_transport.hpp"

#include <sync/sync_user.hpp>
#include <jni_util/bson_util.hpp>
#include <object-store/src/util/bson/bson.hpp>

using namespace realm;
using namespace realm::_impl;
using namespace realm::jni_util;
using namespace realm::util;

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

JNIEXPORT jstring JNICALL Java_io_realm_internal_objectstore_OsSyncUser_nativeGetBirthday(JNIEnv* env, jclass, jlong j_native_ptr)
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
        jobjectArray arr = env->NewObjectArray(ids.size()*2, JavaClassGlobalDef::java_lang_string(), 0);
        if (arr == NULL) {
            ThrowException(env, OutOfMemory, "Could not allocate memory to return identites");
            return NULL;
        }
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

JNIEXPORT jstring JNICALL Java_io_realm_internal_objectstore_OsSyncUser_nativeGetLocalIdentity(JNIEnv* env, jclass, jlong j_native_ptr)
{
    try {
        auto user = *reinterpret_cast<std::shared_ptr<SyncUser>*>(j_native_ptr);
        return to_jstring(env, user->local_identity());
    }
    CATCH_STD();
    return nullptr;
}


JNIEXPORT jbyte JNICALL Java_io_realm_internal_objectstore_OsSyncUser_nativeGetState(JNIEnv* env, jclass, jlong j_native_ptr)
{
    try {
        auto user = *reinterpret_cast<std::shared_ptr<SyncUser>*>(j_native_ptr);
        switch(user->state()) {
            case SyncUser::State::LoggedOut: return static_cast<jbyte>(io_realm_internal_objectstore_OsSyncUser_STATE_LOGGED_OUT);
            case SyncUser::State::LoggedIn:  return static_cast<jbyte>(io_realm_internal_objectstore_OsSyncUser_STATE_LOGGED_IN);
            case SyncUser::State::Removed: return static_cast<jbyte>(io_realm_internal_objectstore_OsSyncUser_STATE_REMOVED);
            default:
                throw std::logic_error(util::format("Unknown state: %1", static_cast<size_t>(user->state())));
        }
    }
    CATCH_STD();
    return static_cast<jbyte>(-1);
}

JNIEXPORT void JNICALL Java_io_realm_internal_objectstore_OsSyncUser_nativeSetState(JNIEnv* env, jclass, jlong j_native_ptr, jbyte j_state)
{
    try {
        auto user = *reinterpret_cast<std::shared_ptr<SyncUser>*>(j_native_ptr);
        switch(j_state) {
            case io_realm_internal_objectstore_OsSyncUser_STATE_LOGGED_OUT:
                user->set_state(SyncUser::State::LoggedOut);
                break;
            case io_realm_internal_objectstore_OsSyncUser_STATE_LOGGED_IN:
                user->set_state(SyncUser::State::LoggedIn);
                break;
            case io_realm_internal_objectstore_OsSyncUser_STATE_REMOVED:
                user->set_state(SyncUser::State::Removed);
                break;
            default:
                throw std::logic_error(util::format("Unknown state: %1", j_state));
        }
    }
    CATCH_STD();
}

JNIEXPORT jstring JNICALL Java_io_realm_internal_objectstore_OsSyncUser_nativeGetProviderType(JNIEnv* env, jclass, jlong j_native_ptr)
{
    try {
        auto user = *reinterpret_cast<std::shared_ptr<SyncUser>*>(j_native_ptr);
        return to_jstring(env, user->provider_type());
    }
    CATCH_STD();
    return nullptr;
}

JNIEXPORT jstring JNICALL Java_io_realm_internal_objectstore_OsSyncUser_nativeGetDeviceId(JNIEnv* env, jclass, jlong j_native_ptr)
{
    try {
        auto user = *reinterpret_cast<std::shared_ptr<SyncUser>*>(j_native_ptr);
        std::string device_id = user->device_id();
        return to_jstring(env, device_id);
    }
    CATCH_STD();
    return nullptr;
}

JNIEXPORT jstring JNICALL Java_io_realm_internal_objectstore_OsSyncUser_nativeCustomData(JNIEnv* env, jclass, jlong j_native_ptr) {
    try {
        auto user = *reinterpret_cast<std::shared_ptr<SyncUser>*>(j_native_ptr);
        const util::Optional<bson::BsonDocument> custom_data(user->custom_data());
        if (custom_data) {
            return JniBsonProtocol::bson_to_jstring(env, *custom_data);
        } else {
            return JniBsonProtocol::bson_to_jstring(env, BsonDocument());
        }
    }
    CATCH_STD()
    return JniBsonProtocol::bson_to_jstring(env, BsonDocument());
}


JNIEXPORT void JNICALL Java_io_realm_internal_objectstore_OsSyncUser_nativeRefreshCustomData
        (JNIEnv* env, jclass, jlong j_native_ptr, jobject j_callback) {
    try {
        auto user = *reinterpret_cast<std::shared_ptr<SyncUser>*>(j_native_ptr);
        std::function<void(util::Optional<app::AppError>)> callback = JavaNetworkTransport::create_void_callback(env, j_callback);
        user->refresh_custom_data(callback);
    }
    CATCH_STD()
}
