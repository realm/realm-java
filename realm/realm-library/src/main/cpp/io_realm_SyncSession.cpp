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

#include "io_realm_SyncSession.h"

#include "object-store/src/sync/sync_manager.hpp"
#include "object-store/src/sync/sync_session.hpp"

#include "util.hpp"
#include "jni_util/java_global_ref.hpp"
#include "jni_util/java_method.hpp"
#include "jni_util/java_class.hpp"
#include "jni_util/java_local_ref.hpp"
#include "jni_util/jni_utils.hpp"

using namespace std;
using namespace realm;
using namespace jni_util;
using namespace sync;

JNIEXPORT jboolean JNICALL Java_io_realm_SyncSession_nativeRefreshAccessToken(JNIEnv* env, jclass,
                                                                              jstring localRealmPath,
                                                                              jstring accessToken,
                                                                              jstring sync_realm_url)
{
    TR_ENTER()
    try {
        JStringAccessor local_realm_path(env, localRealmPath);
        auto session = SyncManager::shared().get_existing_session(local_realm_path);
        if (session) {
            JStringAccessor access_token(env, accessToken);
            JStringAccessor realm_url(env, sync_realm_url);
            session->refresh_access_token(access_token, std::string(realm_url));
            return JNI_TRUE;
        }
        else {
            Log::d("no active/inactive session found");
        }
    }
    CATCH_STD()
    return JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_io_realm_SyncSession_nativeWaitForDownloadCompletion(JNIEnv* env,
                                                                                     jobject session_object,
                                                                                     jstring localRealmPath)
{
    TR_ENTER()
    try {
        JStringAccessor local_realm_path(env, localRealmPath);
        auto session = SyncManager::shared().get_existing_session(local_realm_path);

        if (session) {
            static JavaClass java_sync_session_class(env, "io/realm/SyncSession");
            static JavaMethod java_notify_result_method(env, java_sync_session_class, "notifyAllChangesDownloaded",
                                                        "(Ljava/lang/Long;Ljava/lang/String;)V");
            JavaGlobalRef java_session_object_ref(env, session_object);

            bool listener_registered =
                session->wait_for_download_completion([java_session_object_ref](std::error_code error) {
                    JNIEnv* env = JniUtils::get_env(true);
                    jobject java_error_code = nullptr;
                    jstring java_error_message = nullptr;
                    if (error != std::error_code{}) {
                        java_error_code = NewLong(env, error.value());
                        java_error_message = env->NewStringUTF(error.message().c_str());
                    }
                    env->CallVoidMethod(java_session_object_ref.get(), java_notify_result_method, java_error_code,
                                        java_error_message);
                });

            return to_jbool(listener_registered);
        }
    }
    CATCH_STD()
    return JNI_FALSE;
}
