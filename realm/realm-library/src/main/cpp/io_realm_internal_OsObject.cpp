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

#if REALM_ENABLE_SYNC
#include <realm/sync/object.hpp>
#endif

#include <realm/object-store/object_schema.hpp>
#include <realm/object-store/object.hpp>
#include <realm/object-store/shared_realm.hpp>

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
        std::vector<JavaGlobalRefByMove> field_names;
        auto table = m_wrapper->m_object.obj().get_table();
        for (const auto& col: change_set.columns) {
            if (col.second.empty()) {
                continue;
            }
            // FIXME: After full integration of the OS schema, parse the column name from
            // wrapper->m_object.get_object_schema() will be faster.
            field_names.push_back(JavaGlobalRefByMove(env, to_jstring(env, table->get_column_name(ColKey(col.first))), true));
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
    delete reinterpret_cast<ObjectWrapper*>(ptr);
}

static inline Obj do_create_row_with_primary_key(JNIEnv* env, jlong shared_realm_ptr, jlong table_ref_ptr,
                                                    jlong pk_column_key, jlong pk_value, jboolean is_pk_null)
{
    auto& shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
    TableRef table = TBL_REF(table_ref_ptr);
    ColKey col_key(pk_column_key);
    shared_realm->verify_in_write(); // throws
    if (is_pk_null && !COL_NULLABLE(env, table, pk_column_key)) { // throws
        return Obj();
    }

    if (is_pk_null) {

        if (bool(table->find_first_null(col_key))) {
            THROW_JAVA_EXCEPTION(env, PK_CONSTRAINT_EXCEPTION_CLASS, util::format(PK_EXCEPTION_MSG_FORMAT, "'null'"));
        }
    }
    else {
        if (bool(table->find_first_int(col_key, pk_value))) {
            THROW_JAVA_EXCEPTION(env, PK_CONSTRAINT_EXCEPTION_CLASS, util::format(PK_EXCEPTION_MSG_FORMAT, pk_value));
        }
    }

    Mixed pk_val = is_pk_null ? Mixed() : Mixed(util::Optional<int64_t>(pk_value));
    return table->create_object_with_primary_key(pk_val);
}

static inline Obj do_create_row_with_primary_key(JNIEnv* env, jlong shared_realm_ptr, jlong table_ref_ptr,
                                                    jlong pk_column_key, jstring pk_value)
{
    auto& shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
    TableRef table = TBL_REF(table_ref_ptr);
    ColKey col_key(pk_column_key);
    shared_realm->verify_in_write(); // throws
    JStringAccessor str_accessor(env, pk_value); // throws
    if (!pk_value && !COL_NULLABLE(env, table, pk_column_key)) { // throws
        return Obj();
    }

    if (pk_value) {
        if (bool(table->find_first_string(col_key, str_accessor))) {
            THROW_JAVA_EXCEPTION(env, PK_CONSTRAINT_EXCEPTION_CLASS,
                                 util::format(PK_EXCEPTION_MSG_FORMAT, str_accessor.operator std::string()));
        }
    }
    else {
        if (bool(table->find_first_null(col_key))) {
            THROW_JAVA_EXCEPTION(env, PK_CONSTRAINT_EXCEPTION_CLASS, util::format(PK_EXCEPTION_MSG_FORMAT, "'null'"));
        }
    }
    return table->create_object_with_primary_key(StringData(str_accessor));
}

static inline Obj do_create_row_with_object_id_primary_key(JNIEnv* env, jlong shared_realm_ptr, jlong table_ref_ptr,
                                                            jlong pk_column_key, jstring pk_value)
{
    auto& shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
    TableRef table = TBL_REF(table_ref_ptr);
    ColKey col_key(pk_column_key);
    shared_realm->verify_in_write(); // throws
    JStringAccessor str_accessor(env, pk_value); // throws
    if (!pk_value && !COL_NULLABLE(env, table, pk_column_key)) { // throws
        return Obj();
    }

    if (pk_value) {
        auto objectId = ObjectId(StringData(str_accessor).data());
        if (bool(table->find_first_object_id(col_key, objectId))) {
            THROW_JAVA_EXCEPTION(env, PK_CONSTRAINT_EXCEPTION_CLASS,
                                 util::format(PK_EXCEPTION_MSG_FORMAT, str_accessor.operator std::string()));
        }

        return table->create_object_with_primary_key(objectId);
    }
    else {
        if (bool(table->find_first_null(col_key))) {
            THROW_JAVA_EXCEPTION(env, PK_CONSTRAINT_EXCEPTION_CLASS, util::format(PK_EXCEPTION_MSG_FORMAT, "'null'"));
        }
        return table->create_object_with_primary_key(realm::util::Optional<realm::ObjectId>());
    }
}

