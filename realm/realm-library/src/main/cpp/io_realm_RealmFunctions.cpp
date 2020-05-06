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

#include "io_realm_RealmFunctions.h"

#include "util.hpp"
#include "util_sync.hpp"

using namespace realm;

// FIXME This is just a basic round trip test for passing bson back and forth. Proper implementation
//  will come with actual Function implementation.
JNIEXPORT jstring JNICALL Java_io_realm_RealmFunctions_nativeCallFunction
        (JNIEnv* env, jclass, jstring j_args) {
    try {
        bson::Bson bson = jstring_to_bson(env, j_args);
        return bson_to_jstring(env, bson);
    }
    CATCH_STD()
    return NULL;
}

