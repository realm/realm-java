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
import io.realm.entities.DogPrimaryKey
import io.realm.internal.android.TypeUtils
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import org.bson.types.Decimal128
import org.bson.types.ObjectId
import java.util.*
import kotlin.reflect.KFunction1
import kotlin.reflect.KFunction2
import kotlin.test.*

/**
 * Generic tester for all types of managed dictionaries.
 *
 * It uses `KFunction1` and `KFunction2` to streamline the access to [RealmDictionary] fields in
 * [AllTypes]. This way we only need one tester for all supported types.
 */
class ManagedDictionaryTester<T : Any>(
        private val testerClass: String,
        private val dictionaryGetter: KFunction1<AllTypes, RealmDictionary<T>>,
        private val dictionarySetter: KFunction2<AllTypes, RealmDictionary<T>, Unit>,
        private val initializedDictionary: RealmDictionary<T>,
        private val alternativeDictionary: RealmDictionary<T>
) : DictionaryTester {

    private lateinit var config: RealmConfiguration
    private lateinit var realm: Realm

    override fun toString(): String = "Managed$testerClass"

    override fun setUp(config: RealmConfiguration) {
        this.config = config
        this.realm = Realm.getInstance(config)
    }

    override fun tearDown() {
        realm.close()
    }

    override fun constructorWithAnotherMap() = Unit     // Not applicable in managed mode
    override fun isManaged() = assertManagedIsManaged(realm, dictionaryGetter)
    override fun isValid() = assertManagedIsValid(realm, dictionaryGetter)
    override fun isFrozen() = assertManagedIsFrozen(realm, dictionaryGetter)
    override fun size() = assertManagedSize(realm, dictionaryGetter, initializedDictionary)
    override fun isEmpty() = assertManagedIsEmpty(realm, dictionaryGetter, initializedDictionary)
    override fun containsKey() = assertManagedContainsKey(realm, dictionaryGetter, initializedDictionary)
    override fun containsValue() = assertManagedContainsValue(realm, dictionaryGetter, initializedDictionary)
    override fun get() = assertManagedGet(realm, dictionaryGetter, initializedDictionary)
    override fun put() = assertManagedPut(realm, dictionaryGetter, initializedDictionary, alternativeDictionary)
    override fun remove() = assertManagedRemove(realm, dictionaryGetter, initializedDictionary)
    override fun putAll() = assertManagedPutAll(realm, dictionaryGetter, initializedDictionary)
    override fun clear() = assertManagedClear(realm, dictionaryGetter, initializedDictionary)
    override fun keySet() = assertManagedKeySet(realm, dictionaryGetter, initializedDictionary)
    override fun values() = assertManagedValues(realm, dictionaryGetter, initializedDictionary)
    override fun entrySet() = assertManagedEntrySet(realm, dictionaryGetter, initializedDictionary, alternativeDictionary)
    override fun freeze() = Unit    // This has already been tested in "isFrozen"
    override fun copyToRealm() = assertCopyToRealm(realm, dictionaryGetter, dictionarySetter, initializedDictionary)
    override fun copyFromRealm() = assertCopyFromRealm(realm, dictionaryGetter, initializedDictionary)
}

/**
 * Creates testers for all [DictionarySupportedType]s and initializes them for testing. There are as
 * many Mixed testers as [MixedType]s.
 *
 * The `KFunction1` and `KFunction2` parameters for `dictionaryGetter` and `dictionarySetter`
 * respectively enables agnostic field processing, making it possible to cover all supported types
 * with just one tester class.
 */
