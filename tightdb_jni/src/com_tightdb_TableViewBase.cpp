#include "util.h"
#include "com_tightdb_TableViewBase.h"
#include "mixedutil.h"
#include "tablebase_tpl.hpp"

using namespace tightdb;

JNIEXPORT jlong JNICALL Java_com_tightdb_TableViewBase_nativeSize(
	JNIEnv*, jobject, jlong nativeViewPtr) 
{
	return TV(nativeViewPtr)->size();
}

JNIEXPORT jlong JNICALL Java_com_tightdb_TableViewBase_nativeGetLong(
	JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex)
{
	if (!INDEX_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex)) return 0;

    return TV(nativeViewPtr)->get_int( S(columnIndex), S(rowIndex));
}

JNIEXPORT jboolean JNICALL Java_com_tightdb_TableViewBase_nativeGetBoolean(
	JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex)
{
	if (!INDEX_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex)) return false;

    return TV(nativeViewPtr)->get_bool( S(columnIndex), S(rowIndex));
}

JNIEXPORT jlong JNICALL Java_com_tightdb_TableViewBase_nativeGetDateTimeValue(
	JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex)
{
	if (!INDEX_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex)) return 0;

    return TV(nativeViewPtr)->get_date( S(columnIndex), S(rowIndex));
}

JNIEXPORT jstring JNICALL Java_com_tightdb_TableViewBase_nativeGetString(
	JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex)
{
	if (!INDEX_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex)) return NULL;

    return env->NewStringUTF( TV(nativeViewPtr)->get_string( S(columnIndex), S(rowIndex)) );
}

JNIEXPORT jobject JNICALL Java_com_tightdb_TableViewBase_nativeGetBinary(
	JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex)
{	
	if (!INDEX_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex)) return NULL;

    BinaryData data = TV(nativeViewPtr)->get_binary( S(columnIndex), S(rowIndex));
	return env->NewDirectByteBuffer((void*)data.pointer,  static_cast<jlong>(data.len));
}

JNIEXPORT jbyteArray JNICALL Java_com_tightdb_TableViewBase_nativeGetByteArray(
	JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex)
{
	if (!INDEX_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex)) return NULL;

    return tbl_GetByteArray<TableView>(env, nativeViewPtr, columnIndex, rowIndex);
}

JNIEXPORT jint JNICALL Java_com_tightdb_TableViewBase_nativeGetMixedType(
	JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex)
{
	if (!INDEX_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex)) return 0;

	ColumnType mixedType = TV(nativeViewPtr)->get_mixed_type( S(columnIndex), S(rowIndex));
	return static_cast<jint>(mixedType);
}

JNIEXPORT jobject JNICALL Java_com_tightdb_TableViewBase_nativeGetMixed(
	JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex)
{	
	if (!INDEX_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex)) return NULL;

    Mixed value = TV(nativeViewPtr)->get_mixed( S(columnIndex), S(rowIndex));
	return CreateJMixedFromMixed(env, value);
}

JNIEXPORT jlong JNICALL Java_com_tightdb_TableViewBase_nativeGetSubTableSize(
	JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex)
{
    if (!INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex, COLUMN_TYPE_TABLE)) return 0;

    return TV(nativeViewPtr)->get_subtable_size( S(columnIndex), S(rowIndex));
}

