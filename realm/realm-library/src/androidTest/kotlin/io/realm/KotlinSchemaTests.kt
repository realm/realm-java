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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.reflect.full.memberProperties

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
        val objSchema = realm.getSchema().get(AllKotlinTypes::class.simpleName)

        AllKotlinTypes::class.memberProperties
                .filter { it.name.startsWith("nonNull") }
                .forEach({
                    // Document current nullability
                    // TODO We should fix this. Tracked by https://github.com/realm/realm-java/issues/4701
                    assertTrue("Field ${it.name} was not nullable as expected", objSchema.isNullable(it.name))
                })
    }
}