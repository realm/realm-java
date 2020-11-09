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
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf

/**
 * FIXME
 */
class RealmCoroutinesFactory : CoroutinesFactory {

    override fun <T : RealmModel> from(realm: Realm, results: RealmResults<T>): Flow<RealmResults<T>> {
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
                offer(listenerResults.freeze())
            }

            results.addChangeListener(listener)

            // Emit current (frozen) value
            offer(results.freeze())

            awaitClose {
                // Remove listener and cleanup
                if (!flowRealm.isClosed) {
                    results.removeChangeListener(listener)
                    flowRealm.close()
                }
            }
        }
    }

    override fun <T : RealmObject> from(realm: Realm, realmList: RealmList<T>): Flow<RealmList<T>> {
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
                offer(listenerResults.freeze())
            }

            realmList.addChangeListener(listener)

            // Emit current (frozen) value
            offer(realmList.freeze())

            awaitClose {
                // Remove listener and cleanup
                if (!flowRealm.isClosed) {
                    realmList.removeChangeListener(listener)
                    flowRealm.close()
                }
            }
        }
    }

    override fun <T : RealmModel> from(realm: Realm, realmModel: T): Flow<T> {
        // Return "as is" if frozen, there will be no listening for changes
        if (realm.isFrozen) {
            return flowOf(realmModel)
        }

        val config = realm.configuration

        return callbackFlow<T> {
            // Do nothing if the object is invalid
            if (!RealmObject.isValid(realmModel)) {
                return@callbackFlow
            }

            // Get instance to ensure the Realm is open for as long as we are listening
            val flowRealm = Realm.getInstance(config)
            val listener = RealmChangeListener<T> { listenerObj ->
                offer(RealmObject.freeze(listenerObj) as T)
            }

            RealmObject.addChangeListener(realmModel, listener)

            // Emit current (frozen) value
            offer(RealmObject.freeze(realmModel))

            awaitClose {
                // Remove listener and cleanup
                if (!flowRealm.isClosed) {
                    RealmObject.removeChangeListener(realmModel, listener)
                    flowRealm.close()
                }
            }
        }
    }

    override fun from(dynamicRealm: DynamicRealm, dynamicObject: DynamicRealmObject): Flow<DynamicRealmObject> {
        // Return "as is" if frozen, there will be no listening for changes
        if (dynamicRealm.isFrozen) {
            return flowOf(dynamicObject)
        }

        val config = dynamicRealm.configuration

        return callbackFlow<DynamicRealmObject> {
            // Do nothing if the object is invalid
            if (!dynamicObject.isValid) {
                return@callbackFlow
            }

            // Get instance to ensure the Realm is open for as long as we are listening
            val flowRealm = Realm.getInstance(config)
            val listener = RealmChangeListener<DynamicRealmObject> { listenerObj ->
                offer(listenerObj.freeze())
            }

            dynamicObject.addChangeListener(listener)

            // Emit current (frozen) value
            offer(RealmObject.freeze(dynamicObject))

            awaitClose {
                // Remove listener and cleanup
                if (!flowRealm.isClosed) {
                    dynamicObject.removeChangeListener(listener)
                    flowRealm.close()
                }
            }
        }
    }
}
