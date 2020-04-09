package io.realm

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.realm.TestHelper.generateObjectIdHexString
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required
import io.realm.exceptions.RealmException
import io.realm.exceptions.RealmPrimaryKeyConstraintException
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import org.bson.types.ObjectId
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith

open class ObjectIdPrimaryKeyRequired
 : RealmObject() {
    @field:PrimaryKey
    @field:Required
    var id : ObjectId? = null
    var name : String = ""

}

open class ObjectIdPrimaryKeyNotRequired
    : RealmObject() {
    @field:PrimaryKey
    var id : ObjectId? = null
    var name : String = ""

}

open class ObjectIdAndString
    : RealmObject() {
    var id : ObjectId? = null
    var name : String = ""

}

open class ObjectIdRequiredRealmList
    : RealmObject() {
    var id: Long = 0

    @field:Required
    var ids : RealmList<ObjectId> = RealmList()
    var name : String = ""
}

open class ObjectIdOptionalRealmList
    : RealmObject() {
    var id: Long = 0

    var ids : RealmList<ObjectId> = RealmList()
    var name : String = ""
}

@RunWith(AndroidJUnit4::class)
class ObjectIdTests {
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
                .schema(ObjectIdPrimaryKeyRequired::class.java,
                        ObjectIdPrimaryKeyNotRequired::class.java,
                        ObjectIdAndString::class.java,
                        ObjectIdRequiredRealmList::class.java,
                        ObjectIdOptionalRealmList::class.java)
                .build()
        realm = Realm.getInstance(realmConfiguration)
    }

    @After
    fun tearDown() {
        realm.close()
    }

    @Test
    fun copyToAndFromRealm() {
        val value = ObjectIdAndString()
        val objectIdHex = generateObjectIdHexString(0)
        value.id = ObjectId(objectIdHex)
        value.name = "Foo"

        // copyToRealm
        realm.beginTransaction()
        val obj = realm.copyToRealm(value)
        realm.commitTransaction()
        assertEquals(ObjectId(objectIdHex), obj.id)
        assertEquals("Foo", obj.name)

        // copyFromRealm
        realm.beginTransaction()
        val objectIdHex2 = generateObjectIdHexString(1)
        obj.id = ObjectId(objectIdHex2)
        obj.name = "Bar"
        realm.commitTransaction()

        val copy = realm.copyFromRealm(obj)
        assertEquals(ObjectId(objectIdHex2), copy.id)
        assertEquals("Bar", copy.name)
    }

    @Test
    fun frozen() {
        realm.beginTransaction()
        val hex = generateObjectIdHexString(7)
        val obj = realm.createObject<ObjectIdPrimaryKeyRequired>(ObjectId(hex))
        obj.name = "foo"
        realm.commitTransaction()

        val frozen = obj.freeze<ObjectIdPrimaryKeyRequired>()
        assertEquals(ObjectId(hex), frozen.id)
        assertEquals("foo", frozen.name)
    }


    @Test
    fun requiredPK() {
        realm.beginTransaction()
        try {
            realm.createObject<ObjectIdPrimaryKeyRequired>()
            fail()
        } catch (ignore: RealmException) {
        }

        val obj = realm.createObject<ObjectIdPrimaryKeyRequired>(ObjectId(generateObjectIdHexString(42)))
        obj.name = "foo"

        realm.commitTransaction()

        val result = realm.where<ObjectIdPrimaryKeyRequired>().equalTo("id", ObjectId(generateObjectIdHexString(42))).findFirst()
        assertNotNull(result)
        assertEquals("foo", result?.name)
    }

    @Test
    fun nullablePK() {
        try {
            realm.createObject<ObjectIdPrimaryKeyNotRequired>()
            fail()
        } catch (ignore: RealmException) {
        }

        realm.beginTransaction()
        val obj = realm.createObject<ObjectIdPrimaryKeyNotRequired>(null)
        obj.name = "foo"
        realm.commitTransaction()

        val result = realm.where<ObjectIdPrimaryKeyNotRequired>().equalTo("id", null as ObjectId?).findFirst()
        assertNotNull(result)
        assertEquals("foo", result!!.name)
    }


    @Test
    fun requiredRealmList() {
        realm.beginTransaction()
        val obj = realm.createObject<ObjectIdRequiredRealmList>()
        try {
            obj.ids.add(null)
            fail("It should not be possible to add nullable elements to a required RealmList<ObjectId>")
        } catch (expected: Exception) {
        }
    }

    @Test
    fun optionalRealmList() {
        realm.beginTransaction()
        val obj = realm.createObject<ObjectIdOptionalRealmList>()
        obj.ids.add(null)
        obj.ids.add(ObjectId(generateObjectIdHexString(0)))
        realm.commitTransaction()

        assertEquals(2, realm.where<ObjectIdOptionalRealmList>().findFirst()?.ids?.size)
    }

    @Test
    fun linkQueryNotSupported() {
        try {
            realm.where<ObjectIdRequiredRealmList>().greaterThan("ids", ObjectId(generateObjectIdHexString(0))).findAll()
            fail("It should not be possible to perform link query on ObjectId")
        } catch (expected: IllegalArgumentException) {}

        realm.beginTransaction()
        val obj = realm.createObject<ObjectIdRequiredRealmList>()
        realm.cancelTransaction()

        try {
            obj.ids.where().equalTo("ids", ObjectId(generateObjectIdHexString(0))).findAll()
        } catch (expected: UnsupportedOperationException) {}
    }

    @Test
    fun duplicatePK() {
        realm.beginTransaction()
        realm.createObject<ObjectIdPrimaryKeyRequired>(ObjectId(generateObjectIdHexString(0)))
        try {
            realm.createObject<ObjectIdPrimaryKeyRequired>(ObjectId(generateObjectIdHexString(0)))
            fail("It should throw for duplicate PK usage")
        } catch (expected: RealmPrimaryKeyConstraintException) {}

        realm.cancelTransaction()
    }

    @Test
    fun sort() {
        realm.beginTransaction()
        realm.createObject<ObjectIdAndString>().id = ObjectId(generateObjectIdHexString(10))
        realm.createObject<ObjectIdAndString>().id = ObjectId(generateObjectIdHexString(0))
        realm.createObject<ObjectIdAndString>().id = ObjectId(generateObjectIdHexString(1))
        realm.commitTransaction()

        var all = realm.where<ObjectIdAndString>().sort("id", Sort.ASCENDING).findAll()
        assertEquals(3, all.size)
        assertEquals(ObjectId(generateObjectIdHexString(0)), all[0]!!.id)
        assertEquals(ObjectId(generateObjectIdHexString(1)), all[1]!!.id)
        assertEquals(ObjectId(generateObjectIdHexString(10)), all[2]!!.id)

        all = realm.where<ObjectIdAndString>().sort("id", Sort.DESCENDING).findAll()
        assertEquals(3, all.size)
        assertEquals(ObjectId(generateObjectIdHexString(10)), all[0]!!.id)
        assertEquals(ObjectId(generateObjectIdHexString(1)), all[1]!!.id)
        assertEquals(ObjectId(generateObjectIdHexString(0)), all[2]!!.id)
    }

    @Test
    fun distinct() {
        realm.beginTransaction()
        realm.createObject<ObjectIdAndString>().id = ObjectId(generateObjectIdHexString(1))
        realm.createObject<ObjectIdAndString>().id = ObjectId(generateObjectIdHexString(1))
        realm.createObject<ObjectIdAndString>().id = null
        realm.createObject<ObjectIdAndString>().id = ObjectId(generateObjectIdHexString(0))
        realm.createObject<ObjectIdAndString>().id = ObjectId(generateObjectIdHexString(0))
        realm.createObject<ObjectIdAndString>().id = null
        realm.createObject<ObjectIdAndString>().id = ObjectId(generateObjectIdHexString(10))
        realm.createObject<ObjectIdAndString>().id = ObjectId(generateObjectIdHexString(10))
        realm.createObject<ObjectIdAndString>().id = null
        realm.commitTransaction()

        val all = realm.where<ObjectIdAndString>().distinct("id").sort("id", Sort.ASCENDING).findAll()
        assertEquals(4, all.size)
        assertNull(all[0]!!.id)
        assertEquals(ObjectId(generateObjectIdHexString(0)), all[1]!!.id)
        assertEquals(ObjectId(generateObjectIdHexString(1)), all[2]!!.id)
        assertEquals(ObjectId(generateObjectIdHexString(10)), all[3]!!.id)

    }

    @Test
    fun queries() {
        realm.beginTransaction()
        realm.createObject<ObjectIdAndString>().id = ObjectId(generateObjectIdHexString(1))
        realm.createObject<ObjectIdAndString>().id = null
        realm.createObject<ObjectIdAndString>().id = ObjectId(generateObjectIdHexString(10))
        realm.createObject<ObjectIdAndString>().id = ObjectId(generateObjectIdHexString(0))
        realm.commitTransaction()

        // count
        assertEquals(4, realm.where<ObjectIdAndString>().count())

        // notEqualTo
        var all = realm.where<ObjectIdAndString>()
                .notEqualTo("id", ObjectId(generateObjectIdHexString(1)))
                .sort("id", Sort.ASCENDING)
                .findAll()
        assertEquals(3, all.size)
        assertNull(all[0]!!.id)
        assertEquals(ObjectId(generateObjectIdHexString(0)), all[1]!!.id)
        assertEquals(ObjectId(generateObjectIdHexString(10)), all[2]!!.id)

        // greaterThanOrEqualTo
        all = realm.where<ObjectIdAndString>()
                .greaterThanOrEqualTo("id", ObjectId(generateObjectIdHexString(1)))
                .sort("id", Sort.ASCENDING)
                .findAll()
        assertEquals(2, all.size)
        assertEquals(ObjectId(generateObjectIdHexString(1)), all[0]!!.id)
        assertEquals(ObjectId(generateObjectIdHexString(10)), all[1]!!.id)

        // greaterThan
        all = realm.where<ObjectIdAndString>()
                .greaterThan("id", ObjectId(generateObjectIdHexString(1)))
                .sort("id", Sort.ASCENDING)
                .findAll()
        assertEquals(1, all.size)
        assertEquals(ObjectId(generateObjectIdHexString(10)), all[0]!!.id)


        // lessThanOrEqualTo
        all = realm.where<ObjectIdAndString>()
                .lessThanOrEqualTo("id", ObjectId(generateObjectIdHexString(1)))
                .sort("id", Sort.ASCENDING)
                .findAll()
        assertEquals(2, all.size)
        assertEquals(ObjectId(generateObjectIdHexString(0)), all[0]!!.id)
        assertEquals(ObjectId(generateObjectIdHexString(1)), all[1]!!.id)

        // lessThan
        all = realm.where<ObjectIdAndString>()
                .lessThan("id", ObjectId(generateObjectIdHexString(1)))
                .sort("id", Sort.ASCENDING)
                .findAll()
        assertEquals(1, all.size)
        assertEquals(ObjectId(generateObjectIdHexString(0)), all[0]!!.id)

        // isNull
        all = realm.where<ObjectIdAndString>()
                .isNull("id")
                .findAll()
        assertEquals(1, all.size)
        assertNull(all[0]!!.id)

        // isNotNull
        all = realm.where<ObjectIdAndString>()
                .isNotNull("id")
                .sort("id", Sort.ASCENDING)
                .findAll()
        assertEquals(3, all.size)
        assertEquals(ObjectId(generateObjectIdHexString(0)), all[0]!!.id)
        assertEquals(ObjectId(generateObjectIdHexString(1)), all[1]!!.id)
        assertEquals(ObjectId(generateObjectIdHexString(10)), all[2]!!.id)

        // average
        try {
            realm.where<ObjectIdAndString>().average("id") // FIXME should we support avergae queries in Core?
            fail("Average is not supported for ObjectId")
        } catch (expected: IllegalArgumentException) {}

        // isEmpty
        try {
            realm.where<ObjectIdAndString>().isEmpty("id")
            fail("isEmpty is not supported for ObjectId")
        } catch (expected: IllegalArgumentException) {}
    }

}