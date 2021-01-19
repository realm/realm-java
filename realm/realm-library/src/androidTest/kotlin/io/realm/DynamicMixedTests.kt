package io.realm

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.realm.rule.TestRealmConfigurationFactory
import org.bson.types.Decimal128
import org.bson.types.ObjectId
import org.junit.Rule
import org.junit.Test
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

    init {
        Realm.init(InstrumentationRegistry.getInstrumentation().targetContext)
    }

    @Test
    fun writeRead_primitive() {
        val realm = DynamicRealm.getInstance(configFactory.createConfiguration("Mixed"))

        realm.beginTransaction()

        realm.schema
                .create("MixedObject")
                .addField("myMixed", Mixed::class.java)

        val anObject = realm.createObject("MixedObject")
        anObject.setMixed("myMixed", Mixed.valueOf(Date(10)))

        realm.commitTransaction()

        val myMixed = anObject.getMixed("myMixed")

        assertEquals(Date(10), myMixed.asDate())

        realm.close()
    }

    @Test
    fun nullify() {
        val realm = DynamicRealm.getInstance(configFactory.createConfiguration("Mixed"))

        realm.beginTransaction()

        realm.schema
                .create("MixedObject")
                .addField("myMixed", Mixed::class.java)

        val anObject = realm.createObject("MixedObject")

        realm.commitTransaction()

        val myMixed = anObject.getMixed("myMixed")

        assertTrue(myMixed.isNull)
        assertEquals(MixedType.NULL, myMixed.type)

        realm.close()
    }

    @Test
    fun writeRead_model() {
        val realm = DynamicRealm.getInstance(configFactory.createConfiguration("Mixed"))

        realm.beginTransaction()

        realm.schema
                .create("MixedObject")
                .addField("myMixed", Mixed::class.java)

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

        realm.close()
    }

    @Test
    fun managed_listsAllTypes(){
        val aString = "a string"
        val byteArray = byteArrayOf(0, 1, 0)
        val date = Date()
        val objectId = ObjectId()
        val decimal128 = Decimal128(1)
        val uuid = UUID.randomUUID()

        val realm = DynamicRealm.getInstance(configFactory.createConfiguration("Mixed"))

        realm.executeTransaction {
            realm.schema
                    .create("MixedListObject")
                    .addRealmListField("aList", Mixed::class.java)

            val allJavaTypes = realm.createObject("MixedListObject")
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
        }

        val allJavaTypes= realm.where("MixedListObject").findFirst()
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

        realm.close()
    }
}