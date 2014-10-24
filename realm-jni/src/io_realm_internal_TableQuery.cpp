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


// Integer

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeEqual__JJJ(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlong columnIndex, jlong value)
{
    if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, columnIndex, type_Int))
        return;
    try {
        Q(nativeQueryPtr)->equal(S(columnIndex), static_cast<int64_t>(value));
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeNotEqual__JJJ(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlong columnIndex, jlong value)
{
    if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, columnIndex, type_Int))
        return;
    try {
        Q(nativeQueryPtr)->not_equal(S(columnIndex), static_cast<int64_t>(value));
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeGreater__JJJ(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlong columnIndex, jlong value)
{
    if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, columnIndex, type_Int))
        return;
    try {
        Q(nativeQueryPtr)->greater(S(columnIndex), static_cast<int64_t>(value));
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeGreaterEqual__JJJ(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlong columnIndex, jlong value)
{
    if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, columnIndex, type_Int))
        return;
    try {
        Q(nativeQueryPtr)->greater_equal(S(columnIndex), static_cast<int64_t>(value));
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeLess__JJJ(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlong columnIndex, jlong value)
{
    if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, columnIndex, type_Int))
        return;
    try {
        Q(nativeQueryPtr)->less(S(columnIndex), static_cast<int64_t>(value));
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeLessEqual__JJJ(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlong columnIndex, jlong value)
{
    if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, columnIndex, type_Int))
        return;
    try {
        Q(nativeQueryPtr)->less_equal(S(columnIndex), static_cast<int64_t>(value));
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeBetween__JJJJ(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlong columnIndex, jlong value1, jlong value2)
{
    if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, columnIndex, type_Int))
        return;
    try {
        Q(nativeQueryPtr)->between(S(columnIndex), static_cast<int64_t>(value1), static_cast<int64_t>(value2));
    } CATCH_STD()
}

// Float

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeEqual__JJF(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlong columnIndex, jfloat value)
{
    if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, columnIndex, type_Float))
        return;
    try {
        Q(nativeQueryPtr)->equal(S(columnIndex), value);
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeNotEqual__JJF(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlong columnIndex, jfloat value)
{
    if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, columnIndex, type_Float))
        return;
    try {
        Q(nativeQueryPtr)->not_equal(S(columnIndex), value);
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeGreater__JJF(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlong columnIndex, jfloat value)
{
    if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, columnIndex, type_Float))
        return;
    try {
        Q(nativeQueryPtr)->greater(S(columnIndex), value);
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeGreaterEqual__JJF(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlong columnIndex, jfloat value)
{
    if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, columnIndex, type_Float))
        return;
    try {
        Q(nativeQueryPtr)->greater_equal(S(columnIndex), value);
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeLess__JJF(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlong columnIndex, jfloat value)
{
    if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, columnIndex, type_Float))
        return;
    try {
        Q(nativeQueryPtr)->less(S(columnIndex), value);
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeLessEqual__JJF(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlong columnIndex, jfloat value)
{
    if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, columnIndex, type_Float))
        return;
    try {
        Q(nativeQueryPtr)->less_equal(S(columnIndex), value);
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeBetween__JJFF(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlong columnIndex, jfloat value1, jfloat value2)
{
    if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, columnIndex, type_Float))
        return;
    try {
        Q(nativeQueryPtr)->between(S(columnIndex), value1, value2);
    } CATCH_STD()
}

