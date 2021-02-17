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

import androidx.test.platform.app.InstrumentationRegistry
import io.realm.entities.MyRealmModel
import org.bson.types.Decimal128
import org.bson.types.ObjectId
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.util.*

/**
 * Dictionary tests. It uses [Parameterized] tests for all possible combinations of
 * [RealmDictionary] types (i.e. all primitive Realm types (see [DictionarySupportedType]) plus
 * [RealmModel] and [Mixed] (and in turn all possible types supported by Mixed) in both `managed`
 * and `unmanaged` modes.
 */
@RunWith(Parameterized::class)
class ParameterizedDictionaryTests(
        private val tester: DictionaryTester
) {

    /**
     * Initializer for parameterized tests.
     */
    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun testTypes(): List<DictionaryTester> {
            return DictionaryMode.values().map { type ->
                when (type) {
                    DictionaryMode.UNMANAGED -> unmanagedFactory()
                    DictionaryMode.MANAGED -> managedFactory()
                }
            }.flatten()
        }

        private fun unmanagedFactory(): List<DictionaryTester> {
            val keys = listOf(KEY_HELLO, KEY_BYE)

            // Create primitive testers first
            val primitiveTesters = DictionarySupportedType.values().mapNotNull { supportedType ->
                when (supportedType) {
                    DictionarySupportedType.LONG ->
                        UnmanagedLong(keys, listOf(VALUE_NUMERIC_HELLO.toLong(), VALUE_NUMERIC_BYE.toLong()))
                    DictionarySupportedType.INTEGER ->
                        UnmanagedInteger(keys, listOf(VALUE_NUMERIC_HELLO, VALUE_NUMERIC_BYE))
                    DictionarySupportedType.SHORT ->
                        UnmanagedShort(keys, listOf(VALUE_NUMERIC_HELLO.toShort(), VALUE_NUMERIC_BYE.toShort()))
                    DictionarySupportedType.BYTE ->
                        UnmanagedByte(keys, listOf(VALUE_NUMERIC_HELLO.toByte(), VALUE_NUMERIC_BYE.toByte()))
                    DictionarySupportedType.FLOAT ->
                        UnmanagedFloat(keys, listOf(VALUE_NUMERIC_HELLO.toFloat(), VALUE_NUMERIC_BYE.toFloat()))
                    DictionarySupportedType.DOUBLE ->
                        UnmanagedDouble(keys, listOf(VALUE_NUMERIC_HELLO.toDouble(), VALUE_NUMERIC_BYE.toDouble()))
                    DictionarySupportedType.STRING ->
                        UnmanagedString(keys, listOf(VALUE_STRING_HELLO, VALUE_STRING_BYE))
                    DictionarySupportedType.BOOLEAN ->
                        UnmanagedBoolean(keys, listOf(VALUE_BOOLEAN_HELLO, VALUE_BOOLEAN_BYE))
                    DictionarySupportedType.DATE ->
                        UnmanagedDate(keys, listOf(VALUE_DATE_HELLO, VALUE_DATE_BYE))
                    DictionarySupportedType.DECIMAL128 ->
                        UnmanagedDecimal128(keys, listOf(VALUE_DECIMAL128_HELLO, VALUE_DECIMAL128_BYE))
                    DictionarySupportedType.BOXED_BYTE_ARRAY ->
                        UnmanagedBoxedByteArray(keys, listOf(VALUE_BOXED_BYTE_ARRAY_HELLO, VALUE_BOXED_BYTE_ARRAY_BYE))
                    DictionarySupportedType.BYTE_ARRAY ->
                        UnmanagedByteArray(keys, listOf(VALUE_BYTE_ARRAY_HELLO, VALUE_BYTE_ARRAY_BYE))
                    DictionarySupportedType.OBJECT_ID ->
                        UnmanagedObjectId(keys, listOf(VALUE_OBJECT_ID_HELLO, VALUE_OBJECT_ID_BYE))
                    DictionarySupportedType.UUID ->
                        UnmanagedUUID(keys, listOf(VALUE_UUID_HELLO, VALUE_UUID_BYE))
                    DictionarySupportedType.LINK ->
                        UnmanagedLink(keys, listOf(VALUE_LINK_HELLO, VALUE_LINK_BYE))
                    // Ignore Mixed in this switch
                    else -> null
                }
            }

            // Create Mixed testers now
            val mixedTesters = MixedType.values().map {
                UnmanagedMixed(it.name, keys, getMixedTestValues(it))
            }

            // Put them together
            return primitiveTesters.plus(mixedTesters)
        }

        private fun getMixedTestValues(mixedType: MixedType): List<Mixed> {
            return when (mixedType) {
                MixedType.INTEGER -> listOf(VALUE_MIXED_INTEGER_HELLO, VALUE_MIXED_INTEGER_BYE)
                MixedType.BOOLEAN -> listOf(VALUE_MIXED_BOOLEAN_HELLO, VALUE_MIXED_BOOLEAN_BYE)
                MixedType.STRING -> listOf(VALUE_MIXED_STRING_HELLO, VALUE_MIXED_STRING_BYE)
                MixedType.BINARY -> listOf(VALUE_MIXED_BYTE_ARRAY_HELLO, VALUE_MIXED_BYTE_ARRAY_BYE)
                MixedType.DATE -> listOf(VALUE_MIXED_DATE_HELLO, VALUE_MIXED_DATE_BYE)
                MixedType.FLOAT -> listOf(VALUE_MIXED_FLOAT_HELLO, VALUE_MIXED_FLOAT_BYE)
                MixedType.DOUBLE -> listOf(VALUE_MIXED_DOUBLE_HELLO, VALUE_MIXED_DOUBLE_BYE)
                MixedType.DECIMAL128 -> listOf(VALUE_MIXED_DECIMAL128_HELLO, VALUE_MIXED_DECIMAL128_BYE)
                MixedType.OBJECT_ID -> listOf(VALUE_MIXED_OBJECT_ID_HELLO, VALUE_MIXED_OBJECT_ID_BYE)
                MixedType.OBJECT -> listOf(VALUE_MIXED_LINK_HELLO, VALUE_MIXED_LINK_BYE)
                MixedType.UUID -> listOf(VALUE_MIXED_UUID_HELLO, VALUE_MIXED_UUID_BYE)
                MixedType.NULL -> listOf(Mixed.nullValue(), Mixed.valueOf("Not null"))
            }
        }

        private fun managedFactory(): List<DictionaryTester> {
            // TODO: add when ready
            return listOf()
        }
    }

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
    fun constructorWithAnotherMap() {
        if (!tester.isTesterManaged()) {
            tester.constructorWithAnotherMap()
        }
    }

    @Test
    fun isManaged() {
        tester.isManaged()
    }

    @Test
    fun isValid() {
        tester.isValid()
    }

    @Test
    fun isFrozen() {
        tester.isFrozen()
    }

    @Test
    fun size() {
        tester.size()
    }

    @Test
    fun isEmpty() {
        tester.isEmpty()
    }

    @Test
    fun containsKey() {
        tester.containsKey()
    }

    @Test
    fun containsValue() {
        tester.containsValue()
    }

    @Test
    fun get() {
        tester.get()
    }

    @Test
    fun put() {
        tester.put()
    }

    @Test
    fun remove() {
        tester.remove()
    }

    @Test
    fun putAll() {
        tester.putAll()
    }

    @Test
    fun clear() {
        tester.clear()
    }

    @Test
    fun keySet() {
        tester.keySet()
    }

    @Test
    fun values() {
        tester.values()
    }

    @Test
    fun entrySet() {
        tester.entrySet()
    }

    @Test
    fun freeze() {
        tester.freeze()
    }

