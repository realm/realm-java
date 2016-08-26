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
#include "io_realm_Property.h"

#include <object-store/src/property.hpp>

#include "util.hpp"

using namespace realm;

JNIEXPORT jlong JNICALL
Java_io_realm_Property_nativeCreateProperty__Ljava_lang_String_2IZZZ
(JNIEnv *env, jclass, jstring name, jint type, jboolean is_primary, jboolean is_indexed, jboolean is_nullable) {
    TR_ENTER()
    try {
        JStringAccessor str(env, name);
        PropertyType p_type = static_cast<PropertyType>(static_cast<int>(type)); // FIXME: is validation done by object store?
        Property *property = new Property(str, p_type, "", "", to_bool(is_primary), to_bool(is_indexed), to_bool(is_nullable));
        return reinterpret_cast<jlong>(property);
    }
    CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL
Java_io_realm_Property_nativeCreateProperty__Ljava_lang_String_2ILjava_lang_String_2(JNIEnv *env, jclass, jstring j_name, jint type, jstring j_link_name) {
    TR_ENTER()
    try {
        JStringAccessor name(env, j_name);
        JStringAccessor link_name(env, j_link_name);
        auto  p_type = static_cast<PropertyType>(static_cast<int>(type)); // FIXME: is validation done by object store?
        bool is_nullable = (p_type == PropertyType::Object);
        auto *property = new Property(name, p_type, link_name, "", false, false, is_nullable);
        return reinterpret_cast<jlong>(property);
    }
    CATCH_STD()
    return 0;
}

JNIEXPORT void JNICALL
Java_io_realm_Property_nativeClose
(JNIEnv *env, jclass, jlong property_ptr) {
    TR_ENTER_PTR(property_ptr)
    try {
        auto *property = reinterpret_cast<Property *>(property_ptr);
        delete property;
    }
    CATCH_STD()
}

JNIEXPORT jboolean JNICALL
Java_io_realm_Property_nativeIsIndexable
(JNIEnv *env, jclass, jlong property_ptr) {
    TR_ENTER_PTR(property_ptr)
    try {
        auto *property = reinterpret_cast<Property *>(property_ptr);
        return property->is_indexable();
    }
    CATCH_STD()
    return JNI_FALSE;
}

JNIEXPORT jboolean JNICALL
Java_io_realm_Property_nativeRequiresIndex
(JNIEnv *env, jclass, jlong property_ptr) {
    TR_ENTER_PTR(property_ptr)
    try {
        auto *property = reinterpret_cast<Property *>(property_ptr);
        return property->requires_index();
    }
    CATCH_STD()
    return JNI_FALSE;
}

JNIEXPORT jboolean JNICALL
Java_io_realm_Property_nativeIsNullable(JNIEnv *env, jclass, jlong property_ptr) {
    TR_ENTER_PTR(property_ptr)
    try {
        auto *property = reinterpret_cast<Property *>(property_ptr);
        return static_cast<jboolean>(property->is_nullable);
    }
    CATCH_STD()
    return JNI_FALSE;
}

JNIEXPORT jstring JNICALL
Java_io_realm_Property_nativeGetName(JNIEnv *env, jclass, jlong property_ptr) {
    TR_ENTER_PTR(property_ptr)
    try {
        auto *property = reinterpret_cast<Property *>(property_ptr);
        return to_jstring(env, property->name);
    }
    CATCH_STD()
    return nullptr;
}

JNIEXPORT void JNICALL
Java_io_realm_Property_nativeSetName(JNIEnv *env, jclass, jlong property_ptr, jstring name_) {
    TR_ENTER_PTR(property_ptr)
    try {
        JStringAccessor name(env, name_);
        auto *property = reinterpret_cast<Property *>(property_ptr);
        property->name = name;
    }
    CATCH_STD()
}

JNIEXPORT jboolean JNICALL
Java_io_realm_Property_nativeIsPrimaryKey(JNIEnv *env, jclass, jlong property_ptr) {
    TR_ENTER_PTR(property_ptr)
    try {
        auto *property = reinterpret_cast<Property *>(property_ptr);
        return static_cast<jboolean>(property->is_primary);
    }
    CATCH_STD()
    return JNI_FALSE;
}