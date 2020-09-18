package io.realm

import io.realm.entities.AllTypes
import io.realm.entities.Dog
import io.realm.entities.SimpleObjectClass
import io.realm.kotlin.executeTransactionAwait
import io.realm.kotlin.toFlow
import io.realm.kotlin.where
import io.realm.rule.TestRealmConfigurationFactory
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.junit.*
import java.lang.Exception
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
        realm.executeTransaction { it.deleteAll() }
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

            realmInstance.where(SimpleObjectClass::class.java)
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
                    }.launchIn(scope)
        }

        TestHelper.awaitOrFail(countDownLatch)
    }

    @Test
    fun toFlow_resultsEmittedAfterCollect() {
        realm.executeTransactionAsync { transactionRealm ->
            transactionRealm.createObject(SimpleObjectClass::class.java).name = "Foo"
            transactionRealm.createObject(SimpleObjectClass::class.java).name = "Bar"
        }

        val countDownLatch = CountDownLatch(1)

        val context = Dispatchers.Main
        val scope = CoroutineScope(context)

        scope.launch {
            val realmInstance = Realm.getInstance(configuration)
            realmInstance.where(SimpleObjectClass::class.java)
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
                    }.launchIn(scope)
        }

        TestHelper.awaitOrFail(countDownLatch)
    }

    @Test
    fun toFlow_resultsCancelBeforeCollect() {
        val countDownLatch = CountDownLatch(1)

        val context = Dispatchers.Main
        val scope = CoroutineScope(context)

        scope.launch {
            val realmInstance = Realm.getInstance(configuration)
            realmInstance.where(SimpleObjectClass::class.java)
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
        val findAll = realm.where<SimpleObjectClass>().findAll()

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

            flow = realmInstance.where(SimpleObjectClass::class.java)
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
    fun toFlow_emitObjectOnCollect() {
        val countDownLatch = CountDownLatch(1)

        val context = Dispatchers.Main
        val scope = CoroutineScope(context)

        scope.launch {
            val realmInstance = Realm.getInstance(configuration)
            realmInstance.beginTransaction()
            val obj = realmInstance.createObject(SimpleObjectClass::class.java)
                    .apply { name = "Foo" }
            realmInstance.commitTransaction()

            obj.toFlow()
                    .flowOn(context)
                    .onEach { flowObject ->
                        assertTrue(flowObject.isFrozen)
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
            val obj = realmInstance.createObject(SimpleObjectClass::class.java)
                    .apply { name = "Foo" }
            realmInstance.commitTransaction()

            obj.toFlow()
                    .flowOn(context)
                    .onEach { flowObject ->
                        assertTrue(flowObject.isFrozen)
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
            val list = realmInstance.createObject(AllTypes::class.java).columnRealmList
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
                    }.launchIn(scope)
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
            val list = realmInstance.createObject(AllTypes::class.java).columnRealmList
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
    fun executeTransactionAwait() {
        testScope.runBlockingTest {
            Realm.getInstance(configuration).use { realmInstance ->
                assertEquals(0, realmInstance.where<SimpleObjectClass>().findAll().size)

                realmInstance.executeTransactionAwait(testDispatcher) { transactionRealm ->
                    val simpleObject = SimpleObjectClass().apply { name = "simpleName" }
                    transactionRealm.insert(simpleObject)
                }
                assertEquals(1, realmInstance.where<SimpleObjectClass>().findAll().size)
            }
        }
    }

    @Test
    fun executeTransactionAwait_throwsDueToThreadViolation() {
        // Just to prevent the test to end prematurely
        val countDownLatch = CountDownLatch(1)
        var exception: IllegalStateException? = null

        // It will crash so long we aren't using Dispatchers.Unconfined
        CoroutineScope(Dispatchers.IO).launch {
            assertFailsWith<IllegalStateException> {
                realm.where<SimpleObjectClass>().findAll()
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
