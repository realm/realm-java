package io.realm.objectserver.syncpolicy;

import io.realm.objectserver.session.Session;

/**
 * This synchronization policy continue synchronizes changes as long as the session it self is open.
 * It is the default policy used by Realm.
 *
 * @see io.realm.objectserver.ObjectServerConfiguration.Builder#syncPolicy(SyncPolicy)
 */
public class AutomaticSyncPolicy implements SyncPolicy {

    @Override
    public void apply(Session session) {
        session.bind();
    }

    @Override
    public void stop() {
        // Do nothing
    }
}
