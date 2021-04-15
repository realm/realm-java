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

package io.realm

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.realm.*
import io.realm.entities.*
import io.realm.internal.core.NativeMixedCollection
import org.bson.types.Decimal128
import org.bson.types.ObjectId
import org.junit.*
import org.junit.runner.RunWith
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
@Ignore("RUNNING THESE TESTS MAKES OTHER TESTS FAIL.")
class MixedCollectionTests {
    private lateinit var realmConfiguration: RealmConfiguration
    private lateinit var realm: Realm

    @get:Rule
    val configFactory = TestRealmConfigurationFactory()

    init {
        Realm.init(InstrumentationRegistry.getInstrumentation().targetContext)
    }

    @Before
    fun setUp() {
        realmConfiguration = configFactory.createSchemaConfiguration(
                false,
                PrimaryKeyAsString::class.java)

        realm = Realm.getInstance(realmConfiguration)
    }

    @After
    fun tearDown() {
        realm.close()
    }

    @Test
    fun validateBoolean() {
        val collection = listOf(true, false, null, true)
        val nativeMixedCollection = NativeMixedCollection.newBooleanCollection(collection)

        assertEquals(collection.size, nativeMixedCollection.size)
        collection.forEachIndexed { index, expectedValue ->
            val nativeMixed = nativeMixedCollection.getItem(index)

            if (expectedValue == null) {
                assertEquals(MixedType.NULL, nativeMixed.type)
            } else {
                assertEquals(MixedType.BOOLEAN, nativeMixed.type)
                assertEquals(expectedValue, nativeMixed.asBoolean())
            }
        }
    }

    @Test
    fun validateByte() {
        val collection = listOf(1.toByte(), 2.toByte(), null, 5.toByte())
        val nativeMixedCollection = NativeMixedCollection.newIntegerCollection(collection)

        assertEquals(collection.size, nativeMixedCollection.size)
        collection.forEachIndexed { index, expectedValue ->
            val nativeMixed = nativeMixedCollection.getItem(index)

            if (expectedValue == null) {
                assertEquals(MixedType.NULL, nativeMixed.type)
            } else {
                assertEquals(MixedType.INTEGER, nativeMixed.type)
                assertEquals(expectedValue, nativeMixed.asLong().toByte())
            }
        }
    }

    @Test
    fun validateShort() {
        val collection = listOf(1.toShort(), 2.toShort(), null, 5.toShort())
        val nativeMixedCollection = NativeMixedCollection.newIntegerCollection(collection)

        assertEquals(collection.size, nativeMixedCollection.size)
        collection.forEachIndexed { index, expectedValue ->
            val nativeMixed = nativeMixedCollection.getItem(index)

            if (expectedValue == null) {
                assertEquals(MixedType.NULL, nativeMixed.type)
            } else {
                assertEquals(MixedType.INTEGER, nativeMixed.type)
                assertEquals(expectedValue, nativeMixed.asLong().toShort())
            }
        }
    }

    @Test
    fun validateInteger() {
        val collection = listOf(2, 3, null, 5)
        val nativeMixedCollection = NativeMixedCollection.newIntegerCollection(collection)

        assertEquals(collection.size, nativeMixedCollection.size)
        collection.forEachIndexed { index, expectedValue ->
            val nativeMixed = nativeMixedCollection.getItem(index)

            if (expectedValue == null) {
                assertEquals(MixedType.NULL, nativeMixed.type)
            } else {
                assertEquals(MixedType.INTEGER, nativeMixed.type)
                assertEquals(expectedValue, nativeMixed.asLong().toInt())
            }
        }
    }

    @Test
    fun validateLong() {
        val collection = listOf(2.toLong(), 3.toLong(), null, 5.toLong())
        val nativeMixedCollection = NativeMixedCollection.newIntegerCollection(collection)

        assertEquals(collection.size, nativeMixedCollection.size)
        collection.forEachIndexed { index, expectedValue ->
            val nativeMixed = nativeMixedCollection.getItem(index)

            if (expectedValue == null) {
                assertEquals(MixedType.NULL, nativeMixed.type)
            } else {
                assertEquals(MixedType.INTEGER, nativeMixed.type)
                assertEquals(expectedValue, nativeMixed.asLong())
            }
        }
    }

    @Test
    fun validateFloat() {
        val collection = listOf(1.4.toFloat(), 2.1.toFloat(), null, 5.5.toFloat())
        val nativeMixedCollection = NativeMixedCollection.newFloatCollection(collection)

        assertEquals(collection.size, nativeMixedCollection.size)
        collection.forEachIndexed { index, expectedValue ->
            val nativeMixed = nativeMixedCollection.getItem(index)

            if (expectedValue == null) {
                assertEquals(MixedType.NULL, nativeMixed.type)
            } else {
                assertEquals(MixedType.FLOAT, nativeMixed.type)
                assertEquals(expectedValue, nativeMixed.asFloat().toFloat())
            }
        }
    }

    @Test
    fun validateDouble() {
        val collection = listOf(1.4, 2.1, null, 5.5)
        val nativeMixedCollection = NativeMixedCollection.newDoubleCollection(collection)

        assertEquals(collection.size, nativeMixedCollection.size)
        collection.forEachIndexed { index, expectedValue ->
            val nativeMixed = nativeMixedCollection.getItem(index)

            if (expectedValue == null) {
                assertEquals(MixedType.NULL, nativeMixed.type)
            } else {
                assertEquals(MixedType.DOUBLE, nativeMixed.type)
                assertEquals(expectedValue, nativeMixed.asDouble())
            }
        }
    }

