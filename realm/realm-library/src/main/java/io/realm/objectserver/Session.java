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

package io.realm.objectserver;

import java.net.URI;

import io.realm.internal.Keep;
import io.realm.log.RealmLog;
import io.realm.objectserver.internal.SyncSession;

/**
 * This class represents the connection to the Realm Object Server for one {@link SyncConfiguration}.
 * <p>
 * A Session is created by either calling {@link SyncManager#getSession(SyncConfiguration)} or by opening
 * a Realm instance using that configuration. Once a session has been created it will continue to exist until the app
 * is closed or the {@link SyncConfiguration} is no longer used.
 * <p>
 * A session is fully controlled by Realm, but can provide additional information in case of errors.
 * It is passed along in all {@link Session.ErrorHandler}s.
 * <p>
 * This object is thread safe.
 *
 * @see SessionState
 */
@Keep
public final class Session {

    private final SyncSession syncSession;

    Session(SyncSession rosSession) {
        this.syncSession = rosSession;
    }

    /**
     * Returns the {@link SyncConfiguration} that is responsible for controlling this session.
     *
     * @return SyncConfiguration that defines and controls this session.
     */
    public SyncConfiguration getConfiguration() {
        return syncSession.getConfiguration();
    }

    /**
     * Returns the {@link User} defined by the {@link SyncConfiguration} that is used to connect to the
     * Realm Object Server.
     *
     * @return {@link User} used to authenticate the session on the Realm Object Server.
     */
    public User getUser() {
        return syncSession.getConfiguration().getUser();
    }

    /**
     * Returns the {@link URI} describing the remote Realm which this session connects to and synchronizes changes with.
     *
     * @return {@link URI} describing the remote Realm.
     */
    public URI getServerUrl() {
        return syncSession.getConfiguration().getServerUrl();
    }

    /**
     * Returns the state of this session.
     *
     * @return The current {@link SessionState} for this session.
     */
    public SessionState getState() {
        return syncSession.getState();
    }

    SyncSession getSyncSession() {
        return syncSession;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if (syncSession.getState() != SessionState.STOPPED) {
            RealmLog.warn("Session was not closed before being finalized. This is a potential resource leak.");
            syncSession.stop();
        }
    }

    /**
     * Interface used to report any session errors.
     *
     * @see SyncManager#setDefaultSessionErrorHandler(ErrorHandler)
     * @see io.realm.objectserver.SyncConfiguration.Builder#errorHandler(ErrorHandler)
     */
    public interface ErrorHandler {
        /**
         * Callback for errors on a session object.
         *
         * @param session {@link Session} this error happened on.
         * @param error type of error.
         */
        void onError(Session session, ObjectServerError error);
    }
}

