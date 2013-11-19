#include <sstream>

#include "util.hpp"
#include "mixedutil.hpp"
#include "com_tightdb_Table.h"
#include "columntypeutil.hpp"
#include "TableSpecUtil.hpp"
#include "java_lang_List_Util.hpp"
#include "mixedutil.hpp"
#include "tablebase_tpl.hpp"

using namespace tightdb;

// Note: Don't modify spec on a table which has a shared_spec. 
// A spec is shared on subtables that are not in Mixed columns.
//

JNIEXPORT jlong JNICALL Java_com_tightdb_Table_nativeAddColumn
  (JNIEnv *env, jobject, jlong nativeTablePtr, jint colType, jstring name)
{
    if (!TABLE_VALID(env, TBL(nativeTablePtr)))
        return 0;
    JStringAccessor name2(env, name);
    if (!name2)
        return 0;
    if (TBL(nativeTablePtr)->has_shared_spec()) {
        ThrowException(env, UnsupportedOperation, "Not allowed to add column in subtable. Use getSubTableSchema() on root table instead.");
        return 0;
    }
    try {
        return TBL(nativeTablePtr)->add_column(DataType(colType), name2);
    } CATCH_STD()
    return 0;
}


JNIEXPORT void JNICALL Java_com_tightdb_Table_nativePivot
(JNIEnv *, jobject, jlong dataTablePtr, jlong stringCol, jlong intCol, jlong resultTablePtr)
{
    Table* dataTable = TBL(dataTablePtr);
    Table* resultTable = TBL(resultTablePtr);
    dataTable->pivot(S(stringCol), S(intCol), *resultTable);
}

