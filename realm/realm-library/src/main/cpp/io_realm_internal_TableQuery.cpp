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

#include "io_realm_internal_TableQuery.h"

#include <realm.hpp>
#include <realm/query_expression.hpp>
#include <realm/table.hpp>

#include <realm/object-store/shared_realm.hpp>
#include <realm/object-store/object_store.hpp>
#include <realm/object-store/results.hpp>

#include "java_accessor.hpp"
#include "java_class_global_def.hpp"
#include "util.hpp"

using namespace realm;
using namespace realm::jni_util;
using namespace realm::_impl;

static void finalize_table_query(jlong ptr);


//-------------------------------------------------------

JNIEXPORT jstring JNICALL Java_io_realm_internal_TableQuery_nativeValidateQuery(JNIEnv* env, jobject,
                                                                                jlong nativeQueryPtr)
{
    try {
        const std::string str = Q(nativeQueryPtr)->validate();
        StringData sd(str);
        return to_jstring(env, sd);
    }
    CATCH_STD();
    return nullptr;
}


// helper functions

// Return LinkChain used to build link queries
// Each element in the indicesArray is the index of a column to be used to link to the next TableRef.
// If the corresponding entry in tablesArray is anything other than a nullptr, the link is a backlink.
// In that case, the tablesArray element is the pointer to the backlink source table and the
// indicesArray entry is the source column index in the source table.
static LinkChain getTableForLinkQuery(jlong nativeQueryPtr, const JLongArrayAccessor& tablesArray,
                                     const JLongArrayAccessor& colKeysArray)
{
    LinkChain linkChain(reinterpret_cast<Query *>(nativeQueryPtr)->get_table());
    jsize link_element_count = colKeysArray.size() - 1;
    for (int i = 0; i < link_element_count; ++i) {
        auto col_key = ColKey(colKeysArray[i]);
        if (tablesArray[i]) {
            TableRef linked_table_ref = TBL_REF(tablesArray[i]);
            linkChain.backlink(*linked_table_ref, col_key);
        }
        else {
            linkChain.link(col_key);
        }
    }
    return linkChain;
}

// Return TableRef point to original table or the link table
static ConstTableRef getTableByArray(jlong nativeQueryPtr, const JLongArrayAccessor& tablesArray,
                                const JLongArrayAccessor& colKeysArray)
{
    ConstTableRef table_ref = reinterpret_cast<Query *>(nativeQueryPtr)->get_table();
    jsize link_element_count = colKeysArray.size() - 1;
    for (int i = 0; i < link_element_count; ++i) {
        if (tablesArray[i]) {
            table_ref = TBL_REF(tablesArray[i]);
        }
        else {
            table_ref = table_ref->get_link_target(ColKey(colKeysArray[i]));
        }
    }
    return table_ref;
}

// I am not at all sure that it is even the right idea, let alone correct code. --gbm
static bool isNullable(JNIEnv* env, ConstTableRef* src_table_ptr, ConstTableRef table_ref, jlong column_key)
{
    // if table_arr is not a nullptr, this is a backlink and not allowed.
    if (src_table_ptr) {
        ThrowException(env, IllegalArgument, "LinkingObject from field " + std::string((*(src_table_ptr))->get_column_name(ColKey(column_key))) + " is not nullable.");
        return false;
    }
    return COL_NULLABLE(env, table_ref, column_key);
}

template <typename coretype, typename cpptype, typename javatype>
Query numeric_link_equal(LinkChain lc, jlong columnKey, javatype value)
{
    return lc.column<coretype>(ColKey(columnKey)) == cpptype(value);
}

template <typename coretype, typename cpptype, typename javatype>
Query numeric_link_notequal(LinkChain lc, jlong columnIndex, javatype value)
{
    return lc.column<coretype>(ColKey(columnIndex)) != cpptype(value);
}


template <typename coretype, typename cpptype, typename javatype>
Query numeric_link_greater(LinkChain lc, jlong columnIndex, javatype value)
{
    return lc.column<coretype>(ColKey(columnIndex)) > cpptype(value);
}

template <typename coretype, typename cpptype, typename javatype>
Query numeric_link_greaterequal(LinkChain lc, jlong columnIndex, javatype value)
{
    return lc.column<coretype>(ColKey(columnIndex)) >= cpptype(value);
}

template <typename coretype, typename cpptype, typename javatype>
Query numeric_link_less(LinkChain lc, jlong columnIndex, javatype value)
{
    return lc.column<coretype>(ColKey(columnIndex)) < cpptype(value);
}

template <typename coretype, typename cpptype, typename javatype>
Query numeric_link_lessequal(LinkChain lc, jlong columnIndex, javatype value)
{
    return lc.column<coretype>(ColKey(columnIndex)) <= cpptype(value);
}


