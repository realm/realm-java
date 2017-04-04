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
#include "io_realm_internal_Table.h"
#include "tablebase_tpl.hpp"

using namespace std;
using namespace realm;

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
    if (!TABLE_VALID(env, TBL(nativeTablePtr))) {
        return 0;
    }
    if (TBL(nativeTablePtr)->has_shared_type()) {
        ThrowException(env, UnsupportedOperation,
                       "Not allowed to add field in subtable. Use getSubtableSchema() on root table instead.");
        return 0;
    }
    try {
        JStringAccessor name2(env, name); // throws
        bool is_column_nullable = to_bool(isNullable);

        DataType dataType = DataType(colType);
        if (is_column_nullable && dataType == type_LinkList) {
            ThrowException(env, IllegalArgument, "List fields cannot be nullable.");
        }
        return static_cast<jlong>(TBL(nativeTablePtr)->add_column(dataType, name2, is_column_nullable));
    }
    CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeAddColumnLink(JNIEnv* env, jobject, jlong nativeTablePtr,
                                                                         jint colType, jstring name,
                                                                         jlong targetTablePtr)
{
    if (!TABLE_VALID(env, TBL(nativeTablePtr))) {
        return 0;
    }
    if (TBL(nativeTablePtr)->has_shared_type()) {
        ThrowException(env, UnsupportedOperation,
                       "Not allowed to add field in subtable. Use getSubtableSchema() on root table instead.");
        return 0;
    }
    if (!TBL(targetTablePtr)->is_group_level()) {
        ThrowException(env, UnsupportedOperation, "Links can only be made to toplevel tables.");
        return 0;
    }
    try {
        JStringAccessor name2(env, name); // throws
        return static_cast<jlong>(TBL(nativeTablePtr)->add_column_link(DataType(colType), name2, *TBL(targetTablePtr)));
    }
    CATCH_STD()
    return 0;
}


JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativePivot(JNIEnv* env, jobject, jlong dataTablePtr,
                                                                jlong stringCol, jlong intCol, jint operation,
                                                                jlong resultTablePtr)
{
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
            ThrowException(env, UnsupportedOperation, "No pivot operation specified.");
            return;
    }

    try {
        dataTable->aggregate(S(stringCol), S(intCol), pivotOp, *resultTable);
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeRemoveColumn(JNIEnv* env, jobject, jlong nativeTablePtr,
                                                                       jlong columnIndex)
{
    if (!TBL_AND_COL_INDEX_VALID(env, TBL(nativeTablePtr), columnIndex)) {
        return;
    }
    if (TBL(nativeTablePtr)->has_shared_type()) {
        ThrowException(env, UnsupportedOperation,
                       "Not allowed to remove field in subtable. Use getSubtableSchema() on root table instead.");
        return;
    }
    try {
        TBL(nativeTablePtr)->remove_column(S(columnIndex));
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeRenameColumn(JNIEnv* env, jobject, jlong nativeTablePtr,
                                                                       jlong columnIndex, jstring name)
{
    if (!TBL_AND_COL_INDEX_VALID(env, TBL(nativeTablePtr), columnIndex)) {
        return;
    }
    if (TBL(nativeTablePtr)->has_shared_type()) {
        ThrowException(env, UnsupportedOperation,
                       "Not allowed to rename field in subtable. Use getSubtableSchema() on root table instead.");
        return;
    }
    try {
        JStringAccessor name2(env, name); // throws
        TBL(nativeTablePtr)->rename_column(S(columnIndex), name2);
    }
    CATCH_STD()
}

JNIEXPORT jboolean JNICALL Java_io_realm_internal_Table_nativeIsColumnNullable(JNIEnv* env, jobject,
                                                                               jlong nativeTablePtr,
                                                                               jlong columnIndex)
{
    Table* table = TBL(nativeTablePtr);
    if (!TBL_AND_COL_INDEX_VALID(env, table, columnIndex)) {
        return JNI_FALSE;
    }
    if (table->has_shared_type()) {
        ThrowException(env, UnsupportedOperation, "Not allowed to convert field in subtable.");
        return JNI_FALSE;
    }
    size_t column_index = S(columnIndex);
    return to_jbool(table->is_nullable(column_index));
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

JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeConvertColumnToNullable(JNIEnv* env, jobject,
                                                                                  jlong nativeTablePtr,
                                                                                  jlong columnIndex)
{
    Table* table = TBL(nativeTablePtr);
    if (!TBL_AND_COL_INDEX_VALID(env, table, columnIndex)) {
        return;
    }
    if (table->has_shared_type()) {
        ThrowException(env, UnsupportedOperation, "Not allowed to convert field in subtable.");
        return;
    }
    try {
        size_t column_index = S(columnIndex);
        if (table->is_nullable(column_index)) {
            return; // column is already nullable
        }

        std::string column_name = table->get_column_name(column_index);
        DataType column_type = table->get_column_type(column_index);
        if (column_type == type_Link || column_type == type_LinkList || column_type == type_Mixed ||
            column_type == type_Table) {
            ThrowException(env, IllegalArgument, "Wrong type - cannot be converted to nullable.");
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
                    ThrowException(env, UnsupportedOperation, "The old DateTime type is not supported.");
                    return;
            }
        }
        if (table->has_search_index(column_index + 1)) {
            table->add_search_index(column_index);
        }
        table->remove_column(column_index + 1);
        table->rename_column(table->get_column_index(tmp_column_name), column_name);
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeConvertColumnToNotNullable(JNIEnv* env, jobject,
                                                                                     jlong nativeTablePtr,
                                                                                     jlong columnIndex)
{
    Table* table = TBL(nativeTablePtr);
    if (!TBL_AND_COL_INDEX_VALID(env, table, columnIndex)) {
        return;
    }
    if (table->has_shared_type()) {
        ThrowException(env, UnsupportedOperation, "Not allowed to convert field in subtable.");
        return;
    }
    try {
        size_t column_index = S(columnIndex);
        if (!table->is_nullable(column_index)) {
            return; // column is already not nullable
        }

        std::string column_name = table->get_column_name(column_index);
        DataType column_type = table->get_column_type(column_index);
        if (column_type == type_Link || column_type == type_LinkList || column_type == type_Mixed ||
            column_type == type_Table) {
            ThrowException(env, IllegalArgument, "Wrong type - cannot be converted to nullable.");
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
                    ThrowException(env, UnsupportedOperation, "The old DateTime type is not supported.");
                    return;
            }
        }
        if (table->has_search_index(column_index + 1)) {
            table->add_search_index(column_index);
        }
        table->remove_column(column_index + 1);
        table->rename_column(table->get_column_index(tmp_column_name), column_name);
    }
    CATCH_STD()
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeSize(JNIEnv* env, jobject, jlong nativeTablePtr)
{
    if (!TABLE_VALID(env, TBL(nativeTablePtr))) {
        return 0;
    }
    return static_cast<jlong>(TBL(nativeTablePtr)->size()); // noexcept
}

JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeClear(JNIEnv* env, jobject, jlong nativeTablePtr)
{
    if (!TABLE_VALID(env, TBL(nativeTablePtr))) {
        return;
    }
    try {
        TBL(nativeTablePtr)->clear();
    }
    CATCH_STD()
}


// -------------- Column information


JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeGetColumnCount(JNIEnv* env, jobject, jlong nativeTablePtr)
{
    if (!TABLE_VALID(env, TBL(nativeTablePtr))) {
        return 0;
    }
    return static_cast<jlong>(TBL(nativeTablePtr)->get_column_count()); // noexcept
}

JNIEXPORT jstring JNICALL Java_io_realm_internal_Table_nativeGetColumnName(JNIEnv* env, jobject, jlong nativeTablePtr,
                                                                           jlong columnIndex)
{
    if (!TBL_AND_COL_INDEX_VALID(env, TBL(nativeTablePtr), columnIndex)) {
        return nullptr;
    }
    try {
        return to_jstring(env, TBL(nativeTablePtr)->get_column_name(S(columnIndex)));
    }
    CATCH_STD();
    REALM_UNREACHABLE();
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeGetColumnIndex(JNIEnv* env, jobject, jlong nativeTablePtr,
                                                                          jstring columnName)
{
    if (!TABLE_VALID(env, TBL(nativeTablePtr))) {
        return 0;
    }
    try {
        JStringAccessor columnName2(env, columnName);                                     // throws
        return to_jlong_or_not_found(TBL(nativeTablePtr)->get_column_index(columnName2)); // noexcept
    }
    CATCH_STD()
    return 0;
}

JNIEXPORT jint JNICALL Java_io_realm_internal_Table_nativeGetColumnType(JNIEnv* env, jobject, jlong nativeTablePtr,
                                                                        jlong columnIndex)
{
    if (!TBL_AND_COL_INDEX_VALID(env, TBL(nativeTablePtr), columnIndex)) {
        return 0;
    }

    return static_cast<jint>(TBL(nativeTablePtr)->get_column_type(S(columnIndex))); // noexcept
}


// ---------------- Row handling

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeAddEmptyRow(JNIEnv* env, jclass, jlong nativeTablePtr,
                                                                       jlong rows)
{
    Table* pTable = TBL(nativeTablePtr);
    if (!TABLE_VALID(env, pTable)) {
        return 0;
    }
    if (pTable->get_column_count() < 1) {
        ThrowException(env, IndexOutOfBounds, concat_stringdata("Table has no columns: ", pTable->get_name()));
        return 0;
    }
    try {
        return static_cast<jlong>(pTable->add_empty_row(S(rows)));
    }
    CATCH_STD()
    return 0;
}

JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeRemove(JNIEnv* env, jobject, jlong nativeTablePtr,
                                                                 jlong rowIndex)
{
    if (!TBL_AND_ROW_INDEX_VALID(env, TBL(nativeTablePtr), rowIndex)) {
        return;
    }
    try {
        TBL(nativeTablePtr)->remove(S(rowIndex));
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeRemoveLast(JNIEnv* env, jobject, jlong nativeTablePtr)
{
    if (!TABLE_VALID(env, TBL(nativeTablePtr))) {
        return;
    }
    try {
        TBL(nativeTablePtr)->remove_last();
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeMoveLastOver(JNIEnv* env, jobject, jlong nativeTablePtr,
                                                                       jlong rowIndex)
{
    if (!TBL_AND_ROW_INDEX_VALID_OFFSET(env, TBL(nativeTablePtr), rowIndex, false)) {
        return;
    }
    try {
        TBL(nativeTablePtr)->move_last_over(S(rowIndex));
    }
    CATCH_STD()
}

// ----------------- Get cell

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeGetLong(JNIEnv* env, jobject, jlong nativeTablePtr,
                                                                   jlong columnIndex, jlong rowIndex)
{
    if (!TBL_AND_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex, type_Int)) {
        return 0;
    }
    return TBL(nativeTablePtr)->get_int(S(columnIndex), S(rowIndex)); // noexcept
}

JNIEXPORT jboolean JNICALL Java_io_realm_internal_Table_nativeGetBoolean(JNIEnv* env, jobject, jlong nativeTablePtr,
                                                                         jlong columnIndex, jlong rowIndex)
{
    if (!TBL_AND_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex, type_Bool)) {
        return JNI_FALSE;
    }

    return to_jbool(TBL(nativeTablePtr)->get_bool(S(columnIndex), S(rowIndex))); // noexcept
}

JNIEXPORT jfloat JNICALL Java_io_realm_internal_Table_nativeGetFloat(JNIEnv* env, jobject, jlong nativeTablePtr,
                                                                     jlong columnIndex, jlong rowIndex)
{
    if (!TBL_AND_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex, type_Float)) {
        return 0;
    }

    return TBL(nativeTablePtr)->get_float(S(columnIndex), S(rowIndex)); // noexcept
}

JNIEXPORT jdouble JNICALL Java_io_realm_internal_Table_nativeGetDouble(JNIEnv* env, jobject, jlong nativeTablePtr,
                                                                       jlong columnIndex, jlong rowIndex)
{
    if (!TBL_AND_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex, type_Double)) {
        return 0;
    }

    return TBL(nativeTablePtr)->get_double(S(columnIndex), S(rowIndex)); // noexcept
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeGetTimestamp(JNIEnv* env, jobject, jlong nativeTablePtr,
                                                                        jlong columnIndex, jlong rowIndex)
{
    if (!TBL_AND_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex, type_Timestamp)) {
        return 0;
    }
    try {
        return to_milliseconds(TBL(nativeTablePtr)->get_timestamp(S(columnIndex), S(rowIndex)));
    }
    CATCH_STD()
    return 0;
}

JNIEXPORT jstring JNICALL Java_io_realm_internal_Table_nativeGetString(JNIEnv* env, jobject, jlong nativeTablePtr,
                                                                       jlong columnIndex, jlong rowIndex)
{
    if (!TBL_AND_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex, type_String)) {
        return nullptr;
    }
    try {
        return to_jstring(env, TBL(nativeTablePtr)->get_string(S(columnIndex), S(rowIndex)));
    }
    CATCH_STD()
    return nullptr;
}


/*
JNIEXPORT jobject JNICALL Java_io_realm_internal_Table_nativeGetByteBuffer(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex)
{
    if (!TBL_AND_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex, type_Binary))
        return NULL;

    BinaryData bin = TBL(nativeTablePtr)->get_binary( S(columnIndex), S(rowIndex));
    return env->NewDirectByteBuffer(const_cast<char*>(bin.data()), bin.size());  // throws
}
*/

JNIEXPORT jbyteArray JNICALL Java_io_realm_internal_Table_nativeGetByteArray(JNIEnv* env, jobject,
                                                                             jlong nativeTablePtr, jlong columnIndex,
                                                                             jlong rowIndex)
{
    if (!TBL_AND_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex, type_Binary)) {
        return nullptr;
    }

    return tbl_GetByteArray<Table>(env, nativeTablePtr, columnIndex, rowIndex); // noexcept
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeGetLink(JNIEnv* env, jobject, jlong nativeTablePtr,
                                                                   jlong columnIndex, jlong rowIndex)
{
    if (!TBL_AND_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex, type_Link)) {
        return 0;
    }
    return static_cast<jlong>(TBL(nativeTablePtr)->get_link(S(columnIndex), S(rowIndex))); // noexcept
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeGetLinkView(JNIEnv* env, jclass, jlong nativeTablePtr,
                                                                       jlong columnIndex, jlong rowIndex)
{
    if (!TBL_AND_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex, type_LinkList)) {
        return 0;
    }
    try {
        LinkViewRef* link_view_ptr = new LinkViewRef(TBL(nativeTablePtr)->get_linklist(S(columnIndex), S(rowIndex)));
        return reinterpret_cast<jlong>(link_view_ptr);
    }
    CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeGetLinkTarget(JNIEnv* env, jobject, jlong nativeTablePtr,
                                                                         jlong columnIndex)
{
    try {
        Table* pTable = &(*TBL(nativeTablePtr)->get_link_target(S(columnIndex)));
        LangBindHelper::bind_table_ptr(pTable);
        return reinterpret_cast<jlong>(pTable);
    }
    CATCH_STD()
    return 0;
}

JNIEXPORT jboolean JNICALL Java_io_realm_internal_Table_nativeIsNull(JNIEnv*, jobject, jlong nativeTablePtr,
                                                                     jlong columnIndex, jlong rowIndex)
{
    return to_jbool(TBL(nativeTablePtr)->is_null(S(columnIndex), S(rowIndex))); // noexcept
}

// ----------------- Set cell

JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeSetLink(JNIEnv* env, jclass, jlong nativeTablePtr,
                                                                  jlong columnIndex, jlong rowIndex,
                                                                  jlong targetRowIndex, jboolean isDefault)
{
    if (!TBL_AND_INDEX_AND_TYPE_INSERT_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex, type_Link)) {
        return;
    }
    try {
        TBL(nativeTablePtr)->set_link(S(columnIndex), S(rowIndex), S(targetRowIndex), B(isDefault));
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeSetLong(JNIEnv* env, jclass, jlong nativeTablePtr,
                                                                  jlong columnIndex, jlong rowIndex, jlong value,
                                                                  jboolean isDefault)
{
    if (!TBL_AND_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex, type_Int)) {
        return;
    }
    try {
        TBL(nativeTablePtr)->set_int(S(columnIndex), S(rowIndex), value, B(isDefault));
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeSetLongUnique(JNIEnv* env, jclass, jlong nativeTablePtr,
                                                                        jlong columnIndex, jlong rowIndex,
                                                                        jlong value)
{
    if (!TBL_AND_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex, type_Int)) {
        return;
    }
    try {
        TBL(nativeTablePtr)->set_int_unique(S(columnIndex), S(rowIndex), value);
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeSetBoolean(JNIEnv* env, jclass, jlong nativeTablePtr,
                                                                     jlong columnIndex, jlong rowIndex,
                                                                     jboolean value, jboolean isDefault)
{
    if (!TBL_AND_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex, type_Bool)) {
        return;
    }
    try {
        TBL(nativeTablePtr)->set_bool(S(columnIndex), S(rowIndex), B(value), B(isDefault));
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeSetFloat(JNIEnv* env, jclass, jlong nativeTablePtr,
                                                                   jlong columnIndex, jlong rowIndex, jfloat value,
                                                                   jboolean isDefault)
{
    if (!TBL_AND_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex, type_Float)) {
        return;
    }
    try {
        TBL(nativeTablePtr)->set_float(S(columnIndex), S(rowIndex), value, B(isDefault));
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeSetDouble(JNIEnv* env, jclass, jlong nativeTablePtr,
                                                                    jlong columnIndex, jlong rowIndex, jdouble value,
                                                                    jboolean isDefault)
{
    if (!TBL_AND_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex, type_Double)) {
        return;
    }
    try {
        TBL(nativeTablePtr)->set_double(S(columnIndex), S(rowIndex), value, B(isDefault));
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeSetString(JNIEnv* env, jclass, jlong nativeTablePtr,
                                                                    jlong columnIndex, jlong rowIndex, jstring value,
                                                                    jboolean isDefault)
{
    if (!TBL_AND_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex, type_String)) {
        return;
    }
    try {
        if (value == nullptr) {
            if (!TBL_AND_COL_NULLABLE(env, TBL(nativeTablePtr), columnIndex)) {
                return;
            }
        }
        JStringAccessor value2(env, value); // throws
        TBL(nativeTablePtr)->set_string(S(columnIndex), S(rowIndex), value2, B(isDefault));
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeSetStringUnique(JNIEnv* env, jclass, jlong nativeTablePtr,
                                                                          jlong columnIndex, jlong rowIndex,
                                                                          jstring value)
{
    if (!TBL_AND_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex, type_String)) {
        return;
    }
    try {
        if (value == nullptr) {
            if (!TBL_AND_COL_NULLABLE(env, TBL(nativeTablePtr), columnIndex)) {
                return;
            }
            TBL(nativeTablePtr)->set_string_unique(S(columnIndex), S(rowIndex), null{});
        }
        else {
            JStringAccessor value2(env, value); // throws
            TBL(nativeTablePtr)->set_string_unique(S(columnIndex), S(rowIndex), value2);
        }
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeSetTimestamp(JNIEnv* env, jclass, jlong nativeTablePtr,
                                                                       jlong columnIndex, jlong rowIndex,
                                                                       jlong timestampValue, jboolean isDefault)
{
    if (!TBL_AND_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex, type_Timestamp)) {
        return;
    }
    try {
        TBL(nativeTablePtr)
            ->set_timestamp(S(columnIndex), S(rowIndex), from_milliseconds(timestampValue), B(isDefault));
    }
    CATCH_STD()
}

/*
JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeSetByteBuffer(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex, jobject byteBuffer)
{
    if (!TBL_AND_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex, type_Binary))
        return;
    try {
        tbl_nativeDoBinary(&Table::set_binary, TBL(nativeTablePtr), env, columnIndex, rowIndex, byteBuffer);
    } CATCH_STD()
}
*/

JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeSetByteArray(JNIEnv* env, jclass, jlong nativeTablePtr,
                                                                       jlong columnIndex, jlong rowIndex,
                                                                       jbyteArray dataArray, jboolean isDefault)
{
    if (!TBL_AND_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex, type_Binary)) {
        return;
    }
    try {
        if (dataArray == nullptr && !TBL_AND_COL_NULLABLE(env, TBL(nativeTablePtr), columnIndex)) {
            return;
        }

        JniByteArray byteAccessor(env, dataArray);
        TBL(nativeTablePtr)->set_binary(S(columnIndex), S(rowIndex), byteAccessor, B(isDefault));
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeSetNull(JNIEnv* env, jclass, jlong nativeTablePtr,
                                                                  jlong columnIndex, jlong rowIndex,
                                                                  jboolean isDefault)
{
    Table* pTable = TBL(nativeTablePtr);
    if (!TBL_AND_COL_INDEX_VALID(env, pTable, columnIndex)) {
        return;
    }
    if (!TBL_AND_ROW_INDEX_VALID(env, pTable, rowIndex)) {
        return;
    }
    if (!TBL_AND_COL_NULLABLE(env, pTable, columnIndex)) {
        return;
    }

    try {
        pTable->set_null(S(columnIndex), S(rowIndex), B(isDefault));
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeSetNullUnique(JNIEnv* env, jclass, jlong nativeTablePtr,
                                                                        jlong columnIndex, jlong rowIndex)
{
    Table* pTable = TBL(nativeTablePtr);
    if (!TBL_AND_COL_INDEX_VALID(env, pTable, columnIndex)) {
        return;
    }
    if (!TBL_AND_ROW_INDEX_VALID(env, pTable, rowIndex)) {
        return;
    }
    if (!TBL_AND_COL_NULLABLE(env, pTable, columnIndex)) {
        return;
    }

    try {
        pTable->set_null_unique(S(columnIndex), S(rowIndex));
    }
    CATCH_STD()
}


JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeGetRowPtr(JNIEnv* env, jobject, jlong nativeTablePtr,
                                                                     jlong index)
{
    try {
        Row* row = new Row((*TBL(nativeTablePtr))[S(index)]);
        return reinterpret_cast<jlong>(row);
    }
    CATCH_STD()
    return reinterpret_cast<jlong>(nullptr);
}

//--------------------- Indexing methods:

JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeAddSearchIndex(JNIEnv* env, jobject, jlong nativeTablePtr,
                                                                         jlong columnIndex)
{
    Table* pTable = TBL(nativeTablePtr);
    if (!TBL_AND_COL_INDEX_VALID(env, pTable, columnIndex)) {
        return;
    }

    DataType column_type = pTable->get_column_type(S(columnIndex));
    if (!is_allowed_to_index(env, column_type)) {
        return;
    }

    try {
        pTable->add_search_index(S(columnIndex));
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeRemoveSearchIndex(JNIEnv* env, jobject,
                                                                            jlong nativeTablePtr, jlong columnIndex)
{
    Table* pTable = TBL(nativeTablePtr);
    if (!TBL_AND_COL_INDEX_VALID(env, pTable, columnIndex)) {
        return;
    }
    DataType column_type = pTable->get_column_type(S(columnIndex));
    if (!is_allowed_to_index(env, column_type)) {
        return;
    }
    try {
        pTable->remove_search_index(S(columnIndex));
    }
    CATCH_STD()
}


JNIEXPORT jboolean JNICALL Java_io_realm_internal_Table_nativeHasSearchIndex(JNIEnv* env, jobject,
                                                                             jlong nativeTablePtr, jlong columnIndex)
{
    if (!TBL_AND_COL_INDEX_VALID(env, TBL(nativeTablePtr), columnIndex)) {
        return JNI_FALSE;
    }
    try {
        return to_jbool(TBL(nativeTablePtr)->has_search_index(S(columnIndex)));
    }
    CATCH_STD()
    return JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_io_realm_internal_Table_nativeIsNullLink(JNIEnv* env, jobject, jlong nativeTablePtr,
                                                                         jlong columnIndex, jlong rowIndex)
{
    if (!TBL_AND_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex, type_Link)) {
        return JNI_FALSE;
    }

    return to_jbool(TBL(nativeTablePtr)->is_null_link(S(columnIndex), S(rowIndex)));
}

JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeNullifyLink(JNIEnv* env, jclass, jlong nativeTablePtr,
                                                                      jlong columnIndex, jlong rowIndex)
{
    if (!TBL_AND_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex, type_Link)) {
        return;
    }
    try {
        TBL(nativeTablePtr)->nullify_link(S(columnIndex), S(rowIndex));
    }
    CATCH_STD()
}

//---------------------- Aggregate methods for integers

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeSumInt(JNIEnv* env, jobject, jlong nativeTablePtr,
                                                                  jlong columnIndex)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, type_Int)) {
        return 0;
    }
    try {
        return TBL(nativeTablePtr)->sum_int(S(columnIndex));
    }
    CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeMaximumInt(JNIEnv* env, jobject, jlong nativeTablePtr,
                                                                      jlong columnIndex)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, type_Int)) {
        return 0;
    }
    try {
        return TBL(nativeTablePtr)->maximum_int(S(columnIndex));
    }
    CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeMinimumInt(JNIEnv* env, jobject, jlong nativeTablePtr,
                                                                      jlong columnIndex)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, type_Int)) {
        return 0;
    }
    try {
        return TBL(nativeTablePtr)->minimum_int(S(columnIndex));
    }
    CATCH_STD()
    return 0;
}

JNIEXPORT jdouble JNICALL Java_io_realm_internal_Table_nativeAverageInt(JNIEnv* env, jobject, jlong nativeTablePtr,
                                                                        jlong columnIndex)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, type_Int)) {
        return 0;
    }
    try {
        return TBL(nativeTablePtr)->average_int(S(columnIndex));
    }
    CATCH_STD()
    return 0;
}

//--------------------- Aggregate methods for float

JNIEXPORT jdouble JNICALL Java_io_realm_internal_Table_nativeSumFloat(JNIEnv* env, jobject, jlong nativeTablePtr,
                                                                      jlong columnIndex)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, type_Float)) {
        return 0;
    }
    try {
        return TBL(nativeTablePtr)->sum_float(S(columnIndex));
    }
    CATCH_STD()
    return 0;
}

JNIEXPORT jfloat JNICALL Java_io_realm_internal_Table_nativeMaximumFloat(JNIEnv* env, jobject, jlong nativeTablePtr,
                                                                         jlong columnIndex)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, type_Float)) {
        return 0;
    }
    try {
        return TBL(nativeTablePtr)->maximum_float(S(columnIndex));
    }
    CATCH_STD()
    return 0;
}

