package io.realm.examples.coroutinesexample.data.local.model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Dog : RealmObject() {
    @PrimaryKey
    var name: String = ""
    var age: Int = 1
    var owner: String = ""      // We have an owner but we won't show it on screen

    override fun toString(): String = "Name: $name, age: $age, owner: $owner"
}
