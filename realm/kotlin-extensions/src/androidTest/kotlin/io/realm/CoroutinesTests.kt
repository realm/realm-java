package io.realm

import io.realm.entities.AllTypes
import io.realm.entities.Dog
import io.realm.entities.SimpleClass
import io.realm.kotlin.*
import io.realm.log.RealmLog
import io.realm.rule.BlockingLooperThread
import io.realm.rule.TestRealmConfigurationFactory
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.test.*

@ExperimentalCoroutinesApi
class CoroutinesTests {

    @Suppress("MemberVisibilityCanPrivate")
    @Rule
    @JvmField
    val configFactory = TestRealmConfigurationFactory()

    private lateinit var configuration: RealmConfiguration
    private lateinit var testDispatcher: TestCoroutineDispatcher
    private lateinit var testScope: TestCoroutineScope
    private val looperThread = BlockingLooperThread()

    @Before
    fun setUp() {
        testDispatcher = TestCoroutineDispatcher()
        testScope = TestCoroutineScope(testDispatcher)
        configuration = configFactory.createConfiguration()
    }

    @Test
    fun realm_toFlow_emittedOnCollect() {
        val countDownLatch = CountDownLatch(1)

        val context = Dispatchers.Main
        val scope = CoroutineScope(context)

        scope.launch {
            val realmInstance = Realm.getInstance(configuration)
            realmInstance.toflow()
                    .flowOn(context)
                    .onEach { emittedRealm ->
                        assertNotNull(emittedRealm)
                        scope.cancel("Cancelling scope...")
                    }
                    .onCompletion {
                        realmInstance.close()
                        countDownLatch.countDown()
                    }
                    .collect()
        }

        TestHelper.awaitOrFail(countDownLatch)
    }

    @Test
    fun realm_toFlow_emittedOnUpdate() {
        val countDownLatch = CountDownLatch(1)

        val context = Dispatchers.Main
        val scope = CoroutineScope(context)

        scope.launch {
            val realmInstance = Realm.getInstance(configuration)
            realmInstance.toflow()
                    .flowOn(context)
                    .onEach { emittedRealm ->
                        assertNotNull(emittedRealm)

                        if (emittedRealm.isEmpty) {
                            realmInstance.beginTransaction()
                            realmInstance.createObject(AllTypes::class.java)
                            realmInstance.commitTransaction()
                        } else {
                            assertTrue(emittedRealm.where<AllTypes>().count() > 0)
                            scope.cancel("Cancelling scope...")
                        }
                    }
                    .onCompletion {
                        realmInstance.close()
                        countDownLatch.countDown()
                    }
                    .collect()
        }

        TestHelper.awaitOrFail(countDownLatch)
    }

    @Test
    fun dynamicRealm_toFlow_emittedOnCollect() {
        val countDownLatch = CountDownLatch(1)

        val context = Dispatchers.Main
        val scope = CoroutineScope(context)

        scope.launch {
            val realmInstance = DynamicRealm.getInstance(configuration)
            realmInstance.toflow()
                    .flowOn(context)
                    .onEach { emittedRealm ->
                        assertNotNull(emittedRealm)
                        scope.cancel("Cancelling scope...")
                    }
                    .onCompletion {
                        realmInstance.close()
                        countDownLatch.countDown()
                    }
                    .collect()
        }

        TestHelper.awaitOrFail(countDownLatch)
    }

    @Test
    fun dynamicRealm_toFlow_emittedOnUpdate() {
        val countDownLatch = CountDownLatch(1)

        val context = Dispatchers.Main
        val scope = CoroutineScope(context)

        // Initializes schema. DynamicRealm will not do that, so let a normal Realm create the file first.
        Realm.getInstance(configuration).close()

        scope.launch {
            val realmInstance = DynamicRealm.getInstance(configuration)
            realmInstance.toflow()
                    .flowOn(context)
                    .onEach { emittedRealm ->
                        assertNotNull(emittedRealm)

                        if (emittedRealm.isEmpty) {
                            realmInstance.beginTransaction()
                            realmInstance.createObject(AllTypes.CLASS_NAME)
                            realmInstance.commitTransaction()
                        } else {
                            assertTrue(emittedRealm.where(AllTypes.CLASS_NAME).count() > 0)
                            scope.cancel("Cancelling scope...")
                        }
                    }
                    .onCompletion {
                        realmInstance.close()
                        countDownLatch.countDown()
                    }
                    .collect()
        }

        TestHelper.awaitOrFail(countDownLatch)
    }

    @Test
    fun realmResults_toFlow_throwsOnClosed() {
        val countDownLatch = CountDownLatch(1)

        val context = Dispatchers.Main
        val scope = CoroutineScope(context)

        scope.launch {
            val realmInstance = Realm.getInstance(configuration)

            // No updates will be emitted, but at least ensure that we are not
            // triggering coroutines internal java.lang.IllegalStateException due to missing
            // 'awaitClose { yourCallbackOrListener.cancel() }', which should be used in the end of
            // a callbackFlow block.
            assertFailsWith<TimeoutCancellationException> {
                withTimeout(100) {
                    realmInstance.where<SimpleClass>()
                            .findAllAsync()
                            .toFlow()
                            .onStart {
                                realmInstance.close()
                            }.onCompletion {
                                countDownLatch.countDown()
                            }.collect()
                }
            }
        }

        TestHelper.awaitOrFail(countDownLatch)
    }