static inline Obj do_create_row_with_uuid_primary_key(JNIEnv* env, jlong shared_realm_ptr, jlong table_ref_ptr,
                                                           jlong pk_column_key, jstring pk_value)
{
    auto& shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
    TableRef table = TBL_REF(table_ref_ptr);
    ColKey col_key(pk_column_key);
    shared_realm->verify_in_write(); // throws
    JStringAccessor str_accessor(env, pk_value); // throws
    if (!pk_value && !COL_NULLABLE(env, table, pk_column_key)) { //throws
        return Obj();
    }

    if (pk_value) {
        auto uuid = UUID(StringData(str_accessor).data());
        if (bool(table->find_first_uuid(col_key, uuid))) {
            THROW_JAVA_EXCEPTION(env, PK_CONSTRAINT_EXCEPTION_CLASS,
                                 util::format(PK_EXCEPTION_MSG_FORMAT, str_accessor.operator std::string()));
        }

        return table->create_object_with_primary_key(uuid);
    }
    else {
        if (bool(table->find_first_null(col_key))) {
            THROW_JAVA_EXCEPTION(env, PK_CONSTRAINT_EXCEPTION_CLASS, util::format(PK_EXCEPTION_MSG_FORMAT, "'null'"));
        }
        return table->create_object_with_primary_key(realm::util::Optional<realm::UUID>());
    }
}


