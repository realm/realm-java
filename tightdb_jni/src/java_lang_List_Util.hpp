#ifndef JAVA_LANG_LIST_UTIL_H
#define JAVA_LANG_LIST_UTIL_H

#include <jni.h>

#ifdef __cplusplus
extern "C" {
#endif

jint java_lang_List_size(JNIEnv* env, jobject jList);
jobject java_lang_List_get(JNIEnv* env, jobject jList, jint index);

#ifdef __cplusplus
}
#endif

#endif
