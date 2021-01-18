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

#include "io_realm_internal_OsList.h"

#include <realm/object-store/list.hpp>
#include <realm/object-store/results.hpp>
#include <realm/object-store/shared_realm.hpp>

#include "observable_collection_wrapper.hpp"
#include "java_accessor.hpp"
#include "java_object_accessor.hpp"
#include "java_exception_def.hpp"
#include "jni_util/java_exception_thrower.hpp"
#include "util.hpp"

using namespace realm;
using namespace realm::util;
using namespace realm::_impl;

typedef ObservableCollectionWrapper<List> ListWrapper;

namespace {
void finalize_list(jlong ptr)
{
    delete reinterpret_cast<ListWrapper*>(ptr);
}

inline void add_value(JNIEnv* env, jlong list_ptr, Any&& value)
{
    auto& wrapper = *reinterpret_cast<ListWrapper*>(list_ptr);

    JavaAccessorContext context(env);
    wrapper.collection().add(context, value);
}

inline void insert_value(JNIEnv* env, jlong list_ptr, jlong pos, Any&& value)
{
    auto& wrapper = *reinterpret_cast<ListWrapper*>(list_ptr);

    JavaAccessorContext context(env);
    wrapper.collection().insert(context, pos, value);
}

inline void set_value(JNIEnv* env, jlong list_ptr, jlong pos, Any&& value)
{
    auto& wrapper = *reinterpret_cast<ListWrapper*>(list_ptr);

    JavaAccessorContext context(env);
    wrapper.collection().set(context, pos, value);
}

// Check nullable earlier https://github.com/realm/realm-object-store/issues/544
inline void check_nullable(JNIEnv* env, jlong list_ptr, jobject jobject_ptr = nullptr)
{
    auto& wrapper = *reinterpret_cast<ListWrapper*>(list_ptr);
    if (!jobject_ptr && !is_nullable(wrapper.collection().get_type())) {
        THROW_JAVA_EXCEPTION(env, JavaExceptionDef::IllegalArgument,
                             "This 'RealmList' is not nullable. A non-null value is expected.");
    }
}
} // anonymous namespace

JNIEXPORT jlong JNICALL Java_io_realm_internal_OsList_nativeGetFinalizerPtr(JNIEnv*, jclass)
{
    return reinterpret_cast<jlong>(&finalize_list);
}

