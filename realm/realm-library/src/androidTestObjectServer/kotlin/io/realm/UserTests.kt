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
import io.realm.mongodb.auth.ApiKeyAuth
import io.realm.rule.BlockingLooperThread
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import java.lang.IllegalArgumentException

@RunWith(AndroidJUnit4::class)
class UserTests {

    val looperThread = BlockingLooperThread()

    private lateinit var app: App
    private lateinit var anonUser: User
    private lateinit var admin: ServerAdmin

    @Before
    fun setUp() {
        Realm.init(InstrumentationRegistry.getInstrumentation().targetContext)
        app = TestApp()
        admin = ServerAdmin()
        anonUser = app.login(Credentials.anonymous())
    }

    @After
    fun tearDown() {
        if (this::app.isInitialized) {
            app.close()
        }
    }

    @Test
    fun getApp() {
        assertEquals(app, anonUser.app)
    }

    @Test
    fun getState_anonymousUser() {
        assertEquals(User.State.LOGGED_IN, anonUser.state)
        anonUser.logOut()
        assertEquals(User.State.REMOVED, anonUser.state)
    }

    @Test
    fun getState_emailUser() {
        val emailUser = app.registerUserAndLogin(TestHelper.getRandomEmail(), "123456")
        assertEquals(User.State.LOGGED_IN, emailUser.state)
        emailUser.logOut()
        assertEquals(User.State.LOGGED_OUT, emailUser.state)
        emailUser.remove()
        assertEquals(User.State.REMOVED, emailUser.state)
    }

    @Test
    fun logOut() {
        anonUser.logOut(); // Remove user created for other tests

        // Anonymous users are removed upon log out
        val user1: User = app.login(Credentials.anonymous())
        assertEquals(user1, app.currentUser())
        user1.logOut()
        assertEquals(User.State.REMOVED, user1.state)
        assertNull(app.currentUser())

        // Users registered with Email/Password will register as Logged Out
        val user2: User = app.registerUserAndLogin(TestHelper.getRandomEmail(), "123456")
        assertEquals(user2, app.currentUser())
        user2.logOut()
        assertEquals(User.State.LOGGED_OUT, user2.state)
        assertNull(app.currentUser())
    }

    @Test
    fun logOutAsync() = looperThread.runBlocking {
        assertEquals(anonUser, app.currentUser())
        anonUser.logOutAsync() { result ->
            val callbackUser: User = result.orThrow
            assertNull(app.currentUser())
            assertEquals(anonUser, callbackUser)
            assertEquals(User.State.REMOVED, anonUser.state)
            assertEquals(User.State.REMOVED, callbackUser.state)
            looperThread.testComplete()
        }
    }

    @Test
    fun logOutAsync_throwsOnNonLooperThread() {
        val user: User = app.login(Credentials.anonymous())
        try {
            user.logOutAsync { fail() }
            fail()
        } catch (ignore: IllegalStateException) {
        }
    }

    @Ignore("FIXME: Wait for linkUser support in ObjectStore")
    @Test
    fun linkUser() {
        admin.setAutomaticConfirmation(enabled = false)
        val anonUser: User = app.login(Credentials.anonymous())
        assertEquals(1, anonUser.identities.size)

        val email = TestHelper.getRandomEmail()
        val password = "123456"
        app.emailPasswordAuth.registerUser(email, password) // TODO: Test what happens if auto-confirm is enabled
        var linkedUser: User = anonUser.linkCredentials(Credentials.emailPassword(email, password))
        assertTrue(anonUser === linkedUser)
        assertEquals(2, linkedUser.identities.size)
        assertEquals(Credentials.IdentityProvider.EMAIL_PASSWORD, linkedUser.identities[1].provider)
        admin.setAutomaticConfirmation(enabled = true)

        val otherEmail = TestHelper.getRandomEmail()
        val otherPassword = "123456"
        app.emailPasswordAuth.registerUser(otherEmail, otherPassword)
        linkedUser = anonUser.linkCredentials(Credentials.emailPassword(email, password))
        assertTrue(anonUser === linkedUser)
        assertEquals(3, linkedUser.identities.size)
        assertEquals(Credentials.IdentityProvider.EMAIL_PASSWORD, linkedUser.identities[2].provider)
        admin.setAutomaticConfirmation(enabled = true)
    }

    @Ignore("FIXME: Wait for linkUser support in ObjectStore")
    @Test
    fun linkUser_existingCredentialsThrows() {
        val email = TestHelper.getRandomEmail()
        val password = "123456"
        val emailUser: User = app.registerUserAndLogin(email, password)
        val anonymousUser: User = app.login(Credentials.anonymous())
        try {
            anonymousUser.linkCredentials(Credentials.emailPassword(email, password))
            fail()
        } catch (ex: ObjectServerError) {
            assertEquals(ErrorCode.BAD_REQUEST, ex.errorCode)
        }
    }

    @Ignore("FIXME: Wait for linkUser support in ObjectStore")
    @Test
    fun linkUser_invalidArgsThrows() {
        try {
            anonUser.linkCredentials(TestHelper.getNull())
            fail()
        } catch (ignore: IllegalArgumentException) {
        }
    }

