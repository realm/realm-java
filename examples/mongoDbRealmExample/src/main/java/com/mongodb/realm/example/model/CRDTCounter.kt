/*
 * Copyright 2019 Realm Inc.
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
package com.mongodb.realm.example.model

import io.realm.MutableRealmInteger
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmField
import io.realm.annotations.Required
import org.bson.types.ObjectId

open class CRDTCounter : RealmObject() {

    @PrimaryKey
    @RealmField("_id")
    var id: ObjectId = ObjectId.get()
    @Required
    private val counter: MutableRealmInteger = MutableRealmInteger.valueOf(0L)

    val count: Long
        get() = this.counter.get()!!.toLong()

    fun incrementCounter(delta: Long) {
        counter.increment(delta)
    }

}
