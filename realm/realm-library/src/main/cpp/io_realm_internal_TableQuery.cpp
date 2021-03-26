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

static void finalize_table_query(jlong ptr);

// Find --------------------------------------

JNIEXPORT jlong JNICALL Java_io_realm_internal_TableQuery_nativeFind(JNIEnv *env, jobject, jlong nativeQueryPtr) {
    Query *pQuery = Q(nativeQueryPtr);
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

JNIEXPORT jlong JNICALL Java_io_realm_internal_TableQuery_nativeSumInt(JNIEnv *env, jobject, jlong nativeQueryPtr,
                                                                       jlong columnKey) {
    Query *pQuery = Q(nativeQueryPtr);
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

JNIEXPORT jobject JNICALL Java_io_realm_internal_TableQuery_nativeMaximumInt(JNIEnv *env, jobject,
                                                                             jlong nativeQueryPtr, jlong columnKey) {
    Query *pQuery = Q(nativeQueryPtr);
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

JNIEXPORT jobject JNICALL Java_io_realm_internal_TableQuery_nativeMinimumInt(JNIEnv *env, jobject,
                                                                             jlong nativeQueryPtr, jlong columnKey) {
    Query *pQuery = Q(nativeQueryPtr);
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

JNIEXPORT jdouble JNICALL Java_io_realm_internal_TableQuery_nativeAverageInt(JNIEnv *env, jobject,
                                                                             jlong nativeQueryPtr, jlong columnKey) {
    Query *pQuery = Q(nativeQueryPtr);
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

JNIEXPORT jdouble JNICALL Java_io_realm_internal_TableQuery_nativeSumFloat(JNIEnv *env, jobject, jlong nativeQueryPtr,
                                                                           jlong columnKey) {
    Query *pQuery = Q(nativeQueryPtr);
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

JNIEXPORT jobject JNICALL Java_io_realm_internal_TableQuery_nativeMaximumFloat(JNIEnv *env, jobject,
                                                                               jlong nativeQueryPtr,
                                                                               jlong columnKey) {
    Query *pQuery = Q(nativeQueryPtr);
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

JNIEXPORT jobject JNICALL Java_io_realm_internal_TableQuery_nativeMinimumFloat(JNIEnv *env, jobject,
                                                                               jlong nativeQueryPtr,
                                                                               jlong columnKey) {
    Query *pQuery = Q(nativeQueryPtr);
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

JNIEXPORT jdouble JNICALL Java_io_realm_internal_TableQuery_nativeAverageFloat(JNIEnv *env, jobject,
                                                                               jlong nativeQueryPtr,
                                                                               jlong columnKey) {
    Query *pQuery = Q(nativeQueryPtr);
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
JNIEXPORT jlongArray JNICALL Java_io_realm_internal_TableQuery_nativeSumMixed(JNIEnv *env, jobject,
                                                                              jlong nativeQueryPtr, jlong columnKey) {
    Query *pQuery = Q(nativeQueryPtr);
    ConstTableRef pTable = pQuery->get_table();
    if (!TYPE_VALID(env, pTable, columnKey, col_type_Mixed)) {
        return nullptr;
    }
    try {
        Decimal128 decimal128 = pQuery->sum_mixed(ColKey(columnKey));
        RETURN_DECIMAL128_AS_JLONG_ARRAY__OR_NULL(decimal128)
    }
    CATCH_STD()
    return nullptr;
}

JNIEXPORT jdouble JNICALL Java_io_realm_internal_TableQuery_nativeSumDouble(JNIEnv *env, jobject,
                                                                            jlong nativeQueryPtr, jlong columnKey) {
    Query *pQuery = Q(nativeQueryPtr);
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

JNIEXPORT jobject JNICALL Java_io_realm_internal_TableQuery_nativeMaximumDouble(JNIEnv *env, jobject,
                                                                                jlong nativeQueryPtr,
                                                                                jlong columnKey) {
    Query *pQuery = Q(nativeQueryPtr);
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

JNIEXPORT jobject JNICALL Java_io_realm_internal_TableQuery_nativeMaximumMixed(JNIEnv *env, jobject,
                                                                               jlong nativeQueryPtr,
                                                                               jlong columnKey) {
    Query *pQuery = Q(nativeQueryPtr);
    ConstTableRef pTable = pQuery->get_table();
    if (!TYPE_VALID(env, pTable, columnKey, col_type_Mixed)) {
        return nullptr;
    }
    try {
        ObjKey return_ndx;
        Mixed result = pQuery->maximum_mixed(ColKey(columnKey), &return_ndx);
        if (bool(return_ndx)) {
            return JavaClassGlobalDef::new_mixed(env, result);
        } else {
            return JavaClassGlobalDef::new_mixed(env, Mixed());
        }
    }
    CATCH_STD()
    return nullptr;
}

JNIEXPORT jlongArray JNICALL Java_io_realm_internal_TableQuery_nativeMaximumDecimal128(JNIEnv *env, jobject,
                                                                                       jlong nativeQueryPtr,
                                                                                       jlong columnKey) {
    Query *pQuery = Q(nativeQueryPtr);
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

JNIEXPORT jlongArray JNICALL Java_io_realm_internal_TableQuery_nativeSumDecimal128(JNIEnv *env, jobject,
                                                                                   jlong nativeQueryPtr,
                                                                                   jlong columnKey) {
    Query *pQuery = Q(nativeQueryPtr);
    ConstTableRef pTable = pQuery->get_table();
    if (!TYPE_VALID(env, pTable, columnKey, col_type_Decimal)) {
        return 0;
    }
    try {
        Decimal128 decimal128 = pQuery->sum_decimal128(ColKey(columnKey));
        RETURN_DECIMAL128_AS_JLONG_ARRAY__OR_NULL(decimal128)
    }
    CATCH_STD()
    return 0;
}

JNIEXPORT jobject JNICALL Java_io_realm_internal_TableQuery_nativeMinimumDouble(JNIEnv *env, jobject,
                                                                                jlong nativeQueryPtr,
                                                                                jlong columnKey) {
    Query *pQuery = Q(nativeQueryPtr);
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

JNIEXPORT jobject JNICALL Java_io_realm_internal_TableQuery_nativeMinimumMixed(JNIEnv *env, jobject,
                                                                               jlong nativeQueryPtr,
                                                                               jlong columnKey) {
    Query *pQuery = Q(nativeQueryPtr);
    ConstTableRef pTable = pQuery->get_table();
    if (!TYPE_VALID(env, pTable, columnKey, col_type_Mixed)) {
        return nullptr;
    }
    try {
        ObjKey return_ndx;
        const Mixed result = pQuery->minimum_mixed(ColKey(columnKey), &return_ndx);
        if (bool(return_ndx)) {
            return JavaClassGlobalDef::new_mixed(env, result);
        } else {
            return JavaClassGlobalDef::new_mixed(env, Mixed());
        }
    }
    CATCH_STD()
    return nullptr;
}

JNIEXPORT jlongArray JNICALL Java_io_realm_internal_TableQuery_nativeMinimumDecimal128(JNIEnv *env, jobject,
                                                                                       jlong nativeQueryPtr,
                                                                                       jlong columnKey) {
    Query *pQuery = Q(nativeQueryPtr);
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

JNIEXPORT jdouble JNICALL Java_io_realm_internal_TableQuery_nativeAverageDouble(JNIEnv *env, jobject,
                                                                                jlong nativeQueryPtr,
                                                                                jlong columnKey) {
    Query *pQuery = Q(nativeQueryPtr);
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

JNIEXPORT jlongArray JNICALL Java_io_realm_internal_TableQuery_nativeAverageMixed(JNIEnv *env, jobject,
                                                                                  jlong nativeQueryPtr,
                                                                                  jlong columnKey) {
    Query *pQuery = Q(nativeQueryPtr);
    ConstTableRef pTable = pQuery->get_table();
    if (!TYPE_VALID(env, pTable, columnKey, col_type_Mixed)) {
        return nullptr;
    }
    try {
        Decimal128 decimal128 = pQuery->average_mixed(ColKey(columnKey));
        RETURN_DECIMAL128_AS_JLONG_ARRAY__OR_NULL(decimal128)
    }
    CATCH_STD()
    return nullptr;
}

JNIEXPORT jlongArray JNICALL Java_io_realm_internal_TableQuery_nativeAverageDecimal128(JNIEnv *env, jobject,
                                                                                       jlong nativeQueryPtr,
                                                                                       jlong columnKey) {
    Query *pQuery = Q(nativeQueryPtr);
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
JNIEXPORT jobject JNICALL Java_io_realm_internal_TableQuery_nativeMaximumTimestamp(JNIEnv *env, jobject,
                                                                                   jlong nativeQueryPtr,
                                                                                   jlong columnKey) {
    Query *pQuery = Q(nativeQueryPtr);
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

JNIEXPORT jobject JNICALL Java_io_realm_internal_TableQuery_nativeMinimumTimestamp(JNIEnv *env, jobject,
                                                                                   jlong nativeQueryPtr,
                                                                                   jlong columnKey) {
    Query *pQuery = Q(nativeQueryPtr);
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

JNIEXPORT jlong JNICALL Java_io_realm_internal_TableQuery_nativeCount(JNIEnv *env, jobject, jlong nativeQueryPtr) {
    Query *pQuery = Q(nativeQueryPtr);
    try {
        return static_cast<jlong>(pQuery->count());
    }
    CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_TableQuery_nativeRemove(JNIEnv *env, jobject, jlong nativeQueryPtr) {
    Query *pQuery = Q(nativeQueryPtr);
    try {
        return static_cast<jlong>(pQuery->remove());
    }
    CATCH_STD()
    return 0;
}

static void finalize_table_query(jlong ptr) {
    delete Q(ptr);
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_TableQuery_nativeGetFinalizerPtr(JNIEnv *, jclass) {
    return reinterpret_cast<jlong>(&finalize_table_query);
}

void rawQuery(jlong j_query_ptr,
              const std::string filter,
              const std::vector<Mixed> &args,
              jlong j_mapping_ptr,
              bool onlyOrder = false) {
    auto &query = *reinterpret_cast<Query *>(j_query_ptr);

    query_parser::KeyPathMapping mapping;
    if (j_mapping_ptr) {
        mapping = *reinterpret_cast<query_parser::KeyPathMapping *>(j_mapping_ptr);
    }

    Query predicate = query.get_table()->query(filter, args, mapping);

    if (!onlyOrder) {
        query.and_query(predicate);
    }

    if (auto parsed_ordering = predicate.get_ordering()) {
        auto ordering = query.get_ordering();
        ordering->append(*parsed_ordering);

        query.set_ordering(std::make_unique<DescriptorOrdering>(*ordering));
    }
}

JNIEXPORT void JNICALL
Java_io_realm_internal_TableQuery_nativeRawPredicate(JNIEnv *env,
                                                     jobject,
                                                     jlong j_query_ptr,
                                                     jstring j_filter,
                                                     jlongArray j_args,
                                                     jlong j_mapping_ptr) {
    try {
        JStringAccessor filter(env, j_filter); // throws

        JLongArrayAccessor arguments(env, j_args);
        std::vector<Mixed> args;

        for (jsize i = 0; i < arguments.size(); ++i) {
            auto &value = *reinterpret_cast<JavaValue *>(arguments[i]);
            args.push_back(value.to_mixed());
        }

        rawQuery(j_query_ptr, std::string(filter), args, j_mapping_ptr);
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL
Java_io_realm_internal_TableQuery_nativeRawDescriptor(JNIEnv *env,
                                                      jobject,
                                                      jlong j_query_ptr,
                                                      jstring j_descriptor,
                                                      jlong j_mapping_ptr) {
    try {
        JStringAccessor filter(env, j_descriptor); // throws
        std::vector<Mixed> args(0);

        rawQuery(j_query_ptr, "TRUEPREDICATE " + std::string(filter), args, j_mapping_ptr, true);
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL
Java_io_realm_internal_TableQuery_nativeBeginGroup(JNIEnv *env,
                                                   jobject,
                                                   jlong j_query_ptr) {
    try {
        auto query = reinterpret_cast<Query *>(j_query_ptr);
        query->group();
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL
Java_io_realm_internal_TableQuery_nativeEndGroup(JNIEnv *env,
                                                 jobject,
                                                 jlong j_query_ptr) {
    try {
        auto query = reinterpret_cast<Query *>(j_query_ptr);
        query->end_group();
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL
Java_io_realm_internal_TableQuery_nativeOr(JNIEnv *env,
                                           jobject,
                                           jlong j_query_ptr) {
    try {
        auto query = reinterpret_cast<Query *>(j_query_ptr);
        query->Or();
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL
Java_io_realm_internal_TableQuery_nativeNot(JNIEnv *env,
                                            jobject,
                                            jlong j_query_ptr) {
    try {
        auto query = reinterpret_cast<Query *>(j_query_ptr);
        query->Not();
    }
    CATCH_STD()
}

JNIEXPORT jstring JNICALL
Java_io_realm_internal_TableQuery_nativeValidateQuery(JNIEnv *env, jobject,
                                                      jlong nativeQueryPtr) {
    try {
        const std::string str = Q(nativeQueryPtr)->validate();
        StringData sd(str);
        return to_jstring(env, sd);
    }
    CATCH_STD()
    return nullptr;
}
