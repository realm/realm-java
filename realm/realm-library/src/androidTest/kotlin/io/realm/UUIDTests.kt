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
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required
import io.realm.exceptions.RealmException
import io.realm.exceptions.RealmPrimaryKeyConstraintException
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import java.util.*

open class UUIDPrimaryKeyRequired
    : RealmObject() {
    @field:PrimaryKey
    @field:Required
    var id: UUID? = null
    var name: String = ""
    var anotherId: UUID? = null
}

open class UUIDPrimaryKeyNotRequired
    : RealmObject() {
    @field:PrimaryKey
    var id: UUID? = null
    var name: String = ""

}

open class UUIDAndString
    : RealmObject() {
    var id: UUID? = null
    var name: String = ""
}

open class UUIDRequiredRealmList
    : RealmObject() {
    var id: Long = 0

    @field:Required
    var ids: RealmList<UUID> = RealmList()
    var name: String = ""
}

open class UUIDOptionalRealmList
    : RealmObject() {
    var id: Long = 0

    var ids: RealmList<UUID> = RealmList()
    var name: String = ""
}

@RunWith(AndroidJUnit4::class)
class UUIDTests {
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
                .schema(UUIDPrimaryKeyRequired::class.java,
                        UUIDPrimaryKeyNotRequired::class.java,
                        UUIDAndString::class.java,
                        UUIDRequiredRealmList::class.java,
                        UUIDOptionalRealmList::class.java)
                .build()
        realm = Realm.getInstance(realmConfiguration)
    }

    @After
    fun tearDown() {
        realm.close()
    }

    @Test
    fun copyToAndFromRealm() {
        val uuid1 = UUID.randomUUID()
        val uuid2 = UUID.randomUUID()
        val uuid3 = UUID.randomUUID()

        val value = UUIDPrimaryKeyRequired()
        value.id = uuid1
        value.anotherId = uuid2
        value.name = "Foo"

        // copyToRealm
        realm.beginTransaction()
        val obj = realm.copyToRealm(value)
        realm.commitTransaction()
        assertEquals(uuid1, obj.id)
        assertEquals(uuid2, obj.anotherId)
        assertEquals("Foo", obj.name)

        // copyToRealmOrUpdate
        value.name = "Bar"
        value.anotherId = uuid3
        realm.beginTransaction()
        realm.copyToRealmOrUpdate(value)
        realm.commitTransaction()

        // copyFromRealm
        val copy = realm.copyFromRealm(obj)
        assertEquals(uuid1, copy.id)
        assertEquals(uuid3, copy.anotherId)
        assertEquals("Bar", copy.name)
    }

    @Test
    fun insert() {
        val uuid1 = UUID.randomUUID()
        val uuid2 = UUID.randomUUID()
        val uuid3 = UUID.randomUUID()

        val value = UUIDPrimaryKeyRequired()
        value.id = uuid1
        value.name = "Foo"
        value.anotherId = uuid2

        // insert
        realm.beginTransaction()
        realm.insert(value)
        realm.commitTransaction()

        val obj = realm.where<UUIDPrimaryKeyRequired>().findFirst()
        assertNotNull(obj)
        assertEquals(uuid1, obj!!.id)
        assertEquals(uuid2, obj.anotherId)
        assertEquals("Foo", obj.name)

        // insertOrUpdate
        realm.beginTransaction()
        obj.anotherId = uuid3
        obj.name = "Bar"
        realm.insertOrUpdate(obj)
        realm.commitTransaction()

        val all = realm.where<UUIDPrimaryKeyRequired>().findAll()
        assertEquals(1, all.size)
        assertEquals(uuid1, all[0]!!.id)
        assertEquals(uuid3, all[0]!!.anotherId)
        assertEquals("Bar", all[0]!!.name)
    }

    @Test
    fun frozen() {
        val uuid1 = UUID.randomUUID()

        realm.beginTransaction()
        val obj = realm.createObject<UUIDPrimaryKeyRequired>(uuid1)
        obj.name = "foo"
        realm.commitTransaction()

        val frozen = obj.freeze<UUIDPrimaryKeyRequired>()
        assertEquals(uuid1, frozen.id)
        assertEquals("foo", frozen.name)
    }

    @Test
    fun requiredPK() {
        val uuid1 = UUID.randomUUID()

        realm.beginTransaction()
        try {
            realm.createObject<UUIDPrimaryKeyRequired>()
            fail()
        } catch (ignore: RealmException) {
        }

        val obj = realm.createObject<UUIDPrimaryKeyRequired>(uuid1)
        obj.name = "foo"

        realm.commitTransaction()

        val result = realm.where<UUIDPrimaryKeyRequired>().equalTo("id", uuid1).findFirst()
        assertNotNull(result)
        assertEquals("foo", result?.name)
    }

    @Test
    fun nullablePK() {
        try {
            realm.createObject<UUIDPrimaryKeyNotRequired>()
            fail()
        } catch (ignore: RealmException) {
        }

        realm.beginTransaction()
        val obj = realm.createObject<UUIDPrimaryKeyNotRequired>(null)
        obj.name = "foo"
        realm.commitTransaction()

        val result = realm.where<UUIDPrimaryKeyNotRequired>().equalTo("id", null as UUID?).findFirst()
        assertNotNull(result)
        assertEquals("foo", result!!.name)
    }


    @Test
    fun requiredRealmList() {
        realm.beginTransaction()
        val obj = realm.createObject<UUIDRequiredRealmList>()
        try {
            obj.ids.add(null)
            fail("It should not be possible to add nullable elements to a required RealmList<UUID>")
        } catch (expected: Exception) {
        }
    }

    @Test
    fun optionalRealmList() {
        val uuid1 = UUID.randomUUID()

        realm.beginTransaction()
        val obj = realm.createObject<UUIDOptionalRealmList>()
        obj.ids.add(null)
        obj.ids.add(uuid1)
        realm.commitTransaction()

        assertEquals(2, realm.where<UUIDOptionalRealmList>().findFirst()?.ids?.size)
    }

    @Test
    fun linkQueryNotSupported() {
        val uuid1 = UUID.randomUUID()

        try {
            realm.where<UUIDRequiredRealmList>().greaterThan("ids", uuid1).findAll()
            fail("It should not be possible to perform link query on UUID")
        } catch (expected: IllegalArgumentException) {
        }

        realm.beginTransaction()
        val obj = realm.createObject<UUIDRequiredRealmList>()
        realm.cancelTransaction()

        try {
            obj.ids.where().equalTo("ids", uuid1).findAll()
        } catch (expected: UnsupportedOperationException) {
        }
    }

    @Test
    fun duplicatePK() {
        val uuid1 = UUID.randomUUID()

        realm.beginTransaction()
        realm.createObject<UUIDPrimaryKeyRequired>(uuid1)
        try {
            realm.createObject<UUIDPrimaryKeyRequired>(uuid1)
            fail("It should throw for duplicate PK usage")
        } catch (expected: RealmPrimaryKeyConstraintException) {
        }

        realm.cancelTransaction()
    }

    @Test
    fun sort() {
        val uuid1 = UUID.fromString("017ba5ca-aa12-4afa-9219-e20cc3018599")
        val uuid2 = UUID.fromString("027ba5ca-aa12-4afa-9219-e20cc3018599")
        val uuid3 = UUID.fromString("037ba5ca-aa12-4afa-9219-e20cc3018599")

        realm.beginTransaction()
        realm.createObject<UUIDAndString>().id = uuid3
        realm.createObject<UUIDAndString>().id = uuid1
        realm.createObject<UUIDAndString>().id = uuid2
        realm.commitTransaction()

        var all = realm.where<UUIDAndString>().sort("id", Sort.ASCENDING).findAll()
        assertEquals(3, all.size)
        assertEquals(uuid1, all[0]!!.id)
        assertEquals(uuid2, all[1]!!.id)
        assertEquals(uuid3, all[2]!!.id)

        all = realm.where<UUIDAndString>().sort("id", Sort.DESCENDING).findAll()
        assertEquals(3, all.size)
        assertEquals(uuid3, all[0]!!.id)
        assertEquals(uuid2, all[1]!!.id)
        assertEquals(uuid1, all[2]!!.id)
    }

    @Test
    fun distinct() {
        val uuid1 = UUID.fromString("017ba5ca-aa12-4afa-9219-e20cc3018599")
        val uuid2 = UUID.fromString("027ba5ca-aa12-4afa-9219-e20cc3018599")
        val uuid3 = UUID.fromString("037ba5ca-aa12-4afa-9219-e20cc3018599")

        realm.beginTransaction()
        realm.createObject<UUIDAndString>().id = uuid2
        realm.createObject<UUIDAndString>().id = uuid2
        realm.createObject<UUIDAndString>().id = null
        realm.createObject<UUIDAndString>().id = uuid1
        realm.createObject<UUIDAndString>().id = uuid1
        realm.createObject<UUIDAndString>().id = null
        realm.createObject<UUIDAndString>().id = uuid3
        realm.createObject<UUIDAndString>().id = uuid3
        realm.createObject<UUIDAndString>().id = null
        realm.commitTransaction()

        val all = realm.where<UUIDAndString>().distinct("id").sort("id", Sort.ASCENDING).findAll()
        assertEquals(4, all.size)
        assertNull(all[0]!!.id)
        assertEquals(uuid1, all[1]!!.id)
        assertEquals(uuid2, all[2]!!.id)
        assertEquals(uuid3, all[3]!!.id)

    }

    @Test
    fun queries() {
        val uuid1 = UUID.fromString("017ba5ca-aa12-4afa-9219-e20cc3018599")
        val uuid2 = UUID.fromString("027ba5ca-aa12-4afa-9219-e20cc3018599")
        val uuid3 = UUID.fromString("037ba5ca-aa12-4afa-9219-e20cc3018599")

        realm.beginTransaction()
        realm.createObject<UUIDAndString>().id = uuid2
        realm.createObject<UUIDAndString>().id = null
        realm.createObject<UUIDAndString>().id = uuid3
        realm.createObject<UUIDAndString>().id = uuid1
        realm.commitTransaction()

        // count
        assertEquals(4, realm.where<UUIDAndString>().count())

        // notEqualTo
        var all = realm.where<UUIDAndString>()
                .notEqualTo("id", uuid2)
                .sort("id", Sort.ASCENDING)
                .findAll()

        assertEquals(3, all.size)
        assertNull(all[0]!!.id)
        assertEquals(uuid1, all[1]!!.id)
        assertEquals(uuid3, all[2]!!.id)

        // greaterThanOrEqualTo
        all = realm.where<UUIDAndString>()
                .greaterThanOrEqualTo("id", uuid2)
                .sort("id", Sort.ASCENDING)
                .findAll()
        assertEquals(2, all.size)
        assertEquals(uuid2, all[0]!!.id)
        assertEquals(uuid3, all[1]!!.id)

        // greaterThan
        all = realm.where<UUIDAndString>()
                .greaterThan("id", uuid2)
                .sort("id", Sort.ASCENDING)
                .findAll()
        assertEquals(1, all.size)
        assertEquals(uuid3, all[0]!!.id)


        // lessThanOrEqualTo
        all = realm.where<UUIDAndString>()
                .lessThanOrEqualTo("id", uuid2)
                .sort("id", Sort.ASCENDING)
                .findAll()
        assertEquals(2, all.size)
        assertEquals(uuid1, all[0]!!.id)
        assertEquals(uuid2, all[1]!!.id)

        // lessThan
        all = realm.where<UUIDAndString>()
                .lessThan("id", uuid2)
                .sort("id", Sort.ASCENDING)
                .findAll()
        assertEquals(1, all.size)
        assertEquals(uuid1, all[0]!!.id)

        // isNull
        all = realm.where<UUIDAndString>()
                .isNull("id")
                .findAll()
        assertEquals(1, all.size)
        assertNull(all[0]!!.id)

        // isNotNull
        all = realm.where<UUIDAndString>()
                .isNotNull("id")
                .sort("id", Sort.ASCENDING)
                .findAll()
        assertEquals(3, all.size)
        assertEquals(uuid1, all[0]!!.id)
        assertEquals(uuid2, all[1]!!.id)
        assertEquals(uuid3, all[2]!!.id)

        // average
        try {
            realm.where<UUIDAndString>().average("id") // FIXME should we support average queries in Core?
            fail("Average is not supported for UUID")
        } catch (expected: IllegalArgumentException) {
        }

        // isEmpty
        try {
            realm.where<UUIDAndString>().isEmpty("id")
            fail("isEmpty is not supported for UUID")
        } catch (expected: IllegalArgumentException) {
        }
    }

}
