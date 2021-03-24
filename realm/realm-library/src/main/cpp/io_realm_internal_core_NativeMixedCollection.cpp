/*
 * Copyright 2021 Realm Inc.
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

#include "io_realm_internal_core_NativeMixedCollection.h"

#include "java_accessor.hpp"
#include "java_object_accessor.hpp"
#include "util.hpp"

using namespace std;
using namespace realm;
using namespace realm::_impl;

static void finalize_collection(jlong ptr) {
    delete reinterpret_cast<std::vector<JavaValue> *>(ptr);
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_core_NativeMixedCollection_nativeGetFinalizerPtr(JNIEnv *, jclass) {
    return reinterpret_cast<jlong>(&finalize_collection);
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_core_NativeMixedCollection_nativeCreateBooleanCollection(JNIEnv *env, jclass,
                                                                                jbooleanArray j_boolean_array,
                                                                                jbooleanArray j_not_null) {
    try {
        // TODO: could be worth templating this logic here and in the methods below
        //  https://github.com/realm/realm-java/issues/7384
        JBooleanArrayAccessor values(env, j_boolean_array);
        JBooleanArrayAccessor not_null(env, j_not_null);
        auto collection = new std::vector<JavaValue>();
        for (int i = 0; i < values.size(); i++) {
            if (not_null[i]) {
                collection->push_back(JavaValue(values[i]));
            } else {
                collection->push_back(JavaValue());
            }
        }
        return reinterpret_cast<jlong>(collection);
    } CATCH_STD()

    return reinterpret_cast<jlong>(nullptr);
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_core_NativeMixedCollection_nativeCreateIntegerCollection(JNIEnv *env, jclass,
                                                                                jlongArray j_long_array,
                                                                                jbooleanArray j_not_null) {
    try {
        JLongArrayAccessor values(env, j_long_array);
        JBooleanArrayAccessor not_null(env, j_not_null);
        auto collection = new std::vector<JavaValue>();
        for (int i = 0; i < values.size(); i++) {
            if (not_null[i]) {
                collection->push_back(JavaValue(values[i]));
            } else {
                collection->push_back(JavaValue());
            }
        }
        return reinterpret_cast<jlong>(collection);
    } CATCH_STD()

    return reinterpret_cast<jlong>(nullptr);
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_core_NativeMixedCollection_nativeCreateFloatCollection(JNIEnv *env, jclass,
                                                                              jfloatArray j_float_array,
                                                                              jbooleanArray j_not_null) {
    try {
        JFloatArrayAccessor values(env, j_float_array);
        JBooleanArrayAccessor not_null(env, j_not_null);
        auto collection = new std::vector<JavaValue>();
        for (int i = 0; i < values.size(); i++) {
            if (not_null[i]) {
                collection->push_back(JavaValue(values[i]));
            } else {
                collection->push_back(JavaValue());
            }
        }
        return reinterpret_cast<jlong>(collection);
    } CATCH_STD()

    return reinterpret_cast<jlong>(nullptr);
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_core_NativeMixedCollection_nativeCreateDoubleCollection(JNIEnv *env, jclass,
                                                                               jdoubleArray j_double_arrray,
                                                                               jbooleanArray j_not_null) {
    try {
        JDoubleArrayAccessor values(env, j_double_arrray);
        JBooleanArrayAccessor not_null(env, j_not_null);
        auto collection = new std::vector<JavaValue>();
        for (int i = 0; i < values.size(); i++) {
            if (not_null[i]) {
                collection->push_back(JavaValue(values[i]));
            } else {
                collection->push_back(JavaValue());
            }
        }
        return reinterpret_cast<jlong>(collection);
    } CATCH_STD()

    return reinterpret_cast<jlong>(nullptr);
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_core_NativeMixedCollection_nativeCreateStringCollection(JNIEnv *env, jclass,
                                                                               jobjectArray j_string_array,
                                                                               jbooleanArray j_not_null) {
    try {
        JObjectArrayAccessor<JStringAccessor, jstring> values(env, j_string_array);
        JBooleanArrayAccessor not_null(env, j_not_null);
        auto collection = new std::vector<JavaValue>();
        for (int i = 0; i < values.size(); i++) {
            if (not_null[i]) {
                collection->push_back(JavaValue(StringData(values[i])));
            } else {
                collection->push_back(JavaValue());
            }
        }
        return reinterpret_cast<jlong>(collection);
    } CATCH_STD()

    return reinterpret_cast<jlong>(nullptr);
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_core_NativeMixedCollection_nativeCreateBinaryCollection(JNIEnv *env, jclass,
                                                                               jobjectArray j_binary_array,
                                                                               jbooleanArray j_not_null) {
    try {
        JObjectArrayAccessor<JByteArrayAccessor, jbyteArray> values(env, j_binary_array);
        JBooleanArrayAccessor not_null(env, j_not_null);
        auto collection = new std::vector<JavaValue>();
        for (int i = 0; i < values.size(); i++) {
            if (not_null[i]) {
                collection->push_back(JavaValue(OwnedBinaryData(values[i].transform<BinaryData>())));
            } else {
                collection->push_back(JavaValue());
            }
        }
        return reinterpret_cast<jlong>(collection);
    } CATCH_STD()

    return reinterpret_cast<jlong>(nullptr);
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_core_NativeMixedCollection_nativeCreateDateCollection(JNIEnv *env, jclass,
                                                                             jlongArray j_date_array,
                                                                             jbooleanArray j_not_null) {
    try {
        JLongArrayAccessor values(env, j_date_array);
        JBooleanArrayAccessor not_null(env, j_not_null);
        auto collection = new std::vector<JavaValue>();
        for (int i = 0; i < values.size(); i++) {
            if (not_null[i]) {
                collection->push_back(JavaValue(from_milliseconds(values[i])));
            } else {
                collection->push_back(JavaValue());
            }
        }
        return reinterpret_cast<jlong>(collection);
    } CATCH_STD()

    return reinterpret_cast<jlong>(nullptr);
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_core_NativeMixedCollection_nativeCreateObjectIdCollection(JNIEnv *env, jclass,
                                                                                 jobjectArray j_object_id_array,
                                                                                 jbooleanArray j_not_null) {
    try {
        JObjectArrayAccessor<JStringAccessor, jstring> values(env, j_object_id_array);
        JBooleanArrayAccessor not_null(env, j_not_null);
        auto collection = new std::vector<JavaValue>();
        for (int i = 0; i < values.size(); i++) {
            if (not_null[i]) {
                collection->push_back(JavaValue(ObjectId(StringData(values[i]).data())));
            } else {
                collection->push_back(JavaValue());
            }
        }
        return reinterpret_cast<jlong>(collection);
    } CATCH_STD()

    return reinterpret_cast<jlong>(nullptr);
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_core_NativeMixedCollection_nativeCreateDecimal128Collection(JNIEnv *env, jclass,
                                                                                   jlongArray j_low_array,
                                                                                   jlongArray j_high_array,
                                                                                   jbooleanArray j_not_null) {
    try {
        JLongArrayAccessor low_values(env, j_low_array);
        JLongArrayAccessor high_values(env, j_high_array);
        JBooleanArrayAccessor not_null(env, j_not_null);

        auto collection = new std::vector<JavaValue>();
        for (int i = 0; i < low_values.size(); i++) {
            if (not_null[i]) {
                Decimal128::Bid128 raw{static_cast<uint64_t>(low_values[i]), static_cast<uint64_t>(high_values[i])};
                collection->push_back(JavaValue(Decimal128(raw)));
            } else {
                collection->push_back(JavaValue());
            }
        }
        return reinterpret_cast<jlong>(collection);
    } CATCH_STD()

    return reinterpret_cast<jlong>(nullptr);
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_core_NativeMixedCollection_nativeCreateUUIDCollection(JNIEnv *env, jclass,
                                                                             jobjectArray j_uuid_array,
                                                                             jbooleanArray j_not_null) {
    try {
        JObjectArrayAccessor<JStringAccessor, jstring> values(env, j_uuid_array);
        JBooleanArrayAccessor not_null(env, j_not_null);
        auto collection = new std::vector<JavaValue>();
        for (int i = 0; i < values.size(); i++) {
            if (not_null[i]) {
                collection->push_back(JavaValue(UUID(StringData(values[i]).data())));
            } else {
                collection->push_back(JavaValue());
            }
        }
        return reinterpret_cast<jlong>(collection);
    } CATCH_STD()

    return reinterpret_cast<jlong>(nullptr);
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_core_NativeMixedCollection_nativeCreateObjectCollection(JNIEnv *env, jclass,
                                                                             jlongArray j_object_array,
                                                                             jbooleanArray j_not_null) {
    try {
        JLongArrayAccessor values(env, j_object_array);
        JBooleanArrayAccessor not_null(env, j_not_null);
        auto collection = new std::vector<JavaValue>();
        for (int i = 0; i < values.size(); i++) {
            if (not_null[i]) {
                collection->push_back(JavaValue(reinterpret_cast<Obj*>(values[i])));
            } else {
                collection->push_back(JavaValue());
            }
        }
        return reinterpret_cast<jlong>(collection);
    } CATCH_STD()

    return reinterpret_cast<jlong>(nullptr);
}

JNIEXPORT jint JNICALL
Java_io_realm_internal_core_NativeMixedCollection_nativeGetCollectionSize(JNIEnv *env, jclass,
                                                                          jlong j_native_ptr) {
    try {
        auto &collection = *reinterpret_cast<std::vector<JavaValue> *>(j_native_ptr);
        return collection.size();
    } CATCH_STD()

    return 0;
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_core_NativeMixedCollection_nativeGetCollectionItem(JNIEnv *env, jclass,
                                                                          jlong j_native_ptr,
                                                                          jint j_index) {
    try {
        auto &collection = *reinterpret_cast<std::vector<JavaValue> *>(j_native_ptr);
        return reinterpret_cast<jlong>(&collection[j_index]);
    } CATCH_STD()

    return reinterpret_cast<jlong>(nullptr);
}
