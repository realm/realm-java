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

#include "java_binding_context.hpp"

using namespace realm;
using namespace realm::_impl;

JavaBindingContext::JavaBindingContext(const ConcreteJavaBindContext& concrete_context)
    : m_local_jni_env(concrete_context.jni_env)
{
    if (concrete_context.java_notifier) {
        m_java_notifier = m_local_jni_env->NewWeakGlobalRef(concrete_context.java_notifier);
        jclass cls = m_local_jni_env->GetObjectClass(m_java_notifier);
        m_notify_by_other_method = m_local_jni_env->GetMethodID(cls, "notifyByOtherThread", "()V");
    } else {
        m_java_notifier = nullptr;
    }
}

JavaBindingContext::~JavaBindingContext() {
    if (m_java_notifier) {
        m_local_jni_env->DeleteWeakGlobalRef(m_java_notifier);
    }
}

void JavaBindingContext::changes_available() {
    jobject notifier = m_local_jni_env->NewLocalRef(m_java_notifier);
    if (notifier) {
        m_local_jni_env->CallVoidMethod(m_java_notifier, m_notify_by_other_method);
        m_local_jni_env->DeleteLocalRef(notifier);
    }
}

