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

#include "io_realm_internal_core_NativeMixed.h"

#include "java_accessor.hpp"

using namespace std;
using namespace realm;
using namespace realm::_impl;

static void finalize_mixed(jlong ptr) {
    delete reinterpret_cast<Mixed *>(ptr);
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_core_NativeMixed_nativeGetFinalizerPtr(JNIEnv *, jclass) {
    return reinterpret_cast<jlong>(&finalize_mixed);
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_core_NativeMixed_nativeCreateMixedNull(JNIEnv *, jclass) {
    return reinterpret_cast<jlong>(new Mixed());
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_core_NativeMixed_nativeCreateMixedBoolean(JNIEnv *, jclass, jboolean j_value) {
    return reinterpret_cast<jlong>(new Mixed(B(j_value)));
}

JNIEXPORT jboolean JNICALL
Java_io_realm_internal_core_NativeMixed_nativeMixedAsBoolean(JNIEnv *, jclass, jlong native_ptr) {
    auto mixed = reinterpret_cast<Mixed *>(native_ptr);
    return mixed->get<bool>();
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_core_NativeMixed_nativeCreateMixedLong(JNIEnv *, jclass, jlong j_value) {
    return reinterpret_cast<jlong>(new Mixed(j_value));
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_core_NativeMixed_nativeMixedAsLong(JNIEnv *, jclass, jlong native_ptr) {
    auto mixed = reinterpret_cast<Mixed *>(native_ptr);
    return mixed->get<int64_t>();
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_core_NativeMixed_nativeCreateMixedFloat(JNIEnv *, jclass, jfloat j_value) {
    return reinterpret_cast<jlong>(new Mixed(j_value));
}

JNIEXPORT jfloat JNICALL
Java_io_realm_internal_core_NativeMixed_nativeMixedAsFloat(JNIEnv *, jclass, jlong native_ptr) {
    auto mixed = reinterpret_cast<Mixed *>(native_ptr);
    return mixed->get<float>();
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_core_NativeMixed_nativeCreateMixedDouble(JNIEnv *, jclass, jdouble j_value) {
    return reinterpret_cast<jlong>(new Mixed(j_value));
}

JNIEXPORT jdouble JNICALL
Java_io_realm_internal_core_NativeMixed_nativeMixedAsDouble(JNIEnv *, jclass, jlong native_ptr) {
    auto mixed = reinterpret_cast<Mixed *>(native_ptr);
    return mixed->get<double>();
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_core_NativeMixed_nativeCreateMixedString(JNIEnv *env, jclass, jstring j_value) {
    try{
        JStringAccessor string_accessor(env, j_value); // throws
        return reinterpret_cast<jlong>(new Mixed(StringData(string_accessor)));
    } CATCH_STD()

    return 0;
}

JNIEXPORT jstring JNICALL
Java_io_realm_internal_core_NativeMixed_nativeMixedAsString(JNIEnv *env, jclass, jlong native_ptr) {
    auto mixed = reinterpret_cast<Mixed *>(native_ptr);
    return to_jstring(env, mixed->get<StringData>());
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_core_NativeMixed_nativeCreateMixedBinary(JNIEnv *env, jclass, jbyteArray j_value) {
    try{
        JByteArrayAccessor array_accessor(env, j_value); // throws
        return reinterpret_cast<jlong>(new Mixed(array_accessor.transform<BinaryData>()));
    } CATCH_STD()

    return 0;
}

JNIEXPORT jbyteArray JNICALL
Java_io_realm_internal_core_NativeMixed_nativeMixedAsBinary(JNIEnv *env, jclass, jlong native_ptr) {
    try{
        auto mixed = reinterpret_cast<Mixed *>(native_ptr);

        realm::BinaryData bin = mixed->get<BinaryData>();
        return JavaClassGlobalDef::new_byte_array(env, bin);
    } CATCH_STD()

    return nullptr;
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_core_NativeMixed_nativeCreateMixedDate(JNIEnv *, jclass, jlong j_value) {
    return reinterpret_cast<jlong>(new Mixed(from_milliseconds(j_value)));
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_core_NativeMixed_nativeMixedAsDate(JNIEnv *, jclass, jlong native_ptr) {
    auto mixed = reinterpret_cast<Mixed *>(native_ptr);
    return to_milliseconds(mixed->get<Timestamp>());
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_core_NativeMixed_nativeCreateMixedObjectId(JNIEnv *env, jclass, jstring j_value) {
    try{
        JStringAccessor string_accessor(env, j_value); // throws
        return reinterpret_cast<jlong>(new Mixed(ObjectId(StringData(string_accessor).data())));
    } CATCH_STD()
    return 0;
}

JNIEXPORT jstring JNICALL
Java_io_realm_internal_core_NativeMixed_nativeMixedAsObjectId(JNIEnv *env, jclass, jlong native_ptr) {
    auto mixed = reinterpret_cast<Mixed *>(native_ptr);
    return to_jstring(env, mixed->get<ObjectId>().to_string().data());
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_core_NativeMixed_nativeCreateMixedDecimal128(JNIEnv *, jclass, jlong j_high, jlong j_low) {
    Decimal128::Bid128 raw{static_cast<uint64_t>(j_high), static_cast<uint64_t>(j_low)};
    return reinterpret_cast<jlong>(new Mixed(Decimal128(raw)));
}

JNIEXPORT jlongArray JNICALL
Java_io_realm_internal_core_NativeMixed_nativeMixedAsDecimal128(JNIEnv *env, jclass, jlong native_ptr) {
    auto mixed = reinterpret_cast<Mixed *>(native_ptr);
    Decimal128 decimal128 = mixed->get<Decimal128>();
    RETURN_DECIMAL128_AS_JLONG_ARRAY__OR_NULL(decimal128)
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_core_NativeMixed_nativeCreateMixedUUID(JNIEnv *env, jclass, jstring j_value) {
    try {
        JStringAccessor string_accessor(env, j_value); // throws
        return reinterpret_cast<jlong>(new Mixed(UUID(StringData(string_accessor).data())));
    } CATCH_STD()

    return 0;
}

JNIEXPORT jstring JNICALL
Java_io_realm_internal_core_NativeMixed_nativeMixedAsUUID(JNIEnv *env, jclass, jlong native_ptr) {
    auto mixed = reinterpret_cast<Mixed *>(native_ptr);
    return to_jstring(env, mixed->get<UUID>().to_string().data());
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_core_NativeMixed_nativeCreateMixedLink(JNIEnv *, jclass, jlong target_table_ref,
                                                              jlong target_object_key) {
    TableRef target_table = TBL_REF(target_table_ref);
    ObjKey object_key(target_object_key);
    ObjLink object_link(target_table->get_key(), object_key);

    return reinterpret_cast<jlong>(new Mixed(object_link));
}

JNIEXPORT jint JNICALL
Java_io_realm_internal_core_NativeMixed_nativeGetMixedType(JNIEnv *, jclass, jlong native_ptr) {
    auto mixed = reinterpret_cast<Mixed *>(native_ptr);

    if (mixed->is_null()) {
        return -1;
    } else {
        return mixed->get_type();
    }
}

JNIEXPORT jstring JNICALL
Java_io_realm_internal_core_NativeMixed_nativeGetRealmModelTableName(JNIEnv *env, jclass, jlong native_ptr,
                                                                     jlong shared_realm_ptr) {
    auto mixed = reinterpret_cast<Mixed *>(native_ptr);
    auto &shared_realm = *(reinterpret_cast<SharedRealm *>(shared_realm_ptr));

    auto obj_link = mixed->get<ObjLink>();

    return to_jstring(env, shared_realm->read_group().get_table(obj_link.get_table_key())->get_name());
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_core_NativeMixed_nativeGetRealmModelRowKey(JNIEnv *, jclass, jlong native_ptr) {
    auto mixed = reinterpret_cast<Mixed *>(native_ptr);

    auto obj_link = mixed->get<ObjLink>();
    return obj_link.get_obj_key().value;
}