//    @Test
//    fun () {
//        tester.
//    }
//    @Test
//    fun () {
//        tester.
//    }
}

/**
 * Modes for dictionaries.
 */
enum class DictionaryMode {
    UNMANAGED, MANAGED
}

/**
 * Supported types by dictionaries. Notice that Mixed dictionaries can in turn support all these
 * types internally (except Mixed itself).
 *
 * Add new types ad-hoc here.
 */
enum class DictionarySupportedType {
    LONG, INTEGER, SHORT, BYTE, FLOAT, DOUBLE, STRING, BOOLEAN, DATE, DECIMAL128, BOXED_BYTE_ARRAY,
    BYTE_ARRAY, OBJECT_ID, UUID, LINK, MIXED
}

//-------------------------------------------
// Test values
//-------------------------------------------

private const val KEY_HELLO = "Hello"
private const val KEY_BYE = "Bye"

private const val VALUE_BOOLEAN_HELLO = true
private const val VALUE_BOOLEAN_BYE = false

private const val VALUE_STRING_HELLO = "HELLO"
private const val VALUE_STRING_BYE = "BYE"

private const val VALUE_NUMERIC_HELLO = 42
private const val VALUE_NUMERIC_BYE = 666

private val VALUE_DATE_HELLO = Date(GregorianCalendar(1969, 7, 20).timeInMillis)
private val VALUE_DATE_BYE = Date()

