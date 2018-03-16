package io.realm.entities

import io.realm.RealmModel
import io.realm.RealmObject
import io.realm.annotations.RealmClass

@RealmClass
open class SimpleClass : RealmModel {
    var name: String = ""
}
