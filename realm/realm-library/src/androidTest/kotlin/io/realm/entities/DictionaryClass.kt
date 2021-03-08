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

import io.realm.Mixed
import io.realm.RealmDictionary
import io.realm.RealmObject
import io.realm.entities.embedded.EmbeddedSimpleChild
import org.bson.types.Decimal128
import org.bson.types.ObjectId
import java.util.*

open class DictionaryContainerClass : RealmObject() {

    val myRealmModelDictionary = RealmDictionary<StringOnly>()
    val myBooleanDictionary = RealmDictionary<Boolean>()
    val myStringDictionary = RealmDictionary<String>()
    val myIntDictionary = RealmDictionary<Int>()
    val myFloatDictionary = RealmDictionary<Float>()
    val myLongDictionary = RealmDictionary<Long>()
    val myShortDictionary = RealmDictionary<Short>()
    val myDoubleDictionary = RealmDictionary<Double>()
    val myByteDictionary = RealmDictionary<Byte>()
    val myBinaryDictionary = RealmDictionary<ByteArray>()
    val myDateDictionary = RealmDictionary<Date>()
    val myObjectIdDictionary = RealmDictionary<ObjectId>()
    val myUUIDDictionary = RealmDictionary<UUID>()
    val myDecimal128Dictionary = RealmDictionary<Decimal128>()
    val myMixedDictionary = RealmDictionary<Mixed>()

    companion object {
        const val CLASS_NAME = "DictionaryContainerClass"
    }
}

open class EmbeddedObjectDictionaryContainerClass : RealmObject() {
    val myEmbeddedObjectDictionary = RealmDictionary<EmbeddedSimpleChild>()
}
