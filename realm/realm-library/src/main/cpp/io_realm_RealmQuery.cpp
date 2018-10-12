/*
 * Copyright 2018 Realm Inc.
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

#include "io_realm_RealmQuery.h"

#include <results.hpp>

#include "util.hpp"


using namespace realm;

JNIEXPORT jstring JNICALL Java_io_realm_RealmQuery_nativeSerializeQuery(JNIEnv* env, jclass, jlong table_query_ptr, jlong descriptor_ptr)
{
    TR_ENTER()
    try {
        auto query = reinterpret_cast<Query *>(table_query_ptr);
        auto descriptor = reinterpret_cast<DescriptorOrdering*>(descriptor_ptr);
        std::string serialized_query = query->get_description() + " " + descriptor->get_description(query->get_table());
        return to_jstring(env, serialized_query);
    }
    CATCH_STD()
    return to_jstring(env, "");
}
