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

typedef std::map<ColKey, JavaValue> OsObjectData;

JNIEXPORT void JNICALL Java_io_realm_internal_objectstore_OsObjectBuilder_nativeDestroyBuilder(JNIEnv*, jclass, jlong data_ptr)
{
    delete reinterpret_cast<OsObjectData*>(data_ptr);
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_objectstore_OsObjectBuilder_nativeCreateBuilder(JNIEnv* env, jclass)
{
    try {
        auto map = new std::map<ColKey, JavaValue>();
        return reinterpret_cast<jlong>(map);
    }
    CATCH_STD()
    return -1;
}

static inline void add_property(jlong data_ptr, jlong column_key, JavaValue const& value)
{
    OsObjectData* data = reinterpret_cast<OsObjectData*>(data_ptr);
    (*data)[ColKey(column_key)] = std::move(value);
}

JNIEXPORT void JNICALL Java_io_realm_internal_objectstore_OsObjectBuilder_nativeAddNull
        (JNIEnv* env, jclass, jlong data_ptr, jlong column_key)
{
    try {
        const JavaValue value = JavaValue();
        add_property(data_ptr, column_key, value);
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_objectstore_OsObjectBuilder_nativeAddString
    (JNIEnv* env, jclass, jlong data_ptr, jlong column_key, jstring j_value)
{
    try {
        JStringAccessor value(env, j_value);
        std::string string_value(value);
        const JavaValue wrapped_value(string_value);
        add_property(data_ptr, column_key, wrapped_value);
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_objectstore_OsObjectBuilder_nativeAddInteger
        (JNIEnv* env, jclass, jlong data_ptr, jlong column_key, jlong j_value)
{
    try {
        const JavaValue value(j_value);
        add_property(data_ptr, column_key, value);
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_objectstore_OsObjectBuilder_nativeAddFloat
        (JNIEnv* env, jclass, jlong data_ptr, jlong column_key, jfloat j_value)
{
    try {
        const JavaValue value(j_value);
        add_property(data_ptr, column_key, value);
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_objectstore_OsObjectBuilder_nativeAddDouble
        (JNIEnv* env, jclass, jlong data_ptr, jlong column_key, jdouble j_value)
{
    try {
        const JavaValue value(j_value);
        add_property(data_ptr, column_key, value);
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_objectstore_OsObjectBuilder_nativeAddBoolean
        (JNIEnv* env, jclass, jlong data_ptr, jlong column_key, jboolean j_value)
{
    try {
        const JavaValue value(j_value);
        add_property(data_ptr, column_key, value);
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_objectstore_OsObjectBuilder_nativeAddByteArray
        (JNIEnv* env, jclass, jlong data_ptr, jlong column_key, jbyteArray j_value)
{
    try {
        auto data = OwnedBinaryData(JByteArrayAccessor(env, j_value).transform<BinaryData>());
        const JavaValue value(data);
        add_property(data_ptr, column_key, value);
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_objectstore_OsObjectBuilder_nativeAddDate
        (JNIEnv* env, jclass, jlong data_ptr, jlong column_key, jlong j_value)
{
    try {
        const JavaValue value(from_milliseconds(j_value));
        add_property(data_ptr, column_key, value);
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_objectstore_OsObjectBuilder_nativeAddDecimal128
        (JNIEnv* env, jclass, jlong data_ptr, jlong column_key, jlong j_low_value, jlong j_high_value)
{
    try {
        Decimal128::Bid128 raw {static_cast<uint64_t>(j_low_value), static_cast<uint64_t>(j_high_value)};
        Decimal128 decimal128 = Decimal128(raw);
        const JavaValue value(decimal128);
        add_property(data_ptr, column_key, value);
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_objectstore_OsObjectBuilder_nativeAddObjectId
        (JNIEnv* env, jclass, jlong data_ptr, jlong column_key, jstring j_data)
{
    try {
        JStringAccessor data(env, j_data);
        ObjectId objectId = ObjectId(StringData(data).data());
        const JavaValue value(objectId);
        add_property(data_ptr, column_key, value);
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_objectstore_OsObjectBuilder_nativeAddObject
        (JNIEnv* env, jclass, jlong data_ptr, jlong column_key, jlong row_ptr)
{
    try {
        const JavaValue value(reinterpret_cast<Obj*>(row_ptr));
        add_property(data_ptr, column_key, value);
    }
    CATCH_STD()
}

static inline const ObjectSchema& get_schema(const Schema& schema, TableRef table)
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
        (JNIEnv* env, jclass, jlong shared_realm_ptr, jlong table_ref_ptr, jlong builder_ptr, jboolean update_existing, jboolean ignore_same_values)
{
    try {
        SharedRealm shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));

        CreatePolicy policy = CreatePolicy::ForceCreate;
        if (update_existing && ignore_same_values) {
            policy = CreatePolicy::UpdateModified;
        } else if (update_existing) {
            policy = CreatePolicy::UpdateAll;
        }

        TableRef table = TBL_REF(table_ref_ptr);
        const auto& schema = shared_realm->schema();
        const ObjectSchema& object_schema = get_schema(schema, table);
        JavaContext ctx(env, shared_realm, object_schema);
        auto list = *reinterpret_cast<OsObjectData*>(builder_ptr);
        JavaValue values = JavaValue(list);
        Object obj = Object::create(ctx, shared_realm, object_schema, values, policy);
        return reinterpret_cast<jlong>(new Obj(obj.obj()));
    }
    CATCH_STD()
    return realm::npos;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_objectstore_OsObjectBuilder_nativeStartList
        (JNIEnv* env, jclass, jlong list_size)
{
    try {
        auto list = new std::vector<JavaValue>();
        list->reserve(list_size);
        return reinterpret_cast<jlong>(list);
    }
    CATCH_STD()
    return realm::npos;
}

JNIEXPORT void JNICALL Java_io_realm_internal_objectstore_OsObjectBuilder_nativeStopList
        (JNIEnv* env, jclass, jlong data_ptr, jlong column_key, jlong list_ptr)
{
    try {
        auto list = reinterpret_cast<std::vector<JavaValue>*>(list_ptr);
        const JavaValue value((*list));
        add_property(data_ptr, column_key, value);
        delete list;
    }
    CATCH_STD()
}


JNIEXPORT void JNICALL Java_io_realm_internal_objectstore_OsObjectBuilder_nativeAddObjectList
        (JNIEnv* env, jclass, jlong data_ptr, jlong column_key, jlongArray row_ptrs)
{
    try {
        auto rows = JLongArrayAccessor(env, row_ptrs);
        auto list = std::vector<JavaValue>();
        list.reserve(rows.size());
        for (jsize i = 0; i < rows.size(); ++i) {
            auto item = JavaValue(reinterpret_cast<Obj*>(rows[i]));
            list.push_back(item);
        }
        JavaValue value(list);
        add_property(data_ptr, column_key, value);
    }
    CATCH_STD()
}

static inline void add_list_element(jlong list_ptr, JavaValue const& value)
{
    auto list = reinterpret_cast<std::vector<JavaValue>*>(list_ptr);
    list->push_back(std::move(value));
}

JNIEXPORT void JNICALL Java_io_realm_internal_objectstore_OsObjectBuilder_nativeAddNullListItem
        (JNIEnv* env, jclass, jlong list_ptr)
{
    try {
        const JavaValue value = JavaValue();
        add_list_element(list_ptr, value);
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_objectstore_OsObjectBuilder_nativeAddIntegerListItem
        (JNIEnv* env, jclass, jlong list_ptr, jlong j_value)
{
    try {
        const JavaValue value(j_value);
        add_list_element(list_ptr, value);
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_objectstore_OsObjectBuilder_nativeAddStringListItem
    (JNIEnv* env, jclass, jlong list_ptr, jstring j_value)
{
    try {
        JStringAccessor value(env, j_value);
        std::string string_value(value);
        const JavaValue wrapped_value(string_value);
        add_list_element(list_ptr, wrapped_value);
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_objectstore_OsObjectBuilder_nativeAddFloatListItem
        (JNIEnv* env, jclass, jlong list_ptr, jfloat j_value)
{
    try {
        const JavaValue value(j_value);
        add_list_element(list_ptr, value);
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_objectstore_OsObjectBuilder_nativeAddDoubleListItem
        (JNIEnv* env, jclass, jlong list_ptr, jdouble j_value)
{
    try {
        const JavaValue value(j_value);
        add_list_element(list_ptr, value);
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_objectstore_OsObjectBuilder_nativeAddBooleanListItem
        (JNIEnv* env, jclass, jlong list_ptr, jboolean j_value)
{
    try {
        const JavaValue value(j_value);
        add_list_element(list_ptr, value);
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_objectstore_OsObjectBuilder_nativeAddByteArrayListItem
        (JNIEnv* env, jclass, jlong list_ptr, jbyteArray j_value)
{
    try {
        auto data = OwnedBinaryData(JByteArrayAccessor(env, j_value).transform<BinaryData>());
        const JavaValue value(data);
        add_list_element(list_ptr, value);
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_objectstore_OsObjectBuilder_nativeAddDateListItem
        (JNIEnv* env, jclass, jlong list_ptr, jlong j_value)
{
    try {
        const JavaValue value(from_milliseconds(j_value));
        add_list_element(list_ptr, value);
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_objectstore_OsObjectBuilder_nativeAddObjectListItem
        (JNIEnv* env, jclass, jlong list_ptr, jlong row_ptr)
{
    try {
        const JavaValue value(reinterpret_cast<Obj*>(row_ptr));
        add_list_element(list_ptr, value);
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_objectstore_OsObjectBuilder_nativeAddDecimal128ListItem
        (JNIEnv* env, jclass, jlong list_ptr, jlong j_low_value, jlong j_high_value)
{
    try {
        Decimal128::Bid128 raw {static_cast<uint64_t>(j_low_value), static_cast<uint64_t>(j_high_value)};
        Decimal128 decimal128 = Decimal128(raw);
        const JavaValue value(decimal128);
        add_list_element(list_ptr, value);
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_objectstore_OsObjectBuilder_nativeAddObjectIdListItem
        (JNIEnv* env, jclass, jlong list_ptr, jstring j_data)
{
    try {
        JStringAccessor data(env, j_data);
        ObjectId objectId = ObjectId(StringData(data).data());
        const JavaValue value(objectId);
        add_list_element(list_ptr, value);
    }
    CATCH_STD()
}
