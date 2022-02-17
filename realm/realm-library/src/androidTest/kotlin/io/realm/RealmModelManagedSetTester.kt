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
import io.realm.rule.BlockingLooperThread
import java.lang.IllegalArgumentException
import kotlin.reflect.KFunction1
import kotlin.reflect.KFunction2
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1
import kotlin.test.*

/**
 * Generic tester for Realm models types of managed sets.
 *
 * It delegates the validation of managed realm models to the ManagedSetTester class, as it will validate all the paths,
 * whereas in this test we validate Realm models specific cases.
 */
class RealmModelManagedSetTester<T : Any>(
    private val testerName: String,
    private val setFieldName: String,
    private val setFieldClass: Class<T>,
    private val realmAnyType: RealmAny.Type? = null,
    private val setGetter: KFunction1<SetAllTypes, RealmSet<T>>,
    private val setSetter: KFunction2<SetAllTypes, RealmSet<T>, Unit>,
    private val managedSetGetter: KProperty1<SetContainerClass, RealmSet<T>>,
    private val managedCollectionGetter: KProperty1<SetContainerClass, RealmList<T>>,
    private val unmanagedInitializedSet: List<T?>,
    private val unmanagedNotPresentValue: T,
    private val toArrayManaged: ToArrayManaged<T>,
    private val insertObjects: (realm: Realm, objects: List<T?>) -> List<T?>,
    private val deleteObjects: (objects: List<T?>) -> Unit,
    private val nullable: Boolean,
    private val equalsTo: (expected: T?, value: T?) -> Boolean,
    private val primaryKeyAllTypesSetProperty: KMutableProperty1<SetAllTypesPrimaryKey, RealmSet<T>>
) : SetTester {

    private lateinit var managedTester: ManagedSetTester<T>
    private lateinit var config: RealmConfiguration
    private lateinit var looperThread: BlockingLooperThread
    private lateinit var realm: Realm
    private lateinit var managedInitializedSet: List<T?>
    private lateinit var managedNotPresentValue: T

    private fun initAndAssertEmptySet(realm: Realm = this.realm): RealmSet<T> {
        val allTypesObject = createAllTypesManagedContainerAndAssert(realm, "unmanaged")
        assertNotNull(allTypesObject)
        val set = setGetter.call(allTypesObject)
        assertTrue(set.isEmpty())
        return set
    }

    override fun toString(): String = "RealmModelManagedSet-${testerName}"

    override fun setUp(config: RealmConfiguration, looperThread: BlockingLooperThread) {
        this.config = config
        this.looperThread = looperThread
        this.realm = Realm.getInstance(config)

        realm.executeTransaction { transactionRealm ->
            managedInitializedSet = insertObjects(transactionRealm, unmanagedInitializedSet)
            managedNotPresentValue = insertObjects(transactionRealm, listOf<T?>(unmanagedNotPresentValue))[0]!!
        }

        this.managedTester = ManagedSetTester(
            testerName = testerName,
            setFieldClass = setFieldClass,
            setFieldName = setFieldName,
            realmAnyType = realmAnyType,
            setGetter = setGetter,
            setSetter = setSetter,
            managedSetGetter = managedSetGetter,
            managedCollectionGetter = managedCollectionGetter,
            initializedSet = managedInitializedSet,
            notPresentValue = managedNotPresentValue,
            toArrayManaged = toArrayManaged,
            nullable = nullable,
            equalsTo = equalsTo,
            primaryKeyAllTypesSetProperty = primaryKeyAllTypesSetProperty
        )

        this.managedTester.setUp(config, looperThread)
    }

    override fun tearDown() {
        managedTester.tearDown()
        this.realm.close()
    }

    override fun isManaged() = managedTester.isManaged()

    override fun isValid() = managedTester.isValid()

    override fun isFrozen() = managedTester.isFrozen()

    override fun size() = managedTester.size()

    override fun isEmpty() = managedTester.isEmpty()

    override fun requiredConstraints() = Unit // Not tested

    override fun contains() {
        managedTester.contains()

        // Test with unmanaged realm objects
        val set = initAndAssertEmptySet()
        realm.executeTransaction {
            // Check throws exception when unmanaged values are passed
            assertFailsWith<IllegalArgumentException>("Unmanaged objects not permitted") {
                set.contains(unmanagedNotPresentValue)
            }

            if (!nullable) {
                assertFailsWith<java.lang.NullPointerException>("Set does not support null values") {
                    assertFalse(set.contains(null))
                }
            }
        }

        // Test with object from another realm
        accessTransactionRealmInLooperThread { looperRealm ->
            val value = insertObjects(looperRealm, listOf<T?>(unmanagedNotPresentValue))[0]

            assertFailsWith<IllegalArgumentException>("Cannot pass values from another Realm") {
                set.contains(value)
            }
        }
    }

    override fun iterator() = managedTester.iterator()

    override fun toArray() = managedTester.toArray()

    override fun toArrayWithParameter() = managedTester.toArrayWithParameter()

    override fun dynamic() {
        if (realmAnyType != null) {
            dynamicRealmAnyTest()
        } else {
            dynamicObjectTest()
        }
    }

    private fun toDynamicRealmAny(realm: DynamicRealm, value: RealmAny): RealmAny {
        val id = value.asRealmModel(DogPrimaryKey::class.java).id
        return RealmAny.valueOf(realm.where(DogPrimaryKey.CLASS_NAME).equalTo(DogPrimaryKey.ID, id).findFirst()!!)
    }

    private fun dynamicRealmAnyTest() {
        // Create a set from a immutable schema context
        val set = initAndAssertEmptySet()
        realm.executeTransaction {
            set.addAll(unmanagedInitializedSet)
        }

        val dynamicRealm = DynamicRealm.getInstance(realm.configuration)
        val dynamicObject: DynamicRealmObject = dynamicRealm.where(SetAllTypes.NAME)
            .equalTo(AllTypes.FIELD_STRING, "unmanaged").findFirst()!!
        val dynamicSet: RealmSet<RealmAny> = dynamicObject.getRealmSet(setFieldName, setFieldClass) as RealmSet<RealmAny>

        // Access the previous set from a mutable context
        assertEquals(3, dynamicSet.size)

        // Update the set with a new value
        dynamicRealm.executeTransaction {
            dynamicSet.add(toDynamicRealmAny(dynamicRealm, unmanagedNotPresentValue as RealmAny))
        }

        assertEquals(4, dynamicSet.size)

        // Try to replace the whole set by a new one
        dynamicRealm.executeTransaction {
            dynamicObject.setRealmSet(setFieldName, RealmSet<RealmAny>().apply {
                add(toDynamicRealmAny(dynamicRealm, unmanagedNotPresentValue as RealmAny))
            })
        }

        assertEquals(1, dynamicObject.getRealmSet(setFieldName, setFieldClass).size)

        // Validate that set is properly represented as a String
        managedTester.validateToString(dynamicObject, dynamicSet)

        dynamicRealm.close()
    }

    private fun dynamicObjectTest() {
        // Create a set from a immutable schema context
        val set = initAndAssertEmptySet()
        realm.executeTransaction {
            set.addAll(unmanagedInitializedSet)
        }

        val dynamicRealm = DynamicRealm.getInstance(realm.configuration)
        val dynamicObject: DynamicRealmObject =
            dynamicRealm.where(SetAllTypes.NAME).equalTo(AllTypes.FIELD_STRING, "unmanaged").findFirst()!!
        val dynamicSet = dynamicObject.getRealmSet(setFieldName)

        // Access the previous set from a mutable context
        set.forEach { value ->
            if (RealmObject.isValid(value as DogPrimaryKey)) {
                val managedObject =
                    dynamicRealm.where(DogPrimaryKey.CLASS_NAME).equalTo(DogPrimaryKey.ID, (value as DogPrimaryKey).id)
                        .findFirst()!!
                assertTrue(dynamicSet.contains(managedObject))
            }
        }

        // Update the set with a new value
        dynamicRealm.executeTransaction {
            val notPresentManaged = dynamicRealm.where(DogPrimaryKey.CLASS_NAME)
                .equalTo(DogPrimaryKey.ID, (unmanagedNotPresentValue as DogPrimaryKey).id).findFirst()!!
            dynamicSet.add(notPresentManaged)
        }

        set.plus(unmanagedNotPresentValue).forEach { value ->
            if (RealmObject.isValid(value as DogPrimaryKey)) {
                val managedObject =
                    dynamicRealm.where(DogPrimaryKey.CLASS_NAME).equalTo(DogPrimaryKey.ID, (value as DogPrimaryKey).id)
                        .findFirst()!!
                assertTrue(dynamicSet.contains(managedObject))
            }
        }

        // Try to replace the whole set by a new one
        dynamicRealm.executeTransaction {
            val notPresentManaged = dynamicRealm.where(DogPrimaryKey.CLASS_NAME)
                .equalTo(DogPrimaryKey.ID, (unmanagedNotPresentValue as DogPrimaryKey).id).findFirst()!!
            dynamicObject.setRealmSet(setFieldName, RealmSet<DynamicRealmObject>().apply {
                add(notPresentManaged)
            })
        }

        assertEquals(1, dynamicObject.get<RealmSet<T>>(setFieldName).size)

        // Validate that set is properly represented as a String
        managedTester.validateToString(dynamicObject, dynamicSet)

        dynamicRealm.close()
    }

    override fun add() {
        // Test with managed objects
        managedTester.add()

        // Test with unmanaged objects
        val set = initAndAssertEmptySet()
        realm.executeTransaction {
            // Adding a value for the first time returns true
            unmanagedInitializedSet.forEach { value ->
                assertTrue(set.add(value))
            }
            // Adding an existing value returns false
            unmanagedInitializedSet.forEach { value ->
                assertFalse(set.add(value))
            }
        }

        // Test with object from another realm
        accessTransactionRealmInLooperThread { looperRealm ->
            val value = insertObjects(looperRealm, listOf<T?>(unmanagedNotPresentValue))[0]

            assertFailsWith<IllegalArgumentException>("Cannot pass values from another Realm") {
                set.add(value)
            }
        }

        assertTrue(set.containsAll(managedInitializedSet))
    }

    override fun remove() {
        managedTester.remove()

        // Test with unmanaged realm objects
        val set = initAndAssertEmptySet()
        realm.executeTransaction {
            // Check throws exception when unmanaged values are passed
            assertFailsWith<IllegalArgumentException>("Unmanaged objects not permitted") {
                set.remove(unmanagedNotPresentValue)
            }
        }

        // Test with object from another realm
        accessTransactionRealmInLooperThread { looperRealm ->
            val value = insertObjects(looperRealm, listOf<T?>(unmanagedNotPresentValue))[0]

            assertFailsWith<IllegalArgumentException>("Cannot pass values from another Realm") {
                set.remove(value)
            }
        }
    }

    override fun insert() {
        // This specific test case needs unmanaged objects on PK models
        realm.executeTransaction {
            deleteObjects(managedInitializedSet)
        }

        // Call with unmanaged objects
        managedTester.doInsertTest(unmanagedInitializedSet)
    }

    override fun insertList() {
        // This specific test case needs unmanaged objects on PK models
        realm.executeTransaction {
            deleteObjects(managedInitializedSet)
        }

        // Call with unmanaged objects
        managedTester.doInsertListTest(unmanagedInitializedSet)
    }

    override fun insertOrUpdate() {
        managedTester.insertOrUpdate()
    }

    override fun insertOrUpdateList() {
        managedTester.insertOrUpdate()
    }

    override fun copyToRealm() {
        // This specific test case needs unmanaged objects on PK models
        realm.executeTransaction {
            deleteObjects(managedInitializedSet)
        }

        // Call with unmanaged objects
        managedTester.doCopyToRealmTest(unmanagedInitializedSet)
    }

    override fun copyToRealmOrUpdate() {
        managedTester.copyToRealmOrUpdate()
    }

    override fun containsAll() {
        // Test with managed realm objects
        managedTester.containsAll()

        // Test with unmanaged realm objects
        val set = initAndAssertEmptySet()
        realm.executeTransaction {
            // Check throws exception when unmanaged values are passed
            assertFailsWith<IllegalArgumentException>("Collection with unmanaged objects not permitted") {
                set.containsAll(unmanagedInitializedSet)
            }

            if (!nullable) {
                // Checks it does not contain nulls
                assertFailsWith<java.lang.NullPointerException>("Set does not support null values") {
                    assertFalse(set.containsAll(listOf(null)))
                }
            }
        }

        // Test with objects from another realm
        accessTransactionRealmInLooperThread { looperRealm ->
            val values = insertObjects(looperRealm, unmanagedInitializedSet)

            assertFailsWith<IllegalArgumentException>("Cannot pass values from another Realm") {
                set.containsAll(values)
            }
        }
    }

    override fun addAll() {
        // Test with managed objects
        managedTester.addAll()

        val set = initAndAssertEmptySet()
        realm.executeTransaction { transactionRealm ->
            // Changes after adding collection
            if (!nullable) {
                assertFailsWith<java.lang.NullPointerException>("Cannot add null values into this set") {
                    set.addAll(listOf(null))
                }
            }

            assertTrue(set.addAll(unmanagedInitializedSet))
            assertEquals(unmanagedInitializedSet.size, set.size)

            // Does not change if we add the same data
            assertFalse(set.addAll(unmanagedInitializedSet))
            assertEquals(unmanagedInitializedSet.size, set.size)

            // Does not change if we add itself to it
            assertFalse(set.addAll(set))
            assertEquals(unmanagedInitializedSet.size, set.size)

            // Does not change if we add an empty collection
            assertFalse(set.addAll(listOf()))
            assertEquals(unmanagedInitializedSet.size, set.size)

            // Throws when adding a collection of a different type
            val somethingEntirelyDifferent = unmanagedInitializedSet.map {
                Pair(it, it)
            }
            assertFailsWith<ClassCastException> {
                set.addAll(somethingEntirelyDifferent as Collection<T>)
            }

            // Does not change if we add the same data from a managed set
            val sameValuesManagedSet = managedSetGetter.get(transactionRealm.createObject())
            assertNotNull(sameValuesManagedSet)
            assertTrue(sameValuesManagedSet.addAll(unmanagedInitializedSet))
            assertFalse(set.addAll(sameValuesManagedSet as Collection<T>))
            assertEquals(unmanagedInitializedSet.size, set.size)

            // Does not change if we add an empty RealmSet
            val emptyManagedSet = managedSetGetter.get(transactionRealm.createObject())
            assertTrue(emptyManagedSet.isEmpty())
            assertFalse(set.addAll(emptyManagedSet))
            assertEquals(unmanagedInitializedSet.size, set.size)

            // Changes after adding a managed set containing other values
            val notPresentValueSet = managedSetGetter.get(transactionRealm.createObject())
            assertNotNull(notPresentValueSet)
            notPresentValueSet.add(unmanagedNotPresentValue)
            assertTrue(set.addAll(notPresentValueSet as Collection<T>))
            assertEquals(unmanagedInitializedSet.size + notPresentValueSet.size, set.size)

            // Does not change after adding a managed list with the same elements
            set.clear()
            set.addAll(unmanagedInitializedSet)
            val sameValuesManagedList = managedCollectionGetter.call(transactionRealm.createObject<SetContainerClass>())
            sameValuesManagedList.addAll(unmanagedInitializedSet)
            assertFalse(set.addAll(sameValuesManagedList))
            assertTrue(set.containsAll(sameValuesManagedList))

            // Changes after adding a managed list with other elements
            val differentValuesManagedList =
                managedCollectionGetter.call(transactionRealm.createObject<SetContainerClass>())
            differentValuesManagedList.addAll(listOf(unmanagedNotPresentValue))
            assertTrue(set.addAll(differentValuesManagedList))
            assertTrue(set.containsAll(differentValuesManagedList))

            // Does not change after adding an empty managed list
            set.clear()
            assertTrue(set.addAll(unmanagedInitializedSet))
            val emptyValuesManagedList =
                managedCollectionGetter.call(transactionRealm.createObject<SetContainerClass>())
            assertFalse(set.addAll(emptyValuesManagedList))
            assertEquals(unmanagedInitializedSet.size, set.size)

            // Fails if passed null according to Java Set interface
            assertFailsWith<NullPointerException> {
                set.addAll(TestHelper.getNull())
            }
        }

        // Test with objects from another realm
        accessTransactionRealmInLooperThread { looperRealm ->
            val values = insertObjects(looperRealm, unmanagedInitializedSet)

            assertFailsWith<IllegalArgumentException>("Cannot pass values from another Realm") {
                set.addAll(values)
            }
        }
    }

    override fun retainAll() {
        // Test with managed realm objects
        managedTester.retainAll()

        // Test with unmanaged realm objects
        val set = initAndAssertEmptySet()
        realm.executeTransaction {
            // Check throws exception when unmanaged values are passed
            assertFailsWith<IllegalArgumentException>("Collection with unmanaged objects not permitted") {
                set.retainAll(unmanagedInitializedSet)
            }

            if (!nullable) {
                // Check throws exception when null values are passed
                assertFailsWith<java.lang.NullPointerException>("Collections with nulls are not permitted") {
                    set.retainAll(listOf(null))
                }
            }
        }

        // Test with objects from another realm
        accessTransactionRealmInLooperThread { looperRealm ->
            val values = insertObjects(looperRealm, unmanagedInitializedSet)

            assertFailsWith<IllegalArgumentException>("Cannot pass values from another Realm") {
                set.retainAll(values)
            }
        }
    }

    override fun removeAll() {
        // Test with managed realm objects
        managedTester.removeAll()

        // Test with unmanaged realm objects
        val set = initAndAssertEmptySet()
        realm.executeTransaction {
            // Check throws exception when unmanaged values are passed
            assertFailsWith<IllegalArgumentException>("Collection with unmanaged objects not permitted") {
                set.removeAll(unmanagedInitializedSet)
            }
            if (!nullable) {
                // Check throws exception when null values are passed
                assertFailsWith<java.lang.NullPointerException>("Collections with nulls are not permitted") {
                    set.removeAll(listOf(null))
                }
            }
        }

        // Test with objects from another realm
        accessTransactionRealmInLooperThread { looperRealm ->
            val values = insertObjects(looperRealm, unmanagedInitializedSet)

            assertFailsWith<IllegalArgumentException>("Cannot pass values from another Realm") {
                set.removeAll(values)
            }
        }
    }

    override fun clear() = managedTester.clear()

    override fun freeze() = managedTester.freeze()

    override fun setters() {
        managedTester.setters()

        accessTransactionRealmInLooperThread { looperRealm ->
            val alternativeObject = createAllTypesManagedContainerAndAssert(looperRealm, "alternativeObject", true)
            val alternativeSet = RealmSet<T>().init(managedInitializedSet)

            assertFailsWith<IllegalArgumentException>("Cannot pass values from another Realm") {
                setSetter(alternativeObject, alternativeSet)
            }
        }
    }

    override fun addRealmChangeListener() = Unit

    override fun addSetChangeListener() = Unit

    override fun removeSetChangeListener() = Unit

    override fun removeRealmChangeListener() = Unit

    override fun hasListeners() = Unit

    override fun aggregations() {
        if (realmAnyType == null) {
            // Aggregations on RealmAny type are not supported
            val set = initAndAssertEmptySet()

            realm.executeTransaction {
                set.addAll(managedInitializedSet)
            }

            assertEquals(VALUE_AGE_HELLO, set.min(DogPrimaryKey.AGE))
            assertEquals(VALUE_AGE_BYE, set.max(DogPrimaryKey.AGE))
            assertEquals((VALUE_AGE_HELLO + VALUE_AGE_BYE) / 2.toDouble(), set.average(DogPrimaryKey.AGE))
            assertEquals(VALUE_AGE_HELLO + VALUE_AGE_BYE, set.sum(DogPrimaryKey.AGE))
            assertEquals(VALUE_BIRTHDAY_HELLO, set.minDate(DogPrimaryKey.BIRTHDAY))
            assertEquals(VALUE_BIRTHDAY_BYE, set.maxDate(DogPrimaryKey.BIRTHDAY))

            // Delete all should clear the set and remove all objects from the set
            realm.executeTransaction {
                set.addAll(managedInitializedSet)
                assertEquals(managedInitializedSet.size, set.size)
                set.deleteAllFromRealm()
                assertTrue(set.isEmpty())

                for (element in managedInitializedSet) {
                    assertFalse(RealmObject.isValid(element as RealmModel))
                }
            }
        } else {
            // Aggregations on RealmAny type are not supported
            managedTester.aggregations()
        }
    }

    private fun accessTransactionRealmInLooperThread(block: (looperRealm: Realm) -> Unit) {
        // Test with objects from another realm
        looperThread.runBlocking {
            Realm.getInstance(realm.configuration).use { looperRealm ->
                looperRealm.executeTransaction {
                    block(looperRealm)
                }
            }
            looperThread.testComplete()
        }
    }
}
