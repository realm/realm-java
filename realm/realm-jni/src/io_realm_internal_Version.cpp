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

#include <sstream>
#include <string>

#include "util.hpp"
#include "io_realm_internal_Version.h"
#include <realm/version.hpp>

static int realm_jni_version = 23;


using namespace realm;

JNIEXPORT jint JNICALL Java_io_realm_internal_Version_nativeGetAPIVersion(JNIEnv*, jclass)
{
    return realm_jni_version;
}

JNIEXPORT jstring JNICALL Java_io_realm_internal_Version_nativeGetVersion(JNIEnv *env, jclass)
{
    try {
        return to_jstring(env, Version::get_version());
    }
    CATCH_STD();
    return NULL;
}

JNIEXPORT jboolean JNICALL Java_io_realm_internal_Version_nativeHasFeature(JNIEnv *env, jclass, jint feature)
{
    switch (feature) {
        case 0:
            return Version::has_feature(feature_Debug);
        case 1:
            return Version::has_feature(feature_Replication);
        default: {
            std::ostringstream ss;
            ss << "Unknown feature code: " << feature;
            ThrowException(env, RuntimeError, ss.str());
        }
    }
    return false;
}

JNIEXPORT jboolean JNICALL Java_io_realm_internal_Version_nativeIsAtLeast(JNIEnv *, jclass,
    jint major, jint minor, jint patch)
{
    return Version::is_at_least(major, minor, patch);
}
