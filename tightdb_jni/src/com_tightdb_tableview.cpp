#include "util.hpp"
#include "com_tightdb_TableView.h"
#include "mixedutil.hpp"
#include "tablebase_tpl.hpp"
#include <ostream>

using namespace tightdb;

#define VIEW_VALID(env, ptr) view_valid(env, ptr)

inline bool view_valid(JNIEnv* env, jlong nativeViewPtr) {
    bool valid = (nativeViewPtr != 0);
    if (valid) {
        valid = TV(nativeViewPtr)->get_parent().is_attached();
        if (!valid) {
            ThrowException(env, TableInvalid, "Table is closed, and no longer valid to operate on.");
        }
    }
    return valid;
}


JNIEXPORT jlong JNICALL Java_com_tightdb_TableView_createNativeTableView(
    JNIEnv*, jobject, jobject, jlong)
{
    return reinterpret_cast<jlong>( new TableView() );
}

JNIEXPORT void JNICALL Java_com_tightdb_TableView_nativeClose(
    JNIEnv* env, jobject, jlong nativeViewPtr)
{
    if (nativeViewPtr == 0)
        return;

    delete TV(nativeViewPtr);
}

JNIEXPORT jlong JNICALL Java_com_tightdb_TableView_nativeSize(
    JNIEnv* env, jobject, jlong nativeViewPtr)
{
 // TODO: remove:    std::cerr << "SIZE:  valid:" << VIEW_VALID(env, nativeViewPtr) << std::endl;
    if (!VIEW_VALID(env, nativeViewPtr)) {
  // TODO: REMOVE
  //    std::cerr << "INVALID view" << std::endl;
        return 0;
    }

    return TV(nativeViewPtr)->size();
}


JNIEXPORT jlong JNICALL Java_com_tightdb_TableView_nativeGetLong(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex)
{
    if (!VIEW_VALID(env, nativeViewPtr) || 
        !INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex, type_Int))
        return 0;

    return TV(nativeViewPtr)->get_int( S(columnIndex), S(rowIndex));
}

JNIEXPORT jboolean JNICALL Java_com_tightdb_TableView_nativeGetBoolean(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex)
{
    if (!VIEW_VALID(env, nativeViewPtr) || 
        !INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex, type_Bool))
        return 0;

    return TV(nativeViewPtr)->get_bool( S(columnIndex), S(rowIndex));
}

JNIEXPORT jfloat JNICALL Java_com_tightdb_TableView_nativeGetFloat(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex)
{
    if (!VIEW_VALID(env, nativeViewPtr) || 
        !INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex, type_Float))
        return 0;

    return TV(nativeViewPtr)->get_float( S(columnIndex), S(rowIndex));
}

JNIEXPORT jdouble JNICALL Java_com_tightdb_TableView_nativeGetDouble(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex)
{
    if (!VIEW_VALID(env, nativeViewPtr) || 
        !INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex, type_Double))
        return 0;

    return TV(nativeViewPtr)->get_double( S(columnIndex), S(rowIndex));
}

JNIEXPORT jlong JNICALL Java_com_tightdb_TableView_nativeGetDateTimeValue(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex)
{
    if (!VIEW_VALID(env, nativeViewPtr) || 
        !INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex, type_Date))
        return 0;

    return TV(nativeViewPtr)->get_date( S(columnIndex), S(rowIndex)).get_date();
}

JNIEXPORT jstring JNICALL Java_com_tightdb_TableView_nativeGetString(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex)
{
    if (!VIEW_VALID(env, nativeViewPtr) || 
        !INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex, type_String))
        return NULL;

    return to_jstring(env, TV(nativeViewPtr)->get_string( S(columnIndex), S(rowIndex)));
}

JNIEXPORT jobject JNICALL Java_com_tightdb_TableView_nativeGetBinary(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex)
{
    if (!VIEW_VALID(env, nativeViewPtr) || 
        !INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex, type_Binary))
        return NULL;
    // TODO: Does the native binary get freed?
    BinaryData bin = TV(nativeViewPtr)->get_binary( S(columnIndex), S(rowIndex));
    return env->NewDirectByteBuffer(const_cast<char*>(bin.data()),  static_cast<jlong>(bin.size()));
}

