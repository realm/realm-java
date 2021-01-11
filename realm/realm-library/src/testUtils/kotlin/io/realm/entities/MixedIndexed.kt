package io.realm.entities

import io.realm.Mixed
import io.realm.RealmObject

open class MixedIndexed : RealmObject() {
    companion object {
        const val FIELD_MIXED = "mixed"
    }

    // FIXME: Index disabled until https://jira.mongodb.org/browse/RCORE-434 is fixed
    //@Index
    var mixed: Mixed? = null
}