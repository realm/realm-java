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

#include <realm/util/assert.hpp>

#include <realm/object-store/object_schema.hpp>
#include <realm/object-store/property.hpp>

#include "java_accessor.hpp"
#include "java_exception_def.hpp"
#include "jni_util/java_exception_thrower.hpp"
#include "util.hpp"

using namespace realm;
using namespace realm::jni_util;
using namespace realm::_impl;

static void finalize_object_schema(jlong ptr)
{
    delete reinterpret_cast<ObjectSchema*>(ptr);
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_OsObjectSchemaInfo_nativeCreateRealmObjectSchema(JNIEnv* env, jclass,
                                                                                                jstring j_public_class_name,
                                                                                                jstring j_internal_class_name,
                                                                                                jboolean j_embedded)
{
    try {
        JStringAccessor public_name(env, j_public_class_name);
        JStringAccessor internal_name(env, j_internal_class_name);
        ObjectSchema* object_schema = new ObjectSchema();
        object_schema->name = internal_name;
        object_schema->alias = public_name;
        object_schema->is_embedded = to_bool(j_embedded);
        return reinterpret_cast<jlong>(object_schema);
    }
    CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_OsObjectSchemaInfo_nativeGetFinalizerPtr(JNIEnv*, jclass)
{
    return reinterpret_cast<jlong>(&finalize_object_schema);
}

JNIEXPORT void JNICALL Java_io_realm_internal_OsObjectSchemaInfo_nativeAddProperties(JNIEnv* env, jclass,
                                                                                     jlong native_ptr,
                                                                                     jlongArray j_persisted_properties,
                                                                                     jlongArray j_computed_properties)
{
    try {
        ObjectSchema& object_schema = *reinterpret_cast<ObjectSchema*>(native_ptr);
        JLongArrayAccessor persisted_properties(env, j_persisted_properties);
        for (jsize i = 0; i < persisted_properties.size(); ++i)
        {
            Property* prop = reinterpret_cast<Property*>(persisted_properties[i]);
            REALM_ASSERT_DEBUG(prop != nullptr);
            if (prop->is_primary) {
                object_schema.primary_key = prop->name;
            }
            object_schema.persisted_properties.emplace_back(std::move(*prop));
            delete prop;
        }

        JLongArrayAccessor computed_properties(env, j_computed_properties);
        for (jsize i = 0; i < computed_properties.size(); ++i)
        {
            Property* prop = reinterpret_cast<Property*>(computed_properties[i]);
            REALM_ASSERT_DEBUG(prop != nullptr);
            object_schema.computed_properties.emplace_back(std::move(*prop));
            delete prop;
        }
    }
    CATCH_STD()
}

JNIEXPORT jstring JNICALL Java_io_realm_internal_OsObjectSchemaInfo_nativeGetClassName(JNIEnv* env, jclass,
                                                                                       jlong nativePtr)
{
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
    try {
        auto& object_schema = *reinterpret_cast<ObjectSchema*>(native_ptr);
        JStringAccessor property_name_accessor(env, j_property_name);
        StringData property_name(property_name_accessor);
        auto* property = object_schema.property_for_name(property_name);
        if (property) {
            return reinterpret_cast<jlong>(new Property(*property));
        }
        THROW_JAVA_EXCEPTION(env, JavaExceptionDef::IllegalState,
                             util::format("Property '%1' cannot be found.", property_name.data()));
    }
    CATCH_STD()
    return reinterpret_cast<jlong>(nullptr);
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_OsObjectSchemaInfo_nativeGetPrimaryKeyProperty(JNIEnv* env, jclass,
                                                                                              jlong native_ptr)
{
    try {
        auto& object_schema = *reinterpret_cast<ObjectSchema*>(native_ptr);
        auto* property = object_schema.primary_key_property();
        if (property) {
            return reinterpret_cast<jlong>(new Property(*property));
        }
    }
    CATCH_STD()
    return reinterpret_cast<jlong>(nullptr);
}

JNIEXPORT jboolean JNICALL Java_io_realm_internal_OsObjectSchemaInfo_nativeIsEmbedded(JNIEnv* env, jclass, jlong native_ptr)
{
    try {
        auto& object_schema = *reinterpret_cast<ObjectSchema*>(native_ptr);
        return to_jbool(object_schema.is_embedded);
    }
    CATCH_STD()
    return to_jbool(false);
}
