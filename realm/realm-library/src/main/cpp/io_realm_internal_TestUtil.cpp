/*
 * Copyright 2017 Realm Inc.
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

#include "io_realm_internal_TestUtil.h"
#include "util.hpp"

static jstring throwOrGetExpectedMessage(JNIEnv* env, jlong testcase, bool should_throw);

JNIEXPORT jlong JNICALL Java_io_realm_internal_TestUtil_getMaxExceptionNumber(JNIEnv*, jclass)
{
    return ExceptionKindMax;
}

JNIEXPORT jstring JNICALL Java_io_realm_internal_TestUtil_getExpectedMessage(JNIEnv* env, jclass,
                                                                             jlong exception_kind)
{
    return throwOrGetExpectedMessage(env, exception_kind, false);
}

JNIEXPORT void JNICALL Java_io_realm_internal_TestUtil_testThrowExceptions(JNIEnv* env, jclass, jlong exception_kind)
{
    throwOrGetExpectedMessage(env, exception_kind, true);
}

static jstring throwOrGetExpectedMessage(JNIEnv* env, jlong testcase, bool should_throw)
{
    std::string expect;

    switch (ExceptionKind(testcase)) {
        case ClassNotFound:
            expect = "java.lang.ClassNotFoundException: Class 'parm1' could not be located.";
            if (should_throw)
                ThrowException(env, ClassNotFound, "parm1", "parm2");
            break;
        case IllegalArgument:
            expect = "java.lang.IllegalArgumentException: Illegal Argument: parm1";
            if (should_throw)
                ThrowException(env, IllegalArgument, "parm1", "parm2");
            break;
        case IndexOutOfBounds:
            expect = "java.lang.ArrayIndexOutOfBoundsException: parm1";
            if (should_throw)
                ThrowException(env, IndexOutOfBounds, "parm1", "parm2");
            break;
        case UnsupportedOperation:
            expect = "java.lang.UnsupportedOperationException: parm1";
            if (should_throw)
                ThrowException(env, UnsupportedOperation, "parm1", "parm2");
            break;
        case OutOfMemory:
            expect = "io.realm.internal.OutOfMemoryError: parm1 parm2";
            if (should_throw)
                ThrowException(env, OutOfMemory, "parm1", "parm2");
            break;
        case FatalError:
            expect = "io.realm.exceptions.RealmError: Unrecoverable error. parm1";
            if (should_throw)
                ThrowException(env, FatalError, "parm1", "parm2");
            break;
        case RuntimeError:
            expect = "java.lang.RuntimeException: parm1";
            if (should_throw)
                ThrowException(env, RuntimeError, "parm1", "parm2");
            break;
        case BadVersion:
            expect = "io.realm.internal.async.BadVersionException: parm1";
            if (should_throw)
                ThrowException(env, BadVersion, "parm1", "parm2");
            break;
        case IllegalState:
            expect = "java.lang.IllegalStateException: parm1";
            if (should_throw)
                ThrowException(env, IllegalState, "parm1");
            break;
        // FIXME: This is difficult to test right now. Need to refactor the test.
        // See https://github.com/realm/realm-java/issues/3348
        // case RealmFileError:
        default:
            break;
    }
    if (should_throw) {
        return nullptr;
    }
    return to_jstring(env, expect);
}

JNIEXPORT jboolean JNICALL
Java_io_realm_internal_TestUtil_testNativeString(JNIEnv *env, jclass) {
    // strings can be found in https://github.com/realm/realm-java/issues/4025

    // "class_CoordinatesRealm"
    // According to #4025, the error is "Invalid first byte of UTF-8 sequence, or code point too big for UTF-16"
    // All code points are valid, single byte values.
    char data1[] = {0x63, 0x6c, 0x61, 0x73, 0x73, 0x5f, 0x43, 0x6f, 0x6f, 0x72, 0x64, 0x69, 0x6e, 0x61, 0x74, 0x65, 0x73,
                   0x52, 0x65, 0x61, 0x6c, 0x6d};
    realm::StringData str1(data1, 22);

    jstring jstr1 = to_jstring(env, str1);

    // "9dbca7ae44c14545b7e6088a7e590165"
    char data2[] = {0x39, 0x64, 0x62, 0x63, 0x61, 0x37, 0x61, 0x65, 0x34, 0x34, 0x63, 0x31, 0x34, 0x35, 0x34, 0x35,
                    0x62, 0x37, 0x65, 0x36, 0x30, 0x38, 0x38, 0x61, 0x37, 0x65, 0x35, 0x39, 0x30, 0x31, 0x36, 0x35};
    realm::StringData str2(data2, 32);
    jstring jstr2 = to_jstring(env, str2);

    return (jstr1 != nullptr && jstr2 != nullptr)?JNI_TRUE:JNI_FALSE;
}