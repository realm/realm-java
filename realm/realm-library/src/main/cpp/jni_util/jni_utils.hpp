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

#ifndef REALM_JNI_UTIL_JNI_UTILS_HPP
#define REALM_JNI_UTIL_JNI_UTILS_HPP

#include <jni.h>

#include <vector>

#include "java_global_ref.hpp"

namespace realm {
namespace jni_util {

// Util functions for JNI.
class JniUtils {
public:
    ~JniUtils()
    {
    }

    // Call this only once in JNI_OnLoad.
    static void initialize(JavaVM* vm, jint vm_version) noexcept;
    // Call this in JNI_OnUnload.
    static void release();
    // When attach_if_needed is false, returns the JNIEnv if there is one attached to this thread. Assert if there is
    // none. When attach_if_needed is true, try to attach and return a JNIEnv if necessary.
    static JNIEnv* get_env(bool attach_if_needed = false);
    // Detach the current thread from the JVM. Only required for C++ threads that where attached in the first place.
    // Failing to do so is a resource leak.
    static void detach_current_thread();
    // Keep the given global reference until JNI_OnUnload is called.
    static void keep_global_ref(JavaGlobalRef& ref);

private:
    JniUtils(JavaVM* vm, jint vm_version) noexcept
        : m_vm(vm)
        , m_vm_version(vm_version)
    {
    }

    JavaVM* m_vm;
    jint m_vm_version;
    std::vector<JavaGlobalRef> m_global_refs;
};

} // namespace realm
} // namespace jni_util

#endif // REALM_JNI_UTIL_JNI_UTILS_HPP
