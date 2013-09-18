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

// TODO: check:
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
    //TODO: add check that nativeTablePtr->has_shared_spec() == false
    // the same for other spec modifying operations
    return TBL(nativeTablePtr)->add_column(DataType(colType), name2);
}

JNIEXPORT void JNICALL Java_com_tightdb_Table_nativeRemoveColumn
  (JNIEnv *env, jobject, jlong nativeTablePtr, jlong columnIndex)
{
    if (!TBL_AND_COL_INDEX_VALID(env, TBL(nativeTablePtr), columnIndex))
        return;
    // TODO: see addColumn
    TBL(nativeTablePtr)->remove_column(S(columnIndex));
}

JNIEXPORT void JNICALL Java_com_tightdb_Table_nativeRenameColumn
  (JNIEnv *env, jobject, jlong nativeTablePtr, jlong columnIndex, jstring name)
{
    if (!TBL_AND_COL_INDEX_VALID(env, TBL(nativeTablePtr), columnIndex))
        return;
    JStringAccessor name2(env, name);
    if (!name2)
        return;
    // TODO: see addColumn
    TBL(nativeTablePtr)->rename_column(S(columnIndex), name2);
}

JNIEXPORT void JNICALL Java_com_tightdb_Table_nativeMoveLastOver
  (JNIEnv *env, jobject, jlong nativeTablePtr, jlong rowIndex)
{
    if (!TBL_AND_ROW_INDEX_VALID_OFFSET(env, TBL(nativeTablePtr), rowIndex, -1))
        return;
    TBL(nativeTablePtr)->move_last_over(S(rowIndex));
}


JNIEXPORT void JNICALL Java_com_tightdb_Table_nativeUpdateFromSpec(
    JNIEnv* env, jobject, jlong nativeTablePtr, jobject jTableSpec)
{
    if (!TABLE_VALID(env, TBL(nativeTablePtr)))
        return;

    Table* pTable = TBL(nativeTablePtr);
    TR((env, "nativeUpdateFromSpec(tblPtr %x, spec %x)\n", pTable, jTableSpec));
    Spec& spec = pTable->get_spec();
    updateSpecFromJSpec(env, spec, jTableSpec);
    pTable->update_from_spec();
}

JNIEXPORT jlong JNICALL Java_com_tightdb_Table_nativeSize(
    JNIEnv* env, jobject, jlong nativeTablePtr)
{
    if (!TABLE_VALID(env, TBL(nativeTablePtr)))
        return 0;
    return TBL(nativeTablePtr)->size();
}

JNIEXPORT void JNICALL Java_com_tightdb_Table_nativeClear(
    JNIEnv* env, jobject, jlong nativeTablePtr)
{
    if (!TABLE_VALID(env, TBL(nativeTablePtr)))
        return;
    TBL(nativeTablePtr)->clear();
}

JNIEXPORT jlong JNICALL Java_com_tightdb_Table_nativeGetColumnCount(
    JNIEnv* env, jobject, jlong nativeTablePtr)
{
    if (!TABLE_VALID(env, TBL(nativeTablePtr)))
        return 0;
    return TBL(nativeTablePtr)->get_column_count();
}

JNIEXPORT jstring JNICALL Java_com_tightdb_Table_nativeGetColumnName(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex)
{
    if (!TBL_AND_COL_INDEX_VALID(env, TBL(nativeTablePtr), columnIndex))
        return NULL;
    return to_jstring(env, TBL(nativeTablePtr)->get_column_name( S(columnIndex)));
}

JNIEXPORT jobject JNICALL Java_com_tightdb_Table_nativeGetTableSpec(
    JNIEnv* env, jobject, jlong nativeTablePtr)
{
    if (!TABLE_VALID(env, TBL(nativeTablePtr)))
        return 0;

    TR((env, "nativeGetTableSpec(table %x)\n", nativeTablePtr));
    static jmethodID jTableSpecConsId = GetTableSpecMethodID(env, "<init>", "()V");
    if (jTableSpecConsId) {
        // Create a new TableSpec object in Java
        const Table* pTable = TBL(nativeTablePtr);
        const Spec& tableSpec = pTable->get_spec();
        jobject jTableSpec = env->NewObject(GetClassTableSpec(env), jTableSpecConsId);
        if (jTableSpec) {
            // copy the c++ spec to the new java TableSpec
            UpdateJTableSpecFromSpec(env, tableSpec, jTableSpec);
            return jTableSpec;
        }
    }
    return NULL;
}