JNIEXPORT jlong JNICALL Java_io_realm_internal_OsObject_nativeGetFinalizerPtr(JNIEnv*, jclass)
{
    return reinterpret_cast<jlong>(&finalize_object);
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_OsObject_nativeCreate(JNIEnv*, jclass, jlong shared_realm_ptr,
                                                                     jlong obj_ptr)
{
    // FIXME: Currently OsObject is only used for object notifications. Since the Object Store's schema has not been
    // fully integrated with realm-java, we pass a dummy ObjectSchema to create Object.
    static const ObjectSchema dummy_object_schema;
    auto& shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
    auto& obj = *(reinterpret_cast<Obj*>(obj_ptr));
    Object object(shared_realm, dummy_object_schema, obj); // no throw
    auto wrapper = new ObjectWrapper(object);              // no throw
    return reinterpret_cast<jlong>(wrapper);
}

JNIEXPORT void JNICALL Java_io_realm_internal_OsObject_nativeStartListening(JNIEnv* env, jobject instance,
                                                                            jlong native_ptr)
{
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
    try {
        auto wrapper = reinterpret_cast<ObjectWrapper*>(native_ptr);
        wrapper->m_notification_token = {};
    }
    CATCH_STD()
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_OsObject_nativeCreateRow(JNIEnv* env, jclass, jlong table_ref_ptr)
{
    try {
        TableRef table = TBL_REF(table_ref_ptr);
        Obj obj = table->create_object();
        return (jlong)(obj.get_key().value);
    }
    CATCH_STD()
    return -1;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_OsObject_nativeCreateNewObject(JNIEnv* env, jclass, jlong table_ref_ptr)
{
    try {
        TableRef table = TBL_REF(table_ref_ptr);
        Obj* obj = new Obj(table->create_object());
        return reinterpret_cast<jlong>(obj);
    }
    CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_OsObject_nativeCreateNewObjectWithLongPrimaryKey(
    JNIEnv* env, jclass, jlong shared_realm_ptr, jlong table_ref_ptr, jlong pk_column_ndx, jlong pk_value,
    jboolean is_pk_null)
{
    try {
        Obj obj =
            do_create_row_with_primary_key(env, shared_realm_ptr, table_ref_ptr, pk_column_ndx, pk_value, is_pk_null);
        if (bool(obj)) {
            return reinterpret_cast<jlong>(new Obj(obj));
        }
    }
    CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_OsObject_nativeCreateRowWithLongPrimaryKey(
    JNIEnv* env, jclass, jlong shared_realm_ptr, jlong table_ref_ptr, jlong pk_column_ndx, jlong pk_value,
    jboolean is_pk_null)
{
    try {
        Obj obj = do_create_row_with_primary_key(env, shared_realm_ptr, table_ref_ptr, pk_column_ndx, pk_value, is_pk_null);
        return (jlong)(obj.get_key().value);
    }
    CATCH_STD()
    return realm::npos;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_OsObject_nativeCreateNewObjectWithStringPrimaryKey(
    JNIEnv* env, jclass, jlong shared_realm_ptr, jlong table_ref_ptr, jlong pk_column_ndx, jstring pk_value)
{
    try {
        Obj obj = do_create_row_with_primary_key(env, shared_realm_ptr, table_ref_ptr, pk_column_ndx, pk_value);
        if (bool(obj)) {
            return reinterpret_cast<jlong>(new Obj(obj));
        }
    }
    CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_OsObject_nativeCreateRowWithStringPrimaryKey(
    JNIEnv* env, jclass, jlong shared_realm_ptr, jlong table_ref_ptr, jlong pk_column_ndx, jstring pk_value)
{
    try {
        Obj obj = do_create_row_with_primary_key(env, shared_realm_ptr, table_ref_ptr, pk_column_ndx, pk_value);
        return (jlong)(obj.get_key().value);
    }
    CATCH_STD()
    return realm::npos;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_OsObject_nativeCreateRowWithObjectIdPrimaryKey(
        JNIEnv* env, jclass, jlong shared_realm_ptr, jlong table_ref_ptr, jlong pk_column_ndx, jstring pk_value)
{
    try {
        Obj obj = do_create_row_with_object_id_primary_key(env, shared_realm_ptr, table_ref_ptr, pk_column_ndx, pk_value);
        return (jlong)(obj.get_key().value);
    }
    CATCH_STD()
    return realm::npos;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_OsObject_nativeCreateNewObjectWithObjectIdPrimaryKey(
        JNIEnv* env, jclass, jlong shared_realm_ptr, jlong table_ref_ptr, jlong pk_column_ndx, jstring pk_value)
{
    try {
        Obj obj = do_create_row_with_object_id_primary_key(env, shared_realm_ptr, table_ref_ptr, pk_column_ndx, pk_value);
        if (bool(obj)) {
            return reinterpret_cast<jlong>(new Obj(obj));
        }
    }
    CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_OsObject_nativeCreateRowWithUUIDPrimaryKey(
        JNIEnv* env, jclass, jlong shared_realm_ptr, jlong table_ref_ptr, jlong pk_column_ndx, jstring pk_value)
{
    try {
        Obj obj = do_create_row_with_uuid_primary_key(env, shared_realm_ptr, table_ref_ptr, pk_column_ndx, pk_value);
        return (jlong)(obj.get_key().value);
    }
    CATCH_STD()
    return realm::npos;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_OsObject_nativeCreateNewObjectWithUUIDPrimaryKey(
        JNIEnv* env, jclass, jlong shared_realm_ptr, jlong table_ref_ptr, jlong pk_column_ndx, jstring pk_value)
{
    try {
        Obj obj = do_create_row_with_uuid_primary_key(env, shared_realm_ptr, table_ref_ptr, pk_column_ndx, pk_value);
        if (bool(obj)) {
            return reinterpret_cast<jlong>(new Obj(obj));
        } else {
            THROW_JAVA_EXCEPTION(env, PK_CONSTRAINT_EXCEPTION_CLASS, "Invalid Object returned from 'do_create_row_with_uuid_primary_key'");
        }
    }
    CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_OsObject_nativeCreateEmbeddedObject(
    JNIEnv* env, jclass, jlong j_parent_table_ptr, jlong j_parent_object_key, jlong j_parent_column_key)
{
    try {
        TableRef table = TBL_REF(j_parent_table_ptr);
        ObjKey obj_key(static_cast<int64_t>(j_parent_object_key));
        Obj parent_obj = table->get_object(obj_key);
        ColKey col_key(static_cast<int64_t>(j_parent_column_key));
        Obj child_obj;
        if (table->get_column_type(col_key) == type_Link) {
            child_obj = parent_obj.create_and_set_linked_object(col_key);
        } else {
            LnkLstPtr list = parent_obj.get_linklist_ptr(col_key);
            child_obj = list->create_and_insert_linked_object(list->size());
        }
        return to_jlong_or_not_found(child_obj.get_key());
    }
    CATCH_STD()
    return 0;
}
