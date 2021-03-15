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
package io.realm.mongodb.sync

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.realm.*
import io.realm.entities.*
import io.realm.entities.embedded.*
import io.realm.kotlin.createEmbeddedObject
import io.realm.kotlin.createObject
import io.realm.kotlin.syncSession
import io.realm.kotlin.where
import io.realm.log.LogLevel
import io.realm.log.RealmLog
import io.realm.mongodb.App
import io.realm.mongodb.Credentials
import io.realm.mongodb.SyncTestUtils.Companion.createTestUser
import io.realm.mongodb.User
import io.realm.mongodb.close
import org.bson.BsonNull
import org.bson.BsonString
import org.bson.types.Decimal128
import org.bson.types.ObjectId
import org.junit.*
import org.junit.Assert.assertNotEquals
import org.junit.runner.RunWith
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.fail

/**
 * Testing sync specific methods on [Realm].
 */
@RunWith(AndroidJUnit4::class)
class SyncedRealmTests {

    @get:Rule
    val configFactory = TestSyncConfigurationFactory()

    private lateinit var app: App
    private lateinit var partitionValue: String

    @Before
    fun setUp() {
        Realm.init(InstrumentationRegistry.getInstrumentation().targetContext)
        RealmLog.setLevel(LogLevel.TRACE)
        app = TestApp()
        partitionValue = UUID.randomUUID().toString()
    }

    @After
    fun tearDown() {
        if (this::app.isInitialized) {
            app.close()
        }
        RealmLog.setLevel(LogLevel.WARN)
    }

    // Smoke test for Sync. Waiting for working Sync support.
    @Test
    fun connectWithInitialSchema() {
        val user: User = createNewUser()
        val config = createDefaultConfig(user)
        Realm.getInstance(config).use { realm ->
            with(realm.syncSession) {
                uploadAllLocalChanges()
                downloadAllServerChanges()
            }
            assertTrue(realm.isEmpty)
        }
    }

    // Smoke test for Sync
    @Test
    fun roundTripObjectsNotInServerSchemaObject() {
        // User 1 creates an object an uploads it to MongoDB Realm
        val user1: User = createNewUser()
        val config1: SyncConfiguration = createCustomConfig(user1, partitionValue)
        Realm.getInstance(config1).use { realm ->
            realm.executeTransaction {
                for (i in 1..10) {
                    it.insert(SyncColor())
                }
            }
            realm.syncSession.uploadAllLocalChanges()
            assertEquals(10, realm.where<SyncColor>().count())
        }

        // User 2 logs and using the same partition key should see the object
        val user2: User = createNewUser()
        val config2 = createCustomConfig(user2, partitionValue)
        Realm.getInstance(config2).use { realm ->
            realm.syncSession.downloadAllServerChanges()
            realm.refresh()
            assertEquals(10, realm.where<SyncColor>().count())
        }
    }

    // Smoke test for sync
    // Insert different types with no links between them
    @Test
    fun roundTripSimpleObjectsInServerSchema() {
        // User 1 creates an object an uploads it to MongoDB Realm
        val user1: User = createNewUser()
        val config1: SyncConfiguration = createDefaultConfig(user1, partitionValue)
        Realm.getInstance(config1).use { realm ->
            realm.executeTransaction {
                val person = SyncPerson()
                person.firstName = "Jane"
                person.lastName = "Doe"
                person.age = 42
                realm.insert(person);
                for (i in 0..9) {
                    val dog = SyncDog()
                    dog.name = "Fido $i"
                    it.insert(dog)
                }
            }
            realm.syncSession.uploadAllLocalChanges()
            assertEquals(10, realm.where<SyncDog>().count())
            assertEquals(1, realm.where<SyncPerson>().count())
        }

        // User 2 logs and using the same partition key should see the object
        val user2: User = createNewUser()
        val config2 = createDefaultConfig(user2, partitionValue)
        Realm.getInstance(config2).use { realm ->
            realm.syncSession.downloadAllServerChanges()
            realm.refresh()
            assertEquals(10, realm.where<SyncDog>().count())
            assertEquals(1, realm.where<SyncPerson>().count())
        }
    }

