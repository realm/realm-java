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

import com.google.android.gms.tasks.Task
import java.util.concurrent.CountDownLatch

/**
 * Returns the result of a [Task] in a synchronous way or will throw an exception if a failure is
 * detected. This operation blocks the thread on which it is called.
 *
 * @return the [T] result emitted by the task
 */
fun <T> Task<T>.blockingGetResult(): T? {
    val countDownLatch = CountDownLatch(1)
    var error: Exception? = null
    var result: T? = null

    addOnSuccessListener { successResult ->
        result = successResult
        countDownLatch.countDown()
    }
    addOnFailureListener { exception ->
        error = exception
        countDownLatch.countDown()
    }

    countDownLatch.await()

    if (error != null) {
        throw error!!
    }

    return result
}
