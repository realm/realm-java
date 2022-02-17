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

#include "io_realm_mongodb_EmailPasswordAuthImpl.h"

#include "java_network_transport.hpp"
#include "util.hpp"
#include "jni_util/java_method.hpp"
#include "jni_util/jni_utils.hpp"
#include "jni_util/bson_util.hpp"

#include <realm/object-store/sync/app.hpp>

using namespace realm;
using namespace realm::app;
using namespace realm::jni_util;
using namespace realm::_impl;

JNIEXPORT void JNICALL Java_io_realm_mongodb_EmailPasswordAuthImpl_nativeCallFunction(JNIEnv* env,
                                                                          jclass,
                                                                          jint j_function_type,
                                                                          jlong j_app_ptr,
                                                                          jobject j_callback,
                                                                          jobjectArray j_args)
{
    try {
        auto app = *reinterpret_cast<std::shared_ptr<App>*>(j_app_ptr);
        JObjectArrayAccessor<JStringAccessor, jstring> args(env, j_args);
        auto client = app->provider_client<App::UsernamePasswordProviderClient>();
        switch(j_function_type) {
            case io_realm_mongodb_EmailPasswordAuthImpl_TYPE_REGISTER_USER:
                client.register_email(args[0], args[1], JavaNetworkTransport::create_void_callback(env, j_callback));
                break;
            case io_realm_mongodb_EmailPasswordAuthImpl_TYPE_CONFIRM_USER:
                client.confirm_user(args[0], args[1], JavaNetworkTransport::create_void_callback(env, j_callback));
                break;
            case io_realm_mongodb_EmailPasswordAuthImpl_TYPE_RESEND_CONFIRMATION_EMAIL:
                client.resend_confirmation_email(args[0], JavaNetworkTransport::create_void_callback(env, j_callback));
                break;
            case io_realm_mongodb_EmailPasswordAuthImpl_TYPE_SEND_RESET_PASSWORD_EMAIL:
                client.send_reset_password_email(args[0], JavaNetworkTransport::create_void_callback(env, j_callback));
                break;
            case io_realm_mongodb_EmailPasswordAuthImpl_TYPE_CALL_RESET_PASSWORD_FUNCTION: {
                bson::BsonArray reset_arg(JniBsonProtocol::string_to_bson(args[2]));
                client.call_reset_password_function(args[0], args[1], reset_arg, JavaNetworkTransport::create_void_callback(env, j_callback));
                break;
            }
            case io_realm_mongodb_EmailPasswordAuthImpl_TYPE_RESET_PASSWORD:
                client.reset_password(args[0], args[1], args[2], JavaNetworkTransport::create_void_callback(env, j_callback));
                break;
            case io_realm_mongodb_EmailPasswordAuthImpl_TYPE_RETRY_CUSTOM_CONFIRMATION:
                client.retry_custom_confirmation(args[0], JavaNetworkTransport::create_void_callback(env, j_callback));
                break;
            default:
                throw std::logic_error(util::format("Unknown function: %1", j_function_type));
        }
    }
    CATCH_STD()
}

