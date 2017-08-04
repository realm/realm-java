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

#include "jni_util/java_global_weak_ref.hpp"

namespace realm {

namespace jni_util {
class JavaClass;
}

namespace _impl {
// Binding context which will be called from OS.
class JavaBindingContext final : public BindingContext {
private:
    struct ConcreteJavaBindContext {
        JNIEnv* jni_env;
        jobject java_notifier;
    };

    // Weak global refs to the needed Java objects.
    // Java should hold a strong ref to them as long as the SharedRealm lives
    jni_util::JavaGlobalWeakRef m_java_notifier;
    jni_util::JavaGlobalWeakRef m_schema_changed_callback;
    // Problem has been seen if the class is retrieved directly from loop callback. So make sure get_notifier_class()
    // is called once when create BindingContext.
    jni_util::JavaClass const& m_notifier_class;
    static jni_util::JavaClass const& get_notifier_class(JNIEnv*);

public:
    virtual ~JavaBindingContext(){};
    void before_notify() override;
    void did_change(std::vector<ObserverState> const& observers, std::vector<void*> const& invalidated,
                    bool version_changed = true) override;
    void schema_did_change(Schema const&) override;

    explicit JavaBindingContext(const ConcreteJavaBindContext& concrete_context)
        : m_java_notifier(concrete_context.jni_env, concrete_context.java_notifier)
        , m_schema_changed_callback()
        , m_notifier_class(get_notifier_class(concrete_context.jni_env))
    {
    }
    JavaBindingContext(const JavaBindingContext&) = delete;
    JavaBindingContext& operator=(const JavaBindingContext&) = delete;
    JavaBindingContext(JavaBindingContext&&) = delete;
    JavaBindingContext& operator=(JavaBindingContext&&) = delete;

    void set_schema_changed_callback(JNIEnv* env, jobject schema_changed_callback);

    static inline std::unique_ptr<JavaBindingContext> create(JNIEnv* env, jobject notifier)
    {
        return std::make_unique<JavaBindingContext>(ConcreteJavaBindContext{env, notifier});
    };
};

} // namespace _impl

} // namespace realm

#endif
