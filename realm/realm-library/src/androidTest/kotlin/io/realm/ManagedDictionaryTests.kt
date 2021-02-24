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
        private val alternativeDictionary: RealmDictionary<T>,
        private val typeAsserter: TypeAsserter<T> = TypeAsserter()
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

    // Not applicable in managed mode
    override fun constructorWithAnotherMap() = Unit

    override fun isManaged() {
        assertTrue(initAndAssert().isManaged)
    }

    override fun isValid() {
        assertTrue(initAndAssert().isValid)
    }

    override fun isFrozen() {
        val dictionary = initAndAssert()
        assertFalse(dictionary.isFrozen)
        val frozenDictionary = dictionary.freeze()
        assertTrue(frozenDictionary.isFrozen)
    }

    override fun size() {
        val dictionary = initAndAssert()
        assertEquals(0, dictionary.size)
        realm.executeTransaction {
            initializedDictionary.forEach { key, value ->
                dictionary[key] = value
            }
        }
        assertEquals(initializedDictionary.size, dictionary.size)
    }

    override fun isEmpty() {
        val dictionary = initAndAssert()
        assertTrue(dictionary.isEmpty())
        realm.executeTransaction {
            initializedDictionary.forEach { key, value ->
                dictionary[key] = value
            }
        }
        assertFalse(dictionary.isEmpty())
    }

    override fun containsKey() {
        val dictionary = initAndAssert()

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

    override fun containsValue() {
        val dictionary = initAndAssert()

        initializedDictionary.values.forEachIndexed { index, value ->
            typeAsserter.assertContainsValue(realm, dictionary, index, value)
        }

        realm.executeTransaction {
            initializedDictionary.forEach { key, value ->
                dictionary[key] = value
            }
        }

        dictionary.forEach { key, value ->
//            typeAsserter.assertContainsValueHelper(realm, key, value, dictionary, initializedDictionary)
            typeAsserter.assertContainsValueHelper(realm, key, value, initializedDictionary, dictionary)

//            when (value) {
//                is DogPrimaryKey -> {
//                    // Use managed model for containsValue: managed dictionaries can only contain managed models
//                    val managedRealmModel = realm.where<DogPrimaryKey>()
//                            .equalTo("name", value.name)
//                            .findFirst()
//                    assertTrue(dictionary.containsValue(managedRealmModel as T))
//                }
//                is Mixed -> when {
//                    // If null, check we have "Mixed.nullValue()"
//                    value.isNull -> assertTrue(dictionary.containsValue(Mixed.nullValue() as T))
//                    value.valueClass == DogPrimaryKey::class.java -> {
//                        // If RealmModel, check dictionary contains a Mixed containing the managed model
//                        val managedRealmDog = realm.where<DogPrimaryKey>()
//                                .equalTo("name", value.asRealmModel(DogPrimaryKey::class.java).name)
//                                .findFirst()
//                        val mixedWithManagedDog = Mixed.valueOf(managedRealmDog)
//                        assertTrue(dictionary.containsValue(mixedWithManagedDog as T))
//                    }
//                    else -> assertTrue(dictionary.containsValue(initializedDictionary[key]))
//                }
//                else -> assertTrue(dictionary.containsValue(initializedDictionary[key]))
//            }
//            assertContainsValueHelper(realm, key, value, initializedDictionary, dictionary)
        }
    }

    override fun get() {
        val dictionary = initAndAssert()
        initializedDictionary.forEach { key, _ ->
            assertNull(dictionary[key])
        }
        realm.executeTransaction {
            initializedDictionary.forEach { key, value ->
                dictionary[key] = value
            }
        }
        initializedDictionary.forEach { key, value ->
            typeAsserter.assertEqualsHelper(realm, value, dictionary[key])
        }
    }

    override fun put() {
        val dictionary = initAndAssert()

        realm.executeTransaction {
            // Check we get null since previous values are not present
            initializedDictionary.forEach { key, value ->
                assertNull(dictionary.put(key, value))
            }
        }
        initializedDictionary.forEach { key, value ->
            typeAsserter.assertEqualsHelper(realm, value, dictionary[key])
        }

        // Now check we get the previous value after insertion
        realm.executeTransaction {
            alternativeDictionary.forEach { key, value ->
                typeAsserter.assertEqualsHelper(realm, initializedDictionary[key], dictionary.put(key, value))
            }
        }

        // Finally check that the alternative values are there
        dictionary.forEach { key, value ->
            assertTrue(alternativeDictionary.containsKey(key))
            typeAsserter.assertContainsValueHelper(realm, key, value, alternativeDictionary, dictionary)
        }
    }

    override fun remove() {
        val dictionary = initAndAssert()
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
                    typeAsserter.assertEqualsHelper(realm, value, dictionary.remove(key))
                    assertEquals(index, dictionary.size)

                    // Special case for RealmModels: remove the actual object from the Realm and
                    // check how that affects the dictionary
                    typeAsserter.assertRemove(dictionary, index, key, value)
                }
            }
        }
    }

    override fun putAll() {
        val dictionary = initAndAssert()
        assertTrue(dictionary.isEmpty())

        realm.executeTransaction {
            dictionary.putAll(initializedDictionary)
        }

        // Check initialized dictionary got inserted
        initializedDictionary.forEach { key, value ->
            typeAsserter.assertEqualsHelper(realm, value, dictionary[key])
        }
    }

    override fun clear() {
        val dictionary = initAndAssert()

        // Insert entries, assert not empty, clear and assert empty
        realm.executeTransaction {
            dictionary.putAll(initializedDictionary)
            assertFalse(dictionary.isEmpty())
            dictionary.clear()
            assertTrue(dictionary.isEmpty())
        }
    }

    override fun keySet() {
        val dictionary = initAndAssert()

        realm.executeTransaction {
            dictionary.putAll(initializedDictionary)
        }

        val keySet = dictionary.keys
        initializedDictionary.forEach { key, _ ->
            assertTrue(keySet.contains(key))
        }
    }

    override fun values() {
        val dictionary = initAndAssert()

        realm.executeTransaction {
            dictionary.putAll(initializedDictionary)
        }

        val values = dictionary.values
        values.forEach { value ->
            typeAsserter.assertValues(dictionary, value)
        }
    }

    override fun entrySet() {
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

                typeAsserter.assertEntrySetContains(dictionary, entry)
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
            assertTrue(entrySet is RealmMapEntrySet<String, T?>)
            val entrySetObjectArray = entrySet.toArray()
            for (entry in entrySetObjectArray) {
                assertTrue(entry is Map.Entry<*, *>)
                assertTrue(entrySet.contains(entry))
            }

            // Internal helper function
            fun testToArray(
                    entrySetArray: Array<Map.Entry<String, T?>?>,
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
            val testArraySmallerSize = arrayOfNulls<Map.Entry<String, T?>>(1)
            val entrySetSmallerSizeArray = entrySet.toArray(testArraySmallerSize)
            testToArray(entrySetSmallerSizeArray)

            // Test toArray: same size, return a new instance
            val testArraySameSize = arrayOfNulls<Map.Entry<String, T?>>(entrySet.size)
            val entrySetSameSizeArray = entrySet.toArray(testArraySameSize)
            testToArray(entrySetSameSizeArray)

            // Test toArray: bigger size, add null as the last entry
            val testArrayBiggerSize = arrayOfNulls<Map.Entry<String, T?>>(10)
            val entrySetBiggerSizeArray = entrySet.toArray(testArrayBiggerSize)
            testToArray(entrySetBiggerSizeArray, true)

            // Test containsAll
            typeAsserter.assertEntrySetContainsAll(initializedDictionary, dictionary, entrySet)

            val differentCollection = alternativeDictionary.map { entry ->
                AbstractMap.SimpleImmutableEntry(entry.key, entry.value)
            }.toSet()

            assertFalse(entrySet.containsAll(differentCollection))
        }
    }

    // This has already been tested in "isFrozen"
    override fun freeze() = Unit

    override fun copyToRealm() {
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
            typeAsserter.assertEqualsHelper(realm, value, dictionary[key])
        }
    }

    override fun copyFromRealm() {
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
            typeAsserter.assertEqualsHelper(realm, value, dictionary[key])
        }
    }

    //----------------------------------
    // Private stuff
    //----------------------------------

    private fun initAndAssert(): RealmDictionary<T> {
        val allTypesObject = createAllTypesManagedContainerAndAssert(realm)
        assertNotNull(allTypesObject)
        return dictionaryGetter.call(allTypesObject)
    }

    private fun createAllTypesManagedContainerAndAssert(realm: Realm): AllTypes {
        realm.executeTransaction { transactionRealm ->
            transactionRealm.createObject<AllTypes>()
        }
        val allTypesObject = realm.where<AllTypes>().findFirst()
        assertNotNull(allTypesObject)
        return allTypesObject
    }
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
                    alternativeDictionary = RealmDictionary<Boolean>().init(listOf(KEY_HELLO to null, KEY_NULL to VALUE_BOOLEAN_HELLO))
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
                    testerClass = "Binary",
                    dictionaryGetter = AllTypes::getColumnBinaryDictionary,
                    dictionarySetter = AllTypes::setColumnBinaryDictionary,
                    initializedDictionary = RealmDictionary<ByteArray>().init(listOf(KEY_HELLO to VALUE_BINARY_HELLO, KEY_BYE to VALUE_BINARY_BYE, KEY_NULL to null)),
                    alternativeDictionary = RealmDictionary<ByteArray>().init(listOf(KEY_HELLO to VALUE_BINARY_BYE, KEY_BYE to VALUE_BINARY_HELLO, KEY_NULL to null)),
                    typeAsserter = BinaryAsserter()
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
                    alternativeDictionary = RealmDictionary<DogPrimaryKey>().init(listOf(KEY_HELLO to VALUE_LINK_BYE, KEY_BYE to VALUE_LINK_HELLO, KEY_NULL to null)),
                    typeAsserter = RealmModelAsserter()
            )
    )

    // Create Mixed testers now
    val mixedTesters = MixedType.values().map { mixedType ->
        ManagedDictionaryTester(
                testerClass = "Mixed-${mixedType.name}",
                dictionaryGetter = AllTypes::getColumnMixedDictionary,
                dictionarySetter = AllTypes::setColumnMixedDictionary,
                initializedDictionary = RealmDictionary<Mixed>().init(getMixedKeyValuePairs(mixedType)),
                alternativeDictionary = RealmDictionary<Mixed>().init(getMixedKeyValuePairs(mixedType, true)),
                typeAsserter = MixedAsserter()
        )
    }

    return primitiveTesters.plus(mixedTesters)
}

