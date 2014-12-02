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

#include "util.hpp"
#include "io_realm_internal_TableView.h"
#include "mixedutil.hpp"
#include "tablebase_tpl.hpp"
#include "tablequery.hpp"
#include <ostream>

using namespace tightdb;

#define VIEW_VALID(env, ptr) view_valid(env, ptr)

inline bool view_valid(JNIEnv* env, jlong nativeViewPtr) {
    bool valid = (nativeViewPtr != 0);
    if (valid) {
        if (!TV(nativeViewPtr)->is_attached()) {
            ThrowException(env, TableInvalid, "Table is closed, and no longer valid to operate on.");
            return false;
        }
    }
    return valid;
}


JNIEXPORT jlong JNICALL Java_io_realm_internal_TableView_createNativeTableView(
    JNIEnv* env, jobject, jobject, jlong)
{
    try {
        return reinterpret_cast<jlong>( new TableView() );
    } CATCH_STD()
    return 0;
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableView_nativePivot(
    JNIEnv *env, jobject, jlong dataTablePtr, jlong stringCol, jlong intCol, jint operation, jlong resultTablePtr)
{

    try {
        TV(dataTablePtr)->sync_if_needed();
    } CATCH_STD()

    TableView* dataTable = TV(dataTablePtr);
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

JNIEXPORT void JNICALL Java_io_realm_internal_TableView_nativeClose(
    JNIEnv*, jclass, jlong nativeViewPtr)
{
    if (nativeViewPtr == 0)
        return;

    delete TV(nativeViewPtr);
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_TableView_nativeSize(
    JNIEnv* env, jobject, jlong nativeViewPtr)
{
    if (!VIEW_VALID(env, nativeViewPtr))
        return 0;

    try {
        TV(nativeViewPtr)->sync_if_needed();
    } CATCH_STD()
    return TV(nativeViewPtr)->size();   // noexcept
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_TableView_nativeGetSourceRowIndex
(JNIEnv *env, jobject, jlong nativeViewPtr, jlong rowIndex)
{
    if (!VIEW_VALID(env, nativeViewPtr))
        return 0;
    if (!ROW_INDEX_VALID(env, TV(nativeViewPtr), rowIndex))
        return 0;

    try {
        TV(nativeViewPtr)->sync_if_needed();
    } CATCH_STD()
    return TV(nativeViewPtr)->get_source_ndx(S(rowIndex));   // noexcept
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_TableView_nativeGetColumnCount
  (JNIEnv *env, jobject, jlong nativeViewPtr)
{
    if (!VIEW_VALID(env, nativeViewPtr))
        return 0;

    try {
        TV(nativeViewPtr)->sync_if_needed();
    } CATCH_STD()
    return TV(nativeViewPtr)->get_column_count();
}

JNIEXPORT jstring JNICALL Java_io_realm_internal_TableView_nativeGetColumnName
  (JNIEnv *env, jobject, jlong nativeViewPtr, jlong columnIndex)
{
    if (!VIEW_VALID(env, nativeViewPtr) || !COL_INDEX_VALID(env, TV(nativeViewPtr), columnIndex))
        return NULL;

    try {
        TV(nativeViewPtr)->sync_if_needed();
        return to_jstring(env, TV(nativeViewPtr)->get_column_name( S(columnIndex)));
    } CATCH_STD()
    return NULL;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_TableView_nativeGetColumnIndex
   (JNIEnv *env, jobject, jlong nativeViewPtr, jstring columnName)
{
    if (!VIEW_VALID(env, nativeViewPtr))
        return 0;

    try {
        TV(nativeViewPtr)->sync_if_needed();
        JStringAccessor columnName2(env, columnName); // throws
        return to_jlong_or_not_found( TV(nativeViewPtr)->get_column_index(columnName2) ); // noexcept
        } CATCH_STD();
    return 0;
}

JNIEXPORT jint JNICALL Java_io_realm_internal_TableView_nativeGetColumnType
  (JNIEnv *env, jobject, jlong nativeViewPtr, jlong columnIndex)
{
    if (!VIEW_VALID(env, nativeViewPtr) || !COL_INDEX_VALID(env, TV(nativeViewPtr), columnIndex))
        return 0;

    try {
        TV(nativeViewPtr)->sync_if_needed();
    } CATCH_STD()
    return static_cast<int>( TV(nativeViewPtr)->get_column_type( S(columnIndex)) );
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_TableView_nativeGetLong(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex)
{
    if (!VIEW_VALID(env, nativeViewPtr) ||
        !INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex, type_Int))
        return 0;

    try {
        TV(nativeViewPtr)->sync_if_needed();
    } CATCH_STD()
    return TV(nativeViewPtr)->get_int( S(columnIndex), S(rowIndex));  // noexcept
}

JNIEXPORT jboolean JNICALL Java_io_realm_internal_TableView_nativeGetBoolean(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex)
{
    if (!VIEW_VALID(env, nativeViewPtr) ||
        !INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex, type_Bool))
        return 0;

    try {
        TV(nativeViewPtr)->sync_if_needed();
    } CATCH_STD()
    return TV(nativeViewPtr)->get_bool( S(columnIndex), S(rowIndex));  // noexcept
}

JNIEXPORT jfloat JNICALL Java_io_realm_internal_TableView_nativeGetFloat(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex)
{
    if (!VIEW_VALID(env, nativeViewPtr) ||
        !INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex, type_Float))
        return 0;

    try {
        TV(nativeViewPtr)->sync_if_needed();
    } CATCH_STD()
    return TV(nativeViewPtr)->get_float( S(columnIndex), S(rowIndex));  // noexcept
}

JNIEXPORT jdouble JNICALL Java_io_realm_internal_TableView_nativeGetDouble(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex)
{
    if (!VIEW_VALID(env, nativeViewPtr) ||
        !INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex, type_Double))
        return 0;

    try {
        TV(nativeViewPtr)->sync_if_needed();
    } CATCH_STD()
    return TV(nativeViewPtr)->get_double( S(columnIndex), S(rowIndex));  // noexcept
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_TableView_nativeGetDateTimeValue(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex)
{
    if (!VIEW_VALID(env, nativeViewPtr) ||
        !INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex, type_DateTime))
        return 0;

    try {
        TV(nativeViewPtr)->sync_if_needed();
    } CATCH_STD()
    return TV(nativeViewPtr)->get_datetime( S(columnIndex), S(rowIndex)).get_datetime();  // noexcept
}

JNIEXPORT jstring JNICALL Java_io_realm_internal_TableView_nativeGetString(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex)
{
    if (!VIEW_VALID(env, nativeViewPtr) ||
        !INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex, type_String))
        return NULL;

    try {
        TV(nativeViewPtr)->sync_if_needed();
        return to_jstring(env, TV(nativeViewPtr)->get_string( S(columnIndex), S(rowIndex)) // noexcept
                          );
        } CATCH_STD()
    return NULL;
}

/*
JNIEXPORT jobject JNICALL Java_io_realm_internal_TableView_nativeGetBinary(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex)
{
    if (!VIEW_VALID(env, nativeViewPtr) ||
        !INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex, type_Binary))
        return NULL;
    // TODO: Does the native binary get freed?
    BinaryData bin = TV(nativeViewPtr)->get_binary( S(columnIndex), S(rowIndex));  // noexcept
    return env->NewDirectByteBuffer(const_cast<char*>(bin.data()),  static_cast<jlong>(bin.size()));
}
*/

JNIEXPORT jbyteArray JNICALL Java_io_realm_internal_TableView_nativeGetByteArray(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex)
{
    if (!VIEW_VALID(env, nativeViewPtr) ||
        !INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex, type_Binary))
        return NULL;

    try {
        TV(nativeViewPtr)->sync_if_needed();
        return tbl_GetByteArray<TableView>(env, nativeViewPtr, columnIndex, rowIndex);
    } CATCH_STD()
    return NULL;
}

JNIEXPORT jint JNICALL Java_io_realm_internal_TableView_nativeGetMixedType(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex)
{
    if (!VIEW_VALID(env, nativeViewPtr) ||
        !INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex, type_Mixed))
        return 0;

    try {
        TV(nativeViewPtr)->sync_if_needed();
    } CATCH_STD()
    DataType mixedType = TV(nativeViewPtr)->get_mixed_type( S(columnIndex), S(rowIndex));  // noexcept
    return static_cast<jint>(mixedType);
}

