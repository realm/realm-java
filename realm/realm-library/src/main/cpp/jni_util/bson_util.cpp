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

#include <string>
#include "util.hpp"
#include "bson_util.hpp"

// Must match JniBsonProtocol.VALUE from Java
static const std::string VALUE("value");

using namespace realm::bson;
using namespace realm::jni_util;

Bson JniBsonProtocol::string_to_bson(const std::string arg) {
    BsonDocument document(parse(arg));
    return document[VALUE];
}

Bson JniBsonProtocol::jstring_to_bson(JNIEnv* env, const jstring arg) {
    return string_to_bson(JStringAccessor(env, arg));
}

const Bson& JniBsonProtocol::check(const realm::bson::Bson& bson, const realm::bson::Bson::Type type, const std::string message) {
    if (bson.type() != type) {
        throw realm::util::invalid_argument(message);
    }
    return bson;
}

Bson JniBsonProtocol::parse_checked(JNIEnv* env, const jstring arg, const Bson::Type type, const std::string message) {
    return JniBsonProtocol::check(JniBsonProtocol::jstring_to_bson(env, arg), type, message);
}

std::string JniBsonProtocol::bson_to_string(const Bson& bson) {
    BsonDocument document{{VALUE, bson}};
    std::stringstream buffer;
    buffer << document;
    return buffer.str();
}

jstring JniBsonProtocol::bson_to_jstring(JNIEnv* env, const Bson& bson) {
    std::string r = bson_to_string(bson);
    return to_jstring(env, r);
};
