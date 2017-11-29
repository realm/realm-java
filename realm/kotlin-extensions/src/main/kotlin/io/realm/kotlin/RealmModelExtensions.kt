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

import io.realm.RealmChangeListener
import io.realm.RealmModel
import io.realm.RealmObject
import io.realm.RealmObjectChangeListener

/**
 * Deletes the object from the Realm it is currently associated with.
 *
 * After this method is called the object will be invalid and any operation (read or write) performed on it will
 * fail with an `IllegalStateException`.
 *
 * @throws IllegalStateException if the corresponding Realm is closed or in an incorrect thread.
 * @see [isValid]
 */
fun RealmModel.deleteFromRealm() {
    RealmObject.deleteFromRealm(this)
}

/**
 * Checks if the RealmObject is still valid to use i.e., the RealmObject hasn't been deleted nor has the
 * Realm been closed. It will always return `true` for unmanaged objects.
 *
 * @return `true` if the object is still accessible or an unmanaged object, `false` otherwise.
 */
fun RealmModel.isValid(): Boolean {
    return RealmObject.isValid(this)
}

/**
 * Checks if this object is managed by Realm. A managed object is just a wrapper around the data in the underlying
 * Realm file. On Looper threads, a managed object will be live-updated so it always points to the latest data. It
 * is possible to register a change listener using [addChangeListener] to be
 * notified when changes happen. Managed objects are thread confined so that they cannot be accessed from other threads
 * than the one that created them.
 *
 * If this method returns `false`, the object is unmanaged. An unmanaged object is just a normal Kotlin object,
 * so it can be passed freely across threads, but the data in the object is not connected to the underlying Realm,
 * so it will not be live updated.
 *
 * It is possible to create a managed object from an unmanaged object by using
 * [io.realm.Realm.copyToRealm]. An unmanaged object can be created from a managed object by using
 * [io.realm.Realm.copyFromRealm].
 *
 * @return `true` if the object is managed, `false` if it is unmanaged.
 */
fun RealmModel.isManaged(): Boolean {
    return RealmObject.isManaged(this)
}

/**
 * Checks if the query used to find this RealmObject has completed.
 *
 * Async methods like [io.realm.RealmQuery.findFirstAsync] return an RealmObject that represents the future result
 * of the RealmQuery. It can be considered similar to a [java.util.concurrent.Future] in this regard.
 *
 * Once `isLoaded()` returns `true`, the object represents the query result even if the query
 * didn't find any object matching the query parameters. In this case the RealmObject will
 * become a `null` object.
 *
 * "Null" objects represents `null`.  An exception is thrown if any accessor is called, so it is important to also
 * check isValid before calling any methods. A common pattern is:
 *
 *
 * ```kotlin
 * val person = realm.where<Person>().findFirstAsync()
 * person.isLoaded() // == false
 * person.addChangeListener { p ->
 *     p.isLoaded() // always true here
 *     if(p.isValid()) {
 *         // It is safe to access this person.
 *     }
 * }
 * ```
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

/**
 * Makes an asynchronous query blocking. This will also trigger any registered listeners.
 *
 * Note: This will return `true` if called for an unmanaged object (created outside of Realm).
 *
 * @return `true` if it successfully completed the query, `false` otherwise.
 */
fun RealmModel.load(): Boolean {
    return RealmObject.load(this)
}


/**
 * Adds a change listener to a RealmObject that will be triggered if any value field or referenced RealmObject field
 * is changed, or the RealmList field itself is changed.

 * Registering a change listener will not prevent the underlying RealmObject from being garbage collected.
 * If the RealmObject is garbage collected, the change listener will stop being triggered. To avoid this, keep a
 * strong reference for as long as appropriate e.g. in a class variable.
 *
 * ```kotlin
 * class MyActivity : Activity {
 *
 *     private var person: Person?
 *
 *     override fun onCreate(savedInstanceState: Bundle?) {
 *         super.onCreate(savedInstanceState)
 *         person = realm.where<Person>().findFirst()
 *         person?.addChangeListener(RealmChangeListener { person ->
 *             // React to change
 *         })
 *     }
 * }
 * ```
 *
 * @param listener the change listener to be notified.
 * @throws IllegalArgumentException if the `object` is `null` or an unmanaged object, or the change
 * listener is `null`.
 * @throws IllegalStateException if you try to add a listener from a non-Looper or IntentService thread.
 * @throws IllegalStateException if you try to add a listener inside a transaction.
 */
fun <E : RealmModel> E.addChangeListener(listener: RealmChangeListener<E>) {
    RealmObject.addChangeListener(this, listener)
}

/**
 * Adds a change listener to a RealmObject to get detailed information about the changes. The listener will be
 * triggered if any value field or referenced RealmObject field is changed, or the RealmList field itself is
 * changed.

 * Registering a change listener will not prevent the underlying RealmObject from being garbage collected.
 * If the RealmObject is garbage collected, the change listener will stop being triggered. To avoid this, keep a
 * strong reference for as long as appropriate e.g. in a class variable.
 *
 * ```kotlin
 * class MyActivity : Activity {
 *
 *     private var person: Person?
 *
 *     override fun onCreate(savedInstanceState: Bundle?) {
 *         super.onCreate(savedInstanceState)
 *         person = realm.where<Person>().findFirst()
 *         person?.addChangeListener(RealmObjectChangeListener { person, changeSet ->
 *             // React to change
 *         })
 *     }
 * }
 * ```
 *
 * @param listener the change listener to be notified.
 * @throws IllegalArgumentException if the `object` is `null` or an unmanaged object, or the change
 * listener is `null`.
 * @throws IllegalStateException if you try to add a listener from a non-Looper or IntentService thread.
 * @throws IllegalStateException if you try to add a listener inside a transaction.
 */
fun <E : RealmModel> E.addChangeListener(listener: RealmObjectChangeListener<E>) {
    RealmObject.addChangeListener(this, listener)
}

/**
 * Removes a previously registered listener on the given RealmObject.
 *
 * @param listener the instance to be removed.
 * @throws IllegalArgumentException if the `object` or the change listener is `null`.
 * @throws IllegalArgumentException if object is an unmanaged RealmObject.
 * @throws IllegalStateException if you try to remove a listener from a non-Looper Thread.
 */
fun  <E : RealmModel> E.removeChangeListener(listener: RealmChangeListener<E>) {
    RealmObject.removeChangeListener(this, listener)
}

/**
 * Removes a previously registered listener on the given RealmObject.
 *
 * @param listener the instance to be removed.
 * @throws IllegalArgumentException if the `object` or the change listener is `null`.
 * @throws IllegalArgumentException if object is an unmanaged RealmObject.
 * @throws IllegalStateException if you try to remove a listener from a non-Looper Thread.
 */
fun  <E : RealmModel> E.removeChangeListener(listener: RealmObjectChangeListener<E>) {
    RealmObject.removeChangeListener(this, listener)
}

/**
 * Removes all registered listeners from the given RealmObject.
 *
 * @throws IllegalArgumentException if object is `null` or isn't managed by Realm.
 */
fun RealmModel.removeAllChangeListeners() {
    return RealmObject.removeAllChangeListeners(this)
}

