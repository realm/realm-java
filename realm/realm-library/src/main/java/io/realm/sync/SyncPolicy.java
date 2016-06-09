package io.realm.sync;

public interface SyncPolicy {
    void apply(SyncSession session);
}
