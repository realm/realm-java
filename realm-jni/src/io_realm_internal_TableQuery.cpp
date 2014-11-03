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

#include "util.hpp"
#include "io_realm_internal_TableQuery.h"
#include "tablequery.hpp"

using namespace tightdb;

#if 1
#define COL_TYPE_VALID(env,ptr,col, type)           TBL_AND_COL_INDEX_AND_TYPE_VALID(env,ptr,col, type)
#define QUERY_COL_TYPE_VALID(env, jPtr, col, type)  query_col_type_valid(env, jPtr, col, type)
#define QUERY_VALID(env, pQuery)                    query_valid(env, pQuery)
#else
#define COL_TYPE_VALID(env,ptr,col, type)           (true)
#define QUERY_COL_TYPE_VALID(env, jPtr, col, type)  (true)
#define QUERY_VALID(env, pQuery)                    (true)
#endif

inline tightdb::Table* Ref2Ptr(tightdb::TableRef& tableref)
{
    return &*tableref;
}

inline bool query_valid(JNIEnv* env, Query* pQuery)
{
    TableRef pTable = pQuery->get_table();
    return TABLE_VALID(env, Ref2Ptr(pTable));
}

inline bool query_col_type_valid(JNIEnv* env, jlong nativeQueryPtr, jlong colIndex, DataType type)
{
    TableRef pTable = TQ(nativeQueryPtr)->get_current_table();
    return COL_TYPE_VALID(env, Ref2Ptr(pTable), colIndex, type);
}

//-------------------------------------------------------

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeClose(JNIEnv * env, jclass, jlong nativeQueryPtr) {
    TR((env, "Query nativeClose(ptr %x)\n", nativeQueryPtr));
    delete Q(nativeQueryPtr);
}

JNIEXPORT jstring JNICALL Java_io_realm_internal_TableQuery_nativeValidateQuery
(JNIEnv * env, jobject, jlong nativeQueryPtr)
{
    try {
        return to_jstring(env, Q(nativeQueryPtr)->validate());
    } CATCH_STD();
    return NULL;
}


// helper functions and macros
#define GET_ARRAY() \
    jsize arr_len = env->GetArrayLength(columnIndexes); \
    jlong *arr = env->GetLongArrayElements(columnIndexes, NULL);


#define RELEASE_ARRAY() \
    env->ReleaseLongArrayElements(columnIndexes, arr, 0);

TableRef getTableLink(jlong nativeQueryPtr, jlong *arr, jsize arr_len) {
    TableRef tbl = Q(nativeQueryPtr)->get_table();
    for (int i=0; i<arr_len-1; i++) {
        tbl->link(size_t(arr[i]));
    }
    return tbl;
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
    GET_ARRAY()
    try {
        if (arr_len == 1) {
            if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, arr[0], type_Int))
                return;
            Q(nativeQueryPtr)->equal(S(arr[0]), static_cast<int64_t>(value));
        }
        else {
            TableRef tbl = getTableLink(nativeQueryPtr, arr, arr_len);
            Q(nativeQueryPtr)->and_query(numeric_link_equal<Int, int64_t, jlong>(tbl, arr[arr_len-1], value));
        }
    } CATCH_STD()
    RELEASE_ARRAY()
}

