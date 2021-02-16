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

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.realm.entities.*
import io.realm.kotlin.createObject
import io.realm.kotlin.where
<<<<<<< HEAD
=======
import org.bson.types.Decimal128
import org.bson.types.ObjectId
>>>>>>> ct/mixed-datatype-story
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import java.util.*
<<<<<<< HEAD
import kotlin.test.assertEquals
import kotlin.test.assertTrue
=======
import kotlin.test.*
>>>>>>> ct/mixed-datatype-story

@RunWith(AndroidJUnit4::class)
class MixedBulkInsertsTests {
    private lateinit var realmConfiguration: RealmConfiguration
    private lateinit var realm: Realm

<<<<<<< HEAD
=======
    companion object {
        const val TEST_VALUE_1 = "hello world 1"
        const val TEST_VALUE_2 = "hello world 2"
        const val TEST_VALUE_3 = "hello world 3"
        const val TEST_VALUE_4 = "hello world 4"
    }

>>>>>>> ct/mixed-datatype-story
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
                .schema(MixedNotIndexed::class.java,
                        MixedIndexed::class.java,
                        AllJavaTypes::class.java,
                        MixedRealmListWithPK::class.java,
                        MixedNotIndexedWithPK::class.java,
                        PrimaryKeyAsString::class.java)
                .build()

        realm = Realm.getInstance(realmConfiguration)
    }

    @After
    fun tearDown() {
        realm.close()
    }

