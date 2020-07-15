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
import io.realm.benchmarks.entities.AllTypes
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class RealmObjectWriteBenchmarks {

    @get:Rule
    val benchmarkRule = BenchmarkRule()

    private lateinit var realm: Realm
    private lateinit var writeObject: AllTypes

    @Before
    fun before() {
        val config = RealmConfiguration.Builder().build()
        Realm.deleteRealm(config)
        realm = Realm.getInstance(config)
        realm.beginTransaction()
        writeObject = realm.createObject(AllTypes::class.java)
    }

    @After
    fun after() {
        realm.cancelTransaction()
        realm.close()
    }

    @Test
    fun writeShortString() {
        benchmarkRule.measureRepeated {
            writeObject.columnString = "Foo"
        }
    }

    @Test
    fun writeMediumString() {
        benchmarkRule.measureRepeated {
            writeObject.columnString = "ABCDEFHIJKLMNOPQ"
        }
    }

    @Test
    fun writeLongString() {
        benchmarkRule.measureRepeated {
            writeObject.columnString = "ABCDEFHIJKLMNOPQABCDEFHIJKLMNOPQABCDEFHIJKLMNOPQABCDEFHIJKLMNOPQ"
        }
    }

    @Test
    fun writeLong() {
        benchmarkRule.measureRepeated {
            writeObject.columnLong = 42
        }
    }

    @Test
    fun writeDouble() {
        benchmarkRule.measureRepeated {
            writeObject.columnDouble = 1.234
        }
    }
}
