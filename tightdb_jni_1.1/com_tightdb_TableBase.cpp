#include <jni.h>
#include <Table.h>
#include <Group.h>
#include "mixedutil.h"
#include "com_tightdb_TableBase.h"
#include "util.h"
#include "ColumnTypeUtil.h"

JNIEXPORT void JNICALL Java_com_tightdb_TableBase_nativeRegisterColumn(JNIEnv* env, jobject jTable, jobject jColumnType, jstring jColumnName){
	jlong nativePtrLongValue = GetNativePtrValue(env, jTable);
	TopLevelTable* pTable = NULL;
	pTable = *(TopLevelTable**)&nativePtrLongValue;
	ColumnType columnType = GetColumnTypeFromJColumnType(env, jColumnType);
	const char* columnNameString = env->GetStringUTFChars(jColumnName, NULL);
	/*Spec tableSpec = pTable->GetSpec();
	tableSpec.AddColumn(columnType, columnNameString);
	pTable->UpdateFromSpec(tableSpec.GetRef());
	*/
	pTable->RegisterColumn(columnType, columnNameString);
}

JNIEXPORT jstring JNICALL Java_com_tightdb_TableBase_nativeGetString(JNIEnv* env, jobject jTable, jint colIndex, jint rowIndex){
	jlong nativePtrLongValue = GetNativePtrValue(env, jTable);
	TopLevelTable* pTable = NULL;
	pTable = *(TopLevelTable**)&nativePtrLongValue;
	const char* valueCharPtr = pTable->GetString(colIndex, rowIndex);
	return env->NewStringUTF(valueCharPtr);
}

JNIEXPORT jlong JNICALL Java_com_tightdb_TableBase_nativeGetLong(JNIEnv* env, jobject jTable, jint colIndex, jint rowIndex){
	jlong nativePtrLongValue = GetNativePtrValue(env, jTable);
	TopLevelTable* pTable = NULL;
	pTable = *(TopLevelTable**)&nativePtrLongValue;
	return pTable->Get(colIndex, rowIndex);
}

JNIEXPORT jboolean JNICALL Java_com_tightdb_TableBase_nativeGetBoolean(JNIEnv* env, jobject jTable, jint colIndex, jint rowIndex){
	jlong nativePtrLongValue = GetNativePtrValue(env, jTable);
	TopLevelTable* pTable = NULL;
	pTable = *(TopLevelTable**)&nativePtrLongValue;
	return pTable->GetBool(colIndex, rowIndex);
}

JNIEXPORT jbyteArray JNICALL Java_com_tightdb_TableBase_nativeGetBinaryData(JNIEnv* env, jobject jTable, jint columnIndex, jint rowIndex){
	jlong nativeTablePtrValue = GetNativePtrValue(env, jTable);
	TopLevelTable* pTopLevelTable = NULL;
	pTopLevelTable = *(TopLevelTable**)&nativeTablePtrValue;
	BinaryData data = pTopLevelTable->GetBinary(columnIndex, rowIndex);
	jbyteArray jresult = env->NewByteArray(data.len);
	env->SetByteArrayRegion(jresult, 0, data.len, (const jbyte*)data.pointer);
	printf("\nTODO: USE NIO BUFFER & delete data if required %s", __FUNCTION__);
	return jresult;
}

JNIEXPORT jint JNICALL Java_com_tightdb_TableBase_getColumnType(JNIEnv* env, jobject jTable, jint colIndex){
	jlong nativePtrLongValue = GetNativePtrValue(env, jTable);
	TopLevelTable* pTable = NULL;
	pTable = *(TopLevelTable**)&nativePtrLongValue;
	ColumnType colType = pTable->GetColumnType(colIndex);
	return static_cast<jint>(colType);
}

