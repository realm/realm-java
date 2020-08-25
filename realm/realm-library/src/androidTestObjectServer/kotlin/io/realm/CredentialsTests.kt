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
import io.realm.admin.ServerAdmin
import io.realm.mongodb.*
import io.realm.mongodb.auth.ApiKey
import org.bson.Document
import org.junit.After
import org.junit.Assert.*
import org.junit.BeforeClass
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertFailsWith


@RunWith(AndroidJUnit4::class)
class CredentialsTests {

    private lateinit var app: App
    private lateinit var admin: ServerAdmin

    companion object {

        @BeforeClass
        @JvmStatic
        fun setUp() {
            Realm.init(InstrumentationRegistry.getInstrumentation().targetContext)
        }
    }

    @After
    fun tearDown() {
        if (this::app.isInitialized) {
            app.close()
        }
    }

    @Test
    fun anonymous() {
        val creds = Credentials.anonymous()
        assertEquals(Credentials.Provider.ANONYMOUS, creds.identityProvider)
        assertTrue(creds.asJson().contains("anon-user")) // Treat the JSON as an opaque value.
    }

    @Test
    fun apiKey() {
        val creds = Credentials.apiKey("token")
        assertEquals(Credentials.Provider.API_KEY, creds.identityProvider)
        assertTrue(creds.asJson().contains("token")) // Treat the JSON as an opaque value.
    }

    @Test
    fun apiKey_invalidInput() {
        assertFailsWith<IllegalArgumentException> { Credentials.apiKey("") }
        assertFailsWith<IllegalArgumentException> { Credentials.apiKey(TestHelper.getNull()) }
    }

    @Test
    fun apple() {
        val creds = Credentials.apple("apple-token")
        assertEquals(Credentials.Provider.APPLE, creds.identityProvider)
        assertTrue(creds.asJson().contains("apple-token")) // Treat the JSON as a largely opaque value.
    }

    @Test
    fun apple_invalidInput() {
        assertFailsWith<IllegalArgumentException> { Credentials.apple("") }
        assertFailsWith<IllegalArgumentException> { Credentials.apple(TestHelper.getNull()) }
    }

    @Test
    fun customFunction() {
        val mail = TestHelper.getRandomEmail()
        val id = 666 + TestHelper.getRandomId()
        val creds = mapOf(
                "mail" to mail,
                "id" to id
        ).let { Credentials.customFunction(Document(it)) }
        assertEquals(Credentials.Provider.CUSTOM_FUNCTION, creds.identityProvider)
        assertTrue(creds.asJson().contains(mail))
        assertTrue(creds.asJson().contains(id.toString()))
    }

    @Test
    fun customFunction_invalidInput() {
        assertFailsWith<IllegalArgumentException> { Credentials.customFunction(null) }
    }

    @Test
    fun emailPassword() {
        val creds = Credentials.emailPassword("foo@bar.com", "secret")
        assertEquals(Credentials.Provider.EMAIL_PASSWORD, creds.identityProvider)
        // Treat the JSON as a largely opaque value.
        assertTrue(creds.asJson().contains("foo@bar.com"))
        assertTrue(creds.asJson().contains("secret"))
    }

    @Test
    fun emailPassword_invalidInput() {
        assertFailsWith<IllegalArgumentException> { Credentials.emailPassword("", "password") }
        assertFailsWith<IllegalArgumentException> { Credentials.emailPassword("email", "") }
        assertFailsWith<IllegalArgumentException> { Credentials.emailPassword(TestHelper.getNull(), "password") }
        assertFailsWith<IllegalArgumentException> { Credentials.emailPassword("email", TestHelper.getNull()) }
    }

    @Test
    fun facebook() {
        val creds = Credentials.facebook("fb-token")
        assertEquals(Credentials.Provider.FACEBOOK, creds.identityProvider)
        assertTrue(creds.asJson().contains("fb-token"))
    }

