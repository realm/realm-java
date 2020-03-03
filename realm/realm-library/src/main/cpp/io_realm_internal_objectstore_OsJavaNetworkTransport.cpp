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

#include "io_realm_internal_objectstore_OsJavaNetworkTransport.h"

#include "util.hpp"

#include <sync/app.hpp>
//#include "thread_safe_reference.hpp"
//#include "jni_util/java_method.hpp"
//#include "jni_util/java_class.hpp"
//#include "jni_util/jni_utils.hpp"
//#include "object-store/src/sync/async_open_task.hpp"
//#include "object-store/src/sync/sync_config.hpp"
//
//#include <shared_realm.hpp>
//#include <memory>



using namespace realm;
using namespace realm::jni_util;
using namespace realm::_impl;

static void finalize_transport(jlong ptr)
{
    delete reinterpret_cast<app::GenericNetworkTransport*>(ptr);
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_objectstore_OsJavaNetworkTransport_nativeGetFinalizerMethodPtr(JNIEnv*, jclass)
{
    return reinterpret_cast<jlong>(&finalize_transport);
}

JNIEXPORT jlong JNICALL Java_io_realm_internal_objectstore_OsJavaNetworkTransport_nativeCreate(JNIEnv* env, jclass)
{
    try {
        // FIXME

    }
    CATCH_STD()
    return 0;
}
