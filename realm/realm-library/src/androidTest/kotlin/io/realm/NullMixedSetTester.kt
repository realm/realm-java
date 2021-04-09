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
import io.realm.rule.BlockingLooperThread
import java.util.*
import kotlin.reflect.KFunction1
import kotlin.test.*

/**
 * Tester for Mixed containing values.
 */
class NullMixedSetTester(
        private val testerName: String,
        private val setGetter: KFunction1<AllTypes, RealmSet<Mixed>>
) : SetTester {
    private lateinit var config: RealmConfiguration
    private lateinit var looperThread: BlockingLooperThread
    private lateinit var realm: Realm

    override fun toString(): String = "NoPKRealmModelSetTester-${testerName}"

    override fun setUp(config: RealmConfiguration, looperThread: BlockingLooperThread) {
        this.config = config
        this.looperThread = looperThread
        this.realm = Realm.getInstance(config)
    }

    override fun tearDown() = realm.close()

    override fun isManaged() = assertTrue(initAndAssertEmptySet().isManaged)

    override fun isValid() = assertTrue(initAndAssertEmptySet().isValid)

    override fun isFrozen() = Unit // Tested in frozen

    override fun copyToRealm() = Unit // Not applicable

    override fun copyToRealmOrUpdate() = Unit // Not applicable

    override fun size() {
        val set = initAndAssertEmptySet()

        realm.executeTransaction {
            set.add(Mixed.nullValue())
            set.add(Mixed.nullValue())
        }

        // Null values are all the same
        assertEquals(1, set.size)
    }

    override fun isEmpty() {
        val set = initAndAssertEmptySet()

        assertTrue(set.isEmpty())

        realm.executeTransaction {
            set.add(Mixed.nullValue())
        }

        // Null value counts as an item
        assertFalse(set.isEmpty())
    }

    override fun contains() {
        val set = initAndAssertEmptySet()

        assertFalse(set.contains(Mixed.nullValue()))
        assertFalse(set.contains(Mixed.nullValue()))
    }

    override fun putRequired() = Unit // Not tested

    override fun iterator() = Unit // Not tested

    override fun toArray() = Unit // Not tested

    override fun toArrayWithParameter() = Unit // Not tested

    override fun add() {
        val set = initAndAssertEmptySet()

        realm.executeTransaction {
            assertTrue(set.add(Mixed.nullValue()))
            assertTrue(set.contains(Mixed.nullValue()))

            // Should no be possible to add the same value
            assertFalse(set.add(Mixed.nullValue()))
        }
    }

    override fun remove() {
        val set = initAndAssertEmptySet()

        realm.executeTransaction {
            set.add(Mixed.nullValue())
            assertEquals(1, set.size)

            // Does not change if we remove something that is not there
            assertFalse(set.remove(Mixed.valueOf("Hello world")))
            assertEquals(1, set.size)

            // Changes if we remove something that is there
            set.remove(Mixed.nullValue())
            assertEquals(0, set.size)
        }
    }

    override fun containsAll() {
        val set = initAndAssertEmptySet()

        realm.executeTransaction {
            assertFalse(set.containsAll(listOf(Mixed.nullValue(), Mixed.nullValue())))

            set.add(Mixed.nullValue())

            // Contains any null value
            assertTrue(set.containsAll(listOf(Mixed.nullValue(), Mixed.nullValue())))

            // Contains a managed set (itself)
            assertTrue(set.containsAll(set))

            // Contains an empty collection - every set contains the empty set
            assertTrue(set.containsAll(listOf()))
        }
    }

    override fun addAll() {
        val set = initAndAssertEmptySet()

        realm.executeTransaction {
            // Changes after adding collection
            assertTrue(set.addAll(listOf(Mixed.nullValue(), Mixed.nullValue())))
            assertEquals(1, set.size)

            // Does not changes if we add the data again
            assertFalse(set.addAll(listOf(Mixed.nullValue(), Mixed.nullValue())))
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
        val set = initAndAssertEmptySet()

        realm.executeTransaction {
            // Does not change after empty set intersects with another collection
            assertFalse(set.retainAll(listOf(Mixed.nullValue(), Mixed.nullValue())))
            assertTrue(set.isEmpty())

            // Does not change after empty set intersects with empty collection
            assertFalse(set.retainAll(listOf()))
            assertTrue(set.isEmpty())

            // Does change after adding data and intersecting it with some values
            set.addAll(listOf(Mixed.nullValue(), Mixed.nullValue()))
            assertEquals(1, set.size)
            assertTrue(set.retainAll(listOf(Mixed.valueOf("Hello world"))))
            assertEquals(0, set.size)
        }
    }

    override fun removeAll() {
        val set = initAndAssertEmptySet()

        realm.executeTransaction {
            // Does not change after removing a some values from an empty set
            assertTrue(set.isEmpty())
            assertFalse(set.removeAll(listOf(Mixed.nullValue(), Mixed.nullValue())))
            assertTrue(set.isEmpty())

            // Does change after adding values and then remove all
            set.addAll(listOf(Mixed.nullValue(), Mixed.nullValue()))
            assertEquals(1, set.size)
            assertTrue(set.removeAll(listOf(Mixed.nullValue(), Mixed.nullValue())))
            assertTrue(set.isEmpty())

            // Does not change after removing empty collection
            set.addAll(listOf(Mixed.nullValue(), Mixed.nullValue()))
            assertFalse(set.removeAll(listOf()))
            assertEquals(1, set.size)
        }
    }

    override fun clear() {
        val set = initAndAssertEmptySet()

        realm.executeTransaction {
            set.add(Mixed.nullValue())
            assertEquals(1, set.size)
            set.clear()
            assertEquals(0, set.size)
        }
    }

    override fun freeze() = Unit // Not tested

    override fun setters() = Unit // Not tested

    //----------------------------------
    // Private stuff
    //----------------------------------

    private fun initAndAssertEmptySet(realm: Realm = this.realm): RealmSet<Mixed> {
        val allTypesObject = createAllTypesManagedContainerAndAssert(realm)
        assertNotNull(allTypesObject)
        val set = setGetter.call(allTypesObject)
        assertTrue(set.isEmpty())
        return set
    }
}
