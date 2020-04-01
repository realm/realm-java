package io.realm

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.realm.TestHelper.generateObjectIdHexString
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required
import io.realm.exceptions.RealmException
import io.realm.kotlin.createObject
import org.bson.types.ObjectId
import org.junit.After
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

open class ObjectIdPrimaryKeyRequired
 : RealmObject() {
    @field:PrimaryKey
    @field:Required
    var id : ObjectId? = null
    var name : String = ""

}

@RunWith(AndroidJUnit4::class)
class ObjectIdTests {
    private var realmConfiguration: RealmConfiguration
    private lateinit var realm: Realm

    init {
        Realm.init(InstrumentationRegistry.getInstrumentation().targetContext)
        realmConfiguration = RealmConfiguration
                .Builder(InstrumentationRegistry.getInstrumentation().targetContext)
                .schema(ObjectIdPrimaryKeyRequired::class.java).build()
    }

    @Before
    fun setUp() {
        realm = Realm.getInstance(realmConfiguration)
    }

    @After
    fun tearDown() {
        realm.close()
    }

    @Test
    fun requiredPK() {
        realm.beginTransaction()
        try {
            realm.createObject<ObjectIdPrimaryKeyRequired>()
            fail()
        } catch (ignore: RealmException) {
        }

        var obj = realm.createObject<ObjectIdPrimaryKeyRequired>(ObjectId(generateObjectIdHexString(42)))
        obj.name = "foo"

        realm.commitTransaction()

        // FIXME re-enable when Core add query support
//        val result = realm.where<ObjectIdPrimaryKeyRequired>().equalTo("id", ObjectId(generateObjectIdHexString(42))).findFirst()
//        assertNotNull(result)
//        assertEquals("foo", result?.name)
    }
}