#ifndef UTIL_H
#define UTIL_H

#include <string>
#include <jni.h>
#include "com_tightdb_util.h"


#define TRACE               1       // disable for performance
#define CHECK_PARAMETERS    1       // Check all parameters in API and throw exceptions in java if invalid

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved);

#ifdef __cplusplus
}
#endif

#define MAX_JLONG  9223372036854775807
#define MIN_JLONG -9223372036854775808
#define MAX_JINT   2147483647
#define MAX_JSIZE  MAX_JINT

// Helper macros for better readability
#define S(x) static_cast<size_t>(x)
#define TBL(x) reinterpret_cast<Table*>(x)
#define TV(x) reinterpret_cast<TableView*>(x)
#define Q(x) reinterpret_cast<Query*>(x)
#define G(x) reinterpret_cast<Group*>(x)

// Exception handling

enum ExceptionKind {
    ClassNotFound,
    NoSuchField,
    NoSuchMethod,
    IllegalArgument, 
    IOFailed,
    IndexOutOfBounds
};

void ThrowException(JNIEnv* env, ExceptionKind exception, std::string classStr, std::string itemStr = "");

jclass GetClass(JNIEnv* env, char *classStr);


// Check parameters
#if CHECK_PARAMETERS
bool IndexValid(JNIEnv* env, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex);
bool IndexAndTypeValid(JNIEnv* env, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex, int columnType);
#else
#define IndexValid(a,b,c,d) (TRUE)
#define IndexAndTypeValid(a,b,c,d,e) (TRUE)
#endif


// Debug trace

extern int trace_level;

#if TRACE
#define TR(fmt, ...) if (trace_level > 0) { jprintf(env, fmt, ##__VA_ARGS__); } else {}
#else
#define TR(fmt, ...)
#endif

void jprintf(JNIEnv *env, const char *fmt, ...);

void jprint(JNIEnv *env, char *txt);


#endif // UTIL_H