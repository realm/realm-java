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
#include <exception>
#include <vector>

using namespace std;
using namespace realm;

// if you disable the validation, please remember to call sync_in_needed()
#define VIEW_VALID_AND_IN_SYNC(env, ptr) view_valid_and_in_sync(env, ptr)

inline void view_valid_and_in_sync(JNIEnv* env, jlong nativeViewPtr) {
    bool valid = (TV(nativeViewPtr) != NULL);
    if (valid) {
        if (!TV(nativeViewPtr)->is_attached()) {
            throw illegal_state("The Realm has been closed and is no longer accessible.");
        }
        // depends_on_deleted_linklist() will return true if and only if the current TableView was created from a
        // query on a RealmList and that RealmList was then deleted (as a result of the object being deleted).
        if (!TV(nativeViewPtr)->is_in_sync() && TV(nativeViewPtr)->depends_on_deleted_object()) {
            // This table view is no longer valid. By calling sync_if_needed we ensure it behaves
            // properly as a 0-size TableView.
            TV(nativeViewPtr)->sync_if_needed();
        }
    }
}


JNIEXPORT jlong JNICALL Java_io_realm_internal_TableView_createNativeTableView(
    JNIEnv* env, jobject, jobject, jlong)
{
    return try_catch<jlong>(env, [&]() {
        return reinterpret_cast<jlong>(new TableView());
    });
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableView_nativeDistinct(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex)
{
    try_catch<void>(env, [&]() {
        VIEW_VALID_AND_IN_SYNC(env, nativeViewPtr);
        COL_INDEX_VALID(env, TV(nativeViewPtr), columnIndex);
        if (!TV(nativeViewPtr)->get_parent().has_search_index(S(columnIndex))) {
            throw unsupported_operation("The field must be indexed before distinct() can be used.");
        }

        switch (TV(nativeViewPtr)->get_column_type(S(columnIndex))) {
            case type_Bool:
            case type_Int:
            case type_String:
            case type_Timestamp:
                TV(nativeViewPtr)->distinct(S(columnIndex));
                break;
            default:
                throw invalid_argument("Invalid type - Only String, Date, boolean, byte, short, int, long and their boxed variants are supported.");
        }
    });
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableView_nativeDistinctMulti(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlongArray columnIndexes)
{
    try_catch<void>(env, [&]() {
        VIEW_VALID_AND_IN_SYNC(env, nativeViewPtr);
        TableView* tv = TV(nativeViewPtr);
        JniLongArray indexes(env, columnIndexes);
        jsize indexes_len = indexes.len();
        std::vector<std::vector<size_t>> columns;
        std::vector<bool> ascending;
        for (int i = 0; i < indexes_len; ++i) {
            COL_INDEX_VALID(env, tv, indexes[i]);
            if (!tv->get_parent().has_search_index(S(indexes[i]))) {
                throw invalid_argument("The field must be indexed before distinct(...) can be used.");
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
                    throw invalid_argument("Invalid type - Only String, Date, boolean, byte, short, int, long and their boxed variants are supported.");
            }
        }
        tv->distinct(SortDescriptor(tv->get_parent(), columns, ascending));
    });
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableView_nativePivot(
    JNIEnv *env, jobject, jlong dataTablePtr, jlong stringCol, jlong intCol, jint operation, jlong resultTablePtr)
{

    try_catch<void>(env, [&]() {
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
                throw unsupported_operation("No pivot operation specified.");
        }
        dataTable->aggregate(S(stringCol), S(intCol), pivotOp, *resultTable);
    });
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableView_nativeClose(
    JNIEnv*, jclass, jlong nativeViewPtr)
{
    delete TV(nativeViewPtr);
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_TableView_nativeSize(
    JNIEnv* env, jobject, jlong nativeViewPtr)
{
    return try_catch<jlong>(env, [&]() {
        VIEW_VALID_AND_IN_SYNC(env, nativeViewPtr);
        return TV(nativeViewPtr)->size();
    });
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_TableView_nativeGetSourceRowIndex
(JNIEnv *env, jobject, jlong nativeViewPtr, jlong rowIndex)
{
    return try_catch<jlong>(env, [&]() {
        VIEW_VALID_AND_IN_SYNC(env, nativeViewPtr);
        ROW_INDEX_VALID(env, TV(nativeViewPtr), rowIndex);
        if (!TV(nativeViewPtr)->is_row_attached(rowIndex)) {
            return to_jlong_or_not_found(-1);
        }
        return static_cast<jlong>(TV(nativeViewPtr)->get_source_ndx(S(rowIndex)));
    });
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_TableView_nativeGetColumnCount
  (JNIEnv *env, jobject, jlong nativeViewPtr)
{
    return try_catch<jlong>(env, [&]() {
        VIEW_VALID_AND_IN_SYNC(env, nativeViewPtr);
        return TV(nativeViewPtr)->get_column_count();
    });
}

JNIEXPORT jstring JNICALL Java_io_realm_internal_TableView_nativeGetColumnName
  (JNIEnv *env, jobject, jlong nativeViewPtr, jlong columnIndex)
{
    return try_catch<jstring>(env, [&]() {
        VIEW_VALID_AND_IN_SYNC(env, nativeViewPtr);
        COL_INDEX_VALID(env, TV(nativeViewPtr), columnIndex);
        return to_jstring(env, TV(nativeViewPtr)->get_column_name( S(columnIndex)));
    });
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_TableView_nativeGetColumnIndex
   (JNIEnv *env, jobject, jlong nativeViewPtr, jstring columnName)

{
    return try_catch<jlong>(env, [&]() {
        VIEW_VALID_AND_IN_SYNC(env, nativeViewPtr);
        JStringAccessor columnName2(env, columnName);
        return to_jlong_or_not_found(TV(nativeViewPtr)->get_column_index(columnName2));
    });
}

JNIEXPORT jint JNICALL Java_io_realm_internal_TableView_nativeGetColumnType
  (JNIEnv *env, jobject, jlong nativeViewPtr, jlong columnIndex)
{
    return try_catch<jint>(env, [&]() {
        VIEW_VALID_AND_IN_SYNC(env, nativeViewPtr);
        COL_INDEX_VALID(env, TV(nativeViewPtr), columnIndex);
        return static_cast<int>( TV(nativeViewPtr)->get_column_type( S(columnIndex)) );
    });
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_TableView_nativeGetLong(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex)
{
    return try_catch<jlong>(env, [&]() {
        VIEW_VALID_AND_IN_SYNC(env, nativeViewPtr);
        INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex, type_Int);
        return TV(nativeViewPtr)->get_int( S(columnIndex), S(rowIndex));
    });
}

JNIEXPORT jboolean JNICALL Java_io_realm_internal_TableView_nativeGetBoolean(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex)
{
    return try_catch<jboolean>(env, [&]() {
        VIEW_VALID_AND_IN_SYNC(env, nativeViewPtr);
        INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex, type_Bool);
        return TV(nativeViewPtr)->get_bool( S(columnIndex), S(rowIndex));
    });
}

JNIEXPORT jfloat JNICALL Java_io_realm_internal_TableView_nativeGetFloat(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex)
{
    return try_catch<jfloat>(env, [&]() {
        VIEW_VALID_AND_IN_SYNC(env, nativeViewPtr);
        INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex, type_Float);
        return TV(nativeViewPtr)->get_float( S(columnIndex), S(rowIndex));
    });
}

JNIEXPORT jdouble JNICALL Java_io_realm_internal_TableView_nativeGetDouble(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex)
{
    return try_catch<jdouble>(env, [&]() {
        VIEW_VALID_AND_IN_SYNC(env, nativeViewPtr);
        INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex, type_Double);
        return TV(nativeViewPtr)->get_double( S(columnIndex), S(rowIndex));
    });
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_TableView_nativeGetTimestamp(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex)
{
    return try_catch<jlong>(env, [&]() {
        VIEW_VALID_AND_IN_SYNC(env, nativeViewPtr);
        INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex, type_Timestamp);
        return to_milliseconds(TV(nativeViewPtr)->get_timestamp( S(columnIndex), S(rowIndex)));
    });
}

JNIEXPORT jstring JNICALL Java_io_realm_internal_TableView_nativeGetString(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex)
{
    return try_catch<jstring>(env, [&]() {
        VIEW_VALID_AND_IN_SYNC(env, nativeViewPtr);
        INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex, type_String);
        return to_jstring(env, TV(nativeViewPtr)->get_string(S(columnIndex), S(rowIndex)));
    });
}

JNIEXPORT jbyteArray JNICALL Java_io_realm_internal_TableView_nativeGetByteArray(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex)
{
    return try_catch<jbyteArray>(env, [&]() {
        VIEW_VALID_AND_IN_SYNC(env, nativeViewPtr);
        INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex, type_Binary);
        return tbl_GetByteArray<TableView>(env, nativeViewPtr, columnIndex, rowIndex);
    });
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_TableView_nativeGetLink
  (JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex)
{
    return try_catch<jlong>(env, [&]() {
        VIEW_VALID_AND_IN_SYNC(env, nativeViewPtr);
        INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex, type_Link);
        return TV(nativeViewPtr)->get_link(S(columnIndex), S(rowIndex));
    });
}

JNIEXPORT jboolean JNICALL Java_io_realm_internal_TableView_nativeIsNull
        (JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex)
{
    return try_catch<jboolean>(env, [&]() {
        VIEW_VALID_AND_IN_SYNC(env, nativeViewPtr);
        return TV(nativeViewPtr)->get_parent().is_null(S(columnIndex), TV(nativeViewPtr)->get_source_ndx(S(rowIndex))) ? JNI_TRUE : JNI_FALSE;
    });
}

// Setters

JNIEXPORT void JNICALL Java_io_realm_internal_TableView_nativeSetLong(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex, jlong value)
{
    try_catch<void>(env, [&]() {
        VIEW_VALID_AND_IN_SYNC(env, nativeViewPtr);
        INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex, type_Int);
        TV(nativeViewPtr)->set_int(S(columnIndex), S(rowIndex), value);
    });
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableView_nativeSetBoolean(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex, jboolean value)
{
    try_catch<void>(env, [&]() {
        VIEW_VALID_AND_IN_SYNC(env, nativeViewPtr);
        INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex, type_Bool);
        TV(nativeViewPtr)->set_bool(S(columnIndex), S(rowIndex), value != 0 ? true : false);
    });
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableView_nativeSetFloat(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex, jfloat value)
{
    try_catch<void>(env, [&]() {
        VIEW_VALID_AND_IN_SYNC(env, nativeViewPtr);
        INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex, type_Float);
        TV(nativeViewPtr)->set_float(S(columnIndex), S(rowIndex), value);
    });
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableView_nativeSetDouble(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex, jdouble value)
{
    try_catch<void>(env, [&]() {
        VIEW_VALID_AND_IN_SYNC(env, nativeViewPtr);
        INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex, type_Double);
        TV(nativeViewPtr)->set_double(S(columnIndex), S(rowIndex), value);
    });
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableView_nativeSetTimestampValue(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex, jlong timestampValue)
{
    try_catch<void>(env, [&]() {
        VIEW_VALID_AND_IN_SYNC(env, nativeViewPtr);
        INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex, type_Timestamp);
        TV(nativeViewPtr)->set_timestamp(S(columnIndex), S(rowIndex), from_milliseconds(timestampValue));
    });
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableView_nativeSetString(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex, jstring value)
{
    try_catch<void>(env, [&]() {
        VIEW_VALID_AND_IN_SYNC(env, nativeViewPtr);
        INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex, type_String);
        if (!TV(nativeViewPtr)->get_parent().is_nullable(S(columnIndex))) {
            throw null_value(&(TV(nativeViewPtr)->get_parent()), S(columnIndex));
        }
        JStringAccessor value2(env, value);
        TV(nativeViewPtr)->set_string(S(columnIndex), S(rowIndex), value2);
    });
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableView_nativeSetByteArray(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex, jbyteArray byteArray)
{
    try_catch<void>(env, [&]() {
        VIEW_VALID_AND_IN_SYNC(env, nativeViewPtr);
        INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex, type_Binary);

        JniByteArray bytesAccessor(env, byteArray);
        TV(nativeViewPtr)->set_binary(S(columnIndex), S(rowIndex), bytesAccessor);
    });
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableView_nativeSetLink
  (JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex, jlong targetIndex)
{
    try_catch<void>(env, [&]() {
        VIEW_VALID_AND_IN_SYNC(env, nativeViewPtr);
        INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex, type_Link);
        TV(nativeViewPtr)->set_link(S(columnIndex), S(rowIndex), S(targetIndex));
    });
}

JNIEXPORT jboolean JNICALL Java_io_realm_internal_TableView_nativeIsNullLink
  (JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex)
{
    return try_catch<jboolean>(env, [&]() {
        VIEW_VALID_AND_IN_SYNC(env, nativeViewPtr);
        INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex, type_Link);
        return TV(nativeViewPtr)->is_null_link(S(columnIndex), S(rowIndex));
    });
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableView_nativeNullifyLink
  (JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex)
{
    return try_catch<void>(env, [&]() {
        VIEW_VALID_AND_IN_SYNC(env, nativeViewPtr);
        INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex, type_Link);
        TV(nativeViewPtr)->nullify_link(S(columnIndex), S(rowIndex));
    });
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableView_nativeClear(
    JNIEnv* env, jobject, jlong nativeViewPtr)
{
    try_catch<void>(env, [&]() {
        VIEW_VALID_AND_IN_SYNC(env, nativeViewPtr);
        TV(nativeViewPtr)->clear(RemoveMode::unordered);
    });
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableView_nativeRemoveRow(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong rowIndex)
{
    try_catch<void>(env, [&]() {
        VIEW_VALID_AND_IN_SYNC(env, nativeViewPtr);
        ROW_INDEX_VALID(env, TV(nativeViewPtr), rowIndex);
        TV(nativeViewPtr)->remove(S(rowIndex), RemoveMode::unordered);
    });
}

// FindFirst*

JNIEXPORT jlong JNICALL Java_io_realm_internal_TableView_nativeFindFirstInt(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong value)
{
    return try_catch<jlong>(env, [&]() {
        VIEW_VALID_AND_IN_SYNC(env, nativeViewPtr);
        COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_Int);
        return to_jlong_or_not_found(TV(nativeViewPtr)->find_first_int(S(columnIndex), value));
    });
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_TableView_nativeFindFirstBool(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jboolean value)
{
    return try_catch<jlong>(env, [&]() {
        VIEW_VALID_AND_IN_SYNC(env, nativeViewPtr);
        COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_Bool);
        size_t res = TV(nativeViewPtr)->find_first_bool(S(columnIndex), value != 0 ? true : false);
        return to_jlong_or_not_found(res);
    });
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_TableView_nativeFindFirstFloat(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jfloat value)
{
    return try_catch<jlong>(env, [&]() {
        VIEW_VALID_AND_IN_SYNC(env, nativeViewPtr);
        COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_Float);
        return to_jlong_or_not_found(TV(nativeViewPtr)->find_first_float(S(columnIndex), value));
    });
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_TableView_nativeFindFirstDouble(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jdouble value)
{
    return try_catch<jlong>(env, [&]() {
        VIEW_VALID_AND_IN_SYNC(env, nativeViewPtr);
        COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_Double);
        return to_jlong_or_not_found((TV(nativeViewPtr)->find_first_double(S(columnIndex), value)));
    });
}

// FIXME: find_first_timestamp() isn't implemented
/*
JNIEXPORT jlong JNICALL Java_io_realm_internal_TableView_nativeFindFirstDate(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong dateTimeValue)
{
    try_catch<>(env, [&]() {
        if (!VIEW_VALID_AND_IN_SYNC(env, nativeViewPtr) ||
            !COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_DateTime))
            return 0;
        return to_jlong_or_not_found( TV(nativeViewPtr)->find_first_datetime( S(columnIndex), DateTime(dateTimeValue)) );
    });
    return 0;
}
*/

JNIEXPORT jlong JNICALL Java_io_realm_internal_TableView_nativeFindFirstString(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jstring value)
{
    return try_catch<jlong>(env, [&]() {
        VIEW_VALID_AND_IN_SYNC(env, nativeViewPtr);
        COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_String);
        JStringAccessor value2(env, value);
        size_t searchIndex = TV(nativeViewPtr)->find_first_string(S(columnIndex), value2);
        return to_jlong_or_not_found(searchIndex);
    });
}

