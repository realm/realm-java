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
#include "util.hpp"

using namespace realm;
using namespace realm::_impl;

JavaBindingContext::JavaBindingContext(const ConcreteJavaBindContext& concrete_context)
    : m_local_jni_env(concrete_context.jni_env)
{
    jint ret = m_local_jni_env->GetJavaVM(&m_jvm);
    if (ret != 0) {
        throw std::runtime_error(util::format("Failed to get Java vm. Error: %d", ret));
    }
    if (concrete_context.realm_notifier) {
        m_realm_notifier = m_local_jni_env->NewWeakGlobalRef(concrete_context.realm_notifier);
        jclass cls = m_local_jni_env->GetObjectClass(m_realm_notifier);
        m_notify_by_other_method = m_local_jni_env->GetMethodID(cls, "notifyCommitByOtherThread", "()V");
    } else {
        m_realm_notifier = nullptr;
    }
    if (concrete_context.row_notifier) {
        m_row_notifier = m_local_jni_env->NewWeakGlobalRef(concrete_context.row_notifier);
        jclass cls = m_local_jni_env->GetObjectClass(m_row_notifier);
        m_get_observers_method = m_local_jni_env->GetMethodID(cls, "getObservers",
                                                              "()[Lio/realm/internal/RowNotifier$Observer;");
        m_get_observed_row_ptrs_method = m_local_jni_env->GetMethodID(cls, "getObservedRowPtrs",
                                                                      "([Lio/realm/internal/RowNotifier$Observer;)[J");
        m_clear_row_refs = m_local_jni_env->GetMethodID(cls, "clearRowRefs", "()V");
        jclass observer_cls = GetClass(m_local_jni_env, "io/realm/internal/RowNotifier$Observer");
        m_observer_notify_listener = m_local_jni_env->GetMethodID(observer_cls, "notifyListener", "()V");
    } else {
        m_row_notifier = nullptr;
    }
}

JavaBindingContext::~JavaBindingContext()
{
    if (m_realm_notifier) {
        // Always try to attach here since this may be called in the finalizer/phantom thread where m_local_jni_env
        // should not be used on. No need to call DetachCurrentThread since this thread should always be created by
        // JVM.
        JNIEnv *env;
        m_jvm->AttachCurrentThread(&env, nullptr);
        env->DeleteWeakGlobalRef(m_realm_notifier);
    }
}

void JavaBindingContext::changes_available()
{
    jobject notifier = m_local_jni_env->NewLocalRef(m_realm_notifier);
    if (notifier) {
        m_local_jni_env->CallVoidMethod(m_realm_notifier, m_notify_by_other_method);
        m_local_jni_env->DeleteLocalRef(notifier);
    }
}

std::vector<BindingContext::ObserverState> JavaBindingContext::get_observed_rows()
{
    jobject row_notifier = m_local_jni_env->NewLocalRef(m_row_notifier);
    if (!row_notifier) {
        // The row notifier got GCed
        return {};
    }

    jobjectArray observers = static_cast<jobjectArray>(
            m_local_jni_env->CallObjectMethod(row_notifier, m_get_observers_method));
    jlongArray row_ptr_jarray = static_cast<jlongArray >(
            m_local_jni_env->CallObjectMethod(row_notifier, m_get_observed_row_ptrs_method, observers));
    JniLongArray row_ptrs(m_local_jni_env, row_ptr_jarray);

    std::vector<BindingContext::ObserverState> state_list;
    for (jsize i = 0; i < row_ptrs.len(); ++i) {
        BindingContext::ObserverState observer_state;
        Row* row = reinterpret_cast<Row*>(row_ptrs[i]);
        observer_state.table_ndx = row->get_table()->get_index_in_group();
        observer_state.row_ndx = row->get_index();
        observer_state.info = m_local_jni_env->GetObjectArrayElement(observers, i);
        state_list.push_back(std::move(observer_state));
    }
    return state_list;
}

void JavaBindingContext::did_change(std::vector<BindingContext::ObserverState> const& observer_state_list,
                        std::vector<void*> const& invalidated,
                        bool /*version_changed*/)
{
    for (auto state : observer_state_list) {
        jobject observer = reinterpret_cast<jobject>(state.info);
        //if (!state.changes.empty()) {
            m_local_jni_env->CallVoidMethod(observer, m_observer_notify_listener);
        //}
    }
    for (auto deleted_row_observer : invalidated) {
        jobject observer = reinterpret_cast<jobject>(deleted_row_observer);
        m_local_jni_env->CallVoidMethod(observer, m_observer_notify_listener);
    }
    m_local_jni_env->CallVoidMethod(m_row_notifier, m_clear_row_refs);
}