JNIEXPORT jobject JNICALL Java_io_realm_internal_TableView_nativeGetMixed(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex)
{
    if (!VIEW_VALID(env, nativeViewPtr) ||
        !INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex, type_Mixed))
        return NULL;

    try {
        TV(nativeViewPtr)->sync_if_needed();
        Mixed value = TV(nativeViewPtr)->get_mixed( S(columnIndex), S(rowIndex));   // noexcept
        return CreateJMixedFromMixed(env, value);
    } CATCH_STD()
    return NULL;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_TableView_nativeGetLink
  (JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex)
{
    if (!VIEW_VALID(env, nativeViewPtr) ||
        !INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex, type_Link))
        return 0;

    try {
        TV(nativeViewPtr)->sync_if_needed();
    } CATCH_STD()
    return TV(nativeViewPtr)->get_link( S(columnIndex), S(rowIndex));  // noexcept
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_TableView_nativeGetSubtableSize(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex)
{
    if (!VIEW_VALID(env, nativeViewPtr) ||
        !INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex, type_Table))
        return 0;

    try {
        TV(nativeViewPtr)->sync_if_needed();
    } CATCH_STD()
    return TV(nativeViewPtr)->get_subtable_size( S(columnIndex), S(rowIndex));  // noexcept
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_TableView_nativeGetSubtable(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex)
{
    if (!VIEW_VALID(env, nativeViewPtr) ||
        !INDEX_AND_TYPE_VALID_MIXED(env, TV(nativeViewPtr), columnIndex, rowIndex, type_Table))
        return 0;

    try {
        TV(nativeViewPtr)->sync_if_needed();
        Table* pSubtable = LangBindHelper::get_subtable_ptr(TV(nativeViewPtr), S(columnIndex), S(rowIndex));
        return reinterpret_cast<jlong>(pSubtable);
    } CATCH_STD()
    return 0;
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableView_nativeClearSubtable(
   JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex)
{
    if (!VIEW_VALID(env, nativeViewPtr) ||
        !INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex, type_Table))
        return;

    try {
        TV(nativeViewPtr)->sync_if_needed();
    } CATCH_STD()
    TV(nativeViewPtr)->clear_subtable(S(columnIndex), S(rowIndex));  // noexcept
}

