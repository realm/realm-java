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
        private val initializedSet: List<T?>,
        private val notPresentValue: T,
        private val toArrayManaged: ToArrayManaged<T>
) : SetTester {

    private lateinit var config: RealmConfiguration
    private lateinit var looperThread: BlockingLooperThread
    private lateinit var realm: Realm

    override fun toString(): String = when (mixedType) {
        null -> "ManagedSet-${testerName}"
        else -> "ManagedSet-${testerName}" + mixedType.name.let { "-$it" }
    }

    override fun setUp(config: RealmConfiguration, looperThread: BlockingLooperThread) {
        this.config = config
        this.looperThread = looperThread
        this.realm = Realm.getInstance(config)
    }

    override fun tearDown() = realm.close()

    override fun isManaged() = assertTrue(initAndAssert().isManaged)

    override fun isValid() = assertTrue(initAndAssert().isValid)

    override fun isFrozen() = Unit          // Tested in frozen

    override fun size() {
        val set = initAndAssert()

        realm.executeTransaction {
            initializedSet.forEach { value ->
                set.add(value)
            }
        }
        assertEquals(initializedSet.size, set.size)
    }

    override fun isEmpty() {
        val set = initAndAssert()

        realm.executeTransaction {
            initializedSet.forEach { value ->
                set.add(value)
            }
        }
        assertFalse(set.isEmpty())
    }

    override fun contains() {
        val set = initAndAssert()

        assertFalse(set.contains(notPresentValue))
        realm.executeTransaction {
            set.add(notPresentValue)
        }
        assertTrue(set.contains(notPresentValue))
        assertFalse(set.contains(null))
    }

    override fun iterator() {
        val set = initAndAssert()

        assertNotNull(set.iterator())
        realm.executeTransaction {
            set.addAll(initializedSet)
        }
        set.forEach { value ->
            assertTrue(initializedSet.contains(value))
        }
    }

    override fun toArray() {
        val set = initAndAssert()

        // Empty set
        assertEquals(0, set.toArray().size)

        // Set with some values
        realm.executeTransaction {
            set.addAll(initializedSet)
            assertEquals(initializedSet.size, set.toArray().size)
        }
    }

    override fun toArrayWithParameter() {
        val set = initAndAssert()
        toArrayManaged.toArrayWithParameter(realm, set, initializedSet)
    }

    override fun add() {
        val set = initAndAssert()

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
    }

    override fun remove() {
        val set = initAndAssert()

        realm.executeTransaction {
            initializedSet.forEach { value ->
                set.add(value)
                assertTrue(set.remove(value))
            }
            assertFalse(set.remove(notPresentValue))
        }
        assertEquals(0, set.size)
    }

    override fun containsAll() {
        val set = initAndAssert()

        realm.executeTransaction { transactionRealm ->
            set.addAll(initializedSet)

            // Does not contain a collection of something other than its own type
            assertFalse(set.containsAll(listOf(Pair(1, 2)) as Collection<*>))

            // Contains an unmanaged collection
            assertTrue(set.containsAll(initializedSet))

            // Does not contain an unmanaged collection
            assertFalse(set.containsAll(listOf(notPresentValue)))

            // Contains a managed set (itself)
            assertTrue(set.containsAll(set))

            // Contains an empty collection
            assertTrue(set.containsAll(listOf()))

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

            // TODO: it's not possible to test passing a null value from Kotlin, even if using
            //  TestHelper.getNull(). It seems that Kotlin generates different bytecode when the
            //  parameter to the function is a generics collection with an upper bound.
            //  The only way to test this is by writing a Java test instead.
        }
    }

    override fun addAll() {
        val set = initAndAssert()

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

            // Fails if passed null according to Java Set interface
            assertFailsWith<NullPointerException> {
                set.addAll(TestHelper.getNull())
            }
        }
    }

    override fun retainAll() {
//        val set = initAndAssert()
//
//        realm.executeTransaction { transactionRealm ->
//            // Does not change after empty set intersects with another collection
//            assertFalse(set.retainAll(initializedSet))
//            assertTrue(set.isEmpty())
//
//            // Does not change after adding data and intersecting it with same values
//            set.addAll(initializedSet)
//            assertEquals(initializedSet.size, set.size)
//            assertFalse(set.retainAll(initializedSet))
//            assertEquals(initializedSet.size, set.size)
//
//            // Changes after intersection with empty collection
//            assertTrue(set.retainAll(listOf()))
//            assertTrue(set.isEmpty())
//
//            // Changes after adding data and intersecting it with other values
//            set.addAll(initializedSet)
//            assertEquals(initializedSet.size, set.size)
//            assertTrue(set.retainAll(listOf(notPresentValue)))
//            assertTrue(set.isEmpty())
//
//            // Does not change after intersection with itself
//            set.clear()
//            set.addAll(initializedSet)
//            assertFalse(set.isEmpty())
//            assertFalse(set.retainAll(set))
//            assertEquals(initializedSet.size, set.size)
//
//            // Does not change after intersection with another set containing the same elements
//            set.addAll(initializedSet)
//            val sameValuesManagedSet = managedSetGetter.get(transactionRealm.createObject())
//            assertNotNull(sameValuesManagedSet)
//            sameValuesManagedSet.addAll(initializedSet)
//            assertFalse(set.retainAll(sameValuesManagedSet as Collection<T>))
//            assertEquals(initializedSet.size, set.size)
//
//            // Intersect with a managed set not containing any elements from the original set
//            set.clear()
//            set.addAll(initializedSet)
//            assertEquals(initializedSet.size, set.size)
//            val notPresentValueSet = managedSetGetter.get(transactionRealm.createObject())
//            assertNotNull(notPresentValueSet)
//            notPresentValueSet.add(notPresentValue)
//            assertTrue(set.retainAll(notPresentValueSet as Collection<T>))
//            assertTrue(set.isEmpty())
//
//            // TODO: it's not possible to test passing a null value from Kotlin, even if using
//            //  TestHelper.getNull(). It seems that Kotlin generates different bytecode when the
//            //  parameter to the function is a generics collection with an upper bound.
//            //  The only way to test this is by writing a Java test instead.
//        }
    }

    override fun removeAll() {
        val set = initAndAssert()

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

            // TODO: it's not possible to test passing a null value from Kotlin, even if using
            //  TestHelper.getNull(). It seems that Kotlin generates different bytecode when the
            //  parameter to the function is a generics collection with an upper bound.
            //  The only way to test this is by writing a Java test instead.
        }
    }

    override fun clear() {
        val set = initAndAssert()

        realm.executeTransaction {
            set.add(notPresentValue)
            assertEquals(1, set.size)
            set.clear()
            assertEquals(0, set.size)
        }
    }

    override fun freeze() {
        val set = initAndAssert()

        realm.executeTransaction {
            set.addAll(initializedSet)
        }

        val frozenSet = set.freeze()
        assertFalse(set.isFrozen)
        assertTrue(frozenSet.isFrozen)
        assertEquals(set.size, frozenSet.size)
    }

    //----------------------------------
    // Private stuff
    //----------------------------------

    private fun initAndAssert(realm: Realm = this.realm): RealmSet<T> {
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
//            SetSupportedType.LONG ->
//                UnmanagedSetTester<Long>(
//                        testerName = "Long",
//                        values = listOf(VALUE_NUMERIC_HELLO.toLong(), VALUE_NUMERIC_BYE.toLong(), null),
//                        notPresentValue = VALUE_NUMERIC_NOT_PRESENT.toLong()
//                )

            SetSupportedType.INTEGER ->
                ManagedSetTester<Int>(
                        testerName = "Integer",
                        setGetter = AllTypes::getColumnIntegerSet,
                        setSetter = AllTypes::setColumnIntegerSet,
                        managedSetGetter = SetContainerClass::myIntSet,
                        initializedSet = listOf(VALUE_NUMERIC_HELLO, VALUE_NUMERIC_BYE, null),
                        notPresentValue = VALUE_NUMERIC_NOT_PRESENT,
                        toArrayManaged = ToArrayManaged.IntManaged()
                )

//            SetSupportedType.SHORT ->
//                UnmanagedSetTester<Short>(
//                        testerName = "Short",
//                        values = listOf(VALUE_NUMERIC_HELLO.toShort(), VALUE_NUMERIC_BYE.toShort(), null),
//                        notPresentValue = VALUE_NUMERIC_NOT_PRESENT.toShort()
//                )
//            SetSupportedType.BYTE ->
//                UnmanagedSetTester<Byte>(
//                        testerName = "Byte",
//                        values = listOf(VALUE_NUMERIC_HELLO.toByte(), VALUE_NUMERIC_BYE.toByte(), null),
//                        notPresentValue = VALUE_NUMERIC_NOT_PRESENT.toByte()
//                )
//            SetSupportedType.FLOAT ->
//                UnmanagedSetTester<Float>(
//                        testerName = "Float",
//                        values = listOf(VALUE_NUMERIC_HELLO.toFloat(), VALUE_NUMERIC_BYE.toFloat(), null),
//                        notPresentValue = VALUE_NUMERIC_NOT_PRESENT.toFloat()
//                )
//            SetSupportedType.DOUBLE ->
//                UnmanagedSetTester<Double>(
//                        testerName = "Double",
//                        values = listOf(VALUE_NUMERIC_HELLO.toDouble(), VALUE_NUMERIC_BYE.toDouble(), null),
//                        notPresentValue = VALUE_NUMERIC_NOT_PRESENT.toDouble()
//                )

            SetSupportedType.STRING ->
                ManagedSetTester<String>(
                        testerName = "String",
                        setGetter = AllTypes::getColumnStringSet,
                        setSetter = AllTypes::setColumnStringSet,
                        managedSetGetter = SetContainerClass::myStringSet,
                        initializedSet = listOf(VALUE_STRING_HELLO, VALUE_STRING_BYE, null),
                        notPresentValue = VALUE_STRING_NOT_PRESENT,
                        toArrayManaged = ToArrayManaged.StringManaged()
                )

//            SetSupportedType.BOOLEAN ->
//                UnmanagedSetTester<Boolean>(
//                        testerName = "Boolean",
//                        values = listOf(VALUE_BOOLEAN_HELLO, null),
//                        notPresentValue = VALUE_BOOLEAN_NOT_PRESENT
//                )
//            SetSupportedType.DATE ->
//                UnmanagedSetTester<Date>(
//                        testerName = "Date",
//                        values = listOf(VALUE_DATE_HELLO, VALUE_DATE_BYE, null),
//                        notPresentValue = VALUE_DATE_NOT_PRESENT
//                )
//            SetSupportedType.DECIMAL128 ->
//                UnmanagedSetTester<Decimal128>(
//                        testerName = "Decimal128",
//                        values = listOf(VALUE_DECIMAL128_HELLO, VALUE_DECIMAL128_BYE, null),
//                        notPresentValue = VALUE_DECIMAL128_NOT_PRESENT
//                )
//            SetSupportedType.BINARY ->
//                UnmanagedSetTester<ByteArray>(
//                        testerName = "ByteArray",
//                        values = listOf(VALUE_BINARY_HELLO, VALUE_BINARY_BYE, null),
//                        notPresentValue = VALUE_BINARY_NOT_PRESENT
//                )
//            SetSupportedType.OBJECT_ID ->
//                UnmanagedSetTester<ObjectId>(
//                        testerName = "ObjectId",
//                        values = listOf(VALUE_OBJECT_ID_HELLO, VALUE_OBJECT_ID_BYE, null),
//                        notPresentValue = VALUE_OBJECT_ID_NOT_PRESENT
//                )
//            SetSupportedType.UUID ->
//                UnmanagedSetTester<UUID>(
//                        testerName = "UUID",
//                        values = listOf(VALUE_UUID_HELLO, VALUE_UUID_BYE, null),
//                        notPresentValue = VALUE_UUID_NOT_PRESENT
//                )
//            SetSupportedType.LINK ->
//                UnmanagedSetTester<RealmModel>(
//                        testerName = "UnmanagedRealmModel",
//                        values = listOf(VALUE_LINK_HELLO, VALUE_LINK_BYE, null),
//                        notPresentValue = VALUE_LINK_NOT_PRESENT
//                )
            // Ignore Mixed in this switch
            else -> null
        }
    }

    // Create Mixed testers now
    // TODO

    // Put them together
