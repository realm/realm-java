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
import org.junit.Test
import org.junit.runner.RunWith

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
    fun logOut() {
        val user: RealmUser = app.login(RealmCredentials.anonymous())
        assertEquals(user, app.currentUser())
        app.logOut()
        assertEquals(RealmUser.State.ERROR, user.state) // Should be LOGGED_OUT in a future update of OS
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
                assertEquals(RealmUser.State.ERROR, user.state) // Should be LOGGED_OUT in a future update of OS
                assertEquals(RealmUser.State.ERROR, callbackUser.state) // Should be LOGGED_OUT in a future update of OS
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
