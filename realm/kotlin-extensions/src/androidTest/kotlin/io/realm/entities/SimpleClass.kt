package io.realm.entities

import io.realm.RealmObject
import io.realm.annotations.RealmClass

@RealmClass
open class SimpleClass : RealmObject() {
    var name: String = "";
}