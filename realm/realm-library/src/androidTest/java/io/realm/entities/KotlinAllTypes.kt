package io.realm.entities

import io.realm.RealmList
import io.realm.RealmModel
import io.realm.annotations.RealmClass

@RealmClass
class KotlinAllTypes : RealmModel {
    lateinit var stringField : String
    var listField = RealmList<KotlinAllTypes>()
}

val KOTLIN_ALL_TYPES_STRING_FIELD = "stringField"
val KOTLIN_ALL_TYPES_LIST_FIELD = "listField"

