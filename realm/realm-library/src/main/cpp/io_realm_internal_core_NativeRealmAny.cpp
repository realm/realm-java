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

#include "io_realm_internal_core_NativeRealmAny.h"

#include "java_accessor.hpp"
#include "java_object_accessor.hpp"

using namespace std;
using namespace realm;
using namespace realm::_impl;

static void finalize_mixed(jlong ptr) {
    delete reinterpret_cast<JavaValue *>(ptr);
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_core_NativeRealmAny_nativeGetFinalizerPtr(JNIEnv *, jclass) {
    return reinterpret_cast<jlong>(&finalize_mixed);
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_core_NativeRealmAny_nativeCreateNull(JNIEnv *env, jclass) {
    try {
        return reinterpret_cast<jlong>(new JavaValue());
    } CATCH_STD()

    return reinterpret_cast<jlong>(nullptr);
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_core_NativeRealmAny_nativeCreateBoolean(JNIEnv *env, jclass, jboolean j_value) {
    try {
        return reinterpret_cast<jlong>(new JavaValue(j_value));
    } CATCH_STD()

    return reinterpret_cast<jlong>(nullptr);
}

JNIEXPORT jboolean JNICALL
Java_io_realm_internal_core_NativeRealmAny_nativeAsBoolean(JNIEnv *env, jclass, jlong native_ptr) {
    try {
        auto java_value = *reinterpret_cast<JavaValue *>(native_ptr);
        return java_value.get_boolean();
    } CATCH_STD()

    return 0;
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_core_NativeRealmAny_nativeCreateLong(JNIEnv *env, jclass, jlong j_value) {
    try {
        return reinterpret_cast<jlong>(new JavaValue(j_value));
    } CATCH_STD()

    return reinterpret_cast<jlong>(nullptr);
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_core_NativeRealmAny_nativeAsLong(JNIEnv *env, jclass, jlong native_ptr) {
    try {
        auto java_value = *reinterpret_cast<JavaValue *>(native_ptr);
        return java_value.get_int();
    } CATCH_STD()

    return 0;
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_core_NativeRealmAny_nativeCreateFloat(JNIEnv *env, jclass, jfloat j_value) {
    try {
        return reinterpret_cast<jlong>(new JavaValue(j_value));
    } CATCH_STD()

    return reinterpret_cast<jlong>(nullptr);
}

JNIEXPORT jfloat JNICALL
Java_io_realm_internal_core_NativeRealmAny_nativeAsFloat(JNIEnv *env, jclass, jlong native_ptr) {
    try {
        auto java_value = *reinterpret_cast<JavaValue *>(native_ptr);
        return java_value.get_float();
    } CATCH_STD()

    return 0;
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_core_NativeRealmAny_nativeCreateDouble(JNIEnv *env, jclass, jdouble j_value) {
    try {
        return reinterpret_cast<jlong>(new JavaValue(j_value));
    } CATCH_STD()

    return reinterpret_cast<jlong>(nullptr);
}

JNIEXPORT jdouble JNICALL
Java_io_realm_internal_core_NativeRealmAny_nativeAsDouble(JNIEnv *env, jclass, jlong native_ptr) {
    try {
        auto java_value = *reinterpret_cast<JavaValue *>(native_ptr);
        return java_value.get_double();
    } CATCH_STD()

    return 0;
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_core_NativeRealmAny_nativeCreateString(JNIEnv *env, jclass, jstring j_value) {
    try {
        JStringAccessor string_accessor(env, j_value); // throws
        return reinterpret_cast<jlong>(new JavaValue(std::string(string_accessor)));
    } CATCH_STD()

    return reinterpret_cast<jlong>(nullptr);
}

JNIEXPORT jstring JNICALL
Java_io_realm_internal_core_NativeRealmAny_nativeAsString(JNIEnv *env, jclass, jlong native_ptr) {
    try {
        auto java_value = *reinterpret_cast<JavaValue *>(native_ptr);
        return to_jstring(env, java_value.get_string());
    } CATCH_STD()

    return nullptr;
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_core_NativeRealmAny_nativeCreateBinary(JNIEnv *env, jclass, jbyteArray j_value) {
    try {
        JByteArrayAccessor array_accessor(env, j_value); // throws
        auto data = OwnedBinaryData(JByteArrayAccessor(env, j_value).transform<BinaryData>());
        return reinterpret_cast<jlong>(new JavaValue(data));
    } CATCH_STD()

    return reinterpret_cast<jlong>(nullptr);
}

JNIEXPORT jbyteArray JNICALL
Java_io_realm_internal_core_NativeRealmAny_nativeAsBinary(JNIEnv *env, jclass, jlong native_ptr) {
    try {
        auto java_value = *reinterpret_cast<JavaValue *>(native_ptr);
        return JavaClassGlobalDef::new_byte_array(env, java_value.get_binary().get());
    } CATCH_STD()

    return nullptr;
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_core_NativeRealmAny_nativeCreateDate(JNIEnv *env, jclass, jlong j_value) {
    try {
        return reinterpret_cast<jlong>(new JavaValue(from_milliseconds(j_value)));
    } CATCH_STD()

    return reinterpret_cast<jlong>(nullptr);
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_core_NativeRealmAny_nativeAsDate(JNIEnv *env, jclass, jlong native_ptr) {
    try {
        auto java_value = *reinterpret_cast<JavaValue *>(native_ptr);
        return to_milliseconds(java_value.get_date());
    } CATCH_STD()

    return 0;
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_core_NativeRealmAny_nativeCreateObjectId(JNIEnv *env, jclass, jstring j_value) {
    try {
        JStringAccessor string_accessor(env, j_value); // throws
        return reinterpret_cast<jlong>(new JavaValue(ObjectId(StringData(string_accessor).data())));
    } CATCH_STD()

    return reinterpret_cast<jlong>(nullptr);
}

JNIEXPORT jstring JNICALL
Java_io_realm_internal_core_NativeRealmAny_nativeAsObjectId(JNIEnv *env, jclass, jlong native_ptr) {
    try {
        auto java_value = *reinterpret_cast<JavaValue *>(native_ptr);
        return to_jstring(env, java_value.get_object_id().to_string().data());
    } CATCH_STD()

    return nullptr;
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_core_NativeRealmAny_nativeCreateDecimal128(JNIEnv *env, jclass, jlong j_low, jlong j_high) {
    try {
        Decimal128::Bid128 raw{static_cast<uint64_t>(j_low), static_cast<uint64_t>(j_high)};
        return reinterpret_cast<jlong>(new JavaValue(Decimal128(raw)));
    } CATCH_STD()

    return reinterpret_cast<jlong>(nullptr);
}

JNIEXPORT jlongArray JNICALL
Java_io_realm_internal_core_NativeRealmAny_nativeAsDecimal128(JNIEnv *env, jclass, jlong native_ptr) {
    try {
        auto java_value = *reinterpret_cast<JavaValue *>(native_ptr);
        Decimal128 decimal128 = java_value.get_decimal128();
        RETURN_DECIMAL128_AS_JLONG_ARRAY__OR_NULL(decimal128)
    } CATCH_STD()

    return nullptr;
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_core_NativeRealmAny_nativeCreateUUID(JNIEnv *env, jclass, jstring j_value) {
    try {
        JStringAccessor string_accessor(env, j_value); // throws
        return reinterpret_cast<jlong>(new JavaValue(UUID(StringData(string_accessor).data())));
    } CATCH_STD()

    return reinterpret_cast<jlong>(nullptr);
}

JNIEXPORT jstring JNICALL
Java_io_realm_internal_core_NativeRealmAny_nativeAsUUID(JNIEnv *env, jclass, jlong native_ptr) {
    try {
        auto java_value = *reinterpret_cast<JavaValue *>(native_ptr);
        return to_jstring(env, java_value.get_uuid().to_string().data());
    } CATCH_STD()

    return nullptr;
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_core_NativeRealmAny_nativeCreateLink(JNIEnv *env, jclass, jlong target_table_ref,
                                                              jlong target_object_key) {
    try {
        TableRef target_table = TBL_REF(target_table_ref);
        ObjKey object_key(target_object_key);
        ObjLink object_link(target_table->get_key(), object_key);

        return reinterpret_cast<jlong>(new JavaValue(object_link));
    } CATCH_STD()

    return reinterpret_cast<jlong>(nullptr);
}

JNIEXPORT jint JNICALL
Java_io_realm_internal_core_NativeRealmAny_nativeGetType(JNIEnv *env, jclass, jlong native_ptr) {
    try {
        auto java_value = *reinterpret_cast<JavaValue *>(native_ptr);
        auto mixed = java_value.to_mixed();

        return mixed.is_null() ? -1 : int(mixed.get_type());
    } CATCH_STD()

    return -1;
}

JNIEXPORT jstring JNICALL
Java_io_realm_internal_core_NativeRealmAny_nativeGetRealmModelTableName(JNIEnv *env, jclass, jlong native_ptr,
                                                                     jlong shared_realm_ptr) {
    try {
        auto java_value = *reinterpret_cast<JavaValue *>(native_ptr);
        auto &shared_realm = *(reinterpret_cast<SharedRealm *>(shared_realm_ptr));

        if (java_value.get_type() == JavaValueType::ObjectLink) {
            auto &obj_link = java_value.get_object_link();
            return to_jstring(env, shared_realm->read_group().get_table(obj_link.get_table_key())->get_name());
        } else {
            auto &obj = java_value.get_object();
            return to_jstring(env, obj->get_table()->get_name());
        }
    } CATCH_STD()

    return nullptr;
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_core_NativeRealmAny_nativeGetRealmModelRowKey(JNIEnv *env, jclass, jlong native_ptr) {
    try {
        auto java_value = *reinterpret_cast<JavaValue *>(native_ptr);

        if (java_value.get_type() == JavaValueType::ObjectLink) {
            auto obj_link = java_value.get_object_link();
            return obj_link.get_obj_key().value;
        } else {
            auto &obj = java_value.get_object();
            return obj->get_key().value;
        }

    } CATCH_STD()

    return 0;
}

JNIEXPORT jboolean JNICALL
Java_io_realm_internal_core_NativeRealmAny_nativeEquals(JNIEnv *env, jclass, jlong native_ptr, jlong native_other_ptr) {
    try {
        auto java_value = *reinterpret_cast<JavaValue *>(native_ptr);
        auto other_java_value = *reinterpret_cast<JavaValue *>(native_other_ptr);

        auto mixed = java_value.to_mixed();
        auto other_mixed = other_java_value.to_mixed();

        return mixed.compare(other_mixed) == 0;
    } CATCH_STD()

    return false;
}

