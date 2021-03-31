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
import io.realm.entities.DogPrimaryKey
import io.realm.rule.BlockingLooperThread
import org.bson.types.Decimal128
import org.bson.types.ObjectId
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.util.*

/**
 * Dictionary tests. It uses [Parameterized] tests for all possible combinations of
 * [RealmDictionary] types (i.e. all primitive Realm types (see [DictionarySupportedType]) plus
 * [RealmModel] and [Mixed] (and in turn all possible types supported by Mixed) in both `managed`
 * and `unmanaged` modes.
 *
 * In order to streamline the testing for managed dictionaries we use Kotlin's reflection API
 * `KFunction1` and `KFunction2`. These two methods provide access to the Java getters and setters
 * used to work with each dictionary field.
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
//                    DictionaryMode.UNMANAGED -> unmanagedFactory()
                    DictionaryMode.UNMANAGED -> listOf()
                    DictionaryMode.MANAGED -> managedFactory()
                }
            }.flatten()
        }
    }

    @Rule
    @JvmField
    val configFactory = TestRealmConfigurationFactory()

    private val looperThread = BlockingLooperThread()

    @Before
    fun setUp() {
        Realm.init(InstrumentationRegistry.getInstrumentation().context)
        tester.setUp(configFactory.createConfiguration(), looperThread)
    }

    @After
    fun tearDown() {
        tester.tearDown()
    }

    @Test
    fun constructorWithAnotherMap() {
        tester.constructorWithAnotherMap()
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
    fun putRequired() {
        tester.putRequired()
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

    @Test
    fun copyToRealm() {
        tester.copyToRealm()
    }

    @Test
    fun copyToRealmOrUpdate() {
        tester.copyToRealmOrUpdate()
    }

    @Test
    fun copyFromRealm() {
        tester.copyFromRealm()
    }

    @Test
    fun fieldAccessors() {
        tester.fieldAccessors()
    }

    @Test
    fun addRealmChangeListener() {
        tester.addRealmChangeListener()
    }

    @Test
    fun addMapChangeListener() {
        tester.addMapChangeListener()
    }

    @Test
    fun hasListeners() {
        tester.hasListeners()
    }
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
    LONG, INTEGER, SHORT, BYTE, FLOAT, DOUBLE, STRING, BOOLEAN, DATE, DECIMAL128, BINARY, OBJECT_ID,
    UUID, LINK, MIXED
}

//-------------------------------------------
// Test values
//-------------------------------------------

internal const val KEY_HELLO = "KeyHello"
internal const val KEY_BYE = "KeyBye"
internal const val KEY_NULL = "KeyNull"
internal const val KEY_NOT_PRESENT = "KeyNotPresent"

internal const val KEY_HELLO_NON_LATIN = "Keyこんにちは"
internal const val KEY_BYE_NON_LATIN = "Keyさようなら"
internal const val KEY_NULL_NON_LATIN = "Keyヌル"
internal const val KEY_NOT_PRESENT_NON_LATIN = "Key現在ではない"

internal const val NEW_KEY = "NEW KEY"
internal const val NEW_KEY_NON_LATIN = "NEW KEYさようなら"
internal const val VALUE_BOOLEAN_HELLO = true
internal const val VALUE_BOOLEAN_BYE = false
internal const val VALUE_BOOLEAN_NOT_PRESENT = VALUE_BOOLEAN_BYE

internal const val VALUE_STRING_HELLO = "HELLO"
internal const val VALUE_STRING_BYE = "BYE"
internal const val VALUE_STRING_NOT_PRESENT = "NOT PRESENT"

internal const val VALUE_STRING_NON_LATIN_HELLO = "こんにちは"
internal const val VALUE_STRING_NON_LATIN_BYE = "さようなら"
internal const val VALUE_STRING_NON_LATIN_NOT_PRESENT = "現在ではない"

internal const val VALUE_NUMERIC_HELLO = 42
internal const val VALUE_NUMERIC_BYE = -42
internal const val VALUE_NUMERIC_NOT_PRESENT = 13

internal val VALUE_DATE_HELLO = Calendar.getInstance().apply {
    set(Calendar.YEAR, 1969)
    set(Calendar.MONTH, Calendar.JULY)
    set(Calendar.DAY_OF_MONTH, 20)
}.time
internal val VALUE_DATE_BYE = Calendar.getInstance().time
internal val VALUE_DATE_NOT_PRESENT = Calendar.getInstance().apply {
    set(Calendar.YEAR, 2000)
    set(Calendar.MONTH, Calendar.JANUARY)
    set(Calendar.DAY_OF_MONTH, 1)
}.time

internal val VALUE_DECIMAL128_HELLO = Decimal128(VALUE_NUMERIC_HELLO.toLong())
internal val VALUE_DECIMAL128_BYE = Decimal128(VALUE_NUMERIC_BYE.toLong())
internal val VALUE_DECIMAL128_NOT_PRESENT = Decimal128(VALUE_NUMERIC_NOT_PRESENT.toLong())

internal val VALUE_BOXED_BINARY_HELLO = Array<Byte>(2) { index ->
    if (index == 0) Byte.MIN_VALUE
    else if (index == 1) Byte.MAX_VALUE
    else throw IllegalArgumentException("Incorrect array size")
}
internal val VALUE_BOXED_BINARY_BYE = Array<Byte>(2) { index ->
    if (index == 0) Byte.MAX_VALUE
    else if (index == 1) Byte.MIN_VALUE
    else throw IllegalArgumentException("Incorrect array size")
}
internal val VALUE_BOXED_BINARY_NOT_PRESENT = Array<Byte>(2) { index ->
    if (index == 0) VALUE_NUMERIC_NOT_PRESENT.toByte()
    else if (index == 1) (VALUE_NUMERIC_NOT_PRESENT * -1).toByte()
    else throw IllegalArgumentException("Incorrect array size")
}

internal val VALUE_BINARY_HELLO = ByteArray(2).apply {
    set(0, Byte.MIN_VALUE)
    set(1, Byte.MAX_VALUE)
}
internal val VALUE_BINARY_BYE = ByteArray(2).apply {
    set(0, Byte.MAX_VALUE)
    set(1, Byte.MIN_VALUE)
}
internal val VALUE_BINARY_NOT_PRESENT = ByteArray(2).apply {
    set(0, VALUE_NUMERIC_NOT_PRESENT.toByte())
    set(1, (VALUE_NUMERIC_NOT_PRESENT * -1).toByte())
}

internal val VALUE_OBJECT_ID_HELLO = ObjectId(VALUE_DATE_HELLO)
internal val VALUE_OBJECT_ID_BYE = ObjectId(VALUE_DATE_BYE)
internal val VALUE_OBJECT_ID_NOT_PRESENT = ObjectId(VALUE_DATE_NOT_PRESENT)

internal val VALUE_UUID_HELLO = UUID.nameUUIDFromBytes(VALUE_BINARY_HELLO)
internal val VALUE_UUID_BYE = UUID.nameUUIDFromBytes(VALUE_BINARY_BYE)
internal val VALUE_UUID_NOT_PRESENT = UUID.nameUUIDFromBytes(VALUE_BINARY_NOT_PRESENT)

internal val VALUE_LINK_HELLO = DogPrimaryKey(42, VALUE_STRING_HELLO)
internal val VALUE_LINK_BYE = DogPrimaryKey(43, VALUE_STRING_BYE)
internal val VALUE_LINK_NOT_PRESENT = DogPrimaryKey(44, VALUE_STRING_NOT_PRESENT)

internal val VALUE_MIXED_INTEGER_HELLO = Mixed.valueOf(VALUE_NUMERIC_HELLO)
internal val VALUE_MIXED_INTEGER_BYE = Mixed.valueOf(VALUE_NUMERIC_BYE)
internal val VALUE_MIXED_FLOAT_HELLO = Mixed.valueOf(VALUE_NUMERIC_HELLO.toFloat())
internal val VALUE_MIXED_FLOAT_BYE = Mixed.valueOf(VALUE_NUMERIC_BYE.toFloat())
internal val VALUE_MIXED_DOUBLE_HELLO = Mixed.valueOf(VALUE_NUMERIC_HELLO.toDouble())
internal val VALUE_MIXED_DOUBLE_BYE = Mixed.valueOf(VALUE_NUMERIC_BYE.toDouble())
internal val VALUE_MIXED_STRING_HELLO = Mixed.valueOf(VALUE_STRING_HELLO)
internal val VALUE_MIXED_STRING_BYE = Mixed.valueOf(VALUE_STRING_BYE)
internal val VALUE_MIXED_STRING_NON_LATIN_HELLO = Mixed.valueOf(VALUE_STRING_NON_LATIN_HELLO)
internal val VALUE_MIXED_STRING_NON_LATIN_BYE = Mixed.valueOf(VALUE_STRING_NON_LATIN_BYE)
internal val VALUE_MIXED_BOOLEAN_HELLO = Mixed.valueOf(VALUE_BOOLEAN_HELLO)
internal val VALUE_MIXED_BOOLEAN_BYE = Mixed.valueOf(VALUE_BOOLEAN_BYE)
internal val VALUE_MIXED_DATE_HELLO = Mixed.valueOf(VALUE_DATE_HELLO)
internal val VALUE_MIXED_DATE_BYE = Mixed.valueOf(VALUE_DATE_BYE)
internal val VALUE_MIXED_DECIMAL128_HELLO = Mixed.valueOf(VALUE_DECIMAL128_HELLO)
internal val VALUE_MIXED_DECIMAL128_BYE = Mixed.valueOf(VALUE_DECIMAL128_BYE)
internal val VALUE_MIXED_BINARY_HELLO = Mixed.valueOf(VALUE_BINARY_HELLO)
internal val VALUE_MIXED_BINARY_BYE = Mixed.valueOf(VALUE_BINARY_BYE)
internal val VALUE_MIXED_OBJECT_ID_HELLO = Mixed.valueOf(VALUE_OBJECT_ID_HELLO)
internal val VALUE_MIXED_OBJECT_ID_BYE = Mixed.valueOf(VALUE_OBJECT_ID_BYE)
internal val VALUE_MIXED_UUID_HELLO = Mixed.valueOf(VALUE_UUID_HELLO)
internal val VALUE_MIXED_UUID_BYE = Mixed.valueOf(VALUE_UUID_BYE)
internal val VALUE_MIXED_LINK_HELLO = Mixed.valueOf(VALUE_LINK_HELLO)
internal val VALUE_MIXED_LINK_BYE = Mixed.valueOf(VALUE_LINK_BYE)
internal val VALUE_MIXED_NOT_PRESENT = Mixed.valueOf(VALUE_STRING_NOT_PRESENT)

enum class DictionaryKey {
    KEY_HELLO, KEY_BYE, KEY_NULL
}

fun getMixedForType(mixedType: MixedType, dictionaryKey: DictionaryKey): Mixed? {
    return when (mixedType) {
        MixedType.INTEGER -> when (dictionaryKey) {
            DictionaryKey.KEY_HELLO -> VALUE_MIXED_INTEGER_HELLO
            DictionaryKey.KEY_BYE -> VALUE_MIXED_INTEGER_BYE
            DictionaryKey.KEY_NULL -> null
        }
        MixedType.BOOLEAN -> when (dictionaryKey) {
            DictionaryKey.KEY_HELLO -> VALUE_MIXED_BOOLEAN_HELLO
            DictionaryKey.KEY_BYE -> VALUE_MIXED_BOOLEAN_BYE
            DictionaryKey.KEY_NULL -> null
        }
        MixedType.STRING -> when (dictionaryKey) {
            DictionaryKey.KEY_HELLO -> VALUE_MIXED_STRING_HELLO
            DictionaryKey.KEY_BYE -> VALUE_MIXED_STRING_BYE
            DictionaryKey.KEY_NULL -> null
        }
        MixedType.BINARY -> when (dictionaryKey) {
            DictionaryKey.KEY_HELLO -> VALUE_MIXED_BINARY_HELLO
            DictionaryKey.KEY_BYE -> VALUE_MIXED_BINARY_BYE
            DictionaryKey.KEY_NULL -> null
        }
        MixedType.DATE -> when (dictionaryKey) {
            DictionaryKey.KEY_HELLO -> VALUE_MIXED_DATE_HELLO
            DictionaryKey.KEY_BYE -> VALUE_MIXED_DATE_BYE
            DictionaryKey.KEY_NULL -> null
        }
        MixedType.FLOAT -> when (dictionaryKey) {
            DictionaryKey.KEY_HELLO -> VALUE_MIXED_FLOAT_HELLO
            DictionaryKey.KEY_BYE -> VALUE_MIXED_FLOAT_BYE
            DictionaryKey.KEY_NULL -> null
        }
        MixedType.DOUBLE -> when (dictionaryKey) {
            DictionaryKey.KEY_HELLO -> VALUE_MIXED_DOUBLE_HELLO
            DictionaryKey.KEY_BYE -> VALUE_MIXED_DOUBLE_BYE
            DictionaryKey.KEY_NULL -> null
        }
        MixedType.DECIMAL128 -> when (dictionaryKey) {
            DictionaryKey.KEY_HELLO -> VALUE_MIXED_DECIMAL128_HELLO
            DictionaryKey.KEY_BYE -> VALUE_MIXED_DECIMAL128_BYE
            DictionaryKey.KEY_NULL -> null
        }
        MixedType.OBJECT_ID -> when (dictionaryKey) {
            DictionaryKey.KEY_HELLO -> VALUE_MIXED_OBJECT_ID_HELLO
            DictionaryKey.KEY_BYE -> VALUE_MIXED_OBJECT_ID_BYE
            DictionaryKey.KEY_NULL -> null
        }
        MixedType.OBJECT -> when (dictionaryKey) {
            DictionaryKey.KEY_HELLO -> VALUE_MIXED_LINK_HELLO
            DictionaryKey.KEY_BYE -> VALUE_MIXED_LINK_BYE
            DictionaryKey.KEY_NULL -> null
        }
        MixedType.UUID -> when (dictionaryKey) {
            DictionaryKey.KEY_HELLO -> VALUE_MIXED_UUID_HELLO
            DictionaryKey.KEY_BYE -> VALUE_MIXED_UUID_BYE
            DictionaryKey.KEY_NULL -> null
        }
        MixedType.NULL -> when (dictionaryKey) {
            DictionaryKey.KEY_HELLO -> Mixed.valueOf("Not null")
            DictionaryKey.KEY_BYE -> Mixed.nullValue()
            DictionaryKey.KEY_NULL -> null
        }
    }
}

fun getMixedKeyValuePairs(
        mixedType: MixedType,
        shouldSwapValues: Boolean = false
): List<Pair<String, Mixed?>> {
    return when (mixedType) {
        MixedType.INTEGER ->
            if (shouldSwapValues) {
                listOf(KEY_HELLO to VALUE_MIXED_INTEGER_BYE, KEY_BYE to VALUE_MIXED_INTEGER_HELLO, KEY_NULL to null)
            } else {
                listOf(KEY_HELLO to VALUE_MIXED_INTEGER_HELLO, KEY_BYE to VALUE_MIXED_INTEGER_BYE, KEY_NULL to null)
            }
        MixedType.BOOLEAN ->
            if (shouldSwapValues) {
                listOf(KEY_HELLO to null, KEY_NULL to VALUE_MIXED_BOOLEAN_HELLO)
            } else {
                listOf(KEY_HELLO to VALUE_MIXED_BOOLEAN_HELLO, KEY_NULL to null)
            }
        MixedType.STRING ->
            if (shouldSwapValues) {
                listOf(KEY_HELLO to VALUE_MIXED_STRING_BYE, KEY_BYE to VALUE_MIXED_STRING_HELLO, KEY_NULL to null)
            } else {
                listOf(KEY_HELLO to VALUE_MIXED_STRING_HELLO, KEY_BYE to VALUE_MIXED_STRING_BYE, KEY_NULL to null)
            }
        MixedType.BINARY ->
            if (shouldSwapValues) {
                listOf(KEY_HELLO to VALUE_MIXED_BINARY_BYE, KEY_BYE to VALUE_MIXED_BINARY_HELLO, KEY_NULL to null)
            } else {
                listOf(KEY_HELLO to VALUE_MIXED_BINARY_HELLO, KEY_BYE to VALUE_MIXED_BINARY_BYE, KEY_NULL to null)
            }
        MixedType.DATE ->
            if (shouldSwapValues) {
                listOf(KEY_HELLO to VALUE_MIXED_DATE_BYE, KEY_BYE to VALUE_MIXED_DATE_HELLO, KEY_NULL to null)
            } else {
                listOf(KEY_HELLO to VALUE_MIXED_DATE_HELLO, KEY_BYE to VALUE_MIXED_DATE_BYE, KEY_NULL to null)
            }
        MixedType.FLOAT ->
            if (shouldSwapValues) {
                listOf(KEY_HELLO to VALUE_MIXED_FLOAT_BYE, KEY_BYE to VALUE_MIXED_FLOAT_HELLO, KEY_NULL to null)
            } else {
                listOf(KEY_HELLO to VALUE_MIXED_FLOAT_HELLO, KEY_BYE to VALUE_MIXED_FLOAT_BYE, KEY_NULL to null)
            }
        MixedType.DOUBLE ->
            if (shouldSwapValues) {
                listOf(KEY_HELLO to VALUE_MIXED_DOUBLE_BYE, KEY_BYE to VALUE_MIXED_DOUBLE_HELLO, KEY_NULL to null)
            } else {
                listOf(KEY_HELLO to VALUE_MIXED_DOUBLE_HELLO, KEY_BYE to VALUE_MIXED_DOUBLE_BYE, KEY_NULL to null)
            }
        MixedType.DECIMAL128 ->
            if (shouldSwapValues) {
                listOf(KEY_HELLO to VALUE_MIXED_DECIMAL128_BYE, KEY_BYE to VALUE_MIXED_DECIMAL128_HELLO, KEY_NULL to null)
            } else {
                listOf(KEY_HELLO to VALUE_MIXED_DECIMAL128_HELLO, KEY_BYE to VALUE_MIXED_DECIMAL128_BYE, KEY_NULL to null)
            }
        MixedType.OBJECT_ID ->
            if (shouldSwapValues) {
                listOf(KEY_HELLO to VALUE_MIXED_OBJECT_ID_BYE, KEY_BYE to VALUE_MIXED_OBJECT_ID_HELLO, KEY_NULL to null)
            } else {
                listOf(KEY_HELLO to VALUE_MIXED_OBJECT_ID_HELLO, KEY_BYE to VALUE_MIXED_OBJECT_ID_BYE, KEY_NULL to null)
            }
        MixedType.OBJECT ->
            if (shouldSwapValues) {
                listOf(KEY_HELLO to VALUE_MIXED_LINK_BYE, KEY_BYE to VALUE_MIXED_LINK_HELLO, KEY_NULL to null)
            } else {
                listOf(KEY_HELLO to VALUE_MIXED_LINK_HELLO, KEY_BYE to VALUE_MIXED_LINK_BYE, KEY_NULL to null)
            }
        MixedType.UUID ->
            if (shouldSwapValues) {
                listOf(KEY_HELLO to VALUE_MIXED_UUID_BYE, KEY_BYE to VALUE_MIXED_UUID_HELLO, KEY_NULL to null)
            } else {
                listOf(KEY_HELLO to VALUE_MIXED_UUID_HELLO, KEY_BYE to VALUE_MIXED_UUID_BYE, KEY_NULL to null)
            }
        MixedType.NULL ->
            if (shouldSwapValues) {
                listOf(KEY_HELLO to Mixed.valueOf("Not null"), KEY_BYE to Mixed.nullValue(), KEY_NULL to null)
            } else {
                listOf(KEY_HELLO to Mixed.nullValue(), KEY_BYE to Mixed.valueOf("Not null"), KEY_NULL to null)
            }
    }
}

fun <T : Any> RealmDictionary<T>.init(
        keyValuePairs: List<Pair<String, T?>>
): RealmDictionary<T> {
    return this.apply {
        for (keyValuePair in keyValuePairs) {
            put(keyValuePair.first, keyValuePair.second)
        }
    }
}
