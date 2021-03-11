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

import io.realm.rule.BlockingLooperThread
import org.bson.types.Decimal128
import org.bson.types.ObjectId
import java.util.*
import kotlin.test.*

/**
 * Generic tester for all types of unmanaged sets.
 */
class UnmanagedSetTester<T : Any>(
        private val testerName: String,
        private val values: List<T?>,
        private val notPresentValue: T
) : SetTester {

    override fun toString(): String = testerName

    override fun setUp(config: RealmConfiguration, looperThread: BlockingLooperThread) = Unit
    override fun tearDown() = Unit

    override fun isManaged() {
        val realmSet = RealmSet<T>()
        assertFalse(realmSet.isManaged)
    }

    override fun isValid() {
        val realmSet = RealmSet<T>()
        assertTrue(realmSet.isValid)
    }

    override fun isFrozen() {
        val realmSet = RealmSet<T>()
        assertFalse(realmSet.isFrozen)
    }

    override fun size() {
        val realmSet = RealmSet<T>()
        assertEquals(0, realmSet.size)
        realmSet.addAll(values)
        assertEquals(values.size, realmSet.size)
    }

    override fun isEmpty() {
        val realmSet = RealmSet<T>()
        assertTrue(realmSet.isEmpty())
        realmSet.addAll(values)
        assertFalse(realmSet.isEmpty())
    }

    override fun contains() {
        val realmSet = RealmSet<T>()
        realmSet.addAll(values)
        values.forEach { value ->
            assertTrue(realmSet.contains(value))
        }
    }

    override fun iterator() {
        val realmSet = RealmSet<T>()
        assertNotNull(realmSet.iterator())
    }

    override fun toArray() {
        val realmSet = RealmSet<T>()
        val emptyArray = realmSet.toArray()
        assertEquals(0, emptyArray.size)

        realmSet.addAll(values)
        val fullArray = realmSet.toArray()
        assertEquals(values.size, fullArray.size)
    }

    override fun add() {
        val realmSet = RealmSet<T>()
        assertTrue(realmSet.isEmpty())
        values.forEach { value ->
            realmSet.add(value)
        }
        assertFalse(realmSet.isEmpty())
        assertEquals(values.size, realmSet.size)
    }

    override fun remove() {
        val realmSet = RealmSet<T>()
        realmSet.addAll(values)
        assertEquals(values.size, realmSet.size)

        assertTrue(realmSet.remove(values[0]))
        assertEquals(values.size - 1, realmSet.size)
        assertFalse(realmSet.remove(notPresentValue))
    }

    override fun containsAll() {
        val realmSet = RealmSet<T>()
        realmSet.addAll(values)
        assertTrue(realmSet.containsAll(values))
    }

    override fun addAll() = Unit        // Tested multiple times in the other functions

    override fun retainAll() {
        val realmSet = RealmSet<T>()
        realmSet.addAll(values)

        // Test intersection with present value
        val presentValues = values.subList(0, 1)
        assertTrue(realmSet.retainAll(presentValues))
        assertEquals(presentValues.size, realmSet.size)

        // Test intersection with non-present value
        val differentRealmSet = RealmSet<T>()
        differentRealmSet.addAll(values)
        assertTrue(differentRealmSet.retainAll(listOf(notPresentValue)))
        assertTrue(differentRealmSet.isEmpty())
    }

    override fun removeAll() {
        val realmSet = RealmSet<T>()
        realmSet.addAll(values)
        assertFalse(realmSet.isEmpty())
        realmSet.removeAll(values)
        assertTrue(realmSet.isEmpty())

        val differentRealmSet = RealmSet<T>()
        differentRealmSet.addAll(values)
        assertFalse(differentRealmSet.isEmpty())
        differentRealmSet.removeAll(listOf(notPresentValue))
        assertEquals(values.size, differentRealmSet.size)
    }

    override fun clear() {
        val realmSet = RealmSet<T>()
        realmSet.addAll(values)
        assertFalse(realmSet.isEmpty())
        realmSet.clear()
        assertTrue(realmSet.isEmpty())
    }

    override fun freeze() {
        assertFailsWith<UnsupportedOperationException> {
            RealmSet<T>().freeze()
        }
    }
}

