/*
 * Copyright 2018 Realm Inc.
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

#ifndef REALM_JNI_IMPL_SUBSCRIPTION_WRAPPER_HPP
#define REALM_JNI_IMPL_SUBSCRIPTION_WRAPPER_HPP


#include "jni_util/java_class.hpp"
#include "jni_util/java_global_weak_ref.hpp"
#include "jni_util/java_method.hpp"

#include <realm/util/optional.hpp>
#include <sync/partial_sync.hpp>

namespace realm {
namespace _impl {

// Wrapper of Object Store Subscription
// We need to control the life cycle of Results/List, weak ref of Java OsResults/OsList object and the NotificationToken.
// Wrap all three together, so when the Java OsResults/OsList object gets GCed, all three of them will be invalidated.
class SubscriptionWrapper {
public:
    SubscriptionWrapper(partial_sync::Subscription subscription)
        : m_subscription_weak_ref(),
          m_notification_token(),
          m_subscription(std::move(subscription))
    {
    }

    ~SubscriptionWrapper() = default;

    SubscriptionWrapper(SubscriptionWrapper &&) = delete;
    SubscriptionWrapper &operator=(SubscriptionWrapper &&) = delete;
    SubscriptionWrapper(SubscriptionWrapper const &) = delete;
    SubscriptionWrapper &operator=(SubscriptionWrapper const &) = delete;

    partial_sync::Subscription& subscription() {
        return m_subscription;
    };

    void start_listening(JNIEnv* env, jobject j_subscription_object);
    void stop_listening();

private:
    jni_util::JavaGlobalWeakRef m_subscription_weak_ref;
    partial_sync::SubscriptionNotificationToken m_notification_token;
    partial_sync::Subscription m_subscription;
};

void SubscriptionWrapper::start_listening(JNIEnv *env, jobject j_subscription_object)
{
    static jni_util::JavaClass os_results_class(env, "io/realm/internal/sync/OsSubscription");
    static jni_util::JavaMethod notify_change_listeners(env, os_results_class, "notifyChangeListeners", "()V");

    if (!m_subscription_weak_ref) {
        m_subscription_weak_ref = jni_util::JavaGlobalWeakRef(env, j_subscription_object);
    }

    auto cb = [=]() {
        // OS will call all notifiers' callback in one run, so check the Java exception first!!
        if (env->ExceptionCheck())
            return;

        m_subscription_weak_ref.call_with_local_ref(env, [&](JNIEnv *local_env, jobject subscription_obj) {
            local_env->CallVoidMethod(subscription_obj, notify_change_listeners);
        });
    };

    m_notification_token = m_subscription.add_notification_callback(cb);
}

void SubscriptionWrapper::stop_listening()
{
    m_notification_token = {};
}

} // namespace _impl
} // namespace realm

#endif // REALM_JNI_IMPL_SUBSCRIPTION_WRAPPER_HPP
