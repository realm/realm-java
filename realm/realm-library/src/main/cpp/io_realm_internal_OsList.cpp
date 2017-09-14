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

#include "io_realm_internal_OsList.h"

#include <list.hpp>
#include <results.hpp>
#include <shared_realm.hpp>

#include "observable_collection_wrapper.hpp"
#include "util.hpp"

using namespace realm;
using namespace realm::_impl;

typedef ObservableCollectionWrapper<List> ListWrapper;

static void finalize_list(jlong ptr)
{
    TR_ENTER_PTR(ptr)
    delete reinterpret_cast<ListWrapper*>(ptr);
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_OsList_nativeGetFinalizerPtr(JNIEnv*, jclass)
{
    TR_ENTER()
    return reinterpret_cast<jlong>(&finalize_list);
}

JNIEXPORT jlongArray JNICALL Java_io_realm_internal_OsList_nativeCreate(JNIEnv* env, jclass, jlong shared_realm_ptr, jlong row_ptr,
                                                                        jlong column_index)
{
    TR_ENTER_PTR(row_ptr)

    try {
        auto& row = *reinterpret_cast<realm::Row*>(row_ptr);

        if (!ROW_AND_COL_INDEX_AND_TYPE_VALID(env, &row, column_index, type_LinkList)) {
            return 0;
        }

        auto& shared_realm = *reinterpret_cast<SharedRealm*>(shared_realm_ptr);
        LinkViewRef link_view_ref(row.get_linklist(column_index));
        List list(shared_realm, link_view_ref);
        ListWrapper* wrapper_ptr = new ListWrapper(list);

        Table* target_table_ptr = &(link_view_ref)->get_target_table();
        LangBindHelper::bind_table_ptr(target_table_ptr);

        jlong ret[2];
        ret[0] = reinterpret_cast<jlong>(wrapper_ptr);
        ret[1] = reinterpret_cast<jlong>(target_table_ptr);

        jlongArray ret_array = env->NewLongArray(2);
        if (!ret_array) {
            ThrowException(env, OutOfMemory, "Could not allocate memory to create OsList.");
            return nullptr;
        }
        env->SetLongArrayRegion(ret_array, 0, 2, ret);
        return ret_array;
    }
    CATCH_STD()
    return nullptr;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_OsList_nativeGetRow(JNIEnv* env, jclass, jlong list_ptr,
                                                                   jlong column_index)
{
    TR_ENTER_PTR(list_ptr)

    try {
        auto& wrapper = *reinterpret_cast<ListWrapper*>(list_ptr);
        auto row = wrapper.collection().get(column_index);
        return reinterpret_cast<jlong>(new Row(std::move(row)));
    }
    CATCH_STD()
    return reinterpret_cast<jlong>(nullptr);
}

JNIEXPORT void JNICALL Java_io_realm_internal_OsList_nativeAddRow(JNIEnv* env, jclass, jlong list_ptr,
                                                                  jlong target_row_index)
{
    TR_ENTER_PTR(list_ptr)

    try {
        auto& wrapper = *reinterpret_cast<ListWrapper*>(list_ptr);
        wrapper.collection().add(static_cast<size_t>(target_row_index));
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_OsList_nativeInsertRow(JNIEnv* env, jclass, jlong list_ptr, jlong pos,
                                                                     jlong target_row_index)
{
    TR_ENTER_PTR(list_ptr)

    try {
        auto& wrapper = *reinterpret_cast<ListWrapper*>(list_ptr);
        wrapper.collection().insert(static_cast<size_t>(pos), static_cast<size_t>(target_row_index));
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_OsList_nativeSetRow(JNIEnv* env, jclass, jlong list_ptr, jlong pos,
                                                                  jlong target_row_index)
{
    TR_ENTER_PTR(list_ptr)

    try {
        auto& wrapper = *reinterpret_cast<ListWrapper*>(list_ptr);
        wrapper.collection().set(static_cast<size_t>(pos), static_cast<size_t>(target_row_index));
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_OsList_nativeMove(JNIEnv* env, jclass, jlong list_ptr,
                                                                jlong source_index, jlong target_index)
{
    TR_ENTER_PTR(list_ptr)

    try {
        auto& wrapper = *reinterpret_cast<ListWrapper*>(list_ptr);
        wrapper.collection().move(source_index, target_index);
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_OsList_nativeRemove(JNIEnv* env, jclass, jlong list_ptr, jlong index)
{
    TR_ENTER_PTR(list_ptr)

    try {
        auto& wrapper = *reinterpret_cast<ListWrapper*>(list_ptr);
        wrapper.collection().remove(index);
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_OsList_nativeRemoveAll(JNIEnv* env, jclass, jlong list_ptr)
{
    TR_ENTER_PTR(list_ptr)

    try {
        auto& wrapper = *reinterpret_cast<ListWrapper*>(list_ptr);
        wrapper.collection().remove_all();
    }
    CATCH_STD()
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_OsList_nativeSize(JNIEnv* env, jclass, jlong list_ptr)
{
    TR_ENTER_PTR(list_ptr)

    try {
        auto& wrapper = *reinterpret_cast<ListWrapper*>(list_ptr);
        return wrapper.collection().size();
    }
    CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_OsList_nativeGetQuery(JNIEnv* env, jclass, jlong list_ptr)
{
    TR_ENTER_PTR(list_ptr)

    try {
        auto& wrapper = *reinterpret_cast<ListWrapper*>(list_ptr);
        auto query = wrapper.collection().get_query();
        return reinterpret_cast<jlong>(new Query(std::move(query)));
    }
    CATCH_STD()
    return reinterpret_cast<jlong>(nullptr);
}

JNIEXPORT jboolean JNICALL Java_io_realm_internal_OsList_nativeIsValid(JNIEnv* env, jclass, jlong list_ptr)
{
    TR_ENTER_PTR(list_ptr)

    try {
        auto& wrapper = *reinterpret_cast<ListWrapper*>(list_ptr);
        return wrapper.collection().is_valid();
    }
    CATCH_STD()
    return JNI_FALSE;
}

JNIEXPORT void JNICALL Java_io_realm_internal_OsList_nativeDelete(JNIEnv* env, jclass, jlong list_ptr, jlong index)
{
    TR_ENTER_PTR(list_ptr)

    try {
        auto& wrapper = *reinterpret_cast<ListWrapper*>(list_ptr);
        wrapper.collection().delete_at(S(index));
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_OsList_nativeDeleteAll(JNIEnv* env, jclass, jlong list_ptr)
{
    TR_ENTER_PTR(list_ptr)

    try {
        auto& wrapper = *reinterpret_cast<ListWrapper*>(list_ptr);
        wrapper.collection().delete_all();
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_OsList_nativeStartListening(JNIEnv* env, jobject instance,
                                                                              jlong native_ptr)
{
    TR_ENTER_PTR(native_ptr)

    try {
        auto wrapper = reinterpret_cast<ListWrapper*>(native_ptr);
        wrapper->start_listening(env, instance);
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_OsList_nativeStopListening(JNIEnv* env, jobject, jlong native_ptr)
{
    TR_ENTER_PTR(native_ptr)

    try {
        auto wrapper = reinterpret_cast<ListWrapper*>(native_ptr);
        wrapper->stop_listening();
    }
    CATCH_STD()
}
