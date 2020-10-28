package io.realm.examples.coroutinesexample.model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Dog : RealmObject() {
    @PrimaryKey
    var name: String = ""
    var age: Int = 1
    var owner: String = ""

    override fun toString(): String = "Name: $name, age: $age, owner: $owner"
}
