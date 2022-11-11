/*
 * Copyright 2017 Realm Inc.
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
package io.realm.mongodb.sync

import androidx.test.ext.junit.runners.AndroidJUnit4
import io.realm.TestApp
import io.realm.TestSyncConfigurationFactory
import io.realm.rule.BlockingLooperThread
import io.realm.TestHelper.TestLogger
import android.os.SystemClock
import io.realm.Realm
import io.realm.entities.StringOnly
import io.realm.exceptions.RealmFileException
import io.realm.log.LogLevel
import io.realm.log.RealmLog
import io.realm.mongodb.App
import io.realm.mongodb.Credentials
import io.realm.mongodb.User
import io.realm.mongodb.close
import io.realm.mongodb.registerUserAndLogin
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.rules.Timeout
import org.junit.runner.RunWith
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
class SSLConfigurationTests {

    @get:Rule
    val configFactory = TestSyncConfigurationFactory()
    private val looperThread = BlockingLooperThread()
    private lateinit var app: App

    @Before
    fun setUp() {
        app = TestApp()
    }

    @After
    fun tearDown() {
        app.close()
    }

    @Test
    @Ignore("FIXME: https://github.com/realm/realm-java/issues/6472")
    fun trustedRootCA() = looperThread.runBlocking {
        val username = UUID.randomUUID().toString()
        val password = "password"
        var user = app.registerUserAndLogin(username, password)

        // 1. Copy a valid Realm to the server
        val syncConfig: SyncConfiguration = configFactory.createSyncConfigurationBuilder(user)
                .schema(StringOnly::class.java)
                .build()
        var realm = Realm.getInstance(syncConfig)
        realm.beginTransaction()
        realm.createObject(StringOnly::class.java).chars = "Foo"
        realm.commitTransaction()

        // make sure the changes gets to the server
        app.sync.getSession(syncConfig).uploadAllLocalChanges()
        realm.close()
        user.logOut()

        // 2. Local state should now be completely reset. Open the Realm again with a new configuration which should
        // download the uploaded changes.
        user = app.login(Credentials.emailPassword(username, password))
        val syncConfigSSL: SyncConfiguration = configFactory.createSyncConfigurationBuilder(user)
                .name("useSsl")
                .schema(StringOnly::class.java)
                .waitForInitialRemoteData()
                .trustedRootCA("trusted_ca.pem")
                .build()
        realm = Realm.getInstance(syncConfigSSL)
        val all = realm.where(StringOnly::class.java).findAll()
        try {
            Assert.assertEquals(1, all.size.toLong())
            Assert.assertEquals("Foo", all[0]!!.chars)
        } finally {
            realm.close()
        }
        looperThread.testComplete()
    }

    @Test
    fun withoutSSLVerification() = looperThread.runBlocking {
        val username = UUID.randomUUID().toString()
        val password = "password"
        var user: User = app.registerUserAndLogin(username, password)

        // 1. Copy a valid Realm to the server
        val syncConfig: SyncConfiguration = configFactory.createSyncConfigurationBuilder(user)
                .schema(StringOnly::class.java)
                .build()
        var realm = Realm.getInstance(syncConfig)
        realm.beginTransaction()
        realm.createObject(StringOnly::class.java).chars = "Foo"
        realm.commitTransaction()

        // make sure the changes gets to the server
        app.sync.getSession(syncConfig).uploadAllLocalChanges()
        realm.close()
        user.logOut()

        // 2. Local state should now be completely reset. Open the Realm again with a new configuration which should
        // download the uploaded changes.
        user = app.login(Credentials.emailPassword(username, password))
        val syncConfigSSL: SyncConfiguration = configFactory.createSyncConfigurationBuilder(user)
                .name("useSsl")
                .schema(StringOnly::class.java)
                .waitForInitialRemoteData()
                .disableSSLVerification()
                .build()
        realm = Realm.getInstance(syncConfigSSL)
        val all = realm.where(StringOnly::class.java).findAll()
        try {
            Assert.assertEquals(1, all.size.toLong())
            Assert.assertEquals("Foo", all[0]!!.chars)
        } finally {
            realm.close()
        }
        looperThread.testComplete()
    }

    @Test
    fun trustedRootCA_syncShouldFailWithoutTrustedCA() = looperThread.runBlocking {
        val username = UUID.randomUUID().toString()
        val password = "password"
        var user: User = app.registerUserAndLogin(username, password)

        // 1. Copy a valid Realm to the server
        val syncConfig: SyncConfiguration = configFactory.createSyncConfigurationBuilder(user)
            .schema(StringOnly::class.java)
            .build()
        var realm = Realm.getInstance(syncConfig)
        realm.beginTransaction()
        realm.createObject(StringOnly::class.java).chars = "Foo"
        realm.commitTransaction()

        // make sure the changes gets to the server
        app.sync.getSession(syncConfig).uploadAllLocalChanges()
        realm.close()
        user.logOut()

        // 2. Local state should now be completely reset. Open the Realm again with a new configuration which should
        // download the uploaded changes.
        user = app.login(Credentials.emailPassword(username, password))
        val syncConfigSSL: SyncConfiguration = configFactory.createSyncConfigurationBuilder(user)
                .name("useSsl")
                .schema(StringOnly::class.java)
                .trustedRootCA("untrusted_ca.pem")
                .build()
        // waitForInitialRemoteData will throw an Internal error (125): Operation Canceled
        SystemClock.sleep(TimeUnit.SECONDS.toMillis(2))
        realm = Realm.getInstance(syncConfigSSL)
        try {
            Assert.assertTrue(realm.isEmpty)
        } finally {
            realm.close()
        }
        looperThread.testComplete()
    }

    @Test
    fun combining_trustedRootCA_and_withoutSSLVerification_willThrow() = looperThread.runBlocking {
        val username = UUID.randomUUID().toString()
        val password = "password"
        val user: User = app.registerUserAndLogin(username, password)

        val testLogger = TestLogger()
        val originalLevel = RealmLog.getLevel()
        RealmLog.add(testLogger)
        RealmLog.setLevel(LogLevel.WARN)
        configFactory.createSyncConfigurationBuilder(user)
            .name("useSsl")
            .schema(StringOnly::class.java)
            .trustedRootCA("trusted_ca.pem")
            .disableSSLVerification()
            .build()
        assertEquals(
            "SSL Verification is disabled, the provided server certificate will not be used.",
            testLogger.message
        )
        RealmLog.remove(testLogger)
        RealmLog.setLevel(originalLevel)
        looperThread.testComplete()
    }

    @Test
    @Ignore("FIXME: https://github.com/realm/realm-java/issues/6472")
    fun trustedRootCA_notExisting_certificate_willThrow() = looperThread.runBlocking {
        val username = UUID.randomUUID().toString()
        val password = "password"
        val user: User = app.registerUserAndLogin(username, password)
        val syncConfig: SyncConfiguration = configFactory.createSyncConfigurationBuilder(user)
                .schema(StringOnly::class.java)
                .trustedRootCA("none_existing_file.pem")
                .build()
        try {
            Realm.getInstance(syncConfig)
            Assert.fail()
        } catch (ignored: RealmFileException) {
        }
        looperThread.testComplete()
    }

    @Test
    @Ignore("FIXME: https://github.com/realm/realm-java/issues/6472")
    fun combiningTrustedRootCA_and_disableSSLVerification() = looperThread.runBlocking {
        val username = UUID.randomUUID().toString()
        val password = "password"
        var user: User = app.registerUserAndLogin(username, password)

        // 1. Copy a valid Realm to the server using ssl_verify_path option
        val syncConfigWithCertificate: SyncConfiguration = configFactory.createSyncConfigurationBuilder(user)
                .schema(StringOnly::class.java)
                .trustedRootCA("trusted_ca.pem")
                .build()
        var realm = Realm.getInstance(syncConfigWithCertificate)
        realm.beginTransaction()
        realm.createObject(StringOnly::class.java).chars = "Foo"
        realm.commitTransaction()

        // make sure the changes gets to the server
        app.sync.getSession(syncConfigWithCertificate).uploadAllLocalChanges()
        realm.close()
        user.logOut()

        // 2. Local state should now be completely reset. Open the Realm again with a new configuration which should
        // download the uploaded changes.
        user = app.login(Credentials.emailPassword(username, password))
        val syncConfigDisableSSL = configFactory.createSyncConfigurationBuilder(user)
                .name("useSsl")
                .schema(StringOnly::class.java)
                .waitForInitialRemoteData()
                .disableSSLVerification()
                .build()
        realm = Realm.getInstance(syncConfigDisableSSL)
        val all = realm.where(StringOnly::class.java).findAll()
        try {
            Assert.assertEquals(1, all.size.toLong())
            Assert.assertEquals("Foo", all[0]!!.chars)
        } finally {
            realm.close()
        }
        looperThread.testComplete()
    }

    // IMPORTANT: Following test assume the root certificate is installed on the test device
    //            certificate is located in <realm-java>/tools/sync_test_server/keys/android_test_certificate.crt
    //            adb push <realm-java>/tools/sync_test_server/keys/android_test_certificate.crt /sdcard/
    //            then import the certificate from the device (Settings/Security/Install from storage)
    @Test
    @Ignore("FIXME: https://github.com/realm/realm-java/issues/6472")
    fun sslVerifyCallback_isUsed() = looperThread.runBlocking {
        val username = UUID.randomUUID().toString()
        val password = "password"
        var user: User = app.registerUserAndLogin(username, password)

        // 1. Copy a valid Realm to the server using ssl_verify_path option
        val syncConfig: SyncConfiguration = configFactory.createSyncConfigurationBuilder(user)
                .schema(StringOnly::class.java)
                .build()
        var realm = Realm.getInstance(syncConfig)
        realm.beginTransaction()
        realm.createObject(StringOnly::class.java).chars = "Foo"
        realm.commitTransaction()

        // make sure the changes gets to the server
        app.sync.getSession(syncConfig).uploadAllLocalChanges()
        realm.close()
        user.logOut()

        // 2. Local state should now be completely reset. Open the Realm again with a new configuration which should
        // download the uploaded changes.
        user = app.login(Credentials.emailPassword(username, password))
        val syncConfigSecure: SyncConfiguration = configFactory.createSyncConfigurationBuilder(user)
                .name("useSsl")
                .schema(StringOnly::class.java)
                .waitForInitialRemoteData()
                .build()
        realm = Realm.getInstance(syncConfigSecure)
        val all = realm.where(StringOnly::class.java).findAll()
        try {
            Assert.assertEquals(1, all.size.toLong())
            Assert.assertEquals("Foo", all[0]!!.chars)
        } finally {
            realm.close()
        }
        looperThread.testComplete()
    }
}