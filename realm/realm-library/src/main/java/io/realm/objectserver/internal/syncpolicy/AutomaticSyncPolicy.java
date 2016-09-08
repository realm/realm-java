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

package io.realm.objectserver.internal.syncpolicy;

import io.realm.objectserver.ObjectServerError;
import io.realm.objectserver.Session;

/**
 * This SyncPolicy will automatically start synchronizing changes to a Realm as soon as it is opened.
 * // TODO Figure out how to close connection once all changes have been uploaded.
 */
public class AutomaticSyncPolicy implements SyncPolicy {

    @Override
    public void onRealmOpened(Session session) {
        session.bind(); // Bind Realm first time it is opened.
    }

    @Override
    public void onRealmClosed(Session session) {
        // TODO Sync need to expose callback when there is no more local changes
        // For now just keep the session open.
    }

    @Override
    public void onSessionCreated(Session session) {
        session.start();
    }

    @Override
    public void onSessionStopped(Session session) {
        // Do nothing
    }

    @Override
    public boolean onError(Session session, ObjectServerError error) {
        switch(error.category()) {
            case FATAL:
                return false;   // Report all fatal errors to the user
            case INFO:
                return true;     // Ignore all INFO errors
            case RECOVERABLE:
                rebind(session);
                return true;
            default:
                return false;
        }
    }

    private void rebind(Session session) {
        // FIXME: Do not rebind uncritically. Figure out a good strategy for this.
        // See https://realmio.slack.com/archives/sync-core/p1472415880000002
        session.bind();
    }
}
