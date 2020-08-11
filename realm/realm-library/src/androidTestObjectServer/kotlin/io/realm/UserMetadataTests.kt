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

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.realm.internal.objectstore.OsJavaNetworkTransport
import io.realm.mongodb.*
import io.realm.rule.BlockingLooperThread
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UserMetadataTests {

    val looperThread = BlockingLooperThread()

    private lateinit var app: App

    @Before
    fun setUp() {
        Realm.init(InstrumentationRegistry.getInstrumentation().targetContext)

        val successHeaders: Map<String, String> = mapOf(Pair("Content-Type", "application/json"))
        app = TestApp(object : OsJavaNetworkTransport() {
            override fun sendRequest(method: String, url: String, timeoutMs: Long, headers: MutableMap<String, String>, body: String): Response {
                var result = ""
                if (url.endsWith("/providers/${Credentials.IdentityProvider.EMAIL_PASSWORD.id}/login")) {
                    result = """
                        {
                            "access_token": "eyJhbGciOiJSUzI1NiIsImtpZCI6IjVlNjk2M2RmYWZlYTYzMjU0NTgxYzAyNiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE1ODM5NjcyMDgsImlhdCI6MTU4Mzk2NTQwOCwiaXNzIjoiNWU2OTY0ZTBhZmVhNjMyNTQ1ODFjMWEzIiwic3RpdGNoX2RldklkIjoiMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwIiwic3RpdGNoX2RvbWFpbklkIjoiNWU2OTYzZGVhZmVhNjMyNTQ1ODFjMDI1Iiwic3ViIjoiNWU2OTY0ZTBhZmVhNjMyNTQ1ODFjMWExIiwidHlwIjoiYWNjZXNzIn0.J4mp8LnlsxTQRV_7W2Er4qY0tptR76PJGG1k6HSMmUYqgfpJC2Fnbcf1VCoebzoNolH2-sr8AHDVBBCyjxRjqoY9OudFHmWZKmhDV1ysxPP4XmID0nUuN45qJSO8QEAqoOmP1crXjrUZWedFw8aaCZE-bxYfvcDHyjBcbNKZqzawwUw2PyTOlrNjgs01k2J4o5a5XzYkEsJuzr4_8UqKW6zXvYj24UtqnqoYatW5EzpX63m2qig8AcBwPK4ZHb5wEEUdf4QZxkRY5QmTgRHP8SSqVUB_mkHgKaizC_tSB3E0BekaDfLyWVC1taAstXJNfzgFtLI86AzuXS2dCiCfqQ",
                            "refresh_token": "eyJhbGciOiJSUzI1NiIsImtpZCI6IjVlNjk2M2RmYWZlYTYzMjU0NTgxYzAyNiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE1ODkxNDk0MDgsImlhdCI6MTU4Mzk2NTQwOCwic3RpdGNoX2RhdGEiOm51bGwsInN0aXRjaF9kZXZJZCI6IjAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMCIsInN0aXRjaF9kb21haW5JZCI6IjVlNjk2M2RlYWZlYTYzMjU0NTgxYzAyNSIsInN0aXRjaF9pZCI6IjVlNjk2NGUwYWZlYTYzMjU0NTgxYzFhMyIsInN0aXRjaF9pZGVudCI6eyJpZCI6IjVlNjk2NGUwYWZlYTYzMjU0NTgxYzFhMC1oaWF2b3ZkbmJxbGNsYXBwYnl1cmJpaW8iLCJwcm92aWRlcl90eXBlIjoiYW5vbi11c2VyIiwicHJvdmlkZXJfaWQiOiI1ZTY5NjNlMGFmZWE2MzI1NDU4MWMwNGEifSwic3ViIjoiNWU2OTY0ZTBhZmVhNjMyNTQ1ODFjMWExIiwidHlwIjoicmVmcmVzaCJ9.FhLdpmL48Mw0SyUKWuaplz3wfeS8TCO8S7I9pIJenQww9nPqQ7lIvykQxjCCtinGvsZIJKt_7R31xYCq4Jp53Nw81By79IwkXtO7VXHPsXXZG5_2xV-s0u44e85sYD5su_H-xnx03sU2piJbWJLSB8dKu3rMD4mO-S0HNXCCAty-JkYKSaM2-d_nS8MNb6k7Vfm7y69iz_uwHc-bb_1rPg7r827K6DEeEMF41Hy3Nx1kCdAUOM9-6nYv3pZSU1PFrGYi2uyTXPJ7R7HigY5IGHWd0hwONb_NUr4An2omqfvlkLEd77ut4V9m6mExFkoKzRz7shzn-IGkh3e4h7ECGA",
                            "user_id": "5e6964e0afea63254581c1a1",
                            "device_id": "000000000000000000000000"
                        }            
                    """.trimIndent()
                } else if (url.endsWith("/auth/profile")) {
                    result = """
                        {
                            "user_id": "5e6964e0afea63254581c1a1",
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
                                "name": "NAME",
                                "email": "unique_user@domain.com",
                                "picture_url": "PICTURE_URL",
                                "first_name": "FIRST_NAME",
                                "last_name": "LAST_NAME",
                                "gender": "GENDER",
                                "birthday": "BIRTHDAY",
                                "min_age": "1",
                                "max_age": "99"
                            },
                            "type": "normal",
                            "roles": [
                                {
                                    "role_name": "GROUP_OWNER",
                                    "group_id": "5e68f51e087b1b33a53f56d5"
                                }
                            ]
                        }
                    """.trimIndent()
                } else if (url.endsWith("/location")) {
                    return Response.httpResponse(200, mapOf(), """
                        { "deployment_model" : "GLOBAL",
                          "location": "US-VA", 
                          "hostname": "http://localhost:9090",
                          "ws_hostname": "ws://localhost:9090"
                        }
                        """.trimIndent())
                } else if (url.endsWith("/providers/${Credentials.IdentityProvider.EMAIL_PASSWORD.id}/register")) {
                    result = ""
                } else {
                    fail("Unexpected request url: $url")
                }
                return Response.httpResponse(200, successHeaders, result)
            }
        })
    }

    @After
    fun tearDown() {
        if (this::app.isInitialized) {
            app.close()
        }
    }

    @Test
    fun getUserId() {
        val user: User = app.registerUserAndLogin(TestHelper.getRandomEmail(), "123456")
        assertEquals("5e6964e0afea63254581c1a1", user.id)
    }

    @Test
    fun getDeviceId() {
        val user: User = app.registerUserAndLogin(TestHelper.getRandomEmail(), "123456")
        assertEquals("000000000000000000000000", user.deviceId)
    }

    @Test
    fun accessToken() {
        val user: User = app.registerUserAndLogin(TestHelper.getRandomEmail(), "123456")
        assertEquals("eyJhbGciOiJSUzI1NiIsImtpZCI6IjVlNjk2M2RmYWZlYTYzMjU0NTgxYzAyNiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE1ODM5NjcyMDgsImlhdCI6MTU4Mzk2NTQwOCwiaXNzIjoiNWU2OTY0ZTBhZmVhNjMyNTQ1ODFjMWEzIiwic3RpdGNoX2RldklkIjoiMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwIiwic3RpdGNoX2RvbWFpbklkIjoiNWU2OTYzZGVhZmVhNjMyNTQ1ODFjMDI1Iiwic3ViIjoiNWU2OTY0ZTBhZmVhNjMyNTQ1ODFjMWExIiwidHlwIjoiYWNjZXNzIn0.J4mp8LnlsxTQRV_7W2Er4qY0tptR76PJGG1k6HSMmUYqgfpJC2Fnbcf1VCoebzoNolH2-sr8AHDVBBCyjxRjqoY9OudFHmWZKmhDV1ysxPP4XmID0nUuN45qJSO8QEAqoOmP1crXjrUZWedFw8aaCZE-bxYfvcDHyjBcbNKZqzawwUw2PyTOlrNjgs01k2J4o5a5XzYkEsJuzr4_8UqKW6zXvYj24UtqnqoYatW5EzpX63m2qig8AcBwPK4ZHb5wEEUdf4QZxkRY5QmTgRHP8SSqVUB_mkHgKaizC_tSB3E0BekaDfLyWVC1taAstXJNfzgFtLI86AzuXS2dCiCfqQ",
                user.accessToken)
    }

    @Test
    fun refreshToken() {
        val user: User = app.registerUserAndLogin(TestHelper.getRandomEmail(), "123456")
        assertEquals("eyJhbGciOiJSUzI1NiIsImtpZCI6IjVlNjk2M2RmYWZlYTYzMjU0NTgxYzAyNiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE1ODkxNDk0MDgsImlhdCI6MTU4Mzk2NTQwOCwic3RpdGNoX2RhdGEiOm51bGwsInN0aXRjaF9kZXZJZCI6IjAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMCIsInN0aXRjaF9kb21haW5JZCI6IjVlNjk2M2RlYWZlYTYzMjU0NTgxYzAyNSIsInN0aXRjaF9pZCI6IjVlNjk2NGUwYWZlYTYzMjU0NTgxYzFhMyIsInN0aXRjaF9pZGVudCI6eyJpZCI6IjVlNjk2NGUwYWZlYTYzMjU0NTgxYzFhMC1oaWF2b3ZkbmJxbGNsYXBwYnl1cmJpaW8iLCJwcm92aWRlcl90eXBlIjoiYW5vbi11c2VyIiwicHJvdmlkZXJfaWQiOiI1ZTY5NjNlMGFmZWE2MzI1NDU4MWMwNGEifSwic3ViIjoiNWU2OTY0ZTBhZmVhNjMyNTQ1ODFjMWExIiwidHlwIjoicmVmcmVzaCJ9.FhLdpmL48Mw0SyUKWuaplz3wfeS8TCO8S7I9pIJenQww9nPqQ7lIvykQxjCCtinGvsZIJKt_7R31xYCq4Jp53Nw81By79IwkXtO7VXHPsXXZG5_2xV-s0u44e85sYD5su_H-xnx03sU2piJbWJLSB8dKu3rMD4mO-S0HNXCCAty-JkYKSaM2-d_nS8MNb6k7Vfm7y69iz_uwHc-bb_1rPg7r827K6DEeEMF41Hy3Nx1kCdAUOM9-6nYv3pZSU1PFrGYi2uyTXPJ7R7HigY5IGHWd0hwONb_NUr4An2omqfvlkLEd77ut4V9m6mExFkoKzRz7shzn-IGkh3e4h7ECGA",
                user.refreshToken)
    }

    @Test
    fun getName() {
        val user: User = app.registerUserAndLogin(TestHelper.getRandomEmail(), "123456")
        assertEquals("NAME", user.name)
    }

    @Test
    fun getEmail() {
        val user: User = app.registerUserAndLogin(TestHelper.getRandomEmail(), "123456")
        assertEquals("unique_user@domain.com", user.email)
    }

    @Test
    fun getFirstName() {
        val user: User = app.registerUserAndLogin(TestHelper.getRandomEmail(), "123456")
        assertEquals("FIRST_NAME", user.firstName)
    }

    @Test
    fun getLastName() {
        val user: User = app.registerUserAndLogin(TestHelper.getRandomEmail(), "123456")
        assertEquals("LAST_NAME", user.lastName)
    }

    @Test
    fun getBirthday() {
        val user: User = app.registerUserAndLogin(TestHelper.getRandomEmail(), "123456")
        assertEquals("BIRTHDAY", user.birthday)
    }

    @Test
    fun getGender() {
        val user: User = app.registerUserAndLogin(TestHelper.getRandomEmail(), "123456")
        assertEquals("GENDER", user.gender)
    }

    @Test
    fun getMinAge() {
        val user: User = app.registerUserAndLogin(TestHelper.getRandomEmail(), "123456")
        assertEquals(1L, user.minAge)
    }

    @Test
    fun getMaxAge() {
        val user: User = app.registerUserAndLogin(TestHelper.getRandomEmail(), "123456")
        assertEquals(99L, user.maxAge)
    }

    @Test
    fun getPictureUrl() {
        val user: User = app.registerUserAndLogin(TestHelper.getRandomEmail(), "123456")
        assertEquals("PICTURE_URL", user.pictureUrl)
    }

    @Test
    fun getProviderType() {
        val user: User = app.registerUserAndLogin(TestHelper.getRandomEmail(), "123456")
        assertEquals("local-userpass", user.providerType)
    }
}
