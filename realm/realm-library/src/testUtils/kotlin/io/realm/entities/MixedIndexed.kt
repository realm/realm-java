package io.realm.entities

import io.realm.Mixed
import io.realm.RealmObject
import io.realm.annotations.Index

open class MixedIndexed : RealmObject() {
    companion object {
        const val FIELD_MIXED = "mixed"
    }

//    @Index
    var mixed: Mixed? = null
}
