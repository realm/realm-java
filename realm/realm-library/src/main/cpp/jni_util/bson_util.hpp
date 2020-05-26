/*
 * Copyright 2020 Realm Inc.
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

#ifndef REALM_BSON_UTIL_HPP
#define REALM_BSON_UTIL_HPP

#include <jni.h>
#include <util/bson/bson.hpp>

namespace realm {
namespace jni_util {

using namespace realm::bson;

// Serializes and wraps bson values passed between java and JNI according to JniBsonProtocol.java
class JniBsonProtocol {
public:
    static Bson string_to_bson(const std::string arg);
    static Bson jstring_to_bson(JNIEnv* env, const jstring arg);
    static const Bson& check(const Bson& bson, const Bson::Type type, const std::string message);
    static Bson parse_checked(JNIEnv* env, const jstring arg, const Bson::Type type, const std::string message);
    static std::string bson_to_string(const Bson& bson);
    static jstring bson_to_jstring(JNIEnv* env, const Bson& bson);
};

} // jni_util
} // realm

#endif //REALM_BSON_UTIL_HPP
