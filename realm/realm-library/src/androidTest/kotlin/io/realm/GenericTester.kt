/*
 * Copyright 2021 Realm Inc.
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
import io.realm.entities.DictionaryAllTypes
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import io.realm.rule.BlockingLooperThread
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Generic tester for parameterized tests. Includes two types of `setUp` functions.
 */
interface GenericTester {
    fun setUp(config: RealmConfiguration, looperThread: BlockingLooperThread)
    fun setUp(configFactory: TestRealmConfigurationFactory)
    fun tearDown()
}

/**
 * TODO: rename SetContainerClass to "CollectionContainer" and add all possible RealmList, RealmSet
 *  and RealmDictionary fields in that class and use it for Dictionary and Set tests. Use this new
 *  container instead of "DictionaryAllTypes".
 */
fun createCollectionAllTypesManagedContainerAndAssert(
    realm: Realm,
    id: String? = null
): DictionaryAllTypes {
    var dictionaryAllTypesObject: DictionaryAllTypes? = null
    realm.executeTransaction { transactionRealm ->
        dictionaryAllTypesObject = transactionRealm.createObject()
        assertNotNull(dictionaryAllTypesObject)

        // Assign id if we have one
        if (id != null) {
            dictionaryAllTypesObject!!.columnString = id
        }
    }
    val allTypesObjectFromRealm = if (id == null) {
        realm.where<DictionaryAllTypes>().equalTo("columnString", "").findFirst()
    } else {
        realm.where<DictionaryAllTypes>().equalTo("columnString", id).findFirst()
    }
    assertEquals(dictionaryAllTypesObject, allTypesObjectFromRealm)
    return dictionaryAllTypesObject!!
}

/**
 * TODO: migrate set fields from AllTypes to a "CollectionContainer" class and remove this
 *  method once sets are done.
 */
fun createAllTypesManagedContainerAndAssert(
    realm: Realm,
    id: String? = null,
    inTransaction: Boolean = false
): AllTypes {
    if (!inTransaction) {
        realm.beginTransaction()
    }

    val allTypesObject: AllTypes = realm.createObject()
    assertNotNull(allTypesObject)

    // Assign id if we have one
    if (id != null) {
        allTypesObject.columnString = id
    }

    if (!inTransaction) {
        realm.commitTransaction()
    }

    val allTypesObjectFromRealm = if (id == null) {
        realm.where<AllTypes>().equalTo(AllTypes.FIELD_STRING, "").findFirst()
    } else {
        realm.where<AllTypes>().equalTo(AllTypes.FIELD_STRING, id).findFirst()
    }
    assertEquals(allTypesObject, allTypesObjectFromRealm)
    return allTypesObject
}
