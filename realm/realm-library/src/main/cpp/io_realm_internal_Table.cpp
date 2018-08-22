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
#include "shared_realm.hpp"
#include "jni_util/java_exception_thrower.hpp"

#include <realm/util/to_string.hpp>

using namespace std;
using namespace realm;
using namespace realm::_impl;
using namespace realm::jni_util;
using namespace realm::util;

static_assert(io_realm_internal_Table_MAX_STRING_SIZE == Table::max_string_size, "");
static_assert(io_realm_internal_Table_MAX_BINARY_SIZE == Table::max_binary_size, "");

static const char* c_null_values_cannot_set_required_msg = "The primary key field '%1' has 'null' values stored.  It "
                                                           "cannot be converted to a '@Required' primary key field.";
static const char* const PK_TABLE_NAME = "pk"; // ObjectStore::c_primaryKeyTableName
static const size_t CLASS_COLUMN_INDEX = 0; // ObjectStore::c_primaryKeyObjectClassColumnIndex
static const size_t FIELD_COLUMN_INDEX = 1; // ObjectStore::c_primaryKeyPropertyNameColumnIndex


static void finalize_table(jlong ptr);

inline static bool is_allowed_to_index(JNIEnv* env, DataType column_type)
{
    if (!(column_type == type_String || column_type == type_Int || column_type == type_Bool ||
          column_type == type_Timestamp || column_type == type_OldDateTime)) {
        ThrowException(env, IllegalArgument, "This field cannot be indexed - "
                                             "Only String/byte/short/int/long/boolean/Date fields are supported.");
        return false;
    }
    return true;
}

