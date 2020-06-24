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
import io.realm.mongodb.ErrorCode
import io.realm.mongodb.User
import io.realm.mongodb.close
import io.realm.mongodb.registerUserAndLogin
import io.realm.rule.BlockingLooperThread
import io.realm.util.assertFailsWithErrorCode
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

private const val SERVICE_NAME = "gcm"      // it comes from the test server's gcm/config.json
private const val SAMPLE_TOKEN = "fXXW6Qv0Tb2fgNf3pFOtqt:APA91bGs4YUXswCC2w8-X9tSdwo9-r6KwAeicP0FDJtBubyuFgorbAICNTftI4SbSSynvN0s-KVWXaGUo1eWuumkGJzFWngwuxQWWv5uolsfjidYz3kLEdiwWW0D_igtD5nRtYZu6gMW"

@RunWith(AndroidJUnit4::class)
class PushTest {

    private lateinit var app: TestApp
    private lateinit var user: User

    private val looperThread = BlockingLooperThread()

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
        user.getPush(SERVICE_NAME).registerDevice(SAMPLE_TOKEN)
    }

    @Test
    fun registerDevice_twice() {
        // the API allows registering/deregistering twice, just checking we don't get errors
        user.getPush(SERVICE_NAME).registerDevice(SAMPLE_TOKEN)
        user.getPush(SERVICE_NAME).registerDevice(SAMPLE_TOKEN)
    }

    @Test
    fun registerDevice_throwsBecauseOfUnknownService() {
        assertFailsWithErrorCode(ErrorCode.SERVICE_NOT_FOUND) {
            user.getPush("asdf").registerDevice(SAMPLE_TOKEN)
        }
    }

    @Test
    fun registerDevice_throwsBecauseOfLoggedOutUser() {
        user.logOut()
        assertFailsWithErrorCode(ErrorCode.SERVICE_UNKNOWN) {
            user.getPush(SERVICE_NAME).registerDevice(SAMPLE_TOKEN)
        }
    }

    @Test
    fun registerDeviceAsync() {
        looperThread.runBlocking {
            user.getPush(SERVICE_NAME).registerDeviceAsync(SAMPLE_TOKEN) {
                looperThread.testComplete()
            }
        }
    }

    @Test
    fun registerDeviceAsync_throwsBecauseOfWrongThread() {
        assertFailsWith(IllegalStateException::class) {
            user.getPush(SERVICE_NAME).registerDeviceAsync(SAMPLE_TOKEN) { /* do nothing */ }
        }
    }

    @Test
    fun registerDeviceAsync_throwsBecauseOfUnknownService() {
        looperThread.runBlocking {
            user.getPush("asdf").registerDeviceAsync(SAMPLE_TOKEN) {
                assertEquals(ErrorCode.SERVICE_NOT_FOUND, it.error.errorCode)
                looperThread.testComplete()
            }
        }
    }

    @Test
    fun deregisterDevice() {
        user.getPush(SERVICE_NAME).deregisterDevice()
    }

    @Test
    fun deregisterDevice_twice() {
        // the API allows registering/deregistering twice, just checking we don't get errors
        user.getPush(SERVICE_NAME).deregisterDevice()
        user.getPush(SERVICE_NAME).deregisterDevice()
    }

    @Test
    fun deregisterDevice_throwsBecauseOfUnknownService() {
        assertFailsWithErrorCode(ErrorCode.SERVICE_NOT_FOUND) {
            user.getPush("asdf").deregisterDevice()
        }
    }

    @Test
    fun deregisterDevice_throwsBecauseOfLoggedOutUser() {
        user.logOut()
        assertFailsWithErrorCode(ErrorCode.SERVICE_UNKNOWN) {
            user.getPush(SERVICE_NAME).deregisterDevice()
        }
    }

    @Test
    fun deregisterDeviceAsync() {
        looperThread.runBlocking {
            user.getPush(SERVICE_NAME).deregisterDeviceAsync() {
                looperThread.testComplete()
            }
        }
    }

    @Test
    fun deregisterDeviceAsync_throwsBecauseOfWrongThread() {
        assertFailsWith(IllegalStateException::class) {
            user.getPush(SERVICE_NAME).deregisterDeviceAsync() { /* do nothing */ }
        }
    }

    @Test
    fun deregisterDeviceAsync_throwsBecauseOfUnknownService() {
        looperThread.runBlocking {
            user.getPush("asdf").deregisterDeviceAsync() {
                assertEquals(ErrorCode.SERVICE_NOT_FOUND, it.error.errorCode)
                looperThread.testComplete()
            }
        }
    }
}
