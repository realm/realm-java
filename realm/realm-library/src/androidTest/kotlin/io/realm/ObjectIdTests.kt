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
import org.junit.runner.RunWith
import kotlin.test.assertFailsWith

open class ObjectIdPrimaryKeyRequired
    : RealmObject() {
    @PrimaryKey
    @Required
    var id: ObjectId? = null
    var name: String = ""
    var anotherId: ObjectId? = null
}

open class ObjectIdPrimaryKeyNotRequired
    : RealmObject() {
    @PrimaryKey
    var id: ObjectId? = null
    var name: String = ""
}

open class ObjectIdAndString
    : RealmObject() {
    var id: ObjectId? = null
    var name: String = ""
}

open class ObjectIdRequiredRealmList
    : RealmObject() {
    var id: Long = 0

    @Required
    var ids: RealmList<ObjectId> = RealmList()
    var name: String = ""
}

open class ObjectIdOptionalRealmList
    : RealmObject() {
    var id: Long = 0

    var ids: RealmList<ObjectId> = RealmList()
    var name: String = ""
}

open class LinkedObjectId : RealmObject() {
    var linkedObjectId: ObjectIdPrimaryKeyNotRequired? = null
}

@RunWith(AndroidJUnit4::class)
class ObjectIdTests {
    private lateinit var realmConfiguration: RealmConfiguration
    private lateinit var realm: Realm

    @Rule
    @JvmField
    val configFactory = TestRealmConfigurationFactory()

    init {
        Realm.init(InstrumentationRegistry.getInstrumentation().targetContext)
    }

    @Before
    fun setUp() {
        realmConfiguration = configFactory
                .createConfigurationBuilder()
                .schema(ObjectIdPrimaryKeyRequired::class.java,
                        ObjectIdPrimaryKeyNotRequired::class.java,
                        ObjectIdAndString::class.java,
                        ObjectIdRequiredRealmList::class.java,
                        ObjectIdOptionalRealmList::class.java,
                        LinkedObjectId::class.java)
                .build()
        realm = Realm.getInstance(realmConfiguration)
    }

    @After
    fun tearDown() {
        realm.close()
    }

    @Test
    fun copyToRealm() {
        val objectIdHex1 = generateObjectIdHexString(1)
        val objectIdHex2 = generateObjectIdHexString(2)

        val value = ObjectIdPrimaryKeyRequired()
        value.id = ObjectId(objectIdHex1)
        value.anotherId = ObjectId(objectIdHex2)
        value.name = "Foo"

        realm.beginTransaction()
        val obj = realm.copyToRealm(value)
        realm.commitTransaction()

        assertEquals(ObjectId(objectIdHex1), obj.id)
        assertEquals(ObjectId(objectIdHex2), obj.anotherId)
        assertEquals("Foo", obj.name)
    }

    @Test
    fun copyFromRealm() {
        val objectIdHex1 = generateObjectIdHexString(1)
        val objectIdHex2 = generateObjectIdHexString(2)

        realm.beginTransaction()
        val value = realm.createObject<ObjectIdPrimaryKeyRequired>(ObjectId(objectIdHex1))
        value.anotherId = ObjectId(objectIdHex2)
        value.name = "Foo"
        realm.commitTransaction()

        val copy = realm.copyFromRealm(value)

        assertEquals(ObjectId(objectIdHex1), copy.id)
        assertEquals(ObjectId(objectIdHex2), copy.anotherId)
        assertEquals("Foo", copy.name)
    }

    @Test
    fun copyToRealmOrUpdate() {
        val objectIdHex1 = generateObjectIdHexString(1)
        val objectIdHex2 = generateObjectIdHexString(2)
        val objectIdHex3 = generateObjectIdHexString(3)

        realm.executeTransaction { realm ->
            val obj = realm.createObject<ObjectIdPrimaryKeyRequired>(ObjectId(objectIdHex1))
            obj.anotherId = ObjectId(objectIdHex2)
            obj.name = "Foo"
        }

        val value = ObjectIdPrimaryKeyRequired()
        value.id = ObjectId(objectIdHex1)
        value.anotherId = ObjectId(objectIdHex3)
        value.name = "Bar"

        realm.beginTransaction()
        val obj = realm.copyToRealmOrUpdate(value)
        realm.commitTransaction()

        assertEquals(ObjectId(objectIdHex1), obj.id)
        assertEquals(ObjectId(objectIdHex3), obj.anotherId)
        assertEquals("Bar", obj.name)
    }

