/*
 * Copyright 2019 Realm Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.realm.examples.objectserver.activitytracker.model.entities

import io.realm.RealmObject
import io.realm.RealmResults
import io.realm.annotations.Ignore
import io.realm.annotations.LinkingObjects
import io.realm.annotations.PrimaryKey
import java.util.*

inline class BookingId(val value: String) {
    constructor(): this(UUID.randomUUID().toString())
}

/**
 * Booking object representing both normal and adhoc bookings.
 */
open class Booking: RealmObject() {

    @PrimaryKey
    var id: BookingId = BookingId()

    var guest: Guest? = null
    var checkedIn: Boolean = false
    var adhoc: Boolean = false
    var selected: Boolean = false; // UI-state. Storing here for now for simplicity

    // Should always contain one element, the parent TimeSlot
    @LinkingObjects("bookings")
    private val timeslots: RealmResults<TimeSlot>? = null

    @Ignore
    var offering: TimeSlot = TimeSlot()
        get() = if (timeslots != null) timeslots.first()!! else throw IllegalStateException("Only available on managed objects")
        private set

    @Ignore
    var excursion: Activity = Activity()
        get() = if (timeslots != null) timeslots.first()!!.activity else throw IllegalStateException("Only available on managed objects")
        private set
}