// FindAll*

JNIEXPORT jlong JNICALL Java_io_realm_internal_TableView_nativeFindAllInt(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong value)
{
    return try_catch<jlong>(env, [&]() {
        VIEW_VALID_AND_IN_SYNC(env, nativeViewPtr);
        COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_Int);
        TableView* pResultView = new TableView(TV(nativeViewPtr)->find_all_int(S(columnIndex), value));
        return reinterpret_cast<jlong>(pResultView);
    });
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_TableView_nativeFindAllBool(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jboolean value)
{
    return try_catch<jlong>(env, [&]() {
        VIEW_VALID_AND_IN_SYNC(env, nativeViewPtr);
        COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_Bool);
        TableView* pResultView = new TableView(TV(nativeViewPtr)->find_all_bool(S(columnIndex),
                                               value != 0 ? true : false));
        return reinterpret_cast<jlong>(pResultView);
    });
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_TableView_nativeFindAllFloat(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jfloat value)
{
    return try_catch<jlong>(env, [&]() {
        VIEW_VALID_AND_IN_SYNC(env, nativeViewPtr);
        COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_Float);
        TableView* pResultView = new TableView(TV(nativeViewPtr)->find_all_float(S(columnIndex), value));
        return reinterpret_cast<jlong>(pResultView);
    });
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_TableView_nativeFindAllDouble(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jdouble value)
{
    return try_catch<jlong>(env, [&]() {
        VIEW_VALID_AND_IN_SYNC(env, nativeViewPtr);
        COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_Double);
        TableView* pResultView = new TableView(TV(nativeViewPtr)->find_all_double(S(columnIndex), value));
        return reinterpret_cast<jlong>(pResultView);
    });
}

