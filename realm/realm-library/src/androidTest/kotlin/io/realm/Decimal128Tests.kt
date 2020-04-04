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
                .schema(Decimal128Required::class.java, Decimal128NotRequired::class.java).build()
        realm = Realm.getInstance(realmConfiguration)
    }

    @After
    fun tearDown() {
        realm.close()
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
        realm.close()
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

        realm.close()
    }
}