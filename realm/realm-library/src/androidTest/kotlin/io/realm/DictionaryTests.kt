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

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.realm.annotations.RealmModule
import io.realm.entities.*
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.*

private const val KEY = "KEY"
private const val KEY_1 = "KEY_1"
private const val KEY_2 = "KEY_2"
private const val KEY_HELLO = "Hello"
private const val KEY_BYE = "Bye"
private const val VALUE = 666
private const val VALUE_1 = 1
private const val VALUE_2 = 2
private const val VALUE_HELLO = true
private const val VALUE_BYE = false

@RunWith(AndroidJUnit4::class)
class DictionaryTests {

    private lateinit var config: RealmConfiguration
    private lateinit var realm: Realm

    @Before
    fun setUp() {
        Realm.init(InstrumentationRegistry.getInstrumentation().context)
        config = RealmConfiguration.Builder()
                .modules(MapModule())
                .allowQueriesOnUiThread(true)
                .allowWritesOnUiThread(true)
                .build()
        realm = Realm.getInstance(config)
    }

    @After
    fun tearDown() {
        realm.close()
        Realm.deleteRealm(config)
    }

    // ------------------------------------------
    // Unmanaged map
    // ------------------------------------------

    @Test
    fun unmanaged_isManaged() {
        val realmDictionary = RealmDictionary<Int>()
        assertFalse(realmDictionary.isManaged)
    }

    @Test
    fun unmanaged_isValid() {
        val realmDictionary = RealmDictionary<Int>()
        assertTrue(realmDictionary.isValid)
    }

    @Test
    fun unmanaged_size() {
        val realmDictionary = RealmDictionary<Int>()
        assertEquals(0, realmDictionary.size)
        realmDictionary[KEY] = VALUE
        assertEquals(1, realmDictionary.size)
    }

    @Test
    fun unmanaged_isEmpty() {
        val realmDictionary = RealmDictionary<Int>()
        assertTrue(realmDictionary.isEmpty())
        realmDictionary[KEY] = VALUE
        assertFalse(realmDictionary.isEmpty())
    }

    @Test
    fun unmanaged_containsKey() {
        val realmDictionary = RealmDictionary<Int>()
        realmDictionary[KEY] = VALUE
        assertTrue(realmDictionary.containsKey(KEY))
    }

    @Test
    fun unmanaged_containsValue() {
        val realmDictionary = RealmDictionary<Int>()
        realmDictionary[KEY] = VALUE
        assertTrue(realmDictionary.containsValue(VALUE))
    }

    @Test
    fun unmanaged_get() {
        val realmDictionary = RealmDictionary<Int>()
        realmDictionary[KEY] = VALUE
        assertEquals(realmDictionary[KEY], VALUE)
    }

    @Test
    fun unmanaged_put() {
        val realmDictionary = RealmDictionary<Int>()
        assertEquals(0, realmDictionary.size)
        realmDictionary[KEY] = VALUE
        assertEquals(1, realmDictionary.size)
        assertEquals(realmDictionary[KEY], VALUE)
    }

    @Test
    fun unmanaged_put_nullKey() {
        val realmDictionary = RealmDictionary<Int>()
        assertEquals(0, realmDictionary.size)
        assertFailsWith<IllegalArgumentException> {
            realmDictionary[null] = VALUE
        }
    }

    @Test
    fun unmanaged_remove() {
        val realmDictionary = RealmDictionary<Int>()
        realmDictionary[KEY] = VALUE
        assertEquals(1, realmDictionary.size)
        realmDictionary.remove(KEY)
        assertEquals(0, realmDictionary.size)
        assertNull(realmDictionary[KEY])
    }

    @Test
    fun unmanaged_putAll() {
        val otherMap = HashMap<String, Int>().apply {
            this[KEY_1] = VALUE_1
            this[KEY_2] = VALUE_2
        }
        val realmDictionary = RealmDictionary<Int>()
        realmDictionary.putAll(otherMap)
        assertEquals(2, realmDictionary.size)
        assertTrue(realmDictionary.containsKey(KEY_1))
        assertTrue(realmDictionary.containsKey(KEY_2))
        assertTrue(realmDictionary.containsValue(VALUE_1))
        assertTrue(realmDictionary.containsValue(VALUE_2))
    }

