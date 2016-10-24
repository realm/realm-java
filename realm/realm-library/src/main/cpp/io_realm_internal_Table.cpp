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

#include <realm/table.hpp>
#include <realm/table_view.hpp>
#include <realm/link_view.hpp>

#include "util.hpp"
#include "io_realm_internal_Table.h"
#include "tablebase_tpl.hpp"

using namespace std;
using namespace realm;

inline static void is_allowed_to_index(JNIEnv* env, DataType column_type) {
    if (!(column_type == type_String ||
                column_type == type_Int ||
                column_type == type_Bool ||
                column_type == type_Timestamp ||
                column_type == type_OldDateTime)) {
        throw invalid_argument(
                "This field cannot be indexed - "
                "Only String/byte/short/int/long/boolean/Date fields are supported.");
    }
}

// Note: Don't modify spec on a table which has a shared_spec.
// A spec is shared on subtables that are not in Mixed columns.
//

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeAddColumn
  (JNIEnv *env, jobject, jlong nativeTablePtr, jint colType, jstring name, jboolean isNullable)
{
    return try_catch<jlong>(env, [&]() {
        Table* table = TBL(nativeTablePtr);
        TABLE_VALID(env, table);
        if (table->has_shared_type()) {
            throw JavaUnsupportedOperation(
                    "Not allowed to add field in subtable. Use getSubtableSchema() on root table instead.");
        }
        JStringAccessor name2(env, name); // throws
        bool is_column_nullable = isNullable != 0 ? true : false;

        DataType dataType = DataType(colType);
        if (is_column_nullable && dataType == type_LinkList) {
            throw invalid_argument("List fields cannot be nullable.");
        }
        return table->add_column(dataType, name2, is_column_nullable);
    });
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeAddColumnLink
  (JNIEnv* env, jobject, jlong nativeTablePtr, jint colType, jstring name, jlong targetTablePtr)
{
    return try_catch<jlong>(env, [&]() {
        Table* table = TBL(nativeTablePtr);
        TABLE_VALID(env, table);
        if (table->has_shared_type()) {
            throw JavaUnsupportedOperation("Not allowed to add field in subtable. Use getSubtableSchema() on root table instead.");
        }
        if (!table->is_group_level()) {
            throw JavaUnsupportedOperation("Links can only be made to toplevel tables.");
        }
        JStringAccessor name2(env, name); // throws
        return table->add_column_link(DataType(colType), name2, *TBL(targetTablePtr));
    });
}

JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativePivot
(JNIEnv *env, jobject, jlong dataTablePtr, jlong stringCol, jlong intCol, jint operation, jlong resultTablePtr)
{
    try_catch<void>(env, [&]() {
        Table* dataTable = TBL(dataTablePtr);
        Table* resultTable = TBL(resultTablePtr);
        Table::AggrType pivotOp;
        switch (operation) {
            case 0:
                pivotOp = Table::aggr_count;
                break;
            case 1:
                pivotOp = Table::aggr_sum;
                break;
            case 2:
                pivotOp = Table::aggr_avg;
                break;
            case 3:
                pivotOp = Table::aggr_min;
                break;
            case 4:
                pivotOp = Table::aggr_max;
                break;
            default:
                throw JavaUnsupportedOperation("No pivot operation specified.");
        }
        dataTable->aggregate(S(stringCol), S(intCol), pivotOp, *resultTable);
    });
}

JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeRemoveColumn
  (JNIEnv *env, jobject, jlong nativeTablePtr, jlong columnIndex)
{
    try_catch<void>(env, [&]() {
        Table* table = TBL(nativeTablePtr);
        TBL_AND_COL_INDEX_VALID(env, table, columnIndex);
        if (table->has_shared_type()) {
            throw JavaUnsupportedOperation(
                    "Not allowed to remove field in subtable. Use getSubtableSchema() on root table instead.");
        }
        table->remove_column(S(columnIndex));
    });
}

JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeRenameColumn
  (JNIEnv *env, jobject, jlong nativeTablePtr, jlong columnIndex, jstring name)
{
    try_catch<void>(env, [&]() {
        Table* table = TBL(nativeTablePtr);
        TBL_AND_COL_INDEX_VALID(env, table, columnIndex);
        if (table->has_shared_type()) {
            throw JavaUnsupportedOperation("Not allowed to rename field in subtable. Use getSubtableSchema() on root table instead.");
        }
        JStringAccessor name2(env, name); // throws
        table->rename_column(S(columnIndex), name2);
    });
}

JNIEXPORT jboolean JNICALL Java_io_realm_internal_Table_nativeIsColumnNullable
  (JNIEnv *env, jobject, jlong nativeTablePtr, jlong columnIndex) {
    return try_catch<jboolean>(env, [&]() {
        Table *table = TBL(nativeTablePtr);
        TBL_AND_COL_INDEX_VALID(env, table, columnIndex);
        if (table->has_shared_type()) {
            throw JavaUnsupportedOperation("Not allowed to convert field in subtable.");
        }
        size_t column_index = S(columnIndex);
        return table->is_nullable(column_index);
    });
}


// General comments about the implementation of
// Java_io_realm_internal_Table_nativeConvertColumnToNullable and Java_io_realm_internal_Table_nativeConvertColumnToNotNullable
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

JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeConvertColumnToNullable
  (JNIEnv *env, jobject, jlong nativeTablePtr, jlong columnIndex)
{
    try_catch<void>(env, [&]() {
        Table *table = TBL(nativeTablePtr);
        TBL_AND_COL_INDEX_VALID(env, table, columnIndex);
        if (table->has_shared_type()) {
            throw JavaUnsupportedOperation("Not allowed to convert field in subtable.");
        }

        size_t column_index = S(columnIndex);
        if (table->is_nullable(column_index)) {
            return; // column is already nullable
        }

        std::string column_name = table->get_column_name(column_index);
        DataType column_type = table->get_column_type(column_index);
        if (column_type == type_Link ||
            column_type == type_LinkList ||
            column_type == type_Mixed ||
            column_type == type_Table) {
            throw invalid_argument("Wrong type - cannot be converted to nullable.");
        }

        std::string tmp_column_name;

        size_t j = 0;
        while (true) {
            std::ostringstream ss;
            ss << std::string("__TMP__") << j;
            std::string str = ss.str();
            StringData sd(str);
            if (table->get_column_index(sd) == realm::not_found) {
                table->insert_column(column_index, column_type, sd, true);
                tmp_column_name = ss.str();
                break;
            }
            j++;
        }

        for (size_t i = 0; i < table->size(); ++i) {
            switch (column_type) {
               case type_String: {
                    // Payload copy is needed
                    StringData sd(table->get_string(column_index + 1, i));
                    table->set_string(column_index, i, sd);
                    break;
                }
                case type_Binary: {
                    // Payload copy is needed
                    BinaryData bd = table->get_binary(column_index + 1, i);
                    std::vector<char> binary_copy(bd.data(), bd.data() + bd.size());
                    table->set_binary(column_index, i, BinaryData(binary_copy.data(), binary_copy.size()));
                    break;
                }
                case type_Int:
                    table->set_int(column_index, i, table->get_int(column_index + 1, i));
                    break;
                case type_Bool:
                    table->set_bool(column_index, i, table->get_bool(column_index + 1, i));
                    break;
                case type_Timestamp:
                    table->set_timestamp(column_index, i, table->get_timestamp(column_index + 1, i));
                    break;
                case type_Float:
                    table->set_float(column_index, i, table->get_float(column_index + 1, i));
                    break;
                case type_Double:
                    table->set_double(column_index, i, table->get_double(column_index + 1, i));
                    break;
                case type_Link:
                case type_LinkList:
                case type_Mixed:
                case type_Table:
                    // checked previously
                    break;
                case type_OldDateTime:
                    throw JavaUnsupportedOperation("The old DateTime type is not supported.");
            }
        }
        if (table->has_search_index(column_index + 1)) {
            table->add_search_index(column_index);
        }
        table->remove_column(column_index + 1);
        table->rename_column(table->get_column_index(tmp_column_name), column_name);
    });
}

JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeConvertColumnToNotNullable
  (JNIEnv *env, jobject, jlong nativeTablePtr, jlong columnIndex)
{
    try_catch<void>(env, [&]() {
        Table *table = TBL(nativeTablePtr);
        TBL_AND_COL_INDEX_VALID(env, table, columnIndex);
        if (table->has_shared_type()) {
            throw JavaUnsupportedOperation("Not allowed to convert field in subtable.");
        }

        size_t column_index = S(columnIndex);
        if (!table->is_nullable(column_index)) {
            return; // column is already not nullable
        }

        std::string column_name = table->get_column_name(column_index);
        DataType column_type = table->get_column_type(column_index);
        if (column_type == type_Link ||
            column_type == type_LinkList ||
            column_type == type_Mixed ||
            column_type == type_Table) {
            throw invalid_argument("Wrong type - cannot be converted to nullable.");
        }

        std::string tmp_column_name;
        size_t j = 0;
        while (true) {
            std::ostringstream ss;
            ss << std::string("__TMP__") << j;
            std::string str = ss.str();
            StringData sd(str);
            if (table->get_column_index(sd) == realm::not_found) {
                table->insert_column(column_index, column_type, sd, false);
                tmp_column_name = ss.str();
                break;
            }
            j++;
        }

        for (size_t i = 0; i < table->size(); ++i) {
            switch (column_type) { // FIXME: respect user-specified default values
                case type_String: {
                    StringData sd = table->get_string(column_index + 1, i);
                    if (sd == realm::null()) {
                        table->set_string(column_index, i, "");
                    }
                    else {
                        // Payload copy is needed
                        table->set_string(column_index, i, sd);
                    }
                    break;
                }
                case type_Binary: {
                    BinaryData bd = table->get_binary(column_index + 1, i);
                    if (bd.is_null()) {
                        table->set_binary(column_index, i, BinaryData(""));
                    }
                    else {
                        // Payload copy is needed
                        std::vector<char> bd_copy(bd.data(), bd.data() + bd.size());
                        table->set_binary(column_index, i, BinaryData(bd_copy.data(), bd_copy.size()));
                    }
                    break;
                }
                case type_Int:
                    if (table->is_null(column_index + 1, i)) {
                        table->set_int(column_index, i, 0);
                    }
                    else {
                        table->set_int(column_index, i, table->get_int(column_index + 1, i));
                    }
                    break;
                case type_Bool:
                    if (table->is_null(column_index + 1, i)) {
                        table->set_bool(column_index, i, false);
                    }
                    else {
                        table->set_bool(column_index, i, table->get_bool(column_index + 1, i));
                    }
                    break;
                case type_Timestamp:
                    if (table->is_null(column_index + 1, i)) {
                        table->set_timestamp(column_index, i, Timestamp(0, 0));
                    }
                    else {
                        table->set_timestamp(column_index, i, table->get_timestamp(column_index + 1, i));
                    }
                    break;
                case type_Float:
                    if (table->is_null(column_index + 1, i)) {
                        table->set_float(column_index, i, 0.0);
                    }
                    else {
                        table->set_float(column_index, i, table->get_float(column_index + 1, i));
                    }
                    break;
                case type_Double:
                    if (table->is_null(column_index + 1, i)) {
                        table->set_double(column_index, i, 0.0);
                    }
                    else {
                        table->set_double(column_index, i, table->get_double(column_index + 1, i));
                    }
                    break;
                case type_Link:
                case type_LinkList:
                case type_Mixed:
                case type_Table:
                    // checked previously
                    break;
                case type_OldDateTime:
                    // not used
                    throw JavaUnsupportedOperation("The old DateTime type is not supported.");
            }
        }
        if (table->has_search_index(column_index + 1)) {
            table->add_search_index(column_index);
        }
        table->remove_column(column_index + 1);
        table->rename_column(table->get_column_index(tmp_column_name), column_name);
    });
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeSize(
    JNIEnv* env, jobject, jlong nativeTablePtr)
{
    return try_catch<jlong>(env, [&]() {
        Table *table = TBL(nativeTablePtr);
        TABLE_VALID(env, table);
        return table->size();
    });
}

JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeClear(
    JNIEnv* env, jobject, jlong nativeTablePtr)
{
    try_catch<void>(env, [&]() {
        Table* table = TBL(nativeTablePtr);
        TABLE_VALID(env, table);
        table->clear();
    });
}


// -------------- Column information


JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeGetColumnCount(
    JNIEnv* env, jobject, jlong nativeTablePtr)
{
    return try_catch<jlong>(env, [&]() {
        Table *table = TBL(nativeTablePtr);
        TABLE_VALID(env, TBL(nativeTablePtr));
        return table->get_column_count();
    });
}

JNIEXPORT jstring JNICALL Java_io_realm_internal_Table_nativeGetColumnName(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex)
{
    return try_catch<jstring>(env, [&]() {
        Table* table = TBL(nativeTablePtr);
        TBL_AND_COL_INDEX_VALID(env, table, columnIndex);
        return to_jstring(env, table->get_column_name(S(columnIndex)));
    });
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeGetColumnIndex(
    JNIEnv* env, jobject, jlong nativeTablePtr, jstring columnName)
{
    return try_catch<jlong>(env, [&]() {
        Table* table = TBL(nativeTablePtr);
        TABLE_VALID(env, table);
        JStringAccessor columnName2(env, columnName);
        return to_jlong_or_not_found(table->get_column_index(columnName2));
    });
    }

JNIEXPORT jint JNICALL Java_io_realm_internal_Table_nativeGetColumnType(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex)
{
    return try_catch<jint>(env, [&]() {
        Table *table = TBL(nativeTablePtr);
        TBL_AND_COL_INDEX_VALID(env, table, columnIndex);
        return static_cast<jint>(table->get_column_type(S(columnIndex)));
    });
}


// ---------------- Row handling

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeAddEmptyRow(
    JNIEnv* env, jclass, jlong nativeTablePtr, jlong rows)
{
    return try_catch<jlong>(env, [&]() {
        Table* pTable = TBL(nativeTablePtr);
        TABLE_VALID(env, pTable);
        if (pTable->get_column_count() < 1){
            throw range_error(concat_stringdata("Table has no columns: ", pTable->get_name()));
        }
        return static_cast<jlong>(pTable->add_empty_row(S(rows)));
    });
}

JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeRemove(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong rowIndex)
{
    try_catch<void>(env, [&]() {
        Table* table = TBL(nativeTablePtr);
        TBL_AND_ROW_INDEX_VALID(env, table, rowIndex);
        table->remove(S(rowIndex));
    });
}

JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeRemoveLast(
    JNIEnv* env, jobject, jlong nativeTablePtr)
{
    try_catch<void>(env, [&]() {
        Table* table = TBL(nativeTablePtr);
        TABLE_VALID(env, table);
        table->remove_last();
    });
}

JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeMoveLastOver
  (JNIEnv *env, jobject, jlong nativeTablePtr, jlong rowIndex)
{
    try_catch<void>(env, [&]() {
        Table* table = TBL(nativeTablePtr);
        TBL_AND_ROW_INDEX_VALID_OFFSET(env, table, rowIndex, false);
        table->move_last_over(S(rowIndex));
    });
}

// ----------------- Get cell

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeGetLong(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex)
{
    return try_catch<jlong>(env, [&]() {
        Table *table = TBL(nativeTablePtr);
        TBL_AND_INDEX_AND_TYPE_VALID(env, table, columnIndex, rowIndex, type_Int);
        return table->get_int(S(columnIndex), S(rowIndex));
    });
}

JNIEXPORT jboolean JNICALL Java_io_realm_internal_Table_nativeGetBoolean(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex)
{
    return try_catch<jboolean>(env, [&]() {
        Table *table = TBL(nativeTablePtr);
        TBL_AND_INDEX_AND_TYPE_VALID(env, table, columnIndex, rowIndex, type_Bool);
        return table->get_bool(S(columnIndex), S(rowIndex));
    });
}

JNIEXPORT jfloat JNICALL Java_io_realm_internal_Table_nativeGetFloat(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex)
{
    return try_catch<jfloat>(env, [&]() {
        Table *table = TBL(nativeTablePtr);
        TBL_AND_INDEX_AND_TYPE_VALID(env, table, columnIndex, rowIndex, type_Float);
        return table->get_float(S(columnIndex), S(rowIndex));
    });
}

JNIEXPORT jdouble JNICALL Java_io_realm_internal_Table_nativeGetDouble(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex)
{
    return try_catch<jdouble>(env, [&]() {
        Table *table = TBL(nativeTablePtr);
        TBL_AND_INDEX_AND_TYPE_VALID(env, table, columnIndex, rowIndex, type_Double);
        return table->get_double(S(columnIndex), S(rowIndex));
    });
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeGetTimestamp(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex)
{
    return try_catch<jlong>(env, [&]() {
        Table *table = TBL(nativeTablePtr);
        TBL_AND_INDEX_AND_TYPE_VALID(env, table, columnIndex, rowIndex, type_Timestamp);
        return to_milliseconds(table->get_timestamp(S(columnIndex), S(rowIndex)));
    });
}

JNIEXPORT jstring JNICALL Java_io_realm_internal_Table_nativeGetString(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex)
{
    return try_catch<jstring>(env, [&]() {
        Table *table = TBL(nativeTablePtr);
        TBL_AND_INDEX_AND_TYPE_VALID(env, table, columnIndex, rowIndex, type_String);
        return to_jstring(env, table->get_string(S(columnIndex), S(rowIndex)));
    });
}

JNIEXPORT jbyteArray JNICALL Java_io_realm_internal_Table_nativeGetByteArray(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex)
{
    return try_catch<jbyteArray>(env, [&]() {
        Table *table = TBL(nativeTablePtr);
        TBL_AND_INDEX_AND_TYPE_VALID(env, table, columnIndex, rowIndex, type_Binary);
        return tbl_GetByteArray<Table>(env, nativeTablePtr, columnIndex, rowIndex);
    });
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeGetLink
  (JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex)
{
    return try_catch<jlong>(env, [&]() {
        Table *table = TBL(nativeTablePtr);
        TBL_AND_INDEX_AND_TYPE_VALID(env, table, columnIndex, rowIndex, type_Link);
        return table->get_link(S(columnIndex), S(rowIndex));
    });
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeGetLinkView
        (JNIEnv* env, jclass, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex)
{
    return try_catch<jlong>(env, [&]() {
        Table* table = TBL(nativeTablePtr);
        TBL_AND_INDEX_AND_TYPE_VALID(env, table, columnIndex, rowIndex, type_LinkList);

        LinkViewRef* link_view_ptr = new LinkViewRef(table->get_linklist(S(columnIndex), S(rowIndex)));
        return reinterpret_cast<jlong>(link_view_ptr);
    });
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeGetLinkTarget
  (JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex)
{
    return try_catch<jlong>(env, [&]() {
        Table* pTable = &(*TBL(nativeTablePtr)->get_link_target(S(columnIndex)));
        LangBindHelper::bind_table_ptr(pTable);
        return reinterpret_cast<jlong>(pTable);
    });
}

JNIEXPORT jboolean JNICALL Java_io_realm_internal_Table_nativeIsNull
        (JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex)
{
    return try_catch<jboolean>(env, [&]() {
        Table* table = TBL(nativeTablePtr);
        return table->is_null(S(columnIndex), S(rowIndex)) ? JNI_TRUE : JNI_FALSE;
    });
}

// ----------------- Set cell

JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeSetLink
  (JNIEnv* env, jclass, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex, jlong targetRowIndex, jboolean isDefault)
{
    try_catch<void>(env, [&]() {
        Table* table = TBL(nativeTablePtr);
        TBL_AND_INDEX_AND_TYPE_INSERT_VALID(env, table, columnIndex, rowIndex, type_Link);
        table->set_link(S(columnIndex), S(rowIndex), S(targetRowIndex), B(isDefault));
    });
}

JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeSetLong(
    JNIEnv* env, jclass, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex, jlong value, jboolean isDefault)
{
    try_catch<void>(env, [&]() {
        Table* table = TBL(nativeTablePtr);
        TBL_AND_INDEX_AND_TYPE_VALID(env, table, columnIndex, rowIndex, type_Int);
        table->set_int(S(columnIndex), S(rowIndex), value, B(isDefault));
    });
}

JNIEXPORT void JNICALL
Java_io_realm_internal_Table_nativeSetLongUnique(JNIEnv *env, jclass, jlong nativeTablePtr, jlong columnIndex,
                                                 jlong rowIndex, jlong value)
{
    try_catch<void>(env, [&]() {
        Table* table = TBL(nativeTablePtr);
        TBL_AND_INDEX_AND_TYPE_VALID(env, table, columnIndex, rowIndex, type_Int);
        table->set_int_unique( S(columnIndex), S(rowIndex), value);
    });
}

JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeSetBoolean(
    JNIEnv* env, jclass, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex, jboolean value, jboolean isDefault)
{
    try_catch<void>(env, [&]() {
        Table* table = TBL(nativeTablePtr);
        TBL_AND_INDEX_AND_TYPE_VALID(env, table, columnIndex, rowIndex, type_Bool);
        table->set_bool(S(columnIndex), S(rowIndex), B(value), B(isDefault));
    });
}

JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeSetFloat(
    JNIEnv* env, jclass, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex, jfloat value, jboolean isDefault)
{
    try_catch<void>(env, [&]() {
        Table* table = TBL(nativeTablePtr);
        TBL_AND_INDEX_AND_TYPE_VALID(env, table, columnIndex, rowIndex, type_Float);
        table->set_float(S(columnIndex), S(rowIndex), value, B(isDefault));
    });
}

JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeSetDouble(
    JNIEnv* env, jclass, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex, jdouble value, jboolean isDefault)
{
    try_catch<void>(env, [&]() {
        Table* table = TBL(nativeTablePtr);
        TBL_AND_INDEX_AND_TYPE_VALID(env, table, columnIndex, rowIndex, type_Double);
        table->set_double(S(columnIndex), S(rowIndex), value, B(isDefault));
    });
}

JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeSetString(
    JNIEnv* env, jclass, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex, jstring value, jboolean isDefault)
{
    try_catch<void>(env, [&]() {
        Table* table = TBL(nativeTablePtr);
        TBL_AND_INDEX_AND_TYPE_VALID(env, table, columnIndex, rowIndex, type_String);

        if (value == NULL) {
            TBL_AND_COL_NULLABLE(env, table, columnIndex);
        }
        JStringAccessor value2(env, value);
        table->set_string(S(columnIndex), S(rowIndex), value2, B(isDefault));
    });
}

JNIEXPORT void JNICALL
Java_io_realm_internal_Table_nativeSetStringUnique(JNIEnv *env, jclass, jlong nativeTablePtr, jlong columnIndex,
                                                   jlong rowIndex, jstring value)
{
    try_catch<void>(env, [&]() {
        Table* table = TBL(nativeTablePtr);
        TBL_AND_INDEX_AND_TYPE_VALID(env, table, columnIndex, rowIndex, type_String);

        if (value == NULL) {
            TBL_AND_COL_NULLABLE(env, table, columnIndex);
            table->set_string_unique(S(columnIndex), S(rowIndex), null{});
        } else {
            JStringAccessor value2(env, value);
            table->set_string_unique(S(columnIndex), S(rowIndex), value2);
        }
    });
}

JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeSetTimestamp(
    JNIEnv* env, jclass, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex, jlong timestampValue, jboolean isDefault)
{
    try_catch<void>(env, [&]() {
        Table* table = TBL(nativeTablePtr);
        TBL_AND_INDEX_AND_TYPE_VALID(env, table, columnIndex, rowIndex, type_Timestamp);
        table->set_timestamp(S(columnIndex), S(rowIndex), from_milliseconds(timestampValue), B(isDefault));
    });
}

JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeSetByteArray(
    JNIEnv* env, jclass, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex, jbyteArray dataArray, jboolean isDefault)
{
    try_catch<void>(env, [&]() {
        Table* table = TBL(nativeTablePtr);
        TBL_AND_INDEX_AND_TYPE_VALID(env, table, columnIndex, rowIndex, type_Binary);
        if (dataArray == NULL) {
            TBL_AND_COL_NULLABLE(env, table, columnIndex);

        }
        JniByteArray byteAccessor(env, dataArray);
        table->set_binary(S(columnIndex), S(rowIndex), byteAccessor, B(isDefault));
    });
}

JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeSetNull(
    JNIEnv* env, jclass, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex, jboolean isDefault)
{
    try_catch<void>(env, [&]() {
        Table* pTable = TBL(nativeTablePtr);
        TBL_AND_COL_INDEX_VALID(env, pTable, columnIndex);
        TBL_AND_ROW_INDEX_VALID(env, pTable, rowIndex);
        TBL_AND_COL_NULLABLE(env, pTable, columnIndex);
        pTable->set_null(S(columnIndex), S(rowIndex), B(isDefault));
    });
}

JNIEXPORT void JNICALL
Java_io_realm_internal_Table_nativeSetNullUnique(JNIEnv *env, jclass, jlong nativeTablePtr, jlong columnIndex,
                                                 jlong rowIndex)
{
    try_catch<void>(env, [&]() {
        Table* pTable = TBL(nativeTablePtr);
        TBL_AND_COL_INDEX_VALID(env, pTable, columnIndex);
        TBL_AND_ROW_INDEX_VALID(env, pTable, rowIndex);
        TBL_AND_COL_NULLABLE(env, pTable, columnIndex);
        pTable->set_null_unique(S(columnIndex), S(rowIndex));
    });
}


JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeGetRowPtr
  (JNIEnv* env, jobject, jlong nativeTablePtr, jlong index)
{
    return try_catch<jlong>(env, [&]() {
        Row* row = new Row((*TBL(nativeTablePtr))[S(index)]);
        return reinterpret_cast<jlong>(row);
    });
}

//--------------------- Indexing methods:

JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeAddSearchIndex(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex)
{
    try_catch<void>(env, [&]() {
        Table* pTable = TBL(nativeTablePtr);
        TBL_AND_COL_INDEX_VALID(env, pTable, columnIndex);

        DataType column_type = pTable->get_column_type (S(columnIndex));
        is_allowed_to_index(env, column_type);

        pTable->add_search_index( S(columnIndex));
    });
}

JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeRemoveSearchIndex(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex)
{
    try_catch<void>(env, [&]() {
        Table* pTable = TBL(nativeTablePtr);
        TBL_AND_COL_INDEX_VALID(env, pTable, columnIndex);
        DataType column_type = pTable->get_column_type (S(columnIndex));
        is_allowed_to_index(env, column_type);
        pTable->remove_search_index( S(columnIndex));
    });
}

JNIEXPORT jboolean JNICALL Java_io_realm_internal_Table_nativeHasSearchIndex(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex)
{
    return try_catch<jboolean>(env, [&]() {
        Table* table = TBL(nativeTablePtr);
        TBL_AND_COL_INDEX_VALID(env, table, columnIndex);
        return table->has_search_index(S(columnIndex));
    });
}

JNIEXPORT jboolean JNICALL Java_io_realm_internal_Table_nativeIsNullLink
  (JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex)
{
    return try_catch<jboolean>(env, [&]() {
        Table *table = TBL(nativeTablePtr);
        TBL_AND_INDEX_AND_TYPE_VALID(env, table, columnIndex, rowIndex, type_Link);
        return table->is_null_link(S(columnIndex), S(rowIndex));
    });
}

JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeNullifyLink
  (JNIEnv* env, jclass, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex)
{
    try_catch<void>(env, [&]() {
        Table* table = TBL(nativeTablePtr);
        TBL_AND_INDEX_AND_TYPE_VALID(env, table, columnIndex, rowIndex, type_Link);
        table->nullify_link(S(columnIndex), S(rowIndex));
    });
}

//---------------------- Aggregate methods for integers

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeSumInt(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex)
{
    return try_catch<jlong>(env, [&]() {
        Table* table = TBL(nativeTablePtr);
        TBL_AND_COL_INDEX_AND_TYPE_VALID(env, table, columnIndex, type_Int);
        return table->sum_int( S(columnIndex));
    });
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeMaximumInt(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex)
{
    return try_catch<jlong>(env, [&]() {
        Table* table = TBL(nativeTablePtr);
        TBL_AND_COL_INDEX_AND_TYPE_VALID(env, table, columnIndex, type_Int);
        return table->maximum_int( S(columnIndex));
    });
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeMinimumInt(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex)
{
    return try_catch<jlong>(env, [&]() {
        Table* table = TBL(nativeTablePtr);
        TBL_AND_COL_INDEX_AND_TYPE_VALID(env, table, columnIndex, type_Int);
        return table->minimum_int( S(columnIndex));
    });
}

JNIEXPORT jdouble JNICALL Java_io_realm_internal_Table_nativeAverageInt(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex)
{
    return try_catch<jdouble>(env, [&]() {
        Table* table = TBL(nativeTablePtr);
        TBL_AND_COL_INDEX_AND_TYPE_VALID(env, table, columnIndex, type_Int);
        return table->average_int(S(columnIndex));
    });
}

//--------------------- Aggregate methods for float

JNIEXPORT jdouble JNICALL Java_io_realm_internal_Table_nativeSumFloat(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex)
{
    return try_catch<jdouble>(env, [&]() {
        Table* table = TBL(nativeTablePtr);
        TBL_AND_COL_INDEX_AND_TYPE_VALID(env, table, columnIndex, type_Float);
        return table->sum_float(S(columnIndex));
    });
}

JNIEXPORT jfloat JNICALL Java_io_realm_internal_Table_nativeMaximumFloat(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex)
{
    return try_catch<jfloat>(env, [&]() {
        Table* table = TBL(nativeTablePtr);
        TBL_AND_COL_INDEX_AND_TYPE_VALID(env, table, columnIndex, type_Float);
        return table->maximum_float(S(columnIndex));
    });
}

JNIEXPORT jfloat JNICALL Java_io_realm_internal_Table_nativeMinimumFloat(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex)
{
    return try_catch<jfloat>(env, [&]() {
        Table* table = TBL(nativeTablePtr);
        TBL_AND_COL_INDEX_AND_TYPE_VALID(env, table, columnIndex, type_Float);
        return table->minimum_float(S(columnIndex));
    });
}

JNIEXPORT jdouble JNICALL Java_io_realm_internal_Table_nativeAverageFloat(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex)
{
    return try_catch<jdouble>(env, [&]() {
        Table* table = TBL(nativeTablePtr);
        TBL_AND_COL_INDEX_AND_TYPE_VALID(env, table, columnIndex, type_Float);
        return table->average_float(S(columnIndex));
    });
}


//--------------------- Aggregate methods for double

JNIEXPORT jdouble JNICALL Java_io_realm_internal_Table_nativeSumDouble(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex)
{
    return try_catch<jdouble>(env, [&]() {
        Table* table = TBL(nativeTablePtr);
        TBL_AND_COL_INDEX_AND_TYPE_VALID(env, table, columnIndex, type_Double);
        return table->sum_double(S(columnIndex));
    });
}

JNIEXPORT jdouble JNICALL Java_io_realm_internal_Table_nativeMaximumDouble(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex)
{
    return try_catch<jdouble>(env, [&]() {
        Table* table = TBL(nativeTablePtr);
        TBL_AND_COL_INDEX_AND_TYPE_VALID(env, table, columnIndex, type_Double);
        return table->maximum_double(S(columnIndex));
    });
}

JNIEXPORT jdouble JNICALL Java_io_realm_internal_Table_nativeMinimumDouble(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex)
{
    return try_catch<jdouble>(env, [&]() {
        Table* table = TBL(nativeTablePtr);
        TBL_AND_COL_INDEX_AND_TYPE_VALID(env, table, columnIndex, type_Double);
        return table->minimum_double( S(columnIndex));
    });
}

JNIEXPORT jdouble JNICALL Java_io_realm_internal_Table_nativeAverageDouble(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex)
{
    return try_catch<jdouble>(env, [&]() {
        Table* table = TBL(nativeTablePtr);
        TBL_AND_COL_INDEX_AND_TYPE_VALID(env, table, columnIndex, type_Double);
        return table->average_double(S(columnIndex));
    });
}


//--------------------- Aggregate methods for date

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeMaximumTimestamp(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex)
{
    return try_catch<jlong>(env, [&]() {
        Table* table = TBL(nativeTablePtr);
        TBL_AND_COL_INDEX_AND_TYPE_VALID(env, table, columnIndex, type_Timestamp);
        return to_milliseconds(table->maximum_timestamp(S(columnIndex)));
    });
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeMinimumTimestamp(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex)
{
    return try_catch<jlong>(env, [&]() {
        Table* table = TBL(nativeTablePtr);
        TBL_AND_COL_INDEX_AND_TYPE_VALID(env, table, columnIndex, type_Timestamp);
        return to_milliseconds(table->minimum_timestamp(S(columnIndex)));
    });
}

//---------------------- Count

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeCountLong(
    JNIEnv *env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong value)
{
    return try_catch<jlong>(env, [&]() {
        Table* table = TBL(nativeTablePtr);
        TBL_AND_COL_INDEX_AND_TYPE_VALID(env, table, columnIndex, type_Int);
        return table->count_int(S(columnIndex), value);
    });
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeCountFloat(
    JNIEnv *env, jobject, jlong nativeTablePtr, jlong columnIndex, jfloat value)
{
    return try_catch<jlong>(env, [&]() {
        Table* table = TBL(nativeTablePtr);
        TBL_AND_COL_INDEX_AND_TYPE_VALID(env, table, columnIndex, type_Float);
        return table->count_float(S(columnIndex), value);
    });
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeCountDouble(
    JNIEnv *env, jobject, jlong nativeTablePtr, jlong columnIndex, jdouble value)
{
    return try_catch<jlong>(env, [&]() {
        Table* table = TBL(nativeTablePtr);
        TBL_AND_COL_INDEX_AND_TYPE_VALID(env, table, columnIndex, type_Double);
        return table->count_double(S(columnIndex), value);
    });
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeCountString(
    JNIEnv *env, jobject, jlong nativeTablePtr, jlong columnIndex, jstring value)
{
    return try_catch<jlong>(env, [&]() {
        Table* table = TBL(nativeTablePtr);
        TBL_AND_COL_INDEX_AND_TYPE_VALID(env, table, columnIndex, type_String);
        JStringAccessor value2(env, value);
        return table->count_string( S(columnIndex), value2);
    });
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeWhere(
    JNIEnv *env, jobject, jlong nativeTablePtr)
{
    return try_catch<jlong>(env, [&]() {
        Table* table = TBL(nativeTablePtr);
        TABLE_VALID(env, table);
        Query *queryPtr = new Query(table->where());
        return reinterpret_cast<jlong>(queryPtr);
    });
}

//----------------------- FindFirst

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeFindFirstInt(
    JNIEnv* env, jclass, jlong nativeTablePtr, jlong columnIndex, jlong value)
{
    return try_catch<jlong>(env, [&]() {
        Table* table = TBL(nativeTablePtr);
        TBL_AND_COL_INDEX_AND_TYPE_VALID(env, table, columnIndex, type_Int);
        return to_jlong_or_not_found(table->find_first_int(S(columnIndex), value));
    });
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeFindFirstBool(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jboolean value)
{
    return try_catch<jlong>(env, [&]() {
        Table* table = TBL(nativeTablePtr);
        TBL_AND_COL_INDEX_AND_TYPE_VALID(env, table, columnIndex, type_Bool);
        return to_jlong_or_not_found(table->find_first_bool(S(columnIndex), value != 0 ? true : false));
    });
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeFindFirstFloat(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jfloat value)
{
    return try_catch<jlong>(env, [&]() {
        Table* table = TBL(nativeTablePtr);
        TBL_AND_COL_INDEX_AND_TYPE_VALID(env, table, columnIndex, type_Float);
        return to_jlong_or_not_found(table->find_first_float(S(columnIndex), value));
    });
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeFindFirstDouble(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jdouble value)
{
    return try_catch<jlong>(env, [&]() {
        Table* table = TBL(nativeTablePtr);
        TBL_AND_COL_INDEX_AND_TYPE_VALID(env, table, columnIndex, type_Double);
        return to_jlong_or_not_found(table->find_first_double(S(columnIndex), value));
    });
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeFindFirstTimestamp(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong dateTimeValue)
{
    return try_catch<jlong>(env, [&]() {
        Table* table = TBL(nativeTablePtr);
        TBL_AND_COL_INDEX_AND_TYPE_VALID(env, table, columnIndex, type_Timestamp);
        size_t res = table->find_first_timestamp(S(columnIndex), from_milliseconds(dateTimeValue));
        return to_jlong_or_not_found(res);
    });
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeFindFirstString(
    JNIEnv* env, jclass, jlong nativeTablePtr, jlong columnIndex, jstring value)
{
    return try_catch<jlong>(env, [&]() {
        Table* table = TBL(nativeTablePtr);
        TBL_AND_COL_INDEX_AND_TYPE_VALID(env, table, columnIndex, type_String);
        JStringAccessor value2(env, value);
        return to_jlong_or_not_found(table->find_first_string(S(columnIndex), value2));
    });
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeFindFirstNull(
    JNIEnv* env, jclass, jlong nativeTablePtr, jlong columnIndex)
{
    return try_catch<jlong>(env, [&]() {
        Table* pTable = TBL(nativeTablePtr);
        TBL_AND_COL_INDEX_VALID(env, pTable, columnIndex);
        TBL_AND_COL_NULLABLE(env, pTable, columnIndex);
        return to_jlong_or_not_found( pTable->find_first_null( S(columnIndex) ) );
    });
    return jlong(-1);
}

// FindAll

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeFindAllInt(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong value)
{
    return try_catch<jlong>(env, [&]() {
        Table* table = TBL(nativeTablePtr);
        TBL_AND_COL_INDEX_AND_TYPE_VALID(env, table, columnIndex, type_Int);
        TableView* pTableView = new TableView(table->find_all_int(S(columnIndex), value));
        return reinterpret_cast<jlong>(pTableView);
    });
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeFindAllFloat(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jfloat value)
{
    return try_catch<jlong>(env, [&]() {
        Table* table = TBL(nativeTablePtr);
        TBL_AND_COL_INDEX_AND_TYPE_VALID(env, table, columnIndex, type_Float);
        TableView* pTableView = new TableView(table->find_all_float( S(columnIndex), value));
        return reinterpret_cast<jlong>(pTableView);
    });
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeFindAllDouble(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jdouble value)
{
    return try_catch<jlong>(env, [&]() {
        Table* table = TBL(nativeTablePtr);
        TBL_AND_COL_INDEX_AND_TYPE_VALID(env, table, columnIndex, type_Double);
        TableView* pTableView = new TableView(table->find_all_double(S(columnIndex), value));
        return reinterpret_cast<jlong>(pTableView);
    });
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeFindAllBool(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jboolean value)
{
    return try_catch<jlong>(env, [&]() {
        Table *table = TBL(nativeTablePtr);
        TBL_AND_COL_INDEX_AND_TYPE_VALID(env, table, columnIndex, type_Bool);

        TableView *pTableView = new TableView(table->find_all_bool(S(columnIndex),
                                                                   value != 0 ? true : false));
        return reinterpret_cast<jlong>(pTableView);
    });
}

// FIXME: reenable when find_first_timestamp() is implemented
/*
JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeFindAllTimestamp(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong dateTimeValue)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, table, columnIndex, type_Timestamp))
        return 0;
    try {
        TableView* pTableView = new TableView(table->find_all_timestamp(S(columnIndex), from_milliseconds(dateTimeValue)));
        return reinterpret_cast<jlong>(pTableView);
    });
    return 0;
}
*/

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeFindAllString(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jstring value)
{
    return try_catch<jlong>(env, [&]() {
        Table* table = TBL(nativeTablePtr);
        TBL_AND_COL_INDEX_AND_TYPE_VALID(env, table, columnIndex, type_String);

        JStringAccessor value2(env, value);
        TableView* pTableView = new TableView(table->find_all_string(S(columnIndex), value2));
        return reinterpret_cast<jlong>(pTableView);
    });
}


// experimental
JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeLowerBoundInt(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong value)
{
    return try_catch<jlong>(env, [&]() {
        Table* table = TBL(nativeTablePtr);
        TBL_AND_COL_INDEX_AND_TYPE_VALID(env, table, columnIndex, type_Int);
        return table->lower_bound_int(S(columnIndex), S(value));
    });
}


// experimental
JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeUpperBoundInt(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong value)
{
    return try_catch<jlong>(env, [&]() {
        Table* table = TBL(nativeTablePtr);
        TBL_AND_COL_INDEX_AND_TYPE_VALID(env, table, columnIndex, type_Int);
        return table->upper_bound_int(S(columnIndex), S(value));
    });
}


JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeGetDistinctView(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex)
{
    return try_catch<jlong>(env, [&]() {
        Table *table = TBL(nativeTablePtr);
        TBL_AND_COL_INDEX_VALID(env, table, columnIndex);
        if (!table->has_search_index(S(columnIndex))) {
            throw JavaUnsupportedOperation("The field must be indexed before distinct() can be used.");
        }
        switch (table->get_column_type(S(columnIndex))) {
            case type_Bool:
            case type_Int:
            case type_String:
            case type_Timestamp: {
                TableView *pTableView = new TableView(table->get_distinct_view(S(columnIndex)));
                return reinterpret_cast<jlong>(pTableView);
            };
            default:
                throw invalid_argument(
                        "Invalid type - Only String, Date, boolean, byte, short, int, long and their boxed variants are supported.");
        }
    });
}


JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeGetSortedViewMulti(
   JNIEnv *env, jobject, jlong nativeTablePtr, jlongArray columnIndices, jbooleanArray ascending)
{
    return try_catch<jlong>(env, [&]() {
        Table* pTable = TBL(nativeTablePtr);

        JniLongArray long_arr(env, columnIndices);
        JniBooleanArray bool_arr(env, ascending);
        jsize arr_len = long_arr.len();
        jsize asc_len = bool_arr.len();

        if (arr_len == 0) {
            throw invalid_argument("You must provide at least one field name.");
        }
        if (asc_len == 0) {
            throw invalid_argument("You must provide at least one sort order.");
        }
        if (arr_len != asc_len) {
            throw invalid_argument("Number of column indices and sort orders do not match.");
        }

        std::vector<std::vector<size_t>> indices(S(arr_len));
        std::vector<bool> ascendings(S(arr_len));

        for (int i = 0; i < arr_len; ++i) {
            TBL_AND_COL_INDEX_VALID(env, pTable, S(long_arr[i]));
            int colType = pTable->get_column_type( S(long_arr[i]) );
            switch (colType) {
                case type_Int:
                case type_Bool:
                case type_String:
                case type_Double:
                case type_Float:
                case type_Timestamp:
                    indices[i] = std::vector<size_t> { S(long_arr[i]) };
                    ascendings[i] = S(bool_arr[i]);
                    break;
                default:
                    throw invalid_argument("Sort is only support on String, Date, boolean, byte, short, int, long and their boxed variants.");
            }
        }

        TableView* pTableView = new TableView(pTable->get_sorted_view(SortDescriptor(*pTable, indices, ascendings)));
        return reinterpret_cast<jlong>(pTableView);
    });
}

JNIEXPORT jstring JNICALL Java_io_realm_internal_Table_nativeGetName(
    JNIEnv *env, jobject, jlong nativeTablePtr)
{
    return try_catch<jstring>(env, [&]() {
        Table* table = TBL(nativeTablePtr);
        TABLE_VALID(env, table);
        return to_jstring(env, table->get_name());
    });
}

JNIEXPORT jstring JNICALL Java_io_realm_internal_Table_nativeToJson(
    JNIEnv *env, jobject, jlong nativeTablePtr)
{
    return try_catch<jstring>(env, [&]() {
        Table* table = TBL(nativeTablePtr);
        TABLE_VALID(env, table);

        // Write table to string in JSON format
        ostringstream ss;
        ss.sync_with_stdio(false); // for performance
        table->to_json(ss);
        const string str = ss.str();
        return to_jstring(env, str);
    });
}

JNIEXPORT jboolean JNICALL Java_io_realm_internal_Table_nativeIsValid(
    JNIEnv* env, jobject, jlong nativeTablePtr)
{
    return try_catch<jboolean>(env, [&]() {
        Table *table = TBL(nativeTablePtr);
        return table->is_attached();
    });
}

JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeClose(
    JNIEnv* env, jclass, jlong nativeTablePtr)
{
    TR_ENTER_PTR(env, nativeTablePtr)
    try_catch<void>(env, [&]() {
        LangBindHelper::unbind_table_ptr(TBL(nativeTablePtr));
    });
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_createNative(JNIEnv *env, jobject)
{
    TR_ENTER(env)
    return try_catch<jlong>(env, [&]() {
        return reinterpret_cast<jlong>(LangBindHelper::new_table());
    });
}

// Throws invalid_argument if the primary key column contains any duplicate values, making it
// ineligible as a primary key.
static void check_valid_primary_key_column(JNIEnv* env, Table* table, StringData column_name) // throws
{
    size_t column_index = table->get_column_index(column_name);
    if (column_index == realm::not_found) {
        std::ostringstream error_msg;
        error_msg << table->get_name() << " does not contain the field \"" << column_name << "\"";
        throw invalid_argument(error_msg.str());
    }
    DataType column_type = table->get_column_type(column_index);
    TableView results = table->get_sorted_view(column_index);

    switch(column_type) {
        case type_Int:
            if (results.size() > 1) {
                int64_t val = results.get_int(column_index, 0);
                for (size_t i = 1; i < results.size(); i++) {
                    int64_t next_val = results.get_int(column_index, i);
                    if (val == next_val) {
                        std::ostringstream error_msg;
                        error_msg << "Field \"" << column_name << "\" cannot be a primary key, ";
                        error_msg << "it already contains duplicate values: " << val;
                        throw invalid_argument(error_msg.str());
                    }
                    else {
                        val = next_val;
                    }
                }
            }
            return;

        case type_String:
            if (results.size() > 1) {
                string str = results.get_string(column_index, 0);
                for (size_t i = 1; i < results.size(); i++) {
                    string next_str = results.get_string(column_index, i);
                    if (str.compare(next_str) == 0) {
                        std::ostringstream error_msg;
                        error_msg << "Field \"" << column_name << "\" cannot be a primary key, ";
                        error_msg << "it already contains duplicate values: " << str;
                        throw invalid_argument(error_msg.str());
                    }
                    else {
                        str = next_str;
                    }
                }
            }
            return;

        default:
            throw invalid_argument("Invalid primary key type: " + column_type);
    }
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeSetPrimaryKey(
    JNIEnv* env, jobject, jlong nativePrivateKeyTablePtr, jlong nativeTablePtr, jstring columnName)
{
    return try_catch<jlong>(env, [&]() {
        Table* table = TBL(nativeTablePtr);
        Table* pk_table = TBL(nativePrivateKeyTablePtr);
        const std::string table_name(table->get_name().substr(TABLE_PREFIX.length())); // Remove "class_" prefix
        size_t row_index = pk_table->find_first_string(io_realm_internal_Table_PRIMARY_KEY_CLASS_COLUMN_INDEX, table_name);

        if (columnName == NULL || env->GetStringLength(columnName) == 0) {
            // No primary key provided => remove previous set keys
            if (row_index != realm::not_found) {
                pk_table->remove(row_index);
            }
            return static_cast<jlong>(io_realm_internal_Table_NO_PRIMARY_KEY);
        }
        else {
            JStringAccessor new_primary_key_column_name(env, columnName);
            size_t primary_key_column_index = table->get_column_index(new_primary_key_column_name);
            if (row_index == realm::not_found) {
                // No primary key is currently set
                check_valid_primary_key_column(env, table, new_primary_key_column_name);
                row_index = pk_table->add_empty_row();
                pk_table->set_string_unique(io_realm_internal_Table_PRIMARY_KEY_CLASS_COLUMN_INDEX, row_index, table_name);
                pk_table->set_string(io_realm_internal_Table_PRIMARY_KEY_FIELD_COLUMN_INDEX, row_index, new_primary_key_column_name);
            }
            else {
                // Primary key already exists
                // We only wish to check for duplicate values if a column isn't already a primary key
                Row* row = new Row((*pk_table)[row_index]);
                StringData current_primary_key = row->get_string(io_realm_internal_Table_PRIMARY_KEY_FIELD_COLUMN_INDEX);
                if (new_primary_key_column_name != current_primary_key) {
                    check_valid_primary_key_column(env, table, new_primary_key_column_name);
                    pk_table->set_string(io_realm_internal_Table_PRIMARY_KEY_FIELD_COLUMN_INDEX, row_index, new_primary_key_column_name);
                }
            }

            return static_cast<jlong>(primary_key_column_index);
        }
    });
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
// 3) PK table's column 'pk_table' needs search index in order to use set_string_unique.
// This affects:
// - All Realms created by Cocoa and used by Realm-java before 2.0.0
// See https://github.com/realm/realm-java/pull/3488

// This methods converts the old (wrong) table format (string, integer) to the right (string,string) format and strips
// any class names in the col[0] of their "class_" prefix
JNIEXPORT jboolean JNICALL Java_io_realm_internal_Table_nativeMigratePrimaryKeyTableIfNeeded
    (JNIEnv* env, jclass, jlong groupNativePtr, jlong privateKeyTableNativePtr)
{
    return try_catch<jboolean>(env, [&]() {
        const size_t CLASS_COLUMN_INDEX = io_realm_internal_Table_PRIMARY_KEY_CLASS_COLUMN_INDEX;
        const size_t FIELD_COLUMN_INDEX = io_realm_internal_Table_PRIMARY_KEY_FIELD_COLUMN_INDEX;

        auto group = reinterpret_cast<Group *>(groupNativePtr);
        Table *pk_table = TBL(privateKeyTableNativePtr);
        jboolean changed = JNI_FALSE;

        // Fix wrong types (string, int) -> (string, string)
        if (pk_table->get_column_type(FIELD_COLUMN_INDEX) == type_Int) {
            StringData tmp_col_name = StringData("tmp_field_name");
            size_t tmp_col_ndx = pk_table->add_column(DataType(type_String), tmp_col_name);

            // Create tmp string column with field name instead of column index
            size_t number_of_rows = pk_table->size();
            for (size_t row_ndx = 0; row_ndx < number_of_rows; row_ndx++) {
                StringData table_name = pk_table->get_string(CLASS_COLUMN_INDEX, row_ndx);
                size_t col_ndx = static_cast<size_t>(pk_table->get_int(FIELD_COLUMN_INDEX, row_ndx));
                StringData col_name = group->get_table(table_name)->get_column_name(col_ndx);
                // Make a copy of the string
                pk_table->set_string(tmp_col_ndx, row_ndx, col_name);
            }

            // Delete old int column, and rename tmp column to same name
            // The column index for the renamed column will then be the same as the deleted old column
            pk_table->remove_column(FIELD_COLUMN_INDEX);
            pk_table->rename_column(pk_table->get_column_index(tmp_col_name), StringData("pk_property"));
            changed = JNI_TRUE;
        }

        // If needed remove "class_" prefix from class names
        size_t number_of_rows = pk_table->size();
        for (size_t row_ndx = 0; row_ndx < number_of_rows; row_ndx++) {
            StringData table_name = pk_table->get_string(CLASS_COLUMN_INDEX, row_ndx);
            if (table_name.begins_with(TABLE_PREFIX)) {
                // New string copy is needed, since the original memory will be changed.
                std::string str(table_name.substr(TABLE_PREFIX.length()));
                StringData sd(str);
                pk_table->set_string(CLASS_COLUMN_INDEX, row_ndx, sd);
                changed = JNI_TRUE;
            }
        }

        // From realm-java 2.0.0, pk table's class column requires a search index.
        if (!pk_table->has_search_index(CLASS_COLUMN_INDEX)) {
            pk_table->add_search_index(CLASS_COLUMN_INDEX);
            changed = JNI_TRUE;
        }
        return changed;
    });
}

JNIEXPORT jboolean JNICALL
Java_io_realm_internal_Table_nativePrimaryKeyTableNeedsMigration(JNIEnv* env, jclass, jlong primaryKeyTableNativePtr) {
    return try_catch<jboolean>(env, [&]() {
        const size_t CLASS_COLUMN_INDEX = io_realm_internal_Table_PRIMARY_KEY_CLASS_COLUMN_INDEX;
        const size_t FIELD_COLUMN_INDEX = io_realm_internal_Table_PRIMARY_KEY_FIELD_COLUMN_INDEX;

        Table *pk_table = TBL(primaryKeyTableNativePtr);

        // Fix wrong types (string, int) -> (string, string)
        if (pk_table->get_column_type(FIELD_COLUMN_INDEX) == type_Int) {
            return JNI_TRUE;
        }

        // If needed remove "class_" prefix from class names
        size_t number_of_rows = pk_table->size();
        for (size_t row_ndx = 0; row_ndx < number_of_rows; row_ndx++) {
            StringData table_name = pk_table->get_string(CLASS_COLUMN_INDEX, row_ndx);
            if (table_name.begins_with(TABLE_PREFIX)) {
                return JNI_TRUE;
            }
        }
        // From realm-java 2.0.0, pk table's class column requires a search index.
        if (!pk_table->has_search_index(CLASS_COLUMN_INDEX)) {
            return JNI_TRUE;
        }
        return JNI_FALSE;
    });
}

JNIEXPORT jboolean JNICALL Java_io_realm_internal_Table_nativeHasSameSchema
  (JNIEnv* env, jobject, jlong thisTablePtr, jlong otherTablePtr)
{
    return try_catch<jboolean>(env, [&]() {
        return *TBL(thisTablePtr)->get_descriptor() == *TBL(otherTablePtr)->get_descriptor();
    });
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeVersion(
        JNIEnv* env, jobject, jlong nativeTablePtr)
{
    return try_catch<jlong>(env, [&]() {
        Table* table = TBL(nativeTablePtr);
        bool valid = (table != NULL);
        if (valid) {
            if (!table->is_attached()) {
                throw JavaIllegalState("The Realm has been closed and is no longer accessible.");
            }
        }
        return static_cast<jlong>(table->get_version_counter());
    });
}
