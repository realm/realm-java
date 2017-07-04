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

#ifndef REALM_JNI_IMPL_EXCEPTION_DEF_HPP
#define REALM_JNI_IMPL_EXCEPTION_DEF_HPP

namespace realm {
namespace _impl {

// Definitions of Java exceptions which are used in JNI.
class JavaExceptionDef {
public:
    // Class names
    static const char* IllegalState;
    static const char* IllegalArgument;
    static const char* OutOfMemory;
    static const char* RealmMigrationNeeded;
};

} // namespace realm
} // namespace jni_impl

#endif
