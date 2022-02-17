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

#include "io_realm_internal_OsSet.h"

#include <algorithm>    // std::set_intersection, std::sort
#include <vector>       // std::vector
#include <set>          // std::set
#include <realm/object-store/set.hpp>
#include <realm/object-store/shared_realm.hpp>

#include "java_accessor.hpp"
#include "java_object_accessor.hpp"
#include "java_exception_def.hpp"
#include "jni_util/java_exception_thrower.hpp"
#include "observable_collection_wrapper.hpp"
#include "util.hpp"

using namespace realm;
using namespace realm::util;
using namespace realm::object_store;
using namespace realm::_impl;

typedef ObservableCollectionWrapper<object_store::Set> SetWrapper;

void finalize_set(jlong ptr) {
    delete reinterpret_cast<SetWrapper*>(ptr);
}

inline bool isSetNullable(JNIEnv *env, realm::object_store::Set &set) {
    if (is_nullable(set.get_type())) {
        return true;
    }

    THROW_JAVA_EXCEPTION(env, JavaExceptionDef::NullPointerException,
                         "This 'RealmSet' is not nullable. A non-null value is expected.");
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_OsSet_nativeGetFinalizerPtr(JNIEnv*, jclass) {
    return reinterpret_cast<jlong>(&finalize_set);
}

JNIEXPORT jlongArray JNICALL
Java_io_realm_internal_OsSet_nativeCreate(JNIEnv* env, jclass, jlong shared_realm_ptr,
                                          jlong obj_ptr, jlong column_key) {
    try {
        auto obj = *reinterpret_cast<realm::Obj*>(obj_ptr);
        auto shared_realm = *reinterpret_cast<SharedRealm*>(shared_realm_ptr);

        // Return an array of pointers: first to the wrapper, second to the table (if applicable)
        jlong ret[2];

        // Get set, put it in the wrapper and save pointer to be returned
        object_store::Set set(shared_realm, obj, ColKey(column_key));
        auto wrapper_ptr = new SetWrapper(set, "io/realm/internal/ObservableSet");
        ret[0] = reinterpret_cast<jlong>(wrapper_ptr);

        // Special case for objects: return the table. Ignore for other types
        if (wrapper_ptr->collection().get_type() == PropertyType::Object) {
            auto set_view_ref = obj.get_linkset(ColKey(column_key));

            auto target_table_ptr = new TableRef(set_view_ref.get_target_table());
            ret[1] = reinterpret_cast<jlong>(target_table_ptr);
        } else {
            ret[1] = reinterpret_cast<jlong>(nullptr);
        }

        jlongArray ret_array = env->NewLongArray(2);
        if (!ret_array) {
            ThrowException(env, OutOfMemory, "Could not allocate memory to create OsSet.");
            return nullptr;
        }

        env->SetLongArrayRegion(ret_array, 0, 2, ret);
        return ret_array;
    }
    CATCH_STD()
    return nullptr;
}

JNIEXPORT jboolean JNICALL
Java_io_realm_internal_OsSet_nativeIsValid(JNIEnv* env, jclass, jlong wrapper_ptr) {
    try {
        auto& wrapper = *reinterpret_cast<SetWrapper*>(wrapper_ptr);
        object_store::Set& set = wrapper.collection();
        return set.is_valid();
    }
    CATCH_STD()
    return false;
}

JNIEXPORT void JNICALL
Java_io_realm_internal_OsSet_nativeDeleteAll(JNIEnv* env, jclass, jlong wrapper_ptr) {
    try {
        auto& wrapper = *reinterpret_cast<SetWrapper*>(wrapper_ptr);
        wrapper.collection().delete_all();
    }
    CATCH_STD()
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_OsSet_nativeGetQuery(JNIEnv* env, jclass, jlong wrapper_ptr)
{
    try {
        auto& wrapper = *reinterpret_cast<SetWrapper*>(wrapper_ptr);
        auto query = wrapper.collection().get_query();
        query.set_ordering(std::make_unique<DescriptorOrdering>());
        return reinterpret_cast<jlong>(new Query(std::move(query)));
    }
    CATCH_STD()
    return reinterpret_cast<jlong>(nullptr);
}

JNIEXPORT jobject JNICALL
Java_io_realm_internal_OsSet_nativeGetValueAtIndex(JNIEnv* env, jclass, jlong wrapper_ptr, jint position) {
    try {
        auto& wrapper = *reinterpret_cast<SetWrapper*>(wrapper_ptr);
        object_store::Set& set = wrapper.collection();
        const Mixed& value = set.get_any(position);
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
    CATCH_STD()
    return nullptr;
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_OsSet_nativeSize(JNIEnv* env, jclass, jlong wrapper_ptr) {
    try {
        auto& wrapper = *reinterpret_cast<SetWrapper*>(wrapper_ptr);
        object_store::Set& set = wrapper.collection();
        return set.size();
    }
    CATCH_STD()
    return 0;
}

JNIEXPORT jboolean JNICALL
Java_io_realm_internal_OsSet_nativeContainsNull(JNIEnv *env, jclass, jlong wrapper_ptr) {
    try {
        auto& wrapper = *reinterpret_cast<SetWrapper*>(wrapper_ptr);
        object_store::Set& set = wrapper.collection();

        if (isSetNullable(env, set)) {
            size_t found = set.find_any(Mixed());
            return found != npos;       // npos represents "not found"
        }
    }
    CATCH_STD()
    return reinterpret_cast<jlong>(nullptr);
}

JNIEXPORT jboolean JNICALL
Java_io_realm_internal_OsSet_nativeContainsBoolean(JNIEnv* env, jclass, jlong wrapper_ptr,
                                                   jboolean j_value) {
    try {
        auto& wrapper = *reinterpret_cast<SetWrapper*>(wrapper_ptr);
        object_store::Set& set = wrapper.collection();
        size_t found = set.find_any(Mixed(bool(j_value)));
        return found != npos;       // npos represents "not found"
    }
    CATCH_STD()
    return reinterpret_cast<jlong>(nullptr);
}

JNIEXPORT jboolean JNICALL
Java_io_realm_internal_OsSet_nativeContainsString(JNIEnv* env, jclass, jlong wrapper_ptr,
                                                  jstring j_value) {
    try {
        auto& wrapper = *reinterpret_cast<SetWrapper*>(wrapper_ptr);
        object_store::Set& set = wrapper.collection();
        JStringAccessor value(env, j_value);
        size_t found = set.find_any(Mixed(StringData(value)));
        return found != npos;       // npos represents "not found"
    }
    CATCH_STD()
    return reinterpret_cast<jlong>(nullptr);
}

JNIEXPORT jboolean JNICALL
Java_io_realm_internal_OsSet_nativeContainsLong(JNIEnv* env, jclass, jlong wrapper_ptr,
                                                jlong j_value) {
    try {
        auto& wrapper = *reinterpret_cast<SetWrapper*>(wrapper_ptr);
        object_store::Set& set = wrapper.collection();
        size_t found = set.find_any(Mixed(j_value));
        return found != npos;       // npos represents "not found"
    }
    CATCH_STD()
    return reinterpret_cast<jlong>(nullptr);
}

JNIEXPORT jboolean JNICALL
Java_io_realm_internal_OsSet_nativeContainsFloat(JNIEnv* env, jclass, jlong wrapper_ptr,
                                                 jfloat j_value) {
    try {
        auto& wrapper = *reinterpret_cast<SetWrapper*>(wrapper_ptr);
        object_store::Set& set = wrapper.collection();
        size_t found = set.find_any(Mixed(j_value));
        return found != npos;       // npos represents "not found"
    }
    CATCH_STD()
    return reinterpret_cast<jlong>(nullptr);
}

JNIEXPORT jboolean JNICALL
Java_io_realm_internal_OsSet_nativeContainsDouble(JNIEnv* env, jclass, jlong wrapper_ptr,
                                                  jdouble j_value) {
    try {
        auto& wrapper = *reinterpret_cast<SetWrapper*>(wrapper_ptr);
        object_store::Set& set = wrapper.collection();
        size_t found = set.find_any(Mixed(j_value));
        return found != npos;       // npos represents "not found"
    }
    CATCH_STD()
    return reinterpret_cast<jlong>(nullptr);
}

JNIEXPORT jboolean JNICALL
Java_io_realm_internal_OsSet_nativeContainsBinary(JNIEnv* env, jclass, jlong wrapper_ptr,
                                                  jbyteArray j_value) {
    try {
        auto& wrapper = *reinterpret_cast<SetWrapper*>(wrapper_ptr);
        object_store::Set& set = wrapper.collection();
        const OwnedBinaryData& data = OwnedBinaryData(JByteArrayAccessor(env, j_value).transform<BinaryData>());
        size_t found = set.find_any(Mixed(data.get()));
        return found != npos;       // npos represents "not found"
    }
    CATCH_STD()
    return reinterpret_cast<jlong>(nullptr);
}

JNIEXPORT jboolean JNICALL
Java_io_realm_internal_OsSet_nativeContainsDate(JNIEnv* env, jclass, jlong wrapper_ptr,
                                                jlong j_value) {
    try {
        auto& wrapper = *reinterpret_cast<SetWrapper*>(wrapper_ptr);
        object_store::Set& set = wrapper.collection();
        realm::Timestamp timestamp = from_milliseconds(j_value);
        size_t found = set.find_any(Mixed(timestamp));
        return found != npos;       // npos represents "not found"
    }
    CATCH_STD()
    return reinterpret_cast<jlong>(nullptr);
}

JNIEXPORT jboolean JNICALL
Java_io_realm_internal_OsSet_nativeContainsDecimal128(JNIEnv* env, jclass, jlong wrapper_ptr,
                                                      jlong j_low_value, jlong j_high_value) {
    try {
        auto& wrapper = *reinterpret_cast<SetWrapper*>(wrapper_ptr);
        object_store::Set& set = wrapper.collection();
        Decimal128::Bid128 raw {static_cast<uint64_t>(j_low_value), static_cast<uint64_t>(j_high_value)};
        Decimal128 decimal128 = Decimal128(raw);
        size_t found = set.find_any(Mixed(decimal128));
        return found != npos;       // npos represents "not found"
    }
    CATCH_STD()
    return reinterpret_cast<jlong>(nullptr);
}

JNIEXPORT jboolean JNICALL
Java_io_realm_internal_OsSet_nativeContainsObjectId(JNIEnv* env, jclass, jlong wrapper_ptr,
                                                    jstring j_value) {
    try {
        auto& wrapper = *reinterpret_cast<SetWrapper*>(wrapper_ptr);
        object_store::Set& set = wrapper.collection();
        JStringAccessor data(env, j_value);
        const ObjectId object_id = ObjectId(StringData(data).data());
        size_t found = set.find_any(Mixed(object_id));
        return found != npos;       // npos represents "not found"
    }
    CATCH_STD()
    return reinterpret_cast<jlong>(nullptr);
}

JNIEXPORT jboolean JNICALL
Java_io_realm_internal_OsSet_nativeContainsUUID(JNIEnv* env, jclass, jlong wrapper_ptr,
                                                jstring j_value) {
    try {
        auto& wrapper = *reinterpret_cast<SetWrapper*>(wrapper_ptr);
        object_store::Set& set = wrapper.collection();
        JStringAccessor value(env, j_value);
        const UUID& uuid = UUID(StringData(value).data());
        size_t found = set.find_any(Mixed(uuid));
        return found != npos;       // npos represents "not found"
    }
    CATCH_STD()
    return reinterpret_cast<jlong>(nullptr);
}

JNIEXPORT jboolean JNICALL
Java_io_realm_internal_OsSet_nativeContainsRow(JNIEnv* env, jclass, jlong wrapper_ptr,
                                               jlong j_obj_key) {
    try {
        auto& wrapper = *reinterpret_cast<SetWrapper*>(wrapper_ptr);
        object_store::Set& set = wrapper.collection();
        ObjKey object_key(j_obj_key);
        size_t found = set.find_any(object_key);
        return found != npos;       // npos represents "not found"
    }
    CATCH_STD()
    return reinterpret_cast<jlong>(nullptr);
}

JNIEXPORT jboolean JNICALL
Java_io_realm_internal_OsSet_nativeContainsRealmAny(JNIEnv* env, jclass, jlong wrapper_ptr,
                                                 jlong mixed_ptr) {
    try {
        auto& wrapper = *reinterpret_cast<SetWrapper*>(wrapper_ptr);
        object_store::Set& set = wrapper.collection();
        auto& java_value = *reinterpret_cast<JavaValue*>(mixed_ptr);
        size_t found = set.find_any(java_value.to_mixed());
        return found != npos;       // npos represents "not found"
    }
    CATCH_STD()
    return reinterpret_cast<jlong>(nullptr);
}

JNIEXPORT jlongArray JNICALL
Java_io_realm_internal_OsSet_nativeAddNull(JNIEnv* env, jclass, jlong wrapper_ptr) {
    try {
        auto& wrapper = *reinterpret_cast<SetWrapper*>(wrapper_ptr);
        object_store::Set& set = wrapper.collection();
        JavaAccessorContext context(env);

        if (isSetNullable(env, set)) {
            const std::pair<size_t, bool> &add_pair = set.insert(context, Any());
            jlong ret[2];
            ret[0] = add_pair.first;    // index
            ret[1] = add_pair.second;   // found (or not)
            jlongArray ret_array = env->NewLongArray(2);
            env->SetLongArrayRegion(ret_array, 0, 2, ret);
            return ret_array;
        }
    }
    CATCH_STD()
    return nullptr;
}

JNIEXPORT jlongArray JNICALL
Java_io_realm_internal_OsSet_nativeAddBoolean(JNIEnv* env, jclass, jlong wrapper_ptr,
                                              jboolean j_value) {
    try {
        auto& wrapper = *reinterpret_cast<SetWrapper*>(wrapper_ptr);
        object_store::Set& set = wrapper.collection();
        JavaAccessorContext context(env);

        // TODO: abstract this call so that the rest is the same for all types
        const std::pair<size_t, bool>& add_pair = set.insert(context, Any(j_value));

        jlong ret[2];
        ret[0] = add_pair.first;    // index
        ret[1] = add_pair.second;   // found (or not)
        jlongArray ret_array = env->NewLongArray(2);
        env->SetLongArrayRegion(ret_array, 0, 2, ret);
        return ret_array;
    }
    CATCH_STD()
    return nullptr;
}

JNIEXPORT jlongArray JNICALL
Java_io_realm_internal_OsSet_nativeAddString(JNIEnv* env, jclass, jlong wrapper_ptr, jstring j_value) {
    try {
        auto& wrapper = *reinterpret_cast<SetWrapper*>(wrapper_ptr);
        object_store::Set& set = wrapper.collection();
        JStringAccessor value(env, j_value);
        JavaAccessorContext context(env);

        // TODO: abstract this call so that the rest is the same for all types
        const std::pair<size_t, bool>& add_pair = set.insert(context, Any(value));

        jlong ret[2];
        ret[0] = add_pair.first;    // index
        ret[1] = add_pair.second;   // found (or not)
        jlongArray ret_array = env->NewLongArray(2);
        env->SetLongArrayRegion(ret_array, 0, 2, ret);
        return ret_array;
    }
    CATCH_STD()
    return nullptr;
}

JNIEXPORT jlongArray JNICALL
Java_io_realm_internal_OsSet_nativeAddLong(JNIEnv* env, jclass, jlong wrapper_ptr, jlong j_value) {
    try {
        auto& wrapper = *reinterpret_cast<SetWrapper*>(wrapper_ptr);
        object_store::Set& set = wrapper.collection();
        JavaAccessorContext context(env);

        // TODO: abstract this call so that the rest is the same for all types
        const std::pair<size_t, bool>& add_pair = set.insert(context, Any(j_value));

        jlong ret[2];
        ret[0] = add_pair.first;    // index
        ret[1] = add_pair.second;   // found (or not)
        jlongArray ret_array = env->NewLongArray(2);
        env->SetLongArrayRegion(ret_array, 0, 2, ret);
        return ret_array;
    }
    CATCH_STD()
    return nullptr;
}

JNIEXPORT jlongArray JNICALL
Java_io_realm_internal_OsSet_nativeAddFloat(JNIEnv* env, jclass, jlong wrapper_ptr, jfloat j_value) {
    try {
        auto& wrapper = *reinterpret_cast<SetWrapper*>(wrapper_ptr);
        object_store::Set& set = wrapper.collection();
        JavaAccessorContext context(env);

        // TODO: abstract this call so that the rest is the same for all types
        const std::pair<size_t, bool>& add_pair = set.insert(context, Any(j_value));

        jlong ret[2];
        ret[0] = add_pair.first;    // index
        ret[1] = add_pair.second;   // found (or not)
        jlongArray ret_array = env->NewLongArray(2);
        env->SetLongArrayRegion(ret_array, 0, 2, ret);
        return ret_array;
    }
    CATCH_STD()
    return nullptr;
}

JNIEXPORT jlongArray JNICALL
Java_io_realm_internal_OsSet_nativeAddDouble(JNIEnv* env, jclass, jlong wrapper_ptr,
                                             jdouble j_value) {
    try {
        auto& wrapper = *reinterpret_cast<SetWrapper*>(wrapper_ptr);
        object_store::Set& set = wrapper.collection();
        JavaAccessorContext context(env);

        // TODO: abstract this call so that the rest is the same for all types
        const std::pair<size_t, bool>& add_pair = set.insert(context, Any(j_value));

        jlong ret[2];
        ret[0] = add_pair.first;    // index
        ret[1] = add_pair.second;   // found (or not)
        jlongArray ret_array = env->NewLongArray(2);
        env->SetLongArrayRegion(ret_array, 0, 2, ret);
        return ret_array;
    }
    CATCH_STD()
    return nullptr;
}

JNIEXPORT jlongArray JNICALL
Java_io_realm_internal_OsSet_nativeAddBinary(JNIEnv* env, jclass, jlong wrapper_ptr,
                                             jbyteArray j_value) {
    try {
        auto& wrapper = *reinterpret_cast<SetWrapper*>(wrapper_ptr);
        object_store::Set& set = wrapper.collection();
        JavaAccessorContext context(env);
        JByteArrayAccessor data(env, j_value);

        // TODO: abstract this call so that the rest is the same for all types
        const std::pair<size_t, bool>& add_pair = set.insert(context, Any(data));

        jlong ret[2];
        ret[0] = add_pair.first;    // index
        ret[1] = add_pair.second;   // found (or not)
        jlongArray ret_array = env->NewLongArray(2);
        env->SetLongArrayRegion(ret_array, 0, 2, ret);
        return ret_array;
    }
    CATCH_STD()
    return nullptr;
}

JNIEXPORT jlongArray JNICALL
Java_io_realm_internal_OsSet_nativeAddDate(JNIEnv* env, jclass, jlong wrapper_ptr,
                                           jlong j_value) {
    try {
        auto& wrapper = *reinterpret_cast<SetWrapper*>(wrapper_ptr);
        object_store::Set& set = wrapper.collection();
        JavaAccessorContext context(env);

        // TODO: abstract this call so that the rest is the same for all types
        const std::pair<size_t, bool>& add_pair = set.insert(context, Any(j_value));

        jlong ret[2];
        ret[0] = add_pair.first;    // index
        ret[1] = add_pair.second;   // found (or not)
        jlongArray ret_array = env->NewLongArray(2);
        env->SetLongArrayRegion(ret_array, 0, 2, ret);
        return ret_array;
    }
    CATCH_STD()
    return nullptr;
}

JNIEXPORT jlongArray JNICALL
Java_io_realm_internal_OsSet_nativeAddDecimal128(JNIEnv* env, jclass, jlong wrapper_ptr,
                                                 jlong j_low_value, jlong j_high_value) {
    try {
        auto& wrapper = *reinterpret_cast<SetWrapper*>(wrapper_ptr);
        object_store::Set& set = wrapper.collection();
        JavaAccessorContext context(env);
        Decimal128::Bid128 raw {static_cast<uint64_t>(j_low_value), static_cast<uint64_t>(j_high_value)};
        Decimal128 decimal128 = Decimal128(raw);

        // TODO: abstract this call so that the rest is the same for all types
        const std::pair<size_t, bool>& add_pair = set.insert(context, Any(decimal128));

        jlong ret[2];
        ret[0] = add_pair.first;    // index
        ret[1] = add_pair.second;   // found (or not)
        jlongArray ret_array = env->NewLongArray(2);
        env->SetLongArrayRegion(ret_array, 0, 2, ret);
        return ret_array;
    }
    CATCH_STD()
    return nullptr;
}

JNIEXPORT jlongArray JNICALL
Java_io_realm_internal_OsSet_nativeAddObjectId(JNIEnv* env, jclass, jlong wrapper_ptr,
                                               jstring j_value) {
    try {
        auto& wrapper = *reinterpret_cast<SetWrapper*>(wrapper_ptr);
        object_store::Set& set = wrapper.collection();
        JavaAccessorContext context(env);
        JStringAccessor data(env, j_value);
        const ObjectId object_id = ObjectId(StringData(data).data());

        // TODO: abstract this call so that the rest is the same for all types
        const std::pair<size_t, bool>& add_pair = set.insert(context, Any(object_id));

        jlong ret[2];
        ret[0] = add_pair.first;    // index
        ret[1] = add_pair.second;   // found (or not)
        jlongArray ret_array = env->NewLongArray(2);
        env->SetLongArrayRegion(ret_array, 0, 2, ret);
        return ret_array;
    }
    CATCH_STD()
    return nullptr;
}

JNIEXPORT jlongArray JNICALL
Java_io_realm_internal_OsSet_nativeAddUUID(JNIEnv* env, jclass, jlong wrapper_ptr,
                                           jstring j_value) {
    try {
        auto& wrapper = *reinterpret_cast<SetWrapper*>(wrapper_ptr);
        object_store::Set& set = wrapper.collection();
        JavaAccessorContext context(env);
        JStringAccessor value(env, j_value);
        const UUID& uuid = UUID(StringData(value).data());

        // TODO: abstract this call so that the rest is the same for all types
        const std::pair<size_t, bool>& add_pair = set.insert(context, Any(uuid));

        jlong ret[2];
        ret[0] = add_pair.first;    // index
        ret[1] = add_pair.second;   // found (or not)
        jlongArray ret_array = env->NewLongArray(2);
        env->SetLongArrayRegion(ret_array, 0, 2, ret);
        return ret_array;
    }
    CATCH_STD()
    return nullptr;
}

JNIEXPORT jlongArray JNICALL
Java_io_realm_internal_OsSet_nativeAddRow(JNIEnv* env, jclass, jlong wrapper_ptr,
                                          jlong j_obj_key) {
    try {
        auto& wrapper = *reinterpret_cast<SetWrapper*>(wrapper_ptr);
        object_store::Set& set = wrapper.collection();
        ObjKey object_key(j_obj_key);

        // TODO: abstract this call so that the rest is the same for all types
        const std::pair<size_t, bool>& add_pair = set.insert(object_key);

        jlong ret[2];
        ret[0] = add_pair.first;    // index
        ret[1] = add_pair.second;   // found (or not)
        jlongArray ret_array = env->NewLongArray(2);
        env->SetLongArrayRegion(ret_array, 0, 2, ret);
        return ret_array;
    }
    CATCH_STD()
    return nullptr;
}



JNIEXPORT jlongArray JNICALL
Java_io_realm_internal_OsSet_nativeAddRealmAny(JNIEnv* env, jclass, jlong wrapper_ptr,
                                            jlong mixed_ptr) {
    try {
        auto& wrapper = *reinterpret_cast<SetWrapper*>(wrapper_ptr);
        object_store::Set& set = wrapper.collection();
        auto& java_value = *reinterpret_cast<JavaValue*>(mixed_ptr);

        // TODO: abstract this call so that the rest is the same for all types
        const std::pair<size_t, bool>& add_pair = set.insert(java_value.to_mixed());

        jlong ret[2];
        ret[0] = add_pair.first;    // index
        ret[1] = add_pair.second;   // found (or not)
        jlongArray ret_array = env->NewLongArray(2);
        env->SetLongArrayRegion(ret_array, 0, 2, ret);
        return ret_array;
    }
    CATCH_STD()
    return nullptr;
}

JNIEXPORT jlongArray JNICALL
Java_io_realm_internal_OsSet_nativeRemoveNull(JNIEnv* env, jclass, jlong wrapper_ptr) {
    try {
        auto& wrapper = *reinterpret_cast<SetWrapper*>(wrapper_ptr);
        object_store::Set& set = wrapper.collection();

        if (isSetNullable(env, set)) {
            const std::pair<size_t, bool> &remove_pair = set.remove_any(Mixed());
            jlong ret[2];
            ret[0] = remove_pair.first;     // index
            ret[1] = remove_pair.second;    // found (or not)
            jlongArray ret_array = env->NewLongArray(2);
            env->SetLongArrayRegion(ret_array, 0, 2, ret);
            return ret_array;
        }
    }
    CATCH_STD()
    return nullptr;
}

JNIEXPORT jlongArray JNICALL
Java_io_realm_internal_OsSet_nativeRemoveBoolean(JNIEnv* env, jclass, jlong wrapper_ptr,
                                                 jboolean j_value) {
    try {
        auto& wrapper = *reinterpret_cast<SetWrapper*>(wrapper_ptr);
        object_store::Set& set = wrapper.collection();

        // TODO: abstract this call so that the rest is the same for all types
        const std::pair<size_t, bool>& remove_pair = set.remove_any(Mixed(bool(j_value)));

        jlong ret[2];
        ret[0] = remove_pair.first;     // index
        ret[1] = remove_pair.second;    // found (or not)
        jlongArray ret_array = env->NewLongArray(2);
        env->SetLongArrayRegion(ret_array, 0, 2, ret);
        return ret_array;
    }
    CATCH_STD()
    return nullptr;
}

JNIEXPORT jlongArray JNICALL
Java_io_realm_internal_OsSet_nativeRemoveString(JNIEnv* env, jclass, jlong wrapper_ptr, jstring j_value) {
    try {
        auto& wrapper = *reinterpret_cast<SetWrapper*>(wrapper_ptr);
        object_store::Set& set = wrapper.collection();
        JStringAccessor value(env, j_value);

        // TODO: abstract this call so that the rest is the same for all types
        const std::pair<size_t, bool>& remove_pair = set.remove_any(Mixed(StringData(value)));

        jlong ret[2];
        ret[0] = remove_pair.first;     // index
        ret[1] = remove_pair.second;    // found (or not)
        jlongArray ret_array = env->NewLongArray(2);
        env->SetLongArrayRegion(ret_array, 0, 2, ret);
        return ret_array;
    }
    CATCH_STD()
    return nullptr;
}

JNIEXPORT jlongArray JNICALL
Java_io_realm_internal_OsSet_nativeRemoveLong(JNIEnv* env, jclass, jlong wrapper_ptr,
                                              jlong j_value) {
    try {
        auto& wrapper = *reinterpret_cast<SetWrapper*>(wrapper_ptr);
        object_store::Set& set = wrapper.collection();

        // TODO: abstract this call so that the rest is the same for all types
        const std::pair<size_t, bool>& remove_pair = set.remove_any(Mixed(j_value));

        jlong ret[2];
        ret[0] = remove_pair.first;     // index
        ret[1] = remove_pair.second;    // found (or not)
        jlongArray ret_array = env->NewLongArray(2);
        env->SetLongArrayRegion(ret_array, 0, 2, ret);
        return ret_array;
    }
    CATCH_STD()
    return nullptr;
}

JNIEXPORT jlongArray JNICALL
Java_io_realm_internal_OsSet_nativeRemoveFloat(JNIEnv* env, jclass, jlong wrapper_ptr,
                                               jfloat j_value) {
    try {
        auto& wrapper = *reinterpret_cast<SetWrapper*>(wrapper_ptr);
        object_store::Set& set = wrapper.collection();

        // TODO: abstract this call so that the rest is the same for all types
        const std::pair<size_t, bool>& remove_pair = set.remove_any(Mixed(j_value));

        jlong ret[2];
        ret[0] = remove_pair.first;     // index
        ret[1] = remove_pair.second;    // found (or not)
        jlongArray ret_array = env->NewLongArray(2);
        env->SetLongArrayRegion(ret_array, 0, 2, ret);
        return ret_array;
    }
    CATCH_STD()
    return nullptr;
}

JNIEXPORT jlongArray JNICALL
Java_io_realm_internal_OsSet_nativeRemoveDouble(JNIEnv* env, jclass, jlong wrapper_ptr,
                                                jdouble j_value) {
    try {
        auto& wrapper = *reinterpret_cast<SetWrapper*>(wrapper_ptr);
        object_store::Set& set = wrapper.collection();

        // TODO: abstract this call so that the rest is the same for all types
        const std::pair<size_t, bool>& remove_pair = set.remove_any(Mixed(j_value));

        jlong ret[2];
        ret[0] = remove_pair.first;     // index
        ret[1] = remove_pair.second;    // found (or not)
        jlongArray ret_array = env->NewLongArray(2);
        env->SetLongArrayRegion(ret_array, 0, 2, ret);
        return ret_array;
    }
    CATCH_STD()
    return nullptr;
}

JNIEXPORT jlongArray JNICALL
Java_io_realm_internal_OsSet_nativeRemoveBinary(JNIEnv* env, jclass, jlong wrapper_ptr,
                                                jbyteArray j_value) {
    try {
        auto& wrapper = *reinterpret_cast<SetWrapper*>(wrapper_ptr);
        object_store::Set& set = wrapper.collection();
        const OwnedBinaryData& data = OwnedBinaryData(JByteArrayAccessor(env, j_value).transform<BinaryData>());

        // TODO: abstract this call so that the rest is the same for all types
        const std::pair<size_t, bool>& remove_pair = set.remove_any(Mixed(data.get()));

        jlong ret[2];
        ret[0] = remove_pair.first;     // index
        ret[1] = remove_pair.second;    // found (or not)
        jlongArray ret_array = env->NewLongArray(2);
        env->SetLongArrayRegion(ret_array, 0, 2, ret);
        return ret_array;
    }
    CATCH_STD()
    return nullptr;
}

JNIEXPORT jlongArray JNICALL
Java_io_realm_internal_OsSet_nativeRemoveDate(JNIEnv* env, jclass, jlong wrapper_ptr,
                                              jlong j_value) {
    try {
        auto& wrapper = *reinterpret_cast<SetWrapper*>(wrapper_ptr);
        object_store::Set& set = wrapper.collection();
        realm::Timestamp timestamp = from_milliseconds(j_value);

        // TODO: abstract this call so that the rest is the same for all types
        const std::pair<size_t, bool>& remove_pair = set.remove_any(Mixed(timestamp));

        jlong ret[2];
        ret[0] = remove_pair.first;     // index
        ret[1] = remove_pair.second;    // found (or not)
        jlongArray ret_array = env->NewLongArray(2);
        env->SetLongArrayRegion(ret_array, 0, 2, ret);
        return ret_array;
    }
    CATCH_STD()
    return nullptr;
}

JNIEXPORT jlongArray JNICALL
Java_io_realm_internal_OsSet_nativeRemoveDecimal128(JNIEnv* env, jclass, jlong wrapper_ptr,
                                                    jlong j_low_value, jlong j_high_value) {
    try {
        auto& wrapper = *reinterpret_cast<SetWrapper*>(wrapper_ptr);
        object_store::Set& set = wrapper.collection();
        Decimal128::Bid128 raw {static_cast<uint64_t>(j_low_value), static_cast<uint64_t>(j_high_value)};
        Decimal128 decimal128 = Decimal128(raw);

        // TODO: abstract this call so that the rest is the same for all types
        const std::pair<size_t, bool>& remove_pair = set.remove_any(Mixed(decimal128));

        jlong ret[2];
        ret[0] = remove_pair.first;     // index
        ret[1] = remove_pair.second;    // found (or not)
        jlongArray ret_array = env->NewLongArray(2);
        env->SetLongArrayRegion(ret_array, 0, 2, ret);
        return ret_array;
    }
    CATCH_STD()
    return nullptr;
}

JNIEXPORT jlongArray JNICALL
Java_io_realm_internal_OsSet_nativeRemoveObjectId(JNIEnv* env, jclass, jlong wrapper_ptr,
                                                  jstring j_value) {
    try {
        auto& wrapper = *reinterpret_cast<SetWrapper*>(wrapper_ptr);
        object_store::Set& set = wrapper.collection();
        JStringAccessor data(env, j_value);
        const ObjectId object_id = ObjectId(StringData(data).data());

        // TODO: abstract this call so that the rest is the same for all types
        const std::pair<size_t, bool>& remove_pair = set.remove_any(Mixed(object_id));


        jlong ret[2];
        ret[0] = remove_pair.first;     // index
        ret[1] = remove_pair.second;    // found (or not)
        jlongArray ret_array = env->NewLongArray(2);
        env->SetLongArrayRegion(ret_array, 0, 2, ret);
        return ret_array;
    }
    CATCH_STD()
    return nullptr;
}

JNIEXPORT jlongArray JNICALL
Java_io_realm_internal_OsSet_nativeRemoveUUID(JNIEnv* env, jclass, jlong wrapper_ptr,
                                              jstring j_value) {
    try {
        auto& wrapper = *reinterpret_cast<SetWrapper*>(wrapper_ptr);
        object_store::Set& set = wrapper.collection();
        JStringAccessor value(env, j_value);
        const UUID& uuid = UUID(StringData(value).data());

        // TODO: abstract this call so that the rest is the same for all types
        const std::pair<size_t, bool>& remove_pair = set.remove_any(Mixed(uuid));

        jlong ret[2];
        ret[0] = remove_pair.first;     // index
        ret[1] = remove_pair.second;    // found (or not)
        jlongArray ret_array = env->NewLongArray(2);
        env->SetLongArrayRegion(ret_array, 0, 2, ret);
        return ret_array;
    }
    CATCH_STD()
    return nullptr;
}

JNIEXPORT jlongArray JNICALL
Java_io_realm_internal_OsSet_nativeRemoveRow(JNIEnv* env, jclass, jlong wrapper_ptr,
                                             jlong j_obj_key) {
    try {
        auto& wrapper = *reinterpret_cast<SetWrapper*>(wrapper_ptr);
        object_store::Set& set = wrapper.collection();
        ObjKey object_key(j_obj_key);

        // TODO: abstract this call so that the rest is the same for all types
        const std::pair<size_t, bool>& remove_pair = set.remove(object_key);

        jlong ret[2];
        ret[0] = remove_pair.first;     // index
        ret[1] = remove_pair.second;    // found (or not)
        jlongArray ret_array = env->NewLongArray(2);
        env->SetLongArrayRegion(ret_array, 0, 2, ret);
        return ret_array;
    }
    CATCH_STD()
    return nullptr;
}

JNIEXPORT jlongArray JNICALL
Java_io_realm_internal_OsSet_nativeRemoveRealmAny(JNIEnv* env, jclass, jlong wrapper_ptr,
                                               jlong mixed_ptr) {
    try {
        auto& wrapper = *reinterpret_cast<SetWrapper*>(wrapper_ptr);
        object_store::Set& set = wrapper.collection();
        auto& java_value = *reinterpret_cast<JavaValue*>(mixed_ptr);

        // TODO: abstract this call so that the rest is the same for all types
        const std::pair<size_t, bool>& remove_pair = set.remove(java_value.to_mixed());

        jlong ret[2];
        ret[0] = remove_pair.first;     // index
        ret[1] = remove_pair.second;    // found (or not)
        jlongArray ret_array = env->NewLongArray(2);
        env->SetLongArrayRegion(ret_array, 0, 2, ret);
        return ret_array;
    }
    CATCH_STD()
    return nullptr;
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_OsSet_nativeGetRow(JNIEnv* env, jclass, jlong wrapper_ptr,
                                          jint j_index) {
    try {
        auto& wrapper = *reinterpret_cast<SetWrapper*>(wrapper_ptr);
        object_store::Set& set = wrapper.collection();
        const Obj &obj = set.get(j_index);
        return obj.get_key().value;
    }
    CATCH_STD()
    return -1;
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_OsSet_nativeGetRealmAny(JNIEnv* env, jclass, jlong wrapper_ptr,
                                            jint j_index) {
    try {
        auto& wrapper = *reinterpret_cast<SetWrapper*>(wrapper_ptr);
        object_store::Set& set = wrapper.collection();
        const Mixed& mixed = set.get_any(j_index);
        return reinterpret_cast<jlong>(new JavaValue(from_mixed(mixed)));
    }
    CATCH_STD()
    return -1;
}

JNIEXPORT jboolean JNICALL
Java_io_realm_internal_OsSet_nativeContainsAll(JNIEnv*, jclass, jlong wrapper_ptr, jlong other_wrapper_ptr) {
    auto& wrapper = *reinterpret_cast<SetWrapper*>(wrapper_ptr);
    object_store::Set& set = wrapper.collection();
    auto& other_wrapper = *reinterpret_cast<SetWrapper*>(other_wrapper_ptr);
    object_store::Set& other_set = other_wrapper.collection();

    // If other set is a subset of set then set contains other set
    bool is_contained = other_set.is_subset_of(set);
    return is_contained;
}

JNIEXPORT jboolean JNICALL
Java_io_realm_internal_OsSet_nativeUnion(JNIEnv*, jclass, jlong wrapper_ptr, jlong other_wrapper_ptr) {
    auto& wrapper = *reinterpret_cast<SetWrapper*>(wrapper_ptr);
    object_store::Set& set = wrapper.collection();
    auto& other_wrapper = *reinterpret_cast<SetWrapper*>(other_wrapper_ptr);
    object_store::Set& other_set = other_wrapper.collection();

    // If other set is a subset of set it means set will not change after the union
    bool has_changed = !other_set.is_subset_of(set);
    set.assign_union(other_set);
    return has_changed;
}

JNIEXPORT jboolean JNICALL
Java_io_realm_internal_OsSet_nativeAsymmetricDifference(JNIEnv*,
                                                        jclass,
                                                        jlong wrapper_ptr,
                                                        jlong other_wrapper_ptr) {
    auto& wrapper = *reinterpret_cast<SetWrapper*>(wrapper_ptr);
    object_store::Set& set = wrapper.collection();
    auto& other_wrapper = *reinterpret_cast<SetWrapper*>(other_wrapper_ptr);
    object_store::Set& other_set = other_wrapper.collection();

    // If other set is a subset of set it means set will change after the difference
    bool has_changed = other_set.is_subset_of(set);
    set.assign_difference(other_set);
    return has_changed;
}

JNIEXPORT jboolean JNICALL
Java_io_realm_internal_OsSet_nativeIntersect(JNIEnv*,
                                             jclass,
                                             jlong wrapper_ptr,
                                             jlong other_wrapper_ptr) {
    auto& wrapper = *reinterpret_cast<SetWrapper*>(wrapper_ptr);
    object_store::Set& set = wrapper.collection();
    auto& other_wrapper = *reinterpret_cast<SetWrapper*>(other_wrapper_ptr);
    object_store::Set& other_set = other_wrapper.collection();

    // If other set intersects set it means set will not change after the intersection
    bool has_changed = !set.intersects(other_set);
    set.assign_intersection(other_set);
    return has_changed;
}

JNIEXPORT void JNICALL
Java_io_realm_internal_OsSet_nativeClear(JNIEnv* env, jclass, jlong wrapper_ptr) {
    try {
        auto& wrapper = *reinterpret_cast<SetWrapper*>(wrapper_ptr);
        object_store::Set& set = wrapper.collection();
        set.remove_all();
    }
    CATCH_STD()
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_OsSet_nativeFreeze(JNIEnv* env, jclass, jlong wrapper_ptr,
                                          jlong frozen_realm_ptr) {
    try {
        auto& wrapper = *reinterpret_cast<SetWrapper*>(wrapper_ptr);
        object_store::Set& set = wrapper.collection();
        auto& shared_realm_ptr = *reinterpret_cast<std::shared_ptr<Realm>*>(frozen_realm_ptr);
        object_store::Set frozen_set = set.freeze(shared_realm_ptr);
        return reinterpret_cast<jlong>(new SetWrapper(frozen_set, "io/realm/internal/ObservableSet"));
    }
    CATCH_STD()
    return reinterpret_cast<jlong>(nullptr);
}

JNIEXPORT jboolean JNICALL
Java_io_realm_internal_OsSet_nativeContainsAllRealmAnyCollection(JNIEnv* env, jclass, jlong wrapper_ptr,
                                                              jlong mixed_collection_ptr) {
    try {
        auto& wrapper = *reinterpret_cast<SetWrapper*>(wrapper_ptr);
        object_store::Set& set = wrapper.collection();
        auto &collection = *reinterpret_cast<std::vector<JavaValue> *>(mixed_collection_ptr);
        const std::vector<Mixed>& mixed_collection = to_mixed_vector(collection);

        for (const Mixed& mixed : mixed_collection) {
            if (!mixed.is_null() || isSetNullable(env, set)) {
                size_t found;
                found = set.find_any(mixed);

                if (found == npos) {    // npos represents "not found"
                    return false;
                }
            }
        }
        return true;
    }
    CATCH_STD()
    return false;
}

JNIEXPORT jboolean JNICALL
Java_io_realm_internal_OsSet_nativeAddAllRealmAnyCollection(JNIEnv* env, jclass, jlong wrapper_ptr,
                                                         jlong mixed_collection_ptr) {
    try {
        auto& wrapper = *reinterpret_cast<SetWrapper*>(wrapper_ptr);
        object_store::Set& set = wrapper.collection();
        auto& collection = *reinterpret_cast<std::vector<JavaValue> *>(mixed_collection_ptr);
        const std::vector<Mixed>& mixed_collection = to_mixed_vector(collection);
        bool set_has_changed = false;

        for (const Mixed &mixed : mixed_collection) {
            if (!mixed.is_null() || isSetNullable(env, set)) {
                const std::pair<size_t, bool> &insert_pair = set.insert_any(mixed);

                // If we get true it means the element was not there and therefore it has changed
                if (insert_pair.second) {
                    set_has_changed = true;
                }
            }
        }
        return set_has_changed;
    }
    CATCH_STD()
    return false;
}

JNIEXPORT jboolean JNICALL
Java_io_realm_internal_OsSet_nativeRemoveAllRealmAnyCollection(JNIEnv* env, jclass, jlong wrapper_ptr,
                                                            jlong mixed_collection_ptr) {
    try {
        auto& wrapper = *reinterpret_cast<SetWrapper*>(wrapper_ptr);
        object_store::Set& set = wrapper.collection();
        auto& collection = *reinterpret_cast<std::vector<JavaValue> *>(mixed_collection_ptr);
        const std::vector<Mixed>& mixed_collection = to_mixed_vector(collection);
        bool set_has_changed = false;

        for (const Mixed &mixed : mixed_collection) {
            if (!mixed.is_null() || isSetNullable(env, set)) {
                const std::pair<size_t, bool> &remove_pair = set.remove_any(mixed);

                // If we get true it means the element was not there and therefore it has changed
                if (remove_pair.second) {
                    set_has_changed = true;
                }
            }
        }
        return set_has_changed;
    }
    CATCH_STD()
    return false;
}

JNIEXPORT jboolean JNICALL
Java_io_realm_internal_OsSet_nativeRetainAllRealmAnyCollection(JNIEnv* env, jclass, jlong wrapper_ptr,
                                                            jlong mixed_collection_ptr) {
    try {
        auto& wrapper = *reinterpret_cast<SetWrapper*>(wrapper_ptr);
        object_store::Set& set = wrapper.collection();
        auto &collection = *reinterpret_cast<std::vector<JavaValue> *>(mixed_collection_ptr);
        const std::vector<Mixed> &mixed_collection = to_mixed_vector(collection);

        std::vector<Mixed> common_elements;
        bool set_has_changed = false;

        for (const Mixed &mixed : mixed_collection) {
            if (!mixed.is_null() || isSetNullable(env, set)) {
                // Check for present values
                if (set.find_any(mixed) != realm::npos) {
                    // Put shared elements and store them in an auxiliary structure to use later
                    common_elements.push_back(mixed);
                } else {
                    // If an element is not found that means the set will change
                    set_has_changed = true;
                }
            }
        }

        // Insert shared elements now
        set.remove_all();
        for (auto& shared_element : common_elements) {
            set.insert_any(shared_element);
        }

        return set_has_changed;
    }
    CATCH_STD()
    return false;
}

JNIEXPORT void JNICALL
Java_io_realm_internal_OsSet_nativeStartListening(JNIEnv* env, jclass, jlong wrapper_ptr,
                                                  jobject j_observable_map) {
    try {
        auto wrapper = reinterpret_cast<SetWrapper*>(wrapper_ptr);
        wrapper->start_listening(env, j_observable_map);
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL
Java_io_realm_internal_OsSet_nativeStopListening(JNIEnv* env, jclass, jlong wrapper_ptr) {
    try {
        auto wrapper = reinterpret_cast<SetWrapper*>(wrapper_ptr);
        wrapper->stop_listening();
    }
    CATCH_STD()
}
