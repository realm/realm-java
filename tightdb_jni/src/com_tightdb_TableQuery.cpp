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

inline Table* Ref2Ptr(TableRef tableref)
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
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = get_table_ptr(pQuery);
    if (TABLE_VALID(env, pTable))
        return;

    delete pQuery;
}

// Integer

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeEqual__JJJ(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlong columnIndex, jlong value)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = get_table_ptr(pQuery);
    if (!COL_TYPE_VALID(env, pTable, columnIndex, type_Int))
        return;

    pQuery->equal(S(columnIndex), value);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeNotEqual__JJJ(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlong columnIndex, jlong value)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = get_table_ptr(pQuery);
    if (!COL_TYPE_VALID(env, pTable, columnIndex, type_Int))
        return;

    pQuery->not_equal(S(columnIndex), value);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeGreater__JJJ(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlong columnIndex, jlong value)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = get_table_ptr(pQuery);
    if (!COL_TYPE_VALID(env, pTable, columnIndex, type_Int))
        return;

    pQuery->greater(S(columnIndex), value);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeGreaterEqual__JJJ(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlong columnIndex, jlong value)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = get_table_ptr(pQuery);
    if (!COL_TYPE_VALID(env, pTable, columnIndex, type_Int))
        return;

    pQuery->greater_equal(S(columnIndex), value);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeLess__JJJ(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlong columnIndex, jlong value)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = get_table_ptr(pQuery);
    if (!COL_TYPE_VALID(env, pTable, columnIndex, type_Int))
        return;

    pQuery->less(S(columnIndex), value);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeLessEqual__JJJ(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlong columnIndex, jlong value)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = get_table_ptr(pQuery);
    if (!COL_TYPE_VALID(env, pTable, columnIndex, type_Int))
        return;

    pQuery->less_equal(S(columnIndex), value);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeBetween__JJJJ(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlong columnIndex, jlong value1, jlong value2)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = get_table_ptr(pQuery);
    if (!COL_TYPE_VALID(env, pTable, columnIndex, type_Int))
        return;

    pQuery->between(S(columnIndex), value1, value2);
}

// Float

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeEqual__JJF(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlong columnIndex, jfloat value)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = get_table_ptr(pQuery);
    if (!COL_TYPE_VALID(env, pTable, columnIndex, type_Float))
        return;

    pQuery->equal(S(columnIndex), value);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeNotEqual__JJF(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlong columnIndex, jfloat value)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = get_table_ptr(pQuery);
    if (!COL_TYPE_VALID(env, pTable, columnIndex, type_Float))
        return;

    pQuery->not_equal(S(columnIndex), value);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeGreater__JJF(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlong columnIndex, jfloat value)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = get_table_ptr(pQuery);
    if (!COL_TYPE_VALID(env, pTable, columnIndex, type_Float))
        return;

    pQuery->greater(S(columnIndex), value);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeGreaterEqual__JJF(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlong columnIndex, jfloat value)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = get_table_ptr(pQuery);
    if (!COL_TYPE_VALID(env, pTable, columnIndex, type_Float))
        return;

    pQuery->greater_equal(S(columnIndex), value);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeLess__JJF(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlong columnIndex, jfloat value)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = get_table_ptr(pQuery);
    if (!COL_TYPE_VALID(env, pTable, columnIndex, type_Float))
        return;

    pQuery->less(S(columnIndex), value);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeLessEqual__JJF(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlong columnIndex, jfloat value)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = get_table_ptr(pQuery);
    if (!COL_TYPE_VALID(env, pTable, columnIndex, type_Float))
        return;

    pQuery->less_equal(S(columnIndex), value);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeBetween__JJFF(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlong columnIndex, jfloat value1, jfloat value2)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = get_table_ptr(pQuery);
    if (!COL_TYPE_VALID(env, pTable, columnIndex, type_Float))
        return;

    pQuery->between(S(columnIndex), value1, value2);
}

