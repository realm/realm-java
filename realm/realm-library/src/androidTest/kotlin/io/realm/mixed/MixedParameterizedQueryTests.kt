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
import io.realm.entities.MixedNotIndexed
import io.realm.entities.PrimaryKeyAsString
import io.realm.kotlin.where
import org.bson.types.Decimal128
import org.bson.types.ObjectId
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.lang.reflect.InvocationTargetException
import java.util.*
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.javaMethod
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MixedParameterizedQueryTest(
        val function: KFunction<*>,
        val parameters: Array<Any?>,
        val results: Array<Any?>,
        val expectedSize: Int? = null,
        val expand: Boolean = true,
        val checkUnmanagedModels: Boolean = false
) {
    @Suppress("UNCHECKED_CAST")
    private fun asMixed(array: Array<Any?>, realm: Realm, copyToRealm: Boolean = true): Array<Mixed> {
        val mixedArray = arrayOfNulls<Mixed?>(array.size)

        realm.executeTransaction{
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
                        is RealmModel -> mixedArray[index] = Mixed.valueOf(if (copyToRealm) realm.copyToRealmOrUpdate(value) else value)
                        else -> throw IllegalStateException("EqualsTo test for type ${value::class.qualifiedName} not implemented")
                    }
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

    fun execute(context: Any, realm: Realm) {
        if (expand) {
            function.call(context, asMixed(results, realm), expectedSize ?: results.size, *parameters)
        } else {
            function.call(context, asMixed(results, realm), expectedSize ?: results.size, parameters)
        }
    }

    fun executeMixed(context: Any, realm: Realm) {
        if (expand) {
            function.call(context, asMixed(results, realm), expectedSize ?: results.size, *asMixed(parameters, realm))
        } else {
            function.call(context, asMixed(results, realm), expectedSize ?: results.size, asMixed(parameters, realm))
        }
    }

    fun executeMixed_nonManagedRealmModel(context: Any, realm: Realm) {
        if(!checkUnmanagedModels){
            return
        }

        val exception = assertFailsWith<InvocationTargetException>("Non managed Realm objects are not valid query arguments"){
            if (expand) {
                function.call(context, asMixed(results, realm, false),
                        expectedSize ?: results.size, *asMixed(parameters, realm, false))
            } else {
                function.call(context, asMixed(results, realm, false),
                        expectedSize ?: results.size, asMixed(parameters, realm, false))
            }
        }

        assertTrue(exception.cause is IllegalArgumentException)
    }
}

@RunWith(Parameterized::class)
class MixedParameterizedQueryTests(val test: MixedParameterizedQueryTest) {
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
        fun data(): List<MixedParameterizedQueryTest> = listOf(
                // EQUALS TEST DEFINITIONS
                MixedParameterizedQueryTest(
                        function = MixedParameterizedQueryTests::equalTo,
                        parameters = arrayOf(true),
                        results = arrayOf(true)
                ),
                MixedParameterizedQueryTest(
                        function = MixedParameterizedQueryTests::equalTo,
                        parameters = arrayOf(4.toByte()),
                        results = arrayOf(4, 4, 4, 4, 4.0.toFloat(), 4.0, Decimal128(4))
                ),
                MixedParameterizedQueryTest(
                        function = MixedParameterizedQueryTests::equalTo,
                        parameters = arrayOf(4.toShort()),
                        results = arrayOf(4, 4, 4, 4, 4.0.toFloat(), 4.0, Decimal128(4))
                ),
                MixedParameterizedQueryTest(
                        function = MixedParameterizedQueryTests::equalTo,
                        parameters = arrayOf(4.toInt()),
                        results = arrayOf(4, 4, 4, 4, 4.0.toFloat(), 4.0, Decimal128(4))
                ),
                MixedParameterizedQueryTest(
                        function = MixedParameterizedQueryTests::equalTo,
                        parameters = arrayOf(4.toLong()),
                        results = arrayOf(4, 4, 4, 4, 4.0.toFloat(), 4.0, Decimal128(4))
                ),
                MixedParameterizedQueryTest(
                        function = MixedParameterizedQueryTests::equalTo,
                        parameters = arrayOf(4.toFloat()),
                        results = arrayOf(4, 4, 4, 4, 4.0.toFloat(), 4.0, Decimal128(4))
                ),
                MixedParameterizedQueryTest(
                        function = MixedParameterizedQueryTests::equalTo,
                        parameters = arrayOf(4.toDouble()),
                        results = arrayOf(4, 4, 4, 4, 4.0.toFloat(), 4.0, Decimal128(4))
                ),
                MixedParameterizedQueryTest(
                        function = MixedParameterizedQueryTests::equalTo,
                        parameters = arrayOf("hello world 1"),
                        results = arrayOf("hello world 1")
                ),
                MixedParameterizedQueryTest(
                        function = MixedParameterizedQueryTests::equalTo,
                        parameters = arrayOf(byteArrayOf(0, 1, 0)),
                        results = arrayOf(byteArrayOf(0, 1, 0))
                ),
                MixedParameterizedQueryTest(
                        function = MixedParameterizedQueryTests::equalTo,
                        parameters = arrayOf(Date(4)),
                        results = arrayOf(Date(4))
                ),
                MixedParameterizedQueryTest(
                        function = MixedParameterizedQueryTests::equalTo,
                        parameters = arrayOf(Decimal128(4)),
                        results = arrayOf(4, 4, 4, 4, 4.0.toFloat(), 4.0, Decimal128(4))
                ),
                MixedParameterizedQueryTest(
                        function = MixedParameterizedQueryTests::equalTo,
                        parameters = arrayOf(ObjectId(TestHelper.generateObjectIdHexString(0))),
                        results = arrayOf(ObjectId(TestHelper.generateObjectIdHexString(0)))
                ),
                MixedParameterizedQueryTest(
                        function = MixedParameterizedQueryTests::equalTo,
                        parameters = arrayOf(UUID.fromString(TestHelper.generateUUIDString(0))),
                        results = arrayOf(UUID.fromString(TestHelper.generateUUIDString(0)))
                ),
                MixedParameterizedQueryTest(
                        function = MixedParameterizedQueryTests::equalToInsensitive,
                        parameters = arrayOf("hello world 2"),
                        results = arrayOf("hello world 2", "HELLO WORLD 2")
                ),
                MixedParameterizedQueryTest(
                        function = MixedParameterizedQueryTests::equalTo,
                        parameters = arrayOf(PrimaryKeyAsString("item 1")),
                        results = arrayOf(PrimaryKeyAsString("item 1")),
                        checkUnmanagedModels = true
                ),
                // NOT EQUALS TEST DEFINITIONS
                MixedParameterizedQueryTest(
                        function = MixedParameterizedQueryTests::notEqualTo,
                        parameters = arrayOf(true),
                        results = arrayOf(false),
                        expectedSize = 111
                ),
                MixedParameterizedQueryTest(
                        function = MixedParameterizedQueryTests::notEqualTo,
                        parameters = arrayOf(4.toByte()),
                        results = arrayOf(4.toByte()),
                        expectedSize = 105
                ),
                MixedParameterizedQueryTest(
                        function = MixedParameterizedQueryTests::notEqualTo,
                        parameters = arrayOf(4.toShort()),
                        results = arrayOf(4.toShort()),
                        expectedSize = 105
                ),
                MixedParameterizedQueryTest(
                        function = MixedParameterizedQueryTests::notEqualTo,
                        parameters = arrayOf(4.toInt()),
                        results = arrayOf(4.toInt()),
                        expectedSize = 105
                ),
                MixedParameterizedQueryTest(
                        function = MixedParameterizedQueryTests::notEqualTo,
                        parameters = arrayOf(4.toLong()),
                        results = arrayOf(4.toLong()),
                        expectedSize = 105
                ),
                MixedParameterizedQueryTest(
                        function = MixedParameterizedQueryTests::notEqualTo,
                        parameters = arrayOf(4.4.toFloat()),
                        results = arrayOf(4.4.toFloat()),
                        expectedSize = 111
                ),
                MixedParameterizedQueryTest(
                        function = MixedParameterizedQueryTests::notEqualTo,
                        parameters = arrayOf(4.3),
                        results = arrayOf(4.3),
                        expectedSize = 111
                ),
                MixedParameterizedQueryTest(
                        function = MixedParameterizedQueryTests::notEqualTo,
                        parameters = arrayOf("hello world 2"),
                        results = arrayOf("hello world 2"),
                        expectedSize = 111
                ),
                MixedParameterizedQueryTest(
                        function = MixedParameterizedQueryTests::notEqualTo,
                        parameters = arrayOf(byteArrayOf(0, 1, 0)),
                        results = arrayOf(byteArrayOf(0, 1, 0)),
                        expectedSize = 111
                ),
                MixedParameterizedQueryTest(
                        MixedParameterizedQueryTests::notEqualTo,
                        parameters = arrayOf(Date(4)),
                        results = arrayOf(Date(4)),
                        expectedSize = 111
                ),
                MixedParameterizedQueryTest(
                        function = MixedParameterizedQueryTests::notEqualTo,
                        parameters = arrayOf(Decimal128(4)),
                        results = arrayOf(Decimal128(4)),
                        expectedSize = 105
                ),
                MixedParameterizedQueryTest(
                        function = MixedParameterizedQueryTests::notEqualTo,
                        parameters = arrayOf(ObjectId(TestHelper.generateObjectIdHexString(4))),
                        results = arrayOf(ObjectId(TestHelper.generateObjectIdHexString(4))),
                        expectedSize = 111
                ),
                MixedParameterizedQueryTest(
                        function = MixedParameterizedQueryTests::notEqualTo,
                        parameters = arrayOf(UUID.fromString(TestHelper.generateUUIDString(4))),
                        results = arrayOf(UUID.fromString(TestHelper.generateUUIDString(4))),
                        expectedSize = 111
                ),
                MixedParameterizedQueryTest(
                        function = MixedParameterizedQueryTests::notEqualToInsensitive,
                        parameters = arrayOf("HELLO WORLD 2"),
                        results = arrayOf("HELLO WORLD 2"),
                        expectedSize = 110
                ),
                MixedParameterizedQueryTest(
                        function = MixedParameterizedQueryTests::notEqualTo,
                        parameters = arrayOf(PrimaryKeyAsString("item 1")),
                        results = arrayOf(PrimaryKeyAsString("item 1")),
                        expectedSize = 111,
                        checkUnmanagedModels = true
                ),
                // GREATER THAN TEST DEFINITIONS
                MixedParameterizedQueryTest(
                        function = MixedParameterizedQueryTests::greaterThan,
                        parameters = arrayOf(5.toByte()),
                        results = arrayOf(6, 6, 6, 6, 7, 7, 7, 7, 8, 8, 8, 8, 9, 9, 9, 9)
                ),
                MixedParameterizedQueryTest(
                        function = MixedParameterizedQueryTests::greaterThan,
                        parameters = arrayOf(5.toShort()),
                        results = arrayOf(6, 6, 6, 6, 7, 7, 7, 7, 8, 8, 8, 8, 9, 9, 9, 9)
                ),
                MixedParameterizedQueryTest(
                        function = MixedParameterizedQueryTests::greaterThan,
                        parameters = arrayOf(5.toInt()),
                        results = arrayOf(6, 6, 6, 6, 7, 7, 7, 7, 8, 8, 8, 8, 9, 9, 9, 9)
                ),
                MixedParameterizedQueryTest(
                        function = MixedParameterizedQueryTests::greaterThan,
                        parameters = arrayOf(5.toLong()),
                        results = arrayOf(6, 6, 6, 6, 7, 7, 7, 7, 8, 8, 8, 8, 9, 9, 9, 9)
                ),
                MixedParameterizedQueryTest(
                        function = MixedParameterizedQueryTests::greaterThan,
                        parameters = arrayOf(4.5.toFloat()),
                        results = arrayOf(5, 5, 5, 5, 6, 6, 6, 6, 7, 7, 7, 7, 8, 8, 8, 8, 9, 9, 9, 9, 4.6.toFloat(), 4.7.toFloat(), 4.8.toFloat(), 4.9.toFloat(), 4.6, 4.7, 4.8, 4.9)
                ),
                MixedParameterizedQueryTest(
                        function = MixedParameterizedQueryTests::greaterThan,
                        parameters = arrayOf(4.425),
                        results = arrayOf(5, 5, 5, 5, 6, 6, 6, 6, 7, 7, 7, 7, 8, 8, 8, 8, 9, 9, 9, 9, 4.5.toFloat(), 4.6.toFloat(), 4.7.toFloat(), 4.8.toFloat(), 4.9.toFloat(), 4.5, 4.6, 4.7, 4.8, 4.9)
                ),
                MixedParameterizedQueryTest(
                        function = MixedParameterizedQueryTests::greaterThan,
                        parameters = arrayOf(Date(2)),
                        results = arrayOf(Date(3), Date(4))
                ),
                MixedParameterizedQueryTest(
                        function = MixedParameterizedQueryTests::greaterThan,
                        parameters = arrayOf(Decimal128(3)),
                        results = arrayOf(4, 4, 4, 4, 5, 5, 5, 5, 6, 6, 6, 6, 7, 7, 7, 7, 8, 8, 8, 8, 9, 9, 9, 9, 4.0.toFloat(), 4.1.toFloat(), 4.2.toFloat(), 4.3.toFloat(), 4.4.toFloat(), 4.5.toFloat(), 4.6.toFloat(), 4.7.toFloat(), 4.8.toFloat(), 4.9.toFloat(), 4.0, 4.1, 4.2, 4.3, 4.4, 4.5, 4.6, 4.7, 4.8, 4.9, Decimal128(4))
                ),
                MixedParameterizedQueryTest(
                        function = MixedParameterizedQueryTests::greaterThan,
                        parameters = arrayOf(ObjectId(TestHelper.generateObjectIdHexString(1))),
                        results = arrayOf(ObjectId(TestHelper.generateObjectIdHexString(2)), ObjectId(TestHelper.generateObjectIdHexString(3)), ObjectId(TestHelper.generateObjectIdHexString(4)))
                ),
                MixedParameterizedQueryTest(
                        function = MixedParameterizedQueryTests::greaterThan,
                        parameters = arrayOf(UUID.fromString(TestHelper.generateUUIDString(1))),
                        results = arrayOf(UUID.fromString(TestHelper.generateUUIDString(2)), UUID.fromString(TestHelper.generateUUIDString(3)), UUID.fromString(TestHelper.generateUUIDString(4)))
                ),
                // GREATER THAN OR EQUALS TEST DEFINITIONS
                MixedParameterizedQueryTest(
                        function = MixedParameterizedQueryTests::greaterThanOrEqualTo,
                        parameters = arrayOf(5.toByte()),
                        results = arrayOf(5, 5, 5, 5, 6, 6, 6, 6, 7, 7, 7, 7, 8, 8, 8, 8, 9, 9, 9, 9)
                ),
                MixedParameterizedQueryTest(
                        function = MixedParameterizedQueryTests::greaterThanOrEqualTo,
                        parameters = arrayOf(5.toShort()),
                        results = arrayOf(5, 5, 5, 5, 6, 6, 6, 6, 7, 7, 7, 7, 8, 8, 8, 8, 9, 9, 9, 9)
                ),
                MixedParameterizedQueryTest(
                        function = MixedParameterizedQueryTests::greaterThanOrEqualTo,
                        parameters = arrayOf(5.toInt()),
                        results = arrayOf(5, 5, 5, 5, 6, 6, 6, 6, 7, 7, 7, 7, 8, 8, 8, 8, 9, 9, 9, 9)
                ),
                MixedParameterizedQueryTest(
                        function = MixedParameterizedQueryTests::greaterThanOrEqualTo,
                        parameters = arrayOf(5.toLong()),
                        results = arrayOf(5, 5, 5, 5, 6, 6, 6, 6, 7, 7, 7, 7, 8, 8, 8, 8, 9, 9, 9, 9)
                ),
                MixedParameterizedQueryTest(
                        function = MixedParameterizedQueryTests::greaterThanOrEqualTo,
                        parameters = arrayOf(4.5.toFloat()),
                        results = arrayOf(5, 5, 5, 5, 6, 6, 6, 6, 7, 7, 7, 7, 8, 8, 8, 8, 9, 9, 9, 9, 4.5.toFloat(), 4.6.toFloat(), 4.7.toFloat(), 4.8.toFloat(), 4.9.toFloat(), 4.5, 4.6, 4.7, 4.8, 4.9)
                ),
                MixedParameterizedQueryTest(
                        function = MixedParameterizedQueryTests::greaterThanOrEqualTo,
                        parameters = arrayOf(4.4),
                        results = arrayOf(5, 5, 5, 5, 6, 6, 6, 6, 7, 7, 7, 7, 8, 8, 8, 8, 9, 9, 9, 9, 4.4.toFloat(), 4.5.toFloat(), 4.6.toFloat(), 4.7.toFloat(), 4.8.toFloat(), 4.9.toFloat(), 4.4, 4.5, 4.6, 4.7, 4.8, 4.9)
                ),
                MixedParameterizedQueryTest(
                        function = MixedParameterizedQueryTests::greaterThanOrEqualTo,
                        parameters = arrayOf(Date(2)),
                        results = arrayOf(Date(2), Date(3), Date(4))
                ),
                MixedParameterizedQueryTest(
                        function = MixedParameterizedQueryTests::greaterThanOrEqualTo,
                        parameters = arrayOf(Decimal128(3)),
                        results = arrayOf(3, 3, 3, 3, 4, 4, 4, 4, 5, 5, 5, 5, 6, 6, 6, 6, 7, 7, 7, 7, 8, 8, 8, 8, 9, 9, 9, 9, 4.0.toFloat(), 4.1.toFloat(), 4.2.toFloat(), 4.3.toFloat(), 4.4.toFloat(), 4.5.toFloat(), 4.6.toFloat(), 4.7.toFloat(), 4.8.toFloat(), 4.9.toFloat(), 4.0, 4.1, 4.2, 4.3, 4.4, 4.5, 4.6, 4.7, 4.8, 4.9, Decimal128(3), Decimal128(4))
                ),
                MixedParameterizedQueryTest(
                        function = MixedParameterizedQueryTests::greaterThanOrEqualTo,
                        parameters = arrayOf(ObjectId(TestHelper.generateObjectIdHexString(1))),
                        results = arrayOf(ObjectId(TestHelper.generateObjectIdHexString(1)), ObjectId(TestHelper.generateObjectIdHexString(2)), ObjectId(TestHelper.generateObjectIdHexString(3)), ObjectId(TestHelper.generateObjectIdHexString(4)))
                ),
                MixedParameterizedQueryTest(
                        function = MixedParameterizedQueryTests::greaterThanOrEqualTo,
                        parameters = arrayOf(UUID.fromString(TestHelper.generateUUIDString(1))),
                        results = arrayOf(UUID.fromString(TestHelper.generateUUIDString(1)), UUID.fromString(TestHelper.generateUUIDString(2)), UUID.fromString(TestHelper.generateUUIDString(3)), UUID.fromString(TestHelper.generateUUIDString(4)))
                ),
                // LESS THAN TEST DEFINITIONS
                MixedParameterizedQueryTest(
                        function = MixedParameterizedQueryTests::lessThan,
                        parameters = arrayOf(4.toByte()),
                        results = arrayOf(0, 0, 0, 0, 1, 1, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3, Decimal128(0), Decimal128(1), Decimal128(2), Decimal128(3))
                ),
                MixedParameterizedQueryTest(
                        function = MixedParameterizedQueryTests::lessThan,
                        parameters = arrayOf(4.toShort()),
                        results = arrayOf(0, 0, 0, 0, 1, 1, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3, Decimal128(0), Decimal128(1), Decimal128(2), Decimal128(3))
                ),
                MixedParameterizedQueryTest(
                        function = MixedParameterizedQueryTests::lessThan,
                        parameters = arrayOf(4.toInt()),
                        results = arrayOf(0, 0, 0, 0, 1, 1, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3, Decimal128(0), Decimal128(1), Decimal128(2), Decimal128(3))
                ),
                MixedParameterizedQueryTest(
                        function = MixedParameterizedQueryTests::lessThan,
                        parameters = arrayOf(4.toLong()),
                        results = arrayOf(0, 0, 0, 0, 1, 1, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3, Decimal128(0), Decimal128(1), Decimal128(2), Decimal128(3))
                ),
                MixedParameterizedQueryTest(
                        function = MixedParameterizedQueryTests::lessThan,
                        parameters = arrayOf(4.5.toFloat()),
                        results = arrayOf(0, 0, 0, 0, 1, 1, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3, 4, 4, 4, 4, 4.0.toFloat(), 4.1.toFloat(), 4.2.toFloat(), 4.3.toFloat(), 4.4.toFloat(), 4.0, 4.1, 4.2, 4.3, 4.4, Decimal128(0), Decimal128(1), Decimal128(2), Decimal128(3), Decimal128(4))
                ),
                MixedParameterizedQueryTest(
                        function = MixedParameterizedQueryTests::lessThan,
                        parameters = arrayOf(4.3),
                        results = arrayOf(0, 0, 0, 0, 1, 1, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3, 4, 4, 4, 4, 4.0.toFloat(), 4.1.toFloat(), 4.2.toFloat(), 4.0, 4.1, 4.2, Decimal128(0), Decimal128(1), Decimal128(2), Decimal128(3), Decimal128(4))
                ),
                MixedParameterizedQueryTest(
                        function = MixedParameterizedQueryTests::lessThan,
                        parameters = arrayOf(Date(3)),
                        results = arrayOf(Date(0), Date(1), Date(2))
                ),
                MixedParameterizedQueryTest(
                        function = MixedParameterizedQueryTests::lessThan,
                        parameters = arrayOf(Decimal128(3)),
                        results = arrayOf(0, 0, 0, 0, 1, 1, 1, 1, 2, 2, 2, 2, Decimal128(0), Decimal128(1), Decimal128(2))
                ),
                MixedParameterizedQueryTest(
                        function = MixedParameterizedQueryTests::lessThan,
                        parameters = arrayOf(ObjectId(TestHelper.generateObjectIdHexString(3))),
                        results = arrayOf(ObjectId(TestHelper.generateObjectIdHexString(0)), ObjectId(TestHelper.generateObjectIdHexString(1)), ObjectId(TestHelper.generateObjectIdHexString(2)))
                ),
                MixedParameterizedQueryTest(
                        function = MixedParameterizedQueryTests::lessThan,
                        parameters = arrayOf(UUID.fromString(TestHelper.generateUUIDString(3))),
                        results = arrayOf(UUID.fromString(TestHelper.generateUUIDString(0)), UUID.fromString(TestHelper.generateUUIDString(1)), UUID.fromString(TestHelper.generateUUIDString(2)))
                ),
                // LESS THAN OR EQUALS TEST DEFINITIONS
                MixedParameterizedQueryTest(
                        function = MixedParameterizedQueryTests::lessThanOrEqualTo,
                        parameters = arrayOf(4.toByte()),
                        results = arrayOf(0, 0, 0, 0, 1, 1, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3, 4, 4, 4, 4, 4.0.toFloat(), 4.0, Decimal128(0), Decimal128(1), Decimal128(2), Decimal128(3), Decimal128(4))
                ),
                MixedParameterizedQueryTest(
                        function = MixedParameterizedQueryTests::lessThanOrEqualTo,
                        parameters = arrayOf(4.toShort()),
                        results = arrayOf(0, 0, 0, 0, 1, 1, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3, 4, 4, 4, 4, 4.0.toFloat(), 4.0, Decimal128(0), Decimal128(1), Decimal128(2), Decimal128(3), Decimal128(4))
                ),
                MixedParameterizedQueryTest(
                        function = MixedParameterizedQueryTests::lessThanOrEqualTo,
                        parameters = arrayOf(4.toInt()),
                        results = arrayOf(0, 0, 0, 0, 1, 1, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3, 4, 4, 4, 4, 4.0.toFloat(), 4.0, Decimal128(0), Decimal128(1), Decimal128(2), Decimal128(3), Decimal128(4))
                ),
                MixedParameterizedQueryTest(
                        function = MixedParameterizedQueryTests::lessThanOrEqualTo,
                        parameters = arrayOf(4.toLong()),
                        results = arrayOf(0, 0, 0, 0, 1, 1, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3, 4, 4, 4, 4, 4.0.toFloat(), 4.0, Decimal128(0), Decimal128(1), Decimal128(2), Decimal128(3), Decimal128(4))
                ),
                MixedParameterizedQueryTest(
                        function = MixedParameterizedQueryTests::lessThanOrEqualTo,
                        parameters = arrayOf(4.5.toFloat()),
                        results = arrayOf(0, 0, 0, 0, 1, 1, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3, 4, 4, 4, 4, 4.0.toFloat(), 4.1.toFloat(), 4.2.toFloat(), 4.3.toFloat(), 4.4.toFloat(), 4.5.toFloat(), 4.0, 4.1, 4.2, 4.3, 4.4, 4.5, Decimal128(0), Decimal128(1), Decimal128(2), Decimal128(3), Decimal128(4))
                ),
                MixedParameterizedQueryTest(
                        function = MixedParameterizedQueryTests::lessThanOrEqualTo,
                        parameters = arrayOf(4.325),
                        results = arrayOf(0, 0, 0, 0, 1, 1, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3, 4, 4, 4, 4, 4.0.toFloat(), 4.1.toFloat(), 4.2.toFloat(), 4.3.toFloat(), 4.0, 4.1, 4.2, 4.3, Decimal128(0), Decimal128(1), Decimal128(2), Decimal128(3), Decimal128(4))
                ),
                MixedParameterizedQueryTest(
                        function = MixedParameterizedQueryTests::lessThanOrEqualTo,
                        parameters = arrayOf(Date(3)),
                        results = arrayOf(Date(0), Date(1), Date(2), Date(3))
                ),
                MixedParameterizedQueryTest(
                        function = MixedParameterizedQueryTests::lessThanOrEqualTo,
                        parameters = arrayOf(Decimal128(3)),
                        results = arrayOf(0, 0, 0, 0, 1, 1, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3, Decimal128(0), Decimal128(1), Decimal128(2), Decimal128(3))
                ),
                MixedParameterizedQueryTest(
                        function = MixedParameterizedQueryTests::lessThanOrEqualTo,
                        parameters = arrayOf(ObjectId(TestHelper.generateObjectIdHexString(3))),
                        results = arrayOf(ObjectId(TestHelper.generateObjectIdHexString(0)), ObjectId(TestHelper.generateObjectIdHexString(1)), ObjectId(TestHelper.generateObjectIdHexString(2)), ObjectId(TestHelper.generateObjectIdHexString(3)))
                ),
                MixedParameterizedQueryTest(
                        function = MixedParameterizedQueryTests::lessThanOrEqualTo,
                        parameters = arrayOf(UUID.fromString(TestHelper.generateUUIDString(3))),
                        results = arrayOf(UUID.fromString(TestHelper.generateUUIDString(0)), UUID.fromString(TestHelper.generateUUIDString(1)), UUID.fromString(TestHelper.generateUUIDString(2)), UUID.fromString(TestHelper.generateUUIDString(3)))
                ),
                // IN TEST DEFINITIONS
                MixedParameterizedQueryTest(
                        function = MixedParameterizedQueryTests::`in`,
                        parameters = arrayOf(true, null),
                        results = arrayOf(true, *arrayOfNulls(9)),
                        expand = false
                ),
                MixedParameterizedQueryTest(
                        function = MixedParameterizedQueryTests::`in`,
                        parameters = arrayOf(4.toByte(), 2.toByte(), 5.toByte(), 22.toByte(), null),
                        results = arrayOf(2, 2, 2, 2, 4, 4, 4, 4, 5, 5, 5, 5, 4.0.toFloat(), 4.0, Decimal128(2), Decimal128(4), *arrayOfNulls(9)),
                        expand = false
                ),
                MixedParameterizedQueryTest(
                        function = MixedParameterizedQueryTests::`in`,
                        parameters = arrayOf(4.toShort(), 2.toShort(), 5.toShort(), 22.toShort(), null),
                        results = arrayOf(2, 2, 2, 2, 4, 4, 4, 4, 5, 5, 5, 5, 4.0.toFloat(), 4.0, Decimal128(2), Decimal128(4), *arrayOfNulls(9)),
                        expand = false
                ),
                MixedParameterizedQueryTest(
                        function = MixedParameterizedQueryTests::`in`,
                        parameters = arrayOf(4.toInt(), 2.toInt(), 5.toInt(), 22.toInt(), null),
                        results = arrayOf(2, 2, 2, 2, 4, 4, 4, 4, 5, 5, 5, 5, 4.0.toFloat(), 4.0, Decimal128(2), Decimal128(4), *arrayOfNulls(9)),
                        expand = false
                ),
                MixedParameterizedQueryTest(
                        function = MixedParameterizedQueryTests::`in`,
                        parameters = arrayOf(4.toLong(), 2.toLong(), 5.toLong(), 22.toLong(), null),
                        results = arrayOf(2, 2, 2, 2, 4, 4, 4, 4, 5, 5, 5, 5, 4.0.toFloat(), 4.0, Decimal128(2), Decimal128(4), *arrayOfNulls(9)),
                        expand = false
                ),
                MixedParameterizedQueryTest(
                        function = MixedParameterizedQueryTests::`in`,
                        parameters = arrayOf(4.8.toFloat(), 8.1.toFloat(), 4.3.toFloat(), 4.0.toFloat(), 4.7.toFloat(), null),
                        results = arrayOf(4, 4, 4, 4, 4.0.toFloat(), 4.3.toFloat(), 4.7.toFloat(), 4.8.toFloat(), 4.0, Decimal128(4), *arrayOfNulls(9)),
                        expand = false
                ),
                MixedParameterizedQueryTest(
                        function = MixedParameterizedQueryTests::`in`,
                        parameters = arrayOf(4.8, 8.1, 4.3, 4.0, 4.7, null),
                        results = arrayOf(4, 4, 4, 4, 4.0, 4.0, 4.3, 4.7, 4.8, Decimal128(4), *arrayOfNulls(9)),
                        expand = false
                ),
                MixedParameterizedQueryTest(
                        function = MixedParameterizedQueryTests::`in`,
                        parameters = arrayOf("hello world 3", "hello world 0", "hello world 4", "realm rocks", null),
                        results = arrayOf("hello world 0", "hello world 3", "hello world 4", *arrayOfNulls(9)),
                        expand = false
                ),
                MixedParameterizedQueryTest(
                        function = MixedParameterizedQueryTests::`in`,
                        parameters = arrayOf(
                                byteArrayOf(0, 0, 0),
                                byteArrayOf(0, 1, 1),
                                byteArrayOf(1, 1, 0, 0),
                                byteArrayOf(1, 1, 1),
                                null
                        ),
                        results = arrayOf(
                                byteArrayOf(0, 0, 0),
                                byteArrayOf(0, 1, 1),
                                byteArrayOf(1, 1, 1),
                                *arrayOfNulls(9)
                        ),
                        expand = false
                ),
                MixedParameterizedQueryTest(
                        function = MixedParameterizedQueryTests::`in`,
                        parameters = arrayOf(Date(100), Date(3), null, Date(1)),
                        results = arrayOf(Date(1), Date(3), *arrayOfNulls(9)),
                        expand = false
                ),
                MixedParameterizedQueryTest(
                        function = MixedParameterizedQueryTests::`in`,
                        parameters = arrayOf(Decimal128(100), null, Decimal128(3), Decimal128(2)),
                        results = arrayOf(2, 2, 2, 2, 3, 3, 3, 3, Decimal128(2), Decimal128(3), *arrayOfNulls(9)),
                        expand = false
                ),
                MixedParameterizedQueryTest(
                        function = MixedParameterizedQueryTests::`in`,
                        parameters = arrayOf(ObjectId(TestHelper.generateObjectIdHexString(3)), null, ObjectId(TestHelper.generateObjectIdHexString(9)), ObjectId(TestHelper.generateObjectIdHexString(1))),
                        results = arrayOf(ObjectId(TestHelper.generateObjectIdHexString(1)), ObjectId(TestHelper.generateObjectIdHexString(3)), *arrayOfNulls(9)),
                        expand = false
                ),
                MixedParameterizedQueryTest(
                        function = MixedParameterizedQueryTests::`in`,
                        parameters = arrayOf(UUID.fromString(TestHelper.generateUUIDString(3)), null, UUID.fromString(TestHelper.generateUUIDString(9)), UUID.fromString(TestHelper.generateUUIDString(1))),
                        results = arrayOf(UUID.fromString(TestHelper.generateUUIDString(1)), UUID.fromString(TestHelper.generateUUIDString(3)), *arrayOfNulls(9)),
                        expand = false
                ),
                MixedParameterizedQueryTest(
                        function = MixedParameterizedQueryTests::`in`,
                        parameters = arrayOf(PrimaryKeyAsString("item 1"), PrimaryKeyAsString("item 3")),
                        results = arrayOf(PrimaryKeyAsString("item 1"), PrimaryKeyAsString("item 3")),
                        expand = false
                ),
                MixedParameterizedQueryTest(
                        function = MixedParameterizedQueryTests::`in`,
                        parameters = arrayOf(PrimaryKeyAsString("item 1"), PrimaryKeyAsString("item 3")),
                        results = arrayOf(PrimaryKeyAsString("item 1"), PrimaryKeyAsString("item 3")),
                        expand = false,
                        checkUnmanagedModels = true
                ),
                // BETWEEN TEST DEFINITIONS
                MixedParameterizedQueryTest(
                        function = MixedParameterizedQueryTests::between,
                        parameters = arrayOf(6.toByte(), 8.toByte()),
                        results = arrayOf(6, 6, 6, 6, 7, 7, 7, 7, 8, 8, 8, 8)
                ),
                MixedParameterizedQueryTest(
                        function = MixedParameterizedQueryTests::between,
                        parameters = arrayOf(6.toShort(), 8.toShort()),
                        results = arrayOf(6, 6, 6, 6, 7, 7, 7, 7, 8, 8, 8, 8)
                ),
                MixedParameterizedQueryTest(
                        function = MixedParameterizedQueryTests::between,
                        parameters = arrayOf(6.toInt(), 8.toInt()),
                        results = arrayOf(6, 6, 6, 6, 7, 7, 7, 7, 8, 8, 8, 8)
                ),
                MixedParameterizedQueryTest(
                        function = MixedParameterizedQueryTests::between,
                        parameters = arrayOf(6.toLong(), 8.toLong()),
                        results = arrayOf(6, 6, 6, 6, 7, 7, 7, 7, 8, 8, 8, 8)
                ),
                MixedParameterizedQueryTest(
                        function = MixedParameterizedQueryTests::between,
                        parameters = arrayOf(4.51.toFloat(), 4.89.toFloat()),
                        results = arrayOf(4.6.toFloat(), 4.7.toFloat(), 4.8.toFloat(), 4.6, 4.7, 4.8)
                ),
                MixedParameterizedQueryTest(
                        function = MixedParameterizedQueryTests::between,
                        parameters = arrayOf(4.49, 4.89),
                        results = arrayOf(4.5.toFloat(), 4.6.toFloat(), 4.7.toFloat(), 4.8.toFloat(), 4.5, 4.6, 4.7, 4.8)
                ),
                MixedParameterizedQueryTest(
                        function = MixedParameterizedQueryTests::between,
                        parameters = arrayOf(Date(2), Date(4)),
                        results = arrayOf(Date(2), Date(3), Date(4))
                ),
                MixedParameterizedQueryTest(
                        function = MixedParameterizedQueryTests::between,
                        parameters = arrayOf(Decimal128(2), Decimal128(4)),
                        results = arrayOf(2, 2, 2, 2, 3, 3, 3, 3, 4, 4, 4, 4, 4.0.toFloat(), 4.0, Decimal128(2), Decimal128(3), Decimal128(4))
                ),
                MixedParameterizedQueryTest(
                        function = MixedParameterizedQueryTests::between,
                        parameters = arrayOf(ObjectId(TestHelper.generateObjectIdHexString(2)), ObjectId(TestHelper.generateObjectIdHexString(4))),
                        results = arrayOf(ObjectId(TestHelper.generateObjectIdHexString(2)), ObjectId(TestHelper.generateObjectIdHexString(3)), ObjectId(TestHelper.generateObjectIdHexString(4)))
                ),
                MixedParameterizedQueryTest(
                        function = MixedParameterizedQueryTests::between,
                        parameters = arrayOf(UUID.fromString(TestHelper.generateUUIDString(2)), UUID.fromString(TestHelper.generateUUIDString(4))),
                        results = arrayOf(UUID.fromString(TestHelper.generateUUIDString(2)), UUID.fromString(TestHelper.generateUUIDString(3)), UUID.fromString(TestHelper.generateUUIDString(4)))
                ),
                MixedParameterizedQueryTest(
                        function = MixedParameterizedQueryTests::beginsWith,
                        parameters = arrayOf("hello"),
                        results = arrayOf("hello world 0", "hello world 1", "hello world 2", "hello world 3", "hello world 4")
                ),
                MixedParameterizedQueryTest(
                        function = MixedParameterizedQueryTests::beginsWithInsensitive,
                        parameters = arrayOf("hELlo"),
                        results = arrayOf("hello world 0", "hello world 1", "hello world 2", "hello world 3", "hello world 4",
                                "HELLO WORLD 0", "HELLO WORLD 1", "HELLO WORLD 2", "HELLO WORLD 3", "HELLO WORLD 4")
                ),
                MixedParameterizedQueryTest(
                        function = MixedParameterizedQueryTests::endsWith,
                        parameters = arrayOf("world 4"),
                        results = arrayOf("hello world 4")
                ),
                MixedParameterizedQueryTest(
                        function = MixedParameterizedQueryTests::endsWithInsensitive,
                        parameters = arrayOf("wOrld 4"),
                        results = arrayOf("hello world 4",
                                "HELLO WORLD 4")
                ),
                MixedParameterizedQueryTest(
                        function = MixedParameterizedQueryTests::contains,
                        parameters = arrayOf("world"),
                        results = arrayOf("hello world 0", "hello world 1", "hello world 2", "hello world 3", "hello world 4")
                ),
                MixedParameterizedQueryTest(
                        function = MixedParameterizedQueryTests::containsInsensitive,
                        parameters = arrayOf("WorLD"),
                        results = arrayOf("hello world 0", "hello world 1", "hello world 2", "hello world 3", "hello world 4",
                                "HELLO WORLD 0", "HELLO WORLD 1", "HELLO WORLD 2", "HELLO WORLD 3", "HELLO WORLD 4")
                ),
                MixedParameterizedQueryTest(
                        function = MixedParameterizedQueryTests::like,
                        parameters = arrayOf("*w?rld*"),
                        results = arrayOf("hello world 0", "hello world 1", "hello world 2", "hello world 3", "hello world 4")
                ),
                MixedParameterizedQueryTest(
                        function = MixedParameterizedQueryTests::likeInsensitive,
                        parameters = arrayOf("*W?RLD*"),
                        results = arrayOf("hello world 0", "hello world 1", "hello world 2", "hello world 3", "hello world 4",
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
                MixedNotIndexed::class.java,
                PrimaryKeyAsString::class.java)

        realm = Realm.getInstance(realmConfiguration)

        initializeTestData()
    }

    @After
    fun tearDown() {
        realm.close()
    }

    @Test
    fun primitives() {
        this.test.execute(this, realm)
    }

    @Test
    fun mixed() {
        this.test.executeMixed(this, realm)
    }

    @Test
    fun mixed_nonManagedRealmModel() {
        this.test.executeMixed_nonManagedRealmModel(this, realm)
    }

    private fun validate(expected: Array<Mixed>, results: RealmResults<MixedNotIndexed>, expectedSize: Int) {
        assertEquals(expectedSize, results.size)

        expected.forEachIndexed { index, item ->
            val comparing = results[index]!!.mixed!!
            assertTrue(item.coercedEquals(comparing), "Values are not equal $item [vs] $comparing")
        }
    }

    private fun validateNotEqual(expected: Array<Mixed>, results: RealmResults<MixedNotIndexed>, expectedSize: Int) {
        assertEquals(expectedSize, results.size)

        expected.forEachIndexed { index, item ->
            val comparing = results[index]!!.mixed!!
            assertFalse(item.coercedEquals(comparing), "Values are equal $item [vs] $comparing")
        }
    }

    fun equalTo(expected: Array<Mixed>, expectedSize: Int, value: Any) {
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
            is Mixed -> query.equalTo(MixedNotIndexed.FIELD_MIXED, value)
            is RealmModel -> return
            else -> throw IllegalStateException("EqualTo for type ${value::class.qualifiedName} not implemented")
        }

        validate(expected, query.findAll()!!, expectedSize)
    }

    fun notEqualTo(expected: Array<Mixed>, expectedSize: Int, value: Any) {
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
            is Mixed -> query.notEqualTo(MixedNotIndexed.FIELD_MIXED, value)
            is RealmModel -> return
            else -> throw IllegalStateException("NotEqualTo for type ${value::class.qualifiedName} not implemented")
        }

        validateNotEqual(expected, query.findAll()!!, expectedSize)
    }

    fun equalToInsensitive(expected: Array<Mixed>, expectedSize: Int, value: Any) {
        val query: RealmQuery<MixedNotIndexed> = realm.where()

        when (value) {
            is String -> query.equalTo(MixedNotIndexed.FIELD_MIXED, value, Case.INSENSITIVE)
            is Mixed -> query.equalTo(MixedNotIndexed.FIELD_MIXED, value, Case.INSENSITIVE)
            else -> throw IllegalStateException("EqualTo[c] for type ${value::class.qualifiedName} not implemented")
        }

        validate(expected, query.findAll()!!, expectedSize)
    }

    fun notEqualToInsensitive(expected: Array<Mixed>, expectedSize: Int, value: Any) {
        val query: RealmQuery<MixedNotIndexed> = realm.where()

        when (value) {
            is String -> query.notEqualTo(MixedNotIndexed.FIELD_MIXED, value, Case.INSENSITIVE)
            is Mixed -> query.notEqualTo(MixedNotIndexed.FIELD_MIXED, value, Case.INSENSITIVE)
            else -> throw IllegalStateException("NotEqualTo[c] for type ${value::class.qualifiedName} not implemented")
        }

        validateNotEqual(expected, query.findAll()!!, expectedSize)
    }

    fun greaterThanOrEqualTo(expected: Array<Mixed>, expectedSize: Int, value: Any) {
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
            is Mixed -> query.greaterThanOrEqualTo(MixedNotIndexed.FIELD_MIXED, value)
            is Byte -> return
            is Short -> return
            else -> throw IllegalStateException("GreaterThanOrEqualTo for type ${value::class.qualifiedName} not implemented")
        }

        validate(expected, query.findAll()!!, expectedSize)
    }

    fun greaterThan(expected: Array<Mixed>, expectedSize: Int, value: Any) {
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
            is Mixed -> query.greaterThan(MixedNotIndexed.FIELD_MIXED, value)
            is Byte -> return
            is Short -> return
            else -> throw IllegalStateException("GreaterThan for type ${value::class.qualifiedName} not implemented")
        }

        validate(expected, query.findAll()!!, expectedSize)
    }

    fun lessThanOrEqualTo(expected: Array<Mixed>, expectedSize: Int, value: Any) {
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
            is Mixed -> query.lessThanOrEqualTo(MixedNotIndexed.FIELD_MIXED, value)
            is Byte -> return
            is Short -> return
            else -> throw IllegalStateException("LessThanOrEqualTo for type ${value::class.qualifiedName} not implemented")
        }

        validate(expected, query.findAll()!!, expectedSize)
    }

    fun lessThan(expected: Array<Mixed>, expectedSize: Int, value: Any) {
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
            is Mixed -> query.lessThan(MixedNotIndexed.FIELD_MIXED, value)
            is Byte -> return
            is Short -> return
            else -> throw IllegalStateException("LessThan for type ${value::class.qualifiedName} not implemented")
        }

        validate(expected, query.findAll()!!, expectedSize)
    }

    private inline fun <reified T> convertToType(value: Array<Any?>): Array<T> {
        return Array(value.size) { i ->
            value[i] as T
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun `in`(expected: Array<Mixed>, expectedSize: Int, value: Array<Any?>) {
        val query: RealmQuery<MixedNotIndexed> = realm.where()

        if (value.isNotEmpty()) {
            when (value[0]) {
                is Boolean -> query.`in`(MixedNotIndexed.FIELD_MIXED, convertToType<Boolean?>(value))
                is Byte -> query.`in`(MixedNotIndexed.FIELD_MIXED, convertToType<Byte?>(value))
                is Short -> query.`in`(MixedNotIndexed.FIELD_MIXED, convertToType<Short?>(value))
                is Int -> query.`in`(MixedNotIndexed.FIELD_MIXED, convertToType<Int?>(value))
                is Long -> query.`in`(MixedNotIndexed.FIELD_MIXED, convertToType<Long?>(value))
                is Float -> query.`in`(MixedNotIndexed.FIELD_MIXED, convertToType<Float?>(value))
                is Double -> query.`in`(MixedNotIndexed.FIELD_MIXED, convertToType<Double?>(value))
                is String -> query.`in`(MixedNotIndexed.FIELD_MIXED, convertToType<String?>(value))
                is Date -> query.`in`(MixedNotIndexed.FIELD_MIXED, convertToType<Date?>(value))
                is Mixed -> query.`in`(MixedNotIndexed.FIELD_MIXED, convertToType<Mixed?>(value))
                is Decimal128 -> return
                is ObjectId -> return
                is UUID -> return
                is ByteArray -> return
                is RealmModel -> return
                else -> throw IllegalStateException("In for type ${value::class.qualifiedName} not implemented")
            }
        }

        validate(expected, query.findAll()!!, expectedSize)
    }

    fun between(expected: Array<Mixed>, expectedSize: Int, value1: Any, value2: Any) {
        val query: RealmQuery<MixedNotIndexed> = realm.where()

        when (value1) {
            is Int -> query.between(MixedNotIndexed.FIELD_MIXED, value1, value2 as Int)
            is Long -> query.between(MixedNotIndexed.FIELD_MIXED, value1, value2 as Long)
            is Float -> query.between(MixedNotIndexed.FIELD_MIXED, value1, value2 as Float)
            is Double -> query.between(MixedNotIndexed.FIELD_MIXED, value1, value2 as Double)
            is Decimal128 -> query.between(MixedNotIndexed.FIELD_MIXED, value1, value2 as Decimal128)
            is Date -> query.between(MixedNotIndexed.FIELD_MIXED, value1, value2 as Date)
            is Mixed -> query.between(MixedNotIndexed.FIELD_MIXED, value1, value2 as Mixed)
            is Byte -> return
            is Short -> return
            is ObjectId -> return
            is UUID -> return
            else -> throw IllegalStateException("Between for type ${value1::class.qualifiedName} not implemented")
        }

        validate(expected, query.findAll()!!, expectedSize)
    }

    fun beginsWith(expected: Array<Mixed>, expectedSize: Int, value: Any) {
        val query: RealmQuery<MixedNotIndexed> = realm.where()

        when (value) {
            is String -> query.beginsWith(MixedNotIndexed.FIELD_MIXED, value)
            is Mixed -> query.beginsWith(MixedNotIndexed.FIELD_MIXED, value)
            else -> throw IllegalStateException("BeginsWith for type ${value::class.qualifiedName} not implemented")
        }

        validate(expected, query.findAll()!!, expectedSize)
    }

    fun beginsWithInsensitive(expected: Array<Mixed>, expectedSize: Int, value: Any) {
        val query: RealmQuery<MixedNotIndexed> = realm.where()

        when (value) {
            is String -> query.beginsWith(MixedNotIndexed.FIELD_MIXED, value, Case.INSENSITIVE)
            is Mixed -> query.beginsWith(MixedNotIndexed.FIELD_MIXED, value, Case.INSENSITIVE)
            else -> throw IllegalStateException("BeginsWith for type ${value::class.qualifiedName} not implemented")
        }

        validate(expected, query.findAll()!!, expectedSize)
    }

    fun endsWith(expected: Array<Mixed>, expectedSize: Int, value: Any) {
        val query: RealmQuery<MixedNotIndexed> = realm.where()

        when (value) {
            is String -> query.endsWith(MixedNotIndexed.FIELD_MIXED, value)
            is Mixed -> query.endsWith(MixedNotIndexed.FIELD_MIXED, value)
            else -> throw IllegalStateException("EndsWith for type ${value::class.qualifiedName} not implemented")
        }

        validate(expected, query.findAll()!!, expectedSize)
    }

    fun endsWithInsensitive(expected: Array<Mixed>, expectedSize: Int, value: Any) {
        val query: RealmQuery<MixedNotIndexed> = realm.where()

        when (value) {
            is String -> query.endsWith(MixedNotIndexed.FIELD_MIXED, value, Case.INSENSITIVE)
            is Mixed -> query.endsWith(MixedNotIndexed.FIELD_MIXED, value, Case.INSENSITIVE)
            else -> throw IllegalStateException("EndsWith for type ${value::class.qualifiedName} not implemented")
        }

        validate(expected, query.findAll()!!, expectedSize)
    }

    fun contains(expected: Array<Mixed>, expectedSize: Int, value: Any) {
        val query: RealmQuery<MixedNotIndexed> = realm.where()

        when (value) {
            is String -> query.contains(MixedNotIndexed.FIELD_MIXED, value)
            is Mixed -> query.contains(MixedNotIndexed.FIELD_MIXED, value)
            else -> throw IllegalStateException("Contains for type ${value::class.qualifiedName} not implemented")
        }

        validate(expected, query.findAll()!!, expectedSize)
    }

    fun containsInsensitive(expected: Array<Mixed>, expectedSize: Int, value: Any) {
        val query: RealmQuery<MixedNotIndexed> = realm.where()

        when (value) {
            is String -> query.contains(MixedNotIndexed.FIELD_MIXED, value, Case.INSENSITIVE)
            is Mixed -> query.contains(MixedNotIndexed.FIELD_MIXED, value, Case.INSENSITIVE)
            else -> throw IllegalStateException("Contains for type ${value::class.qualifiedName} not implemented")
        }

        validate(expected, query.findAll()!!, expectedSize)
    }

    fun like(expected: Array<Mixed>, expectedSize: Int, value: Any) {
        val query: RealmQuery<MixedNotIndexed> = realm.where()

        when (value) {
            is String -> query.like(MixedNotIndexed.FIELD_MIXED, value)
            is Mixed -> query.like(MixedNotIndexed.FIELD_MIXED, value)
            else -> throw IllegalStateException("Like for type ${value::class.qualifiedName} not implemented")
        }

        validate(expected, query.findAll()!!, expectedSize)
    }

    fun likeInsensitive(expected: Array<Mixed>, expectedSize: Int, value: Any) {
        val query: RealmQuery<MixedNotIndexed> = realm.where()

        when (value) {
            is String -> query.like(MixedNotIndexed.FIELD_MIXED, value, Case.INSENSITIVE)
            is Mixed -> query.like(MixedNotIndexed.FIELD_MIXED, value, Case.INSENSITIVE)
            else -> throw IllegalStateException("Like for type ${value::class.qualifiedName} not implemented")
        }

        validate(expected, query.findAll()!!, expectedSize)
    }
}
