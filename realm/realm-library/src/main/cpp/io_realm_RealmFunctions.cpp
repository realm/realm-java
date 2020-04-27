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
//#include "object-store/src/sync/app_service_client.hpp"

using namespace realm;

static std::function<jobject(JNIEnv*, std::string )> mapper = [](JNIEnv* env, std::string response) {
    return to_jstring(env, response);
};

JNIEXPORT void JNICALL
Java_io_realm_RealmFunctions_nativeCallFunction(JNIEnv *env, jclass , jstring,
                                                jstring j_args_json , jobject j_callback) {
    try {
        // FIXME Mapper?
        std::function<void(std::string, Optional<app::AppError>)> callback;
        callback = JavaNetworkTransport::create_result_callback(env, j_callback, mapper);
        JStringAccessor args_json(env, j_args_json);

        // FIXME Does not look like AppServiceClient is ready yet
        //  auto client = app->provider_client<App::AppServiceClient>();
        //  client.call_function(j_name, j_args_json, Optional<std::string>(), callback)
        //  So for now just return args
        callback(args_json, {});
    }
    CATCH_STD()
}


