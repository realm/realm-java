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

import io.realm.entities.*
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import io.realm.rule.BlockingLooperThread
import org.bson.types.Decimal128
import org.bson.types.ObjectId
import java.lang.UnsupportedOperationException
import java.util.*
import kotlin.collections.HashSet
import kotlin.reflect.KFunction1
import kotlin.reflect.KFunction2
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1
import kotlin.test.*

/**
 * Generic tester for all types of unmanaged sets.
 */
class ManagedSetTester<T : Any>(
        private val testerName: String,
        private val setFieldName: String,
        private val setFieldClass: Class<T>,
        private val realmAnyType: RealmAny.Type? = null,
        private val setGetter: KFunction1<SetAllTypes, RealmSet<T>>,
        private val setSetter: KFunction2<SetAllTypes, RealmSet<T>, Unit>,
        private val requiredSetGetter: KFunction1<SetAllTypes, RealmSet<T>>? = null,
        private val managedSetGetter: KProperty1<SetContainerClass, RealmSet<T>>,
        private val managedCollectionGetter: KProperty1<SetContainerClass, RealmList<T>>,
        private val initializedSet: List<T?>,
        private val notPresentValue: T,
        private val toArrayManaged: ToArrayManaged<T>,
        private val nullable: Boolean = true,
        private val equalsTo: (expected: T?, value: T?) -> Boolean = { expected, value ->
            // Used to assert that the contents of two collections are the same.
           expected == value
        },
        private val primaryKeyAllTypesSetProperty: KMutableProperty1<SetAllTypesPrimaryKey, RealmSet<T>>
) : SetTester {

    private lateinit var config: RealmConfiguration
    private lateinit var looperThread: BlockingLooperThread
    private lateinit var realm: Realm

    override fun toString(): String = "ManagedSet-${testerName}"

    override fun setUp(config: RealmConfiguration, looperThread: BlockingLooperThread) {
        this.config = config
        this.looperThread = looperThread
        this.realm = Realm.getInstance(config)
    }

    override fun tearDown() = realm.close()

    override fun isManaged() = assertTrue(initAndAssertEmptySet(id = "id").isManaged)

    override fun isValid() = assertTrue(initAndAssertEmptySet(id = "id").isValid)

    override fun isFrozen() = Unit          // Tested in frozen

    override fun size() {
        val set = initAndAssertEmptySet(id = "id")

        realm.executeTransaction {
            initializedSet.forEach { value ->
                set.add(value)
            }
        }
        assertEquals(initializedSet.size, set.size)
    }

    override fun isEmpty() {
        val set = initAndAssertEmptySet(id = "id")

        realm.executeTransaction {
            initializedSet.forEach { value ->
                set.add(value)
            }
        }
        assertFalse(set.isEmpty())
    }

    override fun contains() {
        val set = initAndAssertEmptySet(id = "id")

        initializedSet.forEach { value ->
            assertFalse(set.contains(value))
        }
        realm.executeTransaction {
            set.addAll(initializedSet)
        }
        initializedSet.forEach { value ->
            assertTrue(set.contains(value))
        }
        assertFalse(set.contains(notPresentValue))

        // Throws if we call contains with something entirely different
        val somethingEntirelyDifferent = initializedSet.map {
            Pair(it, it)
        }
        assertFailsWith<ClassCastException> {
            set.contains<Any>(somethingEntirelyDifferent)
        }
    }

    override fun iterator() {
        val set = initAndAssertEmptySet(id = "id")

        assertNotNull(set.iterator())
        realm.executeTransaction {
            set.addAll(initializedSet)
        }

        initializedSet.forEach { value ->
            assertTrue(set.contains(value))
        }
    }

    override fun toArray() {
        val set = initAndAssertEmptySet(id = "id")

        // Empty set
        assertEquals(0, set.toArray().size)

        // Set with some values
        realm.executeTransaction {
            // Empty set
            assertEquals(0, set.toArray().size)

            set.addAll(initializedSet)
            val setToArray = set.toArray()
            assertNotNull(setToArray)
            assertEquals(initializedSet.size, setToArray.size)

            val sameValuesUnmanagedSetToArray = RealmSet<T>().apply {
                addAll(initializedSet)
            }.toArray()

            setToArray.contentEquals(sameValuesUnmanagedSetToArray)
        }
    }

    override fun toArrayWithParameter() {
        val set = initAndAssertEmptySet(id = "id")
        toArrayManaged.assertToArrayWithParameter(realm, set, initializedSet)
    }

    override fun add() {
        val set = initAndAssertEmptySet(id = "id")

        realm.executeTransaction {
            // Adding a value for the first time returns true
            initializedSet.forEach { value ->
                assertTrue(set.add(value))
            }
            // Adding an existing value returns false
            initializedSet.forEach { value ->
                assertFalse(set.add(value))
            }
        }

        assertTrue(set.containsAll(initializedSet))
    }

    override fun remove() {
        val set = initAndAssertEmptySet(id = "id")

        realm.executeTransaction {
            set.addAll(initializedSet)
            initializedSet.forEach { value ->
                assertTrue(set.remove(value))
            }
            assertTrue(set.isEmpty())

            // Does not change if we remove something that is not there
            assertFalse(set.remove(notPresentValue))

            // Throws if we remove an object that is not the same type as the set
            val somethingEntirelyDifferent = initializedSet.map {
                Pair(it, it)
            }
            assertFailsWith<ClassCastException> {
                set.remove<Any>(somethingEntirelyDifferent)
            }

            if (nullable) {
                // Does not change if we remove null and null is not present
                assertFalse(set.remove(null))
            } else {
                assertFailsWith<java.lang.NullPointerException>("Set does not support null values") {
                    set.remove(null)
                }
            }
        }

        assertEquals(0, set.size)
    }

    override fun containsAll() {
        val set = initAndAssertEmptySet(id = "id")

        realm.executeTransaction { transactionRealm ->
            set.addAll(initializedSet)

            // Contains an unmanaged collection
            assertTrue(set.containsAll(initializedSet))

            // Does not contain an unmanaged collection
            assertFalse(set.containsAll(listOf(notPresentValue)))

            // Contains a managed set (itself)
            assertTrue(set.containsAll(set))

            // Contains an empty collection - every set contains the empty set
            assertTrue(set.containsAll(listOf()))

            // Throws when passing a collection of a different type
            val collectionOfDifferentType = initializedSet.map {
                Pair(it, it)
            }
            assertFailsWith<java.lang.ClassCastException> {
                set.containsAll(collectionOfDifferentType as Collection<*>)
            }

            // Contains a managed set containing the same values
            val sameValuesManagedSet = managedSetGetter.get(transactionRealm.createObject())
            sameValuesManagedSet.addAll(initializedSet)
            assertEquals(initializedSet.size, sameValuesManagedSet.size)
            assertTrue(set.containsAll(sameValuesManagedSet as Collection<*>))

            // Does not contain a managed set with other values
            val notPresentValueSet = managedSetGetter.get(transactionRealm.createObject())
            notPresentValueSet.add(notPresentValue)
            assertFalse(notPresentValueSet.isEmpty())
            assertFalse(set.containsAll(notPresentValueSet as Collection<*>))

            // Contains an empty RealmSet
            val emptyManagedSet = managedSetGetter.get(transactionRealm.createObject())
            assertTrue(emptyManagedSet.isEmpty())
            assertTrue(set.containsAll(emptyManagedSet))

            // Contains a managed list with the same elements
            val sameValuesManagedList = managedCollectionGetter.call(transactionRealm.createObject<SetContainerClass>())
            sameValuesManagedList.addAll(initializedSet)
            assertTrue(set.containsAll(sameValuesManagedList))

            // Does not contain a managed list with the other elements
            val differentValuesManagedList =
                managedCollectionGetter.call(transactionRealm.createObject<SetContainerClass>())
            differentValuesManagedList.add(notPresentValue)
            assertFalse(set.containsAll(differentValuesManagedList))

            // Contains an empty managed list
            val emptyValuesManagedList =
                managedCollectionGetter.call(transactionRealm.createObject<SetContainerClass>())
            assertTrue(set.containsAll(emptyValuesManagedList))

            // TODO: it's not possible to test passing a null value from Kotlin, even if using
            //  TestHelper.getNull(). It seems that Kotlin generates different bytecode when the
            //  parameter to the function is a generics collection with an upper bound.
            //  The only way to test this is by writing a Java test instead.
        }
    }

    override fun addAll() {
        val set = initAndAssertEmptySet(id = "id")

        realm.executeTransaction { transactionRealm ->
            // Changes after adding collection
            assertTrue(set.addAll(initializedSet))
            assertEquals(initializedSet.size, set.size)

            // Does not change if we add the same data
            assertFalse(set.addAll(initializedSet))
            assertEquals(initializedSet.size, set.size)

            // Does not change if we add itself to it
            assertFalse(set.addAll(set))
            assertEquals(initializedSet.size, set.size)

            // Does not change if we add an empty collection
            assertFalse(set.addAll(listOf()))
            assertEquals(initializedSet.size, set.size)

            // Throws when adding a collection of a different type
            val somethingEntirelyDifferent = initializedSet.map {
                Pair(it, it)
            }
            assertFailsWith<ClassCastException> {
                set.addAll(somethingEntirelyDifferent as Collection<T>)
            }

            // Does not change if we add the same data from a managed set
            val sameValuesManagedSet = managedSetGetter.get(transactionRealm.createObject())
            assertNotNull(sameValuesManagedSet)
            assertTrue(sameValuesManagedSet.addAll(initializedSet))
            assertFalse(set.addAll(sameValuesManagedSet as Collection<T>))
            assertEquals(initializedSet.size, set.size)

            // Does not change if we add an empty RealmSet
            val emptyManagedSet = managedSetGetter.get(transactionRealm.createObject())
            assertTrue(emptyManagedSet.isEmpty())
            assertFalse(set.addAll(emptyManagedSet))
            assertEquals(initializedSet.size, set.size)

            // Changes after adding a managed set containing other values
            val notPresentValueSet = managedSetGetter.get(transactionRealm.createObject())
            assertNotNull(notPresentValueSet)
            notPresentValueSet.add(notPresentValue)
            assertTrue(set.addAll(notPresentValueSet as Collection<T>))
            assertEquals(initializedSet.size + notPresentValueSet.size, set.size)

            // Does not change after adding a managed list with the same elements
            set.clear()
            set.addAll(initializedSet)
            val sameValuesManagedList = managedCollectionGetter.call(transactionRealm.createObject<SetContainerClass>())
            sameValuesManagedList.addAll(initializedSet)
            assertFalse(set.addAll(sameValuesManagedList))
            assertTrue(set.containsAll(sameValuesManagedList))

            // Changes after adding a managed list with other elements
            val differentValuesManagedList =
                managedCollectionGetter.call(transactionRealm.createObject<SetContainerClass>())
            differentValuesManagedList.addAll(listOf(notPresentValue))
            assertTrue(set.addAll(differentValuesManagedList))
            assertTrue(set.containsAll(differentValuesManagedList))

            // Does not change after adding an empty managed list
            set.clear()
            assertTrue(set.addAll(initializedSet))
            val emptyValuesManagedList =
                managedCollectionGetter.call(transactionRealm.createObject<SetContainerClass>())
            assertFalse(set.addAll(emptyValuesManagedList))
            assertEquals(initializedSet.size, set.size)

            // Fails if passed null according to Java Set interface
            assertFailsWith<NullPointerException> {
                set.addAll(TestHelper.getNull())
            }
        }
    }

    override fun dynamic() {
        // Create a set from a immutable schema context
        val set = initAndAssertEmptySet(id = "id")
        realm.executeTransaction {
            set.addAll(initializedSet)
        }

        val dynamicRealm = DynamicRealm.getInstance(realm.configuration)
        val dynamicObject: DynamicRealmObject = dynamicRealm.where(SetAllTypes.NAME).equalTo(AllTypes.FIELD_STRING, "id").findFirst()!!
        val dynamicSet = dynamicObject.getSet(setFieldName, setFieldClass)

        // Access the previous set from a mutable context
        assertSetContainsSet(initializedSet, dynamicSet)

        // Update the set with a new value
        dynamicRealm.executeTransaction {
            dynamicSet.add(notPresentValue)
        }

        assertSetContainsSet(initializedSet.plus(notPresentValue), dynamicSet)

        // Try to replace the whole set by a new one
        dynamicRealm.executeTransaction {
            dynamicObject.setSet(setFieldName, RealmSet<T>().apply {
                add(notPresentValue)
            })
        }

        assertSetContainsSet(listOf(notPresentValue), dynamicSet)
        assertEquals(1, dynamicObject.getSet(setFieldName, setFieldClass).size)

        dynamicRealm.close()
    }

    override fun insert() {
        doInsertTest(initializedSet)
    }

    // Separate method to allow calls from RealmModelSetManagedTester with unmanaged realm objects
    fun doInsertTest(expectedSet: List<T?>) {
        // Instantiate container and set Set on container
        val manualInstance = SetAllTypes().apply {
            setSetter.call(this, RealmSet<T>().init(expectedSet))
        }

        // Insert into Realm
        realm.executeTransaction {
            realm.insert(manualInstance)
        }

        // Get set from container from Realm
        val allTypesObject = realm.where<SetAllTypes>().findFirst()
        assertNotNull(allTypesObject)
        val set: RealmSet<T> = setGetter.call(allTypesObject)

        assertFalse(set.isEmpty())
        assertSetContainsSet(expectedSet, set)
    }

    override fun insertList() {
        doInsertListTest(initializedSet)
    }

    // Separate method to allow calls from RealmModelSetManagedTester with unmanaged realm objects
    fun doInsertListTest(expectedSet: List<T?>) {
        // Instantiate container and set Set on container
        val manualInstance = SetAllTypes().apply {
            setSetter.call(this, RealmSet<T>().init(expectedSet))
        }

        val emptyInstace = SetAllTypes()

        // Insert into Realm
        realm.executeTransaction {
            realm.insert(listOf(emptyInstace, manualInstance))
        }

        // Get set from container from Realm
        val allTypesObject = realm.where<SetAllTypes>().findAll()[1]
        assertNotNull(allTypesObject)
        val set: RealmSet<T> = setGetter.call(allTypesObject)

        assertFalse(set.isEmpty())
        assertSetContainsSet(expectedSet, set)
    }

    override fun insertOrUpdate() {
        // Instantiate container and set Set on container
        val manualInstance = SetAllTypesPrimaryKey().apply {
            primaryKeyAllTypesSetProperty.setter(this, RealmSet<T>().init(initializedSet))
        }

        // Insert to Realm
        realm.executeTransaction {
            realm.insertOrUpdate(manualInstance)
        }

        // Get Set from container from Realm
        val allTypesPrimaryKey = realm.where<SetAllTypesPrimaryKey>().findFirst()!!
        val set = primaryKeyAllTypesSetProperty.get(allTypesPrimaryKey)
        assertFalse(set.isEmpty())

        assertSetContainsSet(initializedSet, set)

        primaryKeyAllTypesSetProperty.getter(manualInstance).add(notPresentValue)

        // Insert to Realm with non managed updated model
        realm.executeTransaction {
            realm.insertOrUpdate(manualInstance)
        }

        val updatedContainer = realm.where<SetAllTypesPrimaryKey>().findFirst()!!
        val updatedSet = primaryKeyAllTypesSetProperty.get(updatedContainer)
        assertEquals(initializedSet.size + 1, updatedSet.size)

        assertSetContainsSet(initializedSet.plus(notPresentValue), set)
    }

    override fun insertOrUpdateList() {
        // Instantiate container and set Set on container
        val manualInstance = SetAllTypesPrimaryKey().apply {
            columnLong = 0
            primaryKeyAllTypesSetProperty.setter(this, RealmSet<T>().init(initializedSet))
        }

        val emptyInstance = SetAllTypesPrimaryKey().apply {
            columnLong = 1
        }

        // Insert to Realm
        realm.executeTransaction {
            realm.insertOrUpdate(listOf(emptyInstance, manualInstance))
        }

        // Get Set from container from Realm
        val allTypesPrimaryKey = realm.where<SetAllTypesPrimaryKey>().equalTo("columnLong", 0.toLong()).findFirst()!!
        val set = primaryKeyAllTypesSetProperty.get(allTypesPrimaryKey)
        assertFalse(set.isEmpty())

        assertSetContainsSet(initializedSet, set)

        primaryKeyAllTypesSetProperty.getter(manualInstance).add(notPresentValue)

        // Insert to Realm with non managed updated model
        realm.executeTransaction {
            realm.insertOrUpdate(listOf(emptyInstance, manualInstance))
        }

        val updatedContainer = realm.where<SetAllTypesPrimaryKey>().findFirst()!!
        val updatedSet = primaryKeyAllTypesSetProperty.get(updatedContainer)
        assertEquals(initializedSet.size + 1, updatedSet.size)

        assertSetContainsSet(initializedSet.plus(notPresentValue), set)
    }

    override fun copyToRealm() {
        doCopyToRealmTest(initializedSet)
    }

    // Separate method to allow calls from RealmModelSetManagedTester with unmanaged realm objects
    fun doCopyToRealmTest(expectedSet: List<T?>) {
        // Instantiate container and set Set on container
        val manualInstance = SetAllTypes().apply {
            setSetter.call(this, RealmSet<T>().init(expectedSet))
        }

        // Copy to Realm
        realm.executeTransaction {
            val allTypesObject = realm.copyToRealm(manualInstance)
            assertNotNull(allTypesObject)
        }

        // Get set from container from Realm
        val allTypesObject = realm.where<SetAllTypes>().findFirst()
        assertNotNull(allTypesObject)
        val set: RealmSet<T> = setGetter.call(allTypesObject)

        assertFalse(set.isEmpty())
        assertSetContainsSet(expectedSet, set)
    }

    private fun assertSetContainsSet(expectedSet: List<T?>, set: RealmSet<T>) {
        set.forEach loop@{ value ->
            expectedSet.forEach { expected ->
                if (equalsTo(expected, value)) {
                    return@loop
                }
            }
            fail("Missing value")
        }
    }

    override fun copyToRealmOrUpdate() {
        // Instantiate container and set Set on container
        val manualInstance = SetAllTypesPrimaryKey().apply {
            primaryKeyAllTypesSetProperty.setter(this, RealmSet<T>().init(initializedSet))
        }

        // Copy to Realm
        realm.executeTransaction {
            val allTypesObject = realm.copyToRealmOrUpdate(manualInstance)
            assertNotNull(allTypesObject)
        }

        // Get Set from container from Realm
        val allTypesPrimaryKey = realm.where<SetAllTypesPrimaryKey>().findFirst()!!
        val set = primaryKeyAllTypesSetProperty.get(allTypesPrimaryKey)
        assertFalse(set.isEmpty())

        assertSetContainsSet(initializedSet, set)

        primaryKeyAllTypesSetProperty.getter(manualInstance).add(notPresentValue)

        // Copy to Realm with non managed updated model
        realm.executeTransaction {
            val allTypesObject = realm.copyToRealmOrUpdate(manualInstance)
            assertNotNull(allTypesObject)
        }

        val updatedContainer = realm.where<SetAllTypesPrimaryKey>().findFirst()!!
        val updatedSet = primaryKeyAllTypesSetProperty.get(updatedContainer)
        assertEquals(initializedSet.size + 1, updatedSet.size)

        assertSetContainsSet(initializedSet.plus(notPresentValue), set)
    }

    override fun requiredConstraints() {
        // RealmModel and RealmAny setters are ignored since they cannot be marked with "@Required"
        if (requiredSetGetter != null) {
            val allTypesObject = createAllTypesManagedContainerAndAssert(realm, "id")
            assertNotNull(allTypesObject)
            val set: RealmSet<T> = requiredSetGetter.call(allTypesObject)
            // Check we can't operate with nulls on a RealmSet marked as "@Required"
            realm.executeTransaction {
                // Validate we cannot use Null values on add
                assertFailsWith<java.lang.NullPointerException> {
                    set.add(null)
                }

                // Validate we cannot use Null values on remove
                assertFailsWith<java.lang.NullPointerException> {
                    set.remove(null)
                }

                // Validate we cannot use Null values on contains
                assertFailsWith<java.lang.NullPointerException> {
                    set.contains(null)
                }

                // Validate we cannot use Null values on addAll
                assertFailsWith<java.lang.NullPointerException> {
                    set.addAll(listOf(null))
                }

                // Validate we cannot use Null values on removeAll
                assertFailsWith<java.lang.NullPointerException> {
                    set.removeAll(listOf(null))
                }

                // Validate we cannot use Null values on containsAll
                assertFailsWith<java.lang.NullPointerException> {
                    set.containsAll(listOf(null))
                }

                // Validate we cannot use Null values on retainAll
                assertFailsWith<java.lang.NullPointerException> {
                    set.add(initializedSet[0])
                    set.retainAll(listOf(null))
                }
            }

            // Now check it works normally for the same field but without null values
            realm.executeTransaction {
                set.add(notPresentValue)
                set.remove(notPresentValue)
                set.contains(notPresentValue)

                set.addAll(listOf(notPresentValue))
                set.removeAll(listOf(notPresentValue))
                set.containsAll(listOf(notPresentValue))
                set.add(notPresentValue)
                set.retainAll(listOf(notPresentValue))
            }
        }
    }

    override fun retainAll() {
        val set = initAndAssertEmptySet(id = "id")

        realm.executeTransaction { transactionRealm ->
            // Does not change after empty set intersects with another collection
            assertFalse(set.retainAll(initializedSet))
            assertTrue(set.isEmpty())

            // Does not change after empty set intersects with empty collection
            assertFalse(set.retainAll(listOf()))
            assertTrue(set.isEmpty())

            // Does not change after adding data and intersecting it with same values
            set.addAll(initializedSet)
            assertEquals(initializedSet.size, set.size)
            assertFalse(set.retainAll(initializedSet))
            assertEquals(initializedSet.size, set.size)

            // Changes after intersection with empty collection
            assertTrue(set.retainAll(listOf()))
            assertTrue(set.isEmpty())

            // Throws after intersection with a collection of a different type
            set.addAll(initializedSet)
            val collectionOfDifferentType = initializedSet.map {
                Pair(it, it)
            }
            assertFailsWith<ClassCastException> {
                set.retainAll(collectionOfDifferentType as Collection<*>)
            }

            // Changes after adding data and intersecting it with other values
            assertEquals(initializedSet.size, set.size)
            assertTrue(set.retainAll(listOf(notPresentValue)))
            assertTrue(set.isEmpty())

            // Does not change after intersection with itself
            set.clear()
            set.addAll(initializedSet)
            assertFalse(set.isEmpty())
            assertFalse(set.retainAll(set))
            assertEquals(initializedSet.size, set.size)

            // Does not change after intersection with another set containing the same elements
            set.clear()
            set.addAll(initializedSet)
            val sameValuesManagedSet = managedSetGetter.get(transactionRealm.createObject())
            assertNotNull(sameValuesManagedSet)
            sameValuesManagedSet.addAll(initializedSet)
            assertFalse(set.retainAll(sameValuesManagedSet as Collection<T>))
            assertEquals(initializedSet.size, set.size)

            // Changes after intersection with a managed set not containing any elements from the original set
            set.clear()
            set.addAll(initializedSet)
            assertEquals(initializedSet.size, set.size)
            val notPresentValueSet = managedSetGetter.get(transactionRealm.createObject())
            assertNotNull(notPresentValueSet)
            notPresentValueSet.add(notPresentValue)
            assertTrue(set.retainAll(notPresentValueSet as Collection<T>))
            assertTrue(set.isEmpty())

            // Changes after intersection with another empty, managed set
            set.clear()
            set.addAll(initializedSet)
            val emptyManagedSet = managedSetGetter.get(transactionRealm.createObject())
            assertNotNull(emptyManagedSet)
            assertTrue(set.retainAll(emptyManagedSet as Collection<T>))
            assertTrue(set.isEmpty())

            // Does not change after intersection with a managed list with the same elements
            set.clear()
            set.addAll(initializedSet)
            val sameValuesManagedList = managedCollectionGetter.call(transactionRealm.createObject<SetContainerClass>())
            sameValuesManagedList.addAll(initializedSet)
            assertFalse(set.retainAll(sameValuesManagedList))
            assertTrue(set.containsAll(sameValuesManagedList))

            // Changes after intersection with a managed list with other elements
            val differentValuesManagedList =
                managedCollectionGetter.call(transactionRealm.createObject<SetContainerClass>())
            differentValuesManagedList.addAll(listOf(notPresentValue))
            assertTrue(set.retainAll(differentValuesManagedList))
            assertTrue(set.isEmpty())

            // Changes after intersection with an empty managed list
            set.clear()
            set.addAll(initializedSet)
            val emptyValuesManagedList =
                managedCollectionGetter.call(transactionRealm.createObject<SetContainerClass>())
            assertTrue(set.retainAll(emptyValuesManagedList))
            assertTrue(set.isEmpty())

            // TODO: it's not possible to test passing a null value from Kotlin, even if using
            //  TestHelper.getNull(). It seems that Kotlin generates different bytecode when the
            //  parameter to the function is a generics collection with an upper bound.
            //  The only way to test this is by writing a Java test instead.
        }
    }

    override fun removeAll() {
        val set = initAndAssertEmptySet(id = "id")

        realm.executeTransaction { transactionRealm ->
            // Does not change after removing a some values from an empty set
            assertTrue(set.isEmpty())
            assertFalse(set.removeAll(initializedSet))
            assertTrue(set.isEmpty())

            // Changes after adding values and then remove all
            set.addAll(initializedSet)
            assertEquals(initializedSet.size, set.size)
            assertTrue(set.removeAll(initializedSet))
            assertTrue(set.isEmpty())

            // Does not change after adding values again and remove empty collection
            set.addAll(initializedSet)
            assertFalse(set.removeAll(listOf()))
            assertEquals(initializedSet.size, set.size)

            // Throws when removing a list of a different type
            val differentTypeCollection = initializedSet.map {
                Pair(it, it)
            }
            assertFailsWith<ClassCastException> {
                set.removeAll(differentTypeCollection as Collection<*>)
            }

            // Does not change after remove something else from empty set
            assertFalse(set.removeAll(listOf(notPresentValue)))
            assertEquals(initializedSet.size, set.size)

            // Changes if we remove all items using itself
            set.addAll(initializedSet)
            assertEquals(initializedSet.size, set.size)
            assertTrue(set.removeAll(set))
            assertTrue(set.isEmpty())

            // Changes if we add some values and remove all items afterwards using another set containing the same items
            set.addAll(initializedSet)
            val sameValuesManagedSet = managedSetGetter.get(transactionRealm.createObject())
            assertNotNull(sameValuesManagedSet)
            sameValuesManagedSet.addAll(initializedSet)
            assertEquals(initializedSet.size, sameValuesManagedSet.size)
            assertTrue(set.removeAll(sameValuesManagedSet as Collection<T>))
            assertTrue(set.isEmpty())

            // Does not change if we add some values and remove a value not contained in the set afterwards
            set.addAll(initializedSet)
            assertEquals(initializedSet.size, set.size)
            val notPresentValueSet = managedSetGetter.get(transactionRealm.createObject())
            assertNotNull(notPresentValueSet)
            notPresentValueSet.add(notPresentValue)
            assertFalse(set.removeAll(notPresentValueSet as Collection<T>))
            assertEquals(initializedSet.size, set.size)

            // Changes after removing a managed list with the same elements
            set.clear()
            set.addAll(initializedSet)
            val sameValuesManagedList = managedCollectionGetter.call(transactionRealm.createObject<SetContainerClass>())
            sameValuesManagedList.addAll(initializedSet)
            assertTrue(set.removeAll(sameValuesManagedList))
            assertTrue(set.isEmpty())

            // Does not change after removing a managed list with other elements
            set.clear()
            set.addAll(initializedSet)
            val differentValuesManagedList =
                managedCollectionGetter.call(transactionRealm.createObject<SetContainerClass>())
            differentValuesManagedList.addAll(listOf(notPresentValue))
            assertFalse(set.removeAll(differentValuesManagedList))
            assertEquals(initializedSet.size, set.size)

            // Does not change after removing an empty managed list
            set.clear()
            set.addAll(initializedSet)
            val emptyValuesManagedList =
                managedCollectionGetter.call(transactionRealm.createObject<SetContainerClass>())
            assertFalse(set.removeAll(emptyValuesManagedList))
            assertEquals(initializedSet.size, set.size)

            // TODO: it's not possible to test passing a null value from Kotlin, even if using
            //  TestHelper.getNull(). It seems that Kotlin generates different bytecode when the
            //  parameter to the function is a generics collection with an upper bound.
            //  The only way to test this is by writing a Java test instead.
        }
    }

    override fun clear() {
        val set = initAndAssertEmptySet(id = "id")

        realm.executeTransaction {
            set.add(notPresentValue)
            assertEquals(1, set.size)
            set.clear()
            assertEquals(0, set.size)
        }
    }

    override fun freeze() {
        val set = initAndAssertEmptySet(id = "id")

        realm.executeTransaction {
            set.addAll(initializedSet)
        }

        val frozenSet = set.freeze()
        assertFalse(set.isFrozen)
        assertTrue(frozenSet.isFrozen)
        assertEquals(set.size, frozenSet.size)
    }

    override fun setters() {
        val allFields = createAllTypesManagedContainerAndAssert(realm, "id")
        val aSet = RealmSet<T>().init(initializedSet)

        realm.executeTransaction {
            setSetter(allFields, aSet)
            assertEquals(aSet.size, setGetter(allFields).size)

            // Validate it can assign the set to itself
            val managedSet = setGetter(allFields)
            setSetter(allFields, managedSet)
            assertEquals(aSet.size, setGetter(allFields).size)
        }
    }

    override fun addRealmChangeListener() {
        looperThread.runBlocking {
            val looperThreadRealm = Realm.getInstance(config)

            // Get set
            val set = initAndAssertEmptySet(looperThreadRealm, "id")

            // Define operation we perform on the dictionary
            var operation = ChangeListenerOperation.UNDEFINED

            set.addChangeListener { newSet ->
                when (operation) {
                    ChangeListenerOperation.INSERT -> {
                        assertEquals(initializedSet.size, newSet.size)
                    }
                    ChangeListenerOperation.DELETE -> {
                        assertTrue(newSet.isEmpty())
                        looperThreadRealm.close()
                        looperThread.testComplete()
                    }
                    else -> Unit
                }
            }

            // Insert objects in set
            looperThreadRealm.executeTransaction {
                operation = ChangeListenerOperation.INSERT
                set.addAll(initializedSet)
            }

            // Clear set
            looperThreadRealm.executeTransaction {
                operation = ChangeListenerOperation.DELETE
                set.clear()
            }
        }
    }

    override fun addSetChangeListener() {
        looperThread.runBlocking {
            val looperThreadRealm = Realm.getInstance(config)

            // Get set
            val set = initAndAssertEmptySet(looperThreadRealm, "id")

            // Define operation we perform on the dictionary
            var operation = ChangeListenerOperation.UNDEFINED

            set.addChangeListener { newSet, changes ->
                when (operation) {
                    ChangeListenerOperation.INSERT -> {
                        assertEquals(initializedSet.size, newSet.size)
                        assertEquals(initializedSet.size, changes.numberOfInsertions)
                    }
                    ChangeListenerOperation.DELETE -> {
                        assertTrue(newSet.isEmpty())
                        assertEquals(initializedSet.size, changes.numberOfDeletions)
                        looperThreadRealm.close()
                        looperThread.testComplete()
                    }
                    else -> Unit
                }
            }

            // Insert objects in set
            looperThreadRealm.executeTransaction {
                operation = ChangeListenerOperation.INSERT
                set.addAll(initializedSet)
            }

            // Clear set
            looperThreadRealm.executeTransaction {
                operation = ChangeListenerOperation.DELETE
                set.clear()
            }
        }
    }

    override fun removeRealmChangeListener() {
        looperThread.runBlocking {
            val looperThreadRealm = Realm.getInstance(config)

            // Get set
            val set = initAndAssertEmptySet(looperThreadRealm, "id")

            val listener: (RealmSet<T>) -> Unit = { _ -> /* no-op */ }
            set.addChangeListener(listener)

            assertTrue(set.hasListeners())

            set.removeChangeListener(listener)
            assertFalse(set.hasListeners())
        }
    }

    override fun removeSetChangeListener() {
        looperThread.runBlocking {
            val looperThreadRealm = Realm.getInstance(config)

            // Get set
            val set = initAndAssertEmptySet(looperThreadRealm, "id")

            val listener: (RealmSet<T>, SetChangeSet) -> Unit = { _, _ -> /* no-op */ }
            set.addChangeListener(listener)

            assertTrue(set.hasListeners())

            set.removeChangeListener(listener)
            assertFalse(set.hasListeners())
        }
    }

    override fun hasListeners() {
        looperThread.runBlocking {
            val looperThreadRealm = Realm.getInstance(config)

            // Check for RealmChangeListener
            val set = initAndAssertEmptySet(looperThreadRealm, "id")
            assertFalse(set.hasListeners())

            set.addChangeListener { _ -> /* no-op */ }

            assertTrue(set.hasListeners())

            // Check for SetChangeListener
            val anotherSet = initAndAssertEmptySet(looperThreadRealm, "anotherId")
            assertFalse(anotherSet.hasListeners())

            anotherSet.addChangeListener { _ -> /* no-op */ }

            assertTrue(anotherSet.hasListeners())

            // Housekeeping and bye-bye
            set.removeAllChangeListeners()
            anotherSet.removeAllChangeListeners()
            looperThreadRealm.close()
            looperThread.testComplete()
        }
    }

    override fun aggregations() {
        val set = initAndAssertEmptySet(id = "id")

        // Aggregation operations are not supported on primitive types.
        assertFailsWith<UnsupportedOperationException> {
            set.min("aFieldName")
        }

        assertFailsWith<UnsupportedOperationException> {
            set.max("aFieldName")
        }

        assertFailsWith<UnsupportedOperationException> {
            set.average("aFieldName")
        }

        assertFailsWith<UnsupportedOperationException> {
            set.sum("aFieldName")
        }

        assertFailsWith<UnsupportedOperationException> {
            set.minDate("aFieldName")
        }

        assertFailsWith<UnsupportedOperationException> {
            set.maxDate("aFieldName")
        }

        // Delete all is supported and behaves as a clear
        realm.executeTransaction {
            set.addAll(initializedSet)
            assertEquals(initializedSet.size, set.size)
            set.deleteAllFromRealm()
            assertTrue(set.isEmpty())
        }
    }

    //----------------------------------
    // Private stuff
    //----------------------------------

    private fun initAndAssertEmptySet(
        realm: Realm = this.realm,
        id: String? = null
    ): RealmSet<T> {
        val allTypesObject = createAllTypesManagedContainerAndAssert(realm, id)
        assertNotNull(allTypesObject)
        val set = setGetter.call(allTypesObject)
        assertTrue(set.isEmpty())
        return set
    }
}

