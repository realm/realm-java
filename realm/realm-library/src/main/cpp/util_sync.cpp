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

#include "java_accessor.hpp"
#include "util.hpp"
#include "util_sync.hpp"

// Must match OSJNIBsonProtocol.VALUE
static const std::string VALUE("value");

using namespace realm::bson;
using namespace realm::_impl;

Bson jstring_to_bson(std::string arg) {
    BsonDocument document(parse(arg));
    return document[VALUE];
}

Bson jstring_to_bson(JNIEnv* env, jstring arg) {
    JStringAccessor args_json(env, arg);
    return jstring_to_bson(args_json);
}

//Bson jstring_to_bson(JNIEnv* env, jstring arg) {
//    JStringAccessor args_json(env, arg);
//    BsonDocument document(parse(args_json));
//    return document[VALUE];
//}

jstring bson_to_jstring(JNIEnv* env, Bson bson) {
    BsonDocument document{{VALUE, bson}};
    std::stringstream buffer;
    buffer << document;
    std::string r = buffer.str();
    return to_jstring(env, r);
};

BsonArray jobjectarray_to_bsonarray(JObjectArrayAccessor<JStringAccessor, jstring>& documents) {
    uint32_t size = std::uint32_t(documents.size());
    BsonArray bson_array(size);
    for (uint32_t i = 0; i < size; i++) {
        std::string document = documents[i];
        bson_array[i] = jstring_to_bson(document);
    }
    return bson_array;
}
