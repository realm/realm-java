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

#include "io_realm_internal_OsMap.h"

#include <realm/object-store/dictionary.hpp>
#include <realm/object-store/shared_realm.hpp>

#include "java_accessor.hpp"
#include "java_object_accessor.hpp"
#include "java_exception_def.hpp"
#include "jni_util/java_exception_thrower.hpp"
#include "util.hpp"

using namespace realm;
using namespace realm::util;
using namespace realm::_impl;

void finalize_map(jlong ptr) {
    delete reinterpret_cast<Dictionary *>(ptr);
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_OsMap_nativeGetFinalizerPtr(JNIEnv *, jclass) {
    return reinterpret_cast<jlong>(&finalize_map);
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_OsMap_nativeCreate(JNIEnv *env, jclass, jlong,
                                          jlong obj_ptr, jlong column_key) {
    try {
        auto& obj = *reinterpret_cast<realm::Obj*>(obj_ptr);

        Dictionary dict(obj, ColKey(column_key));

        // FIXME: still lots of stuff missing
        // FIXME: figure out whether or not we need to use something similar to ObservableCollectionWrapper from OsList
        return 0;
    }
    CATCH_STD()
//    return nullptr;
    return 0;
}
