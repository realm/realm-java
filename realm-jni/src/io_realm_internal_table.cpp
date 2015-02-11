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
#include "mixedutil.hpp"
#include "io_realm_internal_Table.h"
#include "columntypeutil.hpp"
#include "TableSpecUtil.hpp"
#include "java_lang_List_Util.hpp"
#include "mixedutil.hpp"
#include "tablebase_tpl.hpp"
#include "tablequery.hpp"

using namespace std;
using namespace tightdb;

// Note: Don't modify spec on a table which has a shared_spec.
// A spec is shared on subtables that are not in Mixed columns.
//

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeAddColumn
  (JNIEnv *env, jobject, jlong nativeTablePtr, jint colType, jstring name)
{
    if (!TABLE_VALID(env, TBL(nativeTablePtr)))
        return 0;
    if (TBL(nativeTablePtr)->has_shared_type()) {
        ThrowException(env, UnsupportedOperation, "Not allowed to add column in subtable. Use getSubtableSchema() on root table instead.");
        return 0;
    }
    try {
        JStringAccessor name2(env, name); // throws
        return TBL(nativeTablePtr)->add_column(DataType(colType), name2);
    } CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeAddColumnLink
  (JNIEnv* env, jobject, jlong nativeTablePtr, jint colType, jstring name, jlong targetTablePtr)
{
    if (!TABLE_VALID(env, TBL(nativeTablePtr)))
            return 0;
        if (TBL(nativeTablePtr)->has_shared_type()) {
            ThrowException(env, UnsupportedOperation, "Not allowed to add column in subtable. Use getSubtableSchema() on root table instead.");
            return 0;
        }
        if (!TBL(targetTablePtr)->is_group_level()) {
            ThrowException(env, UnsupportedOperation, "Links can only be made to toplevel tables.");
            return 0;
        }
        try {
            JStringAccessor name2(env, name); // throws
            return TBL(nativeTablePtr)->add_column_link(DataType(colType), name2, *TBL(targetTablePtr));
        } CATCH_STD()
        return 0;
}


JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativePivot
(JNIEnv *env, jobject, jlong dataTablePtr, jlong stringCol, jlong intCol, jint operation, jlong resultTablePtr)
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
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeRemoveColumn
  (JNIEnv *env, jobject, jlong nativeTablePtr, jlong columnIndex)
{
    if (!TBL_AND_COL_INDEX_VALID(env, TBL(nativeTablePtr), columnIndex))
        return;
    if (TBL(nativeTablePtr)->has_shared_type()) {
        ThrowException(env, UnsupportedOperation, "Not allowed to remove column in subtable. Use getSubtableSchema() on root table instead.");
        return;
    }
    try {
        TBL(nativeTablePtr)->remove_column(S(columnIndex));
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeRenameColumn
  (JNIEnv *env, jobject, jlong nativeTablePtr, jlong columnIndex, jstring name)
{
    if (!TBL_AND_COL_INDEX_VALID(env, TBL(nativeTablePtr), columnIndex))
        return;
    if (TBL(nativeTablePtr)->has_shared_type()) {
        ThrowException(env, UnsupportedOperation, "Not allowed to rename column in subtable. Use getSubtableSchema() on root table instead.");
        return;
    }
    try {
        JStringAccessor name2(env, name); // throws
        TBL(nativeTablePtr)->rename_column(S(columnIndex), name2);
    } CATCH_STD()
}


JNIEXPORT jboolean JNICALL Java_io_realm_internal_Table_nativeIsRootTable
  (JNIEnv *, jobject, jlong nativeTablePtr)
{
    //If the spec is shared, it is a subtable, and this method will return false
    return !TBL(nativeTablePtr)->has_shared_type();
}

JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeUpdateFromSpec(
    JNIEnv* env, jobject, jlong nativeTablePtr, jobject jTableSpec)
{
    Table* pTable = TBL(nativeTablePtr);
    TR("nativeUpdateFromSpec(tblPtr %p, spec %p)", VOID_PTR(pTable), VOID_PTR(jTableSpec))
    if (!TABLE_VALID(env, pTable))
        return;
    if (pTable->has_shared_type()) {
        ThrowException(env, UnsupportedOperation, "It is not allowed to update a subtable from spec.");
        return;
    }
    try {
        DescriptorRef desc = pTable->get_descriptor(); // Throws
        set_descriptor(env, *desc, jTableSpec);
    }
    CATCH_STD()
}

JNIEXPORT jobject JNICALL Java_io_realm_internal_Table_nativeGetTableSpec(
    JNIEnv* env, jobject, jlong nativeTablePtr)
{
    if (!TABLE_VALID(env, TBL(nativeTablePtr)))
        return 0;

    TR_ENTER_PTR(nativeTablePtr)
    static jmethodID jTableSpecConsId = GetTableSpecMethodID(env, "<init>", "()V");
    if (jTableSpecConsId) {
        try {
            // Create a new TableSpec object in Java
            const Table* pTable = TBL(nativeTablePtr);
            ConstDescriptorRef desc = pTable->get_descriptor(); // noexcept
            jobject jTableSpec = env->NewObject(GetClassTableSpec(env), jTableSpecConsId);
            if (jTableSpec) {
                get_descriptor(env, *desc, jTableSpec); // Throws
                return jTableSpec;
            }
        }
        CATCH_STD()
    }
    return 0;
}


JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeSize(
    JNIEnv* env, jobject, jlong nativeTablePtr)
{
    if (!TABLE_VALID(env, TBL(nativeTablePtr)))
        return 0;
    return TBL(nativeTablePtr)->size();     // noexcept
}

JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeClear(
    JNIEnv* env, jobject, jlong nativeTablePtr)
{
    if (!TABLE_VALID(env, TBL(nativeTablePtr)))
        return;
    try {
        TBL(nativeTablePtr)->clear();
    } CATCH_STD()
}


// -------------- Column information


JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeGetColumnCount(
    JNIEnv* env, jobject, jlong nativeTablePtr)
{
    if (!TABLE_VALID(env, TBL(nativeTablePtr)))
        return 0;
    return TBL(nativeTablePtr)->get_column_count(); // noexcept
}

JNIEXPORT jstring JNICALL Java_io_realm_internal_Table_nativeGetColumnName(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex)
{
    if (!TBL_AND_COL_INDEX_VALID(env, TBL(nativeTablePtr), columnIndex))
        return NULL;
    try {
        return to_jstring(env, TBL(nativeTablePtr)->get_column_name( S(columnIndex)));
    } CATCH_STD();
    return NULL;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeGetColumnIndex(
    JNIEnv* env, jobject, jlong nativeTablePtr, jstring columnName)
{
    if (!TABLE_VALID(env, TBL(nativeTablePtr)))
        return 0;
    try {
        JStringAccessor columnName2(env, columnName); // throws
        return to_jlong_or_not_found( TBL(nativeTablePtr)->get_column_index(columnName2) ); // noexcept
    } CATCH_STD()
    return 0;
}

JNIEXPORT jint JNICALL Java_io_realm_internal_Table_nativeGetColumnType(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex)
{
    if (!TBL_AND_COL_INDEX_VALID(env, TBL(nativeTablePtr), columnIndex))
        return 0;

    return static_cast<jint>( TBL(nativeTablePtr)->get_column_type( S(columnIndex)) ); // noexcept
}


// ---------------- Row handling

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeAddEmptyRow(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong rows)
{
    Table* pTable = TBL(nativeTablePtr);
    if (!TABLE_VALID(env, pTable))
        return 0;
    if (pTable->get_column_count() < 1){
        ThrowException(env, IndexOutOfBounds, "Table has no columns");
        return 0;
    }
    try {
        return static_cast<jlong>( pTable->add_empty_row( S(rows)) );
    } CATCH_STD()
    return 0;
}

JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeInsertLinkList
  (JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, type_LinkList))
        return;

    TBL(nativeTablePtr)->insert_linklist( S(columnIndex), S(rowIndex) );
}

JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeRemove(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong rowIndex)
{
    if (!TBL_AND_ROW_INDEX_VALID(env, TBL(nativeTablePtr), rowIndex))
        return;
    try {
        TBL(nativeTablePtr)->remove(S(rowIndex));
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeRemoveLast(
    JNIEnv* env, jobject, jlong nativeTablePtr)
{
    if (!TABLE_VALID(env, TBL(nativeTablePtr)))
        return;
    try {
        TBL(nativeTablePtr)->remove_last();
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeMoveLastOver
  (JNIEnv *env, jobject, jlong nativeTablePtr, jlong rowIndex)
{
    if (!TBL_AND_ROW_INDEX_VALID_OFFSET(env, TBL(nativeTablePtr), rowIndex, false))
        return;
    try {
        TBL(nativeTablePtr)->move_last_over(S(rowIndex));
    } CATCH_STD()
}


// ----------------- Insert cell

JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeInsertLong(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex, jlong value)
{
    if (!TBL_AND_INDEX_AND_TYPE_INSERT_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex, type_Int))
        return;
    try {
        TBL(nativeTablePtr)->insert_int( S(columnIndex), S(rowIndex), value);
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeInsertBoolean(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex, jboolean value)
{
    if (!TBL_AND_INDEX_AND_TYPE_INSERT_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex, type_Bool))
        return;
    try {
        TBL(nativeTablePtr)->insert_bool( S(columnIndex), S(rowIndex), value != 0 ? true : false);
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeInsertFloat(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex, jfloat value)
{
    if (!TBL_AND_INDEX_AND_TYPE_INSERT_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex, type_Float))
        return;
    try {
        TBL(nativeTablePtr)->insert_float( S(columnIndex), S(rowIndex), value);
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeInsertDouble(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex, jdouble value)
{
    if (!TBL_AND_INDEX_AND_TYPE_INSERT_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex, type_Double))
        return;
    try {
        TBL(nativeTablePtr)->insert_double( S(columnIndex), S(rowIndex), value);
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeInsertDate(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex, jlong dateTimeValue)
{
    if (!TBL_AND_INDEX_AND_TYPE_INSERT_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex, type_DateTime))
        return;
    try {
        TBL(nativeTablePtr)->insert_datetime( S(columnIndex), S(rowIndex), static_cast<time_t>(dateTimeValue));
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeInsertString(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex, jstring value)
{
    if (!TBL_AND_INDEX_AND_TYPE_INSERT_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex, type_String))
        return;
    try {
        JStringAccessor value2(env, value); // throws
        TBL(nativeTablePtr)->insert_string( S(columnIndex), S(rowIndex), value2);
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeInsertMixed(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex, jobject jMixedValue)
{
    if (!TBL_AND_INDEX_INSERT_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex))
        return;
    try {
        tbl_nativeDoMixed(&Table::insert_mixed, TBL(nativeTablePtr), env, columnIndex, rowIndex, jMixedValue);
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeSetMixed(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex, jobject jMixedValue)
{
    if (!TBL_AND_INDEX_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex))
        return;
    try {
        tbl_nativeDoMixed(&Table::set_mixed, TBL(nativeTablePtr), env, columnIndex, rowIndex, jMixedValue);
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeInsertSubtable(
    JNIEnv* env, jobject jTable, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex)
{
    if (!TBL_AND_INDEX_AND_TYPE_INSERT_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex, type_Table))
        return;

    TR("nativeInsertSubtable(jTable:%p, nativeTablePtr: %p, colIdx: %lld, rowIdx: %lld)",
        VOID_PTR(jTable), VOID_PTR(nativeTablePtr),  S64(columnIndex), S64(rowIndex))
    try {
        TBL(nativeTablePtr)->insert_subtable( S(columnIndex), S(rowIndex));
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeInsertDone(
    JNIEnv* env, jobject, jlong nativeTablePtr)
{
    if (!TABLE_VALID(env, TBL(nativeTablePtr)))
        return;
    try {
        TBL(nativeTablePtr)->insert_done();
    } CATCH_STD()
}


// ----------------- Get cell

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeGetLong(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex)
{
    if (!TBL_AND_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex, type_Int))
        return 0;
    return TBL(nativeTablePtr)->get_int( S(columnIndex), S(rowIndex));  // noexcept
}

JNIEXPORT jboolean JNICALL Java_io_realm_internal_Table_nativeGetBoolean(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex)
{
    if (!TBL_AND_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex, type_Bool))
        return false;

    return TBL(nativeTablePtr)->get_bool( S(columnIndex), S(rowIndex));  // noexcept
}

JNIEXPORT jfloat JNICALL Java_io_realm_internal_Table_nativeGetFloat(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex)
{
    if (!TBL_AND_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex, type_Float))
        return 0;

    return TBL(nativeTablePtr)->get_float( S(columnIndex), S(rowIndex));  // noexcept
}

JNIEXPORT jdouble JNICALL Java_io_realm_internal_Table_nativeGetDouble(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex)
{
    if (!TBL_AND_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex, type_Double))
        return 0;

    return TBL(nativeTablePtr)->get_double( S(columnIndex), S(rowIndex));  // noexcept
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeGetDateTime(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex)
{
    if (!TBL_AND_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex, type_DateTime))
        return 0;
    return TBL(nativeTablePtr)->get_datetime( S(columnIndex), S(rowIndex)).get_datetime();  // noexcept
}

JNIEXPORT jstring JNICALL Java_io_realm_internal_Table_nativeGetString(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex)
{
    if (!TBL_AND_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex, type_String))
        return NULL;
    try {
        return to_jstring(env, TBL(nativeTablePtr)->get_string( S(columnIndex), S(rowIndex)));
    } CATCH_STD()
    return NULL;
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

JNIEXPORT jbyteArray JNICALL Java_io_realm_internal_Table_nativeGetByteArray(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex)
{
    if (!TBL_AND_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex, type_Binary))
        return NULL;

    return tbl_GetByteArray<Table>(env, nativeTablePtr, columnIndex, rowIndex);  // noexcept
}

JNIEXPORT jint JNICALL Java_io_realm_internal_Table_nativeGetMixedType(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex)
{
    if (!TBL_AND_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex, type_Mixed))
        return 0;

    DataType mixedType = TBL(nativeTablePtr)->get_mixed_type( S(columnIndex), S(rowIndex));  // noexcept
    return static_cast<jint>(mixedType);
}

JNIEXPORT jobject JNICALL Java_io_realm_internal_Table_nativeGetMixed(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex)
{
    if (!TBL_AND_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex, type_Mixed))
        return NULL;

    Mixed value = TBL(nativeTablePtr)->get_mixed( S(columnIndex), S(rowIndex));  // noexcept
    try {
        return CreateJMixedFromMixed(env, value);
    } CATCH_STD();
    return NULL;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeGetLink
  (JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex)
{
    if (!TBL_AND_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex, type_Link))
        return 0;
    return TBL(nativeTablePtr)->get_link( S(columnIndex), S(rowIndex));  // noexcept
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeGetLinkTarget
  (JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex)
{
    try {
        Table* pTable = &(*TBL(nativeTablePtr)->get_link_target( S(columnIndex) ));
        LangBindHelper::bind_table_ptr(pTable);
        return (jlong)pTable;
    } CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeGetSubtable(
    JNIEnv* env, jobject jTableBase, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex)
{
    if (!TBL_AND_INDEX_AND_TYPE_VALID_MIXED(env, TBL(nativeTablePtr), columnIndex, rowIndex, type_Table))
        return 0;
    try {
        Table* pSubtable = static_cast<Table*>(LangBindHelper::get_subtable_ptr(TBL(nativeTablePtr),
            S(columnIndex), S(rowIndex)));
        TR("nativeGetSubtable(jTableBase:%p, nativeTablePtr: %p, colIdx: %lld, rowIdx: %lld) : %p",
            VOID_PTR(jTableBase), VOID_PTR(nativeTablePtr), S64(columnIndex), S64(rowIndex), VOID_PTR(pSubtable))
        return (jlong)pSubtable;
    } CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeGetSubtableDuringInsert(
    JNIEnv* env, jobject jTableBase, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex)
{
    if (!TBL_AND_INDEX_AND_TYPE_INSERT_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex, type_Table))
        return 0;
    try {
        Table* pSubtable = static_cast<Table*>(LangBindHelper::get_subtable_ptr_during_insert(
            TBL(nativeTablePtr), S(columnIndex), S(rowIndex)));
        TR("nativeGetSubtableDuringInsert(jTableBase:%p, nativeTablePtr: %p, colIdx: %lld, rowIdx: %lld) : %p",
           VOID_PTR(jTableBase), VOID_PTR(nativeTablePtr), S64(columnIndex), S64(rowIndex), VOID_PTR(pSubtable))
        return (jlong)pSubtable;
    } CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeGetSubtableSize(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex)
{
    if (!TBL_AND_INDEX_AND_TYPE_VALID_MIXED(env, TBL(nativeTablePtr), columnIndex, rowIndex, type_Table))
        return 0;

    return TBL(nativeTablePtr)->get_subtable_size( S(columnIndex), S(rowIndex)); // noexcept
}


// ----------------- Set cell

JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeSetLink
  (JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex, jlong targetRowIndex)
{
    if (!TBL_AND_INDEX_AND_TYPE_INSERT_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex, type_Link))
        return;
    try {
        TBL(nativeTablePtr)->set_link( S(columnIndex), S(rowIndex), S(targetRowIndex));
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeSetLong(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex, jlong value)
{
    if (!TBL_AND_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex, type_Int))
        return;
    try {
        TBL(nativeTablePtr)->set_int( S(columnIndex), S(rowIndex), value);
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeSetBoolean(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex, jboolean value)
{
    if (!TBL_AND_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex, type_Bool))
        return;
    try {
        TBL(nativeTablePtr)->set_bool( S(columnIndex), S(rowIndex), value == JNI_TRUE ? true : false);
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeSetFloat(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex, jfloat value)
{
    if (!TBL_AND_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex, type_Float))
        return;
    try {
        TBL(nativeTablePtr)->set_float( S(columnIndex), S(rowIndex), value);
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeSetDouble(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex, jdouble value)
{
    if (!TBL_AND_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex, type_Double))
        return;
    try {
        TBL(nativeTablePtr)->set_double( S(columnIndex), S(rowIndex), value);
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeSetString(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex, jstring value)
{
    if (!TBL_AND_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex, type_String))
        return;
    try {
        JStringAccessor value2(env, value); // throws
        TBL(nativeTablePtr)->set_string( S(columnIndex), S(rowIndex), value2);
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeSetDate(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex, jlong dateTimeValue)
{
    if (!TBL_AND_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex, type_DateTime))
        return;
    try {
        TBL(nativeTablePtr)->set_datetime( S(columnIndex), S(rowIndex), dateTimeValue);
    } CATCH_STD()
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
/*
JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeInsertByteBuffer(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex, jobject byteBuffer)
{
    if (!TBL_AND_INDEX_AND_TYPE_INSERT_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex, type_Binary))
        return;
    try {
        tbl_nativeDoBinary(&Table::insert_binary, TBL(nativeTablePtr), env, columnIndex, rowIndex, byteBuffer);
    } CATCH_STD()
}
*/

JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeSetByteArray(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex, jbyteArray dataArray)
{
    if (!TBL_AND_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex, type_Binary))
        return;
    try {
        tbl_nativeDoByteArray(&Table::set_binary, TBL(nativeTablePtr), env, columnIndex, rowIndex, dataArray);
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeInsertByteArray(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex, jbyteArray dataArray)
{
    if (!TBL_AND_INDEX_AND_TYPE_INSERT_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex, type_Binary))
        return;
    try {
        tbl_nativeDoByteArray(&Table::insert_binary, TBL(nativeTablePtr), env, columnIndex, rowIndex, dataArray);
    } CATCH_STD()
}


JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeAddInt(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong value)
{
    Table* pTable = TBL(nativeTablePtr);
    if (!TBL_AND_COL_INDEX_VALID(env, pTable, columnIndex))
        return;
    if (pTable->get_column_type (S(columnIndex)) != type_Int) {
        ThrowException(env, IllegalArgument, "Invalid columntype - only Long columns are supported at the moment.");
        return;
    }
    try {
        TBL(nativeTablePtr)->add_int( S(columnIndex), value);
    } CATCH_STD()
}


JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeClearSubtable(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex)
{
    if (!TBL_AND_INDEX_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex))
        return;
    try {
        TBL(nativeTablePtr)->clear_subtable( S(columnIndex), S(rowIndex));
    } CATCH_STD()
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeGetRowPtr
  (JNIEnv* env, jobject, jlong nativeTablePtr, jlong index)
{
    try {
        Row* row = new Row( (*TBL(nativeTablePtr))[ S(index) ] );
        return reinterpret_cast<jlong>(row);
    } CATCH_STD()
    return 0;
}

//--------------------- Indexing methods:

JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeSetIndex(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex)
{
    Table* pTable = TBL(nativeTablePtr);
    if (!TBL_AND_COL_INDEX_VALID(env, pTable, columnIndex))
        return;
    if (pTable->get_column_type (S(columnIndex)) != type_String) {
        ThrowException(env, IllegalArgument, "Invalid columntype - only string columns are supported at the moment.");
        return;
    }
    try {
        pTable->add_search_index( S(columnIndex));
    } CATCH_STD()
}

JNIEXPORT jboolean JNICALL Java_io_realm_internal_Table_nativeHasIndex(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex)
{
    if (!TBL_AND_COL_INDEX_VALID(env, TBL(nativeTablePtr), columnIndex))
        return false;
    try {
        return TBL(nativeTablePtr)->has_search_index( S(columnIndex));
    } CATCH_STD()
    return false;
}

JNIEXPORT jboolean JNICALL Java_io_realm_internal_Table_nativeIsNullLink
  (JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex)
{
    if (!TBL_AND_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex, type_Link))
        return 0;

    return TBL(nativeTablePtr)->is_null_link(S(columnIndex), S(rowIndex));
}

JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeNullifyLink
  (JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex)
{
    if (!TBL_AND_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex, type_Link))
        return;
    try {
        TBL(nativeTablePtr)->nullify_link(S(columnIndex), S(rowIndex));
    } CATCH_STD()
}

//---------------------- Aggregate methods for integers

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeSumInt(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, type_Int))
        return 0;
    try {
        return TBL(nativeTablePtr)->sum_int( S(columnIndex));
    } CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeMaximumInt(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, type_Int))
        return 0;
    try {
        return TBL(nativeTablePtr)->maximum_int( S(columnIndex));
    } CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeMinimumInt(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, type_Int))
        return 0;
    try {
        return TBL(nativeTablePtr)->minimum_int( S(columnIndex));
    } CATCH_STD()
    return 0;
}

JNIEXPORT jdouble JNICALL Java_io_realm_internal_Table_nativeAverageInt(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, type_Int))
        return 0;
    try {
        return TBL(nativeTablePtr)->average_int( S(columnIndex));
    } CATCH_STD()
    return 0;
}

//--------------------- Aggregate methods for float

JNIEXPORT jdouble JNICALL Java_io_realm_internal_Table_nativeSumFloat(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, type_Float))
        return 0;
    try {
        return TBL(nativeTablePtr)->sum_float( S(columnIndex));
    } CATCH_STD()
    return 0;
}

JNIEXPORT jfloat JNICALL Java_io_realm_internal_Table_nativeMaximumFloat(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, type_Float))
        return 0;
    try {
        return TBL(nativeTablePtr)->maximum_float( S(columnIndex));
    } CATCH_STD()
    return 0;
}

JNIEXPORT jfloat JNICALL Java_io_realm_internal_Table_nativeMinimumFloat(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, type_Float))
        return 0;
    try {
        return TBL(nativeTablePtr)->minimum_float( S(columnIndex));
    } CATCH_STD()
    return 0;
}

JNIEXPORT jdouble JNICALL Java_io_realm_internal_Table_nativeAverageFloat(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, type_Float))
        return 0;
    try {
        return TBL(nativeTablePtr)->average_float( S(columnIndex));
    } CATCH_STD()
    return 0;
}


//--------------------- Aggregate methods for double

JNIEXPORT jdouble JNICALL Java_io_realm_internal_Table_nativeSumDouble(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, type_Double))
        return 0;
    try {
        return TBL(nativeTablePtr)->sum_double( S(columnIndex));
    } CATCH_STD()
    return 0;
}