JNIEXPORT jlongArray JNICALL Java_io_realm_internal_OsList_nativeCreate(JNIEnv* env, jclass, jlong shared_realm_ptr,
                                                                        jlong obj_ptr, jlong column_key)
{
    try {
        auto& obj = *reinterpret_cast<realm::Obj*>(obj_ptr);

        auto& shared_realm = *reinterpret_cast<SharedRealm*>(shared_realm_ptr);
        jlong ret[2];

        List list(shared_realm, obj, ColKey(column_key));
        ListWrapper* wrapper_ptr = new ListWrapper(list);
        ret[0] = reinterpret_cast<jlong>(wrapper_ptr);

        if (wrapper_ptr->collection().get_type() == PropertyType::Object) {
            auto link_view_ref = obj.get_linklist(ColKey(column_key));

            TableRef* target_table_ptr = new TableRef(link_view_ref.get_target_table());
            ret[1] = reinterpret_cast<jlong>(target_table_ptr);
        }
        else {
            ret[1] = reinterpret_cast<jlong>(nullptr);
        }

        jlongArray ret_array = env->NewLongArray(2);
        if (!ret_array) {
            ThrowException(env, OutOfMemory, "Could not allocate memory to create OsList.");
            return nullptr;
        }
        env->SetLongArrayRegion(ret_array, 0, 2, ret);
        return ret_array;
    }
    CATCH_STD()
    return nullptr;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_OsList_nativeGetRow(JNIEnv* env, jclass, jlong list_ptr,
                                                                   jlong column_index)
{
    try {
        auto& wrapper = *reinterpret_cast<ListWrapper*>(list_ptr);
        auto obj = wrapper.collection().get(column_index);
        return reinterpret_cast<jlong>(new Obj(std::move(obj)));
    }
    CATCH_STD()
    return reinterpret_cast<jlong>(nullptr);
}

JNIEXPORT void JNICALL Java_io_realm_internal_OsList_nativeAddRow(JNIEnv* env, jclass, jlong list_ptr,
                                                                  jlong target_obj_key)
{

    try {
        auto& wrapper = *reinterpret_cast<ListWrapper*>(list_ptr);
        wrapper.collection().add(ObjKey(target_obj_key));
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_OsList_nativeInsertRow(JNIEnv* env, jclass, jlong list_ptr, jlong pos,
                                                                     jlong target_obj_key)
{
    try {
        auto& wrapper = *reinterpret_cast<ListWrapper*>(list_ptr);
        wrapper.collection().insert(static_cast<size_t>(pos), ObjKey(target_obj_key));
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_OsList_nativeSetRow(JNIEnv* env, jclass, jlong list_ptr, jlong pos,
                                                                  jlong target_obj_key)
{
    try {
        auto& wrapper = *reinterpret_cast<ListWrapper*>(list_ptr);
        wrapper.collection().set(static_cast<size_t>(pos), ObjKey(target_obj_key));
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_OsList_nativeMove(JNIEnv* env, jclass, jlong list_ptr,
                                                                jlong source_index, jlong target_index)
{
    try {
        auto& wrapper = *reinterpret_cast<ListWrapper*>(list_ptr);
        wrapper.collection().move(source_index, target_index);
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_OsList_nativeRemove(JNIEnv* env, jclass, jlong list_ptr, jlong index)
{
    try {
        auto& wrapper = *reinterpret_cast<ListWrapper*>(list_ptr);
        wrapper.collection().remove(index);
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_OsList_nativeRemoveAll(JNIEnv* env, jclass, jlong list_ptr)
{
    try {
        auto& wrapper = *reinterpret_cast<ListWrapper*>(list_ptr);
        wrapper.collection().remove_all();
    }
    CATCH_STD()
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_OsList_nativeSize(JNIEnv* env, jclass, jlong list_ptr)
{
    try {
        auto& wrapper = *reinterpret_cast<ListWrapper*>(list_ptr);
        return wrapper.collection().size();
    }
    CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_OsList_nativeGetQuery(JNIEnv* env, jclass, jlong list_ptr)
{
    try {
        auto& wrapper = *reinterpret_cast<ListWrapper*>(list_ptr);
        auto query = wrapper.collection().get_query();
        return reinterpret_cast<jlong>(new Query(std::move(query)));
    }
    CATCH_STD()
    return reinterpret_cast<jlong>(nullptr);
}

JNIEXPORT jboolean JNICALL Java_io_realm_internal_OsList_nativeIsValid(JNIEnv* env, jclass, jlong list_ptr)
{
    try {
        auto& wrapper = *reinterpret_cast<ListWrapper*>(list_ptr);
        return wrapper.collection().is_valid();
    }
    CATCH_STD()
    return JNI_FALSE;
}

JNIEXPORT void JNICALL Java_io_realm_internal_OsList_nativeDelete(JNIEnv* env, jclass, jlong list_ptr, jlong index)
{
    try {
        auto& wrapper = *reinterpret_cast<ListWrapper*>(list_ptr);
        wrapper.collection().delete_at(S(index));
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_OsList_nativeDeleteAll(JNIEnv* env, jclass, jlong list_ptr)
{
    try {
        auto& wrapper = *reinterpret_cast<ListWrapper*>(list_ptr);
        wrapper.collection().delete_all();
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_OsList_nativeStartListening(JNIEnv* env, jobject instance,
                                                                              jlong native_ptr)
{
    try {
        auto wrapper = reinterpret_cast<ListWrapper*>(native_ptr);
        wrapper->start_listening(env, instance);
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_OsList_nativeStopListening(JNIEnv* env, jobject, jlong native_ptr)
{
    try {
        auto wrapper = reinterpret_cast<ListWrapper*>(native_ptr);
        wrapper->stop_listening();
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_OsList_nativeAddNull(JNIEnv* env, jclass, jlong list_ptr)
{
    try {
        check_nullable(env, list_ptr);
        add_value(env, list_ptr, Any());
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_OsList_nativeInsertNull(JNIEnv* env, jclass, jlong list_ptr, jlong pos)
{
    try {
        check_nullable(env, list_ptr);
        insert_value(env, list_ptr, pos, Any());
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_OsList_nativeSetNull(JNIEnv* env, jclass, jlong list_ptr, jlong pos)
{
    try {
        check_nullable(env, list_ptr);
        set_value(env, list_ptr, pos, Any());
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_OsList_nativeAddLong(JNIEnv* env, jclass, jlong list_ptr, jlong value)
{
    try {
        add_value(env, list_ptr, Any(value));
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_OsList_nativeInsertLong(JNIEnv* env, jclass, jlong list_ptr, jlong pos,
                                                                      jlong value)
{
    try {
        insert_value(env, list_ptr, pos, Any(value));
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_OsList_nativeSetLong(JNIEnv* env, jclass, jlong list_ptr, jlong pos,
                                                                   jlong value)
{
    try {
        set_value(env, list_ptr, pos, Any(value));
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_OsList_nativeAddDouble(JNIEnv* env, jclass, jlong list_ptr,
                                                                     jdouble value)
{
    try {
        add_value(env, list_ptr, Any(value));
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_OsList_nativeInsertDouble(JNIEnv* env, jclass, jlong list_ptr,
                                                                        jlong pos, jdouble value)
{
    try {
        insert_value(env, list_ptr, pos, Any(value));
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_OsList_nativeSetDouble(JNIEnv* env, jclass, jlong list_ptr, jlong pos,
                                                                     jdouble value)
{
    try {
        set_value(env, list_ptr, pos, Any(value));
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_OsList_nativeAddFloat(JNIEnv* env, jclass, jlong list_ptr, jfloat value)
{
    try {
        add_value(env, list_ptr, Any(value));
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_OsList_nativeInsertFloat(JNIEnv* env, jclass, jlong list_ptr, jlong pos,
                                                                       jfloat value)
{
    try {
        insert_value(env, list_ptr, pos, Any(value));
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_OsList_nativeSetFloat(JNIEnv* env, jclass, jlong list_ptr, jlong pos,
                                                                    jfloat value)
{
    try {
        set_value(env, list_ptr, pos, Any(value));
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_OsList_nativeAddBoolean(JNIEnv* env, jclass, jlong list_ptr,
                                                                      jboolean value)
{
    try {
        add_value(env, list_ptr, Any(value));
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_OsList_nativeInsertBoolean(JNIEnv* env, jclass, jlong list_ptr,
                                                                         jlong pos, jboolean value)
{
    try {
        insert_value(env, list_ptr, pos, Any(value));
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_OsList_nativeSetBoolean(JNIEnv* env, jclass, jlong list_ptr, jlong pos,
                                                                      jboolean value)
{
    try {
        set_value(env, list_ptr, pos, Any(value));
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_OsList_nativeAddBinary(JNIEnv* env, jclass, jlong list_ptr,
                                                                     jbyteArray value)
{
    try {
        check_nullable(env, list_ptr, value);
        JByteArrayAccessor accessor(env, value);
        add_value(env, list_ptr, Any(accessor));
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_OsList_nativeInsertBinary(JNIEnv* env, jclass, jlong list_ptr,
                                                                        jlong pos, jbyteArray value)
{
    try {
        check_nullable(env, list_ptr, value);
        JByteArrayAccessor accessor(env, value);
        insert_value(env, list_ptr, pos, Any(accessor));
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_OsList_nativeSetBinary(JNIEnv* env, jclass, jlong list_ptr, jlong pos,
                                                                     jbyteArray value)
{
    try {
        check_nullable(env, list_ptr, value);
        JByteArrayAccessor accessor(env, value);
        set_value(env, list_ptr, pos, Any(accessor));
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_OsList_nativeAddDate(JNIEnv* env, jclass, jlong list_ptr, jlong value)
{
    try {
        add_value(env, list_ptr, Any(value));
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_OsList_nativeInsertDate(JNIEnv* env, jclass, jlong list_ptr, jlong pos,
                                                                      jlong value)
{
    try {
        insert_value(env, list_ptr, pos, Any(value));
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_OsList_nativeSetDate(JNIEnv* env, jclass, jlong list_ptr, jlong pos,
                                                                   jlong value)
{
    try {
        set_value(env, list_ptr, pos, Any(value));
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_OsList_nativeAddString(JNIEnv* env, jclass, jlong list_ptr,
                                                                     jstring value)
{
    try {
        check_nullable(env, list_ptr, value);
        JStringAccessor accessor(env, value);
        add_value(env, list_ptr, Any(accessor));
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_OsList_nativeInsertString(JNIEnv* env, jclass, jlong list_ptr,
                                                                        jlong pos, jstring value)
{
    try {
        check_nullable(env, list_ptr, value);
        JStringAccessor accessor(env, value);
        insert_value(env, list_ptr, pos, Any(accessor));
    }
    CATCH_STD();
}

JNIEXPORT void JNICALL Java_io_realm_internal_OsList_nativeSetString(JNIEnv* env, jclass, jlong list_ptr, jlong pos,
                                                                     jstring value)
{
    try {
        check_nullable(env, list_ptr, value);
        JStringAccessor accessor(env, value);
        set_value(env, list_ptr, pos, Any(accessor));
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_OsList_nativeAddDecimal128(JNIEnv* env, jclass, jlong list_ptr,
                                                                         jlong j_low_value, jlong j_high_value)
{
    try {
          Decimal128::Bid128 raw {static_cast<uint64_t>(j_low_value), static_cast<uint64_t>(j_high_value)};
          add_value(env, list_ptr, Any(Decimal128(raw)));
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_OsList_nativeInsertDecimal128(JNIEnv* env, jclass, jlong list_ptr,
                                                                            jlong pos, jlong j_low_value, jlong j_high_value)
{
    try {
        Decimal128::Bid128 raw {static_cast<uint64_t>(j_low_value), static_cast<uint64_t>(j_high_value)};
        insert_value(env, list_ptr, pos, Any(Decimal128(raw)));
    }
    CATCH_STD();
}

JNIEXPORT void JNICALL Java_io_realm_internal_OsList_nativeSetDecimal128(JNIEnv* env, jclass, jlong list_ptr, jlong pos,
                                                                         jlong j_high_value, jlong j_low_value)
{
    try {
        Decimal128::Bid128 raw {static_cast<uint64_t>(j_low_value), static_cast<uint64_t>(j_high_value)};
        set_value(env, list_ptr, pos, Any(Decimal128(raw)));
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_OsList_nativeAddObjectId(JNIEnv* env, jclass, jlong list_ptr,
                                                                       jstring j_value)
{

    try {
        JStringAccessor value(env, j_value);
        add_value(env, list_ptr, Any(ObjectId(StringData(value).data())));
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_OsList_nativeInsertObjectId(JNIEnv* env, jclass, jlong list_ptr,
                                                                          jlong pos, jstring j_value)
{
    try {
        JStringAccessor value(env, j_value);
        insert_value(env, list_ptr, pos, Any(ObjectId(StringData(value).data())));
    }
    CATCH_STD();
}

JNIEXPORT void JNICALL Java_io_realm_internal_OsList_nativeSetObjectId(JNIEnv* env, jclass, jlong list_ptr, jlong pos,
                                                                       jstring j_value)
{
    try {
        JStringAccessor value(env, j_value);
        set_value(env, list_ptr, pos, Any(ObjectId(StringData(value).data())));
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_OsList_nativeAddUUID(JNIEnv* env, jclass, jlong list_ptr,
                                                                       jstring j_value)
{
    try {
        JStringAccessor value(env, j_value);
        add_value(env, list_ptr, Any(UUID(StringData(value).data())));
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_OsList_nativeInsertUUID(JNIEnv* env, jclass, jlong list_ptr,
                                                                          jlong pos, jstring j_value)
{
    try {
        JStringAccessor value(env, j_value);
        insert_value(env, list_ptr, pos, Any(UUID(StringData(value).data())));
    }
    CATCH_STD();
}

JNIEXPORT void JNICALL Java_io_realm_internal_OsList_nativeSetUUID(JNIEnv* env, jclass, jlong list_ptr, jlong pos,
                                                                       jstring j_value)
{
    try {
        JStringAccessor value(env, j_value);
        set_value(env, list_ptr, pos, Any(UUID(StringData(value).data())));
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_OsList_nativeAddMixed(JNIEnv* env, jclass, jlong list_ptr,
                                                                   jlong mixed_ptr)
{
    try {
        auto mixed = reinterpret_cast<Mixed*>(mixed_ptr);
        add_value(env, list_ptr, Any(*mixed));
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_OsList_nativeInsertMixed(JNIEnv* env, jclass, jlong list_ptr,
                                                                      jlong pos, jlong mixed_ptr)
{
    try {
        auto mixed = reinterpret_cast<Mixed*>(mixed_ptr);
        insert_value(env, list_ptr, pos, Any(*mixed));
    }
    CATCH_STD();
}

JNIEXPORT void JNICALL Java_io_realm_internal_OsList_nativeSetMixed(JNIEnv* env, jclass, jlong list_ptr, jlong pos,
                                                                   jlong mixed_ptr)
{
    try {
        auto mixed = reinterpret_cast<Mixed*>(mixed_ptr);
        set_value(env, list_ptr, pos, Any(*mixed));
    }
    CATCH_STD()
}

JNIEXPORT jobject JNICALL Java_io_realm_internal_OsList_nativeGetValue(JNIEnv* env, jclass, jlong list_ptr, jlong pos)
{
    try {
        auto& wrapper = *reinterpret_cast<ListWrapper*>(list_ptr);
        JavaAccessorContext context(env);
        return any_cast<jobject>(wrapper.collection().get(context, pos));
    }
    CATCH_STD()

    return nullptr;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_OsList_nativeCreateAndAddEmbeddedObject(JNIEnv* env, jclass, jlong native_list_ptr, jlong j_index)
{
    try {
        List& list = reinterpret_cast<ListWrapper*>(native_list_ptr)->collection();
        auto& realm = list.get_realm();
        auto& object_schema = list.get_object_schema();
        JavaContext ctx(env, realm, object_schema);
        // Create dummy object. Properties must be added later.
        // TODO CreatePolicy::Skip is a hack right after the object is inserted and before Schemas
        //  are validated. Figure out a better approach.
        auto array_index = static_cast<size_t>(j_index);
        list.insert(ctx, array_index, JavaValue(std::map<ColKey, JavaValue>()), CreatePolicy::Skip);
        return reinterpret_cast<jlong>(list.get(array_index).get_key().value);
    }
    CATCH_STD()
    return reinterpret_cast<jlong>(nullptr);
}


JNIEXPORT jlong JNICALL Java_io_realm_internal_OsList_nativeCreateAndSetEmbeddedObject(JNIEnv* env, jclass, jlong native_list_ptr, jlong j_index)
{
    try {
        List& list = reinterpret_cast<ListWrapper*>(native_list_ptr)->collection();
        auto& realm = list.get_realm();
        auto& object_schema = list.get_object_schema();
        JavaContext ctx(env, realm, object_schema);
        size_t array_index = static_cast<size_t>(j_index);
        // Create dummy object. Properties must be added later.
        // TODO CreatePolicy::Skip is a hack right after the object is inserted and before Schemas
        //  are validated. Figure out a better approach.
        list.set(ctx, array_index, JavaValue(std::map<ColKey, JavaValue>()), CreatePolicy::Skip);
        return reinterpret_cast<jlong>(list.get(list.size() - 1).get_key().value);
    }
    CATCH_STD()
    return reinterpret_cast<jlong>(nullptr);
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_OsList_nativeFreeze(JNIEnv* env, jclass, jlong native_list_ptr, jlong frozen_realm_native_ptr)
{
    try {
        auto& wrapper = *reinterpret_cast<ListWrapper*>(native_list_ptr);
        auto frozen_realm = *(reinterpret_cast<SharedRealm*>(frozen_realm_native_ptr));
        List list = wrapper.collection().freeze(frozen_realm);
        return reinterpret_cast<jlong>(new ListWrapper(list));
    }
    CATCH_STD()
    return reinterpret_cast<jlong>(nullptr);
}