JNIEXPORT jbyteArray JNICALL Java_com_tightdb_TableView_nativeGetByteArray(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex)
{
    if (!VIEW_VALID(env, nativeViewPtr) || 
        !INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex, type_Binary))
        return NULL;

    return tbl_GetByteArray<TableView>(env, nativeViewPtr, columnIndex, rowIndex);
}

JNIEXPORT jint JNICALL Java_com_tightdb_TableView_nativeGetMixedType(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex)
{
    if (!VIEW_VALID(env, nativeViewPtr) || 
        !INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex, type_Mixed)) 
        return 0;

    DataType mixedType = TV(nativeViewPtr)->get_mixed_type( S(columnIndex), S(rowIndex));
    return static_cast<jint>(mixedType);
}

JNIEXPORT jobject JNICALL Java_com_tightdb_TableView_nativeGetMixed(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex)
{
    if (!VIEW_VALID(env, nativeViewPtr) || 
        !INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex, type_Mixed)) 
        return NULL;

    Mixed value = TV(nativeViewPtr)->get_mixed( S(columnIndex), S(rowIndex));
    return CreateJMixedFromMixed(env, value);
}

JNIEXPORT jlong JNICALL Java_com_tightdb_TableView_nativeGetSubTableSize(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex)
{
    if (!VIEW_VALID(env, nativeViewPtr) || 
        !INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex, type_Table)) 
        return 0;

    return TV(nativeViewPtr)->get_subtable_size( S(columnIndex), S(rowIndex));
}

JNIEXPORT jlong JNICALL Java_com_tightdb_TableView_nativeGetSubTable(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex)
{
    if (!VIEW_VALID(env, nativeViewPtr) || 
        !INDEX_AND_TYPE_VALID_MIXED(env, TV(nativeViewPtr), columnIndex, rowIndex, type_Table)) 
        return 0;

    Table* pSubTable = LangBindHelper::get_subtable_ptr(TV(nativeViewPtr), S(columnIndex), S(rowIndex));
    return reinterpret_cast<jlong>(pSubTable);
}

// Setters

JNIEXPORT void JNICALL Java_com_tightdb_TableView_nativeSetLong(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex, jlong value)
{
    if (!VIEW_VALID(env, nativeViewPtr) || 
        !INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex, type_Int)) 
        return;

    TV(nativeViewPtr)->set_int( S(columnIndex), S(rowIndex), value);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableView_nativeSetBoolean(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex, jboolean value)
{
    if (!VIEW_VALID(env, nativeViewPtr) || 
        !INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex, type_Bool))
        return;

    TV(nativeViewPtr)->set_bool( S(columnIndex), S(rowIndex), value != 0 ? true : false);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableView_nativeSetFloat(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex, jfloat value)
{
    if (!VIEW_VALID(env, nativeViewPtr) || 
        !INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex, type_Float))
        return;

    TV(nativeViewPtr)->set_float( S(columnIndex), S(rowIndex), value);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableView_nativeSetDouble(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex, jdouble value)
{
    if (!VIEW_VALID(env, nativeViewPtr) || 
        !INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex, type_Double)) 
        return;

    TV(nativeViewPtr)->set_double( S(columnIndex), S(rowIndex), value);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableView_nativeSetDateTimeValue(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex, jlong dateTimeValue)
{
    if (!VIEW_VALID(env, nativeViewPtr) || 
        !INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex, type_Date)) 
        return;

    TV(nativeViewPtr)->set_date( S(columnIndex), S(rowIndex), dateTimeValue);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableView_nativeSetString(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex, jstring value)
{
    if (!VIEW_VALID(env, nativeViewPtr) || 
        !INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex, type_String)) 
        return;

    JStringAccessor value2(env, value);
    if (!value2)
        return;
    TV(nativeViewPtr)->set_string( S(columnIndex), S(rowIndex), value2);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableView_nativeSetBinary(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex, jobject byteBuffer)
{
    if (!VIEW_VALID(env, nativeViewPtr) || 
        !INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex, type_Binary)) 
        return;

    tbl_nativeDoBinary(&TableView::set_binary, TV(nativeViewPtr), env, columnIndex, rowIndex, byteBuffer);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableView_nativeSetByteArray(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex, jbyteArray byteArray)
{
    if (!VIEW_VALID(env, nativeViewPtr) || 
        !INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex, type_Binary)) 
        return;

    tbl_nativeDoByteArray(&TableView::set_binary, TV(nativeViewPtr), env, columnIndex, rowIndex, byteArray);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableView_nativeSetMixed(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong rowIndex, jobject jMixedValue)
{
    if (!VIEW_VALID(env, nativeViewPtr) || 
        !INDEX_VALID(env, TV(nativeViewPtr), columnIndex, rowIndex)) 
        return;

    tbl_nativeDoMixed(&TableView::set_mixed, TV(nativeViewPtr), env, columnIndex, rowIndex, jMixedValue);
}


