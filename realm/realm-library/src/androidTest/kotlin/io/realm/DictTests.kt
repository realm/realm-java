///*
// * Copyright 2020 Realm Inc.
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// * http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package io.realm
//
//import androidx.test.ext.junit.runners.AndroidJUnit4
//import androidx.test.platform.app.InstrumentationRegistry
//import io.realm.entities.DictionaryClass
//import io.realm.entities.MyRealmModel
//import io.realm.kotlin.createObject
//import io.realm.rule.TestRealmConfigurationFactory
//import org.junit.After
//import org.junit.Before
//import org.junit.Rule
//import org.junit.Test
//import org.junit.runner.RunWith
//import kotlin.test.assertEquals
//import kotlin.test.assertNotNull
//
//@RunWith(AndroidJUnit4::class)
//class DictTests {
//
//    private lateinit var realm: Realm
//
//    @Rule
//    @JvmField
//    val configFactory = TestRealmConfigurationFactory()
//
//    @Before
//    fun setUp() {
//        Realm.init(InstrumentationRegistry.getInstrumentation().context)
//        realm = Realm.getInstance(configFactory.createConfiguration())
//    }
//
//    @After
//    fun tearDown() {
//        realm.close()
//    }
//
//    @Test
//    fun asd() {
//        realm.executeTransaction {
//            val container = realm.createObject<DictionaryClass>()
//            val populatedMixedDictionary = container.myPopulatedMixedDictionary
//            assertNotNull(populatedMixedDictionary)
//            val populatedStringMixedFromRealm = populatedMixedDictionary["HELLO"]
//            assertNotNull(populatedStringMixedFromRealm)
//            assertEquals("hello", populatedStringMixedFromRealm.asString())
//            val populatedModelMixedFromRealm = populatedMixedDictionary["BYE"]
//            assertNotNull(populatedModelMixedFromRealm)
//            val populatedRealmModelMixedFromRealm = populatedModelMixedFromRealm.asRealmModel(MyRealmModel::class.java)
//
//            val mixedDictionary = container.myMixedDictionary
//            assertNotNull(mixedDictionary)
//            container.myMixedDictionary = RealmDictionary<Mixed>().apply { put("HELLO", Mixed.valueOf("hello")) }
//            val mixedFromRealm = mixedDictionary["HELLO"]
//            assertNotNull(mixedFromRealm)
//            assertEquals("hello", mixedFromRealm.asString())
//        }
//    }
//}