fun managedSetFactory(): List<SetTester> {
    val primitiveTesters: List<SetTester> = SetSupportedType.values().mapNotNull { supportedType ->
        when (supportedType) {
            SetSupportedType.LONG ->
                ManagedSetTester<Long>(
                        testerName = "Long",
                        setFieldClass = Long::class.javaObjectType,
                        setFieldName = "columnLongSet",
                        setGetter = SetAllTypes::getColumnLongSet,
                        setSetter = SetAllTypes::setColumnLongSet,
                        requiredSetGetter = SetAllTypes::getColumnRequiredLongSet,
                        managedSetGetter = SetContainerClass::myLongSet,
                        managedCollectionGetter = SetContainerClass::myLongList,
                        initializedSet = listOf(VALUE_NUMERIC_HELLO.toLong(), VALUE_NUMERIC_BYE.toLong(), null),
                        notPresentValue = VALUE_NUMERIC_NOT_PRESENT.toLong(),
                        primaryKeyAllTypesSetProperty = SetAllTypesPrimaryKey::columnLongSet,
                        toArrayManaged = ToArrayManaged.LongManaged()
                )
            SetSupportedType.INTEGER ->
                ManagedSetTester<Int>(
                        testerName = "Integer",
                        setFieldClass = Int::class.javaObjectType,
                        setFieldName = "columnIntegerSet",
                        setGetter = SetAllTypes::getColumnIntegerSet,
                        setSetter = SetAllTypes::setColumnIntegerSet,
                        requiredSetGetter = SetAllTypes::getColumnRequiredIntegerSet,
                        managedSetGetter = SetContainerClass::myIntSet,
                        managedCollectionGetter = SetContainerClass::myIntList,
                        initializedSet = listOf(VALUE_NUMERIC_HELLO, VALUE_NUMERIC_BYE, null),
                        notPresentValue = VALUE_NUMERIC_NOT_PRESENT,
                        primaryKeyAllTypesSetProperty = SetAllTypesPrimaryKey::columnIntegerSet,
                        toArrayManaged = ToArrayManaged.IntManaged()
                )
            SetSupportedType.SHORT ->
                ManagedSetTester<Short>(
                        testerName = "Short",
                        setFieldClass = Short::class.javaObjectType,
                        setFieldName = "columnShortSet",
                        setGetter = SetAllTypes::getColumnShortSet,
                        setSetter = SetAllTypes::setColumnShortSet,
                        requiredSetGetter = SetAllTypes::getColumnRequiredShortSet,
                        managedSetGetter = SetContainerClass::myShortSet,
                        managedCollectionGetter = SetContainerClass::myShortList,
                        initializedSet = listOf(VALUE_NUMERIC_HELLO.toShort(), VALUE_NUMERIC_BYE.toShort(), null),
                        notPresentValue = VALUE_NUMERIC_NOT_PRESENT.toShort(),
                        primaryKeyAllTypesSetProperty = SetAllTypesPrimaryKey::columnShortSet,
                        toArrayManaged = ToArrayManaged.ShortManaged()
                )
            SetSupportedType.BYTE ->
                ManagedSetTester<Byte>(
                        testerName = "Byte",
                        setFieldClass = Byte::class.javaObjectType,
                        setFieldName = "columnByteSet",
                        setGetter = SetAllTypes::getColumnByteSet,
                        setSetter = SetAllTypes::setColumnByteSet,
                        requiredSetGetter = SetAllTypes::getColumnRequiredByteSet,
                        managedSetGetter = SetContainerClass::myByteSet,
                        managedCollectionGetter = SetContainerClass::myByteList,
                        initializedSet = listOf(VALUE_NUMERIC_HELLO.toByte(), VALUE_NUMERIC_BYE.toByte(), null),
                        notPresentValue = VALUE_NUMERIC_NOT_PRESENT.toByte(),
                        primaryKeyAllTypesSetProperty = SetAllTypesPrimaryKey::columnByteSet,
                        toArrayManaged = ToArrayManaged.ByteManaged()
                )
            SetSupportedType.FLOAT ->
                ManagedSetTester<Float>(
                        testerName = "Float",
                        setFieldClass = Float::class.javaObjectType,
                        setFieldName = "columnFloatSet",
                        setGetter = SetAllTypes::getColumnFloatSet,
                        setSetter = SetAllTypes::setColumnFloatSet,
                        requiredSetGetter = SetAllTypes::getColumnRequiredFloatSet,
                        managedSetGetter = SetContainerClass::myFloatSet,
                        managedCollectionGetter = SetContainerClass::myFloatList,
                        initializedSet = listOf(VALUE_NUMERIC_HELLO.toFloat(), VALUE_NUMERIC_BYE.toFloat(), null),
                        notPresentValue = VALUE_NUMERIC_NOT_PRESENT.toFloat(),
                        primaryKeyAllTypesSetProperty = SetAllTypesPrimaryKey::columnFloatSet,
                        toArrayManaged = ToArrayManaged.FloatManaged()
                )
            SetSupportedType.DOUBLE ->
                ManagedSetTester<Double>(
                        testerName = "Double",
                        setFieldClass = Double::class.javaObjectType,
                        setFieldName = "columnDoubleSet",
                        setGetter = SetAllTypes::getColumnDoubleSet,
                        setSetter = SetAllTypes::setColumnDoubleSet,
                        requiredSetGetter = SetAllTypes::getColumnRequiredDoubleSet,
                        managedSetGetter = SetContainerClass::myDoubleSet,
                        managedCollectionGetter = SetContainerClass::myDoubleList,
                        initializedSet = listOf(VALUE_NUMERIC_HELLO.toDouble(), VALUE_NUMERIC_BYE.toDouble(), null),
                        notPresentValue = VALUE_NUMERIC_NOT_PRESENT.toDouble(),
                        primaryKeyAllTypesSetProperty = SetAllTypesPrimaryKey::columnDoubleSet,
                        toArrayManaged = ToArrayManaged.DoubleManaged()
                )
            SetSupportedType.STRING ->
                ManagedSetTester<String>(
                        testerName = "String",
                        setFieldClass = String::class.javaObjectType,
                        setFieldName = "columnStringSet",
                        setGetter = SetAllTypes::getColumnStringSet,
                        setSetter = SetAllTypes::setColumnStringSet,
                        requiredSetGetter = SetAllTypes::getColumnRequiredStringSet,
                        managedSetGetter = SetContainerClass::myStringSet,
                        managedCollectionGetter = SetContainerClass::myStringList,
                        initializedSet = listOf(VALUE_STRING_HELLO, VALUE_STRING_BYE, null),
                        notPresentValue = VALUE_STRING_NOT_PRESENT,
                        primaryKeyAllTypesSetProperty = SetAllTypesPrimaryKey::columnStringSet,
                        toArrayManaged = ToArrayManaged.StringManaged()
                )
            SetSupportedType.BOOLEAN ->
                ManagedSetTester<Boolean>(
                        testerName = "Boolean",
                        setFieldClass = Boolean::class.javaObjectType,
                        setFieldName = "columnBooleanSet",
                        setGetter = SetAllTypes::getColumnBooleanSet,
                        setSetter = SetAllTypes::setColumnBooleanSet,
                        requiredSetGetter = SetAllTypes::getColumnRequiredBooleanSet,
                        managedSetGetter = SetContainerClass::myBooleanSet,
                        managedCollectionGetter = SetContainerClass::myBooleanList,
                        initializedSet = listOf(VALUE_BOOLEAN_HELLO, null),
                        notPresentValue = VALUE_BOOLEAN_NOT_PRESENT,
                        primaryKeyAllTypesSetProperty = SetAllTypesPrimaryKey::columnBooleanSet,
                        toArrayManaged = ToArrayManaged.BooleanManaged()
                )
            SetSupportedType.DATE ->
                ManagedSetTester<Date>(
                        testerName = "Date",
                        setFieldClass = Date::class.javaObjectType,
                        setFieldName = "columnDateSet",
                        setGetter = SetAllTypes::getColumnDateSet,
                        setSetter = SetAllTypes::setColumnDateSet,
                        requiredSetGetter = SetAllTypes::getColumnRequiredDateSet,
                        managedSetGetter = SetContainerClass::myDateSet,
                        managedCollectionGetter = SetContainerClass::myDateList,
                        initializedSet = listOf(VALUE_DATE_HELLO, VALUE_DATE_BYE, null),
                        notPresentValue = VALUE_DATE_NOT_PRESENT,
                        primaryKeyAllTypesSetProperty = SetAllTypesPrimaryKey::columnDateSet,
                        toArrayManaged = ToArrayManaged.DateManaged()
                )
            SetSupportedType.DECIMAL128 ->
                ManagedSetTester<Decimal128>(
                        testerName = "Decimal128",
                        setFieldClass = Decimal128::class.javaObjectType,
                        setFieldName = "columnDecimal128Set",
                        setGetter = SetAllTypes::getColumnDecimal128Set,
                        setSetter = SetAllTypes::setColumnDecimal128Set,
                        requiredSetGetter = SetAllTypes::getColumnRequiredDecimal128Set,
                        managedSetGetter = SetContainerClass::myDecimal128Set,
                        managedCollectionGetter = SetContainerClass::myDecimal128List,
                        initializedSet = listOf(VALUE_DECIMAL128_HELLO, VALUE_DECIMAL128_BYE, null),
                        notPresentValue = VALUE_DECIMAL128_NOT_PRESENT,
                        primaryKeyAllTypesSetProperty = SetAllTypesPrimaryKey::columnDecimal128Set,
                        toArrayManaged = ToArrayManaged.Decimal128Managed()
                )
            SetSupportedType.BINARY ->
                ManagedSetTester<ByteArray>(
                        testerName = "Binary",
                        setFieldClass = ByteArray::class.javaObjectType,
                        setFieldName = "columnBinarySet",
                        setGetter = SetAllTypes::getColumnBinarySet,
                        setSetter = SetAllTypes::setColumnBinarySet,
                        requiredSetGetter = SetAllTypes::getColumnRequiredBinarySet,
                        managedSetGetter = SetContainerClass::myBinarySet,
                        managedCollectionGetter = SetContainerClass::myBinaryList,
                        initializedSet = listOf(VALUE_BINARY_HELLO, VALUE_BINARY_BYE, null),
                        notPresentValue = VALUE_BINARY_NOT_PRESENT,
                        equalsTo = { expected, value ->
                            Arrays.equals(expected, value)
                        },
                        primaryKeyAllTypesSetProperty = SetAllTypesPrimaryKey::columnBinarySet,
                        toArrayManaged = ToArrayManaged.BinaryManaged()
                )
            SetSupportedType.OBJECT_ID ->
                ManagedSetTester<ObjectId>(
                        testerName = "ObjectId",
                        setFieldClass = ObjectId::class.javaObjectType,
                        setFieldName = "columnObjectIdSet",
                        setGetter = SetAllTypes::getColumnObjectIdSet,
                        setSetter = SetAllTypes::setColumnObjectIdSet,
                        requiredSetGetter = SetAllTypes::getColumnRequiredObjectIdSet,
                        managedSetGetter = SetContainerClass::myObjectIdSet,
                        managedCollectionGetter = SetContainerClass::myObjectIdList,
                        initializedSet = listOf(VALUE_OBJECT_ID_HELLO, VALUE_OBJECT_ID_BYE, null),
                        notPresentValue = VALUE_OBJECT_ID_NOT_PRESENT,
                        primaryKeyAllTypesSetProperty = SetAllTypesPrimaryKey::columnObjectIdSet,
                        toArrayManaged = ToArrayManaged.ObjectIdManaged()
                )

            SetSupportedType.UUID ->
                ManagedSetTester<UUID>(
                        testerName = "UUID",
                        setFieldClass = UUID::class.javaObjectType,
                        setFieldName = "columnUUIDSet",
                        setGetter = SetAllTypes::getColumnUUIDSet,
                        setSetter = SetAllTypes::setColumnUUIDSet,
                        requiredSetGetter = SetAllTypes::getColumnRequiredUUIDSet,
                        managedSetGetter = SetContainerClass::myUUIDSet,
                        managedCollectionGetter = SetContainerClass::myUUIDList,
                        initializedSet = listOf(VALUE_UUID_HELLO, VALUE_UUID_BYE, null),
                        notPresentValue = VALUE_UUID_NOT_PRESENT,
                        primaryKeyAllTypesSetProperty = SetAllTypesPrimaryKey::columnUUIDSet,
                        toArrayManaged = ToArrayManaged.UUIDManaged()
                )

            SetSupportedType.LINK ->
                RealmModelManagedSetTester<DogPrimaryKey>(
                        testerName = "LINK",
                        setFieldClass = DogPrimaryKey::class.java,
                        setFieldName = "columnRealmModelSet",
                        setGetter = SetAllTypes::getColumnRealmModelSet,
                        setSetter = SetAllTypes::setColumnRealmModelSet,
                        managedSetGetter = SetContainerClass::myRealmModelSet,
                        managedCollectionGetter = SetContainerClass::myRealmModelList,
                        unmanagedInitializedSet = listOf(VALUE_LINK_HELLO, VALUE_LINK_BYE),
                        unmanagedNotPresentValue = VALUE_LINK_NOT_PRESENT,
                        primaryKeyAllTypesSetProperty = SetAllTypesPrimaryKey::columnRealmModelSet,
                        toArrayManaged = ToArrayManaged.RealmModelManaged(),
                        insertObjects = { realm, objects ->
                            realm.copyToRealmOrUpdate(objects)
                        },
                        deleteObjects = { objects ->
                            objects.forEach {
                                it!!.deleteFromRealm()
                            }
                        },
                        equalsTo = { expected: DogPrimaryKey?, value: DogPrimaryKey? ->
                            (expected == null && value == null) || ((expected != null && value != null) && (expected.id == value.id))
                        },
                        nullable = false
                )
            // Ignore RealmAny in this switch
            else -> null
        }
    }

    // Add extra tests for RealmAny datatype and Realm Models without PK
    return primitiveTesters
            // We add an extra test for Realm models without a PK
            .plus(NoPKRealmModelSetTester<Owner>(
                    testerName = "LINK_NO_PK",
                    setGetter = SetAllTypes::getColumnRealmModelNoPkSet,
                    setSetter = SetAllTypes::setColumnRealmModelNoPkSet,
                    managedSetGetter = SetContainerClass::myRealmModelNoPkSet,
                    managedCollectionGetter = SetContainerClass::myRealmModelNoPkList,
                    initializedSet = listOf(VALUE_LINK_NO_PK_HELLO, VALUE_LINK_NO_PK_BYE),
                    notPresentValue = VALUE_LINK_NO_PK_NOT_PRESENT,
                    toArrayManaged = ToArrayManaged.RealmModelNoPKManaged()
            ))
            // Then we add the tests for RealmAny types
            .plus(RealmAny.Type.values().map { realmAnyType ->
                when (realmAnyType) {
                    RealmAny.Type.OBJECT -> RealmModelManagedSetTester<RealmAny>(
                            testerName = "MIXED-${realmAnyType.name}",
                            realmAnyType = realmAnyType,
                            setFieldClass = RealmAny::class.java,
                            setFieldName = "columnRealmAnySet",
                            setGetter = SetAllTypes::getColumnRealmAnySet,
                            setSetter = SetAllTypes::setColumnRealmAnySet,
                            managedSetGetter = SetContainerClass::myRealmAnySet,
                            managedCollectionGetter = SetContainerClass::myRealmAnyList,
                            unmanagedInitializedSet = getRealmAnyKeyValuePairs(realmAnyType).map {
                                it.second
                            },
                            unmanagedNotPresentValue = RealmAny.valueOf(VALUE_LINK_NOT_PRESENT),
                            toArrayManaged = ToArrayManaged.RealmAnyManaged(),
                            insertObjects = { realm, objects ->
                                objects.map { realmAny ->
                                    if (realmAny?.type == RealmAny.Type.OBJECT) {
                                        val unmanagedObject = realmAny.asRealmModel(DogPrimaryKey::class.java)
                                        val managedObject = realm.copyToRealmOrUpdate(unmanagedObject)
                                        RealmAny.valueOf(managedObject)
                                    } else {
                                        realmAny
                                    }
                                }
                            },
                            deleteObjects = { objects: List<RealmAny?> ->
                                objects.map { realmAny ->
                                    if (realmAny?.type == RealmAny.Type.OBJECT) {
                                        val managedObject = realmAny.asRealmModel(DogPrimaryKey::class.java)
                                        managedObject.deleteFromRealm()
                                    } else {
                                        realmAny
                                    }
                                }
                            },
                            nullable = true,
                            equalsTo = { expected, value ->
                                if (expected == null && value == RealmAny.nullValue()) {
                                    true
                                } else if(expected != null && value != RealmAny.nullValue()) {
                                    val expectedModel = expected.asRealmModel(DogPrimaryKey::class.java)
                                    // Managed RealmAny values are cannot be null but RealmAny.nullValue()
                                    val valueModel = value!!.asRealmModel(DogPrimaryKey::class.java)

                                    expectedModel.id == valueModel.id
                                } else {
                                    false
                                }
                            },
                            primaryKeyAllTypesSetProperty = SetAllTypesPrimaryKey::columnRealmAnySet
                    )
                    RealmAny.Type.NULL -> NullRealmAnySetTester(
                            testerName = "MIXED-${realmAnyType.name}",
                            setGetter = SetAllTypes::getColumnRealmAnySet
                    )
                    else -> ManagedSetTester<RealmAny>(
                            testerName = "MIXED-${realmAnyType.name}",
                            realmAnyType = realmAnyType,
                            setFieldClass = RealmAny::class.java,
                            setFieldName = "columnRealmAnySet",
                            setGetter = SetAllTypes::getColumnRealmAnySet,
                            setSetter = SetAllTypes::setColumnRealmAnySet,
                            managedSetGetter = SetContainerClass::myRealmAnySet,
                            managedCollectionGetter = SetContainerClass::myRealmAnyList,
                            initializedSet = getRealmAnyKeyValuePairs(realmAnyType).map {
                                it.second
                            },
                            notPresentValue = VALUE_MIXED_NOT_PRESENT,
                            toArrayManaged = ToArrayManaged.RealmAnyManaged(),
                            equalsTo = { expected, value ->
                                (expected == null && value == RealmAny.nullValue()) || ((expected != null) && (expected == value))
                            },
                            primaryKeyAllTypesSetProperty = SetAllTypesPrimaryKey::columnRealmAnySet
                    )
                }
            })
}

