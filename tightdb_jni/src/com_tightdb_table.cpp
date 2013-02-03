#include "util.h"
#include "mixedutil.h"
#include "com_tightdb_Table.h"
#include "columntypeutil.h"
#include "TableSpecUtil.h"
#include "java_lang_List_Util.h"
#include "mixedutil.h"
#include "tablebase_tpl.hpp"

#include <sstream>

using namespace tightdb;


JNIEXPORT void JNICALL Java_com_tightdb_Table_nativeUpdateFromSpec(
	JNIEnv* env, jobject, jlong nativeTablePtr, jobject jTableSpec)
{
    if (!TABLE_VALID(env, TBL(nativeTablePtr))) return;

	Table* pTable = TBL(nativeTablePtr);
    TR((env, "nativeUpdateFromSpec(tblPtr %x, spec %x)\n", pTable, jTableSpec));
    Spec& spec = pTable->get_spec();
	updateSpecFromJSpec(env, spec, jTableSpec);
	pTable->update_from_spec();
}

JNIEXPORT jlong JNICALL Java_com_tightdb_Table_nativeSize(
	JNIEnv* env, jobject, jlong nativeTablePtr)
{
    if (!TABLE_VALID(env, TBL(nativeTablePtr))) return 0;
    return TBL(nativeTablePtr)->size();
}

JNIEXPORT void JNICALL Java_com_tightdb_Table_nativeClear(
	JNIEnv* env, jobject, jlong nativeTablePtr)
{
    if (!TABLE_VALID(env, TBL(nativeTablePtr))) return;
	TBL(nativeTablePtr)->clear();
}

JNIEXPORT jlong JNICALL Java_com_tightdb_Table_nativeGetColumnCount(
	JNIEnv* env, jobject, jlong nativeTablePtr)
{
    if (!TABLE_VALID(env, TBL(nativeTablePtr))) return 0;
	return TBL(nativeTablePtr)->get_column_count();
}

JNIEXPORT jstring JNICALL Java_com_tightdb_Table_nativeGetColumnName(
	JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex)
{
    if (!COL_INDEX_VALID(env, TBL(nativeTablePtr), columnIndex)) return NULL;
	return env->NewStringUTF( TBL(nativeTablePtr)->get_column_name( S(columnIndex)) );
}

JNIEXPORT jobject JNICALL Java_com_tightdb_Table_nativeGetTableSpec(
	JNIEnv* env, jobject, jlong nativeTablePtr)
{
    if (!TABLE_VALID(env, TBL(nativeTablePtr))) return 0;

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
    if (!COL_INDEX_VALID(env, TBL(nativeTablePtr), columnIndex)) return 0;

    return static_cast<int>( TBL(nativeTablePtr)->get_column_type( S(columnIndex)) );
}

JNIEXPORT jlong JNICALL Java_com_tightdb_Table_nativeAddEmptyRow(
	JNIEnv* env, jobject, jlong nativeTablePtr, jlong rows)
{
    if (!TABLE_VALID(env, TBL(nativeTablePtr))) return 0;

    return static_cast<jlong>( TBL(nativeTablePtr)->add_empty_row( S(rows)) );
}

JNIEXPORT void JNICALL Java_com_tightdb_Table_nativeRemove(
	JNIEnv* env, jobject, jlong nativeTablePtr, jlong rowIndex)
{
    if (!ROW_INDEX_VALID(env, TBL(nativeTablePtr), rowIndex)) return;

    TBL(nativeTablePtr)->remove(S(rowIndex));
}

JNIEXPORT void JNICALL Java_com_tightdb_Table_nativeRemoveLast(
	JNIEnv* env, jobject, jlong nativeTablePtr)
{
    if (!TABLE_VALID(env, TBL(nativeTablePtr))) return;

    TBL(nativeTablePtr)->remove_last();
}

JNIEXPORT void JNICALL Java_com_tightdb_Table_nativeInsertLong(
	JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex, jlong value)
{
	if (!INDEX_INSERT_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex)) return;
