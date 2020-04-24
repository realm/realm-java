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
package io.realm

import io.realm.admin.ServerAdmin

/**
 * Resets the Realm Application and delete all local state.
 *
 * Trying to access any Sync or Realm App API's after this has been called has undefined
 * behavior.
 */
fun RealmApp.close() {
    ServerAdmin().deleteAllUsers()
    this.syncManager.reset()
    RealmApp.CREATED = false
    BaseRealm.applicationContext = null // Required for Realm.init() to work
}

/**
 * Helper function for quickly logging in test users.
 * This only works if users in the Realm Application are configured to be automatically confirmed.
 */
fun RealmApp.registerUserAndLogin(email: String, password: String): RealmUser {
    emailPasswordAuth.registerUser(email, password)
    return login(RealmCredentials.emailPassword(email, password))
}
