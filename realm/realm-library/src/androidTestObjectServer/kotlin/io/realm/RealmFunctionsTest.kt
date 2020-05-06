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
import org.bson.*
import org.bson.codecs.StringCodec
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.pojo.PojoCodecProvider
import org.bson.types.Decimal128
import org.bson.types.ObjectId
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class RealmFunctionsTest {

    private lateinit var app: TestRealmApp
    private lateinit var functions : RealmFunctions

    @Before
    fun setup() {
        Realm.init(InstrumentationRegistry.getInstrumentation().targetContext)
        app = TestRealmApp()
        functions = RealmFunctions(app.configuration.codecRegistry)
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
                    assertTypedEcho(java.lang.Float(1.4), java.lang.Float::class.java)
                    assertTypedEcho(java.lang.Double(1.4), java.lang.Double::class.java)
                    assertTypedEcho(BsonDouble(1.4), BsonDouble::class.java)
                }
                BsonType.STRING -> {
                    assertTypedEcho("Realm", String::class.java)
                    assertTypedEcho(BsonString("Realm"), BsonString::class.java)
                }
                BsonType.ARRAY -> {
                    // FIXME Fails in C++ parsing when boolean values are added...needs investigation
                    //  io.realm.exceptions.RealmError: Unrecoverable error. current state '$1' is not of expected state '$2' in /Users/claus.rorbech/proj/realm-java/realm/realm-library/src/main/cpp/io_realm_RealmFunctions.cpp line 32
                    //val listValues = listOf<Any>(true, i32, i64)
                    val listValues = listOf<Any>(i32, i64)
                    assertTypedEcho(listValues, List::class.java)
                }
                BsonType.BINARY -> {
                    val value = byteArrayOf(1, 2, 3)
                    val actual = functions.invoke(value, ByteArray::class.java)
                    assertEquals(value.toList(), actual.toList())
                    // FIXME Does not seem to preserve type
                    // assertTypedEcho(BsonBinary(UUID.randomUUID()), BsonBinary::class.java)
                    assertTypedEcho(BsonBinary(byteArrayOf(1,2,3)), BsonBinary::class.java)
                }
                BsonType.OBJECT_ID -> {
                    assertTypedEcho(ObjectId(), ObjectId::class.java)
                    assertTypedEcho(BsonObjectId(ObjectId()), BsonObjectId::class.java)
                }
                BsonType.BOOLEAN -> {
                    val value: Boolean = true
                    val actual: java.lang.Boolean = functions.invoke(value, java.lang.Boolean::class.java)
                    assertEquals(value, actual.booleanValue())
                    assertTypedEcho(BsonBoolean(true), BsonBoolean::class.java)
                }
                BsonType.INT32 -> {
                    assertTypedEcho(java.lang.Integer(32), Integer::class.java)
                    assertTypedEcho(BsonInt32(32), BsonInt32::class.java)
                }
                BsonType.INT64 -> {
                    assertTypedEcho(java.lang.Long(32L), java.lang.Long::class.java)
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

    private fun <T: Any> assertTypedEcho(value: T, returnClass: Class<T>) : T {
        val actual = functions.invoke(value, returnClass)
        assertEquals(value, actual)
        return actual
    }

    // Test of BSON JNI round trip until superseded with actual public api tests are added.
    data class Dog(var name: String? = null)
    @Test
    fun pojoCodecRegistry() {
        val pojoRegistry = CodecRegistries.fromRegistries(
                CodecRegistries.fromCodecs(StringCodec()),
                CodecRegistries.fromProviders(
                        PojoCodecProvider.builder()
                                .register(Dog::class.java)
                                .build()
                )
        )

        val input = Dog("PojoFido")

        val actual: Dog = functions.invoke(input, Dog::class.java, pojoRegistry)

        assertEquals(input, actual)
    }

}
