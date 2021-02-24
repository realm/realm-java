/*
 * Copyright 2020 Realm Inc.
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

import org.bson.types.Decimal128
import org.bson.types.ObjectId
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Generic tester for all types of unmanaged dictionaries.
 */
class UnmanagedGeneric<T : Any>(
        private val testerName: String,
        private val keyValuePairs: List<Pair<String, T?>>,
        private val notPresentKey: String,
        private val notPresentValue: T
) : DictionaryTester {

    override fun toString(): String = testerName

    override fun setUp(config: RealmConfiguration) = Unit
    override fun tearDown() = Unit

    override fun constructorWithAnotherMap() {
        val otherDictionary = RealmDictionary<T?>().apply {
            for (keyValuePair in keyValuePairs) {
                this[keyValuePair.first] = keyValuePair.second
            }
        }
        val realmDictionary = RealmDictionary<T>(otherDictionary)
        assertEquals(keyValuePairs.size, realmDictionary.size)
        for (keyValuePair in keyValuePairs) {
            assertTrue(realmDictionary.containsKey(keyValuePair.first))
            assertTrue(realmDictionary.containsValue(keyValuePair.second))
        }
    }

    override fun isManaged() {
        val realmDictionary = RealmDictionary<T>()
        assertFalse(realmDictionary.isManaged)
    }

    override fun isValid() {
        val realmDictionary = RealmDictionary<T>()
        assertTrue(realmDictionary.isValid)
    }

    override fun isFrozen() {
        val realmDictionary = RealmDictionary<T>()
        assertFalse(realmDictionary.isFrozen)
    }

    override fun size() {
        val realmDictionary = RealmDictionary<T>()
        assertEquals(0, realmDictionary.size)
        for (keyValuePair in keyValuePairs) {
            realmDictionary[keyValuePair.first] = keyValuePair.second
        }
        assertEquals(keyValuePairs.size, realmDictionary.size)
    }

    override fun isEmpty() {
        val realmDictionary = RealmDictionary<T>()
        assertTrue(realmDictionary.isEmpty())
        for (keyValuePair in keyValuePairs) {
            realmDictionary[keyValuePair.first] = keyValuePair.second
        }
        assertFalse(realmDictionary.isEmpty())
    }

    override fun containsKey() {
        val realmDictionary = RealmDictionary<T>()
        for (keyValuePair in keyValuePairs) {
            realmDictionary[keyValuePair.first] = keyValuePair.second
            assertTrue(realmDictionary.containsKey(keyValuePair.first))
        }
        assertFalse(realmDictionary.containsKey(notPresentKey))
    }

    override fun containsValue() {
        val realmDictionary = RealmDictionary<T>()
        for (keyValuePair in keyValuePairs) {
            realmDictionary[keyValuePair.first] = keyValuePair.second
            assertTrue(realmDictionary.containsValue(keyValuePair.second))
        }
        assertFalse(realmDictionary.containsValue(notPresentValue))
    }

    // This will be tested in "put"
    override fun get() = Unit

    override fun put() {
        val realmDictionary = RealmDictionary<T?>()
        assertEquals(0, realmDictionary.size)
        for (i in keyValuePairs.indices) {
            realmDictionary[keyValuePairs[i].first] = keyValuePairs[i].second
            assertEquals(i + 1, realmDictionary.size)
        }
        for (i in keyValuePairs.indices) {
            assertEquals(keyValuePairs[i].second, realmDictionary[keyValuePairs[i].first])
        }

        // Cannot add a null key
        assertFailsWith<IllegalArgumentException> {
            realmDictionary[null] = keyValuePairs[0].second
        }
    }

    override fun remove() {
        val realmDictionary = RealmDictionary<T>()
        for (keyValuePair in keyValuePairs) {
            realmDictionary[keyValuePair.first] = keyValuePair.second
        }
        for (keyValuePair in keyValuePairs) {
            val removedValue = realmDictionary.remove(keyValuePair.first)
            assertEquals(keyValuePair.second, removedValue)
        }

        // Nothing happens when removing a null key
        realmDictionary.remove(null)
    }

    override fun putAll() {
        val otherMap = HashMap<String, T?>().apply {
            for (keyValuePair in keyValuePairs) {
                this[keyValuePair.first] = keyValuePair.second
            }
        }
        val realmDictionary = RealmDictionary<T>()
        realmDictionary.putAll(otherMap)
        assertEquals(keyValuePairs.size, realmDictionary.size)
        for (keyValuePair in keyValuePairs) {
            assertTrue(realmDictionary.containsKey(keyValuePair.first))
            assertTrue(realmDictionary.containsValue(keyValuePair.second))
        }
    }

    override fun clear() {
        val realmDictionary = RealmDictionary<T>()
        assertTrue(realmDictionary.isEmpty())
        for (keyValuePair in keyValuePairs) {
            realmDictionary[keyValuePair.first] = keyValuePair.second
        }
        assertFalse(realmDictionary.isEmpty())
        realmDictionary.clear()
        assertTrue(realmDictionary.isEmpty())
    }

    override fun keySet() {
        val otherDictionary = RealmDictionary<T>().apply {
            for (keyValuePair in keyValuePairs) {
                this[keyValuePair.first] = keyValuePair.second
            }
        }
        val realmDictionary = RealmDictionary<T>(otherDictionary)
        val keySet = keyValuePairs.map { pair -> pair.first }.toSet()
        assertEquals(keySet, realmDictionary.keys)
    }

    override fun values() {
        val otherDictionary = RealmDictionary<T>().apply {
            for (keyValuePair in keyValuePairs) {
                this[keyValuePair.first] = keyValuePair.second
            }
        }
        val realmDictionary = RealmDictionary<T>(otherDictionary)
        val dictionaryValues = realmDictionary.values

        // Depending on the internal implementation of the chosen Map, the order might be altered
        keyValuePairs.forEach { pair ->
            assertTrue(dictionaryValues.contains(pair.second))
        }
    }

    override fun entrySet() {
        val otherDictionary = RealmDictionary<T>().apply {
            for (keyValuePair in keyValuePairs) {
                this[keyValuePair.first] = keyValuePair.second
            }
        }
        val realmDictionary = RealmDictionary<T>(otherDictionary)
        assertEquals(otherDictionary.entries, realmDictionary.entries)
    }

    override fun freeze() {
        val dictionary = RealmDictionary<T>()
        assertFailsWith<UnsupportedOperationException> {
            dictionary.freeze()
        }
    }

    override fun copyToRealm() = Unit               // Not applicable
    override fun copyFromRealm() = Unit             // Not applicable
}

