#include "util.hpp"
#include "mem_usage.hpp"
#include "com_tightdb_internal_util.h"

int trace_level = 0;

static int TIGHTDB_JNI_VERSION = 19;

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM*, void*)
{
    return JNI_VERSION_1_6;
}

JNIEXPORT void JNICALL Java_com_tightdb_internal_util_nativeSetDebugLevel(JNIEnv*, jclass, jint level)
{
    trace_level = level;
}

JNIEXPORT jlong JNICALL Java_com_tightdb_internal_util_nativeGetMemUsage(JNIEnv*, jclass)
{
    return GetMemUsage();
}

JNIEXPORT jint JNICALL Java_com_tightdb_internal_util_nativeGetVersion(JNIEnv*, jclass)
{
    return TIGHTDB_JNI_VERSION;
}
