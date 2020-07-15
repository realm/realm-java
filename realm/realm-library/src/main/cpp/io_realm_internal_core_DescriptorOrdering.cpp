/*
 * Copyright 2018 Realm Inc.
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

#include "io_realm_internal_core_DescriptorOrdering.h"

#include "java_query_descriptor.hpp"
#include "util.hpp"

using namespace realm;
using namespace realm::util;
using namespace realm::_impl;

static void finalize_descriptor(jlong ptr);
static void finalize_descriptor(jlong ptr)
{
    delete reinterpret_cast<DescriptorOrdering*>(ptr);
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_core_DescriptorOrdering_nativeGetFinalizerMethodPtr(JNIEnv*, jclass)
{
    return reinterpret_cast<jlong>(&finalize_descriptor);
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_core_DescriptorOrdering_nativeCreate(JNIEnv* env, jclass)
{
   try {
        return reinterpret_cast<jlong>(new DescriptorOrdering());
    }
    CATCH_STD()
    return reinterpret_cast<jlong>(nullptr);
}

JNIEXPORT void JNICALL Java_io_realm_internal_core_DescriptorOrdering_nativeAppendSort(JNIEnv* env, jclass,
                                                                             jlong descriptor_ptr,
                                                                             jobject j_sort_descriptor)
{
    try {
        auto descriptor = reinterpret_cast<DescriptorOrdering*>(descriptor_ptr);
        if (j_sort_descriptor) {
            descriptor->append_sort(JavaQueryDescriptor(env, j_sort_descriptor).sort_descriptor());
        }
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_core_DescriptorOrdering_nativeAppendDistinct(JNIEnv* env, jclass,
                                                                             jlong descriptor_ptr,
                                                                             jobject j_distinct_descriptor)
{
    try {
        auto descriptor = reinterpret_cast<DescriptorOrdering*>(descriptor_ptr);
        if (j_distinct_descriptor) {
            descriptor->append_distinct(JavaQueryDescriptor(env, j_distinct_descriptor).distinct_descriptor());
        }
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_core_DescriptorOrdering_nativeAppendLimit(JNIEnv* env, jclass,
                                                                             jlong descriptor_ptr,
                                                                             jlong limit)
{
    try {
         auto descriptor = reinterpret_cast<DescriptorOrdering*>(descriptor_ptr);
         descriptor->append_limit(limit);
    }
    CATCH_STD()
}

JNIEXPORT jboolean JNICALL Java_io_realm_internal_core_DescriptorOrdering_nativeIsEmpty(JNIEnv* env, jclass,
                                                                             jlong descriptor_ptr)
{
    try {
        auto descriptor = reinterpret_cast<DescriptorOrdering*>(descriptor_ptr);
        return descriptor->is_empty() ? JNI_TRUE : JNI_FALSE;
    }
    CATCH_STD()
    return JNI_TRUE;
}