    @Test
    fun facebook_invalidInput() {
        assertFailsWith<IllegalArgumentException> { Credentials.facebook("") }
        assertFailsWith<IllegalArgumentException> { Credentials.facebook(TestHelper.getNull()) }
    }

    @Test
    fun google() {
        val creds = Credentials.google("google-token")
        assertEquals(Credentials.Provider.GOOGLE, creds.identityProvider)
        assertTrue(creds.asJson().contains("google-token"))
    }

    @Test
    fun google_invalidInput() {
        assertFailsWith<IllegalArgumentException> { Credentials.google("") }
        assertFailsWith<IllegalArgumentException> { Credentials.google(TestHelper.getNull()) }
    }

    @Ignore("FIXME: Awaiting ObjectStore support")
    @Test
    fun jwt() {
        val creds = Credentials.jwt("jwt-token")
        assertEquals(Credentials.Provider.JWT, creds.identityProvider)
        assertTrue(creds.asJson().contains("jwt-token"))
    }

    @Test
    fun jwt_invalidInput() {
        assertFailsWith<IllegalArgumentException> { Credentials.jwt("") }
        assertFailsWith<IllegalArgumentException> { Credentials.jwt(TestHelper.getNull()) }
    }

    @Test
    fun loginUsingCredentials() {
        app = TestApp()
        admin = ServerAdmin(app)
        Credentials.Provider.values().forEach { provider ->
            when (provider) {
                Credentials.Provider.ANONYMOUS -> {
                    val user = app.login(Credentials.anonymous())
                    assertNotNull(user)
                }
                Credentials.Provider.API_KEY -> {
                    // Log in, create an API key, log out, log in with the key, compare users
                    val user: User = app.registerUserAndLogin(TestHelper.getRandomEmail(), "123456")
                    val key: ApiKey = user.apiKeys.create("my-key");
                    user.logOut()
                    val apiKeyUser = app.login(Credentials.apiKey(key.value!!))
                    assertEquals(user.id, apiKeyUser.id)
                }
                Credentials.Provider.CUSTOM_FUNCTION -> {
                    val customFunction = mapOf(
                            "mail" to TestHelper.getRandomEmail(),
                            "id" to 666 + TestHelper.getRandomId()
                    ).let {
                        Credentials.customFunction(Document(it))
                    }

                    // We are not testing the authentication function itself, but rather that the
                    // credentials work
                    val functionUser = app.login(customFunction)
                    assertNotNull(functionUser)
                }
                Credentials.Provider.EMAIL_PASSWORD -> {
                    val email = TestHelper.getRandomEmail()
                    val password = "123456"
                    app.emailPassword.registerUser(email, password)
                    val user = app.login(Credentials.emailPassword(email, password))
                    assertNotNull(user)
                }

                // These providers are hard to test for real since they depend on a 3rd party
                // login service. Instead we attempt to login and verify that a proper exception
                // is thrown. At least that should verify that correctly formatted JSON is being
                // sent across the wire.
                Credentials.Provider.FACEBOOK -> {
                    expectErrorCode(app, ErrorCode.INVALID_SESSION, Credentials.facebook("facebook-token"))
                }
                Credentials.Provider.APPLE -> {
                    expectErrorCode(app, ErrorCode.INVALID_SESSION, Credentials.apple("apple-token"))
                }
                Credentials.Provider.GOOGLE -> {
                    expectErrorCode(app, ErrorCode.INVALID_SESSION, Credentials.google("google-token"))
                }
                Credentials.Provider.JWT -> {
                    expectErrorCode(app, ErrorCode.INVALID_SESSION, Credentials.jwt("jwt-token"))
                }
                Credentials.Provider.UNKNOWN -> {
                    // Ignore
                }
            }
        }
    }

    private fun expectErrorCode(app: App, expectedCode: ErrorCode, credentials: Credentials) {
        try {
            app.login(credentials)
            fail()
        } catch (error: AppException) {
            assertEquals(expectedCode, error.errorCode)
        }
    }
}
