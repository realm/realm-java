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
import io.realm.internal.core.NativeRealmAnyCollection
import org.bson.types.Decimal128
import org.bson.types.ObjectId
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*
import kotlin.random.Random
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class RealmAnyCollectionTests {
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
            PrimaryKeyAsString::class.java,
            WithRealmAnyTestEntity::class.java
        )

        realm = Realm.getInstance(realmConfiguration)
    }

    @After
    fun tearDown() {
        realm.close()
    }

    @Test
    fun accessAnyElement() {
        realm.executeTransaction {
            realm.createObject(WithRealmAnyTestEntity::class.java, 1).apply {
                items = RealmList<RealmAny>(RealmAny.valueOf(1L))
            }
        }
        val results: RealmResults<WithRealmAnyTestEntity> = realm.where(WithRealmAnyTestEntity::class.java).findAll()
        val entity = results[0]!!
        val realmAny = entity.items[0]!!
        assertEquals(RealmAny.Type.INTEGER, realmAny.type)
        assertEquals(1, realmAny.asInteger())
    }

    @Test
    fun validateBoolean() {
        val collection = listOf(true, false, null, true)
        val nativeRealmAnyCollection = NativeRealmAnyCollection.newBooleanCollection(collection)

        assertEquals(collection.size, nativeRealmAnyCollection.size)
        collection.forEachIndexed { index, expectedValue ->
            val nativeRealmAny = nativeRealmAnyCollection.getItem(index)

            if (expectedValue == null) {
                assertEquals(RealmAny.Type.NULL, nativeRealmAny.type)
            } else {
                assertEquals(RealmAny.Type.BOOLEAN, nativeRealmAny.type)
                assertEquals(expectedValue, nativeRealmAny.asBoolean())
            }
        }
    }

    @Test
    fun validateByte() {
        val collection = listOf(1.toByte(), 2.toByte(), null, 5.toByte())
        val nativeRealmAnyCollection = NativeRealmAnyCollection.newIntegerCollection(collection)

        assertEquals(collection.size, nativeRealmAnyCollection.size)
        collection.forEachIndexed { index, expectedValue ->
            val nativeRealmAny = nativeRealmAnyCollection.getItem(index)

            if (expectedValue == null) {
                assertEquals(RealmAny.Type.NULL, nativeRealmAny.type)
            } else {
                assertEquals(RealmAny.Type.INTEGER, nativeRealmAny.type)
                assertEquals(expectedValue, nativeRealmAny.asLong().toByte())
            }
        }
    }

    @Test
    fun validateShort() {
        val collection = listOf(1.toShort(), 2.toShort(), null, 5.toShort())
        val nativeRealmAnyCollection = NativeRealmAnyCollection.newIntegerCollection(collection)

        assertEquals(collection.size, nativeRealmAnyCollection.size)
        collection.forEachIndexed { index, expectedValue ->
            val nativeRealmAny = nativeRealmAnyCollection.getItem(index)

            if (expectedValue == null) {
                assertEquals(RealmAny.Type.NULL, nativeRealmAny.type)
            } else {
                assertEquals(RealmAny.Type.INTEGER, nativeRealmAny.type)
                assertEquals(expectedValue, nativeRealmAny.asLong().toShort())
            }
        }
    }

    @Test
    fun validateInteger() {
        val collection = listOf(2, 3, null, 5)
        val nativeRealmAnyCollection = NativeRealmAnyCollection.newIntegerCollection(collection)

        assertEquals(collection.size, nativeRealmAnyCollection.size)
        collection.forEachIndexed { index, expectedValue ->
            val nativeRealmAny = nativeRealmAnyCollection.getItem(index)

            if (expectedValue == null) {
                assertEquals(RealmAny.Type.NULL, nativeRealmAny.type)
            } else {
                assertEquals(RealmAny.Type.INTEGER, nativeRealmAny.type)
                assertEquals(expectedValue, nativeRealmAny.asLong().toInt())
            }
        }
    }

    @Test
    fun validateLong() {
        val collection = listOf(2.toLong(), 3.toLong(), null, 5.toLong())
        val nativeRealmAnyCollection = NativeRealmAnyCollection.newIntegerCollection(collection)

        assertEquals(collection.size, nativeRealmAnyCollection.size)
        collection.forEachIndexed { index, expectedValue ->
            val nativeRealmAny = nativeRealmAnyCollection.getItem(index)

            if (expectedValue == null) {
                assertEquals(RealmAny.Type.NULL, nativeRealmAny.type)
            } else {
                assertEquals(RealmAny.Type.INTEGER, nativeRealmAny.type)
                assertEquals(expectedValue, nativeRealmAny.asLong())
            }
        }
    }

    @Test
    fun validateFloat() {
        val collection = listOf(1.4.toFloat(), 2.1.toFloat(), null, 5.5.toFloat())
        val nativeRealmAnyCollection = NativeRealmAnyCollection.newFloatCollection(collection)

        assertEquals(collection.size, nativeRealmAnyCollection.size)
        collection.forEachIndexed { index, expectedValue ->
            val nativeRealmAny = nativeRealmAnyCollection.getItem(index)

            if (expectedValue == null) {
                assertEquals(RealmAny.Type.NULL, nativeRealmAny.type)
            } else {
                assertEquals(RealmAny.Type.FLOAT, nativeRealmAny.type)
                assertEquals(expectedValue, nativeRealmAny.asFloat().toFloat())
            }
        }
    }

    @Test
    fun validateDouble() {
        val collection = listOf(1.4, 2.1, null, 5.5)
        val nativeRealmAnyCollection = NativeRealmAnyCollection.newDoubleCollection(collection)

        assertEquals(collection.size, nativeRealmAnyCollection.size)
        collection.forEachIndexed { index, expectedValue ->
            val nativeRealmAny = nativeRealmAnyCollection.getItem(index)

            if (expectedValue == null) {
                assertEquals(RealmAny.Type.NULL, nativeRealmAny.type)
            } else {
                assertEquals(RealmAny.Type.DOUBLE, nativeRealmAny.type)
                assertEquals(expectedValue, nativeRealmAny.asDouble())
            }
        }
    }

    @Test
    fun validateString() {
        val collection = listOf(UUID.randomUUID().toString(), UUID.randomUUID().toString(), null, UUID.randomUUID().toString())
        val nativeRealmAnyCollection = NativeRealmAnyCollection.newStringCollection(collection)

        assertEquals(collection.size, nativeRealmAnyCollection.size)
        collection.forEachIndexed { index, expectedValue ->
            val nativeRealmAny = nativeRealmAnyCollection.getItem(index)

            if (expectedValue == null) {
                assertEquals(RealmAny.Type.NULL, nativeRealmAny.type)
            } else {
                assertEquals(RealmAny.Type.STRING, nativeRealmAny.type)
                assertEquals(expectedValue, nativeRealmAny.asString())
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
        val nativeRealmAnyCollection = NativeRealmAnyCollection.newBinaryCollection(collection)

        assertEquals(collection.size, nativeRealmAnyCollection.size)
        collection.forEachIndexed { index, expectedValue ->
            val nativeRealmAny = nativeRealmAnyCollection.getItem(index)

            if (expectedValue == null) {
                assertEquals(RealmAny.Type.NULL, nativeRealmAny.type)
            } else {
                assertEquals(RealmAny.Type.BINARY, nativeRealmAny.type)
                assertTrue(Arrays.equals(expectedValue, nativeRealmAny.asBinary()))
            }
        }
    }

    @Test
    fun validateDate() {
        val collection = listOf(Date(1), Date(2), null, Date(5))
        val nativeRealmAnyCollection = NativeRealmAnyCollection.newDateCollection(collection)

        assertEquals(collection.size, nativeRealmAnyCollection.size)
        collection.forEachIndexed { index, expectedValue ->
            val nativeRealmAny = nativeRealmAnyCollection.getItem(index)

            if (expectedValue == null) {
                assertEquals(RealmAny.Type.NULL, nativeRealmAny.type)
            } else {
                assertEquals(RealmAny.Type.DATE, nativeRealmAny.type)
                assertEquals(expectedValue, nativeRealmAny.asDate())
            }
        }
    }

    @Test
    fun validateObjectId() {
        val collection = listOf(ObjectId(Date(1)), ObjectId(Date(2)), null, ObjectId(Date(5)))
        val nativeRealmAnyCollection = NativeRealmAnyCollection.newObjectIdCollection(collection)

        assertEquals(collection.size, nativeRealmAnyCollection.size)
        collection.forEachIndexed { index, expectedValue ->
            val nativeRealmAny = nativeRealmAnyCollection.getItem(index)

            if (expectedValue == null) {
                assertEquals(RealmAny.Type.NULL, nativeRealmAny.type)
            } else {
                assertEquals(RealmAny.Type.OBJECT_ID, nativeRealmAny.type)
                assertEquals(expectedValue, nativeRealmAny.asObjectId())
            }
        }
    }

    @Test
    fun validateDecimal128() {
        val collection = listOf(Decimal128(1), Decimal128(2), null, Decimal128(5))
        val nativeRealmAnyCollection = NativeRealmAnyCollection.newDecimal128Collection(collection)

        assertEquals(collection.size, nativeRealmAnyCollection.size)
        collection.forEachIndexed { index, expectedValue ->
            val nativeRealmAny = nativeRealmAnyCollection.getItem(index)

            if (expectedValue == null) {
                assertEquals(RealmAny.Type.NULL, nativeRealmAny.type)
            } else {
                assertEquals(RealmAny.Type.DECIMAL128, nativeRealmAny.type)
                assertEquals(expectedValue, nativeRealmAny.asDecimal128())
            }
        }
    }

    @Test
    fun validateUUID() {
        val collection = listOf(UUID.randomUUID(), UUID.randomUUID(), null, UUID.randomUUID())
        val nativeRealmAnyCollection = NativeRealmAnyCollection.newUUIDCollection(collection)

        assertEquals(collection.size, nativeRealmAnyCollection.size)
        collection.forEachIndexed { index, expectedValue ->
            val nativeRealmAny = nativeRealmAnyCollection.getItem(index)

            if (expectedValue == null) {
                assertEquals(RealmAny.Type.NULL, nativeRealmAny.type)
            } else {
                assertEquals(RealmAny.Type.UUID, nativeRealmAny.type)
                assertEquals(expectedValue, nativeRealmAny.asUUID())
            }
        }
    }

    @Test
    fun validateEmpty() {
        val collection = listOf<Double?>()
        val nativeRealmAnyCollection = NativeRealmAnyCollection.newDoubleCollection(collection)
        assertEquals(collection.size, nativeRealmAnyCollection.size)
    }

    @Test
    fun validateRealmModel() {
        val collection = mutableListOf<PrimaryKeyAsString?>()

        realm.executeTransaction {
            val managedObjects = it.copyToRealmOrUpdate(listOf(
                    PrimaryKeyAsString(UUID.randomUUID().toString(), 0),
                    PrimaryKeyAsString(UUID.randomUUID().toString(), 0),
                    PrimaryKeyAsString(UUID.randomUUID().toString(), 0)
            ))

            collection.addAll(managedObjects)
            collection.add(2, null)
        }

        val nativeRealmAnyCollection = NativeRealmAnyCollection.newRealmModelCollection(collection)

        assertEquals(collection.size, nativeRealmAnyCollection.size)
        collection.forEachIndexed { index, expectedValue ->
            val nativeRealmAny = nativeRealmAnyCollection.getItem(index)

            if (expectedValue == null) {
                assertEquals(RealmAny.Type.NULL, nativeRealmAny.type)
            } else {
                val realmAny = RealmAny(RealmAnyOperator.fromNativeRealmAny(realm, nativeRealmAny))
                assertEquals(RealmAny.Type.OBJECT, realmAny.type)
                assertEquals(expectedValue.name, realmAny.asRealmModel(PrimaryKeyAsString::class.java).name)
            }
        }
    }
}
