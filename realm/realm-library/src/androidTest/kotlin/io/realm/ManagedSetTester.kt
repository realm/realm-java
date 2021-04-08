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
import java.util.*
import kotlin.reflect.KFunction1
import kotlin.reflect.KFunction2
import kotlin.reflect.KProperty1
import kotlin.test.*

/**
 * Generic tester for all types of unmanaged sets.
 */
class ManagedSetTester<T : Any>(
        private val testerName: String,
        private val mixedType: MixedType? = null,
        private val setGetter: KFunction1<AllTypes, RealmSet<T>>,
        private val setSetter: KFunction2<AllTypes, RealmSet<T>, Unit>,
        private val managedSetGetter: KProperty1<SetContainerClass, RealmSet<T>>,
        private val managedCollectionGetter: KProperty1<SetContainerClass, RealmList<T>>,
        private val initializedSet: List<T?>,
        private val notPresentValue: T,
        private val toArrayManaged: ToArrayManaged<T>,
        private val nullable: Boolean = true
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

    override fun contains() {
        val set = initAndAssertEmptySet()

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
        val set = initAndAssertEmptySet()

        assertNotNull(set.iterator())
        realm.executeTransaction {
            set.addAll(initializedSet)
        }

        initializedSet.forEach { value ->
            assertTrue(set.contains(value))
        }
    }

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
                assertFalse(set.add(value))
            }
        }

        assertTrue(set.containsAll(initializedSet))
    }

    override fun remove() {
        val set = initAndAssertEmptySet()

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
        val set = initAndAssertEmptySet()

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
            val differentValuesManagedList = managedCollectionGetter.call(transactionRealm.createObject<SetContainerClass>())
            differentValuesManagedList.add(notPresentValue)
            assertFalse(set.containsAll(differentValuesManagedList))

            // Contains an empty managed list
            val emptyValuesManagedList = managedCollectionGetter.call(transactionRealm.createObject<SetContainerClass>())
            assertTrue(set.containsAll(emptyValuesManagedList))

            // TODO: it's not possible to test passing a null value from Kotlin, even if using
            //  TestHelper.getNull(). It seems that Kotlin generates different bytecode when the
            //  parameter to the function is a generics collection with an upper bound.
            //  The only way to test this is by writing a Java test instead.
        }
    }

    override fun addAll() {
        val set = initAndAssertEmptySet()

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
            val differentValuesManagedList = managedCollectionGetter.call(transactionRealm.createObject<SetContainerClass>())
            differentValuesManagedList.addAll(listOf(notPresentValue))
            assertTrue(set.addAll(differentValuesManagedList))
            assertTrue(set.containsAll(differentValuesManagedList))

            // Does not change after adding an empty managed list
            set.clear()
            assertTrue(set.addAll(initializedSet))
            val emptyValuesManagedList = managedCollectionGetter.call(transactionRealm.createObject<SetContainerClass>())
            assertFalse(set.addAll(emptyValuesManagedList))
            assertEquals(initializedSet.size, set.size)

            // Fails if passed null according to Java Set interface
            assertFailsWith<NullPointerException> {
                set.addAll(TestHelper.getNull())
            }
        }
    }

    override fun copyToRealm() {
        // Instantiate container and set dictionary on container
        val manualInstance = AllTypes().apply {
            setSetter.call(this, initializedSet)
        }

        // Copy to Realm
        realm.executeTransaction {
            val allTypesObject = realm.copyToRealm(manualInstance)
            assertNotNull(allTypesObject)
        }

        // Get set from container from Realm
        val allTypesObject = realm.where<AllTypes>().findFirst()
        assertNotNull(allTypesObject)
        val set: RealmSet<T> = setGetter.call(allTypesObject)

        assertFalse(set.isEmpty())
        set.forEachIndexed { index, value ->
            assertEquals(initializedSet[index], value)
        }
    }

    override fun retainAll() {
        val set = initAndAssertEmptySet()

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
            val differentValuesManagedList = managedCollectionGetter.call(transactionRealm.createObject<SetContainerClass>())
            differentValuesManagedList.addAll(listOf(notPresentValue))
            assertTrue(set.retainAll(differentValuesManagedList))
            assertTrue(set.isEmpty())

            // Changes after intersection with an empty managed list
            set.clear()
            set.addAll(initializedSet)
            val emptyValuesManagedList = managedCollectionGetter.call(transactionRealm.createObject<SetContainerClass>())
            assertTrue(set.retainAll(emptyValuesManagedList))
            assertTrue(set.isEmpty())

            // TODO: it's not possible to test passing a null value from Kotlin, even if using
            //  TestHelper.getNull(). It seems that Kotlin generates different bytecode when the
            //  parameter to the function is a generics collection with an upper bound.
            //  The only way to test this is by writing a Java test instead.
        }
    }

    override fun removeAll() {
        // FIXME: add cases for managed lists just as we do in containsAll
        val set = initAndAssertEmptySet()

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
            val differentValuesManagedList = managedCollectionGetter.call(transactionRealm.createObject<SetContainerClass>())
            differentValuesManagedList.addAll(listOf(notPresentValue))
            assertFalse(set.removeAll(differentValuesManagedList))
            assertEquals(initializedSet.size, set.size)

            // Does not change after removing an empty managed list
            set.clear()
            set.addAll(initializedSet)
            val emptyValuesManagedList = managedCollectionGetter.call(transactionRealm.createObject<SetContainerClass>())
            assertFalse(set.removeAll(emptyValuesManagedList))
            assertEquals(initializedSet.size, set.size)

            // TODO: it's not possible to test passing a null value from Kotlin, even if using
            //  TestHelper.getNull(). It seems that Kotlin generates different bytecode when the
            //  parameter to the function is a generics collection with an upper bound.
            //  The only way to test this is by writing a Java test instead.
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

    override fun setters() {
        val allFields = createAllTypesManagedContainerAndAssert(realm)
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

fun managedSetFactory(): List<SetTester> {
    val primitiveTesters: List<SetTester> = SetSupportedType.values().mapNotNull { supportedType ->
        when (supportedType) {
            SetSupportedType.LONG ->
                ManagedSetTester<Long>(
                        testerName = "Long",
                        setGetter = AllTypes::getColumnLongSet,
                        setSetter = AllTypes::setColumnLongSet,
                        managedSetGetter = SetContainerClass::myLongSet,
                        managedCollectionGetter = SetContainerClass::myLongList,
                        initializedSet = listOf(VALUE_NUMERIC_HELLO.toLong(), VALUE_NUMERIC_BYE.toLong(), null),
                        notPresentValue = VALUE_NUMERIC_NOT_PRESENT.toLong(),
                        toArrayManaged = ToArrayManaged.LongManaged()
                )
            SetSupportedType.INTEGER ->
                ManagedSetTester<Int>(
                        testerName = "Integer",
                        setGetter = AllTypes::getColumnIntegerSet,
                        setSetter = AllTypes::setColumnIntegerSet,
                        managedSetGetter = SetContainerClass::myIntSet,
                        managedCollectionGetter = SetContainerClass::myIntList,
                        initializedSet = listOf(VALUE_NUMERIC_HELLO, VALUE_NUMERIC_BYE, null),
                        notPresentValue = VALUE_NUMERIC_NOT_PRESENT,
                        toArrayManaged = ToArrayManaged.IntManaged()
                )
            SetSupportedType.SHORT ->
                ManagedSetTester<Short>(
                        testerName = "Short",
                        setGetter = AllTypes::getColumnShortSet,
                        setSetter = AllTypes::setColumnShortSet,
                        managedSetGetter = SetContainerClass::myShortSet,
                        managedCollectionGetter = SetContainerClass::myShortList,
                        initializedSet = listOf(VALUE_NUMERIC_HELLO.toShort(), VALUE_NUMERIC_BYE.toShort(), null),
                        notPresentValue = VALUE_NUMERIC_NOT_PRESENT.toShort(),
                        toArrayManaged = ToArrayManaged.ShortManaged()
                )
            SetSupportedType.BYTE ->
                ManagedSetTester<Byte>(
                        testerName = "Byte",
                        setGetter = AllTypes::getColumnByteSet,
                        setSetter = AllTypes::setColumnByteSet,
                        managedSetGetter = SetContainerClass::myByteSet,
                        managedCollectionGetter = SetContainerClass::myByteList,
                        initializedSet = listOf(VALUE_NUMERIC_HELLO.toByte(), VALUE_NUMERIC_BYE.toByte(), null),
                        notPresentValue = VALUE_NUMERIC_NOT_PRESENT.toByte(),
                        toArrayManaged = ToArrayManaged.ByteManaged()
                )
            SetSupportedType.FLOAT ->
                ManagedSetTester<Float>(
                        testerName = "Float",
                        setGetter = AllTypes::getColumnFloatSet,
                        setSetter = AllTypes::setColumnFloatSet,
                        managedSetGetter = SetContainerClass::myFloatSet,
                        managedCollectionGetter = SetContainerClass::myFloatList,
                        initializedSet = listOf(VALUE_NUMERIC_HELLO.toFloat(), VALUE_NUMERIC_BYE.toFloat(), null),
                        notPresentValue = VALUE_NUMERIC_NOT_PRESENT.toFloat(),
                        toArrayManaged = ToArrayManaged.FloatManaged()
                )
            SetSupportedType.DOUBLE ->
                ManagedSetTester<Double>(
                        testerName = "Double",
                        setGetter = AllTypes::getColumnDoubleSet,
                        setSetter = AllTypes::setColumnDoubleSet,
                        managedSetGetter = SetContainerClass::myDoubleSet,
                        managedCollectionGetter = SetContainerClass::myDoubleList,
                        initializedSet = listOf(VALUE_NUMERIC_HELLO.toDouble(), VALUE_NUMERIC_BYE.toDouble(), null),
                        notPresentValue = VALUE_NUMERIC_NOT_PRESENT.toDouble(),
                        toArrayManaged = ToArrayManaged.DoubleManaged()
                )
            SetSupportedType.STRING ->
                ManagedSetTester<String>(
                        testerName = "String",
                        setGetter = AllTypes::getColumnStringSet,
                        setSetter = AllTypes::setColumnStringSet,
                        managedSetGetter = SetContainerClass::myStringSet,
                        managedCollectionGetter = SetContainerClass::myStringList,
                        initializedSet = listOf(VALUE_STRING_HELLO, VALUE_STRING_BYE, null),
                        notPresentValue = VALUE_STRING_NOT_PRESENT,
                        toArrayManaged = ToArrayManaged.StringManaged()
                )
            SetSupportedType.BOOLEAN ->
                ManagedSetTester<Boolean>(
                        testerName = "Boolean",
                        setGetter = AllTypes::getColumnBooleanSet,
                        setSetter = AllTypes::setColumnBooleanSet,
                        managedSetGetter = SetContainerClass::myBooleanSet,
                        managedCollectionGetter = SetContainerClass::myBooleanList,
                        initializedSet = listOf(VALUE_BOOLEAN_HELLO, null),
                        notPresentValue = VALUE_BOOLEAN_NOT_PRESENT,
                        toArrayManaged = ToArrayManaged.BooleanManaged()
                )
            SetSupportedType.DATE ->
                ManagedSetTester<Date>(
                        testerName = "Date",
                        setGetter = AllTypes::getColumnDateSet,
                        setSetter = AllTypes::setColumnDateSet,
                        managedSetGetter = SetContainerClass::myDateSet,
                        managedCollectionGetter = SetContainerClass::myDateList,
                        initializedSet = listOf(VALUE_DATE_HELLO, VALUE_DATE_BYE, null),
                        notPresentValue = VALUE_DATE_NOT_PRESENT,
                        toArrayManaged = ToArrayManaged.DateManaged()
                )
            SetSupportedType.DECIMAL128 ->
                ManagedSetTester<Decimal128>(
                        testerName = "Decimal128",
                        setGetter = AllTypes::getColumnDecimal128Set,
                        setSetter = AllTypes::setColumnDecimal128Set,
                        managedSetGetter = SetContainerClass::myDecimal128Set,
                        managedCollectionGetter = SetContainerClass::myDecimal128List,
                        initializedSet = listOf(VALUE_DECIMAL128_HELLO, VALUE_DECIMAL128_BYE, null),
                        notPresentValue = VALUE_DECIMAL128_NOT_PRESENT,
                        toArrayManaged = ToArrayManaged.Decimal128Managed()
                )
            SetSupportedType.BINARY ->
                ManagedSetTester<ByteArray>(
                        testerName = "Binary",
                        setGetter = AllTypes::getColumnBinarySet,
                        setSetter = AllTypes::setColumnBinarySet,
                        managedSetGetter = SetContainerClass::myBinarySet,
                        managedCollectionGetter = SetContainerClass::myBinaryList,
                        initializedSet = listOf(VALUE_BINARY_HELLO, VALUE_BINARY_BYE, null),
                        notPresentValue = VALUE_BINARY_NOT_PRESENT,
                        toArrayManaged = ToArrayManaged.BinaryManaged()
                )

            SetSupportedType.OBJECT_ID ->
                ManagedSetTester<ObjectId>(
                        testerName = "ObjectId",
                        setGetter = AllTypes::getColumnObjectIdSet,
                        setSetter = AllTypes::setColumnObjectIdSet,
                        managedSetGetter = SetContainerClass::myObjectIdSet,
                        managedCollectionGetter = SetContainerClass::myObjectIdList,
                        initializedSet = listOf(VALUE_OBJECT_ID_HELLO, VALUE_OBJECT_ID_BYE, null),
                        notPresentValue = VALUE_OBJECT_ID_NOT_PRESENT,
                        toArrayManaged = ToArrayManaged.ObjectIdManaged()
                )

            SetSupportedType.UUID ->
                ManagedSetTester<UUID>(
                        testerName = "UUID",
                        setGetter = AllTypes::getColumnUUIDSet,
                        setSetter = AllTypes::setColumnUUIDSet,
                        managedSetGetter = SetContainerClass::myUUIDSet,
                        managedCollectionGetter = SetContainerClass::myUUIDList,
                        initializedSet = listOf(VALUE_UUID_HELLO, VALUE_UUID_BYE, null),
                        notPresentValue = VALUE_UUID_NOT_PRESENT,
                        toArrayManaged = ToArrayManaged.UUIDManaged()
                )

            SetSupportedType.LINK ->
                RealmModelManagedSetTester<DogPrimaryKey>(
                        testerName = "LINK",
                        setGetter = AllTypes::getColumnRealmModelSet,
                        setSetter = AllTypes::setColumnRealmModelSet,
                        managedSetGetter = SetContainerClass::myRealmModelSet,
                        managedCollectionGetter = SetContainerClass::myRealmModelList,
                        unmanagedInitializedSet = listOf(VALUE_LINK_HELLO, VALUE_LINK_BYE),
                        unmanagedNotPresentValue = VALUE_LINK_NOT_PRESENT,
                        toArrayManaged = ToArrayManaged.RealmModelManaged(),
                        manageObjects = { realm, objects ->
                            realm.copyToRealmOrUpdate(objects)
                        },
                        nullable = false
                )
            // Ignore Mixed in this switch
            else -> null
        }
    }

    return primitiveTesters
            // We add an extra test for models without a PK
            .plus(NoPKRealmModelSetTester<Owner>(
                    testerName = "LINK_NO_PK",
                    setGetter = AllTypes::getColumnRealmModelNoPkSet,
                    setSetter = AllTypes::setColumnRealmModelNoPkSet,
                    managedSetGetter = SetContainerClass::myRealmModelNoPkSet,
                    managedCollectionGetter = SetContainerClass::myRealmModelNoPkList,
                    initializedSet = listOf(VALUE_LINK_NO_PK_HELLO, VALUE_LINK_NO_PK_BYE),
                    notPresentValue = VALUE_LINK_NO_PK_NOT_PRESENT,
                    toArrayManaged = ToArrayManaged.RealmModelNoPKManaged()
            ))
            // Then we add the tests for Mixed types
            .plus(MixedType.values().map { mixedType ->
                when (mixedType) {
                    MixedType.OBJECT -> RealmModelManagedSetTester<Mixed>(
                            testerName = "LINK",
                            setGetter = AllTypes::getColumnMixedSet,
                            setSetter = AllTypes::setColumnMixedSet,
                            managedSetGetter = SetContainerClass::myMixedSet,
                            managedCollectionGetter = SetContainerClass::myMixedList,
                            unmanagedInitializedSet = getMixedKeyValuePairs(mixedType).map {
                                it.second
                            },
                            unmanagedNotPresentValue = Mixed.valueOf(VALUE_LINK_NOT_PRESENT),
                            toArrayManaged = ToArrayManaged.MixedManaged(),
                            manageObjects = { realm, objects ->
                                objects.map { mixed ->
                                    if (mixed?.type == MixedType.OBJECT) {
                                        val unmanagedObject = mixed.asRealmModel(DogPrimaryKey::class.java)
                                        val managedObject = realm.copyToRealmOrUpdate(unmanagedObject)
                                        Mixed.valueOf(managedObject)
                                    } else {
                                        mixed
                                    }
                                }
                            },
                            nullable = true
                    )
                    MixedType.NULL -> NullMixedSetTester(
                            testerName = "MIXED-${mixedType.name}",
                            setGetter = AllTypes::getColumnMixedSet
                    )
                    else -> ManagedSetTester<Mixed>(
                            testerName = "MIXED-${mixedType.name}",
                            mixedType = mixedType,
                            setGetter = AllTypes::getColumnMixedSet,
                            setSetter = AllTypes::setColumnMixedSet,
                            managedSetGetter = SetContainerClass::myMixedSet,
                            managedCollectionGetter = SetContainerClass::myMixedList,
                            initializedSet = getMixedKeyValuePairs(mixedType).map {
                                it.second
                            },
                            notPresentValue = VALUE_MIXED_NOT_PRESENT,
                            toArrayManaged = ToArrayManaged.MixedManaged()
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
        override fun assertToArrayWithParameter(realm: Realm, set: RealmSet<DogPrimaryKey>, values: List<DogPrimaryKey?>) =
                test(realm, set, values, emptyArray(), arrayOf())
    }

    class RealmModelNoPKManaged : ToArrayManaged<Owner>() {
        override fun assertToArrayWithParameter(realm: Realm, set: RealmSet<Owner>, values: List<Owner?>) =
                test(realm, set, values, emptyArray(), arrayOf())
    }

    class MixedManaged : ToArrayManaged<Mixed>() {
        override fun assertToArrayWithParameter(realm: Realm, set: RealmSet<Mixed>, values: List<Mixed?>) =
                test(realm, set, values, emptyArray(), arrayOf())
    }
}

