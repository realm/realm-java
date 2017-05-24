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


#include "java_sort_descriptor.hpp"
#include "util.hpp"
#include "jni_util/java_method.hpp"

using namespace realm;
using namespace realm::_impl;
using namespace realm::jni_util;

JavaSortDescriptor::operator realm::SortDescriptor() const noexcept
{
    if (m_sort_desc_obj == nullptr) {
        return SortDescriptor();
    }

    // Cache the method IDs.
    static JavaMethod getColumnIndices(m_env, m_sort_desc_obj, "getColumnIndices", "()[[J");
    static JavaMethod getAscendings(m_env, m_sort_desc_obj, "getAscendings", "()[Z");
    static JavaMethod getTablePtr(m_env, m_sort_desc_obj, "getTablePtr", "()J");

    jobjectArray column_indices =
        static_cast<jobjectArray>(m_env->CallObjectMethod(m_sort_desc_obj, getColumnIndices));
    jbooleanArray ascendings = static_cast<jbooleanArray>(m_env->CallObjectMethod(m_sort_desc_obj, getAscendings));
    jlong table_ptr = m_env->CallLongMethod(m_sort_desc_obj, getTablePtr);

    JniArrayOfArrays<JniLongArray, jlongArray> arrays(m_env, column_indices);
    JniBooleanArray ascending_array(m_env, ascendings);
    jsize arr_len = arrays.len();

    std::vector<std::vector<size_t>> indices;
    std::vector<bool> ascending_list;

    for (int i = 0; i < arr_len; ++i) {
        JniLongArray& jni_long_array = arrays[i];
        std::vector<size_t> col_indices;
        for (int j = 0; j < jni_long_array.len(); ++j) {
            col_indices.push_back(static_cast<size_t>(jni_long_array[j]));
        }
        indices.push_back(std::move(col_indices));
        if (ascendings) {
            ascending_list.push_back(static_cast<bool>(ascending_array[i]));
        }
    }

    return ascendings
               ? SortDescriptor(*reinterpret_cast<Table*>(table_ptr), std::move(indices), std::move(ascending_list))
               : SortDescriptor(*reinterpret_cast<Table*>(table_ptr), std::move(indices));
}