// Double

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeEqual__JJD(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlong columnIndex, jdouble value)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = get_table_ptr(pQuery);
    if (!COL_TYPE_VALID(env, pTable, columnIndex, type_Double))
        return;

    pQuery->equal(S(columnIndex), value);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeNotEqual__JJD(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlong columnIndex, jdouble value)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = get_table_ptr(pQuery);
    if (!COL_TYPE_VALID(env, pTable, columnIndex, type_Double))
        return;

    pQuery->not_equal(S(columnIndex), value);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeGreater__JJD(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlong columnIndex, jdouble value)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = get_table_ptr(pQuery);
    if (!COL_TYPE_VALID(env, pTable, columnIndex, type_Double))
        return;

    pQuery->greater(S(columnIndex), value);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeGreaterEqual__JJD(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlong columnIndex, jdouble value)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = get_table_ptr(pQuery);
    if (!COL_TYPE_VALID(env, pTable, columnIndex, type_Double))
        return;

    pQuery->greater_equal(S(columnIndex), value);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeLess__JJD(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlong columnIndex, jdouble value)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = get_table_ptr(pQuery);
    if (!COL_TYPE_VALID(env, pTable, columnIndex, type_Double))
        return;

    pQuery->less(S(columnIndex), value);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeLessEqual__JJD(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlong columnIndex, jdouble value)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = get_table_ptr(pQuery);
    if (!COL_TYPE_VALID(env, pTable, columnIndex, type_Double))
        return;

    pQuery->less_equal(S(columnIndex), value);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeBetween__JJDD(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlong columnIndex, jdouble value1, jdouble value2)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = get_table_ptr(pQuery);
    if (!COL_TYPE_VALID(env, pTable, columnIndex, type_Double))
        return;

    pQuery->between(S(columnIndex), value1, value2);
}

// Bool

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeEqual__JJZ(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlong columnIndex, jboolean value)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = get_table_ptr(pQuery);
    if (!COL_TYPE_VALID(env, pTable, columnIndex, type_Bool))
        return;

    pQuery->equal(S(columnIndex), value != 0 ? true : false);
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

    pQuery->equal(S(columnIndex), value2, caseSensitive ? true : false);
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

    pQuery->begins_with(S(columnIndex), value2, caseSensitive ? true : false);
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

    pQuery->ends_with(S(columnIndex), value2, caseSensitive ? true : false);
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

    pQuery->contains(S(columnIndex), value2, caseSensitive ? true : false);
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

    pQuery->not_equal(S(columnIndex), value2, caseSensitive ? true : false);
}


JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeSubTable(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlong columnIndex)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = get_table_ptr(pQuery);
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, pTable, columnIndex, type_Table))
        return;

    pQuery->subtable(S(columnIndex));
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

    pQuery->tableview(*TV(nativeTableViewPtr));
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeGroup(
    JNIEnv* env, jobject, jlong nativeQueryPtr)
{
    Query* pQuery = Q(nativeQueryPtr);
    if (!QUERY_VALID(env, pQuery))
        return;

    pQuery->group();
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeEndGroup(
    JNIEnv* env, jobject, jlong nativeQueryPtr)
{
    Query* pQuery = Q(nativeQueryPtr);
    if (!QUERY_VALID(env, pQuery))
        return;

    pQuery->end_group();
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeParent(
    JNIEnv* env, jobject, jlong nativeQueryPtr)
{
    // No verification of parameters needed?
    Query* pQuery = Q(nativeQueryPtr);
    if (!QUERY_VALID(env, pQuery))
        return;

    pQuery->end_subtable();
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeOr(
    JNIEnv* env, jobject, jlong nativeQueryPtr)
{
    // No verification of parameters needed?
    Query* pQuery = Q(nativeQueryPtr);
    if (!QUERY_VALID(env, pQuery))
        return;

    pQuery->Or();
}


// Find --------------------------------------


JNIEXPORT jlong JNICALL Java_com_tightdb_TableQuery_nativeFindNext(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlong lastMatch)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = get_table_ptr(pQuery);
// TODO: check ranges for lastmatch is correct - being changed now... 08-09-13
    if (!TBL_AND_ROW_INDEX_VALID(env, pTable, lastMatch))
        return 0;

    return pQuery->find_next(S(lastMatch));
}

JNIEXPORT jlong JNICALL Java_com_tightdb_TableQuery_nativeFindAll(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlong start, jlong end, jlong limit)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = get_table_ptr(pQuery);
    if (!TABLE_VALID(env, pTable) ||
        !ROW_INDEXES_VALID(env, pTable, start, end, limit))
        return 0;

    TableView* pResultView = new TableView( pQuery->find_all(S(start), S(end), S(limit)) );
    return reinterpret_cast<jlong>(pResultView);
}


// Integer Aggregates

JNIEXPORT jlong JNICALL Java_com_tightdb_TableQuery_nativeSum(
    JNIEnv* env, jobject, jlong nativeQueryPtr,
    jlong columnIndex, jlong start, jlong end, jlong limit)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = get_table_ptr(pQuery);
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, pTable, columnIndex, type_Int) ||
        !ROW_INDEXES_VALID(env, pTable, start, end, limit))
        return 0;

    return pQuery->sum(S(columnIndex), NULL, S(start), S(end), S(limit));
}

