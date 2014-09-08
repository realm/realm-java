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

#ifndef TIGHTDB_JNI_TABLEBASE_TPL_HPP
#define TIGHTDB_JNI_TABLEBASE_TPL_HPP


template <class T>
jbyteArray tbl_GetByteArray(JNIEnv* env, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex)
{
    if (!TBL_AND_INDEX_VALID(env, reinterpret_cast<T*>(nativeTablePtr), columnIndex, rowIndex))
        return NULL;

    BinaryData bin = reinterpret_cast<T*>(nativeTablePtr)->get_binary( S(columnIndex), S(rowIndex));
    if (bin.size() <= MAX_JSIZE) {
        jbyteArray jresult = env->NewByteArray(static_cast<jsize>(bin.size()));
        if (jresult)
            env->SetByteArrayRegion(jresult, 0, static_cast<jsize>(bin.size()), reinterpret_cast<const jbyte*>(bin.data()));  // throws
        return jresult;
    }
    else {
        ThrowException(env, IllegalArgument, "Length of ByteArray is larger than an Int.");
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
    (pTable->*doBinary)( S(columnIndex), S(rowIndex), BinaryData(reinterpret_cast<char*>(bytePtr), dataLen));
    env->ReleaseByteArrayElements(dataArray, bytePtr, 0);
}


template <class M, class T>
void tbl_nativeDoBinary(M doBinary, T* pTable, JNIEnv* env, jlong columnIndex, jlong rowIndex, jobject byteBuffer)
{
    BinaryData bin;
    if (GetBinaryData(env, byteBuffer, bin))
        (pTable->*doBinary)( S(columnIndex), S(rowIndex), bin);
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
            (pTable->*doMixed)( S(columnIndex), S(rowIndex), Mixed(static_cast<int64_t>(longValue)));
            return;
        }
    case type_Float:
        {
            jfloat floatValue = GetMixedFloatValue(env, jMixedValue);
            (pTable->*doMixed)( S(columnIndex), S(rowIndex), Mixed(floatValue));
            return;
        }
    case type_Double:
        {
            jdouble doubleValue = GetMixedDoubleValue(env, jMixedValue);
            (pTable->*doMixed)( S(columnIndex), S(rowIndex), Mixed(doubleValue));
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
            JStringAccessor string(env, stringValue); // throws
            (pTable->*doMixed)( S(columnIndex), S(rowIndex), StringData(string));
            return;
        }
    case type_DateTime:
        {
            jlong dateTimeValue = GetMixedDateTimeValue(env, jMixedValue);
            DateTime date(dateTimeValue);
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
                char* data = reinterpret_cast<char*>(env->GetByteArrayElements(dataArray, NULL));
                if (!data)
                    break;
                size_t size = S(env->GetArrayLength(dataArray));
                (pTable->*doMixed)( S(columnIndex), S(rowIndex), BinaryData(data, size));
                env->ReleaseByteArrayElements(dataArray, reinterpret_cast<jbyte*>(data), 0);
                return;
            }
            else if (mixedBinaryType == 1) {
                jobject jByteBuffer = GetMixedByteBufferValue(env, jMixedValue);
                if (!jByteBuffer)
                    break;
                BinaryData binaryData;
                if (GetBinaryData(env, jByteBuffer, binaryData))
                    (pTable->*doMixed)( S(columnIndex), S(rowIndex), binaryData);
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
    case type_Link:
        break;
    case type_LinkList:
        break;
    }
    TR_ERR((env, "\nERROR: nativeSetMixed() failed.\n"));
    ThrowException(env, IllegalArgument, "nativeSetMixed()");
}

template <class R>
void row_nativeSetMixed(R* pRow, JNIEnv* env, jlong columnIndex, jobject jMixedValue)
{
    DataType valueType = GetMixedObjectType(env, jMixedValue);
    switch(valueType) {
    case type_Int:
        {
            jlong longValue = GetMixedIntValue(env, jMixedValue);
            pRow->set_mixed( S(columnIndex), Mixed(static_cast<int64_t>(longValue)));
            return;
        }
    case type_Float:
        {
            jfloat floatValue = GetMixedFloatValue(env, jMixedValue);
            pRow->set_mixed( S(columnIndex), Mixed(floatValue));
            return;
        }
    case type_Double:
        {
            jdouble doubleValue = GetMixedDoubleValue(env, jMixedValue);
            pRow->set_mixed( S(columnIndex), Mixed(doubleValue));
            return;
        }
    case type_Bool:
        {
            jboolean boolValue = GetMixedBooleanValue(env, jMixedValue);
            pRow->set_mixed( S(columnIndex), Mixed(boolValue != 0 ? true : false));
            return;
        }
    case type_String:
        {
            jstring stringValue = GetMixedStringValue(env, jMixedValue);
            JStringAccessor string(env, stringValue); // throws
            pRow->set_mixed( S(columnIndex), StringData(string));
            return;
        }
    case type_DateTime:
        {
            jlong dateTimeValue = GetMixedDateTimeValue(env, jMixedValue);
            DateTime date(dateTimeValue);
            pRow->set_mixed( S(columnIndex), Mixed(date));
            return;
        }
    case type_Binary:
        {
            jint mixedBinaryType = GetMixedBinaryType(env, jMixedValue);
            if (mixedBinaryType == 0) {
                jbyteArray dataArray = GetMixedByteArrayValue(env, jMixedValue);
                if (!dataArray)
                    break;
                char* data = reinterpret_cast<char*>(env->GetByteArrayElements(dataArray, NULL));
                if (!data)
                    break;
                size_t size = S(env->GetArrayLength(dataArray));
                pRow->set_mixed( S(columnIndex), BinaryData(data, size));
                env->ReleaseByteArrayElements(dataArray, reinterpret_cast<jbyte*>(data), 0);
                return;
            }
            else if (mixedBinaryType == 1) {
                jobject jByteBuffer = GetMixedByteBufferValue(env, jMixedValue);
                if (!jByteBuffer)
                    break;
                BinaryData binaryData;
                if (GetBinaryData(env, jByteBuffer, binaryData))
                    pRow->set_mixed( S(columnIndex), binaryData);
                return;
            }
            break; // failed
        }
    case type_Table:
        {
            pRow->set_mixed( S(columnIndex), Mixed::subtable_tag());
            return;
        }
    case type_Mixed:
        break;
    case type_Link:
        break;
    case type_LinkList:
        break;
    }
    TR_ERR((env, "\nERROR: nativeSetMixed() failed.\n"));
    ThrowException(env, IllegalArgument, "nativeSetMixed()");
}


#endif // TIGHTDB_JNI_TABLEBASE_TPL_HPP