JNIEXPORT void JNICALL Java_com_tightdb_Table_nativeRemoveColumn
  (JNIEnv *env, jobject, jlong nativeTablePtr, jlong columnIndex)
{
    if (!TBL_AND_COL_INDEX_VALID(env, TBL(nativeTablePtr), columnIndex))
        return;
    if (TBL(nativeTablePtr)->has_shared_spec()) {
        ThrowException(env, UnsupportedOperation, "Not allowed to remove column in subtable. Use getSubTableSchema() on root table instead.");
        return;
    }
    try {
        TBL(nativeTablePtr)->remove_column(S(columnIndex));
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_com_tightdb_Table_nativeRenameColumn
  (JNIEnv *env, jobject, jlong nativeTablePtr, jlong columnIndex, jstring name)
{
    if (!TBL_AND_COL_INDEX_VALID(env, TBL(nativeTablePtr), columnIndex))
        return;
    JStringAccessor name2(env, name);
    if (!name2)
        return;
    if (TBL(nativeTablePtr)->has_shared_spec()) {
        ThrowException(env, UnsupportedOperation, "Not allowed to rename column in subtable. Use getSubTableSchema() on root table instead.");
        return;
    }
    try {
        TBL(nativeTablePtr)->rename_column(S(columnIndex), name2);
    } CATCH_STD()
}


JNIEXPORT jboolean JNICALL Java_com_tightdb_Table_nativeIsRootTable
  (JNIEnv *, jobject, jlong nativeTablePtr)
{
    //If the spec is shared, it is a subtable, and this method will return false
    return !TBL(nativeTablePtr)->has_shared_spec(); 
}

JNIEXPORT void JNICALL Java_com_tightdb_Table_nativeUpdateFromSpec(
    JNIEnv* env, jobject, jlong nativeTablePtr, jobject jTableSpec)
{
    Table* pTable = TBL(nativeTablePtr);
    TR((env, "nativeUpdateFromSpec(tblPtr %x, spec %x)\n", pTable, jTableSpec));
    if (!TABLE_VALID(env, pTable))
        return;
    if (TBL(nativeTablePtr)->has_shared_spec()) {
        ThrowException(env, UnsupportedOperation, "It is not allowed to update a subtable from spec.");
        return;
    }
    try {
        Spec& spec = pTable->get_spec();
        updateSpecFromJSpec(env, spec, jTableSpec);
        pTable->update_from_spec();
    } CATCH_STD()
}

JNIEXPORT jobject JNICALL Java_com_tightdb_Table_nativeGetTableSpec(
    JNIEnv* env, jobject, jlong nativeTablePtr)
{
    if (!TABLE_VALID(env, TBL(nativeTablePtr)))
        return 0;

    TR((env, "nativeGetTableSpec(table %x)\n", nativeTablePtr));
    static jmethodID jTableSpecConsId = GetTableSpecMethodID(env, "<init>", "()V");
    if (jTableSpecConsId) {
        try {
            // Create a new TableSpec object in Java
            const Table* pTable = TBL(nativeTablePtr);
            const Spec& tableSpec = pTable->get_spec();     // noexcept
            jobject jTableSpec = env->NewObject(GetClassTableSpec(env), jTableSpecConsId);
            if (jTableSpec) {
                // copy the c++ spec to the new java TableSpec
                UpdateJTableSpecFromSpec(env, tableSpec, jTableSpec);
                return jTableSpec;
            }
        } CATCH_STD()
    }
    return 0;
}


JNIEXPORT jlong JNICALL Java_com_tightdb_Table_nativeSize(
    JNIEnv* env, jobject, jlong nativeTablePtr)
{
    if (!TABLE_VALID(env, TBL(nativeTablePtr)))
        return 0;
    return TBL(nativeTablePtr)->size();     // noexcept
}

JNIEXPORT void JNICALL Java_com_tightdb_Table_nativeClear(
    JNIEnv* env, jobject, jlong nativeTablePtr)
{
    if (!TABLE_VALID(env, TBL(nativeTablePtr)))
        return;
    try {
        TBL(nativeTablePtr)->clear();
    } CATCH_STD()
}


// -------------- Column information


JNIEXPORT jlong JNICALL Java_com_tightdb_Table_nativeGetColumnCount(
    JNIEnv* env, jobject, jlong nativeTablePtr)
{
    if (!TABLE_VALID(env, TBL(nativeTablePtr)))
        return 0;
    return TBL(nativeTablePtr)->get_column_count(); // noexcept
}

JNIEXPORT jstring JNICALL Java_com_tightdb_Table_nativeGetColumnName(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex)
{
    if (!TBL_AND_COL_INDEX_VALID(env, TBL(nativeTablePtr), columnIndex))
        return NULL;
    return to_jstring(env, TBL(nativeTablePtr)->get_column_name( S(columnIndex))); // noexcept
}

JNIEXPORT jint JNICALL Java_com_tightdb_Table_nativeGetColumnType(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex)
{
    if (!TBL_AND_COL_INDEX_VALID(env, TBL(nativeTablePtr), columnIndex))
        return 0;

    return static_cast<int>( TBL(nativeTablePtr)->get_column_type( S(columnIndex)) ); // noexcept
}

// TODO: get_column_index() ?


// ---------------- Row handling

JNIEXPORT jlong JNICALL Java_com_tightdb_Table_nativeAddEmptyRow(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong rows)
{
    Table* pTable = TBL(nativeTablePtr);
    if (!TABLE_VALID(env, pTable))
        return 0;
    if(pTable->get_column_count() < 1){
        ThrowException(env, IndexOutOfBounds, "Table has no columns");
        return 0;
    }
    try {
        return static_cast<jlong>( pTable->add_empty_row( S(rows)) );
    } CATCH_STD()
    return 0;
}

JNIEXPORT void JNICALL Java_com_tightdb_Table_nativeRemove(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong rowIndex)
{
    if (!TBL_AND_ROW_INDEX_VALID(env, TBL(nativeTablePtr), rowIndex))
        return;
    try {
        TBL(nativeTablePtr)->remove(S(rowIndex));
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_com_tightdb_Table_nativeRemoveLast(
    JNIEnv* env, jobject, jlong nativeTablePtr)
{
    if (!TABLE_VALID(env, TBL(nativeTablePtr)))
        return;
    try {
        TBL(nativeTablePtr)->remove_last();
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_com_tightdb_Table_nativeMoveLastOver
  (JNIEnv *env, jobject, jlong nativeTablePtr, jlong rowIndex)
{
    if (!TBL_AND_ROW_INDEX_VALID_OFFSET(env, TBL(nativeTablePtr), rowIndex, -1))
        return;
    try {
        TBL(nativeTablePtr)->move_last_over(S(rowIndex));
    } CATCH_STD()
}


// ----------------- Insert cell

JNIEXPORT void JNICALL Java_com_tightdb_Table_nativeInsertLong(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex, jlong value)
{
    if (!TBL_AND_INDEX_AND_TYPE_INSERT_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex, type_Int))
        return;
    try {
        TBL(nativeTablePtr)->insert_int( S(columnIndex), S(rowIndex), value);
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_com_tightdb_Table_nativeInsertBoolean(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex, jboolean value)
{
    if (!TBL_AND_INDEX_AND_TYPE_INSERT_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex, type_Bool))
        return;
    try {
        TBL(nativeTablePtr)->insert_bool( S(columnIndex), S(rowIndex), value != 0 ? true : false);
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_com_tightdb_Table_nativeInsertFloat(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex, jfloat value)
{
    if (!TBL_AND_INDEX_AND_TYPE_INSERT_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex, type_Float))
        return;
    try {
        TBL(nativeTablePtr)->insert_float( S(columnIndex), S(rowIndex), value);
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_com_tightdb_Table_nativeInsertDouble(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex, jdouble value)
{
    if (!TBL_AND_INDEX_AND_TYPE_INSERT_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex, type_Double))
        return;
    try {
        TBL(nativeTablePtr)->insert_double( S(columnIndex), S(rowIndex), value);
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_com_tightdb_Table_nativeInsertDate(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex, jlong dateTimeValue)
{
    if (!TBL_AND_INDEX_AND_TYPE_INSERT_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex, type_DateTime))
        return;
    try {
        TBL(nativeTablePtr)->insert_datetime( S(columnIndex), S(rowIndex), static_cast<time_t>(dateTimeValue));
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_com_tightdb_Table_nativeInsertString(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex, jstring value)
{
    if (!TBL_AND_INDEX_AND_TYPE_INSERT_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex, type_String))
        return;
    JStringAccessor value2(env, value);
    if (!value2)
        return;
    try {
        TBL(nativeTablePtr)->insert_string( S(columnIndex), S(rowIndex), value2);
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_com_tightdb_Table_nativeInsertMixed(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex, jobject jMixedValue)
{
    if (!TBL_AND_INDEX_INSERT_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex)) 
        return;
    try {
        tbl_nativeDoMixed(&Table::insert_mixed, TBL(nativeTablePtr), env, columnIndex, rowIndex, jMixedValue);
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_com_tightdb_Table_nativeSetMixed(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex, jobject jMixedValue)
{
    if (!TBL_AND_INDEX_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex)) 
        return;
    try {
        tbl_nativeDoMixed(&Table::set_mixed, TBL(nativeTablePtr), env, columnIndex, rowIndex, jMixedValue);
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_com_tightdb_Table_nativeInsertSubTable(
    JNIEnv* env, jobject jTable, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex)
{
    if (!TBL_AND_INDEX_AND_TYPE_INSERT_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex, type_Table))
        return;

    TR((env, "nativeInsertSubTable(jTable:%x, nativeTablePtr: %x, colIdx: %lld, rowIdx: %lld)\n",
       jTable, nativeTablePtr,  columnIndex, rowIndex));
    try {
        TBL(nativeTablePtr)->insert_subtable( S(columnIndex), S(rowIndex));
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_com_tightdb_Table_nativeInsertDone(
    JNIEnv* env, jobject, jlong nativeTablePtr)
{
    if (!TABLE_VALID(env, TBL(nativeTablePtr)))
        return;
    try {
        TBL(nativeTablePtr)->insert_done();
    } CATCH_STD()
}


// ----------------- Get cell

JNIEXPORT jlong JNICALL Java_com_tightdb_Table_nativeGetLong(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex)
{
    if (!TBL_AND_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex, type_Int))
        return 0;

    return TBL(nativeTablePtr)->get_int( S(columnIndex), S(rowIndex));  // noexcept
}

JNIEXPORT jboolean JNICALL Java_com_tightdb_Table_nativeGetBoolean(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex)
{
    if (!TBL_AND_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex, type_Bool))
        return false;

    return TBL(nativeTablePtr)->get_bool( S(columnIndex), S(rowIndex));  // noexcept
}

JNIEXPORT jfloat JNICALL Java_com_tightdb_Table_nativeGetFloat(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex)
{
    if (!TBL_AND_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex, type_Float))
        return 0;

    return TBL(nativeTablePtr)->get_float( S(columnIndex), S(rowIndex));  // noexcept
}

JNIEXPORT jdouble JNICALL Java_com_tightdb_Table_nativeGetDouble(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex)
{
    if (!TBL_AND_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex, type_Double))
        return 0;

    return TBL(nativeTablePtr)->get_double( S(columnIndex), S(rowIndex));  // noexcept
}

JNIEXPORT jlong JNICALL Java_com_tightdb_Table_nativeGetDateTime(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex)
{
    if (!TBL_AND_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex, type_DateTime))
        return 0;
    return TBL(nativeTablePtr)->get_datetime( S(columnIndex), S(rowIndex)).get_datetime();  // noexcept
}

JNIEXPORT jstring JNICALL Java_com_tightdb_Table_nativeGetString(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex)
{
    if (!TBL_AND_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex, type_String))
        return NULL;

    return to_jstring(env, TBL(nativeTablePtr)->get_string( S(columnIndex), S(rowIndex)));  // noexcept
}

/*
JNIEXPORT jobject JNICALL Java_com_tightdb_Table_nativeGetByteBuffer(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex)
{
    if (!TBL_AND_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex, type_Binary))
        return NULL;

    BinaryData bin = TBL(nativeTablePtr)->get_binary( S(columnIndex), S(rowIndex));
    return env->NewDirectByteBuffer(const_cast<char*>(bin.data()), bin.size());  // throws
}
*/

JNIEXPORT jbyteArray JNICALL Java_com_tightdb_Table_nativeGetByteArray(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex)
{
    if (!TBL_AND_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex, type_Binary))
        return NULL;

    return tbl_GetByteArray<Table>(env, nativeTablePtr, columnIndex, rowIndex);  // noexcept
}

JNIEXPORT jint JNICALL Java_com_tightdb_Table_nativeGetMixedType(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex)
{
    if (!TBL_AND_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex, type_Mixed))
        return 0;

    DataType mixedType = TBL(nativeTablePtr)->get_mixed_type( S(columnIndex), S(rowIndex));  // noexcept
    return static_cast<jint>(mixedType);
}

JNIEXPORT jobject JNICALL Java_com_tightdb_Table_nativeGetMixed(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex)
{
    if (!TBL_AND_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex, type_Mixed))
        return NULL;

    Mixed value = TBL(nativeTablePtr)->get_mixed( S(columnIndex), S(rowIndex));  // noexcept
    return CreateJMixedFromMixed(env, value);
}

JNIEXPORT jlong JNICALL Java_com_tightdb_Table_nativeGetSubTable(
    JNIEnv* env, jobject jTableBase, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex)
{
    if (!TBL_AND_INDEX_AND_TYPE_VALID_MIXED(env, TBL(nativeTablePtr), columnIndex, rowIndex, type_Table)) 
        return 0;
    try {
        Table* pSubTable = static_cast<Table*>(LangBindHelper::get_subtable_ptr(TBL(nativeTablePtr),
            S(columnIndex), S(rowIndex)));
        TR((env, "nativeGetSubTable(jTableBase:%x, nativeTablePtr: %x, colIdx: %lld, rowIdx: %lld) : %x\n",
            jTableBase, nativeTablePtr, columnIndex, rowIndex, pSubTable));
        return (jlong)pSubTable;
    } CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_com_tightdb_Table_nativeGetSubTableDuringInsert(
    JNIEnv* env, jobject jTableBase, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex)
{
    if (!TBL_AND_INDEX_AND_TYPE_INSERT_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex, type_Table))
        return 0;
    try {
        Table* pSubTable = static_cast<Table*>(LangBindHelper::get_subtable_ptr_during_insert(
            TBL(nativeTablePtr), S(columnIndex), S(rowIndex)));
        TR((env, "nativeGetSubTableDuringInsert(jTableBase:%x, nativeTablePtr: %x, colIdx: %lld, rowIdx: %lld) : %x\n",
            jTableBase, nativeTablePtr, columnIndex, rowIndex, pSubTable));
        return (jlong)pSubTable;
    } CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_com_tightdb_Table_nativeGetSubTableSize(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex)
{
    if (!TBL_AND_INDEX_AND_TYPE_VALID_MIXED(env, TBL(nativeTablePtr), columnIndex, rowIndex, type_Table)) 
        return 0;

    return TBL(nativeTablePtr)->get_subtable_size( S(columnIndex), S(rowIndex)); // noexcept
}


// ----------------- Set cell

JNIEXPORT void JNICALL Java_com_tightdb_Table_nativeSetLong(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex, jlong value)
{
    if (!TBL_AND_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex, type_Int))
        return;
    try {
        TBL(nativeTablePtr)->set_int( S(columnIndex), S(rowIndex), value);
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_com_tightdb_Table_nativeSetBoolean(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex, jboolean value)
{
    if (!TBL_AND_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex, type_Bool)) 
        return;
    try {
        TBL(nativeTablePtr)->set_bool( S(columnIndex), S(rowIndex), value == JNI_TRUE ? true : false);
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_com_tightdb_Table_nativeSetFloat(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex, jfloat value)
{
    if (!TBL_AND_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex, type_Float))
        return;
    try {
        TBL(nativeTablePtr)->set_float( S(columnIndex), S(rowIndex), value);
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_com_tightdb_Table_nativeSetDouble(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex, jdouble value)
{
    if (!TBL_AND_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex, type_Double))
        return;
    try {
        TBL(nativeTablePtr)->set_double( S(columnIndex), S(rowIndex), value);
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_com_tightdb_Table_nativeSetString(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex, jstring value)
{
    if (!TBL_AND_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex, type_String))
        return;
    JStringAccessor value2(env, value);
    try {
        if (value2) {
            TBL(nativeTablePtr)->set_string( S(columnIndex), S(rowIndex), value2);
        }
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_com_tightdb_Table_nativeSetDate(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex, jlong dateTimeValue)
{
    if (!TBL_AND_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex, type_DateTime))
        return;
    try {
        TBL(nativeTablePtr)->set_datetime( S(columnIndex), S(rowIndex), dateTimeValue);
    } CATCH_STD()
}

/*
JNIEXPORT void JNICALL Java_com_tightdb_Table_nativeSetByteBuffer(
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
JNIEXPORT void JNICALL Java_com_tightdb_Table_nativeInsertByteBuffer(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex, jobject byteBuffer)
{
    if (!TBL_AND_INDEX_AND_TYPE_INSERT_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex, type_Binary))
        return;
    try {
        tbl_nativeDoBinary(&Table::insert_binary, TBL(nativeTablePtr), env, columnIndex, rowIndex, byteBuffer);
    } CATCH_STD()
}
*/

JNIEXPORT void JNICALL Java_com_tightdb_Table_nativeSetByteArray(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex, jbyteArray dataArray)
{
    if (!TBL_AND_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex, type_Binary))
        return;
    try {
        tbl_nativeDoByteArray(&Table::set_binary, TBL(nativeTablePtr), env, columnIndex, rowIndex, dataArray);
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_com_tightdb_Table_nativeInsertByteArray(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex, jbyteArray dataArray)
{
    if (!TBL_AND_INDEX_AND_TYPE_INSERT_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex, type_Binary))
        return;
    try {
        tbl_nativeDoByteArray(&Table::insert_binary, TBL(nativeTablePtr), env, columnIndex, rowIndex, dataArray);
    } CATCH_STD()
}


JNIEXPORT void JNICALL Java_com_tightdb_Table_nativeAddInt(
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


JNIEXPORT void JNICALL Java_com_tightdb_Table_nativeClearSubTable(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex)
{
    if (!TBL_AND_INDEX_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex)) 
        return;
    try {
        TBL(nativeTablePtr)->clear_subtable( S(columnIndex), S(rowIndex));
    } CATCH_STD()
}

//--------------------- Indexing methods:

JNIEXPORT void JNICALL Java_com_tightdb_Table_nativeSetIndex(
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
        pTable->set_index( S(columnIndex));
    } CATCH_STD()
}

JNIEXPORT jboolean JNICALL Java_com_tightdb_Table_nativeHasIndex(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex)
{
    if (!TBL_AND_COL_INDEX_VALID(env, TBL(nativeTablePtr), columnIndex)) 
        return false;
    try {
        return TBL(nativeTablePtr)->has_index( S(columnIndex));
    } CATCH_STD()
    return false;
}

//---------------------- Aggregare methods for integers

JNIEXPORT jlong JNICALL Java_com_tightdb_Table_nativeSumInt(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, type_Int)) 
        return 0;
    try {
        return TBL(nativeTablePtr)->sum_int( S(columnIndex));
    } CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_com_tightdb_Table_nativeMaximumInt(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, type_Int))
        return 0;
    try {
        return TBL(nativeTablePtr)->maximum_int( S(columnIndex));
    } CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_com_tightdb_Table_nativeMinimumInt(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, type_Int))
        return 0;
    try {
        return TBL(nativeTablePtr)->minimum_int( S(columnIndex));
    } CATCH_STD()
    return 0;
}

JNIEXPORT jdouble JNICALL Java_com_tightdb_Table_nativeAverageInt(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, type_Int))
        return 0;
    try {
        return TBL(nativeTablePtr)->average_int( S(columnIndex));
    } CATCH_STD()
    return 0;
}

//--------------------- Aggregare methods for float

JNIEXPORT jdouble JNICALL Java_com_tightdb_Table_nativeSumFloat(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, type_Float))
        return 0;
    try {
        return TBL(nativeTablePtr)->sum_float( S(columnIndex));
    } CATCH_STD()
    return 0;
}

JNIEXPORT jfloat JNICALL Java_com_tightdb_Table_nativeMaximumFloat(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, type_Float))
        return 0;
    try {
        return TBL(nativeTablePtr)->maximum_float( S(columnIndex));
    } CATCH_STD()
    return 0;
}

