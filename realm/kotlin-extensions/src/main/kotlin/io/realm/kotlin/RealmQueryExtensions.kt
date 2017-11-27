@file:Suppress("unused")

package io.realm.kotlin

import io.realm.Case
import io.realm.RealmModel
import io.realm.RealmQuery
import java.util.*

// in

fun <T : RealmModel> RealmQuery<T>.oneOf(propertyName: String,
                                         value: Array<String?>,
                                         casing: Case = Case.SENSITIVE): RealmQuery<T> {
    return this.`in`(propertyName, value, casing)
}

fun <T : RealmModel> RealmQuery<T>.oneOf(propertyName: String,
                                         value: Array<Byte?>): RealmQuery<T> {
    return this.`in`(propertyName, value)
}

fun <T : RealmModel> RealmQuery<T>.oneOf(propertyName: String,
                                         value: Array<Short?>): RealmQuery<T> {
    return this.`in`(propertyName, value)
}

fun <T : RealmModel> RealmQuery<T>.oneOf(propertyName: String,
                                         value: Array<Int?>): RealmQuery<T> {
    return this.`in`(propertyName, value)
}

fun <T : RealmModel> RealmQuery<T>.oneOf(propertyName: String,
                                         value: Array<Long?>): RealmQuery<T> {
    return this.`in`(propertyName, value)
}

fun <T : RealmModel> RealmQuery<T>.oneOf(propertyName: String,
                                         value: Array<Double?>): RealmQuery<T> {
    return this.`in`(propertyName, value)
}

fun <T : RealmModel> RealmQuery<T>.oneOf(propertyName: String,
                                         value: Array<Float?>): RealmQuery<T> {
    return this.`in`(propertyName, value)
}

fun <T : RealmModel> RealmQuery<T>.oneOf(propertyName: String,
                                         value: Array<Boolean?>): RealmQuery<T> {
    return this.`in`(propertyName, value)
}

fun <T : RealmModel> RealmQuery<T>.oneOf(propertyName: String,
                                         value: Array<Date?>): RealmQuery<T> {
    return this.`in`(propertyName, value)
}