JNIEXPORT void JNICALL JNICALL Java_io_realm_internal_TableQuery_nativeNotEqual__J_3JJ(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlongArray columnIndexes, jlong value)
{
    GET_ARRAY()
    try {
        if (arr_len == 1) {
            if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, arr[0], type_Int))
                return;
            Q(nativeQueryPtr)->not_equal(S(arr[0]), static_cast<int64_t>(value));
        }
        else {
            TableRef tbl = getTableLink(nativeQueryPtr, arr, arr_len);
            Q(nativeQueryPtr)->and_query(numeric_link_notequal<Int, int64_t, jlong>(tbl, arr[arr_len-1], value));
        }
    } CATCH_STD()
    RELEASE_ARRAY();
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeGreater__J_3JJ(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlongArray columnIndexes, jlong value)
{
    GET_ARRAY()
    try {
        if (arr_len == 1) {
            if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, arr[0], type_Int))
                return;
            Q(nativeQueryPtr)->greater(S(arr[0]), static_cast<int64_t>(value));
        }
        else {
            TableRef tbl = getTableLink(nativeQueryPtr, arr, arr_len);
            Q(nativeQueryPtr)->and_query(numeric_link_greater<Int, int64_t, jlong>(tbl, arr[arr_len-1], value));
        }
    } CATCH_STD()
    RELEASE_ARRAY();
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeGreaterEqual__J_3JJ(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlongArray columnIndexes, jlong value)
{
    GET_ARRAY()
    try {
        if (arr_len == 1) {
            if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, arr[0], type_Int))
                return;
            Q(nativeQueryPtr)->greater_equal(S(arr[0]), static_cast<int64_t>(value));
        }
        else {
            TableRef tbl = getTableLink(nativeQueryPtr, arr, arr_len);
            Q(nativeQueryPtr)->and_query(numeric_link_greaterequal<Int, int64_t, jlong>(tbl, arr[arr_len-1], value));
        }
    } CATCH_STD()
    RELEASE_ARRAY();
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeLess__J_3JJ(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlongArray columnIndexes, jlong value)
{
    GET_ARRAY()
    try {
        if (arr_len == 1) {
            if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, arr[0], type_Int))
                return;
            Q(nativeQueryPtr)->less(S(arr[0]), static_cast<int64_t>(value));
        }
        else {
            TableRef tbl = getTableLink(nativeQueryPtr, arr, arr_len);
            Q(nativeQueryPtr)->and_query(numeric_link_less<Int, int64_t, jlong>(tbl, arr[arr_len-1], value));
        }
    } CATCH_STD()
    RELEASE_ARRAY();
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeLessEqual__J_3JJ(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlongArray columnIndexes, jlong value)
{
    GET_ARRAY()
    try {
        if (arr_len == 1) {
            if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, arr[0], type_Int))
                return;
            Q(nativeQueryPtr)->less_equal(S(arr[0]), static_cast<int64_t>(value));
        }
        else {
            TableRef tbl = getTableLink(nativeQueryPtr, arr, arr_len);
            Q(nativeQueryPtr)->and_query(numeric_link_lessequal<Int, int64_t, jlong>(tbl, arr[arr_len-1], value));
        }
    } CATCH_STD()
    RELEASE_ARRAY();
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeBetween__J_3JJJ(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlongArray columnIndexes, jlong value1, jlong value2)
{
    GET_ARRAY()
    try {
        if (arr_len == 1) {
            if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, arr[0], type_Int))
                return;
            Q(nativeQueryPtr)->between(S(arr[0]), static_cast<int64_t>(value1), static_cast<int64_t>(value2));
        }
        else {
            Q(nativeQueryPtr)->group();
            TableRef tbl = getTableLink(nativeQueryPtr, arr, arr_len);
            Q(nativeQueryPtr)->and_query(numeric_link_greaterequal<Int, int64_t, jlong>(tbl, arr[arr_len-1], value1));
            tbl = getTableLink(nativeQueryPtr, arr, arr_len);
            Q(nativeQueryPtr)->and_query(numeric_link_lessequal<Int, int64_t, jlong>(tbl, arr[arr_len-1], value2));
            Q(nativeQueryPtr)->end_group();
        }
    } CATCH_STD()
    RELEASE_ARRAY();
}

// Float

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeEqual__J_3JF(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlongArray columnIndexes, jfloat value)
{
    GET_ARRAY()
    try {
        if (arr_len == 1) {
            if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, arr[0], type_Float))
                return;
            Q(nativeQueryPtr)->equal(S(arr[0]), static_cast<float>(value));
        }
        else {
            TableRef tbl = getTableLink(nativeQueryPtr, arr, arr_len);
            Q(nativeQueryPtr)->and_query(numeric_link_equal<Float, float, jfloat>(tbl, arr[arr_len-1], value));
        }
    } CATCH_STD()
    RELEASE_ARRAY()
}