// FIXME: find_all_timestamp() isn't implemented
/*
JNIEXPORT jlong JNICALL Java_io_realm_internal_TableView_nativeFindAllDate(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong dateTimeValue)
{
    try_catch<>(env, [&]() {
        if (!VIEW_VALID_AND_IN_SYNC(env, nativeViewPtr) ||
            !COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_DateTime))
            return 0;
        TableView* pResultView = new TableView( TV(nativeViewPtr)->find_all_datetime( S(columnIndex),
                                                DateTime(dateTimeValue)) );
        return reinterpret_cast<jlong>(pResultView);
    });
    return 0;
}
*/

JNIEXPORT jlong JNICALL Java_io_realm_internal_TableView_nativeFindAllString(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jstring value)
{
    return try_catch<jlong>(env, [&]() {
        VIEW_VALID_AND_IN_SYNC(env, nativeViewPtr);
        COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_String);
        JStringAccessor value2(env, value);
        TableView* pResultView = new TableView(TV(nativeViewPtr)->find_all_string(S(columnIndex), value2));
        return reinterpret_cast<jlong>(pResultView);
    });
}

// Integer aggregates

JNIEXPORT jlong JNICALL Java_io_realm_internal_TableView_nativeSumInt(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex)
{
    try_catch<jlong>(env, [&]() {
        VIEW_VALID_AND_IN_SYNC(env, nativeViewPtr);
        COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_Int);
        return TV(nativeViewPtr)->sum_int(S(columnIndex));
    });
}

