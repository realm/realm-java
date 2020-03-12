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

import androidx.test.platform.app.InstrumentationRegistry
import io.realm.internal.network.OkHttpNetworkTransport
import io.realm.internal.objectstore.OsJavaNetworkTransport
import io.realm.log.LogLevel
import java.lang.IllegalStateException

/**
 * This class wraps various methods making it easier to create an RealmApp that can be used
 * for testing.
 *
 * NOTE: This class must remain in the [io.realm] package in order to work.
 */
class TestRealmApp private constructor() {
    companion object {
        private val applicationId = fetchApplicationId()
        val config = RealmAppConfiguration.Builder(applicationId)
                .logLevel(LogLevel.DEBUG)
                .baseUrl("http://127.0.0.1:9090")
                .appName("MongoDB Realm Integration Tests")
                .appVersion("1.0.")
                .build()

        private fun init() {
            Realm.init(InstrumentationRegistry.getInstrumentation().targetContext)
        }

        private fun fetchApplicationId(): String {
            val transport = OkHttpNetworkTransport()
            val response = transport.sendRequest(
                    "get",
                    "http://localhost:8888/application-id",
                    5000,
                    mapOf(),
                    ""
            )
            return when(response.httpResponseCode) {
                200 -> response.body
                else -> throw IllegalStateException(response.toString())
            }
        }

        fun getInstance(networkTransport: OsJavaNetworkTransport? = null): RealmApp {
            init()
            val app = RealmApp(config)
            if (networkTransport != null) {
                app.networkTransport = networkTransport
            }
            return app
        }
    }
}
