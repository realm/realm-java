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
class MapTests {

    // ------------------------------------------
    // Unmanaged map
    // ------------------------------------------

    @Test
    fun unmanagedMap_isManaged() {
        val realmMap = RealmMap<String, Int>()
        assertFalse(realmMap.isManaged)
    }

    @Test
    fun unmanagedMap_isValid() {
        val realmMap = RealmMap<String, Int>()
        assertTrue(realmMap.isValid)
    }

    @Test
    fun unmanagedMap_size() {
        val realmMap = RealmMap<String, Int>()
        assertEquals(0, realmMap.size)
        realmMap[KEY] = VALUE
        assertEquals(1, realmMap.size)
    }

    @Test
    fun unmanagedMap_isEmpty() {
        val realmMap = RealmMap<String, Int>()
        assertTrue(realmMap.isEmpty())
        realmMap[KEY] = VALUE
        assertFalse(realmMap.isEmpty())
    }

    @Test
    fun unmanagedMap_containsKey() {
        val realmMap = RealmMap<String, Int>()
        realmMap[KEY] = VALUE
        assertTrue(realmMap.containsKey(KEY))
    }

    @Test
    fun unmanagedMap_containsValue() {
        val realmMap = RealmMap<String, Int>()
        realmMap[KEY] = VALUE
        assertTrue(realmMap.containsValue(VALUE))
    }

    @Test
    fun unmanagedMap_get() {
        val realmMap = RealmMap<String, Int>()
        realmMap[KEY] = VALUE
        assertEquals(realmMap[KEY], VALUE)
    }

    @Test
    fun unmanagedMap_put() {
        val realmMap = RealmMap<String, Int>()
        assertEquals(0, realmMap.size)
        realmMap[KEY] = VALUE
        assertEquals(1, realmMap.size)
        assertEquals(realmMap[KEY], VALUE)
    }

    @Test
    fun unmanagedMap_remove() {
        val realmMap = RealmMap<String, Int>()
        realmMap[KEY] = VALUE
        assertEquals(1, realmMap.size)
        realmMap.remove(KEY)
        assertEquals(0, realmMap.size)
        assertNull(realmMap[KEY])
    }

    @Test
    fun unmanagedMap_putAll() {
        val otherMap = HashMap<String, Int>().apply {
            this[KEY_1] = VALUE_1
            this[KEY_2] = VALUE_2
        }
        val realmMap = RealmMap<String, Int>()
        realmMap.putAll(otherMap)
        assertEquals(2, realmMap.size)
        assertTrue(realmMap.containsKey(KEY_1))
        assertTrue(realmMap.containsKey(KEY_2))
        assertTrue(realmMap.containsValue(VALUE_1))
        assertTrue(realmMap.containsValue(VALUE_2))
    }

    @Test
    fun unmanagedMap_clear() {
        val realmMap = RealmMap<String, Int>()
        realmMap[KEY] = VALUE
        assertEquals(1, realmMap.size)
        realmMap.clear()
        assertEquals(0, realmMap.size)
        assertNull(realmMap[KEY])
    }

    @Test
    fun unmanagedMap_constructorWithMap() {
        val otherMap = HashMap<String, Int>().apply {
            this[KEY_1] = VALUE_1
            this[KEY_2] = VALUE_2
        }
        val realmMap = RealmMap<String, Int>(otherMap)
        assertEquals(2, realmMap.size)
        assertTrue(realmMap.containsKey(KEY_1))
        assertTrue(realmMap.containsKey(KEY_2))
        assertTrue(realmMap.containsValue(VALUE_1))
        assertTrue(realmMap.containsValue(VALUE_2))
    }

    @Test
    fun unmanagedMap_keySet() {
        val otherMap = HashMap<String, Int>().apply {
            this[KEY_1] = VALUE_1
            this[KEY_2] = VALUE_2
        }
        val realmMap = RealmMap<String, Int>(otherMap)
        val keySet = setOf(KEY_1, KEY_2)
        assertEquals(keySet, realmMap.keys)
    }

    @Test
    fun unmanagedMap_values() {
        val otherMap = HashMap<String, Int>().apply {
            this[KEY_1] = VALUE_1
            this[KEY_2] = VALUE_2
        }
        val realmMap = RealmMap<String, Int>(otherMap)
        val valueCollection = listOf(VALUE_1, VALUE_2)
        assertEquals(valueCollection, realmMap.values.toList())
    }

    @Test
    fun unmanagedMap_entrySet() {
        val otherMap = HashMap<String, Int>().apply {
            this[KEY_1] = VALUE_1
            this[KEY_2] = VALUE_2
        }
        val realmMap = RealmMap<String, Int>(otherMap)
        val expectedEntries = listOf(
                Pair(KEY_1, VALUE_1),
                Pair(KEY_2, VALUE_2)
        )

        val actualEntries = realmMap.entries.map { it.toPair() }
        assertEquals(expectedEntries, actualEntries)
    }

    @Test
    fun unmanagedMap_freeze() {
        assertFailsWith<UnsupportedOperationException> {
            RealmMap<String, Int>().freeze()
        }
    }

    // ------------------------------------------
    // Managed map - TBD
    // ------------------------------------------

    // FIXME: temporary methods

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
        val objectSchema = realm.schema.get("MapClass")
        assertNotNull(objectSchema)
        assertTrue(objectSchema.hasField("myMap"))
        assertEquals(objectSchema.getFieldType("myMap"), RealmFieldType.STRING_TO_MIXED_MAP)
    }
}

@RealmModule(classes = [MapClass::class, Dog::class, Cat::class, Owner::class, DogPrimaryKey::class])
class MapModule