JNIEXPORT jint JNICALL Java_com_tightdb_Table_nativeGetColumnType(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex)
{
    if (!TBL_AND_COL_INDEX_VALID(env, TBL(nativeTablePtr), columnIndex))
        return 0;

    return static_cast<int>( TBL(nativeTablePtr)->get_column_type( S(columnIndex)) );
}

JNIEXPORT jlong JNICALL Java_com_tightdb_Table_nativeAddEmptyRow(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong rows)
{
    if (!TABLE_VALID(env, TBL(nativeTablePtr)))
        return 0;

    return static_cast<jlong>( TBL(nativeTablePtr)->add_empty_row( S(rows)) );
}

JNIEXPORT void JNICALL Java_com_tightdb_Table_nativeRemove(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong rowIndex)
{
    if (!TBL_AND_ROW_INDEX_VALID(env, TBL(nativeTablePtr), rowIndex))
        return;

    TBL(nativeTablePtr)->remove(S(rowIndex));
}

JNIEXPORT void JNICALL Java_com_tightdb_Table_nativeRemoveLast(
    JNIEnv* env, jobject, jlong nativeTablePtr)
{
    if (!TABLE_VALID(env, TBL(nativeTablePtr)))
        return;

    TBL(nativeTablePtr)->remove_last();
}

JNIEXPORT void JNICALL Java_com_tightdb_Table_nativeInsertLong(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex, jlong value)
{
    if (!TBL_AND_INDEX_AND_TYPE_INSERT_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex, type_Int))
        return;

    TBL(nativeTablePtr)->insert_int( S(columnIndex), S(rowIndex), value);
}

JNIEXPORT void JNICALL Java_com_tightdb_Table_nativeInsertBoolean(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex, jboolean value)
{
    if (!TBL_AND_INDEX_AND_TYPE_INSERT_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex, type_Bool))
        return;

    TBL(nativeTablePtr)->insert_bool( S(columnIndex), S(rowIndex), value != 0 ? true : false);
}

JNIEXPORT void JNICALL Java_com_tightdb_Table_nativeInsertFloat(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex, jfloat value)
{
    if (!TBL_AND_INDEX_AND_TYPE_INSERT_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex, type_Float))
        return;

    TBL(nativeTablePtr)->insert_float( S(columnIndex), S(rowIndex), value);
}

JNIEXPORT void JNICALL Java_com_tightdb_Table_nativeInsertDouble(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex, jdouble value)
{
    if (!TBL_AND_INDEX_AND_TYPE_INSERT_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex, type_Double))
        return;

    TBL(nativeTablePtr)->insert_double( S(columnIndex), S(rowIndex), value);
}

JNIEXPORT void JNICALL Java_com_tightdb_Table_nativeInsertDate(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex, jlong dateTimeValue)
{
    if (!TBL_AND_INDEX_AND_TYPE_INSERT_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex, type_Date))
        return;

    TBL(nativeTablePtr)->insert_date( S(columnIndex), S(rowIndex), static_cast<time_t>(dateTimeValue));
}

JNIEXPORT void JNICALL Java_com_tightdb_Table_nativeInsertString(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex, jstring value)
{
    if (!TBL_AND_INDEX_AND_TYPE_INSERT_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex, type_String))
        return;

    JStringAccessor value2(env, value);
    if (!value2)
        return;
    TBL(nativeTablePtr)->insert_string( S(columnIndex), S(rowIndex), value2);
}

JNIEXPORT void JNICALL Java_com_tightdb_Table_nativeInsertMixed(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex, jobject jMixedValue)
{
    if (!TBL_AND_INDEX_INSERT_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex)) 
        return;

    tbl_nativeDoMixed(&Table::insert_mixed, TBL(nativeTablePtr), env, columnIndex, rowIndex, jMixedValue);
}

JNIEXPORT void JNICALL Java_com_tightdb_Table_nativeSetMixed(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex, jobject jMixedValue)
{
    if (!TBL_AND_INDEX_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex)) 
        return;

    tbl_nativeDoMixed(&Table::set_mixed, TBL(nativeTablePtr), env, columnIndex, rowIndex, jMixedValue);
}

