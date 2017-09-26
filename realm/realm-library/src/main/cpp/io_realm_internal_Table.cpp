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

#include "shared_realm.hpp"
#include "util/format.hpp"

#include "java_accessor.hpp"
#include "java_exception_def.hpp"
#include "jni_util/java_exception_thrower.hpp"

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

JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeInsertColumn(JNIEnv* env, jclass, jlong native_table_ptr,
                                                                       jlong column_index, jint type, jstring j_name)
{
    auto table_ptr = reinterpret_cast<realm::Table*>(native_table_ptr);
    if (!TABLE_VALID(env, table_ptr)) {
        return;
    }
    try {
        JStringAccessor name(env, j_name); // throws

        DataType data_type = DataType(type);
        table_ptr->insert_column(column_index, data_type, name);
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
                                                                                  jlong native_table_ptr,
                                                                                  jlong j_column_index,
                                                                                  jboolean is_primary_key)
{
    Table* table = TBL(native_table_ptr);
    if (!TBL_AND_COL_INDEX_VALID(env, table, j_column_index)) {
        return;
    }
    if (table->has_shared_type()) {
        ThrowException(env, UnsupportedOperation, "Not allowed to convert field in subtable.");
        return;
    }
    try {
        size_t column_index = S(j_column_index);
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

        // Search index has too be added first since if it is a PK field, add_xxx_unique will check it.
        if (table->has_search_index(column_index + 1)) {
            table->add_search_index(column_index);
        }

        for (size_t i = 0; i < table->size(); ++i) {
            switch (column_type) {
                case type_String: {
                    // Payload copy is needed
                    StringData sd(table->get_string(column_index + 1, i));
                    if (is_primary_key) {
                        table->set_string_unique(column_index, i, sd);
                    }
                    else {
                        table->set_string(column_index, i, sd);
                    }
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
                    if (is_primary_key) {
                        table->set_int_unique(column_index, i, table->get_int(column_index + 1, i));
                    }
                    else {
                        table->set_int(column_index, i, table->get_int(column_index + 1, i));
                    }
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
        table->remove_column(column_index + 1);
        table->rename_column(table->get_column_index(tmp_column_name), column_name);
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeConvertColumnToNotNullable(JNIEnv* env, jobject,
                                                                                     jlong native_table_ptr,
                                                                                     jlong j_column_index,
                                                                                     jboolean is_primary_key)
{
    Table* table = TBL(native_table_ptr);
    if (!TBL_AND_COL_INDEX_VALID(env, table, j_column_index)) {
        return;
    }
    if (table->has_shared_type()) {
        ThrowException(env, UnsupportedOperation, "Not allowed to convert field in subtable.");
        return;
    }
    try {
        size_t column_index = S(j_column_index);
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

        // Search index has too be added first since if it is a PK field, add_xxx_unique will check it.
        if (table->has_search_index(column_index + 1)) {
            table->add_search_index(column_index);
        }

        for (size_t i = 0; i < table->size(); ++i) {
            switch (column_type) { // FIXME: respect user-specified default values
                case type_String: {
                    StringData sd = table->get_string(column_index + 1, i);
                    if (sd == realm::null()) {
                        if (is_primary_key) {
                            THROW_JAVA_EXCEPTION(env, JavaExceptionDef::IllegalState,
                                                 format(c_null_values_cannot_set_required_msg, column_name));
                        }
                        else {
                            table->set_string(column_index, i, "");
                        }
                    }
                    else {
                        // Payload copy is needed
                        if (is_primary_key) {
                            table->set_string_unique(column_index, i, sd);
                        }
                        else {
                            table->set_string(column_index, i, sd);
                        }
                    }
                    break;
                }
                case type_Binary: {
                    BinaryData bd = table->get_binary(column_index + 1, i);
                    if (bd.is_null()) {
                        table->set_binary(column_index, i, BinaryData("", 0));
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
                        if (is_primary_key) {
                            THROW_JAVA_EXCEPTION(env, JavaExceptionDef::IllegalState,
                                                 format(c_null_values_cannot_set_required_msg, column_name));
                        }
                        else {
                            table->set_int(column_index, i, 0);
                        }
                    }
                    else {
                        if (is_primary_key) {
                            table->set_int_unique(column_index, i, table->get_int(column_index + 1, i));
                        }
                        else {
                            table->set_int(column_index, i, table->get_int(column_index + 1, i));
                        }
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

JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeIncrementLong(JNIEnv* env, jclass, jlong nativeTablePtr,
                                                                  jlong columnIndex, jlong rowIndex, jlong value)
{
    if (!TBL_AND_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex, type_Int)) {
        return;
    }

    try {
        Table* table = TBL(nativeTablePtr);
        if (table->is_null(columnIndex, rowIndex)) {
            THROW_JAVA_EXCEPTION(env, JavaExceptionDef::IllegalState,
                                 "Cannot increment a MutableRealmInteger whose value is null. Set its value first.");
        }

        table->add_int(S(columnIndex), S(rowIndex), value);
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

        JByteArrayAccessor jarray_accessor(env, dataArray);
        TBL(nativeTablePtr)
            ->set_binary(S(columnIndex), S(rowIndex), jarray_accessor.transform<BinaryData>(), B(isDefault));
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

//

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

JNIEXPORT jboolean JNICALL Java_io_realm_internal_Table_nativeIsValid(JNIEnv*, jobject, jlong nativeTablePtr)
{
    TR_ENTER_PTR(nativeTablePtr)
    return to_jbool(TBL(nativeTablePtr)->is_attached()); // noexcept
}

static bool pk_table_needs_migration(ConstTableRef pk_table)
{
    // Fix wrong types (string, int) -> (string, string)
    if (pk_table->get_column_type(FIELD_COLUMN_INDEX) == type_Int) {
        return true;
    }

    // If needed remove "class_" prefix from class names
    size_t number_of_rows = pk_table->size();
    for (size_t row_ndx = 0; row_ndx < number_of_rows; row_ndx++) {
        StringData table_name = pk_table->get_string(CLASS_COLUMN_INDEX, row_ndx);
        if (table_name.begins_with(TABLE_PREFIX)) {
            return true;
        }
    }
    // From realm-java 2.0.0, pk table's class column requires a search index.
    if (!pk_table->has_search_index(CLASS_COLUMN_INDEX)) {
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
static bool migrate_pk_table(const Group& group, TableRef pk_table)
{
    bool changed = false;

    // Fix wrong types (string, int) -> (string, string)
    if (pk_table->get_column_type(FIELD_COLUMN_INDEX) == type_Int) {
        StringData tmp_col_name = StringData("tmp_field_name");
        size_t tmp_col_ndx = pk_table->add_column(DataType(type_String), tmp_col_name);

        // Create tmp string column with field name instead of column index
        size_t number_of_rows = pk_table->size();
        for (size_t row_ndx = 0; row_ndx < number_of_rows; row_ndx++) {
            StringData table_name = pk_table->get_string(CLASS_COLUMN_INDEX, row_ndx);
            size_t col_ndx = static_cast<size_t>(pk_table->get_int(FIELD_COLUMN_INDEX, row_ndx));
            StringData col_name = group.get_table(table_name)->get_column_name(col_ndx);
            // Make a copy of the string
            pk_table->set_string(tmp_col_ndx, row_ndx, col_name);
        }

        // Delete old int column, and rename tmp column to same name
        // The column index for the renamed column will then be the same as the deleted old column
        pk_table->remove_column(FIELD_COLUMN_INDEX);
        pk_table->rename_column(pk_table->get_column_index(tmp_col_name), StringData("pk_property"));
        changed = true;
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
            changed = true;
        }
    }

    // From realm-java 2.0.0, pk table's class column requires a search index.
    if (!pk_table->has_search_index(CLASS_COLUMN_INDEX)) {
        pk_table->add_search_index(CLASS_COLUMN_INDEX);
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
    return to_jbool(*TBL(thisTablePtr)->get_descriptor() == *TBL(otherTablePtr)->get_descriptor());
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