    @Ignore("FIXME: Wait for linkUser support in ObjectStore")
    @Test
    fun linkUserAsync() {
        admin.setAutomaticConfirmation(enabled = false)
        val user: User = app.login(Credentials.anonymous())
        assertEquals(1, user.identities.size)
        val email = TestHelper.getRandomEmail()
        val password = "123456"
        app.emailPasswordAuth.registerUser(email, password) // TODO: Test what happens if auto-confirm is enabled
        looperThread.runBlocking {
            anonUser.linkCredentialsAsync(Credentials.emailPassword(email, password)) { result ->
                val linkedUser: User = result.orThrow
                assertTrue(user === linkedUser)
                assertEquals(2, linkedUser.identities.size)
                assertEquals(Credentials.IdentityProvider.EMAIL_PASSWORD, linkedUser.identities[1].provider)
                admin.setAutomaticConfirmation(enabled = true)
            }
        }
    }

    @Ignore("FIXME: Wait for linkUser support in ObjectStore")
    @Test
    fun linkUserAsync_throwsOnNonLooperThread() {
        try {
            anonUser.linkCredentialsAsync(Credentials.emailPassword(TestHelper.getRandomEmail(), "123456")) { fail() }
            fail()
        } catch (ignore: java.lang.IllegalStateException) {
        }
    }

    @Test
    fun removeUser() {
        anonUser.logOut() // Remove user used by other tests

        // Removing logged in user
        val user1 = app.registerUserAndLogin(TestHelper.getRandomEmail(), "123456")
        assertEquals(user1, app.currentUser())
        assertEquals(1, app.allUsers().size)
        user1.remove()
        assertEquals(User.State.REMOVED, user1.state)
        assertNull(app.currentUser())
        assertEquals(0, app.allUsers().size)

        // Remove logged out user
        val user2 = app.registerUserAndLogin(TestHelper.getRandomEmail(), "123456")
        user2.logOut()
        assertNull(app.currentUser())
        assertEquals(1, app.allUsers().size)
        user2.remove()
        assertEquals(User.State.REMOVED, user2.state)
        assertEquals(0, app.allUsers().size)
    }

    @Test
    fun removeUserAsync() {
        anonUser.logOut() // Remove user used by other tests

        // Removing logged in user
        looperThread.runBlocking {
            val user = app.registerUserAndLogin(TestHelper.getRandomEmail(), "123456")
            assertEquals(user, app.currentUser())
            assertEquals(1, app.allUsers().size)
            user.removeAsync { result ->
                assertEquals(User.State.REMOVED, result.orThrow.state)
                assertNull(app.currentUser())
                assertEquals(0, app.allUsers().size)
                looperThread.testComplete()
            }
        }

        // Removing logged out user
        looperThread.runBlocking {
            val user = app.registerUserAndLogin(TestHelper.getRandomEmail(), "123456")
            user.logOut()
            assertNull(app.currentUser())
            assertEquals(1, app.allUsers().size)
            user.removeAsync { result ->
                assertEquals(User.State.REMOVED, result.orThrow.state)
                assertEquals(0, app.allUsers().size)
                looperThread.testComplete()
            }
        }
    }

    @Test
    fun removeUserAsync_nonLooperThreadThrows() {
        val user: User = app.registerUserAndLogin(TestHelper.getRandomEmail(), "1234567")
        try {
            user.removeAsync { fail() }
        } catch (ignore: IllegalStateException) {
        }
    }

    @Test
    fun getApiKeyAuthProvider() {
        val user: User = app.registerUserAndLogin(TestHelper.getRandomEmail(), "123456")
        val provider1: ApiKeyAuth = user.apiKeyAuth
        assertEquals(user, provider1.user)

        user.logOut()

        try {
            user.apiKeyAuth
            fail()
        } catch (ex: IllegalStateException) {
        }
    }

    @Test
    fun getDeviceId() {
        // TODO No reason to integration test this. Use a stubbed response instead.
        val user: User = app.registerUserAndLogin(TestHelper.getRandomEmail(), "123456")
        assertTrue(user.deviceId.isNotEmpty() && user.deviceId.length == 24) // Server returns a UUID
    }
    @Test
    fun equals() {
        // TODO Could be that we could use a fake user
        val user: User = app.registerUserAndLogin("user1@example.com", "123456")
        assertEquals(user, user)
        assertNotEquals(user, app)
        user.logOut()

        val sameUserNewLogin = app.login(Credentials.emailPassword(user.email!!, "123456"))
        // Verify that it is not same object but uses underlying OSSyncUser equality on identity
        assertFalse(user === sameUserNewLogin)
        assertEquals(user, sameUserNewLogin)

        val differentUser: User = app.registerUserAndLogin("user2@example.com", "123456")
        assertNotEquals(user, differentUser)
    }

    @Test
    fun hashCode_user() {
        val user: User = app.registerUserAndLogin("user1@example.com", "123456")
        user.logOut()

        val sameUserNewLogin = app.login(Credentials.emailPassword(user.email!!, "123456"))
        // Verify that two equal users also returns same hashCode
        assertFalse(user === sameUserNewLogin)
        assertEquals(user.hashCode(), sameUserNewLogin.hashCode())
    }

}
