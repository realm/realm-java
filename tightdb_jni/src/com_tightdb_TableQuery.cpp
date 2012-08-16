#include "util.h"
#include "com_tightdb_tablequery.h"

using namespace tightdb;

JNIEXPORT jlong JNICALL Java_com_tightdb_TableQuery_createNativePtr(
	JNIEnv* env, jobject jQuery)
{
	return reinterpret_cast<jlong>(new Query());
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeEqual__JJJ(
	JNIEnv* env, jobject jTableQuery, jlong nativeQueryPtr, jlong columnIndex, jlong value)
{
	Q(nativeQueryPtr)->equal(S(columnIndex), value);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeNotEqual__JJJ(
	JNIEnv* env, jobject jTableQuery, jlong nativeQueryPtr, jlong columnIndex, jlong value)
{
	Q(nativeQueryPtr)->not_equal(S(columnIndex), value);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeGreater(
	JNIEnv* env, jobject jTableQuery, jlong nativeQueryPtr, jlong columnIndex, jlong value)
{
	Q(nativeQueryPtr)->greater(S(columnIndex), value);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeGreaterEqual(
	JNIEnv* env, jobject jTableQuery, jlong nativeQueryPtr, jlong columnIndex, jlong value)
{	
	Q(nativeQueryPtr)->greater_equal(S(columnIndex), value);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeLess(
	JNIEnv* env, jobject jTableQuery, jlong nativeQueryPtr, jlong columnIndex, jlong value)
{	
	Q(nativeQueryPtr)->less(S(columnIndex), value);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeLessEqual(
	JNIEnv* env, jobject jTableQuery, jlong nativeQueryPtr, jlong columnIndex, jlong value)
{	
	Q(nativeQueryPtr)->less_equal(S(columnIndex), value);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeBetween(
	JNIEnv* env, jobject jTableQuery, jlong nativeQueryPtr, jlong columnIndex, jlong value1, jlong value2)
{	
	Q(nativeQueryPtr)->between(S(columnIndex), value1, value2);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeEqual__JJZ(
	JNIEnv* env, jobject jTableQuery, jlong nativeQueryPtr, jlong columnIndex, jboolean value)
{	
	Q(nativeQueryPtr)->equal(S(columnIndex), value != 0 ? true : false);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeEqual__JJLjava_lang_String_2Z(
	JNIEnv* env, jobject jTableQuery, jlong nativeQueryPtr, jlong columnIndex, jstring value, jboolean caseSensitive)
{	
	const char* valueCharPtr = env->GetStringUTFChars(value, NULL);
    if (!valueCharPtr) 
        return;

	Q(nativeQueryPtr)->equal(S(columnIndex), valueCharPtr, caseSensitive ? true : false);
	env->ReleaseStringUTFChars(value, valueCharPtr);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeBeginsWith(
	JNIEnv* env, jobject jTableQuery, jlong nativeQueryPtr, jlong columnIndex, jstring value, jboolean caseSensitive)
{	
	const char* valueCharPtr = env->GetStringUTFChars(value, NULL);
    if (!valueCharPtr) 
        return;

    Q(nativeQueryPtr)->begins_with(S(columnIndex), valueCharPtr, caseSensitive ? true : false);
	env->ReleaseStringUTFChars(value, valueCharPtr);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeEndsWith(
	JNIEnv* env, jobject jTableQuery, jlong nativeQueryPtr, jlong columnIndex, jstring value, jboolean caseSensitive)
{	
	const char* valueCharPtr = env->GetStringUTFChars(value, NULL);
    if (!valueCharPtr) 
        return;

    Q(nativeQueryPtr)->ends_with(S(columnIndex), valueCharPtr, caseSensitive ? true : false);
	env->ReleaseStringUTFChars(value, valueCharPtr);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeContains(
	JNIEnv* env, jobject jTableQuery, jlong nativeQueryPtr, jlong columnIndex, jstring value, jboolean caseSensitive)
{	
	const char* valueCharPtr = env->GetStringUTFChars(value, NULL);
    if (!valueCharPtr) 
        return;

    Q(nativeQueryPtr)->contains(S(columnIndex), valueCharPtr, caseSensitive ? true : false);
	env->ReleaseStringUTFChars(value, valueCharPtr);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeNotEqual__JJLjava_lang_String_2Z(
	JNIEnv* env, jobject jTableQuery, jlong nativeQueryPtr, jlong columnIndex, jstring value, jboolean caseSensitive)
{	
	const char* valueCharPtr = env->GetStringUTFChars(value, NULL);
    if (!valueCharPtr) 
        return;

    Q(nativeQueryPtr)->not_equal(S(columnIndex), valueCharPtr, caseSensitive ? true : false);
	env->ReleaseStringUTFChars(value, valueCharPtr);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeGroup(
	JNIEnv* env, jobject jTableQuery, jlong nativeQueryPtr)
{
	Q(nativeQueryPtr)->group();
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeEndGroup(
	JNIEnv* env, jobject jTableQuery, jlong nativeQueryPtr)
{	
	Q(nativeQueryPtr)->end_group();
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeSubTable(
	JNIEnv* env, jobject jTableQuery, jlong nativeQueryPtr, jlong columnIndex)
{	
	Q(nativeQueryPtr)->subtable(S(columnIndex));
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeParent(
	JNIEnv* env, jobject jTableQuery, jlong nativeQueryPtr)
{
	Q(nativeQueryPtr)->end_subtable();
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeOr(
	JNIEnv* env, jobject jTableQuery, jlong nativeQueryPtr)
{	
	Q(nativeQueryPtr)->Or();
}

JNIEXPORT jlong JNICALL Java_com_tightdb_TableQuery_nativeFindNext(
	JNIEnv* env, jobject jTableQuery, jlong nativeQueryPtr, jobject jTable, jlong nativeTablePtr, jlong lastMatch)
{
	Table* pTable = TBL(nativeTablePtr);
	return Q(nativeQueryPtr)->find_next(*pTable, S(lastMatch));
}

JNIEXPORT jlong JNICALL Java_com_tightdb_TableQuery_nativeFindAll(
	JNIEnv* env, jobject jTableQuery, jlong nativeQueryPtr, jobject jTableBase, jlong nativeTablePtr, 
    jlong start, jlong end, jlong limit)
{
	Table* pTable = TBL(nativeTablePtr);
    Query* pQuery = Q(nativeQueryPtr);
    TableView* pResultView = new TableView(	pQuery->find_all(*pTable, S(start), S(end), S(limit)) );
    return reinterpret_cast<jlong>(pResultView);
}

JNIEXPORT jlong JNICALL Java_com_tightdb_TableQuery_nativeSum(
	JNIEnv* env, jobject jTableQuery, jlong nativeQueryPtr, jobject jTableBase, jlong nativeTablePtr, 
    jlong columnIndex, jlong start, jlong end, jlong limit)
{	
	Table* pTable = TBL(nativeTablePtr);
	return Q(nativeQueryPtr)->sum(*pTable, S(columnIndex), NULL, S(start), S(end), S(limit));
}

JNIEXPORT jlong JNICALL Java_com_tightdb_TableQuery_nativeMaximum(
	JNIEnv* env, jobject jTableQuery, jlong nativeQueryPtr, jobject jTableBase, jlong nativeTablePtr, 
    jlong columnIndex, jlong start, jlong end, jlong limit)
{
	Table* pTable = TBL(nativeTablePtr);
	return Q(nativeQueryPtr)->maximum(*pTable, S(columnIndex), NULL, S(start), S(end), S(limit));
}

JNIEXPORT jlong JNICALL Java_com_tightdb_TableQuery_nativeMinimum(
	JNIEnv* env, jobject jTableQuery, jlong nativeQueryPtr, jobject jTableBase, jlong nativeTablePtr, 
    jlong columnIndex, jlong start, jlong end, jlong limit)
{	
    Table* pTable = TBL(nativeTablePtr);
	return Q(nativeQueryPtr)->minimum(*pTable, S(columnIndex), NULL, S(start), S(end), S(limit));
}

JNIEXPORT jdouble JNICALL Java_com_tightdb_TableQuery_nativeAverage(
	JNIEnv* env, jobject jTableQuery, jlong nativeQueryPtr, jobject jTableBase, jlong nativeTablePtr, 
    jlong columnIndex, jlong start, jlong end, jlong limit)
{
	Table* pTable = TBL(nativeTablePtr);
	return Q(nativeQueryPtr)->average(*pTable, S(columnIndex), NULL, S(start), S(end), S(limit));
}

JNIEXPORT jlong JNICALL Java_com_tightdb_TableQuery_nativeCount(
	JNIEnv* env, jobject jTableQuery, jlong nativeQueryPtr, jobject jTableBase, jlong nativeTablePtr, 
    jlong start, jlong end, jlong limit)
{
	Table* pTable = TBL(nativeTablePtr);
	return Q(nativeQueryPtr)->count(*pTable, S(start), S(end), S(limit));
}

JNIEXPORT jlong JNICALL Java_com_tightdb_TableQuery_nativeFindAllMulti(
	JNIEnv* env, jobject jTableQuery, jlong nativeQueryPtr, jobject jTableBase, jlong nativeTablePtr, 
    jlong start, jlong end)
{
	Table* pTable = TBL(nativeTablePtr);
	Query* pQuery = Q(nativeQueryPtr);
    TableView* pResultView = new TableView( pQuery->find_all_multi(*pTable, S(start), S(end)) );
	return reinterpret_cast<jlong>(pResultView);
}

JNIEXPORT jstring JNICALL Java_com_tightdb_TableQuery_nativeGetErrorCode(
	JNIEnv* env, jobject jQuery, jlong nativeQueryPtr)
{	
	return env->NewStringUTF(Q(nativeQueryPtr)->error_code.c_str());
}

JNIEXPORT jint JNICALL Java_com_tightdb_TableQuery_nativeSetThreads(
	JNIEnv* env, jobject jQuery, jlong nativeQueryPtr, jint threadCount)
{	
	return Q(nativeQueryPtr)->set_threads(threadCount);
}

JNIEXPORT jlong JNICALL Java_com_tightdb_TableQuery_nativeRemove(
	JNIEnv* env, jobject jTableQuery, jlong nativeQueryPtr, jobject jTableBase, jlong nativeTablePtr, 
    jlong start, jlong end, jlong limit)
{
	return Q(nativeQueryPtr)->remove(*TBL(nativeTablePtr), S(start), S(end), S(limit));
}