<<<<<<< HEAD
    private fun mixedTestValues() = RealmList(
            Mixed.valueOf(10.toInt()),
            Mixed.valueOf("hello world"),
            Mixed.valueOf(10.5),
            Mixed.valueOf(Date(1)),
            Mixed.nullValue()
    )

    private fun mixedAlternativeTestValues() = RealmList(
            Mixed.valueOf(10.9),
            Mixed.valueOf("hello world2"),
            Mixed.nullValue(),
            Mixed.valueOf(Date(2)),
            Mixed.valueOf(15.toInt())
    )

    @Test
    fun copyFromRealm() {
        realm.beginTransaction()
        val value = realm.createObject<MixedNotIndexedWithPK>(0)
        value.mixed = Mixed.valueOf(10.toInt())
        realm.commitTransaction()

        val copy = realm.copyFromRealm(value)

        assertEquals(Mixed.valueOf(10.toInt()), copy.mixed)
    }

    @Test
    fun copyToRealm_primitive() {
        val value = MixedNotIndexedWithPK()
        value.mixed = Mixed.valueOf(10.toInt())

        realm.beginTransaction()
        val managedValue = realm.copyToRealm(value)
        realm.commitTransaction()

        assertEquals(Mixed.valueOf(10.toInt()), managedValue.mixed)
    }

    @Test
    fun copyToRealm_primitiveList() {
        val value = MixedRealmListWithPK()
        value.mixedList = mixedTestValues()

        realm.beginTransaction()
        val managedValue = realm.copyToRealm(value)
        realm.commitTransaction()

        for (i in 0 until mixedTestValues().size) {
            assertEquals(mixedTestValues()[i], managedValue.mixedList[i])
        }
=======
    private val testList1 = arrayOf(
            Mixed.valueOf(10.toByte()),
            Mixed.valueOf(20.toShort()),
            Mixed.valueOf(30.toInt()),
            Mixed.valueOf(40.toLong()),
            Mixed.valueOf(true),
            Mixed.valueOf(TEST_VALUE_1),
            Mixed.valueOf(byteArrayOf(0, 1, 1)),
            Mixed.valueOf(Date(10)),
            Mixed.valueOf(50.toFloat()),
            Mixed.valueOf(60.toDouble()),
            Mixed.valueOf(Decimal128(10)),
            Mixed.valueOf(ObjectId(Date(100))),
            Mixed.valueOf(UUID.randomUUID())
    )

    private val testList2 = arrayOf(
            Mixed.valueOf(1.toByte()),
            Mixed.valueOf(2.toShort()),
            Mixed.valueOf(3.toInt()),
            Mixed.valueOf(4.toLong()),
            Mixed.valueOf(false),
            Mixed.valueOf(TEST_VALUE_2),
            Mixed.valueOf(byteArrayOf(0, 1, 0)),
            Mixed.valueOf(Date(0)),
            Mixed.valueOf(5.toFloat()),
            Mixed.valueOf(6.toDouble()),
            Mixed.valueOf(Decimal128(1)),
            Mixed.valueOf(ObjectId(Date(10))),
            Mixed.valueOf(UUID.randomUUID())
    )

    /**
     * Mixed tests for Realm objects
     */

    @Test
    fun copyFromRealm_realmModel() {
        realm.beginTransaction()
        val value = realm.createObject<MixedNotIndexedWithPK>(0)

        val inner = MixedNotIndexedWithPK(1)
        val allTypes = AllJavaTypes(0)
        allTypes.fieldString = TEST_VALUE_1
        val innerAllJavaTypes = AllJavaTypes(1)
        innerAllJavaTypes.fieldString = TEST_VALUE_2

        allTypes.fieldObject = innerAllJavaTypes

        inner.mixed = Mixed.valueOf(allTypes)
        value.mixed = Mixed.valueOf(inner)

        realm.commitTransaction()

        val copy0 = realm.copyFromRealm(value, 0)
        val copy1 = realm.copyFromRealm(value, 1)
        val copy2 = realm.copyFromRealm(value, 2)
        val copy3 = realm.copyFromRealm(value, 3)

        assertFalse(copy0.isManaged)
        assertFalse(copy1.isManaged)
        assertFalse(copy2.isManaged)
        assertFalse(copy3.isManaged)

        assertEquals(Mixed.nullValue(), copy0.mixed)
        assertEquals(Mixed.nullValue(), copy1.mixed!!.asRealmModel(MixedNotIndexedWithPK::class.java).mixed)
        assertEquals(TEST_VALUE_1, copy2.mixed!!.asRealmModel(MixedNotIndexedWithPK::class.java).mixed!!.asRealmModel(AllJavaTypes::class.java).fieldString)
        assertNull(copy2.mixed!!.asRealmModel(MixedNotIndexedWithPK::class.java).mixed!!.asRealmModel(AllJavaTypes::class.java).fieldObject)

        assertEquals(TEST_VALUE_1, copy3.mixed!!.asRealmModel(MixedNotIndexedWithPK::class.java).mixed!!.asRealmModel(AllJavaTypes::class.java).fieldString)
        assertNotNull(copy3.mixed!!.asRealmModel(MixedNotIndexedWithPK::class.java).mixed!!.asRealmModel(AllJavaTypes::class.java).fieldObject)
        assertEquals(TEST_VALUE_2, copy3.mixed!!.asRealmModel(MixedNotIndexedWithPK::class.java).mixed!!.asRealmModel(AllJavaTypes::class.java).fieldObject.fieldString)
>>>>>>> ct/mixed-datatype-story
    }

    @Test
    fun copyToRealm_realmModel() {
        val value = MixedNotIndexedWithPK()
<<<<<<< HEAD
        value.mixed = Mixed.valueOf(PrimaryKeyAsString("hello world"))
=======
        value.mixed = Mixed.valueOf(PrimaryKeyAsString(TEST_VALUE_1))
>>>>>>> ct/mixed-datatype-story

        realm.beginTransaction()
        val managedValue = realm.copyToRealm(value)
        realm.commitTransaction()

<<<<<<< HEAD
        val managedInnerObject = managedValue.mixed!!.asRealmModel(PrimaryKeyAsString::class.java)

        assertTrue(managedInnerObject.isManaged)
        assertEquals("hello world", managedInnerObject.name)
    }

    @Test
    fun copyToRealm_realmModelList() {
        val value = MixedRealmListWithPK()

        value.mixedList = mixedTestValues()
        value.mixedList.add(Mixed.valueOf(PrimaryKeyAsString("hello world")))

        realm.beginTransaction()
        val managedValue = realm.copyToRealm(value)
        realm.commitTransaction()

        for (i in 0 until mixedTestValues().size) {
            assertEquals(mixedTestValues()[i], managedValue.mixedList[i])
        }

        val managedInnerObject = managedValue.mixedList.last()!!.asRealmModel(PrimaryKeyAsString::class.java)

        assertTrue(managedInnerObject.isManaged)
        assertEquals("hello world", managedInnerObject.name)
    }

    @Test
    fun copyToRealmOrUpdate_primitive() {
        realm.executeTransaction { realm ->
            val obj = realm.createObject<MixedNotIndexedWithPK>(0)
            obj.mixed = Mixed.valueOf(10.toInt())
        }

        val value = MixedNotIndexedWithPK()
        value.mixed = Mixed.valueOf(15.toInt())

        realm.beginTransaction()
        val managedValue = realm.copyToRealmOrUpdate(value)
        realm.commitTransaction()

        assertEquals(Mixed.valueOf(15.toInt()), managedValue.mixed)
    }

    @Test
    fun copyToRealmOrUpdate_primitiveList() {
        realm.executeTransaction { realm ->
            val obj = realm.createObject<MixedRealmListWithPK>(0)
            obj.mixedList = mixedTestValues()
        }

        val value = MixedRealmListWithPK()
        value.mixedList = mixedAlternativeTestValues()

        realm.beginTransaction()
        val managedValue = realm.copyToRealmOrUpdate(value)
        realm.commitTransaction()

        for (i in 0 until mixedTestValues().size) {
            assertEquals(mixedAlternativeTestValues()[i], managedValue.mixedList[i])
        }
=======
        val managedInnerObject = managedValue.mixed!!.asRealmModel(PrimaryKeyAsString::class.java)!!

        assertTrue(managedInnerObject.isManaged)
        assertEquals(TEST_VALUE_1, managedInnerObject.name)
>>>>>>> ct/mixed-datatype-story
    }

    @Test
    fun copyToRealmOrUpdate_realmModel() {
        realm.executeTransaction { realm ->
            val obj = realm.createObject<MixedNotIndexedWithPK>(0)
<<<<<<< HEAD
            obj.mixed = Mixed.valueOf(PrimaryKeyAsString("hello world1"))
        }

        val value = MixedNotIndexedWithPK()
        value.mixed = Mixed.valueOf(PrimaryKeyAsString("hello world2"))
=======
            obj.mixed = Mixed.valueOf(PrimaryKeyAsString(TEST_VALUE_1))
        }

        val value = MixedNotIndexedWithPK()
        value.mixed = Mixed.valueOf(PrimaryKeyAsString(TEST_VALUE_2))
>>>>>>> ct/mixed-datatype-story

        realm.beginTransaction()
        val managedValue = realm.copyToRealmOrUpdate(value)
        realm.commitTransaction()

        val managedInnerObject = managedValue.mixed!!.asRealmModel(PrimaryKeyAsString::class.java)
        assertTrue(managedInnerObject.isManaged)
<<<<<<< HEAD
        assertEquals("hello world2", managedInnerObject.name)
    }

    @Test
    fun copyToRealmOrUpdate_realmModelList() {
        realm.executeTransaction { realm ->
            val value = realm.createObject<MixedRealmListWithPK>(0)
            value.mixedList = mixedTestValues()
            value.mixedList.add(Mixed.valueOf(PrimaryKeyAsString("hello world1")))
        }

        val value = MixedRealmListWithPK()
        value.mixedList = mixedAlternativeTestValues()
        value.mixedList.add(Mixed.valueOf(PrimaryKeyAsString("hello world2")))

        realm.beginTransaction()
        val managedValue = realm.copyToRealmOrUpdate(value)
        realm.commitTransaction()

        for (i in 0 until mixedTestValues().size) {
            assertEquals(mixedAlternativeTestValues()[i], managedValue.mixedList[i])
        }

        val managedInnerObject = managedValue.mixedList.last()!!.asRealmModel(PrimaryKeyAsString::class.java)
        assertTrue(managedInnerObject.isManaged)
        assertEquals("hello world2", managedInnerObject.name)
    }

    @Test
    fun insert_primitive() {
        realm.executeTransaction { realm ->
            val value = MixedNotIndexedWithPK()
            value.mixed = Mixed.valueOf(10.toInt())

            realm.insert(value)
        }

        val managedValue = realm.where<MixedNotIndexedWithPK>().findFirst()

        assertEquals(Mixed.valueOf(10.toInt()), managedValue!!.mixed)
    }

    @Test
    fun insert_primitiveList() {
        realm.executeTransaction { realm ->
            val value = MixedRealmListWithPK()
            value.mixedList = mixedTestValues()

            realm.insert(value)
        }

        val managedValue = realm.where<MixedRealmListWithPK>().findFirst()

        for (i in 0 until mixedTestValues().size) {
            assertEquals(mixedTestValues()[i], managedValue!!.mixedList[i])
        }
=======
        assertEquals(TEST_VALUE_2, managedInnerObject.name)
>>>>>>> ct/mixed-datatype-story
    }

    @Test
    fun insert_realmModel() {
        realm.executeTransaction { realm ->
            val value = MixedNotIndexedWithPK()
<<<<<<< HEAD
            value.mixed = Mixed.valueOf(PrimaryKeyAsString("hello world"))
=======
            value.mixed = Mixed.valueOf(PrimaryKeyAsString(TEST_VALUE_1))
>>>>>>> ct/mixed-datatype-story

            realm.insert(value)
        }

        val managedValue = realm.where<MixedNotIndexedWithPK>().findFirst()!!
        val managedInnerObject = managedValue.mixed!!.asRealmModel(PrimaryKeyAsString::class.java)

        assertTrue(managedInnerObject.isManaged)
<<<<<<< HEAD
        assertEquals("hello world", managedInnerObject.name)
    }

    @Test
    fun insert_realmModelList() {
        realm.executeTransaction { realm ->
            val value = MixedRealmListWithPK()
            value.mixedList = mixedTestValues()
            value.mixedList.add(Mixed.valueOf(PrimaryKeyAsString("hello world")))

            realm.insert(value)
        }

        val managedValue = realm.where<MixedRealmListWithPK>().findFirst()!!

        for (i in 0 until mixedTestValues().size) {
            assertEquals(mixedTestValues()[i], managedValue.mixedList[i])
        }

        val managedInnerObject = managedValue.mixedList.last()!!.asRealmModel(PrimaryKeyAsString::class.java)

        assertTrue(managedInnerObject.isManaged)
        assertEquals("hello world", managedInnerObject.name)
    }

    @Test
    fun insertOrUpdate_primitive() {
        realm.executeTransaction { realm ->
            val value = realm.createObject<MixedNotIndexedWithPK>(0)
            value.mixed = Mixed.valueOf(10.toInt())
        }

        realm.executeTransaction { realm ->
            val value = MixedNotIndexedWithPK()
            value.mixed = Mixed.valueOf(15.toInt())

            realm.insertOrUpdate(value)
        }

        val all = realm.where<MixedNotIndexedWithPK>().findAll()

        assertEquals(1, all.size)
        assertEquals(Mixed.valueOf(15.toInt()), all[0]!!.mixed)
    }

    @Test
    fun insertOrUpdate_primitiveList() {
        realm.executeTransaction { realm ->
            val value = realm.createObject<MixedRealmListWithPK>(0)
            value.mixedList = mixedTestValues()
        }

        realm.executeTransaction { realm ->
            val value = MixedRealmListWithPK()
            value.mixedList = mixedAlternativeTestValues()

            realm.insertOrUpdate(value)
        }

        val all = realm.where<MixedRealmListWithPK>().findAll()!!
        assertEquals(1, all.size)

        for (i in 0 until mixedTestValues().size) {
            val mixed1 = mixedAlternativeTestValues()[i]
            val mixed2 = all.first()!!.mixedList[i]
            assertEquals(mixedAlternativeTestValues()[i], all.first()!!.mixedList[i])
        }
    }

    @Test
    fun insertOrUpdate_realmModel() {
        realm.executeTransaction { realm ->
            val value = realm.createObject<MixedNotIndexedWithPK>(0)
            value.mixed = Mixed.valueOf(PrimaryKeyAsString("hello world1"))
        }

        realm.executeTransaction { realm ->
            val value = MixedNotIndexedWithPK()
            value.mixed = Mixed.valueOf(PrimaryKeyAsString("hello world2"))

            realm.insertOrUpdate(value)
        }

        val all = realm.where<MixedNotIndexedWithPK>().findAll()

        assertEquals(1, all.size)

        val managedInnerObject = all.first()!!.mixed!!.asRealmModel(PrimaryKeyAsString::class.java)

        assertTrue(managedInnerObject.isManaged)
        assertEquals("hello world2", managedInnerObject.name)
=======
        assertEquals(TEST_VALUE_1, managedInnerObject.name)
    }

    @Test
    fun insertOrUpdate_realmModel() {
        realm.executeTransaction { realm ->
            val value = realm.createObject<MixedNotIndexedWithPK>(0)
            value.mixed = Mixed.valueOf(PrimaryKeyAsString(TEST_VALUE_1))
        }

        realm.executeTransaction { realm ->
            val value = MixedNotIndexedWithPK()
            value.mixed = Mixed.valueOf(PrimaryKeyAsString(TEST_VALUE_2))

            realm.insertOrUpdate(value)
        }

        val all = realm.where<MixedNotIndexedWithPK>().findAll()

        assertEquals(1, all.size)

        val managedInnerObject = all.first()!!.mixed!!.asRealmModel(PrimaryKeyAsString::class.java)

        assertTrue(managedInnerObject.isManaged)
        assertEquals(TEST_VALUE_2, managedInnerObject.name)
    }

    /**
     * Tests for Mixed RealmLists
     */

    @Test
    fun copyFromRealm_list() {
        val list = RealmList(*testList1)
        val inner = MixedNotIndexedWithPK(1)
        inner.mixed = Mixed.valueOf(PrimaryKeyAsString(TEST_VALUE_1))

        list.add(Mixed.valueOf(inner))

        realm.beginTransaction()
        val value = realm.createObject<MixedRealmListWithPK>(0)
        value.mixedList = list
        realm.commitTransaction()

        val copy0 = realm.copyFromRealm(value, 0)
        val copy1 = realm.copyFromRealm(value, 1)
        val copy2 = realm.copyFromRealm(value, 2)

        assertFalse(copy0.isManaged)
        assertFalse(copy1.isManaged)
        assertFalse(copy2.isManaged)

        assertNull(copy0.mixedList)
        assertTrue(copy1.mixedList.containsAll(testList1.asList()))
        assertEquals(Mixed.nullValue(), copy1.mixedList.last()!!.asRealmModel(MixedNotIndexedWithPK::class.java).mixed)
        assertEquals(TEST_VALUE_1, copy2.mixedList.last()!!.asRealmModel(MixedNotIndexedWithPK::class.java).mixed!!.asRealmModel(PrimaryKeyAsString::class.java).name)
    }

    @Test
    fun copyToRealmOrUpdate_list() {
        realm.executeTransaction { realm ->
            val value = realm.createObject<MixedRealmListWithPK>(0)
            value.mixedList = RealmList(*testList1)
            value.mixedList.add(Mixed.valueOf(PrimaryKeyAsString(TEST_VALUE_1)))
        }

        val update = MixedRealmListWithPK(0)
        update.mixedList = RealmList(*testList2)
        update.mixedList.add(Mixed.valueOf(PrimaryKeyAsString(TEST_VALUE_2)))

        realm.beginTransaction()
        val managedValue = realm.copyToRealmOrUpdate(update)
        realm.commitTransaction()

        assertTrue(managedValue.mixedList.containsAll(testList2.asList()))

        val managedInnerObject = managedValue.mixedList.last()!!.asRealmModel(PrimaryKeyAsString::class.java)
        assertTrue(managedInnerObject.isManaged)
        assertEquals(TEST_VALUE_2, managedInnerObject.name)
    }

    @Test
    fun insert_realmModelList() {
        realm.executeTransaction { realm ->
            val value = MixedRealmListWithPK()
            value.mixedList = RealmList(*testList1)
            value.mixedList.add(Mixed.valueOf(PrimaryKeyAsString(TEST_VALUE_1)))

            realm.insert(value)
        }

        val managedValue = realm.where<MixedRealmListWithPK>().findFirst()!!

        val managedInnerObject = managedValue.mixedList.last()!!.asRealmModel(PrimaryKeyAsString::class.java)

        assertTrue(managedInnerObject.isManaged)
        assertTrue(managedValue.mixedList.containsAll(testList1.asList()))
        assertEquals(TEST_VALUE_1, managedInnerObject.name)
>>>>>>> ct/mixed-datatype-story
    }

    @Test
    fun insertOrUpdate_realmModelList() {
        realm.executeTransaction { realm ->
            val value = realm.createObject<MixedRealmListWithPK>(0)
<<<<<<< HEAD
            value.mixedList = mixedTestValues()
            value.mixedList.add(Mixed.valueOf(PrimaryKeyAsString("hello world1")))
=======
            value.mixedList = RealmList(*testList1)
            value.mixedList.add(Mixed.valueOf(PrimaryKeyAsString(TEST_VALUE_1)))
>>>>>>> ct/mixed-datatype-story
        }

        realm.executeTransaction { realm ->
            val value = MixedRealmListWithPK(0)
<<<<<<< HEAD
            value.mixedList = mixedAlternativeTestValues()
            value.mixedList.add(Mixed.valueOf(PrimaryKeyAsString("hello world2")))
=======
            value.mixedList = RealmList(*testList2)
            value.mixedList.add(Mixed.valueOf(PrimaryKeyAsString(TEST_VALUE_2)))
>>>>>>> ct/mixed-datatype-story

            realm.insertOrUpdate(value)
        }

        val all = realm.where<MixedRealmListWithPK>().findAll()
        assertEquals(1, all.size)

<<<<<<< HEAD
        for (i in 0 until mixedTestValues().size) {
            assertEquals(mixedAlternativeTestValues()[i], all.first()!!.mixedList[i])
        }

        val managedInnerObject = all.first()!!.mixedList.last()!!.asRealmModel(PrimaryKeyAsString::class.java)
        assertTrue(managedInnerObject.isManaged)
        assertEquals("hello world2", managedInnerObject.name)
=======
        assertTrue(all.first()!!.mixedList.containsAll(testList2.asList()))

        val managedInnerObject = all.first()!!.mixedList.last()!!.asRealmModel(PrimaryKeyAsString::class.java)
        assertTrue(managedInnerObject.isManaged)
        assertEquals(TEST_VALUE_2, managedInnerObject.name)
>>>>>>> ct/mixed-datatype-story
    }

    @Test
    fun bulk_copyToRealm_realmModel() {
        val value1 = MixedNotIndexedWithPK(0)
<<<<<<< HEAD
        value1.mixed = Mixed.valueOf(PrimaryKeyAsString("hello world1"))

        val value2 = MixedNotIndexedWithPK(1)
        value2.mixed = Mixed.valueOf(PrimaryKeyAsString("hello world2"))
=======
        value1.mixed = Mixed.valueOf(PrimaryKeyAsString(TEST_VALUE_1))

        val value2 = MixedNotIndexedWithPK(1)
        value2.mixed = Mixed.valueOf(PrimaryKeyAsString(TEST_VALUE_2))
>>>>>>> ct/mixed-datatype-story

        realm.beginTransaction()
        val objects = realm.copyToRealm(arrayListOf(value1, value2))
        realm.commitTransaction()

        val managedInnerObject1 = objects[0].mixed!!.asRealmModel(PrimaryKeyAsString::class.java)
        val managedInnerObject2 = objects[1].mixed!!.asRealmModel(PrimaryKeyAsString::class.java)

        assertTrue(managedInnerObject1.isManaged)
        assertTrue(managedInnerObject2.isManaged)

<<<<<<< HEAD
        assertEquals("hello world1", managedInnerObject1.name)
        assertEquals("hello world2", managedInnerObject2.name)
=======
        assertEquals(TEST_VALUE_1, managedInnerObject1.name)
        assertEquals(TEST_VALUE_2, managedInnerObject2.name)
>>>>>>> ct/mixed-datatype-story
    }

    @Test
    fun bulk_copyToRealm_realmModelList() {
        val value1 = MixedRealmListWithPK(0)
<<<<<<< HEAD
        value1.mixedList = mixedTestValues()
        value1.mixedList.add(Mixed.valueOf(PrimaryKeyAsString("hello world1")))

        val value2 = MixedRealmListWithPK(1)
        value2.mixedList = mixedTestValues()
        value2.mixedList.add(Mixed.valueOf(PrimaryKeyAsString("hello world2")))
=======
        value1.mixedList = RealmList(*testList1)
        value1.mixedList.add(Mixed.valueOf(PrimaryKeyAsString(TEST_VALUE_1)))

        val value2 = MixedRealmListWithPK(1)
        value2.mixedList = RealmList(*testList2)
        value2.mixedList.add(Mixed.valueOf(PrimaryKeyAsString(TEST_VALUE_2)))
>>>>>>> ct/mixed-datatype-story

        realm.beginTransaction()
        val objects = realm.copyToRealm(arrayListOf(value1, value2))
        realm.commitTransaction()

<<<<<<< HEAD
        for (i in 0 until mixedTestValues().size) {
            assertEquals(mixedTestValues()[i], objects[0].mixedList[i])
            assertEquals(mixedTestValues()[i], objects[1].mixedList[i])
        }
=======
        assertTrue(objects[0].mixedList.containsAll(testList1.asList()))
        assertTrue(objects[1].mixedList.containsAll(testList2.asList()))
>>>>>>> ct/mixed-datatype-story

        val managedInnerObject1 = objects[0].mixedList.last()!!.asRealmModel(PrimaryKeyAsString::class.java)
        val managedInnerObject2 = objects[1].mixedList.last()!!.asRealmModel(PrimaryKeyAsString::class.java)

        assertTrue(managedInnerObject1.isManaged)
<<<<<<< HEAD
        assertEquals("hello world1", managedInnerObject1.name)

        assertTrue(managedInnerObject2.isManaged)
        assertEquals("hello world2", managedInnerObject2.name)
=======
        assertEquals(TEST_VALUE_1, managedInnerObject1.name)

        assertTrue(managedInnerObject2.isManaged)
        assertEquals(TEST_VALUE_2, managedInnerObject2.name)
>>>>>>> ct/mixed-datatype-story
    }

    @Test
    fun bulk_copyToRealmOrUpdate_realmModel() {
        realm.executeTransaction { realm ->
            val obj1 = realm.createObject<MixedNotIndexedWithPK>(0)
<<<<<<< HEAD
            obj1.mixed = Mixed.valueOf(PrimaryKeyAsString("hello world1"))

            val obj2 = realm.createObject<MixedNotIndexedWithPK>(1)
            obj2.mixed = Mixed.valueOf(PrimaryKeyAsString("hello world2"))
        }

        val value1 = MixedNotIndexedWithPK(0)
        value1.mixed = Mixed.valueOf(PrimaryKeyAsString("hello world3"))

        val value2 = MixedNotIndexedWithPK(1)
        value2.mixed = Mixed.valueOf(PrimaryKeyAsString("hello world4"))
=======
            obj1.mixed = Mixed.valueOf(PrimaryKeyAsString(TEST_VALUE_1))

            val obj2 = realm.createObject<MixedNotIndexedWithPK>(1)
            obj2.mixed = Mixed.valueOf(PrimaryKeyAsString(TEST_VALUE_2))
        }

        val value1 = MixedNotIndexedWithPK(0)
        value1.mixed = Mixed.valueOf(PrimaryKeyAsString(TEST_VALUE_3))

        val value2 = MixedNotIndexedWithPK(1)
        value2.mixed = Mixed.valueOf(PrimaryKeyAsString(TEST_VALUE_4))