JNIEXPORT jfloat JNICALL Java_io_realm_internal_Table_nativeMinimumFloat(JNIEnv* env, jobject, jlong nativeTablePtr,
                                                                         jlong columnIndex)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, type_Float)) {
        return 0;
    }
    try {
        return TBL(nativeTablePtr)->minimum_float(S(columnIndex));
    }
    CATCH_STD()
    return 0;
}

JNIEXPORT jdouble JNICALL Java_io_realm_internal_Table_nativeAverageFloat(JNIEnv* env, jobject, jlong nativeTablePtr,
                                                                          jlong columnIndex)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, type_Float)) {
        return 0;
    }
    try {
        return TBL(nativeTablePtr)->average_float(S(columnIndex));
    }
    CATCH_STD()
    return 0;
}


//--------------------- Aggregate methods for double

JNIEXPORT jdouble JNICALL Java_io_realm_internal_Table_nativeSumDouble(JNIEnv* env, jobject, jlong nativeTablePtr,
                                                                       jlong columnIndex)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, type_Double)) {
        return 0;
    }
    try {
        return TBL(nativeTablePtr)->sum_double(S(columnIndex));
    }
    CATCH_STD()
    return 0;
}

JNIEXPORT jdouble JNICALL Java_io_realm_internal_Table_nativeMaximumDouble(JNIEnv* env, jobject, jlong nativeTablePtr,
                                                                           jlong columnIndex)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, type_Double)) {
        return 0;
    }
    try {
        return TBL(nativeTablePtr)->maximum_double(S(columnIndex));
    }
    CATCH_STD()
    return 0;
}

