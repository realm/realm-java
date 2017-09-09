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

#ifndef REALM_JNI_UTIL_JAVA_LOCAL_REF_HPP
#define REALM_JNI_UTIL_JAVA_LOCAL_REF_HPP

#include <jni.h>

namespace realm {
namespace jni_util {

struct NeedToCreateLocalRef {
};
static constexpr NeedToCreateLocalRef need_to_create_local_ref{};

// Wraps jobject and automatically calls DeleteLocalRef when this object is destroyed.
// DeleteLocalRef is not necessary to be called in most cases since all local references will be cleaned up when the
// program returns to Java from native. But if the local ref is created in a loop, consider to use this class to wrap
// it because the size of local reference table is relative small (512 bytes on Android).
template <typename T>
class JavaLocalRef {
public:
    inline JavaLocalRef() noexcept
        : m_jobject(nullptr)
        , m_env(nullptr){};
    inline JavaLocalRef(JNIEnv* env, T obj) noexcept
        : m_jobject(obj)
        , m_env(env){};
    // need_to_create is useful when acquire a local ref from a global weak ref.
    inline JavaLocalRef(JNIEnv* env, T obj, NeedToCreateLocalRef) noexcept
        : m_jobject(env->NewLocalRef(obj))
        , m_env(env){};

    inline ~JavaLocalRef()
    {
        if (m_jobject) {
            m_env->DeleteLocalRef(m_jobject);
        }
    }

    JavaLocalRef& operator=(JavaLocalRef&& rhs)
    {
        this->~JavaLocalRef();
        new (this) JavaLocalRef(std::move(rhs));
        return *this;
    }

    inline JavaLocalRef(JavaLocalRef&& rhs)
        : m_jobject(rhs.m_jobject)
        , m_env(rhs.m_env)
    {
        rhs.m_jobject = nullptr;
        rhs.m_env = nullptr;
    }

    inline operator bool() const noexcept
    {
        return m_jobject != nullptr;
    };
    inline operator T() const noexcept
    {
        return m_jobject;
    }
    inline T get() const noexcept
    {
        return m_jobject;
    };

    JavaLocalRef(const JavaLocalRef&) = delete;
    JavaLocalRef& operator=(const JavaLocalRef&) = delete;

private:
    T m_jobject;
    JNIEnv* m_env;
};

} // namespace realm
} // namespace jni_util
#endif // REALM_JNI_UTIL_JAVA_LOCAL_REF_HPP
