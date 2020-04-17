package io.realm

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import org.bson.types.Decimal128
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import java.math.BigDecimal

open class Decimal128Required
 : RealmObject() {
    @field:PrimaryKey
    var id: Long = 0

    @field:Required
    var decimal : Decimal128? = null

    var name : String = ""

}

open class Decimal128NotRequired
    : RealmObject() {
    @field:PrimaryKey
    var id: Long = 0

    var decimal : Decimal128? = null

    var name : String = ""
}

open class Decimal128RequiredRealmList
    : RealmObject() {
    var id: Long = 0

    @field:Required
    var decimals : RealmList<Decimal128> = RealmList()
    var name : String = ""
}

open class Decimal128OptionalRealmList
    : RealmObject() {
    var id: Long = 0

    var decimals : RealmList<Decimal128> = RealmList()
    var name : String = ""
}

@RunWith(AndroidJUnit4::class)
class Decimal128Tests {
    private lateinit var realmConfiguration: RealmConfiguration
    private lateinit var realm: Realm

    @Rule
    @JvmField val folder = TemporaryFolder()

    init {
        Realm.init(InstrumentationRegistry.getInstrumentation().targetContext)
    }

    @Before
    fun setUp() {
        realmConfiguration = RealmConfiguration
                .Builder(InstrumentationRegistry.getInstrumentation().targetContext)
                .directory(folder.newFolder())
                .schema(Decimal128Required::class.java,
                        Decimal128NotRequired::class.java,
                        Decimal128RequiredRealmList::class.java,
                        Decimal128OptionalRealmList::class.java)
                .build()
        realm = Realm.getInstance(realmConfiguration)
    }

    @After
    fun tearDown() {
        realm.close()
    }

    @Test
    fun copyToAndFromRealm() {
        val value = Decimal128NotRequired()
        value.decimal = Decimal128(BigDecimal.TEN)
        value.id = 42
        value.name = "Foo"

        // copyToRealm
        realm.beginTransaction()
        val obj = realm.copyToRealm(value)
        realm.commitTransaction()
        assertEquals(Decimal128(BigDecimal.TEN), obj.decimal)
        assertEquals(42L, obj.id)
        assertEquals("Foo", obj.name)

        // copyFromRealm
        realm.beginTransaction()
        obj.decimal = Decimal128(BigDecimal.ONE)
        obj.name = "Bar"
        realm.commitTransaction()

        val copy = realm.copyFromRealm(obj)
        assertEquals(Decimal128(BigDecimal.ONE), copy.decimal)
        assertEquals(42L, copy.id)
        assertEquals("Bar", copy.name)
    }

    @Test
    fun frozen() {
        realm.beginTransaction()
        val obj = realm.createObject<Decimal128Required>(42)
        obj.name = "foo"
        obj.decimal = (Decimal128(BigDecimal.TEN))
        realm.commitTransaction()

        val frozen = obj.freeze<Decimal128Required>()
        assertEquals(Decimal128(BigDecimal.TEN), frozen.decimal)
        assertEquals("foo", frozen.name)
        assertEquals(42L, frozen.id)
    }

    @Test
    fun requiredField() {
        realm.beginTransaction()
        val obj = realm.createObject<Decimal128Required>(42)
        obj.name = "foo"
        obj.decimal = (Decimal128(BigDecimal.TEN))
        realm.commitTransaction()

        val result = realm.where<Decimal128Required>().equalTo("decimal", Decimal128(BigDecimal.TEN)).findFirst()
        assertNotNull(result)
        assertEquals(42L, result?.id)
        assertEquals("foo", result?.name)

        realm.beginTransaction()
        try {
            result?.decimal = null
            fail("It should not be possible to set null value for the required decimal field")
        } catch (expected: Exception) {}
        realm.commitTransaction()
    }

    @Test
    fun nullableFiled() {
        realm.beginTransaction()
        val obj = realm.createObject<Decimal128NotRequired>(42)
        obj.name = "foo"
        obj.decimal = null
        realm.commitTransaction()

        val result = realm.where<Decimal128NotRequired>().isNull("decimal").findFirst()
        assertNotNull(result)
        assertEquals(42L, result?.id)
        assertEquals("foo", result?.name)

        realm.beginTransaction()
        result?.decimal = Decimal128(BigDecimal.TEN)
        realm.commitTransaction()

        val result2 = realm.where<Decimal128NotRequired>().equalTo("decimal", Decimal128(BigDecimal.TEN)).findFirst()
        assertEquals(42L, result2?.id)
        assertEquals("foo", result2?.name)
    }

