#ifndef MIXED_UTIL_H
#define MIXED_UTIL_H

#include <jni.h>
#include <tightdb.hpp>

using namespace tightdb;

ColumnType GetMixedObjectType(JNIEnv* env, jobject jMixed);
jobject CreateJMixedFromMixed(JNIEnv* env, Mixed& mixed);
jlong GetMixedIntValue(JNIEnv* env, jobject jMixed);
jstring GetMixedStringValue(JNIEnv* env, jobject jMixed);
jboolean GetMixedBooleanValue(JNIEnv* env, jobject jMixed);
jbyteArray GetMixedByteArrayValue(JNIEnv* env, jobject jMixed);
jlong GetMixedDateTimeValue(JNIEnv* env, jobject jMixed);
jobject GetMixedByteBufferValue(JNIEnv* env, jobject jMixed);
jint GetMixedBinaryType(JNIEnv* env, jobject jMixed);

#endif