//TODO??? check type. Also in set*()

    TBL(nativeTablePtr)->insert_int( S(columnIndex), S(rowIndex), value);
}

JNIEXPORT void JNICALL Java_com_tightdb_Table_nativeInsertBoolean(
	JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex, jboolean value)
{
	if (!INDEX_INSERT_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex)) return;
    
    TBL(nativeTablePtr)->insert_bool( S(columnIndex), S(rowIndex), value != 0 ? true : false);
}

JNIEXPORT void JNICALL Java_com_tightdb_Table_nativeInsertFloat(
	JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex, jfloat value)
{
	if (!INDEX_INSERT_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex)) return;
    
    TBL(nativeTablePtr)->insert_float( S(columnIndex), S(rowIndex), value);
}

JNIEXPORT void JNICALL Java_com_tightdb_Table_nativeInsertDouble(
	JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex, jdouble value)
{
	if (!INDEX_INSERT_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex)) return;
    
    TBL(nativeTablePtr)->insert_double( S(columnIndex), S(rowIndex), value);
}

JNIEXPORT void JNICALL Java_com_tightdb_Table_nativeInsertDate(
	JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex, jlong dateTimeValue)
{
	if (!INDEX_INSERT_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex)) return;

    TBL(nativeTablePtr)->insert_date( S(columnIndex), S(rowIndex), static_cast<time_t>(dateTimeValue));
}

JNIEXPORT void JNICALL Java_com_tightdb_Table_nativeInsertString(
	JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex, jstring value)
{
	if (!INDEX_INSERT_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex)) return;

    const char* valueCharPtr = env->GetStringUTFChars(value, NULL);
    if (!valueCharPtr) 
        return;
	TBL(nativeTablePtr)->insert_string( S(columnIndex), S(rowIndex), valueCharPtr);
	env->ReleaseStringUTFChars(value, valueCharPtr);
}

JNIEXPORT void JNICALL Java_com_tightdb_Table_nativeInsertMixed(
	JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex, jobject jMixedValue)
{ 
    if (!INDEX_INSERT_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex)) return;

    tbl_nativeDoMixed(&Table::insert_mixed, TBL(nativeTablePtr), env, columnIndex, rowIndex, jMixedValue);
}

JNIEXPORT void JNICALL Java_com_tightdb_Table_nativeSetMixed(
	JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex, jobject jMixedValue)
{ 
    if (!INDEX_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex)) return;

    tbl_nativeDoMixed(&Table::set_mixed, TBL(nativeTablePtr), env, columnIndex, rowIndex, jMixedValue);
}

JNIEXPORT void JNICALL Java_com_tightdb_Table_nativeInsertSubTable(
	JNIEnv* env, jobject jTable, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex)
{
	if (!INDEX_INSERT_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex)) return;

    TR((env, "nativeInsertSubTable(jTable:%x, nativeTablePtr: %x, colIdx: %lld, rowIdx: %lld)\n",
       jTable, nativeTablePtr,  columnIndex, rowIndex));
	TBL(nativeTablePtr)->insert_subtable( S(columnIndex), S(rowIndex));
}

JNIEXPORT void JNICALL Java_com_tightdb_Table_nativeInsertDone(
	JNIEnv* env, jobject, jlong nativeTablePtr)
{
    if (!TABLE_VALID(env, TBL(nativeTablePtr))) return;
    
    TBL(nativeTablePtr)->insert_done();
}


JNIEXPORT jlong JNICALL Java_com_tightdb_Table_nativeGetLong(
	JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex)
{
	if (!INDEX_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex)) return 0;

    return TBL(nativeTablePtr)->get_int( S(columnIndex), S(rowIndex));
}

