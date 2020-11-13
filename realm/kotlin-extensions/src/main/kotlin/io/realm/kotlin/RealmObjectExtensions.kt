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
import io.realm.internal.RealmObjectProxy
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Returns a [Flow] that monitors changes to this RealmObject. It will emit the current
 * RealmObject when subscribed to. Object updates will continually be emitted as the RealmObject is
 * updated - `onCompletion` will never be called.
 *
 * Items emitted from Realm flows are frozen - see [RealmObject.freeze]. This means that they are
 * immutable and can be read from any thread.
 *
 * Realm flows always emit items from the thread holding the live RealmObject. This means that if
 * you need to do further processing, it is recommended to collect the values on a computation
 * dispatcher:
 *
 * ```
 * object.toFlow()
 *   .map { obj -> doExpensiveWork(obj) }
 *   .flowOn(Dispatchers.IO)
 *   .onEach { flowObject ->
 *     // ...
 *   }.launchIn(Dispatchers.Main)
 * ```
 *
 * If your would like `toFlow()` to stop emitting items you can instruct the flow to only emit the
 * first item by calling [kotlinx.coroutines.flow.first]:
 * ```
 * val foo = object.toFlow()
 *   .flowOn(context)
 *   .first()
 * ```
 *
 * @return Kotlin [Flow] on which calls to `onEach` or `collect` can be made.
 */
@Beta
fun <T : RealmModel> T?.toFlow(): Flow<T?> {
    // Return flow with object or null flow if this function is called on null
    return this?.let { obj ->
        if (obj is RealmObjectProxy) {
            val proxy = obj as RealmObjectProxy

            @Suppress("INACCESSIBLE_TYPE")
            when (val realm = proxy.`realmGet$proxyState`().`realm$realm`) {
                is Realm -> realm.configuration.flowFactory.from<T>(realm, obj)
                is DynamicRealm -> (obj as DynamicRealmObject).let { dynamicRealmObject ->
                    realm.configuration.flowFactory.from(realm, dynamicRealmObject) as Flow<T?>
                }
                else -> throw UnsupportedOperationException("${realm.javaClass} is not supported as a candidate for 'toFlow'. Only subclasses of RealmModel/RealmObject can be used.")
            }
        } else {
            // Return a one-time emission in case the object is unmanaged
            return flowOf(this)
        }
    } ?: flowOf(null)
}
