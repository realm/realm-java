package io.realm

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.realm.rule.TestRealmConfigurationFactory
import org.bson.types.Decimal128
import org.bson.types.ObjectId
import org.junit.*
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue


// FIXME: MIXED PARAMETRIZED TESTS FOR INDEXED AND UNINDEXED
@RunWith(AndroidJUnit4::class)
class DynamicMixedTests {
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
        realm = DynamicRealm.getInstance(configFactory.createConfiguration("Mixed"))

        realm.executeTransaction {
            realm.schema
                    .create("MixedObject")
                    .addField("myMixed", Mixed::class.java)

            realm.schema
                    .create("MixedListObject")
                    .addRealmListField("aList", Mixed::class.java)

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

        val anObject = realm.createObject("MixedObject")
        anObject.setMixed("myMixed", Mixed.valueOf(Date(10)))

        realm.commitTransaction()

        val myMixed = anObject.getMixed("myMixed")

        assertEquals(Date(10), myMixed.asDate())
        assertEquals(Mixed.valueOf(Date(10)), myMixed)

        realm.close()
    }

    @Test
    fun defaultNullValue() {
        realm.beginTransaction()

        val anObject = realm.createObject("MixedObject")

        realm.commitTransaction()

        val myMixed = anObject.getMixed("myMixed")

        assertTrue(myMixed.isNull)
        assertTrue(myMixed.equals(null))
        assertEquals(Mixed.nullValue(), myMixed)
        assertEquals(MixedType.NULL, myMixed.type)
    }

    @Test
    fun setNullValue() {
        realm.beginTransaction()

        val anObject = realm.createObject("MixedObject")
        anObject.setMixed("myMixed", Mixed.nullValue())

        realm.commitTransaction()

        val myMixed = anObject.getMixed("myMixed")

        assertTrue(myMixed.isNull)
        assertEquals(MixedType.NULL, myMixed.type)
    }

    @Test
    fun writeRead_model() {
        realm.beginTransaction()

        val innerObject = realm.createObject("MixedObject")
        innerObject.setMixed("myMixed", Mixed.valueOf(Date(10)))

        val outerObject = realm.createObject("MixedObject")
        outerObject.setMixed("myMixed", Mixed.valueOf(innerObject))

        realm.commitTransaction()

        val innerMixed = innerObject.getMixed("myMixed")
        val outerMixed = outerObject.getMixed("myMixed")

        assertEquals(Date(10), innerMixed.asDate())
        assertEquals(DynamicRealmObject::class.java, outerMixed.valueClass)

        val aMixed = outerMixed
                .asRealmModel(DynamicRealmObject::class.java)
                .getMixed("myMixed")

        assertEquals(innerMixed.asDate(), aMixed.asDate())
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
            val allJavaTypes = it.createObject("MixedListObject")
            val mixedList = allJavaTypes.getList("aList", Mixed::class.java)

            val dynamicRealmObject = it.createObject("ObjectString", "dynamic")

            mixedList.add(Mixed.valueOf(true))
            mixedList.add(Mixed.valueOf(1.toByte()))
            mixedList.add(Mixed.valueOf(2.toShort()))
            mixedList.add(Mixed.valueOf(3.toInt()))
            mixedList.add(Mixed.valueOf(4.toLong()))
            mixedList.add(Mixed.valueOf(5.toFloat()))
            mixedList.add(Mixed.valueOf(6.toDouble()))
            mixedList.add(Mixed.valueOf(aString))
            mixedList.add(Mixed.valueOf(byteArray))
            mixedList.add(Mixed.valueOf(date))
            mixedList.add(Mixed.valueOf(objectId))
            mixedList.add(Mixed.valueOf(decimal128))
            mixedList.add(Mixed.valueOf(uuid))
            mixedList.add(Mixed.nullValue())
            mixedList.add(null)
            mixedList.add(Mixed.valueOf(dynamicRealmObject))
        }

        val allJavaTypes = realm.where("MixedListObject").findFirst()
        val mixedList = allJavaTypes!!.getList("aList", Mixed::class.java)

