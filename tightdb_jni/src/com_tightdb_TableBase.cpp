#include <jni.h>
#include <tightdb.hpp>
#include <tightdb/lang_bind_helper.hpp>

#include "util.h"
#include "mixedutil.h"
#include "com_tightdb_TableBase.h"
#include "ColumnTypeUtil.h"
#include "TableSpecUtil.h"
#include "java_lang_List_Util.h"
#include "mixedutil.h"

#include "tablebase_tpl.hpp"

using namespace tightdb;


JNIEXPORT void JNICALL Java_com_tightdb_TableBase_nativeUpdateFromSpec(
	JNIEnv* env, jobject jTable, jlong nativeTablePtr, jobject jTableSpec)
{
	Table* pTable = TBL(nativeTablePtr);
	Spec& spec = pTable->get_spec();
	updateSpecFromJSpec(env, spec, jTableSpec);
	pTable->update_from_spec();
}

JNIEXPORT jlong JNICALL Java_com_tightdb_TableBase_nativeSize(
	JNIEnv* env, jobject jTable, jlong nativeTablePtr)
{
	return TBL(nativeTablePtr)->size();
}

JNIEXPORT void JNICALL Java_com_tightdb_TableBase_nativeClear(
	JNIEnv* env, jobject jTable, jlong nativeTablePtr)
{
	TBL(nativeTablePtr)->clear();
}

JNIEXPORT jlong JNICALL Java_com_tightdb_TableBase_nativeGetColumnCount(
	JNIEnv* env, jobject jTable, jlong nativeTablePtr)
{
	return TBL(nativeTablePtr)->get_column_count();
}

JNIEXPORT jstring JNICALL Java_com_tightdb_TableBase_nativeGetColumnName(
	JNIEnv* env, jobject jTable, jlong nativeTablePtr, jlong columnIndex)
{
	return env->NewStringUTF( TBL(nativeTablePtr)->get_column_name( S(columnIndex)) );
}

JNIEXPORT jobject JNICALL Java_com_tightdb_TableBase_nativeGetTableSpec(
	JNIEnv* env, jobject jTable, jlong nativeTablePtr)
{
	static jmethodID jTableSpecConsId = GetTableSpecMethodID(env, "<init>", "()V");
	if (jTableSpecConsId) {
    	jobject jTableSpec = env->NewObject(GetClassTableSpec(env), jTableSpecConsId);
    	
        Table* pTable = TBL(nativeTablePtr);
	    const Spec& tableSpec = pTable->get_spec();
        UpdateJTableSpecFromSpec(env, tableSpec, jTableSpec);
	    
        return jTableSpec;
	}
    return NULL;
}

JNIEXPORT jint JNICALL Java_com_tightdb_TableBase_nativeGetColumnType(
	JNIEnv* env, jobject jTable, jlong nativeTablePtr, jlong columnIndex)
{
	return static_cast<int>( TBL(nativeTablePtr)->get_column_type( S(columnIndex)) );
}

JNIEXPORT jlong JNICALL Java_com_tightdb_TableBase_nativeAddEmptyRow(
	JNIEnv* env, jobject jTable, jlong nativeTablePtr, jlong rows)
{
	return static_cast<jlong>( TBL(nativeTablePtr)->add_empty_row( S(rows)) );
}

JNIEXPORT void JNICALL Java_com_tightdb_TableBase_nativeRemove(
	JNIEnv* env, jobject jTable, jlong nativeTablePtr, jlong rowIndex)
{
	TBL(nativeTablePtr)->remove(S(rowIndex));
}

JNIEXPORT void JNICALL Java_com_tightdb_TableBase_nativeRemoveLast(
	JNIEnv* env, jobject jTableBase, jlong nativeTablePtr)
{
	TBL(nativeTablePtr)->remove_last();
}