    @Test
    fun requiredRealmList() {
        realm.beginTransaction()
        val obj = realm.createObject<Decimal128RequiredRealmList>()
        try {
            obj.decimals.add(null)
            fail("It should not be possible to add nullable elements to a required RealmList<Decimal128>")
        } catch (expected: Exception) {
        }
    }

    @Test
    fun optionalRealmList() {
        realm.beginTransaction()
        val obj = realm.createObject<Decimal128OptionalRealmList>()
        obj.decimals.add(null)
        obj.decimals.add(Decimal128(BigDecimal.ZERO))
        realm.commitTransaction()

        assertEquals(2, realm.where<Decimal128OptionalRealmList>().findFirst()?.decimals?.size)
    }

    @Test
    fun linkQueryNotSupported() {
        try {
            realm.where<Decimal128RequiredRealmList>().greaterThan("decimals", Decimal128(BigDecimal.ZERO)).findAll()
            fail("It should not be possible to perform link query on Decimal128")
        } catch (expected: IllegalArgumentException) {}

        realm.beginTransaction()
        val obj = realm.createObject<Decimal128RequiredRealmList>()
        realm.cancelTransaction()

        try {
            obj.decimals.where().equalTo("decimals", Decimal128(BigDecimal.ZERO)).findAll()
        } catch (expected: UnsupportedOperationException) {}
    }

    @Test
    fun NaN() {
        realm.beginTransaction()
        realm.createObject<Decimal128Required>(1).decimal = Decimal128(BigDecimal(Float.NaN.toLong()))
        realm.createObject<Decimal128Required>(2).decimal = Decimal128(Float.NaN.toLong())
        realm.createObject<Decimal128Required>(3).decimal = Decimal128(Double.NaN.toLong())
        realm.commitTransaction()

        val all = realm.where<Decimal128Required>().equalTo("decimal", Decimal128(Float.NaN.toLong())).findAll()
        assertEquals(3, all.size)
    }

    @Test
    fun minValue() {
        realm.beginTransaction()
        realm.createObject<Decimal128Required>(1).decimal = Decimal128(BigDecimal(Float.MIN_VALUE.toLong()))
        realm.createObject<Decimal128Required>(2).decimal = Decimal128(Float.MIN_VALUE.toLong())
        realm.createObject<Decimal128Required>(3).decimal = Decimal128(Double.MIN_VALUE.toLong())
        realm.commitTransaction()

        val all = realm.where<Decimal128Required>().equalTo("decimal", Decimal128(Float.MIN_VALUE.toLong())).findAll()
        assertEquals(3, all.size)
    }

    @Test
    fun minQuery() {
        realm.beginTransaction()
        realm.createObject<Decimal128Required>(1).decimal = Decimal128(BigDecimal.TEN)
        realm.createObject<Decimal128Required>(2).decimal = Decimal128(BigDecimal.ONE)
        realm.createObject<Decimal128Required>(3).decimal = Decimal128(BigDecimal.ZERO)
        realm.commitTransaction()

        val min: Number? = realm.where<Decimal128Required>().min("decimal")
        assertNotNull(min)
        assertTrue(min is Decimal128)
        assertEquals(Decimal128(BigDecimal.ZERO), min)
    }


    @Test
    fun maxValue() {
        realm.beginTransaction()
        realm.createObject<Decimal128Required>(1).decimal = Decimal128(BigDecimal(Float.MAX_VALUE.toLong()))
        realm.createObject<Decimal128Required>(2).decimal = Decimal128(Float.MAX_VALUE.toLong())
        realm.createObject<Decimal128Required>(3).decimal = Decimal128(Double.MAX_VALUE.toLong())
        realm.commitTransaction()

        val all = realm.where<Decimal128Required>().equalTo("decimal", Decimal128(Float.MAX_VALUE.toLong())).findAll()
        assertEquals(3, all.size)
    }

