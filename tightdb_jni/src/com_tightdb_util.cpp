#include <jni.h>
#include "mem_usage/mem.hpp"
#include "com_tightdb_util.h"
#include "util.h"

int trace_level = 0;

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved) 
{
    return JNI_VERSION_1_6;
}

JNIEXPORT void JNICALL Java_com_tightdb_util_nativeSetDebugLevel
  (JNIEnv *env, jclass, jint level)
{
    trace_level = level;
}

JNIEXPORT jlong JNICALL Java_com_tightdb_util_nativeGetMemUsage(JNIEnv *env, jclass)
{
//javaPrint(env, "??????? HI ????????????????");
    return GetMemUsage();
}


void javaPrint(JNIEnv *env, char *txt)
{
    jclass cls = env->FindClass("com.tightdb.util");
    if (cls) {
        jmethodID mid = env->GetStaticMethodID(cls, "javaPrint", "(Ljava/lang/String;)V");
        if (mid)
            env->CallStaticVoidMethod(cls, mid, env->NewStringUTF(txt));
        else {
            ThrowException(env, NoSuchMethod, "com.tightdb.util", "javaPrint");
            printf("method not found");
        }
    } else {
        ThrowException(env, ClassNotFound, "com.tightdb.util");
        printf("class not found");
    }

}

