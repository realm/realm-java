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

package io.realm.internal.jni

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.realm.Realm
import io.realm.RealmAppConfiguration.Builder.DEFAULT_BSON_CODEC_REGISTRY
import org.bson.*
import org.bson.types.Decimal128
import org.bson.types.ObjectId
import org.hamcrest.core.IsEqual
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ErrorCollector
import org.junit.runner.RunWith
import java.lang.Double
import java.lang.Float
import java.lang.Long
import java.util.*

@RunWith(AndroidJUnit4::class)
class JniBsonProtocolTests {

    // All errors will be collected and reported at end of the test. Full error lists are not
    // displayed in Android Studio but can be seen in logcat
    @get:Rule
    val collector = ErrorCollector()

    @Before
    fun setup() {
        Realm.init(InstrumentationRegistry.getInstrumentation().targetContext)
    }

    @Test
    fun allTypes() {
        // Build list of all types for testing embedding in list and documents later
        val allBsonTypes = mutableListOf<Any>()

        for (type in BsonType.values()) {

            try {
                when (type) {
                    BsonType.DOUBLE -> {
                        collector.checkThat(JniBsonProtocol.roundtrip(1.4f, Float::class.java, DEFAULT_BSON_CODEC_REGISTRY).toFloat(), IsEqual(1.4f));
                        collector.checkThat(JniBsonProtocol.roundtrip(1.4, Double::class.java, DEFAULT_BSON_CODEC_REGISTRY).toDouble(), IsEqual(1.4));
                        val value = BsonDouble(1.4)
                        checkEquals(value, BsonDouble::class.java)
                        allBsonTypes.add(value)
                    }
                    BsonType.STRING -> {
                        checkEquals("Realm", String::class.java)
                        val value = BsonString("Realm")
                        checkEquals(value, BsonString::class.java)
                        allBsonTypes.add(value)
                    }
                    BsonType.ARRAY -> {
                        val listValues = listOf<Any>(true, 32, 32L)
                        checkEquals(listValues, List::class.java)
                        val value = BsonArray(listOf(BsonInt32(1), BsonString("Realm"), BsonDocument("KEY", BsonInt32(5))))
                        checkEquals(value, BsonArray::class.java)
                        allBsonTypes.add(value)

                        // Previously failing
                        val values2 = listOf(listOf(1, true, 3))
                        checkEquals(values2,List::class.java)
                        val values3 = listOf(listOf(1, "Realm", 3))
                        checkEquals(values3,List::class.java)
                    }
                    BsonType.BINARY -> {
                        val value = byteArrayOf(1, 2, 3)
                        collector.checkThat(call(value, ByteArray::class.java).toList(), IsEqual(value.toList()));
                        val value1 = BsonBinary(UUID.randomUUID())
                        checkEquals(value1, BsonBinary::class.java)
                        allBsonTypes.add(value1)
                    }
                    BsonType.OBJECT_ID -> {
                        checkEquals(ObjectId(), ObjectId::class.java)
                        val value = BsonObjectId(ObjectId())
                        checkEquals(value, BsonObjectId::class.java)
                        allBsonTypes.add(value)
                    }
                    BsonType.BOOLEAN -> {
                        val value: Boolean = true
                        collector.checkThat(JniBsonProtocol.roundtrip(value, java.lang.Boolean::class.java, DEFAULT_BSON_CODEC_REGISTRY).booleanValue(), IsEqual(value));
                        val value1 = BsonBoolean(true)
                        checkEquals(value1, BsonBoolean::class.java)
                        allBsonTypes.add(value1)
                    }
                    BsonType.INT32 -> {
                        collector.checkThat(JniBsonProtocol.roundtrip(32, Integer::class.java, DEFAULT_BSON_CODEC_REGISTRY).toInt(), IsEqual(32))
                        collector.checkThat(JniBsonProtocol.roundtrip(32L, Integer::class.java, DEFAULT_BSON_CODEC_REGISTRY).toInt(), IsEqual(32))
                        val value = BsonInt32(32)
                        checkEquals(value, BsonInt32::class.java)
                        allBsonTypes.add(value)
                    }
                    BsonType.INT64 -> {
                        collector.checkThat(JniBsonProtocol.roundtrip(32, Long::class.java, DEFAULT_BSON_CODEC_REGISTRY).toLong(), IsEqual(32L))
                        collector.checkThat(JniBsonProtocol.roundtrip(32L, Integer::class.java, DEFAULT_BSON_CODEC_REGISTRY).toLong(), IsEqual(32L))
                        val value = BsonInt64(32)
                        checkEquals(value, BsonInt64::class.java)
                        allBsonTypes.add(value)
                    }
                    BsonType.DECIMAL128 -> {
                        checkEquals(Decimal128(32L), Decimal128::class.java)
                        val value = BsonDecimal128(Decimal128(32L))
                        checkEquals(value, BsonDecimal128::class.java)
                        allBsonTypes.add(value)
                    }
                    BsonType.DOCUMENT -> {
                        val map = mapOf("foo" to 5, "bar" to 6)
                        val document = Document(map)
                        checkEquals(map, Map::class.java)
                        checkEquals(document, Map::class.java)

                        // Previously failing in C++ parser
                        var documents = listOf(Document(), Document())
                        checkEquals(documents, List::class.java)
                        documents = listOf(Document("KEY", "VALUE"), Document("KEY", "VALUE"), Document("KEY", "VALUE"))
                        checkEquals(documents, List::class.java)
                        allBsonTypes.add(document)
                    }
                    BsonType.DATE_TIME  -> {
                        val value = Date(System.currentTimeMillis())
                        checkEquals(value, Date::class.java)
                        allBsonTypes.add(value)
                    }
                    BsonType.NULL  -> {
                        val value = BsonNull()
                        checkEquals(value, BsonNull::class.java)
                        allBsonTypes.add(value)
                    }
                    BsonType.UNDEFINED  -> {
                        val value = BsonUndefined()
                        checkEquals(value, BsonUndefined::class.java)
                        allBsonTypes.add(value)
                    }
                    BsonType.MAX_KEY -> {
                        val value = BsonMaxKey()
                        checkEquals(value, BsonMaxKey::class.java)
                        allBsonTypes.add(value)
                    }
                    BsonType.MIN_KEY -> {
                        val value = BsonMinKey()
                        checkEquals(value, BsonMinKey::class.java)
                        allBsonTypes.add(value)
                    }
                    BsonType.TIMESTAMP -> {
                        val value = BsonTimestamp(1000, 1000)
                        checkEquals(value, BsonTimestamp::class.java)
                        allBsonTypes.add(value)
                    }
                    BsonType.SYMBOL -> {
                        val value = BsonSymbol("Realm")
                        checkEquals(value, BsonSymbol::class.java)
                        allBsonTypes.add(value)
                    }
                    BsonType.REGULAR_EXPRESSION -> {
                        val value = BsonRegularExpression("Realm")
                        checkEquals(value, BsonRegularExpression::class.java)
                        allBsonTypes.add(value)
                    }
                    BsonType.DB_POINTER -> {
                        val value = BsonDbPointer("Realm", ObjectId())
                        checkEquals(value, BsonDbPointer::class.java)
                        allBsonTypes.add(value)
                    }
                    BsonType.JAVASCRIPT -> {
                        val value = BsonJavaScript("Realm")
                        checkEquals(value, BsonJavaScript::class.java)
                        allBsonTypes.add(value)
                    }
                    BsonType.JAVASCRIPT_WITH_SCOPE -> {
                        val value = BsonJavaScriptWithScope("Realm", BsonDocument("key", BsonInt32(3)))
                        checkEquals(value, BsonJavaScriptWithScope::class.java)
                        allBsonTypes.add(value)
                    }
                    BsonType.END_OF_DOCUMENT -> {
                        // Don't think this is a value BsonType
                    }
                    else ->
                        fail()
                }
            } catch (e: Exception) {
                collector.addError(e)
            }
        }
        // Verify that all types can be embedded in a list
        try {
            checkEquals(allBsonTypes, List::class.java)
        } catch (e : Exception) {
            collector.addError(e)
        }
        // Verify that all types can be embeded in a document
        try {
            checkEquals(Document(allBsonTypes.mapIndexed { index, any -> Pair("key$index", any) }.toMap()), Document::class.java)
        } catch (e : Exception) {
            collector.addError(e)
        }
    }

    // Wrapper around JniBsonProtocol call to make test code more condensed
    private fun <T> call(value: T, clz: Class<T>) : T {
        return JniBsonProtocol.roundtrip(value, clz, DEFAULT_BSON_CODEC_REGISTRY)
    }

    // Utility method to call and verify that output is equal to input and append errors to the collector
    private fun <T> checkEquals(value: T, clz: Class<T>) {
        collector.checkThat(call(value, clz), IsEqual(value));
    }

}
