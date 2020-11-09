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

#include "io_realm_internal_objectstore_OsMongoCollection.h"

#include "java_class_global_def.hpp"
#include "java_network_transport.hpp"
#include "util.hpp"
#include "jni_util/java_method.hpp"
#include "jni_util/jni_utils.hpp"
#include "jni_util/bson_util.hpp"
#include <realm/object-store/util/bson/bson.hpp>

#include <realm/util/optional.hpp>
#include <realm/object-store/sync/app.hpp>
#include <realm/object-store/sync/mongo_database.hpp>
#include <realm/object-store/sync/mongo_collection.hpp>
#include <jni_util/bson_util.hpp>

using namespace realm;
using namespace realm::app;
using namespace realm::bson;
using namespace realm::jni_util;
using namespace realm::_impl;

// This mapper works for both count and delete operations
static std::function<jobject(JNIEnv*, uint64_t)> collection_mapper_count = [](JNIEnv* env, uint64_t result) {
    return JavaClassGlobalDef::new_long(env, result);
};

// This mapper works for both findOne and findOneAndUpdate/Replace functions
static std::function<jobject(JNIEnv*, util::Optional<bson::BsonDocument>)> collection_mapper_find_one = [](JNIEnv* env, util::Optional<bson::BsonDocument> document) {
    return document ? JniBsonProtocol::bson_to_jstring(env, *document) : nullptr;
};

static std::function<jobject(JNIEnv*, util::Optional<Bson>)> collection_mapper_insert_one = [](JNIEnv* env, util::Optional<Bson> bson_id) {
    if (bson_id) {
        return JniBsonProtocol::bson_to_jstring(env, bson_id.value());
    }
    throw std::logic_error("Error in 'insert_one', parameter 'bson_id' has no value.");
};

static std::function<jobject(JNIEnv*, std::vector<Bson>)> collection_mapper_insert_many = [](JNIEnv* env, std::vector<Bson> bson_ids) {
    if (bson_ids.empty()) {
        throw std::logic_error("Error in 'insert_many', parameter 'object_ids' is empty.");
    }
    auto arr = (jobjectArray)env->NewObjectArray(static_cast<jsize>(bson_ids.size()), JavaClassGlobalDef::java_lang_object(), nullptr);
    if (arr == nullptr) {
        ThrowException(env, OutOfMemory, "Could not allocate memory to return list of ObjectIds of inserted documents.");
        return arr;
    }
    for (size_t i = 0; i < bson_ids.size(); ++i) {
        env->SetObjectArrayElement(arr, i, JniBsonProtocol::bson_to_jstring(env, bson_ids[i]));
    }
    return arr;
};

static std::function<jobject(JNIEnv*, MongoCollection::UpdateResult)> collection_mapper_update = [](JNIEnv* env, MongoCollection::UpdateResult result) {
    Bson matched_count(result.matched_count);
    Bson modified_count(result.modified_count);
    Bson upserted_value;
    if (result.upserted_id) {
        upserted_value = Bson(result.upserted_id.value());
    }
    std::vector<Bson> bson_vector = { matched_count, modified_count, upserted_value };
    Bson output(bson_vector);
    return JniBsonProtocol::bson_to_jstring(env, output);
};

