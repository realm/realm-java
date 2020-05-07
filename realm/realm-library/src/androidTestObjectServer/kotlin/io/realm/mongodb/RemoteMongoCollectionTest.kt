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
import com.google.android.gms.tasks.Tasks
import io.realm.*
import io.realm.admin.ServerAdmin
import io.realm.entities.Dog
import io.realm.log.LogLevel
import io.realm.log.RealmLog
import io.realm.mongodb.remote.RemoteCountOptions
import io.realm.mongodb.remote.RemoteInsertOneResult
import io.realm.util.blockingGetResult
import org.bson.Document
import org.junit.*
import org.junit.runner.RunWith
import java.util.concurrent.ExecutionException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.fail

@RunWith(AndroidJUnit4::class)
//@Ignore("Collections not ready to test yet")
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
        database = client.getDatabase(DATABASE_NAME, RealmAppConfiguration.DEFAULT_CODEC_REGISTRY)
    }

    @After
    fun tearDown() {
        if (this::app.isInitialized) {
            app.close()
        }
    }

    @Test
    fun count() {
        with(getCollectionInternal(COLLECTION_NAME)) {
            assertEquals(0, this.count().blockingGetResult())

            val rawDocument = Document(KEY_1, VALUE_1)
            val document1 = Document(rawDocument)
            val document2 = Document(rawDocument)

            this.insertOne(document1).blockingGetResult()
            assertEquals(1, this.count().blockingGetResult())
            this.insertOne(document2).blockingGetResult()
            assertEquals(2, this.count().blockingGetResult())

            assertEquals(2, this.count(rawDocument).blockingGetResult())
            assertEquals(0, this.count(Document("foo", "bar")).blockingGetResult())
            assertEquals(1, this.count(rawDocument, RemoteCountOptions().limit(1)).blockingGetResult())

//            assertFailsWith(ExecutionException::class) {
//                this.count(Document("\$who", 1)).blockingGetResult()
//                fail("Should not reach this!")
//            }

            try {
                this.count(Document("\$who", 1)).blockingGetResult()
                Assert.fail()
            } catch (ex: ExecutionException) {
                val kajshdjk = 0
//                Assert.assertTrue(ex.cause is StitchServiceException)
//                val svcEx = ex.cause as StitchServiceException
//                assertEquals(StitchServiceErrorCode.MONGODB_ERROR, svcEx.errorCode)
            }
        }
    }

//    @Test
//    fun insertOne() {
//        with(getCollectionInternal(COLLECTION_NAME)) {
//            assertEquals(0, this.count().blockingGetResult())
//            insertOneInternal(COLLECTION_NAME, Document(KEY_1, VALUE_1)).let { insertOneResult ->
//                assertNotNull(insertOneResult)
//                insertOneResult.insertedId
//            }
//        }
//
//        val insertedOne = insertOneInternal(COLLECTION_NAME, Document(KEY_1, VALUE_1))
//        assertNotNull(insertedOne)
//        val docsAfter = getCollectionInternal(COLLECTION_NAME).count().blockingGetResult()
//        val kasjhd = 0
//    }

//    @Test
//    @Ignore("Collections not ready to test yet")
//    fun countWithFilter() {
//        // FIXME:
//    }
//
//    @Test
//    @Ignore("Collections not ready to test yet")
//    fun countWithFilterAndLimit() {
//        // FIXME:
//    }

    // FIXME: more to come

//    private fun insertOneInternal(collectionName: String, document: Document): RemoteInsertOneResult? {
//        return getCollectionInternal(collectionName)
//                .insertOne(document)
//                .blockingGetResult()
//    }

    private fun getCollectionInternal(collectionName: String, javaClass: Class<Document>? = null): RemoteMongoCollection<Document> {
        return when (javaClass) {
            null -> database.getCollection(collectionName)
            else -> database.getCollection(collectionName, javaClass)
        }
    }

    private companion object {
        const val SERVICE_NAME = "BackingDB"    // it comes from the test server's BackingDB/config.json
        const val DATABASE_NAME = "test_data"   // same as above

        const val COLLECTION_NAME = "COLLECTION_NAME"
        const val KEY_1 = "KEY_1"
        const val VALUE_1 = "VALUE_1"
    }
}
