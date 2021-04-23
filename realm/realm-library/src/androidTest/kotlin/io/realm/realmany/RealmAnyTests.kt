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
import io.realm.entities.embedded.SimpleEmbeddedObject
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import io.realm.rule.BlockingLooperThread
import org.bson.types.Decimal128
import org.bson.types.ObjectId
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*
import kotlin.test.*


@RunWith(AndroidJUnit4::class)
class RealmAnyTests {
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
                RealmAnyNotIndexedWithPK::class.java,
                SimpleEmbeddedObject::class.java,
                RealmAnyDefaultPK::class.java,
                RealmAnyDefaultNonPK::class.java,
                PrimaryKeyAsString::class.java)

        realm = Realm.getInstance(realmConfiguration)
    }

    @After
    fun tearDown() {
        realm.close()
    }

    // Unmanaged
    @Test
    fun unmanaged_byteValue() {
        val realmAny = RealmAny.valueOf(10.toByte())

        assertEquals(10, realmAny.asByte())
        assertEquals(RealmAny.valueOf(10.toByte()), realmAny)
        assertEquals(RealmAnyType.INTEGER, realmAny.type)
        assertEquals(RealmAnyType.INTEGER.typedClass, realmAny.valueClass)
    }

    @Test
    fun unmanaged_shortValue() {
        val realmAny = RealmAny.valueOf(10.toShort())

        assertEquals(10, realmAny.asShort())
        assertEquals(RealmAny.valueOf(10.toShort()), realmAny)
        assertEquals(RealmAnyType.INTEGER, realmAny.type)
        assertEquals(RealmAnyType.INTEGER.typedClass, realmAny.valueClass)
    }

    @Test
    fun unmanaged_integerValue() {
        val realmAny = RealmAny.valueOf(10.toInt())

        assertEquals(10, realmAny.asInteger())
        assertEquals(RealmAny.valueOf(10.toInt()), realmAny)
        assertEquals(RealmAnyType.INTEGER, realmAny.type)
        assertEquals(RealmAnyType.INTEGER.typedClass, realmAny.valueClass)
    }

    @Test
    fun unmanaged_longValue() {
        val realmAny = RealmAny.valueOf(10.toLong())

        assertEquals(10, realmAny.asLong())
        assertEquals(RealmAny.valueOf(10.toLong()), realmAny)
        assertEquals(RealmAnyType.INTEGER, realmAny.type)
        assertEquals(RealmAnyType.INTEGER.typedClass, realmAny.valueClass)
    }

    @Test
    fun unmanaged_booleanValue() {
        val realmAny = RealmAny.valueOf(true)

        assertEquals(true, realmAny.asBoolean())
        assertEquals(RealmAny.valueOf(true), realmAny)
        assertEquals(RealmAnyType.BOOLEAN, realmAny.type)
        assertEquals(RealmAnyType.BOOLEAN.typedClass, realmAny.valueClass)
    }

    @Test
    fun unmanaged_stringValue() {
        val realmAny = RealmAny.valueOf("hello world")

        assertEquals("hello world", realmAny.asString())
        assertEquals(RealmAny.valueOf("hello world"), realmAny)
        assertEquals(RealmAnyType.STRING, realmAny.type)
        assertEquals(RealmAnyType.STRING.typedClass, realmAny.valueClass)
    }

    @Test
    fun unmanaged_binaryValue() {
        val realmAny = RealmAny.valueOf(byteArrayOf(0, 1, 0))

        assertTrue(Arrays.equals(byteArrayOf(0, 1, 0), realmAny.asBinary()))
        assertEquals(RealmAny.valueOf(byteArrayOf(0, 1, 0)), realmAny)
        assertEquals(RealmAnyType.BINARY, realmAny.type)
        assertEquals(RealmAnyType.BINARY.typedClass, realmAny.valueClass)
    }

    @Test
    fun unmanaged_dateValue() {
        val realmAny = RealmAny.valueOf(Date(10))

        assertEquals(Date(10), realmAny.asDate())
        assertEquals(RealmAny.valueOf(Date(10)), realmAny)
        assertEquals(RealmAnyType.DATE, realmAny.type)
        assertEquals(RealmAnyType.DATE.typedClass, realmAny.valueClass)
    }

    @Test
    fun unmanaged_decimal128Value() {
        val realmAny = RealmAny.valueOf(Decimal128.fromIEEE754BIDEncoding(10, 10))

        assertEquals(Decimal128.fromIEEE754BIDEncoding(10, 10), realmAny.asDecimal128())
        assertEquals(RealmAny.valueOf(Decimal128.fromIEEE754BIDEncoding(10, 10)), realmAny)
        assertEquals(RealmAnyType.DECIMAL128, realmAny.type)
        assertEquals(RealmAnyType.DECIMAL128.typedClass, realmAny.valueClass)
    }

    @Test
    fun unmanaged_doubleValue() {
        val realmAny = RealmAny.valueOf(10.0)

        assertEquals(10.0, realmAny.asDouble())
        assertEquals(RealmAny.valueOf(10.0), realmAny)
        assertEquals(RealmAnyType.DOUBLE, realmAny.type)
        assertEquals(RealmAnyType.DOUBLE.typedClass, realmAny.valueClass)
    }

    @Test
    fun unmanaged_floatValue() {
        val realmAny = RealmAny.valueOf(10.0f)

        assertEquals(10.0f, realmAny.asFloat())
        assertEquals(RealmAny.valueOf(10.0f), realmAny)
        assertEquals(RealmAnyType.FLOAT, realmAny.type)
        assertEquals(RealmAnyType.FLOAT.typedClass, realmAny.valueClass)
    }

    @Test
    fun unmanaged_objectIdValue() {
        val realmAny = RealmAny.valueOf(ObjectId(TestHelper.generateObjectIdHexString(0)))

        assertEquals(ObjectId(TestHelper.generateObjectIdHexString(0)), realmAny.asObjectId())
        assertEquals(RealmAny.valueOf(ObjectId(TestHelper.generateObjectIdHexString(0))), realmAny)
        assertEquals(RealmAnyType.OBJECT_ID, realmAny.type)
        assertEquals(RealmAnyType.OBJECT_ID.typedClass, realmAny.valueClass)
    }

    @Test
    fun unmanaged_realmModel() {
        val obj = RealmAnyNotIndexed()
        val realmAny = RealmAny.valueOf(obj)

        assertEquals(obj, realmAny.asRealmModel(RealmAnyNotIndexed::class.java))
        assertEquals(RealmAny.valueOf(obj), realmAny)
        assertEquals(RealmAnyType.OBJECT, realmAny.type)
        assertEquals(RealmAnyNotIndexed::class.simpleName, realmAny.valueClass?.simpleName)
    }

    @Test
    fun unmanaged_UUIDValue() {
        val realmAny = RealmAny.valueOf(UUID.fromString(TestHelper.generateUUIDString(0)))

        assertEquals(UUID.fromString(TestHelper.generateUUIDString(0)), realmAny.asUUID())
        assertEquals(RealmAny.valueOf(UUID.fromString(TestHelper.generateUUIDString(0))), realmAny)
        assertEquals(RealmAnyType.UUID, realmAny.type)
    }

    @Test
    fun unmanaged_null() {
        val aLong: Boolean? = null

        val realmAny = RealmAny.valueOf(aLong)

        assertTrue(realmAny.isNull)
        assertNotNull(realmAny)
        assertEquals(RealmAny.nullValue(), realmAny)
        assertEquals(RealmAnyType.NULL, realmAny.type)
        assertEquals(null, realmAny.valueClass)
    }


    // Managed Tests
    @Test
    fun managed_byteValue() {
        realm.executeTransaction {
            val realmAnyObject = it.createObject<RealmAnyNotIndexed>()
            realmAnyObject.realmAny = RealmAny.valueOf(10.toByte())
        }

        val realmAnyObject = realm.where<RealmAnyNotIndexed>().findFirst()!!

        assertTrue(realmAnyObject.isManaged)
        assertEquals(10, realmAnyObject.realmAny!!.asByte())
        assertEquals(RealmAny.valueOf(10.toByte()), realmAnyObject.realmAny)
        assertEquals(RealmAnyType.INTEGER, realmAnyObject.realmAny!!.type)
    }

    @Test
    fun managed_shortValue() {
        realm.executeTransaction {
            val realmAnyObject = it.createObject<RealmAnyNotIndexed>()
            realmAnyObject.realmAny = RealmAny.valueOf(10.toShort())
        }

        val realmAnyObject = realm.where<RealmAnyNotIndexed>().findFirst()!!

        assertTrue(realmAnyObject.isManaged)
        assertEquals(10, realmAnyObject.realmAny!!.asShort())
        assertEquals(RealmAny.valueOf(10.toShort()), realmAnyObject.realmAny)
        assertEquals(RealmAnyType.INTEGER, realmAnyObject.realmAny!!.type)
    }

    @Test
    fun managed_integerValue() {
        realm.executeTransaction {
            val realmAnyObject = it.createObject<RealmAnyNotIndexed>()
            realmAnyObject.realmAny = RealmAny.valueOf(10.toInt())
        }

        val realmAnyObject = realm.where<RealmAnyNotIndexed>().findFirst()!!

        assertTrue(realmAnyObject.isManaged)
        assertEquals(10, realmAnyObject.realmAny!!.asInteger())
        assertEquals(RealmAny.valueOf(10.toInt()), realmAnyObject.realmAny)
        assertEquals(RealmAnyType.INTEGER, realmAnyObject.realmAny!!.type)
    }

    @Test
    fun managed_longValue() {
        realm.executeTransaction {
            val realmAnyObject = it.createObject<RealmAnyNotIndexed>()
            realmAnyObject.realmAny = RealmAny.valueOf(10.toLong())
        }

        val realmAnyObject = realm.where<RealmAnyNotIndexed>().findFirst()!!

        assertTrue(realmAnyObject.isManaged)
        assertEquals(10, realmAnyObject.realmAny!!.asLong())
        assertEquals(RealmAny.valueOf(10.toLong()), realmAnyObject.realmAny)
        assertEquals(RealmAnyType.INTEGER, realmAnyObject.realmAny!!.type)
        assertEquals(RealmAnyType.INTEGER.typedClass, realmAnyObject.realmAny!!.valueClass)
    }

    @Test
    fun managed_booleanValue() {
        realm.executeTransaction {
            val realmAnyObject = it.createObject<RealmAnyNotIndexed>()
            realmAnyObject.realmAny = RealmAny.valueOf(true)
        }

        val realmAnyObject = realm.where<RealmAnyNotIndexed>().findFirst()!!

        assertTrue(realmAnyObject.isManaged)
        assertEquals(true, realmAnyObject.realmAny!!.asBoolean())
        assertEquals(RealmAny.valueOf(true), realmAnyObject.realmAny)
        assertEquals(RealmAnyType.BOOLEAN, realmAnyObject.realmAny!!.type)
        assertEquals(RealmAnyType.BOOLEAN.typedClass, realmAnyObject.realmAny!!.valueClass)
    }

    @Test
    fun managed_stringValue() {
        realm.executeTransaction {
            val realmAnyObject = it.createObject<RealmAnyNotIndexed>()
            realmAnyObject.realmAny = RealmAny.valueOf("hello world")
        }

        val realmAnyObject = realm.where<RealmAnyNotIndexed>().findFirst()!!

        assertTrue(realmAnyObject.isManaged)
        assertEquals("hello world", realmAnyObject.realmAny!!.asString())
        assertEquals(RealmAny.valueOf("hello world"), realmAnyObject.realmAny)
        assertEquals(RealmAnyType.STRING, realmAnyObject.realmAny!!.type)
        assertEquals(RealmAnyType.STRING.typedClass, realmAnyObject.realmAny!!.valueClass)
    }

    @Test
    fun managed_binaryValue() {
        realm.executeTransaction {
            val realmAnyObject = it.createObject<RealmAnyNotIndexed>()
            realmAnyObject.realmAny = RealmAny.valueOf(byteArrayOf(0, 1, 0))
        }

        val realmAnyObject = realm.where<RealmAnyNotIndexed>().findFirst()!!

        assertTrue(realmAnyObject.isManaged)
        assertTrue(Arrays.equals(byteArrayOf(0, 1, 0), realmAnyObject.realmAny!!.asBinary()))
        assertEquals(RealmAny.valueOf(byteArrayOf(0, 1, 0)), realmAnyObject.realmAny!!)
        assertEquals(RealmAnyType.BINARY, realmAnyObject.realmAny!!.type)
        assertEquals(RealmAnyType.BINARY.typedClass, realmAnyObject.realmAny!!.valueClass)
    }

    @Test
    fun managed_dateValue() {
        realm.executeTransaction {
            val realmAnyObject = it.createObject<RealmAnyNotIndexed>()
            realmAnyObject.realmAny = RealmAny.valueOf(Date(10))
        }

        val realmAnyObject = realm.where<RealmAnyNotIndexed>().findFirst()!!

        assertTrue(realmAnyObject.isManaged)
        assertEquals(Date(10), realmAnyObject.realmAny!!.asDate())
        assertEquals(RealmAny.valueOf(Date(10)), realmAnyObject.realmAny)
        assertEquals(RealmAnyType.DATE, realmAnyObject.realmAny!!.type)
        assertEquals(RealmAnyType.DATE.typedClass, realmAnyObject.realmAny!!.valueClass)
    }

    @Test
    fun managed_decimal128Value() {
        realm.executeTransaction {
            val realmAnyObject = it.createObject<RealmAnyNotIndexed>()
            realmAnyObject.realmAny = RealmAny.valueOf(Decimal128(10))
        }

        val realmAnyObject = realm.where<RealmAnyNotIndexed>().findFirst()!!

        assertTrue(realmAnyObject.isManaged)
        assertEquals(Decimal128(10), realmAnyObject.realmAny!!.asDecimal128())
        assertEquals(RealmAny.valueOf(Decimal128(10)), realmAnyObject.realmAny)
        assertEquals(RealmAnyType.DECIMAL128, realmAnyObject.realmAny!!.type)
        assertEquals(RealmAnyType.DECIMAL128.typedClass, realmAnyObject.realmAny!!.valueClass)
    }

    @Test
    fun managed_doubleValue() {
        realm.executeTransaction {
            val realmAnyObject = it.createObject<RealmAnyNotIndexed>()
            realmAnyObject.realmAny = RealmAny.valueOf(10.0)
        }

        val realmAnyObject = realm.where<RealmAnyNotIndexed>().findFirst()!!

        assertTrue(realmAnyObject.isManaged)
        assertEquals(10.0, realmAnyObject.realmAny!!.asDouble())
        assertEquals(RealmAny.valueOf(10.0), realmAnyObject.realmAny)
        assertEquals(RealmAnyType.DOUBLE, realmAnyObject.realmAny!!.type)
        assertEquals(RealmAnyType.DOUBLE.typedClass, realmAnyObject.realmAny!!.valueClass)
    }

    @Test
    fun managed_floatValue() {
        realm.executeTransaction {
            val realmAnyObject = it.createObject<RealmAnyNotIndexed>()
            realmAnyObject.realmAny = RealmAny.valueOf(10f)
        }

        val realmAnyObject = realm.where<RealmAnyNotIndexed>().findFirst()!!

        assertTrue(realmAnyObject.isManaged)
        assertEquals(10f, realmAnyObject.realmAny!!.asFloat())
        assertEquals(RealmAny.valueOf(10f), realmAnyObject.realmAny)
        assertEquals(RealmAnyType.FLOAT, realmAnyObject.realmAny!!.type)
        assertEquals(RealmAnyType.FLOAT.typedClass, realmAnyObject.realmAny!!.valueClass)
    }

    @Test
    fun managed_objectIdValue() {
        realm.executeTransaction {
            val realmAnyObject = it.createObject<RealmAnyNotIndexed>()
            realmAnyObject.realmAny = RealmAny.valueOf(ObjectId(TestHelper.generateObjectIdHexString(0)))
        }

        val realmAnyObject = realm.where<RealmAnyNotIndexed>().findFirst()!!

        assertTrue(realmAnyObject.isManaged)
        assertEquals(ObjectId(TestHelper.generateObjectIdHexString(0)), realmAnyObject.realmAny!!.asObjectId())
        assertEquals(RealmAny.valueOf(ObjectId(TestHelper.generateObjectIdHexString(0))), realmAnyObject.realmAny)
        assertEquals(RealmAnyType.OBJECT_ID, realmAnyObject.realmAny!!.type)
        assertEquals(RealmAnyType.OBJECT_ID.typedClass, realmAnyObject.realmAny!!.valueClass)
    }

    @Test
    fun managed_UUIDValue() {
        realm.executeTransaction {
            val realmAnyObject = it.createObject<RealmAnyNotIndexed>()
            realmAnyObject.realmAny = RealmAny.valueOf(UUID.fromString(TestHelper.generateUUIDString(0)))
        }

        val realmAnyObject = realm.where<RealmAnyNotIndexed>().findFirst()!!

        assertTrue(realmAnyObject.isManaged)
        assertEquals(UUID.fromString(TestHelper.generateUUIDString(0)), realmAnyObject.realmAny!!.asUUID())
        assertEquals(RealmAny.valueOf(UUID.fromString(TestHelper.generateUUIDString(0))), realmAnyObject.realmAny)
        assertEquals(RealmAnyType.UUID, realmAnyObject.realmAny!!.type)
    }

    @Test
    fun managed_null() {
        realm.executeTransaction {
            val realmAnyObject = it.createObject<RealmAnyNotIndexed>()
            realmAnyObject.realmAny = null
        }

        val realmAnyObject = realm.where<RealmAnyNotIndexed>().findFirst()!!

        assertTrue(realmAnyObject.isManaged)
        assertNotNull(realmAnyObject.realmAny!!)
        assertTrue(realmAnyObject.realmAny!!.isNull)
        assertEquals(RealmAny.nullValue(), realmAnyObject.realmAny)
        assertEquals(RealmAnyType.NULL, realmAnyObject.realmAny!!.type)
        assertEquals(RealmAnyType.NULL.typedClass, realmAnyObject.realmAny!!.valueClass)
    }

    @Test
    fun managed_linkUnmanagedRealmModel() {
        val key = UUID.randomUUID().toString()

        realm.executeTransaction {
            val realmAnyObject = realm.createObject<RealmAnyNotIndexed>()
            val innerObject = PrimaryKeyAsString(key)

            realmAnyObject.realmAny = RealmAny.valueOf(innerObject)
        }

        val realmAnyObject = realm.where<RealmAnyNotIndexed>().findFirst()!!
        val realmAnyObject2 = realm.where<RealmAnyNotIndexed>().findFirst()!!

        val innerObject = realm.where<PrimaryKeyAsString>().findFirst()!!

        assertTrue(realmAnyObject.isManaged)

        assertEquals(
                realmAnyObject.realmAny!!.asRealmModel(PrimaryKeyAsString::class.java),
                realmAnyObject2.realmAny!!.asRealmModel(PrimaryKeyAsString::class.java)
        )

        assertEquals(innerObject, realmAnyObject.realmAny!!.asRealmModel(PrimaryKeyAsString::class.java))
        assertEquals(RealmAny.valueOf(innerObject), realmAnyObject.realmAny)
        assertEquals(RealmAnyType.OBJECT, realmAnyObject.realmAny!!.type)
        assertEquals(PrimaryKeyAsString::class.simpleName, realmAnyObject.realmAny!!.valueClass!!.simpleName)
    }

    @Test
    fun managed_linkManagedRealmModel() {
        val key = UUID.randomUUID().toString()

        realm.executeTransaction {
            val realmAnyObject = realm.createObject<RealmAnyNotIndexed>()
            val innerObject = realm.createObject<PrimaryKeyAsString>(key)

            realmAnyObject.realmAny = RealmAny.valueOf(innerObject)
        }

        val realmAnyObject = realm.where<RealmAnyNotIndexed>().findFirst()!!
        val realmAnyObject2 = realm.where<RealmAnyNotIndexed>().findFirst()!!

        val innerObject = realm.where<PrimaryKeyAsString>().findFirst()!!

        assertTrue(realmAnyObject.isManaged)

        assertEquals(
                realmAnyObject.realmAny!!.asRealmModel(PrimaryKeyAsString::class.java),
                realmAnyObject2.realmAny!!.asRealmModel(PrimaryKeyAsString::class.java)
        )

        assertEquals(innerObject, realmAnyObject.realmAny!!.asRealmModel(PrimaryKeyAsString::class.java))
        assertEquals(RealmAny.valueOf(innerObject), realmAnyObject.realmAny)
        assertEquals(RealmAnyType.OBJECT, realmAnyObject.realmAny!!.type)
        assertEquals(PrimaryKeyAsString::class.simpleName, realmAnyObject.realmAny!!.valueClass!!.simpleName)
    }

    @Test
    fun managed_nullRealmAny() {
        realm.executeTransaction {
            val realmAnyObject = it.createObject<RealmAnyNotIndexed>()
            realmAnyObject.realmAny = RealmAny.nullValue()
        }

        val realmAnyObject = realm.where<RealmAnyNotIndexed>().findFirst()!!

        assertTrue(realmAnyObject.isManaged)
        assertTrue(realmAnyObject.realmAny!!.isNull)
        assertNotNull(realmAnyObject.realmAny)
        assertEquals(RealmAny.nullValue(), realmAnyObject.realmAny)
        assertEquals(RealmAnyType.NULL, realmAnyObject.realmAny!!.type)
        assertEquals(RealmAnyType.NULL.typedClass, realmAnyObject.realmAny!!.valueClass)
    }

    @Test
    fun managed_validity() {
        realm.executeTransaction {
            val realmAnyObject = it.createObject<RealmAnyNotIndexed>()
            realmAnyObject.realmAny = RealmAny.nullValue()
        }

        val realmAnyObject = realm.where<RealmAnyNotIndexed>().findFirst()!!

        assertTrue(realmAnyObject!!.isValid)

        realm.executeTransaction {
            realmAnyObject.deleteFromRealm()
        }

        assertFalse(realmAnyObject.isValid)
    }

    @Test
    fun managed_frozen() {
        realm.executeTransaction {
            val realmAnyObject = it.createObject<RealmAnyNotIndexed>()
            realmAnyObject.realmAny = RealmAny.nullValue()
        }

        val realmAnyObject = realm.where<RealmAnyNotIndexed>().findFirst()!!

        assertFalse(realmAnyObject!!.isFrozen)
        assertTrue(realmAnyObject.isValid)
        assertTrue(realmAnyObject.realmAny!!.isNull)
        assertEquals(RealmAnyType.NULL, realmAnyObject.realmAny!!.type)
    }

    @Test
    fun managed_notFrozen() {
        realm.executeTransaction {
            val realmAnyObject = it.createObject<RealmAnyNotIndexed>()
            realmAnyObject.realmAny = RealmAny.nullValue()
        }

        val realmAnyObjectFrozen = realm.freeze().where<RealmAnyNotIndexed>().findFirst()!!

        assertTrue(realmAnyObjectFrozen!!.isFrozen)
        assertTrue(realmAnyObjectFrozen.isValid)
        assertTrue(realmAnyObjectFrozen.realmAny!!.isNull)
        assertEquals(RealmAnyType.NULL, realmAnyObjectFrozen.realmAny!!.type)
    }

    @Test
    fun managed_listsAddAllTypes() {
        val aString = "a string"
        val byteArray = byteArrayOf(0, 1, 0)
        val date = Date()
        val objectId = ObjectId()
        val decimal128 = Decimal128(1)
        val uuid = UUID.randomUUID()

        realm.executeTransaction {
            val allJavaTypes = it.createObject<AllJavaTypes>(0)
            val managedRealmModel = it.createObject<PrimaryKeyAsString>("managed")

            allJavaTypes.fieldRealmAnyList.add(RealmAny.valueOf(true))
            allJavaTypes.fieldRealmAnyList.add(RealmAny.valueOf(1.toByte()))
            allJavaTypes.fieldRealmAnyList.add(RealmAny.valueOf(2.toShort()))
            allJavaTypes.fieldRealmAnyList.add(RealmAny.valueOf(3.toInt()))
            allJavaTypes.fieldRealmAnyList.add(RealmAny.valueOf(4.toLong()))
            allJavaTypes.fieldRealmAnyList.add(RealmAny.valueOf(5.toFloat()))
            allJavaTypes.fieldRealmAnyList.add(RealmAny.valueOf(6.toDouble()))
            allJavaTypes.fieldRealmAnyList.add(RealmAny.valueOf(aString))
            allJavaTypes.fieldRealmAnyList.add(RealmAny.valueOf(byteArray))
            allJavaTypes.fieldRealmAnyList.add(RealmAny.valueOf(date))
            allJavaTypes.fieldRealmAnyList.add(RealmAny.valueOf(objectId))
            allJavaTypes.fieldRealmAnyList.add(RealmAny.valueOf(decimal128))
            allJavaTypes.fieldRealmAnyList.add(RealmAny.valueOf(uuid))
            allJavaTypes.fieldRealmAnyList.add(RealmAny.nullValue())
            allJavaTypes.fieldRealmAnyList.add(null)
            allJavaTypes.fieldRealmAnyList.add(RealmAny.valueOf(PrimaryKeyAsString("unmanaged")))
            allJavaTypes.fieldRealmAnyList.add(RealmAny.valueOf(managedRealmModel))
        }

        val allJavaTypes = realm.where<AllJavaTypes>().findFirst()

        assertEquals(true, allJavaTypes!!.fieldRealmAnyList[0]!!.asBoolean())
        assertEquals(1, allJavaTypes.fieldRealmAnyList[1]!!.asByte())
        assertEquals(2, allJavaTypes.fieldRealmAnyList[2]!!.asShort())
        assertEquals(3, allJavaTypes.fieldRealmAnyList[3]!!.asInteger())
        assertEquals(4, allJavaTypes.fieldRealmAnyList[4]!!.asLong())
        assertEquals(5.toFloat(), allJavaTypes.fieldRealmAnyList[5]!!.asFloat())
        assertEquals(6.toDouble(), allJavaTypes.fieldRealmAnyList[6]!!.asDouble())
        assertEquals(aString, allJavaTypes.fieldRealmAnyList[7]!!.asString())
        assertTrue(Arrays.equals(byteArray, allJavaTypes.fieldRealmAnyList[8]!!.asBinary()))
        assertEquals(date, allJavaTypes.fieldRealmAnyList[9]!!.asDate())
        assertEquals(objectId, allJavaTypes.fieldRealmAnyList[10]!!.asObjectId())
        assertEquals(decimal128, allJavaTypes.fieldRealmAnyList[11]!!.asDecimal128())
        assertEquals(uuid, allJavaTypes.fieldRealmAnyList[12]!!.asUUID())
        assertTrue(allJavaTypes.fieldRealmAnyList[13]!!.isNull)
        assertTrue(allJavaTypes.fieldRealmAnyList[14]!!.isNull)

        assertEquals("unmanaged", allJavaTypes.fieldRealmAnyList[15]!!.asRealmModel(PrimaryKeyAsString::class.java).name)
        assertEquals("managed", allJavaTypes.fieldRealmAnyList[16]!!.asRealmModel(PrimaryKeyAsString::class.java).name)
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
            val allJavaTypes = it.createObject<AllJavaTypes>(0)
            val managedRealmModel = it.createObject<PrimaryKeyAsString>("managed")

            allJavaTypes.fieldRealmAnyList.add(0, RealmAny.valueOf(true))
            allJavaTypes.fieldRealmAnyList.add(0, RealmAny.valueOf(1.toByte()))
            allJavaTypes.fieldRealmAnyList.add(0, RealmAny.valueOf(2.toShort()))
            allJavaTypes.fieldRealmAnyList.add(0, RealmAny.valueOf(3.toInt()))
            allJavaTypes.fieldRealmAnyList.add(0, RealmAny.valueOf(4.toLong()))
            allJavaTypes.fieldRealmAnyList.add(0, RealmAny.valueOf(5.toFloat()))
            allJavaTypes.fieldRealmAnyList.add(0, RealmAny.valueOf(6.toDouble()))
            allJavaTypes.fieldRealmAnyList.add(0, RealmAny.valueOf(aString))
            allJavaTypes.fieldRealmAnyList.add(0, RealmAny.valueOf(byteArray))
            allJavaTypes.fieldRealmAnyList.add(0, RealmAny.valueOf(date))
            allJavaTypes.fieldRealmAnyList.add(0, RealmAny.valueOf(objectId))
            allJavaTypes.fieldRealmAnyList.add(0, RealmAny.valueOf(decimal128))
            allJavaTypes.fieldRealmAnyList.add(0, RealmAny.valueOf(uuid))
            allJavaTypes.fieldRealmAnyList.add(0, RealmAny.nullValue())
            allJavaTypes.fieldRealmAnyList.add(0, null)
            allJavaTypes.fieldRealmAnyList.add(0, RealmAny.valueOf(PrimaryKeyAsString("unmanaged")))
            allJavaTypes.fieldRealmAnyList.add(0, RealmAny.valueOf(managedRealmModel))
        }

        val allJavaTypes = realm.where<AllJavaTypes>().findFirst()

        assertEquals(true, allJavaTypes!!.fieldRealmAnyList[16]!!.asBoolean())
        assertEquals(1, allJavaTypes.fieldRealmAnyList[15]!!.asByte())
        assertEquals(2, allJavaTypes.fieldRealmAnyList[14]!!.asShort())
        assertEquals(3, allJavaTypes.fieldRealmAnyList[13]!!.asInteger())
        assertEquals(4, allJavaTypes.fieldRealmAnyList[12]!!.asLong())
        assertEquals(5.toFloat(), allJavaTypes.fieldRealmAnyList[11]!!.asFloat())
        assertEquals(6.toDouble(), allJavaTypes.fieldRealmAnyList[10]!!.asDouble())
        assertEquals(aString, allJavaTypes.fieldRealmAnyList[9]!!.asString())
        assertTrue(Arrays.equals(byteArray, allJavaTypes.fieldRealmAnyList[8]!!.asBinary()))
        assertEquals(date, allJavaTypes.fieldRealmAnyList[7]!!.asDate())
        assertEquals(objectId, allJavaTypes.fieldRealmAnyList[6]!!.asObjectId())
        assertEquals(decimal128, allJavaTypes.fieldRealmAnyList[5]!!.asDecimal128())
        assertEquals(uuid, allJavaTypes.fieldRealmAnyList[4]!!.asUUID())
        assertTrue(allJavaTypes.fieldRealmAnyList[3]!!.isNull)
        assertTrue(allJavaTypes.fieldRealmAnyList[2]!!.isNull)

        assertEquals("unmanaged", allJavaTypes.fieldRealmAnyList[1]!!.asRealmModel(PrimaryKeyAsString::class.java).name)
        assertEquals("managed", allJavaTypes.fieldRealmAnyList[0]!!.asRealmModel(PrimaryKeyAsString::class.java).name)
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
            val allJavaTypes = it.createObject<AllJavaTypes>(0)
            val managedRealmModel = it.createObject<PrimaryKeyAsString>("managed")

            val initialList = RealmList<RealmAny>()
            initialList.addAll(arrayOfNulls(17))

            allJavaTypes.fieldRealmAnyList = initialList

            allJavaTypes.fieldRealmAnyList[0] = RealmAny.valueOf(true)
            allJavaTypes.fieldRealmAnyList[1] = RealmAny.valueOf(1.toByte())
            allJavaTypes.fieldRealmAnyList[2] = RealmAny.valueOf(2.toShort())
            allJavaTypes.fieldRealmAnyList[3] = RealmAny.valueOf(3.toInt())
            allJavaTypes.fieldRealmAnyList[4] = RealmAny.valueOf(4.toLong())
            allJavaTypes.fieldRealmAnyList[5] = RealmAny.valueOf(5.toFloat())
            allJavaTypes.fieldRealmAnyList[6] = RealmAny.valueOf(6.toDouble())
            allJavaTypes.fieldRealmAnyList[7] = RealmAny.valueOf(aString)
            allJavaTypes.fieldRealmAnyList[8] = RealmAny.valueOf(byteArray)
            allJavaTypes.fieldRealmAnyList[9] = RealmAny.valueOf(date)
            allJavaTypes.fieldRealmAnyList[10] = RealmAny.valueOf(objectId)
            allJavaTypes.fieldRealmAnyList[11] = RealmAny.valueOf(decimal128)
            allJavaTypes.fieldRealmAnyList[12] = RealmAny.valueOf(uuid)
            allJavaTypes.fieldRealmAnyList[13] = RealmAny.nullValue()
            allJavaTypes.fieldRealmAnyList[14] = null
            allJavaTypes.fieldRealmAnyList[15] = RealmAny.valueOf(PrimaryKeyAsString("unmanaged"))
            allJavaTypes.fieldRealmAnyList[16] = RealmAny.valueOf(managedRealmModel)
        }

        val allJavaTypes = realm.where<AllJavaTypes>().findFirst()

        assertEquals(true, allJavaTypes!!.fieldRealmAnyList[0]!!.asBoolean())
        assertEquals(1, allJavaTypes.fieldRealmAnyList[1]!!.asByte())
        assertEquals(2, allJavaTypes.fieldRealmAnyList[2]!!.asShort())
        assertEquals(3, allJavaTypes.fieldRealmAnyList[3]!!.asInteger())
        assertEquals(4, allJavaTypes.fieldRealmAnyList[4]!!.asLong())
        assertEquals(5.toFloat(), allJavaTypes.fieldRealmAnyList[5]!!.asFloat())
        assertEquals(6.toDouble(), allJavaTypes.fieldRealmAnyList[6]!!.asDouble())
        assertEquals(aString, allJavaTypes.fieldRealmAnyList[7]!!.asString())
        assertTrue(Arrays.equals(byteArray, allJavaTypes.fieldRealmAnyList[8]!!.asBinary()))
        assertEquals(date, allJavaTypes.fieldRealmAnyList[9]!!.asDate())
        assertEquals(objectId, allJavaTypes.fieldRealmAnyList[10]!!.asObjectId())
        assertEquals(decimal128, allJavaTypes.fieldRealmAnyList[11]!!.asDecimal128())
        assertEquals(uuid, allJavaTypes.fieldRealmAnyList[12]!!.asUUID())
        assertTrue(allJavaTypes.fieldRealmAnyList[13]!!.isNull)
        assertTrue(allJavaTypes.fieldRealmAnyList[14]!!.isNull)
        assertEquals("unmanaged", allJavaTypes.fieldRealmAnyList[15]!!.asRealmModel(PrimaryKeyAsString::class.java).name)
        assertEquals("managed", allJavaTypes.fieldRealmAnyList[16]!!.asRealmModel(PrimaryKeyAsString::class.java).name)
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
            val allJavaTypes = it.createObject<AllJavaTypes>(0)
            val managedRealmModel = it.createObject<PrimaryKeyAsString>("managed")

            allJavaTypes.fieldRealmAnyList.add(RealmAny.valueOf(true))
            allJavaTypes.fieldRealmAnyList.add(RealmAny.valueOf(1.toByte()))
            allJavaTypes.fieldRealmAnyList.add(RealmAny.valueOf(2.toShort()))
            allJavaTypes.fieldRealmAnyList.add(RealmAny.valueOf(3.toInt()))
            allJavaTypes.fieldRealmAnyList.add(RealmAny.valueOf(4.toLong()))
            allJavaTypes.fieldRealmAnyList.add(RealmAny.valueOf(5.toFloat()))
            allJavaTypes.fieldRealmAnyList.add(RealmAny.valueOf(6.toDouble()))
            allJavaTypes.fieldRealmAnyList.add(RealmAny.valueOf(aString))
            allJavaTypes.fieldRealmAnyList.add(RealmAny.valueOf(byteArray))
            allJavaTypes.fieldRealmAnyList.add(RealmAny.valueOf(date))
            allJavaTypes.fieldRealmAnyList.add(RealmAny.valueOf(objectId))
            allJavaTypes.fieldRealmAnyList.add(RealmAny.valueOf(decimal128))
            allJavaTypes.fieldRealmAnyList.add(RealmAny.valueOf(uuid))
            allJavaTypes.fieldRealmAnyList.add(RealmAny.nullValue())
            allJavaTypes.fieldRealmAnyList.add(null)
            allJavaTypes.fieldRealmAnyList.add(RealmAny.valueOf(PrimaryKeyAsString("unmanaged")))
            allJavaTypes.fieldRealmAnyList.add(RealmAny.valueOf(managedRealmModel))
        }

        realm.executeTransaction {
            val allJavaTypes = realm.where<AllJavaTypes>().findFirst()

            for (i in 0..16)
                allJavaTypes!!.fieldRealmAnyList.removeAt(0)

            assertEquals(0, allJavaTypes!!.fieldRealmAnyList.size)
        }
    }

    private val looperThread = BlockingLooperThread()

    @Test
    fun managed_listThrowsOtherRealm() {
        realm.beginTransaction()

        val aDog = realm.createObject(PrimaryKeyAsString::class.java, "a dog")

        realm.commitTransaction()


        looperThread.runBlocking {
            val anotherRealm = Realm.getInstance(realm.configuration)

            anotherRealm.beginTransaction()

            val allTypes = anotherRealm.createObject(AllJavaTypes::class.java, 0)

            assertFailsWith<IllegalArgumentException>("Cannot copy an object from another Realm instance.") {
                allTypes.fieldRealmAnyList.add(RealmAny.valueOf(aDog))
            }

            anotherRealm.commitTransaction()
            anotherRealm.close()

            looperThread.testComplete()
        }
    }

    @Test
    fun managed_listThrowsEmbedded() {
        looperThread.runBlocking {
            val anotherRealm = Realm.getInstance(realm.configuration)

            anotherRealm.beginTransaction()

            val allTypes = anotherRealm.createObject(AllJavaTypes::class.java, 0)

            assertFailsWith<IllegalArgumentException>("Embedded objects are not supported by RealmAny.") {
                allTypes.fieldRealmAnyList.add(RealmAny.valueOf(SimpleEmbeddedObject()))
            }

            anotherRealm.commitTransaction()
            anotherRealm.close()

            looperThread.testComplete()
        }
    }

    @Test
    fun dynamiclists_throwCopyBetweenInstances() {
        realm.beginTransaction()

        val aDog = realm.createObject(PrimaryKeyAsString::class.java, "a dog")

        realm.commitTransaction()

        val dynDog = DynamicRealmObject(aDog)
        val dynamicRealm = DynamicRealm.getInstance(realm.getConfiguration())

        dynamicRealm.beginTransaction()

        assertFailsWith<IllegalArgumentException>("Cannot copy DynamicRealmObject between Realm instances.") {
            dynamicRealm.createObject(AllJavaTypes.CLASS_NAME, 0)
                    .getList(AllJavaTypes.FIELD_REALM_ANY_LIST, RealmAny::class.java)
                    .add(RealmAny.valueOf(dynDog))
        }

        dynamicRealm.commitTransaction()

        dynamicRealm.close()
    }

    @Test
    fun lists_throwCopyBetweenThreads() {
        realm.executeTransaction {
            it.createObject(PrimaryKeyAsString::class.java, "a dog")
        }

        val dynamicRealm = DynamicRealm.getInstance(realm.getConfiguration())
        val dynDog = dynamicRealm.where(PrimaryKeyAsString.CLASS_NAME).findFirst()

        looperThread.runBlocking {
            val innerDynamicRealm = DynamicRealm.getInstance(realm.getConfiguration())
            innerDynamicRealm.beginTransaction()

            assertFailsWith<IllegalStateException>("Cannot copy an object to a Realm instance created in another thread.") {
                dynamicRealm.createObject(AllJavaTypes.CLASS_NAME, 0)
                        .getList(AllJavaTypes.FIELD_REALM_ANY_LIST, RealmAny::class.java)
                        .add(RealmAny.valueOf(dynDog))
            }

            innerDynamicRealm.close()

            looperThread.testComplete()
        }

        dynamicRealm.close()
    }


    @Test
    fun freeze() {
        realm.beginTransaction()
        val obj = realm.createObject<RealmAnyNotIndexedWithPK>(0)
        obj.realmAny = RealmAny.valueOf(10.toInt())
        realm.commitTransaction()

        val frozen = obj.freeze<RealmAnyNotIndexedWithPK>()

        assertEquals(RealmAny.valueOf(10.toInt()), frozen.realmAny)
    }

    @Test
    fun initialize_default_pkRealmModel() {
        realm.executeTransaction {
            realm.createObject<PrimaryKeyAsString>(RealmAnyDefaultPK.NAME)
        }

        realm.executeTransaction {
            realm.createObject<RealmAnyDefaultPK>()
        }

        val realmAnyObject = realm.where<RealmAnyDefaultPK>().findFirst()!!

        assertTrue(realmAnyObject.isManaged)
        assertEquals(RealmAnyDefaultPK.NAME, realmAnyObject.realmAny!!.asRealmModel(PrimaryKeyAsString::class.java).name)
        assertEquals(RealmAnyType.OBJECT, realmAnyObject.realmAny!!.type)
        assertEquals(PrimaryKeyAsString::class.simpleName, realmAnyObject.realmAny!!.valueClass?.simpleName)
    }

    @Test
    fun initialize_default_nonPkRealmModel() {
        realm.executeTransaction {
            realm.createObject<RealmAnyNotIndexed>()
        }

        realm.executeTransaction {
            realm.createObject<RealmAnyDefaultNonPK>()
        }

        val realmAnyObject = realm.where<RealmAnyDefaultNonPK>().findFirst()!!

        assertTrue(realmAnyObject.isManaged)
        assertEquals(RealmAnyType.OBJECT, realmAnyObject.realmAny!!.type)
        assertEquals(RealmAnyNotIndexed::class.simpleName, realmAnyObject.realmAny!!.valueClass?.simpleName)
    }

    @Test
    fun missing_schemaClass() {
        realm.close()

        val missingClassName = "MissingClass"
        val fieldName = "aString"
        val expectedValue = "Hello world"

        DynamicRealm.getInstance(realmConfiguration).use { dynamicRealm ->
            dynamicRealm.executeTransaction { transactionRealm ->
                transactionRealm.schema
                        .create(missingClassName)
                        .addField(fieldName, String::class.java)

                val missingClassObject = transactionRealm.createObject(missingClassName).apply {
                    set(fieldName, expectedValue)
                }
                transactionRealm.createObject(RealmAnyNotIndexed.CLASS_NAME).apply {
                    set(RealmAnyNotIndexed.FIELD_REALM_ANY, RealmAny.valueOf(missingClassObject))
                }
            }
        }

        realm = Realm.getInstance(realmConfiguration)

        val realmAnyNotIndexed = realm.where(RealmAnyNotIndexed::class.java).findFirst()!!
        assertEquals(RealmAnyType.OBJECT, realmAnyNotIndexed.realmAny!!.type)
        assertEquals(DynamicRealmObject::class.java, realmAnyNotIndexed.realmAny!!.valueClass)

        val innerObject = realmAnyNotIndexed.realmAny!!.asRealmModel(DynamicRealmObject::class.java)
        assertEquals(expectedValue, innerObject.getString(fieldName))
    }
}
