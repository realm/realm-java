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

#ifndef REALM_JNI_EXCEPTIONS_HPP
#define REALM_JNI_EXCEPTIONS_HPP

#include <exception>
#include <string>

namespace realm {
namespace jni_util {

// JniPendingException is typically used in in wrappers of callbacks. If the callback has thrown a Java exception,
// it is not to safe most call JNI methods. Instead of clearing the Java exception and continue, the wrapper can throw
// this C++ exception and get quickly back to the JNI border (and back to Java) with the Java exception intact.
class JniPendingException : public std::runtime_error {
public:
    JniPendingException(std::string message) : runtime_error(std::move(message)) {}
};

} // jni_util
} // realm

#endif // defined(REALM_JNI_EXCEPTIONS_HPP)
