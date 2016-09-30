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

package io.realm.internal.objectserver;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Helper class for Object Server classes.
 */
public class SyncUtil {

    /**
     * Fully resolve an URL so all placeholder objects are replaced with the user identity.
     */
    public static URI getFullServerUrl(URI serverUrl, String userIdentity) {
        try {
            return new URI(serverUrl.toString().replace("/~/", "/" + userIdentity + "/"));
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Could not replace '/~/' with a valid user ID.", e);
        }
    }
}
