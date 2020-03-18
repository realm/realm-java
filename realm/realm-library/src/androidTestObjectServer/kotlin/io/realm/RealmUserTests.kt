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
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
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
        assertEquals(RealmUser.State.ACTIVE, anonUser.state)
        anonUser.logOut()
        assertEquals(RealmUser.State.ERROR, anonUser.state)
    }

    @Ignore("Add test when registerUser works")
    @Test
    fun getState_emailUser() {
        TODO("Implement when we implement registerUser")
    }

    @Test
    fun logOut() {
        anonUser.logOut()
        assertEquals(RealmUser.State.ERROR, anonUser.state)
    }

    @Test
    fun logOutAsync() = looperThread.runBlocking {
        anonUser.logOutAsync(object: RealmApp.Callback<RealmUser> {
            override fun onSuccess(t: RealmUser) {
                looperThread.testComplete()
            }

            override fun onError(error: ObjectServerError) {
                fail(error.toString())
            }
        })
    }
}