JNIEXPORT jlong JNICALL Java_com_tightdb_TableViewBase_nativeGetSubTable(
	JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex)
{	
    if (!INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex, COLUMN_TYPE_TABLE)) return 0;

    Table* pSubTable = LangBindHelper::get_subtable_ptr(TV(nativeViewPtr), S(columnIndex), S(rowIndex));
	return reinterpret_cast<jlong>(pSubTable);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableViewBase_nativeSetLong(
	JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex, jlong value)
{	
	if (!INDEX_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex)) return;

    TV(nativeViewPtr)->set_int( S(columnIndex), S(rowIndex), value);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableViewBase_nativeSetBoolean(
	JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex, jboolean value)
{	
	if (!INDEX_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex)) return;

    TV(nativeViewPtr)->set_bool( S(columnIndex), S(rowIndex), value != 0 ? true : false);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableViewBase_nativeSetDateTimeValue(
	JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex, jlong dateTimeValue)
{
	if (!INDEX_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex)) return;

    TV(nativeViewPtr)->set_date( S(columnIndex), S(rowIndex), dateTimeValue);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableViewBase_nativeSetString(
	JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex, jstring value)
{	
	if (!INDEX_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex)) return;

    const char* valueCharPtr = env->GetStringUTFChars(value, NULL);
    if (!valueCharPtr)
        return;

	TV(nativeViewPtr)->set_string( S(columnIndex), S(rowIndex), valueCharPtr);
	env->ReleaseStringUTFChars(value, valueCharPtr);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableViewBase_nativeSetBinary(
	JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex, jobject byteBuffer)
{	
    if (!INDEX_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex)) return;
    
    tbl_nativeDoBinary(&TableView::set_binary, TV(nativeViewPtr), env, columnIndex, rowIndex, byteBuffer);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableViewBase_nativeSetByteArray(
	JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex, jbyteArray byteArray)
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

JNIEXPORT void JNICALL Java_com_tightdb_TableViewBase_nativeAddInt(
	JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong value)
{	
	if (!COL_INDEX_VALID(env, TV(nativeViewPtr), columnIndex)) return;

    TV(nativeViewPtr)->add_int( S(columnIndex), value);
}


JNIEXPORT void JNICALL Java_com_tightdb_TableViewBase_nativeClear(
	JNIEnv*, jobject, jlong nativeViewPtr)
{
	TV(nativeViewPtr)->clear();
}

JNIEXPORT void JNICALL Java_com_tightdb_TableViewBase_nativeRemoveRow(
	JNIEnv* env, jobject, jlong nativeViewPtr, jlong rowIndex)
{
	if (!ROW_INDEX_VALID(env, TV(nativeViewPtr), rowIndex)) return;
    
    TV(nativeViewPtr)->remove( S(rowIndex));
}

JNIEXPORT jlong JNICALL Java_com_tightdb_TableViewBase_nativeFindFirstInt(
	JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong value)
{	
    if (!COL_INDEX_VALID(env, TV(nativeViewPtr), columnIndex)) return 0;

    return static_cast<jlong>(TV(nativeViewPtr)->find_first_int( S(columnIndex), value));
}

JNIEXPORT jlong JNICALL Java_com_tightdb_TableViewBase_nativeFindFirstBool(
	JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jboolean value)
{
   	if (!COL_INDEX_VALID(env, TV(nativeViewPtr), columnIndex)) return false;

    return TV(nativeViewPtr)->find_first_bool( S(columnIndex), value != 0 ? true : false);
}

JNIEXPORT jlong JNICALL Java_com_tightdb_TableViewBase_nativeFindFirstDate(
	JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong dateTimeValue)
{
   	if (!COL_INDEX_VALID(env, TV(nativeViewPtr), columnIndex)) return 0;

	return TV(nativeViewPtr)->find_first_date( S(columnIndex), (time_t)dateTimeValue);
}

JNIEXPORT jlong JNICALL Java_com_tightdb_TableViewBase_nativeFindFirstString(
	JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jstring value)
{
    if (!COL_INDEX_VALID(env, TV(nativeViewPtr), columnIndex)) return 0;

	const char* valueCharPtr = env->GetStringUTFChars(value, NULL);
    if (!valueCharPtr)
        return 0;

	size_t searchIndex = TV(nativeViewPtr)->find_first_string( S(columnIndex), valueCharPtr);
	env->ReleaseStringUTFChars(value, valueCharPtr);
	return static_cast<jlong>(searchIndex);
}

