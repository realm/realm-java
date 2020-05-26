package io.realm.entities.embedded

import io.realm.RealmObject

open class EmbeddedWithConstructorArgs : RealmObject() {
    var child: EmbeddedSimpleChild? = null

    init {
        child = EmbeddedSimpleChild("innerChild")
    }
}