JNIEXPORT void JNICALL JNICALL Java_io_realm_internal_TableQuery_nativeNotEqual__J_3JF(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlongArray columnIndexes, jfloat value)
{
    GET_ARRAY()
    try {
        if (arr_len == 1) {
            if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, arr[0], type_Float))
                return;
            Q(nativeQueryPtr)->not_equal(S(arr[0]), static_cast<float>(value));
        }
        else {
            TableRef tbl = getTableLink(nativeQueryPtr, arr, arr_len);
            Q(nativeQueryPtr)->and_query(numeric_link_notequal<Float, float, jfloat>(tbl, arr[arr_len-1], value));
        }
    } CATCH_STD()
    RELEASE_ARRAY();
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeGreater__J_3JF(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlongArray columnIndexes, jfloat value)
{
    GET_ARRAY()
    try {
        if (arr_len == 1) {
            if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, arr[0], type_Float))
                return;
            Q(nativeQueryPtr)->greater(S(arr[0]), static_cast<float>(value));
        }
        else {
            TableRef tbl = getTableLink(nativeQueryPtr, arr, arr_len);
            Q(nativeQueryPtr)->and_query(numeric_link_greater<Float, float, jfloat>(tbl, arr[arr_len-1], value));
        }
    } CATCH_STD()
    RELEASE_ARRAY();
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeGreaterEqual__J_3JF(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlongArray columnIndexes, jfloat value)
{
    GET_ARRAY()
    try {
        if (arr_len == 1) {
            if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, arr[0], type_Float))
                return;
            Q(nativeQueryPtr)->greater_equal(S(arr[0]), static_cast<float>(value));
        }
        else {
            TableRef tbl = getTableLink(nativeQueryPtr, arr, arr_len);
            Q(nativeQueryPtr)->and_query(numeric_link_greaterequal<Float, float, jfloat>(tbl, arr[arr_len-1], value));
        }
    } CATCH_STD()
    RELEASE_ARRAY();
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeLess__J_3JF(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlongArray columnIndexes, jfloat value)
{
    GET_ARRAY()
    try {
        if (arr_len == 1) {
            if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, arr[0], type_Float))
                return;
            Q(nativeQueryPtr)->less(S(arr[0]), static_cast<float>(value));
        }
        else {
            TableRef tbl = getTableLink(nativeQueryPtr, arr, arr_len);
            Q(nativeQueryPtr)->and_query(numeric_link_less<Float, float, jfloat>(tbl, arr[arr_len-1], value));
        }
    } CATCH_STD()
    RELEASE_ARRAY();
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeLessEqual__J_3JF(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlongArray columnIndexes, jfloat value)
{
    GET_ARRAY()
    try {
        if (arr_len == 1) {
            if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, arr[0], type_Float))
                return;
            Q(nativeQueryPtr)->less_equal(S(arr[0]), static_cast<float>(value));
        }
        else {
            TableRef tbl = getTableLink(nativeQueryPtr, arr, arr_len);
            Q(nativeQueryPtr)->and_query(numeric_link_lessequal<Float, float, jfloat>(tbl, arr[arr_len-1], value));
        }
    } CATCH_STD()
    RELEASE_ARRAY();
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeBetween__J_3JFF(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlongArray columnIndexes, jfloat value1, jfloat value2)
{
    GET_ARRAY()
    try {
        if (arr_len == 1) {
            if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, arr[0], type_Float))
                return;
            Q(nativeQueryPtr)->between(S(arr[0]), static_cast<float>(value1), static_cast<float>(value2));
        }
        else {
            Q(nativeQueryPtr)->group();
            TableRef tbl = getTableLink(nativeQueryPtr, arr, arr_len);
            Q(nativeQueryPtr)->and_query(numeric_link_greaterequal<Float, float, jfloat>(tbl, arr[arr_len-1], value1));
            tbl = getTableLink(nativeQueryPtr, arr, arr_len);
            Q(nativeQueryPtr)->and_query(numeric_link_lessequal<Float, float, jfloat>(tbl, arr[arr_len-1], value2));
            Q(nativeQueryPtr)->end_group();
        }
    } CATCH_STD()
    RELEASE_ARRAY();
}


// Double

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeEqual__J_3JD(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlongArray columnIndexes, jdouble value)
{
    GET_ARRAY()
    try {
        if (arr_len == 1) {
            if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, arr[0], type_Double))
                return;
            Q(nativeQueryPtr)->equal(S(arr[0]), static_cast<double>(value));
        }
        else {
            TableRef tbl = getTableLink(nativeQueryPtr, arr, arr_len);
            Q(nativeQueryPtr)->and_query(numeric_link_equal<Double, double, jdouble>(tbl, arr[arr_len-1], value));
        }
    } CATCH_STD()
    RELEASE_ARRAY()
}

