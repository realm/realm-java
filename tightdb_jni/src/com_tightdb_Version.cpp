#include <sstream>
#include <string>

#include "util.hpp"
#include "com_tightdb_Version.h"
#include <tightdb/version.hpp>

static int TIGHTDB_JNI_VERSION = 23;


using namespace tightdb;

JNIEXPORT jint JNICALL Java_com_tightdb_Version_nativeGetAPIVersion(JNIEnv*, jclass)
{
    return TIGHTDB_JNI_VERSION;
}

JNIEXPORT jstring JNICALL Java_com_tightdb_Version_nativeGetVersion(JNIEnv *env, jclass)
{
    try {
        return to_jstring(env, Version::get_version());
    } CATCH_STD();
    return NULL;
}

JNIEXPORT jboolean JNICALL Java_com_tightdb_Version_nativeHasFeature(JNIEnv *env, jclass, jint feature)
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

JNIEXPORT jboolean JNICALL Java_com_tightdb_Version_nativeIsAtLeast(JNIEnv *, jclass,
    jint major, jint minor, jint patch)
{
    return Version::is_at_least(major, minor, patch);
}
