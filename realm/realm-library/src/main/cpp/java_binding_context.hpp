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

#ifndef JAVA_BINDING_CONTEXT_HPP
#define JAVA_BINDING_CONTEXT_HPP

#include <jni.h>
#include <memory>

#include "binding_context.hpp"

namespace realm {

namespace _impl {

// Binding context which will be called from OS.
class JavaBindingContext final : public BindingContext {
private:
    struct ConcreteJavaBindContext {
        JNIEnv* jni_env;
        jobject realm_notifier;
        jobject row_notifier;
    };

    // The JNIEnv for the thread which creates the Realm. This should only be used on the current thread.
    JNIEnv* m_local_jni_env;
    // All methods should be called from the thread which creates the realm except the destructor which might be
    // called from finalizer/phantom daemon. So we need a jvm pointer to create JNIEnv there if needed.
    JavaVM* m_jvm;
    // A weak global ref to the implementation of RealmNotifier
    // Java should hold a strong ref to it as long as the SharedRealm lives
    jobject m_realm_notifier;
    // Method IDs from RealmNotifier implementation. Cache them as member vars.
    jmethodID m_notify_by_other_method;
    jmethodID m_realm_notifier_on_change;

    // A weak global ref to the RowNotifier object. Java should hold a strong ref to it.
    jobject m_row_notifier;
    // RowNotifier.getObservers()
    jmethodID m_get_observers_method;
    // RowNotifier.getObservedRowPtrs(Observer[])
    jmethodID m_get_observed_row_ptrs_method;
    // RowNotifier.clearRowRefs()
    jmethodID m_clear_row_refs;
    jmethodID m_observer_notify_listener;

public:
    virtual ~JavaBindingContext();
    virtual void changes_available();
    virtual std::vector<ObserverState> get_observed_rows();
    virtual void did_change(std::vector<ObserverState> const& observers,
                            std::vector<void*> const& invalidated,
                            bool version_changed=true);


    explicit JavaBindingContext(const ConcreteJavaBindContext&);
    JavaBindingContext(const JavaBindingContext&) = delete;
    JavaBindingContext& operator=(const JavaBindingContext&) = delete;
    JavaBindingContext(JavaBindingContext&&) = delete;
    JavaBindingContext& operator=(JavaBindingContext&&) = delete;

    static inline std::unique_ptr<JavaBindingContext> create(JNIEnv* env, jobject notifier, jobject row_notifier)
    {
        return std::make_unique<JavaBindingContext>(ConcreteJavaBindContext{env, notifier, row_notifier});
    };
};

} // namespace _impl

} // namespace realm

#endif

