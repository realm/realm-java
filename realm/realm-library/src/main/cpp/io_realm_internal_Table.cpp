/*
 * Copyright 2014 Realm Inc.
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

#include <sstream>

#include "util.hpp"
#include "io_realm_internal_Property.h"
#include "io_realm_internal_Table.h"

#include "java_accessor.hpp"
#include "java_exception_def.hpp"
#include <realm/object-store/shared_realm.hpp>
#include "jni_util/java_exception_thrower.hpp"

#include <realm/util/to_string.hpp>

using namespace std;
using namespace realm;
using namespace realm::_impl;
using namespace realm::jni_util;
using namespace realm::util;

static_assert(io_realm_internal_Table_MAX_STRING_SIZE == Table::max_string_size, "");
static_assert(io_realm_internal_Table_MAX_BINARY_SIZE == Table::max_binary_size, "");

static void finalize_table(jlong ptr);

inline static bool is_allowed_to_index(JNIEnv* env, DataType column_type)
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

    ThrowException(env, IllegalArgument, "This field cannot be indexed - "
                                         "Only String/byte/short/int/long/boolean/Date/ObjectId fields are supported.");
    return false;
}

// Note: Don't modify spec on a table which has a shared_spec.
// A spec is shared on subtables that are not in Mixed columns.
//

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeAddColumn(JNIEnv* env, jobject, jlong nativeTableRefPtr,
                                                                     jint colType, jstring name, jboolean isNullable)
{
    try {
        JStringAccessor name2(env, name); // throws
        bool is_column_nullable = to_bool(isNullable);
        DataType dataType = DataType(colType);
        if (is_column_nullable && dataType == type_LinkList) {
            ThrowException(env, IllegalArgument, "List fields cannot be nullable.");
        }
        TableRef table = TBL_REF(nativeTableRefPtr);
        ColKey col_key = table->add_column(dataType, name2, is_column_nullable);
        return (jlong)(col_key.value);
    }
    CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeAddPrimitiveListColumn(JNIEnv* env, jobject,
                                                                                  jlong native_table_ptr, jint j_col_type,
                                                                                  jstring j_name, jboolean j_is_nullable)
{
    try {
        JStringAccessor name(env, j_name); // throws
        bool is_column_nullable = to_bool(j_is_nullable);
        DataType data_type = DataType(j_col_type);
        TableRef table = TBL_REF(native_table_ptr);
        return (jlong)(table->add_column_list(data_type, name, is_column_nullable).value);
    }
    CATCH_STD()
    return reinterpret_cast<jlong>(nullptr);
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeAddColumnLink(JNIEnv* env, jobject, jlong nativeTableRefPtr,
                                                                         jint colType, jstring name,
                                                                         jlong targetTableRefPtr)
{
    TableRef targetTableRef = TBL_REF(targetTableRefPtr);
    if (!targetTableRef->is_group_level()) {
        ThrowException(env, UnsupportedOperation, "Links can only be made to toplevel tables.");
        return 0;
    }
    try {
        JStringAccessor name_accessor(env, name); // throws
        TableRef table = TBL_REF(nativeTableRefPtr);
        auto data_type = DataType(colType);

        if (REALM_UNLIKELY(!Table::is_link_type(ColumnType(data_type))))
            throw LogicError(LogicError::illegal_type);

        if (data_type == type_LinkList) {
            return static_cast<jlong>(table->add_column_list(*targetTableRef, name_accessor).value);
        }
        else {
            REALM_ASSERT(data_type == type_Link);
            return static_cast<jlong>(table->add_column(*targetTableRef, name_accessor).value);
        }
    }
    CATCH_STD()
    return 0;
}


JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeRemoveColumn(JNIEnv* env, jobject, jlong nativeTableRefPtr,
                                                                       jlong columnKey)
{
    try {
        TableRef table = TBL_REF(nativeTableRefPtr);
        table->remove_column(ColKey(columnKey));
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeRenameColumn(JNIEnv* env, jobject, jlong nativeTableRefPtr,
                                                                       jlong columnKey, jstring name)
{
    try {
        JStringAccessor name2(env, name); // throws
        TableRef table = TBL_REF(nativeTableRefPtr);
        table->rename_column(ColKey(columnKey), name2);
    }
    CATCH_STD()
}

JNIEXPORT jboolean JNICALL Java_io_realm_internal_Table_nativeIsColumnNullable(JNIEnv*, jobject,
                                                                               jlong nativeTableRefPtr,
                                                                               jlong columnKey)
{
    TableRef table = TBL_REF(nativeTableRefPtr);
    return to_jbool(table->is_nullable(ColKey(columnKey))); // noexcept
}

JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeConvertColumnToNullable(JNIEnv* env, jobject,
                                                                                  jlong native_table_ref_ptr,
                                                                                  jlong j_column_key,
                                                                                  jboolean is_primary_key)
{
    try {
        TableRef table = TBL_REF(native_table_ref_ptr);
        ColKey col_key(j_column_key);
        bool nullable = true;
        bool throw_on_value_conversion = false;
        ColKey newCol = table->set_nullability(col_key, nullable, throw_on_value_conversion);
        if (to_bool(is_primary_key)) {
            table->set_primary_key_column(newCol);
        }

    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeConvertColumnToNotNullable(JNIEnv* env, jobject,
                                                                                     jlong native_table_ref_ptr,
                                                                                     jlong j_column_key,
                                                                                     jboolean is_primary_key)
{
    try {
        TableRef table = TBL_REF(native_table_ref_ptr);
        ColKey col_key(j_column_key);
        bool nullable = false;
        bool throw_on_value_conversion = is_primary_key;
        ColKey newCol = table->set_nullability(col_key, nullable, throw_on_value_conversion);
        if (to_bool(is_primary_key)) {
            table->set_primary_key_column(newCol);
        }
    }
    CATCH_STD()
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeSize(JNIEnv*, jobject, jlong nativeTableRefPtr)
{
    TableRef table = TBL_REF(nativeTableRefPtr);
    return static_cast<jlong>(table->size()); // noexcept
}

JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeClear(JNIEnv* env, jobject, jlong nativeTableRefPtr)
{
    try {
        TableRef table = TBL_REF(nativeTableRefPtr);
        table->clear();
    }
    CATCH_STD()
}


// -------------- Column information


JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeGetColumnCount(JNIEnv*, jobject, jlong nativeTableRefPtr)
{
    TableRef table = TBL_REF(nativeTableRefPtr);
    return static_cast<jlong>(table->get_column_count()); // noexcept
}

JNIEXPORT jstring JNICALL Java_io_realm_internal_Table_nativeGetColumnName(JNIEnv* env, jobject, jlong nativeTableRefPtr,
                                                                           jlong columnKey)
{
    try {
        TableRef table = TBL_REF(nativeTableRefPtr);
        ColKey col_key(columnKey);
        StringData stringData = table->get_column_name(col_key);
        return to_jstring(env, stringData);
    }
    CATCH_STD();
    return nullptr;
}

JNIEXPORT jobjectArray JNICALL Java_io_realm_internal_Table_nativeGetColumnNames(JNIEnv* env, jobject, jlong nativeTableRefPtr)
{
    try {
        TableRef table = TBL_REF(nativeTableRefPtr);
        ColKeys col_keys = table->get_column_keys();
        size_t size = col_keys.size();
        jobjectArray col_keys_array = env->NewObjectArray(size, JavaClassGlobalDef::java_lang_string(), 0);
        if (col_keys_array == NULL) {
            ThrowException(env, OutOfMemory, "Could not allocate memory to return column names.");
            return NULL;
        }
        for (size_t i = 0; i < size; ++i) {
            env->SetObjectArrayElement(col_keys_array, i, to_jstring(env,  table->get_column_name(col_keys[i])));
        }

        return col_keys_array;
    }
    CATCH_STD();
    return NULL;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeGetColumnKey(JNIEnv* env, jobject, jlong nativeTableRefPtr,
                                                                          jstring columnName)
{
    try {
        JStringAccessor columnName2(env, columnName);                                     // throws
        TableRef table = TBL_REF(nativeTableRefPtr);
        ColKey col_key = table->get_column_key(columnName2);
        if (table->valid_column(col_key)) {
            return col_key.value;
        }
        return -1;
    }
    CATCH_STD()
    return -1;
}

JNIEXPORT jint JNICALL Java_io_realm_internal_Table_nativeGetColumnType(JNIEnv*, jobject, jlong nativeTableRefPtr,
                                                                        jlong columnKey)
{
    ColKey column_key (columnKey);
    TableRef table = TBL_REF(nativeTableRefPtr);
    DataType column_type = table->get_column_type(column_key);
    if (column_type != type_LinkList &&  table->is_list(column_key)) {
        // add the offset so it can be mapped correctly in Java (RealmFieldType#fromNativeValue)
        return int(column_type) + int(PropertyType::Array);
    }

    return int(column_type);
    // For primitive list
    // FIXME: Add test in https://github.com/realm/realm-java/pull/5221 before merging to master
    // FIXME: Add method in Object Store to return a PropertyType.
}


// ---------------- Row handling

JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeMoveLastOver(JNIEnv* env, jobject, jlong nativeTableRefPtr,
                                                                       jlong rowKey)
{
    try {
        TableRef table = TBL_REF(nativeTableRefPtr);
        table->remove_object(ObjKey(rowKey));
    }
    CATCH_STD()
}

// ----------------- Get cell

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeGetLong(JNIEnv* env, jobject, jlong nativeTableRefPtr,
                                                                   jlong columnKey, jlong rowKey)
{
    TableRef table = TBL_REF(nativeTableRefPtr);
    if (!TYPE_VALID(env, table, columnKey, col_type_Int)) {
        return 0;
    }
    return table->get_object(ObjKey(rowKey)).get<Int>(ColKey(columnKey)); // noexcept
}

JNIEXPORT jboolean JNICALL Java_io_realm_internal_Table_nativeGetBoolean(JNIEnv* env, jobject, jlong nativeTableRefPtr,
                                                                         jlong columnKey, jlong rowKey)
{
    TableRef table = TBL_REF(nativeTableRefPtr);
    if (!TYPE_VALID(env, table, columnKey, col_type_Bool)) {
        return JNI_FALSE;
    }

    return to_jbool(table->get_object(ObjKey(rowKey)).get<bool>(ColKey(columnKey)));
}

JNIEXPORT jfloat JNICALL Java_io_realm_internal_Table_nativeGetFloat(JNIEnv* env, jobject, jlong nativeTableRefPtr,
                                                                     jlong columnKey, jlong rowKey)
{
    TableRef table = TBL_REF(nativeTableRefPtr);
    if (!TYPE_VALID(env, table, columnKey, col_type_Float)) {
        return 0;
    }

    return table->get_object(ObjKey(rowKey)).get<float>(ColKey(columnKey));
}

JNIEXPORT jdouble JNICALL Java_io_realm_internal_Table_nativeGetDouble(JNIEnv* env, jobject, jlong nativeTableRefPtr,
                                                                       jlong columnKey, jlong rowKey)
{
    TableRef table = TBL_REF(nativeTableRefPtr);
    if (!TYPE_VALID(env, table, columnKey, col_type_Double)) {
        return 0;
    }

    return table->get_object(ObjKey(rowKey)).get<double>(ColKey(columnKey));
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeGetTimestamp(JNIEnv* env, jobject, jlong nativeTableRefPtr,
                                                                        jlong columnKey, jlong rowKey)
{
    TableRef table = TBL_REF(nativeTableRefPtr);
    if (!TYPE_VALID(env, table, columnKey, col_type_Timestamp)) {
        return 0;
    }
    try {
        return to_milliseconds(table->get_object(ObjKey(rowKey)).get<Timestamp>(ColKey(columnKey)));
    }
    CATCH_STD()
    return 0;
}

JNIEXPORT jstring JNICALL Java_io_realm_internal_Table_nativeGetString(JNIEnv* env, jobject, jlong nativeTableRefPtr,
                                                                       jlong columnKey, jlong rowKey)
{
    TableRef table = TBL_REF(nativeTableRefPtr);
    if (!TYPE_VALID(env, table, columnKey, col_type_String)) {
        return nullptr;
    }
    try {
        return to_jstring(env, table->get_object(ObjKey(rowKey)).get<StringData>(ColKey(columnKey)));
    }
    CATCH_STD()
    return nullptr;
}

JNIEXPORT jlongArray JNICALL Java_io_realm_internal_Table_nativeGetDecimal128(JNIEnv* env, jobject, jlong nativeTableRefPtr,
                                                                       jlong columnKey, jlong rowKey)
{
    TableRef table = TBL_REF(nativeTableRefPtr);
    if (!TYPE_VALID(env, table, columnKey, col_type_Decimal)) {
        return nullptr;
    }
    try {
        Decimal128 decimal128 = table->get_object(ObjKey(rowKey)).get<Decimal128>(ColKey(columnKey));
        RETURN_DECIMAL128_AS_JLONG_ARRAY__OR_NULL(decimal128)
    }
    CATCH_STD()
    return nullptr;
}

JNIEXPORT jstring JNICALL Java_io_realm_internal_Table_nativeGetObjectId(JNIEnv* env, jobject, jlong nativeTableRefPtr,
                                                                       jlong columnKey, jlong rowKey)
{
    TableRef table = TBL_REF(nativeTableRefPtr);
    if (!TYPE_VALID(env, table, columnKey, col_type_ObjectId)) {
        return nullptr;
    }
    try {
        return to_jstring(env, table->get_object(ObjKey(rowKey)).get<ObjectId>(ColKey(columnKey)).to_string().data());
    }
    CATCH_STD()
    return nullptr;
}

JNIEXPORT jbyteArray JNICALL Java_io_realm_internal_Table_nativeGetByteArray(JNIEnv* env, jobject,
                                                                             jlong nativeTableRefPtr, jlong columnKey,
                                                                             jlong rowKey)
{
    TableRef table = TBL_REF(nativeTableRefPtr);
    if (!TYPE_VALID(env, table, columnKey, col_type_Binary)) {
        return nullptr;
    }
    try {
        realm::BinaryData bin = table->get_object(ObjKey(rowKey)).get<BinaryData>(ColKey(columnKey));
        return JavaClassGlobalDef::new_byte_array(env, bin);
    }
    CATCH_STD()

    return nullptr;
}
JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeGetLink(JNIEnv* env, jobject, jlong nativeTableRefPtr,
                                                                   jlong columnKey, jlong rowKey)
{
    TableRef table = TBL_REF(nativeTableRefPtr);
    if (!TYPE_VALID(env, table, columnKey, col_type_Link)) {
        return 0;
    }
    return static_cast<jlong>(table->get_object(ObjKey(rowKey)).get<ObjKey>(ColKey(columnKey)).value); // noexcept
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeGetLinkTarget(JNIEnv* env, jobject, jlong nativeTableRefPtr,
                                                                         jlong columnKey)
{
    try {
        TableRef table_ref = TBL_REF(nativeTableRefPtr);
        return reinterpret_cast<jlong>(new TableRef(table_ref->get_link_target(ColKey(columnKey))));
    }
    CATCH_STD()
    return 0;
}

JNIEXPORT jboolean JNICALL Java_io_realm_internal_Table_nativeIsNull(JNIEnv*, jobject, jlong nativeTableRefPtr,
                                                                     jlong columnKey, jlong rowKey)
{
    TableRef table = TBL_REF(nativeTableRefPtr);
    return to_jbool(table->get_object(ObjKey(rowKey)).is_null(ColKey(columnKey))); // noexcept
}

// ----------------- Set cell

JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeSetLink(JNIEnv* env, jclass, jlong nativeTableRefPtr,
                                                                  jlong columnKey, jlong rowKey,
                                                                  jlong targetRowKey, jboolean isDefault)
{
    TableRef table = TBL_REF(nativeTableRefPtr);
    if (!TYPE_VALID(env, table, columnKey, col_type_Link)) {
        return;
    }
    try {
        table->get_object(ObjKey(rowKey)).set(ColKey(columnKey), ObjKey(targetRowKey), B(isDefault));
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeSetLong(JNIEnv* env, jclass, jlong nativeTableRefPtr,
                                                                  jlong columnKey, jlong rowKey, jlong value,
                                                                  jboolean isDefault)
{
    TableRef table = TBL_REF(nativeTableRefPtr);
    if (!TYPE_VALID(env, table, columnKey, col_type_Int)) {
        return;
    }
    try {
        table->get_object(ObjKey(rowKey)).set<int64_t>(ColKey(columnKey), value, B(isDefault));
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeIncrementLong(JNIEnv* env, jclass, jlong nativeTableRefPtr,
                                                                  jlong columnKey, jlong rowKey, jlong value)
{

    TableRef table = TBL_REF(nativeTableRefPtr);
    if (!TYPE_VALID(env, table, columnKey, col_type_Int)) {
        return;
    }

    try {
        auto obj = table->get_object(ObjKey(rowKey));
        if (obj.is_null(ColKey(columnKey))) {
            THROW_JAVA_EXCEPTION(env, JavaExceptionDef::IllegalState,
                                 "Cannot increment a MutableRealmInteger whose value is null. Set its value first.");
        }

        obj.add_int(ColKey(columnKey), value);
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeSetBoolean(JNIEnv* env, jclass, jlong nativeTableRefPtr,
                                                                     jlong columnKey, jlong rowKey,
                                                                     jboolean value, jboolean isDefault)
{
    TableRef table = TBL_REF(nativeTableRefPtr);
    if (!TYPE_VALID(env, table, columnKey, col_type_Bool)) {
        return;
    }
    try {
        table->get_object(ObjKey(rowKey)).set(ColKey(columnKey), B(value), B(isDefault));
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeSetFloat(JNIEnv* env, jclass, jlong nativeTableRefPtr,
                                                                   jlong columnKey, jlong rowKey, jfloat value,
                                                                   jboolean isDefault)
{
    TableRef table = TBL_REF(nativeTableRefPtr);
    if (!TYPE_VALID(env, table, columnKey, col_type_Float)) {
        return;
    }
    try {
        table->get_object(ObjKey(rowKey)).set(ColKey(columnKey), value, B(isDefault));
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeSetDouble(JNIEnv* env, jclass, jlong nativeTableRefPtr,
                                                                    jlong columnKey, jlong rowKey, jdouble value,
                                                                    jboolean isDefault)
{
    TableRef table = TBL_REF(nativeTableRefPtr);
    if (!TYPE_VALID(env, table, columnKey, col_type_Double)) {
        return;
    }
    try {
        table->get_object(ObjKey(rowKey)).set(ColKey(columnKey), value, B(isDefault));
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeSetString(JNIEnv* env, jclass, jlong nativeTableRefPtr,
                                                                    jlong columnKey, jlong rowKey, jstring value,
                                                                    jboolean isDefault)
{
    TableRef table = TBL_REF(nativeTableRefPtr);
    if (!TYPE_VALID(env, table, columnKey, col_type_String)) {
        return;
    }
    try {
        if (value == nullptr) {
            if (!COL_NULLABLE(env, table, columnKey)) {
                return;
            }
        }
        JStringAccessor value2(env, value); // throws
        table->get_object(ObjKey(rowKey)).set(ColKey(columnKey), StringData(value2), B(isDefault));
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeSetTimestamp(JNIEnv* env, jclass, jlong nativeTableRefPtr,
                                                                       jlong columnKey, jlong rowKey,
                                                                       jlong timestampValue, jboolean isDefault)
{
    TableRef table = TBL_REF(nativeTableRefPtr);
    if (!TYPE_VALID(env, table, columnKey, col_type_Timestamp)) {
        return;
    }
    try {
        table->get_object(ObjKey(rowKey)).set(ColKey(columnKey), from_milliseconds(timestampValue), B(isDefault));
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeSetByteArray(JNIEnv* env, jclass, jlong nativeTableRefPtr,
                                                                       jlong columnKey, jlong rowKey,
                                                                       jbyteArray dataArray, jboolean isDefault)
{
    TableRef table = TBL_REF(nativeTableRefPtr);
    if (!TYPE_VALID(env, table, columnKey, col_type_Binary)) {
        return;
    }
    try {
        if (dataArray == nullptr && !COL_NULLABLE(env, table, columnKey)) {
            return;
        }

        JByteArrayAccessor jarray_accessor(env, dataArray);
        table->get_object(ObjKey(rowKey)).set(ColKey(columnKey), jarray_accessor.transform<BinaryData>(), B(isDefault));
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeSetDecimal128(JNIEnv* env, jclass, jlong nativeTableRefPtr,
                                                                    jlong columnKey, jlong rowKey, jlong low,
                                                                    jlong high, jboolean isDefault)
{
    TableRef table = TBL_REF(nativeTableRefPtr);
    if (!TYPE_VALID(env, table, columnKey, col_type_Decimal)) {
        return;
    }
    try {
        Decimal128::Bid128 raw {static_cast<uint64_t>(low), static_cast<uint64_t>(high)};
        table->get_object(ObjKey(rowKey)).set(ColKey(columnKey), Decimal128(raw), B(isDefault));
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeSetObjectId(JNIEnv* env, jclass, jlong nativeTableRefPtr,
                                                                      jlong columnKey, jlong rowKey, jstring j_value,
                                                                      jboolean isDefault)
{
    TableRef table = TBL_REF(nativeTableRefPtr);
    if (!TYPE_VALID(env, table, columnKey, col_type_ObjectId)) {
        return;
    }
    try {
        JStringAccessor value(env, j_value);
        table->get_object(ObjKey(rowKey)).set(ColKey(columnKey), ObjectId(StringData(value).data()), B(isDefault));
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeSetUUID(JNIEnv* env, jclass, jlong nativeTableRefPtr,
                                                                      jlong columnKey, jlong rowKey, jstring j_value,
                                                                      jboolean isDefault)
{
    TableRef table = TBL_REF(nativeTableRefPtr);
    if (!TYPE_VALID(env, table, columnKey, col_type_UUID)) {
        return;
    }
    try {
        JStringAccessor value(env, j_value);
        table->get_object(ObjKey(rowKey)).set(ColKey(columnKey), UUID(StringData(value).data()), B(isDefault));
    }
    CATCH_STD()
}


JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeSetNull(JNIEnv* env, jclass, jlong nativeTableRefPtr,
                                                                  jlong columnKey, jlong rowKey,
                                                                  jboolean isDefault)
{
    TableRef table = TBL_REF(nativeTableRefPtr);
    if (!COL_NULLABLE(env, table, columnKey)) {
        return;
    }

    try {
        table->get_object(ObjKey(rowKey)).set_null(ColKey(columnKey), B(isDefault));
    }
    CATCH_STD()
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeGetRowPtr(JNIEnv* env, jobject, jlong nativeTableRefPtr,
                                                                     jlong key)
{
    try {
        TableRef table = TBL_REF(nativeTableRefPtr);
        Obj* obj = new Obj(table->get_object(ObjKey(key)));
        return reinterpret_cast<jlong>(obj);
    }
    CATCH_STD()
    return reinterpret_cast<jlong>(nullptr);
}

//--------------------- Indexing methods:

JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeAddSearchIndex(JNIEnv* env, jobject, jlong nativeTableRefPtr,
                                                                         jlong columnKey)
{
    TableRef table = TBL_REF(nativeTableRefPtr);
    ColKey colKey(columnKey);
    DataType column_type = table->get_column_type(colKey);
    if (!is_allowed_to_index(env, column_type)) {
        return;
    }

    try {
        table->add_search_index(colKey);
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeRemoveSearchIndex(JNIEnv* env, jobject,
                                                                            jlong nativeTableRefPtr, jlong columnKey)
{
    TableRef table = TBL_REF(nativeTableRefPtr);
    DataType column_type = table->get_column_type(ColKey(columnKey));
    if (!is_allowed_to_index(env, column_type)) {
        return;
    }
    try {
        table->remove_search_index(ColKey(columnKey));
    }
    CATCH_STD()
}


JNIEXPORT jboolean JNICALL Java_io_realm_internal_Table_nativeHasSearchIndex(JNIEnv* env, jobject,
                                                                             jlong nativeTableRefPtr, jlong columnKey)
{
    try {
        TableRef table = TBL_REF(nativeTableRefPtr);
        return to_jbool(table->has_search_index(ColKey(columnKey)));
    }
    CATCH_STD()
    return JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_io_realm_internal_Table_nativeIsNullLink(JNIEnv* env, jobject, jlong nativeTableRefPtr,
                                                                         jlong columnKey, jlong rowKey)
{
    TableRef table = TBL_REF(nativeTableRefPtr);
    if (!TYPE_VALID(env, table, columnKey, col_type_Link)) {
        return JNI_FALSE;
    }

    return to_jbool(table->get_object(ObjKey(rowKey)).is_null(ColKey(columnKey)));
}

JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeNullifyLink(JNIEnv* env, jclass, jlong nativeTableRefPtr,
                                                                      jlong columnKey, jlong rowKey)
{
    TableRef table = TBL_REF(nativeTableRefPtr);
    if (!TYPE_VALID(env, table, columnKey, col_type_Link)) {
        return;
    }
    try {
        table->get_object(ObjKey(rowKey)).set_null(ColKey(columnKey));
    }
    CATCH_STD()
}

//---------------------- Count

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeCountLong(JNIEnv* env, jobject, jlong nativeTableRefPtr,
                                                                     jlong columnKey, jlong value)
{
    TableRef table = TBL_REF(nativeTableRefPtr);
    if (!TYPE_VALID(env, table, columnKey, col_type_Int)) {
        return 0;
    }
    try {
        return static_cast<jlong>(table->count_int(ColKey(columnKey), value));
    }
    CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeCountFloat(JNIEnv* env, jobject, jlong nativeTableRefPtr,
                                                                      jlong columnKey, jfloat value)
{
    TableRef table = TBL_REF(nativeTableRefPtr);
    if (!TYPE_VALID(env, table, columnKey, col_type_Float)) {
        return 0;
    }
    try {
        return static_cast<jlong>(table->count_float(ColKey(columnKey), value));
    }
    CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeCountDouble(JNIEnv* env, jobject, jlong nativeTableRefPtr,
                                                                       jlong columnKey, jdouble value)
{
    TableRef table = TBL_REF(nativeTableRefPtr);
    if (!TYPE_VALID(env, table, columnKey, col_type_Double)) {
        return 0;
    }
    try {
        return static_cast<jlong>(table->count_double(ColKey(columnKey), value));
    }
    CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeCountString(JNIEnv* env, jobject, jlong nativeTableRefPtr,
                                                                       jlong columnKey, jstring value)
{
    TableRef table = TBL_REF(nativeTableRefPtr);
    if (!TYPE_VALID(env, table, columnKey, col_type_String)) {
        return 0;
    }
    try {
        JStringAccessor value2(env, value); // throws
        return static_cast<jlong>(table->count_string(ColKey(columnKey), value2));
    }
    CATCH_STD()
    return 0;
}


JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeWhere(JNIEnv* env, jobject, jlong nativeTableRefPtr)
{
    try {
        TableRef table = TBL_REF(nativeTableRefPtr);
        Query* queryPtr = new Query(table->where());
        return reinterpret_cast<jlong>(queryPtr);
    }
    CATCH_STD()
    return reinterpret_cast<jlong>(nullptr);
}

//----------------------- FindFirst

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeFindFirstInt(JNIEnv* env, jclass, jlong nativeTableRefPtr,
                                                                        jlong columnKey, jlong value)
{
    TableRef table = TBL_REF(nativeTableRefPtr);
    if (!TYPE_VALID(env, table, columnKey, col_type_Int)) {
        return -1;
    }
    try {
        return to_jlong_or_not_found(table->find_first_int(ColKey(columnKey), value));
    }
    CATCH_STD()
    return -1;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeFindFirstBool(JNIEnv* env, jobject, jlong nativeTableRefPtr,
                                                                         jlong columnKey, jboolean value)
{
    TableRef table = TBL_REF(nativeTableRefPtr);
    if (!TYPE_VALID(env, table, columnKey, col_type_Bool)) {
        return -1;
    }
    try {
        return to_jlong_or_not_found(table->find_first_bool(ColKey(columnKey), to_bool(value)));
    }
    CATCH_STD()
    return -1;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeFindFirstFloat(JNIEnv* env, jobject, jlong nativeTableRefPtr,
                                                                          jlong columnKey, jfloat value)
{
    TableRef table = TBL_REF(nativeTableRefPtr);
    if (!TYPE_VALID(env, table, columnKey, col_type_Float)) {
        return -1;
    }
    try {
        return to_jlong_or_not_found(table->find_first_float(ColKey(columnKey), value));
    }
    CATCH_STD()
    return -1;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeFindFirstDouble(JNIEnv* env, jobject, jlong nativeTableRefPtr,
                                                                           jlong columnKey, jdouble value)
{
    TableRef table = TBL_REF(nativeTableRefPtr);
    if (!TYPE_VALID(env, table, columnKey, col_type_Double)) {
        return -1;
    }
    try {
        return to_jlong_or_not_found(table->find_first_double(ColKey(columnKey), value));
    }
    CATCH_STD()
    return -1;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeFindFirstTimestamp(JNIEnv* env, jobject,
                                                                              jlong nativeTableRefPtr, jlong columnKey,
                                                                              jlong dateTimeValue)
{
    TableRef table = TBL_REF(nativeTableRefPtr);
    if (!TYPE_VALID(env, table, columnKey, col_type_Timestamp)) {
        return -1;
    }
    try {
        return to_jlong_or_not_found(table->find_first_timestamp(ColKey(columnKey), from_milliseconds(dateTimeValue)));
    }
    CATCH_STD()
    return -1;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeFindFirstString(JNIEnv* env, jclass, jlong nativeTableRefPtr,
                                                                           jlong columnKey, jstring value)
{
    TableRef table = TBL_REF(nativeTableRefPtr);
    if (!TYPE_VALID(env, table, columnKey, col_type_String)) {
        return -1;
    }

    try {
        JStringAccessor value2(env, value); // throws
        return to_jlong_or_not_found(table->find_first_string(ColKey(columnKey), value2));
    }
    CATCH_STD()
    return -1;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeFindFirstDecimal128(JNIEnv* env, jclass, jlong nativeTableRefPtr,
                                                                             jlong columnKey, jlong low, jlong high)
{
    TableRef table = TBL_REF(nativeTableRefPtr);
    if (!TYPE_VALID(env, table, columnKey, col_type_Decimal)) {
        return -1;
    }

    try {
        Decimal128::Bid128 raw {static_cast<uint64_t>(low), static_cast<uint64_t>(high)};
        return to_jlong_or_not_found(table->find_first_decimal(ColKey(columnKey), Decimal128(raw)));
    }
    CATCH_STD()
    return -1;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeFindFirstObjectId(JNIEnv* env, jclass, jlong nativeTableRefPtr,
                                                                             jlong columnKey, jstring j_value)
{
    TableRef table = TBL_REF(nativeTableRefPtr);
    if (!TYPE_VALID(env, table, columnKey, col_type_ObjectId)) {
        return -1;
    }

    try {
        JStringAccessor value(env, j_value); // throws
        ObjectId id = ObjectId(StringData(value).data());
        return to_jlong_or_not_found(table->find_first_object_id(ColKey(columnKey), id));
    }
    CATCH_STD()
    return -1;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeFindFirstUUID(JNIEnv* env, jclass, jlong nativeTableRefPtr,
                                                                             jlong columnKey, jstring j_value)
{
    TableRef table = TBL_REF(nativeTableRefPtr);
    if (!TYPE_VALID(env, table, columnKey, col_type_UUID)) {
        return -1;
    }

    try {
        JStringAccessor value(env, j_value); // throws
        UUID uuid = UUID(StringData(value).data());
        return to_jlong_or_not_found(table->find_first_uuid(ColKey(columnKey), uuid));
    }
    CATCH_STD()
    return -1;
}


JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeFindFirstNull(JNIEnv* env, jclass, jlong nativeTableRefPtr,
                                                                         jlong columnKey)
{
    TableRef table = TBL_REF(nativeTableRefPtr);
    if (!COL_NULLABLE(env, table, columnKey)) {
        return static_cast<jlong>(realm::not_found);
    }
    try {
        return to_jlong_or_not_found(table->find_first_null(ColKey(columnKey)));
    }
    CATCH_STD()
    return static_cast<jlong>(realm::not_found);
}

// FindAll

//

JNIEXPORT jstring JNICALL Java_io_realm_internal_Table_nativeGetName(JNIEnv* env, jobject, jlong nativeTableRefPtr)
{
    try {
        TableRef table = TBL_REF(nativeTableRefPtr);
        // Mirror API in Java for now. Before Core 6 this would return null for tables not attached to the group.
        if (table) {
            return to_jstring(env, table->get_name());
        } else {
            return nullptr;
        }
    }
    CATCH_STD()
    return nullptr;
}

JNIEXPORT jboolean JNICALL Java_io_realm_internal_Table_nativeIsValid(JNIEnv*, jobject, jlong nativeTableRefPtr)
{
    if(TBL_REF(nativeTableRefPtr)) {
        return JNI_TRUE;
    } else {
        return JNI_FALSE;
    }
}

JNIEXPORT jboolean JNICALL Java_io_realm_internal_Table_nativeHasSameSchema(JNIEnv*, jobject, jlong thisTableRefPtr,
                                                                            jlong otherTableRefPtr)
{
    TableRef this_table = TBL_REF(thisTableRefPtr);
    TableRef other_table = TBL_REF(otherTableRefPtr);
    return to_jbool(this_table->get_key() == other_table->get_key());
}

static void finalize_table(jlong ptr)
{
    delete reinterpret_cast<realm::TableRef*>(ptr);
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeGetFinalizerPtr(JNIEnv*, jclass)
{
    return reinterpret_cast<jlong>(&finalize_table);
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeFreeze(JNIEnv*, jclass, jlong j_frozen_shared_realm_ptr, jlong j_table_ptr)
{
    auto& shared_realm = *(reinterpret_cast<SharedRealm*>(j_frozen_shared_realm_ptr));
    TableRef table = TableRef(TBL_REF(j_table_ptr));
    TableRef* frozen_table = new TableRef(shared_realm->import_copy_of(table));
    return reinterpret_cast<jlong>(frozen_table);
}

JNIEXPORT jboolean JNICALL Java_io_realm_internal_Table_nativeIsEmbedded(JNIEnv* env, jclass, jlong j_table_ptr)
{
    try {
        TableRef table = TableRef(TBL_REF(j_table_ptr));
        return to_jbool(table->is_embedded());
    }
    CATCH_STD()
    return false;
}

JNIEXPORT jboolean JNICALL Java_io_realm_internal_Table_nativeSetEmbedded(JNIEnv* env, jclass, jlong j_table_ptr, jboolean j_embedded)
{
    try {
        TableRef table = TableRef(TBL_REF(j_table_ptr));
        return to_jbool(table->set_embedded(to_bool(j_embedded)));
    }
    CATCH_STD()
    return false;
}
