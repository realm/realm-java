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

@RunWith(AndroidJUnit4::class)
class RealmUserTests {

    val looperThread = BlockingLooperThread()

    private lateinit var app: RealmApp
    private lateinit var anonUser: RealmUser

    @Before
    fun setUp() {
        app = TestRealmApp()
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

}