JNIEXPORT jfloat JNICALL Java_com_tightdb_Table_nativeMinimumFloat(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, type_Float))
        return 0;
    try {
        return TBL(nativeTablePtr)->minimum_float( S(columnIndex));
    } CATCH_STD()
    return 0;
}

JNIEXPORT jdouble JNICALL Java_com_tightdb_Table_nativeAverageFloat(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, type_Float))
        return 0;
    try {
        return TBL(nativeTablePtr)->average_float( S(columnIndex));
    } CATCH_STD()
    return 0;
}


//--------------------- Aggregare methods for double

JNIEXPORT jdouble JNICALL Java_com_tightdb_Table_nativeSumDouble(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, type_Double))
        return 0;
    try {
        return TBL(nativeTablePtr)->sum_double( S(columnIndex));
    } CATCH_STD()
    return 0;
}

JNIEXPORT jdouble JNICALL Java_com_tightdb_Table_nativeMaximumDouble(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, type_Double))
        return 0;
    try {
        return TBL(nativeTablePtr)->maximum_double( S(columnIndex));
    } CATCH_STD()
    return 0;
}

JNIEXPORT jdouble JNICALL Java_com_tightdb_Table_nativeMinimumDouble(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, type_Double))
        return 0;
    try {
        return TBL(nativeTablePtr)->minimum_double( S(columnIndex));
    } CATCH_STD()
    return 0;
}

