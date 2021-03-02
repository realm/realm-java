/*
 * Copyright 2020 Realm Inc.
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

package io.realm

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.realm.entities.DogPrimaryKey
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import io.realm.rule.TestRealmConfigurationFactory
import org.bson.types.Decimal128
import org.bson.types.ObjectId
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
class DictTests {

    private lateinit var realm: Realm

    @Rule
    @JvmField
    val configFactory = TestRealmConfigurationFactory()

    @Before
    fun setUp() {
        Realm.init(InstrumentationRegistry.getInstrumentation().context)
        realm = Realm.getInstance(configFactory.createConfiguration())
    }

    @After
    fun tearDown() {
        realm.close()
    }

//    @Test
//    fun test_1() {
//        for (i in 1..5) {
//            realm.executeTransaction {
//                val container = it.createObject<DictionaryClass>()
//                container.myDictionary.close()
//            }
//
//            val container = realm.where<DictionaryClass>()
//                    .findFirst()
//
//            realm.executeTransaction {
//                with(container!!.myDictionary) {
//                    put("HELLO", "hi")
//                    put("BYE", "goodbye")
//                    clear()
//                }
//            }
//        }
//    }
}

open class DictionaryClass : RealmObject() {
//    val myDictionary = RealmDictionary<String>()

    val myLongList = RealmList<Long>()
    val myIntList = RealmList<Int>()
    val myShortList = RealmList<Short>()
    val myByteList = RealmList<Byte>()
    val myFloatList = RealmList<Float>()
    val myDoubleList = RealmList<Double>()
    val myStringList = RealmList<String>()
    val myBooleanList = RealmList<Boolean>()
    val myDateList = RealmList<Date>()
    val myDecimal128List = RealmList<Decimal128>()
    val myBinaryList = RealmList<ByteArray>()
    val myObjectIdList = RealmList<ObjectId>()
    val myUUIDList = RealmList<UUID>()
    val myRealmModelList = RealmList<DogPrimaryKey>()
    val myMixedList = RealmList<Mixed>()
}
