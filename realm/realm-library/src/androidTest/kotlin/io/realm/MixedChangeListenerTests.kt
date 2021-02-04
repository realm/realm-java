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
class MixedChangeListenerTests(private val first: Mixed, private val second: Mixed, private val third: Mixed) {
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
        fun data() = listOf(
                arrayOf(Mixed.valueOf(false), Mixed.valueOf(true), Mixed.valueOf(0.toInt())),
                arrayOf(Mixed.valueOf(1.toByte()), Mixed.valueOf(10.toByte()), Mixed.valueOf(true)),
                arrayOf(Mixed.valueOf(2.toShort()), Mixed.valueOf(20.toShort()), Mixed.valueOf(true)),
                arrayOf(Mixed.valueOf(3.toInt()), Mixed.valueOf(30.toInt()), Mixed.valueOf(true)),
                arrayOf(Mixed.valueOf(4.toLong()), Mixed.valueOf(40.toLong()), Mixed.valueOf(true)),
                arrayOf(Mixed.valueOf(5.toFloat()), Mixed.valueOf(50.toFloat()), Mixed.valueOf(false)),
                arrayOf(Mixed.valueOf(6.toDouble()), Mixed.valueOf(60.toDouble()), Mixed.valueOf(false)),
                arrayOf(Mixed.valueOf("hello world1"), Mixed.valueOf("hello world2"), Mixed.valueOf(10.toInt())),               // 7
                arrayOf(Mixed.valueOf(byteArrayOf(0, 1, 0)), Mixed.valueOf(byteArrayOf(0, 1, 1)), Mixed.valueOf("hello world3")),
                arrayOf(Mixed.valueOf(Date(0)), Mixed.valueOf(Date(10)), Mixed.valueOf(ObjectId(Date(10)))),                //9
                arrayOf(Mixed.valueOf(ObjectId(Date(10))), Mixed.valueOf(ObjectId(Date(100))), Mixed.valueOf(Date(100))),
                arrayOf(Mixed.valueOf(Decimal128(1)), Mixed.valueOf(Decimal128(10)), Mixed.valueOf(10.5.toFloat())),
                arrayOf(Mixed.valueOf(UUID.randomUUID()), Mixed.valueOf(UUID.randomUUID()), Mixed.valueOf("hello world1"))
        )
    }

    @Test
    fun primitives_changeValueKeepType() {
        looperThread.runBlocking {
            Realm.getInstance(realmConfiguration).use { realm ->
                realm.beginTransaction()

                val all = AllJavaTypes(0)
                all.fieldString = "FOO1"
                all.fieldMixed = first
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
                updatingModel.fieldMixed = second

                realm.copyToRealmOrUpdate(updatingModel)

                realm.commitTransaction()
            }
        }
    }

    @Test
    fun primitives_changeValueChangeType() {
        looperThread.runBlocking {
            Realm.getInstance(realmConfiguration).use { realm ->
                realm.beginTransaction()

                val all = AllJavaTypes(0)
                all.fieldString = "FOO1"
                all.fieldMixed = first
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
                updatingModel.fieldMixed = third

                realm.copyToRealmOrUpdate(updatingModel)

                realm.commitTransaction()
            }
        }
    }

    @Test
    fun primitives_changeValueChangeType_checkSameValueFlag() {
        looperThread.runBlocking {
            Realm.getInstance(realmConfiguration).use { realm ->
                realm.beginTransaction()

                val all = AllJavaTypes(0)
                all.fieldString = "FOO1"
                all.fieldMixed = first
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
                updatingModel.fieldMixed = third

                realm.copyToRealmOrUpdate(updatingModel, ImportFlag.CHECK_SAME_VALUES_BEFORE_SET)

                realm.commitTransaction()
            }
        }
    }

    @Test
    fun primitives_keepValues() {
        looperThread.runBlocking {
            Realm.getInstance(realmConfiguration).use { realm ->
                realm.beginTransaction()

                val all = AllJavaTypes(0)
                all.fieldString = "FOO1"
                all.fieldMixed = first
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
        looperThread.runBlocking {
            Realm.getInstance(realmConfiguration).use { realm ->
                realm.beginTransaction()

                val all = AllJavaTypes(0)
                all.fieldString = "FOO1"
                all.fieldMixed = first
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
                updatingModel.fieldMixed = first

                realm.copyToRealmOrUpdate(updatingModel, ImportFlag.CHECK_SAME_VALUES_BEFORE_SET)

                realm.commitTransaction()
            }
        }
    }

    @Test
    fun primitives_toNull() {
        looperThread.runBlocking {
            Realm.getInstance(realmConfiguration).use { realm ->
                realm.beginTransaction()

                val all = AllJavaTypes(0)
                all.fieldString = "FOO1"
                all.fieldMixed = first
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
}
