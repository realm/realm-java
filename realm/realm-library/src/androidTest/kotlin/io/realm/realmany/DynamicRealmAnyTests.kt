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
import org.bson.types.Decimal128
import org.bson.types.ObjectId
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue


// FIXME: MIXED PARAMETRIZED TESTS FOR INDEXED AND UNINDEXED
@RunWith(AndroidJUnit4::class)
class DynamicRealmAnyTests {
    @get:Rule
    val configFactory = TestRealmConfigurationFactory()

    @Rule
    @JvmField
    val folder = TemporaryFolder()

    private lateinit var realm: DynamicRealm

    init {
        Realm.init(InstrumentationRegistry.getInstrumentation().targetContext)
    }

    @Before
    fun setUp() {
        realm = DynamicRealm.getInstance(configFactory.createConfiguration("RealmAny"))

        realm.executeTransaction {
            realm.schema
                    .create("RealmAnyObject")
                    .addField("myRealmAny", RealmAny::class.java)

            realm.schema
                    .create("RealmAnyListObject")
                    .addRealmListField("aList", RealmAny::class.java)

            realm.schema
                    .create("ObjectString")
                    .addField("aString", String::class.java, FieldAttribute.PRIMARY_KEY)
        }
    }

    @After
    fun tearDown() {
        realm.close()
    }

    @Test
    fun writeRead_primitive() {
        realm.beginTransaction()

        val anObject = realm.createObject("RealmAnyObject")
        anObject.setRealmAny("myRealmAny", RealmAny.valueOf(Date(10)))

        realm.commitTransaction()

        val myRealmAny = anObject.getRealmAny("myRealmAny")

        assertEquals(Date(10), myRealmAny.asDate())
        assertEquals(RealmAny.valueOf(Date(10)), myRealmAny)

        realm.close()
    }

    @Test
    fun defaultNullValue() {
        realm.beginTransaction()

        val anObject = realm.createObject("RealmAnyObject")

        realm.commitTransaction()

        val myRealmAny = anObject.getRealmAny("myRealmAny")

        assertNotNull(myRealmAny)
        assertTrue(myRealmAny.isNull)
        assertEquals(RealmAny.nullValue(), myRealmAny)
        assertEquals(RealmAnyType.NULL, myRealmAny.type)
    }

    @Test
    fun setNullValue() {
        realm.beginTransaction()

        val anObject = realm.createObject("RealmAnyObject")
        anObject.setRealmAny("myRealmAny", RealmAny.nullValue())

        realm.commitTransaction()

        val myRealmAny = anObject.getRealmAny("myRealmAny")

        assertTrue(myRealmAny.isNull)
        assertEquals(RealmAnyType.NULL, myRealmAny.type)
    }

    @Test
    fun writeRead_model() {
        realm.beginTransaction()

        val innerObject = realm.createObject("RealmAnyObject")
        innerObject.setRealmAny("myRealmAny", RealmAny.valueOf(Date(10)))

        val outerObject = realm.createObject("RealmAnyObject")
        outerObject.setRealmAny("myRealmAny", RealmAny.valueOf(innerObject))

        realm.commitTransaction()

        val innerRealmAny = innerObject.getRealmAny("myRealmAny")
        val outerRealmAny = outerObject.getRealmAny("myRealmAny")

        assertEquals(Date(10), innerRealmAny.asDate())
        assertEquals(DynamicRealmObject::class.java, outerRealmAny.valueClass)

        val aRealmAny = outerRealmAny
                .asRealmModel(DynamicRealmObject::class.java)
                .getRealmAny("myRealmAny")

        assertEquals(innerRealmAny.asDate(), aRealmAny.asDate())
    }

