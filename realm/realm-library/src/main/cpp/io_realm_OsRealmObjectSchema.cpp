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
#include "io_realm_OsRealmObjectSchema.h"

#include <object-store/src/object_schema.hpp>
#include <object-store/src/property.hpp>

#include "util.hpp"
using namespace realm;

JNIEXPORT jlong JNICALL Java_io_realm_OsRealmObjectSchema_nativeCreateRealmObjectSchema(JNIEnv* env, jclass,
                                                                                        jstring className_)
{
    TR_ENTER()
    try {
        JStringAccessor name(env, className_);
        ObjectSchema* object_schema = new ObjectSchema();
        object_schema->name = name;
        return reinterpret_cast<jlong>(object_schema);
    }
    CATCH_STD()
    return 0;
}

JNIEXPORT void JNICALL Java_io_realm_OsRealmObjectSchema_nativeClose(JNIEnv* env, jclass, jlong native_ptr)
{
    TR_ENTER_PTR(native_ptr)
    try {
        ObjectSchema* object_schema = reinterpret_cast<ObjectSchema*>(native_ptr);
        delete object_schema;
    }
    CATCH_STD()
}


JNIEXPORT void JNICALL Java_io_realm_OsRealmObjectSchema_nativeAddProperty(JNIEnv* env, jclass, jlong native_ptr,
                                                                         jlong property_ptr)
{
    TR_ENTER_PTR(native_ptr)
    try {
        ObjectSchema* object_schema = reinterpret_cast<ObjectSchema*>(native_ptr);
        Property* property = reinterpret_cast<Property*>(property_ptr);
        object_schema->persisted_properties.push_back(*property);
        if (property->is_primary) {
            object_schema->primary_key = property->name;
        }
    }
    CATCH_STD()
}

JNIEXPORT jstring JNICALL Java_io_realm_OsRealmObjectSchema_nativeGetClassName(JNIEnv* env, jclass, jlong nativePtr)
{
    TR_ENTER_PTR(nativePtr)
    try {
        ObjectSchema* object_schema = reinterpret_cast<ObjectSchema*>(nativePtr);
        auto name = object_schema->name;
        return to_jstring(env, name);
    }
    CATCH_STD()

    return nullptr;
}
