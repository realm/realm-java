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
#include "bson_util.hpp"
#include "java_accessor.hpp"
#include "util.hpp"

// Must match JniBsonProtocol.VALUE from Java
static const std::string VALUE("value");

using namespace realm::bson;
using namespace realm::jni_util;

Bson JniBsonProtocol::string_to_bson(std::string arg) {
    BsonDocument document(parse(arg));
    return document[VALUE];
}
Bson JniBsonProtocol::jstring_to_bson(JNIEnv* env, jstring arg) {
    return string_to_bson(JStringAccessor(env, arg));
}

std::string JniBsonProtocol::bson_to_string(Bson bson) {
    BsonDocument document{{VALUE, bson}};
    std::stringstream buffer;
    buffer << document;
    return buffer.str();
}

jstring JniBsonProtocol::bson_to_jstring(JNIEnv* env, Bson bson) {
    std::string r = bson_to_string(bson);
    return to_jstring(env, r);
}

BsonArray JniBsonProtocol::stringarray_to_bsonarray(std::vector<std::string> args) {
    uint32_t size = std::uint32_t(args.size());
    BsonArray bson_array(size);
    for (uint32_t i = 0; i < size; i++) {
        std::string document = args[i];
        bson_array[i] = string_to_bson(document);
    }
    return bson_array;
};

//BsonArray jobjectarray_to_bsonarray(JNIEnv* env, realm::_impl::JObjectArrayAccessor<JStringAccessor, jstring>& documents) {
//    uint32_t size = std::uint32_t(documents.size());
//    BsonArray bson_array(size);
//    for (uint32_t i = 0; i < size; i++) {
//        std::string document = documents[i];
//        bson_array[i] = jstring_to_bson(env, document);
//    }
//    return bson_array;
//};
