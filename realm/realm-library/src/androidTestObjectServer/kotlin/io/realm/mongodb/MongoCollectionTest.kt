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
import io.realm.mongodb.mongo.MongoClient
import io.realm.mongodb.mongo.MongoCollection
import io.realm.mongodb.mongo.MongoDatabase
import io.realm.mongodb.mongo.options.RemoteCountOptions
import io.realm.mongodb.mongo.options.RemoteUpdateOptions
import io.realm.util.blockingGetResult
import org.bson.Document
import org.bson.types.ObjectId
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

private const val SERVICE_NAME = "BackingDB"    // it comes from the test server's BackingDB/config.json
private const val DATABASE_NAME = "test_data"   // same as above
private const val COLLECTION_NAME = "COLLECTION_NAME"
private const val KEY_1 = "KEY"
private const val VALUE_1 = "666"

@RunWith(AndroidJUnit4::class)
class MongoCollectionTest {

    private lateinit var app: TestRealmApp
    private lateinit var user: RealmUser
    private lateinit var client: MongoClient
    private lateinit var database: MongoDatabase

    @Before
    fun setUp() {
        Realm.init(InstrumentationRegistry.getInstrumentation().targetContext)
        app = TestRealmApp()
        user = app.registerUserAndLogin(TestHelper.getRandomEmail(), "123456")
        client = user.getMongoClient(SERVICE_NAME)
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
    fun insertOne() {
        with(getCollectionInternal(COLLECTION_NAME)) {
            assertEquals(0, count().blockingGetResult())
            val doc = Document(mapOf("KEY_1" to "WORLD_1", "KEY_2" to "WORLD_2"))
            insertOne(doc).blockingGetResult()
            assertEquals(1, count().blockingGetResult())

            // FIXME: revisit this later
//            val doc = Document("hello", "world")
//            doc["_id"] = ObjectId()
//
//            assertEquals(doc.getObjectId("_id"), insertOne(doc).blockingGetResult()!!.insertedId.asObjectId().value)
//            assertFailsWith(ObjectServerError::class) { insertOne(doc).blockingGetResult() }
//
//            val doc2 = Document("hello", "world")
//            assertNotEquals(doc.getObjectId("_id"), insertOne(doc2).blockingGetResult()!!.insertedId.asObjectId().value)
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
            assertEquals(1, count(Document("foo", "bar")).blockingGetResult())
            assertEquals(0, count(Document("bar", "foo")).blockingGetResult())
        }
    }

    @Test
    fun count() {
        with(getCollectionInternal(COLLECTION_NAME)) {
            assertEquals(0, count().blockingGetResult())

            val rawDoc = Document("hello", "world")
            val doc1 = Document(rawDoc)
            val doc2 = Document(rawDoc)
            insertOne(doc1).blockingGetResult()
            assertEquals(1, count().blockingGetResult())
            insertOne(doc2).blockingGetResult()
            assertEquals(2, count().blockingGetResult())

            assertEquals(2, count(rawDoc).blockingGetResult())
            assertEquals(0, count(Document("hello", "Friend")).blockingGetResult())
            assertEquals(1,count(rawDoc, RemoteCountOptions().limit(1)).blockingGetResult())

            // FIXME: investigate error handling for malformed payloads
//            try {
//                count(Document("\$who", 1)).blockingGetResult()
//                Assert.fail()
//            } catch (ex: ExecutionException) {
//                // FIXME: add assertion
//                val a = 0
//            }
        }
    }

    @Test
    fun deleteOne_singleDocument() {
        with(getCollectionInternal(COLLECTION_NAME)) {
            assertEquals(0, count().blockingGetResult())

            val rawDoc = Document(KEY_1, VALUE_1)
            val doc1 = Document(rawDoc)

            insertOne(doc1).blockingGetResult()
            assertEquals(1, count().blockingGetResult())
            assertEquals(1, deleteOne(doc1).blockingGetResult()!!.deletedCount)
            assertEquals(0, count().blockingGetResult())
        }
    }

    @Test
    fun deleteOne_listOfDocuments() {
        with(getCollectionInternal(COLLECTION_NAME)) {
            assertEquals(0, count().blockingGetResult())

            val rawDoc = Document(KEY_1, VALUE_1)
            val doc1 = Document(rawDoc)
            val doc1b = Document(rawDoc)
            val doc2 = Document("foo", "bar")
            val doc3 = Document("42", "666")
            insertMany(listOf(doc1, doc1b, doc2, doc3)).blockingGetResult()
            assertEquals(1, deleteOne(rawDoc).blockingGetResult()!!.deletedCount)
            assertEquals(1, deleteOne(Document()).blockingGetResult()!!.deletedCount)
        }
    }

    @Test
    fun deleteMany_singleDocument() {
        with(getCollectionInternal(COLLECTION_NAME)) {
            assertEquals(0, count().blockingGetResult())

            val rawDoc = Document(KEY_1, VALUE_1)
            val doc1 = Document(rawDoc)

            insertOne(doc1).blockingGetResult()
            assertEquals(1, count().blockingGetResult())
            assertEquals(1, deleteMany(doc1).blockingGetResult()!!.deletedCount)
            assertEquals(0, count().blockingGetResult())
        }
    }

    @Test
    fun deleteMany_listOfDocuments() {
        with(getCollectionInternal(COLLECTION_NAME)) {
            assertEquals(0, count().blockingGetResult())

            val rawDoc = Document(KEY_1, VALUE_1)
            val doc1 = Document(rawDoc)
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
            val doc1 = Document("hello", "world1")
            val doc2 = Document("hello", "world2")
            val doc3 = Document("hello", "world3")

            // Test findOne() on empty collection with no filter and no options
            assertNull(findOne().blockingGetResult())

            // Insert a document into the collection
            insertOne(doc1).blockingGetResult()
            assertEquals(1, count().blockingGetResult())

            // Test findOne() with no filter and no options
            assertEquals(doc1, findOne().blockingGetResult()!!.withoutId())

            // Test findOne() with filter that does not match any documents and no options
            assertNull(findOne(Document("hello", "worldDNE")).blockingGetResult())

            // FIXME: revisit this later
//            // Insert 2 more documents into the collection
////            insertMany(listOf(doc2, doc3)).blockingGetResult()    // use insertOne for now
//            insertOne(doc2).blockingGetResult()
//            insertOne(doc3).blockingGetResult()
//            assertEquals(3, count().blockingGetResult())
//
//            // test findOne() with projection and sort options
//            val projection = Document("hello", 1)
//            projection["_id"] = 0
//            val options1 = RemoteFindOptions()
//                    .limit(2)
//                    .projection(projection)
//                    .sort(Document("hello", 1))
//            assertEquals(doc1, findOne(Document(), options1).blockingGetResult()!!.withoutId())
//
//            val options2 = RemoteFindOptions()
//                    .limit(2)
//                    .projection(projection)
//                    .sort(Document("hello", -1))
//            assertEquals(doc3.withoutId(), findOne(Document(), options2).blockingGetResult()!!.withoutId())
//
//            // test findOne() properly fails
//            try {
//                Tasks.await(coll.findOne(Document("\$who", 1)))
//                Assert.fail()
//            } catch (ex: ExecutionException) {
//                Assert.assertTrue(ex.cause is StitchServiceException)
//                val svcEx = ex.cause as StitchServiceException
//                assertEquals(StitchServiceErrorCode.MONGODB_ERROR, svcEx.errorCode)
//            }
        }
    }

    @Test
    fun updateOne() {
        with(getCollectionInternal(COLLECTION_NAME)) {
            val doc1 = Document("hello", "world")
            val result1 = updateOne(Document(), doc1).blockingGetResult()
            assertEquals(0, result1!!.matchedCount)
            assertEquals(0, result1.modifiedCount)
            assertTrue(result1.upsertedId!!.isNull)

            val options2 = RemoteUpdateOptions().upsert(true)
            val result2 = updateOne(Document(), doc1, options2).blockingGetResult()
            assertEquals(0, result2!!.matchedCount)
            assertEquals(0, result2.modifiedCount)
            assertFalse(result2.upsertedId!!.isNull)
//            result = Tasks.await(coll.updateOne(Document(), Document("\$set", Document("woof", "meow"))))
//            assertEquals(1, result.matchedCount)
//            assertEquals(1, result.modifiedCount)
//            assertNull(result.upsertedId)
//            val expectedDoc = Document("hello", "world")
//            expectedDoc["woof"] = "meow"
//            assertEquals(expectedDoc, withoutId(Tasks.await(coll.find(Document()).first())))
//
//            try {
//                Tasks.await(coll.updateOne(Document("\$who", 1), Document()))
//                fail()
//            } catch (ex: ExecutionException) {
//                assertTrue(ex.cause is StitchServiceException)
//                val svcEx = ex.cause as StitchServiceException
//                assertEquals(StitchServiceErrorCode.MONGODB_ERROR, svcEx.errorCode)
//            }
        }
    }

    // FIXME: more to come

    private fun getCollectionInternal(collectionName: String, javaClass: Class<Document>? = null): MongoCollection<Document> {
        return when (javaClass) {
            null -> database.getCollection(collectionName)
            else -> database.getCollection(collectionName, javaClass)
        }
    }

    private fun Document.withId(objectId: ObjectId? = null): Document {
        return apply { this["_id"] = objectId ?: ObjectId() }
    }

    private fun Document.withoutId(): Document {
        return apply { remove("_id") }
    }
}
