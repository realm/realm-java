#include "util.hpp"
#include "com_tightdb_SubTableSchema.h"

using namespace tightdb;
using namespace std;

void arrayToVector(JNIEnv *env, jlongArray path, vector<size_t>& nativePath)
{
    jsize size = env->GetArrayLength(path);
    nativePath.reserve(size+1);

    jlong *pathElements = env->GetLongArrayElements(path, 0);
    for (jsize i = 0; i < size; ++i) {
        nativePath.push_back(pathElements[i]);
    }
    env->ReleaseLongArrayElements(path, pathElements, JNI_ABORT);
}

JNIEXPORT jlong JNICALL Java_com_tightdb_SubTableSchema_nativeAddColumn
  (JNIEnv *env, jobject, jlong nativeTablePtr, jlongArray path, jint colType, jstring name)
{
    if (!TABLE_VALID(env, TBL(nativeTablePtr)))
        return 0;
    JStringAccessor name2(env, name);
    if (!name2)
        return 0;
    try {
        vector<size_t> nativePath;
        arrayToVector(env, path, nativePath);
        return TBL(nativeTablePtr)->add_subcolumn(nativePath, DataType(colType), name2);
    } CATCH_STD()
    return 0;
}

JNIEXPORT void JNICALL Java_com_tightdb_SubTableSchema_nativeRemoveColumn
  (JNIEnv *env, jobject, jlong nativeTablePtr, jlongArray path, jlong columnIndex)
{
    if (!TBL_AND_COL_INDEX_VALID(env, TBL(nativeTablePtr), columnIndex))
        return;
    try {
        vector<size_t> nativePath;
        arrayToVector(env, path, nativePath);
        nativePath.push_back(columnIndex);

        TBL(nativeTablePtr)->remove_subcolumn(nativePath);
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_com_tightdb_SubTableSchema_nativeRenameColumn
  (JNIEnv *env, jobject, jlong nativeTablePtr, jlongArray path, jlong columnIndex, jstring name)
{
    if (!TBL_AND_COL_INDEX_VALID(env, TBL(nativeTablePtr), columnIndex))
        return;
    JStringAccessor name2(env, name);
    if (!name2)
        return;
    try {
        vector<size_t> nativePath;
        arrayToVector(env, path, nativePath);
        nativePath.push_back(columnIndex);

        TBL(nativeTablePtr)->rename_subcolumn(nativePath, name2);
    } CATCH_STD()
}
