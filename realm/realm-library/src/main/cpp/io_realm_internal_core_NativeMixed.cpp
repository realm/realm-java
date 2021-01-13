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

#include "io_realm_internal_core_NativeMixed.h"

#include "util.hpp"

using namespace std;
using namespace realm;

static void finalize_mixed(jlong ptr) {
    delete reinterpret_cast<Mixed *>(ptr);
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_core_NativeMixed_nativeGetFinalizerPtr(JNIEnv *, jclass) {
    return reinterpret_cast<jlong>(&finalize_mixed);
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_core_NativeMixed_nativeCreateMixedBoolean(JNIEnv *, jclass, jboolean jvalue) {
    return reinterpret_cast<jlong>(new Mixed(B(jvalue)));
}

JNIEXPORT jint JNICALL
Java_io_realm_internal_core_NativeMixed_nativeGetMixedType(JNIEnv *, jclass, jlong nativePtr) {
    auto mixed = reinterpret_cast<Mixed *>(nativePtr);

    if (mixed->is_null()) {
        return -1;
    } else {
        return mixed->get_type();
    }
}

JNIEXPORT jboolean JNICALL
Java_io_realm_internal_core_NativeMixed_nativeMixedAsBoolean(JNIEnv *, jclass, jlong nativePtr) {
    auto mixed = reinterpret_cast<Mixed *>(nativePtr);
    return mixed->get<bool>();
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_core_NativeMixed_nativeCreateMixedNull(JNIEnv *, jclass) {
    return reinterpret_cast<jlong>(new Mixed());
}

