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

#include <shared_realm.hpp>
#include <object_store.hpp>
#include <results.hpp>

#include "java_accessor.hpp"
#include "java_class_global_def.hpp"
#include "util.hpp"

using namespace realm;
using namespace realm::jni_util;
using namespace realm::_impl;

#if 1
#define QUERY_COL_TYPE_VALID(env, jPtr, col, type) query_col_type_valid(env, jPtr, col, type)
#else
#define QUERY_COL_TYPE_VALID(env, jPtr, col, type) (true)
#endif

static void finalize_table_query(jlong ptr);

inline bool query_col_type_valid(JNIEnv* env, jlong nativeQueryPtr, jlong colIndex, DataType type)
{
    return TBL_AND_COL_INDEX_AND_TYPE_VALID(env, Q(nativeQueryPtr)->get_table().get(), colIndex, type);
}


const char* ERR_IMPORT_CLOSED_REALM = "Can not import results from a closed Realm";
const char* ERR_SORT_NOT_SUPPORTED = "Sort is not supported on binary data, object references and RealmList";
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

// Return TableRef used for build link queries
// Each element in the indicesArray is the index of a column to be used to link to the next TableRef.
// If the corresponding entry in tablesArray is anything other than a nullptr, the link is a backlink.
// In that case, the tablesArray element is the pointer to the backlink source table and the
// indicesArray entry is the source column index in the source table.
static TableRef getTableForLinkQuery(jlong nativeQueryPtr, const JLongArrayAccessor& tablesArray,
                                     const JLongArrayAccessor& indicesArray)
{
    auto table_ref = reinterpret_cast<Query *>(nativeQueryPtr)->get_table();
    jsize link_element_count = indicesArray.size() - 1;
    for (int i = 0; i < link_element_count; ++i) {
        auto col_index = size_t(indicesArray[i]);
        auto table_ptr = reinterpret_cast<Table *>(tablesArray[i]);
        if (table_ptr == nullptr) {
            table_ref->link(col_index);
        }
        else {
            table_ref->backlink(*table_ptr, col_index);
        }
    }
    return table_ref;
}

// Return TableRef point to original table or the link table
static TableRef getTableByArray(jlong nativeQueryPtr, const JLongArrayAccessor& tablesArray,
                                const JLongArrayAccessor& indicesArray)
{
    auto table_ref = reinterpret_cast<Query *>(nativeQueryPtr)->get_table();
    jsize link_element_count = indicesArray.size() - 1;
    for (int i = 0; i < link_element_count; ++i) {
        auto table_ptr = reinterpret_cast<Table *>(tablesArray[i]);
        if (table_ptr == nullptr) {
            table_ref = table_ref->get_link_target(static_cast<size_t>(indicesArray[i]));
        }
        else {
            table_ref = TableRef(table_ptr);
        }
    }
    return table_ref;
}

// I am not at all sure that it is even the right idea, let alone correct code. --gbm
static bool isNullable(JNIEnv* env, Table* src_table_ptr, TableRef table_ref, jlong column_idx)
{
    // if table_arr is not a nullptr, this is a backlink and not allowed.
    if (src_table_ptr != nullptr) {
        ThrowException(env, IllegalArgument, "LinkingObject from field " + std::string(src_table_ptr->get_column_name(column_idx)) + " is not nullable.");
        return false;
    }
    if (!TBL_AND_COL_NULLABLE(env, table_ref.get(), column_idx)) {
        return false;
    }
    return true;
}

template <typename coretype, typename cpptype, typename javatype>
Query numeric_link_equal(TableRef tbl, jlong columnIndex, javatype value)
{
    return tbl->column<coretype>(size_t(columnIndex)) == cpptype(value);
}

template <typename coretype, typename cpptype, typename javatype>
Query numeric_link_notequal(TableRef tbl, jlong columnIndex, javatype value)
{
    return tbl->column<coretype>(size_t(columnIndex)) != cpptype(value);
}

template <typename coretype, typename cpptype, typename javatype>
Query numeric_link_greater(TableRef tbl, jlong columnIndex, javatype value)
{
    return tbl->column<coretype>(size_t(columnIndex)) > cpptype(value);
}

template <typename coretype, typename cpptype, typename javatype>
Query numeric_link_greaterequal(TableRef tbl, jlong columnIndex, javatype value)
{
    return tbl->column<coretype>(size_t(columnIndex)) >= cpptype(value);
}

template <typename coretype, typename cpptype, typename javatype>
Query numeric_link_less(TableRef tbl, jlong columnIndex, javatype value)
{
    return tbl->column<coretype>(size_t(columnIndex)) < cpptype(value);
}

template <typename coretype, typename cpptype, typename javatype>
Query numeric_link_lessequal(TableRef tbl, jlong columnIndex, javatype value)
{
    return tbl->column<coretype>(size_t(columnIndex)) <= cpptype(value);
}


