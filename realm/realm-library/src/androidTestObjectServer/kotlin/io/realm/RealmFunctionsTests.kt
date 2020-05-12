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
import io.realm.admin.ServerAdmin
import io.realm.rule.BlockingLooperThread
import org.bson.*
import org.bson.codecs.StringCodec
import org.bson.codecs.configuration.CodecConfigurationException
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.pojo.PojoCodecProvider
import org.bson.types.Decimal128
import org.bson.types.ObjectId
import org.junit.After
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class RealmFunctionsTests {

    companion object {
        const val FIRST_ARG_FUNCTION = "firstArg"
    }

    // Pojo class for testing custom encoder/decoder
    data class Dog(var name: String? = null)

    private val looperThread = BlockingLooperThread()

    private lateinit var app: TestRealmApp
    private lateinit var functions: RealmFunctions

    private lateinit var anonUser: RealmUser
    private lateinit var admin: ServerAdmin

    // Custom registry with support for encoding/decoding Dogs
    val pojoRegistry by lazy {
        CodecRegistries.fromRegistries(
                app.configuration.defaultCodecRegistry,
                CodecRegistries.fromProviders(
                        PojoCodecProvider.builder()
                                .register(Dog::class.java)
                                .build()
                )
        )
    }

    @Before
    fun setup() {
        app = TestRealmApp()
        admin = ServerAdmin()
        anonUser = app.login(RealmCredentials.anonymous())
        functions = anonUser.functions
    }

    @After
    fun teardown() {
        if (this::app.isInitialized) {
            app.close()
        }
    }

    // Tests
    // - Default codec factory
    @Test
    fun jniRoundTripForDefaultCodecRegistry() {
        val i32 = 42
        val i64 = 42L

        for (type in BsonType.values()) {
            when (type) {
                BsonType.DOUBLE -> {
                    assertEquals(1.4f, functions.callFunction(FIRST_ARG_FUNCTION, listOf(1.4f), java.lang.Float::class.java).toFloat())
                    assertEquals(1.4, functions.callFunction(FIRST_ARG_FUNCTION, listOf(1.4f), java.lang.Double::class.java).toDouble())
                    assertTypeOfFirstArgFunction(BsonDouble(1.4), BsonDouble::class.java)
                }
                BsonType.STRING -> {
                    assertTypeOfFirstArgFunction("Realm", String::class.java)
                    assertTypeOfFirstArgFunction(BsonString("Realm"), BsonString::class.java)
                }
                BsonType.ARRAY -> {
                    val values1 = listOf<Any>(true, i32, i64)
                    assertEquals(values1[0], functions.callFunction(FIRST_ARG_FUNCTION, values1, java.lang.Boolean::class.java))

                    // Previously failing in C++ parsing
                    val values2 = listOf(1, true, 3)
                    assertEquals(values2, functions.callFunction(FIRST_ARG_FUNCTION, listOf(values2), List::class.java))
                    val values3 = listOf(2, "Realm", 3)
                    assertEquals(values3, functions.callFunction(FIRST_ARG_FUNCTION, listOf(values3), List::class.java))
                }
                // FIXME Does not seem to work, typically this has indicated an issue with C++
                //  parser. Probably because of embedding an array in an array, added explicit test
//                BsonType.BINARY -> {
//                    val value = byteArrayOf(1, 2, 3)
//                    val actual = functions.callFunction(FIRST_ARG_FUNCTION, listOf(value), ByteArray::class.java)
//                    assertEquals(value.toList(), actual.toList())
//                    // FIXME C++ Does not seem to preserve subtype
//                    // arg      = "{"value": {"$binary": {"base64": "JmS8oQitTny4IPS2tyjmdA==", "subType": "04"}}}"
//                    // response = "{"value":{"$binary":{"base64":"JmS8oQitTny4IPS2tyjmdA==","subType":"00"}}}"
//                    // assertTypedEcho(BsonBinary(UUID.randomUUID()), BsonBinary::class.java)
//                    assertTypedEcho(BsonBinary(byteArrayOf(1,2,3)), BsonBinary::class.java)
//                }
                BsonType.OBJECT_ID -> {
                    assertTypeOfFirstArgFunction(ObjectId(), ObjectId::class.java)
                    assertTypeOfFirstArgFunction(BsonObjectId(ObjectId()), BsonObjectId::class.java)
                }
                BsonType.BOOLEAN -> {
                    assertTrue(functions.callFunction(FIRST_ARG_FUNCTION, listOf(true), java.lang.Boolean::class.java).booleanValue())
                    assertTypeOfFirstArgFunction(BsonBoolean(true), BsonBoolean::class.java)
                }
                BsonType.INT32 -> {
                    assertEquals(32, functions.callFunction(FIRST_ARG_FUNCTION, listOf(32), Integer::class.java).toInt())
                    assertEquals(32, functions.callFunction(FIRST_ARG_FUNCTION, listOf(32L), Integer::class.java).toInt())
                    assertTypeOfFirstArgFunction(BsonInt32(32), BsonInt32::class.java)
                }
                BsonType.INT64 -> {
                    assertEquals(32L, functions.callFunction(FIRST_ARG_FUNCTION, listOf(32L), java.lang.Long::class.java).toLong())
                    assertEquals(32L, functions.callFunction(FIRST_ARG_FUNCTION, listOf(32), java.lang.Long::class.java).toLong())
                    assertTypeOfFirstArgFunction(BsonInt64(32), BsonInt64::class.java)
                }
                BsonType.DECIMAL128 -> {
                    assertTypeOfFirstArgFunction(Decimal128(32L), Decimal128::class.java)
                    assertTypeOfFirstArgFunction(BsonDecimal128(Decimal128(32L)), BsonDecimal128::class.java)
                }
                BsonType.DOCUMENT -> {
                    val map = mapOf("foo" to 5, "bar" to 7)
                    val document = Document(map)
                    assertEquals(map, functions.callFunction(FIRST_ARG_FUNCTION, listOf(map), Map::class.java))
                    assertEquals(map, functions.callFunction(FIRST_ARG_FUNCTION, listOf(document), Map::class.java))
                    assertEquals(document, functions.callFunction(FIRST_ARG_FUNCTION, listOf(map), Document::class.java))
                    assertEquals(document, functions.callFunction(FIRST_ARG_FUNCTION, listOf(document), Document::class.java))

                    // Previously failing in C++ parser
                    val documents = listOf(Document(), Document())
                    assertEquals(documents[0], functions.callFunction(FIRST_ARG_FUNCTION, documents, Document::class.java))
                }
                BsonType.DATE_TIME -> {
                    val now = Date(Instant.now().toEpochMilli())
                    assertEquals(now, functions.callFunction(FIRST_ARG_FUNCTION, listOf(now), Date::class.java))
                }
                BsonType.UNDEFINED,
                BsonType.NULL,
                BsonType.REGULAR_EXPRESSION,
                BsonType.SYMBOL,
                BsonType.DB_POINTER,
                BsonType.JAVASCRIPT,
                BsonType.JAVASCRIPT_WITH_SCOPE,
                BsonType.TIMESTAMP,
                BsonType.END_OF_DOCUMENT,
                BsonType.MIN_KEY,
                BsonType.MAX_KEY -> {
                    // Relying on org.bson codec providers for conversion, so skipping explicit
                    // tests for these more exotic types
                }
            }
        }
    }

    private fun <T : Any> assertTypeOfFirstArgFunction(value: T, returnClass: Class<T>) : T {
        val actual = functions.callFunction(FIRST_ARG_FUNCTION, listOf(value), returnClass)
        assertEquals(value, actual)
        return actual
    }

    @Test
    fun asyncCallFunction() = looperThread.runBlocking {
        functions.callFunctionAsync(FIRST_ARG_FUNCTION, listOf(32), Integer::class.java) { result ->
            if (result.isSuccess) {
                assertEquals(32, result.get().toInt())
            } else  {
                fail()
            }
            looperThread.testComplete()
        }
    }


    @Test
    fun codecArgumentFailure() {
        val input = Dog("PojoFido")
        assertFailsWith<CodecConfigurationException> {
            functions.callFunction(FIRST_ARG_FUNCTION, listOf(input), Dog::class.java)
        }
    }

    @Test
    fun asyncCodecArgumentFailure() = looperThread.runBlocking {
        functions.callFunctionAsync(FIRST_ARG_FUNCTION, listOf(Dog("PojoFido")), Integer::class.java) { result ->
            if (result.isSuccess) {
                fail()
            } else  {
                assertTrue(result.error.exception is CodecConfigurationException)
            }
            looperThread.testComplete()
        }
    }

    @Test
    fun codecResponseFailure() {
        assertFailsWith<CodecConfigurationException> {
            functions.callFunction(FIRST_ARG_FUNCTION, listOf(32), Dog::class.java)
        }
    }

    @Test
    fun asyncCodecResponseFailure() = looperThread.runBlocking {
        val input = Dog("PojoFido")
        functions.callFunctionAsync(FIRST_ARG_FUNCTION, listOf(Dog("PojoFido")), Integer::class.java) { result ->
            if (result.isSuccess) {
                fail()
            } else  {
                assertTrue(result.error.exception is CodecConfigurationException)
            }
            looperThread.testComplete()
        }
    }

    @Test
    fun localCodecRegistry() {
        val input = Dog("PojoFido")
        assertEquals(input, functions.callFunction(FIRST_ARG_FUNCTION, listOf(input), Dog::class.java, pojoRegistry))
    }

    @Test
    fun asyncLocalCodecRegistry() = looperThread.runBlocking {
        val input = Dog("PojoFido")
        functions.callFunctionAsync(FIRST_ARG_FUNCTION, listOf(input), Dog::class.java, pojoRegistry) { result ->
            if (result.isSuccess) {
                assertEquals(input, result.get())
            } else  {
                fail()
            }
            looperThread.testComplete()
        }
    }

    @Test
    fun instanceCodecRegistry() {
        val input = Dog("PojoFido")
        val functionsWithCodecRegistry = anonUser.getFunctions(pojoRegistry)
        assertEquals(input, functionsWithCodecRegistry.callFunction(FIRST_ARG_FUNCTION, listOf(input), Dog::class.java))
    }

    @Test
    fun unknownFunction() {
        assertFailsWith<ObjectServerError> {
            functions.callFunction("unknown", listOf(32), Dog::class.java)
        }
    }

    @Test
    fun asyncUnknownFunction() = looperThread.runBlocking {
        val input = Dog("PojoFido")
        functions.callFunctionAsync("unknown", listOf(input), Dog::class.java, pojoRegistry) { result ->
            if (result.isSuccess) {
                fail()
            } else  {
                // FIXME How verify exact error. NativeErrorIntValue? Or error message?
                assertTrue(result.error is ObjectServerError)
            }
            looperThread.testComplete()
        }
    }

    @Test
    fun asyncNonLoopers() {
        assertFailsWith<IllegalStateException> {
            functions.callFunctionAsync(FIRST_ARG_FUNCTION, listOf(32), Integer::class.java, pojoRegistry) { result ->
                fail()
            }
        }
    }

    @Test
    fun callFunction_sum() {
        val numbers = listOf(1, 2, 3, 4)
        assertEquals(10, functions.callFunction("sum", numbers, Integer::class.java).toInt())
    }

    @Test
    fun callFunction_remoteError() {
        assertFailsWith<ObjectServerError> {
            // FIXME Do we need to assert more about the error
            functions.callFunction("error", emptyList<Any>(), String::class.java)
        }
    }

    @Test
    fun callFunction_null() {
        assertTrue(functions.callFunction("null", emptyList<Any>(), BsonNull::class.java).isNull)
    }

    @Test
    fun callFunction_empty() {
        assertEquals(BsonType.UNDEFINED, functions.callFunction("empty", emptyList<Any>(), BsonUndefined::class.java).bsonType)
    }

    @Test
    fun getApp() {
        assertEquals(app, functions.app)
    }

    @Test
    fun getUser() {
        assertEquals(anonUser, functions.user)
    }

    @Test
    fun defaultCodecRegistry() {
        // TODO Maybe we should test that setting configuration specific would propagate all the way
        //  to here, but we do not have infrastructure to easily override TestRealmApp coniguration,
        //  and actual configuration is verified in RealmAppConfigurationTests
        assertEquals(app.configuration.defaultCodecRegistry, functions.defaultCodecRegistry)
    }

    @Test
    fun customCodecRegistry() {
        val configCodecRegistry = CodecRegistries.fromCodecs(StringCodec())
        val customCodecRegistryFunctions = anonUser.getFunctions(configCodecRegistry)
        assertEquals(configCodecRegistry, customCodecRegistryFunctions.defaultCodecRegistry)
    }

    @Test
    @Ignore("JNI parsing crashes tests")
    fun jniParseErrorArrayOfBinary() {
        val value = byteArrayOf(1, 2, 3)
        val listOf = listOf(value)
        val actual = functions.callFunction(FIRST_ARG_FUNCTION, listOf, ByteArray::class.java)
        assertEquals(value.toList(), actual.toList())
    }

}
