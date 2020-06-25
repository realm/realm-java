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
package io.realm.internal.network

import androidx.test.platform.app.InstrumentationRegistry
import io.realm.Realm
import io.realm.TestApp
import io.realm.TestHelper
import io.realm.admin.ServerAdmin
import io.realm.log.LogLevel
import io.realm.log.RealmLog
import io.realm.mongodb.App
import io.realm.mongodb.AppException
import io.realm.mongodb.Credentials
import io.realm.mongodb.close
import io.realm.mongodb.log.obfuscator.HttpLogObfuscator
import io.realm.mongodb.log.obfuscator.RegexPatternObfuscatorFactory
import io.realm.mongodb.log.obfuscator.RegexPatternObfuscatorFactory.LOGIN_FEATURE
import org.bson.Document
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertTrue

class LoggingInterceptorTest {

    private lateinit var app: App
    private lateinit var testLogger: TestHelper.TestLogger

    @Before
    fun setUp() {
        Realm.init(InstrumentationRegistry.getInstrumentation().targetContext)
    }

    @After
    fun tearDown() {
        if (this::app.isInitialized) {
            app.close()
        }
        if (this::testLogger.isInitialized) {
            RealmLog.setLevel(LogLevel.WARN)
            RealmLog.remove(testLogger)
        }
    }

    @Test
    fun emailPasswordRegistrationAndLogin_noObfuscation() {
        app = TestApp()
        testLogger = getLogger()

        val email = TestHelper.getRandomEmail()
        val password = "123456"
        app.emailPasswordAuth.registerUser(email, password)
        assertMessageExists(""""email":"$email"""", """"password":"$password"""")

        app.login(Credentials.emailPassword(email, password))
        assertMessageExists(""""username":"$email"""", """"password":"$password"""")
    }

    @Test
    fun emailPasswordRegistrationAndLogin_obfuscation() {
        app = TestApp { builder ->
            builder.httpLogObfuscator(HttpLogObfuscator(LOGIN_FEATURE, RegexPatternObfuscatorFactory.getObfuscators(LOGIN_FEATURE)))
        }
        testLogger = getLogger()

        val email = TestHelper.getRandomEmail()
        val password = "123456"
        app.emailPasswordAuth.registerUser(email, password)
        assertMessageExists(""""email":"***"""", """"password":"***"""")

        app.login(Credentials.emailPassword(email, password))
        assertMessageExists(""""username":"***"""", """"password":"***"""")
    }

    @Test
    fun apiKeyLogin_noObfuscation() {
        app = TestApp()
        testLogger = getLogger()
        val admin = ServerAdmin()
        val serverKey = admin.createServerApiKey()

        app.login(Credentials.serverApiKey(serverKey))
        assertMessageExists(""""key":"$serverKey"""")
    }

    @Test
    fun apiKeyLogin_obfuscation() {
        app = TestApp { builder ->
            builder.httpLogObfuscator(HttpLogObfuscator(LOGIN_FEATURE, RegexPatternObfuscatorFactory.getObfuscators(LOGIN_FEATURE)))
        }
        testLogger = getLogger()
        val admin = ServerAdmin()
        val serverKey = admin.createServerApiKey()

        app.login(Credentials.serverApiKey(serverKey))
        assertMessageExists(""""key":"***"""")
    }

    @Test
    fun customFunctionLogin_noObfuscation() {
        app = TestApp()
        testLogger = getLogger()

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
        assertMessageExists(""""$key1":"$value1"""")
    }

    @Test
    fun customFunctionLogin_obfuscation() {
        app = TestApp { builder ->
            builder.httpLogObfuscator(HttpLogObfuscator(LOGIN_FEATURE, RegexPatternObfuscatorFactory.getObfuscators(LOGIN_FEATURE)))
        }
        testLogger = getLogger()

        val key1 = "mail"
        val key2 = "id"
        val value1 = "myfakemail@mongodb.com"
        val value2 = 666
        val customFunction = mapOf(
                key1 to value1,
                key2 to value2
        ).let { credsMap ->
            Credentials.customFunction(Document(credsMap))
        }

        app.login(customFunction)
        assertMessageExists(""""functionArgs":"***"""")
    }

    @Test
    fun facebookTokenLogin_noObfuscation() {
        app = TestApp()
        testLogger = getLogger()
        val token = "facebook-token"

        try {
            app.login(Credentials.facebook(token))
        } catch (error: AppException) {
            // It will fail as long as oauth2 tokens aren't supported
        } finally {
            assertMessageExists(""""access_token":"$token"""")
        }
    }

    @Test
    fun facebookTokenLogin_obfuscation() {
        app = TestApp { builder ->
            builder.httpLogObfuscator(HttpLogObfuscator(LOGIN_FEATURE, RegexPatternObfuscatorFactory.getObfuscators(LOGIN_FEATURE)))
        }
        testLogger = getLogger()
        val token = "facebook-token"

        try {
            app.login(Credentials.facebook(token))
        } catch (error: AppException) {
            // It will fail as long as oauth2 tokens aren't supported
        } finally {
            assertMessageExists(""""access_token":"***"""")
        }
    }

    @Test
    fun appleTokenLogin_noObfuscation() {
        app = TestApp()
        testLogger = getLogger()
        val token = "apple-token"

        try {
            app.login(Credentials.apple(token))
        } catch (error: AppException) {
            // It will fail as long as oauth2 tokens aren't supported
        } finally {
            assertMessageExists(""""id_token":"$token"""")
        }
    }

    @Test
    fun appleTokenLogin_obfuscation() {
        app = TestApp { builder ->
            builder.httpLogObfuscator(HttpLogObfuscator(LOGIN_FEATURE, RegexPatternObfuscatorFactory.getObfuscators(LOGIN_FEATURE)))
        }
        testLogger = getLogger()
        val token = "apple-token"

        try {
            app.login(Credentials.apple(token))
        } catch (error: AppException) {
            // It will fail as long as oauth2 tokens aren't supported
        } finally {
            assertMessageExists(""""id_token":"***"""")
        }
    }

    @Test
    fun googleTokenLogin_noObfuscation() {
        app = TestApp()
        testLogger = getLogger()
        val token = "google-token"

        try {
            app.login(Credentials.google(token))
        } catch (error: AppException) {
            // It will fail as long as oauth2 tokens aren't supported
        } finally {
            assertMessageExists(""""authCode":"$token"""")
        }
    }

    @Test
    fun googleTokenLogin_obfuscation() {
        app = TestApp { builder ->
            builder.httpLogObfuscator(HttpLogObfuscator(LOGIN_FEATURE, RegexPatternObfuscatorFactory.getObfuscators(LOGIN_FEATURE)))
        }
        testLogger = getLogger()
        val token = "google-token"

        try {
            app.login(Credentials.google(token))
        } catch (error: AppException) {
            // It will fail as long as oauth2 tokens aren't supported
        } finally {
            assertMessageExists(""""authCode":"***"""")
        }
    }

    private fun getLogger(): TestHelper.TestLogger =
            TestHelper.TestLogger().also {
                RealmLog.add(it)
                RealmLog.setLevel(LogLevel.ALL)
            }

    // Check whether the expected logcat entries are present in the test logger, either as the
    // latest or previous entry
    private fun assertMessageExists(vararg entries: String) {
        var patternExists = false
        for (entry in entries) {
            patternExists = patternExists
                    || testLogger.message.contains(entry)
                    || testLogger.previousMessage.contains(entry)
        }
        assertTrue(patternExists)
    }
}