>>>>>>> ct/mixed-datatype-story

        realm.beginTransaction()
        val objects = realm.copyToRealmOrUpdate(arrayListOf(value1, value2))
        realm.commitTransaction()

        val managedInnerObject1 = objects[0].mixed!!.asRealmModel(PrimaryKeyAsString::class.java)
        val managedInnerObject2 = objects[1].mixed!!.asRealmModel(PrimaryKeyAsString::class.java)

        assertTrue(managedInnerObject1.isManaged)
<<<<<<< HEAD
        assertEquals("hello world3", managedInnerObject1.name)

        assertTrue(managedInnerObject2.isManaged)
        assertEquals("hello world4", managedInnerObject2.name)
=======
        assertEquals(TEST_VALUE_3, managedInnerObject1.name)

        assertTrue(managedInnerObject2.isManaged)
        assertEquals(TEST_VALUE_4, managedInnerObject2.name)
>>>>>>> ct/mixed-datatype-story
    }

    @Test
    fun bulk_copyToRealmOrUpdate_realmModelList() {
        realm.executeTransaction { realm ->
            val value1 = realm.createObject<MixedRealmListWithPK>(0)
<<<<<<< HEAD
            value1.mixedList = mixedTestValues()
            value1.mixedList.add(Mixed.valueOf(PrimaryKeyAsString("hello world1")))

            val value2 = realm.createObject<MixedRealmListWithPK>(1)
            value2.mixedList = mixedTestValues()
            value2.mixedList.add(Mixed.valueOf(PrimaryKeyAsString("hello world1")))
        }

        val value1 = MixedRealmListWithPK(0)
        value1.mixedList = mixedAlternativeTestValues()
        value1.mixedList.add(Mixed.valueOf(PrimaryKeyAsString("hello world3")))

        val value2 = MixedRealmListWithPK(1)
        value2.mixedList = mixedAlternativeTestValues()
        value2.mixedList.add(Mixed.valueOf(PrimaryKeyAsString("hello world4")))
=======
            value1.mixedList = RealmList(*testList1)
            value1.mixedList.add(Mixed.valueOf(PrimaryKeyAsString(TEST_VALUE_1)))

            val value2 = realm.createObject<MixedRealmListWithPK>(1)
            value2.mixedList = RealmList(*testList1)
            value2.mixedList.add(Mixed.valueOf(PrimaryKeyAsString(TEST_VALUE_1)))
        }

        val value1 = MixedRealmListWithPK(0)
        value1.mixedList = RealmList(*testList2)
        value1.mixedList.add(Mixed.valueOf(PrimaryKeyAsString(TEST_VALUE_3)))

        val value2 = MixedRealmListWithPK(1)
        value2.mixedList = RealmList(*testList2)
        value2.mixedList.add(Mixed.valueOf(PrimaryKeyAsString(TEST_VALUE_4)))
