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

#include <realm.hpp>
#include <realm/group_shared.hpp>
#include <realm/commit_log.hpp>
#include "util.hpp"
#include "io_realm_internal_TableQuery.h"
#include "tablequery.hpp"

using namespace realm;

#if 1
#define QUERY_COL_TYPE_VALID(env, jPtr, col, type)  query_col_type_valid(env, jPtr, col, type)
#define QUERY_VALID(env, pQuery)                    query_valid(env, pQuery)
#else
#define QUERY_COL_TYPE_VALID(env, jPtr, col, type)  (true)
#define QUERY_VALID(env, pQuery)                    (true)
#endif

inline bool query_valid(JNIEnv* env, Query* pQuery)
{
    return TABLE_VALID(env, pQuery->get_table().get());
}

inline bool query_col_type_valid(JNIEnv* env, jlong nativeQueryPtr, jlong colIndex, DataType type)
{
    return TBL_AND_COL_INDEX_AND_TYPE_VALID(env, TQ(nativeQueryPtr)->get_current_table().get(), colIndex, type);
}


const char* ERR_IMPORT_CLOSED_REALM = "Can not import results from a closed Realm";
const char* ERR_SORT_NOT_SUPPORTED = "Sort is not supported on binary data, object references and RealmList";
//-------------------------------------------------------

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeClose(JNIEnv *, jclass, jlong nativeQueryPtr) {
    TR_ENTER_PTR(nativeQueryPtr)
    delete Q(nativeQueryPtr);
}

JNIEXPORT jstring JNICALL Java_io_realm_internal_TableQuery_nativeValidateQuery
(JNIEnv *env, jobject, jlong nativeQueryPtr)
{
    try {
        return to_jstring(env, Q(nativeQueryPtr)->validate());
    } CATCH_STD();
    return NULL;
}


// helper functions

// Return TableRef used for build link queries
static TableRef getTableForLinkQuery(jlong nativeQueryPtr, JniLongArray& indicesArray) {
    TableRef table_ref = Q(nativeQueryPtr)->get_table();
    jsize link_element_count = indicesArray.len() - 1;
    for (int i = 0; i < link_element_count; i++) {
        table_ref->link(size_t(indicesArray[i]));
    }
    return table_ref;
}

// Return TableRef point to original table or the link table
static TableRef getTableByArray(jlong nativeQueryPtr, JniLongArray& indicesArray) {
    TableRef table_ref = Q(nativeQueryPtr)->get_table();
    jsize link_element_count = indicesArray.len() - 1;
    for (int i = 0; i < link_element_count; i++) {
        table_ref = table_ref->get_link_target(size_t(indicesArray[i]));
    }
    return table_ref;
}

static jlong findAllWithHandover(JNIEnv* env, jlong bgSharedGroupPtr, std::unique_ptr<Query> query, jlong start, jlong end, jlong limit)
{
    TR_ENTER()
    TableRef table = query.get()->get_table();
    if (!QUERY_VALID(env, query.get()) ||
        !ROW_INDEXES_VALID(env, table.get(), start, end, limit)) {
        return 0;
    }
    // run the query
    TableView tableView(query->find_all(S(start), S(end), S(limit)));

    // handover the result
    std::unique_ptr<SharedGroup::Handover<TableView>> handover = SG(
            bgSharedGroupPtr)->export_for_handover(tableView, MutableSourcePayload::Move);
    return reinterpret_cast<jlong>(handover.release());
}

static jlong getDistinctViewWithHandover
        (JNIEnv *env, jlong bgSharedGroupPtr, std::unique_ptr<Query> query, jlong columnIndex)
{
        TableRef table = query->get_table();
        if (!QUERY_VALID(env, query.get()) ||
            !TBL_AND_COL_INDEX_VALID(env, table.get(), columnIndex)) {
            return 0;
        }
        switch (table->get_column_type(S(columnIndex))) {
            case type_Bool:
            case type_Int:
            case type_DateTime:
            case type_String: {
                TableView tableView(table->get_distinct_view(S(columnIndex)) );

                // handover the result
                std::unique_ptr<SharedGroup::Handover<TableView>> handover = SG(
                        bgSharedGroupPtr)->export_for_handover(tableView, MutableSourcePayload::Move);
                return reinterpret_cast<jlong>(handover.release());
            }
            default:
                ThrowException(env, IllegalArgument, "Invalid type - Only String, Date, boolean, short, int, long and their boxed variants are supported.");
                return 0;
        }
    return 0;
}

static jlong findAllSortedWithHandover
        (JNIEnv *env, jlong bgSharedGroupPtr, std::unique_ptr<Query> query, jlong start, jlong end, jlong limit, jlong columnIndex, jboolean ascending)
{
        TableRef table =  query->get_table();

        if (!(QUERY_VALID(env, query.get()) && ROW_INDEXES_VALID(env, table.get(), start, end, limit))) {
            return 0;
        }

        // run the query
        TableView tableView( query->find_all(S(start), S(end), S(limit)) );

        // sorting the results
        if (!COL_INDEX_VALID(env, &tableView, columnIndex)) {
            return 0;
        }

        int colType = tableView.get_column_type( S(columnIndex) );
        switch (colType) {
            case type_Bool:
            case type_Int:
            case type_DateTime:
            case type_Float:
            case type_Double:
            case type_String:
                tableView.sort( S(columnIndex), ascending != 0);
                break;
            default:
                ThrowException(env, IllegalArgument, ERR_SORT_NOT_SUPPORTED);
                return 0;
        }

        // handover the result
        std::unique_ptr<SharedGroup::Handover<TableView> > handover = SG(bgSharedGroupPtr)->export_for_handover(tableView, MutableSourcePayload::Move);
        return reinterpret_cast<jlong>(handover.release());
}

static jlong findAllMultiSortedWithHandover
        (JNIEnv *env, jlong bgSharedGroupPtr, std::unique_ptr<Query> query, jlong start, jlong end, jlong limit, jlongArray columnIndices, jbooleanArray ascending)
{
        JniLongArray long_arr(env, columnIndices);
        JniBooleanArray bool_arr(env, ascending);
        jsize arr_len = long_arr.len();
        jsize asc_len = bool_arr.len();

        if (arr_len == 0) {
            ThrowException(env, IllegalArgument, "You must provide at least one field name.");
            return 0;
        }
        if (asc_len == 0) {
            ThrowException(env, IllegalArgument, "You must provide at least one sort order.");
            return 0;
        }
        if (arr_len != asc_len) {
            ThrowException(env, IllegalArgument, "Number of fields and sort orders do not match.");
            return 0;
        }

        TableRef table = query->get_table();

        if (!QUERY_VALID(env, query.get()) || !ROW_INDEXES_VALID(env, table.get(), start, end, limit)) {
            return 0;
        }

        // run the query
        TableView tableView( query->find_all(S(start), S(end), S(limit)) );

        // sorting the results
        std::vector<size_t> indices;
        std::vector<bool> ascendings;

        for (int i = 0; i < arr_len; ++i) {
            if (!COL_INDEX_VALID(env, &tableView, long_arr[i])) {
                return -1;
            }
            int colType = tableView.get_column_type( S(long_arr[i]) );
            switch (colType) {
                case type_Bool:
                case type_Int:
                case type_DateTime:
                case type_Float:
                case type_Double:
                case type_String:
                    indices.push_back( S(long_arr[i]) );
                    ascendings.push_back( B(bool_arr[i]) );
                    break;
                default:
                    ThrowException(env, IllegalArgument, ERR_SORT_NOT_SUPPORTED);
                    return 0;
            }
        }

        tableView.sort(indices, ascendings);

        // handover the result
        std::unique_ptr<SharedGroup::Handover<TableView> > handover = SG(bgSharedGroupPtr)->export_for_handover(tableView, MutableSourcePayload::Move);
        return reinterpret_cast<jlong>(handover.release());
}


template <typename coretype, typename cpptype, typename javatype>
Query numeric_link_equal(TableRef tbl, jlong columnIndex, javatype value) {
    return tbl->column<coretype>(size_t(columnIndex)) == cpptype(value);
}

