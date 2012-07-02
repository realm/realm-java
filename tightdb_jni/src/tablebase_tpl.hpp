#ifndef TIGHTDB_JNI_TABLEBASE_TPL_HPP
#define TIGHTDB_JNI_TABLEBASE_TPL_HPP


template <class T> 
jbyteArray tbl_GetByteArray(JNIEnv* env, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex)
{
    if (!INDEX_VALID(env, reinterpret_cast<T*>(nativeTablePtr), columnIndex, rowIndex)) return NULL;

	BinaryData data = reinterpret_cast<T*>(nativeTablePtr)->get_binary( S(columnIndex), S(rowIndex));
	if (data.len <= MAX_JSIZE) {
        jbyteArray jresult = env->NewByteArray(static_cast<jsize>(data.len));
        if (jresult)
            env->SetByteArrayRegion(jresult, 0, static_cast<jsize>(data.len), (const jbyte*)(data.pointer));
	    return jresult;
    } else {
        //???TODO: Better exception
        ThrowException(env, IndexOutOfBounds, "Length of ByteArray is larger than int.");
        return NULL;
    }
}

template <class M, class T>
void tbl_nativeDoByteArray(M doBinary, T* pTable, JNIEnv* env, jlong columnIndex, jlong rowIndex, jbyteArray dataArray)
{
    jbyte* bytePtr = env->GetByteArrayElements(dataArray, NULL);
    if (!bytePtr) {
        ThrowException(env, IllegalArgument, "doByteArray");
        return;
    }
    size_t dataLen = S(env->GetArrayLength(dataArray));
    (pTable->*doBinary)( S(columnIndex), S(rowIndex), reinterpret_cast<const char*>(bytePtr), dataLen);
    env->ReleaseByteArrayElements(dataArray, bytePtr, 0);
}

template <class M, class T>
void tbl_nativeDoBinary(M doBinary, T* pTable, JNIEnv* env, jlong columnIndex, jlong rowIndex, jobject byteBuffer)
{
	const char *dataPtr = (const char*)(env->GetDirectBufferAddress(byteBuffer));
    if (!dataPtr) {
         TR("\nERROR: doBinary( nativePtr %x, col %x, row %x, byteBuf %x) - can't get BufferAddress!\n",
            pTable, columnIndex, rowIndex, byteBuffer);
        ThrowException(env, IllegalArgument, "doBinary(). ByteBuffer is invalid");
        return;
    }
    size_t dataLen = S(env->GetDirectBufferCapacity(byteBuffer));
    if (dataLen < 0) {
        ThrowException(env, IllegalArgument, "doBinary(byteBuffer) - can't get BufferCapacity.");
        return;
    }
    (pTable->*doBinary)( S(columnIndex), S(rowIndex), dataPtr, dataLen);            
}


// insertMixed() or setMixed() value for TableView or Table class

template <class M, class T>
void tbl_nativeDoMixed(M doMixed, T* pTable, JNIEnv* env, jlong columnIndex, jlong rowIndex, jobject jMixedValue)
{	
	ColumnType valueType = GetMixedObjectType(env, jMixedValue);
	switch(valueType) {
	case COLUMN_TYPE_INT:
		{
			jlong longValue = GetMixedIntValue(env, jMixedValue);
			(pTable->*doMixed)( S(columnIndex), S(rowIndex), Mixed(longValue));
			return;
		}
	case COLUMN_TYPE_BOOL:
		{
			jboolean boolValue = GetMixedBooleanValue(env, jMixedValue);
			(pTable->*doMixed)( S(columnIndex), S(rowIndex), Mixed(boolValue != 0 ? true : false));
			return;
		}
	case COLUMN_TYPE_STRING:
		{
			jstring stringValue = GetMixedStringValue(env, jMixedValue);
			const char* stringCharPtr = env->GetStringUTFChars(stringValue, NULL);
            if (stringCharPtr) {
			    (pTable->*doMixed)( S(columnIndex), S(rowIndex), Mixed(stringCharPtr));
			    env->ReleaseStringUTFChars(stringValue, stringCharPtr);
                return;
            }
			break;
		}
	case COLUMN_TYPE_DATE:
		{
			jlong dateTimeValue = GetMixedDateTimeValue(env, jMixedValue);
			Date date(dateTimeValue);
			(pTable->*doMixed)( S(columnIndex), S(rowIndex), Mixed(date));
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
				(pTable->*doMixed)( S(columnIndex), S(rowIndex), Mixed(binaryData));
				env->ReleaseByteArrayElements(dataArray, (jbyte*)(binaryData.pointer), 0);
                return;
			} else if (mixedBinaryType == 1) {
                jobject jByteBuffer = GetMixedByteBufferValue(env, jMixedValue);
                if (!jByteBuffer)
                    break;
				BinaryData binaryData;
				binaryData.pointer = (const char*)(env->GetDirectBufferAddress(jByteBuffer));
                TR("nativeSetMixed(Binary, data=%x, len=%d)\n", binaryData.pointer, binaryData.len);
				if (!binaryData.pointer) 
                    break;
                binaryData.len = S(env->GetDirectBufferCapacity(jByteBuffer));
				TR("nativeSetMixed(Binary, data=%x, len=%d)\n", binaryData.pointer, binaryData.len);
                if (binaryData.len >= 0)
                    (pTable->*doMixed)( S(columnIndex), S(rowIndex), Mixed(binaryData));
                return;
			}
            break; // failed
		}
	case COLUMN_TYPE_TABLE:
		{
			(pTable->*doMixed)( S(columnIndex), S(rowIndex), Mixed(COLUMN_TYPE_TABLE));
		    return;
        }
    default:
		{
			TR_ERR("ERROR: This type of mixed is not supported yet: %d.", valueType);
		}
	}
    TR_ERR("\nERROR: nativeSetMixed() failed.\n");
    ThrowException(env, IllegalArgument, "nativeSetMixed()");
}


#endif // TIGHTDB_JNI_TABLEBASE_TPL_HPP