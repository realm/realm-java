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

import androidx.test.annotation.UiThreadTest
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.realm.admin.ServerAdmin
import io.realm.log.LogLevel
import io.realm.log.RealmLog
import io.realm.rule.BlockingLooperThread
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import java.lang.IllegalStateException

@RunWith(AndroidJUnit4::class)
class EmailPasswordAuthProviderTests {

    private val looperThread = BlockingLooperThread()
    private lateinit var app: TestRealmApp
    private lateinit var admin: ServerAdmin

    // Callback use to verify that an Illegal Argument was thrown from async methods
    private val checkNullArgCallback = RealmApp.Callback<Void> { result ->
        if (result.isSuccess) {
            fail()
        } else {
            assertEquals(ErrorCode.UNKNOWN, result.error.errorCode)
            looperThread.testComplete()
        }
    }

    // Methods exposed by the EmailPasswordAuthProvider
    enum class Method {
        REGISTER_USER,
        CONFIRM_USER,
        RESEND_CONFIRMATION_EMAIL,
        SEND_RESET_PASSWORD_EMAIL,
        CALL_RESET_PASSWORD_FUNCTION,
        RESET_PASSWORD
    }

    @Before
    fun setUp() {
        app = TestRealmApp()
        RealmLog.setLevel(LogLevel.DEBUG)
        admin = ServerAdmin()
    }

    @After
    fun tearDown() {
        app.close()
        admin.deleteAllUsers()
        RealmLog.setLevel(LogLevel.WARN)
    }

    inline fun <reified T : Exception> expectException(method: () -> Unit) {
        try {
            method()
            fail()
        } catch (e: Throwable) {
            if (e !is T) {
                fail("Unexpected exception: $e")
            }
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
            app.emailPasswordAuthProvider.registerUserAsync(email, password) { result ->
                if (result.isSuccess) {
                    val user2 = app.login(RealmCredentials.emailPassword(email, password))
                    assertEquals(RealmUser.State.ACTIVE, user2.state)
                    looperThread.testComplete()
                } else {
                    fail(result.error.toString())
                }
            }
        }
    }

    @Test
    fun registerUser_invalidServerArgsThrows() {
        val provider = app.emailPasswordAuthProvider
        try {
            provider.registerUser("invalid-email", "1234")
            fail()
        } catch (ex: ObjectServerError) {
            assertEquals(ErrorCode.BAD_REQUEST, ex.errorCode)
        }
    }

    @Test
    fun registerUserAsync_invalidServerArgsThrows() {
        val provider = app.emailPasswordAuthProvider
        looperThread.runBlocking {
            provider.registerUserAsync("invalid-email", "1234") { result ->
                if (result.isSuccess) {
                    fail()
                } else {
                    assertEquals(ErrorCode.BAD_REQUEST, result.error.errorCode)
                    looperThread.testComplete()
                }
            }
        }
    }

    @Test
    fun registerUser_invalidArgumentsThrows() {
        val provider: EmailPasswordAuthProvider = app.emailPasswordAuthProvider
        expectException<IllegalArgumentException> { provider.registerUser(TestHelper.getNullString(), "123456") }
        expectException<IllegalArgumentException> { provider.registerUser("foo@bar.baz", TestHelper.getNullString()) }
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
        TODO("Figure out how to manually test this")
    }

    @Ignore("Find a way to automate this")
    @Test
    fun confirmUserAsync() {
        TODO("Figure out how to manually test this")
    }

    @Test
    fun confirmUser_invalidServerArgsThrows() {
        val provider = app.emailPasswordAuthProvider
        try {
            provider.confirmUser("invalid-token", "invalid-token-id")
            fail()
        } catch (ex: ObjectServerError) {
            assertEquals(ErrorCode.BAD_REQUEST, ex.errorCode)
        }
    }

    @Test
    fun confirmUserAsync_invalidServerArgsThrows() {
        val provider = app.emailPasswordAuthProvider
        looperThread.runBlocking {
            provider.confirmUserAsync("invalid-email", "1234") { result ->
                if (result.isSuccess) {
                    fail()
                } else {
                    assertEquals(ErrorCode.BAD_REQUEST, result.error.errorCode)
                    looperThread.testComplete()
                }
            }
        }
    }

