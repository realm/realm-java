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
import io.realm.mongodb.mongo.MongoNamespace
import io.realm.mongodb.mongo.options.CountOptions
import io.realm.mongodb.mongo.options.FindOneAndModifyOptions
import io.realm.mongodb.mongo.options.FindOptions
import io.realm.mongodb.mongo.options.UpdateOptions
import io.realm.util.assertFailsWithErrorCode
import io.realm.util.mongodb.CustomType
import org.bson.Document
import org.bson.codecs.configuration.CodecRegistries
import org.bson.types.ObjectId
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.*

private const val COLLECTION_NAME = "mongo_data" // name of collection used by tests

@RunWith(AndroidJUnit4::class)
class MongoClientTest {

    private lateinit var app: TestApp
    private lateinit var user: User
    private lateinit var client: MongoClient

    @Before
    fun setUp() {
        Realm.init(InstrumentationRegistry.getInstrumentation().targetContext)
        app = TestApp()
        user = app.registerUserAndLogin(TestHelper.getRandomEmail(), "123456")
        client = user.getMongoClient(SERVICE_NAME)
    }

    @After
    fun tearDown() {
        if (this::client.isInitialized) {
            with(getCollectionInternal()) {
                deleteMany(Document()).get()
            }
        }
        if (this::app.isInitialized) {
            app.close()
        }
    }

    @Test
    fun count() {
        with(getCollectionInternal()) {
            assertEquals(0, count().get())

            val rawDoc = Document("hello", "world")
            val doc1 = Document(rawDoc)
            val doc2 = Document(rawDoc)
            insertOne(doc1).get()
            assertEquals(1, count().get())
            insertOne(doc2).get()
            assertEquals(2, count().get())

            assertEquals(2, count(rawDoc).get())
            assertEquals(0, count(Document("hello", "Friend")).get())
            assertEquals(1, count(rawDoc, CountOptions().limit(1)).get())

            assertFailsWithErrorCode(ErrorCode.MONGODB_ERROR) {
                count(Document("\$who", 1)).get()
            }.also { e ->
                assertTrue(e.errorMessage!!.contains("operator", true))
            }
        }
    }

    @Test
    fun count_fails() {
        with(getCollectionInternal()) {
            assertFailsWithErrorCode(ErrorCode.MONGODB_ERROR) {
                count(Document("\$who", 1)).get()
            }.also { e ->
                assertTrue(e.errorMessage!!.contains("operator", true))
            }
        }
    }

    @Test
    fun findOne_nullResult() {
        with(getCollectionInternal()) {
            // Test findOne() on empty collection with no filter and no options
            assertNull(findOne().get())

            // Test findOne() with filter that does not match any documents and no options
            assertNull(findOne(Document("hello", "worldDNE")).get())

            val doc1 = Document("hello", "world1")
            insertOne(doc1).get()
            assertEquals(1, count().get())

            // Test findOne() with filter that does not match any documents and no options
            assertNull(findOne(Document("hello", "worldDNE")).get())
        }
    }

    @Test
    fun findOne_singleDocument() {
        with(getCollectionInternal()) {
            val doc1 = Document("hello", "world1")

            // Insert one document
            insertOne(doc1).get()
            assertEquals(1, count().get())

            // No filter and no options
            assertEquals(doc1, findOne().get()!!.withoutId())

            // Projection (remove "_id") options
            val projection = Document("hello", 1).apply { this["_id"] = 0 }
            var options = FindOptions()
                    .limit(2)
                    .projection(projection)
            assertEquals(doc1, findOne(Document(), options).get()!!)

            // Projection (remove "_id") and sort (by desc "hello") options
            options = FindOptions()
                    .limit(2)
                    .projection(projection)
                    .sort(Document("hello", -1))
            assertEquals(doc1, findOne(Document(), options).get()!!)
        }
    }

    @Test
    fun findOne_multipleDocuments() {
        with(getCollectionInternal()) {
            val doc1 = Document("hello", "world1")
            val doc2 = Document("hello", "world2")
            val doc3 = Document("hello", "world3")

            // Insert 3 documents
            insertMany(listOf(doc1, doc2, doc3)).get()
            assertEquals(3, count().get())

            // Projection (remove "_id") and sort (by asc "hello") options
            val projection = Document("hello", 1).apply { this["_id"] = 0 }
            var options = FindOptions()
                    .limit(2)
                    .projection(projection)
                    .sort(Document("hello", 1))
            assertEquals(doc1, findOne(Document(), options).get()!!)

            // Projection (remove "_id") and sort (by desc "hello") options
            options = FindOptions()
                    .limit(2)
                    .projection(projection)
                    .sort(Document("hello", -1))
            assertEquals(doc3, findOne(Document(), options).get()!!)
        }
    }

