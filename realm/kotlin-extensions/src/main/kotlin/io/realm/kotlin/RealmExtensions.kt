@file:Suppress("unused")

package io.realm.kotlin

import io.realm.Realm
import io.realm.RealmModel
import io.realm.RealmQuery
import io.realm.exceptions.RealmException
import java.util.concurrent.atomic.AtomicReference


//
// Add variants of all methods in Realm.java that takes a `Class` reference and use a reified variant instead.
//

/**
 * Returns a typed RealmQuery, which can be used to query for specific objects of this type
 *
 * @param <T> the class of the object which is to be queried for.
 * @return a typed RealmQuery, which can be used to query for specific objects of this type.
 */
inline fun <reified T : RealmModel> Realm.where(): RealmQuery<T> {
    return this.where(T::class.java)
}

/**
 * Deletes all objects of the specified class from the Realm.
 *
 * @param <T> the class of the object which is to be queried for.
 * @throws IllegalStateException if the corresponding Realm is closed or called from an incorrect thread.
 */
inline fun <reified T : RealmModel> Realm.delete() {
    return this.delete(T::class.java)
}

// TODO: Figure out if we should include this is or. Using this makes it possible to do
// ```
// realm.callTransaction { createObject<Foo>() }
//
//inline fun <T> Realm.callTransaction(crossinline action: Realm.() -> T): T {
//    val ref = AtomicReference<T>()
//    executeTransaction {
//        ref.set(action(it))
//    }
//    return ref.get()
//}

/**
 * Instantiates and adds a new object to the Realm.
 * <p>
 * This method is only available for model classes with no `@PrimaryKey` annotation.
 * If you like to create an object that has a primary key, use [createObject] instead.
 *
 * @param <T> the Class of the object to create.
 * @return the new object.
 * @throws RealmException if the primary key is defined in the model class or an object cannot be created.
 */
inline fun <reified T : RealmModel> Realm.createObject(): T {
    return this.createObject(T::class.java)
}

/**
 * Instantiates and adds a new object to the Realm with the primary key value already set.
 *
 * If the value violates the primary key constraint, no object will be added and a [RealmException] will be
 * thrown. The default value for primary key provided by the model class will be ignored.
 *
 * @param <T> the Class of the object to create.
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
