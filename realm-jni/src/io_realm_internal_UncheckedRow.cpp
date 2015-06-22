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

#include "io_realm_internal_UncheckedRow.h"
#include "util.hpp"
#include "mixedutil.hpp"
#include "tablebase_tpl.hpp"

using namespace realm;

JNIEXPORT jlong JNICALL Java_io_realm_internal_UncheckedRow_nativeGetColumnCount
  (JNIEnv *, jobject, jlong nativeRowPtr)
{
    TR_ENTER_PTR(nativeRowPtr)
    if (!ROW(nativeRowPtr)->is_attached())
        return 0;
    return ROW(nativeRowPtr)->get_column_count(); // noexcept
}

JNIEXPORT jstring JNICALL Java_io_realm_internal_UncheckedRow_nativeGetColumnName
  (JNIEnv* env, jobject, jlong nativeRowPtr, jlong columnIndex)
{
    TR_ENTER_PTR(nativeRowPtr)
    try {
        return to_jstring(env, ROW(nativeRowPtr)->get_column_name( S(columnIndex)));
    } CATCH_STD();
    return NULL;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_UncheckedRow_nativeGetColumnIndex
  (JNIEnv* env, jobject, jlong nativeRowPtr, jstring columnName)
{
    TR_ENTER_PTR(nativeRowPtr)
    if (!ROW(nativeRowPtr)->is_attached())
        return 0;
    try {
        JStringAccessor columnName2(env, columnName); // throws
        return to_jlong_or_not_found( ROW(nativeRowPtr)->get_column_index(columnName2) ); // noexcept
    } CATCH_STD()
    return 0;
}

JNIEXPORT jint JNICALL Java_io_realm_internal_UncheckedRow_nativeGetColumnType
  (JNIEnv*, jobject, jlong nativeRowPtr, jlong columnIndex)
{
    TR_ENTER_PTR(nativeRowPtr)
    return static_cast<jint>( ROW(nativeRowPtr)->get_column_type( S(columnIndex)) ); // noexcept
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_UncheckedRow_nativeGetIndex
  (JNIEnv* env, jobject, jlong nativeRowPtr)
{
    TR_ENTER_PTR(nativeRowPtr)
    if (!ROW_VALID(env, ROW(nativeRowPtr)))
        return 0;

    return ROW(nativeRowPtr)->get_index();
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_UncheckedRow_nativeGetLong
  (JNIEnv*, jobject, jlong nativeRowPtr, jlong columnIndex)
{
    TR_ENTER_PTR(nativeRowPtr)
    return ROW(nativeRowPtr)->get_int( S(columnIndex) );
}

JNIEXPORT jboolean JNICALL Java_io_realm_internal_UncheckedRow_nativeGetBoolean
  (JNIEnv*, jobject, jlong nativeRowPtr, jlong columnIndex)
{
    TR_ENTER_PTR(nativeRowPtr)
    return ROW(nativeRowPtr)->get_bool( S(columnIndex) );
}

JNIEXPORT jfloat JNICALL Java_io_realm_internal_UncheckedRow_nativeGetFloat
  (JNIEnv*, jobject, jlong nativeRowPtr, jlong columnIndex)
{
    TR_ENTER_PTR(nativeRowPtr)
    return ROW(nativeRowPtr)->get_float( S(columnIndex) );
}

JNIEXPORT jdouble JNICALL Java_io_realm_internal_UncheckedRow_nativeGetDouble
  (JNIEnv*, jobject, jlong nativeRowPtr, jlong columnIndex)
{
    TR_ENTER_PTR(nativeRowPtr)
    return ROW(nativeRowPtr)->get_double( S(columnIndex) );
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_UncheckedRow_nativeGetDateTime
  (JNIEnv*, jobject, jlong nativeRowPtr, jlong columnIndex)
{
    TR_ENTER_PTR(nativeRowPtr)
    return ROW(nativeRowPtr)->get_datetime( S(columnIndex) ).get_datetime();
}

JNIEXPORT jstring JNICALL Java_io_realm_internal_UncheckedRow_nativeGetString
  (JNIEnv* env, jobject, jlong nativeRowPtr, jlong columnIndex)
{
    TR_ENTER_PTR(nativeRowPtr)
    if (!ROW_AND_COL_INDEX_AND_TYPE_VALID(env, ROW(nativeRowPtr), columnIndex, type_String))
        return 0;

    try {
        StringData value = ROW(nativeRowPtr)->get_string( S(columnIndex) );
        return to_jstring(env,  value);
    } CATCH_STD()
    return NULL;
}

JNIEXPORT jbyteArray JNICALL Java_io_realm_internal_UncheckedRow_nativeGetByteArray
  (JNIEnv* env, jobject, jlong nativeRowPtr, jlong columnIndex)
{
    TR_ENTER_PTR(nativeRowPtr)
    BinaryData bin = ROW(nativeRowPtr)->get_binary( S(columnIndex) );
    if (bin.is_null()) {
        return NULL;
    }
    else {
        if (bin.size() <= MAX_JSIZE) {
            jbyteArray jresult = env->NewByteArray(static_cast<jsize>(bin.size()));
            if (jresult)
                env->SetByteArrayRegion(jresult, 0, static_cast<jsize>(bin.size()), reinterpret_cast<const jbyte*>(bin.data()));  // throws
            return jresult;
        }
        else {
            ThrowException(env, IllegalArgument, "Length of ByteArray is larger than an Int.");
            return NULL;
        }
    }
}

JNIEXPORT jint JNICALL Java_io_realm_internal_UncheckedRow_nativeGetMixedType
  (JNIEnv*, jobject, jlong nativeRowPtr, jlong columnIndex)
{
    TR_ENTER_PTR(nativeRowPtr)
    DataType mixedType = ROW(nativeRowPtr)->get_mixed_type( S(columnIndex) );  // noexcept
    return static_cast<jint>(mixedType);
}

JNIEXPORT jobject JNICALL Java_io_realm_internal_UncheckedRow_nativeGetMixed
  (JNIEnv* env, jobject, jlong nativeRowPtr, jlong columnIndex)
{
    TR_ENTER_PTR(nativeRowPtr)
    Mixed value = ROW(nativeRowPtr)->get_mixed( S(columnIndex) );  // noexcept
    try {
        return CreateJMixedFromMixed(env, value);
    } CATCH_STD();
    return NULL;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_UncheckedRow_nativeGetLink
  (JNIEnv*, jobject, jlong nativeRowPtr, jlong columnIndex)
{
    TR_ENTER_PTR(nativeRowPtr)
    if (ROW(nativeRowPtr)->is_null_link( S(columnIndex) ))
        return jlong(-1);

    return ROW(nativeRowPtr)->get_link( S(columnIndex) );
}

JNIEXPORT jboolean JNICALL Java_io_realm_internal_UncheckedRow_nativeIsNullLink
  (JNIEnv*, jobject, jlong nativeRowPtr, jlong columnIndex)
{
    TR_ENTER_PTR(nativeRowPtr)
    return ROW(nativeRowPtr)->is_null_link( S(columnIndex) );
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_UncheckedRow_nativeGetLinkView
  (JNIEnv*, jobject, jlong nativeRowPtr, jlong columnIndex)
{
    TR_ENTER_PTR(nativeRowPtr)
    LinkView* link_view_ptr = LangBindHelper::get_linklist_ptr( *ROW( nativeRowPtr ), S( columnIndex) );
    return reinterpret_cast<jlong>(link_view_ptr);
}

JNIEXPORT void JNICALL Java_io_realm_internal_UncheckedRow_nativeSetLong
  (JNIEnv* env, jobject, jlong nativeRowPtr, jlong columnIndex, jlong value)
{
    TR_ENTER_PTR(nativeRowPtr)
    try {
        ROW(nativeRowPtr)->set_int( S(columnIndex), value);
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_UncheckedRow_nativeSetBoolean
  (JNIEnv* env, jobject, jlong nativeRowPtr, jlong columnIndex, jboolean value)
{
    TR_ENTER_PTR(nativeRowPtr)
    try {
        ROW(nativeRowPtr)->set_bool( S(columnIndex), value);
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_UncheckedRow_nativeSetFloat
  (JNIEnv* env, jobject, jlong nativeRowPtr, jlong columnIndex, jfloat value)
{
    TR_ENTER_PTR(nativeRowPtr)
    try {
        ROW(nativeRowPtr)->set_float( S(columnIndex), value);
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_UncheckedRow_nativeSetDouble
  (JNIEnv* env, jobject, jlong nativeRowPtr, jlong columnIndex, jdouble value)
{
    TR_ENTER_PTR(nativeRowPtr)
    try {
        ROW(nativeRowPtr)->set_double( S(columnIndex), value);
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_UncheckedRow_nativeSetDate
  (JNIEnv* env, jobject, jlong nativeRowPtr, jlong columnIndex, jlong value)
{
    TR_ENTER_PTR(nativeRowPtr)
    try {
        ROW(nativeRowPtr)->set_datetime( S(columnIndex), value);
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_UncheckedRow_nativeSetString
  (JNIEnv* env, jobject, jlong nativeRowPtr, jlong columnIndex, jstring value)
{
    TR_ENTER_PTR(nativeRowPtr)
    try {
        if ((value == NULL) && !(ROW(nativeRowPtr)->get_table()->is_nullable( S(columnIndex) ))) {
            ThrowNullValueException(env, ROW(nativeRowPtr)->get_table(), S(columnIndex));
            return;
        }
        JStringAccessor value2(env, value); // throws
        ROW(nativeRowPtr)->set_string( S(columnIndex), value2);
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_UncheckedRow_nativeSetByteArray
  (JNIEnv* env, jobject, jlong nativeRowPtr, jlong columnIndex, jbyteArray value)
{
    TR_ENTER_PTR(nativeRowPtr)

    try {
        if (value == NULL) {
            if (!(ROW(nativeRowPtr)->get_table()->is_nullable(S(columnIndex)))) {
                ThrowNullValueException(env, ROW(nativeRowPtr)->get_table(), S(columnIndex));
                return;
            }
            ROW(nativeRowPtr)->set_binary(S(columnIndex), BinaryData());
        }
        else {
            jbyte* bytePtr = env->GetByteArrayElements(value, NULL);
            if (!bytePtr) {
                ThrowException(env, IllegalArgument, "doByteArray");
                return;
            }
            size_t dataLen = S(env->GetArrayLength(value));
            ROW(nativeRowPtr)->set_binary( S(columnIndex), BinaryData(reinterpret_cast<char*>(bytePtr), dataLen));
            env->ReleaseByteArrayElements(value, bytePtr, 0);
        }
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_UncheckedRow_nativeSetMixed
  (JNIEnv* env, jobject, jlong nativeRowPtr, jlong columnIndex, jobject jMixedValue)
{
    TR_ENTER_PTR(nativeRowPtr)
    try {
        row_nativeSetMixed(ROW(nativeRowPtr), env, columnIndex, jMixedValue);
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_UncheckedRow_nativeSetLink
  (JNIEnv* env, jobject, jlong nativeRowPtr, jlong columnIndex, jlong value)
{
    TR_ENTER_PTR(nativeRowPtr)
    try {
        ROW(nativeRowPtr)->set_link( S(columnIndex), value);
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_UncheckedRow_nativeNullifyLink
  (JNIEnv* env, jobject, jlong nativeRowPtr, jlong columnIndex)
{
    TR_ENTER_PTR(nativeRowPtr)
    try {
        ROW(nativeRowPtr)->nullify_link( S(columnIndex) );
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_UncheckedRow_nativeClose
  (JNIEnv *, jclass, jlong nativeRowPtr)
{
    TR_ENTER_PTR(nativeRowPtr)
    delete ROW(nativeRowPtr);
}

JNIEXPORT jboolean JNICALL Java_io_realm_internal_UncheckedRow_nativeIsAttached
  (JNIEnv *, jobject, jlong nativeRowPtr)
{
    TR_ENTER_PTR(nativeRowPtr)
    return ROW(nativeRowPtr)->is_attached();
}
