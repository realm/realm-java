package io.realm.examples.objectserver.activitytracker.model.entities

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*

inline class ActivityId(val value: String) {
    constructor(): this(UUID.randomUUID().toString())
}

/**
 * This object describes a specific activity. An activity can happen multiple times during a day,
 * each of these are described as a [TimeSlot]
 *
 * Guests will book a slot for a given [TimeSlot], not on the [Activity] which just contains
 * the high-level details.
 */
open class Activity: RealmObject() {
    @PrimaryKey
    var id: ActivityId = ActivityId()
    var name:String = ""

    var timeslots: RealmList<TimeSlot> = RealmList()
}