JNIEXPORT jdouble JNICALL Java_io_realm_internal_Table_nativeMaximumDouble(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, type_Double))
        return 0;
    try {
        return TBL(nativeTablePtr)->maximum_double( S(columnIndex));
    } CATCH_STD()
    return 0;
}

JNIEXPORT jdouble JNICALL Java_io_realm_internal_Table_nativeMinimumDouble(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, type_Double))
        return 0;
    try {
        return TBL(nativeTablePtr)->minimum_double( S(columnIndex));
    } CATCH_STD()
    return 0;
}

JNIEXPORT jdouble JNICALL Java_io_realm_internal_Table_nativeAverageDouble(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, type_Double))
        return 0;
    try {
        return TBL(nativeTablePtr)->average_double( S(columnIndex));
    } CATCH_STD()
    return 0;
}


//--------------------- Aggregate methods for date

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeMaximumDate(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, type_DateTime))
        return 0;
    try {
        // This exploits the fact that dates are stored as int in core
        return TBL(nativeTablePtr)->maximum_int( S(columnIndex));
    } CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeMinimumDate(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, type_DateTime))
        return 0;
    try {
        // This exploits the fact that dates are stored as int in core
        return TBL(nativeTablePtr)->minimum_int( S(columnIndex));
    } CATCH_STD()
    return 0;
}