// Integer

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeEqual__J_3J_3JJ(JNIEnv* env, jobject,
                                                                            jlong nativeQueryPtr,
                                                                            jlongArray columnIndexes,
                                                                            jlongArray tablePointers, jlong value)
{
    JLongArrayAccessor table_arr(env, tablePointers);
    JLongArrayAccessor index_arr(env, columnIndexes);
    jsize arr_len = index_arr.size();
    try {
        if (arr_len == 1) {
            if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, index_arr[0], type_Int)) {
                return;
            }
            Q(nativeQueryPtr)->equal(S(index_arr[0]), static_cast<int64_t>(value));
        }
        else {
            TableRef table_ref = getTableForLinkQuery(nativeQueryPtr, table_arr, index_arr);
            Q(nativeQueryPtr)->and_query(numeric_link_equal<Int, int64_t, jlong>(table_ref, index_arr[arr_len - 1], value));
        }
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL JNICALL Java_io_realm_internal_TableQuery_nativeNotEqual__J_3J_3JJ(JNIEnv* env, jobject,
                                                                                       jlong nativeQueryPtr,
                                                                                       jlongArray columnIndexes,
                                                                                       jlongArray tablePointers,
                                                                                       jlong value)
{
    JLongArrayAccessor table_arr(env, tablePointers);
    JLongArrayAccessor index_arr(env, columnIndexes);
    jsize arr_len = index_arr.size();
    try {
        if (arr_len == 1) {
            if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, index_arr[0], type_Int)) {
                return;
            }
            Q(nativeQueryPtr)->not_equal(S(index_arr[0]), static_cast<int64_t>(value));
        }
        else {
            TableRef table_ref = getTableForLinkQuery(nativeQueryPtr, table_arr, index_arr);
            Q(nativeQueryPtr)
                ->and_query(numeric_link_notequal<Int, int64_t, jlong>(table_ref, index_arr[arr_len - 1], value));
        }
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeGreater__J_3J_3JJ(JNIEnv* env, jobject,
                                                                              jlong nativeQueryPtr,
                                                                              jlongArray columnIndexes,
                                                                              jlongArray tablePointers, jlong value)
{
    JLongArrayAccessor table_arr(env, tablePointers);
    JLongArrayAccessor index_arr(env, columnIndexes);
    jsize arr_len = index_arr.size();
    try {
        if (arr_len == 1) {
            if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, index_arr[0], type_Int)) {
                return;
            }
            Q(nativeQueryPtr)->greater(S(index_arr[0]), static_cast<int64_t>(value));
        }
        else {
            TableRef table_ref = getTableForLinkQuery(nativeQueryPtr, table_arr, index_arr);
            Q(nativeQueryPtr)
                ->and_query(numeric_link_greater<Int, int64_t, jlong>(table_ref, index_arr[arr_len - 1], value));
        }
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeGreaterEqual__J_3J_3JJ(JNIEnv* env, jobject,
                                                                                   jlong nativeQueryPtr,
                                                                                   jlongArray columnIndexes,
                                                                                   jlongArray tablePointers,
                                                                                   jlong value)
{
    JLongArrayAccessor table_arr(env, tablePointers);
    JLongArrayAccessor index_arr(env, columnIndexes);
    jsize arr_len = index_arr.size();
    try {
        if (arr_len == 1) {
            if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, index_arr[0], type_Int)) {
                return;
            }
            Q(nativeQueryPtr)->greater_equal(S(index_arr[0]), static_cast<int64_t>(value));
        }
        else {
            TableRef table_ref = getTableForLinkQuery(nativeQueryPtr, table_arr, index_arr);
            Q(nativeQueryPtr)
                ->and_query(numeric_link_greaterequal<Int, int64_t, jlong>(table_ref, index_arr[arr_len - 1], value));
        }
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeLess__J_3J_3JJ(JNIEnv* env, jobject, jlong nativeQueryPtr,
                                                                           jlongArray columnIndexes,
                                                                           jlongArray tablePointers, jlong value)
{
    JLongArrayAccessor table_arr(env, tablePointers);
    JLongArrayAccessor index_arr(env, columnIndexes);
    jsize arr_len = index_arr.size();
    try {
        if (arr_len == 1) {
            if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, index_arr[0], type_Int)) {
                return;
            }
            Q(nativeQueryPtr)->less(S(index_arr[0]), static_cast<int64_t>(value));
        }
        else {
            TableRef table_ref = getTableForLinkQuery(nativeQueryPtr, table_arr, index_arr);
            Q(nativeQueryPtr)->and_query(numeric_link_less<Int, int64_t, jlong>(table_ref, index_arr[arr_len - 1], value));
        }
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeLessEqual__J_3J_3JJ(JNIEnv* env, jobject,
                                                                                jlong nativeQueryPtr,
                                                                                jlongArray columnIndexes,
                                                                                jlongArray tablePointers, jlong value)
{
    JLongArrayAccessor table_arr(env, tablePointers);
    JLongArrayAccessor index_arr(env, columnIndexes);
    jsize arr_len = index_arr.size();
    try {
        if (arr_len == 1) {
            if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, index_arr[0], type_Int)) {
                return;
            }
            Q(nativeQueryPtr)->less_equal(S(index_arr[0]), static_cast<int64_t>(value));
        }
        else {
            TableRef table_ref = getTableForLinkQuery(nativeQueryPtr, table_arr, index_arr);
            Q(nativeQueryPtr)
                ->and_query(numeric_link_lessequal<Int, int64_t, jlong>(table_ref, index_arr[arr_len - 1], value));
        }
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeBetween__J_3JJJ(JNIEnv* env, jobject,
                                                                               jlong nativeQueryPtr,
                                                                               jlongArray columnIndexes, jlong value1,
                                                                               jlong value2)
{
    JLongArrayAccessor arr(env, columnIndexes);
    jsize arr_len = arr.size();
    if (arr_len == 1) {
        if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, arr[0], type_Int)) {
            return;
        }
        try {
            Q(nativeQueryPtr)->between(S(arr[0]), static_cast<int64_t>(value1), static_cast<int64_t>(value2));
        }
        CATCH_STD()
    }
    else {
        ThrowException(env, IllegalArgument, "between() does not support queries using child object fields.");
    }
}

