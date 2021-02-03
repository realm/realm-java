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
import io.realm.entities.DictJava
import io.realm.entities.DictionaryClass
import io.realm.entities.MyRealmModel
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
private const val VALUE_HELLO_NUMERIC = 42
private const val VALUE_BYE_NUMERIC = 666
private val VALUE_HELLO_DATE = Date()
private val VALUE_HELLO_OBJECT_ID = ObjectId()
private val VALUE_HELLO_UUID = UUID.randomUUID()
private val VALUE_HELLO_DECIMAL128 = Decimal128(VALUE_HELLO_NUMERIC.toLong())
private val VALUE_HELLO_BYTE_ARRAY = ByteArray(2).apply {
    set(0, Byte.MIN_VALUE)
    set(1, Byte.MAX_VALUE)
}
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
    fun managed_containsKey() {
        val key = "SOME_KEY"
        initDictionary()

        realm.executeTransaction {
            val dictionaryObject = realm.where<DictionaryClass>().findFirst()
            assertNotNull(dictionaryObject)
            dictionaryObject.myBooleanDictionary.let { dictionary ->
                assertNotNull(dictionary)
                dictionary[key] = true
                assertTrue(dictionary.containsKey(key))
                assertFalse(dictionary.containsKey("ANOTHER_KEY"))
            }
        }
    }

    @Test
    @Ignore
    fun managed_containsValue() {
        // TODO
    }

    @Test
    fun managed_put() {
        realm.executeTransaction { transactionRealm ->
            transactionRealm.copyToRealm(initDictionaryClass())

            val instance = realm.where<DictionaryClass>()
                    .findFirst()
            assertNotNull(instance)

            with(instance) {
                // All types but byte[] can be asserted like this
                myBooleanDictionary!!.putAndAssert(KEY_HELLO, VALUE_HELLO)
                myStringDictionary!!.putAndAssert(KEY_HELLO, VALUE_HELLO_STRING)
                myIntegerDictionary!!.putAndAssert(KEY_HELLO, VALUE_HELLO_NUMERIC)
                myFloatDictionary!!.putAndAssert(KEY_HELLO, VALUE_HELLO_NUMERIC.toFloat())
                myLongDictionary!!.putAndAssert(KEY_HELLO, VALUE_HELLO_NUMERIC.toLong())
                myShortDictionary!!.putAndAssert(KEY_HELLO, VALUE_HELLO_NUMERIC.toShort())
                myDoubleDictionary!!.putAndAssert(KEY_HELLO, VALUE_HELLO_NUMERIC.toDouble())
                myByteDictionary!!.putAndAssert(KEY_HELLO, VALUE_HELLO_NUMERIC.toByte())
                myDateDictionary!!.putAndAssert(KEY_HELLO, Date())
                myObjectIdDictionary!!.putAndAssert(KEY_HELLO, ObjectId())
                myUUIDDictionary!!.putAndAssert(KEY_HELLO, UUID.randomUUID())
                myDecimal128Dictionary!!.putAndAssert(KEY_HELLO, Decimal128(42))

                val previousValue = myByteArrayDictionary!!.put(KEY_HELLO, VALUE_HELLO_BYTE_ARRAY)
                assertNull(previousValue)
                val actual = myByteArrayDictionary!![KEY_HELLO]
                assertEquals(Byte.MIN_VALUE, actual!![0])
                assertEquals(Byte.MAX_VALUE, actual[1])
            }
        }
    }

    @Test
    fun managed_get() {
        realm.executeTransaction { transactionRealm ->
            transactionRealm.copyToRealm(initDictionaryClass(true))
        }

        val instance = realm.where<DictionaryClass>()
                .findFirst()
        assertNotNull(instance)

        with(instance) {
            myBooleanDictionary.also {
                assertNotNull(it)
                assertEquals(VALUE_HELLO, it[KEY_HELLO])
            }
            myStringDictionary.also {
                assertNotNull(it)
                assertEquals(VALUE_HELLO_STRING, it[KEY_HELLO])
            }
            myIntegerDictionary.also {
                assertNotNull(it)
                assertEquals(VALUE_HELLO_NUMERIC, it[KEY_HELLO])
            }
            myFloatDictionary.also {
                assertNotNull(it)
                assertEquals(VALUE_HELLO_NUMERIC.toFloat(), it[KEY_HELLO])
            }
            myLongDictionary.also {
                assertNotNull(it)
                assertEquals(VALUE_HELLO_NUMERIC.toLong(), it[KEY_HELLO])
            }
            myShortDictionary.also {
                assertNotNull(it)
                assertEquals(VALUE_HELLO_NUMERIC.toShort(), it[KEY_HELLO])
            }
            myDoubleDictionary.also {
                assertNotNull(it)
                assertEquals(VALUE_HELLO_NUMERIC.toDouble(), it[KEY_HELLO])
            }
            myByteDictionary.also {
                assertNotNull(it)
                assertEquals(VALUE_HELLO_NUMERIC.toByte(), it[KEY_HELLO])
            }
            myDateDictionary.also {
                assertNotNull(it)
                assertEquals(VALUE_HELLO_DATE, it[KEY_HELLO])
            }
            myObjectIdDictionary.also {
                assertNotNull(it)
                assertEquals(VALUE_HELLO_OBJECT_ID, it[KEY_HELLO])
            }
            myUUIDDictionary.also {
                assertNotNull(it)
                assertEquals(VALUE_HELLO_UUID, it[KEY_HELLO])
            }
            myDecimal128Dictionary.also {
                assertNotNull(it)
                assertEquals(VALUE_HELLO_DECIMAL128, it[KEY_HELLO])
            }
            myByteArrayDictionary.also {
                assertNotNull(it)
                val actual = it[KEY_HELLO]
                assertNotNull(actual)
                assertEquals(VALUE_HELLO_BYTE_ARRAY[0], actual[0])
                assertEquals(VALUE_HELLO_BYTE_ARRAY[1], actual[1])
            }
        }
    }

    @Test
    fun managed_putAll() {
        val map = HashMap<String, Boolean>().apply {
            put(KEY_HELLO, VALUE_HELLO)
            put(KEY_BYE, VALUE_BYE)
        }
        realm.executeTransaction { transactionRealm ->
            val instance = transactionRealm.copyToRealm(initDictionaryClass(false))
            instance.myBooleanDictionary.also { dictionary ->
                assertNotNull(dictionary)
                assertTrue(dictionary.isEmpty())

                // putAll with unmanaged map
                dictionary.putAll(map)
                assertFalse(dictionary.isEmpty())
                assertEquals(2, dictionary.size)
                assertEquals(VALUE_HELLO, dictionary[KEY_HELLO])
                assertEquals(VALUE_BYE, dictionary[KEY_BYE])
            }

            // TODO: putAll with managed dictionary
            val emptyDictionary = instance.emptyBooleanDictionary
            assertNotNull(emptyDictionary)
            assertTrue(emptyDictionary.isEmpty())
            val booleanDictionaryAsMap: Map<String, Boolean> = instance.myBooleanDictionary!!
            emptyDictionary.putAll(booleanDictionaryAsMap)
        }
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
    @Ignore("TODO - bug in core: https://github.com/realm/realm-core/issues/4374")
    fun managed_keySet() {
        // TODO - similar to managed_values
    }

    @Test
    @Ignore("TODO - bug in core: https://github.com/realm/realm-core/issues/4374")
    fun managed_values() {
        realm.executeTransaction { transactionRealm ->
            val dictionaryObject = DictionaryClass().apply {
                myStringDictionary = RealmDictionary<String>().apply {
                    put(KEY_HELLO, VALUE_HELLO_STRING)
                    put(KEY_BYE, VALUE_BYE_STRING)
                }
            }

            val dictionaryObjectFromRealm = transactionRealm.copyToRealm(dictionaryObject)
            val dictionaryFromRealm = dictionaryObjectFromRealm.myStringDictionary
            assertNotNull(dictionaryFromRealm)

            val realmResults = realm.where<DictionaryClass>()
                    .findAll()
            val result0 = realmResults[0]


            val values = dictionaryFromRealm.values
            assertNotNull(values)
            assertEquals(2, values.size)
//            assertTrue(values.contains(VALUE_HELLO_STRING))
//            assertTrue(values.contains(VALUE_BYE_STRING))
            assertTrue(values is RealmResults)
            (values as RealmResults).let { results ->
                val res0 = results[0]
                val res1 = results[1]
                val kjashd = 0
            }

        }
    }

    @Test
    @Ignore
    fun managed_entrySet() {
        // TODO
    }

    @Test
    fun managed_freeze() {
        realm.executeTransaction { transactionRealm ->
            val dictionaryObject = DictionaryClass().apply {
                myBooleanDictionary = RealmDictionary<Boolean>().apply {
                    put(KEY_HELLO, VALUE_HELLO)
                    put(KEY_BYE, VALUE_BYE)
                    put(KEY_NULL, null)
                }
            }

            transactionRealm.copyToRealm(dictionaryObject)
        }

        val dictionaryObjectFromRealm = realm.where<DictionaryClass>()
                .findFirst()
        assertNotNull(dictionaryObjectFromRealm)

        val dictionaryFromRealm = dictionaryObjectFromRealm.myBooleanDictionary
        assertNotNull(dictionaryFromRealm)
        assertFalse(dictionaryFromRealm.isFrozen)
        val frozenDictionary = dictionaryFromRealm.freeze()
        assertTrue(frozenDictionary.isFrozen)
        assertEquals(VALUE_HELLO, frozenDictionary[KEY_HELLO])
        assertEquals(VALUE_BYE, frozenDictionary[KEY_BYE])
        assertNull(frozenDictionary[KEY_NULL])
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

        assertTrue(objectSchema.hasField(DictionaryClass.STRING_DICTIONARY_FIELD_NAME))
        assertEquals(objectSchema.getFieldType(DictionaryClass.STRING_DICTIONARY_FIELD_NAME), RealmFieldType.STRING_TO_STRING_MAP)

        assertTrue(objectSchema.hasField(DictionaryClass.INTEGER_DICTIONARY_FIELD_NAME))
        assertEquals(objectSchema.getFieldType(DictionaryClass.INTEGER_DICTIONARY_FIELD_NAME), RealmFieldType.STRING_TO_INTEGER_MAP)

        assertTrue(objectSchema.hasField(DictionaryClass.FLOAT_DICTIONARY_FIELD_NAME))
        assertEquals(objectSchema.getFieldType(DictionaryClass.FLOAT_DICTIONARY_FIELD_NAME), RealmFieldType.STRING_TO_FLOAT_MAP)

        assertTrue(objectSchema.hasField(DictionaryClass.LONG_DICTIONARY_FIELD_NAME))
        assertEquals(objectSchema.getFieldType(DictionaryClass.LONG_DICTIONARY_FIELD_NAME), RealmFieldType.STRING_TO_INTEGER_MAP)

        assertTrue(objectSchema.hasField(DictionaryClass.SHORT_DICTIONARY_FIELD_NAME))
        assertEquals(objectSchema.getFieldType(DictionaryClass.SHORT_DICTIONARY_FIELD_NAME), RealmFieldType.STRING_TO_INTEGER_MAP)

        assertTrue(objectSchema.hasField(DictionaryClass.DOUBLE_DICTIONARY_FIELD_NAME))
        assertEquals(objectSchema.getFieldType(DictionaryClass.DOUBLE_DICTIONARY_FIELD_NAME), RealmFieldType.STRING_TO_DOUBLE_MAP)

        assertTrue(objectSchema.hasField(DictionaryClass.BYTE_DICTIONARY_FIELD_NAME))
        assertEquals(objectSchema.getFieldType(DictionaryClass.BYTE_DICTIONARY_FIELD_NAME), RealmFieldType.STRING_TO_INTEGER_MAP)

        assertTrue(objectSchema.hasField(DictionaryClass.BYTE_ARRAY_DICTIONARY_FIELD_NAME))
        assertEquals(objectSchema.getFieldType(DictionaryClass.BYTE_ARRAY_DICTIONARY_FIELD_NAME), RealmFieldType.STRING_TO_BINARY_MAP)

        assertTrue(objectSchema.hasField(DictionaryClass.DATE_DICTIONARY_FIELD_NAME))
        assertEquals(objectSchema.getFieldType(DictionaryClass.DATE_DICTIONARY_FIELD_NAME), RealmFieldType.STRING_TO_DATE_MAP)

        assertTrue(objectSchema.hasField(DictionaryClass.DECIMAL128_DICTIONARY_FIELD_NAME))
        assertEquals(objectSchema.getFieldType(DictionaryClass.DECIMAL128_DICTIONARY_FIELD_NAME), RealmFieldType.STRING_TO_DECIMAL128_MAP)

        assertTrue(objectSchema.hasField(DictionaryClass.OBJECT_ID_DICTIONARY_FIELD_NAME))
        assertEquals(objectSchema.getFieldType(DictionaryClass.OBJECT_ID_DICTIONARY_FIELD_NAME), RealmFieldType.STRING_TO_OBJECT_ID_MAP)

        assertTrue(objectSchema.hasField(DictionaryClass.UUID_DICTIONARY_FIELD_NAME))
        assertEquals(objectSchema.getFieldType(DictionaryClass.UUID_DICTIONARY_FIELD_NAME), RealmFieldType.STRING_TO_UUID_MAP)
    }

    @Test
    fun copyToRealm_realmModel() {
        val helloId = "Hello ID"
        val hello = MyRealmModel().apply { id = helloId }

        realm.executeTransaction { transactionRealm ->
            val dictionaryObject = DictionaryClass().apply {
                myRealmModelDictionary = RealmDictionary<MyRealmModel>().apply {
                    put(KEY_HELLO, hello)
//                    put(KEY_NULL, null)
                }
            }

            val dictionaryObjectFromRealm = transactionRealm.copyToRealm(dictionaryObject)
            val dictionaryFromRealm = dictionaryObjectFromRealm.myRealmModelDictionary
            assertNotNull(dictionaryFromRealm)

            dictionaryFromRealm[KEY_HELLO].let { helloFromDictionary ->
                assertNotNull(helloFromDictionary)
                assertEquals(helloId, helloFromDictionary.id)
            }
//            assertNull(dictionaryFromRealm[KEY_NULL])
        }
    }

    @Test
    fun copyToRealm_UUID() {
        val hello = UUID.randomUUID()
        val bye = UUID.randomUUID()

        realm.executeTransaction { transactionRealm ->
            val dictionaryObject = DictionaryClass().apply {
                myUUIDDictionary = RealmDictionary<UUID>().apply {
                    put(KEY_HELLO, hello)
                    put(KEY_BYE, bye)
                    put(KEY_NULL, null)
                }
            }

            val dictionaryObjectFromRealm = transactionRealm.copyToRealm(dictionaryObject)
            val dictionaryFromRealm = dictionaryObjectFromRealm.myUUIDDictionary
            assertNotNull(dictionaryFromRealm)

            assertEquals(hello.toString(), dictionaryFromRealm[KEY_HELLO].toString())
            assertEquals(bye.toString(), dictionaryFromRealm[KEY_BYE].toString())
            assertNull(dictionaryFromRealm[KEY_NULL])
        }
    }

    @Test
    fun copyToRealm_objectId() {
        val hello = ObjectId()
        val bye = ObjectId()

        realm.executeTransaction { transactionRealm ->
            val dictionaryObject = DictionaryClass().apply {
                myObjectIdDictionary = RealmDictionary<ObjectId>().apply {
                    put(KEY_HELLO, hello)
                    put(KEY_BYE, bye)
                    put(KEY_NULL, null)
                }
            }

            val dictionaryObjectFromRealm = transactionRealm.copyToRealm(dictionaryObject)
            val dictionaryFromRealm = dictionaryObjectFromRealm.myObjectIdDictionary
            assertNotNull(dictionaryFromRealm)

            assertEquals(hello, dictionaryFromRealm[KEY_HELLO])
            assertEquals(bye, dictionaryFromRealm[KEY_BYE])
            assertNull(dictionaryFromRealm[KEY_NULL])
        }
    }

    @Test
    @Ignore("Wait until Clemente is done with Mixed")
    fun copyToRealm_mixedBoolean() {
        realm.executeTransaction { transactionRealm ->
            val dictionaryObject = DictionaryClass().apply {
                myMixedDictionary = createMixedRealmDictionary()
            }

            val dictionaryObjectFromRealm = transactionRealm.copyToRealm(dictionaryObject)
            val dictionaryFromRealm = dictionaryObjectFromRealm.myMixedDictionary
            assertNotNull(dictionaryFromRealm)

            val mixedHello = dictionaryFromRealm[KEY_HELLO]
            val mixedBye = dictionaryFromRealm[KEY_BYE]
            val kajhs = 0
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

    @Test
    fun copyToRealm_string() {
        realm.executeTransaction { transactionRealm ->
            val dictionaryObject = DictionaryClass().apply {
                myStringDictionary = RealmDictionary<String>().apply {
                    put(KEY_HELLO, VALUE_HELLO_STRING)
                    put(KEY_BYE, VALUE_BYE_STRING)
                    put(KEY_NULL, null)
                }
            }

            val dictionaryObjectFromRealm = transactionRealm.copyToRealm(dictionaryObject)
            val dictionaryFromRealm = dictionaryObjectFromRealm.myStringDictionary
            assertNotNull(dictionaryFromRealm)

            assertEquals(VALUE_HELLO_STRING, dictionaryFromRealm[KEY_HELLO])
            assertEquals(VALUE_BYE_STRING, dictionaryFromRealm[KEY_BYE])
            assertNull(dictionaryFromRealm[KEY_NULL])
        }
    }

    @Test
    fun copyToRealm_integer() {
        realm.executeTransaction { transactionRealm ->
            val dictionaryObject = DictionaryClass().apply {
                myIntegerDictionary = RealmDictionary<Int>().apply {
                    put(KEY_HELLO, VALUE_HELLO_NUMERIC)
                    put(KEY_BYE, VALUE_BYE_NUMERIC)
                    put(KEY_NULL, null)
                }
            }

            val dictionaryObjectFromRealm = transactionRealm.copyToRealm(dictionaryObject)
            val dictionaryFromRealm = dictionaryObjectFromRealm.myIntegerDictionary
            assertNotNull(dictionaryFromRealm)

            assertEquals(VALUE_HELLO_NUMERIC, dictionaryFromRealm[KEY_HELLO])
            assertEquals(VALUE_BYE_NUMERIC, dictionaryFromRealm[KEY_BYE])
            assertNull(dictionaryFromRealm[KEY_NULL])
        }
    }

    @Test
    fun copyToRealm_float() {
        realm.executeTransaction { transactionRealm ->
            val dictionaryObject = DictionaryClass().apply {
                myFloatDictionary = RealmDictionary<Float>().apply {
                    put(KEY_HELLO, VALUE_HELLO_NUMERIC.toFloat())
                    put(KEY_BYE, VALUE_BYE_NUMERIC.toFloat())
                    put(KEY_NULL, null)
                }
            }

            val dictionaryObjectFromRealm = transactionRealm.copyToRealm(dictionaryObject)
            val dictionaryFromRealm = dictionaryObjectFromRealm.myFloatDictionary
            assertNotNull(dictionaryFromRealm)

            assertEquals(VALUE_HELLO_NUMERIC.toFloat(), dictionaryFromRealm[KEY_HELLO])
            assertEquals(VALUE_BYE_NUMERIC.toFloat(), dictionaryFromRealm[KEY_BYE])
            assertNull(dictionaryFromRealm[KEY_NULL])
        }
    }

    @Test
    fun copyToRealm_long() {
        realm.executeTransaction { transactionRealm ->
            val dictionaryObject = DictionaryClass().apply {
                myLongDictionary = RealmDictionary<Long>().apply {
                    put(KEY_HELLO, VALUE_HELLO_NUMERIC.toLong())
                    put(KEY_BYE, VALUE_BYE_NUMERIC.toLong())
                    put(KEY_NULL, null)
                }
            }

            val dictionaryObjectFromRealm = transactionRealm.copyToRealm(dictionaryObject)
            val dictionaryFromRealm = dictionaryObjectFromRealm.myLongDictionary
            assertNotNull(dictionaryFromRealm)

            assertEquals(VALUE_HELLO_NUMERIC.toLong(), dictionaryFromRealm[KEY_HELLO])
            assertEquals(VALUE_BYE_NUMERIC.toLong(), dictionaryFromRealm[KEY_BYE])
            assertNull(dictionaryFromRealm[KEY_NULL])
        }
    }

    @Test
    fun copyToRealm_short() {
        realm.executeTransaction { transactionRealm ->
            val dictionaryObject = DictionaryClass().apply {
                myShortDictionary = RealmDictionary<Short>().apply {
                    put(KEY_HELLO, VALUE_HELLO_NUMERIC.toShort())
                    put(KEY_BYE, VALUE_BYE_NUMERIC.toShort())
                    put(KEY_NULL, null)
                }
            }

            val dictionaryObjectFromRealm = transactionRealm.copyToRealm(dictionaryObject)
            val dictionaryFromRealm = dictionaryObjectFromRealm.myShortDictionary
            assertNotNull(dictionaryFromRealm)

            assertEquals(VALUE_HELLO_NUMERIC.toShort(), dictionaryFromRealm[KEY_HELLO])
            assertEquals(VALUE_BYE_NUMERIC.toShort(), dictionaryFromRealm[KEY_BYE])
            assertNull(dictionaryFromRealm[KEY_NULL])
        }
    }

    @Test
    fun copyToRealm_double() {
        realm.executeTransaction { transactionRealm ->
            val dictionaryObject = DictionaryClass().apply {
                myDoubleDictionary = RealmDictionary<Double>().apply {
                    put(KEY_HELLO, VALUE_HELLO_NUMERIC.toDouble())
                    put(KEY_BYE, VALUE_BYE_NUMERIC.toDouble())
                    put(KEY_NULL, null)
                }
            }

            val dictionaryObjectFromRealm = transactionRealm.copyToRealm(dictionaryObject)
            val dictionaryFromRealm = dictionaryObjectFromRealm.myDoubleDictionary
            assertNotNull(dictionaryFromRealm)

            assertEquals(VALUE_HELLO_NUMERIC.toDouble(), dictionaryFromRealm[KEY_HELLO])
            assertEquals(VALUE_BYE_NUMERIC.toDouble(), dictionaryFromRealm[KEY_BYE])
            assertNull(dictionaryFromRealm[KEY_NULL])
        }
    }

    @Test
    fun copyToRealm_byte() {
        realm.executeTransaction { transactionRealm ->
            val dictionaryObject = DictionaryClass().apply {
                myByteDictionary = RealmDictionary<Byte>().apply {
                    put(KEY_HELLO, VALUE_HELLO_NUMERIC.toByte())
                    put(KEY_BYE, VALUE_BYE_NUMERIC.toByte())
                    put(KEY_NULL, null)
                }
            }

            val dictionaryObjectFromRealm = transactionRealm.copyToRealm(dictionaryObject)
            val dictionaryFromRealm = dictionaryObjectFromRealm.myByteDictionary
            assertNotNull(dictionaryFromRealm)

            assertEquals(VALUE_HELLO_NUMERIC.toByte(), dictionaryFromRealm[KEY_HELLO])
            assertEquals(VALUE_BYE_NUMERIC.toByte(), dictionaryFromRealm[KEY_BYE])
            assertNull(dictionaryFromRealm[KEY_NULL])
        }
    }

    @Test
    fun copyToRealm_byteArray() {
        val hello = ByteArray(2).apply {
            // 0 is MIN, 1 is MAX
            set(0, Byte.MIN_VALUE)
            set(1, Byte.MAX_VALUE)
        }
        val bye = ByteArray(2).apply {
            // Opposite of hello
            set(0, Byte.MAX_VALUE)
            set(1, Byte.MIN_VALUE)
        }
        realm.executeTransaction { transactionRealm ->
            val dictionaryObject = DictionaryClass().apply {
                myByteArrayDictionary = RealmDictionary<ByteArray>().apply {
                    put(KEY_HELLO, hello)
                    put(KEY_BYE, bye)
                    put(KEY_NULL, null)
                }
            }

            val dictionaryObjectFromRealm = transactionRealm.copyToRealm(dictionaryObject)
            val dictionaryFromRealm = dictionaryObjectFromRealm.myByteArrayDictionary
            assertNotNull(dictionaryFromRealm)

            val helloFromDictionary = dictionaryFromRealm[KEY_HELLO]
            assertNotNull(helloFromDictionary)
            assertEquals(Byte.MIN_VALUE, helloFromDictionary[0])
            assertEquals(Byte.MAX_VALUE, helloFromDictionary[1])
            val byeFromDictionary = dictionaryFromRealm[KEY_BYE]
            assertNotNull(byeFromDictionary)
            assertEquals(Byte.MAX_VALUE, byeFromDictionary[0])
            assertEquals(Byte.MIN_VALUE, byeFromDictionary[1])
            assertNull(dictionaryFromRealm[KEY_NULL])
        }
    }

    @Test
    fun copyToRealm_date() {
        val hello = Date()
        val bye = Date()

        realm.executeTransaction { transactionRealm ->
            val dictionaryObject = DictionaryClass().apply {
                myDateDictionary = RealmDictionary<Date>().apply {
                    put(KEY_HELLO, hello)
                    put(KEY_BYE, bye)
                    put(KEY_NULL, null)
                }
            }

            val dictionaryObjectFromRealm = transactionRealm.copyToRealm(dictionaryObject)
            val dictionaryFromRealm = dictionaryObjectFromRealm.myDateDictionary
            assertNotNull(dictionaryFromRealm)

            assertEquals(hello, dictionaryFromRealm[KEY_HELLO])
            assertEquals(bye, dictionaryFromRealm[KEY_BYE])
            assertNull(dictionaryFromRealm[KEY_NULL])
        }
    }

    @Test
    fun copyToRealm_decimal128() {
        val hello = Decimal128(42)
        val bye = Decimal128(666)

        realm.executeTransaction { transactionRealm ->
            val dictionaryObject = DictionaryClass().apply {
                myDecimal128Dictionary = RealmDictionary<Decimal128>().apply {
                    put(KEY_HELLO, hello)
                    put(KEY_BYE, bye)
                    put(KEY_NULL, null)
                }
            }

            val dictionaryObjectFromRealm = transactionRealm.copyToRealm(dictionaryObject)
            val dictionaryFromRealm = dictionaryObjectFromRealm.myDecimal128Dictionary
            assertNotNull(dictionaryFromRealm)

            assertEquals(hello, dictionaryFromRealm[KEY_HELLO])
            assertEquals(bye, dictionaryFromRealm[KEY_BYE])
            assertNull(dictionaryFromRealm[KEY_NULL])
        }
    }

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

    private fun initDictionaryClass(withDefaultValues: Boolean = false): DictionaryClass {
        return DictionaryClass().apply {
            // This one will always be empty by default for testing purposes
            myBooleanDictionary = RealmDictionary<Boolean>()

            myBooleanDictionary = RealmDictionary<Boolean>().apply {
                if (withDefaultValues) {
                    put(KEY_HELLO, VALUE_HELLO)
                }
            }
            myStringDictionary = RealmDictionary<String>().apply {
                if (withDefaultValues) {
                    put(KEY_HELLO, VALUE_HELLO_STRING)
                }
            }
            myIntegerDictionary = RealmDictionary<Int>().apply {
                if (withDefaultValues) {
                    put(KEY_HELLO, VALUE_HELLO_NUMERIC)
                }
            }
            myFloatDictionary = RealmDictionary<Float>().apply {
                if (withDefaultValues) {
                    put(KEY_HELLO, VALUE_HELLO_NUMERIC.toFloat())
                }
            }
            myLongDictionary = RealmDictionary<Long>().apply {
                if (withDefaultValues) {
                    put(KEY_HELLO, VALUE_HELLO_NUMERIC.toLong())
                }
            }
            myShortDictionary = RealmDictionary<Short>().apply {
                if (withDefaultValues) {
                    put(KEY_HELLO, VALUE_HELLO_NUMERIC.toShort())
                }
            }
            myDoubleDictionary = RealmDictionary<Double>().apply {
                if (withDefaultValues) {
                    put(KEY_HELLO, VALUE_HELLO_NUMERIC.toDouble())
                }
            }
            myByteDictionary = RealmDictionary<Byte>().apply {
                if (withDefaultValues) {
                    put(KEY_HELLO, VALUE_HELLO_NUMERIC.toByte())
                }
            }
            myDateDictionary = RealmDictionary<Date>().apply {
                if (withDefaultValues) {
                    put(KEY_HELLO, VALUE_HELLO_DATE)
                }
            }
            myObjectIdDictionary = RealmDictionary<ObjectId>().apply {
                if (withDefaultValues) {
                    put(KEY_HELLO, VALUE_HELLO_OBJECT_ID)
                }
            }
            myUUIDDictionary = RealmDictionary<UUID>().apply {
                if (withDefaultValues) {
                    put(KEY_HELLO, VALUE_HELLO_UUID)
                }
            }
            myDecimal128Dictionary = RealmDictionary<Decimal128>().apply {
                if (withDefaultValues) {
                    put(KEY_HELLO, VALUE_HELLO_DECIMAL128)
                }
            }
            myByteArrayDictionary = RealmDictionary<ByteArray>().apply {
                if (withDefaultValues) {
                    put(KEY_HELLO, VALUE_HELLO_BYTE_ARRAY)
                }
            }
        }
    }

    private inline fun <reified T : Any> RealmDictionary<T>.putAndAssert(key: String, value: T) {
        val previousValue = put(key, value)
        assertNull(previousValue)
        val actual = get(key)
        assertEquals(value, actual)
    }
}

@RealmModule(classes = [DictionaryClass::class, MyRealmModel::class, DictJava::class])
class MapModule
