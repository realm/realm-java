/*
 * Copyright 2021 Realm Inc.
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

package io.realm.mixed

import androidx.test.platform.app.InstrumentationRegistry
import io.realm.*
import io.realm.entities.*
import io.realm.kotlin.where
import org.bson.types.Decimal128
import org.bson.types.ObjectId
import org.junit.*
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.lang.IllegalStateException
import java.util.*
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.javaMethod
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ParameterizedTest(
        val function: KFunction<*>,
        val parameters: Array<Any?>,
        val results: Array<Any?>
) {
    @Suppress("UNCHECKED_CAST")
    private fun asMixed(array: Array<Any?>): Array<Mixed> {
        val mixedArray = arrayOfNulls<Mixed?>(array.size)

        array.forEachIndexed { index, value ->
            if (value == null)
                mixedArray[index] = Mixed.nullValue()
            else
                when (value) {
                    is Boolean -> mixedArray[index] = Mixed.valueOf(value)
                    is Byte -> mixedArray[index] = Mixed.valueOf(value)
                    is Short -> mixedArray[index] = Mixed.valueOf(value)
                    is Int -> mixedArray[index] = Mixed.valueOf(value)
                    is Long -> mixedArray[index] = Mixed.valueOf(value)
                    is Float -> mixedArray[index] = Mixed.valueOf(value)
                    is Double -> mixedArray[index] = Mixed.valueOf(value)
                    is String -> mixedArray[index] = Mixed.valueOf(value)
                    is ByteArray -> mixedArray[index] = Mixed.valueOf(value)
                    is Decimal128 -> mixedArray[index] = Mixed.valueOf(value)
                    is ObjectId -> mixedArray[index] = Mixed.valueOf(value)
                    is UUID -> mixedArray[index] = Mixed.valueOf(value)
                    is Date -> mixedArray[index] = Mixed.valueOf(value)
                    else -> throw IllegalStateException("EqualsTo test for type ${value::class.qualifiedName} not implemented")
                }
        }

        return mixedArray as Array<Mixed>
    }

    override fun toString(): String {
        if (parameters.isEmpty()) {
            return function.javaMethod!!.name
        }

        val firstParameter = parameters[0]
        if (firstParameter is Array<*>) {
            return "${function.javaMethod!!.name}:${firstParameter::class.java.componentType}"
        }

        return "${function.javaMethod!!.name}:${parameters[0]!!::class.simpleName}"
    }

    fun execute(context: Any) {
        function.call(context, *parameters, asMixed(results))
    }

    fun executeMixed(context: Any) {
        function.call(context, *asMixed(parameters), asMixed(results))
    }
}

@RunWith(Parameterized::class)
class MixedParameterizedQueryTests(val test: ParameterizedTest) {
    private lateinit var realmConfiguration: RealmConfiguration
    private lateinit var realm: Realm

    private fun initializeTestData() {
        val mixedValues = MixedHelper.generateMixedValues()

        realm.beginTransaction()

        for (value in mixedValues) {
            val mixedObject = MixedNotIndexed(value)
            realm.insert(mixedObject)
        }

        realm.commitTransaction()
    }

    @get:Rule
    val configFactory = TestRealmConfigurationFactory()

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{index}:{0}")
        fun data(): List<ParameterizedTest> = listOf(
                // EQUALS TEST DEFINITIONS
                ParameterizedTest(
                        MixedParameterizedQueryTests::equalTo,
                        arrayOf(true),
                        arrayOf(true)
                ),
                ParameterizedTest(
                        MixedParameterizedQueryTests::equalTo,
                        arrayOf(4.toByte()),
                        arrayOf(4, 4, 4, 4)
                ),
                ParameterizedTest(
                        MixedParameterizedQueryTests::equalTo,
                        arrayOf(4.toShort()),
                        arrayOf(4, 4, 4, 4)
                ),
                ParameterizedTest(
                        MixedParameterizedQueryTests::equalTo,
                        arrayOf(4.toInt()),
                        arrayOf(4, 4, 4, 4)
                ),
                ParameterizedTest(
                        MixedParameterizedQueryTests::equalTo,
                        arrayOf(4.toLong()),
                        arrayOf(4, 4, 4, 4)
                ),
                ParameterizedTest(
                        MixedParameterizedQueryTests::equalTo,
                        arrayOf(4.toFloat()),
                        arrayOf(4.toFloat())
                ),
                ParameterizedTest(
                        MixedParameterizedQueryTests::equalTo,
                        arrayOf(4.toDouble()),
                        arrayOf(4.toDouble())
                ),
                ParameterizedTest(
                        MixedParameterizedQueryTests::equalTo,
                        arrayOf("hello world 1"),
                        arrayOf("hello world 1")
                ),
                ParameterizedTest(
                        MixedParameterizedQueryTests::equalTo,
                        arrayOf(byteArrayOf(0, 1, 0)),
                        arrayOf(byteArrayOf(0, 1, 0))
                ),
                ParameterizedTest(
                        MixedParameterizedQueryTests::equalTo,
                        arrayOf(Date(4)),
                        arrayOf(Date(4))
                ),
                ParameterizedTest(
                        MixedParameterizedQueryTests::equalTo,
                        arrayOf(Decimal128(4)),
                        arrayOf(Decimal128(4))
                ),
                ParameterizedTest(
                        MixedParameterizedQueryTests::equalTo,
                        arrayOf(ObjectId(TestHelper.generateObjectIdHexString(0))),
                        arrayOf(ObjectId(TestHelper.generateObjectIdHexString(0)))
                ),
                ParameterizedTest(
                        MixedParameterizedQueryTests::equalTo,
                        arrayOf(UUID.fromString(TestHelper.generateUUIDString(0))),
                        arrayOf(UUID.fromString(TestHelper.generateUUIDString(0)))
                ),
                // NOT EQUALS TEST DEFINITIONS
                ParameterizedTest(
                        MixedParameterizedQueryTests::notEqualTo,
                        arrayOf(true),
                        arrayOf(false)
                ),
                ParameterizedTest(
                        MixedParameterizedQueryTests::notEqualTo,
                        arrayOf(4.toByte()),
                        arrayOf(0, 0, 0, 0, 1, 1, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3, 5, 5, 5, 5, 6, 6, 6, 6, 7, 7, 7, 7, 8, 8, 8, 8, 9, 9, 9, 9)
                ),
                ParameterizedTest(
                        MixedParameterizedQueryTests::notEqualTo,
                        arrayOf(4.toShort()),
                        arrayOf(0, 0, 0, 0, 1, 1, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3, 5, 5, 5, 5, 6, 6, 6, 6, 7, 7, 7, 7, 8, 8, 8, 8, 9, 9, 9, 9)
                ),
                ParameterizedTest(
                        MixedParameterizedQueryTests::notEqualTo,
                        arrayOf(4.toInt()),
                        arrayOf(0, 0, 0, 0, 1, 1, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3, 5, 5, 5, 5, 6, 6, 6, 6, 7, 7, 7, 7, 8, 8, 8, 8, 9, 9, 9, 9)
                ),
                ParameterizedTest(
                        MixedParameterizedQueryTests::notEqualTo,
                        arrayOf(4.toLong()),
                        arrayOf(0, 0, 0, 0, 1, 1, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3, 5, 5, 5, 5, 6, 6, 6, 6, 7, 7, 7, 7, 8, 8, 8, 8, 9, 9, 9, 9)
                ),
                ParameterizedTest(
                        MixedParameterizedQueryTests::notEqualTo,
                        arrayOf(4.4.toFloat()),
                        arrayOf(4.0.toFloat(), 4.1.toFloat(), 4.2.toFloat(), 4.3.toFloat(), 4.5.toFloat(), 4.6.toFloat(), 4.7.toFloat(), 4.8.toFloat(), 4.9.toFloat())
                ),
                ParameterizedTest(
                        MixedParameterizedQueryTests::notEqualTo,
                        arrayOf(4.3.toDouble()),
                        arrayOf(4.0.toDouble(), 4.1.toDouble(), 4.2.toDouble(), 4.4.toDouble(), 4.5.toDouble(), 4.6.toDouble(), 4.7.toDouble(), 4.8.toDouble(), 4.9.toDouble())
                ),
                ParameterizedTest(
                        MixedParameterizedQueryTests::notEqualTo,
                        arrayOf("hello world 2"),
                        arrayOf("hello world 0", "hello world 1", "hello world 3", "hello world 4")
                ),
                ParameterizedTest(
                        MixedParameterizedQueryTests::notEqualTo,
                        arrayOf(byteArrayOf(0, 1, 0)),
                        arrayOf(byteArrayOf(0, 0, 0),
                                byteArrayOf(0, 1, 1),
                                byteArrayOf(1, 1, 0),
                                byteArrayOf(1, 1, 1))
                ),
                ParameterizedTest(
                        MixedParameterizedQueryTests::notEqualTo,
                        arrayOf(Date(4)),
                        arrayOf(Date(0), Date(1), Date(2), Date(3))
                ),
                ParameterizedTest(
                        MixedParameterizedQueryTests::notEqualTo,
                        arrayOf(Decimal128(4)),
                        arrayOf(Decimal128(0), Decimal128(1), Decimal128(2), Decimal128(3))
                ),
                ParameterizedTest(
                        MixedParameterizedQueryTests::notEqualTo,
                        arrayOf(ObjectId(TestHelper.generateObjectIdHexString(4))),
                        arrayOf(ObjectId(TestHelper.generateObjectIdHexString(0)), ObjectId(TestHelper.generateObjectIdHexString(1)), ObjectId(TestHelper.generateObjectIdHexString(2)), ObjectId(TestHelper.generateObjectIdHexString(3)))
                ),
                ParameterizedTest(
                        MixedParameterizedQueryTests::notEqualTo,
                        arrayOf(UUID.fromString(TestHelper.generateUUIDString(4))),
                        arrayOf(UUID.fromString(TestHelper.generateUUIDString(0)), UUID.fromString(TestHelper.generateUUIDString(1)), UUID.fromString(TestHelper.generateUUIDString(2)), UUID.fromString(TestHelper.generateUUIDString(3)))
                ),
                // GREATER THAN TEST DEFINITIONS
                ParameterizedTest(
                        MixedParameterizedQueryTests::greaterThan,
                        arrayOf(5.toByte()),
                        arrayOf(6, 6, 6, 6, 7, 7, 7, 7, 8, 8, 8, 8, 9, 9, 9, 9)
                ),
                ParameterizedTest(
                        MixedParameterizedQueryTests::greaterThan,
                        arrayOf(5.toShort()),
                        arrayOf(6, 6, 6, 6, 7, 7, 7, 7, 8, 8, 8, 8, 9, 9, 9, 9)
                ),
                ParameterizedTest(
                        MixedParameterizedQueryTests::greaterThan,
                        arrayOf(5.toInt()),
                        arrayOf(6, 6, 6, 6, 7, 7, 7, 7, 8, 8, 8, 8, 9, 9, 9, 9)
                ),
                ParameterizedTest(
                        MixedParameterizedQueryTests::greaterThan,
                        arrayOf(5.toLong()),
                        arrayOf(6, 6, 6, 6, 7, 7, 7, 7, 8, 8, 8, 8, 9, 9, 9, 9)
                ),
                ParameterizedTest(
                        MixedParameterizedQueryTests::greaterThan,
                        arrayOf(4.5.toFloat()),
                        arrayOf(4.6.toFloat(), 4.7.toFloat(), 4.8.toFloat(), 4.9.toFloat())
                ),
                ParameterizedTest(
                        MixedParameterizedQueryTests::greaterThan,
                        arrayOf(4.4.toDouble()),
                        arrayOf(4.5.toDouble(), 4.6.toDouble(), 4.7.toDouble(), 4.8.toDouble(), 4.9.toDouble())
                ),
                ParameterizedTest(
                        MixedParameterizedQueryTests::greaterThan,
                        arrayOf(Date(2)),
                        arrayOf(Date(3), Date(4))
                ),
                ParameterizedTest(
                        MixedParameterizedQueryTests::greaterThan,
                        arrayOf(Decimal128(1)),
                        arrayOf(Decimal128(2), Decimal128(3), Decimal128(4))
                ),
                ParameterizedTest(
                        MixedParameterizedQueryTests::greaterThan,
                        arrayOf(ObjectId(TestHelper.generateObjectIdHexString(1))),
                        arrayOf(ObjectId(TestHelper.generateObjectIdHexString(2)), ObjectId(TestHelper.generateObjectIdHexString(3)), ObjectId(TestHelper.generateObjectIdHexString(4)))
                ),
                ParameterizedTest(
                        MixedParameterizedQueryTests::greaterThan,
                        arrayOf(UUID.fromString(TestHelper.generateUUIDString(1))),
                        arrayOf(UUID.fromString(TestHelper.generateUUIDString(2)), UUID.fromString(TestHelper.generateUUIDString(3)), UUID.fromString(TestHelper.generateUUIDString(4)))
                ),
                // GREATER THAN OR EQUALS TEST DEFINITIONS
                ParameterizedTest(
                        MixedParameterizedQueryTests::greaterThanOrEqualTo,
                        arrayOf(5.toByte()),
                        arrayOf(5, 5, 5, 5, 6, 6, 6, 6, 7, 7, 7, 7, 8, 8, 8, 8, 9, 9, 9, 9)
                ),
                ParameterizedTest(
                        MixedParameterizedQueryTests::greaterThanOrEqualTo,
                        arrayOf(5.toShort()),
                        arrayOf(5, 5, 5, 5, 6, 6, 6, 6, 7, 7, 7, 7, 8, 8, 8, 8, 9, 9, 9, 9)
                ),
                ParameterizedTest(
                        MixedParameterizedQueryTests::greaterThanOrEqualTo,
                        arrayOf(5.toInt()),
                        arrayOf(5, 5, 5, 5, 6, 6, 6, 6, 7, 7, 7, 7, 8, 8, 8, 8, 9, 9, 9, 9)
                ),
                ParameterizedTest(
                        MixedParameterizedQueryTests::greaterThanOrEqualTo,
                        arrayOf(5.toLong()),
                        arrayOf(5, 5, 5, 5, 6, 6, 6, 6, 7, 7, 7, 7, 8, 8, 8, 8, 9, 9, 9, 9)
                ),
                ParameterizedTest(
                        MixedParameterizedQueryTests::greaterThanOrEqualTo,
                        arrayOf(4.5.toFloat()),
                        arrayOf(4.5.toFloat(), 4.6.toFloat(), 4.7.toFloat(), 4.8.toFloat(), 4.9.toFloat())
                ),
                ParameterizedTest(
                        MixedParameterizedQueryTests::greaterThanOrEqualTo,
                        arrayOf(4.4.toDouble()),
                        arrayOf(4.4.toDouble(), 4.5.toDouble(), 4.6.toDouble(), 4.7.toDouble(), 4.8.toDouble(), 4.9.toDouble())
                ),
                ParameterizedTest(
                        MixedParameterizedQueryTests::greaterThanOrEqualTo,
                        arrayOf(Date(2)),
                        arrayOf(Date(2), Date(3), Date(4))
                ),
                ParameterizedTest(
                        MixedParameterizedQueryTests::greaterThanOrEqualTo,
                        arrayOf(Decimal128(1)),
                        arrayOf(Decimal128(1), Decimal128(2), Decimal128(3), Decimal128(4))
                ),
                ParameterizedTest(
                        MixedParameterizedQueryTests::greaterThanOrEqualTo,
                        arrayOf(ObjectId(TestHelper.generateObjectIdHexString(1))),
                        arrayOf(ObjectId(TestHelper.generateObjectIdHexString(1)), ObjectId(TestHelper.generateObjectIdHexString(2)), ObjectId(TestHelper.generateObjectIdHexString(3)), ObjectId(TestHelper.generateObjectIdHexString(4)))
                ),
                ParameterizedTest(
                        MixedParameterizedQueryTests::greaterThanOrEqualTo,
                        arrayOf(UUID.fromString(TestHelper.generateUUIDString(1))),
                        arrayOf(UUID.fromString(TestHelper.generateUUIDString(1)), UUID.fromString(TestHelper.generateUUIDString(2)), UUID.fromString(TestHelper.generateUUIDString(3)), UUID.fromString(TestHelper.generateUUIDString(4)))
                ),
                // LESS THAN TEST DEFINITIONS
                ParameterizedTest(
                        MixedParameterizedQueryTests::lessThan,
                        arrayOf(4.toByte()),
                        arrayOf(0, 0, 0, 0, 1, 1, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3)
                ),
                ParameterizedTest(
                        MixedParameterizedQueryTests::lessThan,
                        arrayOf(4.toShort()),
                        arrayOf(0, 0, 0, 0, 1, 1, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3)
                ),
                ParameterizedTest(
                        MixedParameterizedQueryTests::lessThan,
                        arrayOf(4.toInt()),
                        arrayOf(0, 0, 0, 0, 1, 1, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3)
                ),
                ParameterizedTest(
                        MixedParameterizedQueryTests::lessThan,
                        arrayOf(4.toLong()),
                        arrayOf(0, 0, 0, 0, 1, 1, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3)
                ),
                ParameterizedTest(
                        MixedParameterizedQueryTests::lessThan,
                        arrayOf(4.5.toFloat()),
                        arrayOf(4.0.toFloat(), 4.1.toFloat(), 4.2.toFloat(), 4.3.toFloat(), 4.4.toFloat())
                ),
                ParameterizedTest(
                        MixedParameterizedQueryTests::lessThan,
                        arrayOf(4.3.toDouble()),
                        arrayOf(4.0.toDouble(), 4.1.toDouble(), 4.2.toDouble())
                ),
                ParameterizedTest(
                        MixedParameterizedQueryTests::lessThan,
                        arrayOf(Date(3)),
                        arrayOf(Date(0), Date(1), Date(2))
                ),
                ParameterizedTest(
                        MixedParameterizedQueryTests::lessThan,
                        arrayOf(Decimal128(3)),
                        arrayOf(Decimal128(0), Decimal128(1), Decimal128(2))
                ),
                ParameterizedTest(
                        MixedParameterizedQueryTests::lessThan,
                        arrayOf(ObjectId(TestHelper.generateObjectIdHexString(3))),
                        arrayOf(ObjectId(TestHelper.generateObjectIdHexString(0)), ObjectId(TestHelper.generateObjectIdHexString(1)), ObjectId(TestHelper.generateObjectIdHexString(2)))
                ),
                ParameterizedTest(
                        MixedParameterizedQueryTests::lessThan,
                        arrayOf(UUID.fromString(TestHelper.generateUUIDString(3))),
                        arrayOf(UUID.fromString(TestHelper.generateUUIDString(0)), UUID.fromString(TestHelper.generateUUIDString(1)), UUID.fromString(TestHelper.generateUUIDString(2)))
                ),
                // LESS THAN OR EQUALS TEST DEFINITIONS
                ParameterizedTest(
                        MixedParameterizedQueryTests::lessThanOrEqualTo,
                        arrayOf(4.toByte()),
                        arrayOf(0, 0, 0, 0, 1, 1, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3, 4, 4, 4, 4)
                ),
                ParameterizedTest(
                        MixedParameterizedQueryTests::lessThanOrEqualTo,
                        arrayOf(4.toShort()),
                        arrayOf(0, 0, 0, 0, 1, 1, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3, 4, 4, 4, 4)
                ),
                ParameterizedTest(
                        MixedParameterizedQueryTests::lessThanOrEqualTo,
                        arrayOf(4.toInt()),
                        arrayOf(0, 0, 0, 0, 1, 1, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3, 4, 4, 4, 4)
                ),
                ParameterizedTest(
                        MixedParameterizedQueryTests::lessThanOrEqualTo,
                        arrayOf(4.toLong()),
                        arrayOf(0, 0, 0, 0, 1, 1, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3, 4, 4, 4, 4)
                ),
                ParameterizedTest(
                        MixedParameterizedQueryTests::lessThanOrEqualTo,
                        arrayOf(4.5.toFloat()),
                        arrayOf(4.0.toFloat(), 4.1.toFloat(), 4.2.toFloat(), 4.3.toFloat(), 4.4.toFloat(), 4.5.toFloat())
                ),
                ParameterizedTest(
                        MixedParameterizedQueryTests::lessThanOrEqualTo,
                        arrayOf(4.3.toDouble()),
                        arrayOf(4.0.toDouble(), 4.1.toDouble(), 4.2.toDouble(), 4.3.toDouble())
                ),
                ParameterizedTest(
                        MixedParameterizedQueryTests::lessThanOrEqualTo,
                        arrayOf(Date(3)),
                        arrayOf(Date(0), Date(1), Date(2), Date(3))
                ),
                ParameterizedTest(
                        MixedParameterizedQueryTests::lessThanOrEqualTo,
                        arrayOf(Decimal128(3)),
                        arrayOf(Decimal128(0), Decimal128(1), Decimal128(2), Decimal128(3))
                ),
                ParameterizedTest(
                        MixedParameterizedQueryTests::lessThanOrEqualTo,
                        arrayOf(ObjectId(TestHelper.generateObjectIdHexString(3))),
                        arrayOf(ObjectId(TestHelper.generateObjectIdHexString(0)), ObjectId(TestHelper.generateObjectIdHexString(1)), ObjectId(TestHelper.generateObjectIdHexString(2)), ObjectId(TestHelper.generateObjectIdHexString(3)))
                ),
                ParameterizedTest(
                        MixedParameterizedQueryTests::lessThanOrEqualTo,
                        arrayOf(UUID.fromString(TestHelper.generateUUIDString(3))),
                        arrayOf(UUID.fromString(TestHelper.generateUUIDString(0)), UUID.fromString(TestHelper.generateUUIDString(1)), UUID.fromString(TestHelper.generateUUIDString(2)), UUID.fromString(TestHelper.generateUUIDString(3)))
                ),
                // IN TEST DEFINITIONS
                ParameterizedTest(
                        MixedParameterizedQueryTests::`in`,
                        arrayOf(arrayOf(true, null)),
                        arrayOf(true)
                ),
                ParameterizedTest(
                        MixedParameterizedQueryTests::`in`,
                        arrayOf(arrayOf(4.toByte(), 2.toByte(), 5.toByte(), 22.toByte(), null)),
                        arrayOf(2, 2, 2, 2, 4, 4, 4, 4, 5, 5, 5, 5)
                ),
                ParameterizedTest(
                        MixedParameterizedQueryTests::`in`,
                        arrayOf(arrayOf(4.toShort(), 2.toShort(), 5.toShort(), 22.toShort(), null)),
                        arrayOf(2, 2, 2, 2, 4, 4, 4, 4, 5, 5, 5, 5)
                ),
                ParameterizedTest(
                        MixedParameterizedQueryTests::`in`,
                        arrayOf(arrayOf(4.toInt(), 2.toInt(), 5.toInt(), 22.toInt(), null)),
                        arrayOf(2, 2, 2, 2, 4, 4, 4, 4, 5, 5, 5, 5)
                ),
                ParameterizedTest(
                        MixedParameterizedQueryTests::`in`,
                        arrayOf(arrayOf(4.toLong(), 2.toLong(), 5.toLong(), 22.toLong(), null)),
                        arrayOf(2, 2, 2, 2, 4, 4, 4, 4, 5, 5, 5, 5)
                ),
                ParameterizedTest(
                        MixedParameterizedQueryTests::`in`,
                        arrayOf(arrayOf(4.8.toFloat(), 8.1.toFloat(), 4.3.toFloat(), 4.0.toFloat(), 4.7.toFloat(), null)),
                        arrayOf(4.0.toFloat(), 4.3.toFloat(), 4.7.toFloat(), 4.8.toFloat())
                ),
                ParameterizedTest(
                        MixedParameterizedQueryTests::`in`,
                        arrayOf(arrayOf(4.8.toDouble(), 8.1.toDouble(), 4.3.toDouble(), 4.0.toDouble(), 4.7.toDouble(), null)),
                        arrayOf(4.0.toDouble(), 4.3.toDouble(), 4.7.toDouble(), 4.8.toDouble())
                ),
                ParameterizedTest(
                        MixedParameterizedQueryTests::`in`,
                        arrayOf(arrayOf("hello world 3", "hello world 0", "hello world 4", "realm rocks", null)),
                        arrayOf("hello world 0", "hello world 3", "hello world 4")
                ),
                ParameterizedTest(
                        MixedParameterizedQueryTests::`in`,
                        arrayOf(arrayOf(
                                byteArrayOf(0, 0, 0),
                                byteArrayOf(0, 1, 1),
                                byteArrayOf(1, 1, 0, 0),
                                byteArrayOf(1, 1, 1),
                                null
                        )),
                        arrayOf(
                                byteArrayOf(0, 0, 0),
                                byteArrayOf(0, 1, 1),
                                byteArrayOf(1, 1, 1)
                        )
                ),
                ParameterizedTest(
                        MixedParameterizedQueryTests::`in`,
                        arrayOf(arrayOf(Date(100), Date(3), null, Date(1))),
                        arrayOf(Date(1), Date(3))
                ),
                ParameterizedTest(
                        MixedParameterizedQueryTests::`in`,
                        arrayOf(arrayOf(null, Decimal128(100), Decimal128(3), Decimal128(2))),
                        arrayOf(Decimal128(2), Decimal128(3))
                ),
                ParameterizedTest(
                        MixedParameterizedQueryTests::`in`,
                        arrayOf(arrayOf(null, ObjectId(TestHelper.generateObjectIdHexString(3)), ObjectId(TestHelper.generateObjectIdHexString(9)), ObjectId(TestHelper.generateObjectIdHexString(1)))),
                        arrayOf(ObjectId(TestHelper.generateObjectIdHexString(1)), ObjectId(TestHelper.generateObjectIdHexString(3)))
                ),
                ParameterizedTest(
                        MixedParameterizedQueryTests::`in`,
                        arrayOf(arrayOf(null, UUID.fromString(TestHelper.generateUUIDString(3)), UUID.fromString(TestHelper.generateUUIDString(9)), UUID.fromString(TestHelper.generateUUIDString(1)))),
                        arrayOf(UUID.fromString(TestHelper.generateUUIDString(1)), UUID.fromString(TestHelper.generateUUIDString(3)))
                ),
                // BETWEEN TEST DEFINITIONS
                ParameterizedTest(
                        MixedParameterizedQueryTests::between,
                        arrayOf(6.toByte(), 8.toByte()),
                        arrayOf(6, 6, 6, 6, 7, 7, 7, 7, 8, 8, 8, 8)
                ),
                ParameterizedTest(
                        MixedParameterizedQueryTests::between,
                        arrayOf(6.toShort(), 8.toShort()),
                        arrayOf(6, 6, 6, 6, 7, 7, 7, 7, 8, 8, 8, 8)
                ),
                ParameterizedTest(
                        MixedParameterizedQueryTests::between,
                        arrayOf(6.toInt(), 8.toInt()),
                        arrayOf(6, 6, 6, 6, 7, 7, 7, 7, 8, 8, 8, 8)
                ),
                ParameterizedTest(
                        MixedParameterizedQueryTests::between,
                        arrayOf(6.toLong(), 8.toLong()),
                        arrayOf(6, 6, 6, 6, 7, 7, 7, 7, 8, 8, 8, 8)
                ),
                ParameterizedTest(
                        MixedParameterizedQueryTests::between,
                        arrayOf(4.51.toFloat(), 4.89.toFloat()),
                        arrayOf(4.6.toFloat(), 4.7.toFloat(), 4.8.toFloat())
                ),
                ParameterizedTest(
                        MixedParameterizedQueryTests::between,
                        arrayOf(4.49.toDouble(), 4.89.toDouble()),
                        arrayOf(4.5.toDouble(), 4.6.toDouble(), 4.7.toDouble(), 4.8.toDouble())
                ),
                ParameterizedTest(
                        MixedParameterizedQueryTests::between,
                        arrayOf(Date(2), Date(4)),
                        arrayOf(Date(2), Date(3), Date(4))
                ),
                ParameterizedTest(
                        MixedParameterizedQueryTests::between,
                        arrayOf(Decimal128(2), Decimal128(4)),
                        arrayOf(Decimal128(2), Decimal128(3), Decimal128(4))
                ),
                ParameterizedTest(
                        MixedParameterizedQueryTests::between,
                        arrayOf(ObjectId(TestHelper.generateObjectIdHexString(2)), ObjectId(TestHelper.generateObjectIdHexString(4))),
                        arrayOf(ObjectId(TestHelper.generateObjectIdHexString(2)), ObjectId(TestHelper.generateObjectIdHexString(3)), ObjectId(TestHelper.generateObjectIdHexString(4)))
                ),
                ParameterizedTest(
                        MixedParameterizedQueryTests::between,
                        arrayOf(UUID.fromString(TestHelper.generateUUIDString(2)), UUID.fromString(TestHelper.generateUUIDString(4))),
                        arrayOf(UUID.fromString(TestHelper.generateUUIDString(2)), UUID.fromString(TestHelper.generateUUIDString(3)), UUID.fromString(TestHelper.generateUUIDString(4)))
                ),
                ParameterizedTest(
                        MixedParameterizedQueryTests::beginsWith,
                        arrayOf("hello"),
                        arrayOf("hello world 0", "hello world 1", "hello world 2", "hello world 3", "hello world 4")
                ),
                ParameterizedTest(
                        MixedParameterizedQueryTests::beginsWithInsensitive,
                        arrayOf("hELlo"),
                        arrayOf("hello world 0", "hello world 1", "hello world 2", "hello world 3", "hello world 4",
                                "HELLO WORLD 0", "HELLO WORLD 1", "HELLO WORLD 2", "HELLO WORLD 3", "HELLO WORLD 4")
                ),
                ParameterizedTest(
                        MixedParameterizedQueryTests::endsWith,
                        arrayOf("world 4"),
                        arrayOf("hello world 4")
                ),
                ParameterizedTest(
                        MixedParameterizedQueryTests::endsWithInsensitive,
                        arrayOf("wOrld 4"),
                        arrayOf("hello world 4",
                                "HELLO WORLD 4")
                ),
                ParameterizedTest(
                        MixedParameterizedQueryTests::contains,
                        arrayOf("world"),
                        arrayOf("hello world 0", "hello world 1", "hello world 2", "hello world 3", "hello world 4")
                ),
                ParameterizedTest(
                        MixedParameterizedQueryTests::containsInsensitive,
                        arrayOf("WorLD"),
                        arrayOf("hello world 0", "hello world 1", "hello world 2", "hello world 3", "hello world 4",
                                "HELLO WORLD 0", "HELLO WORLD 1", "HELLO WORLD 2", "HELLO WORLD 3", "HELLO WORLD 4")
                ),
                ParameterizedTest(
                        MixedParameterizedQueryTests::like,
                        arrayOf("*w?rld*"),
                        arrayOf("hello world 0", "hello world 1", "hello world 2", "hello world 3", "hello world 4")
                ),
                ParameterizedTest(
                        MixedParameterizedQueryTests::likeInsensitive,
                        arrayOf("*W?RLD*"),
                        arrayOf("hello world 0", "hello world 1", "hello world 2", "hello world 3", "hello world 4",
                                "HELLO WORLD 0", "HELLO WORLD 1", "HELLO WORLD 2", "HELLO WORLD 3", "HELLO WORLD 4")
                )
        )
    }

    init {
        Realm.init(InstrumentationRegistry.getInstrumentation().targetContext)
    }

    @Before
    fun setUp() {
        realmConfiguration = configFactory.createSchemaConfiguration(
                false,
                MixedNotIndexed::class.java)

        realm = Realm.getInstance(realmConfiguration)

        initializeTestData()
    }

    @After
    fun tearDown() {
        realm.close()
    }

    @Test
    fun primitives() {
        this.test.execute(this)
    }

    @Test
    @Ignore("FIXME: Using mixed as parameters do not work yet.")
    fun mixed() {
        this.test.executeMixed(this)
    }

    private fun validate(expected: Array<Mixed>, results: RealmResults<MixedNotIndexed>, skipContents: Boolean = false) {
        assertEquals(expected.size, results.size)

        if (!skipContents) {
            expected.forEachIndexed { index, item ->
                val comparing = results[index]!!.mixed!!
                assertTrue(item.coercedEquals(comparing))
            }
        }
    }

    fun equalTo(value: Any, expected: Array<Mixed>) {
        val query: RealmQuery<MixedNotIndexed> = realm.where()

        when (value) {
            is Boolean -> query.equalTo(MixedNotIndexed.FIELD_MIXED, value)
            is Byte -> query.equalTo(MixedNotIndexed.FIELD_MIXED, value)
            is Short -> query.equalTo(MixedNotIndexed.FIELD_MIXED, value)
            is Int -> query.equalTo(MixedNotIndexed.FIELD_MIXED, value)
            is Long -> query.equalTo(MixedNotIndexed.FIELD_MIXED, value)
            is Float -> query.equalTo(MixedNotIndexed.FIELD_MIXED, value)
            is Double -> query.equalTo(MixedNotIndexed.FIELD_MIXED, value)
            is String -> query.equalTo(MixedNotIndexed.FIELD_MIXED, value)
            is ByteArray -> query.equalTo(MixedNotIndexed.FIELD_MIXED, value)
            is Decimal128 -> query.equalTo(MixedNotIndexed.FIELD_MIXED, value)
            is ObjectId -> query.equalTo(MixedNotIndexed.FIELD_MIXED, value)
            is UUID -> query.equalTo(MixedNotIndexed.FIELD_MIXED, value)
            is Date -> query.equalTo(MixedNotIndexed.FIELD_MIXED, value)
//            is Mixed -> query.equalTo(MixedNotIndexed.FIELD_MIXED, value)
            else -> throw IllegalStateException("EqualTo for type ${value::class.qualifiedName} not implemented")
        }

        validate(expected, query.findAll()!!)
    }

    fun notEqualTo(value: Any, expected: Array<Mixed>) {
        val query: RealmQuery<MixedNotIndexed> = realm.where()

        when (value) {
            is Boolean -> query.notEqualTo(MixedNotIndexed.FIELD_MIXED, value)
            is Byte -> query.notEqualTo(MixedNotIndexed.FIELD_MIXED, value)
            is Short -> query.notEqualTo(MixedNotIndexed.FIELD_MIXED, value)
            is Int -> query.notEqualTo(MixedNotIndexed.FIELD_MIXED, value)
            is Long -> query.notEqualTo(MixedNotIndexed.FIELD_MIXED, value)
            is Float -> query.notEqualTo(MixedNotIndexed.FIELD_MIXED, value)
            is Double -> query.notEqualTo(MixedNotIndexed.FIELD_MIXED, value)
            is String -> query.notEqualTo(MixedNotIndexed.FIELD_MIXED, value)
            is ByteArray -> query.notEqualTo(MixedNotIndexed.FIELD_MIXED, value)
            is Decimal128 -> query.notEqualTo(MixedNotIndexed.FIELD_MIXED, value)
            is ObjectId -> query.notEqualTo(MixedNotIndexed.FIELD_MIXED, value)
            is UUID -> query.notEqualTo(MixedNotIndexed.FIELD_MIXED, value)
            is Date -> query.notEqualTo(MixedNotIndexed.FIELD_MIXED, value)
//            is Mixed -> query.notEqualTo(MixedNotIndexed.FIELD_MIXED, value)
            else -> throw IllegalStateException("NotEqualTo for type ${value::class.qualifiedName} not implemented")
        }

        validate(expected, query.findAll()!!)
    }

    fun equalToInsensitive(value: Any, expected: Array<Mixed>) {
        val query: RealmQuery<MixedNotIndexed> = realm.where()

        when (value) {
            is String -> query.equalTo(MixedNotIndexed.FIELD_MIXED, value, Case.INSENSITIVE)
//            is Mixed -> query.equalTo(MixedNotIndexed.FIELD_MIXED, value, Case.INSENSITIVE)
            else -> throw IllegalStateException("EqualTo[c] for type ${value::class.qualifiedName} not implemented")
        }

        validate(expected, query.findAll()!!)
    }

    fun notEqualToInsensitive(value: Any, expected: Array<Mixed>) {
        val query: RealmQuery<MixedNotIndexed> = realm.where()

        when (value) {
            is String -> query.notEqualTo(MixedNotIndexed.FIELD_MIXED, value, Case.INSENSITIVE)
//            is Mixed -> query.notEqualTo(MixedNotIndexed.FIELD_MIXED, value, Case.INSENSITIVE)
            else -> throw IllegalStateException("NotEqualTo[c] for type ${value::class.qualifiedName} not implemented")
        }

        validate(expected, query.findAll()!!)
    }

    fun greaterThanOrEqualTo(value: Any, expected: Array<Mixed>) {
        val query: RealmQuery<MixedNotIndexed> = realm.where()

        when (value) {
            is Int -> query.greaterThanOrEqualTo(MixedNotIndexed.FIELD_MIXED, value)
            is Long -> query.greaterThanOrEqualTo(MixedNotIndexed.FIELD_MIXED, value)
            is Float -> query.greaterThanOrEqualTo(MixedNotIndexed.FIELD_MIXED, value)
            is Double -> query.greaterThanOrEqualTo(MixedNotIndexed.FIELD_MIXED, value)
            is Decimal128 -> query.greaterThanOrEqualTo(MixedNotIndexed.FIELD_MIXED, value)
            is ObjectId -> query.greaterThanOrEqualTo(MixedNotIndexed.FIELD_MIXED, value)
            is UUID -> query.greaterThanOrEqualTo(MixedNotIndexed.FIELD_MIXED, value)
            is Date -> query.greaterThanOrEqualTo(MixedNotIndexed.FIELD_MIXED, value)
//            is Mixed -> query.greaterThanOrEqualTo(MixedNotIndexed.FIELD_MIXED, value)
            else -> throw IllegalStateException("GreaterThanOrEqualTo for type ${value::class.qualifiedName} not implemented")
        }

        validate(expected, query.findAll()!!)
    }

    fun greaterThan(value: Any, expected: Array<Mixed>) {
        val query: RealmQuery<MixedNotIndexed> = realm.where()

        when (value) {
            is Int -> query.greaterThan(MixedNotIndexed.FIELD_MIXED, value)
            is Long -> query.greaterThan(MixedNotIndexed.FIELD_MIXED, value)
            is Float -> query.greaterThan(MixedNotIndexed.FIELD_MIXED, value)
            is Double -> query.greaterThan(MixedNotIndexed.FIELD_MIXED, value)
            is Decimal128 -> query.greaterThan(MixedNotIndexed.FIELD_MIXED, value)
            is ObjectId -> query.greaterThan(MixedNotIndexed.FIELD_MIXED, value)
            is UUID -> query.greaterThan(MixedNotIndexed.FIELD_MIXED, value)
            is Date -> query.greaterThan(MixedNotIndexed.FIELD_MIXED, value)
//            is Mixed -> query.greaterThan(MixedNotIndexed.FIELD_MIXED, value)
            else -> throw IllegalStateException("GreaterThan for type ${value::class.qualifiedName} not implemented")
        }

        validate(expected, query.findAll()!!)
    }

    fun lessThanOrEqualTo(value: Any, expected: Array<Mixed>) {
        val query: RealmQuery<MixedNotIndexed> = realm.where()

        when (value) {
            is Int -> query.lessThanOrEqualTo(MixedNotIndexed.FIELD_MIXED, value)
            is Long -> query.lessThanOrEqualTo(MixedNotIndexed.FIELD_MIXED, value)
            is Float -> query.lessThanOrEqualTo(MixedNotIndexed.FIELD_MIXED, value)
            is Double -> query.lessThanOrEqualTo(MixedNotIndexed.FIELD_MIXED, value)
            is Decimal128 -> query.lessThanOrEqualTo(MixedNotIndexed.FIELD_MIXED, value)
            is ObjectId -> query.lessThanOrEqualTo(MixedNotIndexed.FIELD_MIXED, value)
            is UUID -> query.lessThanOrEqualTo(MixedNotIndexed.FIELD_MIXED, value)
            is Date -> query.lessThanOrEqualTo(MixedNotIndexed.FIELD_MIXED, value)
//            is Mixed -> query.lessThanOrEqualTo(MixedNotIndexed.FIELD_MIXED, value)
            else -> throw IllegalStateException("LessThanOrEqualTo for type ${value::class.qualifiedName} not implemented")
        }

        validate(expected, query.findAll()!!)
    }

    fun lessThan(value: Any, expected: Array<Mixed>) {
        val query: RealmQuery<MixedNotIndexed> = realm.where()

        when (value) {
            is Int -> query.lessThan(MixedNotIndexed.FIELD_MIXED, value)
            is Long -> query.lessThan(MixedNotIndexed.FIELD_MIXED, value)
            is Float -> query.lessThan(MixedNotIndexed.FIELD_MIXED, value)
            is Double -> query.lessThan(MixedNotIndexed.FIELD_MIXED, value)
            is Decimal128 -> query.lessThan(MixedNotIndexed.FIELD_MIXED, value)
            is ObjectId -> query.lessThan(MixedNotIndexed.FIELD_MIXED, value)
            is UUID -> query.lessThan(MixedNotIndexed.FIELD_MIXED, value)
            is Date -> query.lessThan(MixedNotIndexed.FIELD_MIXED, value)
//            is Mixed -> query.lessThan(MixedNotIndexed.FIELD_MIXED, value)
            else -> throw IllegalStateException("LessThan for type ${value::class.qualifiedName} not implemented")
        }

        validate(expected, query.findAll()!!)
    }

    @Suppress("UNCHECKED_CAST")
    fun `in`(value: Any, expected: Array<Mixed>) {
        val query: RealmQuery<MixedNotIndexed> = realm.where()

        if (value is Array<*> && value.size > 0) {
            when (value[0]) {
                is Boolean -> query.`in`(MixedNotIndexed.FIELD_MIXED, value as Array<out Boolean?>)
                is Byte -> query.`in`(MixedNotIndexed.FIELD_MIXED, value as Array<out Byte?>)
                is Short -> query.`in`(MixedNotIndexed.FIELD_MIXED, value as Array<out Short?>)
                is Int -> query.`in`(MixedNotIndexed.FIELD_MIXED, value as Array<out Int?>)
                is Long -> query.`in`(MixedNotIndexed.FIELD_MIXED, value as Array<out Long?>)
                is Float -> query.`in`(MixedNotIndexed.FIELD_MIXED, value as Array<out Float?>)
                is Double -> query.`in`(MixedNotIndexed.FIELD_MIXED, value as Array<out Double?>)
                is String -> query.`in`(MixedNotIndexed.FIELD_MIXED, value as Array<out String?>)
                is Date -> query.`in`(MixedNotIndexed.FIELD_MIXED, value as Array<out Date?>)
//            is Mixed -> query.`in`(MixedNotIndexed.FIELD_MIXED, value)
                else -> throw IllegalStateException("In for type ${value::class.qualifiedName} not implemented")
            }
        }

        validate(expected, query.findAll()!!)
    }

    fun between(value1: Any, value2: Any, expected: Array<Mixed>) {
        val query: RealmQuery<MixedNotIndexed> = realm.where()

        when (value1) {
            is Int -> query.between(MixedNotIndexed.FIELD_MIXED, value1, value2 as Int)
            is Long -> query.between(MixedNotIndexed.FIELD_MIXED, value1, value2 as Long)
            is Float -> query.between(MixedNotIndexed.FIELD_MIXED, value1, value2 as Float)
            is Double -> query.between(MixedNotIndexed.FIELD_MIXED, value1, value2 as Double)
            is Decimal128 -> query.between(MixedNotIndexed.FIELD_MIXED, value1, value2 as Decimal128)
            is Date -> query.between(MixedNotIndexed.FIELD_MIXED, value1, value2 as Date)
//            is Mixed -> query.between(MixedNotIndexed.FIELD_MIXED, value)
            else -> throw IllegalStateException("Between for type ${value1::class.qualifiedName} not implemented")
        }

        validate(expected, query.findAll()!!)
    }

    fun beginsWith(value: Any, expected: Array<Mixed>) {
        val query: RealmQuery<MixedNotIndexed> = realm.where()

        when (value) {
            is String -> query.beginsWith(MixedNotIndexed.FIELD_MIXED, value)
//            is Mixed -> query.beginsWith(MixedNotIndexed.FIELD_MIXED, value)
            else -> throw IllegalStateException("BeginsWith for type ${value::class.qualifiedName} not implemented")
        }

        validate(expected, query.findAll()!!)
    }

    fun beginsWithInsensitive(value: Any, expected: Array<Mixed>) {
        val query: RealmQuery<MixedNotIndexed> = realm.where()

        when (value) {
            is String -> query.beginsWith(MixedNotIndexed.FIELD_MIXED, value, Case.INSENSITIVE)
//            is Mixed -> query.beginsWith(MixedNotIndexed.FIELD_MIXED, value, Case.INSENSITIVE)
            else -> throw IllegalStateException("BeginsWith for type ${value::class.qualifiedName} not implemented")
        }

        validate(expected, query.findAll()!!)
    }

    fun endsWith(value: Any, expected: Array<Mixed>) {
        val query: RealmQuery<MixedNotIndexed> = realm.where()

        when (value) {
            is String -> query.endsWith(MixedNotIndexed.FIELD_MIXED, value)
//            is Mixed -> query.beginsWith(MixedNotIndexed.FIELD_MIXED, value)
            else -> throw IllegalStateException("EndsWith for type ${value::class.qualifiedName} not implemented")
        }

        validate(expected, query.findAll()!!)
    }

    fun endsWithInsensitive(value: Any, expected: Array<Mixed>) {
        val query: RealmQuery<MixedNotIndexed> = realm.where()

        when (value) {
            is String -> query.endsWith(MixedNotIndexed.FIELD_MIXED, value, Case.INSENSITIVE)
//            is Mixed -> query.beginsWith(MixedNotIndexed.FIELD_MIXED, value, Case.INSENSITIVE)
            else -> throw IllegalStateException("EndsWith for type ${value::class.qualifiedName} not implemented")
        }

        validate(expected, query.findAll()!!)
    }

    fun contains(value: Any, expected: Array<Mixed>) {
        val query: RealmQuery<MixedNotIndexed> = realm.where()

        when (value) {
            is String -> query.contains(MixedNotIndexed.FIELD_MIXED, value)
//            is Mixed -> query.contains(MixedNotIndexed.FIELD_MIXED, value)
            else -> throw IllegalStateException("Contains for type ${value::class.qualifiedName} not implemented")
        }

        validate(expected, query.findAll()!!)
    }

    fun containsInsensitive(value: Any, expected: Array<Mixed>) {
        val query: RealmQuery<MixedNotIndexed> = realm.where()

        when (value) {
            is String -> query.contains(MixedNotIndexed.FIELD_MIXED, value, Case.INSENSITIVE)
//            is Mixed -> query.contains(MixedNotIndexed.FIELD_MIXED, value, Case.INSENSITIVE)
            else -> throw IllegalStateException("Contains for type ${value::class.qualifiedName} not implemented")
        }

        validate(expected, query.findAll()!!)
    }

    fun like(value: Any, expected: Array<Mixed>) {
        val query: RealmQuery<MixedNotIndexed> = realm.where()

        when (value) {
            is String -> query.like(MixedNotIndexed.FIELD_MIXED, value)
//            is Mixed -> query.like(MixedNotIndexed.FIELD_MIXED, value)
            else -> throw IllegalStateException("Like for type ${value::class.qualifiedName} not implemented")
        }

        validate(expected, query.findAll()!!)
    }

    fun likeInsensitive(value: Any, expected: Array<Mixed>) {
        val query: RealmQuery<MixedNotIndexed> = realm.where()

        when (value) {
            is String -> query.like(MixedNotIndexed.FIELD_MIXED, value, Case.INSENSITIVE)
//            is Mixed -> query.like(MixedNotIndexed.FIELD_MIXED, value, Case.INSENSITIVE)
            else -> throw IllegalStateException("Like for type ${value::class.qualifiedName} not implemented")
        }

        validate(expected, query.findAll()!!)
    }
}