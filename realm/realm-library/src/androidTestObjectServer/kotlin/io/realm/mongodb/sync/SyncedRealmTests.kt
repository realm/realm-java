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
        val expectedRealmAnyValues = arrayListOf(
                RealmAny.valueOf(1.toLong()),
                RealmAny.valueOf(false),
                RealmAny.valueOf(10.5.toFloat()),
                RealmAny.valueOf(10.5.toDouble()),
                RealmAny.valueOf("hello world 2"),
                RealmAny.valueOf(Date(105)),
                RealmAny.valueOf(Decimal128(102)),
                RealmAny.valueOf(ObjectId()),
                RealmAny.valueOf(UUID.randomUUID())
        )

        val primaryKeyValue = ObjectId()
        val expectedRealmInteger = 100.toLong()
        val expectedString = "hello world"
        val expectedLong = 10.toLong()
        val expectedDouble = 10.0
        val expectedFloat = 10.0.toFloat()
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
        val expectedFloatList = RealmList<Float>(0.0.toFloat(), 2.toFloat(), 10.5.toFloat())
        val expectedDateList = RealmList<Date>(Date(100), Date(10), Date(200))
        val expectedDecimal128List = RealmList<Decimal128>(Decimal128(10), Decimal128(100), Decimal128(20))
        val expectedObjectIdList = RealmList<ObjectId>(ObjectId(Date(1000)), ObjectId(Date(100)), ObjectId(Date(2000)))
        val expectedUUIDList = RealmList<UUID>(UUID.randomUUID())
        val expectedRealmAnyList = RealmList<RealmAny>()
        expectedRealmAnyList.addAll(expectedRealmAnyValues)

        val expectedRealmDict = RealmDictionary<SyncDog>()
        val expectedStringDict = RealmDictionary<String>().init(listOf("key" to expectedString))
        val expectedBinaryDict = RealmDictionary<ByteArray>().init(listOf("key" to expectedBinary))
        val expectedBooleanDict = RealmDictionary<Boolean>().init(listOf("key" to expectedBoolean))
        val expectedLongDict = RealmDictionary<Long>().init(listOf("key" to expectedLong))
        val expectedDoubleDict = RealmDictionary<Double>().init(listOf("key" to expectedDouble))
        val expectedFloatDict = RealmDictionary<Float>().init(listOf("key" to expectedFloat))
        val expectedDateDict = RealmDictionary<Date>().init(listOf("key" to expectedDate))
        val expectedDecimal128Dict = RealmDictionary<Decimal128>().init(listOf("key" to expectedDecimal128))
        val expectedObjectIdDict = RealmDictionary<ObjectId>().init(listOf("key" to expectedObjectId))
        val expectedUUIDDict = RealmDictionary<UUID>().init(listOf("key" to expectedUUID))
        val expectedRealmAnyDict = RealmDictionary<RealmAny>()

        val expectedRealmSet = RealmSet<SyncDog>()
        val expectedStringSet = RealmSet<String>().init(listOf(expectedString))
        val expectedBinarySet = RealmSet<ByteArray>().init(listOf(expectedBinary))
        val expectedBooleanSet = RealmSet<Boolean>().init(listOf(expectedBoolean))
        val expectedLongSet = RealmSet<Long>().init(listOf(expectedLong))
        val expectedDoubleSet = RealmSet<Double>().init(listOf(expectedDouble))
        val expectedFloatSet = RealmSet<Float>().init(listOf(expectedFloat))
        val expectedDateSet = RealmSet<Date>().init(listOf(expectedDate))
        val expectedDecimal128Set = RealmSet<Decimal128>().init(listOf(expectedDecimal128))
        val expectedObjectIdSet = RealmSet<ObjectId>().init(listOf(expectedObjectId))
        val expectedUUIDSet = RealmSet<UUID>().init(listOf(expectedUUID))
        val expectedRealmAnySet = RealmSet<RealmAny>()

        val user1: User = createNewUser()
        val config1: SyncConfiguration = createDefaultConfig(user1, partitionValue)

        val user2: User = createNewUser()
        val config2: SyncConfiguration = createDefaultConfig(user2, partitionValue)

        Realm.getInstance(config1).use { realm1 ->
            Realm.getInstance(config2).use { realm2 ->
                for (expectedRealmAny in expectedRealmAnyValues) {
                    realm1.executeTransaction {
                        expectedRealmObject = realm1.copyToRealmOrUpdate(expectedRealmObject)
                        expectedRealmList.add(expectedRealmObject)
                        expectedRealmDict["key"] = expectedRealmObject

                        // Populate object to round-trip
                        val syncObject = SyncAllTypes().apply {
                            id = primaryKeyValue

                            RealmFieldType.values().map { realmFieldType ->
                                when (realmFieldType) {
                                    RealmFieldType.INTEGER -> {
                                        columnLong = expectedLong
                                        // MutableRealmInteger
                                        columnRealmInteger.set(expectedRealmInteger)
                                    }
                                    RealmFieldType.BOOLEAN -> isColumnBoolean = expectedBoolean
                                    RealmFieldType.STRING -> columnString = expectedString
                                    RealmFieldType.BINARY -> columnBinary = expectedBinary
                                    RealmFieldType.DATE -> columnDate = expectedDate
                                    RealmFieldType.DOUBLE -> columnDouble = expectedDouble
                                    RealmFieldType.FLOAT -> columnFloat = expectedFloat
                                    RealmFieldType.OBJECT -> columnRealmObject = expectedRealmObject
                                    RealmFieldType.DECIMAL128 -> columnDecimal128 = expectedDecimal128
                                    RealmFieldType.OBJECT_ID -> columnObjectId = expectedObjectId
                                    RealmFieldType.UUID -> columnUUID = expectedUUID
                                    RealmFieldType.MIXED -> columnRealmAny = expectedRealmAny
                                    RealmFieldType.LIST -> columnRealmList = expectedRealmList
                                    RealmFieldType.INTEGER_LIST -> columnLongList = expectedLongList
                                    RealmFieldType.BOOLEAN_LIST -> columnBooleanList = expectedBooleanList
                                    RealmFieldType.STRING_LIST -> columnStringList = expectedStringList
                                    RealmFieldType.BINARY_LIST -> columnBinaryList = expectedBinaryList
                                    RealmFieldType.DATE_LIST -> columnDateList = expectedDateList
                                    RealmFieldType.DOUBLE_LIST -> columnDoubleList = expectedDoubleList
                                    RealmFieldType.FLOAT_LIST -> columnFloatList = expectedFloatList
                                    RealmFieldType.DECIMAL128_LIST -> columnDecimal128List = expectedDecimal128List
                                    RealmFieldType.OBJECT_ID_LIST -> columnObjectIdList = expectedObjectIdList
                                    RealmFieldType.UUID_LIST -> columnUUIDList = expectedUUIDList
                                    RealmFieldType.MIXED_LIST -> columnRealmAnyList = expectedRealmAnyList
                                    RealmFieldType.STRING_TO_INTEGER_MAP -> columnLongDictionary = expectedLongDict
                                    RealmFieldType.STRING_TO_BOOLEAN_MAP -> columnBooleanDictionary = expectedBooleanDict
                                    RealmFieldType.STRING_TO_STRING_MAP -> columnStringDictionary = expectedStringDict
                                    RealmFieldType.STRING_TO_BINARY_MAP -> columnBinaryDictionary = expectedBinaryDict
                                    RealmFieldType.STRING_TO_DATE_MAP -> columnDateDictionary = expectedDateDict
                                    RealmFieldType.STRING_TO_DOUBLE_MAP -> columnDoubleDictionary = expectedDoubleDict
                                    RealmFieldType.STRING_TO_FLOAT_MAP -> columnFloatDictionary = expectedFloatDict
                                    RealmFieldType.STRING_TO_DECIMAL128_MAP -> columnDecimal128Dictionary = expectedDecimal128Dict
                                    RealmFieldType.STRING_TO_OBJECT_ID_MAP -> columnObjectIdDictionary = expectedObjectIdDict
                                    RealmFieldType.STRING_TO_UUID_MAP -> columnUUIDDictionary = expectedUUIDDict
                                    RealmFieldType.STRING_TO_MIXED_MAP -> {
                                        expectedRealmAnyDict["key"] = expectedRealmAny
                                        columnRealmAnyDictionary = expectedRealmAnyDict
                                    }
                                    RealmFieldType.STRING_TO_LINK_MAP -> columnRealmDictionary = expectedRealmDict
                                    RealmFieldType.LINK_SET -> columnRealmSet = expectedRealmSet
                                    RealmFieldType.INTEGER_SET -> columnLongSet = expectedLongSet
                                    RealmFieldType.BOOLEAN_SET -> columnBooleanSet = expectedBooleanSet
                                    RealmFieldType.STRING_SET -> columnStringSet = expectedStringSet
                                    RealmFieldType.BINARY_SET -> columnBinarySet = expectedBinarySet
                                    RealmFieldType.DATE_SET -> columnDateSet = expectedDateSet
                                    RealmFieldType.DOUBLE_SET -> columnDoubleSet = expectedDoubleSet
                                    RealmFieldType.FLOAT_SET -> columnFloatSet = expectedFloatSet
                                    RealmFieldType.DECIMAL128_SET -> columnDecimal128Set = expectedDecimal128Set
                                    RealmFieldType.OBJECT_ID_SET -> columnObjectIdSet = expectedObjectIdSet
                                    RealmFieldType.UUID_SET -> columnUUIDSet = expectedUUIDSet
                                    RealmFieldType.MIXED_SET -> columnRealmAnySet = expectedRealmAnySet
                                    RealmFieldType.LINKING_OBJECTS,     // Nothing to set
                                    RealmFieldType.TYPED_LINK          // Not an actual exposed type, it is used internally by RealmAny
                                    -> {}
                                }
                            }
                        }

                        realm1.copyToRealmOrUpdate(syncObject)
                    }
                    realm1.syncSession.uploadAllLocalChanges()

                    assertEquals(1, realm1.where<SyncAllTypes>().count())

                    realm2.syncSession.downloadAllServerChanges(TestHelper.STANDARD_WAIT_SECS.toLong(), TimeUnit.SECONDS).let {
                        if (!it) fail()
                    }
                    realm2.refresh()

                    assertEquals(1, realm2.where<SyncAllTypes>().count())

                    // Validate that after a round-trip the values are the initial ones, the expected values
                    realm2.where<SyncAllTypes>().findFirst()!!.let { syncAllTypes ->
                        assertEquals(primaryKeyValue, syncAllTypes.id)

                        RealmFieldType.values().map { realmFieldType ->
                            when (realmFieldType) {
                                RealmFieldType.INTEGER -> {
                                    assertEquals(expectedLong, syncAllTypes.columnLong)
                                    // MutableRealmInteger
                                    assertEquals(expectedRealmInteger, syncAllTypes.columnRealmInteger.get())
                                }
                                RealmFieldType.BOOLEAN -> assertEquals(expectedBoolean, syncAllTypes.isColumnBoolean)
                                RealmFieldType.STRING -> assertEquals(expectedString, syncAllTypes.columnString)
                                RealmFieldType.BINARY -> assertTrue(expectedBinary.contentEquals(syncAllTypes.columnBinary))
                                RealmFieldType.DATE -> assertEquals(expectedDate, syncAllTypes.columnDate)
                                RealmFieldType.DOUBLE -> assertEquals(expectedDouble, syncAllTypes.columnDouble)
                                RealmFieldType.OBJECT -> assertEquals(expectedObjectId, syncAllTypes.columnRealmObject!!.id)
                                RealmFieldType.DECIMAL128 -> assertEquals(expectedDecimal128, syncAllTypes.columnDecimal128)
                                RealmFieldType.OBJECT_ID -> assertEquals(expectedObjectId, syncAllTypes.columnObjectId)
                                RealmFieldType.UUID -> assertEquals(expectedUUID, syncAllTypes.columnUUID)
                                RealmFieldType.MIXED -> assertEquals(expectedRealmAny, syncAllTypes.columnRealmAny)
                                RealmFieldType.LIST -> assertEquals(expectedObjectId, syncAllTypes.columnRealmList.first()!!.id)
                                RealmFieldType.INTEGER_LIST -> assertEquals(expectedLongList, syncAllTypes.columnLongList)
                                RealmFieldType.BOOLEAN_LIST -> assertEquals(expectedBooleanList, syncAllTypes.columnBooleanList)
                                RealmFieldType.STRING_LIST -> assertEquals(expectedStringList, syncAllTypes.columnStringList)
                                RealmFieldType.BINARY_LIST -> {
                                    expectedBinaryList.forEachIndexed { index, bytes ->
                                        Arrays.equals(bytes, syncAllTypes.columnBinaryList[index])
                                    }
                                }
                                RealmFieldType.DATE_LIST -> assertEquals(expectedDateList, syncAllTypes.columnDateList)
                                RealmFieldType.DOUBLE_LIST -> assertEquals(expectedDoubleList, syncAllTypes.columnDoubleList)
                                RealmFieldType.DECIMAL128_LIST -> assertEquals(expectedDecimal128List, syncAllTypes.columnDecimal128List)
                                RealmFieldType.OBJECT_ID_LIST -> assertEquals(expectedObjectIdList, syncAllTypes.columnObjectIdList)
                                RealmFieldType.UUID_LIST -> assertEquals(expectedUUIDList, syncAllTypes.columnUUIDList)
                                RealmFieldType.MIXED_LIST -> assertEquals(expectedRealmAnyList, syncAllTypes.columnRealmAnyList)
                                RealmFieldType.STRING_TO_INTEGER_MAP -> assertEquals(expectedLong, syncAllTypes.columnLongDictionary["key"])
                                RealmFieldType.STRING_TO_BOOLEAN_MAP -> assertEquals(expectedBoolean, syncAllTypes.columnBooleanDictionary["key"])
                                RealmFieldType.STRING_TO_STRING_MAP -> assertEquals(expectedString, syncAllTypes.columnStringDictionary["key"])
                                RealmFieldType.STRING_TO_BINARY_MAP -> assertTrue(Arrays.equals(expectedBinary, syncAllTypes.columnBinaryDictionary["key"]))
                                RealmFieldType.STRING_TO_DATE_MAP -> assertEquals(expectedDate, syncAllTypes.columnDateDictionary["key"])
                                RealmFieldType.STRING_TO_DOUBLE_MAP -> assertEquals(expectedDouble, syncAllTypes.columnDoubleDictionary["key"])
                                RealmFieldType.STRING_TO_DECIMAL128_MAP -> assertEquals(expectedDecimal128, syncAllTypes.columnDecimal128Dictionary["key"])
                                RealmFieldType.STRING_TO_OBJECT_ID_MAP -> assertEquals(expectedObjectId, syncAllTypes.columnObjectIdDictionary["key"])
                                RealmFieldType.STRING_TO_UUID_MAP -> assertEquals(expectedUUID, syncAllTypes.columnUUIDDictionary["key"])
                                RealmFieldType.STRING_TO_MIXED_MAP -> assertEquals(expectedRealmAny, syncAllTypes.columnRealmAnyDictionary["key"])
                                RealmFieldType.STRING_TO_LINK_MAP -> assertEquals(expectedObjectId, syncAllTypes.columnRealmDictionary["key"]!!.id)
                                RealmFieldType.INTEGER_SET -> {
                                    assertEquals(expectedLongSet.size, syncAllTypes.columnLongSet.size)
                                    expectedLongSet.forEach { value ->
                                        assertTrue(syncAllTypes.columnLongSet.contains(value))
                                    }
                                }
                                RealmFieldType.BOOLEAN_SET -> {
                                    assertEquals(expectedBooleanSet.size, syncAllTypes.columnBooleanSet.size)
                                    expectedBooleanSet.forEach { value ->
                                        assertTrue(syncAllTypes.columnBooleanSet.contains(value))
                                    }
                                }
                                RealmFieldType.STRING_SET -> {
                                    assertEquals(expectedStringSet.size, syncAllTypes.columnStringSet.size)
                                    expectedStringSet.forEach { value ->
                                        assertTrue(syncAllTypes.columnStringSet.contains(value))
                                    }
                                }
                                RealmFieldType.BINARY_SET -> {
                                    assertEquals(expectedBinarySet.size, syncAllTypes.columnBinarySet.size)
                                    expectedBinarySet.forEach { value ->
                                        assertTrue(syncAllTypes.columnBinarySet.contains(value))
                                    }
                                }
                                RealmFieldType.DATE_SET -> {
                                    assertEquals(expectedDateSet.size, syncAllTypes.columnDateSet.size)
                                    expectedDateSet.forEach { value ->
                                        assertTrue(syncAllTypes.columnDateSet.contains(value))
                                    }
                                }
                                RealmFieldType.DOUBLE_SET -> {
                                    assertEquals(expectedDoubleSet.size, syncAllTypes.columnDoubleSet.size)
                                    expectedDoubleSet.forEach { value ->
                                        assertTrue(syncAllTypes.columnDoubleSet.contains(value))
                                    }
                                }
                                RealmFieldType.DECIMAL128_SET -> {
                                    assertEquals(expectedDecimal128Set.size, syncAllTypes.columnDecimal128Set.size)
                                    expectedDecimal128Set.forEach { value ->
                                        assertTrue(syncAllTypes.columnDecimal128Set.contains(value))
                                    }
                                }
                                RealmFieldType.OBJECT_ID_SET -> {
                                    assertEquals(expectedObjectIdSet.size, syncAllTypes.columnObjectIdSet.size)
                                    expectedObjectIdSet.forEach { value ->
                                        assertTrue(syncAllTypes.columnObjectIdSet.contains(value))
                                    }
                                }
                                RealmFieldType.UUID_SET -> {
                                    assertEquals(expectedUUIDSet.size, syncAllTypes.columnUUIDSet.size)
                                    expectedUUIDSet.forEach { value ->
                                        assertTrue(syncAllTypes.columnUUIDSet.contains(value))
                                    }
                                }
                                RealmFieldType.MIXED_SET -> {
                                    assertEquals(expectedRealmAnySet.size, syncAllTypes.columnRealmAnySet.size)
                                    expectedRealmAnySet.forEach { value ->
                                        assertTrue(syncAllTypes.columnRealmAnySet.contains(value))
                                    }
                                }
                                RealmFieldType.LINK_SET -> {
                                    assertEquals(expectedRealmSet.size, syncAllTypes.columnRealmSet.size)
                                    expectedRealmSet.forEach { value ->
                                        assertTrue(syncAllTypes.columnRealmSet.contains(value))
                                    }
                                }
                                RealmFieldType.LINKING_OBJECTS -> assertEquals(primaryKeyValue, syncAllTypes.columnRealmObject!!.syncAllTypes!!.first()!!.id)
                                RealmFieldType.TYPED_LINK,          // Not an actual exposed type, it is used internally by RealmAny
                                RealmFieldType.FLOAT,               // Float is not cloud compatible yet
                                RealmFieldType.FLOAT_LIST,          // Float is not cloud compatible yet
                                RealmFieldType.STRING_TO_FLOAT_MAP  // Float is not cloud compatible yet
                                -> {}
                            }
                        }
                    }
                }
            }
        }
    }

    @Test
    // FIXME Missing test, maybe fitting better in SyncSessionTest.kt...when migrated
    @Ignore("Not implemented yet")
    fun refreshConnections() = Unit

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
