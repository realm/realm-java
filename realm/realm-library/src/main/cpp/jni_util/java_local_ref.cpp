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

#include "java_local_ref.hpp"
#include "jni_utils.hpp"

#include <memory>

using namespace realm::jni_util;

JavaLocalRef::~JavaLocalRef()
{
    m_env->DeleteLocalRef(m_jobject);
}

JavaLocalRef& JavaLocalRef::operator=(JavaLocalRef&& rhs)
{
    this->~JavaLocalRef();
    new (this) JavaLocalRef(std::move(rhs));
    return *this;
}

JavaLocalRef::JavaLocalRef(JavaLocalRef&& rhs)
{
    m_env = rhs.m_env;
    m_jobject = rhs.m_jobject;
    rhs.m_env = nullptr;
    rhs.m_jobject = nullptr;
}
