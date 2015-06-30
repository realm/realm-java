/*
 * Copyright 2014 Realm Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include "util.hpp"
#include "mem_usage.hpp"
#include "io_realm_internal_Util.h"

using std::string;

//#define USE_VLD
#if defined(_MSC_VER) && defined(_DEBUG) && defined(USE_VLD)
    #include "C:\\Program Files (x86)\\Visual Leak Detector\\include\\vld.h"
#endif

// used by logging
int trace_level = 0;
const char *log_tag = "REALM";

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM*, void*)
{
    return JNI_VERSION_1_6;
}

JNIEXPORT void JNICALL Java_io_realm_internal_Util_nativeSetDebugLevel(JNIEnv*, jclass, jint level)
{
    trace_level = level;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Util_nativeGetMemUsage(JNIEnv*, jclass)
{
    return GetMemUsage();
}

// -------------------------- Testcases for exception handling

JNIEXPORT jstring JNICALL Java_io_realm_internal_Util_nativeTestcase(
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
            expect = "java.lang.NoSuchFieldException: Field 'parm2' could not be located in class io.realm.parm1";
            if (dotest)
                ThrowException(env, NoSuchField, "parm1", "parm2");
            break;
        case NoSuchMethod:
            expect = "java.lang.NoSuchMethodException: Method 'parm2' could not be located in class io.realm.parm1";
            if (dotest)
                ThrowException(env, NoSuchMethod, "parm1", "parm2");
            break;
        case IllegalArgument:
            expect = "java.lang.IllegalArgumentException: Illegal Argument: parm1";
            if (dotest)
                ThrowException(env, IllegalArgument, "parm1", "parm2");
            break;
        case IOFailed:
            expect = "io.realm.exceptions.RealmIOException: Failed to open parm1. parm2";
            if (dotest)
                ThrowException(env, IOFailed, "parm1", "parm2");
            break;
        case FileNotFound:
            expect = "io.realm.exceptions.RealmIOException: File not found: parm1.";
            if (dotest)
                ThrowException(env, FileNotFound, "parm1", "parm2");
            break;
        case FileAccessError:
            expect = "io.realm.exceptions.RealmIOException: Failed to access: parm1. parm2";
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
            expect = "io.realm.internal.OutOfMemoryError: parm1 parm2";
            if (dotest)
                ThrowException(env, OutOfMemory, "parm1", "parm2");
            break;
        case FatalError:
            expect = "io.realm.exceptions.RealmError: Unrecoverable error. parm1";
            if (dotest)
                ThrowException(env, FatalError, "parm1", "parm2");
            break;
        case RuntimeError:
            expect = "java.lang.RuntimeException: parm1";
            if (dotest)
                ThrowException(env, RuntimeError, "parm1", "parm2");
            break;
        case RowInvalid:
            expect = "java.lang.IllegalStateException: Illegal State: parm1";
            if (dotest)
                ThrowException(env, TableInvalid, "parm1", "parm2");
            break;
    }
    if (dotest) {
        return NULL;
    }
    return to_jstring(env, expect);
}

