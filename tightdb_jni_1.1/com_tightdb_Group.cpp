#include <jni.h>
#include <Group.h>
#include "com_tightdb_Group.h"
#include <string>
#include "util.h"

JNIEXPORT jlong JNICALL Java_com_tightdb_Group_createNativeTable(JNIEnv *env, jobject jGroup, jstring name){
	jlong groupId = GetNativePtrValue(env, jGroup);
	Group* pGroup = NULL;
	pGroup = *(Group **)&groupId;

	const char* tableNameCharPtr = tableNameCharPtr = env->GetStringUTFChars(name, NULL);
	if(tableNameCharPtr == NULL){
		printf("Not able to fetch the chars of the String: %s\n", __FUNCTION__);
		return NULL;
	}
	TopLevelTable* pTable = &(pGroup->GetTable(tableNameCharPtr));
	if(tableNameCharPtr != NULL){
		env->ReleaseStringUTFChars(name, tableNameCharPtr);
	}
	jlong longTableCPtr = 0;
	*(TopLevelTable **)&longTableCPtr = pTable; 
	return longTableCPtr;
}

JNIEXPORT jboolean JNICALL Java_com_tightdb_Group_nativeHasTable(JNIEnv* env, jobject jGroup, jstring jTableName){
	jlong ptrValue = GetNativePtrValue(env, jGroup);
	if(ptrValue == 0){
		char errbuf[1024];
		sprintf_s(errbuf, 1024, "Error fetching the native ptr value, JNI Error: %s", __FUNCTION__);
		env->ThrowNew(env->FindClass("java/lang/InternalError"), errbuf);
		return false;
	}
	Group *pGroup = (Group*) 0 ;
	pGroup = *(Group **)&ptrValue;
	const char* tableNameCharPtr = env->GetStringUTFChars(jTableName, NULL);
	bool result = pGroup->HasTable(tableNameCharPtr);
	if(tableNameCharPtr != NULL){
		env->ReleaseStringUTFChars(jTableName, tableNameCharPtr);
	}
	return result;
}

JNIEXPORT jstring JNICALL Java_com_tightdb_Group_nativeGetTableName(JNIEnv* env, jobject jGroup, jint index){
	jlong ptrValue = GetNativePtrValue(env, jGroup);
	Group* pGroup = 0;
	pGroup = *(Group**)&ptrValue;
	const char* nameCharPtr = pGroup->GetTableName(index);
	return env->NewStringUTF(nameCharPtr);
}

JNIEXPORT jobject JNICALL Java_com_tightdb_Group_nativeLoadFile(JNIEnv* env, jclass jGroupClass, jstring jFileName){
	const char* fileNameCharPtr = env->GetStringUTFChars(jFileName, NULL);
	if(fileNameCharPtr == NULL){
		printf("\nUnable to fetch the characters from the filename input: %s", __FUNCTION__);
		return NULL;
	}
	Group* pGroup = new Group(fileNameCharPtr);
	if(!pGroup->IsValid()){
		delete pGroup;
		env->ThrowNew(env->FindClass("java/lang/IllegalArgumentException"), "File is not a valid tightdb");
		return NULL;
	}
	jlong ptrValue = 0;
	*(Group **)&ptrValue = pGroup; 
	jmethodID jGroupConsID = env->GetMethodID(jGroupClass, "<init>", "(J)V");
	jobject jGroup = env->NewObject(jGroupClass, jGroupConsID, ptrValue);
	if(jGroup == NULL){
		printf("\nError creating object: %s", __FUNCTION__);
	}
	return jGroup;
}

