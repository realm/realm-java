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
#include "java_class_global_def.hpp"
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
            static JavaMethod notify_by_other_method(env, JavaClassGlobalDef::realm_notifier(), "beforeNotify",
                                                     "()V");
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
            static JavaMethod realm_notifier_did_change_method(env, JavaClassGlobalDef::realm_notifier(), "didChange",
                                                               "()V");
            env->CallVoidMethod(notifier_obj, realm_notifier_did_change_method);
        });
    }
}

void JavaBindingContext::schema_did_change(Schema const&)
{
    if (!m_schema_changed_callback) {
        return;
    }
    auto env = JniUtils::get_env(false);
    static JavaMethod on_schema_changed_method(env, JavaClassGlobalDef::shared_realm_schema_change_callback(),
                                               "onSchemaChanged", "()V");
    m_schema_changed_callback.call_with_local_ref(
        env, [](JNIEnv* env, jobject callback_obj) { env->CallVoidMethod(callback_obj, on_schema_changed_method); });
}

void JavaBindingContext::set_schema_changed_callback(JNIEnv* env, jobject schema_changed_callback)
{
    m_schema_changed_callback = JavaGlobalWeakRef(env, schema_changed_callback);
}

void JavaBindingContext::will_send_notifications() {
    auto env = JniUtils::get_env();
    m_java_notifier.call_with_local_ref(env, [&](JNIEnv*, jobject notifier_obj) {
        static JavaMethod realm_notifier_will_send_notifications(env, JavaClassGlobalDef::realm_notifier(),
                                                           "willSendNotifications", "()V");
        env->CallVoidMethod(notifier_obj, realm_notifier_will_send_notifications);
    });
}

void JavaBindingContext::did_send_notifications() {
    auto env = JniUtils::get_env();
    m_java_notifier.call_with_local_ref(env, [&](JNIEnv*, jobject notifier_obj) {
        static JavaMethod realm_notifier_did_send_notifications(env, JavaClassGlobalDef::realm_notifier(),
                                                                 "didSendNotifications", "()V");
        env->CallVoidMethod(notifier_obj, realm_notifier_did_send_notifications);
    });
}