JNIEXPORT void JNICALL Java_com_tightdb_Table_nativeInsertSubTable(
    JNIEnv* env, jobject jTable, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex)
{
    if (!TBL_AND_INDEX_AND_TYPE_INSERT_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex, type_Table))
        return;

    TR((env, "nativeInsertSubTable(jTable:%x, nativeTablePtr: %x, colIdx: %lld, rowIdx: %lld)\n",
       jTable, nativeTablePtr,  columnIndex, rowIndex));
    TBL(nativeTablePtr)->insert_subtable( S(columnIndex), S(rowIndex));
}

JNIEXPORT void JNICALL Java_com_tightdb_Table_nativeInsertDone(
    JNIEnv* env, jobject, jlong nativeTablePtr)
{
    if (!TABLE_VALID(env, TBL(nativeTablePtr)))
        return;

    TBL(nativeTablePtr)->insert_done();
}


JNIEXPORT jlong JNICALL Java_com_tightdb_Table_nativeGetLong(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex)
{
    if (!TBL_AND_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex, type_Int))
        return 0;

    return TBL(nativeTablePtr)->get_int( S(columnIndex), S(rowIndex));
}

JNIEXPORT jboolean JNICALL Java_com_tightdb_Table_nativeGetBoolean(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex)
{
    if (!TBL_AND_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex, type_Bool))
        return false;

    return TBL(nativeTablePtr)->get_bool( S(columnIndex), S(rowIndex));
}

JNIEXPORT jfloat JNICALL Java_com_tightdb_Table_nativeGetFloat(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex)
{
    if (!TBL_AND_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex, type_Float))
        return 0;

    return TBL(nativeTablePtr)->get_float( S(columnIndex), S(rowIndex));
}

JNIEXPORT jdouble JNICALL Java_com_tightdb_Table_nativeGetDouble(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex)
{
    if (!TBL_AND_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex, type_Double))
        return 0;

    return TBL(nativeTablePtr)->get_double( S(columnIndex), S(rowIndex));
}

JNIEXPORT jlong JNICALL Java_com_tightdb_Table_nativeGetDateTime(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex)
{
    if (!TBL_AND_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex, type_Date))
        return 0;

    return TBL(nativeTablePtr)->get_date( S(columnIndex), S(rowIndex)).get_date();
}

JNIEXPORT jstring JNICALL Java_com_tightdb_Table_nativeGetString(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex)
{
    if (!TBL_AND_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex, type_String))
        return NULL;

    return to_jstring(env, TBL(nativeTablePtr)->get_string( S(columnIndex), S(rowIndex)));
}

/*
JNIEXPORT jobject JNICALL Java_com_tightdb_Table_nativeGetByteBuffer(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex)
{
    if (!TBL_AND_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex, type_Binary))
        return NULL;

    BinaryData bin = TBL(nativeTablePtr)->get_binary( S(columnIndex), S(rowIndex));
    return env->NewDirectByteBuffer(const_cast<char*>(bin.data()), bin.size());
}
*/

JNIEXPORT jbyteArray JNICALL Java_com_tightdb_Table_nativeGetByteArray(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex)
{
    if (!TBL_AND_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex, type_Binary))
        return NULL;

    return tbl_GetByteArray<Table>(env, nativeTablePtr, columnIndex, rowIndex);
}

JNIEXPORT jint JNICALL Java_com_tightdb_Table_nativeGetMixedType(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex)
{
    if (!TBL_AND_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex, type_Mixed))
        return 0;

    DataType mixedType = TBL(nativeTablePtr)->get_mixed_type( S(columnIndex), S(rowIndex));
    return static_cast<jint>(mixedType);
}

JNIEXPORT jobject JNICALL Java_com_tightdb_Table_nativeGetMixed(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex)
{
    if (!TBL_AND_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex, type_Mixed))
        return NULL;

    Mixed value = TBL(nativeTablePtr)->get_mixed( S(columnIndex), S(rowIndex));
    return CreateJMixedFromMixed(env, value);
}

