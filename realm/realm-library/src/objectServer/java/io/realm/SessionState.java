package io.realm;

/**
 * Enum describing the various states a {@link SyncSession} can be in.
 */
public enum SessionState {

    /**
     * FIXME
     */
    WAITING_FOR_ACCESS_TOKEN(1),

    /**
     * FIXME
     */
    CONNECTING(2),

    /**
     * FIXME
     */
    ACTIVE(3),

    /**
     * FIXME
     */
    DYING(4),

    /**
     * FIXME
     */
    INACTIVE(5);

    private final int value;

    SessionState(int value) {
        this.value = value;
    }

    public static SessionState fromValue(long stateValue) {
        SessionState[] values = values();
        for (int i = 0; i < values.length; i++) {
            SessionState state = values[i];
            if (state.value == stateValue) {
                return state;
            }
        }
        throw new IllegalArgumentException("Unknown value: " + stateValue);
    }
}
