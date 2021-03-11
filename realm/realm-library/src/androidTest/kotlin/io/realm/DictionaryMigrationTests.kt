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
import io.realm.entities.DictionaryContainerClass
import io.realm.entities.StringOnly
import io.realm.kotlin.createObject
import org.bson.types.Decimal128
import org.bson.types.ObjectId
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class DictionaryMigrationTests {

    @Rule
    @JvmField
    val configFactory = TestRealmConfigurationFactory()

    private val dictionaryFields = listOf(
            // The "java.lang" prefix in primitive types is needed or else Kotlin will map it to raw primitives
            Triple(DictionaryContainerClass::myBooleanDictionary, java.lang.Boolean::class.java, RealmFieldType.STRING_TO_BOOLEAN_MAP),
            Triple(DictionaryContainerClass::myIntDictionary, java.lang.Integer::class.java, RealmFieldType.STRING_TO_INTEGER_MAP),
            Triple(DictionaryContainerClass::myFloatDictionary, java.lang.Float::class.java, RealmFieldType.STRING_TO_FLOAT_MAP),
            Triple(DictionaryContainerClass::myLongDictionary, java.lang.Long::class.java, RealmFieldType.STRING_TO_INTEGER_MAP),
            Triple(DictionaryContainerClass::myShortDictionary, java.lang.Short::class.java, RealmFieldType.STRING_TO_INTEGER_MAP),
            Triple(DictionaryContainerClass::myByteDictionary, java.lang.Byte::class.java, RealmFieldType.STRING_TO_INTEGER_MAP),
            Triple(DictionaryContainerClass::myDoubleDictionary, java.lang.Double::class.java, RealmFieldType.STRING_TO_DOUBLE_MAP),
            Triple(DictionaryContainerClass::myStringDictionary, String::class.java, RealmFieldType.STRING_TO_STRING_MAP),
            Triple(DictionaryContainerClass::myBinaryDictionary, ByteArray::class.java, RealmFieldType.STRING_TO_BINARY_MAP),
            Triple(DictionaryContainerClass::myDateDictionary, Date::class.java, RealmFieldType.STRING_TO_DATE_MAP),
            Triple(DictionaryContainerClass::myObjectIdDictionary, ObjectId::class.java, RealmFieldType.STRING_TO_OBJECT_ID_MAP),
            Triple(DictionaryContainerClass::myUUIDDictionary, UUID::class.java, RealmFieldType.STRING_TO_UUID_MAP),
            Triple(DictionaryContainerClass::myDecimal128Dictionary, Decimal128::class.java, RealmFieldType.STRING_TO_DECIMAL128_MAP),
            Triple(DictionaryContainerClass::myMixedDictionary, Mixed::class.java, RealmFieldType.STRING_TO_MIXED_MAP),
            Triple(DictionaryContainerClass::myRealmModelDictionary, StringOnly::class.java, RealmFieldType.STRING_TO_LINK_MAP)
    )

    private lateinit var realm: Realm

    @Before
    fun setUp() {
        Realm.init(InstrumentationRegistry.getInstrumentation().context)
    }

    @Test
    fun migrate_realmDictionary() {
        // Creates v0 of the Realm.
        val originalConfig = configFactory.createConfigurationBuilder()
                .schema(StringOnly::class.java)
                .build()
        Realm.getInstance(originalConfig).close()

        // Creates v1 of the Realm.
        val realmConfig = configFactory
                .createConfigurationBuilder()
                .schemaVersion(1)
                .schema(StringOnly::class.java, DictionaryContainerClass::class.java)
                .migration { realm, _, _ ->
                    val schema = realm.schema.create(DictionaryContainerClass.CLASS_NAME)

                    dictionaryFields.forEach {
                        if (it.third == RealmFieldType.STRING_TO_LINK_MAP) {
                            val realmModelSchema = realm.schema.get(it.second.simpleName)
                            assertNotNull(realmModelSchema)
                            schema.addRealmDictionaryField(it.first.name, realmModelSchema)
                        } else {
                            schema.addRealmDictionaryField(it.first.name, it.second)
                        }
                    }
                }.build()

        realm = Realm.getInstance(realmConfig)

        val objectSchema = realm.schema.get(DictionaryContainerClass.CLASS_NAME)
        assertNotNull(objectSchema)
        dictionaryFields.forEach {
            assertTrue(objectSchema.hasField(it.first.name))
            assertEquals(it.third, objectSchema.getFieldType(it.first.name))
        }

        realm.executeTransaction { transactionRealm ->
            val container = transactionRealm.createObject<DictionaryContainerClass>()
            dictionaryFields.forEach {
                val dictionary = it.first.get(container)
                assertNotNull(dictionary)
                assertTrue(dictionary.isEmpty())
            }
        }

        realm.close()
    }
}
