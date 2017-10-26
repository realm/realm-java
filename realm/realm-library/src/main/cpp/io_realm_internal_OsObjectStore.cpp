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

#include <object_store.hpp>
#include <shared_realm.hpp>

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

JNIEXPORT void JNICALL Java_io_realm_internal_OsObjectStore_nativeSetPrimaryKeyForObject(JNIEnv* env, jclass,
                                                                                          jlong shared_realm_ptr,
                                                                                          jstring j_class_name,
                                                                                          jstring j_pk_field_name)
{
    TR_ENTER_PTR(shared_realm_ptr)
    try {
        auto& shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
        JStringAccessor class_name_accessor(env, j_class_name);
        JStringAccessor pk_field_name_accessor(env, j_pk_field_name);

        auto table = ObjectStore::table_for_object_type(shared_realm->read_group(), class_name_accessor);
        if (!table) {
            THROW_JAVA_EXCEPTION(env, JavaExceptionDef::IllegalArgument,
                                 format("Class '%1' doesn't exist.", StringData(class_name_accessor)));
        }

        if (j_pk_field_name) {
            // Not removal, check the column.
            auto pk_column_ndx = table->get_column_index(pk_field_name_accessor);
            if (pk_column_ndx == realm::npos) {
                THROW_JAVA_EXCEPTION(env, JavaExceptionDef::IllegalArgument,
                                     format("Field '%1' doesn't exist in Class '%2'.",
                                            StringData(pk_field_name_accessor), StringData(class_name_accessor)));
            }

            // Check valid column type
            auto field_type = table->get_column_type(pk_column_ndx);
            if (field_type != type_Int && field_type != type_String) {
                THROW_JAVA_EXCEPTION(
                    env, JavaExceptionDef::IllegalArgument,
                    format("Field '%1' is not a valid primary key type.", StringData(pk_field_name_accessor)));
            }

            // Check duplicated values. The pk field must have been indexed before set as a PK.
            if (table->get_distinct_view(pk_column_ndx).size() != table->size()) {
                THROW_JAVA_EXCEPTION(env, JavaExceptionDef::IllegalArgument,
                                     format("Field '%1' cannot be set as primary key since there are duplicated "
                                            "values for field '%1' in Class '%2'.",
                                            StringData(pk_field_name_accessor), StringData(class_name_accessor)));
            }
        }
        shared_realm->verify_in_write();
        ObjectStore::set_primary_key_for_object(shared_realm->read_group(), class_name_accessor,
                                                pk_field_name_accessor);
    }
    CATCH_STD()
}

JNIEXPORT jstring JNICALL Java_io_realm_internal_OsObjectStore_nativeGetPrimaryKeyForObject(JNIEnv* env, jclass,
                                                                                             jlong shared_realm_ptr,
                                                                                             jstring j_class_name)
{
    TR_ENTER_PTR(shared_realm_ptr)
    try {
        auto& shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
        JStringAccessor class_name_accessor(env, j_class_name);
        StringData pk_field_name =
            ObjectStore::get_primary_key_for_object(shared_realm->read_group(), class_name_accessor);
        return pk_field_name.size() == 0 ? nullptr : to_jstring(env, pk_field_name);
    }
    CATCH_STD()
    return nullptr;
}

JNIEXPORT void JNICALL Java_io_realm_internal_OsObjectStore_nativeSetSchemaVersion(JNIEnv* env, jclass,
                                                                                   jlong shared_realm_ptr,
                                                                                   jlong schema_version)
{
    TR_ENTER_PTR(shared_realm_ptr)
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
    TR_ENTER_PTR(shared_realm_ptr)
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
    TR_ENTER_PTR(shared_realm_ptr)
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
    TR_ENTER();
    try {
        JStringAccessor path_accessor(env, j_realm_path);
        std::string realm_path(path_accessor);
        static JavaClass runnable_class(env, "java/lang/Runnable");
        static JavaMethod run_method(env, runnable_class, "run", "()V");
        bool result = SharedGroup::call_with_lock(realm_path, [&](std::string path) {
            REALM_ASSERT_RELEASE_EX(realm_path.compare(path) == 0, realm_path.c_str(), path.c_str());
            env->CallVoidMethod(j_runnable, run_method);
            TERMINATE_JNI_IF_JAVA_EXCEPTION_OCCURRED(env, nullptr);
        });
        return result;
    }
    CATCH_STD()
    return false;
}
