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
import io.realm.log.LogLevel
import io.realm.log.RealmLog
import io.realm.mongodb.remote.RemoteCountOptions
import io.realm.util.blockingGetResult
import org.bson.Document
import org.bson.types.ObjectId
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@RunWith(AndroidJUnit4::class)
class RemoteMongoCollectionTest {

    private lateinit var app: TestRealmApp
    private lateinit var admin: ServerAdmin
    private lateinit var user: RealmUser
    private lateinit var client: RemoteMongoClient
    private lateinit var database: RemoteMongoDatabase

    @Before
    fun setUp() {
        Realm.init(InstrumentationRegistry.getInstrumentation().targetContext)
        RealmLog.setLevel(LogLevel.DEBUG)
        admin = ServerAdmin()
        app = TestRealmApp()
        user = app.registerUserAndLogin(TestHelper.getRandomEmail(), "123456")
        client = user.getRemoteMongoClient(SERVICE_NAME)
        database = client.getDatabase(DATABASE_NAME)
    }

    @After
    fun tearDown() {
        // FIXME: probably not the best way to "reset" the state
        with(getCollectionInternal(COLLECTION_NAME)) {
            deleteMany(Document()).blockingGetResult()
        }

        if (this::app.isInitialized) {
            app.close()
        }
    }

    @Test
    fun insertMany() {
        with(getCollectionInternal(COLLECTION_NAME)) {
            assertEquals(0, count().blockingGetResult())

            val rawDoc = Document(KEY_1, VALUE_1)
            val doc1 = Document(rawDoc)
            val doc2 = Document(rawDoc)
            val doc3 = Document(rawDoc)
            val doc4 = Document("foo", "bar")
            val manyDocuments = listOf(doc1, doc2, doc3, doc4)

            insertMany(manyDocuments)
                    .blockingGetResult()
                    .let { assertEquals(manyDocuments.size, it!!.insertedIds.size) }

            assertEquals(manyDocuments.size.toLong(), count().blockingGetResult())
            assertEquals(3, count(rawDoc).blockingGetResult())
            assertEquals(2, count(rawDoc, RemoteCountOptions().limit(2)).blockingGetResult())
            assertEquals(1, count(Document("foo", "bar")).blockingGetResult())
            assertEquals(0, count(Document("bar", "foo")).blockingGetResult())
        }
    }

    @Test
    fun count() {
        with(getCollectionInternal(COLLECTION_NAME)) {
            assertEquals(0, count().blockingGetResult())

            val rawDoc = Document(KEY_1, VALUE_1)//.withId()
            val doc1 = Document(rawDoc)
            val doc2 = Document(rawDoc)

            // FIXME: check feasibility of this assertion, otherwise, just make a plain insert
//            assertTrue(ObjectId.isValid(this.insertOne(doc1).blockingGetResult()!!.insertedId.toString()))
            insertOne(doc1).blockingGetResult()
            assertEquals(1, count().blockingGetResult())
            insertOne(doc2).blockingGetResult()
            assertEquals(2, count().blockingGetResult())

            assertEquals(2, count(rawDoc).blockingGetResult())
            assertEquals(0, count(Document("foo", "bar")).blockingGetResult())
            assertEquals(1, count(rawDoc, RemoteCountOptions().limit(1)).blockingGetResult())

            // FIXME: investigate error handling for malformed payloads
//            assertFailsWith(ExecutionException::class) {
//                this.count(Document("\$who", 1)).blockingGetResult()
//                fail("Should not reach this!")
//            }
        }
    }

    @Test
    fun deleteOne() {
        with(getCollectionInternal(COLLECTION_NAME)) {
            assertEquals(0, count().blockingGetResult())

            val rawDoc = Document(KEY_1, VALUE_1)//.withId()
            val doc1 = Document(rawDoc)

            insertOne(doc1).blockingGetResult()
            assertEquals(1, count().blockingGetResult())
            assertEquals(1, deleteOne(doc1).blockingGetResult()!!.deletedCount)
            assertEquals(0, count().blockingGetResult())

            val doc1b = Document(rawDoc)
            val doc2 = Document("foo", "bar")
            val doc3 = Document("42", "666")
            insertMany(listOf(doc1, doc1b, doc2, doc3)).blockingGetResult()
            assertEquals(1, deleteOne(rawDoc).blockingGetResult()!!.deletedCount)
            assertEquals(1, deleteOne(Document()).blockingGetResult()!!.deletedCount)
        }
    }

    @Test
    fun deleteMany() {
        with(getCollectionInternal(COLLECTION_NAME)) {
            assertEquals(0, count().blockingGetResult())

            val rawDoc = Document(KEY_1, VALUE_1)//.withId()
            val doc1 = Document(rawDoc)

            insertOne(doc1).blockingGetResult()
            assertEquals(1, count().blockingGetResult())
            assertEquals(1, deleteMany(doc1).blockingGetResult()!!.deletedCount)
            assertEquals(0, count().blockingGetResult())

            val doc1b = Document(rawDoc)
            val doc2 = Document("foo", "bar")
            val doc3 = Document("42", "666")
            insertMany(listOf(doc1, doc1b, doc2, doc3)).blockingGetResult()
            assertEquals(2, deleteMany(rawDoc).blockingGetResult()!!.deletedCount)                 // two docs will be deleted
            assertEquals(2, count().blockingGetResult())                                           // two docs still present
            assertEquals(2, deleteMany(Document()).blockingGetResult()!!.deletedCount)             // delete all
            assertEquals(0, count().blockingGetResult())

            insertMany(listOf(doc1, doc1b, doc2, doc3)).blockingGetResult()
            assertEquals(4, deleteMany(Document()).blockingGetResult()!!.deletedCount)             // delete all
            assertEquals(0, count().blockingGetResult())
        }
    }

    @Test
    fun findOne() {
        with(getCollectionInternal(COLLECTION_NAME)) {
            assertEquals(0, count().blockingGetResult())

            val rawDoc = Document(KEY_1, VALUE_1)//.withId()
            insertOne(Document("foo", "bar")).blockingGetResult()
            insertOne(rawDoc).blockingGetResult()
            assertEquals(2, count().blockingGetResult())

            findOne().blockingGetResult().let { findOneResult ->
                assertNotNull(findOneResult)

                // FIXME: add more assertions
            }
        }
    }

    // FIXME: more to come

    private fun getCollectionInternal(collectionName: String, javaClass: Class<Document>? = null): RemoteMongoCollection<Document> {
        return when (javaClass) {
            null -> database.getCollection(collectionName)
            else -> database.getCollection(collectionName, javaClass)
        }
    }

    // FIXME: investigate crash when parsing BSONs that have this property
    private fun Document.withId(objectId: ObjectId? = null): Document {
        return apply { this["_id"] = objectId ?: ObjectId() }
    }

    private companion object {
        const val SERVICE_NAME = "BackingDB"    // it comes from the test server's BackingDB/config.json
        const val DATABASE_NAME = "test_data"   // same as above

        const val COLLECTION_NAME = "COLLECTION_NAME"
        const val KEY_1 = "KEY"
        const val VALUE_1 = "666"
    }
}
