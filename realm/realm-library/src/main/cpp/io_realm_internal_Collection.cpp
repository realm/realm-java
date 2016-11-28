/*
 * Copyright 2016 Realm Inc.
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

#include <jni.h>
#include "io_realm_internal_Collection.h"

#include <vector>

#include <object-store/src/shared_realm.hpp>
#include <object-store/src/results.hpp>

#include "util.hpp"

using namespace realm;

static void finalize_results(jlong ptr);
static void finalize_notification_token(jlong ptr);

static void finalize_results(jlong ptr)
{
    TR_ENTER_PTR(ptr);
    delete reinterpret_cast<Results*>(ptr);
}

static void finalize_notification_token(jlong ptr)
{
    TR_ENTER_PTR(ptr);
    // NotificationToken can be closed by NotificationToken.close(). Then ptr will be reset in that case.
    if (ptr) {
        delete reinterpret_cast<NotificationToken*>(ptr);
    }
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_Collection_nativeCreateResults(JNIEnv* env, jclass, jlong shared_realm_ptr, jlong query_ptr,
        jlongArray colunm_indices, jbooleanArray jsort_orders)
{
    TR_ENTER()
    try {
        auto shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
        auto query = reinterpret_cast<Query*>(query_ptr);

        JniBooleanArray order(env, jsort_orders);
        JniLongArray indices(env, colunm_indices);

        std::vector<bool> sort_order;
        std::vector<std::vector<size_t>> sort_indices;
        for(jsize i = 0; i < order.len(); ++i) {
            sort_order.push_back(to_bool(order[i]));
            sort_indices.push_back(std::vector<size_t> { S(indices[i]) });
        }

        SortDescriptor sort_descriptor(*(query->get_table().get()), sort_indices, sort_order);
        Results results(shared_realm, *query, sort_descriptor);
        return reinterpret_cast<jlong>(new Results(std::move(results)));
    } CATCH_STD()
    return reinterpret_cast<jlong>(nullptr);
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_Collection_nativeCreateSnapshot(JNIEnv* env, jclass, jlong native_ptr)
{
    TR_ENTER_PTR(native_ptr)
    try {
        auto results = reinterpret_cast<Results*>(native_ptr);
        auto snapshot = results->snapshot();
        return reinterpret_cast<jlong>(new Results(snapshot));
    } CATCH_STD()
    return reinterpret_cast<jlong>(nullptr);
}

JNIEXPORT jboolean JNICALL
Java_io_realm_internal_Collection_nativeContains(JNIEnv *env, jclass, jlong native_ptr, jlong native_row_ptr)
{
    TR_ENTER_PTR(native_ptr);
    try {
        auto results = reinterpret_cast<Results*>(native_ptr);
        auto row = reinterpret_cast<Row*>(native_row_ptr);
        size_t index = results->index_of(*row);
        return to_jbool(index != not_found);
    } CATCH_STD();
    return JNI_FALSE;
}

// FIXME: we don't use it at the moment
JNIEXPORT jlong JNICALL
Java_io_realm_internal_Collection_nativeGetRow(JNIEnv *env, jclass, jlong native_ptr, jint index)
{
    TR_ENTER_PTR(native_ptr)
    try {
        auto results = reinterpret_cast<Results*>(native_ptr);
        auto row = results->get(static_cast<size_t>(index));
        return reinterpret_cast<jlong>(new Row(std::move(row)));
    } CATCH_STD()
    return reinterpret_cast<jlong>(nullptr);
}

JNIEXPORT void JNICALL
Java_io_realm_internal_Collection_nativeClear(JNIEnv *env, jclass, jlong native_ptr)
{
    TR_ENTER_PTR(native_ptr)
    try {
        auto results = reinterpret_cast<Results*>(native_ptr);
        results->clear();
    } CATCH_STD()
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_Collection_nativeSize(JNIEnv *env, jclass, jlong native_ptr)
{
    TR_ENTER_PTR(native_ptr)
    try {
        auto results = reinterpret_cast<Results*>(native_ptr);
        return static_cast<jlong>(results->size());
    } CATCH_STD()
    return 0;
}

JNIEXPORT jobject JNICALL
Java_io_realm_internal_Collection_nativeAggregate(JNIEnv *env, jclass, jlong native_ptr, jlong column_index,
        jbyte agg_func)
{
    TR_ENTER_PTR(native_ptr)
    try {
        auto results = reinterpret_cast<Results *>(native_ptr);

        size_t index = S(column_index);
        Optional<Mixed> value;
        switch (agg_func) {
            case io_realm_internal_Collection_AGGREGATE_FUNCTION_MINIMUM:
                value = results->min(index);
                break;
            case io_realm_internal_Collection_AGGREGATE_FUNCTION_MAXIMUM:
                value = results->max(index);
                break;
            case io_realm_internal_Collection_AGGREGATE_FUNCTION_AVERAGE:
                value = results->average(index);
                break;
            case io_realm_internal_Collection_AGGREGATE_FUNCTION_SUM:
                value = results->sum(index);
                break;
        }

        if (!value) {
            return static_cast<jobject>(nullptr);
        }

        Mixed m = *value;
        switch (m.get_type()) {
            case type_Int:
                return NewLong(env, m.get_int());
            case type_Float:
                return NewFloat(env, m.get_float());
            case type_Double:
                return NewDouble(env, m.get_double());
            case type_Timestamp:
                return NewDate(env, m.get_timestamp());
            default:
                throw std::invalid_argument("Excepted numeric type");
        }
    } CATCH_STD()
    return static_cast<jobject>(nullptr);
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_Collection_nativeSort(JNIEnv *env, jclass, jlong native_ptr, jlongArray colunm_indices,
        jbooleanArray jsort_orders)
{
    TR_ENTER_PTR(native_ptr)
    try {
        auto results = reinterpret_cast<Results*>(native_ptr);

        JniBooleanArray order(env, jsort_orders);
        JniLongArray indices(env, colunm_indices);

        if (order.len() != indices.len()) {
            throw std::invalid_argument("Number of columns and sorting orders do not match.");
        }

        std::vector<bool> sort_orders;
        std::vector<std::vector<size_t>> sort_indices;
        for(jsize i = 0; i < order.len(); ++i) {
            sort_orders.push_back(to_bool(order[i]));
            sort_indices.push_back(std::vector<size_t> { S(indices[i]) });
        }

        SortDescriptor sort_descriptor(*(results->get_query().get_table().get()), sort_indices, sort_orders);
        auto sorted_result = results->sort(std::move(sort_descriptor));
        return reinterpret_cast<jlong>(new Results(std::move(sorted_result)));
    } CATCH_STD()
    return reinterpret_cast<jlong>(nullptr);
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_Collection_nativeAddListener(JNIEnv* env, jobject instance, jlong native_ptr)
{
    TR_ENTER_PTR(native_ptr)

    try {
        auto results = reinterpret_cast<Results*>(native_ptr);

        // FIXME: Those need to be freed for all the corner cases!
        jobject weak_results = env->NewWeakGlobalRef(instance);

        auto cb = [=](realm::CollectionChangeSet const& changes,
                                   std::exception_ptr err) {
            jclass results_class = env->GetObjectClass(weak_results);
            jmethodID notify_method = env->GetMethodID(results_class, "notifyChangeListeners", "()V");
            env->CallVoidMethod(weak_results, notify_method);
        };

        NotificationToken token =  results->add_notification_callback(cb);
        return reinterpret_cast<jlong>(new NotificationToken(std::move(token)));
    } CATCH_STD()

    return reinterpret_cast<jlong>(nullptr);
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_Collection_nativeGetFinalizerPtr(JNIEnv *, jclass)
{
    TR_ENTER()
    return reinterpret_cast<jlong>(&finalize_results);
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_Collection_nativeNotificationTokenGetFinalizerPtr(JNIEnv *, jclass)
{
    TR_ENTER()
    return reinterpret_cast<jlong>(&finalize_notification_token);
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_Collection_nativeNotificationTokenClose(JNIEnv *, jclass, jlong native_ptr)
{
    TR_ENTER_PTR(native_ptr)
    delete reinterpret_cast<NotificationToken*>(native_ptr);
}