    @Test
    fun findOne_fails() {
        with(getCollectionInternal()) {
            assertFailsWithErrorCode(ErrorCode.MONGODB_ERROR) {
                findOne(Document("\$who", 1)).get()
            }.also { e ->
                assertTrue(e.errorMessage!!.contains("operator", true))
            }
        }
    }

    @Test
    fun find() {
        with(getCollectionInternal()) {
            // Find on an empty collection returns false on hasNext and null on first
            var iter = find()
            assertFalse(iter.iterator().get()!!.hasNext())
            assertNull(iter.first().get())

            val doc1 = Document("hello", "world")
            val doc2 = Document("hello", "friend")
            doc2["proj"] = "field"
            insertMany(listOf(doc1, doc2)).get()

            // Iterate after inserting two documents
            assertTrue(iter.iterator().get()!!.hasNext())
            assertEquals(doc1, iter.first().get()!!.withoutId())

            // Get next with sort by desc "_id" and limit to 1 document
            assertEquals(doc2,
                    iter.limit(1)
                            .sort(Document("_id", -1))
                            .iterator().get()!!
                            .next().withoutId())

            // Find first document
            iter = find(doc1)
            assertTrue(iter.iterator().get()!!.hasNext())
            assertEquals(doc1,
                    iter.iterator().get()!!
                            .next().withoutId())

            // Find with filter for first document
            iter = find().filter(doc1)
            assertTrue(iter.iterator().get()!!.hasNext())
            assertEquals(doc1,
                    iter.iterator().get()!!
                            .next().withoutId())

            // Find with projection shows "proj" in result
            val expected = Document("proj", "field")
            assertEquals(expected,
                    find(doc2)
                            .projection(Document("proj", 1))
                            .iterator().get()!!
                            .next().withoutId())

            // Getting a new iterator returns first element on tryNext
            val asyncIter = iter.iterator().get()!!
            assertEquals(doc1, asyncIter.tryNext().withoutId())
        }
    }

    @Test
    fun find_fails() {
        with(getCollectionInternal()) {
            assertFailsWithErrorCode(ErrorCode.MONGODB_ERROR) {
                find(Document("\$who", 1)).first().get()
            }.also { e ->
                assertTrue(e.errorMessage!!.contains("operator", true))
            }
        }
    }

    @Test
    fun aggregate() {
        with(getCollectionInternal()) {
            // Aggregate on an empty collection returns false on hasNext and null on first
            var iter = aggregate(listOf())
            assertFalse(iter.iterator().get()!!.hasNext())
            assertNull(iter.first().get())

            // Iterate after inserting two documents
            val doc1 = Document("hello", "world")
            val doc2 = Document("hello", "friend")
            insertMany(listOf(doc1, doc2)).get()
            assertTrue(iter.iterator().get()!!.hasNext())
            assertEquals(doc1.withoutId(), iter.first().get()!!.withoutId())

            // Aggregate with pipeline, sort by desc "_id" and limit to 1 document
            iter = aggregate(listOf(Document("\$sort", Document("_id", -1)), Document("\$limit", 1)))
            assertEquals(doc2.withoutId(),
                    iter.iterator().get()!!
                            .next().withoutId())

            // Aggregate with pipeline, match first document
            iter = aggregate(listOf(Document("\$match", doc1)))
            assertTrue(iter.iterator().get()!!.hasNext())
            assertEquals(doc1.withoutId(), iter.iterator().get()!!.next().withoutId())
        }
    }

    @Test
    fun aggregate_fails() {
        with(getCollectionInternal()) {
            assertFailsWithErrorCode(ErrorCode.MONGODB_ERROR) {
                aggregate(listOf(Document("\$who", 1))).first().get()
            }.also { e ->
                assertTrue(e.errorMessage!!.contains("pipeline", true))
            }
        }
    }

