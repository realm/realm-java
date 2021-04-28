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

package io.realm.realmany

import androidx.test.platform.app.InstrumentationRegistry
import io.realm.*
import io.realm.entities.RealmAnyNotIndexed
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

class RealmAnyParameterizedQueryTest(
        val filter: KFunction<*>,
        val arguments: Array<Any?>,
        val expectedResult: Array<Any?>,
        val expectedSize: Int? = null,
        val expandArguments: Boolean = true,
        val testWithUnmanagedObjects: Boolean = false
) {
    @Suppress("UNCHECKED_CAST")
    private fun asRealmAny(array: Array<Any?>, realm: Realm, copyToRealm: Boolean = true): Array<RealmAny> {
        val realmAnyArray = arrayOfNulls<RealmAny?>(array.size)

        realm.executeTransaction{
            array.forEachIndexed { index, value ->
                if (value == null)
                    realmAnyArray[index] = RealmAny.nullValue()
                else
                    when (value) {
                        is Boolean -> realmAnyArray[index] = RealmAny.valueOf(value)
                        is Byte -> realmAnyArray[index] = RealmAny.valueOf(value)
                        is Short -> realmAnyArray[index] = RealmAny.valueOf(value)
                        is Int -> realmAnyArray[index] = RealmAny.valueOf(value)
                        is Long -> realmAnyArray[index] = RealmAny.valueOf(value)
                        is Float -> realmAnyArray[index] = RealmAny.valueOf(value)
                        is Double -> realmAnyArray[index] = RealmAny.valueOf(value)
                        is String -> realmAnyArray[index] = RealmAny.valueOf(value)
                        is ByteArray -> realmAnyArray[index] = RealmAny.valueOf(value)
                        is Decimal128 -> realmAnyArray[index] = RealmAny.valueOf(value)
                        is ObjectId -> realmAnyArray[index] = RealmAny.valueOf(value)
                        is UUID -> realmAnyArray[index] = RealmAny.valueOf(value)
                        is Date -> realmAnyArray[index] = RealmAny.valueOf(value)
                        is RealmModel -> realmAnyArray[index] = RealmAny.valueOf(if (copyToRealm) realm.copyToRealmOrUpdate(value) else value)
                        else -> throw IllegalStateException("EqualsTo test for type ${value::class.qualifiedName} not implemented")
                    }
            }
        }

        return realmAnyArray as Array<RealmAny>
    }

    override fun toString(): String {
        if (arguments.isEmpty()) {
            return filter.javaMethod!!.name
        }

        val firstParameter = arguments[0]
        if (firstParameter is Array<*>) {
            return "${filter.javaMethod!!.name}:${firstParameter::class.java.componentType}"
        }

        return "${filter.javaMethod!!.name}:${arguments[0]!!::class.simpleName}"
    }

    fun execute(context: Any, realm: Realm) {
        if (expandArguments) {
            filter.call(context, asRealmAny(expectedResult, realm), expectedSize ?: expectedResult.size, *arguments)
        } else {
            filter.call(context, asRealmAny(expectedResult, realm), expectedSize ?: expectedResult.size, arguments)
        }
    }

    fun executeRealmAny(context: Any, realm: Realm) {
        if (expandArguments) {
            filter.call(context, asRealmAny(expectedResult, realm), expectedSize ?: expectedResult.size, *asRealmAny(arguments, realm))
        } else {
            filter.call(context, asRealmAny(expectedResult, realm), expectedSize ?: expectedResult.size, asRealmAny(arguments, realm))
        }
    }

    fun executeRealmAny_nonManagedRealmModel(context: Any, realm: Realm) {
        if(!testWithUnmanagedObjects){
            return
        }

        val exception = assertFailsWith<InvocationTargetException>("Unmanaged Realm objects are not valid query arguments"){
            if (expandArguments) {
                filter.call(context, asRealmAny(expectedResult, realm, false),
                        expectedSize ?: expectedResult.size, *asRealmAny(arguments, realm, false))
            } else {
                filter.call(context, asRealmAny(expectedResult, realm, false),
                        expectedSize ?: expectedResult.size, asRealmAny(arguments, realm, false))
            }
        }

        assertTrue(exception.cause is IllegalArgumentException)
    }
}

@RunWith(Parameterized::class)
class RealmAnyParameterizedQueryTests(val test: RealmAnyParameterizedQueryTest) {
    private lateinit var realmConfiguration: RealmConfiguration
    private lateinit var realm: Realm

    private fun initializeTestData() {
        val realmAnyValues = RealmAnyHelper.generateRealmAnyValues()

        realm.beginTransaction()

        for (value in realmAnyValues) {
            val realmAnyObject = RealmAnyNotIndexed(value)
            realm.insert(realmAnyObject)
        }

        realm.commitTransaction()
    }

