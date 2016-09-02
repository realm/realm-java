package io.realm.sync;

import java.util.HashMap;
import java.util.Map;

public final class SyncManager {
    private static volatile long syncClientPointer = 0;
    private final static Map<String, Long> SYNC_SESSIONS = new HashMap<String, Long>();

    public synchronized static long getSession(final String userToken, final String path, final String serverUrl) {
        if (syncClientPointer == 0) {
            // client event loop is not created for this token
            // we create 1 client per user token
            syncClientPointer = syncCreateClient();
        }

        // check if the session is not already available for the provided RealmConfiguration
        Long syncSessionPointer = SYNC_SESSIONS.get(path);
        if (syncSessionPointer == null) {
             syncSessionPointer = syncCreateSession(syncClientPointer, path, serverUrl, userToken);
        }

        SYNC_SESSIONS.put(path, syncSessionPointer);
        return syncSessionPointer;
    }

    private static native long syncCreateClient();
    private static native long syncCreateSession(long clientPointer, String path, String serverUrl, String userToken);

}
