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

package io.realm.mixed

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.realm.*
import io.realm.entities.MixedNotIndexed
import io.realm.kotlin.where
import org.bson.types.Decimal128
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*
import kotlin.test.*

@RunWith(AndroidJUnit4::class)
class MixedQueryTests {
    private lateinit var realmConfiguration: RealmConfiguration
    private lateinit var realm: Realm

    private fun initializeTestData(stripNulls: Boolean = false) {
        val mixedValues = MixedHelper.generateMixedValues()

        realm.beginTransaction()

        for (value in mixedValues) {
            if (!value.isNull || !stripNulls) {
                val mixedObject = MixedNotIndexed(value)
                realm.insert(mixedObject)
            }
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
                MixedNotIndexed::class.java)

        realm = Realm.getInstance(realmConfiguration)
    }

    @After
    fun tearDown() {
        realm.close()
    }

    @Test
    fun isNull() {
        initializeTestData()
        val results = realm.where<MixedNotIndexed>().isNull(MixedNotIndexed.FIELD_MIXED).findAll()
        assertEquals(9, results.size)
    }

    @Test
    fun isNotNull() {
        initializeTestData()
        val results = realm.where<MixedNotIndexed>().isNotNull(MixedNotIndexed.FIELD_MIXED).findAll()
        assertEquals(97, results.size)
    }

    @Test
    fun isEmpty() {
        initializeTestData()
        val query: RealmQuery<MixedNotIndexed> = realm.where()
        assertFailsWith<IllegalArgumentException> {
            query.isEmpty(MixedNotIndexed.FIELD_MIXED)
        }
    }

    @Test
    fun isNotEmpty() {
        initializeTestData()
        val query: RealmQuery<MixedNotIndexed> = realm.where()
        assertFailsWith<IllegalArgumentException> {
            query.isEmpty(MixedNotIndexed.FIELD_MIXED)
        }
    }

    @Test
    fun count() {
        initializeTestData()
        val value = realm.where<MixedNotIndexed>().count()
        assertEquals(106, value)
    }

    @Test
    fun average() {
        initializeTestData()
        val value = realm.where<MixedNotIndexed>().averageMixed(MixedNotIndexed.FIELD_MIXED)
        assertEquals(Decimal128.parse("4.292307692307692307692307692307692"), value)
    }

    @Test
    fun sum() {
        initializeTestData()
        val value = realm.where<MixedNotIndexed>().sum(MixedNotIndexed.FIELD_MIXED)
        assertEquals(Decimal128.parse("279.0"), value)
    }

    @Test
    fun min() {
        initializeTestData()
        val value = realm.where<MixedNotIndexed>().minMixed(MixedNotIndexed.FIELD_MIXED)
        assertTrue(value.isNull)
    }

    @Test
    fun min_without_nulls() {
        initializeTestData(true)
        realm.executeTransaction{
            val mixedObject = MixedNotIndexed(Mixed.nullValue())
            realm.insert(mixedObject)
        }

        val value = realm.where<MixedNotIndexed>().minMixed(MixedNotIndexed.FIELD_MIXED)

        assertFalse(value.isNull)
        assertEquals(MixedType.INTEGER, value.type)
        assertEquals(0.toLong(), value.asLong())
    }

    @Test
    fun max() {
        initializeTestData()
        val value = realm.where<MixedNotIndexed>().maxMixed(MixedNotIndexed.FIELD_MIXED)
        assertEquals(Mixed.valueOf(UUID.fromString("00000004-aa12-4afa-9219-e20cc3018599")), value)
    }

    @Test
    fun sort() {
        initializeTestData()
        val results = realm.where<MixedNotIndexed>().sort(MixedNotIndexed.FIELD_MIXED).findAll()
        results.forEachIndexed { index, mixedNotIndexed ->
            Log.d("SORT", "$index ${mixedNotIndexed.mixed!!.type} ${mixedNotIndexed.mixed}")
        }
    }

    @Test
    fun distinct() {
        initializeTestData()
        val results = realm.where<MixedNotIndexed>().distinct(MixedNotIndexed.FIELD_MIXED).findAll()
        results.forEachIndexed { index, mixedNotIndexed ->
            Log.d("DISTINCT", "$index ${mixedNotIndexed.mixed!!.type} ${mixedNotIndexed.mixed}")
        }
    }
}