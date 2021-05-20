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

import io.realm.entities.DictionaryAllTypes
import io.realm.entities.DogPrimaryKey
import io.realm.entities.PopulatedDictionaryClass
import io.realm.entities.PrimaryKeyDictionaryContainer
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import io.realm.rule.BlockingLooperThread
import org.bson.types.Decimal128
import org.bson.types.ObjectId
import java.lang.reflect.InvocationTargetException
import java.util.*
import kotlin.reflect.KFunction1
import kotlin.reflect.KFunction2
import kotlin.reflect.KProperty1
import kotlin.test.*

/**
 * Generic tester for all types of managed dictionaries.
 *
 * It uses `KFunction1` and `KFunction2` to streamline the access to [RealmDictionary] fields in
 * [DictionaryAllTypes]. This way we only need one tester for all supported types.
 */
class ManagedDictionaryTester<T : Any>(
        private val testerClass: String,
        private val realmAnyType: RealmAny.Type? = null,
        private val dictionaryFieldName: String,
        private val dictionaryFieldClass: Class<T>,
        private val dictionaryGetter: KFunction1<DictionaryAllTypes, RealmDictionary<T>>,
        private val dictionarySetter: KFunction2<DictionaryAllTypes, RealmDictionary<T>, Unit>,
        private val requiredDictionaryGetter: KFunction1<DictionaryAllTypes, RealmDictionary<T>>? = null,
        private val initializedDictionary: RealmDictionary<T>,
        private val alternativeDictionary: RealmDictionary<T>,
        private val notPresentValue: T,
        private val populatedGetter: KProperty1<PopulatedDictionaryClass, RealmDictionary<T>>,
        private val typeAsserter: TypeAsserter<T> = TypeAsserter(),
        private val primaryKeyDictionaryProperty: KProperty1<PrimaryKeyDictionaryContainer, RealmDictionary<T>>
) : DictionaryTester {

    private lateinit var config: RealmConfiguration
    private lateinit var looperThread: BlockingLooperThread
    private lateinit var realm: Realm

    override fun toString(): String = when (realmAnyType) {
        null -> "ManagedDictionary-$testerClass"
        else -> "ManagedDictionary-$testerClass" + realmAnyType.name.let { "-$it" }
    }

    override fun setUp(config: RealmConfiguration, looperThread: BlockingLooperThread) {
        this.config = config
        this.looperThread = looperThread
        this.realm = Realm.getInstance(config)
    }

    override fun tearDown() = realm.close()

    override fun constructorWithAnotherMap() = Unit             // Not applicable in managed mode

    override fun isManaged() = assertTrue(initAndAssert().isManaged)

    override fun isValid() = assertTrue(initAndAssert().isValid)

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

        assertFailsWith<NullPointerException> {
            dictionary.containsKey(null)
        }

        val somethingEntirelyDifferent = initializedDictionary.map { (key, _) ->
            Pair(key, key)
        }
        assertFailsWith<ClassCastException> {
            dictionary.containsKey(somethingEntirelyDifferent as Any)
        }
    }

    override fun containsValue() {
        val dictionary = initAndAssert()

        initializedDictionary.values.forEachIndexed { index, value ->
            typeAsserter.assertContainsValueNotThere(realm, dictionary, index, value)
        }

        realm.executeTransaction {
            initializedDictionary.forEach { key, value ->
                dictionary[key] = value
            }
        }

        dictionary.forEach { key, value ->
            typeAsserter.assertContainsValueHelper(realm, key, value, initializedDictionary, dictionary)
        }

        val somethingEntirelyDifferent = initializedDictionary.map { (key, _) ->
            Pair(key, key)
        }
        assertFailsWith<ClassCastException> {
            dictionary.containsValue(somethingEntirelyDifferent as Any)
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

        assertFailsWith<NullPointerException> {
            dictionary.get(TestHelper.getNull())
        }

        val somethingEntirelyDifferent = initializedDictionary.map { (key, _) ->
            Pair(key, key)
        }
        assertFailsWith<ClassCastException> {
            dictionary.get(somethingEntirelyDifferent as Any)
        }
    }

    override fun put() = putInternal(initializedDictionary, alternativeDictionary)

    override fun putRequired() {
        // RealmModel and RealmAny dictionaries are ignored since they cannot be marked with "@Required"
        if (requiredDictionaryGetter != null) {
            val allTypesObject = createCollectionAllTypesManagedContainerAndAssert(realm)
            assertNotNull(allTypesObject)
            val dictionary = requiredDictionaryGetter.call(allTypesObject)

            // Check we can't insert null on a RealmDictionary marked as "@Required"
            realm.executeTransaction {
                assertFailsWith<NullPointerException> {
                    dictionary["requiredKey"] = null
                }
            }

            // Now check it works normally for the same field but without inserting null values
            val initializedNoNull = initializedDictionary.apply { assertNull(remove(KEY_NULL)) }
            val alternativeNoNull = alternativeDictionary.apply { assertNull(remove(KEY_NULL)) }
            putInternal(initializedNoNull, alternativeNoNull)
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
                    typeAsserter.assertRemoveRealmModelFromRealm(dictionary, index, key, value)
                }
            }

            assertFailsWith<NullPointerException> {
                dictionary.remove(TestHelper.getNull())
            }

            val somethingEntirelyDifferent = initializedDictionary.map { (key, _) ->
                Pair(key, key)
            }
            assertFailsWith<ClassCastException> {
                dictionary.remove(somethingEntirelyDifferent as Any)
            }
        }
    }

    override fun putAll() {
        val dictionary = initAndAssert()
        val anotherDictionary = initAndAssert(id = "anotherDictionary")

        assertTrue(dictionary.isEmpty())
        assertTrue(anotherDictionary.isEmpty())

        realm.executeTransaction {
            dictionary.putAll(initializedDictionary)

            // Check initialized dictionary got inserted
            initializedDictionary.forEach { key, value ->
                typeAsserter.assertEqualsHelper(realm, value, dictionary[key])
            }

            // Put a managed dictionary (itself)
            dictionary.putAll(dictionary)
            assertEquals(dictionary.size, initializedDictionary.size)

            // Put a managed dictionary containing something else
            anotherDictionary[KEY_NOT_PRESENT] = notPresentValue
            dictionary.putAll(anotherDictionary)
            assertEquals(dictionary.size, initializedDictionary.size + anotherDictionary.size)

            // TODO: It is not possible to test that putting a map containing null keys throws
            //  a NullPointerException from Kotlin, even when using TestHelper.getNull() due to
            //  some bytecode generation that doesn't match.
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
        val allTypesObject = createCollectionAllTypesManagedContainerAndAssert(realm)
        assertNotNull(allTypesObject)
        val dictionary = dictionaryGetter.call(allTypesObject)

        realm.executeTransaction {
            dictionary.putAll(initializedDictionary)
            val entrySet = dictionary.entries
            assertNotNull(entrySet)

            // Test size
            assertEquals(initializedDictionary.size, entrySet.size)

            // Test contains
            dictionary.keys.forEach { dictionaryKey ->
                val dictionaryValue = dictionary[dictionaryKey]
                val otherEntry = AbstractMap.SimpleImmutableEntry(dictionaryKey, dictionaryValue)
                assertTrue(entrySet.contains(otherEntry))
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
                    else -> assertEquals(entrySet.size, entrySetArray.size)
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
            val otherEntryCollection = dictionary.keys.map { dictionaryKey ->
                val dictionaryValue = dictionary[dictionaryKey]
                AbstractMap.SimpleImmutableEntry(dictionaryKey, dictionaryValue)
            }
            assertTrue(entrySet.containsAll(otherEntryCollection))

            val differentCollection = alternativeDictionary.map { entry ->
                AbstractMap.SimpleImmutableEntry(entry.key, entry.value)
            }.toSet()

            assertFalse(entrySet.containsAll(differentCollection))
        }
    }

    override fun dynamic() {
        // The methods for Realm object and primitive types are different
        if (notPresentValue is DogPrimaryKey) {
            doObjectDynamicTest()
        } else {
            doPrimitiveDynamicTest()
        }
    }

    private fun doPrimitiveDynamicTest() {
        // Create a dictionary from a immutable schema context
        val dictionary = initAndAssert()
        realm.executeTransaction {
            dictionary.putAll(initializedDictionary)
        }

        val dynamicRealm = DynamicRealm.getInstance(realm.configuration)
        val dynamicObject: DynamicRealmObject = dynamicRealm.where(DictionaryAllTypes.NAME).equalTo("columnString", "").findFirst()!!
        val dynamicDictionary = dynamicObject.getDictionary(dictionaryFieldName, dictionaryFieldClass)

        // Access the previous dictionary from a mutable context
        dictionary.values.forEach { value ->
            typeAsserter.assertValues(dynamicDictionary, value)
        }

        // Update the dictionary with a new value
        dynamicRealm.executeTransaction {
            dynamicDictionary[KEY_NOT_PRESENT] = notPresentValue
        }

        dictionary.values.plus(notPresentValue).forEach { value ->
            typeAsserter.assertValues(dynamicDictionary, value)
        }

        dictionary.keys.plus(KEY_NOT_PRESENT).forEach { key ->
            typeAsserter.assertKeys(dynamicDictionary, key)
        }

        // Try to replace the whole dictionary by a new one
        dynamicRealm.executeTransaction {
            dynamicObject.setDictionary(dictionaryFieldName, RealmDictionary<T>().apply {
                this[KEY_NOT_PRESENT] = notPresentValue
            })
        }

        assertEquals(1, dynamicObject.get<RealmDictionary<T>>(dictionaryFieldName).size)

        // Validate that dict is properly represented as a String
        validateToString(dynamicObject, dynamicDictionary)

        dynamicRealm.close()
    }

    private fun validateToString(dynamicObject: DynamicRealmObject, dynamicDictionary: RealmDictionary<*>) {
        val type = when (dictionaryFieldClass.simpleName) {
            "Byte", "Short", "Integer" -> "Long"
            else -> dictionaryFieldClass.simpleName
        }

        val expectedDictionaryString = "${dictionaryFieldName}:RealmDictionary<$type>[${dynamicDictionary.size}]"
        assertTrue(
            dynamicObject.toString().contains(expectedDictionaryString),
            "DynamicRealmObject does not contain expected RealmDictionary string: $expectedDictionaryString"
        )
    }

    private fun doObjectDynamicTest() {
        // Create a dictionary from a immutable schema context
        val dictionary = initAndAssert()
        realm.executeTransaction {
            dictionary.putAll(initializedDictionary)
            realm.insert(notPresentValue as DogPrimaryKey)
        }

        val dynamicRealm = DynamicRealm.getInstance(realm.configuration)
        val dynamicObject: DynamicRealmObject =
            dynamicRealm.where(DictionaryAllTypes.NAME).equalTo("columnString", "").findFirst()!!
        val dynamicDictionary = dynamicObject.getDictionary(dictionaryFieldName)

        // Access the previous dictionary from a mutable context
        dictionary.values.forEach { value ->
            if (RealmObject.isValid(value as DogPrimaryKey)) {
                val managedObject =
                    dynamicRealm.where(DogPrimaryKey.CLASS_NAME).equalTo(DogPrimaryKey.ID, (value as DogPrimaryKey).id)
                        .findFirst()!!
                typeAsserter.assertDynamicValues(dynamicDictionary, managedObject)
            }
        }

        // Update the dictionary with a new value
        dynamicRealm.executeTransaction {
            val notPresentManaged = dynamicRealm.where(DogPrimaryKey.CLASS_NAME)
                .equalTo(DogPrimaryKey.ID, (notPresentValue as DogPrimaryKey).id).findFirst()!!
            dynamicDictionary[KEY_NOT_PRESENT] = notPresentManaged
        }

        dictionary.values.plus(notPresentValue).forEach { value ->
            if (RealmObject.isValid(value as DogPrimaryKey)) {
                val managedObject =
                    dynamicRealm.where(DogPrimaryKey.CLASS_NAME).equalTo(DogPrimaryKey.ID, (value as DogPrimaryKey).id)
                        .findFirst()!!
                typeAsserter.assertDynamicValues(dynamicDictionary, managedObject)
            }
        }

        dictionary.keys.plus(KEY_NOT_PRESENT).forEach { key ->
            typeAsserter.assertKeys(dynamicDictionary, key)
        }

        // Try to replace the whole dictionary by a new one
        dynamicRealm.executeTransaction {
            val notPresentManaged = dynamicRealm.where(DogPrimaryKey.CLASS_NAME)
                .equalTo(DogPrimaryKey.ID, (notPresentValue as DogPrimaryKey).id).findFirst()!!
            dynamicObject.setDictionary(dictionaryFieldName, RealmDictionary<DynamicRealmObject>().apply {
                this[KEY_NOT_PRESENT] = notPresentManaged
            })
        }

        assertEquals(1, dynamicObject.get<RealmDictionary<T>>(dictionaryFieldName).size)

        // Validate that dict is properly represented as a String
        validateToString(dynamicObject, dynamicDictionary)

        dynamicRealm.close()
    }

    override fun freeze() = Unit                    // This has already been tested in "isFrozen"

    override fun copyToRealm() {
        // Instantiate container and set dictionary on container
        val manualInstance = DictionaryAllTypes().apply {
            dictionarySetter.call(this, initializedDictionary)
        }

        // Copy to Realm
        realm.executeTransaction {
            val allTypesObject = realm.copyToRealm(manualInstance)
            assertNotNull(allTypesObject)
        }

        // Get dictionary from container from Realm
        val allTypesObject = realm.where<DictionaryAllTypes>().findFirst()
        assertNotNull(allTypesObject)
        val dictionary = dictionaryGetter.call(allTypesObject)
        assertFalse(dictionary.isEmpty())
        initializedDictionary.forEach { key, value ->
            typeAsserter.assertEqualsHelper(realm, value, dictionary[key])
        }
    }

    override fun copyToRealmOrUpdate() {
        // Instantiate container and set dictionary on container
        val manualInstance = PrimaryKeyDictionaryContainer().apply {
            primaryKeyDictionaryProperty.get(this).putAll(initializedDictionary)
        }

        // Copy to Realm
        realm.executeTransaction {
            val allTypesObject = realm.copyToRealmOrUpdate(manualInstance)
            assertNotNull(allTypesObject)
        }

        // Get dictionary from container from Realm
        val primaryKeyDictionaryContainer = realm.where<PrimaryKeyDictionaryContainer>().findFirst()
        assertNotNull(primaryKeyDictionaryContainer)
        val dictionary = primaryKeyDictionaryProperty.get(primaryKeyDictionaryContainer)
        assertFalse(dictionary.isEmpty())
        initializedDictionary.forEach { key, value ->
            typeAsserter.assertEqualsHelper(realm, value, dictionary[key])
        }

        if (testerClass == "String-NonLatin" || testerClass == "RealmAny-NonLatin") {
            primaryKeyDictionaryProperty.get(manualInstance)[NEW_KEY_NON_LATIN] = alternativeDictionary[KEY_BYE_NON_LATIN]
        } else {
            primaryKeyDictionaryProperty.get(manualInstance)[NEW_KEY] = alternativeDictionary[KEY_BYE]
        }

        // Copy to Realm with non managed updated model
        realm.executeTransaction {
            val allTypesObject = realm.copyToRealmOrUpdate(manualInstance)
            assertNotNull(allTypesObject)
        }

        val updatedContainer = realm.where<PrimaryKeyDictionaryContainer>().findFirst()
        assertNotNull(updatedContainer)
        val updatedDictinary = primaryKeyDictionaryProperty.get(primaryKeyDictionaryContainer)
        assertEquals(initializedDictionary.size + 1, updatedDictinary.size)
        if (testerClass == "String-NonLatin" || testerClass == "RealmAny-NonLatin") {
            typeAsserter.assertEqualsHelper(realm, alternativeDictionary[KEY_BYE_NON_LATIN], updatedDictinary[NEW_KEY_NON_LATIN])
        } else {
            typeAsserter.assertEqualsHelper(realm, alternativeDictionary[KEY_BYE], updatedDictinary[NEW_KEY])
        }
    }

    override fun insert() {
        // Instantiate container and set dictionary on container
        val manualInstance = DictionaryAllTypes().apply {
            dictionarySetter.call(this, initializedDictionary)
        }

        // Insert into Realm
        realm.executeTransaction {
            realm.insert(manualInstance)
        }

        // Get dictionary from container from Realm
        val allTypesObject = realm.where<DictionaryAllTypes>().findFirst()
        assertNotNull(allTypesObject)
        val dictionary = dictionaryGetter.call(allTypesObject)
        assertFalse(dictionary.isEmpty())
        initializedDictionary.forEach { key, value ->
            typeAsserter.assertEqualsHelper(realm, value, dictionary[key])
        }
    }

    override fun insertList() {
        // Instantiate container and set dictionary on container
        val manualInstance = DictionaryAllTypes().apply {
            dictionarySetter.call(this, initializedDictionary)
        }
        val emptyInstance = DictionaryAllTypes()

        // Insert into Realm
        realm.executeTransaction {
            realm.insert(listOf(emptyInstance, manualInstance))
        }

        // Get dictionary from container from Realm
        val allTypesObject = realm.where<DictionaryAllTypes>().findAll()[1]
        assertNotNull(allTypesObject)
        val dictionary = dictionaryGetter.call(allTypesObject)
        assertFalse(dictionary.isEmpty())
        initializedDictionary.forEach { key, value ->
            typeAsserter.assertEqualsHelper(realm, value, dictionary[key])
        }
    }

    override fun insertOrUpdate() {
        // Instantiate container and set dictionary on container
        val manualInstance = PrimaryKeyDictionaryContainer().apply {
            primaryKeyDictionaryProperty.get(this).putAll(initializedDictionary)
        }

        // insert into Realm
        realm.executeTransaction {
            realm.insertOrUpdate(manualInstance)
        }

        // Get dictionary from container from Realm
        val primaryKeyDictionaryContainer = realm.where<PrimaryKeyDictionaryContainer>().findFirst()
        assertNotNull(primaryKeyDictionaryContainer)
        val dictionary = primaryKeyDictionaryProperty.get(primaryKeyDictionaryContainer)
        assertFalse(dictionary.isEmpty())
        initializedDictionary.forEach { key, value ->
            typeAsserter.assertEqualsHelper(realm, value, dictionary[key])
        }

        if (testerClass == "String-NonLatin" || testerClass == "RealmAny-NonLatin") {
            primaryKeyDictionaryProperty.get(manualInstance)[NEW_KEY_NON_LATIN] = alternativeDictionary[KEY_BYE_NON_LATIN]
        } else {
            primaryKeyDictionaryProperty.get(manualInstance)[NEW_KEY] = alternativeDictionary[KEY_BYE]
        }

        // Insert to Realm with non managed updated model
        realm.executeTransaction {
            realm.insertOrUpdate(manualInstance)
        }

        val updatedContainer = realm.where<PrimaryKeyDictionaryContainer>().findFirst()
        assertNotNull(updatedContainer)
        val updatedDictinary = primaryKeyDictionaryProperty.get(primaryKeyDictionaryContainer)
        assertEquals(initializedDictionary.size + 1, updatedDictinary.size)
        if (testerClass == "String-NonLatin" || testerClass == "RealmAny-NonLatin") {
            typeAsserter.assertEqualsHelper(realm, alternativeDictionary[KEY_BYE_NON_LATIN], updatedDictinary[NEW_KEY_NON_LATIN])
        } else {
            typeAsserter.assertEqualsHelper(realm, alternativeDictionary[KEY_BYE], updatedDictinary[NEW_KEY])
        }
    }

    override fun insertOrUpdateList() {
        // Instantiate container and set dictionary on container
        val manualInstance = PrimaryKeyDictionaryContainer().apply {
            name = "manual"
            primaryKeyDictionaryProperty.get(this).putAll(initializedDictionary)
        }
        val emptyInstance = PrimaryKeyDictionaryContainer().apply {
            name = "empty"
        }

        // insert into Realm
        realm.executeTransaction {
            realm.insertOrUpdate(listOf(emptyInstance, manualInstance))
        }

        // Get dictionary from container from Realm
        val primaryKeyDictionaryContainer = realm.where<PrimaryKeyDictionaryContainer>()
            .equalTo("name", "manual")
            .findFirst()
        assertNotNull(primaryKeyDictionaryContainer)
        val dictionary = primaryKeyDictionaryProperty.get(primaryKeyDictionaryContainer)
        assertFalse(dictionary.isEmpty())
        initializedDictionary.forEach { key, value ->
            typeAsserter.assertEqualsHelper(realm, value, dictionary[key])
        }

        if (testerClass == "String-NonLatin" || testerClass == "RealmAny-NonLatin") {
            primaryKeyDictionaryProperty.get(manualInstance)[NEW_KEY_NON_LATIN] = alternativeDictionary[KEY_BYE_NON_LATIN]
        } else {
            primaryKeyDictionaryProperty.get(manualInstance)[NEW_KEY] = alternativeDictionary[KEY_BYE]
        }

        // Insert to Realm with non managed updated model
        realm.executeTransaction {
            realm.insertOrUpdate(listOf(emptyInstance, manualInstance))
        }

        val updatedContainer = realm.where<PrimaryKeyDictionaryContainer>()
            .equalTo("name", "manual")
            .findFirst()
        assertNotNull(updatedContainer)
        val updatedDictinary = primaryKeyDictionaryProperty.get(primaryKeyDictionaryContainer)
        assertEquals(initializedDictionary.size + 1, updatedDictinary.size)
        if (testerClass == "String-NonLatin" || testerClass == "RealmAny-NonLatin") {
            typeAsserter.assertEqualsHelper(realm, alternativeDictionary[KEY_BYE_NON_LATIN], updatedDictinary[NEW_KEY_NON_LATIN])
        } else {
            typeAsserter.assertEqualsHelper(realm, alternativeDictionary[KEY_BYE], updatedDictinary[NEW_KEY])
        }
    }

    override fun copyFromRealm() {
        val allTypesObject = createCollectionAllTypesManagedContainerAndAssert(realm)
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

    override fun fieldAccessors(otherConfig: RealmConfiguration?) {
        realm.executeTransaction { transactionRealm ->
            val container = transactionRealm.createObject<PopulatedDictionaryClass>()
            val dictionary = populatedGetter.get(container)
            assertNotNull(dictionary)
            assertTrue(dictionary.isManaged)
            assertFalse(dictionary.isEmpty())
        }

        assertNotNull(otherConfig)
        typeAsserter.assertAccessorSetter(
            realm,
            dictionaryGetter,
            dictionarySetter,
            initializedDictionary,
            otherConfig
        )
    }

    override fun addMapChangeListener() {
        looperThread.runBlocking {
            val looperThreadRealm = Realm.getInstance(config)

            // Get dictionary
            val dictionary = initAndAssert(looperThreadRealm)

            // Define operation we perform on the dictionary
            var operation = ChangeListenerOperation.UNDEFINED

            dictionary.addChangeListener { map, changes ->
                typeAsserter.assertChangeListenerUpdates(
                        testerClass,
                        operation,
                        looperThread,
                        looperThreadRealm,
                        dictionary,
                        initializedDictionary,
                        map,
                        changes
                )
            }

            // Insert objects in dictionary
            looperThreadRealm.executeTransaction {
                operation = ChangeListenerOperation.INSERT
                dictionary.putAll(initializedDictionary)
            }

            // Update object
            looperThreadRealm.executeTransaction {
                operation = ChangeListenerOperation.UPDATE

                // Use 'dictionary' instead of 'alternativeDictionary' as the semantics for
                // inserting unmanaged objects might lead to having one extra update in the
                // change set - calling 'put' with an unmanaged object with PK that already is
                // in the database will trigger two changes: one for the actual modification and
                // two for insertion itself since we use 'copyToRealmOrUpdate'.
                if (testerClass == "String-NonLatin" || testerClass == "RealmAny-NonLatin") {
                    dictionary[KEY_HELLO_NON_LATIN] = dictionary[KEY_BYE_NON_LATIN]
                } else {
                    dictionary[KEY_HELLO] = dictionary[KEY_BYE]
                }
            }

            // Clear dictionary
            looperThreadRealm.executeTransaction {
                operation = ChangeListenerOperation.DELETE
                dictionary.clear()
            }
        }
    }

    override fun addRealmChangeListener() {
        looperThread.runBlocking {
            val looperThreadRealm = Realm.getInstance(config)

            // Get dictionary
            val dictionary = initAndAssert(looperThreadRealm)

            // Define operation we perform on the dictionary
            var operation = ChangeListenerOperation.UNDEFINED

            dictionary.addChangeListener { map ->
                typeAsserter.assertChangeListenerUpdates(
                        testerClass,
                        operation,
                        looperThread,
                        looperThreadRealm,
                        dictionary,
                        initializedDictionary,
                        map
                )
            }

            // Insert objects in dictionary
            looperThreadRealm.executeTransaction {
                operation = ChangeListenerOperation.INSERT
                dictionary.putAll(initializedDictionary)
            }

            // Update object
            looperThreadRealm.executeTransaction {
                operation = ChangeListenerOperation.UPDATE

                // Use 'dictionary' instead of 'alternativeDictionary' as the semantics for
                // inserting unmanaged objects might lead to having one extra update in the
                // change set - calling 'put' with an unmanaged object with PK that already is
                // in the database will trigger two changes: one for the actual modification and
                // two for insertion itself since we use 'copyToRealmOrUpdate'.
                if (testerClass == "String-NonLatin" || testerClass == "RealmAny-NonLatin") {
                    dictionary[KEY_HELLO_NON_LATIN] = dictionary[KEY_BYE_NON_LATIN]
                } else {
                    dictionary[KEY_HELLO] = dictionary[KEY_BYE]
                }
            }

            // Clear dictionary
            looperThreadRealm.executeTransaction {
                operation = ChangeListenerOperation.DELETE
                dictionary.clear()
            }
        }
    }

    override fun hasListeners() {
        val looperThread = BlockingLooperThread()
        looperThread.runBlocking {
            val looperThreadRealm = Realm.getInstance(config)

            // Check for RealmChangeListener
            val dictionary = initAndAssert(looperThreadRealm)
            assertFalse(dictionary.hasListeners())

            dictionary.addChangeListener { _ -> /* no-op */ }

            assertTrue(dictionary.hasListeners())

            // Check for MapChangeListener
            val anotherDictionary = initAndAssert(looperThreadRealm, "anotherDictionary")
            assertFalse(anotherDictionary.hasListeners())

            anotherDictionary.addChangeListener { _, _ -> /* no-op */ }

            assertTrue(anotherDictionary.hasListeners())

            // Housekeeping and bye-bye
            dictionary.removeAllChangeListeners()
            anotherDictionary.removeAllChangeListeners()
            looperThreadRealm.close()
            looperThread.testComplete()
        }
    }

    //----------------------------------
    // Private stuff
    //----------------------------------

    private fun initAndAssert(
            realm: Realm = this.realm,
            id: String? = null
    ): RealmDictionary<T> {
        val allTypesObject = createCollectionAllTypesManagedContainerAndAssert(realm, id)
        assertNotNull(allTypesObject)
        return dictionaryGetter.call(allTypesObject)
    }

    private fun putInternal(
            initialized: RealmDictionary<T>,
            alternative: RealmDictionary<T>
    ) {
        val dictionary = initAndAssert(id = "internal")

        realm.executeTransaction {
            // Check we get null since previous values are not present
            initialized.forEach { key, value ->
                assertNull(dictionary.put(key, value))
            }
        }
        initialized.forEach { key, value ->
            typeAsserter.assertEqualsHelper(realm, value, dictionary[key])
        }

        // Now check we get the previous value after insertion
        realm.executeTransaction {
            alternative.forEach { key, value ->
                typeAsserter.assertEqualsHelper(realm, initialized[key], dictionary.put(key, value))
            }

            // Check null key fails
            assertFailsWith<NullPointerException> {
                dictionary[TestHelper.getNull()] = initializedDictionary[KEY_HELLO]
            }
        }

        // Finally check that the alternative values are there
        dictionary.forEach { key, value ->
            assertTrue(alternative.containsKey(key))
            typeAsserter.assertContainsValueHelper(realm, key, value, alternative, dictionary)
        }
    }
}

enum class ChangeListenerOperation {
    UNDEFINED, INSERT, UPDATE, DELETE
}

/**
 * Creates testers for all [DictionarySupportedType]s and initializes them for testing. There are as
 * many RealmAny testers as [RealmAny.Type]s.
 *
 * The `KFunction1` and `KFunction2` parameters for `dictionaryGetter` and `dictionarySetter`
 * respectively enables agnostic field processing, making it possible to cover all supported types
 * with just one tester class.
 */
fun managedDictionaryFactory(): List<DictionaryTester> {
    val primitiveTesters = listOf<DictionaryTester>(
            ManagedDictionaryTester(
                    testerClass = "Long",
                    dictionaryFieldClass = Long::class.javaObjectType,
                    dictionaryFieldName = "columnLongDictionary",
                    dictionaryGetter = DictionaryAllTypes::getColumnLongDictionary,
                    dictionarySetter = DictionaryAllTypes::setColumnLongDictionary,
                    requiredDictionaryGetter = DictionaryAllTypes::getColumnRequiredLongDictionary,
                    initializedDictionary = RealmDictionary<Long>().init(listOf(KEY_HELLO to VALUE_NUMERIC_HELLO.toLong(), KEY_BYE to VALUE_NUMERIC_BYE.toLong(), KEY_NULL to null)),
                    alternativeDictionary = RealmDictionary<Long>().init(listOf(KEY_HELLO to VALUE_NUMERIC_BYE.toLong(), KEY_BYE to VALUE_NUMERIC_HELLO.toLong(), KEY_NULL to null)),
                    notPresentValue = VALUE_NUMERIC_NOT_PRESENT.toLong(),
                    populatedGetter = PopulatedDictionaryClass::populatedLongDictionary,
                    primaryKeyDictionaryProperty = PrimaryKeyDictionaryContainer::myLongDictionary
            ),
            ManagedDictionaryTester(
                    testerClass = "Int",
                    dictionaryFieldClass = Int::class.javaObjectType,
                    dictionaryFieldName = "columnIntegerDictionary",
                    dictionaryGetter = DictionaryAllTypes::getColumnIntegerDictionary,
                    dictionarySetter = DictionaryAllTypes::setColumnIntegerDictionary,
                    requiredDictionaryGetter = DictionaryAllTypes::getColumnRequiredIntegerDictionary,
                    initializedDictionary = RealmDictionary<Int>().init(listOf(KEY_HELLO to VALUE_NUMERIC_HELLO, KEY_BYE to VALUE_NUMERIC_BYE, KEY_NULL to null)),
                    alternativeDictionary = RealmDictionary<Int>().init(listOf(KEY_HELLO to VALUE_NUMERIC_BYE, KEY_BYE to VALUE_NUMERIC_HELLO, KEY_NULL to null)),
                    notPresentValue = VALUE_NUMERIC_NOT_PRESENT,
                    populatedGetter = PopulatedDictionaryClass::populatedIntDictionary,
                    primaryKeyDictionaryProperty = PrimaryKeyDictionaryContainer::myIntDictionary
            ),
            ManagedDictionaryTester(
                    testerClass = "Short",
                    dictionaryFieldClass = Short::class.javaObjectType,
                    dictionaryFieldName = "columnShortDictionary",
                    dictionaryGetter = DictionaryAllTypes::getColumnShortDictionary,
                    dictionarySetter = DictionaryAllTypes::setColumnShortDictionary,
                    requiredDictionaryGetter = DictionaryAllTypes::getColumnRequiredShortDictionary,
                    initializedDictionary = RealmDictionary<Short>().init(listOf(KEY_HELLO to VALUE_NUMERIC_HELLO.toShort(), KEY_BYE to VALUE_NUMERIC_BYE.toShort(), KEY_NULL to null)),
                    alternativeDictionary = RealmDictionary<Short>().init(listOf(KEY_HELLO to VALUE_NUMERIC_BYE.toShort(), KEY_BYE to VALUE_NUMERIC_HELLO.toShort(), KEY_NULL to null)),
                    notPresentValue = VALUE_NUMERIC_NOT_PRESENT.toShort(),
                    populatedGetter = PopulatedDictionaryClass::populatedShortDictionary,
                    primaryKeyDictionaryProperty = PrimaryKeyDictionaryContainer::myShortDictionary
            ),
            ManagedDictionaryTester(
                    testerClass = "Byte",
                    dictionaryFieldClass = Byte::class.javaObjectType,
                    dictionaryFieldName = "columnByteDictionary",
                    dictionaryGetter = DictionaryAllTypes::getColumnByteDictionary,
                    dictionarySetter = DictionaryAllTypes::setColumnByteDictionary,
                    requiredDictionaryGetter = DictionaryAllTypes::getColumnRequiredByteDictionary,
                    initializedDictionary = RealmDictionary<Byte>().init(listOf(KEY_HELLO to VALUE_NUMERIC_HELLO.toByte(), KEY_BYE to VALUE_NUMERIC_BYE.toByte(), KEY_NULL to null)),
                    alternativeDictionary = RealmDictionary<Byte>().init(listOf(KEY_HELLO to VALUE_NUMERIC_BYE.toByte(), KEY_BYE to VALUE_NUMERIC_HELLO.toByte(), KEY_NULL to null)),
                    notPresentValue = VALUE_NUMERIC_NOT_PRESENT.toByte(),
                    populatedGetter = PopulatedDictionaryClass::populatedByteDictionary,
                    primaryKeyDictionaryProperty = PrimaryKeyDictionaryContainer::myByteDictionary
            ),
            ManagedDictionaryTester(
                    testerClass = "Float",
                    dictionaryFieldClass = Float::class.javaObjectType,
                    dictionaryFieldName = "columnFloatDictionary",
                    dictionaryGetter = DictionaryAllTypes::getColumnFloatDictionary,
                    dictionarySetter = DictionaryAllTypes::setColumnFloatDictionary,
                    requiredDictionaryGetter = DictionaryAllTypes::getColumnRequiredFloatDictionary,
                    initializedDictionary = RealmDictionary<Float>().init(listOf(KEY_HELLO to VALUE_NUMERIC_HELLO.toFloat(), KEY_BYE to VALUE_NUMERIC_BYE.toFloat(), KEY_NULL to null)),
                    alternativeDictionary = RealmDictionary<Float>().init(listOf(KEY_HELLO to VALUE_NUMERIC_BYE.toFloat(), KEY_BYE to VALUE_NUMERIC_HELLO.toFloat(), KEY_NULL to null)),
                    notPresentValue = VALUE_NUMERIC_NOT_PRESENT.toFloat(),
                    populatedGetter = PopulatedDictionaryClass::populatedFloatDictionary,
                    primaryKeyDictionaryProperty = PrimaryKeyDictionaryContainer::myFloatDictionary
            ),
            ManagedDictionaryTester(
                    testerClass = "Double",
                    dictionaryFieldClass = Double::class.javaObjectType,
                    dictionaryFieldName = "columnDoubleDictionary",
                    dictionaryGetter = DictionaryAllTypes::getColumnDoubleDictionary,
                    dictionarySetter = DictionaryAllTypes::setColumnDoubleDictionary,
                    requiredDictionaryGetter = DictionaryAllTypes::getColumnRequiredDoubleDictionary,
                    initializedDictionary = RealmDictionary<Double>().init(listOf(KEY_HELLO to VALUE_NUMERIC_HELLO.toDouble(), KEY_BYE to VALUE_NUMERIC_BYE.toDouble(), KEY_NULL to null)),
                    alternativeDictionary = RealmDictionary<Double>().init(listOf(KEY_HELLO to VALUE_NUMERIC_BYE.toDouble(), KEY_BYE to VALUE_NUMERIC_HELLO.toDouble(), KEY_NULL to null)),
                    notPresentValue = VALUE_NUMERIC_NOT_PRESENT.toDouble(),
                    populatedGetter = PopulatedDictionaryClass::populatedDoubleDictionary,
                    primaryKeyDictionaryProperty = PrimaryKeyDictionaryContainer::myDoubleDictionary
            ),
            ManagedDictionaryTester(
                    testerClass = "String",
                    dictionaryFieldClass = String::class.java,
                    dictionaryFieldName = "columnStringDictionary",
                    dictionaryGetter = DictionaryAllTypes::getColumnStringDictionary,
                    dictionarySetter = DictionaryAllTypes::setColumnStringDictionary,
                    requiredDictionaryGetter = DictionaryAllTypes::getColumnRequiredStringDictionary,
                    initializedDictionary = RealmDictionary<String>().init(listOf(KEY_HELLO to VALUE_STRING_HELLO, KEY_BYE to VALUE_STRING_BYE, KEY_NULL to null)),
                    alternativeDictionary = RealmDictionary<String>().init(listOf(KEY_HELLO to VALUE_STRING_BYE, KEY_BYE to VALUE_STRING_HELLO, KEY_NULL to null)),
                    notPresentValue = VALUE_STRING_NOT_PRESENT,
                    populatedGetter = PopulatedDictionaryClass::populatedStringDictionary,
                    primaryKeyDictionaryProperty = PrimaryKeyDictionaryContainer::myStringDictionary
            ),
            ManagedDictionaryTester(
                    testerClass = "String-NonLatin",
                    dictionaryFieldClass = String::class.java,
                    dictionaryFieldName = "columnStringDictionary",
                    dictionaryGetter = DictionaryAllTypes::getColumnStringDictionary,
                    dictionarySetter = DictionaryAllTypes::setColumnStringDictionary,
                    requiredDictionaryGetter = DictionaryAllTypes::getColumnRequiredStringDictionary,
                    initializedDictionary = RealmDictionary<String>().init(listOf(KEY_HELLO_NON_LATIN to VALUE_STRING_NON_LATIN_HELLO, KEY_BYE_NON_LATIN to VALUE_STRING_NON_LATIN_BYE, KEY_NULL_NON_LATIN to null)),
                    alternativeDictionary = RealmDictionary<String>().init(listOf(KEY_HELLO_NON_LATIN to VALUE_STRING_NON_LATIN_BYE, KEY_BYE_NON_LATIN to VALUE_STRING_NON_LATIN_HELLO, KEY_NULL_NON_LATIN to null)),
                    notPresentValue = VALUE_STRING_NON_LATIN_NOT_PRESENT,
                    populatedGetter = PopulatedDictionaryClass::populatedStringDictionary,
                    primaryKeyDictionaryProperty = PrimaryKeyDictionaryContainer::myStringDictionary
            ),
            ManagedDictionaryTester(
                    testerClass = "Boolean",
                    dictionaryFieldClass = Boolean::class.javaObjectType,
                    dictionaryFieldName = "columnBooleanDictionary",
                    dictionaryGetter = DictionaryAllTypes::getColumnBooleanDictionary,
                    dictionarySetter = DictionaryAllTypes::setColumnBooleanDictionary,
                    requiredDictionaryGetter = DictionaryAllTypes::getColumnRequiredBooleanDictionary,
                    initializedDictionary = RealmDictionary<Boolean>().init(listOf(KEY_HELLO to VALUE_BOOLEAN_HELLO, KEY_BYE to VALUE_BOOLEAN_BYE, KEY_NULL to null)),
                    alternativeDictionary = RealmDictionary<Boolean>().init(listOf(KEY_HELLO to VALUE_BOOLEAN_BYE, KEY_BYE to VALUE_BOOLEAN_HELLO, KEY_NULL to null)),
                    notPresentValue = VALUE_BOOLEAN_NOT_PRESENT,
                    populatedGetter = PopulatedDictionaryClass::populatedBooleanDictionary,
                    primaryKeyDictionaryProperty = PrimaryKeyDictionaryContainer::myBooleanDictionary
            ),
            ManagedDictionaryTester(
                    testerClass = "Date",
                    dictionaryFieldClass = Date::class.java,
                    dictionaryFieldName = "columnDateDictionary",
                    dictionaryGetter = DictionaryAllTypes::getColumnDateDictionary,
                    dictionarySetter = DictionaryAllTypes::setColumnDateDictionary,
                    requiredDictionaryGetter = DictionaryAllTypes::getColumnRequiredDateDictionary,
                    initializedDictionary = RealmDictionary<Date>().init(listOf(KEY_HELLO to VALUE_DATE_HELLO, KEY_BYE to VALUE_DATE_BYE, KEY_NULL to null)),
                    alternativeDictionary = RealmDictionary<Date>().init(listOf(KEY_HELLO to VALUE_DATE_BYE, KEY_BYE to VALUE_DATE_HELLO, KEY_NULL to null)),
                    notPresentValue = VALUE_DATE_NOT_PRESENT,
                    populatedGetter = PopulatedDictionaryClass::populatedDateDictionary,
                    primaryKeyDictionaryProperty = PrimaryKeyDictionaryContainer::myDateDictionary
            ),
            ManagedDictionaryTester(
                    testerClass = "Decimal128",
                    dictionaryFieldClass = Decimal128::class.java,
                    dictionaryFieldName = "columnDecimal128Dictionary",
                    dictionaryGetter = DictionaryAllTypes::getColumnDecimal128Dictionary,
                    dictionarySetter = DictionaryAllTypes::setColumnDecimal128Dictionary,
                    requiredDictionaryGetter = DictionaryAllTypes::getColumnRequiredDecimal128Dictionary,
                    initializedDictionary = RealmDictionary<Decimal128>().init(listOf(KEY_HELLO to VALUE_DECIMAL128_HELLO, KEY_BYE to VALUE_DECIMAL128_BYE, KEY_NULL to null)),
                    alternativeDictionary = RealmDictionary<Decimal128>().init(listOf(KEY_HELLO to VALUE_DECIMAL128_BYE, KEY_BYE to VALUE_DECIMAL128_HELLO, KEY_NULL to null)),
                    notPresentValue = VALUE_DECIMAL128_NOT_PRESENT,
                    populatedGetter = PopulatedDictionaryClass::populatedDecimal128Dictionary,
                    primaryKeyDictionaryProperty = PrimaryKeyDictionaryContainer::myDecimal128Dictionary
            ),
            ManagedDictionaryTester(
                    testerClass = "ByteArray",
                    dictionaryFieldClass = ByteArray::class.java,
                    dictionaryFieldName = "columnBinaryDictionary",
                    dictionaryGetter = DictionaryAllTypes::getColumnBinaryDictionary,
                    dictionarySetter = DictionaryAllTypes::setColumnBinaryDictionary,
                    requiredDictionaryGetter = DictionaryAllTypes::getColumnRequiredBinaryDictionary,
                    initializedDictionary = RealmDictionary<ByteArray>().init(listOf(KEY_HELLO to VALUE_BINARY_HELLO, KEY_BYE to VALUE_BINARY_BYE, KEY_NULL to null)),
                    alternativeDictionary = RealmDictionary<ByteArray>().init(listOf(KEY_HELLO to VALUE_BINARY_BYE, KEY_BYE to VALUE_BINARY_HELLO, KEY_NULL to null)),
                    notPresentValue = VALUE_BINARY_NOT_PRESENT,
                    populatedGetter = PopulatedDictionaryClass::populatedBinaryDictionary,
                    primaryKeyDictionaryProperty = PrimaryKeyDictionaryContainer::myBinaryDictionary,
                    typeAsserter = BinaryAsserter()
            ),
            ManagedDictionaryTester(
                    testerClass = "ObjectId",
                    dictionaryFieldClass = ObjectId::class.java,
                    dictionaryFieldName = "columnObjectIdDictionary",
                    dictionaryGetter = DictionaryAllTypes::getColumnObjectIdDictionary,
                    dictionarySetter = DictionaryAllTypes::setColumnObjectIdDictionary,
                    requiredDictionaryGetter = DictionaryAllTypes::getColumnRequiredObjectIdDictionary,
                    initializedDictionary = RealmDictionary<ObjectId>().init(listOf(KEY_HELLO to VALUE_OBJECT_ID_HELLO, KEY_BYE to VALUE_OBJECT_ID_BYE, KEY_NULL to null)),
                    alternativeDictionary = RealmDictionary<ObjectId>().init(listOf(KEY_HELLO to VALUE_OBJECT_ID_BYE, KEY_BYE to VALUE_OBJECT_ID_HELLO, KEY_NULL to null)),
                    notPresentValue = VALUE_OBJECT_ID_NOT_PRESENT,
                    populatedGetter = PopulatedDictionaryClass::populatedObjectIdDictionary,
                    primaryKeyDictionaryProperty = PrimaryKeyDictionaryContainer::myObjectIdDictionary
            ),
            ManagedDictionaryTester(
                    testerClass = "UUID",
                    dictionaryFieldClass = UUID::class.java,
                    dictionaryFieldName = "columnUUIDDictionary",
                    dictionaryGetter = DictionaryAllTypes::getColumnUUIDDictionary,
                    dictionarySetter = DictionaryAllTypes::setColumnUUIDDictionary,
                    requiredDictionaryGetter = DictionaryAllTypes::getColumnRequiredUUIDDictionary,
                    initializedDictionary = RealmDictionary<UUID>().init(listOf(KEY_HELLO to VALUE_UUID_HELLO, KEY_BYE to VALUE_UUID_BYE, KEY_NULL to null)),
                    alternativeDictionary = RealmDictionary<UUID>().init(listOf(KEY_HELLO to VALUE_UUID_BYE, KEY_BYE to VALUE_UUID_HELLO, KEY_NULL to null)),
                    notPresentValue = VALUE_UUID_NOT_PRESENT,
                    populatedGetter = PopulatedDictionaryClass::populatedUUIDDictionary,
                    primaryKeyDictionaryProperty = PrimaryKeyDictionaryContainer::myUUIDDictionary
            ),
            ManagedDictionaryTester(
                    testerClass = "DogPrimaryKey",
                    dictionaryFieldClass = DogPrimaryKey::class.java,
                    dictionaryFieldName = "columnRealmDictionary",
                    dictionaryGetter = DictionaryAllTypes::getColumnRealmDictionary,
                    dictionarySetter = DictionaryAllTypes::setColumnRealmDictionary,
                    initializedDictionary = RealmDictionary<DogPrimaryKey>().init(listOf(KEY_HELLO to VALUE_LINK_HELLO, KEY_BYE to VALUE_LINK_BYE, KEY_NULL to null)),
                    alternativeDictionary = RealmDictionary<DogPrimaryKey>().init(listOf(KEY_HELLO to VALUE_LINK_BYE, KEY_BYE to VALUE_LINK_HELLO, KEY_NULL to null)),
                    notPresentValue = VALUE_LINK_NOT_PRESENT,
                    populatedGetter = PopulatedDictionaryClass::populatedRealmModelDictionary,
                    primaryKeyDictionaryProperty = PrimaryKeyDictionaryContainer::myRealmModelDictionary,
                    typeAsserter = RealmModelAsserter()
            )
    )

    // Create RealmAny testers now
    val realmAnyTesters = RealmAny.Type.values().map { realmAnyType ->
        ManagedDictionaryTester(
                testerClass = "RealmAny",
                realmAnyType = realmAnyType,
                dictionaryFieldClass = RealmAny::class.java,
                dictionaryFieldName = "columnRealmAnyDictionary",
                dictionaryGetter = DictionaryAllTypes::getColumnRealmAnyDictionary,
                dictionarySetter = DictionaryAllTypes::setColumnRealmAnyDictionary,
                initializedDictionary = RealmDictionary<RealmAny>().init(getRealmAnyKeyValuePairs(realmAnyType)),
                alternativeDictionary = RealmDictionary<RealmAny>().init(getRealmAnyKeyValuePairs(realmAnyType, true)),
                notPresentValue = VALUE_MIXED_NOT_PRESENT,
                populatedGetter = PopulatedDictionaryClass::populatedRealmAnyDictionary,
                primaryKeyDictionaryProperty = PrimaryKeyDictionaryContainer::myRealmAnyDictionary,
                typeAsserter = RealmAnyAsserter()
        )
    }.plus(
            ManagedDictionaryTester(
                    testerClass = "RealmAny-NonLatin",
                    realmAnyType = RealmAny.Type.STRING,
                    dictionaryFieldClass = RealmAny::class.java,
                    dictionaryFieldName = "columnRealmAnyDictionary",
                    dictionaryGetter = DictionaryAllTypes::getColumnRealmAnyDictionary,
                    dictionarySetter = DictionaryAllTypes::setColumnRealmAnyDictionary,
                    initializedDictionary = RealmDictionary<RealmAny>().init(listOf(KEY_HELLO_NON_LATIN to VALUE_MIXED_STRING_NON_LATIN_BYE, KEY_BYE_NON_LATIN to VALUE_MIXED_STRING_NON_LATIN_HELLO, KEY_NULL_NON_LATIN to null)),
                    alternativeDictionary = RealmDictionary<RealmAny>().init(listOf(KEY_HELLO_NON_LATIN to VALUE_MIXED_STRING_NON_LATIN_HELLO, KEY_BYE_NON_LATIN to VALUE_MIXED_STRING_NON_LATIN_BYE, KEY_NULL_NON_LATIN to null)),
                    notPresentValue = VALUE_MIXED_NOT_PRESENT,
                    populatedGetter = PopulatedDictionaryClass::populatedRealmAnyDictionary,
                    primaryKeyDictionaryProperty = PrimaryKeyDictionaryContainer::myRealmAnyDictionary,
                    typeAsserter = RealmAnyAsserter()
            )
    )

    return primitiveTesters.plus(realmAnyTesters)
}

/**
 * Helper to harmonize testing across different types.
 */
open class TypeAsserter<T> {

    // RealmModel and RealmAny require different testing here
    open fun assertContainsValueNotThere(
            realm: Realm,
            dictionary: RealmDictionary<T>,
            index: Int,
            value: T?
    ) {
        assertFalse(dictionary.containsValue(value))
    }

    // RealmModel and RealmAny require different testing here
    open fun assertRemoveRealmModelFromRealm(
            dictionary: RealmDictionary<T>,
            index: Int,
            key: String,
            value: T?
    ) = Unit    // Do nothing if we aren't testing a RealmModel or a RealmAny wrapping a RealmModel

    // RealmModel requires different testing here
    open fun assertValues(dictionary: RealmDictionary<T>, value: T?) =
            assertTrue(dictionary.containsValue(value))

    open fun assertKeys(dictionary: RealmDictionary<*>, value: String?) =
        assertTrue(dictionary.containsKey(value))

    fun assertDynamicValues(dictionary: RealmDictionary<DynamicRealmObject>, value: DynamicRealmObject?) {
        // null entries become "invalid object" when calling dictionary.values()
        assertNotNull(value)
        if (value.isValid) {
            assertTrue(dictionary.containsValue(value))
        } else {
            assertTrue(dictionary.containsValue(null))
        }
    }

    // RealmModel and RealmAny require different testing here
    open fun assertContainsValueHelper(
            realm: Realm,
            key: String,
            value: T?,
            unmanagedDictionary: RealmDictionary<T>,
            managedDictionary: RealmDictionary<T>
    ) {
        assertTrue(managedDictionary.containsValue(unmanagedDictionary[key]))
    }

    // ByteArray, RealmModel and RealmAny require different testing here
    open fun assertEqualsHelper(realm: Realm, value: T?, valueFromRealm: T?) =
            assertEquals(value, valueFromRealm)

    // RealmModel requires different testing here
    open fun assertAccessorSetter(
        realm: Realm,
        dictionaryGetter: KFunction1<DictionaryAllTypes, RealmDictionary<T>>,
        dictionarySetter: KFunction2<DictionaryAllTypes, RealmDictionary<T>, Unit>,
        initializedDictionary: RealmDictionary<T>,
        otherConfig: RealmConfiguration
    ) {
        realm.executeTransaction { transactionRealm ->
            val anotherContainer = transactionRealm.createObject<DictionaryAllTypes>()
            dictionarySetter.call(anotherContainer, initializedDictionary)
            val dictionary = dictionaryGetter.call(anotherContainer)
            assertNotNull(dictionary)
            assertTrue(dictionary.isManaged)
            assertEquals(initializedDictionary.size, dictionary.size)
        }
    }

    fun assertChangeListenerUpdates(
            testerClass: String,
            operation: ChangeListenerOperation,
            looperThread: BlockingLooperThread,
            looperThreadRealm: Realm,
            managedDictionary: RealmDictionary<T>,
            initializedDictionary: RealmDictionary<T>,
            mapFromChangeListener: RealmMap<String, T>,
            changes: MapChangeSet<String>? = null
    ) {
        when (operation) {
            ChangeListenerOperation.INSERT -> {
                // Check dictionary
                initializedDictionary.forEach { key, _ ->
                    assertContainsValueHelper(
                            looperThreadRealm,
                            key,
                            mapFromChangeListener[key],
                            initializedDictionary,
                            mapFromChangeListener as RealmDictionary<T>
                    )
                    assertTrue(mapFromChangeListener.containsKey(key))

                    if (changes != null) {
                        // Check insertions changeset contains keys
                        assertTrue(changes.insertions.contains(key))
                    }
                }
            }
            ChangeListenerOperation.UPDATE -> {
                if (testerClass == "String-NonLatin" || testerClass == "RealmAny-NonLatin") {
                    assertEqualsHelper(
                            looperThreadRealm,
                            initializedDictionary[KEY_BYE_NON_LATIN],
                            mapFromChangeListener[KEY_HELLO_NON_LATIN]
                    )
                } else {
                    assertEqualsHelper(
                            looperThreadRealm,
                            initializedDictionary[KEY_BYE],
                            mapFromChangeListener[KEY_HELLO]
                    )
                }
                if (changes != null) {
                    assertEquals(1, changes.changes.size)
                }
            }
            ChangeListenerOperation.DELETE -> {
                // Dictionary has been cleared
                assertTrue(mapFromChangeListener.isEmpty())
                assertEquals(0, mapFromChangeListener.size)

                if (changes != null) {
                    // Check deletions changeset size matches deleted elements
                    assertEquals(initializedDictionary.size, changes.deletionsCount.toInt())
                }

                // Housekeeping and bye-bye
                managedDictionary.removeAllChangeListeners()
                looperThreadRealm.close()
                looperThread.testComplete()
            }
            ChangeListenerOperation.UNDEFINED ->
                throw IllegalArgumentException("Operation cannot be default")
        }
    }
}

class BinaryAsserter : TypeAsserter<ByteArray>() {

    override fun assertEqualsHelper(realm: Realm, value: ByteArray?, valueFromRealm: ByteArray?) {
        if (value == null && valueFromRealm == null) {
            return
        }

        assertNotNull(value)
        assertNotNull(valueFromRealm)

        // ByteArrays need to be compared with Arrays.equals
        assertTrue(value.contentEquals(valueFromRealm))
    }
}

class RealmModelAsserter : TypeAsserter<DogPrimaryKey>() {

    override fun assertContainsValueNotThere(
            realm: Realm,
            dictionary: RealmDictionary<DogPrimaryKey>,
            index: Int,
            value: DogPrimaryKey?
    ) {
        // Given that only managed objects can be contained in a managed RealmModel dictionary, we
        // need to test containsValue with a dummy model
        realm.executeTransaction { transactionRealm ->
            val dummyRealmModel = transactionRealm.copyToRealm(DogPrimaryKey(666 + index.toLong(), "DUMMY"))
            assertFalse(dictionary.containsValue(dummyRealmModel as DogPrimaryKey))
        }
    }

    override fun assertRemoveRealmModelFromRealm(
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
                .equalTo("name", value?.name)
                .findFirst()
        if (value == null && valueFromRealm == null) {
            assertEquals(modelFromRealm, valueFromRealm)
            return
        }

        assertNotNull(modelFromRealm)
        assertEquals(modelFromRealm, valueFromRealm)
    }

    override fun assertAccessorSetter(
        realm: Realm,
        dictionaryGetter: KFunction1<DictionaryAllTypes, RealmDictionary<DogPrimaryKey>>,
        dictionarySetter: KFunction2<DictionaryAllTypes, RealmDictionary<DogPrimaryKey>, Unit>,
        initializedDictionary: RealmDictionary<DogPrimaryKey>,
        otherConfig: RealmConfiguration
    ) {
        realm.executeTransaction { transactionRealm ->
            // Setter fails when calling with a dictionary that contains unmanaged objects
            // The throwable is an IllegalArgumentException wrapped inside an InvocationTargetException
            // due to calling 'call' on the KFunction2
            val anotherContainer = transactionRealm.createObject<DictionaryAllTypes>()
            assertFailsWith<InvocationTargetException> {
                dictionarySetter.call(anotherContainer, initializedDictionary)
            }.let { e ->
                assertTrue {
                    e.targetException is IllegalArgumentException
                }
            }
        }

        // Setter fails when calling with a dictionary containing managed objects from another Realm
        var otherRealmDictionary: RealmDictionary<DogPrimaryKey>? = null
        val otherRealm = Realm.getInstance(otherConfig)
        otherRealm.executeTransaction { transactionRealm ->
            val otherRealmContainer = transactionRealm.createObject<DictionaryAllTypes>()
            otherRealmDictionary = dictionaryGetter.call(otherRealmContainer)
                .apply { this.putAll(initializedDictionary) }
        }

        realm.executeTransaction { transactionRealm ->
            val anotherContainer = transactionRealm.createObject<DictionaryAllTypes>()

            // The throwable is an IllegalArgumentException wrapped inside an InvocationTargetException
            // due to calling 'call' on the KFunction2
            assertFailsWith<InvocationTargetException> {
                dictionarySetter.call(anotherContainer, otherRealmDictionary)
            }.let { e ->
                assertTrue {
                    e.targetException is IllegalArgumentException
                }
            }
        }

        // Remember to close the other Realm!
        otherRealm.close()
    }
}

class RealmAnyAsserter : TypeAsserter<RealmAny>() {

    override fun assertContainsValueNotThere(
            realm: Realm,
            dictionary: RealmDictionary<RealmAny>,
            index: Int,
            value: RealmAny?
    ) {
        if (value?.valueClass == DogPrimaryKey::class.java) {
            // Similar to RealmModelAsserter
            realm.executeTransaction { transactionRealm ->
                val dummyRealmModel = transactionRealm.copyToRealm(DogPrimaryKey(666 + index.toLong(), "DUMMY"))
                val realmAnyWithManagedModel = RealmAny.valueOf(dummyRealmModel)
                assertFalse(dictionary.containsValue(realmAnyWithManagedModel as RealmAny))
            }
        } else {
            assertFalse(dictionary.containsValue(value))
        }
    }

    override fun assertRemoveRealmModelFromRealm(
            dictionary: RealmDictionary<RealmAny>,
            index: Int,
            key: String,
            value: RealmAny?
    ) {
        // No need to check anything for other types than RealmModel
        if (value is RealmAny && value.valueClass == DogPrimaryKey::class.java) {
            // Removal of actual RealmModel to check whether it vanished from the dictionary
            // Insert again - "value" is unmanaged
            dictionary[key] = value

            // Delete from realm and check we get null if we get it from the dictionary
            val realmAnyValue = dictionary[key] as RealmAny
            val modelFromRealm = realmAnyValue.asRealmModel(DogPrimaryKey::class.java)
            assertTrue(modelFromRealm.isValid)

            modelFromRealm.deleteFromRealm()
            assertFalse(modelFromRealm.isValid)

            assertTrue((dictionary[key] as RealmAny).isNull)

            // Check size again (despite object removal, size should remain unchanged)
            assertEquals(index + 1, dictionary.size)

            // Delete it again so that the forEach size check works
            dictionary.remove(key)
            assertEquals(index, dictionary.size)
        }
    }

    override fun assertContainsValueHelper(
            realm: Realm,
            key: String,
            value: RealmAny?,
            unmanagedDictionary: RealmDictionary<RealmAny>,
            managedDictionary: RealmDictionary<RealmAny>
    ) {
        // We can never get null RealmAny values from a managed dictionary
        assertNotNull(value)

        if (value.isNull) {
            // If null, check we have "RealmAny.nullValue()"
            assertTrue(managedDictionary.containsValue(RealmAny.nullValue()))
        } else if (value.valueClass == DogPrimaryKey::class.java) {
            // If RealmModel, check dictionary contains a RealmAny containing the managed model
            val managedRealmDog = realm.where<DogPrimaryKey>()
                    .equalTo("name", value.asRealmModel(DogPrimaryKey::class.java).name)
                    .findFirst()
            val realmAnyWithManagedDog = RealmAny.valueOf(managedRealmDog)
            assertTrue(managedDictionary.containsValue(realmAnyWithManagedDog))
        } else {
            assertTrue(managedDictionary.containsValue(managedDictionary[key]))
        }
    }

    override fun assertEqualsHelper(realm: Realm, value: RealmAny?, valueFromRealm: RealmAny?) {
        // If null, check we have "RealmAny.nullValue()"
        if (null == value) {
            assertNotNull(valueFromRealm)
            assertTrue(valueFromRealm.isNull)
        } else if (value.valueClass == DogPrimaryKey::class.java) {
            // If RealmModel, check provided the RealmAny equals a RealmAny containing the managed model
            val managedRealmModel = realm.where<DogPrimaryKey>()
                    .equalTo("name", (value as RealmAny).asRealmModel(DogPrimaryKey::class.java).name)
                    .findFirst()
            val realmAnyWithManagedModel = RealmAny.valueOf(managedRealmModel)
            assertEquals(valueFromRealm, realmAnyWithManagedModel)
        } else {
            assertEquals(value, valueFromRealm)
        }
    }
}
