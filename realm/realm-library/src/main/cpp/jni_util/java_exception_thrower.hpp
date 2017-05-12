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

#ifndef REALM_JNI_UTIL_JAVA_EXCEPTION_THROWER_HPP
#define REALM_JNI_UTIL_JAVA_EXCEPTION_THROWER_HPP

#include <jni.h>

#include "java_class.hpp"

namespace realm {
namespace jni_util {

// Class to help throw a Java exception from JNI code.
// This exception will be called from CATCH_STD and throw a Java exception there.
class JavaExceptionThrower : public std::runtime_error {
public:
    JavaExceptionThrower(JNIEnv* env, const char* class_name, std::string message)
        : std::runtime_error(std::move(message))
        , m_exception_class(env, class_name)
    {
    }

    virtual void throw_java_exception(JNIEnv* env)
    {
        env->ThrowNew(m_exception_class, what());
    }

private:
    JavaClass m_exception_class;
};

} // namespace realm
} // namesapce jni_util

#endif // REALM_JNI_UTIL_JAVA_EXCEPTION_THROWER_HPP