JNIEXPORT jint JNICALL Java_com_tightdb_TableBase_nativeGetColumnCount(JNIEnv* env, jobject jobj){
	jlong nativePtrLongValue = GetNativePtrValue(env, jobj);
	TopLevelTable* pTable = NULL;
	pTable = *(TopLevelTable**)&nativePtrLongValue;
	return pTable->GetColumnCount();
}

JNIEXPORT jstring JNICALL Java_com_tightdb_TableBase_nativeGetColumnName(JNIEnv* env, jobject jTable, jint conIndex){
	jlong nativePtrLongValue = GetNativePtrValue(env, jTable);
	TopLevelTable* pTable = NULL;
	pTable = *(TopLevelTable**)&nativePtrLongValue;
	const char* colName = pTable->GetColumnName(conIndex);
	return env->NewStringUTF(colName);
}

JNIEXPORT jint JNICALL Java_com_tightdb_TableBase_nativeGetColumnType(JNIEnv* env, jobject jTable, jint columnIndex){
	jlong nativePtrLongValue = GetNativePtrValue(env, jTable);
	TopLevelTable* pTable = NULL;
	pTable = *(TopLevelTable**)&nativePtrLongValue;
	ColumnType columnType = pTable->GetColumnType(columnIndex);
	return static_cast<int>(columnType);
}

JNIEXPORT jint JNICALL Java_com_tightdb_TableBase_nativeGetCount(JNIEnv* env, jobject jTable){
	jlong nativePtrLongValue = GetNativePtrValue(env, jTable);
	TopLevelTable* pTable = NULL;
	pTable = *(TopLevelTable**)&nativePtrLongValue;
	return pTable->GetSize();
}

