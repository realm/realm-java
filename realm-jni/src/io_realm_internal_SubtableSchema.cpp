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

#include "util.hpp"
#include "io_realm_internal_SubtableSchema.h"

using namespace tightdb;
using namespace std;

void arrayToVector(JNIEnv* env, jlongArray path, vector<size_t>& nativePath)
{
    jsize size = env->GetArrayLength(path);
    nativePath.reserve(size+1);

    jlong* pathElements = env->GetLongArrayElements(path, 0);
    for (jsize i = 0; i < size; ++i) {
        nativePath.push_back(S(pathElements[i]));
    }
    env->ReleaseLongArrayElements(path, pathElements, JNI_ABORT);
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_SubtableSchema_nativeAddColumn
  (JNIEnv* env, jobject, jlong  nativeTablePtr, jlongArray path, jint colType, jstring name)
{
    if (!TABLE_VALID(env, TBL(nativeTablePtr)))
        return 0;
    try {
        JStringAccessor name2(env, name); // throws
        vector<size_t> nativePath;
        arrayToVector(env, path, nativePath);
        return TBL(nativeTablePtr)->add_subcolumn(nativePath, DataType(colType), name2);
    }
    CATCH_STD()
    return 0;
}

JNIEXPORT void JNICALL Java_io_realm_internal_SubtableSchema_nativeRemoveColumn
  (JNIEnv* env, jobject, jlong nativeTablePtr, jlongArray path, jlong columnIndex)
{
    if (!TBL_AND_COL_INDEX_VALID(env, TBL(nativeTablePtr), columnIndex))
        return;
    try {
        vector<size_t> nativePath;
        arrayToVector(env, path, nativePath);
        TBL(nativeTablePtr)->remove_subcolumn(nativePath, columnIndex);
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_SubtableSchema_nativeRenameColumn
  (JNIEnv* env, jobject, jlong nativeTablePtr, jlongArray path, jlong columnIndex, jstring name)
{
    if (!TBL_AND_COL_INDEX_VALID(env, TBL(nativeTablePtr), columnIndex))
        return;
    try {
        JStringAccessor name2(env, name);
        vector<size_t> nativePath;
        arrayToVector(env, path, nativePath);
        TBL(nativeTablePtr)->rename_subcolumn(nativePath, columnIndex, name2);
    }
    CATCH_STD()
}
