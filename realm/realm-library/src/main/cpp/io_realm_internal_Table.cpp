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


// General comments about the implementation of
// Java_io_realm_internal_Table_nativeConvertColumnToNullable and
// Java_io_realm_internal_Table_nativeConvertColumnToNotNullable
//
// 1. converting a (not-)nullable column is idempotent (and is implemented as a no-op)
// 2. not all column types can be converted (cannot be (not-)nullable)
// 3. converting to not-nullable, null values are converted to (core's) default values of the type
// 4. as temporary column is __inserted__ just before the column to be converted
// 4a. __TMP__number is used as name of the temporary column
// 4b. with N columns, at most N __TMP__i (0 <= i < N) must be tried, and while (true) { .. } will always terminate
// 4c. the temporary column will have index columnIndex (or column_index)
// 4d. the column to be converted will index shifted one place to column_index + 1
// 5. search indexing must be preserved
// 6. removing the original column and renaming the temporary column will make it look like original is being modified
//
// WARNING: These methods do NOT work on primary key columns if the Realm is synchronized.
//

// Converts a table to allow for nullable values
// Works on both normal table columns and sub tables
// TODO remove/update this method once Core implement this https://github.com/realm/realm-core-private/issues/209
static void convert_column_to_nullable(JNIEnv* env, Table* old_table, jlong old_col_key, Table* new_table, jlong new_col_key)
{
    ColKey old_col(old_col_key);
    ColKey new_col(new_col_key);
    DataType column_type = old_table->get_column_type(old_col);
    if (old_table != new_table) {
        std::vector<ObjKey> objects;//TODO objects is not used?
        new_table->create_objects(old_table->size(), objects);
    }
    for (size_t i = 0; i < old_table->size(); ++i) {
        switch (column_type) {
            case type_String: {
                // Payload copy is needed
//                StringData sd(old_table->get_string(old_col_ndx, i));
//                new_table->set_string(new_col_ndx, i, sd);
                StringData sd = old_table->get_object(i).get<StringData>(old_col);
                new_table->get_object(i).set(new_col, sd);
                break;
            }
            case type_Binary: {
//                BinaryData bd = old_table->get_binary(old_col_ndx, i);
//                new_table->set_binary(new_col_ndx, i, BinaryData(bd.data(), bd.size()));

                BinaryData binaryData = old_table->get_object(i).get<BinaryData>(old_col);
                new_table->get_object(i).set(new_col, binaryData);
                break;
            }
            case type_Int:
//                new_table->set_int(new_col_ndx, i, old_table->get_int(old_col_ndx, i));
                new_table->get_object(i).set(new_col, old_table->get_object(i).get<Int>(old_col));
                break;
            case type_Bool:
//                new_table->set_bool(new_col_ndx, i, old_table->get_bool(old_col_ndx, i));
                new_table->get_object(i).set(new_col, old_table->get_object(i).get<bool>(old_col));
                break;
            case type_Timestamp:
//                new_table->set_timestamp(new_col_ndx, i, old_table->get_timestamp(old_col_ndx, i));
                new_table->get_object(i).set(new_col, old_table->get_object(i).get<Timestamp>(old_col));
                break;
            case type_Float:
//                new_table->set_float(new_col_ndx, i, old_table->get_float(old_col_ndx, i));
                new_table->get_object(i).set(new_col, old_table->get_object(i).get<float>(old_col));
                break;
            case type_Double:
//                new_table->set_double(new_col_ndx, i, old_table->get_double(old_col_ndx, i));
                new_table->get_object(i).set(new_col, old_table->get_object(i).get<double>(old_col));
                break;
            case type_Link:
            case type_LinkList:
            case type_OldMixed:
            case type_OldTable:
                // checked previously
                break;
            case type_OldDateTime:
                ThrowException(env, UnsupportedOperation, "The old DateTime type is not supported.");
                return;
        }
    }
}

