@file:Suppress("unused")

package io.realm.kotlin

import io.realm.*
import java.util.*
import java.util.concurrent.atomic.AtomicReference
import kotlin.reflect.KMutableProperty1

fun RealmModel.deleteFromRealm() {
    RealmObject.deleteFromRealm(this)
}

fun RealmModel.isValid(): Boolean {
    return RealmObject.isValid(this)
}

fun RealmModel.isManaged(): Boolean {
    return RealmObject.isManaged(this)
}

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