// Double

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeEqual__JJD(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlong columnIndex, jdouble value)
{
    if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, columnIndex, type_Double))
        return;
    try {
        Q(nativeQueryPtr)->equal(S(columnIndex), value);
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeNotEqual__JJD(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlong columnIndex, jdouble value)
{
    if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, columnIndex, type_Double))
        return;
    try {
        Q(nativeQueryPtr)->not_equal(S(columnIndex), value);
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeGreater__JJD(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlong columnIndex, jdouble value)
{
    if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, columnIndex, type_Double))
        return;
    try {
        Q(nativeQueryPtr)->greater(S(columnIndex), value);
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeGreaterEqual__JJD(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlong columnIndex, jdouble value)
{
    if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, columnIndex, type_Double))
        return;
    try {
        Q(nativeQueryPtr)->greater_equal(S(columnIndex), value);
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeLess__JJD(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlong columnIndex, jdouble value)
{
    if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, columnIndex, type_Double))
        return;
    try {
        Q(nativeQueryPtr)->less(S(columnIndex), value);
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeLessEqual__JJD(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlong columnIndex, jdouble value)
{
    if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, columnIndex, type_Double))
        return;
    try {
        Q(nativeQueryPtr)->less_equal(S(columnIndex), value);
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeBetween__JJDD(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlong columnIndex, jdouble value1, jdouble value2)
{
    if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, columnIndex, type_Double))
        return;
    try {
        Q(nativeQueryPtr)->between(S(columnIndex), value1, value2);
    } CATCH_STD()
}


// DateTime

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeEqualDateTime(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlong columnIndex, jlong value)
{
    if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, columnIndex, type_DateTime))
        return;
    try {
        Q(nativeQueryPtr)->equal_datetime(S(columnIndex), DateTime(static_cast<time_t>(value)));
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeNotEqualDateTime(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlong columnIndex, jlong value)
{
    if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, columnIndex, type_DateTime))
        return;
    try {
        Q(nativeQueryPtr)->not_equal_datetime(S(columnIndex), DateTime(static_cast<time_t>(value)));
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeGreaterDateTime(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlong columnIndex, jlong value)
{
    if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, columnIndex, type_DateTime))
        return;
    try {
        Q(nativeQueryPtr)->greater_datetime(S(columnIndex), DateTime(static_cast<time_t>(value)));
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeGreaterEqualDateTime(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlong columnIndex, jlong value)
{
    if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, columnIndex, type_DateTime))
        return;
    try {
        Q(nativeQueryPtr)->greater_equal_datetime(S(columnIndex), DateTime(static_cast<time_t>(value)));
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeLessDateTime(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlong columnIndex, jlong value)
{
    if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, columnIndex, type_DateTime))
        return;
    try {
        Q(nativeQueryPtr)->less_datetime(S(columnIndex), DateTime(static_cast<time_t>(value)));
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeLessEqualDateTime(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlong columnIndex, jlong value)
{
    if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, columnIndex, type_DateTime))
        return;
    try {
        Q(nativeQueryPtr)->less_equal_datetime(S(columnIndex), DateTime(static_cast<time_t>(value)));
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeBetweenDateTime(

    JNIEnv* env, jobject, jlong nativeQueryPtr, jlong columnIndex, jlong value1, jlong value2)
{
    if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, columnIndex, type_DateTime))
        return;
    try {
        Q(nativeQueryPtr)->between_datetime(S(columnIndex), DateTime(static_cast<time_t>(value1)), DateTime(static_cast<time_t>(value2)));
    } CATCH_STD()
}


// Bool

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeEqual__JJZ(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlong columnIndex, jboolean value)
{
    if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, columnIndex, type_Bool))
        return;
    try {
        Q(nativeQueryPtr)->equal(S(columnIndex), value != 0 ? true : false);
    } CATCH_STD()
}

// String

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeEqual__JJLjava_lang_String_2Z(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlong columnIndex, jstring value, jboolean caseSensitive)
{
    if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, columnIndex, type_String))
        return;
    try {
        JStringAccessor value2(env, value); // throws
        Q(nativeQueryPtr)->equal(S(columnIndex), value2, caseSensitive ? true : false);
    } CATCH_STD()
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

JNIEXPORT void JNICALL Java_io_realm_internal_TableQuery_nativeNotEqual__JJLjava_lang_String_2Z(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlong columnIndex, jstring value, jboolean caseSensitive)
{
    if (!QUERY_COL_TYPE_VALID(env, nativeQueryPtr, columnIndex, type_String))
        return;
    try {
        JStringAccessor value2(env, value); // throws
        Q(nativeQueryPtr)->not_equal(S(columnIndex), value2, caseSensitive ? true : false);
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