JNIEXPORT jdouble JNICALL Java_io_realm_internal_TableView_nativeAverageInt(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex)
{
    return try_catch<jdouble>(env, [&]() {
        VIEW_VALID_AND_IN_SYNC(env, nativeViewPtr);
        COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_Int);
        return static_cast<jdouble>(TV(nativeViewPtr)->average_int(S(columnIndex)));
    });
}

JNIEXPORT jobject JNICALL Java_io_realm_internal_TableView_nativeMaximumInt(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex)
{
    return try_catch<jobject>(env, [&]() {
        VIEW_VALID_AND_IN_SYNC(env, nativeViewPtr);
        COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_Int);
        size_t return_ndx;
        int64_t result = TV(nativeViewPtr)->maximum_int(S(columnIndex), &return_ndx);
        if (return_ndx != npos) {
            return NewLong(env, result);
        }
        return static_cast<jobject>(nullptr);
    });
}

JNIEXPORT jobject JNICALL Java_io_realm_internal_TableView_nativeMinimumInt(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex)
{
    return try_catch<jobject>(env, [&]() {
        VIEW_VALID_AND_IN_SYNC(env, nativeViewPtr);
        COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_Int);
        size_t return_ndx;
        int64_t result = TV(nativeViewPtr)->minimum_int(S(columnIndex), &return_ndx);
        if (return_ndx != npos) {
            return NewLong(env, result);
        }
        return static_cast<jobject>(nullptr);
    });
}