JNIEXPORT jdouble JNICALL Java_io_realm_internal_Table_nativeMinimumDouble(JNIEnv* env, jobject, jlong nativeTablePtr,
                                                                           jlong columnIndex)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, type_Double)) {
        return 0;
    }
    try {
        return TBL(nativeTablePtr)->minimum_double(S(columnIndex));
    }
    CATCH_STD()
    return 0;
}

JNIEXPORT jdouble JNICALL Java_io_realm_internal_Table_nativeAverageDouble(JNIEnv* env, jobject, jlong nativeTablePtr,
                                                                           jlong columnIndex)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, type_Double)) {
        return 0;
    }
    try {
        return TBL(nativeTablePtr)->average_double(S(columnIndex));
    }
    CATCH_STD()
    return 0;
}


//--------------------- Aggregate methods for date

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeMaximumTimestamp(JNIEnv* env, jobject,
                                                                            jlong nativeTablePtr, jlong columnIndex)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, type_Timestamp)) {
        return 0;
    }
    try {
        return to_milliseconds(TBL(nativeTablePtr)->maximum_timestamp(S(columnIndex)));
    }
    CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeMinimumTimestamp(JNIEnv* env, jobject,
                                                                            jlong nativeTablePtr, jlong columnIndex)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, type_Timestamp)) {
        return 0;
    }
    try {
        return to_milliseconds(TBL(nativeTablePtr)->minimum_timestamp(S(columnIndex)));
    }
    CATCH_STD()
    return 0;
}

