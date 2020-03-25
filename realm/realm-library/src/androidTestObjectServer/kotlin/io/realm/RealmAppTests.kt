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
class RealmAppTests {

    private val looperThread = BlockingLooperThread()
    private lateinit var app: TestRealmApp

    @Before
    fun setUp() {
        app = TestRealmApp()
    }

    @After
    fun tearDown() {
        app.close()
    }

    // FIXME: Smoke test for the network protocol and associated classes.
    @Test
    fun login() {
        val creds = RealmCredentials.anonymous()
        var user = app.login(creds)
        assertNotNull(user)
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

        // Logging out a normal user will keep them here until they properly removed
        // TODO: Add test once registerUser has been added

        // Removing anonymous users should remove them completely
        user1.logOut()
        allUsers = app.allUsers()
        assertEquals(1, allUsers.size)
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
        TODO()
    }

    @Ignore("Waiting for registerUser support")
    @Test
    fun removeUser() {
        TODO()
    }

    @Ignore("Waiting for registerUser support")
    @Test
    fun removeUser_nullThrows() {
        TODO()
    }

    @Ignore("Waiting for registerUser support")
    @Test
    fun removeUserAsync() {
        TODO()
    }

    @Ignore("Waiting for registerUser support")
    @Test
    fun removeUserAsync_nonLooperThreadThrows() {
        TODO()
    }

    @Test
    fun logOut() {
        val user: RealmUser = app.login(RealmCredentials.anonymous())
        assertEquals(user, app.currentUser())
        app.logOut()
        assertEquals(RealmUser.State.REMOVED, user.state) // Should be LOGGED_OUT in a future update of OS
        assertNull(app.currentUser())
    }

    @Test
    fun logOutAsync() = looperThread.runBlocking {
        val user: RealmUser = app.login(RealmCredentials.anonymous())
        assertEquals(user, app.currentUser())
        app.logOutAsync(object: RealmApp.Callback<RealmUser> {
            override fun onSuccess(callbackUser: RealmUser) {
                assertNull(app.currentUser())
                assertEquals(user, callbackUser)
                assertEquals(RealmUser.State.REMOVED, user.state) // Should be LOGGED_OUT in a future update of OS
                assertEquals(RealmUser.State.REMOVED, callbackUser.state) // Should be LOGGED_OUT in a future update of OS
                looperThread.testComplete()
            }

            override fun onError(error: ObjectServerError) {
                fail(error.toString())
            }
        })
    }

    @Test
    fun logOutAsync_throwsOnNonLooperThread() {
        val user: RealmUser = app.login(RealmCredentials.anonymous())
        assertEquals(user, app.currentUser())
        val callback = object: RealmApp.Callback<RealmUser> {
            override fun onSuccess(t: RealmUser) {
                fail("Method should throw")
            }
            override fun onError(error: ObjectServerError) {
                fail("Method should throw")
            }
        }
        try {
            app.logOutAsync(callback)
            fail()
        } catch (ignore: IllegalStateException) {
        }
    }
}