fun managedFactory(): List<DictionaryTester> {
    val primitiveTesters = listOf<DictionaryTester>(
            ManagedDictionaryTester(
                    testerClass = "Long",
                    dictionaryGetter = AllTypes::getColumnLongDictionary,
                    dictionarySetter = AllTypes::setColumnLongDictionary,
                    initializedDictionary = RealmDictionary<Long>().init(listOf(KEY_HELLO to VALUE_NUMERIC_HELLO.toLong(), KEY_BYE to VALUE_NUMERIC_BYE.toLong(), KEY_NULL to null)),
                    alternativeDictionary = RealmDictionary<Long>().init(listOf(KEY_HELLO to VALUE_NUMERIC_BYE.toLong(), KEY_BYE to VALUE_NUMERIC_HELLO.toLong(), KEY_NULL to null))
            ),
            ManagedDictionaryTester(
                    testerClass = "Integer",
                    dictionaryGetter = AllTypes::getColumnIntegerDictionary,
                    dictionarySetter = AllTypes::setColumnIntegerDictionary,
                    initializedDictionary = RealmDictionary<Int>().init(listOf(KEY_HELLO to VALUE_NUMERIC_HELLO, KEY_BYE to VALUE_NUMERIC_BYE, KEY_NULL to null)),
                    alternativeDictionary = RealmDictionary<Int>().init(listOf(KEY_HELLO to VALUE_NUMERIC_BYE, KEY_BYE to VALUE_NUMERIC_HELLO, KEY_NULL to null))
            ),
            ManagedDictionaryTester(
                    testerClass = "Short",
                    dictionaryGetter = AllTypes::getColumnShortDictionary,
                    dictionarySetter = AllTypes::setColumnShortDictionary,
                    initializedDictionary = RealmDictionary<Short>().init(listOf(KEY_HELLO to VALUE_NUMERIC_HELLO.toShort(), KEY_BYE to VALUE_NUMERIC_BYE.toShort(), KEY_NULL to null)),
                    alternativeDictionary = RealmDictionary<Short>().init(listOf(KEY_HELLO to VALUE_NUMERIC_BYE.toShort(), KEY_BYE to VALUE_NUMERIC_HELLO.toShort(), KEY_NULL to null))
            ),
            ManagedDictionaryTester(
                    testerClass = "Byte",
                    dictionaryGetter = AllTypes::getColumnByteDictionary,
                    dictionarySetter = AllTypes::setColumnByteDictionary,
                    initializedDictionary = RealmDictionary<Byte>().init(listOf(KEY_HELLO to VALUE_NUMERIC_HELLO.toByte(), KEY_BYE to VALUE_NUMERIC_BYE.toByte(), KEY_NULL to null)),
                    alternativeDictionary = RealmDictionary<Byte>().init(listOf(KEY_HELLO to VALUE_NUMERIC_BYE.toByte(), KEY_BYE to VALUE_NUMERIC_HELLO.toByte(), KEY_NULL to null))
            ),
            ManagedDictionaryTester(
                    testerClass = "Float",
                    dictionaryGetter = AllTypes::getColumnFloatDictionary,
                    dictionarySetter = AllTypes::setColumnFloatDictionary,
                    initializedDictionary = RealmDictionary<Float>().init(listOf(KEY_HELLO to VALUE_NUMERIC_HELLO.toFloat(), KEY_BYE to VALUE_NUMERIC_BYE.toFloat(), KEY_NULL to null)),
                    alternativeDictionary = RealmDictionary<Float>().init(listOf(KEY_HELLO to VALUE_NUMERIC_BYE.toFloat(), KEY_BYE to VALUE_NUMERIC_HELLO.toFloat(), KEY_NULL to null))
            ),
            ManagedDictionaryTester(
                    testerClass = "Double",
                    dictionaryGetter = AllTypes::getColumnDoubleDictionary,
                    dictionarySetter = AllTypes::setColumnDoubleDictionary,
                    initializedDictionary = RealmDictionary<Double>().init(listOf(KEY_HELLO to VALUE_NUMERIC_HELLO.toDouble(), KEY_BYE to VALUE_NUMERIC_BYE.toDouble(), KEY_NULL to null)),
                    alternativeDictionary = RealmDictionary<Double>().init(listOf(KEY_HELLO to VALUE_NUMERIC_BYE.toDouble(), KEY_BYE to VALUE_NUMERIC_HELLO.toDouble(), KEY_NULL to null))
            ),
            ManagedDictionaryTester(
                    testerClass = "String",
                    dictionaryGetter = AllTypes::getColumnStringDictionary,
                    dictionarySetter = AllTypes::setColumnStringDictionary,
                    initializedDictionary = RealmDictionary<String>().init(listOf(KEY_HELLO to VALUE_STRING_HELLO, KEY_BYE to VALUE_STRING_BYE, KEY_NULL to null)),
                    alternativeDictionary = RealmDictionary<String>().init(listOf(KEY_HELLO to VALUE_STRING_BYE, KEY_BYE to VALUE_STRING_HELLO, KEY_NULL to null))
            ),
            ManagedDictionaryTester(
                    testerClass = "Boolean",
                    dictionaryGetter = AllTypes::getColumnBooleanDictionary,
                    dictionarySetter = AllTypes::setColumnBooleanDictionary,
                    initializedDictionary = RealmDictionary<Boolean>().init(listOf(KEY_HELLO to VALUE_BOOLEAN_HELLO, KEY_NULL to null)),
                    alternativeDictionary = RealmDictionary<Boolean>().init(listOf(KEY_HELLO to VALUE_BOOLEAN_NOT_PRESENT, KEY_NULL to null))
            ),
            ManagedDictionaryTester(
                    testerClass = "Date",
                    dictionaryGetter = AllTypes::getColumnDateDictionary,
                    dictionarySetter = AllTypes::setColumnDateDictionary,
                    initializedDictionary = RealmDictionary<Date>().init(listOf(KEY_HELLO to VALUE_DATE_HELLO, KEY_BYE to VALUE_DATE_BYE, KEY_NULL to null)),
                    alternativeDictionary = RealmDictionary<Date>().init(listOf(KEY_HELLO to VALUE_DATE_BYE, KEY_BYE to VALUE_DATE_HELLO, KEY_NULL to null))
            ),
            ManagedDictionaryTester(
                    testerClass = "Decimal128",
                    dictionaryGetter = AllTypes::getColumnDecimal128Dictionary,
                    dictionarySetter = AllTypes::setColumnDecimal128Dictionary,
                    initializedDictionary = RealmDictionary<Decimal128>().init(listOf(KEY_HELLO to VALUE_DECIMAL128_HELLO, KEY_BYE to VALUE_DECIMAL128_BYE, KEY_NULL to null)),
                    alternativeDictionary = RealmDictionary<Decimal128>().init(listOf(KEY_HELLO to VALUE_DECIMAL128_BYE, KEY_BYE to VALUE_DECIMAL128_HELLO, KEY_NULL to null))
            ),
            ManagedDictionaryTester(
                    testerClass = "BoxedBinary",
                    dictionaryGetter = AllTypes::getColumnBoxedBinaryDictionary,
                    dictionarySetter = AllTypes::setColumnBoxedBinaryDictionary,
                    initializedDictionary = RealmDictionary<Array<Byte>>().init(listOf(KEY_HELLO to VALUE_BOXED_BINARY_HELLO, KEY_BYE to VALUE_BOXED_BINARY_BYE, KEY_NULL to null)),
                    alternativeDictionary = RealmDictionary<Array<Byte>>().init(listOf(KEY_HELLO to VALUE_BOXED_BINARY_BYE, KEY_BYE to VALUE_BOXED_BINARY_HELLO, KEY_NULL to null))
            ),
            ManagedDictionaryTester(
                    testerClass = "Binary",
                    dictionaryGetter = AllTypes::getColumnBinaryDictionary,
                    dictionarySetter = AllTypes::setColumnBinaryDictionary,
                    initializedDictionary = RealmDictionary<ByteArray>().init(listOf(KEY_HELLO to VALUE_BINARY_HELLO, KEY_BYE to VALUE_BINARY_BYE, KEY_NULL to null)),
                    alternativeDictionary = RealmDictionary<ByteArray>().init(listOf(KEY_HELLO to VALUE_BINARY_BYE, KEY_BYE to VALUE_BINARY_HELLO, KEY_NULL to null))
            ),
            ManagedDictionaryTester(
                    testerClass = "ObjectId",
                    dictionaryGetter = AllTypes::getColumnObjectIdDictionary,
                    dictionarySetter = AllTypes::setColumnObjectIdDictionary,
                    initializedDictionary = RealmDictionary<ObjectId>().init(listOf(KEY_HELLO to VALUE_OBJECT_ID_HELLO, KEY_BYE to VALUE_OBJECT_ID_BYE, KEY_NULL to null)),
                    alternativeDictionary = RealmDictionary<ObjectId>().init(listOf(KEY_HELLO to VALUE_OBJECT_ID_BYE, KEY_BYE to VALUE_OBJECT_ID_HELLO, KEY_NULL to null))
            ),
            ManagedDictionaryTester(
                    testerClass = "UUID",
                    dictionaryGetter = AllTypes::getColumnUUIDDictionary,
                    dictionarySetter = AllTypes::setColumnUUIDDictionary,
                    initializedDictionary = RealmDictionary<UUID>().init(listOf(KEY_HELLO to VALUE_UUID_HELLO, KEY_BYE to VALUE_UUID_BYE, KEY_NULL to null)),
                    alternativeDictionary = RealmDictionary<UUID>().init(listOf(KEY_HELLO to VALUE_UUID_BYE, KEY_BYE to VALUE_UUID_HELLO, KEY_NULL to null))
            ),
            ManagedDictionaryTester(
                    testerClass = "Link",
                    dictionaryGetter = AllTypes::getColumnRealmDictionary,
                    dictionarySetter = AllTypes::setColumnRealmDictionary,
                    initializedDictionary = RealmDictionary<DogPrimaryKey>().init(listOf(KEY_HELLO to VALUE_LINK_HELLO, KEY_BYE to VALUE_LINK_BYE, KEY_NULL to null)),
                    alternativeDictionary = RealmDictionary<DogPrimaryKey>().init(listOf(KEY_HELLO to VALUE_LINK_BYE, KEY_BYE to VALUE_LINK_HELLO, KEY_NULL to null))
            )
    )

    // Create Mixed testers now
    val mixedTesters = MixedType.values().map { mixedType ->
        ManagedDictionaryTester(
                testerClass = "Mixed-${mixedType.name}",
                dictionaryGetter = AllTypes::getColumnMixedDictionary,
                dictionarySetter = AllTypes::setColumnMixedDictionary,
                initializedDictionary = RealmDictionary<Mixed>().init(getMixedKeyValuePairs(mixedType)),
                alternativeDictionary = RealmDictionary<Mixed>().init(getMixedKeyValuePairs(mixedType, true))
        )
    }

    return primitiveTesters.plus(mixedTesters)
}

