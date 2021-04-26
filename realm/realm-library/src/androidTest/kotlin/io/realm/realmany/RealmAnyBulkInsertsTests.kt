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

import androidx.test.ext.junit.runners.AndroidJUnit4
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
import java.util.*
import kotlin.test.*

@RunWith(AndroidJUnit4::class)
class RealmAnyBulkInsertsTests {
    private lateinit var realmConfiguration: RealmConfiguration
    private lateinit var realm: Realm

    companion object {
        const val TEST_VALUE_1 = "hello world 1"
        const val TEST_VALUE_2 = "hello world 2"
        const val TEST_VALUE_3 = "hello world 3"
        const val TEST_VALUE_4 = "hello world 4"
    }

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

    private val testList1 = arrayOf(
            RealmAny.valueOf(10.toByte()),
            RealmAny.valueOf(20.toShort()),
            RealmAny.valueOf(30.toInt()),
            RealmAny.valueOf(40.toLong()),
            RealmAny.valueOf(true),
            RealmAny.valueOf(TEST_VALUE_1),
            RealmAny.valueOf(byteArrayOf(0, 1, 1)),
            RealmAny.valueOf(Date(10)),
            RealmAny.valueOf(50.toFloat()),
            RealmAny.valueOf(60.toDouble()),
            RealmAny.valueOf(Decimal128(10)),
            RealmAny.valueOf(ObjectId(Date(100))),
            RealmAny.valueOf(UUID.randomUUID())
    )

    private val testList2 = arrayOf(
            RealmAny.valueOf(1.toByte()),
            RealmAny.valueOf(2.toShort()),
            RealmAny.valueOf(3.toInt()),
            RealmAny.valueOf(4.toLong()),
            RealmAny.valueOf(false),
            RealmAny.valueOf(TEST_VALUE_2),
            RealmAny.valueOf(byteArrayOf(0, 1, 0)),
            RealmAny.valueOf(Date(0)),
            RealmAny.valueOf(5.toFloat()),
            RealmAny.valueOf(6.toDouble()),
            RealmAny.valueOf(Decimal128(1)),
            RealmAny.valueOf(ObjectId(Date(10))),
            RealmAny.valueOf(UUID.randomUUID())
    )

    /**
     * RealmAny tests for Realm objects
     */

    @Test
    fun copyFromRealm_realmModel() {
        realm.beginTransaction()
        val value = realm.createObject<RealmAnyNotIndexedWithPK>(0)

        val inner = RealmAnyNotIndexedWithPK(1)
        val allTypes = AllJavaTypes(0)
        allTypes.fieldString = TEST_VALUE_1
        val innerAllJavaTypes = AllJavaTypes(1)
        innerAllJavaTypes.fieldString = TEST_VALUE_2

        allTypes.fieldObject = innerAllJavaTypes

        inner.realmAny = RealmAny.valueOf(allTypes)
        value.realmAny = RealmAny.valueOf(inner)

        realm.commitTransaction()

        val copy0 = realm.copyFromRealm(value, 0)
        val copy1 = realm.copyFromRealm(value, 1)
        val copy2 = realm.copyFromRealm(value, 2)
        val copy3 = realm.copyFromRealm(value, 3)

        assertFalse(copy0.isManaged)
        assertFalse(copy1.isManaged)
        assertFalse(copy2.isManaged)
        assertFalse(copy3.isManaged)

        assertEquals(RealmAny.nullValue(), copy0.realmAny)
        assertEquals(RealmAny.nullValue(), copy1.realmAny!!.asRealmModel(RealmAnyNotIndexedWithPK::class.java).realmAny)
        assertEquals(TEST_VALUE_1, copy2.realmAny!!.asRealmModel(RealmAnyNotIndexedWithPK::class.java).realmAny!!.asRealmModel(AllJavaTypes::class.java).fieldString)
        assertNull(copy2.realmAny!!.asRealmModel(RealmAnyNotIndexedWithPK::class.java).realmAny!!.asRealmModel(AllJavaTypes::class.java).fieldObject)

        assertEquals(TEST_VALUE_1, copy3.realmAny!!.asRealmModel(RealmAnyNotIndexedWithPK::class.java).realmAny!!.asRealmModel(AllJavaTypes::class.java).fieldString)
        assertNotNull(copy3.realmAny!!.asRealmModel(RealmAnyNotIndexedWithPK::class.java).realmAny!!.asRealmModel(AllJavaTypes::class.java).fieldObject)
        assertEquals(TEST_VALUE_2, copy3.realmAny!!.asRealmModel(RealmAnyNotIndexedWithPK::class.java).realmAny!!.asRealmModel(AllJavaTypes::class.java).fieldObject.fieldString)
    }

