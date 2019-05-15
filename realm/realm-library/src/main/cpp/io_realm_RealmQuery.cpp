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
#include <shared_realm.hpp>
#if REALM_ENABLE_SYNC
#include <sync/partial_sync.hpp>
#endif

#include "util.hpp"


using namespace realm;

JNIEXPORT jstring JNICALL Java_io_realm_RealmQuery_nativeSerializeQuery(JNIEnv* env, jclass, jlong table_query_ptr, jlong descriptor_ptr)
{
    TR_ENTER()
    try {
        auto query = reinterpret_cast<Query*>(table_query_ptr);
        auto descriptor = reinterpret_cast<DescriptorOrdering*>(descriptor_ptr);
        std::string serialized_query = query->get_description();
        std::string serialized_descriptor = descriptor->get_description(query->get_table());
        if (serialized_descriptor.empty()) {
            return to_jstring(env, serialized_query);
        } else {
            std::string result = serialized_query + " " + serialized_descriptor;
            return to_jstring(env, result);
        }
    }
    CATCH_STD()
    return to_jstring(env, "");
}

JNIEXPORT jlong JNICALL Java_io_realm_RealmQuery_nativeSubscribe(JNIEnv* env, jclass, jlong shared_realm_ptr,
        jstring j_name, jlong table_query_ptr, jlong descriptor_ptr, REALM_UNUSED jlong time_to_live_ms, REALM_UNUSED jboolean update)
{
    TR_ENTER()
    try {
        auto realm = *reinterpret_cast<SharedRealm*>(shared_realm_ptr);
        auto name = util::Optional<std::string>(JStringAccessor(env, j_name));
        auto query = reinterpret_cast<Query*>(table_query_ptr);
        auto descriptor = reinterpret_cast<DescriptorOrdering*>(descriptor_ptr);
        Results r(realm, *query, *descriptor);
#if REALM_ENABLE_SYNC
        RowExpr row = partial_sync::subscribe_blocking(r, name, util::Optional<int64_t>(time_to_live_ms), update);
        return to_jlong_or_not_found(row.get_index());
#endif
    }
    CATCH_STD()
    return realm::npos;
}
