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
    fun unmanaged_isManaged() {
        val realmMap = RealmMap<String, Int>()
        assertFalse(realmMap.isManaged)
    }

    @Test
    fun unmanaged_isValid() {
        val realmMap = RealmMap<String, Int>()
        assertTrue(realmMap.isValid)
    }

    @Test
    fun unmanaged_size() {
        val realmMap = RealmMap<String, Int>()
        assertEquals(0, realmMap.size)
        realmMap[KEY] = VALUE
        assertEquals(1, realmMap.size)
    }

    @Test
    fun unmanaged_isEmpty() {
        val realmMap = RealmMap<String, Int>()
        assertTrue(realmMap.isEmpty())
        realmMap[KEY] = VALUE
        assertFalse(realmMap.isEmpty())
    }

    @Test
    fun unmanaged_containsKey() {
        val realmMap = RealmMap<String, Int>()
        realmMap[KEY] = VALUE
        assertTrue(realmMap.containsKey(KEY))
    }

    @Test
    fun unmanaged_containsValue() {
        val realmMap = RealmMap<String, Int>()
        realmMap[KEY] = VALUE
        assertTrue(realmMap.containsValue(VALUE))
    }

    @Test
    fun unmanaged_get() {
        val realmMap = RealmMap<String, Int>()
        realmMap[KEY] = VALUE
        assertEquals(realmMap[KEY], VALUE)
    }

    @Test
    fun unmanaged_put() {
        val realmMap = RealmMap<String, Int>()
        assertEquals(0, realmMap.size)
        realmMap[KEY] = VALUE
        assertEquals(1, realmMap.size)
        assertEquals(realmMap[KEY], VALUE)
    }

    @Test
    fun unmanaged_remove() {
        val realmMap = RealmMap<String, Int>()
        realmMap[KEY] = VALUE
        assertEquals(1, realmMap.size)
        realmMap.remove(KEY)
        assertEquals(0, realmMap.size)
        assertNull(realmMap[KEY])
    }

    @Test
    fun unmanaged_putAll() {
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
    fun unmanaged_clear() {
        val realmMap = RealmMap<String, Int>()
        realmMap[KEY] = VALUE
        assertEquals(1, realmMap.size)
        realmMap.clear()
        assertEquals(0, realmMap.size)
        assertNull(realmMap[KEY])
    }

    @Test
    fun unmanaged_constructorWithMap() {
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
    fun unmanaged_keySet() {
        val otherMap = HashMap<String, Int>().apply {
            this[KEY_1] = VALUE_1
            this[KEY_2] = VALUE_2
        }
        val realmMap = RealmMap<String, Int>(otherMap)
        val keySet = setOf(KEY_1, KEY_2)
        assertEquals(keySet, realmMap.keys)
    }

    @Test
    fun unmanaged_values() {
        val otherMap = HashMap<String, Int>().apply {
            this[KEY_1] = VALUE_1
            this[KEY_2] = VALUE_2
        }
        val realmMap = RealmMap<String, Int>(otherMap)
        val valueCollection = listOf(VALUE_1, VALUE_2)
        assertEquals(valueCollection, realmMap.values.toList())
    }

    @Test
    fun unmanaged_entrySet() {
        val otherMap = HashMap<String, Int>().apply {
            this[KEY_1] = VALUE_1
            this[KEY_2] = VALUE_2
        }
        val realmMap = RealmMap<String, Int>(otherMap)
        assertEquals(otherMap.entries, realmMap.entries)
    }

    @Test
    fun unmanaged_freeze() {
        assertFailsWith<UnsupportedOperationException> {
            RealmMap<String, Int>().freeze()
        }
    }

    // ------------------------------------------
    // Managed map - TBD
    // ------------------------------------------

    // FIXME: TBD
}