template <typename coretype, typename cpptype, typename javatype>
Query numeric_link_notequal(TableRef tbl, jlong columnIndex, javatype value) {
    return tbl->column<coretype>(size_t(columnIndex)) != cpptype(value);
}

template <typename coretype, typename cpptype, typename javatype>
Query numeric_link_greater(TableRef tbl, jlong columnIndex, javatype value) {
    return tbl->column<coretype>(size_t(columnIndex)) > cpptype(value);
}

template <typename coretype, typename cpptype, typename javatype>
Query numeric_link_greaterequal(TableRef tbl, jlong columnIndex, javatype value) {
    return tbl->column<coretype>(size_t(columnIndex)) >= cpptype(value);
}

template <typename coretype, typename cpptype, typename javatype>
Query numeric_link_less(TableRef tbl, jlong columnIndex, javatype value) {
    return tbl->column<coretype>(size_t(columnIndex)) < cpptype(value);
}

template <typename coretype, typename cpptype, typename javatype>
Query numeric_link_lessequal(TableRef tbl, jlong columnIndex, javatype value) {
    return tbl->column<coretype>(size_t(columnIndex)) <= cpptype(value);
}


// Integer

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeEqual__J_3JJ(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlongArray columnIndexes, jlong value)
{
    JniLongArray arr(env, columnIndexes);
    jsize arr_len = arr.len();
    try {
        if (arr_len == 1) {
            if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, arr[0], type_Int)) {
                return;
            }
            Q(nativeQueryPtr)->equal(S(arr[0]), static_cast<int64_t>(value));
        }
        else {
            TableRef table_ref = getTableForLinkQuery(nativeQueryPtr, arr);
            Q(nativeQueryPtr)->and_query(numeric_link_equal<Int, int64_t, jlong>(table_ref, arr[arr_len-1], value));
        }
    } CATCH_STD()
}

JNIEXPORT void JNICALL JNICALL Java_io_realm_internal_TableQuery_nativeNotEqual__J_3JJ(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlongArray columnIndexes, jlong value)
{
    JniLongArray arr(env, columnIndexes);
    jsize arr_len = arr.len();
    try {
        if (arr_len == 1) {
            if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, arr[0], type_Int)) {
                return;
            }
            Q(nativeQueryPtr)->not_equal(S(arr[0]), static_cast<int64_t>(value));
        }
        else {
            TableRef table_ref = getTableForLinkQuery(nativeQueryPtr, arr);
            Q(nativeQueryPtr)->and_query(numeric_link_notequal<Int, int64_t, jlong>(table_ref, arr[arr_len-1], value));
        }
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeGreater__J_3JJ(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlongArray columnIndexes, jlong value)
{
    JniLongArray arr(env, columnIndexes);
    jsize arr_len = arr.len();
    try {
        if (arr_len == 1) {
            if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, arr[0], type_Int)) {
                return;
            }
            Q(nativeQueryPtr)->greater(S(arr[0]), static_cast<int64_t>(value));
        }
        else {
            TableRef table_ref = getTableForLinkQuery(nativeQueryPtr, arr);
            Q(nativeQueryPtr)->and_query(numeric_link_greater<Int, int64_t, jlong>(table_ref, arr[arr_len-1], value));
        }
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeGreaterEqual__J_3JJ(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlongArray columnIndexes, jlong value)
{
    JniLongArray arr(env, columnIndexes);
    jsize arr_len = arr.len();
    try {
        if (arr_len == 1) {
            if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, arr[0], type_Int)) {
                return;
            }
            Q(nativeQueryPtr)->greater_equal(S(arr[0]), static_cast<int64_t>(value));
        }
        else {
            TableRef table_ref = getTableForLinkQuery(nativeQueryPtr, arr);
            Q(nativeQueryPtr)->and_query(numeric_link_greaterequal<Int, int64_t, jlong>(table_ref, arr[arr_len-1], value));
        }
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeLess__J_3JJ(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlongArray columnIndexes, jlong value)
{
    JniLongArray arr(env, columnIndexes);
    jsize arr_len = arr.len();
    try {
        if (arr_len == 1) {
            if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, arr[0], type_Int)) {
                return;
            }
            Q(nativeQueryPtr)->less(S(arr[0]), static_cast<int64_t>(value));
        }
        else {
            TableRef table_ref = getTableForLinkQuery(nativeQueryPtr, arr);
            Q(nativeQueryPtr)->and_query(numeric_link_less<Int, int64_t, jlong>(table_ref, arr[arr_len-1], value));
        }
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeLessEqual__J_3JJ(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlongArray columnIndexes, jlong value)
{
    JniLongArray arr(env, columnIndexes);
    jsize arr_len = arr.len();
    try {
        if (arr_len == 1) {
            if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, arr[0], type_Int)) {
                return;
            }
            Q(nativeQueryPtr)->less_equal(S(arr[0]), static_cast<int64_t>(value));
        }
        else {
            TableRef table_ref = getTableForLinkQuery(nativeQueryPtr, arr);
            Q(nativeQueryPtr)->and_query(numeric_link_lessequal<Int, int64_t, jlong>(table_ref, arr[arr_len-1], value));
        }
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeBetween__J_3JJJ(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlongArray columnIndexes, jlong value1, jlong value2)
{
    JniLongArray arr(env, columnIndexes);
    jsize arr_len = arr.len();
    if (arr_len == 1) {
        if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, arr[0], type_Int)) {
            return;
        }
        try {
            Q(nativeQueryPtr)->between(S(arr[0]), static_cast<int64_t>(value1), static_cast<int64_t>(value2));
        } CATCH_STD()
    }
    else {
        ThrowException(env, IllegalArgument, "between() does not support queries using child object fields.");
    }
}

// Float

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeEqual__J_3JF(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlongArray columnIndexes, jfloat value)
{
    JniLongArray arr(env, columnIndexes);
    jsize arr_len = arr.len();
    try {
        if (arr_len == 1) {
            if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, arr[0], type_Float)) {
                return;
            }
            Q(nativeQueryPtr)->equal(S(arr[0]), static_cast<float>(value));
        }
        else {
            TableRef table_ref = getTableForLinkQuery(nativeQueryPtr, arr);
            Q(nativeQueryPtr)->and_query(numeric_link_equal<Float, float, jfloat>(table_ref, arr[arr_len-1], value));
        }
    } CATCH_STD()
}

