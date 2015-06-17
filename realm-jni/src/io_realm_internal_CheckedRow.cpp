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

#include "io_realm_internal_CheckedRow.h"
#include "io_realm_internal_Row.h"

#include "util.hpp"
#include "mixedutil.hpp"
#include "tablebase_tpl.hpp"

using namespace realm;

JNIEXPORT jlong JNICALL Java_io_realm_internal_CheckedRow_nativeGetColumnCount
  (JNIEnv* env, jobject obj, jlong nativeRowPtr)
{
    TR_ENTER_PTR(nativeRowPtr)
    if (!ROW(nativeRowPtr)->is_attached())
        return 0;

    return Java_io_realm_internal_Row_nativeGetColumnCount(env, obj, nativeRowPtr);
}

JNIEXPORT jstring JNICALL Java_io_realm_internal_CheckedRow_nativeGetColumnName
  (JNIEnv* env, jobject obj, jlong nativeRowPtr, jlong columnIndex)
{
    TR_ENTER_PTR(nativeRowPtr)
    if (!ROW_AND_COL_INDEX_VALID(env, ROW(nativeRowPtr), columnIndex))
        return NULL;
    try {
        return Java_io_realm_internal_Row_nativeGetColumnName(env, obj, nativeRowPtr, columnIndex);
    } CATCH_STD();
    return NULL;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_CheckedRow_nativeGetColumnIndex
  (JNIEnv* env, jobject obj, jlong nativeRowPtr, jstring columnName)
{
    TR_ENTER_PTR(nativeRowPtr)
    if (!ROW(nativeRowPtr)->is_attached())
        return 0;
    try {
        return Java_io_realm_internal_Row_nativeGetColumnIndex(env, obj, nativeRowPtr, columnName);
    } CATCH_STD()
    return 0;
}

JNIEXPORT jint JNICALL Java_io_realm_internal_CheckedRow_nativeGetColumnType
  (JNIEnv* env, jobject obj, jlong nativeRowPtr, jlong columnIndex)
{
    TR_ENTER_PTR(nativeRowPtr)
    if (!ROW_AND_COL_INDEX_VALID(env, ROW(nativeRowPtr), columnIndex))
        return 0;

    return Java_io_realm_internal_Row_nativeGetColumnType(env, obj, nativeRowPtr, columnIndex);
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_CheckedRow_nativeGetIndex
  (JNIEnv* env, jobject obj, jlong nativeRowPtr)
{
    TR_ENTER_PTR(nativeRowPtr)
    if (!ROW_VALID(env, ROW(nativeRowPtr)))
        return 0;

    return Java_io_realm_internal_Row_nativeGetIndex(env, obj, nativeRowPtr);
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_CheckedRow_nativeGetLong
  (JNIEnv* env, jobject obj, jlong nativeRowPtr, jlong columnIndex)
{
    TR_ENTER_PTR(nativeRowPtr)
    if (!ROW_AND_COL_INDEX_AND_TYPE_VALID(env, ROW(nativeRowPtr), columnIndex, type_Int))
        return 0;

    return Java_io_realm_internal_Row_nativeGetLong(env, obj, nativeRowPtr, columnIndex);
}

JNIEXPORT jboolean JNICALL Java_io_realm_internal_CheckedRow_nativeGetBoolean
  (JNIEnv* env, jobject obj, jlong nativeRowPtr, jlong columnIndex)
{
    TR_ENTER_PTR(nativeRowPtr)
    if (!ROW_AND_COL_INDEX_AND_TYPE_VALID(env, ROW(nativeRowPtr), columnIndex, type_Bool))
        return 0;

    return Java_io_realm_internal_Row_nativeGetBoolean(env, obj, nativeRowPtr, columnIndex);
}

JNIEXPORT jfloat JNICALL Java_io_realm_internal_CheckedRow_nativeGetFloat
  (JNIEnv* env, jobject obj, jlong nativeRowPtr, jlong columnIndex)
{
    TR_ENTER_PTR(nativeRowPtr)
    if (!ROW_AND_COL_INDEX_AND_TYPE_VALID(env, ROW(nativeRowPtr), columnIndex, type_Float))
        return 0;

    return Java_io_realm_internal_Row_nativeGetFloat(env, obj, nativeRowPtr, columnIndex);
}

JNIEXPORT jdouble JNICALL Java_io_realm_internal_CheckedRow_nativeGetDouble
  (JNIEnv* env, jobject obj, jlong nativeRowPtr, jlong columnIndex)
{
    TR_ENTER_PTR(nativeRowPtr)
    if (!ROW_AND_COL_INDEX_AND_TYPE_VALID(env, ROW(nativeRowPtr), columnIndex, type_Double))
        return 0;

    return Java_io_realm_internal_Row_nativeGetDouble(env, obj, nativeRowPtr, columnIndex);
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_CheckedRow_nativeGetDateTime
  (JNIEnv* env, jobject obj, jlong nativeRowPtr, jlong columnIndex)
{
    TR_ENTER_PTR(nativeRowPtr)
    if (!ROW_AND_COL_INDEX_AND_TYPE_VALID(env, ROW(nativeRowPtr), columnIndex, type_DateTime))
        return 0;

    return Java_io_realm_internal_Row_nativeGetDateTime(env, obj, nativeRowPtr, columnIndex);
}

JNIEXPORT jstring JNICALL Java_io_realm_internal_CheckedRow_nativeGetString
  (JNIEnv* env, jobject obj, jlong nativeRowPtr, jlong columnIndex)
{
    TR_ENTER_PTR(nativeRowPtr)
    if (!ROW_AND_COL_INDEX_AND_TYPE_VALID(env, ROW(nativeRowPtr), columnIndex, type_String))
        return 0;

    try {
        return Java_io_realm_internal_Row_nativeGetString(env, obj, nativeRowPtr, columnIndex);
    } CATCH_STD()
    return NULL;
}

JNIEXPORT jbyteArray JNICALL Java_io_realm_internal_CheckedRow_nativeGetByteArray
  (JNIEnv* env, jobject obj, jlong nativeRowPtr, jlong columnIndex)
{
    TR_ENTER_PTR(nativeRowPtr)
    if (!ROW_AND_COL_INDEX_AND_TYPE_VALID(env, ROW(nativeRowPtr), columnIndex, type_Binary))
        return 0;

    return Java_io_realm_internal_Row_nativeGetByteArray(env, obj, nativeRowPtr, columnIndex);
}

JNIEXPORT jint JNICALL Java_io_realm_internal_CheckedRow_nativeGetMixedType
  (JNIEnv* env, jobject obj, jlong nativeRowPtr, jlong columnIndex)
{
    TR_ENTER_PTR(nativeRowPtr)
    if (!ROW_AND_COL_INDEX_AND_TYPE_VALID(env, ROW(nativeRowPtr), columnIndex, type_Mixed))
        return 0;

    return Java_io_realm_internal_Row_nativeGetMixedType(env, obj, nativeRowPtr, columnIndex);
}

JNIEXPORT jobject JNICALL Java_io_realm_internal_CheckedRow_nativeGetMixed
  (JNIEnv* env, jobject obj, jlong nativeRowPtr, jlong columnIndex)
{
    TR_ENTER_PTR(nativeRowPtr)
    if (!ROW_AND_COL_INDEX_AND_TYPE_VALID(env, ROW(nativeRowPtr), columnIndex, type_Mixed))
        return NULL;

    return Java_io_realm_internal_Row_nativeGetMixed(env, obj, nativeRowPtr, columnIndex);
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_CheckedRow_nativeGetLink
  (JNIEnv* env, jobject obj, jlong nativeRowPtr, jlong columnIndex)
{
    TR_ENTER_PTR(nativeRowPtr)
    if (!ROW_AND_COL_INDEX_AND_TYPE_VALID(env, ROW(nativeRowPtr), columnIndex, type_Link))
        return 0;
    if (ROW(nativeRowPtr)->is_null_link( S(columnIndex) )) {
        return jlong(-1);
    }
    return Java_io_realm_internal_Row_nativeGetLink(env, obj, nativeRowPtr, columnIndex);
}

JNIEXPORT jboolean JNICALL Java_io_realm_internal_CheckedRow_nativeIsNullLink
  (JNIEnv* env, jobject obj, jlong nativeRowPtr, jlong columnIndex)
{
    TR_ENTER_PTR(nativeRowPtr)
    if (!ROW_AND_COL_INDEX_AND_TYPE_VALID(env, ROW(nativeRowPtr), columnIndex, type_Link))
        return 0;

    return Java_io_realm_internal_Row_nativeIsNullLink(env, obj, nativeRowPtr, columnIndex);
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_CheckedRow_nativeGetLinkView
  (JNIEnv* env, jobject obj, jlong nativeRowPtr, jlong columnIndex)
{
    TR_ENTER_PTR(nativeRowPtr)
    if (!ROW_AND_COL_INDEX_AND_TYPE_VALID(env, ROW(nativeRowPtr), columnIndex, type_LinkList))
        return 0;

    return Java_io_realm_internal_Row_nativeGetLinkView(env, obj, nativeRowPtr, columnIndex);
}

JNIEXPORT void JNICALL Java_io_realm_internal_CheckedRow_nativeSetLong
  (JNIEnv* env, jobject obj, jlong nativeRowPtr, jlong columnIndex, jlong value)
{
    TR_ENTER_PTR(nativeRowPtr)
    if (!ROW_AND_COL_INDEX_AND_TYPE_VALID(env, ROW(nativeRowPtr), columnIndex, type_Int))
        return;

    Java_io_realm_internal_Row_nativeSetLong(env, obj, nativeRowPtr, columnIndex, value);
}

JNIEXPORT void JNICALL Java_io_realm_internal_CheckedRow_nativeSetBoolean
  (JNIEnv* env, jobject obj, jlong nativeRowPtr, jlong columnIndex, jboolean value)
{
    TR_ENTER_PTR(nativeRowPtr)
    if (!ROW_AND_COL_INDEX_AND_TYPE_VALID(env, ROW(nativeRowPtr), columnIndex, type_Bool))
        return;

    Java_io_realm_internal_Row_nativeSetBoolean(env, obj, nativeRowPtr, columnIndex, value);
}

JNIEXPORT void JNICALL Java_io_realm_internal_CheckedRow_nativeSetFloat
  (JNIEnv* env, jobject obj, jlong nativeRowPtr, jlong columnIndex, jfloat value)
{
    TR_ENTER_PTR(nativeRowPtr)
    if (!ROW_AND_COL_INDEX_AND_TYPE_VALID(env, ROW(nativeRowPtr), columnIndex, type_Float))
        return;

    Java_io_realm_internal_Row_nativeSetFloat(env, obj, nativeRowPtr, columnIndex, value);
}

JNIEXPORT void JNICALL Java_io_realm_internal_CheckedRow_nativeSetDouble
  (JNIEnv* env, jobject obj, jlong nativeRowPtr, jlong columnIndex, jdouble value)
{
    TR_ENTER_PTR(nativeRowPtr)
    if (!ROW_AND_COL_INDEX_AND_TYPE_VALID(env, ROW(nativeRowPtr), columnIndex, type_Double))
        return;

    Java_io_realm_internal_Row_nativeSetDouble(env, obj, nativeRowPtr, columnIndex, value);
}

JNIEXPORT void JNICALL Java_io_realm_internal_CheckedRow_nativeSetDate
  (JNIEnv* env, jobject obj, jlong nativeRowPtr, jlong columnIndex, jlong value)
{
    TR_ENTER_PTR(nativeRowPtr)
    if(!ROW_AND_COL_INDEX_AND_TYPE_VALID(env, ROW(nativeRowPtr), columnIndex, type_DateTime))
        return;

    Java_io_realm_internal_Row_nativeSetDate(env, obj, nativeRowPtr, columnIndex, value);
}

JNIEXPORT void JNICALL Java_io_realm_internal_CheckedRow_nativeSetString
  (JNIEnv* env, jobject obj, jlong nativeRowPtr, jlong columnIndex, jstring value)
{
    TR_ENTER_PTR(nativeRowPtr)
    if (!ROW_AND_COL_INDEX_AND_TYPE_VALID(env, ROW(nativeRowPtr), columnIndex, type_String))
        return;

    Java_io_realm_internal_CheckedRow_nativeSetString(env, obj, nativeRowPtr, columnIndex, value);
}

JNIEXPORT void JNICALL Java_io_realm_internal_CheckedRow_nativeSetByteArray
  (JNIEnv* env, jobject obj, jlong nativeRowPtr, jlong columnIndex, jbyteArray value)
{
    TR_ENTER_PTR(nativeRowPtr)
    if (!ROW_AND_COL_INDEX_AND_TYPE_VALID(env, ROW(nativeRowPtr), columnIndex, type_Binary))
        return;

    Java_io_realm_internal_Row_nativeSetByteArray(env, obj, nativeRowPtr, columnIndex, value);
}

JNIEXPORT void JNICALL Java_io_realm_internal_CheckedRow_nativeSetMixed
  (JNIEnv* env, jobject obj, jlong nativeRowPtr, jlong columnIndex, jobject jMixedValue)
{
    TR_ENTER_PTR(nativeRowPtr)
    if (!ROW_AND_COL_INDEX_AND_TYPE_VALID(env, ROW(nativeRowPtr), columnIndex, type_Mixed))
        return;

    Java_io_realm_internal_Row_nativeSetMixed(env, obj, nativeRowPtr, columnIndex, jMixedValue);
}

JNIEXPORT void JNICALL Java_io_realm_internal_CheckedRow_nativeSetLink
  (JNIEnv* env, jobject obj, jlong nativeRowPtr, jlong columnIndex, jlong value)
{
    TR_ENTER_PTR(nativeRowPtr)
    if (!ROW_AND_COL_INDEX_AND_TYPE_VALID(env, ROW(nativeRowPtr), columnIndex, type_Link))
        return;

    Java_io_realm_internal_Row_nativeSetLink(env, obj, nativeRowPtr, columnIndex, value);
}

JNIEXPORT void JNICALL Java_io_realm_internal_CheckedRow_nativeNullifyLink
  (JNIEnv* env, jobject obj, jlong nativeRowPtr, jlong columnIndex)
{
    TR_ENTER_PTR(nativeRowPtr)
    if (!ROW_AND_COL_INDEX_AND_TYPE_VALID(env, ROW(nativeRowPtr), columnIndex, type_Link))
        return;

    Java_io_realm_internal_Row_nativeNullifyLink(env, obj, nativeRowPtr, columnIndex);
}