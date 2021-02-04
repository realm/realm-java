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
import io.realm.entities.DictionaryClass
import io.realm.entities.embedded.EmbeddedSimpleChild
import io.realm.entities.embedded.EmbeddedSimpleParent
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import org.bson.types.Decimal128
import org.bson.types.ObjectId
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*
import kotlin.collections.HashMap
import kotlin.test.*

private const val KEY = "KEY"
private const val KEY_1 = "KEY_1"
private const val KEY_2 = "KEY_2"
private const val KEY_HELLO = "Hello"
private const val KEY_BYE = "Bye"
private const val KEY_NULL = "Null"
private const val VALUE = 666
private const val VALUE_1 = 1
private const val VALUE_2 = 2
private const val VALUE_HELLO = true
private const val VALUE_BYE = false
private const val VALUE_HELLO_STRING = "HELLO"
private const val VALUE_BYE_STRING = "BYE"
private const val VALUE_HELLO_INTEGER = 42
private const val VALUE_BYE_INTEGER = 666
private val VALUE_NULL = null

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

//    @Test
//    fun schemaTest() {
//        val objectSchema = realm.schema.get(DictionaryClass.CLASS_NAME)
//
//        assertNotNull(objectSchema)
//
//        assertTrue(objectSchema.hasField(DictionaryClass.UUID_DICTIONARY_FIELD_NAME))
//        assertEquals(objectSchema.getFieldType(DictionaryClass.UUID_DICTIONARY_FIELD_NAME), RealmFieldType.STRING_TO_UUID_MAP)
//
//        assertTrue(objectSchema.hasField(DictionaryClass.MIXED_DICTIONARY_FIELD_NAME))
//        assertEquals(objectSchema.getFieldType(DictionaryClass.MIXED_DICTIONARY_FIELD_NAME), RealmFieldType.STRING_TO_MIXED_MAP)
//
//        assertTrue(objectSchema.hasField(DictionaryClass.BOOLEAN_DICTIONARY_FIELD_NAME))
//        assertEquals(objectSchema.getFieldType(DictionaryClass.BOOLEAN_DICTIONARY_FIELD_NAME), RealmFieldType.STRING_TO_BOOLEAN_MAP)
//
//        assertTrue(objectSchema.hasField(DictionaryClass.STRING_DICTIONARY_FIELD_NAME))
//        assertEquals(objectSchema.getFieldType(DictionaryClass.STRING_DICTIONARY_FIELD_NAME), RealmFieldType.STRING_TO_STRING_MAP)
//
//        assertTrue(objectSchema.hasField(DictionaryClass.INTEGER_DICTIONARY_FIELD_NAME))
//        assertEquals(objectSchema.getFieldType(DictionaryClass.INTEGER_DICTIONARY_FIELD_NAME), RealmFieldType.STRING_TO_INTEGER_MAP)
//    }

    @Test
    fun copyToRealm_realmModel() {
        val helloDogId = "Hello Dog"
        val myDog = MyDog().apply { id = helloDogId }

        realm.executeTransaction { transactionRealm ->
            val dictionaryObject = DictionaryClass().apply {
                myDogDictionary = RealmDictionary<MyDog>().apply {
                    put(KEY_HELLO, myDog)

                    // TODO: remove comment once https://github.com/realm/realm-core/issues/4374 is fixed
//                    put(KEY_NULL, null)
                }
            }

            val dictionaryObjectFromRealm = transactionRealm.copyToRealm(dictionaryObject)
            val dictionaryFromRealm = dictionaryObjectFromRealm.myDogDictionary
            assertNotNull(dictionaryFromRealm)

            dictionaryFromRealm[KEY_HELLO].let { helloDog ->
                assertNotNull(helloDog)
                assertEquals(helloDogId, helloDog.id)

            }
            // TODO: remove comment once https://github.com/realm/realm-core/issues/4374 is fixed
//            assertNull(dictionaryFromRealm[KEY_NULL])
        }
    }

    @Test
    fun copyToRealm_UUID() {
        val uuid1 = UUID.randomUUID()
        val uuid2 = UUID.randomUUID()

        realm.executeTransaction { transactionRealm ->
            val dictionaryObject = DictionaryClass().apply {
                myUUIDDictionary = RealmDictionary<UUID>().apply {
                    put(KEY_HELLO, uuid1)
                    put(KEY_BYE, uuid2)
                    put(KEY_NULL, null)
                }
            }

            val dictionaryObjectFromRealm = transactionRealm.copyToRealm(dictionaryObject)
            val dictionaryFromRealm = dictionaryObjectFromRealm.myUUIDDictionary
            assertNotNull(dictionaryFromRealm)

            assertEquals(uuid1.toString(), dictionaryFromRealm[KEY_HELLO].toString())
            assertEquals(uuid2.toString(), dictionaryFromRealm[KEY_BYE].toString())
            assertNull(dictionaryFromRealm[KEY_NULL])
        }
    }

    @Test
    fun copyToRealm_mixed() {
        val entries = mapOf<String, Mixed>(
                "INTEGER" to Mixed.valueOf(42 as Int),
                "BOOLEAN" to Mixed.valueOf(true),
                "STRING" to Mixed.valueOf("this is a string"),
                "BINARY" to Mixed.valueOf(ByteArray(1).apply { set(0, 42) }),
                "DATE" to Mixed.valueOf(Date()),
                "FLOAT" to Mixed.valueOf(42F),
                "DOUBLE" to Mixed.valueOf(42.toDouble()),
                "SHORT" to Mixed.valueOf(42.toShort()),
                "BYTE" to Mixed.valueOf(42.toByte()),
                "DECIMAL128" to Mixed.valueOf(Decimal128(42)),
                "OBJECT_ID" to Mixed.valueOf(ObjectId()),
                "UUID" to Mixed.valueOf(UUID.randomUUID()),
                "NULL" to Mixed.nullValue()
        )

        val unmanagedModel: MyDog = MyDog().apply { id = "unmanaged" }
        val mixedModel = Mixed.valueOf(unmanagedModel)

        realm.executeTransaction { transactionRealm ->
            val dictionaryObject = DictionaryClass().apply {
                myMixedDictionary = RealmDictionary<Mixed>().apply {
                    putAll(entries)
                }
                myMixedDictionary!!["NULL_VALUE"] = null
                myMixedDictionary!!["UNMANAGED_MIXED_MODEL"] = mixedModel
                val managedModel = transactionRealm.createObject<MyDog>().apply { id = "managed" }
                myMixedDictionary!!["MANAGED_MIXED_MODEL"] = Mixed.valueOf(managedModel)
            }
            transactionRealm.copyToRealm(dictionaryObject)
        }

        val dictionaryObjectFromRealm = realm.where<DictionaryClass>()
                .findFirst()
        assertNotNull(dictionaryObjectFromRealm)

        val dictionaryFromRealm = dictionaryObjectFromRealm.myMixedDictionary
        assertNotNull(dictionaryFromRealm)

        // Iterate over all sample entries and compare them to the values we got from realm
        entries.entries.forEach { entry ->
            dictionaryFromRealm[entry.key].also { value ->
                assertNotNull(value)
                if (value.type == MixedType.BINARY) {
                    assertEquals(entry.value.asBinary()[0], value.asBinary()[0])
                } else {
                    assertEquals(entry.value, value)
                }
            }
        }

        // Finally check the RealmModels we inserted manually are actually there
        realm.where<MyDog>()
                .findAll()
                .let { models ->
                    assertEquals(2, models.size)
                }

        dictionaryFromRealm["NULL_VALUE"].also { nullMixed ->
            assertNotNull(nullMixed)
            assertEquals(MixedType.NULL, nullMixed.type)
        }

        // Check the unmanaged model is there
        dictionaryFromRealm["UNMANAGED_MIXED_MODEL"].also { entry ->
            assertNotNull(entry)
            assertEquals(MixedType.OBJECT, entry.type)
            assertEquals("unmanaged", entry.asRealmModel(MyDog::class.java).id)
        }

        // And now that the managed model is there too
        dictionaryFromRealm["MANAGED_MIXED_MODEL"].also { entry ->
            assertNotNull(entry)
            assertEquals(MixedType.OBJECT, entry.type)
            assertEquals("managed", entry.asRealmModel(MyDog::class.java).id)
        }
    }

    @Test
    fun copyToRealm_boolean() {
        realm.executeTransaction { transactionRealm ->
            val dictionaryObject = DictionaryClass().apply {
                myBooleanDictionary = RealmDictionary<Boolean>().apply {
                    put(KEY_HELLO, VALUE_HELLO)
                    put(KEY_BYE, VALUE_BYE)
                    put(KEY_NULL, null)
                }
            }

            val dictionaryObjectFromRealm = transactionRealm.copyToRealm(dictionaryObject)
            val dictionaryFromRealm = dictionaryObjectFromRealm.myBooleanDictionary
            assertNotNull(dictionaryFromRealm)

            assertEquals(VALUE_HELLO, dictionaryFromRealm[KEY_HELLO])
            assertEquals(VALUE_BYE, dictionaryFromRealm[KEY_BYE])
            assertNull(dictionaryFromRealm[KEY_NULL])
        }
    }