JNIEXPORT jlong JNICALL Java_com_tightdb_Table_nativeGetSubTable(
    JNIEnv* env, jobject jTableBase, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex)
{
    if (!TBL_AND_INDEX_AND_TYPE_VALID_MIXED(env, TBL(nativeTablePtr), columnIndex, rowIndex, type_Table)) 
        return 0;

    Table* pSubTable = static_cast<Table*>(LangBindHelper::get_subtable_ptr(TBL(nativeTablePtr),
        S(columnIndex), S(rowIndex)));
    TR((env, "nativeGetSubTable(jTableBase:%x, nativeTablePtr: %x, colIdx: %lld, rowIdx: %lld) : %x\n",
        jTableBase, nativeTablePtr, columnIndex, rowIndex, pSubTable));
    return (jlong)pSubTable;
}

JNIEXPORT jlong JNICALL Java_com_tightdb_Table_nativeGetSubTableDuringInsert(
    JNIEnv* env, jobject jTableBase, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex)
{
    if (!TBL_AND_INDEX_AND_TYPE_INSERT_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex, type_Table))
        return 0;

    Table* pSubTable = static_cast<Table*>(LangBindHelper::get_subtable_ptr_during_insert(
        TBL(nativeTablePtr), S(columnIndex), S(rowIndex)));
    TR((env, "nativeGetSubTableDuringInsert(jTableBase:%x, nativeTablePtr: %x, colIdx: %lld, rowIdx: %lld) : %x\n",
        jTableBase, nativeTablePtr, columnIndex, rowIndex, pSubTable));
    return (jlong)pSubTable;
}

JNIEXPORT jlong JNICALL Java_com_tightdb_Table_nativeGetSubTableSize(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex)
{
    if (!TBL_AND_INDEX_AND_TYPE_VALID_MIXED(env, TBL(nativeTablePtr), columnIndex, rowIndex, type_Table)) 
        return 0;

    return TBL(nativeTablePtr)->get_subtable_size( S(columnIndex), S(rowIndex));
}


JNIEXPORT void JNICALL Java_com_tightdb_Table_nativeSetLong(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex, jlong value)
{
    if (!TBL_AND_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex, type_Int))
        return;

    return TBL(nativeTablePtr)->set_int( S(columnIndex), S(rowIndex), value);
}

JNIEXPORT void JNICALL Java_com_tightdb_Table_nativeSetBoolean(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex, jboolean value)
{
    if (!TBL_AND_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex, type_Bool)) 
        return;

    return TBL(nativeTablePtr)->set_bool( S(columnIndex), S(rowIndex), value == JNI_TRUE ? true : false);
}

JNIEXPORT void JNICALL Java_com_tightdb_Table_nativeSetFloat(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex, jfloat value)
{
    if (!TBL_AND_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex, type_Float))
        return;

    return TBL(nativeTablePtr)->set_float( S(columnIndex), S(rowIndex), value);
}

JNIEXPORT void JNICALL Java_com_tightdb_Table_nativeSetDouble(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex, jdouble value)
{
    if (!TBL_AND_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex, type_Double))
        return;

    return TBL(nativeTablePtr)->set_double( S(columnIndex), S(rowIndex), value);
}

JNIEXPORT void JNICALL Java_com_tightdb_Table_nativeSetString(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex, jstring value)
{
    if (!TBL_AND_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex, type_String))
        return;

    JStringAccessor value2(env, value);
    if (value2) {
        TBL(nativeTablePtr)->set_string( S(columnIndex), S(rowIndex), value2);
    }
}

JNIEXPORT void JNICALL Java_com_tightdb_Table_nativeSetDate(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex, jlong dateTimeValue)
{
    if (!TBL_AND_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex, type_Date))
        return;

    TBL(nativeTablePtr)->set_date( S(columnIndex), S(rowIndex), dateTimeValue);
}

/*
JNIEXPORT void JNICALL Java_com_tightdb_Table_nativeSetByteBuffer(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex, jobject byteBuffer)
{
    if (!TBL_AND_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex, type_Binary))
        return;

    tbl_nativeDoBinary(&Table::set_binary, TBL(nativeTablePtr), env, columnIndex, rowIndex, byteBuffer);
}
*/
/*
JNIEXPORT void JNICALL Java_com_tightdb_Table_nativeInsertByteBuffer(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex, jobject byteBuffer)
{
    if (!TBL_AND_INDEX_AND_TYPE_INSERT_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex, type_Binary))
        return;

    tbl_nativeDoBinary(&Table::insert_binary, TBL(nativeTablePtr), env, columnIndex, rowIndex, byteBuffer);
}
*/

