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

#include "collection_changeset_wrapper.hpp"
#include "observable_collection_wrapper.hpp"
#include "jni_util/java_class.hpp"
#include "jni_util/java_global_weak_ref.hpp"
#include "jni_util/java_method.hpp"

#include <list.hpp>
#include <results.hpp>

using namespace realm;
using namespace realm::_impl;

namespace realm {
namespace _impl {

// Specific override for List that do not supported named callbacks for partial sync
template<>
void ObservableCollectionWrapper<List>::start_listening(JNIEnv *env, jobject j_collection_object,
                                                   util::Optional<std::string>) {
    static jni_util::JavaClass os_results_class(env, "io/realm/internal/ObservableCollection");
    static jni_util::JavaMethod notify_change_listeners(env, os_results_class,
                                                        "notifyChangeListeners", "(J)V");

    if (!m_collection_weak_ref) {
        m_collection_weak_ref = jni_util::JavaGlobalWeakRef(env, j_collection_object);
    }

    bool partial_sync_realm = m_collection.get_realm()->is_partial();
    auto cb = [=](CollectionChangeSet const &changes, std::exception_ptr err) {
        // OS will call all notifiers' callback in one run, so check the Java exception first!!
        if (env->ExceptionCheck())
            return;

        std::string error_message = "";
        if (err) {
            try {
                std::rethrow_exception(err);
            }
            catch (const std::exception &e) {
                error_message = e.what();
            }
        }

        m_collection_weak_ref.call_with_local_ref(env, [&](JNIEnv *local_env,
                                                           jobject collection_obj) {
            local_env->CallVoidMethod(
                    collection_obj, notify_change_listeners,
                    reinterpret_cast<jlong>(new CollectionChangeSetWrapper(changes,
                                                                           error_message,
                                                                           partial_sync_realm)));
        });
    };

    m_notification_token = m_collection.add_notification_callback(cb);
}

template<>
void ObservableCollectionWrapper<Results>::start_listening(JNIEnv *env, jobject j_collection_object,
                                                           util::Optional<std::string> subscription_name) {
    static jni_util::JavaClass os_results_class(env, "io/realm/internal/ObservableCollection");
    static jni_util::JavaMethod notify_change_listeners(env, os_results_class,
                                                        "notifyChangeListeners", "(J)V");

    if (!m_collection_weak_ref) {
        m_collection_weak_ref = jni_util::JavaGlobalWeakRef(env, j_collection_object);
    }

    bool partial_sync_realm = m_collection.get_realm()->is_partial();
    auto cb = [=](CollectionChangeSet const &changes, std::exception_ptr err) {
        // OS will call all notifiers' callback in one run, so check the Java exception first!!
        if (env->ExceptionCheck())
            return;

        std::string error_message = "";
        if (err) {
            try {
                std::rethrow_exception(err);
            }
            catch (const std::exception &e) {
                error_message = e.what();
            }
        }

        m_collection_weak_ref.call_with_local_ref(env, [&](JNIEnv *local_env,
                                                           jobject collection_obj) {
            local_env->CallVoidMethod(
                    collection_obj, notify_change_listeners,
                    reinterpret_cast<jlong>(new CollectionChangeSetWrapper(changes,
                                                                           error_message,
                                                                           partial_sync_realm)));
        });
    };

    m_notification_token = m_collection.add_notification_callback(cb, subscription_name);
}

}
}
