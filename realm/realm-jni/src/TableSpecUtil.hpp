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

#ifndef REALM_JAVA_TABLE_SPEC_UTIL_HPP
#define REALM_JAVA_TABLE_SPEC_UTIL_HPP

#include <cstddef>
#include <vector>
#include <jni.h>
#include <realm/table.hpp>

jlong Java_io_realm_TableSpec_getColumnCount(JNIEnv*, jobject jTableSpec);

jobject Java_io_realm_TableSpec_getColumnType(JNIEnv*, jobject jTableSpec, jlong columnIndex);

jstring Java_io_realm_TableSpec_getColumnName(JNIEnv*, jobject jTableSpec, jlong columnIndex);

jobject Java_io_realm_TableSpec_getTableSpec(JNIEnv*, jobject jTableSpec, jlong columnIndex);

jlong Java_io_realm_TableSpec_getColumnIndex(JNIEnv*, jobject jTableSpec, jstring columnName);

void set_descriptor(JNIEnv*,       realm::Descriptor&, jobject jTableSpec);
void get_descriptor(JNIEnv*, const realm::Descriptor&, jobject jTableSpec);

jclass GetClassTableSpec(JNIEnv*);
jmethodID GetTableSpecMethodID(JNIEnv*, const char* methodStr, const char* typeStr);

#endif // REALM_JAVA_TABLE_SPEC_UTIL_HPP