    @Test
    fun copyToRealm_realmModel() {
        val value = RealmAnyNotIndexedWithPK()
        value.realmAny = RealmAny.valueOf(PrimaryKeyAsString(TEST_VALUE_1))

        realm.beginTransaction()
        val managedValue = realm.copyToRealm(value)
        realm.commitTransaction()

        val managedInnerObject = managedValue.realmAny!!.asRealmModel(PrimaryKeyAsString::class.java)!!

        assertTrue(managedInnerObject.isManaged)
        assertEquals(TEST_VALUE_1, managedInnerObject.name)
    }

    @Test
    fun copyToRealmOrUpdate_realmModel() {
        realm.executeTransaction { realm ->
            val obj = realm.createObject<RealmAnyNotIndexedWithPK>(0)
            obj.realmAny = RealmAny.valueOf(PrimaryKeyAsString(TEST_VALUE_1))
        }

        val value = RealmAnyNotIndexedWithPK()
        value.realmAny = RealmAny.valueOf(PrimaryKeyAsString(TEST_VALUE_2))

        realm.beginTransaction()
        val managedValue = realm.copyToRealmOrUpdate(value)
        realm.commitTransaction()

        val managedInnerObject = managedValue.realmAny!!.asRealmModel(PrimaryKeyAsString::class.java)
        assertTrue(managedInnerObject.isManaged)
        assertEquals(TEST_VALUE_2, managedInnerObject.name)
    }

    @Test
    fun insert_realmModel() {
        realm.executeTransaction { realm ->
            val value = RealmAnyNotIndexedWithPK()
            value.realmAny = RealmAny.valueOf(PrimaryKeyAsString(TEST_VALUE_1))

            realm.insert(value)
        }

        val managedValue = realm.where<RealmAnyNotIndexedWithPK>().findFirst()!!
        val managedInnerObject = managedValue.realmAny!!.asRealmModel(PrimaryKeyAsString::class.java)

        assertTrue(managedInnerObject.isManaged)
        assertEquals(TEST_VALUE_1, managedInnerObject.name)
    }

    @Test
    fun insertOrUpdate_realmModel() {
        realm.executeTransaction { realm ->
            val value = realm.createObject<RealmAnyNotIndexedWithPK>(0)
            value.realmAny = RealmAny.valueOf(PrimaryKeyAsString(TEST_VALUE_1))
        }

        realm.executeTransaction { realm ->
            val value = RealmAnyNotIndexedWithPK()
            value.realmAny = RealmAny.valueOf(PrimaryKeyAsString(TEST_VALUE_2))

            realm.insertOrUpdate(value)
        }

        val all = realm.where<RealmAnyNotIndexedWithPK>().findAll()

        assertEquals(1, all.size)

        val managedInnerObject = all.first()!!.realmAny!!.asRealmModel(PrimaryKeyAsString::class.java)

        assertTrue(managedInnerObject.isManaged)
        assertEquals(TEST_VALUE_2, managedInnerObject.name)
    }

    /**
     * Tests for RealmAny RealmLists
     */

