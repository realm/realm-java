#ifndef TIGHTDB_NATIVE_UTIL_H
#define TIGHTDB_NATIVE_UTIL_H

#include <jni.h>

#ifdef __cplusplus
extern "C" {
#endif

jlong GetNativePtrValue(JNIEnv* env, jobject jobj);
void SetNativePtrValue(JNIEnv* env, jobject jobj, jlong value);

#ifdef __cplusplus
}
#endif

#endif