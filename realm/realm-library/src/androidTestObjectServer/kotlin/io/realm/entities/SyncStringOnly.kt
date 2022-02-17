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

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmField
import org.bson.types.ObjectId

open class SyncStringOnly : RealmObject() {

    companion object {
        const val CLASS_NAME = "SyncStringOnly"
        const val FIELD_ID = "_id"
        const val FIELD_CHARS = "chars"
    }

    @PrimaryKey
    @RealmField(name = "_id")
    var id: ObjectId = ObjectId()

    var chars: String? = null

}
