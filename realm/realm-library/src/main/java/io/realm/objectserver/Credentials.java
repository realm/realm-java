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

import java.util.UUID;

/**
 * Credentials represents a login with a 3rd party login provider in an oAuth2 login flow, and is used by the Realm
 * Object Server to verify the user and grant access.
 *
 * Logging into the Realm Object Server consists of the following steps:
 *
 * <ol>
 * <li>
 *     Login to 3rd party like Facebook, Google or Twitter. The result is usually an Authorization Grant that must be
 *     saved in a {@link Credentials} object of the proper type, e.g {@link Credentials#fromFacebook(String)} for a
 *     Facebook login.
 * </li>
 * <li>
 *     Authenticate a {@link User} through the Realm Object Server using these credentials. Once authenticated
 *     a Realm Object Server user is returned. This user can then be attached to a {@link SyncConfiguration}, which
 *     will make it possible to synchronize data between the local and remote Realm.
 *     <p>
 *     It is possible to persist the user object using e.g. the {@link UserStore} so logging
 *     into e.g Facebook is only required the first time the app is used.
 * </li>
 * </ol>
 *
 * <pre>
 * {@code
 * // Example
 *
 * Credentials credentials = Credentials.fromFacebook(getFacebookToken());
 * boolean createUser = true;
 * User.authenticateUser(credentials, new URL("http://objectserver.realm.io/auth", new User.Callback() {
 *     \@Override
 *     public void onSuccess(User user) {
 *          userStore.saveUser("key", user)
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
    private String field1;
    private String field2;
    private final boolean createUser;

    // Factory constructors

    /**
     * Creates credentials for a local user that is only know by this device.
     * Loosing these credentials or the User once it has been authenticated means that the data stored in
     * the Realm cannot be recovered.
     *
     * @see <a href="LINK_HERE">Tutorial showing how to authenticateUser using local credentials</a>
     */
    public static Credentials createLocal() {
        return new Credentials(LoginType.LOCAL, UUID.randomUUID().toString());
    }

    /**
     * Creates a credentials token based on a login with username and password.
     *
     * @see <a href="LINK_HERE">Tutorial showing how to authenticateUser using username and password</a>
     */
    public static Credentials fromUsernamePassword(String username, String password, boolean createUser) {
        return new Credentials(LoginType.USERNAME_PASSWORD, username, password, createUser);
    }

    /**
     * Creates a credentials token based on a Facebook login.
     *
     * @see <a href="LINK_HERE">Tutorial showing how to authenticateUser using the Facebook SDK</a>
     */
    public static Credentials fromFacebook(String facebookToken) {
        return new Credentials(LoginType.FACEBOOK, facebookToken);
    }

    private Credentials(LoginType type, String token) {
        this.loginType = type;
        this.field1 = token;
        this.createUser = false;
    }

    private Credentials(LoginType usernamePassword, String username, String password, boolean createUser) {
        this.loginType = LoginType.USERNAME_PASSWORD;
        this.field1 = username;
        this.field2 = password;
        this.createUser = createUser;
    }

    /**
     * Returns the type of login used to createFrom these credentials.
     * It is used by the authentication server to determine how these credentials should be validated.
     *
     * @return the login type.
     */
    public LoginType getLoginType() {
        return loginType;
    }

    /**
     * Returns the data in field 1. The type of information in this field will depend on the login type.
     *
     * @return the value of field1 of for these credentials.
     */
    public String getField1() {
        return field1;
    }

    /**
     * Returns the data in field 2. The type of information in this field will depend on the login type.
     *
     * @return the value of field2 of for these credentials.
     */
    public String getField2() {
        return field2;
    }


    /**
     * Returns {@code true} if a User should be created based on these credentials.
     * If the user already exists, this will fail.
     *
     * @return {@code true} if the user should be created on the Realm Object Server, {@code false} if it already exists.
     */
    public boolean shouldCreateUser() {
        return createUser;
    }

    /**
     * Enumeration of the different types of supported authentication method.
     */
    public enum LoginType {
        FACEBOOK,
        TWITTER,
        GOOGLE,
        USERNAME_PASSWORD,
        LOCAL
    }
}