// Setters

JNIEXPORT void JNICALL Java_io_realm_internal_TableView_nativeSetLong(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex, jlong value)
{
    if (!VIEW_VALID(env, nativeViewPtr) ||
        !INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex, type_Int))
        return;

    try {
        TV(nativeViewPtr)->sync_if_needed();
        TV(nativeViewPtr)->set_int( S(columnIndex), S(rowIndex), value);
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableView_nativeSetBoolean(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex, jboolean value)
{
    if (!VIEW_VALID(env, nativeViewPtr) ||
        !INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex, type_Bool))
        return;

    try {
        TV(nativeViewPtr)->sync_if_needed();
        TV(nativeViewPtr)->set_bool( S(columnIndex), S(rowIndex), value != 0 ? true : false);
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableView_nativeSetFloat(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex, jfloat value)
{
    if (!VIEW_VALID(env, nativeViewPtr) ||
        !INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex, type_Float))
        return;

    try {
        TV(nativeViewPtr)->sync_if_needed();
        TV(nativeViewPtr)->set_float( S(columnIndex), S(rowIndex), value);
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableView_nativeSetDouble(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex, jdouble value)
{
    if (!VIEW_VALID(env, nativeViewPtr) ||
        !INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex, type_Double))
        return;

    try {
        TV(nativeViewPtr)->sync_if_needed();
        TV(nativeViewPtr)->set_double( S(columnIndex), S(rowIndex), value);
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableView_nativeSetDateTimeValue(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex, jlong dateTimeValue)
{
    if (!VIEW_VALID(env, nativeViewPtr) ||
        !INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex, type_DateTime))
        return;

    try {
        TV(nativeViewPtr)->sync_if_needed();
        TV(nativeViewPtr)->set_datetime( S(columnIndex), S(rowIndex), dateTimeValue);
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableView_nativeSetString(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex, jstring value)
{
    if (!VIEW_VALID(env, nativeViewPtr) ||
        !INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex, type_String))
        return;

    try {
        TV(nativeViewPtr)->sync_if_needed();
        JStringAccessor value2(env, value);  // throws
        TV(nativeViewPtr)->set_string( S(columnIndex), S(rowIndex), value2);
    } CATCH_STD()
}

/*
JNIEXPORT void JNICALL Java_io_realm_internal_TableView_nativeSetBinary(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex, jobject byteBuffer)
{
    if (!VIEW_VALID(env, nativeViewPtr) ||
        !INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex, type_Binary))
        return;
    try {
        tbl_nativeDoBinary(&TableView::set_binary, TV(nativeViewPtr), env, columnIndex, rowIndex, byteBuffer);
    } CATCH_STD()
}
*/

JNIEXPORT void JNICALL Java_io_realm_internal_TableView_nativeSetByteArray(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex, jbyteArray byteArray)
{
    if (!VIEW_VALID(env, nativeViewPtr) ||
        !INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex, type_Binary))
        return;

    try {
        TV(nativeViewPtr)->sync_if_needed();
        tbl_nativeDoByteArray(&TableView::set_binary, TV(nativeViewPtr), env, columnIndex, rowIndex, byteArray);
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableView_nativeSetMixed(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex, jobject jMixedValue)
{
    if (!VIEW_VALID(env, nativeViewPtr) ||
        !INDEX_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex))
        return;

    try {
        TV(nativeViewPtr)->sync_if_needed();
        tbl_nativeDoMixed(&TableView::set_mixed, TV(nativeViewPtr), env, columnIndex, rowIndex, jMixedValue);
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableView_nativeSetLink
  (JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex, jlong targetIndex)
{
    if (!VIEW_VALID(env, nativeViewPtr) ||
        !INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex, type_Link))
        return;

    try {
        TV(nativeViewPtr)->sync_if_needed();
        TV(nativeViewPtr)->set_link( S(columnIndex), S(rowIndex), S(targetIndex));
    } CATCH_STD()
}

JNIEXPORT jboolean JNICALL Java_io_realm_internal_TableView_nativeIsNullLink
  (JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex)
{
    if (!VIEW_VALID(env, nativeViewPtr) ||
        !INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex, type_Link))
        return 0;
    TV(nativeViewPtr)->sync_if_needed();
    return TV(nativeViewPtr)->is_null_link( S(columnIndex), S(rowIndex));
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableView_nativeNullifyLink
  (JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex)
{
    if (!VIEW_VALID(env, nativeViewPtr) ||
        !INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex, type_Link))
        return;

    try {
        TV(nativeViewPtr)->sync_if_needed();
        TV(nativeViewPtr)->nullify_link( S(columnIndex), S(rowIndex));
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableView_nativeAddInt(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong value)
{
    if (!VIEW_VALID(env, nativeViewPtr) ||
        !COL_INDEX_VALID(env, TV(nativeViewPtr), columnIndex))
        return;

    try {
        TV(nativeViewPtr)->sync_if_needed();
        TV(nativeViewPtr)->add_int( S(columnIndex), value);
    } CATCH_STD()
}


JNIEXPORT void JNICALL Java_io_realm_internal_TableView_nativeClear(
    JNIEnv* env, jobject, jlong nativeViewPtr)
{
    if (!VIEW_VALID(env, nativeViewPtr))
        return;

    try {
        TV(nativeViewPtr)->sync_if_needed();
        TV(nativeViewPtr)->clear();
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableView_nativeRemoveRow(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong rowIndex)
{
    if (!VIEW_VALID(env, nativeViewPtr) ||
        !ROW_INDEX_VALID(env, TV(nativeViewPtr), rowIndex))
        return;

    try {
        TV(nativeViewPtr)->sync_if_needed();
        TV(nativeViewPtr)->remove( S(rowIndex));
    } CATCH_STD()
}

// FindFirst*

JNIEXPORT jlong JNICALL Java_io_realm_internal_TableView_nativeFindFirstInt(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong value)
{
    if (!VIEW_VALID(env, nativeViewPtr) ||
        !COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_Int))
        return 0;

    try {
        TV(nativeViewPtr)->sync_if_needed();
        return to_jlong_or_not_found( TV(nativeViewPtr)->find_first_int( S(columnIndex), value) );
    } CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_TableView_nativeFindFirstBool(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jboolean value)
{
    if (!VIEW_VALID(env, nativeViewPtr) ||
        !COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_Bool))
        return 0;

    try {
        TV(nativeViewPtr)->sync_if_needed();
        size_t res = TV(nativeViewPtr)->find_first_bool( S(columnIndex), value != 0 ? true : false);
        return to_jlong_or_not_found( res );
    } CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_TableView_nativeFindFirstFloat(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jfloat value)
{
    if (!VIEW_VALID(env, nativeViewPtr) ||
        !COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_Float))
        return 0;

    try {
        TV(nativeViewPtr)->sync_if_needed();
        return to_jlong_or_not_found( TV(nativeViewPtr)->find_first_float( S(columnIndex), value) );
    } CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_TableView_nativeFindFirstDouble(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jdouble value)
{
    if (!VIEW_VALID(env, nativeViewPtr) ||
        !COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_Double))
        return 0;

    try {
        TV(nativeViewPtr)->sync_if_needed();
        return to_jlong_or_not_found( (TV(nativeViewPtr)->find_first_double( S(columnIndex), value)) );
    } CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_TableView_nativeFindFirstDate(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong dateTimeValue)
{
    if (!VIEW_VALID(env, nativeViewPtr) ||
        !COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_DateTime))
        return 0;

    try {
        TV(nativeViewPtr)->sync_if_needed();
        return to_jlong_or_not_found( TV(nativeViewPtr)->find_first_datetime( S(columnIndex), (time_t)dateTimeValue) );
    } CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_TableView_nativeFindFirstString(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jstring value)
{
    if (!VIEW_VALID(env, nativeViewPtr) ||
        !COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_String))
        return 0;

    try {
        TV(nativeViewPtr)->sync_if_needed();
        JStringAccessor value2(env, value); // throws
        size_t searchIndex = TV(nativeViewPtr)->find_first_string( S(columnIndex), value2);
        return to_jlong_or_not_found( searchIndex );
    } CATCH_STD()
    return 0;
}

// FindAll*

JNIEXPORT jlong JNICALL Java_io_realm_internal_TableView_nativeFindAllInt(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong value)
{
    if (!VIEW_VALID(env, nativeViewPtr) ||
        !COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_Int))
        return 0;

    try {
        TV(nativeViewPtr)->sync_if_needed();
        TableView* pResultView = new TableView( TV(nativeViewPtr)->find_all_int( S(columnIndex), value) );
        return reinterpret_cast<jlong>(pResultView);
    } CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_TableView_nativeFindAllBool(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jboolean value)
{
    if (!VIEW_VALID(env, nativeViewPtr) ||
        !COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_Bool))
        return 0;

    try {
        TV(nativeViewPtr)->sync_if_needed();
        TableView* pResultView = new TableView( TV(nativeViewPtr)->find_all_bool( S(columnIndex),
                                                value != 0 ? true : false) );
        return reinterpret_cast<jlong>(pResultView);
    } CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_TableView_nativeFindAllFloat(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jfloat value)
{
    if (!VIEW_VALID(env, nativeViewPtr) ||
        !COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_Float))
        return 0;

    try {
        TV(nativeViewPtr)->sync_if_needed();
        TableView* pResultView = new TableView( TV(nativeViewPtr)->find_all_float( S(columnIndex), value) );
        return reinterpret_cast<jlong>(pResultView);
    } CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_TableView_nativeFindAllDouble(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jdouble value)
{
    if (!VIEW_VALID(env, nativeViewPtr) ||
        !COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_Double))
        return 0;

    try {
        TV(nativeViewPtr)->sync_if_needed();
        TableView* pResultView = new TableView( TV(nativeViewPtr)->find_all_double( S(columnIndex), value) );
        return reinterpret_cast<jlong>(pResultView);
    } CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_TableView_nativeFindAllDate(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong dateTimeValue)
{
    if (!VIEW_VALID(env, nativeViewPtr) ||
        !COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_DateTime))
        return 0;

    try {
        TV(nativeViewPtr)->sync_if_needed();
        TableView* pResultView = new TableView( TV(nativeViewPtr)->find_all_datetime( S(columnIndex),
                                                static_cast<time_t>(dateTimeValue)) );
        return reinterpret_cast<jlong>(pResultView);
    } CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_TableView_nativeFindAllString(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jstring value)
{
    if (!VIEW_VALID(env, nativeViewPtr) ||
        !COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_String))
        return 0;

    try {
        TV(nativeViewPtr)->sync_if_needed();
        JStringAccessor value2(env, value); // throws
        TR((env, "nativeFindAllString(col %d, string '%s') ", columnIndex, StringData(value2).data()));
        TableView* pResultView = new TableView( TV(nativeViewPtr)->find_all_string( S(columnIndex), value2) );
        TR((env, "-- resultview size=%lld.\n", pResultView->size()));
        return reinterpret_cast<jlong>(pResultView);
    } CATCH_STD()
    return 0;
}

// Integer aggregates

JNIEXPORT jlong JNICALL Java_io_realm_internal_TableView_nativeSumInt(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex)
{
    if (!VIEW_VALID(env, nativeViewPtr) ||
        !COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_Int))
        return 0;

    try {
        TV(nativeViewPtr)->sync_if_needed();
        return TV(nativeViewPtr)->sum_int( S(columnIndex));
    } CATCH_STD()
    return 0;
}

JNIEXPORT jdouble JNICALL Java_io_realm_internal_TableView_nativeAverageInt(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex)
{
    if (!VIEW_VALID(env, nativeViewPtr) ||
        !COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_Int))
        return 0;

    try {
        TV(nativeViewPtr)->sync_if_needed();
        return static_cast<jdouble>( TV(nativeViewPtr)->average_int( S(columnIndex)));
    } CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_TableView_nativeMaximumInt(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex)
{
    if (!VIEW_VALID(env, nativeViewPtr) ||
        !COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_Int))
        return 0;

    try {
        TV(nativeViewPtr)->sync_if_needed();
        return TV(nativeViewPtr)->maximum_int( S(columnIndex));
    } CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_TableView_nativeMinimumInt(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex)
{
    if (!VIEW_VALID(env, nativeViewPtr) ||
        !COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_Int))
        return 0;

    try {
        TV(nativeViewPtr)->sync_if_needed();
        return TV(nativeViewPtr)->minimum_int( S(columnIndex));
    } CATCH_STD()
    return 0;
}

// float aggregates

JNIEXPORT jdouble JNICALL Java_io_realm_internal_TableView_nativeSumFloat(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex)
{
    if (!VIEW_VALID(env, nativeViewPtr) ||
        !COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_Float))
        return 0;

    try {
        TV(nativeViewPtr)->sync_if_needed();
        return TV(nativeViewPtr)->sum_float( S(columnIndex));
    } CATCH_STD()
    return 0;
}

JNIEXPORT jdouble JNICALL Java_io_realm_internal_TableView_nativeAverageFloat(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex)
{
    if (!VIEW_VALID(env, nativeViewPtr) ||
        !COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_Float))
        return 0;

    try {
        TV(nativeViewPtr)->sync_if_needed();
        return TV(nativeViewPtr)->average_float( S(columnIndex));
    } CATCH_STD()
    return 0;
}

JNIEXPORT jfloat JNICALL Java_io_realm_internal_TableView_nativeMaximumFloat(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex)
{
    if (!VIEW_VALID(env, nativeViewPtr) ||
        !COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_Float))
        return 0;
    TV(nativeViewPtr)->sync_if_needed();
    try {
        return TV(nativeViewPtr)->maximum_float( S(columnIndex));
    } CATCH_STD()
    return 0;
}

JNIEXPORT jfloat JNICALL Java_io_realm_internal_TableView_nativeMinimumFloat(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex)
{
    if (!VIEW_VALID(env, nativeViewPtr) ||
        !COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_Float))
        return 0;

    try {
        TV(nativeViewPtr)->sync_if_needed();
        return TV(nativeViewPtr)->minimum_float( S(columnIndex));
    } CATCH_STD()
    return 0;
}