    @Test
    fun realmResults_toFlow_emittedOnCollect() {
        val countDownLatch = CountDownLatch(1)

        // TODO check this out for better testing: https://proandroiddev.com/from-rxjava-to-kotlin-flow-testing-42f1641d8433
        val context = Dispatchers.Main
        val scope = CoroutineScope(context)

        scope.launch {
            val realmInstance = Realm.getInstance(configuration)

            realmInstance.where<SimpleClass>()
                    .findAllAsync()
                    .toFlow()
                    .flowOn(context)
                    .onEach { flowResults ->
                        assertTrue(flowResults.isFrozen)
                        assertEquals(0, flowResults.size)
                        scope.cancel("Cancelling scope...")
                    }.onCompletion {
                        realmInstance.close()
                        countDownLatch.countDown()
                    }.collect()
        }

        TestHelper.awaitOrFail(countDownLatch)
    }

    @Test
    fun findAll_realmResults_toFlow_resultsEmittedAfterCollect() {
        Realm.getInstance(configuration).use { realm ->
            realm.executeTransaction { transactionRealm ->
                transactionRealm.createObject<SimpleClass>().name = "Foo"
                transactionRealm.createObject<SimpleClass>().name = "Bar"
            }
        }

        val countDownLatch = CountDownLatch(1)

        val context = Dispatchers.Main
        val scope = CoroutineScope(context)

        scope.launch {
            val realmInstance = Realm.getInstance(configuration)
            realmInstance.where<SimpleClass>()
                    .findAllAsync()
                    .toFlow()
                    .flowOn(context)
                    .onEach { flowResults ->
                        assertTrue(flowResults.isFrozen)
                        if (flowResults.size == 2) {
                            scope.cancel("Cancelling scope...")
                        }
                    }.onCompletion {
                        realmInstance.close()
                        countDownLatch.countDown()
                    }.collect()
        }

        TestHelper.awaitOrFail(countDownLatch)
    }

    @Test
    fun realmObject_findFirst_toFlow_onlyValidObjectsAreEmitted() {
        Realm.getInstance(configuration).use { realm ->
            realm.executeTransaction { transactionRealm ->
                transactionRealm.createObject<SimpleClass>().name = "Foo"
                transactionRealm.createObject<SimpleClass>().name = "Bar"
            }
        }

        val countDownLatch = CountDownLatch(1)

        val context = Dispatchers.Main
        val scope = CoroutineScope(context)

        scope.launch {
            val realmInstance = Realm.getInstance(configuration)
            realmInstance.where<SimpleClass>()
                    .findFirstAsync()
                    .toFlow()
                    .flowOn(context)
                    .onEach { flowObject ->
                        assertNotNull(flowObject)
                        assertTrue(flowObject.isFrozen())
                        assertTrue(flowObject.isValid())
                        assertEquals("Foo", flowObject.name)
                        scope.cancel("Cancelling scope...")
                    }.onCompletion {
                        realmInstance.close()
                        countDownLatch.countDown()
                    }.collect()
        }

        TestHelper.awaitOrFail(countDownLatch)
    }

    @Test
    fun realmResults_toChangesetFlow_emittedAfterCollect() {
        Realm.getInstance(configuration).use { realm ->
            realm.executeTransaction { transactionRealm ->
                transactionRealm.createObject<SimpleClass>().name = "Foo"
            }
        }

        val countDownLatch = CountDownLatch(1)

        val context = Dispatchers.Main
        val scope = CoroutineScope(context)

        scope.launch {
            val realmInstance = Realm.getInstance(configuration)
            realmInstance.where<SimpleClass>()
                    .findAllAsync()
                    .toChangesetFlow()
                    .flowOn(context)
                    .onEach { collectionChange ->
                        assertTrue(collectionChange.collection.isFrozen)

                        if (collectionChange.collection.size == 0) {
                            assertNull(collectionChange.changeset)
                        } else {
                            assertNotNull(collectionChange.changeset)
                            assertEquals(1, collectionChange.collection.size)
                            assertEquals("Foo", collectionChange.collection.first()!!.name)
                            scope.cancel("Cancelling scope...")
                        }
                    }.onCompletion {
                        realmInstance.close()
                        countDownLatch.countDown()
                    }.collect()
        }

        TestHelper.awaitOrFail(countDownLatch)
    }

    @Test
    fun realmResults_dynamicRealm_toChangesetFlow_emittedAfterCollect() {
        Realm.getInstance(configuration).use { realm ->
            realm.executeTransaction { transactionRealm ->
                transactionRealm.createObject<SimpleClass>().name = "Foo"
            }
        }

        val countDownLatch = CountDownLatch(1)

        val context = Dispatchers.Main
        val scope = CoroutineScope(context)

        scope.launch {
            val realmInstance = DynamicRealm.getInstance(configuration)
            realmInstance.where("SimpleClass")
                    .findAllAsync()
                    .toChangesetFlow()
                    .flowOn(context)
                    .onEach { collectionChange ->
                        assertTrue(collectionChange.collection.isFrozen)

                        if (collectionChange.collection.size == 0) {
                            assertNull(collectionChange.changeset)
                        } else {
                            assertNotNull(collectionChange.changeset)
                            assertEquals(1, collectionChange.collection.size)
                            assertEquals("Foo", collectionChange.collection.first()!!.getString("name"))
                            scope.cancel("Cancelling scope...")
                        }
                    }.onCompletion {
                        realmInstance.close()
                        countDownLatch.countDown()
                    }.collect()
        }

        TestHelper.awaitOrFail(countDownLatch)
    }

