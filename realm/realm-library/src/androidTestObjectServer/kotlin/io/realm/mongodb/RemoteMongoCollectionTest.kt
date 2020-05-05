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
import io.realm.mongodb.remote.RemoteInsertOneResult
import io.realm.util.blockingGetResult
import org.bson.Document
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
@Ignore("Collections not ready to test yet")
class RemoteMongoCollectionTest {

    private lateinit var app: TestRealmApp
    private lateinit var admin: ServerAdmin
    private lateinit var user: RealmUser
    private lateinit var client: RemoteMongoClient
    private lateinit var database: RemoteMongoDatabase

    @Before
    fun setUp() {
        Realm.init(InstrumentationRegistry.getInstrumentation().targetContext)
        admin = ServerAdmin()
        app = TestRealmApp()
        user = app.registerUserAndLogin(TestHelper.getRandomEmail(), "123456")
        client = user.remoteMongoClient
        database = client.getDatabase(DATABASE_NAME)
    }

    @After
    fun tearDown() {
        if (this::app.isInitialized) {
            app.close()
        }
    }

    @Test
    fun insertOne() {
        with(getCollection(COLLECTION_NAME)) {
            assertEquals(0, this.count().blockingGetResult())
            insertOne(Document())
            assertEquals(1, this.count().blockingGetResult())
            insertOne(Document())
            assertEquals(2, this.count().blockingGetResult())
        }
    }

    @Test
    fun countAllDocumentsBeforeInsert() {
        with(getCollection(COLLECTION_NAME)) {
            assertEquals(0, this.count().blockingGetResult())
        }

        // FIXME: check correct class parameter
        with(getCollection(COLLECTION_NAME, Document::class.java)) {
            assertEquals(0, this.count().blockingGetResult())
        }
    }

    @Test
    fun countWithFilter() {
        // FIXME:
    }

    @Test
    fun countWithFilterAndLimit() {
        // FIXME:
    }

    // FIXME: more to come

    private fun insertOne(collectionName: String): RemoteInsertOneResult? {
        return getCollection(collectionName)
                .insertOne(Document(KEY, VALUE))
                .blockingGetResult()
    }

    private fun getCollection(collectionName: String, javaClass: Class<Document>? = null): RemoteMongoCollection<Document> {
        return when (javaClass) {
            null -> database.getCollection(collectionName)
            else -> database.getCollection(collectionName, javaClass)
        }
    }

    private companion object {
        const val DATABASE_NAME = "DATABASE_NAME"
        const val COLLECTION_NAME = "COLLECTION_NAME"
        const val KEY = "KEY"
        const val VALUE = "VALUE"
    }
}