/**
 * TODO
 */
abstract class ToArrayManaged<T> {

    abstract fun assertToArrayWithParameter(realm: Realm, set: RealmSet<T>, values: List<T?>)

    fun assertContains(array: Array<T>, set: RealmSet<T>) {
        array.forEach { arrayValue ->
            assertTrue(set.contains(arrayValue))
        }
    }

    protected fun test(
        realm: Realm,
        set: RealmSet<T>,
        values: List<T?>,
        emptyArray: Array<T>,
        fullArray: Array<T>
    ) {
        val emptyFromSet = set.toArray(emptyArray)
        assertEquals(0, emptyFromSet.size)

        realm.executeTransaction {
            set.addAll(values as Collection<T>)
        }
        val fullFromSet = set.toArray(fullArray)
        assertEquals(values.size, fullFromSet.size)
    }

    class LongManaged : ToArrayManaged<Long>() {
        override fun assertToArrayWithParameter(realm: Realm, set: RealmSet<Long>, values: List<Long?>) =
            test(realm, set, values, emptyArray(), arrayOf())
    }

    class IntManaged : ToArrayManaged<Int>() {
        override fun assertToArrayWithParameter(realm: Realm, set: RealmSet<Int>, values: List<Int?>) =
            test(realm, set, values, emptyArray(), arrayOf())
    }

