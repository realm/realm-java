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

package io.realm.util

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers
import org.junit.Test
import org.junit.runner.RunWith
import java.lang.RuntimeException

@RunWith(AndroidJUnit4::class)
class KotlinTestUtilsTests {

    @Test
    fun assertFailsWithMessage_noException() {
        kotlin.test.assertFailsWith<AssertionError> {
            assertFailsWithMessage<IllegalArgumentException>(CoreMatchers.anything()) { }
        }
    }

    @Test
    fun assertFailsWithMessage_wrongException() {
        kotlin.test.assertFailsWith<AssertionError> {
            assertFailsWithMessage<IllegalArgumentException>(CoreMatchers.anything()) {
                throw RuntimeException()
            }
        }
    }

    @Test
    fun assertFailsWithMessage_nonMatchingMessage() {
        val message= "Exception error messages"
        kotlin.test.assertFailsWith<AssertionError> {
            assertFailsWithMessage<RuntimeException>(CoreMatchers.equalTo("")) {
                RuntimeException("Exception error messages")
            }
        }
    }

    @Test
    fun assertFailsWithMessage_matchingMessage() {
        val message= "Exception error messages"
        assertFailsWithMessage<RuntimeException>(CoreMatchers.equalTo(message)) {
            throw RuntimeException(message)
        }
    }

}