JNIEXPORT void JNICALL JNICALL Java_io_realm_internal_TableQuery_nativeNotEqual__J_3JF(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlongArray columnIndexes, jfloat value)
{
    JniLongArray arr(env, columnIndexes);
    jsize arr_len = arr.len();
    try {
        if (arr_len == 1) {
            if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, arr[0], type_Float)) {
                return;
            }
            Q(nativeQueryPtr)->not_equal(S(arr[0]), static_cast<float>(value));
        }
        else {
            TableRef table_ref = getTableForLinkQuery(nativeQueryPtr, arr);
            Q(nativeQueryPtr)->and_query(numeric_link_notequal<Float, float, jfloat>(table_ref, arr[arr_len-1], value));
        }
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeGreater__J_3JF(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlongArray columnIndexes, jfloat value)
{
    JniLongArray arr(env, columnIndexes);
    jsize arr_len = arr.len();
    try {
        if (arr_len == 1) {
            if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, arr[0], type_Float)) {
                return;
            }
            Q(nativeQueryPtr)->greater(S(arr[0]), static_cast<float>(value));
        }
        else {
            TableRef table_ref = getTableForLinkQuery(nativeQueryPtr, arr);
            Q(nativeQueryPtr)->and_query(numeric_link_greater<Float, float, jfloat>(table_ref, arr[arr_len-1], value));
        }
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeGreaterEqual__J_3JF(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlongArray columnIndexes, jfloat value)
{
    JniLongArray arr(env, columnIndexes);
    jsize arr_len = arr.len();
    try {
        if (arr_len == 1) {
            if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, arr[0], type_Float)) {
                return;
            }
            Q(nativeQueryPtr)->greater_equal(S(arr[0]), static_cast<float>(value));
        }
        else {
            TableRef table_ref = getTableForLinkQuery(nativeQueryPtr, arr);
            Q(nativeQueryPtr)->and_query(numeric_link_greaterequal<Float, float, jfloat>(table_ref, arr[arr_len-1], value));
        }
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeLess__J_3JF(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlongArray columnIndexes, jfloat value)
{
    JniLongArray arr(env, columnIndexes);
    jsize arr_len = arr.len();
    try {
        if (arr_len == 1) {
            if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, arr[0], type_Float)) {
                return;
            }
            Q(nativeQueryPtr)->less(S(arr[0]), static_cast<float>(value));
        }
        else {
            TableRef table_ref = getTableForLinkQuery(nativeQueryPtr, arr);
            Q(nativeQueryPtr)->and_query(numeric_link_less<Float, float, jfloat>(table_ref, arr[arr_len-1], value));
        }
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeLessEqual__J_3JF(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlongArray columnIndexes, jfloat value)
{
    JniLongArray arr(env, columnIndexes);
    jsize arr_len = arr.len();
    try {
        if (arr_len == 1) {
            if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, arr[0], type_Float)) {
                return;
            }
            Q(nativeQueryPtr)->less_equal(S(arr[0]), static_cast<float>(value));
        }
        else {
            TableRef table_ref = getTableForLinkQuery(nativeQueryPtr, arr);
            Q(nativeQueryPtr)->and_query(numeric_link_lessequal<Float, float, jfloat>(table_ref, arr[arr_len-1], value));
        }
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeBetween__J_3JFF(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlongArray columnIndexes, jfloat value1, jfloat value2)
{
    JniLongArray arr(env, columnIndexes);
    jsize arr_len = arr.len();
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
    } CATCH_STD()
}


// Double

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeEqual__J_3JD(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlongArray columnIndexes, jdouble value)
{
    JniLongArray arr(env, columnIndexes);
    jsize arr_len = arr.len();
    try {
        if (arr_len == 1) {
            if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, arr[0], type_Double)) {
                return;
            }
            Q(nativeQueryPtr)->equal(S(arr[0]), static_cast<double>(value));
        }
        else {
            TableRef table_ref = getTableForLinkQuery(nativeQueryPtr, arr);
            Q(nativeQueryPtr)->and_query(numeric_link_equal<Double, double, jdouble>(table_ref, arr[arr_len-1], value));
        }
    } CATCH_STD()
}

JNIEXPORT void JNICALL JNICALL Java_io_realm_internal_TableQuery_nativeNotEqual__J_3JD(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlongArray columnIndexes, jdouble value)
{
    JniLongArray arr(env, columnIndexes);
    jsize arr_len = arr.len();
    try {
        if (arr_len == 1) {
            if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, arr[0], type_Double)) {
                return;
            }
            Q(nativeQueryPtr)->not_equal(S(arr[0]), static_cast<double>(value));
        }
        else {
            TableRef table_ref = getTableForLinkQuery(nativeQueryPtr, arr);
            Q(nativeQueryPtr)->and_query(numeric_link_notequal<Double, double, jdouble>(table_ref, arr[arr_len-1], value));
        }
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeGreater__J_3JD(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlongArray columnIndexes, jdouble value)
{
    JniLongArray arr(env, columnIndexes);
    jsize arr_len = arr.len();
    try {
        if (arr_len == 1) {
            if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, arr[0], type_Double)) {
                return;
            }
            Q(nativeQueryPtr)->greater(S(arr[0]), static_cast<double>(value));
        }
        else {
            TableRef table_ref = getTableForLinkQuery(nativeQueryPtr, arr);
            Q(nativeQueryPtr)->and_query(numeric_link_greater<Double, double, jdouble>(table_ref, arr[arr_len-1], value));
        }
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeGreaterEqual__J_3JD(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlongArray columnIndexes, jdouble value)
{
    JniLongArray arr(env, columnIndexes);
    jsize arr_len = arr.len();
    try {
        if (arr_len == 1) {
            if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, arr[0], type_Double)) {
                return;
            }
            Q(nativeQueryPtr)->greater_equal(S(arr[0]), static_cast<double>(value));
        }
        else {
            TableRef table_ref = getTableForLinkQuery(nativeQueryPtr, arr);
            Q(nativeQueryPtr)->and_query(numeric_link_greaterequal<Double, double, jdouble>(table_ref, arr[arr_len-1], value));
        }
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeLess__J_3JD(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlongArray columnIndexes, jdouble value)
{
    JniLongArray arr(env, columnIndexes);
    jsize arr_len = arr.len();
    try {
        if (arr_len == 1) {
            if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, arr[0], type_Double)) {
                return;
            }
            Q(nativeQueryPtr)->less(S(arr[0]), static_cast<double>(value));
        }
        else {
            TableRef table_ref = getTableForLinkQuery(nativeQueryPtr, arr);
            Q(nativeQueryPtr)->and_query(numeric_link_less<Double, double, jdouble>(table_ref, arr[arr_len-1], value));
        }
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeLessEqual__J_3JD(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlongArray columnIndexes, jdouble value)
{
    JniLongArray arr(env, columnIndexes);
    jsize arr_len = arr.len();
    try {
        if (arr_len == 1) {
            if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, arr[0], type_Double)) {
                return;
            }
            Q(nativeQueryPtr)->less_equal(S(arr[0]), static_cast<double>(value));
        }
        else {
            TableRef table_ref = getTableForLinkQuery(nativeQueryPtr, arr);
            Q(nativeQueryPtr)->and_query(numeric_link_lessequal<Double, double, jdouble>(table_ref, arr[arr_len-1], value));
        }
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeBetween__J_3JDD(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlongArray columnIndexes, jdouble value1, jdouble value2)
{
    JniLongArray arr(env, columnIndexes);
    jsize arr_len = arr.len();
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
    } CATCH_STD()
}


// DateTime

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeEqualDateTime(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlongArray columnIndexes, jlong value)
{
    JniLongArray arr(env, columnIndexes);
    jsize arr_len = arr.len();
    try {
        if (arr_len == 1) {
            if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, arr[0], type_DateTime)) {
                return;
            }
            Q(nativeQueryPtr)->equal_datetime(S(arr[0]), DateTime(value));
        }
        else {
            TableRef table_ref = getTableForLinkQuery(nativeQueryPtr, arr);
            Q(nativeQueryPtr)->and_query(
                    numeric_link_equal<DateTime, DateTime, jlong>(table_ref, arr[arr_len-1], value));
        }
    } CATCH_STD()
}

