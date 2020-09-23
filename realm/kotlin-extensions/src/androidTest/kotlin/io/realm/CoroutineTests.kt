package io.realm

import io.realm.entities.AllTypes
import io.realm.entities.Dog
import io.realm.entities.SimpleClass
import io.realm.kotlin.*
import io.realm.rule.TestRealmConfigurationFactory
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.CountDownLatch
import kotlin.test.*

@ExperimentalCoroutinesApi
class CoroutineTests {

    @Suppress("MemberVisibilityCanPrivate")
    @Rule
    @JvmField
    val configFactory = TestRealmConfigurationFactory()

    private lateinit var configuration: RealmConfiguration
    private lateinit var testDispatcher: TestCoroutineDispatcher
    private lateinit var testScope: TestCoroutineScope
    private lateinit var realm: Realm

    @Before
    fun setUp() {
        testDispatcher = TestCoroutineDispatcher()
        testScope = TestCoroutineScope(testDispatcher)
        configuration = configFactory.createConfiguration()
        realm = Realm.getInstance(configuration)
    }

    @After
    fun tearDown() {
        realm.close()
    }

    @Test
    fun toFlow_emittedOnCollect() {
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
    fun toFlow_resultsEmittedAfterCollect() {
        realm.executeTransactionAsync { transactionRealm ->
            transactionRealm.createObject<SimpleClass>().name = "Foo"
            transactionRealm.createObject<SimpleClass>().name = "Bar"
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
    fun toFlow_resultsCancelBeforeCollectActualResults() {
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
    fun toFlow_throwsDueToThreadViolation() {
        val countDownLatch = CountDownLatch(1)

        // Get results from the test thread
        val findAll = realm.where<SimpleClass>().findAll()

        CoroutineScope(Dispatchers.Main).launch {
            assertFailsWith<IllegalStateException> {
                // Now we are on the main thread, which means crash
                findAll.toFlow()
                fail("toFlow() must be called from the thread that retrieved the results!")
            }
            countDownLatch.countDown()
        }
        TestHelper.awaitOrFail(countDownLatch)
    }

    @Test
    fun toFlow_throwsDueToBeingCancelled() {
        val countDownLatch = CountDownLatch(1)

        val context = Dispatchers.Main
        val scope = CoroutineScope(context)

        var flow: Flow<*>? = null

        scope.launch {
            val realmInstance = Realm.getInstance(configuration)

            flow = realmInstance.where<SimpleClass>()
                    .findAllAsync()
                    .toFlow()

            flow!!.flowOn(context)
                    .onCompletion {
                        realmInstance.close()
                        countDownLatch.countDown()
                    }.launchIn(scope)

            // Simulate asynchronous event and then cancel
            delay(100)
            scope.cancel("Cancelling")
        }

        TestHelper.awaitOrFail(countDownLatch)

        // Throws when re-subscribing to an already-cancelled flow
        val errorLatch = CountDownLatch(1)
        CoroutineScope(context).launch {
            assertFailsWith<IllegalStateException> { flow!!.collect() }
            errorLatch.countDown()
        }
        TestHelper.awaitOrFail(errorLatch)
    }

    @Test
    fun toFlow_multipleSubscribers() {
        val countDownLatch = CountDownLatch(2)

        val context = Dispatchers.Main
        val scope = CoroutineScope(context)

        var flow: Flow<RealmResults<SimpleClass>>? = null

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
                        countDownLatch.countDown()

                        if (countDownLatch.count == 0L && !realmInstance.isClosed) {
                            realmInstance.close()
                        }
                    }.launchIn(scope)

            // Subscriber 2
            flow!!.flowOn(context)
                    .onEach { flowResults ->
                        assertTrue(flowResults.isFrozen)
                        assertEquals(0, flowResults.size)
                    }.onCompletion {
                        countDownLatch.countDown()

                        if (countDownLatch.count == 0L && !realmInstance.isClosed) {
                            realmInstance.close()
                        }
                    }.launchIn(scope)

            // Simulate asynchronous event and then cancel
            delay(100)
            scope.cancel("Cancelling")
        }

        TestHelper.awaitOrFail(countDownLatch)
    }

    @Test
    fun toFlow_emitObjectOnCollect() {
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
                        assertTrue(flowObject.isFrozen())
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
    fun toFlow_emitObjectOnObjectUpdates() {
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
                        assertTrue(flowObject.isFrozen())
                        if (flowObject.name == "Bar") {
                            scope.cancel("Cancelling scope...")
                        }
                    }.onCompletion {
                        realmInstance.close()
                        countDownLatch.countDown()
                    }.launchIn(scope)

            // Simulate asynchronous event and then update object
            delay(100)
            realmInstance.beginTransaction()
            obj.name = "Bar"
            realmInstance.commitTransaction()
        }

        TestHelper.awaitOrFail(countDownLatch)
    }

    @Test
    fun toFlow_emitListOnCollect() {
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
    fun toFlow_emitListOnListUpdates() {
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
                        } else {
                            // Name has been updated, close everything
                            scope.cancel("Cancelling scope...")
                        }
                    }.onCompletion {
                        realmInstance.close()
                        countDownLatch.countDown()
                    }.launchIn(scope)

            // Simulate asynchronous event and then update list
            delay(100)
            realmInstance.beginTransaction()
            list.first()?.apply {
                this.name = "doggo"
            }
            realmInstance.commitTransaction()
        }

