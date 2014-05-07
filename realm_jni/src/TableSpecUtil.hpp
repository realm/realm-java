#ifndef TIGHTDB_JAVA_TABLE_SPEC_UTIL_HPP
#define TIGHTDB_JAVA_TABLE_SPEC_UTIL_HPP

#include <cstddef>
#include <vector>
#include <jni.h>
#include <tightdb/table.hpp>

jlong Java_io_realm_TableSpec_getColumnCount(JNIEnv*, jobject jTableSpec);

jobject Java_io_realm_TableSpec_getColumnType(JNIEnv*, jobject jTableSpec, jlong columnIndex);

jstring Java_io_realm_TableSpec_getColumnName(JNIEnv*, jobject jTableSpec, jlong columnIndex);

jobject Java_io_realm_TableSpec_getTableSpec(JNIEnv*, jobject jTableSpec, jlong columnIndex);

jlong Java_io_realm_TableSpec_getColumnIndex(JNIEnv*, jobject jTableSpec, jstring columnName);

void set_descriptor(JNIEnv*,       tightdb::Descriptor&, jobject jTableSpec);
void get_descriptor(JNIEnv*, const tightdb::Descriptor&, jobject jTableSpec);

jclass GetClassTableSpec(JNIEnv*);
jmethodID GetTableSpecMethodID(JNIEnv*, const char* methodStr, const char* typeStr);

#endif // TIGHTDB_JAVA_TABLE_SPEC_UTIL_HPP
