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

#include "io_realm_internal_OsObjectSchemaInfo.h"

#include <object_schema.hpp>
#include <property.hpp>

#include "jni_util/java_exception_thrower.hpp"
#include "java_exception_def.hpp"
#include "util.hpp"

using namespace realm;
using namespace realm::jni_util;
using namespace realm::_impl;

static void finalize_object_schema(jlong ptr)
{
    TR_ENTER_PTR(ptr);
    delete reinterpret_cast<ObjectSchema*>(ptr);
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_OsObjectSchemaInfo_nativeCreateRealmObjectSchema(JNIEnv* env, jclass,
                                                                                                jstring j_name_str)
{
    TR_ENTER()
    try {
        JStringAccessor name(env, j_name_str);
        ObjectSchema* object_schema = new ObjectSchema();
        object_schema->name = name;
        return reinterpret_cast<jlong>(object_schema);
    }
    CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_OsObjectSchemaInfo_nativeGetFinalizerPtr(JNIEnv*, jclass)
{
    TR_ENTER()
    return reinterpret_cast<jlong>(&finalize_object_schema);
}


JNIEXPORT void JNICALL Java_io_realm_internal_OsObjectSchemaInfo_nativeAddProperty(JNIEnv* env, jclass,
                                                                                   jlong native_ptr,
                                                                                   jlong property_ptr,
                                                                                   jboolean is_computed)
{
    TR_ENTER_PTR(native_ptr)
    try {
        ObjectSchema* object_schema = reinterpret_cast<ObjectSchema*>(native_ptr);
        Property* property = reinterpret_cast<Property*>(property_ptr);
        if (is_computed) {
            object_schema->computed_properties.push_back(*property);
        }
        else {
            object_schema->persisted_properties.push_back(*property);
            if (property->is_primary) {
                object_schema->primary_key = property->name;
            }
        }
    }
    CATCH_STD()
}

JNIEXPORT jstring JNICALL Java_io_realm_internal_OsObjectSchemaInfo_nativeGetClassName(JNIEnv* env, jclass,
                                                                                       jlong nativePtr)
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

JNIEXPORT jlong JNICALL Java_io_realm_internal_OsObjectSchemaInfo_nativeGetProperty(JNIEnv* env, jclass,
                                                                                    jlong native_ptr,
                                                                                    jstring j_property_name)
{
    TR_ENTER_PTR(native_ptr)
    try {
        auto& object_schema = *reinterpret_cast<ObjectSchema*>(native_ptr);
        JStringAccessor property_name_accessor(env, j_property_name);
        StringData property_name(property_name_accessor);
        auto* property = object_schema.property_for_name(property_name);
        if (property) {
            return reinterpret_cast<jlong>(new Property(*property));
        }
        THROW_JAVA_EXCEPTION(env, JavaExceptionDef::IllegalState,
                             format("Property '%1' cannot be found.", property_name.data()));
    }
    CATCH_STD()
    return reinterpret_cast<jlong>(nullptr);
}
