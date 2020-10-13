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

#ifndef REALM_JNI_UTIL_JAVA_GLOBAL_REF_COPY_HPP
#define REALM_JNI_UTIL_JAVA_GLOBAL_REF_COPY_HPP

#include <jni.h>

namespace realm {
    namespace jni_util {

        // Manages the lifecycle of jobject's global ref via copy constructors
        //
        // It prevents leaking global references by automatically referencing and unreferencing Java objects
        // any time the instance is copied or destroyed. Its principal use is on data structures that don't support
        // moving operations such as in std::function lambdas.
        //
        // Note that there is another flavor available: JavaGlobalRefByMove.
        //
        // JavaGlobalRefByCopy: multiple references will exist to the Java object, one on each instance.
        // JavaGlobalRefByMove: only one reference will only be available at last moved instance.

        class JavaGlobalRefByCopy {
        public:
            JavaGlobalRefByCopy();

            JavaGlobalRefByCopy(JNIEnv *env, jobject obj);

            JavaGlobalRefByCopy(const JavaGlobalRefByCopy &rhs);

            JavaGlobalRefByCopy(JavaGlobalRefByCopy&& rhs) = delete;

            ~JavaGlobalRefByCopy();

            jobject get() const noexcept;

        private:
            jobject m_ref;
        };
    }
}

#endif // REALM_JNI_UTIL_JAVA_GLOBAL_REF_COPY_HPP
