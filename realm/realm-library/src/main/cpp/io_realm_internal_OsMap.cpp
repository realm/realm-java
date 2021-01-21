/*
 * Copyright 2017 Realm Inc.
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

#include "io_realm_internal_OsMap.h"

#include <realm/object-store/dictionary.hpp>
#include <realm/object-store/shared_realm.hpp>

#include "java_accessor.hpp"
#include "java_object_accessor.hpp"
#include "java_exception_def.hpp"
#include "jni_util/java_exception_thrower.hpp"
#include "util.hpp"

using namespace realm;
using namespace realm::util;
using namespace realm::object_store;
using namespace realm::_impl;

void finalize_map(jlong ptr) {
    delete reinterpret_cast<object_store::Dictionary*>(ptr);
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_OsMap_nativeGetFinalizerPtr(JNIEnv*, jclass) {
    return reinterpret_cast<jlong>(&finalize_map);
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_OsMap_nativeCreate(JNIEnv* env, jclass, jlong shared_realm_ptr,
                                          jlong obj_ptr, jlong column_key) {
    try {
        auto &obj = *reinterpret_cast<realm::Obj*>(obj_ptr);
        auto &shared_realm = *reinterpret_cast<SharedRealm*>(shared_realm_ptr);

        // FIXME: figure out whether or not we need to use something similar to ObservableCollectionWrapper from OsList
        return reinterpret_cast<jlong>(new object_store::Dictionary(shared_realm, obj, ColKey(column_key)));
    }
    CATCH_STD()
    return reinterpret_cast<jlong>(nullptr);
}

JNIEXPORT jobject JNICALL
Java_io_realm_internal_OsMap_nativeGetValue(JNIEnv* env, jclass, jlong map_ptr,
                                            jstring j_key) {
    try {
        auto &dictionary = *reinterpret_cast<realm::object_store::Dictionary*>(map_ptr);
        JStringAccessor key(env, j_key);
        JavaAccessorContext context(env);
        return any_cast<jobject>(dictionary.get(context, StringData(key)));
    }
    CATCH_STD()

    return nullptr;
}

JNIEXPORT void JNICALL
Java_io_realm_internal_OsMap_nativePutMixed(JNIEnv* env, jclass, jlong map_ptr, jstring j_key,
                                            jlong mixed_ptr) {
    try {
        auto &dictionary = *reinterpret_cast<realm::object_store::Dictionary*>(map_ptr);
        auto &mixed = *reinterpret_cast<Mixed*>(mixed_ptr);
        JStringAccessor key(env, j_key);

        switch (mixed.get_type()) {
            case type_Int:
                dictionary.insert(StringData(key).data(), mixed.get_int());
                break;
            case type_Bool:
                dictionary.insert(StringData(key).data(), mixed.get_bool());
                break;
            case type_String:
                dictionary.insert(StringData(key).data(), mixed.get_string());
                break;
            case type_Binary:
                dictionary.insert(StringData(key).data(), mixed.get_binary());
                break;
            case type_Timestamp:
                dictionary.insert(StringData(key).data(), mixed.get_timestamp());
                break;
            case type_Float:
                dictionary.insert(StringData(key).data(), mixed.get_float());
                break;
            case type_Double:
                dictionary.insert(StringData(key).data(), mixed.get_double());
                break;
            case type_Decimal:
                dictionary.insert(StringData(key).data(), mixed.get_decimal());
                break;
            case type_Link:
                dictionary.insert(StringData(key).data(), mixed.get_link());
                break;
            case type_ObjectId:
                dictionary.insert(StringData(key).data(), mixed.get_object_id());
                break;
            case type_UUID:
                dictionary.insert(StringData(key).data(), mixed.get_uuid());
                break;
            case type_TypedLink:
            case type_LinkList:
            case type_Mixed:
                throw std::logic_error(util::format("Invalid data type used for mixed: %1", mixed.get_type()));
        }
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL
Java_io_realm_internal_OsMap_nativePutBoolean(JNIEnv *env, jclass, jlong map_ptr,
                                              jstring j_key, jboolean j_value) {
    try {
        auto &dictionary = *reinterpret_cast<realm::object_store::Dictionary*>(map_ptr);
        JStringAccessor key(env, j_key);
        JavaAccessorContext context(env);
        dictionary.insert(context, StringData(key).data(), Any(j_value));
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL
Java_io_realm_internal_OsMap_nativeClear(JNIEnv* env, jclass, jlong map_ptr) {
    try {
        auto &dictionary = *reinterpret_cast<realm::object_store::Dictionary*>(map_ptr);
        dictionary.remove_all();
    }
    CATCH_STD()
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_OsMap_nativeSize(JNIEnv* env, jclass, jlong map_ptr) {
    try {
        auto &dictionary = *reinterpret_cast<realm::object_store::Dictionary*>(map_ptr);
        return dictionary.size();
    }
    CATCH_STD()
    return 0;
}

JNIEXPORT void JNICALL
Java_io_realm_internal_OsMap_nativeRemove(JNIEnv* env, jclass, jlong map_ptr,
                                          jstring j_key) {
    try {
        auto &dictionary = *reinterpret_cast<realm::object_store::Dictionary*>(map_ptr);
        JStringAccessor key(env, j_key);
        dictionary.erase(StringData(key));
    }
    CATCH_STD()
}
