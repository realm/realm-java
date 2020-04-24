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

#include "io_realm_internal_objectstore_OsRemoteMongoDatabase.h"

#include "java_class_global_def.hpp"
#include "java_network_transport.hpp"
#include "util.hpp"
#include "jni_util/java_method.hpp"
#include "jni_util/jni_utils.hpp"

#include <realm/util/optional.hpp>
#include <sync/app.hpp>
#include <sync/sync_user.hpp>
#include <sync/remote_mongo_database.hpp>
#include <sync/remote_mongo_collection.hpp>

using namespace realm;
using namespace realm::app;
using namespace realm::jni_util;
using namespace realm::_impl;

static void finalize_collection(jlong ptr) {
    delete reinterpret_cast<std::shared_ptr<RemoteMongoCollection> *>(ptr);
}

//JNIEXPORT jlong JNICALL
//Java_io_realm_internal_objectstore_OsRemoteMongoClient_nativeCreate(JNIEnv *env,
//                                                                    jclass,
//                                                                    jlong j_app_ptr,
//                                                                    jstring j_service_name) {
//    try {
//        App *app = reinterpret_cast<App *>(j_app_ptr);
////        auto user = *reinterpret_cast<std::shared_ptr<SyncUser> *>(j_user_ptr);   // TODO
////        auto client = app->provider_client<App::UserAPIKeyProviderClient>();      // TODO
//        JStringAccessor name(env, j_service_name);
//        RemoteMongoClient client = app->remote_mongo_client(name);
//        return reinterpret_cast<jlong>(new RemoteMongoClient(std::move(client)));
//    }
//    CATCH_STD()
//    return reinterpret_cast<jlong>(nullptr);
//}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_objectstore_OsRemoteMongoDatabase_nativeGetCollection(JNIEnv *env,
                                                                    jclass,
                                                                    jlong j_database_ptr,
                                                                    jstring j_collection_name) {
    try {
        RemoteMongoDatabase *database = reinterpret_cast<RemoteMongoDatabase *>(j_database_ptr);
        JStringAccessor name(env, j_collection_name);

        RemoteMongoCollection collection = database->collection(name);
        return reinterpret_cast<jlong>(new RemoteMongoCollection(std::move(collection)));
    }
    CATCH_STD()
    return reinterpret_cast<jlong>(nullptr);
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_objectstore_OsRemoteMongoDatabase_nativeGetFinalizerMethodPtr(JNIEnv *, jclass) {
    return reinterpret_cast<jlong>(&finalize_collection);
}
