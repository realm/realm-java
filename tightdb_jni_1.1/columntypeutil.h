#ifndef TIGHTDB_COLUMN_TYPE_UTIL_H
#define TIGTHDB_COLUMN_TYPE_UTIL_H

#include <jni.h>
#include <ColumnType.h>

#ifdef __cplusplus
extern "C" {
#endif

ColumnType GetColumnTypeFromJColumnType(JNIEnv* env, jobject jColumnType);
jobject GetJColumnTypeFromColumnType(JNIEnv* env, ColumnType columnType);

#ifdef __cplusplus
}
#endif

#endif