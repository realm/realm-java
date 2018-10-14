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

#ifndef REALM_JNI_UTIL_JAVA_GLOBAL_WEAK_REF_HPP
#define REALM_JNI_UTIL_JAVA_GLOBAL_WEAK_REF_HPP

#include <jni.h>
#include <functional>

#include "jni_utils.hpp"

namespace realm {
namespace jni_util {

// RAII wrapper for weak global ref.
class JavaGlobalWeakRef {
public:
    JavaGlobalWeakRef();
    JavaGlobalWeakRef(JNIEnv*, jobject);
    ~JavaGlobalWeakRef();

    JavaGlobalWeakRef(JavaGlobalWeakRef&&);
    JavaGlobalWeakRef& operator=(JavaGlobalWeakRef&&);

    JavaGlobalWeakRef(const JavaGlobalWeakRef&);
    JavaGlobalWeakRef& operator=(const JavaGlobalWeakRef&);

    inline operator bool() const noexcept
    {
        return m_weak != nullptr;
    }

    JavaGlobalRef global_ref(JNIEnv* env = nullptr) const;

    using Callback = void(JNIEnv* env, jobject obj);

    // Acquire a local ref and run the callback with it if the weak ref is valid. The local ref will be deleted after
    // callback finished. Return false if the weak ref is not valid anymore.
    bool call_with_local_ref(JNIEnv* env, std::function<Callback> callback) const ;
    // Try to get an JNIEnv for current thread then run the callback.
    bool call_with_local_ref(std::function<Callback> callback) const;

private:
    jweak m_weak;
};

} // namespace jni_util
} // namespace realm

#endif // REALM_JNI_UTIL_JAVA_GLOBAL_WEAK_REF_HPP