JNIEXPORT jdouble JNICALL Java_com_tightdb_Table_nativeAverageDouble(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, type_Double))
        return 0;
    try {
        return TBL(nativeTablePtr)->average_double( S(columnIndex));
    } CATCH_STD()
    return 0;
}

//---------------------- Count

JNIEXPORT jlong JNICALL Java_com_tightdb_Table_nativeCountLong(
    JNIEnv *env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong value)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, type_Int))
        return 0;
    try {
        return TBL(nativeTablePtr)->count_int( S(columnIndex), value);
    } CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_com_tightdb_Table_nativeCountFloat(
    JNIEnv *env, jobject, jlong nativeTablePtr, jlong columnIndex, jfloat value)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, type_Float))
        return 0;
    try {
        return TBL(nativeTablePtr)->count_float( S(columnIndex), value);
    } CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_com_tightdb_Table_nativeCountDouble(
    JNIEnv *env, jobject, jlong nativeTablePtr, jlong columnIndex, jdouble value)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, type_Double))
        return 0;
    try {
        return TBL(nativeTablePtr)->count_double( S(columnIndex), value);
    } CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_com_tightdb_Table_nativeCountString(
    JNIEnv *env, jobject, jlong nativeTablePtr, jlong columnIndex, jstring value)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, type_String))
        return 0;

    JStringAccessor value2(env, value);
    if (!value2)
        return 0;
    try {
        return TBL(nativeTablePtr)->count_string( S(columnIndex), value2);
    } CATCH_STD()
    return 0;
}



