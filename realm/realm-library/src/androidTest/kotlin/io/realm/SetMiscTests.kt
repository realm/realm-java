/*
 * Copyright 2021 Realm Inc.
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
import io.realm.entities.SetContainerAfterMigrationClass
import io.realm.entities.SetContainerMigrationClass
import io.realm.entities.StringOnly
import io.realm.kotlin.createObject
import org.bson.types.Decimal128
import org.bson.types.ObjectId
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*
import kotlin.test.*

@RunWith(AndroidJUnit4::class)
class SetMiscTests {

    @Rule
    @JvmField
    val configFactory = TestRealmConfigurationFactory()

    private val setFields = listOf(
        // The "java.lang" prefix in primitive types is needed or else Kotlin will map it to raw primitives
        Triple(
            SetContainerMigrationClass::myBooleanSet,
            java.lang.Boolean::class.java,
            RealmFieldType.BOOLEAN_SET
        ),
        Triple(
            SetContainerMigrationClass::myIntSet,
            java.lang.Integer::class.java,
            RealmFieldType.INTEGER_SET
        ),
        Triple(
            SetContainerMigrationClass::myFloatSet,
            java.lang.Float::class.java,
            RealmFieldType.FLOAT_SET
        ),
        Triple(
            SetContainerMigrationClass::myLongSet,
            java.lang.Long::class.java,
            RealmFieldType.INTEGER_SET
        ),
        Triple(
            SetContainerMigrationClass::myShortSet,
            java.lang.Short::class.java,
            RealmFieldType.INTEGER_SET
        ),
        Triple(
            SetContainerMigrationClass::myByteSet,
            java.lang.Byte::class.java,
            RealmFieldType.INTEGER_SET
        ),
        Triple(
            SetContainerMigrationClass::myDoubleSet,
            java.lang.Double::class.java,
            RealmFieldType.DOUBLE_SET
        ),
        Triple(
            SetContainerMigrationClass::myStringSet,
            String::class.java,
            RealmFieldType.STRING_SET
        ),
        Triple(
            SetContainerMigrationClass::myBinarySet,
            ByteArray::class.java,
            RealmFieldType.BINARY_SET
        ),
        Triple(
            SetContainerMigrationClass::myDateSet,
            Date::class.java,
            RealmFieldType.DATE_SET
        ),
        Triple(
            SetContainerMigrationClass::myObjectIdSet,
            ObjectId::class.java,
            RealmFieldType.OBJECT_ID_SET
        ),
        Triple(
            SetContainerMigrationClass::myUUIDSet,
            UUID::class.java,
            RealmFieldType.UUID_SET
        ),
        Triple(
            SetContainerMigrationClass::myDecimal128Set,
            Decimal128::class.java,
            RealmFieldType.DECIMAL128_SET
        ),
        Triple(
            SetContainerMigrationClass::myMixedSet,
            Mixed::class.java,
            RealmFieldType.MIXED_SET
        ),
        Triple(
            SetContainerMigrationClass::myRealmModelSet,
            StringOnly::class.java,
            RealmFieldType.LINK_SET
        )
    )

    private lateinit var realm: Realm

    @Before
    fun setUp() {
        Realm.init(InstrumentationRegistry.getInstrumentation().context)
    }

    @After
    fun tearDown() {
        realm.close()
    }

    @Test
    fun migrate_realmSet() {
        // Creates v0 of the Realm.
        val originalConfig = configFactory.createConfigurationBuilder()
            .schema(StringOnly::class.java)
            .build()
        Realm.getInstance(originalConfig).close()

        // Creates v1 of the Realm.
        val realmConfig = configFactory.createConfigurationBuilder()
            .schemaVersion(1)
            .schema(StringOnly::class.java, SetContainerMigrationClass::class.java)
            .migration { realm, _, _ ->
                val schema = realm.schema.create(SetContainerMigrationClass.CLASS_NAME)
                schema.addField("id", String::class.java)
                    .addPrimaryKey("id")

                setFields.forEach {
                    val objectSchema = if (it.third == RealmFieldType.LINK_SET) {
                        val realmModelSchema = realm.schema.get(it.second.simpleName)
                        assertNotNull(realmModelSchema)
                        schema.addRealmSetField(it.first.name, realmModelSchema)
                    } else {
                        schema.addRealmSetField(it.first.name, it.second)
                    }
                    assertNotNull(objectSchema)
                    assertTrue(objectSchema.hasField(it.first.name))
                }
            }.build()

        realm = Realm.getInstance(realmConfig)

        val objectSchema = realm.schema.get(SetContainerMigrationClass.CLASS_NAME)
        assertNotNull(objectSchema)
        setFields.forEach {
            assertTrue(objectSchema.hasField(it.first.name))
            assertEquals(it.third, objectSchema.getFieldType(it.first.name))
        }

        realm.executeTransaction { transactionRealm ->
            val container = transactionRealm.createObject<SetContainerMigrationClass>("")
            setFields.forEach {
                val set = it.first.get(container)
                assertNotNull(set)
                assertTrue(set.isEmpty())
            }
        }

        realm.close()
    }

    @Test
    fun migrate_removeRealmSet() {
        // Creates v0 of the Realm.
        val originalConfig = configFactory.createConfigurationBuilder()
            .schema(StringOnly::class.java, SetContainerAfterMigrationClass::class.java)
            .build()

        // Initialize the schema
        Realm.getInstance(originalConfig).close()
        val localRealm = DynamicRealm.getInstance(originalConfig)
        localRealm.executeTransaction {
            val schema = it.schema

            // Remove the "end-result" class from schema as we need to recreate it "from scratch"
            schema.remove(SetContainerAfterMigrationClass.CLASS_NAME)
            val createdSchema = schema.create(SetContainerAfterMigrationClass.CLASS_NAME)
            createdSchema.addField("id", String::class.java)
                .addPrimaryKey("id")

            setFields.forEach { setField ->
                // Now add the fields that will be removed in the migration
                if (setField.third == RealmFieldType.LINK_SET) {
                    createdSchema.addRealmObjectField(setField.first.name, createdSchema)
                } else {
                    createdSchema.addField(setField.first.name, setField.second)
                }
            }
            assertEquals(setFields.size + 1, createdSchema.fieldNames.size)
        }
        localRealm.close()

        // Creates v1 of the Realm.
        val realmConfig = configFactory.createConfigurationBuilder()
            .schemaVersion(1)
            .schema(StringOnly::class.java, SetContainerAfterMigrationClass::class.java)
            .migration { realm, _, _ ->
                val schema = realm.schema.get(SetContainerAfterMigrationClass.CLASS_NAME)
                assertNotNull(schema)

                setFields.forEach {
                    assertTrue(schema.hasField(it.first.name))
                    schema.removeField(it.first.name)
                    assertFalse(schema.hasField(it.first.name))
                }
                assertEquals(1, schema.fieldNames.size)
            }.build()

        realm = Realm.getInstance(realmConfig)

        val objectSchema = realm.schema.get(SetContainerAfterMigrationClass.CLASS_NAME)
        assertNotNull(objectSchema)
        setFields.forEach {
            assertFalse(objectSchema.hasField(it.first.name))
        }
        assertEquals(1, objectSchema.fieldNames.size)

        realm.close()
    }

    @Test
    fun insert_unsupportedOperation() {
        realm = Realm.getInstance(configFactory.createConfiguration())
        realm.executeTransaction {
            assertFailsWith<UnsupportedOperationException> {
                realm.insert(SetContainerMigrationClass())
            }
        }
    }

    @Test
    fun insertOrUpdate_unsupportedOperation() {
        realm = Realm.getInstance(configFactory.createConfiguration())
        realm.executeTransaction {
            assertFailsWith<UnsupportedOperationException> {
                realm.insertOrUpdate(SetContainerMigrationClass())
            }
        }
    }
}