private fun <T : Any> RealmDictionary<T>.init(
        keyValuePairs: List<Pair<String, T?>>
): RealmDictionary<T> {
    return this.apply {
        for (keyValuePair in keyValuePairs) {
            put(keyValuePair.first, keyValuePair.second)
        }
    }
}

/**
 * TODO
 */
open class TypeAsserter<T> {

    // RealmModel and Mixed require different testing here
    open fun assertContainsValue(realm: Realm, dictionary: RealmDictionary<T>, index: Int, value: T?) =
            assertFalse(dictionary.containsValue(value))

    // RealmModel and Mixed require different testing here
    open fun assertRemove(dictionary: RealmDictionary<T>, index: Int, key: String, value: T?) =
            Unit

    // RealmModel requires different testing here
    open fun assertValues(dictionary: RealmDictionary<T>, value: T?) =
            assertTrue(dictionary.containsValue(value))

    // RealmModel requires different testing here
    open fun assertEntrySetContains(
            dictionary: RealmDictionary<T>,
            entry: Map.Entry<String, T>
    ) = assertTrue(dictionary.containsValue(entry.value))

    // RealmModel and Mixed require different testing here
    open fun assertEntrySetContainsAll(
            initializedDictionary: RealmDictionary<T>,
            dictionary: RealmDictionary<T>,
            entrySet: Set<Map.Entry<String, T?>>
    ) {
        val sameCollection = initializedDictionary.map { entry ->
            AbstractMap.SimpleImmutableEntry(entry.key, entry.value)
        }.toSet()
        assertTrue(entrySet.containsAll(sameCollection))
    }

