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
	if (fileNameCharPtr == NULL)
		return NULL;

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
	Group* pGroup = new Group((const char*)jbytePtr, S(byteArrayLength));
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
    // !!! TODO: Check Buffer for NULL!!!
	Group* pGroup = new Group(static_cast<const char*>(env->GetDirectBufferAddress(jByteBuffer)), 
        S(env->GetDirectBufferCapacity(jByteBuffer)));
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
	delete G(nativeGroupPtr);
}

JNIEXPORT jboolean JNICALL Java_com_tightdb_Group_nativeIsValid(
	JNIEnv* env, jobject jGroup, jlong nativeGroupPtr)
{	
	return G(nativeGroupPtr)->is_valid();
}

JNIEXPORT jlong JNICALL Java_com_tightdb_Group_nativeGetTableCount(
	JNIEnv *env, jobject jGroup, jlong nativeGroupPtr)
{	
    return static_cast<jlong>( G(nativeGroupPtr)->get_table_count() ); 
}

JNIEXPORT jboolean JNICALL Java_com_tightdb_Group_nativeHasTable(
	JNIEnv* env, jobject jGroup, jlong nativeGroupPtr, jstring jTableName)
{	
	const char* tableNameCharPtr = env->GetStringUTFChars(jTableName, NULL);
    if (tableNameCharPtr) {
	    bool result = G(nativeGroupPtr)->has_table(tableNameCharPtr);
	    env->ReleaseStringUTFChars(jTableName, tableNameCharPtr);
	    return result;
    }
    // (exception is thrown by GetStringUTFChars if it fails.)
    return NULL;
}

JNIEXPORT jstring JNICALL Java_com_tightdb_Group_nativeGetTableName(
	JNIEnv* env, jobject jGroup, jlong nativeGroupPtr, jint index)
{	
	const char* nameCharPtr = G(nativeGroupPtr)->get_table_name(index);
	return env->NewStringUTF(nameCharPtr);
}

JNIEXPORT jlong JNICALL Java_com_tightdb_Group_nativeGetTableNativePtr(
	JNIEnv *env, jobject jGroup, jlong nativeGroupPtr, jstring name)
{	
	const char* tableNameCharPtr = env->GetStringUTFChars(name, NULL);
    if (tableNameCharPtr) {
	    Table* pTable = LangBindHelper::get_table_ptr(G(nativeGroupPtr), tableNameCharPtr); 
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
	    bool success = G(nativeGroupPtr)->write(fileNameCharPtr);
        if (!success)
            ThrowException(env, IOFailed, fileNameCharPtr);
	    env->ReleaseStringUTFChars(jFileName, fileNameCharPtr);
    }
    // (exception is thrown by GetStringUTFChars if it fails.)
}

JNIEXPORT jbyteArray JNICALL Java_com_tightdb_Group_nativeWriteToMem(
	JNIEnv* env, jobject jGroup, jlong nativeGroupPtr)
{	
	size_t len;
	char* memValue = G(nativeGroupPtr)->write_to_mem(len);
    jbyteArray jByteArray;
    if (len <= MAX_JSIZE) {
        jsize jlen = static_cast<jsize>(len);
        jByteArray = env->NewByteArray(jlen);
	    env->SetByteArrayRegion(jByteArray, 0, jlen, (const jbyte*)memValue);
    } else {
        jByteArray = NULL;
        ThrowException(env, IndexOutOfBounds, "Group too big to write.");
    }
    free(memValue); // Data was copied to array - so we can free data.
	return jByteArray;
}

JNIEXPORT jobject JNICALL Java_com_tightdb_Group_nativeWriteToByteBuffer(
	JNIEnv* env, jobject jGroup, jlong nativeGroupPtr)
{	
	size_t len;
	char* memValue = G(nativeGroupPtr)->write_to_mem(len);
	if (len <= MAX_JLONG) {
        return env->NewDirectByteBuffer(static_cast<void*>(memValue), static_cast<jlong>(len));
        // Data is NOT copied in DirectByteBuffer - so we can't free it.
    } else {
        ThrowException(env, IndexOutOfBounds, "Group too big to write.");
        return NULL;
    }
}

JNIEXPORT jboolean JNICALL Java_com_tightdb_Group_nativeCommit(
	JNIEnv* env, jobject jGroup, jlong nativeGroupPtr)
{
	return G(nativeGroupPtr)->commit();
}