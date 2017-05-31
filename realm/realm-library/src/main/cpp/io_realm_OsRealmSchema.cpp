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
#include "io_realm_OsRealmSchema.h"

#include <object-store/src/schema.hpp>
#include <object-store/src/object_schema.hpp>
#include <object-store/src/property.hpp>

#include "util.hpp"
using namespace realm;


JNIEXPORT jlong JNICALL Java_io_realm_OsRealmSchema_nativeCreateFromList(JNIEnv* env, jclass,
                                                                       jlongArray objectSchemaPtrs_)
{
    TR_ENTER()
    try {
        std::vector<ObjectSchema> object_schemas;
        JniLongArray array(env, objectSchemaPtrs_);
        for (jsize i = 0; i < array.len(); ++i) {
            object_schemas.push_back(*reinterpret_cast<ObjectSchema*>(array[i]));
        }
        auto* schema = new Schema(object_schemas);
        return reinterpret_cast<jlong>(schema);
    }
    CATCH_STD()
    return 0;
}

JNIEXPORT void JNICALL Java_io_realm_OsRealmSchema_nativeClose(JNIEnv*, jclass, jlong nativePtr)
{
    TR_ENTER_PTR(nativePtr)
    Schema* schema = reinterpret_cast<Schema*>(nativePtr);
    delete schema;
}
