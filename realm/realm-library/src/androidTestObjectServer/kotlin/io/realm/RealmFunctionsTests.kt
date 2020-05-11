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
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.pojo.PojoCodecProvider
import org.bson.types.Decimal128
import org.bson.types.ObjectId
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
class RealmFunctionsTests {
    private val looperThread = BlockingLooperThread()

    private lateinit var app: TestRealmApp
    private lateinit var functions: RealmFunctions

    private lateinit var anonUser: RealmUser
    private lateinit var admin: ServerAdmin

    @Before
    fun setup() {
        app = TestRealmApp()
        admin = ServerAdmin()
        anonUser = app.login(RealmCredentials.anonymous())

        functions = RealmFunctions(anonUser, app.configuration.defaultCodecRegistry)
    }

    @After
    fun teardown() {
        if (this::app.isInitialized) {
            app.close()
        }
    }

    // Test of BSON JNI round trip until superseded with actual public api tests are added.
    @Test
    fun jniRoundTripForDefaultCodecRegistry() {
        val i32 = 42
        val i64 = 42L

        for (type in BsonType.values()) {
            when (type) {
                BsonType.DOUBLE -> {
                    assertEquals(1.4f, functions.callFunction("echo", listOf(1.4f), java.lang.Float::class.java).toFloat())
                    assertEquals(1.4, functions.callFunction("echo", listOf(1.4f), java.lang.Double::class.java).toDouble())
                    assertTypedEcho(BsonDouble(1.4), BsonDouble::class.java)
                }
                BsonType.STRING -> {
                    assertTypedEcho("Realm", String::class.java)
                    assertTypedEcho(BsonString("Realm"), BsonString::class.java)
                }
                // FIXME Does not seem to work, typically this has indicated an issue with C++ parser
//                BsonType.ARRAY -> {
//                    val listValues = listOf<Any>(true, i32, i64)
//                    assertTypedEcho(listValues, List::class.java)
//                    assertEquals(true, functions.callFunction("echo", listOf(true, i32, i64), java.lang.Boolean::class.java).booleanValue())
//                }
                // FIXME Does not seem to work, typically this has indicated an issue with C++ parser
//                BsonType.BINARY -> {
//                    val value = byteArrayOf(1, 2, 3)
//                    val actual = functions.callFunction("echo", listOf(value), ByteArray::class.java)
//                    assertEquals(value.toList(), actual.toList())
//                    // FIXME C++ Does not seem to preserve subtype
//                    // arg      = "{"value": {"$binary": {"base64": "JmS8oQitTny4IPS2tyjmdA==", "subType": "04"}}}"
//                    // response = "{"value":{"$binary":{"base64":"JmS8oQitTny4IPS2tyjmdA==","subType":"00"}}}"
//                    // assertTypedEcho(BsonBinary(UUID.randomUUID()), BsonBinary::class.java)
//                    assertTypedEcho(BsonBinary(byteArrayOf(1,2,3)), BsonBinary::class.java)
//                }
                BsonType.OBJECT_ID -> {
                    assertTypedEcho(ObjectId(), ObjectId::class.java)
                    assertTypedEcho(BsonObjectId(ObjectId()), BsonObjectId::class.java)
                }
                BsonType.BOOLEAN -> {
                    assertEquals(true, functions.callFunction("echo", listOf(true), java.lang.Boolean::class.java).booleanValue())
                    assertTypedEcho(BsonBoolean(true), BsonBoolean::class.java)
                }
                BsonType.INT32 -> {
                    assertEquals(32, functions.callFunction("echo", listOf(32), Integer::class.java).toInt())
                    assertEquals(32, functions.callFunction("echo", listOf(32L), Integer::class.java).toInt())
                    assertTypedEcho(BsonInt32(32), BsonInt32::class.java)
                }
                BsonType.INT64 -> {
                    assertEquals(32L, functions.callFunction("echo", listOf(32L), java.lang.Long::class.java).toLong())
                    assertEquals(32L, functions.callFunction("echo", listOf(32), java.lang.Long::class.java).toLong())
                    assertTypedEcho(BsonInt64(32), BsonInt64::class.java)
                }
                BsonType.DECIMAL128 -> {
                    assertTypedEcho(Decimal128(32L), Decimal128::class.java)
                    assertTypedEcho(BsonDecimal128(Decimal128(32L)), BsonDecimal128::class.java)
                }
                // TODO
                BsonType.DOCUMENT,
                BsonType.UNDEFINED,
                BsonType.DATE_TIME,
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
                    // No conversion is implemented for these types yet
                }
            }
        }
    }

    private fun <T : Any> assertTypedEcho(value: T, returnClass: Class<T>) : T {
        val actual = functions.callFunction("echo", listOf(value), returnClass)
        assertEquals(value, actual)
        return actual
    }

    // Test of BSON JNI round trip until superseded with actual public api tests are added.
    // Tests:
    // - Local codecRegistry
    data class Dog(var name: String? = null)
    @Test
    fun pojoCodecRegistry() {
        val pojoRegistry = CodecRegistries.fromRegistries(
                app.configuration.defaultCodecRegistry,
                CodecRegistries.fromProviders(
                        PojoCodecProvider.builder()
                                .register(Dog::class.java)
                                .build()
                )
        )

        val input = Dog("PojoFido")

        val actual: Dog = functions.callFunction("echo", listOf(input), Dog::class.java, pojoRegistry)

        assertEquals(input, actual)
    }

    // FIXME Remaining tests
    //  - Default App codec registry
    //  - Function specific codec registry
    //  - Argument codec registry
    //  - Argument encoder/decoder
    //  - Codec conversion error
    //  - Unknown function
    //  - Unauthorized access

}
