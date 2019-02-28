package io.realm.examples.objectserver.advanced.model.entities

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.RealmResults
import io.realm.annotations.Ignore
import io.realm.annotations.LinkingObjects
import io.realm.annotations.PrimaryKey
import java.util.*


inline class TimeSlotId(val value: String) {
    constructor(): this(UUID.randomUUID().toString())
}

/**
 * An offering describes an Activity happening at a specific time.
 * [Guest]s will book a time on a specific [TimeSlot]
 */
open class TimeSlot: RealmObject() {
    @PrimaryKey
    var id: TimeSlotId = TimeSlotId()

    var time: Date = Date()
    var totalNumberOfSlots: Int = 0
    var bookings: RealmList<Booking> = RealmList()

    @LinkingObjects("timeslots")
    val activities: RealmResults<Activity>? = null

    // Ignore fields cannot be queried
    // Should always contain one element, the parent Activity. This is only true for managed
    // objects.
    @Ignore
    var activity: Activity = Activity()
        get() = if (activities != null) activities.first()!! else throw IllegalStateException("Only available on managed objects")
        private set

    fun remaining(): Int {
        return bookings.size - bookings.where().equalTo("checkedIn", true).count().toInt()
    }

    fun checkedIn(): Int {
        return bookings.where().equalTo("checkedIn", true).count().toInt()
    }

    fun availableSlots(): Int {
        return totalNumberOfSlots - bookings.size
    }
}