    @Test
    fun copyFromRealm_list() {
        val list = RealmList(*testList1)
        val inner = RealmAnyNotIndexedWithPK(1)
        inner.realmAny = RealmAny.valueOf(PrimaryKeyAsString(TEST_VALUE_1))

        list.add(RealmAny.valueOf(inner))

        realm.beginTransaction()
        val value = realm.createObject<RealmAnyRealmListWithPK>(0)
        value.realmAnyList = list
        realm.commitTransaction()

        val copy0 = realm.copyFromRealm(value, 0)
        val copy1 = realm.copyFromRealm(value, 1)
        val copy2 = realm.copyFromRealm(value, 2)

        assertFalse(copy0.isManaged)
        assertFalse(copy1.isManaged)
        assertFalse(copy2.isManaged)

        assertNull(copy0.realmAnyList)
        assertTrue(copy1.realmAnyList.containsAll(testList1.asList()))
        assertEquals(RealmAny.nullValue(), copy1.realmAnyList.last()!!.asRealmModel(RealmAnyNotIndexedWithPK::class.java).realmAny)
        assertEquals(TEST_VALUE_1, copy2.realmAnyList.last()!!.asRealmModel(RealmAnyNotIndexedWithPK::class.java).realmAny!!.asRealmModel(PrimaryKeyAsString::class.java).name)
    }

    @Test
    fun copyToRealmOrUpdate_list() {
        realm.executeTransaction { realm ->
            val value = realm.createObject<RealmAnyRealmListWithPK>(0)
            value.realmAnyList = RealmList(*testList1)
            value.realmAnyList.add(RealmAny.valueOf(PrimaryKeyAsString(TEST_VALUE_1)))
        }

        val update = RealmAnyRealmListWithPK(0)
        update.realmAnyList = RealmList(*testList2)
        update.realmAnyList.add(RealmAny.valueOf(PrimaryKeyAsString(TEST_VALUE_2)))

        realm.beginTransaction()
        val managedValue = realm.copyToRealmOrUpdate(update)
        realm.commitTransaction()

        assertTrue(managedValue.realmAnyList.containsAll(testList2.asList()))

        val managedInnerObject = managedValue.realmAnyList.last()!!.asRealmModel(PrimaryKeyAsString::class.java)
        assertTrue(managedInnerObject.isManaged)
        assertEquals(TEST_VALUE_2, managedInnerObject.name)
    }

    @Test
    fun insert_realmModelList() {
        realm.executeTransaction { realm ->
            val value = RealmAnyRealmListWithPK()
            value.realmAnyList = RealmList(*testList1)
            value.realmAnyList.add(RealmAny.valueOf(PrimaryKeyAsString(TEST_VALUE_1)))

            realm.insert(value)
        }

        val managedValue = realm.where<RealmAnyRealmListWithPK>().findFirst()!!

        val managedInnerObject = managedValue.realmAnyList.last()!!.asRealmModel(PrimaryKeyAsString::class.java)

        assertTrue(managedInnerObject.isManaged)
        assertTrue(managedValue.realmAnyList.containsAll(testList1.asList()))
        assertEquals(TEST_VALUE_1, managedInnerObject.name)
    }

    @Test
    fun insertOrUpdate_realmModelList() {
        realm.executeTransaction { realm ->
            val value = realm.createObject<RealmAnyRealmListWithPK>(0)
            value.realmAnyList = RealmList(*testList1)
            value.realmAnyList.add(RealmAny.valueOf(PrimaryKeyAsString(TEST_VALUE_1)))
        }

        realm.executeTransaction { realm ->
            val value = RealmAnyRealmListWithPK(0)
            value.realmAnyList = RealmList(*testList2)
            value.realmAnyList.add(RealmAny.valueOf(PrimaryKeyAsString(TEST_VALUE_2)))

            realm.insertOrUpdate(value)
        }

        val all = realm.where<RealmAnyRealmListWithPK>().findAll()
        assertEquals(1, all.size)

        assertTrue(all.first()!!.realmAnyList.containsAll(testList2.asList()))

        val managedInnerObject = all.first()!!.realmAnyList.last()!!.asRealmModel(PrimaryKeyAsString::class.java)
        assertTrue(managedInnerObject.isManaged)
        assertEquals(TEST_VALUE_2, managedInnerObject.name)
    }

