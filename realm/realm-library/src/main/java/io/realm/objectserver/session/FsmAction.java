package io.realm.objectserver.session;

import io.realm.objectserver.Credentials;

/**
 * As {@link Session} is modeled as a state machine, this interface describe all
 * possible actions in that machine.
 *
 * All states should implement this so all possible permutations of state/actions are covered.
 */
interface FsmAction {
    void onStart();
    void onBind();
    void onUnbind();
    void onStop();
    void onRefresh();
    void onSetCredentials(Credentials credentials);
}
