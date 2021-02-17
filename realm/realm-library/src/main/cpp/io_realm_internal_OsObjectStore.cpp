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

#include "io_realm_internal_OsObjectStore.h"

#include <realm/object-store/object_store.hpp>
#include <realm/object-store/shared_realm.hpp>

#include "util.hpp"
#include "jni_util/java_method.hpp"
#include "jni_util/java_exception_thrower.hpp"
#include "jni_util/java_exception_thrower.hpp"

using namespace realm;
using namespace realm::jni_util;
using namespace realm::util;
using namespace realm::_impl;

static_assert(io_realm_internal_OsObjectStore_SCHEMA_NOT_VERSIONED == static_cast<jlong>(ObjectStore::NotVersioned),
              "");

inline static bool is_allowed_to_primary_key(JNIEnv* env, DataType column_type)
{
    if (column_type == type_String
        || column_type == type_Int
        || column_type == type_Bool
        || column_type == type_Timestamp
        || column_type == type_OldDateTime
        || column_type == type_ObjectId
        || column_type == type_UUID) {
        return true;
    }

    ThrowException(env, IllegalArgument, "This field cannot be a primary key - "
                                         "Only String/byte/short/int/long/boolean/Date/ObjectId/UUID fields are supported.");
    return false;
}

JNIEXPORT void JNICALL Java_io_realm_internal_OsObjectStore_nativeSetPrimaryKeyForObject(JNIEnv* env, jclass,
                                                                                          jlong shared_realm_ptr,
                                                                                          jstring j_class_name,
                                                                                          jstring j_pk_field_name)
{
    try {
        auto& shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
        JStringAccessor class_name(env, j_class_name);
        JStringAccessor primary_key_field_name(env, j_pk_field_name);

        auto& group = shared_realm->read_group();
        if (!group.has_table(class_name)) {
            std::string name_str = class_name;
            if (name_str.find(TABLE_PREFIX) == 0) {
                name_str = name_str.substr(TABLE_PREFIX.length());
            }
            THROW_JAVA_EXCEPTION(env, JavaExceptionDef::IllegalArgument,
                                 util::format("The class '%1' doesn't exist in this Realm.", name_str));
        }
        TableRef table = group.get_table(class_name);
        auto column_key = table->get_column_key(primary_key_field_name);
        if (j_pk_field_name && !is_allowed_to_primary_key(env, table->get_column_type(column_key))) {
            return;
        }
        shared_realm->verify_in_write();
        table->set_primary_key_column(column_key);
    }
    CATCH_STD()
}

JNIEXPORT jstring JNICALL Java_io_realm_internal_OsObjectStore_nativeGetPrimaryKeyForObject(JNIEnv* env, jclass,
                                                                                             jlong shared_realm_ptr,
                                                                                             jstring j_class_name)
{
    try {
        auto& shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
        JStringAccessor class_name(env, j_class_name);
        TableRef table = shared_realm->read_group().get_table(class_name);
        auto col = table->get_primary_key_column();
        std::string primary_key_field_name = (col) ? table->get_column_name(col) : "";
        return primary_key_field_name.size() == 0 ? nullptr : to_jstring(env, primary_key_field_name);
    }
    CATCH_STD()
    return nullptr;
}

JNIEXPORT void JNICALL Java_io_realm_internal_OsObjectStore_nativeSetSchemaVersion(JNIEnv* env, jclass,
                                                                                   jlong shared_realm_ptr,
                                                                                   jlong schema_version)
{
    try {
        auto& shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
        shared_realm->verify_in_write();
        ObjectStore::set_schema_version(shared_realm->read_group(), schema_version);
    }
    CATCH_STD()
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_OsObjectStore_nativeGetSchemaVersion(JNIEnv* env, jclass,
                                                                                    jlong shared_realm_ptr)
{
    try {
        auto& shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
        return ObjectStore::get_schema_version(shared_realm->read_group());
    }
    CATCH_STD()
    return ObjectStore::NotVersioned;
}

JNIEXPORT jboolean JNICALL Java_io_realm_internal_OsObjectStore_nativeDeleteTableForObject(JNIEnv* env, jclass,
                                                                                        jlong shared_realm_ptr,
                                                                                        jstring j_class_name)
{
    try {
        auto& shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
        JStringAccessor class_name_accessor(env, j_class_name);
        shared_realm->verify_in_write();
        if (!ObjectStore::table_for_object_type(shared_realm->read_group(), class_name_accessor)) {
            return JNI_FALSE;
        }
        ObjectStore::delete_data_for_object(shared_realm->read_group(), class_name_accessor);
        return JNI_TRUE;
    }
    CATCH_STD()
    return JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_io_realm_internal_OsObjectStore_nativeCallWithLock(JNIEnv* env, jclass,
                                                                                   jstring j_realm_path,
                                                                                   jobject j_runnable)
{
    try {
        JStringAccessor path_accessor(env, j_realm_path);
        std::string realm_path(path_accessor);
        static JavaClass runnable_class(env, "java/lang/Runnable");
        static JavaMethod run_method(env, runnable_class, "run", "()V");
        bool result = DB::call_with_lock(realm_path, [&](std::string path) {
            REALM_ASSERT_RELEASE_EX(realm_path.compare(path) == 0, realm_path.c_str(), path.c_str());
            env->CallVoidMethod(j_runnable, run_method);
            TERMINATE_JNI_IF_JAVA_EXCEPTION_OCCURRED(env, nullptr);
        });
        return result;
    }
    CATCH_STD()
    return false;
}
