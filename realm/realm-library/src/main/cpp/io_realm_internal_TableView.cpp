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
#include "tablebase_tpl.hpp"
#include "io_realm_internal_TableView.h"
#include "realm/array.hpp"
#include <ostream>

using namespace realm;

// if you disable the validation, please remember to call sync_in_needed() 
#define VIEW_VALID_AND_IN_SYNC(env, ptr) view_valid_and_in_sync(env, ptr)

inline bool view_valid_and_in_sync(JNIEnv* env, jlong nativeViewPtr) {
    bool valid = (TV(nativeViewPtr) != NULL);
    if (valid) {
        if (!TV(nativeViewPtr)->is_attached()) {
            ThrowException(env, IllegalState, "The Realm has been closed and is no longer accessible.");
            return false;
        }
        // depends_on_deleted_linklist() will return true if and only if the current TableView was created from a
        // query on a RealmList and that RealmList was then deleted (as a result of the object being deleted).
        if (!TV(nativeViewPtr)->is_in_sync() && TV(nativeViewPtr)->depends_on_deleted_object()) {
            // This table view is no longer valid. By calling sync_if_needed we ensure it behaves
            // properly as a 0-size TableView.
            TV(nativeViewPtr)->sync_if_needed();
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

JNIEXPORT void JNICALL Java_io_realm_internal_TableView_nativeDistinct(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex)
{
    if (!VIEW_VALID_AND_IN_SYNC(env, nativeViewPtr))
        return;
    if (!COL_INDEX_VALID(env, TV(nativeViewPtr), columnIndex))
        return;
    if (!TV(nativeViewPtr)->get_parent().has_search_index(S(columnIndex))) {
        ThrowException(env, UnsupportedOperation, "The field must be indexed before distinct() can be used.");
        return;
    }
    try {
        switch (TV(nativeViewPtr)->get_column_type(S(columnIndex))) {
            case type_Bool:
            case type_Int:
            case type_String:
            case type_Timestamp:
                TV(nativeViewPtr)->distinct(S(columnIndex));
                break;
            default:
                ThrowException(env, IllegalArgument, "Invalid type - Only String, Date, boolean, byte, short, int, long and their boxed variants are supported.");
                break;
        }
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableView_nativeDistinctMulti(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlongArray columnIndexes)
{
    if (!VIEW_VALID_AND_IN_SYNC(env, nativeViewPtr))
        return;
    try {
        TableView* tv = TV(nativeViewPtr);
        JniLongArray indexes(env, columnIndexes);
        jsize indexes_len = indexes.len();
        std::vector<std::vector<size_t>> columns;
        std::vector<bool> ascending;
        for (int i = 0; i < indexes_len; ++i) {
            if (!COL_INDEX_VALID(env, tv, indexes[i])) {
                return;
            }
            if (!tv->get_parent().has_search_index(S(indexes[i]))) {
                ThrowException(env, IllegalArgument, "The field must be indexed before distinct(...) can be used.");
                return;
            }
            switch (tv->get_column_type(S(indexes[i]))) {
                case type_Bool:
                case type_Int:
                case type_String:
                case type_Timestamp:
                    columns.push_back(std::vector<size_t> { S(indexes[i]) });
                    ascending.push_back(true);
                    break;
                default:
                    ThrowException(env, IllegalArgument, "Invalid type - Only String, Date, boolean, byte, short, int, long and their boxed variants are supported.");
                    return;
            }
        }
        tv->distinct(SortDescriptor(tv->get_parent(), columns, ascending));
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableView_nativePivot(
    JNIEnv *env, jobject, jlong dataTablePtr, jlong stringCol, jlong intCol, jint operation, jlong resultTablePtr)
{

    try {
        TV(dataTablePtr)->sync_if_needed();
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
    try {
        if (!VIEW_VALID_AND_IN_SYNC(env, nativeViewPtr))
            return 0;
    } CATCH_STD()
    return TV(nativeViewPtr)->size();   // noexcept
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_TableView_nativeGetSourceRowIndex
(JNIEnv *env, jobject, jlong nativeViewPtr, jlong rowIndex)
{
    try {
        if (!VIEW_VALID_AND_IN_SYNC(env, nativeViewPtr))
            return to_jlong_or_not_found(-1);
        if (!ROW_INDEX_VALID(env, TV(nativeViewPtr), rowIndex))
            return to_jlong_or_not_found(-1);
        if (!TV(nativeViewPtr)->is_row_attached(rowIndex))
            return to_jlong_or_not_found(-1);
    } CATCH_STD()
    return TV(nativeViewPtr)->get_source_ndx(S(rowIndex));   // noexcept
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_TableView_nativeGetColumnCount
  (JNIEnv *env, jobject, jlong nativeViewPtr)
{
    try {
        if (!VIEW_VALID_AND_IN_SYNC(env, nativeViewPtr))
            return 0;
    } CATCH_STD()
    return TV(nativeViewPtr)->get_column_count();
}

JNIEXPORT jstring JNICALL Java_io_realm_internal_TableView_nativeGetColumnName
  (JNIEnv *env, jobject, jlong nativeViewPtr, jlong columnIndex)
{
    try {
        if (!VIEW_VALID_AND_IN_SYNC(env, nativeViewPtr) || !COL_INDEX_VALID(env, TV(nativeViewPtr), columnIndex))
            return NULL;
        return to_jstring(env, TV(nativeViewPtr)->get_column_name( S(columnIndex)));
    } CATCH_STD()
    return NULL;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_TableView_nativeGetColumnIndex
   (JNIEnv *env, jobject, jlong nativeViewPtr, jstring columnName)

{
    try {
        if (!VIEW_VALID_AND_IN_SYNC(env, nativeViewPtr))
            return 0;
        JStringAccessor columnName2(env, columnName); // throws
        return to_jlong_or_not_found( TV(nativeViewPtr)->get_column_index(columnName2) ); // noexcept
    } CATCH_STD()
    return 0;
}

JNIEXPORT jint JNICALL Java_io_realm_internal_TableView_nativeGetColumnType
  (JNIEnv *env, jobject, jlong nativeViewPtr, jlong columnIndex)
{
    try {
        if (!VIEW_VALID_AND_IN_SYNC(env, nativeViewPtr) || !COL_INDEX_VALID(env, TV(nativeViewPtr), columnIndex))
            return 0;
    } CATCH_STD()
    return static_cast<int>( TV(nativeViewPtr)->get_column_type( S(columnIndex)) );
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_TableView_nativeGetLong(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex)
{
    try {
        if (!VIEW_VALID_AND_IN_SYNC(env, nativeViewPtr) ||
            !INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex, type_Int))
            return 0;
    } CATCH_STD()
    return TV(nativeViewPtr)->get_int( S(columnIndex), S(rowIndex));  // noexcept
}

JNIEXPORT jboolean JNICALL Java_io_realm_internal_TableView_nativeGetBoolean(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex)
{
    try {
        if (!VIEW_VALID_AND_IN_SYNC(env, nativeViewPtr) ||
            !INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex, type_Bool))
            return 0;
    } CATCH_STD()
    return TV(nativeViewPtr)->get_bool( S(columnIndex), S(rowIndex));  // noexcept
}

JNIEXPORT jfloat JNICALL Java_io_realm_internal_TableView_nativeGetFloat(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex)
{
    try {
        if (!VIEW_VALID_AND_IN_SYNC(env, nativeViewPtr) ||
            !INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex, type_Float))
            return 0;
    } CATCH_STD()
    return TV(nativeViewPtr)->get_float( S(columnIndex), S(rowIndex));  // noexcept
}

JNIEXPORT jdouble JNICALL Java_io_realm_internal_TableView_nativeGetDouble(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex)
{
    try {
        if (!VIEW_VALID_AND_IN_SYNC(env, nativeViewPtr) ||
            !INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex, type_Double))
            return 0;
    } CATCH_STD()
    return TV(nativeViewPtr)->get_double( S(columnIndex), S(rowIndex));  // noexcept
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_TableView_nativeGetTimestamp(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex)
{
    try {
        if (!VIEW_VALID_AND_IN_SYNC(env, nativeViewPtr) ||
            !INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex, type_Timestamp))
            return 0;
    } CATCH_STD()
    return to_milliseconds(TV(nativeViewPtr)->get_timestamp( S(columnIndex), S(rowIndex)));
}

JNIEXPORT jstring JNICALL Java_io_realm_internal_TableView_nativeGetString(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex)
{
    try {
        if (!VIEW_VALID_AND_IN_SYNC(env, nativeViewPtr) ||
            !INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex, type_String))
            return NULL;
        
        return to_jstring(env, TV(nativeViewPtr)->get_string( S(columnIndex), S(rowIndex)) // noexcept
                          );
    } CATCH_STD()
    return NULL;
}

JNIEXPORT jbyteArray JNICALL Java_io_realm_internal_TableView_nativeGetByteArray(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex)
{
    try {
        if (!VIEW_VALID_AND_IN_SYNC(env, nativeViewPtr) ||
            !INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex, type_Binary))
            return NULL;
        return tbl_GetByteArray<TableView>(env, nativeViewPtr, columnIndex, rowIndex);
    } CATCH_STD()
    return NULL;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_TableView_nativeGetLink
  (JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex)
{
    try {
        if (!VIEW_VALID_AND_IN_SYNC(env, nativeViewPtr) ||
            !INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex, type_Link))
            return 0;
    } CATCH_STD()
    return TV(nativeViewPtr)->get_link( S(columnIndex), S(rowIndex));  // noexcept
}

// Setters

JNIEXPORT void JNICALL Java_io_realm_internal_TableView_nativeSetLong(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex, jlong value)
{
    try {
        if (!VIEW_VALID_AND_IN_SYNC(env, nativeViewPtr) ||
            !INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex, type_Int))
            return;
        TV(nativeViewPtr)->set_int( S(columnIndex), S(rowIndex), value);
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableView_nativeSetBoolean(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex, jboolean value)
{
    try {
        if (!VIEW_VALID_AND_IN_SYNC(env, nativeViewPtr) ||
            !INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex, type_Bool))
            return;
        TV(nativeViewPtr)->set_bool( S(columnIndex), S(rowIndex), value != 0 ? true : false);
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableView_nativeSetFloat(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex, jfloat value)
{
    try {
        if (!VIEW_VALID_AND_IN_SYNC(env, nativeViewPtr) ||
            !INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex, type_Float))
            return;
        TV(nativeViewPtr)->set_float( S(columnIndex), S(rowIndex), value);
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableView_nativeSetDouble(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex, jdouble value)
{
    try {
        if (!VIEW_VALID_AND_IN_SYNC(env, nativeViewPtr) ||
            !INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex, type_Double))
            return;
        TV(nativeViewPtr)->set_double( S(columnIndex), S(rowIndex), value);
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableView_nativeSetTimestampValue(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex, jlong timestampValue)
{
    try {
        if (!VIEW_VALID_AND_IN_SYNC(env, nativeViewPtr) ||
            !INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex, type_Timestamp))
            return;
        TV(nativeViewPtr)->set_timestamp( S(columnIndex), S(rowIndex), from_milliseconds(timestampValue));
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableView_nativeSetString(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex, jstring value)
{
    try {
        if (!VIEW_VALID_AND_IN_SYNC(env, nativeViewPtr) ||
            !INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex, type_String))
            return;
        if (!TV(nativeViewPtr)->get_parent().is_nullable(S(columnIndex))) {
            ThrowNullValueException(env, &(TV(nativeViewPtr)->get_parent()), S(columnIndex));
            return;
        }
        JStringAccessor value2(env, value);  // throws
        TV(nativeViewPtr)->set_string( S(columnIndex), S(rowIndex), value2);
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableView_nativeSetByteArray(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex, jbyteArray byteArray)
{
    try {
        if (!VIEW_VALID_AND_IN_SYNC(env, nativeViewPtr) ||
            !INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex, type_Binary))
            return;

        JniByteArray bytesAccessor(env, byteArray);
        TV(nativeViewPtr)->set_binary(S(columnIndex), S(rowIndex), bytesAccessor);
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableView_nativeSetLink
  (JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex, jlong targetIndex)
{
    try {
        if (!VIEW_VALID_AND_IN_SYNC(env, nativeViewPtr) ||
            !INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex, type_Link))
            return;
        TV(nativeViewPtr)->set_link( S(columnIndex), S(rowIndex), S(targetIndex));
    } CATCH_STD()
}

JNIEXPORT jboolean JNICALL Java_io_realm_internal_TableView_nativeIsNullLink
  (JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex)
{
    try {
        if (!VIEW_VALID_AND_IN_SYNC(env, nativeViewPtr) ||
            !INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex, type_Link))
            return 0;
        return TV(nativeViewPtr)->is_null_link( S(columnIndex), S(rowIndex));
    } CATCH_STD()
    return 0;
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableView_nativeNullifyLink
  (JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex)
{
    try {
        if (!VIEW_VALID_AND_IN_SYNC(env, nativeViewPtr) ||
            !INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex, type_Link))
            return;
        TV(nativeViewPtr)->nullify_link( S(columnIndex), S(rowIndex));
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableView_nativeClear(
    JNIEnv* env, jobject, jlong nativeViewPtr)
{
    try {
        if (!VIEW_VALID_AND_IN_SYNC(env, nativeViewPtr))
            return;
        TV(nativeViewPtr)->clear(RemoveMode::unordered);
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableView_nativeRemoveRow(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong rowIndex)
{
    try {
        if (!VIEW_VALID_AND_IN_SYNC(env, nativeViewPtr) ||
            !ROW_INDEX_VALID(env, TV(nativeViewPtr), rowIndex))
            return;
        TV(nativeViewPtr)->remove( S(rowIndex), RemoveMode::unordered);
    } CATCH_STD()
}

// FindFirst*

JNIEXPORT jlong JNICALL Java_io_realm_internal_TableView_nativeFindFirstInt(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong value)
{
    try {
        if (!VIEW_VALID_AND_IN_SYNC(env, nativeViewPtr) ||
            !COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_Int))
            return 0;
        return to_jlong_or_not_found( TV(nativeViewPtr)->find_first_int( S(columnIndex), value) );
    } CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_TableView_nativeFindFirstBool(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jboolean value)
{
    try {
        if (!VIEW_VALID_AND_IN_SYNC(env, nativeViewPtr) ||
            !COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_Bool))
            return 0;
        size_t res = TV(nativeViewPtr)->find_first_bool( S(columnIndex), value != 0 ? true : false);
        return to_jlong_or_not_found( res );
    } CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_TableView_nativeFindFirstFloat(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jfloat value)
{
    try {
        if (!VIEW_VALID_AND_IN_SYNC(env, nativeViewPtr) ||
            !COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_Float))
            return 0;
        return to_jlong_or_not_found( TV(nativeViewPtr)->find_first_float( S(columnIndex), value) );
    } CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_TableView_nativeFindFirstDouble(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jdouble value)
{
    try {
        if (!VIEW_VALID_AND_IN_SYNC(env, nativeViewPtr) ||
            !COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_Double))
            return 0;
        return to_jlong_or_not_found( (TV(nativeViewPtr)->find_first_double( S(columnIndex), value)) );
    } CATCH_STD()
    return 0;
}

// FIXME: find_first_timestamp() isn't implemented
/*
JNIEXPORT jlong JNICALL Java_io_realm_internal_TableView_nativeFindFirstDate(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong dateTimeValue)
{
    try {
        if (!VIEW_VALID_AND_IN_SYNC(env, nativeViewPtr) ||
            !COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_DateTime))
            return 0;
        return to_jlong_or_not_found( TV(nativeViewPtr)->find_first_datetime( S(columnIndex), DateTime(dateTimeValue)) );
    } CATCH_STD()
    return 0;
}
*/

JNIEXPORT jlong JNICALL Java_io_realm_internal_TableView_nativeFindFirstString(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jstring value)
{
    try {
        if (!VIEW_VALID_AND_IN_SYNC(env, nativeViewPtr) ||
            !COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_String))
            return 0;
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
    try {
        if (!VIEW_VALID_AND_IN_SYNC(env, nativeViewPtr) ||
            !COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_Int))
            return 0;
        TableView* pResultView = new TableView( TV(nativeViewPtr)->find_all_int( S(columnIndex), value) );
        return reinterpret_cast<jlong>(pResultView);
    } CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_TableView_nativeFindAllBool(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jboolean value)
{
    try {
        if (!VIEW_VALID_AND_IN_SYNC(env, nativeViewPtr) ||
            !COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_Bool))
            return 0;
        TableView* pResultView = new TableView( TV(nativeViewPtr)->find_all_bool( S(columnIndex),
                                                value != 0 ? true : false) );
        return reinterpret_cast<jlong>(pResultView);
    } CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_TableView_nativeFindAllFloat(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jfloat value)
{
    try {
        if (!VIEW_VALID_AND_IN_SYNC(env, nativeViewPtr) ||
            !COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_Float))
            return 0;
        TableView* pResultView = new TableView( TV(nativeViewPtr)->find_all_float( S(columnIndex), value) );
        return reinterpret_cast<jlong>(pResultView);
    } CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_TableView_nativeFindAllDouble(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jdouble value)
{
    try {
        if (!VIEW_VALID_AND_IN_SYNC(env, nativeViewPtr) ||
            !COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_Double))
            return 0;
        TableView* pResultView = new TableView( TV(nativeViewPtr)->find_all_double( S(columnIndex), value) );
        return reinterpret_cast<jlong>(pResultView);
    } CATCH_STD()
    return 0;
}

// FIXME: find_all_timestamp() isn't implemented
/*
JNIEXPORT jlong JNICALL Java_io_realm_internal_TableView_nativeFindAllDate(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong dateTimeValue)
{
    try {
        if (!VIEW_VALID_AND_IN_SYNC(env, nativeViewPtr) ||
            !COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_DateTime))
            return 0;
        TableView* pResultView = new TableView( TV(nativeViewPtr)->find_all_datetime( S(columnIndex),
                                                DateTime(dateTimeValue)) );
        return reinterpret_cast<jlong>(pResultView);
    } CATCH_STD()
    return 0;
}
*/

JNIEXPORT jlong JNICALL Java_io_realm_internal_TableView_nativeFindAllString(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jstring value)
{
    try {
        if (!VIEW_VALID_AND_IN_SYNC(env, nativeViewPtr) ||
            !COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_String))
            return 0;
        JStringAccessor value2(env, value); // throws
        TableView* pResultView = new TableView( TV(nativeViewPtr)->find_all_string( S(columnIndex), value2) );
        return reinterpret_cast<jlong>(pResultView);
    } CATCH_STD()
    return 0;
}

// Integer aggregates

JNIEXPORT jlong JNICALL Java_io_realm_internal_TableView_nativeSumInt(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex)
{
    try {
        if (!VIEW_VALID_AND_IN_SYNC(env, nativeViewPtr) ||
            !COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_Int))
            return 0;
        return TV(nativeViewPtr)->sum_int( S(columnIndex));
    } CATCH_STD()
    return 0;
}

JNIEXPORT jdouble JNICALL Java_io_realm_internal_TableView_nativeAverageInt(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex)
{
    try {
        if (!VIEW_VALID_AND_IN_SYNC(env, nativeViewPtr) ||
            !COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_Int))
            return 0;
        return static_cast<jdouble>( TV(nativeViewPtr)->average_int( S(columnIndex)));
    } CATCH_STD()
    return 0;
}

JNIEXPORT jobject JNICALL Java_io_realm_internal_TableView_nativeMaximumInt(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex)
{
    try {
        if (!VIEW_VALID_AND_IN_SYNC(env, nativeViewPtr) ||
            !COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_Int))
            return NULL;
        size_t return_ndx;
        int64_t result = TV(nativeViewPtr)->maximum_int( S(columnIndex), &return_ndx);
        if (return_ndx != npos) {
            return NewLong(env, result);
        }
    } CATCH_STD()
    return NULL;
}

JNIEXPORT jobject JNICALL Java_io_realm_internal_TableView_nativeMinimumInt(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex)
{
    try {
        if (!VIEW_VALID_AND_IN_SYNC(env, nativeViewPtr) ||
            !COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_Int))
            return NULL;
        size_t return_ndx;
        int64_t result = TV(nativeViewPtr)->minimum_int( S(columnIndex), &return_ndx);
        if (return_ndx != npos) {
            return NewLong(env, result);
        }
    } CATCH_STD()
    return NULL;
}

// float aggregates

JNIEXPORT jdouble JNICALL Java_io_realm_internal_TableView_nativeSumFloat(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex)
{
    try {
        if (!VIEW_VALID_AND_IN_SYNC(env, nativeViewPtr) ||
            !COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_Float))
            return 0;
        return TV(nativeViewPtr)->sum_float( S(columnIndex));
    } CATCH_STD()
    return 0;
}

JNIEXPORT jdouble JNICALL Java_io_realm_internal_TableView_nativeAverageFloat(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex)
{
    try {
        if (!VIEW_VALID_AND_IN_SYNC(env, nativeViewPtr) ||
            !COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_Float))
            return 0;
        return TV(nativeViewPtr)->average_float( S(columnIndex));
    } CATCH_STD()
    return 0;
}

JNIEXPORT jobject JNICALL Java_io_realm_internal_TableView_nativeMaximumFloat(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex)
{
    try {
        if (!VIEW_VALID_AND_IN_SYNC(env, nativeViewPtr) ||
            !COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_Float))
            return NULL;
        size_t return_ndx;
        float result = TV(nativeViewPtr)->maximum_float( S(columnIndex), &return_ndx);
        if (return_ndx != npos) {
            return NewFloat(env, result);
        }
    } CATCH_STD()
    return NULL;
}

JNIEXPORT jobject JNICALL Java_io_realm_internal_TableView_nativeMinimumFloat(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex)
{
    try {
        if (!VIEW_VALID_AND_IN_SYNC(env, nativeViewPtr) ||
            !COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_Float))
            return NULL;
        size_t return_ndx;
        float result = TV(nativeViewPtr)->minimum_float( S(columnIndex), &return_ndx);
        if (return_ndx != npos) {
            return NewFloat(env, result);
        }
    } CATCH_STD()
    return NULL;
}

// double aggregates

JNIEXPORT jdouble JNICALL Java_io_realm_internal_TableView_nativeSumDouble(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex)
{
    try {
        if (!VIEW_VALID_AND_IN_SYNC(env, nativeViewPtr) ||
            !COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_Double))
            return 0;
        return TV(nativeViewPtr)->sum_double( S(columnIndex));
    } CATCH_STD()
    return 0;
}

JNIEXPORT jdouble JNICALL Java_io_realm_internal_TableView_nativeAverageDouble(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex)
{
    try {
        if (!VIEW_VALID_AND_IN_SYNC(env, nativeViewPtr) ||
            !COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_Double))
            return 0;
        return static_cast<jdouble>( TV(nativeViewPtr)->average_double( S(columnIndex)) );
    } CATCH_STD()
    return 0;
}

JNIEXPORT jobject JNICALL Java_io_realm_internal_TableView_nativeMaximumDouble(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex)
{
    try {
        if (!VIEW_VALID_AND_IN_SYNC(env, nativeViewPtr) ||
            !COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_Double))
            return NULL;
        size_t return_ndx;
        double result = TV(nativeViewPtr)->maximum_double( S(columnIndex), &return_ndx);
        if (return_ndx != npos) {
            return NewDouble(env, result);
        }
    } CATCH_STD()
    return NULL;
}

JNIEXPORT jobject JNICALL Java_io_realm_internal_TableView_nativeMinimumDouble(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex)
{
    try {
        if (!VIEW_VALID_AND_IN_SYNC(env, nativeViewPtr) ||
            !COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_Double))
            return NULL;
        size_t return_ndx;
        double result = TV(nativeViewPtr)->minimum_double( S(columnIndex), &return_ndx);
        if (return_ndx != npos) {
            return NewDouble(env, result);
        }
    } CATCH_STD()
    return NULL;
}


