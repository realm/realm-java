package io.realm.objectserver.syncpolicy;

import io.realm.objectserver.SyncConfiguration;
import io.realm.objectserver.session.Session;

/**
 * This synchronization policy renders all control to the developer.
 * {@link Session#bind()} and {@link Session#unbind()} must be manually called from the developer.
 *
 * @see SyncConfiguration.Builder#syncPolicy(SyncPolicy)
 */
public class ManualSyncPolicy implements SyncPolicy {

    @Override
    public void apply(Session session) {
        // Do nothing.
    }

    @Override
    public void stop() {
        // Do nothing.
    }
}
