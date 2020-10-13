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

#include "io_realm_mongodb_ApiKeyAuthImpl.h"

#include "java_class_global_def.hpp"
#include "java_network_transport.hpp"
#include "util.hpp"
#include "jni_util/java_method.hpp"
#include "jni_util/jni_utils.hpp"
#include "object-store/src/sync/app.hpp"

#include <realm/util/optional.hpp>
#include <sync/app.hpp>

using namespace realm;
using namespace realm::app;
using namespace realm::jni_util;
using namespace realm::_impl;

static jobjectArray map_key(JNIEnv* env, App::UserAPIKey& key)
{
    jobjectArray arr = (jobjectArray)env->NewObjectArray(4, JavaClassGlobalDef::java_lang_object(), NULL);
    if (arr == NULL) {
        ThrowException(env, OutOfMemory, "Could not allocate memory to return API key.");
        return NULL;
    }
    std::string api_key_id = key.id.to_string();
    env->SetObjectArrayElement(arr, 0, to_jstring(env, api_key_id));
    env->SetObjectArrayElement(arr, 1, (key.key) ? to_jstring(env, key.key) : NULL);
    env->SetObjectArrayElement(arr, 2, to_jstring(env, key.name));
    env->SetObjectArrayElement(arr, 3, JavaClassGlobalDef::new_boolean(env, key.disabled));
    return arr;
}

// Shared mapper function for mapping UserApiKey to Java Object[]
static std::function<jobject(JNIEnv*, App::UserAPIKey)> single_key_mapper = [](JNIEnv* env, App::UserAPIKey key) {
    return map_key(env, key);
};

// Shared mapper function for mapping Vector<UserApiKey> to Java Object[][]
static std::function<jobject(JNIEnv*, std::vector<App::UserAPIKey>)> multi_key_mapper = [](JNIEnv* env, std::vector<App::UserAPIKey> keys) {
    jobjectArray arr = (jobjectArray)env->NewObjectArray(static_cast<jsize>(keys.size()), JavaClassGlobalDef::java_lang_object(), NULL);
    if (arr == NULL) {
        ThrowException(env, OutOfMemory, "Could not allocate memory to return list of API keys.");
        return arr;
    }
    for (size_t i = 0; i < keys.size(); ++i) {
        env->SetObjectArrayElement(arr, i, map_key(env, keys[i]));
    }
    return arr;
};

JNIEXPORT void JNICALL Java_io_realm_mongodb_ApiKeyAuthImpl_nativeCallFunction(JNIEnv* env,
                                                                   jclass,
                                                                   jint j_function_type,
                                                                   jlong j_app_ptr,
                                                                   jlong j_user_ptr,
                                                                   jstring j_arg,
                                                                   jobject j_callback)
{
    try {
        auto app = *reinterpret_cast<std::shared_ptr<App>*>(j_app_ptr);
        auto user = *reinterpret_cast<std::shared_ptr<SyncUser>*>(j_user_ptr);
        auto client = app->provider_client<App::UserAPIKeyProviderClient>();
        switch(j_function_type) {
            case io_realm_mongodb_ApiKeyAuthImpl_TYPE_CREATE: {
                JStringAccessor name(env, j_arg);
                auto callback = JavaNetworkTransport::create_result_callback(env, j_callback, single_key_mapper);
                client.create_api_key(name, user, callback);
                break;
            }
            case io_realm_mongodb_ApiKeyAuthImpl_TYPE_FETCH_SINGLE: {
                auto callback = JavaNetworkTransport::create_result_callback(env, j_callback, single_key_mapper);
                std::string str_id = JStringAccessor(env, static_cast<jstring>(j_arg));
                client.fetch_api_key(ObjectId(str_id.c_str()), user, callback);
                break;
            }
            case io_realm_mongodb_ApiKeyAuthImpl_TYPE_FETCH_ALL: {
                auto callback = JavaNetworkTransport::create_result_callback(env, j_callback, multi_key_mapper);
                client.fetch_api_keys(user, callback);
                break;
            }
            case io_realm_mongodb_ApiKeyAuthImpl_TYPE_DELETE: {
                auto callback = JavaNetworkTransport::create_void_callback(env, j_callback);
                std::string str_id = JStringAccessor(env, static_cast<jstring>(j_arg));
                client.delete_api_key(ObjectId(str_id.c_str()), user, callback);
                break;
            }
            case io_realm_mongodb_ApiKeyAuthImpl_TYPE_ENABLE: {
                auto callback = JavaNetworkTransport::create_void_callback(env, j_callback);
                std::string str_id = JStringAccessor(env, static_cast<jstring>(j_arg));
                client.enable_api_key(ObjectId(str_id.c_str()), user, callback);
                break;
            }
            case io_realm_mongodb_ApiKeyAuthImpl_TYPE_DISABLE: {
                auto callback = JavaNetworkTransport::create_void_callback(env, j_callback);
                std::string str_id = JStringAccessor(env, static_cast<jstring>(j_arg));
                client.disable_api_key(ObjectId(str_id.c_str()), user, callback);
                break;
            }
            default:
                throw std::logic_error(util::format("Unknown function: %1", j_function_type));
        }
    }
    CATCH_STD()
}