    @Test
    fun bulk_copyToRealm_realmModel() {
        val value1 = RealmAnyNotIndexedWithPK(0)
        value1.realmAny = RealmAny.valueOf(PrimaryKeyAsString(TEST_VALUE_1))

        val value2 = RealmAnyNotIndexedWithPK(1)
        value2.realmAny = RealmAny.valueOf(PrimaryKeyAsString(TEST_VALUE_2))

        realm.beginTransaction()
        val objects = realm.copyToRealm(arrayListOf(value1, value2))
        realm.commitTransaction()

        val managedInnerObject1 = objects[0].realmAny!!.asRealmModel(PrimaryKeyAsString::class.java)
        val managedInnerObject2 = objects[1].realmAny!!.asRealmModel(PrimaryKeyAsString::class.java)

        assertTrue(managedInnerObject1.isManaged)
        assertTrue(managedInnerObject2.isManaged)

        assertEquals(TEST_VALUE_1, managedInnerObject1.name)
        assertEquals(TEST_VALUE_2, managedInnerObject2.name)
    }

    @Test
    fun bulk_copyToRealm_realmModelList() {
        val value1 = RealmAnyRealmListWithPK(0)
        value1.realmAnyList = RealmList(*testList1)
        value1.realmAnyList.add(RealmAny.valueOf(PrimaryKeyAsString(TEST_VALUE_1)))

        val value2 = RealmAnyRealmListWithPK(1)
        value2.realmAnyList = RealmList(*testList2)
        value2.realmAnyList.add(RealmAny.valueOf(PrimaryKeyAsString(TEST_VALUE_2)))

        realm.beginTransaction()
        val objects = realm.copyToRealm(arrayListOf(value1, value2))
        realm.commitTransaction()

        assertTrue(objects[0].realmAnyList.containsAll(testList1.asList()))
        assertTrue(objects[1].realmAnyList.containsAll(testList2.asList()))

        val managedInnerObject1 = objects[0].realmAnyList.last()!!.asRealmModel(PrimaryKeyAsString::class.java)
        val managedInnerObject2 = objects[1].realmAnyList.last()!!.asRealmModel(PrimaryKeyAsString::class.java)

        assertTrue(managedInnerObject1.isManaged)
        assertEquals(TEST_VALUE_1, managedInnerObject1.name)

        assertTrue(managedInnerObject2.isManaged)
        assertEquals(TEST_VALUE_2, managedInnerObject2.name)
    }

    @Test
    fun bulk_copyToRealmOrUpdate_realmModel() {
        realm.executeTransaction { realm ->
            val obj1 = realm.createObject<RealmAnyNotIndexedWithPK>(0)
            obj1.realmAny = RealmAny.valueOf(PrimaryKeyAsString(TEST_VALUE_1))

            val obj2 = realm.createObject<RealmAnyNotIndexedWithPK>(1)
            obj2.realmAny = RealmAny.valueOf(PrimaryKeyAsString(TEST_VALUE_2))
        }

        val value1 = RealmAnyNotIndexedWithPK(0)
        value1.realmAny = RealmAny.valueOf(PrimaryKeyAsString(TEST_VALUE_3))

        val value2 = RealmAnyNotIndexedWithPK(1)
        value2.realmAny = RealmAny.valueOf(PrimaryKeyAsString(TEST_VALUE_4))

        realm.beginTransaction()
        val objects = realm.copyToRealmOrUpdate(arrayListOf(value1, value2))
        realm.commitTransaction()

        val managedInnerObject1 = objects[0].realmAny!!.asRealmModel(PrimaryKeyAsString::class.java)
        val managedInnerObject2 = objects[1].realmAny!!.asRealmModel(PrimaryKeyAsString::class.java)

        assertTrue(managedInnerObject1.isManaged)
        assertEquals(TEST_VALUE_3, managedInnerObject1.name)

        assertTrue(managedInnerObject2.isManaged)
        assertEquals(TEST_VALUE_4, managedInnerObject2.name)
    }