    // RealmModel and Mixed require different testing here
    open fun assertContainsValueHelper(
            realm: Realm,
            key: String,
            value: T?,
            unmanagedDictionary: RealmDictionary<T>,
            managedDictionary: RealmDictionary<T>
    ) {
        assertTrue(managedDictionary.containsValue(unmanagedDictionary[key]))
    }

    // ByteArray, RealmModel and Mixed require different testing here
    open fun assertEqualsHelper(realm: Realm, value: T?, valueFromRealm: T?) {
        assertEquals(value, valueFromRealm)
    }
}

/**
 * TODO
 */
class BinaryAsserter : TypeAsserter<ByteArray>() {

    override fun assertEqualsHelper(realm: Realm, value: ByteArray?, valueFromRealm: ByteArray?) {
        if (value == null && valueFromRealm == null) {
            return
        }

        assertNotNull(value)
        assertNotNull(valueFromRealm)
        assertTrue(value.contentEquals(valueFromRealm))
    }
}

/**
 * TODO
 */
class RealmModelAsserter : TypeAsserter<DogPrimaryKey>() {

    override fun assertContainsValue(
            realm: Realm,
            dictionary: RealmDictionary<DogPrimaryKey>,
            index: Int,
            value: DogPrimaryKey?
    ) {
        // Given that only managed models can be contained in a model dictionary, we need to
        // test containsValue with a dummy model
        realm.executeTransaction { transactionRealm ->
            val dummyRealmModel = transactionRealm.copyToRealm(DogPrimaryKey(666 + index.toLong(), "DUMMY"))
            assertFalse(dictionary.containsValue(dummyRealmModel as DogPrimaryKey))
        }
    }

