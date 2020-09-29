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
import io.realm.RealmObject.freeze
import io.realm.annotations.Beta
import io.realm.internal.RealmObjectProxy
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
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
fun <T : RealmModel> T.toFlow(): Flow<T> {
    val obj = this
    return if (obj is RealmObjectProxy) {
        val proxy = obj as RealmObjectProxy
        val realm = proxy.`realmGet$proxyState`().`realm$realm`

        when (realm) {
            is Realm -> flowFromRealm<T>(realm, obj)
            is DynamicRealm -> {
                val dynamicObject = obj as DynamicRealmObject
                flowFromDynamicRealm(realm, dynamicObject) as Flow<T>
            }
            else -> throw UnsupportedOperationException("${realm.javaClass} does not support RxJava. See https://realm.io/docs/java/latest/#rxjava for more details.")
        }
    } else {
        return flowOf(this)
    }
}

private fun <T : RealmModel> flowFromRealm(realm: Realm, obj: T): Flow<T> {
    // Return "as is" if frozen, there will be no listening for changes
    if (realm.isFrozen) {
        return flowOf(obj)
    }

    val config = realm.configuration

    return callbackFlow<T> {
        // Do nothing if the object is invalid
        if (!obj.isValid()) {
            return@callbackFlow
        }

        // Get instance to ensure the Realm is open for as long as we are listening
        val flowRealm = Realm.getInstance(config)
        val listener = RealmChangeListener<T> { listenerObj ->
            offer(listenerObj.freeze())
        }

        obj.addChangeListener(listener)

        // Emit current (frozen) value
        offer(freeze(obj))

        awaitClose {
            // Remove listener and cleanup
            if (!flowRealm.isClosed) {
                obj.removeChangeListener(listener)
                flowRealm.close()
            }
        }
    }
}

private fun flowFromDynamicRealm(
        dynamicRealm: DynamicRealm,
        dynamicObject: DynamicRealmObject
): Flow<DynamicRealmObject> {
    // Return "as is" if frozen, there will be no listening for changes
    if (dynamicRealm.isFrozen) {
        return flowOf(dynamicObject)
    }

    val config = dynamicRealm.configuration

    return callbackFlow<DynamicRealmObject> {
        // Do nothing if the object is invalid
        if (!dynamicObject.isValid()) {
            return@callbackFlow
        }

        // Get instance to ensure the Realm is open for as long as we are listening
        val flowRealm = Realm.getInstance(config)
        val listener = RealmChangeListener<DynamicRealmObject> { listenerObj ->
            offer(listenerObj.freeze())
        }

        dynamicObject.addChangeListener(listener)

        // Emit current (frozen) value
        offer(freeze(dynamicObject))

        awaitClose {
            // Remove listener and cleanup
            if (!flowRealm.isClosed) {
                dynamicObject.removeChangeListener(listener)
                flowRealm.close()
            }
        }
    }
}
