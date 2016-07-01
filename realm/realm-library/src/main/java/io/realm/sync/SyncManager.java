package io.realm.sync;

import android.os.Handler;

import java.util.HashMap;
import java.util.Map;

import io.realm.BaseRealm;
import io.realm.internal.log.RealmLog;

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

    public static void notifyHandlers(String path) {

        for (Map.Entry<Handler, String> handlerIntegerEntry : BaseRealm.handlers.entrySet()) {
            Handler handler = handlerIntegerEntry.getKey();
            String realmPath = handlerIntegerEntry.getValue();

            // For all other threads, use the Handler
            // Note there is a race condition with handler.hasMessages() and handler.sendEmptyMessage()
            // as the target thread consumes messages at the same time. In this case it is not a problem as worst
            // case we end up with two REALM_CHANGED messages in the queue.
            if (
                    realmPath.equals(path)                           // It's the right realm
                            && !handler.hasMessages(14930352)    // HandlerController.REALM_CHANGED The right message
                            && handler.getLooper().getThread().isAlive()                // HandlerController.REALM_CHANGED The receiving thread is alive
                            && !handler.sendEmptyMessage(14930352)) {
                RealmLog.w("Cannot update Looper threads when the Looper has quit. Use realm.setAutoRefresh(false) " +
                        "to prevent this.");
            }
        }
    }

    private static native long syncCreateClient();
    private static native long syncCreateSession(long clientPointer, String path, String serverUrl, String userToken);

}