//---------------------- Count

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeCountLong(JNIEnv* env, jobject, jlong nativeTablePtr,
                                                                     jlong columnIndex, jlong value)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, type_Int)) {
        return 0;
    }
    try {
        return static_cast<jlong>(TBL(nativeTablePtr)->count_int(S(columnIndex), value));
    }
    CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeCountFloat(JNIEnv* env, jobject, jlong nativeTablePtr,
                                                                      jlong columnIndex, jfloat value)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, type_Float)) {
        return 0;
    }
    try {
        return static_cast<jlong>(TBL(nativeTablePtr)->count_float(S(columnIndex), value));
    }
    CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeCountDouble(JNIEnv* env, jobject, jlong nativeTablePtr,
                                                                       jlong columnIndex, jdouble value)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, type_Double)) {
        return 0;
    }
    try {
        return static_cast<jlong>(TBL(nativeTablePtr)->count_double(S(columnIndex), value));
    }
    CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeCountString(JNIEnv* env, jobject, jlong nativeTablePtr,
                                                                       jlong columnIndex, jstring value)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, type_String)) {
        return 0;
    }
    try {
        JStringAccessor value2(env, value); // throws
        return static_cast<jlong>(TBL(nativeTablePtr)->count_string(S(columnIndex), value2));
    }
    CATCH_STD()
    return 0;
}


JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeWhere(JNIEnv* env, jobject, jlong nativeTablePtr)
{
    if (!TABLE_VALID(env, TBL(nativeTablePtr))) {
        return 0;
    }
    try {
        Query* queryPtr = new Query(TBL(nativeTablePtr)->where());
        return reinterpret_cast<jlong>(queryPtr);
    }
    CATCH_STD()
    return reinterpret_cast<jlong>(nullptr);
}

//----------------------- FindFirst

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeFindFirstInt(JNIEnv* env, jclass, jlong nativeTablePtr,
                                                                        jlong columnIndex, jlong value)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, type_Int)) {
        return 0;
    }
    try {
        return to_jlong_or_not_found(TBL(nativeTablePtr)->find_first_int(S(columnIndex), value));
    }
    CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeFindFirstBool(JNIEnv* env, jobject, jlong nativeTablePtr,
                                                                         jlong columnIndex, jboolean value)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, type_Bool)) {
        return 0;
    }
    try {
        return to_jlong_or_not_found(TBL(nativeTablePtr)->find_first_bool(S(columnIndex), to_bool(value)));
    }
    CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeFindFirstFloat(JNIEnv* env, jobject, jlong nativeTablePtr,
                                                                          jlong columnIndex, jfloat value)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, type_Float)) {
        return 0;
    }
    try {
        return to_jlong_or_not_found(TBL(nativeTablePtr)->find_first_float(S(columnIndex), value));
    }
    CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeFindFirstDouble(JNIEnv* env, jobject, jlong nativeTablePtr,
                                                                           jlong columnIndex, jdouble value)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, type_Double)) {
        return 0;
    }
    try {
        return to_jlong_or_not_found(TBL(nativeTablePtr)->find_first_double(S(columnIndex), value));
    }
    CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeFindFirstTimestamp(JNIEnv* env, jobject,
                                                                              jlong nativeTablePtr, jlong columnIndex,
                                                                              jlong dateTimeValue)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, type_Timestamp)) {
        return 0;
    }
    try {
        size_t res = TBL(nativeTablePtr)->find_first_timestamp(S(columnIndex), from_milliseconds(dateTimeValue));
        return to_jlong_or_not_found(res);
    }
    CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeFindFirstString(JNIEnv* env, jclass, jlong nativeTablePtr,
                                                                           jlong columnIndex, jstring value)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, type_String)) {
        return 0;
    }

    try {
        JStringAccessor value2(env, value); // throws
        return to_jlong_or_not_found(TBL(nativeTablePtr)->find_first_string(S(columnIndex), value2));
    }
    CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeFindFirstNull(JNIEnv* env, jclass, jlong nativeTablePtr,
                                                                         jlong columnIndex)
{
    Table* pTable = TBL(nativeTablePtr);
    if (!TBL_AND_COL_INDEX_VALID(env, pTable, columnIndex)) {
        return static_cast<jlong>(realm::not_found);
    }
    if (!TBL_AND_COL_NULLABLE(env, pTable, columnIndex)) {
        return static_cast<jlong>(realm::not_found);
    }
    try {
        return to_jlong_or_not_found(pTable->find_first_null(S(columnIndex)));
    }
    CATCH_STD()
    return static_cast<jlong>(realm::not_found);
}

