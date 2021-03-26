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
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@RunWith(AndroidJUnit4::class)
class MixedQueryTests {
    private lateinit var realmConfiguration: RealmConfiguration
    private lateinit var realm: Realm

    private fun initializeTestData() {
        val mixedValues = MixedHelper.generateMixedValues()

        realm.beginTransaction()

        for (value in mixedValues) {
            val mixedObject = MixedNotIndexed(value)
            realm.insert(mixedObject)
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

        initializeTestData()
    }

    @After
    fun tearDown() {
        realm.close()
    }

    @Test
    fun isNull() {
        val results = realm.where<MixedNotIndexed>().isNull(MixedNotIndexed.FIELD_MIXED).findAll()
        assertEquals(9, results.size)
    }

    @Test
    fun isNotNull() {
        val results = realm.where<MixedNotIndexed>().isNotNull(MixedNotIndexed.FIELD_MIXED).findAll()
        assertEquals(97, results.size)
    }

    @Test
    fun isEmpty() {
        val query: RealmQuery<MixedNotIndexed> = realm.where()
        assertFailsWith<IllegalArgumentException> {
            query.isEmpty(MixedNotIndexed.FIELD_MIXED)
        }
    }

    @Test
    fun isNotEmpty() {
        val query: RealmQuery<MixedNotIndexed> = realm.where()
        assertFailsWith<IllegalArgumentException> {
            query.isEmpty(MixedNotIndexed.FIELD_MIXED)
        }
    }

    @Test
    fun count() {
        val value = realm.where<MixedNotIndexed>().count()
        assertEquals(106, value)
    }

    @Test
    fun average() {
        val value = realm.where<MixedNotIndexed>().averageMixed(MixedNotIndexed.FIELD_MIXED)
        assertEquals("4.292307692", value)
    }

    @Test
    fun sum() {
        val value = realm.where<MixedNotIndexed>().sumMixed(MixedNotIndexed.FIELD_MIXED)
        assertEquals(Decimal128(279), value)
    }

    @Test
    fun min() {
        val value = realm.where<MixedNotIndexed>().minMixed(MixedNotIndexed.FIELD_MIXED)
        assertEquals(Mixed.valueOf(0.toLong()), value)
    }

    @Test
    fun max() {
        val value = realm.where<MixedNotIndexed>().maxMixed(MixedNotIndexed.FIELD_MIXED)
        assertEquals(Mixed.valueOf(9.toLong()), value)
    }

    @Test
    fun minDate() {
        val value = realm.where<MixedNotIndexed>().minimumDate(MixedNotIndexed.FIELD_MIXED)
        assertEquals(Date(0), value)
    }

    @Test
    fun maxDate() {
        val value = realm.where<MixedNotIndexed>().maximumDate(MixedNotIndexed.FIELD_MIXED)
        assertEquals(Date(4), value)
    }

    @Test
    fun sort() {
        val results = realm.where<MixedNotIndexed>().sort(MixedNotIndexed.FIELD_MIXED).findAll()
        results.forEachIndexed { index, mixedNotIndexed ->
            Log.d("SORT", "$index ${mixedNotIndexed.mixed!!.type} ${mixedNotIndexed.mixed}")
        }
    }

    @Test
    fun distinct() {
        val results = realm.where<MixedNotIndexed>().distinct(MixedNotIndexed.FIELD_MIXED).findAll()
        results.forEachIndexed { index, mixedNotIndexed ->
            Log.d("DISTINCT", "$index ${mixedNotIndexed.mixed!!.type} ${mixedNotIndexed.mixed}")
        }
    }
}