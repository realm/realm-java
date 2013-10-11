#include "util.hpp"
#include "mem_usage.hpp"
#include "com_tightdb_internal_Util.h"

using std::string;

//#define USE_VLD
#if defined(_MSC_VER) && defined(_DEBUG) && defined(USE_VLD)
    #include "C:\\Program Files (x86)\\Visual Leak Detector\\include\\vld.h"
#endif

int trace_level = 0;

static int TIGHTDB_JNI_VERSION = 20;

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

// -------------------------- Testcases for exception handling

JNIEXPORT jstring JNICALL Java_com_tightdb_internal_Util_nativeTestcase(
    JNIEnv *env, jclass, jint testcase, jboolean dotest, jlong)
{
    string expect;

    switch (ExceptionKind(testcase)) {
        case ClassNotFound:
            expect = "java.lang.ClassNotFoundException: Class 'parm1' could not be located.";
            if (dotest)
                ThrowException(env, ClassNotFound, "parm1", "parm2");
            break;
        case NoSuchField:
            expect = "java.lang.NoSuchFieldException: Field 'parm2' could not be located in class com.tightdb.parm1";
            if (dotest)
                ThrowException(env, NoSuchField, "parm1", "parm2");
            break;
        case NoSuchMethod:
            expect = "java.lang.NoSuchMethodException: Method 'parm2' could not be located in class com.tightdb.parm1";
            if (dotest)
                ThrowException(env, NoSuchMethod, "parm1", "parm2");
            break;
        case IllegalArgument:
            expect = "java.lang.IllegalArgumentException: Illegal Argument: parm1";
            if (dotest)
                ThrowException(env, IllegalArgument, "parm1", "parm2");
            break;
        case IOFailed:
            expect = "com.tightdb.IOException: Failed to open parm1. parm2";
            if (dotest)
                ThrowException(env, IOFailed, "parm1", "parm2");
            break;
        case FileNotFound:
            expect = "com.tightdb.IOException: File not found: parm1.";
            if (dotest)
                ThrowException(env, FileNotFound, "parm1", "parm2");
            break;
        case FileAccessError:
            expect = "com.tightdb.IOException: Failed to access: parm1. parm2";
            if (dotest)
                ThrowException(env, FileAccessError, "parm1", "parm2");
            break;
        case IndexOutOfBounds:
            expect = "java.lang.ArrayIndexOutOfBoundsException: parm1";
            if (dotest)
                ThrowException(env, IndexOutOfBounds, "parm1", "parm2");
            break;
        case TableInvalid:
            expect = "java.lang.IllegalStateException: Illegal State: parm1";
            if (dotest)
                ThrowException(env, TableInvalid, "parm1", "parm2");
            break;
        case UnsupportedOperation:
            expect = "java.lang.UnsupportedOperationException: parm1";
            if (dotest)
                ThrowException(env, UnsupportedOperation, "parm1", "parm2");
            break;
                ThrowException(env, OutOfMemory, "parm1", "parm2");
        case OutOfMemory:
            expect = "com.tightdb.OutOfMemoryError: parm1 parm2";
            if (dotest)
                ThrowException(env, OutOfMemory, "parm1", "parm2");
            break;
        case Unspecified:
            expect = "java.lang.RuntimeException: Unspecified exception. parm1";
            if (dotest)
                ThrowException(env, Unspecified, "parm1", "parm2");
            break;
        case RuntimeError:
            expect = "java.lang.RuntimeException: parm1";
            if (dotest)
                ThrowException(env, RuntimeError, "parm1", "parm2");
            break;
    }
    return to_jstring(env, expect);
}

