#ifndef MIXED_UTIL_H
#define MIXED_UTIL_H

#include "ColumnType.h"
#include <jni.h>

ColumnType GetMixedObjectType(JNIEnv* env, jobject jMixed);

jlong GetMixedIntValue(JNIEnv* env, jobject jMixed);
jstring GetMixedStringValue(JNIEnv* env, jobject jMixed);
jboolean GetMixedBooleanValue(JNIEnv* env, jobject jMixed);

#endif