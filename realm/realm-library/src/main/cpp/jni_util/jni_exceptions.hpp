////////////////////////////////////////////////////////////////////////////
//
// Copyright 2015 Realm Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
////////////////////////////////////////////////////////////////////////////

#ifndef REALM_JNI_EXCEPTIONS_HPP
#define REALM_JNI_EXCEPTIONS_HPP

#include <exception>
#include <string>
#include <utility>

namespace realm {
namespace jni_util {

class JniPendingException : public std::runtime_error {
    JniPendingException(std::string message) : runtime_error(std::move(message)) {}
};

} // jni_util
} // realm

#endif // defined(REALM_JNI_EXCEPTIONS_HPP)
