package io.realm;

import android.content.Context;
import android.content.pm.PackageInfo;

import io.realm.android.SharedPrefsUserStore;
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
        UserStore userStore = new SharedPrefsUserStore(context);

        SyncManager.init(appId, userStore);
    }
}
