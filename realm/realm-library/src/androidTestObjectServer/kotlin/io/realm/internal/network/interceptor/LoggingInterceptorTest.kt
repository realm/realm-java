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
package io.realm.internal.network.interceptor

import androidx.test.platform.app.InstrumentationRegistry
import io.realm.Realm
import io.realm.TestApp
import io.realm.TestHelper
import io.realm.admin.ServerAdmin
import io.realm.log.LogLevel
import io.realm.log.RealmLog
import io.realm.mongodb.App
import io.realm.mongodb.Credentials
import io.realm.mongodb.RegexObfuscatorPatternFactory
import io.realm.mongodb.close
import org.bson.Document
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertTrue

class LoggingInterceptorTest {

    private lateinit var app: App

    @Before
    fun setUp() {
        Realm.init(InstrumentationRegistry.getInstrumentation().targetContext)
    }

    @After
    fun tearDown() {
        if (this::app.isInitialized) {
            app.close()
        }
    }

    @Test
    fun emailPasswordRegistrationAndLogin_noObfuscation() {
        app = TestApp()
        val testLogger = getLogger()

        val email = TestHelper.getRandomEmail()
        val password = "123456"
        app.emailPasswordAuth.registerUser(email, password)
        assertTrue(testLogger.message.contains("""
            "email":"$email"
        """.trimIndent()))
        assertTrue(testLogger.message.contains("""
            "password":"$password"
        """.trimIndent()))

        app.login(Credentials.emailPassword(email, password))
        val usernameLatestMessage = testLogger.message.contains("""
            "email":"$email"
        """.trimIndent())
        val usernamePreviousMessage = testLogger.previousMessage.contains("""
            "username":"$email"
        """.trimIndent())
        assertTrue(usernameLatestMessage || usernamePreviousMessage)
        val passwordLatestMessage = testLogger.message.contains("""
            "password":"$password"
        """.trimIndent())
        val passwordPreviousMessage = testLogger.previousMessage.contains("""
            "password":"$password"
        """.trimIndent())
        assertTrue(passwordLatestMessage || passwordPreviousMessage)

        RealmLog.remove(testLogger)
    }

    @Test
    fun emailPasswordRegistrationAndLogin_obfuscation() {
        app = TestApp(loggingInterceptor = LoggingInterceptor.interceptor(RegexObfuscatorPatternFactory.LOGIN_FEATURE))
        val testLogger = getLogger()

        val email = TestHelper.getRandomEmail()
        val password = "123456"
        app.emailPasswordAuth.registerUser(email, password)
        assertTrue(testLogger.message.contains("""
            "email":"***"
        """.trimIndent()))
        assertTrue(testLogger.message.contains("""
            "password":"***"
        """.trimIndent()))

        app.login(Credentials.emailPassword(email, password))
        val usernameLatestMessage = testLogger.message.contains("""
            "username":"***"
        """.trimIndent())
        val usernamePreviousMessage = testLogger.previousMessage.contains("""
            "username":"***"
        """.trimIndent())
        assertTrue(usernameLatestMessage || usernamePreviousMessage)
        val passwordLatestMessage = testLogger.message.contains("""
            "password":"***"
        """.trimIndent())
        val passwordPreviousMessage = testLogger.previousMessage.contains("""
            "password":"***"
        """.trimIndent())
        assertTrue(passwordLatestMessage || passwordPreviousMessage)

        RealmLog.remove(testLogger)
    }

    @Test
    fun apiKeyLogin_noObfuscation() {
        app = TestApp()
        val testLogger = getLogger()
        val admin = ServerAdmin()
        val serverKey = admin.createServerApiKey()

        app.login(Credentials.serverApiKey(serverKey))
        val apiKeyLatestMessage = testLogger.message.contains("""
            "key":"$serverKey"
        """.trimIndent())
        val apiKeyPreviousMessage = testLogger.previousMessage.contains("""
            "key":"$serverKey"
        """.trimIndent())
        assertTrue(apiKeyLatestMessage || apiKeyPreviousMessage)
    }

    @Test
    fun apiKeyLogin_obfuscation() {
        app = TestApp(loggingInterceptor = LoggingInterceptor.interceptor(RegexObfuscatorPatternFactory.LOGIN_FEATURE))
        val testLogger = getLogger()
        val admin = ServerAdmin()
        val serverKey = admin.createServerApiKey()

        app.login(Credentials.serverApiKey(serverKey))
        val apiKeyLatestMessage = testLogger.message.contains("""
            "key":"***"
        """.trimIndent())
        val apiKeyPreviousMessage = testLogger.previousMessage.contains("""
            "key":"***"
        """.trimIndent())
        assertTrue(apiKeyLatestMessage || apiKeyPreviousMessage)
    }

    @Test
    fun customFunctionLogin_noObfuscation() {
        app = TestApp()
        val testLogger = getLogger()

        val key1 = "mail"
        val key2 = "id"
        val value1 = "myfakemail@mongodb.com"
        val value2 = 666
        val customFunction = mapOf(
                key1 to value1,
                key2 to value2
        ).let {
            Credentials.customFunction(Document(it))
        }

        app.login(customFunction)
        val customFunctionLatestMessage = testLogger.message.contains("""
            "$key1":"$value1"
        """.trimIndent())
        val customFunctionPreviousMessage = testLogger.previousMessage.contains("""
            "$key1":"$value1"
        """.trimIndent())
        assertTrue(customFunctionLatestMessage || customFunctionPreviousMessage)
    }

    @Test
    fun customFunctionLogin_obfuscation() {
        app = TestApp(loggingInterceptor = LoggingInterceptor.interceptor(RegexObfuscatorPatternFactory.LOGIN_FEATURE))
        val testLogger = getLogger()

        val key1 = "mail"
        val key2 = "id"
        val value1 = "myfakemail@mongodb.com"
        val value2 = 666
        val customFunction = mapOf(
                key1 to value1,
                key2 to value2
        ).let {
            Credentials.customFunction(Document(it))
        }

        app.login(customFunction)
        val customFunctionLatestMessage = testLogger.message.contains("""
            "functionArgs":"***"
        """.trimIndent())
        val customFunctionPreviousMessage = testLogger.previousMessage.contains("""
            "functionArgs":"***"
        """.trimIndent())
        assertTrue(customFunctionLatestMessage || customFunctionPreviousMessage)
    }

    // FIXME: add tests for tokens

    private fun getLogger(): TestHelper.TestLogger =
            TestHelper.TestLogger().also {
                RealmLog.add(it)
                RealmLog.setLevel(LogLevel.ALL)
            }
}
