/*
 * Copyright 2016 Realm Inc.
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

#include "io_realm_internal_SharedRealm.h"
#if REALM_ENABLE_SYNC
#include "object-store/src/sync/sync_manager.hpp"
#include "object-store/src/sync/sync_config.hpp"
#include "object-store/src/sync/sync_session.hpp"
#endif

#include <shared_realm.hpp>

#include "object_store.hpp"
#include "java_binding_context.hpp"
#include "util.hpp"

#include "jni_util/java_method.hpp"
#include "jni_util/java_class.hpp"

using namespace realm;
using namespace realm::_impl;
using namespace realm::jni_util;

static_assert(SchemaMode::Automatic ==
                  static_cast<SchemaMode>(io_realm_internal_SharedRealm_SCHEMA_MODE_VALUE_AUTOMATIC),
              "");
static_assert(SchemaMode::ReadOnly ==
                  static_cast<SchemaMode>(io_realm_internal_SharedRealm_SCHEMA_MODE_VALUE_READONLY),
              "");
static_assert(SchemaMode::ResetFile ==
                  static_cast<SchemaMode>(io_realm_internal_SharedRealm_SCHEMA_MODE_VALUE_RESET_FILE),
              "");
static_assert(SchemaMode::Additive ==
                  static_cast<SchemaMode>(io_realm_internal_SharedRealm_SCHEMA_MODE_VALUE_ADDITIVE),
              "");
static_assert(SchemaMode::Manual == static_cast<SchemaMode>(io_realm_internal_SharedRealm_SCHEMA_MODE_VALUE_MANUAL),
              "");

static void finalize_shared_realm(jlong ptr);

// Wrapper class for SyncConfig. This is required as we need to keep track of the Java session
// object as part of the configuration.
class JniConfigWrapper {

public:
    JniConfigWrapper(const JniConfigWrapper&) = delete;

    JniConfigWrapper& operator=(const JniConfigWrapper&) = delete;
    JniConfigWrapper(JniConfigWrapper&&) = delete;
    JniConfigWrapper& operator=(JniConfigWrapper&&) = delete;

    // Non-sync constructor
    JniConfigWrapper(JNIEnv*, Realm::Config& config)
        : m_config(std::move(config))
    {
    }

    // Sync constructor
    JniConfigWrapper(REALM_UNUSED JNIEnv* env, REALM_UNUSED Realm::Config& config,
                     REALM_UNUSED jstring sync_realm_url, REALM_UNUSED jstring sync_realm_auth_url,
                     REALM_UNUSED jstring sync_user_identity, REALM_UNUSED jstring sync_refresh_token)
        : m_config(std::move(config))
    {
#if REALM_ENABLE_SYNC
        static JavaClass sync_manager_class(env, "io/realm/SyncManager");
        // Doing the methods lookup from the thread that loaded the lib, to avoid
        // https://developer.android.com/training/articles/perf-jni.html#faq_FindClass
        static JavaMethod java_error_callback_method(env, sync_manager_class, "notifyErrorHandler",
                                                     "(ILjava/lang/String;Ljava/lang/String;)V", true);
        static JavaMethod java_bind_session_method(env, sync_manager_class, "bindSessionWithConfig",
                                                   "(Ljava/lang/String;)Ljava/lang/String;", true);

        // error handler will be called form the sync client thread
        auto error_handler = [=](std::shared_ptr<SyncSession> session, SyncError error) {
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
            env->CallStaticVoidMethod(sync_manager_class, java_error_callback_method, error_code,
                                      to_jstring(env, error_message), to_jstring(env, session.get()->path()));
        };

        // path on disk of the Realm file.
        // the sync configuration object.
        // the session which should be bound.
        auto bind_handler = [=](const std::string& path, const SyncConfig& syncConfig,
                                std::shared_ptr<SyncSession> session) {
            realm::jni_util::Log::d("Callback to Java requesting token for path");

            JNIEnv* env = realm::jni_util::JniUtils::get_env(true);

            jstring access_token_string = (jstring)env->CallStaticObjectMethod(
                sync_manager_class, java_bind_session_method, to_jstring(env, path.c_str()));
            if (access_token_string) {
                // reusing cached valid token
                JStringAccessor access_token(env, access_token_string);
                session->refresh_access_token(access_token, realm::util::Optional<std::string>(syncConfig.realm_url));
            }
        };

        // Get logged in user
        JStringAccessor user_identity(env, sync_user_identity);
        JStringAccessor realm_url(env, sync_realm_url);
        std::shared_ptr<SyncUser> user = SyncManager::shared().get_existing_logged_in_user(user_identity);
        if (!user) {
            JStringAccessor realm_auth_url(env, sync_realm_auth_url);
            JStringAccessor refresh_token(env, sync_refresh_token);
            user = SyncManager::shared().get_user(user_identity, refresh_token,
                                                  realm::util::Optional<std::string>(realm_auth_url));
        }
        m_config.sync_config = std::make_shared<SyncConfig>(SyncConfig{
            user, realm_url, SyncSessionStopPolicy::Immediately, std::move(bind_handler), std::move(error_handler)});
#else
        REALM_UNREACHABLE();
#endif
    }

    inline Realm::Config& get_config()
    {
        return m_config;
    }

    ~JniConfigWrapper()
    {
    }

private:
    Realm::Config m_config;
};

JNIEXPORT void JNICALL Java_io_realm_internal_SharedRealm_nativeInit(JNIEnv* env, jclass,
                                                                     jstring temporary_directory_path)
{
    TR_ENTER()

    try {
        JStringAccessor path(env, temporary_directory_path);    // throws
        SharedGroupOptions::set_sys_tmp_dir(std::string(path)); // throws
    }
    CATCH_STD()
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_SharedRealm_nativeCreateConfig(
    JNIEnv* env, jclass, jstring realm_path, jbyteArray key, jbyte schema_mode, jboolean in_memory, jboolean cache,
    jlong /* schema_version */, jboolean disable_format_upgrade, jboolean auto_change_notification,
    REALM_UNUSED jstring sync_server_url, REALM_UNUSED jstring sync_server_auth_url,
    REALM_UNUSED jstring sync_user_identity, REALM_UNUSED jstring sync_refresh_token)
{
    TR_ENTER()

    try {
        JStringAccessor path(env, realm_path); // throws
        JniByteArray key_array(env, key);
        Realm::Config config;
        config.path = path;
        // config->schema_version = schema_version; TODO: Disabled until we remove version handling from Java
        config.encryption_key = key_array;
        config.schema_mode = static_cast<SchemaMode>(schema_mode);
        config.in_memory = in_memory;
        config.cache = cache;
        config.disable_format_upgrade = disable_format_upgrade;
        config.automatic_change_notifications = auto_change_notification;
        if (sync_server_url) {
            return reinterpret_cast<jlong>(new JniConfigWrapper(env, config, sync_server_url, sync_server_auth_url,
                                                                sync_user_identity, sync_refresh_token));
        }
        else {
            return reinterpret_cast<jlong>(new JniConfigWrapper(env, config));
        }
    }
    CATCH_STD()

    return static_cast<jlong>(NULL);
}

