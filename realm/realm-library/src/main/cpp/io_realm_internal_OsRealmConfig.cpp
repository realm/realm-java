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

#include <realm/object-store/shared_realm.hpp>
#if REALM_ENABLE_SYNC
#include <realm/sync/config.hpp>
#include <realm/object-store/sync/app.hpp>
#include <realm/object-store/sync/sync_manager.hpp>
#include <realm/object-store/sync/sync_session.hpp>
#include <realm/util/misc_ext_errors.hpp>
#endif

#include <linux/errno.h>
#include <jni_util/bson_util.hpp>
#include <realm/sync/client.hpp>
#include <realm/object-store/thread_safe_reference.hpp>

#include "java_accessor.hpp"
#include "util.hpp"
#include "jni_util/java_method.hpp"
#include "jni_util/java_class.hpp"
#include "jni_util/java_global_weak_ref.hpp"
#include "jni_util/jni_utils.hpp"
#include "jni_util/java_exception_thrower.hpp"
#include <realm/util/basic_system_errors.hpp>

using namespace realm;
using namespace realm::jni_util;
using namespace realm::_impl;


static_assert(SchemaMode::Automatic ==
                  static_cast<SchemaMode>(io_realm_internal_OsRealmConfig_SCHEMA_MODE_VALUE_AUTOMATIC),
              "");
static_assert(SchemaMode::Immutable==
                  static_cast<SchemaMode>(io_realm_internal_OsRealmConfig_SCHEMA_MODE_VALUE_IMMUTABLE),
              "");
static_assert(SchemaMode::ReadOnly ==
                  static_cast<SchemaMode>(io_realm_internal_OsRealmConfig_SCHEMA_MODE_VALUE_READONLY),
              "");
static_assert(SchemaMode::SoftResetFile ==
                  static_cast<SchemaMode>(io_realm_internal_OsRealmConfig_SCHEMA_MODE_VALUE_SOFT_RESET_FILE),
              "");
static_assert(SchemaMode::AdditiveDiscovered ==
                  static_cast<SchemaMode>(io_realm_internal_OsRealmConfig_SCHEMA_MODE_VALUE_ADDITIVE_DISCOVERED),
              "");
static_assert(SchemaMode::Manual == static_cast<SchemaMode>(io_realm_internal_OsRealmConfig_SCHEMA_MODE_VALUE_MANUAL),
              "");

static void finalize_realm_config(jlong ptr)
{
    delete reinterpret_cast<Realm::Config*>(ptr);
}

