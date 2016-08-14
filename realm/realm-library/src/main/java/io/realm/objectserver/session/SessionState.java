package io.realm.objectserver.session;

/**
 * Enum describing the various states the Session Finite-State-Machine can be in.
 */
enum SessionState {
    INITIAL, // Initial starting state
    STARTED, // Session has been started
    UNBOUND, // Start done, Realm is unbound
    BINDING_REALM, // bind() has been called. Can take a while
    AUTHENTICATING, // authentication is needed. Can take a while.
    BOUND, // local realm was successfully bound to the remote Realm.
    STOPPED // Final state. No more tra
}


