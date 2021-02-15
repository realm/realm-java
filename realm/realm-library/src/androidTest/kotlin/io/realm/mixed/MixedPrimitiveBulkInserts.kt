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

package io.realm.mixed

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
class MixedPrimitivesBulkInsertsTests(
        private val testingType: MixedType,
        private val first: Mixed,
        private val second: Mixed
) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun data(): MutableList<Array<Any>> {
            val list = mutableListOf<Array<Any>>()

            for (type in MixedType.values()) {
                when (type) {
                    MixedType.INTEGER -> {
                        list.add(arrayOf(MixedType.INTEGER,
                                Mixed.valueOf(1.toByte()),
                                Mixed.valueOf(10.toByte())
                        ))
                        list.add(arrayOf(MixedType.INTEGER,
                                Mixed.valueOf(2.toShort()),
                                Mixed.valueOf(20.toShort())
                        ))
                        list.add(arrayOf(MixedType.INTEGER,
                                Mixed.valueOf(3.toInt()),
                                Mixed.valueOf(30.toInt())
                        ))
                        list.add(arrayOf(MixedType.INTEGER,
                                Mixed.valueOf(4.toLong()),
                                Mixed.valueOf(40.toLong())
                        ))
                    }
                    MixedType.BOOLEAN -> list.add(arrayOf(
                            MixedType.BOOLEAN,
                            Mixed.valueOf(false),
                            Mixed.valueOf(true)
                    ))
                    MixedType.STRING -> list.add(arrayOf(
                            MixedType.STRING,
                            Mixed.valueOf("hello world1"),
                            Mixed.valueOf("hello world2")
                    ))
                    MixedType.BINARY -> list.add(arrayOf(
                            MixedType.BINARY,
                            Mixed.valueOf(byteArrayOf(0, 1, 0)),
                            Mixed.valueOf(byteArrayOf(0, 1, 1))
                    ))
                    MixedType.DATE -> list.add(arrayOf(
                            MixedType.DATE,
                            Mixed.valueOf(Date(0)),
                            Mixed.valueOf(Date(10))
                    ))
                    MixedType.FLOAT -> list.add(arrayOf(
                            MixedType.FLOAT,
                            Mixed.valueOf(5.toFloat()),
                            Mixed.valueOf(50.toFloat())
                    ))
                    MixedType.DOUBLE -> list.add(arrayOf(
                            MixedType.DOUBLE,
                            Mixed.valueOf(6.toDouble()),
                            Mixed.valueOf(60.toDouble())
                    ))
                    MixedType.DECIMAL128 -> list.add(arrayOf(
                            MixedType.DECIMAL128,
                            Mixed.valueOf(Decimal128(1)),
                            Mixed.valueOf(Decimal128(10))
                    ))
                    MixedType.OBJECT_ID -> list.add(arrayOf(
                            MixedType.OBJECT_ID,
                            Mixed.valueOf(ObjectId(Date(10))),
                            Mixed.valueOf(ObjectId(Date(100)))
                    ))
                    MixedType.UUID -> list.add(arrayOf(
                            MixedType.UUID,
                            Mixed.valueOf(UUID.randomUUID()),
                            Mixed.valueOf(UUID.randomUUID())
                    ))
                    MixedType.OBJECT,   // Not tested in this test suite
                    MixedType.NULL
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


    @Rule
    @JvmField
    val folder = TemporaryFolder()

    init {
        Realm.init(InstrumentationRegistry.getInstrumentation().targetContext)
    }

    @Before
    fun setUp() {
        realmConfiguration = RealmDebugConfigurationBuilder(InstrumentationRegistry.getInstrumentation().targetContext)
                .setSchema(MixedNotIndexed::class.java,
                        MixedIndexed::class.java,
                        AllJavaTypes::class.java,
                        MixedRealmListWithPK::class.java,
                        MixedNotIndexedWithPK::class.java,
                        PrimaryKeyAsString::class.java)
                .directory(folder.newFolder())
                .build()

        realm = Realm.getInstance(realmConfiguration)
    }

    @After
    fun tearDown() {
        realm.close()
    }

    @Test
    fun copyFromRealm() {
        realm.beginTransaction()
        val value = realm.createObject<MixedNotIndexedWithPK>(0)
        value.mixed = first
        realm.commitTransaction()

        val copy = realm.copyFromRealm(value)

        if (testingType == MixedType.BINARY) {
            assertTrue(Arrays.equals(first.asBinary(), copy.mixed!!.asBinary()))
        } else {
            assertEquals(first, copy.mixed)
        }
    }

    @Test
    fun copyToRealm_primitive() {
        val value = MixedNotIndexedWithPK()
        value.mixed = first

        realm.beginTransaction()
        val managedValue = realm.copyToRealm(value)
        realm.commitTransaction()

        if (testingType == MixedType.BINARY) {
            assertTrue(Arrays.equals(first.asBinary(), managedValue.mixed!!.asBinary()))
        } else {
            assertEquals(first, managedValue.mixed)
        }
    }


    @Test
    fun copyToRealmOrUpdate_primitive() {
        realm.executeTransaction { realm ->
            val obj = realm.createObject<MixedNotIndexedWithPK>(0)
            obj.mixed = first
        }

        val value = MixedNotIndexedWithPK()
        value.mixed = second

        realm.beginTransaction()
        val managedValue = realm.copyToRealmOrUpdate(value)
        realm.commitTransaction()

        if (testingType == MixedType.BINARY) {
            assertTrue(Arrays.equals(second.asBinary(), managedValue.mixed!!.asBinary()))
        } else {
            assertEquals(second, managedValue.mixed!!)
        }
    }

    @Test
    fun insert_primitive() {
        realm.executeTransaction { realm ->
            val value = MixedNotIndexedWithPK()
            value.mixed = first

            realm.insert(value)
        }

        val managedValue = realm.where<MixedNotIndexedWithPK>().findFirst()!!

        if (testingType == MixedType.BINARY) {
            assertTrue(Arrays.equals(first.asBinary(), managedValue.mixed!!.asBinary()))
        } else {
            assertEquals(first, managedValue.mixed)
        }
    }

    @Test
    fun insertOrUpdate_primitive() {
        realm.executeTransaction { realm ->
            val value = realm.createObject<MixedNotIndexedWithPK>(0)
            value.mixed = first
        }

        realm.executeTransaction { realm ->
            val value = MixedNotIndexedWithPK()
            value.mixed = second

            realm.insertOrUpdate(value)
        }

        val all = realm.where<MixedNotIndexedWithPK>().findAll()

        assertEquals(1, all.size)

        if (testingType == MixedType.BINARY) {
            assertTrue(Arrays.equals(second.asBinary(), all[0]!!.mixed!!.asBinary()))
        } else {
            assertEquals(second, all[0]!!.mixed)
        }
    }
}
