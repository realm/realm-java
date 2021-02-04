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
import io.realm.entities.AllJavaTypes
import io.realm.rule.BlockingLooperThread
import org.bson.types.Decimal128
import org.bson.types.ObjectId
import org.junit.*
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.util.*


@RunWith(Parameterized::class)
class MixedChangeListenerTests(private val testingType: MixedType, private val first: Any, private val second: Any, private val third: Any) {
    private lateinit var realmConfiguration: RealmConfiguration
    private val looperThread = BlockingLooperThread()

    @Rule
    @JvmField
    val folder = TemporaryFolder()

    init {
        Realm.init(InstrumentationRegistry.getInstrumentation().targetContext)
    }

    @Before
    fun setUp() {
        realmConfiguration = RealmConfiguration
                .Builder(InstrumentationRegistry.getInstrumentation().targetContext)
                .directory(folder.newFolder())
                .schema(AllJavaTypes::class.java)
                .build()
    }

    @After
    fun tearDown() {
    }

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
                                Mixed.valueOf(10.toByte()),
                                Mixed.valueOf(true))
                        )
                        list.add(arrayOf(MixedType.INTEGER,
                                Mixed.valueOf(2.toShort()),
                                Mixed.valueOf(20.toShort()
                                ),
                                Mixed.valueOf(true)))
                        list.add(arrayOf(MixedType.INTEGER,
                                Mixed.valueOf(3.toInt()),
                                Mixed.valueOf(30.toInt()),
                                Mixed.valueOf(true))
                        )
                        list.add(arrayOf(MixedType.INTEGER,
                                Mixed.valueOf(4.toLong()),
                                Mixed.valueOf(40.toLong()),
                                Mixed.valueOf(true))
                        )
                    }
                    MixedType.BOOLEAN -> list.add(arrayOf(
                            MixedType.BOOLEAN,
                            Mixed.valueOf(false),
                            Mixed.valueOf(true),
                            Mixed.valueOf(0.toInt()))
                    )
                    MixedType.STRING -> list.add(arrayOf(
                            MixedType.STRING,
                            Mixed.valueOf("hello world1"),
                            Mixed.valueOf("hello world2"),
                            Mixed.valueOf(10.toInt()))
                    )
                    MixedType.BINARY -> list.add(arrayOf(
                            MixedType.BINARY,
                            Mixed.valueOf(byteArrayOf(0, 1, 0)),
                            Mixed.valueOf(byteArrayOf(0, 1, 1)),
                            Mixed.valueOf("hello world3"))
                    )
                    MixedType.DATE -> list.add(arrayOf(
                            MixedType.DATE,
                            Mixed.valueOf(Date(0)),
                            Mixed.valueOf(Date(10)),
                            Mixed.valueOf(ObjectId(Date(10))))
                    )
                    MixedType.FLOAT -> list.add(arrayOf(
                            MixedType.FLOAT,
                            Mixed.valueOf(5.toFloat()),
                            Mixed.valueOf(50.toFloat()),
                            Mixed.valueOf(false))
                    )
                    MixedType.DOUBLE -> list.add(arrayOf(
                            MixedType.DOUBLE,
                            Mixed.valueOf(6.toDouble()),
                            Mixed.valueOf(60.toDouble()),
                            Mixed.valueOf(false)))
                    MixedType.DECIMAL128 -> list.add(arrayOf(
                            MixedType.DECIMAL128,
                            Mixed.valueOf(Decimal128(1)),
                            Mixed.valueOf(Decimal128(10)),
                            Mixed.valueOf(10.5.toFloat()))
                    )
                    MixedType.OBJECT_ID -> list.add(arrayOf(
                            MixedType.OBJECT_ID,
                            Mixed.valueOf(ObjectId(Date(10))),
                            Mixed.valueOf(ObjectId(Date(100))),
                            Mixed.valueOf(Date(100)))
                    )
                    MixedType.UUID -> list.add(arrayOf(
                            MixedType.UUID,
                            Mixed.valueOf(UUID.randomUUID()),
                            Mixed.valueOf(UUID.randomUUID()),
                            Mixed.valueOf("hello world1"))
                    )
                    MixedType.OBJECT -> {
                        val first = AllJavaTypes(0)
                        first.fieldString = "FOO"

                        val second = AllJavaTypes(0)
                        second.fieldString = "FOO"
                        second.fieldMixed = Mixed.valueOf(first)

                        list.add(arrayOf(
                                MixedType.OBJECT,
                                first,
                                second,
                                "hello world1")
                        )
                    }
                    MixedType.NULL -> { // Not tested directly
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
        if (testingType == MixedType.OBJECT)
            return

        looperThread.runBlocking {
            Realm.getInstance(realmConfiguration).use { realm ->
                realm.beginTransaction()

                val all = AllJavaTypes(0)
                all.fieldString = "FOO1"
                all.fieldMixed = first as Mixed?
                val managedAll = realm.copyToRealm(all)

                realm.commitTransaction()

                looperThread.keepStrongReference(managedAll)

                RealmObject.addChangeListener<RealmModel>(managedAll, { _, changeSet ->
                    changeSet!!
                    looperThread.postRunnable(Runnable {
                        Assert.assertTrue(changeSet.isFieldChanged(AllJavaTypes.FIELD_STRING))
                        Assert.assertTrue(changeSet.isFieldChanged(AllJavaTypes.FIELD_MIXED))
                        looperThread.testComplete()
                    })
                })

                realm.beginTransaction()

                val updatingModel = AllJavaTypes(0)
                updatingModel.fieldString = "FOO2"
                updatingModel.fieldMixed = second as Mixed?

                realm.copyToRealmOrUpdate(updatingModel)

                realm.commitTransaction()
            }
        }
    }

    @Test
    fun primitives_changeValueChangeType() {
        // Do not test on object types
        if (testingType == MixedType.OBJECT)
            return

        looperThread.runBlocking {
            Realm.getInstance(realmConfiguration).use { realm ->
                realm.beginTransaction()

                val all = AllJavaTypes(0)
                all.fieldString = "FOO1"
                all.fieldMixed = first as Mixed?
                val managedAll = realm.copyToRealm(all)

                realm.commitTransaction()

                looperThread.keepStrongReference(managedAll)

                RealmObject.addChangeListener<RealmModel>(managedAll, { _, changeSet ->
                    changeSet!!
                    looperThread.postRunnable(Runnable {
                        Assert.assertTrue(changeSet.isFieldChanged(AllJavaTypes.FIELD_STRING))
                        Assert.assertTrue(changeSet.isFieldChanged(AllJavaTypes.FIELD_MIXED))
                        looperThread.testComplete()
                    })
                })

                realm.beginTransaction()

                val updatingModel = AllJavaTypes(0)
                updatingModel.fieldString = "FOO2"
                updatingModel.fieldMixed = third as Mixed?

                realm.copyToRealmOrUpdate(updatingModel)

                realm.commitTransaction()
            }
        }
    }

    @Test
    fun primitives_changeValueChangeType_checkSameValueFlag() {
        // Do not test on object types
        if (testingType == MixedType.OBJECT)
            return

        looperThread.runBlocking {
            Realm.getInstance(realmConfiguration).use { realm ->
                realm.beginTransaction()

                val all = AllJavaTypes(0)
                all.fieldString = "FOO1"
                all.fieldMixed = first as Mixed?
                val managedAll = realm.copyToRealm(all)

                realm.commitTransaction()

                looperThread.keepStrongReference(managedAll)

                RealmObject.addChangeListener<RealmModel>(managedAll, { _, changeSet ->
                    changeSet!!
                    looperThread.postRunnable(Runnable {
                        Assert.assertTrue(changeSet.isFieldChanged(AllJavaTypes.FIELD_STRING))
                        Assert.assertTrue(changeSet.isFieldChanged(AllJavaTypes.FIELD_MIXED))
                        looperThread.testComplete()
                    })
                })

                realm.beginTransaction()

                val updatingModel = AllJavaTypes(0)
                updatingModel.fieldString = "FOO2"
                updatingModel.fieldMixed = third as Mixed?

                realm.copyToRealmOrUpdate(updatingModel, ImportFlag.CHECK_SAME_VALUES_BEFORE_SET)

                realm.commitTransaction()
            }
        }
    }

    @Test
    fun primitives_keepValues() {
        // Do not test on object types
        if (testingType == MixedType.OBJECT)
            return

        looperThread.runBlocking {
            Realm.getInstance(realmConfiguration).use { realm ->
                realm.beginTransaction()

                val all = AllJavaTypes(0)
                all.fieldString = "FOO1"
                all.fieldMixed = first as Mixed?
                val managedAll = realm.copyToRealm(all)

                realm.commitTransaction()

                looperThread.keepStrongReference(managedAll)

                RealmObject.addChangeListener<RealmModel>(managedAll, { _, changeSet ->
                    changeSet!!
                    looperThread.postRunnable(Runnable {
                        Assert.assertTrue(changeSet.isFieldChanged(AllJavaTypes.FIELD_STRING))
                        Assert.assertTrue(changeSet.isFieldChanged(AllJavaTypes.FIELD_MIXED))
                        looperThread.testComplete()
                    })
                })

                realm.beginTransaction()

                val updatingModel = AllJavaTypes(0)
                updatingModel.fieldString = "FOO2"
                updatingModel.fieldMixed = first

                realm.copyToRealmOrUpdate(updatingModel)

                realm.commitTransaction()
            }
        }
    }

    @Test
    fun primitives_keepValues_checkSameValuesFlag() {
        // Do not test on object types
        if (testingType == MixedType.OBJECT)
            return

        looperThread.runBlocking {
            Realm.getInstance(realmConfiguration).use { realm ->
                realm.beginTransaction()

                val all = AllJavaTypes(0)
                all.fieldString = "FOO1"
                all.fieldMixed = first as Mixed?
                val managedAll = realm.copyToRealm(all)

                realm.commitTransaction()

                looperThread.keepStrongReference(managedAll)

                RealmObject.addChangeListener<RealmModel>(managedAll, { _, changeSet ->
                    changeSet!!
                    looperThread.postRunnable(Runnable {
                        Assert.assertTrue(changeSet.isFieldChanged(AllJavaTypes.FIELD_STRING))
                        Assert.assertFalse(changeSet.isFieldChanged(AllJavaTypes.FIELD_MIXED))
                        looperThread.testComplete()
                    })
                })

                realm.beginTransaction()

                val updatingModel = AllJavaTypes(0)
                updatingModel.fieldString = "FOO2"
                updatingModel.fieldMixed = first

                realm.copyToRealmOrUpdate(updatingModel, ImportFlag.CHECK_SAME_VALUES_BEFORE_SET)

                realm.commitTransaction()
            }
        }
    }

    @Test
    fun primitives_fromNull() {
        // Do not test on object types
        if (testingType == MixedType.OBJECT)
            return

        looperThread.runBlocking {
            Realm.getInstance(realmConfiguration).use { realm ->
                realm.beginTransaction()

                val all = AllJavaTypes(0)
                all.fieldString = "FOO1"
                val managedAll = realm.copyToRealm(all)

                realm.commitTransaction()

                looperThread.keepStrongReference(managedAll)

                RealmObject.addChangeListener<RealmModel>(managedAll, { _, changeSet ->
                    changeSet!!
                    looperThread.postRunnable(Runnable {
                        Assert.assertTrue(changeSet.isFieldChanged(AllJavaTypes.FIELD_STRING))
                        Assert.assertTrue(changeSet.isFieldChanged(AllJavaTypes.FIELD_MIXED))
                        looperThread.testComplete()
                    })
                })

                realm.beginTransaction()

                val updatingModel = AllJavaTypes(0)
                updatingModel.fieldString = "FOO2"
                updatingModel.fieldMixed = first as Mixed?

                realm.copyToRealmOrUpdate(updatingModel, ImportFlag.CHECK_SAME_VALUES_BEFORE_SET)

                realm.commitTransaction()
            }
        }
    }

    @Test
    fun primitives_toNull() {
        // Do not test on object types
        if (testingType == MixedType.OBJECT)
            return

        looperThread.runBlocking {
            Realm.getInstance(realmConfiguration).use { realm ->
                realm.beginTransaction()

                val all = AllJavaTypes(0)
                all.fieldString = "FOO1"
                all.fieldMixed = first as Mixed?
                val managedAll = realm.copyToRealm(all)

                realm.commitTransaction()

                looperThread.keepStrongReference(managedAll)

                RealmObject.addChangeListener<RealmModel>(managedAll, { _, changeSet ->
                    changeSet!!
                    looperThread.postRunnable(Runnable {
                        Assert.assertTrue(changeSet.isFieldChanged(AllJavaTypes.FIELD_STRING))
                        Assert.assertTrue(changeSet.isFieldChanged(AllJavaTypes.FIELD_MIXED))
                        looperThread.testComplete()
                    })
                })

                realm.beginTransaction()

                val updatingModel = AllJavaTypes(0)
                updatingModel.fieldString = "FOO2"
                updatingModel.fieldMixed = Mixed.nullValue()

                realm.copyToRealmOrUpdate(updatingModel, ImportFlag.CHECK_SAME_VALUES_BEFORE_SET)

                realm.commitTransaction()
            }
        }
    }

    @Test
    fun realmModels_cyclicDependency() {
        // Do not test on object types
        if (testingType != MixedType.OBJECT)
            return

        looperThread.runBlocking {
            Realm.getInstance(realmConfiguration).use { realm ->
                realm.beginTransaction()

                val managedAll = realm.copyToRealmOrUpdate(first as RealmModel)

                realm.commitTransaction()

                looperThread.keepStrongReference(managedAll)

                RealmObject.addChangeListener<RealmModel>(managedAll, { _, changeSet ->
                    changeSet!!
                    looperThread.postRunnable(Runnable {
                        Assert.assertFalse(changeSet.isFieldChanged(AllJavaTypes.FIELD_STRING))
                        Assert.assertTrue(changeSet.isFieldChanged(AllJavaTypes.FIELD_MIXED))
                        looperThread.testComplete()
                    })
                })

                realm.beginTransaction()

                realm.copyToRealmOrUpdate(second as RealmModel, ImportFlag.CHECK_SAME_VALUES_BEFORE_SET)

                realm.commitTransaction()
            }
        }
    }
}