    @Test
    fun realmResults_toFlow_resultsCancelBeforeCollectActualResults() {
        val countDownLatch = CountDownLatch(1)

        val context = Dispatchers.Main
        val scope = CoroutineScope(context)

        scope.launch {
            val realmInstance = Realm.getInstance(configuration)
            realmInstance.where<SimpleClass>()
                    .findAllAsync()
                    .toFlow()
                    .flowOn(context)
                    .onCompletion {
                        realmInstance.close()
                        countDownLatch.countDown()
                    }.launchIn(scope)

            // Simulate asynchronous event and then cancel
            delay(100)
            scope.cancel("Cancelling")
        }

        TestHelper.awaitOrFail(countDownLatch)
    }

    @Test
    fun realmResults_toFlow_throwsDueToThreadViolation() {
        Realm.getInstance(configuration).use { realm ->
            val countDownLatch = CountDownLatch(1)

            // Get results from the test thread
            val findAll = realm.where<SimpleClass>().findAll()

            CoroutineScope(Dispatchers.Main).launch {
                assertFailsWith<IllegalStateException> {
                    // Now we are on the main thread, which means crash
                    findAll.toFlow().collect()
                    fail("toFlow() must be called from the thread that retrieved the results!")
                }
                countDownLatch.countDown()
            }
            TestHelper.awaitOrFail(countDownLatch)
        }
    }

    @Test
    fun realmResults_toFlow_multipleSubscribers() {
        val countDownLatch = CountDownLatch(2)

        val context = Dispatchers.Main
        val scope = CoroutineScope(context)

        var flow: Flow<RealmResults<SimpleClass>>?

        scope.launch {
            val realmInstance = Realm.getInstance(configuration)

            flow = realmInstance.where<SimpleClass>()
                    .findAllAsync()
                    .toFlow()

            // Subscriber 1
            flow!!.flowOn(context)
                    .onEach { flowResults ->
                        assertTrue(flowResults.isFrozen)
                        assertEquals(0, flowResults.size)
                    }.onCompletion {
                        if (countDownLatch.count > 1) {
                            countDownLatch.countDown()
                        } else {
                            realmInstance.close()
                            countDownLatch.countDown()
                        }
                    }.launchIn(scope)

            // Subscriber 2
            flow!!.flowOn(context)
                    .onEach { flowResults ->
                        assertTrue(flowResults.isFrozen)
                        assertEquals(0, flowResults.size)
                    }.onCompletion {
                        if (countDownLatch.count > 1) {
                            countDownLatch.countDown()
                        } else {
                            realmInstance.close()
                            countDownLatch.countDown()
                        }
                    }.launchIn(scope)

            // Simulate asynchronous event and then cancel
            delay(100)
            scope.cancel("Cancelling")
        }

        TestHelper.awaitOrFail(countDownLatch)
    }

    @Test
    fun dynamicRealm_realmResults_toFlow_emittedOnCollect() {
        val countDownLatch = CountDownLatch(1)

        val context = Dispatchers.Main
        val scope = CoroutineScope(context)

        // Initializes schema. DynamicRealm will not do that, so let a normal Realm create the file first.
        Realm.getInstance(configuration).close()

        scope.launch {
            val realmInstance = DynamicRealm.getInstance(configuration)

            realmInstance.where("SimpleClass")
                    .findAllAsync()
                    .toFlow()
                    .flowOn(context)
                    .onEach { flowResults ->
                        assertTrue(flowResults.isFrozen)
                        assertEquals(0, flowResults.size)
                        scope.cancel("Cancelling scope...")
                    }.onCompletion {
                        realmInstance.close()
                        countDownLatch.countDown()
                    }.collect()
        }

        TestHelper.awaitOrFail(countDownLatch)
    }

    @Test
    fun dynamicRealm_realmResults_toFlow_resultsEmittedAfterCollect() {
        Realm.getInstance(configuration).use { realm ->
            realm.executeTransaction { transactionRealm ->
                transactionRealm.createObject<SimpleClass>().name = "Foo"
                transactionRealm.createObject<SimpleClass>().name = "Bar"
            }
        }

        val countDownLatch = CountDownLatch(1)

        val context = Dispatchers.Main
        val scope = CoroutineScope(context)

        scope.launch {
            val realmInstance = DynamicRealm.getInstance(configuration)
            realmInstance.where("SimpleClass")
                    .findAllAsync()
                    .toFlow()
                    .flowOn(context)
                    .onEach { flowResults ->
                        assertTrue(flowResults.isFrozen)
                        if (flowResults.size == 2) {
                            scope.cancel("Cancelling scope...")
                        }
                    }.onCompletion {
                        realmInstance.close()
                        countDownLatch.countDown()
                    }.collect()
        }

        TestHelper.awaitOrFail(countDownLatch)
    }