    class ShortManaged : ToArrayManaged<Short>() {
        override fun assertToArrayWithParameter(realm: Realm, set: RealmSet<Short>, values: List<Short?>) =
            test(realm, set, values, emptyArray(), arrayOf())
    }

    class ByteManaged : ToArrayManaged<Byte>() {
        override fun assertToArrayWithParameter(realm: Realm, set: RealmSet<Byte>, values: List<Byte?>) =
            test(realm, set, values, emptyArray(), arrayOf())
    }

    class FloatManaged : ToArrayManaged<Float>() {
        override fun assertToArrayWithParameter(realm: Realm, set: RealmSet<Float>, values: List<Float?>) =
            test(realm, set, values, emptyArray(), arrayOf())
    }

    class DoubleManaged : ToArrayManaged<Double>() {
        override fun assertToArrayWithParameter(realm: Realm, set: RealmSet<Double>, values: List<Double?>) =
            test(realm, set, values, emptyArray(), arrayOf())
    }

    class StringManaged : ToArrayManaged<String>() {
        override fun assertToArrayWithParameter(realm: Realm, set: RealmSet<String>, values: List<String?>) =
            test(realm, set, values, emptyArray(), arrayOf())
    }

    class BooleanManaged : ToArrayManaged<Boolean>() {
        override fun assertToArrayWithParameter(realm: Realm, set: RealmSet<Boolean>, values: List<Boolean?>) =
            test(realm, set, values, emptyArray(), arrayOf())
    }

