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

#include "java_global_ref.hpp"
#include "jni_utils.hpp"

#include <memory>

using namespace realm::jni_util;

JavaGlobalRef::~JavaGlobalRef()
{
    if (m_ref) {
        JniUtils::get_env()->DeleteGlobalRef(m_ref);
    }
}

JavaGlobalRef& JavaGlobalRef::operator=(JavaGlobalRef&& rhs)
{
    this->~JavaGlobalRef();
    new (this) JavaGlobalRef(std::move(rhs));
    return *this;
}

JavaGlobalRef::JavaGlobalRef(JavaGlobalRef& rhs)
        : m_ref(rhs.m_ref ? jni_util::JniUtils::get_env(true)->NewGlobalRef(rhs.m_ref) : nullptr)
{
}
