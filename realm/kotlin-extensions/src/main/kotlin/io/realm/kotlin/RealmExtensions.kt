package io.realm.kotlin

import io.realm.Realm
import io.realm.RealmModel
import io.realm.RealmQuery
import kotlin.reflect.KClass

fun <E : RealmModel> Realm.where2(clazz: KClass<E>): RealmQuery<E> {
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