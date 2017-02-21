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
import java.util.concurrent.Future;

import io.realm.annotations.Beta;
import io.realm.internal.Keep;
import io.realm.internal.KeepMember;
import io.realm.internal.SyncObjectServerFacade;
import io.realm.internal.async.RealmAsyncTaskImpl;
import io.realm.internal.network.AuthenticateResponse;
import io.realm.internal.network.AuthenticationServer;
import io.realm.internal.network.ExponentialBackoffTask;
import io.realm.internal.network.NetworkStateReceiver;
import io.realm.internal.objectserver.ObjectServerUser;
import io.realm.log.RealmLog;

/**
 * @Beta
 * This class represents the connection to the Realm Object Server for one {@link SyncConfiguration}.
 * <p>
 * A Session is created by opening a Realm instance using that configuration. Once a session has been created,
 * it will continue to exist until the app is closed or all Threads using this {@link SyncConfiguration} closes their respective {@link Realm}s.
 * <p>
 * A session is fully controlled by Realm, but can provide additional information in case of errors.
 * It is passed along in all {@link SyncSession.ErrorHandler}s.
 * <p>
 * This object is thread safe.
 */
@Keep
@Beta
public class SyncSession {
    private final SyncConfiguration configuration;
    private final ErrorHandler errorHandler;

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

    // Called from native code.
    // This callback will happen on the thread running the Sync Client.
    @KeepMember
    void notifySessionError(int errorCode, String errorMessage) {
        ObjectServerError error = new ObjectServerError(ErrorCode.fromInt(errorCode), errorMessage);
        if (errorHandler != null) {
            errorHandler.onError(this, error);
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

    private RealmAsyncTask networkRequest;
    private NetworkStateReceiver.ConnectionListener networkListener;

    void accessToken(final AuthenticationServer authServer) {
        if (NetworkStateReceiver.isOnline(SyncObjectServerFacade.getApplicationContext())) {
            authenticate(authServer);
        } else {
            // Wait for connection to become available, before trying again.
            // The Session might potentially stay in this state for the lifetime of the application.
            // This is acceptable.
            networkListener = new NetworkStateReceiver.ConnectionListener() {
                @Override
                public void onChange(boolean connectionAvailable) {
                    if (connectionAvailable) {
                        authenticate(authServer);
                        NetworkStateReceiver.removeListener(this);
                    }
                }
            };
            NetworkStateReceiver.addListener(networkListener);
        }
    }

    private void authenticate(final AuthenticationServer authServer) {
        authenticateRealm(authServer, new Runnable() {
            @Override
            public void run() {
                RealmLog.debug("Session[%s]: Access token acquired", configuration.getPath());
                //FIXME should update the correspondent user (work to be done when implementing refresh with timer)
                nativeRefreshAccessToken(configuration.getPath(), getUser().getSyncUser().getAccessToken(configuration.getServerUrl()).value(), configuration.getServerUrl().toString());
            }
        }, new SyncSession.ErrorHandler() {
            @Override
            public void onError(SyncSession s, ObjectServerError error) {
                RealmLog.debug("Session[%s]: Failed to get access token (%d)", configuration.getPath(), error.getErrorCode());
                errorHandler.onError(SyncSession.this, error);
            }
        });
    }

    // Authenticate by getting access tokens for the specific Realm
    private void authenticateRealm(final AuthenticationServer authServer, final Runnable onSuccess, final ErrorHandler errorHandler) {
        if (networkRequest != null) {
            networkRequest.cancel();
        }
        // Authenticate in a background thread. This allows incremental backoff and retries in a safe manner.
        Future<?> task = SyncManager.NETWORK_POOL_EXECUTOR.submit(new ExponentialBackoffTask<AuthenticateResponse>() {
            @Override
            protected AuthenticateResponse execute() {
                return authServer.loginToRealm(
                        getUser().getAccessToken(),//refresh token in fact
                        configuration.getServerUrl(),
                        getUser().getSyncUser().getAuthenticationUrl()
                );
            }

            @Override
            protected void onSuccess(AuthenticateResponse response) {
                ObjectServerUser.AccessDescription desc = new ObjectServerUser.AccessDescription(
                        response.getAccessToken(),
                        configuration.getPath(),
                        configuration.shouldDeleteRealmOnLogout()
                );
                getUser().getSyncUser().addRealm(configuration.getServerUrl(), desc);
                onSuccess.run();
            }

            @Override
            protected void onError(AuthenticateResponse response) {
                errorHandler.onError(SyncSession.this, response.getError());
            }
        });
        networkRequest = new RealmAsyncTaskImpl(task, SyncManager.NETWORK_POOL_EXECUTOR);
    }

    private native void nativeRefreshAccessToken(String path, String accessToken, String authURL);
}

