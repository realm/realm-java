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

#include <realm/object-store/shared_realm.hpp>
#include <realm/object-store/results.hpp>
#include <realm/object-store/list.hpp>
#include <realm/util/optional.hpp>

#include "java_class_global_def.hpp"
#include "java_object_accessor.hpp"
#include "java_query_descriptor.hpp"
#include "observable_collection_wrapper.hpp"
#include "util.hpp"

using namespace realm;
using namespace realm::jni_util;
using namespace realm::_impl;

typedef ObservableCollectionWrapper<Results> ResultsWrapper;

static void finalize_results(jlong ptr);

static void finalize_results(jlong ptr)
{
    delete reinterpret_cast<ResultsWrapper*>(ptr);
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_OsResults_nativeCreateResults(JNIEnv* env, jclass,
                                                                             jlong shared_realm_ptr,
                                                                             jlong query_ptr,
                                                                             jlong descriptor_ordering_ptr)
{
    try {
        auto query = reinterpret_cast<Query*>(query_ptr);
        if (!TABLE_VALID(env, query->get_table())) {
            return reinterpret_cast<jlong>(nullptr);
        }

        auto shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
        auto descriptor_ordering = *(reinterpret_cast<DescriptorOrdering*>(descriptor_ordering_ptr));
        Results results(shared_realm, *query, descriptor_ordering);
        auto wrapper = new ResultsWrapper(results);

        return reinterpret_cast<jlong>(wrapper);
    }
    CATCH_STD()
    return reinterpret_cast<jlong>(nullptr);
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_OsResults_nativeCreateSnapshot(JNIEnv* env, jclass, jlong native_ptr)
{
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
                                                                            jlong native_obj_ptr)
{
    try {
        auto wrapper = reinterpret_cast<ResultsWrapper*>(native_ptr);
        const Obj* obj = reinterpret_cast<Obj*>(native_obj_ptr);
        size_t index = wrapper->collection().index_of(*obj);
        return to_jbool(index != not_found);
    }
    CATCH_STD();
    return JNI_FALSE;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_OsResults_nativeGetRow(JNIEnv* env, jclass, jlong native_ptr,
                                                                       jint index)
{
    try {
        auto wrapper = reinterpret_cast<ResultsWrapper*>(native_ptr);
        auto obj = wrapper->collection().get(static_cast<size_t>(index));
        return reinterpret_cast<jlong>(new Obj(std::move(obj)));
    }
    CATCH_STD()
    return reinterpret_cast<jlong>(nullptr);
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_OsResults_nativeFirstRow(JNIEnv* env, jclass, jlong native_ptr)
{
    try {
        auto wrapper = reinterpret_cast<ResultsWrapper*>(native_ptr);
        auto optional_obj = wrapper->collection().first();
        if (optional_obj) {
            return reinterpret_cast<jlong>(new Obj(std::move(optional_obj.value())));
        }
    }
    CATCH_STD()
    return reinterpret_cast<jlong>(nullptr);
}


JNIEXPORT jlong JNICALL Java_io_realm_internal_OsResults_nativeLastRow(JNIEnv* env, jclass, jlong native_ptr)
{
    try {
        auto wrapper = reinterpret_cast<ResultsWrapper*>(native_ptr);
        auto optional_obj = wrapper->collection().last();
        if (optional_obj) {
            return reinterpret_cast<jlong>(new Obj(optional_obj.value()));
        }
    }
    CATCH_STD()
    return reinterpret_cast<jlong>(nullptr);
}

JNIEXPORT void JNICALL Java_io_realm_internal_OsResults_nativeClear(JNIEnv* env, jclass, jlong native_ptr)
{
    try {
        auto wrapper = reinterpret_cast<ResultsWrapper*>(native_ptr);
        wrapper->collection().clear();
    }
    CATCH_STD()
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_OsResults_nativeSize(JNIEnv* env, jclass, jlong native_ptr)
{
    try {
        auto wrapper = reinterpret_cast<ResultsWrapper*>(native_ptr);
        return static_cast<jlong>(wrapper->collection().size());
    }
    CATCH_STD()
    return 0;
}

JNIEXPORT jobject JNICALL Java_io_realm_internal_OsResults_nativeAggregate(JNIEnv* env, jclass, jlong native_ptr,
                                                                            jlong column_key, jbyte agg_func)
{
    try {
        auto wrapper = reinterpret_cast<ResultsWrapper*>(native_ptr);

        ColKey col_key(column_key);
        util::Optional<Mixed> value;
        switch (agg_func) {
            case io_realm_internal_OsResults_AGGREGATE_FUNCTION_MINIMUM:
                value = wrapper->collection().min(col_key);
                break;
            case io_realm_internal_OsResults_AGGREGATE_FUNCTION_MAXIMUM:
                value = wrapper->collection().max(col_key);
                break;
            case io_realm_internal_OsResults_AGGREGATE_FUNCTION_AVERAGE: {
                util::Optional<Mixed> value_count(wrapper->collection().average(col_key));
                if (value_count) {
                    value = value_count;
                }
                else {
                    value = util::Optional<Mixed>(0.0);
                }
                break;
            }
            case io_realm_internal_OsResults_AGGREGATE_FUNCTION_SUM:
                value = wrapper->collection().sum(col_key);
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
    try {
        auto wrapper = reinterpret_cast<ResultsWrapper*>(native_ptr);
        auto sorted_result = wrapper->collection().sort(JavaQueryDescriptor(env, j_sort_desc).sort_descriptor());
        return reinterpret_cast<jlong>(new ResultsWrapper(sorted_result));
    }
    CATCH_STD()
    return reinterpret_cast<jlong>(nullptr);
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_OsResults_nativeDistinct(JNIEnv* env, jclass, jlong native_ptr,
                                                                         jobject j_distinct_desc)
{
    try {
        auto wrapper = reinterpret_cast<ResultsWrapper*>(native_ptr);
        auto distinct_result =
            wrapper->collection().distinct(JavaQueryDescriptor(env, j_distinct_desc).distinct_descriptor());
        return reinterpret_cast<jlong>(new ResultsWrapper(distinct_result));
    }
    CATCH_STD()
    return reinterpret_cast<jlong>(nullptr);
}

JNIEXPORT void JNICALL Java_io_realm_internal_OsResults_nativeStartListening(JNIEnv* env, jobject instance,
                                                                              jlong native_ptr)
{
    try {
        auto wrapper = reinterpret_cast<ResultsWrapper*>(native_ptr);
        wrapper->start_listening(env, instance);
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_OsResults_nativeStopListening(JNIEnv* env, jobject, jlong native_ptr)
{
    try {
        auto wrapper = reinterpret_cast<ResultsWrapper*>(native_ptr);
        wrapper->stop_listening();
    }
    CATCH_STD()
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_OsResults_nativeGetFinalizerPtr(JNIEnv*, jclass)
{
    return reinterpret_cast<jlong>(&finalize_results);
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_OsResults_nativeWhere(JNIEnv* env, jclass, jlong native_ptr)
{
    try {
        auto wrapper = reinterpret_cast<ResultsWrapper*>(native_ptr);

        auto table_view = wrapper->collection().get_tableview();
        Query* query =
            new Query(table_view.get_parent(), std::unique_ptr<ConstTableView>(new TableView(std::move(table_view))));
        return reinterpret_cast<jlong>(query);
    }
    CATCH_STD()
    return 0;
}

JNIEXPORT jstring JNICALL Java_io_realm_internal_OsResults_toJSON(JNIEnv* env, jclass, jlong native_ptr, jint maxDepth)
{
    try {
        auto wrapper = reinterpret_cast<ResultsWrapper*>(native_ptr);

        auto table_view = wrapper->collection().get_tableview();
        std::stringstream ss;
        table_view.to_json(ss, maxDepth);
        return to_jstring(env, ss.str().c_str());
    }
    CATCH_STD()
    return nullptr;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_OsResults_nativeIndexOf(JNIEnv* env, jclass, jlong native_ptr,
                                                                        jlong obj_native_ptr)
{
    try {
        auto wrapper = reinterpret_cast<ResultsWrapper*>(native_ptr);
        Obj* obj = reinterpret_cast<Obj*>(obj_native_ptr);

        return static_cast<jlong>(wrapper->collection().index_of(*obj));
    }
    CATCH_STD()
    return npos;
}

JNIEXPORT jboolean JNICALL Java_io_realm_internal_OsResults_nativeDeleteLast(JNIEnv* env, jclass, jlong native_ptr)
{
    try {
        auto wrapper = reinterpret_cast<ResultsWrapper*>(native_ptr);
        auto obj = wrapper->collection().last();
        if (obj && obj->is_valid()) {
            obj->remove();
            return JNI_TRUE;
        }
    }
    CATCH_STD()
    return JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_io_realm_internal_OsResults_nativeDeleteFirst(JNIEnv* env, jclass, jlong native_ptr)
{
    try {
        auto wrapper = reinterpret_cast<ResultsWrapper*>(native_ptr);
        auto obj = wrapper->collection().first();
        if (obj && obj->is_valid()) {
            obj->remove();
            return JNI_TRUE;
        }
    }
    CATCH_STD()
    return JNI_FALSE;
}

static inline void update_objects(JNIEnv* env, jlong results_ptr, jstring& j_field_name, JavaValue& value) {
    try {
        auto wrapper = reinterpret_cast<ResultsWrapper*>(results_ptr);
        JavaContext ctx(env, wrapper->collection().get_realm(), wrapper->collection().get_object_schema());
        JStringAccessor prop_name(env, j_field_name);
        wrapper->collection().set_property_value(ctx, prop_name, value);
    }
    CATCH_STD()
}


JNIEXPORT void JNICALL Java_io_realm_internal_OsResults_nativeSetNull(JNIEnv* env, jclass, jlong native_ptr, jstring j_field_name)
{
    auto value = JavaValue();
    update_objects(env, native_ptr, j_field_name, value);
}

JNIEXPORT void JNICALL Java_io_realm_internal_OsResults_nativeSetBoolean(JNIEnv* env, jclass, jlong native_ptr, jstring j_field_name, jboolean j_value)
{
    JavaValue value(j_value);
    update_objects(env, native_ptr, j_field_name, value);
}

JNIEXPORT void JNICALL Java_io_realm_internal_OsResults_nativeSetInt(JNIEnv* env, jclass, jlong native_ptr, jstring j_field_name, jlong j_value)
{
    JavaValue value(j_value);
    update_objects(env, native_ptr, j_field_name, value);
}

JNIEXPORT void JNICALL Java_io_realm_internal_OsResults_nativeSetFloat(JNIEnv* env, jclass, jlong native_ptr, jstring j_field_name, jfloat j_value)
{
    JavaValue value(j_value);
    update_objects(env, native_ptr, j_field_name, value);
}

JNIEXPORT void JNICALL Java_io_realm_internal_OsResults_nativeSetDouble(JNIEnv* env, jclass, jlong native_ptr, jstring j_field_name, jdouble j_value)
{
    JavaValue value(j_value);
    update_objects(env, native_ptr, j_field_name, value);
}

JNIEXPORT void JNICALL Java_io_realm_internal_OsResults_nativeSetString(JNIEnv* env, jclass, jlong native_ptr, jstring j_field_name, jstring j_value)
{
    JStringAccessor str(env, j_value);
    JavaValue value = str.is_null() ? JavaValue() : JavaValue(std::string(str));
    update_objects(env, native_ptr, j_field_name, value);
}

JNIEXPORT void JNICALL Java_io_realm_internal_OsResults_nativeSetBinary(JNIEnv* env, jclass, jlong native_ptr, jstring j_field_name, jbyteArray j_value)
{
    auto data = OwnedBinaryData(JByteArrayAccessor(env, j_value).transform<BinaryData>());
    JavaValue value(data);
    update_objects(env, native_ptr, j_field_name, value);
}

JNIEXPORT void JNICALL Java_io_realm_internal_OsResults_nativeSetTimestamp(JNIEnv* env, jclass, jlong native_ptr, jstring j_field_name, jlong j_value)
{
    JavaValue value(from_milliseconds(j_value));
    update_objects(env, native_ptr, j_field_name, value);
}

JNIEXPORT void JNICALL Java_io_realm_internal_OsResults_nativeSetDecimal128(JNIEnv* env, jclass, jlong native_ptr, jstring j_field_name, jlong low, jlong high)
{

    Decimal128::Bid128 raw = {static_cast<uint64_t>(low), static_cast<uint64_t>(high)};
    Decimal128 decimal128 = Decimal128(raw);
    JavaValue value(decimal128);
    update_objects(env, native_ptr, j_field_name, value);
}

JNIEXPORT void JNICALL Java_io_realm_internal_OsResults_nativeSetObjectId(JNIEnv* env, jclass, jlong native_ptr, jstring j_field_name, jstring j_value)
{
    JStringAccessor data(env, j_value);
    ObjectId objectId = ObjectId(StringData(data).data());
    JavaValue value(objectId);
    update_objects(env, native_ptr, j_field_name, value);
}

JNIEXPORT void JNICALL Java_io_realm_internal_OsResults_nativeSetUUID(JNIEnv* env, jclass, jlong native_ptr, jstring j_field_name, jstring j_value)
{
    JStringAccessor data(env, j_value);
    UUID uuid = UUID(StringData(data).data());
    JavaValue value(uuid);
    update_objects(env, native_ptr, j_field_name, value);
}

JNIEXPORT void JNICALL Java_io_realm_internal_OsResults_nativeSetObject(JNIEnv* env, jclass, jlong native_ptr, jstring j_field_name, jlong row_ptr)
{
    JavaValue value(reinterpret_cast<Obj*>(row_ptr));
    update_objects(env, native_ptr, j_field_name, value);
}

JNIEXPORT void JNICALL Java_io_realm_internal_OsResults_nativeSetList(JNIEnv* env, jclass, jlong native_ptr, jstring j_field_name, jlong builder_ptr)
{
    // OsObjectBuilder has been used to build up the list we want to insert. This means the
    // fake object described by the OsObjectBuilder only contains one property, namely the list we
    // want to insert and this list is assumed to be at index = 0.
    std::map<ColKey, JavaValue> builder = *reinterpret_cast<std::map<ColKey, JavaValue>*>(builder_ptr);
    REALM_ASSERT_DEBUG(builder.size() == 1);
    update_objects(env, native_ptr, j_field_name, builder.begin()->second);
}

JNIEXPORT void JNICALL Java_io_realm_internal_OsResults_nativeDelete(JNIEnv* env, jclass, jlong native_ptr,
                                                                      jlong index)
{
    try {
        auto wrapper = reinterpret_cast<ResultsWrapper*>(native_ptr);
        auto obj = wrapper->collection().get(index);
        if (obj.is_valid()) {
            obj.remove();
        }
    }
    CATCH_STD()
}

JNIEXPORT jboolean JNICALL Java_io_realm_internal_OsResults_nativeIsValid(JNIEnv* env, jclass, jlong native_ptr)
{
    try {
        auto wrapper = reinterpret_cast<ResultsWrapper*>(native_ptr);
        return wrapper->collection().is_valid();
    }
    CATCH_STD()
    return JNI_FALSE;
}

JNIEXPORT jbyte JNICALL Java_io_realm_internal_OsResults_nativeGetMode(JNIEnv* env, jclass, jlong native_ptr)
{
    try {
        auto wrapper = reinterpret_cast<ResultsWrapper*>(native_ptr);
        switch (wrapper->collection().get_mode()) {
            case Results::Mode::Empty:
                return io_realm_internal_OsResults_MODE_EMPTY;
            case Results::Mode::Table:
                return io_realm_internal_OsResults_MODE_TABLE;
            case Results::Mode::Collection:
                return io_realm_internal_OsResults_MODE_LIST;
            case Results::Mode::Query:
                return io_realm_internal_OsResults_MODE_QUERY;
            case Results::Mode::LinkList:
                return io_realm_internal_OsResults_MODE_LINK_LIST;
            case Results::Mode::TableView:
                return io_realm_internal_OsResults_MODE_TABLEVIEW;
            default:
                throw std::logic_error(util::format("Unexpected state: %1", static_cast<uint8_t>(wrapper->collection().get_mode())));
        }
    }
    CATCH_STD()
    return -1;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_OsResults_nativeCreateResultsFromBacklinks(JNIEnv *env, jclass,
                                                                                           jlong shared_realm_ptr,
                                                                                           jlong obj_ptr,
                                                                                           jlong src_table_ref_ptr,
                                                                                           jlong src_col_key)
{
    Obj* obj = OBJ(obj_ptr);
    if (!ROW_VALID(env, obj)) {
        return reinterpret_cast<jlong>(nullptr);
    }
    try {
        TableRef src_table = TBL_REF(src_table_ref_ptr);
        TableView backlink_view = obj->get_backlink_view(src_table, ColKey(src_col_key));
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
    try {
        auto wrapper = reinterpret_cast<ResultsWrapper*>(native_ptr);
        wrapper->collection().evaluate_query_if_needed(wants_notifications);
    }
    CATCH_STD()
}


JNIEXPORT jlong JNICALL Java_io_realm_internal_OsResults_nativeFreeze(JNIEnv* env, jclass, jlong native_ptr, jlong frozen_realm_native_ptr)
{
    try {
        auto wrapper = reinterpret_cast<ResultsWrapper*>(native_ptr);
        auto frozen_realm = *(reinterpret_cast<SharedRealm*>(frozen_realm_native_ptr));
        Results results = wrapper->collection().freeze(frozen_realm);
        return reinterpret_cast<jlong>(new ResultsWrapper(results));
    }
    CATCH_STD()
    return reinterpret_cast<jlong>(nullptr);
}
