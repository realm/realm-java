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

package io.realm.mongodb.push

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.realm.Realm
import io.realm.TestApp
import io.realm.TestHelper
import io.realm.mongodb.*
import io.realm.util.assertFailsWithErrorCode
import io.realm.util.blockingGetResult
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlin.test.fail

private const val SERVICE_NAME = "gcm"      // it comes from the test server's gcm/config.json
private const val SAMPLE_TOKEN = "fXXW6Qv0Tb2fgNf3pFOtqt:APA91bGs4YUXswCC2w8-X9tSdwo9-r6KwAeicP0FDJtBubyuFgorbAICNTftI4SbSSynvN0s-KVWXaGUo1eWuumkGJzFWngwuxQWWv5uolsfjidYz3kLEdiwWW0D_igtD5nRtYZu6gMW"

@RunWith(AndroidJUnit4::class)
class PushClientTest {

    private lateinit var app: TestApp
    private lateinit var user: User

    @Before
    fun setUp() {
        Realm.init(InstrumentationRegistry.getInstrumentation().targetContext)

        app = TestApp()
        user = app.registerUserAndLogin(TestHelper.getRandomEmail(), "123456")
    }

    @After
    fun tearDown() {
        if (this::app.isInitialized) {
            app.close()
        }
    }

    @Test
    fun registerDevice() {
        var allGood = true
        try {
            user.pushNotifications.registerDevice(SAMPLE_TOKEN, SERVICE_NAME).blockingGetResult()
        } catch (e: Exception) {
            allGood = false
        }
        assertTrue(allGood)
    }

    @Test
    fun registerDevice_twice() {
        var allGood = true
        try {
            user.pushNotifications.registerDevice(SAMPLE_TOKEN, SERVICE_NAME).blockingGetResult()
            user.pushNotifications.registerDevice(SAMPLE_TOKEN, SERVICE_NAME).blockingGetResult()
        } catch (e: Exception) {
            allGood = false
        }
        assertTrue(allGood)
    }

    @Test
    fun registerDevice_throwsBecauseOfUnknownService() {
        assertFailsWithErrorCode(ErrorCode.SERVICE_NOT_FOUND) {
            user.pushNotifications.registerDevice(SAMPLE_TOKEN, "asdf").blockingGetResult()
            fail("should never reach this")
        }
    }

    @Test
    fun registerDevice_throwsBecauseOfLoggedOutUser() {
        user.logOut()
        assertFailsWithErrorCode(ErrorCode.SERVICE_UNKNOWN) {
            user.pushNotifications.registerDevice(SAMPLE_TOKEN, SERVICE_NAME).blockingGetResult()
            fail("should never reach this")
        }
    }

    @Test
    fun deregisterDevice() {
        var allGood = true
        try {
            user.pushNotifications.registerDevice(SAMPLE_TOKEN, SERVICE_NAME).blockingGetResult()
        } catch (e: Exception) {
            allGood = false
        }
        assertTrue(allGood)
    }

    @Test
    fun deregisterDevice_twice() {
        var allGood = true
        try {
            user.pushNotifications.registerDevice(SAMPLE_TOKEN, SERVICE_NAME).blockingGetResult()
            user.pushNotifications.deregisterDevice(SAMPLE_TOKEN, SERVICE_NAME).blockingGetResult()
        } catch (e: Exception) {
            allGood = false
        }
        assertTrue(allGood)
    }

    @Test
    fun deregisterDevice_throwsBecauseOfUnknownService() {
        assertFailsWithErrorCode(ErrorCode.SERVICE_NOT_FOUND) {
            user.pushNotifications.deregisterDevice(SAMPLE_TOKEN, "asdf").blockingGetResult()
            fail("should never reach this")
        }
    }

    @Test
    fun deregisterDevice_throwsBecauseOfLoggedOutUser() {
        user.logOut()
        assertFailsWithErrorCode(ErrorCode.SERVICE_UNKNOWN) {
            user.pushNotifications.deregisterDevice(SAMPLE_TOKEN, SERVICE_NAME).blockingGetResult()
            fail("should never reach this")
        }
    }
}
