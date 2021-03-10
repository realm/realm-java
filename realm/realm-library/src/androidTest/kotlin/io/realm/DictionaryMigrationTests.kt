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
import io.realm.entities.DictionaryContainerClass
import io.realm.entities.EmbeddedObjectDictionaryContainerClass
import io.realm.entities.StringOnly
import io.realm.entities.embedded.EmbeddedSimpleChild
import io.realm.entities.embedded.EmbeddedSimpleParent
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import org.bson.types.Decimal128
import org.bson.types.ObjectId
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*
import kotlin.reflect.KProperty1
import kotlin.test.*

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

    @After
    fun tearDown() {
        realm.close()
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

    @Test
    fun put_embeddedObject() {
        realm = Realm.getInstance(configFactory.createConfiguration())
        realm.executeTransaction {
            val parent = EmbeddedSimpleParent("parent")
            parent.child = EmbeddedSimpleChild("child")

            val managedParent: EmbeddedSimpleParent = it.copyToRealm(parent)
            assertEquals(1, realm.where<EmbeddedSimpleChild>().count())
            val managedChild: EmbeddedSimpleChild = managedParent.child!!

            // Dictionary
            val container = it.createObject<EmbeddedObjectDictionaryContainerClass>()
            val dictionary = container.myEmbeddedObjectDictionary
            dictionary["HELLO"] = managedChild

            assertEquals(1, dictionary.size)
            val valueFromDictionary = dictionary["HELLO"]
            assertNotNull(valueFromDictionary)
            assertNotEquals(managedChild, valueFromDictionary)     // should be NOT equals, they contain different objKeys

            managedParent.deleteFromRealm()
            assertFalse(managedParent.isValid)
            assertEquals(1, dictionary.size)
            assertFalse(managedChild.isValid)
            assertTrue(valueFromDictionary.isValid)
        }
    }

    @Test
    fun copyToRealm_unmanagedEmbeddedObject() {
        realm = Realm.getInstance(configFactory.createConfiguration())
        realm.executeTransaction {
            val unmanagedChild = EmbeddedSimpleChild("child")

            val unmanagedContainer = EmbeddedObjectDictionaryContainerClass().apply {
                myEmbeddedObjectDictionary["KEY_EMBEDDED"] = unmanagedChild
            }

            val managedContainer = it.copyToRealm(unmanagedContainer)
            assertNotNull(managedContainer)
            val managedDictionary = managedContainer.myEmbeddedObjectDictionary
            assertNotNull(managedDictionary)
            assertEquals(1, managedDictionary.size)

            val managedChild = managedDictionary["KEY_EMBEDDED"]
            assertNotNull(managedChild)
            assertTrue(managedChild.isValid)
            assertEquals(unmanagedChild.childId, managedChild.childId)

            assertEquals(1, it.where<EmbeddedSimpleChild>().count())
            managedDictionary.clear()
            assertTrue(managedDictionary.isEmpty())
            assertEquals(0, it.where<EmbeddedSimpleChild>().count())
            assertFalse(managedChild.isValid)
        }
    }

    @Test
    fun query() {
        realm = Realm.getInstance(configFactory.createConfiguration())
        realm.executeTransaction { transactionRealm ->
            val container = transactionRealm.createObject<DictionaryContainerClass>()

            dictionaryFields.forEach {
                when (it.second) {
                    java.lang.Boolean::class.java -> {
                        val dictionaryProperty = it.first as KProperty1<DictionaryContainerClass, RealmDictionary<Boolean>>
                        val dictionary: RealmDictionary<Boolean> = dictionaryProperty.get(container)
                        dictionary["A"] = true
//                        dictionary["B"] = false

                        val equalsPredicate1 = """${dictionaryProperty.name}.@keys == 'A'"""
                        val equals1 = transactionRealm.where<DictionaryContainerClass>()
                                .rawPredicate(equalsPredicate1)
                                .findAll()
                        val equalsPredicate2 = """${dictionaryProperty.name}.@keys == 'C'"""
                        val equals2 = transactionRealm.where<DictionaryContainerClass>()
                                .rawPredicate(equalsPredicate2)
                                .findAll()

                        val valueContainsPredicate1 = """${dictionaryProperty.name}.@values == TRUE"""
                        val valueContains1 = transactionRealm.where<DictionaryContainerClass>()
                                .rawPredicate(valueContainsPredicate1)
                                .findAll()
                        val valueContainsPredicate2 = """${dictionaryProperty.name}.@values == FALSE"""
                        val valueContains2 = transactionRealm.where<DictionaryContainerClass>()
                                .rawPredicate(valueContainsPredicate2)
                                .findAll()

                        val startsWithPredicate1 = """${dictionaryProperty.name}.@keys beginsWith 'A'"""
                        val startsWith1 = transactionRealm.where<DictionaryContainerClass>()
                                .rawPredicate(startsWithPredicate1)
                                .findAll()
                        val startsWithPredicate2 = """${dictionaryProperty.name}.@keys beginsWith 'B'"""
                        val startsWith2 = transactionRealm.where<DictionaryContainerClass>()
                                .rawPredicate(startsWithPredicate2)
                                .findAll()

                        val kjahs = 0
                    }
//                    java.lang.Boolean::class.java -> ""
//                    java.lang.Integer::class.java -> ""
//                    java.lang.Float::class.java -> ""
//                    java.lang.Long::class.java -> ""
//                    java.lang.Short::class.java -> ""
//                    java.lang.Byte::class.java -> ""
//                    java.lang.Double::class.java -> ""
//                    String::class.java -> ""
//                    ByteArray::class.java -> ""
//                    Date::class.java -> ""
//                    ObjectId::class.java -> ""
//                    UUID::class.java -> ""
//                    Decimal128::class.java -> ""
//                    Mixed::class.java -> ""
//                    StringOnly::class.java -> ""
                }
            }
        }
    }
}
