/*
 * Copyright 2020 Realm Inc.
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
package io.realm

import io.realm.internal.network.OkHttpNetworkTransport
import io.realm.internal.objectstore.OsJavaNetworkTransport
import io.realm.mongodb.App
import io.realm.mongodb.AppConfiguration

/**
 * This class wraps various methods making it easier to create an App that can be used
 * for testing.
 *
 * NOTE: This class must remain in the [io.realm] package in order to work.
 */
const val SERVICE_NAME = "BackingDB"    // it comes from the test server's BackingDB/config.json
const val DATABASE_NAME = "test_data"   // same as above

class TestApp(
        networkTransport: OsJavaNetworkTransport? = null,
        builder: (AppConfiguration.Builder) -> AppConfiguration.Builder = { it }
) : App(builder(configurationBuilder()).build()) {

    init {
        if (networkTransport != null) {
            this.setNetworkTransport(networkTransport)
        }
    }

    companion object {

        fun configurationBuilder(customizeConfig: (AppConfiguration.Builder) -> AppConfiguration.Builder = { it }): AppConfiguration.Builder {
            return AppConfiguration.Builder(initializeMongoDbRealm())
                    .baseUrl("http://127.0.0.1:9090")
                    .appName("MongoDB Realm Integration Tests")
                    .appVersion("1.0.")
                    .httpLogObfuscator(null)
        }

        // Initializes MongoDB Realm. Clears all local state and fetches the application ID.
        private fun initializeMongoDbRealm(): String {
            val transport = OkHttpNetworkTransport(null)
            val response = transport.sendRequest(
                    "get",
                    "http://127.0.0.1:8888/application-id",
                    5000,
                    mapOf(),
                    ""
            )
            return when (response.httpResponseCode) {
                200 -> response.body
                else -> throw IllegalStateException(response.toString())
            }
        }
    }
}
