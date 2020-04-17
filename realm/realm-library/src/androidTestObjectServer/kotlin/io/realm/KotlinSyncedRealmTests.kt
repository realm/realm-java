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
import androidx.test.platform.app.InstrumentationRegistry
import io.realm.entities.SyncTest
import io.realm.log.LogLevel
import io.realm.log.RealmLog
import io.realm.rule.BlockingLooperThread
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class KotlinSyncedRealmTests { // FIXME: Rename to SyncedRealmTests once remaining Java tests have been moved

    private val looperThread = BlockingLooperThread()
    private lateinit var app: TestRealmApp
    private var realm: Realm? = null

    @Before
    fun setUp() {
        Realm.init(InstrumentationRegistry.getInstrumentation().targetContext)
        RealmLog.setLevel(LogLevel.TRACE)
        app = TestRealmApp()
    }

    @After
    fun tearDown() {
        realm?.close()
        app.close()
        RealmLog.setLevel(LogLevel.WARN)
    }

    // Smoke test for sync. Waiting for working Sync support.
    @Test
    fun syncRoundTrip() {
        val email = TestHelper.getRandomEmail()
        val password = "123456"
        app.emailPasswordAuthProvider.registerUser(email, password)
        val user: RealmUser = app.login(RealmCredentials.emailPassword(email, password))
        val config = SyncConfiguration.Builder(user, "foobar")
                .schema(SyncTest::class.java)
                .build()
        realm = Realm.getInstance(config)

        app.syncManager.getAllSyncSessions(user)[0].uploadAllLocalChanges()
        app.syncManager.getAllSyncSessions(user)[0].downloadAllServerChanges()
        assertTrue(realm!!.isEmpty)
    }

//    @Test
//    fun session() {
//        val user: RealmUser = app.login(RealmCredentials.anonymous())
//        val realm = Realm.getInstance(SyncConfiguration.defaultConfig(user))
//        assertNotNull(realm.syncSession)
//        assertEquals(SyncSession.State.ACTIVE, realm.syncSession.state)
//        assertEquals(user, realm.syncSession.user)
//        realm.close()
//    }
}
