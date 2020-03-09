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
import org.junit.Assert.*
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RealmCredentialsTests {

    companion object {
        @BeforeClass
        @JvmStatic
        fun setUp() {
            Realm.init(InstrumentationRegistry.getInstrumentation().targetContext)
        }
    }

    @Test
    fun anonymous() {
        val creds = RealmCredentials.anonymous()
        assertEquals("anon-user", creds.identityProvider)
        assertNotNull(creds.asJson()) // Treat the JSON as an opaque value.
    }

    @Test
    fun apiKey() {
        TODO()
    }

    @Test
    fun apiKey_invalidInput() {
        TODO()
    }

    @Test
    fun apple() {
        val creds = RealmCredentials.apple("apple-token")
        assertEquals("oauth2-apple", creds.identityProvider)
        assertTrue(creds.asJson().contains("apple-token")) // Treat the JSON as a largely opaque value.
    }

    @Test
    fun apple_invalidInput() {
        try {
            RealmCredentials.apple("")
        } catch (ignored: IllegalArgumentException) {
        }
    }

    @Test
    fun customFunction() {
        TODO()
    }

    @Test
    fun customFunction_invalidInput() {
        TODO()
    }

    @Test
    fun emailPassword() {
        val creds = RealmCredentials.emailPassword("foo@bar.com", "secret")
        assertEquals("local-userpass", creds.identityProvider)
        // Treat the JSON as a largely opaque value.
        assertTrue(creds.asJson().contains("foo@bar.com"))
        assertTrue(creds.asJson().contains("secret"))
    }

    @Test
    fun emailPassword_invalidInput() {
        TODO()
    }

    @Test
    fun facebook() {
        val creds = RealmCredentials.facebook("fb-token")
        assertEquals("oauth2-facebook", creds.identityProvider)
        assertTrue(creds.asJson().contains("fb-token"))
    }

    @Test
    fun facebook_invalidInput() {
        try {
            RealmCredentials.facebook("")
        } catch (ignored: IllegalArgumentException) {
        }
    }

    @Test
    fun google() {
        val creds = RealmCredentials.google("google-token")
        assertEquals("google", creds.identityProvider)
        assertTrue(creds.asJson().contains("google-token"))
    }

    @Test
    fun google_invalidInput() {
        try {
            RealmCredentials.google("")
        } catch (ignored: IllegalArgumentException) {
        }
    }

    @Test
    fun jwt() {
        val creds = RealmCredentials.google("jwt-token")
        assertEquals("jwt", creds.identityProvider)
        assertTrue(creds.asJson().contains("jwt-token"))
    }

    @Test
    fun jwt_invalidInput() {
        try {
            RealmCredentials.google("")
        } catch (ignored: IllegalArgumentException) {
        }
    }

}