//---------------------- Count

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeCountLong(
    JNIEnv *env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong value)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, type_Int))
        return 0;
    try {
        return TBL(nativeTablePtr)->count_int( S(columnIndex), value);
    } CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeCountFloat(
    JNIEnv *env, jobject, jlong nativeTablePtr, jlong columnIndex, jfloat value)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, type_Float))
        return 0;
    try {
        return TBL(nativeTablePtr)->count_float( S(columnIndex), value);
    } CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeCountDouble(
    JNIEnv *env, jobject, jlong nativeTablePtr, jlong columnIndex, jdouble value)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, type_Double))
        return 0;
    try {
        return TBL(nativeTablePtr)->count_double( S(columnIndex), value);
    } CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeCountString(
    JNIEnv *env, jobject, jlong nativeTablePtr, jlong columnIndex, jstring value)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, type_String))
        return 0;

    try {
        JStringAccessor value2(env, value); // throws
        return TBL(nativeTablePtr)->count_string( S(columnIndex), value2);
    } CATCH_STD()
    return 0;
}


JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeWhere(
    JNIEnv *env, jobject, jlong nativeTablePtr)
{
    if (!TABLE_VALID(env, TBL(nativeTablePtr)))
        return 0;
    try {
        Query query = TBL(nativeTablePtr)->where();
        TableQuery* queryPtr = new TableQuery(query);
        return reinterpret_cast<jlong>(queryPtr);
    } CATCH_STD()
    return 0;
}

