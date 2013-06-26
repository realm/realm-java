#include "util.hpp"
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

    Group* pGroup = 0;
    try {
        pGroup = new Group(fileNameCharPtr, readOnly != 0 ? Group::mode_ReadOnly : Group::mode_ReadWrite);
    }
    catch (...) {
        // FIXME: Diffrent exception types mean different things. More
        // details must be made available. We should proably have
        // special catches for at least these:
        // tightdb::File::AccessError (and various derivatives),
        // tightdb::ResourceAllocError, std::bad_alloc. In general,
        // any core library function or operator that is not declared
        // 'noexcept' must be considered as being able to throw
        // anything derived from std::exception.
        ThrowException(env, IllegalArgument, "Group(): Invalid database file name.");
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
    jbyte* buf = static_cast<jbyte*>(malloc(S(byteArrayLength)*sizeof(jbyte)));
    if (!buf)
        return 0; // FIXME: Should throw a Java exception here
    env->GetByteArrayRegion(jData, 0, byteArrayLength, buf);

    TR((env, " %d bytes.", byteArrayLength));
    Group* pGroup = 0;
    try {
        pGroup = new Group(BinaryData(reinterpret_cast<char*>(buf), S(byteArrayLength)), true);
    }
    catch (...) {
        // FIXME: Diffrent exception types mean different things. More
        // details must be made available. We should proably have
        // special catches for at least these:
        // tightdb::File::AccessError (and various derivatives),
        // tightdb::ResourceAllocError, std::bad_alloc. In general,
        // any core library function or operator that is not declared
        // 'noexcept' must be considered as being able to throw
        // anything derived from std::exception.
        ThrowException(env, IllegalArgument, "Group(): Invalid tightdb database format.");
        // FIXME: Memory leak here: 'buf' must be freed. Consider using a
        // scoped dealloc guard for safety.
    }
    TR((env, "%x\n", pGroup));
    return reinterpret_cast<jlong>(pGroup);
}

JNIEXPORT jlong JNICALL Java_com_tightdb_Group_createNative__Ljava_nio_ByteBuffer_2(
    JNIEnv* env, jobject, jobject jByteBuffer)
{
    TR((env, "Group::createNative(binaryData): "));
    BinaryData bin;
    if (!GetBinaryData(env, jByteBuffer, bin))
        return 0;
    TR((env, " %d bytes. ", bin.size()));
    // FIXME: Consider whether it is correct that ownership
    // of the memory is transferred. If it should indeed be
    // transferred, then the buffer must be explicitely deallocated
    // when the new-operator or the Group constructor fails.
    Group* pGroup = 0;
    try {
        pGroup = new Group(BinaryData(bin.data(), bin.size()));
    }
    catch (...) {
        // FIXME: Diffrent exception types mean different things. More
        // details must be made available. We should proably have
        // special catches for at least these:
        // tightdb::File::AccessError (and various derivatives),
        // tightdb::ResourceAllocError, std::bad_alloc. In general,
        // any core library function or operator that is not declared
        // 'noexcept' must be considered as being able to throw
        // anything derived from std::exception.
        ThrowException(env, IllegalArgument, "Group(): Invalid tightdb database format.");
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

JNIEXPORT jlong JNICALL Java_com_tightdb_Group_nativeSize(
    JNIEnv*, jobject, jlong nativeGroupPtr)
{
    return static_cast<jlong>( G(nativeGroupPtr)->size() );
}

JNIEXPORT jboolean JNICALL Java_com_tightdb_Group_nativeHasTable(
    JNIEnv* env, jobject, jlong nativeGroupPtr, jstring jTableName)
{
    JStringAccessor tableName(env, jTableName);
    if (tableName) {
        bool result = G(nativeGroupPtr)->has_table(tableName);
        return result;
    }
    // (exception is thrown by GetStringUTFChars if it fails.)
    return false;
}

JNIEXPORT jstring JNICALL Java_com_tightdb_Group_nativeGetTableName(
    JNIEnv* env, jobject, jlong nativeGroupPtr, jint index)
{
    return to_jstring(env, G(nativeGroupPtr)->get_table_name(index));
}

JNIEXPORT jlong JNICALL Java_com_tightdb_Group_nativeGetTableNativePtr(
    JNIEnv *env, jobject, jlong nativeGroupPtr, jstring name)
{
    JStringAccessor tableName(env, name);
    if (tableName) {
        Table* pTable = LangBindHelper::get_table_ptr(G(nativeGroupPtr), tableName);
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
            // FIXME: Diffrent exception types mean different
            // things. More details must be made available. We should
            // proably have special catches for at least these:
            // tightdb::File::AccessError (and various derivatives),
            // tightdb::ResourceAllocError, std::bad_alloc. In
            // general, any core library function or operator that is
            // not declared 'noexcept' must be considered as being
            // able to throw anything derived from std::exception.
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
    try {
        BinaryData buffer = G(nativeGroupPtr)->write_to_mem(); // FIXME: May throw at least std::bad_alloc
        jbyteArray jArray = 0;
        if (buffer.size() <= MAX_JSIZE) {
            jsize jlen = static_cast<jsize>(buffer.size());
            jArray = env->NewByteArray(jlen);
            if (jArray)
                // Copy data to Byte[]
                env->SetByteArrayRegion(jArray, 0, jlen, reinterpret_cast<const jbyte*>(buffer.data()));
        }
        if (!jArray) {
            ThrowException(env, IndexOutOfBounds, "Group too big to write.");
        }
        // FIXME: Deallocation must happen even if somthing fails above
        free(const_cast<char*>(buffer.data())); // free native data.
        return jArray;
    } catch (std::exception& e) {
        ThrowException(env, IOFailed, e.what());
    }
    return 0;
}

JNIEXPORT jobject JNICALL Java_com_tightdb_Group_nativeWriteToByteBuffer(
    JNIEnv* env, jobject, jlong nativeGroupPtr)
{
    TR((env, "nativeWriteToByteBuffer(%x)\n", nativeGroupPtr));
    BinaryData buffer = G(nativeGroupPtr)->write_to_mem(); // FIXME: May throw at least std::bad_alloc
    if (buffer.size() <= MAX_JLONG) {
        return env->NewDirectByteBuffer(const_cast<char*>(buffer.data()), static_cast<jlong>(buffer.size()));
        // Data is NOT copied in DirectByteBuffer - so we can't free it.
    }
    else {
        ThrowException(env, IndexOutOfBounds, "Group too big to write.");
        return NULL;
    }
}
