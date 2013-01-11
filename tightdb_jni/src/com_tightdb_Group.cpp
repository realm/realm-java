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

JNIEXPORT jlong JNICALL Java_com_tightdb_Group_createNative__Ljava_lang_String_2Z(
	JNIEnv* env, jobject, jstring jFileName, jboolean readOnly)
{	
    TR((env, "Group::createNative(file): "));
    const char* fileNameCharPtr = env->GetStringUTFChars(jFileName, NULL);
    if (fileNameCharPtr == NULL)
        return 0;        // Exception is thrown by GetStringUTFChars()

    Group* pGroup;
    try {
        pGroup = new Group(fileNameCharPtr, readOnly != 0 ? Group::mode_ReadOnly : Group::mode_Normal);
    }
    catch (FileOpenError&) {
        ThrowException(env, IllegalArgument, "Group(): Invalid database file name.");
        return 0;
    }
    catch (...) {
        // FIXME: Should throw a Java exception.
        return 0;
    }
    TR((env, "%x\n", pGroup));
    return reinterpret_cast<jlong>(pGroup);
}

JNIEXPORT jlong JNICALL Java_com_tightdb_Group_createNative___3B(
	JNIEnv* env, jobject, jbyteArray jData)
{	
    TR((env, "Group::createNative(byteArray): "));
	jsize byteArrayLength = env->GetArrayLength(jData);
    if (byteArrayLength == 0)
        return 0;
    jbyte* buf = (jbyte*)malloc(S(byteArrayLength)*sizeof(jbyte));
    if (!buf)
        return 0;
    env->GetByteArrayRegion(jData, 0, byteArrayLength, buf);

    TR((env, " %d bytes.", byteArrayLength));
    Group* pGroup;
    try {
        pGroup = new Group(Group::BufferSpec(reinterpret_cast<char*>(buf), S(byteArrayLength)), true);
    }
    catch (InvalidDatabase&) {
        ThrowException(env, IllegalArgument, "Group(): Invalid tightdb database format.");
        // FIXME: Memory leak: 'buf' must be freed. Consider using a scoped dealloc guard.
        return 0;
    }
    catch (...) {
        // FIXME: Should throw a Java exception.
        return 0;
    }
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
    // FIXME: I added the const_cast<> because it had to be added after a const error was fixed in the core library. The necessity of the const_cast<> leads me to suspect that this function has something wrong about it. Maybe it should simply not use a BinaryData instance. Somebody should investigate. Consider also whether it is correct that ownership of the memory is transferred.
    Group* pGroup;
    try {
        pGroup = new Group(Group::BufferSpec(const_cast<char*>(data.pointer), data.len));
    }
    catch (InvalidDatabase&) {
        ThrowException(env, IllegalArgument, "Group(): Invalid tightdb database format.");
        return 0;
    }
    catch (...) {
        // FIXME: Should throw a Java exception.
        return 0;
    }
    TR((env, "%x\n", pGroup));
    return reinterpret_cast<jlong>(pGroup);
}

JNIEXPORT void JNICALL Java_com_tightdb_Group_nativeClose(
	JNIEnv* env, jobject, jlong nativeGroupPtr)
{	
    TR((env, "Group::nativeClose(%x)\n", nativeGroupPtr));
    Group* grp = G(nativeGroupPtr);
    delete grp;
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
        try {
            G(nativeGroupPtr)->write(fileNameCharPtr);
        }
        catch (...) {
            // FIXME: Thrown Java exception should probably contain information from the catched C++ exception
            ThrowException(env, IOFailed, fileNameCharPtr);
	    env->ReleaseStringUTFChars(jFileName, fileNameCharPtr);
        }
    }
    // (exception is thrown by GetStringUTFChars if it fails.)
}

JNIEXPORT jbyteArray JNICALL Java_com_tightdb_Group_nativeWriteToMem(
	JNIEnv* env, jobject, jlong nativeGroupPtr)
{	
    TR((env, "nativeWriteToMem(%x)\n", nativeGroupPtr));
    Group::BufferSpec buffer = G(nativeGroupPtr)->write_to_mem(); // FIXME: May throw
    jbyteArray jArray = NULL;
    if (buffer.m_size <= MAX_JSIZE) {
        jsize jlen = static_cast<jsize>(buffer.m_size);
        jArray = env->NewByteArray(jlen);
        if (jArray)
            // Copy data to Byte[]
	        env->SetByteArrayRegion(jArray, 0, jlen, (const jbyte*)buffer.m_data);
    } 
    if (!jArray) {
        ThrowException(env, IndexOutOfBounds, "Group too big to write.");
    }
    // FIXME: Deallocation must happen even if somthing fails above
    free(buffer.m_data); // free native data.
    return jArray;
}

JNIEXPORT jobject JNICALL Java_com_tightdb_Group_nativeWriteToByteBuffer(
	JNIEnv* env, jobject, jlong nativeGroupPtr)
{	
    TR((env, "nativeWriteToByteBuffer(%x)\n", nativeGroupPtr));
    Group::BufferSpec buffer = G(nativeGroupPtr)->write_to_mem(); // FIXME: May throw
    if (buffer.m_size <= MAX_JLONG) {
        return env->NewDirectByteBuffer(static_cast<void*>(buffer.m_data), static_cast<jlong>(buffer.m_size));
        // Data is NOT copied in DirectByteBuffer - so we can't free it.
    }
    else {
        ThrowException(env, IndexOutOfBounds, "Group too big to write.");
        return NULL;
    }
}
