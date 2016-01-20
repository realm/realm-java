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

#include "util.hpp"

#include "io_realm_RealmConfiguration.h"
#include "shared_realm.hpp"

JNIEXPORT jlong JNICALL Java_io_realm_RealmConfiguration_createConfigurationPointer(
    JNIEnv *env, jobject)
{
    TR_ENTER()
    try {
        realm::Realm::Config* config = new realm::Realm::Config;
        return reinterpret_cast<jlong>(config);
    }
    CATCH_STD()
    return 0;
}

JNIEXPORT void JNICALL Java_io_realm_RealmConfiguration_nativeSetPath(
    JNIEnv *env, jobject, jlong nativePointer, jstring jpath)
{
    TR_ENTER_PTR(nativePointer);
    realm::Realm::RealmConfiguration* realm_configuration = CONF(nativePointer);
    try {
        JStringAccessor path(env, jpath);
        realm_configuration->path = path;
    }
    CATCH_STD()
}

JNIEXPORT jstring JNICALL Java_io_realm_RealmConfiguration_nativeGetPath(
    JNIEnv *env, jobject, jlong nativePointer)
{
    TR_ENTER_PTR(nativePointer);
    realm::Realm::RealmConfiguration* realm_configuration = CONF(nativePointer);
    try {
        return to_jstring(env, realm_configuration->path);
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_RealmConfiguration_nativeSetEncryptionKey(
    JNIEnv *env, jobject, jlong nativePointer, jbyteArray jkey)
{
    TR_ENTER_PTR(nativePointer);
    realm::Realm::RealmConfiguration* realm_configuration = CONF(nativePointer);
    // FIXME: convert jkey to vector<char>
}

JNIEXPORT jbyteArray JNICALL Java_io_realm_RealmConfiguration_nativeGetEncryptionKey(
    JNIEnv *env, jobject, jlong nativePointer)
{
    TR_ENTER_PTR(nativePointer);
    realm::Realm::RealmConfiguration* realm_configuration = CONF(nativePointer);
    // convert vector<char> to jbyteArray
}

JNIEXPORT jlong JNICALL Java_io_realm_RealmConfiguration_nativeGetSchemaVersion(
    JNIEnv *env, jobject, jlong nativePointer)
{
    TR_ENTER_PTR(nativePointer);
    realm::Realm::RealmConfiguration* realm_configuration = CONF(nativePointer);
    try {
        // FIXME: check for overflow
        return schema_version = static_cast<jlong>(realm_configuration->schema_version;
    }
    CATCH_ALL()
}

JNIEXPORT void JNICALL Java_io_realm_RealmConfiguration_nativeSetSchemaVersion(
    JNIEnv *env, jobject, jlong nativePointer, jlong jSchemaVersion)
{
    TR_ENTER_PTR(nativePointer);
    realm::Realm::RealmConfiguration* realm_configuration = CONF(nativePointer);
    try {
        if (schema_version < 0) {
            ThrowException(env, IllegalArgument, "Schema version cannot be negative.");
            return;
        }
        realm_configuration->schema_version = static_cast<uint64_t>(jSchemaVersion);
    }
}
