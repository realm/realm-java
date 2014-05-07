#include "util.hpp"
#include "io_realm_TableView.h"
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


JNIEXPORT jlong JNICALL Java_io_realm_TableView_createNativeTableView(
    JNIEnv* env, jobject, jobject, jlong)
{
    try {
        return reinterpret_cast<jlong>( new TableView() );
    } CATCH_STD()
    return 0;
}

JNIEXPORT void JNICALL Java_io_realm_TableView_nativePivot(
    JNIEnv *env, jobject, jlong dataTablePtr, jlong stringCol, jlong intCol, jint operation, jlong resultTablePtr)
{
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

JNIEXPORT void JNICALL Java_io_realm_TableView_nativeClose(
    JNIEnv*, jclass, jlong nativeViewPtr)
{
    if (nativeViewPtr == 0)
        return;

    delete TV(nativeViewPtr);
}

JNIEXPORT jlong JNICALL Java_io_realm_TableView_nativeSize(
    JNIEnv* env, jobject, jlong nativeViewPtr)
{
    if (!VIEW_VALID(env, nativeViewPtr))
        return 0;
    return TV(nativeViewPtr)->size();   // noexcept
}

JNIEXPORT jlong JNICALL Java_io_realm_TableView_nativeGetSourceRowIndex
(JNIEnv *env, jobject, jlong nativeViewPtr, jlong rowIndex)
{
    if (!VIEW_VALID(env, nativeViewPtr))
        return 0;
    if (!ROW_INDEX_VALID(env, TV(nativeViewPtr), rowIndex))
        return 0;
    return TV(nativeViewPtr)->get_source_ndx(S(rowIndex));   // noexcept
}

JNIEXPORT jlong JNICALL Java_io_realm_TableView_nativeGetColumnCount
  (JNIEnv *env, jobject, jlong nativeViewPtr)
{
    if (!VIEW_VALID(env, nativeViewPtr))
        return 0;
    return TV(nativeViewPtr)->get_column_count();
}

JNIEXPORT jstring JNICALL Java_io_realm_TableView_nativeGetColumnName
  (JNIEnv *env, jobject, jlong nativeViewPtr, jlong columnIndex)
{
    if (!VIEW_VALID(env, nativeViewPtr) || !COL_INDEX_VALID(env, TV(nativeViewPtr), columnIndex))
        return NULL;
    try {
        return to_jstring(env, TV(nativeViewPtr)->get_column_name( S(columnIndex)));
    } CATCH_STD();
    return NULL;
}

JNIEXPORT jlong JNICALL Java_io_realm_TableView_nativeGetColumnIndex
   (JNIEnv *env, jobject, jlong nativeViewPtr, jstring columnName)
{
    if (!VIEW_VALID(env, nativeViewPtr))
        return 0;
    try {
        JStringAccessor columnName2(env, columnName); // throws
        return to_jlong_or_not_found( TV(nativeViewPtr)->get_column_index(columnName2) ); // noexcept
        } CATCH_STD();
    return 0;
}

JNIEXPORT jint JNICALL Java_io_realm_TableView_nativeGetColumnType
  (JNIEnv *env, jobject, jlong nativeViewPtr, jlong columnIndex)
{
    if (!VIEW_VALID(env, nativeViewPtr) || !COL_INDEX_VALID(env, TV(nativeViewPtr), columnIndex))
        return 0;
    return static_cast<int>( TV(nativeViewPtr)->get_column_type( S(columnIndex)) );
}

JNIEXPORT jlong JNICALL Java_io_realm_TableView_nativeGetLong(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex)
{
    if (!VIEW_VALID(env, nativeViewPtr) ||
        !INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex, type_Int))
        return 0;

    return TV(nativeViewPtr)->get_int( S(columnIndex), S(rowIndex));  // noexcept
}

JNIEXPORT jboolean JNICALL Java_io_realm_TableView_nativeGetBoolean(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex)
{
    if (!VIEW_VALID(env, nativeViewPtr) ||
        !INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex, type_Bool))
        return 0;

    return TV(nativeViewPtr)->get_bool( S(columnIndex), S(rowIndex));  // noexcept
}

