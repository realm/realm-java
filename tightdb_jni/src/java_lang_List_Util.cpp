#include "java_lang_List_Util.h"

jint java_lang_List_size(JNIEnv* env, jobject jList) 
{
	// WARNING: donot cache these methods, list class may be different based on the object jlist
	jclass jListClass = env->GetObjectClass(jList);
	jmethodID jListSizeMethodId = env->GetMethodID(jListClass, "size", "()I");
	if (jListSizeMethodId == NULL) {
		jclass jNoSuchMethodExceptionClass = env->FindClass("java/lang/NoSuchMethodException");
		env->ThrowNew(jNoSuchMethodExceptionClass, "Could not locate 'size' method in a list or a derived class of list in java_lang_List_size");
		return -1;
	}
	return env->CallIntMethod(jList, jListSizeMethodId);
}

jobject java_lang_List_get(JNIEnv* env, jobject jList, jint index) 
{
	// WARNING: donot cache these methods/classes, list class may be different based on the object jlist
	jclass jListClass = env->GetObjectClass(jList);
	jmethodID jListGetMethodId = env->GetMethodID(jListClass, "get", "(I)Ljava/lang/Object;");
	if (jListGetMethodId == NULL) {
		jclass jNoSuchMethodExceptionClass = env->FindClass("java/lang/NoSuchMethodException");
		env->ThrowNew(jNoSuchMethodExceptionClass, "Could not locate 'get' method in a list or a derived class of list in java_lang_List_size");
		return NULL;
	}
    return env->CallObjectMethod(jList, jListGetMethodId, index);
}