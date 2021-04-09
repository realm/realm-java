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
import io.realm.entities.DogPrimaryKey
import io.realm.entities.Owner
import io.realm.entities.SetContainerClass
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import io.realm.rule.BlockingLooperThread
import org.bson.types.Decimal128
import org.bson.types.ObjectId
import java.nio.ByteBuffer
import java.util.*
import kotlin.reflect.KFunction1
import kotlin.reflect.KFunction2
import kotlin.reflect.KProperty1
import kotlin.test.*

/**
 * Tester for realm models without PK. The motivation of these tests is to show that RealmSets distinguish between two
 * objects by their internal key, no matter if all fields match they will be treated as different objects. This conflicts
 * with the equalsTo implementation in Java.
 *
 * Also this would help to catch if there is any regression or change in the core implementation for Sets.
 */
class NoPKRealmModelSetTester<T : RealmModel>(
        private val testerName: String,
        private val setGetter: KFunction1<AllTypes, RealmSet<T>>,
        private val setSetter: KFunction2<AllTypes, RealmSet<T>, Unit>,
        private val managedSetGetter: KProperty1<SetContainerClass, RealmSet<T>>,
        private val managedCollectionGetter: KProperty1<SetContainerClass, RealmList<T>>,
        private val initializedSet: List<T?>,
        private val notPresentValue: T,
        private val toArrayManaged: ToArrayManaged<T>,
        private val nullable: Boolean = false
) : SetTester {

    private lateinit var config: RealmConfiguration
    private lateinit var looperThread: BlockingLooperThread
    private lateinit var realm: Realm
    private lateinit var managedInitializedSet: List<T?>
    private lateinit var managedNotPresentValue: T

    override fun toString(): String = "NoPKRealmModelSetTester-${testerName}"

    override fun setUp(config: RealmConfiguration, looperThread: BlockingLooperThread) {
        this.config = config
        this.looperThread = looperThread
        this.realm = Realm.getInstance(config)

        this.realm.executeTransaction { transactionRealm ->
            this.managedInitializedSet = transactionRealm.copyToRealm(initializedSet)
            this.managedNotPresentValue = transactionRealm.copyToRealm(notPresentValue)
        }

    }

    override fun tearDown() = realm.close()

    override fun isManaged() = assertTrue(initAndAssertEmptySet().isManaged)

    override fun isValid() = assertTrue(initAndAssertEmptySet().isValid)

    override fun isFrozen() = Unit          // Tested in frozen

    override fun size() {
        val set = initAndAssertEmptySet()

        realm.executeTransaction {
            initializedSet.forEach { value ->
                set.add(value)
            }
        }
        assertEquals(initializedSet.size, set.size)
    }

    override fun isEmpty() {
        val set = initAndAssertEmptySet()

        realm.executeTransaction {
            initializedSet.forEach { value ->
                set.add(value)
            }
        }
        assertFalse(set.isEmpty())
    }

    override fun putRequired() = Unit // Not tested

    override fun contains() {
        val set = initAndAssertEmptySet()

        managedInitializedSet.forEach { value ->
            assertFalse(set.contains(value))
        }
        realm.executeTransaction {
            set.addAll(initializedSet)
        }
        // All objects without a PK are different
        managedInitializedSet.forEach { value ->
            assertFalse(set.contains(value))
        }
        assertFalse(set.contains(managedNotPresentValue))
    }

    override fun iterator() {
        val set = initAndAssertEmptySet()

        assertNotNull(set.iterator())
        realm.executeTransaction {
            set.addAll(initializedSet)

            // Objects without PK are different
            managedInitializedSet.forEach { value ->
                assertFalse(set.contains(value))
            }
        }
    }

    override fun copyToRealm() = Unit // Not tested

    override fun copyToRealmOrUpdate() = Unit // Not tested

    override fun toArray() {
        val set = initAndAssertEmptySet()

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
        val set = initAndAssertEmptySet()
        toArrayManaged.assertToArrayWithParameter(realm, set, initializedSet)
    }

    override fun add() {
        val set = initAndAssertEmptySet()

        realm.executeTransaction {
            // Adding a value for the first time returns true
            initializedSet.forEach { value ->
                assertTrue(set.add(value))
            }
            // Adding an existing value returns false
            initializedSet.forEach { value ->
                assertTrue(set.add(value))
            }

            // Validate that for the Realm set all objects without PK are different
            assertFalse(set.containsAll(managedInitializedSet))
        }
    }

    override fun remove() {
        val set = initAndAssertEmptySet()

        realm.executeTransaction {
            set.addAll(initializedSet)

            // Validate that all objects without pk are different
            managedInitializedSet.forEach { value ->
                assertFalse(set.remove(value))
            }
            assertEquals(initializedSet.size, set.size)

            // Does not change if we remove something that is not there
            assertFalse(set.remove(managedNotPresentValue))
        }
    }

    override fun containsAll() {
        val set = initAndAssertEmptySet()

        realm.executeTransaction { transactionRealm ->
            set.addAll(initializedSet)

            // Contains an unmanaged collection
            assertFalse(set.containsAll(managedInitializedSet))

            // Does not contain an unmanaged collection
            assertFalse(set.containsAll(listOf(managedNotPresentValue)))

            // Contains a managed set (itself)
            assertTrue(set.containsAll(set))

            // Contains an empty collection - every set contains the empty set
            assertTrue(set.containsAll(listOf()))
        }
    }

    override fun addAll() {
        val set = initAndAssertEmptySet()

        realm.executeTransaction { _ ->
            // Changes after adding collection
            assertTrue(set.addAll(initializedSet))
            assertEquals(initializedSet.size, set.size)

            // Changes if we add the data again
            assertTrue(set.addAll(initializedSet))
            assertEquals(initializedSet.size * 2, set.size)

            // Does not change if we add itself to it
            assertFalse(set.addAll(set))
            assertEquals(initializedSet.size * 2, set.size)

            // Does not change if we add an empty collection
            assertFalse(set.addAll(listOf()))
            assertEquals(initializedSet.size * 2, set.size)
        }
    }

    override fun retainAll() {
        val set = initAndAssertEmptySet()

        realm.executeTransaction { transactionRealm ->
            // Does not change after empty set intersects with another collection
            assertFalse(set.retainAll(managedInitializedSet))
            assertTrue(set.isEmpty())

            // Does not change after empty set intersects with empty collection
            assertFalse(set.retainAll(listOf()))
            assertTrue(set.isEmpty())

            // Does change after adding data and intersecting it with some values
            set.addAll(initializedSet)
            assertEquals(initializedSet.size, set.size)
            assertTrue(set.retainAll(managedInitializedSet))
            assertEquals(0, set.size)
        }
    }

    override fun removeAll() {
        // FIXME: add cases for managed lists just as we do in containsAll
        val set = initAndAssertEmptySet()

        realm.executeTransaction { transactionRealm ->
            // Does not change after removing a some values from an empty set
            assertTrue(set.isEmpty())
            assertFalse(set.removeAll(managedInitializedSet))
            assertTrue(set.isEmpty())

            // Does not change after adding values and then remove all
            set.addAll(initializedSet)
            assertEquals(initializedSet.size, set.size)
            assertFalse(set.removeAll(managedInitializedSet))
            assertTrue(!set.isEmpty())

            // Does not change after removing empty collection
            assertFalse(set.removeAll(listOf()))
            assertEquals(initializedSet.size, set.size)
        }
    }

    override fun clear() {
        val set = initAndAssertEmptySet()

        realm.executeTransaction {
            set.add(notPresentValue)
            assertEquals(1, set.size)
            set.clear()
            assertEquals(0, set.size)
        }
    }

    override fun freeze() {
        val set = initAndAssertEmptySet()

        realm.executeTransaction {
            set.addAll(initializedSet)
        }

        val frozenSet = set.freeze()
        assertFalse(set.isFrozen)
        assertTrue(frozenSet.isFrozen)
        assertEquals(set.size, frozenSet.size)
    }

    override fun setters() = Unit // Not tested

    //----------------------------------
    // Private stuff
    //----------------------------------

    private fun initAndAssertEmptySet(realm: Realm = this.realm): RealmSet<T> {
        val allTypesObject = createAllTypesManagedContainerAndAssert(realm)
        assertNotNull(allTypesObject)
        val set = setGetter.call(allTypesObject)
        assertTrue(set.isEmpty())
        return set
    }
}


