#ifndef TABLE_SPEC_UTIL_H
#define TABLE_SPEC_UTIL_H

#include <jni.h>
#include <tightdb.hpp>

using namespace tightdb;

#ifdef __cplusplus
extern "C" {
#endif

jlong Java_com_tightdb_TableSpec_getColumnCount(
	JNIEnv* env, jobject jTableSpec);

jobject Java_com_tightdb_TableSpec_getColumnType(
	JNIEnv* env, jobject jTableSpec, jlong columnIndex);

jstring Java_com_tightdb_TableSpec_getColumnName(
	JNIEnv* env, jobject jTableSpec, jlong columnIndex);

jobject Java_com_tightdb_TableSpec_getTableSpec(
	JNIEnv* env, jobject jTableSpec, jlong columnIndex);

jlong Java_com_tightdb_TableSpec_getColumnIndex(
	JNIEnv* env, jobject jTableSpec, jstring columnName);

void updateSpecFromJSpec(JNIEnv* env, Spec& spec, jobject jTableSpec);
void UpdateJTableSpecFromSpec(JNIEnv* env, const Spec& spec, jobject jTableSpec);

jclass GetClassTableSpec(JNIEnv* env);
jmethodID GetTableSpecMethodID(JNIEnv* env, char* methodStr, char* typeStr);

#ifdef __cplusplus
}
#endif

#endif