    @Test
    fun insertOne() {
        with(getCollectionInternal()) {
            val doc1 = Document("hello", "world").apply { this["_id"] = ObjectId() }
            assertEquals(doc1.getObjectId("_id"), insertOne(doc1).get()!!.insertedId.asObjectId().value)
            assertEquals(1, count().get())

            val doc2 = Document("hello", "world")
            assertNotEquals(doc1.getObjectId("_id"), insertOne(doc2).get()!!.insertedId.asObjectId().value)
            assertEquals(2, count().get())
        }
    }

    @Test
    fun insertOne_fails() {
        with(getCollectionInternal()) {
            val doc1 = Document("hello", "world").apply { this["_id"] = ObjectId() }
            insertOne(doc1).get()

            assertFailsWithErrorCode(ErrorCode.MONGODB_ERROR) {
                insertOne(doc1).get()
            }.also { e ->
                assertTrue(e.errorMessage!!.contains("duplicate", true))
            }
        }
    }

    @Test
    fun insertMany_singleDocument() {
        with(getCollectionInternal()) {
            val doc1 = Document("hello", "world").apply { this["_id"] = ObjectId() }

            assertEquals(doc1.getObjectId("_id"),
                    insertMany(listOf(doc1)).get()!!.insertedIds[0]!!.asObjectId().value)
            val doc2 = Document("hello", "world")

            assertNotEquals(doc1.getObjectId("_id"), insertMany(listOf(doc2)).get()!!.insertedIds[0]!!.asObjectId().value)

            val doc3 = Document("one", "two")
            val doc4 = Document("three", 4)

            insertMany(listOf(doc3, doc4)).get()
        }
    }

    @Test
    fun insertMany_singleDocument_fails() {
        with(getCollectionInternal()) {
            val doc1 = Document("hello", "world").apply { this["_id"] = ObjectId() }
            insertMany(listOf(doc1)).get()

            assertFailsWithErrorCode(ErrorCode.MONGODB_ERROR) {
                insertMany(listOf(doc1)).get()
            }.also { e ->
                assertTrue(e.errorMessage!!.contains("duplicate", true))
            }
        }
    }

    @Test
    fun insertMany_multipleDocuments() {
        with(getCollectionInternal()) {
            val doc1 = Document("hello", "world").apply { this["_id"] = ObjectId() }
            val doc2 = Document("hello", "world").apply { this["_id"] = ObjectId() }
            val documents = listOf(doc1, doc2)

            insertMany(documents).get()!!
                    .insertedIds
                    .forEach { entry ->
                        assertEquals(documents[entry.key.toInt()]["_id"], entry.value.asObjectId().value)
                    }

            val doc3 = Document("one", "two")
            val doc4 = Document("three", 4)

            insertMany(listOf(doc3, doc4)).get()
            assertEquals(4, count().get())
        }
    }

    @Test
    fun insertMany_multipleDocuments_fails() {
        with(getCollectionInternal()) {
            val doc1 = Document("hello", "world").apply { this["_id"] = ObjectId() }
            val doc2 = Document("hello", "world").apply { this["_id"] = ObjectId() }
            val documents = listOf(doc1, doc2)
            insertMany(documents).get()

            assertFailsWithErrorCode(ErrorCode.MONGODB_ERROR) {
                insertMany(documents).get()
            }.also { e ->
                assertTrue(e.errorMessage!!.contains("duplicate", true))
            }
        }
    }

    @Test
    fun deleteOne_singleDocument() {
        with(getCollectionInternal()) {
            assertEquals(0, deleteOne(Document()).get()!!.deletedCount)
            assertEquals(0, deleteOne(Document("hello", "world")).get()!!.deletedCount)

            val doc1 = Document("hello", "world")

            insertOne(doc1).get()
            assertEquals(1, deleteOne(doc1).get()!!.deletedCount)
            assertEquals(0, count().get())
        }
    }

    @Test
    fun deleteOne_fails() {
        with(getCollectionInternal()) {
            assertFailsWithErrorCode(ErrorCode.MONGODB_ERROR) {
                deleteOne(Document("\$who", 1)).get()
            }.also { e ->
                assertTrue(e.errorMessage!!.contains("operator", true))
            }
        }
    }

