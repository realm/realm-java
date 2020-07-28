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

#include <sync/remote_mongo_collection.hpp>

using namespace realm;
using namespace realm::app;
using namespace realm::bson;
using namespace realm::jni_util;
using namespace realm::_impl;


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
        WatchStream* watch_stream = reinterpret_cast<WatchStream*>(j_watch_stream_ptr);
        JStringAccessor line(env, j_line);

        watch_stream->feed_line(std::string(line));
    }
    CATCH_STD()
}

JNIEXPORT jstring JNICALL
Java_io_realm_internal_objectstore_OsWatchStream_nativeGetState(JNIEnv *env, jclass,
                                                                jlong j_watch_stream_ptr) {
    try {
        WatchStream* watch_stream = reinterpret_cast<WatchStream*>(j_watch_stream_ptr);

        switch (watch_stream->state()){
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
        WatchStream* watch_stream = reinterpret_cast<WatchStream*>(j_watch_stream_ptr);
        return JniBsonProtocol::bson_to_jstring(env, watch_stream->next_event());
    }
    CATCH_STD()

    return nullptr;
}

