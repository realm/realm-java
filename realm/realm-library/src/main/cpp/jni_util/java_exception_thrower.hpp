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

#include <stdexcept>

#include "java_class.hpp"

namespace realm {
namespace jni_util {

#define THROW_JAVA_EXCEPTION(env, class_name, message)                                                               \
    throw realm::jni_util::JavaExceptionThrower(env, class_name, message, __FILE__, __LINE__)

// Class to help throw a Java exception from JNI code.
// This exception will be called from CATCH_STD and throw a Java exception there.
class JavaExceptionThrower : public std::runtime_error {
public:
    JavaExceptionThrower(JNIEnv* env, const char* class_name, std::string message, const char* file_path,
                         int line_num);

    virtual void throw_java_exception(JNIEnv* env);

private:
    JavaClass m_exception_class;
    const char* m_file_path;
    int m_line_num;
};

} // namespace realm
} // namesapce jni_util

#endif // REALM_JNI_UTIL_JAVA_EXCEPTION_THROWER_HPP