    @Test
    fun deleteOne_multipleDocuments() {
        with(getCollectionInternal()) {
            assertEquals(0, count().get())

            val rawDoc = Document("hello", "world")
            val doc1 = Document(rawDoc)
            val doc1b = Document(rawDoc)
            val doc2 = Document("foo", "bar")
            val doc3 = Document("42", "666")
            insertMany(listOf(doc1, doc1b, doc2, doc3)).get()
            assertEquals(1, deleteOne(rawDoc).get()!!.deletedCount)
            assertEquals(1, deleteOne(Document()).get()!!.deletedCount)
            assertEquals(2, count().get())
        }
    }

    @Test
    fun deleteMany_singleDocument() {
        with(getCollectionInternal()) {
            assertEquals(0, count().get())

            val rawDoc = Document("hello", "world")
            val doc1 = Document(rawDoc)

            insertOne(doc1).get()
            assertEquals(1, count().get())
            assertEquals(1, deleteMany(doc1).get()!!.deletedCount)
            assertEquals(0, count().get())
        }
    }

    @Test
    fun deleteMany_multipleDocuments() {
        with(getCollectionInternal()) {
            assertEquals(0, count().get())

            val rawDoc = Document("hello", "world")
            val doc1 = Document(rawDoc)
            val doc1b = Document(rawDoc)
            val doc2 = Document("foo", "bar")
            val doc3 = Document("42", "666")
            insertMany(listOf(doc1, doc1b, doc2, doc3)).get()
            assertEquals(2, deleteMany(rawDoc).get()!!.deletedCount)                 // two docs will be deleted
            assertEquals(2, count().get())                                           // two docs still present
            assertEquals(2, deleteMany(Document()).get()!!.deletedCount)             // delete all
            assertEquals(0, count().get())

            insertMany(listOf(doc1, doc1b, doc2, doc3)).get()
            assertEquals(4, deleteMany(Document()).get()!!.deletedCount)             // delete all
            assertEquals(0, count().get())
        }
    }

    @Test
    fun deleteMany_fails() {
        with(getCollectionInternal()) {
            assertFailsWithErrorCode(ErrorCode.MONGODB_ERROR) {
                deleteMany(Document("\$who", 1)).get()
            }.also { e ->
                assertTrue(e.errorMessage!!.contains("operator", true))
            }
        }
    }

    @Test
    fun updateOne_emptyCollection() {
        with(getCollectionInternal()) {
            val doc1 = Document("hello", "world")

            // Update on an empty collection
            updateOne(Document(), doc1)
                    .get()!!
                    .let {
                        assertEquals(0, it.matchedCount)
                        assertEquals(0, it.modifiedCount)
                        assertNull(it.upsertedId)
                    }

            // Update on an empty collection adding some values
            val doc2 = Document("\$set", Document("woof", "meow"))
            updateOne(Document(), doc2)
                    .get()!!
                    .let {
                        assertEquals(0, it.matchedCount)
                        assertEquals(0, it.modifiedCount)
                        assertNull(it.upsertedId)
                        assertEquals(0, count().get())
                    }
        }
    }

    @Test
    fun updateOne_emptyCollectionWithUpsert() {
        with(getCollectionInternal()) {
            val doc1 = Document("hello", "world")

            // Update on empty collection with upsert
            val options = UpdateOptions().upsert(true)
            updateOne(Document(), doc1, options)
                    .get()!!
                    .let {
                        assertEquals(0, it.matchedCount)
                        assertEquals(0, it.modifiedCount)
                        assertFalse(it.upsertedId!!.isNull)
                    }
            assertEquals(1, count().get())

            assertEquals(doc1, find(Document()).first().get()!!.withoutId())
        }
    }

    @Test
    fun updateOne_fails() {
        with(getCollectionInternal()) {
            assertFailsWithErrorCode(ErrorCode.MONGODB_ERROR) {
                updateOne(Document("\$who", 1), Document()).get()
            }.also { e ->
                assertTrue(e.errorMessage!!.contains("operator", true))
            }
        }
    }

    @Test
    fun updateMany_emptyCollection() {
        with(getCollectionInternal()) {
            val doc1 = Document("hello", "world")

            // Update on empty collection
            updateMany(Document(), doc1)
                    .get()!!
                    .let {
                        assertEquals(0, it.matchedCount)
                        assertEquals(0, it.modifiedCount)
                        assertNull(it.upsertedId)
                    }
            assertEquals(0, count().get())
        }
    }

