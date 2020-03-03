/*
 * Copyright 2020 Realm Inc.
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

import io.realm.internal.Util;
import io.realm.internal.objectstore.OsAppCredentials;


/**
 * Credentials represent a login with a 3rd party login provider in an OAuth2 login flow, and are used by the Realm
 * Object Server to verify the user and grant access.
 * <p>
 * Logging into the Realm Object Server consists of the following steps:
 * <ol>
 * <li>
 *     Log in to 3rd party provider (Facebook or Google). The result is usually an Authorization Grant that must be
 *     saved in a {@link RealmCredentials} object of the proper type e.g., {@link RealmCredentials#facebook(String)} for a
 *     Facebook login.
 * </li>
 * <li>
 *     Authenticate a {@link RealmUser} through the Object Server using these credentials. Once authenticated,
 *     an Object Server user is returned. Then this user can be attached to a {@link io.realm.SyncConfiguration}, which
 *     will make it possible to synchronize data between the local and remote Realm.
 *     <p>
 *     It is possible to persist the user object e.g., using the {@link UserStore}. That means, logging
 *     into an OAuth2 provider is only required the first time the app is used.
 * </li>
 * </ol>
 *
 * <pre>
 * {@code
 * // Example
 *
 * Credentials credentials = Credentials.facebook(getFacebookToken());
 * User.login(credentials, "http://objectserver.realm.io/auth", new User.Callback() {
 *     \@Override
 *     public void onSuccess(User user) {
 *          // User is now authenticated and be be used to open Realms.
 *     }
 *
 *     \@Override
 *     public void onError(ObjectServerError error) {
 *
 *     }
 * });
 * }
 * </pre>
 */
public class RealmCredentials {

    OsAppCredentials osCredentials;

    /**
     * FIXME
     * Creates credentials anonymously.
     *
     *  Note: logging the user out again means that data is lost with no means of recovery
     *  and it isn't possible to share the user details across devices.
     *
     * @return a set of credentials that can be used to log into the Object Server using
     * {@link RealmApp#loginAsync(RealmCredentials, RealmApp.Callback)}.
     */
    public static RealmCredentials anonymous() {
        return new RealmCredentials(OsAppCredentials.anonymous());
    }

    /**
     * FIXME
     */
    public static RealmCredentials apiKey(String key) {
        assertStringNotEmpty(key, "key");
        return new RealmCredentials(OsAppCredentials.apiKey(key));
    }

    /**
     * FIXME
     */
    public static RealmCredentials apple(String idToken) {
        assertStringNotEmpty(idToken, "idToken");
        return new RealmCredentials(OsAppCredentials.apple(idToken));
    }

    /**
     * FIXME
     */
    public static RealmCredentials customFunction(String functionName, Object... arguments) {
//        assertStringNotEmpty(idToken, "idToken");
        return new RealmCredentials(OsAppCredentials.customFunction(functionName, arguments));
    }

    /**
     * FIXME
     */
    public static RealmCredentials emailPassword(String email, String password) {
        assertStringNotEmpty(email, "email");
        assertStringNotEmpty(password, "password");
        return new RealmCredentials(OsAppCredentials.emailPassword(email, password));
    }

    /**
     * FIXME
     * Creates credentials based on a Facebook login.
     *
     * @param accessToken a facebook userIdentifier acquired by logging into Facebook.
     * @return a set of credentials that can be used to log into the Object Server using
     * {@link RealmApp#loginAsync(RealmCredentials, RealmApp.Callback)}.
     * @throws IllegalArgumentException if user name is either {@code null} or empty.
     */
    public static RealmCredentials facebook(String accessToken) {
        assertStringNotEmpty(accessToken, "accessToken");
        return new RealmCredentials(OsAppCredentials.facebook(accessToken));
    }

    /**
     * FIXME
     * Creates credentials based on a Google login.
     *
     * @param googleToken a google userIdentifier acquired by logging into Google.
     * @return a set of credentials that can be used to log into the Object Server using
     * {@link RealmApp#loginAsync(RealmCredentials, RealmApp.Callback)}.
     * @throws IllegalArgumentException if user name is either {@code null} or empty.
     */
    public static RealmCredentials google(String googleToken) {
        assertStringNotEmpty(googleToken, "googleToken");
        return new RealmCredentials(OsAppCredentials.google(googleToken));
    }

    /**
     * FIXME
     * Creates credentials based on a JSON Web Token (JWT).
     *
     * @param jwtToken a JWT token that identifies the user.
     * @return a set of credentials that can be used to log into the Object Server using
     * {@link RealmApp#loginAsync(RealmCredentials, RealmApp.Callback)}.
     * @throws IllegalArgumentException if the token is either {@code null} or empty.
     */
    public static RealmCredentials jwt(String jwtToken) {
        assertStringNotEmpty(jwtToken, "jwtToken");
        return new RealmCredentials(OsAppCredentials.jwt(jwtToken));
    }

    /**
     * Returns the key for the provider used to authenticate with.
     *
     * @return the key identifying the chosen authentication provider.
     */
    public String getIdentityProvider() {
        return osCredentials.getProvider();
    }

    /**
     * Returns the credentials object serialized as a json string.
     *
     * @return a json serialized string of the credentials object.
     */
    public String asJson() {
        return osCredentials.asJson();
    }

    private static void assertStringNotEmpty(String string, String message) {
        //noinspection ConstantConditions
        if (Util.isEmptyString(string)) {
            throw new IllegalArgumentException("Non-null '" + message + "' required.");
        }
    }

    private RealmCredentials(OsAppCredentials credentials) {
        this.osCredentials = credentials;
    }
}
