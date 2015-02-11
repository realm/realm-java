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

#ifndef REALM_JNI_TABLEBASE_TPL_HPP
#define REALM_JNI_TABLEBASE_TPL_HPP


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


#endif // REALM_JNI_TABLEBASE_TPL_HPP