    @Test
    fun maxQuery() {
        realm.beginTransaction()
        realm.createObject<Decimal128Required>(1).decimal = Decimal128(BigDecimal.TEN)
        realm.createObject<Decimal128Required>(2).decimal = Decimal128(BigDecimal.ONE)
        realm.createObject<Decimal128Required>(3).decimal = Decimal128(BigDecimal.ZERO)
        realm.commitTransaction()

        val max: Number? = realm.where<Decimal128Required>().max("decimal")
        assertNotNull(max)
        assertTrue(max is Decimal128)
        assertEquals(Decimal128(BigDecimal.TEN), max)
    }

    @Test
    fun betweenQuery() {
        realm.beginTransaction()
        realm.createObject<Decimal128Required>(1).decimal = Decimal128(BigDecimal.TEN)
        realm.createObject<Decimal128Required>(2).decimal = Decimal128(BigDecimal.ONE)
        realm.createObject<Decimal128Required>(3).decimal = Decimal128(BigDecimal.ZERO)
        realm.commitTransaction()

        val between = realm.where<Decimal128Required>().between("decimal", Decimal128(-1L), Decimal128(11L)).findAll()
        assertEquals(3, between.size)
        assertEquals(Decimal128(BigDecimal.TEN), between[0]!!.decimal)
        assertEquals(Decimal128(BigDecimal.ONE), between[1]!!.decimal)
        assertEquals(Decimal128(BigDecimal.ZERO), between[2]!!.decimal)
    }

    @Test
    fun averageQuery() {
        var average = realm.where<Decimal128Required>().averageDecimal128("decimal")
        assertEquals(Decimal128(0), average)

        realm.beginTransaction()
        realm.createObject<Decimal128Required>(1).decimal = Decimal128(3)
        realm.createObject<Decimal128Required>(2).decimal = Decimal128(7)
        realm.createObject<Decimal128Required>(3).decimal = Decimal128(5)
        realm.commitTransaction()

        average = realm.where<Decimal128Required>().averageDecimal128("decimal")
        assertEquals(Decimal128(5), average)
    }

    @Test
    fun sort() {
        realm.beginTransaction()
        realm.createObject<Decimal128Required>(1).decimal = Decimal128(BigDecimal.ONE)
        realm.createObject<Decimal128Required>(2).decimal = Decimal128(BigDecimal.ZERO)
        realm.createObject<Decimal128Required>(3).decimal = Decimal128(BigDecimal.TEN)
        realm.commitTransaction()

        var all = realm.where<Decimal128Required>().sort("decimal", Sort.ASCENDING).findAll()
        assertEquals(3, all.size)
        assertEquals(Decimal128(BigDecimal.ZERO), all[0]!!.decimal)
        assertEquals(Decimal128(BigDecimal.ONE), all[1]!!.decimal)
        assertEquals(Decimal128(BigDecimal.TEN), all[2]!!.decimal)

        all = realm.where<Decimal128Required>().sort("decimal", Sort.DESCENDING).findAll()
        assertEquals(3, all.size)
        assertEquals(Decimal128(BigDecimal.TEN), all[0]!!.decimal)
        assertEquals(Decimal128(BigDecimal.ONE), all[1]!!.decimal)
        assertEquals(Decimal128(BigDecimal.ZERO), all[2]!!.decimal)
    }

    @Test
    fun distinct() {
        realm.beginTransaction()
        realm.createObject<Decimal128NotRequired>(1).decimal = Decimal128(BigDecimal.ONE)
        realm.createObject<Decimal128NotRequired>(2).decimal = Decimal128(BigDecimal.ONE)
        realm.createObject<Decimal128NotRequired>(3).decimal = null
        realm.createObject<Decimal128NotRequired>(4).decimal = Decimal128(BigDecimal.ZERO)
        realm.createObject<Decimal128NotRequired>(5).decimal = Decimal128(BigDecimal.ZERO)
        realm.createObject<Decimal128NotRequired>(6).decimal = null
        realm.createObject<Decimal128NotRequired>(7).decimal = Decimal128(BigDecimal.TEN)
        realm.createObject<Decimal128NotRequired>(8).decimal = Decimal128(BigDecimal.TEN)
        realm.createObject<Decimal128NotRequired>(9).decimal = null
        realm.commitTransaction()

        val all = realm.where<Decimal128NotRequired>().distinct("decimal").sort("decimal", Sort.ASCENDING).findAll()
        assertEquals(4, all.size)
        assertNull(all[0]!!.decimal)
        assertEquals(Decimal128(BigDecimal.ZERO), all[1]!!.decimal)
        assertEquals(Decimal128(BigDecimal.ONE), all[2]!!.decimal)
        assertEquals(Decimal128(BigDecimal.TEN), all[3]!!.decimal)

    }

