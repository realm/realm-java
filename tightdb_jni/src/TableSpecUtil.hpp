#ifndef TIGHTDB_JAVA_TABLE_SPEC_UTIL_HPP
#define TIGHTDB_JAVA_TABLE_SPEC_UTIL_HPP

#include <cstddef>
#include <vector>
#include <jni.h>
#include <tightdb/table.hpp>

jlong Java_com_tightdb_TableSpec_getColumnCount(JNIEnv*, jobject jTableSpec);

jobject Java_com_tightdb_TableSpec_getColumnType(JNIEnv*, jobject jTableSpec, jlong columnIndex);

jstring Java_com_tightdb_TableSpec_getColumnName(JNIEnv*, jobject jTableSpec, jlong columnIndex);

jobject Java_com_tightdb_TableSpec_getTableSpec(JNIEnv*, jobject jTableSpec, jlong columnIndex);

jlong Java_com_tightdb_TableSpec_getColumnIndex(JNIEnv*, jobject jTableSpec, jstring columnName);

void updateSpecFromJSpec(JNIEnv*, tightdb::Table*, const std::vector<std::size_t>& path,
                         jobject jTableSpec);
void UpdateJTableSpecFromSpec(JNIEnv*, const tightdb::Spec&, jobject jTableSpec);

jclass GetClassTableSpec(JNIEnv*);
jmethodID GetTableSpecMethodID(JNIEnv*, const char* methodStr, const char* typeStr);

#endif // TIGHTDB_JAVA_TABLE_SPEC_UTIL_HPP