    @Test
    fun unmanaged_clear() {
        val realmDictionary = RealmDictionary<Int>()
        realmDictionary[KEY] = VALUE
        assertEquals(1, realmDictionary.size)
        realmDictionary.clear()
        assertEquals(0, realmDictionary.size)
        assertNull(realmDictionary[KEY])
    }

    @Test
    fun unmanaged_constructorWithMap() {
        val otherDictionary = RealmDictionary<Int>().apply {
            this[KEY_1] = VALUE_1
            this[KEY_2] = VALUE_2
        }
        val realmDictionary = RealmDictionary<Int>(otherDictionary)
        assertEquals(2, realmDictionary.size)
        assertTrue(realmDictionary.containsKey(KEY_1))
        assertTrue(realmDictionary.containsKey(KEY_2))
        assertTrue(realmDictionary.containsValue(VALUE_1))
        assertTrue(realmDictionary.containsValue(VALUE_2))
    }

    @Test
    fun unmanaged_keySet() {
        val otherDictionary = RealmDictionary<Int>().apply {
            this[KEY_1] = VALUE_1
            this[KEY_2] = VALUE_2
        }
        val realmDictionary = RealmDictionary<Int>(otherDictionary)
        val keySet = setOf(KEY_1, KEY_2)
        assertEquals(keySet, realmDictionary.keys)
    }

    @Test
    fun unmanaged_values() {
        val otherDictionary = RealmDictionary<Int>().apply {
            this[KEY_1] = VALUE_1
            this[KEY_2] = VALUE_2
        }
        val realmDictionary = RealmDictionary<Int>(otherDictionary)
        val valueCollection = listOf(VALUE_1, VALUE_2)
        assertEquals(valueCollection, realmDictionary.values.toList())
    }

    @Test
    fun unmanaged_entrySet() {
        val otherDictionary = RealmDictionary<Int>().apply {
            this[KEY_1] = VALUE_1
            this[KEY_2] = VALUE_2
        }
        val realmDictionary = RealmDictionary<Int>(otherDictionary)
        assertEquals(otherDictionary.entries, realmDictionary.entries)
    }

    @Test
    fun unmanaged_freeze() {
        assertFailsWith<UnsupportedOperationException> {
            RealmDictionary<Int>().freeze()
        }
    }

    // ------------------------------------------
    // Managed map
    // ------------------------------------------

    @Test
    fun managed_isManaged() {
        assertTrue(initDictionaryAndAssert().isManaged)
    }

    @Test
    fun managed_isValid() {
        assertTrue(initDictionaryAndAssert().isValid)

        val dictionaryObject = realm.where<DictionaryClass>().findFirst()
        assertNotNull(dictionaryObject)

        realm.executeTransaction {
            it.delete(DictionaryClass::class.java)
        }

        assertFalse(dictionaryObject.isValid)
    }

    @Test
    fun managed_isFrozen() {
        assertFalse(initDictionaryAndAssert().isFrozen)

        // TODO: add another check when 'freeze' works
    }

    @Test
    fun managed_size() {
        assertEquals(2, initDictionaryAndAssert().size)
    }

    @Test
    fun managed_isEmpty() {
        assertFalse(initDictionaryAndAssert().isEmpty())

        realm.executeTransaction {
            val dictionaryObject = realm.where<DictionaryClass>().findFirst()
            assertNotNull(dictionaryObject)
            dictionaryObject.myBooleanDictionary!!.let { dictionary ->
                dictionary.clear()

                assertTrue(dictionary.isEmpty())
            }
        }
    }

    @Test
    @Ignore
    fun managed_containsKey() {
        // TODO
    }

    @Test
    @Ignore
    fun managed_containsValue() {
        // TODO
    }

    @Test
    @Ignore
    fun managed_putAll() {
        // TODO
    }

    @Test
    fun managed_clear() {
        val dictionary = initDictionaryAndAssert()
        assertFalse(dictionary.isEmpty())

        realm.executeTransaction {
            dictionary.clear()
        }

        assertTrue(dictionary.isEmpty())
    }