JNIEXPORT void JNICALL Java_com_tightdb_Table_nativeSetByteArray(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex, jbyteArray dataArray)
{
    if (!TBL_AND_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex, type_Binary))
        return;

    tbl_nativeDoByteArray(&Table::set_binary, TBL(nativeTablePtr), env, columnIndex, rowIndex, dataArray);
}

JNIEXPORT void JNICALL Java_com_tightdb_Table_nativeInsertByteArray(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex, jbyteArray dataArray)
{
    if (!TBL_AND_INDEX_AND_TYPE_INSERT_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex, type_Binary))
        return;

    tbl_nativeDoByteArray(&Table::insert_binary, TBL(nativeTablePtr), env, columnIndex, rowIndex, dataArray);
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
    TBL(nativeTablePtr)->add_int( S(columnIndex), value);
}


JNIEXPORT void JNICALL Java_com_tightdb_Table_nativeClearSubTable(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex)
{
    if (!TBL_AND_INDEX_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex)) 
        return;

    TBL(nativeTablePtr)->clear_subtable( S(columnIndex), S(rowIndex));
}

// Indexing methods:

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
    pTable->set_index( S(columnIndex));
}

JNIEXPORT jboolean JNICALL Java_com_tightdb_Table_nativeHasIndex(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex)
{
    if (!TBL_AND_COL_INDEX_VALID(env, TBL(nativeTablePtr), columnIndex)) 
        return false;

    return TBL(nativeTablePtr)->has_index( S(columnIndex));
}

// Aggregare methods for integers

JNIEXPORT jlong JNICALL Java_com_tightdb_Table_nativeSum(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, type_Int)) 
        return 0;

    return TBL(nativeTablePtr)->sum( S(columnIndex));
}

JNIEXPORT jlong JNICALL Java_com_tightdb_Table_nativeMaximum(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, type_Int))
        return 0;

    return TBL(nativeTablePtr)->maximum( S(columnIndex));
}

JNIEXPORT jlong JNICALL Java_com_tightdb_Table_nativeMinimum(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, type_Int))
        return 0;

    return TBL(nativeTablePtr)->minimum( S(columnIndex));
}

JNIEXPORT jdouble JNICALL Java_com_tightdb_Table_nativeAverage(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, type_Int))
        return 0;
    return TBL(nativeTablePtr)->average( S(columnIndex));
}

// Aggregare methods for float

JNIEXPORT jdouble JNICALL Java_com_tightdb_Table_nativeSumFloat(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, type_Float))
        return 0;

    return TBL(nativeTablePtr)->sum_float( S(columnIndex));
}

JNIEXPORT jfloat JNICALL Java_com_tightdb_Table_nativeMaximumFloat(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, type_Float))
        return 0;

    return TBL(nativeTablePtr)->maximum_float( S(columnIndex));
}

JNIEXPORT jfloat JNICALL Java_com_tightdb_Table_nativeMinimumFloat(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, type_Float))
        return 0;

    return TBL(nativeTablePtr)->minimum_float( S(columnIndex));
}

JNIEXPORT jdouble JNICALL Java_com_tightdb_Table_nativeAverageFloat(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, type_Float))
        return 0;

    return TBL(nativeTablePtr)->average_float( S(columnIndex));
}


// Aggregare methods for double

JNIEXPORT jdouble JNICALL Java_com_tightdb_Table_nativeSumDouble(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, type_Double))
        return 0;

    return TBL(nativeTablePtr)->sum_double( S(columnIndex));
}

JNIEXPORT jdouble JNICALL Java_com_tightdb_Table_nativeMaximumDouble(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, type_Double))
        return 0;

    return TBL(nativeTablePtr)->maximum_double( S(columnIndex));
}

JNIEXPORT jdouble JNICALL Java_com_tightdb_Table_nativeMinimumDouble(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, type_Double))
        return 0;

    return TBL(nativeTablePtr)->minimum_double( S(columnIndex));
}