//    return primitiveTesters.plus(mixedTesters)
    return primitiveTesters
}

/**
 * TODO
 */
abstract class ToArrayManaged<T> {

    abstract fun toArrayWithParameter(realm: Realm, set: RealmSet<T>, values: List<T?>)

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
        override fun toArrayWithParameter(realm: Realm, set: RealmSet<Long>, values: List<Long?>) =
                test(realm, set, values, emptyArray(), arrayOf())
    }

    class IntManaged : ToArrayManaged<Int>() {
        override fun toArrayWithParameter(realm: Realm, set: RealmSet<Int>, values: List<Int?>) =
                test(realm, set, values, emptyArray(), arrayOf())
    }

    class ShortManaged : ToArrayManaged<Short>() {
        override fun toArrayWithParameter(realm: Realm, set: RealmSet<Short>, values: List<Short?>) =
                test(realm, set, values, emptyArray(), arrayOf())
    }

    class ByteManaged : ToArrayManaged<Byte>() {
        override fun toArrayWithParameter(realm: Realm, set: RealmSet<Byte>, values: List<Byte?>) =
                test(realm, set, values, emptyArray(), arrayOf())
    }

    class FloatManaged : ToArrayManaged<Float>() {
        override fun toArrayWithParameter(realm: Realm, set: RealmSet<Float>, values: List<Float?>) =
                test(realm, set, values, emptyArray(), arrayOf())
    }

    class DoubleManaged : ToArrayManaged<Double>() {
        override fun toArrayWithParameter(realm: Realm, set: RealmSet<Double>, values: List<Double?>) =
                test(realm, set, values, emptyArray(), arrayOf())
    }

    class StringManaged : ToArrayManaged<String>() {
        override fun toArrayWithParameter(realm: Realm, set: RealmSet<String>, values: List<String?>) =
                test(realm, set, values, emptyArray(), arrayOf())
    }

    class BooleanManaged : ToArrayManaged<Boolean>() {
        override fun toArrayWithParameter(realm: Realm, set: RealmSet<Boolean>, values: List<Boolean?>) =
                test(realm, set, values, emptyArray(), arrayOf())
    }

    class DateManaged : ToArrayManaged<Date>() {
        override fun toArrayWithParameter(realm: Realm, set: RealmSet<Date>, values: List<Date?>) =
                test(realm, set, values, emptyArray(), arrayOf())
    }

    class Decimal128Managed : ToArrayManaged<Decimal128>() {
        override fun toArrayWithParameter(realm: Realm, set: RealmSet<Decimal128>, values: List<Decimal128?>) =
                test(realm, set, values, emptyArray(), arrayOf())
    }

    class BinaryManaged : ToArrayManaged<ByteArray>() {
        override fun toArrayWithParameter(realm: Realm, set: RealmSet<ByteArray>, values: List<ByteArray?>) =
                test(realm, set, values, emptyArray(), arrayOf())
    }

    class ObjectIdManaged : ToArrayManaged<ObjectId>() {
        override fun toArrayWithParameter(realm: Realm, set: RealmSet<ObjectId>, values: List<ObjectId?>) =
                test(realm, set, values, emptyArray(), arrayOf())
    }

    class UUIDManaged : ToArrayManaged<UUID>() {
        override fun toArrayWithParameter(realm: Realm, set: RealmSet<UUID>, values: List<UUID?>) =
                test(realm, set, values, emptyArray(), arrayOf())
    }

    class RealmModelManaged : ToArrayManaged<RealmModel>() {
        override fun toArrayWithParameter(realm: Realm, set: RealmSet<RealmModel>, values: List<RealmModel?>) =
                test(realm, set, values, emptyArray(), arrayOf())
    }

    class MixedManaged : ToArrayManaged<Mixed>() {
        override fun toArrayWithParameter(realm: Realm, set: RealmSet<Mixed>, values: List<Mixed?>) =
                test(realm, set, values, emptyArray(), arrayOf())
    }
}

