package io.realm.objectserver.internal;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import io.realm.objectserver.Session;
import io.realm.objectserver.SyncConfiguration;
import io.realm.objectserver.SyncManager;
import io.realm.objectserver.internal.syncpolicy.AutomaticSyncPolicy;

/**
 * Private class for keeping track of sessions.
 * If {@link io.realm.objectserver.Session} moves into the public API at some point, this class can be folded into
 * {@link io.realm.objectserver.SyncManager};
 */
public class SessionStore {

    // Map of between a local Realm path and any associated sessionInfo
    private static HashMap<String, Session> sessions = new HashMap<String, Session>();


    /**
     * Gets any cached {@link Session} for the given {@link SyncConfiguration} or create a new one if
     * no one exists.
     *
     * @param syncConfiguration configuration object for the synchronized Realm.
     * @return the {@link Session} for the specified Realm.
     */
    public static synchronized Session getSession(SyncConfiguration syncConfiguration) {
        if (syncConfiguration == null) {
            throw new IllegalArgumentException("A non-empty 'syncConfiguration' is required.");
        }

        String localPath = syncConfiguration.getPath();
        Session session = sessions.get(localPath);
        if (session == null) {
            session = new Session(syncConfiguration, SyncManager.getAuthServer(), new AutomaticSyncPolicy());
            session.getSyncPolicy().onSessionCreated(session);
            sessions.put(localPath, session);
        }

        return session;
    }

    /**
     * Removes a session. Should only be once it has been closed.
     */
    static synchronized void removeSession(Session session) {
        if (session == null) {
            return;
        }

        Iterator<Map.Entry<String, Session>> it = sessions.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Session> entry = it.next();
            if (entry.getValue().equals(session)) {
                it.remove();
                break;
            }
        }
    }

    /**
     * Returns a list of all sessions being tracked.
     */
    public static Collection<Session> getSession() {
        return sessions.values();
    }
}