JNIEXPORT jlong JNICALL Java_com_tightdb_Table_nativeLookup(
    JNIEnv *env, jobject, jlong nativeTablePtr, jstring value)
{
    // Must have a string column as first column
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), 0, type_String))
        return 0;

    JStringAccessor value2(env, value);
    if (!value2)
        return 0;
    try {
        size_t res = TBL(nativeTablePtr)->lookup(value2);
        return (res == not_found) ? jlong(-1) : jlong(res);
    } CATCH_STD()
    return 0;
}

//

JNIEXPORT jlong JNICALL Java_com_tightdb_Table_nativeWhere(
    JNIEnv *env, jobject, jlong nativeTablePtr)
{
    if (!TABLE_VALID(env, TBL(nativeTablePtr)))
        return 0;
    try {
        Query query = TBL(nativeTablePtr)->where();
        Query* queryPtr = new Query(query);
        return reinterpret_cast<jlong>(queryPtr);
    } CATCH_STD()
    return 0;
}

//----------------------- FindFirst

JNIEXPORT jlong JNICALL Java_com_tightdb_Table_nativeFindFirstInt(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong value)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, type_Int))
        return 0;
    try {
        return TBL(nativeTablePtr)->find_first_int( S(columnIndex), value);
    } CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_com_tightdb_Table_nativeFindFirstBool(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jboolean value)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, type_Bool))
        return 0;
    try {
        return TBL(nativeTablePtr)->find_first_bool( S(columnIndex), value != 0 ? true : false);
    } CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_com_tightdb_Table_nativeFindFirstFloat(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jfloat value)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, type_Float))
        return 0;
    try {
        return TBL(nativeTablePtr)->find_first_float( S(columnIndex), value);
    } CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_com_tightdb_Table_nativeFindFirstDouble(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jdouble value)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, type_Double))
        return 0;
    try {
        return TBL(nativeTablePtr)->find_first_double( S(columnIndex), value);
    } CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_com_tightdb_Table_nativeFindFirstDate(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong dateTimeValue)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, type_DateTime))
        return 0;
    try {
        return TBL(nativeTablePtr)->find_first_datetime( S(columnIndex), (time_t)dateTimeValue);
    } CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_com_tightdb_Table_nativeFindFirstString(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jstring value)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, type_String))
        return 0;

    JStringAccessor value2(env, value);
    if (!value2)
        return 0;
    try {
        return TBL(nativeTablePtr)->find_first_string( S(columnIndex), value2);
    } CATCH_STD()
    return 0;
}