private val VALUE_DECIMAL128_HELLO = Decimal128(VALUE_NUMERIC_HELLO.toLong())
private val VALUE_DECIMAL128_BYE = Decimal128(VALUE_NUMERIC_BYE.toLong())

private val VALUE_BOXED_BYTE_ARRAY_HELLO = Array<Byte>(2) { index ->
    if (index == 0) Byte.MIN_VALUE
    else if (index == 1) Byte.MAX_VALUE
    else throw IllegalArgumentException("Incorrect array size")
}
private val VALUE_BOXED_BYTE_ARRAY_BYE = Array<Byte>(2) { index ->
    if (index == 0) Byte.MAX_VALUE
    else if (index == 1) Byte.MIN_VALUE
    else throw IllegalArgumentException("Incorrect array size")
}

private val VALUE_BYTE_ARRAY_HELLO = ByteArray(2).apply {
    set(0, Byte.MIN_VALUE)
    set(1, Byte.MAX_VALUE)
}
private val VALUE_BYTE_ARRAY_BYE = ByteArray(2).apply {
    set(0, Byte.MAX_VALUE)
    set(1, Byte.MIN_VALUE)
}

private val VALUE_OBJECT_ID_HELLO = ObjectId(VALUE_DATE_HELLO)
private val VALUE_OBJECT_ID_BYE = ObjectId(VALUE_DATE_BYE)

private val VALUE_UUID_HELLO = UUID.nameUUIDFromBytes(VALUE_BYTE_ARRAY_HELLO)
private val VALUE_UUID_BYE = UUID.nameUUIDFromBytes(VALUE_BYTE_ARRAY_BYE)

private val VALUE_LINK_HELLO = MyRealmModel().apply { id = VALUE_STRING_HELLO }
private val VALUE_LINK_BYE = MyRealmModel().apply { id = VALUE_STRING_BYE }

private val VALUE_MIXED_INTEGER_HELLO = Mixed.valueOf(VALUE_NUMERIC_HELLO)
private val VALUE_MIXED_INTEGER_BYE = Mixed.valueOf(VALUE_NUMERIC_BYE)
private val VALUE_MIXED_FLOAT_HELLO = Mixed.valueOf(VALUE_NUMERIC_HELLO.toFloat())
private val VALUE_MIXED_FLOAT_BYE = Mixed.valueOf(VALUE_NUMERIC_BYE.toFloat())
private val VALUE_MIXED_DOUBLE_HELLO = Mixed.valueOf(VALUE_NUMERIC_HELLO.toDouble())
private val VALUE_MIXED_DOUBLE_BYE = Mixed.valueOf(VALUE_NUMERIC_BYE.toDouble())
private val VALUE_MIXED_STRING_HELLO = Mixed.valueOf(VALUE_STRING_HELLO)
private val VALUE_MIXED_STRING_BYE = Mixed.valueOf(VALUE_STRING_BYE)
private val VALUE_MIXED_BOOLEAN_HELLO = Mixed.valueOf(VALUE_BOOLEAN_HELLO)
private val VALUE_MIXED_BOOLEAN_BYE = Mixed.valueOf(VALUE_BOOLEAN_BYE)
private val VALUE_MIXED_DATE_HELLO = Mixed.valueOf(VALUE_DATE_HELLO)
private val VALUE_MIXED_DATE_BYE = Mixed.valueOf(VALUE_DATE_BYE)
private val VALUE_MIXED_DECIMAL128_HELLO = Mixed.valueOf(VALUE_DECIMAL128_HELLO)
private val VALUE_MIXED_DECIMAL128_BYE = Mixed.valueOf(VALUE_DECIMAL128_BYE)
private val VALUE_MIXED_BYTE_ARRAY_HELLO = Mixed.valueOf(VALUE_BYTE_ARRAY_HELLO)
private val VALUE_MIXED_BYTE_ARRAY_BYE = Mixed.valueOf(VALUE_BYTE_ARRAY_BYE)
private val VALUE_MIXED_OBJECT_ID_HELLO = Mixed.valueOf(VALUE_OBJECT_ID_HELLO)
private val VALUE_MIXED_OBJECT_ID_BYE = Mixed.valueOf(VALUE_OBJECT_ID_BYE)
private val VALUE_MIXED_UUID_HELLO = Mixed.valueOf(VALUE_UUID_HELLO)
private val VALUE_MIXED_UUID_BYE = Mixed.valueOf(VALUE_UUID_BYE)
private val VALUE_MIXED_LINK_HELLO = Mixed.valueOf(VALUE_LINK_HELLO)
private val VALUE_MIXED_LINK_BYE = Mixed.valueOf(VALUE_LINK_BYE)
