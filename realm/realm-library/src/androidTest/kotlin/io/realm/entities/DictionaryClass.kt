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

import io.realm.RealmDictionary
import io.realm.RealmList
import io.realm.RealmObject

open class DictionaryClass : RealmObject() {

    // TODO: having this field causes ThreadStressTests to crash for some obscure reason after
    //  updating core to v11, which we need to investigate
//    var myMixedDictionary: RealmDictionary<Mixed>? = null
    var myBooleanDictionary: RealmDictionary<Boolean>? = null

    // FIXME: remove non-dictionary fields, they are only used for inspiration
    var myBooleanList: RealmList<Boolean>? = null

    companion object {
        const val CLASS_NAME = "DictionaryClass"
        const val MIXED_DICTIONARY_FIELD_NAME = "myMixedDictionary"
        const val BOOLEAN_DICTIONARY_FIELD_NAME = "myBooleanDictionary"
    }
}
