/*
 * Copyright 2014 Realm Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include <tightdb/util/safe_int_ops.hpp>

#include "util.hpp"
#include "io_realm_internal_Group.h"

using namespace tightdb;
using std::string;

JNIEXPORT jlong JNICALL Java_io_realm_internal_Group_createNative__(
    JNIEnv* env, jobject)
{
    TR_ENTER()
    static_cast<void>(env);
    Group *ptr = new Group();
    TR((LOG_DEBUG, log_tag, "Group::createNative(): %p.", VOID_PTR(ptr)));
    return reinterpret_cast<jlong>(ptr);
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Group_createNative__Ljava_lang_String_2I(
    JNIEnv* env, jobject, jstring jFileName, jint mode, jbyteArray keyArray)
{
    TR_ENTER()

    Group* pGroup = 0;
    StringData file_name;
    try {
        JStringAccessor file_name_tmp(env, jFileName); // throws
        file_name = StringData(file_name_tmp);
        Group::OpenMode openmode;
        switch (mode) {
        case 0: openmode = Group::mode_ReadOnly; break;
        case 1: openmode = Group::mode_ReadWrite; break;
        case 2: openmode = Group::mode_ReadWriteNoCreate; break;
        default:
            TR((LOG_DEBUG, log_tag, "Invalid mode: %d", mode));
            ThrowException(env, IllegalArgument, "Group(): Invalid mode parameter.");
            return 0;
        }

        KeyBuffer key(env, keyArray);
#ifdef TIGHTDB_ENABLE_ENCRYPTION
        pGroup = new Group(file_name, key.data(), openmode);
#else
        pGroup = new Group(file_name, openmode);
#endif

        TR((LOG_DEBUG, log_tag, "group: %p", VOID_PTR(pGroup)))
        return reinterpret_cast<jlong>(pGroup);
    }
    CATCH_FILE(file_name)
    CATCH_STD()

    // Failed - cleanup
    if (pGroup)
        delete pGroup;
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Group_createNative___3B(
    JNIEnv* env, jobject, jbyteArray jData)
{
    TR_ENTER()
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

    TR((LOG_DEBUG, log_tag, " %d bytes.", byteArrayLength))
    Group* pGroup = 0;
    try {
        pGroup = new Group(BinaryData(reinterpret_cast<char*>(buf), S(byteArrayLength)), true);
        TR((LOG_DEBUG, log_tag, " groupPtr: %p", VOID_PTR(pGroup)));
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
JNIEXPORT jlong JNICALL Java_io_realm_internal_Group_createNative__Ljava_nio_ByteBuffer_2(
    JNIEnv* env, jobject, jobject jByteBuffer)
{
    TR_ENTER()
    BinaryData bin;
    if (!GetBinaryData(env, jByteBuffer, bin))
        return 0;
    TR((LOG_DEBUG, log_tag, " %d bytes.", bin.size()))

    Group* pGroup = 0;
    try {
        pGroup = new Group(BinaryData(bin.data(), bin.size()), false);
    }
    CATCH_FILE("memory-buffer")
    CATCH_STD()

    TR((LOG_DEBUG, log_tag, "%p", VOID_PTR(pGroup)))
    return reinterpret_cast<jlong>(pGroup);
}

JNIEXPORT void JNICALL Java_io_realm_internal_Group_nativeClose(
    JNIEnv*, jclass, jlong nativeGroupPtr)
{
    TR_ENTER_PTR(nativeGroupPtr)
    delete G(nativeGroupPtr);
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Group_nativeSize(
    JNIEnv*, jobject, jlong nativeGroupPtr)
{
    TR_ENTER_PTR(nativeGroupPtr)
    return static_cast<jlong>( G(nativeGroupPtr)->size() ); // noexcept
}

JNIEXPORT jboolean JNICALL Java_io_realm_internal_Group_nativeHasTable(
    JNIEnv* env, jobject, jlong nativeGroupPtr, jstring jTableName)
{
    TR_ENTER_PTR(nativeGroupPtr)
    try {
        JStringAccessor tableName(env, jTableName); // throws
        return G(nativeGroupPtr)->has_table(tableName);
    } CATCH_STD()
    return false;
}

JNIEXPORT jstring JNICALL Java_io_realm_internal_Group_nativeGetTableName(
    JNIEnv* env, jobject, jlong nativeGroupPtr, jint index)
{
    TR_ENTER_PTR(nativeGroupPtr)
    try {
        return to_jstring(env, G(nativeGroupPtr)->get_table_name(index));
    } CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Group_nativeGetTableNativePtr(
    JNIEnv *env, jobject, jlong nativeGroupPtr, jstring name)
{
    TR_ENTER_PTR(nativeGroupPtr)
    try {
        JStringAccessor tableName(env, name); // throws
        Table* pTable = LangBindHelper::get_or_add_table(*G(nativeGroupPtr), tableName);
        return (jlong)pTable;
    } CATCH_STD()
    return 0;
}

JNIEXPORT void JNICALL Java_io_realm_internal_Group_nativeWriteToFile(
    JNIEnv* env, jobject, jlong nativeGroupPtr, jstring jFileName, jbyteArray keyArray)
{
    TR_ENTER_PTR(nativeGroupPtr)
    StringData file_name;
    KeyBuffer key(env, keyArray);
    try {
        JStringAccessor file_name_tmp(env, jFileName); // throws
        file_name = StringData(file_name_tmp);
#ifdef TIGHTDB_ENABLE_ENCRYPTION
        G(nativeGroupPtr)->write(file_name_tmp), key.data());
#else
        G(nativeGroupPtr)->write(file_name);
#endif
    }
    CATCH_FILE(file_name)
    CATCH_STD()
}

JNIEXPORT jbyteArray JNICALL Java_io_realm_internal_Group_nativeWriteToMem(
    JNIEnv* env, jobject, jlong nativeGroupPtr)
{
    TR_ENTER_PTR(nativeGroupPtr)
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

JNIEXPORT jobject JNICALL Java_io_realm_internal_Group_nativeWriteToByteBuffer(
    JNIEnv* env, jobject, jlong nativeGroupPtr)
{
    TR_ENTER_PTR(nativeGroupPtr)
    BinaryData buffer;
    try {
        buffer = G(nativeGroupPtr)->write_to_mem();
        if (util::int_less_than_or_equal(buffer.size(), MAX_JLONG)) {
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

JNIEXPORT void JNICALL Java_io_realm_internal_Group_nativeCommit(
    JNIEnv*, jobject, jlong nativeGroupPtr)
{
    TR_ENTER()
    G(nativeGroupPtr)->commit();
}


JNIEXPORT jstring JNICALL Java_io_realm_internal_Group_nativeToJson(
    JNIEnv* env, jobject, jlong nativeGroupPtr)
{
    Group* grp = G(nativeGroupPtr);

    try {
        // Write group to string in JSON format
        std::ostringstream ss;
        ss.sync_with_stdio(false); // for performance
        grp->to_json(ss);
        const std::string str = ss.str();
        return to_jstring(env, str);
    } CATCH_STD()
    return 0;
}

JNIEXPORT jstring JNICALL Java_io_realm_internal_Group_nativeToString(
    JNIEnv* env, jobject, jlong nativeGroupPtr)
{
    Group* grp = G(nativeGroupPtr);
    try {
        // Write group to string
        std::ostringstream ss;
        ss.sync_with_stdio(false); // for performance
        grp->to_string(ss);
        const std::string str = ss.str();
        return to_jstring(env, str);
    } CATCH_STD()
    return 0;
}

JNIEXPORT jboolean JNICALL Java_io_realm_internal_Group_nativeEquals(
    JNIEnv* env, jobject, jlong nativeGroupPtr, jlong nativeGroupToComparePtr)
{
    Group* grp = G(nativeGroupPtr);
    Group* grpToCompare = G(nativeGroupToComparePtr);
    try {
        return (*grp == *grpToCompare);
    } CATCH_STD()
    return false;
}
