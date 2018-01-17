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

#include <cstring>

#include "android_logger.hpp"

using namespace realm;
using namespace realm::jni_util;
using namespace realm::jni_impl;
using namespace realm::util;

std::shared_ptr<AndroidLogger> AndroidLogger::shared()
{
    // Private constructor, make_shared is not available.
    static std::shared_ptr<AndroidLogger> android_logger(new AndroidLogger());
    return android_logger;
}

void AndroidLogger::log(Log::Level level, const char* tag, jthrowable, const char* message)
{
    android_LogPriority android_log_priority;
    switch (level) {
        case Log::Level::trace:
            android_log_priority = ANDROID_LOG_VERBOSE;
            break;
        case Log::Level::debug:
            android_log_priority = ANDROID_LOG_DEBUG;
            break;
        case Log::Level::info:
            android_log_priority = ANDROID_LOG_INFO;
            break;
        case Log::Level::warn:
            android_log_priority = ANDROID_LOG_WARN;
            break;
        case Log::Level::error:
            android_log_priority = ANDROID_LOG_ERROR;
            break;
        case Log::Level::fatal:
            android_log_priority = ANDROID_LOG_FATAL;
            break;
        default: // Cannot get here.
            throw std::invalid_argument(format("Invalid log level: %1.", level));
    }
    if (message) {
        print(android_log_priority, tag, message);
    }
}

void AndroidLogger::print(android_LogPriority priority, const char* tag, const char* log_string)
{
    size_t log_size = strlen(log_string);

    if (log_size > LOG_ENTRY_MAX_LENGTH) {
        size_t start = 0;

        while (start < log_size) {
            size_t count = log_size - start > LOG_ENTRY_MAX_LENGTH ? LOG_ENTRY_MAX_LENGTH : log_size - start;
            std::string tmp_str(log_string, start, count);
            __android_log_write(priority, tag, tmp_str.c_str());
            start += count;
        }
    }
    else {
        __android_log_write(priority, tag, log_string);
    }
}

namespace realm {
namespace jni_util {

std::shared_ptr<JniLogger> get_default_logger()
{
    return std::static_pointer_cast<JniLogger>(AndroidLogger::shared());
}
}
}
