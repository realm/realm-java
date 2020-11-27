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

package io.realm.internal.coroutines

import io.realm.*
import io.realm.annotations.Beta
import io.realm.coroutines.FlowFactory
import io.realm.rx.CollectionChange
import io.realm.rx.ObjectChange
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.isActive

/**
 * Internal factory implementation used to conceal Kotlin implementation details from the public
 * API and to avoid having to use Kotlin's documentation solution for just one class.
 */
class InternalFlowFactory(
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
                if (isActive) {
                    if (returnFrozenObjects) {
                        offer(realm.freeze())
                    } else {
                        offer(listenerRealm)
                    }
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
                if (isActive) {
                    if (returnFrozenObjects) {
                        offer(dynamicRealm.freeze())
                    } else {
                        offer(listenerRealm)
                    }
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
                awaitClose {}

                return@callbackFlow
            }

            // Get instance to ensure the Realm is open for as long as we are listening
            val flowRealm = Realm.getInstance(config)
            val listener = RealmChangeListener<RealmResults<T>> { listenerResults ->
                if (isActive) {
                    if (returnFrozenObjects) {
                        offer(listenerResults.freeze())
                    } else {
                        offer(listenerResults)
                    }
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

    override fun <T> changesetFrom(
            realm: Realm,
            results: RealmResults<T>
    ): Flow<CollectionChange<RealmResults<T>>> {
        // Return "as is" if frozen, there will be no listening for changes
        if (realm.isFrozen) {
            return flowOf(CollectionChange(results, null))
        }

        val config = realm.configuration

        return callbackFlow {
            // Do nothing if the results are invalid
            if (!results.isValid) {
                awaitClose {}

                return@callbackFlow
            }

            // Get instance to ensure the Realm is open for as long as we are listening
            val flowRealm = Realm.getInstance(config)
            val listener = OrderedRealmCollectionChangeListener<RealmResults<T>> { listenerResults, changeSet ->
                if (isActive) {
                    if (returnFrozenObjects) {
                        offer(CollectionChange(listenerResults.freeze(), changeSet))
                    } else {
                        offer(CollectionChange(listenerResults, changeSet))
                    }
                }
            }

            results.addChangeListener(listener)

            // Emit current value
            if (returnFrozenObjects) {
                offer(CollectionChange(results.freeze(), null))
            } else {
                offer(CollectionChange(results, null))
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

    override fun <T> from(
            dynamicRealm: DynamicRealm,
            results: RealmResults<T>
    ): Flow<RealmResults<T>> {
        // Return "as is" if frozen, there will be no listening for changes
        if (dynamicRealm.isFrozen) {
            return flowOf(results)
        }

        val config = dynamicRealm.configuration

        return callbackFlow {
            // Do nothing if the results are invalid
            if (!results.isValid) {
                awaitClose {}

                return@callbackFlow
            }

            // Get instance to ensure the Realm is open for as long as we are listening
            val flowRealm = DynamicRealm.getInstance(config)
            val listener = RealmChangeListener<RealmResults<T>> { listenerResults ->
                if (isActive) {
                    if (returnFrozenObjects) {
                        offer(listenerResults.freeze())
                    } else {
                        offer(listenerResults)
                    }
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

    override fun <T> changesetFrom(
            dynamicRealm: DynamicRealm,
            results: RealmResults<T>
    ): Flow<CollectionChange<RealmResults<T>>> {
        // Return "as is" if frozen, there will be no listening for changes
        if (dynamicRealm.isFrozen) {
            return flowOf(CollectionChange(results, null))
        }

        val config = dynamicRealm.configuration

        return callbackFlow {
            // Do nothing if the results are invalid
            if (!results.isValid) {
                awaitClose {}

                return@callbackFlow
            }

            // Get instance to ensure the Realm is open for as long as we are listening
            val flowRealm = DynamicRealm.getInstance(config)
            val listener = OrderedRealmCollectionChangeListener<RealmResults<T>> { listenerResults, changeSet ->
                if (isActive) {
                    if (returnFrozenObjects) {
                        offer(CollectionChange(listenerResults.freeze(), changeSet))
                    } else {
                        offer(CollectionChange(listenerResults, changeSet))
                    }
                }
            }

            results.addChangeListener(listener)

            // Emit current value
            if (returnFrozenObjects) {
                offer(CollectionChange(results.freeze(), null))
            } else {
                offer(CollectionChange(results, null))
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
                awaitClose {}

                return@callbackFlow
            }

            // Get instance to ensure the Realm is open for as long as we are listening
            val flowRealm = Realm.getInstance(config)
            val listener = RealmChangeListener<RealmList<T>> { listenerResults ->
                if (isActive) {
                    if (returnFrozenObjects) {
                        offer(listenerResults.freeze())
                    } else {
                        offer(listenerResults)
                    }
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

    override fun <T : Any?> changesetFrom(
            realm: Realm,
            list: RealmList<T>
    ): Flow<CollectionChange<RealmList<T>>> {
        // Return "as is" if frozen, there will be no listening for changes
        if (realm.isFrozen) {
            return flowOf(CollectionChange(list, null))
        }

        val config = realm.configuration

        return callbackFlow {
            // Do nothing if the results are invalid
            if (!list.isValid) {
                awaitClose {}

                return@callbackFlow
            }

            // Get instance to ensure the Realm is open for as long as we are listening
            val flowRealm = Realm.getInstance(config)
            val listener = OrderedRealmCollectionChangeListener<RealmList<T>> { listenerList, changeSet ->
                if (isActive) {
                    if (returnFrozenObjects) {
                        offer(CollectionChange(listenerList.freeze(), changeSet))
                    } else {
                        offer(CollectionChange(listenerList, changeSet))
                    }
                }
            }

            list.addChangeListener(listener)

            // Emit current value
            if (returnFrozenObjects) {
                offer(CollectionChange(list.freeze(), null))
            } else {
                offer(CollectionChange(list, null))
            }

            awaitClose {
                // Remove listener and cleanup
                if (!flowRealm.isClosed) {
                    list.removeChangeListener(listener)
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
                awaitClose {}

                return@callbackFlow
            }

            // Get instance to ensure the Realm is open for as long as we are listening
            val flowRealm = DynamicRealm.getInstance(config)
            val listener = RealmChangeListener<RealmList<T>> { listenerResults ->
                if (isActive) {
                    if (returnFrozenObjects) {
                        offer(listenerResults.freeze())
                    } else {
                        offer(listenerResults)
                    }
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

    override fun <T> changesetFrom(
            dynamicRealm: DynamicRealm,
            list: RealmList<T>
    ): Flow<CollectionChange<RealmList<T>>> {
        // Return "as is" if frozen, there will be no listening for changes
        if (dynamicRealm.isFrozen) {
            return flowOf(CollectionChange(list, null))
        }

        val config = dynamicRealm.configuration

        return callbackFlow {
            // Do nothing if the results are invalid
            if (!list.isValid) {
                awaitClose {}

                return@callbackFlow
            }

            // Get instance to ensure the Realm is open for as long as we are listening
            val flowRealm = DynamicRealm.getInstance(config)
            val listener = OrderedRealmCollectionChangeListener<RealmList<T>> { listenerList, changeSet ->
                if (isActive) {
                    if (returnFrozenObjects) {
                        offer(CollectionChange(listenerList.freeze(), changeSet))
                    } else {
                        offer(CollectionChange(listenerList, changeSet))
                    }
                }
            }

            list.addChangeListener(listener)

            // Emit current value
            if (returnFrozenObjects) {
                offer(CollectionChange(list.freeze(), null))
            } else {
                offer(CollectionChange(list, null))
            }

            awaitClose {
                // Remove listener and cleanup
                if (!flowRealm.isClosed) {
                    list.removeChangeListener(listener)
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
            // Check if the Realm is closed (instead of using isValid - findFirstAsync always return "invalid object" right away, which would render this logic useless
            if (realm.isClosed) {
                awaitClose {}

                return@callbackFlow
            }

            // Get instance to ensure the Realm is open for as long as we are listening
            val flowRealm = Realm.getInstance(config)
            val listener = RealmChangeListener<T> { listenerObj ->
                if (isActive) {
                    if (returnFrozenObjects) {
                        offer(RealmObject.freeze(listenerObj) as T)
                    } else {
                        offer(listenerObj)
                    }
                }
            }

            RealmObject.addChangeListener(realmObject, listener)

            // Emit current value
            if (RealmObject.isLoaded(realmObject)) {
                if (returnFrozenObjects) {
                    offer(RealmObject.freeze(realmObject))
                } else {
                    offer(realmObject)
                }
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

    override fun <T : RealmModel> changesetFrom(
            realm: Realm,
            realmObject: T
    ): Flow<ObjectChange<T>> {
        // Return "as is" if frozen, there will be no listening for changes
        if (realm.isFrozen) {
            return flowOf(ObjectChange(realmObject, null))
        }

        val config = realm.configuration

        return callbackFlow {
            // Check if the Realm is closed (instead of using isValid - findFirstAsync always return "invalid object" right away, which would render this logic useless
            if (realm.isClosed) {
                awaitClose {}

                return@callbackFlow
            }

            // Get instance to ensure the Realm is open for as long as we are listening
            val flowRealm = Realm.getInstance(config)
            val listener = RealmObjectChangeListener<T> { listenerObject, changeSet ->
                if (isActive) {
                    if (returnFrozenObjects) {
                        offer(ObjectChange(RealmObject.freeze(listenerObject), changeSet))
                    } else {
                        offer(ObjectChange(listenerObject, changeSet))
                    }
                }
            }

            RealmObject.addChangeListener(realmObject, listener)

            // Emit current value
            if (RealmObject.isLoaded(realmObject)) {
                if (returnFrozenObjects) {
                    offer(ObjectChange(RealmObject.freeze(realmObject), null))
                } else {
                    offer(ObjectChange(realmObject, null))
                }
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
    override fun from(
            dynamicRealm: DynamicRealm,
            dynamicRealmObject: DynamicRealmObject
    ): Flow<DynamicRealmObject> {
        // Return "as is" if frozen, there will be no listening for changes
        if (dynamicRealm.isFrozen) {
            return flowOf(dynamicRealmObject)
        }

        val config = dynamicRealm.configuration

        return callbackFlow<DynamicRealmObject> {
            // Check if the Realm is closed (instead of using isValid - findFirstAsync always return "invalid object" right away, which would render this logic useless
            if (dynamicRealm.isClosed) {
                awaitClose {}

                return@callbackFlow
            }

            // Get instance to ensure the Realm is open for as long as we are listening
            val flowRealm = DynamicRealm.getInstance(config)
            val listener = RealmChangeListener<DynamicRealmObject> { listenerObj ->
                if (isActive) {
                    if (returnFrozenObjects) {
                        offer(listenerObj.freeze())
                    } else {
                        offer(listenerObj)
                    }
                }
            }

            dynamicRealmObject.addChangeListener(listener)

            // Emit current value
            if (RealmObject.isLoaded(dynamicRealmObject)) {
                if (returnFrozenObjects) {
                    offer(RealmObject.freeze(dynamicRealmObject))
                } else {
                    offer(dynamicRealmObject)
                }
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

    override fun changesetFrom(
            dynamicRealm: DynamicRealm,
            dynamicRealmObject: DynamicRealmObject
    ): Flow<ObjectChange<DynamicRealmObject>> {
        // Return "as is" if frozen, there will be no listening for changes
        if (dynamicRealm.isFrozen) {
            return flowOf(ObjectChange(dynamicRealmObject, null))
        }

        val config = dynamicRealm.configuration

        return callbackFlow {
            // Do nothing if the results are invalid
            if (!RealmObject.isValid(dynamicRealmObject)) {
                awaitClose {}

                return@callbackFlow
            }

            // Get instance to ensure the Realm is open for as long as we are listening
            val flowRealm = Realm.getInstance(config)
            val listener = RealmObjectChangeListener<DynamicRealmObject> { listenerObject, changeSet ->
                if (isActive) {
                    if (returnFrozenObjects) {
                        offer(ObjectChange(RealmObject.freeze(listenerObject), changeSet))
                    } else {
                        offer(ObjectChange(listenerObject, changeSet))
                    }
                }
            }

            RealmObject.addChangeListener(dynamicRealmObject, listener)

            // Emit current value
            if (RealmObject.isLoaded(dynamicRealmObject)) {
                if (returnFrozenObjects) {
                    offer(ObjectChange(RealmObject.freeze(dynamicRealmObject), null))
                } else {
                    offer(ObjectChange(dynamicRealmObject, null))
                }
            }

            awaitClose {
                // Remove listener and cleanup
                if (!flowRealm.isClosed) {
                    RealmObject.removeChangeListener(dynamicRealmObject, listener)
                    flowRealm.close()
                }
            }
        }
    }
}