JNIEXPORT void JNICALL JNICALL Java_io_realm_internal_TableQuery_nativeNotEqualDateTime(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlongArray columnIndexes, jlong value)
{
    JniLongArray arr(env, columnIndexes);
    jsize arr_len = arr.len();
    try {
        if (arr_len == 1) {
            if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, arr[0], type_DateTime)) {
                return;
            }
            Q(nativeQueryPtr)->not_equal_datetime(S(arr[0]), DateTime(value));
        }
        else {
            TableRef table_ref = getTableForLinkQuery(nativeQueryPtr, arr);
            Q(nativeQueryPtr)->and_query(
                    numeric_link_notequal<DateTime, DateTime, jlong>(table_ref, arr[arr_len-1], value));
        }
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeGreaterDateTime(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlongArray columnIndexes, jlong value)
{
    JniLongArray arr(env, columnIndexes);
    jsize arr_len = arr.len();
    try {
        if (arr_len == 1) {
            if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, arr[0], type_DateTime)) {
                return;
            }
            Q(nativeQueryPtr)->greater_datetime(S(arr[0]), DateTime(value));
        }
        else {
            TableRef table_ref = getTableForLinkQuery(nativeQueryPtr, arr);
            Q(nativeQueryPtr)->and_query(numeric_link_greater<DateTime, DateTime, jlong>(table_ref, arr[arr_len-1], value));
        }
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeGreaterEqualDateTime(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlongArray columnIndexes, jlong value)
{
    JniLongArray arr(env, columnIndexes);
    jsize arr_len = arr.len();
    try {
        if (arr_len == 1) {
            if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, arr[0], type_DateTime)) {
                return;
            }
            Q(nativeQueryPtr)->greater_equal_datetime(S(arr[0]), DateTime(value));
        }
        else {
            TableRef table_ref = getTableForLinkQuery(nativeQueryPtr, arr);
            Q(nativeQueryPtr)->and_query(numeric_link_greaterequal<DateTime, DateTime, jlong>(table_ref, arr[arr_len-1], value));
        }
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeLessDateTime(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlongArray columnIndexes, jlong value)
{
    JniLongArray arr(env, columnIndexes);
    jsize arr_len = arr.len();
    try {
        if (arr_len == 1) {
            if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, arr[0], type_DateTime)) {
                return;
            }
            Q(nativeQueryPtr)->less_datetime(S(arr[0]), DateTime(value));
        }
        else {
            TableRef table_ref = getTableForLinkQuery(nativeQueryPtr, arr);
            Q(nativeQueryPtr)->and_query(numeric_link_less<DateTime, DateTime, jlong>(table_ref, arr[arr_len-1], value));
        }
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeLessEqualDateTime(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlongArray columnIndexes, jlong value)
{
    JniLongArray arr(env, columnIndexes);
    jsize arr_len = arr.len();
    try {
        if (arr_len == 1) {
            if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, arr[0], type_DateTime)) {
                return;
            }
            Q(nativeQueryPtr)->less_equal_datetime(S(arr[0]), DateTime(value));
        }
        else {
            TableRef table_ref = getTableForLinkQuery(nativeQueryPtr, arr);
            Q(nativeQueryPtr)->and_query(numeric_link_lessequal<DateTime, DateTime, jlong>(table_ref, arr[arr_len-1], value));
        }
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeBetweenDateTime(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlongArray columnIndexes, jlong value1, jlong value2)
{
    JniLongArray arr(env, columnIndexes);
    jsize arr_len = arr.len();
    try {
        if (arr_len == 1) {
            if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, arr[0], type_DateTime)) {
                return;
            }
            Q(nativeQueryPtr)->between_datetime(S(arr[0]), DateTime(value1), DateTime(value2));
        }
        else {
            ThrowException(env, IllegalArgument, "between() does not support queries using child object fields.");
        }
    } CATCH_STD()
}

// Bool

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeEqual__J_3JZ(
  JNIEnv* env, jobject, jlong nativeQueryPtr, jlongArray columnIndexes, jboolean value)
{
    JniLongArray arr(env, columnIndexes);
    try {    jsize arr_len = arr.len();

        if (arr_len == 1) {
            if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, arr[0], type_Bool)) {
                return;
            }
            Q(nativeQueryPtr)->equal(S(arr[0]), value != 0 ? true : false);
        }
        else {
            TableRef table_ref = getTableForLinkQuery(nativeQueryPtr, arr);
            Q(nativeQueryPtr)->and_query(numeric_link_equal<Bool, bool, jboolean>(table_ref, arr[arr_len-1], value));
        }
    } CATCH_STD()
}

// String

enum StringPredicate {
    StringEqual,
    StringNotEqual,
    StringContains,
    StringBeginsWith,
    StringEndsWith
};