    @Test
    fun realmObject_toFlow_emitObjectOnCollect() {
        val countDownLatch = CountDownLatch(1)

        val context = Dispatchers.Main
        val scope = CoroutineScope(context)

        scope.launch {
            val realmInstance = Realm.getInstance(configuration)
            realmInstance.beginTransaction()
            val obj = realmInstance.createObject<SimpleClass>()
                    .apply { name = "Foo" }
            realmInstance.commitTransaction()

            obj.toFlow()
                    .flowOn(context)
                    .onEach { flowObject ->
                        assertTrue(flowObject!!.isFrozen())
                        if (flowObject.name == "Foo") {
                            scope.cancel("Cancelling scope...")
                        }
                    }.onCompletion {
                        realmInstance.close()
                        countDownLatch.countDown()
                    }.launchIn(scope)
        }

        TestHelper.awaitOrFail(countDownLatch)
    }

    @Test
    fun realmObject_toFlow_emitObjectOnObjectUpdates() {
        val countDownLatch = CountDownLatch(1)

        val context = Dispatchers.Main
        val scope = CoroutineScope(context)

        scope.launch {
            val realmInstance = Realm.getInstance(configuration)
            realmInstance.beginTransaction()
            val obj = realmInstance.createObject<SimpleClass>()
                    .apply { name = "Foo" }
            realmInstance.commitTransaction()

            obj.toFlow()
                    .flowOn(context)
                    .onEach { flowObject ->
                        assertTrue(flowObject!!.isFrozen())

                        if (flowObject.name == "Foo") {
                            realmInstance.beginTransaction()
                            obj.name = "Bar"
                            realmInstance.commitTransaction()
                        } else {
                            assertEquals("Bar", flowObject.name)
                            scope.cancel("Cancelling scope...")
                        }
                    }.onCompletion {
                        realmInstance.close()
                        countDownLatch.countDown()
                    }.launchIn(scope)
        }

        TestHelper.awaitOrFail(countDownLatch)
    }

    @Test
    fun realmObject_toChangesetFlow_emitObjectOnObjectUpdates() {
        val countDownLatch = CountDownLatch(1)

        val context = Dispatchers.Main
        val scope = CoroutineScope(context)

        scope.launch {
            val realmInstance = Realm.getInstance(configuration)
            realmInstance.beginTransaction()
            val obj = realmInstance.createObject<SimpleClass>()
                    .apply { name = "Foo" }
            realmInstance.commitTransaction()

            obj.toChangesetFlow()
                    .flowOn(context)
                    .onEach { objectChange ->
                        assertNotNull(objectChange)
                        assertTrue(objectChange.`object`.isFrozen())

                        if (objectChange.`object`.name == "Foo") {
                            realmInstance.beginTransaction()
                            obj.name = "Bar"
                            realmInstance.commitTransaction()
                        } else {
                            assertEquals("Bar", objectChange.`object`.name)
                            scope.cancel("Cancelling scope...")
                        }
                    }.onCompletion {
                        realmInstance.close()
                        countDownLatch.countDown()
                    }.launchIn(scope)
        }

        TestHelper.awaitOrFail(countDownLatch)
    }

    @Test
    fun realmObject_toFlow_nullObjectEmitsNullFlow() {
        val countDownLatch = CountDownLatch(1)

        val context = Dispatchers.Main
        val scope = CoroutineScope(context)

        scope.launch {
            val realmInstance = Realm.getInstance(configuration)
            val obj = realmInstance.where<SimpleClass>().findFirst()

            obj.toFlow()
                    .flowOn(context)
                    .onEach {
                        assertNull(it)
                    }.onCompletion {
                        realmInstance.close()
                        countDownLatch.countDown()
                    }.launchIn(scope)
        }

        TestHelper.awaitOrFail(countDownLatch)
    }

    @Test
    fun realmList_toFlow_emitListOnCollect() {
        val countDownLatch = CountDownLatch(1)

        val context = Dispatchers.Main
        val scope = CoroutineScope(context)

        scope.launch {
            val realmInstance = Realm.getInstance(configuration)

            realmInstance.beginTransaction()
            val list = realmInstance.createObject<AllTypes>().columnRealmList
            list.add(Dog("dog"))
            realmInstance.commitTransaction()

            list.toFlow()
                    .onEach { flowList ->
                        assertTrue(flowList.isFrozen)
                        assertEquals(1, flowList.size)
                        assertEquals("dog", flowList.first()!!.name)
                        scope.cancel("Cancelling scope...")
                    }.onCompletion {
                        realmInstance.close()
                        countDownLatch.countDown()
                    }.collect()
        }

        TestHelper.awaitOrFail(countDownLatch)
    }

    @Test
    fun realmList_dynamicRealm_toFlow_emitListOnCollect() {
        val countDownLatch = CountDownLatch(1)

        val context = Dispatchers.Main
        val scope = CoroutineScope(context)

        // Initializes schema. DynamicRealm will not do that, so let a normal Realm create the file first.
        Realm.getInstance(configuration).close()

        scope.launch {
            Realm.getInstance(configuration).use { realmInstance ->
                realmInstance.beginTransaction()
                realmInstance.createObject<AllTypes>()
                        .columnRealmList
                        .apply { add(Dog("dog")) }
                realmInstance.commitTransaction()
            }

            val dynamicRealmInstance = DynamicRealm.getInstance(configuration)
            val list = dynamicRealmInstance.where(AllTypes.CLASS_NAME)
                    .findFirst()!!
                    .getList(AllTypes.FIELD_REALMLIST)
                    .freeze()

            list.toFlow()
                    .onEach { flowList ->
                        assertTrue(flowList.isFrozen)
                        assertEquals(1, flowList.size)
                        assertEquals("dog", flowList.first()!!.getString(Dog.FIELD_NAME))
                        scope.cancel("Cancelling scope...")
                    }.onCompletion {
                        dynamicRealmInstance.close()
                        countDownLatch.countDown()
                    }.collect()
        }

        TestHelper.awaitOrFail(countDownLatch)
    }

