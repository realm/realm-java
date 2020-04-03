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
import io.realm.admin.ServerAdmin
import io.realm.rule.BlockingLooperThread
import io.realm.rule.RunInLooperThread
import io.realm.rule.RunTestInLooperThread
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import java.lang.IllegalArgumentException

@RunWith(AndroidJUnit4::class)
class RealmUserTests {

    val looperThread = BlockingLooperThread()

    private lateinit var app: RealmApp
    private lateinit var anonUser: RealmUser
    private lateinit var admin: ServerAdmin

    @Before
    fun setUp() {
        app = TestRealmApp()
        admin = ServerAdmin()
        anonUser = app.login(RealmCredentials.anonymous())
    }

    @After
    fun tearDown() {
        app.close()
    }

    @Test
    fun getApp() {
        assertEquals(app, anonUser.app)
    }

    @Test
    fun getState_anonymousUser() {
        assertEquals(RealmUser.State.LOGGED_IN, anonUser.state)
        anonUser.logOut()
        assertEquals(RealmUser.State.REMOVED, anonUser.state)
    }

    @Ignore("Add test when registerUser works")
    @Test
    fun getState_emailUser() {
        TODO("Implement when we implement registerUser")
    }

    @Test
    fun logOut() {
        anonUser.logOut(); // Remove user created for other tests

        // Anonymous users are removed upon log out
        val user1: RealmUser = app.login(RealmCredentials.anonymous())
        assertEquals(user1, app.currentUser())
        user1.logOut()
        assertEquals(RealmUser.State.REMOVED, user1.state)
        assertNull(app.currentUser())

        // Users registered with Email/Password will register as Logged Out
        val user2: RealmUser = app.registerUserAndLogin(TestHelper.getRandomEmail(), "123456")
        assertEquals(user2, app.currentUser())
        user2.logOut()
        assertEquals(RealmUser.State.LOGGED_OUT, user2.state)
        assertNull(app.currentUser())
    }

    @Test
    fun logOutAsync() = looperThread.runBlocking {
        assertEquals(anonUser, app.currentUser())
        anonUser.logOutAsync() { result ->
            val callbackUser: RealmUser = result.orThrow
            assertNull(app.currentUser())
            assertEquals(anonUser, callbackUser)
            assertEquals(RealmUser.State.REMOVED, anonUser.state)
            assertEquals(RealmUser.State.REMOVED, callbackUser.state)
            looperThread.testComplete()
        }
    }

    @Test
    fun logOutAsync_throwsOnNonLooperThread() {
        val user: RealmUser = app.login(RealmCredentials.anonymous())
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
        val user: RealmUser = app.login(RealmCredentials.anonymous())
        assertEquals(1, user.identities.size)
        val email = TestHelper.getRandomEmail()
        val password = "123456"
        app.emailPasswordAuthProvider.registerUser(email, password) // TODO: Test what happens if auto-confirm is enabled
        val linkedUser: RealmUser = user.linkUser(RealmCredentials.emailPassword(email, password))
        assertTrue(user === linkedUser)
        assertEquals(2, linkedUser.identities.size)
        assertEquals(RealmCredentials.IdentityProvider.EMAIL_PASSWORD, linkedUser.identities[1].provider)
        admin.setAutomaticConfirmation(enabled = true)
    }

    @Ignore("FIXME: Wait for linkUser support in ObjectStore")
    @Test
    fun linkUser_existingCredentialsThrows() {
        val email = TestHelper.getRandomEmail()
        val password = "123456"
        val emailUser: RealmUser = app.registerUserAndLogin(email, password)
        val anonymousUser: RealmUser = app.login(RealmCredentials.anonymous())
        try {
            anonymousUser.linkUser(RealmCredentials.emailPassword(email, password))
            fail()
        } catch (ex: ObjectServerError) {
            assertEquals(ErrorCode.BAD_REQUEST, ex.errorCode)
        }
    }

    @Ignore("FIXME: Wait for linkUser support in ObjectStore")
    @Test
    fun linkUser_invalidArgsThrows() {
        try {
            anonUser.linkUser(TestHelper.getNull())
            fail()
        } catch (ignore: IllegalArgumentException) {
        }
    }

    @Ignore("FIXME: Wait for linkUser support in ObjectStore")
    @Test
    fun linkUserAsync() {
        admin.setAutomaticConfirmation(enabled = false)
        val user: RealmUser = app.login(RealmCredentials.anonymous())
        assertEquals(1, user.identities.size)
        val email = TestHelper.getRandomEmail()
        val password = "123456"
        app.emailPasswordAuthProvider.registerUser(email, password) // TODO: Test what happens if auto-confirm is enabled
        looperThread.runBlocking {
            anonUser.linkUserAsync(RealmCredentials.emailPassword(email, password)) { result ->
                val linkedUser: RealmUser = result.orThrow
                assertTrue(user === linkedUser)
                assertEquals(2, linkedUser.identities.size)
                assertEquals(RealmCredentials.IdentityProvider.EMAIL_PASSWORD, linkedUser.identities[1].provider)
                admin.setAutomaticConfirmation(enabled = true)
            }
        }
    }

    @Ignore("FIXME: Wait for linkUser support in ObjectStore")
    @Test
    fun linkUserAsync_throwsOnNonLooperThread() {
        try {
            anonUser.linkUserAsync(RealmCredentials.emailPassword(TestHelper.getRandomEmail(), "123456")) { fail() }
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
        user1.removeUser()
        assertEquals(RealmUser.State.REMOVED, user1.state)
        assertNull(app.currentUser())
        assertEquals(0, app.allUsers().size)

        // Remove logged out user
        val user2 = app.registerUserAndLogin(TestHelper.getRandomEmail(), "123456")
        user2.logOut()
        assertNull(app.currentUser())
        assertEquals(1, app.allUsers().size)
        user2.removeUser()
        assertEquals(RealmUser.State.REMOVED, user2.state)
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
            user.removeUserAsync { result ->
                assertEquals(RealmUser.State.REMOVED, result.orThrow.state)
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
            user.removeUserAsync { result ->
                assertEquals(RealmUser.State.REMOVED, result.orThrow.state)
                assertEquals(0, app.allUsers().size)
                looperThread.testComplete()
            }
        }
    }

    @Test
    fun removeUserAsync_nonLooperThreadThrows() {
        val user: RealmUser = app.registerUserAndLogin(TestHelper.getRandomEmail(), "1234567")
        try {
            user.removeUserAsync { fail() }
        } catch (ignore: IllegalStateException) {
        }
    }

    @Test
    fun getApiKeyAuthProvider() {
        val user: RealmUser = app.registerUserAndLogin(TestHelper.getRandomEmail(), "123456")
        val provider1: ApiKeyAuthProvider = user.apiKeyAuthProvider
        assertEquals(user, provider1.user)

        user.logOut()

        try {
            user.apiKeyAuthProvider
            fail()
        } catch (ex: IllegalStateException) {
        }
    }

}
