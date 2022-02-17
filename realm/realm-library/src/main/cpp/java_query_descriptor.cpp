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
#include "java_query_descriptor.hpp"
#include "util.hpp"
#include "jni_util/java_class.hpp"
#include "jni_util/java_method.hpp"

using namespace realm;
using namespace realm::_impl;
using namespace realm::jni_util;

SortDescriptor JavaQueryDescriptor::sort_descriptor() const noexcept
{
    if (m_sort_desc_obj == nullptr) {
        return SortDescriptor();
    }

    return SortDescriptor(get_column_keys(), get_ascendings());
}

DistinctDescriptor JavaQueryDescriptor::distinct_descriptor() const noexcept
{
    if (m_sort_desc_obj == nullptr) {
        return DistinctDescriptor();
    }
    return DistinctDescriptor(get_column_keys());
}

std::vector<std::vector<ColKey>> JavaQueryDescriptor::get_column_keys() const noexcept
{
    static JavaMethod get_column_keys_method(m_env, get_sort_desc_class(), "getColumnKeys", "()[[J");
    jobjectArray column_indices =
            static_cast<jobjectArray>(m_env->CallObjectMethod(m_sort_desc_obj, get_column_keys_method));
    JObjectArrayAccessor<JLongArrayAccessor, jlongArray> arrays(m_env, column_indices);
    jsize arr_len = arrays.size();
    std::vector<std::vector<ColKey>> keys;

    for (int i = 0; i < arr_len; ++i) {
        auto jni_long_array = arrays[i];
        std::vector<ColKey> col_keys;
        for (int j = 0; j < jni_long_array.size(); ++j) {
            col_keys.push_back(ColKey(jni_long_array[j]));
        }
        keys.push_back(std::move(col_keys));
    }
    return keys;
}

std::vector<bool> JavaQueryDescriptor::get_ascendings() const noexcept
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

JavaClass const& JavaQueryDescriptor::get_sort_desc_class() const noexcept
{
    static JavaClass sort_desc_class(m_env, "io/realm/internal/core/QueryDescriptor");
    return sort_desc_class;
}