// float aggregates

JNIEXPORT jdouble JNICALL Java_io_realm_internal_TableView_nativeSumFloat(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex)
{
    return try_catch<jdouble>(env, [&]() {
        VIEW_VALID_AND_IN_SYNC(env, nativeViewPtr);
        COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_Float);
        return TV(nativeViewPtr)->sum_float(S(columnIndex));
    });
}

JNIEXPORT jdouble JNICALL Java_io_realm_internal_TableView_nativeAverageFloat(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex)
{
    return try_catch<jdouble>(env, [&]() {
        VIEW_VALID_AND_IN_SYNC(env, nativeViewPtr);
        COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_Float);
        return TV(nativeViewPtr)->average_float(S(columnIndex));
    });
}

JNIEXPORT jobject JNICALL Java_io_realm_internal_TableView_nativeMaximumFloat(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex)
{
    return try_catch<jobject>(env, [&]() {
        VIEW_VALID_AND_IN_SYNC(env, nativeViewPtr);
        COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_Float);
        size_t return_ndx;
        float result = TV(nativeViewPtr)->maximum_float(S(columnIndex), &return_ndx);
        if (return_ndx != npos) {
            return NewFloat(env, result);
        }
        return static_cast<jobject>(nullptr);
    });
}

JNIEXPORT jobject JNICALL Java_io_realm_internal_TableView_nativeMinimumFloat(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex)
{
    return try_catch<jobject>(env, [&]() {
        VIEW_VALID_AND_IN_SYNC(env, nativeViewPtr);
        COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_Float);
        size_t return_ndx;
        float result = TV(nativeViewPtr)->minimum_float(S(columnIndex), &return_ndx);
        if (return_ndx != npos) {
            return NewFloat(env, result);
        }
        return static_cast<jobject>(nullptr);
    });
}