    @Test
    fun managed_listsAllTypes() {
        val aString = "a string"
        val byteArray = byteArrayOf(0, 1, 0)
        val date = Date()
        val objectId = ObjectId()
        val decimal128 = Decimal128(1)
        val uuid = UUID.randomUUID()

        realm.executeTransaction {
            val allJavaTypes = it.createObject("RealmAnyListObject")
            val realmAnyList = allJavaTypes.getList("aList", RealmAny::class.java)

            val dynamicRealmObject = it.createObject("ObjectString", "dynamic")

            realmAnyList.add(RealmAny.valueOf(true))
            realmAnyList.add(RealmAny.valueOf(1.toByte()))
            realmAnyList.add(RealmAny.valueOf(2.toShort()))
            realmAnyList.add(RealmAny.valueOf(3.toInt()))
            realmAnyList.add(RealmAny.valueOf(4.toLong()))
            realmAnyList.add(RealmAny.valueOf(5.toFloat()))
            realmAnyList.add(RealmAny.valueOf(6.toDouble()))
            realmAnyList.add(RealmAny.valueOf(aString))
            realmAnyList.add(RealmAny.valueOf(byteArray))
            realmAnyList.add(RealmAny.valueOf(date))
            realmAnyList.add(RealmAny.valueOf(objectId))
            realmAnyList.add(RealmAny.valueOf(decimal128))
            realmAnyList.add(RealmAny.valueOf(uuid))
            realmAnyList.add(RealmAny.nullValue())
            realmAnyList.add(null)
            realmAnyList.add(RealmAny.valueOf(dynamicRealmObject))
        }

        val allJavaTypes = realm.where("RealmAnyListObject").findFirst()
        val realmAnyList = allJavaTypes!!.getList("aList", RealmAny::class.java)

        assertEquals(true, realmAnyList[0]!!.asBoolean())
        assertEquals(1, realmAnyList[1]!!.asByte())
        assertEquals(2, realmAnyList[2]!!.asShort())
        assertEquals(3, realmAnyList[3]!!.asInteger())
        assertEquals(4, realmAnyList[4]!!.asLong())
        assertEquals(5.toFloat(), realmAnyList[5]!!.asFloat())
        assertEquals(6.toDouble(), realmAnyList[6]!!.asDouble())
        assertEquals(aString, realmAnyList[7]!!.asString())
        assertTrue(Arrays.equals(byteArray, realmAnyList[8]!!.asBinary()))
        assertEquals(date, realmAnyList[9]!!.asDate())
        assertEquals(objectId, realmAnyList[10]!!.asObjectId())
        assertEquals(decimal128, realmAnyList[11]!!.asDecimal128())
        assertEquals(uuid, realmAnyList[12]!!.asUUID())
        assertTrue(realmAnyList[13]!!.isNull)
        assertTrue(realmAnyList[14]!!.isNull)

        assertEquals("dynamic", realmAnyList[15]!!.asRealmModel(DynamicRealmObject::class.java).getString("aString"))
    }


    @Test
    fun managed_listsInsertAllTypes() {
        val aString = "a string"
        val byteArray = byteArrayOf(0, 1, 0)
        val date = Date()
        val objectId = ObjectId()
        val decimal128 = Decimal128(1)
        val uuid = UUID.randomUUID()

        realm.executeTransaction {
            val allJavaTypes = it.createObject("RealmAnyListObject")
            val realmAnyList = allJavaTypes.getList("aList", RealmAny::class.java)
            val dynamicRealmObject = it.createObject("ObjectString", "dynamic")

            realmAnyList.add(0, RealmAny.valueOf(true))
            realmAnyList.add(0, RealmAny.valueOf(1.toByte()))
            realmAnyList.add(0, RealmAny.valueOf(2.toShort()))
            realmAnyList.add(0, RealmAny.valueOf(3.toInt()))
            realmAnyList.add(0, RealmAny.valueOf(4.toLong()))
            realmAnyList.add(0, RealmAny.valueOf(5.toFloat()))
            realmAnyList.add(0, RealmAny.valueOf(6.toDouble()))
            realmAnyList.add(0, RealmAny.valueOf(aString))
            realmAnyList.add(0, RealmAny.valueOf(byteArray))
            realmAnyList.add(0, RealmAny.valueOf(date))
            realmAnyList.add(0, RealmAny.valueOf(objectId))
            realmAnyList.add(0, RealmAny.valueOf(decimal128))
            realmAnyList.add(0, RealmAny.valueOf(uuid))
            realmAnyList.add(0, RealmAny.nullValue())
            realmAnyList.add(0, null)
            realmAnyList.add(0, RealmAny.valueOf(dynamicRealmObject))
        }

        val allJavaTypes = realm.where("RealmAnyListObject").findFirst()
        val realmAnyList = allJavaTypes!!.getList("aList", RealmAny::class.java)

        assertEquals(true, realmAnyList[15]!!.asBoolean())
        assertEquals(1, realmAnyList[14]!!.asByte())
        assertEquals(2, realmAnyList[13]!!.asShort())
        assertEquals(3, realmAnyList[12]!!.asInteger())
        assertEquals(4, realmAnyList[11]!!.asLong())
        assertEquals(5.toFloat(), realmAnyList[10]!!.asFloat())
        assertEquals(6.toDouble(), realmAnyList[9]!!.asDouble())
        assertEquals(aString, realmAnyList[8]!!.asString())
        assertTrue(Arrays.equals(byteArray, realmAnyList[7]!!.asBinary()))
        assertEquals(date, realmAnyList[6]!!.asDate())
        assertEquals(objectId, realmAnyList[5]!!.asObjectId())
        assertEquals(decimal128, realmAnyList[4]!!.asDecimal128())
        assertEquals(uuid, realmAnyList[3]!!.asUUID())
        assertTrue(realmAnyList[2]!!.isNull)
        assertTrue(realmAnyList[1]!!.isNull)
        assertEquals("dynamic", realmAnyList[0]!!.asRealmModel(DynamicRealmObject::class.java).getString("aString"))
    }

