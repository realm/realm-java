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

import androidx.test.platform.app.InstrumentationRegistry
import org.bson.BsonArray
import org.bson.BsonInt32
import org.bson.BsonInt64
import org.bson.BsonString
import org.junit.Test
import kotlin.test.assertEquals

class RealmFunctionsTest {

    @Test
    operator fun invoke() {
        Realm.init(InstrumentationRegistry.getInstrumentation().targetContext)

        val functions = RealmFunctions()
        val i32 = 42
        val i64 = 42L
        val s = "Realm"

        // Return 64 bit integer
//        assertEquals(i32, functions.invoke(BsonInt32(i32)).asInt32().value)
        assertEquals(i64, functions.invoke(BsonInt64(i64)).asInt64().value)
        assertEquals(s, functions.invoke(BsonString(s)).asString().value)
        // Crashes
//        val invoke = functions.invoke(BsonArray(listOf(BsonInt32(i32), BsonInt64(i64), BsonString(s))))
    }

}