//--------------------------------------------------------------------------------------------------
// Managed helpers
//--------------------------------------------------------------------------------------------------

private fun createAllTypesManagedContainerAndAssert(realm: Realm): AllTypes {
    realm.executeTransaction { transactionRealm ->
        transactionRealm.createObject<AllTypes>()
    }
    val allTypesObject = realm.where<AllTypes>().findFirst()
    assertNotNull(allTypesObject)
    return allTypesObject
}

private fun assertManagedIsManaged(
        realm: Realm,
        dictionaryGetter: KFunction1<AllTypes, RealmDictionary<*>>
) {
    val allTypesObject = createAllTypesManagedContainerAndAssert(realm)
    assertNotNull(allTypesObject)
    val dictionary = dictionaryGetter.call(allTypesObject)
    assertTrue(dictionary.isManaged)
}

private fun assertManagedIsValid(
        realm: Realm,
        dictionaryGetter: KFunction1<AllTypes, RealmDictionary<*>>
) {
    val allTypesObject = createAllTypesManagedContainerAndAssert(realm)
    assertNotNull(allTypesObject)
    val dictionary = dictionaryGetter.call(allTypesObject)
    assertTrue(dictionary.isValid)
}

private fun assertManagedIsFrozen(
        realm: Realm,
        dictionaryGetter: KFunction1<AllTypes, RealmDictionary<*>>
) {
    val allTypesObject = createAllTypesManagedContainerAndAssert(realm)
    assertNotNull(allTypesObject)
    val dictionary = dictionaryGetter.call(allTypesObject)
    assertFalse(dictionary.isFrozen)
    val frozenDictionary = dictionary.freeze()
    assertTrue(frozenDictionary.isFrozen)
}

