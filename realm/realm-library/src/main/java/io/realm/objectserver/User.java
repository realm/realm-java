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

import java.net.URL;

/**
 * The credentials object describes a credentials on the Realm Object Server.
 *
 * It is a helper object that can hold multiple credentials at the same time and execute actions on those.
 *
 * This is e.g. useful if multiple Realms conceptually belong to the same "credentials". Then a
 *
 */
public class User {

    private boolean createUserOnLogin = false;
    private URL authentificationUrl;
    private String localId;
    Credentials credentials;

    /**
     * Gets the default user for this app installation. This user is an anonymous user, but is the same across app
     * restarts. Re-installating the app
     *
     * @see #createAnonymousUser()
     * @see #toJson()
     */
    public static User defaultUser() {
        return null;
    }

    /**
     * Create an anonymous or local user. An anonymous user is only known by the device. Data will still be synchronized
     * to a remote Realm, but can only be accessed again through this user object.
     *
     * WARNING: Not persisting this user across app restarts mean that all data will be lost.
     *
     * @see #toJson();
     */
    public static User createAnonymousUser() {
        return null;
    }

    /**
     * Load a user that has previously been saved using {@link #toJson()}.
     *
     * @param user Json string representing the user.
     *
     * @return the user object.
     */
    public static User fromJson(String user) {
        return null;
    }


    public static User fromAccessToken(String accessToken) {
        return null;
    }

    public boolean isAuthenticated() {
        return false;
    }

    public void refresh() {

    }

    public void authenticate(Credentials credentials, URL authentificationUrl, Callback callback) {

    }

    public void logout() {

    }

    /**
     * Returns {@code true} if the authentification server should create a credentials based on these credentials if one
     * did not already exist. If this returns {@code false} and the authentification server cannot validate the credentials,
     * login will fail with XXX.
     *
     * @return {@code true} if the server can create a new credentials, {@code false} otherwise.
     */
    public boolean isCreatedOncreateUserOnLogin() {
        return false;
    }

    /**
     * Returns the URL of an Realm Mobile Platform Authentification Server that can validate these credentials.
     * If an invalid server is returned, login will fail with XXX.
     *
     * @return the authentification server URL that can validate these credentials.
     */
    public URL getAuthentificationUrl() {
        return authentificationUrl;
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
        return "";
    }

    public String getId() {
        return null;
    }

    public interface Callback {
        void onSuccess(User user);
        void onError(int errorCode, String errorMsg);
    }
}
