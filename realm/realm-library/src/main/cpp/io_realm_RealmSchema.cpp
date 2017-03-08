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
#include "io_realm_RealmSchema.h"

#include <object-store/src/schema.hpp>
#include <object-store/src/object_schema.hpp>
#include <object-store/src/property.hpp>

#include "util.hpp"
using namespace realm;


JNIEXPORT jlong JNICALL Java_io_realm_RealmSchema_nativeCreateFromList(JNIEnv* env, jclass,
                                                                       jlongArray objectSchemaPtrs_)
{
    TR_ENTER()
    try {
        std::vector<ObjectSchema> object_schemas;
        JniLongArray array(env, objectSchemaPtrs_);
        for (jsize i = 0; i < array.len(); ++i) {
            ObjectSchema object_schema = *reinterpret_cast<ObjectSchema*>(array[i]);
            object_schemas.push_back(std::move(object_schema));
        }
        auto* schema = new Schema(object_schemas);
        return reinterpret_cast<jlong>(schema);
    }
    CATCH_STD()
    return 0;
}

JNIEXPORT void JNICALL Java_io_realm_RealmSchema_nativeClose(JNIEnv*, jclass, jlong nativePtr)
{
    TR_ENTER_PTR(nativePtr)
    Schema* schema = reinterpret_cast<Schema*>(nativePtr);
    delete schema;
}

JNIEXPORT jlongArray JNICALL Java_io_realm_RealmSchema_nativeGetAll(JNIEnv* env, jclass, jlong nativePtr)
{
    TR_ENTER_PTR(nativePtr)
    try {
        Schema* schema = reinterpret_cast<Schema*>(nativePtr);
        size_t size = schema->size();
        jlongArray native_ptr_array = env->NewLongArray(static_cast<jsize>(size));
        jlong* tmp = new jlong[size];
        auto it = schema->begin();
        size_t index = 0;
        while (it != schema->end()) {
            auto object_schema = *it;
            tmp[index] = reinterpret_cast<jlong>(new ObjectSchema(std::move(object_schema)));
            ++index;
            ++it;
        }
        env->SetLongArrayRegion(native_ptr_array, 0, static_cast<jsize>(size), tmp);
        delete tmp;
        return native_ptr_array;
    }
    CATCH_STD()
    return nullptr;
}
