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

package io.realm.examples.objectserver.advanced

internal object Constants {
    /**
     * Realm Cloud Users:
     * Replace INSTANCE_ADDRESS with the hostname of your cloud instance
     * e.g., "mycoolapp.us1.cloud.realm.io"
     *
     * ROS On-Premises Users
     * Replace the INSTANCE_ADDRESS with the fully qualified version of
     * address of your ROS server, e.g.: INSTANCE_ADDRESS = "192.168.1.65:9080" and "http://" + INSTANCE_ADDRESS + "/auth"
     * (remember to use 'http/realm' instead of 'https/realms' if you didn't setup SSL on ROS yet)
     */
    private val INSTANCE_ADDRESS = "cmelchior.us1.cloud.realm.io"
    val AUTH_URL = "https://$INSTANCE_ADDRESS/auth"
    val STANDARD_REALM_URL = "realms://$INSTANCE_ADDRESS/demo1" // Query-based Realm
    val ORDERS_REALM_URL = "realms://$INSTANCE_ADDRESS/demo2" // Query-based Realm

}
