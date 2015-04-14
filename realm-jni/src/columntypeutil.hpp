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

#ifndef REALM_COLUMN_TYPE_UTIL_H
#define REALM_COLUMN_TYPE_UTIL_H

#include <jni.h>
#include <realm.hpp>

#ifdef __cplusplus

using realm::DataType;

extern "C" {

#endif

DataType GetColumnTypeFromJColumnType(JNIEnv* env, jobject jColumnType);
jobject GetJColumnTypeFromColumnType(JNIEnv* env, DataType columnType);

#ifdef __cplusplus
}
#endif

#endif
