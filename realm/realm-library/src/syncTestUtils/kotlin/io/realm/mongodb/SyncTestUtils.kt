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
package io.realm.mongodb

import androidx.test.platform.app.InstrumentationRegistry
import io.realm.Realm
import io.realm.TestHelper
import io.realm.internal.network.OkHttpNetworkTransport
import io.realm.internal.objectstore.OsJavaNetworkTransport
import io.realm.log.LogLevel
import io.realm.log.RealmLog
import io.realm.mongodb.sync.SyncConfiguration
import java.io.File
import java.util.*

class SyncTestUtils {
    companion object {
        private var originalLogLevel = RealmLog.getLevel() // Should only be modified by prepareEnvironmentForTest and restoreEnvironmentAfterTest = 0
        fun prepareEnvironmentForTest() {
            Realm.init(InstrumentationRegistry.getInstrumentation().targetContext)
            originalLogLevel = RealmLog.getLevel()
            RealmLog.setLevel(LogLevel.DEBUG)
        }

        // Cleanup filesystem to make sure nothing lives for the next test.
        // Failing to do so might lead to DIVERGENT_HISTORY errors being thrown if Realms from
        // previous tests are being accessed.
        fun deleteSyncFiles() {
            val syncFiles = File(InstrumentationRegistry.getInstrumentation().context.filesDir, "mongodb-realm")
            deleteFile(syncFiles)
        }

        private fun deleteFile(file: File) {
            if (file.isDirectory) {
                for (c in file.listFiles()) {
                    deleteFile(c)
                }
            }
            check(file.delete()) { "Failed to delete file or directory: " + file.absolutePath }
        }

        @JvmStatic
        @JvmOverloads
        fun createTestUser(app: App, userIdentifier: String = UUID.randomUUID().toString()): User {
            val transportBackup = app.osApp.networkTransport
            app.osApp.networkTransport = object : OsJavaNetworkTransport() {
                override fun sendRequest(method: String, url: String, timeoutMs: Long, headers: Map<String, String>, body: String): Response {
                    if (url.endsWith("/login")) {
                        return OkHttpNetworkTransport.Response.httpResponse(200, mapOf(), """
                        {
                            "access_token": "eyJhbGciOiJSUzI1NiIsImtpZCI6IjVlNjk2M2RmYWZlYTYzMjU0NTgxYzAyNiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE1ODM5NjcyMDgsImlhdCI6MTU4Mzk2NTQwOCwiaXNzIjoiNWU2OTY0ZTBhZmVhNjMyNTQ1ODFjMWEzIiwic3RpdGNoX2RldklkIjoiMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwIiwic3RpdGNoX2RvbWFpbklkIjoiNWU2OTYzZGVhZmVhNjMyNTQ1ODFjMDI1Iiwic3ViIjoiNWU2OTY0ZTBhZmVhNjMyNTQ1ODFjMWExIiwidHlwIjoiYWNjZXNzIn0.J4mp8LnlsxTQRV_7W2Er4qY0tptR76PJGG1k6HSMmUYqgfpJC2Fnbcf1VCoebzoNolH2-sr8AHDVBBCyjxRjqoY9OudFHmWZKmhDV1ysxPP4XmID0nUuN45qJSO8QEAqoOmP1crXjrUZWedFw8aaCZE-bxYfvcDHyjBcbNKZqzawwUw2PyTOlrNjgs01k2J4o5a5XzYkEsJuzr4_8UqKW6zXvYj24UtqnqoYatW5EzpX63m2qig8AcBwPK4ZHb5wEEUdf4QZxkRY5QmTgRHP8SSqVUB_mkHgKaizC_tSB3E0BekaDfLyWVC1taAstXJNfzgFtLI86AzuXS2dCiCfqQ",
                            "refresh_token": "eyJhbGciOiJSUzI1NiIsImtpZCI6IjVlNjk2M2RmYWZlYTYzMjU0NTgxYzAyNiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE1ODkxNDk0MDgsImlhdCI6MTU4Mzk2NTQwOCwic3RpdGNoX2RhdGEiOm51bGwsInN0aXRjaF9kZXZJZCI6IjAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMCIsInN0aXRjaF9kb21haW5JZCI6IjVlNjk2M2RlYWZlYTYzMjU0NTgxYzAyNSIsInN0aXRjaF9pZCI6IjVlNjk2NGUwYWZlYTYzMjU0NTgxYzFhMyIsInN0aXRjaF9pZGVudCI6eyJpZCI6IjVlNjk2NGUwYWZlYTYzMjU0NTgxYzFhMC1oaWF2b3ZkbmJxbGNsYXBwYnl1cmJpaW8iLCJwcm92aWRlcl90eXBlIjoiYW5vbi11c2VyIiwicHJvdmlkZXJfaWQiOiI1ZTY5NjNlMGFmZWE2MzI1NDU4MWMwNGEifSwic3ViIjoiNWU2OTY0ZTBhZmVhNjMyNTQ1ODFjMWExIiwidHlwIjoicmVmcmVzaCJ9.FhLdpmL48Mw0SyUKWuaplz3wfeS8TCO8S7I9pIJenQww9nPqQ7lIvykQxjCCtinGvsZIJKt_7R31xYCq4Jp53Nw81By79IwkXtO7VXHPsXXZG5_2xV-s0u44e85sYD5su_H-xnx03sU2piJbWJLSB8dKu3rMD4mO-S0HNXCCAty-JkYKSaM2-d_nS8MNb6k7Vfm7y69iz_uwHc-bb_1rPg7r827K6DEeEMF41Hy3Nx1kCdAUOM9-6nYv3pZSU1PFrGYi2uyTXPJ7R7HigY5IGHWd0hwONb_NUr4An2omqfvlkLEd77ut4V9m6mExFkoKzRz7shzn-IGkh3e4h7ECGA",
                            "user_id": "$userIdentifier",
                            "device_id": "000000000000000000000000"
                        }                    
                        """.trimIndent())
                    } else if (url.endsWith("/auth/profile")) {
                        return OkHttpNetworkTransport.Response.httpResponse(200, mapOf(), """
                        {
                            "user_id": "$userIdentifier",
                            "domain_id": "000000000000000000000000",
                            "identities": [
                                {
                                    "id": "5e68f51ade5ba998bb17500d",
                                    "provider_type": "local-userpass",
                                    "provider_id": "000000000000000000000003",
                                    "provider_data": {
                                        "email": "unique_user@domain.com"
                                    }
                                }
                            ],
                            "data": {
                                "email": "unique_user@domain.com"
                            },
                            "type": "normal",
                            "roles": [
                                {
                                    "role_name": "GROUP_OWNER",
                                    "group_id": "5e68f51e087b1b33a53f56d5"
                                }
                            ]
                        }
                        """.trimIndent())
                    } else if (url.endsWith("/location")) {
                        return OkHttpNetworkTransport.Response.httpResponse(200, mapOf(), """
                        { "deployment_model" : "GLOBAL",
                          "location": "US-VA", 
                          "hostname": "http://localhost:9090",
                          "ws_hostname": "ws://localhost:9090"
                        }
                        """.trimIndent())
                    } else {
                        throw IllegalStateException("Unsupported URL: $url")
                    }
                }

                override fun sendStreamingRequest(request: Request): Response {
                    TODO("Not yet implemented")
                }
            }
            val user = app.login(Credentials.emailPassword(TestHelper.getRandomEmail(), "123456"))
            app.osApp.networkTransport = transportBackup
            return user
        }

        // Fully synchronize a Realm with the server by making sure that all changes are uploaded
        // and downloaded again.
        fun syncRealm(realm: Realm) {
            val config = realm.getConfiguration() as SyncConfiguration
            val session = config.user.app.sync.getSession(config)
            try {
                session.uploadAllLocalChanges()
                session.downloadAllServerChanges()
            } catch (e: InterruptedException) {
                throw AssertionError(e)
            }
            realm.refresh()
        }
    }
}
