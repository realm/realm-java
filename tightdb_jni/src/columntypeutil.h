#ifndef TIGHTDB_COLUMN_TYPE_UTIL_H
#define TIGTHDB_COLUMN_TYPE_UTIL_H

#include <jni.h>
#include <tightdb.hpp>

#ifdef __cplusplus

using tightdb::DataType;

extern "C" {

#endif

DataType GetColumnTypeFromJColumnType(JNIEnv* env, jobject jColumnType);
jobject GetJColumnTypeFromColumnType(JNIEnv* env, DataType columnType);

#ifdef __cplusplus
}
#endif

#endif
