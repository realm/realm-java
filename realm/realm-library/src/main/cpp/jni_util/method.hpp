/*
 * Copyright 2016 Realm Inc.
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

#ifndef REALM_JNI_UTIL_METHOD_HPP
#define REALM_JNI_UTIL_METHOD_HPP

#include <jni.h>
#include <util.hpp>

namespace realm {
namespace jni_util {

class JniMethod {
public:
    JniMethod(JNIEnv *env, jobject obj, const char* method_name, const char* signature) {
        jclass cls = env->GetObjectClass(obj);
        m_method_id = env->GetMethodID(cls, method_name, signature);
        env->DeleteLocalRef(cls);
    }

    JniMethod(JNIEnv *env, const char* class_name, const char* method_name, const char* signature) {
        jclass cls = env->FindClass(class_name);
        if (cls == NULL) {
            // TODO: Throw a cpp exception instead.
            ThrowException(env, ClassNotFound, class_name);
            m_method_id = nullptr;
        } else {
            m_method_id = env->GetMethodID(cls, method_name, signature);
        }
    }

    ~JniMethod() { }

    inline operator jmethodID&() const { return m_method_id; }

private:
    jmethodID m_method_id;
};

} // namespace realm
} // namespace jni_util

#endif //REALM_JNI_UTIL_METHOD_HPP