    @Test
    fun updateMany_emptyCollectionWithUpsert() {
        with(getCollectionInternal()) {
            val doc1 = Document("hello", "world")

            // Update on empty collection with upsert
            updateMany(Document(), doc1, UpdateOptions().upsert(true))
                    .get()!!
                    .let {
                        assertEquals(0, it.matchedCount)
                        assertEquals(0, it.modifiedCount)
                        assertNotNull(it.upsertedId)
                    }
            assertEquals(1, count().get())

            // Add new value using update
            val update = Document("woof", "meow")
            updateMany(Document(), Document("\$set", update))
                    .get()!!
                    .let {
                        assertEquals(1, it.matchedCount)
                        assertEquals(1, it.modifiedCount)
                        assertNull(it.upsertedId)
                    }
            assertEquals(1, count().get())
            val expected = Document(doc1).apply { this["woof"] = "meow" }
            assertEquals(expected, find().first().get()!!.withoutId())

            // Insert empty document, add ["woof", "meow"] to it and check it worked
            insertOne(Document()).get()
            updateMany(Document(), Document("\$set", update))
                    .get()!!
                    .let {
                        assertEquals(2, it.matchedCount)
                        assertEquals(2, it.modifiedCount)
                    }
            assertEquals(2, count().get())
            find().iterator()
                    .get()!!
                    .let {
                        assertEquals(expected, it.next().withoutId())
                        assertEquals(update, it.next().withoutId())
                        assertFalse(it.hasNext())
                    }
        }
    }

    @Test
    fun updateMany_fails() {
        with(getCollectionInternal()) {
            assertFailsWithErrorCode(ErrorCode.MONGODB_ERROR) {
                updateMany(Document("\$who", 1), Document()).get()
            }.also { e ->
                assertTrue(e.errorMessage!!.contains("operator", true))
            }
        }
    }

    @Test
    fun findOneAndUpdate_emptyCollection() {
        with(getCollectionInternal()) {
            // Test null return format
            assertNull(findOneAndUpdate(Document(), Document()).get())
        }
    }

    @Test
    fun findOneAndUpdate_noUpdates() {
        with(getCollectionInternal()) {
            assertNull(findOneAndUpdate(Document(), Document()).get())
            assertEquals(0, count().get())
        }
    }

    @Test
    fun findOneAndUpdate_noUpsert() {
        with(getCollectionInternal()) {
            val sampleDoc = Document("hello", "world1")
            sampleDoc["num"] = 2

            // Insert a sample Document
            insertOne(sampleDoc).get()
            assertEquals(1, count().get())

            // Sample call to findOneAndUpdate() where we get the previous document back
            val sampleUpdate = Document("\$set", Document("hello", "hellothere")).apply {
                this["\$inc"] = Document("num", 1)
            }
            findOneAndUpdate(Document("hello", "world1"), sampleUpdate)
                    .get()!!
                    .withoutId()
                    .let {
                        assertEquals(sampleDoc.withoutId(), it)
                    }
            assertEquals(1, count().get())

            // Make sure the update took place
            val expectedDoc = Document("hello", "hellothere")
            expectedDoc["num"] = 3
            assertEquals(expectedDoc.withoutId(), find().first().get()!!.withoutId())
            assertEquals(1, count().get())

            // Call findOneAndUpdate() again but get the new document
            sampleUpdate.remove("\$set")
            expectedDoc["num"] = 4
            val options = FindOneAndModifyOptions()
                    .returnNewDocument(true)
            findOneAndUpdate(Document("hello", "hellothere"), sampleUpdate, options)
                    .get()!!
                    .withoutId()
                    .let {
                        assertEquals(expectedDoc.withoutId(), it)
                    }
            assertEquals(1, count().get())

            // Test null behaviour again with a filter that should not match any documents
            assertNull(findOneAndUpdate(Document("hello", "zzzzz"), Document()).get())
            assertEquals(1, count().get())
        }
    }

