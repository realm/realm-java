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

#include "io_realm_internal_Property.h"

#include <property.hpp>
#include <object_store.hpp>

#include "util.hpp"

using namespace realm;

static_assert(io_realm_internal_Property_TYPE_INT == static_cast<jint>(PropertyType::Int), "");
static_assert(io_realm_internal_Property_TYPE_BOOL == static_cast<jint>(PropertyType::Bool), "");
static_assert(io_realm_internal_Property_TYPE_STRING == static_cast<jint>(PropertyType::String), "");
static_assert(io_realm_internal_Property_TYPE_DATA == static_cast<jint>(PropertyType::Data), "");
static_assert(io_realm_internal_Property_TYPE_DATE == static_cast<jint>(PropertyType::Date), "");
static_assert(io_realm_internal_Property_TYPE_FLOAT == static_cast<jint>(PropertyType::Float), "");
static_assert(io_realm_internal_Property_TYPE_DOUBLE == static_cast<jint>(PropertyType::Double), "");
static_assert(io_realm_internal_Property_TYPE_OBJECT == static_cast<jint>(PropertyType::Object), "");
static_assert(io_realm_internal_Property_TYPE_LINKING_OBJECTS == static_cast<jint>(PropertyType::LinkingObjects), "");
static_assert(io_realm_internal_Property_TYPE_REQUIRED == static_cast<jint>(PropertyType::Required), "");
static_assert(io_realm_internal_Property_TYPE_NULLABLE == static_cast<jint>(PropertyType::Nullable), "");
static_assert(io_realm_internal_Property_TYPE_ARRAY == static_cast<jint>(PropertyType::Array), "");

static void finalize_property(jlong ptr)
{
    TR_ENTER_PTR(ptr);
    delete reinterpret_cast<Property*>(ptr);
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Property_nativeCreatePersistedProperty(JNIEnv* env, jclass,
                                                                                      jstring j_name_str, jint type,
                                                                                      jboolean is_primary,
                                                                                      jboolean is_indexed)
{
    TR_ENTER()
    try {
        JStringAccessor str(env, j_name_str);
        PropertyType p_type = static_cast<PropertyType>(static_cast<int>(type));
        std::unique_ptr<Property> property(
            new Property(str, p_type, to_bool(is_primary), to_bool(is_indexed)));
        if (to_bool(is_indexed) && !property->type_is_indexable()) {
            throw std::invalid_argument(
                "This field cannot be indexed - Only String/byte/short/int/long/boolean/Date fields are supported.");
        }
        if (to_bool(is_primary) && p_type != PropertyType::Int && p_type != PropertyType::String) {
            std::string typ = property->type_string();
            throw std::invalid_argument("Invalid primary key type: " + typ);
        }
        return reinterpret_cast<jlong>(property.release());
    }
    CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Property_nativeCreatePersistedLinkProperty(JNIEnv* env, jclass,
                                                                                          jstring j_name_str,
                                                                                          jint type,
                                                                                          jstring j_target_class_name)
{
    TR_ENTER()
    try {
        JStringAccessor name(env, j_name_str);
        JStringAccessor link_name(env, j_target_class_name);
        PropertyType p_type = static_cast<PropertyType>(static_cast<int>(type));
        return reinterpret_cast<jlong>(new Property(name, p_type, link_name));
    }
    CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Property_nativeCreateComputedLinkProperty(JNIEnv* env, jclass,
                                                                                         jstring j_name_str,
                                                                                         jstring j_source_class_name,
                                                                                         jstring j_source_field_name)
{
    TR_ENTER()
    try {
        JStringAccessor name(env, j_name_str);
        JStringAccessor target_class_name(env, j_source_class_name);
        JStringAccessor target_field_name(env, j_source_field_name);

        PropertyType p_type = PropertyType::LinkingObjects | PropertyType::Array;
        return reinterpret_cast<jlong>(new Property(name, p_type, target_class_name, target_field_name));
    }
    CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Property_nativeGetFinalizerPtr(JNIEnv*, jclass)
{
    TR_ENTER()
    return reinterpret_cast<jlong>(&finalize_property);
}

JNIEXPORT jint JNICALL Java_io_realm_internal_Property_nativeGetType(JNIEnv*, jclass, jlong native_ptr)
{
    TR_ENTER_PTR(native_ptr);
    auto& property = *reinterpret_cast<Property*>(native_ptr);
    return static_cast<jint>(property.type);
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Property_nativeGetColumnIndex(JNIEnv*, jclass, jlong native_ptr)
{
    TR_ENTER_PTR(native_ptr);
    auto& property = *reinterpret_cast<Property*>(native_ptr);
    return static_cast<jlong>(property.table_column);
}

JNIEXPORT jstring JNICALL Java_io_realm_internal_Property_nativeGetLinkedObjectName(JNIEnv* env, jclass,
                                                                                    jlong native_ptr)
{
    TR_ENTER_PTR(native_ptr);
    try {
        auto& property = *reinterpret_cast<Property*>(native_ptr);
        std::string name = property.object_type;
        if (!name.empty()) {
            return to_jstring(env, name);
        }
    }
    CATCH_STD()
    return nullptr;
}
