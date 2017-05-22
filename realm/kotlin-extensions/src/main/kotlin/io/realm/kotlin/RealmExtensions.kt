package io.realm.kotlin

import io.realm.Realm
import io.realm.RealmModel
import io.realm.RealmQuery
import kotlin.reflect.KClass


//
// Add variants of all methods in Realm.java that takes a `Class` reference and use a `KClass` reference instead
//

/**
 * Returns a typed RealmQuery, which can be used to query for specific objects of this type
 *
 * @param clazz the class of the object which is to be queried for.
 * @return a typed RealmQuery, which can be used to query for specific objects of this type.
 * @see io.realm.RealmQuery
 */
fun <E : RealmModel> Realm.where(clazz: KClass<E>): RealmQuery<E> {
    val c: Class<E> = clazz.java
    return this.where(c);
}

/**
 * Deletes all objects of the specified class from the Realm.
 *
 * @param clazz the class which objects should be removed.
 * @throws IllegalStateException if the corresponding Realm is closed or called from an incorrect thread.
 */
fun <E : RealmModel> Realm.delete(clazz: KClass<E>) {
    this.delete(clazz.java);
}