JNIEXPORT void JNICALL JNICALL Java_io_realm_internal_TableQuery_nativeNotEqual__J_3JD(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlongArray columnIndexes, jdouble value)
{
    GET_ARRAY()
    try {
        if (arr_len == 1) {
            if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, arr[0], type_Double))
                return;
            Q(nativeQueryPtr)->not_equal(S(arr[0]), static_cast<double>(value));
        }
        else {
            TableRef tbl = getTableLink(nativeQueryPtr, arr, arr_len);
            Q(nativeQueryPtr)->and_query(numeric_link_notequal<Double, double, jdouble>(tbl, arr[arr_len-1], value));
        }
    } CATCH_STD()
    RELEASE_ARRAY();
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeGreater__J_3JD(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlongArray columnIndexes, jdouble value)
{
    GET_ARRAY()
    try {
        if (arr_len == 1) {
            if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, arr[0], type_Double))
                return;
            Q(nativeQueryPtr)->greater(S(arr[0]), static_cast<double>(value));
        }
        else {
            TableRef tbl = getTableLink(nativeQueryPtr, arr, arr_len);
            Q(nativeQueryPtr)->and_query(numeric_link_greater<Double, double, jdouble>(tbl, arr[arr_len-1], value));
        }
    } CATCH_STD()
    RELEASE_ARRAY();
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeGreaterEqual__J_3JD(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlongArray columnIndexes, jdouble value)
{
    GET_ARRAY()
    try {
        if (arr_len == 1) {
            if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, arr[0], type_Double))
                return;
            Q(nativeQueryPtr)->greater_equal(S(arr[0]), static_cast<double>(value));
        }
        else {
            TableRef tbl = getTableLink(nativeQueryPtr, arr, arr_len);
            Q(nativeQueryPtr)->and_query(numeric_link_greaterequal<Double, double, jdouble>(tbl, arr[arr_len-1], value));
        }
    } CATCH_STD()
    RELEASE_ARRAY();
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeLess__J_3JD(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlongArray columnIndexes, jdouble value)
{
    GET_ARRAY()
    try {
        if (arr_len == 1) {
            if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, arr[0], type_Double))
                return;
            Q(nativeQueryPtr)->less(S(arr[0]), static_cast<double>(value));
        }
        else {
            TableRef tbl = getTableLink(nativeQueryPtr, arr, arr_len);
            Q(nativeQueryPtr)->and_query(numeric_link_less<Double, double, jdouble>(tbl, arr[arr_len-1], value));
        }
    } CATCH_STD()
    RELEASE_ARRAY();
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeLessEqual__J_3JD(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlongArray columnIndexes, jdouble value)
{
    GET_ARRAY()
    try {
        if (arr_len == 1) {
            if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, arr[0], type_Double))
                return;
            Q(nativeQueryPtr)->less_equal(S(arr[0]), static_cast<double>(value));
        }
        else {
            TableRef tbl = getTableLink(nativeQueryPtr, arr, arr_len);
            Q(nativeQueryPtr)->and_query(numeric_link_lessequal<Double, double, jdouble>(tbl, arr[arr_len-1], value));
        }
    } CATCH_STD()
    RELEASE_ARRAY();
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeBetween__J_3JDD(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlongArray columnIndexes, jdouble value1, jdouble value2)
{
    GET_ARRAY()
    try {
        if (arr_len == 1) {
            if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, arr[0], type_Double))
                return;
            Q(nativeQueryPtr)->between(S(arr[0]), static_cast<double>(value1), static_cast<double>(value2));
        }
        else {
            Q(nativeQueryPtr)->group();
            TableRef tbl = getTableLink(nativeQueryPtr, arr, arr_len);
            Q(nativeQueryPtr)->and_query(numeric_link_greaterequal<Double, double, jdouble>(tbl, arr[arr_len-1], value1));
            tbl = getTableLink(nativeQueryPtr, arr, arr_len);
            Q(nativeQueryPtr)->and_query(numeric_link_lessequal<Double, double, jdouble>(tbl, arr[arr_len-1], value2));
            Q(nativeQueryPtr)->end_group();
        }
    } CATCH_STD()
    RELEASE_ARRAY();
}


// DateTime

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeEqualDateTime(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlongArray columnIndexes, jlong value)
{
    GET_ARRAY()
    try {
        if (arr_len == 1) {
            if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, arr[0], type_DateTime))
                return;
            Q(nativeQueryPtr)->less_equal_datetime(S(arr[0]), DateTime(static_cast<time_t>(value)));
        }
        else {
            TableRef tbl = getTableLink(nativeQueryPtr, arr, arr_len);
            Q(nativeQueryPtr)->and_query(numeric_link_equal<Int, int64_t, jlong>(tbl, arr[arr_len-1], value));
        }
    } CATCH_STD()
    RELEASE_ARRAY()
}

