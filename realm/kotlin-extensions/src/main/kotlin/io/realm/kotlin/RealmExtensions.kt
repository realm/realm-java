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

import io.realm.Realm
import io.realm.RealmModel
import io.realm.RealmQuery
import io.realm.exceptions.RealmException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

/**
 * Returns a typed RealmQuery, which can be used to query for specific objects of this type
 *
 * @param T the class of the object which is to be queried for.
 * @return a typed `RealmQuery`, which can be used to query for specific objects of this type.
 */
inline fun <reified T : RealmModel> Realm.where(): RealmQuery<T> {
    return this.where(T::class.java)
}

/**
 * Deletes all objects of the specified class from the Realm.
 *
 * @param T the class of the object which is to be queried for.
 * @throws IllegalStateException if the corresponding Realm is closed or called from an incorrect thread.
 */
inline fun <reified T : RealmModel> Realm.delete() {
    return this.delete(T::class.java)
}

/**
 *
 * Instantiates and adds a new object to the Realm.
 *
 * This method is only available for model classes with no `@PrimaryKey` annotation.
 * If you like to create an object that has a primary key, use [createObject] instead.
 *
 * @param T the Class of the object to create.
 * @return the new object.
 * @throws RealmException if the primary key is defined in the model class or an object cannot be created.
 */
inline fun <reified T : RealmModel> Realm.createObject(): T {
    return this.createObject(T::class.java)
}

/**
 *
 * Instantiates and adds a new object to the Realm with the primary key value already set.
 *
 * If the value violates the primary key constraint, no object will be added and a RealmException will be
 * thrown. The default value for primary key provided by the model class will be ignored.
 *
 * @param T the Class of the object to create.
 * @param primaryKeyValue value for the primary key field.
 * @return the new object.
 * @throws RealmException if object could not be created due to the primary key being invalid.
 * @throws IllegalStateException if the model class does not have an primary key defined.
 * @throws IllegalArgumentException if the `primaryKeyValue` doesn't have a value that can be converted to the
 * expected value.
 */
inline fun <reified T : RealmModel> Realm.createObject(primaryKeyValue: Any?): T {
    return this.createObject(T::class.java, primaryKeyValue)
}

/**
 * Instantiates and adds a new embedded object to the Realm.
 *
 * This method should only be used to create objects of types marked as embedded.
 *
 * @param T the Class of the object to create. It must be marked with `@RealmClass(embedded = true)`.
 * @param parentObject The parent object which should hold a reference to the embedded object. If the parent property is a list
 * the embedded object will be added to the end of that list.
 * @param parentProperty the property in the parent class which holds the reference.
 * @return the newly created embedded object.
 * @throws IllegalArgumentException if `clazz` is not an embedded class or if the property
 * in the parent class cannot hold objects of the appropriate type.
 */
inline fun <reified T : RealmModel> Realm.createEmbeddedObject(parentObject: RealmModel, parentProperty: String): T {
    return this.createEmbeddedObject(T::class.java, parentObject, parentProperty)
}

/**
 * Suspend version of [Realm.executeTransaction] to use within coroutines.
 *
 * Canceling the scope or job in which this function is executed does not cancel the transaction itself. If you want to ensure
 * your transaction is cooperative, you have to check for the value of [CoroutineScope.isActive] while running the transaction:
 *
 * ```
 * // insert 100 objects
 * realmInstance.executeTransactionAwait { transactionRealm ->
 *   for (i in 1..100) {
 *     // all good if active, otherwise do nothing
 *     if (isActive) {
 *       transactionRealm.insert(MyObject(i))
 *     }
 *   }
 * }
 * ```
 *
 * @param context optional [CoroutineContext] in which this coroutine will run.
 * @param transaction the [Realm.Transaction] to execute.
 * @throws IllegalArgumentException if the `transaction` is `null`.
 * @throws RealmMigrationNeededException if the latest version contains incompatible schema changes.
 */
suspend fun Realm.executeTransactionAwait(
        context: CoroutineContext = Realm.WRITE_EXECUTOR.asCoroutineDispatcher(),
        transaction: (realm: Realm) -> Unit
) {
    // Default to our own thread pool executor (as dispatcher)
    withContext(context) {
        // Get a new coroutine-confined Realm instance from the original Realm's configuration
        Realm.getInstance(configuration).use { coroutineRealm ->
            // Ensure cooperation and prevent execution if the scope is not active.
            if (isActive) {
                coroutineRealm.executeTransaction(transaction)
            }
        }
    }

    // force refresh because we risk fetching stale data from other realms
    refresh()
}

/**
Missing functions. Consider these for inclusion later:
- createAllFromJson(Class<E> clazz, InputStream inputStream)
- createAllFromJson(Class<E> clazz, org.json.JSONArray json)
- createAllFromJson(Class<E> clazz, String json)
- createObjectFromJson(Class<E> clazz, InputStream inputStream)
- createObjectFromJson(Class<E> clazz, org.json.JSONObject json)
- createObjectFromJson(Class<E> clazz, String json)
- createOrUpdateAllFromJson(Class<E> clazz, InputStream in)
- createOrUpdateAllFromJson(Class<E> clazz, org.json.JSONArray json)
- createOrUpdateAllFromJson(Class<E> clazz, String json)
- createOrUpdateObjectFromJson(Class<E> clazz, InputStream in)
- createOrUpdateObjectFromJson(Class<E> clazz, org.json.JSONObject json)
- createOrUpdateObjectFromJson(Class<E> clazz, String json)
- createOrUpdateObjectFromJson(Class<E> clazz, String json)
 */
