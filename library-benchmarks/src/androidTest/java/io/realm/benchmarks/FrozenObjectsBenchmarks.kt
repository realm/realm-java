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
import io.realm.RealmList
import io.realm.RealmResults
import io.realm.benchmarks.entities.AllTypes
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import io.realm.log.RealmLog
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
class FrozenObjectsBenchmarks {

    @get:Rule
    val benchmarkRule = BenchmarkRule()

    private lateinit var realm: Realm
    private lateinit var readObject: AllTypes
    private lateinit var realmConfig: RealmConfiguration

    @Before
    fun setUp() {
        Realm.init(InstrumentationRegistry.getInstrumentation().context)
        RealmLog.error("SETUP")
        realmConfig = RealmConfiguration.Builder().name("frozen${Random().nextLong()}.realm").build()
        realm = Realm.getInstance(realmConfig)
        realm.executeTransaction { realm ->
            readObject = realm.createObject(AllTypes::class.java)
            readObject.columnString = "Foo"
            readObject.columnLong = 42
            readObject.columnDouble = 1.234
        }
    }

    @After
    fun tearDown() {
        RealmLog.error("TEAR_DOWN");
        realm.close()
    }

    @Test
    fun freezeRealm() {
        benchmarkRule.measureRepeated {
            // Skip caching in Java and directly measure how fast it is to freeze the SharedRealm.
            // ObjectStore do not cache it, so it should be safe to run in a loop.
            realm.sharedRealm.freeze()
        }
    }

    @Test
    fun freezeResults() {
        realm.executeTransaction { r ->
            for (i in 0..10_0000) {
                val obj = r.createObject<AllTypes>()
                obj.columnString= "String: " + i
                obj.columnLong = i.toLong()
                obj.isColumnBoolean = (i % 2 == 0)
            }
        }

        var results: RealmResults<AllTypes> = realm.where<AllTypes>().findAll()
        benchmarkRule.measureRepeated {
            results.freeze()
        }
    }

    @Test
    fun freezeList() {
        var list: RealmList<AllTypes> = RealmList()
        realm.executeTransaction { r ->
            for (i in 0..10_0000) {
                list = readObject.columnRealmList
                val obj = r.createObject<AllTypes>()
                obj.columnString= "String: " + i
                obj.columnLong = i.toLong()
                obj.isColumnBoolean = (i % 2 == 0)
                list.add(obj)
            }
        }

        benchmarkRule.measureRepeated {
            list.freeze()
        }
    }

    @Test
    fun freezeObject() {
        benchmarkRule.measureRepeated {
            readObject.freeze<AllTypes>()
        }
    }
}
