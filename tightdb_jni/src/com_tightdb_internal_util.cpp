#include "util.hpp"
#include "mem_usage.hpp"
#include "com_tightdb_internal_Util.h"

//#define USE_VLD
#if defined(_MSC_VER) && defined(_DEBUG) && defined(USE_VLD)
    #include "C:\\Program Files (x86)\\Visual Leak Detector\\include\\vld.h"
#endif

int trace_level = 0;

static int TIGHTDB_JNI_VERSION = 19;

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM*, void*)
{
    return JNI_VERSION_1_6;
}

JNIEXPORT void JNICALL Java_com_tightdb_internal_Util_nativeSetDebugLevel(JNIEnv*, jclass, jint level)
{
    trace_level = level;
}

JNIEXPORT jlong JNICALL Java_com_tightdb_internal_Util_nativeGetMemUsage(JNIEnv*, jclass)
{
    return GetMemUsage();
}

JNIEXPORT jint JNICALL Java_com_tightdb_internal_Util_nativeGetVersion(JNIEnv*, jclass)
{
    return TIGHTDB_JNI_VERSION;
}
