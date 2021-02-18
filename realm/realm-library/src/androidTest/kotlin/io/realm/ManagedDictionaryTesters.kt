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

import io.realm.entities.AllTypes
import io.realm.kotlin.where
import kotlin.test.fail

class ManagedLong(
        private val realm: Realm,
        private val testKeys: List<String>,
        private val testValues: List<Long>
) : DictionaryTester {

    override fun isTesterManaged(): Boolean = true
    override fun constructorWithAnotherMap() = fail("Not available in managed mode.")

    override fun isManaged() {
////        executeManagedOperation(realm) { allTypesObject ->
////            allTypesObject.columnLongDictionary = initDictionary(testKeys, testValues)
////        }
//        val dictionary = initDictionary(testKeys, testValues)
//        executeManagedOperation(realm, allTypesObject.columnLongDictionary) { allTypesObject ->
//            allTypesObject.columnLongDictionary = initDictionary(testKeys, testValues)
//        }
    }

    override fun isValid() {
        TODO("Not yet implemented")
    }

    override fun isFrozen() {
        TODO("Not yet implemented")
    }

    override fun size() {
        TODO("Not yet implemented")
    }

    override fun isEmpty() {
        TODO("Not yet implemented")
    }

    override fun containsKey() {
        TODO("Not yet implemented")
    }

    override fun containsValue() {
        TODO("Not yet implemented")
    }

    override fun get() {
        TODO("Not yet implemented")
    }

    override fun put() {
        TODO("Not yet implemented")
    }

    override fun remove() {
        TODO("Not yet implemented")
    }

    override fun putAll() {
        TODO("Not yet implemented")
    }

    override fun clear() {
        TODO("Not yet implemented")
    }

    override fun keySet() {
        TODO("Not yet implemented")
    }

    override fun values() {
        TODO("Not yet implemented")
    }

    override fun entrySet() {
        TODO("Not yet implemented")
    }

    override fun freeze() {
        TODO("Not yet implemented")
    }

}
