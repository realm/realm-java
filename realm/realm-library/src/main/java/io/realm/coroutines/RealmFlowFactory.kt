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

package io.realm.coroutines

import io.realm.*
import io.realm.annotations.Beta
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf

/**
 * Factory class used to create coroutine [Flow]s.
 *
 * This class is used by default unless overridden in [RealmConfiguration.Builder.flowFactory].
 */
@Beta
class RealmFlowFactory(
        private val returnFrozenObjects: Boolean = true
) : FlowFactory {

    @Beta
    override fun from(realm: Realm): Flow<Realm> {
        if (realm.isFrozen) {
            return flowOf(realm)
        }

        return callbackFlow {
            val flowRealm = Realm.getInstance(realm.configuration)
            val listener = RealmChangeListener<Realm> { listenerRealm ->
                if (returnFrozenObjects) {
                    offer(realm.freeze())
                } else {
                    offer(listenerRealm)
                }
            }

            flowRealm.addChangeListener(listener)

            if (returnFrozenObjects) {
                offer(flowRealm.freeze())
            } else {
                offer(flowRealm)
            }

            awaitClose {
                flowRealm.removeChangeListener(listener)
                flowRealm.close()
            }
        }
    }

    @Beta
    override fun from(dynamicRealm: DynamicRealm): Flow<DynamicRealm> {
        if (dynamicRealm.isFrozen) {
            return flowOf(dynamicRealm)
        }

        return callbackFlow {
            val flowRealm = DynamicRealm.getInstance(dynamicRealm.configuration)
            val listener = RealmChangeListener<DynamicRealm> { listenerRealm ->
                if (returnFrozenObjects) {
                    offer(dynamicRealm.freeze())
                } else {
                    offer(listenerRealm)
                }
            }

            flowRealm.addChangeListener(listener)

            if (returnFrozenObjects) {
                offer(flowRealm.freeze())
            } else {
                offer(flowRealm)
            }

            awaitClose {
                flowRealm.removeChangeListener(listener)
                flowRealm.close()
            }
        }
    }

    @Beta
    override fun <T> from(realm: Realm, results: RealmResults<T>): Flow<RealmResults<T>> {
        // Return "as is" if frozen, there will be no listening for changes
        if (realm.isFrozen) {
            return flowOf(results)
        }

        val config = realm.configuration

        return callbackFlow {
            // Do nothing if the results are invalid
            if (!results.isValid) {
                return@callbackFlow
            }

            // Get instance to ensure the Realm is open for as long as we are listening
            val flowRealm = Realm.getInstance(config)
            val listener = RealmChangeListener<RealmResults<T>> { listenerResults ->
                if (returnFrozenObjects) {
                    offer(listenerResults.freeze())
                } else {
                    offer(listenerResults)
                }
            }

            results.addChangeListener(listener)

            // Emit current value
            if (returnFrozenObjects) {
                offer(results.freeze())
            } else {
                offer(results)
            }

            awaitClose {
                // Remove listener and cleanup
                if (!flowRealm.isClosed) {
                    results.removeChangeListener(listener)
                    flowRealm.close()
                }
            }
        }
    }

    override fun <T> from(dynamicRealm: DynamicRealm, results: RealmResults<T>): Flow<RealmResults<T>> {
        // Return "as is" if frozen, there will be no listening for changes
        if (dynamicRealm.isFrozen) {
            return flowOf(results)
        }

        val config = dynamicRealm.configuration

        return callbackFlow {
            // Do nothing if the results are invalid
            if (!results.isValid) {
                return@callbackFlow
            }

            // Get instance to ensure the Realm is open for as long as we are listening
            val flowRealm = DynamicRealm.getInstance(config)
            val listener = RealmChangeListener<RealmResults<T>> { listenerResults ->
                if (returnFrozenObjects) {
                    offer(listenerResults.freeze())
                } else {
                    offer(listenerResults)
                }
            }

            results.addChangeListener(listener)

            // Emit current value
            if (returnFrozenObjects) {
                offer(results.freeze())
            } else {
                offer(results)
            }

            awaitClose {
                // Remove listener and cleanup
                if (!flowRealm.isClosed) {
                    results.removeChangeListener(listener)
                    flowRealm.close()
                }
            }
        }
    }

    @Beta
    override fun <T> from(realm: Realm, realmList: RealmList<T>): Flow<RealmList<T>> {
        // Return "as is" if frozen, there will be no listening for changes
        if (realm.isFrozen) {
            return flowOf(realmList)
        }

        val config = realm.configuration

        return callbackFlow {
            // Do nothing if the results are invalid
            if (!realmList.isValid) {
                return@callbackFlow
            }

            // Get instance to ensure the Realm is open for as long as we are listening
            val flowRealm = Realm.getInstance(config)
            val listener = RealmChangeListener<RealmList<T>> { listenerResults ->
                if (returnFrozenObjects) {
                    offer(listenerResults.freeze())
                } else {
                    offer(listenerResults)
                }
            }

            realmList.addChangeListener(listener)

            // Emit current value
            if (returnFrozenObjects) {
                offer(realmList.freeze())
            } else {
                offer(realmList)
            }

            awaitClose {
                // Remove listener and cleanup
                if (!flowRealm.isClosed) {
                    realmList.removeChangeListener(listener)
                    flowRealm.close()
                }
            }
        }
    }

    override fun <T> from(dynamicRealm: DynamicRealm, realmList: RealmList<T>): Flow<RealmList<T>> {
        // Return "as is" if frozen, there will be no listening for changes
        if (dynamicRealm.isFrozen) {
            return flowOf(realmList)
        }

        val config = dynamicRealm.configuration

        return callbackFlow {
            // Do nothing if the results are invalid
            if (!realmList.isValid) {
                return@callbackFlow
            }

            // Get instance to ensure the Realm is open for as long as we are listening
            val flowRealm = DynamicRealm.getInstance(config)
            val listener = RealmChangeListener<RealmList<T>> { listenerResults ->
                if (returnFrozenObjects) {
                    offer(listenerResults.freeze())
                } else {
                    offer(listenerResults)
                }
            }

            realmList.addChangeListener(listener)

            // Emit current value
            if (returnFrozenObjects) {
                offer(realmList.freeze())
            } else {
                offer(realmList)
            }

            awaitClose {
                // Remove listener and cleanup
                if (!flowRealm.isClosed) {
                    realmList.removeChangeListener(listener)
                    flowRealm.close()
                }
            }
        }
    }

    @Beta
    override fun <T : RealmModel> from(realm: Realm, realmObject: T): Flow<T> {
        // Return "as is" if frozen, there will be no listening for changes
        if (realm.isFrozen) {
            return flowOf(realmObject)
        }

        val config = realm.configuration

        return callbackFlow<T> {
            // Do nothing if the object is invalid
            if (!RealmObject.isValid(realmObject)) {
                return@callbackFlow
            }

            // Get instance to ensure the Realm is open for as long as we are listening
            val flowRealm = Realm.getInstance(config)
            val listener = RealmChangeListener<T> { listenerObj ->
                if (returnFrozenObjects) {
                    offer(RealmObject.freeze(listenerObj) as T)
                } else {
                    offer(listenerObj)
                }
            }

            RealmObject.addChangeListener(realmObject, listener)

            // Emit current value
            if (returnFrozenObjects) {
                offer(RealmObject.freeze(realmObject))
            } else {
                offer(realmObject)
            }

            awaitClose {
                // Remove listener and cleanup
                if (!flowRealm.isClosed) {
                    RealmObject.removeChangeListener(realmObject, listener)
                    flowRealm.close()
                }
            }
        }
    }

    @Beta
    override fun from(dynamicRealm: DynamicRealm, dynamicRealmObject: DynamicRealmObject): Flow<DynamicRealmObject> {
        // Return "as is" if frozen, there will be no listening for changes
        if (dynamicRealm.isFrozen) {
            return flowOf(dynamicRealmObject)
        }

        val config = dynamicRealm.configuration

        return callbackFlow<DynamicRealmObject> {
            // Do nothing if the object is invalid
            if (!dynamicRealmObject.isValid) {
                return@callbackFlow
            }

            // Get instance to ensure the Realm is open for as long as we are listening
            val flowRealm = DynamicRealm.getInstance(config)
            val listener = RealmChangeListener<DynamicRealmObject> { listenerObj ->
                if (returnFrozenObjects) {
                    offer(listenerObj.freeze())
                } else {
                    offer(listenerObj)
                }
            }

            dynamicRealmObject.addChangeListener(listener)

            // Emit current value
            if (returnFrozenObjects) {
                offer(RealmObject.freeze(dynamicRealmObject))
            } else {
                offer(dynamicRealmObject)
            }

            awaitClose {
                // Remove listener and cleanup
                if (!flowRealm.isClosed) {
                    dynamicRealmObject.removeChangeListener(listener)
                    flowRealm.close()
                }
            }
        }
    }
}
