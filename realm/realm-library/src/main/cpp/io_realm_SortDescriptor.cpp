#include "io_realm_internal_SortDescriptor.h"

#include <realm/views.hpp>

#include "util.hpp"

using namespace realm;

JNIEXPORT jlong JNICALL
Java_io_realm_internal_SortDescriptor_nativeCreate(JNIEnv* env, jclass, jlong table_ptr, jobjectArray column_indices,
                                                   jbooleanArray ascending)
{
    try {
        JniArrayOfArrays<JniLongArray, jlongArray> arrays(env, column_indices);
        JniBooleanArray ascending_array(env, ascending);
        jsize arr_len = arrays.len();

        std::vector<std::vector<size_t>> indices;
        std::vector<bool> ascending_list;

        for (int i = 0; i < arr_len; ++i) {
            JniLongArray& jni_long_array = arrays[i];
            std::vector<size_t> col_indices;
            for (int j = 0; j < jni_long_array.len(); ++j) {
                col_indices.push_back(static_cast<size_t >(jni_long_array[j]));
            }
            indices.push_back(std::move(col_indices));
            if (ascending) {
                ascending_list.push_back(static_cast<bool>(ascending_array[i]));
            }
        }

        SortDescriptor* descriptor = ascending ?
                new SortDescriptor(*reinterpret_cast<Table*>(table_ptr), std::move(indices), std::move(ascending_list))
                          : new SortDescriptor(*reinterpret_cast<Table*>(table_ptr), std::move(indices));
        return reinterpret_cast<jlong>(descriptor);
    } CATCH_STD()

    return reinterpret_cast<jlong>(nullptr);
}

JNIEXPORT void JNICALL
Java_io_realm_internal_SortDescriptor_nativeClose(JNIEnv* env, jclass, jlong ptr) {
    try {
        SortDescriptor* descriptor = reinterpret_cast<SortDescriptor*>(ptr);
        delete descriptor;
    } CATCH_STD()
}
