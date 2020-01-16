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

package io.realm;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Build;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import io.realm.internal.Keep;
import io.realm.internal.Util;
import io.realm.log.RealmLog;

/**
 * Internal initializer class for the Object Server.
 * Use to keep the `SyncManager` free from Android dependencies
 */
@SuppressWarnings("unused")
@Keep
class ObjectServer {

    public static void init(Context context, String appDefinedUserAgent) {
        // Setup AppID
        String appId = "unknown";
        try {
            PackageInfo pi = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            appId = pi.packageName;
        } catch (Exception ignore) {
        }

        // Setup Realm part of User-Agent string
        String userAgentBindingInfo = "Unknown"; // Fallback in case of anything going wrong
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("RealmJava/");
            sb.append(BuildConfig.VERSION_NAME);
            sb.append(" (");
            sb.append(Util.isEmptyString(Build.DEVICE) ? "unknown-device" : Build.DEVICE);
            sb.append(", ");
            sb.append(Util.isEmptyString(Build.MODEL) ? "unknown-model" : Build.MODEL);
            sb.append(", v");
            sb.append(Build.VERSION.SDK_INT);
            sb.append(")");
            userAgentBindingInfo = sb.toString();
        } catch (Exception e) {
            // Failures to construct the user agent should never cause the system itself to crash.
            RealmLog.warn("Constructing User-Agent description failed.", e);
        }

        // init the "sync_manager.cpp" metadata Realm, this is also needed later, when re try
        // to schedule a client reset. in realm-java#master this is already done, when initialising
        // the RealmFileUserStore (not available now on releases)
        if (SyncManager.Debug.separatedDirForSyncManager) {
            try {
                // Files.createTempDirectory is not available on JDK 6.
                File dir = File.createTempFile("remote_sync_", "_" + android.os.Process.myPid(),
                        context.getFilesDir());
                if (!dir.delete()) {
                    throw new IllegalStateException(String.format(Locale.US,
                            "Temp file '%s' cannot be deleted.", dir.getPath()));
                }
                if (!dir.mkdir()) {
                    throw new IllegalStateException(String.format(Locale.US,
                            "Directory '%s' for SyncManager cannot be created. ",
                            dir.getPath()));
                }
                SyncManager.nativeInitializeSyncManager(dir.getPath(), userAgentBindingInfo, appDefinedUserAgent);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        } else {
            SyncManager.nativeInitializeSyncManager(context.getFilesDir().getPath(), userAgentBindingInfo, appDefinedUserAgent);
        }

        // Configure default UserStore
        UserStore userStore = new RealmFileUserStore();

        SyncManager.init(appId, userStore);
    }
}
