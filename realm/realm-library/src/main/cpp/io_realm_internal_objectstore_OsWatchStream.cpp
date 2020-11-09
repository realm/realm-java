/*
 * Copyright 2020 Realm Inc.
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

#include "io_realm_internal_objectstore_OsWatchStream.h"

#include "java_class_global_def.hpp"
#include "jni_util/bson_util.hpp"

#include <realm/object-store/sync/mongo_collection.hpp>

using namespace realm;
using namespace realm::app;
using namespace realm::bson;
using namespace realm::jni_util;
using namespace realm::_impl;

static void finalize_watchstream(jlong ptr) {
    delete reinterpret_cast<WatchStream *>(ptr);
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_objectstore_OsWatchStream_nativeGetFinalizerMethodPtr(JNIEnv *, jclass) {
    return reinterpret_cast<jlong>(&finalize_watchstream);
}

JNIEXPORT jlong JNICALL
Java_io_realm_internal_objectstore_OsWatchStream_nativeCreateWatchStream(JNIEnv *env, jclass) {
    try {
        return (jlong) new WatchStream();
    }
    CATCH_STD()

    return 0;
}

JNIEXPORT void JNICALL
Java_io_realm_internal_objectstore_OsWatchStream_nativeFeedLine(JNIEnv *env, jclass,
                                                                jlong j_watch_stream_ptr,
                                                                jstring j_line) {
    try {
        WatchStream *watch_stream = reinterpret_cast<WatchStream *>(j_watch_stream_ptr);
        JStringAccessor line(env, j_line);

        watch_stream->feed_line(std::string(line));
    }
    CATCH_STD()
}

JNIEXPORT jstring JNICALL
Java_io_realm_internal_objectstore_OsWatchStream_nativeGetState(JNIEnv *env, jclass,
                                                                jlong j_watch_stream_ptr) {
    try {
        WatchStream *watch_stream = reinterpret_cast<WatchStream *>(j_watch_stream_ptr);

        switch (watch_stream->state()) {
            case WatchStream::NEED_DATA:
                return env->NewStringUTF("NEED_DATA");
            case WatchStream::HAVE_EVENT:
                return env->NewStringUTF("HAVE_EVENT");
            case WatchStream::HAVE_ERROR:
                return env->NewStringUTF("HAVE_ERROR");
        }
    }
    CATCH_STD()

    return nullptr;
}

JNIEXPORT jstring JNICALL
Java_io_realm_internal_objectstore_OsWatchStream_nativeGetNextEvent(JNIEnv *env, jclass,
                                                                    jlong j_watch_stream_ptr) {
    try {
        WatchStream *watch_stream = reinterpret_cast<WatchStream *>(j_watch_stream_ptr);
        return JniBsonProtocol::bson_to_jstring(env, watch_stream->next_event());
    }
    CATCH_STD()

    return nullptr;
}


JNIEXPORT jthrowable JNICALL
Java_io_realm_internal_objectstore_OsWatchStream_nativeGetError(JNIEnv *env, jclass,
                                                                jlong j_watch_stream_ptr) {
    try {
        WatchStream *watch_stream = reinterpret_cast<WatchStream *>(j_watch_stream_ptr);

        auto app_error = watch_stream->error();

        jstring error_code_category = env->NewStringUTF(app_error.error_code.category().name());
        jstring error_code_message = env->NewStringUTF(app_error.error_code.message().c_str());

        jstring app_error_message = env->NewStringUTF(app_error.message.c_str());

        static JavaClass app_exception_class(env, "io/realm/mongodb/AppException");
        static JavaMethod app_exception_constructor(env, app_exception_class, "<init>",
                                                    "(Lio/realm/mongodb/ErrorCode;Ljava/lang/String;)V");

        static JavaClass error_code_class(env, "io/realm/mongodb/ErrorCode");
        static JavaMethod error_code_constructor(env, error_code_class, "fromNativeError",
                                                 "(Ljava/lang/String;I)Lio/realm/mongodb/ErrorCode;",
                                                 true);

        jobject j_error_code = env->CallStaticObjectMethod(error_code_class, error_code_constructor,
                                                           error_code_category, error_code_message);
        jobject j_app_error = env->NewObject(app_exception_class, app_exception_constructor,
                                             j_error_code, app_error_message);

        return static_cast<jthrowable>(j_app_error);
    }
    CATCH_STD()

    return nullptr;
}