        assertEquals(true, mixedList[0]!!.asBoolean())
        assertEquals(1, mixedList[1]!!.asByte())
        assertEquals(2, mixedList[2]!!.asShort())
        assertEquals(3, mixedList[3]!!.asInteger())
        assertEquals(4, mixedList[4]!!.asLong())
        assertEquals(5.toFloat(), mixedList[5]!!.asFloat())
        assertEquals(6.toDouble(), mixedList[6]!!.asDouble())
        assertEquals(aString, mixedList[7]!!.asString())
        assertTrue(Arrays.equals(byteArray, mixedList[8]!!.asBinary()))
        assertEquals(date, mixedList[9]!!.asDate())
        assertEquals(objectId, mixedList[10]!!.asObjectId())
        assertEquals(decimal128, mixedList[11]!!.asDecimal128())
        assertEquals(uuid, mixedList[12]!!.asUUID())
        assertTrue(mixedList[13]!!.isNull)
        assertTrue(mixedList[14]!!.isNull)

        assertEquals("dynamic", mixedList[15]!!.asRealmModel(DynamicRealmObject::class.java).getString("aString"))

        realm.close()
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
            val allJavaTypes = it.createObject("MixedListObject")
            val mixedList = allJavaTypes.getList("aList", Mixed::class.java)
            val dynamicRealmObject = it.createObject("ObjectString", "dynamic")

