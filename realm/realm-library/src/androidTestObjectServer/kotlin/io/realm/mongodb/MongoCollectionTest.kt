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
import io.realm.log.LogLevel
import io.realm.log.RealmLog
import io.realm.mongodb.mongo.MongoClient
import io.realm.mongodb.mongo.MongoCollection
import io.realm.mongodb.mongo.MongoDatabase
import io.realm.mongodb.mongo.options.CountOptions
import io.realm.mongodb.mongo.options.FindOneAndModifyOptions
import io.realm.mongodb.mongo.options.FindOptions
import io.realm.mongodb.mongo.options.UpdateOptions
import io.realm.util.blockingGetResult
import io.realm.util.mongodb.CustomType
import org.bson.Document
import org.bson.codecs.configuration.CodecConfigurationException
import org.bson.codecs.configuration.CodecRegistries
import org.bson.types.ObjectId
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.*

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

        RealmLog.setLevel(LogLevel.DEBUG)

        app = TestRealmApp()
        user = app.registerUserAndLogin(TestHelper.getRandomEmail(), "123456")
        client = user.getMongoClient(SERVICE_NAME)
        database = client.getDatabase(DATABASE_NAME)
    }

    @After
    fun tearDown() {
        with(getCollectionInternal()) {
            deleteMany(Document()).blockingGetResult()
        }

        if (this::app.isInitialized) {
            app.close()
        }
    }

    @Test
    fun count() {
        with(getCollectionInternal()) {
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
            assertEquals(1, count(rawDoc, CountOptions().limit(1)).blockingGetResult())
            assertFails { count(Document("\$who", 1)).blockingGetResult() }
        }
    }

    // FIXME: break down into different tests cases
    @Test
    fun findOne() {
        with(getCollectionInternal()) {
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

            // Insert 2 more documents into the collection
            insertMany(listOf(doc2, doc3)).blockingGetResult()
            assertEquals(3, count().blockingGetResult())

            // test findOne() with projection and sort options
            val projection = Document("hello", 1)
            projection["_id"] = 0
            val options1 = FindOptions()
                    .limit(2)
                    .projection(projection)
                    .sort(Document("hello", 1))
            assertEquals(doc1, findOne(Document(), options1).blockingGetResult()!!.withoutId())

            val options2 = FindOptions()
                    .limit(2)
                    .projection(projection)
                    .sort(Document("hello", -1))
            assertEquals(doc3.withoutId(), findOne(Document(), options2).blockingGetResult()!!.withoutId())

            assertFails { findOne(Document("\$who", 1)).blockingGetResult() }
        }
    }

    @Test
    fun find() {
        with(getCollectionInternal()) {
            var iter = find()
            assertFalse(iter.iterator().blockingGetResult()!!.hasNext())
            assertNull(iter.first().blockingGetResult())

            val doc1 = Document("hello", "world")
            val doc2 = Document("hello", "friend")
            doc2["proj"] = "field"
            insertMany(listOf(doc1, doc2)).blockingGetResult()

            assertTrue(iter.iterator().blockingGetResult()!!.hasNext())
            assertEquals(doc1.withoutId(), iter.first().blockingGetResult()!!.withoutId())
            assertEquals(doc2.withoutId(),
                    iter.limit(1)
                            .sort(Document("_id", -1))
                            .iterator().blockingGetResult()!!
                            .next().withoutId())

            iter = find(doc1)
            assertTrue(iter.iterator().blockingGetResult()!!.hasNext())
            assertEquals(doc1.withoutId(),
                    iter.iterator().blockingGetResult()!!
                            .next().withoutId())

            iter = find().filter(doc1)
            assertTrue(iter.iterator().blockingGetResult()!!.hasNext())
            assertEquals(doc1.withoutId(),
                    iter.iterator().blockingGetResult()!!
                            .next().withoutId())

            val expected = Document("proj", "field")
            assertEquals(expected,
                    find(doc2)
                            .projection(Document("proj", 1))
                            .iterator().blockingGetResult()!!
                            .next().withoutId())

            val asyncIter = iter.iterator().blockingGetResult()!!
            assertEquals(doc1, asyncIter.tryNext().withoutId())

            assertFails { find(Document("\$who", 1)).first().blockingGetResult() }
        }
    }

    @Test
    fun aggregate() {
        with(getCollectionInternal()) {
            var iter = aggregate(listOf())
            assertFalse(iter.iterator().blockingGetResult()!!.hasNext())
            assertNull(iter.first().blockingGetResult())

            val doc1 = Document("hello", "world")
            val doc2 = Document("hello", "friend")
            insertMany(listOf(doc1, doc2)).blockingGetResult()
            assertTrue(iter.iterator().blockingGetResult()!!.hasNext())
            assertEquals(doc1.withoutId(), iter.first().blockingGetResult()!!.withoutId())

            iter = aggregate(listOf(Document("\$sort", Document("_id", -1)), Document("\$limit", 1)))
            assertEquals(
                    doc2.withoutId(),
                    iter.iterator().blockingGetResult()!!
                            .next().withoutId()
            )

            iter = aggregate(listOf(Document("\$match", doc1)))
            assertTrue(iter.iterator().blockingGetResult()!!.hasNext())
            assertEquals(doc1.withoutId(), iter.iterator().blockingGetResult()!!.next().withoutId())

            assertFails { aggregate(listOf(Document("\$who", 1))).first().blockingGetResult() }
        }
    }

    @Test
    fun insertOne() {
        with(getCollectionInternal()) {
            assertEquals(0, count().blockingGetResult())

            val doc1 = Document(mapOf("hello_1" to "1", "hello_2" to "2"))
            insertOne(doc1).blockingGetResult()
            assertEquals(1, count().blockingGetResult())

            val doc2 = Document("hello", "world")
            doc2["_id"] = ObjectId()

            assertEquals(doc2.getObjectId("_id"), insertOne(doc2).blockingGetResult()!!.insertedId.asObjectId().value)
            assertFailsWith(ObjectServerError::class) { insertOne(doc2).blockingGetResult() }

            val doc3 = Document("hello", "world")
            assertNotEquals(doc2.getObjectId("_id"), insertOne(doc3).blockingGetResult()!!.insertedId.asObjectId().value)
        }
    }

    @Test
    fun insertMany() {
        with(getCollectionInternal()) {
            val doc1 = Document("hello", "world")
            doc1["_id"] = ObjectId()

            assertEquals(doc1.getObjectId("_id"),
                    insertMany(listOf(doc1)).blockingGetResult()!!.insertedIds[0]!!.asObjectId().value)
            assertFailsWith(ObjectServerError::class) {
                insertMany(listOf(doc1)).blockingGetResult()
            }.also {
                assertEquals(ErrorCode.MONGODB_ERROR, it.errorCode)
                assertNotNull(it.errorMessage)
                assertTrue(it.errorMessage!!.contains("duplicate"))
            }

            val doc2 = Document("hello", "world")

            assertNotEquals(doc1.getObjectId("_id"), insertMany(listOf(doc2)).blockingGetResult()!!.insertedIds[0]!!.asObjectId().value)

            val doc3 = Document("one", "two")
            val doc4 = Document("three", 4)

            insertMany(listOf(doc3, doc4)).blockingGetResult()
        }
    }

    @Test
    fun deleteOne_singleDocument() {
        with(getCollectionInternal()) {
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
        with(getCollectionInternal()) {
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
        with(getCollectionInternal()) {
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
        with(getCollectionInternal()) {
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
    fun updateOne() {
        with(getCollectionInternal()) {
            val doc1 = Document("hello", "world")
            val result1 = updateOne(Document(), doc1).blockingGetResult()!!
            assertEquals(0, result1.matchedCount)
            assertEquals(0, result1.modifiedCount)
            assertNull(result1.upsertedId)

            val options2 = UpdateOptions().upsert(true)
            val result2 = updateOne(Document(), doc1, options2).blockingGetResult()!!
            assertEquals(0, result2.matchedCount)
            assertEquals(0, result2.modifiedCount)
            assertFalse(result2.upsertedId!!.isNull)

            val result3 = updateOne(Document(), Document("\$set", Document("woof", "meow"))).blockingGetResult()!!
            assertEquals(1, result3.matchedCount)
            assertEquals(1, result3.modifiedCount)
            assertNull(result3.upsertedId)

            val expectedDoc = Document("hello", "world")
            expectedDoc["woof"] = "meow"
            assertEquals(expectedDoc, find(Document()).first().blockingGetResult()!!.withoutId())

            assertFails { updateOne(Document("\$who", 1), Document()).blockingGetResult() }
        }
    }

    @Test
    fun updateMany() {
        with(getCollectionInternal()) {
            val doc1 = Document("hello", "world")
            val result1 = updateMany(Document(), doc1).blockingGetResult()!!
            assertEquals(0, result1.matchedCount)
            assertEquals(0, result1.modifiedCount)
            assertNull(result1.upsertedId)

            val options2 = UpdateOptions().upsert(true)
            val result2 = updateMany(Document(), doc1, options2).blockingGetResult()!!
            assertEquals(0, result2.matchedCount)
            assertEquals(0, result2.modifiedCount)
            assertNotNull(result2.upsertedId)

            val result3 = updateMany(Document(), Document("\$set", Document("woof", "meow"))).blockingGetResult()!!
            assertEquals(1, result3.matchedCount)
            assertEquals(1, result3.modifiedCount)
            assertNull(result3.upsertedId)

            insertOne(Document()).blockingGetResult()
            val result4 = updateMany(Document(), Document("\$set", Document("woof", "meow"))).blockingGetResult()!!
            assertEquals(2, result4.matchedCount)
            assertEquals(2, result4.modifiedCount)
        }
    }

    @Test
    fun findOneAndUpdate() {
        with(getCollectionInternal()) {
            val sampleDoc = Document("hello", "world1")
            sampleDoc["num"] = 2

            // Collection should start out empty
            // This also tests the null return format
            assertNull(findOneAndUpdate(Document(), Document()).blockingGetResult())

            // Insert a sample Document
            insertOne(sampleDoc).blockingGetResult()
            assertEquals(1, count().blockingGetResult())

            // Sample call to findOneAndUpdate() where we get the previous document back
            val sampleUpdate = Document("\$set", Document("hello", "hellothere"))
            sampleUpdate["\$inc"] = Document("num", 1)
            assertEquals(sampleDoc.withoutId(),
                    findOneAndUpdate(
                            Document("hello", "world1"),
                            sampleUpdate
                    ).blockingGetResult()!!.withoutId())
            assertEquals(1, count().blockingGetResult())

            // Make sure the update took place
            val expectedDoc = Document("hello", "hellothere")
            expectedDoc["num"] = 3
            assertEquals(expectedDoc.withoutId(),
                    find().first().blockingGetResult()!!.withoutId())
            assertEquals(1, count().blockingGetResult())

            // Call findOneAndUpdate() again but get the new document
            sampleUpdate.remove("\$set")
            expectedDoc["num"] = 4
            var result = findOneAndUpdate(
                    Document("hello", "hellothere"),
                    sampleUpdate,
                    FindOneAndModifyOptions()
                            .returnNewDocument(true)
            ).blockingGetResult()
            assertEquals(expectedDoc.withoutId(), result!!.withoutId())
            assertEquals(1, count().blockingGetResult())

            // Test null behaviour again with a filter that should not match any documents
            val nullResponse = findOneAndUpdate(
                    Document("hello", "zzzzz"),
                    Document()
            ).blockingGetResult()
            assertNull(nullResponse)
            assertEquals(1, count().blockingGetResult())

            val doc1 = Document("hello", "world1")
            doc1["num"] = 1

            val doc2 = Document("hello", "world2")
            doc2["num"] = 2

            val doc3 = Document("hello", "world3")
            doc3["num"] = 3

            // Test the upsert option where it should not actually be invoked
            var options = FindOneAndModifyOptions()
                    .returnNewDocument(true)
                    .upsert(true)
            result = findOneAndUpdate(
                    Document("hello", "hellothere"),
                    Document("\$set", doc1),
                    options
            ).blockingGetResult()
            assertEquals(doc1, result!!.withoutId())
            assertEquals(1, count().blockingGetResult())
            assertEquals(doc1.withoutId(),
                    find().first().blockingGetResult()!!.withoutId())

            // Test the upsert option where the server should perform upsert and return new document
            options = FindOneAndModifyOptions()
                    .returnNewDocument(true)
                    .upsert(true)
            result = findOneAndUpdate(
                    Document("hello", "hellothere"),
                    Document("\$set", doc2),
                    options
            ).blockingGetResult()
            assertEquals(doc2, result!!.withoutId())
            assertEquals(2, count().blockingGetResult())

            // Test the upsert option where the server should perform upsert and return old document
            // The old document should be empty
            options = FindOneAndModifyOptions()
                    .upsert(true)
            result = findOneAndUpdate(
                    Document("hello", "hellothere"),
                    Document("\$set", doc3),
                    options
            ).blockingGetResult()
            assertNull(result)
            assertEquals(3, count().blockingGetResult())

            // FIXME: projections and sorts aren't currently working due to a bug in Stitch: https://jira.mongodb.org/browse/REALMC-5787
//            // Test sort and project
//            val sampleProject = Document("hello", 1)
//            sampleProject["_id"] = 0
//
//            options = FindOneAndModifyOptions()
//                    .projection(sampleProject)
//                    .sort(Document("num", 1))
//            result = findOneAndUpdate(
//                    Document(),
//                    sampleUpdate,
//                    options
//            ).blockingGetResult()
//            assertEquals(Document("hello", "world1"), result!!.withoutId())
//            assertEquals(3, count().blockingGetResult())
//
//            options = FindOneAndModifyOptions()
//                    .projection(sampleProject)
//                    .sort(Document("num", -1))
//            result = findOneAndUpdate(
//                    Document(),
//                    sampleUpdate,
//                    options
//            ).blockingGetResult()
//            assertEquals(Document("hello", "world3"), result!!.withoutId())
//            assertEquals(3, count().blockingGetResult())

            // Test proper failure
            assertFailsWith(ObjectServerError::class) {
                findOneAndUpdate(Document(), Document("\$who", 1)).blockingGetResult()
            }.also {
                assertEquals(ErrorCode.MONGODB_ERROR, it.errorCode)
                assertNotNull(it.errorMessage)
                assertTrue(it.errorMessage!!.contains("modifier"))
            }

            assertFailsWith(ObjectServerError::class) {
                findOneAndUpdate(Document(), Document("\$who", 1), FindOneAndModifyOptions().upsert(true)).blockingGetResult()
            }.also {
                assertEquals(ErrorCode.MONGODB_ERROR, it.errorCode)
                assertNotNull(it.errorMessage)
                assertTrue(it.errorMessage!!.contains("modifier"))
            }
        }
    }

    @Test
    fun testFindOneAndReplace() {
        with(getCollectionInternal()) {
            val sampleDoc = Document("hello", "world1")
            sampleDoc["num"] = 2

            // Collection should start out empty
            // This also tests the null return format
            assertNull(findOneAndReplace(Document(), Document()).blockingGetResult())

            // Insert a sample Document
            insertOne(sampleDoc).blockingGetResult()
            assertEquals(1, count().blockingGetResult())

            // Sample call to findOneAndReplace() where we get the previous document back
            var sampleUpdate = Document("hello", "world2")
            sampleUpdate["num"] = 2
            assertEquals(sampleDoc.withoutId(),
                    findOneAndReplace(Document("hello", "world1"), sampleUpdate).blockingGetResult()!!.withoutId())
            assertEquals(1, count().blockingGetResult())

            // Make sure the update took place
            val expectedDoc = Document("hello", "world2")
            expectedDoc["num"] = 2
            assertEquals(expectedDoc.withoutId(), find().first().blockingGetResult()!!.withoutId())
            assertEquals(1, count().blockingGetResult())

            // Call findOneAndReplace() again but get the new document
            sampleUpdate = Document("hello", "world3")
            sampleUpdate["num"] = 3
            var options = FindOneAndModifyOptions().returnNewDocument(true)
            assertEquals(sampleUpdate.withoutId(),
                    findOneAndReplace(Document(), sampleUpdate, options).blockingGetResult()!!.withoutId())
            assertEquals(1, count().blockingGetResult())

            // Test null behaviour again with a filter that should not match any documents
            assertNull(findOneAndReplace(Document("hello", "zzzzz"), Document()).blockingGetResult())
            assertEquals(1, count().blockingGetResult())

            val doc4 = Document("hello", "world4")
            doc4["num"] = 4

            val doc5 = Document("hello", "world5")
            doc5["num"] = 5

            val doc6 = Document("hello", "world6")
            doc6["num"] = 6

            // Test the upsert option where it should not actually be invoked
            sampleUpdate = Document("hello", "world4")
            sampleUpdate["num"] = 4
            options = FindOneAndModifyOptions().returnNewDocument(true).upsert(true)
            assertEquals(doc4.withoutId(),
                    findOneAndReplace(Document("hello", "world3"), doc4, options).blockingGetResult()!!.withoutId())
            assertEquals(1, count().blockingGetResult())
            assertEquals(doc4.withoutId(), find().first().blockingGetResult()!!.withoutId())

            // Test the upsert option where the server should perform upsert and return new document
            options = FindOneAndModifyOptions().returnNewDocument(true).upsert(true)
            assertEquals(doc5.withoutId(), findOneAndReplace(Document("hello", "hellothere"), doc5, options).blockingGetResult()!!.withoutId())
            assertEquals(2, count().blockingGetResult())

            // Test the upsert option where the server should perform upsert and return old document
            // The old document should be empty
            options = FindOneAndModifyOptions().upsert(true)
            assertNull(findOneAndReplace(Document("hello", "hellothere"), doc6, options).blockingGetResult())
            assertEquals(3, count().blockingGetResult())

            // FIXME: projections and sorts aren't currently working due to a bug in Stitch: https://jira.mongodb.org/browse/REALMC-5787
//            // Test sort and project
//            val sampleProject = Document("hello", 1)
//            sampleProject["_id"] = 0
//
//            sampleUpdate = Document("hello", "world0")
//            sampleUpdate["num"] = 0
//
//            options = FindOneAndModifyOptions().projection(sampleProject).sort(Document("num", 1))
//            assertEquals(Document("hello", "world4"),
//                    findOneAndReplace(Document(), sampleUpdate, options).blockingGetResult()!!.withoutId())
//            assertEquals(3, count().blockingGetResult())
//
//            options = FindOneAndModifyOptions()
//                    .projection(sampleProject)
//                    .sort(Document("num", -1))
//            assertEquals(Document("hello", "world6"),
//                    findOneAndReplace(Document(), sampleUpdate, options).blockingGetResult()!!.withoutId())
//            assertEquals(3, count().blockingGetResult())

            // Test proper failure
            assertFailsWith(ObjectServerError::class) {
                findOneAndReplace(Document(), Document("\$who", 1)).blockingGetResult()
            }.also {
                assertEquals(ErrorCode.INVALID_PARAMETER, it.errorCode)
                assertNotNull(it.errorMessage)
            }

            assertFailsWith(ObjectServerError::class) {
                findOneAndReplace(Document(), Document("\$who", 1), FindOneAndModifyOptions().upsert(true)).blockingGetResult()
            }.also {
                assertEquals(ErrorCode.INVALID_PARAMETER, it.errorCode)
                assertNotNull(it.errorMessage)
            }
        }
    }

    // FIXME: un-ignore when fixed in OS
    @Test
    @Ignore("find_one_and_delete function is wrongly implemented in OS")
    fun testFindOneAndDelete() {
        with(getCollectionInternal()) {
            val sampleDoc = Document("hello", "world1")
            sampleDoc["num"] = 1

            // Collection should start out empty
            // This also tests the null return format
            assertNull(findOneAndDelete(Document()).blockingGetResult())

            // Insert a sample Document
            insertOne(sampleDoc).blockingGetResult()
            assertEquals(1, count().blockingGetResult())

            // Sample call to findOneAndDelete() where we delete the only doc in the collection
            assertEquals(sampleDoc.withoutId(),
                    findOneAndDelete(Document()).blockingGetResult()!!.withoutId())

            // There should be no documents in the collection now
            assertEquals(0, count().blockingGetResult())

            // Insert a sample Document
            insertOne(sampleDoc).blockingGetResult()
            assertEquals(1, count().blockingGetResult())

            // Call findOneAndDelete() again but this time with a filter
            assertEquals(sampleDoc.withoutId(),
                    findOneAndDelete(Document("hello", "world1")).blockingGetResult()!!.withoutId())

            // There should be no documents in the collection now
            assertEquals(0, count().blockingGetResult())

            // Insert a sample Document
            insertOne(sampleDoc).blockingGetResult()
            assertEquals(1, count().blockingGetResult())

            // Test null behaviour again with a filter that should not match any documents
            assertNull(findOneAndDelete(Document("hello", "zzzzz")).blockingGetResult())
            assertEquals(1, count().blockingGetResult())

            val doc2 = Document("hello", "world2")
            doc2["num"] = 2

            val doc3 = Document("hello", "world3")
            doc3["num"] = 3

            // Insert new documents
            insertMany(listOf(doc2, doc3)).blockingGetResult()
            assertEquals(3, count().blockingGetResult())

//            // FIXME: projections and sorts aren't currently working due to a bug in Stitch: https://jira.mongodb.org/browse/REALMC-5787
//            // Test sort and project
//            assertEquals(withoutIds(listOf(sampleDoc, doc2, doc3)),
//                    withoutIds(Tasks.await<MutableList<Document>>(coll.find().into(mutableListOf()))))
//
//            val sampleProject = Document("hello", 1)
//            sampleProject["_id"] = 0
//
//            assertEquals(Document("hello", "world3"), withoutId(Tasks.await(coll.findOneAndDelete(
//                    Document(),
//                    RemoteFindOneAndModifyOptions()
//                            .projection(sampleProject)
//                            .sort(Document("num", -1))
//            ))))
//            assertEquals(2, Tasks.await(coll.count()))
//
//            assertEquals(Document("hello", "world1"), withoutId(Tasks.await(coll.findOneAndDelete(
//                    Document(),
//                    RemoteFindOneAndModifyOptions()
//                            .projection(sampleProject)
//                            .sort(Document("num", 1))
//            ))))
//            assertEquals(1, Tasks.await(coll.count()))
        }
    }

    @Test
    fun testWithDocument() {
        // add support for Bson documents as it's needed for proper collection initialization
        val expandedCodecRegistry = CodecRegistries
                .fromRegistries(RealmAppConfiguration.DEFAULT_BSON_CODEC_REGISTRY,
                        CodecRegistries.fromCodecs(CustomType.Codec()))

        val expected = CustomType(ObjectId(), 42)

        with(getCollectionInternal()) {
            var coll = withDocumentClass(CustomType::class.java)
            assertEquals(CustomType::class.java, coll.documentClass)

            assertFailsWith(CodecConfigurationException::class) {
                coll.insertOne(expected).blockingGetResult()
            }

            val defaultCodecRegistry = RealmAppConfiguration.DEFAULT_BSON_CODEC_REGISTRY
            assertEquals(defaultCodecRegistry, coll.codecRegistry)

            coll = coll.withCodecRegistry(expandedCodecRegistry)
            assertEquals(expected.id,
                    coll.insertOne(expected).blockingGetResult()!!.insertedId.asObjectId().value)
            assertEquals(expected, coll.find().first().blockingGetResult())
        }

        val expected2 = CustomType(null, 42)

        with(getCollectionInternal(CustomType::class.java)
                .withCodecRegistry(expandedCodecRegistry)) {
            insertOne(expected2).blockingGetResult()!!
            val actual = find().first().blockingGetResult()!!
            assertEquals(expected2.intValue, actual.intValue)
            assertNotNull(expected.id)
        }

        with(getCollectionInternal(CustomType::class.java)
                .withCodecRegistry(expandedCodecRegistry)) {
            val actual = find(Document(), CustomType::class.java).first().blockingGetResult()!!
            assertEquals(expected2.intValue, actual.intValue)
            assertNotNull(expected.id)

            val iter = aggregate(listOf(Document("\$match", Document())), CustomType::class.java)
            assertTrue(iter.iterator().blockingGetResult()!!.hasNext())
            assertEquals(expected, iter.iterator().blockingGetResult()!!.next())
        }
    }

    // FIXME: more to come

    private fun getCollectionInternal(): MongoCollection<Document> =
            database.getCollection(COLLECTION_NAME)

    private fun <ResultT> getCollectionInternal(resultClass: Class<ResultT>): MongoCollection<ResultT> =
            database.getCollection(COLLECTION_NAME, resultClass)

    private fun Document.withId(objectId: ObjectId? = null): Document {
        return apply { this["_id"] = objectId ?: ObjectId() }
    }

    private fun Document.withoutId(): Document {
        return apply { remove("_id") }
    }

    private fun List<Document>.withoutIds(): List<Document> {
        return apply { map { it.withoutId() } }
    }
}