JNIEXPORT void JNICALL JNICALL Java_io_realm_internal_TableQuery_nativeNotEqualDateTime(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlongArray columnIndexes, jlong value)
{
    GET_ARRAY()
    try {
        if (arr_len == 1) {
            if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, arr[0], type_DateTime))
                return;
            Q(nativeQueryPtr)->not_equal_datetime(S(arr[0]), DateTime(static_cast<time_t>(value)));
        }
        else {
            TableRef tbl = getTableLink(nativeQueryPtr, arr, arr_len);
            Q(nativeQueryPtr)->and_query(numeric_link_notequal<Int, int64_t, jlong>(tbl, arr[arr_len-1], value));
        }
    } CATCH_STD()
    RELEASE_ARRAY()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeGreaterDateTime(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlongArray columnIndexes, jlong value)
{
    GET_ARRAY()
    try {
        if (arr_len == 1) {
            if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, arr[0], type_DateTime))
                return;
            Q(nativeQueryPtr)->greater_datetime(S(arr[0]), DateTime(static_cast<time_t>(value)));
        }
        else {
            TableRef tbl = getTableLink(nativeQueryPtr, arr, arr_len);
            Q(nativeQueryPtr)->and_query(numeric_link_greater<Int, int64_t, jlong>(tbl, arr[arr_len-1], value));
        }
    } CATCH_STD()
    RELEASE_ARRAY()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeGreaterEqualDateTime(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlongArray columnIndexes, jlong value)
{
    GET_ARRAY()
    try {
        if (arr_len == 1) {
            if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, arr[0], type_DateTime))
                return;
            Q(nativeQueryPtr)->greater_equal_datetime(S(arr[0]), DateTime(static_cast<time_t>(value)));
        }
        else {
            TableRef tbl = getTableLink(nativeQueryPtr, arr, arr_len);
            Q(nativeQueryPtr)->and_query(numeric_link_greaterequal<Int, int64_t, jlong>(tbl, arr[arr_len-1], value));
        }
    } CATCH_STD()
    RELEASE_ARRAY();
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeLessDateTime(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlongArray columnIndexes, jlong value)
{
    GET_ARRAY()
    try {
        if (arr_len == 1) {
            if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, arr[0], type_DateTime))
                return;
            Q(nativeQueryPtr)->less_datetime(S(arr[0]), DateTime(static_cast<time_t>(value)));
        }
        else {
            TableRef tbl = getTableLink(nativeQueryPtr, arr, arr_len);
            Q(nativeQueryPtr)->and_query(numeric_link_less<Int, int64_t, jlong>(tbl, arr[arr_len-1], value));
        }
    } CATCH_STD()
    RELEASE_ARRAY();
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeLessEqualDateTime(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlongArray columnIndexes, jlong value)
{
    GET_ARRAY()
    try {
        if (arr_len == 1) {
            if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, arr[0], type_DateTime))
                return;
            Q(nativeQueryPtr)->less_equal_datetime(S(arr[0]), DateTime(static_cast<time_t>(value)));
        }
        else {
            TableRef tbl = getTableLink(nativeQueryPtr, arr, arr_len);
            Q(nativeQueryPtr)->and_query(numeric_link_lessequal<Int, int64_t, jlong>(tbl, arr[arr_len-1], value));
        }
    } CATCH_STD()
    RELEASE_ARRAY()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeBetweenDateTime(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlongArray columnIndexes, jlong value1, jlong value2)
{
    GET_ARRAY()
    try {
        if (arr_len == 1) {
            if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, arr[0], type_DateTime))
                return;
            Q(nativeQueryPtr)->between_datetime(S(arr[0]), DateTime(static_cast<time_t>(value1)), DateTime(static_cast<time_t>(value2)));
        }
        else {
            Q(nativeQueryPtr)->group();
            TableRef tbl = getTableLink(nativeQueryPtr, arr, arr_len);
            Q(nativeQueryPtr)->and_query(numeric_link_greaterequal<Int, int64_t, jlong>(tbl, arr[arr_len-1], value1));
            tbl = getTableLink(nativeQueryPtr, arr, arr_len);
            Q(nativeQueryPtr)->and_query(numeric_link_lessequal<Int, int64_t, jlong>(tbl, arr[arr_len-1], value2));
            Q(nativeQueryPtr)->end_group();
        }
    } CATCH_STD()
    RELEASE_ARRAY()
}