    @Test
    fun findOneAndUpdate_upsert() {
        with(getCollectionInternal()) {
            val doc1 = Document("hello", "world1").apply { this["num"] = 1 }
            val doc2 = Document("hello", "world2").apply { this["num"] = 2 }
            val doc3 = Document("hello", "world3").apply { this["num"] = 3 }

            val filter = Document("hello", "hellothere")

            // Test the upsert option where it should not actually be invoked
            var options = FindOneAndModifyOptions()
                    .returnNewDocument(true)
                    .upsert(true)
            val update1 = Document("\$set", doc1)
            assertEquals(doc1,
                    findOneAndUpdate(filter, update1, options)
                            .get()!!
                            .withoutId())
            assertEquals(1, count().get())
            assertEquals(doc1.withoutId(),
                    find().first()
                            .get()!!
                            .withoutId())

            // Test the upsert option where the server should perform upsert and return new document
            val update2 = Document("\$set", doc2)
            assertEquals(doc2,
                    findOneAndUpdate(filter, update2, options)
                            .get()!!
                            .withoutId())
            assertEquals(2, count().get())

            // Test the upsert option where the server should perform upsert and return old document
            // The old document should be empty
            options = FindOneAndModifyOptions()
                    .upsert(true)
            val update = Document("\$set", doc3)
            assertNull(findOneAndUpdate(filter, update, options).get())
            assertEquals(3, count().get())
        }
    }

    // FIXME: projections and sorts aren't currently working due to a bug in Stitch: https://jira.mongodb.org/browse/REALMC-5787
    @Test
    @Ignore("Projections and sorts don't work")
    fun findOneAndUpdate_withProjectionAndSort() {
        with(getCollectionInternal()) {
            val sampleUpdate = Document("\$set", Document("hello", "hellothere")).apply {
                this["\$inc"] = Document("num", 1)
            }
            sampleUpdate.remove("\$set")        // FIXME
            val sampleProject = Document("hello", 1)
            sampleProject["_id"] = 0

            var options = FindOneAndModifyOptions()
                    .projection(sampleProject)
                    .sort(Document("num", 1))
            assertEquals(Document("hello", "world1"),
                    findOneAndUpdate(Document(), sampleUpdate, options)
                            .get()!!
                            .withoutId())
            assertEquals(3, count().get())

            options = FindOneAndModifyOptions()
                    .projection(sampleProject)
                    .sort(Document("num", -1))
            assertEquals(Document("hello", "world3"),
                    findOneAndUpdate(Document(), sampleUpdate, options)
                            .get()!!
                            .withoutId())
            assertEquals(3, count().get())
        }
    }

    @Test
    fun findOneAndUpdate_fails() {
        with(getCollectionInternal()) {
            assertFailsWithErrorCode(ErrorCode.MONGODB_ERROR) {
                findOneAndUpdate(Document(), Document("\$who", 1)).get()
            }.also { e ->
                assertTrue(e.errorMessage!!.contains("modifier", true))
            }

            assertFailsWithErrorCode(ErrorCode.MONGODB_ERROR) {
                findOneAndUpdate(Document(), Document("\$who", 1), FindOneAndModifyOptions().upsert(true)).get()
            }.also { e ->
                assertTrue(e.errorMessage!!.contains("modifier", true))
            }
        }
    }

    @Test
    fun findOneAndReplace_noUpdates() {
        with(getCollectionInternal()) {
            // Test null behaviour again with a filter that should not match any documents
            assertNull(findOneAndReplace(Document("hello", "zzzzz"), Document()).get())
            assertEquals(0, count().get())
            assertNull(findOneAndReplace(Document(), Document()).get())
            assertEquals(0, count().get())
        }
    }

    @Test
    fun findOneAndReplace_noUpsert() {
        with(getCollectionInternal()) {
            val sampleDoc = Document("hello", "world1").apply { this["num"] = 2 }

            // Insert a sample Document
            insertOne(sampleDoc).get()
            assertEquals(1, count().get())

            // Sample call to findOneAndReplace() where we get the previous document back
            var sampleUpdate = Document("hello", "world2").apply { this["num"] = 2 }
            assertEquals(sampleDoc.withoutId(),
                    findOneAndReplace(Document("hello", "world1"), sampleUpdate).get()!!.withoutId())
            assertEquals(1, count().get())

            // Make sure the update took place
            val expectedDoc = Document("hello", "world2").apply { this["num"] = 2 }
            assertEquals(expectedDoc.withoutId(), find().first().get()!!.withoutId())
            assertEquals(1, count().get())

            // Call findOneAndReplace() again but get the new document
            sampleUpdate = Document("hello", "world3").apply { this["num"] = 3 }
            val options = FindOneAndModifyOptions().returnNewDocument(true)
            assertEquals(sampleUpdate.withoutId(),
                    findOneAndReplace(Document(), sampleUpdate, options).get()!!.withoutId())
            assertEquals(1, count().get())

            // Test null behaviour again with a filter that should not match any documents
            assertNull(findOneAndReplace(Document("hello", "zzzzz"), Document()).get())
            assertEquals(1, count().get())
        }
    }

