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
import io.realm.entities.DictionaryClass
import io.realm.entities.StringOnly
import io.realm.rule.TestRealmConfigurationFactory
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
            Triple(DictionaryClass::myBooleanDictionary.name, java.lang.Boolean::class.java, RealmFieldType.STRING_TO_BOOLEAN_MAP),
            Triple(DictionaryClass::myIntegerDictionary.name, java.lang.Integer::class.java, RealmFieldType.STRING_TO_INTEGER_MAP),
            Triple(DictionaryClass::myFloatDictionary.name, java.lang.Float::class.java, RealmFieldType.STRING_TO_FLOAT_MAP),
            Triple(DictionaryClass::myLongDictionary.name, java.lang.Long::class.java, RealmFieldType.STRING_TO_INTEGER_MAP),
            Triple(DictionaryClass::myShortDictionary.name, java.lang.Short::class.java, RealmFieldType.STRING_TO_INTEGER_MAP),
            Triple(DictionaryClass::myByteDictionary.name, java.lang.Byte::class.java, RealmFieldType.STRING_TO_INTEGER_MAP),
            Triple(DictionaryClass::myDoubleDictionary.name, java.lang.Double::class.java, RealmFieldType.STRING_TO_DOUBLE_MAP),
            Triple(DictionaryClass::myStringDictionary.name, String::class.java, RealmFieldType.STRING_TO_STRING_MAP),
            Triple(DictionaryClass::myBinaryDictionary.name, ByteArray::class.java, RealmFieldType.STRING_TO_BINARY_MAP),
            Triple(DictionaryClass::myDateDictionary.name, Date::class.java, RealmFieldType.STRING_TO_DATE_MAP),
            Triple(DictionaryClass::myObjectIdDictionary.name, ObjectId::class.java, RealmFieldType.STRING_TO_OBJECT_ID_MAP),
            Triple(DictionaryClass::myUUIDDictionary.name, UUID::class.java, RealmFieldType.STRING_TO_UUID_MAP),
            Triple(DictionaryClass::myDecimal128Dictionary.name, Decimal128::class.java, RealmFieldType.STRING_TO_DECIMAL128_MAP),
            Triple(DictionaryClass::myMixedDictionary.name, Mixed::class.java, RealmFieldType.STRING_TO_MIXED_MAP)
    )

    private lateinit var realm: Realm

    @Before
    fun setUp() {
        Realm.init(InstrumentationRegistry.getInstrumentation().context)
    }

    @Test
    fun migrateRealm_dictionary() {
        // Creates v0 of the Realm.
        val originalConfig = configFactory.createConfigurationBuilder()
                .schema(StringOnly::class.java)
                .build()
        Realm.getInstance(originalConfig).close()

        // Creates v1 of the Realm.
        val realmConfig = configFactory
                .createConfigurationBuilder()
                .schemaVersion(1)
                .schema(StringOnly::class.java, DictionaryClass::class.java)
                .migration { realm, _, _ ->
                    val schema = realm.schema
                    val realmObjectSchema = schema.create(DictionaryClass.CLASS_NAME)
                    dictionaryFields.forEach {
                        realmObjectSchema.addRealmDictionaryField(it.first, it.second)
                    }
                }.build()

        realm = Realm.getInstance(realmConfig)

        val objectSchema = realm.schema.get(DictionaryClass.CLASS_NAME)
        assertNotNull(objectSchema)
        dictionaryFields.forEach {
            assertTrue(objectSchema.hasField(it.first))
            assertEquals(it.third, objectSchema.getFieldType(it.first))
        }

        realm.close()
    }
}
