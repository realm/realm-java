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
 * Contains all the methods to test in RealmDictionary plus some other convenience ones.
 */
interface DictionaryTester {
    fun addRealmInstance(realm: Realm)
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
    fun copyToRealm()
    fun copyFromRealm()
}

//--------------------------------------------------------------------------------------------------
// Unmanaged section
//--------------------------------------------------------------------------------------------------

internal inline fun <reified E : Any> assertConstructorWithAnotherMap(
        keys: List<String>,
        values: List<E?>
) {
    val otherDictionary = RealmDictionary<E?>().apply {
        this[keys[0]] = values[0]
        this[keys[1]] = values[1]
        this[keys[2]] = null
    }
    val realmDictionary = RealmDictionary<E>(otherDictionary)
    assertEquals(3, realmDictionary.size)
    assertTrue(realmDictionary.containsKey(keys[0]))
    assertTrue(realmDictionary.containsKey(keys[1]))
    assertTrue(realmDictionary.containsKey(keys[2]))
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

internal inline fun <reified E : Any> assertUnmanagedSize(key: String, value: E?) {
    val realmDictionary = RealmDictionary<E>()
    assertEquals(0, realmDictionary.size)
    realmDictionary[key] = value
    assertEquals(1, realmDictionary.size)
}

internal inline fun <reified E : Any> assertUnmanagedIsEmpty(key: String, value: E?) {
    val realmDictionary = RealmDictionary<E>()
    assertTrue(realmDictionary.isEmpty())
    realmDictionary[key] = value
    assertFalse(realmDictionary.isEmpty())
}

internal inline fun <reified E : Any> assertUnmanagedContainsKey(key: String, value: E?) {
    val realmDictionary = RealmDictionary<E>()
    realmDictionary[key] = value
    assertTrue(realmDictionary.containsKey(key))
    assertFalse(realmDictionary.containsKey("ANOTHER_KEY"))
}

internal inline fun <reified E : Any> assertUnmanagedContainsValue(
        key: String,
        value: E?,
        anotherValue: E?
) {
    val realmDictionary = RealmDictionary<E>()
    realmDictionary[key] = value
    assertTrue(realmDictionary.containsValue(value))
    assertFalse(realmDictionary.containsValue(anotherValue))
}

internal inline fun <reified E : Any> assertUnmanagedGet(key: String, value: E?) {
    val realmDictionary = RealmDictionary<E?>()
    realmDictionary[key] = value
    val actualValue = realmDictionary[key]
    assertNotNull(actualValue)
    assertEquals(value, actualValue)
}

internal inline fun <reified E : Any> assertUnmanagedPut(key: String, value: E?) {
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

internal inline fun <reified E : Any> assertUnmanagedRemove(key: String, value: E?) {
    val realmDictionary = RealmDictionary<E>()
    realmDictionary[key] = value
    assertEquals(1, realmDictionary.size)
    realmDictionary.remove(key)
    assertEquals(0, realmDictionary.size)
    assertNull(realmDictionary[key])

    // Nothing happens when removing a null key
    realmDictionary.remove(null)
}

internal inline fun <reified E : Any> assertUnmanagedPutAll(keys: List<String>, values: List<E?>) {
    val otherMap = HashMap<String, E?>().apply {
        this[keys[0]] = values[0]
        this[keys[1]] = values[1]
        this[keys[2]] = null
    }
    val realmDictionary = RealmDictionary<E>()
    realmDictionary.putAll(otherMap)
    assertEquals(3, realmDictionary.size)
    assertTrue(realmDictionary.containsKey(keys[0]))
    assertTrue(realmDictionary.containsKey(keys[1]))
    assertTrue(realmDictionary.containsKey(keys[2]))
    assertTrue(realmDictionary.containsValue(values[0]))
    assertTrue(realmDictionary.containsValue(values[1]))
    assertTrue(realmDictionary.containsValue(null))
}

internal inline fun <reified E : Any> assertUnmanagedClear(key: String, value: E?) {
    val realmDictionary = RealmDictionary<E>()
    assertTrue(realmDictionary.isEmpty())
    realmDictionary[key] = value
    assertFalse(realmDictionary.isEmpty())
    realmDictionary.clear()
    assertTrue(realmDictionary.isEmpty())
}

internal inline fun <reified E : Any> assertUnmanagedKeySet(keys: List<String>, values: List<E?>) {
    val otherDictionary = RealmDictionary<E>().apply {
        this[keys[0]] = values[0]
        this[keys[1]] = values[1]
    }
    val realmDictionary = RealmDictionary<E>(otherDictionary)
    val keySet = setOf(keys[0], keys[1])
    assertEquals(keySet, realmDictionary.keys)
}

internal inline fun <reified E : Any> assertUnmanagedValues(keys: List<String>, values: List<E?>) {
    val otherDictionary = RealmDictionary<E?>().apply {
        this[keys[0]] = values[0]
        this[keys[1]] = values[1]
        this[keys[2]] = null
    }
    val realmDictionary = RealmDictionary<E>(otherDictionary)
    val dictionaryValues = realmDictionary.values

    // Depending on the internal implementation of the chosen Map, the order might be altered
    values.forEach { value ->
        assertTrue(dictionaryValues.contains(value))
    }
}

internal inline fun <reified E : Any> assertUnmanagedEntrySet(
        keys: List<String>,
        values: List<E?>
) {
    val otherDictionary = RealmDictionary<E?>().apply {
        this[keys[0]] = values[0]
        this[keys[1]] = values[1]
        this[keys[2]] = null
    }
    val realmDictionary = RealmDictionary<E>(otherDictionary)
    assertEquals(otherDictionary.entries, realmDictionary.entries)
}

internal inline fun <reified E : Any> assertUnmanagedFreeze() {
    val dictionary = RealmDictionary<E>()
    assertFailsWith<UnsupportedOperationException> {
        dictionary.freeze()
    }
}

//--------------------------------------------------------------------------------------------------
// Managed section
//--------------------------------------------------------------------------------------------------

internal fun createAllTypesManagedContainerAndAssert(realm: Realm): AllTypes {
    realm.executeTransaction { transactionRealm ->
        transactionRealm.createObject<AllTypes>()
    }
    val allTypesObject = realm.where<AllTypes>().findFirst()
    assertNotNull(allTypesObject)
    return allTypesObject
}

internal fun assertManagedIsManaged(dictionary: RealmDictionary<*>) {
    assertNotNull(dictionary)
    assertTrue(dictionary.isManaged)
}

internal fun assertManagedIsValid(realm: Realm, dictionary: RealmDictionary<*>) {
    assertNotNull(dictionary)
    assertTrue(dictionary.isValid)

    realm.executeTransaction {
        it.deleteAll()
    }

    assertFalse(dictionary.isValid)
}

internal fun assertManagedIsFrozen(dictionary: RealmDictionary<*>) {
    assertNotNull(dictionary)
    assertFalse(dictionary.isFrozen)
    val frozenDictionary = dictionary.freeze()
    assertTrue(frozenDictionary.isFrozen)
}

internal inline fun <reified E : Any> RealmDictionary<E>.init(
        keys: List<String>,
        values: List<E?>
): RealmDictionary<E> {
    return this.apply {
        put(keys[0], values[0])
        put(keys[1], values[1])
        put(keys[2], values[2])
    }
}

/**
 * The [genericOperation] and [postOperationAssertion] lambdas are customizable in the sense that
 * in some tests you might not want to do anything at all in the operation lambda and assert
 * something in the post lambda, whereas in another test you might want to do something in the
 * operation lambda (including assertions) and nothing in the post lambda.
 */
internal inline fun <reified E : Any> assertManagedGenericOperation(
        realm: Realm,
        keys: List<String>,
        values: List<E?>,
        dictionaryGetter: (AllTypes) -> RealmDictionary<E>,
        preOperationAssertion: (RealmDictionary<E>) -> Unit,
        genericOperation: (Realm, RealmDictionary<E>, AllTypes) -> Unit,
        postOperationAssertion: (RealmDictionary<E>) -> Unit = { /* no-op by default */ }
) {
    val allTypes: AllTypes = createAllTypesManagedContainerAndAssert(realm)
    val dictionary: RealmDictionary<E> = dictionaryGetter.invoke(allTypes)

    preOperationAssertion.invoke(dictionary)

    realm.executeTransaction {
        dictionary.init(keys, values)
    }
    genericOperation.invoke(realm, dictionary, allTypes)

    postOperationAssertion.invoke(dictionary)
}

internal fun createPrefilledAllTypesManagedContainerAndAssert(
        realm: Realm,
        populatingOperation: (AllTypes) -> Unit
): AllTypes {
    realm.executeTransaction { transactionRealm ->
        val allTypes = AllTypes().apply {
            populatingOperation.invoke(this)
        }
        transactionRealm.copyToRealm(allTypes)
    }
    val allTypesObject = realm.where<AllTypes>().findFirst()
    assertNotNull(allTypesObject)
    return allTypesObject
}

internal inline fun <reified E : Any> assertContains(
        dictionary: RealmDictionary<E>,
        keys: List<String>,
        values: List<E?>
) {
    for (i in keys.indices) {
        dictionary.containsKey(keys[i])
        dictionary.containsValue(values[i])
    }
}
