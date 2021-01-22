/*
 * Copyright 2021 Realm Inc.
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

#include "io_realm_internal_objectstore_OsKeyPathMapping.h"

#include <realm/sort_descriptor.hpp>
#include <realm/object-store/keypath_helpers.hpp>
#include <realm/object-store/shared_realm.hpp>
#include <realm/parser/keypath_mapping.hpp>

#include "util.hpp"

using namespace realm;
using namespace realm::jni_util;
using namespace realm::_impl;

static void finalize_client(jlong ptr) {
    delete reinterpret_cast<parser::KeyPathMapping*>(ptr);
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_objectstore_OsKeyPathMapping_nativeGetFinalizerMethodPtr(JNIEnv*, jclass) {
    return reinterpret_cast<jlong>(&finalize_client);
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_objectstore_OsKeyPathMapping_nativeCreateMapping(JNIEnv* env, jclass, jlong j_shared_realm_ptr) {
    try {
        auto shared_realm = *(reinterpret_cast<SharedRealm*>(j_shared_realm_ptr));
        auto mapping = new parser::KeyPathMapping;
        realm::populate_keypath_mapping(*mapping, *shared_realm);
        return reinterpret_cast<jlong>(mapping);
    }
    CATCH_STD()
    return 0;
}