JNIEXPORT void JNICALL Java_com_tightdb_TableView_nativeAddInt(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong value)
{
    if (!VIEW_VALID(env, nativeViewPtr) || 
        !COL_INDEX_VALID(env, TV(nativeViewPtr), columnIndex))
        return;

    TV(nativeViewPtr)->add_int( S(columnIndex), value);
}


JNIEXPORT void JNICALL Java_com_tightdb_TableView_nativeClear(
    JNIEnv* env, jobject, jlong nativeViewPtr)
{
    if (!VIEW_VALID(env, nativeViewPtr))
        return;
    TV(nativeViewPtr)->clear();
}

JNIEXPORT void JNICALL Java_com_tightdb_TableView_nativeRemoveRow(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong rowIndex)
{
    if (!VIEW_VALID(env, nativeViewPtr) || 
        !ROW_INDEX_VALID(env, TV(nativeViewPtr), rowIndex)) 
        return;

    TV(nativeViewPtr)->remove( S(rowIndex));
}

// FindFirst*

JNIEXPORT jlong JNICALL Java_com_tightdb_TableView_nativeFindFirstInt(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong value)
{
    if (!VIEW_VALID(env, nativeViewPtr) || 
        !COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_Int)) 
        return 0;

    return static_cast<jlong>(TV(nativeViewPtr)->find_first_int( S(columnIndex), value));
}

JNIEXPORT jlong JNICALL Java_com_tightdb_TableView_nativeFindFirstBool(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jboolean value)
{
    if (!VIEW_VALID(env, nativeViewPtr) || 
        !COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_Bool))
        return false;

    return TV(nativeViewPtr)->find_first_bool( S(columnIndex), value != 0 ? true : false);
}

JNIEXPORT jlong JNICALL Java_com_tightdb_TableView_nativeFindFirstFloat(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jfloat value)
{
    if (!VIEW_VALID(env, nativeViewPtr) || 
        !COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_Float))
        return 0;

    return static_cast<jlong>(TV(nativeViewPtr)->find_first_float( S(columnIndex), value));
}

JNIEXPORT jlong JNICALL Java_com_tightdb_TableView_nativeFindFirstDouble(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jdouble value)
{
    if (!VIEW_VALID(env, nativeViewPtr) || 
        !COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_Double))
        return 0;

    return static_cast<jlong>(TV(nativeViewPtr)->find_first_double( S(columnIndex), value));
}

JNIEXPORT jlong JNICALL Java_com_tightdb_TableView_nativeFindFirstDate(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong dateTimeValue)
{
    if (!VIEW_VALID(env, nativeViewPtr) || 
        !COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_Date))
        return 0;

    return TV(nativeViewPtr)->find_first_date( S(columnIndex), (time_t)dateTimeValue);
}

JNIEXPORT jlong JNICALL Java_com_tightdb_TableView_nativeFindFirstString(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jstring value)
{
    if (!VIEW_VALID(env, nativeViewPtr) || 
        !COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_String))
        return 0;

    JStringAccessor value2(env, value);
    if (!value2)
        return 0;

    size_t searchIndex = TV(nativeViewPtr)->find_first_string( S(columnIndex), value2);
    return static_cast<jlong>(searchIndex);
}

// FindAll*

JNIEXPORT jlong JNICALL Java_com_tightdb_TableView_nativeFindAllInt(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong value)
{
    if (!VIEW_VALID(env, nativeViewPtr) || 
        !COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_Int))
        return 0;

    TableView* pResultView = new TableView( TV(nativeViewPtr)->find_all_int( S(columnIndex), value) );
    return reinterpret_cast<jlong>(pResultView);
}

