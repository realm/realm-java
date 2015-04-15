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

#ifndef MIXED_UTIL_H
#define MIXED_UTIL_H

#include <jni.h>
#include <realm.hpp>

using namespace realm;

DataType GetMixedObjectType(JNIEnv* env, jobject jMixed);
jobject CreateJMixedFromMixed(JNIEnv* env, Mixed& mixed);
jlong GetMixedIntValue(JNIEnv* env, jobject jMixed);
jfloat GetMixedFloatValue(JNIEnv* env, jobject jMixed);
jdouble GetMixedDoubleValue(JNIEnv* env, jobject jMixed);
jstring GetMixedStringValue(JNIEnv* env, jobject jMixed);
jboolean GetMixedBooleanValue(JNIEnv* env, jobject jMixed);
jbyteArray GetMixedByteArrayValue(JNIEnv* env, jobject jMixed);
jlong GetMixedDateTimeValue(JNIEnv* env, jobject jMixed);
jobject GetMixedByteBufferValue(JNIEnv* env, jobject jMixed);
jint GetMixedBinaryType(JNIEnv* env, jobject jMixed);

#endif
