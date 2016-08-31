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

#include "util/format.hpp"

using namespace realm;
using namespace realm::_impl;

JavaBindingContext::JavaBindingContext(const ConcreteJavaBindContext& concrete_context)
    : m_local_jni_env(concrete_context.jni_env)
{
    jint ret = m_local_jni_env->GetJavaVM(&m_jvm);
    if (ret != 0) {
        throw std::runtime_error(util::format("Failed to get Java vm. Error: %d", ret));
    }
    if (concrete_context.java_notifier) {
        m_java_notifier = m_local_jni_env->NewWeakGlobalRef(concrete_context.java_notifier);
        jclass cls = m_local_jni_env->GetObjectClass(m_java_notifier);
        m_notify_by_other_method = m_local_jni_env->GetMethodID(cls, "notifyCommitByOtherThread", "()V");
    } else {
        m_java_notifier = nullptr;
    }
}

JavaBindingContext::~JavaBindingContext()
{
    if (m_java_notifier) {
        // Always try to attach here since this may be called in the finalizer/phantom thread where m_local_jni_env
        // should not be used on. No need to call DetachCurrentThread since this thread should always be created by
        // JVM.
        JNIEnv *env;
        m_jvm->AttachCurrentThread(&env, nullptr);
        env->DeleteWeakGlobalRef(m_java_notifier);
    }
}

void JavaBindingContext::changes_available()
{
    jobject notifier = m_local_jni_env->NewLocalRef(m_java_notifier);
    if (notifier) {
        m_local_jni_env->CallVoidMethod(m_java_notifier, m_notify_by_other_method);
        m_local_jni_env->DeleteLocalRef(notifier);
    }
}