JNIEXPORT jlong JNICALL Java_com_tightdb_TableView_nativeFindAllBool(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jboolean value)
{
    if (!VIEW_VALID(env, nativeViewPtr) || 
        !COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_Bool))
        return 0;

    TableView* pResultView = new TableView( TV(nativeViewPtr)->find_all_bool( S(columnIndex),
                                            value != 0 ? true : false) );
    return reinterpret_cast<jlong>(pResultView);
}

JNIEXPORT jlong JNICALL Java_com_tightdb_TableView_nativeFindAllFloat(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jfloat value)
{
    if (!VIEW_VALID(env, nativeViewPtr) || 
        !COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_Float))
        return 0;

    TableView* pResultView = new TableView( TV(nativeViewPtr)->find_all_float( S(columnIndex), value) );
    return reinterpret_cast<jlong>(pResultView);
}

JNIEXPORT jlong JNICALL Java_com_tightdb_TableView_nativeFindAllDouble(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jdouble value)
{
    if (!VIEW_VALID(env, nativeViewPtr) || 
        !COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_Double))
        return 0;

    TableView* pResultView = new TableView( TV(nativeViewPtr)->find_all_double( S(columnIndex), value) );
    return reinterpret_cast<jlong>(pResultView);
}

JNIEXPORT jlong JNICALL Java_com_tightdb_TableView_nativeFindAllDate(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jlong dateTimeValue)
{
    if (!VIEW_VALID(env, nativeViewPtr) || 
        !COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_Date))
        return 0;

    TableView* pResultView = new TableView( TV(nativeViewPtr)->find_all_date( S(columnIndex),
                                            static_cast<time_t>(dateTimeValue)) );
    return reinterpret_cast<jlong>(pResultView);
}

JNIEXPORT jlong JNICALL Java_com_tightdb_TableView_nativeFindAllString(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jstring value)
{

    if (!VIEW_VALID(env, nativeViewPtr) || 
        !COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_String))
        return 0;

    JStringAccessor value2(env, value);
    if (!value2)
        return 0;
    TR((env, "nativeFindAllString(col %d, string '%s') ", columnIndex, StringData(value2).data()));

    TableView* pResultView = new TableView( TV(nativeViewPtr)->find_all_string( S(columnIndex), value2) );
    TR((env, "-- resultview size=%lld.\n", pResultView->size()));
    return reinterpret_cast<jlong>(pResultView);
}

// Integer aggregates

JNIEXPORT jlong JNICALL Java_com_tightdb_TableView_nativeSum(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex)
{
    if (!VIEW_VALID(env, nativeViewPtr) || 
        !COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_Int))
        return 0;

    return TV(nativeViewPtr)->sum( S(columnIndex));
}

JNIEXPORT jdouble JNICALL Java_com_tightdb_TableView_nativeAverage(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex)
{
    if (!VIEW_VALID(env, nativeViewPtr) || 
        !COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_Int))
        return 0;

    // FIXME: Add support for native Average
    return static_cast<jdouble>( TV(nativeViewPtr)->sum( S(columnIndex)) ) / TV(nativeViewPtr)->size();
}

JNIEXPORT jlong JNICALL Java_com_tightdb_TableView_nativeMaximum(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex)
{
    if (!VIEW_VALID(env, nativeViewPtr) || 
        !COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_Int))
        return 0;

    return TV(nativeViewPtr)->maximum( S(columnIndex));
}

JNIEXPORT jlong JNICALL Java_com_tightdb_TableView_nativeMinimum(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex)
{
    if (!VIEW_VALID(env, nativeViewPtr) || 
        !COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_Int))
        return 0;

    return TV(nativeViewPtr)->minimum( S(columnIndex));
}

// float aggregates

JNIEXPORT jdouble JNICALL Java_com_tightdb_TableView_nativeSumFloat(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex)
{
    if (!VIEW_VALID(env, nativeViewPtr) || 
        !COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_Float))
        return 0;

    return TV(nativeViewPtr)->sum_float( S(columnIndex));
}

JNIEXPORT jdouble JNICALL Java_com_tightdb_TableView_nativeAverageFloat(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex)
{
    if (!VIEW_VALID(env, nativeViewPtr) || 
        !COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_Float))
        return 0;

    // FIXME: Add support for native Average
    return TV(nativeViewPtr)->sum_float( S(columnIndex)) / TV(nativeViewPtr)->size();
}

