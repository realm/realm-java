package io.realm.objectserver.session;

import io.realm.objectserver.credentials.Credentials;

/**
 * As {@link Session} is modeled as a state machine, this interface describe all
 * possible actions in that machine.
 *
 * All states should implement this so all possible permutations of state/actions are covered.
 */
interface FsmAction {
    void onStart(Session session);
    void onBind(Session session);
    void onUnbind(Session session);
    void onStop(Session session);
    void onRefresh(Session session);
    void onSetCredentials(Session session, Credentials credentials);
}
