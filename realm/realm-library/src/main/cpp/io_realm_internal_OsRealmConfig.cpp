/*
 * Copyright 2017 Realm Inc.
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

#include "io_realm_internal_OsRealmConfig.h"

#include <shared_realm.hpp>
#if REALM_ENABLE_SYNC
#include <sync/sync_config.hpp>
#include <sync/sync_manager.hpp>
#include <sync/sync_session.hpp>

#endif

#include "java_accessor.hpp"
#include "util.hpp"
#include "jni_util/java_method.hpp"
#include "jni_util/java_class.hpp"
#include "jni_util/java_global_weak_ref.hpp"
#include "jni_util/jni_utils.hpp"
#include "jni_util/java_exception_thrower.hpp"

using namespace realm;
using namespace realm::jni_util;
using namespace realm::_impl;

static_assert(SchemaMode::Automatic ==
                  static_cast<SchemaMode>(io_realm_internal_OsRealmConfig_SCHEMA_MODE_VALUE_AUTOMATIC),
              "");
static_assert(SchemaMode::Immutable==
                  static_cast<SchemaMode>(io_realm_internal_OsRealmConfig_SCHEMA_MODE_VALUE_IMMUTABLE),
              "");
static_assert(SchemaMode::ReadOnlyAlternative ==
                  static_cast<SchemaMode>(io_realm_internal_OsRealmConfig_SCHEMA_MODE_VALUE_READONLY),
              "");
static_assert(SchemaMode::ResetFile ==
                  static_cast<SchemaMode>(io_realm_internal_OsRealmConfig_SCHEMA_MODE_VALUE_RESET_FILE),
              "");
static_assert(SchemaMode::Additive ==
                  static_cast<SchemaMode>(io_realm_internal_OsRealmConfig_SCHEMA_MODE_VALUE_ADDITIVE),
              "");
static_assert(SchemaMode::Manual == static_cast<SchemaMode>(io_realm_internal_OsRealmConfig_SCHEMA_MODE_VALUE_MANUAL),
              "");

static void finalize_realm_config(jlong ptr)
{
    TR_ENTER_PTR(ptr)
    delete reinterpret_cast<Realm::Config*>(ptr);
}

static JavaClass& get_shared_realm_class(JNIEnv* env)
{
    static JavaClass shared_realm_class(env, "io/realm/internal/OsSharedRealm");
    return shared_realm_class;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_OsRealmConfig_nativeGetFinalizerPtr(JNIEnv*, jclass)
{
    TR_ENTER()
    return reinterpret_cast<jlong>(&finalize_realm_config);
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_OsRealmConfig_nativeCreate(JNIEnv* env, jclass, jstring j_realm_path,
                                                                          jboolean enable_cache,
                                                                          jboolean enable_format_upgrade)
{
    TR_ENTER()
    try {
        JStringAccessor realm_path(env, j_realm_path);
        auto* config_ptr = new Realm::Config();
        config_ptr->path = realm_path;
        config_ptr->cache = enable_cache;
        config_ptr->disable_format_upgrade = !enable_format_upgrade;
        return reinterpret_cast<jlong>(config_ptr);
    }
    CATCH_STD()
    return reinterpret_cast<jlong>(nullptr);
}

JNIEXPORT void JNICALL Java_io_realm_internal_OsRealmConfig_nativeSetEncryptionKey(JNIEnv* env, jclass,
                                                                                   jlong native_ptr,
                                                                                   jbyteArray j_key_array)
{
    TR_ENTER_PTR(native_ptr)
    try {
        JByteArrayAccessor jarray_accessor(env, j_key_array);
        auto& config = *reinterpret_cast<Realm::Config*>(native_ptr);
        // Encryption key should be set before creating sync_config.
        REALM_ASSERT(!config.sync_config);
        config.encryption_key = jarray_accessor.transform<std::vector<char>>();
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_OsRealmConfig_nativeSetInMemory(JNIEnv*, jclass, jlong native_ptr,
                                                                              jboolean in_mem)
{
    TR_ENTER_PTR(native_ptr)
    auto& config = *reinterpret_cast<Realm::Config*>(native_ptr);
    config.in_memory = in_mem; // no throw
}

JNIEXPORT void JNICALL Java_io_realm_internal_OsRealmConfig_nativeSetSchemaConfig(JNIEnv* env, jobject j_config,
                                                                                  jlong native_ptr, jbyte schema_mode,
                                                                                  jlong schema_version,
                                                                                  jlong schema_info_ptr,
                                                                                  jobject j_migration_callback)
{
    TR_ENTER_PTR(native_ptr)
    try {
        auto& config = *reinterpret_cast<Realm::Config*>(native_ptr);
        config.schema_mode = static_cast<SchemaMode>(schema_mode);
        config.schema_version = schema_version;
        if (schema_info_ptr) {
            auto& schema = *reinterpret_cast<Schema*>(schema_info_ptr);
            config.schema = schema;
        }
        else {
            config.schema = none;
        }

        if (j_migration_callback) {
            static JavaMethod run_migration_callback_method(
                env, get_shared_realm_class(env), "runMigrationCallback",
                "(JLio/realm/internal/OsRealmConfig;Lio/realm/internal/OsSharedRealm$MigrationCallback;J)V", true);
            // weak ref to avoid leaks caused by circular refs.
            JavaGlobalWeakRef j_config_weak(env, j_config);
            JavaGlobalWeakRef j_migration_cb_weak(env, j_migration_callback);
            // TODO: It would be great if we can use move constructor in the lambda capture which was introduced in
            // c++14. But sadly it seems to be a bug with gcc 4.9 to support it.
            config.migration_function = [j_migration_cb_weak, j_config_weak](SharedRealm old_realm,
                                                                                 SharedRealm realm, Schema&) {
                JNIEnv* env = JniUtils::get_env(false);
                // Java needs a new pointer for the OsSharedRealm life control.
                SharedRealm* new_shared_realm_ptr = new SharedRealm(realm);
                JavaGlobalRef config_global = j_config_weak.global_ref(env);
                if (!config_global) {
                    return;
                }

                j_migration_cb_weak.call_with_local_ref(env, [&](JNIEnv* env, jobject obj) {
                    env->CallStaticVoidMethod(get_shared_realm_class(env), run_migration_callback_method,
                                              reinterpret_cast<jlong>(new_shared_realm_ptr), config_global.get(), obj,
                                              old_realm->schema_version());
                });
                TERMINATE_JNI_IF_JAVA_EXCEPTION_OCCURRED(env, nullptr);
            };
        }
        else {
            config.migration_function = nullptr;
        }
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_OsRealmConfig_nativeSetCompactOnLaunchCallback(
    JNIEnv* env, jclass, jlong native_ptr, jobject j_compact_on_launch)
{
    TR_ENTER_PTR(native_ptr)

    try {
        auto& config = *reinterpret_cast<Realm::Config*>(native_ptr);
        if (j_compact_on_launch) {
            static JavaClass compact_on_launch_class(env, "io/realm/CompactOnLaunchCallback");
            static JavaMethod should_compact(env, compact_on_launch_class, "shouldCompact", "(JJ)Z");
            // weak ref to avoid leaks caused by circular refs.
            JavaGlobalWeakRef java_compact_on_launch_weak(env, j_compact_on_launch);

            config.should_compact_on_launch_function = [java_compact_on_launch_weak](uint64_t totalBytes,
                                                                                    uint64_t usedBytes) {
                JNIEnv* env = JniUtils::get_env(false);
                bool result = false;
                java_compact_on_launch_weak.call_with_local_ref(env, [&](JNIEnv* env, jobject obj) {
                    result = env->CallBooleanMethod(obj, should_compact, static_cast<jlong>(totalBytes),
                                                    static_cast<jlong>(usedBytes));
                });
                TERMINATE_JNI_IF_JAVA_EXCEPTION_OCCURRED(env, nullptr);
                return result;
            };
        }
        else {
            config.should_compact_on_launch_function = nullptr;
        }
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_OsRealmConfig_nativeSetInitializationCallback(JNIEnv* env,
                                                                                            jobject j_config,
                                                                                            jlong native_ptr,
                                                                                            jobject j_init_callback)
{
    TR_ENTER_PTR(native_ptr)

    try {
        auto& config = *reinterpret_cast<Realm::Config*>(native_ptr);

        if (j_init_callback) {
            static JavaMethod run_initialization_callback_method(
                env, get_shared_realm_class(env), "runInitializationCallback",
                "(JLio/realm/internal/OsRealmConfig;Lio/realm/internal/OsSharedRealm$InitializationCallback;)V", true);
            // weak ref to avoid leaks caused by circular refs.
            JavaGlobalWeakRef j_init_cb_weak(env, j_init_callback);
            JavaGlobalWeakRef j_config_weak(env, j_config);
            config.initialization_function = [j_init_cb_weak, j_config_weak](SharedRealm realm) {
                JNIEnv* env = JniUtils::get_env(false);
                // Java needs a new pointer for the OsSharedRealm life control.
                SharedRealm* new_shared_realm_ptr = new SharedRealm(realm);
                JavaGlobalRef config_global_ref = j_config_weak.global_ref(env);
                if (!config_global_ref) {
                    return;
                }
                j_init_cb_weak.call_with_local_ref(env, [&](JNIEnv* env, jobject obj) {
                    env->CallStaticVoidMethod(get_shared_realm_class(env), run_initialization_callback_method,
                                              reinterpret_cast<jlong>(new_shared_realm_ptr), config_global_ref.get(),
                                              obj);
                });
                TERMINATE_JNI_IF_JAVA_EXCEPTION_OCCURRED(env, nullptr);
            };
        }
        else {
            config.initialization_function = nullptr;
        }
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_OsRealmConfig_nativeEnableChangeNotification(
    JNIEnv*, jclass, jlong native_ptr, jboolean enable_auto_change_notification)
{
    TR_ENTER_PTR(native_ptr)

    // No throws
    auto& config = *reinterpret_cast<Realm::Config*>(native_ptr);
    config.automatic_change_notifications = enable_auto_change_notification;
}

#if REALM_ENABLE_SYNC
JNIEXPORT jstring JNICALL Java_io_realm_internal_OsRealmConfig_nativeCreateAndSetSyncConfig(
    JNIEnv* env, jclass, jlong native_ptr, jstring j_sync_realm_url, jstring j_auth_url, jstring j_user_id,
    jstring j_refresh_token, jboolean j_is_partial, jbyte j_session_stop_policy, jstring j_url_prefix,
    jstring j_custom_auth_header_name, jobjectArray j_custom_headers_array)
{
    TR_ENTER_PTR(native_ptr)
    auto& config = *reinterpret_cast<Realm::Config*>(native_ptr);
    // sync_config should only be initialized once!
    REALM_ASSERT(!config.sync_config);

    try {
        static JavaClass sync_manager_class(env, "io/realm/SyncManager");
        // Doing the methods lookup from the thread that loaded the lib, to avoid
        // https://developer.android.com/training/articles/perf-jni.html#faq_FindClass
        static JavaMethod java_error_callback_method(env, sync_manager_class, "notifyErrorHandler",
                                                     "(ILjava/lang/String;Ljava/lang/String;)V", true);
        static JavaMethod java_bind_session_method(env, sync_manager_class, "bindSessionWithConfig",
                                                   "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;", true);

        // error handler will be called form the sync client thread
        auto error_handler = [](std::shared_ptr<SyncSession> session, SyncError error) {
            realm::jni_util::Log::d("error_handler lambda invoked");

            auto error_message = error.message;
            auto error_code = error.error_code.value();
            if (error.is_client_reset_requested()) {
                // Hack the error message to send information about the location of the backup.
                // If more uses of the user_info map surfaces. Refactor this to send the full
                // map instead.
                error_message = error.user_info[SyncError::c_recovery_file_path_key];
                error_code = 7; // See ErrorCode.java
            }

            JNIEnv* env = realm::jni_util::JniUtils::get_env(true);
            jstring jerror_message = to_jstring(env, error_message);
            jstring jsession_path = to_jstring(env, session.get()->path());
            env->CallStaticVoidMethod(sync_manager_class, java_error_callback_method, error_code, jerror_message,
                                      jsession_path);
            env->DeleteLocalRef(jerror_message);
            env->DeleteLocalRef(jsession_path);
        };

        // path on disk of the Realm file.
        // the sync configuration object.
        // the session which should be bound.
        auto bind_handler = [](const std::string& path, const SyncConfig& syncConfig,
                               std::shared_ptr<SyncSession> session) {
            realm::jni_util::Log::d("Callback to Java requesting token for path");

            JNIEnv* env = realm::jni_util::JniUtils::get_env(true);

            jstring jpath = to_jstring(env, path.c_str());
            jstring jrefresh_token = to_jstring(env, session->user()->refresh_token().c_str());
            jstring access_token_string = (jstring)env->CallStaticObjectMethod(
                sync_manager_class, java_bind_session_method, jpath, jrefresh_token);
            if (access_token_string) {
                // reusing cached valid token
                JStringAccessor access_token(env, access_token_string);
                session->refresh_access_token(access_token, realm::util::Optional<std::string>(syncConfig.realm_url()));
                env->DeleteLocalRef(access_token_string);
            }
            env->DeleteLocalRef(jpath);
            env->DeleteLocalRef(jrefresh_token);
        };

        // Get logged in user
        JStringAccessor user_id(env, j_user_id);
        JStringAccessor auth_url(env, j_auth_url);
        SyncUserIdentifier sync_user_identifier = {user_id, auth_url};
        std::shared_ptr<SyncUser> user = SyncManager::shared().get_existing_logged_in_user(sync_user_identifier);
        if (!user) {
            JStringAccessor realm_auth_url(env, j_auth_url);
            JStringAccessor refresh_token(env, j_refresh_token);
            user = SyncManager::shared().get_user(sync_user_identifier, refresh_token);
        }

        SyncSessionStopPolicy session_stop_policy = static_cast<SyncSessionStopPolicy>(j_session_stop_policy);

        JStringAccessor realm_url(env, j_sync_realm_url);
        config.sync_config = std::make_shared<SyncConfig>(SyncConfig{user, realm_url});
        config.sync_config->stop_policy = session_stop_policy;
        config.sync_config->bind_session_handler = std::move(bind_handler);
        config.sync_config->error_handler = std::move(error_handler);
        config.sync_config->is_partial = (j_is_partial == JNI_TRUE);

        if (j_url_prefix) {
            JStringAccessor url_prefix(env, j_url_prefix);
            config.sync_config->url_prefix = realm::util::Optional<std::string>(url_prefix);
        }

        if (j_custom_auth_header_name) {
            JStringAccessor custom_auth_header_name(env, j_custom_auth_header_name);
            config.sync_config->authorization_header_name = realm::util::Optional<std::string>(custom_auth_header_name);
        }

        if (j_custom_headers_array) {
            jsize count = env->GetArrayLength(j_custom_headers_array);
            for (int i = 0; i < count; i = i + 2) {
                JStringAccessor key(env, (jstring) env->GetObjectArrayElement(j_custom_headers_array, i));
                JStringAccessor value(env, (jstring) env->GetObjectArrayElement(j_custom_headers_array, i + 1));
                config.sync_config->custom_http_headers[std::string(key)] = std::string(value);
            }
        }

        if (!config.encryption_key.empty()) {
            config.sync_config->realm_encryption_key = std::array<char, 64>();
            std::copy_n(config.encryption_key.begin(), 64, config.sync_config->realm_encryption_key->begin());
        }

        return to_jstring(env, config.sync_config->realm_url().c_str());
    }
    CATCH_STD()
    return nullptr;
}

JNIEXPORT void JNICALL Java_io_realm_internal_OsRealmConfig_nativeSetSyncConfigSslSettings(
    JNIEnv* env, jclass, jlong native_ptr, jboolean sync_client_validate_ssl,
    jstring j_sync_ssl_trust_certificate_path)
{
    TR_ENTER_PTR(native_ptr);

    auto& config = *reinterpret_cast<Realm::Config*>(native_ptr);
    // To ensure the sync_config has been created and this function won't be called multiple time on the same config.
    REALM_ASSERT(config.sync_config);
    REALM_ASSERT(config.sync_config->client_validate_ssl);
    REALM_ASSERT(!config.sync_config->ssl_trust_certificate_path);

    try {
        config.sync_config->client_validate_ssl = sync_client_validate_ssl;
        if (j_sync_ssl_trust_certificate_path) {
            JStringAccessor cert_path(env, j_sync_ssl_trust_certificate_path);
            config.sync_config->ssl_trust_certificate_path = realm::util::Optional<std::string>(cert_path);
        }
        else if (config.sync_config->client_validate_ssl) {
            // set default callback to allow Android to check the certificate
            static JavaClass sync_manager_class(env, "io/realm/SyncManager");
            static JavaMethod java_ssl_verify_callback(env, sync_manager_class, "sslVerifyCallback",
                                                       "(Ljava/lang/String;Ljava/lang/String;I)Z", true);

            std::function<sync::Session::SSLVerifyCallback> ssl_verify_callback =
                [](const std::string server_address, REALM_UNUSED realm::sync::Session::port_type server_port,
                   const char* pem_data, size_t pem_size, REALM_UNUSED int preverify_ok, int depth) {

                    Log::d("Callback to Java requesting certificate validation for host %1",
                                            server_address.c_str());

                    JNIEnv* env = realm::jni_util::JniUtils::get_env(true);

                    jstring jserver_address = to_jstring(env, server_address.c_str());
                    // deep copy the pem_data into a string so DeleteLocalRef delete the local reference not the original const char
                    std::string pem(pem_data, pem_size);
                    jstring jpem = to_jstring(env, pem.c_str());
                    bool isValid = env->CallStaticBooleanMethod(sync_manager_class, java_ssl_verify_callback,
                                                                jserver_address,
                                                                jpem, depth) == JNI_TRUE;
                    env->DeleteLocalRef(jserver_address);
                    env->DeleteLocalRef(jpem);
                    return isValid;
                };
            config.sync_config->ssl_verify_callback = std::move(ssl_verify_callback);
        }
    }
    CATCH_STD()
}

#endif