void TableQuery_StringPredicate(JNIEnv *env, jlong nativeQueryPtr, jlongArray columnIndexes, jstring value, jboolean caseSensitive, StringPredicate predicate) {
    JniLongArray arr(env, columnIndexes);
    jsize arr_len = arr.len();
    try {
        if (value == NULL) {
            if (!TBL_AND_COL_NULLABLE(env, getTableByArray(nativeQueryPtr, arr).get(), arr[arr_len-1])) {
                return;
            }
        }
        bool is_case_sensitive = caseSensitive ? true : false;
        JStringAccessor value2(env, value); // throws
        if (arr_len == 1) {
            if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, arr[0], type_String)) {
                return;
            }
            switch (predicate) {
            case StringEqual:
                Q(nativeQueryPtr)->equal(S(arr[0]), value2, is_case_sensitive);
                break;
            case StringNotEqual:
                Q(nativeQueryPtr)->not_equal(S(arr[0]), value2, is_case_sensitive);
                break;
            case StringContains:
                Q(nativeQueryPtr)->contains(S(arr[0]), value2, is_case_sensitive);
                break;
            case StringBeginsWith:
                Q(nativeQueryPtr)->begins_with(S(arr[0]), value2, is_case_sensitive);
                break;
            case StringEndsWith:
                Q(nativeQueryPtr)->ends_with(S(arr[0]), value2, is_case_sensitive);
                break;
            }
        }
        else {
            TableRef table_ref = getTableForLinkQuery(nativeQueryPtr, arr);
            switch (predicate) {
            case StringEqual:
                Q(nativeQueryPtr)->and_query(table_ref->column<String>(size_t(arr[arr_len-1])).equal(StringData(value2), is_case_sensitive));
                break;
            case StringNotEqual:
                Q(nativeQueryPtr)->and_query(table_ref->column<String>(size_t(arr[arr_len-1])).not_equal(StringData(value2), is_case_sensitive));
                break;
            case StringContains:
                Q(nativeQueryPtr)->and_query(table_ref->column<String>(size_t(arr[arr_len-1])).contains(StringData(value2), is_case_sensitive));
                break;
            case StringBeginsWith:
                Q(nativeQueryPtr)->and_query(table_ref->column<String>(size_t(arr[arr_len-1])).begins_with(StringData(value2), is_case_sensitive));
                break;
            case StringEndsWith:
                Q(nativeQueryPtr)->and_query(table_ref->column<String>(size_t(arr[arr_len-1])).ends_with(StringData(value2), is_case_sensitive));
                break;
            }
        }
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeEqual__J_3JLjava_lang_String_2Z(
    JNIEnv *env, jobject, jlong nativeQueryPtr, jlongArray columnIndexes, jstring value, jboolean caseSensitive)
{
    TableQuery_StringPredicate(env, nativeQueryPtr, columnIndexes, value, caseSensitive, StringEqual);
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeNotEqual__J_3JLjava_lang_String_2Z(
    JNIEnv *env, jobject, jlong nativeQueryPtr, jlongArray columnIndexes, jstring value, jboolean caseSensitive)
{
    TableQuery_StringPredicate(env, nativeQueryPtr, columnIndexes, value, caseSensitive, StringNotEqual);
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeBeginsWith(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlongArray columnIndexes, jstring value, jboolean caseSensitive)
{
    TableQuery_StringPredicate(env, nativeQueryPtr, columnIndexes, value, caseSensitive, StringBeginsWith);
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeEndsWith(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlongArray columnIndexes, jstring value, jboolean caseSensitive)
{
    TableQuery_StringPredicate(env, nativeQueryPtr, columnIndexes, value, caseSensitive, StringEndsWith);
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeContains(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlongArray columnIndexes, jstring value, jboolean caseSensitive)
{
    TableQuery_StringPredicate(env, nativeQueryPtr, columnIndexes, value, caseSensitive, StringContains);
}


// General ----------------------------------------------------
// TODO:
// Some of these methods may not need the check for Table/Query validity,
// as they are called for each method when building up the query.
// Consider to reduce to just the "action" methods on Query

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeTableview(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlong nativeTableViewPtr)
{
    Query* pQuery = Q(nativeQueryPtr);
    if (!QUERY_VALID(env, pQuery))
        return;
    try {
        pQuery->get_table()->where(TV(nativeTableViewPtr));
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeGroup(
    JNIEnv* env, jobject, jlong nativeQueryPtr)
{
    Query* pQuery = Q(nativeQueryPtr);
    if (!QUERY_VALID(env, pQuery))
        return;
    try {
        pQuery->group();
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeEndGroup(
    JNIEnv* env, jobject, jlong nativeQueryPtr)
{
    Query* pQuery = Q(nativeQueryPtr);
    if (!QUERY_VALID(env, pQuery))
        return;
    try {
        pQuery->end_group();
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeOr(
    JNIEnv* env, jobject, jlong nativeQueryPtr)
{
    // No verification of parameters needed?
    Query* pQuery = Q(nativeQueryPtr);
    if (!QUERY_VALID(env, pQuery))
        return;
    try {
        pQuery->Or();
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeNot(
    JNIEnv* env, jobject, jlong nativeQueryPtr)
{
    Query* pQuery = Q(nativeQueryPtr);
    if (!QUERY_VALID(env, pQuery))
        return;
    try {
        pQuery->Not();
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeSubtable(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlong columnIndex)
{
    TableQuery* pTQuery = TQ(nativeQueryPtr);
    if (!QUERY_VALID(env, pTQuery))
        return;

    try {
        Table* pTable = pTQuery->get_current_table().get();
        pTQuery->push_subtable(S(columnIndex));
        if (!COL_INDEX_AND_TYPE_VALID(env, pTable, columnIndex, type_Table))
            return;

        pTQuery->subtable(S(columnIndex));
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeParent(
    JNIEnv* env, jobject, jlong nativeQueryPtr)
{
    TableQuery* pTQuery = TQ(nativeQueryPtr);
    if (!QUERY_VALID(env, pTQuery))
        return;
    try {
        if (pTQuery->pop_subtable()) {
            pTQuery->end_subtable();
        }
        else {
            ThrowException(env, UnsupportedOperation, "No matching subtable().");
        }
    } CATCH_STD()
}


// Find --------------------------------------


JNIEXPORT jlong JNICALL Java_io_realm_internal_TableQuery_nativeFind(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlong fromTableRow)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = pQuery->get_table().get();
    if (!QUERY_VALID(env, pQuery))
        return -1;
    // It's valid to go 1 past the end index
    if ((fromTableRow < 0) || (S(fromTableRow) > pTable->size())) {
        // below check will fail with appropriate exception
        (void) ROW_INDEX_VALID(env, pTable, fromTableRow);
        return -1;
    }

    try {
        size_t r = pQuery->find( S(fromTableRow) );
        return (r == not_found) ? jlong(-1) : jlong(r);
    } CATCH_STD()
    return -1;
}

std::unique_ptr<Query> getHandoverQuery (jlong bgSharedGroupPtr, jlong replicationPtr, jlong queryPtr)
{
    SharedGroup::Handover<Query> *handoverQueryPtr = HO(Query, queryPtr);
    std::unique_ptr<SharedGroup::Handover<Query>> handoverQuery(handoverQueryPtr);

    SG(bgSharedGroupPtr)->end_read();

    SharedGroup::VersionID currentVersion = SG(bgSharedGroupPtr)->get_version_of_current_transaction();
    bool isDifferentVersions = (currentVersion != handoverQuery->version);
    if (isDifferentVersions) {
        SG(bgSharedGroupPtr)->begin_read(handoverQuery->version);
    } else {
        SG(bgSharedGroupPtr)->begin_read();
    }

    std::unique_ptr<Query> query = SG(bgSharedGroupPtr)->import_from_handover(std::move(handoverQuery));
    if (isDifferentVersions) {
        LangBindHelper::advance_read(*SG(bgSharedGroupPtr), *CH(replicationPtr));
    }

    return query;
}

// queryPtr would be owned and released by this function
JNIEXPORT jlong JNICALL Java_io_realm_internal_TableQuery_nativeFindWithHandover(
    JNIEnv* env, jobject, jlong bgSharedGroupPtr, jlong replicationPtr, jlong queryPtr, jlong fromTableRow)
{
    TR_ENTER()
    try {
        std::unique_ptr<Query> query = getHandoverQuery(bgSharedGroupPtr, replicationPtr, queryPtr);
        TableRef table = query->get_table();

        if (!QUERY_VALID(env, query.get())) {
            return 0;
        }

        // It's valid to go 1 past the end index
        if ((fromTableRow < 0) || (S(fromTableRow) > table->size())) {
            // below check will fail with appropriate exception
            (void) ROW_INDEX_VALID(env, table.get(), fromTableRow);
            return 0;
        }

        size_t r = query->find(S(fromTableRow));
        if (r == not_found) {
            return 0;
        } else {
            // handover the result
            Row row = (*table)[r];
            std::unique_ptr<SharedGroup::Handover<Row>> handover = SG(
                    bgSharedGroupPtr)->export_for_handover(row);
            return reinterpret_cast<jlong>(handover.release());
        }

    } CATCH_STD()
    return 0;
}


JNIEXPORT jlong JNICALL Java_io_realm_internal_TableQuery_nativeFindAll(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlong start, jlong end, jlong limit)
{
    TR_ENTER()
    Query* query = Q(nativeQueryPtr);
    TableRef table =  query->get_table();
    if (!QUERY_VALID(env, query) ||
        !ROW_INDEXES_VALID(env, table.get(), start, end, limit))
        return -1;
    try {
        TableView* tableView = new TableView( query->find_all(S(start), S(end), S(limit)) );
        return reinterpret_cast<jlong>(tableView);
    } CATCH_STD()
    return -1;
}

// queryPtr would be owned and released by this function
JNIEXPORT jlong JNICALL Java_io_realm_internal_TableQuery_nativeFindAllWithHandover
  (JNIEnv* env, jobject, jlong bgSharedGroupPtr, jlong replicationPtr, jlong queryPtr, jlong start, jlong end, jlong limit)
  {
      TR_ENTER()
      try {
          std::unique_ptr<Query> query = getHandoverQuery(bgSharedGroupPtr, replicationPtr, queryPtr);
          return findAllWithHandover(env, bgSharedGroupPtr, std::move(query), start, end, limit);
      } CATCH_STD()
      return 0;
  }



// Should match the values in Java ArgumentsHolder class
enum query_type {QUERY_TYPE_FIND_ALL = 0, QUERY_TYPE_DISTINCT = 4, QUERY_TYPE_FIND_ALL_SORTED = 1, QUERY_TYPE_FIND_ALL_MULTI_SORTED = 2};

// batch update of async queries
JNIEXPORT jlongArray JNICALL Java_io_realm_internal_TableQuery_nativeBatchUpdateQueries
        (JNIEnv *env, jobject, jlong bgSharedGroupPtr, jlong replicationPtr,
         jlongArray  handover_queries_array /*list of handover queries*/,
         jobjectArray  query_param_matrix /*type & params of the query to be updated*/,
         jobjectArray  multi_sorted_indices_matrix,
         jobjectArray  multi_sorted_order_matrix)
{
    TR_ENTER()
    try {
        JniLongArray handover_queries_pointer_array(env, handover_queries_array);

        const size_t number_of_queries = env->GetArrayLength(query_param_matrix);

        std::vector<jlong> exported_handover_tableview_array(number_of_queries);

        // Step1: Position the shared group at the handover query version so we can import all queries

        // read the first query to determine the version we should use
        SharedGroup::Handover<Query> *handoverQueryPtr = HO(Query, handover_queries_pointer_array[0]);
        std::unique_ptr<SharedGroup::Handover<Query>> handoverQuery(handoverQueryPtr);
        // position this shared group at the specified version
        SG(bgSharedGroupPtr)->begin_read(handoverQuery->version);

        std::vector<std::unique_ptr<Query>> queries(number_of_queries);

        // import the first query
        queries[0] = std::move(SG(bgSharedGroupPtr)->import_from_handover(std::move(handoverQuery)));

        // import the rest of the queries
        for (size_t i = 1; i < number_of_queries; ++i) {
            std::unique_ptr<SharedGroup::Handover<Query>> handoverQuery(HO(Query, handover_queries_pointer_array[i]));
            queries[i] = std::move(SG(bgSharedGroupPtr)->import_from_handover(std::move(handoverQuery)));
        }

        // Step2: Bring the queries into the latest shared group version
        LangBindHelper::advance_read(*SG(bgSharedGroupPtr), *CH(replicationPtr));

        // Step3: Run & export the queries against the latest shared group
        for (size_t i = 0; i < number_of_queries; ++i) {
            JniLongArray query_param_array(env, (jlongArray) env->GetObjectArrayElement(query_param_matrix, i));
            switch (query_param_array[0]) { // 0, index of the type of query, the next indicies are parameters
                case QUERY_TYPE_FIND_ALL: {// nativeFindAllWithHandover
                    exported_handover_tableview_array[i] =
                            findAllWithHandover
                                    (env,
                                     bgSharedGroupPtr,
                                     std::move(queries[i]),
                                     query_param_array[1]/*start*/,
                                     query_param_array[2]/*end*/,
                                     query_param_array[3]/*limit*/);
                    break;
                }
                case QUERY_TYPE_DISTINCT: {// nativeGetDistinctViewWithHandover
                    exported_handover_tableview_array[i] =
                            getDistinctViewWithHandover
                                    (env,
                                     bgSharedGroupPtr,
                                     std::move(queries[i]),
                                     query_param_array[1]/*columnIndex*/);
                    break;
                }
                case QUERY_TYPE_FIND_ALL_SORTED: {// nativeFindAllSortedWithHandover
                    exported_handover_tableview_array[i] =
                            findAllSortedWithHandover
                                    (env,
                                     bgSharedGroupPtr,
                                     std::move(queries[i]),
                                     query_param_array[1]/*start*/,
                                     query_param_array[2]/*end*/,
                                     query_param_array[3]/*limit*/,
                                     query_param_array[4]/*columnIndex*/,
                                     query_param_array[5] == 1/*ascending order*/);
                    break;
                }
                case QUERY_TYPE_FIND_ALL_MULTI_SORTED: {// nativeFindAllMultiSortedWithHandover
                    jlongArray column_indices_array = (jlongArray) env->GetObjectArrayElement(
                            multi_sorted_indices_matrix, i);
                    jbooleanArray column_order_array = (jbooleanArray) env->GetObjectArrayElement(
                            multi_sorted_order_matrix, i);
                    exported_handover_tableview_array[i] =
                            findAllMultiSortedWithHandover
                                    (env,
                                     bgSharedGroupPtr,
                                     std::move(queries[i]),
                                     query_param_array[1]/*start*/,
                                     query_param_array[2]/*end*/,
                                     query_param_array[3]/*limit*/,
                                     column_indices_array/*columnIndices*/,
                                     column_order_array/*ascending orders*/);
                    break;
                }
                default:
                    ThrowException(env, FatalError, "Unknown type of query.");
                    return NULL;
            }
        }

        jlongArray exported_handover_tableview = env->NewLongArray(number_of_queries);
        if (exported_handover_tableview == NULL) {
            ThrowException(env, OutOfMemory, "Could not allocate memory to return updated queries.");
            return NULL;
        }
        env->SetLongArrayRegion(exported_handover_tableview, 0, number_of_queries,
                exported_handover_tableview_array.data());
        return exported_handover_tableview;

    } CATCH_STD()
    return NULL;
}


JNIEXPORT jlong JNICALL Java_io_realm_internal_TableQuery_nativeGetDistinctViewWithHandover
        (JNIEnv *env, jobject, jlong bgSharedGroupPtr, jlong replicationPtr, jlong queryPtr, jlong columnIndex)
{
    TR_ENTER()
    try {
        std::unique_ptr<Query> query = getHandoverQuery(bgSharedGroupPtr, replicationPtr, queryPtr);
        return getDistinctViewWithHandover(env, bgSharedGroupPtr, std::move(query), columnIndex);
    } CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_TableQuery_nativeFindAllSortedWithHandover
  (JNIEnv *env, jobject, jlong bgSharedGroupPtr, jlong replicationPtr, jlong queryPtr, jlong start, jlong end, jlong limit, jlong columnIndex, jboolean ascending)
  {
      TR_ENTER()
      try {
          std::unique_ptr<Query> query = getHandoverQuery(bgSharedGroupPtr, replicationPtr, queryPtr);
          return findAllSortedWithHandover(env, bgSharedGroupPtr, std::move(query), start, end, limit, columnIndex, ascending);
      } CATCH_STD()
      return 0;
  }

JNIEXPORT jlong JNICALL Java_io_realm_internal_TableQuery_nativeFindAllMultiSortedWithHandover
  (JNIEnv *env, jobject, jlong bgSharedGroupPtr, jlong replicationPtr, jlong queryPtr, jlong start, jlong end, jlong limit, jlongArray columnIndices, jbooleanArray ascending)
  {
      TR_ENTER()
      try {
          // import the handover query pointer using the background SharedGroup
          std::unique_ptr<Query> query = getHandoverQuery(bgSharedGroupPtr, replicationPtr, queryPtr);
          return findAllMultiSortedWithHandover(env, bgSharedGroupPtr, std::move(query), start, end, limit,columnIndices, ascending);
      } CATCH_STD()
      return 0;
  }

// Integer Aggregates

JNIEXPORT jlong JNICALL Java_io_realm_internal_TableQuery_nativeSumInt(
    JNIEnv* env, jobject, jlong nativeQueryPtr,
    jlong columnIndex, jlong start, jlong end, jlong limit)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = pQuery->get_table().get();
    if (!QUERY_VALID(env, pQuery) ||
        !COL_INDEX_AND_TYPE_VALID(env, pTable, columnIndex, type_Int) ||
        !ROW_INDEXES_VALID(env, pTable, start, end, limit))
        return 0;
    try {
        return pQuery->sum_int(S(columnIndex), NULL, S(start), S(end), S(limit));
    } CATCH_STD()
    return 0;
}

JNIEXPORT jobject JNICALL Java_io_realm_internal_TableQuery_nativeMaximumInt(
    JNIEnv* env, jobject, jlong nativeQueryPtr,
    jlong columnIndex, jlong start, jlong end, jlong limit)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = pQuery->get_table().get();
    if (!QUERY_VALID(env, pQuery) ||
        !COL_INDEX_AND_TYPE_VALID(env, pTable, columnIndex, type_Int) ||
        !ROW_INDEXES_VALID(env, pTable, start, end, limit))
        return NULL;
    try {
        size_t return_ndx;
        int64_t result = pQuery->maximum_int(S(columnIndex), NULL, S(start), S(end), S(limit), &return_ndx);
        if (return_ndx != npos) {
            return NewLong(env, result);
        }
    } CATCH_STD()
    return NULL;
}

JNIEXPORT jobject JNICALL Java_io_realm_internal_TableQuery_nativeMinimumInt(
    JNIEnv* env, jobject, jlong nativeQueryPtr,
    jlong columnIndex, jlong start, jlong end, jlong limit)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = pQuery->get_table().get();
    if (!QUERY_VALID(env, pQuery) ||
        !COL_INDEX_AND_TYPE_VALID(env, pTable, columnIndex, type_Int) ||
        !ROW_INDEXES_VALID(env, pTable, start, end, limit))
        return NULL;
    try {
        size_t return_ndx;
        int64_t result = pQuery->minimum_int(S(columnIndex), NULL, S(start), S(end), S(limit), &return_ndx);
        if (return_ndx != npos) {
            return NewLong(env, result);
        }
    } CATCH_STD()
    return NULL;
}

JNIEXPORT jdouble JNICALL Java_io_realm_internal_TableQuery_nativeAverageInt(
    JNIEnv* env, jobject, jlong nativeQueryPtr,
    jlong columnIndex, jlong start, jlong end, jlong limit)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = pQuery->get_table().get();
    if (!QUERY_VALID(env, pQuery) ||
        !COL_INDEX_AND_TYPE_VALID(env, pTable, columnIndex, type_Int) ||
        !ROW_INDEXES_VALID(env, pTable, start, end, limit))
        return 0;
    try {
        size_t resultcount;
        //TODO: return resultcount?
        double avg = pQuery->average_int(S(columnIndex), &resultcount, S(start), S(end), S(limit));
        //fprintf(stderr, "!!!Average(%d, %d) = %f (%d results)\n", start, end, avg, resultcount); fflush(stderr);
        return avg;
    } CATCH_STD()
    return 0;
}


// float Aggregates

JNIEXPORT jdouble JNICALL Java_io_realm_internal_TableQuery_nativeSumFloat(
    JNIEnv* env, jobject, jlong nativeQueryPtr,
    jlong columnIndex, jlong start, jlong end, jlong limit)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = pQuery->get_table().get();
    if (!QUERY_VALID(env, pQuery) ||
        !COL_INDEX_AND_TYPE_VALID(env, pTable, columnIndex, type_Float) ||
        !ROW_INDEXES_VALID(env, pTable, start, end, limit))
        return 0;
    try {
        return pQuery->sum_float(S(columnIndex), NULL, S(start), S(end), S(limit));
    } CATCH_STD()
    return 0;
}

JNIEXPORT jobject JNICALL Java_io_realm_internal_TableQuery_nativeMaximumFloat(
    JNIEnv* env, jobject, jlong nativeQueryPtr,
    jlong columnIndex, jlong start, jlong end, jlong limit)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = pQuery->get_table().get();
    if (!QUERY_VALID(env, pQuery) ||
        !COL_INDEX_AND_TYPE_VALID(env, pTable, columnIndex, type_Float) ||
        !ROW_INDEXES_VALID(env, pTable, start, end, limit))
        return NULL;
    try {
        size_t return_ndx;
        float result = pQuery->maximum_float(S(columnIndex), NULL, S(start), S(end), S(limit), &return_ndx);
        if (return_ndx != npos) {
            return NewFloat(env, result);
        }
    } CATCH_STD()
    return NULL;
}

JNIEXPORT jobject JNICALL Java_io_realm_internal_TableQuery_nativeMinimumFloat(
    JNIEnv* env, jobject, jlong nativeQueryPtr,
    jlong columnIndex, jlong start, jlong end, jlong limit)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = pQuery->get_table().get();
    if (!QUERY_VALID(env, pQuery) ||
        !COL_INDEX_AND_TYPE_VALID(env, pTable, columnIndex, type_Float) ||
        !ROW_INDEXES_VALID(env, pTable, start, end, limit))
        return NULL;
    try {
        size_t return_ndx;
        float result = pQuery->minimum_float(S(columnIndex), NULL, S(start), S(end), S(limit), &return_ndx);
        if (return_ndx != npos) {
            return NewFloat(env, result);
        }
    } CATCH_STD()
    return NULL;
}

JNIEXPORT jdouble JNICALL Java_io_realm_internal_TableQuery_nativeAverageFloat(
    JNIEnv* env, jobject, jlong nativeQueryPtr,
    jlong columnIndex, jlong start, jlong end, jlong limit)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = pQuery->get_table().get();
    if (!QUERY_VALID(env, pQuery) ||
        !COL_INDEX_AND_TYPE_VALID(env, pTable, columnIndex, type_Float) ||
        !ROW_INDEXES_VALID(env, pTable, start, end, limit))
        return 0;
    try {
        size_t resultcount;
        double avg = pQuery->average_float(S(columnIndex), &resultcount, S(start), S(end), S(limit));
        return avg;
    } CATCH_STD()
    return 0;
}

// double Aggregates

JNIEXPORT jdouble JNICALL Java_io_realm_internal_TableQuery_nativeSumDouble(
    JNIEnv* env, jobject, jlong nativeQueryPtr,
    jlong columnIndex, jlong start, jlong end, jlong limit)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = pQuery->get_table().get();
    if (!QUERY_VALID(env, pQuery) ||
        !COL_INDEX_AND_TYPE_VALID(env, pTable, columnIndex, type_Double) ||
        !ROW_INDEXES_VALID(env, pTable, start, end, limit))
        return 0;
    try {
        return pQuery->sum_double(S(columnIndex), NULL, S(start), S(end), S(limit));
    } CATCH_STD()
    return 0;
}

JNIEXPORT jobject JNICALL Java_io_realm_internal_TableQuery_nativeMaximumDouble(
    JNIEnv* env, jobject, jlong nativeQueryPtr,
    jlong columnIndex, jlong start, jlong end, jlong limit)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = pQuery->get_table().get();
    if (!QUERY_VALID(env, pQuery) ||
        !COL_INDEX_AND_TYPE_VALID(env, pTable, columnIndex, type_Double) ||
        !ROW_INDEXES_VALID(env, pTable, start, end, limit))
        return NULL;
    try {
        size_t return_ndx;
        double result = pQuery->maximum_double(S(columnIndex), NULL, S(start), S(end), S(limit), &return_ndx);
        if (return_ndx != npos) {
            return NewDouble(env, result);
        }
    } CATCH_STD()
    return NULL;
}

JNIEXPORT jobject JNICALL Java_io_realm_internal_TableQuery_nativeMinimumDouble(
    JNIEnv* env, jobject, jlong nativeQueryPtr,
    jlong columnIndex, jlong start, jlong end, jlong limit)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = pQuery->get_table().get();
    if (!QUERY_VALID(env, pQuery) ||
        !COL_INDEX_AND_TYPE_VALID(env, pTable, columnIndex, type_Double) ||
        !ROW_INDEXES_VALID(env, pTable, start, end, limit))
        return NULL;
    try {
        size_t return_ndx;
        double result = pQuery->minimum_double(S(columnIndex), NULL, S(start), S(end), S(limit), &return_ndx);
        if (return_ndx != npos) {
            return NewDouble(env, result);
        }
    } CATCH_STD()
    return NULL;
}

JNIEXPORT jdouble JNICALL Java_io_realm_internal_TableQuery_nativeAverageDouble(
    JNIEnv* env, jobject, jlong nativeQueryPtr,
    jlong columnIndex, jlong start, jlong end, jlong limit)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = pQuery->get_table().get();
    if (!QUERY_VALID(env, pQuery) ||
        !COL_INDEX_AND_TYPE_VALID(env, pTable, columnIndex, type_Double) ||
        !ROW_INDEXES_VALID(env, pTable, start, end, limit))
        return 0;
    try {
        //TODO: Return resultcount
        size_t resultcount;
        double avg = pQuery->average_double(S(columnIndex), &resultcount, S(start), S(end), S(limit));
        return avg;
    } CATCH_STD()
    return 0;
}


// date aggregates

JNIEXPORT jobject JNICALL Java_io_realm_internal_TableQuery_nativeMaximumDate(
    JNIEnv* env, jobject, jlong nativeQueryPtr,
    jlong columnIndex, jlong start, jlong end, jlong limit)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = pQuery->get_table().get();
    if (!QUERY_VALID(env, pQuery) ||
        !COL_INDEX_AND_TYPE_VALID(env, pTable, columnIndex, type_DateTime) ||
        !ROW_INDEXES_VALID(env, pTable, start, end, limit))
        return NULL;
    try {
        size_t return_ndx;
        DateTime result = pQuery->maximum_int(S(columnIndex), NULL, S(start), S(end), S(limit), &return_ndx);
        if (return_ndx != npos) {
            return NewLong(env, result.get_datetime());
        }
    } CATCH_STD()
    return NULL;
}

JNIEXPORT jobject JNICALL Java_io_realm_internal_TableQuery_nativeMinimumDate(
    JNIEnv* env, jobject, jlong nativeQueryPtr,
    jlong columnIndex, jlong start, jlong end, jlong limit)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = pQuery->get_table().get();
    if (!QUERY_VALID(env, pQuery) ||
        !COL_INDEX_AND_TYPE_VALID(env, pTable, columnIndex, type_DateTime) ||
        !ROW_INDEXES_VALID(env, pTable, start, end, limit))
        return NULL;
    try {
        size_t return_ndx;
        DateTime result = pQuery->minimum_int(S(columnIndex), NULL, S(start), S(end), S(limit), &return_ndx);
        if (return_ndx != npos) {
            return NewLong(env, result.get_datetime());
        }
    } CATCH_STD()
    return NULL;
}

// Count, Remove

JNIEXPORT jlong JNICALL Java_io_realm_internal_TableQuery_nativeCount(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlong start, jlong end, jlong limit)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = pQuery->get_table().get();
    if (!QUERY_VALID(env, pQuery) ||
        !ROW_INDEXES_VALID(env, pTable, start, end, limit))
        return 0;
    try {
        return pQuery->count(S(start), S(end), S(limit));
    } CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_TableQuery_nativeRemove(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlong start, jlong end, jlong limit)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = pQuery->get_table().get();
    if (!QUERY_VALID(env, pQuery) ||
        !ROW_INDEXES_VALID(env, pTable, start, end, limit))
        return 0;
    try {
        return pQuery->remove(S(start), S(end), S(limit));
    } CATCH_STD()
    return 0;
}

// isNull and isNotNull

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeIsNull(
    JNIEnv *env, jobject, jlong nativeQueryPtr, jlongArray columnIndexes)
{
    JniLongArray arr(env, columnIndexes);
    jsize arr_len = arr.len();
    Query* pQuery = Q(nativeQueryPtr);

    try {
        TableRef src_table_ref = getTableForLinkQuery(nativeQueryPtr, arr);
        jlong column_idx = arr[arr_len-1];
        TableRef table_ref = getTableByArray(nativeQueryPtr, arr);
        if (!TBL_AND_COL_NULLABLE(env, table_ref.get(), column_idx)) {
            return;
        }

        int col_type = table_ref->get_column_type(S(column_idx));
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
                case type_DateTime:
                    Q(nativeQueryPtr)->equal(S(column_idx), realm::null());
                    break;
                default:
                    // this point is unreachable
                    ThrowException(env, FatalError, "This is not reachable.");
                    return;
            }
        } else {
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
                case type_DateTime:
                    pQuery->and_query(src_table_ref->column<DateTime>(S(column_idx)) == realm::null());
                    break;
                default:
                    // this point is unreachable
                    ThrowException(env, FatalError, "This is not reachable.");
                    return;
            }
        }
    } CATCH_STD()
}