//----------------------- FindFirst

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeFindFirstInt(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong value)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, type_Int))
        return 0;
    try {
        return to_jlong_or_not_found( TBL(nativeTablePtr)->find_first_int( S(columnIndex), value) );
    } CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeFindFirstBool(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jboolean value)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, type_Bool))
        return 0;
    try {
        return to_jlong_or_not_found( TBL(nativeTablePtr)->find_first_bool( S(columnIndex), value != 0 ? true : false) );
    } CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeFindFirstFloat(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jfloat value)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, type_Float))
        return 0;
    try {
        return to_jlong_or_not_found( TBL(nativeTablePtr)->find_first_float( S(columnIndex), value) );
    } CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeFindFirstDouble(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jdouble value)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, type_Double))
        return 0;
    try {
        return to_jlong_or_not_found( TBL(nativeTablePtr)->find_first_double( S(columnIndex), value) );
    } CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeFindFirstDate(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong dateTimeValue)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, type_DateTime))
        return 0;
    try {
        size_t res = TBL(nativeTablePtr)->find_first_datetime( S(columnIndex), (time_t)dateTimeValue);
        return to_jlong_or_not_found( res );
    } CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeFindFirstString(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jstring value)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, type_String))
        return 0;

    try {
        JStringAccessor value2(env, value); // throws
        return to_jlong_or_not_found( TBL(nativeTablePtr)->find_first_string( S(columnIndex), value2) );
    } CATCH_STD()
    return 0;
}

