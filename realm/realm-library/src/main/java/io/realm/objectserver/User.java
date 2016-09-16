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
import java.util.concurrent.ThreadPoolExecutor;

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
     * Login the user on the Realm Object Server. This is done synchronously, so calling this method on the Android
     * UI thread will always crash. A logged in user is required to be able to create a {@link SyncConfiguration}.
     *
     * @param credentials credentials to use.
     * @param authenticationUrl Server that can authenticate against.
     * @throws ObjectServerError if the login failed.
     *
     * @see io.realm.objectserver.SyncConfiguration.Builder#user(User)
     */
    public static User login(final Credentials credentials, final String authenticationUrl) throws ObjectServerError {
        final URL authUrl;
        try {
            authUrl = new URL(authenticationUrl);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid URL " + authenticationUrl + ".", e);
        }

        final AuthenticationServer server = SyncManager.getAuthServer();
        try {
            AuthenticateResponse result = server.authenticateUser(credentials, authUrl);
            if (result.isValid()) {
                SyncUser syncUser = new SyncUser(result.getRefreshToken().identity(), result.getRefreshToken(), authUrl);
                User user = new User(syncUser);
                RealmLog.info("Succeeded authenticating user.\n%s", user);
                return user;
            } else {
                RealmLog.info("Failed authenticating user.\n%s", result.getError());
                throw result.getError();
            }
        } catch (IOException e) {
            throw new ObjectServerError(ErrorCode.IO_EXCEPTION, e);
        } catch (Throwable e) {
            throw new ObjectServerError(ErrorCode.UNKNOWN, e);
        }
    }

    /**
     * Login the user on the Realm Object Server. A logged in user is required to be able to create a
     * {@link SyncConfiguration}.
     *
     * @param credentials credentials to use.
     * @param authenticationUrl Server that can authenticate against.
     * @param callback callback when login has completed or failed. This callback will always happen on the UI thread.
     *
     * @see io.realm.objectserver.SyncConfiguration.Builder#user(User)
     */
    public static RealmAsyncTask loginAsync(final Credentials credentials, final String authenticationUrl, final Callback callback) {
        if (Looper.myLooper() == null) {
            throw new IllegalStateException("Asynchronous login is only possible from looper threads.");
        }
        final Handler handler = new Handler(Looper.myLooper());
        ThreadPoolExecutor networkPoolExecutor = SyncManager.NETWORK_POOL_EXECUTOR;
        Future<?> authenticateRequest = networkPoolExecutor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    User user = login(credentials, authenticationUrl);
                    postSuccess(user);
                } catch (ObjectServerError e) {
                    postError(e);
                }
            }

            private void postError(final ObjectServerError error) {
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

        return new RealmAsyncTask(authenticateRequest, networkPoolExecutor);
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
     * Returns the identity of this user on the Realm Object Server. The identity is a guaranteed to be unique
     * among all users on the Realm Object Server.
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

    // Expose internal representation for other package protected classes
    SyncUser getSyncUser() {
        return syncUser;
    }

    public interface Callback {
        void onSuccess(User user);
        void onError(ObjectServerError error);
    }
}
