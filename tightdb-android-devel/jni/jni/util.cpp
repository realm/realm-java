#include <assert.h>
#include <string>

#include "util.h"
#include "com_tightdb_util.h"

void ThrowException(JNIEnv* env, ExceptionKind exception, std::string classStr, std::string itemStr) 
{
    std::string message;
    jclass jExceptionClass = NULL;

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
            jExceptionClass = env->FindClass("java/lang/IllegalArgumentException");
            message = classStr;
            break;
        
        case IOFailed:
            jExceptionClass = env->FindClass("java/lang/IOException");
            message = "Failed to open " + classStr;
            break;
        
        default:
            assert(0);
            return;
    }
    if (jExceptionClass != NULL)
        env->ThrowNew(jExceptionClass, message.c_str());
    env->DeleteLocalRef(jExceptionClass);
}


jclass GetClass(JNIEnv* env, char *classStr) 
{
    jclass localRefClass = env->FindClass(classStr);	
    if (localRefClass == NULL) {
		ThrowException(env, ClassNotFound, classStr);
		return NULL;
	}

    jclass myClass = (jclass)env->NewGlobalRef(localRefClass);
    env->DeleteLocalRef(localRefClass);
    return myClass;
}

void jprintf(JNIEnv *env, const char *fmt, ...) {
    va_list argp;
    char buf[200];
    va_start(argp, fmt);
    // sprintf_s(buf, fmt, argp);
    va_end(argp);
    javaPrint(env, buf);
}

