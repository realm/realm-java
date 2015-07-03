package io.realm.internal.android;

import io.realm.internal.log.RealmLog;

/**
 * This is the RealmLogger used by Realm in Release builds. It only logs warnings and errors by default.
 */
public class ReleaseAndroidLogger extends AndroidLogger {

    public ReleaseAndroidLogger() {
        setMinimumLogLevel(RealmLog.WARN);
    }
}