// double aggregates

JNIEXPORT jdouble JNICALL Java_io_realm_internal_TableView_nativeSumDouble(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex)
{
    return try_catch<jdouble>(env, [&]() {
        VIEW_VALID_AND_IN_SYNC(env, nativeViewPtr);
        COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_Double);
        return TV(nativeViewPtr)->sum_double(S(columnIndex));
    });
}

JNIEXPORT jdouble JNICALL Java_io_realm_internal_TableView_nativeAverageDouble(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex)
{
    return try_catch<jdouble>(env, [&]() {
        VIEW_VALID_AND_IN_SYNC(env, nativeViewPtr);
        COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_Double);
        return static_cast<jdouble>(TV(nativeViewPtr)->average_double(S(columnIndex)));
    });
}

JNIEXPORT jobject JNICALL Java_io_realm_internal_TableView_nativeMaximumDouble(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex)
{
    return try_catch<jobject>(env, [&]() {
        VIEW_VALID_AND_IN_SYNC(env, nativeViewPtr);
        COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_Double);
        size_t return_ndx;
        double result = TV(nativeViewPtr)->maximum_double(S(columnIndex), &return_ndx);
        if (return_ndx != npos) {
            return NewDouble(env, result);
        }
        return static_cast<jobject>(nullptr);
    });
}

JNIEXPORT jobject JNICALL Java_io_realm_internal_TableView_nativeMinimumDouble(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex)
{
    return try_catch<jobject>(env, [&]() {
        VIEW_VALID_AND_IN_SYNC(env, nativeViewPtr);
        COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_Double);
        size_t return_ndx;
        double result = TV(nativeViewPtr)->minimum_double(S(columnIndex), &return_ndx);
        if (return_ndx != npos) {
            return NewDouble(env, result);
        }
        return static_cast<jobject>(nullptr);
    });
}


// date aggregates

JNIEXPORT jobject JNICALL Java_io_realm_internal_TableView_nativeMaximumTimestamp(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex)
{
    return try_catch<jobject>(env, [&]() {
        VIEW_VALID_AND_IN_SYNC(env, nativeViewPtr);
        COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_Timestamp);

        size_t return_ndx;
        Timestamp result = TV(nativeViewPtr)->maximum_timestamp(S(columnIndex), &return_ndx);
        if (return_ndx != npos) {
            return NewLong(env, to_milliseconds(result));
        }
        return static_cast<jobject>(nullptr);
    });
}

JNIEXPORT jobject JNICALL Java_io_realm_internal_TableView_nativeMinimumTimestamp(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex)
{
    return try_catch<jobject>(env, [&]() {
        VIEW_VALID_AND_IN_SYNC(env, nativeViewPtr);
        COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_Timestamp);

        size_t return_ndx;
        Timestamp result = TV(nativeViewPtr)->minimum_timestamp(S(columnIndex), &return_ndx);
        if (return_ndx != npos) {
            return NewLong(env, to_milliseconds(result));
        }
        return static_cast<jobject>(nullptr);
    });
}

