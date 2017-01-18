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

package io.realm.objectserver.utils;

import io.realm.SyncCredentials;
import io.realm.SyncUser;

// Must be in `io.realm.objectserver` to work around package protected methods.
public class UserFactory {
    private static final String USER_NAME = "test-user";
    private static final String PASSWORD = "myPassw0rd";

    public static SyncUser loginWithDefaultUser(String authUrl) {
        SyncCredentials credentials = SyncCredentials.usernamePassword(USER_NAME, PASSWORD, false);
        return SyncUser.login(credentials, authUrl);
    }

    public static SyncUser createDefaultUser(String authUrl) {
        SyncCredentials credentials = SyncCredentials.usernamePassword(USER_NAME, PASSWORD, true);
        return SyncUser.login(credentials, authUrl);
    }

    public static SyncUser createAdminUser(String authUrl) {
        // `admin` required as user identifier to be granted admin rights.
        SyncCredentials credentials = SyncCredentials.custom("admin", "debug", null);
        return SyncUser.login(credentials, authUrl);
    }
}
