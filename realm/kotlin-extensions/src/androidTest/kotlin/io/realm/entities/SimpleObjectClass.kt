package io.realm.entities

import io.realm.RealmObject
import io.realm.annotations.RealmClass

@RealmClass
open class SimpleObjectClass : RealmObject() {
    var name: String = ""
}