    // Smoke test for sync
    // Insert objects with links between them
    @Test
    fun roundTripObjectsWithLists() {
        // User 1 creates an object an uploads it to MongoDB Realm
        val user1: User = createNewUser()
        val config1: SyncConfiguration = createDefaultConfig(user1, partitionValue)
        Realm.deleteRealm(config1)
        Realm.getInstance(config1).use { realm ->
            realm.executeTransaction {
                val person = SyncPerson()
                person.firstName = "Jane"
                person.lastName = "Doe"
                person.age = 42
                for (i in 0..9) {
                    val dog = SyncDog()
                    dog.name = "Fido $i"
                    person.dogs.add(dog)
                }
                realm.insert(person)
            }
            realm.syncSession.uploadAllLocalChanges()
            assertEquals(10, realm.where<SyncDog>().count())
            assertEquals(1, realm.where<SyncPerson>().count())
        }

        // User 2 logs and using the same partition key should see the object
        val user2: User = createNewUser()
        val config2 = createDefaultConfig(user2, partitionValue)
        Realm.deleteRealm(config2)
        Realm.getInstance(config2).use { realm ->
            realm.syncSession.downloadAllServerChanges()
            realm.refresh()
            assertEquals(10, realm.where<SyncDog>().count())
            assertEquals(1, realm.where<SyncPerson>().count())
        }
    }

    @Test
    fun session() {
        val user: User = app.login(Credentials.anonymous())
        Realm.getInstance(createDefaultConfig(user)).use { realm ->
            assertNotNull(realm.syncSession)
            assertEquals(SyncSession.State.ACTIVE, realm.syncSession.state)
            assertEquals(user, realm.syncSession.user)
        }
    }

    @Test
    fun nullPartition() {
        val config = configFactory.createSyncConfigurationBuilder(createNewUser(), BsonNull())
                .modules(DefaultSyncSchema())
                .build()
        assertTrue(config.path.endsWith("null.realm"))
        Realm.getInstance(config).use { realm ->
            realm.syncSession.uploadAllLocalChanges() // Ensures that we can actually connect
        }
    }

    @Test
    @Ignore("FIXME Flaky, seems like Realm.compactRealm(config) sometimes returns false")
    fun compactRealm_populatedRealm() {
        val config = configFactory.createSyncConfigurationBuilder(createNewUser()).build()
        Realm.getInstance(config).use { realm ->
            realm.executeTransaction { r: Realm ->
                for (i in 0..9) {
                    r.insert(AllJavaTypes(i.toLong()))
                }
            }
        }
        assertTrue(Realm.compactRealm(config))

        Realm.getInstance(config).use { realm ->
            assertEquals(10, realm.where(AllJavaTypes::class.java).count())
        }
    }

    @Test
    fun compactOnLaunch_shouldCompact() {
        val user = createTestUser(app)

        // Fill Realm with data and record size
        val config1 = configFactory.createSyncConfigurationBuilder(user)
                .testSchema(SyncByteArray::class.java)
                .build()

        var originalSize: Long? = null
        Realm.getInstance(config1).use { realm ->
            val oneMBData = ByteArray(1024 * 1024)
            realm.executeTransaction {
                for (i in 0..9) {
                    realm.createObject(SyncByteArray::class.java, ObjectId()).columnBinary = oneMBData
                }
            }
            originalSize = File(realm.path).length()
        }

        // Open Realm with CompactOnLaunch
        val config2 = configFactory.createSyncConfigurationBuilder(user)
                .compactOnLaunch { totalBytes, usedBytes -> true }
                .testSchema(SyncByteArray::class.java)
                .build()
        Realm.getInstance(config2).use { realm ->
            val compactedSize = File(realm.path).length()
            assertTrue(originalSize!! > compactedSize)
        }
    }

