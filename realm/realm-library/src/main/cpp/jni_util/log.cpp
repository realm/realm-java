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

#include <algorithm>

#include <realm/util/assert.hpp>

#include "jni_util/log.hpp"
#include "jni_util/java_local_ref.hpp"

using namespace realm;
using namespace realm::jni_util;
using namespace realm::util;

const char* Log::REALM_JNI_TAG = "REALM_JNI";
Log::Level Log::s_level = Log::Level::warn;
std::vector<CoreLoggerBridge*> CoreLoggerBridge::s_bridges;
std::mutex CoreLoggerBridge::s_mutex;

// Native wrapper for Java RealmLogger class
class JavaLogger : public JniLogger {
public:
    JavaLogger(JNIEnv* env, jobject java_logger);
    ~JavaLogger();

    bool is_same_object(JNIEnv* env, jobject java_logger);

protected:
    void log(Log::Level level, const char* tag, jthrowable throwable, const char* message) override;

private:
    JavaVM* m_jvm;
    // Global ref of the logger object.
    jobject m_java_logger;
    jmethodID m_log_method;

    inline JNIEnv* get_current_env() noexcept
    {
        JNIEnv* env;
        if (m_jvm->GetEnv((void**)&env, JNI_VERSION_1_6) != JNI_OK) {
            m_jvm->AttachCurrentThread(&env, nullptr); // Should never fail
        }
        return env;
    }
};

JniLogger::JniLogger()
    : m_is_java_logger(false)
{
}

JniLogger::JniLogger(bool is_java_logger)
    : m_is_java_logger(is_java_logger)
{
}

JavaLogger::JavaLogger(JNIEnv* env, jobject java_logger)
    : JniLogger(true)
{
    jint ret = env->GetJavaVM(&m_jvm);
    if (ret != 0) {
        throw std::runtime_error(util::format("Failed to get Java vm. Error: %d", ret));
    }
    m_java_logger = env->NewGlobalRef(java_logger);
    jclass cls = env->GetObjectClass(m_java_logger);
    m_log_method = env->GetMethodID(cls, "log", "(ILjava/lang/String;Ljava/lang/Throwable;Ljava/lang/String;)V");
}

JavaLogger::~JavaLogger()
{
    get_current_env()->DeleteGlobalRef(m_java_logger);
}

void JavaLogger::log(Log::Level level, const char* tag, jthrowable throwable, const char* message)
{
    JNIEnv* env = get_current_env();

    // NOTE: If a Java exception has been thrown in native code, the below call will trigger an JNI exception
    // "JNI called with pending exception". This is something that should be avoided when printing log in JNI --
    // Always
    // print log before calling env->ThrowNew. Doing env->ExceptionCheck() here creates overhead for normal cases.
    JavaLocalRef<jstring> java_tag(env, env->NewStringUTF(tag));
    JavaLocalRef<jstring> java_error_message(env, env->NewStringUTF(message));
    env->CallVoidMethod(m_java_logger, m_log_method, level, java_tag.get(), throwable, java_error_message.get());
}

bool JavaLogger::is_same_object(JNIEnv* env, jobject java_logger)
{
    return env->IsSameObject(m_java_logger, java_logger);
}

Log::Log()
    : m_loggers()
{
    add_logger(get_default_logger());
}

Log& Log::shared()
{
    static Log log;
    return log;
}

void Log::add_java_logger(JNIEnv* env, const jobject java_logger)
{
    std::shared_ptr<JniLogger> logger = std::make_shared<JavaLogger>(env, java_logger);
    add_logger(logger);
}

void Log::remove_java_logger(JNIEnv* env, const jobject java_logger)
{
    std::lock_guard<std::mutex> lock(m_mutex);
    m_loggers.erase(std::remove_if(m_loggers.begin(), m_loggers.end(),
                                   [&](const auto& obj) {
                                       return obj->m_is_java_logger &&
                                              std::static_pointer_cast<JavaLogger>(obj)->is_same_object(env,
                                                                                                        java_logger);
                                   }),
                    m_loggers.end());
}

void Log::add_logger(std::shared_ptr<JniLogger> logger)
{
    std::lock_guard<std::mutex> lock(m_mutex);
    if (std::find(m_loggers.begin(), m_loggers.end(), logger) == m_loggers.end()) {
        m_loggers.push_back(logger);
    }
}

void Log::remove_logger(std::shared_ptr<JniLogger> logger)
{
    std::lock_guard<std::mutex> lock(m_mutex);

    m_loggers.erase(
        std::remove_if(m_loggers.begin(), m_loggers.end(), [&](const auto& obj) { return obj == logger; }),
        m_loggers.end());
}

void Log::register_default_logger()
{
    add_logger(get_default_logger());
}

void Log::clear_loggers()
{
    std::lock_guard<std::mutex> lock(m_mutex);
    m_loggers.clear();
}

void Log::set_level(Level level)
{
    s_level = level;
    CoreLoggerBridge::set_levels(level);
}

void Log::log(Level level, const char* tag, jthrowable throwable, const char* message)
{
    if (s_level <= level) {
        std::lock_guard<std::mutex> lock(m_mutex);
        for (auto& logger : m_loggers) {
            logger->log(level, tag, throwable, message);
        }
    }
}

realm::util::RootLogger::Level Log::convert_to_core_log_level(Level level)
{
        switch (level) {
            case Log::trace:
                return RootLogger::Level::trace;
            case Log::debug:
                return RootLogger::Level::debug;
            case Log::info:
                return RootLogger::Level::info;
            case Log::warn:
                return RootLogger::Level::warn;
            case Log::error:
                return RootLogger::Level::error;
            case Log::fatal:
                return RootLogger::Level::fatal;
            case Log::all:
                return RootLogger::Level::all;
            case Log::off:
                return RootLogger::Level::off;
            default:
                break;
        }
        REALM_UNREACHABLE();
}

CoreLoggerBridge::CoreLoggerBridge(std::string tag)
    : m_tag(std::move(tag))
{
    std::lock_guard<std::mutex> lock(s_mutex);
    s_bridges.push_back(this);
    set_level_threshold(Log::convert_to_core_log_level(Log::shared().get_level()));
}

CoreLoggerBridge::~CoreLoggerBridge()
{
    std::lock_guard<std::mutex> lock(s_mutex);
    s_bridges.erase(std::remove(s_bridges.begin(), s_bridges.end(), this), s_bridges.end());
}

void CoreLoggerBridge::set_levels(Log::Level level)
{
    std::lock_guard<std::mutex> lock(s_mutex);
    for (auto bridge : s_bridges) {
        bridge->set_level_threshold(Log::convert_to_core_log_level(level));
    }
}

void CoreLoggerBridge::do_log(realm::util::Logger::Level level, std::string msg)
{
    // Ignore the level threshold from the root logger.
    Log::Level jni_level = Log::all; // Initial value to suppress the false positive compile warning.
    switch (level) {
        case Level::trace:
            jni_level = Log::trace;
            break;
        case Level::debug: // Fall through. Map to same level debug.
        case Level::detail:
            jni_level = Log::debug;
            break;
        case Level::info:
            jni_level = Log::info;
            break;
        case Level::warn:
            jni_level = Log::warn;
            break;
        case Level::error:
            jni_level = Log::error;
            break;
        case Level::fatal:
            jni_level = Log::fatal;
            break;
        case Level::all: // Fall through.
        case Level::off: // Fall through.
            throw std::invalid_argument(format("Invalid log level."));
    }
    Log::shared().log(jni_level, m_tag.c_str(), msg.c_str());
}