JNIEXPORT jlong JNICALL Java_com_tightdb_TableQuery_nativeMaximum(
    JNIEnv* env, jobject, jlong nativeQueryPtr,
    jlong columnIndex, jlong start, jlong end, jlong limit)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = Ref2Ptr(pQuery->get_table());
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, pTable, columnIndex, type_Int) ||
        !ROW_INDEXES_VALID(env, pTable, start, end, limit))
        return 0;

    return pQuery->maximum(S(columnIndex), NULL, S(start), S(end), S(limit));
}

JNIEXPORT jlong JNICALL Java_com_tightdb_TableQuery_nativeMinimum(
    JNIEnv* env, jobject, jlong nativeQueryPtr,
    jlong columnIndex, jlong start, jlong end, jlong limit)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = Ref2Ptr(pQuery->get_table());
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, pTable, columnIndex, type_Int) ||
        !ROW_INDEXES_VALID(env, pTable, start, end, limit))
        return 0;

    return pQuery->minimum(S(columnIndex), NULL, S(start), S(end), S(limit));
}

JNIEXPORT jdouble JNICALL Java_com_tightdb_TableQuery_nativeAverage(
    JNIEnv* env, jobject, jlong nativeQueryPtr,
    jlong columnIndex, jlong start, jlong end, jlong limit)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = Ref2Ptr(pQuery->get_table());
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, pTable, columnIndex, type_Int) ||
        !ROW_INDEXES_VALID(env, pTable, start, end, limit))
        return 0;

    size_t resultcount;
//TODO: return resultcount?
    double avg = pQuery->average(S(columnIndex), &resultcount, S(start), S(end), S(limit));
    //fprintf(stderr, "!!!Average(%d, %d) = %f (%d results)\n", start, end, avg, resultcount); fflush(stderr);
    return avg;
}

// float Aggregates

JNIEXPORT jdouble JNICALL Java_com_tightdb_TableQuery_nativeSumFloat(
    JNIEnv* env, jobject, jlong nativeQueryPtr,
    jlong columnIndex, jlong start, jlong end, jlong limit)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = Ref2Ptr(pQuery->get_table());
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, pTable, columnIndex, type_Float) ||
        !ROW_INDEXES_VALID(env, pTable, start, end, limit))
        return 0;

    return pQuery->sum_float(S(columnIndex), NULL, S(start), S(end), S(limit));
}

JNIEXPORT jfloat JNICALL Java_com_tightdb_TableQuery_nativeMaximumFloat(
    JNIEnv* env, jobject, jlong nativeQueryPtr,
    jlong columnIndex, jlong start, jlong end, jlong limit)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = Ref2Ptr(pQuery->get_table());
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, pTable, columnIndex, type_Float) ||
        !ROW_INDEXES_VALID(env, pTable, start, end, limit))
        return 0;

    return pQuery->maximum_float(S(columnIndex), NULL, S(start), S(end), S(limit));
}

