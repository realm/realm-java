/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class com_tightdb_SubTableDefinition */

#ifndef _Included_com_tightdb_SubTableDefinition
#define _Included_com_tightdb_SubTableDefinition
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     com_tightdb_SubTableDefinition
 * Method:    nativeAddColumn
 * Signature: (J[JILjava/lang/String;)J
 */
JNIEXPORT jlong JNICALL Java_com_tightdb_SubTableDefinition_nativeAddColumn
  (JNIEnv *, jobject, jlong, jlongArray, jint, jstring);

/*
 * Class:     com_tightdb_SubTableDefinition
 * Method:    nativeRemoveColumn
 * Signature: (J[JJ)V
 */
JNIEXPORT void JNICALL Java_com_tightdb_SubTableDefinition_nativeRemoveColumn
  (JNIEnv *, jobject, jlong, jlongArray, jlong);

/*
 * Class:     com_tightdb_SubTableDefinition
 * Method:    nativeRenameColumn
 * Signature: (J[JJLjava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_com_tightdb_SubTableDefinition_nativeRenameColumn
  (JNIEnv *, jobject, jlong, jlongArray, jlong, jstring);

#ifdef __cplusplus
}
#endif
#endif
