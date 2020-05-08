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

#ifndef REALM_UTIL_SYNC_HPP
#define REALM_UTIL_SYNC_HPP

#include "java_accessor.hpp"

#include <jni.h>
#include <util/bson/bson.hpp>

//using namespace realm;
//using namespace realm::jni_util;
using namespace realm::bson;
using namespace realm::_impl;

Bson jstring_to_bson(std::string arg);
Bson jstring_to_bson(JNIEnv* env, jstring arg);
jstring bson_to_jstring(JNIEnv* env, realm::bson::Bson bson);
BsonArray jobjectarray_to_bsonarray(JObjectArrayAccessor<JStringAccessor, jstring>& documents);

#endif //REALM_UTIL_SYNC_HPP
