package io.realm.internal;

import android.content.Context;

import io.realm.RealmConfiguration;
import io.realm.exceptions.RealmException;

/**
 * Class acting as an mediator between the basic Realm APIs and the Object Server APIs.
 * This breaks the cyclic dependency between ObjectServer and Realm code.
 */
public class ObjectServerFacade {

    private final static ObjectServerFacade nonSyncFacade = new ObjectServerFacade();
    private static ObjectServerFacade syncFacade = null;

    static {
        //noinspection TryWithIdenticalCatches
        try {
            Class syncFacadeClass = Class.forName("io.realm.internal.SyncObjectServerFacade");
            syncFacade = (ObjectServerFacade) syncFacadeClass.newInstance();
        } catch (ClassNotFoundException ignored) {
        } catch (InstantiationException e) {
            throw new RealmException("Failed to init SyncObjectServerFacade", e);
        } catch (IllegalAccessException e) {
            throw new RealmException("Failed to init SyncObjectServerFacade", e);
        }
    }

    /**
     * Initialize the Object Server library
     * @param context
     */
    public void init(Context context) {
    }

    /**
     * Notify the session for this configuration that a local commit was made.
     */
    public void notifyCommit(RealmConfiguration configuration, long lastSnapshotVersion) {
    }

    /**
     * The first instance of this Realm was opened.
     */
    public void realmClosed(RealmConfiguration configuration) {
    }

    /**
     * The last instance of this Realm was closed.
     */
    public void realmOpened(RealmConfiguration configuration) {
    }

    public String[] getUserAndServerUrl(RealmConfiguration config) {
        return new String[2];
    }

    public static ObjectServerFacade getFacade(boolean needSyncFacade) {
        if (needSyncFacade) {
            return syncFacade;
        }
        return nonSyncFacade;
    }

    // Returns a SyncObjectServerFacade instance if the class exists. Otherwise returns a non-sync one.
    public static ObjectServerFacade getSyncFacadeIfPossible() {
        if (syncFacade != null) {
            return syncFacade;
        }
        return nonSyncFacade;
    }
}