    override fun assertRemove(
            dictionary: RealmDictionary<DogPrimaryKey>,
            index: Int,
            key: String,
            value: DogPrimaryKey?
    ) {
        if (value != null) {
            // Removal of actual RealmModel to check whether it vanished from the dictionary
            // Insert again - "value" is unmanaged
            dictionary[key] = value

            // Delete from realm and check we get null if we get it from the dictionary
            val modelFromRealm = dictionary[key] as DogPrimaryKey
            assertTrue(modelFromRealm.isValid)

            modelFromRealm.deleteFromRealm()
            assertFalse(modelFromRealm.isValid)

            assertNull(dictionary[key])

            // Check size again (despite object removal, size should remain unchanged)
            assertEquals(index + 1, dictionary.size)

            // Delete it again so that the forEach size check works
            dictionary.remove(key)
            assertEquals(index, dictionary.size)
        }
    }

    override fun assertValues(dictionary: RealmDictionary<DogPrimaryKey>, value: DogPrimaryKey?) {
        // null entries become "invalid object" when calling dictionary.values()
        assertNotNull(value)
        if (value.isValid) {
            assertTrue(dictionary.containsValue(value))
        } else {
            assertTrue(dictionary.containsValue(null))
        }
    }

    override fun assertEntrySetContains(
            dictionary: RealmDictionary<DogPrimaryKey>,
            entry: Map.Entry<String, DogPrimaryKey>
    ) {
        val dog = dictionary[entry.key]
        assertEquals(dog, entry.value)
    }

    override fun assertEntrySetContainsAll(
            initializedDictionary: RealmDictionary<DogPrimaryKey>,
            dictionary: RealmDictionary<DogPrimaryKey>,
            entrySet: Set<Map.Entry<String, DogPrimaryKey?>>
    ) {
        val sameRealmModelCollection: List<Map.Entry<String, DogPrimaryKey?>> =
                initializedDictionary.map { originalEntry ->
                    AbstractMap.SimpleImmutableEntry(originalEntry.key, dictionary[originalEntry.key])
                }
        assertTrue(entrySet.containsAll(sameRealmModelCollection))
    }

    override fun assertContainsValueHelper(
            realm: Realm,
            key: String,
            value: DogPrimaryKey?,
            unmanagedDictionary: RealmDictionary<DogPrimaryKey>,
            managedDictionary: RealmDictionary<DogPrimaryKey>
    ) {
        if (value == null) {
            assertTrue(managedDictionary.containsValue(null))
        } else {
            // Use managed model for containsValue: managed dictionaries can only contain managed models
            val managedRealmModel = realm.where<DogPrimaryKey>()
                    .equalTo("name", value.name)
                    .findFirst()
            assertTrue(managedDictionary.containsValue(managedRealmModel))
        }
    }

    override fun assertEqualsHelper(
            realm: Realm,
            value: DogPrimaryKey?,
            valueFromRealm: DogPrimaryKey?
    ) {
        val modelFromRealm = realm.where<DogPrimaryKey>()
                .equalTo("name", valueFromRealm?.name)
                .findFirst()
        if (value == null && valueFromRealm == null) {
            assertEquals(modelFromRealm, valueFromRealm)
            return
        }

        assertNotNull(modelFromRealm)
        assertEquals(modelFromRealm, valueFromRealm)
    }
}

/**
 * TODO
 */
class MixedAsserter : TypeAsserter<Mixed>() {

