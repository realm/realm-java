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

import io.realm.User;
import io.realm.internal.objectserver.Token;
import io.realm.Credentials;

/**
 * Interface for handling communication with Realm Object Servers.
 *
 * Note, any implementation of this class is not responsible for handling retries or error handling, it is
 * only responsible for executing a given network request.
 */
public interface AuthenticationServer {
    AuthenticateResponse authenticateUser(Credentials credentials, URL authenticationUrl);
    AuthenticateResponse authenticateRealm(Token refreshToken, URI path, URL authenticationUrl);
    RefreshResponse refresh(String token, URL authenticationUrl);
    LogoutResponse logout(User user, URL authenticationUrl);
}
