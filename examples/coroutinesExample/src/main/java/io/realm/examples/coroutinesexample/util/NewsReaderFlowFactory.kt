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

package io.realm.examples.coroutinesexample.util

import io.realm.DynamicRealm
import io.realm.Realm
import io.realm.RealmResults
import io.realm.coroutines.RealmFlowFactory
import io.realm.rx.CollectionChange
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.drop

/**
 * Similar to [io.realm.coroutines.RealmFlowFactory] but it will not emit the current value
 * immediately. This is needed by Store to function properly or else it will receive updates with
 * empty [RealmResults] that will make it think existing values for the current key are present.
 *
 * There is no need to override the methods for [io.realm.RealmModel] since the internal factory
 * does check whether or not an object is loaded before the first emission.
 */
class NewsReaderFlowFactory : RealmFlowFactory(true) {

    override fun <T> from(
            realm: Realm,
            results: RealmResults<T>
    ): Flow<RealmResults<T>> =
            super.from(realm, results)
                    .drop(1)

    override fun <T> from(
            dynamicRealm: DynamicRealm,
            results: RealmResults<T>
    ): Flow<RealmResults<T>> =
            super.from(dynamicRealm, results)
                    .drop(1)

    override fun <T> changesetFrom(
            realm: Realm,
            results: RealmResults<T>
    ): Flow<CollectionChange<RealmResults<T>>> =
            super.changesetFrom(realm, results)
                    .drop(1)

    override fun <T> changesetFrom(
            dynamicRealm: DynamicRealm,
            results: RealmResults<T>
    ): Flow<CollectionChange<RealmResults<T>>> =
            super.changesetFrom(dynamicRealm, results)
                    .drop(1)
}
