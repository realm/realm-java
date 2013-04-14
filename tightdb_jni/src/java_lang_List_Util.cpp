#include "util.hpp"
#include "java_lang_List_Util.hpp"

jint java_lang_List_size(JNIEnv* env, jobject jList)
{
    // WARNING: do not cache these methods, list class may be different based on the object jlist
    jclass jListClass = env->GetObjectClass(jList);
    if (jListClass == NULL)
        return 0;
    jmethodID jListSizeMethodId = env->GetMethodID(jListClass, "size", "()I");
    if (jListSizeMethodId == NULL) {
        ThrowException(env, NoSuchMethod, "jList", "size");
        return 0;
    }
    return env->CallIntMethod(jList, jListSizeMethodId);
}

jobject java_lang_List_get(JNIEnv* env, jobject jList, jint index)
{
    // WARNING: do not cache these methods/classes, list class may be different based on the object jlist
    jclass jListClass = env->GetObjectClass(jList);
     if (jListClass == NULL)
        return NULL;
    jmethodID jListGetMethodId = env->GetMethodID(jListClass, "get", "(I)Ljava/lang/Object;");
    if (jListGetMethodId == NULL) {
        ThrowException(env, NoSuchMethod, "jList", "get");
        return NULL;
    }
    return env->CallObjectMethod(jList, jListGetMethodId, index);
}
