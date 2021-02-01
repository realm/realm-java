package io.realm.entities.embedded

import io.realm.RealmObject
import io.realm.annotations.RealmClass

@RealmClass(embedded = true)
open class SimpleEmbeddedObject() : RealmObject() {
    var name: String = "a simple attribute"
}