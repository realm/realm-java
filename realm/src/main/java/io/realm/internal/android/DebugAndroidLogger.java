package io.realm.internal.android;

import io.realm.internal.log.RealmLog;

/**
 * RealmLogger for Android debug builds. This logs everything as default.
 */
public class DebugAndroidLogger extends AndroidLogger {

    public DebugAndroidLogger() {
        setMinimumLogLevel(RealmLog.VERBOSE);
    }
}