    @Test
    fun insert() {
        val objectIdHex1 = generateObjectIdHexString(0)
        val objectIdHex2 = generateObjectIdHexString(7)

        realm.executeTransaction { realm ->
            val value = ObjectIdPrimaryKeyRequired()
            value.id = ObjectId(objectIdHex1)
            value.name = "Foo"
            value.anotherId = ObjectId(generateObjectIdHexString(7))

            realm.insert(value)
        }

        val obj = realm.where<ObjectIdPrimaryKeyRequired>().findFirst()

        assertNotNull(obj)
        assertEquals(ObjectId(objectIdHex1), obj!!.id)
        assertEquals(ObjectId(objectIdHex2), obj.anotherId)
        assertEquals("Foo", obj.name)
    }

    @Test
    fun insertOrUpdate() {
        val value = ObjectIdPrimaryKeyRequired()
        val objectIdHex1 = generateObjectIdHexString(0)
        val objectIdHex2 = generateObjectIdHexString(6)
        val objectIdHex3 = generateObjectIdHexString(7)

        realm.executeTransaction { realm ->
            realm.createObject<ObjectIdPrimaryKeyRequired>(ObjectId(objectIdHex1))
            value.name = "Foo"
            value.anotherId = ObjectId(objectIdHex2)
        }

        realm.executeTransaction { realm ->
            val value = ObjectIdPrimaryKeyRequired()
            value.id = ObjectId(objectIdHex1)
            value.name = "Bar"
            value.anotherId = ObjectId(objectIdHex3)

            realm.insertOrUpdate(value)
        }

        val all = realm.where<ObjectIdPrimaryKeyRequired>().findAll()

        assertEquals(1, all.size)
        assertEquals(ObjectId(objectIdHex1), all[0]!!.id)
        assertEquals(ObjectId(objectIdHex3), all[0]!!.anotherId)
        assertEquals("Bar", all[0]!!.name)
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

        assertFailsWith<RealmException> {
            realm.createObject<ObjectIdPrimaryKeyRequired>()
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
        assertFailsWith<RealmException> {
            realm.createObject<ObjectIdPrimaryKeyNotRequired>()
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

        assertFailsWith<IllegalArgumentException>("It should not be possible to add nullable elements to a required RealmList<ObjectId>") {
            obj.ids.add(null)
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
        realm.beginTransaction()
        val obj = realm.createObject<ObjectIdRequiredRealmList>()
        realm.cancelTransaction()

        assertFailsWith<UnsupportedOperationException> {
            obj.ids.where().equalTo("ids", ObjectId(generateObjectIdHexString(0))).findAll()
        }
    }

    @Test
    fun duplicatePK() {
        realm.beginTransaction()
        realm.createObject<ObjectIdPrimaryKeyRequired>(ObjectId(generateObjectIdHexString(0)))

        assertFailsWith<RealmPrimaryKeyConstraintException>("It should throw for duplicate PK usage") {
            realm.createObject<ObjectIdPrimaryKeyRequired>(ObjectId(generateObjectIdHexString(0)))
        }

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
    fun queriesCount() {
        realm.beginTransaction()
        realm.createObject<ObjectIdAndString>().id = ObjectId(generateObjectIdHexString(1))
        realm.createObject<ObjectIdAndString>().id = null
        realm.createObject<ObjectIdAndString>().id = ObjectId(generateObjectIdHexString(10))
        realm.createObject<ObjectIdAndString>().id = ObjectId(generateObjectIdHexString(0))
        realm.commitTransaction()

        assertEquals(4, realm.where<ObjectIdAndString>().count())
    }

    @Test

    fun queriesNotEqualTo() {
        realm.beginTransaction()
        realm.createObject<ObjectIdAndString>().id = ObjectId(generateObjectIdHexString(1))
        realm.createObject<ObjectIdAndString>().id = null
        realm.createObject<ObjectIdAndString>().id = ObjectId(generateObjectIdHexString(10))
        realm.createObject<ObjectIdAndString>().id = ObjectId(generateObjectIdHexString(0))
        realm.commitTransaction()

        val all = realm.where<ObjectIdAndString>()
                .notEqualTo("id", ObjectId(generateObjectIdHexString(1)))
                .sort("id", Sort.ASCENDING)
                .findAll()

        assertEquals(3, all.size)
        assertNull(all[0]!!.id)
        assertEquals(ObjectId(generateObjectIdHexString(0)), all[1]!!.id)
        assertEquals(ObjectId(generateObjectIdHexString(10)), all[2]!!.id)
    }

    @Test
    fun queriesGreaterThanOrEqualTo() {
        realm.beginTransaction()
        realm.createObject<ObjectIdAndString>().id = ObjectId(generateObjectIdHexString(1))
        realm.createObject<ObjectIdAndString>().id = null
        realm.createObject<ObjectIdAndString>().id = ObjectId(generateObjectIdHexString(10))
        realm.createObject<ObjectIdAndString>().id = ObjectId(generateObjectIdHexString(0))
        realm.commitTransaction()

        val all = realm.where<ObjectIdAndString>()
                .greaterThanOrEqualTo("id", ObjectId(generateObjectIdHexString(1)))
                .sort("id", Sort.ASCENDING)
                .findAll()

        assertEquals(2, all.size)
        assertEquals(ObjectId(generateObjectIdHexString(1)), all[0]!!.id)
        assertEquals(ObjectId(generateObjectIdHexString(10)), all[1]!!.id)
    }

    @Test
    fun queriesGreaterThan() {
        realm.beginTransaction()
        realm.createObject<ObjectIdAndString>().id = ObjectId(generateObjectIdHexString(1))
        realm.createObject<ObjectIdAndString>().id = null
        realm.createObject<ObjectIdAndString>().id = ObjectId(generateObjectIdHexString(10))
        realm.createObject<ObjectIdAndString>().id = ObjectId(generateObjectIdHexString(0))
        realm.commitTransaction()

        val all = realm.where<ObjectIdAndString>()
                .greaterThan("id", ObjectId(generateObjectIdHexString(1)))
                .sort("id", Sort.ASCENDING)
                .findAll()

        assertEquals(1, all.size)
        assertEquals(ObjectId(generateObjectIdHexString(10)), all[0]!!.id)
    }

    @Test
    fun queriesLessThanOrEqualTo() {
        realm.beginTransaction()
        realm.createObject<ObjectIdAndString>().id = ObjectId(generateObjectIdHexString(1))
        realm.createObject<ObjectIdAndString>().id = null
        realm.createObject<ObjectIdAndString>().id = ObjectId(generateObjectIdHexString(10))
        realm.createObject<ObjectIdAndString>().id = ObjectId(generateObjectIdHexString(0))
        realm.commitTransaction()

        val all = realm.where<ObjectIdAndString>()
                .lessThanOrEqualTo("id", ObjectId(generateObjectIdHexString(1)))
                .sort("id", Sort.ASCENDING)
                .findAll()

        assertEquals(2, all.size)
        assertEquals(ObjectId(generateObjectIdHexString(0)), all[0]!!.id)
        assertEquals(ObjectId(generateObjectIdHexString(1)), all[1]!!.id)
    }

    @Test
    fun queriesLessThan() {
        realm.beginTransaction()
        realm.createObject<ObjectIdAndString>().id = ObjectId(generateObjectIdHexString(1))
        realm.createObject<ObjectIdAndString>().id = null
        realm.createObject<ObjectIdAndString>().id = ObjectId(generateObjectIdHexString(10))
        realm.createObject<ObjectIdAndString>().id = ObjectId(generateObjectIdHexString(0))
        realm.commitTransaction()

        val all = realm.where<ObjectIdAndString>()
                .lessThan("id", ObjectId(generateObjectIdHexString(1)))
                .sort("id", Sort.ASCENDING)
                .findAll()

        assertEquals(1, all.size)
        assertEquals(ObjectId(generateObjectIdHexString(0)), all[0]!!.id)
    }

    @Test
    fun queriesIsNull() {
        realm.beginTransaction()
        realm.createObject<ObjectIdAndString>().id = ObjectId(generateObjectIdHexString(1))
        realm.createObject<ObjectIdAndString>().id = null
        realm.createObject<ObjectIdAndString>().id = ObjectId(generateObjectIdHexString(10))
        realm.createObject<ObjectIdAndString>().id = ObjectId(generateObjectIdHexString(0))
        realm.commitTransaction()

        val all = realm.where<ObjectIdAndString>()
                .isNull("id")
                .findAll()

        assertEquals(1, all.size)
        assertNull(all[0]!!.id)
    }

    @Test
    fun queriesIsNotNull() {
        realm.beginTransaction()
        realm.createObject<ObjectIdAndString>().id = ObjectId(generateObjectIdHexString(1))
        realm.createObject<ObjectIdAndString>().id = null
        realm.createObject<ObjectIdAndString>().id = ObjectId(generateObjectIdHexString(10))
        realm.createObject<ObjectIdAndString>().id = ObjectId(generateObjectIdHexString(0))
        realm.commitTransaction()

        val all = realm.where<ObjectIdAndString>()
                .isNotNull("id")
                .sort("id", Sort.ASCENDING)
                .findAll()

        assertEquals(3, all.size)
        assertEquals(ObjectId(generateObjectIdHexString(0)), all[0]!!.id)
        assertEquals(ObjectId(generateObjectIdHexString(1)), all[1]!!.id)
        assertEquals(ObjectId(generateObjectIdHexString(10)), all[2]!!.id)
    }

    @Test
    fun queriesAverage() {
        realm.beginTransaction()
        realm.createObject<ObjectIdAndString>().id = ObjectId(generateObjectIdHexString(1))
        realm.createObject<ObjectIdAndString>().id = null
        realm.createObject<ObjectIdAndString>().id = ObjectId(generateObjectIdHexString(10))
        realm.createObject<ObjectIdAndString>().id = ObjectId(generateObjectIdHexString(0))
        realm.commitTransaction()

        assertFailsWith<IllegalArgumentException>("Average is not supported for ObjectId") {
            realm.where<ObjectIdAndString>().average("id")
        }
    }

    @Test
    fun queriesIsEmpty() {
        realm.beginTransaction()
        realm.createObject<ObjectIdAndString>().id = ObjectId(generateObjectIdHexString(1))
        realm.createObject<ObjectIdAndString>().id = null
        realm.createObject<ObjectIdAndString>().id = ObjectId(generateObjectIdHexString(10))
        realm.createObject<ObjectIdAndString>().id = ObjectId(generateObjectIdHexString(0))
        realm.commitTransaction()

        assertFailsWith<IllegalArgumentException>("isEmpty is not supported for ObjectId") {
            realm.where<ObjectIdAndString>().isEmpty("id").count()
        }
    }

    @Test
    fun linkedQuery() {
        val objectIdArray = arrayOf(null, ObjectId(), ObjectId(), ObjectId())

        realm.executeTransaction { realm ->
            for (objectId in objectIdArray) {
                val objectIdObj = realm.createObject<ObjectIdPrimaryKeyNotRequired>(objectId)
                realm.createObject<LinkedObjectId>().linkedObjectId = objectIdObj
            }
        }

        for (objectId in objectIdArray) {
            val results = realm.where<LinkedObjectId>().equalTo("linkedObjectId.id", objectId).findAll()
            assertEquals(1, results.size)
            assertEquals(objectId, results.first()?.linkedObjectId?.id)
        }
    }

}
