/*
 * Copyright 2017 Realm Inc.
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

#ifndef REALM_JNI_IMPL_OBSERVABLE_COLLECTION_WRAPPER_HPP
#define REALM_JNI_IMPL_OBSERVABLE_COLLECTION_WRAPPER_HPP

#include "jni_util/java_class.hpp"
#include "jni_util/java_global_weak_ref.hpp"
#include "jni_util/java_method.hpp"
#include "jni_util/log.hpp"

#include <results.hpp>
#include <realm/util/optional.hpp>

namespace realm {
namespace _impl {

// Wrapper of Object Store List & Results.
// We need to control the life cycle of Results/List, weak ref of Java OsResults/OsList object and the NotificationToken.
// Wrap all three together, so when the Java OsResults/OsList object gets GCed, all three of them will be invalidated.
template <typename T>
class ObservableCollectionWrapper {
public:
    ObservableCollectionWrapper(T& collection)
        : m_collection_weak_ref()
        , m_notification_token()
        , m_collection(std::move(collection))
    {
    }

    ~ObservableCollectionWrapper() = default;

    ObservableCollectionWrapper(ObservableCollectionWrapper&&) = delete;
    ObservableCollectionWrapper& operator=(ObservableCollectionWrapper&&) = delete;
    ObservableCollectionWrapper(ObservableCollectionWrapper const&) = delete;
    ObservableCollectionWrapper& operator=(ObservableCollectionWrapper const&) = delete;

    T& collection()
    {
        return m_collection;
    };
    void start_listening(JNIEnv* env, jobject j_collection_object);
    void stop_listening();

private:
    jni_util::JavaGlobalWeakRef m_collection_weak_ref;
    NotificationToken m_notification_token;
    T m_collection;
};

template <typename T>
void ObservableCollectionWrapper<T>::start_listening(JNIEnv* env, jobject j_collection_object)
{
    static jni_util::JavaClass os_results_class(env, "io/realm/internal/ObservableCollection");
    static jni_util::JavaMethod notify_change_listeners(env, os_results_class, "notifyChangeListeners", "(J)V");

    if (!m_collection_weak_ref) {
        m_collection_weak_ref = jni_util::JavaGlobalWeakRef(env, j_collection_object);
    }

    auto cb = [=](CollectionChangeSet const& changes, std::exception_ptr err) {
        // OS will call all notifiers' callback in one run, so check the Java exception first!!
        if (env->ExceptionCheck())
            return;

        if (err) {
            try {
                std::rethrow_exception(err);
            }
            catch (const std::exception& e) {
                realm::jni_util::Log::e("Caught exception in collection change callback %1", e.what());
                return;
            }
        }

        m_collection_weak_ref.call_with_local_ref(env, [&](JNIEnv* local_env, jobject collection_obj) {
            local_env->CallVoidMethod(
                collection_obj, notify_change_listeners,
                reinterpret_cast<jlong>(changes.empty() ? 0 : new CollectionChangeSet(changes)));
        });
    };

    m_notification_token = m_collection.add_notification_callback(cb);
}

template <typename T>
void ObservableCollectionWrapper<T>::stop_listening()
{
    m_notification_token = {};
}

} // namespace realm
} // namespace _impl

#endif // REALM_JNI_IMPL_OBSERVABLE_COLLECTION_WRAPPER_HPP
