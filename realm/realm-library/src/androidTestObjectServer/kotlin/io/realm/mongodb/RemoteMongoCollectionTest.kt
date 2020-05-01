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
package io.realm.mongodb

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.realm.*
import io.realm.admin.ServerAdmin
import io.realm.util.blockingGetResult
import org.bson.Document
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
class RemoteMongoCollectionTest {

    private lateinit var app: TestRealmApp
    private lateinit var admin: ServerAdmin
    private lateinit var user: RealmUser
    private lateinit var client: RemoteMongoClient
    private lateinit var database: RemoteMongoDatabase
    private lateinit var collection: RemoteMongoCollection<Document>

    @Before
    fun setUp() {
        Realm.init(InstrumentationRegistry.getInstrumentation().targetContext)
        admin = ServerAdmin()
        app = TestRealmApp()
        user = app.registerUserAndLogin(TestHelper.getRandomEmail(), "123456")
        client = user.remoteMongoClient
        database = client.getDatabase(DATABASE_NAME)
        collection = database.getCollection(COLLECTION_NAME)
    }

    @After
    fun tearDown() {
        if (this::app.isInitialized) {
            app.close()
        }
    }

    @Test
    fun countAllDocumentsWithoutInsert() {
        val result = collection.count().blockingGetResult()
        assertEquals(0, result)
    }

    @Test
    fun countAllDocumentsWithInsert() {
        // TODO:
    }

    @Test
    fun countWithFilter() {
        // TODO:
    }

    @Test
    fun countWithFilterAndLimit() {
        // TODO:
    }

    // TODO: more to come

    private companion object {
        const val DATABASE_NAME = "DATABASE_NAME"
        const val COLLECTION_NAME = "COLLECTION_NAME"
    }
}