JNIEXPORT jfloat JNICALL Java_io_realm_TableView_nativeGetFloat(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex)
{
    if (!VIEW_VALID(env, nativeViewPtr) ||
        !INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex, type_Float))
        return 0;

    return TV(nativeViewPtr)->get_float( S(columnIndex), S(rowIndex));  // noexcept
}

JNIEXPORT jdouble JNICALL Java_io_realm_TableView_nativeGetDouble(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex)
{
    if (!VIEW_VALID(env, nativeViewPtr) ||
        !INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex, type_Double))
        return 0;

    return TV(nativeViewPtr)->get_double( S(columnIndex), S(rowIndex));  // noexcept
}

JNIEXPORT jlong JNICALL Java_io_realm_TableView_nativeGetDateTimeValue(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex)
{
    if (!VIEW_VALID(env, nativeViewPtr) ||
        !INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex, type_DateTime))
        return 0;

    return TV(nativeViewPtr)->get_datetime( S(columnIndex), S(rowIndex)).get_datetime();  // noexcept
}

JNIEXPORT jstring JNICALL Java_io_realm_TableView_nativeGetString(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex)
{
    if (!VIEW_VALID(env, nativeViewPtr) ||
        !INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex, type_String))
        return NULL;
    try {
        return to_jstring(env, TV(nativeViewPtr)->get_string( S(columnIndex), S(rowIndex)) // noexcept
                          );
        } CATCH_STD();
    return NULL;
}

