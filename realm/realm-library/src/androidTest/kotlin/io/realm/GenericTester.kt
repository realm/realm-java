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

fun createAllTypesManagedContainerAndAssert(
        realm: Realm,
        id: String? = null
): AllTypes {
    var allTypesObject: AllTypes? = null
    realm.executeTransaction { transactionRealm ->
        allTypesObject = transactionRealm.createObject()
        assertNotNull(allTypesObject)

        // Assign id if we have one
        if (id != null) {
            allTypesObject!!.columnString = id
        }
    }
    val allTypesObjectFromRealm = if (id == null) {
        realm.where<AllTypes>().equalTo(AllTypes.FIELD_STRING, "").findFirst()
    } else {
        realm.where<AllTypes>().equalTo(AllTypes.FIELD_STRING, id).findFirst()
    }
    assertEquals(allTypesObject, allTypesObjectFromRealm)
    return allTypesObject!!
}
