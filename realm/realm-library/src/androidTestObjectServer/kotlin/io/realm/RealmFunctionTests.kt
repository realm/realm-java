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
import io.realm.admin.ServerAdmin
import io.realm.rule.BlockingLooperThread
import org.bson.BsonInt32
import org.bson.BsonValue
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RealmFunctionTests {
    
    private val looperThread = BlockingLooperThread()
    private lateinit var app: TestRealmApp

    private lateinit var anonUser: RealmUser
    private lateinit var admin: ServerAdmin

    @Before
    fun setUp() {
        app = TestRealmApp()
        admin = ServerAdmin()
        anonUser = app.login(RealmCredentials.anonymous())
    }

    @After
    fun tearDown() {
        app.close() 
    }

    @Test
    fun test_basic() = looperThread.runBlocking {
        val functions = anonUser.functions
        val result1a: BsonValue = functions.callFunction("sum", BsonInt32(32))
        val result1b: BsonInt32? = functions.callFunctionTyped("sum", BsonInt32::class.java, 32)
        val result1d: Integer = functions.callFunctionNativeTyped("sum", Integer::class.java, 32)
        val result1e: Int = functions.callFunctionNativeTyped("sum", Integer::class.java, 32).toInt()
        val result1f: String = functions.callFunctionNativeTyped("sum", String::class.java, "Realm")
        // Does not compile as intended
        // val result1g: BsonString = functions.callFunctionBson("sum", BsonInt32::class.java, BsonInt32(32))

        val result2: RealmAsyncTask = functions.callFunctionAsync(
                "sum",
                RealmApp.Callback<BsonValue> {result ->
                    println("function: "  + result.get())
                    looperThread.testComplete()
                },
                BsonInt32(32)
        )
    }


    
}