JNIEXPORT jobject JNICALL Java_com_tightdb_Group_nativeGetTable(JNIEnv* env, jobject jGroup, jstring jTableName){
	const char* tableNameCharPtr = env->GetStringUTFChars(jTableName, NULL);
	if(tableNameCharPtr == NULL){
		return NULL;
	}
	jlong ptrValue = GetNativePtrValue(env, jGroup);
	Group *pGroup = (Group*) 0 ;
	pGroup = *(Group **)&ptrValue;
	if(!pGroup->HasTable(tableNameCharPtr)){
		env->ReleaseStringUTFChars(jTableName, tableNameCharPtr);
		return NULL;
	}
	TopLevelTable* pTable = &(pGroup->GetTable(tableNameCharPtr));
	env->ReleaseStringUTFChars(jTableName, tableNameCharPtr);
	//printf("\nNative Get Table count: %d", pTable->GetSize());
	jclass jTableClass = env->FindClass("com/tightdb/Table");
	jlong tablePtrValue = 0;
	*(TopLevelTable**)&tablePtrValue = pTable;
	jmethodID jTableConsID = env->GetMethodID(jTableClass, "<init>", "(J)V");
	jobject jTable = env->NewObject(jTableClass, jTableConsID, tablePtrValue);
	return jTable;
}

JNIEXPORT jint JNICALL Java_com_tightdb_Group_getTableCount
  (JNIEnv *env, jobject jobj){
	  jlong ptrValue = GetNativePtrValue(env, jobj);
	Group *group = (Group*) 0 ;
	group = *(Group **)&ptrValue;
	return group->GetTableCount(); 
}

JNIEXPORT void JNICALL Java_com_tightdb_Group_nativeWriteToFile(JNIEnv* env, jobject jGroup, jstring jFileName){
	const char* fileNameCharPtr = env->GetStringUTFChars(jFileName, NULL);
	if(fileNameCharPtr == NULL){
		env->ThrowNew(env->FindClass("java/lang/IOException"), "filename not valid");
		return;
	}
	jlong ptrValue = GetNativePtrValue(env, jGroup);
	Group *pGroup = (Group*) 0 ;
	pGroup = *(Group **)&ptrValue;
	pGroup->Write(fileNameCharPtr);
	
	env->ReleaseStringUTFChars(jFileName, fileNameCharPtr);
}

JNIEXPORT jobject JNICALL Java_com_tightdb_Group_nativeLoadData(JNIEnv* env, jclass jGroupClass, jbyteArray jData){
	jbyte* jbytePtr = env->GetByteArrayElements(jData, NULL);
	if(jbytePtr == NULL){
		env->ThrowNew(env->FindClass("java/lang/NullPointerException"), "Unable to fetch the buffer");
		return NULL;
	}
	jint byteArrayLength = env->GetArrayLength(jData);
	Group* pGroup = new Group((const char*)jbytePtr, byteArrayLength);
		if(!pGroup->IsValid()){
		delete pGroup;
		env->ThrowNew(env->FindClass("java/lang/IllegalArgumentException"), "Data is not a valid tightdb");
		return NULL;
	}
	jlong ptrValue = 0;
	*(Group **)&ptrValue = pGroup; 
	jmethodID jGroupConsID = env->GetMethodID(jGroupClass, "<init>", "(J)V");
	jobject jGroup = env->NewObject(jGroupClass, jGroupConsID, ptrValue);
	//printf("\nGroup native ptr value C++: %ld", ptrValue);
	if(jGroup == NULL){
		printf("\nError creating object: %s", __FUNCTION__);
	}
	return jGroup;
}

JNIEXPORT jbyteArray JNICALL Java_com_tightdb_Group_writeToBuffer(JNIEnv* env, jobject jGroup){
	jlong ptrValue = GetNativePtrValue(env, jGroup);
	Group* pGroup = (Group*)0;
	pGroup = *(Group**)&ptrValue;
	size_t len;
	char* memValue = pGroup->WriteToMem(len);
	jbyteArray jByteArray = env->NewByteArray(len);
	env->SetByteArrayRegion(jByteArray, 0, len, (const jbyte*)memValue);
	//printf("\nTODO we need to check whether we have to delete the memory");
	free(memValue);
	return jByteArray;
}

JNIEXPORT jlong JNICALL Java_com_tightdb_Group_createNative(JNIEnv* env, jclass jcls){
	Group* group = new Group();
	jlong jresult = 0;
	*(Group**)&jresult = group;
	return jresult;
}