    @Test
    fun realmList_toFlow_emitListOnListUpdates() {
        val countDownLatch = CountDownLatch(1)

        val context = Dispatchers.Main
        val scope = CoroutineScope(context)

        scope.launch {
            val realmInstance = Realm.getInstance(configuration)

            realmInstance.beginTransaction()
            val list = realmInstance.createObject<AllTypes>().columnRealmList
            list.add(Dog("dog"))
            realmInstance.commitTransaction()

            list.toFlow()
                    .onEach { flowList ->
                        assertTrue(flowList.isFrozen)
                        assertEquals(1, flowList.size)

                        val dogName = flowList.first()!!.name
                        if (dogName != "doggo") {
                            // Before update we have original name
                            assertEquals("dog", flowList.first()!!.name)

                            // Now update object
                            realmInstance.beginTransaction()
                            list.first()?.apply {
                                this.name = "doggo"
                            }
                            realmInstance.commitTransaction()
                        } else {
                            assertEquals("doggo", dogName)

                            // Name has been updated, close everything
                            scope.cancel("Cancelling scope...")
                        }
                    }.onCompletion {
                        realmInstance.close()
                        countDownLatch.countDown()
                    }.launchIn(scope)
        }

        TestHelper.awaitOrFail(countDownLatch)
    }

    @Test
    fun realmList_toChangesetFlow_emitListOnListUpdates() {
        val countDownLatch = CountDownLatch(1)

        val context = Dispatchers.Main
        val scope = CoroutineScope(context)

        scope.launch {
            val realmInstance = Realm.getInstance(configuration)

            realmInstance.beginTransaction()
            val list = realmInstance.createObject<AllTypes>().columnRealmList
            list.add(Dog("dog"))
            realmInstance.commitTransaction()

            list.toChangesetFlow()
                    .onEach { collectionChange ->
                        assertTrue(collectionChange.collection.isFrozen)
                        assertEquals(1, collectionChange.collection.size)

                        val listDog = collectionChange.collection.first()!!
                        val dogName = listDog.name
                        if (dogName != "doggo") {
                            assertNull(collectionChange.changeset)

                            // Before update we have the original name
                            assertEquals("dog", dogName)

                            // Now update object
                            realmInstance.beginTransaction()
                            list.first()?.apply {
                                this.name = "doggo"
                            }
                            realmInstance.commitTransaction()
                        } else {
                            assertNotNull(collectionChange.changeset)
                            assertEquals("doggo", dogName)

                            // Name has been updated, close everything
                            scope.cancel("Cancelling scope...")
                        }
                    }.onCompletion {
                        realmInstance.close()
                        countDownLatch.countDown()
                    }.launchIn(scope)
        }

        TestHelper.awaitOrFail(countDownLatch)
    }

    @Test
    fun realmList_dynamicRealm_toFlow_emitListOnListUpdates() {
        val countDownLatch = CountDownLatch(1)

        val context = Dispatchers.Main
        val scope = CoroutineScope(context)

        // Initializes schema. DynamicRealm will not do that, so let a normal Realm create the file first.
        Realm.getInstance(configuration).close()

        scope.launch {
            val realmInstance = DynamicRealm.getInstance(configuration)

            realmInstance.beginTransaction()
            val dynamicRealmObject = realmInstance.createObject(AllTypes.CLASS_NAME)
            val dog = realmInstance.createObject("Dog")
                    .apply { setString(Dog.FIELD_NAME, "dog") }
            val list = dynamicRealmObject.getList(AllTypes.FIELD_REALMLIST)
                    .apply { add(dog) }
            realmInstance.commitTransaction()

            list.toFlow()
                    .onEach { flowList ->
                        assertTrue(flowList.isFrozen)
                        assertEquals(1, flowList.size)

                        val flowDog = flowList.first()!!
                        val dogName = flowDog.getString(Dog.FIELD_NAME)
                        if (dogName != "doggo") {
                            // Before update we have original name
                            assertEquals("dog", dogName)

                            // Now update object
                            realmInstance.beginTransaction()
                            list.first()!!.apply {
                                this.setString(Dog.FIELD_NAME, "doggo")
                            }
                            realmInstance.commitTransaction()
                        } else {
                            assertEquals("doggo", dogName)

                            // Name has been updated, close everything
                            scope.cancel("Cancelling scope...")
                        }
                    }.onCompletion {
                        realmInstance.close()
                        countDownLatch.countDown()
                    }.launchIn(scope)
        }

        TestHelper.awaitOrFail(countDownLatch)
    }

