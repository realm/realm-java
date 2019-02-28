package io.realm.examples.objectserver.activitytracker.model.entities

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*

inline class OrderId(val value: String) {
    constructor(): this(UUID.randomUUID().toString())
}

/**
 * This object represents a drink order in the bar.
 *
 */
open class Order: RealmObject() {

    @PrimaryKey
    var id: TimeSlotId = TimeSlotId()

    var name: String = ""
    var createdAt: Date = Date()
    var from: String = ""

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Order

        if (id != other.id) return false
        if (name != other.name) return false
        if (createdAt != other.createdAt) return false
        if (from != other.from) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + createdAt.hashCode()
        result = 31 * result + from.hashCode()
        return result
    }
}