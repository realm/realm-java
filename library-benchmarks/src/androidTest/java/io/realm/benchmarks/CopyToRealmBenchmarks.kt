/* Copyright 2019 Realm Inc.
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
import io.realm.RealmList
import io.realm.benchmarks.entities.AllTypes
import io.realm.benchmarks.entities.AllTypesPrimaryKey
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*


@RunWith(AndroidJUnit4::class)
class CopyToRealmBenchmarks {

    @get:Rule
    val benchmarkRule = BenchmarkRule()

    private val COLLECTION_SIZE = 100
    private lateinit var realm: Realm
    private val noPkObjects = ArrayList<AllTypes>(COLLECTION_SIZE)
    private val pkObjects = ArrayList<AllTypesPrimaryKey>(COLLECTION_SIZE)
    private lateinit var complextTestObjects: ArrayList<AllTypesPrimaryKey>
    private lateinit var simpleTestObjects: ArrayList<AllTypes>

    @Before
    fun before() {
        Realm.init(InstrumentationRegistry.getInstrumentation().targetContext)
        val config = RealmConfiguration.Builder().build()
        Realm.deleteRealm(config)

        // Create test data
        complextTestObjects = ArrayList()
        for (i in 0 until COLLECTION_SIZE) {
            val obj = AllTypesPrimaryKey()
            obj.columnString = "obj$i"
            obj.columnLong = i.toLong()
            obj.columnFloat = 1.23f
            obj.columnDouble = 1.234
            obj.isColumnBoolean = true
            obj.columnDate = Date(1000)
            obj.columnBinary = byteArrayOf(1, 2, 3)
            obj.columnRealmObject = obj
            obj.columnRealmList = RealmList(obj, obj, obj)
            obj.columnBooleanList = RealmList(true, false, true)
            obj.columnStringList = RealmList("foo", "bar", "baz")
            obj.columnBinaryList = RealmList(byteArrayOf(0, 1, 2), byteArrayOf(2, 3, 4), byteArrayOf(4, 5, 6))
            obj.columnByteList = RealmList(1.toByte(), 2.toByte(), 3.toByte())
            obj.columnShortList = RealmList(1.toShort(), 2.toShort(), 3.toShort())
            obj.columnIntegerList = RealmList(1, 2, 3)
            obj.columnLongList = RealmList(1L, 2L, 3L)
            obj.columnFloatList = RealmList(1.1f, 1.2f, 1.3f)
            obj.columnDoubleList = RealmList(1.111, 1.222, 1.333)
            obj.columnDateList = RealmList(Date(1000), Date(2000), Date(3000))
            complextTestObjects.add(obj)
        }

        simpleTestObjects = ArrayList()
        for (i in 0 until COLLECTION_SIZE) {
            val obj = AllTypes()
            obj.columnString = "obj$i"
            obj.columnLong = i.toLong()
            obj.columnFloat = 1.23f
            obj.columnDouble = 1.234
            obj.isColumnBoolean = true
            obj.columnDate = Date(1000)
            obj.columnBinary = byteArrayOf(1, 2, 3)
            simpleTestObjects.add(obj)
        }

        // Setup Realm before test
        realm = Realm.getInstance(config)
        realm.beginTransaction()
    }

    @After
    fun after() {
        realm.cancelTransaction()
        realm.close()
    }

    @Test
    fun copyToRealm_complexObjects() = benchmarkRule.measureRepeated {
        realm.copyToRealmOrUpdate(complextTestObjects)
    }

    @Test
    fun copyToRealm_simpleObjects() = benchmarkRule.measureRepeated {
        realm.copyToRealm(simpleTestObjects)
    }

}