JNIEXPORT jboolean JNICALL Java_com_tightdb_Table_nativeGetBoolean(
	JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex)
{
	if (!INDEX_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex)) return false;

    return TBL(nativeTablePtr)->get_bool( S(columnIndex), S(rowIndex));
}

JNIEXPORT jfloat JNICALL Java_com_tightdb_Table_nativeGetFloat(
	JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex)
{
	if (!INDEX_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex)) return 0;

    return TBL(nativeTablePtr)->get_float( S(columnIndex), S(rowIndex));
}

JNIEXPORT jdouble JNICALL Java_com_tightdb_Table_nativeGetDouble(
	JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex)
{
	if (!INDEX_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex)) return 0;

    return TBL(nativeTablePtr)->get_double( S(columnIndex), S(rowIndex));
}

JNIEXPORT jlong JNICALL Java_com_tightdb_Table_nativeGetDateTime(
	JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex)
{
	if (!INDEX_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex)) return 0;

    return TBL(nativeTablePtr)->get_date( S(columnIndex), S(rowIndex));
}

JNIEXPORT jstring JNICALL Java_com_tightdb_Table_nativeGetString(
	JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex)
{
	if (!INDEX_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex)) return NULL;

	const char* valueCharPtr = TBL(nativeTablePtr)->get_string( S(columnIndex), S(rowIndex));
	return env->NewStringUTF(valueCharPtr);
}

JNIEXPORT jobject JNICALL Java_com_tightdb_Table_nativeGetByteBuffer(
	JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex)
{
	if (!INDEX_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex)) return NULL;
    
    BinaryData data = TBL(nativeTablePtr)->get_binary( S(columnIndex), S(rowIndex));
	return env->NewDirectByteBuffer((void*)data.pointer, data.len);
}

JNIEXPORT jbyteArray JNICALL Java_com_tightdb_Table_nativeGetByteArray(
	JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex)
{
	if (!INDEX_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex)) return NULL;

    return tbl_GetByteArray<Table>(env, nativeTablePtr, columnIndex, rowIndex);
}

JNIEXPORT jint JNICALL Java_com_tightdb_Table_nativeGetMixedType(
	JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex)
{
	if (!INDEX_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex)) return 0;

	ColumnType mixedType = TBL(nativeTablePtr)->get_mixed_type( S(columnIndex), S(rowIndex));
	return static_cast<jint>(mixedType);
}

JNIEXPORT jobject JNICALL Java_com_tightdb_Table_nativeGetMixed(
	JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex)
{
    if (!INDEX_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex)) return NULL;

	Mixed value = TBL(nativeTablePtr)->get_mixed( S(columnIndex), S(rowIndex));
	return CreateJMixedFromMixed(env, value);
}

JNIEXPORT jlong JNICALL Java_com_tightdb_Table_nativeGetSubTable(
	JNIEnv* env, jobject jTableBase, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex)
{
    if (!INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex, COLUMN_TYPE_TABLE)) return 0;

	Table* pSubTable = static_cast<Table*>(LangBindHelper::get_subtable_ptr(TBL(nativeTablePtr), 
        S(columnIndex), S(rowIndex)));
    TR((env, "nativeGetSubTable(jTableBase:%x, nativeTablePtr: %x, colIdx: %lld, rowIdx: %lld) : %x\n",
        jTableBase, nativeTablePtr, columnIndex, rowIndex, pSubTable));
    return (jlong)pSubTable;
}

JNIEXPORT jlong JNICALL Java_com_tightdb_Table_nativeGetSubTableDuringInsert(
	JNIEnv* env, jobject jTableBase, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex)
{
    if (!INDEX_AND_TYPE_INSERT_VALID(env, TBL(nativeTablePtr), columnIndex, 
         rowIndex, COLUMN_TYPE_TABLE)) return 0;

	Table* pSubTable = static_cast<Table*>(LangBindHelper::get_subtable_ptr_during_insert(
        TBL(nativeTablePtr), S(columnIndex), S(rowIndex)));
    TR((env, "nativeGetSubTableDuringInsert(jTableBase:%x, nativeTablePtr: %x, colIdx: %lld, rowIdx: %lld) : %x\n",
        jTableBase, nativeTablePtr, columnIndex, rowIndex, pSubTable));
    return (jlong)pSubTable;
}

