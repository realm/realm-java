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

import io.realm.annotations.Beta;
import io.realm.internal.Keep;

/**
 * @Beta
 * Internal initializer class for the Object Server.
 * Use to keep the `SyncManager` free from Android dependencies
 */
@SuppressWarnings("unused")
@Keep
@Beta
class ObjectServer {

    public static void init(Context context) {
        // Setup AppID
        String appId = "unknown";
        try {
            PackageInfo pi = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            appId = pi.packageName;
        } catch (Exception ignore) {
        }

        // Configure default UserStore
        UserStore userStore = new RealmFileUserStore(context.getFilesDir().getPath());

        SyncManager.init(appId, userStore);
    }
}
