package io.realm.entities

import io.realm.RealmObject
import io.realm.annotations.RealmClass

@RealmClass
open class AllKotlinTypes : RealmObject() {
    var requiredString: String = "";
    var nullableString: String? = null;
}