package io.realm.entities

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class PrimaryKeyClass: RealmObject() {
    @PrimaryKey
    var id: Long = 0
    var name: String = "";
}