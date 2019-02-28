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

package io.realm.examples.objectserver.advanced.ui.bookingslist

import androidx.lifecycle.LiveData
import androidx.lifecycle.LiveDataReactiveStreams
import androidx.lifecycle.MutableLiveData
import io.realm.examples.objectserver.advanced.model.entities.Booking
import io.realm.examples.objectserver.advanced.model.entities.TimeSlot
import io.realm.examples.objectserver.advanced.model.entities.TimeSlotId
import io.realm.examples.objectserver.advanced.ui.BaseViewModel
import io.realm.Case
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import io.realm.kotlin.where

class BookingsListViewModel(private val timeslotId: TimeSlotId, private val mode: Mode): BaseViewModel() {

    enum class Mode {
        CheckIn,
        CancelCheckIn
    }

    enum class NavigationTarget {
        CheckinOverview,
    }

    private val navigateTo = MutableLiveData<Pair<NavigationTarget, TimeSlotId>>()
    private val title: MutableLiveData<String> = MutableLiveData()
    private val bookings: MutableLiveData<RealmResults<Booking>> = MutableLiveData()
    private var selectedOffering: TimeSlot // Strong reference to keep change listener alive
    private val actionText: LiveData<String>

    init {
        selectedOffering = realm.where<TimeSlot>().equalTo("id", timeslotId.value).findFirstAsync()
        selectedOffering.addChangeListener<TimeSlot> { obj ->
            title.value = if (obj.isValid) obj.activity.name else "Activity not found"
        }
        bookings.value = createSearchQuery("")
        actionText = LiveDataReactiveStreams.fromPublisher(realm.where<Booking>()
                .equalTo("timeslots.id", timeslotId.value)
                .equalTo("selected", true)
                .findAllAsync()
                .asFlowable()
                .map {
                    when(mode) {
                        Mode.CheckIn -> "Check-in (${it.count()})"
                        Mode.CancelCheckIn -> "Cancel check-in (${it.count()})"
                    }
                })
    }

    /**
     * Returns the list of all relevant bookings
     */
    fun bookings(): LiveData<RealmResults<Booking>> {
        return bookings
    }

    /**
     * Returns the title of the activity
     */
    fun title(): LiveData<String> {
        return title
    }

    /**
     * Observe navigation events from this ViewModel
     */
    fun navigate(): LiveData<Pair<NavigationTarget, TimeSlotId>> {
        return navigateTo
    }

    fun setSearchCriteria(searchString: String) {
        bookings.value = createSearchQuery(searchString)
    }

    fun toggleSelected(booking: Booking) {
        val id = booking.id
        realm.executeTransactionAsync {
            // We cheat a bit here and save the UI state to the Realm.
            // Most likely we don't want to synchronize a temporary state as "selected"
            // to the Realm, but it simplifies the logic quite a lot.
            //
            // Otherwise we will need to combine two streams of data: RealmResults and SelectedBookings
            // which is a bit complex for a POC, especially if we also want fine-grained animations.
            it.where<Booking>().equalTo("id", id.value).findFirst()?.let { booking ->
                booking.selected = !booking.selected
            }
        }
    }

    /**
     * Returns the String used to describe the action performed on selected guests.
     */
    fun actionText(): LiveData<String> {
        return actionText
    }

    /**
     * Toggle the checked in state and return to the Check-in overview screen
     */
    fun actionSelected() {
        val id = timeslotId.value
        realm.executeTransactionAsync(Realm.Transaction {
            it.where<Booking>()
                    .equalTo("timeslots.id", id)
                    .equalTo("checkedIn", mode != Mode.CheckIn)
                    .equalTo("selected", true)
                    .findAll()
                    .createSnapshot() // Updated to keep objects in query result even after they are updated
                    .forEach {obj ->
                        obj.selected = false
                        obj.checkedIn = (mode == Mode.CheckIn)
                    }
        },
        Realm.Transaction.OnSuccess {
            navigateTo.value = Pair(NavigationTarget.CheckinOverview, timeslotId)
        })
    }

    // Creates the query
    private fun createSearchQuery(nameFilter: String): RealmResults<Booking>? {
        cleanupSubscriptions()
        val query = realm.where<Booking>()
                .equalTo("timeslots.id", timeslotId.value)
                .equalTo("checkedIn", mode == Mode.CancelCheckIn)
                .like("guest.name", "*$nameFilter*", Case.INSENSITIVE)
                .sort("guest.name", Sort.ASCENDING)

        // Use a structured approach to naming subscriptions so we can easily find them later
        return query.findAllAsync("bookings.${timeslotId.value}.$mode.$nameFilter") //
    }

    /**
     * This method cleanup all subscriptions related to this screen
     * It does so in an optimistic manner, so e.g. crashes or killing the app on this screen
     * will cause subscriptions to linger.
     *
     * Having a large amounts of subscriptions is not a problem for the device, but can have
     * negative performance implications for the server. Having some periodic cleanup routine
     * is advised to catch stray subscriptions.
     */
    private fun cleanupSubscriptions() {
        val id = timeslotId.value
        realm.executeTransactionAsync {
            it.getSubscriptions("bookings.$id.*").deleteAllFromRealm()
        }
    }

}