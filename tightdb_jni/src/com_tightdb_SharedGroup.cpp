#include <iostream>
#include <jni.h>
#include <tightdb/group_shared.hpp>

#include "util.h"
#include "com_tightdb_SharedGroup.h"

using namespace std;
using namespace tightdb;

JNIEXPORT jlong JNICALL Java_com_tightdb_SharedGroup_createNative(
    JNIEnv* env, jobject, jstring file_name, jboolean enable_replication)
{
    const char* file_name_ptr = env->GetStringUTFChars(file_name, 0);
    if (!file_name_ptr)
        return 0; // Exception is thrown by GetStringUTFChars()

    SharedGroup* db;
    if (enable_replication) {
#ifdef TIGHTDB_ENABLE_REPLICATION
        db = new SharedGroup(SharedGroup::replication_tag(), *file_name_ptr ? file_name_ptr : 0);
#else
        ThrowException(env, UnsupportedOperation,
                       "Replication was disabled in the native library at compile time.");
        return 0;
#endif
    }
    else {
        db = new SharedGroup(file_name_ptr);
    }
    if (!db->is_valid()) {
        delete db;
        ThrowException(env, IllegalArgument, "Failed to instantiate database."); // FIXME: More details must be made available.
        return 0;
    }
    return reinterpret_cast<jlong>(db);
}

JNIEXPORT void JNICALL Java_com_tightdb_SharedGroup_nativeClose(
    JNIEnv*, jobject, jlong native_ptr)
{
    SharedGroup* db = reinterpret_cast<SharedGroup*>(native_ptr);
    delete db;
}

JNIEXPORT jlong JNICALL Java_com_tightdb_SharedGroup_nativeBeginRead(
    JNIEnv*, jobject, jlong native_ptr)
{
    SharedGroup* db = reinterpret_cast<SharedGroup*>(native_ptr);
    const Group& group = db->begin_read();
    return reinterpret_cast<jlong>(&group);
}

JNIEXPORT void JNICALL Java_com_tightdb_SharedGroup_nativeEndRead(
    JNIEnv *, jobject, jlong native_ptr)
{
    SharedGroup* db = reinterpret_cast<SharedGroup*>(native_ptr);
    db->end_read();
}

JNIEXPORT jlong JNICALL Java_com_tightdb_SharedGroup_nativeBeginWrite(
    JNIEnv*, jobject, jlong native_ptr)
{
    SharedGroup* db = reinterpret_cast<SharedGroup*>(native_ptr);
    Group& group = db->begin_write(); // FIXME: Errors must be handled here
    return reinterpret_cast<jlong>(&group);
}

JNIEXPORT void JNICALL Java_com_tightdb_SharedGroup_nativeCommit(
    JNIEnv*, jobject, jlong native_ptr)
{
    SharedGroup* db = reinterpret_cast<SharedGroup*>(native_ptr);
    db->commit();
}

JNIEXPORT void JNICALL Java_com_tightdb_SharedGroup_nativeRollback(
    JNIEnv*, jobject, jlong native_ptr)
{
    SharedGroup* db = reinterpret_cast<SharedGroup*>(native_ptr);
    db->rollback();
}

JNIEXPORT jstring JNICALL Java_com_tightdb_SharedGroup_nativeGetDefaultReplicationDatabaseFileName(
    JNIEnv* env, jclass)
{
#ifdef TIGHTDB_ENABLE_REPLICATION
    return env->NewStringUTF(Replication::get_path_to_database_file());
#else
    ThrowException(env, UnsupportedOperation,
                   "Replication was disable in the native library at compile time");
    return 0;
#endif
}
