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
package io.realm.kotlin

import io.realm.Realm
import io.realm.RealmChangeListener
import io.realm.RealmModel
import io.realm.RealmResults
import io.realm.annotations.Beta
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf

@Beta
fun <T : RealmModel> RealmResults<T>.toFlow(): Flow<RealmResults<T>> {
    // Return "as is" if frozen, there will be no listening for changes
    if (realm.isFrozen) {
        flowOf(this)
    }

    val config = realm.configuration

    return callbackFlow {
        // Emit current (frozen) value immediately
        offer(freeze())

        val results = this@toFlow

        // Do nothing if the results are invalid
        if (!results.isValid) return@callbackFlow

        // Get instance to ensure the Realm is open for as long we are listening
        val flowRealm = Realm.getInstance(config)
        val listener = RealmChangeListener<RealmResults<T>> { listenerResults ->
            offer(listenerResults.freeze())
        }

        results.addChangeListener(listener)

        awaitClose {
            // Remove listener and cleanup
            if (!flowRealm.isClosed) {
                results.removeChangeListener(listener)
                flowRealm.close()
            }
        }
    }
}

/*
 *
 * ALTERNATIVE IMPLEMENTATION USING flow INSTEAD OF callbackFlow
 *
 *

@Beta
fun <T : RealmModel> RealmResults<T>.toFlowNew(): Flow<RealmResults<T>> {
    if (realm.isFrozen) {
        flowOf(this)
    }

    return flow {
        val results = this@toFlowNew
        if (!results.isValid) return@flow

        val observerChannel = Channel<RealmResults<T>>(Channel.CONFLATED)
        val listener = RealmChangeListener<RealmResults<T>> { listenerResults ->
            observerChannel.offer(listenerResults.freeze())
        }
        observerChannel.offer(freeze())
        val flowContext = coroutineContext
        results.addChangeListener(listener)

        withContext(flowContext) {
            try {
                // This will keep iterating until the flow is cancelled
                for (signalledResults in observerChannel) {
                    emit(signalledResults)
                }
            } finally {
                // Remember to remove change listener
                if (!realm.isClosed) {
                    removeChangeListener(listener)
                }
            }
        }
    }
}
*/
