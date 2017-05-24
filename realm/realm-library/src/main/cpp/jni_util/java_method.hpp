/*
 * Copyright 2017 Realm Inc.
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

#ifndef REALM_JNI_UTIL_JAVA_METHOD_HPP
#define REALM_JNI_UTIL_JAVA_METHOD_HPP

#include <jni.h>

namespace realm {
namespace jni_util {

// RAII wrapper for java method ID. Since normally method ID stays unchanged for the whole JVM life cycle, it would be
// safe to have a static JavaMethod object to avoid calling GetMethodID multiple times.
class JavaMethod {
public:
    JavaMethod()
        : m_method_id(nullptr)
    {
    }
    JavaMethod(JNIEnv* env, jclass cls, const char* method_name, const char* signature, bool static_method = false);
    JavaMethod(JNIEnv* env, jobject obj, const char* method_name, const char* signature);
    JavaMethod(JNIEnv* env, const char* class_name, const char* method_name, const char* signature,
               bool static_method = false);

    JavaMethod(const JavaMethod&) = default;
    JavaMethod& operator=(const JavaMethod&) = default;
    JavaMethod(JavaMethod&& rhs) = delete;
    JavaMethod& operator=(JavaMethod&& rhs) = delete;

    ~JavaMethod()
    {
    }

    inline operator bool() const noexcept
    {
        return m_method_id != nullptr;
    }
    inline operator const jmethodID&() const noexcept
    {
        return m_method_id;
    }

private:
    jmethodID m_method_id;
};

} // namespace realm
} // namespace jni_util

#endif // REALM_JNI_UTIL_JAVA_METHOD_HPP
