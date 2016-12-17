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

package io.realm.internal.syncpolicy;

import io.realm.ObjectServerError;
import io.realm.internal.objectserver.ObjectServerSession;

/**
 * This SyncPolicy will automatically start synchronizing changes to a Realm as soon as it is opened.
 */
public class AutomaticSyncPolicy implements SyncPolicy {

    private Long lastError = null;
    private int recurringErrors = 0;

    @Override
    public void onRealmOpened(ObjectServerSession session) {
        session.bind(); // Bind Realm first time it is opened.
    }

    @Override
    public void onRealmClosed(ObjectServerSession session) {
        // TODO In order to preserve resources we should ideally close the session as well, but first
        // we want to make sure that all local changes have been synchronized to the remote Realm.
    }

    @Override
    public void onSessionCreated(ObjectServerSession session) {
        session.start();
    }

    @Override
    public void onSessionStopped(ObjectServerSession session) {
        // Do nothing
    }

    @Override
    public boolean onError(ObjectServerSession session, ObjectServerError error) {
        switch(error.getCategory()) {
            case FATAL:
                return false;   // Report all fatal errors to the user
            case RECOVERABLE:
                return rebind(session);
            default:
                return false;
        }
    }

    /**
     * Returns {@code true} if we decide to rebind, {@code false} if the error was determined to no longer be solvable.
     */
    private boolean rebind(ObjectServerSession session) {
        // Track all calls to rebind(). If some error reported as RECOVERABLE keeps happening, we need to abort to
        // prevent run-away sessions. Right now we treat an error as recurring if it happens within 3 seconds of each
        // other. After 5 of such errors we terminate the session.
        //
        // Standard IO errors are already handled using incremental backoff by e.g the AUTHENTICATING state, so
        // re-occurring errors at this level are more serious.
        long now = System.currentTimeMillis();
        if (lastError - now < 3000) {
            recurringErrors++;
        } else {
            recurringErrors = 1;
        }
        lastError = now;

        if (recurringErrors == 5) {
            session.stop(); // Abort session, some error that should be temporary keeps happening.
            return false;
        } else {
            session.bind();
            return true;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AutomaticSyncPolicy that = (AutomaticSyncPolicy) o;

        if (recurringErrors != that.recurringErrors) return false;
        return lastError != null ? lastError.equals(that.lastError) : that.lastError == null;
    }

    @Override
    public int hashCode() {
        int result = lastError != null ? lastError.hashCode() : 0;
        result = 31 * result + recurringErrors;
        return result;
    }
}
