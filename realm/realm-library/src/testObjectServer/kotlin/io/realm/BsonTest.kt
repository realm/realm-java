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
import org.bson.types.ObjectId
import org.junit.Assert.assertEquals
import org.junit.Ignore
import org.junit.Test
import kotlin.test.assertFailsWith

class BsonTest {

    /**
     * Simple test to verify semantics of org.bson JSON encoding and decoding.
     */
    @Ignore("Only for Bson API evaluation, not testing Realm functionality")
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
        val s = "Realm"
        val oid = ObjectId()

        val bi32 = BsonInt32(15)
        val bOid = BsonObjectId(oid)
        val bDoc = BsonDocument()

        val values = BsonConverter.to(b, i32, i64, s, bi32, bOid, bDoc)
        assertEquals(listOf(BsonBoolean(b), BsonInt32(i32), BsonInt64(i64), BsonString(s), bi32, bOid, bDoc), values)

        // BsonValue types are just passed as is
        assertEquals(BsonInt32(i32), BsonConverter.from(BsonInt32::class.java, BsonInt32(i32)))
        assertEquals(BsonInt64(i64), BsonConverter.from(BsonInt64::class.java, BsonInt64(i64)))
        assertEquals(BsonString(s),  BsonConverter.from(BsonString::class.java, BsonString(s)))

        // Native types are converted directly from BsonValue to equivalent type
        // FIXME Howto auto box/wrap as Kotlin's primitive types are not assignable
        //  (isAssignablefrom) Java's auto boxed types
        assertEquals(b, BsonConverter.from(java.lang.Boolean::class.java, BsonBoolean(b)))
        // assertEquals(i32, BsonConverter.from(Int::class.java, BsonInt32(i32)))
        assertEquals(i32, BsonConverter.from(Integer::class.java, BsonInt32(i32)))
        // assertEquals(i64, BsonConverter.from(Long::class.java, BsonInt64(i64)))
        assertEquals(i64, BsonConverter.from(java.lang.Long::class.java, BsonInt64(i64)))
        // ...not trying to fit wider types event though possible
        // FIXME Would we like to support this
        assertFailsWith<IllegalArgumentException> {
            BsonConverter.from(java.lang.Long::class.java, BsonInt32(i32))
        }
        assertFailsWith<IllegalArgumentException> {
            BsonConverter.from(Int::class.java, BsonInt64(i64))
        }
        assertEquals(s, BsonConverter.from(String::class.java, BsonString(s)))

        // FIXME Do we actually want to unwrap all of the BsonValues?
        //assertEquals(oid, BsonConverter.from(ObjectId::class.java, bOid))

    }

}
