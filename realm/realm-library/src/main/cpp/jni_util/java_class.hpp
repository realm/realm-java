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

#ifndef REALM_JNI_UTIL_JAVA_CLASS_HPP
#define REALM_JNI_UTIL_JAVA_CLASS_HPP

#include <jni.h>

#include "java_global_ref.hpp"

namespace realm {
namespace jni_util {

// To find the jclass and manage the lifecycle for the jclass's global ref.
class JavaClass {
public:
    JavaClass();
    // when free_on_unload is true, the jclass's global ref will be released when JNI_OnUnload called. This is useful
    // when the JavaClass instance is static. Otherwise the jclass's global ref will be released when this object is
    // deleted.
    JavaClass(JNIEnv* env, const char* class_name, bool free_on_unload = true);
    ~JavaClass()
    {
    }

    JavaClass(JavaClass&&);

    inline jclass get() noexcept
    {
        return m_class;
    }

    inline operator jclass() const noexcept
    {
        return m_class;
    }

    inline operator bool() const noexcept
    {
        return m_class != nullptr;
    }

    // Not implemented for now.
    JavaClass(JavaClass&) = delete;
    JavaClass& operator=(JavaClass&&) = delete;

private:
    JavaGlobalRef m_ref_owner;
    jclass m_class;
    static JavaGlobalRef get_jclass(JNIEnv* env, const char* class_name);
};

} // jni_util
} // realm

#endif
