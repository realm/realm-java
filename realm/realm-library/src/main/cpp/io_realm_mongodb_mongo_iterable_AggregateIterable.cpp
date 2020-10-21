/*
 * Copyright 2020 Realm Inc.
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

#include "io_realm_mongodb_mongo_iterable_AggregateIterable.h"

#include "java_class_global_def.hpp"
#include "java_network_transport.hpp"
#include "util.hpp"
#include "jni_util/java_method.hpp"
#include "jni_util/jni_utils.hpp"
#include "jni_util/bson_util.hpp"

#include <realm/util/optional.hpp>
#include <realm/object-store/util/bson/bson.hpp>
#include <realm/object-store/sync/remote_mongo_collection.hpp>
#include <jni_util/bson_util.hpp>
#include <jni.h>

using namespace realm;
using namespace realm::bson;
using namespace realm::jni_util;
using namespace realm::_impl;

static std::function<jobject(JNIEnv*, util::Optional<bson::BsonArray>)> collection_mapper_aggregate = [](JNIEnv* env, util::Optional<bson::BsonArray> array) {
    return array ? JniBsonProtocol::bson_to_jstring(env, *array) : NULL;
};

JNIEXPORT void JNICALL
Java_io_realm_mongodb_mongo_iterable_AggregateIterable_nativeAggregate(JNIEnv* env,
                                                                       jclass,
                                                                       jlong j_collection_ptr,
                                                                       jstring j_pipeline,
                                                                       jobject j_callback) {
    try {
        auto collection = reinterpret_cast<MongoCollection *>(j_collection_ptr);

        BsonArray bson_array(JniBsonProtocol::parse_checked(env, j_pipeline, Bson::Type::Array, "BSON pipeline must be a BsonArray"));

        collection->aggregate(bson_array, JavaNetworkTransport::create_result_callback(env, j_callback, collection_mapper_aggregate));
    }
    CATCH_STD()
}
