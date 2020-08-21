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
import io.realm.internal.network.OkHttpNetworkTransport
import io.realm.internal.objectstore.OsJavaNetworkTransport
import io.realm.mongodb.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertNull

@RunWith(AndroidJUnit4::class)
class UserMetadataTests {
    companion object {
        const val ACCESS_TOKEN = """eyJhbGciOiJSUzI1NiIsImtpZCI6IjVlNjk2M2RmYWZlYTYzMjU0NTgxYzAyNiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE1ODM5NjcyMDgsImlhdCI6MTU4Mzk2NTQwOCwiaXNzIjoiNWU2OTY0ZTBhZmVhNjMyNTQ1ODFjMWEzIiwic3RpdGNoX2RldklkIjoiMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwIiwic3RpdGNoX2RvbWFpbklkIjoiNWU2OTYzZGVhZmVhNjMyNTQ1ODFjMDI1Iiwic3ViIjoiNWU2OTY0ZTBhZmVhNjMyNTQ1ODFjMWExIiwidHlwIjoiYWNjZXNzIn0.J4mp8LnlsxTQRV_7W2Er4qY0tptR76PJGG1k6HSMmUYqgfpJC2Fnbcf1VCoebzoNolH2-sr8AHDVBBCyjxRjqoY9OudFHmWZKmhDV1ysxPP4XmID0nUuN45qJSO8QEAqoOmP1crXjrUZWedFw8aaCZE-bxYfvcDHyjBcbNKZqzawwUw2PyTOlrNjgs01k2J4o5a5XzYkEsJuzr4_8UqKW6zXvYj24UtqnqoYatW5EzpX63m2qig8AcBwPK4ZHb5wEEUdf4QZxkRY5QmTgRHP8SSqVUB_mkHgKaizC_tSB3E0BekaDfLyWVC1taAstXJNfzgFtLI86AzuXS2dCiCfqQ"""
        const val REFRESH_TOKEN = """eyJhbGciOiJSUzI1NiIsImtpZCI6IjVlNjk2M2RmYWZlYTYzMjU0NTgxYzAyNiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE1ODkxNDk0MDgsImlhdCI6MTU4Mzk2NTQwOCwic3RpdGNoX2RhdGEiOm51bGwsInN0aXRjaF9kZXZJZCI6IjAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMCIsInN0aXRjaF9kb21haW5JZCI6IjVlNjk2M2RlYWZlYTYzMjU0NTgxYzAyNSIsInN0aXRjaF9pZCI6IjVlNjk2NGUwYWZlYTYzMjU0NTgxYzFhMyIsInN0aXRjaF9pZGVudCI6eyJpZCI6IjVlNjk2NGUwYWZlYTYzMjU0NTgxYzFhMC1oaWF2b3ZkbmJxbGNsYXBwYnl1cmJpaW8iLCJwcm92aWRlcl90eXBlIjoiYW5vbi11c2VyIiwicHJvdmlkZXJfaWQiOiI1ZTY5NjNlMGFmZWE2MzI1NDU4MWMwNGEifSwic3ViIjoiNWU2OTY0ZTBhZmVhNjMyNTQ1ODFjMWExIiwidHlwIjoicmVmcmVzaCJ9.FhLdpmL48Mw0SyUKWuaplz3wfeS8TCO8S7I9pIJenQww9nPqQ7lIvykQxjCCtinGvsZIJKt_7R31xYCq4Jp53Nw81By79IwkXtO7VXHPsXXZG5_2xV-s0u44e85sYD5su_H-xnx03sU2piJbWJLSB8dKu3rMD4mO-S0HNXCCAty-JkYKSaM2-d_nS8MNb6k7Vfm7y69iz_uwHc-bb_1rPg7r827K6DEeEMF41Hy3Nx1kCdAUOM9-6nYv3pZSU1PFrGYi2uyTXPJ7R7HigY5IGHWd0hwONb_NUr4An2omqfvlkLEd77ut4V9m6mExFkoKzRz7shzn-IGkh3e4h7ECGA"""
        const val USER_ID = "5e6964e0afea63254581c1a1"
        const val DEVICE_ID = "000000000000000000000000"
        const val NAME = "NAME"
        const val EMAIL = "unique_user@domain.com"
        const val PICTURE_URL = "PICTURE_URL"
        const val FIRST_NAME = "FIRST_NAME"
        const val LAST_NAME = "LAST_NAME"
        const val GENDER = "GENDER"
        const val BIRTHDAY = "BIRTHDAY"
        const val MIN_AGE = 1L
        const val MAX_AGE = 99L
    }

    private lateinit var app: App
    lateinit var profileBody : String

    private fun setDefaultProfile(){
        profileBody = """
                            {
                                "name": "$NAME",
                                "email": "$EMAIL",
                                "picture_url": "$PICTURE_URL",
                                "first_name": "$FIRST_NAME",
                                "last_name": "$LAST_NAME",
                                "gender": "$GENDER",
                                "birthday": "$BIRTHDAY",
                                "min_age": "$MIN_AGE",
                                "max_age": "$MAX_AGE"
                            }
        """.trimIndent()
    }

    private fun setNullProfile(){
        profileBody = """
                            {

                            }
        """.trimIndent()
    }

