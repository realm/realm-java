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

#ifndef REALM_JNI_IMPL_COLLECTION_CHANGESET_WRAPPER_HPP
#define REALM_JNI_IMPL_COLLECTION_CHANGESET_WRAPPER_HPP

#include "collection_notifications.hpp"
#include "util.hpp"
#include "jni_util/java_class.hpp"
#include "jni_util/java_global_weak_ref.hpp"
#include "jni_util/java_method.hpp"
#include "jni_util/log.hpp"
#include "jni_util/jni_utils.hpp"
#include "jni_util/java_class.hpp"
#include "jni_util/java_method.hpp"
#include "sync/partial_sync.hpp"
#include "object-store/src/subscription_state.hpp"

using namespace realm::jni_util;

namespace realm {
namespace _impl {

// Wrapper of Object Store CollectionChangeSet
// It is used to better control the mapping between Object Store concepts and Java API's, especially
// when it comes to states and defining errors.
class CollectionChangeSetWrapper {
public:
    CollectionChangeSetWrapper(CollectionChangeSet const& changeset, std::string error_message)
        : m_changeset(changeset)
        , m_error_message(error_message)
    {
    }

    ~CollectionChangeSetWrapper() = default;

    CollectionChangeSetWrapper(CollectionChangeSetWrapper&&) = delete;
    CollectionChangeSetWrapper& operator=(CollectionChangeSetWrapper&&) = delete;
    CollectionChangeSetWrapper(CollectionChangeSetWrapper const&) = delete;
    CollectionChangeSetWrapper& operator=(CollectionChangeSetWrapper const&) = delete;

    CollectionChangeSet& get()
    {
        return m_changeset;
    };

    jthrowable get_error() {
        JNIEnv* env = JniUtils::get_env(false);
        if (m_error_message != "") {
            static JavaClass realm_exception_class(env, "io/realm/exceptions/RealmException");
            static JavaMethod realm_exception_constructor(env, realm_exception_class, "<init>", "(Ljava/lang/String;)V");
            return (jthrowable) env->NewObject(realm_exception_class, realm_exception_constructor, to_jstring(env, m_error_message));
        } else if (m_changeset.partial_sync_error_message != "") {
            // Indicates a soft error, i.e. illegal name of query.
            static JavaClass illegal_argument_class(env, "java/lang/IllegalArgumentException");
            static JavaMethod illegal_argument_constructor(env, illegal_argument_class, "<init>", "(Ljava/lang/String;)V");
            return (jthrowable) env->NewObject(illegal_argument_class, illegal_argument_constructor, to_jstring(env, m_changeset.partial_sync_error_message));
        } else {
            return nullptr;
        }
    }

    bool is_remote_data_loaded() {
        return m_changeset.partial_sync_new_state == partial_sync::SubscriptionState::Initialized;
    }


private:
    CollectionChangeSet m_changeset;
    std::string m_error_message; // From any exception being thrown that are not reported using Partial Sync
};


} // namespace realm
} // namespace _impl

#endif // REALM_JNI_IMPL_COLLECTION_CHANGESET_WRAPPER_HPP
