package io.realm.examples.coroutinesexample.model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Doggo : RealmObject() {
    @PrimaryKey
    var name: String = ""
    var age: Int = 1
    var owner: String = ""
}
