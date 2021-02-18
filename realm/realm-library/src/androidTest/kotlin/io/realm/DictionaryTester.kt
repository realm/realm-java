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

import io.realm.entities.AllTypes
import io.realm.kotlin.createObject
import io.realm.kotlin.where
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

internal inline fun <reified E : Any> assertConstructorWithAnotherMap(
        keys: List<String>,
        values: List<E>
) {
    val otherDictionary = RealmDictionary<E?>().apply {
        this[keys[0]] = values[0]
        this[keys[1]] = values[1]
        this["KEY_NULL"] = null
    }
    val realmDictionary = RealmDictionary<E>(otherDictionary)
    assertEquals(3, realmDictionary.size)
    assertTrue(realmDictionary.containsKey(keys[0]))
    assertTrue(realmDictionary.containsKey(keys[1]))
    assertTrue(realmDictionary.containsKey("KEY_NULL"))
    assertTrue(realmDictionary.containsValue(values[0]))
    assertTrue(realmDictionary.containsValue(values[1]))
    assertTrue(realmDictionary.containsValue(null))
}

internal inline fun <reified E : Any> assertUnmanagedIsManaged() {
    val realmDictionary = RealmDictionary<E>()
    assertFalse(realmDictionary.isManaged)
}

internal inline fun <reified E : Any> assertUnmanagedIsValid() {
    val realmDictionary = RealmDictionary<E>()
    assertTrue(realmDictionary.isValid)
}

internal inline fun <reified E : Any> assertUnmanagedIsFrozen() {
    val realmDictionary = RealmDictionary<E>()
    assertFalse(realmDictionary.isFrozen)
}

internal inline fun <reified E : Any> assertUnmanagedSize(key: String, value: E) {
    val realmDictionary = RealmDictionary<E>()
    assertEquals(0, realmDictionary.size)
    realmDictionary[key] = value
    assertEquals(1, realmDictionary.size)
}

internal inline fun <reified E : Any> assertUnmanagedIsEmpty(key: String, value: E) {
    val realmDictionary = RealmDictionary<E>()
    assertTrue(realmDictionary.isEmpty())
    realmDictionary[key] = value
    assertFalse(realmDictionary.isEmpty())
}

internal inline fun <reified E : Any> assertContainsKey(key: String, value: E) {
    val realmDictionary = RealmDictionary<E>()
    realmDictionary[key] = value
    assertTrue(realmDictionary.containsKey(key))
    assertFalse(realmDictionary.containsKey("ANOTHER_KEY"))
}

internal inline fun <reified E : Any> assertContainsValue(key: String, value: E, anotherValue: E) {
    val realmDictionary = RealmDictionary<E>()
    realmDictionary[key] = value
    assertTrue(realmDictionary.containsValue(value))
    assertFalse(realmDictionary.containsValue(anotherValue))
}

internal inline fun <reified E : Any> assertGet(key: String, value: E) {
    val realmDictionary = RealmDictionary<E?>()
    realmDictionary[key] = value
    val actualValue = realmDictionary[key]
    assertNotNull(actualValue)
    assertEquals(value, actualValue)
}

internal inline fun <reified E : Any> assertPut(key: String, value: E) {
    val realmDictionary = RealmDictionary<E?>()
    assertEquals(0, realmDictionary.size)
    realmDictionary[key] = value
    assertEquals(1, realmDictionary.size)
    realmDictionary["KEY_NULL"] = null
    assertEquals(2, realmDictionary.size)
    assertNull(realmDictionary["KEY_NULL"])
    val actualValue = realmDictionary[key]
    assertNotNull(actualValue)
    assertEquals(value, actualValue)
    assertFailsWith<IllegalArgumentException> {
        realmDictionary[null] = value
    }
}

internal inline fun <reified E : Any> assertRemove(key: String, value: E) {
    val realmDictionary = RealmDictionary<E>()
    realmDictionary[key] = value
    assertEquals(1, realmDictionary.size)
    realmDictionary.remove(key)
    assertEquals(0, realmDictionary.size)
    assertNull(realmDictionary[key])

    // Nothing happens when removing a null key
    realmDictionary.remove(null)
}

