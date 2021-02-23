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
    override fun constructorWithAnotherMap() = assertConstructorWithAnotherMap(keyValuePairs)
    override fun isManaged() = assertUnmanagedIsManaged<T>()
    override fun isValid() = assertUnmanagedIsValid<T>()
    override fun isFrozen() = assertUnmanagedIsFrozen<T>()
    override fun size() = assertUnmanagedSize(keyValuePairs)
    override fun isEmpty() = assertUnmanagedIsEmpty(keyValuePairs)
    override fun containsKey() = assertUnmanagedContainsKey(keyValuePairs, notPresentKey)
    override fun containsValue() = assertUnmanagedContainsValue(keyValuePairs, notPresentValue)
    override fun get() = Unit   // This has already been tested in "get"
    override fun put() = assertUnmanagedPut(keyValuePairs)
    override fun remove() = assertUnmanagedRemove(keyValuePairs)
    override fun putAll() = assertUnmanagedPutAll(keyValuePairs)
    override fun clear() = assertUnmanagedClear(keyValuePairs)
    override fun keySet() = assertUnmanagedKeySet(keyValuePairs)
    override fun values() = assertUnmanagedValues(keyValuePairs)
    override fun entrySet() = assertUnmanagedEntrySet(keyValuePairs)
    override fun freeze() = assertUnmanagedFreeze<T>()

    // Managed-specific tests
    override fun copyToRealm() = Unit
    override fun copyFromRealm() = Unit
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
            DictionarySupportedType.BOXED_BINARY ->
                UnmanagedGeneric<Array<Byte>>(
                        testerName = "UnmanagedBoxedBinary",
                        keyValuePairs = listOf(KEY_HELLO to VALUE_BOXED_BINARY_HELLO, KEY_BYE to VALUE_BOXED_BINARY_BYE, KEY_NULL to null),
                        notPresentKey = KEY_NOT_PRESENT,
                        notPresentValue = VALUE_BOXED_BINARY_NOT_PRESENT
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

//--------------------------------------------------------------------------------------------------
// Unmanaged helpers
//--------------------------------------------------------------------------------------------------

private fun <T : Any> assertConstructorWithAnotherMap(keyValuePairs: List<Pair<String, T?>>) {
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

private fun <T : Any> assertUnmanagedIsManaged() {
    val realmDictionary = RealmDictionary<T>()
    assertFalse(realmDictionary.isManaged)
}

private fun <T : Any> assertUnmanagedIsValid() {
    val realmDictionary = RealmDictionary<T>()
    assertTrue(realmDictionary.isValid)
}

private fun <T : Any> assertUnmanagedIsFrozen() {
    val realmDictionary = RealmDictionary<T>()
    assertFalse(realmDictionary.isFrozen)
}

private fun <T : Any> assertUnmanagedSize(keyValuePairs: List<Pair<String, T?>>) {
    val realmDictionary = RealmDictionary<T>()
    assertEquals(0, realmDictionary.size)
    for (keyValuePair in keyValuePairs) {
        realmDictionary[keyValuePair.first] = keyValuePair.second
    }
    assertEquals(keyValuePairs.size, realmDictionary.size)
}

private fun <T : Any> assertUnmanagedIsEmpty(keyValuePairs: List<Pair<String, T?>>) {
    val realmDictionary = RealmDictionary<T>()
    assertTrue(realmDictionary.isEmpty())
    for (keyValuePair in keyValuePairs) {
        realmDictionary[keyValuePair.first] = keyValuePair.second
    }
    assertFalse(realmDictionary.isEmpty())
}

private fun <T : Any> assertUnmanagedContainsKey(
        keyValuePairs: List<Pair<String, T?>>,
        notPresentValue: String
) {
    val realmDictionary = RealmDictionary<T>()
    for (keyValuePair in keyValuePairs) {
        realmDictionary[keyValuePair.first] = keyValuePair.second
        assertTrue(realmDictionary.containsKey(keyValuePair.first))
    }
    assertFalse(realmDictionary.containsKey(notPresentValue))
}

private fun <T : Any> assertUnmanagedContainsValue(
        keyValuePairs: List<Pair<String, T?>>,
        notPresentValue: T
) {
    val realmDictionary = RealmDictionary<T>()
    for (keyValuePair in keyValuePairs) {
        realmDictionary[keyValuePair.first] = keyValuePair.second
        assertTrue(realmDictionary.containsValue(keyValuePair.second))
    }
    assertFalse(realmDictionary.containsValue(notPresentValue))
}

private fun <T : Any> assertUnmanagedPut(keyValuePairs: List<Pair<String, T?>>) {
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

private fun <T : Any> assertUnmanagedRemove(keyValuePairs: List<Pair<String, T?>>) {
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

private fun <T : Any> assertUnmanagedPutAll(keyValuePairs: List<Pair<String, T?>>) {
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

private fun <T : Any> assertUnmanagedClear(keyValuePairs: List<Pair<String, T?>>) {
    val realmDictionary = RealmDictionary<T>()
    assertTrue(realmDictionary.isEmpty())
    for (keyValuePair in keyValuePairs) {
        realmDictionary[keyValuePair.first] = keyValuePair.second
    }
    assertFalse(realmDictionary.isEmpty())
    realmDictionary.clear()
    assertTrue(realmDictionary.isEmpty())
}

private fun <T : Any> assertUnmanagedKeySet(keyValuePairs: List<Pair<String, T?>>) {
    val otherDictionary = RealmDictionary<T>().apply {
        for (keyValuePair in keyValuePairs) {
            this[keyValuePair.first] = keyValuePair.second
        }
    }
    val realmDictionary = RealmDictionary<T>(otherDictionary)
    val keySet = keyValuePairs.map { pair -> pair.first }.toSet()
    assertEquals(keySet, realmDictionary.keys)
}

private fun <T : Any> assertUnmanagedValues(keyValuePairs: List<Pair<String, T?>>) {
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

private fun <T : Any> assertUnmanagedEntrySet(
        keyValuePairs: List<Pair<String, T?>>
) {
    val otherDictionary = RealmDictionary<T>().apply {
        for (keyValuePair in keyValuePairs) {
            this[keyValuePair.first] = keyValuePair.second
        }
    }
    val realmDictionary = RealmDictionary<T>(otherDictionary)
    assertEquals(otherDictionary.entries, realmDictionary.entries)
}

private fun <T : Any> assertUnmanagedFreeze() {
    val dictionary = RealmDictionary<T>()
    assertFailsWith<UnsupportedOperationException> {
        dictionary.freeze()
    }
}

