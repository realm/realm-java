package io.realm.entities

import io.realm.Mixed
import io.realm.RealmObject

open class MixedDefaultPK : RealmObject() {
    companion object {
        const val FIELD_MIXED = "mixed"
        const val NAME = "hello world"
    }

    var mixed: Mixed? = Mixed.valueOf(PrimaryKeyAsString(NAME))
}