// FindAll

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeFindAllInt(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong value)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, type_Int))
        return 0;
    try {
        TableView* pTableView = new TableView( TBL(nativeTablePtr)->find_all_int( S(columnIndex), value) );
        return reinterpret_cast<jlong>(pTableView);
    } CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeFindAllFloat(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jfloat value)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, type_Float))
        return 0;
    try {
        TableView* pTableView = new TableView( TBL(nativeTablePtr)->find_all_float( S(columnIndex), value) );
        return reinterpret_cast<jlong>(pTableView);
    } CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeFindAllDouble(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jdouble value)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, type_Double))
        return 0;
    try {
        TableView* pTableView = new TableView( TBL(nativeTablePtr)->find_all_double( S(columnIndex), value) );
        return reinterpret_cast<jlong>(pTableView);
    } CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeFindAllBool(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jboolean value)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, type_Bool))
        return 0;

    TableView* pTableView = new TableView( TBL(nativeTablePtr)->find_all_bool( S(columnIndex),
                                           value != 0 ? true : false) );
    return reinterpret_cast<jlong>(pTableView);
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeFindAllDate(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong dateTimeValue)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, type_DateTime))
        return 0;
    try {
        TableView* pTableView = new TableView( TBL(nativeTablePtr)->find_all_datetime( S(columnIndex),
                                            static_cast<time_t>(dateTimeValue)) );
        return reinterpret_cast<jlong>(pTableView);
    } CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeFindAllString(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jstring value)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, type_String))
        return 0;

    Table* pTable = TBL(nativeTablePtr);
    try {
        JStringAccessor value2(env, value); // throws
        TableView* pTableView = new TableView( pTable->find_all_string( S(columnIndex), value2) );
        return reinterpret_cast<jlong>(pTableView);
    } CATCH_STD()
    return 0;
}


