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

#include <realm.hpp>

template <class T>
jbyteArray tbl_GetByteArray(JNIEnv* env, jlong nativeTablePtr, jlong columnIndex, jlong rowIndex)
{
    if (!TBL_AND_INDEX_VALID(env, reinterpret_cast<T*>(nativeTablePtr), columnIndex, rowIndex))
        return NULL;

    realm::BinaryData bin = reinterpret_cast<T*>(nativeTablePtr)->get_binary( S(columnIndex), S(rowIndex));
    if (bin.is_null()) {
        return NULL;
    }
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

#endif // REALM_JNI_TABLEBASE_TPL_HPP
