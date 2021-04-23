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

package io.realm.realmany

import androidx.test.platform.app.InstrumentationRegistry
import io.realm.*
import io.realm.entities.*
import io.realm.rule.BlockingLooperThread
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
import kotlin.test.assertFalse
import kotlin.test.assertTrue


@RunWith(Parameterized::class)
class RealmAnyChangeListenerTests(private val testingType: RealmAnyType, private val first: Any, private val second: Any, private val third: Any) {
    private lateinit var realmConfiguration: RealmConfiguration

    @get:Rule
    val configFactory = TestRealmConfigurationFactory()

    private val looperThread = BlockingLooperThread()

    @Rule
    @JvmField
    val folder = TemporaryFolder()

    init {
        Realm.init(InstrumentationRegistry.getInstrumentation().targetContext)
    }

    @Before
    fun setUp() {
        realmConfiguration = configFactory.createSchemaConfiguration(
                false,
                AllJavaTypes::class.java)
    }

    @After
    fun tearDown() {
    }

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
                                RealmAny.valueOf(10.toByte()),
                                RealmAny.valueOf(true))
                        )
                        list.add(arrayOf(RealmAnyType.INTEGER,
                                RealmAny.valueOf(2.toShort()),
                                RealmAny.valueOf(20.toShort()
                                ),
                                RealmAny.valueOf(true)))
                        list.add(arrayOf(RealmAnyType.INTEGER,
                                RealmAny.valueOf(3.toInt()),
                                RealmAny.valueOf(30.toInt()),
                                RealmAny.valueOf(true))
                        )
                        list.add(arrayOf(RealmAnyType.INTEGER,
                                RealmAny.valueOf(4.toLong()),
                                RealmAny.valueOf(40.toLong()),
                                RealmAny.valueOf(true))
                        )
                    }
                    RealmAnyType.BOOLEAN -> list.add(arrayOf(
                            RealmAnyType.BOOLEAN,
                            RealmAny.valueOf(false),
                            RealmAny.valueOf(true),
                            RealmAny.valueOf(0.toInt()))
                    )
                    RealmAnyType.STRING -> list.add(arrayOf(
                            RealmAnyType.STRING,
                            RealmAny.valueOf("hello world1"),
                            RealmAny.valueOf("hello world2"),
                            RealmAny.valueOf(10.toInt()))
                    )
                    RealmAnyType.BINARY -> list.add(arrayOf(
                            RealmAnyType.BINARY,
                            RealmAny.valueOf(byteArrayOf(0, 1, 0)),
                            RealmAny.valueOf(byteArrayOf(0, 1, 1)),
                            RealmAny.valueOf("hello world3"))
                    )
                    RealmAnyType.DATE -> list.add(arrayOf(
                            RealmAnyType.DATE,
                            RealmAny.valueOf(Date(0)),
                            RealmAny.valueOf(Date(10)),
                            RealmAny.valueOf(ObjectId(Date(10))))
                    )
                    RealmAnyType.FLOAT -> list.add(arrayOf(
                            RealmAnyType.FLOAT,
                            RealmAny.valueOf(5.toFloat()),
                            RealmAny.valueOf(50.toFloat()),
                            RealmAny.valueOf(false))
                    )
                    RealmAnyType.DOUBLE -> list.add(arrayOf(
                            RealmAnyType.DOUBLE,
                            RealmAny.valueOf(6.toDouble()),
                            RealmAny.valueOf(60.toDouble()),
                            RealmAny.valueOf(false)))
                    RealmAnyType.DECIMAL128 -> list.add(arrayOf(
                            RealmAnyType.DECIMAL128,
                            RealmAny.valueOf(Decimal128(1)),
                            RealmAny.valueOf(Decimal128(10)),
                            RealmAny.valueOf(10.5.toFloat()))
                    )
                    RealmAnyType.OBJECT_ID -> list.add(arrayOf(
                            RealmAnyType.OBJECT_ID,
                            RealmAny.valueOf(ObjectId(Date(10))),
                            RealmAny.valueOf(ObjectId(Date(100))),
                            RealmAny.valueOf(Date(100)))
                    )
                    RealmAnyType.UUID -> list.add(arrayOf(
                            RealmAnyType.UUID,
                            RealmAny.valueOf(UUID.randomUUID()),
                            RealmAny.valueOf(UUID.randomUUID()),
                            RealmAny.valueOf("hello world1"))
                    )
                    RealmAnyType.OBJECT -> {
                        val first = AllJavaTypes(0)
                        first.fieldString = "FOO"

                        val second = AllJavaTypes(0)
                        second.fieldString = "FOO"
                        second.fieldRealmAny = RealmAny.valueOf(first)

                        list.add(arrayOf(
                                RealmAnyType.OBJECT,
                                first,
                                second,
                                "hello world1")
                        )
                    }
                    RealmAnyType.NULL -> { // Not tested directly
                    }

                    else -> throw AssertionError("Missing case for type: ${type.name}")
                }
            }

            return list
        }
    }

    @Test
    fun primitives_changeValueKeepType() {
        // Do not test on object types
        if (testingType == RealmAnyType.OBJECT)
            return

        looperThread.runBlocking {
            Realm.getInstance(realmConfiguration).use { realm ->
                realm.beginTransaction()

                val all = AllJavaTypes(0)
                all.fieldString = "FOO1"
                all.fieldRealmAny = first as RealmAny?
                val managedAll = realm.copyToRealm(all)

                realm.commitTransaction()

                looperThread.keepStrongReference(managedAll)

                managedAll.addChangeListener { _: AllJavaTypes, changeSet ->
                    changeSet!!
                    looperThread.postRunnable(Runnable {
                        assertTrue(changeSet.isFieldChanged(AllJavaTypes.FIELD_STRING))
                        assertTrue(changeSet.isFieldChanged(AllJavaTypes.FIELD_REALM_ANY))
                        looperThread.testComplete()
                    })
                }

                realm.beginTransaction()

                managedAll.fieldString = "FOO2"
                managedAll.fieldRealmAny = second as RealmAny?

                realm.commitTransaction()
            }
        }
    }

    @Test
    fun primitives_changeValueChangeType() {
        // Do not test on object types
        if (testingType == RealmAnyType.OBJECT)
            return

        looperThread.runBlocking {
            Realm.getInstance(realmConfiguration).use { realm ->
                realm.beginTransaction()

                val all = AllJavaTypes(0)
                all.fieldString = "FOO1"
                all.fieldRealmAny = first as RealmAny?
                val managedAll = realm.copyToRealm(all)

                realm.commitTransaction()

                looperThread.keepStrongReference(managedAll)

                managedAll.addChangeListener<RealmModel> { _, changeSet ->
                    changeSet!!
                    looperThread.postRunnable(Runnable {
                        assertTrue(changeSet.isFieldChanged(AllJavaTypes.FIELD_STRING))
                        assertTrue(changeSet.isFieldChanged(AllJavaTypes.FIELD_REALM_ANY))
                        looperThread.testComplete()
                    })
                }

                realm.beginTransaction()

                managedAll.fieldString = "FOO2"
                managedAll.fieldRealmAny = third as RealmAny?

                realm.commitTransaction()
            }
        }
    }

    @Test
    fun primitives_changeValueChangeType_checkSameValueFlag() {
        // Do not test on object types
        if (testingType == RealmAnyType.OBJECT)
            return

        looperThread.runBlocking {
            Realm.getInstance(realmConfiguration).use { realm ->
                realm.beginTransaction()

                val all = AllJavaTypes(0)
                all.fieldString = "FOO1"
                all.fieldRealmAny = first as RealmAny?
                val managedAll = realm.copyToRealm(all)

                realm.commitTransaction()

                looperThread.keepStrongReference(managedAll)

                managedAll.addChangeListener { _: AllJavaTypes, changeSet ->
                    changeSet!!
                    looperThread.postRunnable(Runnable {
                        assertTrue(changeSet.isFieldChanged(AllJavaTypes.FIELD_STRING))
                        assertTrue(changeSet.isFieldChanged(AllJavaTypes.FIELD_REALM_ANY))
                        looperThread.testComplete()
                    })
                }

                realm.beginTransaction()

                val updatingModel = AllJavaTypes(0)
                updatingModel.fieldString = "FOO2"
                updatingModel.fieldRealmAny = third as RealmAny?

                realm.copyToRealmOrUpdate(updatingModel, ImportFlag.CHECK_SAME_VALUES_BEFORE_SET)

                realm.commitTransaction()
            }
        }
    }

    @Test
    fun primitives_keepValues() {
        // Do not test on object types
        if (testingType == RealmAnyType.OBJECT)
            return

        looperThread.runBlocking {
            Realm.getInstance(realmConfiguration).use { realm ->
                realm.beginTransaction()

                val all = AllJavaTypes(0)
                all.fieldString = "FOO1"
                all.fieldRealmAny = first as RealmAny?
                val managedAll = realm.copyToRealm(all)

                realm.commitTransaction()

                looperThread.keepStrongReference(managedAll)

                managedAll.addChangeListener { _: AllJavaTypes, changeSet ->
                    changeSet!!
                    looperThread.postRunnable(Runnable {
                        assertTrue(changeSet.isFieldChanged(AllJavaTypes.FIELD_STRING))
                        assertTrue(changeSet.isFieldChanged(AllJavaTypes.FIELD_REALM_ANY))
                        looperThread.testComplete()
                    })
                }

                realm.beginTransaction()

                managedAll.fieldString = "FOO2"
                managedAll.fieldRealmAny = first

                realm.commitTransaction()
            }
        }
    }

    @Test
    fun primitives_keepValues_checkSameValuesFlag() {
        // Do not test on object types
        if (testingType == RealmAnyType.OBJECT)
            return

        looperThread.runBlocking {
            Realm.getInstance(realmConfiguration).use { realm ->
                realm.beginTransaction()

                val all = AllJavaTypes(0)
                all.fieldString = "FOO1"
                all.fieldRealmAny = first as RealmAny?
                val managedAll = realm.copyToRealm(all)

                realm.commitTransaction()

                looperThread.keepStrongReference(managedAll)

                managedAll.addChangeListener { _: AllJavaTypes, changeSet ->
                    changeSet!!
                    looperThread.postRunnable(Runnable {
                        assertTrue(changeSet.isFieldChanged(AllJavaTypes.FIELD_STRING))
                        assertFalse(changeSet.isFieldChanged(AllJavaTypes.FIELD_REALM_ANY))
                        looperThread.testComplete()
                    })
                }

                realm.beginTransaction()

                val updatingModel = AllJavaTypes(0)
                updatingModel.fieldString = "FOO2"
                updatingModel.fieldRealmAny = first

                realm.copyToRealmOrUpdate(updatingModel, ImportFlag.CHECK_SAME_VALUES_BEFORE_SET)

                realm.commitTransaction()
            }
        }
    }

    @Test
    fun primitives_fromNull() {
        // Do not test on object types
        if (testingType == RealmAnyType.OBJECT)
            return

        looperThread.runBlocking {
            Realm.getInstance(realmConfiguration).use { realm ->
                realm.beginTransaction()

                val all = AllJavaTypes(0)
                all.fieldString = "FOO1"
                val managedAll = realm.copyToRealm(all)

                realm.commitTransaction()

                looperThread.keepStrongReference(managedAll)

                managedAll.addChangeListener { _: AllJavaTypes, changeSet ->
                    changeSet!!
                    looperThread.postRunnable(Runnable {
                        assertTrue(changeSet.isFieldChanged(AllJavaTypes.FIELD_STRING))
                        assertTrue(changeSet.isFieldChanged(AllJavaTypes.FIELD_REALM_ANY))
                        looperThread.testComplete()
                    })
                }

                realm.beginTransaction()

                managedAll.fieldString = "FOO2"
                managedAll.fieldRealmAny = first as RealmAny?

                realm.commitTransaction()
            }
        }
    }

    @Test
    fun primitives_toNull() {
        // Do not test on object types
        if (testingType == RealmAnyType.OBJECT)
            return

        looperThread.runBlocking {
            Realm.getInstance(realmConfiguration).use { realm ->
                realm.beginTransaction()

                val all = AllJavaTypes(0)
                all.fieldString = "FOO1"
                all.fieldRealmAny = first as RealmAny?
                val managedAll = realm.copyToRealm(all)

                realm.commitTransaction()

                looperThread.keepStrongReference(managedAll)

                managedAll.addChangeListener { _: AllJavaTypes, changeSet ->
                    changeSet!!
                    looperThread.postRunnable(Runnable {
                        assertTrue(changeSet.isFieldChanged(AllJavaTypes.FIELD_STRING))
                        assertTrue(changeSet.isFieldChanged(AllJavaTypes.FIELD_REALM_ANY))
                        looperThread.testComplete()
                    })
                }

                realm.beginTransaction()

                managedAll.fieldString = "FOO2"
                managedAll.fieldRealmAny = RealmAny.nullValue()

                realm.commitTransaction()
            }
        }
    }

    @Test
    fun realmModels_cyclicDependency() {
        // Do not test on object types
        if (testingType != RealmAnyType.OBJECT)
            return

        looperThread.runBlocking {
            Realm.getInstance(realmConfiguration).use { realm ->
                realm.beginTransaction()

                val managedAll = realm.copyToRealmOrUpdate(first as AllJavaTypes)

                realm.commitTransaction()

                looperThread.keepStrongReference(managedAll)

                RealmObject.addChangeListener(managedAll) { _, changeSet ->
                    changeSet!!
                    looperThread.postRunnable(Runnable {
                        assertFalse(changeSet.isFieldChanged(AllJavaTypes.FIELD_STRING))
                        assertTrue(changeSet.isFieldChanged(AllJavaTypes.FIELD_REALM_ANY))
                        looperThread.testComplete()
                    })
                }

                realm.beginTransaction()

                realm.copyToRealmOrUpdate(second as RealmModel, ImportFlag.CHECK_SAME_VALUES_BEFORE_SET)

                realm.commitTransaction()
            }
        }
    }
}
