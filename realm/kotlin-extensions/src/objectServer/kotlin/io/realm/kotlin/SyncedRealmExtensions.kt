/*
 * Copyright 2018 Realm Inc.
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
package io.realm.kotlin

import io.realm.Realm
import io.realm.RealmModel
import io.realm.SyncConfiguration
import io.realm.SyncManager
import io.realm.SyncSession
import io.realm.sync.permissions.ClassPermissions


/**
 * Returns the [SyncSession] associated with this Realm.
 *
 * @return the [SyncSession] associated with this Realm.
 * @throws IllegalStateException if the Realm is not a synchronized Realm.
 */
val Realm.syncSession: SyncSession
    get() {
        if (!(this.configuration is SyncConfiguration)) {
            throw IllegalStateException("This method is only available on synchronized Realms")
        }
        return SyncManager.getSession(this.configuration as SyncConfiguration)
    }

/**
 * Returns all permissions associated with the given class. Attach a change listener using
 * [ClassPermissions.addChangeListener] to be notified about any future changes.
 *
 * @return the permissions for the given class or `null` if no permissions where found.
 * @throws RealmException if the class is not part of this Realms schema.
 * @throws IllegalStateException if the Realm is not a synchronized Realm.
 */
inline fun <reified T : RealmModel> Realm.classPermissions(): ClassPermissions {
    if (!(this.configuration is SyncConfiguration)) {
        throw java.lang.IllegalStateException("This method is only available on synchronized Realms")
    }
    return this.getPermissions(T::class.java)
}
