#include <jni.h>
#include <realm/group_shared.hpp>
#include <realm/string_data.hpp>

#include "io_realm_internal_TestUtil.h"
#include "util.hpp"

static jstring throwOrGetExpectedMessage(JNIEnv *env, jlong testcase, bool should_throw);

JNIEXPORT jlong JNICALL
Java_io_realm_internal_TestUtil_getMaxExceptionNumber(JNIEnv*, jclass)
{
    return ExceptionKindMax;
}

JNIEXPORT jstring JNICALL
Java_io_realm_internal_TestUtil_getExpectedMessage(JNIEnv *env, jclass, jlong exception_kind)
{
    return throwOrGetExpectedMessage(env, exception_kind, false);
}

JNIEXPORT void JNICALL
Java_io_realm_internal_TestUtil_testThrowExceptions(JNIEnv *env, jclass, jlong exception_kind)
{
    throwOrGetExpectedMessage(env, exception_kind, true);
}

static jstring
throwOrGetExpectedMessage(JNIEnv *env, jlong testcase, bool should_throw)
{

    return try_catch<jstring>(env, [&]() {
        std::string expect;
        switch (ExceptionKind(testcase)) {
            case ClassNotFound:
                expect = "java.lang.ClassNotFoundException: Class 'parm1' could not be located.";
                if (should_throw)
                    throw class_not_found("parm1");
                break;
            case IllegalArgument:
                expect = "java.lang.IllegalArgumentException: Illegal Argument: parm1";
                if (should_throw)
                    throw std::invalid_argument("parm1");
                break;
            case IndexOutOfBounds:
                expect = "java.lang.ArrayIndexOutOfBoundsException: parm1";
                if (should_throw)
                    throw std::range_error("parm1");
                break;
            case UnsupportedOperation:
                expect = "java.lang.UnsupportedOperationException: parm1";
                if (should_throw)
                    throw unsupported_operation("parm1");
                break;
            case OutOfMemory:
                expect = "io.realm.internal.OutOfMemoryError: std::bad_alloc";
                if (should_throw)
                    throw std::bad_alloc();
                break;
            case FatalError:
                expect = "io.realm.exceptions.RealmError: Unrecoverable error. parm1";
                if (should_throw)
                    throw fatal_error("parm1");
                break;
            case RuntimeError:
                expect = "java.lang.RuntimeException: parm1";
                if (should_throw)
                    throw std::runtime_error("parm1");
                break;
            case BadVersion:
                expect = "io.realm.internal.async.BadVersionException: ";
                if (should_throw)
                    throw realm::SharedGroup::BadVersion();
                break;
            case IllegalState:
                expect = "java.lang.IllegalStateException: parm1";
                if (should_throw)
                    throw illegal_state("parm1");
                break;
                // FIXME: This is difficult to test right now. Need to refactor the test.
                // See https://github.com/realm/realm-java/issues/3348
                // case RealmFileError:
            default:
                break;
        }
        return to_jstring(env, realm::StringData(expect));
    });
}
