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

#include "io_realm_internal_objectstore_OsMongoClient.h"

#include "java_class_global_def.hpp"
#include "java_network_transport.hpp"
#include "util.hpp"
#include "jni_util/java_method.hpp"
#include "jni_util/jni_utils.hpp"

#include <realm/util/optional.hpp>
#include <sync/app.hpp>
#include <sync/sync_user.hpp>
#include <sync/remote_mongo_client.hpp>
#include <sync/remote_mongo_database.hpp>

using namespace realm;
using namespace realm::app;
using namespace realm::jni_util;
using namespace realm::_impl;

static void finalize_client(jlong ptr) {
    delete reinterpret_cast<RemoteMongoClient*>(ptr);
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_objectstore_OsMongoClient_nativeGetFinalizerMethodPtr(JNIEnv*, jclass) {
    return reinterpret_cast<jlong>(&finalize_client);
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_objectstore_OsMongoClient_nativeCreate(JNIEnv* env,
                                                              jclass,
                                                              jlong j_app_ptr,
                                                              jstring j_service_name) {
    try {
        std::shared_ptr<App> &app = *reinterpret_cast<std::shared_ptr<App> *>(j_app_ptr);
        JStringAccessor name(env, j_service_name);
        RemoteMongoClient client(app->remote_mongo_client(name));
        return reinterpret_cast<jlong>(new RemoteMongoClient(std::move(client)));
    }
    CATCH_STD()
    return reinterpret_cast<jlong>(nullptr);
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_objectstore_OsMongoClient_nativeCreateDatabase(JNIEnv* env,
                                                                      jclass,
                                                                      jlong j_client_ptr,
                                                                      jstring j_database_name) {
    try {
        RemoteMongoClient* client = reinterpret_cast<RemoteMongoClient*>(j_client_ptr);
        JStringAccessor name(env, j_database_name);
        RemoteMongoDatabase database(client->db(name));
        return reinterpret_cast<jlong>(new RemoteMongoDatabase(std::move(database)));
    }
    CATCH_STD()
    return reinterpret_cast<jlong>(nullptr);
}