JNIEXPORT void JNICALL Java_com_tightdb_TableBase_nativeInsertLong(
	JNIEnv* env, jobject jTable, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex, jlong value)
{
	TBL(nativeTablePtr)->insert_int( S(columnIndex), S(rowIndex), value);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableBase_nativeInsertBoolean(
	JNIEnv* env, jobject jTable, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex, jboolean value)
{
	TBL(nativeTablePtr)->insert_bool( S(columnIndex), S(rowIndex), value != 0 ? true : false);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableBase_nativeInsertDate(
	JNIEnv* env, jobject jTable, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex, jlong dateTimeValue)
{
	TBL(nativeTablePtr)->insert_date( S(columnIndex), S(rowIndex), static_cast<time_t>(dateTimeValue));
}

JNIEXPORT void JNICALL Java_com_tightdb_TableBase_nativeInsertString(
	JNIEnv* env, jobject jTable, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex, jstring value)
{
	const char* valueCharPtr = env->GetStringUTFChars(value, NULL);
    if (!valueCharPtr) 
        return;

	TBL(nativeTablePtr)->insert_string( S(columnIndex), S(rowIndex), valueCharPtr);
	env->ReleaseStringUTFChars(value, valueCharPtr);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableBase_nativeInsertMixed(
	JNIEnv* env, jobject jTable, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex, jobject jMixedValue)
{
	Table* pTable = TBL(nativeTablePtr);
	ColumnType columnType = GetMixedObjectType(env, jMixedValue);
    TR("\nInsertMixed columnType %d\n", columnType);
	switch(columnType) {
	case COLUMN_TYPE_INT:
		{
			jlong longValue = GetMixedIntValue(env, jMixedValue);
			pTable->insert_mixed( S(columnIndex), S(rowIndex), Mixed(longValue));
			return;
		}
	case COLUMN_TYPE_BOOL:
		{
			jboolean boolValue = GetMixedBooleanValue(env, jMixedValue);
			pTable->insert_mixed( S(columnIndex), S(rowIndex), Mixed(boolValue != 0 ? true : false));
			return;
		}
	case COLUMN_TYPE_STRING:
		{
			jstring jStringValue = GetMixedStringValue(env, jMixedValue);
			const char* stringCharPtr = env->GetStringUTFChars(jStringValue, NULL);
            if (!stringCharPtr) 
                break;
			pTable->insert_mixed( S(columnIndex), S(rowIndex), Mixed(stringCharPtr));
			env->ReleaseStringUTFChars(jStringValue, stringCharPtr);
			return;
		}
	case COLUMN_TYPE_BINARY:
		{
            jint mixedBinaryType = GetMixedBinaryType(env, jMixedValue);
			if (mixedBinaryType == 0) { // byte[]
                TR("\ninsertMixed(byte[])\n");
				jbyteArray dataArray = GetMixedByteArrayValue(env, jMixedValue);
                if (!dataArray) {
                    TR("\nCan't get MixedValue, ByteArray\n");
                    break;
                }
				BinaryData binaryData;
				binaryData.pointer = (const char*)(env->GetByteArrayElements(dataArray, NULL));
                if (!binaryData.pointer) {
                    TR("\nCan't get ByteArray\n");
                    break;
                }
                binaryData.len = S(env->GetArrayLength(dataArray));
				pTable->insert_mixed( S(columnIndex), S(rowIndex), Mixed(binaryData));
				env->ReleaseByteArrayElements(dataArray, (jbyte*)(binaryData.pointer), 0);
                return;

			} else if (mixedBinaryType == 1) { // ByteBuffer
                TR("\ninsertMixed(ByteBuffer)\n");
                jobject jByteBuffer = GetMixedByteBufferValue(env, jMixedValue);
                if (!jByteBuffer) {
                    TR("\nCan't get ByteBuffer\n");
                    break;
                }
                BinaryData binaryData;
				binaryData.pointer = (const char*)(env->GetDirectBufferAddress(jByteBuffer));
                TR("SetMixed(Binary, data=%x, len=%d)", binaryData.pointer, binaryData.len);
				if (!binaryData.pointer) {
                    TR("\nCan't get BufferAddress\n");
                    break;
                }
                binaryData.len = S(env->GetDirectBufferCapacity(jByteBuffer));
				TR("SetMixed(Binary, data=%x, len=%d)", binaryData.pointer, binaryData.len);
                if (binaryData.len >= 0) {
                    pTable->insert_mixed( S(columnIndex), S(rowIndex), Mixed(binaryData));
                    return;
                }
            } else {
				TR("\nError Mixed binary type invalid: %\n", mixedBinaryType);
			}
            break;
		}
	case COLUMN_TYPE_DATE:
		{
			jlong dateTimeValue = GetMixedDateTimeValue(env, jMixedValue);
			pTable->insert_mixed( S(columnIndex), S(rowIndex), Mixed(tightdb::Date(static_cast<time_t>(dateTimeValue))));
			return;
		}
	case COLUMN_TYPE_TABLE:
		{
			pTable->insert_mixed( S(columnIndex), S(rowIndex), Mixed(COLUMN_TYPE_TABLE));
		    return;
        }
	default:
		{
			TR("\nThis type of mixed is not supported yet: %s\n", __FUNCTION__);
		}
	}
    ThrowException(env, IllegalArgument, "nativeInsertMixed()");
}


JNIEXPORT void JNICALL Java_com_tightdb_TableBase_nativeSetMixed(
	JNIEnv* env, jobject jTable, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex, jobject jMixedValue)
{	
	Table* pTable = TBL(nativeTablePtr);
	ColumnType columnType = GetMixedObjectType(env, jMixedValue);
	switch(columnType) {
	case COLUMN_TYPE_INT:
		{
			jlong longValue = GetMixedIntValue(env, jMixedValue);
			pTable->set_mixed( S(columnIndex), S(rowIndex), Mixed(longValue));
			return;
		}
	case COLUMN_TYPE_BOOL:
		{
			jboolean boolValue = GetMixedBooleanValue(env, jMixedValue);
			pTable->set_mixed( S(columnIndex), S(rowIndex), Mixed(boolValue != 0 ? true : false));
			return;
		}
	case COLUMN_TYPE_STRING:
		{
			jstring jStringValue = GetMixedStringValue(env, jMixedValue);
			const char* stringCharPtr = env->GetStringUTFChars(jStringValue, NULL);
            if (stringCharPtr) {
			    pTable->set_mixed( S(columnIndex), S(rowIndex), Mixed(stringCharPtr));
			    env->ReleaseStringUTFChars(jStringValue, stringCharPtr);
            }
			return;
		}
	case COLUMN_TYPE_DATE:
		{
			jlong dateTimeValue = GetMixedDateTimeValue(env, jMixedValue);
			Date date(dateTimeValue);
			pTable->set_mixed( S(columnIndex), S(rowIndex), Mixed(date));
			return;
		}
	case COLUMN_TYPE_BINARY:
		{
			jint mixedBinaryType = GetMixedBinaryType(env, jMixedValue);
			if (mixedBinaryType == 0) {
				jbyteArray dataArray = GetMixedByteArrayValue(env, jMixedValue);
                if (!dataArray)
                    break;
				BinaryData binaryData;
				binaryData.pointer = (const char*)(env->GetByteArrayElements(dataArray, NULL));
                if (!binaryData.pointer) 
                    break;
                binaryData.len = S(env->GetArrayLength(dataArray));
				pTable->set_mixed( S(columnIndex), S(rowIndex), Mixed(binaryData));
				env->ReleaseByteArrayElements(dataArray, (jbyte*)(binaryData.pointer), 0);
                return;
			} else if (mixedBinaryType == 1) {
                jobject jByteBuffer = GetMixedByteBufferValue(env, jMixedValue);
                if (!jByteBuffer)
                    break;
				BinaryData binaryData;
				binaryData.pointer = (const char*)(env->GetDirectBufferAddress(jByteBuffer));
                TR("SetMixed(Binary, data=%x, len=%d)", binaryData.pointer, binaryData.len);
				if (!binaryData.pointer) 
                    break;
                binaryData.len = S(env->GetDirectBufferCapacity(jByteBuffer));
				TR("SetMixed(Binary, data=%x, len=%d)", binaryData.pointer, binaryData.len);
                if (binaryData.len >= 0)
                    pTable->set_mixed( S(columnIndex), S(rowIndex), Mixed(binaryData));
                return;
			}
            break; // failed
		}
	case COLUMN_TYPE_TABLE:
		{
			pTable->set_mixed( S(columnIndex), S(rowIndex), Mixed(COLUMN_TYPE_TABLE));
		    return;
        }
    default:
		{
			TR("\nERROR: This type of mixed is not supported yet: %d.", columnType);
		}
	}
    TR("\nERROR: nativeSetMixed() failed.\n");
    ThrowException(env, IllegalArgument, "nativeSetMixed()");
}

JNIEXPORT void JNICALL Java_com_tightdb_TableBase_nativeInsertSubTable(
	JNIEnv* env, jobject jTable, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex)
{
	TR("nativeInsertSubTable(jTable:%x, nativeTablePtr: %x, colIdx: %lld, rowIdx: %lld)\n",
       jTable, nativeTablePtr,  columnIndex, rowIndex);
	TBL(nativeTablePtr)->insert_subtable( S(columnIndex), S(rowIndex));
}

JNIEXPORT void JNICALL Java_com_tightdb_TableBase_nativeInsertDone(
	JNIEnv* env, jobject jTable, jlong nativeTablePtr)
{
	TBL(nativeTablePtr)->insert_done();
}


JNIEXPORT jlong JNICALL Java_com_tightdb_TableBase_nativeGetLong(
	JNIEnv* env, jobject jTable, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex)
{
	return TBL(nativeTablePtr)->get_int( S(columnIndex), S(rowIndex));
}

JNIEXPORT jboolean JNICALL Java_com_tightdb_TableBase_nativeGetBoolean(
	JNIEnv* env, jobject jTable, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex)
{
	return TBL(nativeTablePtr)->get_bool( S(columnIndex), S(rowIndex));
}

JNIEXPORT jlong JNICALL Java_com_tightdb_TableBase_nativeGetDateTime(
	JNIEnv* env, jobject jTableBase, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex)
{
	return TBL(nativeTablePtr)->get_date( S(columnIndex), S(rowIndex));
}

JNIEXPORT jstring JNICALL Java_com_tightdb_TableBase_nativeGetString(
	JNIEnv* env, jobject jTable, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex)
{
	const char* valueCharPtr = TBL(nativeTablePtr)->get_string( S(columnIndex), S(rowIndex));
	return env->NewStringUTF(valueCharPtr);
}

JNIEXPORT jobject JNICALL Java_com_tightdb_TableBase_nativeGetBinary(
	JNIEnv* env, jobject jTable, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex)
{
	BinaryData data = TBL(nativeTablePtr)->get_binary( S(columnIndex), S(rowIndex));
	return env->NewDirectByteBuffer((void*)data.pointer, data.len);
}

JNIEXPORT jbyteArray JNICALL Java_com_tightdb_TableBase_nativeGetByteArray(
	JNIEnv* env, jobject, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex)
{
    return tbl_GetByteArray<Table>(env, nativeTablePtr, columnIndex, rowIndex);
}

JNIEXPORT jint JNICALL Java_com_tightdb_TableBase_nativeGetMixedType(
	JNIEnv* env, jobject jTableBase, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex)
{
	if (!IndexValid(env, nativeTablePtr, columnIndex, rowIndex))
        return NULL;
	ColumnType mixedType = TBL(nativeTablePtr)->get_mixed_type( S(columnIndex), S(rowIndex));
	return static_cast<jint>(mixedType);
}

JNIEXPORT jobject JNICALL Java_com_tightdb_TableBase_nativeGetMixed(
	JNIEnv* env, jobject jTable, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex)
{
    if (!IndexValid(env, nativeTablePtr, columnIndex, rowIndex))
        return NULL;
	Mixed value = TBL(nativeTablePtr)->get_mixed( S(columnIndex), S(rowIndex));
	return CreateJMixedFromMixed(env, value);
}

JNIEXPORT jlong JNICALL Java_com_tightdb_TableBase_nativeGetSubTable(
	JNIEnv* env, jobject jTableBase, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex)
{
    if (!IndexAndTypeValid(env, nativeTablePtr, columnIndex, rowIndex, COLUMN_TYPE_TABLE))
        return NULL;
	Table* pSubTable = const_cast<Table*>(LangBindHelper::get_subtable_ptr(TBL(nativeTablePtr), 
        S(columnIndex), S(rowIndex)));
    TR("nativeGetSubTable(jTableBase:%x, nativeTablePtr: %x, colIdx: %lld, rowIdx: %lld) : %x\n",
        jTableBase, nativeTablePtr, columnIndex, rowIndex, pSubTable);
    return (jlong)pSubTable;
}

JNIEXPORT jlong JNICALL Java_com_tightdb_TableBase_nativeGetSubTableSize(
	JNIEnv* env, jobject jTable, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex)
{
    Table* tbl = TBL(nativeTablePtr);
    if (IndexAndTypeValid(env, nativeTablePtr, columnIndex, rowIndex, COLUMN_TYPE_TABLE)) {
        return tbl->get_subtable_size( S(columnIndex), S(rowIndex));
    }
    return -1;
}

JNIEXPORT void JNICALL Java_com_tightdb_TableBase_nativeSetString(
	JNIEnv* env, jobject jTable, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex, jstring value)
{
	const char* valueCharPtr = env->GetStringUTFChars(value, NULL);
    if (valueCharPtr) {
	    TBL(nativeTablePtr)->set_string( S(columnIndex), S(rowIndex), valueCharPtr);
	    env->ReleaseStringUTFChars(value, valueCharPtr);
    }
}

JNIEXPORT void JNICALL Java_com_tightdb_TableBase_nativeSetLong(
	JNIEnv* env, jobject jTable, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex, jlong value)
{
	return TBL(nativeTablePtr)->set_int( S(columnIndex), S(rowIndex), value);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableBase_nativeSetBoolean(
	JNIEnv* env, jobject jTable, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex, jboolean value)
{
	return TBL(nativeTablePtr)->set_bool( S(columnIndex), S(rowIndex), value == JNI_TRUE ? true : false);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableBase_nativeSetDate(
	JNIEnv* env, jobject jTable, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex, jlong dateTimeValue)
{
	TBL(nativeTablePtr)->set_date( S(columnIndex), S(rowIndex), dateTimeValue);
}


JNIEXPORT void JNICALL Java_com_tightdb_TableBase_nativeSetBinary(
	JNIEnv* env, jobject jTable, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex, jobject byteBuffer)
{	
	const char *dataPtr = (const char*)(env->GetDirectBufferAddress(byteBuffer));
    if (!dataPtr) {
        ThrowException(env, IllegalArgument, "nativeSetBinary byteBuffer");
        return;
    }
    size_t dataLen = S(env->GetDirectBufferCapacity(byteBuffer));
    if (dataLen < 0) {
        ThrowException(env, IllegalArgument, "nativeSetBinary(byteBuffer) - can't get BufferCapacity.");
        return;
    }
    TBL(nativeTablePtr)->set_binary( S(columnIndex), S(rowIndex), dataPtr, dataLen);            
}

JNIEXPORT void JNICALL Java_com_tightdb_TableBase_nativeInsertBinary__JJJLjava_nio_ByteBuffer_2(
	JNIEnv* env, jobject jTable, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex, jobject byteBuffer)
{
    const char *dataPtr = (const char*)(env->GetDirectBufferAddress(byteBuffer));
    if (!dataPtr) {
        TR("\nERROR: nativeInsertBinary( nativePtr %x, col %x, row %x, byteBuf %x) - can't get BufferAddress!\n",
            nativeTablePtr, columnIndex, rowIndex, byteBuffer);
        ThrowException(env, IllegalArgument, "nativeInsertBinary(byteBuffer) - can't get BufferAddress.");
        return;
    }
    size_t dataLen = S(env->GetDirectBufferCapacity(byteBuffer));
    if (dataLen < 0) {
        ThrowException(env, IllegalArgument, "nativeInsertBinary(byteBuffer) - can't get BufferCapacity.");
        return;
    }
    TBL(nativeTablePtr)->insert_binary( S(columnIndex), S(rowIndex), dataPtr, dataLen);
}


JNIEXPORT void JNICALL Java_com_tightdb_TableBase_nativeSetByteArray(
	JNIEnv* env, jobject jTable, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex, jbyteArray dataArray)
{
	jbyte* bytePtr = env->GetByteArrayElements(dataArray, NULL);
    if (!bytePtr) {
        ThrowException(env, IllegalArgument, "nativeSetByteArray");
        return;
    }
    size_t dataLen = S(env->GetArrayLength(dataArray));
    TBL(nativeTablePtr)->set_binary( S(columnIndex), S(rowIndex), reinterpret_cast<const char*>(bytePtr), dataLen);
    
    env->ReleaseByteArrayElements(dataArray, bytePtr, 0);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableBase_nativeInsertBinary__JJJ_3B(
	JNIEnv* env, jobject jTableBase, jlong nativeTableBasePtr, jlong columnIndex, jlong rowIndex, jbyteArray dataArray)
{
	jbyte* bytePtr = env->GetByteArrayElements(dataArray, NULL);
    if (!bytePtr) {
        ThrowException(env, IllegalArgument, "nativeInsertBinary byte[]");
        return;
    }
    size_t dataLen = S(env->GetArrayLength(dataArray));
	TBL(nativeTableBasePtr)->insert_binary( S(columnIndex), S(rowIndex), reinterpret_cast<const char*>(bytePtr), dataLen);
	
    env->ReleaseByteArrayElements(dataArray, bytePtr, 0);
}


JNIEXPORT void JNICALL Java_com_tightdb_TableBase_nativeClearSubTable(
	JNIEnv* env, jobject jTableBase, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex)
{
	TBL(nativeTablePtr)->clear_subtable( S(columnIndex), S(rowIndex));
}

// Indexing methods:

JNIEXPORT void JNICALL Java_com_tightdb_TableBase_nativeSetIndex(
	JNIEnv* env, jobject jTable, jlong nativeTablePtr, jlong columnIndex)
{
	TBL(nativeTablePtr)->set_index( S(columnIndex));
}

JNIEXPORT jboolean JNICALL Java_com_tightdb_TableBase_nativeHasIndex(
	JNIEnv* env, jobject jTable, jlong nativeTablePtr, jlong columnIndex)
{
	return TBL(nativeTablePtr)->has_index( S(columnIndex));
}

// Aggregare methods:

JNIEXPORT jlong JNICALL Java_com_tightdb_TableBase_nativeSum(
	JNIEnv* env, jobject jTableBase, jlong nativePtr, jlong columnIndex)
{
	return TBL(nativePtr)->sum( S(columnIndex));
}

JNIEXPORT jlong JNICALL Java_com_tightdb_TableBase_nativeMaximum(
	JNIEnv* env, jobject jTableBase, jlong nativePtr, jlong columnIndex)
{	
	return TBL(nativePtr)->maximum( S(columnIndex));
}

JNIEXPORT jlong JNICALL Java_com_tightdb_TableBase_nativeMinimum(
	JNIEnv* env, jobject jTableBase, jlong nativePtr, jlong columnIndex)
{	
	return TBL(nativePtr)->minimum( S(columnIndex));
}

JNIEXPORT jlong JNICALL Java_com_tightdb_TableBase_nativeAverage(
	JNIEnv* env, jobject jTableBase, jlong nativePtr, jlong columnIndex)
{
	//return TBL(nativePtr)->average( S(columnIndex));
	return 0;
}

JNIEXPORT jlong JNICALL Java_com_tightdb_TableBase_nativeFindFirstInt(
	JNIEnv* env, jobject jTable, jlong nativeTablePtr, jlong columnIndex, jlong value)
{
	return TBL(nativeTablePtr)->find_first_int( S(columnIndex), value);
}

JNIEXPORT jlong JNICALL Java_com_tightdb_TableBase_nativeFindFirstBoolean(
	JNIEnv* env, jobject jTable, jlong nativeTablePtr, jlong columnIndex, jboolean value)
{
	return TBL(nativeTablePtr)->find_first_bool( S(columnIndex), value != 0 ? true : false);
}

JNIEXPORT jlong JNICALL Java_com_tightdb_TableBase_nativeFindFirstDate(
	JNIEnv* env, jobject jTable, jlong nativeTablePtr, jlong columnIndex, jlong dateTimeValue)
{
	return TBL(nativeTablePtr)->find_first_date( S(columnIndex), (time_t)dateTimeValue);
}

JNIEXPORT jlong JNICALL Java_com_tightdb_TableBase_nativeFindFirstString(
	JNIEnv* env, jobject jTable, jlong nativeTablePtr, jlong columnIndex, jstring value)
{
	const char* valueCharPtr = env->GetStringUTFChars(value, NULL);
    if (!valueCharPtr) 
        return -1;

	jlong result = TBL(nativeTablePtr)->find_first_string( S(columnIndex), valueCharPtr);
    env->ReleaseStringUTFChars(value, valueCharPtr);
	return result;
}

JNIEXPORT jlong JNICALL Java_com_tightdb_TableBase_nativeFindAllInt(
	JNIEnv* env, jobject jTableBase, jlong nativeTablePtr, jlong columnIndex, jlong value)
{
	Table* pTable = TBL(nativeTablePtr);
	TableView* pTableView = new TableView( pTable->find_all_int( S(columnIndex), value) );
	return reinterpret_cast<jlong>(pTableView);
}

JNIEXPORT jlong JNICALL Java_com_tightdb_TableBase_nativeFindAllBool(
	JNIEnv* env, jobject jTableBase, jlong nativeTablePtr, jlong columnIndex, jboolean value)
{
	Table* pTable = TBL(nativeTablePtr);
	TableView* pTableView = new TableView( pTable->find_all_bool( S(columnIndex), value != 0 ? true : false) );
	return reinterpret_cast<jlong>(pTableView);
}

JNIEXPORT jlong JNICALL Java_com_tightdb_TableBase_nativeFindAllDate(
	JNIEnv* env, jobject jTableBase, jlong nativeTablePtr, jlong columnIndex, jlong dateTimeValue)
{
	Table* pTable = TBL(nativeTablePtr);
	TableView* pTableView = new TableView( pTable->find_all_date( S(columnIndex), static_cast<time_t>(dateTimeValue)) );
	return reinterpret_cast<jlong>(pTableView);
}

JNIEXPORT jlong JNICALL Java_com_tightdb_TableBase_nativeFindAllString(
	JNIEnv* env, jobject jTableBase, jlong nativeTablePtr, jlong columnIndex, jstring value)
{
	Table* pTable = TBL(nativeTablePtr);
	const char* valueCharPtr = env->GetStringUTFChars(value, NULL);
    if (!valueCharPtr) 
        return -1;

	TableView* pTableView = new TableView( pTable->find_all_string( S(columnIndex), valueCharPtr) );
	return reinterpret_cast<jlong>(pTableView);
}

JNIEXPORT void JNICALL Java_com_tightdb_TableBase_nativeOptimize(
	JNIEnv* env, jobject jTableBase, jlong nativeTablePtr)
{
	TBL(nativeTablePtr)->optimize();
}

JNIEXPORT void JNICALL Java_com_tightdb_TableBase_nativeClose(
	JNIEnv* env, jobject jTable, jlong nativeTablePtr)
{
	TR("nativeClose(jTable: %x, nativeTablePtr: %x)\n", jTable, nativeTablePtr);
    LangBindHelper::unbind_table_ref(TBL(nativeTablePtr));
}

JNIEXPORT jlong JNICALL Java_com_tightdb_TableBase_createNative(JNIEnv* env, jobject jTable)
{
    TR("CreateNative(jTable: %x)\n", jTable);
    return reinterpret_cast<jlong>(new Table());
}
