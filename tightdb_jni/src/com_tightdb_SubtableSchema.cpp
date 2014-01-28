#include "util.hpp"
#include "com_tightdb_SubtableSchema.h"

using namespace tightdb;
using namespace std;

void arrayToVector(JNIEnv *env, jlongArray path, vector<size_t>& native_path)
{
    jsize size = env->GetArrayLength(path);
    native_path.reserve(size+1);

    jlong *path_elements = env->GetLongArrayElements(path, 0);
    for (jsize i = 0; i < size; ++i) {
        native_path.push_back(S(path_elements[i]));
    }
    env->ReleaseLongArrayElements(path, path_elements, JNI_ABORT);
}

JNIEXPORT jlong JNICALL Java_com_tightdb_SubtableSchema_nativeAddColumn
  (JNIEnv *env, jobject, jlong native_table_ptr, jlongArray path, jint col_type, jstring name)
{
    if (!TABLE_VALID(env, TBL(native_table_ptr)))
        return 0;
    JStringAccessor name2(env, name);
    if (!name2)
        return 0;
    try {
        vector<size_t> native_path;
        arrayToVector(env, path, native_path);
        return TBL(native_table_ptr)->add_subcolumn(native_path, DataType(col_type), name2);
    }
    CATCH_STD()
    return 0;
}

JNIEXPORT void JNICALL Java_com_tightdb_SubtableSchema_nativeRemoveColumn
  (JNIEnv *env, jobject, jlong native_table_ptr, jlongArray path, jlong column_index)
{
    if (!TBL_AND_COL_INDEX_VALID(env, TBL(native_table_ptr), column_index))
        return;
    try {
        vector<size_t> native_path;
        arrayToVector(env, path, native_path);
        TBL(native_table_ptr)->remove_subcolumn(native_path, column_index);
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_com_tightdb_SubtableSchema_nativeRenameColumn
  (JNIEnv *env, jobject, jlong native_table_ptr, jlongArray path, jlong column_index, jstring name)
{
    if (!TBL_AND_COL_INDEX_VALID(env, TBL(native_table_ptr), column_index))
        return;
    JStringAccessor name2(env, name);
    if (!name2)
        return;
    try {
        vector<size_t> native_path;
        arrayToVector(env, path, native_path);
        TBL(native_table_ptr)->rename_subcolumn(native_path, column_index, name2);
    }
    CATCH_STD()
}
