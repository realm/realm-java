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

#include <sync/sync_user.hpp>

using namespace realm;

static void finalize_user(jlong ptr)
{
    delete reinterpret_cast<std::shared_ptr<SyncUser>*>(ptr);
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_objectstore_OsSyncUser_nativeGetFinalizerMethodPtr(JNIEnv*, jclass)
{
    return reinterpret_cast<jlong>(&finalize_user);
}
//
//private static native String nativeGetName(long nativePtr);
//private static native String nativeGetEmail(long nativePtr);
//private static native String nativeGetPictureUrl(long nativePtr);
//private static native String nativeGetFirstName(long nativePtr);
//private static native String nativeGetLastName(long nativePtr);
//private static native String nativeGetGender(long nativePtr);
//private static native String nativeGetBirthDay(long nativePtr);
//private static native String nativeGetMinAge(long nativePtr);
//private static native String nativeGetMaxAge(long nativePtr);
//
//private static native String nativeGetServerUrl(long nativePtr);
//private static native String nativeGetLocalIdentity(long nativePtr);
//private static native String nativeGetAccessToken(long nativePtr);
//private static native String nativeGetRefreshToken(long nativePtr);
//private static native String nativeGetRefreshToken(long nativePtr);
//private static native String[] nativeGetIdentities(long nativePtr); // Returns pairs of {id, provider}

JNIEXPORT jstring JNICALL Java_io_realm_internal_objectstore_OsSyncUser_nativeGetName(JNIEnv* env, jclass, jlong j_native_ptr)
{
    try {
        auto user = *reinterpret_cast<std::shared_ptr<SyncUser>*>(j_native_ptr);
        return to_jstring(env, user->user_profile().name);
    }
    CATCH_STD();
    return nullptr;
}