// handoverPtr will be released in this function
JNIEXPORT jlong JNICALL Java_io_realm_internal_TableQuery_nativeImportHandoverTableViewIntoSharedGroup
  (JNIEnv *env, jobject, jlong handoverPtr, jlong callerSharedGrpPtr)
  {
    TR_ENTER_PTR(handoverPtr)
    SharedGroup::Handover<TableView> *handoverTableViewPtr = HO(TableView, handoverPtr);
    std::unique_ptr<SharedGroup::Handover<TableView> > handoverTableView(handoverTableViewPtr);

    try {
        // import_from_handover will free (delete) the handover
        if (SG(callerSharedGrpPtr)->is_attached()) {
            std::unique_ptr<TableView> tableView = SG(callerSharedGrpPtr)->import_from_handover(
                    std::move(handoverTableView));
            return reinterpret_cast<jlong>(tableView.release());
        } else {
            ThrowException(env, RuntimeError, ERR_IMPORT_CLOSED_REALM);
        }
    } CATCH_STD()
    return 0;
  }

JNIEXPORT jlong JNICALL Java_io_realm_internal_TableQuery_nativeImportHandoverRowIntoSharedGroup
  (JNIEnv *env, jobject, jlong handoverPtr, jlong callerSharedGrpPtr)
  {
      TR_ENTER_PTR(handoverPtr)
      SharedGroup::Handover<Row> *handoverRowPtr = HO(Row, handoverPtr);
      std::unique_ptr<SharedGroup::Handover<Row>> handoverRow(handoverRowPtr);

      try {
          // import_from_handover will free (delete) the handover
          if (SG(callerSharedGrpPtr)->is_attached()) {
              std::unique_ptr<Row> row = SG(callerSharedGrpPtr)->import_from_handover(
                      std::move(handoverRow));
              return reinterpret_cast<jlong>(row.release());
          } else {
              ThrowException(env, RuntimeError, ERR_IMPORT_CLOSED_REALM);
          }
      } CATCH_STD()
      return 0;
  }