// double aggregates

JNIEXPORT jdouble JNICALL Java_io_realm_internal_TableView_nativeSumDouble(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex)
{
    if (!VIEW_VALID(env, nativeViewPtr) ||
        !COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_Double))
        return 0;

    try {
        TV(nativeViewPtr)->sync_if_needed();
        return TV(nativeViewPtr)->sum_double( S(columnIndex));
    } CATCH_STD()
    return 0;
}

JNIEXPORT jdouble JNICALL Java_io_realm_internal_TableView_nativeAverageDouble(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex)
{
    if (!VIEW_VALID(env, nativeViewPtr) ||
        !COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_Double))
        return 0;

    try {
        TV(nativeViewPtr)->sync_if_needed();
        return static_cast<jdouble>( TV(nativeViewPtr)->average_double( S(columnIndex)) );
    } CATCH_STD()
    return 0;
}

JNIEXPORT jdouble JNICALL Java_io_realm_internal_TableView_nativeMaximumDouble(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex)
{
    if (!VIEW_VALID(env, nativeViewPtr) ||
        !COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_Double))
        return 0;

    try {
        TV(nativeViewPtr)->sync_if_needed();
        return TV(nativeViewPtr)->maximum_double( S(columnIndex));
    } CATCH_STD()
    return 0;
}

