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

import io.realm.internal.common.TaskDispatcher
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.fail

class TaskUtilsKtTest {

    @Test
    fun blockingGetResult() {
        TaskDispatcher()
                .dispatchTask { RESULT }
                .blockingGetResult()
                .let { assertEquals(RESULT, it) }

        TaskDispatcher()
                .dispatchTask { null }
                .blockingGetResult()
                .let { assertNull(it) }
    }

    @Test
    fun blockingGetResultThrows() {
        assertFailsWith(RuntimeException::class) {
            TaskDispatcher()
                    .dispatchTask { throw RuntimeException("BOOM!") }
                    .blockingGetResult()
            fail()
        }
    }

    private companion object {
        const val RESULT = 666
    }
}