    @Test
    fun bulk_copyToRealmOrUpdate_realmModelList() {
        realm.executeTransaction { realm ->
            val value1 = realm.createObject<RealmAnyRealmListWithPK>(0)
            value1.realmAnyList = RealmList(*testList1)
            value1.realmAnyList.add(RealmAny.valueOf(PrimaryKeyAsString(TEST_VALUE_1)))

            val value2 = realm.createObject<RealmAnyRealmListWithPK>(1)
            value2.realmAnyList = RealmList(*testList1)
            value2.realmAnyList.add(RealmAny.valueOf(PrimaryKeyAsString(TEST_VALUE_1)))
        }

        val value1 = RealmAnyRealmListWithPK(0)
        value1.realmAnyList = RealmList(*testList2)
        value1.realmAnyList.add(RealmAny.valueOf(PrimaryKeyAsString(TEST_VALUE_3)))

        val value2 = RealmAnyRealmListWithPK(1)
        value2.realmAnyList = RealmList(*testList2)
        value2.realmAnyList.add(RealmAny.valueOf(PrimaryKeyAsString(TEST_VALUE_4)))

        realm.beginTransaction()
        val objects = realm.copyToRealmOrUpdate(arrayListOf(value1, value2))
        realm.commitTransaction()

        assertTrue(objects[0].realmAnyList.containsAll(testList2.asList()))
        assertTrue(objects[1].realmAnyList.containsAll(testList2.asList()))

        val managedInnerObject1 = objects[0].realmAnyList.last()!!.asRealmModel(PrimaryKeyAsString::class.java)
        val managedInnerObject2 = objects[1].realmAnyList.last()!!.asRealmModel(PrimaryKeyAsString::class.java)

        assertTrue(managedInnerObject1.isManaged)
        assertEquals(TEST_VALUE_3, managedInnerObject1.name)

        assertTrue(managedInnerObject2.isManaged)
        assertEquals(TEST_VALUE_4, managedInnerObject2.name)
    }

    @Test
    fun bulk_insert_realmModel() {
        realm.executeTransaction { realm ->
            val value1 = RealmAnyNotIndexedWithPK(0)
            value1.realmAny = RealmAny.valueOf(PrimaryKeyAsString(TEST_VALUE_1))

            val value2 = RealmAnyNotIndexedWithPK(1)
            value2.realmAny = RealmAny.valueOf(PrimaryKeyAsString(TEST_VALUE_2))

            realm.insert(arrayListOf(value1, value2))
        }

        val objects = realm.where<RealmAnyNotIndexedWithPK>().findAll()

        val managedInnerObject1 = objects[0]!!.realmAny!!.asRealmModel(PrimaryKeyAsString::class.java)
        val managedInnerObject2 = objects[1]!!.realmAny!!.asRealmModel(PrimaryKeyAsString::class.java)

        assertTrue(managedInnerObject1.isManaged)
        assertEquals(TEST_VALUE_1, managedInnerObject1.name)

        assertTrue(managedInnerObject2.isManaged)
        assertEquals(TEST_VALUE_2, managedInnerObject2.name)
    }

    @Test
    fun bulk_insert_realmModelList() {
        realm.executeTransaction { realm ->
            val value1 = RealmAnyRealmListWithPK(0)
            value1.realmAnyList = RealmList(*testList1)
            value1.realmAnyList.add(RealmAny.valueOf(PrimaryKeyAsString(TEST_VALUE_1)))

            val value2 = RealmAnyRealmListWithPK(1)
            value2.realmAnyList = RealmList(*testList1)
            value2.realmAnyList.add(RealmAny.valueOf(PrimaryKeyAsString(TEST_VALUE_2)))

            realm.insert(arrayListOf(value1, value2))
        }

        val objects = realm.where<RealmAnyRealmListWithPK>().findAll()

        assertTrue(objects[0]!!.realmAnyList.containsAll(testList1.asList()))
        assertTrue(objects[1]!!.realmAnyList.containsAll(testList1.asList()))

        val managedInnerObject1 = objects[0]!!.realmAnyList.last()!!.asRealmModel(PrimaryKeyAsString::class.java)
        val managedInnerObject2 = objects[1]!!.realmAnyList.last()!!.asRealmModel(PrimaryKeyAsString::class.java)

        assertTrue(managedInnerObject1.isManaged)
        assertEquals(TEST_VALUE_1, managedInnerObject1.name)

        assertTrue(managedInnerObject2.isManaged)
        assertEquals(TEST_VALUE_2, managedInnerObject2.name)
    }

