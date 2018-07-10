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
#include "io_realm_internal_Property.h"

#include "java_accessor.hpp"
#include "util.hpp"

using namespace realm;
using namespace realm::_impl;

static void finalize_unchecked_row(jlong ptr);

JNIEXPORT jlong JNICALL Java_io_realm_internal_UncheckedRow_nativeGetColumnCount(JNIEnv*, jobject, jlong nativeRowPtr)
{
    TR_ENTER_PTR(nativeRowPtr)
    if (!ROW(nativeRowPtr)->is_valid()) {
        return 0;
    }

    return static_cast<jlong>(ROW(nativeRowPtr)->get_table()->get_column_count()); // noexcept
}

JNIEXPORT jstring JNICALL Java_io_realm_internal_UncheckedRow_nativeGetColumnName(JNIEnv* env, jobject,
                                                                                  jlong nativeRowPtr,
                                                                                  jlong columnKey)
{
    TR_ENTER_PTR(nativeRowPtr)
    if (!ROW_VALID(env, ROW(nativeRowPtr))) {
        return 0;
    }

    try {
        return to_jstring(env, ROW(nativeRowPtr)->get_table()->get_column_name(ColKey(columnKey)));
    }
    CATCH_STD();
    return NULL;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_UncheckedRow_nativeGetColumnIndex(JNIEnv* env, jobject,
                                                                                 jlong nativeRowPtr,
                                                                                 jstring columnName)
{
    TR_ENTER_PTR(nativeRowPtr)
    if (!ROW(nativeRowPtr)->is_valid()) {
        return 0;
    }

    try {
        JStringAccessor columnName2(env, columnName);                                   // throws
        ColKey col_key = ROW(nativeRowPtr)->get_table()->get_column_key(columnName2);
        if (bool(col_key)) {
            return col_key.value;
        }
    }
    CATCH_STD()
    return 0;
}

JNIEXPORT jint JNICALL Java_io_realm_internal_UncheckedRow_nativeGetColumnType(JNIEnv*, jobject, jlong nativeRowPtr,
                                                                               jlong columnKey)
{
    TR_ENTER_PTR(nativeRowPtr)
    auto column_type = ROW(nativeRowPtr)->get_table()->get_column_type(ColKey(columnKey)); // noexcept
    if (column_type != type_OldTable) {
        return static_cast<jint>(column_type);
    }
    // FIXME: Add test in https://github.com/realm/realm-java/pull/5221 before merging to master
    // TODO confirm the subtable code was removed https://github.com/realm/realm-core-private/commit/20d5dc268bbe0cff399ed0e4bab227f309e0e8bb#diff-127deed3dee453dcd27c2e02e48ecdd2
//    return static_cast<jint>(ROW(nativeRowPtr)->get_table()->get_descriptor()->get_subdescriptor(S(columnIndex))->get_column_type(S(0))
//                             + io_realm_internal_Property_TYPE_ARRAY); // noexcept
    return -1;//TODO replace with throw or proper default
}

//TODO renmae index to ObjKey
JNIEXPORT jlong JNICALL Java_io_realm_internal_UncheckedRow_nativeGetIndex(JNIEnv* env, jobject, jlong nativeRowPtr)
{
    TR_ENTER_PTR(nativeRowPtr)
    if (!ROW_VALID(env, ROW(nativeRowPtr))) {
        return 0;
    }

    return static_cast<jlong>(ROW(nativeRowPtr)->get_key().value);
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_UncheckedRow_nativeGetLong(JNIEnv* env, jobject, jlong nativeRowPtr,
                                                                          jlong columnKey)
{
    TR_ENTER_PTR(nativeRowPtr)
    if (!ROW_VALID(env, ROW(nativeRowPtr))) {
        return 0;
    }

    return ROW(nativeRowPtr)->get<Int>(ColKey(columnKey));
}

JNIEXPORT jboolean JNICALL Java_io_realm_internal_UncheckedRow_nativeGetBoolean(JNIEnv* env, jobject,
                                                                                jlong nativeRowPtr, jlong columnKey)
{
    TR_ENTER_PTR(nativeRowPtr)
    if (!ROW_VALID(env, ROW(nativeRowPtr))) {
        return 0;
    }

    return to_jbool(ROW(nativeRowPtr)->get<bool>(ColKey(columnKey)));
}

JNIEXPORT jfloat JNICALL Java_io_realm_internal_UncheckedRow_nativeGetFloat(JNIEnv* env, jobject, jlong nativeRowPtr,
                                                                            jlong columnKey)
{
    TR_ENTER_PTR(nativeRowPtr)
    if (!ROW_VALID(env, ROW(nativeRowPtr))) {
        return 0;
    }

    return ROW(nativeRowPtr)->get<float>(ColKey(columnKey));
}

JNIEXPORT jdouble JNICALL Java_io_realm_internal_UncheckedRow_nativeGetDouble(JNIEnv* env, jobject,
                                                                              jlong nativeRowPtr, jlong columnKey)
{
    TR_ENTER_PTR(nativeRowPtr)
    if (!ROW_VALID(env, ROW(nativeRowPtr))) {
        return 0;
    }

    return ROW(nativeRowPtr)->get<double>(ColKey(columnKey));
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_UncheckedRow_nativeGetTimestamp(JNIEnv* env, jobject,
                                                                               jlong nativeRowPtr, jlong columnKey)
{
    TR_ENTER_PTR(nativeRowPtr)
    if (!ROW_VALID(env, ROW(nativeRowPtr))) {
        return 0;
    }

    return to_milliseconds(ROW(nativeRowPtr)->get<Timestamp>(ColKey(columnKey)));
}

JNIEXPORT jstring JNICALL Java_io_realm_internal_UncheckedRow_nativeGetString(JNIEnv* env, jobject,
                                                                              jlong nativeRowPtr, jlong columnKey)
{
    TR_ENTER_PTR(nativeRowPtr)
    if (!ROW_VALID(env, ROW(nativeRowPtr))) {
        return nullptr;
    }

    try {
        StringData value = ROW(nativeRowPtr)->get<StringData>(ColKey(columnKey));
        return to_jstring(env, value);
    }
    CATCH_STD()
    return nullptr;
}

JNIEXPORT jbyteArray JNICALL Java_io_realm_internal_UncheckedRow_nativeGetByteArray(JNIEnv* env, jobject,
                                                                                    jlong nativeRowPtr,
                                                                                    jlong columnKey)
{
    TR_ENTER_PTR(nativeRowPtr)
    if (!ROW_VALID(env, ROW(nativeRowPtr))) {
        return nullptr;
    }

    try {
        BinaryData bin = ROW(nativeRowPtr)->get<BinaryData>(ColKey(columnKey));
        return JavaClassGlobalDef::new_byte_array(env, bin);
    }
    CATCH_STD()
    return nullptr;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_UncheckedRow_nativeGetLink(JNIEnv* env, jobject, jlong nativeRowPtr,
                                                                          jlong columnKey)
{
    TR_ENTER_PTR(nativeRowPtr)
    if (!ROW_VALID(env, ROW(nativeRowPtr))) {
        return 0;
    }

    ColKey col_key(columnKey);
    if (ROW(nativeRowPtr)->is_null(col_key)) {
        return jlong(-1);
    }

    return static_cast<jlong>(ROW(nativeRowPtr)->get<ObjKey>(col_key).value);
}

JNIEXPORT jboolean JNICALL Java_io_realm_internal_UncheckedRow_nativeIsNullLink(JNIEnv* env, jobject,
                                                                                jlong nativeRowPtr, jlong columnKey)
{
    TR_ENTER_PTR(nativeRowPtr)
    if (!ROW_VALID(env, ROW(nativeRowPtr))) {
        return 0;
    }

    return to_jbool(ROW(nativeRowPtr)->is_null(ColKey(columnKey)));
}

JNIEXPORT void JNICALL Java_io_realm_internal_UncheckedRow_nativeSetLong(JNIEnv* env, jobject, jlong nativeRowPtr,
                                                                         jlong columnKey, jlong value)
{
    TR_ENTER_PTR(nativeRowPtr)
    if (!ROW_VALID(env, ROW(nativeRowPtr))) {
        return;
    }

    try {
        ROW(nativeRowPtr)->set(ColKey(columnKey), value);
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_UncheckedRow_nativeSetBoolean(JNIEnv* env, jobject, jlong nativeRowPtr,
                                                                            jlong columnKey, jboolean value)
{
    TR_ENTER_PTR(nativeRowPtr)
    if (!ROW_VALID(env, ROW(nativeRowPtr))) {
        return;
    }

    try {
        ROW(nativeRowPtr)->set(ColKey(columnKey), value);
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_UncheckedRow_nativeSetFloat(JNIEnv* env, jobject, jlong nativeRowPtr,
                                                                          jlong columnKey, jfloat value)
{
    TR_ENTER_PTR(nativeRowPtr)
    if (!ROW_VALID(env, ROW(nativeRowPtr))) {
        return;
    }

    try {
        ROW(nativeRowPtr)->set(ColKey(columnKey), value);
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_UncheckedRow_nativeSetDouble(JNIEnv* env, jobject, jlong nativeRowPtr,
                                                                           jlong columnKey, jdouble value)
{
    TR_ENTER_PTR(nativeRowPtr)
    if (!ROW_VALID(env, ROW(nativeRowPtr))) {
        return;
    }

    try {
        ROW(nativeRowPtr)->set(ColKey(columnKey), value);
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_UncheckedRow_nativeSetTimestamp(JNIEnv* env, jobject,
                                                                              jlong nativeRowPtr, jlong columnKey,
                                                                              jlong value)
{
    TR_ENTER_PTR(nativeRowPtr)
    if (!ROW_VALID(env, ROW(nativeRowPtr))) {
        return;
    }

    try {
        ROW(nativeRowPtr)->set(ColKey(columnKey), from_milliseconds(value));
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_UncheckedRow_nativeSetString(JNIEnv* env, jobject, jlong nativeRowPtr,
                                                                           jlong columnKey, jstring value)
{
    TR_ENTER_PTR(nativeRowPtr)
    if (!ROW_VALID(env, ROW(nativeRowPtr))) {
        return;
    }

    try {
        ColKey col_key(columnKey);
        if ((value == nullptr) && !(ROW(nativeRowPtr)->get_table()->is_nullable(col_key))) {
            ThrowNullValueException(env, const_cast<Table*>(ROW(nativeRowPtr)->get_table()), S(columnKey));
            return;
        }
        JStringAccessor value2(env, value); // throws
        ROW(nativeRowPtr)->set(col_key, value2);
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_UncheckedRow_nativeSetByteArray(JNIEnv* env, jobject,
                                                                              jlong nativeRowPtr, jlong columnKey,
                                                                              jbyteArray value)
{
    TR_ENTER_PTR(nativeRowPtr)

    if (!ROW_VALID(env, ROW(nativeRowPtr))) {
        return;
    }

    try {
        auto& obj = *reinterpret_cast<realm::Obj*>(nativeRowPtr);
        ColKey col_key(columnKey);
        if (value == nullptr && !(obj.get_table()->is_nullable(col_key))) {
            ThrowNullValueException(env, const_cast<Table*>(ROW(nativeRowPtr)->get_table()), S(columnKey));
            return;
        }

        JByteArrayAccessor jarray_accessor(env, value);
        obj.set(col_key, jarray_accessor.transform<BinaryData>());
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_UncheckedRow_nativeSetLink(JNIEnv* env, jobject, jlong nativeRowPtr,
                                                                         jlong columnKey, jlong valueObjKey)
{
    TR_ENTER_PTR(nativeRowPtr)
    if (!ROW_VALID(env, ROW(nativeRowPtr))) {
        return;
    }

    try {
        ROW(nativeRowPtr)->set(ColKey(columnKey), ObjKey(valueObjKey));
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_UncheckedRow_nativeNullifyLink(JNIEnv* env, jobject, jlong nativeRowPtr,
                                                                             jlong columnKey)
{
    TR_ENTER_PTR(nativeRowPtr)
    if (!ROW_VALID(env, ROW(nativeRowPtr))) {
        return;
    }

    try {
        ROW(nativeRowPtr)->set_null(ColKey(columnKey));
    }
    CATCH_STD()
}

//TODO rename isValid
JNIEXPORT jboolean JNICALL Java_io_realm_internal_UncheckedRow_nativeIsAttached(JNIEnv*, jobject, jlong nativeRowPtr)
{
    TR_ENTER_PTR(nativeRowPtr)
    return to_jbool(ROW(nativeRowPtr)->is_valid());
}

JNIEXPORT jboolean JNICALL Java_io_realm_internal_UncheckedRow_nativeHasColumn(JNIEnv* env, jobject obj,
                                                                               jlong nativeRowPtr, jstring columnName)
{
    jlong ndx = Java_io_realm_internal_UncheckedRow_nativeGetColumnIndex(env, obj, nativeRowPtr, columnName);
    return to_jbool(ndx != to_jlong_or_not_found(realm::not_found));
}

JNIEXPORT jboolean JNICALL Java_io_realm_internal_UncheckedRow_nativeIsNull(JNIEnv* env, jobject, jlong nativeRowPtr,
                                                                            jlong columnKey)
{
    TR_ENTER_PTR(nativeRowPtr)
    if (!ROW_VALID(env, ROW(nativeRowPtr))) {
        return JNI_FALSE;
    }

    try {
        return to_jbool(ROW(nativeRowPtr)->is_null(ColKey(columnKey)));
    }
    CATCH_STD()
    return JNI_FALSE;
}

JNIEXPORT void JNICALL Java_io_realm_internal_UncheckedRow_nativeSetNull(JNIEnv* env, jobject, jlong nativeRowPtr,
                                                                         jlong columnKey)
{
    TR_ENTER_PTR(nativeRowPtr)
    if (!ROW_VALID(env, ROW(nativeRowPtr))) {
        return;
    }
    if (!TBL_AND_COL_NULLABLE(env, ROW(nativeRowPtr)->get_table(), columnKey)) {
        return;
    }
    try {
        ROW(nativeRowPtr)->set_null(ColKey(columnKey));
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
