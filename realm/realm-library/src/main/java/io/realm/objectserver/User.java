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

import android.os.Handler;
import android.os.Looper;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Future;

import io.realm.RealmAsyncTask;
import io.realm.internal.IOException;
import io.realm.objectserver.internal.SyncUser;
import io.realm.objectserver.internal.Token;
import io.realm.objectserver.internal.network.AuthenticateResponse;
import io.realm.objectserver.internal.network.AuthenticationServer;
import io.realm.log.RealmLog;

/**
 * This class represents a user on the Realm Object Server.
 * TODO Rewrite this section
 */
public class User {

    private static RealmAsyncTask authenticateTask;
    private final SyncUser syncUser;

    private User(SyncUser user) {
        this.syncUser = user;
    }

    /**
     * Load a user that has previously been serialized using {@link #toJson()}.
     *
     * @param user JSON string representing the user.
     *
     * @return the user object.
     * @throws IllegalArgumentException if the JSON couldn't be converted to a valid {@link User} object.
     */
    public static User fromJson(String user) {
        try {
            JSONObject obj = new JSONObject(user);
            Token refreshToken = Token.from(obj.getJSONObject("refreshToken"));
            URL authUrl = new URL(obj.getString("authUrl"));
            // FIXME: Add support for  storing access tokens as well
            return new User(new SyncUser(refreshToken.identity(), refreshToken, authUrl));
        } catch (JSONException e) {
            throw new IllegalArgumentException("Could not parse user json: " + user, e);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("URL in JSON not valid: " + user, e);
        }
    }

    /**
     * Creates a user from an existing token. This user is automatically consider validated by the device, but the
     * Realm Object Server might determine that the token has expired or no longer is valid.
     *
     * This should only be used when debugging or testing. In most other cases the user object obtained from a
     * {@link #loginAsync(Credentials, String, Callback)} should be saved and reused. This can e.g. be done using a
     * {@link UserStore}.
     *
     * @param token token to represent user.
     * @see #getAccessToken()
     *
     */
    // FIXME Align with Cocoa on naming
    public static User fromToken(String token) {
        // Define a user with unlimited access. Object Server will reject any invalid access anyway.
        Token refreshToken = new Token(token, null, null, Long.MAX_VALUE, Token.Permission.values());
        SyncUser internalUser = new SyncUser(null, refreshToken, null);
        return new User(internalUser);
    }

    // FIXME Javadoc
    public static User login(final Credentials credentials, final URL authentificationUrl)
            throws ObjectServerError {
        return null; // TODO
    }

    /**
     * Login the user on the Realm Object Server
     *
     * @param credentials credentials to use
     * @param authenticationUrl URL to authenticateUser against
     * @param callback callback when login has completed or failed. This callback will always happen on the UI thread.
     * @throws IllegalArgumentException
     */
    // FIXME Return task that can be canceled
    public static RealmAsyncTask loginAsync(final Credentials credentials, final String authenticationUrl, final Callback callback) {
        final URL authUrl;
        try {
            authUrl = new URL(authenticationUrl);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid URL " + authenticationUrl + ".", e);
        }
        if (Looper.myLooper() == null) {
            throw new IllegalStateException("Asynchronous login is only possible from looper threads.");
        }

        final Handler handler = new Handler(Looper.myLooper());

        final AuthenticationServer server = SyncManager.getAuthServer();
        Future<?> authenticateRequest = SyncManager.NETWORK_POOL_EXECUTOR.submit(new Runnable() {
            @Override
            public void run() {
                // Don't retry authenticateUser requests. The app might want to respond to errors.
                try {
                    AuthenticateResponse result = server.authenticateUser(credentials, authUrl, credentials.shouldCreateUser());
                    if (result.isValid()) {
                        User user = new User(new SyncUser(result.getRefreshToken().identity(), result.getRefreshToken(), authUrl));
                        postSuccess(user);
                    } else {
                        postError(result.getError());
                    }
                } catch (IOException e) {
                    postError(new ObjectServerError(ErrorCode.IO_EXCEPTION, e));
                } catch (Throwable e) {
                    postError(new ObjectServerError(ErrorCode.UNKNOWN, e));
                }
            }

            private void postError(final ObjectServerError error) {
                RealmLog.info("Failed authenticating user.\n%s", error);
                if (callback != null) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onError(error);
                        }
                    });
                }
            }

            private void postSuccess(final User user) {
                RealmLog.info("Succeeded authenticating user.\n%s", user);
                if (callback != null) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onSuccess(user);
                        }
                    });
                }
            }
        });
        authenticateTask = new RealmAsyncTask(authenticateRequest, SyncManager.NETWORK_POOL_EXECUTOR);
        return authenticateTask;
    }

    public void logout() {
        // TODO Stop any session
        // TODO Clear all tokens
    }

    /**
     * Returns a JSON token representing this user.
     *
     * Possession of this JSON token can potentially grant access to data stored on the Realm Object Server, so it
     * should be treated as sensitive data.
     *
     * @return JSON string representing this user. It can be converted back into a real user object using
     *         {@link #fromJson(String)}.
     *
     * @see #fromJson(String)
     */
    public String toJson() {
        return syncUser.toJson();
    }

    /**
     * Returns the identity or key of this user on the Realm Object Server.
     *
     * @return Identity of the user on the Realm Object Server. If the user has logged out or the login has expired
     *         {@code null} is returned.
     */
    public String getIdentity() {
        return syncUser.getIdentity();
    }

    /**
     * Returns this user's access token. This is the users credential for accessing the Realm Object Server and should
     * be treated as sensitive data.
     *
     * @return The user's access token. If this user has logged out or the login has expired {@code null} is returned.
     */
    public String getAccessToken() {
        return syncUser.getRefreshToken().value();
    }

    @Override
    public String toString() {
        return super.toString();
        // FIXME Print representation of user, but be careful about printing anything sensitive
    }

    // Expose internal representation for other package protected classes
    SyncUser getSyncUser() {
        return syncUser;
    }

    public interface Callback {
        void onSuccess(User user);
        void onError(ObjectServerError error);
    }
}