JNIEXPORT jdouble JNICALL Java_io_realm_internal_TableView_nativeMinimumDouble(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex)
{
    if (!VIEW_VALID(env, nativeViewPtr) ||
        !COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_Double))
        return 0;

    try {
        TV(nativeViewPtr)->sync_if_needed();
        return TV(nativeViewPtr)->minimum_double( S(columnIndex));
    } CATCH_STD()
    return 0;
}


// date aggregates

JNIEXPORT jlong JNICALL Java_io_realm_internal_TableView_nativeMaximumDate(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex)
{
    if (!VIEW_VALID(env, nativeViewPtr) ||
        !COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_DateTime))
        return 0;

    try {
        TV(nativeViewPtr)->sync_if_needed();
        // This exploits the fact that dates are stored as int in core
        return TV(nativeViewPtr)->maximum_int( S(columnIndex));
    } CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_TableView_nativeMinimumDate(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex)
{
    if (!VIEW_VALID(env, nativeViewPtr) ||
        !COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_DateTime))
        return 0;

    try {
        TV(nativeViewPtr)->sync_if_needed();
        // This exploits the fact that dates are stored as int in core
        return TV(nativeViewPtr)->minimum_int( S(columnIndex));
    } CATCH_STD()
    return 0;
}

