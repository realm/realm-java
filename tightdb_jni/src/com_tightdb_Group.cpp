#include "util.h"
#include "com_tightdb_Group.h"

using namespace tightdb;

JNIEXPORT jlong JNICALL Java_com_tightdb_Group_createNative__(
	JNIEnv* env, jobject)
{	
	Group *ptr = new Group();
    TR((env, "Group::createNative(): %x.\n", ptr));
    return reinterpret_cast<jlong>(ptr);
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
inline bool groupIsValid(JNIEnv* env, Group* pGroup) {
    if (!pGroup->is_valid()) {
		delete pGroup;
		ThrowException(env, IllegalArgument, "Group(): Not a valid tightdb database");
		return false;
	}
    return true;
}

JNIEXPORT jlong JNICALL Java_com_tightdb_Group_createNative__Ljava_lang_String_2Z(
	JNIEnv* env, jobject, jstring jFileName, jboolean readOnly)
{	
    TR((env, "Group::createNative(file): "));
	const char* fileNameCharPtr = env->GetStringUTFChars(jFileName, NULL);
	if (fileNameCharPtr == NULL)
		return 0;        // Exception is thrown by GetStringUTFChars()

	Group* pGroup = new Group(fileNameCharPtr, readOnly != 0 ? GROUP_READONLY : GROUP_DEFAULT);
	if (!groupIsValid(env, pGroup))
        return 0;
    TR((env, "%x\n", pGroup));
	return reinterpret_cast<jlong>(pGroup);
}

JNIEXPORT jlong JNICALL Java_com_tightdb_Group_createNative___3B(
	JNIEnv* env, jobject, jbyteArray jData)
{	
    TR((env, "Group::createNative(byteArray): "));
	jbyte* jbytePtr = env->GetByteArrayElements(jData, NULL);
	if (jbytePtr == NULL) {
		ThrowException(env, IllegalArgument, "Unable to fetch the buffer");
		return 0;
	}
	jlong byteArrayLength = env->GetArrayLength(jData);     // CHECK, FIXME: Does this return a long?
    TR((env, " %d bytes. ", byteArrayLength));
    Group* pGroup = new Group((const char*)jbytePtr, S(byteArrayLength));
	if (!groupIsValid(env, pGroup))
        return 0;
    TR((env, "%x\n", pGroup));
	return reinterpret_cast<jlong>(pGroup);
}

JNIEXPORT jlong JNICALL Java_com_tightdb_Group_createNative__Ljava_nio_ByteBuffer_2(
	JNIEnv* env, jobject, jobject jByteBuffer)
{	
    TR((env, "Group::createNative(binaryData): "));
    BinaryData data;
    if (!GetBinaryData(env, jByteBuffer, data))
        return 0;
    TR((env, " %d bytes. ", data.len));
	Group* pGroup = new Group(data.pointer, data.len);
    if (!groupIsValid(env, pGroup))
        return 0;
    TR((env, "%x\n", pGroup));
	return reinterpret_cast<jlong>(pGroup);
}

JNIEXPORT void JNICALL Java_com_tightdb_Group_nativeClose(
	JNIEnv* env, jobject, jlong nativeGroupPtr)
{	
    TR((env, "Group::nativeClose(%x)\n", nativeGroupPtr));
	delete G(nativeGroupPtr);
}

JNIEXPORT jboolean JNICALL Java_com_tightdb_Group_nativeIsValid(
	JNIEnv*, jobject, jlong nativeGroupPtr)
{	
	return G(nativeGroupPtr)->is_valid();
}

JNIEXPORT jlong JNICALL Java_com_tightdb_Group_nativeGetTableCount(
	JNIEnv*, jobject, jlong nativeGroupPtr)
{	
    return static_cast<jlong>( G(nativeGroupPtr)->get_table_count() ); 
}

JNIEXPORT jboolean JNICALL Java_com_tightdb_Group_nativeHasTable(
	JNIEnv* env, jobject, jlong nativeGroupPtr, jstring jTableName)
{	
	const char* tableNameCharPtr = env->GetStringUTFChars(jTableName, NULL);
    if (tableNameCharPtr) {
	    bool result = G(nativeGroupPtr)->has_table(tableNameCharPtr);
	    env->ReleaseStringUTFChars(jTableName, tableNameCharPtr);
	    return result;
    }
    // (exception is thrown by GetStringUTFChars if it fails.)
    return false;
}

JNIEXPORT jstring JNICALL Java_com_tightdb_Group_nativeGetTableName(
	JNIEnv* env, jobject, jlong nativeGroupPtr, jint index)
{	
	const char* nameCharPtr = G(nativeGroupPtr)->get_table_name(index);
	return env->NewStringUTF(nameCharPtr);
}

JNIEXPORT jlong JNICALL Java_com_tightdb_Group_nativeGetTableNativePtr(
	JNIEnv *env, jobject, jlong nativeGroupPtr, jstring name)
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
	JNIEnv* env, jobject, jlong nativeGroupPtr, jstring jFileName)
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
	JNIEnv* env, jobject, jlong nativeGroupPtr)
{	
    TR((env, "nativeWriteToMem(%x)\n", nativeGroupPtr));
	size_t len;
	char* memBuf = G(nativeGroupPtr)->write_to_mem(len);
    jbyteArray jArray = NULL;
    if (len <= MAX_JSIZE) {
        jsize jlen = static_cast<jsize>(len);
        jArray = env->NewByteArray(jlen);
        if (jArray)
	        env->SetByteArrayRegion(jArray, 0, jlen, (const jbyte*)memBuf);
    } 
    if (!jArray) {
        ThrowException(env, IndexOutOfBounds, "Group too big to write.");
    }
    free(memBuf); // free native data.
	return jArray;
}

JNIEXPORT jobject JNICALL Java_com_tightdb_Group_nativeWriteToByteBuffer(
	JNIEnv* env, jobject, jlong nativeGroupPtr)
{	
    TR((env, "nativeWriteToByteBuffer(%x)\n", nativeGroupPtr));
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
