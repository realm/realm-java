package io.realm

import io.realm.entities.SimpleClass
import io.realm.kotlin.executeTransactionAwait
import io.realm.kotlin.toFlow
import io.realm.rule.BlockingLooperThread
import io.realm.rule.TestRealmConfigurationFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.CountDownLatch
import kotlin.test.*

class CoroutineTests {

    @Suppress("MemberVisibilityCanPrivate")
    @Rule
    @JvmField
    val configFactory = TestRealmConfigurationFactory()

    private lateinit var testDispatcher: TestCoroutineDispatcher
    private lateinit var testScope: TestCoroutineScope
    private lateinit var realm: Realm

    private val looperThread = BlockingLooperThread()

    @Before
    fun setUp() {
        realm = Realm.getInstance(configFactory.createConfiguration())

        with(Realm.getDefaultInstance()) {
            executeTransaction { it.deleteAll() }
            close()
        }

        testDispatcher = TestCoroutineDispatcher()
        testScope = TestCoroutineScope(testDispatcher)
    }

    @After
    fun tearDown() {
        realm.executeTransaction { it.deleteAll() }
        realm.close()
    }

    @Test
    fun toFlow_emittedOnCollect() {
        Realm.getDefaultInstance().use { defaultRealm ->
            defaultRealm.executeTransactionAsync {
                it.createObject(SimpleClass::class.java).name = "Foo"
                it.createObject(SimpleClass::class.java).name = "Bar"
            }
        }

        val countDownLatch = CountDownLatch(1)

        // TODO check this out for better testing: https://proandroiddev.com/from-rxjava-to-kotlin-flow-testing-42f1641d8433
        val scope = CoroutineScope(Dispatchers.Main)
        scope.launch {
            Realm.getDefaultInstance()
                    .where(SimpleClass::class.java)
                    .findAll()
                    .toFlow()
                    .onEach { flowResults ->
                        assertTrue(flowResults.isFrozen)
                        if (flowResults.size == 2) {
                            scope.cancel()
                        }
                    }.onCompletion {
                        countDownLatch.countDown()
                    }.launchIn(scope)
        }
        countDownLatch.await()
    }

    @Test
    fun toFlow_throwsDueToThreadViolation() {
        val countDownLatch = CountDownLatch(1)

        // Get results from the test thread
        val findAll = Realm.getDefaultInstance()
                .where(SimpleClass::class.java)
                .findAll()

        CoroutineScope(Dispatchers.Main).launch {
            assertFailsWith<IllegalStateException> {
                // Now we are on the main thread, which means crash
                findAll.toFlow().collect()
                fail("toFlow() must be called from the thread that retrieved the results!")
            }
            countDownLatch.countDown()
        }
        countDownLatch.await()
    }

    @Test
    fun executeTransactionAwait() {
        testScope.runBlockingTest {
            Realm.getDefaultInstance().use { defaultRealm ->
                assertEquals(0, defaultRealm.where(SimpleClass::class.java).findAll().size)

                defaultRealm.executeTransactionAwait(testDispatcher) { transactionRealm ->
                    val simpleObject = SimpleClass().apply { name = "simpleName" }
                    transactionRealm.insert(simpleObject)
                }
            }
        }
        assertEquals(1, Realm.getDefaultInstance().where(SimpleClass::class.java).findAll().size)
    }

    @Test
    fun executeTransactionAwait_throwsDueToThreadViolation() {
        // Just to prevent the test to end prematurely
        val countDownLatch = CountDownLatch(1)

        var exception: IllegalStateException? = null

        // Obtain a Realm from a thread other than the one used in the coroutine context
        val defaultRealm = Realm.getDefaultInstance()

        // It will crash so long we aren't using Dispatchers.Unconfined
        CoroutineScope(Dispatchers.IO).launch {
            assertFailsWith<IllegalStateException> {
                defaultRealm.where(SimpleClass::class.java).findAll()
                countDownLatch.countDown()
            }.let {
                exception = it
            }
        }
        countDownLatch.await()

        // Ensure we failed
        assertNotNull(exception)
    }
}
