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
import io.realm.benchmarks.entities.AllTypes
import io.realm.benchmarks.entities.AllTypesPrimaryKey
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*


@RunWith(AndroidJUnit4::class)
class RealmInsertBenchmark {

    @get:Rule
    val benchmarkRule = BenchmarkRule()

    private val COLLECTION_SIZE = 100
    private lateinit var realm: Realm
    private val noPkObjects = ArrayList<AllTypes>(COLLECTION_SIZE)
    private val pkObjects = ArrayList<AllTypesPrimaryKey>(COLLECTION_SIZE)

    @Before
    fun before() {
        Realm.init(InstrumentationRegistry.getInstrumentation().targetContext)
        val config = RealmConfiguration.Builder().build()
        Realm.deleteRealm(config)
        realm = Realm.getInstance(config)

        for (i in 0 until COLLECTION_SIZE) {
            noPkObjects.add(AllTypes())
        }

        for (i in 0 until COLLECTION_SIZE) {
            val allTypesPrimaryKey = AllTypesPrimaryKey()
            allTypesPrimaryKey.columnLong = i.toLong()
            pkObjects.add(allTypesPrimaryKey)
        }

        realm.beginTransaction()
    }

    @After
    fun after() {
        realm.cancelTransaction()
        realm.close()
    }

    @Test
    fun insertNoPrimaryKey() {
        val allTypes = AllTypes()
        benchmarkRule.measureRepeated {
            realm.insert(allTypes)
        }
    }

    @Test
    fun insertNoPrimaryKeyList() {
        benchmarkRule.measureRepeated {
            realm.insert(noPkObjects)
        }
    }

    @Test
    fun insertWithPrimaryKey() {
        val allTypesPrimaryKey = AllTypesPrimaryKey()
        var i: Long = 0
        benchmarkRule.measureRepeated {
            allTypesPrimaryKey.columnLong = (i++)
            realm.insertOrUpdate(allTypesPrimaryKey)
        }
    }

    @Test
    fun insertOrUpdateWithPrimaryKeyList() {
        benchmarkRule.measureRepeated {
            realm.insertOrUpdate(pkObjects)
        }
    }

}
