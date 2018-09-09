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

package io.realm.internal.network;

import java.net.URI;
import java.net.URL;

import javax.annotation.Nullable;

import io.realm.SyncCredentials;
import io.realm.SyncUser;
import io.realm.internal.objectserver.Token;

/**
 * Interface for handling communication with Realm Object Servers.
 * <p>
 * Note, no implementation of this class is responsible for handling retries or error handling. It is
 * only responsible for executing a given network request.
 */
public interface AuthenticationServer {

    /**
     * Overrides the default header name used to send Realm Object Server credentials.
     * The Realm Object Server must be setup to handle this specifically.
     */
    void setAuthorizationHeaderName(String headerName, @Nullable String host);

    /**
     * Add a custom header that should be applied to all HTTP requests made by the authentication
     * server.
     */
    void addHeader(String headerName, String headerValue, @Nullable String host);

    /**
     * Clear any custom header settings (Authorization and others).
     */
    void clearCustomHeaderSettings();

    /**
     * Login a User on the Object Server. This will create a "UserToken" (Currently called RefreshToken) that acts as
     * the users credentials.
     */
    AuthenticateResponse loginUser(SyncCredentials credentials, URL authenticationUrl);

    /**
     * Requests access to a specific Realm. Only users with a valid user token can ask for permission to a remote Realm.
     * Permission to a Realm is granted through an "AccessToken". Each Realm have their own access token, and all
     * tokens should be managed by {@link SyncUser}.
     */
    AuthenticateResponse loginToRealm(Token userToken, URI serverUrl,  URL authenticationUrl);

    /**
     * When the Object Server returns the user token, it also sends a timestamp for when the token expires.
     * Before it expires, the client should try to refresh the token, effectively keeping the user logged in on the
     * Object Server. Failing to do so will cause a "soft logout", where the User will have limited access rights.
     */
    AuthenticateResponse refreshUser(Token userToken, URI serverUrl, URL authenticationUrl);

    /**
     * Logs out the user on the Object Server by invalidating the refresh token. Each device should be given their
     * own refresh token, but if the refresh token for some reason was shared or stolen all these devices will be
     * logged out as well.
     */
    LogoutResponse logout(Token userToken, URL authenticationUrl);

    /**
     * Changes a user's password.
     */
    ChangePasswordResponse changePassword(Token userToken, String newPassword, URL authenticationUrl);

    /**
     * Changes a user's password using admin account.
     */
    ChangePasswordResponse changePassword(Token adminToken, String userID, String newPassword, URL authenticationUrl);

    /**
     * Looks up a {@code SyncUser} using the identity provider {@link io.realm.SyncCredentials.IdentityProvider}
     * used when the account was created and the username or email used to create the account for the first time
     * what is needed will depend on what type of {@link SyncCredentials} was used.
     */
    LookupUserIdResponse retrieveUser(Token adminToken, String provider, String providerId, URL authenticationUrl);

    /**
     * Request a password reset for the user identified by the provided email.
     */
    UpdateAccountResponse requestPasswordReset(String email, URL authenticationUrl);

    /**
     * Complete a password reset by sending the one-time token and the new password.
     */
    UpdateAccountResponse completePasswordReset(String token, String newPassword, URL authenticationUrl);

    /**
     * Request an email confirmation.
     */
    UpdateAccountResponse requestEmailConfirmation(String email, URL authenticationUrl);

    /**
     * Complete an email confirmation by sending the token contained in the email.
     */
    UpdateAccountResponse confirmEmail(String confirmationToken, URL authenticationUrl);

}
