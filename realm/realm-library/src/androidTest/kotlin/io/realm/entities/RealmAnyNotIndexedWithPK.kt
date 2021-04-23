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

package io.realm.entities

import io.realm.RealmAny
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class RealmAnyNotIndexedWithPK(@PrimaryKey var pk: Long) : RealmObject() {
    companion object {
        const val FIELD_REALM_ANY = "realmAny"
    }

    constructor(): this(0)

    var realmAny: RealmAny? = RealmAny.nullValue()
}
