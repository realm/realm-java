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

#include "util.hpp"

using namespace realm;

JNIEXPORT jlong JNICALL
io_realm_RealmSchema_nativeCreateSchema(JNIEnv *env, jclass)
{
    TR_ENTER()
    try {
        auto *schema = new Schema({});
        return reinterpret_cast<jlong>(schema);
    }
    CATCH_STD()
    return 0;
}

JNIEXPORT void JNICALL
io_realm_RealmSchema_nativeClose(JNIEnv *env, jclass, jlong native_ptr)
{
    TR_ENTER_PTR(native_ptr)
    auto *schema = reinterpret_cast<Schema*>(native_ptr);
    delete schema;
}

JNIEXPORT void JNICALL
io_realm_RealmSchema_nativeAddObjectSchema(JNIEnv *env, jclass, jlong native_schema_ptr, jlong native_object_schema_ptr)
{
    TR_ENTER_PTR(native_schema_ptr)
    auto *schema = reinterpret_cast<Schema*>(native_schema_ptr);
    auto object_schema = *reinterpret_cast<ObjectSchema*>(native_object_schema_ptr);
    try {
        schema->push_back(object_schema);
    }
    CATCH_STD()
}

JNIEXPORT jboolean JNICALL
io_realm_RealmSchema_nativeHasObjectSchemaByName(JNIEnv *env, jclass, jlong native_schema_ptr, jstring j_name)
{
    TR_ENTER_PTR(native_schema_ptr)
    auto *schema = reinterpret_cast<Schema*>(native_schema_ptr);
    try {
        JStringAccessor name(env, j_name);
        auto object_schema = schema->find(name);
        return object_schema != schema->end();
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL
io_realm_RealmSchema_nativeRemoveObjectSchemaByName(JNIEnv *env, jclass, jlong native_schema_ptr, jstring j_name)
{
    TR_ENTER_PTR(native_schema_ptr)
    auto *schema = reinterpret_cast<Schema*>(native_schema_ptr);
    try {
        JStringAccessor name(env, j_name);
        auto object_schema = schema->find(name);
        schema->erase(object_schema);
    }
    CATCH_STD()
}

JNIEXPORT jlong JNICALL
io_realm_RealmSchema_nativeGetObjectSchemaByName(JNIEnv *env, jclass, jlong native_schema_ptr, jstring j_name)
{
    TR_ENTER_PTR(native_schema_ptr)
    auto *schema = reinterpret_cast<Schema*>(native_schema_ptr);
    try {
        JStringAccessor name(env, j_name);
        auto object_schema_iterator = schema->find(name);
        if (object_schema_iterator == schema->end()) {
            return nullptr;
        }
        else {
            auto object_schema = *object_schema_iterator;
            return reinterpret_cast<jlong>(&object_schema);
        }
    }
    CATCH_STD()
}