    @Test
    @Ignore
    fun managed_keySet() {
        // TODO
    }

    @Test
    @Ignore
    fun managed_values() {
        // TODO
    }

    @Test
    @Ignore
    fun managed_entrySet() {
        // TODO
    }

    @Test
    @Ignore
    fun managed_freeze() {
        // TODO
    }

    // TODO: sanity-check tests for temporary schema validation - move to an appropriate place

    @Test
    fun schemaTest() {
        val objectSchema = realm.schema.get(DictionaryClass.CLASS_NAME)

        assertNotNull(objectSchema)

        assertTrue(objectSchema.hasField(DictionaryClass.MIXED_DICTIONARY_FIELD_NAME))
        assertEquals(objectSchema.getFieldType(DictionaryClass.MIXED_DICTIONARY_FIELD_NAME), RealmFieldType.STRING_TO_MIXED_MAP)

        assertTrue(objectSchema.hasField(DictionaryClass.BOOLEAN_DICTIONARY_FIELD_NAME))
        assertEquals(objectSchema.getFieldType(DictionaryClass.BOOLEAN_DICTIONARY_FIELD_NAME), RealmFieldType.STRING_TO_BOOLEAN_MAP)
    }

    private fun createBooleanRealmDictionary(): RealmDictionary<Boolean> {
        return RealmDictionary<Boolean>().apply {
            put(KEY_HELLO, VALUE_HELLO)
            put(KEY_BYE, VALUE_BYE)
        }
    }

    private fun createMixedRealmDictionary(): RealmDictionary<Mixed> {
        return RealmDictionary<Mixed>().apply {
            put(KEY_HELLO, Mixed.valueOf(VALUE_HELLO))
            put(KEY_BYE, Mixed.valueOf(VALUE_BYE))
        }
    }

    private fun initDictionary() {
        realm.executeTransaction { transactionRealm ->
            transactionRealm.createObject<DictionaryClass>()
                    .also { it.myBooleanDictionary = createBooleanRealmDictionary() }
        }
    }

    private fun baseAssertions(dictionary: RealmDictionary<*>?) {
        assertNotNull(dictionary)
        assertFalse(dictionary.isEmpty())
        assertEquals(2, dictionary.size)
        assertEquals(VALUE_HELLO, dictionary[KEY_HELLO])
        assertEquals(VALUE_BYE, dictionary[KEY_BYE])
    }

    private fun initDictionaryAndAssert(): RealmDictionary<*> {
        initDictionary()

        val dictionaryObject = realm.where<DictionaryClass>().findFirst()
        assertNotNull(dictionaryObject)

        return dictionaryObject.myBooleanDictionary!!
                .also { baseAssertions(it) }
    }

    @Test
    fun copyToRealm_boolean() {
        realm.executeTransaction { transactionRealm ->
            val dictionaryObject = DictionaryClass().apply {
                myBooleanDictionary = createBooleanRealmDictionary()
            }

            val dictionaryObjectFromRealm = transactionRealm.copyToRealm(dictionaryObject)
            val dictionaryFromRealm = dictionaryObjectFromRealm.myBooleanDictionary
            assertNotNull(dictionaryFromRealm)

            assertEquals(VALUE_HELLO, dictionaryFromRealm[KEY_HELLO])
            assertEquals(VALUE_BYE, dictionaryFromRealm[KEY_BYE])
        }
    }

    @Test
    fun copyToRealm_mixedBoolean() {
        realm.executeTransaction { transactionRealm ->
            val dictionaryObject = DictionaryClass().apply {
                myMixedDictionary = createMixedRealmDictionary()
            }

            val dictionaryObjectFromRealm = transactionRealm.copyToRealm(dictionaryObject)
            val dictionaryFromRealm = dictionaryObjectFromRealm.myBooleanDictionary
            assertNotNull(dictionaryFromRealm)

//            val mixedHello = dictionaryFromRealm[KEY_HELLO]
//            val mixedBye = dictionaryFromRealm[KEY_BYE]
//            val kajhs = 0
        }
    }
}

@RealmModule(classes = [DictionaryClass::class, Dog::class, Cat::class, Owner::class, DogPrimaryKey::class])
class MapModule
