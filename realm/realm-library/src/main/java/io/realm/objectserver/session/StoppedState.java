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

package io.realm.objectserver.session;

import io.realm.objectserver.Credentials;
import io.realm.objectserver.ErrorCode;

/**
 * STOPPED State. This is the final state for a {@link Session}. After this, all actions will throw an
 * {@link IllegalStateException}.
 */
class StoppedState extends FsmState {

    public static final String SESSION_STOPPED_MSG = "Session has been stopped. Not further actions are possible.";

    @Override
    public void onEnterState() {
        // Do nothing. All relevant state was cleared when unbinding.
    }

    @Override
    protected void onExitState() {
        // Cannot exit this state
    }

    @Override
    public void onStart() {
        throw new IllegalStateException(SESSION_STOPPED_MSG);
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
        throw new IllegalStateException(SESSION_STOPPED_MSG);
    }

    @Override
    public void onRefresh() {
        throw new IllegalStateException(SESSION_STOPPED_MSG);
    }

    @Override
    public void onSetCredentials(Credentials credentials) {
        throw new IllegalStateException(SESSION_STOPPED_MSG);
    }

    @Override
    public void onError(ErrorCode errorCode, String errorMessage) {
        // Ignore all errors at this state. None of them would have any impact.
    }
}