/**
 * Creates testers for all [DictionarySupportedType]s and initializes them for testing. There are as
 * many Mixed testers as [MixedType]s.
 */
fun unmanagedFactory(): List<DictionaryTester> {
    // Create primitive testers first
    val primitiveTesters: List<DictionaryTester> = DictionarySupportedType.values().mapNotNull { supportedType ->
        when (supportedType) {
            DictionarySupportedType.LONG ->
                UnmanagedGeneric<Long>(
                        testerName = "UnmanagedLong",
                        keyValuePairs = listOf(KEY_HELLO to VALUE_NUMERIC_HELLO.toLong(), KEY_BYE to VALUE_NUMERIC_BYE.toLong(), KEY_NULL to null),
                        notPresentKey = KEY_NOT_PRESENT,
                        notPresentValue = VALUE_NUMERIC_NOT_PRESENT.toLong()
                )
            DictionarySupportedType.INTEGER ->
                UnmanagedGeneric<Int>(
                        testerName = "UnmanagedInteger",
                        keyValuePairs = listOf(KEY_HELLO to VALUE_NUMERIC_HELLO, KEY_BYE to VALUE_NUMERIC_BYE, KEY_NULL to null),
                        notPresentKey = KEY_NOT_PRESENT,
                        notPresentValue = VALUE_NUMERIC_NOT_PRESENT
                )
            DictionarySupportedType.SHORT ->
                UnmanagedGeneric<Short>(
                        testerName = "UnmanagedShort",
                        keyValuePairs = listOf(KEY_HELLO to VALUE_NUMERIC_HELLO.toShort(), KEY_BYE to VALUE_NUMERIC_BYE.toShort(), KEY_NULL to null),
                        notPresentKey = KEY_NOT_PRESENT,
                        notPresentValue = VALUE_NUMERIC_NOT_PRESENT.toShort()
                )
            DictionarySupportedType.BYTE ->
                UnmanagedGeneric<Byte>(
                        testerName = "UnmanagedByte",
                        keyValuePairs = listOf(KEY_HELLO to VALUE_NUMERIC_HELLO.toByte(), KEY_BYE to VALUE_NUMERIC_BYE.toByte(), KEY_NULL to null),
                        notPresentKey = KEY_NOT_PRESENT,
                        notPresentValue = VALUE_NUMERIC_NOT_PRESENT.toByte()
                )
            DictionarySupportedType.FLOAT ->
                UnmanagedGeneric<Float>(
                        testerName = "UnmanagedFloat",
                        keyValuePairs = listOf(KEY_HELLO to VALUE_NUMERIC_HELLO.toFloat(), KEY_BYE to VALUE_NUMERIC_BYE.toFloat(), KEY_NULL to null),
                        notPresentKey = KEY_NOT_PRESENT,
                        notPresentValue = VALUE_NUMERIC_NOT_PRESENT.toFloat()
                )
            DictionarySupportedType.DOUBLE ->
                UnmanagedGeneric<Double>(
                        testerName = "UnmanagedDouble",
                        keyValuePairs = listOf(KEY_HELLO to VALUE_NUMERIC_HELLO.toDouble(), KEY_BYE to VALUE_NUMERIC_BYE.toDouble(), KEY_NULL to null),
                        notPresentKey = KEY_NOT_PRESENT,
                        notPresentValue = VALUE_NUMERIC_NOT_PRESENT.toDouble()
                )
            DictionarySupportedType.STRING ->
                UnmanagedGeneric<String>(
                        testerName = "UnmanagedString",
                        keyValuePairs = listOf(KEY_HELLO to VALUE_STRING_HELLO, KEY_BYE to VALUE_STRING_BYE, KEY_NULL to null),
                        notPresentKey = KEY_NOT_PRESENT,
                        notPresentValue = VALUE_STRING_NOT_PRESENT
                )
            DictionarySupportedType.BOOLEAN ->
                UnmanagedGeneric<Boolean>(
                        testerName = "UnmanagedBoolean",
                        keyValuePairs = listOf(KEY_HELLO to VALUE_BOOLEAN_HELLO, KEY_NULL to null),
                        notPresentKey = KEY_NOT_PRESENT,
                        notPresentValue = VALUE_BOOLEAN_NOT_PRESENT
                )
            DictionarySupportedType.DATE ->
                UnmanagedGeneric<Date>(
                        testerName = "UnmanagedDate",
                        keyValuePairs = listOf(KEY_HELLO to VALUE_DATE_HELLO, KEY_BYE to VALUE_DATE_BYE, KEY_NULL to null),
                        notPresentKey = KEY_NOT_PRESENT,
                        notPresentValue = VALUE_DATE_NOT_PRESENT
                )
            DictionarySupportedType.DECIMAL128 ->
                UnmanagedGeneric<Decimal128>(
                        testerName = "UnmanagedDecimal128",
                        keyValuePairs = listOf(KEY_HELLO to VALUE_DECIMAL128_HELLO, KEY_BYE to VALUE_DECIMAL128_BYE, KEY_NULL to null),
                        notPresentKey = KEY_NOT_PRESENT,
                        notPresentValue = VALUE_DECIMAL128_NOT_PRESENT
                )
            DictionarySupportedType.BINARY ->
                UnmanagedGeneric<ByteArray>(
                        testerName = "UnmanagedBinary",
                        keyValuePairs = listOf(KEY_HELLO to VALUE_BINARY_HELLO, KEY_BYE to VALUE_BINARY_BYE, KEY_NULL to null),
                        notPresentKey = KEY_NOT_PRESENT,
                        notPresentValue = VALUE_BINARY_NOT_PRESENT
                )
            DictionarySupportedType.OBJECT_ID ->
                UnmanagedGeneric<ObjectId>(
                        testerName = "UnmanagedObjectId",
                        keyValuePairs = listOf(KEY_HELLO to VALUE_OBJECT_ID_HELLO, KEY_BYE to VALUE_OBJECT_ID_BYE, KEY_NULL to null),
                        notPresentKey = KEY_NOT_PRESENT,
                        notPresentValue = VALUE_OBJECT_ID_NOT_PRESENT
                )
            DictionarySupportedType.UUID ->
                UnmanagedGeneric<UUID>(
                        testerName = "UnmanagedUUID",
                        keyValuePairs = listOf(KEY_HELLO to VALUE_UUID_HELLO, KEY_BYE to VALUE_UUID_BYE, KEY_NULL to null),
                        notPresentKey = KEY_NOT_PRESENT,
                        notPresentValue = VALUE_UUID_NOT_PRESENT
                )
            DictionarySupportedType.LINK ->
                UnmanagedGeneric<RealmModel>(
                        testerName = "UnmanagedRealmModel",
                        keyValuePairs = listOf(KEY_HELLO to VALUE_LINK_HELLO, KEY_BYE to VALUE_LINK_BYE, KEY_NULL to null),
                        notPresentKey = KEY_NOT_PRESENT,
                        notPresentValue = VALUE_LINK_NOT_PRESENT
                )
            // Ignore Mixed in this switch
            else -> null
        }
    }

    // Create Mixed testers now
    val mixedTesters = MixedType.values().map { mixedType ->
        UnmanagedGeneric<Mixed>(
                "UnmanagedMixed-${mixedType.name}",
                getMixedKeyValuePairs(mixedType),
                KEY_NOT_PRESENT,
                VALUE_MIXED_NOT_PRESENT
        )
    }

    // Put them together
    return primitiveTesters.plus(mixedTesters)
}
