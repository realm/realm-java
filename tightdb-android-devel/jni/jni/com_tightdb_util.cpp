#include <jni.h>
#include "mem_usage/mem.hpp"
#include "com_tightdb_util.h"
#include "util.h"

int trace_level = 0;

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved) 
{
    return JNI_VERSION_1_6;
}

JNIEXPORT void JNICALL Java_com_tightdb_TightdbJNI_nativeSetDebugLevel
  (JNIEnv *env, jobject, jint level)
{
    trace_level = level;
}

JNIEXPORT jlong JNICALL Java_com_tightdb_TightdbJNI_nativeGetMemUsage(JNIEnv *env, jobject)
{
#ifndef ANDROID
    return GetMemUsage();
#endif //_ANDROID
#ifdef ANDROID
    return 0;
#endif //ANDROID
}

void javaPrint(JNIEnv *env, char *txt)
{
    static jclass cls = env->FindClass("com.tightdb.util");
    static jmethodID mid = env->GetStaticMethodID(cls, "javaPrint", "(Ljava/lang/String;)V");

    env->CallStaticVoidMethod(cls, mid, env->NewStringUTF(txt));
}

