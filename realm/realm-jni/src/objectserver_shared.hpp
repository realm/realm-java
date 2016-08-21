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

#ifndef REALM_OBJECTSERVER_SHARED_HPP
#define REALM_OBJECTSERVER_SHARED_HPP

#include <jni.h>
#include <thread>

// maintain a reference to the threads allocated dynamically, to prevent deallocation
// after Java_io_realm_internal_SharedGroup_nativeStartSession completes.
// To be released later, maybe on JNI_OnUnload
extern std::thread* sync_client_thread;
extern JNIEnv* sync_client_env;

#endif // REALM_OBJECTSERVER_SHARED_HPP
