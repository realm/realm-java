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
