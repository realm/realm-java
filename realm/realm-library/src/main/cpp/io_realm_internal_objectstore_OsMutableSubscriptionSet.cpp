/*
 * Copyright 2022 Realm Inc.
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

#include "io_realm_internal_objectstore_OsMutableSubscriptionSet.h"

#include "java_network_transport.hpp"
#include "util.hpp"
#include "jni_util/java_method.hpp"
#include "jni_util/jni_utils.hpp"

#include <realm/object-store/shared_realm.hpp>
#include <realm/sync/binding_callback_thread_observer.hpp>
#include <realm/object-store/sync/app.hpp>
#include <realm/object-store/sync/sync_manager.hpp>
#include <realm/sync/subscriptions.hpp>

#include <jni_util/bson_util.hpp>

using namespace realm;
using namespace realm::app;
using namespace realm::jni_util;
using namespace realm::_impl;

JNIEXPORT jlong JNICALL Java_io_realm_internal_objectstore_OsMutableSubscriptionSet_nativeInsertOrAssign(JNIEnv* env,
                                                                                              jclass,
                                                                                              jlong j_subscription_set_ptr,
                                                                                              jstring j_name,
                                                                                              jlong j_query,
                                                                                              jboolean j_throw_on_update)
{
    try {
        auto subscriptions = reinterpret_cast<sync::MutableSubscriptionSet*>(j_subscription_set_ptr);
        JStringAccessor name(env, j_name);
        auto query = reinterpret_cast<Query*>(j_query);
        std::pair<sync::SubscriptionSet::iterator, bool> result = name.is_null() ? subscriptions->insert_or_assign(*query) : subscriptions->insert_or_assign(name, *query);

        if (j_throw_on_update && !result.second) {
            ThrowException(env, ExceptionKind::IllegalArgument, "Subscription could not be added because it already existed");
            return -1;
        } else {
            return reinterpret_cast<jlong>(new sync::Subscription(*result.first));
        }
    }
    CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_objectstore_OsMutableSubscriptionSet_nativeCommit(JNIEnv* env,
                                                                                                         jclass,
                                                                                                         jlong j_subscription_set_ptr)
{
    try {
        auto subscriptions = reinterpret_cast<sync::MutableSubscriptionSet*>(j_subscription_set_ptr);
        sync::SubscriptionSet sub_set = std::move(*subscriptions).commit();
        return reinterpret_cast<jlong>(new sync::SubscriptionSet(sub_set));
    }
    CATCH_STD()
    return 0;
}

JNIEXPORT jboolean JNICALL Java_io_realm_internal_objectstore_OsMutableSubscriptionSet_nativeRemoveNamed(JNIEnv *env,
                                                                                                    jclass,
                                                                                                    jlong j_subscriptions_set_ptr,
                                                                                                    jstring j_name)
{
    try {
        auto subscriptions = reinterpret_cast<sync::MutableSubscriptionSet*>(j_subscriptions_set_ptr);
        JStringAccessor name(env, j_name);
        for (auto it = subscriptions->begin(); it != subscriptions->end(); ++it) {
            if (it->name == name) {
                subscriptions->erase(it);
                return true;
            }
        }
    }
    CATCH_STD()
    return false;
}

JNIEXPORT jboolean JNICALL Java_io_realm_internal_objectstore_OsMutableSubscriptionSet_nativeRemove(JNIEnv *env,
                                                                             jclass,
                                                                             jlong j_subscriptions_set_ptr,
                                                                             jlong j_subscription_ptr)
{
    try {
        auto subscriptions = reinterpret_cast<sync::MutableSubscriptionSet*>(j_subscriptions_set_ptr);
        auto sub = reinterpret_cast<sync::Subscription*>(j_subscription_ptr);
        for (auto it = subscriptions->begin(); it != subscriptions->end(); ++it) {
            if (it->id == sub->id) {
                subscriptions->erase(it);
                return true;
            }
        }
    }
    CATCH_STD()
    return false;
}

JNIEXPORT jboolean JNICALL Java_io_realm_internal_objectstore_OsMutableSubscriptionSet_nativeRemoveAll(JNIEnv *env,
                                                                               jclass,
                                                                               jlong j_subscriptions_set_ptr)
{
    try {
        auto subscriptions = reinterpret_cast<sync::MutableSubscriptionSet*>(j_subscriptions_set_ptr);
        bool remove = subscriptions->size() > 0;
        subscriptions->clear();
        return remove;
    }
    CATCH_STD()
    return false;
}

JNIEXPORT jboolean JNICALL Java_io_realm_internal_objectstore_OsMutableSubscriptionSet_nativeRemoveAllForType(JNIEnv *env,
                                                                                                       jclass,
                                                                                                       jlong j_subscriptions_set_ptr,
                                                                                                       jstring j_clazz_type)
{
    try {
        auto subscriptions = reinterpret_cast<sync::MutableSubscriptionSet*>(j_subscriptions_set_ptr);
        JStringAccessor type(env, j_clazz_type);
        bool remove = false;
        for (auto it = subscriptions->begin(); it != subscriptions->end(); ++it) {
            if (it->object_class_name == type) {
                it = subscriptions->erase(it);
                remove = true;
                if (it == subscriptions->end()) {
                    return remove;
                }
            }
        }
        return remove;
    }
    CATCH_STD()
    return false;
}
