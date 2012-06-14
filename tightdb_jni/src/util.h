#ifndef UTIL_H
#define UTIL_H

#include <string>
#include <jni.h>
#include "com_tightdb_util.h"

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved);

#ifdef __cplusplus
}
#endif


enum ExceptionKind {
    ClassNotFound,
    NoSuchField,
    NoSuchMethod,
    IllegalArgument, 
    IOFailed
};

void ThrowException(JNIEnv* env, ExceptionKind exception, std::string classStr, std::string itemStr = "");

jclass GetClass(JNIEnv* env, char *classStr);


extern int trace_level;

#define TRACE 1

#if TRACE
#define TR(fmt, ...) if (trace_level > 0) { jprintf(env, fmt, ##__VA_ARGS__); } else {}
#else
#define TR(fmt, ...)
#endif

void jprintf(JNIEnv *env, const char *fmt, ...);

void jprint(JNIEnv *env, char *txt);

#endif // UTIL_H