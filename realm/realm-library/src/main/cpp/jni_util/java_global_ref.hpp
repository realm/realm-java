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

#ifndef REALM_JNI_UTIL_JAVA_GLOBAL_REF_HPP
#define REALM_JNI_UTIL_JAVA_GLOBAL_REF_HPP

#include <jni.h>

namespace realm {
namespace jni_util {

// Manage the lifecycle of jobject's global ref.
class JavaGlobalRef {
public:
    JavaGlobalRef()
        : m_ref(nullptr)
    {
    }
    // Acquire a global ref on the given jobject. The local ref will be released if given release_local_ref is true.
    JavaGlobalRef(JNIEnv* env, jobject obj, bool release_local_ref = false)
        : m_ref(obj ? env->NewGlobalRef(obj) : nullptr)
    {
        if (release_local_ref) {
            env->DeleteLocalRef(obj);
        }
    }
    JavaGlobalRef(JavaGlobalRef&& rhs)
        : m_ref(rhs.m_ref)
    {
        rhs.m_ref = nullptr;
    }
    ~JavaGlobalRef();

    JavaGlobalRef& operator=(JavaGlobalRef&& rhs);
    JavaGlobalRef& operator=(JavaGlobalRef& rhs) = delete;
    JavaGlobalRef(JavaGlobalRef&);

    inline operator bool() const noexcept
    {
        return m_ref != nullptr;
    }

    inline jobject get() const noexcept
    {
        return m_ref;
    }

private:
    jobject m_ref;
};
}
}

#endif // REALM_JNI_UTIL_JAVA_GLOBAL_REF_HPP
