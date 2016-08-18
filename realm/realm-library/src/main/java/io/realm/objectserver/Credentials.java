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

/**
 * Credentials represents a login with a 3rd party login provider in an oAuth2 login flow, and is used by the Realm
 * Authorization Server to verify the user and grant access to the Realm Object Server.
 *
 * Logging into the Realm Object Server consists of the following steps:
 *
 * <ol>
 * <li>
 *     Login to 3rd party like Facebook, Google or Twitter. The result is usually an Authorization Grant, that must be
 *     saved in a {@link Credentials} object of the proper type, e.g {@link Credentials#fromFacebook(String)} for a
 *     Facebook login.
 * </li>
 * <li>
 *     Authenticate a {@link User} through the Realm Authentication Server using these credentials. Once authenticated
 *     a Realm Object Server user is returned. This represents can then be used to connect to the Realm Object Server
 *     and synchronize data between the local and remote Realm.
 *     <p>
 *     It is possible to persist the Realm Object Server user so logging into e.g Facebook is only required the first
 *     time the app is used.
 * </li>
 * </ol>
 *
 * <pre>
 * {@code
 * // Example
 *
 * Credentials credentials = Credentials.fromFacebook(getFacebookToken());
 * boolean createUser = true;
 * User.authenticate(credentials, new URL("http://objectserver.realm.io/auth", createUser, new User.Callback() {
 *     \@Override
 *     public void onSuccess(User user) {
 *          SyncManager.saveUser("key", user)
 *          // User is now authenticated and be be used to open Realms.
 *     }
 *
 *     \@Override
 *     public void onError(int errorCode, String errorMsg) {
 *
 *     }
 * });
 * }
 * </pre>
 */
public class Credentials {

    private LoginType loginType;
    private String token;

    // Factory constructors

    /**
     * Creates a credentials token based on a Facebook login.
     *
     * @see <a href="LINK_HERE">Tutorial showing how to authenticate using the Facebook SDK</a>
     */
    public static Credentials fromFacebook(String facebookToken) {
        return new Credentials(LoginType.FACEBOOK, facebookToken);
    }

    /**
     * Creates a credentials token based on a Twitter login.
     *
     * @see <a href="LINK_HERE">Tutorial showing how to authenticate using the Twitter SDK</a>
     */
    public static Credentials fromTwitter(String twitterToken) {
        return new Credentials(LoginType.TWITTER, twitterToken);
    }

    /**
     * Creates a credentials token based on a login with username and password.
     *
     * @see <a href="LINK_HERE">Tutorial showing how to authenticate using username and password</a>
     */
    public static Credentials fromUsernamePassword(String username, String password) {
        return new Credentials(LoginType.USERNAME_PASSWORD, username, password);
    }

    /**
     * Creates a credentials token based on a Google login.
     *
     * @see <a href="LINK_HERE">Tutorial showing how to authenticate using username and password</a>
     */
    public static Credentials fromGoogle(String googleToken) {
        return new Credentials(LoginType.GOOGLE, googleToken);
    }

    private Credentials(LoginType type, String token) {
        this.loginType = type;
        this.token = token;
    }

    public Credentials(LoginType usernamePassword, String username, String s) {

    }

    /**
     * Returns the type of login used to create these credentials.
     * It is used by the authentication server to determine how these credentials should be validated.
     *
     * @return the login type.
     */
    public LoginType getLoginType() {
        return loginType;
    }

    /**
     * Enumeration of the different types of supported authentication method.
     */
    public enum LoginType {
        FACEBOOK,
        TWITTER,
        GOOGLE,
        USERNAME_PASSWORD,
    }
}
