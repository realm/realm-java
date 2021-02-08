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
#include "observable_collection_wrapper.hpp"

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
        auto& obj = *reinterpret_cast<realm::Obj*>(obj_ptr);
        auto& shared_realm = *reinterpret_cast<SharedRealm*>(shared_realm_ptr);

        // FIXME: figure out whether or not we need to use something similar to ObservableCollectionWrapper from OsList
        object_store::Dictionary* dictionary_ptr = new object_store::Dictionary(shared_realm, obj, ColKey(column_key));
        return reinterpret_cast<jlong>(dictionary_ptr);
    }
    CATCH_STD()
    return reinterpret_cast<jlong>(nullptr);
}

JNIEXPORT jobject JNICALL
Java_io_realm_internal_OsMap_nativeGetValue(JNIEnv* env, jclass, jlong map_ptr,
                                            jstring j_key) {
    try {
        auto& dictionary = *reinterpret_cast<realm::object_store::Dictionary*>(map_ptr);
        JStringAccessor key(env, j_key);
        const Optional<Mixed>& optional_result = dictionary.try_get_any(StringData(key));
        if (optional_result) {
            const Mixed& value = optional_result.value();
            if (value.is_null()) {
                return nullptr;
            } else {
                const DataType& type = value.get_type();
                switch (type) {
                    case DataType::Type::Int:
                        return JavaClassGlobalDef::new_long(env, value.get_int());
                    case DataType::Type::Double:
                        return JavaClassGlobalDef::new_double(env, value.get_double());
                    case DataType::Type::Bool:
                        return JavaClassGlobalDef::new_boolean(env, value.get_bool());
                    case DataType::Type::String:
                        return to_jstring(env, value.get_string());
                    case DataType::Type::Binary:
                        return JavaClassGlobalDef::new_byte_array(env, value.get_binary());
                    case DataType::Type::Float:
                        return JavaClassGlobalDef::new_float(env, value.get_float());
                    case DataType::Type::UUID:
                        return JavaClassGlobalDef::new_uuid(env, value.get_uuid());
                    case DataType::Type::ObjectId:
                        return JavaClassGlobalDef::new_object_id(env, value.get_object_id());
                    case DataType::Type::Timestamp:
                        return JavaClassGlobalDef::new_date(env, value.get_timestamp());
                    case DataType::Type::Decimal:
                        return JavaClassGlobalDef::new_decimal128(env, value.get_decimal());
                    default:
                        throw std::logic_error("'getValue' method only suitable for int, double, boolean, String, byte[], float, UUID, Decimal128 and ObjectId.");
                }
            }
        }
    }
    CATCH_STD()

    return nullptr;
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_OsMap_nativeGetMixedPtr(JNIEnv *env, jclass, jlong map_ptr,
                                                 jstring j_key) {
    try {
        auto& dictionary = *reinterpret_cast<realm::object_store::Dictionary*>(map_ptr);
        JStringAccessor key(env, j_key);
        const Optional<Mixed>& optional_result = dictionary.try_get_any(StringData(key));
        if (optional_result) {
            return reinterpret_cast<jlong>(new JavaValue(from_mixed(optional_result.value())));
        }
    }
    CATCH_STD();
    return -1;
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_OsMap_nativeGetRow(JNIEnv* env, jclass, jlong map_ptr,
                                          jstring j_key) {
    try {
        auto& dictionary = *reinterpret_cast<realm::object_store::Dictionary*>(map_ptr);
        JStringAccessor key(env, j_key);
        const Optional<Mixed>& optional_result = dictionary.try_get_any(StringData(key));
        if (optional_result) {
            return optional_result.value().get<ObjKey>().value;
        }
    }
    CATCH_STD()

    return -1;
}

JNIEXPORT void JNICALL
Java_io_realm_internal_OsMap_nativePutMixed(JNIEnv* env, jclass, jlong map_ptr, jstring j_key,
                                            jlong mixed_ptr) {
    try {
        auto& dictionary = *reinterpret_cast<realm::object_store::Dictionary*>(map_ptr);
        auto mixed_java_value = *reinterpret_cast<JavaValue*>(mixed_ptr);
        const Mixed& mixed = mixed_java_value.to_mixed();
        JStringAccessor key(env, j_key);
        dictionary.insert(StringData(key), mixed);
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL
Java_io_realm_internal_OsMap_nativePutNull(JNIEnv* env, jclass, jlong map_ptr,
                                           jstring j_key) {
    try {
        auto& dictionary = *reinterpret_cast<realm::object_store::Dictionary*>(map_ptr);
        JStringAccessor key(env, j_key);
        JavaAccessorContext context(env);
        dictionary.insert(context, StringData(key).data(), Any());
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL
Java_io_realm_internal_OsMap_nativePutLong(JNIEnv* env, jclass, jlong map_ptr,
                                           jstring j_key, jlong j_value) {
    try {
        auto& dictionary = *reinterpret_cast<realm::object_store::Dictionary*>(map_ptr);
        JStringAccessor key(env, j_key);
        JavaAccessorContext context(env);
        dictionary.insert(context, StringData(key).data(), Any(j_value));
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL
Java_io_realm_internal_OsMap_nativePutFloat(JNIEnv* env, jclass, jlong map_ptr,
                                            jstring j_key, jfloat j_value) {
    try {
        auto& dictionary = *reinterpret_cast<realm::object_store::Dictionary*>(map_ptr);
        JStringAccessor key(env, j_key);
        JavaAccessorContext context(env);
        dictionary.insert(context, StringData(key).data(), Any(j_value));
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL
Java_io_realm_internal_OsMap_nativePutDouble(JNIEnv* env, jclass, jlong map_ptr,
                                             jstring j_key, jdouble j_value) {
    try {
        auto& dictionary = *reinterpret_cast<realm::object_store::Dictionary*>(map_ptr);
        JStringAccessor key(env, j_key);
        JavaAccessorContext context(env);
        dictionary.insert(context, StringData(key).data(), Any(j_value));
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL
Java_io_realm_internal_OsMap_nativePutString(JNIEnv* env, jclass, jlong map_ptr,
                                             jstring j_key, jstring j_value) {
    try {
        auto& dictionary = *reinterpret_cast<realm::object_store::Dictionary*>(map_ptr);
        JStringAccessor key(env, j_key);
        JStringAccessor value(env, j_value);
        JavaAccessorContext context(env);
        dictionary.insert(context, StringData(key).data(), Any(value));
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL
Java_io_realm_internal_OsMap_nativePutBoolean(JNIEnv* env, jclass, jlong map_ptr,
                                              jstring j_key, jboolean j_value) {
    try {
        auto& dictionary = *reinterpret_cast<realm::object_store::Dictionary*>(map_ptr);
        JStringAccessor key(env, j_key);
        JavaAccessorContext context(env);
        dictionary.insert(context, StringData(key).data(), Any(j_value));
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL
Java_io_realm_internal_OsMap_nativePutDate(JNIEnv* env, jclass, jlong map_ptr,
                                           jstring j_key, jlong j_value) {
    try {
        auto& dictionary = *reinterpret_cast<realm::object_store::Dictionary*>(map_ptr);
        JStringAccessor key(env, j_key);
        JavaAccessorContext context(env);
        dictionary.insert(context, StringData(key).data(), Any(j_value));
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL
Java_io_realm_internal_OsMap_nativePutDecimal128(JNIEnv* env, jclass, jlong map_ptr,
                                                 jstring j_key, jlong j_high_value,
                                                 jlong j_low_value) {
    try {
        auto& dictionary = *reinterpret_cast<realm::object_store::Dictionary*>(map_ptr);
        JStringAccessor key(env, j_key);
        Decimal128::Bid128 raw {static_cast<uint64_t>(j_low_value), static_cast<uint64_t>(j_high_value)};
        auto decimal128 = Decimal128(raw);
        JavaAccessorContext context(env);
        dictionary.insert(context, StringData(key).data(), Any(decimal128));
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL
Java_io_realm_internal_OsMap_nativePutBinary(JNIEnv* env, jclass, jlong map_ptr,
                                             jstring j_key, jbyteArray j_value) {
    try {
        auto& dictionary = *reinterpret_cast<realm::object_store::Dictionary*>(map_ptr);
        JStringAccessor key(env, j_key);
        JByteArrayAccessor data(env, j_value);
        JavaAccessorContext context(env);
        dictionary.insert(context, StringData(key).data(), Any(data));
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL
Java_io_realm_internal_OsMap_nativePutObjectId(JNIEnv* env, jclass, jlong map_ptr, jstring j_key,
                                               jstring j_value) {
    try {
        auto& dictionary = *reinterpret_cast<realm::object_store::Dictionary*>(map_ptr);
        JStringAccessor key(env, j_key);
        JStringAccessor data(env, j_value);

        const ObjectId object_id = ObjectId(StringData(data).data());

        JavaAccessorContext context(env);
        dictionary.insert(context, StringData(key).data(), Any(object_id));
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL
Java_io_realm_internal_OsMap_nativePutUUID(JNIEnv* env, jclass, jlong map_ptr, jstring j_key,
                                           jstring j_value) {
    try {
        auto& dictionary = *reinterpret_cast<realm::object_store::Dictionary*>(map_ptr);
        JStringAccessor key(env, j_key);
        JStringAccessor value(env, j_value);
        JavaAccessorContext context(env);
        dictionary.insert(context, StringData(key).data(), Any(UUID(StringData(value).data())));
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL
Java_io_realm_internal_OsMap_nativePutRow(JNIEnv* env, jclass, jlong map_ptr, jstring j_key,
                                          jlong j_obj_key) {
    try {
        auto& dictionary = *reinterpret_cast<realm::object_store::Dictionary*>(map_ptr);
        JStringAccessor key(env, j_key);
        dictionary.insert(StringData(key).data(), ObjKey(j_obj_key));
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL
Java_io_realm_internal_OsMap_nativeClear(JNIEnv* env, jclass, jlong map_ptr) {
    try {
        auto& dictionary = *reinterpret_cast<realm::object_store::Dictionary*>(map_ptr);
        dictionary.remove_all();
    }
    CATCH_STD()
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_OsMap_nativeSize(JNIEnv* env, jclass, jlong map_ptr) {
    try {
        auto& dictionary = *reinterpret_cast<realm::object_store::Dictionary*>(map_ptr);
        return dictionary.size();
    }
    CATCH_STD()
    return reinterpret_cast<jlong>(nullptr);
}

JNIEXPORT jboolean JNICALL
Java_io_realm_internal_OsMap_nativeContainsKey(JNIEnv* env, jclass, jlong map_ptr,
                                               jstring j_key) {
    try {
        auto& dictionary = *reinterpret_cast<realm::object_store::Dictionary*>(map_ptr);
        JStringAccessor key(env, j_key);
        return dictionary.contains(StringData(key).data());
    }
    CATCH_STD()
    return false;
}

JNIEXPORT void JNICALL
Java_io_realm_internal_OsMap_nativeRemove(JNIEnv* env, jclass, jlong map_ptr,
                                          jstring j_key) {
    try {
        auto& dictionary = *reinterpret_cast<realm::object_store::Dictionary*>(map_ptr);
        JStringAccessor key(env, j_key);
        dictionary.erase(StringData(key));
    }
    CATCH_STD()
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_OsMap_nativeKeys(JNIEnv* env, jclass, jlong map_ptr) {
    try {
        auto& dictionary = *reinterpret_cast<realm::object_store::Dictionary*>(map_ptr);
        const Results& key_results = dictionary.get_keys();
        auto wrapper = new ObservableCollectionWrapper(key_results);
        return reinterpret_cast<jlong>(wrapper);
    }
    CATCH_STD()
    return reinterpret_cast<jlong>(nullptr);
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_OsMap_nativeValues(JNIEnv* env, jclass, jlong map_ptr) {
    try {
        auto& dictionary = *reinterpret_cast<realm::object_store::Dictionary*>(map_ptr);
        const Results& value_results = dictionary.get_values();
        auto wrapper = new ObservableCollectionWrapper(value_results);
        return reinterpret_cast<jlong>(wrapper);
    }
    CATCH_STD()
    return reinterpret_cast<jlong>(nullptr);
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_OsMap_nativeFreeze(JNIEnv* env, jclass, jlong map_ptr,
                                          jlong realm_ptr) {
    try {
        auto& dictionary = *reinterpret_cast<realm::object_store::Dictionary*>(map_ptr);
        std::shared_ptr<Realm>& shared_realm_ptr = *reinterpret_cast<std::shared_ptr<Realm>*>(realm_ptr);
        const object_store::Dictionary& frozen_dictionary = dictionary.freeze(shared_realm_ptr);
        auto* frozen_dictionary_ptr = new object_store::Dictionary(frozen_dictionary);
        return reinterpret_cast<jlong>(frozen_dictionary_ptr);
    }
    CATCH_STD()
    return reinterpret_cast<jlong>(nullptr);
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_OsMap_nativeCreateAndPutEmbeddedObject(JNIEnv* env, jclass,
                                                              jlong shared_realm_ptr,
                                                              jlong map_ptr,
                                                              jstring j_key) {
    try {
        auto& realm = *reinterpret_cast<SharedRealm*>(shared_realm_ptr);
        auto& dictionary = *reinterpret_cast<realm::object_store::Dictionary*>(map_ptr);
        auto& object_schema = dictionary.get_object_schema();

        JStringAccessor key(env, j_key);
        JavaContext context(env, realm, object_schema);

        dictionary.insert(context, StringData(key), JavaValue(std::map<ColKey, JavaValue>()), CreatePolicy::Skip);
        const Mixed& mixed = dictionary.get_any(StringData(key));
        return reinterpret_cast<jlong>(mixed.get_link().get_obj_key().value);
    }
    CATCH_STD()
    return reinterpret_cast<jlong>(nullptr);
}
