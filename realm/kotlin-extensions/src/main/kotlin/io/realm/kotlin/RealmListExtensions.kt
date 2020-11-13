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

import io.realm.*
import io.realm.annotations.Beta
import io.realm.rx.CollectionChange
import kotlinx.coroutines.flow.Flow

/**
 * Returns a [Flow] that monitors changes to this RealmList. It will emit the current
 * RealmResults when subscribed to. RealmList updates will continually be emitted as the RealmList
 * is updated - `onCompletion` will never be called.
 *
 * Items emitted from Realm flows are frozen - see [RealmList.freeze]. This means that they are
 * immutable and can be read from any thread.
 *
 * Realm flows always emit items from the thread holding the live RealmList. This means that if you
 * need to do further processing, it is recommended to collect the values on a computation
 * dispatcher:
 *
 * ```
 * list.toFlow()
 *   .map { list -> doExpensiveWork(list) }
 *   .flowOn(Dispatchers.IO)
 *   .onEach { flowList ->
 *     // ...
 *   }.launchIn(Dispatchers.Main)
 * ```
 *
 * If your would like `toFlow()` to stop emitting items you can instruct the flow to only emit the
 * first item by calling [kotlinx.coroutines.flow.first]:
 * ```
 * val foo = list.toFlow()
 *   .flowOn(context)
 *   .first()
 * ```
 *
 * @return Kotlin [Flow] on which calls to `onEach` or `collect` can be made.
 */
@Beta
fun <T> RealmList<T>.toFlow(): Flow<RealmList<T>> {
    @Suppress("INACCESSIBLE_TYPE")
    return when (val realmInstance = baseRealm) {
        is Realm -> realmInstance.configuration.flowFactory.from(realmInstance, this)
        is DynamicRealm -> realmInstance.configuration.flowFactory.from(realmInstance, this)
        else -> throw IllegalStateException("Wrong type of Realm.")
    }
}

/**
 * FIXME
 */
@Beta
fun <T> RealmList<T>.toChangesetFlow(): Flow<CollectionChange<RealmList<T>>> {
    @Suppress("INACCESSIBLE_TYPE")
    return when (val realmInstance = baseRealm) {
        is Realm -> realmInstance.configuration.flowFactory.changesetFrom(realmInstance, this)
        is DynamicRealm -> realmInstance.configuration.flowFactory.changesetFrom(realmInstance, this)
        else -> throw IllegalStateException("Wrong type of Realm.")
    }
}
