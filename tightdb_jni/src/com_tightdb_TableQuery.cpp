#include "util.h"
#include "com_tightdb_TableQuery.h"

using namespace tightdb;

inline Table* Ref2Ptr(TableRef tableref)
{
    return &*tableref;
}

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

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeGreater(
	JNIEnv*, jobject, jlong nativeQueryPtr, jlong columnIndex, jlong value)
{
	Q(nativeQueryPtr)->greater(S(columnIndex), value);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeGreaterEqual(
	JNIEnv*, jobject, jlong nativeQueryPtr, jlong columnIndex, jlong value)
{	
	Q(nativeQueryPtr)->greater_equal(S(columnIndex), value);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeLess(
	JNIEnv*, jobject, jlong nativeQueryPtr, jlong columnIndex, jlong value)
{	
	Q(nativeQueryPtr)->less(S(columnIndex), value);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeLessEqual(
	JNIEnv*, jobject, jlong nativeQueryPtr, jlong columnIndex, jlong value)
{	
	Q(nativeQueryPtr)->less_equal(S(columnIndex), value);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeBetween(
	JNIEnv*, jobject, jlong nativeQueryPtr, jlong columnIndex, jlong value1, jlong value2)
{	
	Q(nativeQueryPtr)->between(S(columnIndex), value1, value2);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeEqual__JJZ(
	JNIEnv*, jobject, jlong nativeQueryPtr, jlong columnIndex, jboolean value)
{	
	Q(nativeQueryPtr)->equal(S(columnIndex), value != 0 ? true : false);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeEqual__JJLjava_lang_String_2Z(
	JNIEnv* env, jobject, jlong nativeQueryPtr, jlong columnIndex, jstring value, jboolean caseSensitive)
{	
	const char* valueCharPtr = env->GetStringUTFChars(value, NULL);
    if (!valueCharPtr) 
        return;

	Q(nativeQueryPtr)->equal(S(columnIndex), valueCharPtr, caseSensitive ? true : false);
	env->ReleaseStringUTFChars(value, valueCharPtr);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeBeginsWith(
	JNIEnv* env, jobject, jlong nativeQueryPtr, jlong columnIndex, jstring value, jboolean caseSensitive)
{	
	const char* valueCharPtr = env->GetStringUTFChars(value, NULL);
    if (!valueCharPtr) 
        return;

    Q(nativeQueryPtr)->begins_with(S(columnIndex), valueCharPtr, caseSensitive ? true : false);
	env->ReleaseStringUTFChars(value, valueCharPtr);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeEndsWith(
	JNIEnv* env, jobject, jlong nativeQueryPtr, jlong columnIndex, jstring value, jboolean caseSensitive)
{	
	const char* valueCharPtr = env->GetStringUTFChars(value, NULL);
    if (!valueCharPtr) 
        return;

    Q(nativeQueryPtr)->ends_with(S(columnIndex), valueCharPtr, caseSensitive ? true : false);
	env->ReleaseStringUTFChars(value, valueCharPtr);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeContains(
	JNIEnv* env, jobject, jlong nativeQueryPtr, jlong columnIndex, jstring value, jboolean caseSensitive)
{	
	const char* valueCharPtr = env->GetStringUTFChars(value, NULL);
    if (!valueCharPtr) 
        return;

    Q(nativeQueryPtr)->contains(S(columnIndex), valueCharPtr, caseSensitive ? true : false);
	env->ReleaseStringUTFChars(value, valueCharPtr);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeNotEqual__JJLjava_lang_String_2Z(
	JNIEnv* env, jobject, jlong nativeQueryPtr, jlong columnIndex, jstring value, jboolean caseSensitive)
{	
	const char* valueCharPtr = env->GetStringUTFChars(value, NULL);
    if (!valueCharPtr) 
        return;

    Q(nativeQueryPtr)->not_equal(S(columnIndex), valueCharPtr, caseSensitive ? true : false);
	env->ReleaseStringUTFChars(value, valueCharPtr);
}

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
	JNIEnv* env, jobject, jlong nativeQueryPtr, jlong columnIndex)
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

JNIEXPORT jlong JNICALL Java_com_tightdb_TableQuery_nativeFindNext(
	JNIEnv*, jobject, jlong nativeQueryPtr, jlong lastMatch)
{
	return Q(nativeQueryPtr)->find_next(S(lastMatch));
}

JNIEXPORT jlong JNICALL Java_com_tightdb_TableQuery_nativeFindAll(
	JNIEnv* env, jobject, jlong nativeQueryPtr, jlong start, jlong end, jlong limit)
{
    Query* pQuery = Q(nativeQueryPtr);
   // TODO?  if (!ROW_INDEXES_VALID(env, pTable, start, end, limit))
   //     return 0;
        
    TableView* pResultView = new TableView(	pQuery->find_all(S(start), S(end), S(limit)) );
    return reinterpret_cast<jlong>(pResultView);
}

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

// TODO: Check columnTypes in many methods..

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

JNIEXPORT jlong JNICALL Java_com_tightdb_TableQuery_nativeCount(
	JNIEnv* env, jobject, jlong nativeQueryPtr, jlong start, jlong end, jlong limit)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = Ref2Ptr(pQuery->get_table());
    if (!ROW_INDEXES_VALID(env, pTable, start, end, limit))
        return 0;

    return pQuery->count(S(start), S(end), S(limit));
}

JNIEXPORT jlong JNICALL Java_com_tightdb_TableQuery_nativeFindAllMulti(
	JNIEnv* env, jobject, jlong nativeQueryPtr, jlong start, jlong end)
{
    Query* pQuery = Q(nativeQueryPtr);
    Table* pTable = Ref2Ptr(pQuery->get_table());
    if (!ROW_INDEXES_VALID(env, pTable, start, end, 0))
        return 0;

    TableView* pResultView = new TableView( pQuery->find_all_multi(S(start), S(end)) );
	return reinterpret_cast<jlong>(pResultView);
}

JNIEXPORT jstring JNICALL Java_com_tightdb_TableQuery_nativeGetErrorCode(
	JNIEnv* env, jobject, jlong nativeQueryPtr)
{	
	return env->NewStringUTF(Q(nativeQueryPtr)->error_code.c_str());
}

JNIEXPORT jint JNICALL Java_com_tightdb_TableQuery_nativeSetThreads(
	JNIEnv*, jobject, jlong nativeQueryPtr, jint threadCount)
{	
	return Q(nativeQueryPtr)->set_threads(threadCount);
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
