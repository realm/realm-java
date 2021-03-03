/*
 * Copyright 2020 Realm Inc.
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
#include "observable_dictionary_wrapper.hpp"

using namespace realm;
using namespace realm::util;
using namespace realm::object_store;
using namespace realm::_impl;

void finalize_map(jlong ptr) {
    delete reinterpret_cast<ObservableDictionaryWrapper*>(ptr);
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_OsMap_nativeGetFinalizerPtr(JNIEnv*, jclass) {
    return reinterpret_cast<jlong>(&finalize_map);
}

JNIEXPORT jlongArray JNICALL
Java_io_realm_internal_OsMap_nativeCreate(JNIEnv* env, jclass, jlong shared_realm_ptr,
                                          jlong obj_ptr, jlong column_key) {
    try {
        auto obj = *reinterpret_cast<realm::Obj*>(obj_ptr);
        auto shared_realm = *reinterpret_cast<SharedRealm*>(shared_realm_ptr);

        // Return an array of pointers: first to the wrapper, second to the table (if applicable)
        jlong ret[2];

        // Get dictionary, put it in the wrapper and save pointer to be returned
        object_store::Dictionary dictionary(shared_realm, obj, ColKey(column_key));
        auto wrapper_ptr = new ObservableDictionaryWrapper(dictionary);
        ret[0] = reinterpret_cast<jlong>(wrapper_ptr);

        // Special case for objects: return the table. Ignore for other types
        if (wrapper_ptr->collection().get_type() == PropertyType::Object) {
            const DictionaryPtr& ptr = obj.get_dictionary_ptr(ColKey(column_key));
            auto target_table_ptr = new TableRef(ptr->get_target_table());
            ret[1] = reinterpret_cast<jlong>(target_table_ptr);
        } else {
            ret[1] = reinterpret_cast<jlong>(nullptr);
        }

        jlongArray ret_array = env->NewLongArray(2);
        if (!ret_array) {
            ThrowException(env, OutOfMemory, "Could not allocate memory to create OsMap.");
            return nullptr;
        }

        env->SetLongArrayRegion(ret_array, 0, 2, ret);
        return ret_array;
    }
    CATCH_STD()
    return nullptr;
}

JNIEXPORT jobject JNICALL
Java_io_realm_internal_OsMap_nativeGetValue(JNIEnv* env, jclass, jlong wrapper_ptr,
                                            jstring j_key) {
    try {
        auto& wrapper = *reinterpret_cast<ObservableDictionaryWrapper*>(wrapper_ptr);
        auto& dictionary = wrapper.collection();
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
Java_io_realm_internal_OsMap_nativeGetMixedPtr(JNIEnv *env, jclass, jlong wrapper_ptr,
                                               jstring j_key) {
    try {
        auto& wrapper = *reinterpret_cast<ObservableDictionaryWrapper*>(wrapper_ptr);
        auto& dictionary = wrapper.collection();
        JStringAccessor key(env, j_key);
        const Optional<Mixed>& optional_result = dictionary.try_get_any(StringData(key));
        if (optional_result) {
            return reinterpret_cast<jlong>(new JavaValue(from_mixed(optional_result.value())));
        }
    }
    CATCH_STD();
    return io_realm_internal_OsMap_NOT_FOUND;
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_OsMap_nativeGetRow(JNIEnv* env, jclass, jlong wrapper_ptr,
                                          jstring j_key) {
    try {
        auto& wrapper = *reinterpret_cast<ObservableDictionaryWrapper*>(wrapper_ptr);
        auto& dictionary = wrapper.collection();
        JStringAccessor key(env, j_key);
        const Optional<Mixed>& optional_result = dictionary.try_get_any(StringData(key));
        if (optional_result) {
            const Mixed& value = optional_result.value();
            if (!value.is_null()) {
                return optional_result.value().get<ObjKey>().value;
            }
        }
    }
    CATCH_STD()
    return io_realm_internal_OsMap_NOT_FOUND;
}

JNIEXPORT void JNICALL
Java_io_realm_internal_OsMap_nativePutMixed(JNIEnv* env, jclass, jlong wrapper_ptr, jstring j_key,
                                            jlong mixed_ptr) {
    try {
        auto& wrapper = *reinterpret_cast<ObservableDictionaryWrapper*>(wrapper_ptr);
        auto& dictionary = wrapper.collection();
        auto mixed_java_value = *reinterpret_cast<JavaValue*>(mixed_ptr);
        const Mixed& mixed = mixed_java_value.to_mixed();
        JStringAccessor key(env, j_key);
        dictionary.insert(StringData(key), mixed);
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL
Java_io_realm_internal_OsMap_nativePutNull(JNIEnv* env, jclass, jlong wrapper_ptr,
                                           jstring j_key) {
    try {
        auto& wrapper = *reinterpret_cast<ObservableDictionaryWrapper*>(wrapper_ptr);
        auto& dictionary = wrapper.collection();
        JStringAccessor key(env, j_key);
        dictionary.insert(StringData(key).data(), Mixed());
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL
Java_io_realm_internal_OsMap_nativePutLong(JNIEnv* env, jclass, jlong wrapper_ptr,
                                           jstring j_key, jlong j_value) {
    try {
        auto& wrapper = *reinterpret_cast<ObservableDictionaryWrapper*>(wrapper_ptr);
        auto& dictionary = wrapper.collection();
        JStringAccessor key(env, j_key);
        JavaAccessorContext context(env);
        dictionary.insert(context, StringData(key).data(), Any(j_value));
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL
Java_io_realm_internal_OsMap_nativePutFloat(JNIEnv* env, jclass, jlong wrapper_ptr,
                                            jstring j_key, jfloat j_value) {
    try {
        auto& wrapper = *reinterpret_cast<ObservableDictionaryWrapper*>(wrapper_ptr);
        auto& dictionary = wrapper.collection();
        JStringAccessor key(env, j_key);
        JavaAccessorContext context(env);
        dictionary.insert(context, StringData(key).data(), Any(j_value));
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL
Java_io_realm_internal_OsMap_nativePutDouble(JNIEnv* env, jclass, jlong wrapper_ptr,
                                             jstring j_key, jdouble j_value) {
    try {
        auto& wrapper = *reinterpret_cast<ObservableDictionaryWrapper*>(wrapper_ptr);
        auto& dictionary = wrapper.collection();
        JStringAccessor key(env, j_key);
        JavaAccessorContext context(env);
        dictionary.insert(context, StringData(key).data(), Any(j_value));
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL
Java_io_realm_internal_OsMap_nativePutString(JNIEnv* env, jclass, jlong wrapper_ptr,
                                             jstring j_key, jstring j_value) {
    try {
        auto& wrapper = *reinterpret_cast<ObservableDictionaryWrapper*>(wrapper_ptr);
        auto& dictionary = wrapper.collection();
        JStringAccessor key(env, j_key);
        JStringAccessor value(env, j_value);
        JavaAccessorContext context(env);
        dictionary.insert(context, StringData(key).data(), Any(value));
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL
Java_io_realm_internal_OsMap_nativePutBoolean(JNIEnv* env, jclass, jlong wrapper_ptr,
                                              jstring j_key, jboolean j_value) {
    try {
        auto& wrapper = *reinterpret_cast<ObservableDictionaryWrapper*>(wrapper_ptr);
        auto& dictionary = wrapper.collection();
        JStringAccessor key(env, j_key);
        JavaAccessorContext context(env);
        dictionary.insert(context, StringData(key).data(), Any(j_value));
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL
Java_io_realm_internal_OsMap_nativePutDate(JNIEnv* env, jclass, jlong wrapper_ptr,
                                           jstring j_key, jlong j_value) {
    try {
        auto& wrapper = *reinterpret_cast<ObservableDictionaryWrapper*>(wrapper_ptr);
        auto& dictionary = wrapper.collection();
        JStringAccessor key(env, j_key);
        JavaAccessorContext context(env);
        dictionary.insert(context, StringData(key).data(), Any(j_value));
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL
Java_io_realm_internal_OsMap_nativePutDecimal128(JNIEnv* env, jclass, jlong wrapper_ptr,
                                                 jstring j_key, jlong j_high_value,
                                                 jlong j_low_value) {
    try {
        auto& wrapper = *reinterpret_cast<ObservableDictionaryWrapper*>(wrapper_ptr);
        auto& dictionary = wrapper.collection();
        JStringAccessor key(env, j_key);
        Decimal128::Bid128 raw {static_cast<uint64_t>(j_low_value), static_cast<uint64_t>(j_high_value)};
        auto decimal128 = Decimal128(raw);
        JavaAccessorContext context(env);
        dictionary.insert(context, StringData(key).data(), Any(decimal128));
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL
Java_io_realm_internal_OsMap_nativePutBinary(JNIEnv* env, jclass, jlong wrapper_ptr,
                                             jstring j_key, jbyteArray j_value) {
    try {
        auto& wrapper = *reinterpret_cast<ObservableDictionaryWrapper*>(wrapper_ptr);
        auto& dictionary = wrapper.collection();
        JStringAccessor key(env, j_key);
        JByteArrayAccessor data(env, j_value);
        JavaAccessorContext context(env);
        dictionary.insert(context, StringData(key).data(), Any(data));
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL
Java_io_realm_internal_OsMap_nativePutObjectId(JNIEnv* env, jclass, jlong wrapper_ptr, jstring j_key,
                                               jstring j_value) {
    try {
        auto& wrapper = *reinterpret_cast<ObservableDictionaryWrapper*>(wrapper_ptr);
        auto& dictionary = wrapper.collection();
        JStringAccessor key(env, j_key);
        JStringAccessor data(env, j_value);

        const ObjectId object_id = ObjectId(StringData(data).data());

        JavaAccessorContext context(env);
        dictionary.insert(context, StringData(key).data(), Any(object_id));
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL
Java_io_realm_internal_OsMap_nativePutUUID(JNIEnv* env, jclass, jlong wrapper_ptr, jstring j_key,
                                           jstring j_value) {
    try {
        auto& wrapper = *reinterpret_cast<ObservableDictionaryWrapper*>(wrapper_ptr);
        auto& dictionary = wrapper.collection();
        JStringAccessor key(env, j_key);
        JStringAccessor value(env, j_value);
        JavaAccessorContext context(env);
        dictionary.insert(context, StringData(key).data(), Any(UUID(StringData(value).data())));
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL
Java_io_realm_internal_OsMap_nativePutRow(JNIEnv* env, jclass, jlong wrapper_ptr, jstring j_key,
                                          jlong j_obj_key) {
    try {
        auto& wrapper = *reinterpret_cast<ObservableDictionaryWrapper*>(wrapper_ptr);
        auto& dictionary = wrapper.collection();
        JStringAccessor key(env, j_key);
        dictionary.insert(StringData(key).data(), ObjKey(j_obj_key));
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL
Java_io_realm_internal_OsMap_nativeClear(JNIEnv* env, jclass, jlong wrapper_ptr) {
    try {
        auto& wrapper = *reinterpret_cast<ObservableDictionaryWrapper*>(wrapper_ptr);
        auto& dictionary = wrapper.collection();
        dictionary.remove_all();
    }
    CATCH_STD()
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_OsMap_nativeSize(JNIEnv* env, jclass, jlong wrapper_ptr) {
    try {
        auto& wrapper = *reinterpret_cast<ObservableDictionaryWrapper*>(wrapper_ptr);
        auto& dictionary = wrapper.collection();
        return dictionary.size();
    }
    CATCH_STD()
    return reinterpret_cast<jlong>(nullptr);
}

JNIEXPORT jboolean JNICALL
Java_io_realm_internal_OsMap_nativeContainsKey(JNIEnv* env, jclass, jlong wrapper_ptr,
                                               jstring j_key) {
    try {
        auto& wrapper = *reinterpret_cast<ObservableDictionaryWrapper*>(wrapper_ptr);
        auto& dictionary = wrapper.collection();
        JStringAccessor key(env, j_key);
        return dictionary.contains(StringData(key).data());
    }
    CATCH_STD()
    return false;
}

JNIEXPORT jboolean JNICALL
Java_io_realm_internal_OsMap_nativeIsValid(JNIEnv* env, jclass, jlong wrapper_ptr) {
    try {
        auto& wrapper = *reinterpret_cast<ObservableDictionaryWrapper*>(wrapper_ptr);
        auto& dictionary = wrapper.collection();
        return dictionary.is_valid();
    }
    CATCH_STD()
    return false;
}

JNIEXPORT void JNICALL
Java_io_realm_internal_OsMap_nativeRemove(JNIEnv* env, jclass, jlong wrapper_ptr,
                                          jstring j_key) {
    try {
        auto& wrapper = *reinterpret_cast<ObservableDictionaryWrapper*>(wrapper_ptr);
        auto& dictionary = wrapper.collection();
        JStringAccessor key(env, j_key);
        dictionary.erase(StringData(key));
    }
    CATCH_STD()
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_OsMap_nativeKeys(JNIEnv* env, jclass, jlong wrapper_ptr) {
    try {
        auto& wrapper = *reinterpret_cast<ObservableDictionaryWrapper*>(wrapper_ptr);
        auto& dictionary = wrapper.collection();
        const Results& key_results = dictionary.get_keys();

        // Return an ObservableCollectionWrapper as these are results!
        auto results_wrapper = new ObservableCollectionWrapper(key_results);
        return reinterpret_cast<jlong>(results_wrapper);
    }
    CATCH_STD()
    return reinterpret_cast<jlong>(nullptr);
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_OsMap_nativeValues(JNIEnv* env, jclass, jlong wrapper_ptr) {
    try {
        auto& wrapper = *reinterpret_cast<ObservableDictionaryWrapper*>(wrapper_ptr);
        auto& dictionary = wrapper.collection();
        const Results& value_results = dictionary.get_values();

        // Return an ObservableCollectionWrapper as these are results!
        auto results_wrapper = new ObservableCollectionWrapper(value_results);
        return reinterpret_cast<jlong>(results_wrapper);
    }
    CATCH_STD()
    return reinterpret_cast<jlong>(nullptr);
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_OsMap_nativeFreeze(JNIEnv* env, jclass, jlong wrapper_ptr,
                                          jlong realm_ptr) {
    try {
        auto& wrapper = *reinterpret_cast<ObservableDictionaryWrapper*>(wrapper_ptr);
        auto& dictionary = wrapper.collection();
        std::shared_ptr<Realm>& shared_realm_ptr = *reinterpret_cast<std::shared_ptr<Realm>*>(realm_ptr);
        object_store::Dictionary frozen_dictionary = dictionary.freeze(shared_realm_ptr);
        return reinterpret_cast<jlong>(new ObservableDictionaryWrapper(frozen_dictionary));
    }
    CATCH_STD()
    return reinterpret_cast<jlong>(nullptr);
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_OsMap_nativeCreateAndPutEmbeddedObject(JNIEnv* env, jclass,
                                                              jlong shared_realm_ptr,
                                                              jlong wrapper_ptr,
                                                              jstring j_key) {
    try {
        auto& realm = *reinterpret_cast<SharedRealm*>(shared_realm_ptr);
        auto& wrapper = *reinterpret_cast<ObservableDictionaryWrapper*>(wrapper_ptr);
        auto& dictionary = wrapper.collection();
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

JNIEXPORT jobjectArray JNICALL
Java_io_realm_internal_OsMap_nativeGetEntryForModel(JNIEnv* env, jclass, jlong wrapper_ptr, jint j_pos) {
    try {
        auto& wrapper = *reinterpret_cast<ObservableDictionaryWrapper*>(wrapper_ptr);
        auto& dictionary = wrapper.collection();
        const std::pair<StringData, Mixed>& pair = dictionary.get_pair(j_pos);
        const StringData& key = pair.first;
        const Mixed& mixed = pair.second;

        jobjectArray pair_array = env->NewObjectArray(2, JavaClassGlobalDef::java_lang_object(), NULL);
        env->SetObjectArrayElement(pair_array, 0, to_jstring(env, key));
        if (mixed.is_null()) {
            env->SetObjectArrayElement(pair_array, 1, JavaClassGlobalDef::new_long(env, io_realm_internal_OsMap_NOT_FOUND));
        } else {
            env->SetObjectArrayElement(pair_array, 1, JavaClassGlobalDef::new_long(env, mixed.get<ObjKey>().value));
        }
        return pair_array;
    }
    CATCH_STD()
    return nullptr;
}

JNIEXPORT jobjectArray JNICALL
Java_io_realm_internal_OsMap_nativeGetEntryForMixed(JNIEnv* env, jclass, jlong wrapper_ptr, jint j_pos) {
    try {
        auto& wrapper = *reinterpret_cast<ObservableDictionaryWrapper*>(wrapper_ptr);
        auto& dictionary = wrapper.collection();
        const std::pair<StringData, Mixed>& pair = dictionary.get_pair(j_pos);
        const StringData& key = pair.first;
        const Mixed& mixed = pair.second;

        jlong mixed_ptr = reinterpret_cast<jlong>(new JavaValue(from_mixed(mixed)));
        jobjectArray pair_array = env->NewObjectArray(2, JavaClassGlobalDef::java_lang_object(), NULL);
        env->SetObjectArrayElement(pair_array, 0, to_jstring(env, key));
        env->SetObjectArrayElement(pair_array, 1, JavaClassGlobalDef::new_long(env, mixed_ptr));
        return pair_array;
    }
    CATCH_STD()
    return nullptr;
}

JNIEXPORT jobjectArray JNICALL
Java_io_realm_internal_OsMap_nativeGetEntryForPrimitive(JNIEnv* env, jclass, jlong wrapper_ptr,
                                                        jint j_pos) {
    try {
        auto& wrapper = *reinterpret_cast<ObservableDictionaryWrapper*>(wrapper_ptr);
        auto& dictionary = wrapper.collection();
        const std::pair<StringData, Mixed>& pair = dictionary.get_pair(j_pos);
        const StringData& key = pair.first;
        const Mixed& mixed = pair.second;

        jobjectArray pair_array = env->NewObjectArray(2, JavaClassGlobalDef::java_lang_object(), NULL);
        env->SetObjectArrayElement(pair_array, 0, to_jstring(env, key));

        if (mixed.is_null()) {
            env->SetObjectArrayElement(pair_array, 1, NULL);
        } else {
            const DataType& type = mixed.get_type();
            switch (type) {
                case DataType::Type::Int:
                    env->SetObjectArrayElement(pair_array, 1, JavaClassGlobalDef::new_long(env, mixed.get_int()));
                    break;
                case DataType::Type::Double:
                    env->SetObjectArrayElement(pair_array, 1, JavaClassGlobalDef::new_double(env, mixed.get_double()));
                    break;
                case DataType::Type::Bool:
                    env->SetObjectArrayElement(pair_array, 1, JavaClassGlobalDef::new_boolean(env, mixed.get_bool()));
                    break;
                case DataType::Type::String:
                    env->SetObjectArrayElement(pair_array, 1, to_jstring(env, mixed.get_string()));
                    break;
                case DataType::Type::Binary:
                    env->SetObjectArrayElement(pair_array, 1, JavaClassGlobalDef::new_byte_array(env, mixed.get_binary()));
                    break;
                case DataType::Type::Float:
                    env->SetObjectArrayElement(pair_array, 1, JavaClassGlobalDef::new_float(env, mixed.get_float()));
                    break;
                case DataType::Type::UUID:
                    env->SetObjectArrayElement(pair_array, 1, JavaClassGlobalDef::new_uuid(env, mixed.get_uuid()));
                    break;
                case DataType::Type::ObjectId:
                    env->SetObjectArrayElement(pair_array, 1, JavaClassGlobalDef::new_object_id(env, mixed.get_object_id()));
                    break;
                case DataType::Type::Timestamp:
                    env->SetObjectArrayElement(pair_array, 1, JavaClassGlobalDef::new_date(env, mixed.get_timestamp()));
                    break;
                case DataType::Type::Decimal:
                    env->SetObjectArrayElement(pair_array, 1, JavaClassGlobalDef::new_decimal128(env, mixed.get_decimal()));
                    break;
                default:
                    throw std::logic_error("'getEntryForPrimitive' method only suitable for int, double, boolean, String, byte[], float, UUID, Decimal128 and ObjectId.");
            }
        }
        return pair_array;
    }
    CATCH_STD()
    return nullptr;
}

JNIEXPORT jboolean JNICALL
Java_io_realm_internal_OsMap_nativeContainsNull(JNIEnv* env, jclass, jlong wrapper_ptr) {
    try {
        auto& wrapper = *reinterpret_cast<ObservableDictionaryWrapper*>(wrapper_ptr);
        auto& dictionary = wrapper.collection();
        size_t find_result = dictionary.find_any(Mixed());
        if (find_result != realm::not_found) {
            return true;
        }
    }
    CATCH_STD()
    return false;
}

JNIEXPORT jboolean JNICALL
Java_io_realm_internal_OsMap_nativeContainsLong(JNIEnv* env, jclass, jlong wrapper_ptr,
                                                jlong j_value) {
    try {
        auto& wrapper = *reinterpret_cast<ObservableDictionaryWrapper*>(wrapper_ptr);
        auto& dictionary = wrapper.collection();
        size_t find_result = dictionary.find_any(Mixed(j_value));
        if (find_result != realm::not_found) {
            return true;
        }
    }
    CATCH_STD()
    return false;
}

JNIEXPORT jboolean JNICALL
Java_io_realm_internal_OsMap_nativeContainsBoolean(JNIEnv* env, jclass, jlong wrapper_ptr,
                                                   jboolean j_value) {
    try {
        auto& wrapper = *reinterpret_cast<ObservableDictionaryWrapper*>(wrapper_ptr);
        auto& dictionary = wrapper.collection();
        size_t find_result = dictionary.find_any(Mixed(bool(j_value)));
        if (find_result != realm::not_found) {
            return true;
        }
    }
    CATCH_STD()
    return false;
}

JNIEXPORT jboolean JNICALL
Java_io_realm_internal_OsMap_nativeContainsString(JNIEnv* env, jclass, jlong wrapper_ptr,
                                                  jstring j_value) {
    try {
        auto& wrapper = *reinterpret_cast<ObservableDictionaryWrapper*>(wrapper_ptr);
        auto& dictionary = wrapper.collection();
        JStringAccessor key(env, j_value);
        size_t find_result = dictionary.find_any(Mixed(StringData(key)));
        if (find_result != realm::not_found) {
            return true;
        }
    }
    CATCH_STD()
    return false;
}

JNIEXPORT jboolean JNICALL
Java_io_realm_internal_OsMap_nativeContainsBinary(JNIEnv* env, jclass, jlong wrapper_ptr,
                                                  jbyteArray j_value) {
    try {
        auto& wrapper = *reinterpret_cast<ObservableDictionaryWrapper*>(wrapper_ptr);
        auto& dictionary = wrapper.collection();
        const OwnedBinaryData& data = OwnedBinaryData(JByteArrayAccessor(env, j_value).transform<BinaryData>());
        size_t find_result = dictionary.find_any(Mixed(data.get()));
        if (find_result != realm::not_found) {
            return true;
        }
    }
    CATCH_STD()
    return false;
}

JNIEXPORT jboolean JNICALL
Java_io_realm_internal_OsMap_nativeContainsFloat(JNIEnv* env, jclass, jlong wrapper_ptr,
                                                 jfloat j_value) {
    try {
        auto& wrapper = *reinterpret_cast<ObservableDictionaryWrapper*>(wrapper_ptr);
        auto& dictionary = wrapper.collection();
        size_t find_result = dictionary.find_any(Mixed(j_value));
        if (find_result != realm::not_found) {
            return true;
        }
    }
    CATCH_STD()
    return false;
}

JNIEXPORT jboolean JNICALL
Java_io_realm_internal_OsMap_nativeContainsObjectId(JNIEnv* env, jclass, jlong wrapper_ptr,
                                                    jstring j_value) {
    try {
        auto& wrapper = *reinterpret_cast<ObservableDictionaryWrapper*>(wrapper_ptr);
        auto& dictionary = wrapper.collection();
        JStringAccessor data(env, j_value);
        const ObjectId object_id = ObjectId(StringData(data).data());
        size_t find_result = dictionary.find_any(Mixed(object_id));
        if (find_result != realm::not_found) {
            return true;
        }
    }
    CATCH_STD()
    return false;
}

JNIEXPORT jboolean JNICALL
Java_io_realm_internal_OsMap_nativeContainsUUID(JNIEnv* env, jclass, jlong wrapper_ptr,
                                                jstring j_value) {
    try {
        auto& wrapper = *reinterpret_cast<ObservableDictionaryWrapper*>(wrapper_ptr);
        auto& dictionary = wrapper.collection();
        JStringAccessor value(env, j_value);
        const UUID& uuid = UUID(StringData(value).data());
        size_t find_result = dictionary.find_any(Mixed(uuid));
        if (find_result != realm::not_found) {
            return true;
        }
    }
    CATCH_STD()
    return false;
}

JNIEXPORT jboolean JNICALL
Java_io_realm_internal_OsMap_nativeContainsDate(JNIEnv* env, jclass, jlong wrapper_ptr,
                                                jlong j_value) {
    try {
        auto& wrapper = *reinterpret_cast<ObservableDictionaryWrapper*>(wrapper_ptr);
        auto& dictionary = wrapper.collection();
        realm::Timestamp timestamp = from_milliseconds(j_value);
        size_t find_result = dictionary.find_any(Mixed(timestamp));
        if (find_result != realm::not_found) {
            return true;
        }
    }
    CATCH_STD()
    return false;
}

JNIEXPORT jboolean JNICALL
Java_io_realm_internal_OsMap_nativeContainsDecimal128(JNIEnv* env, jclass, jlong wrapper_ptr,
                                                      jlong j_high_value, jlong j_low_value) {
    try {
        auto& wrapper = *reinterpret_cast<ObservableDictionaryWrapper*>(wrapper_ptr);
        auto& dictionary = wrapper.collection();
        Decimal128::Bid128 raw {static_cast<uint64_t>(j_low_value), static_cast<uint64_t>(j_high_value)};
        auto decimal128 = Decimal128(raw);
        size_t find_result = dictionary.find_any(Mixed(decimal128));
        if (find_result != realm::not_found) {
            return true;
        }
    }
    CATCH_STD()
    return false;
}

JNIEXPORT jboolean JNICALL
Java_io_realm_internal_OsMap_nativeContainsMixed(JNIEnv* env, jclass, jlong wrapper_ptr,
                                                 jlong mixed_ptr) {
    try {
        auto& wrapper = *reinterpret_cast<ObservableDictionaryWrapper*>(wrapper_ptr);
        auto& dictionary = wrapper.collection();
        auto mixed_java_value = *reinterpret_cast<JavaValue*>(mixed_ptr);
        const Mixed& mixed = mixed_java_value.to_mixed();
        size_t find_result = dictionary.find_any(mixed);
        if (find_result != realm::not_found) {
            return true;
        }
    }
    CATCH_STD();
    return false;
}

JNIEXPORT jboolean JNICALL
Java_io_realm_internal_OsMap_nativeContainsRealmModel(JNIEnv* env, jclass, jlong wrapper_ptr,
                                                      jlong j_obj_key, jlong j_table_ptr) {
    try {
        auto& wrapper = *reinterpret_cast<ObservableDictionaryWrapper*>(wrapper_ptr);
        auto& dictionary = wrapper.collection();

        TableRef target_table = TBL_REF(j_table_ptr);
        ObjKey object_key(j_obj_key);
        ObjLink object_link(target_table->get_key(), object_key);

        const Mixed& mixed = Mixed(object_link);
        size_t find_result = dictionary.find_any(mixed);
        if (find_result != realm::not_found) {
            return true;
        }
    }
    CATCH_STD();
    return false;
}

JNIEXPORT void JNICALL
Java_io_realm_internal_OsMap_nativeStartListening(JNIEnv* env, jclass, jlong wrapper_ptr,
                                                  jobject j_observable_map) {
    try {
        auto wrapper = reinterpret_cast<ObservableDictionaryWrapper*>(wrapper_ptr);
        wrapper->start_listening(env, j_observable_map);
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL
Java_io_realm_internal_OsMap_nativeStopListening(JNIEnv* env, jclass, jlong wrapper_ptr) {
    try {
        auto wrapper = reinterpret_cast<ObservableDictionaryWrapper*>(wrapper_ptr);
        wrapper->stop_listening();
    }
    CATCH_STD()
}