// Float

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeEqual__J_3J_3JF(JNIEnv* env, jobject,
                                                                            jlong nativeQueryPtr,
                                                                            jlongArray columnIndexes,
                                                                            jlongArray tablePointers, jfloat value)
{
    JLongArrayAccessor table_arr(env, tablePointers);
    JLongArrayAccessor index_arr(env, columnIndexes);
    jsize arr_len = index_arr.size();
    try {
        if (arr_len == 1) {
            if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, index_arr[0], type_Float)) {
                return;
            }
            Q(nativeQueryPtr)->equal(S(index_arr[0]), static_cast<float>(value));
        }
        else {
            TableRef table_ref = getTableForLinkQuery(nativeQueryPtr, table_arr, index_arr);
            Q(nativeQueryPtr)
                ->and_query(numeric_link_equal<Float, float, jfloat>(table_ref, index_arr[arr_len - 1], value));
        }
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL JNICALL Java_io_realm_internal_TableQuery_nativeNotEqual__J_3J_3JF(JNIEnv* env, jobject,
                                                                                       jlong nativeQueryPtr,
                                                                                       jlongArray columnIndexes,
                                                                                       jlongArray tablePointers,
                                                                                       jfloat value)
{
    JLongArrayAccessor table_arr(env, tablePointers);
    JLongArrayAccessor index_arr(env, columnIndexes);
    jsize arr_len = index_arr.size();
    try {
        if (arr_len == 1) {
            if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, index_arr[0], type_Float)) {
                return;
            }
            Q(nativeQueryPtr)->not_equal(S(index_arr[0]), static_cast<float>(value));
        }
        else {
            TableRef table_ref = getTableForLinkQuery(nativeQueryPtr, table_arr, index_arr);
            Q(nativeQueryPtr)
                ->and_query(numeric_link_notequal<Float, float, jfloat>(table_ref, index_arr[arr_len - 1], value));
        }
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeGreater__J_3J_3JF(JNIEnv* env, jobject,
                                                                              jlong nativeQueryPtr,
                                                                              jlongArray columnIndexes,
                                                                              jlongArray tablePointers, jfloat value)
{
    JLongArrayAccessor table_arr(env, tablePointers);
    JLongArrayAccessor index_arr(env, columnIndexes);
    jsize arr_len = index_arr.size();
    try {
        if (arr_len == 1) {
            if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, index_arr[0], type_Float)) {
                return;
            }
            Q(nativeQueryPtr)->greater(S(index_arr[0]), static_cast<float>(value));
        }
        else {
            TableRef table_ref = getTableForLinkQuery(nativeQueryPtr, table_arr, index_arr);
            Q(nativeQueryPtr)
                ->and_query(numeric_link_greater<Float, float, jfloat>(table_ref, index_arr[arr_len - 1], value));
        }
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeGreaterEqual__J_3J_3JF(JNIEnv* env, jobject,
                                                                                   jlong nativeQueryPtr,
                                                                                   jlongArray columnIndexes,
                                                                                   jlongArray tablePointers,
                                                                                   jfloat value)
{
    JLongArrayAccessor table_arr(env, tablePointers);
    JLongArrayAccessor index_arr(env, columnIndexes);
    jsize arr_len = index_arr.size();
    try {
        if (arr_len == 1) {
            if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, index_arr[0], type_Float)) {
                return;
            }
            Q(nativeQueryPtr)->greater_equal(S(index_arr[0]), static_cast<float>(value));
        }
        else {
            TableRef table_ref = getTableForLinkQuery(nativeQueryPtr, table_arr, index_arr);
            Q(nativeQueryPtr)
                ->and_query(numeric_link_greaterequal<Float, float, jfloat>(table_ref, index_arr[arr_len - 1], value));
        }
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeLess__J_3J_3JF(JNIEnv* env, jobject, jlong nativeQueryPtr,
                                                                           jlongArray columnIndexes,
                                                                           jlongArray tablePointers, jfloat value)
{
    JLongArrayAccessor table_arr(env, tablePointers);
    JLongArrayAccessor index_arr(env, columnIndexes);
    jsize arr_len = index_arr.size();
    try {
        if (arr_len == 1) {
            if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, index_arr[0], type_Float)) {
                return;
            }
            Q(nativeQueryPtr)->less(S(index_arr[0]), static_cast<float>(value));
        }
        else {
            TableRef table_ref = getTableForLinkQuery(nativeQueryPtr, table_arr, index_arr);
            Q(nativeQueryPtr)->and_query(numeric_link_less<Float, float, jfloat>(table_ref, index_arr[arr_len - 1], value));
        }
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeLessEqual__J_3J_3JF(JNIEnv* env, jobject,
                                                                                jlong nativeQueryPtr,
                                                                                jlongArray columnIndexes,
                                                                                jlongArray tablePointers,
                                                                                jfloat value)
{
    JLongArrayAccessor table_arr(env, tablePointers);
    JLongArrayAccessor index_arr(env, columnIndexes);
    jsize arr_len = index_arr.size();
    try {
        if (arr_len == 1) {
            if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, index_arr[0], type_Float)) {
                return;
            }
            Q(nativeQueryPtr)->less_equal(S(index_arr[0]), static_cast<float>(value));
        }
        else {
            TableRef table_ref = getTableForLinkQuery(nativeQueryPtr, table_arr, index_arr);
            Q(nativeQueryPtr)
                ->and_query(numeric_link_lessequal<Float, float, jfloat>(table_ref, index_arr[arr_len - 1], value));
        }
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeBetween__J_3JFF(JNIEnv* env, jobject,
                                                                               jlong nativeQueryPtr,
                                                                               jlongArray columnIndexes,
                                                                               jfloat value1, jfloat value2)
{
    JLongArrayAccessor arr(env, columnIndexes);
    jsize arr_len = arr.size();
    try {
        if (arr_len == 1) {
            if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, arr[0], type_Float)) {
                return;
            }
            Q(nativeQueryPtr)->between(S(arr[0]), static_cast<float>(value1), static_cast<float>(value2));
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
                                                                            jlongArray columnIndexes,
                                                                            jlongArray tablePointers, jdouble value)
{
    JLongArrayAccessor table_arr(env, tablePointers);
    JLongArrayAccessor index_arr(env, columnIndexes);
    jsize arr_len = index_arr.size();
    try {
        if (arr_len == 1) {
            if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, index_arr[0], type_Double)) {
                return;
            }
            Q(nativeQueryPtr)->equal(S(index_arr[0]), static_cast<double>(value));
        }
        else {
            TableRef table_ref = getTableForLinkQuery(nativeQueryPtr, table_arr, index_arr);
            Q(nativeQueryPtr)
                ->and_query(numeric_link_equal<Double, double, jdouble>(table_ref, index_arr[arr_len - 1], value));
        }
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL JNICALL Java_io_realm_internal_TableQuery_nativeNotEqual__J_3J_3JD(JNIEnv* env, jobject,
                                                                                       jlong nativeQueryPtr,
                                                                                       jlongArray columnIndexes,
                                                                                       jlongArray tablePointers,
                                                                                       jdouble value)
{
    JLongArrayAccessor table_arr(env, tablePointers);
    JLongArrayAccessor index_arr(env, columnIndexes);
    jsize arr_len = index_arr.size();
    try {
        if (arr_len == 1) {
            if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, index_arr[0], type_Double)) {
                return;
            }
            Q(nativeQueryPtr)->not_equal(S(index_arr[0]), static_cast<double>(value));
        }
        else {
            TableRef table_ref = getTableForLinkQuery(nativeQueryPtr, table_arr, index_arr);
            Q(nativeQueryPtr)
                ->and_query(numeric_link_notequal<Double, double, jdouble>(table_ref, index_arr[arr_len - 1], value));
        }
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeGreater__J_3J_3JD(JNIEnv* env, jobject,
                                                                              jlong nativeQueryPtr,
                                                                              jlongArray columnIndexes,
                                                                              jlongArray tablePointers, jdouble value)
{
    JLongArrayAccessor table_arr(env, tablePointers);
    JLongArrayAccessor index_arr(env, columnIndexes);
    jsize arr_len = index_arr.size();
    try {
        if (arr_len == 1) {
            if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, index_arr[0], type_Double)) {
                return;
            }
            Q(nativeQueryPtr)->greater(S(index_arr[0]), static_cast<double>(value));
        }
        else {
            TableRef table_ref = getTableForLinkQuery(nativeQueryPtr, table_arr, index_arr);
            Q(nativeQueryPtr)
                ->and_query(numeric_link_greater<Double, double, jdouble>(table_ref, index_arr[arr_len - 1], value));
        }
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeGreaterEqual__J_3J_3JD(JNIEnv* env, jobject,
                                                                                   jlong nativeQueryPtr,
                                                                                   jlongArray columnIndexes,
                                                                                   jlongArray tablePointers,
                                                                                   jdouble value)
{
    JLongArrayAccessor table_arr(env, tablePointers);
    JLongArrayAccessor index_arr(env, columnIndexes);
    jsize arr_len = index_arr.size();
    try {
        if (arr_len == 1) {
            if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, index_arr[0], type_Double)) {
                return;
            }
            Q(nativeQueryPtr)->greater_equal(S(index_arr[0]), static_cast<double>(value));
        }
        else {
            TableRef table_ref = getTableForLinkQuery(nativeQueryPtr, table_arr, index_arr);
            Q(nativeQueryPtr)
                ->and_query(numeric_link_greaterequal<Double, double, jdouble>(table_ref, index_arr[arr_len - 1], value));
        }
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeLess__J_3J_3JD(JNIEnv* env, jobject, jlong nativeQueryPtr,
                                                                           jlongArray columnIndexes,
                                                                           jlongArray tablePointers, jdouble value)
{
    JLongArrayAccessor table_arr(env, tablePointers);
    JLongArrayAccessor index_arr(env, columnIndexes);
    jsize arr_len = index_arr.size();
    try {
        if (arr_len == 1) {
            if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, index_arr[0], type_Double)) {
                return;
            }
            Q(nativeQueryPtr)->less(S(index_arr[0]), static_cast<double>(value));
        }
        else {
            TableRef table_ref = getTableForLinkQuery(nativeQueryPtr, table_arr, index_arr);
            Q(nativeQueryPtr)
                ->and_query(numeric_link_less<Double, double, jdouble>(table_ref, index_arr[arr_len - 1], value));
        }
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeLessEqual__J_3J_3JD(JNIEnv* env, jobject,
                                                                                jlong nativeQueryPtr,
                                                                                jlongArray columnIndexes,
                                                                                jlongArray tablePointers,
                                                                                jdouble value)
{
    JLongArrayAccessor table_arr(env, tablePointers);
    JLongArrayAccessor index_arr(env, columnIndexes);
    jsize arr_len = index_arr.size();
    try {
        if (arr_len == 1) {
            if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, index_arr[0], type_Double)) {
                return;
            }
            Q(nativeQueryPtr)->less_equal(S(index_arr[0]), static_cast<double>(value));
        }
        else {
            TableRef table_ref = getTableForLinkQuery(nativeQueryPtr, table_arr, index_arr);
            Q(nativeQueryPtr)
                ->and_query(numeric_link_lessequal<Double, double, jdouble>(table_ref, index_arr[arr_len - 1], value));
        }
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeBetween__J_3JDD(JNIEnv* env, jobject,
                                                                               jlong nativeQueryPtr,
                                                                               jlongArray columnIndexes,
                                                                               jdouble value1, jdouble value2)
{
    JLongArrayAccessor arr(env, columnIndexes);
    jsize arr_len = arr.size();
    try {
        if (arr_len == 1) {
            if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, arr[0], type_Double)) {
                return;
            }
            Q(nativeQueryPtr)->between(S(arr[0]), static_cast<double>(value1), static_cast<double>(value2));
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
                                                                              jlongArray columnIndexes,
                                                                              jlongArray tablePointers, jlong value)
{
    JLongArrayAccessor table_arr(env, tablePointers);
    JLongArrayAccessor index_arr(env, columnIndexes);
    jsize arr_len = index_arr.size();
    try {
        if (arr_len == 1) {
            if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, index_arr[0], type_Timestamp)) {
                return;
            }
            Q(nativeQueryPtr)->equal(S(index_arr[0]), from_milliseconds(value));
        }
        else {
            TableRef table_ref = getTableForLinkQuery(nativeQueryPtr, table_arr, index_arr);
            Q(nativeQueryPtr)
                ->and_query(numeric_link_equal<Timestamp, Timestamp, Timestamp>(table_ref, index_arr[arr_len - 1],
                                                                                from_milliseconds(value)));
        }
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL JNICALL Java_io_realm_internal_TableQuery_nativeNotEqualTimestamp(JNIEnv* env, jobject,
                                                                                         jlong nativeQueryPtr,
                                                                                         jlongArray columnIndexes,
                                                                                         jlongArray tablePointers,
                                                                                         jlong value)
{
    JLongArrayAccessor table_arr(env, tablePointers);
    JLongArrayAccessor index_arr(env, columnIndexes);
    jsize arr_len = index_arr.size();
    try {
        if (arr_len == 1) {
            if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, index_arr[0], type_Timestamp)) {
                return;
            }
            Q(nativeQueryPtr)->not_equal(S(index_arr[0]), from_milliseconds(value));
        }
        else {
            TableRef table_ref = getTableForLinkQuery(nativeQueryPtr, table_arr, index_arr);
            Q(nativeQueryPtr)
                ->and_query(numeric_link_notequal<Timestamp, Timestamp, Timestamp>(table_ref, index_arr[arr_len - 1],
                                                                                   from_milliseconds(value)));
        }
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeGreaterTimestamp(JNIEnv* env, jobject,
                                                                                jlong nativeQueryPtr,
                                                                                jlongArray columnIndexes,
                                                                                jlongArray tablePointers, jlong value)
{
    JLongArrayAccessor table_arr(env, tablePointers);
    JLongArrayAccessor index_arr(env, columnIndexes);
    jsize arr_len = index_arr.size();
    try {
        if (arr_len == 1) {
            if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, index_arr[0], type_Timestamp)) {
                return;
            }
            Q(nativeQueryPtr)->greater(S(index_arr[0]), from_milliseconds(value));
        }
        else {
            TableRef table_ref = getTableForLinkQuery(nativeQueryPtr, table_arr, index_arr);
            Q(nativeQueryPtr)
                ->and_query(numeric_link_greater<Timestamp, Timestamp, Timestamp>(table_ref, index_arr[arr_len - 1],
                                                                                  from_milliseconds(value)));
        }
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeGreaterEqualTimestamp(JNIEnv* env, jobject,
                                                                                     jlong nativeQueryPtr,
                                                                                     jlongArray columnIndexes,
                                                                                     jlongArray tablePointers,
                                                                                     jlong value)
{
    JLongArrayAccessor table_arr(env, tablePointers);
    JLongArrayAccessor index_arr(env, columnIndexes);
    jsize arr_len = index_arr.size();
    try {
        if (arr_len == 1) {
            if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, index_arr[0], type_Timestamp)) {
                return;
            }
            Q(nativeQueryPtr)->greater_equal(S(index_arr[0]), from_milliseconds(value));
        }
        else {
            TableRef table_ref = getTableForLinkQuery(nativeQueryPtr, table_arr, index_arr);
            Q(nativeQueryPtr)
                ->and_query(numeric_link_greaterequal<Timestamp, Timestamp, Timestamp>(table_ref, index_arr[arr_len - 1],
                                                                                       from_milliseconds(value)));
        }
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeLessTimestamp(JNIEnv* env, jobject,
                                                                             jlong nativeQueryPtr,
                                                                             jlongArray columnIndexes,
                                                                             jlongArray tablePointers, jlong value)
{
    JLongArrayAccessor table_arr(env, tablePointers);
    JLongArrayAccessor index_arr(env, columnIndexes);
    jsize arr_len = index_arr.size();
    try {
        if (arr_len == 1) {
            if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, index_arr[0], type_Timestamp)) {
                return;
            }
            Q(nativeQueryPtr)->less(S(index_arr[0]), from_milliseconds(value));
        }
        else {
            TableRef table_ref = getTableForLinkQuery(nativeQueryPtr, table_arr, index_arr);
            Q(nativeQueryPtr)
                ->and_query(numeric_link_less<Timestamp, Timestamp, Timestamp>(table_ref, index_arr[arr_len - 1],
                                                                               from_milliseconds(value)));
        }
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeLessEqualTimestamp(JNIEnv* env, jobject,
                                                                                  jlong nativeQueryPtr,
                                                                                  jlongArray columnIndexes,
                                                                                  jlongArray tablePointers,
                                                                                  jlong value)
{
    JLongArrayAccessor table_arr(env, tablePointers);
    JLongArrayAccessor index_arr(env, columnIndexes);
    jsize arr_len = index_arr.size();
    try {
        if (arr_len == 1) {
            if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, index_arr[0], type_Timestamp)) {
                return;
            }
            Q(nativeQueryPtr)->less_equal(S(index_arr[0]), from_milliseconds(value));
        }
        else {
            TableRef table_ref = getTableForLinkQuery(nativeQueryPtr, table_arr, index_arr);
            Q(nativeQueryPtr)
                ->and_query(numeric_link_lessequal<Timestamp, Timestamp, Timestamp>(table_ref, index_arr[arr_len - 1],
                                                                                    from_milliseconds(value)));
        }
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeBetweenTimestamp(JNIEnv* env, jobject,
                                                                                jlong nativeQueryPtr,
                                                                                jlongArray columnIndexes,
                                                                                jlong value1, jlong value2)
{
    JLongArrayAccessor arr(env, columnIndexes);
    jsize arr_len = arr.size();
    try {
        if (arr_len == 1) {
            if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, arr[0], type_Timestamp)) {
                return;
            }
            Q(nativeQueryPtr)
                ->greater_equal(S(arr[0]), from_milliseconds(value1))
                .less_equal(S(arr[0]), from_milliseconds(value2));
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
                                                                            jlongArray columnIndexes,
                                                                            jlongArray tablePointers, jboolean value)
{
    JLongArrayAccessor table_arr(env, tablePointers);
    JLongArrayAccessor index_arr(env, columnIndexes);
    jsize arr_len = index_arr.size();
    try {
        if (arr_len == 1) {
            if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, index_arr[0], type_Bool)) {
                return;
            }
            Q(nativeQueryPtr)->equal(S(index_arr[0]), to_bool(value));
        }
        else {
            TableRef table_ref = getTableForLinkQuery(nativeQueryPtr, table_arr, index_arr);
            Q(nativeQueryPtr)
                ->and_query(numeric_link_equal<Bool, bool, jboolean>(table_ref, index_arr[arr_len - 1], value));
        }
    }
    CATCH_STD()
}