private fun <E : Any> assertManagedSize(
        realm: Realm,
        dictionaryGetter: KFunction1<AllTypes, RealmDictionary<E>>,
        initializedDictionary: RealmDictionary<E>
) {
    val allTypesObject = createAllTypesManagedContainerAndAssert(realm)
    assertNotNull(allTypesObject)
    val dictionary = dictionaryGetter.call(allTypesObject)
    assertEquals(0, dictionary.size)
    realm.executeTransaction {
        initializedDictionary.forEach { key, value ->
            dictionary[key] = value
        }
    }
    assertEquals(initializedDictionary.size, dictionary.size)
}

private fun <E : Any> assertManagedIsEmpty(
        realm: Realm,
        dictionaryGetter: KFunction1<AllTypes, RealmDictionary<E>>,
        initializedDictionary: RealmDictionary<E>
) {
    val allTypesObject = createAllTypesManagedContainerAndAssert(realm)
    assertNotNull(allTypesObject)
    val dictionary = dictionaryGetter.call(allTypesObject)
    assertTrue(dictionary.isEmpty())
    realm.executeTransaction {
        initializedDictionary.forEach { key, value ->
            dictionary[key] = value
        }
    }
    assertFalse(dictionary.isEmpty())
}

private fun <E : Any> assertManagedContainsKey(
        realm: Realm,
        dictionaryGetter: KFunction1<AllTypes, RealmDictionary<E>>,
        initializedDictionary: RealmDictionary<E>
) {
    val allTypesObject = createAllTypesManagedContainerAndAssert(realm)
    assertNotNull(allTypesObject)
    val dictionary = dictionaryGetter.call(allTypesObject)

    initializedDictionary.forEach { key, _ ->
        assertFalse(dictionary.containsKey(key))
    }

    realm.executeTransaction {
        initializedDictionary.forEach { key, value ->
            dictionary[key] = value
        }
    }

    initializedDictionary.forEach { key, _ ->
        assertTrue(dictionary.containsKey(key))
    }
}

