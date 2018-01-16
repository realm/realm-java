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

#include "io_realm_internal_OsObject.h"

#include <realm/row.hpp>
#if REALM_ENABLE_SYNC
#include <realm/sync/object.hpp>
#endif
#include <realm/util/to_string.hpp>

#include <object_schema.hpp>
#include <object.hpp>
#include <shared_realm.hpp>

#include "util.hpp"
#include "java_class_global_def.hpp"

#include "jni_util/java_global_weak_ref.hpp"
#include "jni_util/java_method.hpp"
#include "jni_util/java_class.hpp"
#include "jni_util/java_exception_thrower.hpp"

using namespace realm;
using namespace realm::jni_util;
using namespace realm::_impl;

static const char* PK_CONSTRAINT_EXCEPTION_CLASS = "io/realm/exceptions/RealmPrimaryKeyConstraintException";
static const char* PK_EXCEPTION_MSG_FORMAT = "Primary key value already exists: %1 .";

// We need to control the life cycle of Object, weak ref of Java OsObject and the NotificationToken.
// Wrap all three together, so when the Java object gets GCed, all three of them will be invalidated.
struct ObjectWrapper {
    JavaGlobalWeakRef m_row_object_weak_ref;
    NotificationToken m_notification_token;
    realm::Object m_object;

    ObjectWrapper(realm::Object& object)
        : m_row_object_weak_ref()
        , m_notification_token()
        , m_object(std::move(object))
    {
    }

    ObjectWrapper(ObjectWrapper&&) = delete;
    ObjectWrapper& operator=(ObjectWrapper&&) = delete;

    ObjectWrapper(ObjectWrapper const&) = delete;
    ObjectWrapper& operator=(ObjectWrapper const&) = delete;

    ~ObjectWrapper()
    {
    }
};

struct ChangeCallback {
    ChangeCallback(ObjectWrapper* wrapper, JavaMethod notify_change_listeners)
        : m_wrapper(wrapper),
        m_notify_change_listeners_method(notify_change_listeners)
    {
    }

    void parse_fields(JNIEnv* env, CollectionChangeSet const& change_set)
    {
        if (m_field_names_array) {
            return;
        }

        if (!change_set.deletions.empty()) {
            m_deleted = true;
            return;
        }

        // The local ref of jstring needs to be released to avoid reach the local ref table size limitation.
        std::vector<JavaGlobalRef> field_names;
        auto table = m_wrapper->m_object.row().get_table();
        for (size_t i = 0; i < change_set.columns.size(); ++i) {
            if (change_set.columns[i].empty()) {
                continue;
            }
            // FIXME: After full integration of the OS schema, parse the column name from
            // wrapper->m_object.get_object_schema() will be faster.
            field_names.push_back(JavaGlobalRef(env, to_jstring(env, table->get_column_name(i)), true));
        }
        m_field_names_array = env->NewObjectArray(field_names.size(), JavaClassGlobalDef::java_lang_string(), 0);
        for (size_t i = 0; i < field_names.size(); ++i) {
            env->SetObjectArrayElement(m_field_names_array, i, field_names[i].get());
        }
    }

    JNIEnv* check_env()
    {
        JNIEnv* env = JniUtils::get_env(false);
        if (!env || env->ExceptionCheck()) {
            // JVM detached or java exception has been thrown before.
            return nullptr;
        }
        return env;
    }

    void before(CollectionChangeSet const& change_set)
    {
        JNIEnv* env = check_env();
        if (!env) {
            return;
        }

        parse_fields(env, change_set);
    }

    void after(CollectionChangeSet const& change_set)
    {
        JNIEnv* env = check_env();
        if (!env) {
            return;
        }
        if (change_set.empty()) {
            return;
        }

        parse_fields(env, change_set);
        m_wrapper->m_row_object_weak_ref.call_with_local_ref(env, [&](JNIEnv*, jobject row_obj) {
            env->CallVoidMethod(row_obj, m_notify_change_listeners_method, m_deleted ? nullptr : m_field_names_array);
        });
        m_field_names_array = nullptr;
        m_deleted = false;
    }

    void error(std::exception_ptr err)
    {
        if (err) {
            try {
                std::rethrow_exception(err);
            }
            catch (const std::exception& e) {
                Log::e("Caught exception in object change callback %1", e.what());
            }
        }
    }

private:
    ObjectWrapper* m_wrapper;
    bool m_deleted = false;
    jobjectArray m_field_names_array = nullptr;
    JavaMethod m_notify_change_listeners_method;
};

static void finalize_object(jlong ptr)
{
    TR_ENTER_PTR(ptr);
    delete reinterpret_cast<ObjectWrapper*>(ptr);
}

