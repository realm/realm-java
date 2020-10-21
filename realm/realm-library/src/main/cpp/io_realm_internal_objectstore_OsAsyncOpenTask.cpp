/*
 * Copyright 2019 Realm Inc.
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

#include "io_realm_internal_objectstore_OsAsyncOpenTask.h"

#include "util.hpp"
#include <realm/object-store/thread_safe_reference.hpp>
#include "jni_util/java_method.hpp"
#include "jni_util/java_class.hpp"
#include "jni_util/jni_utils.hpp"
#include <realm/object-store/sync/async_open_task.hpp>
#include <realm/sync/config.hpp>

#include <realm/object-store/shared_realm.hpp>
#include <memory>

using namespace realm;
using namespace realm::jni_util;
using namespace realm::_impl;

JNIEXPORT jlong JNICALL Java_io_realm_internal_objectstore_OsAsyncOpenTask_start(JNIEnv* env, jobject obj, jlong config_ptr)
{
    try {

        static JavaClass java_async_open_task_class(env, "io/realm/internal/objectstore/OsAsyncOpenTask");
        static JavaMethod java_notify_realm_ready(env, java_async_open_task_class, "notifyRealmReady", "()V", false);
        static JavaMethod java_notify_error(env, java_async_open_task_class, "notifyError", "(Ljava/lang/String;)V", false);

        auto global_obj = env->NewGlobalRef(obj);
        auto& config = *reinterpret_cast<Realm::Config*>(config_ptr);

        std::shared_ptr<realm::AsyncOpenTask> task = Realm::get_synchronized_realm(config);

        auto deleter = [](jobject obj) {
            jni_util::JniUtils::get_env(true)->DeleteGlobalRef(obj);
        };
        std::shared_ptr<_jobject> task_obj(env->NewGlobalRef(global_obj), deleter);
        task->start([task=std::move(task_obj)](realm::ThreadSafeReference realm_ref, std::exception_ptr error) {
            JNIEnv* local_env = jni_util::JniUtils::get_env(true);
            if (error) {
                try {
                    std::rethrow_exception(error);
                }
                catch (const std::exception& e) {
                    jstring j_error_msg = to_jstring(local_env, e.what());
                    local_env->CallVoidMethod(task.get(), java_notify_error, j_error_msg);
                    local_env->DeleteLocalRef(j_error_msg);
                }
            }
            else {
                auto realm = Realm::get_shared_realm(std::move(realm_ref));
                realm->close();
                local_env->CallVoidMethod(task.get(), java_notify_realm_ready);
            }
            const_cast<std::shared_ptr<_jobject>&>(task).reset();
        });
        return reinterpret_cast<jlong>(&(*task));
    }
    CATCH_STD()
    return 0;
}

JNIEXPORT void JNICALL Java_io_realm_internal_objectstore_OsAsyncOpenTask_cancel(JNIEnv*, jobject, jlong task_ptr)
{
    AsyncOpenTask* task = reinterpret_cast<AsyncOpenTask*>(task_ptr);
    task->cancel();
}