    @Test
    fun findOneAndReplace_upsert() {
        with(getCollectionInternal()) {
            val doc4 = Document("hello", "world4").apply { this["num"] = 4 }
            val doc5 = Document("hello", "world5").apply { this["num"] = 5 }
            val doc6 = Document("hello", "world6").apply { this["num"] = 6 }

            // Test the upsert option where it should not actually be invoked
            val sampleUpdate = Document("hello", "world4").apply { this["num"] = 4 }
            var options = FindOneAndModifyOptions()
                    .returnNewDocument(true)
                    .upsert(true)
            assertEquals(doc4.withoutId(),
                    findOneAndReplace(Document("hello", "world3"), doc4, options)
                            .get()!!
                            .withoutId())
            assertEquals(1, count().get())
            assertEquals(doc4.withoutId(), find().first().get()!!.withoutId())

            // Test the upsert option where the server should perform upsert and return new document
            options = FindOneAndModifyOptions().returnNewDocument(true).upsert(true)
            assertEquals(doc5.withoutId(), findOneAndReplace(Document("hello", "hellothere"), doc5, options).get()!!.withoutId())
            assertEquals(2, count().get())

            // Test the upsert option where the server should perform upsert and return old document
            // The old document should be empty
            options = FindOneAndModifyOptions().upsert(true)
            assertNull(findOneAndReplace(Document("hello", "hellothere"), doc6, options).get())
            assertEquals(3, count().get())
        }
    }

    // FIXME: projections and sorts aren't currently working due to a bug in Stitch: https://jira.mongodb.org/browse/REALMC-5787
    @Test
    @Ignore("Projections and sorts don't work")
    fun findOneAndReplace_withProjectionAndSort() {
        with(getCollectionInternal()) {
            val sampleProject = Document("hello", 1)
            sampleProject["_id"] = 0

            val sampleUpdate = Document("hello", "world0")
            sampleUpdate["num"] = 0

            var options = FindOneAndModifyOptions().projection(sampleProject).sort(Document("num", 1))
            val result = findOneAndReplace(Document(), sampleUpdate, options).get()
            assertEquals(Document("hello", "world4"), result!!.withoutId())
            assertEquals(3, count().get())

            options = FindOneAndModifyOptions()
                    .projection(sampleProject)
                    .sort(Document("num", -1))
            assertEquals(Document("hello", "world6"),
                    findOneAndReplace(Document(), sampleUpdate, options).get()!!.withoutId())
            assertEquals(3, count().get())
        }
    }

    @Test
    fun findOneAndReplace_fails() {
        with(getCollectionInternal()) {
            assertFailsWithErrorCode(ErrorCode.INVALID_PARAMETER) {
                findOneAndReplace(Document(), Document("\$who", 1)).get()
            }

            assertFailsWithErrorCode(ErrorCode.INVALID_PARAMETER) {
                findOneAndReplace(Document(), Document("\$who", 1), FindOneAndModifyOptions().upsert(true)).get()
            }
        }
    }

    @Test
    fun findOneAndDelete() {
        with(getCollectionInternal()) {
            val sampleDoc = Document("hello", "world1").apply { this["num"] = 1 }

            // Collection should start out empty
            // This also tests the null return format
            assertNull(findOneAndDelete(Document()).get())

            // Insert a sample Document
            insertOne(sampleDoc).get()
            assertEquals(1, count().get())

            // Sample call to findOneAndDelete() where we delete the only doc in the collection
            assertEquals(sampleDoc.withoutId(),
                    findOneAndDelete(Document()).get()!!.withoutId())

            // There should be no documents in the collection now
            assertEquals(0, count().get())

            // Insert a sample Document
            insertOne(sampleDoc).get()
            assertEquals(1, count().get())

            // Call findOneAndDelete() again but this time with a filter
            assertEquals(sampleDoc.withoutId(),
                    findOneAndDelete(Document("hello", "world1")).get()!!.withoutId())

            // There should be no documents in the collection now
            assertEquals(0, count().get())

            // Insert a sample Document
            insertOne(sampleDoc).get()
            assertEquals(1, count().get())

            // Test null behaviour again with a filter that should not match any documents
            assertNull(findOneAndDelete(Document("hello", "zzzzz")).get())
            assertEquals(1, count().get())

            val doc2 = Document("hello", "world2").apply { this["num"] = 2 }
            val doc3 = Document("hello", "world3").apply { this["num"] = 3 }

            // Insert new documents
            insertMany(listOf(doc2, doc3)).get()
            assertEquals(3, count().get())
        }
    }

