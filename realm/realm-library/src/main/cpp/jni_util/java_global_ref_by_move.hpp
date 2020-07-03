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

// Manages the lifecycle of jobject's global ref via move constructors
//
// It prevents leaking global references by automatically referencing and unreferencing Java objects
// any time the instance is moved or destroyed. Its principal use is on data structures that support move
// operations, such as std::vector.
//
// Note that there is another flavor available: JavaGlobalRefByCopy.
//
// JavaGlobalRefByCopy: multiple references will exist to the Java object, one on each instance.
// JavaGlobalRefByMove: only one reference will only be available at last moved instance.

class JavaGlobalRefByMove {
public:
    JavaGlobalRefByMove()
        : m_ref(nullptr)
    {
    }
    // Acquire a global ref on the given jobject. The local ref will be released if given release_local_ref is true.
    JavaGlobalRefByMove(JNIEnv* env, jobject obj, bool release_local_ref = false)
        : m_ref(obj ? env->NewGlobalRef(obj) : nullptr)
    {
        if (release_local_ref) {
            env->DeleteLocalRef(obj);
        }
    }
    JavaGlobalRefByMove(JavaGlobalRefByMove&& rhs)
        : m_ref(rhs.m_ref)
    {
        rhs.m_ref = nullptr;
    }
    ~JavaGlobalRefByMove();

    JavaGlobalRefByMove& operator=(JavaGlobalRefByMove&& rhs);
    JavaGlobalRefByMove& operator=(JavaGlobalRefByMove& rhs) = delete;
    JavaGlobalRefByMove(JavaGlobalRefByMove&);

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