    @Test
    fun embeddedObject_roundTrip() {
        val user1: User = createNewUser()
        val config1: SyncConfiguration = createDefaultConfig(user1, partitionValue)
        val primaryKeyValue = UUID.randomUUID().toString()
        Realm.getInstance(config1).use { realm ->
            assertTrue(realm.isEmpty)

            realm.executeTransaction {
                realm.createObject<EmbeddedSimpleParent>(primaryKeyValue).let { parent ->
                    realm.createEmbeddedObject<EmbeddedSimpleChild>(parent, "child")
                }
            }
            realm.syncSession.uploadAllLocalChanges()

            assertEquals(1, realm.where<EmbeddedSimpleParent>().count())
            assertEquals(1, realm.where<EmbeddedSimpleChild>().count())
        }

        val user2: User = createNewUser()
        val config2: SyncConfiguration = createDefaultConfig(user2, partitionValue)
        Realm.getInstance(config2).use { realm ->
            realm.syncSession.downloadAllServerChanges(5, TimeUnit.SECONDS).let {
                if (!it) fail()
            }
            realm.refresh()

            val childResults = realm.where<EmbeddedSimpleChild>()
            assertEquals(1, childResults.count())
            val parentResults = realm.where<EmbeddedSimpleParent>()
            assertEquals(1, parentResults.count())
            val parent = parentResults.findFirst()!!
            assertEquals(primaryKeyValue, parent._id)
            assertEquals(parent._id, parent.child!!.parent._id)
        }
    }

    // FIXME: remove ignore when sync issue fixed
    @Test
    @Ignore("ignored until https://jira.mongodb.org/browse/REALMC-6541 is fixed")
    fun embeddedObject_copyUnmanaged_roundTrip() {
        val user1: User = createNewUser()
        val config1: SyncConfiguration = createDefaultConfig(user1, partitionValue)
        val primaryKeyValue = UUID.randomUUID().toString()

        Realm.getInstance(config1).use { realm ->
            assertTrue(realm.isEmpty)

            realm.executeTransaction {
                val parent = EmbeddedSimpleParent(primaryKeyValue)

//                parent.child = EmbeddedSimpleChild()
                val managedParent = it.copyToRealmOrUpdate(parent)
                // FIXME: instantiating the child in managedParent yields this from sync:
                //  "MongoDB error: Updating the path 'child.childID' would create a conflict at 'child'"
                managedParent.child = EmbeddedSimpleChild() // Will copy the object to Realm
            }
            realm.syncSession.uploadAllLocalChanges()

            assertEquals(1, realm.where<EmbeddedSimpleParent>().count())
            assertEquals(1, realm.where<EmbeddedSimpleChild>().count())
        }

        val user2: User = createNewUser()
        val config2: SyncConfiguration = createDefaultConfig(user2, partitionValue)
        Realm.getInstance(config2).use { realm ->
            realm.syncSession.downloadAllServerChanges(5, TimeUnit.SECONDS).let {
                if (!it) fail()
            }
            realm.refresh()

            val childResults = realm.where<EmbeddedSimpleChild>()
            assertEquals(1, childResults.count())
            val parentResults = realm.where<EmbeddedSimpleParent>()
            assertEquals(1, parentResults.count())
            val parent = parentResults.findFirst()!!
            assertEquals(primaryKeyValue, parent._id)
            assertEquals(parent._id, parent.child!!.parent._id)
        }
    }

    @Test
    fun embeddedObject_realmList_roundTrip() {
        val user1: User = createNewUser()
        val config1: SyncConfiguration = createDefaultConfig(user1, partitionValue)
        val primaryKeyValue = UUID.randomUUID().toString()
        Realm.getInstance(config1).use { realm ->
            realm.executeTransaction {
                realm.createObject(EmbeddedSimpleListParent::class.java, primaryKeyValue).let { parent ->
                    realm.createEmbeddedObject(EmbeddedSimpleChild::class.java, parent, "children")
                    realm.createEmbeddedObject(EmbeddedSimpleChild::class.java, parent, "children")
                }
            }
            realm.syncSession.uploadAllLocalChanges()

            assertEquals(1, realm.where<EmbeddedSimpleListParent>().count())
            assertEquals(2, realm.where<EmbeddedSimpleChild>().count())
        }

        val user2: User = createNewUser()
        val config2: SyncConfiguration = createDefaultConfig(user2, partitionValue)
        Realm.getInstance(config2).use { realm ->
            assertEquals(0, realm.where<EmbeddedSimpleListParent>().count())
            assertEquals(0, realm.where<EmbeddedSimpleChild>().count())

            realm.syncSession.downloadAllServerChanges(5, TimeUnit.SECONDS).let {
                if (!it) fail()
            }
            realm.refresh()

            val childResults = realm.where<EmbeddedSimpleChild>()
            assertEquals(2, childResults.count())
            val parentResults = realm.where<EmbeddedSimpleListParent>()
            assertEquals(1, parentResults.count())
            val parentFromResults = parentResults.findFirst()!!
            assertEquals(primaryKeyValue, parentFromResults._id)

            parentFromResults.children.also { childrenInParent ->
                val childrenFromResults = childResults.findAll()
                childrenInParent.forEach { childInParent ->
                    assertTrue(childrenFromResults.contains(childInParent))
                }
            }
        }
    }

