/*
 * Copyright 2019 Realm Inc.
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

#include "io_realm_internal_core_IncludeDescriptor.h"

#include <realm/parser/parser.hpp>
#include <realm/parser/query_builder.hpp>
#include <realm/views.hpp>
#include <object_schema.hpp>
#include <object_store.hpp>
#include <property.hpp>
#include <schema.hpp>
#include <shared_realm.hpp>

#include "java_accessor.hpp"
#include "java_query_descriptor.hpp"
#include "util.hpp"


using namespace realm;
using namespace realm::util;
using namespace realm::_impl;

static void finalize_descriptor(jlong ptr)
{
    TR_ENTER_PTR(ptr)
    delete reinterpret_cast<IncludeDescriptor*>(ptr);
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_core_IncludeDescriptor_nativeGetFinalizerMethodPtr(JNIEnv* env, jclass)
{
    TR_ENTER()
    try {
        return reinterpret_cast<jlong>(&finalize_descriptor);
    }
    CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_core_IncludeDescriptor_nativeCreate(JNIEnv* env, jclass, jlong starting_table_ptr, jlongArray column_indexes, jlongArray table_pointers)
{
    TR_ENTER()
    try {
        JLongArrayAccessor table_arr(env, table_pointers);
        JLongArrayAccessor index_arr(env, column_indexes);
        auto starting_table = reinterpret_cast<Table*>(starting_table_ptr);
        std::vector<LinkPathPart> parts;
        parts.reserve(index_arr.size());
        for (int i = 0; i < index_arr.size(); ++i) {
            auto col_index = static_cast<size_t>(index_arr[i]);
            auto table_ptr = reinterpret_cast<Table *>(table_arr[i]);
            if (table_ptr == nullptr) {
                parts.emplace_back(LinkPathPart(col_index));
            }
            else {
                const ConstTableRef ref = table_ptr->get_table_ref();
                parts.emplace_back(LinkPathPart(col_index, ref));
            }
        }

        std::vector<std::vector<LinkPathPart>> include_path;
        include_path.reserve(1);
        include_path.emplace_back(parts);
        return reinterpret_cast<jlong>(new IncludeDescriptor(*starting_table, include_path));
    }
    CATCH_STD()
    return reinterpret_cast<jlong>(nullptr);
}
