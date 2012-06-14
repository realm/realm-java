#include <tightdb.hpp>
#include <jni.h>

#include "com_tightdb_TableQuery.h"

using namespace tightdb;

JNIEXPORT jlong JNICALL Java_com_tightdb_TableQuery_createNativePtr(
	JNIEnv* env, jobject jQuery)
{
	return reinterpret_cast<jlong>(new Query());
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeEqual__JJJ(
	JNIEnv* env, jobject jTableQuery, jlong nativeQueryPtr, jlong columnIndex, jlong value)
{
	reinterpret_cast<Query*>(nativeQueryPtr)->
        equal(static_cast<size_t>(columnIndex), value);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeNotEqual__JJJ(
	JNIEnv* env, jobject jTableQuery, jlong nativeQueryPtr, jlong columnIndex, jlong value)
{
	reinterpret_cast<Query*>(nativeQueryPtr)->
        not_equal(static_cast<size_t>(columnIndex), value);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeGreater(
	JNIEnv* env, jobject jTableQuery, jlong nativeQueryPtr, jlong columnIndex, jlong value)
{
	reinterpret_cast<Query*>(nativeQueryPtr)->
        greater(static_cast<size_t>(columnIndex), value);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeGreaterEqual(
	JNIEnv* env, jobject jTableQuery, jlong nativeQueryPtr, jlong columnIndex, jlong value)
{	
	reinterpret_cast<Query*>(nativeQueryPtr)->
        greater_equal(static_cast<size_t>(columnIndex), value);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeLess(
	JNIEnv* env, jobject jTableQuery, jlong nativeQueryPtr, jlong columnIndex, jlong value)
{	
	reinterpret_cast<Query*>(nativeQueryPtr)->
        less(static_cast<size_t>(columnIndex), value);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeLessEqual(
	JNIEnv* env, jobject jTableQuery, jlong nativeQueryPtr, jlong columnIndex, jlong value)
{	
	reinterpret_cast<Query*>(nativeQueryPtr)->
        less_equal(static_cast<size_t>(columnIndex), value);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeBetween(
	JNIEnv* env, jobject jTableQuery, jlong nativeQueryPtr, jlong columnIndex, jlong value1, jlong value2)
{	
	reinterpret_cast<Query*>(nativeQueryPtr)->
        between(static_cast<size_t>(columnIndex), value1, value2);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeEqual__JJZ(
	JNIEnv* env, jobject jTableQuery, jlong nativeQueryPtr, jlong columnIndex, jboolean value)
{	
	reinterpret_cast<Query*>(nativeQueryPtr)->
        equal(static_cast<size_t>(columnIndex), value != 0 ? true : false);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeEqual__JJLjava_lang_String_2Z(
	JNIEnv* env, jobject jTableQuery, jlong nativeQueryPtr, jlong columnIndex, jstring value, jboolean caseSensitive)
{	
	const char* valueCharPtr = env->GetStringUTFChars(value, NULL);
    if (!valueCharPtr) 
        return;

	reinterpret_cast<Query*>(nativeQueryPtr)->
        equal(static_cast<size_t>(columnIndex), valueCharPtr, caseSensitive ? true : false);
	env->ReleaseStringUTFChars(value, valueCharPtr);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeBeginsWith(
	JNIEnv* env, jobject jTableQuery, jlong nativeQueryPtr, jlong columnIndex, jstring value, jboolean caseSensitive)
{	
	const char* valueCharPtr = env->GetStringUTFChars(value, NULL);
    if (!valueCharPtr) 
        return;

    reinterpret_cast<Query*>(nativeQueryPtr)->
        begins_with(static_cast<size_t>(columnIndex), valueCharPtr, caseSensitive ? true : false);
	env->ReleaseStringUTFChars(value, valueCharPtr);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeEndsWith(
	JNIEnv* env, jobject jTableQuery, jlong nativeQueryPtr, jlong columnIndex, jstring value, jboolean caseSensitive)
{	
	const char* valueCharPtr = env->GetStringUTFChars(value, NULL);
    if (!valueCharPtr) 
        return;

    reinterpret_cast<Query*>(nativeQueryPtr)->
        ends_with(static_cast<size_t>(columnIndex), valueCharPtr, caseSensitive ? true : false);
	env->ReleaseStringUTFChars(value, valueCharPtr);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeContains(
	JNIEnv* env, jobject jTableQuery, jlong nativeQueryPtr, jlong columnIndex, jstring value, jboolean caseSensitive)
{	
	const char* valueCharPtr = env->GetStringUTFChars(value, NULL);
    if (!valueCharPtr) 
        return;

    reinterpret_cast<Query*>(nativeQueryPtr)->
        contains(static_cast<size_t>(columnIndex), valueCharPtr, caseSensitive ? true : false);
	env->ReleaseStringUTFChars(value, valueCharPtr);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeNotEqual__JJLjava_lang_String_2Z(
	JNIEnv* env, jobject jTableQuery, jlong nativeQueryPtr, jlong columnIndex, jstring value, jboolean caseSensitive)
{	
	const char* valueCharPtr = env->GetStringUTFChars(value, NULL);
    if (!valueCharPtr) 
        return;

    reinterpret_cast<Query*>(nativeQueryPtr)->
        not_equal(static_cast<size_t>(columnIndex), valueCharPtr, caseSensitive ? true : false);
	env->ReleaseStringUTFChars(value, valueCharPtr);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeGroup(
	JNIEnv* env, jobject jTableQuery, jlong nativeQueryPtr)
{
	reinterpret_cast<Query*>(nativeQueryPtr)->
        group();
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeEndGroup(
	JNIEnv* env, jobject jTableQuery, jlong nativeQueryPtr)
{	
	reinterpret_cast<Query*>(nativeQueryPtr)->
        end_group();
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeSubTable(
	JNIEnv* env, jobject jTableQuery, jlong nativeQueryPtr, jlong columnIndex)
{	
	reinterpret_cast<Query*>(nativeQueryPtr)->
        subtable(static_cast<size_t>(columnIndex));
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeParent(
	JNIEnv* env, jobject jTableQuery, jlong nativeQueryPtr)
{
	reinterpret_cast<Query*>(nativeQueryPtr)->
        parent();
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeOr(
	JNIEnv* env, jobject jTableQuery, jlong nativeQueryPtr)
{	
	reinterpret_cast<Query*>(nativeQueryPtr)->
        Or();
}

JNIEXPORT jlong JNICALL Java_com_tightdb_TableQuery_nativeFindNext(
	JNIEnv* env, jobject jTableQuery, jlong nativeQueryPtr, jobject jTable, jlong nativeTablePtr, jlong lastMatch)
{
	Table* pTable = reinterpret_cast<Table*>(nativeTablePtr);
	return reinterpret_cast<Query*>(nativeQueryPtr)->
        find_next(*pTable, static_cast<size_t>(lastMatch));
}

JNIEXPORT jlong JNICALL Java_com_tightdb_TableQuery_nativeFindAll(
	JNIEnv* env, jobject jTableQuery, jlong nativeQueryPtr, jobject jTableBase, jlong nativeTablePtr, jlong start, jlong end, jlong limit)
{
	Table* pTable = reinterpret_cast<Table*>(nativeTablePtr);
    Query* pQuery = reinterpret_cast<Query*>(nativeQueryPtr);
    TableView* pResultView = new TableView(	
        pQuery->find_all(*pTable, static_cast<size_t>(start), static_cast<size_t>(end), static_cast<size_t>(limit)) );
    return reinterpret_cast<jlong>(pResultView);
}

JNIEXPORT jlong JNICALL Java_com_tightdb_TableQuery_nativeSum(
	JNIEnv* env, jobject jTableQuery, jlong nativeQueryPtr, jobject jTableBase, jlong nativeTablePtr, jlong columnIndex, jlong start, jlong end, jlong limit)
{	
	Table* pTable = reinterpret_cast<Table*>(nativeTablePtr);
	return reinterpret_cast<Query*>(nativeQueryPtr)->
        sum(*pTable, static_cast<size_t>(columnIndex), NULL, static_cast<size_t>(start), static_cast<size_t>(end), static_cast<size_t>(limit));
}

JNIEXPORT jlong JNICALL Java_com_tightdb_TableQuery_nativeMaximum(
	JNIEnv* env, jobject jTableQuery, jlong nativeQueryPtr, jobject jTableBase, jlong nativeTablePtr, jlong columnIndex, jlong start, jlong end, jlong limit)
{
	Table* pTable = reinterpret_cast<Table*>(nativeTablePtr);
	return reinterpret_cast<Query*>(nativeQueryPtr)->
        maximum(*pTable, static_cast<size_t>(columnIndex), NULL, 
                static_cast<size_t>(start), static_cast<size_t>(end), static_cast<size_t>(limit));
}

JNIEXPORT jlong JNICALL Java_com_tightdb_TableQuery_nativeMinimum(
	JNIEnv* env, jobject jTableQuery, jlong nativeQueryPtr, jobject jTableBase, jlong nativeTablePtr, jlong columnIndex, jlong start, jlong end, jlong limit)
{	
    Table* pTable = reinterpret_cast<Table*>(nativeTablePtr);
	return reinterpret_cast<Query*>(nativeQueryPtr)->
        minimum(*pTable, static_cast<size_t>(columnIndex), NULL, 
                static_cast<size_t>(start), static_cast<size_t>(end), static_cast<size_t>(limit));
}

JNIEXPORT jdouble JNICALL Java_com_tightdb_TableQuery_nativeAverage(
	JNIEnv* env, jobject jTableQuery, jlong nativeQueryPtr, jobject jTableBase, jlong nativeTablePtr, jlong columnIndex, jlong start, jlong end, jlong limit)
{
	Table* pTable = reinterpret_cast<Table*>(nativeTablePtr);
	return reinterpret_cast<Query*>(nativeQueryPtr)->
        average(*pTable, static_cast<size_t>(columnIndex), NULL, 
                static_cast<size_t>(start), static_cast<size_t>(end), static_cast<size_t>(limit));
}

JNIEXPORT jlong JNICALL Java_com_tightdb_TableQuery_nativeCount(
	JNIEnv* env, jobject jTableQuery, jlong nativeQueryPtr, jobject jTableBase, jlong nativeTablePtr, jlong start, jlong end, jlong limit)
{
	Table* pTable = reinterpret_cast<Table*>(nativeTablePtr);
	return reinterpret_cast<Query*>(nativeQueryPtr)->
        count(*pTable, static_cast<size_t>(start), static_cast<size_t>(end), static_cast<size_t>(limit));
}

JNIEXPORT jlong JNICALL Java_com_tightdb_TableQuery_nativeFindAllMulti(
	JNIEnv* env, jobject jTableQuery, jlong nativeQueryPtr, jobject jTableBase, jlong nativeTablePtr, jlong start, jlong end)
{
	Table* pTable = reinterpret_cast<Table*>(nativeTablePtr);
	Query* pQuery = reinterpret_cast<Query*>(nativeQueryPtr);
    TableView* pResultView = new TableView( 
        pQuery->FindAllMulti(*pTable, static_cast<size_t>(start), static_cast<size_t>(end)) );
	return reinterpret_cast<jlong>(pResultView);
}

JNIEXPORT jstring JNICALL Java_com_tightdb_TableQuery_nativeGetErrorCode(
	JNIEnv* env, jobject jQuery, jlong nativeQueryPtr)
{	
	return env->NewStringUTF(reinterpret_cast<Query*>(nativeQueryPtr)->error_code.c_str());
}

JNIEXPORT jint JNICALL Java_com_tightdb_TableQuery_nativeSetThreads(
	JNIEnv* env, jobject jQuery, jlong nativeQueryPtr, jint threadCount)
{	
	return reinterpret_cast<Query*>(nativeQueryPtr)->SetThreads(threadCount);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableQuery_nativeRemove(
	JNIEnv* env, jobject jTableQuery, jlong nativeQueryPtr, jobject jTableBase, jlong nativeTablePtr, jlong start, jlong end, jlong limit)
{
	Table* pTable = reinterpret_cast<Table*>(nativeTablePtr);
	reinterpret_cast<Query*>(nativeQueryPtr)->
        remove(*pTable, static_cast<size_t>(start), static_cast<size_t>(end), static_cast<size_t>(limit));
}