    @Test
    fun embeddedObject_realmList_copyUnmanaged_roundTrip() {
        val user1: User = createNewUser()
        val config1: SyncConfiguration = createDefaultConfig(user1, partitionValue)
        val primaryKeyValue = UUID.randomUUID().toString()
        Realm.getInstance(config1).use { realm ->
            realm.executeTransaction {
                val parent = EmbeddedSimpleListParent(primaryKeyValue)
                parent.children = RealmList(EmbeddedSimpleChild("child1"), EmbeddedSimpleChild("child2"))
                realm.insert(parent)
            }
            realm.syncSession.uploadAllLocalChanges()

            assertEquals(1, realm.where<EmbeddedSimpleListParent>().count())
            assertEquals(2, realm.where<EmbeddedSimpleChild>().count())
        }

        val user2: User = createNewUser()
        val config2: SyncConfiguration = createDefaultConfig(user2, partitionValue)
        Realm.getInstance(config2).use { realm ->
            assertEquals(0, realm.where<EmbeddedSimpleListParent>().count())
            assertEquals(0, realm.where<EmbeddedSimpleChild>().count())

            realm.syncSession.downloadAllServerChanges(1, TimeUnit.SECONDS).let {
                if (!it) fail()
            }
            realm.refresh()

            val childResults = realm.where<EmbeddedSimpleChild>()
            assertEquals(2, childResults.count())
            val parentResults = realm.where<EmbeddedSimpleListParent>()
            assertEquals(1, parentResults.count())
            val parentFromResults = parentResults.findFirst()!!
            assertEquals(primaryKeyValue, parentFromResults._id)
            assertEquals("child1", childResults.findAll()[0]!!.childId)
            assertEquals("child2", childResults.findAll()[1]!!.childId)
        }
    }

    // FIXME: remember to add tree structure classes to DefaultSyncSchema.kt
    @Test
    @Ignore("Enable when https://jira.mongodb.org/projects/HELP/queues/issue/HELP-17759 is fixed")
    fun copyToRealm_treeSchema() {
        val user1: User = createNewUser()
        val config1: SyncConfiguration = createDefaultConfig(user1, partitionValue)
        val primaryKeyValue = UUID.randomUUID().toString()

        Realm.getInstance(config1).use { realm ->
            realm.executeTransaction {
                val parent = EmbeddedTreeParent("parent1")

                val node1 = EmbeddedTreeNode("node1")
                node1.leafNode = EmbeddedTreeLeaf("leaf1")
                parent.middleNode = node1
                val node2 = EmbeddedTreeNode("node2")
                node2.leafNodeList.add(EmbeddedTreeLeaf("leaf2"))
                node2.leafNodeList.add(EmbeddedTreeLeaf("leaf3"))
                parent.middleNodeList.add(node2)

                it.copyToRealm(parent)
            }
            realm.syncSession.uploadAllLocalChanges()
        }

        val user2: User = createNewUser()
        val config2: SyncConfiguration = createDefaultConfig(user2, partitionValue)
        Realm.getInstance(config2).use { realm ->
            assertEquals(0, realm.where<EmbeddedSimpleListParent>().count())
            assertEquals(0, realm.where<EmbeddedSimpleChild>().count())

            realm.syncSession.downloadAllServerChanges(1, TimeUnit.SECONDS).let {
                if (!it) fail()
            }
            realm.refresh()

            Assert.assertEquals(1, realm.where<EmbeddedTreeParent>().count())
            Assert.assertEquals("parent1", realm.where<EmbeddedTreeParent>().findFirst()!!._id)

            Assert.assertEquals(2, realm.where<EmbeddedTreeNode>().count())
            val nodeResults = realm.where<EmbeddedTreeNode>().findAll()
            Assert.assertTrue(nodeResults.any { it.treeNodeId == "node1" })
            Assert.assertTrue(nodeResults.any { it.treeNodeId == "node2" })

            Assert.assertEquals(3, realm.where<EmbeddedTreeLeaf>().count())
            val leafResults = realm.where<EmbeddedTreeLeaf>().findAll()
            Assert.assertTrue(leafResults.any { it.treeLeafId == "leaf1" })
            Assert.assertTrue(leafResults.any { it.treeLeafId == "leaf2" })
            Assert.assertTrue(leafResults.any { it.treeLeafId == "leaf3" })
        }
    }

