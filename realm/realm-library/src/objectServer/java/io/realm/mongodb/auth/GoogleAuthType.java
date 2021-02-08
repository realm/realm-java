/*
 * Copyright 2020 Realm Inc.
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

package io.realm.mongodb.auth;

/**
 * This enum contains the list of Google authentication types supported by MongoDB Realm.
 *
 * @see <a href="https://docs.mongodb.com/realm/authentication/google">Google Authentication</a>
 */
public enum GoogleAuthType {
    AUTH_CODE,
    ID_TOKEN
}
