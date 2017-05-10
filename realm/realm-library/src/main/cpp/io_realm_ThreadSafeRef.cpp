/*
 * Copyright 2017 Realm Inc.
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
#include <object-store/src/thread_safe_reference.hpp>
#include <object-store/src/shared_realm.hpp>

#include "io_realm_ThreadSafeRef.h"

#include "util.hpp"

using namespace std;
using namespace realm;

//JNIEXPORT jlong JNICALL Java_io_realm_ThreadSafeRef_nativeGetThreadSafeRef
//        (JNIEnv *env, jclass, jlong realmPtr, jlong rowPtr)
//{
//    auto r = reinterpret_cast<SharedRealm *>(realmPtr);
//
//    try {
//        // need to match impedances here: we have a pointer to a Row and the fn wants an Object
//        // also, apparently, neither obtain_thread_safe_reference nor the constructor are visible
//        // auto ref = r->obtain_thread_safe_reference(rowPtr);
//        return reinterpret_cast<jlong>(new ThreadSafeReference<Object>(ref));
//    }
//    CATCH_STD()
//
//    return 0;
//}
//
//JNIEXPORT jlong JNICALL Java_io_realm_ThreadSafeRef_nativeResolveThreadSafeRef
//        (JNIEnv *env, jclass, jlong realmPtr, jlong refPtr)
//{
//    auto r = reinterpret_cast<SharedRealm *>(realmPtr);
//    auto ref = reinterpret_cast<ThreadSafeReference<Object> *>(refPtr);
//
//    try {
//        // again, mis-matched impedances: need a row ref, have an Object
//        // also, apparently, resolve_thread_safe_reference is not visible
//        // auto obj = r->resolve_thread_safe_reference(ref);
//        return reinterpret_cast<jlong>(&obj);
//    }
//    CATCH_STD()
//
//    return 0;
//}
//
//static void finalize_ref(jlong refPtr)
//{
//    auto ref = reinterpret_cast<ThreadSafeReference<Object>*>(refPtr);
//    delete ref;
//}
//
//JNIEXPORT jlong JNICALL Java_io_realm_ThreadSafeRef_nativeGetFinalizerPtr
//        (JNIEnv *, jclass)
//{
//    return reinterpret_cast<jlong>(&finalize_ref);
//}
