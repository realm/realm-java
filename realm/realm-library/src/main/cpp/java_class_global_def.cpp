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
