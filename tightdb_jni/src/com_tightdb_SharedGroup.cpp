#include <iostream>

#include <jni.h>

#include <tightdb/group_shared.hpp>

#include "util.hpp"
#include "com_tightdb_SharedGroup.h"

using namespace std;
using namespace tightdb;

#define SG(ptr) reinterpret_cast<SharedGroup*>(ptr)

JNIEXPORT jlong JNICALL Java_com_tightdb_SharedGroup_createNative(
    JNIEnv* env, jobject, jstring file_name, jint durability, jboolean no_create, jboolean enable_replication)
{
    const char* file_name_ptr = env->GetStringUTFChars(file_name, 0);
    if (!file_name_ptr)
        return 0; // Exception is thrown by GetStringUTFChars()

    SharedGroup* db = 0;
    try {
        if (enable_replication) {
#ifdef TIGHTDB_ENABLE_REPLICATION
            ThrowException(env, UnsupportedOperation,
                           "Replication is not currently supported by the Java language binding.");
//            db = new SharedGroup(SharedGroup::replication_tag(), *file_name_ptr ? file_name_ptr : 0);
#else
            ThrowException(env, UnsupportedOperation,
                           "Replication was disabled in the native library at compile time.");
#endif
        }
        else {
            SharedGroup::DurabilityLevel level;
            if (durability == 0)
                level = SharedGroup::durability_Full;
            else if (durability == 1)
                level = SharedGroup::durability_MemOnly;
            else if (durability == 2)
#ifdef _WIN32
                level = SharedGroup::durability_Full;   // For Windows, use Full instead of Async
#else
                level = SharedGroup::durability_Async;
#endif
            else {
                ThrowException(env, UnsupportedOperation, "Unsupported durability.");
                return 0;
            }
            db = new SharedGroup(file_name_ptr, no_create!=0, level);
        }
        return reinterpret_cast<jlong>(db);
    }
    catch (SharedGroup::PresumablyStaleLockFile& e) {
        ThrowException(env, FileAccessError, e.what(), " Presumably a stall .lock file is present.");
    }
    catch (SharedGroup::LockFileButNoData& e) {
        ThrowException(env, FileAccessError, e.what(), "The database file is missing, but a .lock file is present.");
    }
    CATCH_FILE(file_name_ptr)
    CATCH_STD()
    return 0;
}

JNIEXPORT void JNICALL Java_com_tightdb_SharedGroup_nativeClose(
    JNIEnv*, jobject, jlong native_ptr)
{
    delete SG(native_ptr);
}

JNIEXPORT void JNICALL Java_com_tightdb_SharedGroup_nativeReserve(
   JNIEnv *env, jobject, jlong native_ptr, jlong bytes)
{
    try {
         SG(native_ptr)->reserve(bytes);
    }
    CATCH_STD()
}

JNIEXPORT jlong JNICALL Java_com_tightdb_SharedGroup_nativeBeginRead(
    JNIEnv* env, jobject, jlong native_ptr)
{
    try {
        const Group& group = SG(native_ptr)->begin_read();
        return reinterpret_cast<jlong>(&group);
    }
    CATCH_STD()
    return 0;
}

JNIEXPORT void JNICALL Java_com_tightdb_SharedGroup_nativeEndRead(
    JNIEnv *, jobject, jlong native_ptr)
{
    SG(native_ptr)->end_read();     // noexcept
}

JNIEXPORT jlong JNICALL Java_com_tightdb_SharedGroup_nativeBeginWrite(
    JNIEnv* env, jobject, jlong native_ptr)
{
    try {
        Group& group = SG(native_ptr)->begin_write();
        return reinterpret_cast<jlong>(&group);
    }
    CATCH_STD()
    return 0;
}

JNIEXPORT void JNICALL Java_com_tightdb_SharedGroup_nativeCommit(
    JNIEnv*, jobject, jlong native_ptr)
{
    SG(native_ptr)->commit();   // noexcept
}

JNIEXPORT void JNICALL Java_com_tightdb_SharedGroup_nativeRollback(
    JNIEnv*, jobject, jlong native_ptr)
{
    SG(native_ptr)->rollback();   // noexcept
}

JNIEXPORT jboolean JNICALL Java_com_tightdb_SharedGroup_nativeHasChanged
  (JNIEnv *, jobject, jlong native_ptr)
{
    return SG(native_ptr)->has_changed();   // noexcept
}

JNIEXPORT jstring JNICALL Java_com_tightdb_SharedGroup_nativeGetDefaultReplicationDatabaseFileName(
    JNIEnv* env, jclass)
{
#ifdef TIGHTDB_ENABLE_REPLICATION
    ThrowException(env, UnsupportedOperation,
                   "Replication is not currently supported by the Java language binding.");
    return 0;
//    return env->NewStringUTF(Replication::get_path_to_database_file());
#else
    ThrowException(env, UnsupportedOperation,
                   "Replication was disable in the native library at compile time");
    return 0;
#endif
}