// FindAll

JNIEXPORT jlong JNICALL Java_com_tightdb_Table_nativeFindAllInt(
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

JNIEXPORT jlong JNICALL Java_com_tightdb_Table_nativeFindAllFloat(
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

JNIEXPORT jlong JNICALL Java_com_tightdb_Table_nativeFindAllDouble(
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

JNIEXPORT jlong JNICALL Java_com_tightdb_Table_nativeFindAllBool(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jboolean value)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, type_Bool))
        return 0;

    TableView* pTableView = new TableView( TBL(nativeTablePtr)->find_all_bool( S(columnIndex),
                                           value != 0 ? true : false) );
    return reinterpret_cast<jlong>(pTableView);
}

JNIEXPORT jlong JNICALL Java_com_tightdb_Table_nativeFindAllDate(
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

JNIEXPORT jlong JNICALL Java_com_tightdb_Table_nativeFindAllString(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jstring value)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, type_String))
        return 0;

    Table* pTable = TBL(nativeTablePtr);
    JStringAccessor value2(env, value);
    if (!value2)
        return 0;
    try {
        TableView* pTableView = new TableView( pTable->find_all_string( S(columnIndex), value2) );
        return reinterpret_cast<jlong>(pTableView);
    } CATCH_STD()
    return 0;
}


