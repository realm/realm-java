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

void JavaBindingContext::before_notify()
{
    if (JniUtils::get_env()->ExceptionCheck()) {
        return;
    }
    if (m_java_notifier) {
        m_java_notifier.call_with_local_ref([&](JNIEnv* env, jobject notifier_obj) {
            // Method IDs from RealmNotifier implementation. Cache them as member vars.
            static JavaMethod notify_by_other_method(env, notifier_obj, "beforeNotify", "()V");
            env->CallVoidMethod(notifier_obj, notify_by_other_method);
        });
    }
}

void JavaBindingContext::did_change(std::vector<BindingContext::ObserverState> const&, std::vector<void*> const&,
                                    bool version_changed)
{
    auto env = JniUtils::get_env();

    if (JniUtils::get_env()->ExceptionCheck()) {
        return;
    }
    if (version_changed) {
        m_java_notifier.call_with_local_ref(env, [&](JNIEnv*, jobject notifier_obj) {
            static JavaMethod realm_notifier_did_change_method(env, notifier_obj, "didChange", "()V");
            env->CallVoidMethod(notifier_obj, realm_notifier_did_change_method);
        });
    }
}
