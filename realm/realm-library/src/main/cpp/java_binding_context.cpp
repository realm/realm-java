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
#include "jni_util/java_method.hpp"

#include "util.hpp"

using namespace realm;
using namespace realm::_impl;
using namespace realm::jni_util;

std::vector<BindingContext::ObserverState> JavaBindingContext::get_observed_rows()
{
    std::vector<BindingContext::ObserverState> state_list;

    if (m_row_notifier) {
        m_row_notifier.call_with_local_ref([&] (auto env, auto row_notifier) {
            static JavaMethod get_observers_method(env, row_notifier,
                                                   "getObservers",
                                                   "()[Lio/realm/internal/RowNotifier$RowObserverPair;");
            static JavaMethod get_observed_row_ptrs_method(env, row_notifier,
                                                           "getObservedRowPtrs",
                                                           "([Lio/realm/internal/RowNotifier$RowObserverPair;)[J");

            jobjectArray observers = static_cast<jobjectArray>(
                    env->CallObjectMethod(row_notifier, get_observers_method));
            jlongArray row_ptr_jarray = static_cast<jlongArray >(
                    env->CallObjectMethod(row_notifier, get_observed_row_ptrs_method, observers));
            JniLongArray row_ptrs(env, row_ptr_jarray);

            for (jsize i = 0; i < row_ptrs.len(); ++i) {
                BindingContext::ObserverState observer_state;
                Row* row = reinterpret_cast<Row*>(row_ptrs[i]);
                observer_state.table_ndx = row->get_table()->get_index_in_group();
                observer_state.row_ndx = row->get_index();
                observer_state.info = env->GetObjectArrayElement(observers, i);
                state_list.push_back(std::move(observer_state));
            }
        });
    }

    return state_list;
}

void JavaBindingContext::before_notify()
{
    if (m_java_notifier) {
        m_java_notifier.call_with_local_ref([&] (JNIEnv* env, jobject notifier_obj) {
            // Method IDs from RealmNotifier implementation. Cache them as member vars.
            static JavaMethod notify_by_other_method(env,
                                                     notifier_obj,
                                                     "changesAvailable", "()V");
            env->CallVoidMethod(notifier_obj, notify_by_other_method);
        });
    }
}

void JavaBindingContext::did_change(std::vector<BindingContext::ObserverState> const& observer_state_list,
                        std::vector<void*> const& invalidated,
                        bool version_changed)
{
    auto env = JniUtils::get_env();
    static JavaMethod row_observer_pair_on_change_method(env,
                                                         "io/realm/internal/RowNotifier$RowObserverPair",
                                                         "onChange", "()V");

    for (auto state : observer_state_list) {
        if (env->ExceptionCheck()) return;

        jobject observer = reinterpret_cast<jobject>(state.info);
        //if (!state.changes.empty()) {
            env->CallVoidMethod(observer, row_observer_pair_on_change_method);
        //}
    }
    for (auto deleted_row_observer : invalidated) {
        if (env->ExceptionCheck()) return;

        jobject observer = reinterpret_cast<jobject>(deleted_row_observer);
        env->CallVoidMethod(observer, row_observer_pair_on_change_method);
    }

    if (env->ExceptionCheck()) return;
    m_row_notifier.call_with_local_ref(env, [&] (JNIEnv*, jobject row_notifier_obj) {
        static JavaMethod clear_row_refs_method(env, row_notifier_obj, "clearRowRefs", "()V");
        env->CallVoidMethod(row_notifier_obj, clear_row_refs_method);
    });

    if (env->ExceptionCheck()) return;
    if (version_changed) {
        m_java_notifier.call_with_local_ref(env, [&] (JNIEnv*, jobject notifier_obj) {
            static JavaMethod realm_notifier_did_change_method(env, notifier_obj, "didChange", "()V");
            env->CallVoidMethod(notifier_obj, realm_notifier_did_change_method);
        });
    }
}

