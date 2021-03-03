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

#include "io_realm_internal_OsMapChangeSet.h"

#include <realm/object-store/dictionary.hpp>

#include "util.hpp"
#include "java_class_global_def.hpp"

using namespace realm;
using namespace realm::_impl;

inline jobjectArray generate_change_set(JNIEnv* env, const std::vector<Mixed>& change_set) {
    unsigned long insertions_size = change_set.size();
    jobjectArray ret_array = env->NewObjectArray(insertions_size, JavaClassGlobalDef::java_lang_string(), NULL);
    for (unsigned long i = 0; i < insertions_size; i++) {
        Mixed mixed_key = change_set[i];
        const StringData& key = mixed_key.get_string();
        env->SetObjectArrayElement(ret_array, i, to_jstring(env, key));
    }

    return ret_array;
}

void finalize_changeset(jlong ptr) {
    delete reinterpret_cast<DictionaryChangeSet*>(ptr);
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_OsMapChangeSet_nativeGetFinalizerPtr(JNIEnv*, jclass) {
    return reinterpret_cast<jlong>(&finalize_changeset);
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_OsMapChangeSet_nativeGetDeletionCount(JNIEnv*, jclass,
                                                             jlong native_ptr) {
    DictionaryChangeSet& change_set = *reinterpret_cast<DictionaryChangeSet*>(native_ptr);
    return change_set.deletions.size();
}

JNIEXPORT jobjectArray JNICALL
Java_io_realm_internal_OsMapChangeSet_nativeGetStringKeyInsertions(JNIEnv* env, jclass,
                                                                   jlong native_ptr) {
    DictionaryChangeSet& change_set = *reinterpret_cast<DictionaryChangeSet*>(native_ptr);
    return generate_change_set(env, change_set.insertions);
}

JNIEXPORT jobjectArray JNICALL
Java_io_realm_internal_OsMapChangeSet_nativeGetStringKeyModifications(JNIEnv* env, jclass,
                                                                      jlong native_ptr) {
    DictionaryChangeSet& change_set = *reinterpret_cast<DictionaryChangeSet*>(native_ptr);
    return generate_change_set(env, change_set.modifications);
}