    // Check that we can create multiple apps that synchronize with each other
    @Test
    fun multipleAppsCanSync() {
        val app2 = TestApp(appName = TEST_APP_2)
        var realm1: Realm? = null
        var realm2: Realm? = null
        try {
            // Login users on both Realms
            val app1User = app.login(Credentials.anonymous())
            val app2User = app2.login(Credentials.anonymous())
            assertNotEquals(app1User, app2User)

            // Create one Realm against each app
            val config1 = configFactory.createSyncConfigurationBuilder(app1User, BsonString("foo"))
                    .modules(DefaultSyncSchema())
                    .build()
            val config2 = configFactory.createSyncConfigurationBuilder(app2User, BsonString("foo"))
                    .modules(DefaultSyncSchema())
                    .build()

            // Make sure we can synchronize changes
            realm1 = Realm.getInstance(config1)
            realm2 = Realm.getInstance(config2)
            realm1.syncSession.downloadAllServerChanges()
            realm2.syncSession.downloadAllServerChanges()
            Assert.assertTrue(realm1.isEmpty)
            Assert.assertTrue(realm2.isEmpty)
        } finally {
            realm1?.close()
            realm2?.close()
            app2.close()
        }
    }

    @Test
    fun allTypes_roundTrip() {
        val user1: User = createNewUser()
        val config1: SyncConfiguration = createDefaultConfig(user1, partitionValue)

        val primaryKeyValue = ObjectId()
        val expectedRealmInteger = 100.toLong()
        val expectedString = "hello world"
        val expectedLong = 10.toLong()
        val expectedDouble = 10.0
        val expectedBoolean = true
        val expectedDate = Date()
        val expectedBinary = byteArrayOf(0, 1, 0)
        val expectedDecimal128 = Decimal128(10)
        val expectedObjectId = ObjectId()
        val expectedUUID = UUID.randomUUID()
        var expectedRealmObject = SyncDog().apply {
            id = expectedObjectId
        }
        val expectedRealmList = RealmList<SyncDog>()
        val expectedStringList = RealmList<String>("hello world 1", "hello world 2")
        val expectedBinaryList = RealmList<ByteArray>(expectedBinary)
        val expectedBooleanList = RealmList<Boolean>(true, false, false, true)
        val expectedLongList = RealmList<Long>(0, 1, 2, 5, 7)
        val expectedDoubleList = RealmList<Double>(0.0, 2.toDouble(), 10.5)
        val expectedDateList = RealmList<Date>(Date(100), Date(10), Date(200))
        val expectedDecimal128List = RealmList<Decimal128>(Decimal128(10), Decimal128(100), Decimal128(20))
        val expectedObjectIdList = RealmList<ObjectId>(ObjectId(Date(1000)), ObjectId(Date(100)), ObjectId(Date(2000)))
        val expectedUUIDList = RealmList<UUID>(UUID.randomUUID())

        Realm.getInstance(config1).use { realm ->
            realm.executeTransaction {
                expectedRealmObject = realm.copyToRealmOrUpdate(expectedRealmObject)
                expectedRealmList.add(expectedRealmObject)

                realm.createObject(SyncAllTypes::class.java, primaryKeyValue).apply {
                    columnString = expectedString
                    columnLong = expectedLong
                    columnDouble = expectedDouble
                    isColumnBoolean = expectedBoolean
                    columnDate = expectedDate
                    columnBinary = expectedBinary
                    columnDecimal128 = expectedDecimal128
                    columnObjectId = expectedObjectId
                    columnUUID = expectedUUID
                    columnRealmInteger.set(expectedRealmInteger)
                    columnRealmObject = expectedRealmObject
                    columnRealmList = expectedRealmList
                    columnStringList = expectedStringList
                    columnBinaryList = expectedBinaryList
                    columnBooleanList = expectedBooleanList
                    columnLongList = expectedLongList
                    columnDoubleList = expectedDoubleList
                    columnDateList = expectedDateList
                    columnDecimal128List = expectedDecimal128List
                    columnObjectIdList = expectedObjectIdList
                    columnUUIDList = expectedUUIDList
                }
            }
            realm.syncSession.uploadAllLocalChanges()
            assertEquals(1, realm.where<SyncAllTypes>().count())
        }

        val user2: User = createNewUser()
        val config2: SyncConfiguration = createDefaultConfig(user2, partitionValue)
        Realm.getInstance(config2).use { realm ->
            assertEquals(0, realm.where<SyncAllTypes>().count())

            realm.syncSession.downloadAllServerChanges(TestHelper.STANDARD_WAIT_SECS.toLong(), TimeUnit.SECONDS).let {
                if (!it) fail()
            }
            realm.refresh()

            assertEquals(1, realm.where<SyncAllTypes>().count())

            realm.where<SyncAllTypes>().findFirst()!!.let {
                assertEquals(expectedString, it.columnString)
                assertEquals(expectedLong, it.columnLong)
                assertEquals(expectedDouble, it.columnDouble)
                assertEquals(expectedBoolean, it.isColumnBoolean)
                assertEquals(expectedDate, it.columnDate)
                assertTrue(expectedBinary.contentEquals(it.columnBinary))
                assertEquals(expectedDecimal128, it.columnDecimal128)
                assertEquals(expectedObjectId, it.columnObjectId)
                assertEquals(expectedUUID, it.columnUUID)
                assertEquals(expectedRealmInteger, it.columnRealmInteger.get())
                assertEquals(expectedObjectId, it.columnRealmObject!!.id)
                assertEquals(expectedObjectId, it.columnRealmList.first()!!.id)
                assertEquals(expectedStringList, it.columnStringList)
                expectedBinaryList.forEachIndexed { index, bytes ->
                    Arrays.equals(bytes, it.columnBinaryList[index])
                }
                assertEquals(expectedBooleanList, it.columnBooleanList)
                assertEquals(expectedLongList, it.columnLongList)
                assertEquals(expectedDoubleList, it.columnDoubleList)
                assertEquals(expectedDateList, it.columnDateList)
                assertEquals(expectedDecimal128List, it.columnDecimal128List)
                assertEquals(expectedObjectIdList, it.columnObjectIdList)
                assertEquals(expectedUUIDList, it.columnUUIDList)
            }
        }
    }

    @Test
    // FIXME Missing test, maybe fitting better in SyncSessionTest.kt...when migrated
    @Ignore("Not implemented yet")
    fun refreshConnections() {}

    private fun createDefaultConfig(user: User, partitionValue: String = defaultPartitionValue): SyncConfiguration {
        return SyncConfiguration.Builder(user, partitionValue)
                .modules(DefaultSyncSchema())
                .build()
    }

    private fun createCustomConfig(user: User, partitionValue: String = defaultPartitionValue): SyncConfiguration {
        return SyncConfiguration.Builder(user, partitionValue)
                .schema(SyncColor::class.java)
                .build()
    }

    private fun createNewUser(): User {
        val email = TestHelper.getRandomEmail()
        val password = "123456"
        app.emailPassword.registerUser(email, password)
        return app.login(Credentials.emailPassword(email, password))
    }

}