    @Test
    fun realmList_dynamicRealm_toChangesetFlow_emitListOnListUpdates() {
        val countDownLatch = CountDownLatch(1)

        val context = Dispatchers.Main
        val scope = CoroutineScope(context)

        // Initializes schema. DynamicRealm will not do that, so let a normal Realm create the file first.
        Realm.getInstance(configuration).close()

        scope.launch {
            val realmInstance = DynamicRealm.getInstance(configuration)

            realmInstance.beginTransaction()
            val dynamicRealmObject = realmInstance.createObject(AllTypes.CLASS_NAME)
            val dog = realmInstance.createObject("Dog")
                    .apply { setString(Dog.FIELD_NAME, "dog") }
            val list = dynamicRealmObject.getList(AllTypes.FIELD_REALMLIST)
                    .apply { add(dog) }
            realmInstance.commitTransaction()

            list.toChangesetFlow()
                    .onEach { collectionChange ->
                        assertTrue(collectionChange.collection.isFrozen)
                        assertEquals(1, collectionChange.collection.size)

                        val listDog = collectionChange.collection.first()!!
                        val dogName = listDog.getString(Dog.FIELD_NAME)
                        if (dogName != "doggo") {
                            assertNull(collectionChange.changeset)

                            // Before update we have the original name
                            assertEquals("dog", dogName)

                            // Now update object
                            realmInstance.beginTransaction()
                            list.first()?.apply {
                                this.setString(Dog.FIELD_NAME, "doggo")
                            }
                            realmInstance.commitTransaction()
                        } else {
                            assertNotNull(collectionChange.changeset)
                            assertEquals("doggo", dogName)

                            // Name has been updated, close everything
                            scope.cancel("Cancelling scope...")
                        }
                    }.onCompletion {
                        realmInstance.close()
                        countDownLatch.countDown()
                    }.launchIn(scope)
        }

        TestHelper.awaitOrFail(countDownLatch)
    }

    @Test
    fun realmList_flow_parentDeletionComplete() = looperThread.runBlocking {
        val realm = Realm.getInstance(configuration)
        realm.beginTransaction()
        val parent = realm.createObject<AllTypes>()
        val list = parent.columnRealmList
        list.add(Dog("dog"))
        realm.commitTransaction()

        val collectingStarted = AtomicBoolean(false)
        val context = looperThread.asDispatcher()
        val scope = CoroutineScope(context)
        scope.launch {
            // We should only emit valid lists. If the parent of the list is invalidated
            // it should close the stream gracefully resulting in onComplete being called.
            list.toFlow()
                    .onEach { assertTrue(it.isValid) }
                    .onCompletion {
                        realm.close()
                        looperThread.testComplete()
                    }.collect {
                        collectingStarted.set(true)
                    }
        }

        // Delete object holding list after stream started
        looperThread.postRunnable(object: Runnable {
            override fun run() {
                // The Coroutine might not start straight away, so busy wait for it to happen.
                if (!collectingStarted.get()) {
                    looperThread.postRunnable(this)
                } else {
                    realm.executeTransaction { parent.deleteFromRealm() }
                }
            }
        })
    }

    @Test
    fun realmList_changeSetFlow_parentDeletionComplete() = looperThread.runBlocking {
        val realm = Realm.getInstance(configuration)
        realm.beginTransaction()
        val parent = realm.createObject<AllTypes>()
        val list = parent.columnRealmList
        list.add(Dog("dog"))
        realm.commitTransaction()

        val collectingStarted = AtomicBoolean(false)
        val context = looperThread.asDispatcher()
        val scope = CoroutineScope(context)
        scope.launch {
            // We should only emit valid lists. If the parent of the list is invalidated
            // it should close the stream gracefully resulting in onComplete being called.
            list.toChangesetFlow()
                    .onEach { assertTrue(it.collection.isValid) }
                    .onCompletion {
                        realm.close()
                        looperThread.testComplete()
                    }.collect {
                        collectingStarted.set(true)
                    }
        }

        // Delete object holding list after stream started
        looperThread.postRunnable(object: Runnable {
            override fun run() {
                // The Coroutine might not start straight away, so busy wait for it to happen.
                if (!collectingStarted.get()) {
                    looperThread.postRunnable(this)
                } else {
                    realm.executeTransaction { parent.deleteFromRealm() }
                }
            }
        })
    }

    @Test
    fun dynamicRealmList_flow_parentDeletionComplete() = looperThread.runBlocking {
        Realm.getInstance(configuration).close() // Create schema
        val realm = DynamicRealm.getInstance(configuration)
        realm.beginTransaction()
        val parent = realm.createObject(AllTypes.CLASS_NAME)
        val list = parent.getList(AllTypes.FIELD_REALMLIST)
        list.add(realm.createObject(Dog.CLASS_NAME))
        realm.commitTransaction()

        val collectingStarted = AtomicBoolean(false)
        val context = looperThread.asDispatcher()
        val scope = CoroutineScope(context)
        scope.launch {
            // We should only emit valid lists. If the parent of the list is invalidated
            // it should close the stream gracefully resulting in onComplete being called.
            list.toFlow()
                    .onEach { assertTrue(it.isValid) }
                    .onCompletion {
                        realm.close()
                        looperThread.testComplete()
                    }.collect {
                        collectingStarted.set(true)
                    }
        }

        // Delete object holding list after stream started
        looperThread.postRunnable(object: Runnable {
            override fun run() {
                // The Coroutine might not start straight away, so busy wait for it to happen.
                if (!collectingStarted.get()) {
                    looperThread.postRunnable(this)
                } else {
                    realm.executeTransaction { parent.deleteFromRealm() }
                }
            }
        })
    }