// experimental
JNIEXPORT jlong JNICALL Java_com_tightdb_Table_nativeLowerBoundInt(
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
JNIEXPORT jlong JNICALL Java_com_tightdb_Table_nativeUpperBoundInt(
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

JNIEXPORT jlong JNICALL Java_com_tightdb_Table_nativeGetDistinctView(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex)
{
    Table* pTable = TBL(nativeTablePtr);
    if (!TBL_AND_COL_INDEX_VALID(env, pTable, columnIndex))
        return 0;
    if (!pTable->has_index(S(columnIndex))) {
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

JNIEXPORT void JNICALL Java_com_tightdb_Table_nativeOptimize(
    JNIEnv* env, jobject, jlong nativeTablePtr)
{
    if (!TABLE_VALID(env, TBL(nativeTablePtr)))
        return;
    try {
        TBL(nativeTablePtr)->optimize();
    } CATCH_STD()
}

JNIEXPORT jstring JNICALL Java_com_tightdb_Table_nativeToJson(
    JNIEnv *env, jobject, jlong nativeTablePtr)
{
    Table* table = TBL(nativeTablePtr);
    if (!TABLE_VALID(env, table))
        return NULL;

    // Write table to string in JSON format
    try {
        std::ostringstream ss;
        ss.sync_with_stdio(false); // for performance
        table->to_json(ss);
        const std::string str = ss.str();
        return env->NewStringUTF(str.c_str());
    } CATCH_STD()
    return NULL;
}

JNIEXPORT jstring JNICALL Java_com_tightdb_Table_nativeToString(
    JNIEnv *env, jobject, jlong nativeTablePtr, jlong maxRows)
{
    Table* table = TBL(nativeTablePtr);
    if (!TABLE_VALID(env, table))
        return NULL;
    try {
        std::ostringstream ss;
        table->to_string(ss, S(maxRows));
        const std::string str = ss.str();
        return env->NewStringUTF(str.c_str());
    } CATCH_STD()
    return NULL;
}

JNIEXPORT jstring JNICALL Java_com_tightdb_Table_nativeRowToString(
    JNIEnv *env, jobject, jlong nativeTablePtr, jlong rowIndex)
{
    Table* table = TBL(nativeTablePtr);
    if (!TBL_AND_ROW_INDEX_VALID(env, table, rowIndex))
        return NULL;
    try {
        std::ostringstream ss;
        table->row_to_string(S(rowIndex), ss);
        const std::string str = ss.str();
        return env->NewStringUTF(str.c_str());
    } CATCH_STD()
    return NULL;
}

JNIEXPORT jboolean JNICALL Java_com_tightdb_Table_nativeEquals(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong nativeTableToComparePtr)
{
    Table* tbl = TBL(nativeTablePtr);
    Table* tblToCompare = TBL(nativeTableToComparePtr);
    try {
        return (*tbl == *tblToCompare);
    } CATCH_STD()
    return false;
}

JNIEXPORT jboolean JNICALL Java_com_tightdb_Table_nativeIsValid(
    JNIEnv*, jobject, jlong nativeTablePtr)
{
    return TBL(nativeTablePtr)->is_attached();  // noexcept
}

JNIEXPORT void JNICALL Java_com_tightdb_Table_nativeClose(
    JNIEnv* env, jobject jTable, jlong nativeTablePtr)
{
    if (!TABLE_VALID(env, TBL(nativeTablePtr)))
        return;

    TR((env, "nativeClose(jTable: %x, nativeTablePtr: %x)\n", jTable, nativeTablePtr));
    LangBindHelper::unbind_table_ref(TBL(nativeTablePtr));
}

JNIEXPORT jlong JNICALL Java_com_tightdb_Table_createNative(JNIEnv* env, jobject jTable)
{
    TR((env, "CreateNative(jTable: %x)\n", jTable));
    try {
        return reinterpret_cast<jlong>(LangBindHelper::new_table());
    } CATCH_STD()
    return 0;
}
