#include <sstream>
#include <string>

#include "util.hpp"
#include "io_realm_Version.h"
#include <tightdb/version.hpp>

static int tightdb_jni_version = 23;


using namespace tightdb;

JNIEXPORT jint JNICALL Java_io_realm_Version_nativeGetAPIVersion(JNIEnv*, jclass)
{
    return tightdb_jni_version;
}

JNIEXPORT jstring JNICALL Java_io_realm_Version_nativeGetVersion(JNIEnv *env, jclass)
{
    try {
        return to_jstring(env, Version::get_version());
    }
    CATCH_STD();
    return NULL;
}

JNIEXPORT jboolean JNICALL Java_io_realm_Version_nativeHasFeature(JNIEnv *env, jclass, jint feature)
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

JNIEXPORT jboolean JNICALL Java_io_realm_Version_nativeIsAtLeast(JNIEnv *, jclass,
    jint major, jint minor, jint patch)
{
    return Version::is_at_least(major, minor, patch);
}