    class DateManaged : ToArrayManaged<Date>() {
        override fun assertToArrayWithParameter(realm: Realm, set: RealmSet<Date>, values: List<Date?>) =
            test(realm, set, values, emptyArray(), arrayOf())
    }

    class Decimal128Managed : ToArrayManaged<Decimal128>() {
        override fun assertToArrayWithParameter(realm: Realm, set: RealmSet<Decimal128>, values: List<Decimal128?>) =
            test(realm, set, values, emptyArray(), arrayOf())
    }

    class BinaryManaged : ToArrayManaged<ByteArray>() {
        override fun assertToArrayWithParameter(realm: Realm, set: RealmSet<ByteArray>, values: List<ByteArray?>) =
            test(realm, set, values, emptyArray(), arrayOf())
    }

    class ObjectIdManaged : ToArrayManaged<ObjectId>() {
        override fun assertToArrayWithParameter(realm: Realm, set: RealmSet<ObjectId>, values: List<ObjectId?>) =
            test(realm, set, values, emptyArray(), arrayOf())
    }

    class UUIDManaged : ToArrayManaged<UUID>() {
        override fun assertToArrayWithParameter(realm: Realm, set: RealmSet<UUID>, values: List<UUID?>) =
            test(realm, set, values, emptyArray(), arrayOf())
    }

    class RealmModelManaged : ToArrayManaged<DogPrimaryKey>() {
        override fun assertToArrayWithParameter(
            realm: Realm,
            set: RealmSet<DogPrimaryKey>,
            values: List<DogPrimaryKey?>
        ) =
            test(realm, set, values, emptyArray(), arrayOf())
    }

    class RealmModelNoPKManaged : ToArrayManaged<Owner>() {
        override fun assertToArrayWithParameter(realm: Realm, set: RealmSet<Owner>, values: List<Owner?>) =
            test(realm, set, values, emptyArray(), arrayOf())
    }

    class RealmAnyManaged : ToArrayManaged<RealmAny>() {
        override fun assertToArrayWithParameter(realm: Realm, set: RealmSet<RealmAny>, values: List<RealmAny?>) =
            test(realm, set, values, emptyArray(), arrayOf())
    }
}

