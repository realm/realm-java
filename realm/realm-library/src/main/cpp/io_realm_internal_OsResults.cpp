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

#include "io_realm_internal_OsResults.h"

#include <shared_realm.hpp>
#include <results.hpp>
#include <list.hpp>
#include <realm/util/optional.hpp>

#include "java_class_global_def.hpp"
#include "java_sort_descriptor.hpp"
#include "observable_collection_wrapper.hpp"
#include "util.hpp"

using namespace realm;
using namespace realm::jni_util;
using namespace realm::_impl;

typedef ObservableCollectionWrapper<Results> ResultsWrapper;

static void finalize_results(jlong ptr);

static void finalize_results(jlong ptr)
{
    TR_ENTER_PTR(ptr);
    delete reinterpret_cast<ResultsWrapper*>(ptr);
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_OsResults_nativeCreateResults(JNIEnv* env, jclass,
                                                                              jlong shared_realm_ptr, jlong query_ptr,
                                                                              jobject j_sort_desc,
                                                                              jobject j_distinct_desc)
{
    TR_ENTER()
    try {
        auto query = reinterpret_cast<Query*>(query_ptr);
        if (!QUERY_VALID(env, query)) {
            return reinterpret_cast<jlong>(nullptr);
        }

        auto shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
        DescriptorOrdering descriptor_ordering;
        if (j_sort_desc) {
            descriptor_ordering.append_sort(JavaSortDescriptor(env, j_sort_desc).sort_descriptor());
        }
        if (j_distinct_desc) {
            descriptor_ordering.append_distinct(JavaSortDescriptor(env, j_distinct_desc).distinct_descriptor());
        }
        Results results(shared_realm, *query, descriptor_ordering);
        auto wrapper = new ResultsWrapper(results);

        return reinterpret_cast<jlong>(wrapper);
    }
    CATCH_STD()
    return reinterpret_cast<jlong>(nullptr);
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_OsResults_nativeCreateSnapshot(JNIEnv* env, jclass, jlong native_ptr)
{
    TR_ENTER_PTR(native_ptr);
    try {
        auto wrapper = reinterpret_cast<ResultsWrapper*>(native_ptr);
        auto snapshot_results = wrapper->collection().snapshot();
        auto snapshot_wrapper = new ResultsWrapper(snapshot_results);
        return reinterpret_cast<jlong>(snapshot_wrapper);
    }
    CATCH_STD();
    return reinterpret_cast<jlong>(nullptr);
}

JNIEXPORT jboolean JNICALL Java_io_realm_internal_OsResults_nativeContains(JNIEnv* env, jclass, jlong native_ptr,
                                                                            jlong native_row_ptr)
{
    TR_ENTER_PTR(native_ptr);
    try {
        auto wrapper = reinterpret_cast<ResultsWrapper*>(native_ptr);
        auto row = reinterpret_cast<Row*>(native_row_ptr);
        size_t index = wrapper->collection().index_of(RowExpr(*row));
        return to_jbool(index != not_found);
    }
    CATCH_STD();
    return JNI_FALSE;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_OsResults_nativeGetRow(JNIEnv* env, jclass, jlong native_ptr,
                                                                       jint index)
{
    TR_ENTER_PTR(native_ptr)
    try {
        auto wrapper = reinterpret_cast<ResultsWrapper*>(native_ptr);
        auto row = wrapper->collection().get(static_cast<size_t>(index));
        return reinterpret_cast<jlong>(new Row(std::move(row)));
    }
    CATCH_STD()
    return reinterpret_cast<jlong>(nullptr);
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_OsResults_nativeFirstRow(JNIEnv* env, jclass, jlong native_ptr)
{
    TR_ENTER_PTR(native_ptr)
    try {
        auto wrapper = reinterpret_cast<ResultsWrapper*>(native_ptr);
        auto optional_row = wrapper->collection().first();
        if (optional_row) {
            return reinterpret_cast<jlong>(new Row(std::move(optional_row.value())));
        }
    }
    CATCH_STD()
    return reinterpret_cast<jlong>(nullptr);
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_OsResults_nativeLastRow(JNIEnv* env, jclass, jlong native_ptr)
{
    TR_ENTER_PTR(native_ptr)
    try {
        auto wrapper = reinterpret_cast<ResultsWrapper*>(native_ptr);
        auto optional_row = wrapper->collection().last();
        if (optional_row) {
            return reinterpret_cast<jlong>(new Row(std::move(optional_row.value())));
        }
    }
    CATCH_STD()
    return reinterpret_cast<jlong>(nullptr);
}

JNIEXPORT void JNICALL Java_io_realm_internal_OsResults_nativeClear(JNIEnv* env, jclass, jlong native_ptr)
{
    TR_ENTER_PTR(native_ptr)
    try {
        auto wrapper = reinterpret_cast<ResultsWrapper*>(native_ptr);
        wrapper->collection().clear();
    }
    CATCH_STD()
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_OsResults_nativeSize(JNIEnv* env, jclass, jlong native_ptr)
{
    TR_ENTER_PTR(native_ptr)
    try {
        auto wrapper = reinterpret_cast<ResultsWrapper*>(native_ptr);
        return static_cast<jlong>(wrapper->collection().size());
    }
    CATCH_STD()
    return 0;
}

JNIEXPORT jobject JNICALL Java_io_realm_internal_OsResults_nativeAggregate(JNIEnv* env, jclass, jlong native_ptr,
                                                                            jlong column_index, jbyte agg_func)
{
    TR_ENTER_PTR(native_ptr)
    try {
        auto wrapper = reinterpret_cast<ResultsWrapper*>(native_ptr);

        size_t index = S(column_index);
        Optional<Mixed> value;
        switch (agg_func) {
            case io_realm_internal_OsResults_AGGREGATE_FUNCTION_MINIMUM:
                value = wrapper->collection().min(index);
                break;
            case io_realm_internal_OsResults_AGGREGATE_FUNCTION_MAXIMUM:
                value = wrapper->collection().max(index);
                break;
            case io_realm_internal_OsResults_AGGREGATE_FUNCTION_AVERAGE: {
                Optional<double> value_count(wrapper->collection().average(index));
                if (value_count) {
                    value = Optional<Mixed>(Mixed(value_count.value()));
                }
                else {
                    value = Optional<Mixed>(0.0);
                }
                break;
            }
            case io_realm_internal_OsResults_AGGREGATE_FUNCTION_SUM:
                value = wrapper->collection().sum(index);
                break;
            default:
                REALM_UNREACHABLE();
        }

        if (!value) {
            return static_cast<jobject>(nullptr);
        }

        Mixed m = *value;
        switch (m.get_type()) {
            case type_Int:
                return JavaClassGlobalDef::new_long(env, m.get_int());
            case type_Float:
                return JavaClassGlobalDef::new_float(env, m.get_float());
            case type_Double:
                return JavaClassGlobalDef::new_double(env, m.get_double());
            case type_Timestamp:
                return JavaClassGlobalDef::new_date(env, m.get_timestamp());
            default:
                throw std::invalid_argument("Excepted numeric type");
        }
    }
    CATCH_STD()
    return static_cast<jobject>(nullptr);
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_OsResults_nativeSort(JNIEnv* env, jclass, jlong native_ptr,
                                                                     jobject j_sort_desc)
{
    TR_ENTER_PTR(native_ptr)
    try {
        auto wrapper = reinterpret_cast<ResultsWrapper*>(native_ptr);
        auto sorted_result = wrapper->collection().sort(JavaSortDescriptor(env, j_sort_desc).sort_descriptor());
        return reinterpret_cast<jlong>(new ResultsWrapper(sorted_result));
    }
    CATCH_STD()
    return reinterpret_cast<jlong>(nullptr);
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_OsResults_nativeDistinct(JNIEnv* env, jclass, jlong native_ptr,
                                                                         jobject j_distinct_desc)
{
    TR_ENTER_PTR(native_ptr)
    try {
        auto wrapper = reinterpret_cast<ResultsWrapper*>(native_ptr);
        auto distinct_result =
            wrapper->collection().distinct(JavaSortDescriptor(env, j_distinct_desc).distinct_descriptor());
        return reinterpret_cast<jlong>(new ResultsWrapper(distinct_result));
    }
    CATCH_STD()
    return reinterpret_cast<jlong>(nullptr);
}

JNIEXPORT void JNICALL Java_io_realm_internal_OsResults_nativeStartListening(JNIEnv* env, jobject instance,
                                                                              jlong native_ptr)
{
    TR_ENTER_PTR(native_ptr)

    try {
        auto wrapper = reinterpret_cast<ResultsWrapper*>(native_ptr);
        wrapper->start_listening(env, instance);
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_OsResults_nativeStopListening(JNIEnv* env, jobject, jlong native_ptr)
{
    TR_ENTER_PTR(native_ptr)

    try {
        auto wrapper = reinterpret_cast<ResultsWrapper*>(native_ptr);
        wrapper->stop_listening();
    }
    CATCH_STD()
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_OsResults_nativeGetFinalizerPtr(JNIEnv*, jclass)
{
    TR_ENTER()
    return reinterpret_cast<jlong>(&finalize_results);
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_OsResults_nativeWhere(JNIEnv* env, jclass, jlong native_ptr)
{
    TR_ENTER_PTR(native_ptr)
    try {
        auto wrapper = reinterpret_cast<ResultsWrapper*>(native_ptr);

        auto table_view = wrapper->collection().get_tableview();
        Query* query =
            new Query(table_view.get_parent(), std::unique_ptr<TableViewBase>(new TableView(std::move(table_view))));
        return reinterpret_cast<jlong>(query);
    }
    CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_OsResults_nativeIndexOf(JNIEnv* env, jclass, jlong native_ptr,
                                                                        jlong row_native_ptr)
{
    TR_ENTER_PTR(native_ptr)
    try {
        auto wrapper = reinterpret_cast<ResultsWrapper*>(native_ptr);
        auto row = reinterpret_cast<Row*>(row_native_ptr);

        return static_cast<jlong>(wrapper->collection().index_of(RowExpr(*row)));
    }
    CATCH_STD()
    return npos;
}

JNIEXPORT jboolean JNICALL Java_io_realm_internal_OsResults_nativeDeleteLast(JNIEnv* env, jclass, jlong native_ptr)
{
    TR_ENTER_PTR(native_ptr)
    try {
        auto wrapper = reinterpret_cast<ResultsWrapper*>(native_ptr);
        auto row = wrapper->collection().last();
        if (row && row->is_attached()) {
            row->move_last_over();
            return JNI_TRUE;
        }
    }
    CATCH_STD()
    return JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_io_realm_internal_OsResults_nativeDeleteFirst(JNIEnv* env, jclass, jlong native_ptr)
{
    TR_ENTER_PTR(native_ptr)

    try {
        auto wrapper = reinterpret_cast<ResultsWrapper*>(native_ptr);
        auto row = wrapper->collection().first();
        if (row && row->is_attached()) {
            row->move_last_over();
            return JNI_TRUE;
        }
    }
    CATCH_STD()
    return JNI_FALSE;
}

JNIEXPORT void JNICALL Java_io_realm_internal_OsResults_nativeDelete(JNIEnv* env, jclass, jlong native_ptr,
                                                                      jlong index)
{
    TR_ENTER_PTR(native_ptr)

    try {
        auto wrapper = reinterpret_cast<ResultsWrapper*>(native_ptr);
        auto row = wrapper->collection().get(index);
        if (row.is_attached()) {
            row.move_last_over();
        }
    }
    CATCH_STD()
}

JNIEXPORT jboolean JNICALL Java_io_realm_internal_OsResults_nativeIsValid(JNIEnv* env, jclass, jlong native_ptr)
{
    TR_ENTER_PTR(native_ptr)
    try {
        auto wrapper = reinterpret_cast<ResultsWrapper*>(native_ptr);
        return wrapper->collection().is_valid();
    }
    CATCH_STD()
    return JNI_FALSE;
}

JNIEXPORT jbyte JNICALL Java_io_realm_internal_OsResults_nativeGetMode(JNIEnv* env, jclass, jlong native_ptr)
{
    TR_ENTER_PTR(native_ptr)
    try {
        auto wrapper = reinterpret_cast<ResultsWrapper*>(native_ptr);
        switch (wrapper->collection().get_mode()) {
            case Results::Mode::Empty:
                return io_realm_internal_OsResults_MODE_EMPTY;
            case Results::Mode::Table:
                return io_realm_internal_OsResults_MODE_TABLE;
            case Results::Mode::Query:
                return io_realm_internal_OsResults_MODE_QUERY;
            case Results::Mode::LinkView:
                return io_realm_internal_OsResults_MODE_LINKVIEW;
            case Results::Mode::TableView:
                return io_realm_internal_OsResults_MODE_TABLEVIEW;
        }
    }
    CATCH_STD()
    return -1; // Invalid mode value
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_OsResults_nativeCreateResultsFromBacklinks(JNIEnv *env, jclass,
                                                                                           jlong shared_realm_ptr,
                                                                                           jlong row_ptr,
                                                                                           jlong src_table_ptr,
                                                                                           jlong src_col_index)
{
    TR_ENTER_PTR(row_ptr)
    Row* row = ROW(row_ptr);
    if (!ROW_VALID(env, row)) {
        return reinterpret_cast<jlong>(nullptr);
    }
    try {
        Table* src_table = TBL(src_table_ptr);
        TableView backlink_view = row->get_table()->get_backlink_view(row->get_index(), src_table, src_col_index);
        auto shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
        Results results(shared_realm, std::move(backlink_view));
        auto wrapper = new ResultsWrapper(results);
        return reinterpret_cast<jlong>(wrapper);
    }
    CATCH_STD()
    return reinterpret_cast<jlong>(nullptr);
}

JNIEXPORT void JNICALL Java_io_realm_internal_OsResults_nativeEvaluateQueryIfNeeded(JNIEnv* env, jclass,
                                                                                    jlong native_ptr,
                                                                                    jboolean wants_notifications)
{
    TR_ENTER_PTR(native_ptr)
    try {
        auto wrapper = reinterpret_cast<ResultsWrapper*>(native_ptr);
        wrapper->collection().evaluate_query_if_needed(wants_notifications);
    }
    CATCH_STD()
}
