/*
 * Copyright 2016 Realm Inc.
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

#ifndef REALM_JNI_IMPL_ANDROID_LOGGER_HPP
#define REALM_JNI_IMPL_ANDROID_LOGGER_HPP

#include <android/log.h>
#include "jni_util/log.hpp"

namespace realm {
namespace jni_impl {

// Default logger implementation for Android.
class AndroidLogger : public realm::jni_util::JniLogger {
public:
    static std::shared_ptr<AndroidLogger> shared();

protected:
    void log(realm::jni_util::Log::Level level, const char* tag, jthrowable throwable, const char* message) override;

private:
    AndroidLogger(){};
    static void print(android_LogPriority priority, const char* tag, const char* log_string);
    static const size_t LOG_ENTRY_MAX_LENGTH = 4000;
};
}
}

#endif // REALM_JNI_IMPL_ANDROID_LOGGER_HPP