// sort

JNIEXPORT void JNICALL Java_io_realm_internal_TableView_nativeSort(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jboolean ascending)
{
    if (!VIEW_VALID(env, nativeViewPtr) ||
        !COL_INDEX_VALID(env, TV(nativeViewPtr), columnIndex))
        return;

    TV(nativeViewPtr)->sync_if_needed();
    int colType = TV(nativeViewPtr)->get_column_type( S(columnIndex) );
    switch (colType) {
        case type_Bool:
        case type_Int:
        case type_DateTime:
        case type_Float:
        case type_Double:
        case type_String:
            try {
                TV(nativeViewPtr)->sort( S(columnIndex), ascending != 0 ? true : false);
            } CATCH_STD()
            break;
        default:
            ThrowException(env, IllegalArgument, "Sort is currently only supported on Integer, Float, Double, Boolean, Date, and String columns.");
          return;
    }
}

JNIEXPORT jstring JNICALL Java_io_realm_internal_TableView_nativeToJson(
    JNIEnv *env, jobject, jlong nativeViewPtr)
{
    TableView* tv = TV(nativeViewPtr);
    if (!VIEW_VALID(env, nativeViewPtr))
        return NULL;

    // Write table to string in JSON format
    try {
        TV(nativeViewPtr)->sync_if_needed();
        std::stringstream ss;
        ss.sync_with_stdio(false); // for performance
        tv->to_json(ss);
        const std::string str = ss.str();
        return to_jstring(env, str);
    } CATCH_STD()
    return NULL;
}