// Integer
JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeEqual__J_3J_3JJ(JNIEnv* env, jobject,
                                                                            jlong nativeQueryPtr,
                                                                            jlongArray columnKeys,
                                                                            jlongArray tablePointers, jlong value)
{
    JLongArrayAccessor table_arr(env, tablePointers);
    JLongArrayAccessor col_key_arr(env, columnKeys);
    jsize arr_len = col_key_arr.size();
    try {
        if (arr_len == 1) {
            if (!TYPE_VALID(env, Q(nativeQueryPtr)->get_table(), col_key_arr[0], col_type_Int)) {
                return;
            }
            Q(nativeQueryPtr)->equal(ColKey(col_key_arr[0]), static_cast<int64_t>(value));
        }
        else {
            LinkChain linkChain = getTableForLinkQuery(nativeQueryPtr, table_arr, col_key_arr);
            Q(nativeQueryPtr)->and_query(numeric_link_equal<Int, int64_t, jlong>(linkChain, col_key_arr[arr_len - 1], value));
        }
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL JNICALL Java_io_realm_internal_TableQuery_nativeNotEqual__J_3J_3JJ(JNIEnv* env, jobject,
                                                                                       jlong nativeQueryPtr,
                                                                                       jlongArray columnKeys,
                                                                                       jlongArray tablePointers,
                                                                                       jlong value)
{
    JLongArrayAccessor table_arr(env, tablePointers);
    JLongArrayAccessor col_key_arr(env, columnKeys);
    jsize arr_len = col_key_arr.size();
    try {
        if (arr_len == 1) {
            if (!TYPE_VALID(env, Q(nativeQueryPtr)->get_table(), col_key_arr[0], col_type_Int)) {
                return;
            }
            Q(nativeQueryPtr)->not_equal(ColKey(col_key_arr[0]), static_cast<int64_t>(value));
        }
        else {
            LinkChain linkChain = getTableForLinkQuery(nativeQueryPtr, table_arr, col_key_arr);
            Q(nativeQueryPtr)
                ->and_query(numeric_link_notequal<Int, int64_t, jlong>(linkChain, col_key_arr[arr_len - 1], value));
        }
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeGreater__J_3J_3JJ(JNIEnv* env, jobject,
                                                                              jlong nativeQueryPtr,
                                                                              jlongArray columnKeys,
                                                                              jlongArray tablePointers, jlong value)
{
    JLongArrayAccessor table_arr(env, tablePointers);
    JLongArrayAccessor col_key_arr(env, columnKeys);
    jsize arr_len = col_key_arr.size();
    try {
        if (arr_len == 1) {
            if (!TYPE_VALID(env, Q(nativeQueryPtr)->get_table(), col_key_arr[0], col_type_Int)) {
                return;
            }
            Q(nativeQueryPtr)->greater(ColKey(col_key_arr[0]), static_cast<int64_t>(value));
        }
        else {
            LinkChain linkChain = getTableForLinkQuery(nativeQueryPtr, table_arr, col_key_arr);
            Q(nativeQueryPtr)
                ->and_query(numeric_link_greater<Int, int64_t, jlong>(linkChain, col_key_arr[arr_len - 1], value));
        }
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeGreaterEqual__J_3J_3JJ(JNIEnv* env, jobject,
                                                                                   jlong nativeQueryPtr,
                                                                                   jlongArray columnKeys,
                                                                                   jlongArray tablePointers,
                                                                                   jlong value)
{
    JLongArrayAccessor table_arr(env, tablePointers);
    JLongArrayAccessor col_key_arr(env, columnKeys);
    jsize arr_len = col_key_arr.size();
    try {
        if (arr_len == 1) {
            if (!TYPE_VALID(env, Q(nativeQueryPtr)->get_table(), col_key_arr[0], col_type_Int)) {
                return;
            }
            Q(nativeQueryPtr)->greater_equal(ColKey(col_key_arr[0]), static_cast<int64_t>(value));
        }
        else {
            LinkChain linkChain = getTableForLinkQuery(nativeQueryPtr, table_arr, col_key_arr);
            Q(nativeQueryPtr)
                ->and_query(numeric_link_greaterequal<Int, int64_t, jlong>(linkChain, col_key_arr[arr_len - 1], value));
        }
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeLess__J_3J_3JJ(JNIEnv* env, jobject, jlong nativeQueryPtr,
                                                                           jlongArray columnKeys,
                                                                           jlongArray tablePointers, jlong value)
{
    JLongArrayAccessor table_arr(env, tablePointers);
    JLongArrayAccessor col_key_arr(env, columnKeys);
    jsize arr_len = col_key_arr.size();
    try {
        if (arr_len == 1) {
            if (!TYPE_VALID(env, Q(nativeQueryPtr)->get_table(), col_key_arr[0], col_type_Int)) {
                return;
            }
            Q(nativeQueryPtr)->less(ColKey(col_key_arr[0]), static_cast<int64_t>(value));
        }
        else {
            LinkChain linkChain = getTableForLinkQuery(nativeQueryPtr, table_arr, col_key_arr);
            Q(nativeQueryPtr)->and_query(numeric_link_less<Int, int64_t, jlong>(linkChain, col_key_arr[arr_len - 1], value));
        }
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeLessEqual__J_3J_3JJ(JNIEnv* env, jobject,
                                                                                jlong nativeQueryPtr,
                                                                                jlongArray columnKeys,
                                                                                jlongArray tablePointers, jlong value)
{
    JLongArrayAccessor table_arr(env, tablePointers);
    JLongArrayAccessor col_key_arr(env, columnKeys);
    jsize arr_len = col_key_arr.size();
    try {
        if (arr_len == 1) {
            if (!TYPE_VALID(env, Q(nativeQueryPtr)->get_table(), col_key_arr[0], col_type_Int)) {
                return;
            }
            Q(nativeQueryPtr)->less_equal(ColKey(col_key_arr[0]), static_cast<int64_t>(value));
        }
        else {
            LinkChain linkChain = getTableForLinkQuery(nativeQueryPtr, table_arr, col_key_arr);
            Q(nativeQueryPtr)
                ->and_query(numeric_link_lessequal<Int, int64_t, jlong>(linkChain, col_key_arr[arr_len - 1], value));
        }
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeBetween__J_3JJJ(JNIEnv* env, jobject,
                                                                               jlong nativeQueryPtr,
                                                                               jlongArray columnKeys, jlong value1,
                                                                               jlong value2)
{
    JLongArrayAccessor arr(env, columnKeys);
    jsize arr_len = arr.size();
    try {
        if (arr_len == 1) {
            if (!TYPE_VALID(env, Q(nativeQueryPtr)->get_table(), arr[0], col_type_Int)) {
                return;
            }
            try {
                Q(nativeQueryPtr)
                    ->between(ColKey(arr[0]), static_cast<int64_t>(value1), static_cast<int64_t>(value2));
            }
            CATCH_STD()
        }
        else {
            ThrowException(env, IllegalArgument, "between() does not support queries using child object fields.");
        }
    }
    CATCH_STD()
}

// Float
JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeEqual__J_3J_3JF(JNIEnv* env, jobject,
                                                                            jlong nativeQueryPtr,
                                                                            jlongArray columnKeys,
                                                                            jlongArray tablePointers, jfloat value)
{
    JLongArrayAccessor table_arr(env, tablePointers);
    JLongArrayAccessor col_key_arr(env, columnKeys);
    jsize arr_len = col_key_arr.size();
    try {
        if (arr_len == 1) {
            if (!TYPE_VALID(env, Q(nativeQueryPtr)->get_table(), col_key_arr[0], col_type_Float)) {
                return;
            }
            Q(nativeQueryPtr)->equal(ColKey(col_key_arr[0]), static_cast<float>(value));
        }
        else {
            LinkChain linkChain = getTableForLinkQuery(nativeQueryPtr, table_arr, col_key_arr);
            Q(nativeQueryPtr)
                ->and_query(numeric_link_equal<Float, float, jfloat>(linkChain, col_key_arr[arr_len - 1], value));
        }
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL JNICALL Java_io_realm_internal_TableQuery_nativeNotEqual__J_3J_3JF(JNIEnv* env, jobject,
                                                                                       jlong nativeQueryPtr,
                                                                                       jlongArray columnKeys,
                                                                                       jlongArray tablePointers,
                                                                                       jfloat value)
{
    JLongArrayAccessor table_arr(env, tablePointers);
    JLongArrayAccessor col_key_arr(env, columnKeys);
    jsize arr_len = col_key_arr.size();
    try {
        if (arr_len == 1) {
            if (!TYPE_VALID(env, Q(nativeQueryPtr)->get_table(), col_key_arr[0], col_type_Float)) {
                return;
            }
            Q(nativeQueryPtr)->not_equal(ColKey(col_key_arr[0]), static_cast<float>(value));
        }
        else {
            LinkChain linkChain = getTableForLinkQuery(nativeQueryPtr, table_arr, col_key_arr);
            Q(nativeQueryPtr)
                ->and_query(numeric_link_notequal<Float, float, jfloat>(linkChain, col_key_arr[arr_len - 1], value));
        }
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeGreater__J_3J_3JF(JNIEnv* env, jobject,
                                                                              jlong nativeQueryPtr,
                                                                              jlongArray columnKeys,
                                                                              jlongArray tablePointers, jfloat value)
{
    JLongArrayAccessor table_arr(env, tablePointers);
    JLongArrayAccessor col_key_arr(env, columnKeys);
    jsize arr_len = col_key_arr.size();
    try {
        if (arr_len == 1) {
            if (!TYPE_VALID(env, Q(nativeQueryPtr)->get_table(), col_key_arr[0], col_type_Float)) {
                return;
            }
            Q(nativeQueryPtr)->greater(ColKey(col_key_arr[0]), static_cast<float>(value));
        }
        else {
            LinkChain linkChain = getTableForLinkQuery(nativeQueryPtr, table_arr, col_key_arr);
            Q(nativeQueryPtr)
                ->and_query(numeric_link_greater<Float, float, jfloat>(linkChain, col_key_arr[arr_len - 1], value));
        }
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeGreaterEqual__J_3J_3JF(JNIEnv* env, jobject,
                                                                                   jlong nativeQueryPtr,
                                                                                   jlongArray columnKeys,
                                                                                   jlongArray tablePointers,
                                                                                   jfloat value)
{
    JLongArrayAccessor table_arr(env, tablePointers);
    JLongArrayAccessor col_key_arr(env, columnKeys);
    jsize arr_len = col_key_arr.size();
    try {
        if (arr_len == 1) {
            if (!TYPE_VALID(env, Q(nativeQueryPtr)->get_table(), col_key_arr[0], col_type_Float)) {
                return;
            }
            Q(nativeQueryPtr)->greater_equal(ColKey(col_key_arr[0]), static_cast<float>(value));
        }
        else {
            LinkChain linkChain = getTableForLinkQuery(nativeQueryPtr, table_arr, col_key_arr);
            Q(nativeQueryPtr)
                ->and_query(numeric_link_greaterequal<Float, float, jfloat>(linkChain, col_key_arr[arr_len - 1], value));
        }
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeLess__J_3J_3JF(JNIEnv* env, jobject, jlong nativeQueryPtr,
                                                                           jlongArray columnKeys,
                                                                           jlongArray tablePointers, jfloat value)
{
    JLongArrayAccessor table_arr(env, tablePointers);
    JLongArrayAccessor col_key_arr(env, columnKeys);
    jsize arr_len = col_key_arr.size();
    try {
        if (arr_len == 1) {
            if (!TYPE_VALID(env, Q(nativeQueryPtr)->get_table(), col_key_arr[0], col_type_Float)) {
                return;
            }
            Q(nativeQueryPtr)->less(ColKey(col_key_arr[0]), static_cast<float>(value));
        }
        else {
            LinkChain linkChain = getTableForLinkQuery(nativeQueryPtr, table_arr, col_key_arr);
            Q(nativeQueryPtr)->and_query(numeric_link_less<Float, float, jfloat>(linkChain, col_key_arr[arr_len - 1], value));
        }
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeLessEqual__J_3J_3JF(JNIEnv* env, jobject,
                                                                                jlong nativeQueryPtr,
                                                                                jlongArray columnKeys,
                                                                                jlongArray tablePointers,
                                                                                jfloat value)
{
    JLongArrayAccessor table_arr(env, tablePointers);
    JLongArrayAccessor col_key_arr(env, columnKeys);
    jsize arr_len = col_key_arr.size();
    try {
        if (arr_len == 1) {
            if (!TYPE_VALID(env, Q(nativeQueryPtr)->get_table(), col_key_arr[0], col_type_Float)) {
                return;
            }
            Q(nativeQueryPtr)->less_equal(ColKey(col_key_arr[0]), static_cast<float>(value));
        }
        else {
            LinkChain linkChain = getTableForLinkQuery(nativeQueryPtr, table_arr, col_key_arr);
            Q(nativeQueryPtr)
                ->and_query(numeric_link_lessequal<Float, float, jfloat>(linkChain, col_key_arr[arr_len - 1], value));
        }
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeBetween__J_3JFF(JNIEnv* env, jobject,
                                                                               jlong nativeQueryPtr,
                                                                               jlongArray columnKeys,
                                                                               jfloat value1, jfloat value2)
{
    JLongArrayAccessor col_key_arr(env, columnKeys);
    jsize arr_len = col_key_arr.size();
    try {
        if (arr_len == 1) {
            if (!TYPE_VALID(env, Q(nativeQueryPtr)->get_table(), col_key_arr[0], col_type_Float)) {
                return;
            }
            Q(nativeQueryPtr)->between(ColKey(col_key_arr[0]), static_cast<float>(value1), static_cast<float>(value2));
        }
        else {
            ThrowException(env, IllegalArgument, "between() does not support queries using child object fields.");
        }
    }
    CATCH_STD()
}


// Double
JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeEqual__J_3J_3JD(JNIEnv* env, jobject,
                                                                            jlong nativeQueryPtr,
                                                                            jlongArray columnKeys,
                                                                            jlongArray tablePointers, jdouble value)
{
    JLongArrayAccessor table_arr(env, tablePointers);
    JLongArrayAccessor col_key_arr(env, columnKeys);
    jsize arr_len = col_key_arr.size();
    try {
        if (arr_len == 1) {
            if (!TYPE_VALID(env, Q(nativeQueryPtr)->get_table(), col_key_arr[0], col_type_Double)) {
                return;
            }
            Q(nativeQueryPtr)->equal(ColKey(col_key_arr[0]), static_cast<double>(value));
        }
        else {
            LinkChain linkChain = getTableForLinkQuery(nativeQueryPtr, table_arr, col_key_arr);
            Q(nativeQueryPtr)
                ->and_query(numeric_link_equal<Double, double, jdouble>(linkChain, col_key_arr[arr_len - 1], value));
        }
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL JNICALL Java_io_realm_internal_TableQuery_nativeNotEqual__J_3J_3JD(JNIEnv* env, jobject,
                                                                                       jlong nativeQueryPtr,
                                                                                       jlongArray columnKeys,
                                                                                       jlongArray tablePointers,
                                                                                       jdouble value)
{
    JLongArrayAccessor table_arr(env, tablePointers);
    JLongArrayAccessor col_key_arr(env, columnKeys);
    jsize arr_len = col_key_arr.size();
    try {
        if (arr_len == 1) {
            if (!TYPE_VALID(env, Q(nativeQueryPtr)->get_table(), col_key_arr[0], col_type_Double)) {
                return;
            }
            Q(nativeQueryPtr)->not_equal(ColKey(col_key_arr[0]), static_cast<double>(value));
        }
        else {
            LinkChain linkChain = getTableForLinkQuery(nativeQueryPtr, table_arr, col_key_arr);
            Q(nativeQueryPtr)
                ->and_query(numeric_link_notequal<Double, double, jdouble>(linkChain, col_key_arr[arr_len - 1], value));
        }
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeGreater__J_3J_3JD(JNIEnv* env, jobject,
                                                                              jlong nativeQueryPtr,
                                                                              jlongArray columnKeys,
                                                                              jlongArray tablePointers, jdouble value)
{
    JLongArrayAccessor table_arr(env, tablePointers);
    JLongArrayAccessor col_key_arr(env, columnKeys);
    jsize arr_len = col_key_arr.size();
    try {
        if (arr_len == 1) {
            if (!TYPE_VALID(env, Q(nativeQueryPtr)->get_table(), col_key_arr[0], col_type_Double)) {
                return;
            }
            Q(nativeQueryPtr)->greater(ColKey(col_key_arr[0]), static_cast<double>(value));
        }
        else {
            LinkChain linkChain = getTableForLinkQuery(nativeQueryPtr, table_arr, col_key_arr);
            Q(nativeQueryPtr)
                ->and_query(numeric_link_greater<Double, double, jdouble>(linkChain, col_key_arr[arr_len - 1], value));
        }
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeGreaterEqual__J_3J_3JD(JNIEnv* env, jobject,
                                                                                   jlong nativeQueryPtr,
                                                                                   jlongArray columnKeys,
                                                                                   jlongArray tablePointers,
                                                                                   jdouble value)
{
    JLongArrayAccessor table_arr(env, tablePointers);
    JLongArrayAccessor col_key_arr(env, columnKeys);
    jsize arr_len = col_key_arr.size();
    try {
        if (arr_len == 1) {
            if (!TYPE_VALID(env, Q(nativeQueryPtr)->get_table(), col_key_arr[0], col_type_Double)) {
                return;
            }
            Q(nativeQueryPtr)->greater_equal(ColKey(col_key_arr[0]), static_cast<double>(value));
        }
        else {
            LinkChain linkChain = getTableForLinkQuery(nativeQueryPtr, table_arr, col_key_arr);
            Q(nativeQueryPtr)
                ->and_query(numeric_link_greaterequal<Double, double, jdouble>(linkChain, col_key_arr[arr_len - 1], value));
        }
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeLess__J_3J_3JD(JNIEnv* env, jobject, jlong nativeQueryPtr,
                                                                           jlongArray columnKeys,
                                                                           jlongArray tablePointers, jdouble value)
{
    JLongArrayAccessor table_arr(env, tablePointers);
    JLongArrayAccessor col_key_arr(env, columnKeys);
    jsize arr_len = col_key_arr.size();
    try {
        if (arr_len == 1) {
            if (!TYPE_VALID(env, Q(nativeQueryPtr)->get_table(), col_key_arr[0], col_type_Double)) {
                return;
            }
            Q(nativeQueryPtr)->less(ColKey(col_key_arr[0]), static_cast<double>(value));
        }
        else {
            LinkChain linkChain = getTableForLinkQuery(nativeQueryPtr, table_arr, col_key_arr);
            Q(nativeQueryPtr)
                ->and_query(numeric_link_less<Double, double, jdouble>(linkChain, col_key_arr[arr_len - 1], value));
        }
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeLessEqual__J_3J_3JD(JNIEnv* env, jobject,
                                                                                jlong nativeQueryPtr,
                                                                                jlongArray columnKeys,
                                                                                jlongArray tablePointers,
                                                                                jdouble value)
{
    JLongArrayAccessor table_arr(env, tablePointers);
    JLongArrayAccessor col_key_arr(env, columnKeys);
    jsize arr_len = col_key_arr.size();
    try {
        if (arr_len == 1) {
            if (!TYPE_VALID(env, Q(nativeQueryPtr)->get_table(), col_key_arr[0], col_type_Double)) {
                return;
            }
            Q(nativeQueryPtr)->less_equal(ColKey(col_key_arr[0]), static_cast<double>(value));
        }
        else {
            LinkChain linkChain = getTableForLinkQuery(nativeQueryPtr, table_arr, col_key_arr);
            Q(nativeQueryPtr)
                ->and_query(numeric_link_lessequal<Double, double, jdouble>(linkChain, col_key_arr[arr_len - 1], value));
        }
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeBetween__J_3JDD(JNIEnv* env, jobject,
                                                                               jlong nativeQueryPtr,
                                                                               jlongArray columnKeys,
                                                                               jdouble value1, jdouble value2)
{
    JLongArrayAccessor col_key_arr(env, columnKeys);
    jsize arr_len = col_key_arr.size();
    try {
        if (arr_len == 1) {
            if (!TYPE_VALID(env, Q(nativeQueryPtr)->get_table(), col_key_arr[0], col_type_Double)) {
                return;
            }
            Q(nativeQueryPtr)->between(ColKey(col_key_arr[0]), static_cast<double>(value1), static_cast<double>(value2));
        }
        else {
            ThrowException(env, IllegalArgument, "between() does not support queries using child object fields.");
        }
    }
    CATCH_STD()
}


// Timestamp
JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeEqualTimestamp(JNIEnv* env, jobject,
                                                                              jlong nativeQueryPtr,
                                                                              jlongArray columnKeys,
                                                                              jlongArray tablePointers, jlong value)
{
    JLongArrayAccessor table_arr(env, tablePointers);
    JLongArrayAccessor col_key_arr(env, columnKeys);
    jsize arr_len = col_key_arr.size();
    try {
        if (arr_len == 1) {
            if (!TYPE_VALID(env, Q(nativeQueryPtr)->get_table(), col_key_arr[0], col_type_Timestamp)) {
                return;
            }
            Q(nativeQueryPtr)->equal(ColKey(col_key_arr[0]), from_milliseconds(value));
        }
        else {
            LinkChain linkChain = getTableForLinkQuery(nativeQueryPtr, table_arr, col_key_arr);
            Q(nativeQueryPtr)
                ->and_query(numeric_link_equal<Timestamp, Timestamp, Timestamp>(linkChain, col_key_arr[arr_len - 1],
                                                                                from_milliseconds(value)));
        }
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL JNICALL Java_io_realm_internal_TableQuery_nativeNotEqualTimestamp(JNIEnv* env, jobject,
                                                                                         jlong nativeQueryPtr,
                                                                                         jlongArray columnKeys,
                                                                                         jlongArray tablePointers,
                                                                                         jlong value)
{
    JLongArrayAccessor table_arr(env, tablePointers);
    JLongArrayAccessor col_key_arr(env, columnKeys);
    jsize arr_len = col_key_arr.size();
    try {
        if (arr_len == 1) {
            if (!TYPE_VALID(env, Q(nativeQueryPtr)->get_table(), col_key_arr[0], col_type_Timestamp)) {
                return;
            }
            Q(nativeQueryPtr)->not_equal(ColKey(col_key_arr[0]), from_milliseconds(value));
        }
        else {
            LinkChain linkChain = getTableForLinkQuery(nativeQueryPtr, table_arr, col_key_arr);
            Q(nativeQueryPtr)
                ->and_query(numeric_link_notequal<Timestamp, Timestamp, Timestamp>(linkChain, col_key_arr[arr_len - 1],
                                                                                   from_milliseconds(value)));
        }
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeGreaterTimestamp(JNIEnv* env, jobject,
                                                                                jlong nativeQueryPtr,
                                                                                jlongArray columnKeys,
                                                                                jlongArray tablePointers, jlong value)
{
    JLongArrayAccessor table_arr(env, tablePointers);
    JLongArrayAccessor col_key_arr(env, columnKeys);
    jsize arr_len = col_key_arr.size();
    try {
        if (arr_len == 1) {
            if (!TYPE_VALID(env, Q(nativeQueryPtr)->get_table(), col_key_arr[0], col_type_Timestamp)) {
                return;
            }
            Q(nativeQueryPtr)->greater(ColKey(col_key_arr[0]), from_milliseconds(value));
        }
        else {
            LinkChain linkChain = getTableForLinkQuery(nativeQueryPtr, table_arr, col_key_arr);
            Q(nativeQueryPtr)
                ->and_query(numeric_link_greater<Timestamp, Timestamp, Timestamp>(linkChain, col_key_arr[arr_len - 1],
                                                                                  from_milliseconds(value)));
        }
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeGreaterEqualTimestamp(JNIEnv* env, jobject,
                                                                                     jlong nativeQueryPtr,
                                                                                     jlongArray columnKeys,
                                                                                     jlongArray tablePointers,
                                                                                     jlong value)
{
    JLongArrayAccessor table_arr(env, tablePointers);
    JLongArrayAccessor col_key_arr(env, columnKeys);
    jsize arr_len = col_key_arr.size();
    try {
        if (arr_len == 1) {
            if (!TYPE_VALID(env, Q(nativeQueryPtr)->get_table(), col_key_arr[0], col_type_Timestamp)) {
                return;
            }
            Q(nativeQueryPtr)->greater_equal(ColKey(col_key_arr[0]), from_milliseconds(value));
        }
        else {
            LinkChain linkChain = getTableForLinkQuery(nativeQueryPtr, table_arr, col_key_arr);
            Q(nativeQueryPtr)
                ->and_query(numeric_link_greaterequal<Timestamp, Timestamp, Timestamp>(linkChain, col_key_arr[arr_len - 1],
                                                                                       from_milliseconds(value)));
        }
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeLessTimestamp(JNIEnv* env, jobject,
                                                                             jlong nativeQueryPtr,
                                                                             jlongArray columnKeys,
                                                                             jlongArray tablePointers, jlong value)
{
    JLongArrayAccessor table_arr(env, tablePointers);
    JLongArrayAccessor col_key_arr(env, columnKeys);
    jsize arr_len = col_key_arr.size();
    try {
        if (arr_len == 1) {
            if (!TYPE_VALID(env, Q(nativeQueryPtr)->get_table(), col_key_arr[0], col_type_Timestamp)) {
                return;
            }
            Q(nativeQueryPtr)->less(ColKey(col_key_arr[0]), from_milliseconds(value));
        }
        else {
            LinkChain linkChain = getTableForLinkQuery(nativeQueryPtr, table_arr, col_key_arr);
            Q(nativeQueryPtr)
                ->and_query(numeric_link_less<Timestamp, Timestamp, Timestamp>(linkChain, col_key_arr[arr_len - 1],
                                                                               from_milliseconds(value)));
        }
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeLessEqualTimestamp(JNIEnv* env, jobject,
                                                                                  jlong nativeQueryPtr,
                                                                                  jlongArray columnKeys,
                                                                                  jlongArray tablePointers,
                                                                                  jlong value)
{
    JLongArrayAccessor table_arr(env, tablePointers);
    JLongArrayAccessor col_key_arr(env, columnKeys);
    jsize arr_len = col_key_arr.size();
    try {
        if (arr_len == 1) {
            if (!TYPE_VALID(env, Q(nativeQueryPtr)->get_table(), col_key_arr[0], col_type_Timestamp)) {
                return;
            }
            Q(nativeQueryPtr)->less_equal(ColKey(col_key_arr[0]), from_milliseconds(value));
        }
        else {
            LinkChain linkChain = getTableForLinkQuery(nativeQueryPtr, table_arr, col_key_arr);
            Q(nativeQueryPtr)
                ->and_query(numeric_link_lessequal<Timestamp, Timestamp, Timestamp>(linkChain, col_key_arr[arr_len - 1],
                                                                                    from_milliseconds(value)));
        }
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeBetweenTimestamp(JNIEnv* env, jobject,
                                                                                jlong nativeQueryPtr,
                                                                                jlongArray columnKeys,
                                                                                jlong value1, jlong value2)
{
    JLongArrayAccessor col_key_arr(env, columnKeys);
    jsize arr_len = col_key_arr.size();
    try {
        if (arr_len == 1) {
            if (!TYPE_VALID(env, Q(nativeQueryPtr)->get_table(), col_key_arr[0], col_type_Timestamp)) {
                return;
            }
            Q(nativeQueryPtr)
                    ->greater_equal(ColKey(col_key_arr[0]), from_milliseconds(value1))
                    .less_equal(ColKey(col_key_arr[0]), from_milliseconds(value2));
        }
        else {
            ThrowException(env, IllegalArgument, "between() does not support queries using child object fields.");
        }
    }
    CATCH_STD()
}


// Decimal128
JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeBetweenDecimal128(JNIEnv* env, jobject,
                                                                                 jlong nativeQueryPtr,
                                                                                 jlongArray columnKeys,
                                                                                 jlong value1Low, jlong value1High,
                                                                                 jlong value2Low, jlong value2High)
{
    Decimal128::Bid128 raw1 = {static_cast<uint64_t>(value1Low), static_cast<uint64_t>(value1High)};
    Decimal128::Bid128 raw2 = {static_cast<uint64_t>(value2Low), static_cast<uint64_t>(value2High)};
    Decimal128 value1 = Decimal128(raw1);
    Decimal128 value2 = Decimal128(raw2);

    JLongArrayAccessor col_key_arr(env, columnKeys);
    jsize arr_len = col_key_arr.size();
    try {
        if (arr_len == 1) {
            if (!TYPE_VALID(env, Q(nativeQueryPtr)->get_table(), col_key_arr[0], col_type_Decimal)) {
                return;
            }
            Q(nativeQueryPtr)->between(ColKey(col_key_arr[0]), value1, value2);
        }
        else {
            ThrowException(env, IllegalArgument, "between() does not support queries using child object fields.");
        }
    }
    CATCH_STD()
}

// Bool
JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeEqual__J_3J_3JZ(JNIEnv* env, jobject,
                                                                            jlong nativeQueryPtr,
                                                                            jlongArray columnKeys,
                                                                            jlongArray tablePointers, jboolean value)
{
    JLongArrayAccessor table_arr(env, tablePointers);
    JLongArrayAccessor col_key_arr(env, columnKeys);
    jsize arr_len = col_key_arr.size();
    try {
        if (arr_len == 1) {
            if (!TYPE_VALID(env, Q(nativeQueryPtr)->get_table(), col_key_arr[0], col_type_Bool)) {
                return;
            }
            Q(nativeQueryPtr)->equal(ColKey(col_key_arr[0]), to_bool(value));
        }
        else {
            LinkChain linkChain = getTableForLinkQuery(nativeQueryPtr, table_arr, col_key_arr);
            Q(nativeQueryPtr)
                ->and_query(numeric_link_equal<Bool, bool, jboolean>(linkChain, col_key_arr[arr_len - 1], value));
        }
    }
    CATCH_STD()
}

// Decimal128
enum Decimal128Predicate { Decimal128Equal, Decimal128NotEqual, Decimal128Less, Decimal128LessEqual, Decimal128Greater, Decimal128GreaterEqual };
static void TableQuery_Decimal128Predicate(JNIEnv* env, jlong nativeQueryPtr, jlongArray columnKeys,
                                       jlongArray tablePointers, jlong low, jlong high, Decimal128Predicate predicate)
{
    try {
        JLongArrayAccessor table_arr(env, tablePointers);
        JLongArrayAccessor col_key_arr(env, columnKeys);
        jsize arr_len = col_key_arr.size();
        LinkChain linkChain = getTableForLinkQuery(nativeQueryPtr, table_arr, col_key_arr);

        Decimal128::Bid128 raw = {static_cast<uint64_t>(low), static_cast<uint64_t>(high)};
        Decimal128 decimal128 = Decimal128(raw);
        if (arr_len == 1) {
            if (!TYPE_VALID(env, Q(nativeQueryPtr)->get_table(), col_key_arr[0], col_type_Decimal)) {
                return;
            }
            switch (predicate) {
                case Decimal128Equal:
                    Q(nativeQueryPtr)->equal(ColKey(col_key_arr[0]), decimal128);
                    break;
                case Decimal128NotEqual:
                    Q(nativeQueryPtr)->not_equal(ColKey(col_key_arr[0]), decimal128);
                    break;
                case Decimal128Less:
                    Q(nativeQueryPtr)->less(ColKey(col_key_arr[0]), decimal128);
                    break;
                case Decimal128LessEqual:
                    Q(nativeQueryPtr)->less_equal(ColKey(col_key_arr[0]), decimal128);
                    break;
                case Decimal128Greater:
                    Q(nativeQueryPtr)->greater(ColKey(col_key_arr[0]), decimal128);
                    break;
                case Decimal128GreaterEqual:
                    Q(nativeQueryPtr)->greater_equal(ColKey(col_key_arr[0]), decimal128);
                    break;

            }
        }
        else {
            switch (predicate) {
                case Decimal128Equal:
                    Q(nativeQueryPtr)
                            ->and_query(linkChain.column<Decimal128>(ColKey(col_key_arr[arr_len - 1])) ==
                                                decimal128);
                    break;
                case Decimal128NotEqual:
                    Q(nativeQueryPtr)
                            ->and_query(linkChain.column<Decimal128>(ColKey(col_key_arr[arr_len - 1])) !=
                                                decimal128);
                    break;
                case Decimal128Less:
                    Q(nativeQueryPtr)
                            ->and_query(numeric_link_less<Decimal128, Decimal128, Decimal128>(linkChain, col_key_arr[arr_len - 1], decimal128));
                    break;
                case Decimal128LessEqual:
                    Q(nativeQueryPtr)
                            ->and_query(numeric_link_lessequal<Decimal128, Decimal128, Decimal128>(linkChain, col_key_arr[arr_len - 1], decimal128));
                    break;
                case Decimal128Greater:
                    Q(nativeQueryPtr)
                            ->and_query(numeric_link_greater<Decimal128, Decimal128, Decimal128>(linkChain, col_key_arr[arr_len - 1], decimal128));
                    break;
                case Decimal128GreaterEqual:
                    Q(nativeQueryPtr)
                            ->and_query(numeric_link_greaterequal<Decimal128, Decimal128, Decimal128>(linkChain, col_key_arr[arr_len - 1], decimal128));
                    break;

            }
        }
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeGreaterEqualDecimal128(JNIEnv* env, jobject,
                                                                                      jlong nativeQueryPtr,
                                                                                      jlongArray columnKeys,
                                                                                      jlongArray tablePointers,
                                                                                      jlong low,
                                                                                      jlong high)
{
    TableQuery_Decimal128Predicate(env, nativeQueryPtr, columnKeys, tablePointers, low, high, Decimal128GreaterEqual);
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeGreaterDecimal128(JNIEnv* env, jobject,
                                                                                 jlong nativeQueryPtr,
                                                                                 jlongArray columnKeys,
                                                                                 jlongArray tablePointers,
                                                                                 jlong low,
                                                                                 jlong high)
{
    TableQuery_Decimal128Predicate(env, nativeQueryPtr, columnKeys, tablePointers, low, high, Decimal128Greater);
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeLessEqualDecimal128(JNIEnv* env, jobject,
                                                                                   jlong nativeQueryPtr,
                                                                                   jlongArray columnKeys,
                                                                                   jlongArray tablePointers,
                                                                                   jlong low,
                                                                                   jlong high)
{
    TableQuery_Decimal128Predicate(env, nativeQueryPtr, columnKeys, tablePointers, low, high, Decimal128LessEqual);
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeLessDecimal128(JNIEnv* env, jobject,
                                                                              jlong nativeQueryPtr,
                                                                              jlongArray columnKeys,
                                                                              jlongArray tablePointers,
                                                                              jlong low,
                                                                              jlong high)
{
    TableQuery_Decimal128Predicate(env, nativeQueryPtr, columnKeys, tablePointers, low, high, Decimal128Less);
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeNotEqualDecimal128(JNIEnv* env, jobject,
                                                                                  jlong nativeQueryPtr,
                                                                                  jlongArray columnKeys,
                                                                                  jlongArray tablePointers,
                                                                                  jlong low,
                                                                                  jlong high)
{
    TableQuery_Decimal128Predicate(env, nativeQueryPtr, columnKeys, tablePointers, low, high, Decimal128NotEqual);
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeEqualDecimal128(JNIEnv* env, jobject,
                                                                               jlong nativeQueryPtr,
                                                                               jlongArray columnKeys,
                                                                               jlongArray tablePointers,
                                                                               jlong low,
                                                                               jlong high)
{
    TableQuery_Decimal128Predicate(env, nativeQueryPtr, columnKeys, tablePointers, low, high, Decimal128Equal);
}


// ObjectID
enum ObjectIdPredicate { ObjectIdEqual, ObjectIdNotEqual, ObjectIdLess, ObjectIdLessEqual, ObjectIdGreater, ObjectIdGreaterEqual };
static void TableQuery_ObjectIdPredicate(JNIEnv* env, jlong nativeQueryPtr, jlongArray columnKeys,
                                           jlongArray tablePointers, jstring j_data, ObjectIdPredicate predicate)
{
    try {
        JStringAccessor data(env, j_data);
        JLongArrayAccessor table_arr(env, tablePointers);
        JLongArrayAccessor col_key_arr(env, columnKeys);
        jsize arr_len = col_key_arr.size();
        LinkChain linkChain = getTableForLinkQuery(nativeQueryPtr, table_arr, col_key_arr);

        ObjectId objectId = ObjectId(StringData(data).data());
        if (arr_len == 1) {
            if (!TYPE_VALID(env, Q(nativeQueryPtr)->get_table(), col_key_arr[0], col_type_ObjectId)) {
                return;
            }

            switch (predicate) {
                case ObjectIdEqual:
                    Q(nativeQueryPtr)->equal(ColKey(col_key_arr[0]), objectId);
                    break;
                case ObjectIdNotEqual:
                    Q(nativeQueryPtr)->not_equal(ColKey(col_key_arr[0]), objectId);
                    break;
                case ObjectIdLess:
                    Q(nativeQueryPtr)->less(ColKey(col_key_arr[0]), objectId);
                    break;
                case ObjectIdLessEqual:
                    Q(nativeQueryPtr)->less_equal(ColKey(col_key_arr[0]), objectId);
                    break;
                case ObjectIdGreater:
                    Q(nativeQueryPtr)->greater(ColKey(col_key_arr[0]), objectId);
                    break;
                case ObjectIdGreaterEqual:
                    Q(nativeQueryPtr)->greater_equal(ColKey(col_key_arr[0]), objectId);
                    break;
            }
        }
        else {
            LinkChain linkChain = getTableForLinkQuery(nativeQueryPtr, table_arr, col_key_arr);
            switch (predicate) {
                case ObjectIdEqual:
                    Q(nativeQueryPtr)
                            ->and_query(linkChain.column<ObjectId>(ColKey(col_key_arr[arr_len - 1])) ==
                                        objectId);
                    break;
                case ObjectIdNotEqual:
                    Q(nativeQueryPtr)
                            ->and_query(linkChain.column<ObjectId>(ColKey(col_key_arr[arr_len - 1])) !=
                                        objectId);
                    break;
                case ObjectIdLess:
                    Q(nativeQueryPtr)
                            ->and_query(numeric_link_less<ObjectId, ObjectId, ObjectId>(linkChain, col_key_arr[arr_len - 1], objectId));
                    break;
                case ObjectIdLessEqual:
                    Q(nativeQueryPtr)
                            ->and_query(numeric_link_lessequal<ObjectId, ObjectId, ObjectId>(linkChain, col_key_arr[arr_len - 1], objectId));
                    break;
                case ObjectIdGreater:
                    Q(nativeQueryPtr)
                            ->and_query(numeric_link_greater<ObjectId, ObjectId, ObjectId>(linkChain, col_key_arr[arr_len - 1], objectId));
                    break;
                case ObjectIdGreaterEqual:
                    Q(nativeQueryPtr)
                            ->and_query(numeric_link_greaterequal<ObjectId, ObjectId, ObjectId>(linkChain, col_key_arr[arr_len - 1], objectId));
                    break;
            }
        }
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeEqualObjectId(JNIEnv* env, jobject,
                                                                             jlong nativeQueryPtr,
                                                                             jlongArray columnKeys,
                                                                             jlongArray tablePointers,
                                                                             jstring j_data)
{
    TableQuery_ObjectIdPredicate(env, nativeQueryPtr, columnKeys, tablePointers, j_data, ObjectIdEqual);
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeNotEqualObjectId(JNIEnv* env, jobject,
                                                                                jlong nativeQueryPtr,
                                                                                jlongArray columnKeys,
                                                                                jlongArray tablePointers,
                                                                                jstring data)
{
    TableQuery_ObjectIdPredicate(env, nativeQueryPtr, columnKeys, tablePointers, data, ObjectIdNotEqual);
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeLessObjectId(JNIEnv* env, jobject,
                                                                            jlong nativeQueryPtr,
                                                                            jlongArray columnKeys,
                                                                            jlongArray tablePointers,
                                                                            jstring data)
{
    TableQuery_ObjectIdPredicate(env, nativeQueryPtr, columnKeys, tablePointers, data, ObjectIdLess);
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeLessEqualObjectId(JNIEnv* env, jobject,
                                                                                 jlong nativeQueryPtr,
                                                                                 jlongArray columnKeys,
                                                                                 jlongArray tablePointers,
                                                                                 jstring data)
{
    TableQuery_ObjectIdPredicate(env, nativeQueryPtr, columnKeys, tablePointers, data, ObjectIdLessEqual);
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeGreaterObjectId(JNIEnv* env, jobject,
                                                                               jlong nativeQueryPtr,
                                                                               jlongArray columnKeys,
                                                                               jlongArray tablePointers,
                                                                               jstring data)
{
    TableQuery_ObjectIdPredicate(env, nativeQueryPtr, columnKeys, tablePointers, data, ObjectIdGreater);
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeGreaterEqualObjectId(JNIEnv* env, jobject,
                                                                                    jlong nativeQueryPtr,
                                                                                    jlongArray columnKeys,
                                                                                    jlongArray tablePointers,
                                                                                    jstring data)
{
    TableQuery_ObjectIdPredicate(env, nativeQueryPtr, columnKeys, tablePointers, data, ObjectIdGreaterEqual);
}


// String

enum StringPredicate { StringEqual, StringNotEqual, StringContains, StringBeginsWith, StringEndsWith, StringLike };

static void TableQuery_StringPredicate(JNIEnv* env, jlong nativeQueryPtr, jlongArray columnKeys,
                                       jlongArray tablePointers, jstring value,
                                       jboolean caseSensitive, StringPredicate predicate)
{
    JLongArrayAccessor table_arr(env, tablePointers);
    JLongArrayAccessor col_key_arr(env, columnKeys);
    jsize arr_len = col_key_arr.size();
    try {
        LinkChain linkChain = getTableForLinkQuery(nativeQueryPtr, table_arr, col_key_arr);
        if (value == NULL) {
            if (!COL_NULLABLE(env, linkChain.get_base_table(), col_key_arr[arr_len - 1])) {
                return;
            }
        }
        bool is_case_sensitive = to_bool(caseSensitive);
        JStringAccessor value2(env, value); // throws
        if (arr_len == 1) {
            if (!TYPE_VALID(env, Q(nativeQueryPtr)->get_table(), col_key_arr[0], col_type_String)) {
                return;
            }
            switch (predicate) {
                case StringEqual:{
                    Q(nativeQueryPtr)->equal(ColKey(col_key_arr[0]), value2, is_case_sensitive);
                    break;
                }
                case StringNotEqual:
                    Q(nativeQueryPtr)->not_equal(ColKey(col_key_arr[0]), value2, is_case_sensitive);
                    break;
                case StringContains:
                    Q(nativeQueryPtr)->contains(ColKey(col_key_arr[0]), value2, is_case_sensitive);
                    break;
                case StringBeginsWith:
                    Q(nativeQueryPtr)->begins_with(ColKey(col_key_arr[0]), value2, is_case_sensitive);
                    break;
                case StringEndsWith:
                    Q(nativeQueryPtr)->ends_with(ColKey(col_key_arr[0]), value2, is_case_sensitive);
                    break;
                case StringLike:
                    Q(nativeQueryPtr)->like(ColKey(col_key_arr[0]), value2, is_case_sensitive);
                    break;
            }
        }
        else {
            switch (predicate) {
                case StringEqual:
                    Q(nativeQueryPtr)
                        ->and_query(linkChain.column<String>(ColKey(col_key_arr[arr_len - 1]))
                                        .equal(StringData(value2), is_case_sensitive));
                    break;
                case StringNotEqual:
                    Q(nativeQueryPtr)
                        ->and_query(linkChain.column<String>(ColKey(col_key_arr[arr_len - 1]))
                                        .not_equal(StringData(value2), is_case_sensitive));
                    break;
                case StringContains:
                    Q(nativeQueryPtr)
                        ->and_query(linkChain.column<String>(ColKey(col_key_arr[arr_len - 1]))
                                        .contains(StringData(value2), is_case_sensitive));
                    break;
                case StringBeginsWith:
                    Q(nativeQueryPtr)
                        ->and_query(linkChain.column<String>(ColKey(col_key_arr[arr_len - 1]))
                                        .begins_with(StringData(value2), is_case_sensitive));
                    break;
                case StringEndsWith:
                    Q(nativeQueryPtr)
                        ->and_query(linkChain.column<String>(ColKey(col_key_arr[arr_len - 1]))
                                        .ends_with(StringData(value2), is_case_sensitive));
                    break;
                case StringLike:
                    Q(nativeQueryPtr)
                        ->and_query(linkChain.column<String>(ColKey(col_key_arr[arr_len - 1]))
                                        .like(StringData(value2), is_case_sensitive));
                    break;
            }
        }
    }
    CATCH_STD()
}

// UUID
enum UUIDPredicate { UUIDEqual, UUIDNotEqual, UUIDLess, UUIDLessEqual, UUIDGreater, UUIDGreaterEqual };
static void TableQuery_UUIDPredicate(JNIEnv* env, jlong nativeQueryPtr, jlongArray columnKeys,
                                         jlongArray tablePointers, jstring j_data, UUIDPredicate predicate)
{
    try {
        JStringAccessor data(env, j_data);
        JLongArrayAccessor table_arr(env, tablePointers);
        JLongArrayAccessor col_key_arr(env, columnKeys);
        jsize arr_len = col_key_arr.size();

        UUID uuid = UUID(StringData(data).data());
        if (arr_len == 1) {
            if (!TYPE_VALID(env, Q(nativeQueryPtr)->get_table(), col_key_arr[0], col_type_UUID)) {
                return;
            }

            switch (predicate) {
                case UUIDEqual:
                    Q(nativeQueryPtr)->equal(ColKey(col_key_arr[0]), uuid);
                    break;
                case UUIDNotEqual:
                    Q(nativeQueryPtr)->not_equal(ColKey(col_key_arr[0]), uuid);
                    break;
                case UUIDLess:
                    Q(nativeQueryPtr)->less(ColKey(col_key_arr[0]), uuid);
                    break;
                case UUIDLessEqual:
                    Q(nativeQueryPtr)->less_equal(ColKey(col_key_arr[0]), uuid);
                    break;
                case UUIDGreater:
                    Q(nativeQueryPtr)->greater(ColKey(col_key_arr[0]), uuid);
                    break;
                case UUIDGreaterEqual:
                    Q(nativeQueryPtr)->greater_equal(ColKey(col_key_arr[0]), uuid);
                    break;
            }
        }
        else {
            LinkChain linkChain = getTableForLinkQuery(nativeQueryPtr, table_arr, col_key_arr);
            switch (predicate) {
                case UUIDEqual:
                    Q(nativeQueryPtr)
                            ->and_query(linkChain.column<UUID>(ColKey(col_key_arr[arr_len - 1])) ==
                                        uuid);
                    break;
                case UUIDNotEqual:
                    Q(nativeQueryPtr)
                            ->and_query(linkChain.column<UUID>(ColKey(col_key_arr[arr_len - 1])) !=
                                        uuid);
                    break;
                case UUIDLess:
                    Q(nativeQueryPtr)
                            ->and_query(numeric_link_less<UUID, UUID, UUID>(linkChain, col_key_arr[arr_len - 1], uuid));
                    break;
                case UUIDLessEqual:
                    Q(nativeQueryPtr)
                            ->and_query(numeric_link_lessequal<UUID, UUID, UUID>(linkChain, col_key_arr[arr_len - 1], uuid));
                    break;
                case UUIDGreater:
                    Q(nativeQueryPtr)
                            ->and_query(numeric_link_greater<UUID, UUID, UUID>(linkChain, col_key_arr[arr_len - 1], uuid));
                    break;
                case UUIDGreaterEqual:
                    Q(nativeQueryPtr)
                            ->and_query(numeric_link_greaterequal<UUID, UUID, UUID>(linkChain, col_key_arr[arr_len - 1], uuid));
                    break;
            }
        }
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeEqualUUID(JNIEnv* env, jobject,
                                                                         jlong nativeQueryPtr,
                                                                         jlongArray columnKeys,
                                                                         jlongArray tablePointers,
                                                                         jstring j_data)
{
    TableQuery_UUIDPredicate(env, nativeQueryPtr, columnKeys, tablePointers, j_data, UUIDEqual);
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeNotEqualUUID(JNIEnv* env, jobject,
                                                                            jlong nativeQueryPtr,
                                                                            jlongArray columnKeys,
                                                                            jlongArray tablePointers,
                                                                            jstring j_data)
{
    TableQuery_UUIDPredicate(env, nativeQueryPtr, columnKeys, tablePointers, j_data, UUIDNotEqual);
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeLessUUID(JNIEnv* env, jobject,
                                                                        jlong nativeQueryPtr,
                                                                        jlongArray columnKeys,
                                                                        jlongArray tablePointers,
                                                                        jstring j_data)
{
    TableQuery_UUIDPredicate(env, nativeQueryPtr, columnKeys, tablePointers, j_data, UUIDLess);
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeLessEqualUUID(JNIEnv* env, jobject,
                                                                             jlong nativeQueryPtr,
                                                                             jlongArray columnKeys,
                                                                             jlongArray tablePointers,
                                                                             jstring j_data)
{
    TableQuery_UUIDPredicate(env, nativeQueryPtr, columnKeys, tablePointers, j_data, UUIDLessEqual);
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeGreaterUUID(JNIEnv* env, jobject,
                                                                           jlong nativeQueryPtr,
                                                                           jlongArray columnKeys,
                                                                           jlongArray tablePointers,
                                                                           jstring j_data)
{
    TableQuery_UUIDPredicate(env, nativeQueryPtr, columnKeys, tablePointers, j_data, UUIDGreater);
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeGreaterEqualUUID(JNIEnv* env, jobject,
                                                                                jlong nativeQueryPtr,
                                                                                jlongArray columnKeys,
                                                                                jlongArray tablePointers,
                                                                                jstring j_data)
{
    TableQuery_UUIDPredicate(env, nativeQueryPtr, columnKeys, tablePointers, j_data, UUIDGreaterEqual);
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeEqual__J_3J_3JLjava_lang_String_2Z(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlongArray columnKeys,
    jlongArray tablePointers, jstring value, jboolean caseSensitive)
{
    TableQuery_StringPredicate(env, nativeQueryPtr, columnKeys, tablePointers, value, caseSensitive, StringEqual);
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeNotEqual__J_3J_3JLjava_lang_String_2Z(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlongArray columnKeys,
    jlongArray tablePointers, jstring value, jboolean caseSensitive)
{
    TableQuery_StringPredicate(env, nativeQueryPtr, columnKeys, tablePointers, value, caseSensitive, StringNotEqual);
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeBeginsWith(JNIEnv* env, jobject, jlong nativeQueryPtr,
                                                                          jlongArray columnKeys,
                                                                          jlongArray tablePointers, jstring value,
                                                                          jboolean caseSensitive)
{
    TableQuery_StringPredicate(env, nativeQueryPtr, columnKeys, tablePointers, value, caseSensitive, StringBeginsWith);
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeEndsWith(JNIEnv* env, jobject, jlong nativeQueryPtr,
                                                                        jlongArray columnKeys,
                                                                        jlongArray tablePointers, jstring value,
                                                                        jboolean caseSensitive)
{
    TableQuery_StringPredicate(env, nativeQueryPtr, columnKeys, tablePointers, value, caseSensitive, StringEndsWith);
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeLike(JNIEnv* env, jobject, jlong nativeQueryPtr,
                                                                    jlongArray columnKeys,
                                                                    jlongArray tablePointers, jstring value,
                                                                    jboolean caseSensitive)
{
    TableQuery_StringPredicate(env, nativeQueryPtr, columnKeys, tablePointers, value, caseSensitive, StringLike);
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeContains(JNIEnv* env, jobject, jlong nativeQueryPtr,
                                                                        jlongArray columnKeys,
                                                                        jlongArray tablePointers, jstring value,
                                                                        jboolean caseSensitive)
{
    TableQuery_StringPredicate(env, nativeQueryPtr, columnKeys, tablePointers, value, caseSensitive, StringContains);
}

// Binary

enum BinaryPredicate { BinaryEqual, BinaryNotEqual };
static void TableQuery_BinaryPredicate(JNIEnv* env, jlong nativeQueryPtr, jlongArray columnKeys,
                                       jlongArray tablePointers, jbyteArray value, BinaryPredicate predicate)
{
    try {
        JLongArrayAccessor table_arr(env, tablePointers);
        JLongArrayAccessor col_key_arr(env, columnKeys);
        jsize arr_len = col_key_arr.size();
        LinkChain linkChain = getTableForLinkQuery(nativeQueryPtr, table_arr, col_key_arr);

        if (value == NULL && !COL_NULLABLE(env, linkChain.get_base_table(), col_key_arr[arr_len - 1])) {
            return;
        }

        JByteArrayAccessor jarray_accessor(env, value);
        if (arr_len == 1) {
            if (!TYPE_VALID(env, Q(nativeQueryPtr)->get_table(), col_key_arr[0], col_type_Binary)) {
                return;
            }
            switch (predicate) {
                case BinaryEqual:
                    Q(nativeQueryPtr)->equal(ColKey(col_key_arr[0]), jarray_accessor.transform<BinaryData>());
                    break;
                case BinaryNotEqual:
                    Q(nativeQueryPtr)->not_equal(ColKey(col_key_arr[0]), jarray_accessor.transform<BinaryData>());
                    break;
            }
        }
        else {
            switch (predicate) {
                case BinaryEqual:
                    Q(nativeQueryPtr)
                        ->and_query(linkChain.column<Binary>(ColKey(col_key_arr[arr_len - 1])) ==
                                    jarray_accessor.transform<BinaryData>());
                    break;
                case BinaryNotEqual:
                    Q(nativeQueryPtr)
                        ->and_query(linkChain.column<Binary>(ColKey(col_key_arr[arr_len - 1])) !=
                                    jarray_accessor.transform<BinaryData>());
                    break;
            }
        }
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeEqual__J_3J_3J_3B(JNIEnv* env, jobject,
                                                                              jlong nativeQueryPtr,
                                                                              jlongArray columnKeys,
                                                                              jlongArray tablePointers,
                                                                              jbyteArray value)
{
    TableQuery_BinaryPredicate(env, nativeQueryPtr, columnKeys, tablePointers, value, BinaryEqual);
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeNotEqual__J_3J_3J_3B(JNIEnv* env, jobject,
                                                                                 jlong nativeQueryPtr,
                                                                                 jlongArray columnKeys,
                                                                                 jlongArray tablePointers,
                                                                                 jbyteArray value)
{
    TableQuery_BinaryPredicate(env, nativeQueryPtr, columnKeys, tablePointers, value, BinaryNotEqual);
}

// General ----------------------------------------------------
// TODO:
// Some of these methods may not need the check for Table/Query validity,
// as they are called for each method when building up the query.
// Consider to reduce to just the "action" methods on Query

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeGroup(JNIEnv* env, jobject, jlong nativeQueryPtr)
{
    Query* pQuery = Q(nativeQueryPtr);
    try {
        pQuery->group();
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeEndGroup(JNIEnv* env, jobject, jlong nativeQueryPtr)
{
    Query* pQuery = Q(nativeQueryPtr);
    try {
        pQuery->end_group();
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeOr(JNIEnv* env, jobject, jlong nativeQueryPtr)
{
    // No verification of parameters needed?
    Query* pQuery = Q(nativeQueryPtr);
    try {
        pQuery->Or();
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeNot(JNIEnv* env, jobject, jlong nativeQueryPtr)
{
    Query* pQuery = Q(nativeQueryPtr);
    try {
        pQuery->Not();
    }
    CATCH_STD()
}

// Find --------------------------------------

JNIEXPORT jlong JNICALL Java_io_realm_internal_TableQuery_nativeFind(JNIEnv* env, jobject, jlong nativeQueryPtr)
{
    Query* pQuery = Q(nativeQueryPtr);
    try {
        auto r = pQuery->find();
        return to_jlong_or_not_found(r);
    }
    CATCH_STD()
    return -1;
}

// Integer Aggregates

JNIEXPORT jlong JNICALL Java_io_realm_internal_TableQuery_nativeSumInt(JNIEnv* env, jobject, jlong nativeQueryPtr,
                                                                       jlong columnKey)
{
    Query* pQuery = Q(nativeQueryPtr);
    ConstTableRef pTable = pQuery->get_table();
    if (!TYPE_VALID(env, pTable, columnKey, col_type_Int)) {
        return 0;
    }
    try {
        return pQuery->sum_int(ColKey(columnKey));
    }
    CATCH_STD()
    return 0;
}

JNIEXPORT jobject JNICALL Java_io_realm_internal_TableQuery_nativeMaximumInt(JNIEnv* env, jobject,
                                                                             jlong nativeQueryPtr, jlong columnKey)
{
    Query* pQuery = Q(nativeQueryPtr);
    ConstTableRef pTable = pQuery->get_table();
    if (!TYPE_VALID(env, pTable, columnKey, col_type_Int)) {
        return nullptr;
    }
    try {
        ObjKey return_ndx;
        int64_t result = pQuery->maximum_int(ColKey(columnKey), &return_ndx);
        if (bool(return_ndx)) {
            return JavaClassGlobalDef::new_long(env, result);
        }
        return 0;
    }
    CATCH_STD()
    return nullptr;
}

JNIEXPORT jobject JNICALL Java_io_realm_internal_TableQuery_nativeMinimumInt(JNIEnv* env, jobject,
                                                                             jlong nativeQueryPtr, jlong columnKey)
{
    Query* pQuery = Q(nativeQueryPtr);
    ConstTableRef pTable = pQuery->get_table();
    if (!TYPE_VALID(env, pTable, columnKey, col_type_Int)) {
        return nullptr;
    }
    try {
        ObjKey return_ndx;
        int64_t result = pQuery->minimum_int(ColKey(columnKey), &return_ndx);
        if (bool(return_ndx)) {
            return JavaClassGlobalDef::new_long(env, result);
        }
    }
    CATCH_STD()
    return nullptr;
}

JNIEXPORT jdouble JNICALL Java_io_realm_internal_TableQuery_nativeAverageInt(JNIEnv* env, jobject,
                                                                             jlong nativeQueryPtr, jlong columnKey)
{
    Query* pQuery = Q(nativeQueryPtr);
    ConstTableRef pTable = pQuery->get_table();
    if (!TYPE_VALID(env, pTable, columnKey, col_type_Int)) {
        return 0;
    }
    try {
        double avg = pQuery->average_int(ColKey(columnKey));
        return avg;
    }
    CATCH_STD()
    return 0;
}


// float Aggregates

JNIEXPORT jdouble JNICALL Java_io_realm_internal_TableQuery_nativeSumFloat(JNIEnv* env, jobject, jlong nativeQueryPtr,
                                                                           jlong columnKey)
{
    Query* pQuery = Q(nativeQueryPtr);
    ConstTableRef pTable = pQuery->get_table();
    if (!TYPE_VALID(env, pTable, columnKey, col_type_Float)) {
        return 0;
    }
    try {
        return pQuery->sum_float(ColKey(columnKey));
    }
    CATCH_STD()
    return 0;
}

JNIEXPORT jobject JNICALL Java_io_realm_internal_TableQuery_nativeMaximumFloat(JNIEnv* env, jobject,
                                                                               jlong nativeQueryPtr,
                                                                               jlong columnKey)
{
    Query* pQuery = Q(nativeQueryPtr);
    ConstTableRef pTable = pQuery->get_table();
    if (!TYPE_VALID(env, pTable, columnKey, col_type_Float)) {
        return nullptr;
    }
    try {
        ObjKey return_ndx;
        float result = pQuery->maximum_float(ColKey(columnKey), &return_ndx);
        if (bool(return_ndx)) {
            return JavaClassGlobalDef::new_float(env, result);
        }
    }
    CATCH_STD()
    return nullptr;
}

JNIEXPORT jobject JNICALL Java_io_realm_internal_TableQuery_nativeMinimumFloat(JNIEnv* env, jobject,
                                                                               jlong nativeQueryPtr,
                                                                               jlong columnKey)
{
    Query* pQuery = Q(nativeQueryPtr);
    ConstTableRef pTable = pQuery->get_table();
    if (!TYPE_VALID(env, pTable, columnKey, col_type_Float)) {
        return nullptr;
    }
    try {
        ObjKey return_ndx;
        float result = pQuery->minimum_float(ColKey(columnKey), &return_ndx);
        if (bool(return_ndx)) {
            return JavaClassGlobalDef::new_float(env, result);
        }
    }
    CATCH_STD()
    return nullptr;
}

JNIEXPORT jdouble JNICALL Java_io_realm_internal_TableQuery_nativeAverageFloat(JNIEnv* env, jobject,
                                                                               jlong nativeQueryPtr,
                                                                               jlong columnKey)
{
    Query* pQuery = Q(nativeQueryPtr);
    ConstTableRef pTable = pQuery->get_table();
    if (!TYPE_VALID(env, pTable, columnKey, col_type_Float)) {
        return 0;
    }
    try {
        return pQuery->average_float(ColKey(columnKey));
    }
    CATCH_STD()
    return 0;
}

// double Aggregates

JNIEXPORT jdouble JNICALL Java_io_realm_internal_TableQuery_nativeSumDouble(JNIEnv* env, jobject,
                                                                            jlong nativeQueryPtr, jlong columnKey)
{
    Query* pQuery = Q(nativeQueryPtr);
    ConstTableRef pTable = pQuery->get_table();
    if (!TYPE_VALID(env, pTable, columnKey, col_type_Double)) {
        return 0;
    }
    try {
        return pQuery->sum_double(ColKey(columnKey));
    }
    CATCH_STD()
    return 0;
}

JNIEXPORT jobject JNICALL Java_io_realm_internal_TableQuery_nativeMaximumDouble(JNIEnv* env, jobject,
                                                                                jlong nativeQueryPtr,
                                                                                jlong columnKey)
{
    Query* pQuery = Q(nativeQueryPtr);
    ConstTableRef pTable = pQuery->get_table();
    if (!TYPE_VALID(env, pTable, columnKey, col_type_Double)) {
        return nullptr;
    }
    try {
        ObjKey return_ndx;
        double result = pQuery->maximum_double(ColKey(columnKey), &return_ndx);
        if (bool(return_ndx)) {
            return JavaClassGlobalDef::new_double(env, result);
        }
    }
    CATCH_STD()
    return nullptr;
}

JNIEXPORT jlongArray JNICALL Java_io_realm_internal_TableQuery_nativeMaximumDecimal128(JNIEnv* env, jobject,
                                                                                jlong nativeQueryPtr,
                                                                                jlong columnKey)
{
    Query* pQuery = Q(nativeQueryPtr);
    ConstTableRef pTable = pQuery->get_table();
    if (!TYPE_VALID(env, pTable, columnKey, col_type_Decimal)) {
        return nullptr;
    }
    try {
        Decimal128 decimal128 = pQuery->maximum_decimal128(ColKey(columnKey));
        RETURN_DECIMAL128_AS_JLONG_ARRAY__OR_NULL(decimal128)
    }
    CATCH_STD()
    return nullptr;
}

JNIEXPORT jlongArray JNICALL Java_io_realm_internal_TableQuery_nativeSumDecimal128(JNIEnv* env, jobject,
                                                                            jlong nativeQueryPtr, jlong columnKey)
{
    Query* pQuery = Q(nativeQueryPtr);
    ConstTableRef pTable = pQuery->get_table();
    if (!TYPE_VALID(env, pTable, columnKey, col_type_Decimal)) {
        return 0;
    }
    try {

//        Decimal128 decimal128 = pQuery->sum_decimal128(ColKey(columnKey)); //FIXME waiting for Core to add sum_decimal into query.hpp
        Decimal128 decimal128 = pQuery->get_table()->sum_decimal(ColKey(columnKey));
        RETURN_DECIMAL128_AS_JLONG_ARRAY__OR_NULL(decimal128)
    }
    CATCH_STD()
    return 0;
}

JNIEXPORT jobject JNICALL Java_io_realm_internal_TableQuery_nativeMinimumDouble(JNIEnv* env, jobject,
                                                                                jlong nativeQueryPtr,
                                                                                jlong columnKey)
{
    Query* pQuery = Q(nativeQueryPtr);
    ConstTableRef pTable = pQuery->get_table();
    if (!TYPE_VALID(env, pTable, columnKey, col_type_Double)) {
        return nullptr;
    }
    try {
        ObjKey return_ndx;
        double result = pQuery->minimum_double(ColKey(columnKey), &return_ndx);
        if (bool(return_ndx)) {
            return JavaClassGlobalDef::new_double(env, result);
        }
    }
    CATCH_STD()
    return nullptr;
}

JNIEXPORT jlongArray JNICALL Java_io_realm_internal_TableQuery_nativeMinimumDecimal128(JNIEnv* env, jobject,
                                                                                jlong nativeQueryPtr,
                                                                                jlong columnKey)
{
    Query* pQuery = Q(nativeQueryPtr);
    ConstTableRef pTable = pQuery->get_table();
    if (!TYPE_VALID(env, pTable, columnKey, col_type_Decimal)) {
        return nullptr;
    }
    try {
        Decimal128 decimal128 = pQuery->minimum_decimal128(ColKey(columnKey));
        RETURN_DECIMAL128_AS_JLONG_ARRAY__OR_NULL(decimal128)
    }
    CATCH_STD()
    return nullptr;
}

JNIEXPORT jdouble JNICALL Java_io_realm_internal_TableQuery_nativeAverageDouble(JNIEnv* env, jobject,
                                                                                jlong nativeQueryPtr,
                                                                                jlong columnKey)
{
    Query* pQuery = Q(nativeQueryPtr);
    ConstTableRef pTable = pQuery->get_table();
    if (!TYPE_VALID(env, pTable, columnKey, col_type_Double)) {
        return 0;
    }
    try {
        return pQuery->average_double(ColKey(columnKey));
    }
    CATCH_STD()
    return 0;
}

JNIEXPORT jlongArray JNICALL Java_io_realm_internal_TableQuery_nativeAverageDecimal128(JNIEnv* env, jobject,
                                                                                jlong nativeQueryPtr,
                                                                                jlong columnKey)
{
    Query* pQuery = Q(nativeQueryPtr);
    ConstTableRef pTable = pQuery->get_table();
    if (!TYPE_VALID(env, pTable, columnKey, col_type_Decimal)) {
        return nullptr;
    }
    try {
        Decimal128 decimal128 = pQuery->average_decimal128(ColKey(columnKey));
        RETURN_DECIMAL128_AS_JLONG_ARRAY__OR_NULL(decimal128)
    }
    CATCH_STD()
    return nullptr;
}

// date aggregates
// FIXME: This is a rough workaround while waiting for https://github.com/realm/realm-core/issues/1745 to be solved
JNIEXPORT jobject JNICALL Java_io_realm_internal_TableQuery_nativeMaximumTimestamp(JNIEnv* env, jobject,
                                                                                   jlong nativeQueryPtr,
                                                                                   jlong columnKey)
{
    Query* pQuery = Q(nativeQueryPtr);
    ConstTableRef pTable = pQuery->get_table();
    if (!TYPE_VALID(env, pTable, columnKey, col_type_Timestamp)) {
        return nullptr;
    }
    try {
        ObjKey return_ndx;
        Timestamp result = pQuery->find_all().maximum_timestamp(ColKey(columnKey), &return_ndx);
        if (bool(return_ndx) && !result.is_null()) {
            return JavaClassGlobalDef::new_long(env, to_milliseconds(result));
        }
    }
    CATCH_STD()
    return nullptr;
}

JNIEXPORT jobject JNICALL Java_io_realm_internal_TableQuery_nativeMinimumTimestamp(JNIEnv* env, jobject,
                                                                                   jlong nativeQueryPtr,
                                                                                   jlong columnKey)
{
    Query* pQuery = Q(nativeQueryPtr);
    ConstTableRef pTable = pQuery->get_table();
    if (!TYPE_VALID(env, pTable, columnKey, col_type_Timestamp)) {
        return nullptr;
    }
    try {
        ObjKey return_ndx;
        Timestamp result = pQuery->find_all().minimum_timestamp(ColKey(columnKey), &return_ndx);
        if (bool(return_ndx) && !result.is_null()) {
            return JavaClassGlobalDef::new_long(env, to_milliseconds(result));
        }
    }
    CATCH_STD()
    return nullptr;
}

// Count, Remove

JNIEXPORT jlong JNICALL Java_io_realm_internal_TableQuery_nativeCount(JNIEnv* env, jobject, jlong nativeQueryPtr)
{
    Query* pQuery = Q(nativeQueryPtr);
    try {
        return static_cast<jlong>(pQuery->count());
    }
    CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_TableQuery_nativeRemove(JNIEnv* env, jobject, jlong nativeQueryPtr)
{
    Query* pQuery = Q(nativeQueryPtr);
    try {
        return static_cast<jlong>(pQuery->remove());
    }
    CATCH_STD()
    return 0;
}

// isNull and isNotNull
JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeIsNull(JNIEnv* env, jobject, jlong nativeQueryPtr,
                                                                      jlongArray columnKeys,
                                                                      jlongArray tablePointers)
{
    try {
        JLongArrayAccessor table_arr(env, tablePointers);
        JLongArrayAccessor col_key_arr(env, columnKeys);
        jsize arr_len = col_key_arr.size();
        auto pQuery = reinterpret_cast<Query *>(nativeQueryPtr);

        jlong column_idx = col_key_arr[arr_len - 1];

        ConstTableRef table_ref = getTableByArray(nativeQueryPtr, table_arr, col_key_arr);
        if (!isNullable(env, reinterpret_cast<ConstTableRef*>(table_arr[arr_len - 1]), table_ref, column_idx)) {
            return;
        }

        LinkChain linkChain = getTableForLinkQuery(nativeQueryPtr, table_arr, col_key_arr);
        DataType col_type = table_ref->get_column_type(ColKey(column_idx));
        if (arr_len == 1) {
            switch (col_type) {
                case type_Link:
                    pQuery->and_query(linkChain.column<Link>(ColKey(column_idx)).is_null());
                    break;
                case type_LinkList:
                    // Cannot get here. Exception will be thrown in TBL_AND_COL_NULLABLE
                    ThrowException(env, FatalError, "This is not reachable.");
                    break;
                case type_Binary:
                    pQuery->equal(ColKey(column_idx), BinaryData());
                    break;
                case type_String:
                case type_Bool:
                case type_Int:
                case type_Float:
                case type_Double:
                case type_Timestamp:
                case type_Decimal:
                case type_ObjectId:
                case type_UUID:
                    Q(nativeQueryPtr)->equal(ColKey(column_idx), realm::null());
                    break;
                default:
                    REALM_UNREACHABLE();
            }
        }
        else {
            switch (col_type) {
                case type_Link:
                    ThrowException(env, IllegalArgument, "isNull() by nested query for link field is not supported.");
                    break;
                case type_LinkList:
                    // Cannot get here. Exception will be thrown in TBL_AND_COL_NULLABLE
                    ThrowException(env, FatalError, "This is not reachable.");
                    break;
                case type_String:
                    pQuery->and_query(linkChain.column<String>(ColKey(column_idx)) == realm::null());
                    break;
                case type_Binary:
                    pQuery->and_query(linkChain.column<Binary>(ColKey(column_idx)) == BinaryData());
                    break;
                case type_Bool:
                    pQuery->and_query(linkChain.column<Bool>(ColKey(column_idx)) == realm::null());
                    break;
                case type_Int:
                    pQuery->and_query(linkChain.column<Int>(ColKey(column_idx)) == realm::null());
                    break;
                case type_Float:
                    pQuery->and_query(linkChain.column<Float>(ColKey(column_idx)) == realm::null());
                    break;
                case type_Double:
                    pQuery->and_query(linkChain.column<Double>(ColKey(column_idx)) == realm::null());
                    break;
                case type_Timestamp:
                    pQuery->and_query(linkChain.column<Timestamp>(ColKey(column_idx)) == realm::null());
                    break;
                case type_Decimal:
                    pQuery->and_query(linkChain.column<Decimal128>(ColKey(column_idx)) == realm::null());
                    break;
                case type_ObjectId:
                    pQuery->and_query(linkChain.column<ObjectId>(ColKey(column_idx)) == realm::null());
                    break;
                case type_UUID:
                    pQuery->and_query(linkChain.column<UUID>(ColKey(column_idx)) == realm::null());
                    break;
                default:
                    REALM_UNREACHABLE();
            }
        }
    }
    CATCH_STD()
}
JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeIsNotNull(JNIEnv* env, jobject, jlong nativeQueryPtr,
                                                                         jlongArray columnKeys,
                                                                         jlongArray tablePointers)
{
    JLongArrayAccessor table_arr(env, tablePointers);
    JLongArrayAccessor col_key_arr(env, columnKeys);
    jsize arr_len = col_key_arr.size();
    Query* pQuery = Q(nativeQueryPtr);
    try {
        jlong column_idx = col_key_arr[arr_len - 1];

        ConstTableRef table_ref = getTableByArray(nativeQueryPtr, table_arr, col_key_arr);
        if (!isNullable(env, reinterpret_cast<TableRef*>(table_arr[arr_len - 1]), table_ref, column_idx)) {
            return;
        }

        LinkChain linkChain = getTableForLinkQuery(nativeQueryPtr, table_arr, col_key_arr);

        DataType col_type = table_ref->get_column_type(ColKey(column_idx));
        if (arr_len == 1) {
            switch (col_type) {
                case type_Link:
                    pQuery->and_query(linkChain.column<Link>(ColKey(column_idx)).is_not_null());
                    break;
                case type_LinkList:
                    // Cannot get here. Exception will be thrown in TBL_AND_COL_NULLABLE
                    ThrowException(env, FatalError, "This is not reachable.");
                    break;
                case type_Binary:
                    pQuery->not_equal(ColKey(column_idx), realm::BinaryData());
                    break;
                case type_String:
                case type_Bool:
                case type_Int:
                case type_Float:
                case type_Double:
                case type_Timestamp:
                case type_Decimal:
                case type_ObjectId:
                case type_UUID:
                    pQuery->not_equal(ColKey(column_idx), realm::null());
                    break;
                default:
                    REALM_UNREACHABLE();
            }
        }
        else {
            switch (col_type) {
                case type_Link:
                    ThrowException(env, IllegalArgument,
                                   "isNotNull() by nested query for link field is not supported.");
                    break;
                case type_LinkList:
                    // Cannot get here. Exception will be thrown in TBL_AND_COL_NULLABLE
                    ThrowException(env, FatalError, "This is not reachable.");
                    break;
                case type_String:
                    pQuery->and_query(linkChain.column<String>(ColKey(column_idx)) != realm::null());
                    break;
                case type_Binary:
                    pQuery->and_query(linkChain.column<Binary>(ColKey(column_idx)) != realm::BinaryData());
                    break;
                case type_Bool:
                    pQuery->and_query(linkChain.column<Bool>(ColKey(column_idx)) != realm::null());
                    break;
                case type_Int:
                    pQuery->and_query(linkChain.column<Int>(ColKey(column_idx)) != realm::null());
                    break;
                case type_Float:
                    pQuery->and_query(linkChain.column<Float>(ColKey(column_idx)) != realm::null());
                    break;
                case type_Double:
                    pQuery->and_query(linkChain.column<Double>(ColKey(column_idx)) != realm::null());
                    break;
                case type_Timestamp:
                    pQuery->and_query(linkChain.column<Timestamp>(ColKey(column_idx)) != realm::null());
                    break;
                case type_Decimal:
                    pQuery->and_query(linkChain.column<Decimal128>(ColKey(column_idx)) != realm::null());
                    break;
                case type_ObjectId:
                    pQuery->and_query(linkChain.column<ObjectId>(ColKey(column_idx)) != realm::null());
                    break;
                case type_UUID:
                    pQuery->and_query(linkChain.column<UUID>(ColKey(column_idx)) != realm::null());
                    break;
                default:
                    REALM_UNREACHABLE();
            }
        }
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeIsEmpty(JNIEnv* env, jobject, jlong nativeQueryPtr,
                                                                       jlongArray columnKeys,
                                                                       jlongArray tablePointers)
{
    JLongArrayAccessor table_arr(env, tablePointers);
    JLongArrayAccessor col_arr(env, columnKeys);
    jsize arr_len = col_arr.size();
    Query* pQuery = reinterpret_cast<Query *>(nativeQueryPtr);
    try {
        LinkChain linkChain = getTableForLinkQuery(nativeQueryPtr, table_arr, col_arr);
        ColKey column_idx = ColKey(col_arr[arr_len - 1]);

        // Support a backlink as the last column in a field descriptor
        if (table_arr[arr_len-1]) {
            pQuery->and_query(linkChain.column<BackLink>(*TBL_REF(table_arr[arr_len-1]), column_idx).count() == 0);
            return;
        }

        ConstTableRef table_ref = getTableByArray(nativeQueryPtr, table_arr, col_arr);
        DataType col_type = table_ref->get_column_type(column_idx);
        if (arr_len == 1) {
            // Field queries
            switch (col_type) {
                case type_Binary:
                    pQuery->equal(column_idx, BinaryData("", 0));
                    break;
                case type_LinkList:
                    pQuery->and_query(linkChain.column<Link>(column_idx).count() == 0);
                    break;
                case type_String:
                    pQuery->equal(column_idx, "");
                    break;
                case type_Link:
                case type_Bool:
                case type_Int:
                case type_Float:
                case type_Double:
                case type_Timestamp:
                default:
                    ThrowException(env, IllegalArgument, "isEmpty() only works on String, byte[] and RealmList.");
                    return;
            }
        }
        else {
            // Linked queries
            switch (col_type) {
                case type_Binary:
                    pQuery->and_query(linkChain.column<Binary>(column_idx) == BinaryData("", 0));
                    break;
                case type_LinkList:
                    pQuery->and_query(linkChain.column<Link>(column_idx).count() == 0);
                    break;
                case type_String:
                    pQuery->and_query(linkChain.column<String>(column_idx) == "");
                    break;
                case type_Link:
                case type_Bool:
                case type_Int:
                case type_Float:
                case type_Double:
                case type_Timestamp:
                default:
                    ThrowException(env, IllegalArgument,
                                   "isEmpty() only works on String, byte[] and RealmList across links.");
                    return;
            }
        }
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL
Java_io_realm_internal_TableQuery_nativeIsNotEmpty(JNIEnv *env, jobject, jlong nativeQueryPtr,
                                                   jlongArray columnKeys, jlongArray tablePointers) {
    JLongArrayAccessor table_arr(env, tablePointers);
    JLongArrayAccessor col_arr(env, columnKeys);
    jsize arr_len = col_arr.size();
    Query* pQuery = reinterpret_cast<Query *>(nativeQueryPtr);
    try {
        LinkChain linkChain = getTableForLinkQuery(nativeQueryPtr, table_arr, col_arr);
        ColKey column_idx = ColKey(col_arr[arr_len - 1]);

        // Support a backlink as the last column in a field descriptor
        if (table_arr[arr_len-1]) {
            pQuery->and_query(linkChain.column<BackLink>(*TBL_REF(table_arr[arr_len-1]), column_idx).count() != 0);
            return;
        }

        ConstTableRef table_ref = getTableByArray(nativeQueryPtr, table_arr, col_arr);
        DataType col_type = table_ref->get_column_type(column_idx);
        if (arr_len == 1) {
            // Field queries
            switch (col_type) {
                case type_Binary:
                    pQuery->not_equal(column_idx, BinaryData("", 0));
                    break;
                case type_LinkList:
                    pQuery->and_query(linkChain.column<Link>(column_idx).count() != 0);
                    break;
                case type_String:
                    pQuery->not_equal(column_idx, "");
                    break;
                case type_Link:
                case type_Bool:
                case type_Int:
                case type_Float:
                case type_Double:
                case type_Timestamp:
                default:
                    ThrowException(env, IllegalArgument, "isNotEmpty() only works on String, byte[] and RealmList.");
                    return;
            }
        }
        else {
            // Linked queries
            switch (col_type) {
                case type_Binary:
                    pQuery->and_query(linkChain.column<Binary>(column_idx) != BinaryData("", 0));
                    break;
                case type_LinkList:
                    pQuery->and_query(linkChain.column<Link>(column_idx).count() != 0);
                    break;
                case type_String:
                    pQuery->and_query(linkChain.column<String>(column_idx) != "");
                    break;
                case type_Link:
                case type_Bool:
                case type_Int:
                case type_Float:
                case type_Double:
                case type_Timestamp:
                default:
                    ThrowException(env, IllegalArgument,
                                   "isNotEmpty() only works on String, byte[] and RealmList across links.");
                    return;
            }
        }
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL
Java_io_realm_internal_TableQuery_nativeAlwaysFalse(JNIEnv *env, jobject, jlong nativeQueryPtr) {
    try {
        Query* query = reinterpret_cast<Query *>(nativeQueryPtr);
        query->and_query(std::unique_ptr<Expression>(new FalseExpression));
    }
    CATCH_STD()

}

JNIEXPORT void JNICALL
Java_io_realm_internal_TableQuery_nativeAlwaysTrue(JNIEnv *env, jobject, jlong nativeQueryPtr) {
    try {
        Query* query = reinterpret_cast<Query *>(nativeQueryPtr);
        query->and_query(std::unique_ptr<Expression>(new TrueExpression));
    }
    CATCH_STD()
}

static void finalize_table_query(jlong ptr)
{
    delete Q(ptr);
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_TableQuery_nativeGetFinalizerPtr(JNIEnv*, jclass)
{
    return reinterpret_cast<jlong>(&finalize_table_query);
}
