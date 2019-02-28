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

package io.realm.examples.objectserver.activitytracker.ui.checkin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import io.realm.examples.objectserver.activitytracker.model.entities.*
import io.realm.examples.objectserver.activitytracker.ui.BaseViewModel
import io.realm.RealmResults
import io.realm.Sort
import io.realm.kotlin.where

class CheckinViewModel(private val excursionId: ActivityId): BaseViewModel() {

    enum class NavigationTarget {
        CheckedInGuests,
        RemainingGuests,
        AdhocGuest
    }

    private val navigationTarget = MutableLiveData<Pair<NavigationTarget, TimeSlotId>>()
    private val selectedTimeslot: MutableLiveData<TimeSlot?> = MutableLiveData()
    private val title: MutableLiveData<String> = MutableLiveData()
    private val selectedExcursion: Activity

    init {
        selectedExcursion = realm.where<Activity>().equalTo("id", excursionId.value).findFirstAsync()
        selectedExcursion.addChangeListener<Activity> { obj ->
            title.value = if (obj.isValid) obj.name else "Excursion not found"
        }
        selectedTimeslot.value = null // Set dummy value to initialize other streams
    }

    /**
     * Returns the number of timeslots for the day
     */
    fun timeslots(): RealmResults<TimeSlot> {
        return realm.where<TimeSlot>()
                .equalTo("activities.id", excursionId.value)
                .sort("time", Sort.ASCENDING)
                .findAllAsync("timeslots.${excursionId.value}")
    }

    /**
     * Returns the title of the activity
     */
    fun title(): LiveData<String> {
        return title
    }

    /**
     * Returns the number of checked in guests
     */
    fun checkedIn(): LiveData<String> {
        return Transformations.map(selectedTimeslot) {
            it?.checkedIn()?.toString() ?: "-"
        }
    }

    /**
     * Returns the number of guests not checked in yet
     */
    fun remaining(): LiveData<String> {
        return Transformations.map(selectedTimeslot) {
            it?.remaining()?.toString() ?: "-"
        }
    }

    /**
     * Returns a representation of how many slots are still available
     */
    fun slotsAvailable(): LiveData<String> {
        return Transformations.map(selectedTimeslot) {
            if (it != null) {
            "${it.availableSlots()}/${it.totalNumberOfSlots}"
            } else {
                "-/-"
            }
        }
    }

    // Strong reference to prevent changelistener from being GC'ed
    private lateinit var allBookings: RealmResults<Booking>

    fun selectOffering(offering: TimeSlot?) {
        selectedTimeslot.value?.removeAllChangeListeners()

        if (offering != null) {
            offering.addChangeListener<TimeSlot> { obj ->
                // The offering itself was updated
                selectedTimeslot.value = obj
            }

            allBookings = offering.bookings.where().findAllAsync()
            allBookings.addChangeListener { results ->
                // Some of the bookings associated with the offering was updated
                // The changelistener for TimeSlot is not triggered if an element in an list is modified
                // Only if the list element is added or removed to the list. For that reason we need
                // a seperate listener on all the bookings
                if (!results.isEmpty()) {
                    selectedTimeslot.value = results.first()?.offering
                }
            }
        }

        selectedTimeslot.value = offering
    }

    /**
     * Observe navigation events from this ViewModel
     */
    fun navigate(): LiveData<Pair<NavigationTarget, TimeSlotId>> {
        return navigationTarget
    }

    fun adhocCheckinSelected() {
        val offering = selectedTimeslot.value
        if (offering != null) {
            navigationTarget.value = Pair(NavigationTarget.AdhocGuest, offering.id)
        }
    }

    fun remainingGuestsSelected() {
        val offering = selectedTimeslot.value
        if (offering != null) {
            navigationTarget.value = Pair(NavigationTarget.RemainingGuests, offering.id)
        }
    }

    fun checkedInGuestsSelected() {
        val offering = selectedTimeslot.value
        if (offering != null) {
            navigationTarget.value = Pair(NavigationTarget.CheckedInGuests, offering.id)
        }
    }
}