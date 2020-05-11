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

#include "java_accessor.hpp"
#include "util.hpp"

#include <jni.h>
#include <util/bson/bson.hpp>

namespace realm {
namespace jni_util {

// Serializes and wraps bson values passed between java and JNI according to JniBsonProtocol.java
class JniBsonProtocol {
public:
    static realm::bson::Bson string_to_bson(std::string arg);
    static realm::bson::Bson jstring_to_bson(JNIEnv* env, jstring arg);
    static std::string bson_to_string(realm::bson::Bson bson);
    static jstring bson_to_jstring(JNIEnv* env, realm::bson::Bson bson);
    static realm::bson::BsonArray stringarray_to_bsonarray(std::vector<std::string> args);
//    static realm::bson::BsonArray jobjectarray_to_bsonarray(JNIEnv* env, _impl::JObjectArrayAccessor<JStringAccessor, jstring>& documents);
};

} // jni_util
} // realm

#endif //REALM_BSON_UTIL_HPP
