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

import io.realm.entities.SetAllTypes
import io.realm.rule.BlockingLooperThread
import java.util.*
import kotlin.reflect.KFunction1
import kotlin.test.*

/**
 * Tester for RealmAny containing null values. It evaluates that null is accepted as a value and that only a null instance
 * can exist in a RealmSet.
 */
class NullRealmAnySetTester(
    private val testerName: String,
    private val setGetter: KFunction1<SetAllTypes, RealmSet<RealmAny>>
) : SetTester {
    private lateinit var config: RealmConfiguration
    private lateinit var looperThread: BlockingLooperThread
    private lateinit var realm: Realm

    override fun toString(): String = "NullRealmAnySetTester-${testerName}"

    override fun setUp(config: RealmConfiguration, looperThread: BlockingLooperThread) {
        this.config = config
        this.looperThread = looperThread
        this.realm = Realm.getInstance(config)
    }

    override fun tearDown() = realm.close()

    override fun isManaged() = assertTrue(initAndAssertEmptySet(id = "id").isManaged)

    override fun isValid() = assertTrue(initAndAssertEmptySet(id = "id").isValid)

    override fun isFrozen() = Unit // Tested in frozen

    override fun insert() = Unit // Not applicable

    override fun insertList() = Unit // Not applicable

    override fun insertOrUpdate() = Unit // Not applicable

    override fun insertOrUpdateList() = Unit // Not applicable

    override fun copyToRealm() = Unit // Not applicable

    override fun copyToRealmOrUpdate() = Unit // Not applicable

    override fun size() {
        val set = initAndAssertEmptySet(id = "id")

        realm.executeTransaction {
            set.add(RealmAny.nullValue())
            set.add(RealmAny.nullValue())
        }

        // Null values are all the same
        assertEquals(1, set.size)
    }

    override fun isEmpty() {
        val set = initAndAssertEmptySet(id = "id")

        assertTrue(set.isEmpty())

        realm.executeTransaction {
            set.add(RealmAny.nullValue())
        }

        // Null value counts as an item
        assertFalse(set.isEmpty())
    }

    override fun contains() {
        val set = initAndAssertEmptySet(id = "id")

        assertFalse(set.contains(RealmAny.nullValue()))
        assertFalse(set.contains(RealmAny.nullValue()))
    }

    override fun requiredConstraints() = Unit // Not tested

    override fun iterator() = Unit // Not tested

    override fun toArray() = Unit // Not tested

    override fun toArrayWithParameter() = Unit // Not tested

    override fun add() {
        val set = initAndAssertEmptySet(id = "id")

        realm.executeTransaction {
            assertTrue(set.add(RealmAny.nullValue()))
            assertTrue(set.contains(RealmAny.nullValue()))

            // Should no be possible to add the same value
            assertFalse(set.add(RealmAny.nullValue()))
        }
    }

    override fun remove() {
        val set = initAndAssertEmptySet(id = "id")

        realm.executeTransaction {
            set.add(RealmAny.nullValue())
            assertEquals(1, set.size)

            // Does not change if we remove something that is not there
            assertFalse(set.remove(RealmAny.valueOf("Hello world")))
            assertEquals(1, set.size)

            // Changes if we remove something that is there
            set.remove(RealmAny.nullValue())
            assertEquals(0, set.size)
        }
    }

    override fun containsAll() {
        val set = initAndAssertEmptySet(id = "id")

        realm.executeTransaction {
            assertFalse(set.containsAll(listOf(RealmAny.nullValue(), RealmAny.nullValue())))

            set.add(RealmAny.nullValue())

            // Contains any null value
            assertTrue(set.containsAll(listOf(RealmAny.nullValue(), RealmAny.nullValue())))

            // Contains a managed set (itself)
            assertTrue(set.containsAll(set))

            // Contains an empty collection - every set contains the empty set
            assertTrue(set.containsAll(listOf()))
        }
    }

    override fun addAll() {
        val set = initAndAssertEmptySet(id = "id")

        realm.executeTransaction {
            // Changes after adding collection
            assertTrue(set.addAll(listOf(RealmAny.nullValue(), RealmAny.nullValue())))
            assertEquals(1, set.size)

            // Does not changes if we add the data again
            assertFalse(set.addAll(listOf(RealmAny.nullValue(), RealmAny.nullValue())))
            assertEquals(1, set.size)

            // Does not change if we add itself to it
            assertFalse(set.addAll(set))
            assertEquals(1, set.size)

            // Does not change if we add an empty collection
            assertFalse(set.addAll(listOf()))
            assertEquals(1, set.size)
        }
    }

    override fun retainAll() {
        val set = initAndAssertEmptySet(id = "id")

        realm.executeTransaction {
            // Does not change after empty set intersects with another collection
            assertFalse(set.retainAll(listOf(RealmAny.nullValue(), RealmAny.nullValue())))
            assertTrue(set.isEmpty())

            // Does not change after empty set intersects with empty collection
            assertFalse(set.retainAll(listOf()))
            assertTrue(set.isEmpty())

            // Does change after adding data and intersecting it with some values
            set.addAll(listOf(RealmAny.nullValue(), RealmAny.nullValue()))
            assertEquals(1, set.size)
            assertTrue(set.retainAll(listOf(RealmAny.valueOf("Hello world"))))
            assertEquals(0, set.size)
        }
    }

    override fun removeAll() {
        val set = initAndAssertEmptySet(id = "id")

        realm.executeTransaction {
            // Does not change after removing a some values from an empty set
            assertTrue(set.isEmpty())
            assertFalse(set.removeAll(listOf(RealmAny.nullValue(), RealmAny.nullValue())))
            assertTrue(set.isEmpty())

            // Does change after adding values and then remove all
            set.addAll(listOf(RealmAny.nullValue(), RealmAny.nullValue()))
            assertEquals(1, set.size)
            assertTrue(set.removeAll(listOf(RealmAny.nullValue(), RealmAny.nullValue())))
            assertTrue(set.isEmpty())

            // Does not change after removing empty collection
            set.addAll(listOf(RealmAny.nullValue(), RealmAny.nullValue()))
            assertFalse(set.removeAll(listOf()))
            assertEquals(1, set.size)
        }
    }

    override fun clear() {
        val set = initAndAssertEmptySet(id = "id")

        realm.executeTransaction {
            set.add(RealmAny.nullValue())
            assertEquals(1, set.size)
            set.clear()
            assertEquals(0, set.size)
        }
    }

    override fun freeze() = Unit // Not tested

    override fun setters() = Unit // Not tested

    override fun dynamic() = Unit // Not tested

    override fun addRealmChangeListener() = Unit

    override fun removeSetChangeListener() = Unit

    override fun removeRealmChangeListener() = Unit

    override fun addSetChangeListener() = Unit

    override fun hasListeners() = Unit

    override fun aggregations() = Unit // Not tested

    //----------------------------------
    // Private stuff
    //----------------------------------

    private fun initAndAssertEmptySet(
        realm: Realm = this.realm,
        id: String? = null
    ): RealmSet<RealmAny> {
        val allTypesObject = createAllTypesManagedContainerAndAssert(realm, id)
        assertNotNull(allTypesObject)
        val set = setGetter.call(allTypesObject)
        assertTrue(set.isEmpty())
        return set
    }
}
