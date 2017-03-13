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
#include "io_realm_ClientResetError.h"

#include "object-store/src/sync/sync_manager.hpp"

#include "util.hpp"

using namespace realm;

JNIEXPORT void JNICALL Java_io_realm_ClientResetError_nativeExecuteClientReset(JNIEnv* env, jobject, jstring localRealmPath)
{
    TR_ENTER()
    try {
        JStringAccessor local_realm_path(env, localRealmPath);
        if (!SyncManager::shared().immediately_run_file_actions(std::string(local_realm_path))) {
            ThrowException(env, IllegalState, concat_stringdata("Realm hasn't been closed. Client Reset cannot run for Realm at: ", local_realm_path));
        }
    }
    CATCH_STD()
}
