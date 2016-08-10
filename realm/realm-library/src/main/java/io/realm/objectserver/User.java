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

import io.realm.objectserver.credentials.ObjectServerCredentials;

/**
 * The credentials object describes a credentials on the Realm Object Server.
 *
 * It is a helper object that can hold multiple credentials at the same time and execute actions on those.
 *
 * This is e.g. useful if multiple Realms conceptually belong to the same "credentials". Then a
 *
 */
public class User {

    /**
     * Creates a credentials with a given set of credenStials. Realm Object Server will grant permission and access
     * if _any_ of the credentials are accepted.
     */
    public User(ObjectServerCredentials... credentialses) {

    }

    /**
     * Add a new set of credentials to a given credentials. If the credentials is an anonymous credentials, it will be converted to
     * @param credentials
     */
    public void addCredentials(ObjectServerCredentials credentials) {

    }

    public void removeCredentials(ObjectServerCredentials credentials) {

    }

    public void setUserEventsHandler(UserEventsHandler handler) {

    }
}
