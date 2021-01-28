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

import io.realm.*
import java.util.*

open class DictionaryClass : RealmObject() {

    // TODO: ObjectId and other Java primitives
    var myDogDictionary: RealmDictionary<MyDog>? = null
    var myUUIDDictionary: RealmDictionary<UUID>? = null
    var myMixedDictionary: RealmDictionary<Mixed>? = null
    var myBooleanDictionary: RealmDictionary<Boolean>? = null
    var myStringDictionary: RealmDictionary<String>? = null
    var myIntegerDictionary: RealmDictionary<Int>? = null
    var myFloatDictionary: RealmDictionary<Float>? = null

    // TODO: remove these, only used for inspiration for the proxy generator
    var myBooleanList: RealmList<Boolean>? = null
    var myDogList: RealmList<MyDog>? = null

    companion object {
        const val CLASS_NAME = "DictionaryClass"
        const val REALMMODEL_DICTIONARY_FIELD_NAME = "myDogDictionary"
        const val UUID_DICTIONARY_FIELD_NAME = "myUUIDDictionary"
        const val MIXED_DICTIONARY_FIELD_NAME = "myMixedDictionary"
        const val BOOLEAN_DICTIONARY_FIELD_NAME = "myBooleanDictionary"
        const val STRING_DICTIONARY_FIELD_NAME = "myStringDictionary"
        const val INTEGER_DICTIONARY_FIELD_NAME = "myIntegerDictionary"
        const val FLOAT_DICTIONARY_FIELD_NAME = "myFloatDictionary"
    }
}
