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

#include "java_global_weak_ref.hpp"
#include "java_local_ref.hpp"

using namespace realm::jni_util;

bool JavaGlobalWeakRef::call_with_local_ref(JNIEnv* env, std::function<Callback> callback)
{
    if (!m_weak) {
        return false;
    }

    JavaLocalRef<jobject> obj(env, m_weak, need_to_create_local_ref);

    if (!obj) {
        return false;
    }
    callback(env, obj);
    return true;
}

bool JavaGlobalWeakRef::call_with_local_ref(std::function<Callback> callback)
{
    return call_with_local_ref(JniUtils::get_env(), callback);
}
