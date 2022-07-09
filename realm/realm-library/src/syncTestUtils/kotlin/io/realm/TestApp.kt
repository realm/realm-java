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
import kotlinx.serialization.json.*

/**
 * This class wraps various methods making it easier to create an App that can be used
 * for testing.
 *
 * NOTE: This class must remain in the [io.realm] package in order to work.
 */
const val SERVICE_NAME = "BackingDB"    // it comes from the test server's BackingDB/config.json
const val DATABASE_NAME = "test_data"   // same as above
const val TEST_APP_1 = "testapp1"       // Id for the default test app
const val TEST_APP_2 =
    "testapp2"       // ID for the 2nd test app, which is a direct copy of the default test app.
const val TEST_APP_3 =
    "testapp3"       // ID for the 3rd test app, which is configured for Flexible Sync instead of Partion-based sync

class TestApp(
    networkTransport: OsJavaNetworkTransport? = null,
    private val appName: String = TEST_APP_1,
    builder: (AppConfiguration.Builder) -> AppConfiguration.Builder = { it }
) : App(builder(configurationBuilder(appName)).build()) {

    init {
        if (networkTransport != null) {
            this.setNetworkTransport(networkTransport)
        }
    }

    fun setIsRecoveryModeDisabled(isRecoveryModeDisabled: Boolean) {
        updateMDBDocument(
            dbName = "app",
            collection = "apps",
            query = """{"name": "$appName"}""",
            update = """
                {"${'$'}set": {"services.$[element].config.sync.is_recovery_mode_disabled": $isRecoveryModeDisabled}}
                """.trimIndent(),
            options = """{"arrayFilters": [{"element.name": "BackingDB"}]}"""
        )
    }

    fun triggerClientReset(userId: String, disableRecoveryMode: Boolean = false) {
        val originalState = isRecoveryModeDisabled(appName)

        setIsRecoveryModeDisabled(disableRecoveryMode)

        // deleting clientfile triggers a reset
        deleteMDBDocumentByQuery("__realm_sync", "clientfiles", "{ownerId: \"$userId\"}")

        setIsRecoveryModeDisabled(originalState)
    }

    private fun isRecoveryModeDisabled(app: String): Boolean {
        val app = queryMDBDocument(
            dbName = "app",
            collection = "apps",
            query = """{"name": "$app"}"""
        ).jsonObject

        return app["services"]!!
            .jsonArray.first { element ->
                element.jsonObject["name"]!!.jsonPrimitive.content == "BackingDB"
            }
            .jsonObject["config"]!!
            .jsonObject["sync"]!!
            .jsonObject.getOrDefault(
                "is_recovery_mode_disabled",
                JsonPrimitive(false)
            )
            .jsonPrimitive.boolean
    }

    private fun deleteMDBDocumentByQuery(
        dbName: String,
        collection: String,
        query: String
    ): String {
        val transport = OkHttpNetworkTransport(null)
        val response = transport.executeRequest(
            "get",
            "http://127.0.0.1:8888/delete-document?db=$dbName&collection=$collection&query=$query",
            5000,
            mapOf(),
            ""
        )
        return when (response.httpResponseCode) {
            200 -> response.body
            else -> throw IllegalStateException(response.toString())
        }
    }

    private fun queryMDBDocument(
        dbName: String,
        collection: String,
        query: String
    ): JsonObject {
        val transport = OkHttpNetworkTransport(null)
        val response = transport.executeRequest(
            "get",
            "http://127.0.0.1:8888/query-document?db=$dbName&collection=$collection&query=$query",
            5000,
            mapOf(),
            ""
        )

        return when (response.httpResponseCode) {
            200 -> Json.parseToJsonElement(response.body).jsonObject
            else -> throw IllegalStateException(response.toString())
        }
    }

    private fun updateMDBDocument(
        dbName: String,
        collection: String,
        query: String,
        update: String,
        options: String,
    ): JsonObject {
        val transport = OkHttpNetworkTransport(null)
        val response = transport.executeRequest(
            "get",
            "http://127.0.0.1:8888/update-document?db=$dbName&collection=$collection&query=$query&update=$update&options=$options",
            5000,
            mapOf(),
            ""
        )

        return when (response.httpResponseCode) {
            200 -> Json.parseToJsonElement(response.body).jsonObject
            else -> throw IllegalStateException(response.toString())
        }
    }

    companion object {
        fun configurationBuilder(appName: String): AppConfiguration.Builder {
            return AppConfiguration.Builder(initializeMongoDbRealm(appName))
                .baseUrl("http://127.0.0.1:9090")
                .appName("MongoDB Realm Integration Tests")
                .appVersion("1.0.")
                .httpLogObfuscator(null)
        }

        // Initializes MongoDB Realm. Clears all local state and fetches the application ID.
        private fun initializeMongoDbRealm(appName: String): String {
            val transport = OkHttpNetworkTransport(null)
            val response = transport.executeRequest(
                "get",
                "http://127.0.0.1:8888/$appName",
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
