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
import org.junit.Test
import kotlin.test.assertFailsWith

class BsonTest {

    /**
     * Simple test to verify semantics of org.bson JSON encoding and decoding.
     */
    @Test
    fun test_bson_roundtrip() {
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
    fun test_bson_conversion() {
        val values = BsonConverter.to(32, 43L, "Realm")
        assertEquals(listOf(BsonInt32(32), BsonInt64(43), BsonString("Realm")), values)

        assertEquals(32, BsonConverter.from(Integer::class.java, BsonInt32(32)))

        // FIXME Do we need to add Kotlin extensions to work around having to reference java types explicitly

        assertEquals(32L, BsonConverter.from(java.lang.Long::class.java, BsonInt64(32)))
        assertEquals("Realm", BsonConverter.from(java.lang.String::class.java, BsonString("Realm")))
        assertFailsWith<java.lang.UnsupportedOperationException> {
            BsonConverter.from(Long::class.java, BsonInt32(32))
        }
        assertFailsWith<java.lang.UnsupportedOperationException> {
            BsonConverter.from(Long::class.java, BsonInt32(32))
        }

        // FIXME Add tests for BsonConverter.bsontype
    }

}