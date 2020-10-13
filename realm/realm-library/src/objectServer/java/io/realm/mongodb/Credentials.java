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

import org.bson.Document;

import io.realm.annotations.Beta;
import io.realm.internal.Util;
import io.realm.internal.objectstore.OsAppCredentials;
import io.realm.mongodb.auth.EmailPasswordAuth;

/**
 * Credentials represent a login with a given login provider, and are used by the MongoDB Realm to
 * verify the user and grant access. The {@link Provider#EMAIL_PASSWORD} provider is enabled
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
 *
 * @see <a href="https://docs.mongodb.com/realm/authentication/providers/">Authentication Providers</a>
 */
@Beta
public class Credentials {

    OsAppCredentials osCredentials;

    private final Provider identityProvider;

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
        return new Credentials(OsAppCredentials.anonymous(), Provider.ANONYMOUS);
    }

    /**
     * Creates credentials representing a login using a user API key.
     * <p>
     * This provider must be enabled on MongoDB Realm to work.
     *
     * @param key the API key to use for login.
     * @return a set of credentials that can be used to log into MongoDB Realm using
     * {@link App#loginAsync(Credentials, App.Callback)}.
     */
    public static Credentials apiKey(String key) {
        Util.checkEmpty(key, "key");
        return new Credentials(OsAppCredentials.apiKey(key), Provider.API_KEY);
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
        return new Credentials(OsAppCredentials.apple(idToken), Provider.APPLE);
    }

    /**
     * Creates credentials representing a remote function from MongoDB Realm using a
     * {@link Document} which will be parsed as an argument to the remote function, so the keys must
     * match the format and names the function expects.
     * <p>
     * This provider must be enabled on MongoDB Realm to work.
     *
     * @param arguments document containing the function arguments.
     * @return a set of credentials that can be used to log into MongoDB Realm using
     * {@link App#loginAsync(Credentials, App.Callback)}.
     */
    public static Credentials customFunction(Document arguments) {
        Util.checkNull(arguments, "arguments");
        return new Credentials(OsAppCredentials.customFunction(arguments),
                Provider.CUSTOM_FUNCTION);
    }

    /**
     * Creates credentials representing a login using email and password.
     *
     * @param email    email of the user logging in.
     * @param password password of the user logging in.
     * @return a set of credentials that can be used to log into MongoDB Realm using
     * {@link App#loginAsync(Credentials, App.Callback)}.
     */
    public static Credentials emailPassword(String email, String password) {
        Util.checkEmpty(email, "email");
        Util.checkEmpty(password, "password");
        return new Credentials(OsAppCredentials.emailPassword(email, password),
                Provider.EMAIL_PASSWORD);
    }

    /**
     * Creates credentials representing a login using a Facebook access token.
     * <p>
     * This provider must be enabled on MongoDB Realm to work.
     *
     * @param accessToken the access token returned when logging in to Facebook.
     * @return a set of credentials that can be used to log into MongoDB Realm using
     * {@link App#loginAsync(Credentials, App.Callback)}.
     */
    public static Credentials facebook(String accessToken) {
        Util.checkEmpty(accessToken, "accessToken");
        return new Credentials(OsAppCredentials.facebook(accessToken), Provider.FACEBOOK);
    }

    /**
     * Creates credentials representing a login using a Google Authorization Code.
     * <p>
     * This provider must be enabled on MongoDB Realm to work.
     *
     * @param authorizationCode the authorization code returned when logging in to Google.
     * @return a set of credentials that can be used to log into MongoDB Realm using
     * {@link App#loginAsync(Credentials, App.Callback)}.
     */
    public static Credentials google(String authorizationCode) {
        Util.checkEmpty(authorizationCode, "authorizationCode");
        return new Credentials(OsAppCredentials.google(authorizationCode), Provider.GOOGLE);
    }

    /**
     * Creates credentials representing a login using a JWT Token. This token is normally generated
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
        return new Credentials(OsAppCredentials.jwt(jwtToken), Provider.JWT);
    }

    /**
     * Returns the identity provider used to authenticate with.
     *
     * @return the provider identifying the chosen credentials.
     */
    public Provider getIdentityProvider() {
        String nativeProvider = osCredentials.getProvider();
        String id = identityProvider.getId();

        // Sanity check - ensure nothing changed in the OS
        if (nativeProvider.equals(id)) {
            return identityProvider;
        } else {
            throw new AssertionError("The provider from the Object Store differs from the one in Realm.");
        }
    }

    /**
     * Returns the credentials object serialized as a json string.
     *
     * @return a json serialized string of the credentials object.
     */
    public String asJson() {
        return osCredentials.asJson();
    }

    private Credentials(OsAppCredentials credentials, Provider identityProvider) {
        this.osCredentials = credentials;
        this.identityProvider = identityProvider;
    }

    /**
     * This enum contains the list of identity providers supported by MongoDB Realm.
     * All of these except {@link #EMAIL_PASSWORD} must be enabled manually on MongoDB Realm to
     * work.
     *
     * @see <a href="https://docs.mongodb.com/realm/authentication/providers/">Authentication Providers</a>
     */
    public enum Provider {
        ANONYMOUS("anon-user"),
        API_KEY("api-key"),    // same value as API_KEY as per OS specifications
        APPLE("oauth2-apple"),
        CUSTOM_FUNCTION("custom-function"),
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
        public static Provider fromId(String id) {
            for (Provider value : values()) {
                if (value.getId().equals(id)) {
                    return value;
                }
            }
            return UNKNOWN;
        }

        private final String id;

        Provider(String id) {
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
