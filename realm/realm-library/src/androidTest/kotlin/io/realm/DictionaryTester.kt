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

import kotlin.test.*

/**
 * TODO
 */
interface DictionaryTester {
    fun isTesterManaged(): Boolean
    fun constructorWithAnotherMap()
    fun isManaged()
    fun isValid()
    fun isFrozen()
    fun size()
    fun isEmpty()
    fun containsKey()
    fun containsValue()
    fun get()
    fun put()
    fun remove()
    fun putAll()
    fun clear()
    fun keySet()
    fun values()
    fun entrySet()
    fun freeze()
}

internal inline fun <reified T : Any> assertConstructorWithAnotherMap(
        keys: List<String>,
        values: List<T>
) {
    val otherDictionary = RealmDictionary<T?>().apply {
        this[keys[0]] = values[0]
        this[keys[1]] = values[1]
        this["null"] = null
    }
    val realmDictionary = RealmDictionary<T>(otherDictionary)
    assertEquals(3, realmDictionary.size)
    assertTrue(realmDictionary.containsKey(keys[0]))
    assertTrue(realmDictionary.containsKey(keys[1]))
    assertTrue(realmDictionary.containsKey("null"))
    assertTrue(realmDictionary.containsValue(values[0]))
    assertTrue(realmDictionary.containsValue(values[1]))
    assertTrue(realmDictionary.containsValue(null))
}

internal inline fun <reified T : Any> assertUnmanagedIsManaged() {
    val realmDictionary = RealmDictionary<T>()
    assertFalse(realmDictionary.isManaged)
}

internal inline fun <reified T : Any> assertUnmanagedIsValid() {
    val realmDictionary = RealmDictionary<T>()
    assertTrue(realmDictionary.isValid)
}

internal inline fun <reified T : Any> assertUnmanagedIsFrozen() {
    val realmDictionary = RealmDictionary<T>()
    assertFalse(realmDictionary.isFrozen)
}

internal inline fun <reified T : Any> assertUnmanagedSize(key: String, value: T) {
    val realmDictionary = RealmDictionary<T>()
    assertEquals(0, realmDictionary.size)
    realmDictionary[key] = value
    assertEquals(1, realmDictionary.size)
}

internal inline fun <reified T : Any> assertUnmanagedIsEmpty(key: String, value: T) {
    val realmDictionary = RealmDictionary<T>()
    assertTrue(realmDictionary.isEmpty())
    realmDictionary[key] = value
    assertFalse(realmDictionary.isEmpty())
}

internal inline fun <reified T : Any> assertContainsKey(key: String, value: T) {
    val realmDictionary = RealmDictionary<T>()
    realmDictionary[key] = value
    assertTrue(realmDictionary.containsKey(key))
    assertFalse(realmDictionary.containsKey("ANOTHER_KEY"))
}

internal inline fun <reified T : Any> assertContainsValue(key: String, value: T, anotherValue: T) {
    val realmDictionary = RealmDictionary<T>()
    realmDictionary[key] = value
    assertTrue(realmDictionary.containsValue(value))
    assertFalse(realmDictionary.containsValue(anotherValue))
}

internal inline fun <reified T : Any> assertGet(key: String, value: T) {
    val realmDictionary = RealmDictionary<T?>()
    realmDictionary[key] = value
    val actualValue = realmDictionary[key]
    assertNotNull(actualValue)
    assertEquals(value, actualValue)
}

internal inline fun <reified T : Any> assertPut(key: String, value: T) {
    val realmDictionary = RealmDictionary<T?>()
    assertEquals(0, realmDictionary.size)
    realmDictionary[key] = value
    assertEquals(1, realmDictionary.size)
    realmDictionary["null"] = null
    assertEquals(2, realmDictionary.size)
    assertNull(realmDictionary["null"])
    val actualValue = realmDictionary[key]
    assertNotNull(actualValue)
    assertEquals(value, actualValue)
    assertFailsWith<IllegalArgumentException> {
        realmDictionary[null] = value
    }
}

internal inline fun <reified T : Any> assertRemove(key: String, value: T) {
    val realmDictionary = RealmDictionary<T>()
    realmDictionary[key] = value
    assertEquals(1, realmDictionary.size)
    realmDictionary.remove(key)
    assertEquals(0, realmDictionary.size)
    assertNull(realmDictionary[key])

    // Nothing happens when removing a null key
    realmDictionary.remove(null)
}

internal inline fun <reified T : Any> assertPutAll(keys: List<String>, values: List<T>) {
    val otherMap = HashMap<String, T?>().apply {
        this[keys[0]] = values[0]
        this[keys[1]] = values[1]
        this["null"] = null
    }
    val realmDictionary = RealmDictionary<T>()
    realmDictionary.putAll(otherMap)
    assertEquals(3, realmDictionary.size)
    assertTrue(realmDictionary.containsKey(keys[0]))
    assertTrue(realmDictionary.containsKey(keys[1]))
    assertTrue(realmDictionary.containsKey("null"))
    assertTrue(realmDictionary.containsValue(values[0]))
    assertTrue(realmDictionary.containsValue(values[1]))
    assertTrue(realmDictionary.containsValue(null))
}

internal inline fun <reified T : Any> assertClear(key: String, value: T) {
    val realmDictionary = RealmDictionary<T>()
    assertTrue(realmDictionary.isEmpty())
    realmDictionary[key] = value
    assertFalse(realmDictionary.isEmpty())
    realmDictionary.clear()
    assertTrue(realmDictionary.isEmpty())
}

internal inline fun <reified T : Any> assertKeySet(keys: List<String>, values: List<T>) {
    val otherDictionary = RealmDictionary<T>().apply {
        this[keys[0]] = values[0]
        this[keys[1]] = values[1]
    }
    val realmDictionary = RealmDictionary<T>(otherDictionary)
    val keySet = setOf(keys[0], keys[1])
    assertEquals(keySet, realmDictionary.keys)
}

internal inline fun <reified T : Any> assertValues(keys: List<String>, values: List<T>) {
    val otherDictionary = RealmDictionary<T?>().apply {
        this[keys[0]] = values[0]
        this[keys[1]] = values[1]
        this["null"] = null
    }
    val realmDictionary = RealmDictionary<T>(otherDictionary)
    val dictionaryValues = realmDictionary.values

    // Depending on the internal implementation of the chosen Map, the order might be altered
    values.forEach { value ->
        assertTrue(dictionaryValues.contains(value))
    }
}

internal inline fun <reified T : Any> assertEntrySet(keys: List<String>, values: List<T>) {
    val otherDictionary = RealmDictionary<T?>().apply {
        this[keys[0]] = values[0]
        this[keys[1]] = values[1]
        this["null"] = null
    }
    val realmDictionary = RealmDictionary<T>(otherDictionary)
    assertEquals(otherDictionary.entries, realmDictionary.entries)
}

internal inline fun <reified T : Any> assertFreeze() {
    val dictionary = RealmDictionary<T>()
    assertFailsWith<UnsupportedOperationException> {
        dictionary.freeze()
    }
}
