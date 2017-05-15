package io.realm.entities

import io.realm.RealmObject
import io.realm.annotations.RealmClass

open class AllKotlinTypes : RealmObject() {
    var requiredString: String = "";
    var nullableString: String? = null;
}