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
#include "io_realm_internal_Property.h"

#include <stdexcept>
#include <object-store/src/property.hpp>
#include <object-store/src/object_store.hpp>

#include "util.hpp"

using namespace realm;

JNIEXPORT jlong JNICALL Java_io_realm_internal_Property_nativeCreateProperty__Ljava_lang_String_2IZZZ(
    JNIEnv* env, jclass, jstring name_, jint type, jboolean is_primary, jboolean is_indexed, jboolean is_nullable)
{
    TR_ENTER()
    try {
        JStringAccessor str(env, name_);
        PropertyType p_type = static_cast<PropertyType>(static_cast<int>(type));
        std::unique_ptr<Property> property(
            new Property(str, p_type, "", "", to_bool(is_primary), to_bool(is_indexed), to_bool(is_nullable)));
        if (to_bool(is_indexed) && !property->is_indexable()) {
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

JNIEXPORT jlong JNICALL Java_io_realm_internal_Property_nativeCreateProperty__Ljava_lang_String_2ILjava_lang_String_2(
    JNIEnv* env, jclass, jstring name_, jint type, jstring linkedToName_)
{
    TR_ENTER()
    try {
        JStringAccessor name(env, name_);
        JStringAccessor link_name(env, linkedToName_);
        PropertyType p_type = static_cast<PropertyType>(static_cast<int>(type));
        bool is_nullable = (p_type == PropertyType::Object);
        return reinterpret_cast<jlong>(new Property(name, p_type, link_name, "", false, false, is_nullable));
    }
    CATCH_STD()
    return 0;
}

JNIEXPORT void JNICALL Java_io_realm_internal_Property_nativeClose(JNIEnv* env, jclass, jlong property_ptr)
{
    TR_ENTER_PTR(property_ptr)
    try {
        Property* property = reinterpret_cast<Property*>(property_ptr);
        delete property;
    }
    CATCH_STD()
}