// Creates the new column into which all old data is copied when switching between nullable and non-nullable.
// TODO remove/update this method once Core implement this https://github.com/realm/realm-core-private/issues/209
static ColKey create_new_column(Table* table, size_t column_key, bool nullable)
{
    ColKey col_key(column_key);
    std::string column_name = table->get_column_name(col_key);
    DataType column_type = table->get_column_type(col_key);
    bool is_primitive_list(table->is_list(col_key) && column_type < type_LinkList);
//    bool is_subtable = table->get_column_type(col_key) == DataType::type_Table;
    size_t j = 0;
    ColKey new_col;
    while (true) {
        std::ostringstream ss;
        ss << std::string("__TMP__") << j;
        std::string str = ss.str();
        StringData tmp_column_name(str);
        if (!bool(table->get_column_key(tmp_column_name))) {
//            if (is_subtable) {
//                DataType original_type = table->get_subdescriptor(column_index)->get_column_type(0);
//                table->insert_column(column_index, type_Table, tmp_column_name, true);
//                table->get_subdescriptor(column_index)->add_column(original_type, ObjectStore::ArrayColumnName, nullptr, nullable);
//            }
//            else {
            if (is_primitive_list) {
                new_col = table->add_column_list(column_type, tmp_column_name, nullable);
            } else {
                new_col = table->add_column(column_type, tmp_column_name, nullable);
            }

//            }
            break;
        }
        j++;
    }

    // Search index has too be added first since if it is a PK field, add_xxx_unique will check it.
//    if (!is_subtable) {
        // TODO indexes on sub tables not supported yet?
        if (table->has_search_index(new_col)) {
            table->add_search_index(col_key);
        }
    return new_col;
//    }
}

// TODO remove/update this method once Core implement this https://github.com/realm/realm-core-private/issues/209
JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeConvertColumnToNullable(JNIEnv* env, jobject obj,
                                                                                  jlong native_table_ptr,
                                                                                  jlong j_column_key,
                                                                                  jboolean)
{
//    Table* table = TBL(native_table_ptr);

//    if (!TBL_AND_COL_INDEX_VALID(env, table, j_column_key)) {
//        return;
//    }
    try {
        TableRef table = TBL_REF(native_table_ptr);
//        if (!TBL_AND_COL_INDEX_VALID(env, table, j_column_key)) {
//            return;
//        }
//        if (table->has_shared_type()) {
//            ThrowException(env, UnsupportedOperation, "Not allowed to convert field in subtable.");
//            return;
//        }

//        size_t column_index = S(j_column_index);
        ColKey col_key(j_column_key);
        DataType column_type = table->get_column_type(col_key);
        std::string column_name = table->get_column_name(col_key);
//        bool is_subtable = (column_type == DataType::type_Table);

        // Cannot convert Object links or lists of objects
        if (column_type == type_Link || column_type == type_LinkList) {//TODO make sure we don't need || column_type == type_OldMixed since the Realm was upgraded to the new format already
            ThrowException(env, IllegalArgument, "Wrong type - cannot be converted to nullable.");
        }

        // Exit quickly if column is already nullable
        if (Java_io_realm_internal_Table_nativeIsColumnNullable(env, obj, native_table_ptr, j_column_key)) {
            return;
        }

        // 1. Create temporary table
        ColKey new_col = create_new_column(table, j_column_key, true);

        // Move all values
//        if (is_subtable) {
//            for (size_t i = 0; i < table->size(); ++i) {
//                TableRef new_subtable = table->get_subtable(column_index, i);
//                TableRef old_subtable = table->get_subtable(column_index + 1, i);
//                convert_column_to_nullable(env, old_subtable.get(), 0, new_subtable.get(), 0);
//            }
//        }
//        else {
        convert_column_to_nullable(env, table, j_column_key, table, new_col.value);
//        }

        // Cleanup
        table->remove_column(new_col);
        table->rename_column(col_key, column_name);

    }
    CATCH_STD()
}