JNIEXPORT jlong JNICALL Java_com_tightdb_Table_nativeGetSubTableSize(
	JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex)
{
    if (!INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex, COLUMN_TYPE_TABLE)) return 0;

    return TBL(nativeTablePtr)->get_subtable_size( S(columnIndex), S(rowIndex));
}


JNIEXPORT void JNICALL Java_com_tightdb_Table_nativeSetLong(
	JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex, jlong value)
{
    if (!INDEX_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex)) return;

	return TBL(nativeTablePtr)->set_int( S(columnIndex), S(rowIndex), value);
}

JNIEXPORT void JNICALL Java_com_tightdb_Table_nativeSetBoolean(
	JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex, jboolean value)
{
   	if (!INDEX_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex)) return;

    return TBL(nativeTablePtr)->set_bool( S(columnIndex), S(rowIndex), value == JNI_TRUE ? true : false);
}

JNIEXPORT void JNICALL Java_com_tightdb_Table_nativeSetFloat(
	JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex, jfloat value)
{
    if (!INDEX_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex)) return;

	return TBL(nativeTablePtr)->set_float( S(columnIndex), S(rowIndex), value);
}

JNIEXPORT void JNICALL Java_com_tightdb_Table_nativeSetDouble(
	JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex, jdouble value)
{
    if (!INDEX_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex)) return;

	return TBL(nativeTablePtr)->set_double( S(columnIndex), S(rowIndex), value);
}

JNIEXPORT void JNICALL Java_com_tightdb_Table_nativeSetString(
	JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex, jstring value)
{
    if (!INDEX_AND_TYPE_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex, COLUMN_TYPE_STRING)) return;

    const char* valueCharPtr = env->GetStringUTFChars(value, NULL);
    if (valueCharPtr) {
	    TBL(nativeTablePtr)->set_string( S(columnIndex), S(rowIndex), valueCharPtr);
	    env->ReleaseStringUTFChars(value, valueCharPtr);
    }
}

JNIEXPORT void JNICALL Java_com_tightdb_Table_nativeSetDate(
	JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex, jlong dateTimeValue)
{
   	if (!INDEX_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex)) return;

    TBL(nativeTablePtr)->set_date( S(columnIndex), S(rowIndex), dateTimeValue);
}

JNIEXPORT void JNICALL Java_com_tightdb_Table_nativeSetByteBuffer(
	JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex, jobject byteBuffer)
{
    if (!INDEX_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex)) return;

    tbl_nativeDoBinary(&Table::set_binary, TBL(nativeTablePtr), env, columnIndex, rowIndex, byteBuffer);
}

JNIEXPORT void JNICALL Java_com_tightdb_Table_nativeInsertByteBuffer(
	JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex, jobject byteBuffer)
{
    if (!INDEX_INSERT_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex)) return;

    tbl_nativeDoBinary(&Table::insert_binary, TBL(nativeTablePtr), env, columnIndex, rowIndex, byteBuffer);
}

JNIEXPORT void JNICALL Java_com_tightdb_Table_nativeSetByteArray(
	JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex, jbyteArray dataArray)
{
    if (!INDEX_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex)) return;

    tbl_nativeDoByteArray(&Table::set_binary, TBL(nativeTablePtr), env, columnIndex, rowIndex, dataArray);
}

JNIEXPORT void JNICALL Java_com_tightdb_Table_nativeInsertByteArray(
	JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex, jbyteArray dataArray)
{
    if (!INDEX_INSERT_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex)) return;

    tbl_nativeDoByteArray(&Table::insert_binary, TBL(nativeTablePtr), env, columnIndex, rowIndex, dataArray);
}