// Bool

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeEqual__J_3JZ(
  JNIEnv* env, jobject, jlong nativeQueryPtr, jlongArray columnIndexes, jboolean value)
{
    GET_ARRAY()
    try {
        if (arr_len == 1) {
            if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, arr[0], type_Bool))
                return;
            Q(nativeQueryPtr)->equal(S(arr[0]), value != 0 ? true : false);
        }
        else {
            TableRef tbl = getTableLink(nativeQueryPtr, arr, arr_len);
            Q(nativeQueryPtr)->and_query(numeric_link_equal<Bool, bool, jboolean>(tbl, arr[arr_len-1], value));
        }
    } CATCH_STD()
    RELEASE_ARRAY()
}

// String

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeEqual__J_3JLjava_lang_String_2Z(
    JNIEnv *env, jobject, jlong nativeQueryPtr, jlongArray columnIndexes, jstring value, jboolean caseSensitive)
{
    GET_ARRAY()
    try {
        JStringAccessor value2(env, value); // throws
        if (arr_len == 1) {
            if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, arr[0], type_String))
                return;
            Q(nativeQueryPtr)->equal(S(arr[0]), value2, caseSensitive ? true : false);
        }
        else {
            TableRef tbl = getTableLink(nativeQueryPtr, arr, arr_len);
            Q(nativeQueryPtr)->and_query(tbl->column<String>(size_t(arr[arr_len-1])) == StringData(value2));
        }
    } CATCH_STD()
    RELEASE_ARRAY()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeNotEqual__J_3JLjava_lang_String_2Z(
    JNIEnv *env, jobject, jlong nativeQueryPtr, jlongArray columnIndexes, jstring value, jboolean caseSensitive)
{
    GET_ARRAY()
    try {
        JStringAccessor value2(env, value); // throws
        if (arr_len == 1) {
            if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, arr[0], type_String))
                return;
            Q(nativeQueryPtr)->not_equal(S(arr[0]), value2, caseSensitive ? true : false);
        }
        else {
            TableRef tbl = getTableLink(nativeQueryPtr, arr, arr_len);
            Q(nativeQueryPtr)->and_query(tbl->column<String>(size_t(arr[arr_len-1])) != StringData(value2));
        }
    } CATCH_STD()
    RELEASE_ARRAY()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeBeginsWith(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlong columnIndex, jstring value, jboolean caseSensitive)
{
    if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, columnIndex, type_String))
        return;
    try {
        JStringAccessor value2(env, value); // throws
        Q(nativeQueryPtr)->begins_with(S(columnIndex), value2, caseSensitive ? true : false);
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeEndsWith(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlong columnIndex, jstring value, jboolean caseSensitive)
{
    if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, columnIndex, type_String))
        return;
    try {
        JStringAccessor value2(env, value); // throws
        Q(nativeQueryPtr)->ends_with(S(columnIndex), value2, caseSensitive ? true : false);
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeContains(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlong columnIndex, jstring value, jboolean caseSensitive)
{
    if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, columnIndex, type_String))
        return;
    try {
        JStringAccessor value2(env, value); // throws
        Q(nativeQueryPtr)->contains(S(columnIndex), value2, caseSensitive ? true : false);
    } CATCH_STD()
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

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeSubtable(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlong columnIndex)
{
    TableQuery* pTQuery = TQ(nativeQueryPtr);
    if (!QUERY_VALID(env, pTQuery))
        return;

    try {
        TableRef pTable = pTQuery->get_current_table();
        pTQuery->push_subtable(S(columnIndex));
        if (!COL_INDEX_AND_TYPE_VALID(env, Ref2Ptr(pTable), columnIndex, type_Table))
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
    Table* pTable = Ref2Ptr(pQuery->get_table());
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

JNIEXPORT jlong JNICALL Java_io_realm_internal_TableQuery_nativeFindAll(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlong start, jlong end, jlong limit)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = Ref2Ptr(pQuery->get_table());
    if (!QUERY_VALID(env, pQuery) ||
        !ROW_INDEXES_VALID(env, pTable, start, end, limit))
        return -1;
    try {
        TableView* pResultView = new TableView( pQuery->find_all(S(start), S(end), S(limit)) );
        return reinterpret_cast<jlong>(pResultView);
    } CATCH_STD()
    return -1;
}


// Integer Aggregates

JNIEXPORT jlong JNICALL Java_io_realm_internal_TableQuery_nativeSumInt(
    JNIEnv* env, jobject, jlong nativeQueryPtr,
    jlong columnIndex, jlong start, jlong end, jlong limit)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = Ref2Ptr(pQuery->get_table());
    if (!QUERY_VALID(env, pQuery) ||
        !COL_INDEX_AND_TYPE_VALID(env, pTable, columnIndex, type_Int) ||
        !ROW_INDEXES_VALID(env, pTable, start, end, limit))
        return 0;
    try {
        return pQuery->sum_int(S(columnIndex), NULL, S(start), S(end), S(limit));
    } CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_TableQuery_nativeMaximumInt(
    JNIEnv* env, jobject, jlong nativeQueryPtr,
    jlong columnIndex, jlong start, jlong end, jlong limit)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = Ref2Ptr(pQuery->get_table());
    if (!QUERY_VALID(env, pQuery) ||
        !COL_INDEX_AND_TYPE_VALID(env, pTable, columnIndex, type_Int) ||
        !ROW_INDEXES_VALID(env, pTable, start, end, limit))
        return 0;
    try {
        return pQuery->maximum_int(S(columnIndex), NULL, S(start), S(end), S(limit));
    } CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_TableQuery_nativeMinimumInt(
    JNIEnv* env, jobject, jlong nativeQueryPtr,
    jlong columnIndex, jlong start, jlong end, jlong limit)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = Ref2Ptr(pQuery->get_table());
    if (!QUERY_VALID(env, pQuery) ||
        !COL_INDEX_AND_TYPE_VALID(env, pTable, columnIndex, type_Int) ||
        !ROW_INDEXES_VALID(env, pTable, start, end, limit))
        return 0;
    try {
        return pQuery->minimum_int(S(columnIndex), NULL, S(start), S(end), S(limit));
    } CATCH_STD()
    return 0;
}