internal inline fun <reified E : Any> assertPutAll(keys: List<String>, values: List<E>) {
    val otherMap = HashMap<String, E?>().apply {
        this[keys[0]] = values[0]
        this[keys[1]] = values[1]
        this["KEY_NULL"] = null
    }
    val realmDictionary = RealmDictionary<E>()
    realmDictionary.putAll(otherMap)
    assertEquals(3, realmDictionary.size)
    assertTrue(realmDictionary.containsKey(keys[0]))
    assertTrue(realmDictionary.containsKey(keys[1]))
    assertTrue(realmDictionary.containsKey("KEY_NULL"))
    assertTrue(realmDictionary.containsValue(values[0]))
    assertTrue(realmDictionary.containsValue(values[1]))
    assertTrue(realmDictionary.containsValue(null))
}

internal inline fun <reified E : Any> assertClear(key: String, value: E) {
    val realmDictionary = RealmDictionary<E>()
    assertTrue(realmDictionary.isEmpty())
    realmDictionary[key] = value
    assertFalse(realmDictionary.isEmpty())
    realmDictionary.clear()
    assertTrue(realmDictionary.isEmpty())
}

internal inline fun <reified E : Any> assertKeySet(keys: List<String>, values: List<E>) {
    val otherDictionary = RealmDictionary<E>().apply {
        this[keys[0]] = values[0]
        this[keys[1]] = values[1]
    }
    val realmDictionary = RealmDictionary<E>(otherDictionary)
    val keySet = setOf(keys[0], keys[1])
    assertEquals(keySet, realmDictionary.keys)
}

internal inline fun <reified E : Any> assertValues(keys: List<String>, values: List<E>) {
    val otherDictionary = RealmDictionary<E?>().apply {
        this[keys[0]] = values[0]
        this[keys[1]] = values[1]
        this["KEY_NULL"] = null
    }
    val realmDictionary = RealmDictionary<E>(otherDictionary)
    val dictionaryValues = realmDictionary.values

    // Depending on the internal implementation of the chosen Map, the order might be altered
    values.forEach { value ->
        assertTrue(dictionaryValues.contains(value))
    }
}

internal inline fun <reified E : Any> assertEntrySet(keys: List<String>, values: List<E>) {
    val otherDictionary = RealmDictionary<E?>().apply {
        this[keys[0]] = values[0]
        this[keys[1]] = values[1]
        this["KEY_NULL"] = null
    }
    val realmDictionary = RealmDictionary<E>(otherDictionary)
    assertEquals(otherDictionary.entries, realmDictionary.entries)
}

internal inline fun <reified E : Any> assertFreeze() {
    val dictionary = RealmDictionary<E>()
    assertFailsWith<UnsupportedOperationException> {
        dictionary.freeze()
    }
}

internal inline fun <reified E : Any> initDictionary(
        keys: List<String>,
        values: List<E>
): RealmDictionary<E> {
    return RealmDictionary<E>().apply {
        put(keys[0], values[0])
        put(keys[1], values[1])
        put("KEY_NULL", null)
    }
}

//internal fun executeManagedOperation(realm: Realm, block: (AllTypes) -> Unit) {
//    realm.executeTransaction { transactionRealm ->
//        block.invoke(transactionRealm.createObject())
//    }
//    val allTypesObject = realm.where<AllTypes>().findFirst()
//    assertNotNull(allTypesObject)
//}

//internal inline fun <reified E : Any> executeManagedOperation(
//        realm: Realm,
//        dictionary: RealmDictionary<E>,
//        block: (AllTypes) -> Unit
//) {
//    realm.executeTransaction { transactionRealm ->
//        block.invoke(transactionRealm.createObject())
//    }
//    val allTypesObject = realm.where<AllTypes>().findFirst()
//    assertNotNull(allTypesObject)
//}
