/*
 * Copyright 2019 Realm Inc.
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

package io.realm.benchmarks

import androidx.benchmark.junit4.BenchmarkRule
import androidx.benchmark.junit4.measureRepeated
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmResults
import io.realm.benchmarks.entities.AllTypes
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class RealmResultsBenchmarks {

    @get:Rule
    val benchmarkRule = BenchmarkRule()

    private val DATA_SIZE = 1000
    private lateinit var realm: Realm
    private lateinit var results: RealmResults<AllTypes>

    @Before
    fun before() {
        Realm.init(InstrumentationRegistry.getInstrumentation().targetContext)
        val config = RealmConfiguration.Builder().build()
        Realm.deleteRealm(config)
        realm = Realm.getInstance(config)
        realm.beginTransaction()
        for (i in 0 until DATA_SIZE) {
            val obj = realm.createObject(AllTypes::class.java)
            obj.columnLong = i.toLong()
            obj.isColumnBoolean = i % 2 == 0
            obj.columnString = "Foo $i"
            obj.columnDouble = i + 1.234
        }
        realm.commitTransaction()
        results = realm.where(AllTypes::class.java).findAll()
    }

    @After
    fun after() {
        realm.close()
    }

    @Test
    fun get() {
        benchmarkRule.measureRepeated {
            val item = results[0]
        }
    }

    @Test
    fun size() {
        benchmarkRule.measureRepeated {
            val size = results.size.toLong()
        }
    }

    @Test
    fun min() {
        benchmarkRule.measureRepeated {
            val min = results.min(AllTypes.FIELD_LONG)
        }
    }

    @Test
    fun max() {
        benchmarkRule.measureRepeated {
            val max = results.max(AllTypes.FIELD_LONG)
        }
    }

    @Test
    fun average() {
        benchmarkRule.measureRepeated {
            val average = results.average(AllTypes.FIELD_LONG)
        }
    }

    @Test
    fun sum() {
        benchmarkRule.measureRepeated {
            val sum = results.sum(AllTypes.FIELD_LONG)
        }
    }

    @Test
    fun sort() {
        benchmarkRule.measureRepeated {
            val sorted = results.sort(AllTypes.FIELD_STRING)
        }
    }

}
