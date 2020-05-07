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

#include "io_realm_internal_objectstore_OsRemoteMongoCollection.h"

#include "java_class_global_def.hpp"
#include "java_network_transport.hpp"
#include "util.hpp"
#include "util_sync.hpp"
#include "jni_util/java_method.hpp"
#include "jni_util/jni_utils.hpp"
#include "object-store/src/util/bson/bson.hpp"

#include <realm/util/optional.hpp>
#include <sync/app.hpp>
#include <sync/sync_user.hpp>
#include <sync/remote_mongo_database.hpp>
#include <sync/remote_mongo_collection.hpp>

using namespace realm;
using namespace realm::app;
using namespace realm::bson;
using namespace realm::jni_util;
using namespace realm::_impl;

static std::function<jobject(JNIEnv*, uint64_t)> collection_mapper_count = [](JNIEnv* env, uint64_t result) {
    return JavaClassGlobalDef::new_long(env, result);
};

static std::function<jobject(JNIEnv*, util::Optional<ObjectId>)> collection_mapper_insert_one = [](JNIEnv* env, util::Optional<ObjectId> result) {
    return JavaClassGlobalDef::new_object_id(env, result.value());
};

static void finalize_collection(jlong ptr) {
    delete reinterpret_cast<RemoteMongoCollection*>(ptr);
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_objectstore_OsRemoteMongoCollection_nativeGetFinalizerMethodPtr(JNIEnv*, jclass) {
    return reinterpret_cast<jlong>(&finalize_collection);
}

void JNICALL
Java_io_realm_internal_objectstore_OsRemoteMongoCollection_nativeCount(JNIEnv* env,
                                                                       jclass,
                                                                       jlong j_collection_ptr,
                                                                       jstring j_filter,
                                                                       jlong j_limit,
                                                                       jobject j_callback) {
    try {
        RemoteMongoCollection* collection = reinterpret_cast<RemoteMongoCollection*>(j_collection_ptr);
        uint64_t limit = std::uint64_t(j_limit);
        bson::BsonDocument bson_filter(jstring_to_bson(env, j_filter));
        collection->count(bson_filter, limit, JavaNetworkTransport::create_result_callback(env, j_callback, collection_mapper_count));
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL
Java_io_realm_internal_objectstore_OsRemoteMongoCollection_nativeInsertOne(JNIEnv* env,
                                                                           jclass,
                                                                           jlong j_collection_ptr,
                                                                           jstring j_document,
                                                                           jobject j_callback) {
    try {
        RemoteMongoCollection* collection = reinterpret_cast<RemoteMongoCollection*>(j_collection_ptr);
        bson::BsonDocument bson_filter(jstring_to_bson(env, j_document));
        collection->insert_one(bson_filter, JavaNetworkTransport::create_result_callback(env, j_callback, collection_mapper_insert_one));
    }
    CATCH_STD()
}










///*
// * Copyright 2020 Realm Inc.
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// * http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//#include "io_realm_internal_objectstore_OsRemoteMongoCollection.h"
//
//#include "java_class_global_def.hpp"
//#include "java_network_transport.hpp"
//#include "util.hpp"
//#include "jni_util/java_method.hpp"
//#include "jni_util/jni_utils.hpp"
//#include "object-store/src/util/bson/bson.hpp"
//
//#include <realm/util/optional.hpp>
//#include <sync/app.hpp>
//#include <sync/sync_user.hpp>
//#include <sync/remote_mongo_database.hpp>
//#include <sync/remote_mongo_collection.hpp>
//
//using namespace realm;
//using namespace realm::app;
//using namespace realm::bson;
//using namespace realm::jni_util;
//using namespace realm::_impl;
//
//static std::function<jobject(JNIEnv*, uint64_t)> collection_mapper_count = [](JNIEnv* env, uint64_t result) {
//    return JavaClassGlobalDef::new_long(env, result);
//};
//
//static std::function<jobject(JNIEnv*, util::Optional<ObjectId>)> collection_mapper_insert_one = [](JNIEnv* env, util::Optional<ObjectId> result) {
//    return JavaClassGlobalDef::new_object_id(env, result.value());
//};
//
//static void finalize_collection(jlong ptr) {
//    delete reinterpret_cast<RemoteMongoCollection*>(ptr);
//}
//
//JNIEXPORT jlong JNICALL
//Java_io_realm_internal_objectstore_OsRemoteMongoCollection_nativeGetFinalizerMethodPtr(JNIEnv*, jclass) {
//    return reinterpret_cast<jlong>(&finalize_collection);
//}
//
//void JNICALL
//Java_io_realm_internal_objectstore_OsRemoteMongoCollection_nativeCount(JNIEnv* env,
//                                                                       jclass,
//                                                                       jlong j_collection_ptr,
//                                                                       jstring j_filter,
//                                                                       jlong j_limit,
//                                                                       jobject j_callback) {
//    try {
//        RemoteMongoCollection* collection = reinterpret_cast<RemoteMongoCollection*>(j_collection_ptr);
//        uint64_t limit = std::uint64_t(j_limit);
//        JStringAccessor args_json(env, j_filter);
//        BsonDocument bson_filter(parse(args_json));
//        collection->count(bson_filter, limit, JavaNetworkTransport::create_result_callback(env, j_callback, collection_mapper_count));
//    }
//    CATCH_STD()
//}
//
//JNIEXPORT void JNICALL
//Java_io_realm_internal_objectstore_OsRemoteMongoCollection_nativeInsertOne(JNIEnv* env,
//                                                                           jclass,
//                                                                           jlong j_collection_ptr,
//                                                                           jstring j_document,
//                                                                           jobject j_callback) {
//    try {
//        RemoteMongoCollection* collection = reinterpret_cast<RemoteMongoCollection*>(j_collection_ptr);
//        JStringAccessor args_json(env, j_document);
//        BsonDocument bson_filter(parse(args_json));
//        collection->insert_one(bson_filter, JavaNetworkTransport::create_result_callback(env, j_callback, collection_mapper_insert_one));
//    }
//    CATCH_STD()
//}
