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

package io.realm.realmany

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.realm.*
import io.realm.entities.PrimaryKeyAsString
import io.realm.entities.RealmAnyNotIndexed
import io.realm.kotlin.where
import org.bson.types.Decimal128
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*
import kotlin.collections.HashSet
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class RealmAnyQueryTests {
    private lateinit var realmConfiguration: RealmConfiguration
    private lateinit var realm: Realm

    private fun initializeTestData() {
        val realmAnyValues = RealmAnyHelper.generateRealmAnyValues()

        realm.beginTransaction()

        for (value in realmAnyValues) {
            val realmAnyObject = RealmAnyNotIndexed(value)
            realm.insert(realmAnyObject)
        }

        realm.commitTransaction()
    }
    
    @get:Rule
    val configFactory = TestRealmConfigurationFactory()

    init {
        Realm.init(InstrumentationRegistry.getInstrumentation().targetContext)
    }

    @Before
    fun setUp() {
        realmConfiguration = configFactory.createSchemaConfiguration(
                false,
                RealmAnyNotIndexed::class.java,
                PrimaryKeyAsString::class.java)

        realm = Realm.getInstance(realmConfiguration)
    }

    @After
    fun tearDown() {
        realm.close()
    }

    @Test
    fun isNull() {
        initializeTestData()
        val results = realm.where<RealmAnyNotIndexed>().isNull(RealmAnyNotIndexed.FIELD_REALM_ANY).findAll()
        assertEquals(9, results.size)
        for (result in results) {
            assertTrue(result.realmAny!!.isNull)
        }
    }

    @Test
    fun isNotNull() {
        initializeTestData()
        val results = realm.where<RealmAnyNotIndexed>().isNotNull(RealmAnyNotIndexed.FIELD_REALM_ANY).findAll()
        assertEquals(103, results.size)
        for (result in results) {
            assertFalse(result.realmAny!!.isNull)
        }
    }

    @Test
    fun isEmpty() {
        initializeTestData()
        val query: RealmQuery<RealmAnyNotIndexed> = realm.where()
        assertFailsWith<IllegalArgumentException> {
            query.isEmpty(RealmAnyNotIndexed.FIELD_REALM_ANY)
        }
    }

    @Test
    fun isNotEmpty() {
        initializeTestData()
        val query: RealmQuery<RealmAnyNotIndexed> = realm.where()
        assertFailsWith<IllegalArgumentException> {
            query.isEmpty(RealmAnyNotIndexed.FIELD_REALM_ANY)
        }
    }

    @Test
    fun count() {
        initializeTestData()
        val value = realm.where<RealmAnyNotIndexed>().count()
        assertEquals(112, value)
    }

    @Test
    fun average() {
        initializeTestData()
        val value = realm.where<RealmAnyNotIndexed>().averageRealmAny(RealmAnyNotIndexed.FIELD_REALM_ANY)
        assertEquals(Decimal128.parse("4.292307692307692307692307692307692"), value)
    }

    @Test
    fun sum() {
        initializeTestData()
        val value = realm.where<RealmAnyNotIndexed>().sum(RealmAnyNotIndexed.FIELD_REALM_ANY)
        assertEquals(Decimal128.parse("279.0"), value)
    }

    @Test
    fun min() {
        initializeTestData()
        val value = realm.where<RealmAnyNotIndexed>().minRealmAny(RealmAnyNotIndexed.FIELD_REALM_ANY)

        assertFalse(value.isNull)
        assertEquals(RealmAny.Type.BOOLEAN, value.type)
        assertFalse(value.asBoolean())
    }

    @Test
    fun max() {
        initializeTestData()
        val value = realm.where<RealmAnyNotIndexed>().maxRealmAny(RealmAnyNotIndexed.FIELD_REALM_ANY)
        assertEquals(RealmAny.valueOf(UUID.fromString("00000004-aa12-4afa-9219-e20cc3018599")), value)
    }

    @Test
    fun sort() {
        initializeTestData()
        val results = realm.where<RealmAnyNotIndexed>().sort(RealmAnyNotIndexed.FIELD_REALM_ANY).findAll()
        assertEquals(112, results.size)
        assertTrue(results.first()!!.realmAny!!.isNull)
        assertEquals(RealmAny.Type.UUID, results.last()!!.realmAny!!.type)
    }

    @Test
    fun distinct() {
        initializeTestData()
        val results = realm.where<RealmAnyNotIndexed>().distinct(RealmAnyNotIndexed.FIELD_REALM_ANY).findAll()

        val hashSet = HashSet<RealmAny>()
        for (result in results) {
            hashSet.add(result.realmAny!!)
        }
        assertEquals(66, results.size)
        assertEquals(hashSet.size, results.size)
    }
}
