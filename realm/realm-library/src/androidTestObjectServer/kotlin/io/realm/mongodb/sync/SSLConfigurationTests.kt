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

import android.os.SystemClock
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.realm.Realm
import io.realm.TestApp
import io.realm.TestHelper
import io.realm.TestHelper.TestLogger
import io.realm.TestSyncConfigurationFactory
import io.realm.entities.DefaultSyncSchema
import io.realm.entities.SyncDog
import io.realm.exceptions.RealmFileException
import io.realm.kotlin.syncSession
import io.realm.log.LogLevel
import io.realm.log.RealmLog
import io.realm.mongodb.App
import io.realm.mongodb.AppException
import io.realm.mongodb.Credentials
import io.realm.mongodb.ErrorCode
import io.realm.mongodb.User
import io.realm.mongodb.close
import io.realm.mongodb.registerUserAndLogin
import io.realm.rule.BlockingLooperThread
import org.bson.types.ObjectId
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals

/**
 * Remember: Any test against the local BAAS server will not use an SSL connection.
 */
@RunWith(AndroidJUnit4::class)
@Ignore("Should only be run manually")
class SSLConfigurationTests {

    @get:Rule
    val configFactory = TestSyncConfigurationFactory()
    private val looperThread = BlockingLooperThread()
    private lateinit var app: App

    @Before
    fun setUp() {
        app = App("application-0-nfiqi")
    }

    @Test
    fun disableSSLVerification() = looperThread.runBlocking {
        val username = TestHelper.getRandomEmail()
        val password = "password"
        var user: User = app.registerUserAndLogin(username, password)
        var partition = UUID.randomUUID().toString()

        // 1. Copy a valid Realm to the server
        val syncConfig: SyncConfiguration = configFactory.createSyncConfigurationBuilder(user, partition)
                .modules(DefaultSyncSchema())
                .build()
        var realm = Realm.getInstance(syncConfig)
        realm.beginTransaction()
        realm.createObject(SyncDog::class.java, ObjectId()).name = "Foo"
        realm.commitTransaction()

        // make sure the changes gets to the server
        app.sync.getSession(syncConfig).uploadAllLocalChanges()
        realm.close()
        user.logOut()

        // 2. Local state should now be completely reset. Open the Realm again with a new configuration which should
        // download the uploaded changes.
        user = app.registerUserAndLogin(TestHelper.getRandomEmail(), "password")
        val syncConfigSSL: SyncConfiguration = configFactory.createSyncConfigurationBuilder(user, partition)
                .modules(DefaultSyncSchema())
                .waitForInitialRemoteData()
                .trustedRootCA("trusted_ca.pem") // This is expired and should be ignored
                .disableSSLVerification()
                .build()
        realm = Realm.getInstance(syncConfigSSL)
        val all = realm.where(SyncDog::class.java).findAll()
        try {
            Assert.assertEquals(1, all.size.toLong())
            Assert.assertEquals("Foo", all[0]!!.name)
        } finally {
            realm.close()
        }
        looperThread.testComplete()
    }

    @Test
    fun trustedRootCA_syncShouldFailWithoutTrustedCA() = looperThread.runBlocking {
        val username = TestHelper.getRandomEmail()
        val password = "password"
        var user: User = app.registerUserAndLogin(username, password)

        // 1. Copy a valid Realm to the server
        val syncConfig: SyncConfiguration = configFactory.createSyncConfigurationBuilder(user)
            .modules(DefaultSyncSchema())
            .build()
        var realm = Realm.getInstance(syncConfig)
        realm.beginTransaction()
        realm.createObject(SyncDog::class.java, ObjectId()).name = "Foo"
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
                .modules(DefaultSyncSchema())
                .trustedRootCA("untrusted_ca.pem")
                .build()
        // waitForInitialRemoteData will throw an Internal error (125): Operation Canceled
        SystemClock.sleep(TimeUnit.SECONDS.toMillis(2))
        realm = Realm.getInstance(syncConfigSSL)
        try {
            realm.syncSession.downloadAllServerChanges()
        } catch (ex: AppException) {
            assertEquals(ErrorCode.CLIENT_SSL_SERVER_CERT_REJECTED, ex.errorCode)
        } finally {
            realm.close()
        }
        looperThread.testComplete()
    }

    @Test
    fun combining_trustedRootCA_and_withoutSSLVerification_willWarn() = looperThread.runBlocking {
        val username = TestHelper.getRandomEmail()
        val password = "password"
        val user: User = app.registerUserAndLogin(username, password)

        val testLogger = TestLogger()
        val originalLevel = RealmLog.getLevel()
        RealmLog.add(testLogger)
        RealmLog.setLevel(LogLevel.WARN)
        configFactory.createSyncConfigurationBuilder(user)
            .name("useSsl")
            .modules(DefaultSyncSchema())
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
}