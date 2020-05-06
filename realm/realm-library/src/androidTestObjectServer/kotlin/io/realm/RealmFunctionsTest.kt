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
import io.realm.internal.util.BsonConverter
import org.bson.*
import org.bson.codecs.BsonValueCodecProvider
import org.bson.codecs.IterableCodecProvider
import org.bson.codecs.StringCodec
import org.bson.codecs.ValueCodecProvider
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.pojo.PojoCodecProvider
import org.bson.types.Decimal128
import org.bson.types.ObjectId
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.util.*
import kotlin.test.assertEquals

class RealmFunctionsTest {

    @Before
    fun setup() {
        Realm.init(InstrumentationRegistry.getInstrumentation().targetContext)
    }

    // Test of BSON JNI round trip until superseded with actual public api tests are added.
    @Test
    fun jniBsonOnlyRoundtrip() {
        val functions = RealmFunctions()
        val i32 = 42
        val i64 = 42L
        val s = "Realm"

        assertEquals(i32, functions.invoke(BsonInt32(i32)).asInt32().value)
        assertEquals(i64, functions.invoke(BsonInt64(i64)).asInt64().value)
        assertEquals(s, functions.invoke(BsonString(s)).asString().value)
        
        val values = listOf<Any>(BsonInt32(i32), BsonInt64(i64), BsonString(s))
        val invoke: BsonValue = functions.invoke(BsonConverter.to(values))
        assertEquals(values, invoke.asArray().values)
    }

    @Test
    fun bsonValueCodec() {
        val functions = RealmFunctions()

        val registry = CodecRegistries.fromRegistries(
            CodecRegistries.fromProviders(
                    // For primitive support
                    ValueCodecProvider(),
                    // For BSONValue support
                    BsonValueCodecProvider(),
                    // For list support
                    IterableCodecProvider()
            )
        )

        for(type in BsonType.values()) {
            when (type) {
                BsonType.DOUBLE -> {
                    assertEquals(java.lang.Double(1.5), functions.invoke(1.5, java.lang.Double::class.java, registry))
                }
                BsonType.STRING -> {
                    val value = "Realm"
                    assertEquals(value, functions.invoke(value, String::class.java, registry))
                }
                BsonType.ARRAY -> {
                    // FIXME Fails in C++ parsing when boolean values are add...needs investigation
                    // val listOf = listOf<Any>(1.5, true, "Realm")
                    val listOf = listOf<Any>(1.5, "Realm")
                    assertEquals(listOf, functions.invoke(listOf, List::class.java, registry))
                }
                BsonType.BINARY -> {
                    val value = byteArrayOf(1, 2, 3)
                    assertEquals(value.toList(), functions.invoke(value, ByteArray::class.java, registry).toList())
                }
                BsonType.OBJECT_ID -> {
                    val value = ObjectId()
                    assertEquals(value, functions.invoke(value, ObjectId::class.java, registry))
                }
                BsonType.BOOLEAN -> {
                    val value = true
                    assertEquals(value, functions.invoke(value, java.lang.Boolean::class.java, registry).booleanValue())
                }
                BsonType.DATE_TIME -> {
                    val value = Date(Instant.now().toEpochMilli())
                    assertEquals(value, functions.invoke(value, Date::class.java, registry))
                }
                BsonType.INT32 -> {
                    val value = 32
                    assertEquals(value, functions.invoke(value, Integer::class.java, registry).toInt())
                }
                BsonType.INT64 -> {
                    val value = 32L
                    assertEquals(value, functions.invoke(value, java.lang.Long::class.java, registry).toLong())
                }
                BsonType.DECIMAL128 -> {
                    val value = Decimal128(32)
                    assertEquals(value, functions.invoke(value, Decimal128::class.java, registry))
                }
                BsonType.NULL -> {
                    // No value codec for null
                    val value = BsonNull()
                    assertEquals(value, functions.invoke(value, BsonValue::class.java, registry))
                }
//                BsonType.DOCUMENT -> TODO()
//                BsonType.UNDEFINED -> TODO()
//                BsonType.REGULAR_EXPRESSION -> TODO()
//                BsonType.DB_POINTER -> TODO()
//                BsonType.JAVASCRIPT -> TODO()
//                BsonType.SYMBOL -> TODO()
//                BsonType.JAVASCRIPT_WITH_SCOPE -> TODO()
//                BsonType.MIN_KEY -> TODO()
//                BsonType.MAX_KEY -> TODO()
//                BsonType.END_OF_DOCUMENT -> TODO()
//                BsonType.TIMESTAMP -> TODO()
                else -> {}
            }
        }
    }

    // Test of BSON JNI round trip until superseded with actual public api tests are added.
    data class Dog(var name: String? = null)
    @Test
    fun pojoCodecRegistry() {
        val functions = RealmFunctions()

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
