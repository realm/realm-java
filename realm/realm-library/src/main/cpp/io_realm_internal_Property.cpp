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

#include <object-store/src/property.hpp>

#include "util.hpp"

using namespace realm;

JNIEXPORT jlong JNICALL
Java_io_realm_internal_Property_nativeCreateProperty
(JNIEnv *env, jstring name, jint type, jboolean is_primary, jboolean is_indexed, jboolean is_nullable) {
    TR_ENTER()
    try {
        JStringAccessor str(env, name);
        PropertyType p_type = static_cast<PropertyType>(static_cast<int>(type));
        Property *property = new Property(str, p_type, nullptr, nullptr, to_bool(is_primary), to_bool(is_indexed), to_bool(is_nullable));
        return reinterpret_cast<jlong>(property);
    }
    CATCH_STD()
    return 0;
}

JNIEXPORT void JNICALL
Java_io_realm_internal_Property_nativeClose
(JNIEnv *, jlong property_ptr) {
    TR_ENTER_PTR(property_ptr)
    auto* property = reinterpret_cast<Property*>(property_ptr);
    delete property;
}

JNIEXPORT jboolean JNICALL
Java_io_realm_internal_Property_nativeIsIndexable
(JNIEnv *, jlong property_ptr) {
    TR_ENTER_PTR(property_ptr)

    auto* property = reinterpret_cast<Property*>(property_ptr);
    return property->is_indexable();
}

JNIEXPORT jboolean JNICALL
Java_io_realm_internal_Property_nativeRequiresIndex
(JNIEnv *, jlong property_ptr) {
    TR_ENTER_PTR(property_ptr)

    auto* property = reinterpret_cast<Property*>(property_ptr);
    return property->requires_index();
}
