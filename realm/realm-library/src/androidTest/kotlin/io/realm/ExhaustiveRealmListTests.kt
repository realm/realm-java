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
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import io.realm.rule.TestRealmConfigurationFactory
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.reflect.KProperty1
import kotlin.test.assertNotNull

@RunWith(Parameterized::class)
class ExhaustiveRealmListTests(
        private val tester: ListTester<*>
) {

    companion object {

        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun testTypes(): List<ListTester<*>> {
            val normalTypes = ListSupportedType.values().mapNotNull { type ->
                when (type) {
                    ListSupportedType.LONG ->
                        ListTester("Long", DictionaryClass::myLongList, listOf(VALUE_NUMERIC_HELLO.toLong(), VALUE_NUMERIC_BYE.toLong(), null))
                    ListSupportedType.INTEGER ->
                        ListTester("Integer", DictionaryClass::myIntList, listOf(VALUE_NUMERIC_HELLO, VALUE_NUMERIC_BYE, null))
                    ListSupportedType.SHORT ->
                        ListTester("Short", DictionaryClass::myShortList, listOf(VALUE_NUMERIC_HELLO.toShort(), VALUE_NUMERIC_BYE.toShort(), null))
                    ListSupportedType.BYTE ->
                        ListTester("Byte", DictionaryClass::myByteList, listOf(VALUE_NUMERIC_HELLO.toByte(), VALUE_NUMERIC_BYE.toByte(), null))
                    ListSupportedType.FLOAT ->
                        ListTester("Float", DictionaryClass::myFloatList, listOf(VALUE_NUMERIC_HELLO.toFloat(), VALUE_NUMERIC_BYE.toFloat(), null))
                    ListSupportedType.DOUBLE ->
                        ListTester("Double", DictionaryClass::myDoubleList, listOf(VALUE_NUMERIC_HELLO.toDouble(), VALUE_NUMERIC_BYE.toDouble(), null))
                    ListSupportedType.STRING ->
                        ListTester("String", DictionaryClass::myStringList, listOf(VALUE_STRING_HELLO, VALUE_STRING_BYE, null))
                    ListSupportedType.BOOLEAN ->
                        ListTester("Boolean", DictionaryClass::myBooleanList, listOf(VALUE_BOOLEAN_HELLO, VALUE_BOOLEAN_BYE, null))
                    ListSupportedType.DATE ->
                        ListTester("Date", DictionaryClass::myDateList, listOf(VALUE_DATE_HELLO, VALUE_DATE_BYE, null))
                    ListSupportedType.DECIMAL128 ->
                        ListTester("Decimal128", DictionaryClass::myDecimal128List, listOf(VALUE_DECIMAL128_HELLO, VALUE_DECIMAL128_BYE, null))
                    ListSupportedType.BINARY ->
                        ListTester("Binary", DictionaryClass::myBinaryList, listOf(VALUE_BINARY_HELLO, VALUE_BINARY_BYE, null))
                    ListSupportedType.OBJECT_ID ->
                        ListTester("ObjectId", DictionaryClass::myObjectIdList, listOf(VALUE_OBJECT_ID_HELLO, VALUE_OBJECT_ID_BYE, null))
                    ListSupportedType.UUID ->
                        ListTester("UUID", DictionaryClass::myUUIDList, listOf(VALUE_UUID_HELLO, VALUE_UUID_BYE, null))
//                    ListSupportedType.LINK ->
//                        ListTester(DictionaryClass::myRealmModelList, listOf(VALUE_LINK_HELLO, VALUE_LINK_BYE, null))
                    else -> null
                }
            }

            return normalTypes.plus(getMixedTypes())
        }

        private fun getMixedTypes(): List<ListTester<Mixed>> {
            return MixedType.values().map { mixedType ->
                val values = when (mixedType) {
                    MixedType.INTEGER -> listOf(VALUE_MIXED_INTEGER_HELLO, VALUE_MIXED_INTEGER_BYE, null)
                    MixedType.BOOLEAN -> listOf(VALUE_MIXED_BOOLEAN_HELLO, VALUE_MIXED_BOOLEAN_BYE, null)
                    MixedType.STRING -> listOf(VALUE_MIXED_STRING_HELLO, VALUE_MIXED_STRING_BYE, null)
                    MixedType.BINARY -> listOf(VALUE_MIXED_BINARY_HELLO, VALUE_MIXED_BINARY_BYE, null)
                    MixedType.DATE -> listOf(VALUE_MIXED_DATE_HELLO, VALUE_MIXED_DATE_BYE, null)
                    MixedType.FLOAT -> listOf(VALUE_MIXED_FLOAT_HELLO, VALUE_MIXED_FLOAT_BYE, null)
                    MixedType.DOUBLE -> listOf(VALUE_MIXED_DOUBLE_HELLO, VALUE_MIXED_DOUBLE_BYE, null)
                    MixedType.DECIMAL128 -> listOf(VALUE_MIXED_DECIMAL128_HELLO, VALUE_MIXED_DECIMAL128_BYE, null)
                    MixedType.OBJECT_ID -> listOf(VALUE_MIXED_OBJECT_ID_HELLO, VALUE_MIXED_OBJECT_ID_BYE, null)
                    MixedType.OBJECT -> listOf(VALUE_MIXED_LINK_HELLO, VALUE_MIXED_LINK_BYE, null)
                    MixedType.UUID -> listOf(VALUE_MIXED_UUID_HELLO, VALUE_MIXED_UUID_BYE, null)
                    MixedType.NULL -> listOf(Mixed.nullValue())
                }
                ListTester("Mixed-${mixedType.name}", DictionaryClass::myMixedList, values)
            }
        }
    }

    @Rule
    @JvmField
    val configFactory = TestRealmConfigurationFactory()

    private lateinit var realm: Realm

    @Before
    fun setUp() {
        Realm.init(InstrumentationRegistry.getInstrumentation().context)
        realm = Realm.getInstance(configFactory.createConfiguration())
        tester.setUp(realm)
    }

    @After
    fun tearDown() {
        realm.close()
    }

    @Test
    fun add() {
        tester.add()
    }

    @Test
    fun add_position() {
        tester.addPosition()
    }

    @Test
    fun set() {
        tester.set()
    }

    @Test
    fun move() {
        tester.move()
    }

    @Test
    fun clear() {
        tester.clear()
    }

    @Test
    fun remove() {
        tester.remove()
    }

    @Test
    fun remove_element() {
        tester.remove()
    }

    @Test
    fun get() {
        tester.get()
    }

    @Test
    fun size() {
        tester.size()
    }
}