// String

enum StringPredicate { StringEqual, StringNotEqual, StringContains, StringBeginsWith, StringEndsWith, StringLike };


static void TableQuery_StringPredicate(JNIEnv* env, jlong nativeQueryPtr, jlongArray columnIndexes,
                                       jlongArray tablePointers, jstring value,
                                       jboolean caseSensitive, StringPredicate predicate)
{
    JLongArrayAccessor table_arr(env, tablePointers);
    JLongArrayAccessor index_arr(env, columnIndexes);
    jsize arr_len = index_arr.size();
    try {
        TableRef table_ref = getTableForLinkQuery(nativeQueryPtr, table_arr, index_arr);
        if (value == NULL) {
            if (!TBL_AND_COL_NULLABLE(env, table_ref.get(), index_arr[arr_len - 1])) {
                return;
            }
        }
        bool is_case_sensitive = to_bool(caseSensitive);
        JStringAccessor value2(env, value); // throws
        if (arr_len == 1) {
            if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, index_arr[0], type_String)) {
                return;
            }
            switch (predicate) {
                case StringEqual:
                    Q(nativeQueryPtr)->equal(S(index_arr[0]), value2, is_case_sensitive);
                    break;
                case StringNotEqual:
                    Q(nativeQueryPtr)->not_equal(S(index_arr[0]), value2, is_case_sensitive);
                    break;
                case StringContains:
                    Q(nativeQueryPtr)->contains(S(index_arr[0]), value2, is_case_sensitive);
                    break;
                case StringBeginsWith:
                    Q(nativeQueryPtr)->begins_with(S(index_arr[0]), value2, is_case_sensitive);
                    break;
                case StringEndsWith:
                    Q(nativeQueryPtr)->ends_with(S(index_arr[0]), value2, is_case_sensitive);
                    break;
                case StringLike:
                    Q(nativeQueryPtr)->like(S(index_arr[0]), value2, is_case_sensitive);
                    break;
            }
        }
        else {
            switch (predicate) {
                case StringEqual:
                    Q(nativeQueryPtr)
                        ->and_query(table_ref->column<String>(size_t(index_arr[arr_len - 1]))
                                        .equal(StringData(value2), is_case_sensitive));
                    break;
                case StringNotEqual:
                    Q(nativeQueryPtr)
                        ->and_query(table_ref->column<String>(size_t(index_arr[arr_len - 1]))
                                        .not_equal(StringData(value2), is_case_sensitive));
                    break;
                case StringContains:
                    Q(nativeQueryPtr)
                        ->and_query(table_ref->column<String>(size_t(index_arr[arr_len - 1]))
                                        .contains(StringData(value2), is_case_sensitive));
                    break;
                case StringBeginsWith:
                    Q(nativeQueryPtr)
                        ->and_query(table_ref->column<String>(size_t(index_arr[arr_len - 1]))
                                        .begins_with(StringData(value2), is_case_sensitive));
                    break;
                case StringEndsWith:
                    Q(nativeQueryPtr)
                        ->and_query(table_ref->column<String>(size_t(index_arr[arr_len - 1]))
                                        .ends_with(StringData(value2), is_case_sensitive));
                    break;
                case StringLike:
                    Q(nativeQueryPtr)
                        ->and_query(table_ref->column<String>(size_t(index_arr[arr_len - 1]))
                                        .like(StringData(value2), is_case_sensitive));
                    break;
            }
        }
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeEqual__J_3J_3JLjava_lang_String_2Z(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlongArray columnIndexes,
    jlongArray tablePointers, jstring value, jboolean caseSensitive)
{
    TableQuery_StringPredicate(env, nativeQueryPtr, columnIndexes, tablePointers, value, caseSensitive, StringEqual);
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeNotEqual__J_3J_3JLjava_lang_String_2Z(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlongArray columnIndexes,
    jlongArray tablePointers, jstring value, jboolean caseSensitive)
{
    TableQuery_StringPredicate(env, nativeQueryPtr, columnIndexes, tablePointers, value, caseSensitive, StringNotEqual);
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeBeginsWith(JNIEnv* env, jobject, jlong nativeQueryPtr,
                                                                          jlongArray columnIndexes,
                                                                          jlongArray tablePointers, jstring value,
                                                                          jboolean caseSensitive)
{
    TableQuery_StringPredicate(env, nativeQueryPtr, columnIndexes, tablePointers, value, caseSensitive, StringBeginsWith);
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeEndsWith(JNIEnv* env, jobject, jlong nativeQueryPtr,
                                                                        jlongArray columnIndexes,
                                                                        jlongArray tablePointers, jstring value,
                                                                        jboolean caseSensitive)
{
    TableQuery_StringPredicate(env, nativeQueryPtr, columnIndexes, tablePointers, value, caseSensitive, StringEndsWith);
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeLike(JNIEnv* env, jobject, jlong nativeQueryPtr,
                                                                    jlongArray columnIndexes,
                                                                    jlongArray tablePointers, jstring value,
                                                                    jboolean caseSensitive)
{
    TableQuery_StringPredicate(env, nativeQueryPtr, columnIndexes, tablePointers, value, caseSensitive, StringLike);
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeContains(JNIEnv* env, jobject, jlong nativeQueryPtr,
                                                                        jlongArray columnIndexes,
                                                                        jlongArray tablePointers, jstring value,
                                                                        jboolean caseSensitive)
{
    TableQuery_StringPredicate(env, nativeQueryPtr, columnIndexes, tablePointers, value, caseSensitive, StringContains);
}

// Binary

enum BinaryPredicate { BinaryEqual, BinaryNotEqual };

static void TableQuery_BinaryPredicate(JNIEnv* env, jlong nativeQueryPtr, jlongArray columnIndexes,
                                       jlongArray tablePointers, jbyteArray value, BinaryPredicate predicate)
{
    try {
        JLongArrayAccessor table_arr(env, tablePointers);
        JLongArrayAccessor index_arr(env, columnIndexes);
        jsize arr_len = index_arr.size();
        TableRef table_ref = getTableForLinkQuery(nativeQueryPtr, table_arr, index_arr);

        if (value == NULL && !TBL_AND_COL_NULLABLE(env, table_ref.get(), index_arr[arr_len - 1])) {
            return;
        }

        JByteArrayAccessor jarray_accessor(env, value);
        if (arr_len == 1) {
            if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, index_arr[0], type_Binary)) {
                return;
            }
            switch (predicate) {
                case BinaryEqual:
                    Q(nativeQueryPtr)->equal(S(index_arr[0]), jarray_accessor.transform<BinaryData>());
                    break;
                case BinaryNotEqual:
                    Q(nativeQueryPtr)->not_equal(S(index_arr[0]), jarray_accessor.transform<BinaryData>());
                    break;
            }
        }
        else {
            switch (predicate) {
                case BinaryEqual:
                    Q(nativeQueryPtr)
                        ->and_query(table_ref->column<Binary>(size_t(index_arr[arr_len - 1])) ==
                                    jarray_accessor.transform<BinaryData>());
                    break;
                case BinaryNotEqual:
                    Q(nativeQueryPtr)
                        ->and_query(table_ref->column<Binary>(size_t(index_arr[arr_len - 1])) !=
                                    jarray_accessor.transform<BinaryData>());
                    break;
            }
        }
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeEqual__J_3J_3J_3B(JNIEnv* env, jobject,
                                                                              jlong nativeQueryPtr,
                                                                              jlongArray columnIndices,
                                                                              jlongArray tablePointers,
                                                                              jbyteArray value)
{
    TableQuery_BinaryPredicate(env, nativeQueryPtr, columnIndices, tablePointers, value, BinaryEqual);
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeNotEqual__J_3J_3J_3B(JNIEnv* env, jobject,
                                                                                 jlong nativeQueryPtr,
                                                                                 jlongArray columnIndices,
                                                                                 jlongArray tablePointers,
                                                                                 jbyteArray value)
{
    TableQuery_BinaryPredicate(env, nativeQueryPtr, columnIndices, tablePointers, value, BinaryNotEqual);
}

// General ----------------------------------------------------
// TODO:
// Some of these methods may not need the check for Table/Query validity,
// as they are called for each method when building up the query.
// Consider to reduce to just the "action" methods on Query

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeGroup(JNIEnv* env, jobject, jlong nativeQueryPtr)
{
    Query* pQuery = Q(nativeQueryPtr);
    if (!QUERY_VALID(env, pQuery)) {
        return;
    }
    try {
        pQuery->group();
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeEndGroup(JNIEnv* env, jobject, jlong nativeQueryPtr)
{
    Query* pQuery = Q(nativeQueryPtr);
    if (!QUERY_VALID(env, pQuery)) {
        return;
    }
    try {
        pQuery->end_group();
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeOr(JNIEnv* env, jobject, jlong nativeQueryPtr)
{
    // No verification of parameters needed?
    Query* pQuery = Q(nativeQueryPtr);
    if (!QUERY_VALID(env, pQuery)) {
        return;
    }
    try {
        pQuery->Or();
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeNot(JNIEnv* env, jobject, jlong nativeQueryPtr)
{
    Query* pQuery = Q(nativeQueryPtr);
    if (!QUERY_VALID(env, pQuery)) {
        return;
    }
    try {
        pQuery->Not();
    }
    CATCH_STD()
}

// Find --------------------------------------


JNIEXPORT jlong JNICALL Java_io_realm_internal_TableQuery_nativeFind(JNIEnv* env, jobject, jlong nativeQueryPtr,
                                                                     jlong fromTableRow)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = pQuery->get_table().get();
    if (!QUERY_VALID(env, pQuery)) {
        return -1;
    }
    // It's valid to go 1 past the end index
    if ((fromTableRow < 0) || (S(fromTableRow) > pTable->size())) {
        // below check will fail with appropriate exception
        (void)ROW_INDEX_VALID(env, pTable, fromTableRow);
        return -1;
    }

    try {
        size_t r = pQuery->find(S(fromTableRow));
        return (r == not_found) ? jlong(-1) : jlong(r);
    }
    CATCH_STD()
    return -1;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_TableQuery_nativeFindAll(JNIEnv* env, jobject, jlong nativeQueryPtr,
                                                                        jlong start, jlong end, jlong limit)
{
    TR_ENTER()
    Query* query = Q(nativeQueryPtr);
    TableRef table = query->get_table();
    if (!QUERY_VALID(env, query) || !ROW_INDEXES_VALID(env, table.get(), start, end, limit)) {
        return -1;
    }
    try {
        TableView* tableView = new TableView(query->find_all(S(start), S(end), S(limit)));
        return reinterpret_cast<jlong>(tableView);
    }
    CATCH_STD()
    return -1;
}

// Integer Aggregates

JNIEXPORT jlong JNICALL Java_io_realm_internal_TableQuery_nativeSumInt(JNIEnv* env, jobject, jlong nativeQueryPtr,
                                                                       jlong columnIndex, jlong start, jlong end,
                                                                       jlong limit)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = pQuery->get_table().get();
    if (!QUERY_VALID(env, pQuery) || !COL_INDEX_AND_TYPE_VALID(env, pTable, columnIndex, type_Int) ||
        !ROW_INDEXES_VALID(env, pTable, start, end, limit)) {
        return 0;
    }
    try {
        return pQuery->sum_int(S(columnIndex), NULL, S(start), S(end), S(limit));
    }
    CATCH_STD()
    return 0;
}

JNIEXPORT jobject JNICALL Java_io_realm_internal_TableQuery_nativeMaximumInt(JNIEnv* env, jobject,
                                                                             jlong nativeQueryPtr, jlong columnIndex,
                                                                             jlong start, jlong end, jlong limit)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = pQuery->get_table().get();
    if (!QUERY_VALID(env, pQuery) || !COL_INDEX_AND_TYPE_VALID(env, pTable, columnIndex, type_Int) ||
        !ROW_INDEXES_VALID(env, pTable, start, end, limit)) {
        return nullptr;
    }
    try {
        size_t return_ndx;
        int64_t result = pQuery->maximum_int(S(columnIndex), NULL, S(start), S(end), S(limit), &return_ndx);
        if (return_ndx != npos) {
            return JavaClassGlobalDef::new_long(env, result);
        }
    }
    CATCH_STD()
    return nullptr;
}

JNIEXPORT jobject JNICALL Java_io_realm_internal_TableQuery_nativeMinimumInt(JNIEnv* env, jobject,
                                                                             jlong nativeQueryPtr, jlong columnIndex,
                                                                             jlong start, jlong end, jlong limit)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = pQuery->get_table().get();
    if (!QUERY_VALID(env, pQuery) || !COL_INDEX_AND_TYPE_VALID(env, pTable, columnIndex, type_Int) ||
        !ROW_INDEXES_VALID(env, pTable, start, end, limit)) {
        return nullptr;
    }
    try {
        size_t return_ndx;
        int64_t result = pQuery->minimum_int(S(columnIndex), NULL, S(start), S(end), S(limit), &return_ndx);
        if (return_ndx != npos) {
            return JavaClassGlobalDef::new_long(env, result);
        }
    }
    CATCH_STD()
    return nullptr;
}

JNIEXPORT jdouble JNICALL Java_io_realm_internal_TableQuery_nativeAverageInt(JNIEnv* env, jobject,
                                                                             jlong nativeQueryPtr, jlong columnIndex,
                                                                             jlong start, jlong end, jlong limit)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = pQuery->get_table().get();
    if (!QUERY_VALID(env, pQuery) || !COL_INDEX_AND_TYPE_VALID(env, pTable, columnIndex, type_Int) ||
        !ROW_INDEXES_VALID(env, pTable, start, end, limit)) {
        return 0;
    }
    try {
        size_t resultcount;
        // TODO: return resultcount?
        double avg = pQuery->average_int(S(columnIndex), &resultcount, S(start), S(end), S(limit));
        // fprintf(stderr, "!!!Average(%d, %d) = %f (%d results)\n", start, end, avg, resultcount); fflush(stderr);
        return avg;
    }
    CATCH_STD()
    return 0;
}


// float Aggregates

JNIEXPORT jdouble JNICALL Java_io_realm_internal_TableQuery_nativeSumFloat(JNIEnv* env, jobject, jlong nativeQueryPtr,
                                                                           jlong columnIndex, jlong start, jlong end,
                                                                           jlong limit)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = pQuery->get_table().get();
    if (!QUERY_VALID(env, pQuery) || !COL_INDEX_AND_TYPE_VALID(env, pTable, columnIndex, type_Float) ||
        !ROW_INDEXES_VALID(env, pTable, start, end, limit)) {
        return 0;
    }
    try {
        return pQuery->sum_float(S(columnIndex), NULL, S(start), S(end), S(limit));
    }
    CATCH_STD()
    return 0;
}

JNIEXPORT jobject JNICALL Java_io_realm_internal_TableQuery_nativeMaximumFloat(JNIEnv* env, jobject,
                                                                               jlong nativeQueryPtr,
                                                                               jlong columnIndex, jlong start,
                                                                               jlong end, jlong limit)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = pQuery->get_table().get();
    if (!QUERY_VALID(env, pQuery) || !COL_INDEX_AND_TYPE_VALID(env, pTable, columnIndex, type_Float) ||
        !ROW_INDEXES_VALID(env, pTable, start, end, limit)) {
        return nullptr;
    }
    try {
        size_t return_ndx;
        float result = pQuery->maximum_float(S(columnIndex), NULL, S(start), S(end), S(limit), &return_ndx);
        if (return_ndx != npos) {
            return JavaClassGlobalDef::new_float(env, result);
        }
    }
    CATCH_STD()
    return nullptr;
}

JNIEXPORT jobject JNICALL Java_io_realm_internal_TableQuery_nativeMinimumFloat(JNIEnv* env, jobject,
                                                                               jlong nativeQueryPtr,
                                                                               jlong columnIndex, jlong start,
                                                                               jlong end, jlong limit)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = pQuery->get_table().get();
    if (!QUERY_VALID(env, pQuery) || !COL_INDEX_AND_TYPE_VALID(env, pTable, columnIndex, type_Float) ||
        !ROW_INDEXES_VALID(env, pTable, start, end, limit)) {
        return nullptr;
    }
    try {
        size_t return_ndx;
        float result = pQuery->minimum_float(S(columnIndex), NULL, S(start), S(end), S(limit), &return_ndx);
        if (return_ndx != npos) {
            return JavaClassGlobalDef::new_float(env, result);
        }
    }
    CATCH_STD()
    return nullptr;
}

JNIEXPORT jdouble JNICALL Java_io_realm_internal_TableQuery_nativeAverageFloat(JNIEnv* env, jobject,
                                                                               jlong nativeQueryPtr,
                                                                               jlong columnIndex, jlong start,
                                                                               jlong end, jlong limit)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = pQuery->get_table().get();
    if (!QUERY_VALID(env, pQuery) || !COL_INDEX_AND_TYPE_VALID(env, pTable, columnIndex, type_Float) ||
        !ROW_INDEXES_VALID(env, pTable, start, end, limit)) {
        return 0;
    }
    try {
        size_t resultcount;
        double avg = pQuery->average_float(S(columnIndex), &resultcount, S(start), S(end), S(limit));
        return avg;
    }
    CATCH_STD()
    return 0;
}

// double Aggregates

JNIEXPORT jdouble JNICALL Java_io_realm_internal_TableQuery_nativeSumDouble(JNIEnv* env, jobject,
                                                                            jlong nativeQueryPtr, jlong columnIndex,
                                                                            jlong start, jlong end, jlong limit)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = pQuery->get_table().get();
    if (!QUERY_VALID(env, pQuery) || !COL_INDEX_AND_TYPE_VALID(env, pTable, columnIndex, type_Double) ||
        !ROW_INDEXES_VALID(env, pTable, start, end, limit)) {
        return 0;
    }
    try {
        return pQuery->sum_double(S(columnIndex), NULL, S(start), S(end), S(limit));
    }
    CATCH_STD()
    return 0;
}

JNIEXPORT jobject JNICALL Java_io_realm_internal_TableQuery_nativeMaximumDouble(JNIEnv* env, jobject,
                                                                                jlong nativeQueryPtr,
                                                                                jlong columnIndex, jlong start,
                                                                                jlong end, jlong limit)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = pQuery->get_table().get();
    if (!QUERY_VALID(env, pQuery) || !COL_INDEX_AND_TYPE_VALID(env, pTable, columnIndex, type_Double) ||
        !ROW_INDEXES_VALID(env, pTable, start, end, limit)) {
        return nullptr;
    }
    try {
        size_t return_ndx;
        double result = pQuery->maximum_double(S(columnIndex), NULL, S(start), S(end), S(limit), &return_ndx);
        if (return_ndx != npos) {
            return JavaClassGlobalDef::new_double(env, result);
        }
    }
    CATCH_STD()
    return nullptr;
}

JNIEXPORT jobject JNICALL Java_io_realm_internal_TableQuery_nativeMinimumDouble(JNIEnv* env, jobject,
                                                                                jlong nativeQueryPtr,
                                                                                jlong columnIndex, jlong start,
                                                                                jlong end, jlong limit)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = pQuery->get_table().get();
    if (!QUERY_VALID(env, pQuery) || !COL_INDEX_AND_TYPE_VALID(env, pTable, columnIndex, type_Double) ||
        !ROW_INDEXES_VALID(env, pTable, start, end, limit)) {
        return nullptr;
    }
    try {
        size_t return_ndx;
        double result = pQuery->minimum_double(S(columnIndex), NULL, S(start), S(end), S(limit), &return_ndx);
        if (return_ndx != npos) {
            return JavaClassGlobalDef::new_double(env, result);
        }
    }
    CATCH_STD()
    return nullptr;
}

JNIEXPORT jdouble JNICALL Java_io_realm_internal_TableQuery_nativeAverageDouble(JNIEnv* env, jobject,
                                                                                jlong nativeQueryPtr,
                                                                                jlong columnIndex, jlong start,
                                                                                jlong end, jlong limit)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = pQuery->get_table().get();
    if (!QUERY_VALID(env, pQuery) || !COL_INDEX_AND_TYPE_VALID(env, pTable, columnIndex, type_Double) ||
        !ROW_INDEXES_VALID(env, pTable, start, end, limit)) {
        return 0;
    }
    try {
        // TODO: Return resultcount
        size_t resultcount;
        double avg = pQuery->average_double(S(columnIndex), &resultcount, S(start), S(end), S(limit));
        return avg;
    }
    CATCH_STD()
    return 0;
}


