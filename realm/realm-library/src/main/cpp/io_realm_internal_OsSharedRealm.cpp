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

#include "io_realm_internal_OsSharedRealm.h"
#if REALM_ENABLE_SYNC
#include "object-store/src/sync/sync_manager.hpp"
#include "object-store/src/sync/sync_config.hpp"
#include "object-store/src/sync/sync_session.hpp"
#include "object-store/src/results.hpp"
#include "object-store/src/sync/partial_sync.hpp"

#include "observable_collection_wrapper.hpp"
#endif

#include <realm/util/assert.hpp>

#include <shared_realm.hpp>

#include "java_accessor.hpp"
#include "java_binding_context.hpp"
#include "java_exception_def.hpp"
#include "object_store.hpp"
#include "util.hpp"
#include "jni_util/java_method.hpp"
#include "jni_util/java_class.hpp"
#include "jni_util/java_exception_thrower.hpp"


using namespace realm;
using namespace realm::_impl;
using namespace realm::jni_util;

static const char* c_table_name_exists_exception_msg = "Class already exists: '%1'.";

#if REALM_ENABLE_SYNC // used only for partial sync now
typedef ObservableCollectionWrapper<Results> ResultsWrapper;
#endif

JNIEXPORT void JNICALL Java_io_realm_internal_OsSharedRealm_nativeInit(JNIEnv* env, jclass,
                                                                     jstring temporary_directory_path)
{
    TR_ENTER()

    try {
        JStringAccessor path(env, temporary_directory_path);    // throws
        SharedGroupOptions::set_sys_tmp_dir(std::string(path)); // throws
    }
    CATCH_STD()
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_OsSharedRealm_nativeGetSharedRealm(JNIEnv* env, jclass, jlong config_ptr,
                                                                                jobject realm_notifier)
{
    TR_ENTER_PTR(config_ptr)

    auto& config = *reinterpret_cast<Realm::Config*>(config_ptr);
    try {
        auto shared_realm = Realm::get_shared_realm(config);
        // The migration callback & initialization callback could throw.
        if (env->ExceptionCheck()) {
            return reinterpret_cast<jlong>(nullptr);
        }
        shared_realm->m_binding_context = JavaBindingContext::create(env, realm_notifier);
        return reinterpret_cast<jlong>(new SharedRealm(std::move(shared_realm)));
    }
    catch (SchemaMismatchException& e) {
        // An exception has been thrown in the migration block.
        if (env->ExceptionCheck()) {
            return reinterpret_cast<jlong>(nullptr);
        }
        static JavaClass migration_needed_class(env, JavaExceptionDef::RealmMigrationNeeded);
        static JavaMethod constructor(env, migration_needed_class, "<init>",
                                      "(Ljava/lang/String;Ljava/lang/String;)V");

        jstring message = to_jstring(env, e.what());
        jstring path = to_jstring(env, config.path);
        jobject migration_needed_exception = env->NewObject(migration_needed_class, constructor, path, message);
        env->Throw(reinterpret_cast<jthrowable>(migration_needed_exception));
    }
    catch (InvalidSchemaVersionException& e) {
        // An exception has been thrown in the migration block.
        if (env->ExceptionCheck()) {
            return reinterpret_cast<jlong>(nullptr);
        }
        // To match the old behaviour. Otherwise it will be converted to ISE in the CATCH_STD.
        ThrowException(env, IllegalArgument, e.what());
    }
    CATCH_STD()
    return reinterpret_cast<jlong>(nullptr);
}

JNIEXPORT void JNICALL Java_io_realm_internal_OsSharedRealm_nativeCloseSharedRealm(JNIEnv*, jclass,
                                                                                 jlong shared_realm_ptr)
{
    TR_ENTER_PTR(shared_realm_ptr)

    auto& shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
    // Close the SharedRealm only. Let the finalizer daemon thread free the SharedRealm
    if (!shared_realm->is_closed()) {
        shared_realm->close();
    }
}

JNIEXPORT void JNICALL Java_io_realm_internal_OsSharedRealm_nativeBeginTransaction(JNIEnv* env, jclass,
                                                                                 jlong shared_realm_ptr)
{
    TR_ENTER_PTR(shared_realm_ptr)

    auto& shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
    try {
        shared_realm->begin_transaction();
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_OsSharedRealm_nativeCommitTransaction(JNIEnv* env, jclass,
                                                                                  jlong shared_realm_ptr)
{
    TR_ENTER_PTR(shared_realm_ptr)

    auto& shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
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

JNIEXPORT void JNICALL Java_io_realm_internal_OsSharedRealm_nativeCancelTransaction(JNIEnv* env, jclass,
                                                                                  jlong shared_realm_ptr)
{
    TR_ENTER_PTR(shared_realm_ptr)

    auto& shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
    try {
        shared_realm->cancel_transaction();
    }
    CATCH_STD()
}


JNIEXPORT jboolean JNICALL Java_io_realm_internal_OsSharedRealm_nativeIsInTransaction(JNIEnv*, jclass,
                                                                                    jlong shared_realm_ptr)
{
    TR_ENTER_PTR(shared_realm_ptr)

    auto& shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
    return static_cast<jboolean>(shared_realm->is_in_transaction());
}

JNIEXPORT jboolean JNICALL Java_io_realm_internal_OsSharedRealm_nativeIsEmpty(JNIEnv* env, jclass,
                                                                            jlong shared_realm_ptr)
{
    TR_ENTER_PTR(shared_realm_ptr)

    auto& shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
    try {
        return static_cast<jboolean>(ObjectStore::is_empty(shared_realm->read_group()));
    }
    CATCH_STD()
    return JNI_FALSE;
}

JNIEXPORT void JNICALL Java_io_realm_internal_OsSharedRealm_nativeRefresh(JNIEnv* env, jclass, jlong shared_realm_ptr)
{
    TR_ENTER_PTR(shared_realm_ptr)

    auto& shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
    try {
        shared_realm->refresh();
    }
    CATCH_STD()
}

JNIEXPORT jlongArray JNICALL Java_io_realm_internal_OsSharedRealm_nativeGetVersionID(JNIEnv* env, jclass,
                                                                                   jlong shared_realm_ptr)
{
    TR_ENTER_PTR(shared_realm_ptr)

    auto& shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
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

JNIEXPORT jboolean JNICALL Java_io_realm_internal_OsSharedRealm_nativeIsClosed(JNIEnv*, jclass, jlong shared_realm_ptr)
{
    TR_ENTER_PTR(shared_realm_ptr)

    auto& shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
    return static_cast<jboolean>(shared_realm->is_closed());
}


JNIEXPORT jlong JNICALL Java_io_realm_internal_OsSharedRealm_nativeGetTable(JNIEnv* env, jclass, jlong shared_realm_ptr,
                                                                          jstring table_name)
{
    TR_ENTER_PTR(shared_realm_ptr)

    try {
        JStringAccessor name(env, table_name); // throws
        auto& shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
        if (!shared_realm->read_group().has_table(name)) {
            std::string name_str = name;
            if (name_str.find(TABLE_PREFIX) == 0) {
                name_str = name_str.substr(TABLE_PREFIX.length());
            }
            THROW_JAVA_EXCEPTION(env, JavaExceptionDef::IllegalArgument,
                                 format("The class '%1' doesn't exist in this Realm.", name_str));
        }
        Table* table = LangBindHelper::get_table(shared_realm->read_group(), name);
        return reinterpret_cast<jlong>(table);
    }
    CATCH_STD()

    return reinterpret_cast<jlong>(nullptr);
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_OsSharedRealm_nativeCreateTable(JNIEnv* env, jclass,
                                                                             jlong shared_realm_ptr,
                                                                             jstring j_table_name)
{
    TR_ENTER_PTR(shared_realm_ptr)

    std::string table_name;
    try {
        table_name = JStringAccessor(env, j_table_name); // throws
        auto& shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
        shared_realm->verify_in_write(); // throws
        Table* table;
        auto& group = shared_realm->read_group();
#if REALM_ENABLE_SYNC
        // Sync doesn't throw when table exists.
        if (group.has_table(table_name)) {
            THROW_JAVA_EXCEPTION(env, JavaExceptionDef::IllegalArgument,
                                 format(c_table_name_exists_exception_msg, table_name.substr(TABLE_PREFIX.length())));
        }
        auto table_ref = sync::create_table(group, table_name); // throws
        table = LangBindHelper::get_table(group, table_ref->get_index_in_group());
#else
        table = LangBindHelper::add_table(group, table_name); // throws
#endif
        return reinterpret_cast<jlong>(table);
    }
    catch (TableNameInUse& e) {
        // We need to print the table name, so catch the exception here.
        std::string class_name_str(table_name.substr(TABLE_PREFIX.length()));
        ThrowException(env, IllegalArgument, format(c_table_name_exists_exception_msg, class_name_str));
    }
    CATCH_STD()

    return reinterpret_cast<jlong>(nullptr);
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_OsSharedRealm_nativeCreateTableWithPrimaryKeyField(
    JNIEnv* env, jclass, jlong shared_realm_ptr, jstring j_table_name, jstring j_field_name, jboolean is_string_type,
    jboolean is_nullable)
{
    TR_ENTER_PTR(shared_realm_ptr)

    std::string class_name_str;
    try {
        std::string table_name(JStringAccessor(env, j_table_name));
        class_name_str = std::string(table_name.substr(TABLE_PREFIX.length()));
        JStringAccessor field_name(env, j_field_name); // throws
        auto& shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
        shared_realm->verify_in_write(); // throws
        DataType pkType = is_string_type ? DataType::type_String : DataType::type_Int;
        Table* table;
        auto& group = shared_realm->read_group();
#if REALM_ENABLE_SYNC
        // Sync doesn't throw when table exists.
        if (group.has_table(table_name)) {
            THROW_JAVA_EXCEPTION(env, JavaExceptionDef::IllegalArgument,
                                 format(c_table_name_exists_exception_msg, class_name_str));
        }
        auto table_ref =
            sync::create_table_with_primary_key(group, table_name, pkType, field_name, is_nullable);
        table = LangBindHelper::get_table(group, table_ref->get_index_in_group());
#else
        table = LangBindHelper::add_table(group, table_name);
        size_t column_idx = table->add_column(pkType, field_name, is_nullable);
        table->add_search_index(column_idx);
#endif
        ObjectStore::set_primary_key_for_object(group, class_name_str, field_name);
        return reinterpret_cast<jlong>(table);
    }
    catch (TableNameInUse& e) {
        // We need to print the table name, so catch the exception here.
        ThrowException(env, IllegalArgument, format(c_table_name_exists_exception_msg, class_name_str));
    }
    CATCH_STD()

    return reinterpret_cast<jlong>(nullptr);
}

JNIEXPORT jstring JNICALL Java_io_realm_internal_OsSharedRealm_nativeGetTableName(JNIEnv* env, jclass,
                                                                                jlong shared_realm_ptr, jint index)
{

    TR_ENTER_PTR(shared_realm_ptr)

    auto& shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
    try {
        return to_jstring(env, shared_realm->read_group().get_table_name(static_cast<size_t>(index)));
    }
    CATCH_STD()
    return NULL;
}

JNIEXPORT jboolean JNICALL Java_io_realm_internal_OsSharedRealm_nativeHasTable(JNIEnv* env, jclass,
                                                                             jlong shared_realm_ptr,
                                                                             jstring table_name)
{
    TR_ENTER_PTR(shared_realm_ptr)

    auto& shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
    try {
        JStringAccessor name(env, table_name);
        return static_cast<jboolean>(shared_realm->read_group().has_table(name));
    }
    CATCH_STD()
    return JNI_FALSE;
}

JNIEXPORT void JNICALL Java_io_realm_internal_OsSharedRealm_nativeRenameTable(JNIEnv* env, jclass,
                                                                            jlong shared_realm_ptr,
                                                                            jstring old_table_name,
                                                                            jstring new_table_name)
{
    TR_ENTER_PTR(shared_realm_ptr)

    auto& shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
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

JNIEXPORT jlong JNICALL Java_io_realm_internal_OsSharedRealm_nativeSize(JNIEnv* env, jclass, jlong shared_realm_ptr)
{
    TR_ENTER_PTR(shared_realm_ptr)

    auto& shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
    try {
        return static_cast<jlong>(shared_realm->read_group().size());
    }
    CATCH_STD()

    return 0;
}

JNIEXPORT void JNICALL Java_io_realm_internal_OsSharedRealm_nativeWriteCopy(JNIEnv* env, jclass, jlong shared_realm_ptr,
                                                                          jstring path, jbyteArray key)
{
    TR_ENTER_PTR(shared_realm_ptr);

    auto& shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
    try {
        JStringAccessor path_str(env, path);
        JByteArrayAccessor jarray_accessor(env, key);
        shared_realm->write_copy(path_str, jarray_accessor.transform<BinaryData>());
    }
    CATCH_STD()
}

JNIEXPORT jboolean JNICALL Java_io_realm_internal_OsSharedRealm_nativeWaitForChange(JNIEnv* env, jclass,
                                                                                  jlong shared_realm_ptr)
{
    TR_ENTER_PTR(shared_realm_ptr);

    auto& shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
    try {
        using rf = realm::_impl::RealmFriend;
        return static_cast<jboolean>(rf::get_shared_group(*shared_realm).wait_for_change());
    }
    CATCH_STD()

    return JNI_FALSE;
}

JNIEXPORT void JNICALL Java_io_realm_internal_OsSharedRealm_nativeStopWaitForChange(JNIEnv* env, jclass,
                                                                                  jlong shared_realm_ptr)
{
    TR_ENTER_PTR(shared_realm_ptr);

    auto& shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
    try {
        using rf = realm::_impl::RealmFriend;
        rf::get_shared_group(*shared_realm).wait_for_change_release();
    }
    CATCH_STD()
}

JNIEXPORT jboolean JNICALL Java_io_realm_internal_OsSharedRealm_nativeCompact(JNIEnv* env, jclass,
                                                                            jlong shared_realm_ptr)
{
    TR_ENTER_PTR(shared_realm_ptr);

    auto& shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
    try {
        return static_cast<jboolean>(shared_realm->compact());
    }
    CATCH_STD()

    return JNI_FALSE;
}

static void finalize_shared_realm(jlong ptr)
{
    TR_ENTER_PTR(ptr)
    delete reinterpret_cast<SharedRealm*>(ptr);
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_OsSharedRealm_nativeGetFinalizerPtr(JNIEnv*, jclass)
{
    TR_ENTER()
    return reinterpret_cast<jlong>(&finalize_shared_realm);
}

JNIEXPORT void JNICALL Java_io_realm_internal_OsSharedRealm_nativeSetAutoRefresh(JNIEnv* env, jclass,
                                                                               jlong shared_realm_ptr,
                                                                               jboolean enabled)
{
    TR_ENTER_PTR(shared_realm_ptr)
    try {
        auto& shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
        shared_realm->set_auto_refresh(to_bool(enabled));
    }
    CATCH_STD()
}

JNIEXPORT jboolean JNICALL Java_io_realm_internal_OsSharedRealm_nativeIsAutoRefresh(JNIEnv* env, jclass,
                                                                                  jlong shared_realm_ptr)
{
    TR_ENTER_PTR(shared_realm_ptr)
    try {
        auto& shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
        return to_jbool(shared_realm->auto_refresh());
    }
    CATCH_STD()
    return JNI_FALSE;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_OsSharedRealm_nativeGetSchemaInfo(JNIEnv*, jclass,
                                                                               jlong shared_realm_ptr)
{
    TR_ENTER_PTR(shared_realm_ptr)

    // No throws
    auto& shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
    return reinterpret_cast<jlong>(&shared_realm->schema());
}

JNIEXPORT void JNICALL Java_io_realm_internal_OsSharedRealm_nativeRegisterSchemaChangedCallback(
    JNIEnv* env, jclass, jlong shared_realm_ptr, jobject j_schema_changed_callback)
{
    TR_ENTER_PTR(shared_realm_ptr)

    // No throws
    auto& shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
    JavaGlobalWeakRef callback_weak_ref(env, j_schema_changed_callback);
    if (shared_realm->m_binding_context) {
        JavaBindingContext& java_binding_context =
            *(static_cast<JavaBindingContext*>(shared_realm->m_binding_context.get()));
        java_binding_context.set_schema_changed_callback(env, j_schema_changed_callback);
    }
}

#if REALM_ENABLE_SYNC
JNIEXPORT jint JNICALL Java_io_realm_internal_OsSharedRealm_nativeGetRealmPrivileges(
    JNIEnv*, jclass, jlong shared_realm_ptr)
{
    TR_ENTER_PTR(shared_realm_ptr)
    auto& shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
    return static_cast<jint>(shared_realm->get_privileges());
}

JNIEXPORT jint JNICALL Java_io_realm_internal_OsSharedRealm_nativeGetClassPrivileges(
    JNIEnv* env, jclass, jlong shared_realm_ptr, jstring j_class_name)
{
    TR_ENTER_PTR(shared_realm_ptr)
    try {
        auto& shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
        JStringAccessor class_name(env, j_class_name);
        return static_cast<jint>(shared_realm->get_privileges(StringData(class_name)));
    }
    CATCH_STD()
    return 0;
}

JNIEXPORT jint JNICALL Java_io_realm_internal_OsSharedRealm_nativeGetObjectPrivileges(
    JNIEnv* env, jclass, jlong shared_realm_ptr, jlong row_ptr)
{
    TR_ENTER_PTR(shared_realm_ptr)
    try {
        auto& shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
        auto r = reinterpret_cast<Row*>(row_ptr);
        RowExpr row = r->get_table()->get(r->get_index());

        return static_cast<jint>(shared_realm->get_privileges(row));
    }
    CATCH_STD()
    return 0;
}
#endif

JNIEXPORT jboolean JNICALL Java_io_realm_internal_OsSharedRealm_nativeIsPartial(JNIEnv*, jclass, jlong shared_realm_ptr)
{
    TR_ENTER_PTR(shared_realm_ptr)
    // No throws
    auto& shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
    return to_jbool(shared_realm->is_partial());
}
