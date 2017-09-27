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


#include "java_accessor.hpp"
#include "java_sort_descriptor.hpp"
#include "util.hpp"
#include "jni_util/java_class.hpp"
#include "jni_util/java_method.hpp"

using namespace realm;
using namespace realm::_impl;
using namespace realm::jni_util;

SortDescriptor JavaSortDescriptor::sort_descriptor() const noexcept
{
    if (m_sort_desc_obj == nullptr) {
        return SortDescriptor();
    }

    return SortDescriptor(*get_table_ptr(), get_column_indices(), get_ascendings());
}

DistinctDescriptor JavaSortDescriptor::distinct_descriptor() const noexcept
{
    if (m_sort_desc_obj == nullptr) {
        return DistinctDescriptor();
    }
    return DistinctDescriptor(*get_table_ptr(), get_column_indices());
}

Table* JavaSortDescriptor::get_table_ptr() const noexcept
{
    static JavaMethod get_table_ptr_method(m_env, get_sort_desc_class(), "getTablePtr", "()J");
    jlong table_ptr = m_env->CallLongMethod(m_sort_desc_obj, get_table_ptr_method);
    return reinterpret_cast<Table*>(table_ptr);
}

std::vector<std::vector<size_t>> JavaSortDescriptor::get_column_indices() const noexcept
{
    static JavaMethod get_column_indices_method(m_env, get_sort_desc_class(), "getColumnIndices", "()[[J");
    jobjectArray column_indices =
        static_cast<jobjectArray>(m_env->CallObjectMethod(m_sort_desc_obj, get_column_indices_method));
    JObjectArrayAccessor<JLongArrayAccessor, jlongArray> arrays(m_env, column_indices);
    jsize arr_len = arrays.size();
    std::vector<std::vector<size_t>> indices;

    for (int i = 0; i < arr_len; ++i) {
        auto jni_long_array = arrays[i];
        std::vector<size_t> col_indices;
        for (int j = 0; j < jni_long_array.size(); ++j) {
            col_indices.push_back(static_cast<size_t>(jni_long_array[j]));
        }
        indices.push_back(std::move(col_indices));
    }
    return indices;
}

std::vector<bool> JavaSortDescriptor::get_ascendings() const noexcept
{
    static JavaMethod get_ascendings_method(m_env, get_sort_desc_class(), "getAscendings", "()[Z");

    jbooleanArray ascendings =
        static_cast<jbooleanArray>(m_env->CallObjectMethod(m_sort_desc_obj, get_ascendings_method));

    if (!ascendings) {
        return {};
    }

    JBooleanArrayAccessor ascending_array(m_env, ascendings);
    std::vector<bool> ascending_list;
    jsize arr_len = ascending_array.size();

    for (int i = 0; i < arr_len; i++) {
        ascending_list.push_back(static_cast<bool>(ascending_array[i]));
    }
    return ascending_list;
}

JavaClass const& JavaSortDescriptor::get_sort_desc_class() const noexcept
{
    static JavaClass sort_desc_class(m_env, "io/realm/internal/SortDescriptor");
    return sort_desc_class;
}

