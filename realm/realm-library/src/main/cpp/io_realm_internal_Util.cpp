/*
 * Copyright 2014 Realm Inc.
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

#include <jni.h>

#include <realm/string_data.hpp>
#include <realm/unicode.hpp>

#include "io_realm_log_LogLevel.h"
#include "mem_usage.hpp"
#include "util.hpp"

using std::string;

//#define USE_VLD
#if defined(_MSC_VER) && defined(_DEBUG) && defined(USE_VLD)
    #include "C:\\Program Files (x86)\\Visual Leak Detector\\include\\vld.h"
#endif

// used by logging
int trace_level = 0;
jclass realmlog_class;
jmethodID log_trace;
jmethodID log_debug;
jmethodID log_info;
jmethodID log_warn;
jmethodID log_error;
jmethodID log_fatal;

const string TABLE_PREFIX("class_");


JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void*)
{
    JNIEnv* env;
    if (vm->GetEnv((void **) &env, JNI_VERSION_1_6) != JNI_OK) {
        return JNI_ERR;
    }
    else {
        // Loading classes and constructors for later use - used by box typed fields and a few methods' return value
        java_lang_long        = GetClass(env, "java/lang/Long");
        java_lang_long_init   = env->GetMethodID(java_lang_long, "<init>", "(J)V");
        java_lang_float       = GetClass(env, "java/lang/Float");
        java_lang_float_init  = env->GetMethodID(java_lang_float, "<init>", "(F)V");
        java_lang_double      = GetClass(env, "java/lang/Double");
        java_lang_double_init = env->GetMethodID(java_lang_double, "<init>", "(D)V");
        realmlog_class        = GetClass(env, "io/realm/log/RealmLog");
        log_trace             = env->GetStaticMethodID(realmlog_class, "trace", "(Ljava/lang/String;[Ljava/lang/Object;)V");
        log_debug             = env->GetStaticMethodID(realmlog_class, "debug", "(Ljava/lang/String;[Ljava/lang/Object;)V");
        log_info              = env->GetStaticMethodID(realmlog_class, "info", "(Ljava/lang/String;[Ljava/lang/Object;)V");
        log_warn              = env->GetStaticMethodID(realmlog_class, "warn", "(Ljava/lang/String;[Ljava/lang/Object;)V");
        log_error             = env->GetStaticMethodID(realmlog_class, "error", "(Ljava/lang/String;[Ljava/lang/Object;)V");
        log_fatal             = env->GetStaticMethodID(realmlog_class, "fatal", "(Ljava/lang/String;[Ljava/lang/Object;)V");
    }

    return JNI_VERSION_1_6;
}

JNIEXPORT void JNI_OnUnload(JavaVM* vm, void*)
{
    JNIEnv* env;
    if (vm->GetEnv((void **) &env, JNI_VERSION_1_6) != JNI_OK) {
        return;
    }
    else {
        env->DeleteGlobalRef(java_lang_long);
        env->DeleteGlobalRef(java_lang_float);
        env->DeleteGlobalRef(java_lang_double);
    }
}

JNIEXPORT void JNICALL Java_io_realm_internal_Util_nativeSetDebugLevel(JNIEnv*, jclass, jint level)
{
    /**
     * level should match one of the levels defined in LogLevel.java
     * ALL = 1
     * TRACE = 2
     * DEBUG = 3
     * INFO = 4
     * WARN = 5
     * ERROR = 6
     * FATAL = 7
     * OFF = 8
     */
    trace_level = level;
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_Util_nativeGetMemUsage(JNIEnv*, jclass)
{
    return GetMemUsage();
}

JNIEXPORT jstring JNICALL Java_io_realm_internal_Util_nativeGetTablePrefix(
    JNIEnv* env, jclass)
{
    realm::StringData sd(TABLE_PREFIX);
    return to_jstring(env, sd);
}