>>>>>>> ct/mixed-datatype-story

        realm.beginTransaction()
        val objects = realm.copyToRealmOrUpdate(arrayListOf(value1, value2))
        realm.commitTransaction()

<<<<<<< HEAD
        for (i in 0 until mixedTestValues().size) {
            assertEquals(mixedAlternativeTestValues()[i], objects[0].mixedList[i])
            assertEquals(mixedAlternativeTestValues()[i], objects[1].mixedList[i])
        }
=======
        assertTrue(objects[0].mixedList.containsAll(testList2.asList()))
        assertTrue(objects[1].mixedList.containsAll(testList2.asList()))
>>>>>>> ct/mixed-datatype-story

        val managedInnerObject1 = objects[0].mixedList.last()!!.asRealmModel(PrimaryKeyAsString::class.java)
        val managedInnerObject2 = objects[1].mixedList.last()!!.asRealmModel(PrimaryKeyAsString::class.java)

        assertTrue(managedInnerObject1.isManaged)
<<<<<<< HEAD
        assertEquals("hello world3", managedInnerObject1.name)

        assertTrue(managedInnerObject2.isManaged)
        assertEquals("hello world4", managedInnerObject2.name)
=======
        assertEquals(TEST_VALUE_3, managedInnerObject1.name)

        assertTrue(managedInnerObject2.isManaged)
        assertEquals(TEST_VALUE_4, managedInnerObject2.name)