JNIEXPORT jdouble JNICALL Java_com_tightdb_Table_nativeAverageDouble(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, type_Double))
        return 0;

    return TBL(nativeTablePtr)->average_double( S(columnIndex));
}

// Count

JNIEXPORT jlong JNICALL Java_com_tightdb_Table_nativeCountLong(
    JNIEnv *env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong value)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, type_Int))
        return 0;

    return TBL(nativeTablePtr)->count_int( S(columnIndex), value);
}

JNIEXPORT jlong JNICALL Java_com_tightdb_Table_nativeCountFloat(
    JNIEnv *env, jobject, jlong nativeTablePtr, jlong columnIndex, jfloat value)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, type_Float))
        return 0;

    return TBL(nativeTablePtr)->count_float( S(columnIndex), value);
}

JNIEXPORT jlong JNICALL Java_com_tightdb_Table_nativeCountDouble(
    JNIEnv *env, jobject, jlong nativeTablePtr, jlong columnIndex, jdouble value)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, type_Double))
        return 0;

    return TBL(nativeTablePtr)->count_double( S(columnIndex), value);
}

JNIEXPORT jlong JNICALL Java_com_tightdb_Table_nativeCountString(
    JNIEnv *env, jobject, jlong nativeTablePtr, jlong columnIndex, jstring value)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, type_String))
        return 0;

    JStringAccessor value2(env, value);
    if (!value2)
        return 0;
    return TBL(nativeTablePtr)->count_string( S(columnIndex), value2);
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
    return TBL(nativeTablePtr)->lookup(value2);
}


//

JNIEXPORT jlong JNICALL Java_com_tightdb_Table_nativeWhere(
    JNIEnv *env, jobject, jlong nativeTablePtr)
{
    if (!TABLE_VALID(env, TBL(nativeTablePtr))) return 0;

    Query query = TBL(nativeTablePtr)->where();
    Query* queryPtr = new Query(query);
    return reinterpret_cast<jlong>(queryPtr);
}

// FindFirst

JNIEXPORT jlong JNICALL Java_com_tightdb_Table_nativeFindFirstInt(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong value)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, type_Int))
        return 0;

    return TBL(nativeTablePtr)->find_first_int( S(columnIndex), value);
}

JNIEXPORT jlong JNICALL Java_com_tightdb_Table_nativeFindFirstBool(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jboolean value)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, type_Bool))
        return 0;

    return TBL(nativeTablePtr)->find_first_bool( S(columnIndex), value != 0 ? true : false);
}

JNIEXPORT jlong JNICALL Java_com_tightdb_Table_nativeFindFirstFloat(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jfloat value)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, type_Float))
        return 0;

    return TBL(nativeTablePtr)->find_first_float( S(columnIndex), value);
}

JNIEXPORT jlong JNICALL Java_com_tightdb_Table_nativeFindFirstDouble(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jdouble value)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, type_Double))
        return 0;

    return TBL(nativeTablePtr)->find_first_double( S(columnIndex), value);
}

JNIEXPORT jlong JNICALL Java_com_tightdb_Table_nativeFindFirstDate(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong dateTimeValue)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, type_Date))
        return 0;

    return TBL(nativeTablePtr)->find_first_date( S(columnIndex), (time_t)dateTimeValue);
}

JNIEXPORT jlong JNICALL Java_com_tightdb_Table_nativeFindFirstString(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jstring value)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, type_String))
        return 0;

    JStringAccessor value2(env, value);
    if (!value2)
        return 0;

    jlong result = TBL(nativeTablePtr)->find_first_string( S(columnIndex), value2);
    return result;
}

// FindAll

JNIEXPORT jlong JNICALL Java_com_tightdb_Table_nativeFindAllInt(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong value)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, type_Int))
        return 0;

    TableView* pTableView = new TableView( TBL(nativeTablePtr)->find_all_int( S(columnIndex), value) );
    return reinterpret_cast<jlong>(pTableView);
}

JNIEXPORT jlong JNICALL Java_com_tightdb_Table_nativeFindAllFloat(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jfloat value)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, type_Float))
        return 0;

    TableView* pTableView = new TableView( TBL(nativeTablePtr)->find_all_float( S(columnIndex), value) );
    return reinterpret_cast<jlong>(pTableView);
}

