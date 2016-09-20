package io.realm.internal.objectserver;


import io.realm.RealmConfiguration;
import io.realm.Session;
import io.realm.SyncConfiguration;
import io.realm.SyncManager;
import io.realm.internal.ObjectServerFacade;

@SuppressWarnings("unused") // Used through reflection. See ObjectServerFacade
public class SyncObjectServerFacade extends ObjectServerFacade {

    private static final String WRONG_TYPE_OF_CONFIGURATION =
            "'configuration' has to be an instance of 'SyncConfiguration'.";

    @Override
    public void notifyCommit(RealmConfiguration configuration, long lastSnapshotVersion) {
        if (configuration instanceof SyncConfiguration) {
            Session publicSession = SyncManager.getSession((SyncConfiguration) configuration);
            SyncSession session = SessionStore.getPrivateSession(publicSession);
            session.notifyCommit(lastSnapshotVersion);
        } else {
            throw new IllegalArgumentException(WRONG_TYPE_OF_CONFIGURATION);
        }
    }

    @Override
    public void realmClosed(RealmConfiguration configuration) {
        if (configuration instanceof SyncConfiguration) {
            Session publicSession = SyncManager.getSession((SyncConfiguration) configuration);
            SyncSession session = SessionStore.getPrivateSession(publicSession);
            session.getSyncPolicy().onRealmClosed(session);
        } else {
            throw new IllegalArgumentException(WRONG_TYPE_OF_CONFIGURATION);
        }
    }

    @Override
    public void realmOpened(RealmConfiguration configuration) {
        if (configuration instanceof SyncConfiguration) {
            Session publicSession = SyncManager.getSession((SyncConfiguration) configuration);
            SyncSession session = SessionStore.getPrivateSession(publicSession);
            session.getSyncPolicy().onRealmOpened(session);
        } else {
            throw new IllegalArgumentException(WRONG_TYPE_OF_CONFIGURATION);
        }
    }

    @Override
    public String[] getUserAndServerUrl(RealmConfiguration config) {
        if (config instanceof SyncConfiguration) {
            SyncConfiguration syncConfig = (SyncConfiguration) config;
            String rosServerUrl = syncConfig.getServerUrl().toString();
            String rosUserToken = syncConfig.getUser().getAccessToken();
            return new String[] {rosServerUrl, rosUserToken};
        } else {
            return new String[2];
        }
    }
}