>>>>>>> ct/mixed-datatype-story
    }

    @Test
    fun bulk_insert_realmModel() {
        realm.executeTransaction { realm ->
            val value1 = MixedNotIndexedWithPK(0)
<<<<<<< HEAD
            value1.mixed = Mixed.valueOf(PrimaryKeyAsString("hello world1"))

            val value2 = MixedNotIndexedWithPK(1)
            value2.mixed = Mixed.valueOf(PrimaryKeyAsString("hello world2"))
=======
            value1.mixed = Mixed.valueOf(PrimaryKeyAsString(TEST_VALUE_1))

            val value2 = MixedNotIndexedWithPK(1)
            value2.mixed = Mixed.valueOf(PrimaryKeyAsString(TEST_VALUE_2))
>>>>>>> ct/mixed-datatype-story

            realm.insert(arrayListOf(value1, value2))
        }

<<<<<<< HEAD
        val objects = realm.where<MixedNotIndexedWithPK>().findAll()!!
=======
        val objects = realm.where<MixedNotIndexedWithPK>().findAll()
>>>>>>> ct/mixed-datatype-story

        val managedInnerObject1 = objects[0]!!.mixed!!.asRealmModel(PrimaryKeyAsString::class.java)
        val managedInnerObject2 = objects[1]!!.mixed!!.asRealmModel(PrimaryKeyAsString::class.java)

        assertTrue(managedInnerObject1.isManaged)
<<<<<<< HEAD
        assertEquals("hello world1", managedInnerObject1.name)

        assertTrue(managedInnerObject2.isManaged)
        assertEquals("hello world2", managedInnerObject2.name)
=======
        assertEquals(TEST_VALUE_1, managedInnerObject1.name)

        assertTrue(managedInnerObject2.isManaged)
        assertEquals(TEST_VALUE_2, managedInnerObject2.name)
>>>>>>> ct/mixed-datatype-story
    }

    @Test
    fun bulk_insert_realmModelList() {
        realm.executeTransaction { realm ->
            val value1 = MixedRealmListWithPK(0)
<<<<<<< HEAD
            value1.mixedList = mixedTestValues()
            value1.mixedList.add(Mixed.valueOf(PrimaryKeyAsString("hello world1")))

            val value2 = MixedRealmListWithPK(1)
            value2.mixedList = mixedTestValues()
            value2.mixedList.add(Mixed.valueOf(PrimaryKeyAsString("hello world2")))
=======
            value1.mixedList = RealmList(*testList1)
            value1.mixedList.add(Mixed.valueOf(PrimaryKeyAsString(TEST_VALUE_1)))

            val value2 = MixedRealmListWithPK(1)
            value2.mixedList = RealmList(*testList1)
            value2.mixedList.add(Mixed.valueOf(PrimaryKeyAsString(TEST_VALUE_2)))
>>>>>>> ct/mixed-datatype-story

            realm.insert(arrayListOf(value1, value2))
        }

<<<<<<< HEAD
        val objects = realm.where<MixedRealmListWithPK>().findAll()!!

        for (i in 0 until mixedTestValues().size) {
            assertEquals(mixedTestValues()[i], objects[0]!!.mixedList[i])
            assertEquals(mixedTestValues()[i], objects[1]!!.mixedList[i])
        }
=======
        val objects = realm.where<MixedRealmListWithPK>().findAll()

        assertTrue(objects[0]!!.mixedList.containsAll(testList1.asList()))
        assertTrue(objects[1]!!.mixedList.containsAll(testList1.asList()))
>>>>>>> ct/mixed-datatype-story

        val managedInnerObject1 = objects[0]!!.mixedList.last()!!.asRealmModel(PrimaryKeyAsString::class.java)
        val managedInnerObject2 = objects[1]!!.mixedList.last()!!.asRealmModel(PrimaryKeyAsString::class.java)

        assertTrue(managedInnerObject1.isManaged)
<<<<<<< HEAD
        assertEquals("hello world1", managedInnerObject1.name)

        assertTrue(managedInnerObject2.isManaged)
        assertEquals("hello world2", managedInnerObject2.name)
=======
        assertEquals(TEST_VALUE_1, managedInnerObject1.name)

        assertTrue(managedInnerObject2.isManaged)
        assertEquals(TEST_VALUE_2, managedInnerObject2.name)
>>>>>>> ct/mixed-datatype-story
    }

    @Test
    fun bulk_insertOrUpdate_realmModel() {
        realm.executeTransaction { realm ->
            val value1 = realm.createObject<MixedNotIndexedWithPK>(0)
<<<<<<< HEAD
            value1.mixed = Mixed.valueOf(PrimaryKeyAsString("hello world1"))


            val value2 = realm.createObject<MixedNotIndexedWithPK>(1)
            value2.mixed = Mixed.valueOf(PrimaryKeyAsString("hello world2"))
=======
            value1.mixed = Mixed.valueOf(PrimaryKeyAsString(TEST_VALUE_1))

            val value2 = realm.createObject<MixedNotIndexedWithPK>(1)
            value2.mixed = Mixed.valueOf(PrimaryKeyAsString(TEST_VALUE_2))
>>>>>>> ct/mixed-datatype-story
        }

        realm.executeTransaction { realm ->
            val value1 = MixedNotIndexedWithPK(0)
<<<<<<< HEAD
            value1.mixed = Mixed.valueOf(PrimaryKeyAsString("hello world3"))

            val value2 = MixedNotIndexedWithPK(1)
            value2.mixed = Mixed.valueOf(PrimaryKeyAsString("hello world4"))
=======
            value1.mixed = Mixed.valueOf(PrimaryKeyAsString(TEST_VALUE_3))

            val value2 = MixedNotIndexedWithPK(1)
            value2.mixed = Mixed.valueOf(PrimaryKeyAsString(TEST_VALUE_4))
>>>>>>> ct/mixed-datatype-story

            realm.insertOrUpdate(arrayListOf(value1, value2))
        }

        val all = realm.where<MixedNotIndexedWithPK>().findAll()

        assertEquals(2, all.size)

        val managedInnerObject1 = all[0]!!.mixed!!.asRealmModel(PrimaryKeyAsString::class.java)
        val managedInnerObject2 = all[1]!!.mixed!!.asRealmModel(PrimaryKeyAsString::class.java)

        assertTrue(managedInnerObject1.isManaged)
<<<<<<< HEAD
        assertEquals("hello world3", managedInnerObject1.name)

        assertTrue(managedInnerObject2.isManaged)
        assertEquals("hello world4", managedInnerObject2.name)
=======
        assertEquals(TEST_VALUE_3, managedInnerObject1.name)

        assertTrue(managedInnerObject2.isManaged)
        assertEquals(TEST_VALUE_4, managedInnerObject2.name)
>>>>>>> ct/mixed-datatype-story
    }

    @Test
    fun bulk_insertOrUpdate_realmModelList() {
        realm.executeTransaction { realm ->
            val value1 = realm.createObject<MixedRealmListWithPK>(0)
<<<<<<< HEAD
            value1.mixedList = mixedTestValues()
            value1.mixedList.add(Mixed.valueOf(PrimaryKeyAsString("hello world1")))

            val value2 = realm.createObject<MixedRealmListWithPK>(1)
            value2.mixedList = mixedTestValues()
            value2.mixedList.add(Mixed.valueOf(PrimaryKeyAsString("hello world2")))
=======
            value1.mixedList = RealmList(*testList1)
            value1.mixedList.add(Mixed.valueOf(PrimaryKeyAsString("hello world1")))

            val value2 = realm.createObject<MixedRealmListWithPK>(1)
            value2.mixedList = RealmList(*testList1)
            value2.mixedList.add(Mixed.valueOf(PrimaryKeyAsString(TEST_VALUE_2)))
>>>>>>> ct/mixed-datatype-story
        }

        realm.executeTransaction { realm ->
            val value1 = MixedRealmListWithPK(0)
<<<<<<< HEAD
            value1.mixedList = mixedAlternativeTestValues()
            value1.mixedList.add(Mixed.valueOf(PrimaryKeyAsString("hello world3")))

            val value2 = MixedRealmListWithPK(1)
            value2.mixedList = mixedAlternativeTestValues()
            value2.mixedList.add(Mixed.valueOf(PrimaryKeyAsString("hello world4")))
=======
            value1.mixedList = RealmList(*testList2)
            value1.mixedList.add(Mixed.valueOf(PrimaryKeyAsString(TEST_VALUE_3)))

            val value2 = MixedRealmListWithPK(1)
            value2.mixedList = RealmList(*testList2)
            value2.mixedList.add(Mixed.valueOf(PrimaryKeyAsString(TEST_VALUE_4)))
>>>>>>> ct/mixed-datatype-story

            realm.insertOrUpdate(arrayListOf(value1, value2))
        }

        val all = realm.where<MixedRealmListWithPK>().findAll()
        assertEquals(2, all.size)

<<<<<<< HEAD
        for (i in 0 until mixedAlternativeTestValues().size) {
            assertEquals(mixedAlternativeTestValues()[i], all[0]!!.mixedList[i])
            assertEquals(mixedAlternativeTestValues()[i], all[1]!!.mixedList[i])
        }

        val managedInnerObject1 = all[0]!!.mixedList.last()!!.asRealmModel(PrimaryKeyAsString::class.java)
        assertTrue(managedInnerObject1.isManaged)
        assertEquals("hello world3", managedInnerObject1.name)

        val managedInnerObject2 = all[1]!!.mixedList.last()!!.asRealmModel(PrimaryKeyAsString::class.java)
        assertTrue(managedInnerObject2.isManaged)
        assertEquals("hello world4", managedInnerObject2.name)
=======
        assertTrue(all[0]!!.mixedList.containsAll(testList2.asList()))
        assertTrue(all[1]!!.mixedList.containsAll(testList2.asList()))

        val managedInnerObject1 = all[0]!!.mixedList.last()!!.asRealmModel(PrimaryKeyAsString::class.java)
        assertTrue(managedInnerObject1.isManaged)
        assertEquals(TEST_VALUE_3, managedInnerObject1.name)

        val managedInnerObject2 = all[1]!!.mixedList.last()!!.asRealmModel(PrimaryKeyAsString::class.java)
        assertTrue(managedInnerObject2.isManaged)
        assertEquals(TEST_VALUE_4, managedInnerObject2.name)
>>>>>>> ct/mixed-datatype-story
    }
}
