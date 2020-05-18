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

// FIXME
#include <android/log.h>

#include "io_realm_FunctionsImpl.h"

#include "util.hpp"
#include "jni_util/bson_util.hpp"
#include "java_network_transport.hpp"
#include "object-store/src/sync/app.hpp"


using namespace realm;
using namespace realm::app;
using namespace realm::bson;
using namespace realm::jni_util;

static std::function<jobject(JNIEnv*, Optional<Bson> )> success_mapper = [](JNIEnv* env, Optional<Bson> response) {
    if (response) {
        // FIXME Debug output to debug CI-only issue
        std::string result(JniBsonProtocol::bson_to_string(*response));
        __android_log_print(ANDROID_LOG_DEBUG, "REALM", "call response: %s", result.c_str());
        return JniBsonProtocol::bson_to_jstring(env, *response);
    } else {
        // We should never reach here, as this is the success mapper and we would not end up here
        // if we did not received a parsable BSON response
        throw std::logic_error("Function did not return a result");
    }
};

JNIEXPORT void JNICALL
Java_io_realm_FunctionsImpl_nativeCallFunction(JNIEnv* env, jclass , jlong j_app_ptr, jlong j_user_ptr, jstring j_name,
                                               jstring j_args_json , jobject j_callback) {
    try {
        auto app = *reinterpret_cast<std::shared_ptr<App>*>(j_app_ptr);
        auto user = *reinterpret_cast<std::shared_ptr<SyncUser>*>(j_user_ptr);

        std::function<void(Optional<Bson>, Optional<app::AppError>)> callback = JavaNetworkTransport::create_result_callback(env, j_callback, success_mapper);

        auto handler = [callback](Optional<app::AppError> error, Optional<Bson> response) {
            callback(response, error);
        };

        JStringAccessor name(env, j_name);
        BsonArray args(JniBsonProtocol::parse_checked(env, j_args_json, Bson::Type::Array, "BSON argument must be an BsonArray"));
        std::string result(JniBsonProtocol::bson_to_string(args));
        __android_log_print(ANDROID_LOG_DEBUG, "REALM", "call args: %s", result.c_str());
        app->call_function(user, name, args, handler);
    }
    CATCH_STD()
}
