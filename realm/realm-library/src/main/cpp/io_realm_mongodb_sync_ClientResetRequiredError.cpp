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

#include <realm/object-store/sync/app.hpp>
#include <realm/object-store/sync/sync_manager.hpp>

#include "util.hpp"
#include "io_realm_mongodb_sync_ClientResetRequiredError.h"

using namespace realm;

JNIEXPORT void JNICALL Java_io_realm_mongodb_sync_ClientResetRequiredError_nativeExecuteClientReset(JNIEnv* env, jobject,
                                                                                                    jlong j_app_ptr,
                                                                                                    jstring localRealmPath)
{
    try {
        auto app = *reinterpret_cast<std::shared_ptr<app::App>*>(j_app_ptr);
        JStringAccessor local_realm_path(env, localRealmPath);
        if (!app->sync_manager()->immediately_run_file_actions(std::string(local_realm_path))) {
            ThrowException(
                env, IllegalState,
                concat_stringdata("Realm was not configured correctly. Client Reset could not be run for Realm at: ",
                                  local_realm_path));
            return;
        }
    }
    CATCH_STD()
}
