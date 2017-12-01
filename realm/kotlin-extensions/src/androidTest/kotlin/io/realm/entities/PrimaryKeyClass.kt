package io.realm.entities

import io.realm.RealmModel
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass

@RealmClass
open class PrimaryKeyClass: RealmModel {
    @PrimaryKey
    var id: Long = 0
    var name: String = ""
}