JNIEXPORT void JNICALL Java_com_tightdb_Table_nativeAddInt(
	JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong value)
{	
	if (!COL_INDEX_VALID(env, TBL(nativeTablePtr), columnIndex)) return;

    TBL(nativeTablePtr)->add_int( S(columnIndex), value);
}


JNIEXPORT void JNICALL Java_com_tightdb_Table_nativeClearSubTable(
	JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex)
{
   	if (!INDEX_VALID(env, TBL(nativeTablePtr), columnIndex, rowIndex)) return;

    TBL(nativeTablePtr)->clear_subtable( S(columnIndex), S(rowIndex));
}

// Indexing methods:

JNIEXPORT void JNICALL Java_com_tightdb_Table_nativeSetIndex(
	JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex)
{
    Table* pTable = TBL(nativeTablePtr);
   	if (!COL_INDEX_VALID(env, pTable, columnIndex)) 
        return;
    if (pTable->get_column_type(columnIndex) != COLUMN_TYPE_STRING) {
        ThrowException(env, IllegalArgument, "Invalid columntype - only string columns are supported.");
        return;
    }
    pTable->set_index( S(columnIndex));
}

JNIEXPORT jboolean JNICALL Java_com_tightdb_Table_nativeHasIndex(
	JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex)
{
   	if (!COL_INDEX_VALID(env, TBL(nativeTablePtr), columnIndex)) return false;

    return TBL(nativeTablePtr)->has_index( S(columnIndex));
}

// Aggregare methods for integers

JNIEXPORT jlong JNICALL Java_com_tightdb_Table_nativeSum(
	JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex)
{
   	if (!COL_INDEX_VALID(env, TBL(nativeTablePtr), columnIndex)) return 0;

    return TBL(nativeTablePtr)->sum( S(columnIndex));
}

JNIEXPORT jlong JNICALL Java_com_tightdb_Table_nativeMaximum(
	JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex)
{	
   	if (!COL_INDEX_VALID(env, TBL(nativeTablePtr), columnIndex)) return 0;

    return TBL(nativeTablePtr)->maximum( S(columnIndex));
}

JNIEXPORT jlong JNICALL Java_com_tightdb_Table_nativeMinimum(
	JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex)
{	
   	if (!COL_INDEX_VALID(env, TBL(nativeTablePtr), columnIndex)) return 0;

    return TBL(nativeTablePtr)->minimum( S(columnIndex));
}

JNIEXPORT jdouble JNICALL Java_com_tightdb_Table_nativeAverage(
	JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex)
{
   	if (!COL_INDEX_VALID(env, TBL(nativeTablePtr), columnIndex)) return 0;
    return TBL(nativeTablePtr)->average( S(columnIndex));
}

// Aggregare methods for float

JNIEXPORT jfloat JNICALL Java_com_tightdb_Table_nativeSumFloat(
	JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex)
{
   	if (!COL_INDEX_VALID(env, TBL(nativeTablePtr), columnIndex)) return 0;

    return TBL(nativeTablePtr)->sum_float( S(columnIndex));
}

JNIEXPORT jfloat JNICALL Java_com_tightdb_Table_nativeMaximumFloat(
	JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex)
{	
   	if (!COL_INDEX_VALID(env, TBL(nativeTablePtr), columnIndex)) return 0;

    return TBL(nativeTablePtr)->maximum_float( S(columnIndex));
}

JNIEXPORT jfloat JNICALL Java_com_tightdb_Table_nativeMinimumFloat(
	JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex)
{	
   	if (!COL_INDEX_VALID(env, TBL(nativeTablePtr), columnIndex)) return 0;

    return TBL(nativeTablePtr)->minimum_float( S(columnIndex));
}