    @Test
    fun confirmUser_invalidArgumentsThrows() {
        val provider: EmailPasswordAuthProvider = app.emailPasswordAuthProvider
        expectException<IllegalArgumentException> { provider.confirmUser(TestHelper.getNullString(), "token-id") }
        expectException<IllegalArgumentException> { provider.confirmUser("token", TestHelper.getNullString()) }
        looperThread.runBlocking {
            provider.confirmUserAsync(TestHelper.getNullString(), "token-id", checkNullArgCallback)
        }
        looperThread.runBlocking {
            provider.confirmUserAsync("token", TestHelper.getNullString(), checkNullArgCallback)
        }
    }

    @Test
    fun resendConfirmationEmail() {
        // We only test that the server successfully accepts the request. We have no way of knowing
        // if the Email was actually sent.
        // FIXME: Figure out a way to check if this actually happened. Perhaps a custom SMTP server?
        val email = "test@10gen.com"
        admin.setAutomaticConfirmation(false)
        try {
            val provider = app.emailPasswordAuthProvider
            provider.registerUser(email, "123456")
            provider.resendConfirmationEmail(email)
        } finally {
            admin.setAutomaticConfirmation(true)
        }
    }

    @Test
    fun resendConfirmationEmailAsync() {
        // We only test that the server successfully accepts the request. We have no way of knowing
        // if the Email was actually sent.
        val email = "test@10gen.com"
        admin.setAutomaticConfirmation(false)
        try {
            looperThread.runBlocking {
                val provider = app.emailPasswordAuthProvider
                provider.registerUser(email, "123456")
                provider.resendConfirmationEmailAsync(email) { result ->
                    when(result.isSuccess) {
                        true -> looperThread.testComplete()
                        false -> fail(result.error.toString())
                    }
                }
            }
        } finally {
            admin.setAutomaticConfirmation(true)
        }
    }

    @Test
    fun resendConfirmationEmail_invalidServerArgsThrows() {
        val email = "test@10gen.com"
        admin.setAutomaticConfirmation(false)
        val provider = app.emailPasswordAuthProvider
        provider.registerUser(email, "123456")
        try {
            provider.resendConfirmationEmail("foo")
            fail()
        } catch (error: ObjectServerError) {
            assertEquals(ErrorCode.USER_NOT_FOUND, error.errorCode)
        } finally {
            admin.setAutomaticConfirmation(true)
        }
    }

    @Test
    fun resendConfirmationEmailAsync_invalidServerArgsThrows() {
        val email = "test@10gen.com"
        admin.setAutomaticConfirmation(false)
        val provider = app.emailPasswordAuthProvider
        provider.registerUser(email, "123456")
        try {
            looperThread.runBlocking {
                provider.resendConfirmationEmailAsync("foo") { result ->
                    if (result.isSuccess) {
                        fail()
                    } else {
                        assertEquals(ErrorCode.USER_NOT_FOUND, result.error.errorCode)
                        looperThread.testComplete()
                    }
                }
            }
        } finally {
            admin.setAutomaticConfirmation(true)
        }
    }

    @Test
    fun resendConfirmationEmail_invalidArgumentsThrows() {
        val provider: EmailPasswordAuthProvider = app.emailPasswordAuthProvider
        expectException<IllegalArgumentException> { provider.resendConfirmationEmail(TestHelper.getNullString()) }
        looperThread.runBlocking {
            provider.resendConfirmationEmailAsync(TestHelper.getNullString(), checkNullArgCallback)
        }
    }

    @Test
    fun sendResetPasswordEmail() {
        val provider = app.emailPasswordAuthProvider
        val email: String = "test@10gen.com" // Must be a valid email, otherwise the server will fail
        provider.registerUser(email, "123456")
        provider.sendResetPasswordEmail(email)
    }

    @Test
    fun sendResetPasswordEmailAsync() {
        val provider = app.emailPasswordAuthProvider
        val email: String = "test@10gen.com" // Must be a valid email, otherwise the server will fail
        provider.registerUser(email, "123456")
        looperThread.runBlocking {
            provider.sendResetPasswordEmailAsync(email) { result ->
                when(result.isSuccess) {
                    true -> looperThread.testComplete()
                    false -> fail(result.error.toString())
                }

            }
        }
    }