// Convert a tables values to not nullable, but converting all null values to the defaul value for the type
// Works on both normal table columns and sub tables
// TODO remove/update this method once Core implement this https://github.com/realm/realm-core-private/issues/209
static void convert_column_to_not_nullable(JNIEnv* env, Table* old_table, jlong old_col_key, Table* new_table, jlong new_col_key, bool is_primary_key)
{
    ColKey old_col(old_col_key);
    ColKey new_col(new_col_key);
    DataType column_type = old_table->get_column_type(old_col);
    std::string column_name = old_table->get_column_name(old_col);
    bool is_primitive_list(old_table->is_list(old_col) && column_type < type_LinkList);
    size_t no_rows = old_table->size();
    if (old_table != new_table) {
//        new_table->add_empty_row(no_rows);
        std::vector<ObjKey> objects;//TODO objects is not used?
        new_table->create_objects(no_rows, objects);
    }
    for (size_t i = 0; i < no_rows; ++i) {
        switch (column_type) { // FIXME: respect user-specified default values
            case type_String: {
                // Payload copy is needed
//                StringData sd = old_table->get_string(old_col_ndx, i);
                StringData sd = old_table->get_object(i).get<StringData>(old_col);//TODO check if accessing using the ndx in the case is ok?
                if (sd == realm::null()) {
                    if (is_primary_key) {
                        THROW_JAVA_EXCEPTION(env, JavaExceptionDef::IllegalState,
                                             format(c_null_values_cannot_set_required_msg, column_name));
                    }
                    else {
//                        new_table->set_string(new_col_ndx, i, "");
                        new_table->get_object(i).set(new_col, "");
                    }
                }
                else {
//                    new_table->set_string(new_col_ndx, i, sd);
                    new_table->get_object(i).set(new_col, sd);
                }
                break;
            }
            case type_Binary: {
                if (is_primitive_list) {
                    //TODO do I need to copy all elements, one by one or is there a bulk copy/insert
                    // WIP until https://github.com/realm/realm-core-private/issues/209 is implemented which should get rid of these methods
//                    auto binaryDatas = old_table->get_object(i).get(old_col).get_linklist(old_col);//CRASH
//                    for (int i=0; i<binaryDatas.size(); i++) {
//                        ObjKey objkey = binaryDatas.get(i);
//                        Obj binaryData = old_table->get_object(objkey);
//                    }
                } else {
                    // BinaryData bd = old_table->get_binary(old_col_ndx, i);
                    BinaryData binaryData = old_table->get_object(i).get<BinaryData>(old_col);
                    if (binaryData.is_null()) {
//                    new_table->set_binary(new_col_ndx, i, BinaryData("", 0));
                        new_table->get_object(i).set(new_col, BinaryData("", 0));
                    }
                    else {
                        // Payload copy is needed
                        //FIXME should use BinaryData no set defined in https://github.com/realm/realm-core-private/blob/master/src/realm/obj.cpp#L842
                        //      that uses vector<char>
                        std::vector<char> bd_copy(binaryData.data(), binaryData.data() + binaryData.size());
//                    new_table->set_binary(new_col_ndx, i, BinaryData(bd_copy.data(), bd_copy.size()));
                        new_table->get_object(i).set(new_col,  BinaryData(bd_copy.data(), bd_copy.size()));
                    }
                }

                break;
            }
            case type_Int:
                if (old_table->get_object(i).is_null(old_col)) {
                    if (is_primary_key) {
                        THROW_JAVA_EXCEPTION(env, JavaExceptionDef::IllegalState,
                                             format(c_null_values_cannot_set_required_msg, column_name));
                    }
                    else {
//                        new_table->set_int(new_col_ndx, i, 0);
                        new_table->get_object(i).set(new_col, 0);
                    }
                }
                else {
//                    new_table->set_int(new_col_ndx, i, old_table->get_int(old_col_ndx, i));
                    new_table->get_object(i).set(new_col, old_table->get_object(i).get<Int>(old_col));
                }
                break;
            case type_Bool:
                if (old_table->get_object(i).is_null(old_col)) {
//                    new_table->set_bool(new_col_ndx, i, false);
                    new_table->get_object(i).set(new_col, false);
                }
                else {
//                    new_table->set_bool(new_col_ndx, i, old_table->get_bool(old_col_ndx, i));
                    new_table->get_object(i).set(new_col, old_table->get_object(i).get<bool>(old_col));
                }
                break;
            case type_Timestamp:
                if (old_table->get_object(i).is_null(old_col)) {
//                    new_table->set_timestamp(new_col_ndx, i, Timestamp(0, 0));
                    new_table->get_object(i).set(new_col, Timestamp(0, 0));
                }
                else {
//                    new_table->set_timestamp(new_col_ndx, i, old_table->get_timestamp(old_col_ndx, i));
                    new_table->get_object(i).set(new_col, old_table->get_object(i).get<Timestamp>(old_col));

                }
                break;
            case type_Float:
                if (old_table->get_object(i).is_null(old_col)) {
//                    new_table->set_float(new_col_ndx, i, 0.0);
                    new_table->get_object(i).set(new_col, 0.0);
                }
                else {
//                    new_table->set_float(new_col_ndx, i, old_table->get_float(old_col_ndx, i));
                    new_table->get_object(i).set(new_col, old_table->get_object(i).get<float>(old_col));
                }
                break;
            case type_Double:
                if (old_table->get_object(i).is_null(old_col)) {
//                    new_table->set_double(new_col_ndx, i, 0.0);
                    new_table->get_object(i).set(new_col, 0.0);
                }
                else {
//                    new_table->set_double(new_col_ndx, i, old_table->get_double(old_col_ndx, i));
                    new_table->get_object(i).set(new_col, old_table->get_object(i).get<double>(old_col));
                }
                break;
            case type_Link:
            case type_LinkList:
            case type_OldMixed:
            case type_OldTable:
                // checked previously
                break;
            case type_OldDateTime:
                // not used
                ThrowException(env, UnsupportedOperation, "The old DateTime type is not supported.");
                return;
        }
    }
}