JNIEXPORT jfloat JNICALL Java_com_tightdb_Table_nativeAverageFloat(
	JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex)
{
   	if (!COL_INDEX_VALID(env, TBL(nativeTablePtr), columnIndex)) return 0;
    return TBL(nativeTablePtr)->average_float( S(columnIndex));
}


// Aggregare methods for double

JNIEXPORT jdouble JNICALL Java_com_tightdb_Table_nativeSumDouble(
	JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex)
{
   	if (!COL_INDEX_VALID(env, TBL(nativeTablePtr), columnIndex)) return 0;

    return TBL(nativeTablePtr)->sum_double( S(columnIndex));
}

JNIEXPORT jdouble JNICALL Java_com_tightdb_Table_nativeMaximumDouble(
	JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex)
{	
   	if (!COL_INDEX_VALID(env, TBL(nativeTablePtr), columnIndex)) return 0;

    return TBL(nativeTablePtr)->maximum_double( S(columnIndex));
}

JNIEXPORT jdouble JNICALL Java_com_tightdb_Table_nativeMinimumDouble(
	JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex)
{	
   	if (!COL_INDEX_VALID(env, TBL(nativeTablePtr), columnIndex)) return 0;

    return TBL(nativeTablePtr)->minimum_double( S(columnIndex));
}

JNIEXPORT jdouble JNICALL Java_com_tightdb_Table_nativeAverageDouble(
	JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex)
{
   	if (!COL_INDEX_VALID(env, TBL(nativeTablePtr), columnIndex)) return 0;
    return TBL(nativeTablePtr)->average_double( S(columnIndex));
}

// 

JNIEXPORT jlong JNICALL Java_com_tightdb_Table_nativeWhere(
    JNIEnv *, jobject, jlong nativeTablePtr)
{
    Query query = TBL(nativeTablePtr)->where();
    Query* queryPtr = new Query(query);
    return reinterpret_cast<jlong>(queryPtr);
}

// FindFirst

JNIEXPORT jlong JNICALL Java_com_tightdb_Table_nativeFindFirstInt(
	JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong value)
{
   	if (!COL_INDEX_VALID(env, TBL(nativeTablePtr), columnIndex)) return 0;

	return TBL(nativeTablePtr)->find_first_int( S(columnIndex), value);
}

JNIEXPORT jlong JNICALL Java_com_tightdb_Table_nativeFindFirstBool(
	JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jboolean value)
{
   	if (!COL_INDEX_VALID(env, TBL(nativeTablePtr), columnIndex)) return false;

    return TBL(nativeTablePtr)->find_first_bool( S(columnIndex), value != 0 ? true : false);
}

JNIEXPORT jlong JNICALL Java_com_tightdb_Table_nativeFindFirstFloat(
	JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jfloat value)
{
   	if (!COL_INDEX_VALID(env, TBL(nativeTablePtr), columnIndex)) return 0;

	return TBL(nativeTablePtr)->find_first_float( S(columnIndex), value);
}

JNIEXPORT jlong JNICALL Java_com_tightdb_Table_nativeFindFirstDouble(
	JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jdouble value)
{
   	if (!COL_INDEX_VALID(env, TBL(nativeTablePtr), columnIndex)) return 0;

	return TBL(nativeTablePtr)->find_first_double( S(columnIndex), value);
}

JNIEXPORT jlong JNICALL Java_com_tightdb_Table_nativeFindFirstDate(
	JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong dateTimeValue)
{
   	if (!COL_INDEX_VALID(env, TBL(nativeTablePtr), columnIndex)) return 0;

	return TBL(nativeTablePtr)->find_first_date( S(columnIndex), (time_t)dateTimeValue);
}

JNIEXPORT jlong JNICALL Java_com_tightdb_Table_nativeFindFirstString(
	JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jstring value)
{
  	if (!COL_INDEX_VALID(env, TBL(nativeTablePtr), columnIndex)) return 0;

	const char* valueCharPtr = env->GetStringUTFChars(value, NULL);
    if (!valueCharPtr) 
        return 0;

	jlong result = TBL(nativeTablePtr)->find_first_string( S(columnIndex), valueCharPtr);
    env->ReleaseStringUTFChars(value, valueCharPtr);
	return result;
}

