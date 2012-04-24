#include <jni.h>
#include "util.h"
#include "com_tightdb_TableViewBase.h"
#include <Table.h>

JNIEXPORT jlong JNICALL Java_com_tightdb_TableViewBase_createNativeTableView(JNIEnv* env, jobject jTableView, jobject jTable){
	jlong tableNativePtrValue = GetNativePtrValue(env, jTable);
	Table* pTable = NULL;
	pTable = *(Table**)&tableNativePtrValue;
	TableView* pTableView = new TableView(*pTable);
	jlong tableViewNativePtrValue = 0;
	*(TableView**)&tableViewNativePtrValue = pTableView;
	return tableViewNativePtrValue;
}

JNIEXPORT jint JNICALL Java_com_tightdb_TableViewBase_nativeGetCount(JNIEnv* env, jobject jTableView){
	jlong tableViewNativePtrValue = GetNativePtrValue(env, jTableView);

	TableView* pTableView = NULL;
	pTableView = *(TableView**)&tableViewNativePtrValue;
	return pTableView->GetSize();
}

JNIEXPORT void JNICALL Java_com_tightdb_TableViewBase_nativeRemoveRow(JNIEnv* env, jobject jTableView, jint rowIndex){
	jlong tableViewNativePtrValue = GetNativePtrValue(env, jTableView);
	TableView* pTableView = NULL;
	pTableView = *(TableView**)&tableViewNativePtrValue;
	pTableView->Delete(rowIndex);
}

JNIEXPORT jlong JNICALL Java_com_tightdb_TableViewBase_nativeGetLong(JNIEnv* env, jobject jTableView, jint colIndex, jint rowIndex){
	jlong tableViewNativePtrValue = GetNativePtrValue(env, jTableView);
	TableView* pTableView = NULL;
	pTableView = *(TableView**)&tableViewNativePtrValue;
	return pTableView->Get(colIndex, rowIndex);
}

JNIEXPORT jboolean JNICALL Java_com_tightdb_TableViewBase_nativeGetBoolean(JNIEnv* env, jobject jTableView, jint colIndex, jint rowIndex){
	jlong tableViewNativePtrValue = GetNativePtrValue(env, jTableView);
	TableView* pTableView = NULL;
	pTableView = *(TableView**)&tableViewNativePtrValue;
	return pTableView->GetBool(colIndex, rowIndex);
}

JNIEXPORT jstring JNICALL Java_com_tightdb_TableViewBase_nativeGetString(JNIEnv* env, jobject jTableView, jint colIndex, jint rowIndex){
	jlong tableViewNativePtrValue = GetNativePtrValue(env, jTableView);
	TableView* pTableView = NULL;
	pTableView = *(TableView**)&tableViewNativePtrValue;
	const char* returnCharPtr = pTableView->GetString(colIndex, rowIndex);
	return env->NewStringUTF(returnCharPtr);
}

JNIEXPORT jbyteArray JNICALL Java_com_tightdb_TableViewBase_nativeGetBinaryData(JNIEnv* env, jobject jTableView, jint columnIndex, jint rowIndex){
	jlong tableViewNativePtrValue = GetNativePtrValue(env, jTableView);
	TableView* pTableView = NULL;
	pTableView = *(TableView**)&tableViewNativePtrValue;
	//pTableView->Get
	printf("\nTableView Get binary is not supported yet");
	return NULL;
}

JNIEXPORT void JNICALL Java_com_tightdb_TableViewBase_nativeSetLong(JNIEnv* env, jobject jTableView, jint columnIndex, jint rowIndex, jlong value){
	jlong tableViewPtrValue = GetNativePtrValue(env, jTableView);
	TableView* pTableView = NULL;
	pTableView = *(TableView**)&tableViewPtrValue;
	pTableView->Set(columnIndex, rowIndex, value);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableViewBase_nativeSetBoolean(JNIEnv* env, jobject jTableView, jint columnIndex, jint rowIndex, jboolean value){
	jlong tableViewNativePtrValue = GetNativePtrValue(env, jTableView);
	TableView* pTableView = NULL;
	pTableView = *(TableView**)&tableViewNativePtrValue;
	pTableView->SetBool(columnIndex, rowIndex, value != 0 ? true : false);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableViewBase_nativeSetString(JNIEnv* env, jobject jTableView, jint columnIndex, jint rowIndex, jstring value){
	jlong tableViewNativePtrValue = GetNativePtrValue(env, jTableView);
	const char* valueCharPtr = env->GetStringUTFChars(value, NULL);
	TableView* pTableView = NULL;
	pTableView = *(TableView**)&tableViewNativePtrValue;
	pTableView->SetString(columnIndex, rowIndex, valueCharPtr);
	env->ReleaseStringUTFChars(value, valueCharPtr);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableViewBase_nativeSetBinaryData(JNIEnv* env, jobject jTableView, jint columnIndex, jint rowIndex, jbyteArray data){
	printf("\nTableView setBinary is not supported yet");
}

JNIEXPORT jlong JNICALL Java_com_tightdb_TableViewBase_nativeSum(JNIEnv* env, jobject jTableView, jint columnIndex){
	jlong tableViewNativePtrValue = GetNativePtrValue(env, jTableView);
	TableView* pTableView = NULL;
	pTableView = *(TableView**)&tableViewNativePtrValue;
	return pTableView->Sum(columnIndex);
}

JNIEXPORT jlong JNICALL Java_com_tightdb_TableViewBase_nativeMax(JNIEnv* env, jobject jTableView, jint columnId){
	jlong tableViewNativePtrValue = GetNativePtrValue(env, jTableView);
	TableView* pTableView = NULL;
	pTableView = *(TableView**)&tableViewNativePtrValue;
	return pTableView->Max(columnId);
}

JNIEXPORT jlong JNICALL Java_com_tightdb_TableViewBase_nativeMin(JNIEnv* env, jobject jTableView, jint columnId){
	jlong tableViewNativePtrValue = GetNativePtrValue(env, jTableView);
	TableView* pTableView = NULL;
	pTableView = *(TableView**)&tableViewNativePtrValue;
	return pTableView->Min(columnId);
}