JNIEXPORT jdouble JNICALL Java_io_realm_internal_TableQuery_nativeAverageInt(
    JNIEnv* env, jobject, jlong nativeQueryPtr,
    jlong columnIndex, jlong start, jlong end, jlong limit)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = Ref2Ptr(pQuery->get_table());
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
    Table* pTable = Ref2Ptr(pQuery->get_table());
    if (!QUERY_VALID(env, pQuery) ||
        !COL_INDEX_AND_TYPE_VALID(env, pTable, columnIndex, type_Float) ||
        !ROW_INDEXES_VALID(env, pTable, start, end, limit))
        return 0;
    try {
        return pQuery->sum_float(S(columnIndex), NULL, S(start), S(end), S(limit));
    } CATCH_STD()
    return 0;
}

JNIEXPORT jfloat JNICALL Java_io_realm_internal_TableQuery_nativeMaximumFloat(
    JNIEnv* env, jobject, jlong nativeQueryPtr,
    jlong columnIndex, jlong start, jlong end, jlong limit)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = Ref2Ptr(pQuery->get_table());
    if (!QUERY_VALID(env, pQuery) ||
        !COL_INDEX_AND_TYPE_VALID(env, pTable, columnIndex, type_Float) ||
        !ROW_INDEXES_VALID(env, pTable, start, end, limit))
        return 0;
    try {
        return pQuery->maximum_float(S(columnIndex), NULL, S(start), S(end), S(limit));
    } CATCH_STD()
    return 0;
}

JNIEXPORT jfloat JNICALL Java_io_realm_internal_TableQuery_nativeMinimumFloat(
    JNIEnv* env, jobject, jlong nativeQueryPtr,
    jlong columnIndex, jlong start, jlong end, jlong limit)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = Ref2Ptr(pQuery->get_table());
    if (!QUERY_VALID(env, pQuery) ||
        !COL_INDEX_AND_TYPE_VALID(env, pTable, columnIndex, type_Float) ||
        !ROW_INDEXES_VALID(env, pTable, start, end, limit))
        return 0;
    try {
        return pQuery->minimum_float(S(columnIndex), NULL, S(start), S(end), S(limit));
    } CATCH_STD()
    return 0;
}