// FindAll


// FIXME: reenable when find_first_timestamp() is implemented
/*
JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeFindAllTimestamp(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong dateTimeValue)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, type_Timestamp))
        return 0;
    try {
        TableView* pTableView = new TableView(TBL(nativeTablePtr)->find_all_timestamp(S(columnIndex),
from_milliseconds(dateTimeValue)));
        return reinterpret_cast<jlong>(pTableView);
    } CATCH_STD()
    return 0;
}
*/


// experimental
JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeLowerBoundInt(JNIEnv* env, jobject, jlong nativeTablePtr,
                                                                         jlong columnIndex, jlong value)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, type_Int)) {
        return 0;
    }

    Table* pTable = TBL(nativeTablePtr);
    try {
        return static_cast<jlong>(pTable->lower_bound_int(S(columnIndex), S(value)));
    }
    CATCH_STD()
    return 0;
}


// experimental
JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeUpperBoundInt(JNIEnv* env, jobject, jlong nativeTablePtr,
                                                                         jlong columnIndex, jlong value)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, type_Int)) {
        return 0;
    }

    Table* pTable = TBL(nativeTablePtr);
    try {
        return static_cast<jlong>(pTable->upper_bound_int(S(columnIndex), S(value)));
    }
    CATCH_STD()
    return 0;
}

//

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeGetSortedViewMulti(JNIEnv* env, jobject,
                                                                              jlong nativeTablePtr,
                                                                              jlongArray columnIndices,
                                                                              jbooleanArray ascending)
{
    Table* pTable = TBL(nativeTablePtr);

    JniLongArray long_arr(env, columnIndices);
    JniBooleanArray bool_arr(env, ascending);
    jsize arr_len = long_arr.len();
    jsize asc_len = bool_arr.len();

    if (arr_len == 0) {
        ThrowException(env, IllegalArgument, "You must provide at least one field name.");
        return 0;
    }
    if (asc_len == 0) {
        ThrowException(env, IllegalArgument, "You must provide at least one sort order.");
        return 0;
    }
    if (arr_len != asc_len) {
        ThrowException(env, IllegalArgument, "Number of column indices and sort orders do not match.");
        return 0;
    }

    std::vector<std::vector<size_t>> indices(S(arr_len));
    std::vector<bool> ascendings(S(arr_len));

    for (int i = 0; i < arr_len; ++i) {
        if (!TBL_AND_COL_INDEX_VALID(env, pTable, S(long_arr[i]))) {
            return 0;
        }
        int colType = pTable->get_column_type(S(long_arr[i]));
        switch (colType) {
            case type_Int:
            case type_Bool:
            case type_String:
            case type_Double:
            case type_Float:
            case type_Timestamp:
                indices[i] = std::vector<size_t>{S(long_arr[i])};
                ascendings[i] = S(bool_arr[i]);
                break;
            default:
                ThrowException(env, IllegalArgument, "Sort is only support on String, Date, boolean, byte, short, "
                                                     "int, long and their boxed variants.");
                return 0;
        }
    }

    try {
        TableView* pTableView = new TableView(pTable->get_sorted_view(SortDescriptor(*pTable, indices, ascendings)));
        return reinterpret_cast<jlong>(pTableView);
    }
    CATCH_STD()
    return 0;
}

JNIEXPORT jstring JNICALL Java_io_realm_internal_Table_nativeGetName(JNIEnv* env, jobject, jlong nativeTablePtr)
{
    try {
        Table* table = TBL(nativeTablePtr);
        if (!TABLE_VALID(env, table)) {
            return nullptr;
        }
        return to_jstring(env, table->get_name());
    }
    CATCH_STD()
    return nullptr;
}


JNIEXPORT jstring JNICALL Java_io_realm_internal_Table_nativeToJson(JNIEnv* env, jobject, jlong nativeTablePtr)
{
    Table* table = TBL(nativeTablePtr);
    if (!TABLE_VALID(env, table)) {
        return nullptr;
    }

    // Write table to string in JSON format
    try {
        ostringstream ss;
        ss.sync_with_stdio(false); // for performance
        table->to_json(ss);
        const string str = ss.str();
        return to_jstring(env, str);
    }
    CATCH_STD()
    return nullptr;
}