static JavaClass& get_shared_realm_class(JNIEnv* env)
{
    static JavaClass shared_realm_class(env, "io/realm/internal/OsSharedRealm");
    return shared_realm_class;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_OsRealmConfig_nativeGetFinalizerPtr(JNIEnv*, jclass)
{
    return reinterpret_cast<jlong>(&finalize_realm_config);
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_OsRealmConfig_nativeCreate(JNIEnv* env, jclass, jstring j_realm_path,
                                                                          jstring j_fifo_fallback_dir,
                                                                          jboolean enable_format_upgrade,
                                                                          jlong j_max_number_of_active_versions)
{
    try {
        JStringAccessor realm_path(env, j_realm_path);
        JStringAccessor fifo_fallback_dir(env, j_fifo_fallback_dir);
        auto* config_ptr = new Realm::Config();
        config_ptr->path = realm_path;
        config_ptr->disable_format_upgrade = !enable_format_upgrade;
        config_ptr->fifo_files_fallback_path = fifo_fallback_dir;
        config_ptr->max_number_of_active_versions = j_max_number_of_active_versions;
        return reinterpret_cast<jlong>(config_ptr);
    }
    CATCH_STD()
    return reinterpret_cast<jlong>(nullptr);
}

JNIEXPORT void JNICALL Java_io_realm_internal_OsRealmConfig_nativeSetEncryptionKey(JNIEnv* env, jclass,
                                                                                   jlong native_ptr,
                                                                                   jbyteArray j_key_array)
{
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
    auto& config = *reinterpret_cast<Realm::Config*>(native_ptr);
    config.in_memory = in_mem; // no throw
}

JNIEXPORT void JNICALL Java_io_realm_internal_OsRealmConfig_nativeSetSchemaConfig(JNIEnv* env, jobject j_config,
                                                                                  jlong native_ptr, jbyte schema_mode,
                                                                                  jlong schema_version,
                                                                                  jlong schema_info_ptr,
                                                                                  jobject j_migration_callback)
{
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
                JavaGlobalRefByMove config_global = j_config_weak.global_ref(env);
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
                JavaGlobalRefByMove config_global_ref = j_config_weak.global_ref(env);
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
    // No throws
    auto& config = *reinterpret_cast<Realm::Config*>(native_ptr);
    config.automatic_change_notifications = enable_auto_change_notification;
}

#if REALM_ENABLE_SYNC
JNIEXPORT jstring JNICALL Java_io_realm_internal_OsRealmConfig_nativeCreateAndSetSyncConfig(
    JNIEnv* env, jobject j_config, jlong j_app_ptr, jlong j_config_ptr, jstring j_sync_realm_url, jstring j_user_id, jstring j_user_provider,
    jstring j_refresh_token, jstring j_access_token, jstring j_device_id, jbyte j_session_stop_policy, jstring j_url_prefix,
    jstring j_custom_auth_header_name, jobjectArray j_custom_headers_array, jbyte j_client_reset_mode,
    jobject j_on_before_client_reset_handler,
    jobject j_on_after_client_reset_handler,
    jstring j_partion_key_value, jobject j_java_sync_service)
{
    auto app = *reinterpret_cast<std::shared_ptr<app::App>*>(j_app_ptr);
    auto& config = *reinterpret_cast<Realm::Config*>(j_config_ptr);
    // sync_config should only be initialized once!
    REALM_ASSERT(!config.sync_config);

    try {
        static JavaClass sync_manager_class(env, "io/realm/mongodb/sync/Sync");
        // Doing the methods lookup from the thread that loaded the lib, to avoid
        // https://developer.android.com/training/articles/perf-jni.html#faq_FindClass
        static JavaMethod java_error_callback_method(env, sync_manager_class, "notifyErrorHandler",
                                                     "(BILjava/lang/String;Ljava/lang/String;Ljava/lang/String;)V");

        // error handler will be called form the sync client thread
        auto error_handler = [sync_service_object = JavaGlobalRefByCopy(env, j_java_sync_service)](std::shared_ptr<SyncSession> session, SyncError error) {

            auto std_error_code = error.to_status().get_std_error_code();
            int error_code = error.to_status().get_std_error_code().value();
            const std::error_category& error_category = std_error_code.category();

            jbyte category;
            if (error_category == realm::sync::client_error_category()) {
                category = io_realm_internal_ErrorCategory_RLM_SYNC_ERROR_CATEGORY_CLIENT;
            } else if (error_category == realm::sync::protocol_error_category()) {
                if (realm::sync::is_session_level_error(realm::sync::ProtocolError(std_error_code.value()))) {
                    category = io_realm_internal_ErrorCategory_RLM_SYNC_ERROR_CATEGORY_SESSION;
                }
                else {
                    category = io_realm_internal_ErrorCategory_RLM_SYNC_ERROR_CATEGORY_CONNECTION;
                }
            } else if (error_category == std::system_category() || error_category == realm::util::error::basic_system_error_category()) {
                category = io_realm_internal_ErrorCategory_RLM_SYNC_ERROR_CATEGORY_SYSTEM;
            } else {
                category = io_realm_internal_ErrorCategory_RLM_SYNC_ERROR_CATEGORY_UNKNOWN;
            }


            // System/Connection errors are defined by constants in
            // https://android.googlesource.com/kernel/lk/+/upstream-master/include/errno.h
            // However the integer values are not guaranteed to be stable according to POSIX.
            //
            // For this reason we manually map the constants to the error integer values defined in Java.
            // For simplicity Java re-use the values currently defined in errno.h.
            if (error_category == realm::util::error::basic_system_error_category()) {
                switch(error_code) {
                    case ECONNRESET: error_code = 104; break;
                    case ESHUTDOWN: error_code = 110; break;
                    case ECONNREFUSED: error_code = 111; break;
                    case EADDRINUSE: error_code = 112; break;
                    case ECONNABORTED: error_code = 113; break;
                    default:
                        /* Do nothing */
                        (void)0;
                }
            }

            auto error_message = error.what();

            std::string client_reset_path_info;

            // All client reset errors will be in the protocol category. Re-assign the error code
            // to a value not used by https://github.com/realm/realm-sync/blob/develop/src/realm/sync/protocol.hpp#L232
            // This way we only have one error in Java representing Client Reset.
            if (error.is_client_reset_requested()) {
                client_reset_path_info = error.user_info[SyncError::c_recovery_file_path_key];
                error_code = 7; // See ErrorCode.java
                category = io_realm_internal_ErrorCategory_RLM_APP_ERROR_CATEGORY_CUSTOM;
            }

            JNIEnv* env = realm::jni_util::JniUtils::get_env(true);
            jstring jerror_message = to_jstring(env, error_message);
            jstring jclient_reset_path_info = to_jstring(env, client_reset_path_info);
            jstring jsession_path = to_jstring(env, session->path());
            env->CallVoidMethod(sync_service_object.get(),
                                java_error_callback_method,
                                category,
                                error_code,
                                jerror_message,
                                jclient_reset_path_info,
                                jsession_path);
            env->DeleteLocalRef(jerror_message);
            env->DeleteLocalRef(jsession_path);
            if (env->ExceptionCheck()) {
                env->ExceptionDescribe();
                env->ExceptionClear();
                throw std::runtime_error("User-provided callback failed");
            }
        };

        // Get logged in user
        JStringAccessor user_id(env, j_user_id);
        JStringAccessor user_provider(env, j_user_provider);
        std::shared_ptr<SyncUser> user = app->sync_manager()->get_existing_logged_in_user(user_id);

        // FIXME: Investigate we shall be get the user https://github.com/realm/realm-java/issues/7591
        if (!user) {
            JStringAccessor refresh_token(env, j_refresh_token);
            JStringAccessor access_token(env, j_access_token);
            JStringAccessor device_id(env, j_device_id);
            user = app->sync_manager()->get_user(user_id, refresh_token, access_token, user_provider, device_id);
        }

        SyncSessionStopPolicy session_stop_policy = static_cast<SyncSessionStopPolicy>(j_session_stop_policy);

        JStringAccessor realm_url(env, j_sync_realm_url);
        // TODO Simplify. Java serialization only allows writing full documents, so the partition
        //  key is embedded in a document with key 'value'. To get is as string were we parse it
        //  and reformat with C++ bson serialization as it supports serializing single values.
        if (j_partion_key_value) {
            Bson bson(JniBsonProtocol::jstring_to_bson(env, j_partion_key_value));
            std::stringstream buffer;
            buffer << bson;
            config.sync_config = std::make_shared<SyncConfig>(SyncConfig{user, buffer.str()});
        } else {
            config.sync_config = std::make_shared<SyncConfig>(SyncConfig{user, SyncConfig::FLXSyncEnabled{}});
        }

        config.sync_config->stop_policy = session_stop_policy;
        config.sync_config->error_handler = std::move(error_handler);
        switch (j_client_reset_mode) {
            case io_realm_internal_OsRealmConfig_CLIENT_RESYNC_MODE_MANUAL: config.sync_config->client_resync_mode = realm::ClientResyncMode::Manual; break;
            case io_realm_internal_OsRealmConfig_CLIENT_RESYNC_MODE_DISCARD_LOCAL: config.sync_config->client_resync_mode = realm::ClientResyncMode::DiscardLocal; break;
            case io_realm_internal_OsRealmConfig_CLIENT_RESYNC_MODE_RECOVER: config.sync_config->client_resync_mode = realm::ClientResyncMode::Recover; break;
            case io_realm_internal_OsRealmConfig_CLIENT_RESYNC_MODE_RECOVER_OR_DISCARD: config.sync_config->client_resync_mode = realm::ClientResyncMode::RecoverOrDiscard; break;
            default: throw std::logic_error(util::format("Unsupported value for ClientResetMode: %1", j_client_reset_mode));
        }

        // setup before and after client reset callbacks
        if (j_client_reset_mode != io_realm_internal_OsRealmConfig_CLIENT_RESYNC_MODE_MANUAL) {
            static JavaClass before_client_reset_handler_class(env, "io/realm/internal/SyncObjectServerFacade$BeforeClientResetHandler");
            static JavaClass after_client_reset_handler_class(env, "io/realm/internal/SyncObjectServerFacade$AfterClientResetHandler");
            static JavaMethod on_before_client_reset_method(env, before_client_reset_handler_class, "onBeforeReset","(JLio/realm/internal/OsRealmConfig;)V", false);
            static JavaMethod on_after_client_reset_method(env, after_client_reset_handler_class, "onAfterReset","(JJLio/realm/internal/OsRealmConfig;Z)V", false);

            JavaGlobalWeakRef j_config_weak(env, j_config);
            JavaGlobalWeakRef j_on_before_client_reset_handler_weak(env, j_on_before_client_reset_handler);
            JavaGlobalWeakRef j_on_after_client_reset_handler_weak(env, j_on_after_client_reset_handler);
            config.sync_config->notify_before_client_reset = [j_on_before_client_reset_handler_weak, j_config_weak](SharedRealm before_frozen) {
                JNIEnv* env = JniUtils::get_env(false);
                JavaGlobalRefByMove config_global = j_config_weak.global_ref(env);
                if (!config_global) {
                    return;
                }

                // The local and remote Realm lifecycles are handled in Java via a
                // ManualReleaseNativeContext.
                auto* before_frozen_ptr = new SharedRealm(std::move(before_frozen));
                j_on_before_client_reset_handler_weak.call_with_local_ref(env, [&](JNIEnv* env, jobject obj) {
                    env->CallVoidMethod(obj, on_before_client_reset_method, reinterpret_cast<jlong>(before_frozen_ptr), config_global.get());
                });

                // Propagate the exception back to core that would handle the fallback to manual mode
                if (env->ExceptionCheck()) {
                    env->ExceptionDescribe();
                    env->ExceptionClear();
                    throw std::runtime_error("User-provided callback failed");
                }
            };

            config.sync_config->notify_after_client_reset = [j_on_after_client_reset_handler_weak, j_config_weak](SharedRealm before_frozen, ThreadSafeReference after, bool did_recover) {
                JNIEnv* env = JniUtils::get_env(false);
                JavaGlobalRefByMove config_global = j_config_weak.global_ref(env);
                if (!config_global) {
                    return;
                }

                // The local Realm lifecycle is handled in Java via a
                // ManualReleaseNativeContext.
                auto* before_frozen_ptr = new SharedRealm(std::move(before_frozen));

                // Resolve ThreadSafeReference
                auto* after_ptr = new SharedRealm(after.resolve<std::shared_ptr<Realm>>(nullptr));

                j_on_after_client_reset_handler_weak.call_with_local_ref(env, [&](JNIEnv* env, jobject obj) {
                    env->CallVoidMethod(obj, on_after_client_reset_method, reinterpret_cast<jlong>(before_frozen_ptr), reinterpret_cast<jlong>(after_ptr), config_global.get(), did_recover);
                });

                // Propagate the exception back to core that would handle the fallback to manual mode
                if (env->ExceptionCheck()) {
                    env->ExceptionDescribe();
                    env->ExceptionClear();
                    throw std::runtime_error("User-provided callback failed");
                }
            };
        }

        if (j_url_prefix) {
            JStringAccessor url_prefix(env, j_url_prefix);
            (void) url_prefix;
            // config.sync_config->url_prefix = realm::util::Optional<std::string>(url_prefix);
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

        // return to_jstring(env, config.sync_config->realm_url.c_str());
        // FIXME: We must return the realm url here for proxy support to work
        return to_jstring(env, "");
    }
    CATCH_STD()
    return nullptr;
}

JNIEXPORT void JNICALL Java_io_realm_internal_OsRealmConfig_nativeSetSyncConfigSslSettings(
    JNIEnv* env, jclass, jlong native_ptr, jboolean sync_client_validate_ssl,
    jstring j_sync_ssl_trust_certificate_path)
{
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
            static JavaClass sync_manager_class(env, "io/realm/mongodb/sync/Sync");
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

static_assert(SyncConfig::ProxyConfig::Type::HTTP == static_cast<SyncConfig::ProxyConfig::Type>(io_realm_internal_OsRealmConfig_PROXYCONFIG_TYPE_VALUE_HTTP),
              "");

JNIEXPORT void JNICALL Java_io_realm_internal_OsRealmConfig_nativeSetSyncConfigProxySettings(
    JNIEnv* env, jclass, jlong native_ptr, jbyte proxy_type,
    jstring j_proxy_address, jint proxy_port)
{
    auto& config = *reinterpret_cast<Realm::Config*>(native_ptr);
    // To ensure the sync_config has been created and this function won't be called multiple time on the same config.
    REALM_ASSERT(config.sync_config);
    REALM_ASSERT(!config.sync_config->proxy_config);

    try {
        SyncConfig::ProxyConfig proxy_config;
        proxy_config.type = static_cast<SyncConfig::ProxyConfig::Type>(proxy_type);
        proxy_config.address = JStringAccessor(env, j_proxy_address);
        proxy_config.port = proxy_port;

        config.sync_config->proxy_config.emplace(std::move(proxy_config));
    }
    CATCH_STD()
}
#endif