// experimental
JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeLowerBoundInt(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong value)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, type_Int))
        return 0;

    Table* pTable = TBL(nativeTablePtr);
    try {
        return pTable->lower_bound_int(S(columnIndex), S(value));
    } CATCH_STD()
    return 0;
}


// experimental
JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeUpperBoundInt(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong value)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, type_Int))
        return 0;

    Table* pTable = TBL(nativeTablePtr);
    try {
        return pTable->upper_bound_int(S(columnIndex), S(value));
    } CATCH_STD()
    return 0;
}

//

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeGetDistinctView(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex)
{
    Table* pTable = TBL(nativeTablePtr);
    if (!TBL_AND_COL_INDEX_VALID(env, pTable, columnIndex))
        return 0;
    if (!pTable->has_search_index(S(columnIndex))) {
        ThrowException(env, UnsupportedOperation, "The column must be indexed before distinct() can be used.");
        return 0;
    }
    if (pTable->get_column_type(S(columnIndex)) != type_String) {
        ThrowException(env, IllegalArgument, "Invalid columntype - only string columns are supported.");
        return 0;
    }
    try {
        TableView* pTableView = new TableView( pTable->get_distinct_view(S(columnIndex)) );
        return reinterpret_cast<jlong>(pTableView);
    } CATCH_STD()
    return 0;
}


JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeGetSortedView(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jboolean ascending)
{
    Table* pTable = TBL(nativeTablePtr);
    if (!TBL_AND_COL_INDEX_VALID(env, pTable, columnIndex))
        return 0;
    int colType = pTable->get_column_type( S(columnIndex) );
    switch (colType) {
        case type_Int:
        case type_Bool:
        case type_DateTime:
        case type_String:
        case type_Double:
        case type_Float:
            try {
                TableView* pTableView = new TableView( pTable->get_sorted_view(S(columnIndex), ascending != 0 ? true : false) );
                return reinterpret_cast<jlong>(pTableView);
            } CATCH_STD()
        default:
            ThrowException(env, IllegalArgument, "Sort is currently only supported on integer, boolean, double, float, String, and Date columns.");
            return 0;
    }
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeGetSortedViewMulti(
   JNIEnv *env, jobject, jlong nativeTablePtr, jlongArray columnIndices, jbooleanArray ascending)
{
    Table* pTable = TBL(nativeTablePtr);

    jsize arr_len = env->GetArrayLength(columnIndices);
    jsize asc_len = env->GetArrayLength(ascending);
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

    jlong *long_arr = env->GetLongArrayElements(columnIndices, NULL);
    jboolean *bool_arr = env->GetBooleanArrayElements(ascending, NULL);

    std::vector<size_t> indices(S(arr_len));
    std::vector<bool> ascendings(S(arr_len));

    for (int i = 0; i < arr_len; ++i) {
        if (!TBL_AND_COL_INDEX_VALID(env, pTable, S(long_arr[i]) ))
            return 0;
        int colType = pTable->get_column_type( S(long_arr[i]) );
        switch (colType) {
            case type_Int:
            case type_Bool:
            case type_DateTime:
            case type_String:
            case type_Double:
            case type_Float:
                indices[i] = S(long_arr[i]);
                ascendings[i] = S(bool_arr[i]);
                break;
            default:
                ThrowException(env, IllegalArgument, "Sort is currently only supported on integer, boolean, double, float, String, and Date columns.");
                return 0;
        }
    }

    env->ReleaseLongArrayElements(columnIndices, long_arr, 0);
    env->ReleaseBooleanArrayElements(ascending, bool_arr, 0);

    try {
        TableView* pTableView = new TableView(pTable->get_sorted_view(indices, ascendings));
        return reinterpret_cast<jlong>(pTableView);
    } CATCH_STD()
    return 0;
}

JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeOptimize(
    JNIEnv* env, jobject, jlong nativeTablePtr)
{
    if (!TABLE_VALID(env, TBL(nativeTablePtr)))
        return;
    try {
        TBL(nativeTablePtr)->optimize();
    } CATCH_STD()
}

JNIEXPORT jstring JNICALL Java_io_realm_internal_Table_nativeGetName(
    JNIEnv *env, jobject, jlong nativeTablePtr)
{
    try {
        Table* table = TBL(nativeTablePtr);
        if (!TABLE_VALID(env, table))
            return NULL;
        const string str = table->get_name();
        return to_jstring(env, str);
    } CATCH_STD()
    return NULL;
}


JNIEXPORT jstring JNICALL Java_io_realm_internal_Table_nativeToJson(
    JNIEnv *env, jobject, jlong nativeTablePtr)
{
    Table* table = TBL(nativeTablePtr);
    if (!TABLE_VALID(env, table))
        return NULL;

    // Write table to string in JSON format
    try {
        ostringstream ss;
        ss.sync_with_stdio(false); // for performance
        table->to_json(ss);
        const string str = ss.str();
        return to_jstring(env, str);
    } CATCH_STD()
    return NULL;
}

JNIEXPORT jstring JNICALL Java_io_realm_internal_Table_nativeToString(
    JNIEnv *env, jobject, jlong nativeTablePtr, jlong maxRows)
{
    Table* table = TBL(nativeTablePtr);
    if (!TABLE_VALID(env, table))
        return NULL;
    try {
        ostringstream ss;
        table->to_string(ss, S(maxRows));
        const string str = ss.str();
        return to_jstring(env, str);
    } CATCH_STD()
    return NULL;
}

JNIEXPORT jstring JNICALL Java_io_realm_internal_Table_nativeRowToString(
    JNIEnv *env, jobject, jlong nativeTablePtr, jlong rowIndex)
{
    Table* table = TBL(nativeTablePtr);
    if (!TBL_AND_ROW_INDEX_VALID(env, table, rowIndex))
        return NULL;
    try {
        ostringstream ss;
        table->row_to_string(S(rowIndex), ss);
        const string str = ss.str();
        return to_jstring(env, str);
    } CATCH_STD()
    return NULL;
}

JNIEXPORT jboolean JNICALL Java_io_realm_internal_Table_nativeEquals(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong nativeTableToComparePtr)
{
    Table* tbl = TBL(nativeTablePtr);
    Table* tblToCompare = TBL(nativeTableToComparePtr);
    try {
        return (*tbl == *tblToCompare);
    } CATCH_STD()
    return false;
}

JNIEXPORT jboolean JNICALL Java_io_realm_internal_Table_nativeIsValid(
    JNIEnv*, jobject, jlong nativeTablePtr)
{
    return TBL(nativeTablePtr)->is_attached();  // noexcept
}

JNIEXPORT void JNICALL Java_io_realm_internal_Table_nativeClose(
    JNIEnv*, jclass, jlong nativeTablePtr)
{
    TR_ENTER_PTR(nativeTablePtr)
    LangBindHelper::unbind_table_ptr(TBL(nativeTablePtr));
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_createNative(JNIEnv *env, jobject)
{
    TR_ENTER()
    try {
        return reinterpret_cast<jlong>(LangBindHelper::new_table());
    } CATCH_STD()
    return 0;
}


// Checks if the primary key column contains any duplicate values, making it ineligible as a
// primary key.
bool check_valid_primary_key_column(JNIEnv* env, Table* table, size_t column_index) // throws
{
    int column_type = table->get_column_type(column_index);
    TableView results = table->get_sorted_view(column_index);

    switch(column_type) {
        case type_Int:
            if (results.size() > 1) {
                int64_t val = results.get_int(column_index, 0);
                for (size_t i = 1; i < results.size(); i++) {
                    int64_t next_val = results.get_int(column_index, i);
                    if (val == next_val) {
                        std::ostringstream error_msg;
                        error_msg << "Field \"" << table->get_column_name(column_index).data() << "\" cannot be a primary key, ";
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
                        error_msg << "Field \"" << table->get_column_name(column_index).data() << "\" cannot be a primary key, ";
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
            ThrowException(env, IllegalArgument, "Invalid primary key type: " + column_type);
            return false;
    }
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Table_nativeSetPrimaryKey(
    JNIEnv* env, jobject, jlong nativePrivateKeyTablePtr, jlong nativeTablePtr, jstring columnName)
{
    try {
        Table* table = TBL(nativeTablePtr);
        Table* pk_table = TBL(nativePrivateKeyTablePtr);
        const char* table_name = table->get_name().data();
        size_t row_index = pk_table->find_first_string(io_realm_internal_Table_PRIMARY_KEY_CLASS_COLUMN_INDEX, table_name);

        // I
        if (columnName == NULL || env->GetStringLength(columnName) == 0) {
            // No primary key set. Remove any previous set keys
            if (row_index != tightdb::not_found) {
                pk_table->remove(row_index);
            }
            return jlong(io_realm_internal_Table_NO_PRIMARY_KEY);
        }
        else {
            JStringAccessor columnName2(env, columnName);
            size_t primary_key_column_index = table->get_column_index(columnName2);
            if (row_index == tightdb::not_found) {
                // No primary key is currently set
                if (check_valid_primary_key_column(env, table, primary_key_column_index)) {
                    row_index = pk_table->add_empty_row();
                    pk_table->set_string(io_realm_internal_Table_PRIMARY_KEY_CLASS_COLUMN_INDEX, row_index, table_name);
                    pk_table->set_int(io_realm_internal_Table_PRIMARY_KEY_FIELD_COLUMN_INDEX, row_index, primary_key_column_index);
                }
            }
            else {
                // Primary key already exists
                // We only wish to check for duplicate values if a column isn't already a primary key
                Row* row = new Row((*pk_table)[row_index]);
                size_t current_primary_key = row->get_int(io_realm_internal_Table_PRIMARY_KEY_FIELD_COLUMN_INDEX);
                if (primary_key_column_index != current_primary_key) {
                    if (check_valid_primary_key_column(env, table, primary_key_column_index)) {
                        pk_table->set_int(io_realm_internal_Table_PRIMARY_KEY_FIELD_COLUMN_INDEX, row_index, primary_key_column_index);
                    }
                }
            }

            return jlong(primary_key_column_index);
        }
    } CATCH_STD()
    return 0;
}
