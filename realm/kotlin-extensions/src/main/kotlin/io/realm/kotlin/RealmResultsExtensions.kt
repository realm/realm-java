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
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf

/**
 * Returns a [Flow] that monitors changes to this RealmResults. It will emit the current
 * RealmResults when subscribed to. RealmResults will continually be emitted as the RealmResults are
 * updated - `onCompletion` will never be called.
 *
 * Items emitted from Realm flows are frozen - see [RealmResults.freeze]. This means that they are
 * immutable and can be read from any thread.
 *
 * Realm flows always emit items from the thread holding the live RealmResults. This means that if
 * you need to do further processing, it is recommended to collect the values on a computation
 * dispatcher:
 *
 * ```
 * realmInstance.where(Foo::class.java)
 *   .findAllAsync()
 *   .toFlow()
 *   .map { results -> doExpensiveWork(results) }
 *   .flowOn(Dispatchers.IO)
 *   .onEach { flowResults ->
 *     // ...
 *   }.launchIn(Dispatchers.Main)
 * ```
 *
 * If your would like `toFlow()` to stop emitting items you can instruct the flow to only emit the
 * first item by calling [kotlinx.coroutines.flow.first]:
 * ```
 * val foo = realmInstance.where(Foo::class.java)
 *   .findAllAsync()
 *   .toFlow()
 *   .flowOn(context)
 *   .first()
 * ```
 *
 * @return Kotlin [Flow] on which calls to `onEach` or `collect` can be made.
 */
@Beta
fun <T : RealmModel> RealmResults<T>.toFlow(): Flow<RealmResults<T>> {
    // Return "as is" if frozen, there will be no listening for changes
    if (realm.isFrozen) {
        return flowOf(this)
    }

    val config = realm.configuration

    return callbackFlow {
        val results = this@toFlow

        // Do nothing if the results are invalid
        if (!results.isValid) {
            return@callbackFlow
        }

        // Get instance to ensure the Realm is open for as long as we are listening
        val flowRealm = Realm.getInstance(config)
        val listener = RealmChangeListener<RealmResults<T>> { listenerResults ->
            offer(listenerResults.freeze())
        }

        results.addChangeListener(listener)

        // Emit current (frozen) value
        offer(freeze())

        awaitClose {
            // Remove listener and cleanup
            if (!flowRealm.isClosed) {
                results.removeChangeListener(listener)
                flowRealm.close()
            }
        }
    }
}
