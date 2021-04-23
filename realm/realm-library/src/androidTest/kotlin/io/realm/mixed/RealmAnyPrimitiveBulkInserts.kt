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

package io.realm.realmany

import androidx.test.platform.app.InstrumentationRegistry
import io.realm.*
import io.realm.entities.*
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import org.bson.types.Decimal128
import org.bson.types.ObjectId
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(Parameterized::class)
class RealmAnyPrimitivesBulkInsertsTests(
        private val testingType: RealmAnyType,
        private val first: RealmAny,
        private val second: RealmAny
) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun data(): MutableList<Array<Any>> {
            val list = mutableListOf<Array<Any>>()

            for (type in RealmAnyType.values()) {
                when (type) {
                    RealmAnyType.INTEGER -> {
                        list.add(arrayOf(RealmAnyType.INTEGER,
                                RealmAny.valueOf(1.toByte()),
                                RealmAny.valueOf(10.toByte())
                        ))
                        list.add(arrayOf(RealmAnyType.INTEGER,
                                RealmAny.valueOf(2.toShort()),
                                RealmAny.valueOf(20.toShort())
                        ))
                        list.add(arrayOf(RealmAnyType.INTEGER,
                                RealmAny.valueOf(3.toInt()),
                                RealmAny.valueOf(30.toInt())
                        ))
                        list.add(arrayOf(RealmAnyType.INTEGER,
                                RealmAny.valueOf(4.toLong()),
                                RealmAny.valueOf(40.toLong())
                        ))
                    }
                    RealmAnyType.BOOLEAN -> list.add(arrayOf(
                            RealmAnyType.BOOLEAN,
                            RealmAny.valueOf(false),
                            RealmAny.valueOf(true)
                    ))
                    RealmAnyType.STRING -> list.add(arrayOf(
                            RealmAnyType.STRING,
                            RealmAny.valueOf("hello world1"),
                            RealmAny.valueOf("hello world2")
                    ))
                    RealmAnyType.BINARY -> list.add(arrayOf(
                            RealmAnyType.BINARY,
                            RealmAny.valueOf(byteArrayOf(0, 1, 0)),
                            RealmAny.valueOf(byteArrayOf(0, 1, 1))
                    ))
                    RealmAnyType.DATE -> list.add(arrayOf(
                            RealmAnyType.DATE,
                            RealmAny.valueOf(Date(0)),
                            RealmAny.valueOf(Date(10))
                    ))
                    RealmAnyType.FLOAT -> list.add(arrayOf(
                            RealmAnyType.FLOAT,
                            RealmAny.valueOf(5.toFloat()),
                            RealmAny.valueOf(50.toFloat())
                    ))
                    RealmAnyType.DOUBLE -> list.add(arrayOf(
                            RealmAnyType.DOUBLE,
                            RealmAny.valueOf(6.toDouble()),
                            RealmAny.valueOf(60.toDouble())
                    ))
                    RealmAnyType.DECIMAL128 -> list.add(arrayOf(
                            RealmAnyType.DECIMAL128,
                            RealmAny.valueOf(Decimal128(1)),
                            RealmAny.valueOf(Decimal128(10))
                    ))
                    RealmAnyType.OBJECT_ID -> list.add(arrayOf(
                            RealmAnyType.OBJECT_ID,
                            RealmAny.valueOf(ObjectId(Date(10))),
                            RealmAny.valueOf(ObjectId(Date(100)))
                    ))
                    RealmAnyType.UUID -> list.add(arrayOf(
                            RealmAnyType.UUID,
                            RealmAny.valueOf(UUID.randomUUID()),
                            RealmAny.valueOf(UUID.randomUUID())
                    ))
                    RealmAnyType.OBJECT,   // Not tested in this test suite
                    RealmAnyType.NULL
                    -> { // Not tested directly
                    }

                    else -> throw AssertionError("Missing case for type: ${type.name}")
                }
            }

            return list
        }
    }

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
                RealmAnyNotIndexed::class.java,
                RealmAnyIndexed::class.java,
                AllJavaTypes::class.java,
                RealmAnyRealmListWithPK::class.java,
                RealmAnyNotIndexedWithPK::class.java,
                PrimaryKeyAsString::class.java)

        realm = Realm.getInstance(realmConfiguration)
    }

    @After
    fun tearDown() {
        realm.close()
    }

    @Test
    fun copyFromRealm() {
        realm.beginTransaction()
        val value = realm.createObject<RealmAnyNotIndexedWithPK>(0)
        value.realmAny = first
        realm.commitTransaction()

        val copy = realm.copyFromRealm(value)

        if (testingType == RealmAnyType.BINARY) {
            assertTrue(Arrays.equals(first.asBinary(), copy.realmAny!!.asBinary()))
        } else {
            assertEquals(first, copy.realmAny)
        }
    }

    @Test
    fun copyToRealm_primitive() {
        val value = RealmAnyNotIndexedWithPK()
        value.realmAny = first

        realm.beginTransaction()
        val managedValue = realm.copyToRealm(value)
        realm.commitTransaction()

        if (testingType == RealmAnyType.BINARY) {
            assertTrue(Arrays.equals(first.asBinary(), managedValue.realmAny!!.asBinary()))
        } else {
            assertEquals(first, managedValue.realmAny)
        }
    }


    @Test
    fun copyToRealmOrUpdate_primitive() {
        realm.executeTransaction { realm ->
            val obj = realm.createObject<RealmAnyNotIndexedWithPK>(0)
            obj.realmAny = first
        }

        val value = RealmAnyNotIndexedWithPK()
        value.realmAny = second

        realm.beginTransaction()
        val managedValue = realm.copyToRealmOrUpdate(value)
        realm.commitTransaction()

        if (testingType == RealmAnyType.BINARY) {
            assertTrue(Arrays.equals(second.asBinary(), managedValue.realmAny!!.asBinary()))
        } else {
            assertEquals(second, managedValue.realmAny!!)
        }
    }

    @Test
    fun insert_primitive() {
        realm.executeTransaction { realm ->
            val value = RealmAnyNotIndexedWithPK()
            value.realmAny = first

            realm.insert(value)
        }

        val managedValue = realm.where<RealmAnyNotIndexedWithPK>().findFirst()!!

        if (testingType == RealmAnyType.BINARY) {
            assertTrue(Arrays.equals(first.asBinary(), managedValue.realmAny!!.asBinary()))
        } else {
            assertEquals(first, managedValue.realmAny)
        }
    }

    @Test
    fun insertOrUpdate_primitive() {
        realm.executeTransaction { realm ->
            val value = realm.createObject<RealmAnyNotIndexedWithPK>(0)
            value.realmAny = first
        }

        realm.executeTransaction { realm ->
            val value = RealmAnyNotIndexedWithPK()
            value.realmAny = second

            realm.insertOrUpdate(value)
        }

        val all = realm.where<RealmAnyNotIndexedWithPK>().findAll()

        assertEquals(1, all.size)

        if (testingType == RealmAnyType.BINARY) {
            assertTrue(Arrays.equals(second.asBinary(), all[0]!!.realmAny!!.asBinary()))
        } else {
            assertEquals(second, all[0]!!.realmAny)
        }
    }
}