private fun <E : Any> assertManagedContainsValue(
        realm: Realm,
        dictionaryGetter: KFunction1<AllTypes, RealmDictionary<E>>,
        initializedDictionary: RealmDictionary<E>
) {
    val allTypesObject = createAllTypesManagedContainerAndAssert(realm)
    assertNotNull(allTypesObject)
    val dictionary = dictionaryGetter.call(allTypesObject)

    initializedDictionary.values.forEachIndexed { index, value ->
        if (value is DogPrimaryKey) {
            // Given that only managed models can be contained in a model dictionary, we need to
            // test containsValue with a dummy model
            realm.executeTransaction { transactionRealm ->
                val dummyRealmModel = transactionRealm.copyToRealm(DogPrimaryKey(666 + index.toLong(), "DUMMY"))
                assertFalse(dictionary.containsValue(dummyRealmModel as E))
            }
        } else if (value is Mixed) {
            if (value.valueClass == DogPrimaryKey::class.java) {
                realm.executeTransaction { transactionRealm ->
                    val dummyRealmModel = transactionRealm.copyToRealm(DogPrimaryKey(666 + index.toLong(), "DUMMY"))
                    val mixedWithManagedModel = Mixed.valueOf(dummyRealmModel)
                    assertFalse(dictionary.containsValue(mixedWithManagedModel as E))
                }
            } else {
                assertFalse(dictionary.containsValue(value))
            }
        } else {
            assertFalse(dictionary.containsValue(value))
        }
    }

    realm.executeTransaction {
        initializedDictionary.forEach { key, value ->
            dictionary[key] = value
        }
    }

    dictionary.forEach { key, value ->
        assertContainsValue(realm, key, value, initializedDictionary, dictionary)
    }
}

private fun <E : Any> assertManagedGet(
        realm: Realm,
        dictionaryGetter: KFunction1<AllTypes, RealmDictionary<E>>,
        initializedDictionary: RealmDictionary<E>
) {
    val allTypesObject = createAllTypesManagedContainerAndAssert(realm)
    assertNotNull(allTypesObject)
    val dictionary = dictionaryGetter.call(allTypesObject)
    initializedDictionary.forEach { key, _ ->
        assertNull(dictionary[key])
    }
    realm.executeTransaction {
        initializedDictionary.forEach { key, value ->
            dictionary[key] = value
        }
    }
    initializedDictionary.forEach { key, value ->
        assertEqualsHelper(realm, value, dictionary[key])
    }
}

private fun <E : Any> assertManagedPut(
        realm: Realm,
        dictionaryGetter: KFunction1<AllTypes, RealmDictionary<E>>,
        initializedDictionary: RealmDictionary<E>,
        alternativeDictionary: RealmDictionary<E>
) {
    val allTypesObject = createAllTypesManagedContainerAndAssert(realm)
    assertNotNull(allTypesObject)
    val dictionary = dictionaryGetter.call(allTypesObject)

    realm.executeTransaction {
        // Check we get null since previous values are not present
        initializedDictionary.forEach { key, value ->
            assertNull(dictionary.put(key, value))
        }
    }
    initializedDictionary.forEach { key, value ->
        assertEqualsHelper(realm, value, dictionary[key])
    }

    // Now check we get the previous value after insertion
    realm.executeTransaction {
        alternativeDictionary.forEach { key, value ->
            assertEqualsHelper(realm, initializedDictionary[key], dictionary.put(key, value))
        }
    }

    // Finally check that the alternative values are there
    dictionary.forEach { key, value ->
        assertTrue(alternativeDictionary.containsKey(key))
        assertContainsValue(realm, key, value, alternativeDictionary, dictionary)
    }
}

