/*
 * Copyright 2016 Realm Inc.
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

#include "io_realm_internal_NativeObjectReference.h"

typedef void (*FinalizeFunc)(jlong);

JNIEXPORT void JNICALL Java_io_realm_internal_NativeObjectReference_nativeCleanUp(JNIEnv*, jclass,
                                                                                  jlong finalizer_ptr,
                                                                                  jlong native_ptr)
{
    FinalizeFunc finalize_func = reinterpret_cast<FinalizeFunc>(finalizer_ptr);
    finalize_func(native_ptr);
}
