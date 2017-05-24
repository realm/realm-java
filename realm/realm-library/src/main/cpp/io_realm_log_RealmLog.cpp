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

#include "io_realm_log_RealmLog.h"
#include "jni_util/log.hpp"
#include "util.hpp"

using namespace realm::util;
using namespace realm::jni_util;

JNIEXPORT void JNICALL Java_io_realm_log_RealmLog_nativeAddLogger(JNIEnv* env, jclass, jobject java_logger)
{
    try {
        Log::shared().add_java_logger(env, java_logger);
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_log_RealmLog_nativeRemoveLogger(JNIEnv* env, jclass, jobject java_logger)
{
    try {
        Log::shared().remove_java_logger(env, java_logger);
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_log_RealmLog_nativeClearLoggers(JNIEnv* env, jclass)
{
    try {
        Log::shared().clear_loggers();
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_log_RealmLog_nativeRegisterDefaultLogger(JNIEnv* env, jclass)
{
    try {
        Log::shared().register_default_logger();
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_log_RealmLog_nativeLog(JNIEnv* env, jclass, jint level, jstring tag,
                                                            jthrowable throwable, jstring message)
{
    try {
        JStringAccessor tag_accessor(env, tag);
        JStringAccessor message_accessor(env, message);
        Log::shared().log(static_cast<Log::Level>(level), std::string(tag_accessor).c_str(), throwable,
                          std::string(message_accessor).c_str());
    }
    CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_log_RealmLog_nativeSetLogLevel(JNIEnv* env, jclass, jint level)
{
    try {
        Log::shared().set_level(static_cast<Log::Level>(level));
    }
    CATCH_STD()
}

JNIEXPORT jint JNICALL Java_io_realm_log_RealmLog_nativeGetLogLevel(JNIEnv* env, jclass)
{
    try {
        return static_cast<jint>(Log::shared().get_level());
    }
    CATCH_STD()

    return static_cast<jint>(Log::Level::all);
}

// Methods for testing only.
JNIEXPORT jlong JNICALL Java_io_realm_log_RealmLog_nativeCreateCoreLoggerBridge(JNIEnv* env, jclass, jstring tag)
{
    return reinterpret_cast<jlong>(new CoreLoggerBridge(JStringAccessor(env, tag)));
}

JNIEXPORT void JNICALL Java_io_realm_log_RealmLog_nativeCloseCoreLoggerBridge(JNIEnv*, jclass, jlong native_ptr)
{
    delete reinterpret_cast<CoreLoggerBridge*>(native_ptr);
}

JNIEXPORT void JNICALL Java_io_realm_log_RealmLog_nativeLogToCoreLoggerBridge(JNIEnv* env, jclass, jlong native_ptr,
                                                                              jint level, jstring msg)
{
    CoreLoggerBridge* bridge = reinterpret_cast<CoreLoggerBridge*>(native_ptr);
    std::string message = JStringAccessor(env, msg);
    bridge->log(Log::convert_to_core_log_level(static_cast<Log::Level>(level)), message.c_str());
}
