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
#include "io_realm_internal_UncheckedRow.h"

#include "util.hpp"

using namespace realm;

JNIEXPORT jlong JNICALL Java_io_realm_internal_CheckedRow_nativeGetColumnCount(JNIEnv* env, jobject obj,
                                                                               jlong nativeRowPtr)
{
    if (!OBJ(nativeRowPtr)->is_valid()) {
        return 0;
    }

    return Java_io_realm_internal_UncheckedRow_nativeGetColumnCount(env, obj, nativeRowPtr);
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_CheckedRow_nativeGetColumnKey(JNIEnv* env, jobject obj,
                                                                               jlong nativeRowPtr, jstring columnName)
{
    if (!OBJ(nativeRowPtr)->is_valid()) {
        ThrowException(env, IllegalArgument, "Object passed is not valid");
    }

    ColKey col_key (Java_io_realm_internal_UncheckedRow_nativeGetColumnKey(env, obj, nativeRowPtr, columnName));
    if (!bool(col_key)) {
        JStringAccessor column_name(env, columnName);
        ThrowException(env, IllegalArgument, concat_stringdata("Field not found: ", column_name));
    }
    return col_key.value;
}

JNIEXPORT jint JNICALL Java_io_realm_internal_CheckedRow_nativeGetColumnType(JNIEnv* env, jobject obj,
                                                                             jlong nativeRowPtr, jlong columnKey)
{
    return Java_io_realm_internal_UncheckedRow_nativeGetColumnType(env, obj, nativeRowPtr, columnKey);
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_CheckedRow_nativeGetLong(JNIEnv* env, jobject obj, jlong nativeRowPtr,
                                                                        jlong columnKey)
{
    if (!TYPE_VALID(env, OBJ(nativeRowPtr)->get_table(), columnKey, col_type_Int)) {
        return 0;
    }

    return Java_io_realm_internal_UncheckedRow_nativeGetLong(env, obj, nativeRowPtr, columnKey);
}

JNIEXPORT jboolean JNICALL Java_io_realm_internal_CheckedRow_nativeGetBoolean(JNIEnv* env, jobject obj,
                                                                              jlong nativeRowPtr, jlong columnKey)
{
    if (!TYPE_VALID(env, OBJ(nativeRowPtr)->get_table(), columnKey, col_type_Bool)) {
        return JNI_FALSE;
    }

    return Java_io_realm_internal_UncheckedRow_nativeGetBoolean(env, obj, nativeRowPtr, columnKey);
}

JNIEXPORT jfloat JNICALL Java_io_realm_internal_CheckedRow_nativeGetFloat(JNIEnv* env, jobject obj,
                                                                          jlong nativeRowPtr, jlong columnKey)
{
    if (!TYPE_VALID(env, OBJ(nativeRowPtr)->get_table(), columnKey, col_type_Float)) {
        return 0;
    }

    return Java_io_realm_internal_UncheckedRow_nativeGetFloat(env, obj, nativeRowPtr, columnKey);
}

JNIEXPORT jdouble JNICALL Java_io_realm_internal_CheckedRow_nativeGetDouble(JNIEnv* env, jobject obj,
                                                                            jlong nativeRowPtr, jlong columnKey)
{
    if (!TYPE_VALID(env, OBJ(nativeRowPtr)->get_table(), columnKey, col_type_Double)) {
        return 0;
    }

    return Java_io_realm_internal_UncheckedRow_nativeGetDouble(env, obj, nativeRowPtr, columnKey);
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_CheckedRow_nativeGetTimestamp(JNIEnv* env, jobject obj,
                                                                             jlong nativeRowPtr, jlong columnKey)
{
    if (!TYPE_VALID(env, OBJ(nativeRowPtr)->get_table(), columnKey, col_type_Timestamp)) {
        return 0;
    }

    return Java_io_realm_internal_UncheckedRow_nativeGetTimestamp(env, obj, nativeRowPtr, columnKey);
}

JNIEXPORT jstring JNICALL Java_io_realm_internal_CheckedRow_nativeGetString(JNIEnv* env, jobject obj,
                                                                            jlong nativeRowPtr, jlong columnKey)
{
    if (!TYPE_VALID(env, OBJ(nativeRowPtr)->get_table(), columnKey, col_type_String)) {
        return nullptr;
    }

    return Java_io_realm_internal_UncheckedRow_nativeGetString(env, obj, nativeRowPtr, columnKey);
}

JNIEXPORT jbyteArray JNICALL Java_io_realm_internal_CheckedRow_nativeGetByteArray(JNIEnv* env, jobject obj,
                                                                                  jlong nativeRowPtr,
                                                                                  jlong columnKey)
{
    if (!TYPE_VALID(env, OBJ(nativeRowPtr)->get_table(), columnKey, col_type_Binary)) {
        return nullptr;
    }

    return Java_io_realm_internal_UncheckedRow_nativeGetByteArray(env, obj, nativeRowPtr, columnKey);
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_CheckedRow_nativeGetLink(JNIEnv* env, jobject obj, jlong nativeRowPtr,
                                                                        jlong columnKey)
{
    if (!TYPE_VALID(env, OBJ(nativeRowPtr)->get_table(), columnKey, col_type_Link)) {
        return 0;
    }

    return Java_io_realm_internal_UncheckedRow_nativeGetLink(env, obj, nativeRowPtr, columnKey);
}

JNIEXPORT jboolean JNICALL Java_io_realm_internal_CheckedRow_nativeIsNullLink(JNIEnv* env, jobject obj,
                                                                              jlong nativeRowPtr, jlong columnKey)
{
    if (!TYPE_VALID(env, OBJ(nativeRowPtr)->get_table(), columnKey, col_type_Link)) {
        return JNI_FALSE;
    }

    return Java_io_realm_internal_UncheckedRow_nativeIsNullLink(env, obj, nativeRowPtr, columnKey);
}

JNIEXPORT void JNICALL Java_io_realm_internal_CheckedRow_nativeSetLong(JNIEnv* env, jobject obj, jlong nativeRowPtr,
                                                                       jlong columnKey, jlong value)
{
    if (!TYPE_VALID(env, OBJ(nativeRowPtr)->get_table(), columnKey, col_type_Int)) {
        return;
    }

    Java_io_realm_internal_UncheckedRow_nativeSetLong(env, obj, nativeRowPtr, columnKey, value);
}

JNIEXPORT void JNICALL Java_io_realm_internal_CheckedRow_nativeSetBoolean(JNIEnv* env, jobject obj,
                                                                          jlong nativeRowPtr, jlong columnKey,
                                                                          jboolean value)
{
    if (!TYPE_VALID(env, OBJ(nativeRowPtr)->get_table(), columnKey, col_type_Bool)) {
        return;
    }

    Java_io_realm_internal_UncheckedRow_nativeSetBoolean(env, obj, nativeRowPtr, columnKey, value);
}

JNIEXPORT void JNICALL Java_io_realm_internal_CheckedRow_nativeSetFloat(JNIEnv* env, jobject obj, jlong nativeRowPtr,
                                                                        jlong columnKey, jfloat value)
{
    if (!TYPE_VALID(env, OBJ(nativeRowPtr)->get_table(), columnKey, col_type_Float)) {
        return;
    }

    Java_io_realm_internal_UncheckedRow_nativeSetFloat(env, obj, nativeRowPtr, columnKey, value);
}

JNIEXPORT void JNICALL Java_io_realm_internal_CheckedRow_nativeSetDouble(JNIEnv* env, jobject obj, jlong nativeRowPtr,
                                                                         jlong columnKey, jdouble value)
{
    if (!TYPE_VALID(env, OBJ(nativeRowPtr)->get_table(), columnKey, col_type_Double)) {
        return;
    }

    Java_io_realm_internal_UncheckedRow_nativeSetDouble(env, obj, nativeRowPtr, columnKey, value);
}

JNIEXPORT void JNICALL Java_io_realm_internal_CheckedRow_nativeSetTimestamp(JNIEnv* env, jobject obj,
                                                                            jlong nativeRowPtr, jlong columnKey,
                                                                            jlong value)
{
    if (!TYPE_VALID(env, OBJ(nativeRowPtr)->get_table(), columnKey, col_type_Timestamp)) {
        return;
    }

    Java_io_realm_internal_UncheckedRow_nativeSetTimestamp(env, obj, nativeRowPtr, columnKey, value);
}

JNIEXPORT void JNICALL Java_io_realm_internal_CheckedRow_nativeSetString(JNIEnv* env, jobject obj, jlong nativeRowPtr,
                                                                         jlong columnKey, jstring value)
{
    if (!TYPE_VALID(env, OBJ(nativeRowPtr)->get_table(), columnKey, col_type_String)) {
        return;
    }

    Java_io_realm_internal_UncheckedRow_nativeSetString(env, obj, nativeRowPtr, columnKey, value);
}

JNIEXPORT void JNICALL Java_io_realm_internal_CheckedRow_nativeSetByteArray(JNIEnv* env, jobject obj,
                                                                            jlong nativeRowPtr, jlong columnKey,
                                                                            jbyteArray value)
{
    if (!TYPE_VALID(env, OBJ(nativeRowPtr)->get_table(), columnKey, col_type_Binary)) {
        return;
    }

    Java_io_realm_internal_UncheckedRow_nativeSetByteArray(env, obj, nativeRowPtr, columnKey, value);
}

JNIEXPORT void JNICALL Java_io_realm_internal_CheckedRow_nativeSetLink(JNIEnv* env, jobject obj, jlong nativeRowPtr,
                                                                       jlong columnKey, jlong value)
{
    if (!TYPE_VALID(env, OBJ(nativeRowPtr)->get_table(), columnKey, col_type_Link)) {
        return;
    }

    Java_io_realm_internal_UncheckedRow_nativeSetLink(env, obj, nativeRowPtr, columnKey, value);
}

JNIEXPORT void JNICALL Java_io_realm_internal_CheckedRow_nativeNullifyLink(JNIEnv* env, jobject obj,
                                                                           jlong nativeRowPtr, jlong columnKey)
{
    if (!TYPE_VALID(env, OBJ(nativeRowPtr)->get_table(), columnKey, col_type_Link)) {
        return;
    }

    Java_io_realm_internal_UncheckedRow_nativeNullifyLink(env, obj, nativeRowPtr, columnKey);
}


JNIEXPORT jlongArray JNICALL Java_io_realm_internal_CheckedRow_nativeGetDecimal128(JNIEnv* env, jobject obj, jlong nativeRowPtr, jlong columnKey)
{
    if (!TYPE_VALID(env, OBJ(nativeRowPtr)->get_table(), columnKey, col_type_Decimal)) {
        return nullptr;
    }

    return Java_io_realm_internal_UncheckedRow_nativeGetDecimal128(env, obj, nativeRowPtr, columnKey);
}

JNIEXPORT void JNICALL Java_io_realm_internal_CheckedRow_nativeSetDecimal128(JNIEnv* env, jobject obj, jlong nativeRowPtr, jlong columnKey, jlong low, jlong high)
{
    if (!TYPE_VALID(env, OBJ(nativeRowPtr)->get_table(), columnKey, col_type_Decimal)) {
        return;
    }

    Java_io_realm_internal_UncheckedRow_nativeSetDecimal128(env, obj, nativeRowPtr, columnKey, low, high);
}

JNIEXPORT jstring JNICALL Java_io_realm_internal_CheckedRow_nativeGetObjectId(JNIEnv* env, jobject obj,
                                                                                    jlong nativeRowPtr,
                                                                                    jlong columnKey)
{
    if (!TYPE_VALID(env, OBJ(nativeRowPtr)->get_table(), columnKey, col_type_ObjectId)) {
        return nullptr;
    }

    return Java_io_realm_internal_UncheckedRow_nativeGetObjectId(env, obj, nativeRowPtr, columnKey);
}

JNIEXPORT void JNICALL Java_io_realm_internal_CheckedRow_nativeSetObjectId(JNIEnv* env, jobject obj,
                                                                              jlong nativeRowPtr, jlong columnKey,
                                                                              jstring j_value)
{
    if (!TYPE_VALID(env, OBJ(nativeRowPtr)->get_table(), columnKey, col_type_ObjectId)) {
        return;
    }

    Java_io_realm_internal_UncheckedRow_nativeSetObjectId(env, obj, nativeRowPtr, columnKey, j_value);
}
