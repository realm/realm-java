#ifndef TIGHTDB_JNI_TABLEBASE_TPL_HPP
#define TIGHTDB_JNI_TABLEBASE_TPL_HPP


template <class T> 
jbyteArray tbl_GetByteArray(JNIEnv* env, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex)
{
    if (!INDEX_VALID(env, reinterpret_cast<T*>(nativeTablePtr), columnIndex, rowIndex)) 
        return NULL;

    BinaryData data = reinterpret_cast<T*>(nativeTablePtr)->get_binary( S(columnIndex), S(rowIndex));
    if (data.len <= MAX_JSIZE) {
        jbyteArray jresult = env->NewByteArray(static_cast<jsize>(data.len));
        if (jresult)
            env->SetByteArrayRegion(jresult, 0, static_cast<jsize>(data.len), (const jbyte*)(data.pointer));
        return jresult;
    } else {
        //???TODO: More specific exception
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
    BinaryData data;
    if (GetBinaryData(env, byteBuffer, data))
        (pTable->*doBinary)( S(columnIndex), S(rowIndex), data.pointer, data.len);
}


// insertMixed() or setMixed() value for TableView or Table class

template <class M, class T>
void tbl_nativeDoMixed(M doMixed, T* pTable, JNIEnv* env, jlong columnIndex, jlong rowIndex, jobject jMixedValue)
{
    DataType valueType = GetMixedObjectType(env, jMixedValue);
    switch(valueType) {
    case type_Int:
        {
            jlong longValue = GetMixedIntValue(env, jMixedValue);
            (pTable->*doMixed)( S(columnIndex), S(rowIndex), Mixed(longValue));
            return;
        }
    case type_Bool:
        {
            jboolean boolValue = GetMixedBooleanValue(env, jMixedValue);
            (pTable->*doMixed)( S(columnIndex), S(rowIndex), Mixed(boolValue != 0 ? true : false));
            return;
        }
    case type_String:
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
    case type_Date:
        {
            jlong dateTimeValue = GetMixedDateTimeValue(env, jMixedValue);
            Date date(dateTimeValue);
            (pTable->*doMixed)( S(columnIndex), S(rowIndex), Mixed(date));
            return;
        }
    case type_Binary:
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
                if (GetBinaryData(env, jByteBuffer, binaryData))
                    (pTable->*doMixed)( S(columnIndex), S(rowIndex), Mixed(binaryData));
                return;
            }
            break; // failed
        }
    case type_Table:
        {
            (pTable->*doMixed)( S(columnIndex), S(rowIndex), Mixed::subtable_tag());
            return;
        }
    case type_Mixed:
        break;
    }
    TR_ERR((env, "\nERROR: nativeSetMixed() failed.\n"));
    ThrowException(env, IllegalArgument, "nativeSetMixed()");
}


#endif // TIGHTDB_JNI_TABLEBASE_TPL_HPP
