package io.realm.internal;


import android.content.Context;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import io.realm.RealmConfiguration;
import io.realm.Session;
import io.realm.SyncConfiguration;
import io.realm.SyncManager;
import io.realm.exceptions.RealmException;
import io.realm.internal.objectserver.SessionStore;
import io.realm.internal.objectserver.SyncSession;

@SuppressWarnings("unused") // Used through reflection. See ObjectServerFacade
@Keep
public class SyncObjectServerFacade extends ObjectServerFacade {

    private static final String WRONG_TYPE_OF_CONFIGURATION =
            "'configuration' has to be an instance of 'SyncConfiguration'.";

    @Override
    public void init(Context context) {
        // Trying to keep things out the public API is no fun :/
        // Just use reflection on init. It is a one-time method call so should be acceptable.
        try {
            Class<?> syncManager = Class.forName("io.realm.ObjectServer");
            Method method = syncManager.getDeclaredMethod("init", Context.class);
            method.setAccessible(true);
            method.invoke(null, context);
        } catch (NoSuchMethodException e) {
            throw new RealmException("Could not initialize the Realm Object Server", e);
        } catch (InvocationTargetException e) {
            throw new RealmException("Could not initialize the Realm Object Server", e);
        } catch (IllegalAccessException e) {
            throw new RealmException("Could not initialize the Realm Object Server", e);
        } catch (ClassNotFoundException e) {
            throw new RealmException("Could not initialize the Realm Object Server", e);
        }
    }

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