            mixedList.add(0, Mixed.valueOf(true))
            mixedList.add(0, Mixed.valueOf(1.toByte()))
            mixedList.add(0, Mixed.valueOf(2.toShort()))
            mixedList.add(0, Mixed.valueOf(3.toInt()))
            mixedList.add(0, Mixed.valueOf(4.toLong()))
            mixedList.add(0, Mixed.valueOf(5.toFloat()))
            mixedList.add(0, Mixed.valueOf(6.toDouble()))
            mixedList.add(0, Mixed.valueOf(aString))
            mixedList.add(0, Mixed.valueOf(byteArray))
            mixedList.add(0, Mixed.valueOf(date))
            mixedList.add(0, Mixed.valueOf(objectId))
            mixedList.add(0, Mixed.valueOf(decimal128))
            mixedList.add(0, Mixed.valueOf(uuid))
            mixedList.add(0, Mixed.nullValue())
            mixedList.add(0, null)
            mixedList.add(0, Mixed.valueOf(dynamicRealmObject))
        }

        val allJavaTypes = realm.where("MixedListObject").findFirst()
        val mixedList = allJavaTypes!!.getList("aList", Mixed::class.java)

        assertEquals(true, mixedList[15]!!.asBoolean())
        assertEquals(1, mixedList[14]!!.asByte())
        assertEquals(2, mixedList[13]!!.asShort())
        assertEquals(3, mixedList[12]!!.asInteger())
        assertEquals(4, mixedList[11]!!.asLong())
        assertEquals(5.toFloat(), mixedList[10]!!.asFloat())
        assertEquals(6.toDouble(), mixedList[9]!!.asDouble())
        assertEquals(aString, mixedList[8]!!.asString())
        assertTrue(Arrays.equals(byteArray, mixedList[7]!!.asBinary()))
        assertEquals(date, mixedList[6]!!.asDate())
        assertEquals(objectId, mixedList[5]!!.asObjectId())
        assertEquals(decimal128, mixedList[4]!!.asDecimal128())
        assertEquals(uuid, mixedList[3]!!.asUUID())
        assertTrue(mixedList[2]!!.isNull)
        assertTrue(mixedList[1]!!.isNull)
        assertEquals("dynamic", mixedList[0]!!.asRealmModel(DynamicRealmObject::class.java).getString("aString"))


        realm.close()
    }

    @Test
    @Ignore("FIXME: See: https://github.com/realm/realm-core/issues/4304")
    fun managed_listsSetAllTypes() {
        val aString = "a string"
        val byteArray = byteArrayOf(0, 1, 0)
        val date = Date()
        val objectId = ObjectId()
        val decimal128 = Decimal128(1)
        val uuid = UUID.randomUUID()

        realm.executeTransaction {
            val allJavaTypes = it.createObject("MixedListObject")
            val dynamicRealmObject = it.createObject("ObjectString", "dynamic")

            val initialList = RealmList<Mixed>()
            initialList.addAll(arrayOfNulls(15))
            allJavaTypes.setList("aList", initialList)

            val mixedList = allJavaTypes.getList("aList", Mixed::class.java)

            mixedList[0] = Mixed.valueOf(true)
            mixedList[1] = Mixed.valueOf(1.toByte())
            mixedList[2] = Mixed.valueOf(2.toShort())
            mixedList[3] = Mixed.valueOf(3.toInt())
            mixedList[4] = Mixed.valueOf(4.toLong())
            mixedList[5] = Mixed.valueOf(5.toFloat())
            mixedList[6] = Mixed.valueOf(6.toDouble())
            mixedList[7] = Mixed.valueOf(aString)
            mixedList[8] = Mixed.valueOf(byteArray)
            mixedList[9] = Mixed.valueOf(date)
            mixedList[10] = Mixed.valueOf(objectId)
            mixedList[11] = Mixed.valueOf(decimal128)
            mixedList[12] = Mixed.valueOf(uuid)
            mixedList[13] = Mixed.nullValue()
            mixedList[14] = null
            mixedList.add(Mixed.valueOf(dynamicRealmObject))
        }

        val allJavaTypes = realm.where("MixedListObject").findFirst()
        val mixedList = allJavaTypes!!.getList("aList", Mixed::class.java)

        assertEquals(true, mixedList[0]!!.asBoolean())
        assertEquals(1, mixedList[1]!!.asByte())
        assertEquals(2, mixedList[2]!!.asShort())
        assertEquals(3, mixedList[3]!!.asInteger())
        assertEquals(4, mixedList[4]!!.asLong())
        assertEquals(5.toFloat(), mixedList[5]!!.asFloat())
        assertEquals(6.toDouble(), mixedList[6]!!.asDouble())
        assertEquals(aString, mixedList[7]!!.asString())
        assertTrue(Arrays.equals(byteArray, mixedList[8]!!.asBinary()))
        assertEquals(date, mixedList[9]!!.asDate())
        assertEquals(objectId, mixedList[10]!!.asObjectId())
        assertEquals(decimal128, mixedList[11]!!.asDecimal128())
        assertEquals(uuid, mixedList[12]!!.asUUID())
        assertTrue(mixedList[13]!!.isNull)
        assertTrue(mixedList[14]!!.isNull)
        assertEquals("dynamic", mixedList[15]!!.asRealmModel(DynamicRealmObject::class.java).getString("aString"))

        realm.close()
    }

    @Test
    @Ignore("FIXME: See: https://github.com/realm/realm-core/issues/4304")
    fun managed_listsRemoveAllTypes() {
        val aString = "a string"
        val byteArray = byteArrayOf(0, 1, 0)
        val date = Date()
        val objectId = ObjectId()
        val decimal128 = Decimal128(1)
        val uuid = UUID.randomUUID()

        realm.executeTransaction {
            val allJavaTypes = it.createObject("MixedListObject")
            val dynamicRealmObject = it.createObject("ObjectString", "dynamic")

            val initialList = RealmList<Mixed>()
            initialList.addAll(arrayOfNulls(15))
            allJavaTypes.setList("aList", initialList)

            val mixedList = allJavaTypes.getList("aList", Mixed::class.java)

            mixedList.add(Mixed.valueOf(true))
            mixedList.add(Mixed.valueOf(1.toByte()))
            mixedList.add(Mixed.valueOf(2.toShort()))
            mixedList.add(Mixed.valueOf(3.toInt()))
            mixedList.add(Mixed.valueOf(4.toLong()))
            mixedList.add(Mixed.valueOf(5.toFloat()))
            mixedList.add(Mixed.valueOf(6.toDouble()))
            mixedList.add(Mixed.valueOf(aString))
            mixedList.add(Mixed.valueOf(byteArray))
            mixedList.add(Mixed.valueOf(date))
            mixedList.add(Mixed.valueOf(objectId))
            mixedList.add(Mixed.valueOf(decimal128))
            mixedList.add(Mixed.valueOf(uuid))
            mixedList.add(Mixed.nullValue())
            mixedList.add(null)
            mixedList.add(Mixed.valueOf(dynamicRealmObject))
        }

        realm.executeTransaction {
            val allJavaTypes = realm.where("MixedListObject").findFirst()
            val mixedList = allJavaTypes!!.getList("aList", Mixed::class.java)

            for (i in 0..15)
                mixedList.removeAt(0)

            assertEquals(0, mixedList.size)
        }

        realm.close()
    }
}
