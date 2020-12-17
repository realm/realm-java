package io.realm

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.realm.entities.MixedIndexed
import io.realm.entities.MixedNotIndexed
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
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue


@RunWith(AndroidJUnit4::class)
class MixedTests {
    private lateinit var realmConfiguration: RealmConfiguration
    private lateinit var realm: Realm

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
                        MixedIndexed::class.java)
                .build()

        realm = Realm.getInstance(realmConfiguration)
    }

    @After
    fun tearDown() {
        realm.close()
    }

    // Unmanaged
    @Test
    fun unmanaged_byteValue() {
        val mixed = Mixed.valueOf(10.toByte())

        assertFalse(mixed.isManaged)
        assertEquals(10, mixed.asByte())
        assertEquals(MixedType.INTEGER, mixed.type)
    }

    @Test
    fun unmanaged_shortValue() {
        val mixed = Mixed.valueOf(10.toShort())

        assertFalse(mixed.isManaged)
        assertEquals(10, mixed.asShort())
        assertEquals(MixedType.INTEGER, mixed.type)
    }

    @Test
    fun unmanaged_integerValue() {
        val mixed = Mixed.valueOf(10.toInt())

        assertFalse(mixed.isManaged)
        assertEquals(10, mixed.asInteger())
        assertEquals(MixedType.INTEGER, mixed.type)
    }

    @Test
    fun unmanaged_longValue() {
        val mixed = Mixed.valueOf(10.toLong())

        assertFalse(mixed.isManaged)
        assertEquals(10, mixed.asLong())
        assertEquals(MixedType.INTEGER, mixed.type)
    }

    @Test
    fun unmanaged_booleanValue() {
        val mixed = Mixed.valueOf(true)

        assertFalse(mixed.isManaged)
        assertEquals(true, mixed.asBoolean())
        assertEquals(MixedType.BOOLEAN, mixed.type)
    }

    @Test
    fun unmanaged_stringValue() {
        val mixed = Mixed.valueOf("hello world")

        assertFalse(mixed.isManaged)
        assertEquals("hello world", mixed.asString())
        assertEquals(MixedType.STRING, mixed.type)
    }

    @Test
    fun unmanaged_binaryValue() {
        val mixed = Mixed.valueOf(byteArrayOf(0, 1, 0))

        assertFalse(mixed.isManaged)
        assertTrue(Arrays.equals(byteArrayOf(0, 1, 0), mixed.asBinary()))
        assertEquals(MixedType.BINARY, mixed.type)
    }

    @Test
    fun unmanaged_dateValue() {
        val mixed = Mixed.valueOf(Date(10))

        assertFalse(mixed.isManaged)
        assertEquals(Date(10), mixed.asDate())
        assertEquals(MixedType.DATE, mixed.type)
    }

    @Test
    fun unmanaged_decimal128Value() {
        val mixed = Mixed.valueOf(Decimal128.fromIEEE754BIDEncoding(10, 10))

        assertFalse(mixed.isManaged)
        assertEquals(Decimal128.fromIEEE754BIDEncoding(10, 10), mixed.asDecimal128())
        assertEquals(MixedType.DECIMAL128, mixed.type)
    }

    @Test
    fun unmanaged_doubleValue() {
        val mixed = Mixed.valueOf(10.0)

        assertFalse(mixed.isManaged)
        assertEquals(10.0, mixed.asDouble())
        assertEquals(MixedType.DOUBLE, mixed.type)
    }

    @Test
    fun unmanaged_floatValue() {
        val mixed = Mixed.valueOf(10.0f)

        assertFalse(mixed.isManaged)
        assertEquals(10.0f, mixed.asFloat())
        assertEquals(MixedType.FLOAT, mixed.type)
    }

    @Test
    fun unmanaged_objectIdValue() {
        val mixed = Mixed.valueOf(ObjectId(TestHelper.generateObjectIdHexString(0)))

        assertFalse(mixed.isManaged)
        assertEquals(ObjectId(TestHelper.generateObjectIdHexString(0)), mixed.asObjectId())
        assertEquals(MixedType.OBJECT_ID, mixed.type)
    }

    @Test
    fun unmanaged_UUIDValue() {
        val mixed = Mixed.valueOf(UUID.fromString(TestHelper.generateUUIDString(0)))

        assertFalse(mixed.isManaged)
        assertEquals(UUID.fromString(TestHelper.generateUUIDString(0)), mixed.asUUID())
        assertEquals(MixedType.UUID, mixed.type)
    }

    @Test
    fun unmanaged_null() {
        val aLong: Long? = null

        val mixed = Mixed.valueOf(aLong)

        assertFalse(mixed.isManaged)
        assertTrue(mixed.isNull)
        assertEquals(MixedType.NULL, mixed.type)
    }

    // Managed
    @Test
    fun managed_longValue() {
        realm.executeTransaction {
            val mixedObject = it.createObject<MixedNotIndexed>()
            mixedObject.mixed = Mixed.valueOf(10L)
        }

        val mixedObject = realm.where<MixedNotIndexed>().findFirst()

        assertTrue(mixedObject!!.isManaged)
        assertEquals(10, mixedObject.mixed?.asInteger())
        assertEquals(MixedType.INTEGER, mixedObject.mixed?.type)
    }

    @Test
    fun managed_booleanValue() {
        realm.executeTransaction {
            val mixedObject = it.createObject<MixedNotIndexed>()
            mixedObject.mixed = Mixed.valueOf(true)
        }

        val mixedObject = realm.where<MixedNotIndexed>().findFirst()

        assertTrue(mixedObject!!.isManaged)
        assertEquals(true, mixedObject.mixed?.asBoolean())
        assertEquals(MixedType.BOOLEAN, mixedObject.mixed?.type)
    }

    @Test
    fun managed_stringValue() {
        realm.executeTransaction {
            val mixedObject = it.createObject<MixedNotIndexed>()
            mixedObject.mixed = Mixed.valueOf("hello world")
        }

        val mixedObject = realm.where<MixedNotIndexed>().findFirst()

        assertTrue(mixedObject!!.isManaged)
        assertEquals("hello world", mixedObject.mixed?.asString())
        assertEquals(MixedType.STRING, mixedObject.mixed?.type)
    }

    @Test
    fun managed_binaryValue() {
        realm.executeTransaction {
            val mixedObject = it.createObject<MixedNotIndexed>()
            mixedObject.mixed = Mixed.valueOf(byteArrayOf(0, 1, 0))
        }

        val mixedObject = realm.where<MixedNotIndexed>().findFirst()

        assertTrue(mixedObject!!.isManaged)
        assertTrue(Arrays.equals(byteArrayOf(0, 1, 0), mixedObject.mixed?.asBinary()))
        assertEquals(MixedType.BINARY, mixedObject.mixed?.type)
    }

    @Test
    fun managed_dateValue() {
        realm.executeTransaction {
            val mixedObject = it.createObject<MixedNotIndexed>()
            mixedObject.mixed = Mixed.valueOf(Date(10))
        }

        val mixedObject = realm.where<MixedNotIndexed>().findFirst()

        assertTrue(mixedObject!!.isManaged)
        assertEquals(Date(10), mixedObject.mixed?.asDate())
        assertEquals(MixedType.DATE, mixedObject.mixed!!.type)
    }

    @Test
    fun managed_decimal128Value() {
        realm.executeTransaction {
            val mixedObject = it.createObject<MixedNotIndexed>()
            mixedObject.mixed = Mixed.valueOf(Decimal128(10))
        }

        val mixedObject = realm.where<MixedNotIndexed>().findFirst()

        assertTrue(mixedObject!!.isManaged)
        assertEquals(Decimal128(10), mixedObject.mixed!!.asDecimal128())
        assertEquals(MixedType.DECIMAL128, mixedObject.mixed!!.type)
    }

    @Test
    fun managed_doubleValue() {
        realm.executeTransaction {
            val mixedObject = it.createObject<MixedNotIndexed>()
            mixedObject.mixed = Mixed.valueOf(10.0)
        }

        val mixedObject = realm.where<MixedNotIndexed>().findFirst()

        assertTrue(mixedObject!!.isManaged)
        assertEquals(10.0, mixedObject.mixed!!.asDouble())
        assertEquals(MixedType.DOUBLE, mixedObject.mixed!!.type)
    }

    @Test
    fun managed_floatValue() {
        realm.executeTransaction {
            val mixedObject = it.createObject<MixedNotIndexed>()
            mixedObject.mixed = Mixed.valueOf(10f)
        }

        val mixedObject = realm.where<MixedNotIndexed>().findFirst()

        assertTrue(mixedObject!!.isManaged)
        assertEquals(10f, mixedObject.mixed!!.asFloat())
        assertEquals(MixedType.FLOAT, mixedObject.mixed!!.type)
    }

    @Test
    fun managed_objectIdValue() {
        realm.executeTransaction {
            val mixedObject = it.createObject<MixedNotIndexed>()
            mixedObject.mixed = Mixed.valueOf(ObjectId(TestHelper.generateObjectIdHexString(0)))
        }

        val mixedObject = realm.where<MixedNotIndexed>().findFirst()

        assertTrue(mixedObject!!.isManaged)
        assertEquals(ObjectId(TestHelper.generateObjectIdHexString(0)), mixedObject.mixed!!.asObjectId())
        assertEquals(MixedType.OBJECT_ID, mixedObject.mixed!!.type)
    }

    @Test
    fun managed_UUIDvalue() {
        realm.executeTransaction {
            val mixedObject = it.createObject<MixedNotIndexed>()
            mixedObject.mixed = Mixed.valueOf(UUID.fromString(TestHelper.generateUUIDString(0)))
        }

        val mixedObject = realm.where<MixedNotIndexed>().findFirst()

        assertTrue(mixedObject!!.isManaged)
        assertEquals(UUID.fromString(TestHelper.generateUUIDString(0)), mixedObject.mixed!!.asUUID())
        assertEquals(MixedType.UUID, mixedObject.mixed!!.type)
    }

    @Test
    fun managed_null() {
        realm.executeTransaction {
            val mixedObject = it.createObject<MixedNotIndexed>()
            mixedObject.mixed = null
        }

        val mixedObject = realm.where<MixedNotIndexed>().findFirst()

        assertTrue(mixedObject!!.isManaged)
        assertTrue(mixedObject.mixed!!.isNull)
        assertEquals(MixedType.NULL, mixedObject.mixed!!.type)
    }

    @Test
    fun managed_nullMixed() {
        realm.executeTransaction {
            val mixedObject = it.createObject<MixedNotIndexed>()
            mixedObject.mixed = Mixed.nullValue()
        }

        val mixedObject = realm.where<MixedNotIndexed>().findFirst()

        assertTrue(mixedObject!!.isManaged)
        assertTrue(mixedObject.mixed!!.isNull)
        assertEquals(MixedType.NULL, mixedObject.mixed!!.type)
    }

    @Test
    fun managed_validity() {
        realm.executeTransaction {
            val mixedObject = it.createObject<MixedNotIndexed>()
            mixedObject.mixed = Mixed.nullValue()
        }

        val mixedObject = realm.where<MixedNotIndexed>().findFirst()

        assertTrue(mixedObject!!.isValid)

        realm.executeTransaction {
            mixedObject.deleteFromRealm()
        }

        assertFalse(mixedObject.isValid)
    }

    @Test
    fun managed_frozen() {
        realm.executeTransaction {
            val mixedObject = it.createObject<MixedNotIndexed>()
            mixedObject.mixed = Mixed.nullValue()
        }

        val mixedObject = realm.where<MixedNotIndexed>().findFirst()

        assertFalse(mixedObject!!.isFrozen)
        assertTrue(mixedObject.isValid)
        assertTrue(mixedObject.mixed!!.isNull)
        assertEquals(MixedType.NULL, mixedObject.mixed!!.type)
    }

    @Test
    fun managed_notFrozen() {
        realm.executeTransaction {
            val mixedObject = it.createObject<MixedNotIndexed>()
            mixedObject.mixed = Mixed.nullValue()
        }

        val mixedObjectFrozen = realm.freeze().where<MixedNotIndexed>().findFirst()
        
        assertTrue(mixedObjectFrozen!!.isFrozen)
        assertTrue(mixedObjectFrozen.isValid)
        assertTrue(mixedObjectFrozen.mixed!!.isNull)
        assertEquals(MixedType.NULL, mixedObjectFrozen.mixed!!.type)
    }
}