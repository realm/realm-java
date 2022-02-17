/**
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

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import io.realm.annotations.RealmField
import io.realm.annotations.Required
import org.bson.types.ObjectId

open class SyncPerson(
        @PrimaryKey
        @RealmField(name = "_id")
        var id: ObjectId? = ObjectId(),
        var age: Long = 0,
        var dogs: RealmList<SyncDog> = RealmList(),
        var firstName: String = "",
        var lastName: String = ""
        // This field is not required by clients
        // But if added, it must always have the
        // same value as the partition value
        // used to open the Realm
        // @RealmField(name = "realm_id")
        // var realmId: String? = null
): RealmObject()