// date aggregates
// FIXME: This is a rough workaround while waiting for https://github.com/realm/realm-core/issues/1745 to be solved
JNIEXPORT jobject JNICALL Java_io_realm_internal_TableQuery_nativeMaximumTimestamp(JNIEnv* env, jobject,
                                                                                   jlong nativeQueryPtr,
                                                                                   jlong columnIndex, jlong start,
                                                                                   jlong end, jlong limit)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = pQuery->get_table().get();
    if (!QUERY_VALID(env, pQuery) || !COL_INDEX_AND_TYPE_VALID(env, pTable, columnIndex, type_Timestamp) ||
        !ROW_INDEXES_VALID(env, pTable, start, end, limit)) {
        return nullptr;
    }
    try {
        size_t return_ndx;
        Timestamp result = pQuery->find_all().maximum_timestamp(S(columnIndex), &return_ndx);
        if (return_ndx != npos && !result.is_null()) {
            return JavaClassGlobalDef::new_long(env, to_milliseconds(result));
        }
    }
    CATCH_STD()
    return nullptr;
}

JNIEXPORT jobject JNICALL Java_io_realm_internal_TableQuery_nativeMinimumTimestamp(JNIEnv* env, jobject,
                                                                                   jlong nativeQueryPtr,
                                                                                   jlong columnIndex, jlong start,
                                                                                   jlong end, jlong limit)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = pQuery->get_table().get();
    if (!QUERY_VALID(env, pQuery) || !COL_INDEX_AND_TYPE_VALID(env, pTable, columnIndex, type_Timestamp) ||
        !ROW_INDEXES_VALID(env, pTable, start, end, limit)) {
        return nullptr;
    }
    try {
        size_t return_ndx;
        Timestamp result = pQuery->find_all().minimum_timestamp(S(columnIndex), &return_ndx);
        if (return_ndx != npos && !result.is_null()) {
            return JavaClassGlobalDef::new_long(env, to_milliseconds(result));
        }
    }
    CATCH_STD()
    return nullptr;
}

