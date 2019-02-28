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

package io.realm.examples.objectserver.advanced.ui.activitylist

import androidx.lifecycle.LiveData
import io.realm.examples.objectserver.advanced.model.App
import io.realm.examples.objectserver.advanced.model.entities.*
import io.realm.examples.objectserver.advanced.ui.BaseViewModel
import io.realm.Realm
import io.realm.RealmList
import io.realm.RealmResults
import io.realm.Sort
import io.realm.kotlin.where
import java.util.*

class SelectActivityViewModel: BaseViewModel() {

    enum class NavigationTarget {
        ExcursionDetails,
        Orders
    }
    private val navigationTarget = io.realm.examples.objectserver.advanced.ui.SingleLiveEvent<Pair<NavigationTarget, ActivityId>>()

    /**
     * Returns the list of all available activities for the day
     */
    fun excursions(): RealmResults<Activity> {
        // For now just assume that all activities are available for the day
        // If not, the data model might be a bit tricky since Realm Java doesn't support
        // sub-queries yet. So finding the Activity objects with timeslots might
        // be a bit tricky. See https://github.com/realm/realm-java/issues/1598 and
        // https://github.com/realm/realm-java/pull/6116
        return realm.where<Activity>()
                .sort("name", Sort.ASCENDING)
                .findAllAsync("activities.today")
    }

    /**
     * Select a given activity
     */
    fun excursionSelected(excursion: Activity) {
        navigationTarget.value = Pair(NavigationTarget.ExcursionDetails, excursion.id)
    }

    fun gotoOrdersSelected() {
        navigationTarget.value = Pair(NavigationTarget.Orders, ActivityId())
    }

    /**
     * Navigation event. Used by the Activity to navigationTarget to the selected navigationTarget
     */
    val navigateTo : LiveData<Pair<NavigationTarget, ActivityId>>
        get() = navigationTarget


    /**
     * This method will loop through the users subscriptions and remove those that no longer is
     * needed.
     */
    fun cleanupSubscriptions() {
        realm.executeTransactionAsync {

            // All subscriptions used when searching for guests part of a Booking
            // See `BookingsListViewModel.kt#131`
            it.getSubscriptions("bookings.*.*.*").deleteAllFromRealm()

            // Right now we keep all subscriptions for Offerings in `CheckinViewModel.kt#37`
            // These should only live for a ~day. Until we get time-based subscription support
            // in Realm we need to manually remove them.
            // For this demo, just keep them, but the date should be encoded in the name
            // e.g. `timeslots.dd-mm-yyy` so we can easily identify and remove them here.
            // TODO()
        }
    }

    /**
     * Creates a number of randomized demo data
     */
    fun createDemoData() {
        val random = Random()
        realm.executeTransactionAsync { realm ->
            // Generate guests
            val firstNames = listOf("John", "Jane", "Sean", "Sofia", "Marisa", "Scott", "Peter", "Brian", "Julia")
            val lastNames = listOf("Atkinson", "Reynolds", "Gibson", "Glenn", "Holland", "Brooks", "Hope", "McDonalds")
            repeat(20) {
                val guest = Guest()
                guest.name = "${firstNames.random()} ${lastNames.random()}"
                realm.insertOrUpdate(guest)
            }
            val guests = realm.where<Guest>().findAll()

            // Generate Shore Excursions
            val titles = listOf(
                    "Snorkeling",
                    "Shark fishing",
                    "Scuba diving",
                    "Thrill Waterpark All Day Pass")
            for (name in titles) {
                val excursion = Activity()
                excursion.name = name
                excursion.timeslots = RealmList()

                // Generate timeslots
                repeat(5) {
                    val offering = TimeSlot()
                    offering.totalNumberOfSlots = random.nextInt(20) + 1 // Avoid 0 as number of slots
                    offering.time = Date()
                    offering.time.hours = offering.time.hours + it // Ignore overflow into the next day

                    // Add bookings to each offering
                    repeat(random.nextInt(offering.totalNumberOfSlots)) {
                        val booking = Booking()
                        booking.checkedIn = random.nextBoolean()
                        booking.guest = guests.random()
                        booking.adhoc = (random.nextInt(5) == 0)
                        offering.bookings.add(booking)
                    }
                    excursion.timeslots.add(offering)
                }
                realm.insertOrUpdate(excursion)
            }

            repeat(10) { i ->
                val o = Order()
                o.name = "Drink $i"
                o.from = "Realm1"
                o.createdAt = Date(Math.abs(random.nextInt()).toLong())
                realm.insertOrUpdate(o)
            }
        }

        val realm2 = Realm.getInstance(App.REALM2_CONFIG)
        realm2.executeTransactionAsync(Realm.Transaction { realm ->
            repeat(10) { i ->
                val o = Order()
                o.name = "Drink $i"
                o.from = "Realm2"
                o.createdAt = Date(Math.abs(random.nextInt()).toLong())
                realm.insertOrUpdate(o)
            }
        }, Realm.Transaction.OnSuccess { realm2.close() })
    }

}