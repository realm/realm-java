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

#ifdef NDEBUG
#define TR(fmt, ...)
#else
#define TR(fmt, ...) if (trace_level > 0) { jprintf(env, fmt, ##__VA_ARGS__); } else {}
#endif

void jprintf(JNIEnv *env, const char *fmt, ...);

void javaPrint(JNIEnv *env, char *txt); // from com_tightdb_util.cpp

#endif // UTIL_H