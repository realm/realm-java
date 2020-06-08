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

package io.realm.mongodb;

import io.realm.annotations.Beta;
import io.realm.internal.Util;
import io.realm.internal.objectstore.OsAppCredentials;
import io.realm.mongodb.auth.EmailPasswordAuth;

/**
 * Credentials represent a login with a given login provider, and are used by the MongoDB Realm to
 * verify the user and grant access. The {@link IdentityProvider#EMAIL_PASSWORD} provider is enabled
 * by default. All other providers must be enabled on MongoDB Realm to work.
 * <p>
 * Note that users wanting to login using Email/Password must register first using
 * {@link EmailPasswordAuth#registerUser(String, String)}.
 * </p>
 * Credentials are used the following way:
 * <pre>
 * {@code
 * // Example
 * App app = new App("app-id");
 * Credentials credentials = Credentials.emailPassword("email", "password");
 * User user = app.loginAsync(credentials, new App.Callback&lt;User&gt;() {
 *   \@Override
 *   public void onResult(Result&lt;User&gt; result) {
 *     if (result.isSuccess() {
 *       handleLogin(result.get());
 *     } else {
 *       handleError(result.getError());
 *     }
 *   }
 * ));
 * }
 * }
 * </pre>
 * @see <a href="https://docs.mongodb.com/stitch/authentication/providers/">Authentication Providers</a>
 */
@Beta
public class Credentials {

    OsAppCredentials osCredentials;

    /**
     * Creates credentials representing an anonymous user.
     * <p>
     * Logging the user out again means that data is lost with no means of recovery
     * and it isn't possible to share the user details across devices.
     * <p>
     * The anonymous user must be linked to another real user to preserve data after a log out.
     *
     * @return a set of credentials that can be used to log into MongoDB Realm using
     * {@link App#loginAsync(Credentials, App.Callback)}.
     */
    public static Credentials anonymous() {
        return new Credentials(OsAppCredentials.anonymous());
    }

    /**
     * Creates credentials representing a login using an API key.
     * <p>
     * This provider must be enabled on MongoDB Realm to work.
     *
     * @param key the API key to use for login.
     * @return a set of credentials that can be used to log into MongoDB Realm using
     * {@link App#loginAsync(Credentials, App.Callback)}.
     */
    public static Credentials apiKey(String key) {
        Util.checkEmpty(key, "id");
        return new Credentials(OsAppCredentials.apiKey(key));
    }

    /**
     * Creates credentials representing a login using an Apple ID token.
     * <p>
     * This provider must be enabled on MongoDB Realm to work.
     *
     * @param idToken the ID token generated when using your Apple login.
     * @return a set of credentials that can be used to log into MongoDB Realm using
     * {@link App#loginAsync(Credentials, App.Callback)}.
     */
    public static Credentials apple(String idToken) {
        Util.checkEmpty(idToken, "idToken");
        return new Credentials(OsAppCredentials.apple(idToken));
    }

    /**
     * FIXME
     * <p>
     * This provider must be enabled on MongoDB Realm to work.
     *
     * @return a set of credentials that can be used to log into MongoDB Realm using
     * {@link App#loginAsync(Credentials, App.Callback)}.
     */
    public static Credentials customFunction(String functionName, Object... arguments) {
        // FIXME: How to check arguments?
        Util.checkEmpty(functionName, "functionName");
        return new Credentials(OsAppCredentials.customFunction(functionName, arguments));
    }

    /**
     * Creates credentials representing a login using email and password.
     *
     * @param email email of the user logging in.
     * @param password password of the user logging in.
     * @return a set of credentials that can be used to log into MongoDB Realm using
     * {@link App#loginAsync(Credentials, App.Callback)}.
     */
    public static Credentials emailPassword(String email, String password) {
        Util.checkEmpty(email, "email");
        Util.checkEmpty(password, "password");
        return new Credentials(OsAppCredentials.emailPassword(email, password));
    }

    /**
     * Creates credentials representing a login using an Facebook access token.
     * <p>
     * This provider must be enabled on MongoDB Realm to work.
     *
     * @param accessToken the access token returned when logging in to Facebook.
     * @return a set of credentials that can be used to log into MongoDB Realm using
     * {@link App#loginAsync(Credentials, App.Callback)}.
     */
    public static Credentials facebook(String accessToken) {
        Util.checkEmpty(accessToken, "accessToken");
        return new Credentials(OsAppCredentials.facebook(accessToken));
    }

    /**
     * Creates credentials representing a login using an Google access token.
     * <p>
     * This provider must be enabled on MongoDB Realm to work.
     *
     * @param googleToken the access token returned when logging in to Google.
     * @return a set of credentials that can be used to log into MongoDB Realm using
     * {@link App#loginAsync(Credentials, App.Callback)}.
     */
    public static Credentials google(String googleToken) {
        Util.checkEmpty(googleToken, "googleToken");
        return new Credentials(OsAppCredentials.google(googleToken));
    }

    /**
     * Creates credentials representing a login using an JWT Token. This token is normally generated
     * after a custom OAuth2 login flow.
     * <p>
     * This provider must be enabled on MongoDB Realm to work.
     *
     * @param jwtToken the jwt token returned after a custom login to a another service.
     * @return a set of credentials that can be used to log into MongoDB Realm using
     * {@link App#loginAsync(Credentials, App.Callback)}.
     */
    public static Credentials jwt(String jwtToken) {
        Util.checkEmpty(jwtToken, "jwtToken");
        return new Credentials(OsAppCredentials.jwt(jwtToken));
    }

    /**
     * Returns the id for the provider used to authenticate with.
     *
     * @return the id identifying the chosen authentication provider.
     */
    public IdentityProvider getIdentityProvider() {
        return IdentityProvider.fromId(osCredentials.getProvider());
    }

    /**
     * Returns the credentials object serialized as a json string.
     *
     * @return a json serialized string of the credentials object.
     */
    public String asJson() {
        return osCredentials.asJson();
    }

    private Credentials(OsAppCredentials credentials) {
        this.osCredentials = credentials;
    }

    /**
     * This enum contains the list of identity providers supported by MongoDB Realm.
     * All of these except {@link #EMAIL_PASSWORD} must be enabled manually on MongoDB Realm to
     * work.
     *
     * @see <a href="https://docs.mongodb.com/stitch/authentication/providers/">Authentication Providers</a>
     */
    public enum IdentityProvider {
        ANONYMOUS("anon-user"),
        API_KEY(""), // FIXME
        APPLE("oauth2-apple"),
        CUSTOM_FUNCTION(""), // FIXME
        EMAIL_PASSWORD("local-userpass"),
        FACEBOOK("oauth2-facebook"),
        GOOGLE("oauth2-google"),
        JWT("jwt"),
        UNKNOWN("");

        /**
         * Create the identity provider from the ID string returned by MongoDB Realm.
         *
         * @param id the string identifier for the provider
         * @return the enum representing the provider or {@link #UNKNOWN} if no matching provider
         * was found.
         */
        public static IdentityProvider fromId(String id) {
            for (IdentityProvider value : values()) {
                if (value.getId().equals(id)) {
                    return value;
                }
            }
            return UNKNOWN;
        }

        private final String id;

        IdentityProvider(String id) {
            this.id = id;
        }

        /**
         * Return the string presentation of this identity provider.
         */
        public String getId() {
            return id;
        }
    }
}
