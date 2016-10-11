/*
 * Copyright 2015 Realm Inc.
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
#include <exception>

#include "io_realm_internal_CheckedRow.h"
#include "io_realm_internal_UncheckedRow.h"

#include "util.hpp"

using namespace realm;

JNIEXPORT jlong JNICALL Java_io_realm_internal_CheckedRow_nativeGetColumnCount
  (JNIEnv* env, jobject obj, jlong nativeRowPtr)
{
    return try_catch<jlong>(env, [&]() {
        Row* row = ROW(nativeRowPtr);
        if (!row->is_attached())
            return static_cast<jlong>(0);

        return Java_io_realm_internal_UncheckedRow_nativeGetColumnCount(env, obj, nativeRowPtr);
    });
}

JNIEXPORT jstring JNICALL Java_io_realm_internal_CheckedRow_nativeGetColumnName
  (JNIEnv* env, jobject obj, jlong nativeRowPtr, jlong columnIndex)
{
    return try_catch<jstring>(env, [&]() {
        Row *row = ROW(nativeRowPtr);
        ROW_AND_COL_INDEX_VALID(env, row, columnIndex);
        return Java_io_realm_internal_UncheckedRow_nativeGetColumnName(env, obj, nativeRowPtr, columnIndex);
    });
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_CheckedRow_nativeGetColumnIndex
  (JNIEnv* env, jobject obj, jlong nativeRowPtr, jstring columnName)
{
    return try_catch<jlong>(env, [&]() {
        Row *row = ROW(nativeRowPtr);
        if (!row->is_attached())
            return static_cast<jlong>(0);

        jlong ndx = Java_io_realm_internal_UncheckedRow_nativeGetColumnIndex(env, obj, nativeRowPtr, columnName);
        if (ndx == to_jlong_or_not_found(realm::not_found)) {
            JStringAccessor column_name(env, columnName);
            throw std::invalid_argument(concat_stringdata("Field not found: ", column_name));
        }
        else {
            return ndx;
        }
    });
}

JNIEXPORT jint JNICALL Java_io_realm_internal_CheckedRow_nativeGetColumnType
  (JNIEnv* env, jobject obj, jlong nativeRowPtr, jlong columnIndex)
{
    return try_catch<jint>(env, [&]() {
        ROW_AND_COL_INDEX_VALID(env, ROW(nativeRowPtr), columnIndex);
        return Java_io_realm_internal_UncheckedRow_nativeGetColumnType(env, obj, nativeRowPtr, columnIndex);
    });
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_CheckedRow_nativeGetLong
  (JNIEnv* env, jobject obj, jlong nativeRowPtr, jlong columnIndex)
{
    return try_catch<jlong>(env, [&]() {
        ROW_AND_COL_INDEX_AND_TYPE_VALID(env, ROW(nativeRowPtr), columnIndex, type_Int);
        return Java_io_realm_internal_UncheckedRow_nativeGetLong(env, obj, nativeRowPtr, columnIndex);
    });
}

JNIEXPORT jboolean JNICALL Java_io_realm_internal_CheckedRow_nativeGetBoolean
  (JNIEnv* env, jobject obj, jlong nativeRowPtr, jlong columnIndex)
{
    return try_catch<jboolean>(env, [&]() {
        ROW_AND_COL_INDEX_AND_TYPE_VALID(env, ROW(nativeRowPtr), columnIndex, type_Bool);
        return Java_io_realm_internal_UncheckedRow_nativeGetBoolean(env, obj, nativeRowPtr, columnIndex);
    });
}

JNIEXPORT jfloat JNICALL Java_io_realm_internal_CheckedRow_nativeGetFloat
  (JNIEnv* env, jobject obj, jlong nativeRowPtr, jlong columnIndex)
{
    return try_catch<jfloat>(env, [&]() {
        ROW_AND_COL_INDEX_AND_TYPE_VALID(env, ROW(nativeRowPtr), columnIndex, type_Float);
        return Java_io_realm_internal_UncheckedRow_nativeGetFloat(env, obj, nativeRowPtr, columnIndex);
    });
}

JNIEXPORT jdouble JNICALL Java_io_realm_internal_CheckedRow_nativeGetDouble
  (JNIEnv* env, jobject obj, jlong nativeRowPtr, jlong columnIndex)
{
    return try_catch<jdouble>(env, [&]() {
        ROW_AND_COL_INDEX_AND_TYPE_VALID(env, ROW(nativeRowPtr), columnIndex, type_Double);
        return Java_io_realm_internal_UncheckedRow_nativeGetDouble(env, obj, nativeRowPtr, columnIndex);
    });
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_CheckedRow_nativeGetTimestamp
  (JNIEnv* env, jobject obj, jlong nativeRowPtr, jlong columnIndex)
{
    return try_catch<jlong>(env, [&]() {
        ROW_AND_COL_INDEX_AND_TYPE_VALID(env, ROW(nativeRowPtr), columnIndex, type_Timestamp);
        return Java_io_realm_internal_UncheckedRow_nativeGetTimestamp(env, obj, nativeRowPtr, columnIndex);
    });
}

JNIEXPORT jstring JNICALL Java_io_realm_internal_CheckedRow_nativeGetString
  (JNIEnv* env, jobject obj, jlong nativeRowPtr, jlong columnIndex)
{
    return try_catch<jstring>(env, [&]() {
        ROW_AND_COL_INDEX_AND_TYPE_VALID(env, ROW(nativeRowPtr), columnIndex, type_String);
        return Java_io_realm_internal_UncheckedRow_nativeGetString(env, obj, nativeRowPtr, columnIndex);
    });
}

JNIEXPORT jbyteArray JNICALL Java_io_realm_internal_CheckedRow_nativeGetByteArray
  (JNIEnv* env, jobject obj, jlong nativeRowPtr, jlong columnIndex)
{
    return try_catch<jbyteArray>(env, [&]() {
        ROW_AND_COL_INDEX_AND_TYPE_VALID(env, ROW(nativeRowPtr), columnIndex, type_Binary);
        return Java_io_realm_internal_UncheckedRow_nativeGetByteArray(env, obj, nativeRowPtr, columnIndex);
    });
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_CheckedRow_nativeGetLink
  (JNIEnv* env, jobject obj, jlong nativeRowPtr, jlong columnIndex)
{
    return try_catch<jlong>(env, [&]() {
        ROW_AND_COL_INDEX_AND_TYPE_VALID(env, ROW(nativeRowPtr), columnIndex, type_Link);
        return Java_io_realm_internal_UncheckedRow_nativeGetLink(env, obj, nativeRowPtr, columnIndex);
    });
}

JNIEXPORT jboolean JNICALL Java_io_realm_internal_CheckedRow_nativeIsNullLink
  (JNIEnv* env, jobject obj, jlong nativeRowPtr, jlong columnIndex)
{
    return try_catch<jboolean>(env, [&]() {
        ROW_AND_COL_INDEX_AND_TYPE_VALID(env, ROW(nativeRowPtr), columnIndex, type_Link);
        return Java_io_realm_internal_UncheckedRow_nativeIsNullLink(env, obj, nativeRowPtr, columnIndex);
    });
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_CheckedRow_nativeGetLinkView
  (JNIEnv* env, jobject obj, jlong nativeRowPtr, jlong columnIndex)
{
    return try_catch<jlong>(env, [&]() {
        ROW_AND_COL_INDEX_AND_TYPE_VALID(env, ROW(nativeRowPtr), columnIndex, type_LinkList);
        return Java_io_realm_internal_UncheckedRow_nativeGetLinkView(env, obj, nativeRowPtr, columnIndex);
    });
}

JNIEXPORT void JNICALL Java_io_realm_internal_CheckedRow_nativeSetLong
  (JNIEnv* env, jobject obj, jlong nativeRowPtr, jlong columnIndex, jlong value)
{
    try_catch<void>(env, [&]() {
        ROW_AND_COL_INDEX_AND_TYPE_VALID(env, ROW(nativeRowPtr), columnIndex, type_Int);
        Java_io_realm_internal_UncheckedRow_nativeSetLong(env, obj, nativeRowPtr, columnIndex, value);
    });
}

JNIEXPORT void JNICALL Java_io_realm_internal_CheckedRow_nativeSetBoolean
  (JNIEnv* env, jobject obj, jlong nativeRowPtr, jlong columnIndex, jboolean value)
{
    try_catch<void>(env, [&]() {
        ROW_AND_COL_INDEX_AND_TYPE_VALID(env, ROW(nativeRowPtr), columnIndex, type_Bool);
        Java_io_realm_internal_UncheckedRow_nativeSetBoolean(env, obj, nativeRowPtr, columnIndex, value);
    });
}

JNIEXPORT void JNICALL Java_io_realm_internal_CheckedRow_nativeSetFloat
  (JNIEnv* env, jobject obj, jlong nativeRowPtr, jlong columnIndex, jfloat value) {
    try_catch<void>(env, [&]() {
        ROW_AND_COL_INDEX_AND_TYPE_VALID(env, ROW(nativeRowPtr), columnIndex, type_Float);
        Java_io_realm_internal_UncheckedRow_nativeSetFloat(env, obj, nativeRowPtr, columnIndex, value);
    });
}

JNIEXPORT void JNICALL Java_io_realm_internal_CheckedRow_nativeSetDouble
  (JNIEnv* env, jobject obj, jlong nativeRowPtr, jlong columnIndex, jdouble value) {
    try_catch<void>(env, [&]() {
        ROW_AND_COL_INDEX_AND_TYPE_VALID(env, ROW(nativeRowPtr), columnIndex, type_Double);
        Java_io_realm_internal_UncheckedRow_nativeSetDouble(env, obj, nativeRowPtr, columnIndex, value);
    });
}

JNIEXPORT void JNICALL Java_io_realm_internal_CheckedRow_nativeSetTimestamp
  (JNIEnv* env, jobject obj, jlong nativeRowPtr, jlong columnIndex, jlong value) {
    try_catch<void>(env, [&]() {
        ROW_AND_COL_INDEX_AND_TYPE_VALID(env, ROW(nativeRowPtr), columnIndex, type_Timestamp);
        Java_io_realm_internal_UncheckedRow_nativeSetTimestamp(env, obj, nativeRowPtr, columnIndex, value);
    });
}

JNIEXPORT void JNICALL Java_io_realm_internal_CheckedRow_nativeSetString
  (JNIEnv* env, jobject obj, jlong nativeRowPtr, jlong columnIndex, jstring value) {
    try_catch<void>(env, [&]() {
        ROW_AND_COL_INDEX_AND_TYPE_VALID(env, ROW(nativeRowPtr), columnIndex, type_String);
        Java_io_realm_internal_UncheckedRow_nativeSetString(env, obj, nativeRowPtr, columnIndex, value);
    });
}

JNIEXPORT void JNICALL Java_io_realm_internal_CheckedRow_nativeSetByteArray
  (JNIEnv* env, jobject obj, jlong nativeRowPtr, jlong columnIndex, jbyteArray value) {
    try_catch<void>(env, [&]() {
        ROW_AND_COL_INDEX_AND_TYPE_VALID(env, ROW(nativeRowPtr), columnIndex, type_Binary);
        Java_io_realm_internal_UncheckedRow_nativeSetByteArray(env, obj, nativeRowPtr, columnIndex, value);
    });
}

JNIEXPORT void JNICALL Java_io_realm_internal_CheckedRow_nativeSetLink
  (JNIEnv* env, jobject obj, jlong nativeRowPtr, jlong columnIndex, jlong value) {
    try_catch<void>(env, [&]() {
        ROW_AND_COL_INDEX_AND_TYPE_VALID(env, ROW(nativeRowPtr), columnIndex, type_Link);
        Java_io_realm_internal_UncheckedRow_nativeSetLink(env, obj, nativeRowPtr, columnIndex, value);
    });
}

JNIEXPORT void JNICALL Java_io_realm_internal_CheckedRow_nativeNullifyLink
  (JNIEnv* env, jobject obj, jlong nativeRowPtr, jlong columnIndex) {
    try_catch<void>(env, [&]() {
        ROW_AND_COL_INDEX_AND_TYPE_VALID(env, ROW(nativeRowPtr), columnIndex, type_Link);
        Java_io_realm_internal_UncheckedRow_nativeNullifyLink(env, obj, nativeRowPtr, columnIndex);
    });
}
