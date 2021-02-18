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

import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.Iterator
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.contains
import kotlin.collections.containsAll
import kotlin.collections.forEach
import kotlin.collections.indices
import kotlin.collections.map
import kotlin.collections.setOf
import kotlin.collections.toSet
import kotlin.collections.withIndex
import kotlin.test.*

class ManagedLong(
        private val testKeys: List<String>,
        private val testValues: List<Long?>
) : DictionaryTester {

    lateinit var realm: Realm

    override fun toString(): String = this.javaClass.simpleName

    override fun addRealmInstance(realm: Realm) {
        this.realm = realm
    }

    override fun isTesterManaged(): Boolean = true
    override fun constructorWithAnotherMap() = fail("Not available in managed mode.")

    override fun isManaged() {
        assertManagedIsManaged(createAllTypesManagedContainerAndAssert(realm).columnLongDictionary)
    }

    override fun isValid() {
        assertManagedIsValid(realm, createAllTypesManagedContainerAndAssert(realm).columnLongDictionary)
    }

    override fun isFrozen() {
        assertManagedIsFrozen(createAllTypesManagedContainerAndAssert(realm).columnLongDictionary)
    }

    override fun size() {
        assertManagedGenericOperation(
                realm = realm,
                keys = testKeys,
                values = testValues,
                dictionaryGetter = { allTypes -> allTypes.columnLongDictionary },
                preOperationAssertion = { dictionary -> assertEquals(0, dictionary.size) },
                genericOperation = { _, _, _ -> /* no-op */ }
        ) { dictionary -> assertEquals(3, dictionary.size) }
    }

    override fun isEmpty() {
        assertManagedGenericOperation(
                realm = realm,
                keys = testKeys,
                values = testValues,
                dictionaryGetter = { allTypes -> allTypes.columnLongDictionary },
                preOperationAssertion = { dictionary -> assertTrue(dictionary.isEmpty()) },
                genericOperation = { _, _, _ -> /* no-op */ },
                postOperationAssertion = { dictionary -> assertFalse(dictionary.isEmpty()) }
        )
    }

    override fun containsKey() {
        assertManagedGenericOperation(
                realm = realm,
                keys = testKeys,
                values = testValues,
                dictionaryGetter = { allTypes -> allTypes.columnLongDictionary },
                preOperationAssertion = { dictionary ->
                    testKeys.forEach { key ->
                        assertFalse(dictionary.containsKey(key))
                    }
                },
                genericOperation = { _, _, _ -> /* no-op */ },
                postOperationAssertion = { dictionary ->
                    testKeys.forEach { key ->
                        assertTrue(dictionary.containsKey(key))
                    }
                }
        )
    }

    override fun containsValue() {
        assertManagedGenericOperation(
                realm = realm,
                keys = testKeys,
                values = testValues,
                dictionaryGetter = { allTypes -> allTypes.columnLongDictionary },
                preOperationAssertion = { dictionary ->
                    // TODO: https://github.com/realm/realm-core/issues/4438
//                    testValues.forEach { value ->
//                        assertFalse(dictionary.containsValue(value))
//                    }
//                    assertFalse(dictionary.containsValue(null))
                },
                genericOperation = { _, _, _ -> /* no-op */ },
                postOperationAssertion = { dictionary ->
                    testValues.forEach { value ->
                        assertTrue(dictionary.containsValue(value))
                    }
                    assertTrue(dictionary.containsValue(null))
                }
        )
    }

    override fun get() {
        assertManagedGenericOperation(
                realm = realm,
                keys = testKeys,
                values = testValues,
                dictionaryGetter = { allTypes -> allTypes.columnLongDictionary },
                preOperationAssertion = { dictionary -> assertTrue(dictionary.isEmpty()) },
                genericOperation = { _, _, _ -> /* no-op */ },
                postOperationAssertion = { dictionary ->
                    for (i in testKeys.indices) {
                        val key = testKeys[i]
                        val expectedValue = testValues[i]
                        assertEquals(expectedValue, dictionary[key])
                    }
                }
        )
    }

    override fun put() {
        // The call to init inside assertManagedGenericOperation uses put, so it's the same as get()
        get()
    }

    override fun remove() {
        assertManagedGenericOperation(
                realm = realm,
                keys = testKeys,
                values = testValues,
                dictionaryGetter = { allTypes -> allTypes.columnLongDictionary },
                preOperationAssertion = { dictionary -> assertTrue(dictionary.isEmpty()) },
                genericOperation = { realm, dictionary, _ ->
                    // Remove index 0
                    realm.executeTransaction {
                        assertEquals(3, dictionary.size)
                        dictionary.remove(testKeys[0]).also { removedValue ->
                            assertEquals(testValues[0], removedValue)
                        }
                    }
                },
                postOperationAssertion = { dictionary ->
                    assertEquals(2, dictionary.size)
                    for (i in 1..2) {
                        val key = testKeys[i]
                        val expectedValue = testValues[i]
                        assertEquals(expectedValue, dictionary[key])
                    }
                }
        )
    }

    override fun putAll() {
        assertManagedGenericOperation(
                realm = realm,
                keys = testKeys,
                values = testValues,
                dictionaryGetter = { allTypes -> allTypes.columnLongDictionary },
                preOperationAssertion = { dictionary -> assertTrue(dictionary.isEmpty()) },
                genericOperation = { realm, dictionary, _ ->
                    realm.executeTransaction {
                        dictionary.putAll(HashMap<String, Long>().apply {
                            put("KEY_ONE", 1)
                            put("KEY_TWO", 2)
                        })
                    }
                },
                postOperationAssertion = { dictionary ->
                    for (i in testKeys.indices) {
                        val key = testKeys[i]
                        val expectedValue = testValues[i]
                        assertEquals(expectedValue, dictionary[key])
                    }
                    assertTrue(dictionary.containsKey("KEY_ONE"))
                    assertTrue(dictionary.containsKey("KEY_TWO"))
                    assertTrue(dictionary.containsValue(1))
                    assertTrue(dictionary.containsValue(2))
                }
        )
    }

    override fun clear() {
        assertManagedGenericOperation(
                realm = realm,
                keys = testKeys,
                values = testValues,
                dictionaryGetter = { allTypes -> allTypes.columnLongDictionary },
                preOperationAssertion = { dictionary -> assertTrue(dictionary.isEmpty()) },
                genericOperation = { realm, dictionary, _ ->
                    realm.executeTransaction {
                        dictionary.clear()
                    }
                },
                postOperationAssertion = { dictionary -> assertTrue(dictionary.isEmpty()) }
        )
    }

    override fun keySet() {
        assertManagedGenericOperation(
                realm = realm,
                keys = testKeys,
                values = testValues,
                dictionaryGetter = { allTypes -> allTypes.columnLongDictionary },
                preOperationAssertion = { dictionary -> assertTrue(dictionary.isEmpty()) },
                genericOperation = { _, dictionary, _ ->
                    val keySet = dictionary.keys
                    assertEquals(testKeys.size, keySet.size)
                    keySet.forEach { key -> assertTrue(testKeys.contains(key)) }
                }
        )
    }

    override fun values() {
        assertManagedGenericOperation(
                realm = realm,
                keys = testKeys,
                values = testValues,
                dictionaryGetter = { allTypes -> allTypes.columnLongDictionary },
                preOperationAssertion = { dictionary -> assertTrue(dictionary.isEmpty()) },
                genericOperation = { _, dictionary, _ ->
                    val values = dictionary.values
                    assertEquals(testValues.size, values.size)
                    values.forEach { value -> testValues.contains(value) }
                }
        )
    }

    override fun entrySet() {
        assertManagedGenericOperation(
                realm = realm,
                keys = testKeys,
                values = testValues,
                dictionaryGetter = { allTypes -> allTypes.columnLongDictionary },
                preOperationAssertion = { dictionary -> assertTrue(dictionary.isEmpty()) },
                genericOperation = { _, dictionary, _ ->
                    val entrySet = dictionary.entries

                    // Test size
                    assertEquals(testValues.size, entrySet.size)

                    // Test isEmpty
                    assertFalse(entrySet.isEmpty())

                    // Test contains
                    val expectedEntrySet = (testKeys.indices).map { i ->
                        AbstractMap.SimpleImmutableEntry(testKeys[i], testValues[i])
                    }.toSet()
                    expectedEntrySet.forEach { entry ->
                        assertTrue(entrySet.contains(entry))
                    }

                    // Test iterator
                    val iterator: Iterator<Map.Entry<String, Long?>> = entrySet.iterator()
                    assertNotNull(iterator)
                    var iteratorSize = 0
                    while (iterator.hasNext()) {
                        iteratorSize++
                        iterator.next()
                    }
                    assertEquals(entrySet.size, iteratorSize)

                    // TODO: try to make these checks below type-agnostic

                    // Test toArray
                    assertTrue(entrySet is RealmMapEntrySet<*, *>)
                    val entrySetObjectArray: Array<Any> = entrySet.toArray()
                    for (entry in entrySetObjectArray) {
                        assertTrue(entry is Map.Entry<*, *>)
                        assertTrue(entry.key is String)
                        assertTrue(entry.value is Long?)
                        assertTrue(entrySet.contains(entry))
                    }

                    // Test toArray: smaller size, return a new instance
                    val testArraySmaller = arrayOfNulls<Map.Entry<String, Long?>>(1)
                    val entrySetSmallerArray: Array<Any> = entrySet.toArray(testArraySmaller)
                    assertNotEquals(testArraySmaller.size, entrySetSmallerArray.size)
                    assertEquals(entrySet.size, entrySetSmallerArray.size)
                    for (entry in entrySetSmallerArray) {
                        assertTrue(entry is Map.Entry<*, *>)
                        assertTrue(entry.key is String)
                        assertTrue(entry.value is Long?)
                        assertTrue(entrySet.contains(entry))
                    }

                    // Test toArray: same size, return a new instance
                    val testArraySame = arrayOfNulls<Map.Entry<String, Long?>>(1)
                    val entrySetSameSizeArray: Array<Any> = entrySet.toArray(testArraySame)
                    assertEquals(entrySet.size, entrySetSameSizeArray.size)
                    for (entry in entrySetSameSizeArray) {
                        assertTrue(entry is Map.Entry<*, *>)
                        assertTrue(entry.key is String)
                        assertTrue(entry.value is Long?)
                        assertTrue(entrySet.contains(entry))
                    }

                    // Test toArray: bigger size, add null as the last entry
                    val testArrayBigger = arrayOfNulls<Map.Entry<String, Long?>>(10)
                    val entrySetBiggerArray: Array<Any> = entrySet.toArray(testArrayBigger)
                    assertTrue(entrySetBiggerArray.size > entrySet.size)
                    for ((index, entry) in entrySetBiggerArray.withIndex()) {
                        if (index >= entrySet.size) {
                            assertNull(entry)
                        } else {
                            assertTrue(entry is Map.Entry<*, *>)
                            assertTrue(entry.key is String)
                            assertTrue(entry.value is Long?)
                            assertTrue(entrySet.contains(entry))
                        }
                    }

                    // Test containsAll
                    assertTrue(entrySet.containsAll(expectedEntrySet))
                    val differentCollection = setOf<Map.Entry<String, Long?>>(
                            AbstractMap.SimpleImmutableEntry("SOME_KEY_1", 1),
                            AbstractMap.SimpleImmutableEntry("SOME_KEY_2", 2),
                            AbstractMap.SimpleImmutableEntry("SOME_KEY_NULL", null)
                    )
                    assertFalse(entrySet.containsAll(differentCollection))
                }
        )
    }

    override fun freeze() {
        assertManagedGenericOperation(
                realm = realm,
                keys = testKeys,
                values = testValues,
                dictionaryGetter = { allTypes -> allTypes.columnLongDictionary },
                preOperationAssertion = { dictionary -> assertFalse(dictionary.isFrozen) },
                genericOperation = { _, dictionary, _ ->
                    val frozenDictionary = dictionary.freeze()
                    assertTrue(frozenDictionary.isFrozen)
                }
        )
    }

    override fun copyToRealm() {
        val allTypesFromRealm = createPrefilledAllTypesManagedContainerAndAssert(realm) { allTypes ->
            allTypes.columnLongDictionary = RealmDictionary<Long>().init(testKeys, testValues)
        }
        assertContains(allTypesFromRealm.columnLongDictionary, testKeys, testValues)
    }

    override fun copyFromRealm() {
        assertManagedGenericOperation(
                realm = realm,
                keys = testKeys,
                values = testValues,
                dictionaryGetter = { allTypes -> allTypes.columnLongDictionary },
                preOperationAssertion = { dictionary -> assertTrue(dictionary.isEmpty()) },
                genericOperation = { realm, _, allTypes ->
                    val allTypesCopy = realm.copyFromRealm(allTypes)
                    assertContains(allTypesCopy.columnLongDictionary, testKeys, testValues)
                }
        )
    }
}