JNIEXPORT jfloat JNICALL Java_com_tightdb_TableQuery_nativeMinimumFloat(
    JNIEnv* env, jobject, jlong nativeQueryPtr,
    jlong columnIndex, jlong start, jlong end, jlong limit)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = Ref2Ptr(pQuery->get_table());
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, pTable, columnIndex, type_Float) ||
        !ROW_INDEXES_VALID(env, pTable, start, end, limit))
        return 0;

    return pQuery->minimum_float(S(columnIndex), NULL, S(start), S(end), S(limit));
}

JNIEXPORT jdouble JNICALL Java_com_tightdb_TableQuery_nativeAverageFloat(
    JNIEnv* env, jobject, jlong nativeQueryPtr,
    jlong columnIndex, jlong start, jlong end, jlong limit)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = Ref2Ptr(pQuery->get_table());
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, pTable, columnIndex, type_Float) ||
        !ROW_INDEXES_VALID(env, pTable, start, end, limit))
        return 0;

    size_t resultcount;
    double avg = pQuery->average_float(S(columnIndex), &resultcount, S(start), S(end), S(limit));
    return avg;
}

// double Aggregates

JNIEXPORT jdouble JNICALL Java_com_tightdb_TableQuery_nativeSumDouble(
    JNIEnv* env, jobject, jlong nativeQueryPtr,
    jlong columnIndex, jlong start, jlong end, jlong limit)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = Ref2Ptr(pQuery->get_table());
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, pTable, columnIndex, type_Double) ||
        !ROW_INDEXES_VALID(env, pTable, start, end, limit))
        return 0;

    return pQuery->sum_double(S(columnIndex), NULL, S(start), S(end), S(limit));
}

JNIEXPORT jdouble JNICALL Java_com_tightdb_TableQuery_nativeMaximumDouble(
    JNIEnv* env, jobject, jlong nativeQueryPtr,
    jlong columnIndex, jlong start, jlong end, jlong limit)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = Ref2Ptr(pQuery->get_table());
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, pTable, columnIndex, type_Double) ||
        !ROW_INDEXES_VALID(env, pTable, start, end, limit))
        return 0;

    return pQuery->maximum_double(S(columnIndex), NULL, S(start), S(end), S(limit));
}

JNIEXPORT jdouble JNICALL Java_com_tightdb_TableQuery_nativeMinimumDouble(
    JNIEnv* env, jobject, jlong nativeQueryPtr,
    jlong columnIndex, jlong start, jlong end, jlong limit)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = Ref2Ptr(pQuery->get_table());
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, pTable, columnIndex, type_Double) ||
        !ROW_INDEXES_VALID(env, pTable, start, end, limit))
        return 0;

    return pQuery->minimum_double(S(columnIndex), NULL, S(start), S(end), S(limit));
}

JNIEXPORT jdouble JNICALL Java_com_tightdb_TableQuery_nativeAverageDouble(
    JNIEnv* env, jobject, jlong nativeQueryPtr,
    jlong columnIndex, jlong start, jlong end, jlong limit)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = Ref2Ptr(pQuery->get_table());
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, pTable, columnIndex, type_Double) ||
        !ROW_INDEXES_VALID(env, pTable, start, end, limit))
        return 0;

    //TODO: Return resultcount
    size_t resultcount;
    double avg = pQuery->average_double(S(columnIndex), &resultcount, S(start), S(end), S(limit));
    return avg;
}


// Count, Remove

JNIEXPORT jlong JNICALL Java_com_tightdb_TableQuery_nativeCount(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlong start, jlong end, jlong limit)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = Ref2Ptr(pQuery->get_table());
    if (!TABLE_VALID(env, pTable) ||
        !ROW_INDEXES_VALID(env, pTable, start, end, limit))
        return 0;

    return pQuery->count(S(start), S(end), S(limit));
}

JNIEXPORT jlong JNICALL Java_com_tightdb_TableQuery_nativeRemove(
    JNIEnv* env, jobject, jlong nativeQueryPtr, jlong start, jlong end, jlong limit)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = Ref2Ptr(pQuery->get_table());
    if (!TABLE_VALID(env, pTable) ||
        !ROW_INDEXES_VALID(env, pTable, start, end, limit))
        return 0;

    return pQuery->remove(S(start), S(end), S(limit));
}
