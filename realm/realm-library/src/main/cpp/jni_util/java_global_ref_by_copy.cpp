/*
 * Copyright 2020 Realm Inc.
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

#include "java_global_ref_by_copy.hpp"
#include "jni_utils.hpp"

#include <memory>

using namespace realm::jni_util;

JavaGlobalRefByCopy::JavaGlobalRefByCopy() : m_ref(nullptr) {

}

JavaGlobalRefByCopy::JavaGlobalRefByCopy(JNIEnv *env, jobject obj)
        : m_ref(obj ? env->NewGlobalRef(obj) : nullptr) {
}

JavaGlobalRefByCopy::JavaGlobalRefByCopy(const JavaGlobalRefByCopy &rhs)
        : m_ref(rhs.m_ref ? jni_util::JniUtils::get_env(true)->NewGlobalRef(rhs.m_ref) : nullptr) {
}

JavaGlobalRefByCopy::~JavaGlobalRefByCopy() {
    if (m_ref) {
        JniUtils::get_env()->DeleteGlobalRef(m_ref);
    }
}

jobject JavaGlobalRefByCopy::get() const noexcept {
    return m_ref;
}
