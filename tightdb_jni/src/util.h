#ifndef UTIL_H
#define UTIL_H

#include <string>
#include <jni.h>

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
    NoSuchMethod
};

void ThrowException(JNIEnv* env, ExceptionKind exception, std::string itemStr, std::string classStr);


#endif