    @Test
    fun sendResetPasswordEmail_invalidServerArgsThrows() {
        val provider = app.emailPasswordAuthProvider
        try {
            provider.sendResetPasswordEmail("unknown@10gen.com")
            fail()
        } catch (error: ObjectServerError) {
            assertEquals(ErrorCode.USER_NOT_FOUND, error.errorCode)
        }
    }

    @Test
    fun sendResetPasswordEmailAsync_invalidServerArgsThrows() {
        val provider = app.emailPasswordAuthProvider
        looperThread.runBlocking {
            provider.sendResetPasswordEmailAsync("unknown@10gen.com") { result ->
                if (result.isSuccess) {
                    fail()
                } else {
                    assertEquals(ErrorCode.USER_NOT_FOUND, result.error.errorCode)
                    looperThread.testComplete()
                }
            }
        }
    }

    @Test
    fun sendResetPasswordEmail_invalidArgumentsThrows() {
        val provider = app.emailPasswordAuthProvider
        expectException<IllegalArgumentException> { provider.sendResetPasswordEmail(TestHelper.getNullString()) }
        looperThread.runBlocking {
            provider.sendResetPasswordEmailAsync(TestHelper.getNullString(), checkNullArgCallback)
        }
    }

    @Test
    fun callResetPasswordFunction() {
        val provider = app.emailPasswordAuthProvider
        admin.setResetFunction(enabled = true)
        val email = TestHelper.getRandomEmail()
        provider.registerUser(email, "123456")
        try {
            provider.callResetPasswordFunction(email, "new-password", "say-the-magic-word", 42)
            app.login(RealmCredentials.emailPassword(email, "new-password"))
            app.logOut()
        } finally {
            admin.setResetFunction(enabled = false)
        }
    }

    @Test
    fun callResetPasswordFunctionAsync() {
        val provider = app.emailPasswordAuthProvider
        admin.setResetFunction(enabled = true)
        val email = TestHelper.getRandomEmail()
        provider.registerUser(email, "123456")
        try {
            looperThread.runBlocking {
                provider.callResetPasswordFunctionAsync(email,
                        "new-password",
                        arrayOf("say-the-magic-word", 42)) { result ->
                    if (result.isSuccess) {
                        app.login(RealmCredentials.emailPassword(email, "new-password"))
                        app.logOut()
                        looperThread.testComplete()
                    } else {
                        fail(result.error.toString())
                    }
                }
            }
        } finally {
            admin.setResetFunction(enabled = false)
        }
    }

    @Test
    fun callResetPasswordFunction_invalidServerArgsThrows() {
        val provider = app.emailPasswordAuthProvider
        admin.setResetFunction(enabled = true)
        val email = TestHelper.getRandomEmail()
        provider.registerUser(email, "123456")
        try {
            provider.callResetPasswordFunction(email, "new-password", "wrong-magic-word")
        } catch (error: ObjectServerError) {
            assertEquals(ErrorCode.SERVICE_UNKNOWN, error.errorCode)
        } finally {
            admin.setResetFunction(enabled = false)
        }
    }

    @Test
    fun callResetPasswordFunctionAsync_invalidServerArgsThrows() {
        val provider = app.emailPasswordAuthProvider
        admin.setResetFunction(enabled = true)
        val email = TestHelper.getRandomEmail()
        provider.registerUser(email, "123456")
        try {
            looperThread.runBlocking {
                provider.callResetPasswordFunctionAsync(
                        email,
                        "new-password",
                        arrayOf("wrong-magic-word")) { result ->
                    if (result.isSuccess) {
                        fail()
                    } else {
                        assertEquals(ErrorCode.SERVICE_UNKNOWN, result.error.errorCode)
                        looperThread.testComplete()
                    }
                }
            }
        } finally {
            admin.setResetFunction(enabled = false)
        }
    }

    @Test
    fun callResetPasswordFunction_invalidArgumentsThrows() {
        val provider = app.emailPasswordAuthProvider
        expectException<IllegalArgumentException> { provider.callResetPasswordFunction(TestHelper.getNullString(), "password") }
        expectException<IllegalArgumentException> { provider.callResetPasswordFunction("foo@bar.baz", TestHelper.getNullString()) }
        looperThread.runBlocking {
            provider.callResetPasswordFunctionAsync(TestHelper.getNullString(), "new-password", arrayOf(), checkNullArgCallback)
        }
        looperThread.runBlocking {
            provider.callResetPasswordFunctionAsync("foo@bar.baz", io.realm.TestHelper.getNullString(), arrayOf(), checkNullArgCallback)
        }
    }