    @Test
    fun validateString() {
        val collection = listOf(UUID.randomUUID().toString(), UUID.randomUUID().toString(), null, UUID.randomUUID().toString())
        val nativeMixedCollection = NativeMixedCollection.newStringCollection(collection)

        assertEquals(collection.size, nativeMixedCollection.size)
        collection.forEachIndexed { index, expectedValue ->
            val nativeMixed = nativeMixedCollection.getItem(index)

            if (expectedValue == null) {
                assertEquals(MixedType.NULL, nativeMixed.type)
            } else {
                assertEquals(MixedType.STRING, nativeMixed.type)
                assertEquals(expectedValue, nativeMixed.asString())
            }
        }
    }

    @Test
    fun validateBinary() {
        val collection = listOf(
                byteArrayOf(1, 1, 0),
                byteArrayOf(0, 1, 0),
                null,
                byteArrayOf(0, 1, 1)
        )
        val nativeMixedCollection = NativeMixedCollection.newBinaryCollection(collection)

        assertEquals(collection.size, nativeMixedCollection.size)
        collection.forEachIndexed { index, expectedValue ->
            val nativeMixed = nativeMixedCollection.getItem(index)

            if (expectedValue == null) {
                assertEquals(MixedType.NULL, nativeMixed.type)
            } else {
                assertEquals(MixedType.BINARY, nativeMixed.type)
                assertTrue(Arrays.equals(expectedValue, nativeMixed.asBinary()))
            }
        }
    }

    @Test
    fun validateDate() {
        val collection = listOf(Date(1), Date(2), null, Date(5))
        val nativeMixedCollection = NativeMixedCollection.newDateCollection(collection)

        assertEquals(collection.size, nativeMixedCollection.size)
        collection.forEachIndexed { index, expectedValue ->
            val nativeMixed = nativeMixedCollection.getItem(index)

            if (expectedValue == null) {
                assertEquals(MixedType.NULL, nativeMixed.type)
            } else {
                assertEquals(MixedType.DATE, nativeMixed.type)
                assertEquals(expectedValue, nativeMixed.asDate())
            }
        }
    }

    @Test
    fun validateObjectId() {
        val collection = listOf(ObjectId(Date(1)), ObjectId(Date(2)), null, ObjectId(Date(5)))
        val nativeMixedCollection = NativeMixedCollection.newObjectIdCollection(collection)

        assertEquals(collection.size, nativeMixedCollection.size)
        collection.forEachIndexed { index, expectedValue ->
            val nativeMixed = nativeMixedCollection.getItem(index)

            if (expectedValue == null) {
                assertEquals(MixedType.NULL, nativeMixed.type)
            } else {
                assertEquals(MixedType.OBJECT_ID, nativeMixed.type)
                assertEquals(expectedValue, nativeMixed.asObjectId())
            }
        }
    }

    @Test
    fun validateDecimal128() {
        val collection = listOf(Decimal128(1), Decimal128(2), null, Decimal128(5))
        val nativeMixedCollection = NativeMixedCollection.newDecimal128Collection(collection)

        assertEquals(collection.size, nativeMixedCollection.size)
        collection.forEachIndexed { index, expectedValue ->
            val nativeMixed = nativeMixedCollection.getItem(index)

            if (expectedValue == null) {
                assertEquals(MixedType.NULL, nativeMixed.type)
            } else {
                assertEquals(MixedType.DECIMAL128, nativeMixed.type)
                assertEquals(expectedValue, nativeMixed.asDecimal128())
            }
        }
    }

    @Test
    fun validateUUID() {
        val collection = listOf(UUID.randomUUID(), UUID.randomUUID(), null, UUID.randomUUID())
        val nativeMixedCollection = NativeMixedCollection.newUUIDCollection(collection)

        assertEquals(collection.size, nativeMixedCollection.size)
        collection.forEachIndexed { index, expectedValue ->
            val nativeMixed = nativeMixedCollection.getItem(index)

            if (expectedValue == null) {
                assertEquals(MixedType.NULL, nativeMixed.type)
            } else {
                assertEquals(MixedType.UUID, nativeMixed.type)
                assertEquals(expectedValue, nativeMixed.asUUID())
            }
        }
    }

    @Test
    fun validateEmpty() {
        val collection = listOf<Double?>()
        val nativeMixedCollection = NativeMixedCollection.newDoubleCollection(collection)
        assertEquals(collection.size, nativeMixedCollection.size)
    }

    @Test
    fun validateRealmModel() {
        var collection = mutableListOf<PrimaryKeyAsString?>()

        realm.executeTransaction {
            val managedObjects = it.copyToRealmOrUpdate(listOf(
                    PrimaryKeyAsString(UUID.randomUUID().toString(), 0),
                    PrimaryKeyAsString(UUID.randomUUID().toString(), 0),
                    PrimaryKeyAsString(UUID.randomUUID().toString(), 0)
            ))

            collection.addAll(managedObjects)
            collection.add(2, null)
        }

        val nativeMixedCollection = NativeMixedCollection.newRealmModelCollection(collection)

        assertEquals(collection.size, nativeMixedCollection.size)
        collection.forEachIndexed { index, expectedValue ->
            val nativeMixed = nativeMixedCollection.getItem(index)

            if (expectedValue == null) {
                assertEquals(MixedType.NULL, nativeMixed.type)
            } else {
                val mixed = Mixed(MixedOperator.fromNativeMixed(realm, nativeMixed))
                assertEquals(MixedType.OBJECT, mixed.type)
                assertEquals(expectedValue.name, mixed.asRealmModel(PrimaryKeyAsString::class.java).name)
            }
        }
    }
}
