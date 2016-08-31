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

#include <object-store/src/schema.hpp>
#include <object-store/src/object_schema.hpp>
#include <object-store/src/property.hpp>

#include "io_realm_RealmSchema.h"
#include "util.hpp"

using namespace realm;

JNIEXPORT jlong JNICALL
Java_io_realm_RealmSchema_nativeCreateSchema(JNIEnv *env, jclass)
{
    TR_ENTER()
    try {
        auto *schema = new Schema();
        TR("schema = %p", VOID_PTR(schema));
        return reinterpret_cast<jlong>(schema);
    }
    CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL
Java_io_realm_RealmSchema_nativeCreateSchemaFromArray(JNIEnv *env, jclass, jlongArray realmObjectSchemaPtrs_) {
    TR_ENTER();
    try {
        JniLongArray realmObjectSchemaPtr(env, realmObjectSchemaPtrs_);
        std::vector<ObjectSchema> object_schemas;
        for (jint i = 0; i < realmObjectSchemaPtr.len(); ++i) {
            auto object_schema = *reinterpret_cast<ObjectSchema *>(realmObjectSchemaPtr[i]);
            object_schemas.push_back(std::move(object_schema));
        }
        auto* schema = new Schema(object_schemas);
        return reinterpret_cast<jlong>(schema);
    }
    CATCH_STD();
    return 0;
}

JNIEXPORT void JNICALL
Java_io_realm_RealmSchema_nativeClose(JNIEnv *env, jclass, jlong native_ptr)
{
    TR_ENTER_PTR(native_ptr)
    try {
        auto *schema = reinterpret_cast<Schema *>(native_ptr);
        delete schema;
    }
    CATCH_STD();
}

JNIEXPORT jlong JNICALL
Java_io_realm_RealmSchema_nativeSize(JNIEnv *env, jclass, jlong native_ptr) {
    TR_ENTER_PTR(native_ptr)
    try {
        auto *schema = reinterpret_cast<Schema *>(native_ptr);
        return static_cast<jlong>(schema->size());
    }
    CATCH_STD();
    return 0;
}

JNIEXPORT jboolean JNICALL
Java_io_realm_RealmSchema_nativeHasObjectSchemaByName(JNIEnv *env, jclass, jlong native_schema_ptr, jstring j_name)
{
    TR_ENTER_PTR(native_schema_ptr)
    auto *schema = reinterpret_cast<Schema*>(native_schema_ptr);
    try {
        JStringAccessor name(env, j_name);
        auto object_schema = schema->find(name);
        return static_cast<jboolean>(object_schema != schema->end());
    }
    CATCH_STD()
    return static_cast<jboolean>(false);
}

JNIEXPORT jlong JNICALL
Java_io_realm_RealmSchema_nativeGetObjectSchemaByName(JNIEnv *env, jclass, jlong native_schema_ptr, jstring j_name)
{
    TR_ENTER_PTR(native_schema_ptr)
    auto *schema = reinterpret_cast<Schema*>(native_schema_ptr);
    try {
        JStringAccessor name(env, j_name);
        auto it = schema->find(name);
        if (it == schema->end()) {
            return 0;
        }
        else {
            auto& object_schema = *it;
            return reinterpret_cast<jlong>(new ObjectSchema(object_schema));
        }
    }
    CATCH_STD()
    return 0;
}

JNIEXPORT jlongArray JNICALL
Java_io_realm_RealmSchema_nativeGetRealmObjectSchemas(JNIEnv *env, jclass, jlong nativePtr) {
    TR_ENTER_PTR(nativePtr);
    auto *schema = reinterpret_cast<Schema*>(nativePtr);
    try {
        size_t size = schema->size();
        jlongArray native_ptr_array = env->NewLongArray(static_cast<jsize>(size));
        jlong* tmp = new jlong[size];
        auto it = schema->begin();
        size_t index = 0;
        while (it != schema->end()) {
            auto& object_schema = *it;
            tmp[index] = reinterpret_cast<jlong>(&object_schema);
            ++index;
        }
        env->SetLongArrayRegion(native_ptr_array, 0, static_cast<jsize>(size), tmp);
        delete tmp;
        return native_ptr_array;
    }
    CATCH_STD()
    return nullptr;
}
