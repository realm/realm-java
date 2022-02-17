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

#include "jni_utils.hpp"
#include "java_class.hpp"
#include "java_method.hpp"

#include <realm/util/assert.hpp>

#include <memory>

using namespace realm::jni_util;
using namespace std;

static std::unique_ptr<JniUtils> s_instance;

void JniUtils::initialize(JavaVM* vm, jint vm_version) noexcept
{
    REALM_ASSERT_DEBUG(!s_instance);

    s_instance = std::unique_ptr<JniUtils>(new JniUtils(vm, vm_version));
}

void JniUtils::release()
{
    REALM_ASSERT_DEBUG(s_instance);
    s_instance.release();
}

JNIEnv* JniUtils::get_env(bool attach_if_needed)
{
    REALM_ASSERT_DEBUG(s_instance);

    JNIEnv* env;
    if (s_instance->m_vm->GetEnv(reinterpret_cast<void**>(&env), s_instance->m_vm_version) != JNI_OK) {
        if (attach_if_needed) {
            jint ret = s_instance->m_vm->AttachCurrentThread(&env, nullptr);
            REALM_ASSERT_RELEASE(ret == JNI_OK);
        }
        else {
            REALM_ASSERT_RELEASE(false);
        }
    }

    return env;
}

void JniUtils::detach_current_thread()
{
    s_instance->m_vm->DetachCurrentThread();
}

void JniUtils::keep_global_ref(JavaGlobalRefByMove& ref)
{
    s_instance->m_global_refs.push_back(std::move(ref));
}

jobject JniUtils::to_hash_map(JNIEnv* env, std::map<std::string, std::string> map)
{
    static JavaClass hash_map_class(env, "java/util/HashMap");
    static JavaMethod hash_map_constructor(env, hash_map_class, "<init>", "(I)V");
    static JavaMethod hash_map_put(env, hash_map_class, "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");

    jobject hash_map = env->NewObject(hash_map_class, hash_map_constructor, (jint) map.size());

    for (const auto& it : map)
    {
        jstring key = env->NewStringUTF(it.first.c_str());
        jstring value = env->NewStringUTF(it.second.c_str());

        env->CallObjectMethod(hash_map, hash_map_put,
                              key,
                              value);

        env->DeleteLocalRef(key);
        env->DeleteLocalRef(value);
    }

    return hash_map;
}

