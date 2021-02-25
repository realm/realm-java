/*
 * Copyright 2014 Realm Inc.
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

#include "io_realm_internal_TableQuery.h"

#include <realm.hpp>
#include <realm/query_expression.hpp>
#include <realm/table.hpp>
#include <realm/parser/keypath_mapping.hpp>
#include <realm/parser/query_parser.hpp>

#include <realm/object-store/shared_realm.hpp>
#include <realm/object-store/object_store.hpp>
#include <realm/object-store/results.hpp>

#include "java_accessor.hpp"
#include "java_object_accessor.hpp"
#include "java_class_global_def.hpp"
#include "util.hpp"

using namespace realm;
using namespace realm::jni_util;
using namespace realm::_impl;

typedef std::vector<JavaValue> ArgumentList;

static void finalize_table_query(jlong ptr);

// Find --------------------------------------

JNIEXPORT jlong JNICALL Java_io_realm_internal_TableQuery_nativeFind(JNIEnv* env, jobject, jlong nativeQueryPtr)
{
    Query*pQuery = Q(nativeQueryPtr);
    try {
        auto ordering = pQuery->get_ordering();
        ObjKey objKey;

        if (ordering) {
            auto all = pQuery->find_all(*ordering);
            objKey = all.size() > 0 ? all.get_key(0) : ObjKey();
        } else {
            objKey = pQuery->find();
        }
        
        pQuery->set_ordering(std::make_unique<DescriptorOrdering>(*ordering));

        return to_jlong_or_not_found(objKey);
    }
    CATCH_STD()
    return -1;
}

// Integer Aggregates

JNIEXPORT jlong JNICALL Java_io_realm_internal_TableQuery_nativeSumInt(JNIEnv* env, jobject, jlong nativeQueryPtr,
                                                                       jlong columnKey)
{
    Query* pQuery = Q(nativeQueryPtr);
    ConstTableRef pTable = pQuery->get_table();
    if (!TYPE_VALID(env, pTable, columnKey, col_type_Int)) {
        return 0;
    }
    try {
        return pQuery->sum_int(ColKey(columnKey));
    }
    CATCH_STD()
    return 0;
}

JNIEXPORT jobject JNICALL Java_io_realm_internal_TableQuery_nativeMaximumInt(JNIEnv* env, jobject,
                                                                             jlong nativeQueryPtr, jlong columnKey)
{
    Query* pQuery = Q(nativeQueryPtr);
    ConstTableRef pTable = pQuery->get_table();
    if (!TYPE_VALID(env, pTable, columnKey, col_type_Int)) {
        return nullptr;
    }
    try {
        ObjKey return_ndx;
        int64_t result = pQuery->maximum_int(ColKey(columnKey), &return_ndx);
        if (bool(return_ndx)) {
            return JavaClassGlobalDef::new_long(env, result);
        }
        return 0;
    }
    CATCH_STD()
    return nullptr;
}

JNIEXPORT jobject JNICALL Java_io_realm_internal_TableQuery_nativeMinimumInt(JNIEnv* env, jobject,
                                                                             jlong nativeQueryPtr, jlong columnKey)
{
    Query* pQuery = Q(nativeQueryPtr);
    ConstTableRef pTable = pQuery->get_table();
    if (!TYPE_VALID(env, pTable, columnKey, col_type_Int)) {
        return nullptr;
    }
    try {
        ObjKey return_ndx;
        int64_t result = pQuery->minimum_int(ColKey(columnKey), &return_ndx);
        if (bool(return_ndx)) {
            return JavaClassGlobalDef::new_long(env, result);
        }
    }
    CATCH_STD()
    return nullptr;
}

JNIEXPORT jdouble JNICALL Java_io_realm_internal_TableQuery_nativeAverageInt(JNIEnv* env, jobject,
                                                                             jlong nativeQueryPtr, jlong columnKey)
{
    Query* pQuery = Q(nativeQueryPtr);
    ConstTableRef pTable = pQuery->get_table();
    if (!TYPE_VALID(env, pTable, columnKey, col_type_Int)) {
        return 0;
    }
    try {
        double avg = pQuery->average_int(ColKey(columnKey));
        return avg;
    }
    CATCH_STD()
    return 0;
}


// float Aggregates

JNIEXPORT jdouble JNICALL Java_io_realm_internal_TableQuery_nativeSumFloat(JNIEnv* env, jobject, jlong nativeQueryPtr,
                                                                           jlong columnKey)
{
    Query* pQuery = Q(nativeQueryPtr);
    ConstTableRef pTable = pQuery->get_table();
    if (!TYPE_VALID(env, pTable, columnKey, col_type_Float)) {
        return 0;
    }
    try {
        return pQuery->sum_float(ColKey(columnKey));
    }
    CATCH_STD()
    return 0;
}

JNIEXPORT jobject JNICALL Java_io_realm_internal_TableQuery_nativeMaximumFloat(JNIEnv* env, jobject,
                                                                               jlong nativeQueryPtr,
                                                                               jlong columnKey)
{
    Query* pQuery = Q(nativeQueryPtr);
    ConstTableRef pTable = pQuery->get_table();
    if (!TYPE_VALID(env, pTable, columnKey, col_type_Float)) {
        return nullptr;
    }
    try {
        ObjKey return_ndx;
        float result = pQuery->maximum_float(ColKey(columnKey), &return_ndx);
        if (bool(return_ndx)) {
            return JavaClassGlobalDef::new_float(env, result);
        }
    }
    CATCH_STD()
    return nullptr;
}

JNIEXPORT jobject JNICALL Java_io_realm_internal_TableQuery_nativeMinimumFloat(JNIEnv* env, jobject,
                                                                               jlong nativeQueryPtr,
                                                                               jlong columnKey)
{
    Query* pQuery = Q(nativeQueryPtr);
    ConstTableRef pTable = pQuery->get_table();
    if (!TYPE_VALID(env, pTable, columnKey, col_type_Float)) {
        return nullptr;
    }
    try {
        ObjKey return_ndx;
        float result = pQuery->minimum_float(ColKey(columnKey), &return_ndx);
        if (bool(return_ndx)) {
            return JavaClassGlobalDef::new_float(env, result);
        }
    }
    CATCH_STD()
    return nullptr;
}

JNIEXPORT jdouble JNICALL Java_io_realm_internal_TableQuery_nativeAverageFloat(JNIEnv* env, jobject,
                                                                               jlong nativeQueryPtr,
                                                                               jlong columnKey)
{
    Query* pQuery = Q(nativeQueryPtr);
    ConstTableRef pTable = pQuery->get_table();
    if (!TYPE_VALID(env, pTable, columnKey, col_type_Float)) {
        return 0;
    }
    try {
        return pQuery->average_float(ColKey(columnKey));
    }
    CATCH_STD()
    return 0;
}

// double Aggregates

JNIEXPORT jdouble JNICALL Java_io_realm_internal_TableQuery_nativeSumDouble(JNIEnv* env, jobject,
                                                                            jlong nativeQueryPtr, jlong columnKey)
{
    Query* pQuery = Q(nativeQueryPtr);
    ConstTableRef pTable = pQuery->get_table();
    if (!TYPE_VALID(env, pTable, columnKey, col_type_Double)) {
        return 0;
    }
    try {
        return pQuery->sum_double(ColKey(columnKey));
    }
    CATCH_STD()
    return 0;
}

JNIEXPORT jobject JNICALL Java_io_realm_internal_TableQuery_nativeMaximumDouble(JNIEnv* env, jobject,
                                                                                jlong nativeQueryPtr,
                                                                                jlong columnKey)
{
    Query* pQuery = Q(nativeQueryPtr);
    ConstTableRef pTable = pQuery->get_table();
    if (!TYPE_VALID(env, pTable, columnKey, col_type_Double)) {
        return nullptr;
    }
    try {
        ObjKey return_ndx;
        double result = pQuery->maximum_double(ColKey(columnKey), &return_ndx);
        if (bool(return_ndx)) {
            return JavaClassGlobalDef::new_double(env, result);
        }
    }
    CATCH_STD()
    return nullptr;
}

JNIEXPORT jlongArray JNICALL Java_io_realm_internal_TableQuery_nativeMaximumDecimal128(JNIEnv* env, jobject,
                                                                                jlong nativeQueryPtr,
                                                                                jlong columnKey)
{
    Query* pQuery = Q(nativeQueryPtr);
    ConstTableRef pTable = pQuery->get_table();
    if (!TYPE_VALID(env, pTable, columnKey, col_type_Decimal)) {
        return nullptr;
    }
    try {
        Decimal128 decimal128 = pQuery->maximum_decimal128(ColKey(columnKey));
        RETURN_DECIMAL128_AS_JLONG_ARRAY__OR_NULL(decimal128)
    }
    CATCH_STD()
    return nullptr;
}

JNIEXPORT jlongArray JNICALL Java_io_realm_internal_TableQuery_nativeSumDecimal128(JNIEnv* env, jobject,
                                                                            jlong nativeQueryPtr, jlong columnKey)
{
    Query* pQuery = Q(nativeQueryPtr);
    ConstTableRef pTable = pQuery->get_table();
    if (!TYPE_VALID(env, pTable, columnKey, col_type_Decimal)) {
        return 0;
    }
    try {

//        Decimal128 decimal128 = pQuery->sum_decimal128(ColKey(columnKey)); //FIXME waiting for Core to add sum_decimal into query.hpp
        Decimal128 decimal128 = pQuery->get_table()->sum_decimal(ColKey(columnKey));
        RETURN_DECIMAL128_AS_JLONG_ARRAY__OR_NULL(decimal128)
    }
    CATCH_STD()
    return 0;
}

JNIEXPORT jobject JNICALL Java_io_realm_internal_TableQuery_nativeMinimumDouble(JNIEnv* env, jobject,
                                                                                jlong nativeQueryPtr,
                                                                                jlong columnKey)
{
    Query* pQuery = Q(nativeQueryPtr);
    ConstTableRef pTable = pQuery->get_table();
    if (!TYPE_VALID(env, pTable, columnKey, col_type_Double)) {
        return nullptr;
    }
    try {
        ObjKey return_ndx;
        double result = pQuery->minimum_double(ColKey(columnKey), &return_ndx);
        if (bool(return_ndx)) {
            return JavaClassGlobalDef::new_double(env, result);
        }
    }
    CATCH_STD()
    return nullptr;
}

JNIEXPORT jlongArray JNICALL Java_io_realm_internal_TableQuery_nativeMinimumDecimal128(JNIEnv* env, jobject,
                                                                                jlong nativeQueryPtr,
                                                                                jlong columnKey)
{
    Query* pQuery = Q(nativeQueryPtr);
    ConstTableRef pTable = pQuery->get_table();
    if (!TYPE_VALID(env, pTable, columnKey, col_type_Decimal)) {
        return nullptr;
    }
    try {
        Decimal128 decimal128 = pQuery->minimum_decimal128(ColKey(columnKey));
        RETURN_DECIMAL128_AS_JLONG_ARRAY__OR_NULL(decimal128)
    }
    CATCH_STD()
    return nullptr;
}

JNIEXPORT jdouble JNICALL Java_io_realm_internal_TableQuery_nativeAverageDouble(JNIEnv* env, jobject,
                                                                                jlong nativeQueryPtr,
                                                                                jlong columnKey)
{
    Query* pQuery = Q(nativeQueryPtr);
    ConstTableRef pTable = pQuery->get_table();
    if (!TYPE_VALID(env, pTable, columnKey, col_type_Double)) {
        return 0;
    }
    try {
        return pQuery->average_double(ColKey(columnKey));
    }
    CATCH_STD()
    return 0;
}

JNIEXPORT jlongArray JNICALL Java_io_realm_internal_TableQuery_nativeAverageDecimal128(JNIEnv* env, jobject,
                                                                                jlong nativeQueryPtr,
                                                                                jlong columnKey)
{
    Query* pQuery = Q(nativeQueryPtr);
    ConstTableRef pTable = pQuery->get_table();
    if (!TYPE_VALID(env, pTable, columnKey, col_type_Decimal)) {
        return nullptr;
    }
    try {
        Decimal128 decimal128 = pQuery->average_decimal128(ColKey(columnKey));
        RETURN_DECIMAL128_AS_JLONG_ARRAY__OR_NULL(decimal128)
    }
    CATCH_STD()
    return nullptr;
}

// date aggregates
// FIXME: This is a rough workaround while waiting for https://github.com/realm/realm-core/issues/1745 to be solved
JNIEXPORT jobject JNICALL Java_io_realm_internal_TableQuery_nativeMaximumTimestamp(JNIEnv* env, jobject,
                                                                                   jlong nativeQueryPtr,
                                                                                   jlong columnKey)
{
    Query* pQuery = Q(nativeQueryPtr);
    ConstTableRef pTable = pQuery->get_table();
    if (!TYPE_VALID(env, pTable, columnKey, col_type_Timestamp)) {
        return nullptr;
    }
    try {
        ObjKey return_ndx;
        Timestamp result = pQuery->find_all().maximum_timestamp(ColKey(columnKey), &return_ndx);
        if (bool(return_ndx) && !result.is_null()) {
            return JavaClassGlobalDef::new_long(env, to_milliseconds(result));
        }
    }
    CATCH_STD()
    return nullptr;
}

JNIEXPORT jobject JNICALL Java_io_realm_internal_TableQuery_nativeMinimumTimestamp(JNIEnv* env, jobject,
                                                                                   jlong nativeQueryPtr,
                                                                                   jlong columnKey)
{
    Query* pQuery = Q(nativeQueryPtr);
    ConstTableRef pTable = pQuery->get_table();
    if (!TYPE_VALID(env, pTable, columnKey, col_type_Timestamp)) {
        return nullptr;
    }
    try {
        ObjKey return_ndx;
        Timestamp result = pQuery->find_all().minimum_timestamp(ColKey(columnKey), &return_ndx);
        if (bool(return_ndx) && !result.is_null()) {
            return JavaClassGlobalDef::new_long(env, to_milliseconds(result));
        }
    }
    CATCH_STD()
    return nullptr;
}

// Count, Remove

JNIEXPORT jlong JNICALL Java_io_realm_internal_TableQuery_nativeCount(JNIEnv* env, jobject, jlong nativeQueryPtr)
{
    Query* pQuery = Q(nativeQueryPtr);
    try {
        return static_cast<jlong>(pQuery->count());
    }
    CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_TableQuery_nativeRemove(JNIEnv* env, jobject, jlong nativeQueryPtr)
{
    Query* pQuery = Q(nativeQueryPtr);
    try {
        return static_cast<jlong>(pQuery->remove());
    }
    CATCH_STD()
    return 0;
}

static void finalize_table_query(jlong ptr)
{
    delete Q(ptr);
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_TableQuery_nativeGetFinalizerPtr(JNIEnv*, jclass)
{
    return reinterpret_cast<jlong>(&finalize_table_query);
}

JNIEXPORT void JNICALL
Java_io_realm_internal_TableQuery_nativeRawPredicate(JNIEnv *env,
                                                     jclass,
                                                     jlong j_query_ptr,
                                                     jboolean j_is_or_connected,
                                                     jstring j_filter,
                                                     jlong j_args,
                                                     jlong j_mapping_ptr) {
    try {
        auto query = reinterpret_cast<Query *>(j_query_ptr);

        query_parser::KeyPathMapping mapping;
        if (j_mapping_ptr) {
            mapping = *reinterpret_cast<query_parser::KeyPathMapping *>(j_mapping_ptr);
        }

        JStringAccessor filter(env, j_filter); // throws

        auto data = *reinterpret_cast<ArgumentList *>(j_args);
        std::vector<Mixed> args(data.size());

        for (unsigned long i = 0; i < data.size(); i = i + 1) {
            args[i] = data[i].to_mixed();
        }

        Query predicate = query->get_table()->query(filter, args, mapping);

        if(B(j_is_or_connected)){
            query->Or();
        }

        query->and_query(predicate);

        if (auto parsed_ordering = predicate.get_ordering()) {
            auto ordering = query->get_ordering();
            ordering->append(*parsed_ordering);

            query->set_ordering(std::make_unique<DescriptorOrdering>(*ordering));
        }

        query->validate();

        data.clear();
    }
    CATCH_STD()
}