JNIEXPORT jstring JNICALL Java_io_realm_internal_TableView_nativeToString(
    JNIEnv *env, jobject, jlong nativeViewPtr, jlong maxRows)
{
    TableView* tv = TV(nativeViewPtr);
    if (!VIEW_VALID(env, nativeViewPtr))
        return NULL;

    try {
        TV(nativeViewPtr)->sync_if_needed();
        std::ostringstream ss;
        ss.sync_with_stdio(false); // for performance
        tv->to_string(ss, S(maxRows));
        const std::string str = ss.str();
        return to_jstring(env, str);
    } CATCH_STD()
    return NULL;
}

JNIEXPORT jstring JNICALL Java_io_realm_internal_TableView_nativeRowToString(
    JNIEnv *env, jobject, jlong nativeViewPtr, jlong rowIndex)
{
    TableView* tv = TV(nativeViewPtr);
    if (!VIEW_VALID(env, nativeViewPtr) || !ROW_INDEX_VALID(env, tv, rowIndex))
        return NULL;

    try {
        TV(nativeViewPtr)->sync_if_needed();
        std::ostringstream ss;
        tv->row_to_string(S(rowIndex), ss);
        const std::string str = ss.str();
        return to_jstring(env, str);
    } CATCH_STD()
    return NULL;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_TableView_nativeWhere
  (JNIEnv *env, jobject, jlong nativeViewPtr)
{
    if (!VIEW_VALID(env, nativeViewPtr))
        return 0;

    try {
        TV(nativeViewPtr)->sync_if_needed();
        TableView* tv = TV(nativeViewPtr);
        Query query = tv->get_parent().where(tv);
        TableQuery* queryPtr = new TableQuery(query);
        return reinterpret_cast<jlong>(queryPtr);
    } CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_TableView_nativeSync(
    JNIEnv* env, jobject, jlong nativeViewPtr)
{
    if (!VIEW_VALID(env, nativeViewPtr))
        return 0;

    try {
        return (jlong) TV(nativeViewPtr)->sync_if_needed();
    } CATCH_STD()
    return 0;
}
