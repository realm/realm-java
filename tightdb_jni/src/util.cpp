#include <assert.h>
#include <string>

#include "util.h"

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved) 
{
	return JNI_VERSION_1_6;
}

void ThrowException(JNIEnv* env, ExceptionKind exception, std::string itemStr, std::string classStr) 
{
    std::string message;
    jclass jExceptionClass = NULL;

    switch (exception) {
        case ClassNotFound:
            jExceptionClass = env->FindClass("java/lang/ClassNotFoundException");
            message = "Class com.tightdb.'" + classStr + "could not be located.";
            break;

        case NoSuchField:
            jExceptionClass = env->FindClass("java/lang/NoSuchFieldException");
            message = "Field '" + itemStr + "' could not be located in class com.tightdb." + classStr;
            break;

        case NoSuchMethod:
            jExceptionClass = env->FindClass("java/lang/NoSuchMethodException");
            message = "Method '" + itemStr + "' could not be located in class com.tightdb." + classStr;
            break;

        default:
            assert(0);
            return;
    }
    if (jExceptionClass != NULL)
        env->ThrowNew(jExceptionClass, message.c_str());
}