    @Test
    fun bulk_insertOrUpdate_realmModel() {
        realm.executeTransaction { realm ->
            val value1 = realm.createObject<RealmAnyNotIndexedWithPK>(0)
            value1.realmAny = RealmAny.valueOf(PrimaryKeyAsString(TEST_VALUE_1))

            val value2 = realm.createObject<RealmAnyNotIndexedWithPK>(1)
            value2.realmAny = RealmAny.valueOf(PrimaryKeyAsString(TEST_VALUE_2))
        }

        realm.executeTransaction { realm ->
            val value1 = RealmAnyNotIndexedWithPK(0)
            value1.realmAny = RealmAny.valueOf(PrimaryKeyAsString(TEST_VALUE_3))

            val value2 = RealmAnyNotIndexedWithPK(1)
            value2.realmAny = RealmAny.valueOf(PrimaryKeyAsString(TEST_VALUE_4))

            realm.insertOrUpdate(arrayListOf(value1, value2))
        }

        val all = realm.where<RealmAnyNotIndexedWithPK>().findAll()

        assertEquals(2, all.size)

        val managedInnerObject1 = all[0]!!.realmAny!!.asRealmModel(PrimaryKeyAsString::class.java)
        val managedInnerObject2 = all[1]!!.realmAny!!.asRealmModel(PrimaryKeyAsString::class.java)

        assertTrue(managedInnerObject1.isManaged)
        assertEquals(TEST_VALUE_3, managedInnerObject1.name)

        assertTrue(managedInnerObject2.isManaged)
        assertEquals(TEST_VALUE_4, managedInnerObject2.name)
    }

    @Test
    fun bulk_insertOrUpdate_realmModelList() {
        realm.executeTransaction { realm ->
            val value1 = realm.createObject<RealmAnyRealmListWithPK>(0)
            value1.realmAnyList = RealmList(*testList1)
            value1.realmAnyList.add(RealmAny.valueOf(PrimaryKeyAsString("hello world1")))

            val value2 = realm.createObject<RealmAnyRealmListWithPK>(1)
            value2.realmAnyList = RealmList(*testList1)
            value2.realmAnyList.add(RealmAny.valueOf(PrimaryKeyAsString(TEST_VALUE_2)))
        }

        realm.executeTransaction { realm ->
            val value1 = RealmAnyRealmListWithPK(0)
            value1.realmAnyList = RealmList(*testList2)
            value1.realmAnyList.add(RealmAny.valueOf(PrimaryKeyAsString(TEST_VALUE_3)))

            val value2 = RealmAnyRealmListWithPK(1)
            value2.realmAnyList = RealmList(*testList2)
            value2.realmAnyList.add(RealmAny.valueOf(PrimaryKeyAsString(TEST_VALUE_4)))

            realm.insertOrUpdate(arrayListOf(value1, value2))
        }

        val all = realm.where<RealmAnyRealmListWithPK>().findAll()
        assertEquals(2, all.size)

        assertTrue(all[0]!!.realmAnyList.containsAll(testList2.asList()))
        assertTrue(all[1]!!.realmAnyList.containsAll(testList2.asList()))

        val managedInnerObject1 = all[0]!!.realmAnyList.last()!!.asRealmModel(PrimaryKeyAsString::class.java)
        assertTrue(managedInnerObject1.isManaged)
        assertEquals(TEST_VALUE_3, managedInnerObject1.name)

        val managedInnerObject2 = all[1]!!.realmAnyList.last()!!.asRealmModel(PrimaryKeyAsString::class.java)
        assertTrue(managedInnerObject2.isManaged)
        assertEquals(TEST_VALUE_4, managedInnerObject2.name)
    }
}
