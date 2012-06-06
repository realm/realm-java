#include <jni.h>
#include <tightdb.hpp>
#include <tightdb/lang_bind_helper.hpp>

#include "com_tightdb_TableViewBase.h"
#include "mixedutil.h"

using namespace tightdb;

JNIEXPORT jlong JNICALL Java_com_tightdb_TableViewBase_nativeSize(
	JNIEnv* env, jobject jTableView, jlong nativeViewPtr) 
{
	return reinterpret_cast<TableView*>(nativeViewPtr)->
        size();
}

JNIEXPORT jlong JNICALL Java_com_tightdb_TableViewBase_nativeGetLong(
	JNIEnv* env, jobject jTableView, jlong nativeViewPtr, jlong colIndex, jlong rowIndex)
{
	return reinterpret_cast<TableView*>(nativeViewPtr)->
        get_int(static_cast<size_t>(colIndex), static_cast<size_t>(rowIndex));
}

JNIEXPORT jboolean JNICALL Java_com_tightdb_TableViewBase_nativeGetBoolean(
	JNIEnv* env, jobject jTableView, jlong nativeViewPtr, jlong colIndex, jlong rowIndex)
{
	return reinterpret_cast<TableView*>(nativeViewPtr)->
        get_bool(static_cast<size_t>(colIndex), static_cast<size_t>(rowIndex));
}

JNIEXPORT jlong JNICALL Java_com_tightdb_TableViewBase_nativeGetDateTimeValue(
	JNIEnv* env, jobject jTableViewBase, jlong nativeTableViewPtr, jlong columnIndex, jlong rowIndex)
{
	return reinterpret_cast<TableView*>(nativeTableViewPtr)->
        get_date(static_cast<size_t>(columnIndex), static_cast<size_t>(rowIndex));
}

JNIEXPORT jstring JNICALL Java_com_tightdb_TableViewBase_nativeGetString(
	JNIEnv* env, jobject jTableView, jlong nativeViewPtr, jlong colIndex, jlong rowIndex)
{
	return env->NewStringUTF(reinterpret_cast<TableView*>(nativeViewPtr)->
        get_string(static_cast<size_t>(colIndex), static_cast<size_t>(rowIndex)));
}

JNIEXPORT jobject JNICALL Java_com_tightdb_TableViewBase_nativeGetBinary(
	JNIEnv* env, jobject jTableView, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex)
{	
	BinaryData data = reinterpret_cast<TableView*>(nativeViewPtr)->
        get_binary(static_cast<size_t>(columnIndex), static_cast<size_t>(rowIndex));
	return env->NewDirectByteBuffer((void*)data.pointer, data.len);
}

JNIEXPORT jbyteArray JNICALL Java_com_tightdb_TableViewBase_nativeGetByteArray(
	JNIEnv* env, jobject jTableView, jlong nativeTableViewPtr, jlong columnIndex, jlong rowIndex)
{
	BinaryData data = reinterpret_cast<TableView*>(nativeTableViewPtr)->
        get_binary(static_cast<size_t>(columnIndex), static_cast<size_t>(rowIndex));
	jbyteArray jresult = env->NewByteArray(data.len); 
	env->SetByteArrayRegion(jresult, 0, data.len, (const jbyte*)data.pointer);
	return jresult;
}

JNIEXPORT jobject JNICALL Java_com_tightdb_TableViewBase_nativeGetMixed(
	JNIEnv* env, jobject jTableView, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex)
{	
	Mixed value = reinterpret_cast<TableView*>(nativeViewPtr)->
        get_mixed(static_cast<size_t>(columnIndex), static_cast<size_t>(rowIndex));
	return CreateJMixedFromMixed(env, value);
}

