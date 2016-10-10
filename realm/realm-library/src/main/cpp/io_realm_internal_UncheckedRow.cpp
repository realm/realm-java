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

#include <exception>

#include <realm/row.hpp>

#include "io_realm_internal_UncheckedRow.h"
#include "util.hpp"

using namespace realm;

JNIEXPORT jlong JNICALL Java_io_realm_internal_UncheckedRow_nativeGetColumnCount
  (JNIEnv *env, jobject, jlong nativeRowPtr)
{
    TR_ENTER_PTR(env, nativeRowPtr)
    return try_catch<jlong>(env, [&]() {
        Row* row = reinterpret_cast<Row*>(nativeRowPtr);
        if (!row->is_attached()) {
            return static_cast<jlong>(0);
        }
        return static_cast<jlong>(row->get_column_count());
    });
}

JNIEXPORT jstring JNICALL Java_io_realm_internal_UncheckedRow_nativeGetColumnName
  (JNIEnv* env, jobject, jlong nativeRowPtr, jlong columnIndex)
{
    TR_ENTER_PTR(env, nativeRowPtr)
    return try_catch<jstring>(env, [&]() {
        ROW_VALID(env, ROW(nativeRowPtr));
        return to_jstring(env, ROW(nativeRowPtr)->get_column_name(S(columnIndex)));
    });
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_UncheckedRow_nativeGetColumnIndex
  (JNIEnv* env, jobject, jlong nativeRowPtr, jstring columnName)
{
    TR_ENTER_PTR(env, nativeRowPtr)
    return try_catch<jlong>(env, [&]() {
        if (!ROW(nativeRowPtr)->is_attached()) {
            return static_cast<jlong>(0);
        }
        JStringAccessor columnName2(env, columnName);
        return to_jlong_or_not_found(ROW(nativeRowPtr)->get_column_index(columnName2));
    });
}

JNIEXPORT jint JNICALL Java_io_realm_internal_UncheckedRow_nativeGetColumnType
  (JNIEnv* env, jobject, jlong nativeRowPtr, jlong columnIndex)
{
    TR_ENTER_PTR(env, nativeRowPtr)
    return try_catch<jint>(env, [&]() {
        return static_cast<jint>(ROW(nativeRowPtr)->get_column_type(S(columnIndex)));
    });
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_UncheckedRow_nativeGetIndex
  (JNIEnv* env, jobject, jlong nativeRowPtr)
{
    TR_ENTER_PTR(env, nativeRowPtr)
    return try_catch<jlong>(env, [&]() {
        ROW_VALID(env, ROW(nativeRowPtr));
        return ROW(nativeRowPtr)->get_index();
    });
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_UncheckedRow_nativeGetLong
  (JNIEnv* env, jobject, jlong nativeRowPtr, jlong columnIndex)
{
    TR_ENTER_PTR(env, nativeRowPtr)
    return try_catch<jlong>(env, [&]() {
        ROW_VALID(env, ROW(nativeRowPtr));
        return ROW(nativeRowPtr)->get_int(S(columnIndex));
    });
}

JNIEXPORT jboolean JNICALL Java_io_realm_internal_UncheckedRow_nativeGetBoolean
  (JNIEnv* env, jobject, jlong nativeRowPtr, jlong columnIndex)
{
    TR_ENTER_PTR(env, nativeRowPtr)
    return try_catch<jboolean>(env, [&]() {
        ROW_VALID(env, ROW(nativeRowPtr));
        return ROW(nativeRowPtr)->get_bool(S(columnIndex));
    });
}

JNIEXPORT jfloat JNICALL Java_io_realm_internal_UncheckedRow_nativeGetFloat
  (JNIEnv* env, jobject, jlong nativeRowPtr, jlong columnIndex)
{
    TR_ENTER_PTR(env, nativeRowPtr)
    return try_catch<jfloat>(env, [&]() {
        ROW_VALID(env, ROW(nativeRowPtr));
        return ROW(nativeRowPtr)->get_float(S(columnIndex));
    });
}

JNIEXPORT jdouble JNICALL Java_io_realm_internal_UncheckedRow_nativeGetDouble
  (JNIEnv* env, jobject, jlong nativeRowPtr, jlong columnIndex)
{
    TR_ENTER_PTR(env, nativeRowPtr)
    return try_catch<jdouble>(env, [&]() {
        ROW_VALID(env, ROW(nativeRowPtr));
        return ROW(nativeRowPtr)->get_double(S(columnIndex));
    });
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_UncheckedRow_nativeGetTimestamp
  (JNIEnv* env, jobject, jlong nativeRowPtr, jlong columnIndex)
{
    TR_ENTER_PTR(env, nativeRowPtr)
    return try_catch<jlong>(env, [&]() {
        ROW_VALID(env, ROW(nativeRowPtr));
        return to_milliseconds(ROW(nativeRowPtr)->get_timestamp(S(columnIndex)));
    });
}

JNIEXPORT jstring JNICALL Java_io_realm_internal_UncheckedRow_nativeGetString
  (JNIEnv* env, jobject, jlong nativeRowPtr, jlong columnIndex)
{
    TR_ENTER_PTR(env, nativeRowPtr)
    return try_catch<jstring>(env, [&]() {
        ROW_VALID(env, ROW(nativeRowPtr));
        StringData value = ROW(nativeRowPtr)->get_string(S(columnIndex));
        return to_jstring(env,  value);
    });
}

JNIEXPORT jbyteArray JNICALL Java_io_realm_internal_UncheckedRow_nativeGetByteArray
  (JNIEnv* env, jobject, jlong nativeRowPtr, jlong columnIndex)
{
    TR_ENTER_PTR(env, nativeRowPtr)
    return try_catch<jbyteArray>(env, [&]() {
        ROW_VALID(env, ROW(nativeRowPtr));

        BinaryData bin = ROW(nativeRowPtr)->get_binary( S(columnIndex) );
        if (bin.is_null()) {
            return static_cast<jbyteArray>(nullptr);
        }
        else if (bin.size() <= MAX_JSIZE) {
            jbyteArray jresult = env->NewByteArray(static_cast<jsize>(bin.size()));
            if (jresult)
                env->SetByteArrayRegion(jresult, 0, static_cast<jsize>(bin.size()), reinterpret_cast<const jbyte*>(bin.data()));
            return jresult;
        }
        else {
            throw std::invalid_argument("Length of ByteArray is larger than an Int.");
        }
    });
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_UncheckedRow_nativeGetLink
  (JNIEnv* env, jobject, jlong nativeRowPtr, jlong columnIndex)
{
    TR_ENTER_PTR(env, nativeRowPtr)
    return try_catch<jlong>(env, [&]() {
        ROW_VALID(env, ROW(nativeRowPtr));
        if (ROW(nativeRowPtr)->is_null_link(S(columnIndex))) {
            return static_cast<jlong>(-1);
        }
        return static_cast<jlong>(ROW(nativeRowPtr)->get_link(S(columnIndex)));
    });
}

JNIEXPORT jboolean JNICALL Java_io_realm_internal_UncheckedRow_nativeIsNullLink
  (JNIEnv* env, jobject, jlong nativeRowPtr, jlong columnIndex)
{
    TR_ENTER_PTR(env, nativeRowPtr)
    return try_catch<jboolean>(env, [&]() {
        ROW_VALID(env, ROW(nativeRowPtr));
        return static_cast<jboolean>(ROW(nativeRowPtr)->is_null_link(S(columnIndex)));
    });
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_UncheckedRow_nativeGetLinkView
  (JNIEnv* env, jclass, jlong nativeRowPtr, jlong columnIndex)
{
    TR_ENTER_PTR(env, nativeRowPtr)
    return try_catch<jlong>(env, [&]() {
        ROW_VALID(env, ROW(nativeRowPtr));
        LinkViewRef* link_view_ptr = const_cast<LinkViewRef*>(&(LangBindHelper::get_linklist_ptr(*ROW(nativeRowPtr), S(columnIndex))));
        return reinterpret_cast<jlong>(link_view_ptr);
    });
}

JNIEXPORT void JNICALL Java_io_realm_internal_UncheckedRow_nativeSetLong
  (JNIEnv* env, jobject, jlong nativeRowPtr, jlong columnIndex, jlong value)
{
    TR_ENTER_PTR(env, nativeRowPtr)
    try_catch<void>(env, [&]() {
        ROW_VALID(env, ROW(nativeRowPtr));
        ROW(nativeRowPtr)->set_int(S(columnIndex), value);
    });
}

JNIEXPORT void JNICALL Java_io_realm_internal_UncheckedRow_nativeSetBoolean
  (JNIEnv* env, jobject, jlong nativeRowPtr, jlong columnIndex, jboolean value)
{
    TR_ENTER_PTR(env, nativeRowPtr)
    try_catch<void>(env, [&]() {
        ROW_VALID(env, ROW(nativeRowPtr));
        ROW(nativeRowPtr)->set_bool(S(columnIndex), value);
    });
}

JNIEXPORT void JNICALL Java_io_realm_internal_UncheckedRow_nativeSetFloat
  (JNIEnv* env, jobject, jlong nativeRowPtr, jlong columnIndex, jfloat value)
{
    TR_ENTER_PTR(env, nativeRowPtr)
    try_catch<void>(env, [&]() {
        ROW_VALID(env, ROW(nativeRowPtr));
        ROW(nativeRowPtr)->set_float(S(columnIndex), value);
    });
}

JNIEXPORT void JNICALL Java_io_realm_internal_UncheckedRow_nativeSetDouble
  (JNIEnv* env, jobject, jlong nativeRowPtr, jlong columnIndex, jdouble value)
{
    TR_ENTER_PTR(env, nativeRowPtr)
    try_catch<void>(env, [&]() {
        ROW_VALID(env, ROW(nativeRowPtr));
        ROW(nativeRowPtr)->set_double(S(columnIndex), value);
    });
}

JNIEXPORT void JNICALL Java_io_realm_internal_UncheckedRow_nativeSetTimestamp
  (JNIEnv* env, jobject, jlong nativeRowPtr, jlong columnIndex, jlong value)
{
    TR_ENTER_PTR(env, nativeRowPtr)
    try_catch<void>(env, [&]() {
        ROW_VALID(env, ROW(nativeRowPtr));
        ROW(nativeRowPtr)->set_timestamp(S(columnIndex), from_milliseconds(value));
    });
}

JNIEXPORT void JNICALL Java_io_realm_internal_UncheckedRow_nativeSetString
  (JNIEnv* env, jobject, jlong nativeRowPtr, jlong columnIndex, jstring value)
{
    TR_ENTER_PTR(env, nativeRowPtr)
    try_catch<void>(env, [&]() {
        ROW_VALID(env, ROW(nativeRowPtr));

        if ((value == NULL) && !(ROW(nativeRowPtr)->get_table()->is_nullable(S(columnIndex)))) {
            throw null_value(ROW(nativeRowPtr)->get_table(), S(columnIndex));
        }
        JStringAccessor value2(env, value);
        ROW(nativeRowPtr)->set_string(S(columnIndex), value2);
    });
}

JNIEXPORT void JNICALL Java_io_realm_internal_UncheckedRow_nativeSetByteArray
  (JNIEnv* env, jobject, jlong nativeRowPtr, jlong columnIndex, jbyteArray value)
{
    TR_ENTER_PTR(env, nativeRowPtr)
    try_catch<void>(env, [&]() {
        ROW_VALID(env, ROW(nativeRowPtr));

        jbyte* bytePtr = NULL;
        if (value == NULL) {
            if (!(ROW(nativeRowPtr)->get_table()->is_nullable(S(columnIndex)))) {
                throw null_value(ROW(nativeRowPtr)->get_table(), S(columnIndex));
            }
            ROW(nativeRowPtr)->set_binary(S(columnIndex), BinaryData());
        }
        else {
            bytePtr = env->GetByteArrayElements(value, NULL);
            if (!bytePtr) {
                throw std::invalid_argument("doByteArray");
            }
            size_t dataLen = S(env->GetArrayLength(value));
            ROW(nativeRowPtr)->set_binary( S(columnIndex), BinaryData(reinterpret_cast<char*>(bytePtr), dataLen));
        }

        if (bytePtr) {
            env->ReleaseByteArrayElements(value, bytePtr, JNI_ABORT);
        }
    });
}

JNIEXPORT void JNICALL Java_io_realm_internal_UncheckedRow_nativeSetLink
  (JNIEnv* env, jobject, jlong nativeRowPtr, jlong columnIndex, jlong value)
{
    TR_ENTER_PTR(env, nativeRowPtr)
    try_catch<void>(env, [&]() {
        ROW_VALID(env, ROW(nativeRowPtr));
        ROW(nativeRowPtr)->set_link(S(columnIndex), value);
    });
}

JNIEXPORT void JNICALL Java_io_realm_internal_UncheckedRow_nativeNullifyLink
  (JNIEnv* env, jobject, jlong nativeRowPtr, jlong columnIndex)
{
    TR_ENTER_PTR(env, nativeRowPtr)
    try_catch<void>(env, [&]() {
        ROW_VALID(env, ROW(nativeRowPtr));
        ROW(nativeRowPtr)->nullify_link(S(columnIndex));
    });
}

JNIEXPORT void JNICALL Java_io_realm_internal_UncheckedRow_nativeClose
  (JNIEnv* env, jclass, jlong nativeRowPtr)
{
    TR_ENTER_PTR(env, nativeRowPtr)
    delete ROW(nativeRowPtr);
}

JNIEXPORT jboolean JNICALL Java_io_realm_internal_UncheckedRow_nativeIsAttached
  (JNIEnv* env, jobject, jlong nativeRowPtr)
{
    TR_ENTER_PTR(env, nativeRowPtr)
    return try_catch<jboolean>(env, [&]() {
        return ROW(nativeRowPtr)->is_attached();
    });
}

JNIEXPORT jboolean JNICALL Java_io_realm_internal_UncheckedRow_nativeHasColumn
  (JNIEnv* env, jobject obj, jlong nativeRowPtr, jstring columnName)
{
    return try_catch<jboolean>(env, [&]() {
        jlong ndx = Java_io_realm_internal_UncheckedRow_nativeGetColumnIndex(env, obj, nativeRowPtr, columnName);
        return ndx != to_jlong_or_not_found(realm::not_found);
    });
}

JNIEXPORT jboolean JNICALL Java_io_realm_internal_UncheckedRow_nativeIsNull
  (JNIEnv* env, jobject, jlong nativeRowPtr, jlong columnIndex) {
    TR_ENTER_PTR(env, nativeRowPtr)
    return try_catch<jboolean>(env, [&]() {
        return ROW(nativeRowPtr)->is_null(columnIndex);
    });
}

JNIEXPORT void JNICALL Java_io_realm_internal_UncheckedRow_nativeSetNull
  (JNIEnv *env, jobject, jlong nativeRowPtr, jlong columnIndex) {
    TR_ENTER_PTR(env, nativeRowPtr)
    try_catch<void>(env, [&]() {
        ROW_VALID(env, ROW(nativeRowPtr));
        TBL_AND_COL_NULLABLE(env, ROW(nativeRowPtr)->get_table(), columnIndex);
        ROW(nativeRowPtr)->set_null(columnIndex);
    });
}