    @Test
    fun queries() {
        realm.beginTransaction()
        realm.createObject<Decimal128NotRequired>(1).decimal = Decimal128(BigDecimal.ONE)
        realm.createObject<Decimal128NotRequired>(2).decimal = null
        realm.createObject<Decimal128NotRequired>(3).decimal = Decimal128(BigDecimal.TEN)
        realm.createObject<Decimal128NotRequired>(4).decimal = Decimal128(BigDecimal.ZERO)
        realm.commitTransaction()

        // count
        assertEquals(4, realm.where<Decimal128NotRequired>().count())

        // notEqualTo
        var all = realm.where<Decimal128NotRequired>()
                .notEqualTo("decimal", Decimal128(BigDecimal.ONE))
                .sort("decimal", Sort.ASCENDING)
                .findAll()
        assertEquals(3, all.size)
        assertNull(all[0]!!.decimal)
        assertEquals(Decimal128(BigDecimal.ZERO), all[1]!!.decimal)
        assertEquals(Decimal128(BigDecimal.TEN), all[2]!!.decimal)

        // greaterThanOrEqualTo
        all = realm.where<Decimal128NotRequired>()
                .greaterThanOrEqualTo("decimal", Decimal128(BigDecimal.ONE))
                .sort("decimal", Sort.ASCENDING)
                .findAll()
        assertEquals(2, all.size)
        assertEquals(Decimal128(BigDecimal.ONE), all[0]!!.decimal)
        assertEquals(Decimal128(BigDecimal.TEN), all[1]!!.decimal)

        // greaterThan
        all = realm.where<Decimal128NotRequired>()
                .greaterThan("decimal", Decimal128(BigDecimal.ONE))
                .sort("decimal", Sort.ASCENDING)
                .findAll()
        assertEquals(1, all.size)
        assertEquals(Decimal128(BigDecimal.TEN), all[0]!!.decimal)


        // lessThanOrEqualTo
        all = realm.where<Decimal128NotRequired>()
                .lessThanOrEqualTo("decimal", Decimal128(BigDecimal.ONE))
                .sort("decimal", Sort.ASCENDING)
                .findAll()
        assertEquals(2, all.size)
        assertEquals(Decimal128(BigDecimal.ZERO), all[0]!!.decimal)
        assertEquals(Decimal128(BigDecimal.ONE), all[1]!!.decimal)

        // lessThan
        all = realm.where<Decimal128NotRequired>()
                .lessThan("decimal", Decimal128(BigDecimal.ONE))
                .sort("decimal", Sort.ASCENDING)
                .findAll()
        assertEquals(1, all.size)
        assertEquals(Decimal128(BigDecimal.ZERO), all[0]!!.decimal)

        // isNull
        all = realm.where<Decimal128NotRequired>()
                .isNull("decimal")
                .findAll()
        assertEquals(1, all.size)
        assertNull(all[0]!!.decimal)
        assertEquals(2L, all[0]!!.id)

        // isNotNull
        all = realm.where<Decimal128NotRequired>()
                .isNotNull("decimal")
                .sort("decimal", Sort.ASCENDING)
                .findAll()
        assertEquals(3, all.size)
        assertEquals(Decimal128(BigDecimal.ZERO), all[0]!!.decimal)
        assertEquals(Decimal128(BigDecimal.ONE), all[1]!!.decimal)
        assertEquals(Decimal128(BigDecimal.TEN), all[2]!!.decimal)

        // average
        try {
            realm.where<Decimal128NotRequired>().average("decimal") // FIXME should we support avergae queries in Core?
            fail("Average is not supported for Decimal128")
        } catch (expected: IllegalArgumentException) {}

        // isEmpty
        try {
            realm.where<Decimal128NotRequired>().isEmpty("decimal")
            fail("isEmpty is not supported for Decimal128")
        } catch (expected: IllegalArgumentException) {}
    }

}
