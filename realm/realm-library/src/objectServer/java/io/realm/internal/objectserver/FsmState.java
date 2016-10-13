/*
 * Copyright 2016 Realm Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.realm.internal.objectserver;

import io.realm.SyncSession;
import io.realm.ObjectServerError;
import io.realm.SessionState;

/**
 * Abstract class containing shared logic for all {@link SyncSession} states. All states must extend
 * this class as it contains the logic for entering and leaving states.
 */
abstract class FsmState implements FsmAction {

    volatile ObjectServerSession session; // This is non-null when this state is active.
    private boolean exiting; // TODO: Remind me again what race condition necessitated this.

    /**
     * Entry into the state. This method is also responsible for executing any asynchronous work
     * this state might run.
     *
     * This should only be called from {@link SyncSession}.
     */
    public void entry(ObjectServerSession session) {
        this.session = session;
        this.exiting = false;
        onEnterState();
    }

    /**
     * Called just before leaving the state. Once this method is called no more state changes can be triggered from
     * this state until {@link #entry(ObjectServerSession)} has been called again.
     * <p>
     * This should only be called from {@link SyncSession}.
     */
    public void exit() {
        exiting = true;
        onExitState();
    }

    public void gotoNextState(SessionState state) {
        if (!exiting) {
            session.nextState(state);
        }
    }

    protected abstract void onEnterState();
    protected abstract void onExitState();

    @Override
    public void onStart() {
        // Do nothing
    }

    @Override
    public void onBind() {
        // Do nothing
    }

    @Override
    public void onUnbind() {
        // Do nothing
    }

    @Override
    public void onStop() {
        // Do nothing
    }

    @Override
    public void onError(ObjectServerError error) {
        switch(error.getCategory()) {
            case FATAL:
                gotoNextState(SessionState.STOPPED);
                break;
            case RECOVERABLE:
                gotoNextState(SessionState.UNBOUND);
                break;
        }
    }
}
