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

package io.realm.examples.objectserver.advanced.ui.shared

import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.disposables.Disposables
import io.realm.Realm
import io.realm.RealmConfiguration

// Creates an Observable that wraps the Realm lifecycle
// The Realm will be opened when subscribed to and closed when the stream is disposed.
//
// It isn't possible to add static extension functions to Java classes: https://youtrack.jetbrains.com/issue/KT-11968
// So this is a free function for now.
fun createObservableForRealm(config: RealmConfiguration): Flowable<Realm> {
    return Flowable.create({ emitter ->
        val realm = Realm.getInstance(config)
        emitter.setDisposable(Disposables.fromRunnable {
            realm.close()
        })
        emitter.onNext(realm)
    }, BackpressureStrategy.LATEST)
}

