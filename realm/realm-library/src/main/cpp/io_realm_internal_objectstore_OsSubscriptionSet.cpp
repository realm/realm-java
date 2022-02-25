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

#include "io_realm_internal_objectstore_OsSubscriptionSet.h"

#include "java_network_transport.hpp"
#include "util.hpp"
#include "jni_util/java_global_weak_ref.hpp"
#include "jni_util/java_method.hpp"
#include "jni_util/jni_utils.hpp"

#include <realm/object-store/shared_realm.hpp>
#include <realm/object-store/binding_callback_thread_observer.hpp>
#include <realm/object-store/sync/app.hpp>
#include <realm/object-store/sync/sync_manager.hpp>
#include <realm/sync/subscriptions.hpp>

#include <jni_util/bson_util.hpp>

using namespace realm;
using namespace realm::app;
using namespace realm::jni_util;
using namespace realm::_impl;

static void finalize_subscription_set(jlong ptr) {
    delete reinterpret_cast<sync::SubscriptionSet*>(ptr);
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_objectstore_OsSubscriptionSet_nativeGetFinalizerMethodPtr(JNIEnv*, jclass) {
    return reinterpret_cast<jlong>(&finalize_subscription_set);
}

JNIEXPORT void JNICALL Java_io_realm_internal_objectstore_OsSubscriptionSet_nativeRelease(JNIEnv*, jclass, jlong j_subscription_set_ptr) {
    delete reinterpret_cast<sync::SubscriptionSet*>(j_subscription_set_ptr);
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_objectstore_OsSubscriptionSet_nativeSize(JNIEnv* env, jclass, jlong j_subscription_set_ptr)
{
    try {
        auto subscriptions = reinterpret_cast<sync::SubscriptionSet*>(j_subscription_set_ptr);
        return subscriptions->size();
    }
    CATCH_STD()
    return 0;
}

JNIEXPORT jbyte JNICALL Java_io_realm_internal_objectstore_OsSubscriptionSet_nativeState(JNIEnv* env, jclass, jlong j_subscription_set_ptr)
{
    try {
        auto subscriptions = reinterpret_cast<sync::SubscriptionSet*>(j_subscription_set_ptr);
        sync::SubscriptionSet::State state = subscriptions->state();
        switch(state) {
            case sync::SubscriptionSet::State::Uncommitted:
                return io_realm_internal_objectstore_OsSubscriptionSet_STATE_VALUE_UNCOMMITTED;
            case sync::SubscriptionSet::State::Pending:
                return io_realm_internal_objectstore_OsSubscriptionSet_STATE_VALUE_PENDING;
            case sync::SubscriptionSet::State::Bootstrapping:
                return io_realm_internal_objectstore_OsSubscriptionSet_STATE_VALUE_BOOTSTRAPPING;
            case sync::SubscriptionSet::State::Complete:
                return io_realm_internal_objectstore_OsSubscriptionSet_STATE_VALUE_COMPLETE;
            case sync::SubscriptionSet::State::Error:
                return io_realm_internal_objectstore_OsSubscriptionSet_STATE_VALUE_ERROR;
            case sync::SubscriptionSet::State::Superseded:
                return io_realm_internal_objectstore_OsSubscriptionSet_STATE_VALUE_SUPERSEDED;
        }
    }
    CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_objectstore_OsSubscriptionSet_nativeCreateMutableSubscriptionSet(JNIEnv* env, jclass, jlong j_subscription_set_ptr)
{
    try {
        auto subscriptions = reinterpret_cast<sync::SubscriptionSet*>(j_subscription_set_ptr);
        return reinterpret_cast<jlong>(new sync::MutableSubscriptionSet(subscriptions->make_mutable_copy()));
    }
    CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_objectstore_OsSubscriptionSet_nativeSubscriptionAt(JNIEnv *env, jclass,
                                                                          jlong j_subscription_set_ptr,
                                                                          jint j_index)
{
    try {
        auto subscriptions = reinterpret_cast<sync::SubscriptionSet*>(j_subscription_set_ptr);
        return reinterpret_cast<jlong>(new sync::Subscription(subscriptions->at(j_index)));
    }
    CATCH_STD()
    return 0;
}

JNIEXPORT void JNICALL Java_io_realm_internal_objectstore_OsSubscriptionSet_nativeWaitForSynchronization(JNIEnv *env,
                                                                                  jclass,
                                                                                  jlong j_subscription_set_ptr,
                                                                                  jobject j_callback)
{
    try {
        auto subscriptions = reinterpret_cast<sync::SubscriptionSet *>(j_subscription_set_ptr);
        util::Future<sync::SubscriptionSet::State> result = subscriptions->get_state_change_notification(
                sync::SubscriptionSet::State::Complete);

        static JavaClass callback_class(env, "io/realm/internal/objectstore/OsSubscriptionSet$StateChangeCallback");
        static JavaMethod onchange_method(env, callback_class, "onChange", "(B)V", false);
        JavaGlobalWeakRef j_callback_weak(env, j_callback);
        std::move(result).get_async([j_callback_weak](StatusOrStatusWith<sync::SubscriptionSet::State> status) noexcept {
            JNIEnv* env = JniUtils::get_env(false);
            j_callback_weak.call_with_local_ref(env, [&](JNIEnv* env, jobject obj) {
                if (status.is_ok()) {
                    env->CallVoidMethod(obj, onchange_method, static_cast<jbyte>(status.get_value()));
                } else {
                    env->CallVoidMethod(obj, onchange_method, static_cast<jbyte>(sync::SubscriptionSet::State::Error));
                }
            });
        });
    }
    CATCH_STD()
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_objectstore_OsSubscriptionSet_nativeFindByName(JNIEnv *env, jclass,
                                                                      jlong j_subscription_set_ptr,
                                                                      jstring j_name)
{
    try {
        auto subscriptions = reinterpret_cast<sync::SubscriptionSet*>(j_subscription_set_ptr);
        JStringAccessor name(env, j_name);
        sync::SubscriptionSet::const_iterator iter = subscriptions->find(name);
        if (iter != subscriptions->end()) {
            return reinterpret_cast<jlong>(new sync::Subscription(std::move(*iter)));
        } else {
            return -1;
        }
    }
    CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_objectstore_OsSubscriptionSet_nativeFindByQuery(JNIEnv *env, jclass,
                                                                       jlong j_subscription_set_ptr,
                                                                       jlong j_query_ptr)
{
    try {
        auto subscriptions = reinterpret_cast<sync::SubscriptionSet*>(j_subscription_set_ptr);
        auto query = reinterpret_cast<Query*>(j_query_ptr);
        sync::SubscriptionSet::const_iterator iter = subscriptions->find(*query);
        if (iter != subscriptions->end()) {
            return reinterpret_cast<jlong>(new sync::Subscription(std::move(*iter)));
        } else {
            return -1;
        }
    }
    CATCH_STD()
    return 0;
}

JNIEXPORT jstring JNICALL Java_io_realm_internal_objectstore_OsSubscriptionSet_nativeErrorMessage(JNIEnv *env, jclass,
                                                                        jlong j_subscription_set_ptr)
{
    try {
        auto subscriptions = reinterpret_cast<sync::SubscriptionSet*>(j_subscription_set_ptr);
        return to_jstring(env, subscriptions->error_str());
    }
    CATCH_STD()
    return nullptr;
}

JNIEXPORT void JNICALL
Java_io_realm_internal_objectstore_OsSubscriptionSet_nativeRefresh(JNIEnv *env, jclass, jlong j_subscription_set_ptr) {
    try {
        auto subscriptions = reinterpret_cast<sync::SubscriptionSet *>(j_subscription_set_ptr);
        subscriptions->refresh();
    }
    CATCH_STD()
}