JNIEXPORT jlong JNICALL Java_com_tightdb_Table_nativeFindAllDouble(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jdouble value)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, type_Double))
        return 0;

    TableView* pTableView = new TableView( TBL(nativeTablePtr)->find_all_double( S(columnIndex), value) );
    return reinterpret_cast<jlong>(pTableView);
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
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, type_Date))
        return 0;

    TableView* pTableView = new TableView( TBL(nativeTablePtr)->find_all_date( S(columnIndex),
                                           static_cast<time_t>(dateTimeValue)) );
    return reinterpret_cast<jlong>(pTableView);
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

    TableView* pTableView = new TableView( pTable->find_all_string( S(columnIndex), value2) );
    return reinterpret_cast<jlong>(pTableView);
}


// experimental
JNIEXPORT jlong JNICALL Java_com_tightdb_Table_nativeLowerBoundInt(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong value)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, type_Int))
        return 0;

    Table* pTable = TBL(nativeTablePtr);
    return pTable->lower_bound_int(S(columnIndex), S(value));
}


// experimental
JNIEXPORT jlong JNICALL Java_com_tightdb_Table_nativeUpperBoundInt(
    JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong value)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, type_Int))
        return 0;

    Table* pTable = TBL(nativeTablePtr);
    return pTable->upper_bound_int(S(columnIndex), S(value));
}

//

JNIEXPORT jlong JNICALL Java_com_tightdb_Table_nativeDistinct(
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
    TableView* pTableView = new TableView( pTable->distinct(S(columnIndex)) );
    return reinterpret_cast<jlong>(pTableView);
}

JNIEXPORT void JNICALL Java_com_tightdb_Table_nativeOptimize(
    JNIEnv* env, jobject, jlong nativeTablePtr)
{
    if (!TABLE_VALID(env, TBL(nativeTablePtr))) return;

    TBL(nativeTablePtr)->optimize();
}

JNIEXPORT jstring JNICALL Java_com_tightdb_Table_nativeToJson(
    JNIEnv *env, jobject, jlong nativeTablePtr)
{
   Table* table = TBL(nativeTablePtr);
   if (!TABLE_VALID(env, table)) return NULL;

   // Write table to string in JSON format
   std::ostringstream ss;
   ss.sync_with_stdio(false); // for performance
   table->to_json(ss);
   const std::string str = ss.str();
   return env->NewStringUTF(str.c_str());
}

JNIEXPORT jstring JNICALL Java_com_tightdb_Table_nativeToString(
    JNIEnv *env, jobject, jlong nativeTablePtr, jlong maxRows)
{
   Table* table = TBL(nativeTablePtr);
   if (!TABLE_VALID(env, table)) return NULL;

   std::ostringstream ss;
   table->to_string(ss, maxRows);
   const std::string str = ss.str();
   return env->NewStringUTF(str.c_str());
}

JNIEXPORT jstring JNICALL Java_com_tightdb_Table_nativeRowToString(
    JNIEnv *env, jobject, jlong nativeTablePtr, jlong rowIndex)
{
   Table* table = TBL(nativeTablePtr);
   if (!TBL_AND_ROW_INDEX_VALID(env, table, rowIndex)) return NULL;

   std::ostringstream ss;
   table->row_to_string(rowIndex, ss);
   const std::string str = ss.str();
   return env->NewStringUTF(str.c_str());
}

JNIEXPORT jboolean JNICALL Java_com_tightdb_Table_nativeIsValid(
    JNIEnv*, jobject, jlong nativeTablePtr)
{
    return TBL(nativeTablePtr)->is_attached();
}

JNIEXPORT void JNICALL Java_com_tightdb_Table_nativeClose(
    JNIEnv* env, jobject jTable, jlong nativeTablePtr)
{
    if (!TABLE_VALID(env, TBL(nativeTablePtr))) return;

    TR((env, "nativeClose(jTable: %x, nativeTablePtr: %x)\n", jTable, nativeTablePtr));
    LangBindHelper::unbind_table_ref(TBL(nativeTablePtr));
}

JNIEXPORT jlong JNICALL Java_com_tightdb_Table_createNative(JNIEnv* env, jobject jTable)
{
    TR((env, "CreateNative(jTable: %x)\n", jTable));
    return reinterpret_cast<jlong>(LangBindHelper::new_table()); // FIXME: May throw
}
