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
import org.bson.BsonInt64
import org.bson.BsonString
import org.bson.BsonValue
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals

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

    /**
     * Basic test of Functions invocation.
     *
     * The arguments types combinations are not exhausted at this point as the actual conversions
     * are unit tested in [BsonTest].
     */
    @Test
    fun callFunction() = looperThread.runBlocking {
        val functions = anonUser.functions
        
        val i32 = 32
        val i64 = 32L
        val s = "Realm"

        val result1a: BsonValue = functions.callFunction("sum", listOf(BsonInt32(i32)))
        assertEquals(6L, result1a.asInt64().value)
//        val result1b: BsonInt32 = functions.callFunction("sum", listOf(i32), BsonInt32::class.java)
//        val result1d: Integer = functions.callFunction("sum", listOf(i32), Integer::class.java)
        val result1d: Any = functions.callFunction("sum", listOf(i32), java.lang.Long::class.java)
        val result1e: BsonInt64 = functions.callFunction("sum", listOf(i64), BsonInt64::class.java)
//        val result1f: String? = functions.callFunction("sum", listOf(s), BsonString::class.java).value

        // Does not compile as intended
        // val result1g: BsonString = functions.callFunctionTyped("sum", BsonInt32::class.java, BsonInt32(32))

        val result2: RealmAsyncTask = functions.callFunctionAsync( "sum", listOf(BsonInt32(i32))) { result ->
            assertEquals(6L, result.get().asInt64().value)
            looperThread.testComplete()
        }
    }

}
