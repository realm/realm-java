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
import io.realm.internal.KeepMember;
import io.realm.internal.syncpolicy.SyncPolicy;
import io.realm.log.RealmLog;

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

    private final SyncConfiguration configuration;
    private final ErrorHandler errorHandler;
    // If 'true', the OS SyncSession is available.
    private volatile boolean osSessionAvailable = true;

    SyncSession(SyncConfiguration configuration) {
        this.configuration = configuration;
        this.errorHandler = configuration.getErrorHandler();
    }

    /**
     * Returns the {@link SyncConfiguration} that is responsible for controlling the session.
     *
     * @return SyncConfiguration that defines and controls this session.
     */
    public SyncConfiguration getConfiguration() {
        return configuration;
    }

    /**
     * Returns the {@link SyncUser} defined by the {@link SyncConfiguration} that is used to connect to the
     * Realm Object Server.
     *
     * @return {@link SyncUser} used to authenticate the session on the Realm Object Server.
     */
    public SyncUser getUser() {
        return configuration.getUser();
    }

    /**
     * Returns the {@link URI} describing the remote Realm which this session connects to and synchronizes changes with.
     *
     * @return {@link URI} describing the remote Realm.
     */
    public URI getServerUrl() {
        return configuration.getServerUrl();
    }

    public synchronized void start() {
        if (osSessionAvailable) {
            nativeStartSession(configuration.getPath());
        }

    }

    public synchronized void stop() {
        if (osSessionAvailable) {
            nativeStopSession(configuration.getPath());
        }
    }

    /**
     * Returns the state of this session.
     *
     * @return the current {@link SessionState} for this sessiOon.
     */
    public SessionState getState() {
        return SessionState.INITIAL;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if (getState() != SessionState.STOPPED) {
            RealmLog.warn("JNI Session was not closed before Java Session being finalized.");
        }
    }

    /**
     * Toggle if the underlying ObjectStore SyncSession object is still available.
     * @param osSessionAvailable
     */
    synchronized void objectStoreSessionAvailable(boolean osSessionAvailable) {
//        this.osSessionAvailable = osSessionAvailable;
//        if (osSessionAvailable) {
//            getSyncPolicy().onSessionCreated(this);
//        } else {
//            getSyncPolicy().onSessionDestroyed(this);
//        }
    }

    // Called from native code.
    // This callback will happen on the thread running the Sync Client.
    @KeepMember
    void notifySessionError(int errorCode, String errorMessage) {
        ObjectServerError error = new ObjectServerError(ErrorCode.fromInt(errorCode), errorMessage);
        if (errorHandler != null) {
            errorHandler.onError(this, error);
        }
    }

    public SyncPolicy getSyncPolicy() {
        return configuration.getSyncPolicy();
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

    private native void nativeStartSession(String path);
    private native void nativeStopSession(String path);
}

