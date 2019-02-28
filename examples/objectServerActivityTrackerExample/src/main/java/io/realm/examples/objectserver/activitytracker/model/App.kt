package io.realm.examples.objectserver.activitytracker.model

import io.realm.examples.objectserver.activitytracker.Constants
import io.realm.examples.objectserver.activitytracker.model.entities.Guest
import io.realm.examples.objectserver.activitytracker.model.entities.Order
import io.realm.examples.objectserver.activitytracker.model.entities.Activity
import io.realm.RealmConfiguration
import io.realm.SyncUser
import io.realm.kotlin.where

/**
 * App / Business logic that doesn't naturally fit elsewhere
 */
class App {




    companion object {

        lateinit var REALM1_CONFIG: RealmConfiguration
        lateinit var REALM2_CONFIG: RealmConfiguration

        fun configureRealms(user: SyncUser) {
            REALM1_CONFIG = user
                    .createConfiguration(Constants.STANDARD_REALM_URL)
                    .initialData {
                        // Initial subscriptions
                        it.where<Guest>().subscribe("guests")
                        it.where<Activity>().subscribe("activities")
                        it.where<Order>().subscribe("orders")
                    }
                    .build()

            REALM2_CONFIG = user
                    .createConfiguration(Constants.ORDERS_REALM_URL)
                    .initialData {
                        // Initial subscriptions
                        it.where<Order>().subscribe("orders")
                    }
                    .build()


        }
    }
}