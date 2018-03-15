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
#include "io_realm_internal_sync_OsSubscription.h"

#include "java_class_global_def.hpp"
#include "observable_collection_wrapper.hpp"
#include "util.hpp"
#include "subscription_wrapper.hpp"
#include "jni_util/java_class.hpp"
#include "jni_util/java_method.hpp"

#include <results.hpp>
#include <sync/partial_sync.hpp>

using namespace realm;
using namespace realm::jni_util;
using namespace realm::_impl;

typedef ObservableCollectionWrapper<Results> ResultsWrapper;

static void finalize_subscription(jlong ptr);

static void finalize_subscription(jlong ptr)
{
    TR_ENTER_PTR(ptr);
    delete reinterpret_cast<SubscriptionWrapper*>(ptr);
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_sync_OsSubscription_nativeCreate(JNIEnv* env, jclass, jlong results_ptr, jstring j_subscription_name)
{
    TR_ENTER()
    try {
        const auto results = reinterpret_cast<ResultsWrapper*>(results_ptr);
        JStringAccessor subscription_name(env, j_subscription_name);
        auto key = subscription_name.is_null_or_empty() ? util::none : util::Optional<std::string>(subscription_name);
        auto subscription = partial_sync::subscribe(results->collection(), key);
        auto wrapper = new SubscriptionWrapper(std::move(subscription));
        return reinterpret_cast<jlong>(wrapper);
    }
    CATCH_STD()
    return reinterpret_cast<jlong>(nullptr);
}


JNIEXPORT jlong JNICALL Java_io_realm_internal_sync_OsSubscription_nativeGetFinalizerPtr(JNIEnv*, jclass)
{
    TR_ENTER()
    return reinterpret_cast<jlong>(&finalize_subscription);
}

JNIEXPORT void JNICALL Java_io_realm_internal_sync_OsSubscription_nativeStartListening(JNIEnv* env, jobject object, jlong native_ptr)
{
    TR_ENTER()
    try {
        auto wrapper = reinterpret_cast<SubscriptionWrapper*>(native_ptr);
        wrapper->start_listening(env, object);
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_internal_sync_OsSubscription_nativeStopListening(JNIEnv* env, jobject, jlong native_ptr)
{
    TR_ENTER()
    try {
        auto wrapper = reinterpret_cast<SubscriptionWrapper*>(native_ptr);
        wrapper->stop_listening();
    }
    CATCH_STD()
}

JNIEXPORT jint JNICALL Java_io_realm_internal_sync_OsSubscription_nativeGetState(JNIEnv* env, jclass, jlong native_ptr)
{
    TR_ENTER()
    try {
        auto wrapper = reinterpret_cast<SubscriptionWrapper*>(native_ptr);
        return static_cast<jint>(wrapper->subscription().state());
    }
    CATCH_STD()
    return 0;
}

JNIEXPORT jobject JNICALL Java_io_realm_internal_sync_OsSubscription_nativeGetError(JNIEnv* env, jclass, jlong native_ptr)
{
    TR_ENTER()
    try {
        auto wrapper = reinterpret_cast<SubscriptionWrapper*>(native_ptr);
        auto err = wrapper->subscription().error();
        if (err) {
            std::string error_message = "";
            try {
                std::rethrow_exception(err);
            }
            catch (const std::exception &e) {
                error_message = e.what();
            }

            static JavaClass illegal_argument_class(env, "java/lang/IllegalArgumentException");
            static JavaMethod illegal_argument_constructor(env, illegal_argument_class, "<init>", "(Ljava/lang/String;)V");
            return static_cast<jthrowable>(env->NewObject(illegal_argument_class, illegal_argument_constructor, to_jstring(env, error_message)));
        }
        return nullptr;
    }
    CATCH_STD()
    return nullptr;
}
