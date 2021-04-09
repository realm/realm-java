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
        private val testerClass: Class<T>,
        private val values: List<T?>,
        private val notPresentValue: T,
        private val toArrayUnmanaged: ToArrayUnmanaged<T>,
        private val mixedType: MixedType? = null
) : SetTester {

    override fun toString(): String = when (mixedType) {
        null -> "UnmanagedDictionary-$testerClass"
        else -> "UnmanagedDictionary-$testerClass" + mixedType.name.let { "-$it" }
    }

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

    override fun toArrayWithParameter() {
        toArrayUnmanaged.toArrayWithParameter(values)
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

    override fun putRequired() = Unit

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

    override fun copyToRealm() = Unit  // Not applicable

    override fun copyToRealmOrUpdate() = Unit  // Not applicable

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

    // Not a valid test on unmanaged sets
    override fun setters() = Unit
}

fun unmanagedSetFactory(): List<SetTester> {
    val primitiveTesters: List<SetTester> = SetSupportedType.values().mapNotNull { supportedType ->
        when (supportedType) {
            SetSupportedType.LONG ->
                UnmanagedSetTester(
                        testerClass = Long::class.java,
                        values = listOf(VALUE_NUMERIC_HELLO.toLong(), VALUE_NUMERIC_BYE.toLong(), null),
                        notPresentValue = VALUE_NUMERIC_NOT_PRESENT.toLong(),
                        toArrayUnmanaged = ToArrayUnmanaged.LongUnmanaged()
                )
            SetSupportedType.INTEGER ->
                UnmanagedSetTester(
                        testerClass = Int::class.java,
                        values = listOf(VALUE_NUMERIC_HELLO, VALUE_NUMERIC_BYE),
                        notPresentValue = VALUE_NUMERIC_NOT_PRESENT,
                        toArrayUnmanaged = ToArrayUnmanaged.IntUnmanaged()
                )
            SetSupportedType.SHORT ->
                UnmanagedSetTester(
                        testerClass = Short::class.java,
                        values = listOf(VALUE_NUMERIC_HELLO.toShort(), VALUE_NUMERIC_BYE.toShort(), null),
                        notPresentValue = VALUE_NUMERIC_NOT_PRESENT.toShort(),
                        toArrayUnmanaged = ToArrayUnmanaged.ShortUnmanaged()
                )
            SetSupportedType.BYTE ->
                UnmanagedSetTester(
                        testerClass = Byte::class.java,
                        values = listOf(VALUE_NUMERIC_HELLO.toByte(), VALUE_NUMERIC_BYE.toByte(), null),
                        notPresentValue = VALUE_NUMERIC_NOT_PRESENT.toByte(),
                        toArrayUnmanaged = ToArrayUnmanaged.ByteUnmanaged()
                )
            SetSupportedType.FLOAT ->
                UnmanagedSetTester(
                        testerClass = Float::class.java,
                        values = listOf(VALUE_NUMERIC_HELLO.toFloat(), VALUE_NUMERIC_BYE.toFloat(), null),
                        notPresentValue = VALUE_NUMERIC_NOT_PRESENT.toFloat(),
                        toArrayUnmanaged = ToArrayUnmanaged.FloatUnmanaged()
                )
            SetSupportedType.DOUBLE ->
                UnmanagedSetTester(
                        testerClass = Double::class.java,
                        values = listOf(VALUE_NUMERIC_HELLO.toDouble(), VALUE_NUMERIC_BYE.toDouble(), null),
                        notPresentValue = VALUE_NUMERIC_NOT_PRESENT.toDouble(),
                        toArrayUnmanaged = ToArrayUnmanaged.DoubleUnmanaged()
                )
            SetSupportedType.STRING ->
                UnmanagedSetTester(
                        testerClass = String::class.java,
                        values = listOf(VALUE_STRING_HELLO, VALUE_STRING_BYE, null),
                        notPresentValue = VALUE_STRING_NOT_PRESENT,
                        toArrayUnmanaged = ToArrayUnmanaged.StringUnmanaged()
                )
            SetSupportedType.BOOLEAN ->
                UnmanagedSetTester(
                        testerClass = Boolean::class.java,
                        values = listOf(VALUE_BOOLEAN_HELLO, null),
                        notPresentValue = VALUE_BOOLEAN_NOT_PRESENT,
                        toArrayUnmanaged = ToArrayUnmanaged.BooleanUnmanaged()
                )
            SetSupportedType.DATE ->
                UnmanagedSetTester(
                        testerClass = Date::class.java,
                        values = listOf(VALUE_DATE_HELLO, VALUE_DATE_BYE, null),
                        notPresentValue = VALUE_DATE_NOT_PRESENT,
                        toArrayUnmanaged = ToArrayUnmanaged.DateUnmanaged()
                )
            SetSupportedType.DECIMAL128 ->
                UnmanagedSetTester(
                        testerClass = Decimal128::class.java,
                        values = listOf(VALUE_DECIMAL128_HELLO, VALUE_DECIMAL128_BYE, null),
                        notPresentValue = VALUE_DECIMAL128_NOT_PRESENT,
                        toArrayUnmanaged = ToArrayUnmanaged.Decimal128Unmanaged()
                )
            SetSupportedType.BINARY ->
                UnmanagedSetTester(
                        testerClass = ByteArray::class.java,
                        values = listOf(VALUE_BINARY_HELLO, VALUE_BINARY_BYE, null),
                        notPresentValue = VALUE_BINARY_NOT_PRESENT,
                        toArrayUnmanaged = ToArrayUnmanaged.BinaryUnmanaged()
                )
            SetSupportedType.OBJECT_ID ->
                UnmanagedSetTester(
                        testerClass = ObjectId::class.java,
                        values = listOf(VALUE_OBJECT_ID_HELLO, VALUE_OBJECT_ID_BYE, null),
                        notPresentValue = VALUE_OBJECT_ID_NOT_PRESENT,
                        toArrayUnmanaged = ToArrayUnmanaged.ObjectIdUnmanaged()
                )
            SetSupportedType.UUID ->
                UnmanagedSetTester(
                        testerClass = UUID::class.java,
                        values = listOf(VALUE_UUID_HELLO, VALUE_UUID_BYE, null),
                        notPresentValue = VALUE_UUID_NOT_PRESENT,
                        toArrayUnmanaged = ToArrayUnmanaged.UUIDUnmanaged()
                )
            SetSupportedType.LINK ->
                UnmanagedSetTester(
                        testerClass = RealmModel::class.java,
                        values = listOf(VALUE_LINK_HELLO, VALUE_LINK_BYE, null),
                        notPresentValue = VALUE_LINK_NOT_PRESENT,
                        toArrayUnmanaged = ToArrayUnmanaged.RealmModelUnmanaged()
                )
            SetSupportedType.MIXED -> null      // Ignore Mixed in this switch
            else -> throw IllegalArgumentException("Unknown data type for Sets")
        }
    }

    // Create Mixed testers now
    val mixedTesters = MixedType.values().map { mixedType ->
        UnmanagedSetTester(
                Mixed::class.java,
                getMixedValues(mixedType),
                VALUE_MIXED_NOT_PRESENT,
                ToArrayUnmanaged.MixedUnmanaged(),
                mixedType
        )
    }

    // Put them together
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

/**
 * TODO
 */
abstract class ToArrayUnmanaged<T> {

    abstract fun toArrayWithParameter(values: List<T?>)

    protected fun test(values: List<T?>, emptyArray: Array<T>, fullArray: Array<T>) {
        val realmSet = RealmSet<T>()
        val emptyFromSet = realmSet.toArray(emptyArray)
        assertEquals(0, emptyFromSet.size)

        realmSet.addAll(values as Collection<T>)
        val fullFromSet = realmSet.toArray(fullArray)
        assertEquals(values.size, fullFromSet.size)
    }

    class LongUnmanaged : ToArrayUnmanaged<Long>() {
        override fun toArrayWithParameter(values: List<Long?>) = test(values, emptyArray(), arrayOf())
    }

    class IntUnmanaged : ToArrayUnmanaged<Int>() {
        override fun toArrayWithParameter(values: List<Int?>) = test(values, emptyArray(), arrayOf())
    }

    class ShortUnmanaged : ToArrayUnmanaged<Short>() {
        override fun toArrayWithParameter(values: List<Short?>) = test(values, emptyArray(), arrayOf())
    }

    class ByteUnmanaged : ToArrayUnmanaged<Byte>() {
        override fun toArrayWithParameter(values: List<Byte?>) = test(values, emptyArray(), arrayOf())
    }

    class FloatUnmanaged : ToArrayUnmanaged<Float>() {
        override fun toArrayWithParameter(values: List<Float?>) = test(values, emptyArray(), arrayOf())
    }

    class DoubleUnmanaged : ToArrayUnmanaged<Double>() {
        override fun toArrayWithParameter(values: List<Double?>) = test(values, emptyArray(), arrayOf())
    }

    class StringUnmanaged : ToArrayUnmanaged<String>() {
        override fun toArrayWithParameter(values: List<String?>) = test(values, emptyArray(), arrayOf())
    }

    class BooleanUnmanaged : ToArrayUnmanaged<Boolean>() {
        override fun toArrayWithParameter(values: List<Boolean?>) = test(values, emptyArray(), arrayOf())
    }

    class DateUnmanaged : ToArrayUnmanaged<Date>() {
        override fun toArrayWithParameter(values: List<Date?>) = test(values, emptyArray(), arrayOf())
    }

    class Decimal128Unmanaged : ToArrayUnmanaged<Decimal128>() {
        override fun toArrayWithParameter(values: List<Decimal128?>) = test(values, emptyArray(), arrayOf())
    }

    class BinaryUnmanaged : ToArrayUnmanaged<ByteArray>() {
        override fun toArrayWithParameter(values: List<ByteArray?>) = test(values, emptyArray(), arrayOf())
    }

    class ObjectIdUnmanaged : ToArrayUnmanaged<ObjectId>() {
        override fun toArrayWithParameter(values: List<ObjectId?>) = test(values, emptyArray(), arrayOf())
    }

    class UUIDUnmanaged : ToArrayUnmanaged<UUID>() {
        override fun toArrayWithParameter(values: List<UUID?>) = test(values, emptyArray(), arrayOf())
    }

    class RealmModelUnmanaged : ToArrayUnmanaged<RealmModel>() {
        override fun toArrayWithParameter(values: List<RealmModel?>) = test(values, emptyArray(), arrayOf())
    }

    class MixedUnmanaged : ToArrayUnmanaged<Mixed>() {
        override fun toArrayWithParameter(values: List<Mixed?>) = test(values, emptyArray(), arrayOf())
    }
}
