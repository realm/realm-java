package io.realm.objectserver;

public interface SyncPolicy {
    boolean isBindAllowed(SessionInfo session);
    void apply(SyncSession session);
}
