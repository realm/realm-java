package io.realm.sync;

public class RealtimeSyncPolicy implements SyncPolicy {
    @Override
    public void apply(SyncSession session) {
        session.start();
    }
}