    override fun assertContainsValue(
            realm: Realm,
            dictionary: RealmDictionary<Mixed>,
            index: Int,
            value: Mixed?
    ) {
        if (value?.valueClass == DogPrimaryKey::class.java) {
            realm.executeTransaction { transactionRealm ->
                val dummyRealmModel = transactionRealm.copyToRealm(DogPrimaryKey(666 + index.toLong(), "DUMMY"))
                val mixedWithManagedModel = Mixed.valueOf(dummyRealmModel)
                assertFalse(dictionary.containsValue(mixedWithManagedModel as Mixed))
            }
        } else {
            assertFalse(dictionary.containsValue(value))
        }
    }

    override fun assertRemove(dictionary: RealmDictionary<Mixed>, index: Int, key: String, value: Mixed?) {
        // No need to check anything for other types than RealmModel
        if (value is Mixed && value.valueClass == DogPrimaryKey::class.java) {
            // Removal of actual RealmModel to check whether it vanished from the dictionary
            // Insert again - "value" is unmanaged
            dictionary[key] = value

            // Delete from realm and check we get null if we get it from the dictionary
            val mixedValue = dictionary[key] as Mixed
            val modelFromRealm = mixedValue.asRealmModel(DogPrimaryKey::class.java)
            assertTrue(modelFromRealm.isValid)

            modelFromRealm.deleteFromRealm()
            assertFalse(modelFromRealm.isValid)

            assertTrue((dictionary[key] as Mixed).isNull)

            // Check size again (despite object removal, size should remain unchanged)
            assertEquals(index + 1, dictionary.size)

            // Delete it again so that the forEach size check works
            dictionary.remove(key)
            assertEquals(index, dictionary.size)
        }
    }

    override fun assertEntrySetContainsAll(
            initializedDictionary: RealmDictionary<Mixed>,
            dictionary: RealmDictionary<Mixed>,
            entrySet: Set<Map.Entry<String, Mixed?>>
    ) {
        val sameCollection =
                dictionary.map { entry: Map.Entry<String, Mixed?> ->
                    // Realm doesn't deliver "pure null" values for Mixed, but rather Mixed.nullValue()
                    when (entry.value) {
                        null -> AbstractMap.SimpleImmutableEntry(entry.key, Mixed.nullValue())
                        else -> AbstractMap.SimpleImmutableEntry(entry.key, entry.value)
                    }
                }.toSet() as Set<Map.Entry<String, Mixed>>
        assertTrue(entrySet.containsAll(sameCollection))
    }

    override fun assertContainsValueHelper(
            realm: Realm,
            key: String,
            value: Mixed?,
            unmanagedDictionary: RealmDictionary<Mixed>,
            managedDictionary: RealmDictionary<Mixed>
    ) {
        // We can never get null Mixed values from a managed dictionary
        assertNotNull(value)

        if (value.isNull) {
            // If null, check we have "Mixed.nullValue()"
            assertTrue(managedDictionary.containsValue(Mixed.nullValue()))
        } else if (value.valueClass == DogPrimaryKey::class.java) {
            // If RealmModel, check dictionary contains a Mixed containing the managed model
            val managedRealmDog = realm.where<DogPrimaryKey>()
                    .equalTo("name", value.asRealmModel(DogPrimaryKey::class.java).name)
                    .findFirst()
            val mixedWithManagedDog = Mixed.valueOf(managedRealmDog)
            assertTrue(managedDictionary.containsValue(mixedWithManagedDog))
        } else {
            assertTrue(managedDictionary.containsValue(managedDictionary[key]))
        }
    }

    override fun assertEqualsHelper(realm: Realm, value: Mixed?, valueFromRealm: Mixed?) {
        // If null, check we have "Mixed.nullValue()"
        if (null == value) {
            assertNotNull(valueFromRealm)
            assertTrue(valueFromRealm.isNull)
        } else if (value.valueClass == DogPrimaryKey::class.java) {
            // If RealmModel, check provided the Mixed equals a Mixed containing the managed model
            val managedRealmModel = realm.where<DogPrimaryKey>()
                    .equalTo("name", (value as Mixed).asRealmModel(DogPrimaryKey::class.java).name)
                    .findFirst()
            val mixedWithManagedModel = Mixed.valueOf(managedRealmModel)
            assertEquals(valueFromRealm, mixedWithManagedModel)
        } else {
            assertEquals(value, valueFromRealm)
        }
    }
}
