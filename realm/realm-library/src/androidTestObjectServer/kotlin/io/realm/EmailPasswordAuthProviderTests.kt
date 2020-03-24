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
import io.realm.admin.StitchAdmin
import io.realm.rule.BlockingLooperThread
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import java.lang.IllegalArgumentException

@RunWith(AndroidJUnit4::class)
class EmailPasswordAuthProviderTests {

    private val looperThread = BlockingLooperThread()
    private lateinit var app: TestRealmApp

    // Callback use to verify that an Illegal Argument was thrown from async methods
    private val checkNullArgCallback
            = object : RealmApp.Callback<Void> {
        override fun onSuccess(t: Void) {
            fail()
        }

        override fun onError(error: ObjectServerError) {
            assertEquals(ErrorCode.UNKNOWN, error.errorCode)
            looperThread.testComplete()
        }
    }
    @Before
    fun setUp() {
        app = TestRealmApp()
    }

    @After
    fun tearDown() {
        app.close()
    }

    inline fun testNullArg(method: () -> Unit) {
        try {
            method()
            fail()
        } catch (ignore: IllegalArgumentException) {
        }
    }

    @Test
    fun registerUser() {
        val email = TestHelper.getRandomEmail()
        val password = "password1234"
        app.emailPasswordAuthProvider.registerUser(email, password)
        val user = app.login(RealmCredentials.emailPassword(email, password))
        assertEquals(RealmUser.State.ACTIVE, user.state)
    }

    @Test
    fun registerUserAsync() {
        val email = TestHelper.getRandomEmail()
        val password = "password1234"
        looperThread.runBlocking {
            app.emailPasswordAuthProvider.registerUserAsync(email, password, object: RealmApp.Callback<Void> {
                override fun onSuccess(t: Void) {
                    val user2 = app.login(RealmCredentials.emailPassword(email, password))
                    assertEquals(RealmUser.State.ACTIVE, user2.state)
                    looperThread.testComplete()
                }

                override fun onError(error: ObjectServerError) {
                    fail(error.toString())
                }
            })
        }
    }

    @Test
    fun registerUser_invalidServerArgsThrows() {
        // Synchronous
        try {
            app.emailPasswordAuthProvider.registerUser("invalid-email", "1234")
            fail()
        } catch (ex: ObjectServerError) {
            assertEquals(ErrorCode.BAD_REQUEST, ex.errorCode)
        }

        // ASynchronous
        looperThread.runBlocking {
            app.emailPasswordAuthProvider.registerUserAsync("invalid-email", "1234", object: RealmApp.Callback<Void> {
                override fun onSuccess(t: Void) {
                    fail()
                }

                override fun onError(error: ObjectServerError) {
                    assertEquals(ErrorCode.BAD_REQUEST, error.errorCode)
                    looperThread.testComplete()
                }
            })
        }
    }

    @Test
    fun registerUser_invalidArgumentsThrows() {
        val provider: EmailPasswordAuthProvider = app.emailPasswordAuthProvider
        testNullArg { provider.registerUser(TestHelper.getNullString(), "123456") }
        testNullArg { provider.registerUser("foo@bar.baz", TestHelper.getNullString()) }
        looperThread.runBlocking {
            provider.registerUserAsync(TestHelper.getNullString(), "123456", checkNullArgCallback)
        }
        looperThread.runBlocking {
            provider.registerUserAsync("foo@bar.baz", TestHelper.getNullString(), checkNullArgCallback)
        }
    }

    @Ignore("Find a way to automate this")
    @Test
    fun confirmUser() {
        // Manually test:
        // 1. Start test server: ./tools/sync_test_server/start_server.sh
        // 2. http://127.0.0.1:9090/
        // TODO
        TODO()
    }

    @Ignore("Find a way to automate this")
    @Test
    fun confirmUserAsync() {
        // Manually test:
        //
        TODO()
    }

    @Test
    fun confirmUser_invalidServerArgsThrows() {
        // Synchronous
        try {
            app.emailPasswordAuthProvider.confirmUser("invalid-token", "invalid-token-id")
            fail()
        } catch (ex: ObjectServerError) {
            assertEquals(ErrorCode.BAD_REQUEST, ex.errorCode)
        }

        // ASynchronous
        looperThread.runBlocking {
            app.emailPasswordAuthProvider.confirmUserAsync("invalid-email", "1234", object: RealmApp.Callback<Void> {
                override fun onSuccess(t: Void) {
                    fail()
                }

                override fun onError(error: ObjectServerError) {
                    assertEquals(ErrorCode.BAD_REQUEST, error.errorCode)
                    looperThread.testComplete()
                }
            })
        }
    }

    @Test
    fun confirmUser_invalidArgumentsThrows() {
        val provider: EmailPasswordAuthProvider = app.emailPasswordAuthProvider
        testNullArg { provider.confirmUser(TestHelper.getNullString(), "token-id") }
        testNullArg { provider.confirmUser("token", TestHelper.getNullString()) }
        looperThread.runBlocking {
            provider.confirmUserAsync(TestHelper.getNullString(), "token-id", checkNullArgCallback)
        }
        looperThread.runBlocking {
            provider.confirmUserAsync("token", TestHelper.getNullString(), checkNullArgCallback)
        }
    }

    @Test
    fun resendConfirmationEmail() {
        val admin = StitchAdmin()
        admin.setAutomaticConfirmation(false)
        val email = "christian.melchior@mongodb.com"
        try {
            val provider = app.emailPasswordAuthProvider
            provider.registerUser(email, "123456")
            provider.resendConfirmationEmail(email)
        } finally {
            admin.deletePendingUser(email)
            admin.setAutomaticConfirmation(true)
        }
    }

    @Test
    fun resendConfirmationEmailAsync() {

    }

    @Test
    fun resendConfirmationEmail_invalidServerArgsThrows() {

    }

    @Test
    fun resendConfirmationEmail_invalidArgumentsThrows() {

    }

    @Test
    fun sendResetPasswordEmail() {

    }

    @Test
    fun sendResetPasswordEmailAsync() {

    }

    @Test
    fun sendResetPasswordEmail_invalidServerArgsThrows() {

    }

    @Test
    fun sendResetPasswordEmail_invalidArgumentsThrows() {

    }

    @Test
    fun callResetPasswordFunction() {

    }

    @Test
    fun callResetPasswordFunctionAsync() {

    }

    @Test
    fun callResetPasswordFunction_invalidServerArgsThrows() {

    }

    @Test
    fun callResetPasswordFunction_invalidArgumentsThrows() {

    }

    @Test
    fun resetPassword() {

    }

    @Test
    fun resetPasswordAsync() {

    }

    @Test
    fun resetPassword_invalidServerArgsThrows() {

    }

    @Test
    fun resetPassword_invalidArgumentsThrows() {

    }

    @Test
    fun callMethodsOnMainThreadThrows() {
        TODO()
    }

    @Test
    fun callAsyncMethodsOnNonLooperThreadThrows() {
        TODO()
    }
}