JNIEXPORT void JNICALL Java_io_realm_internal_SharedRealm_nativeCloseConfig(JNIEnv*, jclass, jlong config_ptr)
{
    TR_ENTER_PTR(config_ptr)

    auto config = reinterpret_cast<JniConfigWrapper*>(config_ptr);
    delete config;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_SharedRealm_nativeGetSharedRealm(JNIEnv* env, jclass, jlong config_ptr,
                                                                                jobject realm_notifier)
{
    TR_ENTER_PTR(config_ptr)

    auto config = reinterpret_cast<JniConfigWrapper*>(config_ptr);
    try {
        auto shared_realm = Realm::get_shared_realm(config->get_config());
        shared_realm->m_binding_context = JavaBindingContext::create(env, realm_notifier);
        return reinterpret_cast<jlong>(new SharedRealm(std::move(shared_realm)));
    }
    CATCH_STD()
    return static_cast<jlong>(NULL);
}

JNIEXPORT void JNICALL Java_io_realm_internal_SharedRealm_nativeCloseSharedRealm(JNIEnv*, jclass,
                                                                                 jlong shared_realm_ptr)
{
    TR_ENTER_PTR(shared_realm_ptr)

    auto shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
    // Close the SharedRealm only. Let the finalizer daemon thread free the SharedRealm
    if (!shared_realm->is_closed()) {
        shared_realm->close();
    }
}

JNIEXPORT void JNICALL Java_io_realm_internal_SharedRealm_nativeBeginTransaction(JNIEnv* env, jclass,
                                                                                 jlong shared_realm_ptr)
{
    TR_ENTER_PTR(shared_realm_ptr)

    auto shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
    try {
        shared_realm->begin_transaction();
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_SharedRealm_nativeCommitTransaction(JNIEnv* env, jclass,
                                                                                  jlong shared_realm_ptr)
{
    TR_ENTER_PTR(shared_realm_ptr)

    auto shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
    try {
        shared_realm->commit_transaction();
        // Realm could be closed in the RealmNotifier.didChange().
        if (!shared_realm->is_closed()) {
            // To trigger async queries, so the UI can be refreshed immediately to avoid inconsistency.
            // See more discussion on https://github.com/realm/realm-java/issues/4245
            shared_realm->refresh();
        }
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_SharedRealm_nativeCancelTransaction(JNIEnv* env, jclass,
                                                                                  jlong shared_realm_ptr)
{
    TR_ENTER_PTR(shared_realm_ptr)

    auto shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
    try {
        shared_realm->cancel_transaction();
    }
    CATCH_STD()
}


JNIEXPORT jboolean JNICALL Java_io_realm_internal_SharedRealm_nativeIsInTransaction(JNIEnv*, jclass,
                                                                                    jlong shared_realm_ptr)
{
    TR_ENTER_PTR(shared_realm_ptr)

    auto shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
    return static_cast<jboolean>(shared_realm->is_in_transaction());
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_SharedRealm_nativeReadGroup(JNIEnv* env, jclass,
                                                                           jlong shared_realm_ptr)
{
    TR_ENTER_PTR(shared_realm_ptr)

    auto shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
    try {
        return reinterpret_cast<jlong>(&shared_realm->read_group());
    }
    CATCH_STD()

    return static_cast<jlong>(NULL);
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_SharedRealm_nativeGetVersion(JNIEnv* env, jclass,
                                                                            jlong shared_realm_ptr)
{
    TR_ENTER_PTR(shared_realm_ptr)

    auto shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
    try {
        return static_cast<jlong>(ObjectStore::get_schema_version(shared_realm->read_group()));
    }
    CATCH_STD()
    return static_cast<jlong>(ObjectStore::NotVersioned);
}

JNIEXPORT void JNICALL Java_io_realm_internal_SharedRealm_nativeSetVersion(JNIEnv* env, jclass,
                                                                           jlong shared_realm_ptr, jlong version)
{
    TR_ENTER_PTR(shared_realm_ptr)

    auto shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
    try {
        if (!shared_realm->is_in_transaction()) {
            std::ostringstream ss;
            ss << "Cannot set schema version when the realm is not in transaction.";
            ThrowException(env, IllegalState, ss.str());
            return;
        }

        ObjectStore::set_schema_version(shared_realm->read_group(), static_cast<uint64_t>(version));
    }
    CATCH_STD()
}

JNIEXPORT jboolean JNICALL Java_io_realm_internal_SharedRealm_nativeIsEmpty(JNIEnv* env, jclass,
                                                                            jlong shared_realm_ptr)
{
    TR_ENTER_PTR(shared_realm_ptr)

    auto shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
    try {
        return static_cast<jboolean>(ObjectStore::is_empty(shared_realm->read_group()));
    }
    CATCH_STD()
    return JNI_FALSE;
}

JNIEXPORT void JNICALL Java_io_realm_internal_SharedRealm_nativeRefresh(JNIEnv* env, jclass, jlong shared_realm_ptr)
{
    TR_ENTER_PTR(shared_realm_ptr)

    auto shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
    try {
        shared_realm->refresh();
    }
    CATCH_STD()
}

JNIEXPORT jlongArray JNICALL Java_io_realm_internal_SharedRealm_nativeGetVersionID(JNIEnv* env, jclass,
                                                                                   jlong shared_realm_ptr)
{
    TR_ENTER_PTR(shared_realm_ptr)

    auto shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
    try {
        using rf = realm::_impl::RealmFriend;
        SharedGroup::VersionID version_id = rf::get_shared_group(*shared_realm).get_version_of_current_transaction();

        jlong version_array[2];
        version_array[0] = static_cast<jlong>(version_id.version);
        version_array[1] = static_cast<jlong>(version_id.index);

        jlongArray version_data = env->NewLongArray(2);
        if (version_data == NULL) {
            ThrowException(env, OutOfMemory, "Could not allocate memory to return versionID.");
            return NULL;
        }
        env->SetLongArrayRegion(version_data, 0, 2, version_array);

        return version_data;
    }
    CATCH_STD()

    return NULL;
}

JNIEXPORT jboolean JNICALL Java_io_realm_internal_SharedRealm_nativeIsClosed(JNIEnv*, jclass, jlong shared_realm_ptr)
{
    TR_ENTER_PTR(shared_realm_ptr)

    auto shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
    return static_cast<jboolean>(shared_realm->is_closed());
}


JNIEXPORT jlong JNICALL Java_io_realm_internal_SharedRealm_nativeGetTable(JNIEnv* env, jclass, jlong shared_realm_ptr,
                                                                          jstring table_name)
{
    TR_ENTER_PTR(shared_realm_ptr)

    try {
        JStringAccessor name(env, table_name); // throws
        auto shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
        if (!shared_realm->read_group().has_table(name) && !shared_realm->is_in_transaction()) {
            std::ostringstream ss;
            ss << "Class " << name << " doesn't exist and the shared Realm is not in transaction.";
            ThrowException(env, IllegalState, ss.str());
            return static_cast<jlong>(NULL);
        }
        Table* pTable = LangBindHelper::get_or_add_table(shared_realm->read_group(), name);
        return reinterpret_cast<jlong>(pTable);
    }
    CATCH_STD()

    return static_cast<jlong>(NULL);
}

JNIEXPORT jstring JNICALL Java_io_realm_internal_SharedRealm_nativeGetTableName(JNIEnv* env, jclass,
                                                                                jlong shared_realm_ptr, jint index)
{

    TR_ENTER_PTR(shared_realm_ptr)

    auto shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
    try {
        return to_jstring(env, shared_realm->read_group().get_table_name(static_cast<size_t>(index)));
    }
    CATCH_STD()
    return NULL;
}

JNIEXPORT jboolean JNICALL Java_io_realm_internal_SharedRealm_nativeHasTable(JNIEnv* env, jclass,
                                                                             jlong shared_realm_ptr,
                                                                             jstring table_name)
{
    TR_ENTER_PTR(shared_realm_ptr)

    auto shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
    try {
        JStringAccessor name(env, table_name);
        return static_cast<jboolean>(shared_realm->read_group().has_table(name));
    }
    CATCH_STD()
    return JNI_FALSE;
}

JNIEXPORT void JNICALL Java_io_realm_internal_SharedRealm_nativeRenameTable(JNIEnv* env, jclass,
                                                                            jlong shared_realm_ptr,
                                                                            jstring old_table_name,
                                                                            jstring new_table_name)
{
    TR_ENTER_PTR(shared_realm_ptr)

    auto shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
    try {
        JStringAccessor old_name(env, old_table_name);
        if (!shared_realm->is_in_transaction()) {
            std::ostringstream ss;
            ss << "Class " << old_name << " cannot be removed when the realm is not in transaction.";
            ThrowException(env, IllegalState, ss.str());
            return;
        }
        JStringAccessor new_name(env, new_table_name);
        shared_realm->read_group().rename_table(old_name, new_name);
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_SharedRealm_nativeRemoveTable(JNIEnv* env, jclass,
                                                                            jlong shared_realm_ptr,
                                                                            jstring table_name)
{
    TR_ENTER_PTR(shared_realm_ptr)

    auto shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
    try {
        JStringAccessor name(env, table_name);
        if (!shared_realm->is_in_transaction()) {
            std::ostringstream ss;
            ss << "Class " << name << " cannot be removed when the realm is not in transaction.";
            ThrowException(env, IllegalState, ss.str());
            return;
        }
        shared_realm->read_group().remove_table(name);
    }
    CATCH_STD()
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_SharedRealm_nativeSize(JNIEnv* env, jclass, jlong shared_realm_ptr)
{
    TR_ENTER_PTR(shared_realm_ptr)

    auto shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
    try {
        return static_cast<jlong>(shared_realm->read_group().size());
    }
    CATCH_STD()

    return 0;
}

JNIEXPORT void JNICALL Java_io_realm_internal_SharedRealm_nativeWriteCopy(JNIEnv* env, jclass, jlong shared_realm_ptr,
                                                                          jstring path, jbyteArray key)
{
    TR_ENTER_PTR(shared_realm_ptr);

    auto shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
    try {
        JStringAccessor path_str(env, path);
        JniByteArray key_buffer(env, key);
        shared_realm->write_copy(path_str, key_buffer);
    }
    CATCH_STD()
}

JNIEXPORT jboolean JNICALL Java_io_realm_internal_SharedRealm_nativeWaitForChange(JNIEnv* env, jclass,
                                                                                  jlong shared_realm_ptr)
{
    TR_ENTER_PTR(shared_realm_ptr);

    auto shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
    try {
        using rf = realm::_impl::RealmFriend;
        return static_cast<jboolean>(rf::get_shared_group(*shared_realm).wait_for_change());
    }
    CATCH_STD()

    return JNI_FALSE;
}

JNIEXPORT void JNICALL Java_io_realm_internal_SharedRealm_nativeStopWaitForChange(JNIEnv* env, jclass,
                                                                                  jlong shared_realm_ptr)
{
    TR_ENTER_PTR(shared_realm_ptr);

    auto shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
    try {
        using rf = realm::_impl::RealmFriend;
        rf::get_shared_group(*shared_realm).wait_for_change_release();
    }
    CATCH_STD()
}

JNIEXPORT jboolean JNICALL Java_io_realm_internal_SharedRealm_nativeCompact(JNIEnv* env, jclass,
                                                                            jlong shared_realm_ptr)
{
    TR_ENTER_PTR(shared_realm_ptr);

    auto shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
    try {
        return static_cast<jboolean>(shared_realm->compact());
    }
    CATCH_STD()

    return JNI_FALSE;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_SharedRealm_nativeGetSnapshotVersion(JNIEnv* env, jclass,
                                                                                    jlong shared_realm_ptr)
{
    TR_ENTER_PTR(shared_realm_ptr)

    auto shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
    try {
        using rf = realm::_impl::RealmFriend;
        auto& shared_group = rf::get_shared_group(*shared_realm);
        return LangBindHelper::get_version_of_latest_snapshot(shared_group);
    }
    CATCH_STD()
    return 0;
}

JNIEXPORT void JNICALL Java_io_realm_internal_SharedRealm_nativeUpdateSchema(JNIEnv* env, jclass,
                                                                             jlong shared_realm_ptr, jlong schema_ptr,
                                                                             jlong version)
{
    TR_ENTER_PTR(shared_realm_ptr)
    try {
        auto shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
        auto* schema = reinterpret_cast<Schema*>(schema_ptr);
        shared_realm->update_schema(*schema, static_cast<uint64_t>(version), nullptr, true);
    }
    CATCH_STD()
}

JNIEXPORT jboolean JNICALL Java_io_realm_internal_SharedRealm_nativeRequiresMigration(JNIEnv* env, jclass,
                                                                                      jlong nativePtr,
                                                                                      jlong nativeSchemaPtr)
{

    TR_ENTER()
    try {
        auto shared_realm = *(reinterpret_cast<SharedRealm*>(nativePtr));
        auto* schema = reinterpret_cast<Schema*>(nativeSchemaPtr);
        const std::vector<SchemaChange>& change_list = shared_realm->schema().compare(*schema);
        return static_cast<jboolean>(!change_list.empty());
    }
    CATCH_STD()
    return JNI_FALSE;
}

static void finalize_shared_realm(jlong ptr)
{
    TR_ENTER_PTR(ptr)
    delete reinterpret_cast<SharedRealm*>(ptr);
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_SharedRealm_nativeGetFinalizerPtr(JNIEnv*, jclass)
{
    TR_ENTER()
    return reinterpret_cast<jlong>(&finalize_shared_realm);
}

JNIEXPORT void JNICALL Java_io_realm_internal_SharedRealm_nativeSetAutoRefresh(JNIEnv* env, jclass,
                                                                               jlong shared_realm_ptr,
                                                                               jboolean enabled)
{
    TR_ENTER_PTR(shared_realm_ptr)
    try {
        auto shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
        shared_realm->set_auto_refresh(to_bool(enabled));
    }
    CATCH_STD()
}

JNIEXPORT jboolean JNICALL Java_io_realm_internal_SharedRealm_nativeIsAutoRefresh(JNIEnv* env, jclass,
                                                                                  jlong shared_realm_ptr)
{
    TR_ENTER_PTR(shared_realm_ptr)
    try {
        auto shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
        return to_jbool(shared_realm->auto_refresh());
    }
    CATCH_STD()
    return JNI_FALSE;
}
