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

package io.realm.objectserver.credentials;

import java.net.URL;

/**
 * A credential object describing a login when a 3rd party login provider was used to provide access to the Realm
 * Object server
 *
 * These credentials are authenticated by the Realm Authentification Server and access and permissions are granted
 * based on this.
 *
 *
 * Logging into the Realm Mobile Platform normally consists of the following steps:
 *
 * TODO
 *
 * The result of authenticating is normally an AccessToken.
 *
 * This class is thread safe.
 */
public abstract class Credentials {

    private final boolean createUser;
    private final URL authentificationUrl;

    public Credentials(URL authentificationUrl, boolean createUser) {
        this.authentificationUrl = authentificationUrl;
        this.createUser = createUser;
    }

    /**
     * Returns the type of login used to create these credentials.
     * It is used by the authentication server to determine how these credentials should be validated.
     *
     * @return the login type.
     */
    public abstract LoginType getLoginType();

    public abstract String getToken();

    /**
     * Returns {@code true} if the authentification server should create a credentials based on these credentials if one
     * did not already exist. If this returns {@code false} and the authentification server cannot validate the credentials,
     * login will fail with XXX.
     *
     * @return {@code true} if the server can create a new credentials, {@code false} otherwise.
     */
    public boolean shouldCreateUser() {
        return createUser;
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
     * Enumeration of the different types of supported authentication method.
     */
    public enum LoginType {
        FACEBOOK,
        TWITTER,
        GOOGLE,
        USERNAME_PASSWORD,
        ACCESS_TOKEN
    }
}
