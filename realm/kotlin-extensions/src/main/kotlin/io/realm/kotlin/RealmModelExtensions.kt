/*
 * Copyright 2017 Realm Inc.
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
import java.util.*
import java.util.concurrent.atomic.AtomicReference
import kotlin.reflect.KMutableProperty1

//
// Add default implementations for all methods in the RealmModel interface, which means that Kotlin
// codebases can freely choose between extending RealmObject or implementing RealmModel as the code
// written when accessing the object will be the same.
//

/**
 * Deletes the object from the Realm it is currently associated to.
 *
 * After this method is called the object will be invalid and any operation (read or write) performed on it will
 * fail with an IllegalStateException.
 *
 * @throws IllegalStateException if the corresponding Realm is closed or in an incorrect thread.
 * @see [RealmModel.isValid]
 */
fun RealmModel.deleteFromRealm() {
    RealmObject.deleteFromRealm(this)
}


/**
 * Checks if the RealmObject is still valid to use i.e., the RealmObject hasn't been deleted nor has the
 * {@link io.realm.Realm} been closed. It will always return {@code true} for unmanaged objects.
 *
 * Note that this can be used to check the validity of certain conditions such as being {@code null}
 * when observed.
 * ```
 * realm.where<BannerRealm>().equalTo("type", type).findFirstAsync().asFlowable()
 *      .filter(result.isLoaded() && result.isValid())
 *      .first()
 * }
 * ```
 *
 * @return `true` if the object is still accessible or an unmanaged object, `false` otherwise.
 * @see <a href="https://github.com/realm/realm-java/tree/master/examples/rxJavaExample">Examples using Realm with RxJava</a>
 */
fun RealmModel.isValid(): Boolean {
    return RealmObject.isValid(this)
}

/**
 * Checks if this object is managed by Realm. A managed object is just a wrapper around the data in the underlying
 * Realm file. On Looper threads, a managed object will be live-updated so it always points to the latest data. It
 * is possible to register a change listener using [addChangeListener] to be notified when changes happen. Managed
 * objects are thread confined so that they cannot be accessed from other threads than the one that created them.
 *
 * If this method returns `false`, the object is unmanaged. An unmanaged object is just a normal Java object,
 * so it can be parsed freely across threads, but the data in the object is not connected to the underlying Realm,
 * so it will not be live updated.
 *
 * It is possible to create a managed object from an unmanaged object by using [Realm.copyToRealm]. An unmanaged
 * object can be created from a managed object by using [Realm.copyFromRealm].
 *
 * @return {@code true} if the object is managed, {@code false} if it is unmanaged.
 */
fun RealmModel.isManaged(): Boolean {
    return RealmObject.isManaged(this)
}


/**
 * Checks if the query used to find this RealmObject has completed.
 *
 * Async methods like [RealmQuery.findFirstAsync] return an [RealmObject] that represents the future
 * result of the [RealmQuery]. It can be considered similar to a [java.util.concurrent.Future] in this
 * regard.
 *
 * Once `isLoaded()` returns `true`, the object represents the query result even if the query
 * didn't find any object matching the query parameters. In this case the [RealmObject] will
 * become a "null" object.
 *
 * "Null" objects represents `null`.  An exception is throw if any accessor is called, so it is important to
 * also check [isValid] before calling any methods. A common pattern is:
 *
 * ```
 * val person = realm.where(Person.class).findFirstAsync()
 * person.isLoaded() // == false
 * person.addChangeListener(RealmChangeListener() {
 *      override void onChange(Person person) {
 *          person.isLoaded() // Always true here
 *          if (person.isValid()) {
 *              // It is safe to access the person.
 *          }
 *      }
 * });
 * }
 * ```
 *
 * Synchronous RealmObjects are by definition blocking hence this method will always return `true` for them.
 * This method will return `true` if called on an unmanaged object (created outside of Realm).
 *
 * @return `true` if the query has completed, `false` if the query is in
 * progress.
 * @see [isValid]
 */
fun RealmModel.isLoaded(): Boolean {
    return RealmObject.isLoaded(this)
}

fun RealmModel.load(): Boolean {
    return RealmObject.load(this)
}

fun <E : RealmModel> E.addChangeListener(listener: RealmChangeListener<E>) {
    RealmObject.addChangeListener(this, listener)
}

fun <E : RealmModel> E.addChangeListener(listener: RealmObjectChangeListener<E>) {
    RealmObject.addChangeListener(this, listener)
}

fun  <E : RealmModel> E.removeChangeListener(listener: RealmChangeListener<E>) {
    RealmObject.removeChangeListener(this, listener)
}

fun  <E : RealmModel> E.removeChangeListener(listener: RealmObjectChangeListener<E>) {
    RealmObject.removeChangeListener(this, listener)
}

fun RealmModel.removeAllChangeListeners() {
    return RealmObject.removeAllChangeListeners(this)
}