enum class ListSupportedType {
    LONG, INTEGER, SHORT, BYTE, FLOAT, DOUBLE, STRING, BOOLEAN, DATE, DECIMAL128, BINARY, OBJECT_ID,
    UUID, LINK, MIXED
}

/**
 *
 */
class ListTester<T>(
        private val testerName: String,
        private val listGetter: KProperty1<DictionaryClass, RealmList<T>>,
        private val initialValues: List<T?>
) {

    private lateinit var realm: Realm

    override fun toString(): String = testerName

    fun setUp(realm: Realm) {
        this.realm = realm
    }

    fun add() {
        val list = initList()
        realm.executeTransaction {
            initialValues.forEach { value ->
                list.add(value)
            }
        }
    }

    fun addPosition() {
        val list = initList()
        realm.executeTransaction {
            initialValues.forEachIndexed { index, value ->
                list.add(0, value)
            }
        }
    }

    fun set() {
        val list = initList()
        realm.executeTransaction {
            initialValues.forEach { value ->
                list.add(value)
            }
        }
    }

    fun move() {
        val list = initList()
        realm.executeTransaction {
            initialValues.forEach { value ->
                list.add(value)
            }
        }
    }

    fun clear() {
        val list = initList()
        realm.executeTransaction {
            initialValues.forEach { value ->
                list.add(value)
            }
        }
        realm.executeTransaction {
            list.clear()
        }
    }

    fun remove() {
        val list = initList()
        realm.executeTransaction {
            initialValues.forEach { value ->
                list.add(value)
            }
        }
        realm.executeTransaction {
            initialValues.forEach { value ->
                list.remove(value)
            }
        }
    }

    fun get() {
        val list = initList()
        realm.executeTransaction {
            initialValues.forEach { value ->
                list.add(value)
            }
        }
        for (i in initialValues.indices) {
            list[i]
        }
    }

    fun size() {
        val list = initList()
        realm.executeTransaction {
            initialValues.forEach { value ->
                list.add(value)
                list.size
            }
        }
    }

    private fun initList(): RealmList<T> {
        realm.executeTransaction {
            it.createObject<DictionaryClass>()
        }
        val container = realm.where<DictionaryClass>()
                .findFirst()
        assertNotNull(container)

        val list = listGetter.get(container)
        assertNotNull(list)

        return list
    }
}
