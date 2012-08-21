#include <jni.h>

#ifndef _Included_com_tightdb_Group
#define _Included_com_tightdb_Group
#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT jlong JNICALL Java_com_tightdb_SharedGroup_createNative(
    JNIEnv*, jobject, jstring, jboolean);

JNIEXPORT void JNICALL Java_com_tightdb_SharedGroup_nativeClose(
    JNIEnv*, jobject, jlong);

JNIEXPORT jlong JNICALL Java_com_tightdb_SharedGroup_nativeBeginWrite(
    JNIEnv*, jobject, jlong);

JNIEXPORT void JNICALL Java_com_tightdb_SharedGroup_nativeCommit(
    JNIEnv*, jobject, jlong);

JNIEXPORT void JNICALL Java_com_tightdb_SharedGroup_nativeRollback(
    JNIEnv*, jobject, jlong);

JNIEXPORT jstring JNICALL Java_com_tightdb_SharedGroup_nativeGetDefaultReplicationDatabaseFileName(
    JNIEnv*);

#ifdef __cplusplus
}
#endif
#endif
