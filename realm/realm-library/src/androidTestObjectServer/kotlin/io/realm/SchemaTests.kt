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
import junit.framework.Assert.*
import junit.framework.TestCase
import org.junit.*
import org.junit.rules.ErrorCollector
import org.junit.runner.RunWith
import java.lang.Exception
import java.lang.UnsupportedOperationException
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class SchemaTests {
    @get:Rule
    val configFactory = TestSyncConfigurationFactory()

    @get:Rule
    val errorCollector = ErrorCollector()

    private lateinit var config: SyncConfiguration
    private lateinit var app: TestRealmApp

    @Before
    fun setUp() {
        app = TestRealmApp()
        val user = createTestUser(app)
        config = configFactory.createSyncConfigurationBuilder(user).build()
    }

    @After
    fun tearDown() {
        if (this::app.isInitialized) {
            app.close()
        }
    }

    @Test
    fun instance() {
        val realm = Realm.getInstance(config)
        realm.use {
            TestCase.assertFalse(realm.isClosed)
        }
        assertTrue(realm.isClosed)
    }

    @Test
    fun createObject() {
        Realm.getInstance(config).use { realm ->
            realm.beginTransaction()
            assertTrue(realm.schema.contains("StringOnly"))
            val stringOnly = realm.createObject(StringOnly::class.java)
            stringOnly.chars = "TEST"
            realm.commitTransaction()
            assertEquals(1, realm.where(StringOnly::class.java).count())
        }
    }

    @Test
    fun allow_createClass() {
        DynamicRealm.getInstance(config).use { realm ->
            val className = "Dogplace"
            realm.beginTransaction()
            realm.schema.create("Dogplace")
            realm.commitTransaction()
            assertTrue(realm.schema.contains(className))
        }
    }

    @Test
    fun allow_addField() {
        // Init schema
        Realm.getInstance(config).close()
        val className = "StringOnly"
        DynamicRealm.getInstance(config).use { realm ->
            val objectSchema = realm.schema[className]
            assertNotNull(objectSchema)
            realm.beginTransaction()
            objectSchema!!.addField("foo", String::class.java)
            assertTrue(objectSchema.hasField("foo"))
            realm.commitTransaction()
            assertTrue(objectSchema.hasField("foo"))
        }
    }

    // Special column "__OID" should be hidden from users.
    @Test
    fun fieldNames_stableIdColumnShouldBeHidden() {
        val className = "StringOnly"
        Realm.getInstance(config).use { realm ->
            val objectSchema = realm.schema[className]
            assertNotNull(objectSchema)
            val names = objectSchema!!.fieldNames
            assertEquals(1, names.size)
            assertEquals(StringOnly.FIELD_CHARS, names.iterator().next())
        }
    }

    enum class DestructiveSchemaOperation {
        REMOVE_CLASS,
        RENAME_CLASS,
        SET_CLASS_NAME,
        REMOVE_FIELD,
        RENAME_FIELD,
        REMOVE_INDEX,
        REMOVE_PRIMARY_KEY,
        ADD_PRIMARY_KEY,
        ADD_FIELD_PRIMARY_KEY,
    }

    @Test
    fun disallowDestructiveUpdateOfSyncedRealm() {
        for (operation in DestructiveSchemaOperation.values()) {
            // Init schema
            Realm.getInstance(config).close()
            val className = "StringOnly"
            val newClassName = "Dogplace"
            val fieldName = "chars"
            val newFieldName = "newchars"

            DynamicRealm.getInstance(config).use { realm ->
                assertTrue(realm.schema.contains(className))
                val objectSchema = realm.schema[className]!!
                assertNotNull(objectSchema)
                assertTrue(objectSchema.hasField(fieldName))

                realm.beginTransaction()
                errorCollector.assertFailsWith<UnsupportedOperationException> {
                    when (operation) {
                        DestructiveSchemaOperation.REMOVE_CLASS ->
                            realm.schema.remove(className)
                        DestructiveSchemaOperation.RENAME_CLASS ->
                            realm.schema.rename(className, newClassName)
                        DestructiveSchemaOperation.SET_CLASS_NAME ->
                            objectSchema.setClassName(newClassName)
                        DestructiveSchemaOperation.REMOVE_FIELD ->
                            objectSchema.removeField(fieldName)
                        DestructiveSchemaOperation.RENAME_FIELD ->
                            objectSchema.renameField(fieldName, newFieldName)
                        DestructiveSchemaOperation.REMOVE_INDEX ->
                            objectSchema.removeIndex(fieldName)
                        DestructiveSchemaOperation.REMOVE_PRIMARY_KEY ->
                            objectSchema.removePrimaryKey()
                        DestructiveSchemaOperation.ADD_PRIMARY_KEY ->
                            objectSchema.addPrimaryKey(fieldName)
                        DestructiveSchemaOperation.ADD_FIELD_PRIMARY_KEY -> {
                            objectSchema.addField(newFieldName, String::class.java, FieldAttribute.PRIMARY_KEY)
                        }
                    }
                }
                // Verify that operation is actually not performed in the transaction
                assertTrue(realm.schema.contains(className))
                assertFalse(realm.schema.contains(newClassName))
                assertTrue(objectSchema.hasField(fieldName))
                realm.cancelTransaction()

                // Verify that operation is actually not performed after cancelling
                assertTrue(realm.schema.contains(className))
                assertFalse(realm.schema.contains(newClassName))
                assertTrue(objectSchema.hasField(fieldName))
                assertFalse(objectSchema.hasField(newFieldName))
                assertNotNull(objectSchema)
            }
        }
    }

}

inline fun <reified T> ErrorCollector.assertFailsWith(block : () -> Unit){
    try {
        block()
    } catch (e : Exception) {
        if (e !is T) {
            addError(e)
        }
    }
}