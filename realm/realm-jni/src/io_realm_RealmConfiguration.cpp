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
#include <realm/string_data.hpp>

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
    realm::Realm::Config* realm_configuration = CONF(nativePointer);
    try {
        JStringAccessor path(env, jpath);
        realm::StringData cpath = realm::StringData(path);
        realm_configuration->path = cpath;
        realm_configuration->in_memory = false;
    }
    CATCH_STD()
}

JNIEXPORT jstring JNICALL Java_io_realm_RealmConfiguration_nativeGetPath(
    JNIEnv *env, jobject, jlong nativePointer)
{
    TR_ENTER_PTR(nativePointer);
    realm::Realm::Config* realm_configuration = CONF(nativePointer);
    try {
        if (!realm_configuration->in_memory) {
            return to_jstring(env, realm_configuration->path);
        }
    }
    CATCH_STD()
    return NULL;
}

JNIEXPORT void JNICALL Java_io_realm_RealmConfiguration_nativeSetEncryptionKey(
    JNIEnv *env, jobject, jlong nativePointer, jbyteArray jkey)
{
    TR_ENTER_PTR(nativePointer);
    realm::Realm::Config* realm_configuration = CONF(nativePointer);
    try {
        realm_configuration->encryption_key.clear();
        KeyBuffer kb(env, jkey);
        if (env->GetArrayLength(jkey) != io_realm_RealmConfiguration_KEY_LENGTH ) { // KeyBuffer does the check and sets the exception
            return;
        }
        const char* bytes = kb.data();
        for (std::size_t i = 0; i < io_realm_RealmConfiguration_KEY_LENGTH; ++i) {
            realm_configuration->encryption_key.push_back(bytes[i]);
        }
    }
    CATCH_STD()
}

JNIEXPORT jbyteArray JNICALL Java_io_realm_RealmConfiguration_nativeGetEncryptionKey(
    JNIEnv *env, jobject, jlong nativePointer)
{
    TR_ENTER_PTR(nativePointer);
    realm::Realm::Config* realm_configuration = CONF(nativePointer);
    try {
        if (realm_configuration->encryption_key.size() == 0) {
            return NULL;
        }
        char key[io_realm_RealmConfiguration_KEY_LENGTH];
        for (std::size_t i = 0; i < io_realm_RealmConfiguration_KEY_LENGTH; ++i) {
            key[i] = realm_configuration->encryption_key[i];
        }
        jbyteArray key_array = env->NewByteArray(io_realm_RealmConfiguration_KEY_LENGTH);
        env->SetByteArrayRegion(key_array, 0, io_realm_RealmConfiguration_KEY_LENGTH,
            reinterpret_cast<const jbyte*>(key));
        return key_array;
    }
    CATCH_STD()
    return NULL;
}

JNIEXPORT jlong JNICALL Java_io_realm_RealmConfiguration_nativeGetSchemaVersion(
    JNIEnv *env, jobject, jlong nativePointer)
{
    TR_ENTER_PTR(nativePointer);
    realm::Realm::Config* realm_configuration = CONF(nativePointer);
    try {
        // FIXME: check for overflow
        return static_cast<jlong>(realm_configuration->schema_version);
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_RealmConfiguration_nativeSetSchemaVersion(
    JNIEnv *env, jobject, jlong nativePointer, jlong jSchemaVersion)
{
    TR_ENTER_PTR(nativePointer);
    realm::Realm::Config* realm_configuration = CONF(nativePointer);
    try {
        if (jSchemaVersion < 0) {
            ThrowException(env, IllegalArgument, "Schema version cannot be negative.");
            return;
        }
        realm_configuration->schema_version = static_cast<uint64_t>(jSchemaVersion);
    }
    CATCH_STD()
}