// FindAll

JNIEXPORT jlong JNICALL Java_com_tightdb_Table_nativeFindAllInt(
	JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong value)
{
  	if (!COL_INDEX_VALID(env, TBL(nativeTablePtr), columnIndex)) return 0;

	TableView* pTableView = new TableView( TBL(nativeTablePtr)->find_all_int( S(columnIndex), value) );
	return reinterpret_cast<jlong>(pTableView);
}

JNIEXPORT jlong JNICALL Java_com_tightdb_Table_nativeFindAllFloat(
	JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jfloat value)
{
  	if (!COL_INDEX_VALID(env, TBL(nativeTablePtr), columnIndex)) return 0;

	TableView* pTableView = new TableView( TBL(nativeTablePtr)->find_all_float( S(columnIndex), value) );
	return reinterpret_cast<jlong>(pTableView);
}

JNIEXPORT jlong JNICALL Java_com_tightdb_Table_nativeFindAllDouble(
	JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jdouble value)
{
  	if (!COL_INDEX_VALID(env, TBL(nativeTablePtr), columnIndex)) return 0;

	TableView* pTableView = new TableView( TBL(nativeTablePtr)->find_all_double( S(columnIndex), value) );
	return reinterpret_cast<jlong>(pTableView);
}

JNIEXPORT jlong JNICALL Java_com_tightdb_Table_nativeFindAllBool(
	JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jboolean value)
{
  	if (!COL_INDEX_VALID(env, TBL(nativeTablePtr), columnIndex)) return 0;

	TableView* pTableView = new TableView( TBL(nativeTablePtr)->find_all_bool( S(columnIndex), 
                                           value != 0 ? true : false) );
	return reinterpret_cast<jlong>(pTableView);
}

JNIEXPORT jlong JNICALL Java_com_tightdb_Table_nativeFindAllDate(
	JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong dateTimeValue)
{
  	if (!COL_INDEX_VALID(env, TBL(nativeTablePtr), columnIndex)) return 0;

	TableView* pTableView = new TableView( TBL(nativeTablePtr)->find_all_date( S(columnIndex), 
                                           static_cast<time_t>(dateTimeValue)) );
	return reinterpret_cast<jlong>(pTableView);
}

JNIEXPORT jlong JNICALL Java_com_tightdb_Table_nativeFindAllString(
	JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jstring value)
{
  	if (!COL_INDEX_VALID(env, TBL(nativeTablePtr), columnIndex)) return 0;

    Table* pTable = TBL(nativeTablePtr);
	const char* valueCharPtr = env->GetStringUTFChars(value, NULL);
    if (!valueCharPtr) 
        return 0;

	TableView* pTableView = new TableView( pTable->find_all_string( S(columnIndex), valueCharPtr) );
	return reinterpret_cast<jlong>(pTableView);
}

//

JNIEXPORT jlong JNICALL Java_com_tightdb_Table_nativeDistinct(
    JNIEnv *env, jobject, jlong nativeTablePtr, jlong columnIndex)
{
    Table* pTable = TBL(nativeTablePtr);
    if (!COL_INDEX_VALID(env, pTable, columnIndex)) 
        return 0;
    if (!pTable->has_index(columnIndex)) {
        ThrowException(env, UnsupportedOperation, "The column must be indexed before distinct() can be used.");
        return 0;
    }
    if (pTable->get_column_type(columnIndex) != COLUMN_TYPE_STRING) {
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
    return reinterpret_cast<jlong>(LangBindHelper::new_table());
}

JNIEXPORT jboolean JNICALL Java_com_tightdb_Table_nativeIsValid(
    JNIEnv*, jobject, jlong nativeTablePtr)
{
    return TBL(nativeTablePtr)->is_valid();
}
