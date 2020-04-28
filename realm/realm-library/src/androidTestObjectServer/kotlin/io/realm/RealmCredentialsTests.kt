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
import org.junit.After
import org.junit.Assert.*
import org.junit.BeforeClass
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertFailsWith

@RunWith(AndroidJUnit4::class)
class RealmCredentialsTests {

    private lateinit var app: RealmApp


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
        val creds = RealmCredentials.anonymous()
        assertEquals("anon-user", creds.identityProvider.id)
        assertTrue(creds.asJson().contains("anon-user")) // Treat the JSON as an opaque value.
    }

    @Ignore("FIXME: Awaiting ObjectStore support")
    @Test
    fun apiKey() {
        val creds = RealmCredentials.apiKey("token")
        assertEquals("anon-user", creds.identityProvider.id)
        assertTrue(creds.asJson().contains("token")) // Treat the JSON as an opaque value.
    }

    @Test
    fun apiKey_invalidInput() {
        assertFailsWith<IllegalArgumentException> { RealmCredentials.apiKey("") }
        assertFailsWith<IllegalArgumentException> { RealmCredentials.apiKey(TestHelper.getNull()) }
    }

    @Test
    fun apple() {
        val creds = RealmCredentials.apple("apple-token")
        assertEquals("oauth2-apple", creds.identityProvider.id)
        assertTrue(creds.asJson().contains("apple-token")) // Treat the JSON as a largely opaque value.
    }

    @Test
    fun apple_invalidInput() {
        assertFailsWith<IllegalArgumentException> { RealmCredentials.apple("") }
        assertFailsWith<IllegalArgumentException> { RealmCredentials.apple(TestHelper.getNull()) }
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
        val creds = RealmCredentials.emailPassword("foo@bar.com", "secret")
        assertEquals("local-userpass", creds.identityProvider.id)
        // Treat the JSON as a largely opaque value.
        assertTrue(creds.asJson().contains("foo@bar.com"))
        assertTrue(creds.asJson().contains("secret"))
    }

    @Test
    fun emailPassword_invalidInput() {
        assertFailsWith<IllegalArgumentException> { RealmCredentials.emailPassword("", "password") }
        assertFailsWith<IllegalArgumentException> { RealmCredentials.emailPassword("email", "") }
        assertFailsWith<IllegalArgumentException> { RealmCredentials.emailPassword(TestHelper.getNull(), "password") }
        assertFailsWith<IllegalArgumentException> { RealmCredentials.emailPassword("email", TestHelper.getNull()) }
    }

    @Test
    fun facebook() {
        val creds = RealmCredentials.facebook("fb-token")
        assertEquals("oauth2-facebook", creds.identityProvider.id)
        assertTrue(creds.asJson().contains("fb-token"))
    }

    @Test
    fun facebook_invalidInput() {
        assertFailsWith<IllegalArgumentException> { RealmCredentials.facebook("") }
        assertFailsWith<IllegalArgumentException> { RealmCredentials.facebook(TestHelper.getNull()) }
    }

    @Test
    fun google() {
        val creds = RealmCredentials.google("google-token")
        assertEquals("oauth2-google", creds.identityProvider.id)
        assertTrue(creds.asJson().contains("google-token"))
    }

    @Test
    fun google_invalidInput() {
        assertFailsWith<IllegalArgumentException> { RealmCredentials.google("") }
        assertFailsWith<IllegalArgumentException> { RealmCredentials.google(TestHelper.getNull()) }
    }

    @Ignore("FIXME: Awaiting ObjectStore support")
    @Test
    fun jwt() {
        val creds = RealmCredentials.google("jwt-token")
        assertEquals("jwt", creds.identityProvider.id)
        assertTrue(creds.asJson().contains("jwt-token"))
    }

    @Test
    fun jwt_invalidInput() {
        assertFailsWith<IllegalArgumentException> { RealmCredentials.jwt("") }
        assertFailsWith<IllegalArgumentException> { RealmCredentials.jwt(TestHelper.getNull()) }
    }

    fun expectErrorCode(app: RealmApp, expectedCode: ErrorCode, credentials: RealmCredentials) {
        try {
            app.login(credentials)
            fail()
        } catch (error: ObjectServerError) {
            assertEquals(expectedCode, error.errorCode)
        }
    }

    @Test
    fun loginUsingCredentials() {
        app = TestRealmApp()
        RealmCredentials.IdentityProvider.values().forEach { provider ->
            when(provider) {
                RealmCredentials.IdentityProvider.ANONYMOUS -> {
                    val user = app.login(RealmCredentials.anonymous())
                    assertNotNull(user)
                }
                RealmCredentials.IdentityProvider.API_KEY -> {
                    // FIXME: Wait for API Key support in OS
//                        val user: RealmUser = app.registerUserAndLogin(TestHelper.getRandomEmail(), "123456")
//                        val key: RealmUserApiKey = app.apiKeyAuthProvider.createApiKey("my-key");
//                        val apiKeyUser = app.login(RealmCredentials.apiKey(key.value!!))
//                        assertNotNull(apiKeyUser)
                }
                RealmCredentials.IdentityProvider.CUSTOM_FUNCTION -> {
                    // FIXME Wait for Custom Function support
                }
                RealmCredentials.IdentityProvider.EMAIL_PASSWORD -> {
                    val email = TestHelper.getRandomEmail()
                    val password = "123456"
                    app.emailPasswordAuth.registerUser(email, password)
                    val user = app.login(RealmCredentials.emailPassword(email, password))
                    assertNotNull(user)
                }

                // These providers are hard to test for real since they depend on a 3rd party
                // login service. Instead we attempt to login and verify that a proper exception
                // is thrown. At least that should verify that correctly formatted JSON is being
                // sent across the wire.
                RealmCredentials.IdentityProvider.FACEBOOK -> {
                    expectErrorCode(app, ErrorCode.INVALID_SESSION, RealmCredentials.facebook("facebook-token"))
                }
                RealmCredentials.IdentityProvider.APPLE -> {
                    expectErrorCode(app, ErrorCode.INVALID_SESSION, RealmCredentials.apple("apple-token"))
                }
                RealmCredentials.IdentityProvider.GOOGLE -> {
                    expectErrorCode(app, ErrorCode.INVALID_SESSION, RealmCredentials.google("google-token"))
                }
                RealmCredentials.IdentityProvider.JWT ->  {
                    expectErrorCode(app, ErrorCode.INVALID_SESSION, RealmCredentials.jwt("jwt-token"))
                }
                RealmCredentials.IdentityProvider.UNKNOWN -> {
                    // Ignore
                }
            }
        }
    }
}