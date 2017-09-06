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
#include "java_class_global_def.hpp"

#include <realm/string_data.hpp>
#include <realm/unicode.hpp>

#include "util.hpp"

using std::string;
using namespace realm::jni_util;
using namespace realm::_impl;

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
        JavaClassGlobalDef::initialize(env);
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
        JavaClassGlobalDef::release();
        JniUtils::release();
    }
}

JNIEXPORT jstring JNICALL Java_io_realm_internal_Util_nativeGetTablePrefix(JNIEnv* env, jclass)
{
    realm::StringData sd(TABLE_PREFIX);
    return to_jstring(env, sd);
}
