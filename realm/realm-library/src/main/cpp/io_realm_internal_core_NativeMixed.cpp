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

#include "util.hpp"
#include "java_accessor.hpp"
#include "util.hpp"
#include <realm/util/to_string.hpp>

#include "java_exception_def.hpp"
#include <realm/object-store/shared_realm.hpp>
#include "jni_util/java_exception_thrower.hpp"

using namespace std;
using namespace realm;
using namespace realm::_impl;
using namespace realm::jni_util;
using namespace realm::util;

static void finalize_mixed(jlong ptr) {
    delete reinterpret_cast<Mixed *>(ptr);
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_core_NativeMixed_nativeGetFinalizerPtr(JNIEnv *, jclass) {
    return reinterpret_cast<jlong>(&finalize_mixed);
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_core_NativeMixed_nativeCreateMixedBoolean(JNIEnv *, jclass, jboolean jvalue) {
    return reinterpret_cast<jlong>(new Mixed(B(jvalue)));
}

JNIEXPORT jboolean JNICALL
Java_io_realm_internal_core_NativeMixed_nativeMixedAsBoolean(JNIEnv *, jclass, jlong nativePtr) {
    auto mixed = reinterpret_cast<Mixed *>(nativePtr);
    return mixed->get<bool>();
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_core_NativeMixed_nativeCreateMixedLong(JNIEnv *, jclass, jlong jvalue) {
    return reinterpret_cast<jlong>(new Mixed(jvalue));
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_core_NativeMixed_nativeMixedAsLong(JNIEnv *, jclass, jlong nativePtr) {
    auto mixed = reinterpret_cast<Mixed *>(nativePtr);
    return mixed->get<int64_t>();
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_core_NativeMixed_nativeCreateMixedFloat(JNIEnv *, jclass, jfloat jvalue) {
    return reinterpret_cast<jlong>(new Mixed(jvalue));
}

JNIEXPORT jfloat JNICALL
Java_io_realm_internal_core_NativeMixed_nativeMixedAsFloat(JNIEnv *, jclass, jlong nativePtr) {
    auto mixed = reinterpret_cast<Mixed *>(nativePtr);
    return mixed->get<float>();
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_core_NativeMixed_nativeCreateMixedDouble(JNIEnv *, jclass, jdouble jvalue) {
    return reinterpret_cast<jlong>(new Mixed(jvalue));
}

JNIEXPORT jdouble JNICALL
Java_io_realm_internal_core_NativeMixed_nativeMixedAsDouble(JNIEnv *, jclass, jlong nativePtr) {
    auto mixed = reinterpret_cast<Mixed *>(nativePtr);
    return mixed->get<double>();
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_core_NativeMixed_nativeCreateMixedString(JNIEnv *env, jclass, jstring jvalue) {
    JStringAccessor string_accessor(env, jvalue); // throws
    return reinterpret_cast<jlong>(new Mixed(StringData(string_accessor)));
}

JNIEXPORT jstring JNICALL
Java_io_realm_internal_core_NativeMixed_nativeMixedAsString(JNIEnv *env, jclass, jlong nativePtr) {
    auto mixed = reinterpret_cast<Mixed *>(nativePtr);
    return to_jstring(env, mixed->get<StringData>());
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_core_NativeMixed_nativeCreateMixedBinary(JNIEnv *env, jclass, jbyteArray jvalue) {
    JByteArrayAccessor jarray_accessor(env, jvalue);
    return reinterpret_cast<jlong>(new Mixed(jarray_accessor.transform<BinaryData>()));
}

JNIEXPORT jbyteArray JNICALL
Java_io_realm_internal_core_NativeMixed_nativeMixedAsBinary(JNIEnv *env, jclass, jlong nativePtr) {
    auto mixed = reinterpret_cast<Mixed *>(nativePtr);

    realm::BinaryData bin = mixed->get<BinaryData>();
    return JavaClassGlobalDef::new_byte_array(env, bin);
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_core_NativeMixed_nativeCreateMixedDate(JNIEnv *, jclass, jlong jvalue) {
    return reinterpret_cast<jlong>(new Mixed(from_milliseconds(jvalue)));
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_core_NativeMixed_nativeMixedAsDate(JNIEnv *, jclass, jlong nativePtr) {
    auto mixed = reinterpret_cast<Mixed *>(nativePtr);
    return to_milliseconds(mixed->get<Timestamp>());
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_core_NativeMixed_nativeCreateMixedObjectId(JNIEnv *env, jclass, jstring jvalue) {
    JStringAccessor string_accessor(env, jvalue); // throws
    return reinterpret_cast<jlong>(new Mixed(ObjectId(StringData(string_accessor).data())));
}

JNIEXPORT jstring JNICALL
Java_io_realm_internal_core_NativeMixed_nativeMixedAsObjectId(JNIEnv *env, jclass, jlong nativePtr) {
    auto mixed = reinterpret_cast<Mixed *>(nativePtr);
    return to_jstring(env, mixed->get<ObjectId>().to_string().data());
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_core_NativeMixed_nativeCreateMixedDecimal128(JNIEnv *, jclass, jlong jhigh, jlong jlow) {
    Decimal128::Bid128 raw{static_cast<uint64_t>(jhigh), static_cast<uint64_t>(jlow)};
    return reinterpret_cast<jlong>(new Mixed(Decimal128(raw)));
}

JNIEXPORT jlongArray JNICALL
Java_io_realm_internal_core_NativeMixed_nativeMixedAsDecimal128(JNIEnv *env, jclass, jlong nativePtr) {
    auto mixed = reinterpret_cast<Mixed *>(nativePtr);
    Decimal128 decimal128 = mixed->get<Decimal128>();
    RETURN_DECIMAL128_AS_JLONG_ARRAY__OR_NULL(decimal128)
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_core_NativeMixed_nativeCreateMixedUUID(JNIEnv *env, jclass, jstring jvalue) {
    JStringAccessor string_accessor(env, jvalue); // throws
    return reinterpret_cast<jlong>(new Mixed(UUID(StringData(string_accessor).data())));
}

JNIEXPORT jstring JNICALL
Java_io_realm_internal_core_NativeMixed_nativeMixedAsUUID(JNIEnv *env, jclass, jlong nativePtr) {
    auto mixed = reinterpret_cast<Mixed *>(nativePtr);
    return to_jstring(env, mixed->get<UUID>().to_string().data());
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_core_NativeMixed_nativeCreateMixedLink(JNIEnv *, jclass, jlong targetTableRef,
                                                              jlong targetObjectKey) {
    TableRef target_table = TBL_REF(targetTableRef);
    ObjKey object_key(targetObjectKey);
    ObjLink object_link(target_table->get_key(), object_key);

    return reinterpret_cast<jlong>(new Mixed(object_link));
}

JNIEXPORT jint JNICALL
Java_io_realm_internal_core_NativeMixed_nativeGetMixedType(JNIEnv *, jclass, jlong nativePtr) {
    auto mixed = reinterpret_cast<Mixed *>(nativePtr);

    if (mixed->is_null()) {
        return -1;
    } else {
        return mixed->get_type();
    }
}

JNIEXPORT jstring JNICALL
Java_io_realm_internal_core_NativeMixed_nativeGetRealmModelTableName(JNIEnv *env, jclass, jlong nativePtr,
                                                                     jlong nativeSharedRealmPtr) {
    auto mixed = reinterpret_cast<Mixed *>(nativePtr);
    auto &shared_realm = *(reinterpret_cast<SharedRealm *>(nativeSharedRealmPtr));

    auto obj_link = mixed->get<ObjLink>();

    return to_jstring(env, shared_realm->read_group().get_table(obj_link.get_table_key())->get_name());
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_core_NativeMixed_nativeGetRealmModelRowKey(JNIEnv *, jclass, jlong nativePtr) {
    auto mixed = reinterpret_cast<Mixed *>(nativePtr);

    auto obj_link = mixed->get<ObjLink>();
    return obj_link.get_obj_key().value;
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_core_NativeMixed_nativeCreateMixedNull(JNIEnv *, jclass) {
    return reinterpret_cast<jlong>(new Mixed());
}