JNIEXPORT jlong JNICALL Java_com_tightdb_TableViewBase_nativeGetSubTable(
	JNIEnv* env, jobject jTableView, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex)
{	
    Table* pSubTable = LangBindHelper::get_subtable_ptr(reinterpret_cast<TableView*>(nativeViewPtr), 
                                                        static_cast<size_t>(columnIndex), static_cast<size_t>(rowIndex));
	return reinterpret_cast<jlong>(pSubTable);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableViewBase_nativeSetLong(
	JNIEnv* env, jobject jTableView, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex, jlong value)
{	
	reinterpret_cast<TableView*>(nativeViewPtr)->
        set_int(static_cast<size_t>(columnIndex), static_cast<size_t>(rowIndex), value);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableViewBase_nativeSetBoolean(
	JNIEnv* env, jobject jTableView, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex, jboolean value)
{	
	reinterpret_cast<TableView*>(nativeViewPtr)->
        set_bool(static_cast<size_t>(columnIndex), static_cast<size_t>(rowIndex), value != 0 ? true : false);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableViewBase_nativeSetDateTimeValue(
	JNIEnv* env, jobject jTableView, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex, jlong dateTimeValue)
{
	reinterpret_cast<TableView*>(nativeViewPtr)->
        set_date(static_cast<size_t>(columnIndex), static_cast<size_t>(rowIndex), dateTimeValue);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableViewBase_nativeSetString(
	JNIEnv* env, jobject jTableView, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex, jstring value)
{	
	const char* valueCharPtr = env->GetStringUTFChars(value, NULL);
	reinterpret_cast<TableView*>(nativeViewPtr)->
        set_string(static_cast<size_t>(columnIndex), static_cast<size_t>(rowIndex), valueCharPtr);
	env->ReleaseStringUTFChars(value, valueCharPtr);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableViewBase_nativeSetBinary(
	JNIEnv* env, jobject jTableView, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex, jobject byteBuffer)
{	
	reinterpret_cast<TableView*>(nativeViewPtr)->
        set_binary(static_cast<size_t>(columnIndex), static_cast<size_t>(rowIndex), (char*)env->GetDirectBufferAddress(byteBuffer), static_cast<size_t>(env->GetDirectBufferCapacity(byteBuffer)));
}

JNIEXPORT void JNICALL Java_com_tightdb_TableViewBase_nativeSetByteArray(
	JNIEnv* env, jobject jTableView, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex, jbyteArray byteArray)
{
	jsize len = env->GetArrayLength(byteArray);
	jbyte* dataPtr = env->GetByteArrayElements(byteArray, NULL); 
	reinterpret_cast<TableView*>(nativeViewPtr)->
        set_binary(static_cast<size_t>(columnIndex), static_cast<size_t>(rowIndex), (char*)(dataPtr), static_cast<size_t>(len));
	env->ReleaseByteArrayElements(byteArray, dataPtr, 0);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableViewBase_nativeSetMixed(
	JNIEnv* env, jobject jTableView, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex, jobject jMixed)
{	
	TableView* pTableView = reinterpret_cast<TableView*>(nativeViewPtr);
	ColumnType mixedType = GetMixedObjectType(env, jMixed);
	switch (mixedType) {
	case COLUMN_TYPE_INT: 
		{
			jlong intValue = GetMixedIntValue(env, jMixed);
			pTableView->set_mixed(static_cast<size_t>(columnIndex), static_cast<size_t>(rowIndex), Mixed((int64_t)intValue));
			return;
		}
	case COLUMN_TYPE_BOOL:
		{
			jboolean boolValue = GetMixedBooleanValue(env, jMixed);
			pTableView->set_mixed(static_cast<size_t>(columnIndex), static_cast<size_t>(rowIndex), Mixed(boolValue != 0 ? true : false));
			return;
		}
	case COLUMN_TYPE_DATE:
		{
			jlong dateTimeValue = GetMixedDateTimeValue(env, jMixed);
			Date date(dateTimeValue);
			pTableView->set_mixed(static_cast<size_t>(columnIndex), static_cast<size_t>(rowIndex), Mixed(date));
			return;
		}
	case COLUMN_TYPE_STRING:
		{
			jstring stringValue = GetMixedStringValue(env, jMixed);
			const char* stringValueCharPtr = env->GetStringUTFChars(stringValue, NULL);
			pTableView->set_mixed(static_cast<size_t>(columnIndex), static_cast<size_t>(rowIndex), Mixed(stringValueCharPtr));
			env->ReleaseStringUTFChars(stringValue, stringValueCharPtr);
			return;
		}
	case COLUMN_TYPE_BINARY:
		{
			jobject jByteBuffer = GetMixedByteBufferValue(env, jMixed);

			//jbyteArray binaryDataArray = GetMixedByteArrayValue(env, jMixed);
			//jsize length = env->GetArrayLength(binaryDataArray);
			//jbyte* buf = new jbyte[length];
			//env->GetByteArrayRegion(binaryDataArray, 0, length, buf);
			BinaryData binaryData;
			binaryData.len = static_cast<size_t>(env->GetDirectBufferCapacity(jByteBuffer));
			binaryData.pointer = (const char*)(env->GetDirectBufferAddress(jByteBuffer));
			pTableView->set_mixed(static_cast<size_t>(columnIndex), static_cast<size_t>(rowIndex), Mixed(binaryData));
            //delete[] buf;
			return;
		}
	default:
		{
			printf("\nType not supported as of now: %d in function %s", static_cast<int>(mixedType), __FUNCTION__);
			return;
		}
	}
}

JNIEXPORT void JNICALL Java_com_tightdb_TableViewBase_nativeClear(
	JNIEnv* env, jobject jTableView, jlong nativeViewPtr)
{
	reinterpret_cast<TableView*>(nativeViewPtr)->clear();
}

JNIEXPORT void JNICALL Java_com_tightdb_TableViewBase_nativeRemoveRow(
	JNIEnv* env, jobject jTableView, jlong nativeViewPtr, jlong rowIndex)
{
	reinterpret_cast<TableView*>(nativeViewPtr)->remove(static_cast<size_t>(rowIndex));
}

JNIEXPORT jlong JNICALL Java_com_tightdb_TableViewBase_nativeFindFirst__JJJ(
	JNIEnv* env, jobject jTableView, jlong nativeViewPtr, jlong columnIndex, jlong value)
{	
	return static_cast<jlong>(reinterpret_cast<TableView*>(nativeViewPtr)->
        find_first_int(static_cast<size_t>(columnIndex), value));
}

JNIEXPORT jlong JNICALL Java_com_tightdb_TableViewBase_nativeFindFirst__JJLjava_lang_String_2(
	JNIEnv* env, jobject jTableViewObject, jlong nativeViewPtr, jlong columnIndex, jstring value)
{
	const char* valueCharPtr = env->GetStringUTFChars(value, NULL);
	size_t searchIndex = reinterpret_cast<TableView*>(nativeViewPtr)->
        find_first_string(static_cast<size_t>(columnIndex), valueCharPtr);
	env->ReleaseStringUTFChars(value, valueCharPtr);
	return static_cast<jlong>(searchIndex);
}

JNIEXPORT jlong JNICALL Java_com_tightdb_TableViewBase_nativeFindAll__JJJ(
	JNIEnv* env, jobject jTableViewBase, jlong nativeViewPtr, jlong columnIndex, jlong value)
{	
	TableView* pTableView = reinterpret_cast<TableView*>(nativeViewPtr);
	TableView* pResultView = new TableView(
        pTableView->find_all_int(static_cast<size_t>(columnIndex), value) );
	return reinterpret_cast<jlong>(pResultView);
}

JNIEXPORT jlong JNICALL Java_com_tightdb_TableViewBase_nativeFindAll__JJLjava_lang_String_2(
	JNIEnv* env, jobject jTableView, jlong nativeViewPtr, jlong columnIndex, jstring value)
{	
	TableView* pTableView = reinterpret_cast<TableView*>(nativeViewPtr);
	const char* valueCharPtr = env->GetStringUTFChars(value, NULL);
	TableView* pResultView = new TableView(
        pTableView->find_all_string(static_cast<size_t>(columnIndex), valueCharPtr) );
	env->ReleaseStringUTFChars(value, valueCharPtr);

	return reinterpret_cast<jlong>(pResultView);
}

JNIEXPORT jlong JNICALL Java_com_tightdb_TableViewBase_nativeSum(
	JNIEnv* env, jobject jTableView, jlong nativeViewPtr, jlong columnIndex)
{	
	return reinterpret_cast<TableView*>(nativeViewPtr)->
        sum(static_cast<size_t>(columnIndex));
}

JNIEXPORT jlong JNICALL Java_com_tightdb_TableViewBase_nativeMaximum(
	JNIEnv* env, jobject jTableView, jlong nativeViewPtr, jlong columnId)
{	
	return reinterpret_cast<TableView*>(nativeViewPtr)->
        maximum(static_cast<size_t>(columnId));
}

JNIEXPORT jlong JNICALL Java_com_tightdb_TableViewBase_nativeMinimum(
	JNIEnv* env, jobject jTableView, jlong nativeViewPtr, jlong columnId)
{	
	return reinterpret_cast<TableView*>(nativeViewPtr)->
        minimum(static_cast<size_t>(columnId));
}

JNIEXPORT void JNICALL Java_com_tightdb_TableViewBase_nativeSort(
	JNIEnv* env, jobject jTableView, jlong nativeViewPtr, jlong columnIndex, jboolean ascending)
{	
	reinterpret_cast<TableView*>(nativeViewPtr)->
        sort(static_cast<size_t>(columnIndex), ascending != 0 ? true : false);
}

JNIEXPORT jlong JNICALL Java_com_tightdb_TableViewBase_createNativeTableView(
	JNIEnv* env, jobject jTableView, jobject jTable, jlong nativeTablePtr)
{
    return reinterpret_cast<jlong>( new TableView() );
}

JNIEXPORT void JNICALL Java_com_tightdb_TableViewBase_nativeClose(
	JNIEnv* env, jobject jTableView, jlong nativeTableViewPtr)
{
	delete reinterpret_cast<TableView*>(nativeTableViewPtr);
}

// FIXME: Add support for Count, Average, Remove