    @get:Rule
    val configFactory = TestRealmConfigurationFactory()

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{index}:{0}")
        fun data(): List<RealmAnyParameterizedQueryTest> = listOf(
                // EQUALS TEST DEFINITIONS
                RealmAnyParameterizedQueryTest(
                        filter = RealmAnyParameterizedQueryTests::equalTo,
                        arguments = arrayOf(true),
                        expectedResult = arrayOf(true)
                ),
                RealmAnyParameterizedQueryTest(
                        filter = RealmAnyParameterizedQueryTests::equalTo,
                        arguments = arrayOf(4.toByte()),
                        expectedResult = arrayOf(4, 4, 4, 4, 4.0.toFloat(), 4.0, Decimal128(4))
                ),
                RealmAnyParameterizedQueryTest(
                        filter = RealmAnyParameterizedQueryTests::equalTo,
                        arguments = arrayOf(4.toShort()),
                        expectedResult = arrayOf(4, 4, 4, 4, 4.0.toFloat(), 4.0, Decimal128(4))
                ),
                RealmAnyParameterizedQueryTest(
                        filter = RealmAnyParameterizedQueryTests::equalTo,
                        arguments = arrayOf(4.toInt()),
                        expectedResult = arrayOf(4, 4, 4, 4, 4.0.toFloat(), 4.0, Decimal128(4))
                ),
                RealmAnyParameterizedQueryTest(
                        filter = RealmAnyParameterizedQueryTests::equalTo,
                        arguments = arrayOf(4.toLong()),
                        expectedResult = arrayOf(4, 4, 4, 4, 4.0.toFloat(), 4.0, Decimal128(4))
                ),
                RealmAnyParameterizedQueryTest(
                        filter = RealmAnyParameterizedQueryTests::equalTo,
                        arguments = arrayOf(4.toFloat()),
                        expectedResult = arrayOf(4, 4, 4, 4, 4.0.toFloat(), 4.0, Decimal128(4))
                ),
                RealmAnyParameterizedQueryTest(
                        filter = RealmAnyParameterizedQueryTests::equalTo,
                        arguments = arrayOf(4.toDouble()),
                        expectedResult = arrayOf(4, 4, 4, 4, 4.0.toFloat(), 4.0, Decimal128(4))
                ),
                RealmAnyParameterizedQueryTest(
                        filter = RealmAnyParameterizedQueryTests::equalTo,
                        arguments = arrayOf("hello world 1"),
                        expectedResult = arrayOf("hello world 1")
                ),
                RealmAnyParameterizedQueryTest(
                        filter = RealmAnyParameterizedQueryTests::equalTo,
                        arguments = arrayOf(byteArrayOf(0, 1, 0)),
                        expectedResult = arrayOf(byteArrayOf(0, 1, 0))
                ),
                RealmAnyParameterizedQueryTest(
                        filter = RealmAnyParameterizedQueryTests::equalTo,
                        arguments = arrayOf(Date(4)),
                        expectedResult = arrayOf(Date(4))
                ),
                RealmAnyParameterizedQueryTest(
                        filter = RealmAnyParameterizedQueryTests::equalTo,
                        arguments = arrayOf(Decimal128(4)),
                        expectedResult = arrayOf(4, 4, 4, 4, 4.0.toFloat(), 4.0, Decimal128(4))
                ),
                RealmAnyParameterizedQueryTest(
                        filter = RealmAnyParameterizedQueryTests::equalTo,
                        arguments = arrayOf(ObjectId(TestHelper.generateObjectIdHexString(0))),
                        expectedResult = arrayOf(ObjectId(TestHelper.generateObjectIdHexString(0)))
                ),
                RealmAnyParameterizedQueryTest(
                        filter = RealmAnyParameterizedQueryTests::equalTo,
                        arguments = arrayOf(UUID.fromString(TestHelper.generateUUIDString(0))),
                        expectedResult = arrayOf(UUID.fromString(TestHelper.generateUUIDString(0)))
                ),
                RealmAnyParameterizedQueryTest(
                        filter = RealmAnyParameterizedQueryTests::equalToInsensitive,
                        arguments = arrayOf("hello world 2"),
                        expectedResult = arrayOf("hello world 2", "HELLO WORLD 2")
                ),
                RealmAnyParameterizedQueryTest(
                        filter = RealmAnyParameterizedQueryTests::equalTo,
                        arguments = arrayOf(PrimaryKeyAsString("item 1")),
                        expectedResult = arrayOf(PrimaryKeyAsString("item 1")),
                        testWithUnmanagedObjects = true
                ),
                // NOT EQUALS TEST DEFINITIONS
                RealmAnyParameterizedQueryTest(
                        filter = RealmAnyParameterizedQueryTests::notEqualTo,
                        arguments = arrayOf(true),
                        expectedResult = arrayOf(false),
                        expectedSize = 111
                ),
                RealmAnyParameterizedQueryTest(
                        filter = RealmAnyParameterizedQueryTests::notEqualTo,
                        arguments = arrayOf(4.toByte()),
                        expectedResult = arrayOf(4.toByte()),
                        expectedSize = 105
                ),
                RealmAnyParameterizedQueryTest(
                        filter = RealmAnyParameterizedQueryTests::notEqualTo,
                        arguments = arrayOf(4.toShort()),
                        expectedResult = arrayOf(4.toShort()),
                        expectedSize = 105
                ),
                RealmAnyParameterizedQueryTest(
                        filter = RealmAnyParameterizedQueryTests::notEqualTo,
                        arguments = arrayOf(4.toInt()),
                        expectedResult = arrayOf(4.toInt()),
                        expectedSize = 105
                ),
                RealmAnyParameterizedQueryTest(
                        filter = RealmAnyParameterizedQueryTests::notEqualTo,
                        arguments = arrayOf(4.toLong()),
                        expectedResult = arrayOf(4.toLong()),
                        expectedSize = 105
                ),
                RealmAnyParameterizedQueryTest(
                        filter = RealmAnyParameterizedQueryTests::notEqualTo,
                        arguments = arrayOf(4.4.toFloat()),
                        expectedResult = arrayOf(4.4.toFloat()),
                        expectedSize = 111
                ),
                RealmAnyParameterizedQueryTest(
                        filter = RealmAnyParameterizedQueryTests::notEqualTo,
                        arguments = arrayOf(4.3),
                        expectedResult = arrayOf(4.3),
                        expectedSize = 111
                ),
                RealmAnyParameterizedQueryTest(
                        filter = RealmAnyParameterizedQueryTests::notEqualTo,
                        arguments = arrayOf("hello world 2"),
                        expectedResult = arrayOf("hello world 2"),
                        expectedSize = 111
                ),
                RealmAnyParameterizedQueryTest(
                        filter = RealmAnyParameterizedQueryTests::notEqualTo,
                        arguments = arrayOf(byteArrayOf(0, 1, 0)),
                        expectedResult = arrayOf(byteArrayOf(0, 1, 0)),
                        expectedSize = 111
                ),
                RealmAnyParameterizedQueryTest(
                        RealmAnyParameterizedQueryTests::notEqualTo,
                        arguments = arrayOf(Date(4)),
                        expectedResult = arrayOf(Date(4)),
                        expectedSize = 111
                ),
                RealmAnyParameterizedQueryTest(
                        filter = RealmAnyParameterizedQueryTests::notEqualTo,
                        arguments = arrayOf(Decimal128(4)),
                        expectedResult = arrayOf(Decimal128(4)),
                        expectedSize = 105
                ),
                RealmAnyParameterizedQueryTest(
                        filter = RealmAnyParameterizedQueryTests::notEqualTo,
                        arguments = arrayOf(ObjectId(TestHelper.generateObjectIdHexString(4))),
                        expectedResult = arrayOf(ObjectId(TestHelper.generateObjectIdHexString(4))),
                        expectedSize = 111
                ),
                RealmAnyParameterizedQueryTest(
                        filter = RealmAnyParameterizedQueryTests::notEqualTo,
                        arguments = arrayOf(UUID.fromString(TestHelper.generateUUIDString(4))),
                        expectedResult = arrayOf(UUID.fromString(TestHelper.generateUUIDString(4))),
                        expectedSize = 111
                ),
                RealmAnyParameterizedQueryTest(
                        filter = RealmAnyParameterizedQueryTests::notEqualToInsensitive,
                        arguments = arrayOf("HELLO WORLD 2"),
                        expectedResult = arrayOf("HELLO WORLD 2"),
                        expectedSize = 110
                ),
                RealmAnyParameterizedQueryTest(
                        filter = RealmAnyParameterizedQueryTests::notEqualTo,
                        arguments = arrayOf(PrimaryKeyAsString("item 1")),
                        expectedResult = arrayOf(PrimaryKeyAsString("item 1")),
                        expectedSize = 111,
                        testWithUnmanagedObjects = true
                ),
                // GREATER THAN TEST DEFINITIONS
                RealmAnyParameterizedQueryTest(
                        filter = RealmAnyParameterizedQueryTests::greaterThan,
                        arguments = arrayOf(5.toByte()),
                        expectedResult = arrayOf(6, 6, 6, 6, 7, 7, 7, 7, 8, 8, 8, 8, 9, 9, 9, 9)
                ),
                RealmAnyParameterizedQueryTest(
                        filter = RealmAnyParameterizedQueryTests::greaterThan,
                        arguments = arrayOf(5.toShort()),
                        expectedResult = arrayOf(6, 6, 6, 6, 7, 7, 7, 7, 8, 8, 8, 8, 9, 9, 9, 9)
                ),
                RealmAnyParameterizedQueryTest(
                        filter = RealmAnyParameterizedQueryTests::greaterThan,
                        arguments = arrayOf(5.toInt()),
                        expectedResult = arrayOf(6, 6, 6, 6, 7, 7, 7, 7, 8, 8, 8, 8, 9, 9, 9, 9)
                ),
                RealmAnyParameterizedQueryTest(
                        filter = RealmAnyParameterizedQueryTests::greaterThan,
                        arguments = arrayOf(5.toLong()),
                        expectedResult = arrayOf(6, 6, 6, 6, 7, 7, 7, 7, 8, 8, 8, 8, 9, 9, 9, 9)
                ),
                RealmAnyParameterizedQueryTest(
                        filter = RealmAnyParameterizedQueryTests::greaterThan,
                        arguments = arrayOf(4.5.toFloat()),
                        expectedResult = arrayOf(5, 5, 5, 5, 6, 6, 6, 6, 7, 7, 7, 7, 8, 8, 8, 8, 9, 9, 9, 9, 4.6.toFloat(), 4.7.toFloat(), 4.8.toFloat(), 4.9.toFloat(), 4.6, 4.7, 4.8, 4.9)
                ),
                RealmAnyParameterizedQueryTest(
                        filter = RealmAnyParameterizedQueryTests::greaterThan,
                        arguments = arrayOf(4.425),
                        expectedResult = arrayOf(5, 5, 5, 5, 6, 6, 6, 6, 7, 7, 7, 7, 8, 8, 8, 8, 9, 9, 9, 9, 4.5.toFloat(), 4.6.toFloat(), 4.7.toFloat(), 4.8.toFloat(), 4.9.toFloat(), 4.5, 4.6, 4.7, 4.8, 4.9)
                ),
                RealmAnyParameterizedQueryTest(
                        filter = RealmAnyParameterizedQueryTests::greaterThan,
                        arguments = arrayOf(Date(2)),
                        expectedResult = arrayOf(Date(3), Date(4))
                ),
                RealmAnyParameterizedQueryTest(
                        filter = RealmAnyParameterizedQueryTests::greaterThan,
                        arguments = arrayOf(Decimal128(3)),
                        expectedResult = arrayOf(4, 4, 4, 4, 5, 5, 5, 5, 6, 6, 6, 6, 7, 7, 7, 7, 8, 8, 8, 8, 9, 9, 9, 9, 4.0.toFloat(), 4.1.toFloat(), 4.2.toFloat(), 4.3.toFloat(), 4.4.toFloat(), 4.5.toFloat(), 4.6.toFloat(), 4.7.toFloat(), 4.8.toFloat(), 4.9.toFloat(), 4.0, 4.1, 4.2, 4.3, 4.4, 4.5, 4.6, 4.7, 4.8, 4.9, Decimal128(4))
                ),
                RealmAnyParameterizedQueryTest(
                        filter = RealmAnyParameterizedQueryTests::greaterThan,
                        arguments = arrayOf(ObjectId(TestHelper.generateObjectIdHexString(1))),
                        expectedResult = arrayOf(ObjectId(TestHelper.generateObjectIdHexString(2)), ObjectId(TestHelper.generateObjectIdHexString(3)), ObjectId(TestHelper.generateObjectIdHexString(4)))
                ),
                RealmAnyParameterizedQueryTest(
                        filter = RealmAnyParameterizedQueryTests::greaterThan,
                        arguments = arrayOf(UUID.fromString(TestHelper.generateUUIDString(1))),
                        expectedResult = arrayOf(UUID.fromString(TestHelper.generateUUIDString(2)), UUID.fromString(TestHelper.generateUUIDString(3)), UUID.fromString(TestHelper.generateUUIDString(4)))
                ),
                // GREATER THAN OR EQUALS TEST DEFINITIONS
                RealmAnyParameterizedQueryTest(
                        filter = RealmAnyParameterizedQueryTests::greaterThanOrEqualTo,
                        arguments = arrayOf(5.toByte()),
                        expectedResult = arrayOf(5, 5, 5, 5, 6, 6, 6, 6, 7, 7, 7, 7, 8, 8, 8, 8, 9, 9, 9, 9)
                ),
                RealmAnyParameterizedQueryTest(
                        filter = RealmAnyParameterizedQueryTests::greaterThanOrEqualTo,
                        arguments = arrayOf(5.toShort()),
                        expectedResult = arrayOf(5, 5, 5, 5, 6, 6, 6, 6, 7, 7, 7, 7, 8, 8, 8, 8, 9, 9, 9, 9)
                ),
                RealmAnyParameterizedQueryTest(
                        filter = RealmAnyParameterizedQueryTests::greaterThanOrEqualTo,
                        arguments = arrayOf(5.toInt()),
                        expectedResult = arrayOf(5, 5, 5, 5, 6, 6, 6, 6, 7, 7, 7, 7, 8, 8, 8, 8, 9, 9, 9, 9)
                ),
                RealmAnyParameterizedQueryTest(
                        filter = RealmAnyParameterizedQueryTests::greaterThanOrEqualTo,
                        arguments = arrayOf(5.toLong()),
                        expectedResult = arrayOf(5, 5, 5, 5, 6, 6, 6, 6, 7, 7, 7, 7, 8, 8, 8, 8, 9, 9, 9, 9)
                ),
                RealmAnyParameterizedQueryTest(
                        filter = RealmAnyParameterizedQueryTests::greaterThanOrEqualTo,
                        arguments = arrayOf(4.5.toFloat()),
                        expectedResult = arrayOf(5, 5, 5, 5, 6, 6, 6, 6, 7, 7, 7, 7, 8, 8, 8, 8, 9, 9, 9, 9, 4.5.toFloat(), 4.6.toFloat(), 4.7.toFloat(), 4.8.toFloat(), 4.9.toFloat(), 4.5, 4.6, 4.7, 4.8, 4.9)
                ),
                RealmAnyParameterizedQueryTest(
                        filter = RealmAnyParameterizedQueryTests::greaterThanOrEqualTo,
                        arguments = arrayOf(4.4),
                        expectedResult = arrayOf(5, 5, 5, 5, 6, 6, 6, 6, 7, 7, 7, 7, 8, 8, 8, 8, 9, 9, 9, 9, 4.4.toFloat(), 4.5.toFloat(), 4.6.toFloat(), 4.7.toFloat(), 4.8.toFloat(), 4.9.toFloat(), 4.4, 4.5, 4.6, 4.7, 4.8, 4.9)
                ),
                RealmAnyParameterizedQueryTest(
                        filter = RealmAnyParameterizedQueryTests::greaterThanOrEqualTo,
                        arguments = arrayOf(Date(2)),
                        expectedResult = arrayOf(Date(2), Date(3), Date(4))
                ),
                RealmAnyParameterizedQueryTest(
                        filter = RealmAnyParameterizedQueryTests::greaterThanOrEqualTo,
                        arguments = arrayOf(Decimal128(3)),
                        expectedResult = arrayOf(3, 3, 3, 3, 4, 4, 4, 4, 5, 5, 5, 5, 6, 6, 6, 6, 7, 7, 7, 7, 8, 8, 8, 8, 9, 9, 9, 9, 4.0.toFloat(), 4.1.toFloat(), 4.2.toFloat(), 4.3.toFloat(), 4.4.toFloat(), 4.5.toFloat(), 4.6.toFloat(), 4.7.toFloat(), 4.8.toFloat(), 4.9.toFloat(), 4.0, 4.1, 4.2, 4.3, 4.4, 4.5, 4.6, 4.7, 4.8, 4.9, Decimal128(3), Decimal128(4))
                ),
                RealmAnyParameterizedQueryTest(
                        filter = RealmAnyParameterizedQueryTests::greaterThanOrEqualTo,
                        arguments = arrayOf(ObjectId(TestHelper.generateObjectIdHexString(1))),
                        expectedResult = arrayOf(ObjectId(TestHelper.generateObjectIdHexString(1)), ObjectId(TestHelper.generateObjectIdHexString(2)), ObjectId(TestHelper.generateObjectIdHexString(3)), ObjectId(TestHelper.generateObjectIdHexString(4)))
                ),
                RealmAnyParameterizedQueryTest(
                        filter = RealmAnyParameterizedQueryTests::greaterThanOrEqualTo,
                        arguments = arrayOf(UUID.fromString(TestHelper.generateUUIDString(1))),
                        expectedResult = arrayOf(UUID.fromString(TestHelper.generateUUIDString(1)), UUID.fromString(TestHelper.generateUUIDString(2)), UUID.fromString(TestHelper.generateUUIDString(3)), UUID.fromString(TestHelper.generateUUIDString(4)))
                ),
                // LESS THAN TEST DEFINITIONS
                RealmAnyParameterizedQueryTest(
                        filter = RealmAnyParameterizedQueryTests::lessThan,
                        arguments = arrayOf(4.toByte()),
                        expectedResult = arrayOf(0, 0, 0, 0, 1, 1, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3, Decimal128(0), Decimal128(1), Decimal128(2), Decimal128(3))
                ),
                RealmAnyParameterizedQueryTest(
                        filter = RealmAnyParameterizedQueryTests::lessThan,
                        arguments = arrayOf(4.toShort()),
                        expectedResult = arrayOf(0, 0, 0, 0, 1, 1, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3, Decimal128(0), Decimal128(1), Decimal128(2), Decimal128(3))
                ),
                RealmAnyParameterizedQueryTest(
                        filter = RealmAnyParameterizedQueryTests::lessThan,
                        arguments = arrayOf(4.toInt()),
                        expectedResult = arrayOf(0, 0, 0, 0, 1, 1, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3, Decimal128(0), Decimal128(1), Decimal128(2), Decimal128(3))
                ),
                RealmAnyParameterizedQueryTest(
                        filter = RealmAnyParameterizedQueryTests::lessThan,
                        arguments = arrayOf(4.toLong()),
                        expectedResult = arrayOf(0, 0, 0, 0, 1, 1, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3, Decimal128(0), Decimal128(1), Decimal128(2), Decimal128(3))
                ),
                RealmAnyParameterizedQueryTest(
                        filter = RealmAnyParameterizedQueryTests::lessThan,
                        arguments = arrayOf(4.5.toFloat()),
                        expectedResult = arrayOf(0, 0, 0, 0, 1, 1, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3, 4, 4, 4, 4, 4.0.toFloat(), 4.1.toFloat(), 4.2.toFloat(), 4.3.toFloat(), 4.4.toFloat(), 4.0, 4.1, 4.2, 4.3, 4.4, Decimal128(0), Decimal128(1), Decimal128(2), Decimal128(3), Decimal128(4))
                ),
                RealmAnyParameterizedQueryTest(
                        filter = RealmAnyParameterizedQueryTests::lessThan,
                        arguments = arrayOf(4.3),
                        expectedResult = arrayOf(0, 0, 0, 0, 1, 1, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3, 4, 4, 4, 4, 4.0.toFloat(), 4.1.toFloat(), 4.2.toFloat(), 4.0, 4.1, 4.2, Decimal128(0), Decimal128(1), Decimal128(2), Decimal128(3), Decimal128(4))
                ),
                RealmAnyParameterizedQueryTest(
                        filter = RealmAnyParameterizedQueryTests::lessThan,
                        arguments = arrayOf(Date(3)),
                        expectedResult = arrayOf(Date(0), Date(1), Date(2))
                ),
                RealmAnyParameterizedQueryTest(
                        filter = RealmAnyParameterizedQueryTests::lessThan,
                        arguments = arrayOf(Decimal128(3)),
                        expectedResult = arrayOf(0, 0, 0, 0, 1, 1, 1, 1, 2, 2, 2, 2, Decimal128(0), Decimal128(1), Decimal128(2))
                ),
                RealmAnyParameterizedQueryTest(
                        filter = RealmAnyParameterizedQueryTests::lessThan,
                        arguments = arrayOf(ObjectId(TestHelper.generateObjectIdHexString(3))),
                        expectedResult = arrayOf(ObjectId(TestHelper.generateObjectIdHexString(0)), ObjectId(TestHelper.generateObjectIdHexString(1)), ObjectId(TestHelper.generateObjectIdHexString(2)))
                ),
                RealmAnyParameterizedQueryTest(
                        filter = RealmAnyParameterizedQueryTests::lessThan,
                        arguments = arrayOf(UUID.fromString(TestHelper.generateUUIDString(3))),
                        expectedResult = arrayOf(UUID.fromString(TestHelper.generateUUIDString(0)), UUID.fromString(TestHelper.generateUUIDString(1)), UUID.fromString(TestHelper.generateUUIDString(2)))
                ),
                // LESS THAN OR EQUALS TEST DEFINITIONS
                RealmAnyParameterizedQueryTest(
                        filter = RealmAnyParameterizedQueryTests::lessThanOrEqualTo,
                        arguments = arrayOf(4.toByte()),
                        expectedResult = arrayOf(0, 0, 0, 0, 1, 1, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3, 4, 4, 4, 4, 4.0.toFloat(), 4.0, Decimal128(0), Decimal128(1), Decimal128(2), Decimal128(3), Decimal128(4))
                ),
                RealmAnyParameterizedQueryTest(
                        filter = RealmAnyParameterizedQueryTests::lessThanOrEqualTo,
                        arguments = arrayOf(4.toShort()),
                        expectedResult = arrayOf(0, 0, 0, 0, 1, 1, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3, 4, 4, 4, 4, 4.0.toFloat(), 4.0, Decimal128(0), Decimal128(1), Decimal128(2), Decimal128(3), Decimal128(4))
                ),
                RealmAnyParameterizedQueryTest(
                        filter = RealmAnyParameterizedQueryTests::lessThanOrEqualTo,
                        arguments = arrayOf(4.toInt()),
                        expectedResult = arrayOf(0, 0, 0, 0, 1, 1, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3, 4, 4, 4, 4, 4.0.toFloat(), 4.0, Decimal128(0), Decimal128(1), Decimal128(2), Decimal128(3), Decimal128(4))
                ),
                RealmAnyParameterizedQueryTest(
                        filter = RealmAnyParameterizedQueryTests::lessThanOrEqualTo,
                        arguments = arrayOf(4.toLong()),
                        expectedResult = arrayOf(0, 0, 0, 0, 1, 1, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3, 4, 4, 4, 4, 4.0.toFloat(), 4.0, Decimal128(0), Decimal128(1), Decimal128(2), Decimal128(3), Decimal128(4))
                ),
                RealmAnyParameterizedQueryTest(
                        filter = RealmAnyParameterizedQueryTests::lessThanOrEqualTo,
                        arguments = arrayOf(4.5.toFloat()),
                        expectedResult = arrayOf(0, 0, 0, 0, 1, 1, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3, 4, 4, 4, 4, 4.0.toFloat(), 4.1.toFloat(), 4.2.toFloat(), 4.3.toFloat(), 4.4.toFloat(), 4.5.toFloat(), 4.0, 4.1, 4.2, 4.3, 4.4, 4.5, Decimal128(0), Decimal128(1), Decimal128(2), Decimal128(3), Decimal128(4))
                ),
                RealmAnyParameterizedQueryTest(
                        filter = RealmAnyParameterizedQueryTests::lessThanOrEqualTo,
                        arguments = arrayOf(4.325),
                        expectedResult = arrayOf(0, 0, 0, 0, 1, 1, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3, 4, 4, 4, 4, 4.0.toFloat(), 4.1.toFloat(), 4.2.toFloat(), 4.3.toFloat(), 4.0, 4.1, 4.2, 4.3, Decimal128(0), Decimal128(1), Decimal128(2), Decimal128(3), Decimal128(4))
                ),
                RealmAnyParameterizedQueryTest(
                        filter = RealmAnyParameterizedQueryTests::lessThanOrEqualTo,
                        arguments = arrayOf(Date(3)),
                        expectedResult = arrayOf(Date(0), Date(1), Date(2), Date(3))
                ),
                RealmAnyParameterizedQueryTest(
                        filter = RealmAnyParameterizedQueryTests::lessThanOrEqualTo,
                        arguments = arrayOf(Decimal128(3)),
                        expectedResult = arrayOf(0, 0, 0, 0, 1, 1, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3, Decimal128(0), Decimal128(1), Decimal128(2), Decimal128(3))
                ),
                RealmAnyParameterizedQueryTest(
                        filter = RealmAnyParameterizedQueryTests::lessThanOrEqualTo,
                        arguments = arrayOf(ObjectId(TestHelper.generateObjectIdHexString(3))),
                        expectedResult = arrayOf(ObjectId(TestHelper.generateObjectIdHexString(0)), ObjectId(TestHelper.generateObjectIdHexString(1)), ObjectId(TestHelper.generateObjectIdHexString(2)), ObjectId(TestHelper.generateObjectIdHexString(3)))
                ),
                RealmAnyParameterizedQueryTest(
                        filter = RealmAnyParameterizedQueryTests::lessThanOrEqualTo,
                        arguments = arrayOf(UUID.fromString(TestHelper.generateUUIDString(3))),
                        expectedResult = arrayOf(UUID.fromString(TestHelper.generateUUIDString(0)), UUID.fromString(TestHelper.generateUUIDString(1)), UUID.fromString(TestHelper.generateUUIDString(2)), UUID.fromString(TestHelper.generateUUIDString(3)))
                ),
                // IN TEST DEFINITIONS
                RealmAnyParameterizedQueryTest(
                        filter = RealmAnyParameterizedQueryTests::`in`,
                        arguments = arrayOf(true, null),
                        expectedResult = arrayOf(true, *arrayOfNulls(9)),
                        expandArguments = false
                ),
                RealmAnyParameterizedQueryTest(
                        filter = RealmAnyParameterizedQueryTests::`in`,
                        arguments = arrayOf(4.toByte(), 2.toByte(), 5.toByte(), 22.toByte(), null),
                        expectedResult = arrayOf(2, 2, 2, 2, 4, 4, 4, 4, 5, 5, 5, 5, 4.0.toFloat(), 4.0, Decimal128(2), Decimal128(4), *arrayOfNulls(9)),
                        expandArguments = false
                ),
                RealmAnyParameterizedQueryTest(
                        filter = RealmAnyParameterizedQueryTests::`in`,
                        arguments = arrayOf(4.toShort(), 2.toShort(), 5.toShort(), 22.toShort(), null),
                        expectedResult = arrayOf(2, 2, 2, 2, 4, 4, 4, 4, 5, 5, 5, 5, 4.0.toFloat(), 4.0, Decimal128(2), Decimal128(4), *arrayOfNulls(9)),
                        expandArguments = false
                ),
                RealmAnyParameterizedQueryTest(
                        filter = RealmAnyParameterizedQueryTests::`in`,
                        arguments = arrayOf(4.toInt(), 2.toInt(), 5.toInt(), 22.toInt(), null),
                        expectedResult = arrayOf(2, 2, 2, 2, 4, 4, 4, 4, 5, 5, 5, 5, 4.0.toFloat(), 4.0, Decimal128(2), Decimal128(4), *arrayOfNulls(9)),
                        expandArguments = false
                ),
                RealmAnyParameterizedQueryTest(
                        filter = RealmAnyParameterizedQueryTests::`in`,
                        arguments = arrayOf(4.toLong(), 2.toLong(), 5.toLong(), 22.toLong(), null),
                        expectedResult = arrayOf(2, 2, 2, 2, 4, 4, 4, 4, 5, 5, 5, 5, 4.0.toFloat(), 4.0, Decimal128(2), Decimal128(4), *arrayOfNulls(9)),
                        expandArguments = false
                ),
                RealmAnyParameterizedQueryTest(
                        filter = RealmAnyParameterizedQueryTests::`in`,
                        arguments = arrayOf(4.8.toFloat(), 8.1.toFloat(), 4.3.toFloat(), 4.0.toFloat(), 4.7.toFloat(), null),
                        expectedResult = arrayOf(4, 4, 4, 4, 4.0.toFloat(), 4.3.toFloat(), 4.7.toFloat(), 4.8.toFloat(), 4.0, Decimal128(4), *arrayOfNulls(9)),
                        expandArguments = false
                ),
                RealmAnyParameterizedQueryTest(
                        filter = RealmAnyParameterizedQueryTests::`in`,
                        arguments = arrayOf(4.8, 8.1, 4.3, 4.0, 4.7, null),
                        expectedResult = arrayOf(4, 4, 4, 4, 4.0, 4.0, 4.3, 4.7, 4.8, Decimal128(4), *arrayOfNulls(9)),
                        expandArguments = false
                ),
                RealmAnyParameterizedQueryTest(
                        filter = RealmAnyParameterizedQueryTests::`in`,
                        arguments = arrayOf("hello world 3", "hello world 0", "hello world 4", "realm rocks", null),
                        expectedResult = arrayOf("hello world 0", "hello world 3", "hello world 4", *arrayOfNulls(9)),
                        expandArguments = false
                ),
                RealmAnyParameterizedQueryTest(
                        filter = RealmAnyParameterizedQueryTests::`in`,
                        arguments = arrayOf(
                                byteArrayOf(0, 0, 0),
                                byteArrayOf(0, 1, 1),
                                byteArrayOf(1, 1, 0, 0),
                                byteArrayOf(1, 1, 1),
                                null
                        ),
                        expectedResult = arrayOf(
                                byteArrayOf(0, 0, 0),
                                byteArrayOf(0, 1, 1),
                                byteArrayOf(1, 1, 1),
                                *arrayOfNulls(9)
                        ),
                        expandArguments = false
                ),
                RealmAnyParameterizedQueryTest(
                        filter = RealmAnyParameterizedQueryTests::`in`,
                        arguments = arrayOf(Date(100), Date(3), null, Date(1)),
                        expectedResult = arrayOf(Date(1), Date(3), *arrayOfNulls(9)),
                        expandArguments = false
                ),
                RealmAnyParameterizedQueryTest(
                        filter = RealmAnyParameterizedQueryTests::`in`,
                        arguments = arrayOf(Decimal128(100), null, Decimal128(3), Decimal128(2)),
                        expectedResult = arrayOf(2, 2, 2, 2, 3, 3, 3, 3, Decimal128(2), Decimal128(3), *arrayOfNulls(9)),
                        expandArguments = false
                ),
                RealmAnyParameterizedQueryTest(
                        filter = RealmAnyParameterizedQueryTests::`in`,
                        arguments = arrayOf(ObjectId(TestHelper.generateObjectIdHexString(3)), null, ObjectId(TestHelper.generateObjectIdHexString(9)), ObjectId(TestHelper.generateObjectIdHexString(1))),
                        expectedResult = arrayOf(ObjectId(TestHelper.generateObjectIdHexString(1)), ObjectId(TestHelper.generateObjectIdHexString(3)), *arrayOfNulls(9)),
                        expandArguments = false
                ),
                RealmAnyParameterizedQueryTest(
                        filter = RealmAnyParameterizedQueryTests::`in`,
                        arguments = arrayOf(UUID.fromString(TestHelper.generateUUIDString(3)), null, UUID.fromString(TestHelper.generateUUIDString(9)), UUID.fromString(TestHelper.generateUUIDString(1))),
                        expectedResult = arrayOf(UUID.fromString(TestHelper.generateUUIDString(1)), UUID.fromString(TestHelper.generateUUIDString(3)), *arrayOfNulls(9)),
                        expandArguments = false
                ),
                RealmAnyParameterizedQueryTest(
                        filter = RealmAnyParameterizedQueryTests::`in`,
                        arguments = arrayOf(PrimaryKeyAsString("item 1"), PrimaryKeyAsString("item 3")),
                        expectedResult = arrayOf(PrimaryKeyAsString("item 1"), PrimaryKeyAsString("item 3")),
                        expandArguments = false
                ),
                RealmAnyParameterizedQueryTest(
                        filter = RealmAnyParameterizedQueryTests::`in`,
                        arguments = arrayOf(PrimaryKeyAsString("item 1"), PrimaryKeyAsString("item 3")),
                        expectedResult = arrayOf(PrimaryKeyAsString("item 1"), PrimaryKeyAsString("item 3")),
                        expandArguments = false,
                        testWithUnmanagedObjects = true
                ),
                // BETWEEN TEST DEFINITIONS
                RealmAnyParameterizedQueryTest(
                        filter = RealmAnyParameterizedQueryTests::between,
                        arguments = arrayOf(6.toByte(), 8.toByte()),
                        expectedResult = arrayOf(6, 6, 6, 6, 7, 7, 7, 7, 8, 8, 8, 8)
                ),
                RealmAnyParameterizedQueryTest(
                        filter = RealmAnyParameterizedQueryTests::between,
                        arguments = arrayOf(6.toShort(), 8.toShort()),
                        expectedResult = arrayOf(6, 6, 6, 6, 7, 7, 7, 7, 8, 8, 8, 8)
                ),
                RealmAnyParameterizedQueryTest(
                        filter = RealmAnyParameterizedQueryTests::between,
                        arguments = arrayOf(6.toInt(), 8.toInt()),
                        expectedResult = arrayOf(6, 6, 6, 6, 7, 7, 7, 7, 8, 8, 8, 8)
                ),
                RealmAnyParameterizedQueryTest(
                        filter = RealmAnyParameterizedQueryTests::between,
                        arguments = arrayOf(6.toLong(), 8.toLong()),
                        expectedResult = arrayOf(6, 6, 6, 6, 7, 7, 7, 7, 8, 8, 8, 8)
                ),
                RealmAnyParameterizedQueryTest(
                        filter = RealmAnyParameterizedQueryTests::between,
                        arguments = arrayOf(4.51.toFloat(), 4.89.toFloat()),
                        expectedResult = arrayOf(4.6.toFloat(), 4.7.toFloat(), 4.8.toFloat(), 4.6, 4.7, 4.8)
                ),
                RealmAnyParameterizedQueryTest(
                        filter = RealmAnyParameterizedQueryTests::between,
                        arguments = arrayOf(4.49, 4.89),
                        expectedResult = arrayOf(4.5.toFloat(), 4.6.toFloat(), 4.7.toFloat(), 4.8.toFloat(), 4.5, 4.6, 4.7, 4.8)
                ),
                RealmAnyParameterizedQueryTest(
                        filter = RealmAnyParameterizedQueryTests::between,
                        arguments = arrayOf(Date(2), Date(4)),
                        expectedResult = arrayOf(Date(2), Date(3), Date(4))
                ),
                RealmAnyParameterizedQueryTest(
                        filter = RealmAnyParameterizedQueryTests::between,
                        arguments = arrayOf(Decimal128(2), Decimal128(4)),
                        expectedResult = arrayOf(2, 2, 2, 2, 3, 3, 3, 3, 4, 4, 4, 4, 4.0.toFloat(), 4.0, Decimal128(2), Decimal128(3), Decimal128(4))
                ),
                RealmAnyParameterizedQueryTest(
                        filter = RealmAnyParameterizedQueryTests::between,
                        arguments = arrayOf(ObjectId(TestHelper.generateObjectIdHexString(2)), ObjectId(TestHelper.generateObjectIdHexString(4))),
                        expectedResult = arrayOf(ObjectId(TestHelper.generateObjectIdHexString(2)), ObjectId(TestHelper.generateObjectIdHexString(3)), ObjectId(TestHelper.generateObjectIdHexString(4)))
                ),
                RealmAnyParameterizedQueryTest(
                        filter = RealmAnyParameterizedQueryTests::between,
                        arguments = arrayOf(UUID.fromString(TestHelper.generateUUIDString(2)), UUID.fromString(TestHelper.generateUUIDString(4))),
                        expectedResult = arrayOf(UUID.fromString(TestHelper.generateUUIDString(2)), UUID.fromString(TestHelper.generateUUIDString(3)), UUID.fromString(TestHelper.generateUUIDString(4)))
                ),
                RealmAnyParameterizedQueryTest(
                        filter = RealmAnyParameterizedQueryTests::beginsWith,
                        arguments = arrayOf("hello"),
                        expectedResult = arrayOf("hello world 0", "hello world 1", "hello world 2", "hello world 3", "hello world 4")
                ),
                RealmAnyParameterizedQueryTest(
                        filter = RealmAnyParameterizedQueryTests::beginsWithInsensitive,
                        arguments = arrayOf("hELlo"),
                        expectedResult = arrayOf("hello world 0", "hello world 1", "hello world 2", "hello world 3", "hello world 4",
                                "HELLO WORLD 0", "HELLO WORLD 1", "HELLO WORLD 2", "HELLO WORLD 3", "HELLO WORLD 4")
                ),
                RealmAnyParameterizedQueryTest(
                        filter = RealmAnyParameterizedQueryTests::endsWith,
                        arguments = arrayOf("world 4"),
                        expectedResult = arrayOf("hello world 4")
                ),
                RealmAnyParameterizedQueryTest(
                        filter = RealmAnyParameterizedQueryTests::endsWithInsensitive,
                        arguments = arrayOf("wOrld 4"),
                        expectedResult = arrayOf("hello world 4",
                                "HELLO WORLD 4")
                ),
                RealmAnyParameterizedQueryTest(
                        filter = RealmAnyParameterizedQueryTests::contains,
                        arguments = arrayOf("world"),
                        expectedResult = arrayOf("hello world 0", "hello world 1", "hello world 2", "hello world 3", "hello world 4")
                ),
                RealmAnyParameterizedQueryTest(
                        filter = RealmAnyParameterizedQueryTests::containsInsensitive,
                        arguments = arrayOf("WorLD"),
                        expectedResult = arrayOf("hello world 0", "hello world 1", "hello world 2", "hello world 3", "hello world 4",
                                "HELLO WORLD 0", "HELLO WORLD 1", "HELLO WORLD 2", "HELLO WORLD 3", "HELLO WORLD 4")
                ),
                RealmAnyParameterizedQueryTest(
                        filter = RealmAnyParameterizedQueryTests::like,
                        arguments = arrayOf("*w?rld*"),
                        expectedResult = arrayOf("hello world 0", "hello world 1", "hello world 2", "hello world 3", "hello world 4")
                ),
                RealmAnyParameterizedQueryTest(
                        filter = RealmAnyParameterizedQueryTests::likeInsensitive,
                        arguments = arrayOf("*W?RLD*"),
                        expectedResult = arrayOf("hello world 0", "hello world 1", "hello world 2", "hello world 3", "hello world 4",
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
                RealmAnyNotIndexed::class.java,
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
    fun realmAny() {
        this.test.executeRealmAny(this, realm)
    }

    @Test
    fun realmAny_nonManagedRealmModel() {
        this.test.executeRealmAny_nonManagedRealmModel(this, realm)
    }

    private fun validate(expected: Array<RealmAny>, results: RealmResults<RealmAnyNotIndexed>, expectedSize: Int) {
        assertEquals(expectedSize, results.size)

        expected.forEachIndexed { index, item ->
            val comparing = results[index]!!.realmAny!!
            assertTrue(item.coercedEquals(comparing), "Values are not equal $item [vs] $comparing")
        }
    }

    private fun validateNotEqual(expected: Array<RealmAny>, results: RealmResults<RealmAnyNotIndexed>, expectedSize: Int) {
        assertEquals(expectedSize, results.size)

        expected.forEachIndexed { index, item ->
            val comparing = results[index]!!.realmAny!!
            assertFalse(item.coercedEquals(comparing), "Values are equal $item [vs] $comparing")
        }
    }

    fun equalTo(expected: Array<RealmAny>, expectedSize: Int, value: Any) {
        val query: RealmQuery<RealmAnyNotIndexed> = realm.where()

        when (value) {
            is Boolean -> query.equalTo(RealmAnyNotIndexed.FIELD_MIXED, value)
            is Byte -> query.equalTo(RealmAnyNotIndexed.FIELD_MIXED, value)
            is Short -> query.equalTo(RealmAnyNotIndexed.FIELD_MIXED, value)
            is Int -> query.equalTo(RealmAnyNotIndexed.FIELD_MIXED, value)
            is Long -> query.equalTo(RealmAnyNotIndexed.FIELD_MIXED, value)
            is Float -> query.equalTo(RealmAnyNotIndexed.FIELD_MIXED, value)
            is Double -> query.equalTo(RealmAnyNotIndexed.FIELD_MIXED, value)
            is String -> query.equalTo(RealmAnyNotIndexed.FIELD_MIXED, value)
            is ByteArray -> query.equalTo(RealmAnyNotIndexed.FIELD_MIXED, value)
            is Decimal128 -> query.equalTo(RealmAnyNotIndexed.FIELD_MIXED, value)
            is ObjectId -> query.equalTo(RealmAnyNotIndexed.FIELD_MIXED, value)
            is UUID -> query.equalTo(RealmAnyNotIndexed.FIELD_MIXED, value)
            is Date -> query.equalTo(RealmAnyNotIndexed.FIELD_MIXED, value)
            is RealmAny -> query.equalTo(RealmAnyNotIndexed.FIELD_MIXED, value)
            is RealmModel -> return
            else -> throw IllegalStateException("EqualTo for type ${value::class.qualifiedName} not implemented")
        }

        validate(expected, query.findAll()!!, expectedSize)
    }

    fun notEqualTo(expected: Array<RealmAny>, expectedSize: Int, value: Any) {
        val query: RealmQuery<RealmAnyNotIndexed> = realm.where()

        when (value) {
            is Boolean -> query.notEqualTo(RealmAnyNotIndexed.FIELD_MIXED, value)
            is Byte -> query.notEqualTo(RealmAnyNotIndexed.FIELD_MIXED, value)
            is Short -> query.notEqualTo(RealmAnyNotIndexed.FIELD_MIXED, value)
            is Int -> query.notEqualTo(RealmAnyNotIndexed.FIELD_MIXED, value)
            is Long -> query.notEqualTo(RealmAnyNotIndexed.FIELD_MIXED, value)
            is Float -> query.notEqualTo(RealmAnyNotIndexed.FIELD_MIXED, value)
            is Double -> query.notEqualTo(RealmAnyNotIndexed.FIELD_MIXED, value)
            is String -> query.notEqualTo(RealmAnyNotIndexed.FIELD_MIXED, value)
            is ByteArray -> query.notEqualTo(RealmAnyNotIndexed.FIELD_MIXED, value)
            is Decimal128 -> query.notEqualTo(RealmAnyNotIndexed.FIELD_MIXED, value)
            is ObjectId -> query.notEqualTo(RealmAnyNotIndexed.FIELD_MIXED, value)
            is UUID -> query.notEqualTo(RealmAnyNotIndexed.FIELD_MIXED, value)
            is Date -> query.notEqualTo(RealmAnyNotIndexed.FIELD_MIXED, value)
            is RealmAny -> query.notEqualTo(RealmAnyNotIndexed.FIELD_MIXED, value)
            is RealmModel -> return
            else -> throw IllegalStateException("NotEqualTo for type ${value::class.qualifiedName} not implemented")
        }

        validateNotEqual(expected, query.findAll()!!, expectedSize)
    }

    fun equalToInsensitive(expected: Array<RealmAny>, expectedSize: Int, value: Any) {
        val query: RealmQuery<RealmAnyNotIndexed> = realm.where()

        when (value) {
            is String -> query.equalTo(RealmAnyNotIndexed.FIELD_MIXED, value, Case.INSENSITIVE)
            is RealmAny -> query.equalTo(RealmAnyNotIndexed.FIELD_MIXED, value, Case.INSENSITIVE)
            else -> throw IllegalStateException("EqualTo[c] for type ${value::class.qualifiedName} not implemented")
        }

        validate(expected, query.findAll()!!, expectedSize)
    }

    fun notEqualToInsensitive(expected: Array<RealmAny>, expectedSize: Int, value: Any) {
        val query: RealmQuery<RealmAnyNotIndexed> = realm.where()

        when (value) {
            is String -> query.notEqualTo(RealmAnyNotIndexed.FIELD_MIXED, value, Case.INSENSITIVE)
            is RealmAny -> query.notEqualTo(RealmAnyNotIndexed.FIELD_MIXED, value, Case.INSENSITIVE)
            else -> throw IllegalStateException("NotEqualTo[c] for type ${value::class.qualifiedName} not implemented")
        }

        validateNotEqual(expected, query.findAll()!!, expectedSize)
    }

    fun greaterThanOrEqualTo(expected: Array<RealmAny>, expectedSize: Int, value: Any) {
        val query: RealmQuery<RealmAnyNotIndexed> = realm.where()

        when (value) {
            is Int -> query.greaterThanOrEqualTo(RealmAnyNotIndexed.FIELD_MIXED, value)
            is Long -> query.greaterThanOrEqualTo(RealmAnyNotIndexed.FIELD_MIXED, value)
            is Float -> query.greaterThanOrEqualTo(RealmAnyNotIndexed.FIELD_MIXED, value)
            is Double -> query.greaterThanOrEqualTo(RealmAnyNotIndexed.FIELD_MIXED, value)
            is Decimal128 -> query.greaterThanOrEqualTo(RealmAnyNotIndexed.FIELD_MIXED, value)
            is ObjectId -> query.greaterThanOrEqualTo(RealmAnyNotIndexed.FIELD_MIXED, value)
            is UUID -> query.greaterThanOrEqualTo(RealmAnyNotIndexed.FIELD_MIXED, value)
            is Date -> query.greaterThanOrEqualTo(RealmAnyNotIndexed.FIELD_MIXED, value)
            is RealmAny -> query.greaterThanOrEqualTo(RealmAnyNotIndexed.FIELD_MIXED, value)
            is Byte -> return
            is Short -> return
            else -> throw IllegalStateException("GreaterThanOrEqualTo for type ${value::class.qualifiedName} not implemented")
        }

        validate(expected, query.findAll()!!, expectedSize)
    }

    fun greaterThan(expected: Array<RealmAny>, expectedSize: Int, value: Any) {
        val query: RealmQuery<RealmAnyNotIndexed> = realm.where()

        when (value) {
            is Int -> query.greaterThan(RealmAnyNotIndexed.FIELD_MIXED, value)
            is Long -> query.greaterThan(RealmAnyNotIndexed.FIELD_MIXED, value)
            is Float -> query.greaterThan(RealmAnyNotIndexed.FIELD_MIXED, value)
            is Double -> query.greaterThan(RealmAnyNotIndexed.FIELD_MIXED, value)
            is Decimal128 -> query.greaterThan(RealmAnyNotIndexed.FIELD_MIXED, value)
            is ObjectId -> query.greaterThan(RealmAnyNotIndexed.FIELD_MIXED, value)
            is UUID -> query.greaterThan(RealmAnyNotIndexed.FIELD_MIXED, value)
            is Date -> query.greaterThan(RealmAnyNotIndexed.FIELD_MIXED, value)
            is RealmAny -> query.greaterThan(RealmAnyNotIndexed.FIELD_MIXED, value)
            is Byte -> return
            is Short -> return
            else -> throw IllegalStateException("GreaterThan for type ${value::class.qualifiedName} not implemented")
        }

        validate(expected, query.findAll()!!, expectedSize)
    }

    fun lessThanOrEqualTo(expected: Array<RealmAny>, expectedSize: Int, value: Any) {
        val query: RealmQuery<RealmAnyNotIndexed> = realm.where()

        when (value) {
            is Int -> query.lessThanOrEqualTo(RealmAnyNotIndexed.FIELD_MIXED, value)
            is Long -> query.lessThanOrEqualTo(RealmAnyNotIndexed.FIELD_MIXED, value)
            is Float -> query.lessThanOrEqualTo(RealmAnyNotIndexed.FIELD_MIXED, value)
            is Double -> query.lessThanOrEqualTo(RealmAnyNotIndexed.FIELD_MIXED, value)
            is Decimal128 -> query.lessThanOrEqualTo(RealmAnyNotIndexed.FIELD_MIXED, value)
            is ObjectId -> query.lessThanOrEqualTo(RealmAnyNotIndexed.FIELD_MIXED, value)
            is UUID -> query.lessThanOrEqualTo(RealmAnyNotIndexed.FIELD_MIXED, value)
            is Date -> query.lessThanOrEqualTo(RealmAnyNotIndexed.FIELD_MIXED, value)
            is RealmAny -> query.lessThanOrEqualTo(RealmAnyNotIndexed.FIELD_MIXED, value)
            is Byte -> return
            is Short -> return
            else -> throw IllegalStateException("LessThanOrEqualTo for type ${value::class.qualifiedName} not implemented")
        }

        validate(expected, query.findAll()!!, expectedSize)
    }

    fun lessThan(expected: Array<RealmAny>, expectedSize: Int, value: Any) {
        val query: RealmQuery<RealmAnyNotIndexed> = realm.where()

        when (value) {
            is Int -> query.lessThan(RealmAnyNotIndexed.FIELD_MIXED, value)
            is Long -> query.lessThan(RealmAnyNotIndexed.FIELD_MIXED, value)
            is Float -> query.lessThan(RealmAnyNotIndexed.FIELD_MIXED, value)
            is Double -> query.lessThan(RealmAnyNotIndexed.FIELD_MIXED, value)
            is Decimal128 -> query.lessThan(RealmAnyNotIndexed.FIELD_MIXED, value)
            is ObjectId -> query.lessThan(RealmAnyNotIndexed.FIELD_MIXED, value)
            is UUID -> query.lessThan(RealmAnyNotIndexed.FIELD_MIXED, value)
            is Date -> query.lessThan(RealmAnyNotIndexed.FIELD_MIXED, value)
            is RealmAny -> query.lessThan(RealmAnyNotIndexed.FIELD_MIXED, value)
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
    fun `in`(expected: Array<RealmAny>, expectedSize: Int, value: Array<Any?>) {
        val query: RealmQuery<RealmAnyNotIndexed> = realm.where()

        if (value.isNotEmpty()) {
            when (value[0]) {
                is Boolean -> query.`in`(RealmAnyNotIndexed.FIELD_MIXED, convertToType<Boolean?>(value))
                is Byte -> query.`in`(RealmAnyNotIndexed.FIELD_MIXED, convertToType<Byte?>(value))
                is Short -> query.`in`(RealmAnyNotIndexed.FIELD_MIXED, convertToType<Short?>(value))
                is Int -> query.`in`(RealmAnyNotIndexed.FIELD_MIXED, convertToType<Int?>(value))
                is Long -> query.`in`(RealmAnyNotIndexed.FIELD_MIXED, convertToType<Long?>(value))
                is Float -> query.`in`(RealmAnyNotIndexed.FIELD_MIXED, convertToType<Float?>(value))
                is Double -> query.`in`(RealmAnyNotIndexed.FIELD_MIXED, convertToType<Double?>(value))
                is String -> query.`in`(RealmAnyNotIndexed.FIELD_MIXED, convertToType<String?>(value))
                is Date -> query.`in`(RealmAnyNotIndexed.FIELD_MIXED, convertToType<Date?>(value))
                is RealmAny -> query.`in`(RealmAnyNotIndexed.FIELD_MIXED, convertToType<RealmAny?>(value))
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

    fun between(expected: Array<RealmAny>, expectedSize: Int, value1: Any, value2: Any) {
        val query: RealmQuery<RealmAnyNotIndexed> = realm.where()

        when (value1) {
            is Int -> query.between(RealmAnyNotIndexed.FIELD_MIXED, value1, value2 as Int)
            is Long -> query.between(RealmAnyNotIndexed.FIELD_MIXED, value1, value2 as Long)
            is Float -> query.between(RealmAnyNotIndexed.FIELD_MIXED, value1, value2 as Float)
            is Double -> query.between(RealmAnyNotIndexed.FIELD_MIXED, value1, value2 as Double)
            is Decimal128 -> query.between(RealmAnyNotIndexed.FIELD_MIXED, value1, value2 as Decimal128)
            is Date -> query.between(RealmAnyNotIndexed.FIELD_MIXED, value1, value2 as Date)
            is RealmAny -> query.between(RealmAnyNotIndexed.FIELD_MIXED, value1, value2 as RealmAny)
            is Byte -> return
            is Short -> return
            is ObjectId -> return
            is UUID -> return
            else -> throw IllegalStateException("Between for type ${value1::class.qualifiedName} not implemented")
        }

        validate(expected, query.findAll()!!, expectedSize)
    }

    fun beginsWith(expected: Array<RealmAny>, expectedSize: Int, value: Any) {
        val query: RealmQuery<RealmAnyNotIndexed> = realm.where()

        when (value) {
            is String -> query.beginsWith(RealmAnyNotIndexed.FIELD_MIXED, value)
            is RealmAny -> query.beginsWith(RealmAnyNotIndexed.FIELD_MIXED, value)
            else -> throw IllegalStateException("BeginsWith for type ${value::class.qualifiedName} not implemented")
        }

        validate(expected, query.findAll()!!, expectedSize)
    }

    fun beginsWithInsensitive(expected: Array<RealmAny>, expectedSize: Int, value: Any) {
        val query: RealmQuery<RealmAnyNotIndexed> = realm.where()

        when (value) {
            is String -> query.beginsWith(RealmAnyNotIndexed.FIELD_MIXED, value, Case.INSENSITIVE)
            is RealmAny -> query.beginsWith(RealmAnyNotIndexed.FIELD_MIXED, value, Case.INSENSITIVE)
            else -> throw IllegalStateException("BeginsWith for type ${value::class.qualifiedName} not implemented")
        }

        validate(expected, query.findAll()!!, expectedSize)
    }

    fun endsWith(expected: Array<RealmAny>, expectedSize: Int, value: Any) {
        val query: RealmQuery<RealmAnyNotIndexed> = realm.where()

        when (value) {
            is String -> query.endsWith(RealmAnyNotIndexed.FIELD_MIXED, value)
            is RealmAny -> query.endsWith(RealmAnyNotIndexed.FIELD_MIXED, value)
            else -> throw IllegalStateException("EndsWith for type ${value::class.qualifiedName} not implemented")
        }

        validate(expected, query.findAll()!!, expectedSize)
    }

    fun endsWithInsensitive(expected: Array<RealmAny>, expectedSize: Int, value: Any) {
        val query: RealmQuery<RealmAnyNotIndexed> = realm.where()

        when (value) {
            is String -> query.endsWith(RealmAnyNotIndexed.FIELD_MIXED, value, Case.INSENSITIVE)
            is RealmAny -> query.endsWith(RealmAnyNotIndexed.FIELD_MIXED, value, Case.INSENSITIVE)
            else -> throw IllegalStateException("EndsWith for type ${value::class.qualifiedName} not implemented")
        }

        validate(expected, query.findAll()!!, expectedSize)
    }

    fun contains(expected: Array<RealmAny>, expectedSize: Int, value: Any) {
        val query: RealmQuery<RealmAnyNotIndexed> = realm.where()

        when (value) {
            is String -> query.contains(RealmAnyNotIndexed.FIELD_MIXED, value)
            is RealmAny -> query.contains(RealmAnyNotIndexed.FIELD_MIXED, value)
            else -> throw IllegalStateException("Contains for type ${value::class.qualifiedName} not implemented")
        }

        validate(expected, query.findAll()!!, expectedSize)
    }

    fun containsInsensitive(expected: Array<RealmAny>, expectedSize: Int, value: Any) {
        val query: RealmQuery<RealmAnyNotIndexed> = realm.where()

        when (value) {
            is String -> query.contains(RealmAnyNotIndexed.FIELD_MIXED, value, Case.INSENSITIVE)
            is RealmAny -> query.contains(RealmAnyNotIndexed.FIELD_MIXED, value, Case.INSENSITIVE)
            else -> throw IllegalStateException("Contains for type ${value::class.qualifiedName} not implemented")
        }

        validate(expected, query.findAll()!!, expectedSize)
    }

    fun like(expected: Array<RealmAny>, expectedSize: Int, value: Any) {
        val query: RealmQuery<RealmAnyNotIndexed> = realm.where()

        when (value) {
            is String -> query.like(RealmAnyNotIndexed.FIELD_MIXED, value)
            is RealmAny -> query.like(RealmAnyNotIndexed.FIELD_MIXED, value)
            else -> throw IllegalStateException("Like for type ${value::class.qualifiedName} not implemented")
        }

        validate(expected, query.findAll()!!, expectedSize)
    }

    fun likeInsensitive(expected: Array<RealmAny>, expectedSize: Int, value: Any) {
        val query: RealmQuery<RealmAnyNotIndexed> = realm.where()

        when (value) {
            is String -> query.like(RealmAnyNotIndexed.FIELD_MIXED, value, Case.INSENSITIVE)
            is RealmAny -> query.like(RealmAnyNotIndexed.FIELD_MIXED, value, Case.INSENSITIVE)
            else -> throw IllegalStateException("Like for type ${value::class.qualifiedName} not implemented")
        }

        validate(expected, query.findAll()!!, expectedSize)
    }
}