    @Before
    fun setUp() {
        Realm.init(InstrumentationRegistry.getInstrumentation().targetContext)

        setDefaultProfile();

        app = TestApp(object : OsJavaNetworkTransport() {
            override fun sendRequest(method: String, url: String, timeoutMs: Long, headers: MutableMap<String, String>, body: String): Response {
                var result = ""
                if (url.endsWith("/providers/${Credentials.IdentityProvider.EMAIL_PASSWORD.id}/login")) {
                    result = """
                        {
                            "access_token": "$ACCESS_TOKEN",
                            "refresh_token": "$REFRESH_TOKEN",
                            "user_id": "$USER_ID",
                            "device_id": "$DEVICE_ID"
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
                            "data": $profileBody,
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
                    return OkHttpNetworkTransport.Response.httpResponse(200, mapOf(), """
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
                return OkHttpNetworkTransport.Response.httpResponse(200, mapOf(Pair("Content-Type", "application/json")), result)
            }

            override fun sendStreamingRequest(request: Request): Response? {
                return null
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
        assertEquals(USER_ID, user.id)
    }

    @Test
    fun getDeviceId() {
        val user: User = app.registerUserAndLogin(TestHelper.getRandomEmail(), "123456")
        assertEquals(DEVICE_ID, user.deviceId)
    }

    @Test
    fun accessToken() {
        val user: User = app.registerUserAndLogin(TestHelper.getRandomEmail(), "123456")
        assertEquals(ACCESS_TOKEN, user.accessToken)
    }

    @Test
    fun refreshToken() {
        val user: User = app.registerUserAndLogin(TestHelper.getRandomEmail(), "123456")
        assertEquals(REFRESH_TOKEN, user.refreshToken)
    }

    @Test
    fun getName() {
        val user: User = app.registerUserAndLogin(TestHelper.getRandomEmail(), "123456")
        assertEquals(NAME, user.name)
    }

    @Test
    fun getEmail() {
        val user: User = app.registerUserAndLogin(TestHelper.getRandomEmail(), "123456")
        assertEquals(EMAIL, user.email)
    }

    @Test
    fun getFirstName() {
        val user: User = app.registerUserAndLogin(TestHelper.getRandomEmail(), "123456")
        assertEquals(FIRST_NAME, user.firstName)
    }

    @Test
    fun getLastName() {
        val user: User = app.registerUserAndLogin(TestHelper.getRandomEmail(), "123456")
        assertEquals(LAST_NAME, user.lastName)
    }

    @Test
    fun getBirthday() {
        val user: User = app.registerUserAndLogin(TestHelper.getRandomEmail(), "123456")
        assertEquals(BIRTHDAY, user.birthday)
    }

    @Test
    fun getGender() {
        val user: User = app.registerUserAndLogin(TestHelper.getRandomEmail(), "123456")
        assertEquals(GENDER, user.gender)
    }

    @Test
    fun getMinAge() {
        val user: User = app.registerUserAndLogin(TestHelper.getRandomEmail(), "123456")
        assertEquals(MIN_AGE, user.minAge)
    }

    @Test
    fun getMaxAge() {
        val user: User = app.registerUserAndLogin(TestHelper.getRandomEmail(), "123456")
        assertEquals(MAX_AGE, user.maxAge)
    }

    @Test
    fun getPictureUrl() {
        val user: User = app.registerUserAndLogin(TestHelper.getRandomEmail(), "123456")
        assertEquals(PICTURE_URL, user.pictureUrl)
    }

    @Test
    fun getNameNullable() {
        setNullProfile()

        val user: User = app.registerUserAndLogin(TestHelper.getRandomEmail(), "123456")
        assertNull(user.name)
    }

    @Test
    fun getEmailNullable() {
        setNullProfile()

        val user: User = app.registerUserAndLogin(TestHelper.getRandomEmail(), "123456")
        assertNull(user.email)
    }

    @Test
    fun getFirstNameNullable() {
        setNullProfile()

        val user: User = app.registerUserAndLogin(TestHelper.getRandomEmail(), "123456")
        assertNull(user.firstName)
    }

    @Test
    fun getLastNameNullable() {
        setNullProfile()

        val user: User = app.registerUserAndLogin(TestHelper.getRandomEmail(), "123456")
        assertNull(user.lastName)
    }

    @Test
    fun getBirthdayNullable() {
        setNullProfile()

        val user: User = app.registerUserAndLogin(TestHelper.getRandomEmail(), "123456")
        assertNull(user.birthday)
    }

    @Test
    fun getGenderNullable() {
        setNullProfile()

        val user: User = app.registerUserAndLogin(TestHelper.getRandomEmail(), "123456")
        assertNull(user.gender)
    }

    @Test
    fun getMinAgeNullable() {
        setNullProfile()

        val user: User = app.registerUserAndLogin(TestHelper.getRandomEmail(), "123456")
        assertNull(user.minAge)
    }

    @Test
    fun getMaxAgeNullable() {
        setNullProfile()

        val user: User = app.registerUserAndLogin(TestHelper.getRandomEmail(), "123456")
        assertNull(user.maxAge)
    }

    @Test
    fun getPictureUrlNullable() {
        setNullProfile()

        val user: User = app.registerUserAndLogin(TestHelper.getRandomEmail(), "123456")
        assertNull(user.pictureUrl)
    }

    @Test
    fun getProviderType() {
        val user: User = app.registerUserAndLogin(TestHelper.getRandomEmail(), "123456")
        assertEquals(Credentials.IdentityProvider.EMAIL_PASSWORD, user.providerType)
    }
}