private fun <E : Any> assertManagedRemove(
        realm: Realm,
        dictionaryGetter: KFunction1<AllTypes, RealmDictionary<E>>,
        initializedDictionary: RealmDictionary<E>
) {
    val allTypesObject = createAllTypesManagedContainerAndAssert(realm)
    assertNotNull(allTypesObject)
    val dictionary = dictionaryGetter.call(allTypesObject)
    realm.executeTransaction {
        initializedDictionary.forEach { key, value ->
            dictionary[key] = value
        }
    }

    // Remove, assert value and check size
    realm.executeTransaction {
        initializedDictionary.map {
            Pair(it.key, it.value)
        }.also { pairs ->
            for (index in pairs.size - 1 downTo 0) {
                val key = pairs[index].first
                val value = pairs[index].second
                assertEqualsHelper(realm, value, dictionary.remove(key))
                assertEquals(index, dictionary.size)
            }
        }
    }
}

private fun <E : Any> assertManagedPutAll(
        realm: Realm,
        dictionaryGetter: KFunction1<AllTypes, RealmDictionary<E>>,
        initializedDictionary: RealmDictionary<E>
) {
    val allTypesObject = createAllTypesManagedContainerAndAssert(realm)
    assertNotNull(allTypesObject)
    val dictionary = dictionaryGetter.call(allTypesObject)
    assertTrue(dictionary.isEmpty())

    realm.executeTransaction {
        dictionary.putAll(initializedDictionary)
    }

    // Check initialized dictionary got inserted
    initializedDictionary.forEach { key, value ->
        assertEqualsHelper(realm, value, dictionary[key])
    }
}

private fun <E : Any> assertManagedClear(
        realm: Realm,
        dictionaryGetter: KFunction1<AllTypes, RealmDictionary<E>>,
        initializedDictionary: RealmDictionary<E>
) {
    val allTypesObject = createAllTypesManagedContainerAndAssert(realm)
    assertNotNull(allTypesObject)
    val dictionary = dictionaryGetter.call(allTypesObject)

    // Insert entries, assert not empty, clear and assert empty
    realm.executeTransaction {
        dictionary.putAll(initializedDictionary)
        assertFalse(dictionary.isEmpty())
        dictionary.clear()
        assertTrue(dictionary.isEmpty())
    }
}

private fun <E : Any> assertManagedKeySet(
        realm: Realm,
        dictionaryGetter: KFunction1<AllTypes, RealmDictionary<E>>,
        initializedDictionary: RealmDictionary<E>
) {
    val allTypesObject = createAllTypesManagedContainerAndAssert(realm)
    assertNotNull(allTypesObject)
    val dictionary = dictionaryGetter.call(allTypesObject)

    realm.executeTransaction {
        dictionary.putAll(initializedDictionary)
    }

    val keySet = dictionary.keys
    initializedDictionary.forEach { key, _ ->
        assertTrue(keySet.contains(key))
    }
}

private fun <E : Any> assertManagedValues(
        realm: Realm,
        dictionaryGetter: KFunction1<AllTypes, RealmDictionary<E>>,
        initializedDictionary: RealmDictionary<E>
) {
    val allTypesObject = createAllTypesManagedContainerAndAssert(realm)
    assertNotNull(allTypesObject)
    val dictionary = dictionaryGetter.call(allTypesObject)

    realm.executeTransaction {
        dictionary.putAll(initializedDictionary)
    }

    val values = dictionary.values
    values.forEach { value ->
        when (value) {
            is Array<*> -> {
                val dictionaryBytes = TypeUtils.convertNonPrimitiveBinaryToPrimitive(value as Array<Byte>)
                assertTrue((values as Collection<ByteArray>).contains(dictionaryBytes))
            }
            is DogPrimaryKey -> {
                // null entries become "invalid object" when calling dictionary.values()
                if (value.isValid) {
                    assertTrue(dictionary.containsValue(value))
                } else {
                    assertTrue(dictionary.containsValue(null))
                }
            }
            else -> assertTrue(dictionary.containsValue(value))
        }
    }
}

