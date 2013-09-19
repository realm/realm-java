#include "util.hpp"
#include "com_tightdb_SubtableDefinition.h"

using namespace tightdb;


JNIEXPORT jlong JNICALL Java_com_tightdb_SubTableDefinition_nativeAddColumn
  (JNIEnv *env, jobject, jlong nativeTablePtr, jlongArray path, jint colType, jstring name)
{
    if (!TABLE_VALID(env, TBL(nativeTablePtr)))
        return 0;
    JStringAccessor name2(env, name);
    if (!name2)
        return 0;

    jsize size = env->GetArrayLength(path);
    std::vector<std::size_t> nativePath;
    nativePath.reserve(size);

    jlong *pathElements = env->GetLongArrayElements(path, 0);
    for(int i = 0; i < size; i++) {
        nativePath.push_back(pathElements[i]);
    }
    env->ReleaseLongArrayElements(path, pathElements, JNI_ABORT);


    return TBL(nativeTablePtr)->add_subcolumn(nativePath, DataType(colType), name2);
}

JNIEXPORT void JNICALL Java_com_tightdb_SubTableDefinition_nativeRemoveColumn
  (JNIEnv *env, jobject, jlong nativeTablePtr, jlongArray path, jlong columnIndex)
{
    if (!TABLE_VALID(env, TBL(nativeTablePtr)))
        return;


    jsize size = env->GetArrayLength(path);
    std::vector<std::size_t> nativePath;
    nativePath.reserve(size+1);

    jlong *pathElements = env->GetLongArrayElements(path, 0);
    for(int i = 0; i < size; i++) {
        nativePath.push_back(pathElements[i]);
    }
    env->ReleaseLongArrayElements(path, pathElements, JNI_ABORT);
    nativePath.push_back(columnIndex);

    TBL(nativeTablePtr)->remove_subcolumn(nativePath);
}

JNIEXPORT void JNICALL Java_com_tightdb_SubTableDefinition_nativeRenameColumn
  (JNIEnv *env, jobject, jlong nativeTablePtr, jlongArray path, jlong columnIndex, jstring name)
{
    if (!TABLE_VALID(env, TBL(nativeTablePtr)))
        return;
    JStringAccessor name2(env, name);
    if (!name2)
        return;

    jsize size = env->GetArrayLength(path);
    std::vector<std::size_t> nativePath;
    nativePath.reserve(size+1);

    jlong *pathElements = env->GetLongArrayElements(path, 0);
    for(int i = 0; i < size; i++) {
        nativePath.push_back(pathElements[i]);
    }
    env->ReleaseLongArrayElements(path, pathElements, JNI_ABORT);
    nativePath.push_back(columnIndex);

    TBL(nativeTablePtr)->rename_subcolumn(nativePath, name2);
}