fun unmanagedSetFactory(): List<SetTester> {
    val primitiveTesters: List<SetTester> = SetSupportedType.values().mapNotNull { supportedType ->
        when (supportedType) {
            SetSupportedType.LONG ->
                UnmanagedSetTester<Long>(
                        testerName = "Long",
                        values = listOf(VALUE_NUMERIC_HELLO.toLong(), VALUE_NUMERIC_BYE.toLong(), null),
                        notPresentValue = VALUE_NUMERIC_NOT_PRESENT.toLong()
                )
            SetSupportedType.INTEGER ->
                UnmanagedSetTester<Int>(
                        testerName = "Int",
                        values = listOf(VALUE_NUMERIC_HELLO, VALUE_NUMERIC_BYE),
                        notPresentValue = VALUE_NUMERIC_NOT_PRESENT
                )
            SetSupportedType.SHORT ->
                UnmanagedSetTester<Short>(
                        testerName = "Short",
                        values = listOf(VALUE_NUMERIC_HELLO.toShort(), VALUE_NUMERIC_BYE.toShort(), null),
                        notPresentValue = VALUE_NUMERIC_NOT_PRESENT.toShort()
                )
            SetSupportedType.BYTE ->
                UnmanagedSetTester<Byte>(
                        testerName = "Byte",
                        values = listOf(VALUE_NUMERIC_HELLO.toByte(), VALUE_NUMERIC_BYE.toByte(), null),
                        notPresentValue = VALUE_NUMERIC_NOT_PRESENT.toByte()
                )
            SetSupportedType.FLOAT ->
                UnmanagedSetTester<Float>(
                        testerName = "Float",
                        values = listOf(VALUE_NUMERIC_HELLO.toFloat(), VALUE_NUMERIC_BYE.toFloat(), null),
                        notPresentValue = VALUE_NUMERIC_NOT_PRESENT.toFloat()
                )
            SetSupportedType.DOUBLE ->
                UnmanagedSetTester<Double>(
                        testerName = "Double",
                        values = listOf(VALUE_NUMERIC_HELLO.toDouble(), VALUE_NUMERIC_BYE.toDouble(), null),
                        notPresentValue = VALUE_NUMERIC_NOT_PRESENT.toDouble()
                )
            SetSupportedType.STRING ->
                UnmanagedSetTester<String>(
                        testerName = "String",
                        values = listOf(VALUE_STRING_HELLO, VALUE_STRING_BYE, null),
                        notPresentValue = VALUE_STRING_NOT_PRESENT
                )
            SetSupportedType.BOOLEAN ->
                UnmanagedSetTester<Boolean>(
                        testerName = "Boolean",
                        values = listOf(VALUE_BOOLEAN_HELLO, null),
                        notPresentValue = VALUE_BOOLEAN_NOT_PRESENT
                )
            SetSupportedType.DATE ->
                UnmanagedSetTester<Date>(
                        testerName = "Date",
                        values = listOf(VALUE_DATE_HELLO, VALUE_DATE_BYE, null),
                        notPresentValue = VALUE_DATE_NOT_PRESENT
                )
            SetSupportedType.DECIMAL128 ->
                UnmanagedSetTester<Decimal128>(
                        testerName = "Decimal128",
                        values = listOf(VALUE_DECIMAL128_HELLO, VALUE_DECIMAL128_BYE, null),
                        notPresentValue = VALUE_DECIMAL128_NOT_PRESENT
                )
            SetSupportedType.BINARY ->
                UnmanagedSetTester<ByteArray>(
                        testerName = "ByteArray",
                        values = listOf(VALUE_BINARY_HELLO, VALUE_BINARY_BYE, null),
                        notPresentValue = VALUE_BINARY_NOT_PRESENT
                )
            SetSupportedType.OBJECT_ID ->
                UnmanagedSetTester<ObjectId>(
                        testerName = "ObjectId",
                        values = listOf(VALUE_OBJECT_ID_HELLO, VALUE_OBJECT_ID_BYE, null),
                        notPresentValue = VALUE_OBJECT_ID_NOT_PRESENT
                )
            SetSupportedType.UUID ->
                UnmanagedSetTester<UUID>(
                        testerName = "UUID",
                        values = listOf(VALUE_UUID_HELLO, VALUE_UUID_BYE, null),
                        notPresentValue = VALUE_UUID_NOT_PRESENT
                )
            SetSupportedType.LINK ->
                UnmanagedSetTester<RealmModel>(
                        testerName = "UnmanagedRealmModel",
                        values = listOf(VALUE_LINK_HELLO, VALUE_LINK_BYE, null),
                        notPresentValue = VALUE_LINK_NOT_PRESENT
                )
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
    return primitiveTesters.plus(mixedTesters)
}

fun getMixedValues(mixedType: MixedType): List<Mixed?> {
    return when (mixedType) {
        MixedType.INTEGER ->
            listOf(VALUE_MIXED_INTEGER_HELLO, VALUE_MIXED_INTEGER_BYE, null)
        MixedType.BOOLEAN ->
            listOf(VALUE_MIXED_BOOLEAN_HELLO, null)
        MixedType.STRING ->
            listOf(VALUE_MIXED_STRING_HELLO, VALUE_MIXED_STRING_BYE, null)
        MixedType.BINARY ->
            listOf(VALUE_MIXED_BINARY_HELLO, VALUE_MIXED_BINARY_BYE, null)
        MixedType.DATE ->
            listOf(VALUE_MIXED_DATE_HELLO, VALUE_MIXED_DATE_BYE, null)
        MixedType.FLOAT ->
            listOf(VALUE_MIXED_FLOAT_HELLO, VALUE_MIXED_FLOAT_BYE, null)
        MixedType.DOUBLE ->
            listOf(VALUE_MIXED_DOUBLE_HELLO, VALUE_MIXED_DOUBLE_BYE, null)
        MixedType.DECIMAL128 ->
            listOf(VALUE_MIXED_DECIMAL128_HELLO, VALUE_MIXED_DECIMAL128_BYE, null)
        MixedType.OBJECT_ID ->
            listOf(VALUE_MIXED_OBJECT_ID_HELLO, VALUE_MIXED_OBJECT_ID_BYE, null)
        MixedType.OBJECT ->
            listOf(VALUE_MIXED_LINK_HELLO, VALUE_MIXED_LINK_BYE, null)
        MixedType.UUID ->
            listOf(VALUE_MIXED_UUID_HELLO, VALUE_MIXED_UUID_BYE, null)
        MixedType.NULL ->
            listOf(Mixed.nullValue(), Mixed.valueOf("Not null"), null)
    }
}