JNIEXPORT jlong JNICALL Java_io_realm_internal_TableQuery_nativeHandoverQuery
   (JNIEnv* env, jobject, jlong bgSharedGroupPtr, jlong nativeQueryPtr)
{
    TR_ENTER_PTR(nativeQueryPtr)
    Query* pQuery = Q(nativeQueryPtr);
    if (!QUERY_VALID(env, pQuery))
        return 0;
    try {
        std::unique_ptr<SharedGroup::Handover<Query> > handoverQueryPtr = SG(bgSharedGroupPtr)->export_for_handover(*pQuery, ConstSourcePayload::Copy);
        return reinterpret_cast<jlong>(handoverQueryPtr.release());
    } CATCH_STD()
    return 0;
}


JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeCloseQueryHandover
  (JNIEnv *, jobject, jlong nativeHandoverQuery)
  {
    TR_ENTER_PTR(nativeHandoverQuery)
    delete HO(Query, nativeHandoverQuery);
  }

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeIsNotNull
  (JNIEnv *env, jobject, jlong nativeQueryPtr, jlongArray columnIndexes) {
    JniLongArray arr(env, columnIndexes);
    jsize arr_len = arr.len();
    Query* pQuery = Q(nativeQueryPtr);
    try {
        TableRef src_table_ref = getTableForLinkQuery(nativeQueryPtr, arr);
        jlong column_idx = arr[arr_len-1];
        TableRef table_ref = getTableByArray(nativeQueryPtr, arr);

        if (!TBL_AND_COL_NULLABLE(env, table_ref.get(), column_idx)) {
            return;
        }

        int col_type = table_ref->get_column_type(S(column_idx));
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
                case type_DateTime:
                    pQuery->not_equal(S(column_idx), realm::null());
                    break;
                default:
                    // this point is unreachable
                    ThrowException(env, FatalError, "This is not reachable.");
                    return;
            }
        }
        else {
            switch (col_type) {
                case type_Link:
                    ThrowException(env, IllegalArgument, "isNotNull() by nested query for link field is not supported.");
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
                case type_DateTime:
                    pQuery->and_query(src_table_ref->column<DateTime>(S(column_idx)) != realm::null());
                    break;
                default:
                    // this point is unreachable
                    ThrowException(env, FatalError, "This is not reachable.");
                    return;
            }
        }
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeIsEmpty
    (JNIEnv *env, jobject, jlong nativeQueryPtr, jlongArray columnIndexes) {

    JniLongArray arr(env, columnIndexes);
    jsize arr_len = arr.len();
    Query* pQuery = Q(nativeQueryPtr);
    try {
        TableRef src_table_ref = getTableForLinkQuery(nativeQueryPtr, arr);
        jlong column_idx = arr[arr_len - 1];
        TableRef table_ref = getTableByArray(nativeQueryPtr, arr);

        int col_type = table_ref->get_column_type(S(column_idx));
        if (arr_len == 1) {
            // Field queries
            switch (col_type) {
                case type_Binary:
                    pQuery->equal(S(column_idx), BinaryData("", 0));
                    break;
                case type_LinkList:
                    pQuery->and_query(table_ref->column<LinkList>(S(column_idx)).count() == 0);
                    break;
                case type_String:
                    pQuery->equal(S(column_idx), "");
                    break;
                case type_Link:
                case type_Bool:
                case type_Int:
                case type_Float:
                case type_Double:
                case type_DateTime:
                default:
                    ThrowException(env, IllegalArgument, "isEmpty() only works on String, byte[] and RealmList.");
                    return;
            }
        }
        else {
            // Linked queries
            switch (col_type) {
                case type_Binary:
                    pQuery->and_query(src_table_ref->column<Binary>(S(column_idx)) == BinaryData("", 0));
                    break;
                case type_LinkList:
                    pQuery->and_query(src_table_ref->column<LinkList>(S(column_idx)).count() == 0);
                    break;
                case type_String:
                    pQuery->and_query(src_table_ref->column<String>(S(column_idx)) == "");
                    break;
                case type_Link:
                case type_Bool:
                case type_Int:
                case type_Float:
                case type_Double:
                case type_DateTime:
                default:
                    ThrowException(env, IllegalArgument, "isEmpty() only works on String, byte[] and RealmList across links.");
                    return;
            }
        }
    } CATCH_STD()
}
