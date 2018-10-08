/*
 * Copyright 2018 Realm Inc.
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

#include "io_realm_internal_objectstore_OsObjectBuilder.h"

#include "java_object_accessor.hpp"
#include "util.hpp"

#include <realm/util/any.hpp>

using namespace realm;
using namespace realm::jni_util;
using namespace realm::_impl;

typedef std::map<std::string, util::Any> OsObjectData;

static void finalize_object(jlong ptr)
{
    TR_ENTER_PTR(ptr);
    delete reinterpret_cast<OsObjectData*>(ptr);
}


JNIEXPORT jlong JNICALL Java_io_realm_internal_objectstore_OsObjectBuilder_nativeGetFinalizerPtr(JNIEnv*, jclass)
{
    TR_ENTER()
    return reinterpret_cast<jlong>(&finalize_object);
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_objectstore_OsObjectBuilder_nativeCreateBuilder(JNIEnv*, jclass)
{
    TR_ENTER()
    return reinterpret_cast<jlong>(new std::map<std::string, util::Any>);
}

static inline void add_property(JNIEnv* env, jlong data_ptr, jstring& j_key, util::Any& value)
{
    try {
        OsObjectData *data = reinterpret_cast<OsObjectData *>(data_ptr);
        std::string key(JStringAccessor(env, j_key));
        data->insert(std::pair<std::string, util::Any>(key, value));
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_objectstore_OsObjectBuilder_nativeAddNull
        (JNIEnv* env, jclass, jlong data_ptr, jstring j_key)
{
    auto value = util::Any();
    add_property(env, data_ptr, j_key, value);
}

JNIEXPORT void JNICALL Java_io_realm_internal_objectstore_OsObjectBuilder_nativeAddString
    (JNIEnv* env, jclass, jlong data_ptr, jstring j_key, jstring j_value)
{
    auto value = util::Any(JStringAccessor(env, j_value));
    add_property(env, data_ptr, j_key, value);
}

JNIEXPORT void JNICALL Java_io_realm_internal_objectstore_OsObjectBuilder_nativeAddInteger
        (JNIEnv* env, jclass, jlong data_ptr, jstring j_key, jlong j_value)
{
    auto value = util::Any(j_value);
    add_property(env, data_ptr, j_key, value);
}

JNIEXPORT void JNICALL Java_io_realm_internal_objectstore_OsObjectBuilder_nativeAddFloat
        (JNIEnv* env, jclass, jlong data_ptr, jstring j_key, jfloat j_value)
{
    auto value = util::Any(j_value);
    add_property(env, data_ptr, j_key, value);
}

JNIEXPORT void JNICALL Java_io_realm_internal_objectstore_OsObjectBuilder_nativeAddDouble
        (JNIEnv* env, jclass, jlong data_ptr, jstring j_key, jdouble j_value)
{
    auto value = util::Any(j_value);
    add_property(env, data_ptr, j_key, value);
}

JNIEXPORT void JNICALL Java_io_realm_internal_objectstore_OsObjectBuilder_nativeAddBoolean
        (JNIEnv* env, jclass, jlong data_ptr, jstring j_key, jboolean j_value)
{
    auto value = util::Any(j_value);
    add_property(env, data_ptr, j_key, value);
}

JNIEXPORT void JNICALL Java_io_realm_internal_objectstore_OsObjectBuilder_nativeAddByteArray
        (JNIEnv* env, jclass, jlong data_ptr, jstring j_key, jbyteArray j_value)
{
    auto value = util::Any(j_value);
    add_property(env, data_ptr, j_key, value);
}

JNIEXPORT void JNICALL Java_io_realm_internal_objectstore_OsObjectBuilder_nativeAddDate
        (JNIEnv* env, jclass, jlong data_ptr, jstring j_key, jlong j_value)
{
    auto value = util::Any(j_value);
    add_property(env, data_ptr, j_key, value);
}


JNIEXPORT void JNICALL Java_io_realm_internal_objectstore_OsObjectBuilder_nativeAddObject
        (JNIEnv* env, jclass, jlong data_ptr, jstring j_key, jlong row_ptr)
{
    auto value = util::Any(*reinterpret_cast<Row*>(row_ptr));
    add_property(env, data_ptr, j_key, value);
}

static inline const ObjectSchema& get_schema(const Schema& schema, Table* table)
{
    std::string table_name(table->get_name());
    std::string class_name = std::string(table_name.substr(TABLE_PREFIX.length()));
    auto it = schema.find(class_name);
    if (it == schema.end()) {
        throw std::runtime_error(format("Class '%1' cannot be found in the schema.", class_name.data()));
    }
    return *it;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_objectstore_OsObjectBuilder_nativeCreateOrUpdate
        (JNIEnv* env, jclass, jlong shared_realm_ptr, jlong table_ptr, jlong builder_ptr, jboolean update_existing)
{
//    try {
        SharedRealm shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
        Table* table = reinterpret_cast<realm::Table*>(table_ptr);
        const auto& schema = shared_realm->schema();
        const ObjectSchema& object_schema = get_schema(schema, table);
        JavaContext ctx(env, shared_realm, object_schema);
        util::Any values = util::Any(*(reinterpret_cast<OsObjectData*>(builder_ptr)));
        Object obj = Object::create(ctx, shared_realm, object_schema, values, update_existing);
        return reinterpret_cast<jlong>(new Row(obj.row()));
//    }
//    CATCH_STD()
//    return realm::npos;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_objectstore_OsObjectBuilder_nativeStartList
        (JNIEnv* env, jclass, jlong list_size)
{
    try {
        auto list = new std::vector<util::Any>();
        list->reserve(list_size);
        return reinterpret_cast<jlong>(list);
    }
    CATCH_STD()
    return realm::npos;
}

JNIEXPORT void JNICALL Java_io_realm_internal_objectstore_OsObjectBuilder_nativeStopList
        (JNIEnv* env, jclass, jlong data_ptr, jstring j_key, jlong list_ptr)
{
    try {
        auto list = reinterpret_cast<std::vector<util::Any>*>(list_ptr);
        auto value = util::Any((*list));
        add_property(env, data_ptr, j_key, value);
        delete list;
    }
    CATCH_STD()
}


JNIEXPORT void JNICALL Java_io_realm_internal_objectstore_OsObjectBuilder_nativeAddObjectList
        (JNIEnv* env, jclass, jlong data_ptr, jstring j_key, jlongArray row_ptrs)
{
    auto rows = JLongArrayAccessor(env, row_ptrs);
    auto list = new std::vector<util::Any>();
    list->reserve(rows.size());
    for (jsize i = 0; i < rows.size(); ++i) {
         list->push_back(util::Any(*reinterpret_cast<Row*>(rows[i])));
    }
    auto value = util::Any(list);
    add_property(env, data_ptr, j_key, value);
}


static inline void add_list_element(JNIEnv* env, jlong list_ptr, util::Any& value)
{
    try {
        auto list = reinterpret_cast<std::vector<util::Any>*>(list_ptr);
        list->push_back(value);
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_objectstore_OsObjectBuilder_nativeAddNullListItem
        (JNIEnv* env, jclass, jlong list_ptr)
{
    auto value = util::Any();
    add_list_element(env, list_ptr, value);
}

JNIEXPORT void JNICALL Java_io_realm_internal_objectstore_OsObjectBuilder_nativeAddIntegerListItem
        (JNIEnv* env, jclass, jlong list_ptr, jlong j_value)
{
    auto value = util::Any(j_value);
    add_list_element(env, list_ptr, value);
}

JNIEXPORT void JNICALL Java_io_realm_internal_objectstore_OsObjectBuilder_nativeAddStringListItem
    (JNIEnv* env, jclass, jlong list_ptr, jstring j_value)
{
    auto value = util::Any(JStringAccessor(env, j_value));
    add_list_element(env, list_ptr, value);
}

JNIEXPORT void JNICALL Java_io_realm_internal_objectstore_OsObjectBuilder_nativeAddFloatListItem
        (JNIEnv* env, jclass, jlong list_ptr, jfloat j_value)
{
    auto value = util::Any(j_value);
    add_list_element(env, list_ptr, value);
}

JNIEXPORT void JNICALL Java_io_realm_internal_objectstore_OsObjectBuilder_nativeAddDoubleListItem
        (JNIEnv* env, jclass, jlong list_ptr, jdouble j_value)
{
    auto value = util::Any(j_value);
    add_list_element(env, list_ptr, value);
}

JNIEXPORT void JNICALL Java_io_realm_internal_objectstore_OsObjectBuilder_nativeAddBooleanListItem
        (JNIEnv* env, jclass, jlong list_ptr, jboolean j_value)
{
    auto value = util::Any(j_value);
    add_list_element(env, list_ptr, value);
}

JNIEXPORT void JNICALL Java_io_realm_internal_objectstore_OsObjectBuilder_nativeAddByteArrayListItem
        (JNIEnv* env, jclass, jlong list_ptr, jbyteArray j_value)
{
    auto value = util::Any(j_value);
    add_list_element(env, list_ptr, value);
}

JNIEXPORT void JNICALL Java_io_realm_internal_objectstore_OsObjectBuilder_nativeAddDateListItem
        (JNIEnv* env, jclass, jlong list_ptr, jlong j_value)
{
    auto value = util::Any(j_value);
    add_list_element(env, list_ptr, value);
}

JNIEXPORT void JNICALL Java_io_realm_internal_objectstore_OsObjectBuilder_nativeAddObjectListItem
        (JNIEnv* env, jclass, jlong list_ptr, jlong row_ptr)
{
    auto value = util::Any(*reinterpret_cast<Row*>(row_ptr));
    add_list_element(env, list_ptr, value);
}