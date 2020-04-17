/*
 * Copyright 2017 Realm Inc.
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

#include "realm/array_blob.hpp"

#include "java_class_global_def.hpp"
#include "java_exception_def.hpp"
#include "jni_util/java_exception_thrower.hpp"

using namespace realm;
using namespace realm::_impl;

jbyteArray JavaClassGlobalDef::new_byte_array(JNIEnv* env, const BinaryData& binary_data)
{
    static_assert(MAX_JSIZE >= ArrayBlob::max_binary_size, "ArrayBlob's max size is too big.");

    if (binary_data.is_null()) {
        return nullptr;
    }

    auto size = static_cast<jsize>(binary_data.size());
    jbyteArray ret = env->NewByteArray(size);
    if (!ret) {
        THROW_JAVA_EXCEPTION(env, JavaExceptionDef::OutOfMemory,
                             util::format("'NewByteArray' failed with size %1.", size));
    }

    env->SetByteArrayRegion(ret, 0, size, reinterpret_cast<const jbyte*>(binary_data.data()));
    return ret;
}


jobject JavaClassGlobalDef::new_decimal128(JNIEnv* env, const Decimal128& decimal128)
{
    if (decimal128.is_null()) {
        return nullptr;
    }
    static jni_util::JavaMethod fromIEEE754BIDEncoding(env, instance()->m_bson_decimal128, "fromIEEE754BIDEncoding", "(JJ)Lorg/bson/types/Decimal128;", true);
    const Decimal128::Bid128* raw = decimal128.raw();
    return env->CallStaticObjectMethod(instance()->m_bson_decimal128, fromIEEE754BIDEncoding, static_cast<jlong>(raw->w[1]), static_cast<jlong>(raw->w[0]));
}

jobject JavaClassGlobalDef::new_object_id(JNIEnv* env, const ObjectId& objectId)
{
    static jni_util::JavaMethod init(env, instance()->m_bson_object_id, "<init>", "(Ljava/lang/String;)V");
    return env->NewObject(instance()->m_bson_object_id, init, to_jstring(env, objectId.to_string().data()));
}
