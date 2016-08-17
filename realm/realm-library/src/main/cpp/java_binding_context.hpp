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

#include "binding_context.hpp"

#include <memory>

namespace realm {

namespace _impl {

// Binding context which will be called from OS.
class JavaBindingContext final : public BindingContext {
private:
    struct ConcreteJavaBindContext {
        JNIEnv* jni_env;
        jobject java_notifier;
        explicit ConcreteJavaBindContext(JNIEnv* env, jobject notifier)
            :jni_env(env), java_notifier(notifier) { }
    };

    // The JNIEnv for the thread which creates the Realm. This should only be used on the current thread.
    JNIEnv* m_local_jni_env;
    // A weak global ref to the implementation of RealmNotifier
    // Java should hold a strong ref to it as long as the SharedRealm lives
    jobject m_java_notifier;
    // Method IDs from RealmNotifier implementation. Cache them as member vars.
    jmethodID m_notify_by_other_method;

public:
    virtual ~JavaBindingContext();
    virtual void changes_available();

    JavaBindingContext(const ConcreteJavaBindContext&);
    static std::unique_ptr<JavaBindingContext> create(JNIEnv* env, jobject notifier) {
        return std::make_unique<JavaBindingContext>(ConcreteJavaBindContext{env, notifier});
    };
};

} // namespace _impl

} // namespace realm

#endif