JNIEXPORT jlong JNICALL Java_com_tightdb_TableViewBase_nativeFindAllInt(
	JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong value)
{	
    if (!COL_INDEX_VALID(env, TV(nativeViewPtr), columnIndex)) return 0;

	TableView* pResultView = new TableView( TV(nativeViewPtr)->find_all_int( S(columnIndex), value) );
	return reinterpret_cast<jlong>(pResultView);
}

JNIEXPORT jlong JNICALL Java_com_tightdb_TableViewBase_nativeFindAllBool(
	JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jboolean value)
{	
    if (!COL_INDEX_VALID(env, TV(nativeViewPtr), columnIndex)) return 0;

	TableView* pResultView = new TableView( TV(nativeViewPtr)->find_all_bool( S(columnIndex), 
                                            value != 0 ? true : false) );
	return reinterpret_cast<jlong>(pResultView);
}

JNIEXPORT jlong JNICALL Java_com_tightdb_TableViewBase_nativeFindAllDate(
	JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong dateTimeValue)
{	
    if (!COL_INDEX_VALID(env, TV(nativeViewPtr), columnIndex)) return 0;

	TableView* pResultView = new TableView( TV(nativeViewPtr)->find_all_date( S(columnIndex), 
                                            static_cast<time_t>(dateTimeValue)) );
	return reinterpret_cast<jlong>(pResultView);
}

JNIEXPORT jlong JNICALL Java_com_tightdb_TableViewBase_nativeFindAllString(
	JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jstring value)
{	
    
    if (!COL_INDEX_VALID(env, TV(nativeViewPtr), columnIndex)) return 0;

	const char* valueCharPtr = env->GetStringUTFChars(value, NULL);
    if (!valueCharPtr)
        return 0;
    TR((env, "nativeFindAllString(col %d, string '%s') ", columnIndex, valueCharPtr));

	TableView* pResultView = new TableView( TV(nativeViewPtr)->find_all_string( S(columnIndex), valueCharPtr) );
	TR((env, "-- resultview size=%lld.\n", pResultView->size()));
    env->ReleaseStringUTFChars(value, valueCharPtr);
	return reinterpret_cast<jlong>(pResultView);
}

JNIEXPORT jlong JNICALL Java_com_tightdb_TableViewBase_nativeSum(
	JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex)
{	
    if (!COL_INDEX_VALID(env, TV(nativeViewPtr), columnIndex)) return 0;

	return TV(nativeViewPtr)->sum( S(columnIndex));
}

JNIEXPORT jlong JNICALL Java_com_tightdb_TableViewBase_nativeMaximum(
	JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex)
{	
    if (!COL_INDEX_VALID(env, TV(nativeViewPtr), columnIndex)) return 0;

	return TV(nativeViewPtr)->maximum( S(columnIndex));
}

JNIEXPORT jlong JNICALL Java_com_tightdb_TableViewBase_nativeMinimum(
	JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex)
{	
    if (!COL_INDEX_VALID(env, TV(nativeViewPtr), columnIndex)) return 0;

	return TV(nativeViewPtr)->minimum( S(columnIndex));
}

JNIEXPORT void JNICALL Java_com_tightdb_TableViewBase_nativeSort(
	JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jboolean ascending)
{	
    if (!COL_INDEX_VALID(env, TV(nativeViewPtr), columnIndex)) return;

	TV(nativeViewPtr)->sort( S(columnIndex), ascending != 0 ? true : false);
}

JNIEXPORT jlong JNICALL Java_com_tightdb_TableViewBase_createNativeTableView(
	JNIEnv*, jobject, jobject, jlong)
{
    return reinterpret_cast<jlong>( new TableView() );
}

JNIEXPORT void JNICALL Java_com_tightdb_TableViewBase_nativeClose(
	JNIEnv*, jobject, jlong nativeTableViewPtr)
{
	delete TV(nativeTableViewPtr);
}

// FIXME: Add support for Count, Average, Remove
