package io.realm;

import javax.annotation.Nullable;

public interface SessionStateListener {

    /**
     * @param oldState
     * @param newState
     */
    void onChange(@Nullable SyncSession.State oldState, SyncSession.State newState);
}
