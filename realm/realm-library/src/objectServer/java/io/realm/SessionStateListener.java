package io.realm;

import javax.annotation.Nullable;

public interface SessionStateListener {

    /**
     * @param oldState
     * @param newState
     */
    void onChange(@Nullable SessionState oldState, SessionState newState);
}