    @Ignore("Find a way to automate this")
    @Test
    fun resetPassword() {
        TODO("How to test this manually?")
    }

    @Ignore("Find a way to automate this")
    @Test
    fun resetPasswordAsync() {
        TODO("How to test this manually?")
    }

    @Test
    fun resetPassword_invalidServerArgsThrows() {
        val provider = app.emailPasswordAuthProvider
        try {
            provider.resetPassword("invalid-token", "invalid-token-id", "new-password")
        } catch (error: ObjectServerError) {
            assertEquals(ErrorCode.BAD_REQUEST, error.errorCode)
        }
    }

    @Test
    fun resetPasswordASync_invalidServerArgsThrows() {
        val provider = app.emailPasswordAuthProvider
        looperThread.runBlocking {
            provider.resetPasswordAsync("invalid-token", "invalid-token-id", "new-password") { result ->
                if (result.isSuccess) {
                    fail()
                } else {
                    assertEquals(ErrorCode.BAD_REQUEST, result.error.errorCode)
                    looperThread.testComplete()
                }
            }
        }
    }

    @Test
    fun resetPassword_invalidArgumentsThrows() {
        val provider = app.emailPasswordAuthProvider
        expectException<IllegalArgumentException> { provider.resetPassword(TestHelper.getNullString(), "token-id", "password") }
        expectException<IllegalArgumentException> { provider.resetPassword("token", TestHelper.getNullString(), "password") }
        expectException<IllegalArgumentException> { provider.resetPassword("token", "token-id", TestHelper.getNullString()) }
        looperThread.runBlocking {
            provider.resetPasswordAsync(TestHelper.getNullString(), "token-id", "password", checkNullArgCallback)
        }
        looperThread.runBlocking {
            provider.resetPasswordAsync("token", TestHelper.getNullString(), "password", checkNullArgCallback)
        }
        looperThread.runBlocking {
            provider.resetPasswordAsync("token","token-id", TestHelper.getNullString(), checkNullArgCallback)
        }
    }

    @Test
    @UiThreadTest
    fun callMethodsOnMainThreadThrows() {
        val provider: EmailPasswordAuthProvider = app.emailPasswordAuthProvider
        val email: String = TestHelper.getRandomEmail()
        for (method in Method.values()) {
            try {
                when(method) {
                    Method.REGISTER_USER -> provider.registerUser(email, "123456")
                    Method.CONFIRM_USER -> provider.confirmUser("token", "tokenId")
                    Method.RESEND_CONFIRMATION_EMAIL -> provider.resendConfirmationEmail(email)
                    Method.SEND_RESET_PASSWORD_EMAIL -> provider.sendResetPasswordEmail(email)
                    Method.CALL_RESET_PASSWORD_FUNCTION -> provider.callResetPasswordFunction(email, "123456")
                    Method.RESET_PASSWORD -> provider.resetPassword("token", "token-id", "password")
                }
                fail("$method should have thrown an exception")
            } catch (error: ObjectServerError) {
                assertEquals(ErrorCode.NETWORK_UNKNOWN, error.errorCode)
            }
        }
    }

    @Test
    fun callAsyncMethodsOnNonLooperThreadThrows() {
        val provider: EmailPasswordAuthProvider = app.emailPasswordAuthProvider
        val email: String = TestHelper.getRandomEmail()
        val callback = RealmApp.Callback<Void> { fail() }
        for (method in Method.values()) {
            try {
                when(method) {
                    Method.REGISTER_USER -> provider.registerUserAsync(email, "123456", callback)
                    Method.CONFIRM_USER -> provider.confirmUserAsync("token", "tokenId", callback)
                    Method.RESEND_CONFIRMATION_EMAIL -> provider.resendConfirmationEmailAsync(email, callback)
                    Method.SEND_RESET_PASSWORD_EMAIL -> provider.sendResetPasswordEmailAsync(email, callback)
                    Method.CALL_RESET_PASSWORD_FUNCTION -> provider.callResetPasswordFunctionAsync(email, "123456", arrayOf(), callback)
                    Method.RESET_PASSWORD -> provider.resetPasswordAsync("token", "token-id", "password", callback)
                }
                fail("$method should have thrown an exception")
            } catch (ignore: IllegalStateException) {
            }
        }
    }
}

