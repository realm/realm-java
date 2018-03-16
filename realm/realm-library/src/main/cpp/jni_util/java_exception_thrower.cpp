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

#include "java_exception_thrower.hpp"
#include "log.hpp"

using namespace realm::util;
using namespace realm::jni_util;

JavaExceptionThrower::JavaExceptionThrower(const char* file_path, int line_num)
    : std::runtime_error("Java exception has been occurred. Terminate JNI by throwing a c++ exception.")
    , m_exception_class()
    , m_file_path(file_path)
    , m_line_num(line_num)
{
}

JavaExceptionThrower::JavaExceptionThrower(JNIEnv* env, const char* class_name, std::string message,
                                           const char* file_path, int line_num)
    : std::runtime_error(std::move(message))
    , m_exception_class(env, class_name, false)
    , m_file_path(file_path)
    , m_line_num(line_num)
{
}

void JavaExceptionThrower::throw_java_exception(JNIEnv* env)
{
    std::string message = format("%1\n(%2:%3)", what(), m_file_path, m_line_num);
    Log::w(message.c_str());

    // There is a pending Java exception, just return.
    if (env->ExceptionCheck()) {
        Log::w("There is a pending Java exception.");
        return;
    }
    env->ThrowNew(m_exception_class, message.c_str());
}

void JavaExceptionThrower::terminate_jni_if_java_exception_occurred(JNIEnv* env, CleanUpFunction clean_up_func,
                                                                    const char* file_path, int line_num)
{
    if (!env->ExceptionCheck()) {
        return;
    }
    if (clean_up_func) {
        clean_up_func();
    }
    throw JavaExceptionThrower(file_path, line_num);
}