        TestHelper.awaitOrFail(countDownLatch)
    }

    @Test
    fun toFlow_realmModel_emitsOnCollect() {
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
                        assertTrue(flowObject.isFrozen())
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
    fun toFlow_dynamicRealmObject_emitsOnCollect() {
        val countDownLatch = CountDownLatch(1)

        val context = Dispatchers.Main
        val scope = CoroutineScope(context)

        scope.launch {
            val realmInstance = Realm.getInstance(configuration)
            realmInstance.beginTransaction()
            realmInstance.createObject<AllTypes>()
            realmInstance.commitTransaction()

            val dynamicRealm = DynamicRealm.getInstance(configuration)
            dynamicRealm.where(AllTypes.CLASS_NAME)
                    .findFirst()!!
                    .toFlow()
                    .flowOn(context)
                    .onEach { flowObject ->
                        assertTrue(flowObject.isFrozen)
                        scope.cancel("Cancelling scope...")
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
    fun executeTransactionAwait_cancel() {
        var realmInstance: Realm? = null

        val mainScope = CoroutineScope(Dispatchers.Main)
        mainScope.launch {
            realmInstance = Realm.getInstance(configuration)

            for (i in 1..100) {
                realmInstance!!.executeTransactionAwait { transactionRealm ->
                    val simpleObject = SimpleClass().apply { name = "simpleName $i" }
                    transactionRealm.insert(simpleObject)
                }
                delay(100)
            }
        }

        val countDownLatch = CountDownLatch(1)
        val newMainScope = CoroutineScope(Dispatchers.Main)
        newMainScope.launch {
            // Wait for 150 ms and cancel scope so that only two elements are inserted
            delay(150)
            mainScope.cancel("Cancelling")

            assertEquals(2, realmInstance!!.where<SimpleClass>().count())

            realmInstance!!.close()
            countDownLatch.countDown()
            newMainScope.cancel()
        }

        TestHelper.awaitOrFail(countDownLatch)
    }

    @Test
    fun executeTransactionAwait_throwsDueToThreadViolation() {
        // Just to prevent the test to end prematurely
        val countDownLatch = CountDownLatch(1)
        var exception: IllegalStateException? = null

        // It will crash so long we aren't using Dispatchers.Unconfined
        CoroutineScope(Dispatchers.IO).launch {
            assertFailsWith<IllegalStateException> {
                realm.where<SimpleClass>().findAll()
            }.let {
                exception = it
                countDownLatch.countDown()
            }
        }
        TestHelper.awaitOrFail(countDownLatch)

        // Ensure we failed
        assertNotNull(exception)
    }
}
