#include <string>
#include <jni.h>
#include <tightdb.hpp>
#include <tightdb/lang_bind_helper.hpp>

#include "util.h"
#include "com_tightdb_Group.h"

using namespace tightdb;

JNIEXPORT jlong JNICALL Java_com_tightdb_Group_createNative__(
	JNIEnv* env, jobject jGroup)
{	
	return reinterpret_cast<jlong>(new Group());
}

/* !!! TODO:
    Update interface :

enum GroupMode {
    GROUP_DEFAULT  =  0,
    GROUP_READONLY =  1,
    GROUP_SHARED   =  2,
    GROUP_APPEND   =  4,
    GROUP_ASYNC    =  8,
    GROUP_SWAPONLY = 16
};
*/

JNIEXPORT jlong JNICALL Java_com_tightdb_Group_createNative__Ljava_lang_String_2Z(
	JNIEnv* env, jobject jGroup, jstring jFileName, jboolean readOnly)
{	
	const char* fileNameCharPtr = env->GetStringUTFChars(jFileName, NULL);
	if (fileNameCharPtr == NULL) {
		printf("\nUnable to fetch the characters from the filename input: %s", __FUNCTION__);
		return NULL;
	}
	Group* pGroup = new Group(fileNameCharPtr, readOnly != 0 ? GROUP_READONLY : GROUP_DEFAULT);
	if (!pGroup->is_valid()) {
		delete pGroup;
		ThrowException(env, IllegalArgument, "Group(): File is not a valid tightdb database");
		return NULL;
	}
	return reinterpret_cast<jlong>(pGroup);
}

JNIEXPORT jlong JNICALL Java_com_tightdb_Group_createNative___3B(
	JNIEnv* env, jobject jGroup, jbyteArray jData)
{	
	jbyte* jbytePtr = env->GetByteArrayElements(jData, NULL);
	if (jbytePtr == NULL) {
		ThrowException(env, IllegalArgument, "Unable to fetch the buffer");
		return NULL;
	}
	jlong byteArrayLength = env->GetArrayLength(jData);     // CHECK, FIXME: Does this return a long?
	Group* pGroup = new Group((const char*)jbytePtr, static_cast<size_t>(byteArrayLength));
	if (!pGroup->is_valid()) {
	    delete pGroup;
        ThrowException(env, IllegalArgument, "Data is not a valid tightdb database");
	    return NULL;
	}
	return reinterpret_cast<jlong>(pGroup);
}

JNIEXPORT jlong JNICALL Java_com_tightdb_Group_createNative__Ljava_nio_ByteBuffer_2(
	JNIEnv* env, jobject jTableBase, jobject jByteBuffer)
{	
	Group* pGroup = new Group(static_cast<const char*>(env->GetDirectBufferAddress(jByteBuffer)), static_cast<size_t>(env->GetDirectBufferCapacity(jByteBuffer)));
	if (!(pGroup->is_valid())) {
		delete pGroup;
        ThrowException(env, IllegalArgument, "Data is not a valid tightdb database");
		return NULL;
	}
	return reinterpret_cast<jlong>(pGroup);
}

JNIEXPORT void JNICALL Java_com_tightdb_Group_nativeClose(
	JNIEnv* env, jobject jGroup, jlong nativeGroupPtr)
{	
	Group* pGroup = reinterpret_cast<Group*>(nativeGroupPtr);
	delete pGroup;
}

JNIEXPORT jboolean JNICALL Java_com_tightdb_Group_nativeIsValid(
	JNIEnv* env, jobject jGroup, jlong nativeGroupPtr)
{	
	Group* pGroup = reinterpret_cast<Group*>(nativeGroupPtr);
	return pGroup->is_valid();
}

// TODO: return long
JNIEXPORT jint JNICALL Java_com_tightdb_Group_nativeGetTableCount(
	JNIEnv *env, jobject jGroup, jlong nativeGroupPtr)
{	
	Group* pGroup = reinterpret_cast<Group*>(nativeGroupPtr);
    return pGroup->get_table_count(); 
}