//    @Test
//    fun copyToRealm_string() {
//        realm.executeTransaction { transactionRealm ->
//            val dictionaryObject = DictionaryClass().apply {
//                myStringDictionary = RealmDictionary<String>().apply {
//                    put(KEY_HELLO, VALUE_HELLO_STRING)
//                    put(KEY_BYE, VALUE_BYE_STRING)
//                    put(KEY_NULL, null)
//                }
//            }
//
//            val dictionaryObjectFromRealm = transactionRealm.copyToRealm(dictionaryObject)
//            val dictionaryFromRealm = dictionaryObjectFromRealm.myStringDictionary
//            assertNotNull(dictionaryFromRealm)
//
//            assertEquals(VALUE_HELLO_STRING, dictionaryFromRealm[KEY_HELLO])
//            assertEquals(VALUE_BYE_STRING, dictionaryFromRealm[KEY_BYE])
//            assertNull(dictionaryFromRealm[KEY_NULL])
//        }
//    }

//    @Test
//    fun copyToRealm_integer() {
//        realm.executeTransaction { transactionRealm ->
//            val dictionaryObject = DictionaryClass().apply {
//                myIntegerDictionary = RealmDictionary<Int>().apply {
//                    put(KEY_HELLO, VALUE_HELLO_INTEGER)
//                    put(KEY_BYE, VALUE_BYE_INTEGER)
//                    put(KEY_NULL, null)
//                }
//            }
//
//            val dictionaryObjectFromRealm = transactionRealm.copyToRealm(dictionaryObject)
//            val dictionaryFromRealm = dictionaryObjectFromRealm.myIntegerDictionary
//            assertNotNull(dictionaryFromRealm)
//
//            assertEquals(VALUE_HELLO_INTEGER, dictionaryFromRealm[KEY_HELLO])
//            assertEquals(VALUE_BYE_INTEGER, dictionaryFromRealm[KEY_BYE])
//            assertNull(dictionaryFromRealm[KEY_NULL])
//        }
//    }

    private fun createBooleanRealmDictionary(
            withNullValues: Boolean = false
    ): RealmDictionary<Boolean> {
        return RealmDictionary<Boolean>().apply {
            put(KEY_HELLO, VALUE_HELLO)
            put(KEY_BYE, VALUE_BYE)

            if (withNullValues) {
                put(KEY_NULL, VALUE_NULL)
            }
        }
    }

//    private fun createMixedRealmDictionary(): RealmDictionary<Mixed> {
//        return RealmDictionary<Mixed>().apply {
//            put(KEY_HELLO, Mixed.valueOf(VALUE_HELLO))
//            put(KEY_BYE, Mixed.valueOf(VALUE_BYE))
//        }
//    }

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
}

@RealmModule(classes = [DictionaryClass::class, MyDog::class, EmbeddedSimpleChild::class, EmbeddedSimpleParent::class])
class MapModule

open class MyDog : RealmObject() {
    var id: String? = null
}
