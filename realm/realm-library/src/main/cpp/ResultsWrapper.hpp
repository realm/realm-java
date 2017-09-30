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

#ifndef REALM_JNI_IMPL_RESULTS_WRAPPER_HPP
#define REALM_JNI_IMPL_RESULTS_WRAPPER_HPP

#include <jni.h>

#include <shared_realm.hpp>
#include <results.hpp>
#include <list.hpp>

#include "java_sort_descriptor.hpp"
#include "util.hpp"
#include "java_class_global_def.hpp"

#include "jni_util/java_class.hpp"
#include "jni_util/java_global_weak_ref.hpp"
#include "jni_util/java_method.hpp"

namespace realm {
// We need to control the life cycle of Results, weak ref of Java Collection object and the NotificationToken.
// Wrap all three together, so when the Java Collection object gets GCed, all three of them will be invalidated.
struct ResultsWrapper {
    jni_util::JavaGlobalWeakRef m_collection_weak_ref;
    NotificationToken m_notification_token;
    Results m_results;

    ResultsWrapper(Results& results)
        : m_collection_weak_ref()
        , m_notification_token()
        , m_results(std::move(results))
    {
    }

    ResultsWrapper(ResultsWrapper&&) = delete;
    ResultsWrapper& operator=(ResultsWrapper&&) = delete;

    ResultsWrapper(ResultsWrapper const&) = delete;
    ResultsWrapper& operator=(ResultsWrapper const&) = delete;

    ~ResultsWrapper()
    {
    }
};
} // namespace realm

#endif // REALM_JNI_IMPL_RESULTS_WRAPPER_HPP
