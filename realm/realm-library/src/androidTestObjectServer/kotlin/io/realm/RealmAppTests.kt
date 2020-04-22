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
import io.realm.log.LogLevel
import io.realm.log.RealmLog
import io.realm.rule.BlockingLooperThread
import io.realm.util.expectException
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.atomic.AtomicReference

@RunWith(AndroidJUnit4::class)
class RealmAppTests {

    private val looperThread = BlockingLooperThread()
    private lateinit var app: TestRealmApp
    private lateinit var admin: ServerAdmin

    @Before
    fun setUp() {
        app = TestRealmApp()
        admin = ServerAdmin()
    }

    @After
    fun tearDown() {
        app.close()
    }

    @Test
    fun login() {
        val creds = RealmCredentials.anonymous()
        var user = app.login(creds)
        assertNotNull(user)
    }

    @Test
    fun login_invalidUserThrows() {
        val credentials = RealmCredentials.emailPassword("foo", "bar")
        try {
            app.login(credentials)
            fail()
        } catch(ex: ObjectServerError) {
            assertEquals(ErrorCode.SERVICE_UNKNOWN, ex.errorCode)
        }
    }

    @Test
    fun login_invalidArgsThrows() {
        try {
            app.login(TestHelper.getNull())
            fail()
        } catch(ignore: IllegalArgumentException) {
        }
    }

    @Test
    fun loginAsync() = looperThread.runBlocking {
        app.loginAsync(RealmCredentials.anonymous()) { result ->
            assertNotNull(result.orThrow)
            looperThread.testComplete()
        }
    }

    @Test
    fun loginAsync_invalidUserThrows() = looperThread.runBlocking {
        app.loginAsync(RealmCredentials.emailPassword("foo", "bar")) { result ->
            assertFalse(result.isSuccess)
            assertEquals(ErrorCode.SERVICE_UNKNOWN, result.error.errorCode)
            looperThread.testComplete()
        }
    }

    @Test
    fun loginAsync_throwsOnNonLooperThread() {
        try {
            app.loginAsync(RealmCredentials.anonymous()) { fail() }
            fail()
        } catch (ignore: IllegalStateException) {
        }
    }

    @Test
    fun currentUser() {
        assertNull(app.currentUser())
        val user: RealmUser = app.login(RealmCredentials.anonymous())
        assertEquals(user, app.currentUser())
        user.logOut()
        assertNull(app.currentUser())
    }

    @Test
    fun allUsers() {
        assertEquals(0, app.allUsers().size)
        val user1 = app.login(RealmCredentials.anonymous())
        var allUsers = app.allUsers()
        assertEquals(1, allUsers.size)
        assertTrue(allUsers.containsKey(user1.id))
        assertEquals(user1, allUsers[user1.id])

        val user2 = app.login(RealmCredentials.anonymous())
        allUsers = app.allUsers()
        assertEquals(2, allUsers.size)
        assertTrue(allUsers.containsKey(user2.id))

        val user3: RealmUser = app.registerUserAndLogin(TestHelper.getRandomEmail(), "123456")
        allUsers = app.allUsers()
        assertEquals(3, allUsers.size)
        assertTrue(allUsers.containsKey(user3.id))

        // Logging out users that registered with email/password will just put them in LOGGED_OUT state
        user3.logOut();
        allUsers = app.allUsers()
        assertEquals(3, allUsers.size)
        assertTrue(allUsers.containsKey(user3.id))
        assertEquals(RealmUser.State.LOGGED_OUT, allUsers[user3.id]!!.state)

        // Logging out anonymous users will remove them completely
        user1.logOut()
        allUsers = app.allUsers()
        assertEquals(2, allUsers.size)
        assertFalse(allUsers.containsKey(user1.id))
    }

    @Test
    fun allUsers_retrieveRemovedUser() {
        val user1: RealmUser = app.login(RealmCredentials.anonymous())
        val allUsers: Map<String, RealmUser> = app.allUsers()
        assertEquals(1, allUsers.size)
        user1.logOut()
        assertEquals(1, allUsers.size)
        val userCopy: RealmUser = allUsers[user1.id] ?: error("Could not find user")
        assertEquals(user1, userCopy)
        assertEquals(RealmUser.State.REMOVED, userCopy.state)
        assertTrue(app.allUsers().isEmpty())
    }

    @Test
    fun switchUser() {
        val user1: RealmUser = app.login(RealmCredentials.anonymous())
        assertEquals(user1, app.currentUser())
        val user2: RealmUser = app.login(RealmCredentials.anonymous())
        assertEquals(user2, app.currentUser())

        assertEquals(user1, app.switchUser(user1))
        assertEquals(user1, app.currentUser())
    }

    @Test
    fun switchUser_throwIfUserNotLoggedIn() {
        val user1: RealmUser = app.login(RealmCredentials.anonymous())
        val user2: RealmUser = app.login(RealmCredentials.anonymous())
        assertEquals(user2, app.currentUser())

        user1.logOut()
        try {
            app.switchUser(user1)
            fail()
        } catch (ignore: IllegalArgumentException) {
        }
    }

    @Test
    fun currentUser_FallbackToNextValidUser() {
        val user1: RealmUser = app.login(RealmCredentials.anonymous())
        val user2: RealmUser = app.login(RealmCredentials.anonymous())
        assertEquals(user2, app.currentUser())
        user2.logOut()
        assertEquals(user1, app.currentUser())
        user1.logOut()
        assertNull(app.currentUser())
    }

    @Test
    fun switchUser_nullThrows() {
        try {
            app.switchUser(TestHelper.getNull())
            fail()
        } catch (ignore: IllegalArgumentException) {
        }
    }

    @Ignore("Add this test once we have support for both EmailPassword and ApiKey Auth Providers")
    @Test
    fun switchUser_authProvidersLockUsers() {
        TODO("FIXME")
    }

    @Test
    fun authListener() {
        val userRef = AtomicReference<RealmUser>(null)
        looperThread.runBlocking {
            val authenticationListener = object : AuthenticationListener {
                override fun loggedIn(user: RealmUser) {
                    userRef.set(user)
                    user.logOutAsync { /* Ignore */ }
                }

                override fun loggedOut(user: RealmUser) {
                    assertEquals(userRef.get(), user)
                    looperThread.testComplete()
                }
            }
            app.addAuthenticationListener(authenticationListener)
            app.login(RealmCredentials.anonymous())
        }
    }

    @Test
    fun authListener_nullThrows() {
        expectException<java.lang.IllegalArgumentException> { app.addAuthenticationListener(TestHelper.getNull()) }
    }

    @Test
    fun authListener_remove() = looperThread.runBlocking {
        val failListener = object : AuthenticationListener {
            override fun loggedIn(user: RealmUser) { fail() }
            override fun loggedOut(user: RealmUser) { fail() }
        }
        val successListener = object : AuthenticationListener {
            override fun loggedOut(user: RealmUser) { fail() }
            override fun loggedIn(user: RealmUser) { looperThread.testComplete() }
        }
        // This test depends on listeners being executed in order which is an
        // implementation detail, but there isn't a sure fire way to do this
        // without depending on implementation details or assume a specific timing.
        app.addAuthenticationListener(failListener)
        app.addAuthenticationListener(successListener)
        app.removeAuthenticationListener(failListener)
        app.login(RealmCredentials.anonymous())
    }

}
