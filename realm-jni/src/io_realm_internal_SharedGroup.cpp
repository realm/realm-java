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

#include <iostream>

#include <jni.h>

#include "util.hpp"

#include <tightdb/group_shared.hpp>
#include <tightdb/replication.hpp>
#include <tightdb/commit_log.hpp>

#include "util.hpp"
#include "io_realm_internal_SharedGroup.h"

using namespace std;
using namespace tightdb;

#define SG(ptr) reinterpret_cast<SharedGroup*>(ptr)

JNIEXPORT jlong JNICALL Java_io_realm_internal_SharedGroup_nativeCreate(
    JNIEnv* env, jobject, jstring jfile_name, jint durability, jboolean no_create, jboolean enable_replication, jbyteArray keyArray)
{
    TR_ENTER()
    StringData file_name;

    SharedGroup* db = 0;
    try {
        JStringAccessor file_name_tmp(env, jfile_name); // throws
        file_name = StringData(file_name_tmp);

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

            KeyBuffer key(env, keyArray);
#ifdef TIGHTDB_ENABLE_ENCRYPTION
            db = new SharedGroup(file_name, no_create!=0, level, key.data());
#else
            db = new SharedGroup(file_name, no_create!=0, level);
#endif
        }
        return reinterpret_cast<jlong>(db);
    }
    CATCH_FILE(file_name)
    CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_SharedGroup_createNativeWithImplicitTransactions
  (JNIEnv* env, jobject, jlong native_replication_ptr, jbyteArray keyArray)
{
    TR_ENTER()
    try {
        KeyBuffer key(env, keyArray);
#ifdef TIGHTDB_ENABLE_ENCRYPTION
        SharedGroup* db = new SharedGroup(*reinterpret_cast<tightdb::Replication*>(native_replication_ptr), SharedGroup::durability_Full, key.data());
#else
        SharedGroup* db = new SharedGroup(*reinterpret_cast<tightdb::Replication*>(native_replication_ptr));
#endif

        return reinterpret_cast<jlong>(db);
    }
    CATCH_FILE()
    CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_SharedGroup_nativeCreateReplication
  (JNIEnv* env, jobject, jstring jfile_name, jbyteArray keyArray)
{
    TR_ENTER()
    StringData file_name;
    try {     
        JStringAccessor file_name_tmp(env, jfile_name); // throws
        file_name = StringData(file_name_tmp);
        KeyBuffer key(env, keyArray);
#ifdef TIGHTDB_ENABLE_ENCRYPTION
        Replication* repl = makeWriteLogCollector(file_name, false, key.data());
#else
        Replication* repl = makeWriteLogCollector(file_name);
#endif
        return reinterpret_cast<jlong>(repl);
    }
    CATCH_FILE(file_name)
    CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_SharedGroup_nativeBeginImplicit
  (JNIEnv* env, jobject, jlong native_ptr)
{
    TR_ENTER_PTR(native_ptr)
    try {
        Group& group = const_cast<Group&>(SG(native_ptr)->begin_read());
        return reinterpret_cast<jlong>(&group);
    }
    CATCH_STD()
    return 0;
}

JNIEXPORT void JNICALL Java_io_realm_internal_SharedGroup_nativeAdvanceRead
(JNIEnv *env, jobject, jlong native_ptr)
{
    TR_ENTER_PTR(native_ptr)
    try {
        LangBindHelper::advance_read( *SG(native_ptr) );
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_SharedGroup_nativePromoteToWrite
  (JNIEnv *env, jobject, jlong native_ptr)
{
    TR_ENTER_PTR(native_ptr) 
    try {
        LangBindHelper::promote_to_write( *SG(native_ptr) );
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_SharedGroup_nativeCommitAndContinueAsRead
  (JNIEnv *env, jobject, jlong native_ptr)
{
    TR_ENTER_PTR(native_ptr)
    try {
        LangBindHelper::commit_and_continue_as_read( *SG(native_ptr) );
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_SharedGroup_nativeCloseReplication
  (JNIEnv *, jobject, jlong native_replication_ptr)
{
    TR_ENTER_PTR(native_replication_ptr)
    delete reinterpret_cast<Replication*>(native_replication_ptr);
}

JNIEXPORT void JNICALL Java_io_realm_internal_SharedGroup_nativeClose(
    JNIEnv*, jclass, jlong native_ptr)
{
    TR_ENTER_PTR(native_ptr)
    delete SG(native_ptr);
}

JNIEXPORT void JNICALL Java_io_realm_internal_SharedGroup_nativeReserve(
   JNIEnv *env, jobject, jlong native_ptr, jlong bytes)
{
    TR_ENTER_PTR(native_ptr)
    if (bytes <= 0) {
        ThrowException(env, UnsupportedOperation, "number of bytes must be > 0.");
        return;
    }

    try {
         SG(native_ptr)->reserve(S(bytes));
    }
    CATCH_STD()
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_SharedGroup_nativeBeginRead(
    JNIEnv* env, jobject, jlong native_ptr)
{
    TR_ENTER_PTR(native_ptr)
    try {
        const Group& group = SG(native_ptr)->begin_read();
        return reinterpret_cast<jlong>(&group);
    }
    CATCH_STD()
    return 0;
}

JNIEXPORT void JNICALL Java_io_realm_internal_SharedGroup_nativeEndRead(
    JNIEnv *, jobject, jlong native_ptr)
{
    TR_ENTER_PTR(native_ptr)
    SG(native_ptr)->end_read();     // noexcept
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_SharedGroup_nativeBeginWrite(
    JNIEnv* env, jobject, jlong native_ptr)
{
    TR_ENTER_PTR(native_ptr)
    try {
        Group& group = SG(native_ptr)->begin_write();
        return reinterpret_cast<jlong>(&group);
    }
    CATCH_STD()
    return 0;
}

JNIEXPORT void JNICALL Java_io_realm_internal_SharedGroup_nativeCommit(
    JNIEnv*, jobject, jlong native_ptr)
{
    TR_ENTER_PTR(native_ptr)
    SG(native_ptr)->commit();   // noexcept
}

JNIEXPORT void JNICALL Java_io_realm_internal_SharedGroup_nativeRollback(
    JNIEnv*, jobject, jlong native_ptr)
{
    TR_ENTER_PTR(native_ptr)
    SG(native_ptr)->rollback();   // noexcept
}

JNIEXPORT void JNICALL Java_io_realm_internal_SharedGroup_nativeRollbackAndContinueAsRead(
    JNIEnv *, jobject, jlong native_ptr)
{
    TR_ENTER_PTR(native_ptr)
    LangBindHelper::rollback_and_continue_as_read(*SG(native_ptr));
}


JNIEXPORT jboolean JNICALL Java_io_realm_internal_SharedGroup_nativeHasChanged
  (JNIEnv *, jobject, jlong native_ptr)
{
    TR_ENTER_PTR(native_ptr)
    return SG(native_ptr)->has_changed();   // noexcept
}

JNIEXPORT jstring JNICALL Java_io_realm_internal_SharedGroup_nativeGetDefaultReplicationDatabaseFileName(
    JNIEnv* env, jclass)
{
    TR_ENTER()
#ifdef TIGHTDB_ENABLE_REPLICATION
    ThrowException(env, UnsupportedOperation,
                   "Replication is not currently supported by the Java language binding.");
    return 0;
//    return to_jstring(env, Replication::get_path_to_database_file());
#else
    ThrowException(env, UnsupportedOperation,
                   "Replication was disable in the native library at compile time");
    return 0;
#endif
}
