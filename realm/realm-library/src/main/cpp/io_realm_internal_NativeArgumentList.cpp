/*
 * Copyright 2014 Realm Inc.
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

#include "io_realm_internal_NativeArgumentList.h"

#include <realm.hpp>
#include <realm/query_expression.hpp>
#include <realm/table.hpp>
#include <realm/parser/keypath_mapping.hpp>
#include <realm/parser/query_parser.hpp>

#include <realm/object-store/shared_realm.hpp>
#include <realm/object-store/object_store.hpp>
#include <realm/object-store/results.hpp>

#include "java_accessor.hpp"
#include "java_object_accessor.hpp"
#include "java_class_global_def.hpp"
#include "util.hpp"

using namespace realm;
using namespace realm::jni_util;
using namespace realm::_impl;

typedef std::vector<JavaValue> ArgumentList;

static void finalize_argument_list(jlong ptr)
{
    delete reinterpret_cast<ArgumentList*>(ptr);
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_NativeArgumentList_nativeGetFinalizerPtr(JNIEnv*, jclass)
{
    return reinterpret_cast<jlong>(&finalize_argument_list);
}



JNIEXPORT jlong JNICALL Java_io_realm_internal_NativeArgumentList_nativeCreate(JNIEnv* env, jclass)
{
    try {
        auto vector = new std::vector<JavaValue>();
        return reinterpret_cast<jlong>(vector);
    }
    CATCH_STD()
    return -1;
}

static inline long add_argument(jlong data_ptr, JavaValue const& value)
{
    ArgumentList* data = reinterpret_cast<ArgumentList*>(data_ptr);
    long size = data->size();
    (*data).push_back(std::move(value));

    return size;
}


JNIEXPORT jlong JNICALL Java_io_realm_internal_NativeArgumentList_nativeInsertNull
        (JNIEnv* env, jclass, jlong data_ptr)
{
    try {
        const JavaValue value = JavaValue();
        return add_argument(data_ptr, value);
    }
    CATCH_STD()

    return -1;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_NativeArgumentList_nativeInsertString
        (JNIEnv* env, jclass, jlong data_ptr, jstring j_value)
{
    try {
        JStringAccessor value(env, j_value);
        std::string string_value(value);
        const JavaValue wrapped_value(string_value);
        return add_argument(data_ptr, wrapped_value);
    }
    CATCH_STD()

    return -1;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_NativeArgumentList_nativeInsertInteger
        (JNIEnv* env, jclass, jlong data_ptr, jlong j_value)
{
    try {
        const JavaValue value(j_value);
        return add_argument(data_ptr, value);
    }
    CATCH_STD()

    return -1;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_NativeArgumentList_nativeInsertFloat
        (JNIEnv* env, jclass, jlong data_ptr, jfloat j_value)
{
    try {
        const JavaValue value(j_value);
        return add_argument(data_ptr, value);
    }
    CATCH_STD()

    return -1;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_NativeArgumentList_nativeInsertDouble
        (JNIEnv* env, jclass, jlong data_ptr, jdouble j_value)
{
    try {
        const JavaValue value(j_value);
        return add_argument(data_ptr, value);
    }
    CATCH_STD()

    return -1;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_NativeArgumentList_nativeInsertBoolean
        (JNIEnv* env, jclass, jlong data_ptr, jboolean j_value)
{
    try {
        const JavaValue value(j_value);
        return add_argument(data_ptr, value);
    }
    CATCH_STD()

    return -1;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_NativeArgumentList_nativeInsertByteArray
        (JNIEnv* env, jclass, jlong data_ptr, jbyteArray j_value)
{
    try {
        auto data = OwnedBinaryData(JByteArrayAccessor(env, j_value).transform<BinaryData>());
        const JavaValue value(data);
        return add_argument(data_ptr, value);
    }
    CATCH_STD()

    return -1;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_NativeArgumentList_nativeInsertDate
        (JNIEnv* env, jclass, jlong data_ptr, jlong j_value)
{
    try {
        const JavaValue value(from_milliseconds(j_value));
        return add_argument(data_ptr, value);
    }
    CATCH_STD()

    return -1;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_NativeArgumentList_nativeInsertDecimal128
        (JNIEnv* env, jclass, jlong data_ptr, jlong j_low_value, jlong j_high_value)
{
    try {
        Decimal128::Bid128 raw {static_cast<uint64_t>(j_low_value), static_cast<uint64_t>(j_high_value)};
        Decimal128 decimal128 = Decimal128(raw);
        const JavaValue value(decimal128);
        return add_argument(data_ptr, value);
    }
    CATCH_STD()

    return -1;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_NativeArgumentList_nativeInsertObjectId
        (JNIEnv* env, jclass, jlong data_ptr, jstring j_data)
{
    try {
        JStringAccessor data(env, j_data);
        ObjectId objectId = ObjectId(StringData(data).data());
        const JavaValue value(objectId);
        return add_argument(data_ptr, value);
    }
    CATCH_STD()

    return -1;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_NativeArgumentList_nativeInsertUUID
        (JNIEnv* env, jclass, jlong data_ptr, jstring j_data)
{
    try {
        JStringAccessor data(env, j_data);
        UUID uuid = UUID(StringData(data).data());
        const JavaValue value(uuid);
        return add_argument(data_ptr, value);
    }
    CATCH_STD()

    return -1;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_NativeArgumentList_nativeInsertObject
        (JNIEnv* env, jclass, jlong data_ptr, jlong row_ptr)
{
    try {
        const JavaValue value(reinterpret_cast<Obj*>(row_ptr));
        return add_argument(data_ptr, value);
    }
    CATCH_STD()

    return -1;
}
