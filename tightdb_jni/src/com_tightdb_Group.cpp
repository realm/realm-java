#include "util.hpp"
#include "com_tightdb_Group.h"

using namespace tightdb;
using std::string;

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
        TR((env, "%x\n", pGroup));
        return reinterpret_cast<jlong>(pGroup);
    }
    CATCH_FILE(fileNameCharPtr)
    CATCH_STD()

    // Failed - cleanup
    if (pGroup) 
        delete pGroup;
    return 0;
}

JNIEXPORT jlong JNICALL Java_com_tightdb_Group_createNative___3B(
    JNIEnv* env, jobject, jbyteArray jData)
{
    TR((env, "Group::createNative(byteArray): "));
    // Copy the group buffer given
    jsize byteArrayLength = env->GetArrayLength(jData);
    if (byteArrayLength == 0)
        return 0;
    jbyte* buf = static_cast<jbyte*>(malloc(S(byteArrayLength)*sizeof(jbyte)));
    if (!buf) {
        ThrowException(env, OutOfMemory, "copying the group buffer.");
        return 0;
    }
    env->GetByteArrayRegion(jData, 0, byteArrayLength, buf);

    TR((env, " %d bytes.", byteArrayLength));
    Group* pGroup = 0;
    try {
        pGroup = new Group(BinaryData(reinterpret_cast<char*>(buf), S(byteArrayLength)), true);
        TR((env, " groupPtr: %x\n", pGroup));
        return reinterpret_cast<jlong>(pGroup);
    }
    CATCH_FILE("memory-buffer")
    CATCH_STD()

    // Failed - cleanup
    if (buf)
        free(buf);
    return 0;
}

// FIXME: Remove this method? It's dangerous to not own the group data...
JNIEXPORT jlong JNICALL Java_com_tightdb_Group_createNative__Ljava_nio_ByteBuffer_2(
    JNIEnv* env, jobject, jobject jByteBuffer)
{
    TR((env, "Group::createNative(binaryData): "));
    BinaryData bin;
    if (!GetBinaryData(env, jByteBuffer, bin))
        return 0;
    TR((env, " %d bytes. ", bin.size()));

    Group* pGroup = 0;
    try {
        pGroup = new Group(BinaryData(bin.data(), bin.size()), false);
    }
    CATCH_FILE("memory-buffer")
    CATCH_STD()

    TR((env, "%x\n", pGroup));
    return reinterpret_cast<jlong>(pGroup);
}

JNIEXPORT void JNICALL Java_com_tightdb_Group_nativeClose(
    JNIEnv* env, jobject, jlong nativeGroupPtr)
{
    TR((env, "Group::nativeClose(%x)\n", nativeGroupPtr));
    delete G(nativeGroupPtr);
}

JNIEXPORT jlong JNICALL Java_com_tightdb_Group_nativeSize(
    JNIEnv*, jobject, jlong nativeGroupPtr)
{
    return static_cast<jlong>( G(nativeGroupPtr)->size() ); // noexcept
}

JNIEXPORT jboolean JNICALL Java_com_tightdb_Group_nativeHasTable(
    JNIEnv* env, jobject, jlong nativeGroupPtr, jstring jTableName)
{
    JStringAccessor tableName(env, jTableName);
    if (tableName) {
        try {
            return G(nativeGroupPtr)->has_table(tableName);
        } CATCH_STD()
    }
    return false;
}

JNIEXPORT jstring JNICALL Java_com_tightdb_Group_nativeGetTableName(
    JNIEnv* env, jobject, jlong nativeGroupPtr, jint index)
{
    try {
        return to_jstring(env, G(nativeGroupPtr)->get_table_name(index));
    } CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_com_tightdb_Group_nativeGetTableNativePtr(
    JNIEnv *env, jobject, jlong nativeGroupPtr, jstring name)
{
    JStringAccessor tableName(env, name);
    if (tableName) {
        try {
            Table* pTable = LangBindHelper::get_table_ptr(G(nativeGroupPtr), tableName);
            return (jlong)pTable;
        } CATCH_STD()
    }
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
        CATCH_FILE(fileNameCharPtr)
        CATCH_STD()
        
        env->ReleaseStringUTFChars(jFileName, fileNameCharPtr);
    }
    // (exception is thrown by GetStringUTFChars if it fails.)
}

