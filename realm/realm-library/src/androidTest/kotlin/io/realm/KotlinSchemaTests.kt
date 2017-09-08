/*
 * Copyright 2017 Realm Inc.
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

import android.support.test.runner.AndroidJUnit4
import io.realm.entities.AllKotlinTypes
import io.realm.rule.TestRealmConfigurationFactory
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * This class tests how Kotlin classes are interpreted by Realm and exposed in the RealmSchema
 */
@RunWith(AndroidJUnit4::class)
class KotlinSchemaTests {

    @get:Rule
    val configFactory = TestRealmConfigurationFactory()

    private lateinit var realm: Realm

    @Before
    fun setUp() {
        realm = Realm.getInstance(configFactory.createConfiguration())
    }

    @After
    fun tearDown() {
        realm.close()
    }

    @Test
    fun kotlinTypeNonNull() {
        val objSchema = realm.schema.get(AllKotlinTypes::class.simpleName!!)!!

        assertFalse(objSchema.isNullable(AllKotlinTypes::nonNullBinary.name));
        assertFalse(objSchema.isNullable(AllKotlinTypes::nonNullBoolean.name));
        assertFalse(objSchema.isNullable(AllKotlinTypes::nonNullString.name));
        assertFalse(objSchema.isNullable(AllKotlinTypes::nonNullLong.name));
        assertFalse(objSchema.isNullable(AllKotlinTypes::nonNullInt.name));
        assertFalse(objSchema.isNullable(AllKotlinTypes::nonNullShort.name));
        assertFalse(objSchema.isNullable(AllKotlinTypes::nonNullByte.name));
        assertFalse(objSchema.isNullable(AllKotlinTypes::nonNullDate.name));
        assertFalse(objSchema.isNullable(AllKotlinTypes::nonNullDouble.name));
        assertFalse(objSchema.isNullable(AllKotlinTypes::nonNullFloat.name));
        assertFalse(objSchema.isNullable(AllKotlinTypes::nonNullList.name));
        // We cannot enforce this constraint inside the schema right now.
        // If people maintain the variant themselves they need a custom getter
        // assertTrue(objSchema.isNullable(AllKotlinTypes::nonNullObject.name));
    }

    @Test
    fun kotlinTypeNull() {
        val objSchema = realm.schema.get(AllKotlinTypes::class.simpleName!!)!!
        assertTrue(objSchema.isNullable(AllKotlinTypes::nullBinary.name));
        assertTrue(objSchema.isNullable(AllKotlinTypes::nullBoolean.name));
        assertTrue(objSchema.isNullable(AllKotlinTypes::nullString.name));
        assertTrue(objSchema.isNullable(AllKotlinTypes::nullLong.name));
        assertTrue(objSchema.isNullable(AllKotlinTypes::nullInt.name));
        assertTrue(objSchema.isNullable(AllKotlinTypes::nullShort.name));
        assertTrue(objSchema.isNullable(AllKotlinTypes::nullByte.name));
        assertTrue(objSchema.isNullable(AllKotlinTypes::nullDate.name));
        assertTrue(objSchema.isNullable(AllKotlinTypes::nullDouble.name));
        assertTrue(objSchema.isNullable(AllKotlinTypes::nullFloat.name));
        assertFalse(objSchema.isNullable(AllKotlinTypes::nullList.name)); // Managed realm objects do not allow null lists
        assertTrue(objSchema.isNullable(AllKotlinTypes::nullObject.name));
    }

}