    // FIXME: projections and sorts aren't currently working due to a bug in Stitch: https://jira.mongodb.org/browse/REALMC-5787
    @Test
    @Ignore("find_one_and_delete function is wrongly implemented in OS and projections and sorts don't work")
    fun findOneAndDelete_withProjectionAndSort() {
        with(getCollectionInternal()) {
            val doc2 = Document("hello", "world2").apply { this["num"] = 2 }
            val doc3 = Document("hello", "world3").apply { this["num"] = 3 }

            insertMany(listOf(doc2, doc3)).get()

            // Return "hello", hide "_id"
            val sampleProject = Document("hello", 1).apply { this["_id"] = 0 }

            var options = FindOneAndModifyOptions()
                    .projection(sampleProject)
                    .sort(Document("num", -1))
            assertEquals(Document("hello", "world3"),
                    findOneAndDelete(Document(), options).get()!!.withoutId())
            assertEquals(2, count().get())

            options = FindOneAndModifyOptions()
                    .projection(sampleProject)
                    .sort(Document("num", 1))
            assertEquals(Document("hello", "world1"),
                    findOneAndDelete(Document(), options).get()!!.withoutId())
            assertEquals(1, count().get())
        }
    }

    @Test
    fun withDocument() {
        // aAd default codecs as they too are needed for proper collection initialization
        val expandedCodecRegistry = CodecRegistries
                .fromRegistries(AppConfiguration.DEFAULT_BSON_CODEC_REGISTRY,
                        CodecRegistries.fromCodecs(CustomType.Codec()))

        val expected = CustomType(ObjectId(), 42)

        // Get default collection
        with(getCollectionInternal()) {
            // Now specify custom class
            var coll = withDocumentClass(CustomType::class.java)
            assertEquals(CustomType::class.java, coll.documentClass)

            assertFailsWith(AppException::class) {
                coll.insertOne(expected).get()
            }

            val defaultCodecRegistry = AppConfiguration.DEFAULT_BSON_CODEC_REGISTRY
            assertEquals(defaultCodecRegistry, coll.codecRegistry)

            // Use expanded registry
            coll = coll.withCodecRegistry(expandedCodecRegistry)
            assertEquals(expected.id,
                    coll.insertOne(expected).get()!!.insertedId.asObjectId().value)
            assertEquals(expected, coll.find().first().get())
        }

        val expected2 = CustomType(null, 42)

        // Now get new collection for CustomType
        with(getCollectionInternal(CustomType::class.java)
                .withCodecRegistry(expandedCodecRegistry)) {
            insertOne(expected2).get()!!
            val actual: CustomType = find().first().get()!!
            assertEquals(expected2.intValue, actual.intValue)
        }

        with(getCollectionInternal(CustomType::class.java)
                .withCodecRegistry(expandedCodecRegistry)) {
            val actual: CustomType = find(Document(), CustomType::class.java)
                    .first()
                    .get()!!
            assertEquals(expected2.intValue, actual.intValue)
            assertNotNull(expected.id)

            val iter = aggregate(listOf(Document("\$match", Document())), CustomType::class.java)
            assertTrue(iter.iterator().get()!!.hasNext())
            assertEquals(expected, iter.iterator().get()!!.next())
        }
    }

    private fun getCollectionInternal(): MongoCollection<Document> {
        return client.getDatabase(DATABASE_NAME).let {
            assertEquals(it.name, DATABASE_NAME)
            it.getCollection(COLLECTION_NAME).also { collection ->
                assertEquals(MongoNamespace(DATABASE_NAME, COLLECTION_NAME), collection.namespace)
            }
        }
    }

    private fun <ResultT> getCollectionInternal(
            resultClass: Class<ResultT>
    ): MongoCollection<ResultT> {
        return client.getDatabase(DATABASE_NAME).let {
            assertEquals(it.name, DATABASE_NAME)
            it.getCollection(COLLECTION_NAME, resultClass).also { collection ->
                assertEquals(MongoNamespace(DATABASE_NAME, COLLECTION_NAME), collection.namespace)
            }
        }
    }

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
