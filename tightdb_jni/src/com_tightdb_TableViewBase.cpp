#include <jni.h>
#include <tightdb.hpp>
#include <tightdb/lang_bind_helper.hpp>

#include "com_tightdb_TableViewBase.h"
#include "mixedutil.h"
#include "util.h"

#include "tablebase_tpl.hpp"

using namespace tightdb;

JNIEXPORT jlong JNICALL Java_com_tightdb_TableViewBase_nativeSize(
	JNIEnv* env, jobject jTableView, jlong nativeViewPtr) 
{
	return TV(nativeViewPtr)->size();
}

JNIEXPORT jlong JNICALL Java_com_tightdb_TableViewBase_nativeGetLong(
	JNIEnv* env, jobject jTableView, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex)
{
	if (!INDEX_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex)) return 0;

    return TV(nativeViewPtr)->get_int( S(columnIndex), S(rowIndex));
}

JNIEXPORT jboolean JNICALL Java_com_tightdb_TableViewBase_nativeGetBoolean(
	JNIEnv* env, jobject jTableView, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex)
{
	if (!INDEX_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex)) return false;

    return TV(nativeViewPtr)->get_bool( S(columnIndex), S(rowIndex));
}

JNIEXPORT jlong JNICALL Java_com_tightdb_TableViewBase_nativeGetDateTimeValue(
	JNIEnv* env, jobject jTableViewBase, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex)
{
	if (!INDEX_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex)) return 0;

    return TV(nativeViewPtr)->get_date( S(columnIndex), S(rowIndex));
}

JNIEXPORT jstring JNICALL Java_com_tightdb_TableViewBase_nativeGetString(
	JNIEnv* env, jobject jTableView, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex)
{
	if (!INDEX_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex)) return NULL;

    return env->NewStringUTF( TV(nativeViewPtr)->get_string( S(columnIndex), S(rowIndex)) );
}

JNIEXPORT jobject JNICALL Java_com_tightdb_TableViewBase_nativeGetBinary(
	JNIEnv* env, jobject jTableView, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex)
{	
	if (!INDEX_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex)) return NULL;

    BinaryData data = TV(nativeViewPtr)->get_binary( S(columnIndex), S(rowIndex));
	return env->NewDirectByteBuffer((void*)data.pointer,  static_cast<jlong>(data.len));
}

JNIEXPORT jbyteArray JNICALL Java_com_tightdb_TableViewBase_nativeGetByteArray(
	JNIEnv* env, jobject jTableView, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex)
{
	if (!INDEX_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex)) return NULL;

    return tbl_GetByteArray<TableView>(env, nativeViewPtr, columnIndex, rowIndex);
}

JNIEXPORT jobject JNICALL Java_com_tightdb_TableViewBase_nativeGetMixed(
	JNIEnv* env, jobject jTableView, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex)
{	
	if (!INDEX_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex)) return NULL;

    Mixed value = TV(nativeViewPtr)->get_mixed( S(columnIndex), S(rowIndex));
	return CreateJMixedFromMixed(env, value);
}

//TODO ?? add getSubtableSize ??

JNIEXPORT jlong JNICALL Java_com_tightdb_TableViewBase_nativeGetSubTable(
	JNIEnv* env, jobject jTableView, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex)
{	
    if (!INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex, COLUMN_TYPE_TABLE)) return 0;

    Table* pSubTable = LangBindHelper::get_subtable_ptr(TV(nativeViewPtr), S(columnIndex), S(rowIndex));
	return reinterpret_cast<jlong>(pSubTable);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableViewBase_nativeSetLong(
	JNIEnv* env, jobject jTableView, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex, jlong value)
{	
	if (!INDEX_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex)) return;

    TV(nativeViewPtr)->set_int( S(columnIndex), S(rowIndex), value);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableViewBase_nativeSetBoolean(
	JNIEnv* env, jobject jTableView, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex, jboolean value)
{	
	if (!INDEX_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex)) return;

    TV(nativeViewPtr)->set_bool( S(columnIndex), S(rowIndex), value != 0 ? true : false);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableViewBase_nativeSetDateTimeValue(
	JNIEnv* env, jobject jTableView, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex, jlong dateTimeValue)
{
	if (!INDEX_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex)) return;

    TV(nativeViewPtr)->set_date( S(columnIndex), S(rowIndex), dateTimeValue);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableViewBase_nativeSetString(
	JNIEnv* env, jobject jTableView, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex, jstring value)
{	
	if (!INDEX_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex)) return;

    const char* valueCharPtr = env->GetStringUTFChars(value, NULL);
    if (!valueCharPtr)
        return;

	TV(nativeViewPtr)->set_string( S(columnIndex), S(rowIndex), valueCharPtr);
	env->ReleaseStringUTFChars(value, valueCharPtr);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableViewBase_nativeSetBinary(
	JNIEnv* env, jobject jTableView, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex, jobject byteBuffer)
{	
    if (!INDEX_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex)) return;
    
    tbl_nativeDoBinary(&TableView::set_binary, TV(nativeViewPtr), env, columnIndex, rowIndex, byteBuffer);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableViewBase_nativeSetByteArray(
	JNIEnv* env, jobject jTableView, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex, jbyteArray byteArray)
{
    if (!INDEX_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex)) return;

    tbl_nativeDoByteArray(&TableView::set_binary, TV(nativeViewPtr), env, columnIndex, rowIndex, byteArray);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableViewBase_nativeSetMixed(
	JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex, jobject jMixedValue)
{	
    if (!INDEX_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex)) return;

    tbl_nativeDoMixed(&TableView::set_mixed, TV(nativeViewPtr), env, columnIndex, rowIndex, jMixedValue);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableViewBase_nativeClear(
	JNIEnv* env, jobject jTableView, jlong nativeViewPtr)
{
	TV(nativeViewPtr)->clear();
}

