#include "util.h"
#include "com_tightdb_internal_util.h"
#include <assert.h>


void ThrowException(JNIEnv* env, ExceptionKind exception, std::string classStr, std::string itemStr)
{
    std::string message;
    jclass jExceptionClass = NULL;

    TR_ERR((env, "\njni: ThrowingException %d, %s, %s.\n", exception, classStr.c_str(), itemStr.c_str()));

    switch (exception) {
        case ClassNotFound:
            jExceptionClass = env->FindClass("java/lang/ClassNotFoundException");
            message = "Class '" + classStr + "' could not be located.";
            break;

        case NoSuchField:
            jExceptionClass = env->FindClass("java/lang/NoSuchFieldException");
            message = "Field '" + itemStr + "' could not be located in class com.tightdb." + classStr;
            break;

        case NoSuchMethod:
            jExceptionClass = env->FindClass("java/lang/NoSuchMethodException");
            message = "Method '" + itemStr + "' could not be located in class com.tightdb." + classStr;
            break;

        case IllegalArgument:
        case TableInvalid:
            jExceptionClass = env->FindClass("java/lang/IllegalArgumentException");
            message = "Illegal Argument: " + classStr;
            break;

        case IOFailed:
            jExceptionClass = env->FindClass("java/lang/IOException");
            message = "Failed to open " + classStr;
            break;

        case IndexOutOfBounds:
            jExceptionClass = env->FindClass("java/lang/ArrayIndexOutOfBoundsException");
            message = classStr;
            break;

        case UnsupportedOperation:
            jExceptionClass = env->FindClass("java/lang/UnsupportedOperationException");
            message = classStr;
            break;

        default:
            assert(0);
            return;
    }
    if (jExceptionClass != NULL)
        env->ThrowNew(jExceptionClass, message.c_str());
    else {
        TR_ERR((env, "\nERROR: Couldn't throw exception.\n"));
    }

    env->DeleteLocalRef(jExceptionClass);
}

jclass GetClass(JNIEnv* env, const char* classStr)
{
    jclass localRefClass = env->FindClass(classStr);
    if (localRefClass == NULL) {
		ThrowException(env, ClassNotFound, classStr);
		return NULL;
	}

    jclass myClass = reinterpret_cast<jclass>( env->NewGlobalRef(localRefClass) );
    env->DeleteLocalRef(localRefClass);
    return myClass;
}

void jprint(JNIEnv *env, char *txt)
{
#if 1
    static_cast<void>(env);
    fprintf(stderr, " -- JNI: %s", txt);  fflush(stderr);
#else
    static jclass myClass = GetClass(env, "com/tightdb/util");
    static jmethodID myMethod = env->GetStaticMethodID(myClass, "javaPrint", "(Ljava/lang/String;)V");
    if (myMethod)
        env->CallStaticVoidMethod(myClass, myMethod, env->NewStringUTF(txt));
#endif
}

void jprintf(JNIEnv *env, const char *format, ...) {
    va_list argptr;
    char buf[200];
    va_start(argptr, format);
    //vfprintf(stderr, format, argptr);
    vsnprintf(buf, 200, format, argptr);
    jprint(env, buf);
    va_end(argptr);
}

bool GetBinaryData(JNIEnv* env, jobject jByteBuffer, tightdb::BinaryData& data)
{
	data.pointer = (const char*)(env->GetDirectBufferAddress(jByteBuffer));
    if (!data.pointer) {
        ThrowException(env, IllegalArgument, "ByteBuffer is invalid");
        return false;
    }
    jlong len = env->GetDirectBufferCapacity(jByteBuffer);
    if (len < 1) {
        ThrowException(env, IllegalArgument, "Can't get BufferCapacity.");
        return false;
    }
    data.len = S(len);
    return true;
}