    @Test
    fun managed_listsSetAllTypes() {
        val aString = "a string"
        val byteArray = byteArrayOf(0, 1, 0)
        val date = Date()
        val objectId = ObjectId()
        val decimal128 = Decimal128(1)
        val uuid = UUID.randomUUID()

        realm.executeTransaction {
            val allJavaTypes = it.createObject("RealmAnyListObject")
            val dynamicRealmObject = it.createObject("ObjectString", "dynamic")

            val initialList = RealmList<RealmAny>()
            initialList.addAll(arrayOfNulls(16))
            allJavaTypes.setList("aList", initialList)

            val realmAnyList = allJavaTypes.getList("aList", RealmAny::class.java)

            realmAnyList[0] = RealmAny.valueOf(true)
            realmAnyList[1] = RealmAny.valueOf(1.toByte())
            realmAnyList[2] = RealmAny.valueOf(2.toShort())
            realmAnyList[3] = RealmAny.valueOf(3.toInt())
            realmAnyList[4] = RealmAny.valueOf(4.toLong())
            realmAnyList[5] = RealmAny.valueOf(5.toFloat())
            realmAnyList[6] = RealmAny.valueOf(6.toDouble())
            realmAnyList[7] = RealmAny.valueOf(aString)
            realmAnyList[8] = RealmAny.valueOf(byteArray)
            realmAnyList[9] = RealmAny.valueOf(date)
            realmAnyList[10] = RealmAny.valueOf(objectId)
            realmAnyList[11] = RealmAny.valueOf(decimal128)
            realmAnyList[12] = RealmAny.valueOf(uuid)
            realmAnyList[13] = RealmAny.nullValue()
            realmAnyList[14] = null
            realmAnyList[15] = RealmAny.valueOf(dynamicRealmObject)
        }

        val allJavaTypes = realm.where("RealmAnyListObject").findFirst()
        val realmAnyList = allJavaTypes!!.getList("aList", RealmAny::class.java)

        assertEquals(true, realmAnyList[0]!!.asBoolean())
        assertEquals(1, realmAnyList[1]!!.asByte())
        assertEquals(2, realmAnyList[2]!!.asShort())
        assertEquals(3, realmAnyList[3]!!.asInteger())
        assertEquals(4, realmAnyList[4]!!.asLong())
        assertEquals(5.toFloat(), realmAnyList[5]!!.asFloat())
        assertEquals(6.toDouble(), realmAnyList[6]!!.asDouble())
        assertEquals(aString, realmAnyList[7]!!.asString())
        assertTrue(Arrays.equals(byteArray, realmAnyList[8]!!.asBinary()))
        assertEquals(date, realmAnyList[9]!!.asDate())
        assertEquals(objectId, realmAnyList[10]!!.asObjectId())
        assertEquals(decimal128, realmAnyList[11]!!.asDecimal128())
        assertEquals(uuid, realmAnyList[12]!!.asUUID())
        assertTrue(realmAnyList[13]!!.isNull)
        assertTrue(realmAnyList[14]!!.isNull)
        assertEquals("dynamic", realmAnyList[15]!!.asRealmModel(DynamicRealmObject::class.java).getString("aString"))
    }

    @Test
    fun managed_listsRemoveAllTypes() {
        val aString = "a string"
        val byteArray = byteArrayOf(0, 1, 0)
        val date = Date()
        val objectId = ObjectId()
        val decimal128 = Decimal128(1)
        val uuid = UUID.randomUUID()

        realm.executeTransaction {
            val allJavaTypes = it.createObject("RealmAnyListObject")
            val dynamicRealmObject = it.createObject("ObjectString", "dynamic")

            val realmAnyList = allJavaTypes.getList("aList", RealmAny::class.java)

            realmAnyList.add(RealmAny.valueOf(true))
            realmAnyList.add(RealmAny.valueOf(1.toByte()))
            realmAnyList.add(RealmAny.valueOf(2.toShort()))
            realmAnyList.add(RealmAny.valueOf(3.toInt()))
            realmAnyList.add(RealmAny.valueOf(4.toLong()))
            realmAnyList.add(RealmAny.valueOf(5.toFloat()))
            realmAnyList.add(RealmAny.valueOf(6.toDouble()))
            realmAnyList.add(RealmAny.valueOf(aString))
            realmAnyList.add(RealmAny.valueOf(byteArray))
            realmAnyList.add(RealmAny.valueOf(date))
            realmAnyList.add(RealmAny.valueOf(objectId))
            realmAnyList.add(RealmAny.valueOf(decimal128))
            realmAnyList.add(RealmAny.valueOf(uuid))
            realmAnyList.add(RealmAny.nullValue())
            realmAnyList.add(null)
            realmAnyList.add(RealmAny.valueOf(dynamicRealmObject))
        }

        realm.executeTransaction {
            val allJavaTypes = realm.where("RealmAnyListObject").findFirst()
            val realmAnyList = allJavaTypes!!.getList("aList", RealmAny::class.java)

            for (i in 0..15)
                realmAnyList.removeAt(0)

            assertEquals(0, realmAnyList.size)
        }
    }
}