private fun <E : Any> assertManagedEntrySet(
        realm: Realm,
        dictionaryGetter: KFunction1<AllTypes, RealmDictionary<E>>,
        initializedDictionary: RealmDictionary<E>,
        alternativeDictionary: RealmDictionary<E>
) {
    val allTypesObject = createAllTypesManagedContainerAndAssert(realm)
    assertNotNull(allTypesObject)
    val dictionary = dictionaryGetter.call(allTypesObject)

    realm.executeTransaction {
        dictionary.putAll(initializedDictionary)
        val entrySet = dictionary.entries
        assertNotNull(entrySet)

        // Test size
        assertEquals(initializedDictionary.size, entrySet.size)

        // Test contains
        entrySet.forEach { entry ->
            assertTrue(initializedDictionary.containsKey(entry.key))

            if (entry.value is DogPrimaryKey) {
                val dog = dictionary[entry.key]
                assertEquals(dog, entry.value)
            } else {
                assertTrue(dictionary.containsValue(entry.value))
            }
        }

        // Test iterator
        val iterator = entrySet.iterator()
        assertNotNull(iterator)
        var iteratorSize = 0
        while (iterator.hasNext()) {
            iteratorSize++
            iterator.next()
        }
        assertEquals(entrySet.size, iteratorSize)

        // Test toArray
        assertTrue(entrySet is RealmMapEntrySet<String, E?>)
        val entrySetObjectArray = entrySet.toArray()
        for (entry in entrySetObjectArray) {
            assertTrue(entry is Map.Entry<*, *>)
            assertTrue(entrySet.contains(entry))
        }

        fun testToArray(
                entrySetArray: Array<Map.Entry<String, E?>?>,
                biggerSize: Boolean = false
        ) {
            when {
                biggerSize -> assertTrue(entrySetArray.size > entrySet.size)
                else -> {
                    assertEquals(entrySet.size, entrySetArray.size)
                }
            }
            for ((index, entry) in entrySetArray.withIndex()) {
                if (index >= entrySet.size) {
                    assertNull(entry)
                } else {
                    assertTrue(entry is Map.Entry<*, *>)
                    assertTrue(entrySet.contains(entry))
                }
            }
        }

        // Test toArray: smaller size, return a new instance
        val testArraySmallerSize = arrayOfNulls<Map.Entry<String, E?>>(1)
        val entrySetSmallerSizeArray = entrySet.toArray(testArraySmallerSize)
        testToArray(entrySetSmallerSizeArray)

        // Test toArray: same size, return a new instance
        val testArraySameSize = arrayOfNulls<Map.Entry<String, E?>>(entrySet.size)
        val entrySetSameSizeArray = entrySet.toArray(testArraySameSize)
        testToArray(entrySetSameSizeArray)

        // Test toArray: bigger size, add null as the last entry
        val testArrayBiggerSize = arrayOfNulls<Map.Entry<String, E?>>(10)
        val entrySetBiggerSizeArray = entrySet.toArray(testArrayBiggerSize)
        testToArray(entrySetBiggerSizeArray, true)

        // Test containsAll
        when (entrySet.first().value) {
            is DogPrimaryKey -> {
                val sameRealmModelCollection = initializedDictionary.map { originalEntry ->
                    AbstractMap.SimpleImmutableEntry(originalEntry.key, dictionary[originalEntry.key])
                }
                assertTrue(entrySet.containsAll(sameRealmModelCollection))
            }
            is Mixed -> {
                val sameCollection =
                        dictionary.map { entry: Map.Entry<String, E?> ->
                            // Realm doesn't deliver "pure null" values for Mixed, but rather Mixed.nullValue()
                            when (entry.value) {
                                null -> AbstractMap.SimpleImmutableEntry(entry.key, Mixed.nullValue())
                                else -> AbstractMap.SimpleImmutableEntry(entry.key, entry.value)
                            }
                        }.toSet() as Set<Map.Entry<String, E>>
                assertTrue(entrySet.containsAll(sameCollection))
            }
            else -> {
                val sameCollection =
                        initializedDictionary.map { entry ->
                            AbstractMap.SimpleImmutableEntry(entry.key, entry.value)
                        }.toSet()
                assertTrue(entrySet.containsAll(sameCollection))
            }
        }

        val differentCollection = alternativeDictionary.map { entry ->
            AbstractMap.SimpleImmutableEntry(entry.key, entry.value)
        }.toSet()

        assertFalse(entrySet.containsAll(differentCollection))
    }
}

private fun <E : Any> assertCopyToRealm(
        realm: Realm,
        dictionaryGetter: KFunction1<AllTypes, RealmDictionary<E>>,
        dictionarySetter: KFunction2<AllTypes, RealmDictionary<E>, Unit>,
        initializedDictionary: RealmDictionary<E>
) {
    // Instantiate container and set dictionary on container
    val manualInstance = AllTypes().apply {
        dictionarySetter.call(this, initializedDictionary)
    }

    // Copy to Realm
    realm.executeTransaction {
        val allTypesObject = realm.copyToRealm(manualInstance)
        assertNotNull(allTypesObject)
    }

    // Get dictionary from container from Realm
    val allTypesObject = realm.where<AllTypes>().findFirst()
    assertNotNull(allTypesObject)
    val dictionary = dictionaryGetter.call(allTypesObject)
    assertFalse(dictionary.isEmpty())
    initializedDictionary.forEach { key, value ->
        assertEqualsHelper(realm, value, dictionary[key])
    }
}

