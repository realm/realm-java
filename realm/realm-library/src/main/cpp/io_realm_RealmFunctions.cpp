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

#include "io_realm_RealmFunctions.h"

#include "util.hpp"
#include "java_network_transport.hpp"
#include "object-store/src/sync/app.hpp"

using namespace realm;
using namespace realm::app;

static const std::string VALUE("value");

static bson::BsonArray arg_mapper(JNIEnv* env, jstring arg) {
    // FIXME Relies heavily on experimental convention; guard appropriately
    // FIXME How do we propagate errors from here
    JStringAccessor args_json(env, arg);
    bson::BsonDocument document(bson::parse(args_json));
    return static_cast<bson::BsonArray >(document[VALUE]);
}

static std::function<jobject(JNIEnv*, Optional<bson::Bson> )> response_mapper = [](JNIEnv* env, Optional<bson::Bson> response) {
    if (response) {
        // FIXME JNI type conversion
        bson::BsonDocument document  {{ VALUE, *response }};
        std::stringstream buffer;
        buffer << document;
        std::string r = buffer.str();
        return to_jstring(env, r);
    } else {
        // FIXME How to raise errors here
        return to_jstring(env, "{}");
    }
};

JNIEXPORT void JNICALL
Java_io_realm_RealmFunctions_nativeCallFunction(JNIEnv* env, jclass , jlong j_app_ptr, jlong j_user_ptr, jstring j_name,
                                                jstring j_args_json , jobject j_callback) {
    try {
        auto app = *reinterpret_cast<std::shared_ptr<App>*>(j_app_ptr);
        auto user = *reinterpret_cast<std::shared_ptr<SyncUser>*>(j_user_ptr);

        std::function<void(Optional<bson::Bson>, Optional<app::AppError>)> callback;
        callback = JavaNetworkTransport::create_result_callback(env, j_callback, response_mapper);

        // FIXME Seems like we need to swap the arguments!??! Maybe align conventions
        auto handler = [callback](Optional<app::AppError> error, Optional<bson::Bson> response) {
            callback(response, error);
        };

        JStringAccessor name(env, j_name);
        bson::BsonArray args = arg_mapper(env, j_args_json);

        app->call_function(user, name, args, handler);
    }
    CATCH_STD()
}


