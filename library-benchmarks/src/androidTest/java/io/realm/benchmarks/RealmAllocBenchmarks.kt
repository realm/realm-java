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
import androidx.test.platform.app.InstrumentationRegistry
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.benchmarks.entities.AllTypes
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class RealmAllocBenchmarks {

    @get:Rule
    val benchmarkRule = BenchmarkRule()

    private lateinit var realm: Realm

    @Before
    fun before() {
        Realm.init(InstrumentationRegistry.getInstrumentation().targetContext)
        val config = RealmConfiguration.Builder().build()
        Realm.deleteRealm(config)
        realm = Realm.getInstance(config)
        realm.beginTransaction()
        realm.createObject(AllTypes::class.java).columnRealmList.add(realm.createObject(AllTypes::class.java))
        realm.commitTransaction()
    }

    @After
    fun after() {
        realm.close()
    }

    @Test
    fun createObjects() {
        val results = realm.where(AllTypes::class.java).findAll()
        benchmarkRule.measureRepeated {
            results.first()
        }
    }

    @Test
    fun createQueries() {
        benchmarkRule.measureRepeated {
            realm.where(AllTypes::class.java)
        }
    }

    @Test
    fun createRealmResults() {
        val query = realm.where(AllTypes::class.java)
        benchmarkRule.measureRepeated {
            query.findAll()
        }
    }

    @Test
    fun createRealmLists() {
        val allTypes = realm.where(AllTypes::class.java).findFirst()!!
        benchmarkRule.measureRepeated {
            allTypes.columnRealmList
        }
    }
}