private fun <E : Any> assertCopyFromRealm(
        realm: Realm,
        dictionaryGetter: KFunction1<AllTypes, RealmDictionary<E>>,
        initializedDictionary: RealmDictionary<E>
) {
    val allTypesObject = createAllTypesManagedContainerAndAssert(realm)
    assertNotNull(allTypesObject)
    val dictionary = dictionaryGetter.call(allTypesObject)

    realm.executeTransaction {
        dictionary.putAll(initializedDictionary)
    }

    val detachedAllTypes = realm.copyFromRealm(allTypesObject)
    val detachedDictionary = dictionaryGetter.call(detachedAllTypes)
    assertEquals(dictionary.size, detachedDictionary.size)

    // Compare elements to the original values
    detachedDictionary.forEach { key, value ->
        assertEqualsHelper(realm, value, dictionary[key])
    }
}

private fun <E : Any> RealmDictionary<E>.init(
        keyValuePairs: List<Pair<String, E?>>
): RealmDictionary<E> {
    return this.apply {
        for (keyValuePair in keyValuePairs) {
            put(keyValuePair.first, keyValuePair.second)
        }
    }
}

private fun <E> assertEqualsHelper(realm: Realm, value: E?, valueFromRealm: E?) {
    when (valueFromRealm) {
        is Array<*> -> {
            val bytes = TypeUtils.convertNonPrimitiveBinaryToPrimitive(value as Array<Byte>)
            val otherBytes = TypeUtils.convertNonPrimitiveBinaryToPrimitive(valueFromRealm as Array<Byte>)
            Arrays.equals(bytes, otherBytes)
        }
        is ByteArray -> (value as ByteArray).contentEquals(valueFromRealm as ByteArray)
        is DogPrimaryKey -> assertEquals((value as DogPrimaryKey).name, valueFromRealm.name)
        is Mixed -> when {
            // If null, check we have "Mixed.nullValue()"
            value == null -> assertTrue(valueFromRealm.isNull)
            (value as Mixed).valueClass == DogPrimaryKey::class.java -> {
                // If RealmModel, check provided Mixed equals a Mixed containing the managed model
                val managedRealmModel = realm.where<DogPrimaryKey>()
                        .equalTo("name", (value as Mixed).asRealmModel(DogPrimaryKey::class.java).name)
                        .findFirst()
                val mixedWithManagedModel = Mixed.valueOf(managedRealmModel)
                assertEquals(valueFromRealm, mixedWithManagedModel)
            }
            else -> assertEquals(value as E, valueFromRealm)
        }
        else -> assertEquals(value, valueFromRealm)
    }
}

private fun <E : Any> assertContainsValue(
        realm: Realm,
        key: String,
        value: E?,
        unmanagedDictionary: RealmDictionary<E>,
        managedDictionary: RealmDictionary<E>
) {
    when (value) {
        is DogPrimaryKey -> {
            // Use managed model for containsValue: managed dictionaries can only contain managed models
            val managedRealmModel = realm.where<DogPrimaryKey>()
                    .equalTo("name", value.name)
                    .findFirst()
            assertTrue(managedDictionary.containsValue(managedRealmModel as E))
        }
        is Mixed -> when {
            // If null, check we have "Mixed.nullValue()"
            value.isNull -> assertTrue(managedDictionary.containsValue(Mixed.nullValue() as E))
            value.valueClass == DogPrimaryKey::class.java -> {
                // If RealmModel, check dictionary contains a Mixed containing the managed model
                val managedRealmDog = realm.where<DogPrimaryKey>()
                        .equalTo("name", value.asRealmModel(DogPrimaryKey::class.java).name)
                        .findFirst()
                val mixedWithManagedDog = Mixed.valueOf(managedRealmDog)
                assertTrue(managedDictionary.containsValue(mixedWithManagedDog as E))
            }
            else -> assertTrue(managedDictionary.containsValue(unmanagedDictionary[key]))
        }
        else -> assertTrue(managedDictionary.containsValue(unmanagedDictionary[key]))
    }
}
