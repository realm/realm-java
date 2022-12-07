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

#include "io_realm_internal_objectstore_OsSubscription.h"

#include "java_network_transport.hpp"
#include "util.hpp"
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

static void finalize_subscription(jlong ptr) {
    delete reinterpret_cast<sync::Subscription*>(ptr);
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_objectstore_OsSubscription_nativeGetFinalizerMethodPtr(JNIEnv*, jclass) {
    return reinterpret_cast<jlong>(&finalize_subscription);
}

JNIEXPORT jstring JNICALL Java_io_realm_internal_objectstore_OsSubscription_nativeName(JNIEnv* env,
                                                                                              jclass,
                                                                                              jlong j_subscription_ptr)
{
    try {
        auto sub = reinterpret_cast<sync::Subscription*>(j_subscription_ptr);
        return to_jstring(env, sub->name);
    }
    CATCH_STD()
    return nullptr;
}

JNIEXPORT jstring JNICALL Java_io_realm_internal_objectstore_OsSubscription_nativeObjectClassName(JNIEnv* env,
                                                                                       jclass,
                                                                                       jlong j_subscription_ptr)
{
    try {
        auto sub = reinterpret_cast<sync::Subscription*>(j_subscription_ptr);
        return to_jstring(env, sub->object_class_name);
    }
    CATCH_STD()
    return nullptr;
}

JNIEXPORT jstring JNICALL Java_io_realm_internal_objectstore_OsSubscription_nativeQueryString(JNIEnv* env,
                                                                                                  jclass,
                                                                                                  jlong j_subscription_ptr)
{
    try {
        auto sub = reinterpret_cast<sync::Subscription*>(j_subscription_ptr);
        return to_jstring(env, sub->query_string);
    }
    CATCH_STD()
    return nullptr;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_objectstore_OsSubscription_nativeCreatedAt(JNIEnv* env,
                                                                                              jclass,
                                                                                              jlong j_subscription_ptr)
{
    try {
        auto sub = reinterpret_cast<sync::Subscription*>(j_subscription_ptr);
        return to_milliseconds(sub->created_at);
    }
    CATCH_STD()
    return 0;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_objectstore_OsSubscription_nativeUpdatedAt(JNIEnv* env,
                                                                                            jclass,
                                                                                            jlong j_subscription_ptr)
{
    try {
        auto sub = reinterpret_cast<sync::Subscription*>(j_subscription_ptr);
        return to_milliseconds(sub->updated_at);
    }
    CATCH_STD()
    return 0;
}
