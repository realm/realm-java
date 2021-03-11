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

#include "io_realm_internal_OsSet.h"

#include <realm/object-store/set.hpp>
#include <realm/object-store/shared_realm.hpp>

#include "java_accessor.hpp"
#include "java_object_accessor.hpp"
#include "java_exception_def.hpp"
#include "jni_util/java_exception_thrower.hpp"
#include "util.hpp"

using namespace realm;
using namespace realm::util;
using namespace realm::object_store;
using namespace realm::_impl;

void finalize_set(jlong ptr) {
    delete reinterpret_cast<object_store::Set*>(ptr);
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_OsSet_nativeGetFinalizerPtr(JNIEnv*, jclass) {
    return reinterpret_cast<jlong>(&finalize_set);
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_OsSet_nativeCreate(JNIEnv* env, jclass, jlong shared_realm_ptr,
                                          jlong obj_ptr, jlong column_key) {
    try {
        auto obj = *reinterpret_cast<realm::Obj*>(obj_ptr);
        auto shared_realm = *reinterpret_cast<SharedRealm*>(shared_realm_ptr);
        auto set_ptr = new object_store::Set(shared_realm, obj, ColKey(column_key));
        return reinterpret_cast<jlong>(set_ptr);
    }
    CATCH_STD()
    return reinterpret_cast<jlong>(nullptr);
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_OsSet_nativeSize(JNIEnv* env, jclass, jlong set_ptr) {
    try {
        auto& set = *reinterpret_cast<realm::object_store::Set*>(set_ptr);
        return set.size();
    }
    CATCH_STD()
    return 0;
}

JNIEXPORT jboolean JNICALL
Java_io_realm_internal_OsSet_nativeContainsNull(JNIEnv *env, jclass, jlong set_ptr) {
    try {
        auto& set = *reinterpret_cast<realm::object_store::Set*>(set_ptr);
        size_t found = set.find_any(Mixed());
        return found;
    }
    CATCH_STD()
    return reinterpret_cast<jlong>(nullptr);
}

JNIEXPORT jboolean JNICALL
Java_io_realm_internal_OsSet_nativeContainsString(JNIEnv* env, jclass, jlong set_ptr,
                                                  jstring j_value) {
    try {
        auto& set = *reinterpret_cast<realm::object_store::Set*>(set_ptr);
        JStringAccessor value(env, j_value);
        size_t found = set.find_any(Mixed(StringData(value)));
        return found;
    }
    CATCH_STD()
    return reinterpret_cast<jlong>(nullptr);
}

JNIEXPORT void JNICALL
Java_io_realm_internal_OsSet_nativeAddNull(JNIEnv* env, jclass, jlong set_ptr) {
    try {
        auto& set = *reinterpret_cast<realm::object_store::Set*>(set_ptr);
        set.insert(Mixed());
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL
Java_io_realm_internal_OsSet_nativeAddString(JNIEnv* env, jclass, jlong set_ptr, jstring j_value) {
    try {
        auto& set = *reinterpret_cast<realm::object_store::Set*>(set_ptr);
        JStringAccessor value(env, j_value);
        JavaAccessorContext context(env);
        set.insert(context, Any(value));
    }
    CATCH_STD()
}

JNIEXPORT jlongArray JNICALL
Java_io_realm_internal_OsSet_nativeRemoveNull(JNIEnv* env, jclass, jlong set_ptr) {
    try {
        auto& set = *reinterpret_cast<realm::object_store::Set*>(set_ptr);
        const std::pair<size_t, bool>& remove_pair = set.remove_any(Mixed());
        jlong ret[2];
        ret[0] = remove_pair.first;     // index
        ret[1] = remove_pair.second;    // found (or not)
        jlongArray ret_array = env->NewLongArray(2);
        env->SetLongArrayRegion(ret_array, 0, 2, ret);
        return ret_array;
    }
    CATCH_STD()
    return nullptr;
}

JNIEXPORT jlongArray JNICALL
Java_io_realm_internal_OsSet_nativeRemoveString(JNIEnv* env, jclass, jlong set_ptr, jstring j_value) {
    try {
        auto& set = *reinterpret_cast<realm::object_store::Set*>(set_ptr);
        JStringAccessor value(env, j_value);
        const std::pair<size_t, bool>& remove_pair = set.remove_any(Mixed(StringData(value)));
        jlong ret[2];
        ret[0] = remove_pair.first;     // index
        ret[1] = remove_pair.second;    // found (or not)
        jlongArray ret_array = env->NewLongArray(2);
        env->SetLongArrayRegion(ret_array, 0, 2, ret);
        return ret_array;
    }
    CATCH_STD()
    return nullptr;
}
