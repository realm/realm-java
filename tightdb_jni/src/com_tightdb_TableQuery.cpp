#include "util.hpp"
#include "com_tightdb_TableQuery.h"

using namespace tightdb;

#if 1
#define COL_TYPE_VALID(env,ptr,col, type)   TBL_AND_COL_INDEX_AND_TYPE_VALID(env,ptr,col, type)
#define QUERY_VALID(env, pQuery)            QueryValid(env, pQuery)
#else
#define COL_TYPE_VALID(env,ptr,col, type)   true
#define QUERY_VALID(env, pQuery)            true
#endif

inline tightdb::Table* Ref2Ptr(tightdb::TableRef tableref)
{
    return &*tableref;
}

inline Table* get_table_ptr(Query* queryPtr)
{
    return Ref2Ptr( queryPtr->get_table() );
}

inline bool QueryValid(JNIEnv* env, Query* pQuery) 
{
    Table* pTable = get_table_ptr(pQuery);
    return TABLE_VALID(env, pTable);
}


//-------------------------------------------------------

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeClose(JNIEnv * env, jobject, jlong nativeQueryPtr) {
    TR((env, "Query nativeClose(ptr %x)\n", nativeQueryPtr));
    delete Q(nativeQueryPtr);
}

// Integer

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeEqual__JJJ(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlong columnIndex, jlong value)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = get_table_ptr(pQuery);
    if (!COL_TYPE_VALID(env, pTable, columnIndex, type_Int))
        return;
    try {
        pQuery->equal(S(columnIndex), value);
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeNotEqual__JJJ(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlong columnIndex, jlong value)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = get_table_ptr(pQuery);
    if (!COL_TYPE_VALID(env, pTable, columnIndex, type_Int))
        return;
    try {
        pQuery->not_equal(S(columnIndex), value);
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeGreater__JJJ(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlong columnIndex, jlong value)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = get_table_ptr(pQuery);
    if (!COL_TYPE_VALID(env, pTable, columnIndex, type_Int))
        return;
    try {
        pQuery->greater(S(columnIndex), value);
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeGreaterEqual__JJJ(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlong columnIndex, jlong value)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = get_table_ptr(pQuery);
    if (!COL_TYPE_VALID(env, pTable, columnIndex, type_Int))
        return;
    try {
        pQuery->greater_equal(S(columnIndex), value);
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeLess__JJJ(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlong columnIndex, jlong value)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = get_table_ptr(pQuery);
    if (!COL_TYPE_VALID(env, pTable, columnIndex, type_Int))
        return;
    try {
        pQuery->less(S(columnIndex), value);
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeLessEqual__JJJ(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlong columnIndex, jlong value)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = get_table_ptr(pQuery);
    if (!COL_TYPE_VALID(env, pTable, columnIndex, type_Int))
        return;
    try {
        pQuery->less_equal(S(columnIndex), value);
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeBetween__JJJJ(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlong columnIndex, jlong value1, jlong value2)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = get_table_ptr(pQuery);
    if (!COL_TYPE_VALID(env, pTable, columnIndex, type_Int))
        return;
    try {
        pQuery->between(S(columnIndex), value1, value2);
    } CATCH_STD()
}

// Float

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeEqual__JJF(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlong columnIndex, jfloat value)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = get_table_ptr(pQuery);
    if (!COL_TYPE_VALID(env, pTable, columnIndex, type_Float))
        return;
    try {
        pQuery->equal(S(columnIndex), value);
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeNotEqual__JJF(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlong columnIndex, jfloat value)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = get_table_ptr(pQuery);
    if (!COL_TYPE_VALID(env, pTable, columnIndex, type_Float))
        return;
    try {
        pQuery->not_equal(S(columnIndex), value);
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeGreater__JJF(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlong columnIndex, jfloat value)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = get_table_ptr(pQuery);
    if (!COL_TYPE_VALID(env, pTable, columnIndex, type_Float))
        return;
    try {
        pQuery->greater(S(columnIndex), value);
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeGreaterEqual__JJF(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlong columnIndex, jfloat value)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = get_table_ptr(pQuery);
    if (!COL_TYPE_VALID(env, pTable, columnIndex, type_Float))
        return;
    try {
        pQuery->greater_equal(S(columnIndex), value);
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeLess__JJF(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlong columnIndex, jfloat value)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = get_table_ptr(pQuery);
    if (!COL_TYPE_VALID(env, pTable, columnIndex, type_Float))
        return;
    try {
        pQuery->less(S(columnIndex), value);
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeLessEqual__JJF(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlong columnIndex, jfloat value)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = get_table_ptr(pQuery);
    if (!COL_TYPE_VALID(env, pTable, columnIndex, type_Float))
        return;
    try {
        pQuery->less_equal(S(columnIndex), value);
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeBetween__JJFF(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlong columnIndex, jfloat value1, jfloat value2)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = get_table_ptr(pQuery);
    if (!COL_TYPE_VALID(env, pTable, columnIndex, type_Float))
        return;
    try {
        pQuery->between(S(columnIndex), value1, value2);
    } CATCH_STD()
}

// Double

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeEqual__JJD(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlong columnIndex, jdouble value)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = get_table_ptr(pQuery);
    if (!COL_TYPE_VALID(env, pTable, columnIndex, type_Double))
        return;
    try {
        pQuery->equal(S(columnIndex), value);
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeNotEqual__JJD(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlong columnIndex, jdouble value)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = get_table_ptr(pQuery);
    if (!COL_TYPE_VALID(env, pTable, columnIndex, type_Double))
        return;
    try {
        pQuery->not_equal(S(columnIndex), value);
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeGreater__JJD(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlong columnIndex, jdouble value)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = get_table_ptr(pQuery);
    if (!COL_TYPE_VALID(env, pTable, columnIndex, type_Double))
        return;
    try {
        pQuery->greater(S(columnIndex), value);
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeGreaterEqual__JJD(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlong columnIndex, jdouble value)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = get_table_ptr(pQuery);
    if (!COL_TYPE_VALID(env, pTable, columnIndex, type_Double))
        return;
    try {
        pQuery->greater_equal(S(columnIndex), value);
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeLess__JJD(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlong columnIndex, jdouble value)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = get_table_ptr(pQuery);
    if (!COL_TYPE_VALID(env, pTable, columnIndex, type_Double))
        return;
    try {
        pQuery->less(S(columnIndex), value);
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeLessEqual__JJD(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlong columnIndex, jdouble value)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = get_table_ptr(pQuery);
    if (!COL_TYPE_VALID(env, pTable, columnIndex, type_Double))
        return;
    try {
        pQuery->less_equal(S(columnIndex), value);
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeBetween__JJDD(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlong columnIndex, jdouble value1, jdouble value2)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = get_table_ptr(pQuery);
    if (!COL_TYPE_VALID(env, pTable, columnIndex, type_Double))
        return;
    try {
        pQuery->between(S(columnIndex), value1, value2);
    } CATCH_STD()
}


// DateTime

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeEqualDateTime(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlong columnIndex, jlong value)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = get_table_ptr(pQuery);
    if (!COL_TYPE_VALID(env, pTable, columnIndex, type_DateTime))
        return;
    try {
        Q(nativeQueryPtr)->equal_datetime(S(columnIndex), DateTime(static_cast<time_t>(value)));
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeNotEqualDateTime(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlong columnIndex, jlong value)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = get_table_ptr(pQuery);
    if (!COL_TYPE_VALID(env, pTable, columnIndex, type_DateTime))
        return;
    try {
        Q(nativeQueryPtr)->not_equal_datetime(S(columnIndex), DateTime(static_cast<time_t>(value)));
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeGreaterDateTime(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlong columnIndex, jlong value)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = get_table_ptr(pQuery);
    if (!COL_TYPE_VALID(env, pTable, columnIndex, type_DateTime))
        return;
    try {
        Q(nativeQueryPtr)->greater_datetime(S(columnIndex), DateTime(static_cast<time_t>(value)));
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeGreaterEqualDateTime(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlong columnIndex, jlong value)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = get_table_ptr(pQuery);
    if (!COL_TYPE_VALID(env, pTable, columnIndex, type_DateTime))
        return;
    try {
        Q(nativeQueryPtr)->greater_equal_datetime(S(columnIndex), DateTime(static_cast<time_t>(value)));
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeLessDateTime(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlong columnIndex, jlong value)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = get_table_ptr(pQuery);
    if (!COL_TYPE_VALID(env, pTable, columnIndex, type_DateTime))
        return;
    try {
        Q(nativeQueryPtr)->less_datetime(S(columnIndex), DateTime(static_cast<time_t>(value)));
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeLessEqualDateTime(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlong columnIndex, jlong value)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = get_table_ptr(pQuery);
    if (!COL_TYPE_VALID(env, pTable, columnIndex, type_DateTime))
        return;
    try {
        Q(nativeQueryPtr)->less_equal_datetime(S(columnIndex), DateTime(static_cast<time_t>(value)));
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeBetweenDateTime(

    JNIEnv* env, jobject, jlong nativeQueryPtr, jlong columnIndex, jlong value1, jlong value2)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = get_table_ptr(pQuery);
    if (!COL_TYPE_VALID(env, pTable, columnIndex, type_DateTime))
        return;
    try {
        Q(nativeQueryPtr)->between_datetime(S(columnIndex), DateTime(static_cast<time_t>(value1)), DateTime(static_cast<time_t>(value2)));
    } CATCH_STD()
}


// Bool

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeEqual__JJZ(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlong columnIndex, jboolean value)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = get_table_ptr(pQuery);
    if (!COL_TYPE_VALID(env, pTable, columnIndex, type_Bool))
        return;
    try {
        pQuery->equal(S(columnIndex), value != 0 ? true : false);
    } CATCH_STD()
}

// String

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeEqual__JJLjava_lang_String_2Z(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlong columnIndex, jstring value, jboolean caseSensitive)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = get_table_ptr(pQuery);
    if (!COL_TYPE_VALID(env, pTable, columnIndex, type_String))
        return;
    JStringAccessor value2(env, value);
    if (!value2)
        return;
    try {
        pQuery->equal(S(columnIndex), value2, caseSensitive ? true : false);
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeBeginsWith(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlong columnIndex, jstring value, jboolean caseSensitive)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = get_table_ptr(pQuery);
    if (!COL_TYPE_VALID(env, pTable, columnIndex, type_String))
        return;
    JStringAccessor value2(env, value);
    if (!value2)
        return;
    try {
        pQuery->begins_with(S(columnIndex), value2, caseSensitive ? true : false);
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeEndsWith(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlong columnIndex, jstring value, jboolean caseSensitive)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = get_table_ptr(pQuery);
    if (!COL_TYPE_VALID(env, pTable, columnIndex, type_String))
        return;
    JStringAccessor value2(env, value);
    if (!value2)
        return;
    try {
        pQuery->ends_with(S(columnIndex), value2, caseSensitive ? true : false);
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeContains(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlong columnIndex, jstring value, jboolean caseSensitive)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = get_table_ptr(pQuery);
    if (!COL_TYPE_VALID(env, pTable, columnIndex, type_String))
        return;
    JStringAccessor value2(env, value);
    if (!value2)
        return;
    try {
        pQuery->contains(S(columnIndex), value2, caseSensitive ? true : false);
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeNotEqual__JJLjava_lang_String_2Z(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlong columnIndex, jstring value, jboolean caseSensitive)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = get_table_ptr(pQuery);
    if (!COL_TYPE_VALID(env, pTable, columnIndex, type_String))
        return;
    JStringAccessor value2(env, value);
    if (!value2)
        return;
    try {
        pQuery->not_equal(S(columnIndex), value2, caseSensitive ? true : false);
    } CATCH_STD()
}


JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeSubTable(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlong columnIndex)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = get_table_ptr(pQuery);
    if (!QUERY_VALID(env, pQuery) ||
        !COL_INDEX_AND_TYPE_VALID(env, pTable, columnIndex, type_Table))
        return;
    try {
        pQuery->subtable(S(columnIndex));
    } CATCH_STD()
}

// General ----------------------------------------------------
// TODO:
// Some of these methods may not need the check for Table/Query validity,
// as they are called for each method when building up the query.
// Consider to reduce to just the "action" methods on Query

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeTableview(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlong nativeTableViewPtr)
{
    Query* pQuery = Q(nativeQueryPtr);
    if (!QUERY_VALID(env, pQuery))
        return;
    try {
        pQuery->tableview(*TV(nativeTableViewPtr));
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeGroup(
    JNIEnv* env, jobject, jlong nativeQueryPtr)
{
    Query* pQuery = Q(nativeQueryPtr);
    if (!QUERY_VALID(env, pQuery))
        return;
    try {
        pQuery->group();
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeEndGroup(
    JNIEnv* env, jobject, jlong nativeQueryPtr)
{
    Query* pQuery = Q(nativeQueryPtr);
    if (!QUERY_VALID(env, pQuery))
        return;
    try {
        pQuery->end_group();
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeParent(
    JNIEnv* env, jobject, jlong nativeQueryPtr)
{
    // No verification of parameters needed?
    Query* pQuery = Q(nativeQueryPtr);
    if (!QUERY_VALID(env, pQuery))
        return;
    try {
        pQuery->end_subtable();
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeOr(
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


// Find --------------------------------------


JNIEXPORT jlong JNICALL Java_com_tightdb_TableQuery_nativeFindNext(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlong lastMatch)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = get_table_ptr(pQuery);
// TODO: check ranges for lastmatch is correct - being changed now... 08-09-13
    if (!QUERY_VALID(env, pQuery) ||
        !ROW_INDEX_VALID(env, pTable, lastMatch))
        return 0;
    try {
        return pQuery->find_next(S(lastMatch));
    } CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_com_tightdb_TableQuery_nativeFindAll(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlong start, jlong end, jlong limit)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = get_table_ptr(pQuery);
    if (!QUERY_VALID(env, pQuery) ||
        !ROW_INDEXES_VALID(env, pTable, start, end, limit))
        return 0;
    try {
        TableView* pResultView = new TableView( pQuery->find_all(S(start), S(end), S(limit)) );
        return reinterpret_cast<jlong>(pResultView);
    } CATCH_STD()
    return 0;
}


// Integer Aggregates

JNIEXPORT jlong JNICALL Java_com_tightdb_TableQuery_nativeSumInt(
    JNIEnv* env, jobject, jlong nativeQueryPtr,
    jlong columnIndex, jlong start, jlong end, jlong limit)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = get_table_ptr(pQuery);
    if (!QUERY_VALID(env, pQuery) ||
        !COL_INDEX_AND_TYPE_VALID(env, pTable, columnIndex, type_Int) ||
        !ROW_INDEXES_VALID(env, pTable, start, end, limit))
        return 0;
    try {
        return pQuery->sum_int(S(columnIndex), NULL, S(start), S(end), S(limit));
    } CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_com_tightdb_TableQuery_nativeMaximumInt(
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

JNIEXPORT jlong JNICALL Java_com_tightdb_TableQuery_nativeMinimumInt(
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

JNIEXPORT jdouble JNICALL Java_com_tightdb_TableQuery_nativeAverageInt(
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

JNIEXPORT jdouble JNICALL Java_com_tightdb_TableQuery_nativeSumFloat(
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

JNIEXPORT jfloat JNICALL Java_com_tightdb_TableQuery_nativeMaximumFloat(
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

JNIEXPORT jfloat JNICALL Java_com_tightdb_TableQuery_nativeMinimumFloat(
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

JNIEXPORT jdouble JNICALL Java_com_tightdb_TableQuery_nativeAverageFloat(
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

JNIEXPORT jdouble JNICALL Java_com_tightdb_TableQuery_nativeSumDouble(
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

JNIEXPORT jdouble JNICALL Java_com_tightdb_TableQuery_nativeMaximumDouble(
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

JNIEXPORT jdouble JNICALL Java_com_tightdb_TableQuery_nativeMinimumDouble(
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

JNIEXPORT jdouble JNICALL Java_com_tightdb_TableQuery_nativeAverageDouble(
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


// Count, Remove

JNIEXPORT jlong JNICALL Java_com_tightdb_TableQuery_nativeCount(
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

JNIEXPORT jlong JNICALL Java_com_tightdb_TableQuery_nativeRemove(
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
