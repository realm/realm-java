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

import io.realm.internal.util.BsonConverter
import org.bson.*
import org.bson.types.Decimal128
import org.bson.types.ObjectId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Ignore
import org.junit.Test
import kotlin.test.assertFailsWith

class BsonTest {

    /**
     * Simple test to verify semantics of org.bson JSON encoding and decoding.
     */
    // Only for Bson API evaluation, not testing Realm functionality
    @Test
    fun bsonRoundtrip() {
        val valueInt32   = 42
        val valueInt64   = 42L
        val valueString  = "Realm"
        val valueBoolean = true
        val valueOid     = ObjectId()

        val document = BsonDocument.parse("{}")

        document.append("arg1", BsonInt32(valueInt32))
        document.append("arg2", BsonInt64(valueInt64))
        document.append("arg3", BsonString(valueString))
        document.append("arg4", BsonBoolean(valueBoolean))
        document.append("arg5", BsonObjectId(valueOid))

        val roundtrip = BsonDocument.parse(document.toJson())
        assertEquals(valueInt32,   roundtrip.get("arg1")?.asInt32()?.value)
        assertEquals(valueInt64,   roundtrip.get("arg2")?.asInt64()?.value)
        assertEquals(valueString,  roundtrip.get("arg3")?.asString()?.value)
        assertEquals(valueBoolean, roundtrip.get("arg4")?.asBoolean()?.value)
        assertEquals(valueOid,     roundtrip.get("arg5")?.asObjectId()?.value)

        // We cannot retrieve bson values differently type, not even if it could fit in the type
        assertFailsWith<RuntimeException> {
            roundtrip.getInt32("arg2");
        }
        assertFailsWith<RuntimeException> {
            roundtrip.getInt64("arg1");
        }
    }

    /**
     * Simple test of type conversion between native Java object types and BSON types.
     */
    @Test
    fun bsonConversion() {
        val b = true
        val i32 = 32
        val i64 = 32L
        val f = 1.24f
        val d = 2.34.toDouble()
        val s = "Realm"
        val oid = ObjectId()
        val d128 = Decimal128(i64)
        val bin = byteArrayOf(0, 1, 2, 3)

        val bi32 = BsonInt32(15)
        val bOid = BsonObjectId(oid)
        val bDoc = BsonDocument()

        for (type in BsonType.values()) {
            when (type) {
                BsonType.DOUBLE -> {
                    assertEquals(BsonDouble(f.toDouble()), BsonConverter.to(f))
                    assertEquals(BsonDouble(d), BsonConverter.to(d))
                    assertEquals(d, BsonConverter.from(java.lang.Double::class.java, BsonDouble(d)))
                }
                BsonType.STRING -> {
                    assertEquals(BsonString(s), BsonConverter.to(s))
                    assertEquals(s, BsonConverter.from(String::class.java, BsonString(s)))

                }
                BsonType.ARRAY -> {
                    assertTrue(BsonConverter.to(b, i32, i64) is BsonArray)
                    val listValues = listOf<BsonValue>(BsonInt32(i32), BsonInt64(i64))
                    assertEquals(listValues, BsonConverter.from(List::class.java, BsonArray(listValues)))
                }
                BsonType.BINARY -> {
                    assertEquals(BsonBinary(bin), BsonConverter.to(bin))
                    assertEquals(bin, BsonConverter.from(ByteArray::class.java, BsonBinary(bin)))
                }
                BsonType.OBJECT_ID -> {
                    assertEquals(BsonObjectId(oid), BsonConverter.to(oid))
                    assertEquals(oid, BsonConverter.from(ObjectId::class.java, BsonObjectId(oid)))
                }
                BsonType.BOOLEAN -> {
                    assertEquals(BsonBoolean(b), BsonConverter.to(b))
                    assertEquals(b, BsonConverter.from(java.lang.Boolean::class.java, BsonBoolean(b)))
                }
                BsonType.INT32 -> {
                    assertEquals(BsonInt32(i32), BsonConverter.to(i32))
                    assertEquals(i32, BsonConverter.from(Integer::class.java, BsonInt32(i32)))
                }
                BsonType.INT64 -> {
                    assertEquals(BsonInt64(i64), BsonConverter.to(i64))
                    assertEquals(i64, BsonConverter.from(java.lang.Long::class.java, BsonInt64(i64)))
                }
                BsonType.DECIMAL128 -> {
                    assertEquals(BsonDecimal128(d128), BsonConverter.to(d128))
                    assertEquals(oid, BsonConverter.from(ObjectId::class.java, BsonObjectId(oid)))
                }
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

        // To BSONValue
        assertEquals(bi32, BsonConverter.to(bi32))
        assertEquals(bOid, BsonConverter.to(bOid))
        assertEquals(bDoc, BsonConverter.to(bDoc))

        assertEquals(listOf(BsonBoolean(b), BsonInt32(i32), BsonInt64(i64)), BsonConverter.to(b, i32, i64))
        val list = listOf<Any>(BsonInt32(i32), BsonInt64(i64), BsonString(s))
        assertEquals(list, BsonConverter.to(list))

        // From BSONValue
        // BsonValue types are just passed as is
        assertEquals(BsonInt32(i32), BsonConverter.from(BsonInt32::class.java, BsonInt32(i32)))
        assertEquals(BsonInt64(i64), BsonConverter.from(BsonInt64::class.java, BsonInt64(i64)))
        assertEquals(BsonString(s),  BsonConverter.from(BsonString::class.java, BsonString(s)))

        // FIXME Howto auto box/wrap as Kotlin's primitive types are not assignable
        //  (isAssignablefrom) Java's auto boxed types
        // assertEquals(i32, BsonConverter.from(Int::class.java, BsonInt32(i32)))
        assertEquals(i32, BsonConverter.from(Integer::class.java, BsonInt32(i32)))

        // Not trying to fit wider types event though possible
        // FIXME Would we like to support this
        assertFailsWith<IllegalArgumentException> {
            BsonConverter.from(java.lang.Long::class.java, BsonInt32(i32))
        }
        assertFailsWith<IllegalArgumentException> {
            BsonConverter.from(Int::class.java, BsonInt64(i64))
        }

    }

}
