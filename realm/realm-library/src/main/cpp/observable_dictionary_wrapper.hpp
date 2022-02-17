/*
 * Copyright 2021 Realm Inc.
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

#ifndef REALM_JNI_IMPL_OBSERVABLE_DICTIONARY_WRAPPER_HPP
#define REALM_JNI_IMPL_OBSERVABLE_DICTIONARY_WRAPPER_HPP

#include "jni_util/java_class.hpp"
#include "jni_util/java_global_weak_ref.hpp"
#include "jni_util/java_method.hpp"
#include "jni_util/log.hpp"

#include <realm/object-store/results.hpp>
#include <realm/util/optional.hpp>
#include <io_realm_internal_OsMapChangeSet.h>

namespace realm {
namespace _impl {

// Wrapper of Object Store Dictionary.
// We need to control the life cycle of Dictionary, weak ref of Java ObservableMap object and the NotificationToken.
// Wrap all three together, so when the Java ObservableMap object gets GCed, all three of them will be invalidated.
class ObservableDictionaryWrapper {
public:
    ObservableDictionaryWrapper(object_store::Dictionary& collection)
            : m_collection_weak_ref()
            , m_notification_token()
            , m_collection(std::move(collection))
    {
    }

    ~ObservableDictionaryWrapper() = default;

    ObservableDictionaryWrapper(ObservableDictionaryWrapper&&) = delete;
    ObservableDictionaryWrapper& operator=(ObservableDictionaryWrapper&&) = delete;
    ObservableDictionaryWrapper(ObservableDictionaryWrapper const&) = delete;
    ObservableDictionaryWrapper& operator=(ObservableDictionaryWrapper const&) = delete;

    object_store::Dictionary& collection()
    {
        return m_collection;
    };
    void start_listening(JNIEnv* env, jobject j_collection_object);
    void stop_listening();

private:
    jni_util::JavaGlobalWeakRef m_collection_weak_ref;
    NotificationToken m_notification_token;
    object_store::Dictionary m_collection;
};

void ObservableDictionaryWrapper::start_listening(JNIEnv* env, jobject j_observable_map)
{
    static jni_util::JavaClass os_map_class(env, "io/realm/internal/ObservableMap");
    static jni_util::JavaMethod notify_change_listeners(env, os_map_class, "notifyChangeListeners", "(J)V");

    if (!m_collection_weak_ref) {
        m_collection_weak_ref = jni_util::JavaGlobalWeakRef(env, j_observable_map);
    }

    auto cb = [=](DictionaryChangeSet changes, std::exception_ptr err) {
        // OS will call all notifiers' callback in one run, so check the Java exception first!!
        if (env->ExceptionCheck())
            return;

        if (err) {
            try {
                std::rethrow_exception(err);
            }
            catch (const std::exception& e) {
                realm::jni_util::Log::e("Caught exception in dictionary change callback %1", e.what());
                return;
            }
        }

        m_collection_weak_ref.call_with_local_ref(env, [&](JNIEnv* local_env, jobject collection_obj) {
            bool changes_empty = changes.deletions.empty() &&
                                 changes.insertions.empty() &&
                                 changes.modifications.empty();

            local_env->CallVoidMethod(
                    collection_obj,
                    notify_change_listeners,
                    reinterpret_cast<jlong>(changes_empty ? io_realm_internal_OsMapChangeSet_EMPTY_CHANGESET : new DictionaryChangeSet(changes)));
        });
    };
    m_notification_token = m_collection.add_key_based_notification_callback(cb);
}

void ObservableDictionaryWrapper::stop_listening()
{
    m_notification_token = {};
}

} // namespace realm
} // namespace _impl

#endif // REALM_JNI_IMPL_OBSERVABLE_DICTIONARY_WRAPPER_HPP
