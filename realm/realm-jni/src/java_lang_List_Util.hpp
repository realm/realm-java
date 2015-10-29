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

#ifndef JAVA_LANG_LIST_UTIL_H
#define JAVA_LANG_LIST_UTIL_H

#include <jni.h>

#ifdef __cplusplus
extern "C" {
#endif

jint java_lang_List_size(JNIEnv* env, jobject jList);
jobject java_lang_List_get(JNIEnv* env, jobject jList, jint index);

#ifdef __cplusplus
}
#endif

#endif
