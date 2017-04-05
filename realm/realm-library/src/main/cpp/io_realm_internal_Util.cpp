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

#include "jni_util/jni_utils.hpp"
#include "jni_util/hack.hpp"

#include <realm/string_data.hpp>
#include <realm/unicode.hpp>

#include "mem_usage.hpp"
#include "util.hpp"

using std::string;
using namespace realm::jni_util;

//#define USE_VLD
#if defined(_MSC_VER) && defined(_DEBUG) && defined(USE_VLD)
#include "C:\\Program Files (x86)\\Visual Leak Detector\\include\\vld.h"
#endif

const string TABLE_PREFIX("class_");


JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void*)
{
    // Workaround for some known bugs in system calls on specific devices.
    hack_init();

    JNIEnv* env;
    if (vm->GetEnv((void**)&env, JNI_VERSION_1_6) != JNI_OK) {
        return JNI_ERR;
    }
    else {
        JniUtils::initialize(vm, JNI_VERSION_1_6);
        // Loading classes and constructors for later use - used by box typed fields and a few methods' return value
        java_lang_long = GetClass(env, "java/lang/Long");
        java_lang_long_init = env->GetMethodID(java_lang_long, "<init>", "(J)V");
        java_lang_float = GetClass(env, "java/lang/Float");
        java_lang_float_init = env->GetMethodID(java_lang_float, "<init>", "(F)V");
        java_lang_double = GetClass(env, "java/lang/Double");
        java_lang_string = GetClass(env, "java/lang/String");
        java_lang_double_init = env->GetMethodID(java_lang_double, "<init>", "(D)V");
        java_util_date = GetClass(env, "java/util/Date");
        java_util_date_init = env->GetMethodID(java_util_date, "<init>", "(J)V");
    }

    return JNI_VERSION_1_6;
}

JNIEXPORT void JNI_OnUnload(JavaVM* vm, void*)
{
    JNIEnv* env;
    if (vm->GetEnv((void**)&env, JNI_VERSION_1_6) != JNI_OK) {
        return;
    }
    else {
        env->DeleteGlobalRef(java_lang_long);
        env->DeleteGlobalRef(java_lang_float);
        env->DeleteGlobalRef(java_lang_double);
        env->DeleteGlobalRef(java_util_date);
        env->DeleteGlobalRef(java_lang_string);
        JniUtils::release();
    }
}


JNIEXPORT jlong JNICALL Java_io_realm_internal_Util_nativeGetMemUsage(JNIEnv*, jclass)
{
    return static_cast<jlong>(GetMemUsage());
}

JNIEXPORT jstring JNICALL Java_io_realm_internal_Util_nativeGetTablePrefix(JNIEnv* env, jclass)
{
    realm::StringData sd(TABLE_PREFIX);
    return to_jstring(env, sd);
}