JNIEXPORT jboolean JNICALL Java_io_realm_internal_Table_nativeIsValid(JNIEnv*, jobject, jlong nativeTablePtr)
{
    TR_ENTER_PTR(nativeTablePtr)
    return to_jbool(TBL(nativeTablePtr)->is_attached()); // noexcept
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_createNative(JNIEnv* env, jobject)
{
    TR_ENTER()
    try {
        return reinterpret_cast<jlong>(LangBindHelper::new_table());
    }
    CATCH_STD()
    return 0;
}

// Checks if the primary key column contains any duplicate values, making it ineligible as a
// primary key.
static bool check_valid_primary_key_column(JNIEnv* env, Table* table, StringData column_name) // throws
{
    size_t column_index = table->get_column_index(column_name);
    if (column_index == realm::not_found) {
        std::ostringstream error_msg;
        error_msg << table->get_name() << " does not contain the field \"" << column_name << "\"";
        ThrowException(env, IllegalArgument, error_msg.str());
    }
    DataType column_type = table->get_column_type(column_index);
    TableView results = table->get_sorted_view(column_index);

    switch (column_type) {
        case type_Int:
            if (results.size() > 1) {
                int64_t val = results.get_int(column_index, 0);
                for (size_t i = 1; i < results.size(); i++) {
                    int64_t next_val = results.get_int(column_index, i);
                    if (val == next_val) {
                        std::ostringstream error_msg;
                        error_msg << "Field \"" << column_name << "\" cannot be a primary key, ";
                        error_msg << "it already contains duplicate values: " << val;
                        ThrowException(env, IllegalArgument, error_msg.str());
                        return false;
                    }
                    else {
                        val = next_val;
                    }
                }
            }
            return true;

        case type_String:
            if (results.size() > 1) {
                string str = results.get_string(column_index, 0);
                for (size_t i = 1; i < results.size(); i++) {
                    string next_str = results.get_string(column_index, i);
                    if (str.compare(next_str) == 0) {
                        std::ostringstream error_msg;
                        error_msg << "Field \"" << column_name << "\" cannot be a primary key, ";
                        error_msg << "it already contains duplicate values: " << str;
                        ThrowException(env, IllegalArgument, error_msg.str());
                        return false;
                    }
                    else {
                        str = next_str;
                    }
                }
            }
            return true;

        default:
            std::ostringstream error_msg;
            error_msg << "Invalid primary key type for column: " << column_name;
            ThrowException(env, IllegalArgument, error_msg.str());
            return false;
    }
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeSetPrimaryKey(JNIEnv* env, jobject,
                                                                         jlong nativePrivateKeyTablePtr,
                                                                         jlong nativeTablePtr, jstring columnName)
{
    try {
        Table* table = TBL(nativeTablePtr);
        Table* pk_table = TBL(nativePrivateKeyTablePtr);
        const std::string table_name(table->get_name().substr(TABLE_PREFIX.length())); // Remove "class_" prefix
        size_t row_index =
            pk_table->find_first_string(io_realm_internal_Table_PRIMARY_KEY_CLASS_COLUMN_INDEX, table_name);

        if (columnName == NULL || env->GetStringLength(columnName) == 0) {
            // No primary key provided => remove previous set keys
            if (row_index != realm::not_found) {
                pk_table->remove(row_index);
            }
            return io_realm_internal_Table_NO_PRIMARY_KEY;
        }
        else {
            JStringAccessor new_primary_key_column_name(env, columnName);
            size_t primary_key_column_index = table->get_column_index(new_primary_key_column_name);
            if (row_index == realm::not_found) {
                // No primary key is currently set
                if (check_valid_primary_key_column(env, table, new_primary_key_column_name)) {
                    row_index = pk_table->add_empty_row();
                    pk_table->set_string_unique(io_realm_internal_Table_PRIMARY_KEY_CLASS_COLUMN_INDEX, row_index,
                                                table_name);
                    pk_table->set_string(io_realm_internal_Table_PRIMARY_KEY_FIELD_COLUMN_INDEX, row_index,
                                         new_primary_key_column_name);
                }
            }
            else {
                // Primary key already exists
                // We only wish to check for duplicate values if a column isn't already a primary key
                StringData current_primary_key =
                    pk_table->get_string(io_realm_internal_Table_PRIMARY_KEY_FIELD_COLUMN_INDEX, row_index);
                if (new_primary_key_column_name != current_primary_key) {
                    if (check_valid_primary_key_column(env, table, new_primary_key_column_name)) {
                        pk_table->set_string(io_realm_internal_Table_PRIMARY_KEY_FIELD_COLUMN_INDEX, row_index,
                                             new_primary_key_column_name);
                    }
                }
            }

            return static_cast<jlong>(primary_key_column_index);
        }
    }
    CATCH_STD()
    return 0;
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
JNIEXPORT jboolean JNICALL Java_io_realm_internal_Table_nativeMigratePrimaryKeyTableIfNeeded(
    JNIEnv*, jclass, jlong groupNativePtr, jlong privateKeyTableNativePtr)
{
    const size_t CLASS_COLUMN_INDEX = io_realm_internal_Table_PRIMARY_KEY_CLASS_COLUMN_INDEX;
    const size_t FIELD_COLUMN_INDEX = io_realm_internal_Table_PRIMARY_KEY_FIELD_COLUMN_INDEX;

    auto group = reinterpret_cast<Group*>(groupNativePtr);
    Table* pk_table = TBL(privateKeyTableNativePtr);
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
}

JNIEXPORT jboolean JNICALL
Java_io_realm_internal_Table_nativePrimaryKeyTableNeedsMigration(JNIEnv*, jclass, jlong primaryKeyTableNativePtr)
{

    const size_t CLASS_COLUMN_INDEX = io_realm_internal_Table_PRIMARY_KEY_CLASS_COLUMN_INDEX;
    const size_t FIELD_COLUMN_INDEX = io_realm_internal_Table_PRIMARY_KEY_FIELD_COLUMN_INDEX;

    Table* pk_table = TBL(primaryKeyTableNativePtr);

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
}

JNIEXPORT jboolean JNICALL Java_io_realm_internal_Table_nativeHasSameSchema(JNIEnv*, jobject, jlong thisTablePtr,
                                                                            jlong otherTablePtr)
{
    return to_jbool(*TBL(thisTablePtr)->get_descriptor() == *TBL(otherTablePtr)->get_descriptor());
}


JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeVersion(JNIEnv* env, jobject, jlong nativeTablePtr)
{
    bool valid = (TBL(nativeTablePtr) != nullptr);
    if (valid) {
        if (!TBL(nativeTablePtr)->is_attached()) {
            ThrowException(env, IllegalState, "The Realm has been closed and is no longer accessible.");
            return 0;
        }
    }
    try {
        return static_cast<jlong>(TBL(nativeTablePtr)->get_version_counter());
    }
    CATCH_STD()
    return 0;
}

static void finalize_table(jlong ptr)
{
    TR_ENTER_PTR(ptr)
    LangBindHelper::unbind_table_ptr(TBL(ptr));
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeGetFinalizerPtr(JNIEnv*, jclass)
{
    TR_ENTER()
    return reinterpret_cast<jlong>(&finalize_table);
}