// TODO remove/update this method once Core implement this https://github.com/realm/realm-core-private/issues/209
JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeConvertColumnToNotNullable(JNIEnv* env, jobject obj,
                                                                                     jlong native_table_ptr,
                                                                                     jlong j_column_key,
                                                                                     jboolean is_primary_key)
{
    try {
        TableRef table = TBL_REF(native_table_ptr);
//        if (!TBL_AND_COL_INDEX_VALID(env, table, j_column_key)) {
//            return;
//        }
//        if (table->has_shared_type()) {
//            ThrowException(env, UnsupportedOperation, "Not allowed to convert field in subtable.");
//            return;
//        }

        // Exit quickly if column is already non-nullable
        if (!Java_io_realm_internal_Table_nativeIsColumnNullable(env, obj, native_table_ptr, j_column_key)) {
            return;
        }

//        size_t column_index = S(j_column_index);
        ColKey col_key(j_column_key);
        std::string column_name = table->get_column_name(col_key);
        DataType column_type = table->get_column_type(col_key);
//        bool is_subtable = (column_type == DataType::type_Table);

        if (column_type == type_Link || column_type == type_LinkList) {//TODO make sure this is not needed  || column_type == type_OldMixed since the Realm has been already upgraded
            ThrowException(env, IllegalArgument, "Wrong type - cannot be converted to nullable.");
        }

        // 1. Create temporary table
        ColKey new_col_key = create_new_column(table, j_column_key, false);

        // 2. Move all values
//        if (is_subtable) {
//            for (size_t i = 0; i < table->size(); ++i) {
//                TableRef new_subtable = table->get_subtable(column_index, i);
//                TableRef old_subtable = table->get_subtable(column_index + 1, i);
//                convert_column_to_not_nullable(env, old_subtable.get(), 0, new_subtable.get(), 0, is_primary_key);
//            }
//        }
//        if (is_primitive_list) {
//            for (size_t i = 0; i < table->size(); ++i) {
//                TableRef new_subtable = table->get_subtable(column_index, i);
//                TableRef old_subtable = table->get_subtable(column_index + 1, i);
//                convert_column_to_not_nullable(env, old_subtable.get(), 0, new_subtable.get(), 0, is_primary_key);
//            }
//        }
//        else {
            convert_column_to_not_nullable(env, table, j_column_key, table, new_col_key.value, is_primary_key);
//        }

        // 3. Delete old values
        table->remove_column(new_col_key);
        table->rename_column(col_key, column_name);
    }
    CATCH_STD()

//    try {
//        TableRef table = TBL_REF(native_table_ptr);
//        ColKey col_key(j_column_key);
//        bool nullable = false;
//        bool throw_on_value_conversion = is_primary_key;
//        table->set_nullability(col_key, nullable, throw_on_value_conversion);
//    }
//    CATCH_STD()
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
