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

import io.realm.ObjectServerError;
import io.realm.SyncSession;

/**
 * STOPPED State. This is the final state for a {@link SyncSession}. After this, all actions will throw an
 * {@link IllegalStateException}.
 */
class StoppedState extends FsmState {

    @Override
    public void onEnterState() {
        session.stopNativeSession();
        session.getSyncPolicy().onSessionStopped(session);
    }

    @Override
    protected void onExitState() {
        // Cannot exit this state
    }

    @Override
    public void onStart() {
        // To harsh to to throw here as any SyncPolicy might not have been made aware
        // that the Session is stopped. Just ignore the call instead.
    }

    @Override
    public void onBind() {
        // To harsh to to throw here as any SyncPolicy might not have been made aware
        // that the Session is stopped. Just ignore the call instead.
    }

    @Override
    public void onUnbind() {
        // To harsh to to throw here as any SyncPolicy might not have been made aware
        // that the Session is stopped. Just ignore the call instead.
    }

    @Override
    public void onStop() {
        // To harsh to to throw here as any SyncPolicy might not have been made aware
        // that the Session is stopped. Just ignore the call instead.
    }

    @Override
    public void onError(ObjectServerError error) {
        // Ignore all errors at this state. None of them would have any impact.
    }
}
