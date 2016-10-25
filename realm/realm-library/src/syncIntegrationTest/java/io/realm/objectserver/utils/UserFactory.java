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

import java.net.URI;
import java.net.URISyntaxException;

import io.realm.SyncUser;
import io.realm.objectserver.utils.Constants;

// Must be in `io.realm.objectserver` to work around package protected methods.
public class UserFactory {
    // FIXME: Not working right now.
    /*
    public static User createDefaultUser(String SERVER_URL, String USER_TOKEN) {
        try {
            User user = User.createLocal();

            user.addAccessToken(new URI(SERVER_URL), USER_TOKEN);
            return user;
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
    */
}
