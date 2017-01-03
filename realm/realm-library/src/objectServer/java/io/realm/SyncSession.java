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

package io.realm;

import java.net.URI;

import io.realm.annotations.Beta;
import io.realm.internal.Keep;
import io.realm.log.RealmLog;
import io.realm.internal.objectserver.ObjectServerSession;

/**
 * @Beta
 * This class represents the connection to the Realm Object Server for one {@link SyncConfiguration}.
 * <p>
 * A Session is created by either calling {@link SyncManager#getSession(SyncConfiguration)} or by opening
 * a Realm instance using that configuration. Once a session has been created, it will continue to exist until the app
 * is closed or the {@link SyncConfiguration} is no longer used.
 * <p>
 * A session is fully controlled by Realm, but can provide additional information in case of errors.
 * It is passed along in all {@link SyncSession.ErrorHandler}s.
 * <p>
 * This object is thread safe.
 *
 * @see SessionState
 */
@Keep
@Beta
public class SyncSession {

    private final ObjectServerSession osSession;

    SyncSession(ObjectServerSession osSession) {
        this.osSession = osSession;
        osSession.setUserSession(this);
    }

    /**
     * Returns the {@link SyncConfiguration} that is responsible for controlling the session.
     *
     * @return SyncConfiguration that defines and controls this session.
     */
    public SyncConfiguration getConfiguration() {
        return osSession.getConfiguration();
    }

    /**
     * Returns the {@link SyncUser} defined by the {@link SyncConfiguration} that is used to connect to the
     * Realm Object Server.
     *
     * @return {@link SyncUser} used to authenticate the session on the Realm Object Server.
     */
    public SyncUser getUser() {
        return osSession.getConfiguration().getUser();
    }

    /**
     * Returns the {@link URI} describing the remote Realm which this session connects to and synchronizes changes with.
     *
     * @return {@link URI} describing the remote Realm.
     */
    public URI getServerUrl() {
        return osSession.getConfiguration().getServerUrl();
    }

    /**
     * Returns the state of this session.
     *
     * @return the current {@link SessionState} for this session.
     */
    public SessionState getState() {
        return osSession.getState();
    }

    ObjectServerSession getOsSession() {
        return osSession;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if (osSession.getState() != SessionState.STOPPED) {
            RealmLog.warn("Session was not closed before being finalized. This is a potential resource leak.");
            osSession.stop();
        }
    }

    /**
     * Interface used to report any session errors.
     *
     * @see SyncManager#setDefaultSessionErrorHandler(ErrorHandler)
     * @see SyncConfiguration.Builder#errorHandler(ErrorHandler)
     */
    public interface ErrorHandler {
        /**
         * Callback for errors on a session object.
         *
         * @param session {@link SyncSession} this error happened on.
         * @param error type of error.
         */
        void onError(SyncSession session, ObjectServerError error);
    }
}

