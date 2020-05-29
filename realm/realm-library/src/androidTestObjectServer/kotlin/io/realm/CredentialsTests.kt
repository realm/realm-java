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
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.realm.mongodb.ErrorCode
import io.realm.mongodb.ObjectServerError
import io.realm.mongodb.App
import io.realm.mongodb.Credentials
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
        assertEquals("anon-user", creds.identityProvider.id)
        assertTrue(creds.asJson().contains("anon-user")) // Treat the JSON as an opaque value.
    }

    @Ignore("FIXME: Awaiting ObjectStore support")
    @Test
    fun apiKey() {
        val creds = Credentials.apiKey("token")
        assertEquals("anon-user", creds.identityProvider.id)
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
        assertEquals("oauth2-apple", creds.identityProvider.id)
        assertTrue(creds.asJson().contains("apple-token")) // Treat the JSON as a largely opaque value.
    }

    @Test
    fun apple_invalidInput() {
        assertFailsWith<IllegalArgumentException> { Credentials.apple("") }
        assertFailsWith<IllegalArgumentException> { Credentials.apple(TestHelper.getNull()) }
    }

    @Ignore("FIXME: Awaiting ObjectStore support")
    @Test
    fun customFunction() {
        TODO()
    }

    @Ignore("FIXME: Awaiting ObjectStore support")
    @Test
    fun customFunction_invalidInput() {
        TODO()
    }

    @Test
    fun emailPassword() {
        val creds = Credentials.emailPassword("foo@bar.com", "secret")
        assertEquals("local-userpass", creds.identityProvider.id)
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
        assertEquals("oauth2-facebook", creds.identityProvider.id)
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
        assertEquals("oauth2-google", creds.identityProvider.id)
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
        val creds = Credentials.google("jwt-token")
        assertEquals("jwt", creds.identityProvider.id)
        assertTrue(creds.asJson().contains("jwt-token"))
    }

    @Test
    fun jwt_invalidInput() {
        assertFailsWith<IllegalArgumentException> { Credentials.jwt("") }
        assertFailsWith<IllegalArgumentException> { Credentials.jwt(TestHelper.getNull()) }
    }

    fun expectErrorCode(app: App, expectedCode: ErrorCode, credentials: Credentials) {
        try {
            app.login(credentials)
            fail()
        } catch (error: ObjectServerError) {
            assertEquals(expectedCode, error.errorCode)
        }
    }

    @Test
    fun loginUsingCredentials() {
        app = TestApp()
        Credentials.IdentityProvider.values().forEach { provider ->
            when(provider) {
                Credentials.IdentityProvider.ANONYMOUS -> {
                    val user = app.login(Credentials.anonymous())
                    assertNotNull(user)
                }
                Credentials.IdentityProvider.API_KEY -> {
                    // FIXME: Wait for API Key support in OS
//                        val user: User = app.registerUserAndLogin(TestHelper.getRandomEmail(), "123456")
//                        val key: UserApiKey = app.apiKeyAuthProvider.createApiKey("my-key");
//                        val apiKeyUser = app.login(Credentials.apiKey(key.value!!))
//                        assertNotNull(apiKeyUser)
                }
                Credentials.IdentityProvider.CUSTOM_FUNCTION -> {
                    // FIXME Wait for Custom Function support
                }
                Credentials.IdentityProvider.EMAIL_PASSWORD -> {
                    val email = TestHelper.getRandomEmail()
                    val password = "123456"
                    app.emailPasswordAuth.registerUser(email, password)
                    val user = app.login(Credentials.emailPassword(email, password))
                    assertNotNull(user)
                }

                // These providers are hard to test for real since they depend on a 3rd party
                // login service. Instead we attempt to login and verify that a proper exception
                // is thrown. At least that should verify that correctly formatted JSON is being
                // sent across the wire.
                Credentials.IdentityProvider.FACEBOOK -> {
                    expectErrorCode(app, ErrorCode.INVALID_SESSION, Credentials.facebook("facebook-token"))
                }
                Credentials.IdentityProvider.APPLE -> {
                    expectErrorCode(app, ErrorCode.INVALID_SESSION, Credentials.apple("apple-token"))
                }
                Credentials.IdentityProvider.GOOGLE -> {
                    expectErrorCode(app, ErrorCode.INVALID_SESSION, Credentials.google("google-token"))
                }
                Credentials.IdentityProvider.JWT ->  {
                    expectErrorCode(app, ErrorCode.INVALID_SESSION, Credentials.jwt("jwt-token"))
                }
                Credentials.IdentityProvider.UNKNOWN -> {
                    // Ignore
                }
            }
        }
    }
}