JNIEXPORT jbyteArray JNICALL Java_com_tightdb_Group_nativeWriteToMem(
    JNIEnv* env, jobject, jlong nativeGroupPtr)
{
    TR((env, "nativeWriteToMem(%x)\n", nativeGroupPtr));
    BinaryData buffer;
    char* bufPtr = 0;
    try {
        buffer = G(nativeGroupPtr)->write_to_mem(); // throws
        bufPtr = const_cast<char*>(buffer.data());
        // Copy the data to Java array, so Java owns it.
        jbyteArray jArray = 0;
        if (buffer.size() <= MAX_JSIZE) {
            jsize jlen = static_cast<jsize>(buffer.size());
            jArray = env->NewByteArray(jlen);
            if (jArray)
                // Copy data to Byte[]
                env->SetByteArrayRegion(jArray, 0, jlen, reinterpret_cast<const jbyte*>(bufPtr));
                // SetByteArrayRegion() may throw ArrayIndexOutOfBoundsException - logic error
        }
        if (!jArray) {
            ThrowException(env, IndexOutOfBounds, "Group too big to copy and write.");
        }
        free(bufPtr);
        return jArray;
    } 
    CATCH_STD()
    if (bufPtr)
        free(bufPtr);
    return 0;
}

JNIEXPORT jobject JNICALL Java_com_tightdb_Group_nativeWriteToByteBuffer(
    JNIEnv* env, jobject, jlong nativeGroupPtr)
{
    TR((env, "nativeWriteToByteBuffer(%x)\n", nativeGroupPtr));
    BinaryData buffer;
    try {
        buffer = G(nativeGroupPtr)->write_to_mem();
        if (buffer.size() <= MAX_JLONG) {
            return env->NewDirectByteBuffer(const_cast<char*>(buffer.data()), static_cast<jlong>(buffer.size()));
            // Data is now owned by the Java DirectByteBuffer - so we must not free it.
        }
        else {
            ThrowException(env, IndexOutOfBounds, "Group too big to write.");
            return NULL;
        }
    }
    CATCH_STD()
    return NULL;
}

JNIEXPORT jstring JNICALL Java_com_tightdb_Group_nativeToJson(
    JNIEnv* env, jobject, jlong nativeGroupPtr)
{
    Group* grp = G(nativeGroupPtr);

    try {
        // Write group to string in JSON format
        std::ostringstream ss;
        ss.sync_with_stdio(false); // for performance
        grp->to_json(ss);
        const std::string str = ss.str();
        return env->NewStringUTF(str.c_str());
    } CATCH_STD()
    return 0;
}

JNIEXPORT jstring JNICALL Java_com_tightdb_Group_nativeToString(
    JNIEnv* env, jobject, jlong nativeGroupPtr)
{
    Group* grp = G(nativeGroupPtr);
    try {
        // Write group to string
        std::ostringstream ss;
        ss.sync_with_stdio(false); // for performance
        grp->to_string(ss);
        const std::string str = ss.str();
        return env->NewStringUTF(str.c_str());
    } CATCH_STD()
    return 0;
}

JNIEXPORT jboolean JNICALL Java_com_tightdb_Group_nativeEquals(
    JNIEnv* env, jobject, jlong nativeGroupPtr, jlong nativeGroupToComparePtr)
{
    Group* grp = G(nativeGroupPtr);
    Group* grpToCompare = G(nativeGroupToComparePtr);
    try {
        return (grp == grpToCompare);
    } CATCH_STD()
    return false;
}
