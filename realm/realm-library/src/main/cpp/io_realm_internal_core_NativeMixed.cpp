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

#include <realm/object-store/shared_realm.hpp>

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

JNIEXPORT jlong JNICALL
Java_io_realm_internal_core_NativeMixed_nativeCreateMixedLink(JNIEnv *, jclass, jlong targetTableRef,
                                                              jlong targetObjectKey) {
    TableRef target_table = TBL_REF(targetTableRef);
    ObjKey object_key(targetObjectKey);
    ObjLink object_link(target_table->get_key(), object_key);

    return reinterpret_cast<jlong>(new Mixed(object_link));
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

JNIEXPORT jstring JNICALL
Java_io_realm_internal_core_NativeMixed_nativeGetRealmModelTableName(JNIEnv *env, jclass , jlong nativePtr,
                                                                     jlong nativeSharedRealmPtr) {
    auto mixed = reinterpret_cast<Mixed *>(nativePtr);
    auto& shared_realm = *(reinterpret_cast<SharedRealm*>(nativeSharedRealmPtr));

    auto obj_link = mixed->get<ObjLink>();

    return to_jstring(env, shared_realm->read_group().get_table(obj_link.get_table_key())->get_name());
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_core_NativeMixed_nativeGetRealmModelRowKey(JNIEnv *, jclass , jlong nativePtr) {
    auto mixed = reinterpret_cast<Mixed *>(nativePtr);

    auto obj_link = mixed->get<ObjLink>();
    return obj_link.get_obj_key().value;
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_core_NativeMixed_nativeCreateMixedNull(JNIEnv *, jclass) {
    return reinterpret_cast<jlong>(new Mixed());
}

