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
import io.realm.entities.SetContainerClass
import io.realm.kotlin.createObject
import io.realm.rule.BlockingLooperThread
import java.lang.IllegalArgumentException
import kotlin.reflect.KFunction1
import kotlin.reflect.KFunction2
import kotlin.reflect.KProperty1
import kotlin.test.*

/**
 * Generic tester for Realm models types of managed sets.
 *
 * It delegates the validation of managed realm models to the ManagedSetTester class, as it will validate all the paths,
 * whereas in this test we validate Realm models specific cases.
 */
class RealmModelManagedSetTester<T : RealmModel>(
        private val testerName: String,
        private val mixedType: MixedType? = null,
        private val setGetter: KFunction1<AllTypes, RealmSet<T>>,
        private val setSetter: KFunction2<AllTypes, RealmSet<T>, Unit>,
        private val managedSetGetter: KProperty1<SetContainerClass, RealmSet<T>>,
        private val managedCollectionGetter: KProperty1<SetContainerClass, RealmList<T>>,
        private val unmanagedInitializedSet: List<T?>,
        private val unmanagedNotPresentValue: T,
        private val toArrayManaged: ToArrayManaged<T>
) : SetTester {

    private lateinit var managedTester: ManagedSetTester<T>
    private lateinit var config: RealmConfiguration
    private lateinit var looperThread: BlockingLooperThread
    private lateinit var realm: Realm
    private lateinit var managedInitializedSet: MutableList<T>
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

        realm.executeTransaction {
            managedInitializedSet = realm.copyToRealmOrUpdate(unmanagedInitializedSet)
            managedNotPresentValue = realm.copyToRealmOrUpdate(unmanagedNotPresentValue)
        }

        this.managedTester = ManagedSetTester(
                testerName = testerName,
                mixedType = mixedType,
                setGetter = setGetter,
                setSetter = setSetter,
                managedSetGetter = managedSetGetter,
                managedCollectionGetter = managedCollectionGetter,
                initializedSet = managedInitializedSet,
                notPresentValue = managedNotPresentValue,
                toArrayManaged = toArrayManaged,
                nullable = false
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

    override fun contains() {
        managedTester.contains()

        // Test with unmanaged realm objects
        val set = initAndAssertEmptySet()
        realm.executeTransaction {
            // Check throws exception when unmanaged values are passed
            assertFailsWith<IllegalArgumentException>("Unmanaged objects not permitted") {
                set.contains(unmanagedNotPresentValue)
            }

            assertFailsWith<java.lang.NullPointerException>("Set does not support null values") {
                assertFalse(set.contains(null))
            }
        }

        // Test with objects from another realm
        objectFromOtherThread {
            set.contains(it)
        }
    }

    override fun iterator() = managedTester.iterator()

    override fun toArray() = managedTester.toArray()

    override fun toArrayWithParameter() = managedTester.toArrayWithParameter()

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

        // Test with objects from another realm
        objectFromOtherThread {
            set.add(it)
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


        // Test with objects from another realm
        objectFromOtherThread {
            set.remove(it)
        }
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

            // Checks it does not contain nulls
            assertFailsWith<java.lang.NullPointerException>("Set does not support null values") {
                assertFalse(set.containsAll(listOf(null)))
            }
        }


        // Test with objects from another realm
        objectsFromOtherThread {
            set.containsAll(it)
        }
    }

    override fun addAll() {
        // Test with managed objects
        managedTester.addAll()

        val set = initAndAssertEmptySet()
        realm.executeTransaction { transactionRealm ->
            // Changes after adding collection
            assertFailsWith<java.lang.NullPointerException>("Cannot add null values into this set") {
                set.addAll(listOf(null))
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
            val differentValuesManagedList = managedCollectionGetter.call(transactionRealm.createObject<SetContainerClass>())
            differentValuesManagedList.addAll(listOf(unmanagedNotPresentValue))
            assertTrue(set.addAll(differentValuesManagedList))
            assertTrue(set.containsAll(differentValuesManagedList))

            // Does not change after adding an empty managed list
            set.clear()
            assertTrue(set.addAll(unmanagedInitializedSet))
            val emptyValuesManagedList = managedCollectionGetter.call(transactionRealm.createObject<SetContainerClass>())
            assertFalse(set.addAll(emptyValuesManagedList))
            assertEquals(unmanagedInitializedSet.size, set.size)

            // Fails if passed null according to Java Set interface
            assertFailsWith<NullPointerException> {
                set.addAll(TestHelper.getNull())
            }
        }


        // Test with objects from another realm
        objectsFromOtherThread {
            set.addAll(it)
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

            // Check throws exception when null values are passed
            assertFailsWith<java.lang.NullPointerException>("Collections with nulls are not permitted") {
                set.retainAll(listOf(null))
            }
        }


        // Test with objects from another realm
        objectsFromOtherThread {
            set.retainAll(it)
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

            // Check throws exception when null values are passed
            assertFailsWith<java.lang.NullPointerException>("Collections with nulls are not permitted") {
                set.removeAll(listOf(null))
            }
        }

        // Test with objects from another realm
        objectsFromOtherThread {
            set.removeAll(it)
        }
    }

    override fun clear() = managedTester.clear()

    override fun freeze() = managedTester.freeze()

    override fun setters() {
        managedTester.setters()

        // Test with objects from another realm
        looperThread.runBlocking {
            Realm.getInstance(realm.configuration).use { alternativeRealm ->
                val alternativeObject = createAllTypesManagedContainerAndAssert(alternativeRealm, "alternativeObject")

                alternativeRealm.executeTransaction {
                    val alternativeSet = RealmSet<T>().init(managedInitializedSet)

                    assertFailsWith<IllegalArgumentException>("Cannot pass values from another Realm") {
                        setSetter(alternativeObject, alternativeSet)
                    }
                }
            }
            looperThread.testComplete()
        }
    }

    private fun objectsFromOtherThread(block: (values: List<T?>) -> Unit) {
        // Test with objects from another realm
        looperThread.runBlocking {
            Realm.getInstance(realm.configuration).use { alternativeRealm ->
                alternativeRealm.executeTransaction {
                    val values: List<T?> = alternativeRealm.copyToRealmOrUpdate(unmanagedInitializedSet)

                    assertFailsWith<IllegalArgumentException>("Cannot pass an object from another Realm instance") {
                        block(values)
                    }
                }
            }
            looperThread.testComplete()
        }
    }

    private fun objectFromOtherThread(block: (values: T) -> Unit) {
        // Test with objects from another realm
        looperThread.runBlocking {
            Realm.getInstance(realm.configuration).use { alternativeRealm ->
                alternativeRealm.executeTransaction {
                    val value = alternativeRealm.copyToRealmOrUpdate(unmanagedNotPresentValue)

                    assertFailsWith<IllegalArgumentException>("Cannot pass an object from another Realm instance") {
                        block(value)
                    }
                }
            }
            looperThread.testComplete()
        }
    }
}
