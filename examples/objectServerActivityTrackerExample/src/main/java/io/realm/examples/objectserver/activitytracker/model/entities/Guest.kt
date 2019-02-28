package io.realm.examples.objectserver.advanced.model.entities

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*

inline class GuestId(val value: String) {
    constructor(): this(UUID.randomUUID().toString())
}

/**
 * Object describing a Guest that can book activities.
 */
open class Guest: RealmObject() {
    @PrimaryKey
    var id: GuestId = GuestId()
    var name: String = ""
}