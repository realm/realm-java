@file:Suppress("unused")

package io.realm.kotlin

import io.realm.Realm
import io.realm.RealmModel
import io.realm.RealmQuery
import java.util.concurrent.atomic.AtomicReference


//
// Add variants of all methods in Realm.java that takes a `Class` reference and use a `KClass` reference instead
//

/**
 * Returns a typed RealmQuery, which can be used to query for specific objects of this type
 *
 * @param <T> the class of the object which is to be queried for.
 * @return a typed RealmQuery, which can be used to query for specific objects of this type.
 * @see io.realm.RealmQuery
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
inline fun <reified T : RealmModel> Realm.delete(): Unit {
    return this.delete(T::class.java)
}

inline fun <T> Realm.callTransaction(crossinline action: Realm.() -> T): T {
    val ref = AtomicReference<T>()
    executeTransaction {
        ref.set(action(it))
    }
    return ref.get()
}

inline fun <reified T : RealmModel> Realm.createObject(): T {
    return this.createObject(T::class.java)
}

inline fun <reified T : RealmModel> Realm.createObject(primaryKeyValue: Any?): T {
    return this.createObject(T::class.java, primaryKeyValue)
}