JNIEXPORT void JNICALL Java_com_tightdb_TableViewBase_nativeRemoveRow(
	JNIEnv* env, jobject jTableView, jlong nativeViewPtr, jlong rowIndex)
{
	if (!ROW_INDEX_VALID(env, TV(nativeViewPtr), rowIndex)) return;
    
    TV(nativeViewPtr)->remove( S(rowIndex));
}

JNIEXPORT jlong JNICALL Java_com_tightdb_TableViewBase_nativeFindFirst__JJJ(
	JNIEnv* env, jobject jTableView, jlong nativeViewPtr, jlong columnIndex, jlong value)
{	
    if (!COL_INDEX_VALID(env, TV(nativeViewPtr), columnIndex)) return 0;

    return static_cast<jlong>(TV(nativeViewPtr)->find_first_int( S(columnIndex), value));
}

JNIEXPORT jlong JNICALL Java_com_tightdb_TableViewBase_nativeFindFirst__JJLjava_lang_String_2(
	JNIEnv* env, jobject jTableViewObject, jlong nativeViewPtr, jlong columnIndex, jstring value)
{
    if (!COL_INDEX_VALID(env, TV(nativeViewPtr), columnIndex)) return 0;

	const char* valueCharPtr = env->GetStringUTFChars(value, NULL);
    if (!valueCharPtr)
        return -1;

	size_t searchIndex = TV(nativeViewPtr)->find_first_string( S(columnIndex), valueCharPtr);
	env->ReleaseStringUTFChars(value, valueCharPtr);
	return static_cast<jlong>(searchIndex);
}

JNIEXPORT jlong JNICALL Java_com_tightdb_TableViewBase_nativeFindAll__JJJ(
	JNIEnv* env, jobject jTableViewBase, jlong nativeViewPtr, jlong columnIndex, jlong value)
{	
    if (!COL_INDEX_VALID(env, TV(nativeViewPtr), columnIndex)) return 0;

	TableView* pResultView = new TableView( TV(nativeViewPtr)->find_all_int( S(columnIndex), value) );
	return reinterpret_cast<jlong>(pResultView);
}

JNIEXPORT jlong JNICALL Java_com_tightdb_TableViewBase_nativeFindAll__JJLjava_lang_String_2(
	JNIEnv* env, jobject jTableView, jlong nativeViewPtr, jlong columnIndex, jstring value)
{	
    if (!COL_INDEX_VALID(env, TV(nativeViewPtr), columnIndex)) return 0;

	const char* valueCharPtr = env->GetStringUTFChars(value, NULL);
    if (!valueCharPtr)
        return -1;

	TableView* pResultView = new TableView( TV(nativeViewPtr)->find_all_string( S(columnIndex), valueCharPtr) );
	env->ReleaseStringUTFChars(value, valueCharPtr);
	return reinterpret_cast<jlong>(pResultView);
}

JNIEXPORT jlong JNICALL Java_com_tightdb_TableViewBase_nativeSum(
	JNIEnv* env, jobject jTableView, jlong nativeViewPtr, jlong columnIndex)
{	
    if (!COL_INDEX_VALID(env, TV(nativeViewPtr), columnIndex)) return 0;

	return TV(nativeViewPtr)->sum( S(columnIndex));
}

JNIEXPORT jlong JNICALL Java_com_tightdb_TableViewBase_nativeMaximum(
	JNIEnv* env, jobject jTableView, jlong nativeViewPtr, jlong columnIndex)
{	
    if (!COL_INDEX_VALID(env, TV(nativeViewPtr), columnIndex)) return 0;

	return TV(nativeViewPtr)->maximum( S(columnIndex));
}

JNIEXPORT jlong JNICALL Java_com_tightdb_TableViewBase_nativeMinimum(
	JNIEnv* env, jobject jTableView, jlong nativeViewPtr, jlong columnIndex)
{	
    if (!COL_INDEX_VALID(env, TV(nativeViewPtr), columnIndex)) return 0;

	return TV(nativeViewPtr)->minimum( S(columnIndex));
}

JNIEXPORT void JNICALL Java_com_tightdb_TableViewBase_nativeSort(
	JNIEnv* env, jobject jTableView, jlong nativeViewPtr, jlong columnIndex, jboolean ascending)
{	
    if (!COL_INDEX_VALID(env, TV(nativeViewPtr), columnIndex)) return;

	TV(nativeViewPtr)->sort( S(columnIndex), ascending != 0 ? true : false);
}

JNIEXPORT jlong JNICALL Java_com_tightdb_TableViewBase_createNativeTableView(
	JNIEnv* env, jobject jTableView, jobject jTable, jlong nativeTablePtr)
{
    return reinterpret_cast<jlong>( new TableView() );
}

JNIEXPORT void JNICALL Java_com_tightdb_TableViewBase_nativeClose(
	JNIEnv* env, jobject jTableView, jlong nativeTableViewPtr)
{
	delete TV(nativeTableViewPtr);
}

// FIXME: Add support for Count, Average, Remove