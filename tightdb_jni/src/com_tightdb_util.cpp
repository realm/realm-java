#include "util.h"
#include "mem_usage.hpp"
#include "com_tightdb_util.h"

int trace_level = 0;

static int TIGHTDB_JNI_VERSION = 8;

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved) 
{
    return JNI_VERSION_1_6;
}

JNIEXPORT void JNICALL Java_com_tightdb_util_nativeSetDebugLevel(JNIEnv *, jclass, jint level)
{
    trace_level = level;
}

JNIEXPORT jlong JNICALL Java_com_tightdb_util_nativeGetMemUsage(JNIEnv *, jclass)
{
    return GetMemUsage();
}

JNIEXPORT jint JNICALL Java_com_tightdb_util_nativeGetVersion(JNIEnv *, jclass) 
{
    return TIGHTDB_JNI_VERSION;
}

JNIEXPORT jlong JNICALL Java_com_tightdb_util_nativeGetInfiniteValue(JNIEnv *, jclass)
{
    return (size_t)(-1);
}
