package io.realm.entities

import io.realm.Mixed
import io.realm.RealmObject

open class MixedDefaultNonPK : RealmObject() {
    companion object {
        const val FIELD_MIXED = "mixed"
    }

    var mixed: Mixed? = Mixed.valueOf(MixedNotIndexed())
}