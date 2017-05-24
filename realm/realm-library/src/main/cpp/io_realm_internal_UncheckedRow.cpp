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

using namespace realm;

static void finalize_unchecked_row(jlong ptr);

JNIEXPORT jlong JNICALL Java_io_realm_internal_UncheckedRow_nativeGetColumnCount(JNIEnv*, jobject, jlong nativeRowPtr)
{
    TR_ENTER_PTR(nativeRowPtr)
    if (!ROW(nativeRowPtr)->is_attached()) {
        return 0;
    }

    return static_cast<jlong>(ROW(nativeRowPtr)->get_column_count()); // noexcept
}

JNIEXPORT jstring JNICALL Java_io_realm_internal_UncheckedRow_nativeGetColumnName(JNIEnv* env, jobject,
                                                                                  jlong nativeRowPtr,
                                                                                  jlong columnIndex)
{
    TR_ENTER_PTR(nativeRowPtr)
    if (!ROW_VALID(env, ROW(nativeRowPtr))) {
        return 0;
    }

    try {
        return to_jstring(env, ROW(nativeRowPtr)->get_column_name(S(columnIndex)));
    }
    CATCH_STD();
    return NULL;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_UncheckedRow_nativeGetColumnIndex(JNIEnv* env, jobject,
                                                                                 jlong nativeRowPtr,
                                                                                 jstring columnName)
{
    TR_ENTER_PTR(nativeRowPtr)
    if (!ROW(nativeRowPtr)->is_attached()) {
        return 0;
    }

    try {
        JStringAccessor columnName2(env, columnName);                                   // throws
        return to_jlong_or_not_found(ROW(nativeRowPtr)->get_column_index(columnName2)); // noexcept
    }
    CATCH_STD()
    return 0;
}

JNIEXPORT jint JNICALL Java_io_realm_internal_UncheckedRow_nativeGetColumnType(JNIEnv*, jobject, jlong nativeRowPtr,
                                                                               jlong columnIndex)
{
    TR_ENTER_PTR(nativeRowPtr)
    return static_cast<jint>(ROW(nativeRowPtr)->get_column_type(S(columnIndex))); // noexcept
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_UncheckedRow_nativeGetIndex(JNIEnv* env, jobject, jlong nativeRowPtr)
{
    TR_ENTER_PTR(nativeRowPtr)
    if (!ROW_VALID(env, ROW(nativeRowPtr))) {
        return 0;
    }

    return static_cast<jlong>(ROW(nativeRowPtr)->get_index());
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_UncheckedRow_nativeGetLong(JNIEnv* env, jobject, jlong nativeRowPtr,
                                                                          jlong columnIndex)
{
    TR_ENTER_PTR(nativeRowPtr)
    if (!ROW_VALID(env, ROW(nativeRowPtr))) {
        return 0;
    }

    return ROW(nativeRowPtr)->get_int(S(columnIndex));
}

JNIEXPORT jboolean JNICALL Java_io_realm_internal_UncheckedRow_nativeGetBoolean(JNIEnv* env, jobject,
                                                                                jlong nativeRowPtr, jlong columnIndex)
{
    TR_ENTER_PTR(nativeRowPtr)
    if (!ROW_VALID(env, ROW(nativeRowPtr))) {
        return 0;
    }

    return to_jbool(ROW(nativeRowPtr)->get_bool(S(columnIndex)));
}

JNIEXPORT jfloat JNICALL Java_io_realm_internal_UncheckedRow_nativeGetFloat(JNIEnv* env, jobject, jlong nativeRowPtr,
                                                                            jlong columnIndex)
{
    TR_ENTER_PTR(nativeRowPtr)
    if (!ROW_VALID(env, ROW(nativeRowPtr))) {
        return 0;
    }

    return ROW(nativeRowPtr)->get_float(S(columnIndex));
}

JNIEXPORT jdouble JNICALL Java_io_realm_internal_UncheckedRow_nativeGetDouble(JNIEnv* env, jobject,
                                                                              jlong nativeRowPtr, jlong columnIndex)
{
    TR_ENTER_PTR(nativeRowPtr)
    if (!ROW_VALID(env, ROW(nativeRowPtr))) {
        return 0;
    }

    return ROW(nativeRowPtr)->get_double(S(columnIndex));
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_UncheckedRow_nativeGetTimestamp(JNIEnv* env, jobject,
                                                                               jlong nativeRowPtr, jlong columnIndex)
{
    TR_ENTER_PTR(nativeRowPtr)
    if (!ROW_VALID(env, ROW(nativeRowPtr))) {
        return 0;
    }

    return to_milliseconds(ROW(nativeRowPtr)->get_timestamp(S(columnIndex)));
}

JNIEXPORT jstring JNICALL Java_io_realm_internal_UncheckedRow_nativeGetString(JNIEnv* env, jobject,
                                                                              jlong nativeRowPtr, jlong columnIndex)
{
    TR_ENTER_PTR(nativeRowPtr)
    if (!ROW_VALID(env, ROW(nativeRowPtr))) {
        return nullptr;
    }

    try {
        StringData value = ROW(nativeRowPtr)->get_string(S(columnIndex));
        return to_jstring(env, value);
    }
    CATCH_STD()
    return nullptr;
}

JNIEXPORT jbyteArray JNICALL Java_io_realm_internal_UncheckedRow_nativeGetByteArray(JNIEnv* env, jobject,
                                                                                    jlong nativeRowPtr,
                                                                                    jlong columnIndex)
{
    TR_ENTER_PTR(nativeRowPtr)
    if (!ROW_VALID(env, ROW(nativeRowPtr))) {
        return nullptr;
    }

    BinaryData bin = ROW(nativeRowPtr)->get_binary(S(columnIndex));
    if (bin.is_null()) {
        return nullptr;
    }
    else if (bin.size() <= MAX_JSIZE) {
        jbyteArray jresult = env->NewByteArray(static_cast<jsize>(bin.size()));
        if (jresult) {
            env->SetByteArrayRegion(jresult, 0, static_cast<jsize>(bin.size()),
                                    reinterpret_cast<const jbyte*>(bin.data())); // throws
        }
        return jresult;
    }
    else {
        ThrowException(env, IllegalArgument, "Length of ByteArray is larger than an Int.");
        return nullptr;
    }
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_UncheckedRow_nativeGetLink(JNIEnv* env, jobject, jlong nativeRowPtr,
                                                                          jlong columnIndex)
{
    TR_ENTER_PTR(nativeRowPtr)
    if (!ROW_VALID(env, ROW(nativeRowPtr))) {
        return 0;
    }

    if (ROW(nativeRowPtr)->is_null_link(S(columnIndex))) {
        return jlong(-1);
    }

    return static_cast<jlong>(ROW(nativeRowPtr)->get_link(S(columnIndex)));
}

JNIEXPORT jboolean JNICALL Java_io_realm_internal_UncheckedRow_nativeIsNullLink(JNIEnv* env, jobject,
                                                                                jlong nativeRowPtr, jlong columnIndex)
{
    TR_ENTER_PTR(nativeRowPtr)
    if (!ROW_VALID(env, ROW(nativeRowPtr))) {
        return 0;
    }

    return to_jbool(ROW(nativeRowPtr)->is_null_link(S(columnIndex)));
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_UncheckedRow_nativeGetLinkView(JNIEnv* env, jobject,
                                                                              jlong nativeRowPtr, jlong columnIndex)
{
    TR_ENTER_PTR(nativeRowPtr)
    if (!ROW_VALID(env, ROW(nativeRowPtr))) {
        return 0;
    }

    LinkViewRef* link_view_ptr =
        const_cast<LinkViewRef*>(&(LangBindHelper::get_linklist_ptr(*ROW(nativeRowPtr), S(columnIndex))));
    return reinterpret_cast<jlong>(link_view_ptr);
}

JNIEXPORT void JNICALL Java_io_realm_internal_UncheckedRow_nativeSetLong(JNIEnv* env, jobject, jlong nativeRowPtr,
                                                                         jlong columnIndex, jlong value)
{
    TR_ENTER_PTR(nativeRowPtr)
    if (!ROW_VALID(env, ROW(nativeRowPtr))) {
        return;
    }

    try {
        ROW(nativeRowPtr)->set_int(S(columnIndex), value);
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_UncheckedRow_nativeSetBoolean(JNIEnv* env, jobject, jlong nativeRowPtr,
                                                                            jlong columnIndex, jboolean value)
{
    TR_ENTER_PTR(nativeRowPtr)
    if (!ROW_VALID(env, ROW(nativeRowPtr))) {
        return;
    }

    try {
        ROW(nativeRowPtr)->set_bool(S(columnIndex), value);
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_UncheckedRow_nativeSetFloat(JNIEnv* env, jobject, jlong nativeRowPtr,
                                                                          jlong columnIndex, jfloat value)
{
    TR_ENTER_PTR(nativeRowPtr)
    if (!ROW_VALID(env, ROW(nativeRowPtr))) {
        return;
    }

    try {
        ROW(nativeRowPtr)->set_float(S(columnIndex), value);
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_UncheckedRow_nativeSetDouble(JNIEnv* env, jobject, jlong nativeRowPtr,
                                                                           jlong columnIndex, jdouble value)
{
    TR_ENTER_PTR(nativeRowPtr)
    if (!ROW_VALID(env, ROW(nativeRowPtr))) {
        return;
    }

    try {
        ROW(nativeRowPtr)->set_double(S(columnIndex), value);
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_UncheckedRow_nativeSetTimestamp(JNIEnv* env, jobject,
                                                                              jlong nativeRowPtr, jlong columnIndex,
                                                                              jlong value)
{
    TR_ENTER_PTR(nativeRowPtr)
    if (!ROW_VALID(env, ROW(nativeRowPtr))) {
        return;
    }

    try {
        ROW(nativeRowPtr)->set_timestamp(S(columnIndex), from_milliseconds(value));
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_UncheckedRow_nativeSetString(JNIEnv* env, jobject, jlong nativeRowPtr,
                                                                           jlong columnIndex, jstring value)
{
    TR_ENTER_PTR(nativeRowPtr)
    if (!ROW_VALID(env, ROW(nativeRowPtr))) {
        return;
    }

    try {
        if ((value == nullptr) && !(ROW(nativeRowPtr)->get_table()->is_nullable(S(columnIndex)))) {
            ThrowNullValueException(env, ROW(nativeRowPtr)->get_table(), S(columnIndex));
            return;
        }
        JStringAccessor value2(env, value); // throws
        ROW(nativeRowPtr)->set_string(S(columnIndex), value2);
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_UncheckedRow_nativeSetByteArray(JNIEnv* env, jobject,
                                                                              jlong nativeRowPtr, jlong columnIndex,
                                                                              jbyteArray value)
{
    TR_ENTER_PTR(nativeRowPtr)

    if (!ROW_VALID(env, ROW(nativeRowPtr))) {
        return;
    }

    jbyte* bytePtr = nullptr;
    try {
        if (value == nullptr) {
            if (!(ROW(nativeRowPtr)->get_table()->is_nullable(S(columnIndex)))) {
                ThrowNullValueException(env, ROW(nativeRowPtr)->get_table(), S(columnIndex));
                return;
            }
            ROW(nativeRowPtr)->set_binary(S(columnIndex), BinaryData());
        }
        else {
            bytePtr = env->GetByteArrayElements(value, NULL);
            if (!bytePtr) {
                ThrowException(env, IllegalArgument, "doByteArray");
                return;
            }
            size_t dataLen = S(env->GetArrayLength(value));
            ROW(nativeRowPtr)->set_binary(S(columnIndex), BinaryData(reinterpret_cast<char*>(bytePtr), dataLen));
        }
    }
    CATCH_STD()

    if (bytePtr) {
        env->ReleaseByteArrayElements(value, bytePtr, JNI_ABORT);
    }
}

JNIEXPORT void JNICALL Java_io_realm_internal_UncheckedRow_nativeSetLink(JNIEnv* env, jobject, jlong nativeRowPtr,
                                                                         jlong columnIndex, jlong value)
{
    TR_ENTER_PTR(nativeRowPtr)
    if (!ROW_VALID(env, ROW(nativeRowPtr))) {
        return;
    }

    try {
        ROW(nativeRowPtr)->set_link(S(columnIndex), value);
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_UncheckedRow_nativeNullifyLink(JNIEnv* env, jobject, jlong nativeRowPtr,
                                                                             jlong columnIndex)
{
    TR_ENTER_PTR(nativeRowPtr)
    if (!ROW_VALID(env, ROW(nativeRowPtr))) {
        return;
    }

    try {
        ROW(nativeRowPtr)->nullify_link(S(columnIndex));
    }
    CATCH_STD()
}

JNIEXPORT jboolean JNICALL Java_io_realm_internal_UncheckedRow_nativeIsAttached(JNIEnv*, jobject, jlong nativeRowPtr)
{
    TR_ENTER_PTR(nativeRowPtr)
    return to_jbool(ROW(nativeRowPtr)->is_attached());
}

JNIEXPORT jboolean JNICALL Java_io_realm_internal_UncheckedRow_nativeHasColumn(JNIEnv* env, jobject obj,
                                                                               jlong nativeRowPtr, jstring columnName)
{
    jlong ndx = Java_io_realm_internal_UncheckedRow_nativeGetColumnIndex(env, obj, nativeRowPtr, columnName);
    return to_jbool(ndx != to_jlong_or_not_found(realm::not_found));
}

JNIEXPORT jboolean JNICALL Java_io_realm_internal_UncheckedRow_nativeIsNull(JNIEnv* env, jobject, jlong nativeRowPtr,
                                                                            jlong columnIndex)
{
    TR_ENTER_PTR(nativeRowPtr)
    if (!ROW_VALID(env, ROW(nativeRowPtr))) {
        return JNI_FALSE;
    }

    try {
        return to_jbool(ROW(nativeRowPtr)->is_null(columnIndex));
    }
    CATCH_STD()
    return JNI_FALSE;
}

JNIEXPORT void JNICALL Java_io_realm_internal_UncheckedRow_nativeSetNull(JNIEnv* env, jobject, jlong nativeRowPtr,
                                                                         jlong columnIndex)
{
    TR_ENTER_PTR(nativeRowPtr)
    if (!ROW_VALID(env, ROW(nativeRowPtr))) {
        return;
    }
    if (!TBL_AND_COL_NULLABLE(env, ROW(nativeRowPtr)->get_table(), columnIndex)) {
        return;
    }
    try {
        ROW(nativeRowPtr)->set_null(columnIndex);
    }
    CATCH_STD()
}

static void finalize_unchecked_row(jlong ptr)
{
    TR_ENTER_PTR(ptr)
    delete ROW(ptr);
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_UncheckedRow_nativeGetFinalizerPtr(JNIEnv*, jclass)
{
    TR_ENTER()
    return reinterpret_cast<jlong>(&finalize_unchecked_row);
}
