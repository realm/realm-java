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

#include "io_realm_internal_OsCollectionChangeSet.h"

#include <collection_notifications.hpp>

#include "util.hpp"

using namespace realm;

static void finalize_changeset(jlong ptr);
static jintArray index_set_to_jint_array(JNIEnv* env, const IndexSet& index_set);
static jintArray index_set_to_indices_array(JNIEnv* env, const IndexSet& index_set);

static void finalize_changeset(jlong ptr)
{
    TR_ENTER_PTR(ptr);
    delete reinterpret_cast<CollectionChangeSet*>(ptr);
}

static jintArray index_set_to_jint_array(JNIEnv* env, const IndexSet& index_set)
{
    if (index_set.empty()) {
        return env->NewIntArray(0);
    }

    std::vector<jint> ranges_vector;
    for (auto& changes : index_set) {
        ranges_vector.push_back(changes.first);
        ranges_vector.push_back(changes.second - changes.first);
    }

    if (ranges_vector.size() > io_realm_internal_OsCollectionChangeSet_MAX_ARRAY_LENGTH) {
        std::ostringstream error_msg;
        error_msg << "There are too many ranges changed in this change set. They cannot fit into an array."
                  << " ranges_vector's size: " << ranges_vector.size()
                  << " Java array's max size: " << io_realm_internal_OsCollectionChangeSet_MAX_ARRAY_LENGTH << ".";
        ThrowException(env, IllegalState, error_msg.str());
        return nullptr;
    }
    jintArray jint_array = env->NewIntArray(static_cast<jsize>(ranges_vector.size()));
    env->SetIntArrayRegion(jint_array, 0, ranges_vector.size(), ranges_vector.data());
    return jint_array;
}

static jintArray index_set_to_indices_array(JNIEnv* env, const IndexSet& index_set)
{
    if (index_set.empty()) {
        return env->NewIntArray(0);
    }

    std::vector<jint> indices_vector;
    for (auto index : index_set.as_indexes()) {
        indices_vector.push_back(index);
    }
    if (indices_vector.size() > io_realm_internal_OsCollectionChangeSet_MAX_ARRAY_LENGTH) {
        std::ostringstream error_msg;
        error_msg << "There are too many indices in this change set. They cannot fit into an array."
                  << " indices_vector's size: " << indices_vector.size()
                  << " Java array's max size: " << io_realm_internal_OsCollectionChangeSet_MAX_ARRAY_LENGTH << ".";
        ThrowException(env, IllegalState, error_msg.str());
        return nullptr;
    }
    jintArray jint_array = env->NewIntArray(static_cast<jsize>(indices_vector.size()));
    env->SetIntArrayRegion(jint_array, 0, indices_vector.size(), indices_vector.data());
    return jint_array;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_OsCollectionChangeSet_nativeGetFinalizerPtr(JNIEnv*, jclass)
{
    TR_ENTER()
    return reinterpret_cast<jlong>(&finalize_changeset);
}

JNIEXPORT jintArray JNICALL Java_io_realm_internal_OsCollectionChangeSet_nativeGetRanges(JNIEnv* env, jclass,
                                                                                         jlong native_ptr, jint type)
{
    TR_ENTER_PTR(native_ptr)
    // no throws
    auto& change_set = *reinterpret_cast<CollectionChangeSet*>(native_ptr);
    switch (type) {
        case io_realm_internal_OsCollectionChangeSet_TYPE_DELETION:
            return index_set_to_jint_array(env, change_set.deletions);
        case io_realm_internal_OsCollectionChangeSet_TYPE_INSERTION:
            return index_set_to_jint_array(env, change_set.insertions);
        case io_realm_internal_OsCollectionChangeSet_TYPE_MODIFICATION:
            return index_set_to_jint_array(env, change_set.modifications_new);
        default:
            REALM_UNREACHABLE();
    }
}

JNIEXPORT jintArray JNICALL Java_io_realm_internal_OsCollectionChangeSet_nativeGetIndices(JNIEnv* env, jclass,
                                                                                          jlong native_ptr, jint type)
{
    TR_ENTER_PTR(native_ptr)
    // no throws
    auto& change_set = *reinterpret_cast<CollectionChangeSet*>(native_ptr);
    switch (type) {
        case io_realm_internal_OsCollectionChangeSet_TYPE_DELETION:
            return index_set_to_indices_array(env, change_set.deletions);
        case io_realm_internal_OsCollectionChangeSet_TYPE_INSERTION:
            return index_set_to_indices_array(env, change_set.insertions);
        case io_realm_internal_OsCollectionChangeSet_TYPE_MODIFICATION:
            return index_set_to_indices_array(env, change_set.modifications_new);
        default:
            REALM_UNREACHABLE();
    }
}
