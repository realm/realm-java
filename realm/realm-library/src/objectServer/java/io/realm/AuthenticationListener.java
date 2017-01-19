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

package io.realm;

/**
 * Interface describing events related to Users and their authentication
 */
public interface AuthenticationListener {
    /**
     * A user was logged into the Object Server
     *
     * @param user {@link SyncUser} that is now logged in.
     */
    void loggedIn(SyncUser user);

    /**
     * A user was successfully logged out from the Object Server.
     *
     * @param user {@link SyncUser} that was successfully logged out.
     */
    void loggedOut(SyncUser user);
}