JNIEXPORT jdouble JNICALL Java_io_realm_internal_TableQuery_nativeAverageFloat(
    JNIEnv* env, jobject, jlong nativeQueryPtr,
    jlong columnIndex, jlong start, jlong end, jlong limit)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = Ref2Ptr(pQuery->get_table());
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
    Table* pTable = Ref2Ptr(pQuery->get_table());
    if (!QUERY_VALID(env, pQuery) ||
        !COL_INDEX_AND_TYPE_VALID(env, pTable, columnIndex, type_Double) ||
        !ROW_INDEXES_VALID(env, pTable, start, end, limit))
        return 0;
    try {
        return pQuery->sum_double(S(columnIndex), NULL, S(start), S(end), S(limit));
    } CATCH_STD()
    return 0;
}

JNIEXPORT jdouble JNICALL Java_io_realm_internal_TableQuery_nativeMaximumDouble(
    JNIEnv* env, jobject, jlong nativeQueryPtr,
    jlong columnIndex, jlong start, jlong end, jlong limit)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = Ref2Ptr(pQuery->get_table());
    if (!QUERY_VALID(env, pQuery) ||
        !COL_INDEX_AND_TYPE_VALID(env, pTable, columnIndex, type_Double) ||
        !ROW_INDEXES_VALID(env, pTable, start, end, limit))
        return 0;
    try {
        return pQuery->maximum_double(S(columnIndex), NULL, S(start), S(end), S(limit));
    } CATCH_STD()
    return 0;
}

JNIEXPORT jdouble JNICALL Java_io_realm_internal_TableQuery_nativeMinimumDouble(
    JNIEnv* env, jobject, jlong nativeQueryPtr,
    jlong columnIndex, jlong start, jlong end, jlong limit)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = Ref2Ptr(pQuery->get_table());
    if (!QUERY_VALID(env, pQuery) ||
        !COL_INDEX_AND_TYPE_VALID(env, pTable, columnIndex, type_Double) ||
        !ROW_INDEXES_VALID(env, pTable, start, end, limit))
        return 0;
    try {
        return pQuery->minimum_double(S(columnIndex), NULL, S(start), S(end), S(limit));
    } CATCH_STD()
    return 0;
}

JNIEXPORT jdouble JNICALL Java_io_realm_internal_TableQuery_nativeAverageDouble(
    JNIEnv* env, jobject, jlong nativeQueryPtr,
    jlong columnIndex, jlong start, jlong end, jlong limit)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = Ref2Ptr(pQuery->get_table());
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

JNIEXPORT jlong JNICALL Java_io_realm_internal_TableQuery_nativeMaximumDate(
    JNIEnv* env, jobject, jlong nativeQueryPtr,
    jlong columnIndex, jlong start, jlong end, jlong limit)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = Ref2Ptr(pQuery->get_table());
    if (!QUERY_VALID(env, pQuery) ||
        !COL_INDEX_AND_TYPE_VALID(env, pTable, columnIndex, type_DateTime) ||
        !ROW_INDEXES_VALID(env, pTable, start, end, limit))
        return 0;
    try {
        // This exploits the fact that dates are stored as int in core
        return pQuery->maximum_int(S(columnIndex), NULL, S(start), S(end), S(limit));
    } CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_TableQuery_nativeMinimumDate(
    JNIEnv* env, jobject, jlong nativeQueryPtr,
    jlong columnIndex, jlong start, jlong end, jlong limit)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = Ref2Ptr(pQuery->get_table());
    if (!QUERY_VALID(env, pQuery) ||
        !COL_INDEX_AND_TYPE_VALID(env, pTable, columnIndex, type_DateTime) ||
        !ROW_INDEXES_VALID(env, pTable, start, end, limit))
        return 0;
    try {
        // This exploits the fact that dates are stored as int in core
        return pQuery->minimum_int(S(columnIndex), NULL, S(start), S(end), S(limit));
    } CATCH_STD()
    return 0;
}

// Count, Remove

JNIEXPORT jlong JNICALL Java_io_realm_internal_TableQuery_nativeCount(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlong start, jlong end, jlong limit)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = Ref2Ptr(pQuery->get_table());
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
    Table* pTable = Ref2Ptr(pQuery->get_table());
    if (!QUERY_VALID(env, pQuery) ||
        !ROW_INDEXES_VALID(env, pTable, start, end, limit))
        return 0;
    try {
        return pQuery->remove(S(start), S(end), S(limit));
    } CATCH_STD()
    return 0;
}
