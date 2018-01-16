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

// Specific override for List that do not support named callbacks for partial sync
template<>
void ObservableCollectionWrapper<List>::start_listening(JNIEnv *env, jobject j_collection_object, util::Optional<std::string>) {
    auto cb = create_callback(env, j_collection_object);
    m_notification_token = m_collection.add_notification_callback(cb);
}

// Specific override for Results that do support named callbacks
template<>
void ObservableCollectionWrapper<Results>::start_listening(JNIEnv *env, jobject j_collection_object,
                                                           util::Optional<std::string> subscription_name) {
    auto cb = create_callback(env, j_collection_object);
    m_notification_token = m_collection.add_notification_callback(cb, subscription_name);
}

} // end _impl namespace
} // end realm namespace
