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
package io.realm.kotlin

import android.os.Handler
import android.os.Looper
import io.realm.RealmChangeListener
import io.realm.RealmModel
import io.realm.RealmResults
import io.realm.annotations.Beta
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.android.asCoroutineDispatcher
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flowOn
import kotlin.coroutines.CoroutineContext


@Beta
@ExperimentalCoroutinesApi
fun <T : RealmModel> RealmResults<T>.asFlow(): Flow<RealmResults<T>> {
    val dispatcher: CoroutineContext = getCurrentLooperThreadDispatcher(this)
    return callbackFlow {
        @Suppress("UNUSED_VARIABLE")
        val results = this@asFlow // Keep strong reference to keep listeners alive
        val listener: RealmChangeListener<RealmResults<T>> = RealmChangeListener {
            if (!isClosedForSend) { // Is this needed?
                offer(it.freeze())
            }
        }
        results.addChangeListener(listener)
        offer(this@asFlow.freeze())
        awaitClose {
            results.removeChangeListener(listener)
        }
    }
    // If downstream cannot keep up, this will drop older values, so only latest is returned.
    // Similar to BackpressureStrategy.LATEST
//    .conflate()
//    .flowOn(dispatcher)
}

private fun <T : RealmModel> getCurrentLooperThreadDispatcher(results: RealmResults<T>): CoroutineContext {
    val handler = Handler(Looper.myLooper())
    return handler.asCoroutineDispatcher("RealmResultsContext[${System.identityHashCode(results)}]")
}

