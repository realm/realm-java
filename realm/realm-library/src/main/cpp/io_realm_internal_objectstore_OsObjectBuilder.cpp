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

JNIEXPORT void JNICALL Java_io_realm_internal_objectstore_OsObjectBuilder_nativeAddUUID
        (JNIEnv* env, jclass, jlong data_ptr, jlong column_key, jstring j_data)
{
    try {
        JStringAccessor data(env, j_data);
        UUID uuid = UUID(StringData(data).data());
        const JavaValue value(uuid);
        add_property(data_ptr, column_key, value);
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_objectstore_OsObjectBuilder_nativeAddMixed
        (JNIEnv* env, jclass, jlong data_ptr, jlong column_key, jlong native_ptr)
{
    try {
        auto java_value = reinterpret_cast<JavaValue*>(native_ptr);
        const JavaValue value(java_value);
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
        throw std::runtime_error(util::format("Class '%1' cannot be found in the schema.", class_name.data()));
    }
    return *it;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_objectstore_OsObjectBuilder_nativeCreateOrUpdateTopLevelObject(JNIEnv* env,
                                                                                                              jclass,
                                                                                                              jlong shared_realm_ptr,
                                                                                                              jlong table_ref_ptr,
                                                                                                              jlong builder_ptr,
                                                                                                              jboolean update_existing,
                                                                                                              jboolean ignore_same_values)
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

JNIEXPORT jlong JNICALL Java_io_realm_internal_objectstore_OsObjectBuilder_nativeUpdateEmbeddedObject(JNIEnv* env,
                                                                                                      jclass,
                                                                                                      jlong shared_realm_ptr,
                                                                                                      jlong table_ref_ptr,
                                                                                                      jlong builder_ptr,
                                                                                                      jlong j_obj_key,
                                                                                                      jboolean ignore_same_values)
{
    try {
        SharedRealm shared_realm = *(reinterpret_cast<SharedRealm*>(shared_realm_ptr));
        CreatePolicy policy = (ignore_same_values) ? CreatePolicy::UpdateModified : CreatePolicy::UpdateAll;
        TableRef table = TBL_REF(table_ref_ptr);
        ObjKey embedded_object_key(j_obj_key);
        const auto& schema = shared_realm->schema();
        const ObjectSchema& object_schema = get_schema(schema, table);
        JavaContext ctx(env, shared_realm, object_schema);
        auto list = *reinterpret_cast<OsObjectData*>(builder_ptr);
        JavaValue values = JavaValue(list);
        Object obj = Object::create(ctx, shared_realm, object_schema, values, policy, embedded_object_key);
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

JNIEXPORT void JNICALL Java_io_realm_internal_objectstore_OsObjectBuilder_nativeAddUUIDListItem
        (JNIEnv* env, jclass, jlong list_ptr, jstring j_data)
{
    try {
        JStringAccessor data(env, j_data);
        UUID uuid = UUID(StringData(data).data());
        const JavaValue value(uuid);
        add_list_element(list_ptr, value);
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_objectstore_OsObjectBuilder_nativeAddMixedListItem
        (JNIEnv* env, jclass, jlong list_ptr, jlong mixed_ptr)
{
    try {
        auto java_value = *reinterpret_cast<JavaValue *>(mixed_ptr);
        add_list_element(list_ptr, java_value);
    }
    CATCH_STD()
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_objectstore_OsObjectBuilder_nativeStartDictionary(JNIEnv* env,
                                                                         jclass) {
    try {
        auto dictionary = new std::map<std::string, JavaValue>();
        return reinterpret_cast<jlong>(dictionary);
    }
    CATCH_STD()
    return realm::npos;
}

JNIEXPORT void JNICALL
Java_io_realm_internal_objectstore_OsObjectBuilder_nativeStopDictionary(JNIEnv* env, jclass,
                                                                        jlong data_ptr,
                                                                        jlong column_key,
                                                                        jlong dictionary_ptr) {
    try {
        auto dictionary = reinterpret_cast<std::map<std::string, JavaValue>*>(dictionary_ptr);
        const JavaValue value((*dictionary));
        add_property(data_ptr, column_key, value);
        delete dictionary;
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL
Java_io_realm_internal_objectstore_OsObjectBuilder_nativeAddNullDictionaryEntry(JNIEnv *env,
                                                                                jclass,
                                                                                jlong dictionary_ptr,
                                                                                jstring j_key) {
    try {
        auto dictionary = reinterpret_cast<std::map<std::string, JavaValue>*>(dictionary_ptr);
        JStringAccessor key(env, j_key);
        const JavaValue value = JavaValue();
        dictionary->insert(std::make_pair(key, value));
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL
Java_io_realm_internal_objectstore_OsObjectBuilder_nativeAddBooleanDictionaryEntry(JNIEnv* env,
                                                                                   jclass,
                                                                                   jlong dictionary_ptr,
                                                                                   jstring j_key,
                                                                                   jboolean j_value) {
    try {
        auto dictionary = reinterpret_cast<std::map<std::string, JavaValue>*>(dictionary_ptr);
        JStringAccessor key(env, j_key);
        const JavaValue value(j_value);
        dictionary->insert(std::make_pair(key, value));
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL
Java_io_realm_internal_objectstore_OsObjectBuilder_nativeAddStringDictionaryEntry(JNIEnv* env,
                                                                                  jclass,
                                                                                  jlong dictionary_ptr,
                                                                                  jstring j_key,
                                                                                  jstring j_value) {
    try {
        auto dictionary = reinterpret_cast<std::map<std::string, JavaValue>*>(dictionary_ptr);
        JStringAccessor key(env, j_key);
        JStringAccessor value(env, j_value);
        JavaValue java_value(value);
        dictionary->insert(std::make_pair(key, value));
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL
Java_io_realm_internal_objectstore_OsObjectBuilder_nativeAddIntegerDictionaryEntry(JNIEnv* env,
                                                                                   jclass,
                                                                                   jlong dictionary_ptr,
                                                                                   jstring j_key,
                                                                                   jlong j_value) {
    try {
        auto dictionary = reinterpret_cast<std::map<std::string, JavaValue>*>(dictionary_ptr);
        JStringAccessor key(env, j_key);
        const JavaValue value(j_value);
        dictionary->insert(std::make_pair(key, value));
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL
Java_io_realm_internal_objectstore_OsObjectBuilder_nativeAddDoubleDictionaryEntry(JNIEnv* env,
                                                                                  jclass,
                                                                                  jlong dictionary_ptr,
                                                                                  jstring j_key,
                                                                                  jdouble j_value) {
    try {
        auto dictionary = reinterpret_cast<std::map<std::string, JavaValue>*>(dictionary_ptr);
        JStringAccessor key(env, j_key);
        const JavaValue value(j_value);
        dictionary->insert(std::make_pair(key, value));
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL
Java_io_realm_internal_objectstore_OsObjectBuilder_nativeAddFloatDictionaryEntry(JNIEnv* env,
                                                                                 jclass,
                                                                                 jlong dictionary_ptr,
                                                                                 jstring j_key,
                                                                                 jfloat j_value) {
    try {
        auto dictionary = reinterpret_cast<std::map<std::string, JavaValue>*>(dictionary_ptr);
        JStringAccessor key(env, j_key);
        const JavaValue value(j_value);
        dictionary->insert(std::make_pair(key, value));
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL
Java_io_realm_internal_objectstore_OsObjectBuilder_nativeAddBinaryDictionaryEntry(JNIEnv* env,
                                                                                  jclass,
                                                                                  jlong dictionary_ptr,
                                                                                  jstring j_key,
                                                                                  jbyteArray j_value) {
    try {
        auto dictionary = reinterpret_cast<std::map<std::string, JavaValue>*>(dictionary_ptr);
        JStringAccessor key(env, j_key);
        auto data = OwnedBinaryData(JByteArrayAccessor(env, j_value).transform<BinaryData>());
        const JavaValue value(data);
        dictionary->insert(std::make_pair(key, value));
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL
Java_io_realm_internal_objectstore_OsObjectBuilder_nativeAddDateDictionaryEntry(JNIEnv* env,
                                                                                jclass,
                                                                                jlong dictionary_ptr,
                                                                                jstring j_key,
                                                                                jlong j_value) {
    try {
        auto dictionary = reinterpret_cast<std::map<std::string, JavaValue>*>(dictionary_ptr);
        JStringAccessor key(env, j_key);
        const JavaValue value(from_milliseconds(j_value));
        dictionary->insert(std::make_pair(key, value));
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL
Java_io_realm_internal_objectstore_OsObjectBuilder_nativeAddDecimal128DictionaryEntry(JNIEnv* env,
                                                                                      jclass,
                                                                                      jlong dictionary_ptr,
                                                                                      jstring j_key,
                                                                                      jlong j_high_value,
                                                                                      jlong j_low_value) {
    try {
        auto dictionary = reinterpret_cast<std::map<std::string, JavaValue>*>(dictionary_ptr);

        JStringAccessor key(env, j_key);

        Decimal128::Bid128 raw {static_cast<uint64_t>(j_low_value), static_cast<uint64_t>(j_high_value)};
        auto decimal128 = Decimal128(raw);
        const JavaValue value(decimal128);

        dictionary->insert(std::make_pair(key, value));
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL
Java_io_realm_internal_objectstore_OsObjectBuilder_nativeAddObjectIdDictionaryEntry(JNIEnv* env,
                                                                                    jclass,
                                                                                    jlong dictionary_ptr,
                                                                                    jstring j_key,
                                                                                    jstring j_value) {
    try {
        auto dictionary = reinterpret_cast<std::map<std::string, JavaValue>*>(dictionary_ptr);

        JStringAccessor key(env, j_key);
        JStringAccessor data(env, j_value);

        const ObjectId object_id = ObjectId(StringData(data).data());
        const JavaValue object_id_value(object_id);

        dictionary->insert(std::make_pair(key, object_id_value));
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL
Java_io_realm_internal_objectstore_OsObjectBuilder_nativeAddUUIDDictionaryEntry(JNIEnv* env,
                                                                                jclass,
                                                                                jlong dictionary_ptr,
                                                                                jstring j_key,
                                                                                jstring j_value) {
    try {
        auto dictionary = reinterpret_cast<std::map<std::string, JavaValue>*>(dictionary_ptr);

        JStringAccessor key(env, j_key);
        JStringAccessor data(env, j_value);

        const UUID uuid = UUID(StringData(data).data());
        const JavaValue uuid_value(uuid);

        dictionary->insert(std::make_pair(key, uuid_value));
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL
Java_io_realm_internal_objectstore_OsObjectBuilder_nativeAddMixedDictionaryEntry(JNIEnv* env,
                                                                                 jclass,
                                                                                 jlong dictionary_ptr,
                                                                                 jstring j_key,
                                                                                 jlong mixed_ptr) {
    try {
        auto dictionary = reinterpret_cast<std::map<std::string, JavaValue>*>(dictionary_ptr);
        auto mixed_java_value = *reinterpret_cast<JavaValue *>(mixed_ptr);

        JStringAccessor key(env, j_key);

        dictionary->insert(std::make_pair(key, std::move(mixed_java_value)));
    }
    CATCH_STD()
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_objectstore_OsObjectBuilder_nativeStartSet(JNIEnv* env,
                                                                  jclass,
                                                                  jlong j_size) {
    try {
        auto set_as_list = new std::vector<JavaValue>();
        set_as_list->reserve(j_size);
        return reinterpret_cast<jlong>(set_as_list);
    }
    CATCH_STD()
    return realm::npos;
}

JNIEXPORT void JNICALL
Java_io_realm_internal_objectstore_OsObjectBuilder_nativeStopSet(JNIEnv* env,
                                                                 jclass,
                                                                 jlong data_ptr,
                                                                 jlong column_key,
                                                                 jlong set_ptr) {
    try {
        auto set_as_list = reinterpret_cast<std::vector<JavaValue>*>(set_ptr);
        const JavaValue value((*set_as_list));
        add_property(data_ptr, column_key, value);
        delete set_as_list;
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL
Java_io_realm_internal_objectstore_OsObjectBuilder_nativeAddNullSetItem(JNIEnv* env,
                                                                        jclass,
                                                                        jlong set_ptr) {
    try {
        const JavaValue value = JavaValue();
        add_list_element(set_ptr, value);
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL
Java_io_realm_internal_objectstore_OsObjectBuilder_nativeAddStringSetItem(JNIEnv* env,
                                                                          jclass,
                                                                          jlong set_ptr,
                                                                          jstring j_value) {
    try {
        JStringAccessor value(env, j_value);
        std::string string_value(value);
        const JavaValue wrapped_value(string_value);
        add_list_element(set_ptr, wrapped_value);
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL
Java_io_realm_internal_objectstore_OsObjectBuilder_nativeAddIntegerSetItem(JNIEnv* env,
                                                                           jclass,
                                                                           jlong set_ptr,
                                                                           jlong j_value) {
    try {
        const JavaValue value(j_value);
        add_list_element(set_ptr, value);
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL
Java_io_realm_internal_objectstore_OsObjectBuilder_nativeAddByteArraySetItem(JNIEnv* env,
                                                                             jclass,
                                                                             jlong set_ptr,
                                                                             jbyteArray j_value) {
    try {
        auto data = OwnedBinaryData(JByteArrayAccessor(env, j_value).transform<BinaryData>());
        const JavaValue value(data);
        add_list_element(set_ptr, value);
    }
    CATCH_STD()
}