static inline size_t do_create_row(jlong shared_realm_ptr, jlong table_ptr)
{
    auto& shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
    auto& table = *(reinterpret_cast<realm::Table*>(table_ptr));
    shared_realm->verify_in_write();
#if REALM_ENABLE_SYNC
    return sync::create_object(shared_realm->read_group(), table);
#else
    return table.add_empty_row();
#endif
}

static inline size_t do_create_row_with_primary_key(JNIEnv* env, jlong shared_realm_ptr, jlong table_ptr,
                                                    jlong pk_column_ndx, jlong pk_value, jboolean is_pk_null)
{
    auto& shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
    auto& table = *(reinterpret_cast<realm::Table*>(table_ptr));
    shared_realm->verify_in_write(); // throws
    if (is_pk_null && !TBL_AND_COL_NULLABLE(env, &table, pk_column_ndx)) {
        return realm::npos;
    }

    if (is_pk_null) {
        if (table.find_first_null(pk_column_ndx) != npos) {
            THROW_JAVA_EXCEPTION(env, PK_CONSTRAINT_EXCEPTION_CLASS, format(PK_EXCEPTION_MSG_FORMAT, "'null'"));
        }
    }
    else {
        if (table.find_first_int(pk_column_ndx, pk_value) != npos) {
            THROW_JAVA_EXCEPTION(env, PK_CONSTRAINT_EXCEPTION_CLASS,
                                 format(PK_EXCEPTION_MSG_FORMAT, reinterpret_cast<long long>(pk_value)));
        }
    }

    size_t row_ndx;
#if REALM_ENABLE_SYNC
    if (is_pk_null) {
        row_ndx = sync::create_object_with_primary_key(shared_realm->read_group(), table, util::none);
    }
    else {
        row_ndx = sync::create_object_with_primary_key(shared_realm->read_group(), table,
                                                       util::Optional<int64_t>(pk_value));
    }
#else
    row_ndx = table.add_empty_row();

    if (is_pk_null) {
        table.set_null_unique(pk_column_ndx, row_ndx);
    }
    else {
        table.set_int_unique(pk_column_ndx, row_ndx, pk_value);
    }
#endif
    return row_ndx;
}

