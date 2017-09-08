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

#include "java_method.hpp"
#include "java_class.hpp"

#include <realm/util/assert.hpp>

using namespace realm::jni_util;

JavaMethod::JavaMethod(JNIEnv* env, JavaClass const& cls, const char* method_name, const char* signature, bool static_method)
{
    if (static_method) {
        m_method_id = env->GetStaticMethodID(cls, method_name, signature);
    }
    else {
        m_method_id = env->GetMethodID(cls, method_name, signature);
    }

    REALM_ASSERT_RELEASE_EX(m_method_id != nullptr, method_name, signature);
}
