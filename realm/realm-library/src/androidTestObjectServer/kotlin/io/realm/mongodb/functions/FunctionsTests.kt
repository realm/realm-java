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

package io.realm.mongodb.functions

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.realm.*
import io.realm.admin.ServerAdmin
import io.realm.rule.BlockingLooperThread
import io.realm.util.assertFailsWithErrorCode
import org.bson.*
import org.bson.codecs.*
import org.bson.codecs.configuration.CodecConfigurationException
import org.bson.codecs.configuration.CodecProvider
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.configuration.CodecRegistry
import org.bson.codecs.pojo.PojoCodecProvider
import org.bson.types.Decimal128
import org.bson.types.ObjectId
import org.junit.After
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class FunctionsTests {

    companion object {
        const val FIRST_ARG_FUNCTION = "firstArg"
    }

    // Pojo class for testing custom encoder/decoder
    data class Dog(var name: String? = null)

    private val looperThread = BlockingLooperThread()

    private lateinit var app: TestRealmApp
    private lateinit var functions: Functions

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
        Realm.init(InstrumentationRegistry.getInstrumentation().targetContext)
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
                    val map = mapOf("foo" to 5)
                    val document = Document(map)
                    assertEquals(map, functions.callFunction(FIRST_ARG_FUNCTION, listOf(map), Map::class.java))
                    assertEquals(map, functions.callFunction(FIRST_ARG_FUNCTION, listOf(document), Map::class.java))
                    assertEquals(document, functions.callFunction(FIRST_ARG_FUNCTION, listOf(map), Document::class.java))
                    assertEquals(document, functions.callFunction(FIRST_ARG_FUNCTION, listOf(document), Document::class.java))

                    // Previously failing in C++ parser
                    var documents = listOf(Document(), Document())
                    assertEquals(documents[0], functions.callFunction(FIRST_ARG_FUNCTION, documents, Document::class.java))
                    documents = listOf(Document("KEY", "VALUE"), Document("KEY", "VALUE"), Document("KEY", "VALUE"))
                    assertEquals(documents[0], functions.callFunction(FIRST_ARG_FUNCTION, documents, Document::class.java))
                }
                BsonType.DATE_TIME -> {
                    // FIXME See jniParseError_date
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
            try {
                assertEquals(32, result.orThrow.toInt())
            } finally {
                looperThread.testComplete()
            }
        }
    }


    @Test
    fun codecArgumentFailure() {
        assertFailsWith<CodecConfigurationException> {
            functions.callFunction(FIRST_ARG_FUNCTION, listOf(Dog("PojoFido")), Dog::class.java)
        }
    }

    @Test
    fun asyncCodecArgumentFailure() = looperThread.runBlocking {
        functions.callFunctionAsync(FIRST_ARG_FUNCTION, listOf(Dog("PojoFido")), Integer::class.java) { result ->
            try {
                assertTrue(result.error.exception is CodecConfigurationException)
            } finally {
                looperThread.testComplete()
            }
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
        functions.callFunctionAsync(FIRST_ARG_FUNCTION, listOf(Dog("PojoFido")), Integer::class.java) { result ->
            try {
                assertTrue(result.error.exception is CodecConfigurationException)
            } finally {
                looperThread.testComplete()
            }
        }
    }

    @Test
    fun codecBsonFailure() {
        assertFailsWith<BSONException> {
            functions.callFunction(FIRST_ARG_FUNCTION, listOf(32), String::class.java)
        }
    }

    @Test
    fun asyncCodecBsonFailure() = looperThread.runBlocking {
        functions.callFunctionAsync(FIRST_ARG_FUNCTION, listOf(32), String::class.java) { result ->
            try {
                assertTrue(result.error.exception is BSONException)
            } finally {
                looperThread.testComplete()
            }
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
            try {
                assertEquals(input, result.orThrow)
            } finally {
                looperThread.testComplete()
            }
        }
    }

    @Test
    fun instanceCodecRegistry() {
        val input = Dog("PojoFido")
        val functionsWithCodecRegistry = anonUser.getFunctions(pojoRegistry)
        assertEquals(input, functionsWithCodecRegistry.callFunction(FIRST_ARG_FUNCTION, listOf(input), Dog::class.java))
    }

    @Test
    fun resultDecoder() {
        val input = "Realm"
        val customResult = "Decoded realm"
        val decoder = object: Decoder<String> {
            override fun decode(reader: BsonReader, decoderContext: DecoderContext): String {
                reader.readString()
                return customResult
            }
        }
        assertEquals(customResult, functions.callFunction(FIRST_ARG_FUNCTION, listOf(input), decoder))
    }

    @Test
    fun asyncResultDecoder() = looperThread.runBlocking {
        val input = "Realm"
        val customResult = "Decoded realm"
        val decoder = Decoder<String> { reader, decoderContext ->
            reader.readString()
            customResult
        }
        functions.callFunctionAsync(FIRST_ARG_FUNCTION, listOf(input), decoder, RealmApp.Callback<String> { result ->
            try {
                assertEquals(customResult, result.orThrow)
            } finally {
                looperThread.testComplete()
            }
        })
    }

    @Test
    fun unknownFunction() {
        assertFailsWithErrorCode(ErrorCode.FUNCTION_NOT_FOUND) {
             functions.callFunction("unknown", listOf(32), String::class.java)
        }
    }

    @Test
    fun asyncUnknownFunction() = looperThread.runBlocking {
        val input = Dog("PojoFido")
        functions.callFunctionAsync("unknown", listOf(input), Dog::class.java, pojoRegistry) { result ->
            try {
                assertEquals(ErrorCode.FUNCTION_NOT_FOUND, result.error.errorCode)
            } finally {
                looperThread.testComplete()
            }
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
        assertFailsWithErrorCode(ErrorCode.FUNCTION_EXECUTION_ERROR) {
            functions.callFunction("error", emptyList<Any>(), String::class.java)
        }
    }

    @Test
    fun callFunction_null() {
        assertTrue(functions.callFunction("null", emptyList<Any>(), BsonNull::class.java).isNull)
    }

    @Test
    fun callFunction_void() {
        assertEquals(BsonType.UNDEFINED, functions.callFunction("void", emptyList<Any>(), BsonUndefined::class.java).bsonType)
    }

    @Test
    fun callFunction_afterLogout() {
        anonUser.logOut()
        assertFailsWithErrorCode(ErrorCode.SERVICE_UNKNOWN) {
            functions.callFunction(FIRST_ARG_FUNCTION, listOf(1, 2, 3), Integer::class.java)
        }
    }

    // Tests that functions that should not execute based on "canevalute"-expression fails.
    @Test
    fun callFunction_authorizedOnly() {
        // Not allow for anonymous user
        assertFailsWithErrorCode(ErrorCode.FUNCTION_EXECUTION_ERROR) {
            functions.callFunction("authorizedOnly", listOf(1, 2, 3), Document::class.java)
        }
        // User email must match "canevaluate" section of servers "functions/authorizedOnly/config.json"
        val authorizedUser = app.registerUserAndLogin("authorizeduser@example.org", "asdfasdf")
        assertNotNull(authorizedUser.functions.callFunction("authorizedOnly", listOf(1,2,3), Document::class.java))
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
        //  to here, but we do not have infrastructure to easily override TestRealmApp configuration,
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
    fun illegalBsonArgument() {
        // Coded that will generate non-BsonArray from list
        val faultyListCodec = object : Codec<Iterable<*>> {
            override fun getEncoderClass(): Class<Iterable<*>> { return Iterable::class.java }
            override fun encode(writer: BsonWriter, value: Iterable<*>, encoderContext: EncoderContext) {
                writer.writeString("Not an array")
            }
            override fun decode(reader: BsonReader?, decoderContext: DecoderContext?): ArrayList<*> {
                TODO("Not yet implemented")
            }
        }
        // Codec registry that will use the above faulty codec for lists
        val faultyCodecRegistry = CodecRegistries.fromProviders(
                object: CodecProvider {
                    override fun <T : Any> get(clazz: Class<T>?, registry: CodecRegistry?): Codec<T> {
                        return faultyListCodec as Codec<T>
                    }
                }
        )
        assertFailsWith<IllegalArgumentException> {
            functions.callFunction(FIRST_ARG_FUNCTION, listOf("Realm"), String::class.java, faultyCodecRegistry)
        }
    }

    @Test
    @Ignore("JNI parsing crashes tests")
    fun jniParseError_arrayOfBinary() {
        val value = byteArrayOf(1, 2, 3)
        val listOf = listOf(value)
        val actual = functions.callFunction(FIRST_ARG_FUNCTION, listOf, ByteArray::class.java)
        assertEquals(value.toList(), actual.toList())
    }

    @Test
    @Ignore("JNI parsing fails to parse into a bson array")
    fun jniParseError_arrayOfDocuments() {
        val map = mapOf("foo" to 5, "bar" to  7)
        assertEquals(map, functions.callFunction(FIRST_ARG_FUNCTION, listOf(map), Map::class.java))
    }

    @Test
    @Ignore("JNI parsing seems to truncate value to 32-bit")
    fun jniParseError_date() {
        val now = Date(System.currentTimeMillis())
        assertEquals(now, functions.callFunction(FIRST_ARG_FUNCTION, listOf(now), Date::class.java))
    }
}
