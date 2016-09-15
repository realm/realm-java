package io.realm.objectserver.internal;

import io.realm.RealmConfiguration;
import io.realm.objectserver.Session;
import io.realm.objectserver.SyncConfiguration;
import io.realm.objectserver.SyncManager;

/**
 * Class acting as an mediator between the basic Realm APIs and the Object Server APIs.
 * This breaks the cyclic dependency between ObjectServer and Realm code.
 *
 * TODO Move this class into a `common` module that both realm-library and objectserver-library depends on.
 */
public class ObjectServerFacade {

    public static final boolean SYNC_AVAILABLE;

    static {
        boolean syncAvailable;
        try {
            Class.forName("io.realm.objectserver.SyncManager");
            syncAvailable = true;
        } catch (ClassNotFoundException e) {
            syncAvailable = false;
        }
        SYNC_AVAILABLE = syncAvailable;
    }
    /**
     * Notify the session for this configuration that a local commit was made.
     */
    public static void notifyCommit(RealmConfiguration configuration, long lastSnapshotVersion) {
        if (SYNC_AVAILABLE && configuration instanceof SyncConfiguration) {
            Session publicSession = SyncManager.getSession((SyncConfiguration) configuration);
            SyncSession session = SessionStore.getPrivateSession(publicSession);
            session.notifyCommit(lastSnapshotVersion);
        }
    }

    public static void realmClosed(RealmConfiguration configuration) {
        if (SYNC_AVAILABLE && configuration instanceof SyncConfiguration) {
            Session publicSession = SyncManager.getSession((SyncConfiguration) configuration);
            SyncSession session = SessionStore.getPrivateSession(publicSession);
            session.getSyncPolicy().onRealmClosed(session);
        }
    }

    public static void realmOpened(RealmConfiguration configuration) {
        if (SYNC_AVAILABLE && configuration instanceof SyncConfiguration) {
            Session publicSession = SyncManager.getSession((SyncConfiguration) configuration);
            SyncSession session = SessionStore.getPrivateSession(publicSession);
            session.getSyncPolicy().onRealmOpened(session);
        }
    }

    public static String[] getUserAndServerUrl(RealmConfiguration config) {
        if (SYNC_AVAILABLE && config instanceof SyncConfiguration) {
            SyncConfiguration syncConfig = (SyncConfiguration) config;
            String rosServerUrl = syncConfig.getServerUrl().toString();
            String rosUserToken = syncConfig.getUser().getAccessToken();
            return new String[] {rosServerUrl, rosUserToken};
        } else {
            return new String[2];
        }
    }
}
