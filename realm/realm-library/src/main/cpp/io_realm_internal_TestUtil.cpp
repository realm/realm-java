#include "io_realm_internal_TestUtil.h"
#include "util.hpp"

static jstring throwOrGetExpectedMessage(JNIEnv *env, jlong testcase, bool should_throw);

JNIEXPORT jlong JNICALL
Java_io_realm_internal_TestUtil_getMaxExceptionNumber
(JNIEnv *, jclass)
{
    return ExceptionKindMax;
}

JNIEXPORT jstring JNICALL
Java_io_realm_internal_TestUtil_getExpectedMessage
(JNIEnv *env, jclass, jlong exception_kind)
{
    return throwOrGetExpectedMessage(env, exception_kind, false);
}

JNIEXPORT void JNICALL
Java_io_realm_internal_TestUtil_testThrowExceptions
(JNIEnv *env, jclass, jlong exception_kind)
{
    throwOrGetExpectedMessage(env, exception_kind, true);
}

static jstring
throwOrGetExpectedMessage
(JNIEnv *env, jlong testcase, bool should_throw)
{
    std::string expect;

    switch (ExceptionKind(testcase)) {
        case ClassNotFound:
            expect = "java.lang.ClassNotFoundException: Class 'parm1' could not be located.";
            if (should_throw)
                ThrowException(env, ClassNotFound, "parm1", "parm2");
            break;
        case NoSuchField:
            expect = "java.lang.NoSuchFieldException: Field 'parm2' could not be located in class io.realm.parm1";
            if (should_throw)
                ThrowException(env, NoSuchField, "parm1", "parm2");
            break;
        case NoSuchMethod:
            expect = "java.lang.NoSuchMethodException: Method 'parm2' could not be located in class io.realm.parm1";
            if (should_throw)
                ThrowException(env, NoSuchMethod, "parm1", "parm2");
            break;
        case IllegalArgument:
            expect = "java.lang.IllegalArgumentException: Illegal Argument: parm1";
            if (should_throw)
                ThrowException(env, IllegalArgument, "parm1", "parm2");
            break;
        case IOFailed:
            expect = "io.realm.exceptions.RealmIOException: Failed to open parm1. parm2";
            if (should_throw)
                ThrowException(env, IOFailed, "parm1", "parm2");
            break;
        case FileNotFound:
            expect = "io.realm.exceptions.RealmIOException: File not found: parm1.";
            if (should_throw)
                ThrowException(env, FileNotFound, "parm1", "parm2");
            break;
        case FileAccessError:
            expect = "io.realm.exceptions.RealmIOException: Failed to access: parm1. parm2";
            if (should_throw)
                ThrowException(env, FileAccessError, "parm1", "parm2");
            break;
        case IndexOutOfBounds:
            expect = "java.lang.ArrayIndexOutOfBoundsException: parm1";
            if (should_throw)
                ThrowException(env, IndexOutOfBounds, "parm1", "parm2");
            break;
        case TableInvalid:
            expect = "java.lang.IllegalStateException: Illegal State: parm1";
            if (should_throw)
                ThrowException(env, TableInvalid, "parm1", "parm2");
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
        case RowInvalid:
            expect = "java.lang.IllegalStateException: Illegal State: parm1";
            if (should_throw)
                ThrowException(env, RowInvalid, "parm1", "parm2");
            break;
        case CrossTableLink:
            expect = "java.lang.IllegalStateException: This class is referenced by other classes. Remove those fields first before removing this class.";
            if (should_throw)
                ThrowException(env, CrossTableLink, "parm1");
            break;
        case BadVersion:
            expect = "io.realm.internal.async.BadVersionException: parm1";
            if (should_throw)
                ThrowException(env, BadVersion, "parm1", "parm2");
            break;
        case LockFileError:
            expect = "io.realm.exceptions.IncompatibleLockFileException: parm1";
            if (should_throw)
                ThrowException(env, LockFileError, "parm1", "parm2");
            break;
        case IllegalState:
            expect = "java.lang.IllegalStateException: parm1";
            if (should_throw)
                ThrowException(env, IllegalState, "parm1");
            break;
        default:
            break;
    }
    if (should_throw) {
        return NULL;
    }
    return to_jstring(env, expect);
}
