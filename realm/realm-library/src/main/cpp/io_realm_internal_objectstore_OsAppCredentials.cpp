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

#include "io_realm_internal_objectstore_OsAppCredentials.h"

#include "util.hpp"

#include <jni_util/bson_util.hpp>
#include <sync/app_credentials.hpp>

using namespace realm;
using namespace realm::app;
using namespace realm::bson;
using namespace realm::jni_util;

static void finalize_credentials(jlong ptr)
{
    delete reinterpret_cast<AppCredentials*>(ptr);
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_objectstore_OsAppCredentials_nativeGetFinalizerMethodPtr(JNIEnv*, jclass)
{
    return reinterpret_cast<jlong>(&finalize_credentials);
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_objectstore_OsAppCredentials_nativeCreate(JNIEnv* env,
                                                                                         jclass,
                                                                                         jint j_type,
                                                                                         jobjectArray j_args)
{
    try {
        AppCredentials creds = AppCredentials::anonymous(); // Is there a way to avoid setting this to a specific value?
        switch(j_type) {
            case io_realm_internal_objectstore_OsAppCredentials_TYPE_ANONYMOUS:
                /* Default, do nothing */;
                break;
            case io_realm_internal_objectstore_OsAppCredentials_TYPE_FACEBOOK: {
                JStringAccessor access_token(env, (jstring) env->GetObjectArrayElement(j_args, 0));
                creds = AppCredentials::facebook(access_token);
                break;
            }
            case io_realm_internal_objectstore_OsAppCredentials_TYPE_EMAIL_PASSWORD: {
                JStringAccessor email(env, (jstring) env->GetObjectArrayElement(j_args, 0));
                JStringAccessor password(env, (jstring) env->GetObjectArrayElement(j_args, 1));
                creds = AppCredentials::username_password(email, password);
                break;
            }
            case io_realm_internal_objectstore_OsAppCredentials_TYPE_APPLE: {
                JStringAccessor id_token(env, (jstring) env->GetObjectArrayElement(j_args, 0));
                creds = AppCredentials::apple(id_token);
                break;
            }
            case io_realm_internal_objectstore_OsAppCredentials_TYPE_GOOGLE: {
                JStringAccessor id_token(env, (jstring) env->GetObjectArrayElement(j_args, 0));
                creds = AppCredentials::google(id_token);
                break;
            }
            case io_realm_internal_objectstore_OsAppCredentials_TYPE_JWT: {
                JStringAccessor token(env, (jstring) env->GetObjectArrayElement(j_args, 0));
                creds = AppCredentials::custom(token);
                break;
            }
            case io_realm_internal_objectstore_OsAppCredentials_TYPE_API_KEY: {
                JStringAccessor token(env, (jstring) env->GetObjectArrayElement(j_args, 0));
                creds = AppCredentials::user_api_key(token);
                break;
            }
            case io_realm_internal_objectstore_OsAppCredentials_TYPE_SERVER_API_KEY: {
                JStringAccessor token(env, (jstring) env->GetObjectArrayElement(j_args, 0));
                creds = AppCredentials::server_api_key(token);
                break;
            }
            case io_realm_internal_objectstore_OsAppCredentials_TYPE_CUSTOM_FUNCTION: {
                jstring j_payload = (jstring) env->GetObjectArrayElement(j_args, 0);
                bson::BsonDocument payload(JniBsonProtocol::parse_checked(env, j_payload, Bson::Type::Document, "Payload must be a Document"));
                creds = AppCredentials::function(payload);
                break;
            }
            default:
                throw std::runtime_error(util::format("Unknown credentials type: %1", j_type));
        }
        return reinterpret_cast<jlong>(new AppCredentials(std::move(creds)));
    }
    CATCH_STD()
    return 0;
}

JNIEXPORT jstring JNICALL Java_io_realm_internal_objectstore_OsAppCredentials_nativeGetProvider(JNIEnv* env, jclass, jlong j_native_ptr)
{
    try {
        auto credentials = reinterpret_cast<AppCredentials*>(j_native_ptr);
        std::string provider = credentials->provider_as_string();
        return to_jstring(env, provider);
    }
    CATCH_STD()
    return nullptr;
}

JNIEXPORT jstring JNICALL Java_io_realm_internal_objectstore_OsAppCredentials_nativeAsJson(JNIEnv* env, jclass, jlong j_native_ptr)
{
    try {
        auto credentials = reinterpret_cast<AppCredentials*>(j_native_ptr);
        std::string json = credentials->serialize_as_json();
        return to_jstring(env, json);
    }
    CATCH_STD()
    return 0;
}