/*
JNIEXPORT jobject JNICALL Java_io_realm_TableView_nativeGetBinary(
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

JNIEXPORT jbyteArray JNICALL Java_io_realm_TableView_nativeGetByteArray(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex)
{
    if (!VIEW_VALID(env, nativeViewPtr) ||
        !INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex, type_Binary))
        return NULL;
    try {
        return tbl_GetByteArray<TableView>(env, nativeViewPtr, columnIndex, rowIndex);
    } CATCH_STD()
    return NULL;
}

JNIEXPORT jint JNICALL Java_io_realm_TableView_nativeGetMixedType(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex)
{
    if (!VIEW_VALID(env, nativeViewPtr) ||
        !INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex, type_Mixed))
        return 0;

    DataType mixedType = TV(nativeViewPtr)->get_mixed_type( S(columnIndex), S(rowIndex));  // noexcept
    return static_cast<jint>(mixedType);
}

JNIEXPORT jobject JNICALL Java_io_realm_TableView_nativeGetMixed(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex)
{
    if (!VIEW_VALID(env, nativeViewPtr) ||
        !INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex, type_Mixed))
        return NULL;

    Mixed value = TV(nativeViewPtr)->get_mixed( S(columnIndex), S(rowIndex));   // noexcept
    try {
        return CreateJMixedFromMixed(env, value);
    } CATCH_STD()
    return NULL;
}

JNIEXPORT jlong JNICALL Java_io_realm_TableView_nativeGetSubtableSize(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex)
{
    if (!VIEW_VALID(env, nativeViewPtr) ||
        !INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex, type_Table))
        return 0;

    return TV(nativeViewPtr)->get_subtable_size( S(columnIndex), S(rowIndex));  // noexcept
}

JNIEXPORT jlong JNICALL Java_io_realm_TableView_nativeGetSubtable(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex)
{
    if (!VIEW_VALID(env, nativeViewPtr) ||
        !INDEX_AND_TYPE_VALID_MIXED(env, TV(nativeViewPtr), columnIndex, rowIndex, type_Table))
        return 0;
    try { // needed?
        Table* pSubtable = LangBindHelper::get_subtable_ptr(TV(nativeViewPtr), S(columnIndex), S(rowIndex));
        return reinterpret_cast<jlong>(pSubtable);
    } CATCH_STD()
    return 0;
}

JNIEXPORT void JNICALL Java_io_realm_TableView_nativeClearSubtable(
   JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex)
{
    if (!VIEW_VALID(env, nativeViewPtr) ||
        !INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex, type_Table))
        return;
    TV(nativeViewPtr)->clear_subtable(S(columnIndex), S(rowIndex));  // noexcept
}

// Setters

JNIEXPORT void JNICALL Java_io_realm_TableView_nativeSetLong(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex, jlong value)
{
    if (!VIEW_VALID(env, nativeViewPtr) ||
        !INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex, type_Int))
        return;
    try {
        TV(nativeViewPtr)->set_int( S(columnIndex), S(rowIndex), value);
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_TableView_nativeSetBoolean(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex, jboolean value)
{
    if (!VIEW_VALID(env, nativeViewPtr) ||
        !INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex, type_Bool))
        return;
    try {
        TV(nativeViewPtr)->set_bool( S(columnIndex), S(rowIndex), value != 0 ? true : false);
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_TableView_nativeSetFloat(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex, jfloat value)
{
    if (!VIEW_VALID(env, nativeViewPtr) ||
        !INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex, type_Float))
        return;
    try {
        TV(nativeViewPtr)->set_float( S(columnIndex), S(rowIndex), value);
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_TableView_nativeSetDouble(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex, jdouble value)
{
    if (!VIEW_VALID(env, nativeViewPtr) ||
        !INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex, type_Double))
        return;
    try {
        TV(nativeViewPtr)->set_double( S(columnIndex), S(rowIndex), value);
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_TableView_nativeSetDateTimeValue(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex, jlong dateTimeValue)
{
    if (!VIEW_VALID(env, nativeViewPtr) ||
        !INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex, type_DateTime))
        return;
    try {
        TV(nativeViewPtr)->set_datetime( S(columnIndex), S(rowIndex), dateTimeValue);
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_TableView_nativeSetString(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex, jstring value)
{
    if (!VIEW_VALID(env, nativeViewPtr) ||
        !INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex, type_String))
        return;
    try {
        JStringAccessor value2(env, value);  // throws
        TV(nativeViewPtr)->set_string( S(columnIndex), S(rowIndex), value2);
    } CATCH_STD()
}

/*
JNIEXPORT void JNICALL Java_io_realm_TableView_nativeSetBinary(
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

JNIEXPORT void JNICALL Java_io_realm_TableView_nativeSetByteArray(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex, jbyteArray byteArray)
{
    if (!VIEW_VALID(env, nativeViewPtr) ||
        !INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex, type_Binary))
        return;
    try {
        tbl_nativeDoByteArray(&TableView::set_binary, TV(nativeViewPtr), env, columnIndex, rowIndex, byteArray);
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_TableView_nativeSetMixed(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex, jobject jMixedValue)
{
    if (!VIEW_VALID(env, nativeViewPtr) ||
        !INDEX_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex))
        return;
    try {
        tbl_nativeDoMixed(&TableView::set_mixed, TV(nativeViewPtr), env, columnIndex, rowIndex, jMixedValue);
    } CATCH_STD()
}


JNIEXPORT void JNICALL Java_io_realm_TableView_nativeAddInt(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong value)
{
    if (!VIEW_VALID(env, nativeViewPtr) ||
        !COL_INDEX_VALID(env, TV(nativeViewPtr), columnIndex))
        return;
    try {
        TV(nativeViewPtr)->add_int( S(columnIndex), value);
    } CATCH_STD()
}


JNIEXPORT void JNICALL Java_io_realm_TableView_nativeClear(
    JNIEnv* env, jobject, jlong nativeViewPtr)
{
    if (!VIEW_VALID(env, nativeViewPtr))
        return;
    try {
        TV(nativeViewPtr)->clear();
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_TableView_nativeRemoveRow(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong rowIndex)
{
    if (!VIEW_VALID(env, nativeViewPtr) ||
        !ROW_INDEX_VALID(env, TV(nativeViewPtr), rowIndex))
        return;
    try {
        TV(nativeViewPtr)->remove( S(rowIndex));
    } CATCH_STD()
}

// FindFirst*

JNIEXPORT jlong JNICALL Java_io_realm_TableView_nativeFindFirstInt(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong value)
{
    if (!VIEW_VALID(env, nativeViewPtr) ||
        !COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_Int))
        return 0;
    try {
        return to_jlong_or_not_found( TV(nativeViewPtr)->find_first_int( S(columnIndex), value) );
    } CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_TableView_nativeFindFirstBool(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jboolean value)
{
    if (!VIEW_VALID(env, nativeViewPtr) ||
        !COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_Bool))
        return 0;
    try {
        size_t res = TV(nativeViewPtr)->find_first_bool( S(columnIndex), value != 0 ? true : false);
        return to_jlong_or_not_found( res );
    } CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_TableView_nativeFindFirstFloat(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jfloat value)
{
    if (!VIEW_VALID(env, nativeViewPtr) ||
        !COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_Float))
        return 0;
    try {
        return to_jlong_or_not_found( TV(nativeViewPtr)->find_first_float( S(columnIndex), value) );
    } CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_TableView_nativeFindFirstDouble(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jdouble value)
{
    if (!VIEW_VALID(env, nativeViewPtr) ||
        !COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_Double))
        return 0;
    try {
        return to_jlong_or_not_found( (TV(nativeViewPtr)->find_first_double( S(columnIndex), value)) );
    } CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_TableView_nativeFindFirstDate(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong dateTimeValue)
{
    if (!VIEW_VALID(env, nativeViewPtr) ||
        !COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_DateTime))
        return 0;
    try {
        return to_jlong_or_not_found( TV(nativeViewPtr)->find_first_datetime( S(columnIndex), (time_t)dateTimeValue) );
    } CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_TableView_nativeFindFirstString(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jstring value)
{
    if (!VIEW_VALID(env, nativeViewPtr) ||
        !COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_String))
        return 0;

    try {
        JStringAccessor value2(env, value); // throws
        size_t searchIndex = TV(nativeViewPtr)->find_first_string( S(columnIndex), value2);
        return to_jlong_or_not_found( searchIndex );
    } CATCH_STD()
    return 0;
}

// FindAll*

JNIEXPORT jlong JNICALL Java_io_realm_TableView_nativeFindAllInt(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong value)
{
    if (!VIEW_VALID(env, nativeViewPtr) ||
        !COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_Int))
        return 0;
    try {
        TableView* pResultView = new TableView( TV(nativeViewPtr)->find_all_int( S(columnIndex), value) );
        return reinterpret_cast<jlong>(pResultView);
    } CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_TableView_nativeFindAllBool(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jboolean value)
{
    if (!VIEW_VALID(env, nativeViewPtr) ||
        !COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_Bool))
        return 0;
    try {
        TableView* pResultView = new TableView( TV(nativeViewPtr)->find_all_bool( S(columnIndex),
                                                value != 0 ? true : false) );
        return reinterpret_cast<jlong>(pResultView);
    } CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_TableView_nativeFindAllFloat(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jfloat value)
{
    if (!VIEW_VALID(env, nativeViewPtr) ||
        !COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_Float))
        return 0;
    try {
        TableView* pResultView = new TableView( TV(nativeViewPtr)->find_all_float( S(columnIndex), value) );
        return reinterpret_cast<jlong>(pResultView);
    } CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_TableView_nativeFindAllDouble(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jdouble value)
{
    if (!VIEW_VALID(env, nativeViewPtr) ||
        !COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_Double))
        return 0;
    try {
        TableView* pResultView = new TableView( TV(nativeViewPtr)->find_all_double( S(columnIndex), value) );
        return reinterpret_cast<jlong>(pResultView);
    } CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_TableView_nativeFindAllDate(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong dateTimeValue)
{
    if (!VIEW_VALID(env, nativeViewPtr) ||
        !COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_DateTime))
        return 0;
    try {
        TableView* pResultView = new TableView( TV(nativeViewPtr)->find_all_datetime( S(columnIndex),
                                                static_cast<time_t>(dateTimeValue)) );
        return reinterpret_cast<jlong>(pResultView);
    } CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_TableView_nativeFindAllString(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jstring value)
{
    if (!VIEW_VALID(env, nativeViewPtr) ||
        !COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_String))
        return 0;

    try {
        JStringAccessor value2(env, value); // throws
        TR((env, "nativeFindAllString(col %d, string '%s') ", columnIndex, StringData(value2).data()));
        TableView* pResultView = new TableView( TV(nativeViewPtr)->find_all_string( S(columnIndex), value2) );
        TR((env, "-- resultview size=%lld.\n", pResultView->size()));
        return reinterpret_cast<jlong>(pResultView);
    } CATCH_STD()
    return 0;
}

// Integer aggregates

JNIEXPORT jlong JNICALL Java_io_realm_TableView_nativeSumInt(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex)
{
    if (!VIEW_VALID(env, nativeViewPtr) ||
        !COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_Int))
        return 0;
    try {
        return TV(nativeViewPtr)->sum_int( S(columnIndex));
    } CATCH_STD()
    return 0;
}

JNIEXPORT jdouble JNICALL Java_io_realm_TableView_nativeAverageInt(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex)
{
    if (!VIEW_VALID(env, nativeViewPtr) ||
        !COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_Int))
        return 0;

    // FIXME: Add support for native Average
    try {
        return static_cast<jdouble>( TV(nativeViewPtr)->sum_int( S(columnIndex)) ) / TV(nativeViewPtr)->size();
    } CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_TableView_nativeMaximumInt(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex)
{
    if (!VIEW_VALID(env, nativeViewPtr) ||
        !COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_Int))
        return 0;
    try {
        return TV(nativeViewPtr)->maximum_int( S(columnIndex));
    } CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_TableView_nativeMinimumInt(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex)
{
    if (!VIEW_VALID(env, nativeViewPtr) ||
        !COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_Int))
        return 0;
    try {
        return TV(nativeViewPtr)->minimum_int( S(columnIndex));
    } CATCH_STD()
    return 0;
}

// float aggregates

JNIEXPORT jdouble JNICALL Java_io_realm_TableView_nativeSumFloat(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex)
{
    if (!VIEW_VALID(env, nativeViewPtr) ||
        !COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_Float))
        return 0;
    try {
        return TV(nativeViewPtr)->sum_float( S(columnIndex));
    } CATCH_STD()
    return 0;
}

JNIEXPORT jdouble JNICALL Java_io_realm_TableView_nativeAverageFloat(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex)
{
    if (!VIEW_VALID(env, nativeViewPtr) ||
        !COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_Float))
        return 0;

    // FIXME: Add support for native Average
    try {
        return TV(nativeViewPtr)->sum_float( S(columnIndex)) / TV(nativeViewPtr)->size();
    } CATCH_STD()
    return 0;
}

JNIEXPORT jfloat JNICALL Java_io_realm_TableView_nativeMaximumFloat(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex)
{
    if (!VIEW_VALID(env, nativeViewPtr) ||
        !COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_Float))
        return 0;
    try {
        return TV(nativeViewPtr)->maximum_float( S(columnIndex));
    } CATCH_STD()
    return 0;
}

JNIEXPORT jfloat JNICALL Java_io_realm_TableView_nativeMinimumFloat(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex)
{
    if (!VIEW_VALID(env, nativeViewPtr) ||
        !COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_Float))
        return 0;
    try {
        return TV(nativeViewPtr)->minimum_float( S(columnIndex));
    } CATCH_STD()
    return 0;
}

// double aggregates

JNIEXPORT jdouble JNICALL Java_io_realm_TableView_nativeSumDouble(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex)
{
    if (!VIEW_VALID(env, nativeViewPtr) ||
        !COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_Double))
        return 0;
    try {
        return TV(nativeViewPtr)->sum_double( S(columnIndex));
    } CATCH_STD()
    return 0;
}

JNIEXPORT jdouble JNICALL Java_io_realm_TableView_nativeAverageDouble(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex)
{
    if (!VIEW_VALID(env, nativeViewPtr) ||
        !COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_Double))
        return 0;

    // FIXME: Add support for native Average
    try {
        return static_cast<jdouble>( TV(nativeViewPtr)->sum_double( S(columnIndex)) ) / TV(nativeViewPtr)->size();
    } CATCH_STD()
    return 0;
}

JNIEXPORT jdouble JNICALL Java_io_realm_TableView_nativeMaximumDouble(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex)
{
    if (!VIEW_VALID(env, nativeViewPtr) ||
        !COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_Double))
        return 0;
    try {
        return TV(nativeViewPtr)->maximum_double( S(columnIndex));
    } CATCH_STD()
    return 0;
}

JNIEXPORT jdouble JNICALL Java_io_realm_TableView_nativeMinimumDouble(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex)
{
    if (!VIEW_VALID(env, nativeViewPtr) ||
        !COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_Double))
        return 0;
    try {
        return TV(nativeViewPtr)->minimum_double( S(columnIndex));
    } CATCH_STD()
    return 0;
}


// date aggregates

JNIEXPORT jlong JNICALL Java_io_realm_TableView_nativeMaximumDate(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex)
{
    if (!VIEW_VALID(env, nativeViewPtr) ||
        !COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_DateTime))
        return 0;
    try {
        // This exploits the fact that dates are stored as int in core
        return TV(nativeViewPtr)->maximum_int( S(columnIndex));
    } CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_TableView_nativeMinimumDate(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex)
{
    if (!VIEW_VALID(env, nativeViewPtr) ||
        !COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_DateTime))
        return 0;
    try {
        // This exploits the fact that dates are stored as int in core
        return TV(nativeViewPtr)->minimum_int( S(columnIndex));
    } CATCH_STD()
    return 0;
}

// sort

JNIEXPORT void JNICALL Java_io_realm_TableView_nativeSort(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jboolean ascending)
{
    if (!VIEW_VALID(env, nativeViewPtr) ||
        !COL_INDEX_VALID(env, TV(nativeViewPtr), columnIndex))
        return;
    int colType = TV(nativeViewPtr)->get_column_type( S(columnIndex) );
    if (colType != type_Int && colType != type_Bool && colType != type_DateTime) {
        ThrowException(env, IllegalArgument, "Sort is currently only supported on Integer, Boolean and Date columns.");
        return;
    }
    try {
        TV(nativeViewPtr)->sort( S(columnIndex), ascending != 0 ? true : false);
    } CATCH_STD()
}

JNIEXPORT jstring JNICALL Java_io_realm_TableView_nativeToJson(
    JNIEnv *env, jobject, jlong nativeViewPtr)
{
    TableView* tv = TV(nativeViewPtr);
    if (!VIEW_VALID(env, nativeViewPtr))
        return NULL;

    // Write table to string in JSON format
    try {
        std::stringstream ss;
        ss.sync_with_stdio(false); // for performance
        tv->to_json(ss);
        const std::string str = ss.str();
        return env->NewStringUTF(str.c_str());
    } CATCH_STD()
    return NULL;
}

JNIEXPORT jstring JNICALL Java_io_realm_TableView_nativeToString(
    JNIEnv *env, jobject, jlong nativeViewPtr, jlong maxRows)
{
    TableView* tv = TV(nativeViewPtr);
    if (!VIEW_VALID(env, nativeViewPtr))
        return NULL;
    try {
       std::ostringstream ss;
       ss.sync_with_stdio(false); // for performance
       tv->to_string(ss, S(maxRows));
       const std::string str = ss.str();
       return env->NewStringUTF(str.c_str());
    } CATCH_STD()
    return NULL;
}

JNIEXPORT jstring JNICALL Java_io_realm_TableView_nativeRowToString(
    JNIEnv *env, jobject, jlong nativeViewPtr, jlong rowIndex)
{
    TableView* tv = TV(nativeViewPtr);
    if (!VIEW_VALID(env, nativeViewPtr) || !ROW_INDEX_VALID(env, tv, rowIndex))
        return NULL;
    try {
        std::ostringstream ss;
        tv->row_to_string(S(rowIndex), ss);
        const std::string str = ss.str();
        return env->NewStringUTF(str.c_str());
    } CATCH_STD()
    return NULL;
}

JNIEXPORT jlong JNICALL Java_io_realm_TableView_nativeWhere
  (JNIEnv *env, jobject, jlong nativeViewPtr)
{
    if (!VIEW_VALID(env, nativeViewPtr))
        return 0;
    try {
        TableView* tv = TV(nativeViewPtr);
        Query query = tv->get_parent().where().tableview(*tv);
        TableQuery* queryPtr = new TableQuery(query);
        return reinterpret_cast<jlong>(queryPtr);
    } CATCH_STD()
    return 0;
}

