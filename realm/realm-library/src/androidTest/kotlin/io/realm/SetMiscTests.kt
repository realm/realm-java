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
import io.realm.entities.SetMigrationContainerClass
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
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class SetMiscTests {

    @Rule
    @JvmField
    val configFactory = TestRealmConfigurationFactory()

    private val setFields = listOf(
        // The "java.lang" prefix in primitive types is needed or else Kotlin will map it to raw primitives
        Triple(
            SetMigrationContainerClass::myBooleanSet,
            java.lang.Boolean::class.java,
            RealmFieldType.BOOLEAN_SET
        ),
        Triple(
            SetMigrationContainerClass::myIntSet,
            java.lang.Integer::class.java,
            RealmFieldType.INTEGER_SET
        ),
        Triple(
            SetMigrationContainerClass::myFloatSet,
            java.lang.Float::class.java,
            RealmFieldType.FLOAT_SET
        ),
        Triple(
            SetMigrationContainerClass::myLongSet,
            java.lang.Long::class.java,
            RealmFieldType.INTEGER_SET
        ),
        Triple(
            SetMigrationContainerClass::myShortSet,
            java.lang.Short::class.java,
            RealmFieldType.INTEGER_SET
        ),
        Triple(
            SetMigrationContainerClass::myByteSet,
            java.lang.Byte::class.java,
            RealmFieldType.INTEGER_SET
        ),
        Triple(
            SetMigrationContainerClass::myDoubleSet,
            java.lang.Double::class.java,
            RealmFieldType.DOUBLE_SET
        ),
        Triple(
            SetMigrationContainerClass::myStringSet,
            String::class.java,
            RealmFieldType.STRING_SET
        ),
        Triple(
            SetMigrationContainerClass::myBinarySet,
            ByteArray::class.java,
            RealmFieldType.BINARY_SET
        ),
        Triple(
            SetMigrationContainerClass::myDateSet,
            Date::class.java,
            RealmFieldType.DATE_SET
        ),
        Triple(
            SetMigrationContainerClass::myObjectIdSet,
            ObjectId::class.java,
            RealmFieldType.OBJECT_ID_SET
        ),
        Triple(
            SetMigrationContainerClass::myUUIDSet,
            UUID::class.java,
            RealmFieldType.UUID_SET
        ),
        Triple(
            SetMigrationContainerClass::myDecimal128Set,
            Decimal128::class.java,
            RealmFieldType.DECIMAL128_SET
        ),
        Triple(
            SetMigrationContainerClass::myMixedSet,
            Mixed::class.java,
            RealmFieldType.MIXED_SET
        ),
        Triple(
            SetMigrationContainerClass::myRealmModelSet,
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
            .schema(StringOnly::class.java, SetMigrationContainerClass::class.java)
            .migration { realm, _, _ ->
                val schema = realm.schema.create(SetMigrationContainerClass.CLASS_NAME)

                setFields.forEach {
                    if (it.third == RealmFieldType.LINK_SET) {
                        val realmModelSchema = realm.schema.get(it.second.simpleName)
                        assertNotNull(realmModelSchema)
                        schema.addRealmSetField(it.first.name, realmModelSchema)
                    } else {
                        schema.addRealmSetField(it.first.name, it.second)
                    }
                }
            }.build()

        realm = Realm.getInstance(realmConfig)

        val objectSchema = realm.schema.get(SetMigrationContainerClass.CLASS_NAME)
        assertNotNull(objectSchema)
        setFields.forEach {
            assertTrue(objectSchema.hasField(it.first.name))
            assertEquals(it.third, objectSchema.getFieldType(it.first.name))
        }

        realm.executeTransaction { transactionRealm ->
            val container = transactionRealm.createObject<SetMigrationContainerClass>()
            setFields.forEach {
                val set = it.first.get(container)
                assertNotNull(set)
                assertTrue(set.isEmpty())
            }
        }

        realm.close()
    }
}
