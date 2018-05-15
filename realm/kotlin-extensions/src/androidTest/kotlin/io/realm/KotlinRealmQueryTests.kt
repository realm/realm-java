package io.realm

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import io.realm.entities.AllPropTypesClass
import io.realm.kotlin.createObject
import io.realm.kotlin.oneOf
import io.realm.kotlin.where
import io.realm.rule.TestRealmConfigurationFactory
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
class KotlinRealmQueryTests {

    @Suppress("MemberVisibilityCanPrivate")
    @get:Rule
    val configFactory = TestRealmConfigurationFactory()

    private lateinit var realm: Realm

    @Before
    fun setUp() {
        Realm.init(InstrumentationRegistry.getTargetContext())
        realm = Realm.getInstance(configFactory.createConfiguration())
    }

    @After
    fun tearDown() {
        realm.close()
    }

    @Test
    fun oneOf_String() {
        realm.beginTransaction()
        val obj = realm.createObject<AllPropTypesClass>()
        obj.stringVar = "test"
        realm.commitTransaction()

        assertEquals(1,
                realm.where<AllPropTypesClass>()
                     .oneOf(AllPropTypesClass::nullableStringVar.name, arrayOf<String?>(null, "test"))
                     .count())

        assertEquals(1,
                realm.where<AllPropTypesClass>()
                        .oneOf(AllPropTypesClass::stringVar.name, arrayOf<String>("test"))
                        .count())
    }

    @Test
    fun oneOf_Byte() {
        realm.beginTransaction()
        val obj = realm.createObject<AllPropTypesClass>()
        obj.byteVar = 3
        realm.commitTransaction()

        assertEquals(1,
                realm.where<AllPropTypesClass>()
                        .oneOf(AllPropTypesClass::nullableByteVar.name, arrayOf<Byte?>(null, 3))
                        .count())

        assertEquals(1,
                realm.where<AllPropTypesClass>()
                        .oneOf(AllPropTypesClass::byteVar.name, arrayOf<Byte>(3))
                        .count())
    }

    @Test
    fun oneOf_Short() {
        realm.beginTransaction()
        val obj = realm.createObject<AllPropTypesClass>()
        obj.shortVar = 3
        realm.commitTransaction()

        assertEquals(1,
                realm.where<AllPropTypesClass>()
                        .oneOf(AllPropTypesClass::nullableShortVar.name, arrayOf<Short?>(null, 3))
                        .count())

        assertEquals(1,
                realm.where<AllPropTypesClass>()
                        .oneOf(AllPropTypesClass::shortVar.name, arrayOf<Short>(3))
                        .count())
    }

    @Test
    fun oneOf_Int() {
        realm.beginTransaction()
        val obj = realm.createObject<AllPropTypesClass>()
        obj.intVar = 3
        realm.commitTransaction()

        assertEquals(1,
                realm.where<AllPropTypesClass>()
                        .oneOf(AllPropTypesClass::nullableIntVar.name, arrayOf<Int?>(null, 3))
                        .count())

        assertEquals(1,
                realm.where<AllPropTypesClass>()
                        .oneOf(AllPropTypesClass::intVar.name, arrayOf<Int>(3))
                        .count())
    }

    @Test
    fun oneOf_Long() {
        realm.beginTransaction()
        val obj = realm.createObject<AllPropTypesClass>()
        obj.longVar = 3
        realm.commitTransaction()

        assertEquals(1,
                realm.where<AllPropTypesClass>()
                        .oneOf(AllPropTypesClass::nullableLongVar.name, arrayOf<Long?>(null, 3))
                        .count())

        assertEquals(1,
                realm.where<AllPropTypesClass>()
                        .oneOf(AllPropTypesClass::longVar.name, arrayOf<Long>(3))
                        .count())
    }

    @Test
    fun oneOf_Double() {
        realm.beginTransaction()
        val obj = realm.createObject<AllPropTypesClass>()
        obj.doubleVar = 3.5
        realm.commitTransaction()

        assertEquals(1,
                realm.where<AllPropTypesClass>()
                        .oneOf(AllPropTypesClass::nullableDoubleVar.name, arrayOf<Double?>(null, 3.5))
                        .count())

        assertEquals(1,
                realm.where<AllPropTypesClass>()
                        .oneOf(AllPropTypesClass::doubleVar.name, arrayOf<Double>(3.5))
                        .count())
    }

    @Test
    fun oneOf_Float() {
        realm.beginTransaction()
        val obj = realm.createObject<AllPropTypesClass>()
        obj.floatVar = 3.5f
        realm.commitTransaction()

        assertEquals(1,
                realm.where<AllPropTypesClass>()
                        .oneOf(AllPropTypesClass::nullableFloatVar.name, arrayOf<Float?>(null, 3.5f))
                        .count())

        assertEquals(1,
                realm.where<AllPropTypesClass>()
                        .oneOf(AllPropTypesClass::floatVar.name, arrayOf<Float>(3.5f))
                        .count())
    }

    @Test
    fun oneOf_Boolean() {
        realm.beginTransaction()
        val obj = realm.createObject<AllPropTypesClass>()
        obj.booleanVar = true
        realm.commitTransaction()

        assertEquals(1,
                realm.where<AllPropTypesClass>()
                        .oneOf(AllPropTypesClass::nullableBooleanVar.name, arrayOf<Boolean?>(null, true))
                        .count())

        assertEquals(1,
                realm.where<AllPropTypesClass>()
                        .oneOf(AllPropTypesClass::booleanVar.name, arrayOf<Boolean?>(true))
                        .count())
    }

    @Test
    fun oneOf_Date() {

        val testDate = Date()

        realm.beginTransaction()
        val obj = realm.createObject<AllPropTypesClass>()
        obj.dateVar = testDate
        realm.commitTransaction()

        assertEquals(1,
                realm.where<AllPropTypesClass>()
                        .oneOf(AllPropTypesClass::nullableDateVar.name, arrayOf<Date?>(null, testDate))
                        .count())

        assertEquals(1,
                realm.where<AllPropTypesClass>()
                        .oneOf(AllPropTypesClass::dateVar.name, arrayOf<Date>(testDate))
                        .count())
    }

}