    @Test
    fun dynamicRealmList_changeSetFlow_parentDeletionComplete() = looperThread.runBlocking {
        Realm.getInstance(configuration).close() // Create schema
        val realm = DynamicRealm.getInstance(configuration)
        realm.beginTransaction()
        val parent = realm.createObject(AllTypes.CLASS_NAME)
        val list = parent.getList(AllTypes.FIELD_REALMLIST)
        list.add(realm.createObject(Dog.CLASS_NAME))
        realm.commitTransaction()

        val collectingStarted = AtomicBoolean(false)
        val context = looperThread.asDispatcher()
        val scope = CoroutineScope(context)
        scope.launch {
            // We should only emit valid lists. If the parent of the list is invalidated
            // it should close the stream gracefully resulting in onComplete being called.
            list.toChangesetFlow()
                    .onEach { assertTrue(it.collection.isValid) }
                    .onCompletion {
                        realm.close()
                        looperThread.testComplete()
                    }.collect {
                        collectingStarted.set(true)
                    }
        }

        // Delete object holding list after stream started
        looperThread.postRunnable(object: Runnable {
            override fun run() {
                // The Coroutine might not start straight away, so busy wait for it to happen.
                if (!collectingStarted.get()) {
                    looperThread.postRunnable(this)
                } else {
                    realm.executeTransaction { parent.deleteFromRealm() }
                }
            }
        })
    }

    @Test
    fun realmObject_toFlow_emitsOnCollect() {
        val countDownLatch = CountDownLatch(1)

        val context = Dispatchers.Main
        val scope = CoroutineScope(context)

        scope.launch {
            val realmInstance = Realm.getInstance(configuration)
            realmInstance.beginTransaction()
            val obj = realmInstance.createObject<SimpleClass>()
                    .apply { name = "Foo" }
            realmInstance.commitTransaction()

            obj.toFlow()
                    .flowOn(context)
                    .onEach { flowObject ->
                        assertTrue(flowObject!!.isFrozen())
                        if (flowObject.name == "Foo") {
                            scope.cancel("Cancelling scope...")
                        }
                    }.onCompletion {
                        realmInstance.close()
                        countDownLatch.countDown()
                    }.collect()
        }

        TestHelper.awaitOrFail(countDownLatch)
    }

    @Test
    fun dynamicRealmObject_toFlow_emitsOnCollect() {
        val countDownLatch = CountDownLatch(1)

        val context = Dispatchers.Main
        val scope = CoroutineScope(context)

        scope.launch {
            Realm.getInstance(configuration).use { realmInstance ->
                realmInstance.executeTransaction {
                    realmInstance.createObject<AllTypes>()
                }
            }

            val dynamicRealm = DynamicRealm.getInstance(configuration)
            dynamicRealm.where(AllTypes.CLASS_NAME)
                    .findFirst()!!
                    .toFlow()
                    .flowOn(context)
                    .onEach { flowObject ->
                        assertTrue(flowObject!!.isFrozen)
                        scope.cancel("Cancelling scope...")
                    }.onCompletion {
                        dynamicRealm.close()
                        countDownLatch.countDown()
                    }.collect()
        }

        TestHelper.awaitOrFail(countDownLatch)
    }

    @Test
    fun dynamicRealmObject_toFlow_emitsOnUpdate() {
        val countDownLatch = CountDownLatch(1)

        val context = Dispatchers.Main
        val scope = CoroutineScope(context)

        // Initializes schema. DynamicRealm will not do that, so let a normal Realm create the file first.
        Realm.getInstance(configuration).close()

        scope.launch {
            val realmInstance = DynamicRealm.getInstance(configuration)
            realmInstance.beginTransaction()
            val simpleObject = realmInstance.createObject("SimpleClass")
                    .apply { setString("name", "simpleName") }
            realmInstance.commitTransaction()

            val dynamicRealm = DynamicRealm.getInstance(configuration)
            dynamicRealm.where("SimpleClass")
                    .findFirst()
                    .toFlow()
                    .flowOn(context)
                    .onEach { flowObject ->
                        assertNotNull(flowObject)
                        assertTrue(flowObject.isFrozen)

                        val name = flowObject.getString("name")
                        if (name == "simpleName") {
                            realmInstance.beginTransaction()
                            simpleObject.setString("name", "advancedName")
                            realmInstance.commitTransaction()
                        } else {
                            assertEquals("advancedName", name)
                            scope.cancel("Cancelling scope...")
                        }
                    }.onCompletion {
                        realmInstance.close()
                        dynamicRealm.close()
                        countDownLatch.countDown()
                    }.collect()
        }

        TestHelper.awaitOrFail(countDownLatch)
    }