JNIEXPORT void JNICALL Java_com_tightdb_TableBase_nativeSetString(JNIEnv* env, jobject jTable, jint colIndex, jint rowIndex, jstring value){
	const char* valueCharPtr = env->GetStringUTFChars(value, NULL);
	jlong nativePtrLongValue = GetNativePtrValue(env, jTable);
	TopLevelTable* pTable = NULL;
	pTable = *(TopLevelTable**)&nativePtrLongValue;
	pTable->SetString(colIndex, rowIndex, valueCharPtr);
	env->ReleaseStringUTFChars(value, valueCharPtr);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableBase_nativeSetLong(JNIEnv* env, jobject jTable, jint colIndex, jint rowIndex, jlong value){
	jlong nativePtrLongValue = GetNativePtrValue(env, jTable);
	TopLevelTable* pTable = NULL;
	pTable = *(TopLevelTable**)&nativePtrLongValue;
	return pTable->Set(colIndex, rowIndex, value);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableBase_nativeSetBoolean(JNIEnv* env, jobject jTable, jint colIndex, jint rowIndex, jboolean value){
	jlong nativePtrLongValue = GetNativePtrValue(env, jTable);
	TopLevelTable* pTable = NULL;
	pTable = *(TopLevelTable**)&nativePtrLongValue;
	return pTable->SetBool(colIndex, rowIndex, value == JNI_TRUE ? true : false);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableBase_nativeSetBinaryData(JNIEnv* env, jobject jTable, jint columnIndex, jint rowIndex, jbyteArray byteArray){
	jlong nativePtrLongValue = GetNativePtrValue(env, jTable);
	TopLevelTable* pTable = NULL;
	pTable = *(TopLevelTable**)&nativePtrLongValue;
	jbyte* buf = new jbyte[env->GetArrayLength(byteArray)];
	env->GetByteArrayRegion(byteArray, 0, env->GetArrayLength(byteArray), buf);
	pTable->SetBinary(columnIndex, rowIndex, (void*)buf, env->GetArrayLength(byteArray));
	delete[] buf;
}

JNIEXPORT void JNICALL Java_com_tightdb_TableBase_nativeInsertString(JNIEnv* env, jobject jTable, jint columnIndex, jint rowIndex, jstring value){
	jlong nativePtrLongValue = GetNativePtrValue(env, jTable);
	Table* pTable = NULL;
	pTable = *(Table**)&nativePtrLongValue;
	const char* valueCharPtr = env->GetStringUTFChars(value, NULL);
	pTable->InsertString(columnIndex, rowIndex, valueCharPtr);
	env->ReleaseStringUTFChars(value, valueCharPtr);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableBase_nativeInsertLong(JNIEnv* env, jobject jTable, jint columnIndex, jint rowIndex, jlong value){
	jlong nativePtrLongValue = GetNativePtrValue(env, jTable);
	Table* pTable = NULL;
	pTable = *(Table**)&nativePtrLongValue;
	pTable->InsertInt(columnIndex, rowIndex, value);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableBase_nativeInsertBoolean(JNIEnv* env, jobject jTable, jint columnIndex, jint rowIndex, jboolean value){
	jlong nativePtrLongValue = GetNativePtrValue(env, jTable);
	TopLevelTable* pTable = NULL;
	pTable = *(TopLevelTable**)&nativePtrLongValue;
	pTable->InsertBool(columnIndex, rowIndex, value != 0 ? true : false);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableBase_nativeInsertBinaryData(JNIEnv* env, jobject jTable, jint columnIndex, jint rowIndex, jbyteArray jData){
	jlong nativePtrLongValue = GetNativePtrValue(env, jTable);
	TopLevelTable* pTable = NULL;
	pTable = *(TopLevelTable**)&nativePtrLongValue;
	jsize len = env->GetArrayLength(jData);
	jbyte* buf = new jbyte[len];
	env->GetByteArrayRegion(jData, 0, len, buf);
	pTable->InsertBinary(columnIndex, rowIndex, (void*)buf, len);
	delete[] buf;
}

JNIEXPORT void JNICALL Java_com_tightdb_TableBase_nativeInsertDone(JNIEnv* env, jobject jTable){
	jlong nativePtrLongValue = GetNativePtrValue(env, jTable);
	TopLevelTable* pTable = NULL;
	pTable = *(TopLevelTable**)&nativePtrLongValue;
	pTable->InsertDone();
}

JNIEXPORT void JNICALL Java_com_tightdb_TableBase_nativeRemoveRow(JNIEnv* env, jobject jTable, jint rowIndex){
	jlong nativePtrLongValue = GetNativePtrValue(env, jTable);
	TopLevelTable* pTable = NULL;
	pTable = *(TopLevelTable**)&nativePtrLongValue;
	pTable->DeleteRow(rowIndex);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableBase_nativeClear(JNIEnv* env, jobject jobj){
	jlong nativePtrLongValue = GetNativePtrValue(env, jobj);
	TopLevelTable* pTable = NULL;
	pTable = *(TopLevelTable**)&nativePtrLongValue;
	pTable->Clear();
}

JNIEXPORT jlong JNICALL Java_com_tightdb_TableBase_createNative(JNIEnv* env, jobject jTable){
	TopLevelTable* pTable = new TopLevelTable();
	jlong jresult = 0;
	*(Table**)&jresult = pTable;
	return jresult;
}

JNIEXPORT void JNICALL Java_com_tightdb_TableBase_executeNative(JNIEnv* env, jclass jTableClass){
	Table* pTable = new Table();
	pTable->RegisterColumn(COLUMN_TYPE_STRING, "Name");
	pTable->RegisterColumn(COLUMN_TYPE_INT, "Age");
	pTable->RegisterColumn(COLUMN_TYPE_BOOL, "Hired");

	int count = 100000;
	char buf[1024];
	for(int i=0; i<count; i++){
		sprintf_s(buf, 1024, "Employee_%d", i);
		int age = (30 + i) % 60;
		int hired = (i % 2) == 0 ? true : false;
		pTable->InsertString(0, i, buf);
		pTable->InsertInt(1, i, age);
		pTable->InsertBool(2, i, hired);
		pTable->InsertDone();
	}
	printf("\nTable size: %d", pTable->GetSize());
}