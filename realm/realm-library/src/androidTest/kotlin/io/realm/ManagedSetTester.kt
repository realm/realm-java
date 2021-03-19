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
import org.bson.types.Decimal128
import org.bson.types.ObjectId
import java.util.*
import kotlin.reflect.KFunction
import kotlin.reflect.KFunction1
import kotlin.reflect.KFunction2
import kotlin.test.*

/**
 * Generic tester for all types of unmanaged sets.
 */
class ManagedSetTester<T : Any>(
        private val testerName: String,
        private val mixedType: MixedType? = null,
        private val setGetter: KFunction1<AllTypes, RealmSet<T>>,
        private val setSetter: KFunction2<AllTypes, RealmSet<T>, Unit>,
        private val initializedSet: List<T?>,
        private val notPresentValue: T
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

    override fun isFrozen() {
        // TODO
    }

    override fun size() {
        val set = initAndAssert()
        assertEquals(0, set.size)
        realm.executeTransaction {
            initializedSet.forEach { value ->
                set.add(value)
            }
        }
        assertEquals(initializedSet.size, set.size)
    }

    override fun isEmpty() {
        val set = initAndAssert()
        assertTrue(set.isEmpty())
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
    }

    override fun iterator() {
        // TODO
    }

    override fun toArray() {
        // TODO
    }

    override fun add() {
        val set = initAndAssert()
        assertEquals(0, set.size)
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

        // FIXME: assert sets are equal
    }

    override fun remove() {
        val set = initAndAssert()
        assertEquals(0, set.size)
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
        // TODO
    }

    override fun addAll() {
        // TODO
    }

    override fun retainAll() {
        // TODO
    }

    override fun removeAll() {
        // TODO
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
        // TODO
    }

    //----------------------------------
    // Private stuff
    //----------------------------------

    private fun initAndAssert(realm: Realm = this.realm): RealmSet<T> {
        val allTypesObject = createAllTypesManagedContainerAndAssert(realm)
        assertNotNull(allTypesObject)
        return setGetter.call(allTypesObject)
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
//            SetSupportedType.INTEGER ->
//                UnmanagedSetTester<Int>(
//                        testerName = "Int",
//                        values = listOf(VALUE_NUMERIC_HELLO, VALUE_NUMERIC_BYE),
//                        notPresentValue = VALUE_NUMERIC_NOT_PRESENT
//                )
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
                        initializedSet = listOf(VALUE_STRING_HELLO, VALUE_STRING_BYE, null),
                        notPresentValue = VALUE_STRING_NOT_PRESENT
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
    val mixedTesters = MixedType.values().map { mixedType ->
        UnmanagedSetTester<Mixed>(
                "UnmanagedSetMixed-${mixedType.name}",
                getMixedValues(mixedType),
                VALUE_MIXED_NOT_PRESENT
        )
    }

    // Put the together
//    return primitiveTesters.plus(mixedTesters)
    return primitiveTesters
}
