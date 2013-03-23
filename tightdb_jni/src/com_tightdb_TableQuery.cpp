#include "util.hpp"
#include "com_tightdb_TableQuery.hpp"

using namespace tightdb;

inline Table* Ref2Ptr(TableRef tableref)
{
    return &*tableref;
}

// Integer

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeEqual__JJJ(
	JNIEnv*, jobject, jlong nativeQueryPtr, jlong columnIndex, jlong value)
{
	Q(nativeQueryPtr)->equal(S(columnIndex), value);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeNotEqual__JJJ(
	JNIEnv*, jobject, jlong nativeQueryPtr, jlong columnIndex, jlong value)
{
	Q(nativeQueryPtr)->not_equal(S(columnIndex), value);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeGreater__JJJ(
	JNIEnv*, jobject, jlong nativeQueryPtr, jlong columnIndex, jlong value)
{
	Q(nativeQueryPtr)->greater(S(columnIndex), value);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeGreaterEqual__JJJ(
	JNIEnv*, jobject, jlong nativeQueryPtr, jlong columnIndex, jlong value)
{	
	Q(nativeQueryPtr)->greater_equal(S(columnIndex), value);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeLess__JJJ(
	JNIEnv*, jobject, jlong nativeQueryPtr, jlong columnIndex, jlong value)
{	
	Q(nativeQueryPtr)->less(S(columnIndex), value);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeLessEqual__JJJ(
	JNIEnv*, jobject, jlong nativeQueryPtr, jlong columnIndex, jlong value)
{	
	Q(nativeQueryPtr)->less_equal(S(columnIndex), value);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeBetween__JJJJ(
	JNIEnv*, jobject, jlong nativeQueryPtr, jlong columnIndex, jlong value1, jlong value2)
{	
	Q(nativeQueryPtr)->between(S(columnIndex), value1, value2);
}

// Float

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeEqual__JJF(
	JNIEnv*, jobject, jlong nativeQueryPtr, jlong columnIndex, jfloat value)
{
	Q(nativeQueryPtr)->equal(S(columnIndex), value);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeNotEqual__JJF(
	JNIEnv*, jobject, jlong nativeQueryPtr, jlong columnIndex, jfloat value)
{
	Q(nativeQueryPtr)->not_equal(S(columnIndex), value);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeGreater__JJF(
	JNIEnv*, jobject, jlong nativeQueryPtr, jlong columnIndex, jfloat value)
{
	Q(nativeQueryPtr)->greater(S(columnIndex), value);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeGreaterEqual__JJF(
	JNIEnv*, jobject, jlong nativeQueryPtr, jlong columnIndex, jfloat value)
{	
	Q(nativeQueryPtr)->greater_equal(S(columnIndex), value);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeLess__JJF(
	JNIEnv*, jobject, jlong nativeQueryPtr, jlong columnIndex, jfloat value)
{	
	Q(nativeQueryPtr)->less(S(columnIndex), value);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeLessEqual__JJF(
	JNIEnv*, jobject, jlong nativeQueryPtr, jlong columnIndex, jfloat value)
{	
	Q(nativeQueryPtr)->less_equal(S(columnIndex), value);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeBetween__JJFF(
	JNIEnv*, jobject, jlong nativeQueryPtr, jlong columnIndex, jfloat value1, jfloat value2)
{	
	Q(nativeQueryPtr)->between(S(columnIndex), value1, value2);
}

// Double

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeEqual__JJD(
	JNIEnv*, jobject, jlong nativeQueryPtr, jlong columnIndex, jdouble value)
{
	Q(nativeQueryPtr)->equal(S(columnIndex), value);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeNotEqual__JJD(
	JNIEnv*, jobject, jlong nativeQueryPtr, jlong columnIndex, jdouble value)
{
	Q(nativeQueryPtr)->not_equal(S(columnIndex), value);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeGreater__JJD(
	JNIEnv*, jobject, jlong nativeQueryPtr, jlong columnIndex, jdouble value)
{
	Q(nativeQueryPtr)->greater(S(columnIndex), value);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeGreaterEqual__JJD(
	JNIEnv*, jobject, jlong nativeQueryPtr, jlong columnIndex, jdouble value)
{	
	Q(nativeQueryPtr)->greater_equal(S(columnIndex), value);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeLess__JJD(
	JNIEnv*, jobject, jlong nativeQueryPtr, jlong columnIndex, jdouble value)
{	
	Q(nativeQueryPtr)->less(S(columnIndex), value);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeLessEqual__JJD(
	JNIEnv*, jobject, jlong nativeQueryPtr, jlong columnIndex, jdouble value)
{	
	Q(nativeQueryPtr)->less_equal(S(columnIndex), value);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeBetween__JJDD(
	JNIEnv*, jobject, jlong nativeQueryPtr, jlong columnIndex, jdouble value1, jdouble value2)
{	
	Q(nativeQueryPtr)->between(S(columnIndex), value1, value2);
}

// Bool

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeEqual__JJZ(
	JNIEnv*, jobject, jlong nativeQueryPtr, jlong columnIndex, jboolean value)
{	
	Q(nativeQueryPtr)->equal(S(columnIndex), value != 0 ? true : false);
}

// String

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeEqual__JJLjava_lang_String_2Z(
	JNIEnv* env, jobject, jlong nativeQueryPtr, jlong columnIndex, jstring value, jboolean caseSensitive)
{	
    JStringAccessor value2(env, value);
    if (!value2) 
        return;

    Q(nativeQueryPtr)->equal(S(columnIndex), value2, caseSensitive ? true : false);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeBeginsWith(
	JNIEnv* env, jobject, jlong nativeQueryPtr, jlong columnIndex, jstring value, jboolean caseSensitive)
{	
    JStringAccessor value2(env, value);
    if (!value2) 
        return;

    Q(nativeQueryPtr)->begins_with(S(columnIndex), value2, caseSensitive ? true : false);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeEndsWith(
	JNIEnv* env, jobject, jlong nativeQueryPtr, jlong columnIndex, jstring value, jboolean caseSensitive)
{	
    JStringAccessor value2(env, value);
    if (!value2) 
        return;

    Q(nativeQueryPtr)->ends_with(S(columnIndex), value2, caseSensitive ? true : false);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeContains(
	JNIEnv* env, jobject, jlong nativeQueryPtr, jlong columnIndex, jstring value, jboolean caseSensitive)
{	
    JStringAccessor value2(env, value);
    if (!value2) 
        return;

    Q(nativeQueryPtr)->contains(S(columnIndex), value2, caseSensitive ? true : false);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeNotEqual__JJLjava_lang_String_2Z(
	JNIEnv* env, jobject, jlong nativeQueryPtr, jlong columnIndex, jstring value, jboolean caseSensitive)
{	
    JStringAccessor value2(env, value);
    if (!value2) 
        return;

    Q(nativeQueryPtr)->not_equal(S(columnIndex), value2, caseSensitive ? true : false);
}

// General

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeTableview(
	JNIEnv*, jobject, jlong nativeQueryPtr, jlong nativeTableViewPtr)
{
	Q(nativeQueryPtr)->tableview(*TV(nativeTableViewPtr));
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeGroup(
	JNIEnv*, jobject, jlong nativeQueryPtr)
{
	Q(nativeQueryPtr)->group();
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeEndGroup(
	JNIEnv*, jobject, jlong nativeQueryPtr)
{	
	Q(nativeQueryPtr)->end_group();
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeSubTable(
	JNIEnv*, jobject, jlong nativeQueryPtr, jlong columnIndex)
{	
	Q(nativeQueryPtr)->subtable(S(columnIndex));
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeParent(
	JNIEnv*, jobject, jlong nativeQueryPtr)
{
	Q(nativeQueryPtr)->end_subtable();
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeOr(
	JNIEnv*, jobject, jlong nativeQueryPtr)
{	
	Q(nativeQueryPtr)->Or();
}

// Find

JNIEXPORT jlong JNICALL Java_com_tightdb_TableQuery_nativeFindNext(
	JNIEnv*, jobject, jlong nativeQueryPtr, jlong lastMatch)
{
	return Q(nativeQueryPtr)->find_next(S(lastMatch));
}

JNIEXPORT jlong JNICALL Java_com_tightdb_TableQuery_nativeFindAll(
	JNIEnv*, jobject, jlong nativeQueryPtr, jlong start, jlong end, jlong limit)
{
    Query* pQuery = Q(nativeQueryPtr);
   // TODO?  if (!ROW_INDEXES_VALID(env, pTable, start, end, limit))
   //     return 0;
        
    TableView* pResultView = new TableView(	pQuery->find_all(S(start), S(end), S(limit)) );
    return reinterpret_cast<jlong>(pResultView);
}


// TODO: Check columnTypes in many methods..

// Integer Aggregates

JNIEXPORT jlong JNICALL Java_com_tightdb_TableQuery_nativeSum(
	JNIEnv* env, jobject, jlong nativeQueryPtr, 
    jlong columnIndex, jlong start, jlong end, jlong limit)
{	
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = Ref2Ptr(pQuery->get_table());
    if (!COL_INDEX_VALID(env, pTable, columnIndex)) 
        return 0;
    if (!ROW_INDEXES_VALID(env, pTable, start, end, limit))
        return 0;

    return pQuery->sum(S(columnIndex), NULL, S(start), S(end), S(limit));
}

JNIEXPORT jlong JNICALL Java_com_tightdb_TableQuery_nativeMaximum(
	JNIEnv* env, jobject, jlong nativeQueryPtr, 
    jlong columnIndex, jlong start, jlong end, jlong limit)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = Ref2Ptr(pQuery->get_table());
    if (!COL_INDEX_VALID(env, pTable, columnIndex)) 
        return 0;
    if (!ROW_INDEXES_VALID(env, pTable, start, end, limit))
        return 0;

	return pQuery->maximum(S(columnIndex), NULL, S(start), S(end), S(limit));
}

JNIEXPORT jlong JNICALL Java_com_tightdb_TableQuery_nativeMinimum(
	JNIEnv* env, jobject, jlong nativeQueryPtr,  
    jlong columnIndex, jlong start, jlong end, jlong limit)
{	
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = Ref2Ptr(pQuery->get_table());
    if (!COL_INDEX_VALID(env, pTable, columnIndex)) 
        return 0;
    if (!ROW_INDEXES_VALID(env, pTable, start, end, limit))
        return 0;
    
    return pQuery->minimum(S(columnIndex), NULL, S(start), S(end), S(limit));
}

JNIEXPORT jdouble JNICALL Java_com_tightdb_TableQuery_nativeAverage(
	JNIEnv* env, jobject, jlong nativeQueryPtr, 
    jlong columnIndex, jlong start, jlong end, jlong limit)
{	
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = Ref2Ptr(pQuery->get_table());
    if (!COL_INDEX_VALID(env, pTable, columnIndex)) 
        return 0;
    if (!ROW_INDEXES_VALID(env, pTable, start, end, limit))
        return 0;

    size_t resultcount;
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
    if (!COL_INDEX_VALID(env, pTable, columnIndex)) 
        return 0;
    if (!ROW_INDEXES_VALID(env, pTable, start, end, limit))
        return 0;

    return pQuery->sum_float(S(columnIndex), NULL, S(start), S(end), S(limit));
}

JNIEXPORT jfloat JNICALL Java_com_tightdb_TableQuery_nativeMaximumFloat(
	JNIEnv* env, jobject, jlong nativeQueryPtr, 
    jlong columnIndex, jlong start, jlong end, jlong limit)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = Ref2Ptr(pQuery->get_table());
    if (!COL_INDEX_VALID(env, pTable, columnIndex)) 
        return 0;
    if (!ROW_INDEXES_VALID(env, pTable, start, end, limit))
        return 0;

	return pQuery->maximum_float(S(columnIndex), NULL, S(start), S(end), S(limit));
}

JNIEXPORT jfloat JNICALL Java_com_tightdb_TableQuery_nativeMinimumFloat(
	JNIEnv* env, jobject, jlong nativeQueryPtr,  
    jlong columnIndex, jlong start, jlong end, jlong limit)
{	
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = Ref2Ptr(pQuery->get_table());
    if (!COL_INDEX_VALID(env, pTable, columnIndex)) 
        return 0;
    if (!ROW_INDEXES_VALID(env, pTable, start, end, limit))
        return 0;
    
    return pQuery->minimum_float(S(columnIndex), NULL, S(start), S(end), S(limit));
}

JNIEXPORT jdouble JNICALL Java_com_tightdb_TableQuery_nativeAverageFloat(
	JNIEnv* env, jobject, jlong nativeQueryPtr, 
    jlong columnIndex, jlong start, jlong end, jlong limit)
{	
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = Ref2Ptr(pQuery->get_table());
    if (!COL_INDEX_VALID(env, pTable, columnIndex)) 
        return 0;
    if (!ROW_INDEXES_VALID(env, pTable, start, end, limit))
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
    if (!COL_INDEX_VALID(env, pTable, columnIndex)) 
        return 0;
    if (!ROW_INDEXES_VALID(env, pTable, start, end, limit))
        return 0;

    return pQuery->sum_double(S(columnIndex), NULL, S(start), S(end), S(limit));
}

JNIEXPORT jdouble JNICALL Java_com_tightdb_TableQuery_nativeMaximumDouble(
	JNIEnv* env, jobject, jlong nativeQueryPtr, 
    jlong columnIndex, jlong start, jlong end, jlong limit)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = Ref2Ptr(pQuery->get_table());
    if (!COL_INDEX_VALID(env, pTable, columnIndex)) 
        return 0;
    if (!ROW_INDEXES_VALID(env, pTable, start, end, limit))
        return 0;

	return pQuery->maximum_double(S(columnIndex), NULL, S(start), S(end), S(limit));
}

JNIEXPORT jdouble JNICALL Java_com_tightdb_TableQuery_nativeMinimumDouble(
	JNIEnv* env, jobject, jlong nativeQueryPtr,  
    jlong columnIndex, jlong start, jlong end, jlong limit)
{	
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = Ref2Ptr(pQuery->get_table());
    if (!COL_INDEX_VALID(env, pTable, columnIndex)) 
        return 0;
    if (!ROW_INDEXES_VALID(env, pTable, start, end, limit))
        return 0;
    
    return pQuery->minimum_double(S(columnIndex), NULL, S(start), S(end), S(limit));
}

JNIEXPORT jdouble JNICALL Java_com_tightdb_TableQuery_nativeAverageDouble(
	JNIEnv* env, jobject, jlong nativeQueryPtr, 
    jlong columnIndex, jlong start, jlong end, jlong limit)
{	
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = Ref2Ptr(pQuery->get_table());
    if (!COL_INDEX_VALID(env, pTable, columnIndex)) 
        return 0;
    if (!ROW_INDEXES_VALID(env, pTable, start, end, limit))
        return 0;

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
    if (!ROW_INDEXES_VALID(env, pTable, start, end, limit))
        return 0;

    return pQuery->count(S(start), S(end), S(limit));
}

JNIEXPORT jlong JNICALL Java_com_tightdb_TableQuery_nativeRemove(
	JNIEnv* env, jobject, jlong nativeQueryPtr, jlong start, jlong end, jlong limit)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = Ref2Ptr(pQuery->get_table());
    if (!ROW_INDEXES_VALID(env, pTable, start, end, limit))
        return 0;

	return pQuery->remove(S(start), S(end), S(limit));
}
