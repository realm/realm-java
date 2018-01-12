/*
 * Copyright 2016 Realm Inc.
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

#include "io_realm_internal_OsSchemaInfo.h"

#include <schema.hpp>
#include <object_schema.hpp>
#include <property.hpp>
#include "java_accessor.hpp"
#include "java_exception_def.hpp"
#include "util.hpp"
#include "jni_util/java_exception_thrower.hpp"

using namespace realm;
using namespace realm::jni_util;
using namespace realm::util;
using namespace realm::_impl;

static void finalize_schema(jlong ptr)
{
    TR_ENTER_PTR(ptr);
    delete reinterpret_cast<Schema*>(ptr);
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_OsSchemaInfo_nativeCreateFromList(JNIEnv* env, jclass,
                                                                                 jlongArray objectSchemaPtrs_)
{
    TR_ENTER()
    try {
        std::vector<ObjectSchema> object_schemas;
        JLongArrayAccessor array(env, objectSchemaPtrs_);
        for (jsize i = 0; i < array.size(); ++i) {
            object_schemas.push_back(*reinterpret_cast<ObjectSchema*>(array[i]));
        }
        auto* schema = new Schema(std::move(object_schemas));
        return reinterpret_cast<jlong>(schema);
    }
    CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_OsSchemaInfo_nativeGetFinalizerPtr(JNIEnv*, jclass)
{
    TR_ENTER()
    return reinterpret_cast<jlong>(&finalize_schema);
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_OsSchemaInfo_nativeGetObjectSchemaInfo(JNIEnv* env, jclass,
                                                                                      jlong native_ptr,
                                                                                      jstring j_class_name)
{
    TR_ENTER_PTR(native_ptr)

    try {
        JStringAccessor class_name_accessor(env, j_class_name);
        StringData class_name(class_name_accessor);
        auto& schema = *reinterpret_cast<Schema*>(native_ptr);
        auto it = schema.find(class_name);
        if (it == schema.end()) {
            THROW_JAVA_EXCEPTION(env, JavaExceptionDef::IllegalState,
                                 format("Class '%1' cannot be found in the schema.", class_name.data()));
        } else {
            return reinterpret_cast<jlong>(new ObjectSchema(*it));
        }
    }
    CATCH_STD()

    return reinterpret_cast<jlong>(nullptr);
}