JNIEXPORT jfloat JNICALL Java_com_tightdb_TableView_nativeMaximumFloat(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex)
{
    if (!VIEW_VALID(env, nativeViewPtr) || 
        !COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_Float))
        return 0;

    return TV(nativeViewPtr)->maximum_float( S(columnIndex));
}

JNIEXPORT jfloat JNICALL Java_com_tightdb_TableView_nativeMinimumFloat(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex)
{
    if (!VIEW_VALID(env, nativeViewPtr) || 
        !COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_Float))
        return 0;

    return TV(nativeViewPtr)->minimum_float( S(columnIndex));
}

// double aggregates

JNIEXPORT jdouble JNICALL Java_com_tightdb_TableView_nativeSumDouble(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex)
{
    if (!VIEW_VALID(env, nativeViewPtr) || 
        !COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_Double))
        return 0;

    return TV(nativeViewPtr)->sum_double( S(columnIndex));
}

JNIEXPORT jdouble JNICALL Java_com_tightdb_TableView_nativeAverageDouble(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex)
{
    if (!VIEW_VALID(env, nativeViewPtr) || 
        !COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_Double))
        return 0;

    // FIXME: Add support for native Average
    return static_cast<jdouble>( TV(nativeViewPtr)->sum_double( S(columnIndex)) ) / TV(nativeViewPtr)->size();
}

JNIEXPORT jdouble JNICALL Java_com_tightdb_TableView_nativeMaximumDouble(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex)
{
    if (!VIEW_VALID(env, nativeViewPtr) || 
        !COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_Double))
        return 0;

    return TV(nativeViewPtr)->maximum_double( S(columnIndex));
}

JNIEXPORT jdouble JNICALL Java_com_tightdb_TableView_nativeMinimumDouble(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex)
{
    if (!VIEW_VALID(env, nativeViewPtr) || 
        !COL_INDEX_AND_TYPE_VALID(env, TV(nativeViewPtr), columnIndex, type_Double))
        return 0;

    return TV(nativeViewPtr)->minimum_double( S(columnIndex));
}

// sort

JNIEXPORT void JNICALL Java_com_tightdb_TableView_nativeSort(
    JNIEnv* env, jobject, jlong nativeViewPtr, jlong columnIndex, jboolean ascending)
{
    if (!VIEW_VALID(env, nativeViewPtr) || 
        !COL_INDEX_VALID(env, TV(nativeViewPtr), columnIndex)) 
        return;
    int colType = TV(nativeViewPtr)->get_column_type( S(columnIndex) );
    if (colType != type_Int && colType != type_Bool && colType != type_Date) {
        ThrowException(env, IllegalArgument, "Sort is currently not supported on this ColumnType.");
        return;
    }

    TV(nativeViewPtr)->sort( S(columnIndex), ascending != 0 ? true : false);
}

JNIEXPORT jstring JNICALL Java_com_tightdb_TableView_nativeToJson(
    JNIEnv *env, jobject, jlong nativeViewPtr)
{
    TableView* tv = TV(nativeViewPtr);
    if (!VIEW_VALID(env, nativeViewPtr))
        return NULL;

   // Write table to string in JSON format
   std::stringstream ss;
   ss.sync_with_stdio(false); // for performance
   tv->to_json(ss);
   const std::string str = ss.str();

   return env->NewStringUTF(str.c_str());
}

JNIEXPORT jstring JNICALL Java_com_tightdb_TableView_nativeToString(
    JNIEnv *env, jobject, jlong nativeViewPtr, jlong maxRows)
{
   TableView* tv = TV(nativeViewPtr);
   if (!VIEW_VALID(env, nativeViewPtr)) 
       return NULL;

   std::ostringstream ss;
   ss.sync_with_stdio(false); // for performance
   tv->to_string(ss, maxRows);
   const std::string str = ss.str();
   return env->NewStringUTF(str.c_str());
}

JNIEXPORT jstring JNICALL Java_com_tightdb_TableView_nativeRowToString(
    JNIEnv *env, jobject, jlong nativeViewPtr, jlong rowIndex)
{
   TableView* tv = TV(nativeViewPtr);
   if (!VIEW_VALID(env, nativeViewPtr) || 
       !ROW_INDEX_VALID(env, tv, rowIndex)) return NULL;

   std::ostringstream ss;
   tv->row_to_string(rowIndex, ss);
   const std::string str = ss.str();
   return env->NewStringUTF(str.c_str());
}