JNIEXPORT jboolean JNICALL Java_com_tightdb_Group_nativeHasTable(
	JNIEnv* env, jobject jGroup, jlong nativeGroupPtr, jstring jTableName)
{	
	Group* pGroup = reinterpret_cast<Group*>(nativeGroupPtr);
	const char* tableNameCharPtr = env->GetStringUTFChars(jTableName, NULL);
    if (tableNameCharPtr) {
	    bool result = pGroup->has_table(tableNameCharPtr);
	    env->ReleaseStringUTFChars(jTableName, tableNameCharPtr);
	    return result;
    }
    // (exception is thrown by GetStringUTFChars if it fails.)
    return NULL;
}

JNIEXPORT jstring JNICALL Java_com_tightdb_Group_nativeGetTableName(
	JNIEnv* env, jobject jGroup, jlong nativeGroupPtr, jint index)
{	
	Group* pGroup = reinterpret_cast<Group*>(nativeGroupPtr);
	const char* nameCharPtr = pGroup->get_table_name(index);
	return env->NewStringUTF(nameCharPtr);
}

JNIEXPORT jlong JNICALL Java_com_tightdb_Group_nativeGetTableNativePtr(
	JNIEnv *env, jobject jGroup, jlong nativeGroupPtr, jstring name)
{	
	Group* pGroup = reinterpret_cast<Group*>(nativeGroupPtr);
	const char* tableNameCharPtr = env->GetStringUTFChars(name, NULL);
    if (tableNameCharPtr) {
	    Table* pTable = LangBindHelper::get_table_ptr(pGroup, tableNameCharPtr); 
	    env->ReleaseStringUTFChars(name, tableNameCharPtr);
	    return (jlong)pTable;
    }
    // (exception is thrown by GetStringUTFChars if it fails.)
    return 0;
}

JNIEXPORT void JNICALL Java_com_tightdb_Group_nativeWriteToFile(
	JNIEnv* env, jobject jGroup, jlong nativeGroupPtr, jstring jFileName)
{	
	const char* fileNameCharPtr = env->GetStringUTFChars(jFileName, NULL);
	if (fileNameCharPtr) {	
	    Group* pGroup = reinterpret_cast<Group*>(nativeGroupPtr);
	    bool success = pGroup->write(fileNameCharPtr);
        if (!success)
            ThrowException(env, IOFailed, fileNameCharPtr);
	    env->ReleaseStringUTFChars(jFileName, fileNameCharPtr);
    }
    // (exception is thrown by GetStringUTFChars if it fails.)
}

JNIEXPORT jbyteArray JNICALL Java_com_tightdb_Group_nativeWriteToMem(
	JNIEnv* env, jobject jGroup, jlong nativeGroupPtr)
{	
	Group* pGroup = reinterpret_cast<Group*>(nativeGroupPtr);
	size_t len;
	char* memValue = pGroup->write_to_mem(len);
    // TODO: handle write size long.
    jbyteArray jByteArray = env->NewByteArray(len);
	env->SetByteArrayRegion(jByteArray, 0, len, (const jbyte*)memValue);
	free(memValue);
	return jByteArray;
}

JNIEXPORT jobject JNICALL Java_com_tightdb_Group_nativeWriteToByteBuffer(
	JNIEnv* env, jobject jGroup, jlong nativeGroupPtr)
{	
	Group* pGroup = reinterpret_cast<Group*>(nativeGroupPtr);
	size_t len;
	char* memValue = pGroup->write_to_mem(len);
	return env->NewDirectByteBuffer(static_cast<void*>(memValue), static_cast<jlong>(len));
}

JNIEXPORT jboolean JNICALL Java_com_tightdb_Group_nativeCommit(
	JNIEnv* env, jobject jGroup, jlong nativeGroupPtrValue)
{
	Group* pGroup = reinterpret_cast<Group*>(nativeGroupPtrValue);
	return pGroup->commit();
}