// Note: Don't modify spec on a table which has a shared_spec.
// A spec is shared on subtables that are not in Mixed columns.
//

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeAddColumn(JNIEnv* env, jobject, jlong nativeTablePtr,
                                                                     jint colType, jstring name, jboolean isNullable)
{
    try {
        JStringAccessor name2(env, name); // throws
        bool is_column_nullable = to_bool(isNullable);
        DataType dataType = DataType(colType);
        if (is_column_nullable && dataType == type_LinkList) {
            ThrowException(env, IllegalArgument, "List fields cannot be nullable.");
        }
        TableRef table = TBL_REF(nativeTablePtr);
        ColKey col_key = table->add_column(dataType, name2, is_column_nullable);
        return reinterpret_cast<jlong>(col_key.value);
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
        return reinterpret_cast<jlong>(table->add_column_list(data_type, name, is_column_nullable).value);
    }
    CATCH_STD()
    return reinterpret_cast<jlong>(nullptr);
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeAddColumnLink(JNIEnv* env, jobject, jlong nativeTablePtr,
                                                                         jint colType, jstring name,
                                                                         jlong targetTablePtr)
{
    TableRef targetTableRef = TBL_REF(targetTablePtr);
    if (!targetTableRef->is_group_level()) {
        ThrowException(env, UnsupportedOperation, "Links can only be made to toplevel tables.");
        return 0;
    }
    try {
        JStringAccessor name2(env, name); // throws
        TableRef table = TBL_REF(nativeTablePtr);
        return static_cast<jlong>(table->add_column_link(DataType(colType), name2, *targetTableRef).value);
    }
    CATCH_STD()
    return 0;
}


JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeRemoveColumn(JNIEnv* env, jobject, jlong nativeTablePtr,
                                                                       jlong columnKey)
{
    try {
        TableRef table = TBL_REF(nativeTablePtr);
        table->remove_column(ColKey(columnKey));
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeInsertColumn(JNIEnv* env, jclass, jlong native_table_ptr,
                                                                       jlong columnIndex, jint type, jstring j_name)
{
    auto table_ptr = reinterpret_cast<realm::Table*>(native_table_ptr);
    try {
        JStringAccessor name(env, j_name); // throws

        DataType data_type = DataType(type);
        table_ptr->insert_column(table_ptr->ndx2colkey(columnIndex), data_type, name);
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeRenameColumn(JNIEnv* env, jobject, jlong nativeTablePtr,
                                                                       jlong columnKey, jstring name)
{
    try {
        JStringAccessor name2(env, name); // throws
        TableRef table = TBL_REF(nativeTablePtr);
        table->rename_column(ColKey(columnKey), name2);
    }
    CATCH_STD()
}

JNIEXPORT jboolean JNICALL Java_io_realm_internal_Table_nativeIsColumnNullable(JNIEnv* env, jobject,
                                                                               jlong nativeTablePtr,
                                                                               jlong columnKey)
{
TableRef table = TBL_REF(nativeTablePtr);
    return to_jbool(table->is_nullable(ColKey(columnKey))); // noexcept
}

JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeConvertColumnToNullable(JNIEnv* env, jobject obj,
                                                                                  jlong native_table_ptr,
                                                                                  jlong j_column_key,
                                                                                  jboolean)
{
    try {
        TableRef table = TBL_REF(native_table_ptr);
        ColKey col_key(j_column_key);
        bool nullable = true;
        bool throw_on_value_conversion = false;
        table->set_nullability(col_key, nullable, throw_on_value_conversion);
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeConvertColumnToNotNullable(JNIEnv* env, jobject obj,
                                                                                     jlong native_table_ptr,
                                                                                     jlong j_column_key,
                                                                                     jboolean is_primary_key)
{
    try {
        TableRef table = TBL_REF(native_table_ptr);
        ColKey col_key(j_column_key);
        bool nullable = false;
        bool throw_on_value_conversion = is_primary_key;
        table->set_nullability(col_key, nullable, throw_on_value_conversion);
    }
    CATCH_STD()
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeSize(JNIEnv* env, jobject, jlong nativeTablePtr)
{
    TableRef table = TBL_REF(nativeTablePtr);
    return static_cast<jlong>(table->size()); // noexcept
}

JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeClear(JNIEnv* env, jobject, jlong nativeTablePtr, jboolean is_partial_realm)
{
    try {
        TableRef table = TBL_REF(nativeTablePtr);
        if (is_partial_realm) {
            table->where().find_all().clear();
        } else {
            table->clear();
        }
    }
    CATCH_STD()
}


// -------------- Column information


JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeGetColumnCount(JNIEnv* env, jobject, jlong nativeTablePtr)
{
    TableRef table = TBL_REF(nativeTablePtr);
    return static_cast<jlong>(table->get_column_count()); // noexcept
}

JNIEXPORT jstring JNICALL Java_io_realm_internal_Table_nativeGetColumnName(JNIEnv* env, jobject, jlong nativeTablePtr,
                                                                           jlong columnKey)
{
    try {
        TableRef table = TBL_REF(nativeTablePtr);
        ColKey col_key(columnKey);
        StringData stringData = table->get_column_name(col_key);//<----- this is crashing
        return to_jstring(env, stringData);
    }
    CATCH_STD();
}

JNIEXPORT jstring JNICALL Java_io_realm_internal_Table_nativeGetColumnNameByIndex(JNIEnv* env, jobject, jlong nativeTablePtr,
                                                                           jlong columnIndex)
{
    try {
        TableRef table = TBL_REF(nativeTablePtr);
        auto column_key = table->ndx2colkey(columnIndex);
        StringData stringData = table->get_column_name(column_key);
        return to_jstring(env, stringData);
    }
    CATCH_STD();
}

//TODO rename index to objkey
JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeGetColumnIndex(JNIEnv* env, jobject, jlong nativeTablePtr,
                                                                          jstring columnName)
{
    try {
        TableRef table = TBL_REF(nativeTablePtr);
        JStringAccessor columnName2(env, columnName);                                     // throws
        ColKey col_key = table->get_column_key(columnName2);
        if (bool(col_key)) {//TODO generalize this test & return for similar lookups
            return to_jlong_or_not_found(table->colkey2ndx(col_key)); // noexcept //TODO does colkey2ndx return realm::not_found?
        }
        return -1;
    }
    CATCH_STD()
    return -1;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeGetColumnKey(JNIEnv* env, jobject, jlong nativeTablePtr,
                                                                          jstring columnName)
{
    try {
        JStringAccessor columnName2(env, columnName);                                     // throws
        TableRef table = TBL_REF(nativeTablePtr);
        ColKey col_key = table->get_column_key(columnName2);
        if (bool(col_key)) {//TODO generalize this test & return for similar lookups
            return reinterpret_cast<jlong>(col_key.value); // noexcept
        }
        return -1;
    }
    CATCH_STD()
    return -1;
}

JNIEXPORT jint JNICALL Java_io_realm_internal_Table_nativeGetColumnType(JNIEnv* env, jobject, jlong nativeTablePtr,
                                                                        jlong columnKey)
{
    ColKey column_key (columnKey);
    TableRef table = TBL_REF(nativeTablePtr);
    jint column_type = table->get_column_type(column_key);
    if (table->is_list(column_key) && column_type < type_LinkList) {
        // add the offset so it can be mapped correctly in Java (RealmFieldType#fromNativeValue)
        column_type += 128;
    }

    return column_type;
    // For primitive list
    // FIXME: Add test in https://github.com/realm/realm-java/pull/5221 before merging to master
    // FIXME: Add method in Object Store to return a PropertyType.
}


// ---------------- Row handling

JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeMoveLastOver(JNIEnv* env, jobject, jlong nativeTablePtr,
                                                                       jlong rowKey)
{
    try {
        TableRef table = TBL_REF(nativeTablePtr);
        table->remove_object(ObjKey(rowKey));
    }
    CATCH_STD()
}

// ----------------- Get cell

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeGetLong(JNIEnv* env, jobject, jlong nativeTablePtr,
                                                                   jlong columnKey, jlong rowKey)
{
    Table* table = ((Table*)*reinterpret_cast<realm::TableRef*>(nativeTablePtr));
    if (!TYPE_VALID(env, table, columnKey, type_Int)) {
        return 0;//TODO throw instead of returning a default value , check for similar use cases
    }
    return table->get_object(ObjKey(rowKey)).get<Int>(ColKey(columnKey)); // noexcept
}

JNIEXPORT jboolean JNICALL Java_io_realm_internal_Table_nativeGetBoolean(JNIEnv* env, jobject, jlong nativeTablePtr,
                                                                         jlong columnKey, jlong rowKey)
{
    Table* table = ((Table*)*reinterpret_cast<realm::TableRef*>(nativeTablePtr));
    if (!TYPE_VALID(env, table, columnKey, type_Bool)) {
        return JNI_FALSE;
    }

    return to_jbool(table->get_object(ObjKey(rowKey)).get<bool>(ColKey(columnKey)));
}

JNIEXPORT jfloat JNICALL Java_io_realm_internal_Table_nativeGetFloat(JNIEnv* env, jobject, jlong nativeTablePtr,
                                                                     jlong columnKey, jlong rowKey)
{
    Table* table = ((Table*)*reinterpret_cast<realm::TableRef*>(nativeTablePtr));
    if (!TYPE_VALID(env, table, columnKey, type_Float)) {
        return 0;
    }

    return table->get_object(ObjKey(rowKey)).get<float>(ColKey(columnKey));
}

JNIEXPORT jdouble JNICALL Java_io_realm_internal_Table_nativeGetDouble(JNIEnv* env, jobject, jlong nativeTablePtr,
                                                                       jlong columnKey, jlong rowKey)
{
    Table* table = ((Table*)*reinterpret_cast<realm::TableRef*>(nativeTablePtr));
    if (!TYPE_VALID(env, table, columnKey, type_Double)) {
        return 0;
    }

    return table->get_object(ObjKey(rowKey)).get<double>(ColKey(columnKey));
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeGetTimestamp(JNIEnv* env, jobject, jlong nativeTablePtr,
                                                                        jlong columnKey, jlong rowKey)
{
    Table* table = ((Table*)*reinterpret_cast<realm::TableRef*>(nativeTablePtr));
    if (!TYPE_VALID(env, table, columnKey, type_Timestamp)) {
        return 0;
    }
    try {
        return to_milliseconds(table->get_object(ObjKey(rowKey)).get<Timestamp>(ColKey(columnKey)));
    }
    CATCH_STD()
    return 0;
}

JNIEXPORT jstring JNICALL Java_io_realm_internal_Table_nativeGetString(JNIEnv* env, jobject, jlong nativeTablePtr,
                                                                       jlong columnKey, jlong rowKey)
{
    Table* table = ((Table*)*reinterpret_cast<realm::TableRef*>(nativeTablePtr));
    if (!TYPE_VALID(env, table, columnKey, type_String)) {
        return nullptr;
    }
    try {
        return to_jstring(env, table->get_object(ObjKey(rowKey)).get<StringData>(ColKey(columnKey)));
    }
    CATCH_STD()
    return nullptr;
}


JNIEXPORT jbyteArray JNICALL Java_io_realm_internal_Table_nativeGetByteArray(JNIEnv* env, jobject,
                                                                             jlong nativeTablePtr, jlong columnKey,
                                                                             jlong rowKey)
{
    Table* table = ((Table*)*reinterpret_cast<realm::TableRef*>(nativeTablePtr));
    if (!TYPE_VALID(env, table, columnKey, type_Binary)) {
        return nullptr;
    }
    try {
        realm::BinaryData bin = table->get_object(ObjKey(rowKey)).get<BinaryData>(ColKey(columnKey));
        return JavaClassGlobalDef::new_byte_array(env, bin);
    }
    CATCH_STD()

    return nullptr;
}
JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeGetLink(JNIEnv* env, jobject, jlong nativeTablePtr,
                                                                   jlong columnKey, jlong rowKey)
{
    Table* table = ((Table*)*reinterpret_cast<realm::TableRef*>(nativeTablePtr));
    if (!TYPE_VALID(env, table, columnKey, type_Link)) {
        return 0;
    }
    return static_cast<jlong>(table->get_object(ObjKey(rowKey)).get<ObjKey>(ColKey(columnKey)).value); // noexcept
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeGetLinkTarget(JNIEnv* env, jobject, jlong nativeTablePtr,
                                                                         jlong columnKey)
{
    try {
        Table* table = &(*((Table*)TBL_REF(nativeTablePtr))->get_link_target(ColKey(columnKey)));
        return reinterpret_cast<jlong>(new TableRef(table));
    }
    CATCH_STD()
    return 0;
}

JNIEXPORT jboolean JNICALL Java_io_realm_internal_Table_nativeIsNull(JNIEnv*, jobject, jlong nativeTablePtr,
                                                                     jlong columnKey, jlong rowKey)
{
    TableRef table = TBL_REF(nativeTablePtr);
    return to_jbool(table->get_object(ObjKey(rowKey)).is_null(ColKey(columnKey))); // noexcept
}

// ----------------- Set cell

JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeSetLink(JNIEnv* env, jclass, jlong nativeTablePtr,
                                                                  jlong columnKey, jlong rowKey,
                                                                  jlong targetRowKey, jboolean isDefault)
{
    Table* table = ((Table*)*reinterpret_cast<realm::TableRef*>(nativeTablePtr));
    if (!TYPE_VALID(env, table, columnKey, type_Link)) {
        return;
    }
    try {
        table->get_object(ObjKey(rowKey)).set(ColKey(columnKey), ObjKey(targetRowKey), B(isDefault));
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeSetLong(JNIEnv* env, jclass, jlong nativeTablePtr,
                                                                  jlong columnKey, jlong rowKey, jlong value,
                                                                  jboolean isDefault)
{
    Table* table = ((Table*)*reinterpret_cast<realm::TableRef*>(nativeTablePtr));
    if (!TYPE_VALID(env, table, columnKey, type_Int)) {
        return;
    }
    try {
        table->get_object(ObjKey(rowKey)).set(ColKey(columnKey), value, B(isDefault));
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeIncrementLong(JNIEnv* env, jclass, jlong nativeTablePtr,
                                                                  jlong columnKey, jlong rowKey, jlong value)
{

    Table* table = ((Table*)*reinterpret_cast<realm::TableRef*>(nativeTablePtr));
    if (!TYPE_VALID(env, table, columnKey, type_Int)) {
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

JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeSetBoolean(JNIEnv* env, jclass, jlong nativeTablePtr,
                                                                     jlong columnKey, jlong rowKey,
                                                                     jboolean value, jboolean isDefault)
{
    Table* table = ((Table*)*reinterpret_cast<realm::TableRef*>(nativeTablePtr));
    if (!TYPE_VALID(env, table, columnKey, type_Bool)) {
        return;
    }
    try {
        table->get_object(ObjKey(rowKey)).set(ColKey(columnKey), B(value), B(isDefault));
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeSetFloat(JNIEnv* env, jclass, jlong nativeTablePtr,
                                                                   jlong columnKey, jlong rowKey, jfloat value,
                                                                   jboolean isDefault)
{
    Table* table = ((Table*)*reinterpret_cast<realm::TableRef*>(nativeTablePtr));
    if (!TYPE_VALID(env, table, columnKey, type_Float)) {
        return;
    }
    try {
        table->get_object(ObjKey(rowKey)).set(ColKey(columnKey), value, B(isDefault));
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeSetDouble(JNIEnv* env, jclass, jlong nativeTablePtr,
                                                                    jlong columnKey, jlong rowKey, jdouble value,
                                                                    jboolean isDefault)
{
    Table* table = ((Table*)*reinterpret_cast<realm::TableRef*>(nativeTablePtr));
    if (!TYPE_VALID(env, table, columnKey, type_Double)) {
        return;
    }
    try {
        table->get_object(ObjKey(rowKey)).set(ColKey(columnKey), value, B(isDefault));
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeSetString(JNIEnv* env, jclass, jlong nativeTablePtr,
                                                                    jlong columnKey, jlong rowKey, jstring value,
                                                                    jboolean isDefault)
{
    Table* table = ((Table*)*reinterpret_cast<realm::TableRef*>(nativeTablePtr));
    if (!TYPE_VALID(env, table, columnKey, type_String)) {
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

JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeSetTimestamp(JNIEnv* env, jclass, jlong nativeTablePtr,
                                                                       jlong columnKey, jlong rowKey,
                                                                       jlong timestampValue, jboolean isDefault)
{
    Table* table = ((Table*)*reinterpret_cast<realm::TableRef*>(nativeTablePtr));
    if (!TYPE_VALID(env, table, columnKey, type_Timestamp)) {
        return;
    }
    try {
        table->get_object(ObjKey(rowKey)).set(ColKey(columnKey), from_milliseconds(timestampValue), B(isDefault));
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeSetByteArray(JNIEnv* env, jclass, jlong nativeTablePtr,
                                                                       jlong columnKey, jlong rowKey,
                                                                       jbyteArray dataArray, jboolean isDefault)
{
    Table* table = ((Table*)*reinterpret_cast<realm::TableRef*>(nativeTablePtr));
    if (!TYPE_VALID(env, table, columnKey, type_Binary)) {
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

JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeSetNull(JNIEnv* env, jclass, jlong nativeTablePtr,
                                                                  jlong columnKey, jlong rowKey,
                                                                  jboolean isDefault)
{
    Table* table = ((Table*)*reinterpret_cast<realm::TableRef*>(nativeTablePtr));
    if (!COL_NULLABLE(env, table, columnKey)) {
        return;
    }

    try {
        table->get_object(ObjKey(rowKey)).set_null(ColKey(columnKey), B(isDefault));
    }
    CATCH_STD()
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeGetRowPtr(JNIEnv* env, jobject, jlong nativeTablePtr,
                                                                     jlong key)
{
    try {
        TableRef table = TBL_REF(nativeTablePtr);
        Obj* obj = new Obj(table->get_object(ObjKey(key)));
        return reinterpret_cast<jlong>(obj);
    }
    CATCH_STD()
    return reinterpret_cast<jlong>(nullptr);
}

//--------------------- Indexing methods:

JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeAddSearchIndex(JNIEnv* env, jobject, jlong nativeTablePtr,
                                                                         jlong columnKey)
{
    TableRef table = TBL_REF(nativeTablePtr);
    DataType column_type = table->get_column_type(ColKey(columnKey));
    if (!is_allowed_to_index(env, column_type)) {
        return;
    }

    try {
        table->add_search_index(ColKey(columnKey));
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeRemoveSearchIndex(JNIEnv* env, jobject,
                                                                            jlong nativeTablePtr, jlong columnKey)
{
    TableRef table = TBL_REF(nativeTablePtr);
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
                                                                             jlong nativeTablePtr, jlong columnKey)
{
    try {
        TableRef table = TBL_REF(nativeTablePtr);
        return to_jbool(table->has_search_index(ColKey(columnKey)));
    }
    CATCH_STD()
    return JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_io_realm_internal_Table_nativeIsNullLink(JNIEnv* env, jobject, jlong nativeTablePtr,
                                                                         jlong columnKey, jlong rowKey)
{
    Table* table = ((Table*)*reinterpret_cast<realm::TableRef*>(nativeTablePtr));
    if (!TYPE_VALID(env, table, columnKey, type_Link)) {
        return JNI_FALSE;
    }

    return to_jbool(table->get_object(ObjKey(rowKey)).is_null(ColKey(columnKey)));
}

JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeNullifyLink(JNIEnv* env, jclass, jlong nativeTablePtr,
                                                                      jlong columnKey, jlong rowKey)
{
    Table* table = ((Table*)*reinterpret_cast<realm::TableRef*>(nativeTablePtr));
    if (!TYPE_VALID(env, table, columnKey, type_Link)) {
        return;
    }
    try {
        table->get_object(ObjKey(rowKey)).set_null(ColKey(columnKey));
    }
    CATCH_STD()
}

//---------------------- Count

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeCountLong(JNIEnv* env, jobject, jlong nativeTablePtr,
                                                                     jlong columnKey, jlong value)
{
    Table* table = ((Table*)*reinterpret_cast<realm::TableRef*>(nativeTablePtr));
    if (!TYPE_VALID(env, table, columnKey, type_Int)) {
        return 0;
    }
    try {
        return static_cast<jlong>(table->count_int(ColKey(columnKey), value));
    }
    CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeCountFloat(JNIEnv* env, jobject, jlong nativeTablePtr,
                                                                      jlong columnKey, jfloat value)
{
    Table* table = ((Table*)*reinterpret_cast<realm::TableRef*>(nativeTablePtr));
    if (!TYPE_VALID(env, table, columnKey, type_Float)) {
        return 0;
    }
    try {
        return static_cast<jlong>(table->count_float(ColKey(columnKey), value));
    }
    CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeCountDouble(JNIEnv* env, jobject, jlong nativeTablePtr,
                                                                       jlong columnKey, jdouble value)
{
    Table* table = ((Table*)*reinterpret_cast<realm::TableRef*>(nativeTablePtr));
    if (!TYPE_VALID(env, table, columnKey, type_Double)) {
        return 0;
    }
    try {
        return static_cast<jlong>(table->count_double(ColKey(columnKey), value));
    }
    CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeCountString(JNIEnv* env, jobject, jlong nativeTablePtr,
                                                                       jlong columnKey, jstring value)
{
    Table* table = ((Table*)*reinterpret_cast<realm::TableRef*>(nativeTablePtr));
    if (!TYPE_VALID(env, table, columnKey, type_String)) {
        return 0;
    }
    try {
        JStringAccessor value2(env, value); // throws
        return static_cast<jlong>(table->count_string(ColKey(columnKey), value2));
    }
    CATCH_STD()
    return 0;
}


JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeWhere(JNIEnv* env, jobject, jlong nativeTablePtr)
{
    try {
        TableRef table = TBL_REF(nativeTablePtr);
        Query* queryPtr = new Query(table->where());
        return reinterpret_cast<jlong>(queryPtr);
    }
    CATCH_STD()
    return reinterpret_cast<jlong>(nullptr);
}

//----------------------- FindFirst

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeFindFirstInt(JNIEnv* env, jclass, jlong nativeTablePtr,
                                                                        jlong columnKey, jlong value)
{
    Table* table = ((Table*)*reinterpret_cast<realm::TableRef*>(nativeTablePtr));
    if (!TYPE_VALID(env, table, columnKey, type_Int)) {
        return 0;
    }
    try {
        return to_jlong_or_not_found(table->find_first_int(ColKey(columnKey), value));
    }
    CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeFindFirstBool(JNIEnv* env, jobject, jlong nativeTablePtr,
                                                                         jlong columnKey, jboolean value)
{
    Table* table = ((Table*)*reinterpret_cast<realm::TableRef*>(nativeTablePtr));
    if (!TYPE_VALID(env, table, columnKey, type_Bool)) {
        return 0;
    }
    try {
        return to_jlong_or_not_found(table->find_first_bool(ColKey(columnKey), to_bool(value)));
    }
    CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeFindFirstFloat(JNIEnv* env, jobject, jlong nativeTablePtr,
                                                                          jlong columnKey, jfloat value)
{
    Table* table = ((Table*)*reinterpret_cast<realm::TableRef*>(nativeTablePtr));
    if (!TYPE_VALID(env, table, columnKey, type_Float)) {
        return 0;
    }
    try {
        return to_jlong_or_not_found(table->find_first_float(ColKey(columnKey), value));
    }
    CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeFindFirstDouble(JNIEnv* env, jobject, jlong nativeTablePtr,
                                                                           jlong columnKey, jdouble value)
{
    Table* table = ((Table*)*reinterpret_cast<realm::TableRef*>(nativeTablePtr));
    if (!TYPE_VALID(env, table, columnKey, type_Double)) {
        return 0;
    }
    try {
        return to_jlong_or_not_found(table->find_first_double(ColKey(columnKey), value));
    }
    CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeFindFirstTimestamp(JNIEnv* env, jobject,
                                                                              jlong nativeTablePtr, jlong columnKey,
                                                                              jlong dateTimeValue)
{
    Table* table = ((Table*)*reinterpret_cast<realm::TableRef*>(nativeTablePtr));
    if (!TYPE_VALID(env, table, columnKey, type_Timestamp)) {
        return 0;
    }
    try {
        return to_jlong_or_not_found(table->find_first_timestamp(ColKey(columnKey), from_milliseconds(dateTimeValue)));
    }
    CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeFindFirstString(JNIEnv* env, jclass, jlong nativeTablePtr,
                                                                           jlong columnKey, jstring value)
{
    Table* table = ((Table*)*reinterpret_cast<realm::TableRef*>(nativeTablePtr));
    if (!TYPE_VALID(env, table, columnKey, type_String)) {
        return 0;
    }

    try {
        JStringAccessor value2(env, value); // throws
        return to_jlong_or_not_found(table->find_first_string(ColKey(columnKey), value2));
    }
    CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeFindFirstNull(JNIEnv* env, jclass, jlong nativeTablePtr,
                                                                         jlong columnKey)
{
    Table* table = ((Table*)*reinterpret_cast<realm::TableRef*>(nativeTablePtr));
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

JNIEXPORT jstring JNICALL Java_io_realm_internal_Table_nativeGetName(JNIEnv* env, jobject, jlong nativeTablePtr)
{
    try {
        TableRef table = TBL_REF(nativeTablePtr);
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

JNIEXPORT jboolean JNICALL Java_io_realm_internal_Table_nativeIsValid(JNIEnv*, jobject, jlong nativeTablePtr)
{
    TR_ENTER_PTR(nativeTablePtr)
    if(TBL_REF(nativeTablePtr)) {
        return JNI_TRUE;
    } else {
        return JNI_FALSE;
    }
}
//TODO optimise calls to ndx
static bool pk_table_needs_migration(ConstTableRef pk_table)
{
    // Fix wrong types (string, int) -> (string, string)
    //TODO check ndx2colkey is the correct way to access the column type
    if (pk_table->get_column_type(pk_table->ndx2colkey(FIELD_COLUMN_INDEX)) == type_Int) {
        return true;
    }

    // If needed remove "class_" prefix from class names
    size_t number_of_rows = pk_table->size();
    for (size_t row_ndx = 0; row_ndx < number_of_rows; row_ndx++) {
        StringData table_name = pk_table->get_object(row_ndx).get<StringData>(pk_table->ndx2colkey(CLASS_COLUMN_INDEX));
        if (table_name.begins_with(TABLE_PREFIX)) {
            return true;
        }
    }
    // From realm-java 2.0.0, pk table's class column requires a search index.
    if (!pk_table->has_search_index(pk_table->ndx2colkey(CLASS_COLUMN_INDEX))) {
        return true;
    }
    return false;
}

// 1) Fixes interop issue with Cocoa Realm where the Primary Key table had different types.
// This affects:
// - All Realms created by Cocoa and used by Realm-android up to 0.80.1
// - All Realms created by Realm-Android 0.80.1 and below
// See https://github.com/realm/realm-java/issues/1059
//
// 2) Fix interop issue with Cocoa Realm where primary key tables on Cocoa doesn't have the "class_" prefix.
// This affects:
// - All Realms created by Cocoa and used by Realm-android up to 0.84.1
// - All Realms created by Realm-Android 0.84.1 and below
// See https://github.com/realm/realm-java/issues/1703
//
// 3> PK table's column 'pk_table' needs search index in order to use set_string_unique.
// This affects:
// - All Realms created by Cocoa and used by Realm-java before 2.0.0
// See https://github.com/realm/realm-java/pull/3488

// This methods converts the old (wrong) table format (string, integer) to the right (string,string) format and strips
// any class names in the col[0] of their "class_" prefix
//TODO optimise calls to ndx
static bool migrate_pk_table(const Group& group, TableRef pk_table)
{
    bool changed = false;

    // Fix wrong types (string, int) -> (string, string)
    if (pk_table->get_column_type(pk_table->ndx2colkey(FIELD_COLUMN_INDEX)) == type_Int) {
        StringData tmp_col_name = StringData("tmp_field_name");
        ColKey tmp_col_ndx = pk_table->add_column(DataType(type_String), tmp_col_name);

        // Create tmp string column with field name instead of column index
        size_t number_of_rows = pk_table->size();
        for (size_t row_ndx = 0; row_ndx < number_of_rows; row_ndx++) {
            StringData table_name = pk_table->get_object(row_ndx).get<StringData>(pk_table->ndx2colkey(CLASS_COLUMN_INDEX));
            size_t col_ndx = static_cast<size_t>(pk_table->get_object(row_ndx).get<int64_t>(pk_table->ndx2colkey(CLASS_COLUMN_INDEX)));
            StringData col_name = group.get_table(table_name)->get_column_name(pk_table->ndx2colkey(col_ndx));
            // Make a copy of the string
            pk_table->get_object(row_ndx).set(tmp_col_ndx, col_name);
        }

        // Delete old int column, and rename tmp column to same name
        // The column index for the renamed column will then be the same as the deleted old column
        pk_table->remove_column(pk_table->ndx2colkey(FIELD_COLUMN_INDEX));
        pk_table->rename_column(pk_table->get_column_key(tmp_col_name), StringData("pk_property"));
        changed = true;
    }

    // If needed remove "class_" prefix from class names
    size_t number_of_rows = pk_table->size();
    for (size_t row_ndx = 0; row_ndx < number_of_rows; row_ndx++) {
        StringData table_name = pk_table->get_object(row_ndx).get<StringData>(pk_table->ndx2colkey(CLASS_COLUMN_INDEX));
        if (table_name.begins_with(TABLE_PREFIX)) {
            // New string copy is needed, since the original memory will be changed.
            std::string str(table_name.substr(TABLE_PREFIX.length()));
            StringData sd(str);
            pk_table->get_object(row_ndx).set(pk_table->ndx2colkey(CLASS_COLUMN_INDEX), sd);
            changed = true;
        }
    }

    // From realm-java 2.0.0, pk table's class column requires a search index.
    if (!pk_table->has_search_index(pk_table->ndx2colkey(CLASS_COLUMN_INDEX))) {
        pk_table->add_search_index(pk_table->ndx2colkey(CLASS_COLUMN_INDEX));
        changed = true;
    }
    return changed;
}

JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeMigratePrimaryKeyTableIfNeeded(JNIEnv* env, jclass,
                                                                                         jlong shared_realm_ptr)
{
    TR_ENTER_PTR(shared_realm_ptr)
    auto& shared_realm = *reinterpret_cast<SharedRealm*>(shared_realm_ptr);
    try {
        if (!shared_realm->read_group().has_table(PK_TABLE_NAME)) {
            return;
        }

        auto pk_table = shared_realm->read_group().get_table(PK_TABLE_NAME);
        if (!pk_table_needs_migration(pk_table)) {
            return;
        }

        shared_realm->begin_transaction();
        if (migrate_pk_table(shared_realm->read_group(), pk_table)) {
            shared_realm->commit_transaction();
        }
        else {
            shared_realm->cancel_transaction();
        }
    }
    CATCH_STD()
}

JNIEXPORT jboolean JNICALL Java_io_realm_internal_Table_nativeHasSameSchema(JNIEnv*, jobject, jlong thisTablePtr,
                                                                            jlong otherTablePtr)
{
    //TODO check is the correct replacement
    using tf = _impl::TableFriend;
    TableRef this_table = TBL_REF(thisTablePtr);
    TableRef other_table = TBL_REF(otherTablePtr);
    return to_jbool(tf::get_spec(*this_table) == tf::get_spec(*other_table));
}

static void finalize_table(jlong ptr)
{
    TR_ENTER_PTR(ptr)
    delete reinterpret_cast<realm::TableRef*>(ptr);
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeGetFinalizerPtr(JNIEnv*, jclass)
{
    TR_ENTER()
    return reinterpret_cast<jlong>(&finalize_table);
}
