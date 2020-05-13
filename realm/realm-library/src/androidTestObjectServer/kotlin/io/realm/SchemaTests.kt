/*
 * Copyright 2016 Realm Inc.
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
import io.realm.SyncTestUtils.Companion.createTestUser
import io.realm.entities.StringOnly
import junit.framework.Assert
import junit.framework.TestCase
import org.junit.*
import org.junit.runner.RunWith

@Ignore("FIXME: RealmApp refactor")
@RunWith(AndroidJUnit4::class)
class SchemaTests {
    @Rule
    val configFactory = TestSyncConfigurationFactory()
    private var config: SyncConfiguration? = null
    private var app: TestRealmApp? = null

    @Before
    fun setUp() {
        app = TestRealmApp()
        val user = createTestUser(app!!)
        config = configFactory.createSyncConfigurationBuilder(user).build()
    }

    @After
    fun tearDown() {
        if (app != null) {
            app!!.close()
        }
    }

    @get:Test
    val instance: Unit
        get() {
            val realm = Realm.getInstance(config!!)
            TestCase.assertFalse(realm.isClosed)
            realm.close()
            Assert.assertTrue(realm.isClosed)
        }

    @Test
    fun createObject() {
        val realm = Realm.getInstance(config!!)
        realm.beginTransaction()
        Assert.assertTrue(realm.schema.contains("StringOnly"))
        val stringOnly = realm.createObject(StringOnly::class.java)
        stringOnly.chars = "TEST"
        realm.commitTransaction()
        Assert.assertEquals(1, realm.where(StringOnly::class.java).count())
        realm.close()
    }

    @Test
    fun disallow_removeClass() {
        // Init schema
        Realm.getInstance(config!!).close()
        val realm = DynamicRealm.getInstance(config!!)
        val className = "StringOnly"
        realm.beginTransaction()
        Assert.assertTrue(realm.schema.contains(className))
        try {
            realm.schema.remove(className)
            org.junit.Assert.fail()
        } catch (ignored: IllegalArgumentException) {
        } finally {
            realm.cancelTransaction()
            realm.close()
        }
    }

    @Test
    fun allow_createClass() {
        val realm = DynamicRealm.getInstance(config!!)
        val className = "Dogplace"
        realm.beginTransaction()
        realm.schema.create("Dogplace")
        realm.commitTransaction()
        Assert.assertTrue(realm.schema.contains(className))
        realm.close()
    }

    @Test
    fun disallow_renameClass() {
        // Init schema
        Realm.getInstance(config!!).close()
        val realm = DynamicRealm.getInstance(config!!)
        val className = "StringOnly"
        realm.beginTransaction()
        try {
            realm.schema.rename(className, "Dogplace")
            org.junit.Assert.fail()
        } catch (ignored: IllegalArgumentException) {
        } finally {
            realm.cancelTransaction()
            Assert.assertTrue(realm.schema.contains(className))
            realm.close()
        }
    }

    @Test
    fun disallow_removeField() {
        // Init schema
        Realm.getInstance(config!!).close()
        val realm = DynamicRealm.getInstance(config!!)
        val className = "StringOnly"
        val fieldName = "chars"
        val objectSchema = realm.schema[className]
        Assert.assertNotNull(objectSchema)
        Assert.assertTrue(objectSchema!!.hasField(fieldName))
        realm.beginTransaction()
        try {
            objectSchema.removeField(fieldName)
            org.junit.Assert.fail()
        } catch (ignored: IllegalArgumentException) {
        } finally {
            realm.cancelTransaction()
            realm.close()
        }
    }

    @Test
    fun allow_addField() {
        // Init schema
        Realm.getInstance(config!!).close()
        val className = "StringOnly"
        val realm = DynamicRealm.getInstance(config!!)
        val objectSchema = realm.schema[className]
        Assert.assertNotNull(objectSchema)
        realm.beginTransaction()
        objectSchema!!.addField("foo", String::class.java)
        realm.commitTransaction()
        Assert.assertTrue(objectSchema.hasField("foo"))
        realm.close()
    }

    @Test
    fun addPrimaryKey_notAllowed() {
        // Init schema
        Realm.getInstance(config!!).close()
        val className = "StringOnly"
        val fieldName = "chars"
        val realm = DynamicRealm.getInstance(config!!)
        val objectSchema = realm.schema[className]
        Assert.assertNotNull(objectSchema)
        Assert.assertTrue(objectSchema!!.hasField(fieldName))
        realm.beginTransaction()
        try {
            objectSchema.addPrimaryKey(fieldName)
            org.junit.Assert.fail()
        } catch (ignored: UnsupportedOperationException) {
        } finally {
            realm.cancelTransaction()
            realm.close()
        }
    }

    @Test
    fun addField_withPrimaryKeyModifier_notAllowed() {
        // Init schema
        Realm.getInstance(config!!).close()
        val className = "StringOnly"
        val realm = DynamicRealm.getInstance(config!!)
        realm.beginTransaction()
        val objectSchema = realm.schema[className]
        Assert.assertNotNull(objectSchema)
        try {
            objectSchema!!.addField("bar", String::class.java, FieldAttribute.PRIMARY_KEY)
            org.junit.Assert.fail()
        } catch (ignored: UnsupportedOperationException) {
        } finally {
            realm.cancelTransaction()
            realm.close()
        }
    }

    // Special column "__OID" should be hidden from users.
    @get:Test
    val fieldNames_stableIdColumnShouldBeHidden: Unit
        get() {
            val className = "StringOnly"
            val realm = Realm.getInstance(config!!)
            val objectSchema = realm.schema[className]
            Assert.assertNotNull(objectSchema)
            val names = objectSchema!!.fieldNames
            Assert.assertEquals(1, names.size)
            Assert.assertEquals(StringOnly.FIELD_CHARS, names.iterator().next())
            realm.close()
        }
}