// Count, Remove

JNIEXPORT jlong JNICALL Java_io_realm_internal_TableQuery_nativeCount(JNIEnv* env, jobject, jlong nativeQueryPtr,
                                                                      jlong start, jlong end, jlong limit)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = pQuery->get_table().get();
    if (!QUERY_VALID(env, pQuery) || !ROW_INDEXES_VALID(env, pTable, start, end, limit)) {
        return 0;
    }
    try {
        return static_cast<jlong>(pQuery->count(S(start), S(end), S(limit)));
    }
    CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_TableQuery_nativeRemove(JNIEnv* env, jobject, jlong nativeQueryPtr)
{
    Query* pQuery = Q(nativeQueryPtr);
    if (!QUERY_VALID(env, pQuery)) {
        return 0;
    }
    try {
        return static_cast<jlong>(pQuery->remove());
    }
    CATCH_STD()
    return 0;
}

// isNull and isNotNull

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeIsNull(JNIEnv* env, jobject, jlong nativeQueryPtr,
                                                                      jlongArray columnIndexes,
                                                                      jlongArray tablePointers)
{
    try {
        JLongArrayAccessor table_arr(env, tablePointers);
        JLongArrayAccessor index_arr(env, columnIndexes);
        jsize arr_len = index_arr.size();
        auto pQuery = reinterpret_cast<Query *>(nativeQueryPtr);

        jlong column_idx = index_arr[arr_len - 1];

        TableRef table_ref = getTableByArray(nativeQueryPtr, table_arr, index_arr);
        if (!isNullable(env, reinterpret_cast<Table *>(table_arr[arr_len - 1]), table_ref, column_idx)) {
            return;
        }

        TableRef src_table_ref = getTableForLinkQuery(nativeQueryPtr, table_arr, index_arr);
        DataType col_type = table_ref->get_column_type(S(column_idx));
        if (arr_len == 1) {
            switch (col_type) {
                case type_Link:
                    pQuery->and_query(src_table_ref->column<Link>(S(column_idx)).is_null());
                    break;
                case type_LinkList:
                    // Cannot get here. Exception will be thrown in TBL_AND_COL_NULLABLE
                    ThrowException(env, FatalError, "This is not reachable.");
                    break;
                case type_Binary:
                    pQuery->equal(S(column_idx), BinaryData());
                    break;
                case type_String:
                case type_Bool:
                case type_Int:
                case type_Float:
                case type_Double:
                case type_Timestamp:
                    Q(nativeQueryPtr)->equal(S(column_idx), realm::null());
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
                    pQuery->and_query(src_table_ref->column<String>(S(column_idx)) == realm::null());
                    break;
                case type_Binary:
                    pQuery->and_query(src_table_ref->column<Binary>(S(column_idx)) == BinaryData());
                    break;
                case type_Bool:
                    pQuery->and_query(src_table_ref->column<Bool>(S(column_idx)) == realm::null());
                    break;
                case type_Int:
                    pQuery->and_query(src_table_ref->column<Int>(S(column_idx)) == realm::null());
                    break;
                case type_Float:
                    pQuery->and_query(src_table_ref->column<Float>(S(column_idx)) == realm::null());
                    break;
                case type_Double:
                    pQuery->and_query(src_table_ref->column<Double>(S(column_idx)) == realm::null());
                    break;
                case type_Timestamp:
                    pQuery->and_query(src_table_ref->column<Timestamp>(S(column_idx)) == realm::null());
                    break;
                default:
                    REALM_UNREACHABLE();
            }
        }
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeIsNotNull(JNIEnv* env, jobject, jlong nativeQueryPtr,
                                                                         jlongArray columnIndexes,
                                                                         jlongArray tablePointers)
{
    JLongArrayAccessor table_arr(env, tablePointers);
    JLongArrayAccessor index_arr(env, columnIndexes);
    jsize arr_len = index_arr.size();
    Query* pQuery = Q(nativeQueryPtr);
    try {
        jlong column_idx = index_arr[arr_len - 1];

        TableRef table_ref = getTableByArray(nativeQueryPtr, table_arr, index_arr);
        if (!isNullable(env, TBL(table_arr[arr_len - 1]), table_ref, column_idx)) {
            return;
        }

        TableRef src_table_ref = getTableForLinkQuery(nativeQueryPtr, table_arr, index_arr);

        DataType col_type = table_ref->get_column_type(S(column_idx));
        if (arr_len == 1) {
            switch (col_type) {
                case type_Link:
                    pQuery->and_query(src_table_ref->column<Link>(S(column_idx)).is_not_null());
                    break;
                case type_LinkList:
                    // Cannot get here. Exception will be thrown in TBL_AND_COL_NULLABLE
                    ThrowException(env, FatalError, "This is not reachable.");
                    break;
                case type_Binary:
                    pQuery->not_equal(S(column_idx), realm::BinaryData());
                    break;
                case type_String:
                case type_Bool:
                case type_Int:
                case type_Float:
                case type_Double:
                case type_Timestamp:
                    pQuery->not_equal(S(column_idx), realm::null());
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
                    pQuery->and_query(src_table_ref->column<String>(S(column_idx)) != realm::null());
                    break;
                case type_Binary:
                    pQuery->and_query(src_table_ref->column<Binary>(S(column_idx)) != realm::BinaryData());
                    break;
                case type_Bool:
                    pQuery->and_query(src_table_ref->column<Bool>(S(column_idx)) != realm::null());
                    break;
                case type_Int:
                    pQuery->and_query(src_table_ref->column<Int>(S(column_idx)) != realm::null());
                    break;
                case type_Float:
                    pQuery->and_query(src_table_ref->column<Float>(S(column_idx)) != realm::null());
                    break;
                case type_Double:
                    pQuery->and_query(src_table_ref->column<Double>(S(column_idx)) != realm::null());
                    break;
                case type_Timestamp:
                    pQuery->and_query(src_table_ref->column<Timestamp>(S(column_idx)) != realm::null());
                    break;
                default:
                    REALM_UNREACHABLE();
            }
        }
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeIsEmpty(JNIEnv* env, jobject, jlong nativeQueryPtr,
                                                                       jlongArray columnIndexes,
                                                                       jlongArray tablePointers)
{
    JLongArrayAccessor table_arr(env, tablePointers);
    JLongArrayAccessor index_arr(env, columnIndexes);
    jsize arr_len = index_arr.size();
    Query* pQuery = reinterpret_cast<Query *>(nativeQueryPtr);
    try {
        TableRef src_table_ref = getTableForLinkQuery(nativeQueryPtr, table_arr, index_arr);
        auto column_idx = static_cast<size_t>(index_arr[arr_len - 1]);

        // Support a backlink as the last column in a field descriptor
        auto last = reinterpret_cast<Table*>(table_arr[arr_len-1]);
        if (last != nullptr) {
            pQuery->and_query(src_table_ref->column<BackLink>(*last, column_idx).count() == 0);
            return;
        }

        TableRef table_ref = getTableByArray(nativeQueryPtr, table_arr, index_arr);
        DataType col_type = table_ref->get_column_type(column_idx);
        if (arr_len == 1) {
            // Field queries
            switch (col_type) {
                case type_Binary:
                    pQuery->equal(column_idx, BinaryData("", 0));
                    break;
                case type_LinkList:
                    pQuery->and_query(src_table_ref->column<LinkList>(column_idx).count() == 0);
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
                    pQuery->and_query(src_table_ref->column<Binary>(column_idx) == BinaryData("", 0));
                    break;
                case type_LinkList:
                    pQuery->and_query(src_table_ref->column<LinkList>(column_idx).count() == 0);
                    break;
                case type_String:
                    pQuery->and_query(src_table_ref->column<String>(column_idx) == "");
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
                                                   jlongArray columnIndexes, jlongArray tablePointers) {
    JLongArrayAccessor table_arr(env, tablePointers);
    JLongArrayAccessor index_arr(env, columnIndexes);
    jsize arr_len = index_arr.size();
    Query* pQuery = reinterpret_cast<Query *>(nativeQueryPtr);
    try {
        TableRef src_table_ref = getTableForLinkQuery(nativeQueryPtr, table_arr, index_arr);
        auto column_idx = static_cast<size_t>(index_arr[arr_len - 1]);

        // Support a backlink as the last column in a field descriptor
        auto last = reinterpret_cast<Table*>(table_arr[arr_len-1]);
        if (last != nullptr) {
            pQuery->and_query(src_table_ref->column<BackLink>(*last, column_idx).count() != 0);
            return;
        }

        TableRef table_ref = getTableByArray(nativeQueryPtr, table_arr, index_arr);
        DataType col_type = table_ref->get_column_type(column_idx);
        if (arr_len == 1) {
            // Field queries
            switch (col_type) {
                case type_Binary:
                    pQuery->not_equal(column_idx, BinaryData("", 0));
                    break;
                case type_LinkList:
                    pQuery->and_query(src_table_ref->column<LinkList>(column_idx).count() != 0);
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
                    pQuery->and_query(src_table_ref->column<Binary>(column_idx) != BinaryData("", 0));
                    break;
                case type_LinkList:
                    pQuery->and_query(src_table_ref->column<LinkList>(column_idx).count() != 0);
                    break;
                case type_String:
                    pQuery->and_query(src_table_ref->column<String>(column_idx) != "");
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
    TR_ENTER_PTR(nativeQueryPtr);
    try {
        Query* query = reinterpret_cast<Query *>(nativeQueryPtr);
        query->and_query(std::unique_ptr<Expression>(new FalseExpression));
    }
    CATCH_STD()

}

JNIEXPORT void JNICALL
Java_io_realm_internal_TableQuery_nativeAlwaysTrue(JNIEnv *env, jobject, jlong nativeQueryPtr) {
    TR_ENTER_PTR(nativeQueryPtr);
    try {
        Query* query = reinterpret_cast<Query *>(nativeQueryPtr);
        query->and_query(std::unique_ptr<Expression>(new TrueExpression));
    }
    CATCH_STD()
}

static void finalize_table_query(jlong ptr)
{
    TR_ENTER_PTR(ptr)
    delete Q(ptr);
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_TableQuery_nativeGetFinalizerPtr(JNIEnv*, jclass)
{
    TR_ENTER()
    return reinterpret_cast<jlong>(&finalize_table_query);
}