static void finalize_collection(jlong ptr) {
    delete reinterpret_cast<MongoCollection*>(ptr);
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_objectstore_OsMongoCollection_nativeGetFinalizerMethodPtr(JNIEnv*, jclass) {
    return reinterpret_cast<jlong>(&finalize_collection);
}

JNIEXPORT void JNICALL
Java_io_realm_internal_objectstore_OsMongoCollection_nativeCount(JNIEnv* env,
                                                                 jclass,
                                                                 jlong j_collection_ptr,
                                                                 jstring j_filter,
                                                                 jlong j_limit,
                                                                 jobject j_callback) {
    try {
        auto collection = reinterpret_cast<MongoCollection*>(j_collection_ptr);

        bson::BsonDocument filter(JniBsonProtocol::parse_checked(env, j_filter, Bson::Type::Document, "BSON filter must be a Document"));
        uint64_t limit = std::uint64_t(j_limit);
        collection->count(filter, limit, JavaNetworkTransport::create_result_callback(env, j_callback, collection_mapper_count));
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL
Java_io_realm_internal_objectstore_OsMongoCollection_nativeFindOne(JNIEnv* env,
                                                                   jclass,
                                                                   jint j_find_one_type,
                                                                   jlong j_collection_ptr,
                                                                   jstring j_filter,
                                                                   jstring j_projection,
                                                                   jstring j_sort,
                                                                   jlong j_limit,
                                                                   jobject j_callback) {
    try {
        auto collection = reinterpret_cast<MongoCollection*>(j_collection_ptr);

        bson::BsonDocument filter(JniBsonProtocol::parse_checked(env, j_filter, Bson::Type::Document, "BSON filter must be a Document"));

        switch (j_find_one_type) {
            case io_realm_internal_objectstore_OsMongoCollection_FIND_ONE:
                collection->find_one(filter, JavaNetworkTransport::create_result_callback(env, j_callback, collection_mapper_find_one));
                break;
            case io_realm_internal_objectstore_OsMongoCollection_FIND_ONE_WITH_OPTIONS: {
                uint64_t limit = std::uint64_t(j_limit);

                bson::BsonDocument projection(JniBsonProtocol::parse_checked(env, j_projection, Bson::Type::Document, "BSON projection must be a Document"));
                bson::BsonDocument sort(JniBsonProtocol::parse_checked(env, j_sort, Bson::Type::Document, "BSON sort must be a Document"));
                MongoCollection::FindOptions options = {
                        limit,
                        projection,
                        sort
                };

                collection->find_one(filter, options, JavaNetworkTransport::create_result_callback(env, j_callback, collection_mapper_find_one));
                break;
            }
            default:
                throw std::logic_error(util::format("Unknown find_one type: %1", j_find_one_type));
        }
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL
Java_io_realm_internal_objectstore_OsMongoCollection_nativeInsertOne(JNIEnv* env,
                                                                     jclass,
                                                                     jlong j_collection_ptr,
                                                                     jstring j_document,
                                                                     jobject j_callback) {
    try {
        auto collection = reinterpret_cast<MongoCollection*>(j_collection_ptr);

        bson::BsonDocument filter(JniBsonProtocol::parse_checked(env, j_document, Bson::Type::Document, "BSON document must be a Document"));
        collection->insert_one(filter, JavaNetworkTransport::create_result_callback(env, j_callback, collection_mapper_insert_one));
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL
Java_io_realm_internal_objectstore_OsMongoCollection_nativeInsertMany(JNIEnv* env,
                                                                      jclass,
                                                                      jlong j_collection_ptr,
                                                                      jstring j_documents,
                                                                      jobject j_callback) {
    try {
        auto collection = reinterpret_cast<MongoCollection*>(j_collection_ptr);

        BsonArray bson_array(JniBsonProtocol::parse_checked(env, j_documents, Bson::Type::Array, "BSON documents must be a BsonArray"));
        collection->insert_many(bson_array, JavaNetworkTransport::create_result_callback(env, j_callback, collection_mapper_insert_many));
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL
Java_io_realm_internal_objectstore_OsMongoCollection_nativeDelete(JNIEnv* env,
                                                                  jclass,
                                                                  jint j_delete_type,
                                                                  jlong j_collection_ptr,
                                                                  jstring j_document,
                                                                  jobject j_callback) {
    try {
        auto collection = reinterpret_cast<MongoCollection*>(j_collection_ptr);
        bson::BsonDocument filter(JniBsonProtocol::parse_checked(env, j_document, Bson::Type::Document, "BSON document must be a Document"));

        switch (j_delete_type) {
            case io_realm_internal_objectstore_OsMongoCollection_DELETE_ONE:
                collection->delete_one(filter, JavaNetworkTransport::create_result_callback(env, j_callback, collection_mapper_count));
                break;
            case io_realm_internal_objectstore_OsMongoCollection_DELETE_MANY:
                collection->delete_many(filter, JavaNetworkTransport::create_result_callback(env, j_callback, collection_mapper_count));
                break;
            default:
                throw std::logic_error(util::format("Unknown delete type: %1", j_delete_type));
        }
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL
Java_io_realm_internal_objectstore_OsMongoCollection_nativeUpdate(JNIEnv *env,
                                                                  jclass,
                                                                  jint j_update_type,
                                                                  jlong j_collection_ptr,
                                                                  jstring j_filter,
                                                                  jstring j_update,
                                                                  jboolean j_upsert,
                                                                  jobject j_callback) {
    try {
        auto collection = reinterpret_cast<MongoCollection*>(j_collection_ptr);

        bson::BsonDocument filter(JniBsonProtocol::parse_checked(env, j_filter, Bson::Type::Document, "BSON filter must be a Document"));
        bson::BsonDocument update(JniBsonProtocol::parse_checked(env, j_update, Bson::Type::Document, "BSON update must be a Document"));

        switch (j_update_type) {
            case io_realm_internal_objectstore_OsMongoCollection_UPDATE_ONE:
                collection->update_one(filter, update, JavaNetworkTransport::create_result_callback(env, j_callback, collection_mapper_update));
                break;
            case io_realm_internal_objectstore_OsMongoCollection_UPDATE_ONE_WITH_OPTIONS:
                collection->update_one(filter, update, to_bool(j_upsert), JavaNetworkTransport::create_result_callback(env, j_callback, collection_mapper_update));
                break;
            case io_realm_internal_objectstore_OsMongoCollection_UPDATE_MANY:
                collection->update_many(filter, update, JavaNetworkTransport::create_result_callback(env, j_callback, collection_mapper_update));
                break;
            case io_realm_internal_objectstore_OsMongoCollection_UPDATE_MANY_WITH_OPTIONS:
                collection->update_many(filter, update, to_bool(j_upsert), JavaNetworkTransport::create_result_callback(env, j_callback, collection_mapper_update));
                break;
            default:
                throw std::logic_error(util::format("Unknown update type: %1", j_update_type));
        }
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL
Java_io_realm_internal_objectstore_OsMongoCollection_nativeFindOneAndUpdate(JNIEnv *env,
                                                                            jclass,
                                                                            jint j_find_one_and_update_type,
                                                                            jlong j_collection_ptr,
                                                                            jstring j_filter,
                                                                            jstring j_update,
                                                                            jstring j_projection,
                                                                            jstring j_sort,
                                                                            jboolean j_upsert,
                                                                            jboolean j_return_new_document,
                                                                            jobject j_callback) {
    try {
        auto collection = reinterpret_cast<MongoCollection*>(j_collection_ptr);

        bson::BsonDocument filter(JniBsonProtocol::parse_checked(env, j_filter, Bson::Type::Document, "BSON filter must be a Document"));
        bson::BsonDocument update(JniBsonProtocol::parse_checked(env, j_update, Bson::Type::Document, "BSON update must be a Document"));

        switch (j_find_one_and_update_type) {
            case io_realm_internal_objectstore_OsMongoCollection_FIND_ONE_AND_UPDATE:
                collection->find_one_and_update(filter, update, JavaNetworkTransport::create_result_callback(env, j_callback, collection_mapper_find_one));
                break;
            case io_realm_internal_objectstore_OsMongoCollection_FIND_ONE_AND_UPDATE_WITH_OPTIONS: {
                bson::BsonDocument projection(JniBsonProtocol::parse_checked(env, j_projection, Bson::Type::Document, "BSON projection must be a Document"));
                bson::BsonDocument sort(JniBsonProtocol::parse_checked(env, j_sort, Bson::Type::Document, "BSON sort must be a Document"));
                MongoCollection::FindOneAndModifyOptions options = {
                        projection,
                        sort,
                        to_bool(j_upsert),
                        to_bool(j_return_new_document)
                };
                collection->find_one_and_update(filter, update, options, JavaNetworkTransport::create_result_callback(env, j_callback, collection_mapper_find_one));
                break;
            }
            default:
                throw std::logic_error(util::format("Unknown find_one_and_update type: %1", j_find_one_and_update_type));
        }
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL
Java_io_realm_internal_objectstore_OsMongoCollection_nativeFindOneAndReplace(JNIEnv *env,
                                                                             jclass,
                                                                             jint j_find_one_and_replace_type,
                                                                             jlong j_collection_ptr,
                                                                             jstring j_filter,
                                                                             jstring j_update,
                                                                             jstring j_projection,
                                                                             jstring j_sort,
                                                                             jboolean j_upsert,
                                                                             jboolean j_return_new_document,
                                                                             jobject j_callback) {
    try {
        auto collection = reinterpret_cast<MongoCollection*>(j_collection_ptr);

        bson::BsonDocument filter(JniBsonProtocol::parse_checked(env, j_filter, Bson::Type::Document, "BSON filter must be a Document"));
        bson::BsonDocument update(JniBsonProtocol::parse_checked(env, j_update, Bson::Type::Document, "BSON update must be a Document"));

        switch (j_find_one_and_replace_type) {
            case io_realm_internal_objectstore_OsMongoCollection_FIND_ONE_AND_REPLACE:
                collection->find_one_and_replace(filter, update, JavaNetworkTransport::create_result_callback(env, j_callback, collection_mapper_find_one));
                break;
            case io_realm_internal_objectstore_OsMongoCollection_FIND_ONE_AND_REPLACE_WITH_OPTIONS: {
                bson::BsonDocument projection(JniBsonProtocol::parse_checked(env, j_projection, Bson::Type::Document, "BSON projection must be a Document"));
                bson::BsonDocument sort(JniBsonProtocol::parse_checked(env, j_sort, Bson::Type::Document, "BSON sort must be a Document"));
                MongoCollection::FindOneAndModifyOptions options = {
                        projection,
                        sort,
                        to_bool(j_upsert),
                        to_bool(j_return_new_document)
                };
                collection->find_one_and_replace(filter, update, options, JavaNetworkTransport::create_result_callback(env, j_callback, collection_mapper_find_one));
                break;
            }
            default:
                throw std::logic_error(util::format("Unknown find_one_and_replace type: %1", j_find_one_and_replace_type));
        }
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL
Java_io_realm_internal_objectstore_OsMongoCollection_nativeFindOneAndDelete(JNIEnv *env,
                                                                            jclass,
                                                                            jint j_find_one_and_delete_type,
                                                                            jlong j_collection_ptr,
                                                                            jstring j_filter,
                                                                            jstring j_projection,
                                                                            jstring j_sort,
                                                                            jboolean j_upsert,
                                                                            jboolean j_return_new_document,
                                                                            jobject j_callback) {
    try {
        auto collection = reinterpret_cast<MongoCollection*>(j_collection_ptr);

        bson::BsonDocument filter(JniBsonProtocol::parse_checked(env, j_filter, Bson::Type::Document, "BSON filter must be a Document"));

        switch (j_find_one_and_delete_type) {
            case io_realm_internal_objectstore_OsMongoCollection_FIND_ONE_AND_DELETE:
                collection->find_one_and_delete(filter, JavaNetworkTransport::create_result_callback(env, j_callback, collection_mapper_find_one));
                break;
            case io_realm_internal_objectstore_OsMongoCollection_FIND_ONE_AND_DELETE_WITH_OPTIONS: {
                bson::BsonDocument projection(JniBsonProtocol::parse_checked(env, j_projection, Bson::Type::Document, "BSON projection must be a Document"));
                bson::BsonDocument sort(JniBsonProtocol::parse_checked(env, j_sort, Bson::Type::Document, "BSON sort must be a Document"));
                MongoCollection::FindOneAndModifyOptions options = {
                        projection,
                        sort,
                        to_bool(j_upsert),
                        to_bool(j_return_new_document)
                };
                collection->find_one_and_delete(filter, options, JavaNetworkTransport::create_result_callback(env, j_callback, collection_mapper_find_one));
                break;
            }
            default:
                throw std::logic_error(util::format("Unknown find_one_and_delete type: %1", j_find_one_and_delete_type));
        }
    }
    CATCH_STD()
}