static inline size_t do_create_row_with_primary_key(JNIEnv* env, jlong shared_realm_ptr, jlong table_ptr,
                                                    jlong pk_column_ndx, jstring pk_value)
{
    auto& shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
    auto& table = *(reinterpret_cast<realm::Table*>(table_ptr));
    shared_realm->verify_in_write(); // throws
    JStringAccessor str_accessor(env, pk_value); // throws
    if (!pk_value && !TBL_AND_COL_NULLABLE(env, &table, pk_column_ndx)) {
        return realm::npos;
    }

    if (pk_value) {
        if (table.find_first_string(pk_column_ndx, str_accessor) != npos) {
            THROW_JAVA_EXCEPTION(env, PK_CONSTRAINT_EXCEPTION_CLASS,
                                 format(PK_EXCEPTION_MSG_FORMAT, str_accessor.operator std::string()));
        }
    }
    else {
        if (table.find_first_null(pk_column_ndx) != npos) {
            THROW_JAVA_EXCEPTION(env, PK_CONSTRAINT_EXCEPTION_CLASS, format(PK_EXCEPTION_MSG_FORMAT, "'null'"));
        }
    }

    size_t row_ndx;
#if REALM_ENABLE_SYNC
    row_ndx = sync::create_object_with_primary_key(shared_realm->read_group(), table, str_accessor);
#else
    row_ndx = table.add_empty_row();
    if (pk_value) {
        table.set_string_unique(pk_column_ndx, row_ndx, str_accessor);
    }
    else {
        table.set_string_unique(pk_column_ndx, row_ndx, null{});
    }
#endif

    return row_ndx;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_OsObject_nativeGetFinalizerPtr(JNIEnv*, jclass)
{
    TR_ENTER()
    return reinterpret_cast<jlong>(&finalize_object);
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_OsObject_nativeCreate(JNIEnv*, jclass, jlong shared_realm_ptr,
                                                                     jlong row_ptr)
{
    TR_ENTER_PTR(row_ptr)

    // FIXME: Currently OsObject is only used for object notifications. Since the Object Store's schema has not been
    // fully integrated with realm-java, we pass a dummy ObjectSchema to create Object.
    static const ObjectSchema dummy_object_schema;

    auto& shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
    auto& row = *(reinterpret_cast<Row*>(row_ptr));
    Object object(shared_realm, dummy_object_schema, row); // no throw
    auto wrapper = new ObjectWrapper(object);              // no throw

    return reinterpret_cast<jlong>(wrapper);
}

JNIEXPORT void JNICALL Java_io_realm_internal_OsObject_nativeStartListening(JNIEnv* env, jobject instance,
                                                                            jlong native_ptr)
{
    TR_ENTER_PTR(native_ptr)

    try {
        auto wrapper = reinterpret_cast<ObjectWrapper*>(native_ptr);
        if (!wrapper->m_row_object_weak_ref) {
            wrapper->m_row_object_weak_ref = JavaGlobalWeakRef(env, instance);
        }

        static JavaClass os_object_class(env, "io/realm/internal/OsObject");
        static JavaMethod notify_change_listeners(env, os_object_class, "notifyChangeListeners",
                                                  "([Ljava/lang/String;)V");
        // The wrapper pointer will be used in the callback. But it should never become an invalid pointer when the
        // notification block gets called. This should be guaranteed by the Object Store that after the notification
        // token is destroyed, the block shouldn't be called.
        wrapper->m_notification_token = wrapper->m_object.add_notification_callback(ChangeCallback(wrapper, notify_change_listeners));
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_OsObject_nativeStopListening(JNIEnv* env, jobject, jlong native_ptr)
{
    TR_ENTER_PTR(native_ptr)

    try {
        auto wrapper = reinterpret_cast<ObjectWrapper*>(native_ptr);
        wrapper->m_notification_token = {};
    }
    CATCH_STD()
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_OsObject_nativeCreateRow(JNIEnv* env, jclass, jlong shared_realm_ptr,
                                                                        jlong table_ptr)
{
    try {
        return do_create_row(shared_realm_ptr, table_ptr);
    }
    CATCH_STD()
    return -1;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_OsObject_nativeCreateNewObject(JNIEnv* env, jclass,
                                                                              jlong shared_realm_ptr, jlong table_ptr)
{
    try {
        size_t row_ndx = do_create_row(shared_realm_ptr, table_ptr);
        auto& table = *(reinterpret_cast<realm::Table*>(table_ptr));
        return reinterpret_cast<jlong>(new Row(table[row_ndx]));
    }
    CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_OsObject_nativeCreateNewObjectWithLongPrimaryKey(
    JNIEnv* env, jclass, jlong shared_realm_ptr, jlong table_ptr, jlong pk_column_ndx, jlong pk_value,
    jboolean is_pk_null)
{
    try {
        auto& table = *(reinterpret_cast<realm::Table*>(table_ptr));
        size_t row_ndx =
            do_create_row_with_primary_key(env, shared_realm_ptr, table_ptr, pk_column_ndx, pk_value, is_pk_null);
        if (row_ndx != realm::npos) {
            return reinterpret_cast<jlong>(new Row(table[row_ndx]));
        }
    }
    CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_OsObject_nativeCreateRowWithLongPrimaryKey(
    JNIEnv* env, jclass, jlong shared_realm_ptr, jlong table_ptr, jlong pk_column_ndx, jlong pk_value,
    jboolean is_pk_null)
{
    try {
        return do_create_row_with_primary_key(env, shared_realm_ptr, table_ptr, pk_column_ndx, pk_value, is_pk_null);
    }
    CATCH_STD()
    return realm::npos;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_OsObject_nativeCreateNewObjectWithStringPrimaryKey(
    JNIEnv* env, jclass, jlong shared_realm_ptr, jlong table_ptr, jlong pk_column_ndx, jstring pk_value)
{
    try {
        auto& table = *(reinterpret_cast<realm::Table*>(table_ptr));
        size_t row_ndx = do_create_row_with_primary_key(env, shared_realm_ptr, table_ptr, pk_column_ndx, pk_value);
        if (row_ndx != realm::npos) {
            return reinterpret_cast<jlong>(new Row(table[row_ndx]));
        }
    }
    CATCH_STD()

    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_OsObject_nativeCreateRowWithStringPrimaryKey(
    JNIEnv* env, jclass, jlong shared_realm_ptr, jlong table_ptr, jlong pk_column_ndx, jstring pk_value)
{
    try {
        return do_create_row_with_primary_key(env, shared_realm_ptr, table_ptr, pk_column_ndx, pk_value);
    }
    CATCH_STD()

    return realm::npos;
}

JNIEXPORT jstring JNICALL Java_io_realm_internal_OsObject_nativeGetObjectIdColumName(JNIEnv* env, jclass)
{
// TODO: Remove the macro and get the name from core when core has stable ID support.
#if REALM_ENABLE_SYNC
    const char* object_id_column_name = sync::object_id_column_name;
#else
    const char* object_id_column_name = "!OID";
#endif
    return to_jstring(env, object_id_column_name);
}