// sort
JNIEXPORT void JNICALL Java_io_realm_internal_TableView_nativeSort(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jboolean ascending)
{
    try_catch<void>(env, [&]() {
        VIEW_VALID_AND_IN_SYNC(env, nativeViewPtr);
        COL_INDEX_VALID(env, TV(nativeViewPtr), columnIndex);
        int colType = TV(nativeViewPtr)->get_column_type(S(columnIndex));

        switch (colType) {
            case type_Bool:
            case type_Int:
            case type_Float:
            case type_Double:
            case type_String:
            case type_Timestamp:
                TV(nativeViewPtr)->sort(S(columnIndex), ascending != 0 ? true : false);
                break;
            default:
                throw invalid_argument("Sort is not supported on binary data, object references and RealmList.");
        }
    });
}

JNIEXPORT void JNICALL Java_io_realm_internal_TableView_nativeSortMulti(
  JNIEnv* env, jobject, jlong nativeViewPtr, jlongArray columnIndices, jbooleanArray ascending)
{
    try_catch<void>(env, [&]() {
        VIEW_VALID_AND_IN_SYNC(env, nativeViewPtr);

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
            throw invalid_argument("Number of fields and sort orders do not match.");
        }

        TableView* tv = TV(nativeViewPtr);
        std::vector<std::vector<size_t>> indices;
        std::vector<bool> ascendings;

        for (int i = 0; i < arr_len; ++i) {
            COL_INDEX_VALID(env, tv, long_arr[i]);
            int colType = tv->get_column_type(S(long_arr[i]));
            switch (colType) {
                case type_Bool:
                case type_Int:
                case type_Float:
                case type_Double:
                case type_String:
                case type_Timestamp:
                    indices.push_back(std::vector<size_t> { S(long_arr[i]) });
                    ascendings.push_back(B(bool_arr[i]));
                    break;
                default:
                    throw invalid_argument("Sort is not supported on binary data, object references and RealmList.");
            }
        }
        tv->sort(SortDescriptor(tv->get_parent(), indices, ascendings));
    });
}

JNIEXPORT jstring JNICALL Java_io_realm_internal_TableView_nativeToJson(
    JNIEnv *env, jobject, jlong nativeViewPtr)
{
    return try_catch<jstring>(env, [&]() {
        VIEW_VALID_AND_IN_SYNC(env, nativeViewPtr);

        // Write table to string in JSON format
        std::stringstream ss;
        ss.sync_with_stdio(false); // for performance
        TV(nativeViewPtr)->to_json(ss);
        const std::string str = ss.str();
        return to_jstring(env, str);
    });
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_TableView_nativeWhere(
    JNIEnv *env, jobject, jlong nativeViewPtr)
{
    TR_ENTER_PTR(env, nativeViewPtr)
    return try_catch<jlong>(env, [&]() {
        VIEW_VALID_AND_IN_SYNC(env, nativeViewPtr);
        Query *queryPtr = new Query(TV(nativeViewPtr)->get_parent().where(TV(nativeViewPtr)));
        return reinterpret_cast<jlong>(queryPtr);
    });
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_TableView_nativeSyncIfNeeded(
    JNIEnv* env, jobject, jlong nativeViewPtr)
{
    return try_catch<jlong>(env, [&]() {
        bool valid = (TV(nativeViewPtr) != NULL);
        if (valid) {
            if (!TV(nativeViewPtr)->is_attached()) {
                throw illegal_state("The Realm has been closed and is no longer accessible.");
            }
        }

        return static_cast<jlong>(TV(nativeViewPtr)->sync_if_needed());
    });
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_TableView_nativeFindBySourceNdx
        (JNIEnv *env, jobject, jlong nativeViewPtr, jlong sourceIndex)
{
    TR_ENTER_PTR(env, nativeViewPtr);
    return try_catch<jlong>(env, [&]() {
        VIEW_VALID_AND_IN_SYNC(env, nativeViewPtr);
        ROW_INDEX_VALID(env, &(TV(nativeViewPtr)->get_parent()), sourceIndex);

        size_t ndx = TV(nativeViewPtr)->find_by_source_ndx(sourceIndex);
        return to_jlong_or_not_found(ndx);
    });
}
