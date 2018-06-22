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

import io.realm.*

/**
 * Returns the [SyncSession] associated with this Realm.
 *
 * @throws IllegalStateException if this Realm was not opened using a [SyncConfiguration].
 * @return the [SyncSession] associated with this Realm.
 */
val Realm.syncSession: SyncSession
    get() {
        if (!(this.configuration is SyncConfiguration)) {
            throw IllegalStateException("This method is only available on synchronized Realms")
        }
        return SyncManager.getSession(this.configuration as SyncConfiguration)
    }
