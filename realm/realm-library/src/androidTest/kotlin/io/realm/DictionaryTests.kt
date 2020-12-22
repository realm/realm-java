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
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.*

private const val KEY = "KEY"
private const val KEY_1 = "KEY_1"
private const val KEY_2 = "KEY_2"
private const val VALUE = 666
private const val VALUE_1 = 1
private const val VALUE_2 = 2

@RunWith(AndroidJUnit4::class)
class DictionaryTests {

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
    // Managed map - TBD
    // ------------------------------------------

    // TODO: sanity-check tests for temporary schema validation - move to an appropriate place

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

    @Test
    fun schemaTest() {
        val objectSchema = realm.schema.get(DictionaryClass.CLASS_NAME)

        assertNotNull(objectSchema)

        assertTrue(objectSchema.hasField(DictionaryClass.MIXED_DICTIONARY_FIELD_NAME))
        assertEquals(objectSchema.getFieldType(DictionaryClass.MIXED_DICTIONARY_FIELD_NAME), RealmFieldType.STRING_TO_MIXED_MAP)

        assertTrue(objectSchema.hasField(DictionaryClass.BOOLEAN_DICTIONARY_FIELD_NAME))
        assertEquals(objectSchema.getFieldType(DictionaryClass.BOOLEAN_DICTIONARY_FIELD_NAME), RealmFieldType.STRING_TO_BOOLEAN_MAP)
    }
}

@RealmModule(classes = [DictionaryClass::class, Dog::class, Cat::class, Owner::class, DogPrimaryKey::class])
class MapModule
