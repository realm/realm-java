package io.realm.objectserver.session;

/**
 * Enum describing the various states the Session Finite-State-Machine can be in.
 */
enum SessionState {
    INITIAL,                    // Initial starting state
    STARTED,                    // Session has been started
    UNBOUND,                    // Start done, Realm is unbound
    BINDING_REALM,              // ind() has been called. Can take a while.
    AUTHENTICATING,             // Trying to authenticate credentials. Can take a while.
    AUTHENTICATION_REQUIRED,    // New authentication required by calling `session.setCredentials()` before we can proceed.
    BOUND,                      // Local realm was successfully bound to the remote Realm. Changes are being synchronized.
    STOPPED                     // Terminal state. Session can no longer be used.
}