// date aggregates

JNIEXPORT jobject JNICALL Java_io_realm_internal_TableView_nativeMaximumTimestamp(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex)
{
    try {
        if (!VIEW_VALID_AND_IN_SYNC(env, nativeViewPtr) ||
            !COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_Timestamp))
            return NULL;

        size_t return_ndx;
        Timestamp result = TV(nativeViewPtr)->maximum_timestamp( S(columnIndex), &return_ndx);
        if (return_ndx != npos) {
            return NewLong(env, to_milliseconds(result));
        }
    } CATCH_STD()
    return NULL;
}

JNIEXPORT jobject JNICALL Java_io_realm_internal_TableView_nativeMinimumTimestamp(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex)
{
    try {
        if (!VIEW_VALID_AND_IN_SYNC(env, nativeViewPtr) ||
            !COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_Timestamp))
            return NULL;

        size_t return_ndx;
        Timestamp result = TV(nativeViewPtr)->minimum_timestamp( S(columnIndex), &return_ndx);
        if (return_ndx != npos) {
            return NewLong(env, to_milliseconds(result));
        }
    } CATCH_STD()
    return NULL;
}

// sort
JNIEXPORT void JNICALL Java_io_realm_internal_TableView_nativeSort(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jboolean ascending)
{
    try {
        if (!VIEW_VALID_AND_IN_SYNC(env, nativeViewPtr) ||
            !COL_INDEX_VALID(env, TV(nativeViewPtr), columnIndex))
            return;
        int colType = TV(nativeViewPtr)->get_column_type( S(columnIndex) );
    
        switch (colType) {
            case type_Bool:
            case type_Int:
            case type_Float:
            case type_Double:
            case type_String:
            case type_Timestamp:
                TV(nativeViewPtr)->sort( S(columnIndex), ascending != 0 ? true : false);
                break;
            default:
                ThrowException(env, IllegalArgument, "Sort is not supported on binary data, object references and RealmList.");
                return;
        }
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableView_nativeSortMulti(
  JNIEnv* env, jobject, jlong nativeViewPtr, jlongArray columnIndices, jbooleanArray ascending)
{
    try {
        if (!VIEW_VALID_AND_IN_SYNC(env, nativeViewPtr))
            return;

        JniLongArray long_arr(env, columnIndices);
        JniBooleanArray bool_arr(env, ascending);
        jsize arr_len = long_arr.len();
        jsize asc_len = bool_arr.len();

        if (arr_len == 0) {
            ThrowException(env, IllegalArgument, "You must provide at least one field name.");
            return;
        }
        if (asc_len == 0) {
            ThrowException(env, IllegalArgument, "You must provide at least one sort order.");
            return;
        }
        if (arr_len != asc_len) {
            ThrowException(env, IllegalArgument, "Number of fields and sort orders do not match.");
            return;
        }

        TableView* tv = TV(nativeViewPtr);
        std::vector<std::vector<size_t>> indices;
        std::vector<bool> ascendings;

        for (int i = 0; i < arr_len; ++i) {
            if (!COL_INDEX_VALID(env, tv, long_arr[i])) {
                return;
            }
            int colType = tv->get_column_type( S(long_arr[i]) );
            switch (colType) {
                case type_Bool:
                case type_Int:
                case type_Float:
                case type_Double:
                case type_String:
                case type_Timestamp:
                    indices.push_back(std::vector<size_t> { S(long_arr[i]) });
                    ascendings.push_back( B(bool_arr[i]) );
                    break;
                default:
                    ThrowException(env, IllegalArgument, "Sort is not supported on binary data, object references and RealmList.");
                    return;
            }
        }
        tv->sort(SortDescriptor(tv->get_parent(), indices, ascendings));
    } CATCH_STD()
}

JNIEXPORT jstring JNICALL Java_io_realm_internal_TableView_nativeToJson(
    JNIEnv *env, jobject, jlong nativeViewPtr)
{
    try {
        if (!VIEW_VALID_AND_IN_SYNC(env, nativeViewPtr))
            return NULL;
        
        // Write table to string in JSON format
        std::stringstream ss;
        ss.sync_with_stdio(false); // for performance
        TV(nativeViewPtr)->to_json(ss);
        const std::string str = ss.str();
        return to_jstring(env, str);
    } CATCH_STD()
    return NULL;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_TableView_nativeWhere(
    JNIEnv *env, jobject, jlong nativeViewPtr)
{
    TR_ENTER_PTR(env, nativeViewPtr)
    try {
        if (!VIEW_VALID_AND_IN_SYNC(env, nativeViewPtr))
            return 0;

        Query *queryPtr = new Query(TV(nativeViewPtr)->get_parent().where(TV(nativeViewPtr)));
        return reinterpret_cast<jlong>(queryPtr);
    } CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_TableView_nativeSyncIfNeeded(
    JNIEnv* env, jobject, jlong nativeViewPtr)
{
    bool valid = (TV(nativeViewPtr) != NULL);
    if (valid) {
        if (!TV(nativeViewPtr)->is_attached()) {
            ThrowException(env, IllegalState, "The Realm has been closed and is no longer accessible.");
            return 0;
        }
    }
    try {
        return (jlong) TV(nativeViewPtr)->sync_if_needed();
    } CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_TableView_nativeFindBySourceNdx
        (JNIEnv *env, jobject, jlong nativeViewPtr, jlong sourceIndex)
{
    TR_ENTER_PTR(env, nativeViewPtr);
    try {
        if (!VIEW_VALID_AND_IN_SYNC(env, nativeViewPtr) || !ROW_INDEX_VALID(env, &(TV(nativeViewPtr)->get_parent()), sourceIndex))
            return -1;

        size_t ndx = TV(nativeViewPtr)->find_by_source_ndx(sourceIndex);
        return to_jlong_or_not_found(ndx);
    } CATCH_STD()
    return -1;
}
