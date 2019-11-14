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
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.Sort
import io.realm.benchmarks.entities.AllTypes
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class RealmQueryBenchmarks {

    @get:Rule
    val benchmarkRule = BenchmarkRule()

    private val DATA_SIZE = 1000
    private lateinit var realm: Realm

    @Before
    fun before() {
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
    }

    @After
    fun after() {
        realm.close()
    }

    @Test
    fun containsQuery() {
        benchmarkRule.measureRepeated {
            val realmResults = realm.where(AllTypes::class.java).contains(AllTypes.FIELD_STRING, "Foo 1").findAll()
        }
    }

    @Test
    fun count() {
        benchmarkRule.measureRepeated {
            val size = realm.where(AllTypes::class.java).count()
        }
    }

    @Test
    fun findAll() {
        benchmarkRule.measureRepeated {
            val results = realm.where(AllTypes::class.java).findAll()
        }
    }

    @Test
    fun findAllSortedOneField() {
        benchmarkRule.measureRepeated {
            val results = realm.where(AllTypes::class.java).sort(AllTypes.FIELD_STRING, Sort.ASCENDING).findAll()
        }
    }

}