    @Test
    fun dynamicRealmObject_toChangesetFlow_emitsOnUpdate() {
        val countDownLatch = CountDownLatch(1)

        val context = Dispatchers.Main
        val scope = CoroutineScope(context)

        // Initializes schema. DynamicRealm will not do that, so let a normal Realm create the file first.
        Realm.getInstance(configuration).close()

        scope.launch {
            val realmInstance = DynamicRealm.getInstance(configuration)
            realmInstance.beginTransaction()
            val simpleObject = realmInstance.createObject("SimpleClass")
                    .apply { setString("name", "simpleName") }
            realmInstance.commitTransaction()

            val dynamicRealm = DynamicRealm.getInstance(configuration)
            dynamicRealm.where("SimpleClass")
                    .findFirst()
                    .toChangesetFlow()
                    .flowOn(context)
                    .onEach { objectChange ->
                        assertNotNull(objectChange)
                        assertNotNull(objectChange.`object`)
                        assertTrue(objectChange.`object`.isFrozen)

                        val name = objectChange.`object`.getString("name")
                        if (name == "simpleName") {
                            assertNull(objectChange.changeset)
                            realmInstance.beginTransaction()
                            simpleObject.setString("name", "advancedName")
                            realmInstance.commitTransaction()
                        } else {
                            assertNotNull(objectChange.changeset)
                            assertEquals("advancedName", name)
                            scope.cancel("Cancelling scope...")
                        }
                    }.onCompletion {
                        realmInstance.close()
                        dynamicRealm.close()
                        countDownLatch.countDown()
                    }.collect()
        }

        TestHelper.awaitOrFail(countDownLatch)
    }

    @Test
    fun executeTransactionAwait() {
        testScope.runBlockingTest {
            Realm.getInstance(configuration).use { realmInstance ->
                assertEquals(0, realmInstance.where<SimpleClass>().findAll().size)

                realmInstance.executeTransactionAwait(testDispatcher) { transactionRealm ->
                    val simpleObject = SimpleClass().apply { name = "simpleName" }
                    transactionRealm.insert(simpleObject)
                }
                assertEquals(1, realmInstance.where<SimpleClass>().findAll().size)
            }
        }
    }

    @Test
    fun executeTransactionAwait_cancelCoroutineWithMultipleTransactions() {
        val upperBound = 10
        var realmInstance: Realm? = null

        val job = CoroutineScope(Dispatchers.Main).launch {
            realmInstance = Realm.getInstance(configuration)

            for (i in 1..upperBound) {
                realmInstance!!.executeTransactionAwait { transactionRealm ->
                    val simpleObject = SimpleClass().apply { name = "simpleName $i" }
                    transactionRealm.insert(simpleObject)
                }

                // Wait for 10 ms between inserts
                delay(10)
            }
        }

        val countDownLatch = CountDownLatch(1)
        CoroutineScope(Dispatchers.Main).launch {
            // Wait for 50 ms and cancel job so that not all planned 10 elements are inserted
            delay(50)
            job.cancelAndJoin()

            assertNotEquals(upperBound.toLong(), realmInstance!!.where<SimpleClass>().count())

            realmInstance!!.close()

            countDownLatch.countDown()
            this.cancel()
        }

        TestHelper.awaitOrFail(countDownLatch)
    }

    @Test
    fun executeTransactionAwait_cancelCoroutineWithHeavyCooperativeTransaction() {
        val upperBound = 100000
        var realmInstance: Realm? = null

        val job = CoroutineScope(Dispatchers.Main).launch {
            realmInstance = Realm.getInstance(configuration)

            realmInstance!!.executeTransactionAwait { transactionRealm ->
                // Try to insert 100000 objects to give time to be cancelled after 5ms
                for (i in 1..upperBound) {
                    // The coroutine itself will not cancel the transaction, but we can make it cooperative ourselves
                    if (isActive) {
                        val simpleObject = SimpleClass().apply { name = "simpleName $i" }
                        transactionRealm.insert(simpleObject)
                    }
                }
            }
        }

        val countDownLatch = CountDownLatch(1)
        CoroutineScope(Dispatchers.Main).launch {
            // Wait for 5 ms and cancel job
            delay(5)
            job.cancelAndJoin()

            // The coroutine won't finish until the transaction is completely done but not all
            // elements will have been inserted since the transaction is cooperative.
            // It isn't possible to guarantee we have inserted any element at all either because
            // another coroutine is launched inside executeTransactionAwait and that triggers a
            // context switching, which might result in that the call to cancelAndJoin above this
            // comment be executed even before we check for isActive inside executeTransactionAwait.
            // So the result yielded by count() will be a number from 0 to anywhere below 100000.
            assertNotEquals(upperBound.toLong(), realmInstance!!.where<SimpleClass>().count())

            realmInstance!!.close()

            countDownLatch.countDown()
            this.cancel()
        }

        TestHelper.awaitOrFail(countDownLatch)
    }

    @Test
    fun executeTransactionAwait_throwsDueToThreadViolation() {
        // Just to prevent the test to end prematurely
        val countDownLatch = CountDownLatch(1)
        var exception: IllegalStateException? = null

        Realm.getInstance(configuration).use { realm ->
            // It will crash so long we aren't using Dispatchers.Unconfined
            CoroutineScope(Dispatchers.IO).launch {
                assertFailsWith<IllegalStateException> {
                    realm.executeTransactionAwait {
                        // no-op
                    }
                }.let {
                    exception = it
                    countDownLatch.countDown()
                }
            }
            TestHelper.awaitOrFail(countDownLatch)
        }

        // Ensure we failed
        assertNotNull(exception)
        assertTrue(exception!!.message!!.contains("incorrect thread"))
    }
}
